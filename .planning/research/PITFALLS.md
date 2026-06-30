# Pitfalls Research

**Domain:** Knowledge Graph Engine + GraphRAG + Dual-Domain Platform (MySQL + Qdrant, Spring Boot Hexagonal, Angular 21)
**Researched:** 2026-06-29
**Confidence:** HIGH (multiple verified sources per pitfall)

---

## Critical Pitfalls

### Pitfall 1: MySQL Recursive CTE Cycle → Infinite Loop / Server Lock

**What goes wrong:**
Graph data is cyclic by design (document A cites document B which references A; two people co-authored three papers together). Without explicit cycle detection in each recursive CTE, a traversal query enters an infinite loop. MySQL's default `cte_max_recursion_depth` is 1000, so the server accumulates 1000 recursive rows before erroring — this blocks the connection and can cause cascading load under concurrent traffic. MySQL does NOT have a `CYCLE` clause (unlike PostgreSQL 14+), so protection must be written manually every time.

**Why it happens:**
Developers test on tree-shaped data (documents in folders, hierarchical categories) where cycles are impossible. The graph schema is then extended to allow many-to-many relationships, and the traversal queries are never updated with cycle guards.

**How to avoid:**
Every recursive CTE on the graph must include a path-tracking column: accumulate visited node IDs as a JSON array in the recursive step, and break when `JSON_CONTAINS(path, CAST(node_id AS JSON))` is true. Enforce this with an architecture rule: the `GraphTraversalRepository` port/out interface must reject any traversal query builder that does not declare a max_depth and cycle guard. Set `cte_max_recursion_depth = 50` at session level before any traversal (not globally, to avoid breaking other app queries).

**Warning signs:**
- Traversal queries that return the same node IDs multiple times in a result set
- INFORMATION_SCHEMA shows queries with `cte_recursion_count` approaching default limit in slow query log
- Any new graph edge added between nodes that are already connected (even indirectly) breaks a query

**Phase to address:**
Graph Core Engine phase (the very first schema definition). Retrofitting cycle guards into existing CTEs after schema is in use is possible but risky if not covered by tests.

---

### Pitfall 2: Adjacency List Only → O(depth × fan-out) Read Performance

**What goes wrong:**
The simplest graph schema stores one row per edge (parent_id → child_id). Reading all descendants of a node requires a recursive CTE that re-executes at each depth level. On graphs with depth 10 and fan-out 20, one "find all descendants" query issues ~10 recursive iterations each joining thousands of rows. Egnyte benchmarks show a recursive CTE 5× slower than closure table at depth 100. At production scale (50K+ nodes), graph neighborhood queries used by GraphRAG take seconds instead of milliseconds.

**Why it happens:**
Adjacency list is simple to write and correct. Closure tables require additional write logic and more storage. Developers start with adjacency list intending to optimize later, but by the time performance is a problem, GraphRAG queries depend on the query shape.

**How to avoid:**
Use a dual representation from the start: keep the adjacency table for writes (one row per edge, easy updates), and maintain a closure table (`graph_path` with columns `ancestor_id`, `descendant_id`, `depth`, `weight`) for read traversals. The closure table is updated by a trigger or service method on every edge insert/update/delete. GraphRAG traversal queries read only from `graph_path`. For the project's Flyway single-statement constraint, closure table maintenance requires a dedicated Spring service (not DB triggers) because trigger creation requires multiple statements.

**Warning signs:**
- Subgraph extraction for a single GraphRAG query takes >200ms with the graph still small (< 5K nodes)
- Slow query log shows `cte_recursion_count` consistently above 20
- EXPLAIN output shows nested loops on the graph_edges table

**Phase to address:**
Graph Core Engine phase. The dual-table design must be in the initial Flyway migrations (V79+). Adding a closure table after thousands of nodes exist requires a one-time backfill migration.

---

### Pitfall 3: MySQL–Qdrant Dual Store Desync

**What goes wrong:**
Each graph node in MySQL has a corresponding embedding in Qdrant. When a node is updated (content changed, merged with another node) or deleted, the Qdrant vector is not updated atomically — there is no distributed transaction across the two stores. The result: GraphRAG semantic search returns embeddings for nodes that no longer exist in MySQL, causing null pointer failures in the enrichment step; or returns stale semantic neighbors based on outdated content.

**Why it happens:**
MySQL operations happen inside Spring `@Transactional` boundaries. Qdrant operations happen via HTTP client calls. If the Qdrant call succeeds but MySQL rolls back (or vice versa), the stores diverge. Developers rely on "eventual consistency" informally without explicit reconciliation.

**How to avoid:**
Apply the outbox pattern: the domain service writes a `graph_sync_event` row in MySQL within the same transaction as the node mutation. A separate reconciliation job (Spring Batch step) reads the outbox table and applies the corresponding Qdrant operation, then marks the event processed. On node delete, the service writes `DELETE` to the outbox; the batch job calls `qdrant.deleteById()`. Implement a scheduled consistency check (nightly) that queries MySQL for all node IDs and verifies corresponding Qdrant points exist — log discrepancies as WARN for manual repair.

**Warning signs:**
- GraphRAG returns `NodeNotFoundException` for IDs found in Qdrant but absent from MySQL
- After a node bulk-delete, semantic search still returns results pointing to those deleted nodes
- High error rate in the GraphRAG enrichment step specifically after batch ingestion runs

**Phase to address:**
Graph Core Engine phase. The outbox table must be part of the initial schema. The reconciliation job belongs in the Batch layer.

---

### Pitfall 4: Entity Extraction Inflation with Small Ollama Models

**What goes wrong:**
GraphRAG builds the knowledge graph by asking the LLM to extract entities and relations from text. Models below 7B parameters (e.g., `phi3:mini`, early `llama3:8b` variants) fail to follow structured extraction schemas reliably — they hallucinate entity types not defined in the schema, produce relation names that are verbose and inconsistent (`"is_related_to_in_context_of"` vs `"references"`), or emit malformed JSON. The resulting graph fills up with low-quality nodes, inflating edge counts without semantic value. This cannot be easily fixed retroactively: the bad data is already embedded in Qdrant and persisted in MySQL.

**Why it happens:**
Developers use the smallest available Ollama model to keep extraction fast and resource-light. The default Ollama context of 2048 tokens also causes the model to see only partial document chunks, making extraction worse.

**How to avoid:**
Validate extracted entities against the domain schema before persistence. The `EntityExtractionPort` must return a typed result (`List<ExtractedEntity>`) that the domain validates against a registered `NodeTypeRegistry` before any write. Reject or quarantine extractions that reference unknown node/relation types. For Ollama, use at minimum `llama3.1:8b` or `mistral:7b`, and increase context to 8192 (`num_ctx: 8192` in Ollama Modelfile or via API parameter). Add an extraction quality gate: if a single extraction pass returns more than N new relation types not in the registry, flag it for human review rather than committing.

**Warning signs:**
- The `graph_relation_type` enum/table grows by more than 3 new types per day without a deliberate schema change
- Node count grows faster than document count by an order of magnitude
- GraphRAG responses contain hallucinated entity names not present in source documents

**Phase to address:**
GraphRAG Integration phase. The `NodeTypeRegistry` and extraction validation gate must exist before batch ingestion is enabled for enterprise documents.

---

### Pitfall 5: Spring `@Transactional` / `@Entity` in Domain Layer (Graph Edition)

**What goes wrong:**
Graph traversal algorithms (DFS, BFS, Dijkstra for weighted shortest paths) tempt developers to use JPA lazy-loaded entity graphs directly inside the domain service. The code compiles and passes unit tests (with mocks), but the actual implementation leaks Spring's `@Transactional`, `@Entity`, or `EntityManager` into the `localmind-domain` module which has zero Spring dependencies by design. This breaks `DomainConfig.java` wiring, forces the domain module to add Spring Boot dependencies, and makes domain services non-testable without a Spring context.

**Why it happens:**
Graph traversal on JPA entities feels natural: `node.getEdges().stream().flatMap(e -> e.getTarget().getEdges()...)`. This works in tests because Hibernate opens a session. In production, the traversal happens outside a transaction boundary if the domain service has no `@Transactional`, causing `LazyInitializationException`. The fix instinct is to add `@Transactional` to the domain service, breaking the architecture rule.

**How to avoid:**
Graph traversal must be expressed via domain port interfaces: `GraphTraversalPort.findSubgraph(nodeId, depth, filter)` returns plain domain model objects (not JPA entities). The infrastructure adapter fetches the full subgraph in one CTE query and maps to domain models before returning. The domain service operates only on the returned plain objects. Enforce with ArchUnit tests that assert `localmind-domain` has zero dependency on `org.springframework`, `jakarta.persistence`, or `javax.persistence`.

**Warning signs:**
- Any `@Transactional` annotation appearing in `localmind-domain/src/main/java/`
- Test for a domain service requires `@SpringBootTest` to pass
- `LazyInitializationException` stack traces in application logs originating from `domain.knowledge.service`

**Phase to address:**
Graph Core Engine phase. The `GraphTraversalPort` interface definition must be designed before any implementation starts.

---

### Pitfall 6: JPA N+1 Query on Edge Loading

**What goes wrong:**
When the infrastructure adapter loads graph neighborhoods using JPA entities (`GraphNodeEntity` with a collection of `GraphEdgeEntity`), Hibernate's default lazy loading issues one query per node to load its edges. Loading a 100-node subgraph triggers 1 + 100 = 101 queries. At a 5ms round-trip each, that is 505ms for a subgraph load that a single CTE query would execute in 15ms. This becomes the dominant latency in GraphRAG responses.

**Why it happens:**
JPA `@OneToMany` collections default to `FetchType.LAZY`. Developers discover the `LazyInitializationException` problem (Pitfall 5) and add `FetchType.EAGER`, which then causes the N+1 problem instead.

**How to avoid:**
Do not use JPA entity graph traversal for subgraph loading. The `GraphTraversalAdapter` must issue a single native SQL query (CTE or closure table join) that returns all node + edge rows for the requested subgraph, and manually maps them to domain model objects. Use `@Query` with JPQL JOIN FETCH only for simple one-hop queries. For multi-hop traversal, use `jdbcTemplate.query()` or `namedParameterJdbcTemplate.query()` with the CTE directly. Enable Hibernate statistics in dev profile and add a test that asserts subgraph loading issues fewer than 3 SQL statements.

**Warning signs:**
- Enabling `spring.jpa.show-sql=true` shows dozens of identical `SELECT * FROM graph_edges WHERE node_id=?` with different parameters in sequence
- Response time for graph neighborhood queries scales linearly with the number of returned nodes
- Hibernate statistics show `StatementCount` > 50 for any single graph API request

**Phase to address:**
Graph Core Engine phase. The adapter implementation must use the CTE-first approach from the start.

---

### Pitfall 7: Ollama Context Window Overflow in GraphRAG

**What goes wrong:**
GraphRAG serializes subgraphs (node content + edge descriptions + community summaries) into the LLM context before asking a question. Ollama's default context window is 2048 tokens. A modest subgraph of 15 nodes with 200-word descriptions each already exceeds 3000 tokens. Without explicit context management, the serialized graph is silently truncated by Ollama, and the LLM answers based on partial context — often producing confident but incoherent or incorrect answers. This is especially dangerous for enterprise queries where missing context means missing a critical dependency or process step.

**Why it happens:**
The context limit is not an error — Ollama silently truncates at `num_ctx`. Developers test with small documents and don't notice truncation until the graph grows.

**How to avoid:**
The `GraphRagContextBuilder` domain service must measure estimated token count of the assembled context before sending to the LLM. Use a token estimation function (characters / 4 as a conservative approximation, or a tokenizer library). If the estimated context exceeds 70% of the model's configured `num_ctx`, apply pruning: drop edges with weight below a threshold, keep only the k nearest neighbors by semantic score, summarize distant nodes rather than including their full content. Configure `num_ctx` explicitly in the Ollama adapter: minimum 8192 for GraphRAG queries. Add a WARN log when pruning is applied.

**Warning signs:**
- GraphRAG responses that are factually correct but miss obvious connections visible in the graph UI
- Ollama API responses where `prompt_eval_count` equals `num_ctx` exactly (indicates truncation)
- Responses that reference "the documents you mentioned" when no documents were mentioned (hallucinates context)

**Phase to address:**
GraphRAG Integration phase. The `GraphRagContextBuilder` must implement token budgeting before any user-facing GraphRAG endpoint is exposed.

---

### Pitfall 8: Edge Weight Staleness → Stale Rankings

**What goes wrong:**
Edge weights are computed at ingestion time based on current signals (co-occurrence frequency, semantic similarity, user feedback at that moment). Six months later, a deprecated API endpoint still shows as "highly connected" because the weight is never decayed. Community content from bots that were later removed still influences rankings because historical votes are not retracted from weights. The graph presents a snapshot of past knowledge as current truth.

**Why it happens:**
Weight computation is implemented as part of the ingestion pipeline (one-time calculation). Developers assume that new ingestions will naturally shift weights, but orphaned edges from deleted/deprecated nodes keep their old weights and bias traversal results.

**How to avoid:**
Store the weight computation factors separately from the computed weight: `(co_occurrence_score, semantic_score, user_feedback_score, last_updated_at)`. Run a nightly scheduled job (`WeightDecayJob` in the batch layer) that applies exponential decay: `w_new = w_old * e^(-λ * days_since_update)` where `λ` is configurable per edge type (enterprise relations decay slower than community upvotes). When a node is deleted or deprecated, the job sets all its incident edge weights to 0 rather than deleting them (preserving history). The `GraphTraversalPort` must filter out zero-weight edges by default.

**Warning signs:**
- Enterprise users report that "retired" services or processes appear prominently in AI answers
- Consumer vertical surfaces closed venues or past events as top recommendations
- The graph visualization shows highly weighted connections to nodes marked as "archived"

**Phase to address:**
Graph Weight Engine phase (after Core Engine). The weight schema columns must be in the initial graph schema so the decay job can be added without a destructive migration.

---

### Pitfall 9: Community Ranking Farming (Consumer Vertical)

**What goes wrong:**
The consumer vertical allows community contributions: users create nodes (places, events) and vote on them. Edge weights increase with positive votes. Without sybil detection, a business owner creates 50 fake accounts and votes up their venue, artificially inflating its weight in the graph. The AI then recommends this venue preferentially for any related query. This poisons the entire graph for genuine users and destroys trust in the platform.

**Why it happens:**
Ranking systems based on raw vote counts are the simplest to implement. Sybil detection is complex and not considered in the initial design.

**How to avoid:**
Apply trust-weighted voting from the start: each vote carries a `voter_trust_score` that is the product of account age (days), verified status, and historical accuracy of past contributions. Edge weight delta from a vote is `vote_direction * voter_trust_score`, not a flat +1/-1. Introduce a velocity throttle: no single account can shift any edge weight by more than X points per day. Flag rapid vote surges (>10 votes on one node within 1 hour) for moderation review. Use IP + device fingerprint to detect coordinated voting rings. This does not require ML at MVP: trust score based on account age and verification status is sufficient to significantly raise the cost of farming.

**Warning signs:**
- A node that was created recently climbs to top weight within 24 hours of creation
- Multiple accounts with the same registration date and similar voting patterns
- Nodes with high weight that have no organic search traffic (no one navigates to them naturally)

**Phase to address:**
Consumer Vertical phase. The `voter_trust_score` column must be in the vote schema from day one. Retroactively assigning trust scores to historical votes is difficult.

---

### Pitfall 10: PII Leakage via Graph Entity Extraction in Enterprise Mode

**What goes wrong:**
Enterprise document ingestion (email, tickets, HR processes, architecture docs) uses LLM entity extraction to build graph nodes. Extracted entities include person names, email addresses, project code names, salary references, and client IDs. These become graph nodes persisted in MySQL and indexed in Qdrant. Any user with "read graph" permission can retrieve them via GraphRAG queries or direct graph API calls, even if the source document was access-restricted. Research shows GraphRAG's structured entity extraction leaks significantly more sensitive information than naive RAG because entities are explicitly labeled and indexed.

**Why it happens:**
The ingestion pipeline does not distinguish between semantic content (useful for graph navigation) and sensitive entity content (should be restricted). Access control is applied at document level but not at graph node level.

**How to avoid:**
Implement node-level access control. Each graph node carries the `source_document_ids` it was extracted from and inherits the most restrictive visibility of those documents. The `GraphTraversalPort` interface must accept a `SecurityContext` parameter that the adapter uses to filter returned nodes and edges by caller permissions. Before building a Qdrant embedding for a node, run a PII detection pass (regex for email addresses, phone numbers, ID patterns; or a small local NER model) and mark detected PII fields as `pii=true`. PII-tagged nodes are not included in GraphRAG context unless the caller has explicit clearance. Never send PII-tagged node content to external LLM providers.

**Warning signs:**
- GraphRAG responses contain email addresses or personal names that were only in restricted documents
- A non-privileged user's semantic search returns results that reference `source_document_ids` they cannot access
- Qdrant payloads for graph node embeddings contain raw entity text with visible PII

**Phase to address:**
Enterprise Vertical phase (before any enterprise document ingestion is enabled). The `SecurityContext` filtering in the graph port must be designed alongside the node schema, not added later.

---

### Pitfall 11: GraphRAG Hallucination via Community Detection LLM-as-Judge

**What goes wrong:**
Microsoft's original GraphRAG evaluation used LLM-as-judge (asking an LLM to score answer quality) rather than ground-truth evaluation. Subsequent research shows this methodology significantly inflates perceived quality — when evaluated against ground truth, community-based GraphRAG (global search via community summaries) frequently underperforms simple vector RAG on factual questions. Building roadmap phases around the assumption that community detection + LLM summarization produces superior answers leads to over-investing in community detection infrastructure that delivers worse factual accuracy.

**Why it happens:**
The GraphRAG paper's results look convincing, but the evaluation methodology bias is not mentioned in most tutorials or guides. Developers implement community detection (graph clustering) and LLM summarization assuming quality gains.

**How to avoid:**
Implement GraphRAG as a hybrid: use semantic vector search (Qdrant, already working) as the primary retrieval path, and supplement with graph traversal (1-2 hops from retrieved nodes) to surface related context. Do NOT build community detection + LLM summarization as the primary retrieval path at MVP. Validate GraphRAG quality against a ground truth QA set before declaring it ready: ask 20 factual questions whose correct answers are known, compare GraphRAG vs. naive RAG, and only invest in community detection if GraphRAG shows measurable improvement. Use local Ollama models for this evaluation, not a cloud LLM as judge.

**Warning signs:**
- GraphRAG responses "sound confident" but contain entities or facts not present in any source document
- Global search (community summary path) produces consistently longer but less accurate answers than local search (vector + 1-hop path)
- The LLM judge score and human evaluation score diverge by more than 20 percentage points

**Phase to address:**
GraphRAG Integration phase. The quality gate (ground-truth QA set) must be established before building community detection.

---

### Pitfall 12: Flyway Single-Statement Constraint Explosion with Graph Schema

**What goes wrong:**
The project requires one SQL statement per Flyway migration file. A typical graph schema addition (new node type + index + foreign key) requires 3–5 statements. Adding a closure table requires: CREATE TABLE (1), CREATE INDEX on ancestor_id (2), CREATE INDEX on descendant_id (3), possibly an ALTER to add FK (4). Each must be a separate Flyway file. By V150, the migration history has hundreds of near-empty single-statement files that are hard to follow, and the graph schema evolution story becomes invisible in commit history.

**Why it happens:**
The single-statement constraint exists for a good reason (MySQL DDL transactionality is limited, and multi-statement files create partial-apply risk). But graph schema is inherently more complex than CRUD schema. Developers initially write multi-statement migration files and must then split them, often losing the narrative of what a migration was doing.

**How to avoid:**
Adopt a naming convention for migration families: V85__01_graph_node_base_table.sql, V85__02_graph_node_type_index.sql, V85__03_graph_edge_table.sql, etc. (Flyway resolves multiple files at the same version segment if using underscore suffixes). Write a migration plan comment at the top of each file referencing the family: `-- Part 2 of 4: graph_node_base family. Creates type index for node lookup.` Accept that graph schema will require 10–15 files per logical change. Document the full migration plan before writing SQL.

**Warning signs:**
- A developer writes a migration file with two SQL statements separated by a semicolon
- `flyway:migrate` succeeds but one index is missing because it was in a separate file that was not yet created
- Migration files have names like `V89__fix_previous_migration.sql` indicating a previous file was incomplete

**Phase to address:**
Graph Core Engine phase. Establish the naming convention and migration family documentation standard before writing V79.

---

### Pitfall 13: Graph Visualization Browser Freeze with Full Graph Render

**What goes wrong:**
The frontend loads the full graph (all nodes and all edges visible) into a force-directed layout rendered with D3.js SVG. At 500+ nodes and 2000+ edges, the browser's main thread is blocked for seconds by the force simulation. At 5000+ nodes (a realistic enterprise knowledge graph), the browser tab crashes. D3 SVG rendering also does not scale: each node and edge is an individual DOM element, and the browser's reflow engine cannot handle thousands of simultaneously animated elements.

**Why it happens:**
Graph visualization demos always start with small example graphs that run smoothly. The force-directed layout is visually appealing and is the first thing shown in demos. No performance testing is done until the feature is "done."

**How to avoid:**
Use a WebGL-based renderer from the start. Sigma.js (WebGL) and Cytoscape.js (with the `cytoscape-euler` layout and WebGL renderer extension) handle tens of thousands of elements without DOM overhead. Implement progressive loading: on first render, show only the ego-graph of the current node (1-hop neighborhood, max 50 nodes). Expand on user click. Never load the full graph into the frontend at once; implement server-side pagination of subgraphs with a maximum of 200 nodes per response. Add a node count warning in the UI: "Graph has 5,000 nodes. Showing top 50 by weight. [Load more]."

**Warning signs:**
- The force simulation continues running (CPU usage stays high) after the user stops interacting
- Chrome DevTools Memory tab shows the graph component using >200MB for a 1000-node graph
- `requestAnimationFrame` callbacks take >16ms (visible as dropped frames in Performance panel)

**Phase to address:**
Visualization phase. The pagination and WebGL renderer decision must be made before the first Angular component for graph visualization is written.

---

### Pitfall 14: Plugin Schema Coupling → Flyway History Corruption

**What goes wrong:**
Domain modules (consumer vertical, enterprise vertical) installed as PF4J plugins add their own node types, relation types, and potentially their own auxiliary tables. If these plugin migrations are co-mingled with the core Flyway history (e.g., placed in the same `db/migration` folder), then uninstalling a plugin leaves orphaned migration records in `flyway_schema_history`. Reinstalling a different version of the plugin fails because Flyway sees the old checksums. The migration history becomes inconsistent and manual intervention is required.

**Why it happens:**
PF4J plugins run in the same JVM and Spring context. Developers take the easy path of putting plugin SQL in the same migration location as core SQL.

**How to avoid:**
Each domain module must own its own Flyway `SchemaHistory` table with a module-specific prefix: `flyway_schema_history_consumer`, `flyway_schema_history_enterprise`. Use Flyway's `table` property: configure separate `FlywayConfigurationCustomizer` beans per module, each targeting a different table and a different migration location (`classpath:db/migration/consumer/`). The core `FlywayMigration` bean must only manage `db/migration/core/`. This means the plugin schema is versioned independently and can be uninstalled cleanly. The domain plugin extension point in PF4J should include a `getDatabaseMigrationLocation()` method that the infrastructure layer uses to configure the module-specific Flyway instance.

**Warning signs:**
- Uninstalling a plugin causes Flyway to report "checksum mismatch" on subsequent startup
- Two developers with different plugins installed see different results from `SHOW TABLES`
- A plugin migration file in `db/migration/` folder has a version number that conflicts with a core migration

**Phase to address:**
Plugin/Module Architecture phase. The Flyway isolation design must be established before any domain module adds its first migration.

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Adjacency list only (no closure table) | Simpler schema, faster to write | O(depth) read performance; all GraphRAG traversal queries must be rewritten when adding closure table | Never — add closure table in the same phase as adjacency list |
| Store graph node embeddings in the same Qdrant collection as document chunks | No new collection setup | Retrieval pollution: semantic search for a person node returns document chunks and vice versa; impossible to tune collection parameters per entity type | Never — create a dedicated `graph_nodes` Qdrant collection |
| Compute edge weights synchronously in the API request path | Simpler code flow | Weight computation (LLM calls, aggregation queries) adds 200-2000ms to every write; blocks the response thread | Never in production — use async queue or batch job |
| Use `@Data` (mutable) Lombok on graph node domain models | Fast to write | Violates existing immutability constraint; mutation of node weight during traversal causes race conditions | Never — existing CONCERNS.md already flags this for LlmResponse |
| Single Flyway migration for a multi-step graph schema change | Fewer files | Violates project constraint; MySQL DDL partially applies on failure with no rollback | Never — this is a hard project rule |
| Use LLM-as-judge to evaluate GraphRAG quality | Easy to automate | Inflates perceived quality by up to 20 percentage points vs ground truth; leads to wrong architectural investments | Never for quality gate decisions; only for exploratory analysis |
| Magic constant `max_depth = 10` hardcoded in traversal queries | Works for current data | Becomes wrong as graph depth grows; no way to override per query type | Acceptable for MVP only if behind a named constant `GraphConstants.DEFAULT_MAX_DEPTH` |

---

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| Qdrant (graph node embeddings) | Sharing the `localmind-documents` collection with graph nodes | Create a dedicated `localmind-graph-nodes` Qdrant collection with graph-specific metadata payload schema |
| Qdrant (delete on node removal) | Deleting directly in the same transaction as MySQL node delete | Write a `graph_sync_event` outbox row in MySQL transaction; reconciliation job applies Qdrant delete asynchronously |
| Ollama (entity extraction) | Using default `num_ctx` (2048) for entity extraction prompts | Set `num_ctx = 8192` explicitly in the extraction request; validate token count before sending |
| Ollama (GraphRAG context) | Sending full subgraph to LLM without token budget | Implement `GraphRagContextBuilder` with token counting and pruning before LLM call |
| MySQL (reserved word `recursive`) | Using `recursive` as column or table name alias in CTE | This is already a known concern (CONCERNS.md); use backtick escaping or rename to `with_recursive` syntax |
| PF4J plugin loading | Plugin registers Spring beans that depend on core domain services | Use PF4J extension points and `localmind-plugin-api` interfaces only; plugins must not import `localmind-domain` directly |
| Angular graph visualization | Using HTTP GET for full graph on component init | Use paginated endpoint `GET /api/v1/graph/nodes?center={id}&depth=1&limit=50`; expand progressively on user interaction |

---

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| Full graph load in browser | Browser tab freeze, CPU at 100% during layout | Server-side subgraph pagination (max 200 nodes); WebGL renderer (Sigma.js or Cytoscape WebGL) | >500 nodes with SVG/D3; >5000 nodes with any DOM-based renderer |
| CTE without cycle detection | Server-side query timeout (30s+); MySQL error 3636 (recursion limit) | Path-tracking JSON column in CTE; `JSON_CONTAINS` cycle check | First time a cycle exists in any graph path traversal |
| Subgraph load via JPA lazy collection traversal | 100+ SQL queries per graph API call; response time >1s | Use single CTE native query in adapter; assert <3 SQL statements in integration test | >20 nodes in a traversal result |
| Weight computation on write path | Node creation API calls taking >2s; timeout under batch ingestion load | Async weight computation via Spring event or batch step; return unweighted node immediately | >50 concurrent document ingestion events |
| Force-directed layout simulation running forever | CPU usage stays >30% when graph tab is visible but idle | Stop simulation after convergence (`alpha` reaches threshold); use `alphaDecay` parameter | Immediately on graphs with >200 nodes unless explicitly terminated |
| Community summary generation at query time | GraphRAG global search takes >30s per query | Pre-compute community summaries in a scheduled batch job; cache results with TTL | First query on a community with >100 member nodes |

---

## Security Mistakes

| Mistake | Risk | Prevention |
|---------|------|------------|
| Graph node content not filtered by caller permission | Enterprise employee reads other department's confidential process nodes | Inject `SecurityContext` into `GraphTraversalPort`; adapter adds `WHERE node.visibility IN (caller_permissions)` to every query |
| PII entity nodes accessible via GraphRAG semantic search | GDPR violation; salary data or HR records surfaced via natural language queries | PII detection pass before node persistence; `pii=true` flag on node; exclude PII nodes from Qdrant indexing unless caller has clearance |
| Community-contributed node content sent to cloud LLM without review | Prompt injection via adversarial node content; exfiltration of other users' data | Sanitize node content before including in LLM context; limit community node content in GraphRAG context to moderated or verified nodes only |
| Graph admin API (create/delete node types) without elevated auth | Any authenticated user can corrupt the node type registry | Restrict `POST /api/v1/graph/types` and `DELETE /api/v1/graph/nodes/{id}` to `ROLE_ADMIN`; audit log every type registry change |
| Qdrant accessible without auth in local-first deployment | Co-located process or network neighbor reads all graph embeddings | Configure Qdrant with API key authentication even in local deployment; document in self-hosting guide |

---

## UX Pitfalls

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| Showing raw edge weight numbers (0.732, 0.418) in graph UI | Users don't understand what the number means; confusing rather than informative | Show weight as visual encoding only (edge thickness, opacity); provide tooltip "Strong connection (based on X co-occurrences and Y user confirmations)" |
| Force-directed layout that rearranges on every visit | Users build mental map of graph; layout change on reload disorients them | Persist layout positions for nodes the user has manually arranged; only auto-layout new nodes |
| No loading state during GraphRAG traversal | User waits silently for 3-10 seconds; assumes the app is broken | Stream GraphRAG response via SSE (same pattern as existing chat streaming); show "Searching graph…" → "Found 12 related nodes…" → "Generating answer…" status |
| Graph bilingual label support missing | Italian users see English node type labels; violates i18n requirement | Node types and relation types must store both `label_it` and `label_en`; the frontend uses the `TranslatePipe` to select the current language label |
| Typing indicator missing for slow local LLM GraphRAG queries | 10-15s response time with no feedback → user retries → duplicate queries | Integrate GraphRAG endpoint with existing SSE streaming; disable submit button while query is in flight |

---

## "Looks Done But Isn't" Checklist

- [ ] **Graph node CRUD:** Often missing the closure table update on edge insert/delete — verify that `graph_path` rows are created and deleted atomically with edge mutations.
- [ ] **GraphRAG response:** Often missing source citation (which nodes/paths were used) — verify that the API response includes `sourceNodeIds` and `pathsTaken` fields, not just the answer text.
- [ ] **Consumer moderation:** Often missing the moderation queue — verify that flagged nodes enter a `PENDING_MODERATION` state visible only to moderators, not soft-deleted immediately.
- [ ] **Plugin uninstall:** Often missing the cleanup of orphaned node type entries — verify that uninstalling a plugin removes (or deactivates) all node types and relation types it registered.
- [ ] **Graph visualization bilingual:** Often missing Italian labels for node types — verify that switching UI language to IT shows Italian node type labels, not English fallbacks.
- [ ] **Weight decay job:** Often missing the zero-weight edge filter in traversal — verify that edges with `weight = 0.0` are excluded from subgraph queries and do not appear in the graph visualization.
- [ ] **Enterprise PII gate:** Often missing the PII detection step for email body extraction — verify that email-derived graph nodes pass PII detection before embedding and persistence.
- [ ] **Qdrant consistency:** Often missing the outbox reconciliation job in the batch layer — verify that deleting a graph node in the UI eventually removes the corresponding Qdrant point (check within 5 minutes of deletion).

---

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| MySQL CTE infinite loop in production | MEDIUM | Kill query via `KILL QUERY <id>`; add cycle detection to the offending CTE query; deploy fix; add ArchUnit test to prevent recurrence |
| MySQL–Qdrant desync (phantom Qdrant points) | MEDIUM | Run reconciliation job in DRY_RUN mode to identify orphaned Qdrant points; switch to APPLY mode after manual review; add nightly consistency check |
| Entity extraction inflation (bad graph data) | HIGH | Export affected node IDs; run batch delete for quarantined nodes; re-ingest source documents with improved extraction prompt and quality gate; rebuild affected Qdrant embeddings |
| Browser freeze from graph visualization | LOW | Deploy server-side pagination limit; reload page; no data loss |
| Flyway history corruption from plugin migration collision | HIGH | Manual SQL: update `flyway_schema_history` to correct checksums; isolate plugin to its own history table immediately; consider rollback migration |
| PII leakage via graph nodes | CRITICAL | Identify affected nodes via PII scan; revoke Qdrant access temporarily; delete or restrict affected nodes; run GDPR deletion procedure; review ingestion pipeline before re-enabling |
| Edge weight corruption from ranking farming | MEDIUM | Identify suspicious vote clusters via velocity analysis; reset affected edge weights to pre-farming baseline; ban identified bot accounts; enable trust-weighted voting |

---

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| CTE infinite loop / cycle detection | Graph Core Engine | Integration test with circular graph data asserts no infinite loop |
| Adjacency list only (no closure table) | Graph Core Engine | Benchmark: 10K-node subgraph query <100ms |
| MySQL–Qdrant desync | Graph Core Engine | Outbox table exists in schema; reconciliation job runs and reports 0 discrepancies |
| Entity extraction inflation | GraphRAG Integration | Extraction quality gate rejects 100% of unknown relation types |
| Spring annotations in domain layer | Graph Core Engine | ArchUnit test passes: zero Spring imports in `localmind-domain` |
| JPA N+1 on edge loading | Graph Core Engine | Integration test asserts <3 SQL statements for 100-node subgraph load |
| Ollama context window overflow | GraphRAG Integration | Token budget test: 200-node subgraph does not overflow configured `num_ctx` |
| Edge weight staleness | Graph Weight Engine | Decay job test: edge created 90 days ago has weight < 50% of initial |
| Community ranking farming | Consumer Vertical | Velocity test: >10 votes/hour on one node triggers moderation flag |
| PII leakage via entity extraction | Enterprise Vertical | PII detection test: email with SSN produces node with `pii=true`, excluded from Qdrant |
| GraphRAG LLM-as-judge inflation | GraphRAG Integration | Ground-truth QA set evaluated before declaring GraphRAG ready |
| Flyway single-statement explosion | Graph Core Engine | Migration naming convention documented; CI check rejects multi-statement files |
| Browser freeze from full graph render | Visualization | Performance test: 5000-node graph renders without frame drops (Lighthouse) |
| Plugin schema / Flyway history coupling | Plugin/Module Architecture | Uninstall plugin test: `flyway_schema_history` core table unchanged after plugin removal |

---

## Sources

- Egnyte Engineering: Evaluating MySQL Recursive CTE at Scale — https://www.egnyte.com/blog/post/12780evaluating-mysql-recursive-cte-at-scale/
- MySQL Official Blog: CTE Cycle Avoidance — https://dev.mysql.com/blog-archive/mysql-8-0-1-recursive-common-table-expressions-in-mysql-ctes-part-four-depth-first-or-breadth-first-traversal-transitive-closure-cycle-avoidance/
- MySQL Worklog: cte_max_recursion_depth — https://dev.mysql.com/worklog/task/?id=10972
- Ramu Ramaiah: Recursive CTE vs Closure Tables in MySQL — https://medium.com/@ramu.ramaiah/recursive-cte-vs-closure-tables-in-mysql-choosing-the-right-strategy-for-hierarchical-data-c1c89ebd264f
- Chi-Sheng Liu: GraphRAG Local Ollama Pitfalls Prevention Guide — https://chishengliu.com/posts/graphrag-local-ollama/
- arXiv 2512.09148: Detecting Hallucinations in GraphRAG via Attention Patterns — https://arxiv.org/abs/2512.09148
- arXiv 2603.14828: Mitigating KG Quality Issues — Multi-Hop GraphRAG Retrieval — https://arxiv.org/pdf/2603.14828
- arXiv 2605.20815: GraphRAG on Consumer Hardware: Benchmarking Local LLMs — https://arxiv.org/html/2605.20815v1
- Medium: GraphRAG Complete Guide (community detection evaluation methodology) — https://medium.com/@brian-curry-research/graphrag-the-complete-guide-to-graph-powered-retrieval-augmented-generation-eeb58a6bb4d1
- arXiv 2508.17222: Exposing Privacy Risks in Graph RAG — https://arxiv.org/pdf/2508.17222
- RAG Systems Leaking Sensitive Data (we45) — https://www.we45.com/post/rag-systems-are-leaking-sensitive-data
- Secure RAG Enterprise Architecture Patterns — https://petronellatech.com/blog/secure-rag-enterprise-architecture-patterns-for-accurate-leak-free-ai/
- Medium: Big Data Graph Visualisations (browser scale limits) — https://medium.com/@jollyp/big-data-graph-visualisations-75f341dc36ec
- Medium: Best Libraries for Large Network Graphs (WebGL vs SVG) — https://weber-stephen.medium.com/the-best-libraries-and-methods-to-render-large-network-graphs-on-the-web-d122ece2f4dc
- Knowledge Graph Decay (Neo4j, temporal decay patterns) — https://isuruig.medium.com/knowledge-graph-decay-how-your-neo4j-graph-quietly-diverges-from-reality-376d9452220b
- Medium: Mastering JPA — N+1 Queries and Cartesian Explosions — https://medium.com/@markus.jessenitschnig/mastering-jpa-performance-real-world-strategies-to-eliminate-n-1-queries-and-cartesian-explosions-94ddd9c59b90
- VentureBeat: Dual-store consistency challenges in GraphRAG — https://venturebeat.com/data/surrealdb-3-0-wants-to-replace-your-five-database-rag-stack-with-one/
- Cognee: Vectors and Graphs in Practice (dual store pitfalls) — https://www.cognee.ai/blog/fundamentals/vectors-and-graphs-in-practice
- Baeldung: Hexagonal Architecture with Spring Boot — https://www.baeldung.com/hexagonal-architecture-ddd-spring
- AWS: Combining content moderation with graph databases — https://aws.amazon.com/blogs/gametech/combining-content-moderation-services-with-graph-databases-analytics-to-reduce-community-toxicity/
- LocalMind codebase: `.planning/codebase/CONCERNS.md`, `.planning/codebase/ARCHITECTURE.md`, `.planning/PROJECT.md` (brownfield constraints and existing known issues)

---
*Pitfalls research for: Knowledge Graph Engine + GraphRAG + Dual-Domain Platform on MySQL + Qdrant*
*Researched: 2026-06-29*
