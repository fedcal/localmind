# MCP Configuration in LocalMind

**Project:** LocalMind - Local-First AI Platform
**Version:** 0.1.0
**Last updated:** 2026-02-09
**Reference modules:** localmind-infrastructure, localmind-app

---

## Table of Contents

1. [Maven dependencies](#1-maven-dependencies)
2. [Application YAML configuration](#2-application-yaml-configuration)
3. [Available properties](#3-available-properties)
4. [Flyway V6 migration](#4-flyway-v6-migration)
5. [Conditional beans](#5-conditional-beans)
6. [McpConfiguration.java](#6-mcpconfigurationjava)
7. [Per-profile configuration](#7-per-profile-configuration)

---

## 1. Maven dependencies

MCP dependencies are declared in the `localmind-infrastructure/pom.xml` module.
Versions are managed by the Spring AI BOM in the parent POM.

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
<!-- MCP Server (WebMVC) - LocalMind as MCP Server -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>

<!-- MCP Client - LocalMind as MCP Client -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
```

### Starter details

| Starter                               | Functionality                                      |
|---------------------------------------|---------------------------------------------------|
| `spring-ai-starter-mcp-server-webmvc`| MCP Server on WebMVC (SSE + HTTP endpoint)         |
| `spring-ai-starter-mcp-client`       | MCP Client with STDIO and SSE support              |

**Note:** The `mcp-server-webmvc` starter is specific to Spring MVC applications (not WebFlux).
LocalMind uses Spring MVC, so this is the correct starter. For reactive applications,
`spring-ai-starter-mcp-server-webflux` exists.

---

## 2. Application YAML configuration

### application-dev.yml (development profile)

This is the main configuration file for the development environment, located in
`localmind-app/src/main/resources/application-dev.yml`:

```yaml
spring:
  ai:
    mcp:
      server:
        name: localmind          # MCP server identifier name
        version: 0.1.0           # MCP server version

# LocalMind-specific MCP configuration
localmind:
  mcp:
    server:
      enabled: true              # Enable MCP server (expose tools/resources)
    client:
      enabled: true              # Enable MCP client (connect to external servers)

# Logging for MCP debug
logging:
  level:
    com.localmind: DEBUG
    org.springframework.ai: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Configuration structure

```
spring.ai.mcp.server.*        -> Spring AI MCP Server configuration (name, version)
localmind.mcp.server.enabled   -> ON/OFF flag for the LocalMind MCP server
localmind.mcp.client.enabled   -> ON/OFF flag for the LocalMind MCP client
```

---

## 3. Available properties

### Spring AI MCP Server properties

| Property                       | Type   | Default     | Description                          |
|--------------------------------|--------|-------------|--------------------------------------|
| `spring.ai.mcp.server.name`   | String | `spring-ai` | MCP server name                      |
| `spring.ai.mcp.server.version`| String | `1.0.0`     | MCP server version                   |

### LocalMind MCP properties

| Property                        | Type    | Default | Description                               |
|---------------------------------|---------|---------|-------------------------------------------|
| `localmind.mcp.server.enabled`  | boolean | `true`  | Enable MCP tool exposure                  |
| `localmind.mcp.client.enabled`  | boolean | `false` | Enable connection to external servers     |

**Important note on defaults:**
- The MCP **server** is active by default (`matchIfMissing = true`) because it is a core
  feature that does not require additional configuration.
- The MCP **client** is disabled by default because it requires explicit configuration
  (registration of servers to connect to).

### Logging properties for diagnostics

| Property                                       | Recommended value | Description                |
|------------------------------------------------|-------------------|----------------------------|
| `logging.level.com.localmind`                  | `DEBUG`           | LocalMind application logs |
| `logging.level.org.springframework.ai`         | `DEBUG`           | Spring AI / MCP logs       |
| `logging.level.io.modelcontextprotocol`        | `DEBUG`           | MCP SDK Java logs          |

---

## 4. Flyway V6 migration

The table for MCP server persistence is created by the Flyway migration
`V6__create_mcp_servers_table.sql`, located in:

```
localmind-app/src/main/resources/db/migration/V6__create_mcp_servers_table.sql
```

### Complete SQL script

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

### Detailed table schema

| Column              | Type          | Nullable | Default | Description                      |
|---------------------|---------------|----------|---------|----------------------------------|
| `id`                | VARCHAR(255)  | NO       | -       | Application-generated UUID       |
| `name`              | VARCHAR(255)  | NO       | -       | Human-readable server name       |
| `description`       | VARCHAR(1000) | YES      | NULL    | Optional description             |
| `type`              | VARCHAR(50)   | NO       | -       | 'STDIO' or 'SSE'                |
| `status`            | VARCHAR(50)   | NO       | -       | Connection status                |
| `command`           | VARCHAR(500)  | YES      | NULL    | Command for STDIO                |
| `args`              | TEXT          | YES      | NULL    | Arguments (comma-separated)      |
| `url`               | VARCHAR(500)  | YES      | NULL    | URL for SSE                      |
| `timeout_seconds`   | INTEGER       | YES      | 30      | Connection timeout               |
| `auto_reconnect`    | BOOLEAN       | YES      | true    | Automatic reconnection           |
| `created_at`        | TIMESTAMP     | NO       | -       | Registration date                |
| `last_connected_at` | TIMESTAMP     | YES      | NULL    | Last successful connection       |

### Constraints

| Constraint          | Type  | Rule                                                      |
|---------------------|-------|-----------------------------------------------------------|
| `PRIMARY KEY`       | PK    | `id` is the primary key                                   |
| `type CHECK`        | CHECK | `type` must be 'STDIO' or 'SSE'                          |
| `status CHECK`      | CHECK | `status` must be one of the 4 valid values               |
| `chk_stdio_command` | CHECK | If `type='STDIO'`, then `command` cannot be NULL          |
| `chk_sse_url`       | CHECK | If `type='SSE'`, then `url` cannot be NULL               |

### Indexes

| Index                     | Column    | Motivation                                 |
|---------------------------|-----------|--------------------------------------------|
| `idx_mcp_servers_status`  | `status`  | Frequent queries by status (e.g. CONNECTED)|
| `idx_mcp_servers_type`    | `type`    | Filtering by transport type                |

---

## 5. Conditional beans

MCP component activation is controlled via `@ConditionalOnProperty`:

### MCP Server components

These beans are active when `localmind.mcp.server.enabled=true` (default: active):

```java
@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled",
                       havingValue = "true",
                       matchIfMissing = true)    // <-- Active by default
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

### MCP Client components

These beans are active **only** when `localmind.mcp.client.enabled=true`:

```java
@Component
@ConditionalOnProperty(name = "localmind.mcp.client.enabled",
                       havingValue = "true")    // <-- matchIfMissing NOT present
public class SpringAiMcpClientAdapter implements McpClientPort { ... }
```

### Activation matrix

| Property                        | Value  | LocalMindMcpTools | SpringAiMcpClientAdapter | McpServerManagementService |
|---------------------------------|--------|-------------------|--------------------------|---------------------------|
| `server.enabled` not set        | (true) | ACTIVE            | -                        | -                         |
| `server.enabled=true`           | true   | ACTIVE            | -                        | -                         |
| `server.enabled=false`          | false  | INACTIVE          | -                        | -                         |
| `client.enabled` not set        | -      | -                 | INACTIVE                 | INACTIVE                  |
| `client.enabled=true`           | true   | -                 | ACTIVE                   | ACTIVE                    |
| `client.enabled=false`          | false  | -                 | INACTIVE                 | INACTIVE                  |

---

## 6. McpConfiguration.java

The central configuration class for MCP beans is located in
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

**Why are domain services defined in `McpConfiguration` instead of with `@Service`?**

LocalMind domain services are **pure POJOs** without Spring annotations. This is
an architectural choice of the hexagonal architecture: the domain must not depend
on the framework. Bean creation is delegated to the configuration layer in infrastructure.

### Bean dependency graph

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

## 7. Per-profile configuration

### `dev` profile (local development)

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

### `prod` profile (production)

Recommended configuration for production environments:

```yaml
# application-prod.yml (suggested)
spring:
  ai:
    mcp:
      server:
        name: localmind
        version: 0.1.0

localmind:
  mcp:
    server:
      enabled: true       # MCP server active (expose tools to external clients)
    client:
      enabled: true        # MCP client active (connect to external servers)

logging:
  level:
    com.localmind: INFO
    org.springframework.ai: WARN
    io.modelcontextprotocol: WARN
```

### Complete MCP disablement

To completely disable MCP (useful in environments with strict security requirements):

```yaml
localmind:
  mcp:
    server:
      enabled: false      # No tools exposed via MCP
    client:
      enabled: false      # No connections to external servers
```

### Configuration with environment variables

All properties can be overridden via environment variables:

```bash
# Enable/disable MCP
export LOCALMIND_MCP_SERVER_ENABLED=true
export LOCALMIND_MCP_CLIENT_ENABLED=true

# MCP server name and version
export SPRING_AI_MCP_SERVER_NAME=localmind
export SPRING_AI_MCP_SERVER_VERSION=0.1.0
```

---

> **Documentation navigation:**
> - Previous: [03-client-implementation.md](03-client-implementation.md)
> - Next: [05-usage-examples.md](05-usage-examples.md)
> - Troubleshooting: [07-troubleshooting.md](07-troubleshooting.md)
