import logging

from reciply_ocr.invoice_schema import column_names

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

    def save_invoice(self, receipt_id: int, invoice: dict):
        columns = ["receipt_id"] + column_names()
        placeholders = ", ".join(["%s"] * len(columns))
        column_list = ", ".join(columns)
        values = [receipt_id] + [invoice.get(name) for name in column_names()]

        conn = self._get_conn()
        try:
            with conn.cursor() as cur:
                cur.execute(
                    f"""
                    INSERT INTO invoices ({column_list})
                    VALUES ({placeholders})
                    ON CONFLICT (receipt_id) DO UPDATE
                    SET
                        invoice_number = EXCLUDED.invoice_number,
                        invoice_date = EXCLUDED.invoice_date,
                        supplier_name = EXCLUDED.supplier_name,
                        supplier_tax_id = EXCLUDED.supplier_tax_id,
                        currency = EXCLUDED.currency,
                        subtotal = EXCLUDED.subtotal,
                        discount = EXCLUDED.discount,
                        tax_amount = EXCLUDED.tax_amount,
                        tax_rate = EXCLUDED.tax_rate,
                        total_amount = EXCLUDED.total_amount,
                        category = EXCLUDED.category,
                        updated_at = NOW()
                    """,
                    values,
                )
                conn.commit()
        finally:
            self._put_conn(conn)
