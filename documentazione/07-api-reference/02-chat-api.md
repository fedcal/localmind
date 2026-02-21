# Chat API

**Progetto:** LocalMind
**Versione:** 1.0.0
**Data:** 2026-02-18
**Base URL:** `http://localhost:8080/api/v1`

---

## Indice

1. [Panoramica](#1-panoramica)
2. [POST /api/v1/chat](#2-post-apiv1chat)
3. [POST /api/v1/chat/stream (SSE Streaming)](#3-post-apiv1chatstream-sse-streaming)
4. [Modelli di Dati](#4-modelli-di-dati)
5. [Flusso di Esecuzione](#5-flusso-di-esecuzione)
6. [Esempi](#6-esempi)
7. [Codici di Errore](#7-codici-di-errore)

---

## 1. Panoramica

L'API Chat consente di inviare messaggi a un modello LLM e ricevere risposte. Supporta la selezione dinamica del provider e del modello, la gestione delle conversazioni e parametri di generazione personalizzabili.

| Proprieta'       | Valore                                |
|------------------|---------------------------------------|
| **Controller**   | `ChatController`                      |
| **Package**      | `com.localmind.api.llm.controller`    |
| **Base path**    | `/api/v1/chat`                        |
| **Content-Type** | `application/json`                    |

| Proprieta'       | Valore                                |
|------------------|---------------------------------------|
| **Controller**   | `StreamingChatController`             |
| **Package**      | `com.localmind.api.llm.controller`    |
| **Base path**    | `/api/v1/chat/stream`                 |
| **Content-Type** | `text/event-stream`                   |

---

## 2. POST /api/v1/chat

Invia un messaggio al modello LLM configurato e restituisce la risposta generata.

### Request

| Proprieta'         | Valore                                |
|--------------------|---------------------------------------|
| **URL**            | `POST /api/v1/chat`                   |
| **Content-Type**   | `application/json`                    |
| **Autenticazione** | Nessuna                               |

### Request Body - ChatRequestDto

```json
{
  "message": "Qual e' il contenuto del documento caricato?",
  "provider": "OLLAMA",
  "model": "llama3.2",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "temperature": 0.7,
  "maxTokens": 2048
}
```

| Campo            | Tipo     | Obbligatorio | Default          | Descrizione                                             |
|------------------|----------|--------------|------------------|---------------------------------------------------------|
| `message`        | `String` | Si           | -                | Messaggio dell'utente (`@NotBlank`)                     |
| `provider`       | `String` | No           | Da config        | Provider LLM: `OLLAMA`, `OPENAI`, `ANTHROPIC`, `GOOGLE` |
| `model`          | `String` | No           | Da config        | Nome del modello specifico                              |
| `conversationId` | `String` | No           | `null`           | UUID della conversazione esistente                      |
| `temperature`    | `double` | No           | `0.7`            | Temperatura di generazione (0.0 - 2.0)                  |
| `maxTokens`      | `int`    | No           | `0` (illimitato) | Numero massimo di token nella risposta                  |

### Validazione

- `message`: annotato con `@NotBlank`, deve essere non nullo e non vuoto. In caso di violazione, viene restituito un errore 400 Bad Request.
- `temperature`: valore di default `0.7` tramite `@Builder.Default`.

### Response Body - ChatResponseDto

```json
{
  "content": "Il documento caricato contiene informazioni riguardanti...",
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

| Campo       | Tipo           | Descrizione                                        |
|-------------|----------------|----------------------------------------------------|
| `content`   | `String`       | Testo della risposta generata                      |
| `model`     | `String`       | Nome del modello che ha generato la risposta       |
| `provider`  | `String`       | Provider utilizzato                                |
| `tokenUsage`| `TokenUsageDto`| Statistiche di utilizzo token (puo' essere `null`) |
| `latencyMs` | `long`         | Latenza della chiamata in millisecondi             |

### TokenUsageDto

| Campo             | Tipo  | Descrizione                              |
|-------------------|-------|------------------------------------------|
| `promptTokens`    | `int` | Numero di token nel prompt               |
| `completionTokens`| `int` | Numero di token nella risposta           |
| `totalTokens`     | `int` | Totale token (prompt + completion)       |

### Status Codes

| Codice | Descrizione                                              |
|--------|----------------------------------------------------------|
| 200    | OK - Risposta generata con successo                      |
| 400    | Bad Request - Messaggio vuoto o parametri non validi     |
| 502    | Bad Gateway - Errore del provider LLM (timeout, servizio non disponibile) |

---

## 3. POST /api/v1/chat/stream (SSE Streaming)

Invia un messaggio al modello LLM e riceve la risposta in streaming tramite Server-Sent Events (SSE). I token vengono inviati progressivamente man mano che il modello li genera.

### Request

| Proprieta' | Valore |
|---|---|
| **URL** | `POST /api/v1/chat/stream` |
| **Content-Type** | `application/json` |
| **Accept** | `text/event-stream` |

### Request Body

Identico a `ChatRequestDto` della sezione 2.

### Sequenza Eventi SSE

La risposta e' un flusso di eventi SSE con i seguenti tipi:

| Evento | Dati | Descrizione |
|---|---|---|
| `conversation` | `{ "conversationId": "..." }` | ID della conversazione (primo evento) |
| `token` | `{ "content": "..." }` | Singolo token generato (ripetuto N volte) |
| `metadata` | `{ "model": "...", "provider": "...", "tokenUsage": {...} }` | Metadati al termine della generazione |
| `done` | `{}` | Segnale di completamento |

### Esempio

```bash
curl -X POST http://localhost:8080/api/v1/chat/stream \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  --no-buffer \
  -d '{"message": "Ciao, come stai?"}'
```

Risposta (stream):
```
event: conversation
data: {"conversationId":"550e8400-e29b-41d4-a716-446655440000"}

event: token
data: {"content":"Ciao"}

event: token
data: {"content":"!"}

event: token
data: {"content":" Come"}

event: token
data: {"content":" posso"}

event: token
data: {"content":" aiutarti"}

event: token
data: {"content":"?"}

event: metadata
data: {"model":"llama3.2","provider":"OLLAMA","tokenUsage":{"promptTokens":12,"completionTokens":6,"totalTokens":18}}

event: done
data: {}
```

### Flusso di Esecuzione

```
Client HTTP
    |
    v
StreamingChatController (POST /api/v1/chat/stream)
    |  Crea SseEmitter (timeout 5 min)
    v
StreamingChatUseCase (port in - domain)
    |  Logica di routing/fallback identica a chat sincrona
    v
StreamingLlmClient adapter (infrastructure)
    |  OllamaStreamingLlmAdapter / OpenAiStreamingLlmAdapter / AnthropicStreamingLlmAdapter
    |  Chiama il provider tramite Spring AI StreamingChatModel
    v
Flux<String> (token stream)
    |  Ogni token inviato come evento SSE
    v
SseEmitter -> Client HTTP (text/event-stream)
```

### Note

- Il tool calling e' **disabilitato** in modalita' streaming
- Il contesto RAG viene preparato **prima** dell'avvio dello stream
- La conversazione viene salvata su DB al completamento dello stream
- Timeout SseEmitter: 5 minuti

---

## 4. Modelli di Dati

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

## 5. Flusso di Esecuzione

```
Client HTTP
    |
    v
ChatController (POST /api/v1/chat)
    |  Converte ChatRequestDto -> LlmRequest (domain model)
    v
ChatUseCase (port in - domain)
    |  Logica di business: selezione provider, gestione conversazione
    v
LlmGatewayService (domain service)
    |  Seleziona il client LLM appropriato
    v
LlmClient adapter (infrastructure)
    |  OllamaLlmAdapter / OpenAiLlmAdapter / AnthropicLlmAdapter
    |  Chiama il provider tramite Spring AI ChatClient
    v
LLM Provider (Ollama / OpenAI / Anthropic)
    |
    v  (risposta)
LlmResponse (domain model)
    |
    v
ChatController
    |  Converte LlmResponse -> ChatResponseDto
    v
Client HTTP (200 OK)
```

### Dettaglio della conversione nel controller

Il controller `ChatController` esegue le seguenti conversioni:

1. **Request**: `ChatRequestDto` -> `LlmRequest` (modello di dominio)
   - Il campo `message` viene incapsulato in un `ChatMessage` con ruolo `USER`.
   - Il campo `provider` viene convertito in `LlmProvider` enum tramite `valueOf()`.
   - I campi opzionali (`model`, `temperature`, `maxTokens`, `conversationId`) vengono passati direttamente.

2. **Response**: `LlmResponse` -> `ChatResponseDto`
   - Il campo `provider` viene convertito in stringa tramite `.name()`.
   - Il campo `tokenUsage` viene mappato a `TokenUsageDto` (con null check).
   - Il campo `latencyMs` viene passato direttamente.

---

## 6. Esempi

### Esempio 1: Chat semplice con Ollama (provider di default)

**Request**:

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Ciao, come funziona LocalMind?"
  }'
```

**Response** (200 OK):

```json
{
  "content": "LocalMind e' una piattaforma AI local-first che ti permette di...",
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

### Esempio 2: Chat con provider specifico

**Request**:

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Riassumi il documento caricato",
    "provider": "OPENAI",
    "model": "gpt-4o",
    "temperature": 0.3,
    "maxTokens": 1024
  }'
```

**Response** (200 OK):

```json
{
  "content": "Il documento tratta dei seguenti argomenti principali...",
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

### Esempio 3: Errore - messaggio vuoto

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

### Esempio 4: Errore - provider non disponibile

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

## 7. Codici di Errore

| Codice | Eccezione                         | Causa                                              | Risoluzione                                                        |
|--------|-----------------------------------|----------------------------------------------------|--------------------------------------------------------------------|
| 400    | `MethodArgumentNotValidException` | Campo `message` vuoto o nullo                      | Fornire un messaggio non vuoto                                     |
| 502    | `LlmProviderException`            | Provider non disponibile, timeout, API key mancante| Verificare la configurazione del provider in `application-dev.yml` |
| 500    | `Exception` (generica)            | Errore interno imprevisto                          | Consultare i log applicativi                                       |
