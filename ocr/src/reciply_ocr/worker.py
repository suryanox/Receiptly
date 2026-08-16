import json
import logging
import os
import tempfile

from psycopg2.extras import Json

from reciply_ocr.ocr import run_ocr
from reciply_ocr.telegram_client import TelegramClient

logger = logging.getLogger("reciply_ocr.worker")


def claim_pending(conn):
    with conn.cursor() as cur:
        cur.execute(
            """
            UPDATE receipts
            SET ocr_status = 'PROCESSING', updated_at = NOW()
            WHERE id = (
                SELECT id FROM receipts
                WHERE ocr_status = 'PENDING'
                ORDER BY created_at
                LIMIT 1
                FOR UPDATE SKIP LOCKED
            )
            RETURNING id, image_file_id
            """
        )
        row = cur.fetchone()
        conn.commit()
        return row


def save_result_and_complete(conn, receipt_id: int, ocr_result: list, status: str):
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO ocr_results (receipt_id, result_json, created_at, updated_at)
            VALUES (%s, %s, NOW(), NOW())
            ON CONFLICT (receipt_id) DO UPDATE
            SET result_json = EXCLUDED.result_json, updated_at = NOW()
            """,
            (receipt_id, Json(json.loads(json.dumps(ocr_result)))),
        )
        cur.execute(
            "UPDATE receipts SET ocr_status = %s, updated_at = NOW() WHERE id = %s",
            (status, receipt_id),
        )
        conn.commit()


def update_status(conn, receipt_id: int, status: str):
    with conn.cursor() as cur:
        cur.execute(
            "UPDATE receipts SET ocr_status = %s, updated_at = NOW() WHERE id = %s",
            (status, receipt_id),
        )
        conn.commit()


async def process_once(conn, tg: TelegramClient):
    claimed = claim_pending(conn)
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

        save_result_and_complete(conn, receipt_id, ocr_result, "OCR_COMPLETED")
        logger.info("Receipt id=%s completed", receipt_id)
    except Exception:
        logger.exception("OCR failed for receipt id=%s", receipt_id)
        update_status(conn, receipt_id, "FAILED")
    finally:
        if tmp_path and os.path.exists(tmp_path):
            os.remove(tmp_path)
