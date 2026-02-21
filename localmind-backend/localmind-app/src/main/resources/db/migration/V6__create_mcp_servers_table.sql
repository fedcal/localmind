-- MCP Server registrations
CREATE TABLE mcp_servers (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    command VARCHAR(500),
    args TEXT,
    url VARCHAR(500),
    timeout_seconds INTEGER DEFAULT 30,
    auto_reconnect BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_connected_at TIMESTAMP NULL,
    CONSTRAINT chk_type CHECK (type IN ('STDIO', 'SSE')),
    CONSTRAINT chk_status CHECK (status IN ('CONNECTED', 'DISCONNECTED', 'ERROR', 'CONNECTING')),
    CONSTRAINT chk_stdio_command CHECK (type != 'STDIO' OR command IS NOT NULL),
    CONSTRAINT chk_sse_url CHECK (type != 'SSE' OR url IS NOT NULL)
);

CREATE INDEX idx_mcp_servers_status ON mcp_servers(status);
CREATE INDEX idx_mcp_servers_type ON mcp_servers(type);
