# Feature Research

**Domain:** Universal Knowledge Graph Platform — Consumer (territory/tourism/community) + Enterprise (internal knowledge/docs/repos/APIs)
**Researched:** 2026-06-29
**Confidence:** MEDIUM-HIGH (graph visualization and moderation from HIGH-confidence sources; GraphRAG implementation details MEDIUM; missing-link UX patterns LOW from single sources)

---

## Context: Brownfield Extension

This research covers only the NEW graph-centric features. The following capabilities already exist and are excluded:

- Multi-provider LLM chat with fallback chain and streaming
- Document ingestion + chunking + Qdrant semantic search
- Auth, MCP, plugin system (PF4J), marketplace
- Email/calendar ingestion domains
- Angular 21 UI with Signals, i18n IT/EN
- Hexagonal architecture (Spring Boot 3.4.2 + Java 17 + Maven multi-module)

The constraint is: MySQL 8.0 + Qdrant as the only datastores (no Neo4j), extending the existing domain `knowledge`.

---

## Feature Landscape

### Table Stakes — Shared Core (Users Expect These)

Features where absence makes the product feel broken or incomplete regardless of vertical.

| Feature | Why Expected | Complexity | Domain |
|---------|--------------|------------|--------|
| Typed node model with properties | Any graph product must let users define what a node *is* (Place, Document, Service, Person). Without types, the graph is a flat blob | HIGH | Shared |
| Typed weighted edges (relationships) | Graph edges must have a type ("depends_on", "located_in", "authored_by") and a numeric weight (strength/frequency). Weightless edges prevent GraphRAG ranking | HIGH | Shared |
| CRUD API for nodes and relationships | Create/read/update/delete nodes and edges — the absolute minimum for any data management feature | MEDIUM | Shared |
| Graph traversal queries | Neighbors, shortest path, subgraph extraction by relation type. Without this, the graph is just a relational table | MEDIUM | Shared |
| Node/edge type filtering | Users cannot work with >50 nodes without filtering by type, domain, or relationship kind | LOW | Shared |
| Full-text + semantic search within graph | Users must be able to find nodes by name, content, or semantic similarity. Leverages existing Qdrant | LOW | Shared |
| Interactive graph visualization | The defining UX of a graph product. Without a canvas where users see nodes and edges, the product is indistinguishable from a wiki or a table | HIGH | Shared |
| Pan, zoom, progressive node expansion | Users expand from a focal node outward — collapsing subgraphs they don't need. Standard graph UX since Gephi era | MEDIUM | Shared |
| Domain schema module loader | Consumer and enterprise need different node/edge types. A plugin-aware schema loader (extending existing PF4J) lets each domain define its own types without touching the core | MEDIUM | Shared |
| POI/Place/Event/Experience node types | Consumer vertical: users come expecting to find places and events, not generic "nodes" | LOW | Consumer |
| Star rating + review text submission | Minimum viable community contribution for consumer. TripAdvisor/Google Maps set this expectation universally | LOW | Consumer |
| Photo upload per node | Consumers expect visual content on place/event pages. Leverage existing document upload pipeline | LOW | Consumer |
| Content moderation queue | Without moderation, a community-contribution system accumulates spam within hours of launch. Needed before any public contribution is enabled | MEDIUM | Consumer |
| Document → node auto-conversion | Existing ingested documents must create knowledge graph nodes, not just vector embeddings. Users expect their docs to appear in the graph | MEDIUM | Enterprise |
| Privacy controls (local-only flag) | Enterprise will not deploy a knowledge graph that might send internal data to cloud LLMs without explicit consent. Hard requirement before any enterprise adoption | MEDIUM | Enterprise |
| Person/Team node types | Enterprise knowledge graphs without people are useless. "Who owns this service?" and "Who wrote this doc?" are the most common queries | LOW | Enterprise |

### Table Stakes — Graph Visualization Specifics

Based on research into Cytoscape.js, Sigma.js, and established graph UX patterns:

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Force-directed layout | Default layout users expect from graph tools (Obsidian, Gephi, network viz). Nodes cluster by connectivity | MEDIUM | Sigma.js or Cytoscape.js |
| Hierarchical layout | Needed for tree-like enterprise structures (org charts, service dependencies) | MEDIUM | Alternative layout |
| Edge weight visual encoding | Heavier/more relevant relationships must appear visually distinct (stroke width, opacity, color) | LOW | CSS + layout weight |
| Node color/icon by type | Users must distinguish Place from Document from Person at a glance | LOW | Per-schema type icon |
| Tooltip on hover | Node summary, key properties, relationship count on hover | LOW | Standard UX |
| Click to navigate / open detail | Clicking a node opens a side panel or navigates to the node detail page | LOW | Standard UX |

---

### Differentiators (Competitive Advantage)

Features that set LocalMind apart from Google Maps/TripAdvisor (consumer side) and Confluence/Notion (enterprise side).

| Feature | Value Proposition | Complexity | Domain |
|---------|-------------------|------------|--------|
| GraphRAG Q&A with cited paths | Users ask "Why does service X depend on database Y?" or "What are the best hidden restaurants near the medieval quarter?" — LLM navigates the graph to answer, citing the traversal path. Neither Confluence nor TripAdvisor offer this. Estimated 3.4x accuracy vs flat vector RAG on multi-hop queries | HIGH | Shared |
| Missing link suggestion | AI proactively surfaces "these two nodes might be related but are not connected". Comparable to Obsidian+InfraNodus gap detection but integrated and graph-native. Reduces blind spots in both knowledge bases and community maps | HIGH | Shared |
| Relationship weight from signals | Edge weights evolve from usage frequency, user upvotes, citation counts, co-occurrence — emergent graph intelligence that gets smarter over time without manual curation | HIGH | Shared |
| Fully self-hosted / local-first | Unique differentiator vs every SaaS competitor. No data leaves the machine unless explicitly configured. Critical for enterprise and privacy-conscious community operators | LOW | Shared |
| AI-generated itineraries from graph traversal | Users ask "build me a 3-hour itinerary in the old town avoiding touristy spots" — AI traverses the consumer graph (POIs, events, ratings, relationships) to generate a personalized route. No competitor offers this on a community-contributed open graph | HIGH | Consumer |
| Emergent content ranking (trust-weighted) | Community-contributed nodes rank by a Bayesian average (not simple mean) weighted by contributor trust score — surfaces quality content, suppresses gaming. More sophisticated than Google Maps star average or TripAdvisor bubble rating | MEDIUM | Consumer |
| Contributor trust score | Wikipedia-style reputation: contributors earn trust by having edits accepted and upvoted. Trust multiplies their rating weight. Reduces spam impact without requiring extensive human moderation | MEDIUM | Consumer |
| Cross-domain relational queries | "Show me all processes that reference the payment-service API and whose owning team has no documented oncall runbook" — impossible in Confluence or Notion. GraphRAG makes this a natural language query | HIGH | Enterprise |
| Automatic Git repo → graph ingestion | Files, functions, PRs, commits, contributors as nodes; function-call, file-modification, commit-to-PR as edges. Automatically maps codebase knowledge without documentation effort | HIGH | Enterprise |
| API spec → node ingestion (OpenAPI) | Parse Swagger/OpenAPI specs to create Microservice, Endpoint, Schema nodes with typed relationships. No manual entry. Confluence requires manual documentation | MEDIUM | Enterprise |
| Dependency mapping + gap detection | Auto-detect orphan services, undocumented APIs, processes with no assigned owner — surfaces structural ignorance in the enterprise knowledge graph | HIGH | Enterprise |

---

### Anti-Features (Deliberately NOT Build)

| Anti-Feature | Why Requested | Why Problematic | Alternative |
|---|---|---|---|
| Dedicated graph database (Neo4j/ArangoDB) | "Graph databases are made for this" | Adds a third infrastructure dependency. MySQL with adjacency list + recursive CTEs handles graphs of < 1M nodes. Already explicitly excluded in PROJECT.md | MySQL recursive CTEs for traversal + Qdrant for semantic proximity |
| OWL/SPARQL reasoning engine | "Semantic Web standards" | Inference overhead is prohibitive on commodity hardware. Schema rigidity kills extensibility. No practical user in this domain uses SPARQL | LLM-based relationship inference at query time (GraphRAG) |
| Manual ontology editor UI (Protégé-style) | "Users should define schemas visually" | Creates ontology hell — schema proliferation without governance. Domain schemas should be code-first, versioned, shipped as module JARs via PF4J | Code-first schema definition in domain module JARs, loaded at startup |
| Full Wikipedia-style revision history (diff/rollback) | "Community contributions need audit trail" | 10x implementation complexity for v1. Most users never use edit history | Soft-delete with "last modified by" metadata; full versioning deferred to v2+ |
| Real-time multiplayer collaborative editing | "Teams need to co-edit the graph" | Requires CRDT or OT algorithms, WebSocket infrastructure, conflict resolution UI — enormous scope for marginal v1 value | Optimistic concurrency (last-write-wins with conflict notification) for v1 |
| Geographic tile map rendering | "Consumer apps need a map view" | Building competitive geo-rendering is a decade of Google Maps R&D. Replacing it is not feasible | Embed Leaflet.js + OpenStreetMap tiles for geo context only; graph is the primary interface |
| Social network features (follow/feed/DMs) | "Community needs engagement" | Scope creep. Turns the knowledge graph into a social platform — different product with different moderation burden | Upvote/downvote on nodes and reviews; contributor profiles are enough for v1 |
| Gamification beyond minimal badges | "Points/leaderboards drive contribution" | Gamification creates perverse incentives — users optimize for points, not quality. Known failure mode in crowdsourced platforms | Contributor trust score as quality signal; badges are display-only cosmetic |
| Paid SaaS hosting / open-core | "Monetization" | Explicitly excluded in PROJECT.md. Destroys community trust | Open-source only; operators self-host |
| Native mobile apps | "Users want mobile" | Explicitly excluded in PROJECT.md until web is mature | PWA-compatible responsive Angular UI for mobile browsers |
| Full-text search replacing Qdrant | "Graph should have its own search" | Duplicates what Qdrant already does well. Re-implementing semantic search is months of work | Reuse existing QdrantVectorStoreAdapter via a GraphNodeSearchPort |

---

## Feature Dependencies

```
[Graph Data Model: Typed Nodes + Weighted Edges]
    ├──requires──> [Domain Schema Module Loader]
    ├──enables──>  [CRUD API for Nodes/Edges]
    │                   └──enables──> [Graph Traversal Queries]
    │                                     └──enables──> [Interactive Visualization]
    │                                     └──enables──> [GraphRAG Q&A]
    │                                                       └──enables──> [AI Itinerary Generation] (consumer)
    │                                                       └──enables──> [Cross-Domain Relational Queries] (enterprise)
    │
    ├──requires for POPULATION──> [Document → Node Auto-Conversion] (enterprise)
    │                              └──requires──> [Existing Document Ingestion Pipeline]
    │
    ├──requires for POPULATION──> [Git Repo → Graph Ingestion] (enterprise)
    ├──requires for POPULATION──> [API Spec → Node Ingestion] (enterprise)
    ├──requires for POPULATION──> [Community Contribution] (consumer)
    │                              └──requires──> [Content Moderation Queue]
    │                                                 └──enables──> [Emergent Ranking]
    │                                                 └──enables──> [Contributor Trust Score]
    │
    └──after sufficient population──> [Missing Link Suggestion]
                                      └──requires──> [LLM (existing)]
                                      └──requires──> [Graph Traversal Queries]

[Relationship Weight from Signals]
    └──requires──> [CRUD API] + [Rating/Upvote signals] + [Usage telemetry]
    └──enhances──> [GraphRAG Q&A] (higher-weight edges prioritized in traversal)
    └──enhances──> [Emergent Ranking] (consumer)

[Privacy Controls]
    └──cross-cuts──> [Document → Node Auto-Conversion]
    └──cross-cuts──> [GraphRAG Q&A] (must not call cloud LLM with private nodes)
    └──cross-cuts──> [Git Repo → Graph Ingestion]

[Existing Qdrant Semantic Search]
    └──reused-by──> [Graph Node Search]
    └──reused-by──> [GraphRAG Q&A] (hybrid: graph traversal + vector similarity)
    └──reused-by──> [Missing Link Suggestion] (vector proximity as candidate links)

[Existing LLM Gateway (Ollama/cloud fallback)]
    └──reused-by──> [GraphRAG Q&A]
    └──reused-by──> [Missing Link Suggestion]
    └──reused-by──> [AI Itinerary Generation]
    └──reused-by──> [Entity Extraction for graph construction]
```

### Critical Dependency Notes

- **Graph Data Model blocks everything else:** Schema tables (graph_nodes, graph_edges, node_types, edge_types) must be the first Flyway migrations. No other graph feature can be built until this exists.
- **Graph population must precede GraphRAG:** A GraphRAG engine on an empty graph returns nothing useful. Document → node conversion must run before GraphRAG Q&A is exposed to users.
- **Missing Link Suggestion requires a populated, multi-typed graph:** Suggesting links between two documents is trivial; the feature becomes valuable only once consumer POIs + ratings + events or enterprise services + APIs + docs coexist in the same graph. Defer to after vertical modules are built.
- **Privacy Controls cross-cut everything:** Must be designed into the graph data model (a `visibility` or `privacy_level` field on nodes) from day one, not retrofitted.
- **Emergent Ranking depends on Moderation:** Publishing a ranking system before moderation is live invites spam optimization. Moderation must gate community contribution.
- **Consumer Photo Upload reuses existing Document Upload:** The `DocumentIngestionUseCase` and its file storage adapter can serve photo uploads with minor extension. Not a net-new system.

---

## MVP Definition

### Launch With — Graph Engine (v1)

Minimum to validate the graph-as-a-product concept and unblock both verticals.

- [ ] Flyway migrations: `graph_nodes`, `graph_edges`, `node_types`, `edge_types` tables — foundation for everything
- [ ] CRUD API: `/api/v1/graph/nodes` and `/api/v1/graph/edges` — create, read, update, delete
- [ ] Domain schema module loader — JSON-schema or SPI-based node/edge type definitions loaded from PF4J modules at startup
- [ ] Graph traversal: BFS neighbors, shortest path (implemented as Java-side traversal over MySQL adjacency list), subgraph by type
- [ ] Minimal graph visualization — Sigma.js force-directed canvas, 20-200 nodes, pan/zoom, node type icons, click-to-detail side panel
- [ ] Node/edge type filter panel in the Angular graph feature module
- [ ] Graph node search — reuse `QdrantVectorStoreAdapter` for semantic + keyword node search
- [ ] Document → Node auto-conversion — extend existing `DocumentIngestionPipelineService` to create a `graph_node` row alongside the Qdrant vector
- [ ] Privacy controls: `privacy_level` enum field on `graph_nodes` (LOCAL / SHARED / PUBLIC); guards in `GraphRAGService` to exclude PRIVATE nodes from cloud LLM calls

### Add After Validation — GraphRAG Layer (v1.5)

Once the graph is populated with enough nodes to be meaningful.

- [ ] GraphRAG Q&A — extend existing `ChatUseCase` with a `GraphContextEnhancerPort` that retrieves relevant subgraph context before calling the LLM
- [ ] Cited path responses — Q&A answers include a `usedNodes[]` and `traversedEdges[]` array for transparency
- [ ] Entity extraction pipeline — LLM-driven extraction of entities and relationships from existing documents to enrich the graph (runs as a Spring Batch job)
- [ ] Relationship weight signals — increment edge weight on usage, citation, upvote; expose weight in GraphRAG traversal priority

### First Vertical Module — Consumer or Enterprise (v2)

Build whichever vertical the operator installs. They should not be built simultaneously.

**Consumer module:**
- [ ] Node types: Place, POI, Restaurant, Event, Experience, Itinerary
- [ ] Geolocation field on Place/POI nodes (lat/lon, stored in graph_nodes as a JSON property)
- [ ] Star rating + review submission (new `node_ratings` and `node_reviews` tables)
- [ ] Photo upload per node (reuse document upload, tag to node)
- [ ] Content moderation queue with auto-flag rules (profanity filter, spam pattern detection)
- [ ] Emergent ranking (Bayesian average, computed via scheduled job)
- [ ] Contributor trust score (basic: accepted edits / total edits ratio)

**Enterprise module:**
- [ ] Node types: Process, GitRepository, Microservice, ApiEndpoint, Database, Team, Decision, Runbook
- [ ] Git connector: parse repository metadata → GitRepository + file structure nodes (Tree-sitter for function-level nodes is v3+)
- [ ] OpenAPI spec ingestion → Microservice + ApiEndpoint + Schema nodes
- [ ] Dependency mapping: auto-detect edges from code imports, API calls (static analysis), doc references

### Future Consideration (v3+)

Defer until both core graph and at least one vertical have been validated in production.

- [ ] Missing link suggestion — requires populated multi-type graph to be meaningful
- [ ] AI itinerary generation (consumer) — requires GraphRAG + populated consumer graph
- [ ] Cross-domain relational queries (enterprise) — requires enterprise schema + GraphRAG maturity
- [ ] Hierarchical community detection (Leiden algorithm) — for large graphs (>10K nodes) only
- [ ] Full-text revision history / rollback — defer; soft-delete suffices for v1-v2
- [ ] Real-time graph updates (WebSocket push when a node changes) — complexity not justified pre-v3
- [ ] Tree-sitter function-level code graph (files as nodes in v2; functions in v3)

---

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Typed node + edge data model (Flyway schema) | HIGH | MEDIUM | P1 |
| CRUD API for nodes/edges | HIGH | MEDIUM | P1 |
| Domain schema module loader | HIGH | MEDIUM | P1 |
| Graph traversal (BFS, shortest path) | HIGH | MEDIUM | P1 |
| Graph visualization (Sigma.js, force-directed) | HIGH | HIGH | P1 |
| Document → node auto-conversion | HIGH | LOW | P1 |
| Privacy controls (visibility field + guards) | HIGH | MEDIUM | P1 |
| Node/edge type filtering | MEDIUM | LOW | P1 |
| Graph node semantic search | MEDIUM | LOW | P1 |
| GraphRAG Q&A with cited paths | HIGH | HIGH | P2 |
| Entity extraction pipeline (Batch job) | HIGH | HIGH | P2 |
| Relationship weight signals | MEDIUM | HIGH | P2 |
| Consumer: POI/Place/Event node types | HIGH | LOW | P2 |
| Consumer: Star rating + review submission | HIGH | LOW | P2 |
| Consumer: Content moderation queue | HIGH | MEDIUM | P2 |
| Consumer: Emergent ranking (Bayesian) | MEDIUM | MEDIUM | P2 |
| Enterprise: Git repo → graph ingestion | HIGH | HIGH | P2 |
| Enterprise: OpenAPI spec → nodes | MEDIUM | MEDIUM | P2 |
| Enterprise: Dependency gap detection | HIGH | HIGH | P2 |
| Contributor trust score | MEDIUM | MEDIUM | P3 |
| Missing link suggestion | HIGH | HIGH | P3 |
| AI itinerary generation (consumer) | HIGH | HIGH | P3 |
| Cross-domain relational queries (enterprise) | HIGH | HIGH | P3 |
| Hierarchical community detection (Leiden) | LOW | HIGH | P3 |
| Revision history / rollback | LOW | HIGH | P3 |

**Priority key:**
- P1: Must have for graph engine to be usable (Phase 1)
- P2: Required for first vertical to be usable (Phase 2-3)
- P3: Meaningful differentiation, requires P1+P2 foundations to be valuable (Phase 4+)

---

## Competitor Feature Analysis

### Consumer Side: LocalMind vs Google Maps vs TripAdvisor

| Feature | Google Maps | TripAdvisor | LocalMind Approach |
|---------|-------------|-------------|-------------------|
| POI database | Closed, proprietary, global | Closed, travel-focused | Open, community-contributed, local-first |
| Community ratings | 1-5 stars, text, photos | 1-5 bubbles, text, photos | Same basics + Bayesian ranking + trust-weighted |
| AI Q&A on places | Gemini integration (cloud) | None significant | GraphRAG on local graph, Ollama default |
| Relationship awareness | None ("nearby places") | None | Graph-native: "Places that share a neighborhood AND have events this weekend AND are walkable from each other" |
| Self-hostable | No | No | Yes — unique differentiator for municipalities, tourism boards, regional operators |
| Itinerary generation | Basic route planning | Curated editorial lists | AI-traversed community graph → personalized itinerary |
| Graph exploration | None | None | Visual graph canvas unique to LocalMind |
| Moderation | Automated + Google staff | 300+ staff + algorithms | Auto-flag + community moderators + contributor trust score |

### Enterprise Side: LocalMind vs Confluence vs Notion

| Feature | Confluence | Notion | LocalMind Approach |
|---------|------------|--------|-------------------|
| Documentation structure | Hierarchical pages + spaces | Databases + pages | Graph of typed nodes — no artificial hierarchy |
| Relationship navigation | Jira links (only) | Relation fields (manual) | Typed weighted edges with traversal queries |
| AI Q&A on internal knowledge | Rovo AI (paid, cloud) | AI features (cloud) | GraphRAG with Ollama (local), no data egress |
| Code / repo integration | None native | None | Git connector → code nodes (enterprise module) |
| Missing link detection | None | None | AI-powered missing link suggestion |
| Dependency mapping | None | None | Auto-detected from code, API specs, doc references |
| Self-hostable | Data Center (expensive) | No | Yes — free, open source |
| Search accuracy | OK for text, poor for relations | OK for text, poor for relations | Hybrid: semantic (Qdrant) + graph traversal |

---

## Sources

- [Graph RAG in 2026: A Practitioner's Guide — Medium / Graph Praxis](https://medium.com/graph-praxis/graph-rag-in-2026-a-practitioners-guide-to-what-actually-works-dca4962e7517)
- [GraphRAG Implementation Guide: Entity Extraction, Query Routing 2026 — premai.io](https://blog.premai.io/graphrag-implementation-guide-entity-extraction-query-routing-when-it-beats-vector-rag-2026/)
- [Graph RAG Guide 2025: Architecture, Implementation & ROI — Salfati Group](https://salfati.group/topics/graph-rag)
- [How Microsoft GraphRAG Works Step-By-Step — Bertelsmann Tech](https://tech.bertelsmann.com/en/blog/articles/how-microsoft-graphrag-works-step-by-step-part-12)
- [Knowledge Graphs for Enterprise AI — gend.co](https://www.gend.co/blog/knowledge-graphs-enterprise-ai)
- [Cytoscape.js vs vis-network vs Sigma.js 2026 — PkgPulse](https://www.pkgpulse.com/blog/cytoscape-vs-vis-network-vs-sigma-graph-visualization-javascript-2026)
- [Top 13 JavaScript Graph Visualization Libraries — Linkurious](https://linkurious.com/blog/top-javascript-graph-libraries/)
- [Guide to Creating Knowledge Graph Visualizations — yFiles](https://www.yfiles.com/resources/how-to/guide-to-visualizing-knowledge-graphs)
- [InfraNodus Obsidian Plugin: AI Enhanced Knowledge Graph View](https://infranodus.com/obsidian-plugin)
- [7 Best Obsidian Alternatives with Powerful Graph Features 2025 — Scrintal](https://scrintal.com/comparisons/obsidian-alternatives-with-robust-graph-features)
- [Obsidian vs Roam Research: Knowledge Management Comparison — DeepTerm](https://deepterm.app/blog/obsidian-vs-roam-research-knowledge-management-comparison)
- [Confluence vs Notion 2026 — docsie.io](https://www.docsie.io/blog/articles/confluence-vs-notion-comparison-2026/)
- [The 5 Biggest Knowledge Management Challenges in Confluence — Atlassian Community](https://community.atlassian.com/forums/App-Central-articles/The-5-Biggest-Knowledge-Management-Challenges-in-Confluence-and/ba-p/3159601)
- [Google Maps vs TripAdvisor Comparison 2026 — Slant](https://www.slant.co/versus/6934/14774/~google-maps_vs_tripadvisor)
- [Deep Learning of Dynamic POI Generation for Itinerary Recommendation — ACM](https://dl.acm.org/doi/10.1145/3713079)
- [Establishing Trust in Crowdsourced Data — arxiv 2511.03016](https://arxiv.org/pdf/2511.03016)
- [From Wikidata to Smart Tourism: AI Pipeline for POI Classification — MDPI](https://www.mdpi.com/2227-7390/14/12/2227)
- [Knowledge Graph Software and Research Mapping Tools 2026 — Atlas Workspace](https://www.atlasworkspace.ai/blog/knowledge-graph-tools)
- [Enterprise Knowledge Graph Architecture — Superblocks](https://www.superblocks.com/blog/enterprise-knowledge-graph)
- [LLM-Empowered Knowledge Graph Construction: A Survey — arxiv 2510.20345](https://arxiv.org/html/2510.20345v1)
- [Build Knowledge Graphs with LLM-Driven Entity Extraction — neuml](https://neuml.hashnode.dev/build-knowledge-graphs-with-llm-driven-entity-extraction)
- [How to Optimize Enterprise Knowledge Graphs — freeCodeCamp](https://www.freecodecamp.org/news/how-to-optimize-enterprise-knowledge-graphs-for-scalable-digital-product-platforms/)
- [Graphiti: Build Real-Time Knowledge Graphs for AI Agents — GitHub](https://github.com/getzep/graphiti)

---

*Feature research for: LocalMind Universal Knowledge Graph Platform*
*Researched: 2026-06-29*
