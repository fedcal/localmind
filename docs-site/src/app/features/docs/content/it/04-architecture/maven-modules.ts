export const content = `# Struttura Moduli Maven

| Campo        | Valore                          |
|--------------|---------------------------------|
| **Documento**| Struttura Moduli Maven          |
| **Versione** | 0.1.0                           |
| **Data**     | 2026-02-09                      |
| **Progetto** | LocalMind                       |

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Parent POM](#2-parent-pom)
3. [Modulo localmind-domain](#3-modulo-localmind-domain)
4. [Modulo localmind-infrastructure](#4-modulo-localmind-infrastructure)
5. [Modulo localmind-api](#5-modulo-localmind-api)
6. [Modulo localmind-batch](#6-modulo-localmind-batch)
7. [Modulo localmind-app](#7-modulo-localmind-app)
8. [Grafo delle Dipendenze](#8-grafo-delle-dipendenze)
9. [Versioni delle Dipendenze](#9-versioni-delle-dipendenze)

---

## 1. Panoramica

Il backend di LocalMind e' strutturato come progetto Maven multi-modulo con un parent POM e cinque moduli figli. Questa organizzazione riflette fedelmente l'architettura esagonale, garantendo che le regole di dipendenza siano enforced a tempo di compilazione.

\`\`\`
localmind-backend/              (parent POM)
+-- localmind-domain/           (dominio puro, zero framework)
+-- localmind-infrastructure/   (adapter, JPA, Spring AI, Tika)
+-- localmind-api/              (controller REST, DTO, mapping)
+-- localmind-batch/            (Spring Batch jobs)
+-- localmind-app/              (bootstrap, fat JAR)
\`\`\`

---

## 2. Parent POM

Il parent POM gestisce la configurazione comune a tutti i moduli:

### 2.1 Configurazione Principale

| Proprieta'         | Valore                          |
|--------------------|---------------------------------|
| GroupId            | com.localmind                   |
| ArtifactId         | localmind-backend               |
| Version            | 0.1.0-SNAPSHOT                  |
| Packaging          | pom                             |
| Parent             | spring-boot-starter-parent 3.4.2|
| Java Version       | 17                              |

### 2.2 Bill of Materials (BOM)

Il parent POM importa i seguenti BOM per la gestione centralizzata delle versioni:

| BOM                           | Versione | Scopo                               |
|-------------------------------|----------|-------------------------------------|
| spring-ai-bom                 | 1.0.0    | Spring AI starters e dipendenze     |
| spring-boot-dependencies      | 3.4.2    | Spring Boot (ereditato dal parent)  |

### 2.3 Plugin Comuni

| Plugin                 | Versione | Scopo                               |
|------------------------|----------|-------------------------------------|
| maven-compiler-plugin  | 3.11.0   | Compilazione Java 17                |
| maven-surefire-plugin  | 3.2.5    | Esecuzione test unitari             |
| lombok-maven-plugin    | -        | Annotation processing Lombok        |
| mapstruct-processor    | 1.6.3    | Code generation MapStruct           |

---

## 3. Modulo localmind-domain

### 3.1 Responsabilita'

Il modulo domain contiene la logica di business pura dell'applicazione. Ogni entita', value object, servizio di dominio, porta e enum e' definito in questo modulo.

### 3.2 Dipendenze

| Dipendenza     | Scope          | Motivazione                                     |
|----------------|----------------|-------------------------------------------------|
| lombok         | provided       | Riduzione boilerplate (getter, setter, builder) |

**Nessuna dipendenza da framework**. Questo modulo compila e funziona senza Spring Boot, JPA, Spring AI o qualsiasi altra libreria infrastrutturale.

### 3.3 Contenuto

\`\`\`
localmind-domain/
+-- src/main/java/com/localmind/domain/
|   +-- llm/          (model, port, service)
|   +-- document/      (model, port, service)
|   +-- agent/         (model, port, service)
|   +-- automation/    (model, port, service)
|   +-- common/        (eccezioni comuni, utility di dominio)
+-- pom.xml
\`\`\`

---

## 4. Modulo localmind-infrastructure

### 4.1 Responsabilita'

Il modulo infrastructure contiene tutte le implementazioni concrete delle porte definite nel dominio: adapter per provider LLM, persistenza JPA, integrazione vector store, estrazione testo, scansione filesystem, client HTTP.

### 4.2 Dipendenze

| Dipendenza                                 | Scope    | Motivazione                          |
|--------------------------------------------|----------|--------------------------------------|
| **localmind-domain**                       | compile  | Implementazione delle porte          |
| spring-boot-starter-data-jpa               | compile  | Persistenza relazionale              |
| spring-boot-starter-security               | compile  | Autenticazione e autorizzazione      |
| spring-ai-ollama-spring-boot-starter       | compile  | Integrazione Ollama                  |
| spring-ai-openai-spring-boot-starter       | compile  | Integrazione OpenAI                  |
| spring-ai-anthropic-spring-boot-starter    | compile  | Integrazione Anthropic               |
| spring-ai-qdrant-store-spring-boot-starter | compile  | Integrazione Qdrant                  |
| mysql-connector-j                          | runtime  | Driver JDBC MySQL                    |
| spring-retry                               | compile  | Logica di retry con backoff          |
| spring-boot-starter-aop                    | compile  | AOP per retry annotations            |
| tika-core                                  | compile  | Estrazione testo (core)              |
| tika-parsers-standard-package              | compile  | Parser per PDF, DOCX, etc.           |
| lombok                                     | provided | Riduzione boilerplate                |

### 4.3 Contenuto

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
|   +-- db/migration/          (Flyway migrations, se presente)
+-- pom.xml
\`\`\`

---

## 5. Modulo localmind-api

### 5.1 Responsabilita'

Il modulo API contiene i controller REST, i DTO (Data Transfer Object) e i mapper che convertono tra DTO e modelli di dominio. Rappresenta il layer di presentazione HTTP dell'applicazione.

### 5.2 Dipendenze

| Dipendenza                         | Scope    | Motivazione                          |
|------------------------------------|----------|--------------------------------------|
| **localmind-domain**               | compile  | Accesso ai use case e modelli        |
| spring-boot-starter-web            | compile  | REST controllers, Jackson            |
| spring-boot-starter-validation     | compile  | Bean Validation (Jakarta)            |
| mapstruct                          | compile  | Mapping DTO <-> Domain               |
| mapstruct-processor                | provided | Code generation                      |
| lombok                             | provided | Riduzione boilerplate                |

### 5.3 Contenuto

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

## 6. Modulo localmind-batch

### 6.1 Responsabilita'

Il modulo batch contiene le configurazioni di Spring Batch per i job di elaborazione asincrona: ingestione documenti, scansione cartelle, indicizzazione periodica.

### 6.2 Dipendenze

| Dipendenza                         | Scope    | Motivazione                          |
|------------------------------------|----------|--------------------------------------|
| **localmind-domain**               | compile  | Accesso ai servizi di dominio        |
| **localmind-infrastructure**       | compile  | Accesso agli adapter per l'esecuzione|
| spring-boot-starter-batch          | compile  | Spring Batch framework               |
| lombok                             | provided | Riduzione boilerplate                |

### 6.3 Contenuto

\`\`\`
localmind-batch/
+-- src/main/java/com/localmind/batch/
|   +-- job/
|   |   +-- DocumentIngestionJobConfig.java  (Job ingestione documenti)
|   |   +-- FolderScanJobConfig.java         (Job scansione cartelle)
|   +-- step/
|   |   +-- TextExtractionStep.java          (Step estrazione testo)
|   |   +-- ChunkingStep.java               (Step chunking)
|   |   +-- EmbeddingStep.java              (Step embedding)
|   +-- config/
|       +-- BatchSchedulerConfig.java        (Scheduling dei job)
+-- pom.xml
\`\`\`

---

## 7. Modulo localmind-app

### 7.1 Responsabilita'

Il modulo app e' il modulo bootstrap dell'applicazione. Dipende da tutti gli altri moduli e produce il fat JAR eseguibile. Contiene la classe \`@SpringBootApplication\`, le configurazioni globali e le migrazioni database.

### 7.2 Dipendenze

| Dipendenza                         | Scope    | Motivazione                          |
|------------------------------------|----------|--------------------------------------|
| **localmind-domain**               | compile  | Modelli e servizi di dominio         |
| **localmind-infrastructure**       | compile  | Adapter e persistenza                |
| **localmind-api**                  | compile  | Controller REST                      |
| **localmind-batch**                | compile  | Job batch                            |
| spring-boot-starter-actuator       | compile  | Health check e metriche              |
| flyway-core                        | compile  | Migrazioni database                  |
| flyway-mysql                       | compile  | Supporto MySQL per Flyway            |
| spring-boot-starter-test           | test     | Testing framework                    |

### 7.3 Contenuto

\`\`\`
localmind-app/
+-- src/main/java/com/localmind/
|   +-- LocalMindApplication.java       (@SpringBootApplication)
+-- src/main/resources/
|   +-- application.yml                 (Configurazione base)
|   +-- application-dev.yml             (Configurazione sviluppo)
|   +-- application-docker.yml          (Configurazione Docker)
|   +-- db/migration/                   (Flyway SQL migrations)
|       +-- V1__init_schema.sql
|       +-- V2__add_documents.sql
|       +-- ...
+-- pom.xml
\`\`\`

### 7.4 Packaging

Il modulo app produce il fat JAR eseguibile tramite il plugin \`spring-boot-maven-plugin\`:

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

Comando di build:
\`\`\`bash
mvn clean package -pl localmind-app -am
\`\`\`

Il risultato e' un file \`localmind-app/target/localmind-app-0.1.0-SNAPSHOT.jar\` eseguibile con \`java -jar\`.

---

## 8. Grafo delle Dipendenze

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
  |        ZERO dipendenze framework    |
  +-------------------------------------+
\`\`\`

### 8.1 Regole di Dipendenza

| Modulo                   | Dipende da                                |
|--------------------------|-------------------------------------------|
| localmind-domain         | (nessuno)                                 |
| localmind-infrastructure | localmind-domain                          |
| localmind-api            | localmind-domain                          |
| localmind-batch          | localmind-domain, localmind-infrastructure|
| localmind-app            | TUTTI                                     |

### 8.2 Direzione delle Dipendenze

Tutte le frecce puntano verso il basso, verso il dominio. Nessuna dipendenza risale dal dominio verso i moduli superiori. Questa regola e' garantita strutturalmente dai POM Maven.

---

## 9. Versioni delle Dipendenze

### 9.1 Dipendenze Principali

| Dipendenza             | Versione    | Gestita da                 |
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

### 9.2 Dipendenze Esplicite

| Dipendenza             | Versione    | Motivazione              |
|------------------------|-------------|--------------------------|
| MapStruct              | 1.6.3       | Mapping DTO <-> Domain   |
| Lombok                 | 1.18.36     | Riduzione boilerplate    |
| Apache Tika (core)     | 2.9.2       | Estrazione testo         |
| Apache Tika (parsers)  | 2.9.2       | Parser PDF, DOCX, etc.   |

### 9.3 Dipendenze di Test

| Dipendenza             | Versione    | Scope    |
|------------------------|-------------|----------|
| JUnit 5                | 5.10.x      | test     |
| Mockito                | 5.x         | test     |
| AssertJ                | 3.25.x      | test     |
| Spring Boot Test       | 3.4.2       | test     |
| Testcontainers         | 1.19.x      | test     |
| H2 Database            | 2.2.x       | test     |
`;
