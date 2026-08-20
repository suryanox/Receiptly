from __future__ import annotations

import asyncio
import base64
import logging
import mimetypes

import httpx
import litellm

from reciply_ocr.config import OpenRouterSettings
from reciply_ocr.invoice_schema import Invoice, response_format
from reciply_ocr.prompts import SYSTEM_PROMPT, build_user_prompt

logger = logging.getLogger("reciply_ocr.openrouter")

OPENROUTER_API_BASE = "https://openrouter.ai/api/v1"
_OPENROUTER_MODELS_URL = "https://openrouter.ai/api/v1/models"
_MAX_RETRIES = 3
_BASE_BACKOFF_SECONDS = 2.0


class InvoiceExtractionError(RuntimeError):
    """Raised when a receipt could not be turned into an Invoice."""


class OpenRouterExtractor:
    """Extracts a structured Invoice from a receipt image via OpenRouter's LLM API."""

    def __init__(self, settings: OpenRouterSettings) -> None:
        self._api_key = settings.api_key
        self._model_override = settings.model
        self._free_models: list[str] | None = None

    async def extract(self, image_path: str, ocr_text: str) -> Invoice:
        model = await self._select_model()
        logger.info("Calling OpenRouter model %s for invoice extraction", model)

        messages = [
            {"role": "system", "content": SYSTEM_PROMPT},
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": build_user_prompt(ocr_text)},
                    {"type": "image_url", "image_url": {"url": _encode_image(image_path)}},
                ],
            },
        ]

        last_error: Exception | None = None
        for attempt in range(1, _MAX_RETRIES + 1):
            try:
                response = await litellm.acompletion(
                    model=f"openrouter/{model}",
                    messages=messages,
                    api_key=self._api_key,
                    api_base=OPENROUTER_API_BASE,
                    temperature=0,
                    response_format=response_format(),
                )
                content = response.choices[0].message.content or ""
                return Invoice.model_validate_json(content.strip())
            except asyncio.CancelledError:
                raise
            except Exception as exc:  # noqa: BLE001 - retry any transient upstream failure
                last_error = exc
                logger.warning(
                    "Invoice extraction attempt %d/%d failed: %s",
                    attempt,
                    _MAX_RETRIES,
                    exc,
                )
                if attempt < _MAX_RETRIES:
                    await asyncio.sleep(_BASE_BACKOFF_SECONDS * (2 ** (attempt - 1)))

        raise InvoiceExtractionError("Failed to extract invoice after retries") from last_error

    async def _select_model(self) -> str:
        if self._model_override:
            return self._model_override
        models = await self._list_free_models()
        if not models:
            raise InvoiceExtractionError("No free vision+text models available on OpenRouter")
        selected = models[0]
        logger.info("Auto-selected OpenRouter model: %s", selected)
        return selected

    async def _list_free_models(self) -> list[str]:
        if self._free_models is not None:
            return self._free_models

        async with httpx.AsyncClient(timeout=30) as client:
            response = await client.get(
                _OPENROUTER_MODELS_URL,
                headers={"Authorization": f"Bearer {self._api_key}"},
            )
            response.raise_for_status()
            payload = response.json()

        models = [model["id"] for model in payload.get("data", []) if _is_free_vision_model(model)]
        self._free_models = models
        logger.info("Found %d free vision+text models on OpenRouter", len(models))
        return models


def _is_free_vision_model(model: dict) -> bool:
    pricing = model.get("pricing", {})
    prompt_price = float(pricing.get("prompt", "0") or 0)
    completion_price = float(pricing.get("completion", "0") or 0)
    modalities = model.get("architecture", {}).get("input_modalities", [])
    return (
        prompt_price == 0
        and completion_price == 0
        and "image" in modalities
        and "text" in modalities
    )


def _encode_image(image_path: str) -> str:
    mime, _ = mimetypes.guess_type(image_path)
    mime = mime or "image/jpeg"
    with open(image_path, "rb") as file:
        encoded = base64.b64encode(file.read()).decode("ascii")
    return f"data:{mime};base64,{encoded}"
