<!-- refreshed: 2026-06-29 -->
# Architecture

**Analysis Date:** 2026-06-29

## System Overview

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                        Angular 21 Frontend                               │
│  `localmind-frontend/src/app/`                                           │
│  features/: chat, documents, search, mcp, settings, calendar, email...  │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │ HTTP REST /api/v1/
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     localmind-api (REST Layer)                           │
│  `localmind-backend/localmind-api/src/main/java/com/localmind/api/`     │
│  Controllers at /api/v1/* │ DTOs │ GlobalExceptionHandler               │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │ Calls port/in/ interfaces
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│               localmind-domain (Business Logic — Pure Java)             │
│  `localmind-backend/localmind-domain/src/main/java/com/localmind/domain/`│
│  Domains: llm │ document │ mcp │ auth │ automation │ messaging           │
│           calendar │ email │ knowledge │ finetuning │ marketplace         │
│           common │ plugin │ agent                                         │
└──────┬───────────────────────────────────────────────────┬──────────────┘
       │ port/out/ interfaces                              │
       ▼                                                   ▼
┌──────────────────────────┐              ┌───────────────────────────────┐
│  localmind-infrastructure│              │    localmind-batch            │
│  JPA adapters │ LLM adapters│           │  Spring Batch document        │
│  VectorStore │ Security    │            │  ingestion + folder scan jobs │
│  `infrastructure/...`     │            │  `localmind-batch/...`        │
└──────────────────────────┘              └───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────────────────────┐
│  External Stores & Services                                              │
│  MySQL 8.0 (localmind DB) │ Qdrant (vector store) │ Ollama / OpenAI     │
│  Anthropic / DeepSeek / Mistral / XAI / Google                          │
└──────────────────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | Location |
|-----------|----------------|----------|
| `localmind-shared-types` | Shared contracts: PageRequest, PageResponse, ErrorResponse, ServiceInfo | `localmind-backend/localmind-shared-types/` |
| `localmind-domain` | Pure Java business logic, domain models, port interfaces | `localmind-backend/localmind-domain/` |
| `localmind-plugin-api` | External plugin extension points (LocalMindPlugin, DocumentParserExtension) | `localmind-backend/localmind-plugin-api/` |
| `localmind-infrastructure` | Spring adapters: JPA, LLM providers, Qdrant, text extraction, security, DomainConfig | `localmind-backend/localmind-infrastructure/` |
| `localmind-api` | REST controllers, DTOs, GlobalExceptionHandler | `localmind-backend/localmind-api/` |
| `localmind-batch` | Spring Batch jobs: document ingestion, folder scanning | `localmind-backend/localmind-batch/` |
| `localmind-app` | Spring Boot entry point, Flyway migrations V1–V78, profiles | `localmind-backend/localmind-app/` |
| Angular frontend | Feature-driven SPA with lazy-loaded routes, Signal stores | `localmind-frontend/src/app/` |

## Pattern Overview

**Backend:** Hexagonal Architecture (Ports & Adapters), organized as a Maven multi-module project

**Frontend:** Feature-Driven Architecture with Angular Signals for state management

**Key Characteristics:**
- Domain layer has zero framework dependencies (no Spring annotations in `localmind-domain`)
- Domain services are wired as `@Bean` methods in `DomainConfig.java` — the only place Spring touches domain code
- Infrastructure adapters are `@Component` classes implementing domain port interfaces
- Frontend features are independently lazy-loaded via `loadChildren` in `app.routes.ts`
- All code and documentation is bilingual (Italian/English)

## Layers

**Domain Layer:**
- Purpose: Pure business logic; defines what the system does
- Location: `localmind-backend/localmind-domain/src/main/java/com/localmind/domain/`
- Contains: Domain models (plain Java classes), `port/in/` use case interfaces, `port/out/` repository/adapter interfaces, `service/` implementations
- Depends on: Nothing external (plain Java only, Lombok permitted)
- Used by: `localmind-infrastructure` (via port interfaces), `localmind-api` (via port/in/ interfaces), `localmind-batch`

**Infrastructure Layer:**
- Purpose: Framework adapters bridging domain ports to external systems
- Location: `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/`
- Contains: JPA entities (`persistence/entity/`), repository adapters (`persistence/adapter/`), LLM adapters (`llm/adapter/`), vector store adapter (`vectorstore/adapter/`), document processing (`document/adapter/`), domain event publishing (`event/`), Spring Security config, `DomainConfig.java`
- Depends on: `localmind-domain`, Spring Boot, Spring AI, Spring Data JPA, Spring Security
- Used by: `localmind-app`

**API Layer:**
- Purpose: HTTP interface — receives requests, delegates to domain use cases, returns responses
- Location: `localmind-backend/localmind-api/src/main/java/com/localmind/api/`
- Contains: `@RestController` classes per domain (chat, documents, search, mcp, settings, etc.), DTOs (Request/Response), mappers, `GlobalExceptionHandler`
- Depends on: `localmind-domain` (port/in interfaces only), `localmind-shared-types`
- Used by: Angular frontend

**Batch Layer:**
- Purpose: Scheduled document ingestion and folder scanning
- Location: `localmind-backend/localmind-batch/src/main/java/com/localmind/batch/`
- Contains: `document/job/` (Spring Batch job configs), `document/step/`, `folder/job/`, `folder/step/`, `scheduler/`
- Depends on: `localmind-domain`, Spring Batch

**Frontend Core:**
- Purpose: Cross-cutting Angular infrastructure
- Location: `localmind-frontend/src/app/core/`
- Contains: `services/api.service.ts` (base HTTP wrapper), `services/auth.service.ts`, `guards/auth.guard.ts`, `interceptors/auth.interceptor.ts`, `i18n/translate.pipe.ts`, `models/`

**Frontend Features:**
- Purpose: Business feature areas, independently lazy-loaded
- Location: `localmind-frontend/src/app/features/`
- Contains: Per-feature directories with `pages/`, `components/`, `services/`, `models/`, `state/` (when Signal store is present)

## Data Flow

### Chat Request (Standard)

1. User submits message — `ChatPageComponent` (`localmind-frontend/src/app/features/chat/pages/chat-page/`)
2. `ChatStore` updates Signal state, calls `ChatService`
3. HTTP POST `/api/v1/chat` via `ApiService` (`core/services/api.service.ts`)
4. `ChatController.chat()` (`localmind-api/src/main/java/com/localmind/api/chat/controller/`)
5. Calls `ChatUseCase.chat(LlmRequest)` (domain port/in interface)
6. `LlmGatewayService.chat()` (`localmind-domain/.../llm/service/LlmGatewayService.java`) routes to provider
7. `LlmClient.call()` adapter (e.g., `OllamaLlmAdapter`) sends request to Ollama/cloud provider
8. Response + token usage tracked, `ConversationCompletedEvent` published via `DomainEventPublisherPort`
9. `SpringDomainEventPublisher` → `ConversationEventListener` (infrastructure) handles side effects

### Chat Streaming

1. HTTP GET (SSE) `/api/v1/chat/stream`
2. `LlmGatewayService.streamChat()` routes to `StreamingLlmClient` adapter
3. Tokens pushed via `Consumer<String>` callback; frontend `EventSource` appends to `ChatStore._messages` Signal via `appendTokenToLastMessage()`

### Document Ingestion

1. File uploaded via `DocumentController.upload()` or detected by folder watcher (batch)
2. `DocumentIngestionPipelineService` orchestrates: text extraction (Tika/Tesseract) → chunking (`ChunkingService`) → embedding → vector storage (`QdrantVectorStoreAdapter`)
3. Document and chunk metadata persisted in MySQL via JPA adapters

### LLM Provider Fallback Chain

1. `LlmGatewayService.chat()` resolves preferred provider from `LlmRequest.provider` or `defaultProvider`
2. Builds provider chain: `[preferred, ...fallbackOrder]` (default: OLLAMA → OPENAI → ANTHROPIC → GOOGLE)
3. Iterates chain — skips unavailable providers (`client.isAvailable()`)
4. First successful response returned; usage recorded via `LlmUsageRepository`

**State Management (Frontend):**
- `ChatStore` — Angular Signals (`localmind-frontend/src/app/features/chat/state/chat.store.ts`) — full conversation state
- Other features — RxJS Observables returned from feature-specific services

## Key Abstractions

**Domain Ports (in):**
- Purpose: Use case interfaces that controllers call; define what the application can do
- Examples: `ChatUseCase`, `StreamingChatUseCase`, `DocumentIngestionUseCase`, `FolderManagementUseCase`, `McpServerManagementUseCase`
- Location: `localmind-domain/src/main/java/com/localmind/domain/{domain}/port/in/`
- Pattern: Java interfaces implemented by domain services

**Domain Ports (out):**
- Purpose: Repository/adapter interfaces that domain services depend on; define what the domain needs
- Examples: `LlmClient`, `StreamingLlmClient`, `VectorStorePort`, `DocumentRepository`, `ConversationRepository`, `DomainEventPublisherPort`
- Location: `localmind-domain/src/main/java/com/localmind/domain/{domain}/port/out/`
- Pattern: Java interfaces implemented by infrastructure adapters annotated with `@Component`

**LlmClient / StreamingLlmClient:**
- Purpose: Abstraction over a single LLM provider
- Key method: `LlmProvider getProvider()`, `boolean isAvailable()`, `LlmResponse call(LlmRequest)`
- Implementations: `OllamaLlmAdapter`, `OpenAiLlmAdapter`, `AnthropicLlmAdapter`, `DeepSeekLlmAdapter`, `MistralLlmAdapter`, `XaiLlmAdapter`
- Location: `localmind-infrastructure/src/main/java/com/localmind/infrastructure/llm/adapter/`

**ApiService (Frontend):**
- Purpose: Thin wrapper over `HttpClient`, prepends `environment.apiUrl`
- Location: `localmind-frontend/src/app/core/services/api.service.ts`
- Pattern: All feature services inject `ApiService` and call `.get<T>()`, `.post<T>()`, etc.

**ChatStore (Frontend):**
- Purpose: Signal-based reactive store for entire chat session state
- Location: `localmind-frontend/src/app/features/chat/state/chat.store.ts`
- Pattern: `private _field = signal<T>(...)` + `readonly field = this._field.asReadonly()` + mutating methods like `addMessage()`, `appendTokenToLastMessage()`

## Entry Points

**Backend Boot:**
- Location: `localmind-backend/localmind-app/src/main/java/com/localmind/LocalMindApplication.java`
- Triggers: `mvn -pl localmind-app spring-boot:run -Dspring-boot.run.profiles=dev`
- Responsibilities: Starts Spring context, triggers Flyway migrations, enables scheduling

**Backend Domain Wiring:**
- Location: `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/config/DomainConfig.java`
- Triggers: Spring context initialization
- Responsibilities: Instantiates all domain services as `@Bean`s, injecting port interfaces provided by infrastructure `@Component` adapters

**Frontend Entry:**
- Location: `localmind-frontend/src/main.ts` → `localmind-frontend/src/app/app.routes.ts`
- Triggers: `npm start` / Angular build
- Responsibilities: Bootstraps Angular app, defines all lazy-loaded routes

**REST API Routes:**
- Location: `localmind-backend/localmind-api/src/main/java/com/localmind/api/*/controller/*.java`
- All under: `/api/v1/`
- Auth: JWT-like token via `LocalAuthFilter`, all `/api/v1/**` permitAll in SecurityConfig (token validated by filter)

## Architectural Constraints

- **Threading:** Spring async pool (`AsyncConfig.java`) handles async operations; streaming uses reactive patterns via `Consumer<String>` callbacks in `LlmGatewayService`
- **Global state:** `DomainConfig` is a singleton Spring `@Configuration`; `ChatStore` is `providedIn: 'root'` (global singleton in Angular); `LlmGatewayService` holds the `clients` map as instance state
- **Circular imports:** None documented; `AnalyticsService` in `common` domain directly imports from `document` and `llm` domain ports — documented as a known boundary violation in `MODULE_BOUNDARIES.md`
- **Domain isolation:** Domain services must never import `org.springframework.*`; any Spring-aware code belongs in infrastructure or API modules
- **Flyway constraint:** Each migration SQL file must contain only one SQL statement (per CLAUDE.md rule)
- **UUID mapping:** All JPA entity `@Id` UUID fields require `@JdbcTypeCode(SqlTypes.CHAR)` to work with MySQL `CHAR(36)` columns
- **LLM provider conditional:** Each `LlmAdapter` uses `@ConditionalOnProperty(name = "localmind.llm.{provider}.enabled", havingValue = "true")` — adapters only register when enabled

## Anti-Patterns

### Using Spring Annotations in Domain Services

**What happens:** Adding `@Service`, `@Component`, or `@Autowired` directly to classes in `localmind-domain`
**Why it's wrong:** Domain module has no Spring dependency by design. Adding Spring annotations creates a circular coupling between the core business logic and the framework, and breaks testability
**Do this instead:** Register domain services exclusively as `@Bean` methods in `DomainConfig.java` (`localmind-infrastructure/src/main/java/com/localmind/infrastructure/config/DomainConfig.java`)

### Calling Domain Repositories Directly from Controllers

**What happens:** Injecting `DocumentRepository` or `ConversationRepository` (domain port interfaces) into API controllers
**Why it's wrong:** Controllers should only know about port/in use case interfaces, not the underlying repositories; this bypasses service business logic and event publishing
**Do this instead:** Controllers inject only port/in interfaces (e.g., `ChatUseCase`, `DocumentIngestionUseCase`); the service layer handles orchestration

### Cross-Domain Direct Imports in Domain Layer

**What happens:** A domain service in one package imports model classes from another domain package (e.g., `AnalyticsService` in `common` importing from `document` and `llm`)
**Why it's wrong:** Creates tight coupling that prevents microservice extraction (documented in `localmind-backend/MODULE_BOUNDARIES.md`)
**Do this instead:** Introduce an output port (e.g., `AnalyticsDataPort`) in the `common` domain with a dedicated adapter in infrastructure

## Error Handling

**Strategy:** Centralized exception translation in the API layer; domain exceptions are plain Java classes

**Patterns:**
- Domain throws typed exceptions: `ResourceNotFoundException`, `LlmProviderException`, `DocumentProcessingException`, `AuthenticationException`
- All exceptions are defined in `localmind-domain/src/main/java/com/localmind/domain/common/exception/`
- `GlobalExceptionHandler` (`localmind-api/src/main/java/com/localmind/api/common/advice/GlobalExceptionHandler.java`) catches each type and returns `ErrorResponseDto` with appropriate HTTP status
- Frontend `authInterceptor` catches 401 → triggers `authService.logout()`
- No silent swallowing; all handlers log before responding

## Cross-Cutting Concerns

**Logging:** SLF4J via Lombok `@Slf4j` in API and infrastructure layers; config at `localmind-app/src/main/resources/logback-spring.xml`; domain layer does not log directly
**Validation:** Bean Validation (`@Valid`) on REST DTOs; `MethodArgumentNotValidException` handled by `GlobalExceptionHandler`
**Authentication:** Custom `LocalAuthFilter` (JWT-like token); stateless sessions (`SessionCreationPolicy.STATELESS`); frontend `authInterceptor` appends `Authorization: Bearer <token>` header
**i18n:** Frontend uses custom `TranslatePipe` + language switcher; backend enums must expose both Italian and English labels per CLAUDE.md rules
**Caching:** `CacheConfig.java` in infrastructure (Spring Cache); `DocumentStatsCacheEntity` for document stats
**Domain Events:** Published via `DomainEventPublisherPort` → `SpringDomainEventPublisher` → Spring `@EventListener` handlers in `infrastructure/event/listener/`

---

*Architecture analysis: 2026-06-29*
