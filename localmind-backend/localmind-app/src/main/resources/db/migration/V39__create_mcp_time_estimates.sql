CREATE TABLE mcp_time_estimates (
    id CHAR(36) NOT NULL PRIMARY KEY,
    task_id VARCHAR(200),
    estimate_minutes INT NOT NULL DEFAULT 0,
    description LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
