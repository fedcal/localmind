CREATE TABLE documents (
    id CHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    filename VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000),
    mime_type VARCHAR(100),
    file_size BIGINT,
    file_hash VARCHAR(64),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    metadata JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    indexed_at TIMESTAMP NULL
);

CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_documents_hash ON documents(file_hash);
