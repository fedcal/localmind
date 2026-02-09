# Guida Utente - Chat

## Accesso

Navigare a **Chat** dal sidebar oppure visitare `http://localhost:4200/chat`.

## Panoramica

La pagina Chat e' l'interfaccia principale per conversare con i modelli AI. Permette di selezionare il provider e il modello, inviare messaggi e ricevere risposte in tempo reale.

## Selezione provider e modello

Nella barra superiore sono presenti due menu a tendina:

### Provider
Selezionare il provider LLM da utilizzare tra quelli disponibili:
- **OLLAMA** - Modelli locali tramite Ollama
- **OPENAI** - Modelli OpenAI (richiede API key)
- **ANTHROPIC** - Modelli Anthropic Claude (richiede API key)

### Modello
Selezionare il modello specifico. Le opzioni predefinite includono:
- llama3.2, llama3.1, mistral, codellama, phi3, gemma2

> **Nota**: i modelli effettivamente disponibili dipendono da quelli scaricati su Ollama o dalla configurazione dei provider cloud.

### Nuova chat

Il pulsante **Nuova Chat** a destra nell'intestazione cancella la cronologia dei messaggi e avvia una nuova conversazione. Accanto viene mostrato il conteggio dei messaggi nella conversazione corrente.

## Area messaggi

### Stato iniziale (nessun messaggio)

Quando la chat e' vuota, viene mostrato un messaggio di benvenuto con tre suggerimenti cliccabili:
- "Spiega come funziona il RAG"
- "Riassumi i documenti caricati"
- "Quali modelli sono disponibili?"

Cliccando un suggerimento, il testo viene inviato automaticamente come messaggio.

### Messaggi nella conversazione

I messaggi vengono visualizzati in ordine cronologico:
- **Messaggi utente**: sfondo blu, allineati a destra, con icona avatar "U"
- **Messaggi assistente**: sfondo bianco con ombra, allineati a sinistra, con icona avatar "AI"

Il contenuto dei messaggi supporta formattazione base:
- **Testo in grassetto** (racchiuso tra `**`)
- `Codice inline` (racchiuso tra backtick singoli)
- Blocchi di codice (racchiusi tra tripli backtick) con sfondo scuro e font monospace

### Indicatore di caricamento

Quando il modello sta elaborando una risposta, viene mostrata un'animazione con tre punti pulsanti sotto l'ultimo messaggio.

### Errori

In caso di errore di connessione o del server, una barra rossa appare in fondo all'area messaggi con il testo dell'errore.

## Invio messaggi

### Area di input

In fondo alla pagina e' presente un campo di testo espandibile con placeholder "Scrivi un messaggio...".

### Comandi da tastiera
- **Invio** (`Enter`): invia il messaggio
- **Shift + Invio** (`Shift+Enter`): inserisce una nuova riga senza inviare

### Pulsante invio

Il pulsante con l'icona freccia a destra del campo di testo invia il messaggio. E' disabilitato (grigio) quando:
- Il campo di testo e' vuoto
- Una risposta e' gia' in fase di elaborazione (stato di caricamento attivo)
