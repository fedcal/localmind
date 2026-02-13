CREATE TABLE mcp_api_documentations (
    id CHAR(36) NOT NULL PRIMARY KEY,
    documentation_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500),
    result LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
