# Architecture Research

**Domain:** Weighted Knowledge Graph Engine + GraphRAG integration into existing hexagonal Spring Boot app
**Researched:** 2026-06-29
**Confidence:** HIGH (based on direct codebase inspection + verified external sources)

---

## Standard Architecture

### System Overview: Graph Layer Integration Points

```
Angular 21 Frontend
 features/knowledge/   ← extend with Cytoscape.js canvas + GraphRAG chat
         │
         │ HTTP REST /api/v1/knowledge/** + /api/v1/graph-rag/**
         ▼
localmind-api
 knowledge/controller/KnowledgeGraphController  ← extend existing
 graphrag/controller/GraphRagController         ← NEW
         │
         │ calls port/in interfaces only
         ▼
localmind-domain  (pure Java, zero Spring)
 ┌────────────────────────────────────────────────────────┐
 │  knowledge/ (EXTEND — do not create new domain)        │
 │                                                        │
 │  model/                                                │
 │    KnowledgeEntity     ← add: embeddingId, domainId    │
 │    KnowledgeRelation   ← add: weight, domainId,        │
 │                               confidenceScore,         │
 │                               usageCount               │
 │    WeightFactors       ← NEW value object              │
 │    GraphPath           ← NEW value object              │
 │    GraphRagContext     ← NEW value object              │
 │    GraphRagAnswer      ← NEW value object              │
 │    DomainSchema        ← NEW value object              │
 │                                                        │
 │  port/in/                                              │
 │    KnowledgeGraphUseCase  ← extend with weight ops     │
 │    GraphRagUseCase        ← NEW                        │
 │                                                        │
 │  port/out/                                             │
 │    KnowledgeGraphPort     ← extend: weight, domain     │
 │    GraphSemanticPort      ← NEW (Qdrant node embeds)   │
 │    GraphWeightCalculatorPort ← NEW                     │
 │    GraphDomainSchemaPort  ← NEW (plugin registry)      │
 │    GraphRagLlmPort        ← NEW (LLM context call)     │
 │                                                        │
 │  service/                                              │
 │    KnowledgeGraphService  ← extend (weight updates)    │
 │    GraphRagService        ← NEW                        │
 └────────────────────────────────────────────────────────┘
         │ port/out/ interfaces
         ▼
localmind-infrastructure  (Spring adapters)
 knowledge/adapter/
   JdbcKnowledgeGraphAdapter   ← extend: weight columns,
                                   domain filter, MySQL 8
                                   WITH RECURSIVE path queries
   GraphSemanticAdapter        ← NEW: Qdrant collection
                                   'localmind_graph_nodes'
   UsageWeightCalculatorAdapter ← NEW: weight formula impl
   Pf4jGraphDomainSchemaAdapter ← NEW: discovers PF4J plugins
   GraphRagLlmAdapter          ← NEW: wraps LlmGatewayService
         │
         ▼
 localmind-plugin-api  (extension contracts)
   GraphDomainSchemaExtension  ← NEW ExtensionPoint
         │
         │ implements
         ▼
 Domain Module JARs (loaded at runtime by PF4J)
   localmind-module-consumer   ← node types: PLACE, POI,
                                   EVENT, ITINERARY, EXPERIENCE
   localmind-module-enterprise ← node types: DOCUMENT, PROCESS,
                                   REPOSITORY, MICROSERVICE,
                                   API, PERSON, TEAM, DECISION
         │
         ▼
 External Stores
   MySQL 8.0  knowledge_entities + knowledge_relations tables
              ← add weight, domain_id, embedding_id,
                 confidence_score, usage_count columns
   Qdrant     'localmind_graph_nodes' collection (NEW)
              'localmind' collection (existing, document chunks)
```

### Component Responsibilities

| Component | Responsibility | Location |
|-----------|---------------|---------|
| `KnowledgeEntity` (model) | Node with id, name, EntityType, properties, embeddingId, domainId | `domain/knowledge/model/` |
| `KnowledgeRelation` (model) | Edge with weight (0.0-1.0), from/to node IDs, type, domainId, confidenceScore, usageCount | `domain/knowledge/model/` |
| `WeightFactors` (model) | Value object: usageWeight, confidenceWeight, feedbackWeight, freshnessWeight — configurable per deployment | `domain/knowledge/model/` |
| `GraphPath` (model) | Ordered list of nodes + edges from source to target, total path weight | `domain/knowledge/model/` |
| `GraphRagContext` (model) | Serialized subgraph (nodes, weighted edges, paths) + document chunks for LLM prompt | `domain/knowledge/model/` |
| `GraphRagAnswer` (model) | LLM answer text + cited node IDs + traversed relation IDs | `domain/knowledge/model/` |
| `DomainSchema` (model) | Domain vocabulary: allowed node types, relation types, default weight config | `domain/knowledge/model/` |
| `KnowledgeGraphUseCase` (port/in) | Extend: add updateRelationWeight, findPath, listByDomain | `domain/knowledge/port/in/` |
| `GraphRagUseCase` (port/in) | query(question, options), suggestMissingLinks(nodeId), recordFeedback(relationId, positive) | `domain/knowledge/port/in/` |
| `KnowledgeGraphPort` (port/out) | Extend: storeWithDomain, findByDomain, updateWeight, findPath(from, to, minWeight, maxDepth) via recursive CTE | `domain/knowledge/port/out/` |
| `GraphSemanticPort` (port/out) | storeNodeEmbedding(nodeId, vector, metadata), searchSimilarNodes(queryVector, topK, domainFilter) | `domain/knowledge/port/out/` |
| `GraphWeightCalculatorPort` (port/out) | computeWeight(relation, factors) — pluggable formula | `domain/knowledge/port/out/` |
| `GraphDomainSchemaPort` (port/out) | getSchema(domainId), listSchemas() — backed by PF4J discovery | `domain/knowledge/port/out/` |
| `GraphRagLlmPort` (port/out) | answer(question, graphRagContext) — delegates to LlmGatewayService | `domain/knowledge/port/out/` |
| `KnowledgeGraphService` | Extended: weight update, domain-scoped ops, embedding trigger | `domain/knowledge/service/` |
| `GraphRagService` | Orchestrates: embed query → find nodes semantically → expand subgraph → merge → build context → call LLM → update weights | `domain/knowledge/service/` |
| `JdbcKnowledgeGraphAdapter` | Extend: weight column ops, WITH RECURSIVE CTE for path queries, domain_id filtering | `infrastructure/knowledge/adapter/` |
| `GraphSemanticAdapter` | Implements GraphSemanticPort using Qdrant `localmind_graph_nodes` collection + Spring AI EmbeddingModel | `infrastructure/knowledge/adapter/` |
| `UsageWeightCalculatorAdapter` | Implements GraphWeightCalculatorPort; formula: configurable weighted sum of usage_count, confidence_score, user_feedback | `infrastructure/knowledge/adapter/` |
| `Pf4jGraphDomainSchemaAdapter` | Implements GraphDomainSchemaPort; calls PF4J PluginManager to find all GraphDomainSchemaExtension implementations | `infrastructure/knowledge/adapter/` |
| `GraphRagLlmAdapter` | Implements GraphRagLlmPort; delegates to existing LlmGatewayService.chat() with structured graph prompt | `infrastructure/knowledge/adapter/` |
| `GraphDomainSchemaExtension` | PF4J extension point; plugins implement to register domain vocabularies | `localmind-plugin-api/` |
| `GraphRagController` | REST POST /api/v1/graph-rag/query, GET /api/v1/graph-rag/suggest/{nodeId} | `api/graphrag/controller/` |
| `KnowledgeGraphController` | Extend: PATCH /api/v1/knowledge/relations/{id}/weight, GET /api/v1/knowledge/path, GET /api/v1/knowledge/domains | `api/knowledge/controller/` |
| `graph-canvas` (Angular) | Cytoscape.js force-directed visualization; edges rendered with thickness proportional to weight | `features/knowledge/components/graph-canvas/` |
| `graph-filter-panel` (Angular) | Filters: domain selector, node type checkboxes, min-weight slider, depth spinner | `features/knowledge/components/graph-filter-panel/` |
| `node-detail-panel` (Angular) | Right panel: selected node properties, adjacent relations, "Ask AI about this node" shortcut | `features/knowledge/components/node-detail-panel/` |
| `graphrag-chat` (Angular) | Chat-like interface for GraphRAG queries; response shows answer + collapsible "Path used" panel | `features/knowledge/components/graphrag-chat/` |
| `graph.store.ts` (Angular) | Signal store: nodes, edges, selectedNodeId, filters, ragAnswer | `features/knowledge/state/` |

---

## Recommended Project Structure

```
localmind-domain/src/main/java/com/localmind/domain/knowledge/
├── model/
│   ├── KnowledgeEntity.java         # extend: + embeddingId, domainId
│   ├── KnowledgeRelation.java       # extend: + weight, domainId,
│   │                                #   confidenceScore, usageCount
│   ├── EntityType.java              # extend: keep open via domainId override
│   ├── RelationType.java            # extend: keep open via domainId override
│   ├── KnowledgeSubgraph.java       # existing (no change needed)
│   ├── KnowledgeGraphStats.java     # extend: + weightDistribution, domainBreakdown
│   ├── WeightFactors.java           # NEW — configurable weight formula params
│   ├── GraphPath.java               # NEW — ordered node+edge path with total weight
│   ├── GraphRagContext.java         # NEW — subgraph serialization for LLM prompt
│   ├── GraphRagAnswer.java          # NEW — answer + cited node IDs + relation IDs
│   ├── DomainSchema.java            # NEW — vocabulary for one domain module
│   ├── NodeTypeDefinition.java      # NEW — name, labelIT, labelEN, properties
│   └── RelationTypeDefinition.java  # NEW — name, labelIT, labelEN, directional
├── port/
│   ├── in/
│   │   ├── KnowledgeGraphUseCase.java  # extend: +updateWeight, +findPath, +listByDomain
│   │   └── GraphRagUseCase.java        # NEW
│   └── out/
│       ├── KnowledgeGraphPort.java     # extend: +updateWeight, +findPath, +storeWithDomain
│       ├── EntityExtractorPort.java    # existing (no change)
│       ├── GraphSemanticPort.java      # NEW
│       ├── GraphWeightCalculatorPort.java # NEW
│       ├── GraphDomainSchemaPort.java  # NEW
│       └── GraphRagLlmPort.java       # NEW
└── service/
    ├── KnowledgeGraphService.java  # extend: weight update loop, domain ops
    └── GraphRagService.java        # NEW — orchestrates GraphRAG pipeline

localmind-infrastructure/src/main/java/com/localmind/infrastructure/knowledge/
├── adapter/
│   ├── JdbcKnowledgeGraphAdapter.java    # extend: weight, domain, CTE path
│   ├── LlmEntityExtractorAdapter.java    # existing (no change)
│   ├── GraphSemanticAdapter.java         # NEW
│   ├── UsageWeightCalculatorAdapter.java # NEW
│   ├── Pf4jGraphDomainSchemaAdapter.java # NEW
│   └── GraphRagLlmAdapter.java           # NEW
├── config/
│   └── KnowledgeGraphProperties.java     # extend: + weightFactors, + qdrant.graphCollection
└── persistence/
    ├── entity/
    │   ├── KnowledgeEntityEntity.java    # extend: + embeddingId, domainId
    │   └── KnowledgeRelationEntity.java  # extend: + weight, domainId,
    │                                     #   confidenceScore, usageCount
    └── repository/
        ├── KnowledgeEntityJpaRepository.java   # extend: + findByDomainId
        └── KnowledgeRelationJpaRepository.java # extend: + findByDomainId,
                                                #   + updateWeight, + findPath (native SQL)

localmind-plugin-api/src/main/java/com/localmind/plugin/
├── DocumentParserExtension.java       # existing
├── LlmProviderExtension.java          # existing
├── VectorStoreExtension.java          # existing
└── GraphDomainSchemaExtension.java    # NEW

localmind-api/src/main/java/com/localmind/api/
├── knowledge/
│   ├── controller/KnowledgeGraphController.java  # extend: weight + domain endpoints
│   └── dto/                                      # extend: weight fields in DTOs
└── graphrag/
    ├── controller/GraphRagController.java        # NEW
    └── dto/
        ├── GraphRagQueryRequest.java             # NEW
        ├── GraphRagQueryResponse.java            # NEW
        └── LinkSuggestionResponseDto.java        # NEW

localmind-app/src/main/resources/db/migration/
├── V79__add_weight_to_knowledge_relations.sql
├── V80__add_domain_id_to_knowledge_entities.sql
├── V81__add_domain_id_to_knowledge_relations.sql
├── V82__add_embedding_id_to_knowledge_entities.sql
├── V83__add_usage_count_to_knowledge_relations.sql
├── V84__add_confidence_score_to_knowledge_relations.sql
├── V85__add_index_weight_knowledge_relations.sql
└── V86__add_index_domain_id_knowledge_relations.sql

localmind-frontend/src/app/features/knowledge/
├── knowledge.routes.ts         # extend: add /graph, /query child routes
├── models/
│   ├── knowledge.model.ts      # extend: + weight, domainId on KnowledgeRelation
│   ├── graph-rag.model.ts      # NEW
│   └── domain-schema.model.ts  # NEW
├── services/
│   ├── knowledge.service.ts    # extend: weight endpoint, path endpoint
│   └── graph-rag.service.ts    # NEW
├── state/
│   └── graph.store.ts          # NEW — Signal store
├── pages/
│   ├── knowledge-page/         # existing — keep as entry, refactor to shell
│   ├── graph-page/             # NEW — Cytoscape canvas view
│   └── graphrag-page/          # NEW — chat-style query + answer
└── components/
    ├── graph-canvas/           # NEW — Cytoscape.js (direct, no wrapper)
    ├── graph-filter-panel/     # NEW
    ├── node-detail-panel/      # NEW
    └── graphrag-chat/          # NEW
```

---

## Architectural Patterns

### Pattern 1: Extend Existing `knowledge` Domain — Do Not Add a New Domain

The existing `knowledge` domain already has the right shape: `KnowledgeGraphService`, `KnowledgeGraphPort`, `EntityExtractorPort`. The graph engine is an evolution of this domain, not a new bounded context.

**What to do:** Add `weight`, `domainId`, `confidenceScore`, `usageCount` to the existing models. Add `GraphRagService` as a second service inside the same `knowledge` domain package. Add new port interfaces alongside the existing ones.

**What NOT to do:** Do not create a `graph` domain alongside `knowledge`. Two domains for the same concept creates boundary violations and forces cross-domain imports inside the domain layer — exactly the anti-pattern documented in `MODULE_BOUNDARIES.md`.

### Pattern 2: Separate Graph Storage (MySQL) from Graph Semantics (Qdrant)

Use two distinct port/out interfaces for two distinct capabilities:

- `KnowledgeGraphPort` → MySQL — stores structural graph data (nodes, typed edges, weights)
- `GraphSemanticPort` → Qdrant `localmind_graph_nodes` collection — stores node embeddings

This mirrors the existing document architecture: MySQL holds document metadata + chunk metadata (structural), Qdrant holds chunk embeddings (semantic). The graph follows the same split.

The `localmind_graph_nodes` Qdrant collection is separate from the existing `localmind` document chunk collection. This avoids mixing document chunk vectors with graph node vectors — their semantic spaces are different and merging them would corrupt GraphRAG retrieval.

```
GraphSemanticAdapter.storeNodeEmbedding():
  1. Compute embedding via @Primary EmbeddingModel (Ollama default)
  2. Store in Qdrant: collection='localmind_graph_nodes',
     payload: { nodeId, entityType, domainId, name }
  3. Update knowledge_entities.embedding_id = qdrant_point_id

GraphSemanticAdapter.searchSimilarNodes(queryVector, topK, domainId):
  1. Qdrant similarity search on 'localmind_graph_nodes'
  2. Optional payload filter: { domainId }
  3. Return list of nodeIds ranked by cosine similarity
```

### Pattern 3: MySQL 8 WITH RECURSIVE for Subgraph / Path Queries

MySQL 8.0 supports `WITH RECURSIVE` CTEs. The existing `JdbcKnowledgeGraphAdapter.collectSubgraph()` uses in-application BFS (Java recursion). This must be replaced with a single native SQL recursive CTE for correctness (cycle safety) and performance.

```sql
-- Example: find subgraph up to depth 3 from a given node, min weight 0.3
WITH RECURSIVE graph_traversal AS (
  SELECT id, name, entity_type, 0 AS depth,
         CAST(id AS CHAR(500)) AS visited_path
  FROM knowledge_entities
  WHERE id = :startNodeId

  UNION ALL

  SELECT e.id, e.name, e.entity_type, gt.depth + 1,
         CONCAT(gt.visited_path, ',', e.id)
  FROM knowledge_entities e
  JOIN knowledge_relations r
    ON (r.from_entity_id = e.id OR r.to_entity_id = e.id)
  JOIN graph_traversal gt
    ON (r.from_entity_id = gt.id OR r.to_entity_id = gt.id)
  WHERE gt.depth < :maxDepth
    AND r.weight >= :minWeight
    AND FIND_IN_SET(e.id, gt.visited_path) = 0
)
SELECT DISTINCT id, name, entity_type, depth FROM graph_traversal
```

Use `@Query(value = "...", nativeQuery = true)` in `KnowledgeRelationJpaRepository` for path queries. Add `SET cte_max_recursion_depth = 100` in `KnowledgeGraphProperties` to prevent unbounded recursion.

### Pattern 4: GraphRAG Service Orchestration Pipeline

`GraphRagService` is a pure domain service. It has no Spring dependencies. All external capabilities are injected via port/out interfaces.

```
GraphRagService.query(question, options):

  STEP 1 — Semantic node anchoring
    float[] questionEmbedding = graphSemanticPort.embed(question)
    List<String> anchorNodeIds = graphSemanticPort
        .searchSimilarNodes(questionEmbedding, options.topKNodes, options.domainId)

  STEP 2 — Graph expansion from anchors
    for each anchorNodeId:
        KnowledgeSubgraph subgraph = knowledgeGraphPort
            .getSubgraph(anchorNodeId, options.depth, options.minWeight)
        mergedSubgraph.merge(subgraph)     // deduplicate nodes + edges

  STEP 3 — Optional document chunk enrichment
    for each node in mergedSubgraph.nodes (if node has sourceDocumentId):
        List<DocumentChunk> chunks = vectorStorePort
            .findBySourceDocument(node.sourceDocumentId, topK=3)
        context.addChunks(chunks)

  STEP 4 — Context serialization
    String graphText = GraphRagContextBuilder.serialize(mergedSubgraph)
    // Format: "Node: Paris [PLACE] connected to: Eiffel Tower [PLACE] (weight=0.9),
    //          France [ORGANIZATION] (weight=0.7) ..."

  STEP 5 — LLM answer generation
    GraphRagAnswer answer = graphRagLlmPort.answer(question, graphText)
    // LLM prompt includes: system instruction, serialized graph, user question
    // Response includes: answer text + cited node names

  STEP 6 — Weight reinforcement
    for each traversed relation:
        knowledgeGraphPort.incrementUsageCount(relation.id)
        graphWeightCalculatorPort.recalculate(relation)
        knowledgeGraphPort.updateWeight(relation.id, newWeight)

  RETURN answer
```

### Pattern 5: PF4J Plugin Provides Domain Vocabulary, Not Graph Logic

The plugin extension point `GraphDomainSchemaExtension` declares ONLY the vocabulary (node type names, relation type names, labels in IT/EN, default weight config). It does NOT contain any graph traversal logic — that stays in the core domain service.

```java
// localmind-plugin-api
public interface GraphDomainSchemaExtension extends ExtensionPoint {
    String getDomainId();                              // e.g. "consumer"
    Set<NodeTypeDefinition> getNodeTypes();            // PLACE, POI, EVENT...
    Set<RelationTypeDefinition> getRelationTypes();    // LOCATED_IN, NEAR, PART_OF...
    WeightConfig getDefaultWeightConfig();             // usageWeight=0.4, freshness=0.2...
}
```

`Pf4jGraphDomainSchemaAdapter` (infrastructure) calls `pluginManager.getExtensions(GraphDomainSchemaExtension.class)` to collect all registered schemas at startup and on plugin load events. Registered schemas are cached in memory and invalidated when a plugin is loaded/unloaded.

`KnowledgeEntityEntity` and `KnowledgeRelationEntity` store `domain_id VARCHAR(100)` as a plain string — validated at the service layer against registered schemas. This avoids tight coupling between the DB schema and the set of installed plugins.

### Pattern 6: Cytoscape.js Direct Integration in Angular (No Wrapper)

Use `cytoscape` npm package directly. Do NOT use `ngx-cytoscape` or similar wrappers — they are outdated (Angular 5-13 era) and unmaintained. Use `ViewChild` to get the canvas div reference and initialize Cytoscape in `ngAfterViewInit`.

```typescript
// graph-canvas.component.ts (standalone)
@Component({ selector: 'app-graph-canvas', standalone: true, ... })
export class GraphCanvasComponent implements AfterViewInit, OnDestroy {
  @ViewChild('graphContainer') graphContainerRef!: ElementRef;
  private cy?: cytoscape.Core;

  private graphStore = inject(GraphStore);

  ngAfterViewInit(): void {
    this.cy = cytoscape({
      container: this.graphContainerRef.nativeElement,
      layout: { name: 'cose' },    // force-directed
      style: [
        { selector: 'node', style: { label: 'data(name)', ... } },
        { selector: 'edge', style: {
            width: 'mapData(weight, 0, 1, 1, 8)',  // thickness = weight
            'line-color': '#aaa', ...
        }}
      ]
    });
    effect(() => this.syncGraphToStore(this.graphStore.nodes(), this.graphStore.edges()));
  }
}
```

Edge width mapped to weight using Cytoscape's `mapData()` selector — visual weight representation without custom rendering code. Install: `npm install cytoscape @types/cytoscape`.

### Pattern 7: DomainConfig Wiring with Optional Ports

Follow the existing `Optional<KnowledgeGraphPort>` pattern in `DomainConfig.java` for the new ports. This ensures the application boots even if graph-related adapters are conditionally disabled.

```java
// DomainConfig.java additions
@Bean
public GraphRagService graphRagService(
        KnowledgeGraphPort graphPort,
        GraphSemanticPort semanticPort,
        GraphWeightCalculatorPort weightCalculator,
        GraphRagLlmPort llmPort,
        Optional<VectorStorePort> vectorStorePort) {
    return new GraphRagService(graphPort, semanticPort, weightCalculator,
                               llmPort, vectorStorePort.orElse(null));
}
```

Adapters gated by `@ConditionalOnProperty(name = "localmind.knowledge-graph.enabled", havingValue = "true")` — same property already used by `JdbcKnowledgeGraphAdapter`.

---

## Data Flow

### Flow 1: Graph Node Ingestion (Automatic, from Document)

```
Document ingested via DocumentIngestionPipelineService
    ↓
KnowledgeGraphService.indexText(text, sourceDocumentId)
    ↓
EntityExtractorPort.extractEntities(text)      [LlmEntityExtractorAdapter]
    ↓ list of KnowledgeEntity (no weight yet)
EntityExtractorPort.extractRelations(text, entities)
    ↓ list of KnowledgeRelation (confidence = LLM confidence score)
KnowledgeGraphPort.storeEntities(entities)     [JdbcKnowledgeGraphAdapter]
    ↓ entities stored in MySQL knowledge_entities
GraphSemanticPort.storeNodeEmbedding(nodeId, name, entityType)
    ↓ embedding computed (Ollama), stored in Qdrant 'localmind_graph_nodes'
    ↓ embedding_id written back to knowledge_entities.embedding_id
KnowledgeGraphPort.storeRelations(relations)   [JdbcKnowledgeGraphAdapter]
    ↓ relations stored in knowledge_relations
    ↓ initial weight = confidence_score from extraction
GraphWeightCalculatorPort.computeInitialWeight(relation)
    ↓ weight = confidenceWeight * confidence_score + freshnessBonus
KnowledgeGraphPort.updateWeight(relationId, weight)
```

### Flow 2: GraphRAG Query

```
POST /api/v1/graph-rag/query { question, domainId?, depth?, minWeight?, topKNodes? }
    ↓
GraphRagController.query(GraphRagQueryRequest)
    ↓
GraphRagUseCase.query(question, options)       [GraphRagService]
    ↓
  [1] GraphSemanticPort.embed(question)         [GraphSemanticAdapter → Ollama EmbeddingModel]
  [2] GraphSemanticPort.searchSimilarNodes()   [GraphSemanticAdapter → Qdrant]
      returns: top-K node IDs
  [3] KnowledgeGraphPort.getSubgraph(nodeId, depth, minWeight)  × topK
                                               [JdbcKnowledgeGraphAdapter → MySQL WITH RECURSIVE]
  [4] merge subgraphs, deduplicate
  [5] Optional: VectorStorePort.findBySourceDocumentIds()  → document chunks
  [6] GraphRagContextBuilder.serialize(subgraph, chunks)   → structured text
  [7] GraphRagLlmPort.answer(question, context)           [GraphRagLlmAdapter → LlmGatewayService]
  [8] Async: increment usage_count + recalculate weights on traversed edges
    ↓
GraphRagAnswer { answer, citedNodeIds, traversedRelationIds }
    ↓ mapped to GraphRagQueryResponse DTO
200 OK
```

### Flow 3: Weight Recalculation (Triggered Post-Query and via Feedback)

```
Trigger A: After GraphRAG query (async)
Trigger B: POST /api/v1/knowledge/relations/{id}/feedback { positive: true }

    ↓
KnowledgeGraphUseCase.recordFeedback(relationId, positive)   [KnowledgeGraphService]
    ↓
KnowledgeGraphPort.incrementUsageCount(relationId) OR decrementScore
    ↓
GraphWeightCalculatorPort.computeWeight(relation, factors)
    weight = (usageWeight * normalizedUsage)
           + (confidenceWeight * confidenceScore)
           + (feedbackWeight * positiveFeedbackRatio)
           + (freshnessWeight * decayFunction(createdAt))
    ↓
KnowledgeGraphPort.updateWeight(relationId, newWeight)
    ↓ UPDATE knowledge_relations SET weight = ? WHERE id = ?
```

### Flow 4: Plugin-Driven Domain Schema Registration

```
Application startup / plugin load event
    ↓
Pf4jGraphDomainSchemaAdapter.init()
    ↓
PluginManager.getExtensions(GraphDomainSchemaExtension.class)
    ↓ collects: ConsumerDomainSchemaExtension, EnterpriseDomainSchemaExtension
GraphDomainSchemaPort.registerAll(schemas)
    ↓ in-memory map: domainId → DomainSchema

POST /api/v1/knowledge/entities   { name, type, domainId }
    ↓ KnowledgeGraphController
    ↓ KnowledgeGraphUseCase.createEntity()
    ↓ GraphDomainSchemaPort.getSchema(domainId) → validate type is allowed
    ↓ KnowledgeGraphPort.storeEntity(entity)
    ↓ GraphSemanticPort.storeNodeEmbedding(nodeId, ...)
```

### State Management (Frontend)

```
graph.store.ts (Signal-based, follows chat.store.ts pattern)

Private signals:
  _nodes = signal<GraphNode[]>([])
  _edges = signal<GraphEdge[]>([])   // each edge has weight, domainId
  _selectedNodeId = signal<string | null>(null)
  _filters = signal<GraphFilters>({ domainId: null, minWeight: 0.0, nodeTypes: [] })
  _ragAnswer = signal<GraphRagAnswer | null>(null)
  _loading = signal(false)

Public readonly:
  nodes = _nodes.asReadonly()
  edges = _edges.asReadonly()
  selectedNodeId = _selectedNodeId.asReadonly()
  filters = _filters.asReadonly()
  ragAnswer = _ragAnswer.asReadonly()
  loading = _loading.asReadonly()

Computed:
  filteredEdges = computed(() =>
    _edges().filter(e => e.weight >= _filters().minWeight))

Methods (immutable — always set new array/object, never mutate):
  setNodes(nodes: GraphNode[])   → _nodes.set([...nodes])
  setEdges(edges: GraphEdge[])   → _edges.set([...edges])
  selectNode(id: string | null)  → _selectedNodeId.set(id)
  updateFilters(partial)         → _filters.update(f => ({ ...f, ...partial }))
  setRagAnswer(answer)           → _ragAnswer.set(answer)
```

---

## Scaling Considerations

| Scale | Architecture Adjustment |
|-------|------------------------|
| < 10K nodes | Current JPA + BFS works. MySQL CTE queries for paths. Qdrant on single node. |
| 10K–500K nodes | Add `FULLTEXT INDEX` on `name` + weight-filtered indexes. Tune Qdrant HNSW `m` and `ef_construction`. Move weight recalculation to async Spring Batch job instead of inline. |
| > 500K nodes | MySQL recursive CTEs hit performance ceiling. Consider materialized path closure table (append-only) for BFS. Evaluate dedicated graph DB (Neo4j) migration — architecture supports this via `KnowledgeGraphPort` swap without domain changes. |

### Scaling Priorities

1. **First bottleneck:** MySQL recursive CTE performance on deep traversals (depth > 4). Fix: `cte_max_recursion_depth` limit + index on `(from_entity_id, weight)` + `(to_entity_id, weight)` composite indexes.
2. **Second bottleneck:** Inline weight recalculation after every GraphRAG query. Fix: async Spring `@EventListener` pattern (already exists in infrastructure — use `ConversationCompletedEvent` as model for `GraphQueryCompletedEvent`).
3. **Third bottleneck:** LLM entity extraction on ingestion is synchronous. Fix: move to Spring Batch step (already have `DocumentIngestionJobConfig` as template).

---

## Anti-Patterns

### Anti-Pattern 1: Creating a Separate `graph` Domain Package

**What people do:** Add `com.localmind.domain.graph` alongside the existing `com.localmind.domain.knowledge`.

**Why it's wrong:** Forces cross-domain imports in the domain layer (`GraphRagService` in `graph` importing `KnowledgeEntity` from `knowledge`). This is the exact violation documented in `MODULE_BOUNDARIES.md` (`AnalyticsService` importing from `document` and `llm`). It also means two separate controller paths, two separate DB adapters, and duplicated wiring in `DomainConfig`.

**Do this instead:** Extend the existing `knowledge` domain. `GraphRagService` lives inside `knowledge/service/`. It is a second orchestration service in the same bounded context.

### Anti-Pattern 2: Storing Graph Node Embeddings in the Document Chunk Qdrant Collection

**What people do:** Reuse the existing `localmind` Qdrant collection (used for document chunks) to also store node embeddings.

**Why it's wrong:** GraphRAG's semantic search phase needs to find nodes, not document chunks. Mixing both in one collection means similarity search returns a blend of chunk vectors and node vectors. This corrupts the GraphRAG anchor selection and makes domain filtering impossible.

**Do this instead:** Create a dedicated `localmind_graph_nodes` Qdrant collection via `GraphSemanticAdapter`. Configure collection name in `KnowledgeGraphProperties`. Keep zero coupling between the two collections at the port level.

### Anti-Pattern 3: Encoding Domain Vocabulary in Java Enums Only

**What people do:** Add `PLACE`, `POI`, `EVENT` to `EntityType` enum and `LOCATED_IN`, `NEAR_BY` to `RelationType` enum to support the Consumer domain.

**Why it's wrong:** The enum approach requires a code change and recompile for every new domain module. It prevents plugins from registering their own vocabularies at runtime. It also forces Consumer and Enterprise types to coexist in the same enum, creating a single monolithic vocabulary instead of per-domain schemas.

**Do this instead:** Enums remain for the built-in, domain-agnostic types (PERSON, CONCEPT, etc.). Domain-specific types are declared by `GraphDomainSchemaExtension` plugins and stored as plain strings in `entity_type`/`relation_type` VARCHAR columns. The service validates against the registered schema at write time.

### Anti-Pattern 4: In-Application BFS for Subgraph Traversal

**What people do:** Keep the existing Java recursive `collectSubgraph()` in `JdbcKnowledgeGraphAdapter`.

**Why it's wrong:** Each recursive call issues a separate SQL query. For depth=3 with 10 nodes at each level, that is 1 + 10 + 100 = 111 separate queries per GraphRAG call. This causes N+1 query explosions and makes it impossible to apply `min_weight` filtering in a single pass.

**Do this instead:** Replace with MySQL 8 `WITH RECURSIVE` CTE. One query traverses the full subgraph up to `maxDepth`, applies `weight >= minWeight` in the JOIN condition, and handles cycle prevention via path string accumulation. Use `nativeQuery = true` in Spring Data.

### Anti-Pattern 5: Synchronous Weight Recalculation in GraphRAG Query Path

**What people do:** After every GraphRAG query, immediately recalculate and persist weights for all traversed edges before returning the answer.

**Why it's wrong:** Adds latency directly to the user-facing query. A single GraphRAG query might traverse 50 edges — 50 UPDATE statements in the critical path.

**Do this instead:** Publish a `GraphQueryCompletedEvent` (domain event) at the end of `GraphRagService.query()`. `GraphWeightUpdateEventListener` (infrastructure, `@EventListener`) processes it asynchronously. Use the same event infrastructure pattern as `ConversationEventListener` (already in the codebase).

---

## Integration Points

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| MySQL 8.0 `knowledge_entities` | Extend `JdbcKnowledgeGraphAdapter` with Flyway migrations V79–V86 (one ALTER per file) | Existing table; additive columns only, no destructive changes |
| MySQL 8.0 `knowledge_relations` | Add `weight`, `domain_id`, `confidence_score`, `usage_count`; add composite indexes | WITH RECURSIVE CTE replaces Java BFS |
| Qdrant `localmind_graph_nodes` | New collection, created by `GraphSemanticAdapter` on first use (auto-create pattern from `QdrantVectorStoreAdapter`) | Vector dimension = EmbeddingModel output dim (same as document chunks) |
| Ollama / EmbeddingModel | Reuse existing `@Primary EmbeddingModel` from `EmbeddingConfig.java` | No new embedding client; inject via port |
| LlmGatewayService | `GraphRagLlmAdapter` calls `LlmGatewayService.chat()` with structured graph context prompt | Reuse provider fallback chain; no new LLM integration |
| PF4J PluginManager | `Pf4jGraphDomainSchemaAdapter` calls `pluginManager.getExtensions(GraphDomainSchemaExtension.class)` | Inject `PluginManager` from infrastructure; not into domain |

### Internal Boundaries

| Boundary | Communication Pattern | Notes |
|----------|-----------------------|-------|
| `GraphRagService` → `LlmGatewayService` | Via `GraphRagLlmPort` → `GraphRagLlmAdapter` → `LlmGatewayService` | Domain must NOT import infrastructure directly; port is the boundary |
| `GraphRagService` → `VectorStorePort` | Direct port injection (document chunks for context enrichment) | VectorStorePort is already a domain port — legal to inject |
| `knowledge` domain ↔ `document` domain | `GraphRagService` may read document chunk IDs via `VectorStorePort`, NOT via `DocumentRepository` — port-level only | Avoids cross-domain model imports |
| `Pf4jGraphDomainSchemaAdapter` → PF4J | Infrastructure layer only; domain sees only `GraphDomainSchemaPort` | PF4J annotation/runtime classes must never appear in `localmind-domain` |
| `KnowledgeGraphService` → `GraphRagService` | They coexist in `knowledge/service/`; no circular dependency — GraphRAG delegates to graph port, not to KnowledgeGraphService | Keep services independent; share ports, not service references |
| `graph.store.ts` → `knowledge.service.ts` | Store calls service methods; service returns Observables; store converts to Signal via `toSignal()` | Follow chat.store.ts pattern |

---

## Build Order

Dependencies between components drive the build order. Each phase should be a shippable increment.

```
PHASE 1 — Schema Foundation (prerequisite for everything)
  V79–V86 Flyway migrations
  → domain model enrichment (add fields to KnowledgeEntity, KnowledgeRelation)
  → JPA entity extension (KnowledgeEntityEntity, KnowledgeRelationEntity)
  → Extend KnowledgeEntityJpaRepository, KnowledgeRelationJpaRepository

PHASE 2 — Weighted Graph Core (depends on Phase 1)
  KnowledgeGraphPort + KnowledgeGraphUseCase extensions
  JdbcKnowledgeGraphAdapter: weight update, domain filter, WITH RECURSIVE path query
  KnowledgeGraphService: weight update + domain CRUD
  API: PATCH /weight, GET /path, GET /domains
  Frontend: weight visible on edges (KnowledgeRelationDto + model update)

PHASE 3 — Graph Semantic Layer (depends on Phase 1, independent of Phase 2)
  GraphSemanticPort (new port/out)
  GraphSemanticAdapter (new Qdrant adapter)
  Hook into KnowledgeGraphService.indexText() to trigger embedding storage

PHASE 4 — Plugin Domain Schemas (depends on Phase 1, independent of Phases 2-3)
  GraphDomainSchemaExtension (new localmind-plugin-api extension point)
  GraphDomainSchemaPort (new port/out)
  Pf4jGraphDomainSchemaAdapter (new infrastructure adapter)
  Domain validation in KnowledgeGraphService.createEntity/createRelation
  Consumer module JAR skeleton + Enterprise module JAR skeleton

PHASE 5 — GraphRAG Service (depends on Phases 2 + 3)
  GraphRagUseCase (new port/in)
  GraphRagLlmPort (new port/out)
  GraphWeightCalculatorPort (new port/out)
  GraphRagLlmAdapter, UsageWeightCalculatorAdapter
  GraphRagService (domain service, no Spring)
  DomainConfig wiring for GraphRagService
  GraphQueryCompletedEvent + GraphWeightUpdateEventListener (async weight update)
  API: POST /api/v1/graph-rag/query, GET /suggest/{nodeId}

PHASE 6 — Frontend Graph Visualization (depends on Phase 2 REST API)
  npm install cytoscape @types/cytoscape
  graph.store.ts (Signal store)
  graph-canvas component (Cytoscape.js, force-directed, edge width = weight)
  graph-filter-panel component
  node-detail-panel component
  graph-page route

PHASE 7 — Frontend GraphRAG Chat (depends on Phase 5 REST API + Phase 6 store)
  graph-rag.service.ts
  graphrag-chat component (query input + cited path display)
  graphrag-page route
```

---

## Sources

- Codebase inspection: `localmind-backend/localmind-domain/knowledge/`, `localmind-infrastructure/knowledge/`, `localmind-plugin-api/` — HIGH confidence (direct code read)
- [MySQL 8.0 Recursive CTEs — Official MySQL Blog](https://dev.mysql.com/blog-archive/mysql-8-0-1-recursive-common-table-expressions-in-mysql-ctes-part-four-depth-first-or-breadth-first-traversal-transitive-closure-cycle-avoidance/) — HIGH confidence (official MySQL documentation)
- [What is GraphRAG? — Neo4j](https://neo4j.com/blog/genai/what-is-graphrag/) — MEDIUM confidence (vendor blog but authoritative on GraphRAG pattern)
- [GraphRAG Survey — arXiv 2501.00309](https://arxiv.org/abs/2501.00309) — HIGH confidence (academic survey)
- [Cytoscape.js — Official](https://js.cytoscape.org/) — HIGH confidence (official documentation)
- [Spring AI Qdrant Vector Store — Official Docs](https://docs.spring.io/spring-ai/reference/api/vectordbs/qdrant.html) — HIGH confidence (official Spring AI docs)
- [RAG in 2026 for Java Developers — Medium](https://medium.com/@elammarisoufiane/rag-in-2026-architecture-shifts-emerging-patterns-and-what-it-means-for-java-developers-6f2803e39787) — MEDIUM confidence (community article, recent)

---

*Architecture research for: LocalMind Knowledge Graph Engine + GraphRAG*
*Researched: 2026-06-29*
