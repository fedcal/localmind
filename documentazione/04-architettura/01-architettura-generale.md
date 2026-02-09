# Architettura Generale

| Campo        | Valore                          |
|--------------|---------------------------------|
| **Documento**| Architettura Generale           |
| **Versione** | 0.1.0                          |
| **Data**     | 2026-02-09                      |
| **Progetto** | LocalMind                       |

---

## Indice

1. [Panoramica Architetturale](#1-panoramica-architetturale)
2. [Architettura a 3 Tier](#2-architettura-a-3-tier)
3. [Diagramma C4 Context](#3-diagramma-c4-context)
4. [Diagramma C4 Container](#4-diagramma-c4-container)
5. [Protocolli di Comunicazione](#5-protocolli-di-comunicazione)
6. [Stack Docker](#6-stack-docker)

---

## 1. Panoramica Architetturale

LocalMind adotta un'architettura a tre livelli (3-tier) con una netta separazione tra presentazione, logica applicativa e persistenza dei dati. L'intera piattaforma e' containerizzata tramite Docker Compose per garantire riproducibilita', isolamento e semplicita' di deployment.

L'architettura e' progettata per:

- **Modularita'**: ogni componente e' indipendente e sostituibile
- **Scalabilita'**: i servizi possono essere scalati indipendentemente
- **Testabilita'**: la separazione dei livelli consente test unitari, di integrazione e end-to-end
- **Deployment semplificato**: un singolo `docker-compose up` avvia l'intero sistema

---

## 2. Architettura a 3 Tier

```
+----------------------------------------------------------------------+
|                        PRESENTATION TIER                              |
|                                                                       |
|  +----------------------------------------------------------------+  |
|  |                    LocalMind UI (Angular 21)                    |  |
|  |                                                                 |  |
|  |  - Standalone components                                       |  |
|  |  - Signal-based state management                               |  |
|  |  - REST client (HttpClient)                                    |  |
|  |  - Porta: 4200 (dev) / 80 (prod via nginx)                    |  |
|  +----------------------------------------------------------------+  |
|                              |                                        |
|                         HTTP REST (JSON)                              |
|                              |                                        |
+----------------------------------------------------------------------+
|                        APPLICATION TIER                                |
|                                                                       |
|  +----------------------------------------------------------------+  |
|  |                 LocalMind API (Spring Boot 3.4)                 |  |
|  |                                                                 |  |
|  |  +------------------+  +------------------+  +---------------+  |  |
|  |  | localmind-api    |  | localmind-domain |  | localmind-    |  |  |
|  |  | (controllers,    |  | (entities,       |  | infrastructure|  |  |
|  |  |  DTOs, mapping)  |  |  services, ports)|  | (adapters,    |  |  |
|  |  +------------------+  +------------------+  |  JPA, AI)     |  |  |
|  |                                               +---------------+  |  |
|  |  +------------------+  +------------------+                      |  |
|  |  | localmind-batch  |  | localmind-app    |                      |  |
|  |  | (Spring Batch    |  | (bootstrap,      |                      |  |
|  |  |  jobs, config)   |  |  configuration)  |                      |  |
|  |  +------------------+  +------------------+                      |  |
|  |                                                                 |  |
|  |  Porta: 8080                                                    |  |
|  +----------------------------------------------------------------+  |
|                              |                                        |
+----------------------------------------------------------------------+
|                          DATA TIER                                     |
|                                                                       |
|  +-----------------------+  +------------------------------------+   |
|  |    PostgreSQL 16      |  |           Qdrant                   |   |
|  |                       |  |                                     |   |
|  |  - Dati relazionali   |  |  - Vector store                    |   |
|  |  - Metadati documenti |  |  - Embedding documenti             |   |
|  |  - Config utente      |  |  - Ricerca semantica               |   |
|  |  - Usage/cost data    |  |                                     |   |
|  |  - Batch job metadata |  |  Porte: 6333 (HTTP), 6334 (gRPC)  |   |
|  |                       |  |                                     |   |
|  |  Porta: 5432          |  |                                     |   |
|  +-----------------------+  +------------------------------------+   |
|                                                                       |
+----------------------------------------------------------------------+
```

---

## 3. Diagramma C4 Context

Il diagramma C4 Context mostra LocalMind nel contesto del suo ambiente esterno, evidenziando le interazioni con utenti e sistemi esterni.

```
                                +-------------------+
                                |                   |
                                |      Utente       |
                                |                   |
                                +--------+----------+
                                         |
                                    Browser
                                         |
                                         v
                        +----------------+------------------+
                        |                                    |
                        |       LocalMind UI (Angular)       |
                        |                                    |
                        +----------------+------------------+
                                         |
                                    REST API
                                         |
                                         v
                        +----------------+------------------+
                        |                                    |
                        |     LocalMind API (Spring Boot)    |
                        |                                    |
                        +--+------+------+------+------+---+
                           |      |      |      |      |
              +------------+  +---+--+ +-+----+ | +----+--------+
              |               |      | |      | | |             |
              v               v      v v      v v v             v
    +---------+---+  +--------+-+ +--+----+ +-+-+-----+ +------+------+
    |             |  |          | |       | |         | |             |
    |   Ollama    |  |  OpenAI  | |Anthro.| | Google  | | File System |
    |  (locale)   |  |  (cloud) | |(cloud)| | (cloud) | |  (locale)   |
    |             |  |          | |       | |         | |             |
    +-------------+  +----------+ +-------+ +---------+ +-------------+

              +---------------+  +-----------------+
              |               |  |                 |
              |  PostgreSQL   |  |     Qdrant      |
              |  (locale)     |  |    (locale)     |
              |               |  |                 |
              +---------------+  +-----------------+

                        +-------------------+
                        |                   |
                        |       n8n         |
                        |   (self-hosted)   |
                        |                   |
                        +-------------------+
```

### 3.1 Descrizione delle Interazioni

| Sorgente          | Destinazione       | Protocollo    | Descrizione                          |
|------------------|-------------------|---------------|---------------------------------------|
| Utente           | LocalMind UI       | HTTPS         | Interfaccia web via browser           |
| LocalMind UI     | LocalMind API      | HTTP REST     | Chiamate API JSON                     |
| LocalMind API    | Ollama             | HTTP REST     | Richieste LLM locali (porta 11434)   |
| LocalMind API    | OpenAI             | HTTPS REST    | Richieste LLM cloud                  |
| LocalMind API    | Anthropic          | HTTPS REST    | Richieste LLM cloud                  |
| LocalMind API    | Google             | HTTPS REST    | Richieste LLM cloud                  |
| LocalMind API    | PostgreSQL         | JDBC          | Persistenza dati relazionali (5432)   |
| LocalMind API    | Qdrant             | gRPC/HTTP     | Vector store (6334/6333)             |
| LocalMind API    | n8n                | HTTP POST     | Webhook per automazioni (5678)       |
| LocalMind API    | File System        | Java NIO      | Lettura documenti locali             |
| n8n              | LocalMind API      | HTTP REST     | Callback e invocazione API           |
| n8n              | Servizi Esterni    | HTTPS         | Email, Slack, Drive, etc.            |

---

## 4. Diagramma C4 Container

Il diagramma C4 Container mostra i container (processi) che compongono LocalMind e le loro interazioni.

```
+------------------------------------------------------------------+
|                    Docker Compose Stack                            |
|                                                                   |
|  +-----------------------------+  +----------------------------+  |
|  |   localmind-frontend        |  |   localmind-backend        |  |
|  |   (nginx + Angular)         |  |   (Spring Boot fat JAR)    |  |
|  |                             |  |                            |  |
|  |   Porta: 80                 |  |   Porta: 8080              |  |
|  |   Immagine: nginx:alpine    |  |   Immagine: eclipse-       |  |
|  |   Volume: /usr/share/       |  |   temurin:17-jre-alpine    |  |
|  |   nginx/html                |  |                            |  |
|  +-----------------------------+  +---+------+------+------+---+  |
|                                       |      |      |      |     |
|                          +------------+  +---+--+ +-+----+ |     |
|                          |               |      | |      | |     |
|                          v               v      v v      v v     |
|  +-----------------------------+  +----------------------------+  |
|  |   localmind-db              |  |   localmind-qdrant         |  |
|  |   (PostgreSQL 16)           |  |   (Qdrant)                 |  |
|  |                             |  |                            |  |
|  |   Porta: 5432              |  |   Porta: 6333 (HTTP)       |  |
|  |   Volume: pg_data          |  |         6334 (gRPC)       |  |
|  |   Immagine: postgres:16    |  |   Volume: qdrant_data     |  |
|  +-----------------------------+  +----------------------------+  |
|                                                                   |
|  +-----------------------------+  +----------------------------+  |
|  |   localmind-ollama          |  |   localmind-n8n            |  |
|  |   (Ollama)                  |  |   (n8n)                    |  |
|  |                             |  |                            |  |
|  |   Porta: 11434             |  |   Porta: 5678              |  |
|  |   Volume: ollama_data      |  |   Volume: n8n_data         |  |
|  |   GPU: nvidia (opzionale)  |  |   Auth: Basic              |  |
|  +-----------------------------+  +----------------------------+  |
|                                                                   |
+------------------------------------------------------------------+
```

### 4.1 Elenco Container

| Container           | Immagine                    | Porta  | Volume            | Funzione                 |
|--------------------|-----------------------------|---------|--------------------|--------------------------|
| localmind-frontend | nginx:alpine                | 80      | static files       | Serving Angular build    |
| localmind-backend  | eclipse-temurin:17-jre-alpine| 8080  | -                  | API Spring Boot          |
| localmind-db       | postgres:16                 | 5432    | pg_data            | Database relazionale     |
| localmind-qdrant   | qdrant/qdrant               | 6333/6334 | qdrant_data     | Vector store             |
| localmind-ollama   | ollama/ollama               | 11434   | ollama_data        | LLM locale               |
| localmind-n8n      | n8nio/n8n                   | 5678    | n8n_data           | Automazioni              |

---

## 5. Protocolli di Comunicazione

### 5.1 REST API (JSON)

La comunicazione tra frontend e backend avviene tramite API REST con payload JSON:

- **Content-Type**: `application/json`
- **Encoding**: UTF-8
- **Autenticazione**: JWT (futuro) / Basic Auth (fase iniziale)
- **Versioning**: URL path (`/api/v1/...`)
- **Error handling**: RFC 7807 Problem Details

### 5.2 gRPC (Qdrant)

La comunicazione tra backend e Qdrant utilizza preferibilmente il protocollo gRPC per massime performance:

- **Porta**: 6334
- **Protocollo**: HTTP/2 + Protocol Buffers
- **Fallback**: HTTP REST (porta 6333) in caso di problemi gRPC

### 5.3 Comunicazione Futura

| Protocollo | Caso d'Uso                    | Stato      |
|-----------|-------------------------------|------------|
| SSE       | Streaming risposte LLM        | Pianificato|
| WebSocket | Chat real-time bidirezionale  | Pianificato|
| gRPC      | Comunicazione inter-servizio  | Valutazione|

---

## 6. Stack Docker

### 6.1 Docker Compose di Riferimento

```yaml
version: '3.8'

services:
  frontend:
    image: nginx:alpine
    container_name: localmind-frontend
    ports:
      - "80:80"
    volumes:
      - ./frontend/dist:/usr/share/nginx/html
      - ./frontend/nginx.conf:/etc/nginx/conf.d/default.conf
    depends_on:
      - backend
    networks:
      - localmind-network

  backend:
    image: eclipse-temurin:17-jre-alpine
    container_name: localmind-backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/localmind
      - SPRING_DATASOURCE_USERNAME=localmind
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD:-localmind}
    volumes:
      - ./backend/target/localmind-app.jar:/app/app.jar
    command: java -jar /app/app.jar
    depends_on:
      - db
      - qdrant
      - ollama
    networks:
      - localmind-network

  db:
    image: postgres:16
    container_name: localmind-db
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_DB=localmind
      - POSTGRES_USER=localmind
      - POSTGRES_PASSWORD=${DB_PASSWORD:-localmind}
    volumes:
      - pg_data:/var/lib/postgresql/data
    networks:
      - localmind-network

  qdrant:
    image: qdrant/qdrant
    container_name: localmind-qdrant
    ports:
      - "6333:6333"
      - "6334:6334"
    volumes:
      - qdrant_data:/qdrant/storage
    networks:
      - localmind-network

  ollama:
    image: ollama/ollama
    container_name: localmind-ollama
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              count: all
              capabilities: [gpu]
    networks:
      - localmind-network

  n8n:
    image: n8nio/n8n:latest
    container_name: localmind-n8n
    ports:
      - "5678:5678"
    environment:
      - N8N_BASIC_AUTH_ACTIVE=true
      - N8N_BASIC_AUTH_USER=${N8N_USERNAME:-localmind}
      - N8N_BASIC_AUTH_PASSWORD=${N8N_PASSWORD:-localmind}
    volumes:
      - n8n_data:/home/node/.n8n
    networks:
      - localmind-network

volumes:
  pg_data:
  qdrant_data:
  ollama_data:
  n8n_data:

networks:
  localmind-network:
    driver: bridge
```
