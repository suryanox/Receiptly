from __future__ import annotations

import logging
import textwrap

from reciply_ocr.db import Database
from reciply_ocr.models import PendingReceipt, ReceiptStatus

logger = logging.getLogger("reciply_ocr.repository")

_CLAIM_PENDING_SQL = textwrap.dedent(
    """
    UPDATE receipts
       SET status = %s, updated_at = NOW()
     WHERE id = (
           SELECT id
             FROM receipts
            WHERE status = %s
            ORDER BY created_at
            LIMIT 1
            FOR UPDATE SKIP LOCKED
     )
    RETURNING id, image_file_id
    """
)


class ReceiptRepository:
    def __init__(self, database: Database) -> None:
        self._database = database

    def claim_next_pending(self) -> PendingReceipt | None:
        """Atomically claim the oldest PENDING receipt, or None if none exists."""
        with self._database.transaction() as cursor:
            cursor.execute(
                _CLAIM_PENDING_SQL,
                (ReceiptStatus.PROCESSING.value, ReceiptStatus.PENDING.value),
            )
            row = cursor.fetchone()

        if row is None:
            logger.debug("No pending receipts")
            return None

        logger.info("Claimed receipt id=%s", row[0])
        return PendingReceipt(id=row[0], image_file_id=row[1])

    def update_status(self, receipt_id: int, status: ReceiptStatus) -> None:
        with self._database.transaction() as cursor:
            cursor.execute(
                "UPDATE receipts SET status = %s, updated_at = NOW() WHERE id = %s",
                (status.value, receipt_id),
            )
        logger.info("Receipt id=%s -> status=%s", receipt_id, status.value)
