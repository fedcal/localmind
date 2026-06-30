# Stack Research

**Domain:** Knowledge Graph extension — weighted graph on MySQL, GraphRAG with Spring AI, interactive graph visualization in Angular 21
**Researched:** 2026-06-29
**Confidence:** HIGH (all library versions npm-verified; Spring AI patterns verified against official docs and JAX London 2026 article; MySQL patterns verified against 2026 sources)

> This is a BROWNFIELD research file. It covers ONLY the new additions needed for the Knowledge Graph milestone.
> Existing stack (Spring Boot 3.4.2, Spring AI 1.0.0, MySQL 8.0, Qdrant, Angular 21, Flyway, PF4J) is documented in `.planning/codebase/STACK.md` and is not repeated here.

---

## Recommended Stack

### Area 1 — Graph Data Model on MySQL (No New Datastore)

**Pattern: Dual-table Adjacency List + MySQL 8.0 Recursive CTE traversal**

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| MySQL 8.0 `WITH RECURSIVE` CTE | 8.0 (existing) | N-hop graph traversal, BFS neighbor queries | Native MySQL 8.0 feature, zero new dependencies; handles weighted path accumulation; verified with 2026 sources |
| Adjacency list schema (`kg_nodes` + `kg_edges`) | n/a (schema design) | Typed nodes with JSON properties + weighted directed/undirected edges | Optimal for write-heavy ingestion pipelines (Spring Batch auto-ingest); weight updates are single-row UPDATEs; CTE queries are indexed via source/target FKs |
| Spring Data JPA (existing) | 3.4.2 (existing) | CRUD for nodes and edges | Already wired; no new ORM needed |
| Spring JDBC (existing, part of Spring Boot) | 3.4.2 (existing) | Executing recursive CTE queries that JPA cannot express | JPA's JPQL cannot write `WITH RECURSIVE` — native queries via `EntityManager.createNativeQuery()` or `JdbcTemplate` for traversal |

**Schema design decision — Adjacency List, not Closure Table:**

Closure table (storing all ancestor-descendant pairs) is fast for read-heavy, rarely-updated tree structures. LocalMind's graph is neither a pure tree nor static: Spring Batch constantly ingests documents/emails/repos, adding nodes and edges programmatically; edge weights update as users interact (feedback, usage frequency). Each node addition to a closure table requires inserting O(ancestors) rows. For a general graph (not a tree), closure tables are impractical. Adjacency list + recursive CTE is the correct choice.

**What the CTE handles:**
- N-hop neighbor discovery (BFS from a seed node)
- Accumulated-weight path traversal (weighted sum along edges)
- Cycle detection via visited-node path string (`FIND_IN_SET` guard)

**What stays in Java (domain service layer):**
- Dijkstra shortest path: implemented as a domain service using JDBC-fetched edge lists; keeps MySQL free from stored procedures and keeps business logic in the hexagonal domain layer
- PageRank / betweenness centrality for weight computation: runs as a Spring Batch job on periodic schedule; results written back to `kg_edges.weight`

**Flyway constraint:** One SQL statement per migration file (CLAUDE.md rule). The graph schema requires at minimum 6 migration files (V79–V84):
- V79: `CREATE TABLE kg_nodes`
- V80: `CREATE TABLE kg_edges`
- V81: `CREATE INDEX idx_kg_edges_source ON kg_edges(source_id)`
- V82: `CREATE INDEX idx_kg_edges_target ON kg_edges(target_id)`
- V83: `CREATE INDEX idx_kg_nodes_type ON kg_nodes(node_type, domain)`
- V84: `CREATE INDEX idx_kg_edges_relation ON kg_edges(relation_type, weight)`

**UUID mapping:** All `@Id` fields in `kg_nodes` and `kg_edges` JPA entities must use `@JdbcTypeCode(SqlTypes.CHAR)` to match `CHAR(36)` columns — existing project constraint.

---

### Area 2 — GraphRAG with Spring AI 1.0

**Pattern: `RetrievalAugmentationAdvisor` with custom `KnowledgeGraphRetriever`**

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| `spring-ai-rag` | 1.0.0 (managed via existing `spring-ai-bom`) | Modular RAG pipeline: pre-retrieval, retrieval, post-retrieval, generation | Already in BOM; no version conflict; provides `RetrievalAugmentationAdvisor`, `DocumentRetriever` interface, `ContextualQueryAugmenter` |
| `VectorStoreDocumentRetriever` (Spring AI built-in) | 1.0.0 | Stage 1: semantic similarity retrieval from Qdrant (existing) | Directly wraps the existing `QdrantVectorStoreAdapter`; returns `List<Document>` with node IDs |
| Custom `KnowledgeGraphRetriever` | project code | Stage 2: extract node IDs from vector results → CTE traversal → return neighbor context as Documents | Implements Spring AI's `DocumentRetriever` interface; lives in `localmind-infrastructure` as an adapter; injected into `RetrievalAugmentationAdvisor` |
| `ContextualQueryAugmenter` (Spring AI built-in) | 1.0.0 | Format combined context (semantic + graph path) into LLM prompt with citation | Handles prompt augmentation; graph paths appear as cited sources in responses |
| Custom `GraphQueryAugmenter` (alternative) | project code | Alternative: post-retrieval step that injects graph relationships into prompt as structured context | Use if graph context needs to be separate from document context in the prompt |

**Two-stage retrieval architecture:**

```
User Query
    │
    ▼
RetrievalAugmentationAdvisor
    │
    ├── Stage 1: VectorStoreDocumentRetriever (Qdrant)
    │   → top-K semantically similar nodes/documents
    │   → returns List<Document> with node_id metadata
    │
    ├── Stage 2: KnowledgeGraphRetriever (custom, MySQL CTE)
    │   → extracts node IDs from Stage 1 results
    │   → runs WITH RECURSIVE CTE to fetch N-hop neighbors + edge weights
    │   → returns neighbor nodes + relationship paths as List<Document>
    │
    └── ContextualQueryAugmenter
        → combines semantic + graph context into prompt
        → cites graph paths used in response
```

**Where it lives in the hexagonal architecture:**
- `GraphRagUseCase` (port/in) — domain interface for graph-augmented chat
- `KnowledgeGraphRetriever` — infrastructure adapter (`localmind-infrastructure/src/main/java/com/localmind/infrastructure/knowledge/adapter/`)
- `GraphRagService` (domain service) — orchestrates the two-stage retrieval via domain ports; wired in `DomainConfig.java`
- No Spring annotations in domain layer — consistent with existing pattern

**Maven addition (no version needed — managed by BOM):**
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-rag</artifactId>
</dependency>
```

**Why this approach and not alternatives:**
- Microsoft GraphRAG (Python library) — Python, not Java; not usable in this stack
- LangChain4j — would require replacing Spring AI 1.0; architectural breaking change
- Raw Neo4j Spring Data — explicit project constraint (no Neo4j this cycle)
- Spring AI's built-in Neo4j vector store — Neo4j out of scope; pattern ports cleanly to MySQL CTE via custom `DocumentRetriever`

---

### Area 3 — Interactive Graph Visualization in Angular 21

**Recommendation: Cytoscape.js 3.34.0 — direct integration via `ElementRef`, no wrapper**

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `cytoscape` | 3.34.0 (npm verified 2026-06-29) | Core graph rendering: nodes, edges, weights, interactive gestures | Always — the foundation |
| `@types/cytoscape` | 3.31.0 (npm verified) | TypeScript type definitions | Always alongside `cytoscape` |
| `cytoscape-fcose` | 2.2.0 (npm verified) | fCoSE force-directed layout — positions nodes by relationship strength | Default layout for knowledge graph exploration view; force-directed is the standard for general graphs |
| `cytoscape-dagre` | 4.0.0 (npm verified) | Hierarchical DAG layout | Use for dependency views, process flows, document hierarchies — when graph has a clear direction/levels |

**Angular 21 integration pattern (no wrapper library):**

All Angular wrappers for Cytoscape.js (`cytoscape-angular`, `ngx-cytoscape`, `cytoscape-ng`) are abandoned — last published 2–3 years ago with no Angular 17+ support. Direct integration with `ElementRef` is 12 lines of code and fully compatible with Angular 21 standalone components and signals:

```typescript
// graph-view.component.ts (standalone)
import cytoscape from 'cytoscape';
import fcose from 'cytoscape-fcose';
cytoscape.use(fcose);

export class GraphViewComponent implements AfterViewInit, OnDestroy {
  @ViewChild('graphContainer') containerRef!: ElementRef;
  private cy!: cytoscape.Core;

  ngAfterViewInit(): void {
    this.cy = cytoscape({
      container: this.containerRef.nativeElement,
      elements: this.graphSignal(),       // read from Signal store
      style: this.buildStylesheet(),      // weight → edge width mapping
      layout: { name: 'fcose' }
    });
  }

  ngOnDestroy(): void { this.cy.destroy(); }
}
```

Edge weight is expressed in the stylesheet via `width: mapData(weight, 0, 1, 1, 8)` — Cytoscape's built-in data-driven styling maps numeric weight to visual thickness.

**Why Cytoscape.js and not alternatives:**

| Alternative | Why Not for LocalMind |
|-------------|----------------------|
| **Sigma.js 3.0.3** | WebGL excels at 10,000+ nodes; LocalMind knowledge graph starts at hundreds to low thousands. No official Angular bindings. Requires separate `graphology` 0.26.0 data model layer (extra dependency). Algorithmic analysis (centrality, path highlighting) not built-in — must import `graphology-algorithms` separately. Sigma is the right choice when scale > 5,000 nodes becomes a bottleneck. |
| **vis-network 10.1.0** | Canvas-only rendering (no SVG/WebGL option). Physics simulation suitable for org charts, less natural for knowledge exploration. Bundle size larger (~2 MB). Graph algorithms not built-in. |
| **D3.js** | Too low-level for graph visualization; building interactive pan/zoom/select/layout from D3 primitives is months of work. Use D3 for custom charts, not graph networks. |
| **Angular wrappers** (`cytoscape-angular`, `ngx-cytoscape`) | Abandoned packages. `cytoscape-angular` last published 2023, no Angular 17+ support. Wrapping Cytoscape in Angular adds zero value — direct `ElementRef` integration is simpler and more maintainable. |

**When to switch to Sigma.js:** If the production graph grows beyond 3,000–5,000 nodes visible simultaneously, migrate the renderer to Sigma.js + graphology; the data model and API contracts stay the same (just swap the rendering layer in the Angular component).

---

## Full Installation Commands

### Frontend (Angular 21)

```bash
cd localmind-frontend

# Core graph visualization
npm install cytoscape@3.34.0

# Layouts
npm install cytoscape-fcose@2.2.0
npm install cytoscape-dagre@4.0.0

# TypeScript types (dev)
npm install -D @types/cytoscape@3.31.0
```

### Backend (Maven — add to `localmind-infrastructure/pom.xml`)

```xml
<!-- Managed by spring-ai-bom 1.0.0 — no version needed -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-rag</artifactId>
</dependency>
```

No other new Maven dependencies are required. MySQL recursive CTEs are SQL — they require no Java library. JPA native query or `JdbcTemplate` (already in Spring Boot) handles execution.

---

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|----------|-------------|-------------|---------|
| Graph store | MySQL adjacency list + CTE | Neo4j | Explicit project constraint: no new graph DB this cycle |
| Graph store | MySQL adjacency list + CTE | Closure table in MySQL | Write-heavy ingestion invalidates closure table's read advantage; dynamic edge weights require O(n) updates on every edge change |
| Graph store | MySQL adjacency list + CTE | FalkorDB / ArcadeDB | New infrastructure to run and operate; contradicts local-first self-hosting simplicity |
| GraphRAG orchestration | Spring AI `RetrievalAugmentationAdvisor` | Microsoft GraphRAG (Python) | Python library, incompatible with Java/Spring ecosystem |
| GraphRAG orchestration | Spring AI `RetrievalAugmentationAdvisor` | LangChain4j | Requires replacing Spring AI 1.0 — architectural breaking change |
| Frontend graph | Cytoscape.js 3.34.0 | Sigma.js 3.0.3 | Overkill for projected graph size; no Angular bindings; separate data model dependency |
| Frontend graph | Cytoscape.js 3.34.0 | vis-network 10.1.0 | Less algorithmic depth; canvas-only; heavier bundle |
| Frontend graph | Cytoscape.js 3.34.0 | D3.js | Too low-level; prohibitive build time for this use case |
| Frontend graph | Cytoscape.js 3.34.0 | Angular wrappers | Abandoned packages; direct `ElementRef` integration is cleaner |

---

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| `cytoscape-angular` npm package | Last published 2023; no Angular 17+ support; brings no value over direct integration | Direct `cytoscape` + `ElementRef` in `ngAfterViewInit()` |
| `ngx-cytoscape` npm package | Outdated, targets Angular 5; abandoned | Direct `cytoscape` + `ElementRef` |
| JGraphT (Java graph algorithms library) | Heavyweight dependency; MySQL CTE + Java JDBC handles the traversal LocalMind needs without importing a graph algorithms framework | MySQL `WITH RECURSIVE` CTE + custom `GraphTraversalService` domain service |
| Stored procedures in MySQL | Business logic in DB breaks hexagonal architecture; bypasses domain layer; hard to test | Keep Dijkstra / centrality in Java domain service; use JDBC for raw CTE execution |
| `spring-ai-neo4j-store` | Neo4j out of scope this cycle | Custom `KnowledgeGraphRetriever` implementing `DocumentRetriever` against MySQL |
| Sigma.js at current graph scale | WebGL overhead and separate data model layer (`graphology`) unnecessary until node count exceeds ~5,000 | Cytoscape.js 3.34.0 with fcose layout |

---

## Version Compatibility

| Package | Compatible With | Notes |
|---------|-----------------|-------|
| `cytoscape@3.34.0` | Angular 21, TypeScript 5.9 | No Angular-specific build config needed; tree-shakes cleanly with esbuild |
| `@types/cytoscape@3.31.0` | `cytoscape@3.34.0` | Community types; minor version gap (3.31 vs 3.34) is acceptable — core API stable |
| `cytoscape-fcose@2.2.0` | `cytoscape@3.x` | Requires `cytoscape.use(fcose)` before instantiation |
| `cytoscape-dagre@4.0.0` | `cytoscape@3.x` | Requires `cytoscape.use(dagre)` before instantiation |
| `spring-ai-rag` | `spring-ai-bom@1.0.0` | Managed by existing BOM; `RetrievalAugmentationAdvisor` stable since 1.0.0-M6 |
| MySQL `WITH RECURSIVE` | MySQL 8.0+ | Already satisfied by project MySQL 8.0 container |
| Flyway V79+ | Flyway existing (V1–V78) | Sequential; one SQL per file (CLAUDE.md rule enforced) |

---

## Stack Patterns by Variant

**If the graph is used for document/knowledge retrieval (GraphRAG):**
- Use `RetrievalAugmentationAdvisor` with both `VectorStoreDocumentRetriever` (Qdrant) and custom `KnowledgeGraphRetriever` (MySQL CTE)
- Register as a named advisor bean in `DomainConfig.java` for injection into `GraphRagService`
- Prompt augmentation: `ContextualQueryAugmenter` with a template that cites node IDs and relationship paths

**If the graph is displayed to users (frontend exploration):**
- Cytoscape.js with fcose layout for general graph exploration
- Switch to dagre layout for dependency/hierarchy subgraphs
- Use Cytoscape's built-in `cy.elements().dijkstra()` for client-side path highlighting (no extra library)
- Edge weight → visual thickness via `mapData(weight, 0, 1, 1, 10)` in stylesheet

**If the graph exceeds 5,000 nodes in a single view:**
- Replace Cytoscape renderer with Sigma.js 3.0.3 + graphology 0.26.0 in the Angular component
- The `GraphService` API and Signal store contract remain identical; only the rendering component changes
- This is a future decision; do not pre-optimize now

**If complex graph algorithms are needed (Dijkstra, PageRank, centrality):**
- Implement as domain services (`GraphAlgorithmService`) in `localmind-domain` — pure Java, no Spring
- Feed them adjacency data via `GraphEdgeRepository` (port/out) — fetched from MySQL via JDBC
- Run expensive computations (PageRank for weight assignment) in Spring Batch jobs on a schedule
- Do NOT add JGraphT — the implementation surface is small enough to do inline

---

## Sources

- [Cytoscape.js official site](https://js.cytoscape.org/) — version 3.34.0 confirmed; features and integration pattern (HIGH confidence)
- [npm: cytoscape](https://www.npmjs.com/package/cytoscape) — version 3.34.0 verified 2026-06-29
- [npm: cytoscape-fcose](https://www.npmjs.com/package/cytoscape-fcose) — version 2.2.0 verified
- [npm: cytoscape-dagre](https://www.npmjs.com/package/cytoscape-dagre) — version 4.0.0 verified
- [npm: sigma](https://www.npmjs.com/package/sigma) — version 3.0.3 verified
- [npm: vis-network](https://www.npmjs.com/package/vis-network) — version 10.1.0 verified
- [pkgpulse: Cytoscape vs vis-network vs Sigma 2026](https://www.pkgpulse.com/guides/cytoscape-vs-vis-network-vs-sigma-graph-visualization-2026) — comparison guide (MEDIUM confidence; corroborates Cytoscape recommendation)
- [Spring AI RAG Reference Docs](https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html) — `RetrievalAugmentationAdvisor`, `DocumentRetriever` interface, `ContextualQueryAugmenter` (HIGH confidence; official)
- [JAX London 2026: Building a GraphRAG Application with Spring AI Advisors](https://jaxlondon.com/blog/building-a-graphrag-application-with-spring-ai-advisors/) — two-stage retrieval pattern, `CallAdvisor` vs `DocumentRetriever` approaches (HIGH confidence; recent, official conference)
- [MySQL Recursive CTE Graph Traversal (oneuptime, 2026)](https://oneuptime.com/blog/post/2026-03-31-mysql-recursive-queries-graph-traversal/view) — schema design, CTE query patterns, cycle guard, index strategy (MEDIUM confidence; verified against MySQL 8.0 docs)
- [Recursive CTE vs Closure Tables in MySQL (Medium)](https://medium.com/@ramu.ramaiah/recursive-cte-vs-closure-tables-in-mysql-choosing-the-right-strategy-for-hierarchical-data-c1c89ebd264f) — adjacency list vs closure table tradeoffs (MEDIUM confidence)
- [Maven Central: spring-ai-rag](https://central.sonatype.com/artifact/org.springframework.ai/spring-ai-rag) — artifact confirmed in BOM (HIGH confidence)

---

*Stack research for: LocalMind Knowledge Graph milestone (brownfield extension)*
*Researched: 2026-06-29*
