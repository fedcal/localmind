# Panoramica del Model Context Protocol (MCP)

**Progetto:** LocalMind - Piattaforma AI Local-First  
**Versione:** 0.1.0  
**Ultimo aggiornamento:** 2026-02-09  
**Modulo di riferimento:** localmind-infrastructure (MCP server/client)

---

## Indice

1. [Cos'e' il Model Context Protocol](#1-cose-il-model-context-protocol)
2. [Architettura del protocollo](#2-architettura-del-protocollo)
3. [Concetti fondamentali](#3-concetti-fondamentali)
4. [Meccanismi di trasporto](#4-meccanismi-di-trasporto)
5. [Flusso di comunicazione JSON-RPC](#5-flusso-di-comunicazione-json-rpc)
6. [Perche' MCP e' importante per LocalMind](#6-perche-mcp-e-importante-per-localmind)
7. [Riferimenti](#7-riferimenti)

---

## 1. Cos'e' il Model Context Protocol

Il **Model Context Protocol (MCP)** e' uno standard aperto, proposto da Anthropic nel novembre 2024,
che definisce un'interfaccia universale per la comunicazione tra applicazioni AI e sorgenti di dati
o strumenti esterni. MCP risolve il problema dell'integrazione N x M: senza uno standard, ogni
applicazione AI deve implementare connettori specifici per ogni servizio esterno. Con MCP, un
singolo protocollo consente a qualsiasi client compatibile di comunicare con qualsiasi server
compatibile.

### Analogia: USB per l'AI

MCP e' per le applicazioni AI cio' che USB e' per i dispositivi hardware. Cosi' come USB ha
eliminato la necessita' di cavi proprietari per ogni periferica, MCP elimina la necessita' di
integrazioni personalizzate per ogni sorgente di contesto.

```
  Senza MCP:                          Con MCP:

  App1 ---custom---> Servizio A        App1 ---MCP---> Servizio A
  App1 ---custom---> Servizio B        App1 ---MCP---> Servizio B
  App2 ---custom---> Servizio A        App2 ---MCP---> Servizio A
  App2 ---custom---> Servizio B        App2 ---MCP---> Servizio B
  (4 integrazioni custom)             (1 protocollo, 4 connessioni)
```

---

## 2. Architettura del protocollo

MCP segue un'architettura **client-server** con ruoli ben definiti:

```
+------------------+          +------------------+
|                  |          |                  |
|    MCP Client    | <------> |    MCP Server    |
|   (Host App)     |  JSON-   |  (Tool/Resource  |
|                  |  RPC     |   Provider)      |
+------------------+          +------------------+
       |                             |
       v                             v
  Modello LLM                   Dati / Servizi
  (Ollama, GPT,                (Filesystem, DB,
   Claude, etc.)                API, etc.)
```

### Ruoli

| Ruolo       | Descrizione                                                      | Esempio in LocalMind            |
|-------------|------------------------------------------------------------------|---------------------------------|
| **Host**    | Applicazione che ospita il client MCP e il modello LLM           | localmind-app (Spring Boot)     |
| **Client**  | Componente che gestisce la connessione verso un MCP server       | `SpringAiMcpClientAdapter`      |
| **Server**  | Componente che espone tool, risorse e prompt                     | `LocalMindMcpTools` et al.      |

### Ciclo di vita della connessione

```
Client                              Server
  |                                    |
  |--- initialize (capabilities) ----->|
  |<-- initialize response ------------|
  |--- initialized notification ------>|
  |                                    |
  |--- tools/list -------------------->|
  |<-- tools list response ------------|
  |                                    |
  |--- tools/call (tool, args) ------->|
  |<-- tool result --------------------|
  |                                    |
  |--- shutdown ---------------------->|
  |<-- shutdown ack -------------------|
```

---

## 3. Concetti fondamentali

MCP definisce tre primitive principali che un server puo' esporre:

### 3.1 Tool (Strumenti)

I **Tool** sono funzioni invocabili che il modello LLM puo' decidere di chiamare durante una
conversazione. Rappresentano azioni con effetti collaterali (ricerca, scrittura, calcolo).

```json
{
  "name": "document_search",
  "description": "Search documents in the LocalMind knowledge base using RAG",
  "inputSchema": {
    "type": "object",
    "properties": {
      "query": { "type": "string", "description": "The search query text" },
      "topK": { "type": "integer", "description": "Number of top results" }
    },
    "required": ["query"]
  }
}
```

In LocalMind, i tool sono implementati in `LocalMindMcpTools.java` con l'annotazione `@Tool`
di Spring AI (vedi [02-server-implementation.md](02-server-implementation.md)).

### 3.2 Resource (Risorse)

Le **Resource** sono dati esposti dal server che il client puo' leggere. Sono identificate
da URI e possono essere statiche o dinamiche.

```
document://{id}        -> Contenuto di un documento indicizzato
config://providers     -> Configurazione dei provider LLM
```

### 3.3 Prompt (Template)

I **Prompt** sono template parametrizzati che il server suggerisce al client per interazioni
comuni. Consentono di standardizzare le modalita' di interrogazione.

```
rag-query              -> Query con contesto RAG precompilato
summarize-document     -> Riassunto automatico di un documento
```

### Tabella riassuntiva

| Primitiva    | Controllata da | Descrizione                         | Analogia API     |
|-------------|----------------|-------------------------------------|------------------|
| **Tool**    | Modello (LLM)  | Funzione invocabile                 | POST endpoint    |
| **Resource**| Applicazione   | Dato leggibile                      | GET endpoint     |
| **Prompt**  | Utente         | Template di interazione             | Query template   |

---

## 4. Meccanismi di trasporto

MCP supporta tre meccanismi di trasporto per la comunicazione tra client e server:

### 4.1 STDIO (Standard Input/Output)

Il server viene avviato come processo figlio dal client. La comunicazione avviene
attraverso stdin/stdout del processo.

```
Client (LocalMind)
    |
    +-- spawn process --> Server (es. npx @modelcontextprotocol/server-filesystem)
    |       stdin  ------>
    |       stdout <------
```

**Vantaggi:** Semplicita', nessuna configurazione di rete, isolamento del processo.  
**Svantaggi:** Solo locale, un client per server.

In LocalMind, il tipo `STDIO` e' gestito da `McpServerType.STDIO` e richiede i campi
`command` e `args` in `McpServerConfig`.

### 4.2 SSE (Server-Sent Events)

Il server espone un endpoint HTTP con SSE per lo streaming degli eventi.
Il client si connette via HTTP.

```
Client (LocalMind)  ---HTTP POST--->  Server (remoto)
                    <---SSE stream--
```

**Vantaggi:** Comunicazione remota, compatibile con firewall/proxy HTTP.  
**Svantaggi:** Unidirezionale per SSE (richiede POST separato per le richieste).

In LocalMind, il tipo `SSE` e' gestito da `McpServerType.SSE` e richiede il campo
`url` in `McpServerConfig`.

### 4.3 HTTP Streamable (Specifica 2025)

Evoluzione di SSE introdotta nella specifica MCP 2025-03-26. Utilizza HTTP standard
con streaming bidirezionale tramite chunked transfer encoding.

**Vantaggi:** Bidirezionale, stateless opzionale, migliore scalabilita'.  
**Svantaggi:** Specifica piu' recente, supporto SDK in fase di adozione.

> **Nota:** LocalMind attualmente supporta STDIO e SSE. Il supporto per HTTP Streamable
> e' previsto nella roadmap (vedi [07-troubleshooting.md](07-troubleshooting.md)).

---

## 5. Flusso di comunicazione JSON-RPC

MCP utilizza **JSON-RPC 2.0** come formato dei messaggi. Ogni scambio segue il pattern
request/response con supporto per notifiche unidirezionali.

### Esempio: Chiamata a un tool

**Request (Client -> Server):**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "document_search",
    "arguments": {
      "query": "architettura esagonale",
      "topK": 5
    }
  }
}
```

**Response (Server -> Client):**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "[{\"documentId\": \"abc-123\", \"content\": \"...\", \"score\": 0.92}]"
      }
    ]
  }
}
```

### Metodi principali

| Metodo              | Direzione      | Descrizione                              |
|---------------------|----------------|------------------------------------------|
| `initialize`        | Client->Server | Negoziazione capabilities iniziale       |
| `tools/list`        | Client->Server | Elenco dei tool disponibili              |
| `tools/call`        | Client->Server | Invocazione di un tool specifico         |
| `resources/list`    | Client->Server | Elenco delle risorse disponibili         |
| `resources/read`    | Client->Server | Lettura di una risorsa specifica         |
| `prompts/list`      | Client->Server | Elenco dei prompt template               |
| `prompts/get`       | Client->Server | Recupero di un prompt con argomenti      |
| `notifications/*`   | Bidirezionale  | Notifiche senza risposta attesa          |

---

## 6. Perche' MCP e' importante per LocalMind

LocalMind e' una piattaforma AI local-first che beneficia enormemente dall'integrazione MCP
per diversi motivi:

### 6.1 Estensibilita' senza limiti

Grazie a MCP, LocalMind puo' espandere le proprie capacita' connettendosi a qualsiasi server
MCP compatibile senza modificare il codice core. Un utente puo' aggiungere:

- Accesso al filesystem locale (lettura/scrittura file)
- Connessione a database esterni (PostgreSQL, SQLite, MongoDB)
- Web scraping e ricerca web
- Integrazione con API aziendali
- Strumenti di sviluppo (Git, Docker, Kubernetes)

### 6.2 Interoperabilita'

LocalMind puo' essere utilizzato come MCP server da applicazioni esterne come Claude Desktop,
rendendo la knowledge base e il gateway LLM di LocalMind accessibili attraverso un protocollo
standard.

### 6.3 Architettura local-first

MCP con trasporto STDIO consente a LocalMind di avviare server MCP come processi locali,
mantenendo tutti i dati sulla macchina dell'utente. Questo si allinea perfettamente con la
filosofia local-first della piattaforma.

### 6.4 Ruolo duale

LocalMind implementa **sia il ruolo di server che di client MCP**:

```
                    +-------------------+
                    |                   |
  Claude Desktop -->|  LocalMind come   |--> Knowledge Base (RAG)
  Altro MCP Client->|   MCP SERVER      |--> LLM Gateway
                    |                   |
                    +-------------------+
                    |                   |
                    |  LocalMind come   |--> Filesystem MCP Server
                    |   MCP CLIENT      |--> Database MCP Server
                    |                   |--> Custom MCP Server
                    +-------------------+
```

Per i dettagli implementativi, si vedano:
- [02-server-implementation.md](02-server-implementation.md) - LocalMind come MCP Server
- [03-client-implementation.md](03-client-implementation.md) - LocalMind come MCP Client

---

## 7. Riferimenti

| Risorsa                                    | URL / Note                                         |
|--------------------------------------------|----------------------------------------------------|
| Specifica MCP ufficiale                    | https://modelcontextprotocol.io                     |
| MCP SDK Java                               | https://github.com/modelcontextprotocol/java-sdk    |
| Spring AI MCP Documentation                | https://docs.spring.io/spring-ai/reference/api/mcp/ |
| Spring AI MCP Server (WebMVC)              | `spring-ai-starter-mcp-server-webmvc`               |
| Spring AI MCP Client                       | `spring-ai-starter-mcp-client`                      |
| Specifica JSON-RPC 2.0                     | https://www.jsonrpc.org/specification                |
| MCP Servers Registry                       | https://github.com/modelcontextprotocol/servers      |
| Annuncio MCP (Anthropic, Nov 2024)         | https://www.anthropic.com/news/model-context-protocol|

---

> **Navigazione documentazione:**
> - Prossimo: [02-server-implementation.md](02-server-implementation.md)
> - Indice modulo: [README.md](README.md)
