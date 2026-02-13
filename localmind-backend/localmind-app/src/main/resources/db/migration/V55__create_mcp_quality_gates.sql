CREATE TABLE mcp_quality_gates (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    project_name VARCHAR(200),
    checks_json LONGTEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);