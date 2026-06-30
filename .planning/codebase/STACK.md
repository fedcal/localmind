# Technology Stack

**Analysis Date:** 2026-06-29

## Languages

**Primary:**
- Java 17 - Backend (all Spring modules under `localmind-backend/`)
- TypeScript 5.9 - Frontend (`localmind-frontend/src/`)

**Secondary:**
- Python 3.9+ - SDK (`localmind-sdk-python/`) and fine-tuning scripts (`scripts/finetuning/`)

## Runtime

**Backend:**
- JVM (Java 17, minimum)
- Spring Boot fat-jar execution via `spring-boot-maven-plugin`

**Frontend:**
- Node 22+ (build/dev), served by Nginx in production (Docker: `Dockerfile.frontend`)
- Package Manager: npm 11.6.3 (pinned via `packageManager` field in `localmind-frontend/package.json`)
- Lockfile: `localmind-frontend/package-lock.json` present

## Frameworks

**Backend Core:**
- Spring Boot 3.4.2 — parent POM at `localmind-backend/pom.xml`, inherited by all modules
- Spring AI 1.0.0 — BOM imported; provides all LLM provider starters and vector store starters
- Spring Security — stateless filter chain (`localmind-infrastructure/src/main/java/com/localmind/infrastructure/config/SecurityConfig.java`)
- Spring Batch — folder scan and document ingestion jobs (`localmind-batch/`)
- Spring WebFlux — reactive WebClient used internally by LLM adapters (`localmind-infrastructure/pom.xml`)
- Spring Retry — retry logic for LLM calls (`localmind-infrastructure/pom.xml`)
- Spring Cache — Caffeine-backed caches for provider configs, Ollama models, MCP servers (`localmind-infrastructure/src/main/java/com/localmind/infrastructure/config/CacheConfig.java`)
- Spring Actuator — metrics/health endpoints exposed at `/actuator/**`

**Frontend Core:**
- Angular 21 (standalone components, no NgModules) — `localmind-frontend/src/`
- RxJS 7.8 — reactive streams for HTTP and state
- Angular Signals — primary state management (e.g., `ChatStore` at `localmind-frontend/src/app/features/chat/state/`)
- Angular Router — lazy-loaded routes via `loadChildren`/`loadComponent`
- zone.js 0.16 — required polyfill listed in `angular.json`

**Testing:**
- Vitest 4.0 — frontend unit tests (`localmind-frontend/package.json`)
- Playwright 1.58 — frontend E2E tests (`localmind-frontend/package.json`, scripts: `e2e`, `e2e:headed`, etc.)
- JUnit 5 — backend unit tests (via `spring-boot-starter-test`)
- Testcontainers 1.20.4 — integration tests with real MySQL containers (`localmind-app/pom.xml`)
- JaCoCo 0.8.12 — code coverage reporting, aggregate report in `localmind-app`

**Build/Dev:**
- Maven 3.9+ (multi-module build) — parent at `localmind-backend/pom.xml`
- Angular CLI 21 — frontend build/serve, configured in `localmind-frontend/`
- Prettier (printWidth=100, singleQuote=true) — frontend formatting, config in `localmind-frontend/package.json`
- Springdoc OpenAPI 2.8.0 — Swagger UI at `/swagger-ui.html`, API docs at `/api-docs`

## Key Dependencies

**Critical:**
- `spring-ai-bom` 1.0.0 — manages all Spring AI artifacts (`localmind-backend/pom.xml`)
- `spring-ai-starter-model-ollama` — Ollama LLM and embedding integration (`localmind-infrastructure/pom.xml`)
- `spring-ai-starter-model-openai` — OpenAI chat integration (`localmind-infrastructure/pom.xml`)
- `spring-ai-starter-model-anthropic` — Anthropic Claude integration (`localmind-infrastructure/pom.xml`)
- `spring-ai-starter-model-mistral-ai` — Mistral AI integration (`localmind-infrastructure/pom.xml`)
- `spring-ai-starter-vector-store-qdrant` — Qdrant vector store integration (`localmind-infrastructure/pom.xml`)
- `spring-ai-starter-mcp-server-webmvc` — MCP server (WebMVC) (`localmind-infrastructure/pom.xml`)
- `spring-ai-starter-mcp-client` — MCP client (`localmind-infrastructure/pom.xml`)
- `mysql-connector-j` — MySQL 8 JDBC driver (`localmind-infrastructure/pom.xml`)
- `flyway-core` + `flyway-mysql` — schema migration, V1–V78 scripts in `localmind-app/src/main/resources/db/migration/`

**Document Processing:**
- Apache Tika 2.9.2 (core + parsers-standard-package) — document parsing/extraction (`localmind-infrastructure/pom.xml`)
- Tess4J 5.13.0 — Tesseract OCR wrapper for Java (`localmind-infrastructure/pom.xml`)

**Mapping & Code Generation:**
- MapStruct 1.6.3 — DTO-to-domain mapping (annotation processor) (`localmind-backend/pom.xml`)
- Lombok 1.18.36 — boilerplate reduction (annotation processor) (`localmind-backend/pom.xml`)

**Plugin System:**
- PF4J 3.12.0 + pf4j-spring 0.9.0 — plugin framework for dynamic extension loading (`localmind-infrastructure/pom.xml`)
- Plugin SPI defined in `localmind-plugin-api` module (extension points: `LlmProviderExtension`, `DocumentParserExtension`, `VectorStoreExtension`)

**Infrastructure:**
- Caffeine cache (in-memory, TTL 5 min, max 100 entries) — `localmind-infrastructure/src/main/java/com/localmind/infrastructure/config/CacheConfig.java`
- Eclipse Angus Mail 2.0.3 — IMAP/SMTP email client (`localmind-infrastructure/pom.xml`)
- Micrometer Prometheus registry — metrics scraping at `/actuator/prometheus` (`localmind-app/pom.xml`)

## Configuration

**Environment:**
- All runtime configuration loaded from `.env` at project root (see `.env.example` for full list)
- Spring profile is `dev` by default; activate `prod` in Docker full profile
- Critical env vars: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `OLLAMA_HOST`, `OLLAMA_PORT`, `QDRANT_HOST`, `QDRANT_PORT`, `LLM_DEFAULT_PROVIDER`, `OPENAI_API_KEY`, `ANTHROPIC_API_KEY`, `MISTRAL_API_KEY`, `DEEPSEEK_API_KEY`, `XAI_API_KEY`

**Dev profile overrides:**
- `localmind-backend/localmind-app/src/main/resources/application-dev.yml` — disables OpenAI/Anthropic/Mistral auto-configs when keys are empty; sets Flyway locations and datasource

**Prod profile:**
- `localmind-backend/localmind-app/src/main/resources/application-prod.yml` — SQL logging off, LLM level WARN

**Test profile:**
- `localmind-backend/localmind-app/src/test/resources/application-test.yml`

**Build:**
- Backend: `mvn install -DskipTests` from `localmind-backend/` then `mvn -pl localmind-app spring-boot:run`
- Frontend: `npm install && npm start` from `localmind-frontend/`
- Full stack scripts: `scripts/start-backend.sh`, `scripts/start-frontend.sh`, `scripts/start-all.sh`

## Platform Requirements

**Development:**
- Java 17+, Maven 3.9+, Node 22+, npm 11+
- Docker (for MySQL 8.0, Qdrant, Ollama containers)
- Tesseract OCR 5 installed at `${OCR_DATAPATH:/usr/share/tesseract-ocr/5/tessdata}` for OCR feature

**Production:**
- Docker + Docker Compose (`docker-compose.yml`)
- Profiles: `full` (backend + frontend containers), `monitoring` (Prometheus + Grafana)
- Backend served on port 8080, Frontend (Nginx) on port 4200 (dev) or 80 (Docker)
- Nginx configuration at `nginx/` directory

---

*Stack analysis: 2026-06-29*
