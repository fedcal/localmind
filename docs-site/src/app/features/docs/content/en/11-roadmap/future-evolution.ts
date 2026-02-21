export const content = `# Future Evolution

| | |
|---|---|
| **Document** | Future Evolution and Vision |
| **Version** | 1.0.0 |
| **Date** | 2026-02-21 |
| **Project** | LocalMind |

---

## Table of Contents

1. [Overview](#1-overview)
2. [Planned Features Post-v1.0](#2-planned-features-post-v10)
   - 2.1 [SSE/WebSocket Streaming](#21-ssewebsocket-streaming)
   - 2.2 [Integrated OCR](#22-integrated-ocr)
   - 2.3 [Plugin System](#23-plugin-system)
   - 2.4 [Mobile Responsive / PWA](#24-mobile-responsive--pwa)
   - 2.5 [API SDK for External Integration](#25-api-sdk-for-external-integration)
   - 2.6 [Multimodal Model Support](#26-multimodal-model-support)
   - 2.7 [Knowledge Graph](#27-knowledge-graph)
   - 2.8 [Local Fine-Tuning](#28-local-fine-tuning)
   - 2.9 [Agent and Workflow Marketplace](#29-agent-and-workflow-marketplace)
   - 2.10 [Configuration Backup and Restore](#210-configuration-backup-and-restore)
   - 2.11 [Conversation Import/Export](#211-conversation-importexport)
   - 2.12 [Multi-Language UI Support](#212-multi-language-ui-support)
   - 2.13 [Advanced Metrics and Analytics](#213-advanced-metrics-and-analytics)
   - 2.14 [Calendar and Email Integration](#214-calendar-and-email-integration)
3. [Architectural Evolution](#3-architectural-evolution)
   - 3.1 [Microservices](#31-microservices)
   - 3.2 [Event-Driven Architecture](#32-event-driven-architecture)
   - 3.3 [CQRS](#33-cqrs)
   - 3.4 [Kubernetes Deployment](#34-kubernetes-deployment)
4. [Community and Open Source](#4-community-and-open-source)
5. [Priority and Impact Matrix](#5-priority-and-impact-matrix)

---

## 1. Overview

> **Update February 2026**: With the completion of all 6 original phases, this document has been updated to reflect the current project state. Priorities have been recalculated using the RICE framework (Reach x Impact x Confidence / Effort) and aligned with the 2026 competitor and trend analysis.

This document describes the long-term vision for LocalMind, outlining planned features for versions following v1.0.0, planned architectural evolutions, and the strategy for community and open source.

The listed features represent a project aspiration and not a binding commitment. Their implementation will be driven by:

- **User feedback**: the most requested features will have higher priority.
- **Technological maturity**: some features depend on the evolution of external technologies (LLM models, frameworks, libraries).
- **Available resources**: development will be calibrated on the resources actually available.
- **Architectural coherence**: each feature must integrate harmoniously with the existing architecture.

---

## 1.5 v1.0.0 GA Prerequisites (Hardening Sprint)

**Target**: March 2026 (4 weeks)

Before the v1.0.0 GA release, the following critical gaps must be resolved:

| # | Gap | RICE Score | Priority | Status |
|---|-----|-----------|----------|--------|
| 1 | **i18n JSON files** - 725 keys in it.json/en.json, TranslationService, TranslatePipe | 333 | P0 | COMPLETED (Feb 2026) |
| 2 | **Loading skeleton loaders** - 5 variants, used in 7+ pages | 300 | P0 | COMPLETED (Feb 2026) |
| 3 | **Dark mode toggle** - ThemeService, ThemeToggle, 125+ CSS variables | 267 | P0 | COMPLETED (Feb 2026) |
| 4 | **Frontend form validation** - Template-driven with shared validators | 210 | P1 | COMPLETED (Feb 2026) |
| 5 | **Chat streaming SSE** - SseEmitter, 3 provider adapters | 191 | P1 | COMPLETED (Feb 2026) |
| 6 | **Simple/Advanced mode** - Signal-based toggle, system prompt/RAG/tool calling | 95 | P1 | COMPLETED (Feb 2026) |

**v1.0.0 GO-LIVE Criteria:**
- All 1788 backend tests pass (8 modules) - DONE
- All 67 Playwright E2E tests pass
- Complete IT/EN/FR/DE/ES i18n functional (762 keys x 5 languages) - DONE
- Dark/light mode with toggle and localStorage persistence - DONE
- Chat streaming SSE for token-by-token responses - DONE
- Frontend form validation with shared validators - DONE
- OCR integration with Tesseract (tess4j) - DONE
- Plugin system with PF4J framework - DONE
- Conversation Import/Export (JSON/MD/PDF + ChatGPT/Claude import) - DONE
- Configuration Backup and Restore (ZIP, schedulable) - DONE
- API SDK Java/Python/JS - DONE
- Multimodal model support (LLaVA, GPT-4V, Whisper) - DONE
- Knowledge Graph with LLM-based entity extraction - DONE
- Local Fine-Tuning (Python orchestration LoRA/QLoRA) - DONE
- Agent and Workflow Marketplace - DONE
- Advanced metrics and analytics dashboard - DONE
- Calendar integration (CalDAV) and email (IMAP/SMTP) - DONE
- Event-Driven Architecture (Spring Events + @Async) - DONE
- CQRS (separate read models for conversations and documents) - DONE
- Microservices preparation (shared-types, docker-compose, nginx gateway) - DONE
- Swagger/OpenAPI updated
- Docker image functional and tested

---

## 2. Planned Features Post-v1.0

### 2.1 SSE/WebSocket Streaming

**Target version:** v1.1.0

**Description:** Implementation of real-time LLM response streaming, allowing the user to see the response token by token during generation.

**Motivation:** Currently, the LLM response is returned in full at the end of generation. With large models or complex prompts, this results in wait times of several seconds during which the user receives no feedback.

**Technical detail:**

| Technology | Approach | Advantages |
|---|---|---|
| **Server-Sent Events (SSE)** | Unidirectional server-to-client stream | Simple, natively supported by browsers, compatible with HTTP/2 |
| **WebSocket** | Full-duplex bidirectional connection | Bidirectional communication, lower overhead for frequent messages |

**Planned implementation:**

\`\`\`
Client (Angular)                    Server (Spring Boot)
     │                                      │
     │──── POST /api/v1/chat/stream ────►   │
     │                                      │──► Ollama (streaming)
     │◄──── SSE: token 1 ─────────────     │
     │◄──── SSE: token 2 ─────────────     │
     │◄──── SSE: token 3 ─────────────     │
     │◄──── SSE: [DONE] ──────────────     │
     │                                      │
\`\`\`

Spring Boot natively supports SSE via \`SseEmitter\` or the reactive type \`Flux<ServerSentEvent>\`. Spring AI already provides streaming APIs for supported providers.

---

### 2.2 Integrated OCR

**Target version:** v1.2.0

**Description:** Integration of an OCR (Optical Character Recognition) engine for text extraction from scanned documents, images, and non-textual PDFs.

**Motivation:** Many business and personal documents are available exclusively as scans or images (invoices, digitized paper contracts, photographed notes). Without OCR, these documents cannot be indexed by the RAG pipeline.

**Proposed technology:**

| Engine | License | Languages | Self-hosted |
|---|---|---|---|
| **Tesseract** | Apache 2.0 | 100+ languages | Yes (Docker container) |
| **EasyOCR** | Apache 2.0 | 80+ languages | Yes (Python) |
| **PaddleOCR** | Apache 2.0 | 80+ languages | Yes (Python) |

**Planned flow:**

\`\`\`
Scanned document (image/PDF)
    │
    ▼
[OCR] ──► Extracted text
    │
    ▼
[Standard RAG pipeline] ──► Chunk, embed, store
\`\`\`

---

### 2.3 Plugin System

**Target version:** v1.4.0

**Description:** Plugin system that allows the community to extend LocalMind's functionality without modifying the core code.

**Motivation:** A plugin system encourages adoption and customization, allowing third-party developers to add features specific to their use cases.

**Proposed architecture:**

\`\`\`
LocalMind Core
    │
    ├── Plugin API (stable interfaces)
    │       │
    │       ├── LlmProviderPlugin     (new LLM providers)
    │       ├── ParserPlugin          (new file formats)
    │       ├── VectorStorePlugin     (new vector stores)
    │       ├── AgentPlugin           (new specialized agents)
    │       └── UIPlugin              (new UI components)
    │
    ├── Plugin Registry (discovery and lifecycle)
    │
    └── Plugin Sandbox (isolation and security)
\`\`\`

**Loading mechanism:**

- Plugin JARs in the \`/plugins\` directory.
- Isolated class loader for each plugin.
- Stable and versioned interfaces (backward compatibility).
- Plugin configuration via YAML files.

---

### 2.4 Mobile Responsive / PWA

**Target version:** v1.5.0

**Description:** Transformation of the Angular frontend into a Progressive Web App (PWA) with complete mobile device support.

**PWA features:**

| Feature | Description |
|---|---|
| **Installable** | Add to device home screen |
| **Offline first** | Basic functionality even without connection (local cache) |
| **Responsive** | Layout optimized for smartphones and tablets |
| **Push notifications** | Notifications for events (document indexed, workflow completed) |
| **Service Worker** | Intelligent caching of static resources |

---

### 2.5 API SDK for External Integration

**Target version:** v1.6.0

**Description:** Software Development Kit (SDK) for integrating LocalMind features into external applications.

**Planned SDKs:**

| SDK | Language | Usage |
|---|---|---|
| \`localmind-sdk-java\` | Java/Kotlin | Integration into JVM applications |
| \`localmind-sdk-python\` | Python | Integration into Python scripts and applications |
| \`localmind-sdk-js\` | JavaScript/TypeScript | Integration into web and Node.js applications |

**Exposed features:**

\`\`\`python
# Example: localmind-sdk-python
from localmind import LocalMindClient

client = LocalMindClient(base_url="http://localhost:8080")

# Chat
response = client.chat("Riassumi il documento sulle vendite Q4")

# Ricerca semantica
results = client.search("politica ferie aziendali", top_k=5)

# Indicizzazione documento
client.documents.index("/path/to/document.pdf")
\`\`\`

---

### 2.6 Multimodal Model Support

**Target version:** v2.0.0

**Description:** Support for multimodal LLM models capable of processing images, audio, and video in addition to text.

**Use cases:**

| Input | Model | Use case |
|---|---|---|
| **Images** | LLaVA, GPT-4V | Photo analysis, image description, data extraction from screenshots |
| **Audio** | Whisper | Meeting transcription, voice notes, podcasts |
| **Video** | Future | Video content analysis, key frame extraction |

**Architectural impact:**

- Extension of the \`LlmPort\` interface to support multimodal inputs.
- New adapters for multimodal models.
- Storage for multimedia files (images, audio, video).
- Pre-processing pipeline for each media type.

---

### 2.7 Knowledge Graph

**Target version:** v2.1.0

**Description:** Integration of a knowledge graph in addition to the vector store, to represent structured relationships between entities extracted from documents.

**Motivation:** The vector store excels at semantic similarity search but does not capture explicit relationships between entities (people, organizations, concepts). A knowledge graph complements the vector store by offering relational navigation.

**Proposed technology:**

| Component | Technology | Self-hosted |
|---|---|---|
| Graph database | **Neo4j Community** | Yes (Docker container) |
| NER (Named Entity Recognition) | spaCy or LLM model | Yes |
| Relation extraction | LLM model | Yes |

**Flow:**

\`\`\`
Document
    │
    ├──► [RAG Pipeline] ──► Embedding (Qdrant)
    │
    └──► [KG Pipeline] ──► Entities and relationships (Neo4j)
             │
             ├── NER: entity extraction (people, places, orgs)
             └── RE: relationship extraction between entities
\`\`\`

---

### 2.8 Local Fine-Tuning

**Target version:** v2.2.0

**Description:** Ability to perform local fine-tuning of LLM models on user documents, using low-resource techniques such as LoRA (Low-Rank Adaptation) and QLoRA (Quantized LoRA).

**Motivation:** Fine-tuning allows specializing a generic model to the user's specific domain, significantly improving response quality for their context.

**Hardware requirements:**

| Technique | Minimum VRAM | Estimated time (1000 documents) |
|---|---|---|
| LoRA (7B params) | 8 GB | 2-4 hours |
| QLoRA (7B params) | 4 GB | 4-8 hours |
| LoRA (13B params) | 16 GB | 6-12 hours |

**Flow:**

\`\`\`
User documents ──► Training dataset (automatic)
    │
    ▼
Base model (e.g. llama3.2) + LoRA adapter
    │
    ▼
Local fine-tuning (GPU)
    │
    ▼
Personalized model (base + LoRA weights)
    │
    ▼
Deploy to local Ollama
\`\`\`

---

### 2.9 Agent and Workflow Marketplace

**Target version:** v2.3.0

**Description:** Platform for sharing and distributing specialized agents and n8n workflows created by the community.

**Features:**

| Aspect | Description |
|---|---|
| **Catalog** | List of available agents and workflows with description, rating, downloads |
| **Installation** | One-click install from LocalMind UI |
| **Versioning** | Agent and workflow version management |
| **Reviews** | Community review and rating system |
| **Publishing** | Ability for users to publish their own agents and workflows |

---

### 2.10 Configuration Backup and Restore

**Target version:** v1.2.0

**Description:** Built-in functionality for backup and restore of the entire LocalMind configuration, including database, vector store, workflows, and user settings.

**Features:**

- Complete backup in a single compressed file (.tar.gz).
- Selective backup (database only, configuration only, workflows only).
- Scheduled backup (daily, weekly).
- Restore with integrity verification.
- Export/Import configuration between different installations.

---

### 2.11 Conversation Import/Export

**Target version:** v1.1.0

**Description:** Ability to export conversations in standard formats and import conversations from other tools.

**Supported formats:**

| Format | Export | Import |
|---|---|---|
| JSON | Yes | Yes |
| Markdown | Yes | No |
| PDF | Yes | No |
| ChatGPT export | No | Yes |
| Claude export | No | Yes |

---

### 2.12 Multi-Language UI Support

**Target version:** v1.3.0

**Description:** Internationalization (i18n) of the user interface with support for multiple languages.

**Planned languages:**

| Language | Code | Priority |
|---|---|---|
| Italian | \`it\` | High (primary language) |
| English | \`en\` | High |
| French | \`fr\` | Medium |
| German | \`de\` | Medium |
| Spanish | \`es\` | Medium |

**Implementation:** Angular i18n or ngx-translate for translation management with JSON files per language.

---

### 2.13 Advanced Metrics and Analytics

**Target version:** v1.4.0

**Description:** Advanced analytics dashboard with detailed system usage metrics.

**Planned metrics:**

| Category | Metrics |
|---|---|
| **LLM Usage** | Tokens consumed per provider/model/day, estimated cost, response times |
| **RAG Performance** | Retrieval quality (precision, recall), document coverage |
| **Document Stats** | Documents by type, average size, update rate |
| **User Activity** | Conversations per day, most frequent queries, usage times |
| **System Health** | CPU, RAM, storage, uptime, errors by type |

---

### 2.14 Calendar and Email Integration

**Target version:** v2.0.0

**Description:** Integration with calendars (CalDAV, Google Calendar) and email clients (IMAP/SMTP) for a complete personal assistant.

**Features:**

| Integration | Features |
|---|---|
| **Calendar** | Appointment viewing, event creation, smart reminders |
| **Email** | Email reading, inbox summary, LLM-generated reply drafts |

**Note:** These integrations will respect the local-first principle. Email and calendar credentials will be managed locally, and data will not transit through third-party services. For calendar, the CalDAV protocol (local/self-hosted) will be preferred. For email, direct IMAP/SMTP will be used.

---

## 3. Architectural Evolution

### 3.1 Microservices

**Trigger:** When the monolith becomes too complex to manage effectively, or when the need to scale components individually arises.

**Planned decomposition:**

| Microservice | Responsibility |
|---|---|
| \`localmind-chat-service\` | Conversation management and LLM interaction |
| \`localmind-document-service\` | Indexing, parsing, document management |
| \`localmind-search-service\` | Semantic search and RAG |
| \`localmind-agent-service\` | AI agent orchestration |
| \`localmind-automation-service\` | n8n integration and workflows |
| \`localmind-gateway\` | API Gateway (routing, authentication, rate limiting) |

**Important note:** Migration to microservices is NOT planned in the short-term roadmap. The Hexagonal architecture adopted since v0.1.0 facilitates future decomposition, but the modular monolith is adequate for current and foreseeable needs.

### 3.2 Event-Driven Architecture

**Trigger:** When synchronous communication between components becomes a bottleneck, or when the need for large-scale asynchronous processing arises.

**Proposed technology:**

| Component | Technology | Self-hosted |
|---|---|---|
| Message broker | **Apache Kafka** | Yes (Docker container) |
| Lightweight alternative | **RabbitMQ** | Yes (Docker container) |
| Embedded alternative | **Spring Events** (already in use) | Yes (in-process) |

**Planned events:**

| Event | Producer | Consumer |
|---|---|---|
| \`DocumentIndexed\` | Document Service | Search Service, Automation Service |
| \`ConversationCompleted\` | Chat Service | Analytics Service, Automation Service |
| \`EmbeddingGenerated\` | Search Service | Vector Store |
| \`AgentTaskCompleted\` | Agent Service | Chat Service, Automation Service |
| \`CostThresholdExceeded\` | Analytics Service | Automation Service (alert) |

**Progressive evolution:**

\`\`\`
v1.0: Spring Events (in-process, synchronous)
    │
    ▼
v1.x: Spring Events + @Async (in-process, asynchronous)
    │
    ▼
v2.x: Apache Kafka / RabbitMQ (distributed, asynchronous)
\`\`\`

### 3.3 CQRS

**Trigger:** When read and write patterns diverge significantly, or when read queries become complex and require optimized data models.

**Description:** The CQRS (Command Query Responsibility Segregation) pattern separates the read model from the write model, allowing independent optimizations.

**Applicability in LocalMind:**

| Domain | Command (Write) | Query (Read) |
|---|---|---|
| **Documents** | Indexing, metadata updates | Search, listing, complex filters |
| **Conversations** | Message creation, status updates | History, search, analytics |
| **Metrics** | Token/cost recording | Dashboard, reports, charts |

**Planned implementation:**

- **Write model:** Normalized domain entities on MySQL.
- **Read model:** Materialized views or denormalized tables optimized for queries.
- **Synchronization:** Domain events that update the read model.

### 3.4 Kubernetes Deployment

**Trigger:** When LocalMind is adopted in enterprise contexts with high availability, scalability, and centralized management requirements.

**Planned Kubernetes components:**

| K8s Resource | Usage |
|---|---|
| **Deployment** | For each microservice (when applicable) |
| **StatefulSet** | For MySQL and Qdrant (persistent data) |
| **Service** | For inter-pod communication |
| **Ingress** | For API and frontend exposure |
| **ConfigMap** | For non-sensitive configurations |
| **Secret** | For credentials and API keys |
| **PersistentVolumeClaim** | For persistent storage |
| **HorizontalPodAutoscaler** | For automatic scaling |

**Helm Chart:** Distribution via Helm chart for simplified installation on any Kubernetes cluster.

\`\`\`bash
# Future installation via Helm
helm repo add localmind https://charts.localmind.io
helm install localmind localmind/localmind \\
  --set ollama.gpu.enabled=true \\
  --set mysql.persistence.size=50Gi
\`\`\`

---

## 4. Community and Open Source

### License

The license for the LocalMind project is still to be defined. The options under evaluation are:

| License | Characteristics | Pros | Cons |
|---|---|---|---|
| **MIT** | Permissive, minimal | Maximum adoption, simplicity | No copyleft protection |
| **Apache 2.0** | Permissive with patent grant | Patent protection, enterprise adoption | Slightly more complex |
| **AGPL 3.0** | Strong copyleft | Protects code from proprietary appropriation | May discourage enterprise adoption |
| **BSL** | Source available with timer | Monetization possible, becomes open after X years | Not pure open source |

**Planned decision:** By v1.0.0.

### Contributing Guidelines

Guidelines for community contributors:

| Aspect | Rule |
|---|---|
| **Code style** | Google Java Style Guide (backend), Angular Style Guide (frontend) |
| **Commit convention** | Conventional Commits (feat, fix, docs, refactor, test) |
| **Branch strategy** | GitFlow (main, develop, feature/*, release/*, hotfix/*) |
| **Pull request** | Mandatory template, at least 1 review, green CI |
| **Issue tracker** | GitHub Issues with templates (bug report, feature request) |
| **Code of conduct** | Contributor Covenant |

### Plugin Development Kit

To facilitate plugin development by the community:

| Resource | Description |
|---|---|
| \`localmind-plugin-api\` | Maven artifact with stable plugin interfaces |
| \`localmind-plugin-archetype\` | Maven archetype for new plugin scaffolding |
| Plugin documentation | Complete plugin development guide |
| Plugin examples | Repository with plugin examples for each type |
| Plugin testing framework | Tools for plugin testing in isolation |

### Documentation Site

Planning for a dedicated documentation site:

| Aspect | Technology |
|---|---|
| **Framework** | Docusaurus or MkDocs |
| **Hosting** | GitHub Pages (free) |
| **Content** | User guide, developer guide, API reference, tutorials |
| **Versioning** | Versioned documentation for each major release |
| **Search** | Integrated full-text search |
| **Internationalization** | Italian and English (minimum) |

---

## 5. Priority and Impact Matrix

The following matrix classifies features by implementation priority and impact on user experience:

### Critical Priority (pre-release v1.0.0 GA)

| # | Feature | Target | RICE Score | Status |
|---|---------|--------|------------|--------|
| - | i18n JSON files (IT/EN) | v1.0.0 | 333 | COMPLETED |
| - | Loading skeleton loaders | v1.0.0 | 300 | COMPLETED |
| - | Dark mode toggle | v1.0.0 | 267 | COMPLETED |
| - | Chat streaming SSE | v1.0.0 | 191 | COMPLETED |
| - | Form validation | v1.0.0 | 210 | COMPLETED |
| - | Simple/Advanced mode | v1.0.0 | 95 | COMPLETED |

### High priority, high impact

| # | Feature | Target version | Status |
|---|---|---|---|
| 1 | SSE/WebSocket Streaming | v1.1.0 | COMPLETED (Feb 2026) |
| 12 | Conversation Import/Export | v1.1.0 | COMPLETED (Feb 2026) |
| 11 | Backup and Restore | v1.2.0 | COMPLETED (Feb 2026) |

### High priority, medium impact

| # | Feature | Target version | Status |
|---|---|---|---|
| 2 | Integrated OCR | v1.2.0 | COMPLETED (Feb 2026) |
| 13 | Multi-Language UI | v1.3.0 | COMPLETED (Feb 2026) |
| 14 | Advanced Metrics | v1.4.0 | COMPLETED (Feb 2026) |

### Medium priority, high impact

| # | Feature | Target version | Status |
|---|---|---|---|
| 4 | Plugin System | v1.4.0 | COMPLETED (Feb 2026) |
| 5 | PWA | v1.5.0 | TO IMPLEMENT |
| 7 | Multimodal Models | v2.0.0 | COMPLETED (Feb 2026) |

### Medium priority, medium impact

| # | Feature | Target version | Status |
|---|---|---|---|
| 6 | API SDK | v1.6.0 | COMPLETED (Feb 2026) |
| 15 | Calendar and Email | v2.0.0 | COMPLETED (Feb 2026) |
| 8 | Knowledge Graph | v2.1.0 | COMPLETED (Feb 2026) |

### Low priority (long term)

| # | Feature | Target version | Status |
|---|---|---|---|
| 9 | Local Fine-Tuning | v2.2.0 | COMPLETED (Feb 2026) |
| 10 | Marketplace | v2.3.0 | COMPLETED (Feb 2026) |

### Architectural evolution (on-demand)

| Feature | Trigger | Status |
|---|---|---|
| Microservices (prep) | Unsustainable monolith complexity | COMPLETED (Feb 2026) |
| Event-Driven | Need for distributed asynchronous processing | COMPLETED (Feb 2026) |
| CQRS | Significant divergence of read/write patterns | COMPLETED (Feb 2026) |
| Kubernetes | Enterprise adoption with HA requirements | TO IMPLEMENT |

These architectural evolutions do not have a fixed target version, as they will be triggered based on the actual needs of the project and its community.
`;
