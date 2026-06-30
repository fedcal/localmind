# Documents API

**Project:** LocalMind
**Version:** 0.1.0
**Date:** 2026-02-09
**Base URL:** `http://localhost:8080/api/v1`

---

## Table of Contents

1. [Overview](#1-overview)
2. [POST /api/v1/documents/upload](#2-post-apiv1documentsupload)
3. [GET /api/v1/documents](#3-get-apiv1documents)
4. [GET /api/v1/documents/{id}](#4-get-apiv1documentsid)
5. [DELETE /api/v1/documents/{id}](#5-delete-apiv1documentsid)
6. [POST /api/v1/documents/search](#6-post-apiv1documentssearch)
7. [Data Models](#7-data-models)

---

## 1. Overview

The Documents API manages the document lifecycle in the LocalMind system: upload, retrieval, deletion, and semantic search. Uploaded documents are processed by the batch pipeline (text extraction, chunking, embedding) and indexed in the vector store for semantic search.

| Property         | Value                                                                  |
|------------------|------------------------------------------------------------------------|
| **Controller**   | `DocumentController`, `DocumentSearchController`                       |
| **Package**      | `com.localmind.api.document.controller`                                |
| **Base path**    | `/api/v1/documents`                                                    |
| **Use cases**    | `DocumentIngestionUseCase`, `DocumentSearchUseCase`, `DocumentService` |

---

## 2. POST /api/v1/documents/upload

Uploads a new document to the system. The file is saved to the filesystem and its metadata is persisted in the database with `PENDING` status.

### Request

| Property         | Value                                 |
|------------------|---------------------------------------|
| **URL**          | `POST /api/v1/documents/upload`       |
| **Content-Type** | `multipart/form-data`                 |
| **Authentication** | None                                |

### Parameters

| Parameter | Type           | Required     | Description                    |
|-----------|----------------|--------------|--------------------------------|
| `file`    | `MultipartFile`| Yes          | File to upload (max 50MB)      |

### Limits

| Parameter                                   | Value  | Description                         |
|---------------------------------------------|--------|-------------------------------------|
| `spring.servlet.multipart.max-file-size`    | 50MB   | Maximum size per single file        |
| `spring.servlet.multipart.max-request-size` | 50MB   | Maximum request size                |

### Supported Formats

| Format  | Extension  | MIME Type                                                                 |
|---------|------------|---------------------------------------------------------------------------|
| PDF     | `.pdf`     | `application/pdf`                                                         |
| DOCX    | `.docx`    | `application/vnd.openxmlformats-officedocument.wordprocessingml.document` |
| TXT     | `.txt`     | `text/plain`                                                              |
| EML     | `.eml`     | `message/rfc822`                                                          |

### Response (200 OK)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "filename": "technical-report.pdf",
  "filePath": "/home/user/.localmind/uploads/550e8400-e29b-41d4-a716-446655440000.pdf",
  "mimeType": "application/pdf",
  "fileSize": 2048576,
  "status": "PENDING",
  "createdAt": "2026-02-09T14:30:00Z",
  "indexedAt": null
}
```

### Status Codes

| Code   | Description                                           |
|--------|-------------------------------------------------------|
| 200    | OK - Document uploaded successfully                   |
| 400    | Bad Request - Missing file or unsupported format      |
| 500    | Internal Server Error - Error during saving           |

### Example

```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
  -F "file=@/path/to/technical-report.pdf"
```

---

## 3. GET /api/v1/documents

Returns the list of all documents in the system.

### Request

| Property           | Value                                 |
|--------------------|---------------------------------------|
| **URL**            | `GET /api/v1/documents`               |
| **Content-Type**   | -                                     |
| **Authentication** | None                                  |

### Response (200 OK)

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "filename": "technical-report.pdf",
    "filePath": "/home/user/.localmind/uploads/550e8400-e29b-41d4-a716-446655440000.pdf",
    "mimeType": "application/pdf",
    "fileSize": 2048576,
    "status": "INDEXED",
    "createdAt": "2026-02-09T14:30:00Z",
    "indexedAt": "2026-02-09T14:31:15Z"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "filename": "notes.txt",
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

| Code   | Description                               |
|--------|-------------------------------------------|
| 200    | OK - List returned (can be empty)         |

### Example

```bash
curl -X GET http://localhost:8080/api/v1/documents
```

---

## 4. GET /api/v1/documents/{id}

Returns the details of a single document identified by its UUID.

### Request

| Property           | Value                                 |
|--------------------|---------------------------------------|
| **URL**            | `GET /api/v1/documents/{id}`          |
| **Content-Type**   | -                                     |
| **Authentication** | None                                  |

### Path Parameters

| Parameter | Type     | Description                    |
|-----------|----------|--------------------------------|
| `id`      | `String` | Document UUID                  |

### Response (200 OK)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "filename": "technical-report.pdf",
  "filePath": "/home/user/.localmind/uploads/550e8400-e29b-41d4-a716-446655440000.pdf",
  "mimeType": "application/pdf",
  "fileSize": 2048576,
  "status": "INDEXED",
  "createdAt": "2026-02-09T14:30:00Z",
  "indexedAt": "2026-02-09T14:31:15Z"
}
```

### Status Codes

| Code   | Description                       |
|--------|-----------------------------------|
| 200    | OK - Document found               |
| 404    | Not Found - Document not found    |

### Error example (404)

```json
{
  "status": 404,
  "message": "Document not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-09T14:30:00Z",
  "path": "/api/v1/documents/550e8400-e29b-41d4-a716-446655440000"
}
```

### Example

```bash
curl -X GET http://localhost:8080/api/v1/documents/550e8400-e29b-41d4-a716-446655440000
```

---

## 5. DELETE /api/v1/documents/{id}

Deletes a document from the system. Removes both the metadata from the database and the file from the filesystem.

### Request

| Property           | Value                                 |
|--------------------|---------------------------------------|
| **URL**            | `DELETE /api/v1/documents/{id}`       |
| **Content-Type**   | -                                     |
| **Authentication** | None                                  |

### Path Parameters

| Parameter | Type     | Description                    |
|-----------|----------|--------------------------------|
| `id`      | `String` | Document UUID                  |

### Response

- **204 No Content**: deletion completed (no body).

### Status Codes

| Code   | Description                       |
|--------|-----------------------------------|
| 204    | No Content - Document deleted     |
| 404    | Not Found - Document not found    |

### Example

```bash
curl -X DELETE http://localhost:8080/api/v1/documents/550e8400-e29b-41d4-a716-446655440000
```

---

## 6. POST /api/v1/documents/search

Performs a semantic search in indexed documents via the vector store (Qdrant). The query is converted into a vector embedding and compared with the embeddings of the document chunks.

### Request

| Property         | Value                                 |
|------------------|---------------------------------------|
| **URL**          | `POST /api/v1/documents/search`       |
| **Content-Type** | `application/json`                    |
| **Controller**   | `DocumentSearchController`            |
| **Authentication** | None                                |

### Request Body - SearchRequestDto

```json
{
  "query": "IT security policies",
  "topK": 5
}
```

| Field  | Type     | Required     | Default | Description                               |
|--------|----------|--------------|---------|-------------------------------------------|
| `query`| `String` | Yes          | -       | Search query text (`@NotBlank`)           |
| `topK` | `int`    | No           | `5`     | Maximum number of results to return       |

### Response (200 OK)

```json
[
  {
    "documentId": "550e8400-e29b-41d4-a716-446655440000",
    "filename": "security-policy.pdf",
    "content": "The organization's IT security policies provide for...",
    "score": 0.92,
    "chunkIndex": 3
  },
  {
    "documentId": "660e8400-e29b-41d4-a716-446655440001",
    "filename": "it-manual.docx",
    "content": "The security chapter describes the procedures for...",
    "score": 0.78,
    "chunkIndex": 12
  }
]
```

### SearchResultDto

| Field       | Type     | Description                              |
|-------------|----------|------------------------------------------|
| `documentId`| `String` | UUID of the source document              |
| `filename`  | `String` | Source file name                         |
| `content`   | `String` | Text of the chunk that produced the match|
| `score`     | `double` | Similarity score (0.0 - 1.0)            |
| `chunkIndex`| `int`    | Chunk index in the original document     |

### Status Codes

| Code   | Description                                             |
|--------|---------------------------------------------------------|
| 200    | OK - Results returned (can be an empty list)            |
| 400    | Bad Request - Empty query                               |

### Example

```bash
curl -X POST http://localhost:8080/api/v1/documents/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "IT security policies",
    "topK": 3
  }'
```

---

## 7. Data Models

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

| Field        | Type      | Description                                          |
|--------------|-----------|------------------------------------------------------|
| `id`         | `String`  | Document UUID                                        |
| `filename`   | `String`  | Original file name                                   |
| `filePath`   | `String`  | Storage path on filesystem                           |
| `mimeType`   | `String`  | File MIME type                                       |
| `fileSize`   | `long`    | Size in bytes                                        |
| `status`     | `String`  | Status: `PENDING`, `PROCESSING`, `INDEXED`, `ERROR`  |
| `createdAt`  | `Instant` | Upload date/time                                     |
| `indexedAt`  | `Instant` | Indexing date/time (null if not indexed)              |

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
