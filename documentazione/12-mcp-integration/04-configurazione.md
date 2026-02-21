# Configurazione MCP in LocalMind

**Progetto:** LocalMind - Piattaforma AI Local-First  
**Versione:** 0.1.0  
**Ultimo aggiornamento:** 2026-02-09  
**Moduli di riferimento:** localmind-infrastructure, localmind-app

---

## Indice

1. [Dipendenze Maven](#1-dipendenze-maven)
2. [Configurazione application YAML](#2-configurazione-application-yaml)
3. [Properties disponibili](#3-properties-disponibili)
4. [Migrazione Flyway V6](#4-migrazione-flyway-v6)
5. [Bean condizionali](#5-bean-condizionali)
6. [McpConfiguration.java](#6-mcpconfigurationjava)
7. [Configurazione per profilo](#7-configurazione-per-profilo)

---

## 1. Dipendenze Maven

Le dipendenze MCP sono dichiarate nel modulo `localmind-infrastructure/pom.xml`.
Le versioni sono gestite dal BOM di Spring AI nel parent POM.

### Parent POM (localmind-backend/pom.xml)

```xml
<properties>
    <spring-ai.version>1.0.0</spring-ai.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Infrastructure POM (localmind-infrastructure/pom.xml)

```xml
<!-- MCP Server (WebMVC) - LocalMind come MCP Server -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>

<!-- MCP Client - LocalMind come MCP Client -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
```

### Dettaglio degli starter

| Starter                               | Funzionalita'                                     |
|---------------------------------------|---------------------------------------------------|
| `spring-ai-starter-mcp-server-webmvc` | Server MCP su WebMVC (SSE + HTTP endpoint)        |
| `spring-ai-starter-mcp-client`        | Client MCP con supporto STDIO e SSE               |

**Nota:** Lo starter `mcp-server-webmvc` e' specifico per applicazioni Spring MVC (non WebFlux).
LocalMind usa Spring MVC, quindi questo e' lo starter corretto. Per applicazioni reactive,
esiste `spring-ai-starter-mcp-server-webflux`.

---

## 2. Configurazione application YAML

### application-dev.yml (profilo sviluppo)

Questo e' il file di configurazione principale per l'ambiente di sviluppo, situato in
`localmind-app/src/main/resources/application-dev.yml`:

```yaml
spring:
  ai:
    mcp:
      server:
        name: localmind          # Nome identificativo del server MCP
        version: 0.1.0           # Versione del server MCP

# Configurazione LocalMind specifica per MCP
localmind:
  mcp:
    server:
      enabled: true              # Abilita il server MCP (espone tool/risorse)
    client:
      enabled: true              # Abilita il client MCP (connessione a server esterni)

# Logging per debug MCP
logging:
  level:
    com.localmind: DEBUG
    org.springframework.ai: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Struttura della configurazione

```
spring.ai.mcp.server.*        -> Configurazione Spring AI MCP Server (nome, versione)
localmind.mcp.server.enabled   -> Flag ON/OFF per il server MCP di LocalMind
localmind.mcp.client.enabled   -> Flag ON/OFF per il client MCP di LocalMind
```

---

## 3. Properties disponibili

### Properties Spring AI MCP Server

| Property                       | Tipo   | Default     | Descrizione                          |
|--------------------------------|--------|-------------|--------------------------------------|
| `spring.ai.mcp.server.name`    | String | `spring-ai` | Nome del server MCP                  |
| `spring.ai.mcp.server.version` | String | `1.0.0`     | Versione del server MCP              |

### Properties LocalMind MCP

| Property                        | Tipo    | Default | Descrizione                               |
|---------------------------------|---------|---------|-------------------------------------------|
| `localmind.mcp.server.enabled`  | boolean | `true`  | Abilita l'esposizione di tool MCP         |
| `localmind.mcp.client.enabled`  | boolean | `false` | Abilita la connessione a server esterni   |

**Nota importante sui default:**
- Il **server** MCP e' attivo di default (`matchIfMissing = true`) perche' e' una funzionalita'
  core che non richiede configurazione aggiuntiva.
- Il **client** MCP e' disattivato di default perche' richiede configurazione esplicita
  (registrazione dei server a cui connettersi).

### Properties di logging per diagnostica

| Property                                       | Valore consigliato | Descrizione                |
|------------------------------------------------|--------------------|----------------------------|
| `logging.level.com.localmind`                  | `DEBUG`            | Log applicativi LocalMind  |
| `logging.level.org.springframework.ai`         | `DEBUG`            | Log Spring AI / MCP        |
| `logging.level.io.modelcontextprotocol`        | `DEBUG`            | Log MCP SDK Java           |

---

## 4. Migrazione Flyway V6

La tabella per la persistenza dei server MCP e' creata dalla migrazione Flyway
`V6__create_mcp_servers_table.sql`, situata in:

```
localmind-app/src/main/resources/db/migration/V6__create_mcp_servers_table.sql
```

### Script SQL completo

```sql
-- MCP Server registrations
CREATE TABLE mcp_servers (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    type VARCHAR(50) NOT NULL CHECK (type IN ('STDIO', 'SSE')),
    status VARCHAR(50) NOT NULL CHECK (status IN ('CONNECTED', 'DISCONNECTED', 'ERROR', 'CONNECTING')),
    command VARCHAR(500),
    args TEXT,
    url VARCHAR(500),
    timeout_seconds INTEGER DEFAULT 30,
    auto_reconnect BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    last_connected_at TIMESTAMP,
    CONSTRAINT chk_stdio_command CHECK (type != 'STDIO' OR command IS NOT NULL),
    CONSTRAINT chk_sse_url CHECK (type != 'SSE' OR url IS NOT NULL)
);

CREATE INDEX idx_mcp_servers_status ON mcp_servers(status);
CREATE INDEX idx_mcp_servers_type ON mcp_servers(type);
```

### Schema dettagliato della tabella

| Colonna             | Tipo          | Nullable | Default | Descrizione                      |
|---------------------|---------------|----------|---------|----------------------------------|
| `id`                | VARCHAR(255)  | NO       | -       | UUID generato dall'applicazione  |
| `name`              | VARCHAR(255)  | NO       | -       | Nome leggibile del server        |
| `description`       | VARCHAR(1000) | SI'      | NULL    | Descrizione opzionale            |
| `type`              | VARCHAR(50)   | NO       | -       | 'STDIO' o 'SSE'                  |
| `status`            | VARCHAR(50)   | NO       | -       | Stato della connessione          |
| `command`           | VARCHAR(500)  | SI'      | NULL    | Comando per STDIO                |
| `args`              | TEXT          | SI'      | NULL    | Argomenti (comma-separated)      |
| `url`               | VARCHAR(500)  | SI'      | NULL    | URL per SSE                      |
| `timeout_seconds`   | INTEGER       | SI'      | 30      | Timeout di connessione           |
| `auto_reconnect`    | BOOLEAN       | SI'      | true    | Riconnessione automatica         |
| `created_at`        | TIMESTAMP     | NO       | -       | Data di registrazione            |
| `last_connected_at` | TIMESTAMP     | SI'      | NULL    | Ultima connessione riuscita      |

### Vincoli (Constraints)

| Constraint          | Tipo  | Regola                                                      |
|---------------------|-------|-------------------------------------------------------------|
| `PRIMARY KEY`       | PK    | `id` e' chiave primaria                                     |
| `type CHECK`        | CHECK | `type` deve essere 'STDIO' o 'SSE'                          |
| `status CHECK`      | CHECK | `status` deve essere uno dei 4 valori validi                |
| `chk_stdio_command` | CHECK | Se `type='STDIO'`, allora `command` non puo' essere NULL    |
| `chk_sse_url`       | CHECK | Se `type='SSE'`, allora `url` non puo' essere NULL          |

### Indici

| Indice                    | Colonna   | Motivazione                                |
|---------------------------|-----------|--------------------------------------------|
| `idx_mcp_servers_status`  | `status`  | Query frequenti per stato (es. CONNECTED)  |
| `idx_mcp_servers_type`    | `type`    | Filtraggio per tipo di trasporto           |

---

## 5. Bean condizionali

L'attivazione dei componenti MCP e' controllata tramite `@ConditionalOnProperty`:

### Componenti Server MCP

Questi bean sono attivi quando `localmind.mcp.server.enabled=true` (default: attivo):

```java
@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled",
                       havingValue = "true",
                       matchIfMissing = true)    // <-- Attivo per default
public class LocalMindMcpTools { ... }

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled",
                       havingValue = "true",
                       matchIfMissing = true)
public class LocalMindMcpResources { ... }

@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled",
                       havingValue = "true",
                       matchIfMissing = true)
public class LocalMindMcpPrompts { ... }
```

### Componenti Client MCP

Questi bean sono attivi **solo** quando `localmind.mcp.client.enabled=true`:

```java
@Component
@ConditionalOnProperty(name = "localmind.mcp.client.enabled",
                       havingValue = "true")    // <-- matchIfMissing NON presente
public class SpringAiMcpClientAdapter implements McpClientPort { ... }
```

### Matrice di attivazione

| Property                        | Valore | LocalMindMcpTools | SpringAiMcpClientAdapter | McpServerManagementService |
|---------------------------------|--------|-------------------|--------------------------|----------------------------|
| `server.enabled` non impostata  | (true) | ATTIVO            | -                        | -                          |
| `server.enabled=true`           | true   | ATTIVO            | -                        | -                          |
| `server.enabled=false`          | false  | DISATTIVO         | -                        | -                          |
| `client.enabled` non impostata  | -      | -                 | DISATTIVO                | DISATTIVO                  |
| `client.enabled=true`           | true   | -                 | ATTIVO                   | ATTIVO                     |
| `client.enabled=false`          | false  | -                 | DISATTIVO                | DISATTIVO                  |

---

## 6. McpConfiguration.java

La classe di configurazione centrale per i bean MCP si trova in
`com.localmind.infrastructure.mcp.config.McpConfiguration`:

```java
@Configuration
public class McpConfiguration {

    @Bean
    @ConditionalOnProperty(name = "localmind.mcp.client.enabled", havingValue = "true")
    public McpServerManagementService mcpServerManagementService(
            McpServerRegistrationRepository repository,
            McpClientPort clientPort) {
        return new McpServerManagementService(repository, clientPort);
    }

    @Bean
    @ConditionalOnProperty(name = "localmind.mcp.client.enabled", havingValue = "true")
    public McpToolOrchestratorService mcpToolOrchestratorService(
            McpServerRegistrationRepository repository,
            McpClientPort clientPort) {
        return new McpToolOrchestratorService(repository, clientPort);
    }
}
```

**Perche' i domain service sono definiti in `McpConfiguration` anziche' con `@Service`?**

I domain service di LocalMind sono **POJO puri** senza annotazioni Spring. Questo e'
una scelta architetturale dell'architettura esagonale: il dominio non deve dipendere
dal framework. La creazione dei bean e' delegata al layer di configurazione nell'infrastruttura.

### Grafo delle dipendenze dei bean

```
McpConfiguration
    |
    +-- mcpServerManagementService
    |       |-- McpServerRegistrationRepository (impl: McpServerRepositoryAdapter)
    |       +-- McpClientPort (impl: SpringAiMcpClientAdapter)
    |
    +-- mcpToolOrchestratorService
            |-- McpServerRegistrationRepository (impl: McpServerRepositoryAdapter)
            +-- McpClientPort (impl: SpringAiMcpClientAdapter)
```

---

## 7. Configurazione per profilo

### Profilo `dev` (sviluppo locale)

```yaml
# application-dev.yml
spring:
  ai:
    mcp:
      server:
        name: localmind
        version: 0.1.0

localmind:
  mcp:
    server:
      enabled: true
    client:
      enabled: true

logging:
  level:
    com.localmind: DEBUG
    org.springframework.ai: DEBUG
    io.modelcontextprotocol: DEBUG
```

### Profilo `prod` (produzione)

Configurazione consigliata per ambienti di produzione:

```yaml
# application-prod.yml (suggerito)
spring:
  ai:
    mcp:
      server:
        name: localmind
        version: 0.1.0

localmind:
  mcp:
    server:
      enabled: true       # Server MCP attivo (esponi tool a client esterni)
    client:
      enabled: true        # Client MCP attivo (connetti a server esterni)

logging:
  level:
    com.localmind: INFO
    org.springframework.ai: WARN
    io.modelcontextprotocol: WARN
```

### Disabilitazione completa MCP

Per disabilitare completamente MCP (utile in ambienti con requisiti di sicurezza stringenti):

```yaml
localmind:
  mcp:
    server:
      enabled: false      # Nessun tool esposto via MCP
    client:
      enabled: false      # Nessuna connessione a server esterni
```

### Configurazione con variabili d'ambiente

Tutte le property possono essere sovrascritte via variabili d'ambiente:

```bash
# Abilita/disabilita MCP
export LOCALMIND_MCP_SERVER_ENABLED=true
export LOCALMIND_MCP_CLIENT_ENABLED=true

# Nome e versione server MCP
export SPRING_AI_MCP_SERVER_NAME=localmind
export SPRING_AI_MCP_SERVER_VERSION=0.1.0
```

---

> **Navigazione documentazione:**
> - Precedente: [03-client-implementation.md](03-client-implementation.md)
> - Prossimo: [05-esempi-utilizzo.md](05-esempi-utilizzo.md)
> - Troubleshooting: [07-troubleshooting.md](07-troubleshooting.md)
