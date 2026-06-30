export const content = `# Guida Utente - Dashboard

## Accesso

Navigare a **Dashboard** dal sidebar oppure visitare \`http://localhost:4200/dashboard\`.

## Panoramica

La Dashboard fornisce una vista d'insieme dello stato del sistema e dei collegamenti rapidi alle funzioni principali. E' divisa in due sezioni: **indicatori di stato** e **azioni rapide**.

## Indicatori di stato

La parte superiore mostra 4 card informative disposte in una griglia:

### Stato API
- Mostra **UP** (verde) se il backend e' raggiungibile e funzionante
- Mostra **DOWN** (rosso) se il backend non risponde
- Dati caricati dall'endpoint \`/dashboard/health\`

### Documenti
- Numero totale di documenti caricati nel sistema
- Include documenti in tutti gli stati (in attesa, in elaborazione, indicizzati, falliti)

### Server MCP
- Numero di server MCP registrati
- Include server in qualsiasi stato di connessione

### Provider LLM
- Nome del provider LLM predefinito attualmente configurato (es: "Ollama")

## Azioni rapide

La parte inferiore mostra 4 card cliccabili per navigare velocemente alle funzioni principali:

| Azione               | Destinazione | Descrizione                                   |
|----------------------|--------------|-----------------------------------------------|
| **Nuova Chat**       | /chat        | Apre l'interfaccia di conversazione AI        |
| **Carica Documento** | /documents   | Vai alla gestione documenti per caricare file |
| **Ricerca RAG**      | /search      | Apre la ricerca semantica nei documenti       |
| **Gestione MCP**     | /mcp         | Vai alla configurazione dei server MCP        |

## Aggiornamento dati

Cliccando il pulsante **Aggiorna** nell'intestazione della pagina, tutti gli indicatori vengono ricaricati dal backend.
`;
