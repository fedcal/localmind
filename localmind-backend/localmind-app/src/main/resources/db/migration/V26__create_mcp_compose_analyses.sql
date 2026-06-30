CREATE TABLE mcp_compose_analyses (
    id CHAR(36) NOT NULL PRIMARY KEY,
    content_type VARCHAR(50) NOT NULL,
    service_count INT NOT NULL DEFAULT 0,
    result LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
