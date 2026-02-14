# Implementazione MCP Client in LocalMind

**Progetto:** LocalMind - Piattaforma AI Local-First
**Versione:** 0.1.0
**Ultimo aggiornamento:** 2026-02-13
**Moduli di riferimento:** localmind-domain (`domain.mcp`), localmind-infrastructure (`infrastructure.mcp`), localmind-api (`api.mcp`)

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Architettura esagonale del client MCP](#2-architettura-esagonale-del-client-mcp)
3. [Domain model](#3-domain-model)
4. [Ports di ingresso (Use Case)](#4-ports-di-ingresso-use-case)
5. [Ports di uscita (SPI)](#5-ports-di-uscita-spi)
6. [Domain services](#6-domain-services)
7. [Infrastructure adapter](#7-infrastructure-adapter)
8. [Persistenza](#8-persistenza)
9. [API REST](#9-api-rest)
10. [Flusso completo](#10-flusso-completo)
11. [Security](#11-security)
12. [Dashboard e Insight con dati reali](#12-dashboard-e-insight-con-dati-reali)

---

## 1. Panoramica

LocalMind agisce come **MCP Client** per connettersi a server MCP esterni e utilizzare i loro
tool, risorse e prompt. Questo consente di estendere dinamicamente le capacita' della piattaforma
senza modificare il codice core.

L'implementazione segue fedelmente l'**architettura esagonale** del progetto, con una netta
separazione tra domain model, ports (use case e SPI) e adapters (infrastructure e API).

```
+-----------------------------------------------------------------+
|                         localmind-api                           |
|  McpServerController   McpToolController                        |
|  McpScrumController    McpIncidentController  McpTimeController |
|         |                      |                                |
+---------+----------------------+--------------------------------+
          |                      |
+---------v----------------------v---------------------------------+
|                       localmind-domain                           |
|  McpServerManagementUseCase  McpToolDiscoveryUseCase             |
|  McpToolExecutionUseCase     ScrumBoardUseCase                   |
|  IncidentManagerUseCase      TimeTrackingUseCase                 |
|  AccessPolicyUseCase         DashboardUseCase                    |
|         |                                                        |
|  McpServerManagementService  McpToolOrchestratorService          |
|  DashboardService            InsightEngineService                |
|         |                      |                                 |
|  McpClientPort (SPI)   McpServerRegistrationRepository (SPI)     |
|  LocalToolDiscoveryPort (SPI)                                    |
+---------+----------------------+---------------------------------+
          |                      |
+---------v----------------------v---------------------------------+
|                   localmind-infrastructure                       |
|  SpringAiMcpClientAdapter    McpServerRepositoryAdapter          |
|  (MCP SDK: McpSyncClient)   (JPA -> McpServerEntity)            |
|  LocalToolDiscoveryService   McpAccessInterceptor                |
+------------------------------------------------------------------+
```

---

## 2. Architettura esagonale del client MCP

Il client MCP e' organizzato secondo il pattern Ports & Adapters:

### Layer e responsabilita'

| Layer             | Package                                      | Responsabilita'                   |
|-------------------|----------------------------------------------|-----------------------------------|
| **Domain Model**  | `domain.mcp.model`                           | Entita' e value objects           |
| **Inbound Ports** | `domain.mcp.port.in`                         | Use case (interfacce)             |
| **Outbound Ports**| `domain.mcp.port.out`                        | SPI per infrastruttura            |
| **Services**      | `domain.mcp.service`                         | Implementazione logica di business|
| **Infra Adapter** | `infrastructure.mcp.adapter`                 | Adapter MCP SDK reale (SSE/STDIO) |
| **Discovery**     | `infrastructure.mcp.discovery`               | Scoperta tool locali @Tool        |
| **Security**      | `infrastructure.mcp.security`                | Interceptor controllo accesso     |
| **Persistence**   | `infrastructure.mcp.persistence`             | JPA entity, repository adapter    |
| **API**           | `api.mcp.controller`, `api.mcp.dto`          | REST controller e DTO             |

### Principio della dipendenza

Le frecce di dipendenza puntano sempre verso l'interno (domain):

```
API --> Domain <-- Infrastructure
```

Il dominio non conosce Spring, JPA, o il MCP SDK. Definisce solo interfacce (ports)
che gli adapter implementano.

---

## 3. Domain model

Il domain model MCP si trova in `com.localmind.domain.mcp.model` e comprende:

### 3.1 `McpServerRegistration`

Rappresenta la registrazione di un server MCP esterno nel sistema.

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpServerRegistration {
    private String id;                    // UUID generato dal sistema
    private String name;                  // Nome leggibile del server
    private String description;           // Descrizione opzionale
    private McpServerType type;           // STDIO o SSE
    private McpServerConfig config;       // Configurazione di connessione
    private McpServerStatus status;       // Stato attuale della connessione
    private LocalDateTime createdAt;      // Timestamp di registrazione
    private LocalDateTime lastConnectedAt;// Ultima connessione riuscita
}
```

### 3.2 `McpServerConfig`

Value object con i parametri di connessione al server.

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpServerConfig {
    private String command;               // Comando per STDIO (es. "npx")
    private List<String> args;            // Argomenti comando STDIO
    private String url;                   // URL per SSE (es. "http://localhost:3001/sse")
    @Builder.Default
    private int timeoutSeconds = 30;      // Timeout connessione
    @Builder.Default
    private boolean autoReconnect = true; // Riconnessione automatica
}
```

**Invarianti di dominio:**
- Se `type == STDIO`, `command` deve essere non-null (validato dal DB constraint `chk_stdio_command`)
- Se `type == SSE`, `url` deve essere non-null (validato dal DB constraint `chk_sse_url`)

### 3.3 `McpServerType`

Enum che definisce i tipi di trasporto supportati.

```java
public enum McpServerType {
    STDIO("Standard Input/Output"),   // Processo locale via stdin/stdout
    SSE("Server-Sent Events");        // Connessione HTTP remota
    // ...
}
```

### 3.4 `McpServerStatus`

Enum che rappresenta lo stato del ciclo di vita della connessione.

```java
public enum McpServerStatus {
    CONNECTED,      // Connessione attiva e funzionante
    DISCONNECTED,   // Non connesso (stato iniziale o dopo disconnect)
    ERROR,          // Errore di connessione
    CONNECTING      // Connessione in corso (stato transitorio)
}
```

**Diagramma di stato:**

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

Rappresenta un tool scoperto da un server MCP esterno.

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpExternalTool {
    private String name;           // Nome univoco del tool
    private String description;    // Descrizione per il modello LLM
    private String inputSchema;    // JSON Schema dei parametri di input
    private String serverId;       // ID del server che espone il tool
}
```

### 3.6 `McpToolExecutionRequest`

Richiesta di esecuzione di un tool esterno.

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpToolExecutionRequest {
    private String toolName;               // Nome del tool da eseguire
    private String serverId;               // Server su cui eseguire il tool
    private Map<String, Object> arguments; // Argomenti da passare al tool
}
```

### 3.7 `McpToolExecutionResult`

Risultato dell'esecuzione di un tool, con informazioni di diagnostica.

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class McpToolExecutionResult {
    private String toolName;        // Nome del tool eseguito
    private Object result;          // Risultato (deserializzato dal JSON-RPC)
    private boolean success;        // Esito dell'esecuzione
    private String errorMessage;    // Messaggio di errore (se success=false)
    private long executionTimeMs;   // Tempo di esecuzione in millisecondi
}
```

---

## 4. Ports di ingresso (Use Case)

I ports di ingresso definiscono le operazioni disponibili per i layer superiori (API, agenti).
Si trovano in `com.localmind.domain.mcp.port.in`.

### 4.1 `McpServerManagementUseCase`

Gestione del ciclo di vita dei server MCP registrati.

```java
public interface McpServerManagementUseCase {
    McpServerRegistration register(McpServerRegistration server);   // Registra e connetti
    void remove(String serverId);                                   // Rimuovi e disconnetti
    McpServerRegistration get(String serverId);                     // Ottieni per ID
    List<McpServerRegistration> listAll();                          // Elenca tutti
    McpServerRegistration testConnection(String serverId);          // Testa connessione
    void reconnect(String serverId);                                // Riconnetti
}
```

### 4.2 `McpToolDiscoveryUseCase`

Scoperta dei tool disponibili sui server connessi.

```java
public interface McpToolDiscoveryUseCase {
    List<McpExternalTool> listExternalTools(String serverId);   // Tool di un server
    List<McpExternalTool> listAllExternalTools();               // Tool di tutti i server
}
```

### 4.3 `McpToolExecutionUseCase`

Esecuzione di tool su server esterni.

```java
public interface McpToolExecutionUseCase {
    McpToolExecutionResult executeTool(McpToolExecutionRequest request);
}
```

### 4.4 `AccessPolicyUseCase`

Gestione delle policy di accesso ai tool MCP.

```java
public interface AccessPolicyUseCase {
    Map<String, Object> createPolicy(String name, String effect, String rulesJson);
    Map<String, Object> checkAccess(String userId, String server, String tool);
    Map<String, Object> listPolicies();
    Map<String, Object> assignRole(String userId, String roleName);
    Map<String, Object> auditAccess(String userId, String server, int limit);
}
```

Utilizzato dal `McpAccessInterceptor` per verificare i permessi di accesso durante l'esecuzione dei tool (vedi [sezione 11](#11-security)).

---

## 5. Ports di uscita (SPI)

I ports di uscita sono interfacce implementate dall'infrastructure layer.
Si trovano in `com.localmind.domain.mcp.port.out`.

### 5.1 `McpClientPort`

SPI per la comunicazione con server MCP esterni. Astrae il MCP SDK sottostante.

```java
public interface McpClientPort {
    boolean connect(McpServerRegistration server);                        // Connetti
    void disconnect(String serverId);                                     // Disconnetti
    boolean testConnection(String serverId);                              // Verifica
    List<McpExternalTool> discoverTools(String serverId);                 // Scopri tool
    McpToolExecutionResult executeTool(String serverId,
                                       McpToolExecutionRequest request);  // Esegui tool
}
```

### 5.2 `McpServerRegistrationRepository`

SPI per la persistenza delle registrazioni dei server.

```java
public interface McpServerRegistrationRepository {
    McpServerRegistration save(McpServerRegistration server);
    void delete(String serverId);
    Optional<McpServerRegistration> findById(String serverId);
    List<McpServerRegistration> findAll();
}
```

### 5.3 `LocalToolDiscoveryPort`

SPI per la scoperta dinamica dei tool locali annotati con `@Tool`.

```java
public interface LocalToolDiscoveryPort {
    /**
     * Ritorna tutti i metodi @Tool locali scoperti.
     * Ogni mappa contiene: name (String), description (String), local (Boolean).
     *
     * @return lista immutabile di mappe con informazioni sui tool
     */
    List<Map<String, Object>> discoverLocalTools();
}
```

Questo port e' stato introdotto per sostituire la precedente lista hardcoded di 3 tool locali con una scoperta dinamica basata su reflection. L'implementazione si trova in `LocalToolDiscoveryService` (vedi [sezione 7.2](#72-localttooldiscoveryservice)).

---

## 6. Domain services

I domain service implementano la logica di business e sono POJO puri (nessuna dipendenza
da framework). Sono istanziati come bean tramite `McpConfiguration`.

### 6.1 `McpServerManagementService`

Implementa `McpServerManagementUseCase`. Gestisce il ciclo di vita completo dei server.

```java
public class McpServerManagementService implements McpServerManagementUseCase {

    private final McpServerRegistrationRepository repository;
    private final McpClientPort clientPort;

    // Constructor injection (no @Autowired, POJO puro)

    @Override
    public McpServerRegistration register(McpServerRegistration server) {
        if (server.getId() == null) {
            server.setId(UUID.randomUUID().toString());
        }
        server.setCreatedAt(LocalDateTime.now());
        server.setStatus(McpServerStatus.DISCONNECTED);

        McpServerRegistration saved = repository.save(server);

        // Tentativo automatico di connessione alla registrazione
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

**Comportamento chiave:**
- `register()`: Genera UUID, persiste, poi tenta la connessione automatica
- `remove()`: Disconnette prima di eliminare dal DB
- `testConnection()`: Verifica la connessione e aggiorna lo stato
- `reconnect()`: Ciclo completo disconnect -> CONNECTING -> connect -> CONNECTED/ERROR

### 6.2 `McpToolOrchestratorService`

Implementa `McpToolDiscoveryUseCase` e `McpToolExecutionUseCase`. Orchestrazione tool.

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

**Comportamento chiave:**
- `listAllExternalTools()`: Itera su tutti i server CONNECTED e aggrega i tool
- `listExternalTools(serverId)`: Delega direttamente al `McpClientPort`
- `executeTool()`: Delega al `McpClientPort` per il server specificato

---

## 7. Infrastructure adapter

### 7.1 `SpringAiMcpClientAdapter` (MCP Client Reale SSE/STDIO)

Implementa `McpClientPort` e gestisce le connessioni MCP usando il **vero MCP SDK**
(`io.modelcontextprotocol`). Il vecchio stub e' stato sostituito con chiamate reali a
`McpSyncClient` per `initialize()`, `listTools()` e `callTool()`.

Si trova in `com.localmind.infrastructure.mcp.adapter`.

```java
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

    @Override
    public List<McpExternalTool> discoverTools(String serverId) {
        McpClientConnection connection = connections.get(serverId);
        if (connection == null) {
            return List.of();
        }
        return connection.getTools();
    }

    // ... executeTool(), disconnect(), testConnection()
}
```

#### Classe interna `McpClientConnection`

Wrapper del lifecycle di una singola connessione MCP. Supporta entrambi i trasporti
SSE e STDIO tramite il vero MCP SDK.

```java
private static class McpClientConnection {
    private final McpServerRegistration server;
    private final McpSyncClient client;
    private final ObjectMapper objectMapper;
    private volatile boolean alive;

    McpClientConnection(McpServerRegistration server, ObjectMapper objectMapper) {
        this.server = server;
        this.objectMapper = objectMapper;
        this.client = createClient(server);
        this.client.initialize();     // Inizializzazione reale MCP
        this.alive = true;
    }
    // ...
}
```

#### Creazione trasporto SSE

Per server di tipo `SSE`, viene creato un `HttpClientSseClientTransport` con l'URL configurato:

```java
private static McpSyncClient createSseClient(McpServerRegistration server,
                                               McpServerConfig config,
                                               Duration timeout) {
    HttpClientSseClientTransport transport =
        HttpClientSseClientTransport.builder(config.getUrl()).build();
    return McpClient.sync(transport)
            .requestTimeout(timeout)
            .build();
}
```

#### Creazione trasporto STDIO

Per server di tipo `STDIO`, viene creato un `StdioClientTransport` con `ServerParameters`:

```java
private static McpSyncClient createStdioClient(McpServerRegistration server,
                                                 McpServerConfig config,
                                                 Duration timeout) {
    ServerParameters.Builder paramsBuilder = ServerParameters.builder(config.getCommand());
    List<String> args = config.getArgs();
    if (args != null && !args.isEmpty()) {
        paramsBuilder.args(args.toArray(new String[0]));
    }
    StdioClientTransport transport = new StdioClientTransport(paramsBuilder.build());
    return McpClient.sync(transport)
            .requestTimeout(timeout)
            .build();
}
```

#### Scoperta tool via MCP SDK

La scoperta dei tool utilizza `client.listTools()` che esegue una vera chiamata JSON-RPC
`tools/list` al server MCP collegato:

```java
List<McpExternalTool> getTools() {
    McpSchema.ListToolsResult toolsResult = client.listTools();
    return toolsResult.tools().stream()
            .map(this::mapToExternalTool)
            .collect(Collectors.toList());
}
```

Ogni `McpSchema.Tool` viene mappato a `McpExternalTool` con serializzazione dell'`inputSchema`
tramite `ObjectMapper`.

#### Esecuzione tool via MCP SDK

L'esecuzione utilizza `client.callTool()` che esegue una vera chiamata JSON-RPC
`tools/call` al server MCP:

```java
Object executeTool(String toolName, Map<String, Object> arguments) {
    McpSchema.CallToolResult result = client.callTool(
            new McpSchema.CallToolRequest(toolName, arguments != null ? arguments : Map.of()));

    if (Boolean.TRUE.equals(result.isError())) {
        String errorText = extractTextContent(result);
        throw new RuntimeException("MCP tool '" + toolName + "' returned error: " + errorText);
    }
    return extractContent(result);
}
```

Il risultato viene estratto gestendo tre tipi di contenuto MCP:
- `McpSchema.TextContent` -- contenuto testuale (caso piu' comune)
- `McpSchema.ImageContent` -- contenuto immagine (con data e mimeType)
- `McpSchema.EmbeddedResource` -- risorsa embedded

Se il risultato contiene un singolo `TextContent`, viene restituita direttamente la stringa.
Altrimenti viene restituita una lista di mappe con tipo e contenuto.

#### Chiusura connessione

La chiusura utilizza `client.closeGracefully()` per terminare in modo pulito la connessione:

```java
void close() {
    alive = false;
    client.closeGracefully();
}
```

**Dipendenze MCP SDK:**
- `io.modelcontextprotocol.client.McpClient` -- factory per creare client MCP
- `io.modelcontextprotocol.client.McpSyncClient` -- client sincrono per chiamate JSON-RPC
- `io.modelcontextprotocol.client.transport.HttpClientSseClientTransport` -- trasporto SSE
- `io.modelcontextprotocol.client.transport.StdioClientTransport` -- trasporto STDIO
- `io.modelcontextprotocol.client.transport.ServerParameters` -- parametri per STDIO
- `io.modelcontextprotocol.spec.McpSchema` -- schema MCP (Tool, CallToolResult, ListToolsResult, etc.)

### Gestione delle connessioni in memoria

```
ConcurrentHashMap<serverId, McpClientConnection>
   |
   +-- "abc-123" -> McpClientConnection(filesystem-server, McpSyncClient[SSE], alive=true)
   +-- "def-456" -> McpClientConnection(database-server, McpSyncClient[STDIO], alive=true)
   +-- "ghi-789" -> McpClientConnection(web-scraper, McpSyncClient[SSE], alive=false)
```

### 7.2 `LocalToolDiscoveryService`

Implementa `LocalToolDiscoveryPort` e scopre dinamicamente tutti i metodi annotati con `@Tool`
nel contesto Spring. Si trova in `com.localmind.infrastructure.mcp.discovery`.

```java
@Component
public class LocalToolDiscoveryService implements LocalToolDiscoveryPort {

    private final ApplicationContext applicationContext;
    private volatile List<Map<String, Object>> cachedTools;

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

            // Filtra solo i bean LocalMind*Tools
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
```

**Caratteristiche principali:**

- **Scoperta via reflection**: Scansiona tutti i bean il cui nome classe inizia con `LocalMind` e termina con `Tools`, cercando metodi annotati con `@Tool` (Spring AI)
- **Cache lazy**: I risultati vengono calcolati una sola volta e poi memorizzati in una lista immutabile (`volatile` per thread-safety)
- **135 tool locali scoperti**: Dai 12 bean tool registrati (`LocalMindMcpTools`, `LocalMindUtilityTools`, `LocalMindCodeTools`, `LocalMindTestTools`, `LocalMindDevOpsTools`, `LocalMindDatabaseTools`, `LocalMindDocTools`, `LocalMindProjectTools`, `LocalMindGovernanceTools`, `LocalMindOpsTools`, `LocalMindQualityTools`, `LocalMindCommTools`)
- **Sostituisce la lista hardcoded**: L'endpoint `GET /api/v1/mcp/tools/local` ora ritorna i tool scoperti dinamicamente, non piu' i 3 tool fissi precedenti

**Port nel dominio (`LocalToolDiscoveryPort`):**

Il port nel dominio `com.localmind.domain.mcp.port.out.LocalToolDiscoveryPort` definisce il contratto:

```java
public interface LocalToolDiscoveryPort {
    List<Map<String, Object>> discoverLocalTools();
}
```

Il `McpToolController` inietta direttamente questo port per servire l'endpoint `/local`:

```java
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
```

---

## 8. Persistenza

### 8.1 `McpServerEntity`

Entity JPA mappata sulla tabella `mcp_servers` (creata da Flyway V6).

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
    private String type;              // "STDIO" o "SSE"

    @Column(nullable = false, length = 50)
    private String status;            // "CONNECTED", "DISCONNECTED", etc.

    @Column(length = 500)
    private String command;           // Per STDIO

    @Column(columnDefinition = "TEXT")
    private String args;              // Per STDIO (comma-separated)

    @Column(length = 500)
    private String url;               // Per SSE

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

Repository Spring Data JPA con query derivata.

```java
@Repository
public interface McpServerJpaRepository extends JpaRepository<McpServerEntity, String> {
    List<McpServerEntity> findByStatus(String status);
}
```

### 8.3 `McpServerRepositoryAdapter`

Adapter che implementa il port di dominio `McpServerRegistrationRepository` traducendo
tra domain model e JPA entity.

```java
@Component
public class McpServerRepositoryAdapter implements McpServerRegistrationRepository {

    private final McpServerJpaRepository jpaRepository;

    // Mapping bidirezionale: domain <-> entity
    private McpServerEntity toEntity(McpServerRegistration domain) { /* ... */ }
    private McpServerRegistration toDomain(McpServerEntity entity) { /* ... */ }
}
```

**Nota sul mapping degli args:** Gli argomenti del comando STDIO sono memorizzati come
stringa comma-separated nel DB (`args TEXT`) e convertiti in `List<String>` nel domain
model tramite `String.join(",", args)` / `args.split(",")`.

---

## 9. API REST

L'API REST per MCP si trova nel modulo `localmind-api` sotto `com.localmind.api.mcp`.

### 9.1 `McpServerController`

Endpoint per la gestione dei server MCP registrati.

| Metodo | Endpoint                                   | Descrizione                 |
|--------|--------------------------------------------|-----------------------------|
| POST   | `/api/v1/mcp/servers`                      | Registra un nuovo server    |
| GET    | `/api/v1/mcp/servers`                      | Elenca tutti i server       |
| GET    | `/api/v1/mcp/servers/{serverId}`           | Ottieni dettagli server     |
| DELETE | `/api/v1/mcp/servers/{serverId}`           | Rimuovi un server           |
| POST   | `/api/v1/mcp/servers/{serverId}/test`      | Testa la connessione        |
| POST   | `/api/v1/mcp/servers/{serverId}/reconnect` | Riconnetti al server        |

**DTO di richiesta (`CreateMcpServerRequestDto`):**

```java
public class CreateMcpServerRequestDto {
    @NotBlank private String name;          // Obbligatorio
    private String description;             // Opzionale
    @NotNull private String type;           // "STDIO" o "SSE"
    private String command;                 // Per STDIO
    private List<String> args;              // Per STDIO
    private String url;                     // Per SSE
    private Integer timeoutSeconds;         // Default: 30
    private Boolean autoReconnect;          // Default: true
}
```

**DTO di risposta (`McpServerDto`):**

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

Endpoint per la scoperta e l'esecuzione dei tool.

| Metodo | Endpoint                                | Descrizione                     |
|--------|-----------------------------------------|---------------------------------|
| GET    | `/api/v1/mcp/tools`                     | Tutti i tool esterni            |
| GET    | `/api/v1/mcp/tools/servers/{serverId}`  | Tool di un server specifico     |
| GET    | `/api/v1/mcp/tools/local`               | Tool locali (scoperta dinamica) |
| POST   | `/api/v1/mcp/tools/execute`             | Esegui un tool (protetto da McpAccessInterceptor) |

**Nota:** L'endpoint `GET /api/v1/mcp/tools/local` ora utilizza `LocalToolDiscoveryPort` per la scoperta dinamica dei tool locali via reflection, restituendo tutti i 135 metodi `@Tool` registrati nel contesto Spring. Il precedente approccio con 3 tool hardcoded e' stato rimosso.

**DTO esecuzione (`McpToolExecutionRequestDto`):**

```java
public class McpToolExecutionRequestDto {
    @NotBlank private String toolName;      // Nome del tool
    @NotBlank private String serverId;      // ID del server
    private Map<String, Object> arguments;  // Argomenti del tool
}
```

### 9.3 `McpScrumController`

Endpoint per la gestione Scrum (backlog, sprint, storie, task). Delegano a `ScrumBoardUseCase`.

Base path: `/api/v1/mcp/scrum`

| Metodo | Endpoint                                 | Descrizione                      |
|--------|------------------------------------------|----------------------------------|
| GET    | `/api/v1/mcp/scrum/backlog`              | Ottieni il backlog completo      |
| GET    | `/api/v1/mcp/scrum/sprints/{sprintId}`   | Dettagli di uno sprint           |
| GET    | `/api/v1/mcp/scrum/sprints/{sprintId}/board` | Board visuale dello sprint   |
| POST   | `/api/v1/mcp/scrum/sprints`              | Crea un nuovo sprint             |
| POST   | `/api/v1/mcp/scrum/stories`              | Crea una nuova user story        |
| POST   | `/api/v1/mcp/scrum/tasks`                | Crea un nuovo task               |
| PUT    | `/api/v1/mcp/scrum/tasks/{taskId}/status`| Aggiorna lo stato di un task     |

**DTO di richiesta:**

- `CreateSprintRequest`: `name`, `startDate`, `endDate`, `goals`
- `CreateStoryRequest`: `title`, `description`, `acceptanceCriteria`, `storyPoints`, `priority`, `sprintId`
- `CreateTaskRequest`: `title`, `description`, `storyId`, `assignee`
- `UpdateTaskStatusRequest`: `status`

**Esempio di utilizzo:**

```bash
# Ottieni il backlog
curl -X GET http://localhost:8080/api/v1/mcp/scrum/backlog

# Crea un nuovo sprint
curl -X POST http://localhost:8080/api/v1/mcp/scrum/sprints \
  -H "Content-Type: application/json" \
  -d '{"name":"Sprint 5","startDate":"2026-02-13","endDate":"2026-02-27","goals":"Feature X"}'

# Aggiorna stato task
curl -X PUT http://localhost:8080/api/v1/mcp/scrum/tasks/abc-123/status \
  -H "Content-Type: application/json" \
  -d '{"status":"in_progress"}'
```

### 9.4 `McpIncidentController`

Endpoint per la gestione degli incidenti. Delegano a `IncidentManagerUseCase`.

Base path: `/api/v1/mcp/incidents`

| Metodo | Endpoint                                    | Descrizione                      |
|--------|---------------------------------------------|----------------------------------|
| GET    | `/api/v1/mcp/incidents`                     | Lista incidenti (con filtri)     |
| POST   | `/api/v1/mcp/incidents`                     | Apri un nuovo incidente          |
| PUT    | `/api/v1/mcp/incidents/{id}`                | Aggiorna un incidente            |
| POST   | `/api/v1/mcp/incidents/{id}/timeline`       | Aggiungi entry alla timeline     |
| POST   | `/api/v1/mcp/incidents/{id}/resolve`        | Risolvi un incidente             |
| GET    | `/api/v1/mcp/incidents/{id}/postmortem`     | Genera il postmortem             |

**Parametri di query per GET lista:**
- `status` (opzionale): filtra per stato (es. `open`, `resolved`)
- `severity` (opzionale): filtra per severita' (es. `critical`, `high`, `medium`, `low`)
- `limit` (default: 50): numero massimo di risultati

**DTO di richiesta:**

- `OpenIncidentRequest`: `title`, `severity`, `description`, `affectedSystemsJson`
- `UpdateIncidentRequest`: `status`, `note`
- `TimelineEntryRequest`: `description`, `source`
- `ResolveIncidentRequest`: `resolution`, `rootCause`

**Esempio di utilizzo:**

```bash
# Lista incidenti aperti con severita' critica
curl -X GET "http://localhost:8080/api/v1/mcp/incidents?status=open&severity=critical&limit=10"

# Apri un nuovo incidente
curl -X POST http://localhost:8080/api/v1/mcp/incidents \
  -H "Content-Type: application/json" \
  -d '{"title":"DB timeout","severity":"high","description":"Timeout queries","affectedSystemsJson":"[\"mysql\"]"}'

# Risolvi un incidente
curl -X POST http://localhost:8080/api/v1/mcp/incidents/abc-123/resolve \
  -H "Content-Type: application/json" \
  -d '{"resolution":"Aumentato pool connessioni","rootCause":"Pool esaurito sotto carico"}'
```

### 9.5 `McpTimeController`

Endpoint per il time tracking. Delegano a `TimeTrackingUseCase`.

Base path: `/api/v1/mcp/time`

| Metodo | Endpoint                          | Descrizione                      |
|--------|-----------------------------------|----------------------------------|
| POST   | `/api/v1/mcp/time/start`          | Avvia un timer                   |
| POST   | `/api/v1/mcp/time/stop`           | Ferma il timer corrente          |
| POST   | `/api/v1/mcp/time/log`            | Registra tempo manualmente       |
| GET    | `/api/v1/mcp/time/timesheet`      | Ottieni il timesheet             |

**Parametri di query per GET timesheet:**
- `startDate` (opzionale): data di inizio (formato `YYYY-MM-DD`)
- `endDate` (opzionale): data di fine (formato `YYYY-MM-DD`)
- `userId` (opzionale): filtra per utente

**DTO di richiesta:**

- `StartTimerRequest`: `taskId`, `description`
- `LogTimeRequest`: `taskId`, `durationMinutes`, `description`, `date`

**Esempio di utilizzo:**

```bash
# Avvia timer per un task
curl -X POST http://localhost:8080/api/v1/mcp/time/start \
  -H "Content-Type: application/json" \
  -d '{"taskId":"task-abc","description":"Implementazione feature Y"}'

# Ferma il timer
curl -X POST http://localhost:8080/api/v1/mcp/time/stop

# Registra tempo manualmente
curl -X POST http://localhost:8080/api/v1/mcp/time/log \
  -H "Content-Type: application/json" \
  -d '{"taskId":"task-abc","durationMinutes":90,"description":"Code review","date":"2026-02-13"}'

# Ottieni timesheet ultimo mese
curl -X GET "http://localhost:8080/api/v1/mcp/time/timesheet?startDate=2026-01-13&endDate=2026-02-13"
```

---

## 10. Flusso completo

### Registrazione e connessione a un server

```
1. Client HTTP --> POST /api/v1/mcp/servers
        |
2. McpServerController.register(CreateMcpServerRequestDto)
        |
3. McpServerManagementService.register(McpServerRegistration)
        |
4. McpServerRegistrationRepository.save()  --> DB (mcp_servers)
        |
5. McpClientPort.connect(server)  --> SpringAiMcpClientAdapter
        |                                  |
        |                           new McpClientConnection(server)
        |                                  |
        |                           createClient(server)  [SSE o STDIO]
        |                                  |
        |                           client.initialize()  [handshake MCP]
        |                                  |
        |                           ConcurrentHashMap.put(id, connection)
        |
6. Se connected: aggiorna status=CONNECTED, lastConnectedAt
        |
7. McpServerRegistrationRepository.save()  --> DB aggiornato
        |
8. Risposta --> McpServerDto (con status aggiornato)
```

### Scoperta ed esecuzione tool

```
1. Client HTTP --> GET /api/v1/mcp/tools/servers/{serverId}
        |
2. McpToolController --> McpToolDiscoveryUseCase.listExternalTools(serverId)
        |
3. McpToolOrchestratorService --> McpClientPort.discoverTools(serverId)
        |
4. SpringAiMcpClientAdapter --> connection.getTools()
        |                        client.listTools()
        |                        (JSON-RPC tools/list via MCP SDK)
5. Lista McpExternalTool <-- risposta
        |
6. Client HTTP --> POST /api/v1/mcp/tools/execute
        |                  (passa attraverso McpAccessInterceptor)
        |
7. McpToolController --> McpToolExecutionUseCase.executeTool(request)
        |
8. McpToolOrchestratorService --> McpClientPort.executeTool(serverId, request)
        |
9. SpringAiMcpClientAdapter --> connection.executeTool(name, args)
        |                        client.callTool(new CallToolRequest(...))
        |                        (JSON-RPC tools/call via MCP SDK)
10. McpToolExecutionResult <-- risposta con result, success, executionTimeMs
```

### Scoperta tool locali

```
1. Client HTTP --> GET /api/v1/mcp/tools/local
        |
2. McpToolController --> localToolDiscoveryService.discoverLocalTools()
        |
3. LocalToolDiscoveryService --> ApplicationContext.getBeanDefinitionNames()
        |                        (scansione bean LocalMind*Tools)
        |
4. Per ogni bean: Method.getAnnotation(@Tool)
        |              -> estrai name, description
        |
5. Lista Map<name, description, local=true> <-- cache immutabile
        |
6. McpToolController --> map a McpToolDto
        |
7. Risposta --> Lista di 135 McpToolDto (local=true)
```

---

## 11. Security

### 11.1 `McpAccessInterceptor`

`HandlerInterceptor` che protegge l'endpoint `POST /api/v1/mcp/tools/execute` con controllo
di accesso basato su policy. Si trova in `com.localmind.infrastructure.mcp.security`.

```java
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
        // Filtra solo POST su /mcp/tools/execute
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;
        if (!request.getRequestURI().endsWith("/mcp/tools/execute")) return true;

        // Estrai userId dall'header (default: "anonymous")
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.isBlank()) userId = DEFAULT_USER;

        // Estrai toolName e serverId dal body (richiede CachedBodyHttpServletRequest)
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

        // Verifica accesso tramite AccessPolicyUseCase
        Map<String, Object> checkResult =
            accessPolicyUseCase.checkAccess(userId, serverForPolicy, toolName);

        Boolean allowed = (Boolean) checkResult.get("allowed");
        if (allowed != null && !allowed) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            String errorJson = objectMapper.writeValueAsString(Map.of(
                    "error", "Access denied by policy: " + policyName,
                    "userId", userId,
                    "tool", toolName,
                    "server", serverForPolicy
            ));
            response.getWriter().write(errorJson);
            return false;   // Blocca la richiesta
        }

        return true;        // Accesso consentito
    }
}
```

**Flusso di controllo accesso:**

```
1. POST /api/v1/mcp/tools/execute
        |
2. OncePerRequestFilter (cachedBodyFilter)
        |   wrappa il request in CachedBodyHttpServletRequest
        |
3. McpAccessInterceptor.preHandle()
        |   legge X-User-Id header (default: "anonymous")
        |   legge toolName e serverId dal body cached
        |
4. accessPolicyUseCase.checkAccess(userId, server, tool)
        |
5a. allowed=true  --> prosegui al controller
5b. allowed=false --> 403 Forbidden con JSON errore
5c. nessuna policy corrispondente --> accesso consentito (default allow)
```

### 11.2 `CachedBodyHttpServletRequest`

Wrapper di `HttpServletRequest` che permette la rilettura del body della richiesta.
Necessario perche' il body dello `HttpServletRequest` standard puo' essere letto una sola volta.

```java
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = request.getInputStream().readAllBytes();
    }

    @Override
    public ServletInputStream getInputStream() {
        // Restituisce un nuovo stream dal body cachato
        return new ServletInputStream() { /* ... ByteArrayInputStream wrapper ... */ };
    }

    public byte[] getCachedBody() {
        return cachedBody;
    }
}
```

### 11.3 Registrazione dell'interceptor

L'interceptor e il filtro di caching del body sono registrati in `WebMvcConfig`:

```java
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
        // Crea CachedBodyHttpServletRequest solo per POST su /mcp/tools/execute
        // Ordine: 1 (eseguito prima dell'interceptor)
    }
}
```

**Comportamento default:**
- Nessun header `X-User-Id` --> l'utente e' considerato `anonymous`
- Nessuna policy che corrisponde a userId/server/tool --> accesso consentito
- Policy con `effect=deny` che corrisponde --> 403 Forbidden con dettagli JSON

**Esempio di risposta 403:**

```json
{
    "error": "Access denied by policy: restrict-admin-tools",
    "userId": "user-123",
    "tool": "deleteDatabase",
    "server": "local"
}
```

---

## 12. Dashboard e Insight con dati reali

### 12.1 `DashboardService`

Il `DashboardService` e' un domain service (POJO puro) che implementa `DashboardUseCase`.
Aggrega dati reali da 6 use case per fornire una vista unificata dello stato del progetto.

**Use case iniettati:**

| Use Case                  | Dati forniti                                  |
|---------------------------|-----------------------------------------------|
| `ScrumBoardUseCase`       | Backlog, storie, stato sprint, story points   |
| `AgileMetricsUseCase`     | Metriche agili (velocity, burndown, etc.)     |
| `TimeTrackingUseCase`     | Timesheet, ore tracciate, periodo             |
| `ProjectEconomicsUseCase` | Budget status, percentuale utilizzo           |
| `IncidentManagerUseCase`  | Lista incidenti, conteggio open/resolved      |
| `QualityGateUseCase`      | Quality gates definite, monitoraggio          |

**Metodi principali:**

- `getOverview()`: Aggregazione di sprint, velocity, time tracking e budget. Calcola progresso sprint in base alle storie completate dal backlog reale.
- `getServerStatus(serverName)`: Stato dei 12 server tool locali con filtro opzionale per nome.
- `getRecentActivity(limit)`: Attivita' recenti basate su incidenti reali da `IncidentManagerUseCase`.
- `getProjectSummary(project)`: Sommario completo con sprint, velocity, budget, incidenti e quality, tutti da dati reali.

**Gestione errori con fallback graceful:** Ogni sezione dati e' wrappata in try-catch con fallback a valori `"N/A"` in caso di errore, garantendo che la dashboard non fallisca anche se uno dei servizi sottostanti non e' disponibile.

### 12.2 `InsightEngineService`

Il `InsightEngineService` e' un domain service (POJO puro) che implementa `InsightEngineUseCase`.
Fornisce insight basati su domande in linguaggio naturale, correlazione metriche, spiegazione trend e health dashboard.

**Use case iniettati:**

| Use Case                  | Dati forniti                                  |
|---------------------------|-----------------------------------------------|
| `ScrumBoardUseCase`       | Dati backlog per insight velocity/sprint      |
| `ProjectEconomicsUseCase` | Dati budget per insight economici             |
| `IncidentManagerUseCase`  | Dati incidenti per insight stabilita'         |
| `QualityGateUseCase`      | Dati quality gates per insight qualita'       |
| `InsightRepository`       | Persistenza dei risultati delle query         |

**Metodi principali:**

- `queryInsight(question)`: Analisi basata su parole chiave (velocity, budget, sprint, incident, quality) con dati reali. Restituisce insight testuale, livello di confidence e azioni suggerite. I risultati vengono persistiti via `InsightRepository`.
- `correlateMetrics(metricsJson, period)`: Genera correlazioni tra coppie di metriche con forza e direzione.
- `explainTrend(metric, direction, period)`: Spiega un trend con fattori contribuenti e confidence.
- `healthDashboard()`: Dashboard salute del progetto con scoring per 6 aree (Sprint Progress, Code Quality, CI/CD, Budget, Incident Response, Team Velocity). Calcola `overallHealth` come media pesata degli score individuali con soglie `good/warning/critical`.

**Health scoring:**
- Score >= 75 e nessun warning/critical --> `good`
- Score >= 50 o presenza warning --> `warning`
- Score < 50 o presenza critical --> `critical`

---

> **Navigazione documentazione:**
> - Precedente: [02-server-implementation.md](02-server-implementation.md)
> - Prossimo: [04-configurazione.md](04-configurazione.md)
> - Integrazione agenti: [06-integrazione-agenti.md](06-integrazione-agenti.md)
