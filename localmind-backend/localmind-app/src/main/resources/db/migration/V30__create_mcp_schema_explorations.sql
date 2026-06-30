CREATE TABLE mcp_schema_explorations (
    id CHAR(36) NOT NULL PRIMARY KEY,
    jdbc_url VARCHAR(500) NOT NULL,
    table_count INT NOT NULL DEFAULT 0,
    result LONGTEXT,
    explored_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
