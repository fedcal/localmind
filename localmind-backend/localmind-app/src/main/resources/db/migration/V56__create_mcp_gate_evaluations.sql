CREATE TABLE mcp_gate_evaluations (
    id CHAR(36) NOT NULL PRIMARY KEY,
    gate_id CHAR(36) NOT NULL,
    passed BOOLEAN NOT NULL,
    metrics_json LONGTEXT,
    results_json LONGTEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);