CREATE TABLE conversation_summaries (
    id CHAR(36) PRIMARY KEY,
    title VARCHAR(200),
    message_count INT DEFAULT 0,
    last_message_preview VARCHAR(500),
    tags TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cs_updated (updated_at DESC),
    INDEX idx_cs_title (title)
)
