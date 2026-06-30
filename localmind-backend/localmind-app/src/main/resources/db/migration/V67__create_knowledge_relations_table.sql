CREATE TABLE knowledge_relations (
    id CHAR(36) PRIMARY KEY,
    from_entity_id CHAR(36) NOT NULL,
    to_entity_id CHAR(36) NOT NULL,
    relation_type VARCHAR(30) NOT NULL,
    properties TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_kr_from (from_entity_id),
    INDEX idx_kr_to (to_entity_id),
    FOREIGN KEY (from_entity_id) REFERENCES knowledge_entities(id),
    FOREIGN KEY (to_entity_id) REFERENCES knowledge_entities(id)
)
