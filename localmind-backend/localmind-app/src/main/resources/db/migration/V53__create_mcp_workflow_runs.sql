CREATE TABLE mcp_workflow_runs (
    id CHAR(36) NOT NULL PRIMARY KEY,
    workflow_id CHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'pending',
    payload_json LONGTEXT,
    step_results_json LONGTEXT,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL
);