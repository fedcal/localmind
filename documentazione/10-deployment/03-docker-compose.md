# Configurazione Docker Compose

| | |
|---|---|
| **Documento** | Documentazione Configurazione Docker Compose |
| **Versione** | 0.1.0 |
| **Data** | 2026-02-09 |
| **Progetto** | LocalMind |

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Struttura del File](#2-struttura-del-file)
3. [Servizi Dettagliati](#3-servizi-dettagliati)
   - 3.1 [PostgreSQL](#31-postgresql)
   - 3.2 [Qdrant](#32-qdrant)
   - 3.3 [Ollama](#33-ollama)
   - 3.4 [n8n](#34-n8n)
4. [Network](#4-network)
5. [Volumes](#5-volumes)
6. [Comandi Utili](#6-comandi-utili)
7. [Configurazione GPU per Ollama](#7-configurazione-gpu-per-ollama)
8. [Personalizzazione](#8-personalizzazione)

---

## 1. Panoramica

L'infrastruttura di LocalMind e' orchestrata tramite **Docker Compose** nella versione V2 (formato `services`, senza il campo `version` deprecato). Il file `docker-compose.yml` si trova nella **directory root del progetto**.

Docker Compose gestisce quattro servizi principali:

| Servizio | Funzione | Immagine |
|---|---|---|
| **postgres** | Database relazionale per persistence strutturata | `postgres:16-alpine` |
| **qdrant** | Vector store per embedding e ricerca semantica | `qdrant/qdrant:latest` |
| **ollama** | LLM inference locale | `ollama/ollama:latest` |
| **n8n** | Workflow automation | `n8nio/n8n:latest` |

Tutti i servizi sono configurati con:

- **Volumes persistenti**: i dati sopravvivono al riavvio dei container.
- **Health checks**: Docker verifica periodicamente lo stato di salute.
- **Restart policy**: i container vengono riavviati automaticamente in caso di crash.
- **Port mapping**: ogni servizio e' accessibile da `localhost`.

---

## 2. Struttura del File

```yaml
# docker-compose.yml
# LocalMind - Infrastructure Services
# Compose V2 format (no version field)

services:
  postgres:
    # ... configurazione PostgreSQL
  qdrant:
    # ... configurazione Qdrant
  ollama:
    # ... configurazione Ollama
  n8n:
    # ... configurazione n8n

volumes:
  localmind-postgres-data:
  localmind-qdrant-data:
  localmind-ollama-data:
  localmind-n8n-data:
```

---

## 3. Servizi Dettagliati

### 3.1 PostgreSQL

PostgreSQL 16 e' il database relazionale principale di LocalMind, utilizzato per la persistence di documenti, conversazioni, configurazioni, metriche e tutti i dati strutturati dell'applicazione.

```yaml
postgres:
  image: postgres:16-alpine
  container_name: localmind-postgres
  ports:
    - "5432:5432"
  environment:
    POSTGRES_DB: localmind
    POSTGRES_USER: ${DB_USERNAME:-localmind}
    POSTGRES_PASSWORD: ${DB_PASSWORD:-localmind}
  volumes:
    - localmind-postgres-data:/var/lib/postgresql/data
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME:-localmind}"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 30s
  restart: unless-stopped
```

#### Dettaglio configurazione

| Parametro | Valore | Descrizione |
|---|---|---|
| `image` | `postgres:16-alpine` | Immagine Alpine (dimensione ridotta ~80 MB vs ~400 MB per l'immagine standard) |
| `container_name` | `localmind-postgres` | Nome fisso del container per riferimenti cross-service |
| `ports` | `5432:5432` | Mapping porta host:container. Accessibile da `localhost:5432` |
| `POSTGRES_DB` | `localmind` | Nome del database creato automaticamente al primo avvio |
| `POSTGRES_USER` | `${DB_USERNAME:-localmind}` | Username. Valore da `.env`, default `localmind` |
| `POSTGRES_PASSWORD` | `${DB_PASSWORD:-localmind}` | Password. Valore da `.env`, default `localmind` |
| `volumes` | `localmind-postgres-data:/var/lib/postgresql/data` | Volume nominato per persistenza dati |
| `restart` | `unless-stopped` | Riavvio automatico tranne se fermato manualmente |

#### Health check

| Parametro | Valore | Descrizione |
|---|---|---|
| `test` | `pg_isready -U localmind` | Comando per verificare che PostgreSQL accetti connessioni |
| `interval` | `10s` | Frequenza del controllo |
| `timeout` | `5s` | Timeout massimo per il singolo controllo |
| `retries` | `5` | Numero di tentativi falliti prima di dichiarare `unhealthy` |
| `start_period` | `30s` | Periodo di grazia dopo l'avvio (i check falliti non contano) |

#### Connessione dall'applicazione Spring Boot

| Ambiente | URL JDBC |
|---|---|
| Sviluppo | `jdbc:postgresql://localhost:5432/localmind` |
| Produzione (Docker network) | `jdbc:postgresql://postgres:5432/localmind` |

---

### 3.2 Qdrant

Qdrant e' il vector store utilizzato per memorizzare gli embedding dei documenti e per eseguire ricerche semantiche ad alta performance.

```yaml
qdrant:
  image: qdrant/qdrant:latest
  container_name: localmind-qdrant
  ports:
    - "6333:6333"
    - "6334:6334"
  volumes:
    - localmind-qdrant-data:/qdrant/storage
  healthcheck:
    test: ["CMD-SHELL", "curl -f http://localhost:6333/healthz || exit 1"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 20s
  restart: unless-stopped
```

#### Dettaglio configurazione

| Parametro | Valore | Descrizione |
|---|---|---|
| `image` | `qdrant/qdrant:latest` | Ultima versione stabile di Qdrant |
| `container_name` | `localmind-qdrant` | Nome fisso del container |
| `ports` (REST) | `6333:6333` | API REST per operazioni CRUD e ricerca |
| `ports` (gRPC) | `6334:6334` | API gRPC per comunicazione ad alte prestazioni (usata da Spring AI) |
| `volumes` | `localmind-qdrant-data:/qdrant/storage` | Volume per persistenza collezioni e indici |
| `restart` | `unless-stopped` | Riavvio automatico |

#### Porte esposte

| Porta | Protocollo | Utilizzo |
|---|---|---|
| `6333` | HTTP/REST | API REST per gestione collezioni, upload punti, ricerca. Interfaccia web su `http://localhost:6333/dashboard` |
| `6334` | gRPC | Comunicazione ad alte prestazioni. Utilizzata da Spring AI per operazioni bulk e ricerca |

#### Connessione dall'applicazione Spring Boot

| Ambiente | Host | Porta |
|---|---|---|
| Sviluppo | `localhost` | `6334` (gRPC) |
| Produzione (Docker network) | `qdrant` | `6334` (gRPC) |

#### Risorse di storage

Qdrant memorizza i dati nel volume `localmind-qdrant-data`:

- **Collezioni**: strutture che raggruppano i vettori per dominio.
- **Segmenti**: suddivisioni interne per ottimizzazione delle ricerche.
- **Indici HNSW**: strutture dati per ricerca approssimata del nearest neighbor.

---

### 3.3 Ollama

Ollama fornisce l'inference LLM locale, permettendo di eseguire modelli di linguaggio senza connessione a servizi cloud.

```yaml
ollama:
  image: ollama/ollama:latest
  container_name: localmind-ollama
  ports:
    - "11434:11434"
  volumes:
    - localmind-ollama-data:/root/.ollama
  restart: unless-stopped
  # Per il supporto GPU NVIDIA, decommentare la sezione seguente:
  # deploy:
  #   resources:
  #     reservations:
  #       devices:
  #         - driver: nvidia
  #           count: all
  #           capabilities: [gpu]
```

#### Dettaglio configurazione

| Parametro | Valore | Descrizione |
|---|---|---|
| `image` | `ollama/ollama:latest` | Ultima versione stabile di Ollama |
| `container_name` | `localmind-ollama` | Nome fisso del container |
| `ports` | `11434:11434` | API REST per inference (compatibile con OpenAI API format) |
| `volumes` | `localmind-ollama-data:/root/.ollama` | Volume per persistenza modelli scaricati |
| `restart` | `unless-stopped` | Riavvio automatico |

#### Modelli da scaricare manualmente

Dopo il primo avvio del container, e' necessario scaricare i modelli manualmente:

```bash
# Modello di chat (obbligatorio)
docker exec -it localmind-ollama ollama pull llama3.2

# Modello di embedding (obbligatorio per RAG)
docker exec -it localmind-ollama ollama pull nomic-embed-text

# Verifica modelli installati
docker exec localmind-ollama ollama list
```

**Nota importante:** I modelli vengono salvati nel volume `localmind-ollama-data` e persistono tra i riavvii del container. Non e' necessario riscaricarli dopo un `docker-compose down` (che non rimuove i volumi) o un riavvio del container.

#### API Ollama

L'API di Ollama e' accessibile su `http://localhost:11434` e supporta i seguenti endpoint principali:

| Endpoint | Metodo | Descrizione |
|---|---|---|
| `/api/tags` | GET | Lista dei modelli installati |
| `/api/generate` | POST | Generazione testo (completion) |
| `/api/chat` | POST | Chat con cronologia messaggi |
| `/api/embeddings` | POST | Generazione embedding |
| `/api/pull` | POST | Download di un modello |
| `/api/show` | POST | Dettagli di un modello |

#### Risorse hardware

| Risorsa | Senza GPU | Con GPU NVIDIA |
|---|---|---|
| CPU | Utilizzata per inference (lento) | Utilizzata per preprocessing |
| RAM | 3-8 GB (dipende dal modello) | 2-4 GB (il modello risiede in VRAM) |
| VRAM | N/A | 3-8 GB (dipende dal modello) |
| Velocita' | ~5-15 token/s | ~30-100 token/s |

---

### 3.4 n8n

n8n e' la piattaforma di workflow automation utilizzata da LocalMind per gestire flussi automatizzati trigger-based.

```yaml
n8n:
  image: n8nio/n8n:latest
  container_name: localmind-n8n
  ports:
    - "5678:5678"
  environment:
    - N8N_BASIC_AUTH_ACTIVE=true
    - N8N_BASIC_AUTH_USER=${N8N_BASIC_AUTH_USER:-admin}
    - N8N_BASIC_AUTH_PASSWORD=${N8N_BASIC_AUTH_PASSWORD:-admin}
    - N8N_HOST=localhost
    - N8N_PORT=5678
    - N8N_PROTOCOL=http
    - WEBHOOK_URL=http://localhost:5678/
  volumes:
    - localmind-n8n-data:/home/node/.n8n
  depends_on:
    postgres:
      condition: service_healthy
  restart: unless-stopped
```

#### Dettaglio configurazione

| Parametro | Valore | Descrizione |
|---|---|---|
| `image` | `n8nio/n8n:latest` | Ultima versione stabile di n8n |
| `container_name` | `localmind-n8n` | Nome fisso del container |
| `ports` | `5678:5678` | Interfaccia web e API webhook |
| `N8N_BASIC_AUTH_ACTIVE` | `true` | Abilita l'autenticazione Basic Auth |
| `N8N_BASIC_AUTH_USER` | `${N8N_BASIC_AUTH_USER:-admin}` | Username da `.env`, default `admin` |
| `N8N_BASIC_AUTH_PASSWORD` | `${N8N_BASIC_AUTH_PASSWORD:-admin}` | Password da `.env`, default `admin` |
| `N8N_HOST` | `localhost` | Hostname per generazione URL webhook |
| `WEBHOOK_URL` | `http://localhost:5678/` | URL base per i webhook |
| `volumes` | `localmind-n8n-data:/home/node/.n8n` | Volume per persistenza workflow e credenziali |
| `depends_on` | `postgres: service_healthy` | n8n si avvia solo dopo che PostgreSQL e' healthy |
| `restart` | `unless-stopped` | Riavvio automatico |

#### Dipendenze

n8n dipende da PostgreSQL (`depends_on` con `condition: service_healthy`). Questo garantisce che:

1. Docker Compose avvii PostgreSQL prima di n8n.
2. n8n non si avvii finche' PostgreSQL non supera il health check.
3. I workflow n8n che interagiscono con il database trovino il servizio disponibile.

#### Interfaccia web

Accessibile su `http://localhost:5678` con le credenziali configurate nelle variabili d'ambiente.

L'interfaccia web di n8n consente di:

- Creare e modificare workflow visualmente (drag & drop).
- Configurare trigger (webhook, cron, eventi).
- Testare workflow in modalita' manuale.
- Monitorare le esecuzioni e consultare i log.

---

## 4. Network

Docker Compose crea automaticamente una rete bridge dedicata per i servizi definiti nel file:

```
localmind_default (bridge)
```

### Comunicazione tra servizi

Nella rete Docker interna, i servizi si raggiungono tramite il nome del servizio come hostname:

| Da | A | Hostname | Porta |
|---|---|---|---|
| Spring Boot | PostgreSQL | `postgres` | `5432` |
| Spring Boot | Qdrant | `qdrant` | `6334` (gRPC) |
| Spring Boot | Ollama | `ollama` | `11434` |
| Spring Boot | n8n | `n8n` | `5678` |
| n8n | PostgreSQL | `postgres` | `5432` |
| n8n | Spring Boot | `host.docker.internal` o IP host | `8080` |

### Isolamento di rete

La rete Docker interna garantisce che:

- I servizi possono comunicare tra loro senza esporre porte all'esterno.
- Le porte mappate (`ports`) rendono i servizi accessibili da `localhost` per debug e sviluppo.
- Nessun servizio e' accessibile da macchine esterne alla rete locale (binding su `0.0.0.0` per default, modificabile a `127.0.0.1` per maggiore sicurezza).

### Binding su localhost (configurazione sicura)

Per limitare l'accesso ai servizi esclusivamente dalla macchina locale:

```yaml
ports:
  - "127.0.0.1:5432:5432"  # Solo localhost
  # invece di
  - "5432:5432"             # Tutte le interfacce
```

---

## 5. Volumes

LocalMind utilizza **named volumes** di Docker per la persistenza dei dati:

```yaml
volumes:
  localmind-postgres-data:
  localmind-qdrant-data:
  localmind-ollama-data:
  localmind-n8n-data:
```

### Dettaglio dei volumi

| Volume | Mount point nel container | Contenuto |
|---|---|---|
| `localmind-postgres-data` | `/var/lib/postgresql/data` | Dati PostgreSQL (tabelle, indici, WAL) |
| `localmind-qdrant-data` | `/qdrant/storage` | Dati Qdrant (collezioni, segmenti, indici HNSW) |
| `localmind-ollama-data` | `/root/.ollama` | Modelli LLM scaricati |
| `localmind-n8n-data` | `/home/node/.n8n` | Workflow, credenziali, configurazioni n8n |

### Gestione dei volumi

```bash
# Elencare i volumi
docker volume ls | grep localmind

# Ispezionare un volume (es. postgres)
docker volume inspect localmind_localmind-postgres-data

# Calcolare lo spazio utilizzato
docker system df -v | grep localmind
```

### Ciclo di vita dei volumi

| Operazione | Effetto sui volumi |
|---|---|
| `docker-compose stop` | Container fermati, volumi **preservati** |
| `docker-compose down` | Container rimossi, volumi **preservati** |
| `docker-compose down -v` | Container rimossi, volumi **eliminati** |
| `docker volume prune` | Volumi orfani **eliminati** |

**Attenzione:** `docker-compose down -v` elimina **tutti i dati** (database, modelli, workflow). Utilizzare con estrema cautela e solo dopo aver eseguito un backup.

---

## 6. Comandi Utili

### Gestione del ciclo di vita

```bash
# Avviare tutti i servizi in background
docker-compose up -d

# Avviare un singolo servizio
docker-compose up -d postgres

# Fermare tutti i servizi (preserva volumi)
docker-compose stop

# Fermare e rimuovere container (preserva volumi)
docker-compose down

# Fermare, rimuovere container E volumi (DISTRUTTIVO)
docker-compose down -v

# Riavviare un singolo servizio
docker-compose restart ollama

# Ricreare un servizio (es. dopo modifica docker-compose.yml)
docker-compose up -d --force-recreate ollama
```

### Monitoraggio

```bash
# Stato di tutti i servizi
docker-compose ps

# Log di tutti i servizi (live)
docker-compose logs -f

# Log di un singolo servizio (ultime 100 righe, live)
docker-compose logs -f --tail=100 postgres

# Statistiche risorse (CPU, RAM, I/O)
docker stats localmind-postgres localmind-qdrant localmind-ollama localmind-n8n
```

### Accesso ai container

```bash
# Shell interattiva in un container
docker exec -it localmind-postgres bash
docker exec -it localmind-ollama bash
docker exec -it localmind-n8n sh

# Eseguire un comando specifico
docker exec localmind-postgres pg_isready -U localmind
docker exec localmind-ollama ollama list
```

### Manutenzione

```bash
# Aggiornare le immagini all'ultima versione
docker-compose pull

# Aggiornare e ricreare i container
docker-compose pull && docker-compose up -d

# Rimuovere immagini non utilizzate
docker image prune

# Pulizia completa Docker (immagini, container, reti non utilizzate)
docker system prune
```

---

## 7. Configurazione GPU per Ollama

Per abilitare il supporto GPU NVIDIA in Ollama, e' necessario:

### Prerequisiti

1. **GPU NVIDIA** con supporto CUDA.
2. **NVIDIA Driver** versione 525 o superiore.
3. **NVIDIA Container Toolkit** installato.

### Installazione NVIDIA Container Toolkit (Ubuntu/Debian)

```bash
# Aggiungere il repository NVIDIA
distribution=$(. /etc/os-release;echo $ID$VERSION_ID) \
  && curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey | sudo gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg \
  && curl -s -L https://nvidia.github.io/libnvidia-container/$distribution/libnvidia-container.list | \
    sed 's#deb https://#deb [signed-by=/usr/share/keyrings/nvidia-container-toolkit-keyring.gpg] https://#g' | \
    sudo tee /etc/apt/sources.list.d/nvidia-container-toolkit.list

# Installare il toolkit
sudo apt-get update && sudo apt-get install -y nvidia-container-toolkit

# Configurare Docker per utilizzare il runtime NVIDIA
sudo nvidia-ctk runtime configure --runtime=docker
sudo systemctl restart docker
```

### Configurazione docker-compose.yml

Decommentare la sezione `deploy` nel servizio `ollama`:

```yaml
ollama:
  image: ollama/ollama:latest
  container_name: localmind-ollama
  ports:
    - "11434:11434"
  volumes:
    - localmind-ollama-data:/root/.ollama
  restart: unless-stopped
  deploy:
    resources:
      reservations:
        devices:
          - driver: nvidia
            count: all
            capabilities: [gpu]
```

### Verifica del supporto GPU

```bash
# Verificare che il container veda la GPU
docker exec localmind-ollama nvidia-smi

# Verificare che Ollama utilizzi la GPU
docker exec localmind-ollama ollama run llama3.2 "Ciao, che GPU stai usando?"
# Controllare nei log: "using CUDA"
docker-compose logs ollama | grep -i cuda
```

---

## 8. Personalizzazione

### Modificare le porte

Per evitare conflitti con altri servizi sulla macchina, e' possibile modificare le porte esposte:

```yaml
# Esempio: spostare PostgreSQL sulla porta 15432
postgres:
  ports:
    - "15432:5432"
```

**Nota:** Dopo aver modificato le porte, aggiornare anche il file `application.yml` di Spring Boot per riflettere le nuove porte.

### Aggiungere limiti di risorse

```yaml
postgres:
  deploy:
    resources:
      limits:
        cpus: "2.0"
        memory: 2G
      reservations:
        cpus: "0.5"
        memory: 512M
```

### Aggiungere un servizio personalizzato

Per aggiungere un nuovo servizio all'infrastruttura (es. Redis per caching):

```yaml
redis:
  image: redis:7-alpine
  container_name: localmind-redis
  ports:
    - "6379:6379"
  volumes:
    - localmind-redis-data:/data
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 10s
    timeout: 5s
    retries: 5
  restart: unless-stopped

# Aggiungere il volume alla sezione volumes:
volumes:
  localmind-redis-data:
```
