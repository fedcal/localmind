export const content = `# Panoramica API

**Progetto:** LocalMind
**Versione:** 1.0.0
**Data:** 2026-02-18
**Base URL:** \`http://localhost:8080/api/v1\`

---

## Indice

1. [Informazioni Generali](#1-informazioni-generali)
2. [Convenzioni](#2-convenzioni)
3. [Autenticazione](#3-autenticazione)
4. [Gestione Errori](#4-gestione-errori)
5. [CORS](#5-cors)
6. [Riepilogo Endpoint](#6-riepilogo-endpoint)

---

## 1. Informazioni Generali

Le API REST di LocalMind espongono le funzionalita' della piattaforma attraverso endpoint HTTP standard. Tutte le API sono prefissate con \`/api/v1\` per consentire il versionamento futuro.

| Proprieta'         | Valore                                  |
|--------------------|-----------------------------------------|
| **Base URL**       | \`http://localhost:8080/api/v1\`          |
| **Protocollo**     | HTTP (HTTPS pianificato per produzione) |
| **Porta**          | 8080                                    |
| **Formato dati**   | JSON (\`application/json\`)               |
| **Encoding**       | UTF-8                                   |
| **Upload file**    | \`multipart/form-data\`                   |
| **Autenticazione** | JWT Bearer Token                        |

---

## 2. Convenzioni

### Content-Type

- **Request/Response standard**: \`application/json\`
- **Upload file**: \`multipart/form-data\` (endpoint \`/documents/upload\`)
- **Streaming chat SSE**: \`text/event-stream\` (endpoint \`/chat/stream\`)
- **Limite upload**: 50MB (\`spring.servlet.multipart.max-file-size=50MB\`)

### Identificatori

Tutti gli identificatori di risorse sono di tipo UUID in formato stringa:

\`\`\`
550e8400-e29b-41d4-a716-446655440000
\`\`\`

### Timestamp

I timestamp sono in formato ISO 8601 con timezone UTC:

\`\`\`
2026-02-09T14:30:00Z
\`\`\`

### Codici di stato HTTP

| Codice | Significato          | Utilizzo                                   |
|--------|----------------------|--------------------------------------------|
| 200    | OK                   | Richiesta completata con successo          |
| 201    | Created              | Risorsa creata con successo (upload)       |
| 204    | No Content           | Eliminazione completata                    |
| 400    | Bad Request          | Validazione fallita, parametri mancanti    |
| 404    | Not Found            | Risorsa non trovata                        |
| 500    | Internal Server Error| Errore interno del server                  |
| 502    | Bad Gateway          | Errore del provider LLM                    |

---

## 3. Autenticazione

A partire dalla versione 1.0.0, le API di LocalMind utilizzano **JWT Bearer Token** per l'autenticazione.

### Setup iniziale

Al primo avvio, l'applicazione richiede la configurazione di una password tramite l'endpoint di setup:

\`\`\`bash
curl -X POST http://localhost:8080/api/v1/auth/setup \\
  -H "Content-Type: application/json" \\
  -d '{"password": "la-tua-password"}'
\`\`\`

### Login

Per ottenere un token JWT:

\`\`\`bash
curl -X POST http://localhost:8080/api/v1/auth/login \\
  -H "Content-Type: application/json" \\
  -d '{"password": "la-tua-password"}'
\`\`\`

**Response**:

\`\`\`json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresAt": "2026-02-19T14:30:00Z"
}
\`\`\`

### Utilizzo del token

Includere il token JWT nell'header \`Authorization\` di ogni richiesta:

\`\`\`
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
\`\`\`

### Endpoint pubblici

I seguenti endpoint **non richiedono autenticazione**:

- \`POST /api/v1/auth/login\`
- \`POST /api/v1/auth/setup\`
- \`GET /api/v1/auth/status\`

### Verifica stato autenticazione

Per verificare se il sistema e' gia' configurato:

\`\`\`bash
curl http://localhost:8080/api/v1/auth/status
\`\`\`

**Response**:

\`\`\`json
{
  "configured": true,
  "authenticated": false
}
\`\`\`

> **Nota**: il token JWT ha una scadenza configurabile. Alla scadenza, e' necessario effettuare un nuovo login.

---

## 4. Gestione Errori

### GlobalExceptionHandler

La gestione centralizzata degli errori e' implementata tramite \`@RestControllerAdvice\` nel package \`com.localmind.api.common.advice\`.

**File**: \`localmind-api/src/main/java/com/localmind/api/common/advice/GlobalExceptionHandler.java\`

### Mapping eccezioni

| Eccezione                     | Codice HTTP | Descrizione                                    |
|-------------------------------|-------------|------------------------------------------------|
| \`ResourceNotFoundException\`   | 404         | Risorsa richiesta non trovata                  |
| \`LlmProviderException\`        | 502         | Errore nella comunicazione con il provider LLM |
| \`DocumentProcessingException\` | 500         | Errore durante l'elaborazione di un documento  |
| \`Exception\` (generica)        | 500         | Errore interno non gestito                     |

### Formato ErrorResponseDto

Tutte le risposte di errore seguono il formato standard \`ErrorResponseDto\`:

**Classe**: \`com.localmind.api.common.dto.ErrorResponseDto\`

\`\`\`json
{
  "status": 404,
  "message": "Document not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-09T14:30:00Z",
  "path": "/api/v1/documents/550e8400-e29b-41d4-a716-446655440000"
}
\`\`\`

| Campo       | Tipo     | Descrizione                                       |
|-------------|----------|---------------------------------------------------|
| \`status\`    | \`int\`    | Codice di stato HTTP                              |
| \`message\`   | \`String\` | Messaggio descrittivo dell'errore                 |
| \`timestamp\` | \`Instant\`| Data/ora dell'errore in formato ISO 8601          |
| \`path\`      | \`String\` | Percorso della richiesta che ha generato l'errore |

### Implementazione

\`\`\`java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(404).body(
                ErrorResponseDto.of(404, ex.getMessage(), req.getRequestURI())
        );
    }

    @ExceptionHandler(LlmProviderException.class)
    public ResponseEntity<ErrorResponseDto> handleLlmError(
            LlmProviderException ex, HttpServletRequest req) {
        log.error("LLM provider error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(502).body(
                ErrorResponseDto.of(502, "LLM provider error: " + ex.getMessage(),
                    req.getRequestURI())
        );
    }

    @ExceptionHandler(DocumentProcessingException.class)
    public ResponseEntity<ErrorResponseDto> handleDocumentError(
            DocumentProcessingException ex, HttpServletRequest req) {
        log.error("Document processing error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(500).body(
                ErrorResponseDto.of(500, ex.getMessage(), req.getRequestURI())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneral(
            Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(500).body(
                ErrorResponseDto.of(500, "Internal server error", req.getRequestURI())
        );
    }
}
\`\`\`

---

## 5. CORS

La configurazione CORS e' gestita tramite Spring Security e consente l'accesso dal frontend Angular in esecuzione locale.

| Proprieta'             | Valore                                    |
|------------------------|-------------------------------------------|
| **Origini consentite** | \`http://localhost:4200\`                   |
| **Metodi consentiti**  | \`GET\`, \`POST\`, \`PUT\`, \`DELETE\`, \`OPTIONS\` |
| **Header consentiti**  | \`*\` (tutti)                               |
| **Credenziali**        | Non abilitate                             |
             
> **Nota**: in produzione, la configurazione CORS dovra' essere aggiornata per riflettere il dominio effettivo dell'applicazione.

---

## 6. Riepilogo Endpoint

### Autenticazione

| Metodo | Endpoint                  | Descrizione            | Request Body       | Response          |
|--------|---------------------------|------------------------|--------------------|-------------------|
| POST   | \`/api/v1/auth/login\`      | Login                  | \`LoginRequestDto\`  | \`AuthResponseDto\` |
| POST   | \`/api/v1/auth/setup\`      | Setup password         | \`SetupRequestDto\`  | \`AuthResponseDto\` |
| GET    | \`/api/v1/auth/status\`     | Stato autenticazione   | -                  | \`AuthStatusDto\`   |

### Chat

| Metodo | Endpoint        | Descrizione                    | Request Body      | Response              |
|--------|-----------------|--------------------------------|-------------------|-----------------------|
| POST   | \`/api/v1/chat\`  | Invia un messaggio alla chat   | \`ChatRequestDto\`  | \`ChatResponseDto\`     |

### Chat Streaming

| Metodo | Endpoint               | Descrizione                    | Request Body      | Response              |
|--------|------------------------|--------------------------------|-------------------|-----------------------|
| POST   | \`/api/v1/chat/stream\`  | Chat con streaming SSE         | \`ChatRequestDto\`  | \`text/event-stream\`   |

### Conversazioni

| Metodo | Endpoint                       | Descrizione               | Request Body             | Response                                  |
|--------|--------------------------------|---------------------------|--------------------------|-------------------------------------------|
| GET    | \`/api/v1/conversations\`        | Lista conversazioni       | -                        | \`PaginatedResponse<ConversationSummaryDto>\` |
| GET    | \`/api/v1/conversations/{id}\`   | Dettaglio conversazione   | -                        | \`ConversationDto\`                         |
| POST   | \`/api/v1/conversations\`        | Crea conversazione        | \`CreateConversationDto\`  | \`ConversationDto\`                         |
| PUT    | \`/api/v1/conversations/{id}\`   | Aggiorna conversazione    | \`UpdateConversationDto\`  | \`ConversationDto\`                         |
| DELETE | \`/api/v1/conversations/{id}\`   | Elimina conversazione     | -                        | 204 No Content                            |

### Documenti

| Metodo | Endpoint                      | Descrizione                    | Request Body        | Response                  |
|--------|-------------------------------|--------------------------------|---------------------|---------------------------|
| POST   | \`/api/v1/documents/upload\`    | Carica un documento            | \`multipart/form-data\`| \`DocumentDto\`            |
| GET    | \`/api/v1/documents\`           | Lista tutti i documenti        | -                   | \`List<DocumentDto>\`       |
| GET    | \`/api/v1/documents/{id}\`      | Dettaglio documento            | -                   | \`DocumentDto\`             |
| DELETE | \`/api/v1/documents/{id}\`      | Elimina un documento           | -                   | 204 No Content            |
| POST   | \`/api/v1/documents/search\`    | Ricerca semantica nei documenti| \`SearchRequestDto\`  | \`List<SearchResultDto>\`   |

### Cartelle

| Metodo | Endpoint                      | Descrizione            | Request Body      | Response              |
|--------|-------------------------------|------------------------|-------------------|-----------------------|
| GET    | \`/api/v1/folders\`             | Lista cartelle         | -                 | \`List<FolderConfigDto>\` |
| POST   | \`/api/v1/folders\`             | Crea cartella          | \`CreateFolderDto\` | \`FolderConfigDto\`     |
| DELETE | \`/api/v1/folders/{id}\`        | Elimina cartella       | -                 | 204 No Content        |
| POST   | \`/api/v1/folders/{id}/sync\`   | Trigger sync           | -                 | 200 OK                |

### Settings/Providers

| Metodo | Endpoint                                       | Descrizione              | Request Body              | Response              |
|--------|-------------------------------------------------|--------------------------|---------------------------|-----------------------|
| GET    | \`/api/v1/settings/providers\`                   | Lista provider LLM       | -                         | \`List<ProviderConfigDto>\` |
| POST   | \`/api/v1/settings/providers\`                   | Crea/aggiorna provider   | \`CreateProviderRequestDto\`| \`ProviderConfigDto\`   |
| DELETE | \`/api/v1/settings/providers/{id}\`              | Elimina provider         | -                         | 204 No Content        |
| POST   | \`/api/v1/settings/providers/{id}/test\`         | Test connessione         | -                         | \`TestResultDto\`       |
| GET    | \`/api/v1/settings/providers/ollama/models\`     | Modelli Ollama           | \`?baseUrl=\`               | \`List<String>\`        |

### Webhooks

| Metodo | Endpoint                        | Descrizione            | Request Body        | Response              |
|--------|---------------------------------|------------------------|---------------------|-----------------------|
| GET    | \`/api/v1/webhooks\`              | Lista webhooks         | -                   | \`List<WebhookResponseDto>\` |
| GET    | \`/api/v1/webhooks/{id}\`         | Dettaglio webhook      | -                   | \`WebhookResponseDto\`  |
| POST   | \`/api/v1/webhooks\`              | Crea webhook           | \`WebhookRequestDto\` | \`WebhookResponseDto\`  |
| PUT    | \`/api/v1/webhooks/{id}\`         | Aggiorna webhook       | \`WebhookRequestDto\` | \`WebhookResponseDto\`  |
| DELETE | \`/api/v1/webhooks/{id}\`         | Elimina webhook        | -                   | 204 No Content        |
| POST   | \`/api/v1/webhooks/{id}/test\`    | Test webhook           | -                   | 200 OK                |

### MCP

| Metodo | Endpoint                      | Descrizione            | Request Body                | Response              |
|--------|-------------------------------|------------------------|-----------------------------|-----------------------|
| POST   | \`/api/v1/mcp/servers\`         | Registra server MCP    | \`CreateMcpServerRequestDto\` | \`McpServerDto\`        |
| GET    | \`/api/v1/mcp/servers\`         | Lista server MCP       | -                           | \`List<McpServerDto>\`  |
| DELETE | \`/api/v1/mcp/servers/{id}\`    | Rimuovi server         | -                           | 204 No Content        |
| GET    | \`/api/v1/mcp/tools\`           | Lista tool MCP         | -                           | \`List<McpToolDto>\`    |
| POST   | \`/api/v1/mcp/tools/execute\`   | Esegui tool            | \`ExecuteToolDto\`            | \`ToolResultDto\`       |

### Modelli

| Metodo | Endpoint               | Descrizione                    | Request Body | Response              |
|--------|------------------------|--------------------------------|--------------|-----------------------|
| GET    | \`/api/v1/models\`       | Lista modelli LLM disponibili  | -            | \`List<ModelDto>\`      |
| GET    | \`/api/v1/models/{id}\`  | Dettaglio modello              | -            | \`ModelDto\`            |

### Dashboard

| Metodo | Endpoint                    | Descrizione                    | Request Body | Response              |
|--------|-----------------------------|--------------------------------|--------------|-----------------------|
| GET    | \`/api/v1/dashboard/health\`  | Stato di salute dei servizi    | -            | \`HealthStatusDto\`     |
`;
