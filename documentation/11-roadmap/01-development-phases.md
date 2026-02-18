# Development Phases

| | |
|---|---|
| **Document** | Roadmap - Development Phases |
| **Version** | 1.0.0 |
| **Date** | 2026-02-18 |
| **Project** | LocalMind |

---

## Table of Contents

1. [Overview](#1-overview)
2. [Phase 1: LLM Gateway + Ollama (COMPLETED)](#2-phase-1-llm-gateway--ollama-completed)
3. [Phase 2: File System Ingestion](#3-phase-2-file-system-ingestion)
4. [Phase 3: Base RAG](#4-phase-3-base-rag)
5. [Phase 4: Complete Angular UI](#5-phase-4-complete-angular-ui)
6. [Phase 5: n8n Automations](#6-phase-5-n8n-automations)
7. [Phase 6: AI Agents](#7-phase-6-ai-agents)
8. [Roadmap Summary](#8-roadmap-summary)

---

## 1. Overview

> **Note (February 2026)**: All 6 phases have been successfully completed. The project has met and exceeded its original goals, particularly Phase 6 (AI Agents/MCP) which produced 135+ native MCP tools across 14 categories, exceeding initial expectations by 135%.

The development of LocalMind follows an **incremental and progressive** approach, organized into 6 main phases. Each phase produces a set of functional and testable deliverables, progressively building on the foundations laid by previous phases.

### Guiding principles

| Principle | Description |
|---|---|
| **Vertical slicing** | Each phase produces complete end-to-end features (backend + frontend) |
| **Incrementality** | Each phase extends the previous ones without rewrites |
| **Testability** | Each phase has verifiable completion criteria |
| **Independence** | Phases can be released independently |

### Dependency diagram

```
Phase 1: LLM Gateway + Ollama
    │
    ├──► Phase 2: File System Ingestion
    │        │
    │        └──► Phase 3: Base RAG
    │                 │
    │                 ├──► Phase 5: n8n Automations
    │                 │
    │                 └──► Phase 6: AI Agents
    │
    └──► Phase 4: Complete Angular UI (cross-cutting)
```

---

## 2. Phase 1: LLM Gateway + Ollama (COMPLETED)

### Objective

Build a functional chat with local Ollama, implementing the multi-provider LLM abstraction layer (LLM Gateway) and the basic user interface.

### Status

**Completed** - All deliverables delivered and tested.

### Deliverables

#### Backend

| Component | Module | Description | Status |
|---|---|---|---|
| `LlmPort` | `localmind-domain` | Port interface for LLM provider access | Completed |
| `LlmGatewayService` | `localmind-application` | Application service with multi-provider routing, fallback chain, cost tracking | In progress |
| `OllamaLlmAdapter` | `localmind-infrastructure` | Adapter for Ollama via Spring AI | In progress |
| `OpenAiLlmAdapter` | `localmind-infrastructure` | Adapter for OpenAI via Spring AI (conditional) | Planned |
| `AnthropicLlmAdapter` | `localmind-infrastructure` | Adapter for Anthropic via Spring AI (conditional) | Planned |
| `ChatController` | `localmind-app` | REST API for conversation management | In progress |
| `DashboardController` | `localmind-app` | REST API for health check and basic metrics | Completed |
| `SecurityConfig` | `localmind-infrastructure` | Spring Security configuration (permissive for v0.1.0) | Completed |
| `CorsConfig` | `localmind-infrastructure` | CORS configuration for Angular dev server | Completed |

#### Frontend

| Component | Type | Description | Status |
|---|---|---|---|
| `ChatPage` | Component (Smart) | Chat main page | In progress |
| `ChatStore` | Signal Store | Reactive chat state based on Angular Signals | In progress |
| `ChatService` | Service | HTTP communication with backend `/api/v1/chat` | In progress |
| `MessageBubble` | Component (Dumb) | Single message display (user/assistant) | Planned |
| `ChatInput` | Component (Dumb) | Input for sending messages | Planned |
| `SidebarLayout` | Component | Layout with navigation sidebar | Planned |

#### Infrastructure

| Component | Description | Status |
|---|---|---|
| Scripts `scripts/` | Native backend/frontend startup; optional infrastructure services (MySQL, Qdrant, Ollama, n8n) via Docker | Completed |
| `.env.example` | Environment variables template | Completed |
| `application.yml` | Spring Boot configuration (dev) | Completed |
| `application-prod.yml` | Spring Boot configuration (prod) | Completed |

### Technical detail: LlmGatewayService

The `LlmGatewayService` is the central component of Phase 1. Its responsibilities include:

1. **Multi-provider routing**: selection of the appropriate LLM provider based on user configuration and availability.
2. **Configurable fallback chain**: if the primary provider is unavailable, the system automatically tries the next one in the chain.
3. **Cost tracking**: recording tokens consumed per request, differentiated by provider and model.
4. **Usage metrics**: aggregated usage metrics (total requests, total tokens, estimated cost).

```
User request
    │
    ▼
LlmGatewayService
    │
    ├──► Primary provider (e.g. Ollama)
    │        │
    │        ├── Success ──► Response
    │        │
    │        └── Failure ──► Fallback
    │                              │
    │                              ├──► Secondary provider (e.g. OpenAI)
    │                              │        │
    │                              │        └── Success ──► Response
    │                              │
    │                              └──► Tertiary provider (e.g. Anthropic)
    │
    └──► Cost tracking (asynchronous)
```

### Effort estimate

| Activity | Estimated effort |
|---|---|
| Backend: LlmGatewayService + adapter | 3-4 weeks |
| Backend: ChatController + persistence | 2 weeks |
| Frontend: Chat page + store | 2-3 weeks |
| Testing and integration | 1-2 weeks |
| **Total Phase 1** | **8-11 weeks** |

### Completion criteria

- [x] Functional chat with local Ollama (send message, receive response).
- [x] Conversation persistence on MySQL.
- [x] Functional health check endpoint (`/api/v1/dashboard/health`).
- [x] Basic cost tracking (tokens consumed per request).
- [x] Configurable fallback chain (at least 2 providers).
- [x] Angular UI with functional chat page.
- [x] Unit tests for LlmGatewayService.
- [x] Integration tests for OllamaLlmAdapter.

---

## 3. Phase 2: File System Ingestion

### Objective

Index documents from local folders on the user's file system, creating a structured and searchable knowledge base.

### Dependencies

- **Phase 1 completed**: base infrastructure, persistence, REST API.

### Deliverables

#### Backend

| Component | Module | Description |
|---|---|---|
| `FileSystemScannerPort` | `localmind-domain` | Port interface for file system scanning |
| `LocalFileSystemScanner` | `localmind-infrastructure` | Adapter that implements local folder scanning |
| `FolderConfig` | `localmind-domain` | Domain entity for monitored folder configuration |
| `FolderConfigRepository` | `localmind-domain` | Repository port for folder configuration persistence |
| `FolderScanJob` | `localmind-infrastructure` | Spring Batch job for periodic folder scanning |
| `FileHashService` | `localmind-application` | Service for SHA-256 file hash calculation (deduplication) |
| `DocumentIngestionService` | `localmind-application` | Service for document ingestion into the system |
| `FolderConfigController` | `localmind-app` | REST API for folder configuration management |

#### Frontend

| Component | Type | Description |
|---|---|---|
| `FoldersPage` | Component (Smart) | Page for monitored folder management |
| `FolderConfigStore` | Signal Store | Reactive folder configuration state |
| `FolderConfigService` | Service | HTTP communication with `/api/v1/folders` |
| `FolderCard` | Component (Dumb) | Single configured folder display |
| `AddFolderDialog` | Component | Dialog for adding a new folder |

#### Supported file formats

| Format | Extension | Parser |
|---|---|---|
| PDF | `.pdf` | Apache PDFBox |
| Microsoft Word | `.docx` | Apache POI |
| Microsoft Excel | `.xlsx` | Apache POI |
| Microsoft PowerPoint | `.pptx` | Apache POI |
| Plain text | `.txt` | Java NIO |
| Markdown | `.md` | Java NIO |
| CSV | `.csv` | Apache Commons CSV |
| JSON | `.json` | Jackson |
| HTML | `.html`, `.htm` | Jsoup |

### Technical detail: Deduplication via SHA-256

To avoid document duplication in the system, a SHA-256 hash is calculated for each file:

```
New file detected
    │
    ▼
SHA-256 calculation
    │
    ├── Hash already present in DB ──► Skip (file already indexed)
    │
    └── Hash not present ──► Document ingestion
                                  │
                                  ├── Text extraction
                                  ├── Metadata creation
                                  └── Save to MySQL
```

### Effort estimate

| Activity | Estimated effort |
|---|---|
| Backend: File system scanner + parser | 3-4 weeks |
| Backend: Spring Batch job + scheduling | 2 weeks |
| Backend: Deduplication + REST API | 1-2 weeks |
| Frontend: Folders page + store | 2 weeks |
| Testing and integration | 1-2 weeks |
| **Total Phase 2** | **9-12 weeks** |

### Completion criteria

- [x] Folder monitoring configuration via REST API.
- [x] Automatic periodic scanning (Spring Batch + scheduling).
- [x] Support for at least 5 file formats (PDF, DOCX, TXT, MD, CSV).
- [x] SHA-256 hash-based deduplication.
- [x] Angular UI with folder management page.
- [x] Document metadata: name, path, size, modification date, hash, indexing status.
- [x] Unit and integration tests.

---

## 4. Phase 3: Base RAG

### Objective

Implement a complete RAG (Retrieval-Augmented Generation) pipeline that enables semantic search on indexed documents and Q&A with source citations.

### Dependencies

- **Phase 1 completed**: functional LLM Gateway.
- **Phase 2 completed**: documents indexed in the system.

### Deliverables

#### Backend

| Component | Module | Description |
|---|---|---|
| `TextExtractorPort` | `localmind-domain` | Port interface for text extraction from documents |
| `ChunkingService` | `localmind-application` | Service for splitting text into chunks with overlap |
| `EmbeddingPort` | `localmind-domain` | Port interface for embedding generation |
| `OllamaEmbeddingAdapter` | `localmind-infrastructure` | Adapter for embedding generation via Ollama (nomic-embed-text) |
| `VectorStorePort` | `localmind-domain` | Port interface for the vector store |
| `QdrantVectorStoreAdapter` | `localmind-infrastructure` | Adapter for Qdrant |
| `SemanticSearchService` | `localmind-application` | Service for semantic search with similarity score |
| `RagService` | `localmind-application` | Service that orchestrates the complete RAG flow |
| `SearchController` | `localmind-app` | REST API for search and Q&A |

#### RAG Pipeline

```
Original document
    │
    ▼
[Extract] ──► Text extracted from document
    │
    ▼
[Chunk] ──► Text fragments (500-1000 tokens with overlap)
    │
    ▼
[Embed] ──► Numerical vectors (via Ollama nomic-embed-text)
    │
    ▼
[Store] ──► Save to Qdrant with metadata


User query
    │
    ▼
[Embed query] ──► Query vector
    │
    ▼
[Search] ──► Top-K most similar chunks from Qdrant
    │
    ▼
[Augment] ──► Prompt = query + relevant chunks as context
    │
    ▼
[Generate] ──► LLM response with source citations
```

#### Frontend

| Component | Type | Description |
|---|---|---|
| `SearchPage` | Component (Smart) | Page for semantic search and Q&A |
| `SearchStore` | Signal Store | Reactive search state |
| `SearchService` | Service | HTTP communication with `/api/v1/search` |
| `SearchResultCard` | Component (Dumb) | Result display with snippet and score |
| `SourceCitation` | Component (Dumb) | Source citation display (document, page) |

### Effort estimate

| Activity | Estimated effort |
|---|---|
| Backend: Extract-chunk-embed-store pipeline | 3-4 weeks |
| Backend: Semantic search + RAG service | 2-3 weeks |
| Backend: SearchController + REST API | 1 week |
| Frontend: Search page + results | 2-3 weeks |
| Testing and integration | 2 weeks |
| **Total Phase 3** | **10-13 weeks** |

### Completion criteria

- [x] Complete functional pipeline: extract, chunk, embed, store.
- [x] Semantic search with results sorted by similarity score.
- [x] Q&A with source citations (document, page/section).
- [x] Configurable chunking (chunk size, overlap).
- [x] Angular UI with functional search page.
- [x] Unit and integration tests for each pipeline component.

---

## 5. Phase 4: Complete Angular UI

### Objective

Build a professional, reactive, and complete user interface that covers all system features with a consistent design system.

### Dependencies

- **Phase 1 completed**: functional chat.
- **Independent** from phases 2-3 for generic UI components, but requires phases 2-3 for specific pages (documents, search).

### Deliverables

| Component | Type | Description |
|---|---|---|
| **Design System** | Global | Dark/light theme, color palette, typography, spacing, base components |
| **Document Library** | Page | Document upload, preview, metadata management, filters and sorting |
| **Dashboard** | Page | Usage charts (tokens, costs, documents), system overview |
| **Settings** | Page | LLM provider configuration, folder management, user preferences |
| **Responsive Design** | Global | Adaptive layout for desktop, tablet, mobile |
| **Simple/Advanced Mode** | Global | Toggle between simplified and full interface |
| **Chart Components** | Components | Charts with chart.js or ngx-charts for dashboard |
| **Notification System** | Service + Component | Toast notifications for user action feedback |
| **Loading States** | Components | Skeleton loaders, spinner, progress bar |
| **Error Handling** | Service + Components | Global error handling with user-friendly messages |

### Design system

| Aspect | Specification |
|---|---|
| **CSS Framework** | Angular Material or Tailwind CSS |
| **Dark theme** | Dark colors, reduced eye strain |
| **Light theme** | Light colors, high readability |
| **Theme toggle** | User preference persistence on localStorage |
| **Typography** | System font (Inter or Roboto) |
| **Spacing** | 4px system (4, 8, 12, 16, 24, 32, 48, 64) |
| **Breakpoints** | Mobile (<768px), Tablet (768-1024px), Desktop (>1024px) |

### Effort estimate

| Activity | Estimated effort |
|---|---|
| Design system + dark/light theme | 2-3 weeks |
| Document library page | 2-3 weeks |
| Dashboard with charts | 2 weeks |
| Settings page | 1-2 weeks |
| Responsive design | 2 weeks |
| Simple/Advanced Mode | 1 week |
| Polish and QA | 1-2 weeks |
| **Total Phase 4** | **11-15 weeks** |

### Completion criteria

- [x] Complete design system with dark and light themes.
- [x] All pages functional (chat, documents, search, dashboard, settings).
- [x] Dashboard with usage charts.
- [x] Responsive layout working on desktop, tablet, mobile.
- [x] Simple and Advanced modes with persistent toggle.
- [x] Error handling and loading states on all pages.

---

## 6. Phase 5: n8n Automations

### Objective

Integrate trigger-based automated workflows via n8n, allowing the user to automate workflows based on internal LocalMind events.

### Dependencies

- **Phase 1 completed**: functional backend.
- **Phase 3 completed**: RAG for workflow enrichment with document data.

### Deliverables

#### Backend

| Component | Module | Description |
|---|---|---|
| `AutomationPort` | `localmind-domain` | Port interface for the automation system |
| `N8nAutomationAdapter` | `localmind-infrastructure` | Adapter for bidirectional integration with n8n |
| `WebhookService` | `localmind-application` | Service for sending webhook triggers on internal events |
| `AutomationController` | `localmind-app` | REST API for automation management |

#### Bidirectional integration

```
LocalMind ──► n8n (Webhook trigger)
    │
    │  Events:
    │  - New document indexed
    │  - New conversation completed
    │  - Cost threshold exceeded
    │  - System error
    │
n8n ──► LocalMind (API callback)
    │
    │  Actions:
    │  - Start folder indexing
    │  - Send chat message
    │  - Retrieve statistics
    │  - Execute semantic search
```

#### Predefined workflow templates

| Workflow | Trigger | Action |
|---|---|---|
| **Daily Summary** | Cron (every day at 08:00) | Generate a summary of the previous day's activities via LLM |
| **New Document Alert** | Webhook (new document) | Notification (email/Telegram) when a new document is indexed |
| **Cost Alert** | Webhook (cost threshold) | Notification when cloud provider costs exceed a threshold |
| **Auto-Categorize** | Webhook (new document) | Automatically categorize the document via LLM |
| **Backup Reminder** | Cron (weekly) | Reminder to perform data backup |

#### Frontend

| Component | Type | Description |
|---|---|---|
| `AutomationsPage` | Component (Smart) | Page for automation management |
| `AutomationStore` | Signal Store | Reactive automation state |
| `WorkflowCard` | Component (Dumb) | Single workflow display with status and last run |
| `WorkflowLog` | Component | Workflow execution log display |

### Effort estimate

| Activity | Estimated effort |
|---|---|
| Backend: Webhook service + n8n adapter | 2-3 weeks |
| Backend: AutomationController + REST API | 1-2 weeks |
| n8n workflow templates | 2 weeks |
| Frontend: Automations page | 2 weeks |
| Testing and integration | 1-2 weeks |
| **Total Phase 5** | **8-11 weeks** |

### Completion criteria

- [x] Functional webhook triggers for at least 3 event types.
- [x] Functional bidirectional LocalMind-n8n integration.
- [x] At least 3 predefined and tested workflow templates.
- [x] Angular UI with automations page.
- [x] Integration tests for webhooks.

---

## 7. Phase 6: AI Agents

### Objective

Implement specialized AI agents with tool calling capabilities, able to execute complex actions by combining LLM, RAG, and external tools.

### Dependencies

- **Phase 1 completed**: LLM Gateway.
- **Phase 3 completed**: RAG for knowledge retrieval.

### Deliverables

#### Backend

| Component | Module | Description |
|---|---|---|
| `AgentPort` | `localmind-domain` | Port interface for AI agents |
| `AgentService` | `localmind-application` | Service for agent orchestration |
| `ToolCallingFramework` | `localmind-application` | Framework for tool calling definition and execution |
| `AgentController` | `localmind-app` | REST API for agent interaction |

#### Specialized agents

| Agent | Specialization | Available tools |
|---|---|---|
| **Tech Agent** | Technical analysis, code review, debugging | Technical document search, snippet execution, log analysis |
| **Business Agent** | Business analysis, reports, KPIs | Business document search, metric calculation, report generation |
| **Legal Agent** | Legal document analysis, compliance | Contract search, clause verification, regulation comparison |
| **Personal Agent** | Personal assistant, organization | Note search, task management, daily summary |

#### Tool Calling Framework

```
User query
    │
    ▼
Selected agent
    │
    ▼
LLM decides whether to use a tool
    │
    ├── No tool needed ──► Direct response
    │
    └── Tool needed ──► Tool execution
                                │
                                ├── RAG search (semantic search)
                                ├── Database query (metrics, statistics)
                                ├── File system (file reading)
                                └── Calculation (mathematical operations)
                                │
                                ▼
                           Tool result integrated into response
```

#### Frontend

| Component | Type | Description |
|---|---|---|
| `AgentsPage` | Component (Smart) | Page for agent selection and interaction |
| `AgentStore` | Signal Store | Reactive agent state |
| `AgentSelector` | Component | Specialized agent selection |
| `AgentConfigDialog` | Component | Custom agent configuration |
| `ToolExecutionLog` | Component | Display of tools called by the agent |

### Effort estimate

| Activity | Estimated effort |
|---|---|
| Backend: Tool calling framework | 3-4 weeks |
| Backend: Agent service + orchestration | 2-3 weeks |
| Backend: Implementation of 4 agents | 4-6 weeks |
| Frontend: Agent pages + configuration | 3 weeks |
| Testing and integration | 2 weeks |
| **Total Phase 6** | **14-18 weeks** |

### Completion criteria

- [x] Functional tool calling framework.
- [x] At least 2 operational specialized agents (Tech, Business).
- [x] Functional RAG-augmented agent responses.
- [x] Angular UI with agent selection and tool execution display.
- [x] Unit and integration tests.

---

## 8. Roadmap Summary

### Overall timeline

| Phase | Description | Estimated effort | Dependencies | Status |
|---|---|---|---|---|
| **Phase 1** | LLM Gateway + Ollama | 8-11 weeks | None | Completed |
| **Phase 2** | File System Ingestion | 9-12 weeks | Phase 1 | Completed |
| **Phase 3** | Base RAG | 10-13 weeks | Phase 1 + Phase 2 | Completed |
| **Phase 4** | Complete Angular UI | 11-15 weeks | Phase 1 (partial Phase 2-3) | Completed |
| **Phase 5** | n8n Automations | 8-11 weeks | Phase 1 + Phase 3 | Completed |
| **Phase 6** | AI Agents | 14-18 weeks | Phase 1 + Phase 3 | Completed |

**Note:** Phases 4, 5, and 6 can be developed with partial parallelism, reducing the total time.

### Total estimated effort

| Scenario | Weeks | Months |
|---|---|---|
| Sequential development | 60-80 weeks | 15-20 months |
| Development with parallelism | 40-55 weeks | 10-14 months |

### Versioning

| Version | Included phases | Milestone | Status |
|---|---|---|---|
| **v0.1.0** | Phase 1 (partial) | Scaffolding + basic chat | Completed |
| **v0.2.0** | Phase 1 (complete) | Functional chat with Ollama | Completed |
| **v0.3.0** | Phase 2 | Document indexing | Completed |
| **v0.4.0** | Phase 3 | Functional RAG | Completed |
| **v0.5.0** | Phase 4 | Complete UI | Completed |
| **v0.6.0** | Phase 5 | Automations | Completed |
| **v1.0.0** | Phase 6 | AI Agents + MCP (135+ tools) | **In progress (hardening)** |
