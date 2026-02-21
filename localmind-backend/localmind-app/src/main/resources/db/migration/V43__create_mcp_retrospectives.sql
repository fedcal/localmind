CREATE TABLE mcp_retrospectives (
    id CHAR(36) NOT NULL PRIMARY KEY,
    sprint_id VARCHAR(200),
    format VARCHAR(50) NOT NULL DEFAULT 'mad-sad-glad',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
