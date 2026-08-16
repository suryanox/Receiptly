CREATE TABLE receipts (
    id BIGSERIAL PRIMARY KEY,
    image_file_id VARCHAR(255) NOT NULL UNIQUE,
    chat_id BIGINT NOT NULL,
    ocr_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_receipts_chat_id ON receipts(chat_id);
CREATE INDEX idx_receipts_image_file_id ON receipts(image_file_id);
