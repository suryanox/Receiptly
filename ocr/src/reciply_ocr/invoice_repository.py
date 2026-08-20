from __future__ import annotations

import logging
import textwrap

from psycopg2.extensions import cursor as PsycopgCursor
from psycopg2.sql import SQL, Composed, Identifier

from reciply_ocr.db import Database
from reciply_ocr.invoice_schema import DB_COLUMNS, Invoice

logger = logging.getLogger("reciply_ocr.repository")


class InvoiceRepository:
    """Persists extracted invoices under their source receipt."""

    def __init__(self, database: Database) -> None:
        self._database = database

    def save(self, receipt_id: int, invoice: Invoice) -> None:
        columns = [Identifier("receipt_id"), *(Identifier(column) for column in DB_COLUMNS)]
        setters = [
            SQL("{column} = EXCLUDED.{column}").format(column=Identifier(column))
            for column in DB_COLUMNS
        ]

        upsert = SQL(
            textwrap.dedent(
                """
                INSERT INTO invoices ({columns})
                VALUES ({placeholders})
                ON CONFLICT (receipt_id) DO UPDATE
                SET {setters}, updated_at = NOW()
                """
            )
        ).format(
            columns=Composed([SQL(", ").join(columns)]),
            placeholders=Composed([SQL(", ").join(SQL("%s") for _ in columns)]),
            setters=Composed([SQL(", ").join(setters)]),
        )

        values = [receipt_id, *invoice.db_values()]
        with self._database.transaction() as cursor:
            self._execute(cursor, upsert, values)

        logger.info("Saved invoice for receipt id=%s", receipt_id)

    @staticmethod
    def _execute(cursor: PsycopgCursor, statement: Composed, values: list[object]) -> None:
        cursor.execute(statement, values)
