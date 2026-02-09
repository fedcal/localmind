# Specifica Funzionale: Automazioni n8n

| Campo        | Valore                          |
|--------------|---------------------------------|
| **Documento**| Automazioni n8n                 |
| **Versione** | 0.1.0                          |
| **Data**     | 2026-02-09                      |
| **Progetto** | LocalMind                       |

---

## Indice

1. [Descrizione del Componente](#1-descrizione-del-componente)
2. [Architettura dell'Integrazione](#2-architettura-dellintegrazione)
3. [Trigger Supportati](#3-trigger-supportati)
4. [Esempi di Workflow](#4-esempi-di-workflow)
5. [Modello dei Dati](#5-modello-dei-dati)
6. [Classi Coinvolte](#6-classi-coinvolte)
7. [Configurazione](#7-configurazione)
8. [Deployment Docker](#8-deployment-docker)

---

## 1. Descrizione del Componente

n8n e' una piattaforma di automazione open-source, self-hosted, che consente la creazione di workflow automatici tramite un'interfaccia visuale drag-and-drop (no-code). LocalMind integra nativamente n8n per consentire agli utenti di orchestrare azioni automatiche in risposta a eventi interni della piattaforma.

L'integrazione avviene tramite meccanismo di webhook HTTP: LocalMind genera eventi interni che vengono inoltrati a n8n sotto forma di chiamate webhook. n8n riceve l'evento e orchestra il workflow configurato dall'utente.

---

## 2. Architettura dell'Integrazione

```
+------------------+         HTTP POST          +------------------+
|                  |  (webhook)                  |                  |
|    LocalMind     |--------------------------->|       n8n        |
|    Backend       |                            |    (self-hosted) |
|                  |                            |                  |
|  - Evento interno|                            |  - Riceve evento |
|  - AutomationSvc |                            |  - Esegue workflow|
|  - WebhookClient |                            |  - Azioni esterne|
|                  |         HTTP REST           |                  |
|    (API REST)    |<---------------------------|  (callback)      |
|                  |                            |                  |
+------------------+                            +------------------+
       ^                                               |
       |                                               v
+------+------+                               +-------+--------+
|  PostgreSQL  |                               |  Servizi       |
|  (config     |                               |  Esterni       |
|   webhook)   |                               |  (Email, Slack,|
+--------------+                               |   Drive, etc.) |
                                               +----------------+
```

Il flusso bidirezionale consente:

- **LocalMind -> n8n**: eventi interni che triggano workflow n8n
- **n8n -> LocalMind**: workflow n8n che invocano le API REST di LocalMind per triggare operazioni (ingestione documenti, chat, ricerca)

---

## 3. Trigger Supportati

LocalMind genera i seguenti tipi di evento che possono attivare workflow n8n:

| Trigger            | Descrizione                                      | Payload                          |
|-------------------|--------------------------------------------------|----------------------------------|
| `NEW_FILE`        | Nuovo file rilevato nel filesystem               | filePath, filename, size, hash   |
| `DOCUMENT_INDEXED`| Documento indicizzato con successo nel RAG       | documentId, filename, chunkCount |
| `DOCUMENT_FAILED` | Indicizzazione documento fallita                 | documentId, filename, error      |
| `SCHEDULED`       | Trigger schedulato (cron)                        | scheduleName, timestamp          |
| `CHAT_COMPLETED`  | Conversazione chat completata                    | conversationId, provider, tokens |
| `AGENT_EXECUTED`  | Agente AI eseguito                               | agentType, query, latencyMs      |

### 3.1 Meccanismo di Dispatching

1. Un evento interno si verifica in LocalMind (es. un documento viene indicizzato)
2. Il servizio `AutomationService` riceve l'evento
3. Il servizio interroga il database per trovare i webhook registrati per quel tipo di evento
4. Per ogni webhook trovato, il `WebhookClientPort` invia una richiesta HTTP POST all'URL del webhook n8n
5. n8n riceve la richiesta e esegue il workflow associato

---

## 4. Esempi di Workflow

### 4.1 Summary Automatica su Documento Caricato

```
Trigger: DOCUMENT_INDEXED
  |
  v
n8n riceve webhook con documentId
  |
  v
n8n chiama LocalMind API: GET /api/v1/documents/{documentId}
  |
  v
n8n chiama LocalMind API: POST /api/v1/agents/execute
  body: { agentType: "BUSINESS", query: "Genera una sintesi esecutiva di questo documento" }
  |
  v
n8n salva la sintesi in una cartella locale
  |
  v
n8n invia notifica email all'utente
```

### 4.2 Classificazione Email Automatica

```
Trigger: NEW_FILE (file .eml in cartella inbox)
  |
  v
n8n riceve webhook con filePath
  |
  v
n8n chiama LocalMind API: POST /api/v1/documents/upload
  (upload del file .eml)
  |
  v
Attesa DOCUMENT_INDEXED webhook
  |
  v
n8n chiama LocalMind API: POST /api/v1/agents/execute
  body: { agentType: "BUSINESS", query: "Classifica questa email: urgente/normale/spam" }
  |
  v
n8n applica tag basati sulla classificazione
  |
  v
n8n sposta il file nella cartella appropriata
```

### 4.3 Report Settimanale Automatico

```
Trigger: SCHEDULED (cron: ogni venerdi' ore 17:00)
  |
  v
n8n chiama LocalMind API: POST /api/v1/agents/execute
  body: { agentType: "BUSINESS", query: "Genera un report settimanale dei documenti indicizzati questa settimana" }
  |
  v
n8n formatta il report in HTML
  |
  v
n8n invia il report via email al destinatario configurato
```

---

## 5. Modello dei Dati

### 5.1 Webhook (Entity)

| Campo         | Tipo             | Descrizione                              |
|--------------|------------------|------------------------------------------|
| `id`         | UUID             | Identificativo univoco del webhook       |
| `name`       | String           | Nome descrittivo del webhook             |
| `url`        | String           | URL completo del webhook n8n             |
| `eventType`  | AutomationEvent  | Tipo di evento che attiva il webhook     |
| `enabled`    | boolean          | Webhook abilitato/disabilitato           |
| `headers`    | Map<String,String>| Header HTTP aggiuntivi                  |
| `createdAt`  | LocalDateTime    | Data di creazione                        |
| `updatedAt`  | LocalDateTime    | Data ultimo aggiornamento                |

### 5.2 AutomationEvent (Enum)

```java
public enum AutomationEvent {
    NEW_FILE,
    DOCUMENT_INDEXED,
    DOCUMENT_FAILED,
    SCHEDULED,
    CHAT_COMPLETED,
    AGENT_EXECUTED
}
```

### 5.3 WebhookPayload (Value Object)

| Campo         | Tipo              | Descrizione                              |
|--------------|-------------------|------------------------------------------|
| `eventType`  | AutomationEvent   | Tipo di evento                           |
| `timestamp`  | LocalDateTime     | Timestamp dell'evento                    |
| `data`       | Map<String,Object>| Dati specifici dell'evento               |
| `source`     | String            | Componente sorgente (es. "document-service") |

---

## 6. Classi Coinvolte

```
Domain Layer (localmind-domain)
+-- model/
|   +-- Webhook                # Entity: configurazione webhook
|   +-- AutomationEvent (enum) # Tipi di evento
|   +-- WebhookPayload         # Value Object: payload evento
+-- port/
|   +-- in/
|   |   +-- AutomationUseCase      # Port in: gestione automazioni
|   +-- out/
|       +-- WebhookRepository      # Port out: persistenza webhook
|       +-- WebhookClientPort      # Port out: invio webhook HTTP
+-- service/
    +-- AutomationService          # Domain service: logica automazioni

Infrastructure Layer (localmind-infrastructure)
+-- automation/
    +-- adapter/
    |   +-- N8nWebhookClient       # Adapter: implementa WebhookClientPort (HTTP client)
    +-- persistence/
        +-- entity/
        |   +-- WebhookEntity          # JPA entity
        +-- repository/
        |   +-- JpaWebhookRepository   # Spring Data JPA
        +-- adapter/
            +-- WebhookPersistenceAdapter  # Implementa WebhookRepository

API Layer (localmind-api)
+-- automation/
    +-- controller/
    |   +-- AutomationController   # REST: /api/v1/automations
    +-- dto/
        +-- WebhookDto             # DTO webhook
        +-- WebhookCreateDto       # DTO creazione webhook
```

---

## 7. Configurazione

```yaml
localmind:
  n8n:
    # URL base dell'istanza n8n
    base-url: http://localhost:5678

    # Path base per i webhook
    webhook-path: /webhook

    # Timeout per le chiamate webhook
    timeout: 30s

    # Retry in caso di errore
    retry:
      max-attempts: 3
      backoff-ms: 2000

    # Autenticazione
    auth:
      enabled: true
      type: basic
      username: ${N8N_USERNAME:localmind}
      password: ${N8N_PASSWORD:localmind}
```

---

## 8. Deployment Docker

n8n viene deployato come servizio Docker all'interno dello stack LocalMind:

```yaml
# docker-compose.yml (estratto)
services:
  n8n:
    image: n8nio/n8n:latest
    container_name: localmind-n8n
    restart: unless-stopped
    ports:
      - "5678:5678"
    environment:
      - N8N_BASIC_AUTH_ACTIVE=true
      - N8N_BASIC_AUTH_USER=${N8N_USERNAME:-localmind}
      - N8N_BASIC_AUTH_PASSWORD=${N8N_PASSWORD:-localmind}
      - N8N_HOST=localhost
      - N8N_PORT=5678
      - N8N_PROTOCOL=http
      - WEBHOOK_URL=http://localhost:5678
    volumes:
      - n8n_data:/home/node/.n8n
    networks:
      - localmind-network

volumes:
  n8n_data:

networks:
  localmind-network:
    driver: bridge
```

### 8.1 Accesso

- **URL**: http://localhost:5678
- **Autenticazione**: Basic Auth (username/password configurabili)
- **Interfaccia**: editor visuale drag-and-drop per creazione workflow
- **Webhook URL**: http://localhost:5678/webhook/{webhook-id}
