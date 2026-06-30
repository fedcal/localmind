# External Integrations

**Analysis Date:** 2026-06-29

## LLM Providers

**Ollama (local, default):**
- Purpose: Local LLM inference — chat, embeddings, multimodal
- SDK/Client: `spring-ai-starter-model-ollama` (Spring AI 1.0.0)
- Adapters: `OllamaLlmAdapter`, `OllamaStreamingLlmAdapter`, `OllamaMultimodalAdapter`, `OllamaModelAdapter`
  - Location: `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/llm/adapter/`
- Base URL: `http://${OLLAMA_HOST:localhost}:${OLLAMA_PORT:11434}`
- Default chat model: `${OLLAMA_CHAT_MODEL:llama3.2}`
- Default embedding model: `${OLLAMA_EMBED_MODEL:nomic-embed-text}` — marked `@Primary` in `EmbeddingConfig.java`
- Enabled: `${LLM_OLLAMA_ENABLED:true}`
- Optional GPU support via Docker Compose NVIDIA deploy config (commented out in `docker-compose.yml`)

**OpenAI:**
- Purpose: Cloud LLM inference — chat, multimodal (GPT-4o)
- SDK/Client: `spring-ai-starter-model-openai`
- Adapters: `OpenAiLlmAdapter`, `OpenAiStreamingLlmAdapter`, `OpenAiMultimodalAdapter`
  - Location: `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/llm/adapter/`
- Auth: `OPENAI_API_KEY` env var
- Default model: `${OPENAI_MODEL:gpt-4o}`
- Enabled: `${LLM_OPENAI_ENABLED:false}` — auto-config excluded in dev profile when key is empty
- Whisper transcription: `WhisperTranscriptionAdapter` (enabled via `${WHISPER_ENABLED:false}`)

**Anthropic:**
- Purpose: Cloud LLM inference — chat (Claude)
- SDK/Client: `spring-ai-starter-model-anthropic`
- Adapters: `AnthropicLlmAdapter`, `AnthropicStreamingLlmAdapter`
  - Location: `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/llm/adapter/`
- Auth: `ANTHROPIC_API_KEY` env var
- Default model: `${ANTHROPIC_MODEL:claude-sonnet-4-20250514}`
- Enabled: `${LLM_ANTHROPIC_ENABLED:false}` — auto-config excluded in dev profile when key is empty

**Mistral AI:**
- Purpose: Cloud LLM inference — chat and embeddings
- SDK/Client: `spring-ai-starter-model-mistral-ai`
- Adapters: `MistralLlmAdapter`, `MistralStreamingLlmAdapter`
  - Location: `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/llm/adapter/`
- Auth: `MISTRAL_API_KEY` env var
- Default model: `${MISTRAL_MODEL:mistral-large-latest}`
- Enabled: `${LLM_MISTRAL_ENABLED:false}` — auto-config excluded in dev profile

**DeepSeek:**
- Purpose: Cloud LLM inference — chat (OpenAI-compatible API)
- SDK/Client: OpenAI-compatible HTTP adapter (custom)
- Adapters: `DeepSeekLlmAdapter`, `DeepSeekStreamingLlmAdapter`
  - Location: `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/llm/adapter/`
- Auth: `DEEPSEEK_API_KEY` env var
- Base URL: `${DEEPSEEK_BASE_URL:https://api.deepseek.com}`
- Enabled: `${LLM_DEEPSEEK_ENABLED:false}`

**xAI Grok:**
- Purpose: Cloud LLM inference — chat (OpenAI-compatible API)
- SDK/Client: OpenAI-compatible HTTP adapter (custom)
- Adapters: `XaiLlmAdapter`, `XaiStreamingLlmAdapter`
  - Location: `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/llm/adapter/`
- Auth: `XAI_API_KEY` env var
- Base URL: `${XAI_BASE_URL:https://api.x.ai/v1}`
- Enabled: `${LLM_XAI_ENABLED:false}`

**Google (planned, not yet implemented):**
- Provider enum value defined: `LlmProvider.GOOGLE` at `localmind-backend/localmind-domain/src/main/java/com/localmind/domain/llm/model/LlmProvider.java`
- Auth env var defined: `GOOGLE_API_KEY` in `.env.example`
- No concrete adapter class exists yet in `localmind-infrastructure/src/.../llm/adapter/`
- Enabled: `${LLM_GOOGLE_ENABLED:false}`

**LLM Routing:**
- `LlmGatewayService` in `localmind-domain` routes requests with fallback chain
- Fallback order (configurable): `OLLAMA,OPENAI,ANTHROPIC,GOOGLE,DEEPSEEK,MISTRAL,XAI`
- Config: `localmind.llm.fallback.order` in `application-dev.yml`
- Provider configs also stored in database table `llm_provider_configs` (V7 migration), editable via settings UI

## Data Storage

**Databases:**

MySQL 8.0:
- Connection: `jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:localmind}`
- Auth: `DB_USERNAME` / `DB_PASSWORD` env vars
- Client: Spring Data JPA (Hibernate 6), entities in `localmind-infrastructure/src/main/java/com/localmind/infrastructure/persistence/entity/`
- UUID mapping: all `@Id` UUID fields use `@JdbcTypeCode(SqlTypes.CHAR)` for `CHAR(36)` compatibility
- Schema migration: Flyway (V1–V78 as of analysis date), scripts at `localmind-app/src/main/resources/db/migration/`
- Each migration file contains exactly one SQL statement (project rule)
- Docker: `mysql:8.0` image, volume `localmind-mysql-data`, container `localmind-mysql`

Qdrant (vector store):
- Purpose: Stores document embeddings for semantic search
- Connection: `${QDRANT_HOST:localhost}:${QDRANT_PORT:6334}` (gRPC)
- HTTP health port: 6333
- Collection: `${QDRANT_COLLECTION:localmind-documents}`
- Client: `spring-ai-starter-vector-store-qdrant`
- Adapter: `QdrantVectorStoreAdapter` at `localmind-infrastructure/src/main/java/com/localmind/infrastructure/vectorstore/adapter/`
- Docker: `qdrant/qdrant:latest`, volume `localmind-qdrant-data`, container `localmind-qdrant`

**File Storage:**
- Local filesystem at `${DOCUMENT_UPLOAD_DIR:${user.home}/.localmind/uploads}`
- Max upload size: `${MAX_FILE_SIZE:50MB}`
- Supported document types: `pdf, docx, txt, eml`
- Backup directory: `${BACKUP_DIRECTORY:./backups}`, retention `${BACKUP_RETENTION_DAYS:30}` days
- Fine-tuning output: `${FINETUNING_OUTPUT_DIR:${user.home}/.localmind/finetuning}`

**Caching:**
- In-memory Caffeine cache (no Redis)
- Named caches: `providerConfigs`, `ollamaModels`, `mcpServers`
- TTL: 5 minutes, max 100 entries
- Config: `localmind-infrastructure/src/main/java/com/localmind/infrastructure/config/CacheConfig.java`

## Authentication & Identity

**Auth Provider:**
- Custom local authentication via `LocalAuthFilter`
- Implementation: `localmind-infrastructure/src/main/java/com/localmind/infrastructure/security/LocalAuthFilter.java`
- Auth service domain: `LocalAuthService` wired in `DomainConfig.java`
- Auth endpoints: `GET/POST /api/v1/auth/**` (permitted without auth)
- All other `/api/v1/**` routes currently also permitted (stateless, local-first model)
- Session policy: `SessionCreationPolicy.STATELESS`
- CSRF: disabled
- CORS: only `http://localhost:4200` allowed (dev config, `SecurityConfig.java`)

## MCP (Model Context Protocol)

**MCP Server:**
- Implementation: `spring-ai-starter-mcp-server-webmvc`
- Local tools exposed: `LocalMindMcpTools`, `LocalMindMcpResources` at `localmind-infrastructure/src/main/java/com/localmind/infrastructure/mcp/server/`
- Server name: `localmind`, version: `0.1.0`
- Enabled: `localmind.mcp.server.enabled=true`

**MCP Client:**
- Implementation: `spring-ai-starter-mcp-client`
- Manages connections to external MCP servers via DB (`mcp_servers` table, V6 migration)
- Discovery, persistence, service at `localmind-infrastructure/src/main/java/com/localmind/infrastructure/mcp/`
- Enabled: `localmind.mcp.client.enabled=true`

## Automation

**n8n Workflow Automation:**
- Purpose: Trigger external automation workflows via webhooks
- Adapter: `N8nWebhookClient` at `localmind-infrastructure/src/main/java/com/localmind/infrastructure/automation/adapter/`
- Base URL: `http://${N8N_HOST:localhost}:${N8N_PORT:5678}`
- Webhook path: `/webhook`
- Auth: `N8N_BASIC_AUTH_USER` / `N8N_BASIC_AUTH_PASSWORD` env vars
- Docker: configured in `docker-compose.yml` environment variables but not as a managed service (external)

## Email

**IMAP/SMTP Email:**
- Purpose: Email reading (IMAP) and sending (SMTP) for document ingestion from mailboxes
- Client: Eclipse Angus Mail 2.0.3 (`localmind-infrastructure/pom.xml`)
- Adapter: `localmind-infrastructure/src/main/java/com/localmind/infrastructure/email/adapter/`
- Config: `EmailProperties` at `localmind-infrastructure/src/main/java/com/localmind/infrastructure/email/config/`
- IMAP: `${IMAP_HOST}:${IMAP_PORT:993}`
- SMTP: `${SMTP_HOST}:${SMTP_PORT:587}`
- Auth: `EMAIL_USER` / `EMAIL_PASS` env vars
- Enabled: `${EMAIL_ENABLED:false}`

## Calendar

**CalDAV Calendar Integration:**
- Purpose: Calendar event reading/writing via CalDAV protocol
- Adapter: `CalDavCalendarAdapter` at `localmind-infrastructure/src/main/java/com/localmind/infrastructure/calendar/adapter/`
- Config: `CalendarProperties` at `localmind-infrastructure/src/main/java/com/localmind/infrastructure/calendar/config/`
- URL: `${CALDAV_URL}` (any CalDAV-compliant server — Nextcloud, Radicale, etc.)
- Auth: `CALDAV_USER` / `CALDAV_PASS` env vars
- Enabled: `${CALENDAR_ENABLED:false}`

## Messaging Channels

**Slack:**
- Purpose: Send/receive messages via Slack API
- Adapter: `SlackMessagingAdapter` at `localmind-infrastructure/src/main/java/com/localmind/infrastructure/messaging/adapter/`
- Enabled: `localmind.messaging.slack.enabled=false` (config default in `application-dev.yml`)
- DB persistence: `messaging_channels` table (V76 migration), `channel_contexts` (V77)

**Discord:**
- Purpose: Send/receive messages via Discord API
- Adapter: `DiscordMessagingAdapter` at `localmind-infrastructure/src/main/java/com/localmind/infrastructure/messaging/adapter/`
- Enabled: `localmind.messaging.discord.enabled=false`

**Telegram:**
- Purpose: Send/receive messages via Telegram Bot API
- Adapter: `TelegramMessagingAdapter` at `localmind-infrastructure/src/main/java/com/localmind/infrastructure/messaging/adapter/`
- Enabled: `localmind.messaging.telegram.enabled=false`
- Router: `MessagingClientRouter` selects adapter by channel type

## Monitoring & Observability

**Metrics:**
- Micrometer Prometheus registry — `localmind-app/pom.xml`
- Endpoint: `/actuator/prometheus` (exposed in `application-dev.yml`)
- Docker optional: Prometheus (`prom/prometheus:latest`) + Grafana (`grafana/grafana:latest`) in `monitoring` compose profile
- Prometheus config: `docker/prometheus/prometheus.yml`
- Grafana provisioning: `docker/grafana/provisioning/`
- Retention: 7 days (`docker-compose.yml`)

**Health Check:**
- Spring Actuator health: `/actuator/health` (details: `when-authorized`)
- Application health endpoint: `/api/v1/dashboard/health` (publicly accessible)

**Logs:**
- SLF4J/Logback (Spring Boot default)
- Dev: `com.localmind=DEBUG`, `org.springframework.ai=DEBUG`
- Prod: `com.localmind=INFO`, `org.springframework.ai=WARN`
- Console pattern: `%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n`

## Document Processing

**Apache Tika 2.9.2:**
- Purpose: Content extraction from PDF, DOCX, EML, TXT, and other formats
- Dependency: `tika-core` + `tika-parsers-standard-package` (`localmind-infrastructure/pom.xml`)
- Used by document ingestion adapters in `localmind-infrastructure/src/main/java/com/localmind/infrastructure/document/adapter/`

**Tesseract OCR (via Tess4J 5.13.0):**
- Purpose: Text extraction from images and scanned PDFs
- Dependency: `tess4j` (`localmind-infrastructure/pom.xml`)
- Config: `OcrProperties` at `localmind-infrastructure/src/main/java/com/localmind/infrastructure/document/config/`
- Data path: `${OCR_DATAPATH:/usr/share/tesseract-ocr/5/tessdata}`
- Languages: `${OCR_LANGUAGES:ita+eng}` (Italian + English)
- DPI: `${OCR_DPI:300}`, min confidence: `${OCR_MIN_CONFIDENCE:30.0}`
- Enabled: `${OCR_ENABLED:true}`

## CI/CD & Deployment

**Hosting:**
- Docker Compose (`docker-compose.yml`) for local/production deployment
- Three profiles: default (infra only), `full` (all services), `monitoring` (Prometheus + Grafana)
- Frontend: Nginx serving Angular SPA (`Dockerfile.frontend`, `nginx/` config directory)

**CI Pipeline:**
- GitHub Pages deployment: `.github/workflows/pages.yml` (docs site only)
- No full CI/CD pipeline for backend/frontend (removed per git history)

## SDKs (Client Libraries)

**Java SDK:**
- Location: `localmind-sdk-java/` — Maven project (`pom.xml`)
- Purpose: Client library for external Java applications consuming LocalMind API

**JavaScript/TypeScript SDK:**
- Location: `localmind-sdk-js/` — npm package (`package.json`)
- Purpose: Client library for JavaScript/TypeScript consumers

**Python SDK:**
- Location: `localmind-sdk-python/` — `pyproject.toml`, requires Python 3.9+, `requests>=2.31.0`
- Package: `localmind-sdk`
- Purpose: Client library for Python consumers

## Environment Configuration

**Required env vars (production):**
- `DB_PASSWORD` — MySQL root/app password
- `DB_USERNAME` — MySQL username
- `DB_HOST` / `DB_PORT` / `DB_NAME` — MySQL connection
- `OLLAMA_HOST` / `OLLAMA_PORT` — Ollama service
- `QDRANT_HOST` / `QDRANT_PORT` — Qdrant service

**Optional cloud LLM keys:**
- `OPENAI_API_KEY`, `ANTHROPIC_API_KEY`, `MISTRAL_API_KEY`, `DEEPSEEK_API_KEY`, `XAI_API_KEY`, `GOOGLE_API_KEY`

**Optional integrations:**
- `CALDAV_URL`, `CALDAV_USER`, `CALDAV_PASS` — CalDAV calendar
- `IMAP_HOST`, `SMTP_HOST`, `EMAIL_USER`, `EMAIL_PASS` — Email
- `N8N_HOST`, `N8N_PORT`, `N8N_BASIC_AUTH_USER`, `N8N_BASIC_AUTH_PASSWORD` — n8n automation

**Secrets location:**
- `.env` at project root (never committed; templated via `.env.example`)
- Cloud LLM API keys also stored per-provider in `llm_provider_configs` MySQL table (settable via settings UI at `/api/v1/settings/providers`)

## Webhooks & Callbacks

**Incoming:**
- MCP HTTP requests: stored in `mcp_http_requests` table (V17 migration), manageable via `/api/v1/mcp/` endpoints

**Outgoing:**
- n8n automation webhooks: `N8nWebhookClient` sends POST to `http://${N8N_HOST}:${N8N_PORT}/webhook`

---

*Integration audit: 2026-06-29*
