from __future__ import annotations

from dataclasses import dataclass
from enum import Enum


class ReceiptStatus(str, Enum):
    PENDING = "PENDING"
    PROCESSING = "PROCESSING"
    INVALID_IMAGE = "INVALID_IMAGE"
    INVOICE_CREATED = "INVOICE_CREATED"
    FAILED = "FAILED"


@dataclass(frozen=True)
class PendingReceipt:
    id: int
    image_file_id: str


@dataclass(frozen=True)
class OcrLine:
    text: str
    confidence: float
