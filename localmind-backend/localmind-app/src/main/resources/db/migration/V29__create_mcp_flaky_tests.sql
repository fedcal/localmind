CREATE TABLE mcp_flaky_tests (
    id CHAR(36) NOT NULL PRIMARY KEY,
    repo VARCHAR(200),
    test_name VARCHAR(500) NOT NULL,
    workflow VARCHAR(200),
    pass_count INT NOT NULL DEFAULT 0,
    fail_count INT NOT NULL DEFAULT 0,
    flakiness_rate DOUBLE NOT NULL DEFAULT 0,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
