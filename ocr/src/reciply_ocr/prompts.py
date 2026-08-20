from __future__ import annotations

from reciply_ocr.invoice_schema import schema_description

SYSTEM_PROMPT = (
    "You are a precise data-extraction assistant. You are given a photo of an invoice/receipt "
    "and the raw OCR text extracted from it. Fill the provided JSON schema using ONLY information "
    "that appears in the document. Use null for any field that is not present. "
    "Return ONLY a single valid JSON object and nothing else."
)


def build_user_prompt(ocr_text: str) -> str:
    return (
        "Extract the invoice/receipt data and return it as a JSON object matching this schema:\n\n"
        f"{schema_description()}\n\n"
        "OCR text from the document:\n"
        "----\n"
        f"{ocr_text}\n"
        "----\n\n"
        "Respond with ONLY the JSON object."
    )
