# Configurazione Docker Compose (Opzionale)

|               |                                              |
|---------------|----------------------------------------------|
| **Documento** | Documentazione Configurazione Docker Compose |
| **Versione**  | 0.1.0                                        |
| **Data**      | 2026-02-09                                   |
| **Progetto**  | LocalMind                                    |

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Struttura del File](#2-struttura-del-file)
3. [Servizi Dettagliati](#3-servizi-dettagliati)
   - 3.1 [Qdrant](#31-qdrant)
   - 3.2 [Ollama](#32-ollama)
   - 3.3 [n8n](#33-n8n)
4. [Network](#4-network)
5. [Volumes](#5-volumes)
6. [Comandi Utili](#6-comandi-utili)
7. [Configurazione GPU per Ollama](#7-configurazione-gpu-per-ollama)
8. [Personalizzazione](#8-personalizzazione)

---

## 1. Panoramica

Docker Compose e' utilizzato in LocalMind in modo **opzionale** per i soli servizi infrastrutturali. Il backend (Spring Boot) e il frontend (Angular) vengono eseguiti nativamente tramite gli script nella directory `scripts/`. Il database MySQL viene installato e gestito nativamente (o in Docker, auto-rilevato dallo script `setup-mysql.sh`).

Il file `docker-compose.yml` si trova nella **directory root del progetto** e gestisce tre servizi infrastrutturali opzionali:

| Servizio   | Funzione                                       | Immagine               |
|------------|------------------------------------------------|------------------------|
| **qdrant** | Vector store per embedding e ricerca semantica | `qdrant/qdrant:latest` |
| **ollama** | LLM inference locale                           | `ollama/ollama:latest` |
| **n8n**    | Workflow automation                            | `n8nio/n8n:latest`     |

**Nota:** Tutti questi servizi possono essere eseguiti anche nativamente senza Docker. Docker Compose e' fornito come opzione di convenienza per chi preferisce l'approccio containerizzato.

Tutti i servizi Docker sono configurati con:

- **Volumes persistenti**: i dati sopravvivono al riavvio dei container.
- **Health checks**: Docker verifica periodicamente lo stato di salute.
- **Restart policy**: i container vengono riavviati automaticamente in caso di crash.
- **Port mapping**: ogni servizio e' accessibile da `localhost`.

---

## 2. Struttura del File

```yaml
# docker-compose.yml
# LocalMind - Infrastructure Services (opzionale)
# Compose V2 format (no version field)
# Nota: backend, frontend e MySQL vengono eseguiti nativamente

services:
  qdrant:
    # ... configurazione Qdrant
  ollama:
    # ... configurazione Ollama
  n8n:
    # ... configurazione n8n

volumes:
  localmind-qdrant-data:
  localmind-ollama-data:
  localmind-n8n-data:
```

---

## 3. Servizi Dettagliati

### 3.1 Qdrant

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

| Parametro        | Valore                                  | Descrizione                                                         |
|------------------|-----------------------------------------|---------------------------------------------------------------------|
| `image`          | `qdrant/qdrant:latest`                  | Ultima versione stabile di Qdrant                                   |
| `container_name` | `localmind-qdrant`                      | Nome fisso del container                                            |
| `ports` (REST)   | `6333:6333`                             | API REST per operazioni CRUD e ricerca                              |
| `ports` (gRPC)   | `6334:6334`                             | API gRPC per comunicazione ad alte prestazioni (usata da Spring AI) |
| `volumes`        | `localmind-qdrant-data:/qdrant/storage` | Volume per persistenza collezioni e indici                          |
| `restart`        | `unless-stopped`                        | Riavvio automatico                                                  |

#### Porte esposte

| Porta  | Protocollo | Utilizzo                                                                                                      |
|--------|------------|---------------------------------------------------------------------------------------------------------------|
| `6333` | HTTP/REST  | API REST per gestione collezioni, upload punti, ricerca. Interfaccia web su `http://localhost:6333/dashboard` |
| `6334` | gRPC       | Comunicazione ad alte prestazioni. Utilizzata da Spring AI per operazioni bulk e ricerca                      |

#### Connessione dall'applicazione Spring Boot

| Ambiente   | Host                        | Porta         |
|------------|-----------------------------|---------------|
| Sviluppo   | `localhost`                 | `6334` (gRPC) |
| Produzione | `localhost` (configurabile) | `6334` (gRPC) |

#### Risorse di storage

Qdrant memorizza i dati nel volume `localmind-qdrant-data`:

- **Collezioni**: strutture che raggruppano i vettori per dominio.
- **Segmenti**: suddivisioni interne per ottimizzazione delle ricerche.
- **Indici HNSW**: strutture dati per ricerca approssimata del nearest neighbor.

---

### 3.2 Ollama

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

| Parametro        | Valore                                | Descrizione                                                |
|------------------|---------------------------------------|------------------------------------------------------------|
| `image`          | `ollama/ollama:latest`                | Ultima versione stabile di Ollama                          |
| `container_name` | `localmind-ollama`                    | Nome fisso del container                                   |
| `ports`          | `11434:11434`                         | API REST per inference (compatibile con OpenAI API format) |
| `volumes`        | `localmind-ollama-data:/root/.ollama` | Volume per persistenza modelli scaricati                   |
| `restart`        | `unless-stopped`                      | Riavvio automatico                                         |

#### Modelli da scaricare manualmente

Dopo il primo avvio del container (o del servizio nativo), e' necessario scaricare i modelli manualmente:

```bash
# Se Ollama e' in Docker:
docker exec -it localmind-ollama ollama pull llama3.2
docker exec -it localmind-ollama ollama pull nomic-embed-text
docker exec localmind-ollama ollama list

# Se Ollama e' installato nativamente:
ollama pull llama3.2
ollama pull nomic-embed-text
ollama list
```

**Nota importante:** I modelli vengono salvati nel volume `localmind-ollama-data` (Docker) o in `~/.ollama/models` (nativo) e persistono tra i riavvii. Non e' necessario riscaricarli dopo un riavvio del servizio.

#### API Ollama

L'API di Ollama e' accessibile su `http://localhost:11434` e supporta i seguenti endpoint principali:

| Endpoint          | Metodo | Descrizione                    |
|-------------------|--------|--------------------------------|
| `/api/tags`       | GET    | Lista dei modelli installati   |
| `/api/generate`   | POST   | Generazione testo (completion) |
| `/api/chat`       | POST   | Chat con cronologia messaggi   |
| `/api/embeddings` | POST   | Generazione embedding          |
| `/api/pull`       | POST   | Download di un modello         |
| `/api/show`       | POST   | Dettagli di un modello         |

#### Risorse hardware

| Risorsa   | Senza GPU                        | Con GPU NVIDIA                      |
|-----------|----------------------------------|-------------------------------------|
| CPU       | Utilizzata per inference (lento) | Utilizzata per preprocessing        |
| RAM       | 3-8 GB (dipende dal modello)     | 2-4 GB (il modello risiede in VRAM) |
| VRAM      | N/A                              | 3-8 GB (dipende dal modello)        |
| Velocita' | ~5-15 token/s                    | ~30-100 token/s                     |

---

### 3.3 n8n

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
  restart: unless-stopped
```

#### Dettaglio configurazione

| Parametro                 | Valore                                | Descrizione                                   |
|---------------------------|---------------------------------------|-----------------------------------------------|
| `image`                   | `n8nio/n8n:latest`                    | Ultima versione stabile di n8n                |
| `container_name`          | `localmind-n8n`                       | Nome fisso del container                      |
| `ports`                   | `5678:5678`                           | Interfaccia web e API webhook                 |
| `N8N_BASIC_AUTH_ACTIVE`   | `true`                                | Abilita l'autenticazione Basic Auth           |
| `N8N_BASIC_AUTH_USER`     | `${N8N_BASIC_AUTH_USER:-admin}`       | Username da `.env`, default `admin`           |
| `N8N_BASIC_AUTH_PASSWORD` | `${N8N_BASIC_AUTH_PASSWORD:-admin}`   | Password da `.env`, default `admin`           |
| `N8N_HOST`                | `localhost`                           | Hostname per generazione URL webhook          |
| `WEBHOOK_URL`             | `http://localhost:5678/`              | URL base per i webhook                        |
| `volumes`                 | `localmind-n8n-data:/home/node/.n8n`  | Volume per persistenza workflow e credenziali |
| `restart`                 | `unless-stopped`                      | Riavvio automatico                            |

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

I servizi infrastrutturali in Docker comunicano tra loro tramite la rete interna. Il backend Spring Boot (che gira nativamente) si connette ai servizi tramite `localhost` e le porte mappate:

| Da                   | A                        | Hostname    | Porta         |
|----------------------|--------------------------|-------------|---------------|
| Spring Boot (nativo) | MySQL (nativo)           | `localhost` | `3306`        |
| Spring Boot (nativo) | Qdrant (Docker)          | `localhost` | `6334` (gRPC) |
| Spring Boot (nativo) | Ollama (Docker o nativo) | `localhost` | `11434`       |
| Spring Boot (nativo) | n8n (Docker o nativo)    | `localhost` | `5678`        |

### Binding su localhost (configurazione sicura)

Per limitare l'accesso ai servizi esclusivamente dalla macchina locale:

```yaml
ports:
  - "127.0.0.1:6333:6333"  # Solo localhost
  # invece di
  - "6333:6333"             # Tutte le interfacce
```

---

## 5. Volumes

LocalMind utilizza **named volumes** di Docker per la persistenza dei dati dei servizi infrastrutturali:

```yaml
volumes:
  localmind-qdrant-data:
  localmind-ollama-data:
  localmind-n8n-data:
```

### Dettaglio dei volumi

| Volume                  | Mount point nel container | Contenuto                                       |
|-------------------------|---------------------------|-------------------------------------------------|
| `localmind-qdrant-data` | `/qdrant/storage`         | Dati Qdrant (collezioni, segmenti, indici HNSW) |
| `localmind-ollama-data` | `/root/.ollama`           | Modelli LLM scaricati                           |
| `localmind-n8n-data`    | `/home/node/.n8n`         | Workflow, credenziali, configurazioni n8n       |

**Nota:** Il database MySQL viene gestito nativamente e i suoi dati risiedono nella directory standard del sistema (`/var/lib/mysql` o equivalente).

### Gestione dei volumi

```bash
# Elencare i volumi
docker volume ls | grep localmind

# Ispezionare un volume (es. qdrant)
docker volume inspect localmind_localmind-qdrant-data

# Calcolare lo spazio utilizzato
docker system df -v | grep localmind
```

### Ciclo di vita dei volumi

| Operazione               | Effetto sui volumi                         |
|--------------------------|--------------------------------------------|
| `docker compose stop`    | Container fermati, volumi **preservati**   |
| `docker compose down`    | Container rimossi, volumi **preservati**   |
| `docker compose down -v` | Container rimossi, volumi **eliminati**    |
| `docker volume prune`    | Volumi orfani **eliminati**                |

**Attenzione:** `docker compose down -v` elimina **tutti i dati** dei servizi Docker (modelli Ollama, dati Qdrant, workflow n8n). Utilizzare con estrema cautela e solo dopo aver eseguito un backup. Il database MySQL non e' influenzato da questo comando in quanto gestito nativamente.

---

## 6. Comandi Utili

### Gestione del ciclo di vita

```bash
# Avviare tutti i servizi infrastrutturali in background
docker compose up -d

# Avviare un singolo servizio
docker compose up -d qdrant

# Fermare tutti i servizi (preserva volumi)
docker compose stop

# Fermare e rimuovere container (preserva volumi)
docker compose down

# Fermare, rimuovere container E volumi (DISTRUTTIVO)
docker compose down -v

# Riavviare un singolo servizio
docker compose restart ollama

# Ricreare un servizio (es. dopo modifica docker-compose.yml)
docker compose up -d --force-recreate ollama
```

### Monitoraggio

```bash
# Stato di tutti i servizi
docker compose ps

# Log di tutti i servizi (live)
docker compose logs -f

# Log di un singolo servizio (ultime 100 righe, live)
docker compose logs -f --tail=100 qdrant

# Statistiche risorse (CPU, RAM, I/O)
docker stats localmind-qdrant localmind-ollama localmind-n8n
```

### Accesso ai container

```bash
# Shell interattiva in un container
docker exec -it localmind-ollama bash
docker exec -it localmind-n8n sh

# Eseguire un comando specifico
docker exec localmind-ollama ollama list
```

### Manutenzione

```bash
# Aggiornare le immagini all'ultima versione
docker compose pull

# Aggiornare e ricreare i container
docker compose pull && docker compose up -d

# Rimuovere immagini non utilizzate
docker image prune

# Pulizia completa Docker (immagini, container, reti non utilizzate)
docker system prune
```

---

## 7. Configurazione GPU per Ollama

Per abilitare il supporto GPU NVIDIA in Ollama (sia Docker che nativo), e' necessario:

### Prerequisiti

1. **GPU NVIDIA** con supporto CUDA.
2. **NVIDIA Driver** versione 525 o superiore.
3. **NVIDIA Container Toolkit** installato (solo per Docker).

### Installazione NVIDIA Container Toolkit (Ubuntu/Debian) - Solo per Docker

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

**Nota:** Se Ollama e' installato nativamente, rileva automaticamente la GPU NVIDIA senza bisogno del Container Toolkit.

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
# Se in Docker:
docker exec localmind-ollama nvidia-smi
docker compose logs ollama | grep -i cuda

# Se nativo:
nvidia-smi
ollama run llama3.2 "Ciao, che GPU stai usando?"
```

---

## 8. Personalizzazione

### Modificare le porte

Per evitare conflitti con altri servizi sulla macchina, e' possibile modificare le porte esposte:

```yaml
# Esempio: spostare Qdrant REST sulla porta 16333
qdrant:
  ports:
    - "16333:6333"
    - "16334:6334"
```

**Nota:** Dopo aver modificato le porte, aggiornare anche il file `application.yml` di Spring Boot per riflettere le nuove porte.

### Aggiungere limiti di risorse

```yaml
qdrant:
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
