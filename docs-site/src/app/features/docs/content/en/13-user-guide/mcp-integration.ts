export const content = `# User Guide - MCP Integration

## Access

Navigate to **MCP** from the sidebar or visit \`http://localhost:4200/mcp\`.

## Overview

The MCP (Model Context Protocol) section allows you to register external MCP servers and to explore and execute the tools that these servers provide. MCP is a standard protocol for integrating tools and resources with AI models.

The interface is organized in two tabs:
- **External Servers**: registration and management of MCP servers
- **Available Tools**: exploration and execution of tools

---

## Tab: External Servers

### Registering an MCP Server

1. Click **+ Add Server**
2. Fill out the registration form:

#### Common Fields

| Field | Required | Description |
|-------|----------|-------------|
| **Name** | Yes | Identifying name of the server |
| **Description** | No | Description of the server's capabilities |
| **Type** | Yes | Connection type: STDIO or SSE |
| **Timeout** | No | Timeout in seconds (5-300, default 30) |
| **Auto-reconnect** | No | Checkbox for automatic reconnection (default: enabled) |

#### Fields Specific to STDIO Type

| Field | Required | Description | Example |
|-------|----------|-------------|---------|
| **Command** | Yes | Command to execute | \`npx\` |
| **Arguments** | No | Comma-separated arguments | \`-y,@modelcontextprotocol/server-filesystem,./\` |

#### Fields Specific to SSE Type

| Field | Required | Description | Example |
|-------|----------|-------------|---------|
| **URL** | Yes | URL of the server's SSE endpoint | \`http://localhost:8082/sse\` |

3. Click **Register** to save

### List of Registered Servers

Each server is displayed as a card with:

- **Name** of the server
- **Status badge** with color coding:
  - **CONNECTED** (green): connected and operational
  - **DISCONNECTED** (gray): disconnected
  - **ERROR** (red): connection error
  - **CONNECTING** (orange): connection attempt in progress
- **Description** (if present)
- **Type badge**: STDIO or SSE
- **Command** or **URL** (based on type, monospace font)
- **Arguments** (if present, monospace font)

### Server Actions

| Action | Description |
|--------|-------------|
| **Test** | Verifies connectivity with the server. Shows a spinner during the test and a notification with the result. |
| **Reconnect** | Attempts to reconnect to the server. Shows a spinner during the attempt. |
| **Delete** | Removes the server from the configuration (red button). |

---

## Tab: Available Tools

### Local Tools Section

Shows the tools exposed directly by LocalMind (implemented in the backend). Each tool is presented as a card with:
- **Name** of the tool (monospace font)
- **Description** of the functionality
- **Badge** "Local" (blue)

### External Tools Section

Shows the tools available on connected MCP servers. Each tool shows:
- **Name** of the tool (monospace font)
- **Description** of the functionality
- **Badge** "External" (purple)
- **Server ID** abbreviated (first 8 characters)

If there are no external tools, the suggestion is shown: "Connect an MCP server to view external tools."

### Executing a Tool

1. Click on the tool card to execute (it highlights with border and shadow)
2. In the section that appears below, the following are shown:
   - **Name** and **description** of the tool
   - **Input schema**: the JSON structure of the required arguments (in JSON format, monospace font, scrollable area)
3. Fill in the **Arguments** field in JSON format (4-line textarea)
4. Click **Execute**

### Execution Results

After execution, the card shows:
- **Status badge**: green "success" or red "error"
- **Execution time** in milliseconds
- **Error message** (only in case of failure)
- **Result data**: tool output in JSON format (dark background, monospace font, scrollable area)

### Tool List Refresh

The **Refresh** button in the header reloads the list of all available tools (local and external).
`;
