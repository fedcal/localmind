# Esempi di Utilizzo MCP in LocalMind

**Progetto:** LocalMind - Piattaforma AI Local-First  
**Versione:** 0.1.0  
**Ultimo aggiornamento:** 2026-02-09  
**Prerequisiti:** LocalMind backend avviato con profilo `dev`

---

## Indice

1. [Esempio 1: Connessione a filesystem MCP server](#esempio-1-connessione-a-filesystem-mcp-server)
2. [Esempio 2: Uso da Claude Desktop](#esempio-2-uso-da-claude-desktop)
3. [Esempio 3: Connessione a server SSE remoto](#esempio-3-connessione-a-server-sse-remoto)
4. [Esempio 4: API REST - Registrare un server](#esempio-4-api-rest---registrare-un-server)
5. [Esempio 5: API REST - Testare connessione](#esempio-5-api-rest---testare-connessione)
6. [Esempio 6: API REST - Scoprire tool](#esempio-6-api-rest---scoprire-tool)
7. [Esempio 7: API REST - Eseguire un tool](#esempio-7-api-rest---eseguire-un-tool)
8. [Uso dal frontend Angular](#8-uso-dal-frontend-angular)

---

## Esempio 1: Connessione a filesystem MCP server

Il server MCP `@modelcontextprotocol/server-filesystem` consente di leggere e scrivere file
sul filesystem locale. Questo e' uno dei server MCP piu' comuni e utili.

### Prerequisiti

```bash
# Assicurarsi di avere Node.js >= 18 installato
node --version

# Il server si avvia automaticamente via npx (non richiede installazione)
```

### Registrazione tramite API

```bash
curl -X POST http://localhost:8080/api/v1/mcp/servers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Filesystem locale",
    "description": "Accesso ai file nella directory documenti",
    "type": "STDIO",
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-filesystem", "/home/utente/documenti"],
    "timeoutSeconds": 30,
    "autoReconnect": true
  }'
```

### Risposta attesa

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Filesystem locale",
  "description": "Accesso ai file nella directory documenti",
  "type": "STDIO",
  "status": "CONNECTED",
  "config": {
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-filesystem", "/home/utente/documenti"],
    "timeoutSeconds": 30,
    "autoReconnect": true
  },
  "createdAt": "2026-02-09T10:30:00",
  "lastConnectedAt": "2026-02-09T10:30:01"
}
```

### Tool disponibili dopo la connessione

Il server filesystem espone tipicamente i seguenti tool:

| Tool              | Descrizione                                |
|-------------------|--------------------------------------------|
| `read_file`       | Leggi il contenuto di un file              |
| `write_file`      | Scrivi contenuto in un file                |
| `list_directory`  | Elenca il contenuto di una directory       |
| `create_directory`| Crea una nuova directory                   |
| `move_file`       | Sposta/rinomina un file                    |
| `search_files`    | Cerca file per pattern                     |
| `get_file_info`   | Ottieni metadati di un file                |

---

## Esempio 2: Uso da Claude Desktop

LocalMind puo' essere utilizzato come MCP server da **Claude Desktop** (o qualsiasi
altro MCP client). Questo espone la knowledge base RAG e il gateway LLM a Claude.

### Configurazione claude_desktop_config.json

Individuare il file di configurazione di Claude Desktop:

- **macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`
- **Linux:** `~/.config/Claude/claude_desktop_config.json`

Aggiungere la configurazione del server LocalMind:

```json
{
  "mcpServers": {
    "localmind": {
      "url": "http://localhost:8080/mcp/sse",
      "description": "LocalMind knowledge base and LLM gateway"
    }
  }
}
```

### Verifica della connessione

Dopo aver riavviato Claude Desktop, verificare che i tool di LocalMind siano visibili.
Claude mostrera' un'icona di martello (tool) con i seguenti strumenti:

- **document_search** - Ricerca nella knowledge base
- **chat** - Invio messaggi al gateway LLM
- **list_models** - Elenco provider disponibili

### Esempio di interazione in Claude Desktop

```
Utente: "Cerca nella mia knowledge base informazioni sull'architettura esagonale"

Claude: [Chiama document_search con query="architettura esagonale", topK=5]
        "Ho trovato 5 documenti rilevanti nella tua knowledge base..."
```

---

## Esempio 3: Connessione a server SSE remoto

Per connettersi a un server MCP che espone un endpoint SSE remoto (ad esempio, un server
MCP aziendale o un servizio cloud).

### Registrazione server SSE

```bash
curl -X POST http://localhost:8080/api/v1/mcp/servers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Server MCP aziendale",
    "description": "Server MCP con accesso al database aziendale",
    "type": "SSE",
    "url": "http://mcp-server.azienda.local:3001/sse",
    "timeoutSeconds": 60,
    "autoReconnect": true
  }'
```

### Risposta

```json
{
  "id": "f1e2d3c4-b5a6-7890-fedc-ba0987654321",
  "name": "Server MCP aziendale",
  "type": "SSE",
  "status": "CONNECTED",
  "config": {
    "url": "http://mcp-server.azienda.local:3001/sse",
    "timeoutSeconds": 60,
    "autoReconnect": true
  },
  "createdAt": "2026-02-09T14:00:00",
  "lastConnectedAt": "2026-02-09T14:00:02"
}
```

---

## Esempio 4: API REST - Registrare un server

### Request

```bash
# Registrazione server STDIO (processo locale)
curl -X POST http://localhost:8080/api/v1/mcp/servers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "SQLite Explorer",
    "description": "Server MCP per accesso a database SQLite",
    "type": "STDIO",
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-sqlite", "/path/to/database.db"],
    "timeoutSeconds": 30,
    "autoReconnect": true
  }'
```

### Validazione

Il campo `name` e' obbligatorio (`@NotBlank`). Il campo `type` e' obbligatorio (`@NotNull`)
e deve essere `"STDIO"` o `"SSE"`. I constraint del database garantiscono che:

- Per `STDIO`: `command` sia presente
- Per `SSE`: `url` sia presente

### Elencare tutti i server registrati

```bash
curl -X GET http://localhost:8080/api/v1/mcp/servers
```

### Risposta

```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "name": "Filesystem locale",
    "type": "STDIO",
    "status": "CONNECTED"
  },
  {
    "id": "f1e2d3c4-b5a6-7890-fedc-ba0987654321",
    "name": "Server MCP aziendale",
    "type": "SSE",
    "status": "CONNECTED"
  }
]
```

### Eliminare un server

```bash
curl -X DELETE http://localhost:8080/api/v1/mcp/servers/a1b2c3d4-e5f6-7890-abcd-ef1234567890
# Risposta: 204 No Content
```

---

## Esempio 5: API REST - Testare connessione

### Request

```bash
curl -X POST http://localhost:8080/api/v1/mcp/servers/a1b2c3d4-e5f6-7890-abcd-ef1234567890/test
```

### Risposta (connessione riuscita)

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Filesystem locale",
  "type": "STDIO",
  "status": "CONNECTED",
  "lastConnectedAt": "2026-02-09T15:30:00"
}
```

### Risposta (connessione fallita)

```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Filesystem locale",
  "type": "STDIO",
  "status": "ERROR",
  "lastConnectedAt": null
}
```

### Riconnessione manuale

```bash
curl -X POST http://localhost:8080/api/v1/mcp/servers/a1b2c3d4-e5f6-7890-abcd-ef1234567890/reconnect
# Risposta: 200 OK
```

---

## Esempio 6: API REST - Scoprire tool

### Tutti i tool da tutti i server connessi

```bash
curl -X GET http://localhost:8080/api/v1/mcp/tools
```

### Risposta

```json
[
  {
    "name": "read_file",
    "description": "Read the complete contents of a file from the file system",
    "inputSchema": "{"type":"object","properties":{"path":{"type":"string"}},"required":["path"]}",
    "serverId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "local": false
  },
  {
    "name": "list_directory",
    "description": "List directory contents with file metadata",
    "inputSchema": "{"type":"object","properties":{"path":{"type":"string"}}}",
    "serverId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "local": false
  }
]
```

### Tool di un server specifico

```bash
curl -X GET http://localhost:8080/api/v1/mcp/tools/servers/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

### Tool locali di LocalMind

```bash
curl -X GET http://localhost:8080/api/v1/mcp/tools/local
```

### Risposta

```json
[
  {
    "name": "document_search",
    "description": "Search documents using RAG",
    "local": true
  },
  {
    "name": "chat",
    "description": "Send a message to an LLM",
    "local": true
  },
  {
    "name": "list_models",
    "description": "List available LLM providers",
    "local": true
  }
]
```

---

## Esempio 7: API REST - Eseguire un tool

### Eseguire `read_file` sul filesystem server

```bash
curl -X POST http://localhost:8080/api/v1/mcp/tools/execute \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "read_file",
    "serverId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "arguments": {
      "path": "/home/utente/documenti/readme.md"
    }
  }'
```

### Risposta (successo)

```json
{
  "toolName": "read_file",
  "result": "# My Project\n\nThis is the README file content...",
  "success": true,
  "errorMessage": null,
  "executionTimeMs": 45
}
```

### Risposta (errore)

```json
{
  "toolName": "read_file",
  "result": null,
  "success": false,
  "errorMessage": "File not found: /home/utente/documenti/nonexistent.txt",
  "executionTimeMs": 12
}
```

### Eseguire `list_directory`

```bash
curl -X POST http://localhost:8080/api/v1/mcp/tools/execute \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "list_directory",
    "serverId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "arguments": {
      "path": "/home/utente/documenti"
    }
  }'
```

---

## 8. Uso dal frontend Angular

Il frontend Angular di LocalMind puo' interagire con MCP attraverso le API REST descritte
sopra. Di seguito l'architettura dei servizi Angular suggerita.

### Servizio MCP Angular

```typescript
// mcp-server.service.ts
@Injectable({ providedIn: 'root' })
export class McpServerService {
  private readonly apiUrl = '/api/v1/mcp/servers';

  constructor(private http: HttpClient) {}

  register(request: CreateMcpServerRequest): Observable<McpServer> {
    return this.http.post<McpServer>(this.apiUrl, request);
  }

  listAll(): Observable<McpServer[]> {
    return this.http.get<McpServer[]>(this.apiUrl);
  }

  get(serverId: string): Observable<McpServer> {
    return this.http.get<McpServer>(`${this.apiUrl}/${serverId}`);
  }

  remove(serverId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${serverId}`);
  }

  testConnection(serverId: string): Observable<McpServer> {
    return this.http.post<McpServer>(`${this.apiUrl}/${serverId}/test`, {});
  }

  reconnect(serverId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${serverId}/reconnect`, {});
  }
}
```

```typescript
// mcp-tool.service.ts
@Injectable({ providedIn: 'root' })
export class McpToolService {
  private readonly apiUrl = '/api/v1/mcp/tools';

  constructor(private http: HttpClient) {}

  listAllTools(): Observable<McpTool[]> {
    return this.http.get<McpTool[]>(this.apiUrl);
  }

  listServerTools(serverId: string): Observable<McpTool[]> {
    return this.http.get<McpTool[]>(`${this.apiUrl}/servers/${serverId}`);
  }

  listLocalTools(): Observable<McpTool[]> {
    return this.http.get<McpTool[]>(`${this.apiUrl}/local`);
  }

  executeTool(request: McpToolExecutionRequest): Observable<McpToolExecutionResult> {
    return this.http.post<McpToolExecutionResult>(`${this.apiUrl}/execute`, request);
  }
}
```

### Interfacce TypeScript

```typescript
// mcp.models.ts
export interface McpServer {
  id: string;
  name: string;
  description?: string;
  type: 'STDIO' | 'SSE';
  status: 'CONNECTED' | 'DISCONNECTED' | 'ERROR' | 'CONNECTING';
  config: McpServerConfig;
  createdAt: string;
  lastConnectedAt?: string;
}

export interface McpServerConfig {
  command?: string;
  args?: string[];
  url?: string;
  timeoutSeconds?: number;
  autoReconnect?: boolean;
}

export interface CreateMcpServerRequest {
  name: string;
  description?: string;
  type: 'STDIO' | 'SSE';
  command?: string;
  args?: string[];
  url?: string;
  timeoutSeconds?: number;
  autoReconnect?: boolean;
}

export interface McpTool {
  name: string;
  description: string;
  inputSchema?: string;
  serverId?: string;
  local: boolean;
}

export interface McpToolExecutionRequest {
  toolName: string;
  serverId: string;
  arguments: Record<string, unknown>;
}

export interface McpToolExecutionResult {
  toolName: string;
  result: unknown;
  success: boolean;
  errorMessage?: string;
  executionTimeMs: number;
}
```

### Esempio componente Angular

```typescript
// mcp-servers-list.component.ts
@Component({
  selector: 'app-mcp-servers-list',
  template: `
    <div class="mcp-servers">
      <h2>Server MCP registrati</h2>
      @for (server of servers$ | async; track server.id) {
        <div class="server-card" [class.connected]="server.status === 'CONNECTED'">
          <h3>{{ server.name }}</h3>
          <span class="badge">{{ server.status }}</span>
          <span class="type">{{ server.type }}</span>
          <button (click)="testConnection(server.id)">Test</button>
          <button (click)="remove(server.id)">Rimuovi</button>
        </div>
      }
    </div>
  `
})
export class McpServersListComponent implements OnInit {
  servers$ = this.mcpServerService.listAll();

  constructor(private mcpServerService: McpServerService) {}

  testConnection(serverId: string) {
    this.mcpServerService.testConnection(serverId).subscribe(
      server => console.log('Test result:', server.status)
    );
  }

  remove(serverId: string) {
    this.mcpServerService.remove(serverId).subscribe(
      () => this.servers$ = this.mcpServerService.listAll()
    );
  }
}
```

---

> **Navigazione documentazione:**
> - Precedente: [04-configurazione.md](04-configurazione.md)
> - Prossimo: [06-integrazione-agenti.md](06-integrazione-agenti.md)
> - Troubleshooting: [07-troubleshooting.md](07-troubleshooting.md)
