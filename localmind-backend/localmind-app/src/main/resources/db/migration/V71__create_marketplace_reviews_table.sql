CREATE TABLE marketplace_reviews (
    id CHAR(36) PRIMARY KEY,
    agent_id CHAR(36) NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_mr_agent (agent_id),
    FOREIGN KEY (agent_id) REFERENCES marketplace_agents(id)
)
