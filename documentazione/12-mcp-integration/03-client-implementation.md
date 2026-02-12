# Implementazione MCP Client in LocalMind

**Progetto:** LocalMind - Piattaforma AI Local-First  
**Versione:** 0.1.0  
**Ultimo aggiornamento:** 2026-02-09  
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
|         |                      |                                |
+---------+----------------------+--------------------------------+
          |                      |
+---------v----------------------v---------------------------------+
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
|                   localmind-infrastructure                       |
|  SpringAiMcpClientAdapter    McpServerRepositoryAdapter          |
|  (ConcurrentHashMap)         (JPA -> McpServerEntity)            |
+-------------------------------------------------------------- ---+
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
| **Infra Adapter** | `infrastructure.mcp.adapter`                 | Adapter MCP SDK (Spring AI)       |
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

### 7.1 `SpringAiMcpClientAdapter`

Implementa `McpClientPort` e gestisce le connessioni MCP usando il Spring AI MCP Client SDK.
Si trova in `com.localmind.infrastructure.mcp.adapter`.

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

**Caratteristiche principali:**
- `ConcurrentHashMap<String, McpClientConnection>` per gestione thread-safe delle connessioni
- Classe interna `McpClientConnection` come wrapper del lifecycle della singola connessione
- Attivazione condizionale tramite `@ConditionalOnProperty`
- Logging strutturato per diagnostica connessioni

### Gestione delle connessioni in memoria

```
ConcurrentHashMap<serverId, McpClientConnection>
   |
   +-- "abc-123" -> McpClientConnection(filesystem-server, alive=true)
   +-- "def-456" -> McpClientConnection(database-server, alive=true)
   +-- "ghi-789" -> McpClientConnection(web-scraper, alive=false)
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
| GET    | `/api/v1/mcp/tools/local`               | Tool locali di LocalMind        |
| POST   | `/api/v1/mcp/tools/execute`             | Esegui un tool                  |

**DTO esecuzione (`McpToolExecutionRequestDto`):**

```java
public class McpToolExecutionRequestDto {
    @NotBlank private String toolName;      // Nome del tool
    @NotBlank private String serverId;      // ID del server
    private Map<String, Object> arguments;  // Argomenti del tool
}
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
        |                        (JSON-RPC tools/list)
5. Lista McpExternalTool <-- risposta
        |
6. Client HTTP --> POST /api/v1/mcp/tools/execute
        |
7. McpToolController --> McpToolExecutionUseCase.executeTool(request)
        |
8. McpToolOrchestratorService --> McpClientPort.executeTool(serverId, request)
        |
9. SpringAiMcpClientAdapter --> connection.executeTool(name, args)
        |                        (JSON-RPC tools/call)
10. McpToolExecutionResult <-- risposta con result, success, executionTimeMs
```

---

> **Navigazione documentazione:**
> - Precedente: [02-server-implementation.md](02-server-implementation.md)
> - Prossimo: [04-configurazione.md](04-configurazione.md)
> - Integrazione agenti: [06-integrazione-agenti.md](06-integrazione-agenti.md)
