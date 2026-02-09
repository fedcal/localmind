# Documents API

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09
**Base URL:** `http://localhost:8080/api/v1`

---

## Indice

1. [Panoramica](#1-panoramica)
2. [POST /api/v1/documents/upload](#2-post-apiv1documentsupload)
3. [GET /api/v1/documents](#3-get-apiv1documents)
4. [GET /api/v1/documents/{id}](#4-get-apiv1documentsid)
5. [DELETE /api/v1/documents/{id}](#5-delete-apiv1documentsid)
6. [POST /api/v1/documents/search](#6-post-apiv1documentssearch)
7. [Modelli di Dati](#7-modelli-di-dati)

---

## 1. Panoramica

L'API Documents gestisce il ciclo di vita dei documenti nel sistema LocalMind: caricamento, consultazione, eliminazione e ricerca semantica. I documenti caricati vengono elaborati dalla pipeline batch (estrazione testo, chunking, embedding) e indicizzati nel vector store per la ricerca semantica.

| Proprieta'       | Valore                                        |
|------------------|-----------------------------------------------|
| **Controller**   | `DocumentController`, `DocumentSearchController` |
| **Package**      | `com.localmind.api.document.controller`       |
| **Base path**    | `/api/v1/documents`                           |
| **Use cases**    | `DocumentIngestionUseCase`, `DocumentSearchUseCase`, `DocumentService` |

---

## 2. POST /api/v1/documents/upload

Carica un nuovo documento nel sistema. Il file viene salvato su filesystem e i relativi metadati vengono persistiti nel database con stato `PENDING`.

### Request

| Proprieta'       | Valore                                |
|------------------|---------------------------------------|
| **URL**          | `POST /api/v1/documents/upload`      |
| **Content-Type** | `multipart/form-data`                |
| **Autenticazione** | Nessuna                             |

### Parametri

| Parametro | Tipo           | Obbligatorio | Descrizione                    |
|-----------|----------------|-------------|--------------------------------|
| `file`    | `MultipartFile`| Si          | File da caricare (max 50MB)   |

### Limiti

| Parametro                        | Valore | Descrizione                    |
|----------------------------------|--------|--------------------------------|
| `spring.servlet.multipart.max-file-size` | 50MB   | Dimensione massima per singolo file |
| `spring.servlet.multipart.max-request-size` | 50MB   | Dimensione massima della richiesta |

### Formati supportati

| Formato | Estensione | MIME Type                                                     |
|---------|-----------|---------------------------------------------------------------|
| PDF     | `.pdf`    | `application/pdf`                                             |
| DOCX    | `.docx`   | `application/vnd.openxmlformats-officedocument.wordprocessingml.document` |
| TXT     | `.txt`    | `text/plain`                                                  |
| EML     | `.eml`    | `message/rfc822`                                              |

### Response (200 OK)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "filename": "relazione-tecnica.pdf",
  "filePath": "/home/user/.localmind/uploads/550e8400-e29b-41d4-a716-446655440000.pdf",
  "mimeType": "application/pdf",
  "fileSize": 2048576,
  "status": "PENDING",
  "createdAt": "2026-02-09T14:30:00Z",
  "indexedAt": null
}
```

### Status Codes

| Codice | Descrizione                                    |
|--------|------------------------------------------------|
| 200    | OK - Documento caricato con successo           |
| 400    | Bad Request - File mancante o formato non supportato |
| 500    | Internal Server Error - Errore durante il salvataggio |

### Esempio

```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@/path/to/relazione-tecnica.pdf"
```

---

## 3. GET /api/v1/documents

Restituisce la lista di tutti i documenti presenti nel sistema.

### Request

| Proprieta'       | Valore                                |
|------------------|---------------------------------------|
| **URL**          | `GET /api/v1/documents`              |
| **Content-Type** | -                                    |
| **Autenticazione** | Nessuna                             |

### Response (200 OK)

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "filename": "relazione-tecnica.pdf",
    "filePath": "/home/user/.localmind/uploads/550e8400-e29b-41d4-a716-446655440000.pdf",
    "mimeType": "application/pdf",
    "fileSize": 2048576,
    "status": "INDEXED",
    "createdAt": "2026-02-09T14:30:00Z",
    "indexedAt": "2026-02-09T14:31:15Z"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "filename": "appunti.txt",
    "filePath": "/home/user/.localmind/uploads/660e8400-e29b-41d4-a716-446655440001.txt",
    "mimeType": "text/plain",
    "fileSize": 4096,
    "status": "PENDING",
    "createdAt": "2026-02-09T15:00:00Z",
    "indexedAt": null
  }
]
```

### Status Codes

| Codice | Descrizione                    |
|--------|--------------------------------|
| 200    | OK - Lista restituita (puo' essere vuota) |

### Esempio

```bash
curl -X GET http://localhost:8080/api/v1/documents
```

---

## 4. GET /api/v1/documents/{id}

Restituisce i dettagli di un singolo documento identificato dal suo UUID.

### Request

| Proprieta'       | Valore                                |
|------------------|---------------------------------------|
| **URL**          | `GET /api/v1/documents/{id}`         |
| **Content-Type** | -                                    |
| **Autenticazione** | Nessuna                             |

### Path Parameters

| Parametro | Tipo     | Descrizione                    |
|-----------|----------|--------------------------------|
| `id`      | `String` | UUID del documento             |

### Response (200 OK)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "filename": "relazione-tecnica.pdf",
  "filePath": "/home/user/.localmind/uploads/550e8400-e29b-41d4-a716-446655440000.pdf",
  "mimeType": "application/pdf",
  "fileSize": 2048576,
  "status": "INDEXED",
  "createdAt": "2026-02-09T14:30:00Z",
  "indexedAt": "2026-02-09T14:31:15Z"
}
```

### Status Codes

| Codice | Descrizione                    |
|--------|--------------------------------|
| 200    | OK - Documento trovato         |
| 404    | Not Found - Documento non trovato |

### Esempio di errore (404)

```json
{
  "status": 404,
  "message": "Document not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-09T14:30:00Z",
  "path": "/api/v1/documents/550e8400-e29b-41d4-a716-446655440000"
}
```

### Esempio

```bash
curl -X GET http://localhost:8080/api/v1/documents/550e8400-e29b-41d4-a716-446655440000
```

---

## 5. DELETE /api/v1/documents/{id}

Elimina un documento dal sistema. Rimuove sia i metadati dal database sia il file dal filesystem.

### Request

| Proprieta'       | Valore                                |
|------------------|---------------------------------------|
| **URL**          | `DELETE /api/v1/documents/{id}`      |
| **Content-Type** | -                                    |
| **Autenticazione** | Nessuna                             |

### Path Parameters

| Parametro | Tipo     | Descrizione                    |
|-----------|----------|--------------------------------|
| `id`      | `String` | UUID del documento             |

### Response

- **204 No Content**: eliminazione completata (nessun body).

### Status Codes

| Codice | Descrizione                    |
|--------|--------------------------------|
| 204    | No Content - Documento eliminato |
| 404    | Not Found - Documento non trovato |

### Esempio

```bash
curl -X DELETE http://localhost:8080/api/v1/documents/550e8400-e29b-41d4-a716-446655440000
```

---

## 6. POST /api/v1/documents/search

Esegue una ricerca semantica nei documenti indicizzati tramite il vector store (Qdrant). La query viene convertita in un embedding vettoriale e confrontata con gli embedding dei chunk documentali.

### Request

| Proprieta'       | Valore                                |
|------------------|---------------------------------------|
| **URL**          | `POST /api/v1/documents/search`      |
| **Content-Type** | `application/json`                   |
| **Controller**   | `DocumentSearchController`           |
| **Autenticazione** | Nessuna                             |

### Request Body - SearchRequestDto

```json
{
  "query": "politiche di sicurezza informatica",
  "topK": 5
}
```

| Campo  | Tipo     | Obbligatorio | Default | Descrizione                              |
|--------|----------|-------------|---------|------------------------------------------|
| `query`| `String` | Si          | -       | Testo della query di ricerca (`@NotBlank`)|
| `topK` | `int`    | No          | `5`     | Numero massimo di risultati da restituire |

### Response (200 OK)

```json
[
  {
    "documentId": "550e8400-e29b-41d4-a716-446655440000",
    "filename": "policy-sicurezza.pdf",
    "content": "Le politiche di sicurezza informatica dell'organizzazione prevedono...",
    "score": 0.92,
    "chunkIndex": 3
  },
  {
    "documentId": "660e8400-e29b-41d4-a716-446655440001",
    "filename": "manuale-it.docx",
    "content": "Il capitolo sulla sicurezza descrive le procedure per...",
    "score": 0.78,
    "chunkIndex": 12
  }
]
```

### SearchResultDto

| Campo        | Tipo     | Descrizione                              |
|-------------|----------|------------------------------------------|
| `documentId`| `String` | UUID del documento sorgente              |
| `filename`  | `String` | Nome del file sorgente                   |
| `content`   | `String` | Testo del chunk che ha prodotto il match |
| `score`     | `double` | Punteggio di similarita' (0.0 - 1.0)    |
| `chunkIndex`| `int`    | Indice del chunk nel documento originale |

### Status Codes

| Codice | Descrizione                                    |
|--------|------------------------------------------------|
| 200    | OK - Risultati restituiti (puo' essere una lista vuota) |
| 400    | Bad Request - Query vuota                      |

### Esempio

```bash
curl -X POST http://localhost:8080/api/v1/documents/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "politiche di sicurezza informatica",
    "topK": 3
  }'
```

---

## 7. Modelli di Dati

### DocumentDto

**Package**: `com.localmind.api.document.dto`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private String id;
    private String filename;
    private String filePath;
    private String mimeType;
    private long fileSize;
    private String status;
    private Instant createdAt;
    private Instant indexedAt;
}
```

| Campo       | Tipo      | Descrizione                              |
|-------------|-----------|------------------------------------------|
| `id`        | `String`  | UUID del documento                       |
| `filename`  | `String`  | Nome originale del file                  |
| `filePath`  | `String`  | Percorso di storage su filesystem        |
| `mimeType`  | `String`  | Tipo MIME del file                       |
| `fileSize`  | `long`    | Dimensione in byte                       |
| `status`    | `String`  | Stato: `PENDING`, `PROCESSING`, `INDEXED`, `ERROR` |
| `createdAt` | `Instant` | Data/ora di caricamento                  |
| `indexedAt`  | `Instant` | Data/ora di indicizzazione (null se non indicizzato) |

### SearchRequestDto

**Package**: `com.localmind.api.document.dto`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDto {
    @NotBlank(message = "Query is required")
    private String query;
    @Builder.Default
    private int topK = 5;
}
```

### SearchResultDto

**Package**: `com.localmind.api.document.dto`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {
    private String documentId;
    private String filename;
    private String content;
    private double score;
    private int chunkIndex;
}
```
