export const content = `# MCP Integration with LocalMind AI Agents

**Project:** LocalMind - Local-First AI Platform
**Version:** 0.1.0
**Last updated:** 2026-02-09
**Reference module:** localmind-domain (\`domain.mcp\`, \`domain.agent\`)

---

## Table of Contents

1. [Overview](#1-overview)
2. [Integration architecture](#2-integration-architecture)
3. [Flow: Agent with MCP tools](#3-flow-agent-with-mcp-tools)
4. [Mapping McpExternalTool -> AgentTool](#4-mapping-mcpexternaltool---agenttool)
5. [Use case scenarios](#5-use-case-scenarios)
6. [Extensibility](#6-extensibility)

---

## 1. Overview

LocalMind AI agents can use tools from external MCP servers to expand their capabilities
beyond local tools (RAG search, LLM chat). This transforms LocalMind from a closed platform
into an extensible AI hub, where each MCP server adds new capabilities without code changes.

\`\`\`
+------------------------------------------------------+
|                   LocalMind AI Agent                 |
|                                                      |
|  "Find the .pdf files in the documents folder,       |
|   index them in the knowledge base, and generate     |
|   a summary for each one"                            |
|                                                      |
|  Available tools:                                    |
|  +-- document_search (local)                         |
|  +-- chat (local)                                    |
|  +-- list_directory (MCP: filesystem server)         |
|  +-- read_file (MCP: filesystem server)              |
|  +-- write_file (MCP: filesystem server)             |
+------------------------------------------------------+
\`\`\`

The agent sees a unified list of tools (local + external MCP) and can dynamically
choose which ones to use based on the task requested by the user.

---

## 2. Integration architecture

The integration between agents and MCP leverages the ports/use cases already defined in the domain:

\`\`\`
+--------------------------------------------+
|              Agent Domain                   |
|                                             |
|  AgentOrchestrator                          |
|       |                                     |
|       +-- Tool Selection (LLM decides)      |
|       |       |                             |
|       |       +-- Local Tool                |
|       |       |     (DocumentSearchUseCase) |
|       |       |                             |
|       |       +-- External MCP Tool         |
|       |             |                       |
|       |             v                       |
|  +----+-----------------------------------+ |
|  |    McpToolDiscoveryUseCase             | |
|  |    McpToolExecutionUseCase             | |
|  +----------------------------------------+ |
+--------------------------------------------+
              |
              v
+--------------------------------------------+
|         Infrastructure Layer                |
|  SpringAiMcpClientAdapter                   |
|  (connections to external MCP servers)      |
+--------------------------------------------+
\`\`\`

### Components involved

| Component                   | Layer          | Responsibility                            |
|-----------------------------|----------------|-------------------------------------------|
| \`AgentOrchestrator\`         | Domain (agent) | Agent cycle orchestration                 |
| \`McpToolDiscoveryUseCase\`   | Domain (mcp)   | Tool discovery from connected servers      |
| \`McpToolExecutionUseCase\`   | Domain (mcp)   | Tool execution on external servers         |
| \`McpToolOrchestratorService\`| Domain (mcp)   | Implementation of the two use cases        |
| \`SpringAiMcpClientAdapter\`  | Infrastructure  | Actual communication via MCP SDK           |

---

## 3. Flow: Agent with MCP tools

Below is the detailed flow when an agent uses an external MCP tool:

### Phase 1: Discovery of available tools

\`\`\`
1. AgentOrchestrator starts a new task
2. Retrieves local tools (document_search, chat, list_models)
3. Calls McpToolDiscoveryUseCase.listAllExternalTools()
4. McpToolOrchestratorService iterates over CONNECTED servers
5. For each server: McpClientPort.discoverTools(serverId)
6. Aggregates all tools into a unified list
7. Prepares the tool list for the LLM model
\`\`\`

### Phase 2: Tool selection by the LLM

\`\`\`
8. AgentOrchestrator sends to the LLM:
   - The user's message
   - The list of available tools (local + MCP)
   - The conversation context
9. The LLM analyzes the task and decides which tool to call
10. The LLM returns a tool_call with:
    - tool name
    - arguments (JSON)
\`\`\`

### Phase 3: MCP tool execution

\`\`\`
11. AgentOrchestrator checks if the tool is local or external (MCP)
12. If MCP: creates McpToolExecutionRequest with:
    - toolName: tool name chosen by the LLM
    - serverId: ID of the server exposing the tool
    - arguments: arguments from the LLM
13. Calls McpToolExecutionUseCase.executeTool(request)
14. McpToolOrchestratorService delegates to McpClientPort.executeTool()
15. SpringAiMcpClientAdapter sends JSON-RPC tools/call to the MCP server
16. Receives the result and wraps it in McpToolExecutionResult
17. AgentOrchestrator provides the result to the LLM as tool_result
18. The LLM continues reasoning with the tool result
\`\`\`

### Sequence diagram

\`\`\`
User     Agent        LLM         McpToolDiscovery   McpToolExecution   MCP Server
  |          |           |                |                  |                |
  |--task--->|           |                |                  |                |
  |          |--listAll->|                |                  |                |
  |          |           |<--tool list----|                  |                |
  |          |--prompt-->|                |                  |                |
  |          |<-tool_call|                |                  |                |
  |          |           |                |--executeTool---->|                |
  |          |           |                |                  |--tools/call--->|
  |          |           |                |                  |<--result-------|
  |          |           |                |<--result---------|                |
  |          |--result-->|                |                  |                |
  |          |<-response-|                |                  |                |
  |<-answer--|           |                |                  |                |
\`\`\`

---

## 4. Mapping McpExternalTool -> AgentTool

To integrate MCP tools with the agent system, a mapping between the MCP model
and the agent model is needed.

### MCP Model

\`\`\`java
// com.localmind.domain.mcp.model.McpExternalTool
public class McpExternalTool {
    private String name;           // "read_file"
    private String description;    // "Read the complete contents of a file"
    private String inputSchema;    // JSON Schema as string
    private String serverId;       // "a1b2c3d4-..."
}
\`\`\`

### Agent Model (suggested)

\`\`\`java
// com.localmind.domain.agent.model.AgentTool
public class AgentTool {
    private String name;
    private String description;
    private String parametersSchema;   // JSON Schema
    private ToolSource source;         // LOCAL or MCP
    private String mcpServerId;        // Only for source=MCP
}

public enum ToolSource {
    LOCAL,   // Local tools (document_search, chat, etc.)
    MCP      // Tools from external MCP servers
}
\`\`\`

### Mapping function

\`\`\`java
public static AgentTool fromMcpExternalTool(McpExternalTool mcpTool) {
    return AgentTool.builder()
            .name(mcpTool.getName())
            .description(mcpTool.getDescription())
            .parametersSchema(mcpTool.getInputSchema())
            .source(ToolSource.MCP)
            .mcpServerId(mcpTool.getServerId())
            .build();
}
\`\`\`

### Format for the LLM (OpenAI function calling compatible)

\`\`\`json
{
  "type": "function",
  "function": {
    "name": "read_file",
    "description": "Read the complete contents of a file from the file system",
    "parameters": {
      "type": "object",
      "properties": {
        "path": {
          "type": "string",
          "description": "The path of the file to read"
        }
      },
      "required": ["path"]
    }
  }
}
\`\`\`

---

## 5. Use case scenarios

### 5.1 Agent with filesystem access via MCP

**Scenario:** The user asks the agent to find all Python files in a directory,
read their contents, and generate documentation.

**MCP Server:** \`@modelcontextprotocol/server-filesystem\`

**Flow:**

1. Agent uses \`list_directory\` (MCP) to enumerate files
2. Filters \`.py\` files from the list
3. For each file, uses \`read_file\` (MCP) to read the content
4. Uses \`chat\` (local) to generate documentation with the LLM
5. Uses \`write_file\` (MCP) to save the generated documentation

\`\`\`
Task: "Document all Python files in /home/user/project/src"

Agent:
  1. list_directory("/home/user/project/src")  --> [main.py, utils.py, ...]
  2. read_file("/home/user/project/src/main.py")  --> content
  3. chat("Generate docstrings for: {content}")  --> documentation
  4. write_file("/home/user/project/docs/main.md", documentation)
  ... repeat for each file
\`\`\`

### 5.2 Agent with external database access via MCP

**Scenario:** The user asks the agent to analyze data from a SQLite database and
compare it with the knowledge base.

**MCP Server:** \`@modelcontextprotocol/server-sqlite\`

**Flow:**

1. Agent uses \`read_query\` (MCP SQLite) to execute a SQL query
2. Uses \`document_search\` (local) to search for context in the knowledge base
3. Uses \`chat\` (local) to generate a combined analysis
4. Returns the result to the user

### 5.3 Agent with web scraping capabilities via MCP

**Scenario:** The user asks the agent to gather information from the web and
add it to the knowledge base.

**MCP Server:** \`@anthropic/server-puppeteer\` or \`@modelcontextprotocol/server-fetch\`

**Flow:**

1. Agent uses \`fetch\` (MCP) to download content from a URL
2. Uses \`chat\` (local) to extract relevant information
3. Uses \`document_search\` (local) to check if the information already exists
4. Optionally indexes the new content

---

## 6. Extensibility

### Adding new MCP servers to expand capabilities

The system is designed to be extensible without code changes:

\`\`\`
Step 1: Register the new MCP server via REST API
         POST /api/v1/mcp/servers

Step 2: The server connects automatically
         Status: CONNECTED

Step 3: The new server's tools are immediately available
         GET /api/v1/mcp/tools  --> includes new tools

Step 4: Agents can use the new tools in the next execution
         AgentOrchestrator -> listAllExternalTools() -> new tools included
\`\`\`

### Recommended MCP servers for common scenarios

| Desired capability         | MCP Server                                    | Main tools                         |
|----------------------------|-----------------------------------------------|------------------------------------|
| Filesystem access          | \`@modelcontextprotocol/server-filesystem\`     | read_file, write_file, list_dir    |
| SQLite database            | \`@modelcontextprotocol/server-sqlite\`         | read_query, write_query, list_tables|
| MySQL database             | \`@modelcontextprotocol/server-mysql\`          | query, list_tables, describe_table |
| Web search                 | \`@modelcontextprotocol/server-fetch\`          | fetch                              |
| Browser automation         | \`@anthropic/server-puppeteer\`                 | navigate, screenshot, click        |
| Git operations             | \`@modelcontextprotocol/server-git\`            | log, diff, status, commit          |
| GitHub API                 | \`@modelcontextprotocol/server-github\`         | issues, pull_requests, repos       |
| Slack                      | \`@modelcontextprotocol/server-slack\`          | send_message, list_channels        |
| Google Drive               | \`@modelcontextprotocol/server-gdrive\`         | search, read, create               |
| Persistent memory          | \`@modelcontextprotocol/server-memory\`         | store, retrieve, search            |

### Composition patterns

By combining multiple MCP servers, agents can execute complex workflows:

\`\`\`
Example: "Analyze this week's commits and generate a report"

Git MCP Server       +   LocalMind chat tool   +   Filesystem MCP Server
(git log, git diff)      (analysis with LLM)        (write report.md)
\`\`\`

---

> **Documentation navigation:**
> - Previous: [05-usage-examples.md](/docs/12-mcp-integration/usage-examples)
> - Next: [07-troubleshooting.md](/docs/12-mcp-integration/troubleshooting)
> - Client implementation: [03-client-implementation.md](/docs/12-mcp-integration/client-implementation)
`;
