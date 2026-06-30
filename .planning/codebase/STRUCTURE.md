# Codebase Structure

**Analysis Date:** 2026-06-29

## Directory Layout

```
localmind/                              # Project root
├── localmind-backend/                  # Spring Boot multi-module Maven project
│   ├── pom.xml                         # Parent POM, module declarations
│   ├── MODULE_BOUNDARIES.md            # Cross-module violation docs & microservice roadmap
│   ├── plugins/                        # Runtime plugin JARs (loaded by PluginManagementService)
│   ├── localmind-shared-types/         # Shared types: PageRequest, PageResponse, ServiceInfo
│   ├── localmind-domain/               # Pure Java business logic (zero Spring)
│   ├── localmind-plugin-api/           # External plugin API contracts
│   ├── localmind-infrastructure/       # Spring adapters (JPA, LLM, Qdrant, security)
│   ├── localmind-api/                  # REST controllers + DTOs
│   ├── localmind-batch/                # Spring Batch document/folder jobs
│   └── localmind-app/                  # Boot entry point, Flyway migrations, app profiles
├── localmind-frontend/                 # Angular 21 SPA
│   └── src/app/
│       ├── core/                       # Guards, interceptors, services, i18n, models
│       ├── features/                   # Lazy-loaded feature modules
│       ├── layout/                     # App shell: sidebar + header
│       └── shared/                     # Reusable components, pipes, validators
├── localmind-sdk-java/                 # Java client SDK
├── localmind-sdk-js/                   # JavaScript client SDK
├── localmind-sdk-python/               # Python client SDK
├── docs-site/                          # Angular documentation site (standalone)
├── documentation/                      # Markdown docs (English)
├── documentazione/                     # Markdown docs (Italian)
├── Sviluppi/                           # Dev progress logs (YYYY-MM-DD_NN_FeatureName.md)
├── docker/                             # Grafana, Nginx, Prometheus configs
├── nginx/                              # Nginx reverse proxy config
├── scripts/                            # start-backend.sh, start-frontend.sh, start-all.sh
├── docker-compose.yml                  # Standard compose: MySQL, Qdrant
├── docker-compose.microservices.yml    # Future microservices compose
├── Dockerfile                          # Backend container
├── Dockerfile.frontend                 # Frontend container
└── .planning/codebase/                 # GSD codebase maps (this directory)
```

## Domain Module Internal Layout

Every domain inside `localmind-domain` follows this exact pattern:

```
localmind-domain/src/main/java/com/localmind/domain/{domain}/
├── model/          # Plain Java classes: domain entities, value objects, enums
├── port/
│   ├── in/         # Use case interfaces (called by controllers/batch)
│   └── out/        # Repository & adapter interfaces (implemented by infrastructure)
└── service/        # Business logic; implements port/in interfaces; depends on port/out interfaces
```

**Active domains:** `agent`, `auth`, `automation`, `calendar`, `common`, `document`, `email`, `finetuning`, `knowledge`, `llm`, `marketplace`, `mcp`, `messaging`, `plugin`

`common` domain contains: `event/` (DomainEvent types), `exception/` (typed exceptions), `model/`, `port/in/`, `port/out/`, `service/` (AnalyticsService, BackupService)

## Infrastructure Module Internal Layout

```
localmind-infrastructure/src/main/java/com/localmind/infrastructure/
├── config/
│   ├── DomainConfig.java               # @Bean wiring for ALL domain services
│   ├── SecurityConfig.java             # Spring Security filter chain
│   ├── EmbeddingConfig.java            # @Primary Ollama EmbeddingModel
│   ├── AsyncConfig.java                # Async thread pool
│   ├── CacheConfig.java                # Spring Cache config
│   ├── OpenApiConfig.java              # Swagger/OpenAPI
│   └── WebMvcConfig.java               # CORS and MVC setup
├── persistence/
│   ├── entity/                         # JPA entities (CHAR(36) UUID mapping)
│   │   ├── {EntityName}Entity.java
│   │   ├── mcp/                        # MCP-specific JPA entities
│   │   ├── automation/
│   │   └── messaging/
│   ├── adapter/                        # Repository adapters implementing domain port/out
│   │   ├── automation/
│   │   ├── mcp/
│   │   └── messaging/
│   ├── repository/                     # Spring Data JPA repositories (interfaces)
│   │   ├── automation/
│   │   ├── mcp/
│   │   └── messaging/
│   ├── mapper/                         # Entity ↔ domain model mappers
│   └── sync/                           # Sync utilities
├── llm/
│   ├── adapter/                        # LLM provider adapters
│   │   ├── OllamaLlmAdapter.java
│   │   ├── OllamaStreamingLlmAdapter.java
│   │   ├── OllamaMultimodalAdapter.java
│   │   ├── OllamaModelAdapter.java
│   │   ├── OpenAiLlmAdapter.java
│   │   ├── OpenAiStreamingLlmAdapter.java
│   │   ├── OpenAiMultimodalAdapter.java
│   │   ├── AnthropicLlmAdapter.java
│   │   ├── AnthropicStreamingLlmAdapter.java
│   │   ├── DeepSeekLlmAdapter.java
│   │   ├── DeepSeekStreamingLlmAdapter.java
│   │   ├── MistralLlmAdapter.java
│   │   ├── MistralStreamingLlmAdapter.java
│   │   ├── XaiLlmAdapter.java
│   │   ├── XaiStreamingLlmAdapter.java
│   │   ├── WhisperTranscriptionAdapter.java
│   │   ├── ConversationExportAdapter.java
│   │   └── ConversationImportAdapter.java
│   └── config/                         # LLM-specific Spring configs
├── document/
│   ├── adapter/
│   │   ├── TikaTextExtractor.java
│   │   ├── TesseractOcrExtractor.java
│   │   └── LocalFileSystemScanner.java
│   └── config/
├── vectorstore/
│   ├── adapter/
│   │   └── QdrantVectorStoreAdapter.java
│   └── config/
├── event/
│   ├── adapter/
│   │   └── SpringDomainEventPublisher.java
│   ├── config/
│   └── listener/
│       ├── AnalyticsEventListener.java
│       ├── ConversationEventListener.java
│       ├── DocumentEventListener.java
│       └── WebhookEventListener.java
├── security/                           # LocalAuthFilter, password hasher
├── mcp/                                # MCP server discovery, security, persistence, service
├── {domain}/adapter/                   # Per-domain adapters (automation, backup, calendar, email, etc.)
└── {domain}/config/
```

## API Module Internal Layout

```
localmind-api/src/main/java/com/localmind/api/
├── common/
│   ├── advice/
│   │   └── GlobalExceptionHandler.java # @RestControllerAdvice for all domains
│   └── dto/
│       └── ErrorResponseDto.java
├── config/
├── {domain}/
│   ├── controller/                     # @RestController mapped to /api/v1/{domain}
│   ├── dto/                            # Request and Response DTOs
│   └── mapper/                         # (present in document/, llm/ domains)
├── agent/controller/ + dto/
├── auth/controller/ + dto/
├── automation/controller/ + dto/
├── backup/controller/ + dto/
├── calendar/controller/ + dto/
├── dashboard/controller/ + dto/
├── document/controller/ + dto/ + mapper/
├── email/controller/ + dto/
├── finetuning/controller/ + dto/
├── knowledge/controller/ + dto/
├── llm/controller/ + dto/ + mapper/
├── marketplace/controller/ + dto/
├── mcp/controller/ + dto/
├── messaging/controller/ + dto/
├── plugin/controller/ + dto/
└── settings/controller/ + dto/
```

## Batch Module Internal Layout

```
localmind-batch/src/main/java/com/localmind/batch/
├── document/
│   ├── job/
│   │   └── DocumentIngestionJobConfig.java
│   ├── step/                           # Step implementations
│   └── listener/
│       └── DocumentJobListener.java
├── folder/
│   ├── job/
│   │   └── FolderScanJobConfig.java
│   └── step/
└── scheduler/                          # Quartz/Spring Scheduler triggers
```

## App Module Internal Layout

```
localmind-app/src/main/java/com/localmind/
│   └── LocalMindApplication.java       # @SpringBootApplication @EnableScheduling
localmind-app/src/main/resources/
├── application.yml                     # Base config
├── application-dev.yml                 # Dev profile (excludes OpenAI/Anthropic auto-config when keys empty)
├── application-prod.yml                # Production profile
├── logback-spring.xml                  # Logging config
└── db/migration/
    └── V{N}__{description}.sql         # Flyway migrations (currently V1–V78, one SQL per file)
```

## Frontend Structure

```
localmind-frontend/src/app/
├── app.routes.ts                       # Root routes: login + layout shell with all feature children
├── app.config.ts                       # Angular providers: HTTP, router, animations, i18n
├── core/
│   ├── guards/
│   │   └── auth.guard.ts               # Route guard protecting all non-login routes
│   ├── interceptors/
│   │   └── auth.interceptor.ts         # Appends Bearer token; handles 401 → logout
│   ├── services/
│   │   ├── api.service.ts              # Base HTTP wrapper (get/post/put/patch/delete)
│   │   ├── auth.service.ts             # Login, logout, token storage
│   │   └── ollama-download.service.ts  # Ollama model pull status
│   ├── i18n/
│   │   └── translate.pipe.ts           # Custom translation pipe
│   └── models/                         # Shared TypeScript interfaces
├── features/
│   ├── auth/pages/login-page/          # Login page (not behind auth guard)
│   ├── backup/
│   │   ├── backup.routes.ts
│   │   ├── pages/backup-page/
│   │   └── services/
│   ├── calendar/
│   │   ├── calendar.routes.ts
│   │   ├── models/
│   │   ├── pages/calendar-page/
│   │   └── services/
│   ├── channels/                       # Messaging channels
│   ├── chat/
│   │   ├── chat.routes.ts
│   │   ├── components/                 # chat-input, chat-message, chat-sidebar, model-selector
│   │   ├── models/
│   │   ├── pages/chat-page/
│   │   ├── services/
│   │   └── state/
│   │       └── chat.store.ts           # Signal-based store (the primary state pattern)
│   ├── dashboard/
│   ├── documents/
│   │   ├── components/                 # document-card, document-upload
│   │   ├── pages/document-list-page/
│   │   ├── services/
│   │   └── state/                      # RxJS-based (no Signal store)
│   ├── email/
│   ├── finetuning/
│   ├── folders/
│   ├── guide/
│   ├── knowledge/
│   ├── marketplace/
│   ├── mcp/                            # pages/: nested routes for servers + tools
│   ├── not-found/
│   ├── plugins/
│   ├── search/
│   │   └── components/                 # search-bar, search-result
│   └── settings/
│       ├── components/llm-config/
│       └── pages/
│           ├── settings-page/
│           └── webhooks-page/
├── layout/
│   ├── layout.component.ts             # Shell with collapsible sidebar + router outlet
│   ├── header/
│   └── sidebar/
└── shared/
    ├── components/
    │   ├── empty-state/
    │   ├── field-error/
    │   ├── language-switcher/
    │   ├── loading-skeleton/
    │   ├── loading-spinner/
    │   └── theme-toggle/
    ├── pipes/
    └── validators/
```

## Key File Locations

**Entry Points:**
- `localmind-backend/localmind-app/src/main/java/com/localmind/LocalMindApplication.java`: Spring Boot main
- `localmind-frontend/src/app/app.routes.ts`: All Angular routes
- `localmind-frontend/src/main.ts`: Angular bootstrap

**Core Configuration:**
- `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/config/DomainConfig.java`: All domain service bean wiring
- `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/config/SecurityConfig.java`: Spring Security + CORS
- `localmind-backend/localmind-app/src/main/resources/application-dev.yml`: Dev profile config
- `localmind-frontend/src/environments/environment.ts`: API base URL

**LLM Routing:**
- `localmind-backend/localmind-domain/src/main/java/com/localmind/domain/llm/service/LlmGatewayService.java`: Provider fallback chain logic
- `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/llm/adapter/`: All LLM provider adapters

**Database Migrations:**
- `localmind-backend/localmind-app/src/main/resources/db/migration/V{N}__*.sql`: Flyway scripts (V1–V78)

**REST Error Handling:**
- `localmind-backend/localmind-api/src/main/java/com/localmind/api/common/advice/GlobalExceptionHandler.java`

**Frontend State:**
- `localmind-frontend/src/app/features/chat/state/chat.store.ts`: Primary Signal store example
- `localmind-frontend/src/app/core/services/api.service.ts`: HTTP base service

## Naming Conventions

**Backend Files:**
- Domain models: `{ConceptName}.java` (e.g., `LlmProviderConfig.java`, `Document.java`)
- JPA entities: `{ConceptName}Entity.java` (e.g., `DocumentEntity.java`, `ConversationEntity.java`)
- Port interfaces (in): `{Action}UseCase.java` (e.g., `ChatUseCase.java`, `DocumentIngestionUseCase.java`)
- Port interfaces (out): `{Resource}Repository.java` or `{Resource}Port.java`
- Domain services: `{Domain}Service.java` (e.g., `LlmGatewayService.java`, `DocumentService.java`)
- Infrastructure adapters: `{Provider}{Capability}Adapter.java` (e.g., `OllamaLlmAdapter.java`, `QdrantVectorStoreAdapter.java`)
- REST controllers: `{Domain}Controller.java`
- DTOs: `{Action}RequestDto.java` / `{Action}ResponseDto.java`

**Frontend Files:**
- Components: `{name}.component.ts` (kebab-case directory)
- Services: `{name}.service.ts`
- Stores: `{name}.store.ts`
- Routes: `{feature}.routes.ts` exporting `{FEATURE}_ROUTES`
- Models: `{name}.model.ts`

**Directories:**
- Backend: snake-case Maven module names (`localmind-domain`, `localmind-infrastructure`)
- Frontend features: kebab-case (`chat`, `documents`, `mcp`)
- Frontend feature pages: `{name}-page/` directory
- Frontend feature components: `{name}-{type}/` directory (e.g., `chat-input/`, `document-card/`)

## Where to Add New Code

**New Backend Domain Feature:**
1. Add domain package in `localmind-domain/src/main/java/com/localmind/domain/{newdomain}/`
   - `model/` → domain entities
   - `port/in/` → use case interface
   - `port/out/` → repository interface
   - `service/` → service implementation (no Spring annotations)
2. Add `@Bean` registration in `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/config/DomainConfig.java`
3. Add JPA entity in `localmind-infrastructure/src/main/java/com/localmind/infrastructure/persistence/entity/`
4. Add repository adapter in `localmind-infrastructure/src/main/java/com/localmind/infrastructure/persistence/adapter/`
5. Add controller + DTOs in `localmind-api/src/main/java/com/localmind/api/{newdomain}/`
6. Add Flyway migration in `localmind-app/src/main/resources/db/migration/V{N+1}__{description}.sql` (one SQL per file)

**New LLM Provider:**
1. Add enum value in `localmind-domain/src/main/java/com/localmind/domain/llm/model/LlmProvider.java`
2. Add `{Provider}LlmAdapter.java` in `localmind-infrastructure/src/main/java/com/localmind/infrastructure/llm/adapter/` implementing `LlmClient` + `@ConditionalOnProperty`
3. Optionally add `{Provider}StreamingLlmAdapter.java` implementing `StreamingLlmClient`
4. Update `application.yml` with `localmind.llm.{provider}.enabled` property

**New Frontend Feature:**
1. Create `localmind-frontend/src/app/features/{name}/` with:
   - `{name}.routes.ts` exporting `{NAME}_ROUTES`
   - `pages/{name}-page/{name}-page.component.ts`
   - `services/{name}.service.ts` injecting `ApiService`
   - `models/{name}.model.ts`
2. Register route in `localmind-frontend/src/app/app.routes.ts` as `loadChildren`
3. Add nav link in `localmind-frontend/src/app/layout/layout.component.ts`

**New Shared Frontend Component:**
- Place in `localmind-frontend/src/app/shared/components/{component-name}/`
- Use standalone component pattern (no NgModule)

**New API Endpoint on Existing Domain:**
- Add method to existing controller in `localmind-api/src/main/java/com/localmind/api/{domain}/controller/`
- Add corresponding use case method to port/in interface in `localmind-domain`
- Implement in domain service

## Special Directories

**`localmind-backend/plugins/`:**
- Purpose: Runtime plugin JARs dropped here for dynamic loading by `PluginManagementService`
- Generated: No
- Committed: No (empty placeholder)

**`localmind-app/src/main/resources/db/migration/`:**
- Purpose: Flyway SQL migrations, auto-applied on startup
- Generated: No
- Committed: Yes — each file is immutable once merged

**`localmind-frontend/src/environments/`:**
- Purpose: Angular build-time environment config (`apiUrl` for dev vs prod)
- Generated: No
- Committed: Yes (no secrets)

**`Sviluppi/`:**
- Purpose: Development progress logs in format `YYYY-MM-DD_NN_FeatureName.md`; required by CLAUDE.md before starting any task
- Generated: No
- Committed: Yes

**`.planning/codebase/`:**
- Purpose: GSD codebase maps (this directory) consumed by `/gsd:plan-phase` and `/gsd:execute-phase`
- Generated: Yes (by `/gsd:map-codebase`)
- Committed: Yes

---

*Structure analysis: 2026-06-29*
