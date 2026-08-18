package com.reciply.db

enum class Status {
    PENDING,
    PROCESSING,
    OCR_COMPLETED,
    INVALID_IMAGE,
    INVOICE_CREATED,
    FAILED
}
