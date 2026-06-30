export const content = `# API Overview

**Project:** LocalMind
**Version:** 1.0.0
**Date:** 2026-02-18
**Base URL:** \`http://localhost:8080/api/v1\`

---

## Table of Contents

1. [General Information](#1-general-information)
2. [Conventions](#2-conventions)
3. [Authentication](#3-authentication)
4. [Error Handling](#4-error-handling)
5. [CORS](#5-cors)
6. [Endpoint Summary](#6-endpoint-summary)

---

## 1. General Information

The LocalMind REST APIs expose the platform's functionality through standard HTTP endpoints. All APIs are prefixed with \`/api/v1\` to allow future versioning.

| Property           | Value                                   |
|--------------------|-----------------------------------------|
| **Base URL**       | \`http://localhost:8080/api/v1\`          |
| **Protocol**       | HTTP (HTTPS planned for production)     |
| **Port**           | 8080                                    |
| **Data format**    | JSON (\`application/json\`)               |
| **Encoding**       | UTF-8                                   |
| **File upload**    | \`multipart/form-data\`                   |
| **Authentication** | JWT Bearer Token                        |

---

## 2. Conventions

### Content-Type

- **Standard Request/Response**: \`application/json\`
- **File upload**: \`multipart/form-data\` (endpoint \`/documents/upload\`)
- **Streaming chat SSE**: \`text/event-stream\` (endpoint \`/chat/stream\`)
- **Upload limit**: 50MB (\`spring.servlet.multipart.max-file-size=50MB\`)

### Identifiers

All resource identifiers are UUID type in string format:

\`\`\`
550e8400-e29b-41d4-a716-446655440000
\`\`\`

### Timestamp

Timestamps are in ISO 8601 format with UTC timezone:

\`\`\`
2026-02-09T14:30:00Z
\`\`\`

### HTTP Status Codes

| Code   | Meaning              | Usage                                      |
|--------|----------------------|--------------------------------------------|
| 200    | OK                   | Request completed successfully             |
| 201    | Created              | Resource created successfully (upload)     |
| 204    | No Content           | Deletion completed                         |
| 400    | Bad Request          | Validation failed, missing parameters      |
| 404    | Not Found            | Resource not found                         |
| 500    | Internal Server Error| Internal server error                      |
| 502    | Bad Gateway          | LLM provider error                         |

---

## 3. Authentication

Starting from version 1.0.0, LocalMind APIs use **JWT Bearer Token** for authentication.

### Initial Setup

On first startup, the application requires a password to be configured via the setup endpoint:

\`\`\`bash
curl -X POST http://localhost:8080/api/v1/auth/setup \\
  -H "Content-Type: application/json" \\
  -d '{"password": "your-password"}'
\`\`\`

### Login

To obtain a JWT token:

\`\`\`bash
curl -X POST http://localhost:8080/api/v1/auth/login \\
  -H "Content-Type: application/json" \\
  -d '{"password": "your-password"}'
\`\`\`

**Response**:

\`\`\`json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresAt": "2026-02-19T14:30:00Z"
}
\`\`\`

### Using the Token

Include the JWT token in the \`Authorization\` header of every request:

\`\`\`
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
\`\`\`

### Public Endpoints

The following endpoints **do not require authentication**:

- \`POST /api/v1/auth/login\`
- \`POST /api/v1/auth/setup\`
- \`GET /api/v1/auth/status\`

### Authentication Status Check

To verify if the system is already configured:

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

> **Note**: the JWT token has a configurable expiration. Upon expiration, a new login is required.

---

## 4. Error Handling

### GlobalExceptionHandler

Centralized error handling is implemented via \`@RestControllerAdvice\` in the \`com.localmind.api.common.advice\` package.

**File**: \`localmind-api/src/main/java/com/localmind/api/common/advice/GlobalExceptionHandler.java\`

### Exception Mapping

| Exception                     | HTTP Code   | Description                                    |
|-------------------------------|-------------|------------------------------------------------|
| \`ResourceNotFoundException\`   | 404         | Requested resource not found                   |
| \`LlmProviderException\`        | 502         | Error communicating with the LLM provider      |
| \`DocumentProcessingException\` | 500         | Error during document processing               |
| \`Exception\` (generic)        | 500         | Unhandled internal error                       |

### ErrorResponseDto Format

All error responses follow the standard \`ErrorResponseDto\` format:

**Class**: \`com.localmind.api.common.dto.ErrorResponseDto\`

\`\`\`json
{
  "status": 404,
  "message": "Document not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-02-09T14:30:00Z",
  "path": "/api/v1/documents/550e8400-e29b-41d4-a716-446655440000"
}
\`\`\`

| Field       | Type     | Description                                       |
|-------------|----------|---------------------------------------------------|
| \`status\`    | \`int\`    | HTTP status code                                  |
| \`message\`   | \`String\` | Descriptive error message                         |
| \`timestamp\` | \`Instant\`| Error date/time in ISO 8601 format                |
| \`path\`      | \`String\` | Path of the request that generated the error      |

### Implementation

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

The CORS configuration is managed through Spring Security and allows access from the locally running Angular frontend.

| Property               | Value                                     |
|------------------------|-------------------------------------------|
| **Allowed origins**    | \`http://localhost:4200\`                   |
| **Allowed methods**    | \`GET\`, \`POST\`, \`PUT\`, \`DELETE\`, \`OPTIONS\` |
| **Allowed headers**    | \`*\` (all)                                 |
| **Credentials**        | Not enabled                               |

> **Note**: in production, the CORS configuration will need to be updated to reflect the actual application domain.

---

## 6. Endpoint Summary

### Authentication

| Method | Endpoint                  | Description            | Request Body       | Response          |
|--------|---------------------------|------------------------|--------------------|-------------------|
| POST   | \`/api/v1/auth/login\`      | Login                  | \`LoginRequestDto\`  | \`AuthResponseDto\` |
| POST   | \`/api/v1/auth/setup\`      | Password setup         | \`SetupRequestDto\`  | \`AuthResponseDto\` |
| GET    | \`/api/v1/auth/status\`     | Authentication status  | -                  | \`AuthStatusDto\`   |

### Chat

| Method | Endpoint        | Description                    | Request Body      | Response              |
|--------|-----------------|--------------------------------|-------------------|-----------------------|
| POST   | \`/api/v1/chat\`  | Send a message to the chat     | \`ChatRequestDto\`  | \`ChatResponseDto\`     |

### Chat Streaming

| Method | Endpoint               | Description                    | Request Body      | Response              |
|--------|------------------------|--------------------------------|-------------------|-----------------------|
| POST   | \`/api/v1/chat/stream\`  | Chat with SSE streaming        | \`ChatRequestDto\`  | \`text/event-stream\`   |

### Conversations

| Method | Endpoint                       | Description               | Request Body             | Response                                  |
|--------|--------------------------------|---------------------------|--------------------------|-------------------------------------------|
| GET    | \`/api/v1/conversations\`        | List conversations        | -                        | \`PaginatedResponse<ConversationSummaryDto>\` |
| GET    | \`/api/v1/conversations/{id}\`   | Conversation detail       | -                        | \`ConversationDto\`                         |
| POST   | \`/api/v1/conversations\`        | Create conversation       | \`CreateConversationDto\`  | \`ConversationDto\`                         |
| PUT    | \`/api/v1/conversations/{id}\`   | Update conversation       | \`UpdateConversationDto\`  | \`ConversationDto\`                         |
| DELETE | \`/api/v1/conversations/{id}\`   | Delete conversation       | -                        | 204 No Content                            |

### Documents

| Method | Endpoint                      | Description                       | Request Body        | Response                  |
|--------|-------------------------------|-----------------------------------|---------------------|---------------------------|
| POST   | \`/api/v1/documents/upload\`    | Upload a document                 | \`multipart/form-data\`| \`DocumentDto\`            |
| GET    | \`/api/v1/documents\`           | List all documents                | -                   | \`List<DocumentDto>\`       |
| GET    | \`/api/v1/documents/{id}\`      | Document detail                   | -                   | \`DocumentDto\`             |
| DELETE | \`/api/v1/documents/{id}\`      | Delete a document                 | -                   | 204 No Content            |
| POST   | \`/api/v1/documents/search\`    | Semantic search in documents      | \`SearchRequestDto\`  | \`List<SearchResultDto>\`   |

### Folders

| Method | Endpoint                      | Description            | Request Body      | Response              |
|--------|-------------------------------|------------------------|-------------------|-----------------------|
| GET    | \`/api/v1/folders\`             | List folders           | -                 | \`List<FolderConfigDto>\` |
| POST   | \`/api/v1/folders\`             | Create folder          | \`CreateFolderDto\` | \`FolderConfigDto\`     |
| DELETE | \`/api/v1/folders/{id}\`        | Delete folder          | -                 | 204 No Content        |
| POST   | \`/api/v1/folders/{id}/sync\`   | Trigger sync           | -                 | 200 OK                |

### Settings/Providers

| Method | Endpoint                                       | Description              | Request Body              | Response              |
|--------|-------------------------------------------------|--------------------------|---------------------------|-----------------------|
| GET    | \`/api/v1/settings/providers\`                   | List LLM providers       | -                         | \`List<ProviderConfigDto>\` |
| POST   | \`/api/v1/settings/providers\`                   | Create/update provider   | \`CreateProviderRequestDto\`| \`ProviderConfigDto\`   |
| DELETE | \`/api/v1/settings/providers/{id}\`              | Delete provider          | -                         | 204 No Content        |
| POST   | \`/api/v1/settings/providers/{id}/test\`         | Test connection          | -                         | \`TestResultDto\`       |
| GET    | \`/api/v1/settings/providers/ollama/models\`     | Ollama models            | \`?baseUrl=\`               | \`List<String>\`        |

### Webhooks

| Method | Endpoint                        | Description            | Request Body        | Response              |
|--------|---------------------------------|------------------------|---------------------|-----------------------|
| GET    | \`/api/v1/webhooks\`              | List webhooks          | -                   | \`List<WebhookResponseDto>\` |
| GET    | \`/api/v1/webhooks/{id}\`         | Webhook detail         | -                   | \`WebhookResponseDto\`  |
| POST   | \`/api/v1/webhooks\`              | Create webhook         | \`WebhookRequestDto\` | \`WebhookResponseDto\`  |
| PUT    | \`/api/v1/webhooks/{id}\`         | Update webhook         | \`WebhookRequestDto\` | \`WebhookResponseDto\`  |
| DELETE | \`/api/v1/webhooks/{id}\`         | Delete webhook         | -                   | 204 No Content        |
| POST   | \`/api/v1/webhooks/{id}/test\`    | Test webhook           | -                   | 200 OK                |

### MCP

| Method | Endpoint                      | Description            | Request Body                | Response              |
|--------|-------------------------------|------------------------|-----------------------------|-----------------------|
| POST   | \`/api/v1/mcp/servers\`         | Register MCP server    | \`CreateMcpServerRequestDto\` | \`McpServerDto\`        |
| GET    | \`/api/v1/mcp/servers\`         | List MCP servers       | -                           | \`List<McpServerDto>\`  |
| DELETE | \`/api/v1/mcp/servers/{id}\`    | Remove server          | -                           | 204 No Content        |
| GET    | \`/api/v1/mcp/tools\`           | List MCP tools         | -                           | \`List<McpToolDto>\`    |
| POST   | \`/api/v1/mcp/tools/execute\`   | Execute tool           | \`ExecuteToolDto\`            | \`ToolResultDto\`       |

### Models

| Method | Endpoint               | Description                       | Request Body | Response              |
|--------|------------------------|-----------------------------------|--------------|-----------------------|
| GET    | \`/api/v1/models\`       | List available LLM models         | -            | \`List<ModelDto>\`      |
| GET    | \`/api/v1/models/{id}\`  | Model detail                      | -            | \`ModelDto\`            |

### Dashboard

| Method | Endpoint                    | Description                       | Request Body | Response              |
|--------|-----------------------------|-----------------------------------|--------------|-----------------------|
| GET    | \`/api/v1/dashboard/health\`  | Service health status             | -            | \`HealthStatusDto\`     |
`;
