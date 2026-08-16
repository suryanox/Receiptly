import json
import logging
import os
import tempfile

from reciply_ocr.ocr import run_ocr
from reciply_ocr.repository import ReceiptRepository
from reciply_ocr.telegram_client import TelegramClient

logger = logging.getLogger("reciply_ocr.worker")


async def process_once(repo: ReceiptRepository, tg: TelegramClient):
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

        logger.info(
            "Receipt id=%s OCR found %d line(s): %s",
            receipt_id,
            len(ocr_result),
            json.dumps(ocr_result, ensure_ascii=False),
        )
        for i, line in enumerate(ocr_result):
            logger.info(
                "  line %d: text=%r confidence=%.4f",
                i,
                line["text"],
                line["confidence"],
            )

        repo.save_result_and_complete(receipt_id, ocr_result, "OCR_COMPLETED")
        logger.info("Receipt id=%s completed", receipt_id)
    except Exception:
        logger.exception("OCR failed for receipt id=%s", receipt_id)
        repo.update_status(receipt_id, "FAILED")
    finally:
        if tmp_path and os.path.exists(tmp_path):
            os.remove(tmp_path)
