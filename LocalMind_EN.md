# LocalMind
### *Your AI, your data, your machine.*

LocalMind is a **local-first AI** platform, modular and self-hosted, that allows you to use **local LLMs (Ollama)** and **cloud LLMs (ChatGPT, Claude, Gemini)** to manage documents, knowledge, automations, and intelligent assistance, with an Angular graphical interface.

---

## Vision

> Bring advanced AI **to the user's computer**, maintaining total control over data, costs, and privacy.

LocalMind is designed for:
- developers
- business / legal professionals
- everyday users

without sacrificing power, explainability, and automation.

---

## Key Features

### LLM Gateway (multi-provider)
- **Ollama (local)** support
- **ChatGPT / Claude / Gemini** support via API key
- Automatic local to cloud routing
- Fallback, retry, rate limit
- Usage and cost tracking

---

### Document Intelligence & RAG

#### Document Ingestion
- Manual upload (PDF, DOCX, TXT, EML)
- **Filesystem indexing**
- Automatic preview and metadata

#### Local Folder Indexing (KEY FEATURE)
LocalMind allows you to specify **one or more local paths** from which to automatically read documents.

Examples:
```
/home/user/Documents
/home/user/contracts
/mnt/shared/legal
```

Features:
- Recursive folder scanning
- Filesystem watcher (optional)
- Incremental indexing
- File deduplication
- Large volume support

This feature makes LocalMind perfect as:
- personal knowledge base
- corporate archive
- offline legal repository

---

### RAG Pipeline
- Text extraction (PDFBox / Tika)
- OCR (Tesseract)
- Configurable chunking
- Local or cloud embedding
- Vector DB (Chroma / Qdrant)
- Q&A with source citations

---

### Spring Batch - Document Processing
- Asynchronous jobs
- Automatic retries
- Scheduling
- Job monitoring
- Error handling

---

### AI Agents
- Tech Agent (code, debug)
- Business Agent (reports, summaries)
- Legal Agent (clauses, references)
- Personal Agent (simple explanations)

Each agent uses:
- LLM Gateway
- RAG
- Tool calling

---

### Automations with n8n (free, self-hosted)
- Webhook integration
- No-code workflows
- Triggers on:
  - new files
  - new indexed documents
  - schedules

Examples:
- Document -> summary -> save
- Email -> classification -> tag
- Automatic weekly report

---

## Angular Interface

Sections:
- Multi-model AI Chat
- Document Library
- Semantic Search
- Automations
- Model and filesystem path configuration
- Usage Dashboard

Modes:
- Simple
- Advanced
- Role presets

---

## Architecture

```
Angular UI (port 4200)
   |
Spring Boot API (port 8080)
   +- LLM Gateway
   +- RAG Engine
   +- Spring Batch
   +- Agents
   +- File System Scanner
   |
   +- Ollama (port 11434)
   +- Qdrant Vector DB (port 6333/6334)
   +- MySQL 8.0 (port 3306)
   +- n8n (port 5678)
```

---

## Technology Stack

| Component | Technology |
|-----------|------------|
| Backend | Java 17, Spring Boot 3.4.2, Spring AI 1.0.0 |
| Frontend | Angular 21, TypeScript 5.9, SCSS |
| Database | MySQL 8.0, Flyway migrations |
| Vector Store | Qdrant |
| Local LLM | Ollama |
| Cloud LLM | OpenAI, Anthropic, Google (optional) |
| Automations | n8n (self-hosted) |
| Build | Maven (backend), npm (frontend) |

---

## Prerequisites

| Software | Minimum Version | Verification |
|----------|----------------|--------------|
| Java JDK | 17+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 22+ | `node -v` |
| npm | 10+ | `npm -v` |
| MySQL | 8.0+ | `mysql --version` |
| Ollama | latest | `ollama --version` |

**Optional:**
- Qdrant (for RAG/vector search)
- n8n (for automations)

---

## Getting Started Guide

### 1. MySQL Database Setup

Create the `localmind` database and the dedicated user:

**Linux/Mac:**
```bash
cd scripts
./setup-mysql.sh
```

**Windows:**
```cmd
cd scripts
setup-mysql.bat
```

The script creates:
- Database: `localmind` (charset utf8mb4)
- User: `localmind` / password: `localmind`
- Tables are automatically created by Flyway on the first backend startup

### 2. Start the Backend

**Linux/Mac:**
```bash
./scripts/start-backend.sh
```

**Windows:**
```cmd
scripts\start-backend.bat
```

The backend:
- Compiles all Maven modules
- Starts Spring Boot with the `dev` profile
- Runs Flyway migrations (creates tables)
- Connects to MySQL on `localhost:3306`
- Listens on **http://localhost:8080**

### 3. Start the Frontend

**Linux/Mac:**
```bash
./scripts/start-frontend.sh
```

**Windows:**
```cmd
scripts\start-frontend.bat
```

The frontend:
- Installs npm dependencies (if needed)
- Starts the Angular dev server
- Available at **http://localhost:4200**

### 4. Start Everything Together

**Linux/Mac:**
```bash
./scripts/start-all.sh
```
Starts backend and frontend in parallel. `Ctrl+C` stops both.

**Windows:**
```cmd
scripts\start-all.bat
```
Opens backend and frontend in separate windows.

### 5. External Services (optional)

**Ollama** (local LLM):
```bash
ollama serve
ollama pull llama3.2
ollama pull nomic-embed-text
```

**Qdrant** (vector store):
```bash
# Option 1: Docker
docker run -p 6333:6333 -p 6334:6334 -v qdrant-data:/qdrant/storage qdrant/qdrant

# Option 2: Native installation
# See https://qdrant.tech/documentation/guides/installation/
```

**n8n** (automations):
```bash
# Option 1: Docker
docker run -p 5678:5678 -v n8n-data:/home/node/.n8n n8nio/n8n

# Option 2: npm
npm install -g n8n && n8n start
```

---

## Project Structure

```
localmind/
├── scripts/                     # Setup and startup scripts
│   ├── setup-mysql.sh/.bat      # Creates MySQL database and user
│   ├── start-backend.sh/.bat    # Starts Spring Boot backend
│   ├── start-frontend.sh/.bat   # Starts Angular frontend
│   └── start-all.sh/.bat        # Starts everything together
├── localmind-backend/           # Spring Boot backend (multi-module Maven)
│   ├── localmind-domain/        # Models, ports, pure logic (zero dependencies)
│   ├── localmind-infrastructure/# Adapters: DB, LLM, vector store, filesystem
│   ├── localmind-api/           # REST controllers, DTOs
│   ├── localmind-batch/         # Spring Batch jobs (folder scan)
│   └── localmind-app/           # Executable module, config, migrations
│       └── src/main/resources/
│           ├── application.yml         # Base config
│           ├── application-dev.yml     # Development config (localhost)
│           ├── application-prod.yml    # Production config
│           └── db/migration/           # Flyway migrations (V1-V6)
├── localmind-frontend/          # Angular 21 frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/            # Singleton services, guards, interceptors
│   │   │   ├── shared/          # Reusable components
│   │   │   ├── layout/          # Header, sidebar, layout
│   │   │   └── features/        # Lazy-loaded modules
│   │   │       ├── chat/
│   │   │       ├── documents/
│   │   │       ├── search/
│   │   │       ├── folders/
│   │   │       ├── settings/
│   │   │       └── dashboard/
│   │   └── styles.scss
│   └── package.json
├── .env.example                 # Environment variables template
└── LocalMind.md                 # This file
```

---

## Configuration

### Environment Variables (.env)

Copy `.env.example` to `.env` and customize:

```bash
cp .env.example .env
```

| Variable | Default | Description |
|----------|---------|-------------|
| `MYSQL_ROOT_PASSWORD` | `rootpassword` | MySQL root password |
| `MYSQL_DATABASE` | `localmind` | Database name |
| `MYSQL_USER` | `localmind` | Database user |
| `MYSQL_PASSWORD` | `localmind` | User password |
| `OPENAI_API_KEY` | *(empty)* | OpenAI API key (optional) |
| `ANTHROPIC_API_KEY` | *(empty)* | Anthropic API key (optional) |
| `GOOGLE_API_KEY` | *(empty)* | Google AI API key (optional) |

### Spring Boot Profiles

| Profile | Usage | Database |
|---------|-------|----------|
| `dev` | Local development | `localhost:3306` |
| `prod` | Production | Configurable via env vars |

---

## Competitor Analysis

| Product | Limitations |
|---------|-------------|
| ChatGPT / Notion AI | Cloud-only, lock-in |
| PrivateGPT | Limited UI and workflows |
| LangChain Python apps | No enterprise Java |
| n8n | Automation only |
| LocalMind | **All-in-one, local-first** |

---

## Privacy & Security
- Local data
- Encrypted API keys
- Full offline mode
- No vendor lock-in

---

## Roadmap
1. LLM Gateway + Ollama
2. File system ingestion
3. Base RAG
4. Angular UI
5. n8n automations
6. Agents

---

## Conclusion

LocalMind is not a chatbot.
It is **a personal and professional AI operating system**.
