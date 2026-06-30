CREATE TABLE mcp_log_analyses (
    id CHAR(36) NOT NULL PRIMARY KEY,
    total_lines INT NOT NULL DEFAULT 0,
    levels VARCHAR(500),
    top_errors LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
