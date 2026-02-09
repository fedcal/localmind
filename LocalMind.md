# 🧠 LocalMind
### *Your AI, your data, your machine.*

LocalMind è una piattaforma **AI local-first**, modulare e self-hosted che permette di utilizzare **LLM locali (Ollama)** e **LLM cloud (ChatGPT, Claude, Gemini)** per gestire documenti, conoscenza, automazioni e assistenza intelligente, con un'interfaccia grafica Angular.

---

## 🎯 Visione

> Portare l’AI avanzata **sul computer dell’utente**, mantenendo controllo totale su dati, costi e privacy.

LocalMind è pensato per:
- sviluppatori
- professionisti business / legal
- utenti comuni

senza rinunciare a potenza, spiegabilità e automazione.

---

## 🧩 Funzionalità Principali

### 🔌 LLM Gateway (multi-provider)
- Supporto **Ollama (local)**  
- Supporto **ChatGPT / Claude / Gemini** via API key  
- Routing automatico local → cloud  
- Fallback, retry, rate limit  
- Tracking utilizzo e costi  

---

### 📚 Document Intelligence & RAG

#### Ingestione Documenti
- Upload manuale (PDF, DOCX, TXT, EML)
- **Indicizzazione da filesystem**
- Preview e metadata automatici

#### 📂 Indicizzazione da Cartelle Locali (FEATURE CHIAVE)
LocalMind consente di indicare **uno o più path locali** da cui leggere automaticamente i documenti.

Esempi:
```
/home/user/Documents
/home/user/contracts
/mnt/shared/legal
```

Funzionalità:
- Scan ricorsivo delle cartelle
- Watcher filesystem (opzionale)
- Indicizzazione incrementale
- Deduplicazione file
- Supporto grandi volumi

Questa funzione rende LocalMind perfetto come:
- knowledge base personale
- archivio aziendale
- repository legale offline

---

### 🧠 Pipeline RAG
- Estrazione testo (PDFBox / Tika)
- OCR (Tesseract)
- Chunking configurabile
- Embedding local o cloud
- Vector DB (Chroma / Qdrant)
- Q&A con citazione delle fonti

---

### 🔄 Spring Batch – Document Processing
- Job asincroni
- Retry automatici
- Scheduling
- Monitoraggio job
- Gestione errori

---

### 🤖 AI Agents
- Tech Agent (codice, debug)
- Business Agent (report, sintesi)
- Legal Agent (clausole, riferimenti)
- Personal Agent (spiegazioni semplici)

Ogni agente usa:
- LLM Gateway
- RAG
- Tool calling

---

### 🔁 Automazioni con n8n (gratuito, self-hosted)
- Integrazione via webhook
- Workflow no-code
- Trigger su:
  - nuovi file
  - nuovi documenti indicizzati
  - schedulazioni

Esempi:
- Documento → summary → salvataggio
- Email → classificazione → tag
- Report settimanale automatico

---

## 🖥️ Interfaccia Angular

Sezioni:
- Chat AI multi-modello
- Libreria documenti
- Ricerca semantica
- Automazioni
- Configurazione modelli e path filesystem
- Dashboard utilizzo

Modalità:
- Semplice
- Avanzata
- Preset per ruolo

---

## 🏗️ Architettura

```
Angular UI (porta 4200)
   │
Spring Boot API (porta 8080)
   ├─ LLM Gateway
   ├─ RAG Engine
   ├─ Spring Batch
   ├─ Agents
   ├─ File System Scanner
   │
   ├─ Ollama (porta 11434)
   ├─ Qdrant Vector DB (porta 6333/6334)
   ├─ MySQL 8.0 (porta 3306)
   └─ n8n (porta 5678)
```

---

## 🧰 Stack Tecnologico

| Componente | Tecnologia |
|-----------|------------|
| Backend | Java 17, Spring Boot 3.4.2, Spring AI 1.0.0 |
| Frontend | Angular 21, TypeScript 5.9, SCSS |
| Database | MySQL 8.0, Flyway migrations |
| Vector Store | Qdrant |
| LLM locale | Ollama |
| LLM cloud | OpenAI, Anthropic, Google (opzionali) |
| Automazioni | n8n (self-hosted) |
| Build | Maven (backend), npm (frontend) |

---

## ⚙️ Prerequisiti

| Software | Versione minima | Verifica |
|----------|----------------|----------|
| Java JDK | 17+ | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Node.js | 22+ | `node -v` |
| npm | 10+ | `npm -v` |
| MySQL | 8.0+ | `mysql --version` |
| Ollama | latest | `ollama --version` |

**Opzionali:**
- Qdrant (per RAG/vector search)
- n8n (per automazioni)

---

## 🚀 Guida all'avvio

### 1. Setup Database MySQL

Crea il database `localmind` e l'utente dedicato:

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

Lo script crea:
- Database: `localmind` (charset utf8mb4)
- Utente: `localmind` / password: `localmind`
- Le tabelle vengono create automaticamente da Flyway al primo avvio del backend

### 2. Avvia il Backend

**Linux/Mac:**
```bash
./scripts/start-backend.sh
```

**Windows:**
```cmd
scripts\start-backend.bat
```

Il backend:
- Compila tutti i moduli Maven
- Avvia Spring Boot con profilo `dev`
- Esegue le migration Flyway (crea le tabelle)
- Si connette a MySQL su `localhost:3306`
- Ascolta su **http://localhost:8080**

### 3. Avvia il Frontend

**Linux/Mac:**
```bash
./scripts/start-frontend.sh
```

**Windows:**
```cmd
scripts\start-frontend.bat
```

Il frontend:
- Installa le dipendenze npm (se necessario)
- Avvia Angular dev server
- Disponibile su **http://localhost:4200**

### 4. Avvia tutto insieme

**Linux/Mac:**
```bash
./scripts/start-all.sh
```
Avvia backend e frontend in parallelo. `Ctrl+C` ferma entrambi.

**Windows:**
```cmd
scripts\start-all.bat
```
Apre backend e frontend in finestre separate.

### 5. Servizi esterni (opzionali)

**Ollama** (LLM locale):
```bash
ollama serve
ollama pull llama3.2
ollama pull nomic-embed-text
```

**Qdrant** (vector store):
```bash
# Opzione 1: Docker
docker run -p 6333:6333 -p 6334:6334 -v qdrant-data:/qdrant/storage qdrant/qdrant

# Opzione 2: Installazione nativa
# Vedi https://qdrant.tech/documentation/guides/installation/
```

**n8n** (automazioni):
```bash
# Opzione 1: Docker
docker run -p 5678:5678 -v n8n-data:/home/node/.n8n n8nio/n8n

# Opzione 2: npm
npm install -g n8n && n8n start
```

---

## 📁 Struttura Progetto

```
localmind/
├── scripts/                     # Script di setup e avvio
│   ├── setup-mysql.sh/.bat      # Crea database e utente MySQL
│   ├── start-backend.sh/.bat    # Avvia backend Spring Boot
│   ├── start-frontend.sh/.bat   # Avvia frontend Angular
│   └── start-all.sh/.bat        # Avvia tutto insieme
├── localmind-backend/           # Backend Spring Boot (multi-module Maven)
│   ├── localmind-domain/        # Modelli, porte, logica pura (zero dipendenze)
│   ├── localmind-infrastructure/# Adapter: DB, LLM, vector store, filesystem
│   ├── localmind-api/           # REST controller, DTO
│   ├── localmind-batch/         # Spring Batch jobs (folder scan)
│   └── localmind-app/           # Modulo eseguibile, config, migrations
│       └── src/main/resources/
│           ├── application.yml         # Config base
│           ├── application-dev.yml     # Config sviluppo (localhost)
│           ├── application-prod.yml    # Config produzione
│           └── db/migration/           # Flyway migrations (V1-V6)
├── localmind-frontend/          # Frontend Angular 21
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/            # Servizi singleton, guard, interceptor
│   │   │   ├── shared/          # Componenti riutilizzabili
│   │   │   ├── layout/          # Header, sidebar, layout
│   │   │   └── features/        # Moduli lazy-loaded
│   │   │       ├── chat/
│   │   │       ├── documents/
│   │   │       ├── search/
│   │   │       ├── folders/
│   │   │       ├── settings/
│   │   │       └── dashboard/
│   │   └── styles.scss
│   └── package.json
├── .env.example                 # Template variabili d'ambiente
└── LocalMind.md                 # Questo file
```

---

## 🔧 Configurazione

### Variabili d'ambiente (.env)

Copia `.env.example` in `.env` e personalizza:

```bash
cp .env.example .env
```

| Variabile | Default | Descrizione |
|-----------|---------|-------------|
| `MYSQL_ROOT_PASSWORD` | `rootpassword` | Password root MySQL |
| `MYSQL_DATABASE` | `localmind` | Nome database |
| `MYSQL_USER` | `localmind` | Utente database |
| `MYSQL_PASSWORD` | `localmind` | Password utente |
| `OPENAI_API_KEY` | *(vuoto)* | API key OpenAI (opzionale) |
| `ANTHROPIC_API_KEY` | *(vuoto)* | API key Anthropic (opzionale) |
| `GOOGLE_API_KEY` | *(vuoto)* | API key Google AI (opzionale) |

### Profili Spring Boot

| Profilo | Uso | Database |
|---------|-----|----------|
| `dev` | Sviluppo locale | `localhost:3306` |
| `prod` | Produzione | Configurabile via env vars |

---

## 🆚 Analisi Competitor

| Prodotto | Limiti |
|--------|-------|
| ChatGPT / Notion AI | Cloud-only, lock-in |
| PrivateGPT | UI e workflow limitati |
| LangChain Python apps | No enterprise Java |
| n8n | Solo automazione |
| LocalMind | **Tutto integrato, local-first** |

---

## 🔐 Privacy & Sicurezza
- Dati locali
- API key cifrate
- Modalità offline totale
- Nessun vendor lock-in

---

## 🚀 Roadmap
1. LLM Gateway + Ollama
2. File system ingestion
3. RAG base
4. UI Angular
5. Automazioni n8n
6. Agents

---

## 🏁 Conclusione

LocalMind non è un chatbot.
È **un sistema operativo AI personale e professionale**.

