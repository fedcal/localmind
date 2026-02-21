# Model Context Protocol (MCP) Overview

**Project:** LocalMind - Local-First AI Platform
**Version:** 0.1.0
**Last updated:** 2026-02-09
**Reference module:** localmind-infrastructure (MCP server/client)

---

## Table of Contents

1. [What is the Model Context Protocol](#1-what-is-the-model-context-protocol)
2. [Protocol architecture](#2-protocol-architecture)
3. [Fundamental concepts](#3-fundamental-concepts)
4. [Transport mechanisms](#4-transport-mechanisms)
5. [JSON-RPC communication flow](#5-json-rpc-communication-flow)
6. [Why MCP is important for LocalMind](#6-why-mcp-is-important-for-localmind)
7. [References](#7-references)

---

## 1. What is the Model Context Protocol

The **Model Context Protocol (MCP)** is an open standard, proposed by Anthropic in November 2024,
that defines a universal interface for communication between AI applications and external data
sources or tools. MCP solves the N x M integration problem: without a standard, every AI
application must implement specific connectors for each external service. With MCP, a single
protocol allows any compatible client to communicate with any compatible server.

### Analogy: USB for AI

MCP is to AI applications what USB is to hardware devices. Just as USB eliminated the need
for proprietary cables for each peripheral, MCP eliminates the need for custom integrations
for each context source.

```
  Without MCP:                       With MCP:

  App1 ---custom---> Service A        App1 ---MCP---> Service A
  App1 ---custom---> Service B        App1 ---MCP---> Service B
  App2 ---custom---> Service A        App2 ---MCP---> Service A
  App2 ---custom---> Service B        App2 ---MCP---> Service B
  (4 custom integrations)            (1 protocol, 4 connections)
```

---

## 2. Protocol architecture

MCP follows a **client-server** architecture with well-defined roles:

```
+------------------+          +------------------+
|                  |          |                  |
|    MCP Client    | <------> |    MCP Server    |
|   (Host App)     |  JSON-   |  (Tool/Resource  |
|                  |  RPC     |   Provider)      |
+------------------+          +------------------+
       |                             |
       v                             v
  LLM Model                    Data / Services
  (Ollama, GPT,                (Filesystem, DB,
   Claude, etc.)                API, etc.)
```

### Roles

| Role        | Description                                                      | Example in LocalMind            |
|-------------|------------------------------------------------------------------|---------------------------------|
| **Host**    | Application that hosts the MCP client and the LLM model         | localmind-app (Spring Boot)     |
| **Client**  | Component that manages the connection to an MCP server           | `SpringAiMcpClientAdapter`      |
| **Server**  | Component that exposes tools, resources, and prompts             | `LocalMindMcpTools` et al.      |

### Connection lifecycle

```
Client                              Server
  |                                    |
  |--- initialize (capabilities) ----->|
  |<-- initialize response ------------|
  |--- initialized notification ------>|
  |                                    |
  |--- tools/list -------------------->|
  |<-- tools list response ------------|
  |                                    |
  |--- tools/call (tool, args) ------->|
  |<-- tool result --------------------|
  |                                    |
  |--- shutdown ---------------------->|
  |<-- shutdown ack -------------------|
```

---

## 3. Fundamental concepts

MCP defines three main primitives that a server can expose:

### 3.1 Tool (Tools)

**Tools** are callable functions that the LLM model can decide to call during a
conversation. They represent actions with side effects (search, write, compute).

```json
{
  "name": "document_search",
  "description": "Search documents in the LocalMind knowledge base using RAG",
  "inputSchema": {
    "type": "object",
    "properties": {
      "query": { "type": "string", "description": "The search query text" },
      "topK": { "type": "integer", "description": "Number of top results" }
    },
    "required": ["query"]
  }
}
```

In LocalMind, tools are implemented in `LocalMindMcpTools.java` with the `@Tool` annotation
from Spring AI (see [02-server-implementation.md](02-server-implementation.md)).

### 3.2 Resource (Resources)

**Resources** are data exposed by the server that the client can read. They are identified
by URI and can be static or dynamic.

```
document://{id}        -> Content of an indexed document
config://providers     -> LLM provider configuration
```

### 3.3 Prompt (Templates)

**Prompts** are parameterized templates that the server suggests to the client for common
interactions. They allow standardizing query methods.

```
rag-query              -> Query with precompiled RAG context
summarize-document     -> Automatic document summary
```

### Summary table

| Primitive    | Controlled by | Description                         | API Analogy      |
|-------------|----------------|-------------------------------------|------------------|
| **Tool**    | Model (LLM)   | Callable function                   | POST endpoint    |
| **Resource**| Application    | Readable data                       | GET endpoint     |
| **Prompt**  | User           | Interaction template                | Query template   |

---

## 4. Transport mechanisms

MCP supports three transport mechanisms for communication between client and server:

### 4.1 STDIO (Standard Input/Output)

The server is launched as a child process by the client. Communication occurs
through the process's stdin/stdout.

```
Client (LocalMind)
    |
    +-- spawn process --> Server (e.g. npx @modelcontextprotocol/server-filesystem)
    |       stdin  ------>
    |       stdout <------
```

**Advantages:** Simplicity, no network configuration, process isolation.
**Disadvantages:** Local only, one client per server.

In LocalMind, the `STDIO` type is handled by `McpServerType.STDIO` and requires the
`command` and `args` fields in `McpServerConfig`.

### 4.2 SSE (Server-Sent Events)

The server exposes an HTTP endpoint with SSE for event streaming.
The client connects via HTTP.

```
Client (LocalMind)  ---HTTP POST--->  Server (remote)
                    <---SSE stream--
```

**Advantages:** Remote communication, compatible with HTTP firewalls/proxies.
**Disadvantages:** Unidirectional for SSE (requires separate POST for requests).

In LocalMind, the `SSE` type is handled by `McpServerType.SSE` and requires the `url`
field in `McpServerConfig`.

### 4.3 HTTP Streamable (2025 Specification)

Evolution of SSE introduced in the MCP specification 2025-03-26. Uses standard HTTP
with bidirectional streaming via chunked transfer encoding.

**Advantages:** Bidirectional, optional stateless, better scalability.
**Disadvantages:** More recent specification, SDK support in adoption phase.

> **Note:** LocalMind currently supports STDIO and SSE. Support for HTTP Streamable
> is planned in the roadmap (see [07-troubleshooting.md](07-troubleshooting.md)).

---

## 5. JSON-RPC communication flow

MCP uses **JSON-RPC 2.0** as the message format. Each exchange follows the
request/response pattern with support for unidirectional notifications.

### Example: Tool call

**Request (Client -> Server):**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "document_search",
    "arguments": {
      "query": "architettura esagonale",
      "topK": 5
    }
  }
}
```

**Response (Server -> Client):**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "[{\"documentId\": \"abc-123\", \"content\": \"...\", \"score\": 0.92}]"
      }
    ]
  }
}
```

### Main methods

| Method              | Direction      | Description                              |
|---------------------|----------------|------------------------------------------|
| `initialize`        | Client->Server | Initial capabilities negotiation         |
| `tools/list`        | Client->Server | List of available tools                  |
| `tools/call`        | Client->Server | Invocation of a specific tool            |
| `resources/list`    | Client->Server | List of available resources              |
| `resources/read`    | Client->Server | Reading a specific resource              |
| `prompts/list`      | Client->Server | List of prompt templates                 |
| `prompts/get`       | Client->Server | Retrieval of a prompt with arguments     |
| `notifications/*`   | Bidirectional  | Notifications without expected response  |

---

## 6. Why MCP is important for LocalMind

LocalMind is a local-first AI platform that benefits enormously from MCP integration
for several reasons:

### 6.1 Unlimited extensibility

Thanks to MCP, LocalMind can expand its capabilities by connecting to any compatible
MCP server without modifying the core code. A user can add:

- Local filesystem access (read/write files)
- Connection to external databases (MySQL, PostgreSQL, SQLite, MongoDB)
- Web scraping and web search
- Integration with enterprise APIs
- Development tools (Git, Docker, Kubernetes)

### 6.2 Interoperability

LocalMind can be used as an MCP server by external applications such as Claude Desktop,
making the LocalMind knowledge base and LLM gateway accessible through a standard protocol.

### 6.3 Local-first architecture

MCP with STDIO transport allows LocalMind to launch MCP servers as local processes,
keeping all data on the user's machine. This aligns perfectly with the platform's
local-first philosophy.

### 6.4 Dual role

LocalMind implements **both the server and client MCP roles**:

```
                    +-------------------+
                    |                   |
  Claude Desktop -->|  LocalMind as     |--> Knowledge Base (RAG)
  Other MCP Client->|   MCP SERVER      |--> LLM Gateway
                    |                   |
                    +-------------------+
                    |                   |
                    |  LocalMind as     |--> Filesystem MCP Server
                    |   MCP CLIENT      |--> Database MCP Server
                    |                   |--> Custom MCP Server
                    +-------------------+
```

For implementation details, see:
- [02-server-implementation.md](02-server-implementation.md) - LocalMind as MCP Server
- [03-client-implementation.md](03-client-implementation.md) - LocalMind as MCP Client

---

## 7. References

| Resource                                   | URL / Notes                                        |
|--------------------------------------------|----------------------------------------------------|
| Official MCP specification                 | https://modelcontextprotocol.io                     |
| MCP SDK Java                               | https://github.com/modelcontextprotocol/java-sdk    |
| Spring AI MCP Documentation                | https://docs.spring.io/spring-ai/reference/api/mcp/ |
| Spring AI MCP Server (WebMVC)              | `spring-ai-starter-mcp-server-webmvc`               |
| Spring AI MCP Client                       | `spring-ai-starter-mcp-client`                      |
| JSON-RPC 2.0 specification                 | https://www.jsonrpc.org/specification                |
| MCP Servers Registry                       | https://github.com/modelcontextprotocol/servers      |
| MCP announcement (Anthropic, Nov 2024)     | https://www.anthropic.com/news/model-context-protocol|

---

> **Documentation navigation:**
> - Next: [02-server-implementation.md](02-server-implementation.md)
> - Module index: [README.md](README.md)
