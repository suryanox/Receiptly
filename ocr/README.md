# Receiptly OCR

Background worker that turns receipt images into structured invoices.

## How it works

1. Polls PostgreSQL for `PENDING` receipts and atomically claims one (`FOR UPDATE SKIP LOCKED`).
2. Downloads the receipt image from Telegram.
3. Extracts text lines with PaddleOCR.
4. Sends the image + OCR text to an OpenRouter LLM, constrained by a JSON schema.
5. Upserts the resulting invoice and marks the receipt `INVOICE_CREATED`.

Failures are logged and the receipt is marked `FAILED` so a single bad image never stalls the queue.

## Configuration

All values are read from `resources/reference.conf` and can be overridden by environment variables:

| Env var | Purpose |
| --- | --- |
| `TELEGRAM_BOT_TOKEN` | Telegram bot token |
| `OPENROUTER_API_KEY` | OpenRouter API key |
| `OPENROUTER_MODEL` | Optional explicit model id (empty = auto-select a free vision model) |
| `DATABASE_URL` / `DATABASE_USER` / `DATABASE_PASSWORD` | Postgres connection |
| `POLL_INTERVAL_SECONDS` | Poll cadence |

## Run

```bash
poetry install
TELEGRAM_BOT_TOKEN=<token> OPENROUTER_API_KEY=<key> poetry run python -m reciply_ocr.main
```

## Layout

```
reciply_ocr/
├── main.py               # entrypoint: wiring + poll loop + graceful shutdown
├── config.py             # typed settings with env overrides
├── db.py                 # connection pool + transactional cursor context manager
├── models.py             # domain types (ReceiptStatus, PendingReceipt, OcrLine)
├── repository.py         # ReceiptRepository (claim / status transitions)
├── invoice_repository.py # InvoiceRepository (parameterized upsert)
├── invoice_schema.py     # pydantic Invoice + JSON-schema scaffold for the LLM
├── prompts.py            # system/user prompt builders
├── ocr.py                # lazy PaddleOCR wrapper
├── openrouter.py         # LLM client with retries + free-model auto-select
├── processor.py          # end-to-end pipeline orchestration
└── telegram_client.py    # Telegram file download
```

## Fmt

```bash
poetry run ruff format src/
poetry run ruff check src/
```
