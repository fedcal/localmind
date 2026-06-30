# Ticketing & Decisions (ADR)

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

## 1. What We Solve (Problem & Value)

### 1.1 The Problem of Decision Memory That Dissolves

In every organization that develops software or manages complex processes, the same silent phenomenon occurs: **decisions are made, executed, and then forgotten**, while the *why* behind those decisions — the rationale, the context, the discarded alternatives, the constraints of the moment — evaporates. Six months later, no one remembers why PostgreSQL was chosen instead of MongoDB, why that microservice was split in two, why that bug was "resolved" by disabling a feature rather than fixing it. The knowledge exists, but it is **fragmented, disconnected, and not queryable**:

- **Tickets live in silos.** Issues sit in Jira, GitHub Issues, GitLab, Azure DevOps, or Redmine. Each ticket tells a piece of the story (a bug, a request, a task), but the links between tickets — "this bug is caused by that change", "this feature depends on that epic" — are weak, manual, or nonexistent. The ticketing system knows statuses and assignees, but it cannot *reason* about the network of causes and effects.
- **Architecture decisions (ADR) are dead documents.** When they exist, Architecture Decision Records are Markdown files scattered in a `docs/adr/` folder or Confluence pages. They capture the single decision but are not linked to the tickets that motivated them, the services they impact, the code derived from them, or the subsequent decisions that superseded them. A "Superseded" ADR almost never explicitly points, in a navigable way, to the ADR that replaces it.
- **The rationale is not tracked.** The value of a decision is not the choice itself, but the **reasoning**: the context, the forces at play, the trade-offs, the alternatives evaluated and why they were rejected. Today, this rationale survives in the head of whoever was present at the meeting, in lost chat threads, in laconic commit messages. When that person leaves the company, the rationale leaves with them.
- **The cause-effect chain is invisible.** "Why did Friday's deploy break checkout?" The answer requires linking: the incident → the bug ticket → the PR → the commit → the architecture decision that introduced that component → the epic that required that feature. Today this chain has to be reconstructed by hand, digging through five different tools, and often it is not reconstructed at all.
- **Decisions are repeated and contradicted.** Without a navigable decision memory, different teams (or the same team at different times) make inconsistent decisions or re-discuss questions already resolved. The wheel is reinvented, or an architectural inconsistency is introduced because no one knew a contrary decision already existed.
- **Onboarding is painful.** A new hire, or a team that inherits a system, has no way to understand "how we got here". They have to do archaeology on closed tickets, read thousands of lines of misaligned documentation, and interview the veterans. The cost is weeks of lost productivity and decisions made without historical context.
- **Audit and compliance are fragile.** In regulated contexts (finance, healthcare, public administration) it is necessary to demonstrate *why* a decision was made, who approved it, on what basis. Without structured tracking of the rationale, the audit becomes an exercise in after-the-fact reconstruction, costly and unreliable.

The common thread is one: **there is a huge body of causal and decisional knowledge that remains inert** because it is not modeled as a network of nodes and weighted relationships, and because no tool allows the AI to navigate it to answer questions of the type "why", "what happens if", "what is connected to".

### 1.2 Our Response: The Causal Graph of Decisions and Tickets

LocalMind, in its evolution into a **universal knowledge graph engine**, addresses exactly this problem for the enterprise group. The value proposition of the "Ticketing & decisions (ADR)" scope is to build a **causal and decisional graph** of the organization: a weighted network in which the nodes are tickets, issues, epics, bugs, incidents, architecture decisions (ADR), alternatives, requirements, people, and technical artifacts (services, repositories, commits, PRs, components), and the edges represent semantically rich relationships — "causes", "blocks", "depends-on", "motivates", "implements", "supersedes", "discards-alternative", "impacts". Three capabilities are grafted onto this graph:

1. **Structured capture of the rationale.** Each decision (ADR) is no longer an isolated file but a node in the graph, linked to the context that generated it (the tickets, the incidents, the requirements), to the alternatives considered (nodes in their own right, with the reasons for exclusion), and to the consequences (the services created, the subsequent decisions). The *why* becomes a first-class citizen, navigable and versioned.
2. **Navigable cause-effect tracking.** Tickets are ingested from existing systems (Jira, GitHub, GitLab…) and linked to each other and to technical artifacts. The chain incident → bug → commit → decision becomes an explicit path on the graph, traversable in both directions (from the effect to the cause and vice versa).
3. **AI that reasons over the decision network (GraphRAG).** The AI (Ollama locally by default) answers complex questions — "why did we choose X?", "what do we risk if we remove Y?", "which decisions depend on this now-superseded ADR?" — by navigating the graph and retrieving the semantically relevant fragments from Qdrant, **always citing the nodes and paths used** (ticket, ADR, commit). No company data leaves the infrastructure without explicit consent.

In summary: we transform decisional knowledge from **passive documentation and silos** to a **queryable causal graph**, where the rationale is preserved, the cause-effect chains are explicit, and the AI acts as the living institutional memory of the organization.

### 1.3 Why LocalMind Is the Right Platform

| Enterprise need | LocalMind feature that satisfies it |
|---|---|
| Absolute confidentiality of decisions, incidents, project data | **Local-first / self-hosted**: everything runs on-premise; no sending to the cloud without explicit consent |
| No lock-in, predictable costs | **Pure open source**, local Ollama AI by default, no paywall |
| Tickets and ADR in heterogeneous formats and tools | Existing ingestion pipeline (**Tika**, parsers) + plugin connectors for Jira/GitHub/GitLab |
| Answers grounded in *your own* tickets and decisions | **GraphRAG** over the causal graph + Qdrant semantic search, with source citation |
| Privacy by design on sensitive data (audit, compliance) | Data never exfiltrated; immutable and versioned tracking of decisions |
| Documentation and interface in multiple languages | **Bilingual IT/EN** platform by design, translated enums |
| Extensibility toward new corporate tools | **PF4J plugin** system + marketplace |
| Integration with the rest of internal knowledge | Same graph engine that serves documents, repos, microservices, people, mail |

The competitive differentiator is clear. Tools like Jira track the *status* of tickets but not the rationale nor the deep causal chains; "dependency graph" plugins for Jira visualize the links but do not make them queryable by an AI; ADR tools (adr-tools, Log4brains) produce documents but not a navigable graph linked to tickets and code; root-cause analysis solutions (e.g., on Grafana) cover runtime observability but not the *decision memory*. Research from 2025-2026 confirms the direction: frameworks such as **ADR-E** (Explainable ADR) introduce structured rationale, discarded alternatives, and traceability links inspired by explainability principles, and GraphRAG approaches over Jira tickets model explicit relationships such as `caused_by`, `clone_of`, `related_to`. LocalMind unites these worlds — **explicit causal graph + local GraphRAG + total privacy at zero cost** — in a single self-hostable platform, something no competitor offers together.

## 2. Personas & Target Users

| Persona | Profile | Primary needs | How they use LocalMind |
|---|---|---|---|
| **Elena, Software Architect / Tech Lead** | Responsible for the architectural choices of one or more teams | Document decisions with rationale, avoid inconsistencies, show the evolution | Creates and links ADR on the graph, navigates the dependencies between decisions and services, uses the AI to verify consistency with past decisions |
| **Marco, Engineering Manager** | Coordinates multiple teams and backlogs | Understand why we arrived at a situation, status of dependencies between epics | Queries the graph on cause-effect chains, blocked priorities, pending decisions |
| **Sara, Senior Developer** | Implements features and fixes bugs | Understand the historical context of a component before modifying it | Starts from a service/repo and traces back to the decisions and tickets that shaped it; links PRs and commits to decisions |
| **Luca, SRE / On-call** | Manages production incidents | Rapid root-cause: what caused the incident, which decisions are involved | During the incident follows the `causes`/`impacts` edges from the incident to the responsible commit/decision |
| **Giulia, Product Owner** | Manages requirements and product backlog | Track requirements → decisions → implementation, justify choices to stakeholders | Links requirements to ADR and tickets, shows the rationale at product reviews |
| **Davide, Auditor / Compliance Officer** | Verifies conformity and traceability | Demonstrate who decided what, when, and why | Explores the versioned decision log, exports the chain of approvals and rationales |
| **Anna, New hire / inheriting team** | Must understand an existing system | Rapid onboarding on "how we got here" | Asks the AI about the decisional history of an area, navigates the graph to orient herself |
| **Distributed team / open source maintainer** | Collaborators in different time zones and moments | Shared and asynchronous memory of decisions | Consult and enrich the decisional graph as a common source of truth |

The primary user for the MVP is **Elena (Architect/Tech Lead)** alongside **Sara (Developer)**: they are the producers and the first consumers of the rationale, with the greatest need to link decisions, tickets, and code. The other personas drive the evolutions (incident/SRE, compliance, onboarding).

## 3. Input Requirements

This section defines in detail **what must be able to enter the system** for the Ticketing & decisions scope to work. The inputs are divided into: tickets and issues, architecture decisions, linkable technical artifacts, relationship metadata, people/organization, configuration, and feedback. Each input must be validated at the system boundary ("never trust external data" principle) and treated immutably (each revision creates a new version, it does not mutate the existing one).

### 3.1 Tickets, Issues, and Work Items (Content to Ingest)

| Item type | Essential fields | Typical source | Notes |
|---|---|---|---|
| **Bug** | title, description, steps to reproduce, severity, status, assignee, component | Jira, GitHub/GitLab Issues, Redmine | Heart of causal tracking (what broke and why) |
| **Feature / Story** | title, description, acceptance criteria, status, parent epic | Jira, Azure DevOps | Links requirements and implementation |
| **Epic / Initiative** | title, objective, scope, status, linked stories | Jira, GitHub Projects | High-level aggregating node |
| **Task / Sub-task** | title, description, status, parent | All | Operational granularity |
| **Incident / Postmortem** | description, timeline, severity, impacted services, corrective actions | PagerDuty, Opsgenie, internal docs | Typical starting point of root-cause |
| **Change Request** | description, motivation, risk, approvals | ITSM / ServiceNow | Relevant for compliance |
| **Comment / Discussion** | author, text, timestamp, reference ticket | All | Often contains the "hidden" rationale |

Cross-cutting requirements on tickets:
- **Provenance always tracked**: each node must trace back to the source system, the external ID, and the URL (for the deep-link and incremental updating).
- **Normalized statuses and types**: the heterogeneous statuses of the various tools (e.g., "In Progress", "Doing", "WIP") must be mapped onto a canonical set of statuses, with an enum **translated IT/EN** toward the frontend.
- **Status history**: status transitions (created → in progress → resolved → closed) are valuable input for the weights and for the causal timelines; they must be preserved as dated events.
- **Deduplication**: recognize the same ticket imported multiple times (by external ID + system) to avoid duplicate nodes.
- **Language** automatically detected (IT/EN and beyond) for embedding and AI answers.

### 3.2 Architecture Decisions (ADR) and Rationale

The rationale is the heart of the scope. A decision, to be a useful node, must be able to contain (based on the MADR / ADR-E model):

- **Title and ID** of the decision (e.g., "ADR-0042: Adopt event sourcing for the orders module").
- **Status**: Proposed, Accepted, Rejected, Deprecated, Superseded — **bilingual IT/EN** enum.
- **Context / Problem**: the situation and the forces at play that made the decision necessary.
- **Decision**: what was chosen to do.
- **Rationale / Consequences**: why, with the positive and negative trade-offs.
- **Alternatives considered**: each as a distinct element, with the reason for exclusion (key input for "why not X?" questions).
- **Stakeholders and approvers**: who proposed, who decided, who approved (for audit).
- **References**: tickets, incidents, requirements, documents, commits/PRs that motivate or derive from the decision.
- **Date and version**: each modification of the content produces a new version (immutability, audit trail).

ADR input modes:
1. **UI-guided creation** (structured form with the fields above, in MADR style) — native MVP mode.
2. **Import from existing Markdown files** (`docs/adr/` folders, Nygard/MADR format) with field parsing.
3. **AI-assisted proposal**: starting from a ticket/incident, the AI suggests a draft ADR to review (evolution).

### 3.3 Linkable Technical Artifacts (Enterprise Context)

The causal value emerges by linking tickets and decisions to the technical world, reusing the other enterprise scopes of the graph:

- **Git repositories, commits, Pull/Merge Requests** (bidirectional ticket↔code link via IDs in commit/PR messages).
- **Microservices, APIs, components, modules** (what a decision creates, modifies, or deprecates).
- **Documents** (specifications, RFCs, design docs already ingested by the `document` domain).
- **Databases, infrastructure, environments** (what a change impacts).

These artifacts may already exist as nodes in the graph (coming from other enterprise scopes) or be created on-demand as referenced "lightweight" nodes.

### 3.4 Relationship Metadata (the Links to Capture)

Beyond the content, the most valuable input is the **explicit relationships** already present in the source systems, to be imported and then enriched:
- Native Jira/GitHub links: `blocks`, `is blocked by`, `relates to`, `duplicates`, `caused by`, `clones`, parent/child (epic→story→task).
- Cross-references in the texts (e.g., "see #123", "fixes JIRA-456") to be extracted via parsing.
- Commit↔ticket links (smart commits) and PR↔issue.
- Decision↔decision links (supersedes / superseded by).

### 3.5 People and Organization

- **People**: authors, assignees, reviewers, approvers, reporters — as `Person` nodes (reusable by other enterprise scopes, e.g., mail/HR).
- **Teams / organizational units**: ownership of services and decisions.
- **Roles**: who can approve an ADR, who can modify the graph (governance).

### 3.6 System Configuration

- **LLM provider and model** (default local Ollama; cloud optional with explicit consent), embedding model, interface language (IT/EN).
- **Active connectors and credentials** (Jira/GitHub/GitLab…): URL, token, projects/repos to synchronize, sync frequency.
- **Normalization mapping**: statuses, types, and links of external systems → LocalMind canonical enums.
- **Privacy/visibility policies**: which projects are private, who sees what, what is excluded from ingestion.
- **Weights and governance**: who can approve ADR, confidence thresholds for AI-suggested links, decay rules.

### 3.7 Feedback (Continuous Loop)

- **Confirmation/rejection of AI-suggested links** (e.g., "is this bug really caused by that commit?") — feeds the weight of the edges (§5).
- **Corrections to the graph**: add/remove/relabel nodes and edges.
- **Evaluation of AI answers** (useful/out of context, correct citation).
- **Supersession marking**: declare an ADR as superseded by another, with automatic creation of the `supersedes` edge.

### 3.8 Validation and Input Rules

- Type, schema, and integrity validation on all inputs (DTO with Bean Validation on the API side; "fail fast" with clear messages).
- Minimum required fields: for a ticket at least title + source system + ID; for an ADR at least title + status + decision.
- **Immutability**: no input mutated in place; each revision (e.g., status change of an ADR, re-weighting of an edge) creates a new version with timestamp and author, preserving the entire history for audit.
- **Ingestion idempotency**: re-synchronizing the same source must not duplicate nodes/edges, but update the versions.
- **Privacy**: no content sent to cloud providers without explicit consent; connector tokens encrypted at rest.

## 4. Activity Flow (Step-by-Step)

The flow describes the end-to-end experience, from connecting the sources to maintaining the decision memory. It is designed for the MVP but also indicates the evolution points.

### Phase A — Setup and Source Connection

1. **Access and domain selection.** The user accesses LocalMind (existing local-first auth), selects/enables the "Ticketing & decisions" enterprise domain, and chooses the interface language (IT/EN) and the AI provider (default local Ollama).
2. **Connector configuration.** The administrator connects the sources (Jira, GitHub/GitLab, local ADR files) via the plugin connectors (PF4J): enters URL, token (encrypted at rest), and selects the projects/repositories to synchronize. Defines the sync frequency and the visibility policies.
3. **Normalization mapping.** The system proposes (and the user confirms) the mapping between the statuses/types/links of external systems and the canonical enums of LocalMind, with bilingual IT/EN labels.

### Phase B — Ingestion of Tickets and Decisions

4. **Initial synchronization.** A batch job imports tickets, issues, epics, incidents, and their native links from the connected systems, in addition to the Markdown ADR files present. Each element is validated (type, minimum fields, integrity) and deduplicated by external ID + system.
5. **Text and reference extraction.** For each item, title, description, comments are extracted and — via parsing — the cross-references in the texts ("fixes #123", "see JIRA-456", "supersedes ADR-0007"). Errors on one element do not block the others and are reported with clear messages.
6. **Segmentation and embedding.** The textual contents are split into chunks (ChunkingService), vectorized, and indexed on Qdrant with provenance metadata (system, ID, URL, timestamp). Tickets and decisions are persisted on MySQL as graph nodes.
7. **Ingestion confirmation.** The user sees the summary: how many tickets/ADR imported, how many native links acquired, any errors, and a first estimate of the inferred causal links.

### Phase C — Construction of the Causal and Decisional Graph

8. **Node creation.** Each ticket, ADR, alternative, incident, requirement, and person becomes a typed node in the graph, with its attributes and its provenance.
9. **Import of explicit links.** Native links (blocks, caused by, duplicates, parent/child, supersedes) become edges with high weight (they are explicitly declared, hence reliable).
10. **AI inference of implicit links.** An AI job analyzes the contents and proposes missing but probable edges: a bug whose text describes a symptom consistent with a recent change (`causes`), a ticket that motivates a decision (`motivates`), a decision that impacts a service (`impacts`), commit↔ticket via ID. Each edge is born with a **confidence weight**.
11. **Human review (human-in-the-loop).** The user sees the proposed links and confirms, corrects, or rejects them. Confirmations increase the weight, rejections reduce it or eliminate the edge. This step is crucial for reliability and trust, especially on causal links.

### Phase D — Structured Capture of a New Decision (ADR)

12. **Decision trigger.** During work the need for an architectural choice emerges (e.g., from a recurring incident or from a new feature). The user opens the ADR form directly from the context ticket/incident.
13. **Guided compilation.** Fills in context/problem, decision, rationale, and consequences. Adds the **alternatives considered** as distinct elements, each with the reason for exclusion. Indicates stakeholders and approvers.
14. **AI assistance (optional).** The AI can pre-fill a draft starting from the linked context (tickets, incidents, related decisions) and suggest typical alternatives and known risks, which the user reviews.
15. **Automatic linking to the context.** Upon saving the ADR, the system automatically creates the edges: `motivates` (from the tickets/incidents), `discards-alternative` (toward the alternative nodes), `impacts` (toward the indicated services), `decided-by` (toward the people). If the ADR replaces a previous one, the `supersedes` edge is created.
16. **Approval workflow.** The ADR goes through the states Proposed → Accepted/Rejected (with registered approvers). Each transition is a dated and immutable event (audit trail).

### Phase E — Querying and Reasoning (GraphRAG)

17. **Natural language question.** The user asks, for example, "Why did we abandon the monolith for the orders module?" or "What do we risk if we remove the caching service?".
18. **GraphRAG retrieval.** The system identifies the relevant nodes, navigates the graph to gather the relevant sub-graph (decision + alternatives + motivating tickets + impacted services + subsequent decisions), and retrieves the semantically closest chunks from Qdrant.
19. **Grounded and cited answer.** The AI (Ollama by default) generates the answer using the organization's material, **citing the nodes and paths used** (ADR-0042, JIRA-456, commit abc123) with deep-links to the source system. The user can open each source with a click.
20. **Visual exploration.** In parallel, the user can open the graph view: start from the incident and visually trace back the `causes`/`motivates`/`supersedes` chain, progressively expanding the neighbors and filtering by node/relationship type.

### Phase F — Root-Cause and Impact Analysis

21. **From effect to cause.** Faced with an incident, the user starts from the Incident node and follows the `causes`/`impacts` edges backward up to the responsible commit/decision, with the AI summarizing the chain and the critical points.
22. **Impact analysis (what-if).** Before a change, the user asks "what depends on X?": the system traverses forward the `depends-on`/`blocks`/`impacts` edges and shows the impact radius, highlighting the decisions and services involved.
23. **Consistency check.** When a new decision is proposed, the AI checks the graph to detect conflicts with existing decisions ("this contradicts ADR-0019") or duplications of already-resolved discussions.

### Phase G — Maintenance and Evolution of the Memory

24. **Incremental update.** Periodic sync jobs update ticket statuses, new tickets, and new links without rebuilding the graph; the modifications generate new versions of the nodes/edges (immutability).
25. **Graph hygiene.** Periodic routines flag orphan nodes, weak unconfirmed links, "Accepted" ADR long without a link to the implementation, alternatives never linked.
26. **Export and audit.** Upon request (compliance), the system exports the decisional chain of an area — decisions, rationales, approvers, timeline — in a readable format (Markdown/JSON), all locally.

## 5. Graph Model (Node Types, Relationship Types, Weight Criteria)

The model reuses the infrastructure of the **core knowledge graph engine** (typed nodes + weighted edges on MySQL for the structure, Qdrant for the semantics). Below are the types specific to the Ticketing & decisions scope.

### 5.1 Node Types

| Node type | Description | Key attributes |
|---|---|---|
| **Ticket / Issue** | Generic work item | external id, system, title, description, status, type, assignee, URL |
| **Bug** | Defect to fix | severity, steps to reproduce, component, status |
| **Feature / Story** | Requested functionality | acceptance criteria, epic, status |
| **Epic / Initiative** | High-level aggregate | objective, scope, status |
| **Task / Sub-task** | Operational unit | parent, status |
| **Incident / Postmortem** | Production event | severity, timeline, impacted services, corrective actions |
| **Decision (ADR)** | Architecture decision | id, title, status (Proposed/Accepted/…), context, decision, rationale, version |
| **Alternative** | Option considered and (usually) discarded | description, reason for exclusion, reference decision |
| **Requirement** | Functional/non-functional need | description, type, priority |
| **Change Request** | Controlled change request | motivation, risk, approvals |
| **Repository / Service / Component** | Technical artifact (reused from other scopes) | name, type, owner |
| **Commit / Pull Request** | Code artifact | hash/number, author, message, repo |
| **Person** | Author, assignee, approver | name, role, team |
| **Team / Unit** | Organizational group | name, scope |
| **Comment / Discussion** | Discussion text (often hidden rationale) | author, text, reference ticket |
| **Chunk / Fragment** | Text segment linked to Qdrant vectors | text, vector id, provenance |

### 5.2 Relationship Types (Edges)

| Relationship | From → To | Meaning | Directed |
|---|---|---|---|
| **causes** | Commit/Ticket/Decision → Bug/Incident | A is at the origin of B | Yes |
| **blocks / is_blocked_by** | Ticket → Ticket | A prevents the progress of B | Yes |
| **depends_on** | Ticket/Service → Ticket/Service | A requires B | Yes |
| **duplicates** | Ticket → Ticket | Same problem | No |
| **is_related_to** | Ticket → Ticket | Generic link | No |
| **parent_of / sub_task_of** | Epic → Story → Task | Decomposition hierarchy | Yes |
| **motivates** | Ticket/Incident/Requirement → Decision | Triggered the decision | Yes |
| **discards_alternative** | Decision → Alternative | Option evaluated and rejected | Yes |
| **supersedes / superseded_by** | Decision → Decision | A replaces B | Yes |
| **contradicts** | Decision → Decision | Conflicting theses/choices | No |
| **implements** | Commit/PR/Service → Decision/Story | Realizes the choice/feature | Yes |
| **impacts** | Decision/Change → Service/Component | Effect on an artifact | Yes |
| **resolves** | Commit/PR → Bug/Ticket | Closes the problem | Yes |
| **decided_by / approved_by** | Decision → Person | Authorship/approval | Yes |
| **assigned_to / reported_by** | Ticket → Person | Responsibility | Yes |
| **satisfies** | Decision/Feature → Requirement | Covers a requirement | Yes |
| **discussed_in** | Comment → Ticket/Decision | Rationale captured in discussion | Yes |

### 5.3 Criteria for Edge Weights

The weight (normalized value, e.g., 0–1) expresses the **strength/reliability** of the relationship and guides both the visualization (thicker edges) and the GraphRAG (exploration and citation priority). The weight is a configurable combination of the following factors, consistent with the core principle "weight derived from configurable factors":

| Factor | Effect on the weight | Example |
|---|---|---|
| **Explicit origin** | Very high base | Native Jira `caused by` link or `fixes #123` reference in the commit |
| **AI extraction confidence** | Initial base (medium-low) | The LLM hypothesizes that the bug is caused by that commit |
| **Human confirmation** | Increases strongly and stabilizes | A dev confirms the causal link → high weight, "verified" |
| **Human rejection** | Zeroes/removes | The proposed link is rejected |
| **Temporal proximity** | Increases (for `causes`) | The bug appeared right after that deploy/commit |
| **Co-occurrence / co-citation** | Increases | Two tickets often cited together, or in the same commit |
| **Semantic similarity (Qdrant)** | Increases | Descriptions of the bug and the change very close in the vector space |
| **Navigation frequency** | Increases | Causal path traversed often during analyses |
| **Authoritativeness/role** | Weighs `decided_by`/`approved_by` | Decision approved by a senior architect |
| **Decision status** | Modulates visibility | A `Superseded` ADR loses weight in current answers, but remains for history |
| **Recency / decay** | Reduces over time | Unconfirmed and unused links decay slowly |

Immutability rule: the weight is not mutated in place; each re-evaluation produces a new version of the value (with timestamp and contributing factors), so as to be able to explain *why* an edge has that weight — an interpretability requirement essential in audit and compliance contexts.

## 6. Data Sources & Connectors (Ingestion)

| Source | Mode | Status | Notes |
|---|---|---|---|
| **Markdown ADR files** (Nygard/MADR) | Parsing `docs/adr/` folders + folder watcher | MVP | Reuses the `document` pipeline (Tika) and `LocalFileSystemScanner` |
| **ADR creation from UI** | Structured form | MVP | Native mode, no import needed |
| **Jira** | Plugin connector (PF4J) + REST API | MVP/early | Issues, epics, native links, status transitions |
| **GitHub Issues / Projects** | Plugin connector + REST/GraphQL API | MVP/early | Issues, PRs, `fixes #` links, labels |
| **GitLab Issues / Merge Requests** | Plugin connector + API | Evolution | Analogous to GitHub |
| **Azure DevOps / Redmine** | Plugin connector | Evolution | Work items and relationships |
| **Git repositories (commit/PR)** | Connector + message parsing | Early | Commit↔ticket↔decision link |
| **Incident management (PagerDuty, Opsgenie)** | Plugin connector | Evolution | Incidents as nodes, root-cause |
| **ITSM (ServiceNow)** | Plugin connector | Evolution | Change requests, approvals |
| **Confluence / Wiki** | Connector + extraction | Evolution | ADR and rationales documented on a wiki |
| **Email** (decisions via thread) | Existing `email` module | Evolution | Extraction of decisions discussed via mail |

All external connectors pass through the **PF4J plugin + marketplace** system, so as not to bloat the core and to respect modularity by domain. Each connector must declare what data it reads, where it ends up, and with what credentials (encrypted at rest), consistent with local-first privacy. Synchronization is **incremental and idempotent**: re-running it updates the versions without duplicating nodes/edges.

## 7. Features to Create, Develop, and Maintain (MVP → Evolution)

### 7.1 MVP (First Release of the Scope)

| # | Feature | What it involves (backend / frontend) | Modules touched |
|---|---|---|---|
| 1 | **`decisions` domain (or extension of `knowledge`)** | Specific node/edge models (Ticket, ADR, Alternative, causal relationships), in/out ports, service; wiring in `DomainConfig` | domain, infrastructure |
| 2 | **Structured ADR CRUD (MADR)** | Form and API to create/read/update ADR with context, decision, rationale, alternatives, status; immutable versioning; `/api/v1/decisions/*` controller; standalone UI feature (Signals) | api, frontend, MySQL (Flyway, one query/file) |
| 3 | **Markdown ADR import** | `docs/adr/` file parser (Nygard/MADR) → ADR nodes + `supersedes` edges | infrastructure, batch |
| 4 | **Ticket ingestion (Jira/GitHub)** | PF4J plugin connectors + incremental sync; status/type/link normalization; deduplication | infrastructure (plugin), batch |
| 5 | **Graph construction: nodes + explicit links** | Creation of typed nodes and import of native links (blocks, caused by, parent/child) with high weight | domain, infrastructure |
| 6 | **AI inference of implicit links** | AI job that proposes `causes`/`motivates`/`impacts` edges with confidence weight | domain, infrastructure (Ollama) |
| 7 | **Human-in-the-loop review** | API and UI to confirm/correct nodes and edges; weight updating | api, frontend |
| 8 | **Interactive visualization of the causal graph** | Graph view with progressive expansion, filters by node/relationship type, highlighting of causal chains | frontend |
| 9 | **GraphRAG "why/what-if" with citations** | Sub-graph retrieval + Qdrant chunks; answer with citation of nodes/paths and deep-link; integration with existing chat | domain (knowledge/llm), frontend (chat) |
| 10 | **Automatic ADR↔context linking** | Upon ADR creation, automatic `motivates`/`discards-alternative`/`impacts`/`decided-by` edges | domain, api |
| 11 | **i18n IT/EN** | Enums (node/edge types, ticket/ADR statuses) translated and routed to the frontend according to the language switch | api, frontend |

### 7.2 Evolutions (Subsequent Releases)

| # | Feature | Added value |
|---|---|---|
| 12 | **ADR approval workflow** | Proposed→Accepted/Rejected states with registered approvers and audit trail |
| 13 | **Assisted root-cause** | Automatic effect→cause navigation with AI summary of the chain |
| 14 | **Impact analysis (what-if)** | "What depends on X?" — forward propagation over `depends-on`/`impacts` |
| 15 | **Decision consistency check** | The AI detects contradictions with existing decisions and duplicate discussions |
| 16 | **AI proposal of ADR drafts** | Generates a decision draft (with alternatives and risks) starting from a ticket/incident |
| 17 | **Additional connectors** (GitLab, Azure DevOps, PagerDuty, ServiceNow, Confluence) | Complete coverage of enterprise sources via PF4J plugin |
| 18 | **Commit/PR↔decision link** | End-to-end decision→implementation traceability |
| 19 | **Export for audit/compliance** | Decisional chain (rationale, approvers, timeline) in Markdown/JSON |
| 20 | **Decisional timeline and analytics (private)** | Proposed→Accepted time, superseded decisions, inconsistency hot-spots |
| 21 | **Link and gap suggestion** | The AI proposes non-obvious causal links and "orphan" ADR to review |
| 22 | **MCP exposure** | MCP tool to query the decisional graph from external agents/IDEs |

### 7.3 To Maintain (Continuous Maintenance)

- Plugin connectors (compatibility with the external Jira/GitHub/GitLab APIs that change over time).
- Normalization mapping of statuses/types/links as the tool configurations vary.
- GraphRAG prompts and logic (quality of causal inference and source citation).
- Graph schema and Flyway migrations (one query per file), with backward-compatible evolution.
- IT/EN translations of enums and UI at every new feature.
- Tuning of the weight factors (especially temporal and semantic) based on real feedback.
- Periodic graph hygiene (weak links, orphan nodes, ADR not linked to the implementation).

## 8. AI / GraphRAG Use Cases

1. **Memory of the "why".** "Why did we choose event sourcing for orders?" → The AI navigates the ADR-0042 node, its discarded alternatives and the motivating tickets, and answers citing "ADR-0042", "JIRA-456" and the linked postmortem. All locally with Ollama.
2. **Root-cause of an incident.** "What caused Friday's checkout outage?" → The AI starts from the Incident, traces back the `causes`/`resolves` edges up to the responsible commit and the decision that introduced the component, summarizing the chain with deep-links.
3. **Pre-change impact analysis.** "What do we risk if we remove the caching service?" → The AI traverses `depends-on`/`impacts` and shows the services, decisions, and tickets involved.
4. **Consistency check.** "Is this new decision consistent with the past?" → The AI detects that it contradicts ADR-0019 and flags a discussion already resolved in JIRA-321.
5. **Decisional onboarding.** "Tell me the architectural history of the payments module." → The AI reconstructs the sequence of ADR (including superseded ones), the key tickets, and the people involved.
6. **Superseded decisions still in use.** "Which decisions depend on now-superseded ADR?" → The AI follows `supersedes`/`depends-on` and highlights architectural debt.
7. **AI-assisted ADR draft.** Starting from a recurring incident, the AI proposes a decision draft with context, typical alternatives, and known risks, to be reviewed.
8. **Causal backlog synthesis.** "Which open bugs are blocked by decisions not yet made?" → The AI cross-references ticket status and the status of linked ADR.
9. **Multi-hop cross-domain question.** "Which customers/features are impacted by the services that depend on decision X?" → multi-hop reasoning over the graph, combining enterprise scopes.

## 9. KPIs & Success Metrics

| Category | Metric | Indicative target |
|---|---|---|
| **Adoption** | ADR created/linked per active team | Steady growth; every relevant decision tracked |
| **Coverage** | % tickets/ADR with at least one causal link | ≥ 70% of non-orphan nodes |
| **Graph quality** | % AI-suggested links confirmed | ≥ 60% acceptance net of corrections |
| **Groundedness** | % AI answers with at least one verifiable citation | ≥ 95% when the source exists |
| **Answer quality** | % answers rated useful and grounded | ≥ 80% thumbs up |
| **Root-cause effectiveness** | Average time to reconstruct a causal chain | Marked reduction vs manual search |
| **Institutional memory** | % decisions with rationale + alternatives filled in | ≥ 80% of ADR |
| **Onboarding** | Time to orient oneself in a new area | Measurable reduction |
| **Performance** | Local AI answer latency (Ollama) | Acceptable on on-prem hardware (target < a few seconds to first token) |
| **Privacy** | Data sent to cloud without consent | Zero (constraint, not objective) |
| **Compliance** | Decisions with approvers and timeline tracked | 100% for regulated projects |

## 10. Risks & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| **Noisy causal inference** (incorrect `causes` links) | Misleading chains, distrust | Mandatory human-in-the-loop on causal links; distinct weights for explicit vs inferred links; confidence thresholds |
| **AI hallucinations** | Wrong answers on critical decisions | GraphRAG constrained to the graph; mandatory citation; "not present in the data" answer when the source is missing |
| **Uncompiled rationale** (ADR empty of motivation) | Poor memory | Guided form that encourages context/alternatives; AI draft assistance; completeness KPI |
| **Connector drift** (changing external APIs) | Broken sync, stale data | Connectors isolated as versioned PF4J plugins; monitoring; idempotent sync |
| **Privacy of enterprise data** | Trust/compliance violation | Strict local-first; encrypted tokens; no cloud sending without consent; no telemetry |
| **Graph overload** (too many nodes/edges) | Unreadable view, slow queries | Progressive expansion, filters, clustering by area/service; depth limits |
| **Deep graph queries on MySQL+Qdrant** | Slow causal paths on large graphs | Targeted indexing, materialization of frequent paths, depth limits; reconsider a dedicated graph only if necessary (project constraint) |
| **Heterogeneous normalization** (different statuses/links across tools) | Wrong mappings, lost links | Configurable mapping confirmed by the user; reasonable defaults; bilingual enums |
| **Cultural resistance** (teams that do not document) | Low adoption | Low friction (ADR from a ticket in one click), immediate value ("why" answers), integration into the existing flow |
| **Ambiguity of historical rationale** | Misinterpreted decisions | Immutable and versioned preservation; citation of the original discussions; no rewriting of history |

## 11. Maintenance & Evolution

- **Incremental and idempotent update.** The sync jobs update statuses and new links without rebuilding the graph; each modification generates a new version (immutability) and preserves the audit trail.
- **Graph hygiene.** Periodic routines flag orphan nodes, weak unconfirmed links, `Accepted` ADR without a linked implementation, and alternatives never associated with a decision.
- **Schema versioning.** Each evolution of the node/edge types goes through backward-compatible Flyway migrations (one query per file), with a documented backfill strategy.
- **Model and prompt tuning.** Periodic update of the causal inference and GraphRAG prompts, of the recommended Ollama models, and of the chunking parameters, guided by the metrics of §9.
- **Weight calibration.** The weight factors (§5.3) — in particular temporal proximity, semantic similarity, and explicit origin — are refined on real data, maintaining interpretability for the audit.
- **Connector compatibility.** Monitoring of the external APIs (Jira, GitHub, GitLab, PagerDuty…) and updating of the corresponding PF4J plugins, with regression tests.
- **Bilingual documentation.** Each feature updates the IT/EN documentation and the logs in `Sviluppi/` according to the project conventions.
- **Evaluation roadmap.** Introduce a set of decisional "golden questions" (e.g., known causal chains) to measure regressions in the quality of the answers and of the inference.

## 12. Integration with Existing LocalMind Modules

| Existing module | Role in the Ticketing & decisions scope |
|---|---|
| **knowledge** | Base of the graph engine: extension with ticketing and decision node/edge types; natural point where to graft the domain |
| **document** | Ingestion and parsing of Markdown ADR files, RFCs, design docs (Tika), chunking and provenance |
| **llm** | Causal inference, ADR drafts, and GraphRAG answers via `LlmGatewayService`; Ollama default, cloud optional with consent |
| **(Qdrant) vectorstore** | Semantic index of the ticket/ADR texts for GraphRAG retrieval |
| **batch** | Incremental connector sync jobs and periodic graph recalculation/hygiene jobs |
| **mcp** | Exposure of tools (e.g., query the decisional graph, create an ADR) to external agents/IDEs |
| **agent** | "Decision memory" agent that orchestrates root-cause, impact analysis, and consistency check |
| **automation** | Automatic triggers: "new incident → propose ADR draft", "superseded ADR → flag dependencies", "bug blocked by decision → notify" |
| **email** | Extraction of decisions and rationales discussed via mail threads (evolution) |
| **calendar** | Architecture review dates and decision deadlines as context |
| **marketplace + plugin (PF4J)** | Jira/GitHub/GitLab/PagerDuty connectors and domain packages as installable modules |
| **messaging** | Notifications on decisions awaiting approval, causal links to confirm |
| **auth** | Local-first identity, roles, and governance (who approves ADR, who modifies the graph) |
| **common** | Domain events (e.g., "ADR accepted", "link confirmed"), private analytics, error handling |
| **finetuning** | Possible adaptation of local models to the decisional lexicon of the organization (advanced) |
| **Frontend (Angular 21)** | New standalone feature with Signal store, interactive causal graph view, ADR form (MADR), GraphRAG chat with citations; i18n IT/EN |

The Ticketing & decisions scope is therefore an **enterprise vertical** of the universal engine: it fully reuses the existing infrastructure (ingestion, embedding, LLM, graph, plugins, events) and adds only the causal and decisional node/relationship types, the rationale capture and root-cause features, and the user experience specific to architects, developers, and teams — consistent with the "one platform, multiple ecosystems" principle, remaining local-first, free, private, and bilingual.
