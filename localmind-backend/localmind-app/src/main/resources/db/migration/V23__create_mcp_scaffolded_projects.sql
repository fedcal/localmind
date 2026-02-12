CREATE TABLE mcp_scaffolded_projects (
    id CHAR(36) NOT NULL PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    output_path VARCHAR(500),
    files_generated INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
