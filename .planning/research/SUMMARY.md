# Project Research Summary

**Project:** LocalMind — Universal Knowledge Graph Engine
**Domain:** Weighted Knowledge Graph + GraphRAG on brownfield Spring Boot / Angular 21 app
**Researched:** 2026-06-29
**Confidence:** HIGH (stack npm-verified; architecture grounded in direct codebase inspection; pitfalls multiply-sourced)

---

## Executive Summary

LocalMind is evolving from a document management and semantic search platform into a universal knowledge graph engine. Experts build this class of product by layering a structural graph store (typed nodes + weighted directed edges) over a relational database, coupling it to a vector store for semantic node anchoring, and wiring both into a GraphRAG pipeline that combines graph traversal with LLM generation. The research confirms this is exactly the right pattern for LocalMind's existing stack: MySQL 8.0 adjacency list + WITH RECURSIVE CTEs for graph structure, Qdrant localmind_graph_nodes collection for node embeddings, Spring AI RetrievalAugmentationAdvisor with a custom KnowledgeGraphRetriever for GraphRAG, and Cytoscape.js 3.34.0 (direct ElementRef integration, no wrapper) for the Angular 21 visualization layer.

The dominant architectural insight is brownfield: the existing knowledge domain already contains KnowledgeEntity, KnowledgeRelation, KnowledgeGraphService, and the port interfaces. Everything must be extended inside that domain adding fields, new port interfaces, and a second service (GraphRagService) and never recreated in a parallel graph domain. Creating a new domain would introduce exactly the cross-domain import violations already flagged in MODULE_BOUNDARIES.md. The Flyway single-statement constraint (CLAUDE.md) means the schema foundation alone requires 8 migration files (V79 through V86) for additive columns and indexes on the existing knowledge_entities and knowledge_relations tables.

The three highest-risk failure modes are: MySQL recursive CTE cycles (infinite loops without explicit path-tracking), MySQL-Qdrant dual-store desync (no distributed transaction; requires the outbox pattern from day one), and entity extraction inflation with small Ollama models (hallucinated relation types contaminate the graph irreversibly). All three must be addressed in the Graph Core Engine phase before any GraphRAG or vertical work begins. Privacy controls (privacy_level on nodes, SecurityContext filtering in graph ports) must also be designed into the initial schema because retrofitting them after enterprise document ingestion is a GDPR risk.

---

## Key Findings

### Recommended Stack

The recommended stack adds minimal new dependencies to the existing application. On the backend, the only new Maven artifact is spring-ai-rag (no version needed, managed by the existing spring-ai-bom 1.0.0), which provides RetrievalAugmentationAdvisor, DocumentRetriever, and ContextualQueryAugmenter. Graph traversal uses MySQL 8.0 WITH RECURSIVE CTEs (zero new dependencies). On the frontend, Cytoscape.js 3.34.0 and two layout plugins (fcose 2.2.0, dagre 4.0.0) are the only additions; all npm-verified 2026-06-29. No Angular wrapper is used because all known wrappers are abandoned packages targeting Angular 5-13.

**Core technologies:**

- **MySQL 8.0 WITH RECURSIVE CTE** — graph traversal with cycle guard and weight filtering, zero new infrastructure
- **Adjacency list schema** (knowledge_entities + knowledge_relations, existing tables extended) — optimal for write-heavy ingestion; single-row weight updates
- **spring-ai-rag (BOM-managed 1.0.0)** — modular two-stage RAG pipeline; DocumentRetriever interface is the seam for the custom KnowledgeGraphRetriever
- **Custom KnowledgeGraphRetriever** — implements Spring AI DocumentRetriever; extracts node IDs from vector results, runs CTE, returns neighbor context as List<Document>
- **Cytoscape.js 3.34.0** (cytoscape-fcose for force-directed, cytoscape-dagre for hierarchy) — direct ElementRef integration in Angular 21 standalone components; edge weight as visual thickness via mapData(weight, 0, 1, 1, 8)
- **Qdrant localmind_graph_nodes collection (new, separate)** — node embeddings must not share the existing localmind document chunk collection; mixing corrupts GraphRAG anchor selection
- **Flyway V79-V86** — eight additive migrations on existing tables; one SQL statement per file (hard project constraint)

**Visualization library reconciliation — Cytoscape.js vs Sigma.js:**

FEATURES.md references Sigma.js in its MVP definition section while STACK.md (the dedicated technology evaluation with npm-verified versions) recommends Cytoscape.js 3.34.0. The recommendation is Cytoscape.js as the default because: (1) LocalMind's graph starts at hundreds to low thousands of nodes where Sigma.js WebGL overhead provides no benefit; (2) Sigma.js requires a separate graphology data model layer; (3) Cytoscape.js has built-in algorithmic support (Dijkstra, centrality) needed for GraphRAG path highlighting; (4) both lack maintained Angular bindings but Cytoscape's API is more complete for interactive graph exploration. Switch to Sigma.js 3.0.3 + graphology 0.26.0 only when a single graph view regularly exceeds 3,000-5,000 visible nodes simultaneously; the data model and API contracts remain identical and only the rendering component changes.

### Expected Features

**Must have — Graph Engine (table stakes, blocks everything else):**
- Typed node model with domainId and privacy_level — foundation for domain scoping and privacy enforcement
- Typed weighted edges (weight 0.0-1.0, domainId, confidenceScore, usageCount) — weightless edges prevent GraphRAG priority traversal
- CRUD API for nodes and edges (/api/v1/knowledge/ extensions and new /api/v1/graph-rag/)
- Graph traversal: BFS neighbors, N-hop subgraph via MySQL CTE, shortest weighted path in Java domain service
- Interactive graph visualization with Cytoscape.js: force-directed layout, edge thickness by weight, type icons, click-to-detail panel, pan/zoom
- Node/edge type filtering panel (domain selector, type checkboxes, min-weight slider)
- Document-to-node auto-conversion: existing document ingestion pipeline emits KnowledgeEntity alongside Qdrant chunk
- Domain schema module loader: PF4J GraphDomainSchemaExtension extension point; node/edge type strings validated against registered schemas at write time
- NodeTypeRegistry validation gate: entity extraction results validated before any write
- Outbox table (graph_sync_event) for MySQL-Qdrant consistency: part of initial schema

**Must have — GraphRAG layer (after graph is populated):**
- Two-stage retrieval: Qdrant semantic anchor then CTE subgraph expansion then merge then LLM generation
- Cited path responses: sourceNodeIds and pathsTaken in every GraphRAG answer
- Async weight reinforcement via domain event, never on the query critical path

**Should have — differentiators:**
- GraphRAG Q&A with cited paths (estimated 3.4x accuracy improvement vs flat vector RAG on multi-hop queries)
- Missing link suggestion (requires populated, multi-typed graph — defer to after vertical modules)
- Edge weight signals from usage, upvotes, co-occurrence
- Self-hosted / local-first with Ollama default (unique vs every SaaS competitor)
- Consumer: AI itinerary generation from graph traversal
- Enterprise: cross-domain relational queries in natural language

**Defer to v2+:**
- Missing link suggestion (requires populated multi-type graph)
- Git repository-to-graph ingestion (enterprise, v2)
- OpenAPI spec-to-node ingestion (enterprise, v2)
- Hierarchical community detection (Leiden — only valuable at >10K nodes)
- Full revision history / rollback (soft-delete is sufficient for v1)
- Real-time graph updates via WebSocket (not justified pre-v3)
- Community detection + LLM global summarization (validate with ground-truth QA set before building; research shows this underperforms hybrid retrieval)

**Explicitly out of scope:**
- Neo4j or dedicated graph database (this cycle)
- OWL/SPARQL reasoning engine
- Manual ontology editor UI
- Real-time multiplayer collaborative editing
- SaaS / paid hosting / open-core model

### Architecture Approach

The graph engine extends the existing knowledge bounded context. KnowledgeEntity and KnowledgeRelation gain new fields (weight, domainId, embeddingId, confidenceScore, usageCount, privacyLevel). GraphRagService is a new pure-Java domain service alongside the existing KnowledgeGraphService, sharing domain ports but never importing each other. The infrastructure layer gains four new adapters: GraphSemanticAdapter (Qdrant localmind_graph_nodes), UsageWeightCalculatorAdapter, Pf4jGraphDomainSchemaAdapter, and GraphRagLlmAdapter. JdbcKnowledgeGraphAdapter is extended with weight-column operations and a WITH RECURSIVE CTE replacing the current Java-side BFS.

**Major components:**

1. **KnowledgeEntity / KnowledgeRelation (domain models, extended)** — add weight, domainId, embeddingId, confidenceScore, usageCount, privacyLevel; new value objects (WeightFactors, GraphPath, GraphRagContext, GraphRagAnswer, DomainSchema) are pure Java records
2. **GraphRagService (new domain service, zero Spring)** — orchestrates 6-step pipeline: embed query, semantic anchor via Qdrant, CTE subgraph expansion via MySQL, optional document chunk enrichment, GraphRagContextBuilder serialization with token budgeting, LLM call, async weight reinforcement via domain event
3. **JdbcKnowledgeGraphAdapter (extended)** — replaces Java BFS with single WITH RECURSIVE CTE including path-tracking cycle guard; composite indexes on (source_id, weight) and (target_id, weight); SET cte_max_recursion_depth = 50 at session level
4. **GraphSemanticAdapter (new)** — manages localmind_graph_nodes Qdrant collection; stores embeddings after entity extraction; writes graph_sync_event outbox row in same MySQL transaction; domain-scoped similarity search
5. **Pf4jGraphDomainSchemaAdapter (new)** — discovers GraphDomainSchemaExtension plugins at startup; caches schemas; invalidates on plugin lifecycle events; vocabulary as VARCHAR strings in DB, not Java enums
6. **GraphRagContextBuilder (domain utility)** — token-budget aware: estimates context size (chars/4), prunes at 70% of configured num_ctx; logs WARN when pruning
7. **GraphWeightUpdateEventListener (infrastructure, @EventListener)** — async weight recalculation after GraphQueryCompletedEvent; nightly WeightDecayJob (Spring Batch) applies exponential decay
8. **graph.store.ts (Angular Signal store)** — immutable state (always set new array/object, never mutate); follows chat.store.ts pattern
9. **graph-canvas (Angular standalone component)** — Cytoscape.js via ElementRef; Angular effect() syncs Signal store to Cytoscape elements; ngOnDestroy calls cy.destroy(); max 200 nodes per server response
10. **Flyway V79-V86** — additive columns only; one SQL statement per file; naming convention for migration families documented before V79

### Critical Pitfalls

1. **MySQL CTE infinite loop without cycle guard** — every WITH RECURSIVE query must include path-tracking column and FIND_IN_SET check; SET cte_max_recursion_depth = 50 at session level; ArchUnit test asserting zero unbounded CTEs; must be addressed in Graph Core Engine phase
2. **MySQL-Qdrant dual-store desync** — write graph_sync_event in the same MySQL transaction as every node mutation; Spring Batch reconciliation job applies Qdrant operations asynchronously; outbox table must be in initial Flyway schema
3. **Entity extraction inflation with small Ollama models** — NodeTypeRegistry validation gate must reject extracted entities/relations with unregistered types before any write; use minimum llama3.1:8b with num_ctx = 8192; quarantine if a single pass produces more than 3 new relation types
4. **privacy_level must be day-one schema** — enterprise document ingestion extracts person names, emails, salary data, client IDs into graph nodes; SecurityContext filtering in KnowledgeGraphPort prevents unauthorized access; retrofitting this after data exists is a GDPR incident
5. **JPA lazy loading / N+1 on edge traversal** — never use JPA entity graph traversal for subgraph loading; single native CTE query in JdbcKnowledgeGraphAdapter; integration test must assert fewer than 3 SQL statements for 100-node subgraph; zero @Transactional or @Entity in localmind-domain
6. **domainId and edge weight must be in initial schema** — these fields cross-cut every downstream feature; adding them later requires risky ALTER migrations with data backfill

---

## Implications for Roadmap

The architecture research defines a 7-phase backend+frontend build order followed by two independent vertical modules. Dependencies are strict: each phase is a prerequisite for the next in its chain. Phases 3 and 4 are independent of each other and of Phase 2 (can be parallelized on a team). Consumer and Enterprise verticals are independent.

### Phase 1: Schema Foundation

**Rationale:** Flyway migrations are the hard prerequisite for every other phase. The outbox table, privacy_level, domainId, weight, embeddingId, confidence_score, and usage_count must all be in the initial schema. Establish the Flyway migration family naming convention before writing V79.

**Delivers:** Flyway V79-V86 (additive columns + indexes on knowledge_entities and knowledge_relations); graph_sync_event outbox table; extended JPA entities and repositories; domain model field additions to KnowledgeEntity and KnowledgeRelation

**Addresses:** Typed weighted edges with domainId and privacyLevel (table stakes); outbox table prerequisite for dual-store sync

**Avoids:** Dual-store desync (outbox present from day one); PII leakage (privacy_level in schema from day one); Flyway single-statement explosion (naming convention established first)

**Research flag:** Standard patterns (Flyway, JPA ALTER, MySQL) — skip research-phase.

---

### Phase 2: Weighted Graph Core

**Rationale:** Implement weighted graph operations and replace Java BFS with MySQL CTE. The cycle guard and N+1 prevention must be implemented here, not deferred.

**Delivers:** KnowledgeGraphPort and KnowledgeGraphUseCase extensions (updateWeight, findPath, listByDomain, getSubgraph); JdbcKnowledgeGraphAdapter with WITH RECURSIVE CTE including cycle guard; KnowledgeGraphService weight update and domain CRUD; REST extensions (PATCH /weight, GET /path, GET /domains); ArchUnit test (zero Spring imports in localmind-domain); integration test (fewer than 3 SQL statements for 100-node subgraph)

**Addresses:** Graph traversal queries and weighted edges API (table stakes P1)

**Avoids:** CTE infinite loop; JPA N+1; Spring annotations leaking into domain layer

**Research flag:** CTE cycle detection needs validation in planning (FIND_IN_SET vs JSON_CONTAINS performance comparison; composite index design for traversal queries).

---

### Phase 3: Graph Semantic Layer

**Rationale:** Node embeddings in a dedicated Qdrant collection enable the GraphRAG semantic anchor step. Independent of Phase 2 — can be built in parallel.

**Delivers:** GraphSemanticPort (new port/out); GraphSemanticAdapter (auto-creates localmind_graph_nodes collection; stores embeddings; writes graph_sync_event in same MySQL transaction; domain-scoped similarity search); Spring Batch GraphQdrantReconciliationJob; nightly consistency check

**Addresses:** Semantic search within graph (table stakes); GraphRAG Stage 1 anchor prerequisite

**Avoids:** Dual-store desync (reconciliation job); retrieval pollution from mixing document chunks and graph node vectors

**Research flag:** QdrantVectorStoreAdapter auto-create pattern already in codebase — skip research-phase.

---

### Phase 4: Plugin Domain Schemas (NodeTypeRegistry)

**Rationale:** Consumer and Enterprise verticals need different node/edge type vocabularies. The GraphDomainSchemaExtension PF4J extension point must exist before any vertical module is built. Independent of Phases 2 and 3 — can be built in parallel.

**Delivers:** GraphDomainSchemaExtension interface in localmind-plugin-api; GraphDomainSchemaPort (port/out); Pf4jGraphDomainSchemaAdapter; domain type validation gate in KnowledgeGraphService; node/edge types as VARCHAR strings validated against registered schemas (not Java enums); Consumer and Enterprise module JAR skeletons; separate Flyway SchemaHistory tables per module (flyway_schema_history_consumer, flyway_schema_history_enterprise)

**Addresses:** Domain schema module loader (table stakes P1); plugin uninstall/reinstall safety; entity extraction inflation prevention

**Avoids:** Enum pollution; Flyway history corruption from plugin migrations co-mingled with core; entity extraction inflation (validation gate blocks unregistered types)

**Research flag:** Flyway multi-instance configuration with separate SchemaHistory tables per plugin module is non-standard — needs research during planning.

---

### Phase 5: GraphRAG Service

**Rationale:** Depends on Phases 2 (weighted subgraph traversal) and 3 (semantic node anchoring). GraphRagService is a pure domain orchestrator — no Spring. Weight reinforcement is async. Token budgeting must exist before any user-facing endpoint is exposed.

**Delivers:** GraphRagUseCase (port/in); GraphRagLlmPort, GraphWeightCalculatorPort (ports/out); GraphRagLlmAdapter, UsageWeightCalculatorAdapter; GraphRagService (6-step pipeline); GraphRagContextBuilder (token-budget aware, prunes at 70% of num_ctx); GraphQueryCompletedEvent + async GraphWeightUpdateEventListener; WeightDecayJob (nightly exponential decay); GraphRagController (POST /api/v1/graph-rag/query, GET /api/v1/graph-rag/suggest/{nodeId}); answers include sourceNodeIds and pathsTaken; ground-truth QA set (20 factual questions) evaluated before declaring production-ready

**Addresses:** GraphRAG Q&A with cited paths (differentiator P2); relationship weight from signals; async weight reinforcement

**Avoids:** Ollama context window overflow (token budgeting); synchronous weight recalculation blocking API response; GraphRAG quality inflation via LLM-as-judge (ground-truth QA gate)

**Research flag:** GraphRagContextBuilder token estimation and pruning heuristics need research during planning. num_ctx per-request override propagation through LlmGatewayService needs evaluation.

---

### Phase 6: Frontend Graph Visualization

**Rationale:** Depends on Phase 2 REST API. Server-side pagination (max 200 nodes per response) must be implemented before the first render. Force simulation must terminate on convergence.

**Delivers:** npm install cytoscape@3.34.0 cytoscape-fcose@2.2.0 cytoscape-dagre@4.0.0; npm install -D @types/cytoscape@3.31.0; graph.store.ts (immutable Signal store, follows chat.store.ts pattern); graph-canvas component (Cytoscape.js, fcose default, dagre for dependency subgraphs, alphaDecay termination); graph-filter-panel; node-detail-panel; graph-page route; bilingual node type labels IT/EN via TranslatePipe; paginated subgraph endpoint GET /api/v1/graph/nodes?center={id}&depth=1&limit=50; layout position persistence for user-arranged nodes

**Addresses:** Interactive graph visualization (table stakes P1); pan/zoom/progressive expansion; edge weight visual encoding; i18n labels

**Avoids:** Browser freeze from full graph render (pagination + Cytoscape vs D3/SVG); force simulation running idle (alphaDecay); missing bilingual labels

**Research flag:** Layout position persistence storage strategy (localStorage vs server-side) needs decision during planning. Standard Cytoscape.js integration otherwise — skip research-phase.

---

### Phase 7: Frontend GraphRAG Chat

**Rationale:** Depends on Phase 5 REST API and Phase 6 Signal store. Integrates with existing SSE streaming. Submit must be disabled while query is in flight to prevent duplicate queries.

**Delivers:** graph-rag.service.ts; graphrag-chat component (SSE streaming, Path used collapsible panel showing sourceNodeIds + pathsTaken, submit disabled while in-flight); graphrag-page route; IT/EN i18n labels

**Addresses:** GraphRAG Q&A UI (differentiator); streaming response; missing typing indicator

**Avoids:** Duplicate queries from impatient retry; silent 10-second wait with no feedback

**Research flag:** SSE streaming pattern already established in existing chat feature — reuse directly, skip research-phase.

---

### Phase 8: Consumer Vertical Module

**Rationale:** First domain vertical. Requires Phases 1-7 complete. Trust-weighted voting must be in the vote schema from day one. Moderation queue must be live before community contribution is publicly enabled.

**Delivers:** Node types: PLACE, POI, RESTAURANT, EVENT, EXPERIENCE, ITINERARY; geolocation as JSON property on Place nodes; node_ratings + node_reviews tables (consumer Flyway migrations in separate schema history); photo upload per node (reuse existing DocumentIngestionUseCase); content moderation queue with PENDING_MODERATION state; voter_trust_score on vote schema; Bayesian average emergent ranking (scheduled batch job); AI itinerary generation (GraphRAG query with ITINERARY node type)

**Avoids:** Community ranking farming (trust-weighted voting from day one); spam accumulation (moderation gates public contribution)

**Research flag:** Bayesian Laplace smoothing formula and trust score decay model need research during planning.

---

### Phase 9: Enterprise Vertical Module

**Rationale:** Second domain vertical, independent of Phase 8. PII detection must run before any enterprise document ingestion produces graph nodes. SecurityContext filtering must be wired before enterprise data enters the graph.

**Delivers:** Node types: PROCESS, REPOSITORY, MICROSERVICE, API_ENDPOINT, DATABASE, TEAM, PERSON, DECISION, RUNBOOK; SecurityContext parameter in KnowledgeGraphPort traversal methods (filters by privacy_level and caller permissions); PII detection pass before Qdrant embedding (pii=true flag, excluded from GraphRAG context for non-privileged callers); Git connector; OpenAPI spec ingestion to MICROSERVICE + API_ENDPOINT nodes; dependency gap detection

**Avoids:** PII leakage via entity extraction (PII gate before Qdrant write); unauthorized cross-department node access (SecurityContext filter)

**Research flag:** PII detection approach for local-first constraint (regex vs opennlp NER vs small local Ollama model) needs research during planning. Git connector library choice (JGit vs CLI subprocess) needs evaluation.

---

### Phase Ordering Rationale

- Schema first (Phase 1): domainId, weight, privacy_level, outbox table, and embeddingId must exist before any code writes to them
- Weighted graph core before GraphRAG (Phase 2 before Phase 5): GraphRAG depends on weighted subgraph traversal; an unweighted graph returns meaningless results
- Semantic layer and plugin schemas in parallel (Phases 3 and 4): both share only the Phase 1 dependency and are otherwise independent; parallelizing reduces calendar time on a team
- GraphRAG service after both semantic and structural layers (Phase 5): it orchestrates both Qdrant semantic anchoring (Phase 3) and MySQL CTE expansion (Phase 2)
- Frontend after backend REST is stable (Phases 6-7 after Phases 2 and 5): components need real endpoints; the pagination API contract must be settled first
- Verticals last (Phases 8-9): both install domain vocabularies via PF4J (Phase 4), populate via ingestion (Phase 2), and surface via GraphRAG (Phase 5)
- Consumer vs Enterprise order is not mandated: the two verticals are independent; operator deployment target determines which comes first

### Research Flags

Phases needing deeper research during planning:

- **Phase 2 (Weighted Graph Core):** MySQL CTE cycle detection (FIND_IN_SET vs JSON_CONTAINS performance); cte_max_recursion_depth session-level vs global scope; composite index design for traversal queries
- **Phase 4 (Plugin Domain Schemas):** Flyway multi-instance configuration with per-module SchemaHistory tables; PF4J plugin lifecycle events for schema cache invalidation
- **Phase 5 (GraphRAG Service):** GraphRagContextBuilder token estimation and pruning heuristics; num_ctx per-request override propagation; ground-truth QA set construction methodology
- **Phase 8 (Consumer Vertical):** Bayesian Laplace smoothing formula; trust score decay model; velocity throttle for vote surge detection
- **Phase 9 (Enterprise Vertical):** PII detection approach for local-first constraint; Git connector library evaluation; SecurityContext propagation pattern through domain port interfaces

Phases with standard patterns (skip research-phase during planning):

- **Phase 1 (Schema Foundation):** Flyway migrations, JPA entity extension, MySQL ALTER — fully documented standard patterns
- **Phase 3 (Graph Semantic Layer):** QdrantVectorStoreAdapter auto-create pattern already in codebase; reuse directly
- **Phase 6 (Frontend Graph Visualization):** Cytoscape.js ElementRef integration is a 12-line pattern fully specified in STACK.md
- **Phase 7 (Frontend GraphRAG Chat):** SSE streaming already implemented in existing chat feature

---

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | All npm versions verified 2026-06-29; Spring AI RAG docs official; MySQL CTE patterns verified against MySQL 8.0 official docs and 2026 production benchmarks |
| Features | MEDIUM-HIGH | Core graph and GraphRAG features from multiple HIGH-confidence practitioner sources; missing-link UX patterns from single sources (deferred to v3+ accordingly) |
| Architecture | HIGH | Grounded in direct codebase inspection of knowledge domain, JdbcKnowledgeGraphAdapter, DomainConfig, QdrantVectorStoreAdapter; patterns cross-validated against hexagonal architecture standards and existing MODULE_BOUNDARIES.md |
| Pitfalls | HIGH | 14 pitfalls each with multiple verified sources; CTE cycle, dual-store desync, entity extraction inflation all from production incident reports and academic benchmarks |

**Overall confidence:** HIGH

### Gaps to Address

- **Closure table vs adjacency list at scale:** Start with adjacency list + CTE; add closure table if benchmark shows subgraph queries exceeding 200ms at 5K nodes. The initial Flyway schema must include full weight_factor columns so the closure table can be added without destructive migration if needed.
- **GraphRagContextBuilder pruning heuristics:** No official Spring AI guidance on token budget management. The 70% threshold and chars/4 token estimation are defaults requiring validation against actual Ollama model behavior during Phase 5 implementation.
- **PII detection for local-first deployments:** Cloud NER services violate local-first constraint. Decision between regex-only, opennlp models (Java-native), or small Ollama model for PII classification must be made before Phase 9 design begins.
- **Plugin Flyway isolation:** The separate SchemaHistory table approach is architecturally correct but non-standard. Exact Spring Boot configuration (FlywayConfigurationCustomizer per module) needs validation against existing auto-configuration setup before Phase 4 planning.
- **Ground-truth QA evaluation set:** No existing QA dataset in LocalMind. Must be constructed from real documents during Phase 5 validation. Minimum 20 factual questions with known answers required before declaring GraphRAG production-ready.

---

## Sources

### Primary (HIGH confidence)

- Cytoscape.js official site (js.cytoscape.org) — version 3.34.0, integration pattern, layout plugin APIs
- npm registry: cytoscape 3.34.0, cytoscape-fcose 2.2.0, cytoscape-dagre 4.0.0, sigma 3.0.3, vis-network 10.1.0 — verification 2026-06-29
- Spring AI RAG Reference Docs (docs.spring.io) — RetrievalAugmentationAdvisor, DocumentRetriever interface, ContextualQueryAugmenter
- JAX London 2026: Building a GraphRAG Application with Spring AI Advisors — two-stage retrieval pattern confirmation
- MySQL Official Blog: Recursive CTEs, Cycle Avoidance, Depth-First Traversal (dev.mysql.com) — path-tracking, cte_max_recursion_depth
- Maven Central: spring-ai-rag — BOM-managed artifact in spring-ai-bom 1.0.0 confirmed
- LocalMind codebase direct inspection: knowledge domain models and services, JdbcKnowledgeGraphAdapter, DomainConfig, QdrantVectorStoreAdapter, MODULE_BOUNDARIES.md, CONCERNS.md
- arXiv 2501.00309: GraphRAG Survey — architecture patterns and evaluation
- arXiv 2508.17222: Exposing Privacy Risks in Graph RAG — PII leakage via entity extraction, mitigation strategies

### Secondary (MEDIUM confidence)

- oneuptime.com (2026-03-31): MySQL recursive queries graph traversal — schema design, index strategy
- pkgpulse.com (2026): Cytoscape vs vis-network vs Sigma — comparative analysis corroborating Cytoscape recommendation
- Egnyte Engineering: Evaluating MySQL Recursive CTE at Scale — closure table vs CTE performance comparison
- arXiv 2605.20815: GraphRAG on Consumer Hardware (local LLMs) — Ollama context overflow patterns and num_ctx recommendations
- Ramu Ramaiah (Medium): Recursive CTE vs Closure Tables in MySQL — write-heavy graph tradeoff analysis
- Chi-Sheng Liu: GraphRAG Local Ollama Pitfalls Prevention Guide — extraction quality gates, context management
- arXiv 2603.14828: Mitigating KG Quality Issues in Multi-Hop GraphRAG Retrieval — hybrid retrieval validation
- Medium: GraphRAG Complete Guide — community detection evaluation methodology critique
- Cognee: Vectors and Graphs in Practice — dual store desync pitfall documentation
- arXiv 2512.09148: Detecting Hallucinations in GraphRAG via Attention Patterns

### Tertiary (LOW confidence)

- Single-source UX patterns for missing link suggestion (InfraNodus, Obsidian comparisons) — needs validation if building in v3+
- Microsoft GraphRAG community detection quality claims — contested by multiple arXiv papers; use hybrid retrieval (semantic + 1-hop) as primary path instead

---

*Research completed: 2026-06-29*
*Ready for roadmap: yes*
