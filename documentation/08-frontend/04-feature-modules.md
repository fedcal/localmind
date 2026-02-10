# Feature Modules

**Project:** LocalMind
**Version:** 0.1.0
**Date:** 2026-02-09
**Framework:** Angular 21.0.0

---

## Table of Contents

1. [Overview](#1-overview)
2. [Chat (/chat)](#2-chat-chat)
3. [Documents (/documents)](#3-documents-documents)
4. [Search (/search)](#4-search-search)
5. [Folders (/folders)](#5-folders-folders)
6. [Settings (/settings)](#6-settings-settings)
7. [Dashboard (/dashboard)](#7-dashboard-dashboard)
8. [Feature Summary](#8-feature-summary)

---

## 1. Overview

LocalMind features are organized as autonomous modules within the `src/app/features/` directory. Each feature is loaded via lazy loading and contains its own components, services, models, and state. No NgModules are used: all components are standalone.

### Common feature structure

```
features/{feature-name}/
+-- {feature-name}.routes.ts    # Feature routes
+-- models/                     # TypeScript interfaces
+-- services/                   # HTTP services
+-- state/                      # Stores with Signals (optional)
+-- pages/                      # Page components (smart components)
+-- components/                 # UI components (dumb components, optional)
```

### Completeness level

| Feature    | Status          | Description                              |
|------------|-----------------|------------------------------------------|
| Chat       | Implemented     | Most complete feature, with Signals store|
| Documents  | Implemented     | CRUD with status badge                   |
| Search     | Placeholder     | Basic UI, RAG pipeline under development |
| Folders    | Placeholder     | Basic UI, CRUD planned                   |
| Settings   | Placeholder     | Basic UI, configuration planned          |
| Dashboard  | Implemented     | Health check with reactive Signals       |

---

## 2. Chat (/chat)

The Chat feature is the most complete in the project and represents the primary interface for interacting with LLM models. It includes state management with Signals, API calls, message rendering, and provider/model selection.

### Structure

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

### Models

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

| Method | Endpoint            | Description                             |
|--------|---------------------|-----------------------------------------|
| `chat` | `POST /api/v1/chat` | Sends a message and receives a response |

### ChatStore

The chat store manages reactive state through Angular Signals. See the `03-state-management-signals.md` document for detailed documentation.

| Signal               | Type                     | Description                          |
|----------------------|--------------------------|--------------------------------------|
| `messages`           | `Signal<ChatMessage[]>`  | Conversation message list            |
| `isLoading`          | `Signal<boolean>`        | Loading state                        |
| `selectedProvider`   | `Signal<string>`         | Selected LLM provider                |
| `selectedModel`      | `Signal<string>`         | Selected LLM model                   |
| `messageCount`       | `Signal<number>`         | Computed: message count              |
| `hasMessages`        | `Signal<boolean>`        | Computed: messages present           |

### ChatPageComponent

The Chat page component is the most complex in the project:

- **Message list**: reactive rendering via `@for` with tracking by `msg.id`.
- **Loading indicator**: shown during response generation via `@if (isLoading())`.
- **Message input**: text field with `[(ngModel)]` and submission via `(keyup.enter)`.
- **Provider selection**: dropdown for selecting the LLM provider.
- **Model selection**: dropdown for selecting the specific model.

#### Template (excerpt)

```html
<div class="chat-container">
  <!-- Header with provider/model selection -->
  <div class="chat-header">
    <select [(ngModel)]="selectedProviderValue"
            (ngModelChange)="onProviderChange($event)">
      <option value="OLLAMA">Ollama</option>
      <option value="OPENAI">OpenAI</option>
      <option value="ANTHROPIC">Anthropic</option>
    </select>
  </div>

  <!-- Message list -->
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

### Interaction flow

1. The user types a message in the input field.
2. They press Enter or click the "Send" button.
3. The component invokes `sendMessage()`:
   - Adds the user message to the store (`addUserMessage`).
   - Sets the loading state (`setLoading(true)`).
   - Calls `ChatService.chat()`.
4. The template reactively updates:
   - The user message appears in the list.
   - The loading indicator is shown.
5. Upon receiving the response:
   - The assistant message is added to the store (`addAssistantMessage`).
   - The loading state is deactivated (`setLoading(false)`).
   - The indicator disappears and the response appears.

---

## 3. Documents (/documents)

The Documents feature manages the display and deletion of documents uploaded to the system.

### Structure

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

### Document model

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

The `status` field is defined as a TypeScript union type, ensuring type safety when using status values.

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

| Method            | Endpoint                       | Description                    |
|-------------------|--------------------------------|--------------------------------|
| `getDocuments`    | `GET /api/v1/documents`        | Lists all documents            |
| `deleteDocument`  | `DELETE /api/v1/documents/{id}`| Deletes a document             |

### DocumentListPageComponent

- **Document grid**: grid or list display of uploaded documents.
- **Status badge**: colored badge based on document status.
- **Actions**: delete button for each document.
- **Refresh**: list reload on component mount.

#### Status badge

| Status       | Color    | Description                    |
|--------------|----------|--------------------------------|
| `PENDING`    | Yellow   | Awaiting processing            |
| `PROCESSING` | Blue     | Processing in progress         |
| `INDEXED`    | Green    | Successfully indexed           |
| `ERROR`      | Red      | Error during processing        |

---

## 4. Search (/search)

The Search feature provides the interface for semantic search across indexed documents.

### Structure

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

- **Search bar**: text field for entering the query.
- **Results**: list of matching document chunks with similarity score.
- **Current status**: functional placeholder; the complete RAG pipeline is under development.

### Expected flow

1. The user enters a search query.
2. The query is sent to `POST /api/v1/documents/search`.
3. Results are displayed with:
   - Source document name.
   - Matching text excerpt.
   - Similarity score (0.0 - 1.0).
   - Chunk index in the original document.

---

## 5. Folders (/folders)

The Folders feature manages the configuration of local folders monitored by the system for automatic scanning of new documents.

### Structure

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

- **Folder list**: display of configured folders with monitoring status.
- **Configuration**: toggle for recursive scanning and real-time monitoring.
- **Current status**: placeholder; CRUD operations for folder configuration are planned.

### Planned features

| Feature                   | Description                                         |
|---------------------------|-----------------------------------------------------|
| Add folder                | Path selection and option configuration              |
| Remove folder             | Removal from the monitoring list                    |
| Recursion toggle          | Enable/disable subfolder scanning                   |
| Monitoring toggle         | Enable/disable real-time watching                   |
| Manual scan               | Manual scan trigger for a single folder             |

---

## 6. Settings (/settings)

The Settings feature provides the interface for configuring LLM providers and API keys.

### Structure

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

- **Provider configuration**: section for each supported LLM provider.
- **API key management**: fields for entering API keys (OpenAI, Anthropic).
- **Current status**: placeholder; settings management is planned.

### Planned features

| Feature                 | Description                                         |
|-------------------------|-----------------------------------------------------|
| Ollama configuration    | Base URL, default model, connection status           |
| OpenAI configuration    | API key, default model, enablement                  |
| Anthropic configuration | API key, default model, enablement                  |
| Default provider        | Default provider selection                          |
| Generation parameters   | Default temperature, max tokens                     |

---

## 7. Dashboard (/dashboard)

The Dashboard feature provides an overview of service health status and usage statistics.

### Structure

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

The Dashboard component uses reactive Signals to display service status:

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

  // Signals for service status
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

### HealthStatus interface

```typescript
interface HealthStatus {
  status: string;
  services: Record<string, string>;
}
```

### Implemented features

| Feature                 | Description                                         |
|-------------------------|-----------------------------------------------------|
| Health check            | `GET /api/v1/dashboard/health` call                 |
| Service status          | Display with colored badges (UP/DOWN)               |
| Manual refresh          | Button to reload status                             |

### Planned features

| Feature                 | Description                                         |
|-------------------------|-----------------------------------------------------|
| Document statistics     | Count by status (pending, indexed, error)           |
| LLM usage statistics    | Tokens consumed, costs, average latency per provider|
| Time-series charts      | Usage over time                                     |

---

## 8. Feature Summary

| Feature    | Path        | Main component                | Service             | Store        | API Endpoint             |
|------------|-------------|-------------------------------|---------------------|--------------|--------------------------|
| Chat       | `/chat`     | `ChatPageComponent`           | `ChatService`       | `ChatStore`  | `POST /chat`             |
| Documents  | `/documents`| `DocumentListPageComponent`   | `DocumentService`   | -            | `GET/DELETE /documents`  |
| Search     | `/search`   | `SearchPageComponent`         | -                   | -            | `POST /documents/search` |
| Folders    | `/folders`  | `FolderConfigPageComponent`   | -                   | -            | (planned)                |
| Settings   | `/settings` | `SettingsPageComponent`       | -                   | -            | (planned)                |
| Dashboard  | `/dashboard`| `DashboardPageComponent`      | -                   | -            | `GET /dashboard/health`  |
