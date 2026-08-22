from __future__ import annotations

import asyncio
import logging
import os
import tempfile

from reciply_ocr.invoice_repository import InvoiceRepository
from reciply_ocr.models import OcrLine, ReceiptStatus
from reciply_ocr.ocr import OcrEngine
from reciply_ocr.llmclient import OpenRouterExtractor
from reciply_ocr.repository import ReceiptRepository
from reciply_ocr.telegram_client import TelegramClient

logger = logging.getLogger("reciply_ocr.processor")


class ReceiptProcessor:
    """Orchestrates the end-to-end receipt pipeline for a single receipt."""

    def __init__(
        self,
        receipts: ReceiptRepository,
        invoices: InvoiceRepository,
        telegram: TelegramClient,
        ocr_engine: OcrEngine,
        extractor: OpenRouterExtractor,
    ) -> None:
        self._receipts = receipts
        self._invoices = invoices
        self._telegram = telegram
        self._ocr = ocr_engine
        self._extractor = extractor

    async def process_next(self) -> bool:
        """Process the next claimed receipt. Returns True if a receipt was handled."""
        pending = await asyncio.to_thread(self._receipts.claim_next_pending)
        if pending is None:
            return False

        try:
            await self._process(pending.id, pending.image_file_id)
        except Exception:  # noqa: BLE001 - a failing receipt must not kill the loop
            logger.exception("Processing failed for receipt id=%s", pending.id)
            await asyncio.to_thread(self._receipts.update_status, pending.id, ReceiptStatus.FAILED)
        return True

    async def _process(self, receipt_id: int, image_file_id: str) -> None:
        with tempfile.TemporaryDirectory() as tmp_dir:
            image_path = os.path.join(tmp_dir, "receipt.jpg")
            logger.info("Processing receipt id=%s", receipt_id)

            await self._telegram.download(image_file_id, image_path)
            lines = await asyncio.to_thread(self._ocr.extract, image_path)

            if not lines:
                logger.info("Receipt id=%s has no detectable text", receipt_id)
                await asyncio.to_thread(
                    self._receipts.update_status, receipt_id, ReceiptStatus.INVALID_IMAGE
                )
                return

            await self._extract_and_store(receipt_id, image_path, lines)
            await asyncio.to_thread(
                self._receipts.update_status, receipt_id, ReceiptStatus.INVOICE_CREATED
            )
            logger.info("Receipt id=%s invoice created", receipt_id)

    async def _extract_and_store(
        self, receipt_id: int, image_path: str, lines: list[OcrLine]
    ) -> None:
        ocr_text = "\n".join(line.text for line in lines)
        invoice = await self._extractor.extract(image_path, ocr_text)
        logger.info("Receipt id=%s extracted invoice: %s", receipt_id, invoice.model_dump())
        await asyncio.to_thread(self._invoices.save, receipt_id, invoice)
