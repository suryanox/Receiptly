from __future__ import annotations

from datetime import date

from pydantic import BaseModel, ConfigDict, Field

# Column order must match the `invoices` table schema (V2 migration).
DB_COLUMNS: tuple[str, ...] = (
    "invoice_number",
    "invoice_date",
    "supplier_name",
    "supplier_tax_id",
    "currency",
    "subtotal",
    "discount",
    "tax_amount",
    "tax_rate",
    "total_amount",
    "category",
)


class Invoice(BaseModel):
    """Structured invoice extracted from a receipt by the LLM."""

    model_config = ConfigDict(extra="forbid")

    invoice_number: str | None = Field(
        default=None, description="Invoice/receipt number as printed. Null if not present."
    )
    invoice_date: date | None = Field(
        default=None, description="Issue date in ISO YYYY-MM-DD. Null if not present."
    )
    supplier_name: str | None = Field(
        default=None, description="Name of the merchant / seller / supplier."
    )
    supplier_tax_id: str | None = Field(
        default=None,
        description="Tax ID (VAT/TIN/GST) of the supplier. Null if not present.",
    )
    currency: str = Field(default="USD", description="ISO 4217 currency code, 3 uppercase letters.")
    subtotal: float | None = Field(
        default=None, description="Sum of line items before tax/discount. Null if not present."
    )
    discount: float | None = Field(
        default=None, description="Total discount amount. Null if not present."
    )
    tax_amount: float | None = Field(
        default=None, description="Total tax/VAT amount. Null if not present."
    )
    tax_rate: float | None = Field(
        default=None, description="Tax rate as percentage, e.g. 18.0. Null if not present."
    )
    total_amount: float = Field(
        default=0.0, description="Final amount payable including tax and discounts."
    )
    category: str | None = Field(
        default=None,
        description="Short category: GROCERIES, RESTAURANT, UTILITIES, TRAVEL, ELECTRONICS, OTHER.",
    )

    def db_values(self) -> list[object]:
        """Values aligned with `db_columns()`, ready for a parameterized insert."""
        return [getattr(self, column) for column in DB_COLUMNS]


def schema_description() -> str:
    lines = []
    for name, field in Invoice.model_fields.items():
        requirement = "required" if field.is_required() else "optional"
        lines.append(f"- {name} ({field.annotation}, {requirement}): {field.description}")
    return "\n".join(lines)


def response_format() -> dict:
    """Structured-output scaffold that constrains the LLM to our schema."""
    return {
        "type": "json_schema",
        "json_schema": {
            "name": "invoice",
            "strict": True,
            "schema": Invoice.model_json_schema(),
        },
    }
