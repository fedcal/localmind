CREATE TABLE backup_history (
    id CHAR(36) PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    size_bytes BIGINT DEFAULT 0,
    backup_type VARCHAR(20) NOT NULL,
    components VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
)
