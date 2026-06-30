CREATE TABLE finetuning_jobs (
    id CHAR(36) NOT NULL PRIMARY KEY,
    base_model VARCHAR(200) NOT NULL,
    technique VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    progress INT NOT NULL DEFAULT 0,
    dataset_id CHAR(36),
    output_model_name VARCHAR(200),
    hyperparameters TEXT,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
)
