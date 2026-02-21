# MCP Troubleshooting in LocalMind

**Project:** LocalMind - Local-First AI Platform
**Version:** 0.1.0
**Last updated:** 2026-02-09
**Reference module:** localmind-infrastructure (`infrastructure.mcp`)

---

## Table of Contents

1. [Common Problems and Solutions](#1-common-problems-and-solutions)
2. [Logging and Debugging](#2-logging-and-debugging)
3. [Connection Status Verification](#3-connection-status-verification)
4. [Manual Testing with curl](#4-manual-testing-with-curl)
5. [Troubleshooting Checklist](#5-troubleshooting-checklist)

---

## 1. Common Problems and Solutions

### 1.1 MCP Server Does Not Connect (STDIO)

**Symptom:** The server status remains `DISCONNECTED` or changes to `ERROR` after registration.

**Possible causes and solutions:**

| Cause                              | Diagnosis                                      | Solution                                       |
|------------------------------------|-------------------------------------------------|------------------------------------------------|
| Command not found                  | Log: `Cannot run program "npx"`                | Verify that the command is in PATH             |
| Node.js not installed              | `node --version` returns error                 | Install Node.js >= 18                          |
| npm package does not exist         | Log: `npm ERR! 404 Not Found`                  | Verify the package name                        |
| Incorrect arguments                | Log: `Error: invalid argument`                 | Check the args in `McpServerConfig`            |
| Insufficient permissions           | Log: `Permission denied`                       | Verify filesystem permissions                  |
| Process terminates immediately     | Log: `Process exited with code 1`              | Run the command manually to verify             |

**Manual STDIO command verification:**

```bash
# Test the command manually before registering it
npx -y @modelcontextprotocol/server-filesystem /home/user/documents

# If the server starts correctly, it will display an initialization message
# Ctrl+C to terminate
```

**Verify that npx is in the Java process PATH:**

```bash
# Verify the PATH available to Java
which npx
echo $PATH

# If npx is not in the global PATH, specify the full path:
# command: "/usr/local/bin/npx" (or the actual path)
```

### 1.2 MCP Server Does Not Connect (SSE)

**Symptom:** The connection to a remote SSE server fails.

| Cause                              | Diagnosis                                      | Solution                                       |
|------------------------------------|-------------------------------------------------|------------------------------------------------|
| URL unreachable                    | Log: `Connection refused`                      | Verify that the server is running              |
| Incorrect URL                      | Log: `404 Not Found`                           | Verify the URL (must end with `/sse`)          |
| Firewall blocks the connection     | Log: `Connection timed out`                    | Verify firewall/proxy rules                    |
| CORS not configured                | Log: `CORS error` (browser only)               | Configure CORS on the MCP server               |
| SSL/TLS mismatch                   | Log: `SSL handshake failed`                    | Verify SSL certificates                        |

**Manual SSE connection verification:**

```bash
# Test the SSE endpoint with curl
curl -v -N http://mcp-server.local:3001/sse

# Expected response: SSE event stream
# Content-Type: text/event-stream
# data: {"jsonrpc":"2.0",...}
```

### 1.3 Tool Discovery Returns Empty List

**Symptom:** `GET /api/v1/mcp/tools/servers/{serverId}` returns `[]`.

**Possible causes:**

1. **Server not connected:** Verify that the status is `CONNECTED`
2. **Server does not support tools:** Some servers only expose resources or prompts
3. **Implementation in progress:** The `McpClientConnection.getTools()` method contains
   a `// TODO` and returns `List.of()` in the current version

```java
// SpringAiMcpClientAdapter.McpClientConnection
List<McpExternalTool> getTools() {
    // TODO: Use MCP SDK to discover tools from the connected server
    return List.of();
}
```

**Solution:** Complete the `getTools()` implementation in `McpClientConnection`
using the MCP SDK to send `tools/list` to the server.

### 1.4 Tool Execution Fails with Timeout

**Symptom:** `executeTool()` returns `success=false` with a timeout error.

**Solutions:**

```bash
# Increase the timeout when registering the server
curl -X POST http://localhost:8080/api/v1/mcp/servers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Slow server",
    "type": "SSE",
    "url": "http://server.local:3001/sse",
    "timeoutSeconds": 120
  }'
```

**Note:** The timeout is configured per server in `McpServerConfig.timeoutSeconds`.
The default value is 30 seconds. For long operations (e.g., web scraping,
queries on large datasets), increase to 60-120 seconds.

### 1.5 JSON Serialization Errors

**Symptom:** Tool execution returns a JSON parsing error.

**Common causes:**

1. **Incompatible types:** The tool result contains non-serializable Java types
2. **Circular references:** Objects with circular references in the result
3. **Unhandled null fields:** Jackson includes nulls by default

**Solution:** Check logs at DEBUG level to see the raw JSON payload:

```yaml
logging:
  level:
    io.modelcontextprotocol: TRACE
    org.springframework.ai.mcp: DEBUG
```

---

## 2. Logging and Debugging

### 2.1 MCP Logging Configuration

Add the following properties in `application-dev.yml` for complete debugging:

```yaml
logging:
  level:
    # MCP Java SDK logs
    io.modelcontextprotocol: DEBUG

    # Spring AI MCP logs
    org.springframework.ai.mcp: DEBUG

    # LocalMind MCP adapter logs
    com.localmind.infrastructure.mcp: DEBUG

    # MCP domain service logs
    com.localmind.domain.mcp: DEBUG

    # MCP API controller logs
    com.localmind.api.mcp: DEBUG
```

### 2.2 Log Patterns for Diagnostics

Log messages follow a pattern that facilitates filtering:

```
# Connecting to MCP server
2026-02-09 10:30:01 [main] INFO  c.l.i.mcp.adapter.SpringAiMcpClientAdapter -
    Connecting to MCP server: Filesystem locale (STDIO)

# Connection successful
2026-02-09 10:30:02 [main] INFO  c.l.i.mcp.adapter.SpringAiMcpClientAdapter -
    Connected to MCP server: Filesystem locale

# Connection failed
2026-02-09 10:30:02 [main] ERROR c.l.i.mcp.adapter.SpringAiMcpClientAdapter -
    Failed to connect to MCP server: Filesystem locale
    java.io.IOException: Cannot run program "npx": No such file or directory
```

### 2.3 Log Filtering

```bash
# Filter only MCP logs in log files
grep "mcp\|MCP\|McpServer\|McpTool\|McpClient" application.log

# Filter connection logs
grep "Connecting to MCP\|Connected to MCP\|Failed to connect" application.log

# Filter tool execution logs
grep "executeTool\|tool execution\|tools/call" application.log
```

---

## 3. Connection Status Verification

### 3.1 Server List with Status

```bash
curl -s http://localhost:8080/api/v1/mcp/servers | python3 -m json.tool
```

**Status interpretation:**

| Status         | Meaning                                      | Action                            |
|----------------|----------------------------------------------|-----------------------------------|
| `CONNECTED`    | Active and working connection                | No action needed                  |
| `DISCONNECTED` | Not connected (initial state after error)    | Try `reconnect`                   |
| `CONNECTING`   | Connection in progress                       | Wait a few seconds                |
| `ERROR`        | Connection error                             | Check logs, then `reconnect`      |

### 3.2 Connection Test for a Specific Server

```bash
# Test the connection and update the status
curl -s -X POST http://localhost:8080/api/v1/mcp/servers/{serverId}/test | python3 -m json.tool

# Reconnect a server in error state
curl -X POST http://localhost:8080/api/v1/mcp/servers/{serverId}/reconnect
```

### 3.3 Available Tools Verification

```bash
# All tools from all connected servers
curl -s http://localhost:8080/api/v1/mcp/tools | python3 -m json.tool

# Tools from a specific server
curl -s http://localhost:8080/api/v1/mcp/tools/servers/{serverId} | python3 -m json.tool

# LocalMind local tools
curl -s http://localhost:8080/api/v1/mcp/tools/local | python3 -m json.tool
```

---

## 4. Manual Testing with curl

### 4.1 Complete Test Cycle

Bash script for a complete MCP system test:

```bash
#!/bin/bash
# test-mcp.sh - Complete test of the LocalMind MCP system
BASE_URL="http://localhost:8080/api/v1/mcp"

echo "=== 1. Register filesystem server ==="
SERVER_ID=$(curl -s -X POST "$BASE_URL/servers" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Filesystem",
    "type": "STDIO",
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-filesystem", "/tmp"],
    "timeoutSeconds": 30
  }' | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
echo "Server ID: $SERVER_ID"

echo ""
echo "=== 2. Verify server status ==="
curl -s "$BASE_URL/servers/$SERVER_ID" | python3 -m json.tool

echo ""
echo "=== 3. Test connection ==="
curl -s -X POST "$BASE_URL/servers/$SERVER_ID/test" | python3 -m json.tool

echo ""
echo "=== 4. Tool discovery ==="
curl -s "$BASE_URL/tools/servers/$SERVER_ID" | python3 -m json.tool

echo ""
echo "=== 5. List all tools (local + external) ==="
curl -s "$BASE_URL/tools" | python3 -m json.tool
curl -s "$BASE_URL/tools/local" | python3 -m json.tool

echo ""
echo "=== 6. Execute tool (list_directory) ==="
curl -s -X POST "$BASE_URL/tools/execute" \
  -H "Content-Type: application/json" \
  -d "{
    \"toolName\": \"list_directory\",
    \"serverId\": \"$SERVER_ID\",
    \"arguments\": { \"path\": \"/tmp\" }
  }" | python3 -m json.tool

echo ""
echo "=== 7. Cleanup - Remove server ==="
curl -s -X DELETE "$BASE_URL/servers/$SERVER_ID"
echo "Server $SERVER_ID removed"
```

### 4.2 Testing LocalMind as MCP Server

To test LocalMind as an MCP server, verify that the SSE endpoint is reachable:

```bash
# Verify LocalMind MCP server SSE endpoint
curl -v -N http://localhost:8080/mcp/sse

# Send a JSON-RPC initialization message
curl -X POST http://localhost:8080/mcp/message \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {},
      "clientInfo": {
        "name": "test-client",
        "version": "1.0.0"
      }
    }
  }'

# List tools exposed by LocalMind
curl -X POST http://localhost:8080/mcp/message \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }'
```

---

## 5. Troubleshooting Checklist

When something is not working, follow this ordered checklist:

### MCP Server (STDIO)

- [ ] **Is the backend running?** `curl http://localhost:8080/actuator/health`
- [ ] **Is the MCP client enabled?** Verify `localmind.mcp.client.enabled=true` in `application-dev.yml`
- [ ] **Is the command installed?** Run `which npx` (or the specified command)
- [ ] **Is Node.js installed?** `node --version` (required >= 18)
- [ ] **Does the command work manually?** Run the command with the same args
- [ ] **Are the permissions correct?** Verify permissions on the target directory
- [ ] **Has the server status been checked?** `GET /api/v1/mcp/servers`
- [ ] **Do the logs show errors?** Check logs with DEBUG level enabled
- [ ] **Has a reconnection been attempted?** `POST /api/v1/mcp/servers/{id}/reconnect`

### MCP Server (SSE)

- [ ] **Is the remote server running?** `curl -v http://server:port/sse`
- [ ] **Is the URL correct?** Verify host, port, and path (`/sse`)
- [ ] **Is the network reachable?** `ping server-host` or `telnet server-host port`
- [ ] **Does the firewall allow the connection?** Verify firewall rules
- [ ] **Are the SSL certificates valid?** (if HTTPS) `curl -v https://server:port/sse`

### MCP Tools

- [ ] **Is the server CONNECTED?** Verify the server status
- [ ] **Does discovery work?** `GET /api/v1/mcp/tools/servers/{serverId}`
- [ ] **Are the arguments correct?** Verify the tool's `inputSchema`
- [ ] **Is the timeout sufficient?** Increase `timeoutSeconds` if necessary
- [ ] **Is the result serializable?** Check logs for serialization errors

### LocalMind as MCP Server

- [ ] **Is the MCP server enabled?** Verify `localmind.mcp.server.enabled=true`
- [ ] **Does the SSE endpoint respond?** `curl -N http://localhost:8080/mcp/sse`
- [ ] **Does the message endpoint respond?** `curl -X POST http://localhost:8080/mcp/message`
- [ ] **Are the tool beans loaded?** Verify in logs that `LocalMindMcpTools` is initialized
- [ ] **Does the MCP client see the tools?** Send `tools/list` via JSON-RPC

---

> **Documentation navigation:**
> - Previous: [06-agent-integration.md](06-agent-integration.md)
> - Overview: [01-mcp-protocol-overview.md](01-mcp-protocol-overview.md)
> - Configuration: [04-configuration.md](04-configuration.md)
