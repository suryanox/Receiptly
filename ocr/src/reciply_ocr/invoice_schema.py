from datetime import date
from typing import Optional

from pydantic import BaseModel, Field


class Invoice(BaseModel):
    invoice_number: Optional[str] = Field(
        default=None, description="Invoice/receipt number as printed. Null if not present."
    )
    invoice_date: Optional[date] = Field(
        default=None, description="Issue date in ISO YYYY-MM-DD. Null if not present."
    )
    supplier_name: Optional[str] = Field(
        default=None, description="Name of the merchant / seller / supplier."
    )
    supplier_tax_id: Optional[str] = Field(
        default=None, description="Tax ID (VAT/TIN/GST) of the supplier. Null if not present."
    )
    currency: str = Field(
        default="USD", description="ISO 4217 currency code, 3 uppercase letters."
    )
    subtotal: Optional[float] = Field(
        default=None, description="Sum of line items before tax/discount. Null if not present."
    )
    discount: Optional[float] = Field(
        default=None, description="Total discount amount. Null if not present."
    )
    tax_amount: Optional[float] = Field(
        default=None, description="Total tax/VAT amount. Null if not present."
    )
    tax_rate: Optional[float] = Field(
        default=None, description="Tax rate as percentage, e.g. 18.0. Null if not present."
    )
    total_amount: float = Field(
        default=0.0, description="Final amount payable including tax and discounts."
    )
    category: Optional[str] = Field(
        default=None,
        description="Short category: GROCERIES, RESTAURANT, UTILITIES, TRAVEL, ELECTRONICS, OTHER.",
    )


def column_names() -> list[str]:
    return list(Invoice.model_fields.keys())


def schema_description() -> str:
    lines = []
    for name, field in Invoice.model_fields.items():
        req = "required" if field.is_required() else "optional"
        lines.append(f"- {name} ({field.annotation}, {req}): {field.description}")
    return "\n".join(lines)


def response_format() -> dict:
    schema = Invoice.model_json_schema()
    return {
        "type": "json_schema",
        "json_schema": {
            "name": "invoice",
            "strict": True,
            "schema": schema,
        },
    }
