CREATE TABLE mcp_workflows (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    trigger_event VARCHAR(200) NOT NULL,
    trigger_conditions_json LONGTEXT,
    steps_json LONGTEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);