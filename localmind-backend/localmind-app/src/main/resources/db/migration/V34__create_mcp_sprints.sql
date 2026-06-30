CREATE TABLE mcp_sprints (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    start_date VARCHAR(20),
    end_date VARCHAR(20),
    goals LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
