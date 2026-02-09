# Implementazione MCP Server in LocalMind

**Progetto:** LocalMind - Piattaforma AI Local-First  
**Versione:** 0.1.0  
**Ultimo aggiornamento:** 2026-02-09  
**Modulo di riferimento:** localmind-infrastructure (`infrastructure.mcp.server`)

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Architettura del server MCP](#2-architettura-del-server-mcp)
3. [Tool esposti](#3-tool-esposti)
4. [Risorse esposte](#4-risorse-esposte)
5. [Prompt template](#5-prompt-template)
6. [Implementazione con Spring AI](#6-implementazione-con-spring-ai)
7. [Delegazione ai domain use case](#7-delegazione-ai-domain-use-case)
8. [Mappa dei file sorgente](#8-mappa-dei-file-sorgente)

---

## 1. Panoramica

LocalMind agisce come **MCP Server**, esponendo la propria knowledge base RAG e il gateway
multi-provider LLM attraverso il protocollo MCP. Questo consente a qualsiasi MCP client
compatibile (Claude Desktop, altri agenti AI, IDE con supporto MCP) di utilizzare le capacita'
di LocalMind come strumenti esterni.

```
+-----------------------------------------------------+
|                   LocalMind Backend                  |
|                                                      |
|  +--------------------+    +-----------------------+ |
|  | LocalMindMcpTools  |--->| DocumentSearchUseCase | |
|  |   @Tool methods    |--->| ChatUseCase           | |
|  +--------------------+    +-----------------------+ |
|  +--------------------+         Domain Layer         |
|  | LocalMindMcpRes.   |                             |
|  +--------------------+                             |
|  +--------------------+                             |
|  | LocalMindMcpPrompts|                             |
|  +--------------------+                             |
|         |                                            |
|         v                                            |
|  Spring AI MCP Server WebMVC                         |
|  (spring-ai-starter-mcp-server-webmvc)               |
+-----------------------------------------------------+
         |
         | HTTP/SSE (JSON-RPC 2.0)
         v
  MCP Client esterno (Claude Desktop, IDE, etc.)
```

Il server MCP e' abilitato dalla property `localmind.mcp.server.enabled=true` (default: `true`)
e si configura in `application-dev.yml`:

```yaml
spring:
  ai:
    mcp:
      server:
        name: localmind
        version: 0.1.0
```

---

## 2. Architettura del server MCP

Il server MCP di LocalMind e' composto da tre classi principali nel package
`com.localmind.infrastructure.mcp.server`:

| Classe                   | Responsabilita'                              | Primitiva MCP |
|--------------------------|----------------------------------------------|---------------|
| `LocalMindMcpTools`      | Espone tool invocabili (ricerca, chat, modelli)| Tool          |
| `LocalMindMcpResources`  | Espone risorse leggibili (documenti, config)  | Resource      |
| `LocalMindMcpPrompts`    | Fornisce template di prompt parametrizzati    | Prompt        |

Tutte le classi sono annotate con:
- `@Component` - registrazione automatica nel contesto Spring
- `@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)`

Il flag `matchIfMissing = true` garantisce che il server MCP sia attivo di default.

---

## 3. Tool esposti

LocalMind espone tre tool MCP attraverso `LocalMindMcpTools.java`:

### 3.1 `document_search(query, topK)`

**Descrizione:** Ricerca documenti nella knowledge base LocalMind usando RAG (Retrieval-Augmented
Generation). Restituisce chunk di documenti rilevanti con punteggi di similarita'.

**Parametri:**

| Parametro | Tipo    | Obbligatorio | Default | Descrizione                       |
|-----------|---------|--------------|---------|-----------------------------------|
| `query`   | String  | Si'          | -       | Testo della query di ricerca      |
| `topK`    | int     | No           | 5       | Numero massimo di risultati       |

**Risposta:** Lista di oggetti con `documentId`, `filename`, `content`, `score`, `chunkIndex`.

**Implementazione:**

```java
@Tool(description = "Search documents in the LocalMind knowledge base using RAG " +
      "(Retrieval-Augmented Generation). Returns relevant document chunks with " +
      "similarity scores.")
public List<Map<String, Object>> documentSearch(
        @ToolParam(description = "The search query text") String query,
        @ToolParam(description = "Number of top results to return (default 5)") int topK) {
    if (topK <= 0) topK = 5;
    List<SearchResult> results = documentSearchUseCase.search(query, topK);
    return results.stream()
            .map(r -> {
                Map<String, Object> map = new HashMap<>();
                map.put("documentId", r.getDocumentId());
                map.put("filename", r.getFilename());
                map.put("content", r.getContent());
                map.put("score", r.getScore());
                map.put("chunkIndex", r.getChunkIndex());
                return map;
            })
            .collect(Collectors.toList());
}
```

**Flusso interno:**
```
MCP Client --> tools/call "document_search" --> LocalMindMcpTools.documentSearch()
    --> DocumentSearchUseCase.search(query, topK)
    --> Qdrant Vector Store (similarity search)
    --> Lista SearchResult --> Conversione in Map --> JSON-RPC response
```

### 3.2 `chat(message, provider, model, temperature)`

**Descrizione:** Invia un messaggio a un LLM attraverso il gateway multi-provider di LocalMind.
Supporta Ollama, OpenAI, Anthropic, Google.

**Parametri:**

| Parametro     | Tipo   | Obbligatorio | Default  | Descrizione                          |
|---------------|--------|--------------|----------|--------------------------------------|
| `message`     | String | Si'          | -        | Messaggio da inviare al LLM          |
| `provider`    | String | No           | OLLAMA   | Provider: OLLAMA, OPENAI, ANTHROPIC, GOOGLE |
| `model`       | String | No           | default  | Nome specifico del modello           |
| `temperature` | Double | No           | 0.7      | Temperatura per la generazione (0-1) |

**Risposta:** Oggetto con `content`, `model`, `provider`, `latencyMs`.

**Implementazione:**

```java
@Tool(description = "Send a message to an LLM through LocalMind's multi-provider gateway. " +
      "Supports Ollama, OpenAI, Anthropic, Google.")
public Map<String, Object> chat(
        @ToolParam(description = "The message to send to the LLM") String message,
        @ToolParam(description = "LLM provider: OLLAMA, OPENAI, ANTHROPIC, GOOGLE") String provider,
        @ToolParam(description = "Specific model name (optional)") String model,
        @ToolParam(description = "Temperature for generation, 0.0-1.0") Double temperature) {

    LlmRequest.LlmRequestBuilder requestBuilder = LlmRequest.builder()
            .messages(List.of(ChatMessage.builder()
                    .role(ChatMessage.Role.USER)
                    .content(message)
                    .build()));

    if (provider != null && !provider.isBlank()) {
        requestBuilder.provider(LlmProvider.valueOf(provider.toUpperCase()));
    }
    if (model != null && !model.isBlank()) {
        requestBuilder.model(model);
    }
    if (temperature != null) {
        requestBuilder.temperature(temperature);
    }

    LlmResponse response = chatUseCase.chat(requestBuilder.build());

    Map<String, Object> result = new HashMap<>();
    result.put("content", response.getContent());
    result.put("model", response.getModel());
    result.put("provider", response.getProvider().name());
    result.put("latencyMs", response.getLatencyMs());
    return result;
}
```

**Flusso interno:**
```
MCP Client --> tools/call "chat" --> LocalMindMcpTools.chat()
    --> ChatUseCase.chat(LlmRequest)
    --> Provider selezionato (Ollama/OpenAI/Anthropic/Google)
    --> LlmResponse --> Conversione in Map --> JSON-RPC response
```

### 3.3 `list_models()`

**Descrizione:** Elenca i provider LLM disponibili e il loro stato in LocalMind.

**Parametri:** Nessuno.

**Risposta:** Oggetto con `providers` (lista) e `defaultProvider`.

```java
@Tool(description = "List available LLM providers and their status in LocalMind")
public Map<String, Object> listModels() {
    Map<String, Object> result = new HashMap<>();
    result.put("providers", List.of("OLLAMA", "OPENAI", "ANTHROPIC", "GOOGLE"));
    result.put("defaultProvider", "OLLAMA");
    return result;
}
```

### Riepilogo tool

| Tool               | Metodo JSON-RPC       | Use Case di dominio          | Effetto                    |
|--------------------|-----------------------|------------------------------|----------------------------|
| `document_search`  | `tools/call`          | `DocumentSearchUseCase`      | Ricerca RAG su Qdrant      |
| `chat`             | `tools/call`          | `ChatUseCase`                | Chiamata LLM multi-provider|
| `list_models`      | `tools/call`          | (statico)                    | Elenco provider            |

---

## 4. Risorse esposte

Le risorse sono implementate in `LocalMindMcpResources.java`. Attualmente le risorse vengono
registrate programmaticamente tramite `McpConfiguration` poiche' il supporto per annotazioni
`@McpResource` dipende dalla versione di Spring AI.

### 4.1 `config://providers`

**URI:** `config://providers`  
**Tipo MIME:** `application/json`  
**Descrizione:** Restituisce la configurazione completa dei provider LLM disponibili.

```json
{
  "providers": [
    {"name": "OLLAMA", "local": true, "defaultModel": "llama3.2"},
    {"name": "OPENAI", "local": false, "defaultModel": "gpt-4o"},
    {"name": "ANTHROPIC", "local": false, "defaultModel": "claude-sonnet-4-20250514"},
    {"name": "GOOGLE", "local": false, "defaultModel": "gemini-pro"}
  ],
  "defaultProvider": "OLLAMA"
}
```

**Implementazione:**

```java
@Component
@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true",
                       matchIfMissing = true)
public class LocalMindMcpResources {

    public String getProviderConfig() {
        return """
                {
                  "providers": [
                    {"name": "OLLAMA", "local": true, "defaultModel": "llama3.2"},
                    {"name": "OPENAI", "local": false, "defaultModel": "gpt-4o"},
                    {"name": "ANTHROPIC", "local": false, "defaultModel": "claude-sonnet-4-20250514"},
                    {"name": "GOOGLE", "local": false, "defaultModel": "gemini-pro"}
                  ],
                  "defaultProvider": "OLLAMA"
                }
                """;
    }
}
```

### 4.2 `document://{id}` (pianificato)

**URI:** `document://{id}`  
**Tipo MIME:** `text/plain`  
**Descrizione:** Restituisce il contenuto completo di un documento indicizzato, identificato
dal suo UUID. Questa risorsa sara' implementata quando il document retrieval by ID sara'
disponibile nel domain layer.

---

## 5. Prompt template

I prompt template sono implementati in `LocalMindMcpPrompts.java` e forniscono pattern
predefiniti per interazioni comuni con la knowledge base.

### 5.1 `rag-query`

**Descrizione:** Query con contesto RAG precompilato. Combina i risultati della ricerca
semantica con la query dell'utente in un prompt strutturato.

**Parametri:**
- `query` (String) - La domanda dell'utente
- `context` (String) - Il contesto estratto dalla knowledge base

**Template:**

```java
public String getRagQueryPrompt(String query, String context) {
    return String.format("""
            You are a helpful AI assistant with access to a knowledge base.

            Context from knowledge base:
            %s

            User query: %s

            Please provide a comprehensive answer based on the context provided.
            If the context doesn't contain relevant information, say so clearly.
            """, context, query);
}
```

### 5.2 `summarize-document`

**Descrizione:** Genera un riassunto strutturato di un documento.

**Parametri:**
- `content` (String) - Il contenuto del documento da riassumere

**Template:**

```java
public String getSummarizeDocumentPrompt(String content) {
    return String.format("""
            Please provide a concise summary of the following document:

            %s

            The summary should be 2-3 paragraphs highlighting the main points,
            key findings, and actionable insights.
            """, content);
}
```

### Riepilogo prompt

| Prompt                | Parametri          | Caso d'uso                              |
|-----------------------|--------------------|-----------------------------------------|
| `rag-query`           | query, context     | Q&A con contesto dalla knowledge base    |
| `summarize-document`  | content            | Riassunto automatico di documenti        |

---

## 6. Implementazione con Spring AI

### 6.1 Annotazione `@Tool`

Spring AI 1.0.0 fornisce l'annotazione `@Tool` per dichiarare metodi come tool MCP.
L'annotazione genera automaticamente lo schema JSON per i parametri e registra il tool
nel server MCP.

```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

@Tool(description = "Descrizione del tool per il modello LLM")
public ReturnType methodName(
    @ToolParam(description = "Descrizione parametro") ParamType param) {
    // implementazione
}
```

### 6.2 Registrazione automatica

Il starter `spring-ai-starter-mcp-server-webmvc` scansiona automaticamente tutti i bean
contenenti metodi annotati `@Tool` e li registra come tool MCP. La configurazione in
`application-dev.yml` imposta il nome e la versione del server:

```yaml
spring:
  ai:
    mcp:
      server:
        name: localmind        # Nome del server MCP
        version: 0.1.0         # Versione del server
```

### 6.3 Endpoint esposti

Con il starter WebMVC, il server MCP espone automaticamente:

| Endpoint          | Metodo | Descrizione                           |
|-------------------|--------|---------------------------------------|
| `/mcp/sse`        | GET    | Stream SSE per notifiche server->client|
| `/mcp/message`    | POST   | Messaggi JSON-RPC client->server      |

Questi endpoint sono separati dalle API REST di LocalMind (`/api/v1/*`).

---

## 7. Delegazione ai domain use case

Un principio chiave dell'architettura esagonale di LocalMind e' che i componenti MCP server
(nel modulo `infrastructure`) delegano la logica di business ai **domain use case**:

```
+------------------------------+       +-----------------------------+
|   Infrastructure Layer       |       |     Domain Layer            |
|                              |       |                             |
| LocalMindMcpTools            |       | DocumentSearchUseCase       |
|   .documentSearch() ---------|------>|   .search(query, topK)      |
|   .chat() -------------------|------>| ChatUseCase                 |
|                              |       |   .chat(LlmRequest)         |
+------------------------------+       +-----------------------------+
```

### Dipendenze iniettate

```java
public class LocalMindMcpTools {

    private final DocumentSearchUseCase documentSearchUseCase;
    private final ChatUseCase chatUseCase;

    public LocalMindMcpTools(DocumentSearchUseCase documentSearchUseCase,
                             ChatUseCase chatUseCase) {
        this.documentSearchUseCase = documentSearchUseCase;
        this.chatUseCase = chatUseCase;
    }
    // ...
}
```

Questo garantisce che:
1. La logica di business rimane nel dominio, non nell'infrastruttura
2. I tool MCP sono una semplice facciata (adapter) verso il dominio
3. La testabilita' e' garantita iniettando mock dei use case
4. Aggiungere nuovi tool e' semplice: basta aggiungere un metodo `@Tool` che delega

---

## 8. Mappa dei file sorgente

```
localmind-infrastructure/
  src/main/java/com/localmind/infrastructure/mcp/
    server/
      LocalMindMcpTools.java         # @Tool: document_search, chat, list_models
      LocalMindMcpResources.java     # Resources: config://providers
      LocalMindMcpPrompts.java       # Prompts: rag-query, summarize-document
    config/
      McpConfiguration.java          # Bean definitions per MCP client/server
```

**Dipendenze Maven (localmind-infrastructure/pom.xml):**

```xml
<!-- MCP Server (WebMVC) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

---

> **Navigazione documentazione:**
> - Precedente: [01-panoramica-protocollo-mcp.md](01-panoramica-protocollo-mcp.md)
> - Prossimo: [03-client-implementation.md](03-client-implementation.md)
> - Configurazione: [04-configurazione.md](04-configurazione.md)
