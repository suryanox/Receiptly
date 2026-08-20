from __future__ import annotations

import logging

from paddleocr import PaddleOCR

from reciply_ocr.models import OcrLine

logger = logging.getLogger("reciply_ocr.ocr")


class OcrEngine:
    """Lazy, single-owner wrapper around PaddleOCR."""

    def __init__(self, lang: str = "en") -> None:
        self._lang = lang
        self._engine: PaddleOCR | None = None

    def extract(self, image_path: str) -> list[OcrLine]:
        logger.info("Running OCR on %s", image_path)
        raw = self._engine_instance().ocr(image_path)
        return self._to_lines(raw)

    def _engine_instance(self) -> PaddleOCR:
        if self._engine is None:
            logger.info("Initializing PaddleOCR (lang=%s)", self._lang)
            self._engine = PaddleOCR(use_textline_orientation=True, lang=self._lang)
        return self._engine

    @staticmethod
    def _to_lines(raw: list[dict] | None) -> list[OcrLine]:
        lines: list[OcrLine] = []
        for page in raw or []:
            texts = page.get("rec_texts") or []
            scores = page.get("rec_scores") or []
            for text, score in zip(texts, scores):
                lines.append(OcrLine(text=text, confidence=round(float(score), 4)))
        return lines
