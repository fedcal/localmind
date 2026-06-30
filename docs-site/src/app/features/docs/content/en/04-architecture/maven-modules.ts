export const content = `# Maven Module Structure

| Field        | Value                           |
|--------------|---------------------------------|
| **Document** | Maven Module Structure          |
| **Version**  | 0.1.0                           |
| **Date**     | 2026-02-09                      |
| **Project**  | LocalMind                       |

---

## Table of Contents

1. [Overview](#1-overview)
2. [Parent POM](#2-parent-pom)
3. [localmind-domain Module](#3-localmind-domain-module)
4. [localmind-infrastructure Module](#4-localmind-infrastructure-module)
5. [localmind-api Module](#5-localmind-api-module)
6. [localmind-batch Module](#6-localmind-batch-module)
7. [localmind-app Module](#7-localmind-app-module)
8. [Dependency Graph](#8-dependency-graph)
9. [Dependency Versions](#9-dependency-versions)

---

## 1. Overview

The LocalMind backend is structured as a Maven multi-module project with a parent POM and five child modules. This organization faithfully reflects the hexagonal architecture, ensuring that dependency rules are enforced at compile time.

\`\`\`
localmind-backend/              (parent POM)
+-- localmind-domain/           (pure domain, zero framework)
+-- localmind-infrastructure/   (adapters, JPA, Spring AI, Tika)
+-- localmind-api/              (REST controllers, DTOs, mapping)
+-- localmind-batch/            (Spring Batch jobs)
+-- localmind-app/              (bootstrap, fat JAR)
\`\`\`

---

## 2. Parent POM

The parent POM manages the configuration shared across all modules:

### 2.1 Main Configuration

| Property           | Value                           |
|--------------------|---------------------------------|
| GroupId            | com.localmind                   |
| ArtifactId         | localmind-backend               |
| Version            | 0.1.0-SNAPSHOT                  |
| Packaging          | pom                             |
| Parent             | spring-boot-starter-parent 3.4.2|
| Java Version       | 17                              |

### 2.2 Bill of Materials (BOM)

The parent POM imports the following BOMs for centralized version management:

| BOM                           | Version  | Purpose                             |
|-------------------------------|----------|-------------------------------------|
| spring-ai-bom                 | 1.0.0    | Spring AI starters and dependencies |
| spring-boot-dependencies      | 3.4.2    | Spring Boot (inherited from parent) |

### 2.3 Common Plugins

| Plugin                 | Version  | Purpose                             |
|------------------------|----------|-------------------------------------|
| maven-compiler-plugin  | 3.11.0   | Java 17 compilation                 |
| maven-surefire-plugin  | 3.2.5    | Unit test execution                 |
| lombok-maven-plugin    | -        | Lombok annotation processing        |
| mapstruct-processor    | 1.6.3    | MapStruct code generation           |

---

## 3. localmind-domain Module

### 3.1 Responsibilities

The domain module contains the pure business logic of the application. Every entity, value object, domain service, port, and enum is defined in this module.

### 3.2 Dependencies

| Dependency     | Scope          | Rationale                                       |
|----------------|----------------|-------------------------------------------------|
| lombok         | provided       | Boilerplate reduction (getter, setter, builder) |

**No framework dependencies**. This module compiles and works without Spring Boot, JPA, Spring AI, or any other infrastructure library.

### 3.3 Contents

\`\`\`
localmind-domain/
+-- src/main/java/com/localmind/domain/
|   +-- llm/          (model, port, service)
|   +-- document/      (model, port, service)
|   +-- agent/         (model, port, service)
|   +-- automation/    (model, port, service)
|   +-- common/        (common exceptions, domain utilities)
+-- pom.xml
\`\`\`

---

## 4. localmind-infrastructure Module

### 4.1 Responsibilities

The infrastructure module contains all the concrete implementations of the ports defined in the domain: adapters for LLM providers, JPA persistence, vector store integration, text extraction, filesystem scanning, HTTP clients.

### 4.2 Dependencies

| Dependency                                 | Scope    | Rationale                            |
|--------------------------------------------|----------|--------------------------------------|
| **localmind-domain**                       | compile  | Port implementation                  |
| spring-boot-starter-data-jpa               | compile  | Relational persistence               |
| spring-boot-starter-security               | compile  | Authentication and authorization     |
| spring-ai-ollama-spring-boot-starter       | compile  | Ollama integration                   |
| spring-ai-openai-spring-boot-starter       | compile  | OpenAI integration                   |
| spring-ai-anthropic-spring-boot-starter    | compile  | Anthropic integration                |
| spring-ai-qdrant-store-spring-boot-starter | compile  | Qdrant integration                   |
| mysql-connector-j                          | runtime  | MySQL JDBC driver                    |
| spring-retry                               | compile  | Retry logic with backoff             |
| spring-boot-starter-aop                    | compile  | AOP for retry annotations            |
| tika-core                                  | compile  | Text extraction (core)               |
| tika-parsers-standard-package              | compile  | Parsers for PDF, DOCX, etc.          |
| lombok                                     | provided | Boilerplate reduction                |

### 4.3 Contents

\`\`\`
localmind-infrastructure/
+-- src/main/java/com/localmind/infrastructure/
|   +-- llm/adapter/           (OllamaLlmAdapter, OpenAiLlmAdapter, etc.)
|   +-- persistence/entity/    (JPA entities)
|   +-- persistence/repository/ (Spring Data JPA repositories)
|   +-- persistence/adapter/   (Persistence adapters)
|   +-- document/adapter/      (TikaTextExtractor, LocalFileSystemScanner)
|   +-- vectorstore/adapter/   (QdrantVectorStoreAdapter)
|   +-- automation/adapter/    (N8nWebhookClient)
|   +-- config/                (Spring configuration classes)
+-- src/main/resources/
|   +-- db/migration/          (Flyway migrations, if present)
+-- pom.xml
\`\`\`

---

## 5. localmind-api Module

### 5.1 Responsibilities

The API module contains the REST controllers, DTOs (Data Transfer Objects), and mappers that convert between DTOs and domain models. It represents the HTTP presentation layer of the application.

### 5.2 Dependencies

| Dependency                         | Scope    | Rationale                            |
|------------------------------------|----------|--------------------------------------|
| **localmind-domain**               | compile  | Access to use cases and models       |
| spring-boot-starter-web            | compile  | REST controllers, Jackson            |
| spring-boot-starter-validation     | compile  | Bean Validation (Jakarta)            |
| mapstruct                          | compile  | DTO <-> Domain mapping               |
| mapstruct-processor                | provided | Code generation                      |
| lombok                             | provided | Boilerplate reduction                |

### 5.3 Contents

\`\`\`
localmind-api/
+-- src/main/java/com/localmind/api/
|   +-- llm/controller/        (ChatController)
|   +-- llm/dto/               (ChatRequestDto, ChatResponseDto)
|   +-- document/controller/   (DocumentController, DocumentSearchController)
|   +-- document/dto/          (DocumentUploadDto, SearchResultDto, etc.)
|   +-- agent/controller/      (AgentController)
|   +-- agent/dto/             (AgentExecutionRequestDto, etc.)
|   +-- automation/controller/ (AutomationController)
|   +-- automation/dto/        (WebhookDto, etc.)
|   +-- dashboard/controller/  (DashboardController)
|   +-- dashboard/dto/         (HealthStatusDto)
|   +-- common/                (GlobalExceptionHandler, ApiErrorResponse)
+-- pom.xml
\`\`\`

---

## 6. localmind-batch Module

### 6.1 Responsibilities

The batch module contains the Spring Batch configurations for asynchronous processing jobs: document ingestion, folder scanning, periodic indexing.

### 6.2 Dependencies

| Dependency                         | Scope    | Rationale                            |
|------------------------------------|----------|--------------------------------------|
| **localmind-domain**               | compile  | Access to domain services            |
| **localmind-infrastructure**       | compile  | Access to adapters for execution     |
| spring-boot-starter-batch          | compile  | Spring Batch framework               |
| lombok                             | provided | Boilerplate reduction                |

### 6.3 Contents

\`\`\`
localmind-batch/
+-- src/main/java/com/localmind/batch/
|   +-- job/
|   |   +-- DocumentIngestionJobConfig.java  (Document ingestion job)
|   |   +-- FolderScanJobConfig.java         (Folder scan job)
|   +-- step/
|   |   +-- TextExtractionStep.java          (Text extraction step)
|   |   +-- ChunkingStep.java               (Chunking step)
|   |   +-- EmbeddingStep.java              (Embedding step)
|   +-- config/
|       +-- BatchSchedulerConfig.java        (Job scheduling)
+-- pom.xml
\`\`\`

---

## 7. localmind-app Module

### 7.1 Responsibilities

The app module is the application's bootstrap module. It depends on all other modules and produces the executable fat JAR. It contains the \`@SpringBootApplication\` class, global configurations, and database migrations.

### 7.2 Dependencies

| Dependency                         | Scope    | Rationale                            |
|------------------------------------|----------|--------------------------------------|
| **localmind-domain**               | compile  | Domain models and services           |
| **localmind-infrastructure**       | compile  | Adapters and persistence             |
| **localmind-api**                  | compile  | REST controllers                     |
| **localmind-batch**                | compile  | Batch jobs                           |
| spring-boot-starter-actuator       | compile  | Health check and metrics             |
| flyway-core                        | compile  | Database migrations                  |
| flyway-mysql                       | compile  | MySQL support for Flyway             |
| spring-boot-starter-test           | test     | Testing framework                    |

### 7.3 Contents

\`\`\`
localmind-app/
+-- src/main/java/com/localmind/
|   +-- LocalMindApplication.java       (@SpringBootApplication)
+-- src/main/resources/
|   +-- application.yml                 (Base configuration)
|   +-- application-dev.yml             (Development configuration)
|   +-- application-docker.yml          (Docker configuration)
|   +-- db/migration/                   (Flyway SQL migrations)
|       +-- V1__init_schema.sql
|       +-- V2__add_documents.sql
|       +-- ...
+-- pom.xml
\`\`\`

### 7.4 Packaging

The app module produces the executable fat JAR via the \`spring-boot-maven-plugin\`:

\`\`\`xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <mainClass>com.localmind.LocalMindApplication</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>
\`\`\`

Build command:
\`\`\`bash
mvn clean package -pl localmind-app -am
\`\`\`

The result is an executable file \`localmind-app/target/localmind-app-0.1.0-SNAPSHOT.jar\` that can be run with \`java -jar\`.

---

## 8. Dependency Graph

\`\`\`
                    +-------------------+
                    |                   |
                    |  localmind-app    |
                    |  (bootstrap)      |
                    |                   |
                    +--+--+--+--+-------+
                       |  |  |  |
          +------------+  |  |  +------------+
          |               |  |               |
          v               v  v               v
+---------+---+ +---------+--+---+ +---------+----+
|             | |                | |              |
|localmind-api| | localmind-batch| |localmind-    |
|(controllers)| | (Spring Batch) | |infrastructure|
|             | |                | |(adapters)    |
+------+------+ +----+-----+-----+ +------+-------+
       |              |     |            |
       |              |     +-----+------+
       |              |           |
       v              v           v
  +----+--------------+-----------+-----+
  |                                     |
  |        localmind-domain             |
  |        (entities, services, ports)  |
  |                                     |
  |        ZERO framework dependencies  |
  +-------------------------------------+
\`\`\`

### 8.1 Dependency Rules

| Module                   | Depends on                                |
|--------------------------|-------------------------------------------|
| localmind-domain         | (none)                                    |
| localmind-infrastructure | localmind-domain                          |
| localmind-api            | localmind-domain                          |
| localmind-batch          | localmind-domain, localmind-infrastructure|
| localmind-app            | ALL                                       |

### 8.2 Dependency Direction

All arrows point downward, toward the domain. No dependency goes back from the domain to the upper modules. This rule is structurally guaranteed by the Maven POMs.

---

## 9. Dependency Versions

### 9.1 Main Dependencies

| Dependency             | Version     | Managed by                 |
|------------------------|-------------|----------------------------|
| Java                   | 17          | maven-compiler-plugin      |
| Spring Boot            | 3.4.2       | spring-boot-starter-parent |
| Spring AI              | 1.0.0       | spring-ai-bom              |
| Spring Batch           | 5.1.x       | spring-boot-dependencies   |
| Spring Security        | 6.4.x       | spring-boot-dependencies   |
| Spring Data JPA        | 3.4.x       | spring-boot-dependencies   |
| MySQL Connector/J      | 8.x         | spring-boot-dependencies   |
| Flyway                 | 10.x        | spring-boot-dependencies   |
| Jackson                | 2.17.x      | spring-boot-dependencies   |
| Hibernate              | 6.6.x       | spring-boot-dependencies   |

### 9.2 Explicit Dependencies

| Dependency             | Version     | Rationale                |
|------------------------|-------------|--------------------------|
| MapStruct              | 1.6.3       | DTO <-> Domain mapping   |
| Lombok                 | 1.18.36     | Boilerplate reduction    |
| Apache Tika (core)     | 2.9.2       | Text extraction          |
| Apache Tika (parsers)  | 2.9.2       | PDF, DOCX, etc. parsers  |

### 9.3 Test Dependencies

| Dependency             | Version     | Scope    |
|------------------------|-------------|----------|
| JUnit 5                | 5.10.x      | test     |
| Mockito                | 5.x         | test     |
| AssertJ                | 3.25.x      | test     |
| Spring Boot Test       | 3.4.2       | test     |
| Testcontainers         | 1.19.x      | test     |
| H2 Database            | 2.2.x       | test     |
`;
