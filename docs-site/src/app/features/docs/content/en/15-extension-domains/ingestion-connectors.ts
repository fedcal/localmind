export const content = `# Ingestion & Connectors

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This document describes the **Ingestion & connectors** scope (group: *core*): the set of pipelines, adapters and connectors that transform any **source** (documents, mail, Git repositories, external APIs, monitored folders, calendars, messaging channels, databases) into **weighted nodes and edges** of LocalMind's universal Knowledge Graph. It is the *operational heart* that feeds the graph engine described in \`20-motore-knowledge-graph-it.md\`: without a robust, incremental and traceable ingestion, the graph stays empty or, worse, fills up with duplicates and noise. The scope is cross-cutting across all verticals (consumer and enterprise): only *which* connectors are installed and *which* node/relation types are produced changes, but the backbone of the pipeline — extraction, normalization, chunking, embedding, entity/relation extraction, deduplication, upsert into the graph, incremental synchronization — is a single one.

The starting point **is not greenfield**. LocalMind already has a mature document ingestion pipeline (\`DocumentIngestionPipelineService\`), a Spring batch layer (\`localmind-batch\` with \`DocumentIngestionJobConfig\`, \`FolderScanJobConfig\`, \`BatchScheduler\`), de facto connectors to the file system (\`LocalFileSystemScanner\` / \`FileSystemScannerPort\`), text extraction (Apache Tika via \`TikaTextExtractor\`) and OCR (Tesseract via \`TesseractOcrExtractor\`), chunking (\`ChunkingService\`), embedding and vector indexing on Qdrant (\`QdrantVectorStoreAdapter\`), as well as already-active source domains: \`email\` (IMAP/SMTP via Angus Mail), \`calendar\`, \`messaging\` (in/out channels), \`mcp\`. There is also the AI entity extractor (\`EntityExtractorPort\` / \`LlmEntityExtractorAdapter\`) and the \`KnowledgeGraphPort\`. This scope **reuses and generalizes** that base: it transforms the "document → chunk → vector" pipeline into a "source → weighted nodes+edges → graph + vector" pipeline, introducing a uniform **connector abstraction** and **incremental synchronization**.

---

## 1. What we solve (problem & value)

### 1.1 The underlying problem

A Knowledge Graph is worth only as much as the data that populates it. The real bottleneck of any GraphRAG platform is not the traversal algorithm: it is **bringing knowledge in from the sources where it actually lives** — and keeping it up to date over time without manual intervention. Today LocalMind knows how to ingest *one type* of source well (files/documents), but the leap toward the "universal engine" requires solving very specific structural problems:

- **Sources are heterogeneous and scattered.** Enterprise knowledge lives in Drive/SharePoint, in IMAP mailboxes, in Git repositories, in Confluence/Notion/wiki, in application databases, in OpenAPI/Swagger of microservices, in ticketing (Jira/GitHub Issues), in calendars. Consumer knowledge lives in CSV/Excel of local tourist boards, in event feeds (ICS, RSS), in OpenStreetMap/POI, in community contribution forms. Each source has a different protocol, authentication, format and semantics. Without a common connector abstraction, every integration becomes a project of its own.

- **"One-shot" ingestion is not enough: the incremental approach is needed.** Loading documents once is easy; the real problem is **keeping the graph aligned** when sources change. Re-indexing everything on each cycle is expensive (CPU, embeddings, LLM calls for entity extraction) and produces unstable graphs. The 2026 state of the art (Airbyte, Cognee, CDC connectors) has made *incrementality, cursors/watermarks and checkpoints* first-class features: you synchronize **only what is new or changed**, with resumption after transient errors without starting from scratch.

- **Duplicates and ambiguous entities degrade the graph.** The same person ("Mario Rossi", "M. Rossi", "mario.rossi@…"), the same place, the same microservice appear in different sources. Without **deduplication and entity resolution**, the graph fragments into disconnected clones and edge weights get diluted. This is the *entity ambiguity / relation noise* problem flagged by the GraphRAG literature as the main cause of low quality.

- **AI extraction is expensive and must be governed.** Extracting entities and relations with an LLM over the *entire* corpus on each run is unsustainable locally (Ollama). What is needed is extraction **only on the delta**, with caching, batching, and a "sequential by type" mode (first people, then places, then organizations…) that 2026 research indicates as more accurate than joint extraction.

- **Traceability and provenance are missing.** In the enterprise space (audit, compliance, privacy) every node/edge must be able to answer *"from which source, which document, which line/offset, when, with what confidence does this information come?"*. Without **lineage** the GraphRAG answer is not auditable and you cannot do *right-to-be-forgotten* (cascade-deleting everything derived from a removed source).

- **Privacy and local-first are constraints, not options.** Source credentials (Git tokens, IMAP passwords, API keys) and ingested data must never leave the self-hosted instance without consent. AI extraction must be able to run entirely on Ollama. A connector that "phones home" is unacceptable.

- **Every domain risks reinventing the pipeline.** Without a connector framework, the tourism vertical, the enterprise one and the education one would each build three separate ingestions. What is needed is **a single ingestion engine** that is parameterizable, with the specific connectors as PF4J plugins installable from the marketplace.

### 1.2 The solution: a connector-based ingestion framework, incremental and traceable

LocalMind responds with a **unified ingestion framework** built around three new abstractions and the reuse of existing ones:

1. **Connector (\`SourceConnector\`)** — interface that abstracts *how* a source is read: authentication, enumeration of resources, reading of content, detection of changes (cursor/watermark). Concrete implementations (FileSystem, IMAP, Git, REST/OpenAPI, ICS/RSS, CSV, Drive…) are **adapters** in infrastructure or **PF4J plugins** from the marketplace.

2. **Generalized ingestion pipeline (\`IngestionPipeline\`)** — domain-agnostic orchestrator that receives a *RawRecord* from the connector and carries it all the way to the graph through composable stages: normalization → text extraction (Tika/OCR) → chunking → embedding (Qdrant) → entity/relation extraction (LLM) → deduplication/entity resolution → **weighted upsert of nodes and edges** into the \`KnowledgeGraphPort\` → lineage registration. It extends the existing \`DocumentIngestionPipelineService\`, it does not replace it.

3. **Synchronization state (\`IngestionState\` / cursors)** — persistence of per-source watermarks (timestamp, ETag, commit SHA, IMAP UID, Drive change token…) that enables **incremental sync** and resume checkpoints, on the Airbyte model (cursor field + state message).

On this base, ingestion becomes a **continuous and idempotent service**: connectors, scheduled by the \`BatchScheduler\` or activated by webhooks/events, bring in only the delta, transform it into weighted nodes/edges and keep the graph alive and traceable.

### 1.3 Why a single framework for all sources

| Dimension | Current ingestion (documents only) | Ingestion & connectors framework |
|---|---|---|
| Supported sources | File system + upload | Files, mail, Git, REST/API, ICS/RSS, CSV, Drive, DB, messaging… (extensible via plugin) |
| Output | Chunk + vector | Typed nodes + weighted edges + chunk + vector + lineage |
| Update | Folder rescan (full) | Incremental with cursors/watermarks + resume checkpoints |
| Deduplication | File hash (\`fileHash\`) | Hash + semantic entity resolution (merge of equivalent entities) |
| AI extraction | Not connected to the doc pipeline | Integrated, delta-only, batched, sequential by type, Ollama-first |
| Traceability | Document metadata | Full lineage source→node→edge with confidence and timestamp |
| Extensibility | Code in the core | Connectors as PF4J plugins from the marketplace |
| Privacy | Local | Encrypted credentials, local extraction, no external transmission without consent |

### 1.4 Concrete value by adoption type

- **Consumer (tourism, events, culture, sport…):** a local tourist board connects a CSV of POIs, an ICS feed of events and an OSM export; the framework transforms them into *Place/Event/Experience* nodes with *LOCATED_IN / TAKES_PLACE_AT / PAIRABLE_WITH* edges weighted by relevance and timeliness, kept up to date with each new feed. Community contributions enter as one source among others, with moderation.
- **Enterprise (docs, processes, repos, microservices, APIs, people, mail):** Drive, mailboxes, Git repositories and microservice OpenAPI specs are connected; the framework builds the living map of dependencies (*DEPENDS_ON, CALLS, OWNER_OF, MENTIONED_IN*) with auditable lineage and guaranteed privacy. The incremental approach keeps the graph synchronized with every commit/mail/document.
- **Integrators/developers:** they write a new connector by implementing \`SourceConnector\` (or take it from the marketplace) and the graph populates, without touching the pipeline core.

### 1.5 Current state and gaps to fill (brownfield baseline)

| Aspect | Current state (baseline) | Gap / necessary evolution |
|---|---|---|
| Source abstraction | \`FileSystemScannerPort\` (FS only) | Introduce a generic \`SourceConnector\` (auth, list, read, delta) |
| Pipeline | \`DocumentIngestionPipelineService\` (file→chunk→vector) | Generalize to \`IngestionPipeline\` with an "entity/relation extraction → graph upsert" stage |
| Link to the graph | The doc pipeline does **not** feed \`knowledge\` | Add a stage that invokes \`EntityExtractorPort\` + \`KnowledgeGraphPort\` with weight |
| Incrementality | \`FolderScanJobConfig\` rescans (hash for skip) | Per-source persisted cursors/watermarks + Spring Batch checkpoints |
| Deduplication | Only \`fileHash\` at the document level | Entity resolution (exact + semantic match) on nodes |
| Lineage/provenance | Basic metadata in the document | Provenance table source→node/edge with confidence, offset, timestamp |
| Non-file connectors | \`email\`/\`calendar\`/\`messaging\` domains isolated | Wrap them as \`SourceConnector\`s that produce nodes/edges |
| Connector extensibility | Plugin API only has \`DocumentParserExtension\` | Add a \`SourceConnectorExtension\` extension point (PF4J) |
| Scheduling | \`BatchScheduler\` (folder scan) | Generalize to per-connector scheduling + webhook/event triggers |
| Observability | Logs + \`DocumentJobListener\` | Per-connector metrics (records read/dedup/failed, lag), sync state in UI |

---

## 2. Personas & target users

| Persona | Profile | Goals regarding ingestion | Needs from the system |
|---|---|---|---|
| **Administrator / DevOps** | Manages the self-hosted instance | Configure sources, schedules, credentials; monitor sync state | Connector UI, encrypted secret management, metrics, logs, retry |
| **Knowledge / Data engineer** | Curates graph quality | Map sources→node types, deduplication rules, confidence thresholds | Configurable mapping, entity resolution tools, preview/dry-run |
| **Connector developer** | Extends LocalMind with new sources | Write a \`SourceConnector\` as a plugin | Stable SPI, SDK, IT/EN docs, examples, test harness |
| **Domain developer** | Builds a vertical | Define which nodes/edges to produce from each source | Parameterizable pipeline, modular schema, enrichment hooks |
| **Curator / Moderator (consumer)** | Validates community contributions and sources | Approve/reject ingested records before merge into the graph | Staging queue, preview, contribution audit |
| **Compliance / Security officer (enterprise)** | Ensures privacy and auditability | Know data provenance, cascade-delete | Lineage, retention, right-to-be-forgotten, consents |
| **End user** | Benefits indirectly | Have an up-to-date and complete graph | Data freshness, source coverage |
| **AI / LLM agent** | Consumes the populated graph | Obtain rich and traceable context | Nodes/edges with lineage and confidence for citations |

---

## 3. Input requirements

This section is deliberately detailed: it defines *what is needed as input* for a connector and the pipeline to work robustly, securely and incrementally. The requirements are grouped by area.

### 3.1 Source configuration (per connector)

| Field | Type | Required | Description | Validation |
|---|---|---|---|---|
| \`id\` | UUID | yes (gen.) | Identifier of the configured source | \`CHAR(36)\` |
| \`connectorType\` | enum | yes | FILE_SYSTEM, IMAP, GIT, REST_API, OPENAPI, ICS, RSS, CSV, GDRIVE, DATABASE, MESSAGING… | A registered connector must exist |
| \`name\` | string | yes | Human-readable name of the source | non-empty, max 200 |
| \`domainProfile\` | enum | yes | Domain profile (CONSUMER_TURISMO, ENTERPRISE_DOCS…) that determines the node/edge mapping | among the installed profiles |
| \`connectionParams\` | JSON | yes | Specific parameters (path, host, port, base URL, repo URL, SQL query…) | schema per type |
| \`credentialsRef\` | string | depends | Reference to the encrypted secret (never in cleartext) | required if the source is authenticated |
| \`schedule\` | cron | no | Sync scheduling (if not event-driven) | valid cron expression |
| \`incrementalMode\` | enum | yes | FULL_REFRESH, INCREMENTAL_APPEND, INCREMENTAL_DEDUPED | consistent with the connector's capabilities |
| \`cursorField\` | string | depends | Field used as watermark (e.g. \`updatedAt\`, \`commitDate\`, UID) | required if INCREMENTAL |
| \`filters\` | JSON | no | Inclusions/exclusions (glob, IMAP label, Git branch, date range) | syntax per type |
| \`mappingConfig\` | JSON | no | Override of the source→node/relation type mapping | valid against the domain schema |
| \`enabled\` | bool | yes | Enables/disables the sync | default false |

### 3.2 Credentials and secrets (privacy/local-first constraint)

- **Never in cleartext in the DB nor in logs.** Credentials (IMAP password, Git PAT token, REST API key, OAuth refresh token, Drive service account) must be encrypted at-rest (reuse of the pattern already adopted for LLM keys in \`llm_provider_configs\`, evolved toward symmetric encryption with a key from \`.env\`).
- **Minimum scope.** Each connector declares the required permissions (read-only by default). Ingestion must never modify the sources.
- **Explicit consent for cloud sources.** Connectors that leave the local network (Drive, external REST) require opt-in and are highlighted in the UI; by default everything stays on-premise.
- **Rotation and revocation.** Ability to rotate/revoke a secret, invalidating the sync without losing the graph's history.

### 3.3 Content requirements (per record)

Each ingested unit is a **\`RawRecord\`** with at least:

| Field | Description |
|---|---|
| \`sourceId\` + \`externalId\` | Stable identity of the record in the source (for dedup/upsert and deletion) |
| \`contentType\` / \`mimeType\` | To choose the extractor (Tika, OCR, structured parser) |
| \`payload\` | InputStream/bytes or already-extracted text |
| \`metadata\` | Key-value map (author, date, path, label, participants…) |
| \`cursorValue\` | Value of the record's watermark (to advance the state) |
| \`checksum\`/\`hash\` | To detect changes and deduplicate |
| \`acl\` (opt.) | Original permissions/visibility, to propagate to the graph (enterprise) |

Validation constraints: configurable maximum size per record; encoding detected/normalized to UTF-8; MIME verified and not merely trusted from the extension; suspicious binary payloads quarantined.

### 3.4 Mapping requirements (source → graph)

- **Active domain profile.** Determines which \`EntityType\`/\`RelationType\` are allowed and how the \`RawRecord\` fields project onto nodes/edges (e.g. mail sender → \`PERSON\` node, thread → \`MENTIONED_IN\` relations).
- **Modular extensible schema.** Node/relation types must be extensible per domain without recompiling the core (consistent with the gap highlighted in \`20-motore-knowledge-graph-it.md\`).
- **Initial weighting rules.** For each generated edge type, a configurable default formula/weight (see §5).

### 3.5 Non-functional requirements

- **Idempotency:** re-running the sync on the same delta must not create duplicates nor alter weights in a non-deterministic way.
- **Resumability:** checkpoints to resume after crash/timeout (reuse of Spring Batch state + persisted cursors).
- **Backpressure and limits:** rate limit toward the sources and toward Ollama (entity extraction), so as not to saturate local CPU/GPU.
- **Observability:** for each run, counts of records read/new/updated/deduplicated/discarded/failed, duration, lag relative to the source.
- **i18n:** messages, states and enums (connectorType, syncStatus, incrementalMode) exposed bilingually IT/EN to the frontend.
- **Flyway persistence:** new tables (\`ingestion_sources\`, \`ingestion_runs\`, \`ingestion_state\`, \`source_lineage\`) with a single query per migration and UUID \`CHAR(36)\`.

### 3.6 System preconditions

- MySQL and Qdrant reachable; Ollama available (or a cloud provider enabled with consent) for entity extraction and embedding.
- \`knowledge\` domain extended with edge weight and node/relation CRUD (dependency on the *Knowledge Graph Engine* scope).
- Connector registered (core or loaded PF4J plugin).

---

## 4. Activity flow (step-by-step)

The flow describes a complete synchronization cycle of a source, from configuration to graph update. It is designed to be **incremental, idempotent and resumable**, reusing the existing batch layer.

### 4.1 Phase 0 — Source configuration and validation

1. The administrator creates a source from the UI (Angular \`ingestion\`/\`connectors\` feature) choosing \`connectorType\` and domain profile.
2. They enter \`connectionParams\` and credentials; the latter are encrypted and saved as a referenced secret (\`credentialsRef\`).
3. The backend performs a **connectivity validation** (\`SourceConnector.testConnection()\`): authenticates, verifies read-only permissions, counts available resources. Errors returned with user-friendly bilingual messages.
4. \`incrementalMode\`, \`cursorField\`, \`schedule\`, \`filters\` and \`mappingConfig\` are configured. The source is persisted in \`ingestion_sources\` (state \`CONFIGURED\`).

### 4.2 Phase 1 — Synchronization trigger

The sync starts in one of three ways:
1. **Scheduled** — the (generalized) \`BatchScheduler\` launches the job at the source's cron cadence.
2. **Manual** — the user presses "Sync now" from the UI (POST \`/api/v1/ingestion/sources/{id}/sync\`).
3. **Event-driven** — an external webhook (e.g. Git push, new mail) or an internal domain event triggers the sync (reuse of the \`DomainEventPublisherPort\`).

At startup an \`ingestion_runs\` record is created (state \`RUNNING\`, timestamp, trigger). A **per-source lock** is applied to avoid concurrent runs on the same source.

### 4.3 Phase 2 — Delta detection (incremental)

5. The connector reads the last **cursor/watermark** from \`ingestion_state\` (e.g. \`last commit SHA\`, \`max(updatedAt)\`, \`IMAP UIDNEXT\`, Drive \`changeToken\`).
6. \`SourceConnector.listChanges(cursor)\` enumerates **only** the resources new/modified/deleted from the cursor onward (for FULL_REFRESH, it enumerates everything).
   - **Deletions** at the source are propagated as tombstones (for cascade deletion in the graph).
7. A stream of \`RawRecord\` is produced with its respective \`cursorValue\`, in batches sized for backpressure.

> Reference pattern (Airbyte): cursor field + state message; the cursor advances **only after** the batch has been persisted successfully, so a crash resumes from the last checkpoint without reprocessing everything.

### 4.4 Phase 3 — Content extraction and normalization

8. For each \`RawRecord\`, based on the \`mimeType\`:
   - text/documents → \`TextExtractorPort\` (Tika);
   - images/scans → \`OcrExtractorPort\` (Tesseract);
   - structured formats (CSV, JSON, OpenAPI, ICS, MIME email) → the connector's dedicated parsers;
9. The text is normalized (UTF-8 encoding, cleanup, detected language) and enriched with the source's metadata. The hash for deduplication is computed/confirmed.

### 4.5 Phase 4 — Chunking and embedding (reuse)

10. The normalized text passes to the existing \`ChunkingService\` (chunks sized with overlap).
11. Each chunk is embedded and upserted into **Qdrant** (\`QdrantVectorStoreAdapter\`), with a reference to \`sourceId\`/\`externalId\`. This provides the **semantic seed** of the GraphRAG.

### 4.6 Phase 5 — Entity and relation extraction (AI, delta-only)

12. On the delta text, \`EntityExtractorPort.extractEntities()\` and \`extractRelations()\` are invoked (adapter \`LlmEntityExtractorAdapter\`, Ollama-first).
    - **Delta-only**, batched, with a **sequential-by-type** strategy (first people, then places, then organizations…) to reduce missed entities, and with caching for unchanged texts.
13. The raw entities/relations are **mapped** onto the active domain profile's types (\`mappingConfig\`), discarding what does not fit the schema.

### 4.7 Phase 6 — Deduplication & entity resolution

14. For each candidate entity, an equivalent existing node is sought:
    - **exact match** on natural keys (email, repo URL, external id, hash);
    - **semantic match** (embedding similarity above threshold) for textual variants ("Mario Rossi" ≈ "M. Rossi").
15. If found → **merge** (update properties, accumulate provenance, reinforce the weight); if not found → new node. The resolution is recorded for audit and is reversible.

### 4.8 Phase 7 — Weighted upsert of nodes and edges into the graph

16. Nodes and edges are inserted/updated via \`KnowledgeGraphPort\` with a **weight** computed (see §5): AI extraction confidence × signal strength × recency, accumulated over repeated co-occurrences.
17. The operation is **idempotent**: the same re-ingested record reinforces (does not duplicate) the edges; properties are updated immutably (new version, not in-place mutation — consistent with the project's coding rules).

### 4.9 Phase 8 — Lineage, deletions and state advancement

18. For each produced node/edge, a **provenance** row is written in \`source_lineage\` (source, externalId, offset, confidence, timestamp, runId).
19. The **tombstones** (Phase 2) trigger cascade deletion: nodes/edges whose *sole* provenance was the deleted resource are removed/decayed (right-to-be-forgotten).
20. After the batch commit, the **cursor is advanced** in \`ingestion_state\` and \`ingestion_runs\` is updated (state \`SUCCESS\`, counts, duration, lag).

### 4.10 Phase 9 — Outcome, errors and observability

21. In case of an error on a record, retry with backoff is applied (Spring Retry); unrecoverable records end up in a **dead-letter** with a reason, without blocking the entire run.
22. Metrics are published (records read/new/updated/deduplicated/discarded/failed, duration, lag) and an \`IngestionCompletedEvent\`.
23. The UI shows the run history, the freshness state per source, and the errors in the user's language. In case of a crash, the next run resumes from the last cursor/checkpoint.

### 4.11 Synthetic flow diagram

\`\`\`text
[Source] → SourceConnector(testConnection/listChanges/read)
   │  (cursor/watermark from ingestion_state)
   ▼
RawRecord(batch) → Tika/OCR/parser → normalize → ChunkingService → embed → Qdrant
   │
   ├──→ EntityExtractor (LLM, delta only, sequential by type)
   │        ▼
   │     map → deduplication/entity resolution → weighted upsert → KnowledgeGraphPort (MySQL)
   │                                                              │
   │                                                              ▼
   │                                                        source_lineage
   ▼
batch commit → advance cursor (ingestion_state) → ingestion_runs(SUCCESS) → metrics/events
\`\`\`

---

## 5. Graph model (node types, relation types, weight criteria)

The *Ingestion* scope does not invent its own domain schema: it **feeds** that of the graph engine. However, it introduces **provenance** (lineage) nodes and relations that live alongside the domain ones, and it produces the domain types starting from the sources.

### 5.1 Node types produced / introduced

| Category | Node type | Typical origin | Notes |
|---|---|---|---|
| Provenance | \`SOURCE\` | Configured source | Node representing the source (mailbox, repo, folder…) |
| Provenance | \`INGESTION_RUN\` | Sync execution | For audit/temporal lineage |
| Provenance | \`RAW_RECORD\` | Document/mail/file/commit | Ingested unit, anchor for lineage |
| Domain (reuse) | \`PERSON\` | Mail, repo, doc | Senders, authors, owners |
| Domain (reuse) | \`ORGANIZATION\` | Doc, mail | Companies, teams |
| Domain (reuse) | \`PLACE\` | CSV/OSM/ICS (consumer) | Places, POIs |
| Domain (reuse) | \`EVENT\` | ICS/RSS, calendar | Events |
| Domain (reuse) | \`DOCUMENT\` | File, Drive | Documents |
| Domain (reuse) | \`TECHNOLOGY\` | Repo, OpenAPI | Microservices, APIs, libraries |
| Domain (reuse) | \`CONCEPT\` | Free text | Themes, topics |
| Domain (ext) | \`API_ENDPOINT\`, \`REPOSITORY\`, \`TICKET\`, \`EXPERIENCE\`… | Specific connectors | Extended types per domain (modular schema) |

### 5.2 Relation types produced / introduced

| Category | Relation type | Meaning | Origin |
|---|---|---|---|
| Provenance | \`EXTRACTED_FROM\` | Domain node/edge derives from a \`RAW_RECORD\` | Lineage |
| Provenance | \`INGESTED_BY\` | \`RAW_RECORD\` ingested by an \`INGESTION_RUN\` | Lineage |
| Provenance | \`PROVIDED_BY\` | \`RAW_RECORD\` provided by a \`SOURCE\` | Lineage |
| Domain (reuse) | \`MENTIONED_IN\` | Entity cited in a record | AI extraction |
| Domain (reuse) | \`CREATED_BY\` / \`WORKS_AT\` | Authorship/affiliation | Mail, doc, repo |
| Domain (reuse) | \`LOCATED_IN\` | Localization | Consumer |
| Domain (reuse) | \`DEPENDS_ON\` / \`REFERENCES\` | Technical dependencies | Repo, OpenAPI |
| Domain (reuse) | \`PART_OF\` / \`RELATED_TO\` | Aggregation/affinity | Generic |
| Domain (ext) | \`CALLS\`, \`OWNER_OF\`, \`REPLIES_TO\`, \`ATTENDS\`… | Extended relations | Specific connectors |

### 5.3 Edge weight criteria

The weight produced during ingestion is the **initial value** that the engine then evolves. It is composed of normalized and configurable factors:

| Factor | Meaning for ingestion | How it is computed |
|---|---|---|
| **Extraction confidence** | How sure the LLM is about the relation | Extractor score (\`EntityExtractorPort\`), 0–1 |
| **Signal strength** | How "explicit" the relation is in the source | E.g. a dependency declared in \`pom.xml\` > textual co-occurrence |
| **Frequency/co-occurrence** | Repetition of the relation across multiple records/sources | Incremental accumulation at each re-ingestion |
| **Recency** | Timeliness of the information | Temporal decay based on the record's \`cursorValue\`/date |
| **Source reliability** | Trust in the source | Weight per \`SOURCE\` (e.g. official repo > informal mail) |
| **Feedback/curation** | Human confirmations or corrections | Increases/decreases the weight (consumer: votes; enterprise: validation) |

Operational rules:
- Final weight = configurable function (by default a weighted average normalized to 0–1) of the factors above.
- **Idempotent accumulation:** re-ingesting the same relation reinforces the weight (asymptotic saturation), it does not double it.
- **Decay:** edges no longer confirmed by new ingestions decay over time, until pruning.
- Each edge retains the **component factors** (not just the final weight) for explainability and recomputation.

---

## 6. Data sources & connectors (ingestion)

### 6.1 The \`SourceConnector\` abstraction

All connectors implement a single port (port/out of the \`ingestion\` domain), with declared capabilities:

\`\`\`text
SourceConnector
  ConnectorType type()
  ConnectionTest testConnection(SourceConfig cfg)
  Stream<RawRecord> listChanges(SourceConfig cfg, Cursor cursor)   // delta or full
  InputStream read(RawRecord ref)
  Set<Capability> capabilities()   // INCREMENTAL, DELETES, ACL, WEBHOOK...
\`\`\`

Connectors are **adapters** in \`localmind-infrastructure/ingestion/adapter/\` or **PF4J plugins** loaded at runtime. The existing \`FileSystemScannerPort\` is adapted as the first \`SourceConnector\` (FILE_SYSTEM).

### 6.2 Connector catalog

| Connector | Source | Incremental (cursor) | Existing reuse | Priority |
|---|---|---|---|---|
| **FILE_SYSTEM** | Local folders | mtime + file hash | \`LocalFileSystemScanner\`, \`FolderScanJobConfig\` | MVP |
| **UPLOAD** | Manual upload | n/a (one-shot) | \`DocumentController.upload\` | MVP |
| **IMAP/Email** | Mailbox | UID / UIDNEXT, INTERNALDATE | \`email\` domain (Angus Mail) | MVP→core |
| **ICS/Calendar** | Calendars | LAST-MODIFIED / SEQUENCE | \`calendar\` domain | Evolution |
| **Git** | Repository | commit SHA / date | new (JGit) | Evolution (enterprise) |
| **OpenAPI/REST** | Microservice APIs | ETag / spec version | new | Evolution (enterprise) |
| **CSV/Excel** | Tabular datasets | row hash / date column | new parser | MVP (consumer) |
| **RSS/Atom** | Feed | GUID / pubDate | new | Evolution (consumer) |
| **Google Drive / SharePoint** | Cloud storage | changeToken / delta | new (OAuth) | Evolution |
| **Messaging** | In/out channels | message id / timestamp | \`messaging\` domain | Evolution |
| **Database/JDBC** | Application DBs | cursor column / CDC | new | Future |
| **Wiki (Confluence/Notion)** | Knowledge base | page version | plugin | Future |
| **Ticketing (Jira/GitHub Issues)** | Tickets | updated / id | plugin | Future |

### 6.3 Synchronization modes (Airbyte model)

| Mode | When to use it | Behavior |
|---|---|---|
| **FULL_REFRESH** | Small sources or those without a reliable cursor | Re-reads everything; idempotent upsert |
| **INCREMENTAL_APPEND** | Append-only sources (logs, feeds) | Only records after the cursor |
| **INCREMENTAL_DEDUPED** | Mutable sources (mail, doc, repo) | Only delta + deduplication by primary key, keeps the most recent version |

### 6.4 Extensibility via plugin

The **\`SourceConnectorExtension\`** extension point is introduced in \`localmind-plugin-api\` (alongside \`DocumentParserExtension\`, \`LlmProviderExtension\`, \`VectorStoreExtension\`). Third-party connectors are installed from the **marketplace** as PF4J JARs, declaring capabilities and parameters; the core discovers them and exposes them in the UI without recompilation.

---

## 7. Features to create, develop and maintain (MVP → evolution)

### 7.1 MVP (framework foundations)

| # | Feature | Type | Detail |
|---|---|---|---|
| 1 | \`ingestion\` domain | **Create** | \`model/\` (SourceConfig, RawRecord, Cursor, IngestionRun, ConnectorType, SyncStatus enum), \`port/in\` (\`IngestionUseCase\`), \`port/out\` (\`SourceConnector\`, \`IngestionStateRepository\`, \`LineageRepository\`) |
| 2 | Generalized \`IngestionPipeline\` | **Create** (extends doc) | Stage orchestrator: normalize→chunk→embed→extract→dedup→upsert→lineage |
| 3 | "Graph" stage in the pipeline | **Develop** | Connect the existing doc pipeline to \`EntityExtractorPort\` + \`KnowledgeGraphPort\` (currently disconnected) |
| 4 | FILE_SYSTEM connector | **Develop** (reuse) | Adapt \`FileSystemScannerPort\` to \`SourceConnector\` with mtime+hash cursor |
| 5 | IMAP connector | **Develop** (reuse) | Wrap the \`email\` domain as a \`SourceConnector\` with UID cursor |
| 6 | CSV connector | **Create** | Tabular parser + columns→nodes mapping (consumer) |
| 7 | Incremental sync + cursors | **Create** | \`ingestion_state\` + Spring Batch checkpoint; advancement only at commit |
| 8 | Basic deduplication | **Create** | Exact match on natural keys + hash (semantic entity resolution in evolution) |
| 9 | Lineage/provenance | **Create** | \`source_lineage\` table + writing for each node/edge |
| 10 | Encrypted secret management | **Create** | Encryption of connector credentials (evolution of the \`llm_provider_configs\` pattern) |
| 11 | Per-connector scheduling | **Develop** (reuse) | Generalize \`BatchScheduler\` beyond folder scan |
| 12 | REST API \`/api/v1/ingestion/*\` | **Create** | Source CRUD, connection test, sync now, run history, state |
| 13 | Angular UI \`connectors\`/\`ingestion\` | **Create** | Source list, connector wizard, sync state, error logs (IT/EN) |
| 14 | Flyway migrations | **Create** | \`ingestion_sources\`, \`ingestion_runs\`, \`ingestion_state\`, \`source_lineage\` (one query/file) |
| 15 | Basic observability | **Develop** | Run counts + \`IngestionCompletedEvent\` + Micrometer metrics |

### 7.2 Evolutions (post-MVP)

| # | Feature | Type | Detail |
|---|---|---|---|
| 16 | Semantic entity resolution | Create | Merge nodes via embedding similarity + thresholds + reversible audit |
| 17 | Optimized AI extraction | Develop | Sequential by type, batching, caching on delta, Ollama budget |
| 18 | Cascade deletion (tombstone) | Create | Right-to-be-forgotten driven by lineage |
| 19 | Git / OpenAPI / RSS / ICS connectors | Create | Enterprise/consumer verticals |
| 20 | \`SourceConnectorExtension\` (PF4J) | Create | Connectors from the marketplace |
| 21 | Event-driven ingestion (webhook) | Create | Git/mail push → immediate sync |
| 22 | Staging & moderation (consumer) | Create | Review queue before merge into the graph |
| 23 | Dead-letter & advanced retry | Develop | Record quarantine, replay |
| 24 | Drive/SharePoint/DB/CDC connectors | Create | Cloud and database sources, also streaming |
| 25 | Freshness & lag dashboard | Create | Per-source KPIs, staleness alarms |

### 7.3 Continuous maintenance

- Update parsers/extractors (Tika, OCR) and adapt connectors to the APIs of sources that change.
- Keep cursors robust (handle reset, clock skew, realignments).
- Monitor costs/times of local AI extraction and tune batches/thresholds.
- Keep the modular node/edge schema aligned when new domains arise.
- Update IT/EN documentation and the developments in the \`Sviluppi/\` folder.

---

## 8. AI / GraphRAG use cases

Ingestion is an enabler: the richer and fresher it is, the more powerful the GraphRAG. Concrete cases:

- **Impact analysis (enterprise):** "If I change the \`/orders\` endpoint, which microservices and owners are impacted?" — the OpenAPI+Git connector produced \`API_ENDPOINT\`, \`REPOSITORY\`, weighted \`CALLS\`/\`DEPENDS_ON\` edges and \`OWNER_OF\`; the AI traverses the strong edges and cites the sources.
- **Accelerated onboarding:** a new hire asks "who knows about X and where is the doc?" — the graph, populated from mail+Drive+repo, responds with people, documents and paths, with lineage.
- **Consumer discovery:** "experiences pairable with this village for this weekend" — the CSV/ICS connectors produced \`PLACE\`/\`EVENT\`/\`EXPERIENCE\` with \`LOCATED_IN\`/\`TAKES_PLACE_AT\` edges updated to the most recent feed.
- **Non-obvious connections:** the AI suggests missing links between entities ingested from different sources (link prediction over the accumulated weights).
- **Traceable answers:** every AI statement is anchored to the lineage (\`EXTRACTED_FROM\` → \`RAW_RECORD\` → \`SOURCE\`), a requirement for audit/compliance.
- **AI as an ingestion orchestrator:** an agent that, on request, launches the sync of a specific source via an MCP tool before responding.

---

## 9. KPIs & success metrics

| Category | KPI | Indicative target |
|---|---|---|
| Coverage | No. of active connector types | ≥ 5 (MVP), ≥ 10 (evolution) |
| Freshness | Average source→graph lag | < 15 min (event-driven), < 24h (scheduled) |
| Efficiency | % of records processed incrementally (not full) | > 90% after the first sync |
| Quality | Duplicate rate in the graph | < 2% of nodes |
| Quality | Entity resolution precision (correct matches) | > 90% on a validation set |
| Reliability | % of runs completed without intervention | > 98% |
| Reliability | % of records in dead-letter | < 1% |
| Local cost | Average AI extraction time per MB of delta | tracked and decreasing over time |
| Traceability | % of nodes/edges with full lineage | 100% |
| Privacy | Cleartext credentials in the DB/logs | 0 (verified in security review) |
| Adoption | No. of connectors installed from the marketplace | growing |

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Explosion of duplicates without good entity resolution | Fragmented graph, diluted weights | Exact dedup from MVP; semantic resolution with thresholds + reversible audit |
| Cost of local AI extraction (Ollama) | Slow syncs, saturated CPU/GPU | Delta-only extraction, batching, caching, sequential by type, rate limit |
| Unreliable cursors (clock skew, source reset) | Records lost or reprocessed endlessly | Robust cursors + hash fallback + periodic reconciliation runs |
| Exposed credentials | Privacy/security breach | At-rest encryption, read-only scope, no logging of secrets, security review |
| Sources that change API/format | Broken connectors | Versioned capabilities, connection tests, connectors as updatable plugins |
| Non-idempotent re-ingestion | Inflated weights, unstable graph | Idempotent upsert with asymptotic accumulation + stable keys |
| Data deleted at the source remains in the graph | Compliance/right-to-be-forgotten | Tombstone + cascade deletion driven by lineage |
| Malicious plugin connector from the marketplace | Data exfiltration | PF4J sandbox, declared permissions, marketplace review, offline default |
| Missing backpressure toward external sources | Ban/rate-limit from the source | Configurable throttling and respect for rate limits |
| Monolithic pipeline hard to extend | Technical debt | Composable stages, small and cohesive files (project rules) |

---

## 11. Maintenance & evolution

- **Connector versioning:** each connector declares version and capabilities; source breakages are handled by updating the plugin without touching the core.
- **Periodic reconciliation:** in addition to the incremental approach, a scheduled full-refresh job (at a low cadence) catches drift and lost cursors.
- **Continuous tuning:** dedup thresholds, weight formulas, batch sizes and AI budget must be monitored and tuned via configuration (no hardcoding).
- **Living modular schema:** when a new domain arises, node/relation types and mappings are added without recompiling the engine.
- **Observability as a first-class citizen:** freshness/lag dashboards, staleness and dead-letter alarms, lineage audit.
- **Documentation and developments:** each connector documented IT/EN (usage, parameters, permissions); each development tracked in \`Sviluppi/\` with dated naming; development in plan mode.
- **Tests:** unit (parsers, cursors, dedup), integration (Testcontainers MySQL, mock sources), E2E (Playwright on the connector UI); coverage ≥ 80%.

---

## 12. Integration with existing LocalMind modules

| Module / component | Role in ingestion | Integration point |
|---|---|---|
| **\`document\`** | Base of the pipeline (extraction, chunking, vector) | \`DocumentIngestionPipelineService\` → generalized into \`IngestionPipeline\`; reuse of \`TextExtractorPort\`, \`OcrExtractorPort\`, \`ChunkingService\`, \`VectorStorePort\` |
| **\`knowledge\`** | Destination: weighted nodes and edges | \`EntityExtractorPort\` (extraction) + \`KnowledgeGraphPort\` (upsert); depends on adding the weight (KG Engine scope) |
| **\`localmind-batch\`** | Orchestration and scheduling | \`DocumentIngestionJobConfig\`/\`FolderScanJobConfig\` generalized; \`BatchScheduler\` per-connector; Spring Batch checkpoint/state for resumption |
| **\`email\`** | IMAP connector | \`EmailPort\` (Angus Mail) wrapped as a \`SourceConnector\` |
| **\`calendar\`** | ICS connector | Events as \`EVENT\` nodes |
| **\`messaging\`** | Channels connector | \`MessagingClientPort\` as a source (in evolution) |
| **\`mcp\`** | Ingestion via tool / AI orchestrator | The AI can launch syncs or read sources via MCP |
| **\`plugin\` (PF4J) + \`localmind-plugin-api\`** | Third-party connectors | New \`SourceConnectorExtension\` alongside \`DocumentParserExtension\` |
| **\`marketplace\`** | Connector distribution | Installation/update of connectors as PF4J JARs |
| **\`llm\` / Ollama** | Entity/relation extraction and embedding | \`EntityExtractorPort\` (Ollama-first), \`@Primary\` Ollama embedding |
| **\`common\` (events)** | Ingestion events and analytics | \`DomainEventPublisherPort\` for \`IngestionCompletedEvent\`; event-driven trigger |
| **\`auth\` / security** | API and secret protection | \`LocalAuthFilter\`; connector credential encryption |
| **Qdrant** | Semantic index (GraphRAG seed) | \`QdrantVectorStoreAdapter\` with reference to \`sourceId\`/\`externalId\` |
| **MySQL + Flyway** | State, sources, lineage | New tables (one query per migration, UUID \`CHAR(36)\`) |
| **Angular Frontend** | Connector UI and monitoring | New \`ingestion\`/\`connectors\` feature (standalone, Signals, i18n IT/EN) |

### 12.1 Consistency with project constraints

- **Local-first / privacy:** read-only connectors, encrypted credentials, extraction on Ollama, no external transmission without explicit consent (cloud sources opt-in).
- **MySQL + Qdrant reuse:** no new datastore; the sync state and lineage live in MySQL, the embeddings in Qdrant.
- **Hexagonal architecture:** pure \`ingestion\` domain (no Spring), connectors as adapters/plugins, wiring in \`DomainConfig\`.
- **i18n IT/EN:** enums (\`ConnectorType\`, \`SyncStatus\`, \`IncrementalMode\`) translated and routed to the frontend according to the language switch.
- **Flyway:** a single query per file; record immutability (upsert as a new version, not in-place mutation).
- **Small and cohesive files:** each connector and each pipeline stage in separate modules (200–400 lines typical).

---

*Guidance document for the developments of the "Ingestion & connectors" scope (core). The detailed choices (exact signatures, table names, weight formulas) are to be consolidated in plan mode before implementation, updating the \`Sviluppi/\` folder and the bilingual documentation.*
`;
