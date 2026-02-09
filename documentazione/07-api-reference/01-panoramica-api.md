# Panoramica API

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09
**Base URL:** `http://localhost:8080/api/v1`

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

Le API REST di LocalMind espongono le funzionalita' della piattaforma attraverso endpoint HTTP standard. Tutte le API sono prefissate con `/api/v1` per consentire il versionamento futuro.

| Proprieta'         | Valore                                  |
|--------------------|-----------------------------------------|
| **Base URL**       | `http://localhost:8080/api/v1`          |
| **Protocollo**     | HTTP (HTTPS pianificato per produzione) |
| **Porta**          | 8080                                    |
| **Formato dati**   | JSON (`application/json`)               |
| **Encoding**       | UTF-8                                   |
| **Upload file**    | `multipart/form-data`                   |
| **Autenticazione** | Nessuna (v0.1.0)                        |

---

## 2. Convenzioni

### Content-Type

- **Request/Response standard**: `application/json`
- **Upload file**: `multipart/form-data` (endpoint `/documents/upload`)
- **Limite upload**: 50MB (`spring.servlet.multipart.max-file-size=50MB`)

### Identificatori

Tutti gli identificatori di risorse sono di tipo UUID in formato stringa:

```
550e8400-e29b-41d4-a716-446655440000
```

### Timestamp

I timestamp sono in formato ISO 8601 con timezone UTC:

```
2026-02-09T14:30:00Z
```

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

Nella versione 0.1.0, le API di LocalMind **non richiedono autenticazione**. Tutte le rotte sono configurate come `permitAll` in Spring Security.

```java
http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
```

> **Nota**: l'autenticazione (JWT, OAuth2) e' pianificata per versioni successive del progetto. Si consiglia di non esporre le API su reti pubbliche nella configurazione attuale.

---

## 4. Gestione Errori

### GlobalExceptionHandler

La gestione centralizzata degli errori e' implementata tramite `@RestControllerAdvice` nel package `com.localmind.api.common.advice`.

**File**: `localmind-api/src/main/java/com/localmind/api/common/advice/GlobalExceptionHandler.java`

### Mapping eccezioni

| Eccezione                     | Codice HTTP | Descrizione                                    |
|-------------------------------|-------------|------------------------------------------------|
| `ResourceNotFoundException`   | 404         | Risorsa richiesta non trovata                  |
| `LlmProviderException`        | 502         | Errore nella comunicazione con il provider LLM |
| `DocumentProcessingException` | 500         | Errore durante l'elaborazione di un documento  |
| `Exception` (generica)        | 500         | Errore interno non gestito                     |

### Formato ErrorResponseDto

Tutte le risposte di errore seguono il formato standard `ErrorResponseDto`:

**Classe**: `com.localmind.api.common.dto.ErrorResponseDto`

```json
{
  "status": 404,
  "message": "Document not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-09T14:30:00Z",
  "path": "/api/v1/documents/550e8400-e29b-41d4-a716-446655440000"
}
```

| Campo       | Tipo     | Descrizione                                       |
|-------------|----------|---------------------------------------------------|
| `status`    | `int`    | Codice di stato HTTP                              |
| `message`   | `String` | Messaggio descrittivo dell'errore                 |
| `timestamp` | `Instant`| Data/ora dell'errore in formato ISO 8601          |
| `path`      | `String` | Percorso della richiesta che ha generato l'errore |

### Implementazione

```java
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
```

---

## 5. CORS

La configurazione CORS e' gestita tramite Spring Security e consente l'accesso dal frontend Angular in esecuzione locale.

| Proprieta'             | Valore                                    |
|------------------------|-------------------------------------------|
| **Origini consentite** | `http://localhost:4200`                   |
| **Metodi consentiti**  | `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS` |
| **Header consentiti**  | `*` (tutti)                               |
| **Credenziali**        | Non abilitate                             |
             
> **Nota**: in produzione, la configurazione CORS dovra' essere aggiornata per riflettere il dominio effettivo dell'applicazione.

---

## 6. Riepilogo Endpoint

### Chat

| Metodo | Endpoint        | Descrizione                    | Request Body      | Response              |
|--------|-----------------|--------------------------------|-------------------|-----------------------|
| POST   | `/api/v1/chat`  | Invia un messaggio alla chat   | `ChatRequestDto`  | `ChatResponseDto`     |

### Documenti

| Metodo | Endpoint                      | Descrizione                    | Request Body        | Response                  |
|--------|-------------------------------|--------------------------------|---------------------|---------------------------|
| POST   | `/api/v1/documents/upload`    | Carica un documento            | `multipart/form-data`| `DocumentDto`            |
| GET    | `/api/v1/documents`           | Lista tutti i documenti        | -                   | `List<DocumentDto>`       |
| GET    | `/api/v1/documents/{id}`      | Dettaglio documento            | -                   | `DocumentDto`             |
| DELETE | `/api/v1/documents/{id}`      | Elimina un documento           | -                   | 204 No Content            |
| POST   | `/api/v1/documents/search`    | Ricerca semantica nei documenti| `SearchRequestDto`  | `List<SearchResultDto>`   |

### Modelli

| Metodo | Endpoint               | Descrizione                    | Request Body | Response              |
|--------|------------------------|--------------------------------|--------------|-----------------------|
| GET    | `/api/v1/models`       | Lista modelli LLM disponibili  | -            | `List<ModelDto>`      |
| GET    | `/api/v1/models/{id}`  | Dettaglio modello              | -            | `ModelDto`            |

### Dashboard

| Metodo | Endpoint                    | Descrizione                    | Request Body | Response              |
|--------|-----------------------------|--------------------------------|--------------|-----------------------|
| GET    | `/api/v1/dashboard/health`  | Stato di salute dei servizi    | -            | `HealthStatusDto`     |
