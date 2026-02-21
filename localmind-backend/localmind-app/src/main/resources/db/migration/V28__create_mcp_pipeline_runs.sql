CREATE TABLE mcp_pipeline_runs (
    id CHAR(36) NOT NULL PRIMARY KEY,
    owner VARCHAR(200),
    repo VARCHAR(200),
    run_id VARCHAR(100),
    title VARCHAR(500),
    branch VARCHAR(200),
    status VARCHAR(50),
    conclusion VARCHAR(50),
    workflow VARCHAR(200),
    url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
