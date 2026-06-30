export const content = `# Guida Installazione LocalMind

|               |                                                           |
|---------------|-----------------------------------------------------------|
| **Documento** | Guida Completa all'Installazione di LocalMind             |
| **Versione**  | 0.1.0                                                     |
| **Data**      | 2026-02-14                                                |
| **Progetto**  | LocalMind                                                 |

---

## Indice

1. [Introduzione](#introduzione)
2. [Prerequisiti](#prerequisiti)
   - 2.1 [Docker e Docker Compose](#docker-e-docker-compose)
   - 2.2 [Modalita' Nativa (facoltativa)](#modalita-nativa-facoltativa)
   - 2.3 [GPU NVIDIA (opzionale)](#gpu-nvidia-opzionale)
3. [Installazione Rapida](#installazione-rapida)
4. [Installazione Docker Completa](#installazione-docker-completa)
5. [Installazione Modalita' Sviluppatore](#installazione-modalita-sviluppatore)
6. [Configurazione .env](#configurazione-env)
7. [Comandi Utili](#comandi-utili)
8. [Troubleshooting](#troubleshooting)

---

## Introduzione

LocalMind e' una piattaforma AI local-first che consente la gestione di documenti, ricerca semantica e chat con modelli LLM. Il progetto supporta tre modalita' di installazione:

1. **Installazione Rapida** - Setup automatico con uno script (consigliato per principianti)
2. **Installazione Docker Completa** - Tutto in container (backend, frontend, infra)
3. **Installazione Sviluppatore** - Backend e frontend nativi, infra in Docker (consigliato per sviluppo)

---

## Prerequisiti

### Docker e Docker Compose

**Obbligatorio per tutte le modalita'.**

| Software | Versione minima | Verifica |
|----------|-----------------|----------|
| Docker | 24.0+ | \`docker --version\` |
| Docker Compose | 2.0+ | \`docker compose version\` |

#### Installazione Docker

**Su Ubuntu/Debian:**

\`\`\`bash
# Aggiornare i repository
sudo apt-get update

# Installare Docker
sudo apt-get install -y docker.io docker-compose-plugin

# Aggiungere l'utente al gruppo docker (per evitare sudo)
sudo usermod -aG docker $USER
newgrp docker

# Verificare l'installazione
docker --version
docker compose version
\`\`\`

**Su macOS:**

Scaricare Docker Desktop da https://www.docker.com/products/docker-desktop

**Su Windows:**

Scaricare Docker Desktop da https://www.docker.com/products/docker-desktop

### Modalita' Nativa (facoltativa)

Se si desidera eseguire backend e frontend nativamente (senza container), installare i seguenti componenti:

| Software | Versione minima | Verifica |
|----------|-----------------|----------|
| Java JDK | 17+ | \`java -version\` |
| Maven | 3.9+ | \`mvn -version\` |
| Node.js | 22+ | \`node -v\` |
| npm | 11+ | \`npm -v\` |
| Git | 2.40+ | \`git --version\` |

#### Installazione su Ubuntu/Debian

\`\`\`bash
# Java JDK 17
sudo apt-get install -y openjdk-17-jdk

# Maven
sudo apt-get install -y maven

# Node.js 22 (tramite NodeSource repository)
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt-get install -y nodejs

# Git
sudo apt-get install -y git
\`\`\`

#### Installazione su macOS

\`\`\`bash
# Usando Homebrew
brew install java17 maven node git
\`\`\`

### GPU NVIDIA (opzionale)

Per accelerare Ollama con GPU NVIDIA:

| Componente | Versione minima |
|------------|-----------------|
| NVIDIA Driver | 525+ |
| CUDA Toolkit | 11.8+ |
| NVIDIA Container Toolkit | 1.13+ (solo per Docker) |

**Verifica GPU disponibile:**

\`\`\`bash
nvidia-smi
\`\`\`

Se non è installato, seguire la guida ufficiale NVIDIA.

---

## Installazione Rapida

La modalita' piu' semplice per iniziare. Uno script automatizzato configurera' tutto per te.

### Step 1: Clonare il Repository

\`\`\`bash
git clone <repository-url> localmind
cd localmind
\`\`\`

### Step 2: Eseguire lo Script di Setup

\`\`\`bash
./scripts/setup.sh
\`\`\`

Lo script guifera' attraverso i seguenti passaggi:

1. Verifica prerequisiti (Docker, Docker Compose)
2. Copia del file \`.env\` da \`.env.example\`
3. Richiesta configurazione base (password MySQL, API key opzionali)
4. Avvio infrastruttura Docker (MySQL, Qdrant, Ollama)
5. Download modelli Ollama (llama3.2, nomic-embed-text)
6. Compilazione e avvio backend
7. Installazione dipendenze e avvio frontend

### Step 3: Accedere all'Applicazione

Dopo il completamento dello script:

- **Frontend**: Aprire http://localhost:4200 nel browser
- **Backend API**: http://localhost:8080/api/v1
- **Ollama**: http://localhost:11434

---

## Installazione Docker Completa

Questa modalita' esegue backend, frontend e infrastruttura completamente in container Docker. Ideale per ambienti di produzione e per chi non vuole installare Java, Maven, Node.js nativamente.

### Step 1: Clonare il Repository

\`\`\`bash
git clone <repository-url> localmind
cd localmind
\`\`\`

### Step 2: Configurare il File .env

\`\`\`bash
cp .env.example .env
\`\`\`

Editare il file \`.env\` per adattarlo a Docker:

\`\`\`env
# Database MySQL - in Docker, l'host e' il nome del servizio
DB_HOST=mysql
DB_PORT=3306
DB_NAME=localmind
DB_USERNAME=root
DB_PASSWORD=localmind

# Ollama - in Docker, l'host e' il nome del servizio
OLLAMA_HOST=ollama
OLLAMA_PORT=11434
OLLAMA_CHAT_MODEL=llama3.2
OLLAMA_EMBED_MODEL=nomic-embed-text

# Qdrant - in Docker, l'host e' il nome del servizio
QDRANT_HOST=qdrant
QDRANT_PORT=6334

# Altra configurazione
SERVER_PORT=8080
LLM_DEFAULT_PROVIDER=OLLAMA
\`\`\`

### Step 3: Avviare i Servizi

\`\`\`bash
# Avviare il full stack in background
docker compose --profile full up -d --build
\`\`\`

**Nota:** La prima build puo' richiedere 5-10 minuti.

### Step 4: Attendere l'Avvio

\`\`\`bash
# Monitorare i log
docker compose logs -f

# Oppure verificare lo stato dei servizi
docker compose ps
\`\`\`

Attendere finche' i seguenti servizi risultino \`healthy\` o \`running\`:
- \`localmind-mysql\`
- \`localmind-qdrant\`
- \`localmind-ollama\`
- \`localmind-backend\`
- \`localmind-frontend\`

### Step 5: Accedere all'Applicazione

- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080/api/v1
- **Ollama**: http://localhost:11434
- **Qdrant**: http://localhost:6333

### Fermare i Servizi

\`\`\`bash
# Fermare e preservare i dati
docker compose down

# Fermare e cancellare i dati (ATTENZIONE: distruttivo)
docker compose down -v
\`\`\`

---

## Installazione Modalita' Sviluppatore

Modalita' consigliata per lo sviluppo. Backend e frontend girano nativamente, mentre l'infrastruttura (MySQL, Qdrant, Ollama) e' in Docker.

### Step 1: Clonare il Repository

\`\`\`bash
git clone <repository-url> localmind
cd localmind
\`\`\`

### Step 2: Configurare il File .env

\`\`\`bash
cp .env.example .env
\`\`\`

Verificare che i seguenti valori siano corretti per la modalita' nativa:

\`\`\`env
DB_HOST=localhost
DB_PORT=3306
OLLAMA_HOST=localhost
OLLAMA_PORT=11434
QDRANT_HOST=localhost
QDRANT_PORT=6334
\`\`\`

### Step 3: Avviare l'Infrastruttura Docker

\`\`\`bash
# Avviare solo i servizi infrastrutturali
docker compose up -d mysql qdrant ollama
\`\`\`

**Nota:** Se si preferi avviare solo alcuni servizi:

\`\`\`bash
# Solo MySQL e Qdrant (senza Ollama)
docker compose up -d mysql qdrant

# Solo Ollama
docker compose up -d ollama
\`\`\`

### Step 4: Scaricare i Modelli Ollama

\`\`\`bash
# Modello di chat
docker exec -it localmind-ollama ollama pull llama3.2

# Modello di embedding (per RAG)
docker exec -it localmind-ollama ollama pull nomic-embed-text

# Verificare i modelli installati
docker exec localmind-ollama ollama list
\`\`\`

**Tempo stimato:** 10-20 minuti (dipende dalla connessione internet).

### Step 5: Avviare il Backend

\`\`\`bash
# Usando lo script fornito
./scripts/start-backend.sh

# Oppure manualmente:
cd localmind-backend
mvn clean install -DskipTests
mvn -pl localmind-app spring-boot:run -Dspring-boot.run.profiles=dev
\`\`\`

Output atteso:

\`\`\`
Tomcat started on port 8080 (http)
Started LocalMindApplication in X.XXX seconds
\`\`\`

### Step 6: Avviare il Frontend

In un **nuovo terminale**:

\`\`\`bash
# Usando lo script fornito
./scripts/start-frontend.sh

# Oppure manualmente:
cd localmind-frontend
npm install
npm start
\`\`\`

Output atteso:

\`\`\`
** Angular Live Development Server is listening on localhost:4200 **
✔ Compiled successfully.
\`\`\`

### Step 7: Accedere all'Applicazione

- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080/api/v1

### Avvio Rapido

Per avviare backend e frontend insieme in un singolo comando:

\`\`\`bash
./scripts/start-all.sh
\`\`\`

Questo script aprira' due terminali: uno per il backend, uno per il frontend.

### Fermare i Servizi

Backend e frontend:

\`\`\`bash
# Interrompere i processi nei rispettivi terminali con Ctrl+C
\`\`\`

Infrastruttura Docker:

\`\`\`bash
docker compose down
\`\`\`

---

## Configurazione .env

Il file \`.env\` contiene tutte le variabili di configurazione necessarie. Copiare da \`.env.example\` e personalizzare:

### Database MySQL

| Variabile | Default | Descrizione |
|-----------|---------|-------------|
| \`DB_HOST\` | localhost | Host MySQL (localhost per nativo, mysql per Docker) |
| \`DB_PORT\` | 3306 | Porta MySQL |
| \`DB_NAME\` | localmind | Nome del database |
| \`DB_USERNAME\` | root | Username MySQL |
| \`DB_PASSWORD\` | (vuoto) | Password MySQL |

### Server

| Variabile | Default | Descrizione |
|-----------|---------|-------------|
| \`SERVER_PORT\` | 8080 | Porta backend Spring Boot |

### Ollama (LLM Locale)

| Variabile | Default | Descrizione |
|-----------|---------|-------------|
| \`OLLAMA_HOST\` | localhost | Host Ollama (localhost per nativo, ollama per Docker) |
| \`OLLAMA_PORT\` | 11434 | Porta Ollama |
| \`OLLAMA_CHAT_MODEL\` | llama3.2 | Modello per chat |
| \`OLLAMA_EMBED_MODEL\` | nomic-embed-text | Modello per embedding |

### Provider LLM

| Variabile | Default | Descrizione |
|-----------|---------|-------------|
| \`LLM_DEFAULT_PROVIDER\` | OLLAMA | Provider predefinito: OLLAMA, OPENAI, ANTHROPIC, GOOGLE |
| \`LLM_OLLAMA_ENABLED\` | true | Abilita Ollama |
| \`LLM_OPENAI_ENABLED\` | false | Abilita OpenAI |
| \`LLM_ANTHROPIC_ENABLED\` | false | Abilita Anthropic |
| \`LLM_GOOGLE_ENABLED\` | false | Abilita Google Gemini |

### API Keys Cloud (opzionali)

| Variabile | Descrizione |
|-----------|-------------|
| \`OPENAI_API_KEY\` | Chiave API OpenAI (se vuoto, OpenAI disabilitato) |
| \`OPENAI_MODEL\` | Modello OpenAI (default: gpt-4o) |
| \`ANTHROPIC_API_KEY\` | Chiave API Anthropic (se vuoto, Anthropic disabilitato) |
| \`ANTHROPIC_MODEL\` | Modello Anthropic (default: claude-sonnet-4-20250514) |
| \`GOOGLE_API_KEY\` | Chiave API Google (se vuoto, Google disabilitato) |

### Qdrant (Vector Store)

| Variabile | Default | Descrizione |
|-----------|---------|-------------|
| \`QDRANT_HOST\` | localhost | Host Qdrant (localhost per nativo, qdrant per Docker) |
| \`QDRANT_PORT\` | 6334 | Porta Qdrant gRPC |
| \`QDRANT_COLLECTION\` | localmind-documents | Nome della collezione vettoriale |

### n8n (Automazioni)

| Variabile | Default | Descrizione |
|-----------|---------|-------------|
| \`N8N_HOST\` | localhost | Host n8n |
| \`N8N_PORT\` | 5678 | Porta n8n |
| \`N8N_BASIC_AUTH_USER\` | admin | Username n8n |
| \`N8N_BASIC_AUTH_PASSWORD\` | localmind | Password n8n |

### Documenti

| Variabile | Default | Descrizione |
|-----------|---------|-------------|
| \`DOCUMENT_UPLOAD_DIR\` | ~/.localmind/uploads | Directory per caricamento documenti |
| \`MAX_FILE_SIZE\` | 50MB | Dimensione massima file |

---

## Comandi Utili

### Monitoraggio Servizi Docker

\`\`\`bash
# Stato di tutti i servizi
docker compose ps

# Stato dettagliato di un servizio
docker compose ps mysql

# Log di tutti i servizi (live)
docker compose logs -f

# Log di un servizio specifico (ultime 100 righe, live)
docker compose logs -f --tail=100 ollama

# Log di backend (contenitore)
docker compose logs -f backend

# Statistiche risorse (CPU, RAM, I/O)
docker stats
\`\`\`

### Gestione Servizi Docker

\`\`\`bash
# Avviare un singolo servizio
docker compose up -d qdrant

# Fermare un servizio (preserva dati)
docker compose stop ollama

# Riavviare un servizio
docker compose restart mysql

# Ricreare un servizio (dopo modifica docker-compose.yml)
docker compose up -d --force-recreate ollama

# Fermare tutti i servizi
docker compose stop

# Fermare e rimuovere container (preserva volumi)
docker compose down

# Fermare, rimuovere container E volumi (DISTRUTTIVO)
docker compose down -v
\`\`\`

### Accesso ai Container

\`\`\`bash
# Shell in container Ollama
docker exec -it localmind-ollama bash

# Eseguire comando in container Ollama
docker exec localmind-ollama ollama list

# Shell in container MySQL
docker exec -it localmind-mysql mysql -u root -p
\`\`\`

### Gestione Modelli Ollama

\`\`\`bash
# Elencare modelli installati
docker exec localmind-ollama ollama list

# Scaricare un modello
docker exec localmind-ollama ollama pull llama2

# Rimuovere un modello
docker exec localmind-ollama ollama rm llama2

# Mostrare dettagli di un modello
docker exec localmind-ollama ollama show llama3.2
\`\`\`

### Backup e Ripristino

\`\`\`bash
# Backup del volume Ollama (modelli)
docker run --rm -v localmind_localmind-ollama-data:/data -v $(pwd):/backup \\
  busybox tar czf /backup/ollama-backup.tar.gz -C /data .

# Backup del volume Qdrant
docker run --rm -v localmind_localmind-qdrant-data:/data -v $(pwd):/backup \\
  busybox tar czf /backup/qdrant-backup.tar.gz -C /data .

# Ripristino Ollama
docker run --rm -v localmind_localmind-ollama-data:/data -v $(pwd):/backup \\
  busybox tar xzf /backup/ollama-backup.tar.gz -C /data

# Ripristino Qdrant
docker run --rm -v localmind_localmind-qdrant-data:/data -v $(pwd):/backup \\
  busybox tar xzf /backup/qdrant-backup.tar.gz -C /data
\`\`\`

### Pulizia e Manutenzione

\`\`\`bash
# Rimuovere volumi non utilizzati
docker volume prune

# Rimuovere immagini non utilizzate
docker image prune

# Pulizia completa (immagini, container, reti non utilizzate)
docker system prune

# Pulizia aggressiva (include volumi)
docker system prune -a --volumes
\`\`\`

---

## Troubleshooting

### Docker Compose non trovato

**Sintomo:** \`command not found: docker compose\`

**Soluzione:**

\`\`\`bash
# Verificare versione Docker Compose
docker compose version

# Se non installato, installare Docker Compose V2
sudo apt-get install docker-compose-plugin

# Oppure usare il comando legacy
docker-compose --version
\`\`\`

### Porta gia' in uso

**Sintomo:** \`Error response from daemon: Ports are not available\` o \`Address already in use\`

**Soluzione:**

\`\`\`bash
# Identificare il processo che occupa la porta (esempio: porta 8080)
sudo lsof -i :8080

# Oppure con netstat
sudo netstat -tlnp | grep 8080

# Terminare il processo
kill -9 <PID>

# Alternativa: modificare la porta in docker-compose.yml o .env
\`\`\`

### MySQL non si avvia

**Sintomo:** \`localmind-mysql\` rimane in stato \`restarting\` o \`exited\`

**Cause e soluzioni:**

1. **Porta 3306 occupata:**
   \`\`\`bash
   # Cercare il processo
   sudo lsof -i :3306
   # Terminare o cambiare porta
   \`\`\`

2. **Permessi di volume:**
   \`\`\`bash
   # Verificare i log
   docker compose logs mysql
   # Reset del volume
   docker compose down
   docker volume rm localmind_localmind-mysql-data
   docker compose up -d mysql
   \`\`\`

3. **Corruzione del database:**
   \`\`\`bash
   # Backup dei dati
   docker volume inspect localmind_localmind-mysql-data
   # Eliminare e ricreate
   docker compose down -v
   docker compose up -d mysql
   \`\`\`

### Ollama lento al primo avvio

**Sintomo:** I modelli impiegano molto tempo a scaricare

**Cause e soluzioni:**

1. **Connessione internet lenta:**
   - Attendere il completamento del download
   - Verificare con \`docker compose logs ollama\`

2. **Spazio disco insufficiente:**
   \`\`\`bash
   # Verificare spazio disponibile
   df -h
   # I modelli llama3.2 e nomic-embed-text richiedono ~2.3 GB
   \`\`\`

3. **Modello gia' in fase di download:**
   \`\`\`bash
   # Verificare i log
   docker compose logs -f ollama
   \`\`\`

### Backend non si connette a MySQL

**Sintomo:** \`Connection refused\` o \`FATAL ERROR in mysql\`

**Cause e soluzioni:**

1. **MySQL non avviato:**
   \`\`\`bash
   docker compose up -d mysql
   docker compose logs mysql
   \`\`\`

2. **Credenziali errate nel .env:**
   \`\`\`bash
   # Verificare .env
   cat .env | grep DB_
   # Aggiornare se necessario e riavviare
   docker compose restart backend
   \`\`\`

3. **Host sbagliato in .env:**
   \`\`\`env
   # Per Docker completo, deve essere:
   DB_HOST=mysql
   # Per modalita' nativa (backend nativo, MySQL in Docker):
   DB_HOST=localhost
   \`\`\`

4. **Reset della connessione:**
   \`\`\`bash
   docker compose down
   docker compose up -d mysql
   # Attendere 10 secondi
   docker compose up -d backend
   \`\`\`

### Backend/Frontend non raggiungibili

**Sintomo:** \`ERR_CONNECTION_REFUSED\` nel browser

**Soluzioni:**

1. **Verificare che i servizi siano in esecuzione:**
   \`\`\`bash
   # Per Docker completo
   docker compose ps backend frontend
   # Per modalita' nativa
   curl http://localhost:8080/api/v1/dashboard/health
   curl http://localhost:4200
   \`\`\`

2. **Verificare i log:**
   \`\`\`bash
   # Backend
   docker compose logs -f backend
   # Oppure nel terminale del backend nativo
   \`\`\`

3. **Attendere l'avvio (puo' richiedere 1-2 minuti):**
   \`\`\`bash
   # Verificare ripetutamente
   curl http://localhost:8080/api/v1/dashboard/health
   \`\`\`

### Qdrant non raggiungibile

**Sintomo:** Errore nella ricerca semantica o upload documenti

**Soluzione:**

\`\`\`bash
# Verificare che Qdrant sia avviato
docker compose ps qdrant

# Verificare la connessione
curl http://localhost:6333/healthz

# Riavviare Qdrant
docker compose restart qdrant
\`\`\`

### Frontend mostra errore di API

**Sintomo:** Nella console del browser: \`Failed to fetch from /api/v1/...\`

**Cause e soluzioni:**

1. **Backend non avviato:**
   \`\`\`bash
   curl http://localhost:8080/api/v1/dashboard/health
   # Se non risponde, riavviare il backend
   \`\`\`

2. **CORS non configurato:**
   - Verificare in \`application-dev.yml\` la configurazione CORS
   - Riavviare il backend

3. **Firewall o proxy che blocca:**
   - Verificare le impostazioni di rete
   - Controllare i log del browser

### Problemi di memoria con Ollama

**Sintomo:** Ollama non risponde o processo terminato per OOM

**Soluzioni:**

1. **Usare un modello piu' leggero:**
   \`\`\`bash
   docker exec localmind-ollama ollama pull llama3.2:1b
   # Aggiornare in .env
   OLLAMA_CHAT_MODEL=llama3.2:1b
   \`\`\`

2. **Verificare RAM disponibile:**
   \`\`\`bash
   free -h
   docker stats localmind-ollama
   \`\`\`

3. **Abilitare GPU (se disponibile):**
   - Verificare che \`nvidia-smi\` funzioni
   - Decommentare la sezione \`deploy.resources\` nel servizio \`ollama\` di docker-compose.yml
   - Riavviare: \`docker compose up -d --force-recreate ollama\`

### Docker Compose hang o timeout

**Sintomo:** \`docker compose up\` si blocca oppure \`timeout\` durante l'avvio

**Soluzione:**

\`\`\`bash
# Interrompere il processo
Ctrl+C

# Verificare lo stato
docker compose ps

# Pulire e ricominciare
docker compose down
docker compose up -d --force-recreate

# Monitorare i log per errori
docker compose logs -f
\`\`\`

### Migrazione Flyway fallisce

**Sintomo:** Backend non avvia con errore di migrazione database

**Soluzione:**

\`\`\`bash
# Verificare i log
docker compose logs -f backend

# Reset del database (ATTENZIONE: cancella tutti i dati)
docker compose exec mysql mysql -u root -p$DB_PASSWORD -e "DROP DATABASE localmind;"
docker compose exec mysql mysql -u root -p$DB_PASSWORD -e "CREATE DATABASE localmind CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Riavviare il backend
docker compose restart backend
\`\`\`

---

## Verifica dell'Installazione

Dopo l'installazione, verificare che tutto sia funzionante:

### Checklist

| Componente | Comando/URL | Stato atteso |
|---|---|---|
| Docker | \`docker --version\` | Version 24.0+ |
| Docker Compose | \`docker compose version\` | Version v2.0+ |
| MySQL | \`docker compose ps mysql\` | running/healthy |
| Qdrant | \`curl http://localhost:6333/healthz\` | 200 OK |
| Ollama | \`curl http://localhost:11434/api/tags\` | JSON con modelli |
| Backend | \`curl http://localhost:8080/api/v1/dashboard/health\` | JSON con status UP |
| Frontend | http://localhost:4200 | Interfaccia visibile |

### Mappa delle Porte

\`\`\`
┌─────────────────────────────────────────────┐
│                  localhost                  │
│                                             │
│   :4200  ─── Angular Frontend (dev server)  │
│   :8080  ─── Spring Boot Backend API        │
│   :3306  ─── MySQL Database                 │
│   :6333  ─── Qdrant REST API                │
│   :6334  ─── Qdrant gRPC                    │
│   :11434 ─── Ollama LLM Inference           │
│   :5678  ─── n8n Workflow Automation        │
│                                             │
└─────────────────────────────────────────────┘
\`\`\`

---

## Prossimi Passi

Dopo l'installazione e verifica:

1. **Carica i tuoi documenti:**
   - Accedi a http://localhost:4200
   - Naviga su "Documenti"
   - Carica i tuoi file (PDF, DOCX, TXT)

2. **Configura provider LLM aggiuntivi (opzionale):**
   - Vai su "Impostazioni" > "Provider LLM"
   - Aggiungi chiavi API per OpenAI, Anthropic, Google Gemini

3. **Esplora le funzionalita':**
   - Chat con i tuoi documenti usando RAG
   - Ricerca semantica nei documenti
   - Gestione cartelle per monitoring automatico

4. **Consulta la documentazione:**
   - Guida utente: \`documentazione/08-frontend/\`
   - API reference: \`documentation/06-api-reference/\`
   - Architettura: \`documentazione/01-architettura/\`

`;
