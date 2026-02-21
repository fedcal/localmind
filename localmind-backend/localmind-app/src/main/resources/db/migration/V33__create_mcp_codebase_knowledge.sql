CREATE TABLE mcp_codebase_knowledge (
    id CHAR(36) NOT NULL PRIMARY KEY,
    analysis_type VARCHAR(50) NOT NULL,
    target_path VARCHAR(500),
    result LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
