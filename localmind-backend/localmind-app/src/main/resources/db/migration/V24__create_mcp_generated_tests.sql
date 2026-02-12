CREATE TABLE mcp_generated_tests (
    id CHAR(36) NOT NULL PRIMARY KEY,
    source_file_path VARCHAR(500),
    language VARCHAR(50) NOT NULL,
    framework VARCHAR(50) NOT NULL,
    functions_found INT NOT NULL DEFAULT 0,
    test_code LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
