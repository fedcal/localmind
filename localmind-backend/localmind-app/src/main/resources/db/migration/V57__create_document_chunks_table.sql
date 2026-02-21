CREATE TABLE document_chunks (
    id CHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
    document_id CHAR(36) NOT NULL,
    content TEXT NOT NULL,
    chunk_index INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chunks_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
)
