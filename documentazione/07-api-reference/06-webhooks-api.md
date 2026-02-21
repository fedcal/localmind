# Webhooks API

**Progetto:** LocalMind
**Versione:** 1.0.0
**Data:** 2026-02-18
**Base URL:** `http://localhost:8080/api/v1`

---

## Indice

1. [Panoramica](#1-panoramica)
2. [GET /api/v1/webhooks](#2-get-apiv1webhooks)
3. [GET /api/v1/webhooks/{id}](#3-get-apiv1webhooksid)
4. [POST /api/v1/webhooks](#4-post-apiv1webhooks)
5. [PUT /api/v1/webhooks/{id}](#5-put-apiv1webhooksid)
6. [DELETE /api/v1/webhooks/{id}](#6-delete-apiv1webhooksid)
7. [POST /api/v1/webhooks/{id}/test](#7-post-apiv1webhooksidtest)
8. [Modelli di Dati](#8-modelli-di-dati)
9. [Tipi di Evento](#9-tipi-di-evento)
10. [Codici di Errore](#10-codici-di-errore)

---

## 1. Panoramica

L'API Webhooks consente di configurare notifiche HTTP automatiche verso URL esterni quando si verificano determinati eventi nella piattaforma LocalMind. I webhook permettono di integrare LocalMind con sistemi esterni ricevendo callback in tempo reale per eventi come l'indicizzazione di nuovi documenti, il completamento di una chat, o il fallimento di un'elaborazione.

| Proprieta'       | Valore                                |
|------------------|---------------------------------------|
| **Controller**   | `WebhookController`                   |
| **Package**      | `com.localmind.api.webhook.controller`|
| **Base path**    | `/api/v1/webhooks`                    |
| **Content-Type** | `application/json`                    |

---

## 2. GET /api/v1/webhooks

Restituisce la lista di tutti i webhook configurati.

### Request

| Proprieta'         | Valore                                |
|--------------------|---------------------------------------|
| **URL**            | `GET /api/v1/webhooks`                |
| **Autenticazione** | JWT Bearer Token                      |

### Response Body

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Notifica indicizzazione",
    "url": "https://example.com/webhook/indexed",
    "eventType": "DOCUMENT_INDEXED",
    "active": true
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "name": "Notifica errori",
    "url": "https://example.com/webhook/errors",
    "eventType": "DOCUMENT_FAILED",
    "active": true
  }
]
```

### Status Codes

| Codice | Descrizione                              |
|--------|------------------------------------------|
| 200    | OK - Lista restituita con successo       |
| 401    | Unauthorized - Token JWT mancante o non valido |

### Esempio

```bash
curl -X GET http://localhost:8080/api/v1/webhooks \
  -H "Authorization: Bearer <token>"
```

---

## 3. GET /api/v1/webhooks/{id}

Restituisce il dettaglio di un singolo webhook.

### Request

| Proprieta'         | Valore                                |
|--------------------|---------------------------------------|
| **URL**            | `GET /api/v1/webhooks/{id}`           |
| **Autenticazione** | JWT Bearer Token                      |

### Path Parameters

| Parametro | Tipo   | Descrizione              |
|-----------|--------|--------------------------|
| `id`      | `UUID` | Identificativo webhook   |

### Response Body

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Notifica indicizzazione",
  "url": "https://example.com/webhook/indexed",
  "eventType": "DOCUMENT_INDEXED",
  "active": true
}
```

### Status Codes

| Codice | Descrizione                              |
|--------|------------------------------------------|
| 200    | OK - Webhook restituito con successo     |
| 401    | Unauthorized - Token JWT mancante o non valido |
| 404    | Not Found - Webhook non trovato          |

### Esempio

```bash
curl -X GET http://localhost:8080/api/v1/webhooks/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <token>"
```

---

## 4. POST /api/v1/webhooks

Crea un nuovo webhook.

### Request

| Proprieta'         | Valore                                |
|--------------------|---------------------------------------|
| **URL**            | `POST /api/v1/webhooks`               |
| **Content-Type**   | `application/json`                    |
| **Autenticazione** | JWT Bearer Token                      |

### Request Body - WebhookRequestDto

```json
{
  "name": "Notifica indicizzazione",
  "url": "https://example.com/webhook/indexed",
  "eventType": "DOCUMENT_INDEXED",
  "active": true
}
```

| Campo       | Tipo      | Obbligatorio | Default | Validazione                          | Descrizione                        |
|-------------|-----------|--------------|---------|--------------------------------------|------------------------------------|
| `name`      | `String`  | Si           | -       | `@NotBlank`, `@Size(min=2, max=200)` | Nome descrittivo del webhook       |
| `url`       | `String`  | Si           | -       | `@NotBlank`, `@Size(max=1000)`       | URL di destinazione della callback |
| `eventType` | `String`  | Si           | -       | `@NotBlank`                          | Tipo di evento che attiva il webhook |
| `active`    | `boolean` | No           | `true`  | -                                    | Stato di attivazione del webhook   |

### Response Body - WebhookResponseDto

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Notifica indicizzazione",
  "url": "https://example.com/webhook/indexed",
  "eventType": "DOCUMENT_INDEXED",
  "active": true
}
```

### Status Codes

| Codice | Descrizione                              |
|--------|------------------------------------------|
| 201    | Created - Webhook creato con successo    |
| 400    | Bad Request - Validazione fallita        |
| 401    | Unauthorized - Token JWT mancante o non valido |

### Esempio

```bash
curl -X POST http://localhost:8080/api/v1/webhooks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "name": "Notifica indicizzazione",
    "url": "https://example.com/webhook/indexed",
    "eventType": "DOCUMENT_INDEXED",
    "active": true
  }'
```

---

## 5. PUT /api/v1/webhooks/{id}

Aggiorna un webhook esistente.

### Request

| Proprieta'         | Valore                                |
|--------------------|---------------------------------------|
| **URL**            | `PUT /api/v1/webhooks/{id}`           |
| **Content-Type**   | `application/json`                    |
| **Autenticazione** | JWT Bearer Token                      |

### Path Parameters

| Parametro | Tipo   | Descrizione              |
|-----------|--------|--------------------------|
| `id`      | `UUID` | Identificativo webhook   |

### Request Body - WebhookRequestDto

Identico a quello della creazione (sezione 4).

### Response Body - WebhookResponseDto

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Notifica indicizzazione aggiornata",
  "url": "https://example.com/webhook/indexed-v2",
  "eventType": "DOCUMENT_INDEXED",
  "active": true
}
```

### Status Codes

| Codice | Descrizione                              |
|--------|------------------------------------------|
| 200    | OK - Webhook aggiornato con successo     |
| 400    | Bad Request - Validazione fallita        |
| 401    | Unauthorized - Token JWT mancante o non valido |
| 404    | Not Found - Webhook non trovato          |

### Esempio

```bash
curl -X PUT http://localhost:8080/api/v1/webhooks/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "name": "Notifica indicizzazione aggiornata",
    "url": "https://example.com/webhook/indexed-v2",
    "eventType": "DOCUMENT_INDEXED",
    "active": true
  }'
```

---

## 6. DELETE /api/v1/webhooks/{id}

Elimina un webhook.

### Request

| Proprieta'         | Valore                                |
|--------------------|---------------------------------------|
| **URL**            | `DELETE /api/v1/webhooks/{id}`        |
| **Autenticazione** | JWT Bearer Token                      |

### Path Parameters

| Parametro | Tipo   | Descrizione              |
|-----------|--------|--------------------------|
| `id`      | `UUID` | Identificativo webhook   |

### Status Codes

| Codice | Descrizione                              |
|--------|------------------------------------------|
| 204    | No Content - Webhook eliminato           |
| 401    | Unauthorized - Token JWT mancante o non valido |
| 404    | Not Found - Webhook non trovato          |

### Esempio

```bash
curl -X DELETE http://localhost:8080/api/v1/webhooks/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <token>"
```

---

## 7. POST /api/v1/webhooks/{id}/test

Invia una richiesta di test al webhook specificato per verificarne la connettivita'. Viene inviato un payload di test all'URL configurato.

### Request

| Proprieta'         | Valore                                    |
|--------------------|-------------------------------------------|
| **URL**            | `POST /api/v1/webhooks/{id}/test`         |
| **Autenticazione** | JWT Bearer Token                          |

### Path Parameters

| Parametro | Tipo   | Descrizione              |
|-----------|--------|--------------------------|
| `id`      | `UUID` | Identificativo webhook   |

### Status Codes

| Codice | Descrizione                                      |
|--------|--------------------------------------------------|
| 200    | OK - Test eseguito con successo                  |
| 401    | Unauthorized - Token JWT mancante o non valido   |
| 404    | Not Found - Webhook non trovato                  |
| 502    | Bad Gateway - URL di destinazione non raggiungibile |

### Esempio

```bash
curl -X POST http://localhost:8080/api/v1/webhooks/550e8400-e29b-41d4-a716-446655440000/test \
  -H "Authorization: Bearer <token>"
```

---

## 8. Modelli di Dati

### WebhookRequestDto

**Package**: `com.localmind.api.webhook.dto`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequestDto {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;

    @NotBlank(message = "URL is required")
    @Size(max = 1000, message = "URL must not exceed 1000 characters")
    private String url;

    @NotBlank(message = "Event type is required")
    private String eventType;

    @Builder.Default
    private boolean active = true;
}
```

### WebhookResponseDto

**Package**: `com.localmind.api.webhook.dto`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponseDto {
    private UUID id;
    private String name;
    private String url;
    private String eventType;
    private boolean active;
}
```

---

## 9. Tipi di Evento

I seguenti tipi di evento sono supportati per i webhook:

| Tipo Evento         | Descrizione                                                  |
|----------------------|--------------------------------------------------------------|
| `NEW_FILE`           | Un nuovo file e' stato rilevato in una cartella monitorata   |
| `DOCUMENT_INDEXED`   | Un documento e' stato indicizzato con successo nel vector store |
| `DOCUMENT_FAILED`    | L'elaborazione di un documento e' fallita                    |
| `CHAT_COMPLETED`     | Una sessione di chat e' stata completata                     |
| `SCHEDULED`          | Evento pianificato (esecuzione periodica)                    |

### Payload della callback

Quando un evento viene attivato, LocalMind invia una richiesta HTTP POST all'URL configurato con il seguente payload:

```json
{
  "webhookId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "DOCUMENT_INDEXED",
  "timestamp": "2026-02-18T14:30:00Z",
  "data": {
    "documentId": "770e8400-e29b-41d4-a716-446655440002",
    "documentName": "report.pdf"
  }
}
```

---

## 10. Codici di Errore

| Codice | Eccezione                         | Causa                                              | Risoluzione                                            |
|--------|-----------------------------------|----------------------------------------------------|---------------------------------------------------------|
| 400    | `MethodArgumentNotValidException` | Campi obbligatori mancanti o non validi            | Verificare name, url, eventType nel body della request |
| 401    | `UnauthorizedException`           | Token JWT mancante, scaduto o non valido           | Effettuare il login e includere il token Bearer        |
| 404    | `ResourceNotFoundException`       | Webhook con l'ID specificato non trovato           | Verificare l'ID del webhook                            |
| 502    | `WebhookDeliveryException`        | URL di destinazione non raggiungibile durante il test | Verificare che l'URL sia corretto e raggiungibile    |
| 500    | `Exception` (generica)            | Errore interno imprevisto                          | Consultare i log applicativi                           |
