export const content = `# MCP Client Implementation in LocalMind

**Project:** LocalMind - Local-First AI Platform
**Version:** 0.1.0
**Last updated:** 2026-02-13
**Reference modules:** localmind-domain (\`domain.mcp\`), localmind-infrastructure (\`infrastructure.mcp\`), localmind-api (\`api.mcp\`)

---

## Table of Contents

1. [Overview](#1-overview)
2. [Hexagonal architecture of the MCP client](#2-hexagonal-architecture-of-the-mcp-client)
3. [Domain model](#3-domain-model)
4. [Inbound ports (Use Case)](#4-inbound-ports-use-case)
5. [Outbound ports (SPI)](#5-outbound-ports-spi)
6. [Domain services](#6-domain-services)
7. [Infrastructure adapter](#7-infrastructure-adapter)
   - 7.1 [SpringAiMcpClientAdapter (Real MCP SDK)](#71-springaimcpclientadapter-real-mcp-sdk)
   - 7.2 [LocalToolDiscoveryService](#72-localtool-discoveryservice)
8. [Persistence](#8-persistence)
9. [REST API](#9-rest-api)
   - 9.1 [McpServerController](#91-mcpservercontroller)
   - 9.2 [McpToolController](#92-mcptoolcontroller)
   - 9.3 [McpScrumController](#93-mcpscrumcontroller)
   - 9.4 [McpIncidentController](#94-mcpincidentcontroller)
   - 9.5 [McpTimeController](#95-mcptimecontroller)
10. [Complete flow](#10-complete-flow)
11. [Security -- McpAccessInterceptor](#11-security----mcpaccessinterceptor)

---

## 1. Overview

LocalMind acts as an **MCP Client** to connect to external MCP servers and use their
tools, resources, and prompts. This allows dynamically extending the platform's capabilities
without modifying the core code.

The implementation faithfully follows the project's **hexagonal architecture**, with a clear
separation between domain model, ports (use case and SPI), and adapters (infrastructure and API).

The client now uses the **real MCP SDK** (\`McpSyncClient\`) with support for both SSE and STDIO
transports, a **reflection-based local tool discovery** system that dynamically finds all 135+
local \`@Tool\` methods, **three domain-specific REST controllers** (Scrum, Incident, Time), and
an **access policy interceptor** that enforces tool-level authorization.

\`\`\`
+----------------------------------------------------------------+
|                         localmind-api                            |
|  McpServerController   McpToolController                        |
|  McpScrumController    McpIncidentController  McpTimeController |
|         |                      |                                |
+---------+----------------------+--------------------------------+
          |                      |
+---------v----------------------v--------------------------------+
|                       localmind-domain                           |
|  McpServerManagementUseCase  McpToolDiscoveryUseCase             |
|  McpToolExecutionUseCase     ScrumBoardUseCase                   |
|  IncidentManagerUseCase      TimeTrackingUseCase                 |
|  AccessPolicyUseCase                                             |
|         |                                                        |
|  McpServerManagementService  McpToolOrchestratorService          |
|         |                      |                                 |
|  McpClientPort (SPI)   McpServerRegistrationRepository (SPI)     |
|  LocalToolDiscoveryPort (SPI)                                    |
+---------+----------------------+---------------------------------+
          |                      |
+---------v----------------------v---------------------------------+
|                   localmind-infrastructure                        |
|  SpringAiMcpClientAdapter    McpServerRepositoryAdapter          |
|  (McpSyncClient + SSE/STDIO) (JPA -> McpServerEntity)           |
|  LocalToolDiscoveryService   McpAccessInterceptor                |
|  (Reflection @Tool scanner)  (HandlerInterceptor + policy check) |
+-----------------------------------------------------------------+
\`\`\`

---

## 2. Hexagonal architecture of the MCP client

The MCP client is organized according to the Ports & Adapters pattern:

### Layers and responsibilities

| Layer            | Package                                      | Responsibility                    |
|------------------|----------------------------------------------|-----------------------------------|
| **Domain Model** | \`domain.mcp.model\`                           | Entities and value objects        |
| **Inbound Ports**| \`domain.mcp.port.in\`                         | Use cases (interfaces)            |
| **Outbound Ports**| \`domain.mcp.port.out\`                       | SPI for infrastructure            |
| **Services**     | \`domain.mcp.service\`                         | Business logic implementation     |
| **Infra Adapter**| \`infrastructure.mcp.adapter\`                 | MCP SDK adapter (Spring AI)       |
| **Discovery**    | \`infrastructure.mcp.discovery\`               | Local @Tool reflection scanner    |
| **Security**     | \`infrastructure.mcp.security\`                | Access policy interceptor         |
| **Persistence**  | \`infrastructure.mcp.persistence\`             | JPA entity, repository adapter    |
| **API**          | \`api.mcp.controller\`, \`api.mcp.dto\`          | REST controller and DTOs          |

### Dependency principle

Dependency arrows always point inward (toward domain):

\`\`\`
API --> Domain <-- Infrastructure
\`\`\`

The domain does not know about Spring, JPA, or the MCP SDK. It only defines interfaces (ports)
that adapters implement.

---

## 3. Domain model

The MCP domain model is located in \`com.localmind.domain.mcp.model\` and includes:

### 3.1 \`McpServerRegistration\`

Represents the registration of an external MCP server in the system.

\`\`\`java
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
\`\`\`

### 3.2 \`McpServerConfig\`

Value object with server connection parameters.

\`\`\`java
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
\`\`\`

**Domain invariants:**
- If \`type == STDIO\`, \`command\` must be non-null (validated by DB constraint \`chk_stdio_command\`)
- If \`type == SSE\`, \`url\` must be non-null (validated by DB constraint \`chk_sse_url\`)

### 3.3 \`McpServerType\`

Enum defining supported transport types.

\`\`\`java
public enum McpServerType {
    STDIO("Standard Input/Output"),   // Local process via stdin/stdout
    SSE("Server-Sent Events");        // Remote HTTP connection
    // ...
}
\`\`\`

### 3.4 \`McpServerStatus\`

Enum representing the connection lifecycle state.

\`\`\`java
public enum McpServerStatus {
    CONNECTED,      // Active and functional connection
    DISCONNECTED,   // Not connected (initial state or after disconnect)
    ERROR,          // Connection error
    CONNECTING      // Connection in progress (transient state)
}
\`\`\`

**State diagram:**

\`\`\`
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
\`\`\`

### 3.5 \`McpExternalTool\`

Represents a tool discovered from an external MCP server.

\`\`\`java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpExternalTool {
    private String name;           // Unique tool name
    private String description;    // Description for the LLM model
    private String inputSchema;    // JSON Schema of input parameters
    private String serverId;       // ID of the server exposing the tool
}
\`\`\`

### 3.6 \`McpToolExecutionRequest\`

Request for executing an external tool.

\`\`\`java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpToolExecutionRequest {
    private String toolName;               // Name of the tool to execute
    private String serverId;               // Server on which to execute the tool
    private Map<String, Object> arguments; // Arguments to pass to the tool
}
\`\`\`

### 3.7 \`McpToolExecutionResult\`

Tool execution result with diagnostic information.

\`\`\`java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpToolExecutionResult {
    private String toolName;        // Name of the executed tool
    private Object result;          // Result (deserialized from JSON-RPC)
    private boolean success;        // Execution outcome
    private String errorMessage;    // Error message (if success=false)
    private long executionTimeMs;   // Execution time in milliseconds
}
\`\`\`

---

## 4. Inbound ports (Use Case)

Inbound ports define operations available to upper layers (API, agents).
They are located in \`com.localmind.domain.mcp.port.in\`.

### 4.1 \`McpServerManagementUseCase\`

Management of the lifecycle of registered MCP servers.

\`\`\`java
public interface McpServerManagementUseCase {
    McpServerRegistration register(McpServerRegistration server);   // Register and connect
    void remove(String serverId);                                   // Remove and disconnect
    McpServerRegistration get(String serverId);                     // Get by ID
    List<McpServerRegistration> listAll();                          // List all
    McpServerRegistration testConnection(String serverId);          // Test connection
    void reconnect(String serverId);                                // Reconnect
}
\`\`\`

### 4.2 \`McpToolDiscoveryUseCase\`

Discovery of available tools on connected servers.

\`\`\`java
public interface McpToolDiscoveryUseCase {
    List<McpExternalTool> listExternalTools(String serverId);   // Tools from a server
    List<McpExternalTool> listAllExternalTools();               // Tools from all servers
}
\`\`\`

### 4.3 \`McpToolExecutionUseCase\`

Execution of tools on external servers.

\`\`\`java
public interface McpToolExecutionUseCase {
    McpToolExecutionResult executeTool(McpToolExecutionRequest request);
}
\`\`\`

---

## 5. Outbound ports (SPI)

Outbound ports are interfaces implemented by the infrastructure layer.
They are located in \`com.localmind.domain.mcp.port.out\`.

### 5.1 \`McpClientPort\`

SPI for communication with external MCP servers. Abstracts the underlying MCP SDK.

\`\`\`java
public interface McpClientPort {
    boolean connect(McpServerRegistration server);                        // Connect
    void disconnect(String serverId);                                     // Disconnect
    boolean testConnection(String serverId);                              // Verify
    List<McpExternalTool> discoverTools(String serverId);                 // Discover tools
    McpToolExecutionResult executeTool(String serverId,
                                       McpToolExecutionRequest request);  // Execute tool
}
\`\`\`

### 5.2 \`McpServerRegistrationRepository\`

SPI for persisting server registrations.

\`\`\`java
public interface McpServerRegistrationRepository {
    McpServerRegistration save(McpServerRegistration server);
    void delete(String serverId);
    Optional<McpServerRegistration> findById(String serverId);
    List<McpServerRegistration> findAll();
}
\`\`\`

### 5.3 \`LocalToolDiscoveryPort\`

SPI for discovering local \`@Tool\` methods registered in the application context.

\`\`\`java
public interface LocalToolDiscoveryPort {
    /**
     * Returns all discovered local @Tool methods.
     * Each map contains: name (String), description (String), local (Boolean).
     *
     * @return unmodifiable list of tool info maps
     */
    List<Map<String, Object>> discoverLocalTools();
}
\`\`\`

This port is implemented by \`LocalToolDiscoveryService\` in the infrastructure layer
(see [section 7.2](#72-localtool-discoveryservice)) and injected directly into
\`McpToolController\` to serve the \`GET /api/v1/mcp/tools/local\` endpoint.

---

## 6. Domain services

Domain services implement business logic and are pure POJOs (no framework dependencies).
They are instantiated as beans via \`McpConfiguration\`.

### 6.1 \`McpServerManagementService\`

Implements \`McpServerManagementUseCase\`. Manages the complete server lifecycle.

\`\`\`java
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
\`\`\`

**Key behavior:**
- \`register()\`: Generates UUID, persists, then attempts automatic connection
- \`remove()\`: Disconnects before deleting from DB
- \`testConnection()\`: Verifies connection and updates status
- \`reconnect()\`: Complete cycle disconnect -> CONNECTING -> connect -> CONNECTED/ERROR

### 6.2 \`McpToolOrchestratorService\`

Implements \`McpToolDiscoveryUseCase\` and \`McpToolExecutionUseCase\`. Tool orchestration.

\`\`\`java
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
\`\`\`

**Key behavior:**
- \`listAllExternalTools()\`: Iterates over all CONNECTED servers and aggregates tools
- \`listExternalTools(serverId)\`: Delegates directly to \`McpClientPort\`
- \`executeTool()\`: Delegates to \`McpClientPort\` for the specified server

---

## 7. Infrastructure adapter

### 7.1 \`SpringAiMcpClientAdapter\` (Real MCP SDK)

Implements \`McpClientPort\` and manages MCP connections using the **real MCP SDK**.
Located in \`com.localmind.infrastructure.mcp.adapter\`.

The adapter uses \`McpSyncClient\` from the \`io.modelcontextprotocol\` package with two
transport implementations:

- **\`HttpClientSseClientTransport\`** for SSE (Server-Sent Events) servers
- **\`StdioClientTransport\`** with \`ServerParameters\` for STDIO (local process) servers

Each connection goes through a full MCP lifecycle: transport creation, \`client.initialize()\`,
\`client.listTools()\` for discovery, and \`client.callTool()\` for execution.

\`\`\`java
@Component
@ConditionalOnProperty(name = "localmind.mcp.client.enabled", havingValue = "true")
public class SpringAiMcpClientAdapter implements McpClientPort {

    private final Map<String, McpClientConnection> connections = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean connect(McpServerRegistration server) {
        try {
            McpClientConnection connection = new McpClientConnection(server, objectMapper);
            connections.put(server.getId(), connection);
            return true;
        } catch (Exception e) {
            log.error("Failed to connect to MCP server: {}", server.getName(), e);
            return false;
        }
    }
    // ... disconnect, testConnection, discoverTools, executeTool
}
\`\`\`

#### Inner class: \`McpClientConnection\`

The \`McpClientConnection\` inner class wraps the MCP SDK lifecycle for a single server
connection. It creates the appropriate transport based on server type and initializes the
client during construction.

\`\`\`java
private static class McpClientConnection {
    private final McpServerRegistration server;
    private final McpSyncClient client;
    private final ObjectMapper objectMapper;
    private volatile boolean alive;

    McpClientConnection(McpServerRegistration server, ObjectMapper objectMapper) {
        this.server = server;
        this.objectMapper = objectMapper;
        this.client = createClient(server);
        this.client.initialize();   // MCP handshake
        this.alive = true;
    }
    // ...
}
\`\`\`

**Transport creation by server type:**

| Server Type | Transport Class                  | Key Parameter                              |
|-------------|----------------------------------|--------------------------------------------|
| **SSE**     | \`HttpClientSseClientTransport\`   | \`config.getUrl()\` (e.g. \`http://host/sse\`) |
| **STDIO**   | \`StdioClientTransport\`           | \`ServerParameters(command, args)\`          |

**SSE transport:**

\`\`\`java
private static McpSyncClient createSseClient(McpServerRegistration server,
                                               McpServerConfig config, Duration timeout) {
    HttpClientSseClientTransport transport =
            HttpClientSseClientTransport.builder(config.getUrl()).build();
    return McpClient.sync(transport)
            .requestTimeout(timeout)
            .build();
}
\`\`\`

**STDIO transport:**

\`\`\`java
private static McpSyncClient createStdioClient(McpServerRegistration server,
                                                 McpServerConfig config, Duration timeout) {
    ServerParameters.Builder paramsBuilder = ServerParameters.builder(config.getCommand());
    if (config.getArgs() != null && !config.getArgs().isEmpty()) {
        paramsBuilder.args(config.getArgs().toArray(new String[0]));
    }
    StdioClientTransport transport = new StdioClientTransport(paramsBuilder.build());
    return McpClient.sync(transport)
            .requestTimeout(timeout)
            .build();
}
\`\`\`

**Tool discovery via MCP SDK:**

\`\`\`java
List<McpExternalTool> getTools() {
    McpSchema.ListToolsResult toolsResult = client.listTools();
    return toolsResult.tools().stream()
            .map(this::mapToExternalTool)
            .collect(Collectors.toList());
}
\`\`\`

**Tool execution via MCP SDK:**

\`\`\`java
Object executeTool(String toolName, Map<String, Object> arguments) {
    McpSchema.CallToolResult result = client.callTool(
            new McpSchema.CallToolRequest(toolName, arguments != null ? arguments : Map.of()));

    if (Boolean.TRUE.equals(result.isError())) {
        String errorText = extractTextContent(result);
        throw new RuntimeException("MCP tool '" + toolName + "' returned error: " + errorText);
    }
    return extractContent(result);
}
\`\`\`

**Content extraction** supports multiple MCP content types:

| Content Type            | Handling                                       |
|-------------------------|------------------------------------------------|
| Single \`TextContent\`    | Returns the text string directly               |
| Multiple content items  | Returns \`List<Map<String, Object>>\` with type  |
| \`ImageContent\`          | Includes \`data\` and \`mimeType\` in map          |
| \`EmbeddedResource\`      | Includes \`resource\` object in map              |

**Connection lifecycle methods:**

- \`close()\`: Sets \`alive = false\` and calls \`client.closeGracefully()\`
- \`isAlive()\`: Returns the volatile \`alive\` flag for connection health checks

### In-memory connection management

\`\`\`
ConcurrentHashMap<serverId, McpClientConnection>
   |
   +-- "abc-123" -> McpClientConnection(filesystem-server, McpSyncClient, alive=true)
   +-- "def-456" -> McpClientConnection(database-server, McpSyncClient, alive=true)
   +-- "ghi-789" -> McpClientConnection(web-scraper, McpSyncClient, alive=false)
\`\`\`

### 7.2 \`LocalToolDiscoveryService\`

Implements \`LocalToolDiscoveryPort\` and dynamically discovers all local \`@Tool\` methods
via reflection. Located in \`com.localmind.infrastructure.mcp.discovery\`.

This service replaces the previous hardcoded list of 3 local tools with a runtime scan
that currently discovers **135+ local tools** across all \`LocalMind*Tools\` beans.

\`\`\`java
@Component
public class LocalToolDiscoveryService implements LocalToolDiscoveryPort {

    private final ApplicationContext applicationContext;
    private volatile List<Map<String, Object>> cachedTools;

    public LocalToolDiscoveryService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public List<Map<String, Object>> discoverLocalTools() {
        if (cachedTools != null) {
            return cachedTools;
        }

        List<Map<String, Object>> tools = new ArrayList<>();
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            String className = beanClass.getSimpleName();

            // Filter: only scan beans matching LocalMind*Tools pattern
            if (!className.startsWith("LocalMind") || !className.endsWith("Tools")) {
                continue;
            }

            for (Method method : beanClass.getDeclaredMethods()) {
                Tool toolAnnotation = method.getAnnotation(Tool.class);
                if (toolAnnotation != null) {
                    tools.add(Map.of(
                            "name", method.getName(),
                            "description", toolAnnotation.description(),
                            "local", true
                    ));
                }
            }
        }

        cachedTools = Collections.unmodifiableList(tools);
        return cachedTools;
    }
}
\`\`\`

**Key characteristics:**

- **Bean scanning pattern**: Only beans whose class name matches \`LocalMind*Tools\` are scanned
  (e.g. \`LocalMindMcpTools\`, \`LocalMindCodeTools\`, \`LocalMindDatabaseTools\`, etc.)
- **Annotation extraction**: Reads \`@Tool(description = "...")\` from each method to populate
  the \`name\` and \`description\` fields
- **Lazy caching**: Results are computed once on first invocation and cached as an
  \`unmodifiableList\` in a \`volatile\` field for thread safety
- **Output format**: Each tool is a \`Map<String, Object>\` with keys \`name\`, \`description\`, \`local\`

**Scanned tool bean classes:**

| Bean Class                  | Domain           |
|-----------------------------|------------------|
| \`LocalMindMcpTools\`         | MCP management   |
| \`LocalMindCodeTools\`        | Code analysis    |
| \`LocalMindTestTools\`        | Testing tools    |
| \`LocalMindDatabaseTools\`    | Database ops     |
| \`LocalMindDevOpsTools\`      | DevOps/CI-CD     |
| \`LocalMindDocTools\`         | Documentation    |
| \`LocalMindProjectTools\`     | Project mgmt     |
| \`LocalMindUtilityTools\`     | Utilities        |
| \`LocalMindGovernanceTools\`  | Governance       |
| \`LocalMindOpsTools\`         | Operations       |
| \`LocalMindQualityTools\`     | Quality gates    |
| \`LocalMindCommTools\`        | Communications   |

---

## 8. Persistence

### 8.1 \`McpServerEntity\`

JPA entity mapped to the \`mcp_servers\` table (created by Flyway V6).

\`\`\`java
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
\`\`\`

### 8.2 \`McpServerJpaRepository\`

Spring Data JPA repository with derived query.

\`\`\`java
@Repository
public interface McpServerJpaRepository extends JpaRepository<McpServerEntity, String> {
    List<McpServerEntity> findByStatus(String status);
}
\`\`\`

### 8.3 \`McpServerRepositoryAdapter\`

Adapter that implements the domain port \`McpServerRegistrationRepository\`, translating
between domain model and JPA entity.

\`\`\`java
@Component
public class McpServerRepositoryAdapter implements McpServerRegistrationRepository {

    private final McpServerJpaRepository jpaRepository;

    // Bidirectional mapping: domain <-> entity
    private McpServerEntity toEntity(McpServerRegistration domain) { /* ... */ }
    private McpServerRegistration toDomain(McpServerEntity entity) { /* ... */ }
}
\`\`\`

**Note on args mapping:** STDIO command arguments are stored as a comma-separated
string in the DB (\`args TEXT\`) and converted to \`List<String>\` in the domain model
via \`String.join(",", args)\` / \`args.split(",")\`.

---

## 9. REST API

The REST API for MCP is located in the \`localmind-api\` module under \`com.localmind.api.mcp\`.

### 9.1 \`McpServerController\`

Endpoints for managing registered MCP servers.

| Method | Endpoint                              | Description                 |
|--------|---------------------------------------|-----------------------------|
| POST   | \`/api/v1/mcp/servers\`                 | Register a new server       |
| GET    | \`/api/v1/mcp/servers\`                 | List all servers            |
| GET    | \`/api/v1/mcp/servers/{serverId}\`      | Get server details          |
| DELETE | \`/api/v1/mcp/servers/{serverId}\`      | Remove a server             |
| POST   | \`/api/v1/mcp/servers/{serverId}/test\` | Test the connection         |
| POST   | \`/api/v1/mcp/servers/{serverId}/reconnect\` | Reconnect to server    |

**Request DTO (\`CreateMcpServerRequestDto\`):**

\`\`\`java
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
\`\`\`

**Response DTO (\`McpServerDto\`):**

\`\`\`java
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
\`\`\`

### 9.2 \`McpToolController\`

Endpoints for tool discovery and execution.

| Method | Endpoint                                | Description                     |
|--------|-----------------------------------------|---------------------------------|
| GET    | \`/api/v1/mcp/tools\`                     | All external tools              |
| GET    | \`/api/v1/mcp/tools/servers/{serverId}\`  | Tools from a specific server    |
| GET    | \`/api/v1/mcp/tools/local\`               | LocalMind local tools (dynamic) |
| POST   | \`/api/v1/mcp/tools/execute\`             | Execute a tool                  |

The \`/local\` endpoint now uses \`LocalToolDiscoveryPort\` to dynamically discover all \`@Tool\`
methods via reflection, instead of returning a hardcoded list. The controller injects the
port directly:

\`\`\`java
@RestController
@RequestMapping("/api/v1/mcp/tools")
public class McpToolController {

    private final McpToolDiscoveryUseCase discoveryUseCase;
    private final McpToolExecutionUseCase executionUseCase;
    private final LocalToolDiscoveryPort localToolDiscoveryService;

    @GetMapping("/local")
    public ResponseEntity<List<McpToolDto>> listLocalTools() {
        List<McpToolDto> localTools = localToolDiscoveryService.discoverLocalTools().stream()
                .map(tool -> McpToolDto.builder()
                        .name((String) tool.get("name"))
                        .description((String) tool.get("description"))
                        .local(true)
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(localTools);
    }
}
\`\`\`

**Execution DTO (\`McpToolExecutionRequestDto\`):**

\`\`\`java
public class McpToolExecutionRequestDto {
    @NotBlank private String toolName;      // Tool name
    @NotBlank private String serverId;      // Server ID
    private Map<String, Object> arguments;  // Tool arguments
}
\`\`\`

### 9.3 \`McpScrumController\`

REST controller for Scrum board operations. Delegates to \`ScrumBoardUseCase\`.
Located at \`/api/v1/mcp/scrum\`.

| Method | Endpoint                              | Description                      |
|--------|---------------------------------------|----------------------------------|
| GET    | \`/api/v1/mcp/scrum/backlog\`           | Get the product backlog          |
| GET    | \`/api/v1/mcp/scrum/sprints/{id}\`      | Get sprint details by ID         |
| GET    | \`/api/v1/mcp/scrum/sprints/{id}/board\`| Get sprint board (columns/tasks) |
| POST   | \`/api/v1/mcp/scrum/sprints\`           | Create a new sprint              |
| POST   | \`/api/v1/mcp/scrum/stories\`           | Create a new user story          |
| POST   | \`/api/v1/mcp/scrum/tasks\`             | Create a new task                |
| PUT    | \`/api/v1/mcp/scrum/tasks/{id}/status\` | Update a task's status           |

\`\`\`java
@RestController
@RequestMapping("/api/v1/mcp/scrum")
public class McpScrumController {

    private final ScrumBoardUseCase scrumBoardUseCase;

    @GetMapping("/backlog")
    public ResponseEntity<Map<String, Object>> getBacklog() {
        return ResponseEntity.ok(scrumBoardUseCase.getBacklog());
    }

    @PostMapping("/sprints")
    public ResponseEntity<Map<String, Object>> createSprint(
            @RequestBody CreateSprintRequest request) {
        return ResponseEntity.ok(scrumBoardUseCase.createSprint(
                request.getName(), request.getStartDate(),
                request.getEndDate(), request.getGoals()));
    }

    @PostMapping("/stories")
    public ResponseEntity<Map<String, Object>> createStory(
            @RequestBody CreateStoryRequest request) {
        return ResponseEntity.ok(scrumBoardUseCase.createStory(
                request.getTitle(), request.getDescription(),
                request.getAcceptanceCriteria(), request.getStoryPoints(),
                request.getPriority(), request.getSprintId()));
    }

    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<Map<String, Object>> updateTaskStatus(
            @PathVariable String taskId,
            @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(scrumBoardUseCase.updateTaskStatus(
                taskId, request.getStatus()));
    }
}
\`\`\`

**Request DTOs:**

| DTO                       | Fields                                                         |
|---------------------------|----------------------------------------------------------------|
| \`CreateSprintRequest\`     | \`name\`, \`startDate\`, \`endDate\`, \`goals\`                        |
| \`CreateStoryRequest\`      | \`title\`, \`description\`, \`acceptanceCriteria\`, \`storyPoints\`, \`priority\`, \`sprintId\` |
| \`CreateTaskRequest\`       | \`title\`, \`description\`, \`storyId\`, \`assignee\`                  |
| \`UpdateTaskStatusRequest\` | \`status\`                                                       |

### 9.4 \`McpIncidentController\`

REST controller for incident management. Delegates to \`IncidentManagerUseCase\`.
Located at \`/api/v1/mcp/incidents\`.

| Method | Endpoint                                  | Description                         |
|--------|-------------------------------------------|-------------------------------------|
| GET    | \`/api/v1/mcp/incidents\`                   | List incidents (filter by status, severity, limit) |
| POST   | \`/api/v1/mcp/incidents\`                   | Open a new incident                 |
| PUT    | \`/api/v1/mcp/incidents/{id}\`              | Update an existing incident         |
| POST   | \`/api/v1/mcp/incidents/{id}/timeline\`     | Add a timeline entry to an incident |
| POST   | \`/api/v1/mcp/incidents/{id}/resolve\`      | Resolve an incident                 |
| GET    | \`/api/v1/mcp/incidents/{id}/postmortem\`   | Generate a postmortem report        |

\`\`\`java
@RestController
@RequestMapping("/api/v1/mcp/incidents")
public class McpIncidentController {

    private final IncidentManagerUseCase incidentManagerUseCase;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listIncidents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(
                incidentManagerUseCase.listIncidents(status, severity, limit));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> openIncident(
            @RequestBody OpenIncidentRequest request) {
        return ResponseEntity.ok(incidentManagerUseCase.openIncident(
                request.getTitle(), request.getSeverity(),
                request.getDescription(), request.getAffectedSystemsJson()));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<Map<String, Object>> resolveIncident(
            @PathVariable String id,
            @RequestBody ResolveIncidentRequest request) {
        return ResponseEntity.ok(incidentManagerUseCase.resolveIncident(
                id, request.getResolution(), request.getRootCause()));
    }
}
\`\`\`

**Request DTOs:**

| DTO                     | Fields                                                    |
|-------------------------|-----------------------------------------------------------|
| \`OpenIncidentRequest\`   | \`title\`, \`severity\`, \`description\`, \`affectedSystemsJson\` |
| \`UpdateIncidentRequest\` | \`status\`, \`note\`                                          |
| \`TimelineEntryRequest\`  | \`description\`, \`source\`                                   |
| \`ResolveIncidentRequest\`| \`resolution\`, \`rootCause\`                                 |

### 9.5 \`McpTimeController\`

REST controller for time tracking operations. Delegates to \`TimeTrackingUseCase\`.
Located at \`/api/v1/mcp/time\`.

| Method | Endpoint                       | Description                       |
|--------|--------------------------------|-----------------------------------|
| POST   | \`/api/v1/mcp/time/start\`       | Start a timer for a task          |
| POST   | \`/api/v1/mcp/time/stop\`        | Stop the currently running timer  |
| POST   | \`/api/v1/mcp/time/log\`         | Log time manually                 |
| GET    | \`/api/v1/mcp/time/timesheet\`   | Get timesheet (filter by dates, user) |

\`\`\`java
@RestController
@RequestMapping("/api/v1/mcp/time")
public class McpTimeController {

    private final TimeTrackingUseCase timeTrackingUseCase;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startTimer(
            @RequestBody StartTimerRequest request) {
        return ResponseEntity.ok(timeTrackingUseCase.startTimer(
                request.getTaskId(), request.getDescription()));
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopTimer() {
        return ResponseEntity.ok(timeTrackingUseCase.stopTimer());
    }

    @PostMapping("/log")
    public ResponseEntity<Map<String, Object>> logTime(
            @RequestBody LogTimeRequest request) {
        return ResponseEntity.ok(timeTrackingUseCase.logTime(
                request.getTaskId(), request.getDurationMinutes(),
                request.getDescription(), request.getDate()));
    }

    @GetMapping("/timesheet")
    public ResponseEntity<Map<String, Object>> getTimesheet(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String userId) {
        return ResponseEntity.ok(
                timeTrackingUseCase.getTimesheet(startDate, endDate, userId));
    }
}
\`\`\`

**Request DTOs:**

| DTO                 | Fields                                          |
|---------------------|-------------------------------------------------|
| \`StartTimerRequest\` | \`taskId\`, \`description\`                         |
| \`LogTimeRequest\`    | \`taskId\`, \`durationMinutes\`, \`description\`, \`date\` |

---

## 10. Complete flow

### Registration and connection to a server

\`\`\`
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
        |                           new McpClientConnection(server, objectMapper)
        |                              |-> createSseClient() or createStdioClient()
        |                              |-> client.initialize()  (MCP handshake)
        |                           ConcurrentHashMap.put(id, connection)
        |
6. If connected: update status=CONNECTED, lastConnectedAt
        |
7. McpServerRegistrationRepository.save()  --> Updated DB
        |
8. Response --> McpServerDto (with updated status)
\`\`\`

### Tool discovery and execution

\`\`\`
1. HTTP Client --> GET /api/v1/mcp/tools/servers/{serverId}
        |
2. McpToolController --> McpToolDiscoveryUseCase.listExternalTools(serverId)
        |
3. McpToolOrchestratorService --> McpClientPort.discoverTools(serverId)
        |
4. SpringAiMcpClientAdapter --> connection.getTools()
        |                        client.listTools() -> McpSchema.ListToolsResult
        |                        map McpSchema.Tool -> McpExternalTool
5. McpExternalTool list <-- response
        |
6. HTTP Client --> POST /api/v1/mcp/tools/execute
        |                (intercepted by McpAccessInterceptor - see section 11)
        |
7. McpToolController --> McpToolExecutionUseCase.executeTool(request)
        |
8. McpToolOrchestratorService --> McpClientPort.executeTool(serverId, request)
        |
9. SpringAiMcpClientAdapter --> connection.executeTool(name, args)
        |                        client.callTool(CallToolRequest)
        |                        -> extractContent(CallToolResult)
10. McpToolExecutionResult <-- response with result, success, executionTimeMs
\`\`\`

### Local tool discovery

\`\`\`
1. HTTP Client --> GET /api/v1/mcp/tools/local
        |
2. McpToolController --> LocalToolDiscoveryPort.discoverLocalTools()
        |
3. LocalToolDiscoveryService (infrastructure)
        |-> scans ApplicationContext for beans matching LocalMind*Tools
        |-> reflects on methods with @Tool annotation
        |-> extracts method name + @Tool description
        |-> caches result as unmodifiable list
        |
4. Response --> List<McpToolDto> (135+ tools with name, description, local=true)
\`\`\`

---

## 11. Security -- McpAccessInterceptor

The \`McpAccessInterceptor\` is a Spring \`HandlerInterceptor\` that enforces access policies
on MCP tool execution requests. It is located in
\`com.localmind.infrastructure.mcp.security\`.

### 11.1 How it works

The interceptor is registered in \`WebMvcConfig\` on the path pattern
\`/api/v1/mcp/tools/execute\` (POST only). It operates in the \`preHandle\` phase before
the controller method is invoked.

**Processing flow:**

\`\`\`
1. HTTP POST /api/v1/mcp/tools/execute
        |
2. CachedBodyFilter (OncePerRequestFilter)
        |-> wraps request in CachedBodyHttpServletRequest
        |   (allows body to be read by interceptor AND controller)
        |
3. McpAccessInterceptor.preHandle()
        |-> reads X-User-Id header (default: "anonymous")
        |-> parses request body JSON for toolName and serverId
        |-> calls AccessPolicyUseCase.checkAccess(userId, server, tool)
        |
4a. If allowed=true or no matching policy --> proceeds to controller
4b. If allowed=false --> returns 403 with error JSON, blocks request
\`\`\`

### 11.2 \`McpAccessInterceptor\`

\`\`\`java
@Component
public class McpAccessInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String DEFAULT_USER = "anonymous";

    private final AccessPolicyUseCase accessPolicyUseCase;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // Only intercept POST to /mcp/tools/execute
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;
        if (!request.getRequestURI().endsWith("/mcp/tools/execute")) return true;

        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.isBlank()) userId = DEFAULT_USER;

        // Parse toolName and serverId from cached request body
        String toolName = null;
        String serverId = null;
        if (request instanceof CachedBodyHttpServletRequest cachedRequest) {
            byte[] body = cachedRequest.getCachedBody();
            JsonNode node = objectMapper.readTree(body);
            toolName = node.has("toolName") ? node.get("toolName").asText() : null;
            serverId = node.has("serverId") ? node.get("serverId").asText() : null;
        }

        if (toolName == null || toolName.isBlank()) return true;

        String serverForPolicy = serverId != null && !serverId.isBlank() ? serverId : "local";

        Map<String, Object> checkResult =
                accessPolicyUseCase.checkAccess(userId, serverForPolicy, toolName);

        Boolean allowed = (Boolean) checkResult.get("allowed");
        if (allowed != null && !allowed) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                    "error", "Access denied by policy: " + extractPolicyName(checkResult),
                    "userId", userId,
                    "tool", toolName,
                    "server", serverForPolicy
            )));
            return false;
        }
        return true;
    }
}
\`\`\`

### 11.3 \`CachedBodyHttpServletRequest\`

A \`HttpServletRequestWrapper\` that caches the request body on construction so it can be
read multiple times -- once by the interceptor and again by the controller's \`@RequestBody\`
deserialization.

\`\`\`java
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
    private final byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = request.getInputStream().readAllBytes();
    }

    @Override
    public ServletInputStream getInputStream() {
        return new DelegatingServletInputStream(new ByteArrayInputStream(cachedBody));
    }

    public byte[] getCachedBody() { return cachedBody; }
}
\`\`\`

### 11.4 \`WebMvcConfig\` registration

The interceptor and its supporting filter are wired in \`WebMvcConfig\`:

\`\`\`java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final McpAccessInterceptor mcpAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mcpAccessInterceptor)
                .addPathPatterns("/api/v1/mcp/tools/execute");
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> cachedBodyFilter() {
        // Wraps POST requests to /mcp/tools/execute in CachedBodyHttpServletRequest
        // so the body can be read by both the interceptor and the controller
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OncePerRequestFilter() { /* ... */ });
        registration.addUrlPatterns("/api/v1/mcp/tools/execute");
        registration.setOrder(1);
        return registration;
    }
}
\`\`\`

### 11.5 Default behavior

| Condition                     | Result         |
|-------------------------------|----------------|
| No \`X-User-Id\` header        | \`userId = "anonymous"\` |
| No matching policy found      | Access **allowed** |
| Policy found, \`allowed=true\`  | Access **allowed** |
| Policy found, \`allowed=false\` | Access **denied** (403) |
| Missing \`toolName\` in body    | Access **allowed** (skips check) |

### 11.6 403 error response format

When access is denied, the interceptor returns:

\`\`\`json
{
    "error": "Access denied by policy: <policyName>",
    "userId": "<userId>",
    "tool": "<toolName>",
    "server": "<serverId>"
}
\`\`\`

---

> **Documentation navigation:**
> - Previous: [02-server-implementation.md](/docs/12-mcp-integration/server-implementation)
> - Next: [04-configuration.md](/docs/12-mcp-integration/configuration)
> - Agent integration: [06-agent-integration.md](/docs/12-mcp-integration/agent-integration)
`;
