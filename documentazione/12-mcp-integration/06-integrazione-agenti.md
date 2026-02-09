# Integrazione MCP con Agenti AI di LocalMind

**Progetto:** LocalMind - Piattaforma AI Local-First  
**Versione:** 0.1.0  
**Ultimo aggiornamento:** 2026-02-09  
**Modulo di riferimento:** localmind-domain (`domain.mcp`, `domain.agent`)

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Architettura dell'integrazione](#2-architettura-dellintegrazione)
3. [Flusso: Agent con tool MCP](#3-flusso-agent-con-tool-mcp)
4. [Mapping McpExternalTool -> AgentTool](#4-mapping-mcpexternaltool---agenttool)
5. [Scenari d'uso](#5-scenari-duso)
6. [Estensibilita'](#6-estensibilita)

---

## 1. Panoramica

Gli agenti AI di LocalMind possono utilizzare tool provenienti da server MCP esterni per
espandere le proprie capacita' oltre i tool locali (ricerca RAG, chat LLM). Questo trasforma
LocalMind da una piattaforma chiusa a un hub AI estensibile, dove ogni server MCP aggiunge
nuove capacita' senza modifiche al codice.

```
+------------------------------------------------------+
|                   Agente AI LocalMind                |
|                                                      |
|  "Trova i file .pdf nella cartella documenti,        |
|   indicizzali nella knowledge base, e genera         |
|   un riassunto per ciascuno"                         |
|                                                      |
|  Tool disponibili:                                   |
|  +-- document_search (locale)                        |
|  +-- chat (locale)                                   |
|  +-- list_directory (MCP: filesystem server)         |
|  +-- read_file (MCP: filesystem server)              |
|  +-- write_file (MCP: filesystem server)             |
+------------------------------------------------------+
```

L'agente vede una lista unificata di tool (locali + MCP esterni) e puo' scegliere
dinamicamente quali utilizzare in base al task richiesto dall'utente.

---

## 2. Architettura dell'integrazione

L'integrazione tra agenti e MCP sfrutta i port/use case gia' definiti nel dominio:

```
+--------------------------------------------+
|              Agent Domain                   |
|                                             |
|  AgentOrchestrator                          |
|       |                                     |
|       +-- Tool Selection (LLM decide)       |
|       |       |                             |
|       |       +-- Local Tool                |
|       |       |     (DocumentSearchUseCase) |
|       |       |                             |
|       |       +-- External MCP Tool         |
|       |             |                       |
|       |             v                       |
|  +----+-----------------------------------+ |
|  |    McpToolDiscoveryUseCase             | |
|  |    McpToolExecutionUseCase             | |
|  +----------------------------------------+ |
+--------------------------------------------+
              |
              v
+--------------------------------------------+
|         Infrastructure Layer                |
|  SpringAiMcpClientAdapter                   |
|  (connessioni a server MCP esterni)         |
+--------------------------------------------+
```

### Componenti coinvolti

| Componente                  | Layer          | Responsabilita'                           |
|-----------------------------|----------------|-------------------------------------------|
| `AgentOrchestrator`         | Domain (agent) | Orchestrazione del ciclo agent            |
| `McpToolDiscoveryUseCase`   | Domain (mcp)   | Scoperta tool dai server connessi          |
| `McpToolExecutionUseCase`   | Domain (mcp)   | Esecuzione tool su server esterni          |
| `McpToolOrchestratorService`| Domain (mcp)   | Implementazione dei due use case           |
| `SpringAiMcpClientAdapter`  | Infrastructure  | Comunicazione effettiva via MCP SDK        |

---

## 3. Flusso: Agent con tool MCP

Di seguito il flusso dettagliato quando un agente utilizza un tool MCP esterno:

### Fase 1: Discovery dei tool disponibili

```
1. AgentOrchestrator inizia un nuovo task
2. Recupera tool locali (document_search, chat, list_models)
3. Chiama McpToolDiscoveryUseCase.listAllExternalTools()
4. McpToolOrchestratorService itera sui server CONNECTED
5. Per ogni server: McpClientPort.discoverTools(serverId)
6. Aggrega tutti i tool in una lista unificata
7. Prepara la lista tool per il modello LLM
```

### Fase 2: Selezione del tool da parte del LLM

```
8. AgentOrchestrator invia al LLM:
   - Il messaggio dell'utente
   - La lista di tool disponibili (locali + MCP)
   - Il contesto della conversazione
9. Il LLM analizza il task e decide quale tool chiamare
10. Il LLM restituisce una tool_call con:
    - nome del tool
    - argomenti (JSON)
```

### Fase 3: Esecuzione del tool MCP

```
11. AgentOrchestrator verifica se il tool e' locale o esterno (MCP)
12. Se MCP: crea McpToolExecutionRequest con:
    - toolName: nome del tool scelto dal LLM
    - serverId: ID del server che espone il tool
    - arguments: argomenti dal LLM
13. Chiama McpToolExecutionUseCase.executeTool(request)
14. McpToolOrchestratorService delega a McpClientPort.executeTool()
15. SpringAiMcpClientAdapter invia JSON-RPC tools/call al server MCP
16. Riceve il risultato e lo wrappa in McpToolExecutionResult
17. AgentOrchestrator fornisce il risultato al LLM come tool_result
18. Il LLM continua il ragionamento con il risultato del tool
```

### Diagramma di sequenza

```
Utente     Agent        LLM         McpToolDiscovery   McpToolExecution   MCP Server
  |          |           |                |                  |                |
  |--task--->|           |                |                  |                |
  |          |--listAll->|                |                  |                |
  |          |           |<--tool list----|                  |                |
  |          |--prompt-->|                |                  |                |
  |          |<-tool_call|                |                  |                |
  |          |           |                |--executeTool---->|                |
  |          |           |                |                  |--tools/call--->|
  |          |           |                |                  |<--result-------|
  |          |           |                |<--result---------|                |
  |          |--result-->|                |                  |                |
  |          |<-response-|                |                  |                |
  |<-answer--|           |                |                  |                |
```

---

## 4. Mapping McpExternalTool -> AgentTool

Per integrare i tool MCP con il sistema agenti, e' necessario un mapping tra il modello
MCP e il modello agenti.

### Modello MCP

```java
// com.localmind.domain.mcp.model.McpExternalTool
public class McpExternalTool {
    private String name;           // "read_file"
    private String description;    // "Read the complete contents of a file"
    private String inputSchema;    // JSON Schema come stringa
    private String serverId;       // "a1b2c3d4-..."
}
```

### Modello Agenti (suggerito)

```java
// com.localmind.domain.agent.model.AgentTool
public class AgentTool {
    private String name;
    private String description;
    private String parametersSchema;   // JSON Schema
    private ToolSource source;         // LOCAL o MCP
    private String mcpServerId;        // Solo per source=MCP
}

public enum ToolSource {
    LOCAL,   // Tool locali (document_search, chat, etc.)
    MCP      // Tool da server MCP esterni
}
```

### Funzione di mapping

```java
public static AgentTool fromMcpExternalTool(McpExternalTool mcpTool) {
    return AgentTool.builder()
            .name(mcpTool.getName())
            .description(mcpTool.getDescription())
            .parametersSchema(mcpTool.getInputSchema())
            .source(ToolSource.MCP)
            .mcpServerId(mcpTool.getServerId())
            .build();
}
```

### Formato per il LLM (OpenAI function calling compatible)

```json
{
  "type": "function",
  "function": {
    "name": "read_file",
    "description": "Read the complete contents of a file from the file system",
    "parameters": {
      "type": "object",
      "properties": {
        "path": {
          "type": "string",
          "description": "The path of the file to read"
        }
      },
      "required": ["path"]
    }
  }
}
```

---

## 5. Scenari d'uso

### 5.1 Agente con accesso al filesystem via MCP

**Scenario:** L'utente chiede all'agente di trovare tutti i file Python in una directory,
leggerne il contenuto e generare documentazione.

**Server MCP:** `@modelcontextprotocol/server-filesystem`

**Flusso:**

1. Agente usa `list_directory` (MCP) per enumerare i file
2. Filtra i file `.py` dalla lista
3. Per ogni file, usa `read_file` (MCP) per leggere il contenuto
4. Usa `chat` (locale) per generare la documentazione con il LLM
5. Usa `write_file` (MCP) per salvare la documentazione generata

```
Task: "Documenta tutti i file Python in /home/utente/progetto/src"

Agente:
  1. list_directory("/home/utente/progetto/src")  --> [main.py, utils.py, ...]
  2. read_file("/home/utente/progetto/src/main.py")  --> contenuto
  3. chat("Genera docstring per: {contenuto}")  --> documentazione
  4. write_file("/home/utente/progetto/docs/main.md", documentazione)
  ... ripeti per ogni file
```

### 5.2 Agente con accesso a database esterno via MCP

**Scenario:** L'utente chiede all'agente di analizzare dati da un database SQLite e
confrontarli con la knowledge base.

**Server MCP:** `@modelcontextprotocol/server-sqlite`

**Flusso:**

1. Agente usa `read_query` (MCP SQLite) per eseguire una query SQL
2. Usa `document_search` (locale) per cercare contesto nella knowledge base
3. Usa `chat` (locale) per generare un'analisi combinata
4. Restituisce il risultato all'utente

### 5.3 Agente con capacita' di web scraping via MCP

**Scenario:** L'utente chiede all'agente di raccogliere informazioni dal web e
aggiungerle alla knowledge base.

**Server MCP:** `@anthropic/server-puppeteer` o `@modelcontextprotocol/server-fetch`

**Flusso:**

1. Agente usa `fetch` (MCP) per scaricare contenuto da una URL
2. Usa `chat` (locale) per estrarre informazioni rilevanti
3. Usa `document_search` (locale) per verificare se l'informazione e' gia' presente
4. Eventualmente indicizza il nuovo contenuto

---

## 6. Estensibilita'

### Aggiungere nuovi server MCP per espandere le capacita'

Il sistema e' progettato per essere estensibile senza modifiche al codice:

```
Passo 1: Registrare il nuovo server MCP via API REST
         POST /api/v1/mcp/servers

Passo 2: Il server si connette automaticamente
         Status: CONNECTED

Passo 3: I tool del nuovo server sono immediatamente disponibili
         GET /api/v1/mcp/tools  --> include nuovi tool

Passo 4: Gli agenti possono usare i nuovi tool nella prossima esecuzione
         AgentOrchestrator -> listAllExternalTools() -> nuovi tool inclusi
```

### Server MCP consigliati per scenari comuni

| Capacita' desiderata     | Server MCP                                    | Tool principali                    |
|--------------------------|-----------------------------------------------|------------------------------------|
| Accesso filesystem       | `@modelcontextprotocol/server-filesystem`     | read_file, write_file, list_dir    |
| Database SQLite          | `@modelcontextprotocol/server-sqlite`         | read_query, write_query, list_tables|
| Database PostgreSQL      | `@modelcontextprotocol/server-postgres`       | query, list_tables, describe_table |
| Ricerca web              | `@modelcontextprotocol/server-fetch`          | fetch                              |
| Browser automation       | `@anthropic/server-puppeteer`                 | navigate, screenshot, click        |
| Git operations           | `@modelcontextprotocol/server-git`            | log, diff, status, commit          |
| GitHub API               | `@modelcontextprotocol/server-github`         | issues, pull_requests, repos       |
| Slack                    | `@modelcontextprotocol/server-slack`          | send_message, list_channels        |
| Google Drive             | `@modelcontextprotocol/server-gdrive`         | search, read, create               |
| Memoria persistente      | `@modelcontextprotocol/server-memory`         | store, retrieve, search            |

### Pattern di composizione

Combinando piu' server MCP, gli agenti possono eseguire workflow complessi:

```
Esempio: "Analizza i commit della settimana e genera un report"

Git MCP Server       +   LocalMind chat tool   +   Filesystem MCP Server
(git log, git diff)      (analisi con LLM)          (scrivi report.md)
```

---

> **Navigazione documentazione:**
> - Precedente: [05-esempi-utilizzo.md](05-esempi-utilizzo.md)
> - Prossimo: [07-troubleshooting.md](07-troubleshooting.md)
> - Client implementation: [03-client-implementation.md](03-client-implementation.md)
