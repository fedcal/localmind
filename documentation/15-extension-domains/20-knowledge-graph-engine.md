# Knowledge Graph Engine

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This document describes the **technological heart** of LocalMind: the **universal, domain-agnostic Knowledge Graph Engine**. It is not a vertical (tourism, events, enterprise…) but the *core* on which every vertical is instantiated. The goal is to transform LocalMind from a document manager + semantic search + multi-provider chat into a **weighted and interactive graph of knowledge**, where every node is a typed piece of information and every edge is a weighted relationship, navigable by the AI through GraphRAG. Everything must remain **local-first**, self-hostable, with Ollama AI by default, **reusing MySQL 8.0 (graph structure) and Qdrant (semantics)** — without introducing Neo4j or other dedicated graph databases. The engine is the shared foundation: only the node types, relationship types and installed modules change, while the core of persistence, querying, weighting and AI navigation remains a single one.

The starting point is not greenfield: in the `knowledge` domain there already exist `KnowledgeEntity`, `KnowledgeRelation`, `EntityType`, `RelationType`, `KnowledgeSubgraph`, the `KnowledgeGraphUseCase`, the `KnowledgeGraphPort`, the `JdbcKnowledgeGraphAdapter` adapter, the `LlmEntityExtractorAdapter` extractor and the Flyway tables `knowledge_entities` (V66) and `knowledge_relations` (V67). This document starts from that base, highlights its limitations (first and foremost the **absence of edge weight** and the rigidity of the type enums) and charts the evolution toward a complete, modular and weighted engine.

---

## 1. What we solve (problem & value)

### 1.1 The underlying problem

Current knowledge platforms — both consumer (maps, reviews) and enterprise (wikis, drives, ticketing) — all suffer from the same structural limitation: **they treat information as isolated documents indexed by keyword or by semantic proximity, but not as a network of explicit and weighted relationships**. The practical consequences are heavy and recurring in every domain:

- **Knowledge lives in disconnected silos.** A document, an email, a microservice, a place, a person each exists in its own system; the connections between them (who depends on what, what is linked to what, why) are in people's heads or scattered across PDFs and threads, never materialized into a queryable structure.
- **Pure semantic search (classic RAG) is not enough.** Vector RAG retrieves the chunks *most similar* to a question, but it is blind to **multi-hop relationships**: it cannot answer *"which microservices would be impacted if I change this API, and who are their owners?"* or *"which experiences can be paired with this village reachable without a highway and connected to an event this weekend?"*. These questions require **traversing edges**, not just comparing embeddings. This is the well-documented limitation of "flat" RAG that GraphRAG was created to overcome.
- **Non-obvious connections remain invisible.** The highest value of knowledge lies in the links that no one has made explicit: two processes that share a fragile dependency, two places united by the same producer, two tickets that are symptoms of the same root cause. Without a graph, these patterns never emerge.
- **AI answers are neither explainable nor traceable.** An LLM that answers "from memory" or from a flat RAG cannot cite *which path of facts* it used. In the enterprise (privacy, audit, compliance) and consumer (trust, ranking transparency) realms, this opacity is unacceptable.
- **Every domain reinvents the wheel.** Building a tourism graph, an enterprise one, an educational one as three separate products means tripling persistence, querying, weighting and AI logic. **A single engine** parameterizable by domain is needed.

### 1.2 The solution: a weighted, domain-agnostic graph engine

LocalMind responds with a **universal Knowledge Graph Engine** that models any information as a **typed node** and any connection as a **weighted and typed edge**. On top of this structure the AI navigates (GraphRAG) by combining two complementary signals:

1. **Structural signal** — the explicit relationships of the graph (paths, neighbors, subgraphs, dependencies), persisted in MySQL.
2. **Semantic signal** — the vector similarity of the embeddings of nodes and descriptions, persisted in Qdrant.

The fusion of the two signals (hybrid retrieval) is what distinguishes a mature GraphRAG engine from a flat RAG: you start from a *seed* found by semantic similarity, **expand along the weighted edges** to gather the relevant relational context, and provide the LLM with a cohesive and citable subgraph instead of a bag of disconnected chunks.

The **edge weight** is the central differentiating element required by the project vision. The weight is not an arbitrary number: it encodes **intensity, quality, reliability and currency** of a relationship, derived from configurable factors (usage frequency, relevance/co-occurrence, dependency strength, user feedback, recency, AI extraction confidence). The weight guides navigation (the AI explores the strongest edges first), ranking (the most relevant links emerge), visualization (thick edges = strong relationships) and context pruning (weak edges are cut to stay within the token budget).

### 1.3 Why a single engine for two (and more) ecosystems

| Dimension | Current flat RAG | LocalMind Knowledge Graph Engine |
|---|---|---|
| Unit of knowledge | Text chunk | Typed node + weighted edge + chunk |
| Type of query | "What is similar to X?" | "What is connected to X, how, and how strongly?" (paths, neighbors, subgraphs) |
| Multi-hop relationships | Not supported | Native (weighted traversal at configurable depth) |
| Explainability | Low (disconnected chunks) | High (nodes + paths cited in the answer) |
| Non-obvious connections | Invisible | Emergent (link prediction, community detection) |
| Cross-domain reuse | Per-product | One core, N domain modules (modular schema) |
| Infrastructure | Qdrant only | MySQL (structure) + Qdrant (semantics), already present |

The engine is therefore **the backbone** of the Core Value declared in `.planning/PROJECT.md`: *"the AI must be able to navigate a weighted graph of knowledge to answer complex questions and surface non-obvious connections — in any domain, while remaining local-first"*. If everything else fails, this must work.

### 1.4 Concrete value by type of adoption

- **Consumer (tourism, events, culture, sports…):** relational discovery, AI-generated itineraries on the graph, emergent and transparent ranking, data sovereignty for municipalities and tourist boards.
- **Enterprise (docs, processes, repos, microservices, APIs, people, mail):** a living map of dependencies, impact analysis, accelerated onboarding, traceable and auditable AI answers, privacy guaranteed by local-first.
- **Developers/integrators:** a single graph API + PF4J plugins to define new node/relationship types and connectors, without touching the core.

### 1.5 Current state and gap to fill (brownfield baseline)

The `knowledge` domain already provides a base, but one that is incomplete relative to the vision. The main gaps this engine must fill:

| Aspect | Current state (baseline) | Gap / evolution needed |
|---|---|---|
| Edge weight | **Absent** — `KnowledgeRelation` only has `id, from, to, type, properties` | Add `weight` (and component factors) as a first-class citizen of the model, schema and queries |
| Node/relationship types | Hard-coded enums (`EntityType`: 7 values; `RelationType`: 8 values) | **Modular** schema extensible per domain without recompiling the core |
| Query operations | Only `searchEntities`, `getEntitySubgraph(depth)` | Add **paths** (shortest/weighted path), **neighbors** (weighted k-hop), **filtered subgraphs**, search by relationship |
| Node/relationship CRUD | Only `indexText` (AI extraction) and `deleteEntity` | Complete and idempotent CRUD APIs for nodes and edges (manual creation + from connectors) |
| Semantic persistence | Entities in MySQL; embeddings not linked to nodes | Link nodes to Qdrant vectors for the GraphRAG semantic seed |
| AI navigation | `indexText` feeds the graph, but the graph is not yet queried by the AI | GraphRAG tool that the LLM uses to explore and cite |
| Visualization | Absent on the frontend side | Interactive view of the weighted graph (Angular) |

---

## 2. Personas & target users

| Persona | Profile | Goals with respect to the engine | Needs from the system |
|---|---|---|---|
| **Domain developer** | Builds a vertical (tourism, enterprise…) on top of the engine | Define node/relationship types, weights, connectors | Stable graph API, modular schema, PF4J plugins, SDK, IT/EN docs |
| **Data/Knowledge engineer** | Curates the quality of the graph | Ingestion, deduplication, merge, weight validation | Ingestion pipeline, entity resolution tools, audit |
| **End user (consumer)** | Explorer of a consumer vertical | Get navigable answers and paths | GraphRAG answers with citations, visual navigation |
| **Knowledge worker (enterprise)** | Employee searching internal knowledge | Understand dependencies, owners, impacts | Impact analysis, relational search, guaranteed privacy |
| **Administrator / DevOps** | Manages the self-hosted instance | Performance, backup, weight tuning | Weight configuration, metrics, depth limits, MySQL+Qdrant |
| **AI / LLM agent** | Programmatic consumer of the graph | Explore the graph as a tool during reasoning | Traversal tools/functions, token budget, citable formats |
| **Moderator / curator** | Validates contributions and relationships (consumer) or approves merges (enterprise) | Confirm/correct edges and weights | Review queues, feedback that feeds the weight |

Primary persona of the engine (core): the **Domain developer** and the **AI/LLM agent**. The engine is infrastructure: its success is measured by how easy it is to build a vertical on top of it and how well the AI knows how to navigate it.

---

## 3. Input requirements

This section defines in detail **what is needed as input** for the engine to work: data, configurations, contracts and constraints. It is deliberately exhaustive because it is the contract on which every subsequent development depends.

### 3.1 Domain input (definition of the modular schema)

The engine is domain-agnostic: before ingesting data, a domain must **declare its own schema**. This is the foundational input.

| Input | Description | Expected form | Mandatory |
|---|---|---|---|
| **Node type catalog** | Set of the domain's `NodeType`s (e.g. `PLACE`, `MICROSERVICE`) with IT/EN label | Registered definition (DB or plugin), no longer a hard-coded enum | Yes |
| **Relationship type catalog** | Set of `RelationType`s with IT/EN label, directionality, and *allowed type pairs* (from→to) | Registered definition with domain/codomain constraints | Yes |
| **Property schema** | For each node/edge type, the expected properties (key, type, validation) | Lightweight schema (JSON Schema-like) over `properties` | Recommended |
| **Weighting policy** | Which factors weight the edges and with which coefficients for that domain | `WeightPolicy` configuration (see §5) | Yes (with defaults) |
| **Embedding mapping** | Which node fields contribute to the semantic embedding | Ingestion configuration | Recommended |

Constraint: the catalogs must be **bilingual IT/EN** (translated labels, redirected to the frontend based on the language switch, as per the project rule). Types must not require recompilation of the core: they are to be managed as data or as PF4J extensions.

### 3.2 Content input (feeding the graph)

These are the raw or structured data from which nodes and edges are born.

| Source | Examples | Ingestion mode | Notes |
|---|---|---|---|
| **Unstructured text** | Documents (Tika/OCR already present), notes, mail | Entity+relationship extraction via LLM (existing `LlmEntityExtractorAdapter`) | Extraction confidence → weight factor |
| **Structured data** | CSV/JSON, open data (OSM, Wikidata), DB exports | Dedicated connectors that map records → nodes/edges | Deterministic, high confidence |
| **External APIs** | Git repos, microservice registries, ticketing, calendars | Connectors/plugins with polling or webhook | Enterprise; privacy compliance |
| **Manual contributions** | Users/curators who create nodes and relationships from the UI | CRUD API | Attribution + reputation → weight feedback |
| **System events** | LocalMind domain events (conversations, document ingestion) | `DomainEventPublisherPort` → listener that creates edges | Usage frequency → weight factor |

For each ingested element at least the following are required: **source identity** (`sourceDocumentId`/`sourceConnectorId`), **timestamp**, and — where applicable — **confidence**. These metadata feed deduplication, audit and weighting.

### 3.3 Input for creating/updating a node (API contract)

To create or update a node the caller must provide:

- `type` (valid NodeType in the domain catalog) — **mandatory**, validated.
- `name` / canonical label — **mandatory**, used for deduplication and display (max 200 chars, consistent with the current schema `VARCHAR(200)`).
- `properties` — key/value map conforming to the type schema (validated at the system boundaries). Examples: coordinates for `PLACE`, repository URL for `MICROSERVICE`.
- `sourceDocumentId` / origin — for traceability.
- *(optional)* fields for the embedding (descriptive text) used to index the node on Qdrant.
- *(optional)* `externalId` — natural key for idempotent upsert from connectors.

Minimum validations: type existing in the catalog, non-empty name, properties conforming, lengths respected, no trusted input without validation (project security rule).

### 3.4 Input for creating/updating a relationship (API contract)

- `fromEntityId`, `toEntityId` — **mandatory**, must reference existing nodes (FK constraint already present in V67).
- `type` (valid RelationType) — **mandatory**; the pair (fromType, toType) must respect the declared domain/codomain constraints.
- `weight` and/or **weight factors** (see §5) — if not provided, computed from the domain's `WeightPolicy`. **Gap relative to the baseline: the weight field does not exist today and must be introduced.**
- `properties` — metadata (e.g. confidence, source, recency).
- `directed` — whether the relationship is oriented or symmetric (default by type).

Validations: existing nodes, type allowed for the pair, weight normalized in `[0,1]`, no self-loops except for types that allow it, idempotency on (from, to, type) to avoid duplicate edges.

### 3.5 Input for graph queries

| Query | Required inputs | Default/constraints |
|---|---|---|
| **Neighbors** | `nodeId`, `depth` (k-hop), filters (node/edge types), `minWeight`, `limit` | `depth` with cap (e.g. ≤3) for cost; `minWeight` for pruning |
| **Path** | `fromId`, `toId`, strategy (shortest / max-weight), type filters | Hop limit; weights as cost/utility |
| **Subgraph** | `seedNodeId(s)`, `depth`, filters, `minWeight`, `maxNodes` | Cap on nodes/edges for memory and UI protection |
| **Search by relationship** | `relationType`, filters on node types, ordering by weight | Pagination |
| **GraphRAG query** | natural language question, domain, token budget, depth | Combines semantic seed (Qdrant) + weighted expansion |

### 3.6 Configuration and environment input

- **LLM profile** for extraction and for GraphRAG: provider (Ollama default), model, temperature — reusing the `LlmGatewayService` fallback chain.
- **Qdrant connection** and node collection (reuse `QdrantVectorStoreAdapter`).
- **MySQL datasource** and Flyway migrations (one query per file — project rule).
- **Operational limits:** maximum traversal depth, maximum subgraph size, query timeout, GraphRAG token budget.
- **WeightPolicy** per domain (coefficients, normalization, temporal decay).
- **Language** (IT/EN) for labels and answers.

### 3.7 Constraints and pre-conditions (non-functional)

- **Local-first / self-hostable:** no input must require mandatory cloud services; the extraction AI and GraphRAG must run with Ollama.
- **Enterprise privacy:** ingested data does not leave the instance without explicit consent; cloud connectors are opt-in.
- **Hexagonal architecture:** inputs pass through `port/in`; no framework logic in the domain.
- **MySQL+Qdrant reuse:** no new graph datastore.
- **i18n:** every enum/type exposed to the frontend is translated IT/EN.

---

## 4. Activity flow (step-by-step)

The engine has three macro flows: **(A) feeding the graph (ingestion)**, **(B) structural querying of the graph**, **(C) AI navigation / GraphRAG**. They are described in detail because they constitute the observable behavior of the system.

### 4.1 Flow A — Feeding the graph (ingestion → weighted nodes/edges)

```
Source → Extraction → Identity resolution → Node/edge construction →
Weighting → Persistence (MySQL + Qdrant) → Events/indexing
```

1. **Input acquisition.** A document/record/contribution arrives via a connector or the CRUD API. The system validates the input at the boundaries (type, properties, source) and records origin + timestamp.
2. **Entity and relationship extraction.**
   - For unstructured text: `LlmEntityExtractorAdapter` (Ollama default) extracts candidate nodes and edges with a **confidence score** for each.
   - For structured data: the connector deterministically maps the fields to nodes/edges (confidence = 1.0).
3. **Identity resolution (entity resolution / deduplication).** Each candidate node is compared with existing nodes by canonical name + type + semantic similarity (Qdrant) + `externalId`. If a match above threshold exists → **merge** (update properties, sum evidence); otherwise → **new node**. This avoids the explosion of duplicates, a typical problem of LLM-fed knowledge graphs.
4. **Edge construction.** For each candidate relationship the domain/codomain constraints are verified (is the type pair allowed?). Duplicate edges on (from, to, type) are consolidated instead of recreated.
5. **Weight computation.** The domain's `WeightPolicy` is applied, combining the available factors (extraction confidence, co-occurrence, recency, any prior feedback) into a weight normalized to `[0,1]`. If the edge already existed, the weight is **updated incrementally** (e.g. reinforcement for new evidence, temporal decay).
6. **Dual persistence.**
   - **MySQL:** idempotent upsert on `knowledge_entities` and `knowledge_relations` (with the new `weight` column and the factors). Transactional operation.
   - **Qdrant:** upsert of the node embedding (descriptive text) with payload `nodeId, type, domain` for the future semantic seed.
7. **Events and side-effects.** Publication of a domain event (`KnowledgeGraphUpdatedEvent`) via `DomainEventPublisherPort`; listeners can update statistics, moderation queues (consumer) or impact analysis triggers (enterprise).
8. **Tracking.** Each node/edge keeps the link to the source for audit and for computing the "usage frequency" weight over time.

**Error handling:** failed extraction → log + skip the single element without aborting the batch; FK/type constraint violation → rejection with a clear message; merge conflict → configurable strategy (auto vs review queue). No error is silently swallowed (project rule).

### 4.2 Flow B — Structural querying of the graph

```
Query request → Validation/limits → Weighted traversal on MySQL →
Pruning (minWeight, cap) → Subgraph assembly → Typed response
```

1. **Request.** The client calls `port/in` with one of the patterns: neighbors, path, subgraph, search by relationship (see §3.5).
2. **Validation and limit application.** Depth within the cap, `maxNodes` respected, valid type filters, `minWeight` applied.
3. **Traversal execution.** On MySQL, starting from the seed node(s), edges are expanded respecting filters and `minWeight`, ordering by descending weight. For paths, shortest-path or max-weight-path is applied within the hop limit. Queries use the existing indexes (`idx_kr_from`, `idx_kr_to`) and additional indexes on `(relation_type, weight)`.
4. **Pruning and protection.** Edges below the weight threshold are discarded; the subgraph is limited in nodes/edges to protect memory and UI.
5. **Assembly.** A `KnowledgeSubgraph` (nodes + edges + weights) is built, typed and immutable (project immutability pattern).
6. **Response.** Returned via a bilingual DTO (IT/EN labels of the types). The frontend can render the interactive view.

### 4.3 Flow C — AI navigation / GraphRAG (the flagship flow)

```
NL question → Semantic seed (Qdrant) → Weighted expansion (MySQL) →
Context selection within budget → Prompt with citable subgraph →
LLM (Ollama) → Answer + citations of the nodes/paths
```

1. **Natural language question.** The user (or an agent) poses a complex question in a domain.
2. **Seed identification (semantic).** The question is embedded and searched on Qdrant → top-N semantically closest nodes (starting anchors). This connects GraphRAG to the existing semantic search.
3. **Relational expansion (structural).** From the seed nodes a weighted traversal is performed (Flow B) to gather the relevant subgraph: strong neighbors, paths between seeds, dependencies. The **weight guides the order of exploration** and the pruning.
4. **Context selection within budget.** The subgraph is "flattened" into a textual context ordered by relevance (weight + similarity), trimmed to respect the local model's token budget. High-weight nodes/edges and paths that connect multiple seeds are prioritized.
5. **Prompt construction.** The LLM is provided with the subgraph as structured and citable context (nodes with id, edges with type and weight), with explicit instruction to **cite the nodes/paths used**.
6. **Generation.** Call to `LlmGatewayService` (Ollama default, opt-in cloud fallback) to produce the answer.
7. **Answer with traceability.** The output includes the answer + the list of the cited nodes and paths (explainability). On the frontend side, the cited nodes are clickable and open the graph view.
8. **Feedback loop.** Any user feedback (useful/not useful, confirmation of a relationship) feeds the "feedback" factor of the weight of the edges involved, improving future answers.

### 4.4 Cross-cutting flow — Suggestion of non-obvious connections (link prediction)

Periodically (batch) or on-demand, the engine analyzes the graph to **propose missing edges**: nodes with many neighbors in common, high semantic similarity but no direct edge, recurring structural patterns. The suggestions enter a review queue (curator/moderator) or, above a high confidence threshold, are created with a low initial weight and reinforced by feedback. It is the function that fulfills the promise of "surfacing non-obvious connections".

---

## 5. Graph model (node types, relationship types, weight criteria)

### 5.1 Principle: core schema + domain catalogs

The core defines the **primitives** (Node, Edge, Weight, Subgraph) and a **minimal cross-cutting catalog**; each domain adds its own types through a registered catalog or PF4J plugin. Today `EntityType` and `RelationType` are hard-coded enums: the evolution is to make them **extensible** (type registry + IT/EN labels) while maintaining backward compatibility with the existing values.

### 5.2 Core node types (cross-cutting) — baseline and extensions

| NodeType (core) | Description | State |
|---|---|---|
| `DOCUMENT` | Document/textual resource | Existing |
| `PERSON` | Person | Existing |
| `ORGANIZATION` | Organization/entity | Existing |
| `PLACE` | Geographic place | Existing |
| `CONCEPT` | Concept/theme/tag | Existing |
| `EVENT` | Temporal event | Existing |
| `TECHNOLOGY` | Technology/tool | Existing |

Examples of **per-domain** extensions (registered by the modules, not in the core):

| Domain | Additional node types |
|---|---|
| Consumer / tourism | `POI`, `RESTAURANT`, `EXPERIENCE`, `ITINERARY`, `REVIEW` |
| Enterprise | `MICROSERVICE`, `API`, `REPOSITORY`, `DATABASE`, `PROCESS`, `PROCEDURE`, `TICKET`, `DECISION`, `FAQ`, `SKILL` |
| Education | `COURSE`, `TOPIC`, `LESSON`, `STUDENT` |

### 5.3 Core relationship types — baseline and extensions

| RelationType (core) | Semantics | Directed | State |
|---|---|---|---|
| `RELATED_TO` | Generic/thematic link | No | Existing |
| `PART_OF` | Composition/membership | Yes | Existing |
| `LOCATED_IN` | Localization | Yes | Existing |
| `WORKS_AT` | Person→org affiliation | Yes | Existing |
| `CREATED_BY` | Authorship | Yes | Existing |
| `DEPENDS_ON` | Dependency | Yes | Existing |
| `REFERENCES` | Reference/citation | Yes | Existing |
| `MENTIONED_IN` | Mention in a document | Yes | Existing |

Per-domain extensions: consumer (`NEAR`, `PAIRED_WITH`, `STAGE_OF`, `SAME_PRODUCER`), enterprise (`CALLS`, `OWNED_BY`, `DEPLOYS_TO`, `CAUSED_BY`, `SUPERSEDES`, `DOCUMENTS`).

### 5.4 The edge weight — model and criteria

The weight `w ∈ [0,1]` is a **normalized combination of factors**, each with a configurable coefficient per domain (`WeightPolicy`). The factors envisaged by the project vision:

| Factor | What it measures | How it is computed | Usage example |
|---|---|---|---|
| **Usage frequency** | How often the relationship is traversed/co-occurs | Normalized count of traversals/co-occurrences over time | Heavily used edges rise in weight |
| **Relevance / co-occurrence** | Strength of the association between the nodes | Co-occurrence in documents, semantic similarity of the nodes (Qdrant) | Two concepts often cited together |
| **Dependency strength** | Criticality of the structural relationship | Explicit from the connector (e.g. hard vs soft dependency) | Critical vs optional `DEPENDS_ON` |
| **User feedback** | Human validation of the relationship | Votes/confirmations from curators/users, contributor reputation | Confirmed edges reinforced |
| **Extraction confidence** | Reliability of the AI origin | LLM score or 1.0 for structured data | Uncertain extractions weigh less |
| **Recency / decay** | Currency of the relationship | Temporal decay (configurable half-life) | Old relationships are attenuated |

Reference formula (normalized linear): `w = clamp01( Σ (coeff_i · factor_i) )`, with the coefficients defined in the domain's `WeightPolicy` and the factors normalized to `[0,1]`. The individual factors must be **persisted** (not just the final weight) to recompute the weight when the coefficients change or to explain it in the UI ("why does this edge weigh 0.82").

**Schema implications (gap to fill):** the `knowledge_relations` table must be extended with at least `weight DOUBLE` and the component factors (or a JSON blob of factors), plus an index on `(relation_type, weight)` and on `from_entity_id, weight`. Consistent with the Flyway rule "one query per file": one migration for `ADD COLUMN weight`, one for each index/factor.

### 5.5 Physical representation (MySQL + Qdrant)

- **MySQL** = graph structure: `knowledge_entities` (nodes), `knowledge_relations` (weighted edges). Traversals are recursive/iterative SQL queries with joins on the from/to indexes. The word `recursive` is reserved: it must be escaped with backticks in the DDL/CTE (project gotcha).
- **Qdrant** = semantics: one vector per node (embedding of the descriptive text) with payload `{nodeId, type, domain}`, used for the GraphRAG semantic seed and for entity resolution.
- **UUID mapping:** all `@Id` UUIDs require `@JdbcTypeCode(SqlTypes.CHAR)` (CHAR(36)), as already done in `KnowledgeRelationEntity`.

### 5.6 Conceptual diagram

```
                 (semantics)                      (structure)
        ┌───────────────────────┐        ┌────────────────────────────┐
        │        Qdrant         │        │           MySQL            │
        │  vector per node      │        │  knowledge_entities (nodes)│
        │  payload: type,domain │◄──────►│  knowledge_relations       │
        └───────────────────────┘  link  │  (edges + weight + factors)│
                  ▲   nodeId             └────────────────────────────┘
                  │                                   ▲
            semantic seed                      weighted traversal
                  │                                   │
                  └──────────► GraphRAG ◄─────────────┘
                       (Ollama default, citations)
```

---

## 6. Data sources & connectors (ingestion)

Feeding the graph reuses and extends the existing ingestion infrastructure. Connectors are the natural extension point via PF4J plugins.

| Source | Connector | Existing reuse | Output in the graph |
|---|---|---|---|
| Documents (PDF, Office, images) | Doc pipeline + Tika/Tesseract + `LlmEntityExtractorAdapter` | `DocumentIngestionPipelineService`, batch folder scan | `DOCUMENT` nodes/extracted entities + `MENTIONED_IN`/`REFERENCES` edges |
| Email (IMAP) | Email connector | `email` domain (Angus Mail) | `PERSON`/`ORGANIZATION`/`DOCUMENT` nodes + `CREATED_BY`, `REFERENCES` edges |
| Calendar | Calendar connector | `calendar` domain | `EVENT` nodes + `PART_OF`, `LOCATED_IN` edges |
| Git repository / microservices / API | New enterprise plugin | PF4J extension point | `REPOSITORY`/`MICROSERVICE`/`API` nodes + `DEPENDS_ON`, `CALLS`, `OWNED_BY` edges |
| Open data (OSM, Wikidata) | New consumer connector | — | `PLACE`/`POI` nodes + `NEAR`, `LOCATED_IN` edges |
| Manual contributions | CRUD API + UI | Angular frontend | Any node/edge, with attribution |
| System events | Listener on `DomainEventPublisherPort` | Existing event infrastructure | Derived edges + "usage frequency" update |
| MCP / external tools | MCP connector | `mcp` domain | Nodes/edges from federated tools |

Principles: every connector is **idempotent** (upsert by `externalId`), **traceable** (origin + timestamp), and **opt-in for cloud sources** (enterprise privacy). Connectors register as adapters of an ingestion `port/out`, keeping the domain pure.

---

## 7. Features to create, develop and maintain (MVP → evolution)

Legend: **[C]** create from scratch, **[E]** extend/evolve the existing, **[M]** maintain/hardening.

### 7.1 Engine MVP (indispensable foundations)

| # | Feature | Type | Implementation notes |
|---|---|---|---|
| 1 | **Edge weight** | [E] | Add `weight` (+ factors) to `KnowledgeRelation`, `KnowledgeRelationEntity`, Flyway migrations (one query per file), mapper |
| 2 | **Node and relationship CRUD** | [E] | Extend `KnowledgeGraphUseCase`/`KnowledgeGraphPort` with idempotent and validated create/update/get/delete |
| 3 | **Configurable WeightPolicy** | [C] | Weight computation service (factors + coefficients per domain), in the domain, wired in `DomainConfig` |
| 4 | **Query: weighted k-hop neighbors** | [E] | Traversal on MySQL with `minWeight`, type filters, depth cap |
| 5 | **Query: path (shortest / max-weight)** | [C] | Path between two nodes within a hop limit |
| 6 | **Query: filtered subgraph** | [E] | Evolve `getEntitySubgraph` with filters, `minWeight`, `maxNodes` |
| 7 | **Semantic indexing of nodes on Qdrant** | [E] | Link nodes to vectors for the GraphRAG seed and entity resolution |
| 8 | **Entity resolution / deduplication** | [C] | Merge by name+type+similarity+externalId during ingestion |
| 9 | **Base GraphRAG** | [C] | Semantic seed → weighted expansion → context within budget → answer with citations (Ollama default) |
| 10 | **REST API + bilingual DTOs** | [E] | `KnowledgeController` with CRUD/query endpoints under `/api/v1/knowledge`, enums translated IT/EN |
| 11 | **Modular type schema (registry)** | [C] | Replace hard-coded enums with an extensible registry + IT/EN catalogs, backward-compatible |
| 12 | **Flyway migrations** | [C] | `weight`, factors, indexes `(relation_type, weight)`; one query per file |

### 7.2 Evolution (post-MVP)

| # | Feature | Type | Value |
|---|---|---|---|
| 13 | **Interactive graph visualization (Angular)** | [C] | Nodes/edges/weight, progressive expansion, filters by type/domain, clickable cited nodes |
| 14 | **Link prediction (non-obvious connections)** | [C] | Suggestions of missing edges → review/feedback queue |
| 15 | **Community detection** | [C] | Graph clustering (e.g. Leiden/Louvain) for thematic summaries in GraphRAG "global search" style |
| 16 | **GraphRAG as an agent tool** | [E] | Expose the traversal as an LLM/MCP tool for the `agent` domain |
| 17 | **Temporal decay of weights (batch)** | [C] | Scheduled job that applies recency/half-life |
| 18 | **Weighting from user feedback** | [E] | Consumer/enterprise feedback loop → weight factor |
| 19 | **Domain connectors (repo/API/open data)** | [C] | PF4J plugins for enterprise and consumer |
| 20 | **Graph versioning / audit** | [C] | History of node/edge changes for enterprise compliance |
| 21 | **Installable domain modules** | [E] | Consumer/enterprise packages via marketplace |
| 22 | **Query optimization / caching** | [M] | Caching of hot subgraphs (Spring Cache/Caffeine already present), indexes |

### 7.3 Continuous maintenance

- **[M]** Graph quality: monitoring of duplicates, orphan edges, anomalous weights; MySQL↔Qdrant consistency jobs.
- **[M]** Traversal performance as the graph grows (caps, indexes, possible neighborhood denormalization).
- **[M]** Consistency of the IT/EN type catalogs as modules change.
- **[M]** Tests: unit (pure domain), integration (Testcontainers MySQL), coverage ≥80% as per project rules.
- **[M]** IT/EN documentation updated with every development + log in `Sviluppi/`.

---

## 8. AI / GraphRAG use cases

| Use case | Domain | How the graph is navigated | Output |
|---|---|---|---|
| **Impact analysis** | Enterprise | From an `API`/`MICROSERVICE`, weighted k-hop `DEPENDS_ON`/`CALLS` traversal | "If you change X, you impact Y, Z; owners: …" with cited path |
| **Onboarding / complex question** | Enterprise | Semantic seed on docs/processes + `PART_OF`/`DOCUMENTS` expansion | Synthetic answer with cited nodes and procedures |
| **Root cause** | Enterprise | `CAUSED_BY` traversal on tickets/incidents | Probable causal chain, weighted |
| **Tailored itinerary** | Consumer | `PLACE` seed + `NEAR`/`PAIRED_WITH`/`STAGE_OF` expansion with constraints | Itinerary as a path in the graph, explained |
| **Discovery of hidden gems** | Consumer | Emergent ranking from weight + community, not popularity | Transparent and citable suggestions |
| **Non-obvious connections** | Cross-cutting | Link prediction on common neighbors + similarity | Proposals of new edges |
| **Global thematic summary** | Cross-cutting | Community detection + per-cluster summary | Overview of an area of the graph |
| **Agent with graph tool** | Cross-cutting | The LLM invokes traversal as a tool during reasoning | Traceable multi-step answers |

All use cases produce answers **with citation of the nodes/paths**, run with **Ollama by default** and respect the budget/token and depth limits.

---

## 9. KPIs & success metrics

| Category | KPI | Indicative target |
|---|---|---|
| **Graph coverage** | No. of nodes/edges, % nodes with ≥1 edge, % edges with computed weight | Steady growth; <5% isolated nodes |
| **Quality** | Duplicate rate post-resolution, orphan edges, % edges confirmed by feedback | Duplicates <2%, zero orphans |
| **AI quality (GraphRAG)** | % answers with valid citations, correctness on an evaluation set, multi-hop recall vs flat RAG | Measurable improvement vs RAG baseline |
| **Explainability** | % answers that cite nodes/paths actually used | ~100% |
| **Performance** | Traversal latency (k-hop), end-to-end GraphRAG latency, p95 | Traversal <300ms at depth 2 on a medium graph |
| **Developer adoption** | Time to instantiate a new domain, no. of custom types/connectors | New domain in days, not weeks |
| **Local-first** | % functions operational with Ollama only (no cloud) | 100% of the core functions |
| **Privacy** | No. of data sent to cloud without consent | 0 |

### 9.1 Instrumentation

Reuse of Actuator + Micrometer/Prometheus (already present) for latency metrics, graph size, subgraph cache hit/miss, and ingestion error rates.

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| **Expensive traversals on MySQL as the graph grows** | Latency, DB load | Depth/node caps, `minWeight` pruning, targeted indexes, hot subgraph caching, possible denormalized neighborhood table |
| **Explosion of duplicates from LLM extraction** | Noisy graph, unreliable queries | Robust entity resolution (name+type+similarity+externalId), thresholds, review queue |
| **Arbitrary or unexplainable weights** | Loss of trust, poor ranking | Persist the factors, transparent formula, UI that explains the weight, feedback loop |
| **GraphRAG hallucinations / invented citations** | Unreliable answers | Provide only the real subgraph as context, validate citations against the provided nodes |
| **Native absence of graph operations in MySQL (no Neo4j)** | Query complexity | Limited recursive CTEs + iterative application-level traversal; re-evaluate a dedicated graph only if the KPIs require it (currently Out of Scope) |
| **Rigid schema (hard-coded enums)** | Blocks new domains | Migration to an extensible type registry + PF4J plugins, maintaining backward compatibility |
| **MySQL↔Qdrant inconsistency** | Wrong semantic seed | Reconciliation job, transactional write + outbox for vector upserts |
| **Enterprise privacy** | Data leak | Opt-in cloud connectors, local AI default, no sending without consent |
| **Cost of computing embeddings/extraction locally** | Slowness on modest hardware | Batching, lightweight Ollama models, asynchronous computation, caching |
| **Monolithic Flyway migrations** | Violation of the project rule | One query per file, always |

---

## 11. Maintenance & evolution

- **Incremental hardening:** after the MVP, prioritize traversal performance (indexes, caching, neighborhood denormalization) and graph quality (consistency jobs, continuous deduplication).
- **Schema evolution:** each new domain adds types/connectors without touching the core; migrations remain additive and with a single query per file. The existing enum values must be preserved for backward compatibility during the transition to the registry.
- **Weight governance:** periodically review the coefficients of the `WeightPolicy`s based on the answer quality metrics; the temporal decay must be calibrated per domain.
- **Consistency of the two stores:** monitor and reconcile MySQL↔Qdrant; treat Qdrant as a derived index rebuildable from MySQL (source of truth of the structure).
- **Extensibility via plugins:** connectors and domain types live as PF4J extensions and marketplace modules, tested in isolation.
- **Tests and coverage:** unit on the pure domain, integration with Testcontainers MySQL, periodic evaluation of GraphRAG quality with a reference question set; coverage ≥80%.
- **Documentation:** constant IT/EN update with every development and a dated log in `Sviluppi/` (project rules). The exposed enums/types remain bilingual toward the frontend.
- **Architectural re-evaluation:** the "no Neo4j" choice is valid now; it should be reopened only if the traversal latency/scalability KPIs require it, as foreseen in `PROJECT.md`.

---

## 12. Integration with the existing LocalMind modules

| Module / domain | Role with respect to the engine | Concrete integration |
|---|---|---|
| **`knowledge`** | It is the engine itself (brownfield base) | Extend models, ports, services, adapters (`JdbcKnowledgeGraphAdapter`, `LlmEntityExtractorAdapter`) |
| **`llm` / `LlmGatewayService`** | Entity extraction and GraphRAG generation | Ollama default + fallback chain; no LLM logic in the knowledge domain |
| **`document`** | Primary source of nodes/edges | Tika/OCR pipeline + extraction → graph; `MENTIONED_IN` edges |
| **Qdrant (`vectorstore`)** | Graph semantics | Embedding per node, GraphRAG seed, entity resolution (`QdrantVectorStoreAdapter`, `EmbeddingConfig` @Primary Ollama) |
| **MySQL + Flyway** | Graph structure | `knowledge_entities`/`knowledge_relations` tables extended with weight; additive migrations |
| **`email` / `calendar`** | Ingestion connectors | Events, people, organizations → nodes/edges |
| **`mcp`** | External tools and federation | GraphRAG exposed as an MCP tool; ingestion from tools |
| **`agent`** | Consumer of the graph | The agent uses the traversal as a tool in its reasoning |
| **`automation` / event infra** | Incremental update | Listener on `DomainEventPublisherPort` → derived edges + usage frequency |
| **`plugin` (PF4J) / `marketplace`** | Extensibility | Connectors and domain types as installable plugins/modules |
| **`auth`** | Security and privacy | Access to the graph behind `LocalAuthFilter`; enterprise data isolation |
| **`common`** | Events, exceptions, analytics | `KnowledgeGraphUpdatedEvent`, typed exceptions, statistics |
| **Angular frontend** | Visualization and navigation | `knowledge` feature with interactive graph view, Signals, IT/EN i18n |
| **`finetuning`** | Extraction improvement | Dataset from the graph to fine-tune the local extraction models |

**Architectural wiring:** all the new domain services remain pure (zero Spring) and are registered as `@Bean` in `DomainConfig.java`; the adapters (MySQL, Qdrant, connectors) are `@Component`s implementing the `port/out`; the controllers expose only the `port/in`. This preserves the hexagonal architecture and enables the future extraction to a microservice documented in `MODULE_BOUNDARIES.md`.

---

*Direction document for the developments of the Knowledge Graph Engine (core, domain-agnostic). To be kept aligned with `.planning/PROJECT.md` and the files in `.planning/codebase/`.*
