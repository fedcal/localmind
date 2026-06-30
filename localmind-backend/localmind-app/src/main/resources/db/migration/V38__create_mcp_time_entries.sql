CREATE TABLE mcp_time_entries (
    id CHAR(36) NOT NULL PRIMARY KEY,
    task_id VARCHAR(200),
    user_id VARCHAR(200),
    duration_minutes INT NOT NULL DEFAULT 0,
    description LONGTEXT,
    entry_date VARCHAR(20),
    timer_started_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
