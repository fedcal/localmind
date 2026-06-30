export const content = `# Architettura Generale

| Campo        | Valore                          |
|--------------|---------------------------------|
| **Documento**| Architettura Generale           |
| **Versione** | 1.0.0                           |
| **Data**     | 2026-02-09                      |
| **Progetto** | LocalMind                       |

---

## Indice

- [Architettura Generale](#architettura-generale)
  - [Indice](#indice)
  - [1. Panoramica Architetturale](#1-panoramica-architetturale)
  - [2. Architettura a 3 Tier](#2-architettura-a-3-tier)
  - [3. Diagramma C4 Context](#3-diagramma-c4-context)
    - [3.1 Descrizione delle Interazioni](#31-descrizione-delle-interazioni)
  - [4. Diagramma C4 Container](#4-diagramma-c4-container)
    - [4.1 Elenco Servizi](#41-elenco-servizi)
  - [5. Protocolli di Comunicazione](#5-protocolli-di-comunicazione)
    - [5.1 REST API (JSON)](#51-rest-api-json)
    - [5.2 gRPC (Qdrant)](#52-grpc-qdrant)
    - [5.3 SSE (Server-Sent Events)](#53-sse-server-sent-events)
    - [5.4 Comunicazione Futura](#54-comunicazione-futura)
  - [6. Esecuzione dei Servizi](#6-esecuzione-dei-servizi)
    - [6.1 Modalita' di Esecuzione](#61-modalita-di-esecuzione)
    - [6.2 Configurazione Database](#62-configurazione-database)

---

## 1. Panoramica Architetturale

LocalMind adotta un'architettura a tre livelli (3-tier) con una netta separazione tra presentazione, logica applicativa e persistenza dei dati. Backend e frontend vengono eseguiti nativamente tramite script dedicati nella cartella \`scripts/\`, mentre i servizi infrastrutturali opzionali (Qdrant, Ollama, n8n) possono essere avviati nativamente o tramite Docker.

L'architettura e' progettata per:

- **Modularita'**: ogni componente e' indipendente e sostituibile
- **Scalabilita'**: i servizi possono essere scalati indipendentemente
- **Testabilita'**: la separazione dei livelli consente test unitari, di integrazione e end-to-end
- **Deployment semplificato**: script di setup e avvio automatizzano la configurazione dell'ambiente

---

## 2. Architettura a 3 Tier

\`\`\`
+-----------------------------------------------------------------------+
|                        PRESENTATION TIER                              |
|                                                                       |
|  +----------------------------------------------------------------+   |
|  |                    LocalMind UI (Angular 21)                   |   |
|  |                                                                |   |
|  |  - Standalone components                                       |   |
|  |  - Signal-based state management                               |   |
|  |  - REST client (HttpClient)                                    |   |
|  |  - Porta: 4200 (dev) / 80 (prod via nginx)                     |   |
|  +----------------------------------------------------------------+   |
|                              |                                        |
|                         HTTP REST (JSON)                              |
|                              |                                        |
+-----------------------------------------------------------------------+
|                        APPLICATION TIER                               |
|                                                                       |
|  +-----------------------------------------------------------------+  |
|  |                 LocalMind API (Spring Boot 3.4)                 |  |
|  |                                                                 |  |
|  |  +------------------+  +------------------+  +---------------+  |  |
|  |  | localmind-api    |  | localmind-domain |  | localmind-    |  |  |
|  |  | (controllers,    |  | (entities,       |  | infrastructure|  |  |
|  |  |  DTOs, mapping)  |  |  services, ports)|  | (adapters,    |  |  |
|  |  +------------------+  +------------------+  |  JPA, AI)     |  |  |
|  |                                              +---------------+  |  |
|  |  +------------------+  +------------------+                     |  |
|  |  | localmind-batch  |  | localmind-app    |                     |  |
|  |  | (Spring Batch    |  | (bootstrap,      |                     |  |
|  |  |  jobs, config)   |  |  configuration)  |                     |  |
|  |  +------------------+  +------------------+                     |  |
|  |                                                                 |  |
|  |  Porta: 8080                                                    |  |
|  +-----------------------------------------------------------------+  |
|                              |                                        |
+-----------------------------------------------------------------------+
|                          DATA TIER                                    |
|                                                                       |
|  +-----------------------+  +------------------------------------+    |
|  |    MySQL 8.0          |  |           Qdrant                   |    |
|  |                       |  |                                    |    |
|  |  - Dati relazionali   |  |  - Vector store                    |    |
|  |  - Metadati documenti |  |  - Embedding documenti             |    |
|  |  - Config utente      |  |  - Ricerca semantica               |    |
|  |  - Usage/cost data    |  |                                    |    |
|  |  - Batch job metadata |  |  Porte: 6333 (HTTP), 6334 (gRPC)   |    |
|  |                       |  |                                    |    |
|  |  Porta: 3306          |  |                                    |    |
|  +-----------------------+  +------------------------------------+    |
|                                                                       |
+-----------------------------------------------------------------------+
\`\`\`

---

## 3. Diagramma C4 Context

Il diagramma C4 Context mostra LocalMind nel contesto del suo ambiente esterno, evidenziando le interazioni con utenti e sistemi esterni.

\`\`\`
                                +-------------------+
                                |                   |
                                |      Utente       |
                                |                   |
                                +--------+----------+
                                         |
                                    Browser
                                         |
                                         v
                        +----------------+-------------------+
                        |                                    |
                        |       LocalMind UI (Angular)       |
                        |                                    |
                        +----------------+-------------------+
                                         |
                                    REST API
                                         |
                                         v
                        +----------------+-------------------+
                        |                                    |
                        |     LocalMind API (Spring Boot)    |
                        |                                    |
                        +--+------+------+------+------+-----+
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
              |    MySQL      |  |     Qdrant      |
              |  (locale)     |  |    (locale)     |
              |               |  |                 |
              +---------------+  +-----------------+

                        +-------------------+
                        |                   |
                        |       n8n         |
                        |   (self-hosted)   |
                        |                   |
                        +-------------------+
\`\`\`

### 3.1 Descrizione delle Interazioni

| Sorgente         | Destinazione       | Protocollo    | Descrizione                           |
|------------------|--------------------|---------------|---------------------------------------|
| Utente           | LocalMind UI       | HTTPS         | Interfaccia web via browser           |
| LocalMind UI     | LocalMind API      | HTTP REST     | Chiamate API JSON                     |
| LocalMind API    | Ollama             | HTTP REST     | Richieste LLM locali (porta 11434)    |
| LocalMind API    | OpenAI             | HTTPS REST    | Richieste LLM cloud                   |
| LocalMind API    | Anthropic          | HTTPS REST    | Richieste LLM cloud                   |
| LocalMind API    | Google             | HTTPS REST    | Richieste LLM cloud                   |
| LocalMind API    | MySQL              | JDBC          | Persistenza dati relazionali (3306)   |
| LocalMind API    | Qdrant             | gRPC/HTTP     | Vector store (6334/6333)              |
| LocalMind API    | n8n                | HTTP POST     | Webhook per automazioni (5678)        |
| LocalMind API    | File System        | Java NIO      | Lettura documenti locali              |
| n8n              | LocalMind API      | HTTP REST     | Callback e invocazione API            |
| n8n              | Servizi Esterni    | HTTPS         | Email, Slack, Drive, etc.             |

---

## 4. Diagramma C4 Container

Il diagramma C4 Container mostra i container (processi) che compongono LocalMind e le loro interazioni.

\`\`\`
+-------------------------------------------------------------------+
|                   Esecuzione Nativa (host)                        |
|                                                                   |
|  +-----------------------------+  +----------------------------+  |
|  |   localmind-frontend        |  |   localmind-backend        |  |
|  |   (Angular CLI / ng serve)  |  |   (Spring Boot fat JAR)    |  |
|  |                             |  |                            |  |
|  |   Porta: 4200 (dev)         |  |   Porta: 8080              |  |
|  +-----------------------------+  +---+------+------+------+---+  |
|                                       |      |      |      |      |
|                          +------------+  +---+--+ +-+----+ |      |
|                          |               |      | |      | |      |
|                          v               v      v v      v v      |
|  +-----------------------------+  +----------------------------+  |
|  |   localmind-db              |  |   localmind-qdrant         |  |
|  |   (MySQL 8.0)               |  |   (Qdrant) [opzionale]     |  |
|  |                             |  |                            |  |
|  |   Porta: 3306               |  |   Porta: 6333 (HTTP)       |  |
|  |   nativo o Docker           |  |         6334 (gRPC)        |  |
|  +-----------------------------+  +----------------------------+  |
|                                                                   |
|  +-----------------------------+  +----------------------------+  |
|  |   localmind-ollama          |  |   localmind-n8n            |  |
|  |   (Ollama) [opzionale]      |  |   (n8n) [opzionale]        |  |
|  |                             |  |                            |  |
|  |   Porta: 11434              |  |   Porta: 5678              |  |
|  |   nativo o Docker           |  |   nativo o Docker          |  |
|  |   GPU: nvidia (opzionale)   |  |   Auth: Basic              |  |
|  +-----------------------------+  +----------------------------+  |
|                                                                   |
+-------------------------------------------------------------------+
\`\`\`

### 4.1 Elenco Servizi

| Servizio            | Esecuzione                     | Porta     | Funzione                 |
|---------------------|--------------------------------|-----------|--------------------------|
| localmind-frontend  | Nativo (Angular CLI)           | 4200      | UI Angular               |
| localmind-backend   | Nativo (Java JAR)              | 8080      | API Spring Boot          |
| localmind-db        | Nativo o Docker (\`mysql:8.0\`)  | 3306      | Database relazionale     |
| localmind-qdrant    | Nativo o Docker (opzionale)    | 6333/6334 | Vector store             |
| localmind-ollama    | Nativo o Docker (opzionale)    | 11434     | LLM locale               |
| localmind-n8n       | Nativo o Docker (opzionale)    | 5678      | Automazioni              |

---

## 5. Protocolli di Comunicazione

### 5.1 REST API (JSON)

La comunicazione tra frontend e backend avviene tramite API REST con payload JSON:

- **Content-Type**: \`application/json\`
- **Encoding**: UTF-8
- **Autenticazione**: JWT (futuro) / Basic Auth (fase iniziale)
- **Versioning**: URL path (\`/api/v1/...\`)
- **Error handling**: RFC 7807 Problem Details

### 5.2 gRPC (Qdrant)

La comunicazione tra backend e Qdrant utilizza preferibilmente il protocollo gRPC per massime performance:

- **Porta**: 6334
- **Protocollo**: HTTP/2 + Protocol Buffers
- **Fallback**: HTTP REST (porta 6333) in caso di problemi gRPC

### 5.3 SSE (Server-Sent Events)

La comunicazione in streaming tra frontend e backend per la chat utilizza il protocollo SSE:

- **Content-Type**: \`text/event-stream\`
- **Caso d'uso**: Chat streaming in tempo reale
- **Endpoint**: \`POST /api/v1/chat/stream\`
- **Stato**: Implementato
- **Fallback**: in caso di errore, il sistema ricade automaticamente sulla modalita' sincrona (\`POST /api/v1/chat\`)

### 5.4 Comunicazione Futura

| Protocollo | Caso d'Uso                    | Stato       |
|------------|-------------------------------|-------------|
| WebSocket  | Chat real-time bidirezionale  | Pianificato |
| gRPC       | Comunicazione inter-servizio  | Valutazione |

---

## 6. Esecuzione dei Servizi

### 6.1 Modalita' di Esecuzione

Il progetto non utilizza piu' un file \`docker-compose.yml\` per lo stack completo. Backend e frontend vengono eseguiti nativamente tramite script nella cartella \`scripts/\`.

| Servizio       | Modalita'                                      | Avvio                          |
|----------------|------------------------------------------------|--------------------------------|
| Backend        | Nativo (Java JAR)                              | \`./scripts/start-backend.sh\`   |
| Frontend       | Nativo (Angular CLI)                           | \`./scripts/start-frontend.sh\`  |
| MySQL          | Nativo o Docker (auto-rilevato da setup.sh)    | \`./scripts/setup.sh\`           |
| Qdrant         | Nativo o Docker (opzionale)                    | Avvio manuale                  |
| Ollama         | Nativo o Docker (opzionale)                    | Avvio manuale                  |
| n8n            | Nativo o Docker (opzionale)                    | Avvio manuale                  |

### 6.2 Configurazione Database

\`\`\`yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/localmind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: localmind
    password: localmind
    driver-class-name: com.mysql.cj.jdbc.Driver
\`\`\`

> **Nota**: per i dettagli sull'avvio dei servizi infrastrutturali opzionali tramite Docker, consultare la sezione [Infrastruttura e Servizi](/docs/05-technology-stack/docker-infrastructure).
`;
