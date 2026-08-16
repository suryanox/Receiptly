import asyncio
import logging

from reciply_ocr.config import load_config
from reciply_ocr.db import get_conn, init_db, put_conn
from reciply_ocr.repository import ReceiptRepository
from reciply_ocr.telegram_client import TelegramClient
from reciply_ocr.worker import process_once

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(name)s: %(message)s",
)
logger = logging.getLogger("reciply_ocr")


async def main():
    config = load_config()
    init_db(config)

    tg = TelegramClient(config["telegram"]["botToken"])
    repo = ReceiptRepository(get_conn, put_conn)
    interval = config["app"]["pollIntervalSeconds"]
    logger.info("%s started, polling every %ss", config["app"]["name"], interval)

    try:
        while True:
            await process_once(repo, tg)
            await asyncio.sleep(interval)
    finally:
        await tg.close()


if __name__ == "__main__":
    asyncio.run(main())
