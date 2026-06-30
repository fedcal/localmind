CREATE TABLE mcp_agile_metrics (
    id CHAR(36) NOT NULL PRIMARY KEY,
    metric_type VARCHAR(50) NOT NULL,
    sprint_ref VARCHAR(200),
    result LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
