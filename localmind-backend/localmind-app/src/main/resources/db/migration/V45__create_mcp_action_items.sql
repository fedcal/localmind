CREATE TABLE mcp_action_items (
    id CHAR(36) NOT NULL PRIMARY KEY,
    retro_id CHAR(36) NOT NULL,
    description LONGTEXT NOT NULL,
    assignee VARCHAR(200),
    due_date VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
