CREATE TABLE training_datasets (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    document_ids TEXT,
    sample_count INT NOT NULL DEFAULT 0,
    format VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
)
