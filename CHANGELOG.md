# Changelog

Tutte le modifiche significative al progetto sono documentate in questo file.
All notable changes to this project are documented in this file.

Il formato è basato su [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [0.1.0] - 2026-02-14

### Aggiunto / Added
- Sistema di autenticazione locale con password BCrypt
- Rate limiting con Token Bucket in-memory (100 req/min per IP)
- Swagger/OpenAPI con springdoc-openapi-starter-webmvc-ui
- CI/CD con GitHub Actions (test + Docker build)
- Monitoring: logback strutturato, CorrelationId, Actuator endpoints
- Caching con Caffeine (provider configs, modelli Ollama, TTL 5min)
- Frontend: EmptyStateComponent, LoadingSkeletonComponent
- Frontend: pagina 404, sidebar responsive, dark mode toggle
- Documentazione: CONTRIBUTING.md, CHANGELOG.md, SECURITY.md
- Documentazione sicurezza IT/EN (autenticazione locale, rate limiting)
- Test E2E Playwright per autenticazione (67 test totali)
- File tracking sviluppi in `Sviluppi/`

### Modificato / Changed
- Migliorata UX con empty states e loading skeletons
- Header X-Correlation-Id per tracciamento richieste
- Log strutturati con pattern JSON-like

### Precedente / Previous (v0.0.x)

#### Core Features
- Chat AI multi-provider (Ollama, OpenAI, Anthropic, Google Gemini)
- Gestione documenti con pipeline RAG (upload, chunking, embedding)
- Ricerca semantica con Qdrant vector store
- Cartelle monitorate con scansione automatica (Spring Batch)
- Interfaccia multilingua (IT/EN) con i18n

#### MCP Integration
- 135 tool nativi distribuiti in 8 server MCP
- Scrum Board con sprint, story, task
- Incident Management con severità e timeline
- Time Tracking con timer e log time
- Dashboard unificata per tutti i tool

#### Architecture
- Backend: Spring Boot 3.4.2, Spring AI 1.0.0, architettura esagonale
- Frontend: Angular 21 standalone components, Signals
- Database: MySQL 8.0, Qdrant vector store, Flyway migrations
- Docker support con docker-compose.yml

#### Testing
- Backend: 1306 unit test (JUnit 5 + Mockito 5.14)
- Frontend: 67 test E2E Playwright
- Coverage ~80% domain, ~70% infrastructure

## [Unreleased]

### Da Fare / To Do
- Integrazione Prometheus/Grafana per metriche
- Backup automatico database e vector store
- Compressione file uploadati
- Export/import configurazioni
- API versioning (v2)
