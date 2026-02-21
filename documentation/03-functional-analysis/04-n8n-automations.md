# Functional Specification: n8n Automations

| Field        | Value                          |
|--------------|--------------------------------|
| **Document** | n8n Automations                |
| **Version**  | 0.1.0                          |
| **Date**     | 2026-02-09                     |
| **Project**  | LocalMind                      |

---

## Table of Contents

1. [Component Description](#1-component-description)
2. [Integration Architecture](#2-integration-architecture)
3. [Supported Triggers](#3-supported-triggers)
4. [Workflow Examples](#4-workflow-examples)
5. [Data Model](#5-data-model)
6. [Involved Classes](#6-involved-classes)
7. [Configuration](#7-configuration)
8. [n8n Deployment](#8-n8n-deployment)

---

## 1. Component Description

n8n is an open-source, self-hosted automation platform that allows the creation of automated workflows through a visual drag-and-drop (no-code) interface. LocalMind natively integrates n8n to enable users to orchestrate automatic actions in response to internal platform events.

The integration occurs through an HTTP webhook mechanism: LocalMind generates internal events that are forwarded to n8n as webhook calls. n8n receives the event and orchestrates the workflow configured by the user.

---

## 2. Integration Architecture

```
+------------------+         HTTP POST          +-------------------+
|                  |  (webhook)                 |                   |
|    LocalMind     |--------------------------->|       n8n         |
|    Backend       |                            |    (self-hosted)  |
|                  |                            |                   |
|  - Internal event|                            | - Receives event  |
|  - AutomationSvc |                            | - Runs workflow   |
|  - WebhookClient |                            | - External actions|
|                  |         HTTP REST          |                   |
|    (API REST)    |<---------------------------|  (callback)       |
|                  |                            |                   |
+------------------+                            +-------------------+
       ^                                               |
       |                                               v
+------+-------+                               +-------+--------+
|    MySQL     |                               |  External      |
|  (webhook    |                               |  Services      |
|   config)    |                               |  (Email, Slack,|
+--------------+                               |   Drive, etc.) |
                                               +----------------+
```

The bidirectional flow enables:

- **LocalMind -> n8n**: internal events that trigger n8n workflows
- **n8n -> LocalMind**: n8n workflows that invoke LocalMind REST APIs to trigger operations (document ingestion, chat, search)

---

## 3. Supported Triggers

LocalMind generates the following event types that can activate n8n workflows:

| Trigger           | Description                                      | Payload                          |
|-------------------|--------------------------------------------------|----------------------------------|
| `NEW_FILE`        | New file detected in the filesystem              | filePath, filename, size, hash   |
| `DOCUMENT_INDEXED`| Document successfully indexed in RAG             | documentId, filename, chunkCount |
| `DOCUMENT_FAILED` | Document indexing failed                         | documentId, filename, error      |
| `SCHEDULED`       | Scheduled trigger (cron)                         | scheduleName, timestamp          |
| `CHAT_COMPLETED`  | Chat conversation completed                      | conversationId, provider, tokens |
| `AGENT_EXECUTED`  | AI agent executed                                | agentType, query, latencyMs      |

### 3.1 Dispatching Mechanism

1. An internal event occurs in LocalMind (e.g. a document is indexed)
2. The `AutomationService` receives the event
3. The service queries the database to find registered webhooks for that event type
4. For each webhook found, the `WebhookClientPort` sends an HTTP POST request to the n8n webhook URL
5. n8n receives the request and executes the associated workflow

---

## 4. Workflow Examples

### 4.1 Automatic Summary on Uploaded Document

```
Trigger: DOCUMENT_INDEXED
  |
  v
n8n receives webhook with documentId
  |
  v
n8n calls LocalMind API: GET /api/v1/documents/{documentId}
  |
  v
n8n calls LocalMind API: POST /api/v1/agents/execute
  body: { agentType: "BUSINESS", query: "Genera una sintesi esecutiva di questo documento" }
  |
  v
n8n saves the summary to a local folder
  |
  v
n8n sends email notification to the user
```

### 4.2 Automatic Email Classification

```
Trigger: NEW_FILE (.eml file in inbox folder)
  |
  v
n8n receives webhook with filePath
  |
  v
n8n calls LocalMind API: POST /api/v1/documents/upload
  (upload of the .eml file)
  |
  v
Wait for DOCUMENT_INDEXED webhook
  |
  v
n8n calls LocalMind API: POST /api/v1/agents/execute
  body: { agentType: "BUSINESS", query: "Classifica questa email: urgente/normale/spam" }
  |
  v
n8n applies tags based on the classification
  |
  v
n8n moves the file to the appropriate folder
```

### 4.3 Automatic Weekly Report

```
Trigger: SCHEDULED (cron: every Friday at 5:00 PM)
  |
  v
n8n calls LocalMind API: POST /api/v1/agents/execute
  body: { agentType: "BUSINESS", query: "Genera un report settimanale dei documenti indicizzati questa settimana" }
  |
  v
n8n formats the report in HTML
  |
  v
n8n sends the report via email to the configured recipient
```

---

## 5. Data Model

### 5.1 Webhook (Entity)

| Field        | Type              | Description                              |
|--------------|-------------------|------------------------------------------|
| `id`         | UUID              | Unique webhook identifier                |
| `name`       | String            | Descriptive webhook name                 |
| `url`        | String            | Full n8n webhook URL                     |
| `eventType`  | AutomationEvent   | Event type that activates the webhook    |
| `enabled`    | boolean           | Webhook enabled/disabled                 |
| `headers`    | Map<String,String>| Additional HTTP headers                  |
| `createdAt`  | LocalDateTime     | Creation date                            |
| `updatedAt`  | LocalDateTime     | Last update date                         |

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

| Field        | Type              | Description                                  |
|--------------|-------------------|----------------------------------------------|
| `eventType`  | AutomationEvent   | Event type                                   |
| `timestamp`  | LocalDateTime     | Event timestamp                              |
| `data`       | Map<String,Object>| Event-specific data                          |
| `source`     | String            | Source component (e.g. "document-service")   |

---

## 6. Involved Classes

```
Domain Layer (localmind-domain)
+-- model/
|   +-- Webhook                # Entity: webhook configuration
|   +-- AutomationEvent (enum) # Event types
|   +-- WebhookPayload         # Value Object: event payload
+-- port/
|   +-- in/
|   |   +-- AutomationUseCase      # Port in: automation management
|   +-- out/
|       +-- WebhookRepository      # Port out: webhook persistence
|       +-- WebhookClientPort      # Port out: HTTP webhook sending
+-- service/
    +-- AutomationService          # Domain service: automation logic

Infrastructure Layer (localmind-infrastructure)
+-- automation/
    +-- adapter/
    |   +-- N8nWebhookClient       # Adapter: implements WebhookClientPort (HTTP client)
    +-- persistence/
        +-- entity/
        |   +-- WebhookEntity          # JPA entity
        +-- repository/
        |   +-- JpaWebhookRepository   # Spring Data JPA
        +-- adapter/
            +-- WebhookPersistenceAdapter  # Implements WebhookRepository

API Layer (localmind-api)
+-- automation/
    +-- controller/
    |   +-- AutomationController   # REST: /api/v1/automations
    +-- dto/
        +-- WebhookDto             # Webhook DTO
        +-- WebhookCreateDto       # Webhook creation DTO
```

---

## 7. Configuration

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

## 8. n8n Deployment

n8n is an optional infrastructure service that can be run natively or via Docker:

### 8.1 Native Execution

```bash
# Installazione globale via npm
npm install -g n8n

# Avvio con variabili d'ambiente
N8N_BASIC_AUTH_ACTIVE=true \
N8N_BASIC_AUTH_USER=${N8N_USERNAME:-localmind} \
N8N_BASIC_AUTH_PASSWORD=${N8N_PASSWORD:-localmind} \
n8n start
```

### 8.2 Docker Execution (optional)

```bash
docker run -d \
  --name localmind-n8n \
  -p 5678:5678 \
  -e N8N_BASIC_AUTH_ACTIVE=true \
  -e N8N_BASIC_AUTH_USER=${N8N_USERNAME:-localmind} \
  -e N8N_BASIC_AUTH_PASSWORD=${N8N_PASSWORD:-localmind} \
  -e N8N_HOST=localhost \
  -e N8N_PORT=5678 \
  -e N8N_PROTOCOL=http \
  -e WEBHOOK_URL=http://localhost:5678 \
  -v n8n_data:/home/node/.n8n \
  n8nio/n8n:latest
```

### 8.3 Access

- **URL**: http://localhost:5678
- **Authentication**: Basic Auth (configurable username/password)
- **Interface**: visual drag-and-drop editor for workflow creation
- **Webhook URL**: http://localhost:5678/webhook/{webhook-id}
