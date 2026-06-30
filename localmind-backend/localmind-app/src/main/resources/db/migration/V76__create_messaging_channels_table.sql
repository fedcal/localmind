CREATE TABLE messaging_channels (
    id CHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    platform VARCHAR(50) NOT NULL,
    bot_token VARCHAR(500) NOT NULL,
    webhook_secret VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    default_system_prompt TEXT,
    default_model VARCHAR(255),
    default_provider VARCHAR(50),
    enable_rag BOOLEAN NOT NULL DEFAULT FALSE,
    enable_tool_calling BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_messaging_platform CHECK (platform IN ('SLACK', 'DISCORD', 'TELEGRAM')),
    CONSTRAINT uq_channel_name UNIQUE (name)
)
