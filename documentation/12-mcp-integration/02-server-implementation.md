# MCP Server Implementation in LocalMind

**Project:** LocalMind - Local-First AI Platform
**Version:** 0.1.0
**Last updated:** 2026-02-09
**Reference module:** localmind-infrastructure (`infrastructure.mcp.server`)

---

## Table of Contents

1. [Overview](#1-overview)
2. [MCP server architecture](#2-mcp-server-architecture)
3. [Exposed tools](#3-exposed-tools)
4. [Exposed resources](#4-exposed-resources)
5. [Prompt templates](#5-prompt-templates)
6. [Implementation with Spring AI](#6-implementation-with-spring-ai)
7. [Delegation to domain use cases](#7-delegation-to-domain-use-cases)
8. [Source file map](#8-source-file-map)

---

## 1. Overview

LocalMind acts as an **MCP Server**, exposing its RAG knowledge base and multi-provider
LLM gateway through the MCP protocol. This allows any compatible MCP client (Claude Desktop,
other AI agents, IDEs with MCP support) to use LocalMind's capabilities as external tools.

```
+-----------------------------------------------------+
|                   LocalMind Backend                  |
|                                                      |
|  +--------------------+    +-----------------------+ |
|  | LocalMindMcpTools  |--->| DocumentSearchUseCase | |
|  |   @Tool methods    |--->| ChatUseCase           | |
|  +--------------------+    +-----------------------+ |
|  +--------------------+         Domain Layer         |
|  | LocalMindMcpRes.   |                             |
|  +--------------------+                             |
|  +--------------------+                             |
|  | LocalMindMcpPrompts|                             |
|  +--------------------+                             |
|         |                                            |
|         v                                            |
|  Spring AI MCP Server WebMVC                         |
|  (spring-ai-starter-mcp-server-webmvc)               |
+-----------------------------------------------------+
         |
         | HTTP/SSE (JSON-RPC 2.0)
         v
  External MCP Client (Claude Desktop, IDE, etc.)
```

The MCP server is enabled by the property `localmind.mcp.server.enabled=true` (default: `true`)
and is configured in `application-dev.yml`:

```yaml
spring:
  ai:
    mcp:
      server:
        name: localmind
        version: 0.1.0
```

---

## 2. MCP server architecture

The LocalMind MCP server is composed of three main classes in the package
`com.localmind.infrastructure.mcp.server`:

| Class                    | Responsibility                                | MCP Primitive |
|--------------------------|-----------------------------------------------|---------------|
| `LocalMindMcpTools`      | Exposes callable tools (search, chat, models)  | Tool          |
| `LocalMindMcpResources`  | Exposes readable resources (documents, config)  | Resource      |
| `LocalMindMcpPrompts`    | Provides parameterized prompt templates         | Prompt        |

All classes are annotated with:
- `@Component` - automatic registration in the Spring context
- `@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)`

The `matchIfMissing = true` flag ensures the MCP server is active by default.

---

## 3. Exposed tools

LocalMind exposes three MCP tools through `LocalMindMcpTools.java`:

### 3.1 `document_search(query, topK)`

**Description:** Search documents in the LocalMind knowledge base using RAG (Retrieval-Augmented
Generation). Returns relevant document chunks with similarity scores.

**Parameters:**

| Parameter | Type    | Required | Default | Description                       |
|-----------|---------|----------|---------|-----------------------------------|
| `query`   | String  | Yes      | -       | Search query text                 |
| `topK`    | int     | No       | 5       | Maximum number of results         |

**Response:** List of objects with `documentId`, `filename`, `content`, `score`, `chunkIndex`.

**Implementation:**

```java
@Tool(description = "Search documents in the LocalMind knowledge base using RAG " +
      "(Retrieval-Augmented Generation). Returns relevant document chunks with " +
      "similarity scores.")
public List<Map<String, Object>> documentSearch(
        @ToolParam(description = "The search query text") String query,
        @ToolParam(description = "Number of top results to return (default 5)") int topK) {
    if (topK <= 0) topK = 5;
    List<SearchResult> results = documentSearchUseCase.search(query, topK);
    return results.stream()
            .map(r -> {
                Map<String, Object> map = new HashMap<>();
                map.put("documentId", r.getDocumentId());
                map.put("filename", r.getFilename());
                map.put("content", r.getContent());
                map.put("score", r.getScore());
                map.put("chunkIndex", r.getChunkIndex());
                return map;
            })
            .collect(Collectors.toList());
}
```

**Internal flow:**
```
MCP Client --> tools/call "document_search" --> LocalMindMcpTools.documentSearch()
    --> DocumentSearchUseCase.search(query, topK)
    --> Qdrant Vector Store (similarity search)
    --> SearchResult list --> Conversion to Map --> JSON-RPC response
```

### 3.2 `chat(message, provider, model, temperature)`

**Description:** Send a message to an LLM through LocalMind's multi-provider gateway.
Supports Ollama, OpenAI, Anthropic, Google.

**Parameters:**

| Parameter     | Type   | Required | Default  | Description                          |
|---------------|--------|----------|----------|--------------------------------------|
| `message`     | String | Yes      | -        | Message to send to the LLM           |
| `provider`    | String | No       | OLLAMA   | Provider: OLLAMA, OPENAI, ANTHROPIC, GOOGLE |
| `model`       | String | No       | default  | Specific model name                  |
| `temperature` | Double | No       | 0.7      | Generation temperature (0-1)         |

**Response:** Object with `content`, `model`, `provider`, `latencyMs`.

**Implementation:**

```java
@Tool(description = "Send a message to an LLM through LocalMind's multi-provider gateway. " +
      "Supports Ollama, OpenAI, Anthropic, Google.")
public Map<String, Object> chat(
        @ToolParam(description = "The message to send to the LLM") String message,
        @ToolParam(description = "LLM provider: OLLAMA, OPENAI, ANTHROPIC, GOOGLE") String provider,
        @ToolParam(description = "Specific model name (optional)") String model,
        @ToolParam(description = "Temperature for generation, 0.0-1.0") Double temperature) {

    LlmRequest.LlmRequestBuilder requestBuilder = LlmRequest.builder()
            .messages(List.of(ChatMessage.builder()
                    .role(ChatMessage.Role.USER)
                    .content(message)
                    .build()));

    if (provider != null && !provider.isBlank()) {
        requestBuilder.provider(LlmProvider.valueOf(provider.toUpperCase()));
    }
    if (model != null && !model.isBlank()) {
        requestBuilder.model(model);
    }
    if (temperature != null) {
        requestBuilder.temperature(temperature);
    }

    LlmResponse response = chatUseCase.chat(requestBuilder.build());

    Map<String, Object> result = new HashMap<>();
    result.put("content", response.getContent());
    result.put("model", response.getModel());
    result.put("provider", response.getProvider().name());
    result.put("latencyMs", response.getLatencyMs());
    return result;
}
```

**Internal flow:**
```
MCP Client --> tools/call "chat" --> LocalMindMcpTools.chat()
    --> ChatUseCase.chat(LlmRequest)
    --> Selected provider (Ollama/OpenAI/Anthropic/Google)
    --> LlmResponse --> Conversion to Map --> JSON-RPC response
```

### 3.3 `list_models()`

**Description:** List available LLM providers and their status in LocalMind.

**Parameters:** None.

**Response:** Object with `providers` (list) and `defaultProvider`.

```java
@Tool(description = "List available LLM providers and their status in LocalMind")
public Map<String, Object> listModels() {
    Map<String, Object> result = new HashMap<>();
    result.put("providers", List.of("OLLAMA", "OPENAI", "ANTHROPIC", "GOOGLE"));
    result.put("defaultProvider", "OLLAMA");
    return result;
}
```

### Tool summary

| Tool               | JSON-RPC Method       | Domain Use Case                | Effect                     |
|--------------------|-----------------------|--------------------------------|----------------------------|
| `document_search`  | `tools/call`          | `DocumentSearchUseCase`        | RAG search on Qdrant       |
| `chat`             | `tools/call`          | `ChatUseCase`                  | Multi-provider LLM call    |
| `list_models`      | `tools/call`          | (static)                       | Provider listing           |

---

## 4. Exposed resources

Resources are implemented in `LocalMindMcpResources.java`. Currently, resources are
registered programmatically via `McpConfiguration` since support for `@McpResource`
annotations depends on the Spring AI version.

### 4.1 `config://providers`

**URI:** `config://providers`
**MIME Type:** `application/json`
**Description:** Returns the complete configuration of available LLM providers.

```json
{
  "providers": [
    {"name": "OLLAMA", "local": true, "defaultModel": "llama3.2"},
    {"name": "OPENAI", "local": false, "defaultModel": "gpt-4o"},
    {"name": "ANTHROPIC", "local": false, "defaultModel": "claude-sonnet-4-20250514"},
    {"name": "GOOGLE", "local": false, "defaultModel": "gemini-pro"}
  ],
  "defaultProvider": "OLLAMA"
}
```

**Implementation:**

```java
@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true",
                       matchIfMissing = true)
public class LocalMindMcpResources {

    public String getProviderConfig() {
        return """
                {
                  "providers": [
                    {"name": "OLLAMA", "local": true, "defaultModel": "llama3.2"},
                    {"name": "OPENAI", "local": false, "defaultModel": "gpt-4o"},
                    {"name": "ANTHROPIC", "local": false, "defaultModel": "claude-sonnet-4-20250514"},
                    {"name": "GOOGLE", "local": false, "defaultModel": "gemini-pro"}
                  ],
                  "defaultProvider": "OLLAMA"
                }
                """;
    }
}
```

### 4.2 `document://{id}` (planned)

**URI:** `document://{id}`
**MIME Type:** `text/plain`
**Description:** Returns the complete content of an indexed document, identified
by its UUID. This resource will be implemented when document retrieval by ID is
available in the domain layer.

---

## 5. Prompt templates

Prompt templates are implemented in `LocalMindMcpPrompts.java` and provide predefined
patterns for common interactions with the knowledge base.

### 5.1 `rag-query`

**Description:** Query with precompiled RAG context. Combines semantic search results
with the user's query in a structured prompt.

**Parameters:**
- `query` (String) - The user's question
- `context` (String) - The context extracted from the knowledge base

**Template:**

```java
public String getRagQueryPrompt(String query, String context) {
    return String.format("""
            You are a helpful AI assistant with access to a knowledge base.

            Context from knowledge base:
            %s

            User query: %s

            Please provide a comprehensive answer based on the context provided.
            If the context doesn't contain relevant information, say so clearly.
            """, context, query);
}
```

### 5.2 `summarize-document`

**Description:** Generates a structured summary of a document.

**Parameters:**
- `content` (String) - The content of the document to summarize

**Template:**

```java
public String getSummarizeDocumentPrompt(String content) {
    return String.format("""
            Please provide a concise summary of the following document:

            %s

            The summary should be 2-3 paragraphs highlighting the main points,
            key findings, and actionable insights.
            """, content);
}
```

### Prompt summary

| Prompt                | Parameters         | Use case                                |
|-----------------------|--------------------|-----------------------------------------|
| `rag-query`           | query, context     | Q&A with context from the knowledge base |
| `summarize-document`  | content            | Automatic document summary               |

---

## 6. Implementation with Spring AI

### 6.1 `@Tool` annotation

Spring AI 1.0.0 provides the `@Tool` annotation to declare methods as MCP tools.
The annotation automatically generates the JSON schema for parameters and registers
the tool in the MCP server.

```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

@Tool(description = "Tool description for the LLM model")
public ReturnType methodName(
    @ToolParam(description = "Parameter description") ParamType param) {
    // implementation
}
```

### 6.2 Automatic registration

The `spring-ai-starter-mcp-server-webmvc` starter automatically scans all beans
containing `@Tool` annotated methods and registers them as MCP tools. The configuration
in `application-dev.yml` sets the server name and version:

```yaml
spring:
  ai:
    mcp:
      server:
        name: localmind        # MCP server name
        version: 0.1.0         # Server version
```

### 6.3 Exposed endpoints

With the WebMVC starter, the MCP server automatically exposes:

| Endpoint          | Method | Description                           |
|-------------------|--------|---------------------------------------|
| `/mcp/sse`        | GET    | SSE stream for server->client notifications |
| `/mcp/message`    | POST   | JSON-RPC messages client->server      |

These endpoints are separate from the LocalMind REST APIs (`/api/v1/*`).

---

## 7. Delegation to domain use cases

A key principle of LocalMind's hexagonal architecture is that MCP server components
(in the `infrastructure` module) delegate business logic to **domain use cases**:

```
+------------------------------+       +-----------------------------+
|   Infrastructure Layer       |       |     Domain Layer            |
|                              |       |                             |
| LocalMindMcpTools            |       | DocumentSearchUseCase       |
|   .documentSearch() ---------|------>|   .search(query, topK)      |
|   .chat() -------------------|------>| ChatUseCase                 |
|                              |       |   .chat(LlmRequest)         |
+------------------------------+       +-----------------------------+
```

### Injected dependencies

```java
public class LocalMindMcpTools {

    private final DocumentSearchUseCase documentSearchUseCase;
    private final ChatUseCase chatUseCase;

    public LocalMindMcpTools(DocumentSearchUseCase documentSearchUseCase,
                             ChatUseCase chatUseCase) {
        this.documentSearchUseCase = documentSearchUseCase;
        this.chatUseCase = chatUseCase;
    }
    // ...
}
```

This ensures that:
1. Business logic remains in the domain, not in infrastructure
2. MCP tools are a simple facade (adapter) to the domain
3. Testability is guaranteed by injecting use case mocks
4. Adding new tools is simple: just add a `@Tool` method that delegates

---

## 8. Source file map

```
localmind-infrastructure/
  src/main/java/com/localmind/infrastructure/mcp/
    server/
      LocalMindMcpTools.java         # @Tool: document_search, chat, list_models
      LocalMindMcpResources.java     # Resources: config://providers
      LocalMindMcpPrompts.java       # Prompts: rag-query, summarize-document
    config/
      McpConfiguration.java          # Bean definitions for MCP client/server
```

**Maven dependencies (localmind-infrastructure/pom.xml):**

```xml
<!-- MCP Server (WebMVC) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

---

> **Documentation navigation:**
> - Previous: [01-mcp-protocol-overview.md](01-mcp-protocol-overview.md)
> - Next: [03-client-implementation.md](03-client-implementation.md)
> - Configuration: [04-configuration.md](04-configuration.md)
