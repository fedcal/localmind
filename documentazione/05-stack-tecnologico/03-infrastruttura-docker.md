# Infrastruttura Docker

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Servizio PostgreSQL 16](#2-servizio-postgresql-16)
3. [Servizio Qdrant](#3-servizio-qdrant)
4. [Servizio Ollama](#4-servizio-ollama)
5. [Servizio n8n](#5-servizio-n8n)
6. [Network](#6-network)
7. [Volumi Persistenti](#7-volumi-persistenti)
8. [Comandi Operativi](#8-comandi-operativi)
9. [Docker Compose Completo](#9-docker-compose-completo)

---

## 1. Panoramica

L'infrastruttura di sviluppo locale di LocalMind e' orchestrata tramite Docker Compose. Il file `docker-compose.yml` definisce quattro servizi principali, una rete bridge condivisa e quattro volumi persistenti per la conservazione dei dati tra i riavvii dei container.

### Architettura dei servizi

```
+------------------+     +------------------+     +------------------+
|   PostgreSQL 16  |     |      Qdrant      |     |      Ollama      |
|   :5432          |     |  :6333 (REST)    |     |   :11434         |
|                  |     |  :6334 (gRPC)    |     |                  |
+--------+---------+     +--------+---------+     +--------+---------+
         |                        |                        |
         +------------------------+------------------------+
                                  |
                       localmind-network (bridge)
                                  |
         +------------------------+------------------------+
         |                                                 |
+--------+---------+                            +----------+---------+
|       n8n        |                            |  Spring Boot App   |
|   :5678          |                            |  :8080 (host)      |
+------------------+                            +--------------------+
```

---

## 2. Servizio PostgreSQL 16

| Proprieta'       | Valore                           |
|------------------|----------------------------------|
| **Image**        | `postgres:16-alpine`             |
| **Container name** | `localmind-postgres`           |
| **Porta esposta** | `5432:5432`                     |
| **Volume**       | `localmind-postgres-data:/var/lib/postgresql/data` |

### Variabili d'ambiente

| Variabile            | Valore       | Descrizione                    |
|----------------------|-------------|--------------------------------|
| `POSTGRES_DB`        | `localmind` | Nome del database              |
| `POSTGRES_USER`      | `localmind` | Utente database                |
| `POSTGRES_PASSWORD`  | `localmind` | Password database              |

### Healthcheck

```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U localmind -d localmind"]
  interval: 10s
  timeout: 5s
  retries: 5
```

Il healthcheck verifica la disponibilita' del database tramite il comando `pg_isready`, con intervallo di 10 secondi tra i tentativi e un massimo di 5 retry.

### Note

- L'immagine `alpine` e' utilizzata per ridurre la dimensione del container.
- Il volume `localmind-postgres-data` garantisce la persistenza dei dati tra i riavvii.
- La porta 5432 e' esposta sull'host per consentire la connessione diretta dal backend Spring Boot in esecuzione locale.

---

## 3. Servizio Qdrant

| Proprieta'       | Valore                           |
|------------------|----------------------------------|
| **Image**        | `qdrant/qdrant:latest`           |
| **Container name** | `localmind-qdrant`             |
| **Porte esposte** | `6333:6333` (REST), `6334:6334` (gRPC) |
| **Volume**       | `localmind-qdrant-data:/qdrant/storage` |

### Porte

| Porta  | Protocollo | Descrizione                              |
|--------|-----------|------------------------------------------|
| `6333` | REST/HTTP | API REST per operazioni di gestione e debug; dashboard web disponibile su `http://localhost:6333/dashboard` |
| `6334` | gRPC      | Protocollo ad alte prestazioni utilizzato da Spring AI per operazioni su vettori |

### Healthcheck

```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:6333/healthz || exit 1"]
  interval: 10s
  timeout: 5s
  retries: 5
```

### Note

- La porta gRPC 6334 e' quella configurata in Spring AI (`spring.ai.vectorstore.qdrant.port=6334`).
- La collection `localmind-documents` viene creata automaticamente da Spring AI al primo inserimento.
- La REST API sulla porta 6333 e' utile per ispezionare le collection e i punti vettoriali durante lo sviluppo.

---

## 4. Servizio Ollama

| Proprieta'       | Valore                           |
|------------------|----------------------------------|
| **Image**        | `ollama/ollama:latest`           |
| **Container name** | `localmind-ollama`             |
| **Porta esposta** | `11434:11434`                   |
| **Volume**       | `localmind-ollama-data:/root/.ollama` |

### Modelli da scaricare

Dopo l'avvio del container, e' necessario scaricare i modelli LLM richiesti:

| Modello            | Scopo                        | Comando                              |
|--------------------|------------------------------|--------------------------------------|
| `llama3.2`         | Chat e generazione testo     | `docker exec localmind-ollama ollama pull llama3.2` |
| `nomic-embed-text` | Generazione embedding        | `docker exec localmind-ollama ollama pull nomic-embed-text` |

### Supporto GPU (opzionale)

Per abilitare l'accelerazione GPU NVIDIA, aggiungere la sezione `deploy` al servizio:

```yaml
deploy:
  resources:
    reservations:
      devices:
        - driver: nvidia
          count: 1
          capabilities: [gpu]
```

### Prerequisiti per GPU

- Driver NVIDIA installati sull'host.
- NVIDIA Container Toolkit installato (`nvidia-docker2`).
- Docker configurato per utilizzare il runtime NVIDIA.

### Note

- Senza GPU, i modelli funzionano in modalita' CPU-only con prestazioni ridotte.
- Il volume `localmind-ollama-data` conserva i modelli scaricati tra i riavvii.
- La porta 11434 corrisponde alla configurazione `spring.ai.ollama.base-url=http://localhost:11434`.

---

## 5. Servizio n8n

| Proprieta'       | Valore                           |
|------------------|----------------------------------|
| **Image**        | `n8nio/n8n:latest`               |
| **Container name** | `localmind-n8n`                |
| **Porta esposta** | `5678:5678`                     |
| **Volume**       | `localmind-n8n-data:/home/node/.n8n` |

### Variabili d'ambiente

| Variabile                | Valore                      | Descrizione                    |
|--------------------------|-----------------------------|--------------------------------|
| `N8N_BASIC_AUTH_ACTIVE`  | `true`                      | Abilita autenticazione basic   |
| `N8N_BASIC_AUTH_USER`    | `admin`                     | Username per accesso web UI    |
| `N8N_BASIC_AUTH_PASSWORD` | `localmind`                | Password per accesso web UI    |
| `WEBHOOK_URL`            | `http://localhost:5678/`    | URL base per webhook           |

### Dipendenze

```yaml
depends_on:
  postgres:
    condition: service_healthy
```

Il servizio n8n dipende dalla disponibilita' di PostgreSQL (healthcheck superato) prima di avviarsi.

### Note

- n8n e' utilizzato per automazioni e workflow di integrazione (es. webhook per notifiche post-indicizzazione).
- L'interfaccia web e' accessibile su `http://localhost:5678`.
- La configurazione `localmind.n8n.base-url=http://localhost:5678` nel backend punta a questa istanza.

---

## 6. Network

| Proprieta'   | Valore                           |
|-------------|----------------------------------|
| **Nome**    | `localmind-network`              |
| **Driver**  | `bridge`                         |

### Configurazione

```yaml
networks:
  localmind-network:
    driver: bridge
```

Tutti i servizi sono connessi alla rete `localmind-network`, consentendo la comunicazione inter-container tramite il nome del servizio come hostname (es. `postgres`, `qdrant`, `ollama`).

---

## 7. Volumi Persistenti

| Volume                     | Mount Point nel container            | Descrizione                          |
|----------------------------|--------------------------------------|--------------------------------------|
| `localmind-postgres-data`  | `/var/lib/postgresql/data`           | Dati del database PostgreSQL         |
| `localmind-qdrant-data`    | `/qdrant/storage`                    | Collection e indici vettoriali       |
| `localmind-ollama-data`    | `/root/.ollama`                      | Modelli LLM scaricati                |
| `localmind-n8n-data`       | `/home/node/.n8n`                    | Workflow e configurazione n8n        |

### Configurazione

```yaml
volumes:
  localmind-postgres-data:
  localmind-qdrant-data:
  localmind-ollama-data:
  localmind-n8n-data:
```

Tutti i volumi sono di tipo `named volume`, gestiti da Docker. I dati persistono tra i riavvii dei container e vengono eliminati solo con `docker compose down -v`.

---

## 8. Comandi Operativi

### Avvio dell'infrastruttura

```bash
# Avvia tutti i servizi in background
docker compose up -d

# Verifica lo stato dei servizi
docker compose ps

# Visualizza i log in tempo reale
docker compose logs -f
```

### Download modelli Ollama

```bash
# Scarica il modello di chat
docker exec localmind-ollama ollama pull llama3.2

# Scarica il modello di embedding
docker exec localmind-ollama ollama pull nomic-embed-text

# Verifica i modelli disponibili
docker exec localmind-ollama ollama list
```

### Arresto dell'infrastruttura

```bash
# Arresta tutti i servizi (mantiene i volumi)
docker compose down

# Arresta tutti i servizi e rimuove i volumi (ATTENZIONE: cancella i dati)
docker compose down -v
```

### Operazioni di manutenzione

```bash
# Riavvia un singolo servizio
docker compose restart postgres

# Visualizza i log di un singolo servizio
docker compose logs -f qdrant

# Accedi al container PostgreSQL
docker exec -it localmind-postgres psql -U localmind -d localmind
```

---

## 9. Docker Compose Completo

Di seguito la configurazione Docker Compose completa di riferimento:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: localmind-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: localmind
      POSTGRES_USER: localmind
      POSTGRES_PASSWORD: localmind
    volumes:
      - localmind-postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U localmind -d localmind"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - localmind-network

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
    networks:
      - localmind-network

  ollama:
    image: ollama/ollama:latest
    container_name: localmind-ollama
    ports:
      - "11434:11434"
    volumes:
      - localmind-ollama-data:/root/.ollama
    # Decommentare per supporto GPU NVIDIA:
    # deploy:
    #   resources:
    #     reservations:
    #       devices:
    #         - driver: nvidia
    #           count: 1
    #           capabilities: [gpu]
    networks:
      - localmind-network

  n8n:
    image: n8nio/n8n:latest
    container_name: localmind-n8n
    ports:
      - "5678:5678"
    environment:
      N8N_BASIC_AUTH_ACTIVE: "true"
      N8N_BASIC_AUTH_USER: admin
      N8N_BASIC_AUTH_PASSWORD: localmind
      WEBHOOK_URL: http://localhost:5678/
    volumes:
      - localmind-n8n-data:/home/node/.n8n
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - localmind-network

networks:
  localmind-network:
    driver: bridge

volumes:
  localmind-postgres-data:
  localmind-qdrant-data:
  localmind-ollama-data:
  localmind-n8n-data:
```
