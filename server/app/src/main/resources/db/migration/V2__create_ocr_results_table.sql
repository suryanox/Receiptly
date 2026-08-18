CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    receipt_id BIGINT NOT NULL UNIQUE REFERENCES receipts(id),

    invoice_number VARCHAR(255),
    invoice_date DATE,

    supplier_name VARCHAR(255),
    supplier_tax_id VARCHAR(255),

    currency VARCHAR(3) NOT NULL,

    subtotal NUMERIC(12, 2),
    discount NUMERIC(12, 2),
    tax_amount NUMERIC(12, 2),
    tax_rate NUMERIC(5, 2),
    total_amount NUMERIC(12, 2),

    category VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
