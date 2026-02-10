# Security and Privacy Principles

| | |
|---|---|
| **Document** | Security and Privacy Principles |
| **Version** | 0.1.0 |
| **Date** | 2026-02-09 |
| **Project** | LocalMind |

---

## Table of Contents

- [Security and Privacy Principles](#security-and-privacy-principles)
  - [Table of Contents](#table-of-contents)
  - [1. Fundamental Principle](#1-fundamental-principle)
  - [2. The 5 Pillars of LocalMind Security](#2-the-5-pillars-of-localmind-security)
    - [2.1 Local-First](#21-local-first)
    - [2.2 Zero Data Leakage](#22-zero-data-leakage)
    - [2.3 Cloud Opt-In](#23-cloud-opt-in)
    - [2.4 No Telemetry](#24-no-telemetry)
    - [2.5 Self-Hosted](#25-self-hosted)
  - [3. Privacy Comparison vs Competitors](#3-privacy-comparison-vs-competitors)
    - [Detailed Analysis](#detailed-analysis)
  - [4. Full Offline Mode](#4-full-offline-mode)
    - [Prerequisites for Offline Operation](#prerequisites-for-offline-operation)
    - [Features Available Offline](#features-available-offline)
    - [Offline Use Cases](#offline-use-cases)
  - [5. GDPR Compliance](#5-gdpr-compliance)
    - [GDPR Principles Satisfied](#gdpr-principles-satisfied)
    - [Absence of Cross-Border Transfer](#absence-of-cross-border-transfer)
  - [6. No Vendor Lock-In](#6-no-vendor-lock-in)
    - [Replaceable Components](#replaceable-components)
    - [Migration Between Components](#migration-between-components)

---

## 1. Fundamental Principle

> **"Your AI, your data, your machine."**

The core principle upon which the entire security architecture of LocalMind is founded is that **user data never leaves the computer**. Every uploaded document, every conversation started, every generated embedding, and every personal configuration resides exclusively on the end user's machine.

This approach radically contrasts with the cloud-first model adopted by the vast majority of competitors, where user data is transmitted, processed, and stored on remote infrastructure, often outside the user's direct control.

LocalMind guarantees that:

- No data transits through external servers without the user's explicit consent.
- The user maintains full ownership and control over their data at all times.
- Data deletion is immediate, complete, and verifiable, as it resides on local storage.

---

## 2. The 5 Pillars of LocalMind Security

### 2.1 Local-First

All data managed by LocalMind physically resides on the user's machine:

| Data Type             | Local Storage        | Technology                       |
|-----------------------|----------------------|----------------------------------|
| Original documents    | Local file system    | Configurable paths               |
| Document metadata     | MySQL                | Local instance                   |
| Vector embeddings     | Qdrant               | Local instance (native or Docker) |
| Conversations and chat| MySQL                | Local instance                   |
| LLM models            | Ollama model store   | Local instance (native or Docker) |
| Automation workflows  | n8n                  | Local instance (native or Docker) |
| User configurations   | MySQL + file         | Local instance + .env            |

There is no component in the architecture that mandatorily requires a connection to remote servers for the base system to function.

### 2.2 Zero Data Leakage

No data is transmitted to third-party services without the user's explicit consent.

- **Ollama** operates entirely locally: LLM models are downloaded once and subsequently executed without any network communication.
- **MySQL** and **Qdrant** accept connections exclusively from `localhost` in the default configuration.
- The backend REST APIs are exposed only on `localhost:8080`.
- The Angular frontend is served on `localhost:4200`.

**Data flow in local mode:**

```
User -> Angular (localhost:4200)
     -> Spring Boot (localhost:8080)
     -> Ollama (localhost:11434)     [LLM inference]
     -> MySQL (localhost:3306)       [persistence]
     -> Qdrant (localhost:6333)      [vector search]
```

No network hop leaves the `localhost` perimeter.

### 2.3 Cloud Opt-In

Cloud providers are **optional** and require an explicit action by the user to be activated:

- **OpenAI**: requires manual entry of `OPENAI_API_KEY` and explicit enablement via `localmind.llm.openai.enabled=true`.
- **Anthropic**: requires manual entry of `ANTHROPIC_API_KEY` and explicit enablement via `localmind.llm.anthropic.enabled=true`.
- **Google AI**: requires manual entry of `GOOGLE_API_KEY` and explicit enablement via `localmind.llm.google.enabled=true`.

When a cloud provider is enabled, the user is aware that:

1. The **prompt** (user's question) is sent to the provider.
2. The **RAG context** (relevant document fragments) may be included in the prompt.
3. The **response** is received from the provider and stored locally.
4. Cloud providers may apply their own data retention policies.

**Important:** in the default mode, no cloud provider is enabled. The system operates exclusively with Ollama locally.

### 2.4 No Telemetry

LocalMind **does not collect any usage data** and does not transmit metrics, statistics, or diagnostic information to external servers:

- No analytics services (Google Analytics, Mixpanel, Amplitude, etc.).
- No remote crash reporting (Sentry, Bugsnag, etc.).
- No phone-home calls at first launch or during use.
- No tracking of features used or navigation patterns.

The only metrics collected are internal ones, stored locally in MySQL:

- Number of tokens consumed per provider (for personal cost tracking).
- Number of indexed documents.
- Query response times (for local optimization).

These metrics are accessible exclusively by the user through the local dashboard.

### 2.5 Self-Hosted

Every component of the LocalMind architecture runs on the user's machine. The backend and frontend are executed natively via scripts in the `scripts/` folder, while infrastructure services can be run natively or via Docker:

| Component   | Execution Mode              | Function                   |
|-------------|-----------------------------|----------------------------|
| MySQL 8.0   | Native local instance       | Relational database        |
| Qdrant      | Native or Docker (optional) | Vector store for embeddings|
| Ollama      | Native or Docker (optional) | Local LLM inference        |
| n8n         | Native or Docker (optional) | Workflow automation        |
| Spring Boot | Local JVM (script)          | Backend API                |
| Angular     | Local dev server (script)   | Frontend UI                |

The user has full control over:

- **Updates**: decides when and whether to update each component.
- **Configuration**: can modify every parameter of every service.
- **Data**: can perform backup, restore, migration, or deletion at any time.
- **Resources**: can allocate CPU, RAM, and GPU according to their needs.

---

## 3. Privacy Comparison vs Competitors

| Feature                        | ChatGPT                         | Notion AI              | PrivateGPT         | **LocalMind**                                       |
|--------------------------------|---------------------------------|------------------------|--------------------|-----------------------------------------------------|
| **Where data resides**         | OpenAI Cloud                    | Notion Cloud           | Local              | **Local**                                           |
| **Training on user data**      | Possible (opt-out available)    | Not disclosed          | No                 | **No**                                              |
| **Structured persistence**     | Cloud-only                      | Cloud-only             | Limited            | **Local MySQL + Qdrant**                            |
| **Works offline**              | No                              | No                     | Yes                | **Yes**                                             |
| **Dedicated vector store**     | Not exposed                     | Not exposed            | In-memory / Chroma | **Local Qdrant**                                    |
| **Automations**                | No                              | Limited                | No                 | **Local n8n**                                       |
| **Multi-provider LLM**         | OpenAI only                     | OpenAI only            | Limited            | **Ollama + OpenAI + Anthropic + Google**            |
| **GDPR by design**             | No (US transfer)                | No (US transfer)       | Yes                | **Yes**                                             |
| **Open source**                | No                              | No                     | Yes                | **Yes**                                             |
| **Self-hosted**                | N/A                             | N/A                    | Partial            | **Complete (native execution + optional Docker)**   |

### Detailed Analysis

**ChatGPT (OpenAI):**
- Conversation data transits through and is stored on OpenAI servers in the United States.
- OpenAI reserves the possibility of using data for model training, unless explicitly opted out via account settings or API usage with data retention disabled.
- The user has no direct control over the infrastructure.

**Notion AI:**
- Documents and AI conversations reside on Notion's cloud infrastructure.
- AI queries are processed through third-party providers (typically OpenAI).
- The user is subject to Notion's and its sub-processors' data retention and privacy policies.

**PrivateGPT:**
- Local approach similar to LocalMind, but with limited persistence.
- Does not offer a structured relational database for metadata.
- Does not include integrated automations.
- Less modular and less extensible architecture.

**LocalMind:**
- Combines the advantage of local privacy with an enterprise-grade architecture.
- MySQL for structured and relational persistence.
- Qdrant for high-performance vector search.
- Ollama for LLM inference without cloud dependencies.
- n8n for advanced automations, all runnable locally.

---

## 4. Full Offline Mode

LocalMind is designed to work **completely offline** when configured with Ollama and local models:

### Prerequisites for Offline Operation

1. **Downloaded LLM models**: at least one chat model (e.g., `llama3.2`) and one embedding model (e.g., `nomic-embed-text`) must have been previously downloaded via `ollama pull`.
2. **Infrastructure services started**: MySQL, Qdrant, Ollama, and n8n must be running (natively or via Docker).
3. **Application started**: the Spring Boot backend and Angular frontend must have been compiled and started.

### Features Available Offline

| Feature | Available offline | Notes |
|---|---|---|
| Chat with LLM | Yes | Via local Ollama |
| Document indexing | Yes | Embedding via Ollama |
| Semantic search | Yes | Local Qdrant |
| Q&A on documents | Yes | Full local RAG |
| n8n automations | Yes | Local workflows |
| Dashboard and metrics | Yes | Data from local MySQL |
| Cloud providers (OpenAI, etc.) | **No** | Require internet connection |
| Download new models | **No** | Requires connection for `ollama pull` |

### Offline Use Cases

- **Air-gapped environment**: corporate networks without internet access.
- **Travel**: use on a laptop without connectivity.
- **Classified data**: environments where internet connection is forbidden by security policy.
- **Business continuity**: guaranteed operation even in case of connectivity interruption.

---

## 5. GDPR Compliance

LocalMind is, by its architecture, inherently compliant with the principles of the **General Data Protection Regulation (GDPR - EU Regulation 2016/679)**:

### GDPR Principles Satisfied

| GDPR Principle | How LocalMind Satisfies It |
|---|---|
| **Data minimization** | Only data strictly necessary for operation is collected |
| **Storage limitation** | The user has full control over retention and can delete data at any time |
| **Integrity and confidentiality** | Data resides exclusively on the user's machine, protected by the operating system's security mechanisms |
| **Privacy by design** | The local-first architecture is designed from the ground up for privacy |
| **Privacy by default** | The default configuration does not transmit any data to external services |
| **Right to erasure** | The user can delete any data at any time, with immediate and verifiable effect |
| **Data portability** | Data is in standard formats (MySQL, file system) and easily exportable |

### Absence of Cross-Border Transfer

Since data does not leave the user's machine:

- No data transfer to third countries occurs.
- No adequacy assessment is required (Art. 45 GDPR).
- No Standard Contractual Clauses (SCC) are needed.
- The issues related to the Schrems II ruling do not apply.

**Exception:** when the user voluntarily enables a cloud provider (OpenAI, Anthropic, Google), the data sent in queries is subject to the data processing policies of the selected provider. In that case, it is the user's responsibility to verify the GDPR compliance of the chosen provider.

---

## 6. No Vendor Lock-In

LocalMind adopts a modular architecture based on ports and adapters (Hexagonal Architecture) that guarantees the replaceability of every component:

### Replaceable Components

| Current Component | Possible Alternatives | Port/Interface |
|---|---|---|
| MySQL | PostgreSQL, MariaDB, H2 | Spring Data JPA (abstraction) |
| Qdrant | Chroma, Milvus, Weaviate, Pinecone | `VectorStorePort` |
| Ollama | llama.cpp, vLLM, LocalAI | `LlmPort` |
| Spring AI | LangChain4j | `LlmPort` adapter |
| n8n | Apache Airflow, Prefect | `AutomationPort` |
| Angular | React, Vue.js | REST API (decoupled) |

### Migration Between Components

Thanks to Hexagonal Architecture:

1. **Ports (interfaces)** define the contract between the domain and the infrastructure.
2. **Adapters** implement the contract for a specific technology.
3. To replace a component, it is sufficient to **implement a new adapter** without modifying the domain or application services.

**Example: migration from Qdrant to Chroma**

```
// Existing port (unchanged)
public interface VectorStorePort {
    void store(List<EmbeddingChunk> chunks);
    List<SearchResult> search(String query, int topK);
}

// Current adapter
@Component
@ConditionalOnProperty(name = "localmind.vectorstore.provider", havingValue = "qdrant")
public class QdrantVectorStoreAdapter implements VectorStorePort { ... }

// New adapter (to be implemented)
@Component
@ConditionalOnProperty(name = "localmind.vectorstore.provider", havingValue = "chroma")
public class ChromaVectorStoreAdapter implements VectorStorePort { ... }
```

The replacement only requires:

1. Implementation of the new adapter.
2. Modification of the `localmind.vectorstore.provider` property in the configuration file.
3. No changes to domain code or application services.

This approach ensures that the user is never locked into a specific technology and can evolve their stack according to their needs.
