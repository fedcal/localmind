# Feature Modules

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09
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
8. [Riepilogo Feature](#8-riepilogo-feature)

---

## 1. Panoramica

Le feature di LocalMind sono organizzate come moduli autonomi all'interno della directory `src/app/features/`. Ogni feature e' caricata tramite lazy loading e contiene i propri componenti, servizi, modelli e stato. Non vengono utilizzati NgModule: tutti i componenti sono standalone.

### Struttura comune di una feature

```
features/{feature-name}/
+-- {feature-name}.routes.ts    # Route della feature
+-- models/                     # Interfacce TypeScript
+-- services/                   # Servizi HTTP
+-- state/                      # Store con Signals (opzionale)
+-- pages/                      # Componenti pagina (smart components)
+-- components/                 # Componenti UI (dumb components, opzionale)
```

### Livello di completezza

| Feature    | Stato           | Descrizione                              |
|------------|-----------------|------------------------------------------|
| Chat       | Implementata    | Feature piu' completa, con store Signals |
| Documents  | Implementata    | CRUD con status badge                    |
| Search     | Placeholder     | UI base, pipeline RAG in sviluppo        |
| Folders    | Placeholder     | UI base, CRUD pianificato                |
| Settings   | Placeholder     | UI base, configurazione pianificata      |
| Dashboard  | Implementata    | Health check con Signals reattivi        |

---

## 2. Chat (/chat)

La feature Chat e' la piu' completa del progetto e rappresenta l'interfaccia principale di interazione con i modelli LLM. Include gestione dello stato con Signals, chiamate API, rendering messaggi e selezione provider/modello.

### Struttura

```
features/chat/
+-- chat.routes.ts
+-- models/
|   +-- chat-message.model.ts
|   +-- chat-request.model.ts
|   +-- chat-response.model.ts
+-- services/
|   +-- chat.service.ts
+-- state/
|   +-- chat.store.ts
+-- pages/
    +-- chat-page/
        +-- chat-page.component.ts
        +-- chat-page.component.html
        +-- chat-page.component.scss
```

### Modelli

#### ChatMessage

```typescript
export interface ChatMessage {
  id: string;
  role: 'USER' | 'ASSISTANT';
  content: string;
  createdAt: string;
}
```

#### ChatRequest

```typescript
export interface ChatRequest {
  message: string;
  provider?: string;
  model?: string;
  conversationId?: string;
  temperature?: number;
  maxTokens?: number;
}
```

#### ChatResponse

```typescript
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
```

### ChatService

```typescript
@Injectable({ providedIn: 'root' })
export class ChatService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiBaseUrl}/chat`;

  chat(request: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(this.apiUrl, request);
  }
}
```

| Metodo | Endpoint            | Descrizione                             |
|--------|---------------------|-----------------------------------------|
| `chat` | `POST /api/v1/chat` | Invia un messaggio e riceve la risposta |

### ChatStore

Lo store della chat gestisce lo stato reattivo tramite Angular Signals. Vedere il documento `03-state-management-signals.md` per la documentazione dettagliata.

| Signal               | Tipo                     | Descrizione                          |
|----------------------|--------------------------|--------------------------------------|
| `messages`           | `Signal<ChatMessage[]>`  | Lista messaggi della conversazione   |
| `isLoading`          | `Signal<boolean>`        | Stato di caricamento                 |
| `selectedProvider`   | `Signal<string>`         | Provider LLM selezionato             |
| `selectedModel`      | `Signal<string>`         | Modello LLM selezionato              |
| `messageCount`       | `Signal<number>`         | Computed: numero messaggi            |
| `hasMessages`        | `Signal<boolean>`        | Computed: presenza messaggi          |

### ChatPageComponent

Il componente pagina della Chat e' il piu' complesso del progetto:

- **Lista messaggi**: rendering reattivo tramite `@for` con tracking per `msg.id`.
- **Indicatore di caricamento**: mostrato durante la generazione della risposta tramite `@if (isLoading())`.
- **Input messaggio**: campo di testo con `[(ngModel)]` e invio tramite `(keyup.enter)`.
- **Selezione provider**: dropdown per la selezione del provider LLM.
- **Selezione modello**: dropdown per la selezione del modello specifico.

#### Template (estratto)

```html
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
```

### Flusso di interazione

1. L'utente digita un messaggio nel campo di input.
2. Preme Invio o clicca il pulsante "Invia".
3. Il componente invoca `sendMessage()`:
   - Aggiunge il messaggio utente allo store (`addUserMessage`).
   - Imposta lo stato di caricamento (`setLoading(true)`).
   - Chiama `ChatService.chat()`.
4. Il template si aggiorna reattivamente:
   - Il messaggio utente appare nella lista.
   - L'indicatore di caricamento viene mostrato.
5. Alla ricezione della risposta:
   - Il messaggio assistente viene aggiunto allo store (`addAssistantMessage`).
   - Lo stato di caricamento viene disattivato (`setLoading(false)`).
   - L'indicatore scompare e appare la risposta.

---

## 3. Documents (/documents)

La feature Documents gestisce la visualizzazione e l'eliminazione dei documenti caricati nel sistema.

### Struttura

```
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
```

### Modello Document

```typescript
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
```

Il campo `status` e' definito come union type TypeScript, garantendo type safety nell'utilizzo dei valori di stato.

### DocumentService

```typescript
@Injectable({ providedIn: 'root' })
export class DocumentService {

  private http = inject(HttpClient);
  private apiUrl = `${environment.apiBaseUrl}/documents`;

  getDocuments(): Observable<Document[]> {
    return this.http.get<Document[]>(this.apiUrl);
  }

  deleteDocument(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

| Metodo            | Endpoint                       | Descrizione                    |
|-------------------|--------------------------------|--------------------------------|
| `getDocuments`    | `GET /api/v1/documents`        | Lista tutti i documenti        |
| `deleteDocument`  | `DELETE /api/v1/documents/{id}`| Elimina un documento           |

### DocumentListPageComponent

- **Griglia documenti**: visualizzazione a griglia o lista dei documenti caricati.
- **Status badge**: badge colorato in base allo stato del documento.
- **Azioni**: pulsante di eliminazione per ogni documento.
- **Refresh**: ricaricamento della lista al mount del componente.

#### Status badge

| Stato        | Colore   | Descrizione                    |
|--------------|----------|--------------------------------|
| `PENDING`    | Giallo   | In attesa di elaborazione      |
| `PROCESSING` | Blu      | Elaborazione in corso          |
| `INDEXED`    | Verde    | Indicizzato con successo       |
| `ERROR`      | Rosso    | Errore durante l'elaborazione  |

---

## 4. Search (/search)

La feature Search fornisce l'interfaccia per la ricerca semantica nei documenti indicizzati.

### Struttura

```
features/search/
+-- search.routes.ts
+-- pages/
    +-- search-page/
        +-- search-page.component.ts
        +-- search-page.component.html
        +-- search-page.component.scss
```

### SearchPageComponent

- **Barra di ricerca**: campo di testo per l'inserimento della query.
- **Risultati**: lista dei chunk documentali corrispondenti con punteggio di similarita'.
- **Stato attuale**: placeholder funzionale; la pipeline RAG completa e' in fase di sviluppo.

### Flusso previsto

1. L'utente inserisce una query di ricerca.
2. La query viene inviata a `POST /api/v1/documents/search`.
3. I risultati vengono visualizzati con:
   - Nome del documento sorgente.
   - Estratto del testo corrispondente.
   - Punteggio di similarita' (0.0 - 1.0).
   - Indice del chunk nel documento originale.

---

## 5. Folders (/folders)

La feature Folders gestisce la configurazione delle cartelle locali monitorate dal sistema per la scansione automatica di nuovi documenti.

### Struttura

```
features/folders/
+-- folders.routes.ts
+-- pages/
    +-- folder-config-page/
        +-- folder-config-page.component.ts
        +-- folder-config-page.component.html
        +-- folder-config-page.component.scss
```

### FolderConfigPageComponent

- **Lista cartelle**: visualizzazione delle cartelle configurate con stato di monitoraggio.
- **Configurazione**: toggle per scansione ricorsiva e monitoraggio in tempo reale.
- **Stato attuale**: placeholder; le operazioni CRUD per la configurazione delle cartelle sono pianificate.

### Funzionalita' pianificate

| Funzionalita'             | Descrizione                                         |
|---------------------------|-----------------------------------------------------|
| Aggiunta cartella         | Selezione percorso e configurazione opzioni         |
| Rimozione cartella        | Eliminazione dalla lista di monitoraggio            |
| Toggle ricorsivita'       | Abilitazione/disabilitazione scansione sottocartelle|
| Toggle monitoraggio       | Abilitazione/disabilitazione watch in tempo reale   |
| Scansione manuale         | Trigger manuale della scansione per singola cartella|

---

## 6. Settings (/settings)

La feature Settings fornisce l'interfaccia per la configurazione dei provider LLM e delle API key.

### Struttura

```
features/settings/
+-- settings.routes.ts
+-- pages/
    +-- settings-page/
        +-- settings-page.component.ts
        +-- settings-page.component.html
        +-- settings-page.component.scss
```

### SettingsPageComponent

- **Provider configuration**: sezione per ogni provider LLM supportato.
- **API key management**: campi per l'inserimento delle API key (OpenAI, Anthropic).
- **Stato attuale**: placeholder; la gestione delle impostazioni e' pianificata.

### Funzionalita' pianificate

| Funzionalita'           | Descrizione                                         |
|-------------------------|-----------------------------------------------------|
| Configurazione Ollama   | URL base, modello di default, stato connessione     |
| Configurazione OpenAI   | API key, modello di default, abilitazione           |
| Configurazione Anthropic| API key, modello di default, abilitazione           |
| Provider di default     | Selezione del provider predefinito                  |
| Parametri di generazione| Temperature, max tokens di default                  |

---

## 7. Dashboard (/dashboard)

La feature Dashboard fornisce una visione d'insieme sullo stato di salute dei servizi e sulle statistiche di utilizzo.

### Struttura

```
features/dashboard/
+-- dashboard.routes.ts
+-- pages/
    +-- dashboard-page/
        +-- dashboard-page.component.ts
        +-- dashboard-page.component.html
        +-- dashboard-page.component.scss
```

### DashboardPageComponent

Il componente Dashboard utilizza Signals reattivi per mostrare lo stato dei servizi:

```typescript
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

    this.http.get<HealthStatus>(`${environment.apiBaseUrl}/dashboard/health`)
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
```

### Interfaccia HealthStatus

```typescript
interface HealthStatus {
  status: string;
  services: Record<string, string>;
}
```

### Funzionalita' implementate

| Funzionalita'           | Descrizione                                         |
|-------------------------|-----------------------------------------------------|
| Health check            | Chiamata `GET /api/v1/dashboard/health`             |
| Stato servizi           | Visualizzazione con badge colorati (UP/DOWN)        |
| Refresh manuale         | Pulsante per ricaricare lo stato                    |

### Funzionalita' pianificate

| Funzionalita'           | Descrizione                                         |
|-------------------------|-----------------------------------------------------|
| Statistiche documenti   | Conteggio per stato (pending, indexed, error)       |
| Statistiche utilizzo LLM| Token consumati, costi, latenza media per provider  |
| Grafici temporali       | Utilizzo nel tempo                                  |

---

## 8. Riepilogo Feature

| Feature    | Path        | Componente principale         | Servizio            | Store        | API Endpoint             |
|------------|-------------|-------------------------------|---------------------|--------------|--------------------------|
| Chat       | `/chat`     | `ChatPageComponent`           | `ChatService`       | `ChatStore`  | `POST /chat`             |
| Documents  | `/documents`| `DocumentListPageComponent`   | `DocumentService`   | -            | `GET/DELETE /documents`  |
| Search     | `/search`   | `SearchPageComponent`         | -                   | -            | `POST /documents/search` |
| Folders    | `/folders`  | `FolderConfigPageComponent`   | -                   | -            | (pianificato)            |
| Settings   | `/settings` | `SettingsPageComponent`       | -                   | -            | (pianificato)            |
| Dashboard  | `/dashboard`| `DashboardPageComponent`      | -                   | -            | `GET /dashboard/health`  |
