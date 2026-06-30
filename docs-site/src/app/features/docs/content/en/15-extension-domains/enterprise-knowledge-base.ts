export const content = `# Enterprise Knowledge Base

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

## 1. What we solve (problem & value)

### 1.1 The problem of fragmented enterprise knowledge

In any organization of non-trivial size, knowledge exists, but it is **scattered, duplicated, contradictory, and not navigable**. An operating procedure lives on Confluence, its "true but outdated" version on a PDF in SharePoint, the explanation of *why* it is done that way only in a Slack thread from eight months ago, the exception that applies to a specific customer in an email, and the decisive detail only in the head of the colleague who attended the meeting. The result is a set of recurring and costly symptoms:

- **Fragmentation across tools.** Knowledge is spread across wikis (Confluence, Notion), file systems (SharePoint, Google Drive), chats (Slack, Teams), ticketing (Jira, ServiceNow), repositories (Git), email, and calendars. None of these systems knows about the others: the same piece of information exists in multiple copies that diverge over time.
- **Tacit knowledge never written down.** Most institutional knowledge is undocumented: it lives in conversations, call recordings, scattered notes, and in people's experience. When a person leaves, that knowledge leaves the company with them.
- **Stale and untrustworthy documentation.** A wiki page has no way to say "I am obsolete." The user does not know whether the procedure they are reading is still valid; when in doubt, they ask a colleague, generating interruptions and perpetuating word-of-mouth.
- **Implicit and invisible connections.** "Which services depend on this API?", "Which procedure is impacted if I change this process?", "Who is the expert on this module?" — these are relationship questions, but documentation systems are made of isolated pages with a few manual links, not of a navigable graph. The relationship between two pieces of information exists only in the mind of whoever connected them.
- **Keyword search that does not understand meaning.** Wiki text search finds pages that contain the words typed, not the pages that answer the question. Complex, multi-step questions ("why did we decide to use X instead of Y, and which services are affected?") remain without a direct answer.
- **Slow and redundant onboarding.** A new hire takes weeks to understand "how things work here" because they have to manually reconstruct a map that no one has ever drawn. The same questions are asked and re-asked endlessly.
- **Compliance risk and loss of traceability.** Without a governed representation of who-knows-what and which-version-is-valid, audits, certifications, and risk management become manual and fragile exercises.

### 1.2 Our answer: a single enterprise knowledge graph

LocalMind, in its evolution into a **universal knowledge graph engine**, addresses exactly this problem. The value proposition for the enterprise domain is to build a **unified enterprise knowledge graph**: a weighted network in which the nodes are the units of knowledge (documents, procedures, processes, FAQs, decisions, people, systems, tickets…) and the edges represent operationally meaningful relationships — "documents", "is a prerequisite of", "supersedes", "depends on", "is an expert in", "contradicts", "derives from the decision". On top of this graph, three capabilities are grafted that no traditional wiki offers together:

1. **Unification and de-fragmentation.** Content coming from heterogeneous sources (wiki, drive, chat, ticket, repo, mail) is ingested, segmented, vectorized on Qdrant, and connected in the graph. The same information present in multiple copies is recognized, related, and — where possible — reconciled into a single unit of knowledge with its variants tracked.
2. **GraphRAG assistant over internal knowledge.** The AI (Ollama locally by default) answers employees' questions by navigating the graph and retrieving the semantically relevant fragments, **always citing the exact source** (document, section, version, author). The answers combine graph relationships + semantic search: multi-hop questions become traversals, not attempts. No enterprise data leaves the infrastructure without explicit consent.
3. **Living and governed knowledge.** The graph knows the freshness of every node, its authoritativeness, who oversees it, and how many times it has actually been used. It can flag obsolete documentation, contradictions between sources, gaps ("no procedure covers this process"), and missing links, transforming the knowledge base from a static archive into a system that maintains itself.

### 1.3 Why a graph, and not yet another wiki

The differentiator is not "another place to write documents," but the **relationship layer** on top of the documents that already exist. Confluence and Notion are excellent page editors but model knowledge as trees of pages with manual links: the structure is hierarchical and static, the relationships are poor and unweighted, the search is lexical. A weighted graph makes entities and relationships **first-class objects** with properties, timestamps, and confidence scores, and enables reasoning that a tree of pages cannot do: dependency paths, neighborhoods, thematic subgraphs, contradiction detection, and obsolescence detection.

| Enterprise need | Limit of Confluence/Notion | How LocalMind satisfies it |
|---|---|---|
| Finding *answers*, not pages | Keyword search | **GraphRAG**: semantics (Qdrant) + graph relationships, with source citation |
| Multi-hop questions ("what depends on X?") | Requires manual navigation of links | Weighted graph traversal on the AI side |
| Knowing whether content is still valid | No freshness signal | Weight/freshness/authoritativeness on the node, alerts on stale content |
| Privacy of enterprise data | Often cloud-only SaaS | **Local-first / self-hosted**, local Ollama AI by default |
| Per-user license costs | Per-seat pricing | **Pure open source**, no paywall |
| Joining heterogeneous sources without migration | Separate silos, paid connectors | Ingestion connectors into the graph, sources stay where they are |
| Extending to proprietary systems | Closed marketplace | **PF4J plugins** + marketplace, custom connectors |
| Bilingual IT/EN | Variable | Bilingual platform by design, translated enums |

In summary: LocalMind does not replace the tools where knowledge is born, but builds on top of them the **AI-navigable connective tissue** that is missing today, reducing fragmentation without imposing a migration and without exfiltrating data.

## 2. Personas & target users

| Persona | Profile | Primary needs | How they use LocalMind |
|---|---|---|---|
| **Marta, new hire (onboarding)** | Joined 2 weeks ago, needs to become productive | Understand "how things work here", who to turn to, where the procedures are | Asks natural-language questions to the assistant; explores the graph to orient herself among processes and teams |
| **Luca, senior knowledge worker** | 8 years in the company, repository of much tacit knowledge | Stop always answering the same questions; leave a trace of decisions | Answers once, the assistant reuses; curates the nodes in his area; validates suggested links |
| **Sara, knowledge manager / documentalist** | Responsible for the quality of the knowledge base | Reduce duplicates and stale content, fill gaps, govern the ontology | Uses dashboards on freshness/contradictions/gaps; curates node and relationship types; manages curation |
| **Davide, IT / platform engineer** | Maintains services, APIs, infrastructure | Map dependencies, understand the impact of a change | Connects repos/services/APIs in the graph; queries "what depends on…"; uses linked ADRs and runbooks |
| **Elena, IT admin / self-hoster** | Installs and manages LocalMind on-prem | Local-first deployment, access control, connectors, privacy | Configures connectors, permissions, local AI provider; monitors ingestion and security |
| **Giovanni, team lead / manager** | Coordinates a team, makes decisions | Big-picture view, find the right expert, traceability of decisions | Explores the graph of competencies and decisions; asks the AI for thematic summaries |
| **Support / help desk operator** | Answers customers or colleagues | Quick and well-founded answers from FAQs and procedures | Uses the assistant as a single source; flags wrong answers or gaps |
| **Auditor / compliance (secondary)** | Verifies compliance and traceability | Know which version is valid, who is responsible | Consults history, versions, and responsibilities of nodes |

The primary user for the MVP is the pairing of **knowledge worker + new hire** mediated by the **knowledge manager**: maximum pain (fragmentation, repeated questions, slow onboarding) and maximum immediate value. The other personas drive the evolutions (IT dependencies, compliance, support).

## 3. Input requirements

This section defines in detail **what must be able to enter the system** for the enterprise knowledge base to work. Inputs are divided into: content to ingest, structure and governance metadata, identity and permissions, configuration, and feedback. Each input must be validated at the system boundary ("never trust external data" principle) and treated immutably: each revision generates a new version, never an in-place mutation.

### 3.1 Content to ingest (units of knowledge)

| Content type | Typical sources / formats | Extraction | Notes |
|---|---|---|---|
| Wiki pages | Confluence, Notion, MediaWiki (HTML/export/API) | Fetch + text extraction, preserves space/page hierarchy | Keep internal links to infer edges |
| Office documents | PDF, DOCX, XLSX, PPTX, ODT | Tika; OCR (Tesseract) for scans and images | Keep page/section number for precise citation |
| Procedures / SOPs / runbooks | PDF, DOCX, Markdown, wiki | Tika + segmentation by step | Become "Procedure" nodes with ordered steps |
| FAQ | CSV/JSON, wiki pages, help desk export | Parsing of question-answer pairs | "FAQ" node linked to procedures/processes/documents |
| Architecture decisions (ADR) | Markdown in repos, wiki | Tika + extraction of context/decision/consequences | "Decision" node linked to systems/processes |
| Code and technical documentation | Git repositories (README, docstring, OpenAPI/Swagger) | Repo parser + API/service extraction | "Repository", "Service", "API", "Database" nodes |
| Conversations / chat | Slack, Teams (export/API) | Thread extraction, noise deduplication | Tacit knowledge; lower authoritativeness weight |
| Tickets / issues | Jira, ServiceNow, GitHub Issues | Extraction of title/description/resolution | "Ticket" node linked to procedures/systems |
| Email | IMAP via existing \`email\` (Angus Mail) | Body/attachment extraction, threading | Tacit knowledge, maximum attention to privacy |
| Meeting recordings | Audio/video (MP3, WAV, MP4) | Transcription via \`WhisperTranscriptionAdapter\` | Timestamps kept for citation to the minute |
| Calendar / events | Existing \`calendar\` module | Extraction of meetings/deadlines | Links decisions and people to events |
| Intranet / web pages | HTML URL | Fetch + extraction, snapshot for reproducibility | Local saving for local-first consistency |

Cross-cutting requirements on content:
- **Maximum size per file** configurable (reasonable default, e.g., 100 MB) with a clear error message when exceeded.
- **Language** automatically detected (IT/EN and beyond) to choose the embedding model and the response language of the assistant.
- **Deduplication and near-duplicate detection**: recognize identical content (hash) and similar content (embedding similarity) to avoid duplicate nodes and to reconcile divergent copies.
- **Provenance always tracked**: every chunk and every node must trace back to source, position (page/section/minute), version, and original author.
- **Source permissions preserved**: the source's ACL (who could read the page) must travel with the content and constrain who can see it in LocalMind.

### 3.2 Structure and governance metadata

To make sense of the content, metadata is needed, provided by the user or inferred and confirmed:

- **Taxonomy / areas**: department, team, product, functional domain to which the content belongs.
- **Type and lifecycle**: draft / under review / approved / obsolete; creation date, last modification, **next review date**.
- **Owner and reviewer**: the person or team that oversees the node (essential for freshness and for "ask the expert").
- **Source authoritativeness**: official (approved wiki) vs informal (Slack thread) — feeds the weight of the edges and the trust in answers.
- **Confidentiality classification**: internal public / restricted / confidential — guides visibility and AI policies.
- **Version**: every piece of content is versioned; the graph knows which is the valid version and which supersede it.
- **Glossary / domain ontology**: terms, synonyms, company acronyms, to be used to normalize entities and improve extraction.

### 3.3 Identity, permissions, and organization

These are inputs that make the knowledge base secure and "aware of who-knows-what":

- **People directory**: name, role, team, declared competencies (integrable with LDAP/SSO in evolution, manual in the MVP).
- **Organizational structure**: teams, departments, hierarchical relationships, to contextualize permissions and responsibilities.
- **Permission map**: who can see what (ACLs inherited from sources + local rules), who can modify the graph, who curates the ontology.
- **Systems map**: inventory of services, APIs, databases, infrastructures and their owners, the basis for the dependency graph.

### 3.4 System configuration

- **LLM provider and model** (default local Ollama; cloud optional with explicit consent), embedding model, interface language (IT/EN).
- **Connectors and sources**: monitored local folders (existing folder watcher), wiki/drive/chat/ticket/repo connectors (see §6), with synchronization frequency (one-shot, periodic, manual).
- **Privacy policies**: what can be sent to a cloud provider (by default nothing), what can be anonymized, which sources are excluded from the AI.
- **Graph parameters**: weights of relevance factors, freshness/obsolescence thresholds, similarity threshold for deduplication, aggressiveness of link suggestions.
- **Retention policies**: how long to keep historical versions, how to handle content deleted at the source.

### 3.5 User feedback (continuous loop)

- **Corrections to the graph**: add/remove/relabel nodes and edges, confirm or reject the links suggested by the AI — this feedback feeds the weight of the edges (§5).
- **Evaluation of the assistant's answers**: thumbs up/down, flagging of out-of-context or unfounded answers, indication of the correct source.
- **Reporting of obsolescence/contradiction**: "this procedure is outdated", "these two pages contradict each other" — triggers curation.
- **Authoritativeness marking**: validation of content as "official" by an owner.

### 3.6 Validation and rules on inputs

- All files go through validation of MIME type, size, and integrity (non-corrupt PDF/Office) before ingestion.
- The mandatory minimum metadata (at least type and, where possible, owner) is required; the rest can be progressively enriched.
- **No input is ever mutated in place**: every revision (correction of an edge, new version of a document) creates a new immutable version, preserving the history for audit and for the weight calculation.
- Declarative inputs (self-assessments of authoritativeness, labels) are always revisable and cross-checked with objective signals (actual use, feedback).
- The source's permissions are binding: in case of doubt, the most restrictive rule prevails (fail-safe on confidentiality).

## 4. Activity flow (step-by-step)

The flow describes the end-to-end experience, from connecting sources to daily use and maintenance. It is designed for the MVP but indicates the points of evolution.

### Phase A — Setup and connecting sources

1. **Local-first installation and configuration.** The IT admin (Elena) installs LocalMind on-prem or self-hosted. She chooses the interface language (IT/EN) and the AI provider (default local Ollama), configures the existing MySQL + Qdrant.
2. **Definition of scope and base ontology.** The knowledge manager (Sara) selects the "Enterprise knowledge base" domain, which preloads the node and relationship types of §5. She can adapt the taxonomy and load the company glossary.
3. **Connecting sources.** The connectors (§6) are configured: local folders, wiki export/API, drive, chat, ticket, repo, mail (existing \`email\` module), calendar (\`calendar\` module). For each source, the sync frequency and permission mapping are set.
4. **Census of people and systems (minimal MVP).** The people directory and the inventory of systems/APIs/repos are imported or entered manually, the basis for the relational nodes.

### Phase B — Ingestion and graph construction

5. **Content extraction.** The existing pipeline (\`DocumentIngestionPipelineService\`) extracts text via Tika, applies OCR (Tesseract) to scans, and transcribes audio/video via Whisper. Each piece of content keeps provenance, position, version, and permissions.
6. **Chunking and embedding.** The content is segmented (\`ChunkingService\`) and vectorized on **Qdrant**; the relational metadata is persisted on **MySQL**.
7. **Deduplication and reconciliation.** Hash + embedding similarity identify identical and near-duplicate copies: a single unit of knowledge is created with the variants linked, avoiding fragmentation.
8. **Extraction of entities and relationships (AI).** The local AI analyzes the chunks and extracts candidate entities (procedures, systems, people, decisions…) and candidate relationships ("depends on", "documents", "supersedes"), bootstrapping the graph from unstructured content.
9. **Calculation of initial weights.** The edges receive an initial weight from the factors of §5 (source authoritativeness, co-occurrence, explicit links, freshness). The nodes receive freshness and authoritativeness scores.
10. **Human validation (curation).** The knowledge manager and the owners review the suggested links in a review queue: they confirm, correct, or reject. Each decision feeds back into the weight.

### Phase C — Daily use (querying)

11. **Natural-language question.** An employee (Marta, Luca, support) asks the assistant a question — "What is the procedure for production release and which services does it touch?".
12. **GraphRAG retrieval.** The system performs semantic search on Qdrant for the relevant fragments, then **enriches** the results with the graph relationships (dependencies, valid versions, owner, known contradictions). Multi-hop questions become graph traversals.
13. **Permission filter.** Before composing the answer, the system filters nodes and chunks based on the user's permissions (inherited ACLs): the user sees only what they are entitled to.
14. **Well-founded and cited answer.** The AI (local Ollama) generates the answer citing the exact sources (document, section, version, owner) and flagging whether a source is potentially obsolete or in conflict with another.
15. **Graph exploration.** The user can open the interactive visualization: starts from the "release procedure" node, expands the neighbors (dependent services, related decisions, experts), filters by node/relationship type.
16. **Follow-up actions.** From the answer, the user can jump to the document, contact the owner, open a ticket, or report an inaccuracy.

### Phase D — Feedback and continuous maintenance

17. **Answer evaluation.** The user votes on the answer and, if wrong, indicates the correct source: a signal for ranking and for curation.
18. **Detection of obsolescence and gaps.** The system monitors freshness (review date exceeded), actual use (nodes never consulted vs heavily requested), contradictions, and processes without a procedure, generating alerts for the knowledge manager.
19. **Periodic curation.** Sara works on a prioritized queue: updates stale content, reconciles contradictions, fills gaps, refines the ontology. Owners receive review requests on their own nodes.
20. **Incremental synchronization.** The connectors detect changes at the source (delta) and update nodes/chunks/versions in minutes, without reloading everything; content deleted at the source is marked according to the retention policy.

### Synthetic diagram of the flow

\`\`\`text
Sources (wiki/drive/chat/ticket/repo/mail) ─► Connectors (sync)
        │
        ▼
Extraction (Tika/OCR/Whisper) ─► Chunking ─► Embedding (Qdrant) + Metadata (MySQL)
        │
        ▼
Deduplication/Reconciliation ─► Entity & relationship extraction (AI) ─► Initial weights
        │
        ▼
Human curation (confirm/correct edges) ◄──── feedback
        │
        ▼
KNOWLEDGE GRAPH (nodes + weighted edges)
        │
        ├─► GraphRAG assistant (question ► semantics + traversal ► permission filter ► cited answer)
        ├─► Interactive visualization (exploration by relationships)
        └─► Governance (freshness, contradictions, gaps, owner) ─► alert ─► curation
\`\`\`

## 5. Graph model (node types, relationship types, weighting criteria)

The model reuses the engine's generic schema (typed nodes + weighted edges on MySQL for structure, Qdrant for semantics) and specializes it with the types of this domain. All types are enums **translated IT/EN** to the frontend, consistently with the project constraints.

### 5.1 Node types

| Node type | Description | Specific key attributes |
|---|---|---|
| **Document** | Generic documentary unit (PDF, DOCX, wiki page) | title, source, version, owner, confidentiality, freshness |
| **Procedure / SOP** | Sequence of operational steps | ordered steps, prerequisites, covered system/process |
| **Process** | High-level business flow | input/output, owner, linked procedures |
| **FAQ** | Question-answer pair | question, answer, usage frequency |
| **Decision (ADR)** | Decision and its rationale | context, decision, consequences, date |
| **Person** | Member of the organization | role, team, competencies, contacts |
| **Team / Department** | Organizational unit | manager, scope |
| **Repository** | Code repo | url, language, owner |
| **Service / Microservice** | Software component | endpoint, owner, criticality |
| **API** | Exposed/consumed interface | spec (OpenAPI), version, consumers |
| **Database / Datastore** | Data store | technology, owner, data contained |
| **System / Infrastructure** | Infrastructural component | environment, owner |
| **Ticket / Issue** | Request or problem | status, resolution, linked system |
| **Customer / Supplier** | External business entity | sector, internal contact |
| **Competency / Skill** | Area of expertise | level, people who possess it |
| **Concept / Term** | Glossary / ontology entry | definition, synonyms, acronyms |
| **Meeting / Event** | Meeting with relevant outcomes | date, participants, decisions produced |
| **Conversation** | Relevant chat/mail thread | participants, summary, low authoritativeness |
| **Fragment (Chunk)** | Vectorized segment | embedding (Qdrant), position, parent document |

### 5.2 Relationship types (edges, directed and weighted)

| Relationship | From → To | Meaning |
|---|---|---|
| **documents** | Document/FAQ → Process/Procedure/System | the content describes the entity |
| **is_prerequisite_of** | Procedure/Concept → Procedure | must be known/executed before |
| **is_part_of** | Procedure → Process; Section → Document | hierarchical composition |
| **supersedes / is_superseded_by** | Document(vN) → Document(vN-1) | versioning and supersession |
| **contradicts** | Document → Document | content conflict to resolve |
| **depends_on** | Service/API → Service/API/Database | technical dependency |
| **exposes / consumes** | Service → API | producer/consumer relationship |
| **is_expert_in** | Person → Competency/System/Process | knowledge ownership |
| **is_owner_of** | Person/Team → Document/Service/Process | formal responsibility |
| **derives_from** | Procedure/System → Decision | consequence of a choice |
| **mentions / references** | Conversation/Ticket → entity | informal citation |
| **resolves** | Ticket/Procedure → Problem/Ticket | resolution |
| **related_to** | any ↔ any | generic semantic relationship (weight from similarity) |
| **belongs_to** | Person → Team; Service → Domain | organizational aggregation |
| **defines** | Concept → Document/Procedure | hook to the ontology/glossary |

### 5.3 Criteria for edge weighting

The weight is a normalized value (0–1) calculated as a **configurable combination** of factors; each factor is traceable and recalculable in an immutable way (a new calculation produces a new version of the weight, does not overwrite). Main factors:

| Factor | What it measures | Effect on the weight |
|---|---|---|
| **Source authoritativeness** | Official (approved wiki) vs informal (chat) | official sources weigh more |
| **Link explicitness** | Explicit link vs inferred by the AI | confirmed links weigh more than suggested ones |
| **Human validation** | Confirmation/rejection by an owner/curator | confirmation increases, rejection zeroes/penalizes |
| **Co-occurrence & semantic similarity** | How much two nodes appear together or are close in the embeddings | greater similarity → greater weight (for \`related_to\`) |
| **Actual usage frequency** | How many times the edge is traversed in useful answers | usage reinforces the weight (learning from usage) |
| **Freshness / temporal decay** | Age and review date of the node/relationship | stale content decays in weight |
| **Feedback on answers** | Positive/negative vote when the edge contributes to an answer | votes calibrate the weight |
| **Criticality / centrality** | Importance of the node (e.g., critical service, core process) | increases the weight of the relationships that involve it |

Calculation rules:
- The weights are **recalculable** in batch (scheduled job) and in real time upon arrival of new feedback.
- The **temporal decay** prevents old links from staying dominant forever; recent usage frequency counteracts the decay.
- Each component of the weight remains inspectable, so the AI can explain *why* it followed a certain path ("relationship confirmed by the owner, official source, used 14 times").

## 6. Data sources & connectors (ingestion)

The strategy is **not to impose migrations**: the sources stay where they are, the connectors bring them into the graph and keep them synchronized, preserving permissions and provenance. The existing pipeline is reused (Tika/OCR/Whisper, chunking, Qdrant) and connectors are added as **PF4J plugins** or domain adapters.

| Source | Access method | Suggested status | Privacy/permission notes |
|---|---|---|---|
| **Local folders / file server** | Existing batch folder watcher | MVP | Already present; respect file system permissions |
| **Confluence / Notion / wiki** | API or periodic export | MVP→Evolution | Inherit space/page ACLs; incremental sync (delta) |
| **SharePoint / Google Drive** | API (OAuth) or folder sync | Evolution | Permission inheritance mandatory |
| **Git repository** | Clone/pull + parser (README, OpenAPI, docstring) | MVP→Evolution | Extracts Services/APIs/Repos/ADRs |
| **Slack / Teams** | Export or API | Evolution | Tacit knowledge; low authoritativeness; explicit consent |
| **Jira / ServiceNow / issue tracker** | API | Evolution | Ticket nodes linked to systems/procedures |
| **Email** | Existing \`email\` module (IMAP, Angus Mail) | MVP (reuse) | Maximum privacy caution; opt-in per mailbox |
| **Calendar** | Existing \`calendar\` module | MVP (reuse) | Meetings/events as nodes |
| **Intranet / web pages** | HTTP fetch + snapshot | Evolution | Local snapshot for reproducibility |
| **Database / proprietary systems** | Custom connectors via PF4J plugin | Evolution | Marketplace extension |

Connector requirements:
- **Synchronization** one-shot, periodic, and manual; **incremental** (delta detection) to update in minutes.
- **Preservation of the source's permissions** (ACL) as binding metadata.
- **Complete provenance** (source, position, version, author) on every ingested unit.
- **Idempotency and deduplication**: re-syncing does not create duplicates.
- **Handling of deletions** at the source according to the retention policy.
- **Observability**: ingestion logs and metrics (existing Actuator/Prometheus), per-connector status in the UI.

## 7. Features to create, develop, and maintain (MVP → evolution)

The table distinguishes **CREATE** (new), **DEVELOP/EXTEND** (on an existing base), and **MAINTAIN** (reuse with care). The domain relies on the core graph engine (under construction) and on the existing domains.

### 7.1 MVP (first useful release)

| Feature | Action | LocalMind components involved |
|---|---|---|
| "Enterprise knowledge base" domain with node/relationship types of §5 | CREATE | new domain package + IT/EN enums; extends the \`knowledge\` domain |
| Graph schema on MySQL (nodes, edges, weights, versions) | CREATE | Flyway migrations (one query per file), JPA entities with UUID \`@JdbcTypeCode(CHAR)\` |
| Enterprise document ingestion reusing the pipeline | DEVELOP | \`DocumentIngestionPipelineService\`, Tika, Tesseract, Whisper, \`ChunkingService\` |
| Embedding and semantic search | MAINTAIN | \`QdrantVectorStoreAdapter\`, EmbeddingModel Ollama \`@Primary\` |
| AI extraction of candidate entities/relationships | CREATE | new domain service + LLM via \`LlmGatewayService\` (Ollama default) |
| Content deduplication/reconciliation | CREATE | domain service (hash + embedding similarity) |
| CRUD API for nodes/edges and graph queries (neighbors, paths, subgraphs) | CREATE | \`/api/v1/knowledge/graph\` controller, port/in, port/out |
| GraphRAG assistant with source citation | CREATE/DEVELOP | new GraphRAG orchestrator combining Qdrant + graph traversal; reuses chat/SSE |
| Permission filter on answers (inherited ACLs) | CREATE | authorization service on nodes/chunks + existing \`LocalAuthFilter\` |
| Edge weight calculation (base factors + human validation) | CREATE | weights domain service |
| Curation queue (confirm/correct links) | CREATE | Angular UI + API; feedback toward the weights |
| Interactive graph visualization (base) | CREATE | new standalone Angular feature (lazy), Signal store |
| MVP connectors: local folders, email, calendar, Git (base) | DEVELOP/MAINTAIN | folder watcher, \`email\`/\`calendar\` modules, new repo parser |
| IT/EN i18n of content and enums | MAINTAIN | \`TranslatePipe\`, translated enums |

### 7.2 Evolutions (subsequent releases)

| Feature | Action | Notes |
|---|---|---|
| Wiki/drive/chat/ticket connectors (Confluence, Notion, SharePoint, Drive, Slack, Teams, Jira) | CREATE (PF4J plugin) | incremental sync + permission inheritance |
| Detection of obsolescence, contradictions, and gaps | CREATE | scheduled jobs + alerts; knowledge manager dashboard |
| Proactive suggestion of missing links | DEVELOP | AI proposes high-probability edges for curation |
| Community detection / thematic summaries (global questions) | CREATE | graph clustering + per-community summary |
| IT dependency graph with impact analysis ("what breaks if…") | DEVELOP | advanced OpenAPI/repo parser, dependency traversals |
| "Find the expert" and competency map | CREATE | Person/Competency nodes + ranking |
| SSO/LDAP integration for identity and permissions | DEVELOP | beyond the MVP local-first auth |
| Publishing the assistant in channels (Slack/Teams/web widget) | DEVELOP | reuses the \`messaging\`/\`agent\` domain |
| Advanced versioning and audit/compliance | DEVELOP | immutable history, traceability reports |
| Installable domain packages (vertical by sector) | DEVELOP | marketplace + PF4J plugins |
| Advanced graph visualization (filters, time-travel, freshness heatmap) | DEVELOP | evolved Angular feature |

### 7.3 To maintain with care (known risks)

- **MySQL UUID mapping** (\`@JdbcTypeCode(SqlTypes.CHAR)\`) on all new graph entities.
- **Boundaries between domains**: avoid direct cross-domain imports; use dedicated port/out (see \`MODULE_BOUNDARIES.md\`).
- **Flyway one query per file**: the graph schema requires many small, atomic migrations.
- **Wiring in \`DomainConfig\`**: the new domain services stay pure, registered as \`@Bean\`.

## 8. AI / GraphRAG use cases

1. **Well-founded procedural question.** "How do you do a production release?" → semantic search + retrieval of the **valid procedure** (not the obsolete version), with citation of document, section, version, and owner.
2. **Multi-hop dependency question.** "If I update the payments API, which services and procedures are impacted?" → traversal of \`depends_on\`/\`consumes\`/\`documents\`, answer with a list of the touched nodes and their owners.
3. **Conversational onboarding.** A new hire asks "how does the deploy process work and who oversees it?" → the AI reconstructs process, procedures, systems, and experts from the graph.
4. **Find the expert.** "Who knows about OAuth authentication in our stack?" → traversal of \`is_expert_in\` with ranking by authoritativeness and recent activity.
5. **Contradiction detection.** The AI flags two documents that describe the same process in a divergent way and proposes which is the authoritative/recent source.
6. **Global thematic summary (community).** "What are the recurring themes in the incident tickets of the last quarter?" → community detection + per-community summary without retrieving thousands of tickets.
7. **Suggestion of missing links.** The AI proposes "this decision (ADR-42) seems to explain *why* this procedure exists: should they be linked?" to be validated in curation.
8. **Answer with traceability and freshness.** Every answer indicates the sources and warns if a source is past the review date or in conflict, remaining local-first (Ollama) unless cloud consent is given.
9. **Path explanation.** The AI can make explicit the path followed in the graph and the weights of the edges ("I followed the relationship confirmed by the owner, official source, recently used").

## 9. KPIs & success metrics

| Category | KPI | Objective / direction |
|---|---|---|
| Adoption | Weekly active users, no. of questions/day | growth |
| AI effectiveness | % of answers rated useful (thumbs up), correct citation rate | high and increasing |
| Fragmentation reduction | % of duplicates reconciled, no. of sources unified in the graph | growth |
| Knowledge quality | % of nodes with owner, % of nodes within review date, no. of open contradictions | high freshness, contradictions decreasing |
| Coverage | % of processes with a linked procedure, gaps identified vs filled | increasing coverage |
| Productivity | Average time to find an answer, reduction of repeated questions to seniors | decreasing |
| Onboarding | Time to productivity of new hires | decreasing |
| Graph | No. of nodes/edges, density, % of human-validated edges | controlled growth |
| Operations | GraphRAG response latency, connector sync freshness | within thresholds |
| Privacy | % of queries served locally (Ollama) without cloud transmission | maximize |

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| **Inaccurate AI extraction** (wrong entities/relationships) | Noisy graph, wrong answers | Mandatory human curation on suggested edges; weights that reward validation; confidence thresholds |
| **Loss of permission control** (exposure of confidential data) | Severe (privacy/compliance) | Binding inherited ACLs, pre-answer permission filter, restrictive fail-safe, local-first default |
| **Stale documentation** retaining authoritativeness | Obsolete answers | Temporal decay of the weight, freshness alerts, owners and review dates |
| **Graph performance on MySQL** for multi-hop queries | Latency | Targeted indexes, traversal queries limited in depth, cache (existing Caffeine), evaluate optimizations; Neo4j out of scope but reassessable |
| **Curation overload** | Unsustainable maintenance | Prioritized queues, batch suggestions, automations, focus on the most used nodes |
| **Fragile connectors / changing APIs** | Broken sync | Connectors as isolated plugins, retry (Spring Retry), observability, incremental sync |
| **Resistance to change** (yet another tool) | Low adoption | Does not replace the sources; immediate value via the assistant; onboarding as a flagship use case |
| **LLM hallucinations** | Distrust | GraphRAG with mandatory citations, refusal if no source, showing path/weights |
| **Boundary violation between domains** | Technical debt | Dedicated port/out, compliance with \`MODULE_BOUNDARIES.md\` |
| **Embedding/storage scalability** | Costs/resources | Deduplication, configurable retention, ingestion batches |

## 11. Maintenance & evolution

- **Continuous curation of the graph.** Scheduled jobs recalculate weights, freshness, and identify contradictions/gaps; the knowledge manager works on prioritized queues. Curation is part of the product, not an occasional activity.
- **Incremental synchronization.** The connectors apply deltas and handle deletions according to the retention policy; ingestion metrics exposed via Actuator/Prometheus.
- **Immutable versioning.** Every revision of a node/edge/weight creates a new version; the history is preserved for audit and to explain the answers.
- **Ontology evolution.** Node/relationship types extensible by sector; domain packages installable via marketplace and PF4J plugins.
- **Observability and quality.** Tests (JUnit backend, Vitest/Playwright frontend), coverage, monitoring of the % of useful answers and of freshness.
- **Bilingual documentation.** Constant update of the IT/EN documentation and tracking of developments in the \`Sviluppi/\` folder with dated nomenclature, as per CLAUDE.md.
- **Connector roadmap.** Progressive expansion (wiki → drive → chat → ticket → proprietary systems) driven by value and user demand.

## 12. Integration with existing LocalMind modules

| Module / component | Role in the Enterprise Knowledge Base domain |
|---|---|
| **\`knowledge\` (domain)** | Base to extend toward the enterprise graph: node/relationship types, graph queries |
| **\`document\` + ingestion pipeline** | Extraction (Tika), OCR (Tesseract), chunking — direct reuse |
| **\`llm\` + \`LlmGatewayService\`** | Entity/relationship extraction and answer generation; local Ollama by default, optional cloud fallback |
| **Qdrant (\`vectorstore\`)** | Semantic search and similarity for deduplication and GraphRAG |
| **MySQL + Flyway** | Persistence of nodes, edges, weights, versions (atomic migrations) |
| **\`email\` (Angus Mail)** | Mail ingestion connector (tacit knowledge) — reuse |
| **\`calendar\`** | Meetings/events as nodes; hook to decisions and people |
| **\`mcp\`** | Exposure of the graph/tools as MCP tools for external agents |
| **\`agent\` + \`messaging\`** | Publishing the assistant in channels (Slack/Teams/web widget) |
| **\`auth\` + \`LocalAuthFilter\`** | Identity and basis for the permission filter on answers |
| **\`marketplace\` + PF4J plugins** | Installable connectors and domain packages (extensibility) |
| **\`automation\`** | Scheduled sync, curation jobs, and freshness/contradiction alerts |
| **\`common\` (events, analytics)** | Domain events for side effects (weight recalculation, indexing) and metrics |
| **Angular frontend (standalone feature)** | New \`knowledge\`/graph feature: interactive visualization, curation, assistant; IT/EN i18n, Signal store |
| **Existing Chat/SSE** | Reuse of the streaming response channel for the GraphRAG assistant |

The domain **introduces no new infrastructure** (no Neo4j in this cycle): it reuses MySQL + Qdrant, respects the hexagonal architecture (pure domain services wired in \`DomainConfig\`), stays local-first with Ollama AI by default, preserves the privacy of enterprise data, and is entirely bilingual IT/EN.
`;
