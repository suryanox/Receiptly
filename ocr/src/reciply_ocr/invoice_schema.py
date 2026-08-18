from typing import Any

INVOICE_FIELDS: list[dict[str, Any]] = [
    {
        "name": "invoice_number",
        "type": "string",
        "description": "Invoice or receipt number as printed on the document. Null if not present.",
        "nullable": True,
    },
    {
        "name": "invoice_date",
        "type": "date",
        "description": "Issue date of the invoice in ISO format YYYY-MM-DD. Null if not present.",
        "nullable": True,
    },
    {
        "name": "supplier_name",
        "type": "string",
        "description": "Name of the merchant / seller / supplier.",
        "nullable": True,
    },
    {
        "name": "supplier_tax_id",
        "type": "string",
        "description": "Tax identification number (VAT/TIN/GST) of the supplier. Null if not present.",
        "nullable": True,
    },
    {
        "name": "currency",
        "type": "string",
        "description": "ISO 4217 currency code of 3 uppercase letters, e.g. USD, EUR, INR. Required.",
        "nullable": False,
    },
    {
        "name": "subtotal",
        "type": "number",
        "description": "Sum of line items before tax and discounts. Null if not present.",
        "nullable": True,
    },
    {
        "name": "discount",
        "type": "number",
        "description": "Total discount amount applied. Null if not present (treat as 0 only if explicitly shown).",
        "nullable": True,
    },
    {
        "name": "tax_amount",
        "type": "number",
        "description": "Total tax / VAT amount. Null if not present.",
        "nullable": True,
    },
    {
        "name": "tax_rate",
        "type": "number",
        "description": "Tax rate as a percentage, e.g. 18.0 for 18%. Null if not present.",
        "nullable": True,
    },
    {
        "name": "total_amount",
        "type": "number",
        "description": "Final amount payable including tax and discounts. Required.",
        "nullable": False,
    },
    {
        "name": "category",
        "type": "string",
        "description": (
            "A short category for the purchase, e.g. GROCERIES, RESTAURANT, "
            "UTILITIES, TRAVEL, ELECTRONICS, OTHER. Default to OTHER if unclear."
        ),
        "nullable": True,
    },
]

FIELD_NAMES = [f["name"] for f in INVOICE_FIELDS]


def schema_description() -> str:
    lines = []
    for f in INVOICE_FIELDS:
        req = "required" if not f["nullable"] else "optional"
        lines.append(f"- {f['name']} ({f['type']}, {req}): {f['description']}")
    return "\n".join(lines)
