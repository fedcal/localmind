export const content = `# Guida Utente - Impostazioni Provider LLM

## Accesso

Navigare a **Impostazioni** dal sidebar oppure visitare \`http://localhost:4200/settings\`.

## Panoramica

La sezione Impostazioni permette di configurare i provider LLM (Large Language Model) utilizzati dal sistema. E' possibile aggiungere piu' provider, ciascuno con le proprie credenziali e il modello predefinito. Le API key vengono salvate in modo persistente nel database.

## Aggiungere un provider

1. Cliccare **+ Aggiungi Provider** nell'intestazione
2. Compilare il form "Nuovo Provider LLM"

### Campi del form

#### Nome (obbligatorio)
Nome descrittivo per il provider (es: "Ollama Locale", "OpenAI Produzione").

#### Tipo (obbligatorio)
Selezionare il tipo di provider dal menu a tendina:

| Tipo | Descrizione | Richiede API Key |
|------|-------------|------------------|
| **Ollama** | Modelli LLM locali tramite Ollama | No |
| **OpenAI** | GPT-4, GPT-4o, GPT-3.5 e altri modelli OpenAI | Si |
| **Anthropic** | Claude e altri modelli Anthropic | Si |
| **Google Gemini** | Gemini Pro, Flash e altri modelli Google | Si |

Cambiando il tipo, l'URL base e i placeholder si aggiornano automaticamente.

#### URL Base (obbligatorio)
L'URL del servizio del provider. Viene pre-compilato in base al tipo selezionato:

| Tipo | URL predefinito |
|------|----------------|
| Ollama | \`http://localhost:11434\` |
| OpenAI | \`https://api.openai.com\` |
| Anthropic | \`https://api.anthropic.com\` |
| Google Gemini | \`https://generativelanguage.googleapis.com\` |

Per Ollama, se il servizio e' su un altro host o porta, modificare l'URL di conseguenza.

#### API Key (obbligatorio per provider cloud)
Il campo API Key appare solo per i provider non-Ollama. Inserire la chiave API:
- **OpenAI**: formato \`sk-...\`
- **Anthropic**: formato \`sk-ant-...\`
- **Google**: formato \`AIza...\`

Il campo e' di tipo password (il testo viene mascherato). La chiave viene salvata nel database.

#### Modello Predefinito

Il comportamento di questo campo cambia in base al tipo di provider:

**Per Ollama (dropdown dinamico):**
- Il campo mostra un **menu a tendina** popolato automaticamente con i modelli scaricati sull'istanza Ollama
- La lista viene caricata automaticamente alla selezione del tipo Ollama o alla modifica dell'URL base
- Un pulsante di **aggiornamento** (icona freccia circolare) accanto al dropdown permette di ricaricare la lista
- Durante il caricamento, il dropdown e' disabilitato e mostra uno spinner
- Se non vengono trovati modelli, appare il messaggio: "Nessun modello trovato. Verifica URL e che Ollama sia in esecuzione."
- Il primo modello della lista viene selezionato automaticamente

**Per OpenAI, Anthropic, Google (campo testo libero):**
- Campo di testo con placeholder suggerito:
  - OpenAI: \`gpt-4o-mini\`
  - Anthropic: \`claude-sonnet-4-20250514\`
  - Google: \`gemini-2.0-flash\`

### Validazione form

Il pulsante **Salva Provider** e' abilitato solo quando:
- Il campo Nome e' compilato
- Il campo URL Base e' compilato
- Per i provider cloud: il campo API Key e' compilato

## Lista provider configurati

I provider salvati vengono mostrati come card con le seguenti informazioni:

### Intestazione card
- **Icona tipo**: cerchio colorato con l'iniziale del provider
  - Ollama: verde
  - OpenAI: verde acqua
  - Anthropic: marrone chiaro
  - Google: blu
- **Nome** del provider
- **URL base** in font monospace
- **Badge stato**: "Attivo" (verde) o "Disattivato" (grigio)

### Dettagli
- **Tipo**: badge con il nome del tipo di provider
- **Modello default**: nome del modello predefinito (se configurato)
- **Modelli**: lista di tag con i modelli disponibili (se presenti)

### Azioni

| Azione | Descrizione |
|--------|-------------|
| **Test Connessione** | Verifica che il provider sia raggiungibile e funzionante. Per Ollama, mostra il numero di modelli disponibili. Per i provider cloud, verifica che l'API key sia configurata. |
| **Rimuovi** | Elimina il provider dalla configurazione |

Il pulsante Test Connessione mostra uno spinner durante il test. L'esito viene comunicato tramite notifica:
- **Successo**: "[nome]: Connessione riuscita"
- **Errore**: "[nome]: [messaggio di errore]"

## Notifiche

Le notifiche appaiono in alto nella pagina e scompaiono automaticamente dopo 4 secondi:
- **Verde** (successo): provider aggiunto, rimosso, o test riuscito
- **Rosso** (errore): errore nel salvataggio, rimozione, o test fallito

---

## Validazione dei Campi

Tutti i campi del form di configurazione provider hanno validazione inline:

| Campo | Validazione | Messaggio di errore |
|---|---|---|
| Nome provider | Obbligatorio, min. 2 caratteri | "Campo obbligatorio" / "Minimo 2 caratteri" |
| URL base | Obbligatorio | "Campo obbligatorio" |
| API Key | Obbligatorio, min. 10 caratteri | "Campo obbligatorio" / "Minimo 10 caratteri" |

I campi non validi mostrano un bordo rosso e un messaggio di errore sotto il campo quando l'utente li tocca. I campi validi mostrano un bordo verde.

---

## Webhooks

La sezione Webhooks (accessibile da Impostazioni > Webhooks nel sidebar) consente di configurare notifiche automatiche verso servizi esterni.

### Configurazione

Ogni webhook richiede:
- **Nome**: identificativo del webhook (min. 2 caratteri)
- **URL**: endpoint HTTP/HTTPS di destinazione
- **Tipo evento**: l'evento che triggera il webhook
- **Attivo**: stato attivo/inattivo

### Tipi di Evento

| Tipo | Descrizione |
|---|---|
| DOCUMENT_INDEXED | Un documento e' stato indicizzato con successo |
| DOCUMENT_FAILED | L'indicizzazione di un documento e' fallita |
| NEW_FILE | Un nuovo file e' stato rilevato in una cartella monitorata |
| CHAT_COMPLETED | Una conversazione chat e' stata completata |
| SCHEDULED | Evento pianificato |

### Azioni

- **Test**: invia una richiesta di test al webhook per verificare la connessione
- **Modifica**: modifica la configurazione del webhook
- **Elimina**: rimuove il webhook
- **Toggle attivo**: abilita/disabilita il webhook senza eliminarlo
`;
