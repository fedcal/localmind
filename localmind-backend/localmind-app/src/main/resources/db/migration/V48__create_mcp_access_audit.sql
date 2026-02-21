CREATE TABLE mcp_access_audit (
    id CHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(200) NOT NULL,
    server VARCHAR(200) NOT NULL,
    tool VARCHAR(200),
    allowed BOOLEAN NOT NULL,
    policy_id CHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);