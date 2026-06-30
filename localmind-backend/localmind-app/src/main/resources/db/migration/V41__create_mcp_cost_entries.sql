CREATE TABLE mcp_cost_entries (
    id CHAR(36) NOT NULL PRIMARY KEY,
    project_name VARCHAR(200) NOT NULL,
    category VARCHAR(100),
    amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    description LONGTEXT,
    entry_date VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
