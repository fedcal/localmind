CREATE TABLE mcp_decisions (
    id CHAR(36) NOT NULL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    context VARCHAR(2000) NOT NULL,
    decision_text TEXT NOT NULL,
    alternatives_json LONGTEXT,
    consequences TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'proposed',
    related_tickets_json LONGTEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);