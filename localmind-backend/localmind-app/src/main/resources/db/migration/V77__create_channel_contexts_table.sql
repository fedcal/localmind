CREATE TABLE channel_contexts (
    id CHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    channel_id CHAR(36) NOT NULL,
    platform VARCHAR(50) NOT NULL,
    platform_user_id VARCHAR(255) NOT NULL,
    platform_channel_id VARCHAR(255) NOT NULL,
    conversation_id CHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_channel_ctx_channel FOREIGN KEY (channel_id) REFERENCES messaging_channels(id) ON DELETE CASCADE,
    CONSTRAINT fk_channel_ctx_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE SET NULL
)
