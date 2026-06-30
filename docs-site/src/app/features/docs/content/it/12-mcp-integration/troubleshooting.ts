export const content = `# Troubleshooting MCP in LocalMind

**Progetto:** LocalMind - Piattaforma AI Local-First  
**Versione:** 0.1.0  
**Ultimo aggiornamento:** 2026-02-09  
**Modulo di riferimento:** localmind-infrastructure (\`infrastructure.mcp\`)

---

## Indice

1. [Problemi comuni e soluzioni](#1-problemi-comuni-e-soluzioni)
2. [Logging e debug](#2-logging-e-debug)
3. [Verifica stato connessioni](#3-verifica-stato-connessioni)
4. [Test manuale con curl](#4-test-manuale-con-curl)
5. [Checklist di troubleshooting](#5-checklist-di-troubleshooting)

---

## 1. Problemi comuni e soluzioni

### 1.1 Server MCP non si connette (STDIO)

**Sintomo:** Lo status del server rimane \`DISCONNECTED\` o passa a \`ERROR\` dopo la registrazione.

**Cause possibili e soluzioni:**

| Causa                              | Diagnosi                                       | Soluzione                                      |
|------------------------------------|------------------------------------------------|------------------------------------------------|
| Comando non trovato                | Log: \`Cannot run program "npx"\`                | Verificare che il comando sia nel PATH         |
| Node.js non installato             | \`node --version\` restituisce errore            | Installare Node.js >= 18                       |
| Pacchetto npm non esistente        | Log: \`npm ERR! 404 Not Found\`                  | Verificare il nome del pacchetto               |
| Argomenti errati                   | Log: \`Error: invalid argument\`                 | Controllare gli args nel \`McpServerConfig\`     |
| Permessi insufficienti             | Log: \`Permission denied\`                       | Verificare i permessi del filesystem           |
| Processo termina immediatamente    | Log: \`Process exited with code 1\`              | Eseguire il comando manualmente per verificare |

**Verifica manuale del comando STDIO:**

\`\`\`bash
# Testare il comando manualmente prima di registrarlo
npx -y @modelcontextprotocol/server-filesystem /home/utente/documenti

# Se il server si avvia correttamente, mostrera' un messaggio di inizializzazione
# Ctrl+C per terminare
\`\`\`

**Verifica che npx sia nel PATH del processo Java:**

\`\`\`bash
# Verificare il PATH disponibile per Java
which npx
echo $PATH

# Se npx non e' nel PATH globale, specificare il percorso completo:
# command: "/usr/local/bin/npx" (o il percorso effettivo)
\`\`\`

### 1.2 Server MCP non si connette (SSE)

**Sintomo:** La connessione a un server SSE remoto fallisce.

| Causa                              | Diagnosi                                       | Soluzione                                      |
|------------------------------------|------------------------------------------------|------------------------------------------------|
| URL non raggiungibile              | Log: \`Connection refused\`                      | Verificare che il server sia attivo            |
| URL errato                         | Log: \`404 Not Found\`                           | Verificare l'URL (deve terminare con \`/sse\`)   |
| Firewall blocca la connessione     | Log: \`Connection timed out\`                    | Verificare regole firewall/proxy               |
| CORS non configurato               | Log: \`CORS error\` (solo da browser)            | Configurare CORS sul server MCP                |
| SSL/TLS mismatch                   | Log: \`SSL handshake failed\`                    | Verificare certificati SSL                     |

**Verifica manuale della connessione SSE:**

\`\`\`bash
# Testare l'endpoint SSE con curl
curl -v -N http://mcp-server.local:3001/sse

# Risposta attesa: stream di eventi SSE
# Content-Type: text/event-stream
# data: {"jsonrpc":"2.0",...}
\`\`\`

### 1.3 Tool discovery restituisce lista vuota

**Sintomo:** \`GET /api/v1/mcp/tools/servers/{serverId}\` restituisce \`[]\`.

**Cause possibili:**

1. **Server non connesso:** Verificare che lo status sia \`CONNECTED\`
2. **Server non supporta tool:** Alcuni server espongono solo risorse o prompt
3. **Implementazione in corso:** Il metodo \`McpClientConnection.getTools()\` contiene
   un \`// TODO\` e restituisce \`List.of()\` nella versione attuale

\`\`\`java
// SpringAiMcpClientAdapter.McpClientConnection
List<McpExternalTool> getTools() {
    // TODO: Use MCP SDK to discover tools from the connected server
    return List.of();
}
\`\`\`

**Soluzione:** Completare l'implementazione di \`getTools()\` nel \`McpClientConnection\`
utilizzando il MCP SDK per inviare \`tools/list\` al server.

### 1.4 Esecuzione tool fallisce con timeout

**Sintomo:** \`executeTool()\` restituisce \`success=false\` con errore di timeout.

**Soluzioni:**

\`\`\`bash
# Aumentare il timeout alla registrazione del server
curl -X POST http://localhost:8080/api/v1/mcp/servers \\
  -H "Content-Type: application/json" \\
  -d '{
    "name": "Server lento",
    "type": "SSE",
    "url": "http://server.local:3001/sse",
    "timeoutSeconds": 120
  }'
\`\`\`

**Nota:** Il timeout e' configurato per server in \`McpServerConfig.timeoutSeconds\`.
Il valore di default e' 30 secondi. Per operazioni lunghe (es. web scraping,
query su grandi dataset), aumentare a 60-120 secondi.

### 1.5 Errori di serializzazione JSON

**Sintomo:** L'esecuzione del tool restituisce un errore di parsing JSON.

**Cause comuni:**

1. **Tipi non compatibili:** Il risultato del tool contiene tipi Java non serializzabili
2. **Riferimenti circolari:** Oggetti con riferimenti circolari nel risultato
3. **Campi null non gestiti:** Jackson di default include i null

**Soluzione:** Verificare i log a livello DEBUG per vedere il payload JSON raw:

\`\`\`yaml
logging:
  level:
    io.modelcontextprotocol: TRACE
    org.springframework.ai.mcp: DEBUG
\`\`\`

---

## 2. Logging e debug

### 2.1 Configurazione logging per MCP

Aggiungere le seguenti property in \`application-dev.yml\` per il debug completo:

\`\`\`yaml
logging:
  level:
    # Log MCP SDK Java
    io.modelcontextprotocol: DEBUG

    # Log Spring AI MCP
    org.springframework.ai.mcp: DEBUG

    # Log adapter LocalMind
    com.localmind.infrastructure.mcp: DEBUG

    # Log domain service MCP
    com.localmind.domain.mcp: DEBUG

    # Log API controller MCP
    com.localmind.api.mcp: DEBUG
\`\`\`

### 2.2 Pattern di log per diagnostica

I messaggi di log seguono un pattern che facilita il filtraggio:

\`\`\`
# Connessione a server MCP
2026-02-09 10:30:01 [main] INFO  c.l.i.mcp.adapter.SpringAiMcpClientAdapter -
    Connecting to MCP server: Filesystem locale (STDIO)

# Connessione riuscita
2026-02-09 10:30:02 [main] INFO  c.l.i.mcp.adapter.SpringAiMcpClientAdapter -
    Connected to MCP server: Filesystem locale

# Connessione fallita
2026-02-09 10:30:02 [main] ERROR c.l.i.mcp.adapter.SpringAiMcpClientAdapter -
    Failed to connect to MCP server: Filesystem locale
    java.io.IOException: Cannot run program "npx": No such file or directory
\`\`\`

### 2.3 Filtraggio log

\`\`\`bash
# Filtrare solo i log MCP nei file di log
grep "mcp\\|MCP\\|McpServer\\|McpTool\\|McpClient" application.log

# Filtrare log di connessione
grep "Connecting to MCP\\|Connected to MCP\\|Failed to connect" application.log

# Filtrare log di esecuzione tool
grep "executeTool\\|tool execution\\|tools/call" application.log
\`\`\`

---

## 3. Verifica stato connessioni

### 3.1 Elenco server con stato

\`\`\`bash
curl -s http://localhost:8080/api/v1/mcp/servers | python3 -m json.tool
\`\`\`

**Interpretazione degli stati:**

| Stato          | Significato                                  | Azione                          |
|----------------|----------------------------------------------|---------------------------------|
| \`CONNECTED\`    | Connessione attiva e funzionante             | Nessuna azione necessaria       |
| \`DISCONNECTED\` | Non connesso (stato iniziale dopo errore)    | Tentare \`reconnect\`             |
| \`CONNECTING\`   | Connessione in corso                         | Attendere qualche secondo       |
| \`ERROR\`        | Errore di connessione                        | Verificare log, poi \`reconnect\` |

### 3.2 Test di connessione per un server specifico

\`\`\`bash
# Testa la connessione e aggiorna lo stato
curl -s -X POST http://localhost:8080/api/v1/mcp/servers/{serverId}/test | python3 -m json.tool

# Riconnetti un server in errore
curl -X POST http://localhost:8080/api/v1/mcp/servers/{serverId}/reconnect
\`\`\`

### 3.3 Verifica tool disponibili

\`\`\`bash
# Tutti i tool da tutti i server connessi
curl -s http://localhost:8080/api/v1/mcp/tools | python3 -m json.tool

# Tool di un server specifico
curl -s http://localhost:8080/api/v1/mcp/tools/servers/{serverId} | python3 -m json.tool

# Tool locali di LocalMind
curl -s http://localhost:8080/api/v1/mcp/tools/local | python3 -m json.tool
\`\`\`

---

## 4. Test manuale con curl

### 4.1 Ciclo completo di test

Script bash per un test completo del sistema MCP:

\`\`\`bash
#!/bin/bash
# test-mcp.sh - Test completo del sistema MCP di LocalMind
BASE_URL="http://localhost:8080/api/v1/mcp"

echo "=== 1. Registrazione server filesystem ==="
SERVER_ID=$(curl -s -X POST "$BASE_URL/servers" \\
  -H "Content-Type: application/json" \\
  -d '{
    "name": "Test Filesystem",
    "type": "STDIO",
    "command": "npx",
    "args": ["-y", "@modelcontextprotocol/server-filesystem", "/tmp"],
    "timeoutSeconds": 30
  }' | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
echo "Server ID: $SERVER_ID"

echo ""
echo "=== 2. Verifica stato server ==="
curl -s "$BASE_URL/servers/$SERVER_ID" | python3 -m json.tool

echo ""
echo "=== 3. Test connessione ==="
curl -s -X POST "$BASE_URL/servers/$SERVER_ID/test" | python3 -m json.tool

echo ""
echo "=== 4. Discovery tool ==="
curl -s "$BASE_URL/tools/servers/$SERVER_ID" | python3 -m json.tool

echo ""
echo "=== 5. Elenco tutti i tool (locali + esterni) ==="
curl -s "$BASE_URL/tools" | python3 -m json.tool
curl -s "$BASE_URL/tools/local" | python3 -m json.tool

echo ""
echo "=== 6. Esecuzione tool (list_directory) ==="
curl -s -X POST "$BASE_URL/tools/execute" \\
  -H "Content-Type: application/json" \\
  -d "{
    \\"toolName\\": \\"list_directory\\",
    \\"serverId\\": \\"$SERVER_ID\\",
    \\"arguments\\": { \\"path\\": \\"/tmp\\" }
  }" | python3 -m json.tool

echo ""
echo "=== 7. Pulizia - Rimozione server ==="
curl -s -X DELETE "$BASE_URL/servers/$SERVER_ID"
echo "Server $SERVER_ID rimosso"
\`\`\`

### 4.2 Test del server MCP di LocalMind (come server)

Per testare LocalMind come MCP server, verificare che l'endpoint SSE sia raggiungibile:

\`\`\`bash
# Verifica endpoint SSE del server MCP di LocalMind
curl -v -N http://localhost:8080/mcp/sse

# Invia un messaggio JSON-RPC di inizializzazione
curl -X POST http://localhost:8080/mcp/message \\
  -H "Content-Type: application/json" \\
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {},
      "clientInfo": {
        "name": "test-client",
        "version": "1.0.0"
      }
    }
  }'

# Elenco tool esposti da LocalMind
curl -X POST http://localhost:8080/mcp/message \\
  -H "Content-Type: application/json" \\
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }'
\`\`\`

---

## 5. Checklist di troubleshooting

Quando qualcosa non funziona, seguire questa checklist ordinata:

### Server MCP (STDIO)

- [ ] **Il backend e' avviato?** \`curl http://localhost:8080/actuator/health\`
- [ ] **MCP client e' abilitato?** Verificare \`localmind.mcp.client.enabled=true\` in \`application-dev.yml\`
- [ ] **Il comando e' installato?** Eseguire \`which npx\` (o il comando specificato)
- [ ] **Node.js e' installato?** \`node --version\` (richiesto >= 18)
- [ ] **Il comando funziona manualmente?** Eseguire il comando con gli stessi args
- [ ] **I permessi sono corretti?** Verificare permessi sulla directory target
- [ ] **Lo stato del server e' stato verificato?** \`GET /api/v1/mcp/servers\`
- [ ] **I log mostrano errori?** Controllare i log con livello DEBUG abilitato
- [ ] **E' stata tentata una riconnessione?** \`POST /api/v1/mcp/servers/{id}/reconnect\`

### Server MCP (SSE)

- [ ] **Il server remoto e' avviato?** \`curl -v http://server:porta/sse\`
- [ ] **L'URL e' corretto?** Verificare host, porta, e path (\`/sse\`)
- [ ] **La rete e' raggiungibile?** \`ping server-host\` o \`telnet server-host porta\`
- [ ] **Il firewall consente la connessione?** Verificare regole firewall
- [ ] **I certificati SSL sono validi?** (se HTTPS) \`curl -v https://server:porta/sse\`

### Tool MCP

- [ ] **Il server e' CONNECTED?** Verificare lo stato del server
- [ ] **La discovery funziona?** \`GET /api/v1/mcp/tools/servers/{serverId}\`
- [ ] **Gli argomenti sono corretti?** Verificare l'\`inputSchema\` del tool
- [ ] **Il timeout e' sufficiente?** Aumentare \`timeoutSeconds\` se necessario
- [ ] **Il risultato e' serializzabile?** Controllare i log per errori di serializzazione

### LocalMind come server MCP

- [ ] **MCP server e' abilitato?** Verificare \`localmind.mcp.server.enabled=true\`
- [ ] **L'endpoint SSE risponde?** \`curl -N http://localhost:8080/mcp/sse\`
- [ ] **L'endpoint message risponde?** \`curl -X POST http://localhost:8080/mcp/message\`
- [ ] **I bean dei tool sono caricati?** Verificare nei log che \`LocalMindMcpTools\` sia inizializzato
- [ ] **Il client MCP vede i tool?** Inviare \`tools/list\` via JSON-RPC

---

> **Navigazione documentazione:**
> - Precedente: [06-integrazione-agenti.md](/docs/12-mcp-integration/agent-integration)
> - Panoramica: [01-panoramica-protocollo-mcp.md](/docs/12-mcp-integration/mcp-protocol-overview)
> - Configurazione: [04-configurazione.md](/docs/12-mcp-integration/configuration)
`;
