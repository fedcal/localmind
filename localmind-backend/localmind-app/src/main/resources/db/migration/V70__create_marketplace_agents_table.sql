CREATE TABLE marketplace_agents (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    author VARCHAR(100),
    version VARCHAR(20),
    category VARCHAR(30) NOT NULL,
    download_url VARCHAR(500),
    download_count INT DEFAULT 0,
    avg_rating DOUBLE DEFAULT 0,
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ma_category (category),
    INDEX idx_ma_name (name)
)
