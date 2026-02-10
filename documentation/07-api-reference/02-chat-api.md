# Chat API

**Project:** LocalMind
**Version:** 0.1.0
**Date:** 2026-02-09
**Base URL:** `http://localhost:8080/api/v1`

---

## Table of Contents

1. [Overview](#1-overview)
2. [POST /api/v1/chat](#2-post-apiv1chat)
3. [Data Models](#3-data-models)
4. [Execution Flow](#4-execution-flow)
5. [Examples](#5-examples)
6. [Error Codes](#6-error-codes)

---

## 1. Overview

The Chat API allows sending messages to an LLM model and receiving responses. It supports dynamic provider and model selection, conversation management, and customizable generation parameters.

| Property         | Value                                 |
|------------------|---------------------------------------|
| **Controller**   | `ChatController`                      |
| **Package**      | `com.localmind.api.llm.controller`    |
| **Base path**    | `/api/v1/chat`                        |
| **Content-Type** | `application/json`                    |

---

## 2. POST /api/v1/chat

Sends a message to the configured LLM model and returns the generated response.

### Request

| Property           | Value                                 |
|--------------------|---------------------------------------|
| **URL**            | `POST /api/v1/chat`                   |
| **Content-Type**   | `application/json`                    |
| **Authentication** | None                                  |

### Request Body - ChatRequestDto

```json
{
  "message": "What is the content of the uploaded document?",
  "provider": "OLLAMA",
  "model": "llama3.2",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "temperature": 0.7,
  "maxTokens": 2048
}
```

| Field            | Type     | Required     | Default          | Description                                             |
|------------------|----------|--------------|------------------|---------------------------------------------------------|
| `message`        | `String` | Yes          | -                | User message (`@NotBlank`)                              |
| `provider`       | `String` | No           | From config      | LLM provider: `OLLAMA`, `OPENAI`, `ANTHROPIC`, `GOOGLE` |
| `model`          | `String` | No           | From config      | Specific model name                                     |
| `conversationId` | `String` | No           | `null`           | UUID of an existing conversation                        |
| `temperature`    | `double` | No           | `0.7`            | Generation temperature (0.0 - 2.0)                      |
| `maxTokens`      | `int`    | No           | `0` (unlimited)  | Maximum number of tokens in the response                |

### Validation

- `message`: annotated with `@NotBlank`, must be non-null and non-empty. If violated, a 400 Bad Request error is returned.
- `temperature`: default value `0.7` via `@Builder.Default`.

### Response Body - ChatResponseDto

```json
{
  "content": "The uploaded document contains information regarding...",
  "model": "llama3.2",
  "provider": "OLLAMA",
  "tokenUsage": {
    "promptTokens": 45,
    "completionTokens": 128,
    "totalTokens": 173
  },
  "latencyMs": 2340
}
```

| Field       | Type           | Description                                        |
|-------------|----------------|----------------------------------------------------|
| `content`   | `String`       | Generated response text                            |
| `model`     | `String`       | Name of the model that generated the response      |
| `provider`  | `String`       | Provider used                                      |
| `tokenUsage`| `TokenUsageDto`| Token usage statistics (can be `null`)             |
| `latencyMs` | `long`         | Call latency in milliseconds                       |

### TokenUsageDto

| Field             | Type  | Description                              |
|-------------------|-------|------------------------------------------|
| `promptTokens`    | `int` | Number of tokens in the prompt           |
| `completionTokens`| `int` | Number of tokens in the response         |
| `totalTokens`     | `int` | Total tokens (prompt + completion)       |

### Status Codes

| Code   | Description                                              |
|--------|----------------------------------------------------------|
| 200    | OK - Response generated successfully                     |
| 400    | Bad Request - Empty message or invalid parameters        |
| 502    | Bad Gateway - LLM provider error (timeout, service unavailable) |

---

## 3. Data Models

### ChatRequestDto

**Package**: `com.localmind.api.llm.dto`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDto {
    @NotBlank(message = "Message is required")
    private String message;
    private String provider;
    private String model;
    private String conversationId;
    @Builder.Default
    private double temperature = 0.7;
    private int maxTokens;
}
```

### ChatResponseDto

**Package**: `com.localmind.api.llm.dto`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {
    private String content;
    private String model;
    private String provider;
    private TokenUsageDto tokenUsage;
    private long latencyMs;
}
```

### TokenUsageDto

**Package**: `com.localmind.api.llm.dto`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUsageDto {
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
}
```

---

## 4. Execution Flow

```
HTTP Client
    |
    v
ChatController (POST /api/v1/chat)
    |  Converts ChatRequestDto -> LlmRequest (domain model)
    v
ChatUseCase (port in - domain)
    |  Business logic: provider selection, conversation management
    v
LlmGatewayService (domain service)
    |  Selects the appropriate LLM client
    v
LlmClient adapter (infrastructure)
    |  OllamaLlmAdapter / OpenAiLlmAdapter / AnthropicLlmAdapter
    |  Calls the provider via Spring AI ChatClient
    v
LLM Provider (Ollama / OpenAI / Anthropic)
    |
    v  (response)
LlmResponse (domain model)
    |
    v
ChatController
    |  Converts LlmResponse -> ChatResponseDto
    v
HTTP Client (200 OK)
```

### Controller conversion detail

The `ChatController` performs the following conversions:

1. **Request**: `ChatRequestDto` -> `LlmRequest` (domain model)
   - The `message` field is encapsulated in a `ChatMessage` with role `USER`.
   - The `provider` field is converted to `LlmProvider` enum via `valueOf()`.
   - Optional fields (`model`, `temperature`, `maxTokens`, `conversationId`) are passed directly.

2. **Response**: `LlmResponse` -> `ChatResponseDto`
   - The `provider` field is converted to string via `.name()`.
   - The `tokenUsage` field is mapped to `TokenUsageDto` (with null check).
   - The `latencyMs` field is passed directly.

---

## 5. Examples

### Example 1: Simple chat with Ollama (default provider)

**Request**:

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello, how does LocalMind work?"
  }'
```

**Response** (200 OK):

```json
{
  "content": "LocalMind is a local-first AI platform that allows you to...",
  "model": "llama3.2",
  "provider": "OLLAMA",
  "tokenUsage": {
    "promptTokens": 12,
    "completionTokens": 89,
    "totalTokens": 101
  },
  "latencyMs": 1850
}
```

### Example 2: Chat with specific provider

**Request**:

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Summarize the uploaded document",
    "provider": "OPENAI",
    "model": "gpt-4o",
    "temperature": 0.3,
    "maxTokens": 1024
  }'
```

**Response** (200 OK):

```json
{
  "content": "The document covers the following main topics...",
  "model": "gpt-4o",
  "provider": "OPENAI",
  "tokenUsage": {
    "promptTokens": 156,
    "completionTokens": 512,
    "totalTokens": 668
  },
  "latencyMs": 3200
}
```

### Example 3: Error - empty message

**Request**:

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": ""
  }'
```

**Response** (400 Bad Request):

```json
{
  "status": 400,
  "message": "Message is required",
  "timestamp": "2026-02-09T14:30:00Z",
  "path": "/api/v1/chat"
}
```

### Example 4: Error - provider unavailable

**Request**:

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Test",
    "provider": "OPENAI"
  }'
```

**Response** (502 Bad Gateway):

```json
{
  "status": 502,
  "message": "LLM provider error: OpenAI API key not configured",
  "timestamp": "2026-02-09T14:30:00Z",
  "path": "/api/v1/chat"
}
```

---

## 6. Error Codes

| Code   | Exception                         | Cause                                              | Resolution                                                         |
|--------|-----------------------------------|----------------------------------------------------|--------------------------------------------------------------------|
| 400    | `MethodArgumentNotValidException` | `message` field empty or null                      | Provide a non-empty message                                        |
| 502    | `LlmProviderException`            | Provider unavailable, timeout, missing API key     | Verify the provider configuration in `application-dev.yml`         |
| 500    | `Exception` (generic)            | Unexpected internal error                          | Check the application logs                                         |
