export const content = `# Guida Utente - MCP Integration

## Accesso

Navigare a **MCP** dal sidebar oppure visitare \`http://localhost:4200/mcp\`.

## Panoramica

La sezione MCP (Model Context Protocol) permette di registrare server MCP esterni e di esplorare ed eseguire i tool che questi server mettono a disposizione. MCP e' un protocollo standard per l'integrazione di tool e risorse con modelli AI.

L'interfaccia e' organizzata in due tab:
- **Server Esterni**: registrazione e gestione dei server MCP
- **Tool Disponibili**: esplorazione ed esecuzione dei tool

---

## Tab: Server Esterni

### Registrare un server MCP

1. Cliccare **+ Aggiungi Server**
2. Compilare il form di registrazione:

#### Campi comuni

| Campo | Obbligatorio | Descrizione |
|-------|-------------|-------------|
| **Nome** | Si | Nome identificativo del server |
| **Descrizione** | No | Descrizione delle funzionalita' del server |
| **Tipo** | Si | Tipo di connessione: STDIO o SSE |
| **Timeout** | No | Timeout in secondi (5-300, predefinito 30) |
| **Auto-reconnect** | No | Checkbox per la riconnessione automatica (predefinito: attivo) |

#### Campi specifici per tipo STDIO

| Campo | Obbligatorio | Descrizione | Esempio |
|-------|-------------|-------------|---------|
| **Comando** | Si | Comando da eseguire | \`npx\` |
| **Argomenti** | No | Argomenti separati da virgola | \`-y,@modelcontextprotocol/server-filesystem,./\` |

#### Campi specifici per tipo SSE

| Campo | Obbligatorio | Descrizione | Esempio |
|-------|-------------|-------------|---------|
| **URL** | Si | URL dell'endpoint SSE del server | \`http://localhost:8082/sse\` |

3. Cliccare **Registra** per salvare

### Lista server registrati

Ogni server viene mostrato come card con:

- **Nome** del server
- **Badge stato** con codifica colore:
  - **CONNECTED** (verde): connesso e funzionante
  - **DISCONNECTED** (grigio): disconnesso
  - **ERROR** (rosso): errore di connessione
  - **CONNECTING** (arancione): tentativo di connessione in corso
- **Descrizione** (se presente)
- **Badge tipo**: STDIO o SSE
- **Comando** o **URL** (in base al tipo, font monospace)
- **Argomenti** (se presenti, font monospace)

### Azioni sui server

| Azione | Descrizione |
|--------|-------------|
| **Test** | Verifica la connettivita' con il server. Mostra spinner durante il test e notifica con l'esito. |
| **Riconnetti** | Tenta la riconnessione al server. Mostra spinner durante il tentativo. |
| **Elimina** | Rimuove il server dalla configurazione (pulsante rosso). |

---

## Tab: Tool Disponibili

### Sezione Tool Locali

Mostra i tool esposti direttamente da LocalMind (implementati nel backend). Ogni tool e' presentato come card con:
- **Nome** del tool (font monospace)
- **Descrizione** della funzionalita'
- **Badge** "Locale" (blu)

### Sezione Tool Esterni

Mostra i tool disponibili sui server MCP connessi. Ogni tool mostra:
- **Nome** del tool (font monospace)
- **Descrizione** della funzionalita'
- **Badge** "Esterno" (viola)
- **Server ID** abbreviato (primi 8 caratteri)

Se non ci sono tool esterni, viene mostrato il suggerimento: "Connetti un server MCP per visualizzare i tool esterni."

### Eseguire un tool

1. Cliccare sulla card del tool da eseguire (si evidenzia con bordo e ombra)
2. Nella sezione che appare in basso vengono mostrati:
   - **Nome** e **descrizione** del tool
   - **Schema di input**: la struttura JSON degli argomenti richiesti (in formato JSON, font monospace, area scrollabile)
3. Compilare il campo **Argomenti** in formato JSON (textarea di 4 righe)
4. Cliccare **Esegui**

### Risultati dell'esecuzione

Dopo l'esecuzione, nella card vengono mostrati:
- **Badge stato**: verde "success" o rosso "error"
- **Tempo di esecuzione** in millisecondi
- **Messaggio di errore** (solo in caso di fallimento)
- **Dati risultato**: output del tool in formato JSON (sfondo scuro, font monospace, area scrollabile)

### Aggiornamento lista tool

Il pulsante **Aggiorna** nell'intestazione ricarica la lista di tutti i tool disponibili (locali ed esterni).
`;
