CREATE TABLE mcp_http_requests (
    id CHAR(36) PRIMARY KEY,
    url VARCHAR(2000) NOT NULL,
    method VARCHAR(10) NOT NULL,
    status_code INT NOT NULL,
    response_time_ms BIGINT NOT NULL,
    response_body LONGTEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
)
