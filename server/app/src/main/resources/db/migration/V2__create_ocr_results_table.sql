CREATE TABLE ocr_results (
    id BIGSERIAL PRIMARY KEY,
    receipt_id BIGINT NOT NULL UNIQUE,
    result_json JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_ocr_results_receipt FOREIGN KEY (receipt_id) REFERENCES receipts(id)
);

CREATE INDEX idx_ocr_results_receipt_id ON ocr_results(receipt_id);
