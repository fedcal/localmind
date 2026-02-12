CREATE TABLE mcp_license_audits (
    id CHAR(36) NOT NULL PRIMARY KEY,
    project_path VARCHAR(500) NOT NULL,
    project_type VARCHAR(20) NOT NULL,
    total_checked INT NOT NULL DEFAULT 0,
    copyleft_count INT NOT NULL DEFAULT 0,
    result LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
