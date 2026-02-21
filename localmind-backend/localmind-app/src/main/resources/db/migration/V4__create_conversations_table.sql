CREATE TABLE conversations (
    id CHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    title VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE chat_messages (
    id CHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    conversation_id CHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_messages_conversation ON chat_messages(conversation_id);
