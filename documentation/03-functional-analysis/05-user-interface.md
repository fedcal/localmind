# Functional Specification: User Interface

| Field        | Value                          |
|--------------|--------------------------------|
| **Document** | User Interface                 |
| **Version**  | 0.1.0                          |
| **Date**     | 2026-02-09                     |
| **Project**  | LocalMind                      |

---

## Table of Contents

1. [Technology Overview](#1-technology-overview)
2. [General Layout](#2-general-layout)
3. [Chat Section](#3-chat-section)
4. [Documents Section](#4-documents-section)
5. [Search Section](#5-search-section)
6. [Folders Section](#6-folders-section)
7. [Settings Section](#7-settings-section)
8. [Dashboard Section](#8-dashboard-section)
9. [Interface Modes](#9-interface-modes)

---

## 1. Technology Overview

The LocalMind user interface is built with the following technologies:

| Component           | Technology                        |
|---------------------|-----------------------------------|
| Framework           | Angular 21                        |
| Language            | TypeScript (strict mode)          |
| Architecture        | Standalone components             |
| State management    | Angular Signals                   |
| Styling             | SCSS with custom design system    |
| HTTP client         | Angular HttpClient                |
| Routing             | Angular Router (lazy loading)     |
| Build               | Angular CLI / esbuild             |

### 1.1 Design Principles

- **Component-based**: every interface element is a reusable Angular standalone component
- **Reactive**: application state is managed through Signals for granular and performant updates
- **Accessible**: WCAG 2.1 Level AA compliance for accessibility
- **Responsive**: adaptive layout for desktop and tablet (mobile as a future goal)

---

## 2. General Layout

The interface follows a two-column layout:

```
+------------------+----------------------------------------------+
|                  |                                              |
|    SIDEBAR       |              CONTENT AREA                    |
|    (navigation)  |              (current page)                  |
|                  |                                              |
|  +------------+  |                                              |
|  | Logo       |  |                                              |
|  +------------+  |                                              |
|  | Chat       |  |                                              |
|  | Documents  |  |                                              |
|  | Search     |  |                                              |
|  | Folders    |  |                                              |
|  | Settings   |  |                                              |
|  | Dashboard  |  |                                              |
|  +------------+  |                                              |
|                  |                                              |
|  +------------+  |                                              |
|  | Mode       |  |                                              |
|  | [Simple]   |  |                                              |
|  +------------+  |                                              |
|                  |                                              |
+------------------+----------------------------------------------+
```

### 2.1 Sidebar

- **Width**: 260px (collapsible to 64px with icons)
- **Background color**: #1a1a2e (dark theme)
- **Text color**: #e0e0e0
- **Accent color**: #4f46e5 (indigo)
- **Elements**: logo, navigation items with icons, mode selector
- **Behavior**: fixed on desktop, overlay on tablet

### 2.2 Content Area

- **Background**: #f8f9fa (light) or #121212 (dark mode)
- **Padding**: 24px
- **Header**: page title + breadcrumb + contextual actions
- **Content**: specific to each section

---

## 3. Chat Section

The Chat section is the main interface for interaction with LLMs and AI agents.

### 3.1 Layout

```
+-------------------------------------------------+
| Chat [Provider: Ollama v] [Model: llama3.2 v]   |
+-------------------------------------------------+
|                                                 |
|    +----------------------------------------+   |
|    | [System] Hello! How can I help you?    |   |
|    +----------------------------------------+   |
|                                                 |
|    +----------------------------------------+   |
|    | [User] Explain the hexagonal            |   |
|    | architecture                            |   |
|    +----------------------------------------+   |
|                                                 |
|    +----------------------------------------+   |
|    | [Assistant] The hexagonal architecture  |   |
|    | is an architectural pattern...          |   |
|    |                                        |   |
|    | Sources: doc1.pdf (p.12), doc2.pdf (p.3)|   |
|    +----------------------------------------+   |
|                                                 |
|    +----------------------------------------+   |
|    | [Loading...]                ///         |  |
|    +----------------------------------------+   |
|                                                 |
+-------------------------------------------------+
| [Agent: Tech v]  [Message...          ] [>]     |
+-------------------------------------------------+
```

### 3.2 Components

| Component              | Description                                          |
|------------------------|------------------------------------------------------|
| **Header**             | Provider and model selection (dropdown)              |
| **Message list**       | Scrollable list of messages (user/assistant)         |
| **Message bubble**     | Single message with avatar, text, timestamp          |
| **Citation block**     | RAG source citation block (document, page, score)    |
| **Loading indicator**  | Loading indicator during generation                  |
| **Input bar**          | Agent selection, text field, send button             |
| **Token counter**      | Token usage counter (advanced mode)                  |

### 3.3 Interactions

- **Send message**: button click or Enter key
- **Provider selection**: dropdown with enabled providers
- **Model selection**: dropdown with available models for the selected provider
- **Agent selection**: dropdown with available agents (Tech, Business, Legal, Personal)
- **Copy response**: copy button for each assistant message
- **Retry**: button to regenerate the last response

---

## 4. Documents Section

The Documents section displays uploaded documents and their indexing status.

### 4.1 Layout

```
+----------------------------------------------+
| Documents                    [Upload +]      |
+----------------------------------------------+
|                                              |
|   +----------+  +----------+  +----------+   |
|   | report   |  | contract |  | email    |   |
|   | .pdf     |  | .docx    |  | .eml     |   |
|   |          |  |          |  |          |   |
|   | 2.3 MB   |  | 156 KB   |  | 45 KB    |   |
|   | 12 chunks|  | 8 chunks |  | 3 chunks |   |
|   |          |  |          |  |          |   |
|   | [INDEXED]|  | [PENDING]|  | [FAILED] |   |
|   +----------+  +----------+  +----------+   |
|                                              |
|   +----------+  +----------+                 |
|   | manual   |  | slides   |                 |
|   | .txt     |  | .pdf     |                 |
|   |          |  |          |                 |
|   | 89 KB    |  | 5.1 MB   |                 |
|   | 15 chunks|  | PROCESSING|                |
|   |          |  |          |                 |
|   | [INDEXED]|  | [...]    |                 |
|   +----------+  +----------+                 |
|                                              |
+----------------------------------------------+
```

### 4.2 Status Badge

| Status      | Color      | Hex Code      | Icon         |
|-------------|------------|---------------|--------------|
| `PENDING`   | Yellow     | #f59e0b     | Clock        |
| `PROCESSING`| Blue       | #3b82f6     | Spinner      |
| `INDEXED`   | Green      | #10b981     | Checkmark    |
| `FAILED`    | Red        | #ef4444     | X            |
| `ARCHIVED`  | Gray       | #6b7280     | Archive      |

### 4.3 Actions

- **Upload**: dialog for file upload (drag-and-drop or file picker)
- **Document detail**: click on the card to view metadata, chunks, embedding info
- **Retry**: button to retry indexing of FAILED documents
- **Archive**: button to archive a document
- **Delete**: button to delete a document and its related chunks/embeddings

---

## 5. Search Section

The Search section provides an interface for semantic search within indexed documents.

### 5.1 Layout

```
+----------------------------------------------+
| Search                                       |
+----------------------------------------------+
|                                              |
| +------------------------------------------+ |
| | Search your documents...          [Search]| |
| +------------------------------------------+ |
|                                              |
| Results for: "withdrawal clause"             |
|                                              |
| +------------------------------------------+ |
| | contract.docx - Chunk 4 - Score: 0.92    | |
| | "...the party may withdraw from the      | |
| | contract with 30 days notice..."          | |
| +------------------------------------------+ |
|                                              |
| +------------------------------------------+ |
| | agreement.pdf - Chunk 12 - Score: 0.87   | |
| | "...unilateral withdrawal is permitted    | |
| | in the following cases..."               | |
| +------------------------------------------+ |
|                                              |
| +------------------------------------------+ |
| | policy.docx - Chunk 7 - Score: 0.74      | |
| | "...early withdrawal procedures..."      | |
| +------------------------------------------+ |
|                                              |
+----------------------------------------------+
```

### 5.2 Components

- **Search bar**: text field with search button and suggestions
- **Result**: card with file name, chunk index, similarity score, extracted text
- **Score indicator**: visual bar for the similarity score (0.0 - 1.0)
- **Filters** (advanced mode): top_k, similarity threshold, filter by format/date

---

## 6. Folders Section

The Folders section allows configuration of filesystem folders to index.

### 6.1 Layout

```
+----------------------------------------------+
| Folders                        [Add +]       |
+----------------------------------------------+
|                                              |
| +------------------------------------------+ |
| | /home/user/documents/work                | |
| | Recursive: Yes | Enabled: Yes            | |
| | Last scan: 2026-02-09 15:30              | |
| | Files found: 42 | Indexed: 38            | |
| | [Scan now] [Edit] [Remove]               | |
| +------------------------------------------+ |
|                                              |
| +------------------------------------------+ |
| | /home/user/documents/personal            | |
| | Recursive: No | Enabled: Yes             | |
| | Last scan: 2026-02-09 15:15              | |
| | Files found: 15 | Indexed: 15            | |
| | [Scan now] [Edit] [Remove]               | |
| +------------------------------------------+ |
|                                              |
+----------------------------------------------+
```

### 6.2 Features

- **Add folder**: dialog to specify path, recursiveness, enablement
- **Immediate scan**: manual trigger of a scan for a specific folder
- **Edit**: modify folder parameters (recursiveness, enablement)
- **Remove**: remove the folder from the monitoring list (does not delete files)
- **Statistics**: number of files found, indexed, in error

---

## 7. Settings Section

The Settings section allows configuration of LLM providers and API keys.

### 7.1 Layout

```
+----------------------------------------------+
| Settings                                     |
+----------------------------------------------+
|                                              |
| LLM Providers                                |
| +------------------------------------------+ |
| | Ollama          [Enabled: Yes]           | |
| | URL: http://localhost:11434              | |
| | Model: llama3.2                          | |
| | Status: Connected                        | |
| +------------------------------------------+ |
|                                              |
| +------------------------------------------+ |
| | OpenAI          [Enabled: No]            | |
| | API Key: sk-...****                      | |
| | Model: gpt-4o-mini                       | |
| | Status: Not configured                   | |
| +------------------------------------------+ |
|                                              |
| +------------------------------------------+ |
| | Anthropic       [Enabled: No]            | |
| | API Key: sk-ant-...****                  | |
| | Model: claude-3-5-sonnet                 | |
| | Status: Not configured                   | |
| +------------------------------------------+ |
|                                              |
| Default Provider: [Ollama v]                 |
| Fallback enabled: [Yes]                      |
| Fallback order: [OLLAMA, OPENAI, ...]        |
|                                              |
| [Save configuration]                         |
+----------------------------------------------+
```

### 7.2 Features

- **Provider enablement**: toggle to enable/disable each provider
- **API key**: masked field for API key entry (cloud providers)
- **Connection test**: button to verify connectivity to the provider
- **Default provider selection**: dropdown to choose the default provider
- **Fallback configuration**: toggle for enablement and fallback chain ordering
- **Advanced parameters** (advanced mode): timeout, default temperature, default max_tokens

---

## 8. Dashboard Section

The Dashboard section provides an overview of system status and usage metrics.

### 8.1 Layout

```
+----------------------------------------------+
| Dashboard                                    |
+----------------------------------------------+
|                                              |
| +--------+ +--------+ +--------+ +--------+  |
| |Postgres| |Qdrant  | |Ollama  | |n8n     |  |
| | UP     | | UP     | | UP     | | DOWN   |  |
| | 23ms   | | 15ms   | | 45ms   | | --     |  |
| +--------+ +--------+ +--------+ +--------+  |
|                                              |
| Usage Statistics                             |
| +------------------------------------------+ |
| | Total tokens: 1,234,567                  | |
| | Total cost: $2.45                        | |
| | Total requests: 342                      | |
| | Average latency: 1,230ms                 | |
| +------------------------------------------+ |
|                                              |
| Documents                                    |
| +------------------------------------------+ |
| | Total: 156                               | |
| | Indexed: 142 | Pending: 8                | |
| | Failed: 4 | Archived: 2                  | |
| +------------------------------------------+ |
|                                              |
| Batch Jobs                                   |
| +------------------------------------------+ |
| | Completed: 45 | Failed: 2               | |
| | Running: 1                               | |
| | Last job: 2026-02-09 15:30               | |
| +------------------------------------------+ |
|                                              |
+----------------------------------------------+
```

### 8.2 Real-Time Updates

The stat cards use Angular Signals for reactive updates. The frontend performs periodic polling (default: every 30 seconds) on the `/api/v1/dashboard/health` endpoint and updates the cards through signals.

---

## 9. Interface Modes

### 9.1 Simple Mode

Simplified interface with only essential features:

- Chat with agent selection (no technical parameters)
- Document upload with drag-and-drop
- Search with simple text bar
- Dashboard with basic health status
- No advanced configuration visible

### 9.2 Advanced Mode

Full interface with all configurable parameters:

- Chat with provider, model, temperature, max_tokens selection
- Visible RAG configuration (chunk size, overlap, similarity threshold, top_k)
- Detailed metrics (token usage, latency, cost per request)
- Accessible logs and diagnostics
- Full provider configuration

### 9.3 Role Presets

Predefined configurations that activate the optimal agent and parameters for the role:

| Preset      | Agent    | Temperature | Max Tokens | UI Mode  |
|-------------|----------|-------------|------------|----------|
| Developer   | Tech     | 0.2         | 4096       | Advanced |
| Business    | Business | 0.5         | 4096       | Simple   |
| Legal       | Legal    | 0.1         | 4096       | Simple   |
| Personal    | Personal | 0.7         | 2048       | Simple   |
