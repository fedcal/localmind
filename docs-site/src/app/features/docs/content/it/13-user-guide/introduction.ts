export const content = `# Guida Utente - Introduzione

## Panoramica dell'interfaccia

LocalMind si presenta come un'applicazione web single-page accessibile all'indirizzo \`http://localhost:4200\`. L'interfaccia e' organizzata con un **sidebar di navigazione laterale** sulla sinistra e un'**area di contenuto principale** sulla destra.

## Struttura della navigazione

Il sidebar contiene le seguenti voci, ciascuna corrispondente a una sezione del sistema:

| Voce             | Descrizione |
|------------------|-------------|
| **Dashboard**    | Panoramica dello stato del sistema e accesso rapido alle funzioni principali       |
| **Chat**         | Interfaccia di conversazione con i modelli LLM configurati                         |
| **Documenti**    | Gestione dei documenti caricati (upload, visualizzazione stato, eliminazione)      |
| **Ricerca**      | Ricerca semantica nei documenti indicizzati tramite RAG                            |
| **Cartelle**     | Configurazione delle cartelle locali da monitorare per l'indicizzazione automatica |
| **MCP**          | Gestione dei server e tool MCP (Model Context Protocol)                            |
| **Impostazioni** | Configurazione dei provider LLM (Ollama, OpenAI, Anthropic, Google Gemini)         |

### Sidebar comprimibile

Il sidebar puo' essere compresso cliccando il pulsante freccia in alto a destra del pannello. In modalita' compressa vengono mostrate solo le icone, liberando spazio per il contenuto principale. La versione del sistema (v0.1.0) e' visibile in fondo al sidebar.

## Primo avvio

Al primo accesso, l'applicazione redirige automaticamente alla pagina **Chat**. Per iniziare a utilizzare il sistema si consiglia di:

1. Verificare lo stato del sistema nella **Dashboard**
2. Configurare almeno un provider LLM nelle **Impostazioni**
3. Caricare dei documenti nella sezione **Documenti** o configurare delle **Cartelle** monitorate
4. Utilizzare la **Chat** per interagire con i modelli AI
5. Usare la **Ricerca** semantica per trovare informazioni nei documenti indicizzati
`;
