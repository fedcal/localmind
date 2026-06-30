# Hexagonal Architecture

| Field        | Value                           |
|--------------|---------------------------------|
| **Document** | Hexagonal Architecture          |
| **Version**  | 1.0.0                           |
| **Date**     | 2026-02-09                      |
| **Project**  | LocalMind                       |

---

## Table of Contents

1. [Fundamental Principles](#1-fundamental-principles)
2. [The Three Concentric Layers](#2-the-three-concentric-layers)
3. [Package Structure](#3-package-structure)
4. [Dependency Rule](#4-dependency-rule)
5. [Architecture Benefits](#5-architecture-benefits)
6. [Practical Example: LLM Module](#6-practical-example-llm-module)
7. [Practical Example: Document Module](#7-practical-example-document-module)

---

## 1. Fundamental Principles

The hexagonal architecture (also known as Ports & Adapters, conceived by Alistair Cockburn) is founded on three core principles that LocalMind rigorously implements:

### 1.1 Separation of Domain from Framework

The domain code (entities, value objects, domain services, business rules) contains no dependencies on frameworks or infrastructure libraries. The `localmind-domain` module compiles and works without Spring Boot, JPA, Spring AI, or any other external library (the only exception is Lombok for boilerplate reduction).

### 1.2 Dependency Inversion

The domain defines the interfaces (ports) that the infrastructure must implement, not the other way around. The domain does not depend on the infrastructure: it is the infrastructure that depends on the domain.

```
WRONG:   Domain --> Infrastructure (domain knows about JPA, Spring AI, etc.)
CORRECT: Infrastructure --> Domain  (infrastructure implements the domain ports)
```

### 1.3 Testability

Thanks to the separation, domain services can be unit tested with port mocks, without needing to start the Spring context, the database, or external services. This drastically reduces test execution time and increases test suite reliability.

---

## 2. The Three Concentric Layers

The architecture is organized into three concentric layers, from the center (most stable) to the outside (most volatile):

```
+---------------------------------------------------------------------+
|                                                                     |
|   INFRASTRUCTURE LAYER (Adapters)                                   |
|   - REST Controllers                                                |
|   - JPA Repositories                                                |
|   - Spring AI Clients                                               |
|   - Apache Tika Adapters                                            |
|   - Spring Batch Jobs                                               |
|   - HTTP Clients (n8n, Qdrant)                                      |
|                                                                     |
|   +--------------------------------------------------------------+  |
|   |                                                              |  |
|   |   APPLICATION LAYER (Ports)                                  |  |
|   |   - Use Case interfaces (ports in)                           |  |
|   |   - Repository interfaces (ports out)                        |  |
|   |   - Client interfaces (ports out)                            |  |
|   |                                                              |  |
|   |   +------------------------------------------------------+   |  |
|   |   |                                                      |   |  |
|   |   |   DOMAIN LAYER (Core)                                |   |  |
|   |   |   - Entities (Document, Agent, Webhook, etc.)        |   |  |
|   |   |   - Value Objects (ChatRequest, ChatResponse, etc.)  |   |  |
|   |   |   - Domain Services (LlmGatewayService, etc.)        |   |  |
|   |   |   - Enums (LlmProvider, DocumentStatus, etc.)        |   |  |
|   |   |   - Domain Exceptions                                |   |  |
|   |   |   - ZERO framework dependencies                      |   |  |
|   |   |                                                      |   |  |
|   |   +------------------------------------------------------+   |  |
|   |                                                              |  |
|   +--------------------------------------------------------------+  |
|                                                                     |
+---------------------------------------------------------------------+
```

### 2.1 Domain Layer (Center)

The heart of the application. Contains pure business logic:

- **Entities**: objects with persistent identity (Document, Agent, Webhook, LlmUsage)
- **Value Objects**: immutable objects without identity (ChatRequest, ChatResponse, SearchResult, DocumentChunk)
- **Domain Services**: services that implement complex business logic that does not belong to a single entity (LlmGatewayService, DocumentService, ChunkingService, CostTrackingService, AutomationService, AgentService, StreamingChatService)
- **Enums**: domain enumerations (LlmProvider, DocumentStatus, AgentType, AutomationEvent)
- **Exceptions**: domain-specific exceptions (ProviderUnavailableException, DocumentProcessingException)

**Dependencies**: ZERO framework dependencies. Only Lombok (compile-only).

### 2.2 Application Layer (Ports)

Defines the contracts between the domain and the external world:

- **Ports In (Use Case)**: interfaces that the external world (controllers) invokes to interact with the domain
  - `ChatUseCase`: send chat messages
  - `StreamingChatUseCase`: streaming chat with token callback
  - `DocumentIngestionUseCase`: document ingestion
  - `DocumentSearchUseCase`: semantic search
  - `AgentExecutionUseCase`: agent execution
  - `AutomationUseCase`: webhook CRUD, event triggering and webhook testing

- **Ports Out (SPI)**: interfaces that the domain invokes to access external resources
  - `LlmClient`: communication with LLM providers
  - `StreamingLlmClient`: streaming LLM, returns `Flux<String>` for tokens
  - `DocumentRepository`: document persistence
  - `VectorStorePort`: vector store for embeddings
  - `TextExtractorPort`: text extraction from files
  - `FileSystemScannerPort`: filesystem scanning
  - `LlmUsageRepository`: usage metrics persistence
  - `AgentConfigRepository`: agent configuration persistence
  - `WebhookRepository`: webhook persistence (save, findById, findAll, findByEventType, deleteById)
  - `WebhookClientPort`: HTTP invocation to external webhooks

### 2.3 Infrastructure Layer (Adapters)

Concrete implementations of the ports:

- **Adapter In (Driving)**: REST controllers that invoke the use cases
  - `ChatController` -> `ChatUseCase`
  - `StreamingChatController` -> `StreamingChatUseCase` (`POST /api/v1/chat/stream` with `SseEmitter`)
  - `DocumentController` -> `DocumentIngestionUseCase`
  - `DocumentSearchController` -> `DocumentSearchUseCase`
  - `AgentController` -> `AgentExecutionUseCase`
  - `AutomationController` -> `AutomationUseCase`
  - `WebhookController` -> `AutomationUseCase` (CRUD REST on `/api/v1/webhooks`)

- **Adapter Out (Driven)**: implementations that the domain uses through the ports
  - `OllamaLlmAdapter` -> `LlmClient`
  - `OpenAiLlmAdapter` -> `LlmClient`
  - `AnthropicLlmAdapter` -> `LlmClient`
  - `OllamaStreamingLlmAdapter` -> `StreamingLlmClient` (streaming via Spring AI `StreamingChatModel` for Ollama)
  - `OpenAiStreamingLlmAdapter` -> `StreamingLlmClient` (streaming for OpenAI)
  - `AnthropicStreamingLlmAdapter` -> `StreamingLlmClient` (streaming for Anthropic)
  - `DocumentPersistenceAdapter` -> `DocumentRepository`
  - `QdrantVectorStoreAdapter` -> `VectorStorePort`
  - `TikaTextExtractor` -> `TextExtractorPort`
  - `LocalFileSystemScanner` -> `FileSystemScannerPort`
  - `WebhookRepositoryAdapter` -> `WebhookRepository` (mapping domain Webhook <-> JPA WebhookEntity)
  - `N8nWebhookClient` -> `WebhookClientPort` (implementation with Spring WebClient)

---

## 3. Package Structure

### 3.1 Domain Module

```
com.localmind.domain
+-- llm/
|   +-- model/
|   |   +-- ChatRequest.java          # Value Object
|   |   +-- ChatResponse.java         # Value Object
|   |   +-- LlmProvider.java          # Enum
|   |   +-- LlmUsage.java             # Entity
|   +-- port/
|   |   +-- in/
|   |   |   +-- ChatUseCase.java       # Port In
|   |   |   +-- StreamingChatUseCase.java # Port In
|   |   +-- out/
|   |       +-- LlmClient.java         # Port Out
|   |       +-- StreamingLlmClient.java # Port Out (Flux<String>)
|   |       +-- LlmUsageRepository.java # Port Out
|   +-- service/
|       +-- LlmGatewayService.java     # Domain Service (implements ChatUseCase)
|       +-- StreamingChatService.java  # Domain Service (implements StreamingChatUseCase)
|       +-- CostTrackingService.java   # Domain Service
+-- document/
|   +-- model/
|   |   +-- Document.java             # Entity
|   |   +-- DocumentChunk.java        # Value Object
|   |   +-- DocumentStatus.java       # Enum
|   |   +-- SearchResult.java         # Value Object
|   +-- port/
|   |   +-- in/
|   |   |   +-- DocumentIngestionUseCase.java  # Port In
|   |   |   +-- DocumentSearchUseCase.java     # Port In
|   |   +-- out/
|   |       +-- DocumentRepository.java         # Port Out
|   |       +-- VectorStorePort.java            # Port Out
|   |       +-- TextExtractorPort.java          # Port Out
|   |       +-- FileSystemScannerPort.java      # Port Out
|   +-- service/
|       +-- DocumentService.java       # Domain Service
|       +-- ChunkingService.java       # Domain Service
+-- agent/
|   +-- model/
|   |   +-- Agent.java                 # Entity
|   |   +-- AgentType.java            # Enum
|   |   +-- AgentTool.java            # Value Object
|   |   +-- AgentExecutionResult.java  # Value Object
|   |   +-- Citation.java             # Value Object
|   +-- port/
|   |   +-- in/
|   |   |   +-- AgentExecutionUseCase.java  # Port In
|   |   +-- out/
|   |       +-- AgentConfigRepository.java  # Port Out
|   +-- service/
|       +-- AgentService.java          # Domain Service
+-- automation/
    +-- model/
    |   +-- Webhook.java               # Entity
    |   +-- AutomationEvent.java       # Enum
    |   +-- WebhookPayload.java        # Value Object
    +-- port/
    |   +-- in/
    |   |   +-- AutomationUseCase.java  # Port In
    |   +-- out/
    |       +-- WebhookRepository.java   # Port Out
    |       +-- WebhookClientPort.java   # Port Out
    +-- service/
        +-- AutomationService.java      # Domain Service
```

### 3.2 Infrastructure Module

```
com.localmind.infrastructure
+-- llm/
|   +-- adapter/
|       +-- OllamaLlmAdapter.java          # Implements LlmClient
|       +-- OpenAiLlmAdapter.java          # Implements LlmClient
|       +-- AnthropicLlmAdapter.java       # Implements LlmClient
|       +-- GoogleLlmAdapter.java          # Implements LlmClient
|       +-- OllamaStreamingLlmAdapter.java    # Implements StreamingLlmClient
|       +-- OpenAiStreamingLlmAdapter.java    # Implements StreamingLlmClient
|       +-- AnthropicStreamingLlmAdapter.java # Implements StreamingLlmClient
+-- persistence/
|   +-- entity/
|   |   +-- DocumentEntity.java             # JPA Entity
|   |   +-- LlmUsageEntity.java            # JPA Entity
|   |   +-- AgentEntity.java               # JPA Entity
|   |   +-- WebhookEntity.java             # JPA Entity
|   +-- repository/
|   |   +-- JpaDocumentRepository.java      # Spring Data JPA
|   |   +-- JpaLlmUsageRepository.java     # Spring Data JPA
|   |   +-- JpaAgentRepository.java        # Spring Data JPA
|   |   +-- JpaWebhookRepository.java      # Spring Data JPA
|   +-- adapter/
|       +-- DocumentPersistenceAdapter.java # Implements DocumentRepository
|       +-- LlmUsagePersistenceAdapter.java # Implements LlmUsageRepository
|       +-- AgentConfigPersistenceAdapter.java # Implements AgentConfigRepository
|       +-- WebhookRepositoryAdapter.java   # Implements WebhookRepository
+-- document/
|   +-- adapter/
|       +-- TikaTextExtractor.java          # Implements TextExtractorPort
|       +-- LocalFileSystemScanner.java     # Implements FileSystemScannerPort
+-- vectorstore/
|   +-- adapter/
|       +-- QdrantVectorStoreAdapter.java   # Implements VectorStorePort
+-- automation/
    +-- adapter/
        +-- N8nWebhookClient.java           # Implements WebhookClientPort
```

### 3.3 API Module

```
com.localmind.api
+-- llm/
|   +-- controller/
|   |   +-- ChatController.java             # REST Controller
|   |   +-- StreamingChatController.java    # REST Controller (SSE)
|   +-- dto/
|       +-- ChatRequestDto.java             # DTO
|       +-- ChatResponseDto.java            # DTO
+-- document/
|   +-- controller/
|   |   +-- DocumentController.java         # REST Controller
|   |   +-- DocumentSearchController.java   # REST Controller
|   +-- dto/
|       +-- DocumentUploadDto.java          # DTO
|       +-- DocumentResponseDto.java        # DTO
|       +-- SearchRequestDto.java           # DTO
|       +-- SearchResultDto.java            # DTO
+-- agent/
|   +-- controller/
|   |   +-- AgentController.java            # REST Controller
|   +-- dto/
|       +-- AgentExecutionRequestDto.java   # DTO
|       +-- AgentExecutionResponseDto.java  # DTO
+-- automation/
|   +-- controller/
|   |   +-- AutomationController.java       # REST Controller
|   |   +-- WebhookController.java          # REST Controller (CRUD /api/v1/webhooks)
|   +-- dto/
|       +-- WebhookDto.java                 # DTO
|       +-- WebhookCreateDto.java           # DTO
+-- dashboard/
    +-- controller/
    |   +-- DashboardController.java        # REST Controller
    +-- dto/
        +-- HealthStatusDto.java            # DTO
```

---

## 4. Dependency Rule

The fundamental rule of hexagonal architecture is:

> **Dependencies point only toward the center, never toward the outside.**

```
Infrastructure --depends on--> Domain    (CORRECT)
API            --depends on--> Domain    (CORRECT)
Batch          --depends on--> Domain    (CORRECT)

Domain         --depends on--> Infrastructure  (FORBIDDEN)
Domain         --depends on--> API             (FORBIDDEN)
Domain         --depends on--> Spring Boot     (FORBIDDEN)
Domain         --depends on--> JPA             (FORBIDDEN)
```

### 4.1 Rule Verification

The rule is structurally guaranteed by Maven modules:

- `localmind-domain`: the POM declares no dependencies on Spring, JPA, or other frameworks
- `localmind-infrastructure`: the POM declares a dependency on `localmind-domain`
- `localmind-api`: the POM declares a dependency on `localmind-domain`
- `localmind-batch`: the POM declares a dependency on `localmind-domain` and `localmind-infrastructure`

This module-level Maven separation makes it **impossible at compile time** to introduce reverse dependencies.

### 4.2 Flow Direction

```
Controller (API) --> Use Case (Port In) --> Domain Service --> Port Out --> Adapter (Infrastructure)

ChatController    --> ChatUseCase        --> LlmGatewayService --> LlmClient --> OllamaLlmAdapter
```

The controller only knows the Use Case interface. The domain service only knows the Port Out interface. The adapter implements the Port Out interface. No component knows the concrete implementations of the others.

---

## 5. Architecture Benefits

### 5.1 The Domain Compiles without Spring Boot

```bash
cd localmind-domain
mvn compile
# SUCCESS: no Spring dependency needed
```

This means the business logic is completely independent from the application framework.

### 5.2 Adapters Are Replaceable

To switch from Ollama to a completely different LLM provider, it is sufficient to:

1. Create a new adapter that implements `LlmClient`
2. Register it as a Spring bean
3. The domain continues to work without any modification

The same principle applies to: database (JPA -> MongoDB), vector store (Qdrant -> Pinecone), text extractor (Tika -> custom), etc.

### 5.3 Unit Tests Do Not Require Spring Context

```java
// Test of domain service with port mocks
@Test
void shouldRouteToDefaultProvider() {
    // Mock of port out
    LlmClient mockClient = mock(LlmClient.class);
    when(mockClient.getProvider()).thenReturn(LlmProvider.OLLAMA);
    when(mockClient.isAvailable()).thenReturn(true);
    when(mockClient.generate(any())).thenReturn(expectedResponse);

    // Service creation with mock (NO Spring context)
    LlmGatewayService service = new LlmGatewayService(List.of(mockClient));

    // Execution
    ChatResponse response = service.chat(request);

    // Verification
    assertThat(response).isEqualTo(expectedResponse);
}
```

### 5.4 Long-Term Maintainability

The clear separation between layers enables:

- Modifying infrastructure without touching the domain
- Adding new features with limited impact
- Safe refactoring with the guarantee that domain unit tests continue to pass
- Rapid onboarding of new developers thanks to the predictable structure

---

## 6. Practical Example: LLM Module

Complete flow of a chat request through the three layers:

```
1. ChatController (API layer)
   - Receives POST /api/v1/chat with ChatRequestDto
   - Maps ChatRequestDto -> ChatRequest (domain model)
   - Invokes chatUseCase.chat(request)

2. LlmGatewayService (Domain layer, implements ChatUseCase)
   - Selects the provider through domain logic
   - Invokes llmClient.generate(request) through the port out
   - If error, applies retry and fallback (domain logic)
   - Invokes costTrackingService.track(usage)
   - Returns ChatResponse

3. OllamaLlmAdapter (Infrastructure layer, implements LlmClient)
   - Receives the request from the domain service through the port
   - Uses Spring AI OllamaChatModel to communicate with Ollama
   - Builds the response and returns it as ChatResponse (domain model)
```

---

## 7. Practical Example: Document Module

Complete document ingestion flow:

```
1. DocumentController (API layer)
   - Receives POST /api/v1/documents/upload with multipart file
   - Invokes documentIngestionUseCase.ingest(content, filename, contentType)

2. DocumentService (Domain layer, implements DocumentIngestionUseCase)
   - Computes SHA-256 hash of the file (domain logic)
   - Verifies deduplication via documentRepository.existsByHash(hash)
   - Creates Document entity with PENDING status
   - Saves via documentRepository.save(document)

3. DocumentIngestionJobConfig (Batch layer)
   - Spring Batch job processes PENDING documents
   - For each document:
     a. textExtractorPort.extract(content) -> TikaTextExtractor
     b. chunkingService.chunk(text) -> ChunkingService (domain)
     c. vectorStorePort.store(chunks) -> QdrantVectorStoreAdapter
     d. documentRepository.updateStatus(INDEXED)
```
