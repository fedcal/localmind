CREATE TABLE mcp_decision_links (
    id CHAR(36) NOT NULL PRIMARY KEY,
    decision_id CHAR(36) NOT NULL,
    link_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(200) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);