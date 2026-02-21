CREATE TABLE mcp_code_reviews (
    id CHAR(36) NOT NULL PRIMARY KEY,
    review_type VARCHAR(50) NOT NULL,
    issues_found INT NOT NULL DEFAULT 0,
    suggestions TEXT,
    result LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
