CREATE TABLE mcp_generated_datasets (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(200),
    format VARCHAR(50) NOT NULL,
    row_count INT NOT NULL DEFAULT 0,
    schema_json LONGTEXT,
    sample_data LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
