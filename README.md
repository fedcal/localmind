# LocalMind

**Your AI, your data, your machine.**

Piattaforma AI local-first per gestione documenti, ricerca semantica e chat multi-provider LLM.

---

## Prerequisiti

| Requisito | Versione | Note |
|-----------|----------|------|
| Docker + Compose | v2+ | **Obbligatorio** per i servizi infra |
| Java | 17+ | Solo per modalita' nativa |
| Maven | 3.9+ | Solo per modalita' nativa |
| Node.js | 22+ | Solo per modalita' nativa |
| npm | 11+ | Solo per modalita' nativa |

## Quick Start

### Opzione A: Setup Automatico (consigliato)

```bash
git clone <repo-url> localmind
cd localmind
./scripts/setup.sh
```

Lo script guida la configurazione passo-passo: crea `.env`, avvia MySQL/Qdrant/Ollama in Docker, configura il database e scarica i modelli AI.

### Opzione B: Docker Completo (tutto in container)

```bash
git clone <repo-url> localmind
cd localmind
cp .env.example .env
# Modifica .env con le tue impostazioni

# Avvia tutto (infra + backend + frontend)
docker compose --profile full up -d --build
```

### Opzione C: Manuale (sviluppatori)

```bash
git clone <repo-url> localmind
cd localmind
cp .env.example .env
# Modifica .env con le tue credenziali

# 1. Avvia i servizi infra
docker compose up -d

# 2. Scarica modelli Ollama
./scripts/setup-ollama-models.sh

# 3. Avvia backend + frontend
./scripts/start-all.sh
```

### Accedi

- **Frontend**: http://localhost:4200
- **API**: http://localhost:8080/api/v1
- **Health check**: http://localhost:8080/api/v1/dashboard/health

---

## Architettura

```
localmind/
├── localmind-backend/          # Spring Boot 3.4.2 + Spring AI 1.0.0
│   ├── localmind-domain/       # Logica pura Java (zero dipendenze framework)
│   ├── localmind-infrastructure/ # Adapter Spring (JPA, LLM, Vector Store, MCP Tools)
│   ├── localmind-api/          # Controller REST + DTO
│   ├── localmind-batch/        # Spring Batch (scansione cartelle, ingestion)
│   └── localmind-app/          # Entry point, Flyway migrations, profili
├── localmind-frontend/         # Angular 21, standalone components, Signals
├── scripts/                    # Script di avvio
├── documentazione/             # Documentazione in italiano
└── documentation/              # Documentazione in inglese
```

**Pattern architetturale**: Hexagonal (Ports & Adapters). I servizi di dominio non hanno dipendenze Spring e sono registrati come `@Bean` in `DomainConfig.java`.

**LLM Gateway**: Routing multi-provider con fallback chain configurabile (OLLAMA -> OPENAI -> ANTHROPIC -> GOOGLE).

---

## Comandi principali

### Backend

```bash
cd localmind-backend

# Compilare tutti i moduli
mvn install -DskipTests

# Avviare con profilo dev
mvn -pl localmind-app spring-boot:run -Dspring-boot.run.profiles=dev

# Eseguire i test
mvn test
```

### Frontend

```bash
cd localmind-frontend

npm install
npm start           # Dev server su porta 4200
npm test            # Unit test (Vitest)
npm run e2e         # E2E test (Playwright)
```

---

## Configurazione

Le variabili d'ambiente sono definite in `.env` (vedi `.env.example`):

| Variabile | Default | Descrizione |
|-----------|---------|-------------|
| `DB_HOST` | localhost | Host MySQL |
| `DB_PORT` | 3306 | Porta MySQL |
| `DB_NAME` | localmind | Nome database |
| `DB_USERNAME` | root | Utente database |
| `DB_PASSWORD` | - | Password database |
| `SERVER_PORT` | 8080 | Porta backend |
| `OLLAMA_HOST` | localhost | Host Ollama |
| `OLLAMA_PORT` | 11434 | Porta Ollama |
| `OLLAMA_CHAT_MODEL` | llama3.2 | Modello chat |
| `OLLAMA_EMBED_MODEL` | nomic-embed-text | Modello embedding |
| `QDRANT_HOST` | localhost | Host Qdrant |
| `QDRANT_PORT` | 6334 | Porta Qdrant |
| `LLM_DEFAULT_PROVIDER` | OLLAMA | Provider predefinito |
| `OPENAI_API_KEY` | - | Chiave API OpenAI (opzionale) |
| `ANTHROPIC_API_KEY` | - | Chiave API Anthropic (opzionale) |
| `GOOGLE_AI_API_KEY` | - | Chiave API Google (opzionale) |

---

## API Endpoints

Tutti sotto `/api/v1/`:

| Endpoint | Descrizione |
|----------|-------------|
| `/chat` | Chat con LLM |
| `/documents` | CRUD documenti e upload |
| `/search` | Ricerca semantica |
| `/folders` | Configurazione monitoraggio cartelle |
| `/settings/providers` | Gestione provider LLM |
| `/mcp/servers` | Gestione server MCP |
| `/mcp/tools` | Esecuzione tool MCP |
| `/models` | Lista modelli disponibili |
| `/dashboard/health` | Health check |

---

## Test

| Suite | Framework | Test | Comando |
|-------|-----------|------|---------|
| Backend Unit | JUnit 5 + Mockito | ~500 | `cd localmind-backend && mvn test` |
| Frontend Unit | Vitest | - | `cd localmind-frontend && npm test` |
| E2E | Playwright | 67 | `cd localmind-frontend && npm run e2e` |

---

## MCP Tools Nativi

LocalMind integra nativamente 132+ tool MCP organizzati in 9 classi:

| Classe | Tool | Funzionalita' |
|--------|------|----------------|
| LocalMindUtilityTools | 13 | Regex, HTTP client, snippet |
| LocalMindCodeTools | 9 | Code review, dependency analysis, scaffolding |
| LocalMindTestTools | 6 | Test generation, performance profiling |
| LocalMindDevOpsTools | 12 | Docker, log analysis, CI/CD monitor |
| LocalMindDatabaseTools | 8 | Schema explorer, mock data generator |
| LocalMindDocTools | 8 | API documentation, codebase knowledge |
| LocalMindProjectTools | 31 | Scrum, agile metrics, time tracking |
| LocalMindCommTools | 8 | Standup notes, environment manager |
| LocalMindOpsTools | 37 | Access policy, incident manager, workflow |

---

## Licenza

Progetto privato. Tutti i diritti riservati.
