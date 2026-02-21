export const content = `# MCP Usage Examples in LocalMind

**Project:** LocalMind - Local-First AI Platform
**Version:** 0.1.0
**Last updated:** 2026-02-09
**Prerequisites:** LocalMind backend started with \`dev\` profile

---

## Table of Contents

1. [Example 1: Connection to filesystem MCP server](#example-1-connection-to-filesystem-mcp-server)
2. [Example 2: Use from Claude Desktop](#example-2-use-from-claude-desktop)
3. [Example 3: Connection to remote SSE server](#example-3-connection-to-remote-sse-server)
4. [Example 4: REST API - Register a server](#example-4-rest-api---register-a-server)
5. [Example 5: REST API - Test connection](#example-5-rest-api---test-connection)
6. [Example 6: REST API - Discover tools](#example-6-rest-api---discover-tools)
7. [Example 7: REST API - Execute a tool](#example-7-rest-api---execute-a-tool)
8. [Use from the Angular frontend](#8-use-from-the-angular-frontend)

---

## Example 1: Connection to filesystem MCP server

The MCP server \`@modelcontextprotocol/server-filesystem\` allows reading and writing files
on the local filesystem. This is one of the most common and useful MCP servers.

### Prerequisites

\`\`\`bash
# Make sure Node.js >= 18 is installed
node --version

# The server starts automatically via npx (no installation required)
\`\`\`

### Registration via API

\`\`\`bash
curl -X POST http://localhost:8080/api/v1/mcp/servers \\
  -H "Content-Type: application/json" \\
  -d '{
    "name": "Local Filesystem",
    "description": "Access to files in the documents directory",
    "type": "STDIO",
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-filesystem", "/home/user/documents"],
    "timeoutSeconds": 30,
    "autoReconnect": true
  }'
\`\`\`

### Expected response

\`\`\`json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Local Filesystem",
  "description": "Access to files in the documents directory",
  "type": "STDIO",
  "status": "CONNECTED",
  "config": {
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-filesystem", "/home/user/documents"],
    "timeoutSeconds": 30,
    "autoReconnect": true
  },
  "createdAt": "2026-02-09T10:30:00",
  "lastConnectedAt": "2026-02-09T10:30:01"
}
\`\`\`

### Available tools after connection

The filesystem server typically exposes the following tools:

| Tool             | Description                                |
|------------------|--------------------------------------------|
| \`read_file\`      | Read the contents of a file                |
| \`write_file\`     | Write content to a file                    |
| \`list_directory\` | List the contents of a directory           |
| \`create_directory\`| Create a new directory                    |
| \`move_file\`      | Move/rename a file                         |
| \`search_files\`   | Search files by pattern                    |
| \`get_file_info\`  | Get file metadata                          |

---

## Example 2: Use from Claude Desktop

LocalMind can be used as an MCP server by **Claude Desktop** (or any other MCP client).
This exposes the RAG knowledge base and the LLM gateway to Claude.

### claude_desktop_config.json configuration

Locate the Claude Desktop configuration file:

- **macOS:** \`~/Library/Application Support/Claude/claude_desktop_config.json\`
- **Windows:** \`%APPDATA%\\Claude\\claude_desktop_config.json\`
- **Linux:** \`~/.config/Claude/claude_desktop_config.json\`

Add the LocalMind server configuration:

\`\`\`json
{
  "mcpServers": {
    "localmind": {
      "url": "http://localhost:8080/mcp/sse",
      "description": "LocalMind knowledge base and LLM gateway"
    }
  }
}
\`\`\`

### Connection verification

After restarting Claude Desktop, verify that the LocalMind tools are visible.
Claude will show a hammer icon (tool) with the following tools:

- **document_search** - Search in the knowledge base
- **chat** - Send messages to the LLM gateway
- **list_models** - List available providers

### Example interaction in Claude Desktop

\`\`\`
User: "Search my knowledge base for information about hexagonal architecture"

Claude: [Calls document_search with query="hexagonal architecture", topK=5]
        "I found 5 relevant documents in your knowledge base..."
\`\`\`

---

## Example 3: Connection to remote SSE server

To connect to an MCP server that exposes a remote SSE endpoint (for example, a corporate
MCP server or a cloud service).

### SSE server registration

\`\`\`bash
curl -X POST http://localhost:8080/api/v1/mcp/servers \\
  -H "Content-Type: application/json" \\
  -d '{
    "name": "Corporate MCP Server",
    "description": "MCP server with access to the corporate database",
    "type": "SSE",
    "url": "http://mcp-server.company.local:3001/sse",
    "timeoutSeconds": 60,
    "autoReconnect": true
  }'
\`\`\`

### Response

\`\`\`json
{
  "id": "f1e2d3c4-b5a6-7890-fedc-ba0987654321",
  "name": "Corporate MCP Server",
  "type": "SSE",
  "status": "CONNECTED",
  "config": {
    "url": "http://mcp-server.company.local:3001/sse",
    "timeoutSeconds": 60,
    "autoReconnect": true
  },
  "createdAt": "2026-02-09T14:00:00",
  "lastConnectedAt": "2026-02-09T14:00:02"
}
\`\`\`

---

## Example 4: REST API - Register a server

### Request

\`\`\`bash
# Register STDIO server (local process)
curl -X POST http://localhost:8080/api/v1/mcp/servers \\
  -H "Content-Type: application/json" \\
  -d '{
    "name": "SQLite Explorer",
    "description": "MCP server for SQLite database access",
    "type": "STDIO",
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-sqlite", "/path/to/database.db"],
    "timeoutSeconds": 30,
    "autoReconnect": true
  }'
\`\`\`

### Validation

The \`name\` field is required (\`@NotBlank\`). The \`type\` field is required (\`@NotNull\`)
and must be \`"STDIO"\` or \`"SSE"\`. Database constraints ensure that:

- For \`STDIO\`: \`command\` is present
- For \`SSE\`: \`url\` is present

### List all registered servers

\`\`\`bash
curl -X GET http://localhost:8080/api/v1/mcp/servers
\`\`\`

### Response

\`\`\`json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Local Filesystem",
    "type": "STDIO",
    "status": "CONNECTED"
  },
  {
    "id": "f1e2d3c4-b5a6-7890-fedc-ba0987654321",
    "name": "Corporate MCP Server",
    "type": "SSE",
    "status": "CONNECTED"
  }
]
\`\`\`

### Delete a server

\`\`\`bash
curl -X DELETE http://localhost:8080/api/v1/mcp/servers/a1b2c3d4-e5f6-7890-abcd-ef1234567890
# Response: 204 No Content
\`\`\`

---

## Example 5: REST API - Test connection

### Request

\`\`\`bash
curl -X POST http://localhost:8080/api/v1/mcp/servers/a1b2c3d4-e5f6-7890-abcd-ef1234567890/test
\`\`\`

### Response (successful connection)

\`\`\`json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Local Filesystem",
  "type": "STDIO",
  "status": "CONNECTED",
  "lastConnectedAt": "2026-02-09T15:30:00"
}
\`\`\`

### Response (failed connection)

\`\`\`json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Local Filesystem",
  "type": "STDIO",
  "status": "ERROR",
  "lastConnectedAt": null
}
\`\`\`

### Manual reconnection

\`\`\`bash
curl -X POST http://localhost:8080/api/v1/mcp/servers/a1b2c3d4-e5f6-7890-abcd-ef1234567890/reconnect
# Response: 200 OK
\`\`\`

---

## Example 6: REST API - Discover tools

### All tools from all connected servers

\`\`\`bash
curl -X GET http://localhost:8080/api/v1/mcp/tools
\`\`\`

### Response

\`\`\`json
[
  {
    "name": "read_file",
    "description": "Read the complete contents of a file from the file system",
    "inputSchema": "{"type":"object","properties":{"path":{"type":"string"}},"required":["path"]}",
    "serverId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "local": false
  },
  {
    "name": "list_directory",
    "description": "List directory contents with file metadata",
    "inputSchema": "{"type":"object","properties":{"path":{"type":"string"}}}",
    "serverId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "local": false
  }
]
\`\`\`

### Tools from a specific server

\`\`\`bash
curl -X GET http://localhost:8080/api/v1/mcp/tools/servers/a1b2c3d4-e5f6-7890-abcd-ef1234567890
\`\`\`

### LocalMind local tools

\`\`\`bash
curl -X GET http://localhost:8080/api/v1/mcp/tools/local
\`\`\`

### Response

\`\`\`json
[
  {
    "name": "document_search",
    "description": "Search documents using RAG",
    "local": true
  },
  {
    "name": "chat",
    "description": "Send a message to an LLM",
    "local": true
  },
  {
    "name": "list_models",
    "description": "List available LLM providers",
    "local": true
  }
]
\`\`\`

---

## Example 7: REST API - Execute a tool

### Execute \`read_file\` on the filesystem server

\`\`\`bash
curl -X POST http://localhost:8080/api/v1/mcp/tools/execute \\
  -H "Content-Type: application/json" \\
  -d '{
    "toolName": "read_file",
    "serverId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "arguments": {
      "path": "/home/user/documents/readme.md"
    }
  }'
\`\`\`

### Response (success)

\`\`\`json
{
  "toolName": "read_file",
  "result": "# My Project\\n\\nThis is the README file content...",
  "success": true,
  "errorMessage": null,
  "executionTimeMs": 45
}
\`\`\`

### Response (error)

\`\`\`json
{
  "toolName": "read_file",
  "result": null,
  "success": false,
  "errorMessage": "File not found: /home/user/documents/nonexistent.txt",
  "executionTimeMs": 12
}
\`\`\`

### Execute \`list_directory\`

\`\`\`bash
curl -X POST http://localhost:8080/api/v1/mcp/tools/execute \\
  -H "Content-Type: application/json" \\
  -d '{
    "toolName": "list_directory",
    "serverId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "arguments": {
      "path": "/home/user/documents"
    }
  }'
\`\`\`

---

## 8. Use from the Angular frontend

The LocalMind Angular frontend can interact with MCP through the REST APIs described
above. Below is the suggested Angular service architecture.

### Angular MCP Service

\`\`\`typescript
// mcp-server.service.ts
@Injectable({ providedIn: 'root' })
export class McpServerService {
  private readonly apiUrl = '/api/v1/mcp/servers';

  constructor(private http: HttpClient) {}

  register(request: CreateMcpServerRequest): Observable<McpServer> {
    return this.http.post<McpServer>(this.apiUrl, request);
  }

  listAll(): Observable<McpServer[]> {
    return this.http.get<McpServer[]>(this.apiUrl);
  }

  get(serverId: string): Observable<McpServer> {
    return this.http.get<McpServer>(\`\${this.apiUrl}/\${serverId}\`);
  }

  remove(serverId: string): Observable<void> {
    return this.http.delete<void>(\`\${this.apiUrl}/\${serverId}\`);
  }

  testConnection(serverId: string): Observable<McpServer> {
    return this.http.post<McpServer>(\`\${this.apiUrl}/\${serverId}/test\`, {});
  }

  reconnect(serverId: string): Observable<void> {
    return this.http.post<void>(\`\${this.apiUrl}/\${serverId}/reconnect\`, {});
  }
}
\`\`\`

\`\`\`typescript
// mcp-tool.service.ts
@Injectable({ providedIn: 'root' })
export class McpToolService {
  private readonly apiUrl = '/api/v1/mcp/tools';

  constructor(private http: HttpClient) {}

  listAllTools(): Observable<McpTool[]> {
    return this.http.get<McpTool[]>(this.apiUrl);
  }

  listServerTools(serverId: string): Observable<McpTool[]> {
    return this.http.get<McpTool[]>(\`\${this.apiUrl}/servers/\${serverId}\`);
  }

  listLocalTools(): Observable<McpTool[]> {
    return this.http.get<McpTool[]>(\`\${this.apiUrl}/local\`);
  }

  executeTool(request: McpToolExecutionRequest): Observable<McpToolExecutionResult> {
    return this.http.post<McpToolExecutionResult>(\`\${this.apiUrl}/execute\`, request);
  }
}
\`\`\`

### TypeScript interfaces

\`\`\`typescript
// mcp.models.ts
export interface McpServer {
  id: string;
  name: string;
  description?: string;
  type: 'STDIO' | 'SSE';
  status: 'CONNECTED' | 'DISCONNECTED' | 'ERROR' | 'CONNECTING';
  config: McpServerConfig;
  createdAt: string;
  lastConnectedAt?: string;
}

export interface McpServerConfig {
  command?: string;
  args?: string[];
  url?: string;
  timeoutSeconds?: number;
  autoReconnect?: boolean;
}

export interface CreateMcpServerRequest {
  name: string;
  description?: string;
  type: 'STDIO' | 'SSE';
  command?: string;
  args?: string[];
  url?: string;
  timeoutSeconds?: number;
  autoReconnect?: boolean;
}

export interface McpTool {
  name: string;
  description: string;
  inputSchema?: string;
  serverId?: string;
  local: boolean;
}

export interface McpToolExecutionRequest {
  toolName: string;
  serverId: string;
  arguments: Record<string, unknown>;
}

export interface McpToolExecutionResult {
  toolName: string;
  result: unknown;
  success: boolean;
  errorMessage?: string;
  executionTimeMs: number;
}
\`\`\`

### Angular component example

\`\`\`typescript
// mcp-servers-list.component.ts
@Component({
  selector: 'app-mcp-servers-list',
  template: \`
    <div class="mcp-servers">
      <h2>Registered MCP Servers</h2>
      @for (server of servers$ | async; track server.id) {
        <div class="server-card" [class.connected]="server.status === 'CONNECTED'">
          <h3>{{ server.name }}</h3>
          <span class="badge">{{ server.status }}</span>
          <span class="type">{{ server.type }}</span>
          <button (click)="testConnection(server.id)">Test</button>
          <button (click)="remove(server.id)">Remove</button>
        </div>
      }
    </div>
  \`
})
export class McpServersListComponent implements OnInit {
  servers$ = this.mcpServerService.listAll();

  constructor(private mcpServerService: McpServerService) {}

  testConnection(serverId: string) {
    this.mcpServerService.testConnection(serverId).subscribe(
      server => console.log('Test result:', server.status)
    );
  }

  remove(serverId: string) {
    this.mcpServerService.remove(serverId).subscribe(
      () => this.servers$ = this.mcpServerService.listAll()
    );
  }
}
\`\`\`

---

> **Documentation navigation:**
> - Previous: [04-configuration.md](/docs/12-mcp-integration/configuration)
> - Next: [06-agent-integration.md](/docs/12-mcp-integration/agent-integration)
> - Troubleshooting: [07-troubleshooting.md](/docs/12-mcp-integration/troubleshooting)
`;
