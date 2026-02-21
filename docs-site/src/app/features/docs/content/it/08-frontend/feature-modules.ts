export const content = `# Feature Modules

**Progetto:** LocalMind
**Versione:** 1.0.0
**Data:** 2026-02-18
**Framework:** Angular 21.0.0

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Chat (/chat)](#2-chat-chat)
3. [Documents (/documents)](#3-documents-documents)
4. [Search (/search)](#4-search-search)
5. [Folders (/folders)](#5-folders-folders)
6. [Settings (/settings)](#6-settings-settings)
7. [Dashboard (/dashboard)](#7-dashboard-dashboard)
8. [MCP (/mcp)](#8-mcp-mcp)
9. [Webhooks (/settings/webhooks)](#9-webhooks-settingswebhooks)
10. [Guide (/guide)](#10-guide-guide)
11. [Riepilogo Feature](#11-riepilogo-feature)

---

## 1. Panoramica

Le feature di LocalMind sono organizzate come moduli autonomi all'interno della directory \`src/app/features/\`. Ogni feature e' caricata tramite lazy loading e contiene i propri componenti, servizi, modelli e stato. Non vengono utilizzati NgModule: tutti i componenti sono standalone.

### Struttura comune di una feature

\`\`\`
features/{feature-name}/
+-- {feature-name}.routes.ts    # Route della feature
+-- models/                     # Interfacce TypeScript
+-- services/                   # Servizi HTTP
+-- state/                      # Store con Signals (opzionale)
+-- pages/                      # Componenti pagina (smart components)
+-- components/                 # Componenti UI (dumb components, opzionale)
\`\`\`

### Livello di completezza

| Feature    | Stato           | Descrizione                                                              |
|------------|-----------------|--------------------------------------------------------------------------|
| Chat       | Implementata    | Feature completa con store Signals, streaming SSE, modalita' Simple/Advanced |
| Documents  | Implementata    | CRUD con upload, status badge, eliminazione                              |
| Search     | Implementata    | Ricerca semantica con pipeline RAG                                       |
| Folders    | Implementata    | CRUD cartelle monitorate, trigger sync, form validation                  |
| Settings   | Implementata    | CRUD provider LLM, Ollama model management, form validation             |
| Dashboard  | Implementata    | Health check, statistiche, quick actions                                 |
| MCP        | Implementata    | Servers, tools, dashboard, scrum board, incidents, time tracking         |
| Webhooks   | Implementata    | CRUD webhook, test, event types                                          |
| Guide      | Implementata    | Guida utente interattiva                                                 |

---

## 2. Chat (/chat)

La feature Chat e' la piu' completa del progetto e rappresenta l'interfaccia principale di interazione con i modelli LLM. Include gestione dello stato con Signals, streaming SSE in tempo reale, modalita' Simple/Advanced, chiamate API, rendering messaggi e selezione provider/modello.

### Struttura

\`\`\`
features/chat/
+-- chat.routes.ts
+-- models/
|   +-- chat.model.ts
+-- services/
|   +-- chat.service.ts
|   +-- chat-stream.service.ts    # SSE streaming
+-- state/
|   +-- chat.store.ts
+-- pages/
    +-- chat-page/
        +-- chat-page.component.ts
+-- components/
    +-- chat-sidebar/
        +-- chat-sidebar.component.ts
\`\`\`

### Modelli

#### ChatMessage

\`\`\`typescript
export interface ChatMessage {
  id: string;
  role: 'USER' | 'ASSISTANT';
  content: string;
  createdAt: string;
}
\`\`\`

#### ChatRequest

\`\`\`typescript
export interface ChatRequest {
  message: string;
  provider?: string;
  model?: string;
  conversationId?: string;
  temperature?: number;
  maxTokens?: number;
}
\`\`\`

#### ChatResponse

\`\`\`typescript
export interface ChatResponse {
  content: string;
  model: string;
  provider: string;
  tokenUsage?: {
    promptTokens: number;
    completionTokens: number;
    totalTokens: number;
  };
  latencyMs: number;
}
\`\`\`

### ChatService

\`\`\`typescript
@Injectable({ providedIn: 'root' })
export class ChatService {

  private http = inject(HttpClient);
  private apiUrl = \`\${environment.apiBaseUrl}/chat\`;

  chat(request: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(this.apiUrl, request);
  }
}
\`\`\`

| Metodo | Endpoint            | Descrizione                             |
|--------|---------------------|-----------------------------------------|
| \`chat\` | \`POST /api/v1/chat\` | Invia un messaggio e riceve la risposta |

### ChatStreamService

Gestisce lo streaming SSE tramite \`fetch()\` + \`ReadableStream\`. Parse manuale degli eventi SSE (linee \`event:\` e \`data:\`). Ritorna un Observable di eventi tipizzati (conversation, token, metadata, done, error).

| Metodo          | Endpoint                    | Descrizione                                    |
|-----------------|-----------------------------|------------------------------------------------|
| \`chatStream\`    | \`POST /api/v1/chat/stream\`  | Invia un messaggio e riceve la risposta in streaming SSE |

Il servizio apre una connessione SSE verso il backend. Ogni evento contiene un tipo (\`event:\`) e un payload JSON (\`data:\`). I tipi di evento gestiti sono:

- **conversation**: contiene l'ID della conversazione appena creata o esistente.
- **token**: singolo token generato dal modello LLM, appeso progressivamente al messaggio.
- **metadata**: informazioni sull'utilizzo dei token, latenza e provider.
- **done**: segnale di fine streaming.
- **error**: errore dal backend durante la generazione.

### ChatStore

Lo store della chat gestisce lo stato reattivo tramite Angular Signals. Vedere il documento \`03-state-management-signals.md\` per la documentazione dettagliata.

| Signal                     | Tipo                              | Descrizione                                      |
|----------------------------|-----------------------------------|--------------------------------------------------|
| \`messages\`                 | \`Signal<ChatMessage[]>\`           | Lista messaggi della conversazione               |
| \`isLoading\`                | \`Signal<boolean>\`                 | Stato di caricamento                             |
| \`isStreaming\`              | \`Signal<boolean>\`                 | Streaming SSE in corso                           |
| \`selectedProvider\`         | \`Signal<string>\`                  | Provider LLM selezionato                         |
| \`selectedModel\`            | \`Signal<string>\`                  | Modello LLM selezionato                          |
| \`currentConversationId\`    | \`Signal<string\\|null>\`            | ID conversazione corrente                        |
| \`currentConversationTitle\` | \`Signal<string\\|null>\`            | Titolo conversazione corrente                    |
| \`conversations\`            | \`Signal<ConversationSummary[]>\`   | Lista conversazioni                              |
| \`advancedMode\`             | \`Signal<boolean>\`                 | Modalita' avanzata (persistente localStorage)    |
| \`toolCallingEnabled\`       | \`Signal<boolean>\`                 | Tool calling abilitato                           |
| \`ragEnabled\`               | \`Signal<boolean>\`                 | RAG abilitato                                    |
| \`currentSystemPrompt\`      | \`Signal<string\\|null>\`            | System prompt corrente                           |
| \`messageCount\`             | \`Signal<number>\`                  | Computed: numero messaggi                        |

### Simple/Advanced Mode

Toggle persistente (localStorage \`localmind-chat-mode\`) che nasconde in modalita' Simple: System Prompt, Tool Calling, RAG, Context Window. In entrambe le modalita' rimangono visibili: Provider selector, Model selector, input messaggio.

La modalita' viene inizializzata al caricamento leggendo il valore da localStorage. Il toggle aggiorna sia il signal \`advancedMode\` nello store sia il valore persistito. Questo consente all'utente di mantenere la propria preferenza tra sessioni diverse.

### ChatPageComponent

Il componente pagina della Chat e' il piu' complesso del progetto:

- **Lista messaggi**: rendering reattivo tramite \`@for\` con tracking per \`msg.id\`.
- **Indicatore di caricamento**: mostrato durante la generazione della risposta tramite \`@if (isLoading())\`.
- **Cursore lampeggiante**: durante lo streaming SSE, un cursore animato indica la generazione in corso.
- **Input messaggio**: campo di testo con \`[(ngModel)]\` e invio tramite \`(keyup.enter)\`.
- **Selezione provider**: dropdown per la selezione del provider LLM.
- **Selezione modello**: dropdown per la selezione del modello specifico.
- **Sidebar conversazioni**: componente laterale per la lista delle conversazioni salvate.

#### Template (estratto)

\`\`\`html
<div class="chat-container">
  <!-- Header con selezione provider/modello -->
  <div class="chat-header">
    <select [(ngModel)]="selectedProviderValue"
            (ngModelChange)="onProviderChange($event)">
      <option value="OLLAMA">Ollama</option>
      <option value="OPENAI">OpenAI</option>
      <option value="ANTHROPIC">Anthropic</option>
    </select>
  </div>

  <!-- Lista messaggi -->
  <div class="messages-container">
    @for (msg of messages(); track msg.id) {
      <div class="message" [class]="msg.role.toLowerCase()">
        <div class="role-badge">{{ msg.role }}</div>
        <div class="content">{{ msg.content }}</div>
      </div>
    }

    @if (isLoading()) {
      <div class="message assistant loading">
        <div class="typing-indicator">
          <span></span><span></span><span></span>
        </div>
      </div>
    }
  </div>

  <!-- Input area -->
  <div class="input-area">
    <input
      type="text"
      [(ngModel)]="userInput"
      (keyup.enter)="sendMessage()"
      placeholder="Scrivi un messaggio..."
      [disabled]="isLoading()"
    />
    <button (click)="sendMessage()" [disabled]="isLoading() || !userInput.trim()">
      Invia
    </button>
  </div>
</div>
\`\`\`

### Flusso di interazione

1. L'utente digita un messaggio nel campo di input.
2. Preme Invio o clicca il pulsante "Invia".
3. Il componente invoca \`sendMessage()\`:
   - Aggiunge il messaggio utente allo store (\`addUserMessage\`).
   - Imposta lo stato di caricamento (\`setLoading(true)\`).
   - Chiama \`ChatStreamService.chatStream()\` per avviare lo streaming SSE.
4. Il template si aggiorna reattivamente:
   - Il messaggio utente appare nella lista.
   - Un messaggio assistente vuoto viene creato nello store (\`addStreamingAssistantMessage\`).
5. Durante lo streaming:
   - Ogni token ricevuto viene appeso al messaggio assistente (\`appendTokenToLastMessage\`).
   - Il testo della risposta appare progressivamente, token per token.
   - Un cursore lampeggiante indica la generazione in corso.
   - L'auto-scroll mantiene visibile l'ultimo contenuto generato.
6. Al completamento dello streaming (evento \`done\`):
   - Lo stato di streaming viene disattivato (\`setStreaming(false)\`).
   - Il cursore lampeggiante scompare.
   - I metadati (token usage, latenza) vengono aggiornati se disponibili.

---

## 3. Documents (/documents)

La feature Documents gestisce la visualizzazione, l'upload e l'eliminazione dei documenti caricati nel sistema.

### Struttura

\`\`\`
features/documents/
+-- documents.routes.ts
+-- models/
|   +-- document.model.ts
+-- services/
|   +-- document.service.ts
+-- pages/
    +-- document-list-page/
        +-- document-list-page.component.ts
        +-- document-list-page.component.html
        +-- document-list-page.component.scss
\`\`\`

### Modello Document

\`\`\`typescript
export interface Document {
  id: string;
  filename: string;
  filePath: string;
  mimeType: string;
  fileSize: number;
  status: 'PENDING' | 'PROCESSING' | 'INDEXED' | 'ERROR';
  createdAt: string;
  indexedAt: string | null;
}
\`\`\`

Il campo \`status\` e' definito come union type TypeScript, garantendo type safety nell'utilizzo dei valori di stato.

### DocumentService

\`\`\`typescript
@Injectable({ providedIn: 'root' })
export class DocumentService {

  private http = inject(HttpClient);
  private apiUrl = \`\${environment.apiBaseUrl}/documents\`;

  getDocuments(): Observable<Document[]> {
    return this.http.get<Document[]>(this.apiUrl);
  }

  uploadDocument(file: File): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Document>(this.apiUrl, formData);
  }

  deleteDocument(id: string): Observable<void> {
    return this.http.delete<void>(\`\${this.apiUrl}/\${id}\`);
  }
}
\`\`\`

| Metodo            | Endpoint                       | Descrizione                    |
|-------------------|--------------------------------|--------------------------------|
| \`getDocuments\`    | \`GET /api/v1/documents\`        | Lista tutti i documenti        |
| \`uploadDocument\`  | \`POST /api/v1/documents\`       | Upload di un nuovo documento   |
| \`deleteDocument\`  | \`DELETE /api/v1/documents/{id}\`| Elimina un documento           |

### DocumentListPageComponent

- **Griglia documenti**: visualizzazione a griglia o lista dei documenti caricati.
- **Upload**: caricamento di nuovi documenti tramite selezione file.
- **Status badge**: badge colorato in base allo stato del documento.
- **Azioni**: pulsante di eliminazione per ogni documento.
- **Refresh**: ricaricamento della lista al mount del componente.

#### Status badge

| Stato        | Colore   | Descrizione                    |
|--------------|----------|--------------------------------|
| \`PENDING\`    | Giallo   | In attesa di elaborazione      |
| \`PROCESSING\` | Blu      | Elaborazione in corso          |
| \`INDEXED\`    | Verde    | Indicizzato con successo       |
| \`ERROR\`      | Rosso    | Errore durante l'elaborazione  |

---

## 4. Search (/search)

La feature Search fornisce l'interfaccia per la ricerca semantica nei documenti indicizzati tramite la pipeline RAG.

### Struttura

\`\`\`
features/search/
+-- search.routes.ts
+-- models/
|   +-- search.model.ts
+-- services/
|   +-- search.service.ts
+-- pages/
    +-- search-page/
        +-- search-page.component.ts
        +-- search-page.component.html
        +-- search-page.component.scss
\`\`\`

### SearchService

| Metodo   | Endpoint                         | Descrizione                                |
|----------|----------------------------------|--------------------------------------------|
| \`search\` | \`POST /api/v1/documents/search\` | Ricerca semantica nei documenti indicizzati |

### SearchPageComponent

- **Barra di ricerca**: campo di testo per l'inserimento della query.
- **Risultati**: lista dei chunk documentali corrispondenti con punteggio di similarita'.
- **Dettagli risultato**: per ogni risultato vengono mostrati il nome del documento sorgente, l'estratto del testo corrispondente, il punteggio di similarita' (0.0 - 1.0) e l'indice del chunk nel documento originale.

### Flusso di interazione

1. L'utente inserisce una query di ricerca.
2. La query viene inviata a \`POST /api/v1/documents/search\`.
3. I risultati vengono visualizzati con:
   - Nome del documento sorgente.
   - Estratto del testo corrispondente.
   - Punteggio di similarita' (0.0 - 1.0).
   - Indice del chunk nel documento originale.

---

## 5. Folders (/folders)

La feature Folders gestisce la configurazione delle cartelle locali monitorate dal sistema per la scansione automatica di nuovi documenti. Include operazioni CRUD complete, trigger di sincronizzazione manuale e form validation.

### Struttura

\`\`\`
features/folders/
+-- folders.routes.ts
+-- models/
|   +-- folder.model.ts
+-- services/
|   +-- folder.service.ts
+-- pages/
    +-- folder-config-page/
        +-- folder-config-page.component.ts
        +-- folder-config-page.component.html
        +-- folder-config-page.component.scss
\`\`\`

### FolderService

| Metodo            | Endpoint                       | Descrizione                                  |
|-------------------|--------------------------------|----------------------------------------------|
| \`getFolders\`      | \`GET /api/v1/folders\`          | Lista tutte le cartelle monitorate           |
| \`addFolder\`       | \`POST /api/v1/folders\`         | Aggiunge una nuova cartella da monitorare    |
| \`deleteFolder\`    | \`DELETE /api/v1/folders/{id}\`  | Rimuove una cartella dal monitoraggio        |
| \`syncFolder\`      | \`POST /api/v1/folders/{id}/sync\` | Trigger manuale di sincronizzazione        |

### FolderConfigPageComponent

- **Lista cartelle**: visualizzazione delle cartelle configurate con stato di monitoraggio.
- **Aggiunta cartella**: form con validazione per l'inserimento del percorso (required, minlength=2).
- **Configurazione**: toggle per scansione ricorsiva e monitoraggio in tempo reale.
- **Trigger sync**: pulsante per avviare manualmente la scansione di una singola cartella.
- **Eliminazione**: rimozione di una cartella dalla lista di monitoraggio.

### Form Validation

Il form di aggiunta cartella utilizza Reactive Forms con le seguenti regole di validazione:

| Campo        | Validazione         | Messaggio di errore                       |
|--------------|---------------------|-------------------------------------------|
| \`path\`       | \`required\`          | "Il percorso e' obbligatorio"             |
| \`path\`       | \`minlength(2)\`      | "Il percorso deve avere almeno 2 caratteri" |

I messaggi di errore vengono mostrati in tempo reale sotto il campo quando la validazione fallisce e il campo e' stato toccato (\`touched\`).

### Funzionalita' implementate

| Funzionalita'             | Descrizione                                         |
|---------------------------|-----------------------------------------------------|
| Aggiunta cartella         | Selezione percorso e configurazione opzioni         |
| Rimozione cartella        | Eliminazione dalla lista di monitoraggio            |
| Toggle ricorsivita'       | Abilitazione/disabilitazione scansione sottocartelle|
| Toggle monitoraggio       | Abilitazione/disabilitazione watch in tempo reale   |
| Scansione manuale         | Trigger manuale della scansione per singola cartella|
| Form validation           | Validazione percorso con messaggi di errore reattivi|

---

## 6. Settings (/settings)

La feature Settings fornisce l'interfaccia per la configurazione dei provider LLM, la gestione dei modelli Ollama e delle API key. Include operazioni CRUD complete e form validation.

### Struttura

\`\`\`
features/settings/
+-- settings.routes.ts
+-- models/
|   +-- settings.model.ts
|   +-- webhook.model.ts
+-- services/
|   +-- settings.service.ts
|   +-- webhook.service.ts
+-- pages/
    +-- settings-page/
    |   +-- settings-page.component.ts
    +-- webhooks-page/
        +-- webhooks-page.component.ts
\`\`\`

### SettingsService

Il servizio gestisce le operazioni CRUD per i provider LLM e la discovery dei modelli Ollama.

| Metodo              | Endpoint                                          | Descrizione                                 |
|---------------------|---------------------------------------------------|---------------------------------------------|
| \`getProviders\`      | \`GET /api/v1/settings/providers\`                  | Lista tutti i provider configurati          |
| \`createProvider\`    | \`POST /api/v1/settings/providers\`                 | Crea un nuovo provider                      |
| \`updateProvider\`    | \`PUT /api/v1/settings/providers/{id}\`             | Aggiorna un provider esistente              |
| \`deleteProvider\`    | \`DELETE /api/v1/settings/providers/{id}\`          | Elimina un provider                         |
| \`getOllamaModels\`   | \`GET /api/v1/settings/providers/ollama/models\`   | Lista i modelli scaricati su Ollama         |

### SettingsPageComponent

- **Lista provider**: visualizzazione dei provider LLM configurati (Ollama, OpenAI, Anthropic, Google).
- **CRUD provider**: creazione, modifica e eliminazione dei provider con form validation.
- **Ollama model management**: dropdown per la selezione del modello tra quelli scaricati su Ollama, con refresh della lista.
- **API key management**: campi per l'inserimento delle API key (OpenAI, Anthropic, Google).
- **Abilitazione/disabilitazione**: toggle per abilitare o disabilitare ciascun provider.

### Form Validation

Il form di configurazione dei provider utilizza Reactive Forms con le seguenti regole:

| Campo          | Validazione         | Messaggio di errore                          |
|----------------|---------------------|----------------------------------------------|
| \`name\`         | \`required\`          | "Il nome e' obbligatorio"                    |
| \`name\`         | \`minlength(2)\`      | "Il nome deve avere almeno 2 caratteri"      |
| \`apiKey\`       | \`required\` (cloud)  | "La API key e' obbligatoria"                 |
| \`baseUrl\`      | \`required\` (Ollama) | "L'URL base e' obbligatorio"                 |
| \`model\`        | \`required\`          | "Il modello e' obbligatorio"                 |

---

## 7. Dashboard (/dashboard)

La feature Dashboard fornisce una visione d'insieme sullo stato di salute dei servizi, sulle statistiche di utilizzo e azioni rapide.

### Struttura

\`\`\`
features/dashboard/
+-- dashboard.routes.ts
+-- pages/
    +-- dashboard-page/
        +-- dashboard-page.component.ts
        +-- dashboard-page.component.html
        +-- dashboard-page.component.scss
\`\`\`

### DashboardPageComponent

Il componente Dashboard utilizza Signals reattivi per mostrare lo stato dei servizi:

\`\`\`typescript
@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.scss'
})
export class DashboardPageComponent implements OnInit {

  private http = inject(HttpClient);

  // Signals per lo stato dei servizi
  healthStatus = signal<HealthStatus | null>(null);
  isLoading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadHealth();
  }

  loadHealth(): void {
    this.isLoading.set(true);
    this.error.set(null);

    this.http.get<HealthStatus>(\`\${environment.apiBaseUrl}/dashboard/health\`)
      .subscribe({
        next: (status) => {
          this.healthStatus.set(status);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.error.set('Impossibile contattare il backend');
          this.isLoading.set(false);
        }
      });
  }
}
\`\`\`

### Interfaccia HealthStatus

\`\`\`typescript
interface HealthStatus {
  status: string;
  services: Record<string, string>;
}
\`\`\`

### Funzionalita' implementate

| Funzionalita'           | Descrizione                                         |
|-------------------------|-----------------------------------------------------|
| Health check            | Chiamata \`GET /api/v1/dashboard/health\`             |
| Stato servizi           | Visualizzazione con badge colorati (UP/DOWN)        |
| Statistiche             | Conteggio documenti, conversazioni, provider attivi |
| Quick actions           | Link rapidi alle sezioni principali dell'applicazione |
| Refresh manuale         | Pulsante per ricaricare lo stato                    |

---

## 8. MCP (/mcp)

La feature MCP (Model Context Protocol) gestisce l'integrazione con server MCP esterni, l'esecuzione di tool, e fornisce una dashboard operativa con funzionalita' di project management integrate.

### Struttura

\`\`\`
features/mcp/
+-- mcp.routes.ts
+-- pages/
    +-- mcp-servers.component.ts
    +-- mcp-tools.component.ts
    +-- mcp-dashboard.component.ts
    +-- mcp-scrum.component.ts
    +-- mcp-incidents.component.ts
    +-- mcp-time.component.ts
\`\`\`

### Sotto-pagine

| Componente                | Path               | Descrizione                                                |
|---------------------------|--------------------|------------------------------------------------------------|
| \`McpServersComponent\`     | \`/mcp/servers\`     | Gestione dei server MCP: aggiunta, rimozione, stato connessione |
| \`McpToolsComponent\`       | \`/mcp/tools\`       | Catalogo dei tool disponibili dai server MCP connessi, esecuzione tool |
| \`McpDashboardComponent\`   | \`/mcp/dashboard\`   | Dashboard operativa con panoramica dello stato dei server e dei tool |
| \`McpScrumComponent\`       | \`/mcp/scrum\`       | Scrum board per la gestione di sprint, user stories e task |
| \`McpIncidentsComponent\`   | \`/mcp/incidents\`   | Gestione degli incidenti con tracking dello stato e priorita' |
| \`McpTimeComponent\`        | \`/mcp/time\`        | Time tracking per la registrazione del tempo lavorato su task e attivita' |

### McpService

| Metodo            | Endpoint                       | Descrizione                                    |
|-------------------|--------------------------------|------------------------------------------------|
| \`getServers\`      | \`GET /api/v1/mcp/servers\`      | Lista tutti i server MCP configurati           |
| \`addServer\`       | \`POST /api/v1/mcp/servers\`     | Aggiunge un nuovo server MCP                   |
| \`deleteServer\`    | \`DELETE /api/v1/mcp/servers/{id}\` | Rimuove un server MCP                       |
| \`getTools\`        | \`GET /api/v1/mcp/tools\`        | Lista tutti i tool disponibili                 |
| \`executeTool\`     | \`POST /api/v1/mcp/tools/execute\` | Esegue un tool MCP con parametri forniti     |

---

## 9. Webhooks (/settings/webhooks)

La feature Webhooks consente di configurare endpoint HTTP da notificare in risposta a eventi specifici dell'applicazione.

### Struttura

\`\`\`
features/settings/
+-- pages/
    +-- webhooks-page/
        +-- webhooks-page.component.ts
\`\`\`

### WebhookService

| Metodo              | Endpoint                         | Descrizione                              |
|---------------------|----------------------------------|------------------------------------------|
| \`getWebhooks\`       | \`GET /api/v1/webhooks\`          | Lista tutti i webhook configurati        |
| \`createWebhook\`     | \`POST /api/v1/webhooks\`         | Crea un nuovo webhook                    |
| \`updateWebhook\`     | \`PUT /api/v1/webhooks/{id}\`     | Aggiorna un webhook esistente            |
| \`deleteWebhook\`     | \`DELETE /api/v1/webhooks/{id}\`  | Elimina un webhook                       |
| \`testWebhook\`       | \`POST /api/v1/webhooks/{id}/test\` | Invia un evento di test al webhook     |

### WebhooksPageComponent

- **Lista webhook**: visualizzazione dei webhook configurati con stato (attivo/inattivo).
- **CRUD webhook**: creazione, modifica e eliminazione con form validation.
- **Event types**: selezione degli eventi da monitorare per ogni webhook.
- **Test webhook**: invio di un evento di test per verificare la corretta configurazione dell'endpoint.

---

## 10. Guide (/guide)

La feature Guide fornisce una guida utente interattiva integrata nell'applicazione. Non richiede servizi backend.

### Struttura

\`\`\`
features/guide/
+-- guide.routes.ts
+-- pages/
    +-- guide-page/
        +-- guide-page.component.ts
\`\`\`

### GuidePageComponent

- **Guida interattiva**: contenuti di aiuto organizzati per sezione dell'applicazione.
- **Navigazione**: link rapidi alle diverse sezioni della guida.
- **Funzionamento offline**: la guida e' interamente client-side, non richiede connessione al backend.

---

## 11. Riepilogo Feature

| Feature    | Path                 | Componente principale                    | Servizio                              | Store        | API Endpoint                          |
|------------|----------------------|------------------------------------------|---------------------------------------|--------------|---------------------------------------|
| Chat       | \`/chat\`              | \`ChatPageComponent\`                      | \`ChatService\`, \`ChatStreamService\`    | \`ChatStore\`  | \`POST /chat\`, \`POST /chat/stream\`     |
| Documents  | \`/documents\`         | \`DocumentListPageComponent\`              | \`DocumentService\`                     | -            | \`GET/POST/DELETE /documents\`          |
| Search     | \`/search\`            | \`SearchPageComponent\`                    | \`SearchService\`                       | -            | \`POST /documents/search\`             |
| Folders    | \`/folders\`           | \`FolderConfigPageComponent\`              | \`FolderService\`                       | -            | \`GET/POST/DELETE /folders\`            |
| Settings   | \`/settings\`          | \`SettingsPageComponent\`                  | \`SettingsService\`                     | -            | \`GET/POST/DELETE /settings/providers\` |
| Webhooks   | \`/settings/webhooks\` | \`WebhooksPageComponent\`                  | \`WebhookService\`                      | -            | \`GET/POST/PUT/DELETE /webhooks\`       |
| Dashboard  | \`/dashboard\`         | \`DashboardPageComponent\`                 | \`DashboardService\`                    | -            | \`GET /dashboard/health\`              |
| MCP        | \`/mcp\`               | \`McpServersComponent\`, etc.              | \`McpService\`                          | -            | \`/mcp/servers\`, \`/mcp/tools\`          |
| Guide      | \`/guide\`             | \`GuidePageComponent\`                     | -                                     | -            | -                                     |
`;
