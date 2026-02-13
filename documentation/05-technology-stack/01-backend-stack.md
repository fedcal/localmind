# Backend Technology Stack

**Project:** LocalMind
**Version:** 0.1.0
**Date:** 2026-02-09
**Module:** localmind-backend

---

## Table of Contents

1. [Overview](#1-overview)
2. [Java 17](#2-java-17)
3. [Spring Boot 3.4.2](#3-spring-boot-342)
4. [Spring AI 1.0.0](#4-spring-ai-100)
5. [Spring Data JPA / Hibernate](#5-spring-data-jpa--hibernate)
6. [Spring Security](#6-spring-security)
7. [Spring Batch](#7-spring-batch)
8. [Spring Retry](#8-spring-retry)
9. [MySQL 8.0](#9-mysql-80)
10. [Qdrant](#10-qdrant)
11. [Apache Tika 2.9.2](#11-apache-tika-292)
12. [Flyway](#12-flyway)
13. [MapStruct 1.6.3](#13-mapstruct-163)
14. [Lombok 1.18.36](#14-lombok-11836)
15. [Maven](#15-maven)
16. [Dependency Summary Table](#16-dependency-summary-table)

---

## 1. Overview

The LocalMind backend is built on a Maven multi-module architecture based on Spring Boot 3.4.2 with Java 17. The project adopts a Hexagonal Architecture with a clear separation between the domain, infrastructure, and API layers. Dependencies are centrally managed in the parent POM via `dependencyManagement` and the BOM (Bill of Materials) for Spring AI.

The multi-module structure includes:

| Module                    | Description                                                                |
|---------------------------|----------------------------------------------------------------------------|
| `localmind-domain`        | Domain entities, ports, and business logic (zero framework dependencies)   |
| `localmind-infrastructure`| Adapters: database, LLM clients, vector store, filesystem                  |
| `localmind-api`           | REST controllers, DTOs, and mappers                                        |
| `localmind-batch`         | Spring Batch jobs for document processing                                  |
| `localmind-app`           | Executable application module (aggregator)                                 |

---

## 2. Java 17

| Property     | Value                              |
|--------------|------------------------------------|
| **Name**     | Java Development Kit (JDK)         |
| **Version**  | 17 (LTS)                           |
| **Purpose**  | Programming language and runtime   |

### Rationale

Java 17 was selected as a Long-Term Support (LTS) version, ensuring stability and long-term support. Key features used in the project include:

- **Records**: used for immutable domain models and DTOs where appropriate.
- **Sealed Classes**: to define closed and type-safe hierarchies in the domain.
- **Pattern Matching for instanceof**: simplifies type-checking and casting code in the service layer.
- **Text Blocks**: for inline SQL queries and multi-line text templates.
- **Enhanced Switch Expressions**: for concise conditional logic in LLM provider handling.

### Alternatives Considered

| Alternative | Reason for Rejection                                                                                                                                             |
|-------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Java 21     | Advanced features (virtual threads) not needed for v0.1.0; compatibility with Spring AI 1.0.0 not fully verified at the time of development                      |
| Java 11     | Lacks records, sealed classes, and other modern features essential for domain design                                                                             |

---

## 3. Spring Boot 3.4.2

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | Spring Boot                                    |
| **Version** | 3.4.2                                          |
| **Purpose** | Application framework with auto-configuration  |

### Rationale

Spring Boot 3.4.2 is the project's parent POM (`spring-boot-starter-parent`). The main reasons for this choice are:

- **Auto-configuration**: automatic configuration of datasource, JPA, security, batch, and AI clients via starters.
- **Mature ecosystem**: native integration with Spring Data, Spring Security, Spring Batch, Spring AI.
- **Production-ready**: Actuator for metrics and health checks, profile management (dev/prod), native container support.
- **Convention over configuration**: drastic reduction of configuration boilerplate.
- **Jakarta EE 10**: based on Jakarta EE, with the `jakarta.*` namespace for servlet, persistence, and validation.

### Alternatives Considered

| Alternative  | Reason for Rejection                                                                                                             |
|--------------|----------------------------------------------------------------------------------------------------------------------------------|
| Quarkus      | Excellent for cloud-native microservices and GraalVM, but AI ecosystem less mature; Spring AI integration not available          |
| Micronaut    | Comparable performance, but smaller community and less extensive documentation; no Spring AI integration                         |

---

## 4. Spring AI 1.0.0

| Property    | Value                                                |
|-------------|------------------------------------------------------|
| **Name**    | Spring AI                                            |
| **Version** | 1.0.0 (managed via `spring-ai-bom` BOM)              |
| **Purpose** | Unified API for LLM providers and vector stores      |

### Rationale

Spring AI provides a unified abstraction for interacting with different LLM providers and vector stores, with native auto-configuration for Spring Boot. The starters used in the project are:

- `spring-ai-starter-model-ollama` - Client for Ollama (local LLM model execution)
- `spring-ai-starter-model-openai` - Client for OpenAI (GPT-4o)
- `spring-ai-starter-model-anthropic` - Client for Anthropic (Claude)
- `spring-ai-starter-vector-store-qdrant` - Client for Qdrant (vector database)

### Architectural Advantages

- **Uniform `ChatClient` API**: every provider exposes the same interface, making the system LLM-provider agnostic.
- **Per-provider auto-configuration**: each starter automatically configures the client based on properties in `application.yml`.
- **Native embedding support**: vector embedding generation through the same API.
- **Vector store integration**: CRUD operations on vectorized documents via the `VectorStore` interface.

### Alternatives Considered

| Alternative   | Reason for Rejection                                                                                                                                           |
|---------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| LangChain4j   | Valid and mature library, but lacks native integration with the Spring stack (auto-configuration, starters, profiles); requires more manual configuration code |

---

## 5. Spring Data JPA / Hibernate

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | Spring Data JPA with Hibernate ORM             |
| **Version** | Managed by Spring Boot 3.4.2 (Hibernate 6.x)   |
| **Purpose** | Object-Relational Mapping and data access      |

### Rationale

Spring Data JPA is the de facto standard for data access in Spring applications. In the project, it is used in the `localmind-infrastructure` module via the `spring-boot-starter-data-jpa` starter.

Features used:
- **Repository interface**: `JpaRepository<Entity, UUID>` for automatic CRUD operations.
- **Hibernate 6.x**: ORM with native support for Jakarta EE 10 and MySQL JSON mapping.
- **`@JdbcTypeCode(SqlTypes.JSON)`**: for native mapping of MySQL JSON fields to `Map<String, Object>`.
- **Lifecycle callbacks**: `@PrePersist`, `@PreUpdate` for automatic timestamp management.
- **DDL validation**: `ddl-auto: validate` combined with Flyway to ensure schema-entity consistency.

### Alternatives Considered

| Alternative | Reason for Rejection                                                                                                             |
|-------------|----------------------------------------------------------------------------------------------------------------------------------|
| jOOQ        | Excellent for complex and type-safe SQL queries; not necessary for the query complexity level of LocalMind v0.1.0                |
| MyBatis     | Good SQL control, but requires more boilerplate code compared to Spring Data JPA for standard CRUD operations                    |

---

## 6. Spring Security

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | Spring Security                                |
| **Version** | Managed by Spring Boot 3.4.2                   |
| **Purpose** | Authentication, authorization, CORS            |

### Usage in the Project

In version 0.1.0, Spring Security is configured in permissive mode for local development:

- **CORS**: enabled for `http://localhost:4200` (Angular frontend).
- **CSRF**: disabled for stateless REST APIs.
- **Authentication**: no authentication required (all routes are `permitAll`).
- **Session management**: stateless (`SessionCreationPolicy.STATELESS`).

> **Note**: full authentication (JWT, OAuth2) is planned for future versions.

---

## 7. Spring Batch

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | Spring Batch                                   |
| **Version** | Managed by Spring Boot 3.4.2                   |
| **Purpose** | Document processing pipeline and scheduling    |

### Usage in the Project

The `localmind-batch` module uses Spring Batch for the document processing pipeline:

- **Document Processing Pipeline**: read documents -> text extraction (Tika) -> chunking -> embedding generation -> save to Qdrant.
- **Scheduling**: periodic scanning of configured folders via cron expression (`0 */15 * * * *`).
- **Configuration**: `spring.batch.job.enabled=false` by default; jobs are triggered programmatically or by triggers.

### Alternatives Considered

| Alternative       | Reason for Rejection                                                                                   |
|-------------------|--------------------------------------------------------------------------------------------------------|
| Custom Scheduler  | Less robust; Spring Batch offers built-in transaction management, retry, skip, restart, and monitoring |

---

## 8. Spring Retry

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | Spring Retry                                   |
| **Version** | Managed by Spring Boot 3.4.2                   |
| **Purpose** | Retry logic for LLM calls                      |

### Usage in the Project

Spring Retry is used in the `localmind-infrastructure` module to handle transient errors in LLM provider calls:

- **Max attempts**: 3 attempts (configurable via `localmind.llm.retry.max-attempts`).
- **Backoff**: 1000ms between attempts (configurable via `localmind.llm.retry.backoff-ms`).
- **Fallback**: support for automatic fallback between providers (`OLLAMA -> OPENAI -> ANTHROPIC -> GOOGLE`).

---

## 9. MySQL 8.0

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | MySQL                                          |
| **Version** | 8.0                                            |
| **Purpose** | Primary relational database                    |

### Rationale

MySQL 8.0 is the primary relational database of the system. It can be installed natively on the host or run via Docker (`mysql:8.0`); the setup script automatically detects the available mode.

Features used:
- **JSON**: for the `metadata` field in the `documents` table, allowing flexible storage of heterogeneous metadata.
- **`UUID()`**: database-side UUID generation for primary keys.
- **B-tree indexes**: on high-cardinality columns for performant queries.
- **TIMESTAMP**: precise temporal management for audit trails.

### Configuration

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/localmind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: localmind
    password: localmind
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### Alternatives Considered

| Alternative          | Reason for Rejection                                                                                                                                            |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| PostgreSQL           | Advanced features (JSONB, gen_random_uuid) not necessary for the complexity level of LocalMind v0.1.0; MySQL is more widespread and simpler to install natively |
| H2                   | Suitable only for testing; not adequate for production with JSON and UUID                                                                                       |

---

## 10. Qdrant

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | Qdrant                                         |
| **Version** | latest                                         |
| **Purpose** | Vector database for semantic search            |

### Rationale

Qdrant is the vector database chosen for storing and searching vector embeddings generated from documents.

Key characteristics:
- **gRPC support**: high-performance communication via port 6334.
- **Dedicated Spring AI starter**: `spring-ai-starter-vector-store-qdrant` with auto-configuration.
- **REST API**: REST interface on port 6333 for management and debugging operations.
- **Collection management**: automatic collection management via Spring AI.
- **Performance**: optimized for nearest-neighbor search on large volumes of vectors.

### Configuration

```yaml
spring:
  ai:
    vectorstore:
      qdrant:
        host: localhost
        port: 6334
        collection-name: localmind-documents
```

### Alternatives Considered

| Alternative | Reason for Rejection                                                                        |
|-------------|---------------------------------------------------------------------------------------------|
| Chroma      | Smaller community; no dedicated Spring AI starter available at the time of development      |
| Milvus      | More complex to configure and operate; overhead for a single-node project                   |
| Weaviate    | Advanced features not needed; Spring AI starter not available                               |
| Pinecone    | Cloud-only service; incompatible with LocalMind's local-first approach                      |

---

## 11. Apache Tika 2.9.2

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | Apache Tika                                    |
| **Version** | 2.9.2                                          |
| **Purpose** | Multi-format text extraction                   |

### Rationale

Apache Tika is the reference library for extracting text from heterogeneous formats. Two artifacts are used in the project:

- `tika-core` (2.9.2): Base API for MIME type detection and parsing.
- `tika-parsers-standard-package` (2.9.2): Parsers for PDF, DOCX, TXT, EML, and other formats.

### Supported Formats

| Format  | MIME Type                                                                 |
|---------|---------------------------------------------------------------------------|
| PDF     | `application/pdf`                                                         |
| DOCX    | `application/vnd.openxmlformats-officedocument.wordprocessingml.document` |
| TXT     | `text/plain`                                                              |
| EML     | `message/rfc822`                                                          |

### Alternatives Considered

| Alternative | Reason for Rejection                                                                         |
|-------------|----------------------------------------------------------------------------------------------|
| PDFBox      | Supports only PDF; Tika offers a unified API for multiple formats                            |
| iText       | Commercial license (AGPL); oriented towards PDF generation rather than text extraction       |

---

## 12. Flyway

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | Flyway                                         |
| **Version** | Managed by Spring Boot 3.4.2                   |
| **Purpose** | Versioned database schema migration            |

### Usage in the Project

Flyway manages MySQL database migrations in a versioned and repeatable manner:

- **Artifacts**: `flyway-core`, `flyway-mysql`.
- **Migration directory**: `localmind-app/src/main/resources/db/migration/`.
- **Naming convention**: `V{number}__{description}.sql`.
- **Integration**: automatically activated at Spring Boot application startup.

### Configuration

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
  jpa:
    hibernate:
      ddl-auto: validate
```

> **Note**: `ddl-auto: validate` ensures that Hibernate verifies consistency between JPA entities and the database schema without ever modifying the schema autonomously.

### Alternatives Considered

| Alternative | Reason for Rejection                                                                                                 |
|-------------|----------------------------------------------------------------------------------------------------------------------|
| Liquibase   | More flexible (XML/YAML/JSON format), but greater complexity; Flyway is sufficient for pure SQL migrations           |

---

## 13. MapStruct 1.6.3

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | MapStruct                                      |
| **Version** | 1.6.3                                          |
| **Purpose** | Compile-time DTO mapping                       |

### Rationale

MapStruct generates mapping code between DTOs and domain models at compile time, ensuring:

- **Type safety**: mapping errors detected at compile time.
- **Performance**: no reflection overhead at runtime.
- **Lombok integration**: configured as an annotation processor alongside Lombok in the `maven-compiler-plugin`.

### Maven Configuration

```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${mapstruct.version}</version>
    </path>
</annotationProcessorPaths>
```

### Alternatives Considered

| Alternative   | Reason for Rejection                                                                          |
|---------------|-----------------------------------------------------------------------------------------------|
| ModelMapper   | Reflection-based mapping at runtime; less performant and type-safe compared to MapStruct      |

---

## 14. Lombok 1.18.36

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | Project Lombok                                 |
| **Version** | 1.18.36                                        |
| **Purpose** | Java boilerplate reduction                     |

### Usage in the Project

Lombok is a shared dependency (`<optional>true</optional>`) across all project modules. Annotations used include:

- `@Data`: generates getter, setter, `equals()`, `hashCode()`, `toString()`.
- `@Builder`: builder pattern for fluent object construction.
- `@NoArgsConstructor`, `@AllArgsConstructor`: constructors required by JPA and Jackson.
- `@Builder.Default`: default values in builders (e.g., `recursive = true`).
- `@Slf4j`: SLF4J logger automatically injected.

---

## 15. Maven

| Property    | Value                                          |
|-------------|------------------------------------------------|
| **Name**    | Apache Maven                                   |
| **Version** | 3.x (wrapper not included)                     |
| **Purpose** | Build tool and dependency management           |

### Rationale

Maven is the project's build tool, chosen for:

- **Multi-module support**: native structure for multi-module projects with parent POM.
- **Centralized dependency management**: versions managed in the parent POM via `<dependencyManagement>` and BOM.
- **Mature plugin ecosystem**: `maven-compiler-plugin` with annotation processor for Lombok and MapStruct.
- **Standard conventions**: standard directory structure (`src/main/java`, `src/main/resources`).
- **Spring Boot plugin**: `spring-boot-maven-plugin` for executable fat JAR packaging.

### Alternatives Considered

| Alternative | Reason for Rejection                                                                                                          |
|-------------|-------------------------------------------------------------------------------------------------------------------------------|
| Gradle      | Faster incremental builds, but Groovy/Kotlin DSL adds complexity; Maven is more predictable for heterogeneous teams           |

---

## 16. Dependency Summary Table

The following table lists all dependencies with their respective versions, as defined in the project POM files.

### Dependencies Managed by the Parent POM

| Dependency                              | Version     | Module                    | Scope     |
|-----------------------------------------|-------------|---------------------------|-----------|
| `spring-boot-starter-parent`            | 3.4.2       | Parent POM                | -         |
| `spring-ai-bom`                         | 1.0.0       | Parent POM (BOM)          | import    |
| `lombok`                                | 1.18.36     | All modules               | optional  |
| `spring-boot-starter-test`              | 3.4.2*      | All modules               | test      |
| `mapstruct` / `mapstruct-processor`     | 1.6.3       | infrastructure, api       | compile   |

### Dependencies by Module

#### localmind-domain
| Dependency    | Version | Notes                                       |
|---------------|---------|---------------------------------------------|
| (none)        | -       | Pure Java module, zero framework dependencies |

#### localmind-infrastructure
| Dependency                                  | Version   | Notes                      |
|---------------------------------------------|-----------|----------------------------|
| `spring-boot-starter-data-jpa`              | 3.4.2*    | ORM and data access        |
| `spring-boot-starter-security`              | 3.4.2*    | Authentication and CORS    |
| `spring-boot-starter-webflux`               | 3.4.2*    | WebClient for HTTP calls   |
| `spring-ai-starter-model-ollama`            | 1.0.0*    | Ollama client              |
| `spring-ai-starter-model-openai`            | 1.0.0*    | OpenAI client              |
| `spring-ai-starter-model-anthropic`         | 1.0.0*    | Anthropic client           |
| `spring-ai-starter-vector-store-qdrant`     | 1.0.0*    | Qdrant client              |
| `mysql-connector-j`                         | runtime*  | MySQL JDBC driver          |
| `tika-core`                                 | 2.9.2     | Text extraction (core)     |
| `tika-parsers-standard-package`             | 2.9.2     | Multi-format parser        |
| `spring-retry`                              | 3.4.2*    | Retry for LLM calls        |

#### localmind-api
| Dependency                          | Version  | Notes                     |
|-------------------------------------|----------|---------------------------|
| `spring-boot-starter-web`           | 3.4.2*   | Web MVC and REST          |
| `spring-boot-starter-validation`    | 3.4.2*   | Bean Validation (Jakarta) |

#### localmind-batch
| Dependency                          | Version  | Notes                    |
|-------------------------------------|----------|--------------------------|
| `spring-boot-starter-batch`         | 3.4.2*   | Spring Batch framework   |

#### localmind-app
| Dependency                          | Version  | Notes                    |
|-------------------------------------|----------|--------------------------|
| `spring-boot-starter-actuator`      | 3.4.2*   | Health check and metrics |
| `flyway-core`                       | 3.4.2*   | Database migrations      |
| `flyway-mysql`                      | 3.4.2*   | MySQL support            |

> **Note**: versions marked with `*` are automatically managed by the parent POM (`spring-boot-starter-parent` 3.4.2) or by the Spring AI 1.0.0 BOM and are not explicitly specified in the module POMs.
