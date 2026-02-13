CREATE TABLE mcp_user_stories (
    id CHAR(36) NOT NULL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    description LONGTEXT,
    acceptance_criteria LONGTEXT,
    story_points INT DEFAULT 0,
    priority VARCHAR(20) DEFAULT 'medium',
    sprint_id CHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
