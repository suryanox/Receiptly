import base64
import json
import logging
import mimetypes

import httpx
import litellm

from reciply_ocr.invoice_schema import FIELD_NAMES, schema_description

logger = logging.getLogger("reciply_ocr.openrouter")

OPENROUTER_MODELS_URL = "https://openrouter.ai/api/v1/models"
OPENROUTER_API_BASE = "https://openrouter.ai/api/v1"

_SYSTEM_PROMPT = (
    "You are a precise data-extraction assistant. You are given a photo of an invoice/receipt "
    "and the raw OCR text extracted from it. Fill the provided JSON schema using ONLY information "
    "that appears in the document. Use null for any field that is not present. "
    "Return ONLY a single valid JSON object and nothing else."
)


def _encode_image(image_path: str) -> str:
    mime, _ = mimetypes.guess_type(image_path)
    mime = mime or "image/jpeg"
    with open(image_path, "rb") as f:
        b64 = base64.b64encode(f.read()).decode("ascii")
    return f"data:{mime};base64,{b64}"


def _build_user_prompt(ocr_text: str) -> str:
    return (
        "Extract the invoice/receipt data and return it as a JSON object matching this schema:\n\n"
        f"{schema_description()}\n\n"
        "OCR text from the document:\n"
        "----\n"
        f"{ocr_text}\n"
        "----\n\n"
        "Respond with ONLY the JSON object."
    )


class OpenRouterClient:
    def __init__(self, api_key: str, model: str | None = None):
        self._api_key = api_key
        self._model_override = model
        self._free_models: list[str] | None = None

    async def list_free_vision_models(self) -> list[str]:
        if self._free_models is not None:
            return self._free_models

        async with httpx.AsyncClient(timeout=30) as client:
            resp = await client.get(
                OPENROUTER_MODELS_URL,
                headers={"Authorization": f"Bearer {self._api_key}"},
            )
            resp.raise_for_status()
            payload = resp.json()

        models: list[str] = []
        for m in payload.get("data", []):
            pricing = m.get("pricing", {})
            prompt_price = float(pricing.get("prompt", "0") or 0)
            completion_price = float(pricing.get("completion", "0") or 0)
            modalities = m.get("architecture", {}).get("input_modalities", [])
            if prompt_price == 0 and completion_price == 0:
                if "image" in modalities and "text" in modalities:
                    models.append(m["id"])

        self._free_models = models
        logger.info("Found %d free vision+text models on OpenRouter", len(models))
        return models

    async def select_model(self) -> str:
        if self._model_override:
            return self._model_override
        models = await self.list_free_vision_models()
        if not models:
            raise RuntimeError("No free vision+text models available on OpenRouter")
        logger.info("Using OpenRouter model: %s", models[0])
        return models[0]

    async def extract_invoice(self, image_path: str, ocr_text: str) -> dict:
        model = await self.select_model()
        user_content = [
            {"type": "text", "text": _build_user_prompt(ocr_text)},
            {"type": "image_url", "image_url": {"url": _encode_image(image_path)}},
        ]
        messages = [
            {"role": "system", "content": _SYSTEM_PROMPT},
            {"role": "user", "content": user_content},
        ]

        logger.info("Calling OpenRouter model %s for invoice extraction", model)
        response = await litellm.acompletion(
            model=f"openrouter/{model}",
            messages=messages,
            api_key=self._api_key,
            api_base=OPENROUTER_API_BASE,
            temperature=0,
        )

        content = response.choices[0].message.content or ""
        return _parse_invoice_json(content)


def _parse_invoice_json(content: str) -> dict:
    cleaned = content.strip()
    if cleaned.startswith("```"):
        cleaned = cleaned.strip("`")
        if cleaned.lstrip().lower().startswith("json"):
            cleaned = cleaned.lstrip()[4:]
        cleaned = cleaned.strip()
    start = cleaned.find("{")
    end = cleaned.rfind("}")
    if start != -1 and end != -1 and end > start:
        cleaned = cleaned[start : end + 1]

    data = json.loads(cleaned)

    invoice: dict = {}
    for name in FIELD_NAMES:
        invoice[name] = data.get(name)
    return invoice
