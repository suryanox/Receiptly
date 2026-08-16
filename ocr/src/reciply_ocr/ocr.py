from typing import Any

from paddleocr import PaddleOCR

_ocr = None


def get_ocr() -> PaddleOCR:
    global _ocr
    if _ocr is None:
        _ocr = PaddleOCR(use_textline_orientation=True, lang="en")
    return _ocr


def run_ocr(image_path: str) -> list[dict[str, Any]]:
    raw = get_ocr().ocr(image_path)
    if not raw:
        return []

    result: list[dict[str, Any]] = []
    for page in raw:
        texts = page.get("rec_texts") or []
        scores = page.get("rec_scores") or []
        for text, score in zip(texts, scores):
            result.append(
                {
                    "text": text,
                    "confidence": round(float(score), 4),
                }
            )
    return result
