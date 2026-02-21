CREATE TABLE llm_usage (
    id CHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    provider VARCHAR(20) NOT NULL,
    model VARCHAR(100),
    prompt_tokens INTEGER NOT NULL DEFAULT 0,
    completion_tokens INTEGER NOT NULL DEFAULT 0,
    total_tokens INTEGER NOT NULL DEFAULT 0,
    cost DOUBLE NOT NULL DEFAULT 0,
    latency_ms BIGINT NOT NULL DEFAULT 0,
    `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_llm_usage_timestamp ON llm_usage(`timestamp`);
CREATE INDEX idx_llm_usage_provider ON llm_usage(provider);
