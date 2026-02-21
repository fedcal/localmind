CREATE TABLE document_stats_cache (
    id CHAR(36) PRIMARY KEY,
    title VARCHAR(500),
    status VARCHAR(20),
    chunk_count INT DEFAULT 0,
    total_size BIGINT DEFAULT 0,
    mime_type VARCHAR(100),
    ocr_confidence DOUBLE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_dsc_status (status),
    INDEX idx_dsc_mime (mime_type)
)
