import json
import logging
import os
import tempfile

from reciply_ocr.ocr import run_ocr
from reciply_ocr.openrouter import OpenRouterClient
from reciply_ocr.repository import ReceiptRepository
from reciply_ocr.telegram_client import TelegramClient

logger = logging.getLogger("reciply_ocr.worker")


def _ocr_text(ocr_result: list[dict]) -> str:
    return "\n".join(line["text"] for line in ocr_result)


async def process_once(repo: ReceiptRepository, tg: TelegramClient, or_client: OpenRouterClient):
    claimed = repo.claim_pending()
    if claimed is None:
        logger.debug("No pending receipts")
        return

    receipt_id, file_id = claimed
    logger.info("Processing receipt id=%s", receipt_id)

    tmp_path = None
    try:
        with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp:
            tmp_path = tmp.name

        await tg.download(file_id, tmp_path)

        ocr_result = run_ocr(tmp_path)

        if not ocr_result:
            logger.info("Receipt id=%s has no detectable text, marking INVALID_IMAGE", receipt_id)
            repo.update_status(receipt_id, "INVALID_IMAGE")
            return

        logger.info(
            "Receipt id=%s OCR found %d line(s)", receipt_id, len(ocr_result)
        )
        for i, line in enumerate(ocr_result):
            logger.info(
                "  line %d: text=%r confidence=%.4f",
                i,
                line["text"],
                line["confidence"],
            )

        ocr_text = _ocr_text(ocr_result)
        invoice = await or_client.extract_invoice(tmp_path, ocr_text)

        logger.info(
            "Receipt id=%s extracted invoice: %s",
            receipt_id,
            json.dumps(invoice.model_dump(), ensure_ascii=False),
        )

        repo.save_invoice(receipt_id, invoice.model_dump())
        repo.update_status(receipt_id, "INVOICE_CREATED")
        logger.info("Receipt id=%s invoice created", receipt_id)
    except Exception:
        logger.exception("Processing failed for receipt id=%s", receipt_id)
        repo.update_status(receipt_id, "FAILED")
    finally:
        if tmp_path and os.path.exists(tmp_path):
            os.remove(tmp_path)
