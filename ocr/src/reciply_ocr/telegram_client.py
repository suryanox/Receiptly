import logging
import os

from telegram import Bot

logger = logging.getLogger("reciply_ocr.telegram")


class TelegramClient:
    def __init__(self, bot_token: str):
        self._bot = Bot(token=bot_token)

    async def download(self, file_id: str, dest_path: str) -> str:
        logger.info("Downloading file file_id=%s -> %s", file_id, dest_path)
        file = await self._bot.get_file(file_id)
        logger.debug("Resolved file_path=%s", file.file_path)
        await file.download_to_drive(dest_path)
        logger.info("Downloaded file_id=%s (%d bytes)", file_id, os.path.getsize(dest_path))
        return dest_path

    async def close(self):
        await self._bot.shutdown()
