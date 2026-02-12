CREATE TABLE mcp_snippet_tags (
    id CHAR(36) PRIMARY KEY,
    snippet_id CHAR(36) NOT NULL,
    tag VARCHAR(100) NOT NULL,
    CONSTRAINT fk_snippet_tags_snippet FOREIGN KEY (snippet_id) REFERENCES mcp_snippets(id) ON DELETE CASCADE,
    CONSTRAINT uq_snippet_tag UNIQUE (snippet_id, tag)
)
