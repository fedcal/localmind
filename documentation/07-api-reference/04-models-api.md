# Models API

**Project:** LocalMind
**Version:** 0.1.0
**Date:** 2026-02-09
**Base URL:** `http://localhost:8080/api/v1`

---

## Table of Contents

1. [Overview](#1-overview)
2. [GET /api/v1/models](#2-get-apiv1models)
3. [GET /api/v1/models/{id}](#3-get-apiv1modelsid)
4. [Data Models](#4-data-models)
5. [Examples](#5-examples)

---

## 1. Overview

The Models API allows querying the LLM models available in the system. Each model is associated with a provider (Ollama, OpenAI, Anthropic, Google) and has properties such as the context window size and availability status.

| Property         | Value                                 |
|------------------|---------------------------------------|
| **Controller**   | `ModelController`                     |
| **Package**      | `com.localmind.api.llm.controller`    |
| **Base path**    | `/api/v1/models`                      |
| **Use case**     | `ModelManagementUseCase`              |
| **Content-Type** | `application/json`                    |

---

## 2. GET /api/v1/models

Returns the list of all LLM models available in the system, regardless of the provider.

### Request

| Property           | Value                                 |
|--------------------|---------------------------------------|
| **URL**            | `GET /api/v1/models`                  |
| **Content-Type**   | -                                     |
| **Authentication** | None                                  |

### Response (200 OK)

```json
[
  {
    "id": "ollama:llama3.2",
    "name": "llama3.2",
    "provider": "OLLAMA",
    "contextWindow": 131072,
    "available": true
  },
  {
    "id": "ollama:nomic-embed-text",
    "name": "nomic-embed-text",
    "provider": "OLLAMA",
    "contextWindow": 8192,
    "available": true
  },
  {
    "id": "openai:gpt-4o",
    "name": "gpt-4o",
    "provider": "OPENAI",
    "contextWindow": 128000,
    "available": false
  },
  {
    "id": "anthropic:claude-sonnet-4-20250514",
    "name": "claude-sonnet-4-20250514",
    "provider": "ANTHROPIC",
    "contextWindow": 200000,
    "available": false
  }
]
```

### Status Codes

| Code   | Description                              |
|--------|------------------------------------------|
| 200    | OK - List returned (can be empty)        |

### Example

```bash
curl -X GET http://localhost:8080/api/v1/models
```

---

## 3. GET /api/v1/models/{id}

Returns the details of a single model identified by its ID.

### Request

| Property           | Value                                 |
|--------------------|---------------------------------------|
| **URL**            | `GET /api/v1/models/{id}`             |
| **Content-Type**   | -                                     |
| **Authentication** | None                                  |

### Path Parameters

| Parameter | Type     | Description                                        |
|-----------|----------|----------------------------------------------------|
| `id`      | `String` | Model identifier (e.g., `ollama:llama3.2`)         |

### Response (200 OK)

```json
{
  "id": "ollama:llama3.2",
  "name": "llama3.2",
  "provider": "OLLAMA",
  "contextWindow": 131072,
  "available": true
}
```

### Status Codes

| Code   | Description                    |
|--------|--------------------------------|
| 200    | OK - Model found               |
| 404    | Not Found - Model not found    |

### Error example (404)

```json
{
  "status": 404,
  "message": "Model not found with id: unknown-model",
  "timestamp": "2026-02-09T14:30:00Z",
  "path": "/api/v1/models/unknown-model"
}
```

### Example

```bash
curl -X GET http://localhost:8080/api/v1/models/ollama:llama3.2
```

---

## 4. Data Models

### ModelDto

**Package**: `com.localmind.api.llm.dto`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelDto {
    private String id;
    private String name;
    private String provider;
    private int contextWindow;
    private boolean available;
}
```

| Field          | Type      | Description                                          |
|----------------|-----------|------------------------------------------------------|
| `id`           | `String`  | Unique identifier (format `provider:name`)           |
| `name`         | `String`  | Model name                                           |
| `provider`     | `String`  | Provider: `OLLAMA`, `OPENAI`, `ANTHROPIC`, `GOOGLE`  |
| `contextWindow`| `int`     | Context window size in tokens                        |
| `available`    | `boolean` | `true` if the model is reachable and usable          |

### Mapping from domain

The controller performs manual conversion from the domain model `LlmModel` to the DTO:

```java
private ModelDto toDto(LlmModel model) {
    return ModelDto.builder()
            .id(model.getId())
            .name(model.getName())
            .provider(model.getProvider().name())
            .contextWindow(model.getContextWindow())
            .available(model.isAvailable())
            .build();
}
```

### Domain model - LlmModel

| Field          | Type          | Description                                     |
|----------------|---------------|-------------------------------------------------|
| `id`           | `String`      | Unique identifier                               |
| `name`         | `String`      | Model name                                      |
| `provider`     | `LlmProvider` | Enum: `OLLAMA`, `OPENAI`, `ANTHROPIC`, `GOOGLE` |
| `contextWindow`| `int`         | Context window size                             |
| `available`    | `boolean`     | Availability status                             |

---

## 5. Examples

### List available models

```bash
curl -X GET http://localhost:8080/api/v1/models
```

### Specific model detail

```bash
curl -X GET http://localhost:8080/api/v1/models/ollama:llama3.2
```

### Check model availability before using it in chat

```bash
# 1. Check that the model is available
curl -s http://localhost:8080/api/v1/models/ollama:llama3.2 | jq '.available'

# 2. If available, send a message
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello!",
    "provider": "OLLAMA",
    "model": "llama3.2"
  }'
```
