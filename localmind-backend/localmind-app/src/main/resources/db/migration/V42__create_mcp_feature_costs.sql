CREATE TABLE mcp_feature_costs (
    id CHAR(36) NOT NULL PRIMARY KEY,
    feature_id VARCHAR(200) NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    hours_spent DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    hourly_rate DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_cost DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    description LONGTEXT,
    currency VARCHAR(10) NOT NULL DEFAULT 'EUR',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
