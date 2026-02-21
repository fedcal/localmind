export const content = `# Feature Modules

**Project:** LocalMind
**Version:** 1.0.0
**Date:** 2026-02-18
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
8. [MCP (/mcp)](#8-mcp-mcp)
9. [Webhooks (/settings/webhooks)](#9-webhooks-settingswebhooks)
10. [Guide (/guide)](#10-guide-guide)
11. [Feature Summary](#11-feature-summary)

---

## 1. Overview

LocalMind features are organized as autonomous modules within the \`src/app/features/\` directory. Each feature is loaded via lazy loading and contains its own components, services, models, and state. No NgModules are used: all components are standalone.

### Common feature structure

\`\`\`
features/{feature-name}/
+-- {feature-name}.routes.ts    # Feature routes
+-- models/                     # TypeScript interfaces
+-- services/                   # HTTP services
+-- state/                      # Stores with Signals (optional)
+-- pages/                      # Page components (smart components)
+-- components/                 # UI components (dumb components, optional)
\`\`\`

### Completeness level

| Feature    | Status          | Description                                                              |
|------------|-----------------|--------------------------------------------------------------------------|
| Chat       | Implemented     | Full feature with Signals store, SSE streaming, Simple/Advanced mode     |
| Documents  | Implemented     | CRUD with upload, status badge, deletion                                 |
| Search     | Implemented     | Semantic search with RAG pipeline                                        |
| Folders    | Implemented     | CRUD for monitored folders, sync trigger, form validation                |
| Settings   | Implemented     | CRUD for LLM providers, Ollama model management, form validation         |
| Dashboard  | Implemented     | Health check, statistics, quick actions                                  |
| MCP        | Implemented     | Servers, tools, dashboard, scrum board, incidents, time tracking         |
| Webhooks   | Implemented     | CRUD for webhooks, test, event types                                     |
| Guide      | Implemented     | Interactive user guide                                                   |

---

## 2. Chat (/chat)

The Chat feature is the most complete in the project and represents the primary interface for interacting with LLM models. It includes state management with Signals, real-time SSE streaming, Simple/Advanced mode, API calls, message rendering, and provider/model selection.

### Structure

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

### Models

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

| Method | Endpoint            | Description                             |
|--------|---------------------|-----------------------------------------|
| \`chat\` | \`POST /api/v1/chat\` | Sends a message and receives a response |

### ChatStreamService

Manages SSE streaming via \`fetch()\` + \`ReadableStream\`. Manual parsing of SSE events (\`event:\` and \`data:\` lines). Returns an Observable of typed events (conversation, token, metadata, done, error).

| Method          | Endpoint                    | Description                                         |
|-----------------|-----------------------------|-----------------------------------------------------|
| \`chatStream\`    | \`POST /api/v1/chat/stream\`  | Sends a message and receives a streaming SSE response |

The service opens an SSE connection to the backend. Each event contains a type (\`event:\`) and a JSON payload (\`data:\`). The handled event types are:

- **conversation**: contains the ID of the newly created or existing conversation.
- **token**: single token generated by the LLM model, progressively appended to the message.
- **metadata**: information about token usage, latency, and provider.
- **done**: end-of-stream signal.
- **error**: error from the backend during generation.

### ChatStore

The chat store manages reactive state through Angular Signals. See the \`03-state-management-signals.md\` document for detailed documentation.

| Signal                     | Type                              | Description                                      |
|----------------------------|-----------------------------------|--------------------------------------------------|
| \`messages\`                 | \`Signal<ChatMessage[]>\`           | Conversation message list                        |
| \`isLoading\`                | \`Signal<boolean>\`                 | Loading state                                    |
| \`isStreaming\`              | \`Signal<boolean>\`                 | SSE streaming in progress                        |
| \`selectedProvider\`         | \`Signal<string>\`                  | Selected LLM provider                            |
| \`selectedModel\`            | \`Signal<string>\`                  | Selected LLM model                               |
| \`currentConversationId\`    | \`Signal<string\\|null>\`            | Current conversation ID                          |
| \`currentConversationTitle\` | \`Signal<string\\|null>\`            | Current conversation title                       |
| \`conversations\`            | \`Signal<ConversationSummary[]>\`   | Conversation list                                |
| \`advancedMode\`             | \`Signal<boolean>\`                 | Advanced mode (persisted in localStorage)        |
| \`toolCallingEnabled\`       | \`Signal<boolean>\`                 | Tool calling enabled                             |
| \`ragEnabled\`               | \`Signal<boolean>\`                 | RAG enabled                                      |
| \`currentSystemPrompt\`      | \`Signal<string\\|null>\`            | Current system prompt                            |
| \`messageCount\`             | \`Signal<number>\`                  | Computed: message count                          |

### Simple/Advanced Mode

Persistent toggle (localStorage \`localmind-chat-mode\`) that hides in Simple mode: System Prompt, Tool Calling, RAG, Context Window. In both modes the following remain visible: Provider selector, Model selector, message input.

The mode is initialized on load by reading the value from localStorage. The toggle updates both the \`advancedMode\` signal in the store and the persisted value. This allows the user to maintain their preference across different sessions.

### ChatPageComponent

The Chat page component is the most complex in the project:

- **Message list**: reactive rendering via \`@for\` with tracking by \`msg.id\`.
- **Loading indicator**: shown during response generation via \`@if (isLoading())\`.
- **Blinking cursor**: during SSE streaming, an animated cursor indicates ongoing generation.
- **Message input**: text field with \`[(ngModel)]\` and submission via \`(keyup.enter)\`.
- **Provider selection**: dropdown for selecting the LLM provider.
- **Model selection**: dropdown for selecting the specific model.
- **Conversation sidebar**: side component for the list of saved conversations.

#### Template (excerpt)

\`\`\`html
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
\`\`\`

### Interaction flow

1. The user types a message in the input field.
2. They press Enter or click the "Send" button.
3. The component invokes \`sendMessage()\`:
   - Adds the user message to the store (\`addUserMessage\`).
   - Sets the loading state (\`setLoading(true)\`).
   - Calls \`ChatStreamService.chatStream()\` to start SSE streaming.
4. The template reactively updates:
   - The user message appears in the list.
   - An empty assistant message is created in the store (\`addStreamingAssistantMessage\`).
5. During streaming:
   - Each received token is appended to the assistant message (\`appendTokenToLastMessage\`).
   - The response text appears progressively, token by token.
   - A blinking cursor indicates ongoing generation.
   - Auto-scroll keeps the latest generated content visible.
6. Upon streaming completion (\`done\` event):
   - The streaming state is deactivated (\`setStreaming(false)\`).
   - The blinking cursor disappears.
   - Metadata (token usage, latency) is updated if available.

---

## 3. Documents (/documents)

The Documents feature manages the display, upload, and deletion of documents uploaded to the system.

### Structure

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

### Document model

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

The \`status\` field is defined as a TypeScript union type, ensuring type safety when using status values.

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

| Method            | Endpoint                       | Description                    |
|-------------------|--------------------------------|--------------------------------|
| \`getDocuments\`    | \`GET /api/v1/documents\`        | Lists all documents            |
| \`uploadDocument\`  | \`POST /api/v1/documents\`       | Uploads a new document         |
| \`deleteDocument\`  | \`DELETE /api/v1/documents/{id}\`| Deletes a document             |

### DocumentListPageComponent

- **Document grid**: grid or list display of uploaded documents.
- **Upload**: uploading new documents via file selection.
- **Status badge**: colored badge based on document status.
- **Actions**: delete button for each document.
- **Refresh**: list reload on component mount.

#### Status badge

| Status       | Color    | Description                    |
|--------------|----------|--------------------------------|
| \`PENDING\`    | Yellow   | Awaiting processing            |
| \`PROCESSING\` | Blue     | Processing in progress         |
| \`INDEXED\`    | Green    | Successfully indexed           |
| \`ERROR\`      | Red      | Error during processing        |

---

## 4. Search (/search)

The Search feature provides the interface for semantic search across indexed documents through the RAG pipeline.

### Structure

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

| Method   | Endpoint                         | Description                                    |
|----------|----------------------------------|------------------------------------------------|
| \`search\` | \`POST /api/v1/documents/search\` | Semantic search across indexed documents        |

### SearchPageComponent

- **Search bar**: text field for entering the query.
- **Results**: list of matching document chunks with similarity score.
- **Result details**: for each result, the source document name, matching text excerpt, similarity score (0.0 - 1.0), and chunk index in the original document are displayed.

### Interaction flow

1. The user enters a search query.
2. The query is sent to \`POST /api/v1/documents/search\`.
3. Results are displayed with:
   - Source document name.
   - Matching text excerpt.
   - Similarity score (0.0 - 1.0).
   - Chunk index in the original document.

---

## 5. Folders (/folders)

The Folders feature manages the configuration of local folders monitored by the system for automatic scanning of new documents. It includes full CRUD operations, manual sync trigger, and form validation.

### Structure

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

| Method            | Endpoint                         | Description                                    |
|-------------------|----------------------------------|------------------------------------------------|
| \`getFolders\`      | \`GET /api/v1/folders\`            | Lists all monitored folders                    |
| \`addFolder\`       | \`POST /api/v1/folders\`           | Adds a new folder to monitor                   |
| \`deleteFolder\`    | \`DELETE /api/v1/folders/{id}\`    | Removes a folder from monitoring               |
| \`syncFolder\`      | \`POST /api/v1/folders/{id}/sync\` | Manual sync trigger                            |

### FolderConfigPageComponent

- **Folder list**: display of configured folders with monitoring status.
- **Add folder**: form with validation for path entry (required, minlength=2).
- **Configuration**: toggle for recursive scanning and real-time monitoring.
- **Sync trigger**: button to manually start scanning a single folder.
- **Deletion**: removal of a folder from the monitoring list.

### Form Validation

The add folder form uses Reactive Forms with the following validation rules:

| Field        | Validation          | Error message                              |
|--------------|---------------------|--------------------------------------------|
| \`path\`       | \`required\`          | "Path is required"                         |
| \`path\`       | \`minlength(2)\`      | "Path must be at least 2 characters long"  |

Error messages are displayed in real time below the field when validation fails and the field has been touched.

### Implemented features

| Feature                   | Description                                         |
|---------------------------|-----------------------------------------------------|
| Add folder                | Path selection and option configuration              |
| Remove folder             | Removal from the monitoring list                    |
| Recursion toggle          | Enable/disable subfolder scanning                   |
| Monitoring toggle         | Enable/disable real-time watching                   |
| Manual scan               | Manual scan trigger for a single folder             |
| Form validation           | Path validation with reactive error messages        |

---

## 6. Settings (/settings)

The Settings feature provides the interface for configuring LLM providers, managing Ollama models, and API keys. It includes full CRUD operations and form validation.

### Structure

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

The service manages CRUD operations for LLM providers and Ollama model discovery.

| Method              | Endpoint                                          | Description                                 |
|---------------------|---------------------------------------------------|---------------------------------------------|
| \`getProviders\`      | \`GET /api/v1/settings/providers\`                  | Lists all configured providers              |
| \`createProvider\`    | \`POST /api/v1/settings/providers\`                 | Creates a new provider                      |
| \`updateProvider\`    | \`PUT /api/v1/settings/providers/{id}\`             | Updates an existing provider                |
| \`deleteProvider\`    | \`DELETE /api/v1/settings/providers/{id}\`          | Deletes a provider                          |
| \`getOllamaModels\`   | \`GET /api/v1/settings/providers/ollama/models\`   | Lists downloaded Ollama models              |

### SettingsPageComponent

- **Provider list**: display of configured LLM providers (Ollama, OpenAI, Anthropic, Google).
- **Provider CRUD**: creation, editing, and deletion of providers with form validation.
- **Ollama model management**: dropdown for model selection among those downloaded on Ollama, with list refresh.
- **API key management**: fields for entering API keys (OpenAI, Anthropic, Google).
- **Enable/disable**: toggle to enable or disable each provider.

### Form Validation

The provider configuration form uses Reactive Forms with the following rules:

| Field          | Validation          | Error message                                |
|----------------|---------------------|----------------------------------------------|
| \`name\`         | \`required\`          | "Name is required"                           |
| \`name\`         | \`minlength(2)\`      | "Name must be at least 2 characters long"    |
| \`apiKey\`       | \`required\` (cloud)  | "API key is required"                        |
| \`baseUrl\`      | \`required\` (Ollama) | "Base URL is required"                       |
| \`model\`        | \`required\`          | "Model is required"                          |

---

## 7. Dashboard (/dashboard)

The Dashboard feature provides an overview of service health status, usage statistics, and quick actions.

### Structure

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

The Dashboard component uses reactive Signals to display service status:

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

    this.http.get<HealthStatus>(\`\${environment.apiBaseUrl}/dashboard/health\`)
      .subscribe({
        next: (status) => {
          this.healthStatus.set(status);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.error.set('Unable to contact the backend');
          this.isLoading.set(false);
        }
      });
  }
}
\`\`\`

### HealthStatus interface

\`\`\`typescript
interface HealthStatus {
  status: string;
  services: Record<string, string>;
}
\`\`\`

### Implemented features

| Feature                 | Description                                         |
|-------------------------|-----------------------------------------------------|
| Health check            | \`GET /api/v1/dashboard/health\` call                 |
| Service status          | Display with colored badges (UP/DOWN)               |
| Statistics              | Document count, conversations, active providers     |
| Quick actions           | Quick links to main application sections            |
| Manual refresh          | Button to reload status                             |

---

## 8. MCP (/mcp)

The MCP (Model Context Protocol) feature manages integration with external MCP servers, tool execution, and provides an operational dashboard with integrated project management capabilities.

### Structure

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

### Sub-pages

| Component                 | Path               | Description                                                    |
|---------------------------|--------------------|------------------------------------------------------------|
| \`McpServersComponent\`     | \`/mcp/servers\`     | MCP server management: adding, removing, connection status |
| \`McpToolsComponent\`       | \`/mcp/tools\`       | Catalog of available tools from connected MCP servers, tool execution |
| \`McpDashboardComponent\`   | \`/mcp/dashboard\`   | Operational dashboard with overview of server and tool status |
| \`McpScrumComponent\`       | \`/mcp/scrum\`       | Scrum board for managing sprints, user stories, and tasks  |
| \`McpIncidentsComponent\`   | \`/mcp/incidents\`   | Incident management with status and priority tracking      |
| \`McpTimeComponent\`        | \`/mcp/time\`        | Time tracking for recording time spent on tasks and activities |

### McpService

| Method            | Endpoint                          | Description                                    |
|-------------------|-----------------------------------|------------------------------------------------|
| \`getServers\`      | \`GET /api/v1/mcp/servers\`         | Lists all configured MCP servers               |
| \`addServer\`       | \`POST /api/v1/mcp/servers\`        | Adds a new MCP server                          |
| \`deleteServer\`    | \`DELETE /api/v1/mcp/servers/{id}\` | Removes an MCP server                          |
| \`getTools\`        | \`GET /api/v1/mcp/tools\`           | Lists all available tools                      |
| \`executeTool\`     | \`POST /api/v1/mcp/tools/execute\`  | Executes an MCP tool with provided parameters  |

---

## 9. Webhooks (/settings/webhooks)

The Webhooks feature allows configuring HTTP endpoints to be notified in response to specific application events.

### Structure

\`\`\`
features/settings/
+-- pages/
    +-- webhooks-page/
        +-- webhooks-page.component.ts
\`\`\`

### WebhookService

| Method              | Endpoint                           | Description                              |
|---------------------|------------------------------------|------------------------------------------|
| \`getWebhooks\`       | \`GET /api/v1/webhooks\`            | Lists all configured webhooks            |
| \`createWebhook\`     | \`POST /api/v1/webhooks\`           | Creates a new webhook                    |
| \`updateWebhook\`     | \`PUT /api/v1/webhooks/{id}\`       | Updates an existing webhook              |
| \`deleteWebhook\`     | \`DELETE /api/v1/webhooks/{id}\`    | Deletes a webhook                        |
| \`testWebhook\`       | \`POST /api/v1/webhooks/{id}/test\` | Sends a test event to the webhook        |

### WebhooksPageComponent

- **Webhook list**: display of configured webhooks with status (active/inactive).
- **Webhook CRUD**: creation, editing, and deletion with form validation.
- **Event types**: selection of events to monitor for each webhook.
- **Test webhook**: sending a test event to verify the correct configuration of the endpoint.

---

## 10. Guide (/guide)

The Guide feature provides an interactive user guide integrated within the application. It does not require backend services.

### Structure

\`\`\`
features/guide/
+-- guide.routes.ts
+-- pages/
    +-- guide-page/
        +-- guide-page.component.ts
\`\`\`

### GuidePageComponent

- **Interactive guide**: help content organized by application section.
- **Navigation**: quick links to different guide sections.
- **Offline operation**: the guide is entirely client-side and does not require a backend connection.

---

## 11. Feature Summary

| Feature    | Path                 | Main component                           | Service                               | Store        | API Endpoint                          |
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
