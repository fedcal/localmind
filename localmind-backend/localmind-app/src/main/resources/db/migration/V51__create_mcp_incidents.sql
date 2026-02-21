CREATE TABLE mcp_incidents (
    id CHAR(36) NOT NULL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'open',
    description TEXT NOT NULL,
    affected_systems_json LONGTEXT,
    timeline_json LONGTEXT,
    resolution TEXT,
    root_cause TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL
);