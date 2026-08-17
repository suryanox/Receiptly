import json
import logging

from psycopg2.extras import Json

logger = logging.getLogger("reciply_ocr.repository")


class ReceiptRepository:
    def __init__(self, get_conn, put_conn):
        self._get_conn = get_conn
        self._put_conn = put_conn

    def claim_pending(self):
        conn = self._get_conn()
        try:
            with conn.cursor() as cur:
                cur.execute(
                    """
                    UPDATE receipts
                    SET status = 'PROCESSING', updated_at = NOW()
                    WHERE id = (
                        SELECT id FROM receipts
                        WHERE status = 'PENDING'
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
        finally:
            self._put_conn(conn)

    def save_result_and_complete(self, receipt_id: int, ocr_result: list, status: str):
        conn = self._get_conn()
        try:
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
                    "UPDATE receipts SET status = %s, updated_at = NOW() WHERE id = %s",
                    (status, receipt_id),
                )
                conn.commit()
        finally:
            self._put_conn(conn)

    def update_status(self, receipt_id: int, status: str):
        conn = self._get_conn()
        try:
            with conn.cursor() as cur:
                cur.execute(
                    "UPDATE receipts SET status = %s, updated_at = NOW() WHERE id = %s",
                    (status, receipt_id),
                )
                conn.commit()
        finally:
            self._put_conn(conn)
