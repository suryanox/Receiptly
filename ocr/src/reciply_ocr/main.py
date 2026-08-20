from __future__ import annotations

import asyncio
import logging

from reciply_ocr.config import load_settings
from reciply_ocr.db import Database
from reciply_ocr.invoice_repository import InvoiceRepository
from reciply_ocr.ocr import OcrEngine
from reciply_ocr.openrouter import OpenRouterExtractor
from reciply_ocr.processor import ReceiptProcessor
from reciply_ocr.repository import ReceiptRepository
from reciply_ocr.telegram_client import TelegramClient

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(name)s: %(message)s",
)
logger = logging.getLogger("reciply_ocr")


def build_processor() -> tuple[ReceiptProcessor, Database, TelegramClient]:
    settings = load_settings()
    database = Database(settings.database)
    telegram = TelegramClient(settings.telegram.bot_token)
    processor = ReceiptProcessor(
        receipts=ReceiptRepository(database),
        invoices=InvoiceRepository(database),
        telegram=telegram,
        ocr_engine=OcrEngine(),
        extractor=OpenRouterExtractor(settings.openrouter),
    )
    return processor, database, telegram


async def _poll_forever(processor: ReceiptProcessor, interval_seconds: int) -> None:
    while True:
        if not await processor.process_next():
            await asyncio.sleep(interval_seconds)


async def main() -> None:
    settings = load_settings()
    processor, database, telegram = build_processor()
    logger.info(
        "%s started, polling every %ss",
        settings.name,
        settings.poll_interval_seconds,
    )
    try:
        await _poll_forever(processor, settings.poll_interval_seconds)
    except asyncio.CancelledError:
        logger.info("Shutdown requested")
    finally:
        await telegram.close()
        database.close()


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        pass
