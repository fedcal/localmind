# Specifica Funzionale: LLM Gateway

| Campo        | Valore                          |
|--------------|---------------------------------|
| **Documento**| Specifica Funzionale LLM Gateway|
| **Versione** | 0.1.0                           |
| **Data**     | 2026-02-09                      |
| **Progetto** | LocalMind                       |

---

## Indice

1. [Descrizione del Componente](#1-descrizione-del-componente)
2. [Provider Supportati](#2-provider-supportati)
3. [Funzionalita'](#3-funzionalita)
4. [Configurazione](#4-configurazione)
5. [Classi Coinvolte](#5-classi-coinvolte)
6. [Flusso Richiesta Chat](#6-flusso-richiesta-chat)
7. [Modello dei Dati](#7-modello-dei-dati)
8. [Gestione Errori](#8-gestione-errori)

---

## 1. Descrizione del Componente

L'LLM Gateway e' il componente centrale di LocalMind responsabile dell'astrazione dell'accesso a multipli provider LLM (Large Language Model). Esso funge da punto di ingresso unico per tutte le richieste di generazione testo, garantendo che la logica applicativa sia completamente disaccoppiata dal provider specifico utilizzato.

Il gateway implementa il pattern Strategy combinato con Chain of Responsibility per il meccanismo di fallback, consentendo di instradare le richieste al provider ottimale e di gestire automaticamente scenari di indisponibilita'.

---

## 2. Provider Supportati

| Provider     | Tipo    | Protocollo | Modelli Principali                   | Porta Default |
|--------------|---------|------------|--------------------------------------|---------------|
| **Ollama**   | Locale  | HTTP REST  | Llama 3, Mistral, Phi-3, Gemma       | 11434         |
| **OpenAI**   | Cloud   | HTTP REST  | GPT-4o, GPT-4o-mini, GPT-4 Turbo     | -             |
| **Anthropic**| Cloud   | HTTP REST  | Claude 3.5 Sonnet, Claude 3 Opus     | -             |
| **Google**   | Cloud   | HTTP REST  | Gemini 1.5 Pro, Gemini 1.5 Flash     | -             |

Ogni provider e' implementato come adapter dell'interfaccia `LlmClient` (port out), garantendo che l'aggiunta di nuovi provider non richieda modifiche al dominio.

---

## 3. Funzionalita'

### 3.1 Routing Automatico

Il gateway seleziona il provider da utilizzare in base alla configurazione corrente. L'utente puo' specificare esplicitamente un provider nella richiesta oppure lasciare che il sistema utilizzi il provider predefinito configurato.

Ordine di selezione:
1. Provider esplicitamente specificato nella richiesta
2. Provider predefinito configurato (`localmind.llm.default-provider`)
3. Primo provider disponibile nella catena di fallback

### 3.2 Fallback Chain

In caso di indisponibilita' del provider selezionato, il gateway attiva automaticamente la catena di fallback:

```
Ordine predefinito: OLLAMA -> OPENAI -> ANTHROPIC -> GOOGLE
```

Il fallback si attiva quando:
- Il provider non risponde entro il timeout configurato
- Il provider restituisce un errore HTTP 5xx
- Il provider non e' abilitato nella configurazione
- Il provider ha superato il rate limit

La catena e' configurabile dall'utente tramite il parametro `localmind.llm.fallback.order`.

### 3.3 Retry Logic

Ogni chiamata al provider e' protetta da logica di retry con backoff esponenziale:

- **Max tentativi**: 3 (configurabile via `localmind.llm.retry.max-attempts`)
- **Backoff base**: 1000ms (configurabile via `localmind.llm.retry.backoff-ms`)
- **Backoff esponenziale**: tentativo 1 = 1000ms, tentativo 2 = 2000ms, tentativo 3 = 4000ms
- **Errori retryable**: timeout, errori di rete, HTTP 429 (rate limit), HTTP 503 (service unavailable)
- **Errori non retryable**: HTTP 400 (bad request), HTTP 401 (unauthorized), HTTP 403 (forbidden)

### 3.4 Rate Limiting

Il gateway gestisce il rate limiting per ciascun provider:

- Rispetto dei limiti imposti dal provider (HTTP 429)
- Attesa automatica con retry dopo il periodo indicato nell'header `Retry-After`
- Logging delle occorrenze di rate limiting per monitoraggio

### 3.5 Cost Tracking

Il servizio `CostTrackingService` calcola automaticamente il costo di ogni richiesta:

- Calcolo basato su token input e token output
- Pricing configurabile per provider e modello
- Persistenza dei dati di utilizzo tramite `LlmUsageRepository`
- Aggregazione per provider, modello, periodo temporale

### 3.6 Usage Metrics

Per ogni chiamata LLM vengono raccolte le seguenti metriche:

| Metrica            | Tipo   | Descrizione                              |
|--------------------|--------|------------------------------------------|
| `promptTokens`     | int    | Numero di token nel prompt (input)       |
| `completionTokens` | int    | Numero di token nella risposta (output)  |
| `totalTokens`      | int    | Somma prompt + completion                |
| `latencyMs`        | long   | Tempo di risposta in millisecondi        |
| `provider`         | String | Provider utilizzato                      |
| `model`            | String | Modello utilizzato                       |
| `estimatedCost`    | double | Costo stimato in USD                     |

---

## 4. Configurazione

Configurazione di riferimento estratta da `application-dev.yml`:

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

## 5. Classi Coinvolte

### 5.1 Architettura delle Classi

```
Domain Layer (localmind-domain)
+-- model/
|   +-- ChatRequest           # Value Object: richiesta chat
|   +-- ChatResponse          # Value Object: risposta chat
|   +-- LlmProvider (enum)    # OLLAMA, OPENAI, ANTHROPIC, GOOGLE
|   +-- LlmUsage             # Entity: metriche di utilizzo
+-- port/
|   +-- in/
|   |   +-- ChatUseCase       # Port in: interfaccia per il controller
|   +-- out/
|       +-- LlmClient         # Port out: interfaccia per ogni provider
|       +-- LlmUsageRepository # Port out: persistenza usage
+-- service/
    +-- LlmGatewayService     # Domain service: implementa ChatUseCase
    +-- CostTrackingService   # Domain service: calcolo costi

Infrastructure Layer (localmind-infrastructure)
+-- llm/
|   +-- adapter/
|       +-- OllamaLlmAdapter       # Adapter: implementa LlmClient per Ollama
|       +-- OpenAiLlmAdapter       # Adapter: implementa LlmClient per OpenAI
|       +-- AnthropicLlmAdapter    # Adapter: implementa LlmClient per Anthropic
|       +-- GoogleLlmAdapter       # Adapter: implementa LlmClient per Google
+-- persistence/
    +-- entity/
    |   +-- LlmUsageEntity          # JPA entity per llm_usage
    +-- repository/
    |   +-- JpaLlmUsageRepository   # Spring Data JPA repository
    +-- adapter/
        +-- LlmUsagePersistenceAdapter # Adapter: implementa LlmUsageRepository

API Layer (localmind-api)
+-- llm/
    +-- controller/
    |   +-- ChatController          # REST controller: /api/v1/chat
    +-- dto/
        +-- ChatRequestDto          # DTO richiesta
        +-- ChatResponseDto         # DTO risposta
```

### 5.2 Interfacce Chiave

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

## 6. Flusso Richiesta Chat

Il seguente diagramma illustra il flusso completo di una richiesta chat dal controller REST fino alla risposta:

```
Utente          ChatController    ChatUseCase       LlmGatewayService   LlmClient (Ollama)   CostTrackingService
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

### Flusso con Fallback

In caso di errore del provider primario, il flusso si estende con il meccanismo di fallback:

```
LlmGatewayService      LlmClient (Ollama)     LlmClient (OpenAI)
       |                       |                       |
       |  generate(request)    |                       |
       |---------------------->|                       |
       |                       |                       |
       |  ERRORE (timeout)     |                       |
       |<----------------------|                       |
       |                       |                       |
       |  [retry 1 - 1000ms]   |                       |
       |---------------------->|                       |
       |                       |                       |
       |  ERRORE (timeout)     |                       |
       |<----------------------|                       |
       |                       |                       |
       |  [retry 2 - 2000ms]   |                       |
       |---------------------->|                       |
       |                       |                       |
       |  ERRORE (timeout)     |                       |
       |<----------------------|                       |
       |                       |                       |
       |  [max retry raggiunto - fallback]             |
       |                       |                       |
       |  generate(request)    |                       |
       |---------------------------------------------->|
       |                       |                       |
       |  ChatResponse         |                       |
       |<----------------------------------------------|
```

---

## 7. Modello dei Dati

### 7.1 ChatRequest (Value Object)

| Campo           | Tipo          | Obbligatorio | Descrizione                          |
|-----------------|---------------|--------------|--------------------------------------|
| `message`       | String        | Si           | Messaggio dell'utente                |
| `provider`      | LlmProvider   | No           | Provider specifico (override default)|
| `model`         | String        | No           | Modello specifico (override default) |
| `temperature`   | Double        | No           | Temperatura (0.0-2.0, default 0.7)   |
| `maxTokens`     | Integer       | No           | Max token in risposta (default 2048) |
| `systemPrompt`  | String        | No           | System prompt personalizzato         |
| `conversationId`| UUID          | No           | ID conversazione per contesto        |

### 7.2 ChatResponse (Value Object)

| Campo             | Tipo       | Descrizione                              |
|-------------------|------------|------------------------------------------|
| `content`         | String     | Contenuto della risposta                 |
| `provider`        | LlmProvider| Provider che ha generato la risposta     |
| `model`           | String     | Modello utilizzato                       |
| `promptTokens`    | int        | Token nel prompt                         |
| `completionTokens`| int        | Token nella risposta                     |
| `totalTokens`     | int        | Token totali                             |
| `latencyMs`       | long       | Latenza in millisecondi                  |
| `estimatedCost`   | double     | Costo stimato in USD                     |
| `fallbackUsed`    | boolean    | Indica se e' stato usato il fallback     |

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

## 8. Gestione Errori

### 8.1 Errori Gestiti

| Codice | Scenario                      | Azione                                    |
|--------|-------------------------------|-------------------------------------------|
| 408    | Timeout provider              | Retry, poi fallback                       |
| 429    | Rate limit raggiunto          | Attesa Retry-After, poi retry             |
| 500    | Errore interno provider       | Retry, poi fallback                       |
| 503    | Provider non disponibile      | Fallback immediato                        |
| 401    | API key invalida              | Errore, no retry, no fallback             |
| 403    | Accesso negato                | Errore, no retry, no fallback             |

### 8.2 Comportamento in caso di esaurimento fallback

Se tutti i provider nella catena di fallback risultano indisponibili, il gateway restituisce un errore HTTP 503 (Service Unavailable) con un messaggio descrittivo che indica i provider tentati e i relativi errori.
