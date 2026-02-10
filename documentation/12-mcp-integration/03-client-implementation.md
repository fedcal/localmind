# MCP Client Implementation in LocalMind

**Project:** LocalMind - Local-First AI Platform
**Version:** 0.1.0
**Last updated:** 2026-02-09
**Reference modules:** localmind-domain (`domain.mcp`), localmind-infrastructure (`infrastructure.mcp`), localmind-api (`api.mcp`)

---

## Table of Contents

1. [Overview](#1-overview)
2. [Hexagonal architecture of the MCP client](#2-hexagonal-architecture-of-the-mcp-client)
3. [Domain model](#3-domain-model)
4. [Inbound ports (Use Case)](#4-inbound-ports-use-case)
5. [Outbound ports (SPI)](#5-outbound-ports-spi)
6. [Domain services](#6-domain-services)
7. [Infrastructure adapter](#7-infrastructure-adapter)
8. [Persistence](#8-persistence)
9. [REST API](#9-rest-api)
10. [Complete flow](#10-complete-flow)

---

## 1. Overview

LocalMind acts as an **MCP Client** to connect to external MCP servers and use their
tools, resources, and prompts. This allows dynamically extending the platform's capabilities
without modifying the core code.

The implementation faithfully follows the project's **hexagonal architecture**, with a clear
separation between domain model, ports (use case and SPI), and adapters (infrastructure and API).

```
+----------------------------------------------------------------+
|                         localmind-api                            |
|  McpServerController   McpToolController                        |
|         |                      |                                |
+---------+----------------------+--------------------------------+
          |                      |
+---------v----------------------v--------------------------------+
|                       localmind-domain                           |
|  McpServerManagementUseCase  McpToolDiscoveryUseCase             |
|  McpToolExecutionUseCase                                         |
|         |                                                        |
|  McpServerManagementService  McpToolOrchestratorService          |
|         |                      |                                 |
|  McpClientPort (SPI)   McpServerRegistrationRepository (SPI)     |
+---------+----------------------+---------------------------------+
          |                      |
+---------v----------------------v---------------------------------+
|                   localmind-infrastructure                        |
|  SpringAiMcpClientAdapter    McpServerRepositoryAdapter          |
|  (ConcurrentHashMap)         (JPA -> McpServerEntity)            |
+-----------------------------------------------------------------+
```

---

## 2. Hexagonal architecture of the MCP client

The MCP client is organized according to the Ports & Adapters pattern:

### Layers and responsibilities

| Layer            | Package                                      | Responsibility                    |
|------------------|----------------------------------------------|-----------------------------------|
| **Domain Model** | `domain.mcp.model`                           | Entities and value objects        |
| **Inbound Ports**| `domain.mcp.port.in`                         | Use cases (interfaces)            |
| **Outbound Ports**| `domain.mcp.port.out`                       | SPI for infrastructure            |
| **Services**     | `domain.mcp.service`                         | Business logic implementation     |
| **Infra Adapter**| `infrastructure.mcp.adapter`                 | MCP SDK adapter (Spring AI)       |
| **Persistence**  | `infrastructure.mcp.persistence`             | JPA entity, repository adapter    |
| **API**          | `api.mcp.controller`, `api.mcp.dto`          | REST controller and DTOs          |

### Dependency principle

Dependency arrows always point inward (toward domain):

```
API --> Domain <-- Infrastructure
```

The domain does not know about Spring, JPA, or the MCP SDK. It only defines interfaces (ports)
that adapters implement.

---

## 3. Domain model

The MCP domain model is located in `com.localmind.domain.mcp.model` and includes:

### 3.1 `McpServerRegistration`

Represents the registration of an external MCP server in the system.

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpServerRegistration {
    private String id;                    // System-generated UUID
    private String name;                  // Human-readable server name
    private String description;           // Optional description
    private McpServerType type;           // STDIO or SSE
    private McpServerConfig config;       // Connection configuration
    private McpServerStatus status;       // Current connection status
    private LocalDateTime createdAt;      // Registration timestamp
    private LocalDateTime lastConnectedAt;// Last successful connection
}
```

### 3.2 `McpServerConfig`

Value object with server connection parameters.

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpServerConfig {
    private String command;               // Command for STDIO (e.g. "npx")
    private List<String> args;            // STDIO command arguments
    private String url;                   // URL for SSE (e.g. "http://localhost:3001/sse")
    @Builder.Default
    private int timeoutSeconds = 30;      // Connection timeout
    @Builder.Default
    private boolean autoReconnect = true; // Automatic reconnection
}
```

**Domain invariants:**
- If `type == STDIO`, `command` must be non-null (validated by DB constraint `chk_stdio_command`)
- If `type == SSE`, `url` must be non-null (validated by DB constraint `chk_sse_url`)

### 3.3 `McpServerType`

Enum defining supported transport types.

```java
public enum McpServerType {
    STDIO("Standard Input/Output"),   // Local process via stdin/stdout
    SSE("Server-Sent Events");        // Remote HTTP connection
    // ...
}
```

### 3.4 `McpServerStatus`

Enum representing the connection lifecycle state.

```java
public enum McpServerStatus {
    CONNECTED,      // Active and functional connection
    DISCONNECTED,   // Not connected (initial state or after disconnect)
    ERROR,          // Connection error
    CONNECTING      // Connection in progress (transient state)
}
```

**State diagram:**

```
                 register()
  [new] ------> DISCONNECTED
                    |
             connect() ok         connect() fail
                    |                  |
                    v                  v
               CONNECTED            ERROR
                    |                  |
            disconnect()          reconnect()
                    |                  |
                    v                  v
              DISCONNECTED        CONNECTING
                                      |
                              connect() ok / fail
                                      |
                                 CONNECTED / ERROR
```

### 3.5 `McpExternalTool`

Represents a tool discovered from an external MCP server.

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpExternalTool {
    private String name;           // Unique tool name
    private String description;    // Description for the LLM model
    private String inputSchema;    // JSON Schema of input parameters
    private String serverId;       // ID of the server exposing the tool
}
```

### 3.6 `McpToolExecutionRequest`

Request for executing an external tool.

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpToolExecutionRequest {
    private String toolName;               // Name of the tool to execute
    private String serverId;               // Server on which to execute the tool
    private Map<String, Object> arguments; // Arguments to pass to the tool
}
```

### 3.7 `McpToolExecutionResult`

Tool execution result with diagnostic information.

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpToolExecutionResult {
    private String toolName;        // Name of the executed tool
    private Object result;          // Result (deserialized from JSON-RPC)
    private boolean success;        // Execution outcome
    private String errorMessage;    // Error message (if success=false)
    private long executionTimeMs;   // Execution time in milliseconds
}
```

---

## 4. Inbound ports (Use Case)

Inbound ports define operations available to upper layers (API, agents).
They are located in `com.localmind.domain.mcp.port.in`.

### 4.1 `McpServerManagementUseCase`

Management of the lifecycle of registered MCP servers.

```java
public interface McpServerManagementUseCase {
    McpServerRegistration register(McpServerRegistration server);   // Register and connect
    void remove(String serverId);                                   // Remove and disconnect
    McpServerRegistration get(String serverId);                     // Get by ID
    List<McpServerRegistration> listAll();                          // List all
    McpServerRegistration testConnection(String serverId);          // Test connection
    void reconnect(String serverId);                                // Reconnect
}
```

### 4.2 `McpToolDiscoveryUseCase`

Discovery of available tools on connected servers.

```java
public interface McpToolDiscoveryUseCase {
    List<McpExternalTool> listExternalTools(String serverId);   // Tools from a server
    List<McpExternalTool> listAllExternalTools();               // Tools from all servers
}
```

### 4.3 `McpToolExecutionUseCase`

Execution of tools on external servers.

```java
public interface McpToolExecutionUseCase {
    McpToolExecutionResult executeTool(McpToolExecutionRequest request);
}
```

---

## 5. Outbound ports (SPI)

Outbound ports are interfaces implemented by the infrastructure layer.
They are located in `com.localmind.domain.mcp.port.out`.

### 5.1 `McpClientPort`

SPI for communication with external MCP servers. Abstracts the underlying MCP SDK.

```java
public interface McpClientPort {
    boolean connect(McpServerRegistration server);                        // Connect
    void disconnect(String serverId);                                     // Disconnect
    boolean testConnection(String serverId);                              // Verify
    List<McpExternalTool> discoverTools(String serverId);                 // Discover tools
    McpToolExecutionResult executeTool(String serverId,
                                       McpToolExecutionRequest request);  // Execute tool
}
```

### 5.2 `McpServerRegistrationRepository`

SPI for persisting server registrations.

```java
public interface McpServerRegistrationRepository {
    McpServerRegistration save(McpServerRegistration server);
    void delete(String serverId);
    Optional<McpServerRegistration> findById(String serverId);
    List<McpServerRegistration> findAll();
}
```

---

## 6. Domain services

Domain services implement business logic and are pure POJOs (no framework dependencies).
They are instantiated as beans via `McpConfiguration`.

### 6.1 `McpServerManagementService`

Implements `McpServerManagementUseCase`. Manages the complete server lifecycle.

```java
public class McpServerManagementService implements McpServerManagementUseCase {

    private final McpServerRegistrationRepository repository;
    private final McpClientPort clientPort;

    // Constructor injection (no @Autowired, pure POJO)

    @Override
    public McpServerRegistration register(McpServerRegistration server) {
        if (server.getId() == null) {
            server.setId(UUID.randomUUID().toString());
        }
        server.setCreatedAt(LocalDateTime.now());
        server.setStatus(McpServerStatus.DISCONNECTED);

        McpServerRegistration saved = repository.save(server);

        // Automatic connection attempt upon registration
        boolean connected = clientPort.connect(saved);
        if (connected) {
            saved.setStatus(McpServerStatus.CONNECTED);
            saved.setLastConnectedAt(LocalDateTime.now());
            return repository.save(saved);
        }
        return saved;
    }

    @Override
    public void reconnect(String serverId) {
        McpServerRegistration server = get(serverId);
        clientPort.disconnect(serverId);
        server.setStatus(McpServerStatus.CONNECTING);
        repository.save(server);

        boolean connected = clientPort.connect(server);
        server.setStatus(connected ? McpServerStatus.CONNECTED : McpServerStatus.ERROR);
        if (connected) {
            server.setLastConnectedAt(LocalDateTime.now());
        }
        repository.save(server);
    }
    // ... (remove, get, listAll, testConnection)
}
```

**Key behavior:**
- `register()`: Generates UUID, persists, then attempts automatic connection
- `remove()`: Disconnects before deleting from DB
- `testConnection()`: Verifies connection and updates status
- `reconnect()`: Complete cycle disconnect -> CONNECTING -> connect -> CONNECTED/ERROR

### 6.2 `McpToolOrchestratorService`

Implements `McpToolDiscoveryUseCase` and `McpToolExecutionUseCase`. Tool orchestration.

```java
public class McpToolOrchestratorService implements McpToolDiscoveryUseCase,
                                                    McpToolExecutionUseCase {

    private final McpServerRegistrationRepository serverRepository;
    private final McpClientPort clientPort;

    @Override
    public List<McpExternalTool> listAllExternalTools() {
        List<McpExternalTool> allTools = new ArrayList<>();
        serverRepository.findAll().stream()
                .filter(server -> server.getStatus() == McpServerStatus.CONNECTED)
                .forEach(server -> allTools.addAll(
                    clientPort.discoverTools(server.getId())));
        return allTools;
    }

    @Override
    public McpToolExecutionResult executeTool(McpToolExecutionRequest request) {
        return clientPort.executeTool(request.getServerId(), request);
    }
}
```

**Key behavior:**
- `listAllExternalTools()`: Iterates over all CONNECTED servers and aggregates tools
- `listExternalTools(serverId)`: Delegates directly to `McpClientPort`
- `executeTool()`: Delegates to `McpClientPort` for the specified server

---

## 7. Infrastructure adapter

### 7.1 `SpringAiMcpClientAdapter`

Implements `McpClientPort` and manages MCP connections using the Spring AI MCP Client SDK.
Located in `com.localmind.infrastructure.mcp.adapter`.

```java
@Component
@ConditionalOnProperty(name = "localmind.mcp.client.enabled", havingValue = "true")
public class SpringAiMcpClientAdapter implements McpClientPort {

    private static final Logger log = LoggerFactory.getLogger(SpringAiMcpClientAdapter.class);
    private final Map<String, McpClientConnection> connections = new ConcurrentHashMap<>();

    @Override
    public boolean connect(McpServerRegistration server) {
        try {
            log.info("Connecting to MCP server: {} ({})", server.getName(), server.getType());
            McpClientConnection connection = new McpClientConnection(server);
            connections.put(server.getId(), connection);
            log.info("Connected to MCP server: {}", server.getName());
            return true;
        } catch (Exception e) {
            log.error("Failed to connect to MCP server: {}", server.getName(), e);
            return false;
        }
    }

    @Override
    public McpToolExecutionResult executeTool(String serverId,
                                               McpToolExecutionRequest request) {
        McpClientConnection connection = connections.get(serverId);
        if (connection == null) {
            return McpToolExecutionResult.builder()
                    .toolName(request.getToolName())
                    .success(false)
                    .errorMessage("MCP server not connected: " + serverId)
                    .build();
        }

        long start = System.currentTimeMillis();
        try {
            Object result = connection.executeTool(
                request.getToolName(), request.getArguments());
            return McpToolExecutionResult.builder()
                    .toolName(request.getToolName())
                    .result(result)
                    .success(true)
                    .executionTimeMs(System.currentTimeMillis() - start)
                    .build();
        } catch (Exception e) {
            return McpToolExecutionResult.builder()
                    .toolName(request.getToolName())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .executionTimeMs(System.currentTimeMillis() - start)
                    .build();
        }
    }
    // ...
}
```

**Key characteristics:**
- `ConcurrentHashMap<String, McpClientConnection>` for thread-safe connection management
- Internal class `McpClientConnection` as a wrapper for single connection lifecycle
- Conditional activation via `@ConditionalOnProperty`
- Structured logging for connection diagnostics

### In-memory connection management

```
ConcurrentHashMap<serverId, McpClientConnection>
   |
   +-- "abc-123" -> McpClientConnection(filesystem-server, alive=true)
   +-- "def-456" -> McpClientConnection(database-server, alive=true)
   +-- "ghi-789" -> McpClientConnection(web-scraper, alive=false)
```

---

## 8. Persistence

### 8.1 `McpServerEntity`

JPA entity mapped to the `mcp_servers` table (created by Flyway V6).

```java
@Entity
@Table(name = "mcp_servers")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpServerEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 50)
    private String type;              // "STDIO" or "SSE"

    @Column(nullable = false, length = 50)
    private String status;            // "CONNECTED", "DISCONNECTED", etc.

    @Column(length = 500)
    private String command;           // For STDIO

    @Column(columnDefinition = "TEXT")
    private String args;              // For STDIO (comma-separated)

    @Column(length = 500)
    private String url;               // For SSE

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    @Column(name = "auto_reconnect")
    private Boolean autoReconnect;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_connected_at")
    private LocalDateTime lastConnectedAt;
}
```

### 8.2 `McpServerJpaRepository`

Spring Data JPA repository with derived query.

```java
@Repository
public interface McpServerJpaRepository extends JpaRepository<McpServerEntity, String> {
    List<McpServerEntity> findByStatus(String status);
}
```

### 8.3 `McpServerRepositoryAdapter`

Adapter that implements the domain port `McpServerRegistrationRepository`, translating
between domain model and JPA entity.

```java
@Component
public class McpServerRepositoryAdapter implements McpServerRegistrationRepository {

    private final McpServerJpaRepository jpaRepository;

    // Bidirectional mapping: domain <-> entity
    private McpServerEntity toEntity(McpServerRegistration domain) { /* ... */ }
    private McpServerRegistration toDomain(McpServerEntity entity) { /* ... */ }
}
```

**Note on args mapping:** STDIO command arguments are stored as a comma-separated
string in the DB (`args TEXT`) and converted to `List<String>` in the domain model
via `String.join(",", args)` / `args.split(",")`.

---

## 9. REST API

The REST API for MCP is located in the `localmind-api` module under `com.localmind.api.mcp`.

### 9.1 `McpServerController`

Endpoints for managing registered MCP servers.

| Method | Endpoint                              | Description                 |
|--------|---------------------------------------|-----------------------------|
| POST   | `/api/v1/mcp/servers`                 | Register a new server       |
| GET    | `/api/v1/mcp/servers`                 | List all servers            |
| GET    | `/api/v1/mcp/servers/{serverId}`      | Get server details          |
| DELETE | `/api/v1/mcp/servers/{serverId}`      | Remove a server             |
| POST   | `/api/v1/mcp/servers/{serverId}/test` | Test the connection         |
| POST   | `/api/v1/mcp/servers/{serverId}/reconnect` | Reconnect to server    |

**Request DTO (`CreateMcpServerRequestDto`):**

```java
public class CreateMcpServerRequestDto {
    @NotBlank private String name;          // Required
    private String description;             // Optional
    @NotNull private String type;           // "STDIO" or "SSE"
    private String command;                 // For STDIO
    private List<String> args;              // For STDIO
    private String url;                     // For SSE
    private Integer timeoutSeconds;         // Default: 30
    private Boolean autoReconnect;          // Default: true
}
```

**Response DTO (`McpServerDto`):**

```java
public class McpServerDto {
    private String id;
    private String name;
    private String description;
    private String type;
    private String status;
    private McpServerConfigDto config;
    private LocalDateTime createdAt;
    private LocalDateTime lastConnectedAt;
}
```

### 9.2 `McpToolController`

Endpoints for tool discovery and execution.

| Method | Endpoint                                | Description                     |
|--------|-----------------------------------------|---------------------------------|
| GET    | `/api/v1/mcp/tools`                     | All external tools              |
| GET    | `/api/v1/mcp/tools/servers/{serverId}`  | Tools from a specific server    |
| GET    | `/api/v1/mcp/tools/local`               | LocalMind local tools           |
| POST   | `/api/v1/mcp/tools/execute`             | Execute a tool                  |

**Execution DTO (`McpToolExecutionRequestDto`):**

```java
public class McpToolExecutionRequestDto {
    @NotBlank private String toolName;      // Tool name
    @NotBlank private String serverId;      // Server ID
    private Map<String, Object> arguments;  // Tool arguments
}
```

---

## 10. Complete flow

### Registration and connection to a server

```
1. HTTP Client --> POST /api/v1/mcp/servers
        |
2. McpServerController.register(CreateMcpServerRequestDto)
        |
3. McpServerManagementService.register(McpServerRegistration)
        |
4. McpServerRegistrationRepository.save()  --> DB (mcp_servers)
        |
5. McpClientPort.connect(server)  --> SpringAiMcpClientAdapter
        |                                  |
        |                           ConcurrentHashMap.put(id, connection)
        |
6. If connected: update status=CONNECTED, lastConnectedAt
        |
7. McpServerRegistrationRepository.save()  --> Updated DB
        |
8. Response --> McpServerDto (with updated status)
```

### Tool discovery and execution

```
1. HTTP Client --> GET /api/v1/mcp/tools/servers/{serverId}
        |
2. McpToolController --> McpToolDiscoveryUseCase.listExternalTools(serverId)
        |
3. McpToolOrchestratorService --> McpClientPort.discoverTools(serverId)
        |
4. SpringAiMcpClientAdapter --> connection.getTools()
        |                        (JSON-RPC tools/list)
5. McpExternalTool list <-- response
        |
6. HTTP Client --> POST /api/v1/mcp/tools/execute
        |
7. McpToolController --> McpToolExecutionUseCase.executeTool(request)
        |
8. McpToolOrchestratorService --> McpClientPort.executeTool(serverId, request)
        |
9. SpringAiMcpClientAdapter --> connection.executeTool(name, args)
        |                        (JSON-RPC tools/call)
10. McpToolExecutionResult <-- response with result, success, executionTimeMs
```

---

> **Documentation navigation:**
> - Previous: [02-server-implementation.md](02-server-implementation.md)
> - Next: [04-configuration.md](04-configuration.md)
> - Agent integration: [06-agent-integration.md](06-agent-integration.md)
