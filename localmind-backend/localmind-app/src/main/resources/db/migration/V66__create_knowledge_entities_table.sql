CREATE TABLE knowledge_entities (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    entity_type VARCHAR(30) NOT NULL,
    properties TEXT,
    source_document_id CHAR(36),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ke_name (name),
    INDEX idx_ke_type (entity_type),
    INDEX idx_ke_source (source_document_id)
)
