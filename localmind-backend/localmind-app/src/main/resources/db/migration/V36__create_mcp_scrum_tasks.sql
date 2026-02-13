CREATE TABLE mcp_scrum_tasks (
    id CHAR(36) NOT NULL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    description LONGTEXT,
    story_id CHAR(36),
    assignee VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'todo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
