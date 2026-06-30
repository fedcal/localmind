-- LLM provider configurations (API keys, base URLs, default models)
CREATE TABLE llm_provider_configs (
    id CHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    base_url VARCHAR(500) NOT NULL,
    api_key VARCHAR(500),
    default_model VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_provider_type CHECK (type IN ('OLLAMA', 'OPENAI', 'ANTHROPIC', 'GOOGLE')),
    CONSTRAINT uq_provider_name UNIQUE (name)
);

CREATE INDEX idx_provider_configs_type ON llm_provider_configs(type);
CREATE INDEX idx_provider_configs_enabled ON llm_provider_configs(enabled);
