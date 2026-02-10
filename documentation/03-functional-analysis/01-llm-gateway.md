# Functional Specification: LLM Gateway

| Field        | Value                              |
|--------------|------------------------------------|
| **Document** | Functional Specification LLM Gateway|
| **Version**  | 0.1.0                              |
| **Date**     | 2026-02-09                         |
| **Project**  | LocalMind                          |

---

## Table of Contents

1. [Component Description](#1-component-description)
2. [Supported Providers](#2-supported-providers)
3. [Features](#3-features)
4. [Configuration](#4-configuration)
5. [Involved Classes](#5-involved-classes)
6. [Chat Request Flow](#6-chat-request-flow)
7. [Data Model](#7-data-model)
8. [Error Handling](#8-error-handling)

---

## 1. Component Description

The LLM Gateway is the central component of LocalMind responsible for abstracting access to multiple LLM (Large Language Model) providers. It serves as a single entry point for all text generation requests, ensuring that application logic is completely decoupled from the specific provider being used.

The gateway implements the Strategy pattern combined with Chain of Responsibility for the fallback mechanism, allowing requests to be routed to the optimal provider and automatically handling unavailability scenarios.

---

## 2. Supported Providers

| Provider     | Type    | Protocol   | Main Models                          | Default Port  |
|--------------|---------|------------|--------------------------------------|---------------|
| **Ollama**   | Local   | HTTP REST  | Llama 3, Mistral, Phi-3, Gemma       | 11434         |
| **OpenAI**   | Cloud   | HTTP REST  | GPT-4o, GPT-4o-mini, GPT-4 Turbo     | -             |
| **Anthropic**| Cloud   | HTTP REST  | Claude 3.5 Sonnet, Claude 3 Opus     | -             |
| **Google**   | Cloud   | HTTP REST  | Gemini 1.5 Pro, Gemini 1.5 Flash     | -             |

Each provider is implemented as an adapter of the `LlmClient` interface (port out), ensuring that adding new providers does not require changes to the domain.

---

## 3. Features

### 3.1 Automatic Routing

The gateway selects the provider to use based on the current configuration. The user can explicitly specify a provider in the request or let the system use the configured default provider.

Selection order:
1. Provider explicitly specified in the request
2. Configured default provider (`localmind.llm.default-provider`)
3. First available provider in the fallback chain

### 3.2 Fallback Chain

In case of unavailability of the selected provider, the gateway automatically activates the fallback chain:

```
Default order: OLLAMA -> OPENAI -> ANTHROPIC -> GOOGLE
```

Fallback is triggered when:
- The provider does not respond within the configured timeout
- The provider returns an HTTP 5xx error
- The provider is not enabled in the configuration
- The provider has exceeded its rate limit

The chain is configurable by the user through the `localmind.llm.fallback.order` parameter.

### 3.3 Retry Logic

Each call to a provider is protected by retry logic with exponential backoff:

- **Max attempts**: 3 (configurable via `localmind.llm.retry.max-attempts`)
- **Base backoff**: 1000ms (configurable via `localmind.llm.retry.backoff-ms`)
- **Exponential backoff**: attempt 1 = 1000ms, attempt 2 = 2000ms, attempt 3 = 4000ms
- **Retryable errors**: timeout, network errors, HTTP 429 (rate limit), HTTP 503 (service unavailable)
- **Non-retryable errors**: HTTP 400 (bad request), HTTP 401 (unauthorized), HTTP 403 (forbidden)

### 3.4 Rate Limiting

The gateway manages rate limiting for each provider:

- Compliance with limits imposed by the provider (HTTP 429)
- Automatic wait with retry after the period indicated in the `Retry-After` header
- Logging of rate limiting occurrences for monitoring

### 3.5 Cost Tracking

The `CostTrackingService` automatically calculates the cost of each request:

- Calculation based on input tokens and output tokens
- Configurable pricing per provider and model
- Persistence of usage data through `LlmUsageRepository`
- Aggregation by provider, model, time period

### 3.6 Usage Metrics

The following metrics are collected for each LLM call:

| Metric             | Type   | Description                              |
|--------------------|--------|------------------------------------------|
| `promptTokens`     | int    | Number of tokens in the prompt (input)   |
| `completionTokens` | int    | Number of tokens in the response (output)|
| `totalTokens`      | int    | Sum of prompt + completion               |
| `latencyMs`        | long   | Response time in milliseconds            |
| `provider`         | String | Provider used                            |
| `model`            | String | Model used                               |
| `estimatedCost`    | double | Estimated cost in USD                    |

---

## 4. Configuration

Reference configuration extracted from `application-dev.yml`:

```yaml
localmind:
  llm:
    # Provider predefinito
    default-provider: OLLAMA

    # Configurazione Ollama
    ollama:
      enabled: true
      base-url: http://localhost:11434
      model: llama3.2
      embedding-model: nomic-embed-text
      timeout: 120s

    # Configurazione OpenAI
    openai:
      enabled: false
      api-key: ${OPENAI_API_KEY:}
      model: gpt-4o-mini
      timeout: 60s

    # Configurazione Anthropic
    anthropic:
      enabled: false
      api-key: ${ANTHROPIC_API_KEY:}
      model: claude-3-5-sonnet-20241022
      timeout: 60s

    # Configurazione Google
    google:
      enabled: false
      api-key: ${GOOGLE_API_KEY:}
      model: gemini-1.5-flash
      timeout: 60s

    # Retry
    retry:
      max-attempts: 3
      backoff-ms: 1000

    # Fallback
    fallback:
      enabled: true
      order: OLLAMA,OPENAI,ANTHROPIC,GOOGLE
```

---

## 5. Involved Classes

### 5.1 Class Architecture

```
Domain Layer (localmind-domain)
+-- model/
|   +-- ChatRequest           # Value Object: chat request
|   +-- ChatResponse          # Value Object: chat response
|   +-- LlmProvider (enum)    # OLLAMA, OPENAI, ANTHROPIC, GOOGLE
|   +-- LlmUsage             # Entity: usage metrics
+-- port/
|   +-- in/
|   |   +-- ChatUseCase       # Port in: interface for the controller
|   +-- out/
|       +-- LlmClient         # Port out: interface for each provider
|       +-- LlmUsageRepository # Port out: usage persistence
+-- service/
    +-- LlmGatewayService     # Domain service: implements ChatUseCase
    +-- CostTrackingService   # Domain service: cost calculation

Infrastructure Layer (localmind-infrastructure)
+-- llm/
|   +-- adapter/
|       +-- OllamaLlmAdapter       # Adapter: implements LlmClient for Ollama
|       +-- OpenAiLlmAdapter       # Adapter: implements LlmClient for OpenAI
|       +-- AnthropicLlmAdapter    # Adapter: implements LlmClient for Anthropic
|       +-- GoogleLlmAdapter       # Adapter: implements LlmClient for Google
+-- persistence/
    +-- entity/
    |   +-- LlmUsageEntity          # JPA entity for llm_usage
    +-- repository/
    |   +-- JpaLlmUsageRepository   # Spring Data JPA repository
    +-- adapter/
        +-- LlmUsagePersistenceAdapter # Adapter: implements LlmUsageRepository

API Layer (localmind-api)
+-- llm/
    +-- controller/
    |   +-- ChatController          # REST controller: /api/v1/chat
    +-- dto/
        +-- ChatRequestDto          # Request DTO
        +-- ChatResponseDto         # Response DTO
```

### 5.2 Key Interfaces

**ChatUseCase** (port in):
```java
public interface ChatUseCase {
    ChatResponse chat(ChatRequest request);
}
```

**LlmClient** (port out):
```java
public interface LlmClient {
    ChatResponse generate(ChatRequest request);
    LlmProvider getProvider();
    boolean isAvailable();
}
```

**LlmUsageRepository** (port out):
```java
public interface LlmUsageRepository {
    void save(LlmUsage usage);
    List<LlmUsage> findByProviderAndPeriod(LlmProvider provider, LocalDate from, LocalDate to);
}
```

---

## 6. Chat Request Flow

The following diagram illustrates the complete flow of a chat request from the REST controller to the response:

```
User            ChatController    ChatUseCase       LlmGatewayService   LlmClient (Ollama)   CostTrackingService
  |                  |                |                    |                    |                    |
  |  POST /chat      |                |                    |                    |                    |
  |----------------->|                |                    |                    |                    |
  |                  |  chat(request) |                    |                    |                    |
  |                  |--------------->|                    |                    |                    |
  |                  |                |  chat(request)     |                    |                    |
  |                  |                |------------------->|                    |                    |
  |                  |                |                    |                    |                    |
  |                  |                |                    |  selectProvider()  |                    |
  |                  |                |                    |------+             |                    |
  |                  |                |                    |      |             |                    |
  |                  |                |                    |<-----+             |                    |
  |                  |                |                    |                    |                    |
  |                  |                |                    |  generate(request) |                    |
  |                  |                |                    |------------------->|                    |
  |                  |                |                    |                    |                    |
  |                  |                |                    |                    | call Ollama API    |
  |                  |                |                    |                    |------+             |
  |                  |                |                    |                    |      |             |
  |                  |                |                    |                    |<-----+             |
  |                  |                |                    |                    |                    |
  |                  |                |                    |  ChatResponse      |                    |
  |                  |                |                    |<-------------------|                    |
  |                  |                |                    |                    |                    |
  |                  |                |                    |  trackCost(usage)  |                    |
  |                  |                |                    |------------------------------------------->|
  |                  |                |                    |                    |                    |
  |                  |                |                    |                    |              save(usage)
  |                  |                |                    |                    |                    |
  |                  |                |  ChatResponse      |                    |                    |
  |                  |                |<-------------------|                    |                    |
  |                  |  ChatResponse  |                    |                    |                    |
  |                  |<---------------|                    |                    |                    |
  |  JSON response   |                |                    |                    |                    |
  |<-----------------|                |                    |                    |                    |
```

### Fallback Flow

In case of a primary provider error, the flow extends with the fallback mechanism:

```
LlmGatewayService      LlmClient (Ollama)     LlmClient (OpenAI)
       |                       |                       |
       |  generate(request)    |                       |
       |---------------------->|                       |
       |                       |                       |
       |  ERROR (timeout)      |                       |
       |<----------------------|                       |
       |                       |                       |
       |  [retry 1 - 1000ms]   |                       |
       |---------------------->|                       |
       |                       |                       |
       |  ERROR (timeout)      |                       |
       |<----------------------|                       |
       |                       |                       |
       |  [retry 2 - 2000ms]   |                       |
       |---------------------->|                       |
       |                       |                       |
       |  ERROR (timeout)      |                       |
       |<----------------------|                       |
       |                       |                       |
       |  [max retry reached - fallback]               |
       |                       |                       |
       |  generate(request)    |                       |
       |---------------------------------------------->|
       |                       |                       |
       |  ChatResponse         |                       |
       |<----------------------------------------------|
```

---

## 7. Data Model

### 7.1 ChatRequest (Value Object)

| Field           | Type          | Required | Description                              |
|-----------------|---------------|----------|------------------------------------------|
| `message`       | String        | Yes      | User message                             |
| `provider`      | LlmProvider   | No       | Specific provider (override default)     |
| `model`         | String        | No       | Specific model (override default)        |
| `temperature`   | Double        | No       | Temperature (0.0-2.0, default 0.7)       |
| `maxTokens`     | Integer       | No       | Max tokens in response (default 2048)    |
| `systemPrompt`  | String        | No       | Custom system prompt                     |
| `conversationId`| UUID          | No       | Conversation ID for context              |

### 7.2 ChatResponse (Value Object)

| Field             | Type       | Description                              |
|-------------------|------------|------------------------------------------|
| `content`         | String     | Response content                         |
| `provider`        | LlmProvider| Provider that generated the response     |
| `model`           | String     | Model used                               |
| `promptTokens`    | int        | Tokens in the prompt                     |
| `completionTokens`| int        | Tokens in the response                   |
| `totalTokens`     | int        | Total tokens                             |
| `latencyMs`       | long       | Latency in milliseconds                  |
| `estimatedCost`   | double     | Estimated cost in USD                    |
| `fallbackUsed`    | boolean    | Indicates whether the fallback was used  |

### 7.3 LlmProvider (Enum)

```java
public enum LlmProvider {
    OLLAMA,
    OPENAI,
    ANTHROPIC,
    GOOGLE
}
```

---

## 8. Error Handling

### 8.1 Handled Errors

| Code   | Scenario                      | Action                                    |
|--------|-------------------------------|-------------------------------------------|
| 408    | Provider timeout              | Retry, then fallback                      |
| 429    | Rate limit reached            | Wait for Retry-After, then retry          |
| 500    | Provider internal error       | Retry, then fallback                      |
| 503    | Provider unavailable          | Immediate fallback                        |
| 401    | Invalid API key               | Error, no retry, no fallback              |
| 403    | Access denied                 | Error, no retry, no fallback              |

### 8.2 Behavior When Fallback Is Exhausted

If all providers in the fallback chain are unavailable, the gateway returns an HTTP 503 (Service Unavailable) error with a descriptive message indicating the providers attempted and their respective errors.
