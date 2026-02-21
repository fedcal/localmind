export const content = `# Webhooks API

**Project:** LocalMind
**Version:** 1.0.0
**Date:** 2026-02-18
**Base URL:** \`http://localhost:8080/api/v1\`

---

## Table of Contents

1. [Overview](#1-overview)
2. [GET /api/v1/webhooks](#2-get-apiv1webhooks)
3. [GET /api/v1/webhooks/{id}](#3-get-apiv1webhooksid)
4. [POST /api/v1/webhooks](#4-post-apiv1webhooks)
5. [PUT /api/v1/webhooks/{id}](#5-put-apiv1webhooksid)
6. [DELETE /api/v1/webhooks/{id}](#6-delete-apiv1webhooksid)
7. [POST /api/v1/webhooks/{id}/test](#7-post-apiv1webhooksidtest)
8. [Data Models](#8-data-models)
9. [Event Types](#9-event-types)
10. [Error Codes](#10-error-codes)

---

## 1. Overview

The Webhooks API allows configuring automatic HTTP notifications to external URLs when certain events occur in the LocalMind platform. Webhooks enable integrating LocalMind with external systems by receiving real-time callbacks for events such as new document indexing, chat completion, or processing failure.

| Property         | Value                                 |
|------------------|---------------------------------------|
| **Controller**   | \`WebhookController\`                   |
| **Package**      | \`com.localmind.api.webhook.controller\`|
| **Base path**    | \`/api/v1/webhooks\`                    |
| **Content-Type** | \`application/json\`                    |

---

## 2. GET /api/v1/webhooks

Returns the list of all configured webhooks.

### Request

| Property           | Value                                 |
|--------------------|---------------------------------------|
| **URL**            | \`GET /api/v1/webhooks\`                |
| **Authentication** | JWT Bearer Token                      |

### Response Body

\`\`\`json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Indexing notification",
    "url": "https://example.com/webhook/indexed",
    "eventType": "DOCUMENT_INDEXED",
    "active": true
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "name": "Error notification",
    "url": "https://example.com/webhook/errors",
    "eventType": "DOCUMENT_FAILED",
    "active": true
  }
]
\`\`\`

### Status Codes

| Code   | Description                              |
|--------|------------------------------------------|
| 200    | OK - List returned successfully          |
| 401    | Unauthorized - Missing or invalid JWT token |

### Example

\`\`\`bash
curl -X GET http://localhost:8080/api/v1/webhooks \\
  -H "Authorization: Bearer <token>"
\`\`\`

---

## 3. GET /api/v1/webhooks/{id}

Returns the detail of a single webhook.

### Request

| Property           | Value                                 |
|--------------------|---------------------------------------|
| **URL**            | \`GET /api/v1/webhooks/{id}\`           |
| **Authentication** | JWT Bearer Token                      |

### Path Parameters

| Parameter | Type   | Description          |
|-----------|--------|----------------------|
| \`id\`      | \`UUID\` | Webhook identifier   |

### Response Body

\`\`\`json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Indexing notification",
  "url": "https://example.com/webhook/indexed",
  "eventType": "DOCUMENT_INDEXED",
  "active": true
}
\`\`\`

### Status Codes

| Code   | Description                              |
|--------|------------------------------------------|
| 200    | OK - Webhook returned successfully       |
| 401    | Unauthorized - Missing or invalid JWT token |
| 404    | Not Found - Webhook not found            |

### Example

\`\`\`bash
curl -X GET http://localhost:8080/api/v1/webhooks/550e8400-e29b-41d4-a716-446655440000 \\
  -H "Authorization: Bearer <token>"
\`\`\`

---

## 4. POST /api/v1/webhooks

Creates a new webhook.

### Request

| Property           | Value                                 |
|--------------------|---------------------------------------|
| **URL**            | \`POST /api/v1/webhooks\`               |
| **Content-Type**   | \`application/json\`                    |
| **Authentication** | JWT Bearer Token                      |

### Request Body - WebhookRequestDto

\`\`\`json
{
  "name": "Indexing notification",
  "url": "https://example.com/webhook/indexed",
  "eventType": "DOCUMENT_INDEXED",
  "active": true
}
\`\`\`

| Field       | Type      | Required | Default | Validation                           | Description                        |
|-------------|-----------|----------|---------|--------------------------------------|------------------------------------|
| \`name\`      | \`String\`  | Yes      | -       | \`@NotBlank\`, \`@Size(min=2, max=200)\` | Descriptive name for the webhook   |
| \`url\`       | \`String\`  | Yes      | -       | \`@NotBlank\`, \`@Size(max=1000)\`       | Callback destination URL           |
| \`eventType\` | \`String\`  | Yes      | -       | \`@NotBlank\`                          | Event type that triggers the webhook |
| \`active\`    | \`boolean\` | No       | \`true\`  | -                                    | Webhook activation status          |

### Response Body - WebhookResponseDto

\`\`\`json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Indexing notification",
  "url": "https://example.com/webhook/indexed",
  "eventType": "DOCUMENT_INDEXED",
  "active": true
}
\`\`\`

### Status Codes

| Code   | Description                              |
|--------|------------------------------------------|
| 201    | Created - Webhook created successfully   |
| 400    | Bad Request - Validation failed          |
| 401    | Unauthorized - Missing or invalid JWT token |

### Example

\`\`\`bash
curl -X POST http://localhost:8080/api/v1/webhooks \\
  -H "Content-Type: application/json" \\
  -H "Authorization: Bearer <token>" \\
  -d '{
    "name": "Indexing notification",
    "url": "https://example.com/webhook/indexed",
    "eventType": "DOCUMENT_INDEXED",
    "active": true
  }'
\`\`\`

---

## 5. PUT /api/v1/webhooks/{id}

Updates an existing webhook.

### Request

| Property           | Value                                 |
|--------------------|---------------------------------------|
| **URL**            | \`PUT /api/v1/webhooks/{id}\`           |
| **Content-Type**   | \`application/json\`                    |
| **Authentication** | JWT Bearer Token                      |

### Path Parameters

| Parameter | Type   | Description          |
|-----------|--------|----------------------|
| \`id\`      | \`UUID\` | Webhook identifier   |

### Request Body - WebhookRequestDto

Identical to the creation request body (section 4).

### Response Body - WebhookResponseDto

\`\`\`json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Updated indexing notification",
  "url": "https://example.com/webhook/indexed-v2",
  "eventType": "DOCUMENT_INDEXED",
  "active": true
}
\`\`\`

### Status Codes

| Code   | Description                              |
|--------|------------------------------------------|
| 200    | OK - Webhook updated successfully        |
| 400    | Bad Request - Validation failed          |
| 401    | Unauthorized - Missing or invalid JWT token |
| 404    | Not Found - Webhook not found            |

### Example

\`\`\`bash
curl -X PUT http://localhost:8080/api/v1/webhooks/550e8400-e29b-41d4-a716-446655440000 \\
  -H "Content-Type: application/json" \\
  -H "Authorization: Bearer <token>" \\
  -d '{
    "name": "Updated indexing notification",
    "url": "https://example.com/webhook/indexed-v2",
    "eventType": "DOCUMENT_INDEXED",
    "active": true
  }'
\`\`\`

---

## 6. DELETE /api/v1/webhooks/{id}

Deletes a webhook.

### Request

| Property           | Value                                 |
|--------------------|---------------------------------------|
| **URL**            | \`DELETE /api/v1/webhooks/{id}\`        |
| **Authentication** | JWT Bearer Token                      |

### Path Parameters

| Parameter | Type   | Description          |
|-----------|--------|----------------------|
| \`id\`      | \`UUID\` | Webhook identifier   |

### Status Codes

| Code   | Description                              |
|--------|------------------------------------------|
| 204    | No Content - Webhook deleted             |
| 401    | Unauthorized - Missing or invalid JWT token |
| 404    | Not Found - Webhook not found            |

### Example

\`\`\`bash
curl -X DELETE http://localhost:8080/api/v1/webhooks/550e8400-e29b-41d4-a716-446655440000 \\
  -H "Authorization: Bearer <token>"
\`\`\`

---

## 7. POST /api/v1/webhooks/{id}/test

Sends a test request to the specified webhook to verify connectivity. A test payload is sent to the configured URL.

### Request

| Property           | Value                                     |
|--------------------|-------------------------------------------|
| **URL**            | \`POST /api/v1/webhooks/{id}/test\`         |
| **Authentication** | JWT Bearer Token                          |

### Path Parameters

| Parameter | Type   | Description          |
|-----------|--------|----------------------|
| \`id\`      | \`UUID\` | Webhook identifier   |

### Status Codes

| Code   | Description                                      |
|--------|--------------------------------------------------|
| 200    | OK - Test executed successfully                  |
| 401    | Unauthorized - Missing or invalid JWT token      |
| 404    | Not Found - Webhook not found                    |
| 502    | Bad Gateway - Destination URL unreachable        |

### Example

\`\`\`bash
curl -X POST http://localhost:8080/api/v1/webhooks/550e8400-e29b-41d4-a716-446655440000/test \\
  -H "Authorization: Bearer <token>"
\`\`\`

---

## 8. Data Models

### WebhookRequestDto

**Package**: \`com.localmind.api.webhook.dto\`

\`\`\`java
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
\`\`\`

### WebhookResponseDto

**Package**: \`com.localmind.api.webhook.dto\`

\`\`\`java
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
\`\`\`

---

## 9. Event Types

The following event types are supported for webhooks:

| Event Type           | Description                                                  |
|----------------------|--------------------------------------------------------------|
| \`NEW_FILE\`           | A new file was detected in a monitored folder                |
| \`DOCUMENT_INDEXED\`   | A document was successfully indexed in the vector store      |
| \`DOCUMENT_FAILED\`    | Document processing failed                                   |
| \`CHAT_COMPLETED\`     | A chat session was completed                                 |
| \`SCHEDULED\`          | Scheduled event (periodic execution)                         |

### Callback Payload

When an event is triggered, LocalMind sends an HTTP POST request to the configured URL with the following payload:

\`\`\`json
{
  "webhookId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "DOCUMENT_INDEXED",
  "timestamp": "2026-02-18T14:30:00Z",
  "data": {
    "documentId": "770e8400-e29b-41d4-a716-446655440002",
    "documentName": "report.pdf"
  }
}
\`\`\`

---

## 10. Error Codes

| Code   | Exception                         | Cause                                              | Resolution                                             |
|--------|-----------------------------------|----------------------------------------------------|---------------------------------------------------------|
| 400    | \`MethodArgumentNotValidException\` | Required fields missing or invalid                 | Verify name, url, eventType in the request body        |
| 401    | \`UnauthorizedException\`           | JWT token missing, expired or invalid              | Login and include the Bearer token                     |
| 404    | \`ResourceNotFoundException\`       | Webhook with the specified ID not found            | Verify the webhook ID                                  |
| 502    | \`WebhookDeliveryException\`        | Destination URL unreachable during test            | Verify the URL is correct and reachable                |
| 500    | \`Exception\` (generic)            | Unexpected internal error                          | Check the application logs                             |
`;
