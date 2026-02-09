# Ambiente di Sviluppo

| | |
|---|---|
| **Documento** | Guida alla Configurazione dell'Ambiente di Sviluppo |
| **Versione** | 0.1.0 |
| **Data** | 2026-02-09 |
| **Progetto** | LocalMind |

---

## Indice

1. [Prerequisiti](#1-prerequisiti)
2. [Step 1: Clonare il Repository](#2-step-1-clonare-il-repository)
3. [Step 2: Configurare le Variabili d'Ambiente](#3-step-2-configurare-le-variabili-dambiente)
4. [Step 3: Avviare l'Infrastruttura Docker](#4-step-3-avviare-linfrastruttura-docker)
5. [Step 4: Scaricare i Modelli Ollama](#5-step-4-scaricare-i-modelli-ollama)
6. [Step 5: Compilare e Avviare il Backend](#6-step-5-compilare-e-avviare-il-backend)
7. [Step 6: Installare Dipendenze e Avviare il Frontend](#7-step-6-installare-dipendenze-e-avviare-il-frontend)
8. [Verifica dell'Installazione](#8-verifica-dellinstallazione)
9. [Troubleshooting](#9-troubleshooting)
   - 9.1 [Porta gia' in uso](#91-porta-gia-in-uso)
   - 9.2 [Ollama non risponde](#92-ollama-non-risponde)
   - 9.3 [PostgreSQL connection refused](#93-postgresql-connection-refused)
   - 9.4 [Angular build errors](#94-angular-build-errors)
   - 9.5 [Problemi di memoria con Ollama](#95-problemi-di-memoria-con-ollama)
   - 9.6 [Docker Compose non si avvia](#96-docker-compose-non-si-avvia)

---

## 1. Prerequisiti

Prima di procedere con la configurazione dell'ambiente di sviluppo, verificare che i seguenti strumenti siano installati e configurati correttamente sulla propria macchina.

### Software obbligatorio

| Software | Versione minima | Verifica installazione | Note |
|---|---|---|---|
| **Java JDK** | 17+ | `java -version` | Necessario il JDK completo, non il solo JRE |
| **Maven** | 3.9+ | `mvn -version` | Wrapper Maven (`mvnw`) incluso nel progetto |
| **Node.js** | 22+ | `node -v` | Runtime per il frontend Angular |
| **npm** | 11+ | `npm -v` | Package manager per Node.js |
| **Docker** | 24+ | `docker --version` | Container runtime |
| **Docker Compose** | 2.20+ (V2) | `docker compose version` | Orchestrazione container |
| **Git** | 2.40+ | `git --version` | Version control |

### Software opzionale (raccomandato)

| Software | Utilizzo | Note |
|---|---|---|
| **IntelliJ IDEA** (Ultimate o Community) | IDE per lo sviluppo backend Java/Spring Boot | Plugin consigliati: Spring Boot, Lombok |
| **VS Code** | IDE alternativo o per lo sviluppo frontend | Estensioni consigliate: Angular Language Service, ESLint |
| **Postman** o **Insomnia** | Testing API REST | Alternativa: `curl` da terminale |
| **DBeaver** | Client GUI per PostgreSQL | Alternativa: `psql` da terminale |

### Verifica rapida dei prerequisiti

Eseguire il seguente script per verificare tutti i prerequisiti in un'unica operazione:

```bash
echo "=== Verifica Prerequisiti LocalMind ==="
echo ""

echo "Java:"
java -version 2>&1 | head -1
echo ""

echo "Maven:"
mvn -version 2>&1 | head -1
echo ""

echo "Node.js:"
node -v
echo ""

echo "npm:"
npm -v
echo ""

echo "Docker:"
docker --version
echo ""

echo "Docker Compose:"
docker compose version
echo ""

echo "Git:"
git --version
echo ""

echo "=== Verifica completata ==="
```

---

## 2. Step 1: Clonare il Repository

```bash
git clone <repository-url> localmind
cd localmind
```

### Struttura del repository dopo la clonazione

```
localmind/
├── localmind-backend/          # Backend Spring Boot (multi-modulo Maven)
│   ├── localmind-app/          # Modulo applicazione (main, controller)
│   ├── localmind-domain/       # Modulo dominio (entita', porte)
│   ├── localmind-application/  # Modulo servizi applicativi
│   ├── localmind-infrastructure/ # Modulo infrastruttura (adapter, config)
│   └── pom.xml                 # POM parent
├── localmind-frontend/         # Frontend Angular
│   ├── src/
│   ├── angular.json
│   └── package.json
├── docker-compose.yml          # Orchestrazione servizi Docker
├── .env.example                # Template variabili d'ambiente
├── .gitignore
└── README.md
```

---

## 3. Step 2: Configurare le Variabili d'Ambiente

```bash
cp .env.example .env
```

Editare il file `.env` con un editor di testo:

```bash
# Con VS Code
code .env

# Con nano
nano .env

# Con vim
vim .env
```

### Contenuto del file .env

```env
# ==================================================
# LocalMind - Environment Variables
# ==================================================

# PostgreSQL
DB_USERNAME=localmind
DB_PASSWORD=localmind

# LLM Providers (opzionali - lasciare vuoti per modalita' solo locale)
OPENAI_API_KEY=
ANTHROPIC_API_KEY=
GOOGLE_API_KEY=

# n8n
N8N_BASIC_AUTH_USER=admin
N8N_BASIC_AUTH_PASSWORD=admin
```

**Nota:** Per lo sviluppo locale, i valori di default per PostgreSQL e n8n sono sufficienti. Le API key dei provider cloud sono opzionali e possono essere aggiunte in un secondo momento.

---

## 4. Step 3: Avviare l'Infrastruttura Docker

```bash
docker-compose up -d
```

### Verifica dello stato dei container

```bash
docker-compose ps
```

Output atteso:

```
NAME                  STATUS                   PORTS
localmind-postgres    Up (healthy)             0.0.0.0:5432->5432/tcp
localmind-qdrant      Up (healthy)             0.0.0.0:6333->6333/tcp, 0.0.0.0:6334->6334/tcp
localmind-ollama      Up                       0.0.0.0:11434->11434/tcp
localmind-n8n         Up                       0.0.0.0:5678->5678/tcp
```

### Servizi e porte

| Servizio | Porta locale | Protocollo | Verifica |
|---|---|---|---|
| PostgreSQL | `5432` | TCP | `psql -h localhost -U localmind -d localmind` |
| Qdrant REST | `6333` | HTTP | `curl http://localhost:6333/healthz` |
| Qdrant gRPC | `6334` | gRPC | - |
| Ollama | `11434` | HTTP | `curl http://localhost:11434/api/tags` |
| n8n | `5678` | HTTP | Aprire `http://localhost:5678` nel browser |

### Verifica rapida dell'infrastruttura

```bash
echo "PostgreSQL:"
docker exec localmind-postgres pg_isready -U localmind && echo "OK" || echo "ERRORE"

echo ""
echo "Qdrant:"
curl -s http://localhost:6333/healthz && echo " OK" || echo "ERRORE"

echo ""
echo "Ollama:"
curl -s http://localhost:11434/api/tags > /dev/null && echo "OK" || echo "ERRORE"

echo ""
echo "n8n:"
curl -s -o /dev/null -w "%{http_code}" http://localhost:5678 && echo " OK" || echo "ERRORE"
```

---

## 5. Step 4: Scaricare i Modelli Ollama

Dopo il primo avvio del container Ollama, e' necessario scaricare i modelli LLM che verranno utilizzati da LocalMind.

### Modello di chat (obbligatorio)

```bash
docker exec -it localmind-ollama ollama pull llama3.2
```

Tempo stimato: 5-15 minuti (dipende dalla connessione internet e dalla dimensione del modello).

### Modello di embedding (obbligatorio per RAG)

```bash
docker exec -it localmind-ollama ollama pull nomic-embed-text
```

Tempo stimato: 1-3 minuti.

### Verifica dei modelli installati

```bash
docker exec localmind-ollama ollama list
```

Output atteso:

```
NAME                    ID              SIZE      MODIFIED
llama3.2:latest         xxxxxxxxx       2.0 GB    x minutes ago
nomic-embed-text:latest xxxxxxxxx       274 MB    x minutes ago
```

### Modelli alternativi (opzionali)

| Modello | Dimensione | Utilizzo | Comando |
|---|---|---|---|
| `llama3.2` | ~2 GB | Chat (default, buon bilanciamento) | `ollama pull llama3.2` |
| `llama3.2:1b` | ~1.3 GB | Chat (leggero, per macchine con poca RAM) | `ollama pull llama3.2:1b` |
| `mistral` | ~4 GB | Chat (alternativa a Llama) | `ollama pull mistral` |
| `codellama` | ~3.8 GB | Generazione codice | `ollama pull codellama` |
| `nomic-embed-text` | ~274 MB | Embedding (default) | `ollama pull nomic-embed-text` |
| `mxbai-embed-large` | ~670 MB | Embedding (maggiore qualita') | `ollama pull mxbai-embed-large` |

**Nota:** I modelli vengono salvati nel volume Docker `localmind-ollama-data` e persistono tra i riavvii del container.

---

## 6. Step 5: Compilare e Avviare il Backend

### Compilazione

```bash
cd localmind-backend
mvn clean compile
```

Output atteso alla fine della compilazione:

```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO]
[INFO] localmind-backend ................................. SUCCESS
[INFO] localmind-domain .................................. SUCCESS
[INFO] localmind-application ............................. SUCCESS
[INFO] localmind-infrastructure .......................... SUCCESS
[INFO] localmind-app ..................................... SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Avvio dell'applicazione

```bash
mvn spring-boot:run -pl localmind-app
```

Output atteso all'avvio:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.x.x)

...
Tomcat started on port 8080 (http)
Started LocalMindApplication in X.XXX seconds
```

### Verifica del backend

```bash
curl http://localhost:8080/api/v1/dashboard/health
```

Risposta attesa:

```json
{
  "status": "UP",
  "components": {
    "ollama": { "status": "UP" },
    "postgres": { "status": "UP" },
    "qdrant": { "status": "UP" }
  }
}
```

---

## 7. Step 6: Installare Dipendenze e Avviare il Frontend

### Installazione dipendenze

```bash
cd localmind-frontend
npm install
```

Tempo stimato: 1-3 minuti (primo avvio). Le dipendenze vengono cachate in `node_modules/`.

### Avvio del dev server Angular

```bash
ng serve
```

Oppure tramite npm script:

```bash
npm start
```

Output atteso:

```
** Angular Live Development Server is listening on localhost:4200 **

✔ Compiled successfully.
```

### Verifica del frontend

Aprire nel browser: **http://localhost:4200**

La pagina principale dell'applicazione LocalMind dovrebbe essere visualizzata. Il dev server Angular supporta il live reload: ogni modifica ai file sorgente causera' il ricaricamento automatico della pagina nel browser.

---

## 8. Verifica dell'Installazione

Dopo aver completato tutti gli step, verificare che l'intero stack sia funzionante:

### Checklist di verifica

| Componente | URL/Comando | Stato atteso |
|---|---|---|
| PostgreSQL | `docker exec localmind-postgres pg_isready` | `/var/run/postgresql:5432 - accepting connections` |
| Qdrant | `curl http://localhost:6333/healthz` | `OK` (HTTP 200) |
| Ollama | `curl http://localhost:11434/api/tags` | JSON con lista modelli |
| n8n | `http://localhost:5678` (browser) | Interfaccia web n8n |
| Backend | `curl http://localhost:8080/api/v1/dashboard/health` | JSON con status UP |
| Frontend | `http://localhost:4200` (browser) | UI LocalMind |

### Mappa delle porte

```
┌─────────────────────────────────────────────┐
│                  localhost                     │
│                                               │
│   :4200  ─── Angular Dev Server (Frontend)    │
│   :8080  ─── Spring Boot (Backend API)        │
│   :5432  ─── PostgreSQL (Database)            │
│   :6333  ─── Qdrant REST API (Vector Store)   │
│   :6334  ─── Qdrant gRPC (Vector Store)       │
│   :11434 ─── Ollama (LLM Inference)           │
│   :5678  ─── n8n (Workflow Automation)         │
│                                               │
└─────────────────────────────────────────────┘
```

---

## 9. Troubleshooting

### 9.1 Porta gia' in uso

**Sintomo:** Errore all'avvio di un servizio con messaggio `Address already in use` o `port is already allocated`.

**Causa:** Un altro processo sta gia' utilizzando la porta richiesta.

**Soluzione:**

```bash
# Identificare il processo che occupa la porta (esempio: porta 8080)
sudo lsof -i :8080

# Oppure con netstat
sudo netstat -tlnp | grep 8080

# Terminare il processo (sostituire PID con il valore trovato)
kill -9 <PID>
```

**Alternativa:** Modificare la porta nel file di configurazione:

- Backend Spring Boot: modificare `server.port` in `application.yml`
- Docker services: modificare il port mapping in `docker-compose.yml`

### 9.2 Ollama non risponde

**Sintomo:** `curl http://localhost:11434/api/tags` restituisce `Connection refused` o timeout.

**Cause possibili e soluzioni:**

1. **Container non avviato:**
   ```bash
   docker-compose ps | grep ollama
   # Se il container non e' in esecuzione:
   docker-compose up -d ollama
   ```

2. **Container in fase di avvio (modello in caricamento):**
   ```bash
   docker-compose logs -f ollama
   # Attendere che il log mostri "Listening on 0.0.0.0:11434"
   ```

3. **Risorse insufficienti:**
   ```bash
   # Verificare l'utilizzo di memoria
   docker stats localmind-ollama
   # Se la memoria e' al 100%, considerare un modello piu' leggero
   ```

4. **Conflitto con Ollama installato nativamente:**
   ```bash
   # Verificare se Ollama e' installato anche nativamente
   which ollama
   # Se presente, fermarlo per evitare conflitti di porta
   sudo systemctl stop ollama
   ```

### 9.3 PostgreSQL connection refused

**Sintomo:** L'applicazione Spring Boot non riesce a connettersi a PostgreSQL con errore `Connection refused`.

**Cause possibili e soluzioni:**

1. **Container non avviato o non healthy:**
   ```bash
   docker-compose ps | grep postgres
   # Se lo stato non e' "healthy":
   docker-compose logs postgres
   ```

2. **Credenziali errate:**
   ```bash
   # Verificare le credenziali nel .env
   cat .env | grep DB_
   # Tentare la connessione manuale
   docker exec -it localmind-postgres psql -U localmind -d localmind -c "SELECT 1;"
   ```

3. **Database non ancora creato:**
   ```bash
   # Il database viene creato automaticamente dal container
   # Se necessario, crearlo manualmente:
   docker exec -it localmind-postgres createdb -U localmind localmind
   ```

4. **Volume corrotto:**
   ```bash
   # ATTENZIONE: questo cancella tutti i dati del database!
   docker-compose down
   docker volume rm localmind_localmind-postgres-data
   docker-compose up -d
   ```

### 9.4 Angular build errors

**Sintomo:** `ng serve` restituisce errori di compilazione.

**Cause possibili e soluzioni:**

1. **Dipendenze non installate:**
   ```bash
   cd localmind-frontend
   rm -rf node_modules package-lock.json
   npm install
   ```

2. **Versione Node.js non compatibile:**
   ```bash
   node -v
   # Se la versione e' inferiore alla 22, aggiornare Node.js
   # Con nvm:
   nvm install 22
   nvm use 22
   ```

3. **Cache Angular corrotta:**
   ```bash
   cd localmind-frontend
   rm -rf .angular/cache
   ng serve
   ```

4. **Errori di tipo TypeScript:**
   ```bash
   # Verificare i tipi
   npx tsc --noEmit
   ```

### 9.5 Problemi di memoria con Ollama

**Sintomo:** Ollama risponde lentamente o il container viene terminato per Out Of Memory (OOM).

**Cause e soluzioni:**

1. **Modello troppo grande per la RAM disponibile:**
   ```bash
   # Verificare la RAM disponibile
   free -h
   # Utilizzare un modello piu' leggero
   docker exec -it localmind-ollama ollama pull llama3.2:1b
   ```

2. **Aumentare la memoria Docker:**
   - Su Docker Desktop: Settings -> Resources -> Memory -> aumentare il limite.
   - Su Linux: Docker utilizza tutta la RAM disponibile per default.

3. **Utilizzare la GPU (se disponibile):**
   Verificare che il supporto GPU sia configurato in `docker-compose.yml`:
   ```yaml
   ollama:
     deploy:
       resources:
         reservations:
           devices:
             - driver: nvidia
               count: all
               capabilities: [gpu]
   ```

### 9.6 Docker Compose non si avvia

**Sintomo:** `docker-compose up -d` restituisce errori.

**Cause possibili e soluzioni:**

1. **Docker daemon non in esecuzione:**
   ```bash
   sudo systemctl status docker
   # Se non attivo:
   sudo systemctl start docker
   ```

2. **Versione Docker Compose obsoleta:**
   ```bash
   docker compose version
   # Se la versione e' inferiore alla 2.20, aggiornare Docker
   ```

3. **Conflitti di rete Docker:**
   ```bash
   # Rimuovere reti orfane
   docker network prune
   ```

4. **Spazio disco insufficiente:**
   ```bash
   df -h
   # Liberare spazio se necessario
   docker system prune -a
   ```
