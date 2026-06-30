export const content = `# GraphRAG & AI

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

## 1. What We Solve (Problem & Value)

### 1.1 The Heart of the Platform: Making AI Reason Over the Graph

This area is not a domain vertical (tourism, events, knowledge base…): it is the **cross-cutting engine** that gives meaning to all the others. It is the capability defined as the project's *core value* in \`PROJECT.md\`: «the AI must be able to navigate a weighted knowledge graph to answer complex questions and surface non-obvious connections — in any domain, while staying local-first. If everything else fails, this must work». GraphRAG & AI is that "this". Every consumer or enterprise vertical produces nodes and edges; this area is the way artificial intelligence **reads, traverses, enriches, and cites** that graph to generate grounded answers.

The problem we solve arises from the now well-documented limits of the two most widespread retrieval paradigms when used alone:

- **Vector search alone (classic RAG) is not enough.** LocalMind already has an embedding and semantic search pipeline on Qdrant, excellent for finding the fragments *most similar* to a question. But semantic similarity is blind to **relationships**. It finds the chunks that "talk about" a topic, not the chunks that, chained together, *answer* a multi-step question. Questions like «if I update the payments API, which services and procedures are affected?» or «which restaurants near Saturday's event serve vegan cuisine and accept dogs?» are not similarity questions: they are questions of **path** in a graph. Pure vector RAG retrieves disconnected fragments, leaves the LLM the (fragile) task of stitching them back together, and often hallucinates the missing connection.
- **The graph alone (structured queries) is not enough.** A graph of nodes and edges answers «which nodes are connected to X with relation Y» very well, but it does not understand **natural language** nor the semantic nuance of the question. It does not know that "production release" and "deploy" are the same thing, nor that "romantic place for dinner" maps to a subset of POIs. On its own, the graph requires the user to already know the exact structure and terminology.

The consolidated answer in 2026 — and ours — is the **hybrid GraphRAG approach**: combining semantic retrieval (Qdrant) and traversal of the weighted graph (MySQL), merging the two sets of results, and giving the LLM a **connected and traceable** context instead of a bag of disconnected fragments. Vector search identifies the right **entry points** into the graph (the semantic "anchors"); the traversal expands those points along the relevant relations, following the weights; fusion and re-ranking produce a compact and pertinent context; the LLM generates the answer **citing the nodes and paths** actually used.

### 1.2 The Three Capabilities This Area Delivers

The GraphRAG & AI area delivers three tightly intertwined capabilities, all explicitly required by the "AI over the graph (GraphRAG)" section of \`PROJECT.md\`:

1. **Hybrid graph exploration (traversal + semantic retrieval).** The AI answers complex questions by combining graph relations and semantic search. Multi-hop questions become **traversals**, not guesses. The system automatically chooses how much semantics and how much graph are needed based on the question (adaptive routing).
2. **Suggestion of missing / non-obvious connections.** The AI does not merely read the graph: it **enriches** it. It proposes probable edges between nodes that are not connected today ("this architectural decision seems to explain why this procedure exists: shall I link them?", "this event and this venue recur together in reviews: a relationship?"). It is *link prediction* driven by LLM + structural signals, always mediated by human curation.
3. **Answers with citation of nodes and paths.** Every answer is **grounded and traceable**: it indicates the source nodes, the path followed in the graph, the weights of the traversed edges, and the confidence level. If there is no grounding, the AI says so instead of making things up. This cuts down hallucinations and makes the answer verifiable — a requirement both consumer (trust) and enterprise (compliance, audit).

### 1.3 Why It Is Strategic (and Why Hybrid, and Why Local-First)

| Need | Limit of vector RAG alone | Limit of the graph alone | How GraphRAG & AI satisfies it |
|---|---|---|---|
| Multi-hop questions ("what depends on X?") | Disconnected fragments, hallucinated connection | Structured queries and exact terminology required | Semantic anchoring + weighted traversal + fusion |
| Natural language understanding | Good | Absent | Embeddings to map the question onto the right nodes |
| Verifiable / anti-hallucination answers | Cites chunks, not relations | Cites nodes, not text | Cites **nodes + path + weights + chunks** |
| Surfacing non-obvious connections | Not supported | Only already-inserted connections | Link prediction (semantics + structure + LLM) |
| "Global" / overview questions | Local fragments | No synthesis | Community detection + per-community synthesis |
| Data privacy | Depends on the model | — | **Local Ollama by default**, no cloud sending without consent |
| Infrastructure reuse | Vector DB | Often requires Neo4j | **Reuse of MySQL (structure) + Qdrant (semantics)**, no Neo4j |

LocalMind's differentiator versus mainstream GraphRAG stacks (Microsoft GraphRAG, LightRAG, and the like) is the combination: **hybrid + local-first + open source + bilingual + without new graph infrastructure**. The reference stacks often assume cloud LLMs and/or a dedicated graph database; we obtain the same power by reasoning over the MySQL + Qdrant already present, with local Ollama AI by default and cloud fallback only optional and consensual. This is what makes graph exploration practicable *on-premise*, where enterprise data cannot leave, and *at zero license cost* for community-driven consumer scenarios.

## 2. Personas & Target Users

GraphRAG & AI is infrastructure: its direct users are partly the **end users** of the verticals (who consume answers), partly the **technical and governance roles** that configure and curate it.

| Persona | Profile | Primary needs | How they use GraphRAG & AI |
|---|---|---|---|
| **End user (consumer/enterprise)** | Looks for answers, knows nothing about the graph | Fast, grounded answers in natural language | Asks questions in chat; receives cited answers; explores the graph from the answer's links |
| **Power user / analyst** | Wants to understand *why* an answer | Traceability, path exploration | Inspects cited nodes/paths/weights; poses complex multi-hop questions |
| **Curator / knowledge manager** | Governs graph quality | Validate suggested links, fill gaps | Works the queue of AI-suggested links (confirm/reject), monitors confidence |
| **Developer / integrator** | Builds verticals on the engine | Stable retrieval and traversal APIs, extensibility | Uses the GraphRAG port/in; exposes the graph via MCP; adds strategies via PF4J plugin |
| **Prompt/AI engineer** | Optimizes answer quality | Control over routing, prompts, thresholds, re-ranking | Configures retrieval strategies, fusion parameters, citation templates |
| **IT admin / self-hoster** | Installs and governs on-prem | Local-first, privacy, model costs, observability | Chooses provider (Ollama default), sets privacy policies, watches latency/costs |
| **Auditor / compliance (enterprise)** | Verifies grounding and traceability | Knowing which sources an answer comes from | Consults citations, path, node versions |
| **Open source contributor** | Extends the engine | Clear documentation, extension points | Contributes retrieval strategies, connectors, GraphRAG improvements |

The lead user for the MVP is the pair **end user asking multi-hop questions** + **curator validating connections**: together they close the loop "question → cited answer → feedback → better graph".

## 3. Input Requirements

This section defines in detail **what the GraphRAG engine must receive** to function. Unlike the verticals, here the inputs are not primarily documents to ingest (those already arrive as nodes/edges from the ingestion areas), but **the question, the graph to reason over, the AI behavior configuration, the user context, and the feedback**. All inputs must be validated at the boundary ("never trust external data" principle) and treated immutably.

### 3.1 The User's Question (Query)

It is the triggering input. It must be normalized and analyzed before retrieval:

| Element | Description | Validation / handling |
|---|---|---|
| Question text | Natural language (IT/EN and beyond) | Maximum length, sanitization, language detection |
| Detected language | To choose embeddings and answer language | Auto-detect with manual override |
| Query type/intent | Pointed factual, multi-hop, global/overview, exploratory | Classified by the router (see §4) to choose the strategy |
| Mentioned entities | Candidate nodes cited in the question | Extracted via NER/LLM, hooked to graph nodes (entity linking) |
| Explicit filters | Node/relation type, domain, time range, geographic area | Schema-validated; applied as traversal constraints |
| Previous conversation | History for follow-up questions | Reuse of the conversation (\`llm\` domain), limited window |

### 3.2 The Graph to Reason Over

The engine consumes — does not produce — the graph structure, provided by the ingestion areas and the core engine:

- **Typed nodes** with attributes, provenance metadata, version, freshness, authoritativeness, and **ACL/visibility** (who can see the node).
- **Weighted and directed edges** with relation type, normalized weight (0–1), and inspectable weight components (see §5 and the core engine document).
- **Node/chunk embeddings on Qdrant**, aligned to nodes on MySQL via a common identifier (UUID with \`@JdbcTypeCode(SqlTypes.CHAR)\`), so that a semantic result can be "promoted" to a traversal entry point.
- **Traversal indexes** on MySQL (on \`source_node_id\`, \`target_node_id\`, \`relation_type\`, \`weight\`) to make multi-hop paths practicable without a dedicated graph DB.

### 3.3 User Context, Identity, and Permissions

GraphRAG must produce **permission-filtered** answers: a critical input is therefore the authorization context.

- **User identity** from the existing \`LocalAuthFilter\` (JWT-like token).
- **Effective ACLs**: the set of nodes/edges visible to the user, inherited from the sources (enterprise) or from the publishing policies (consumer).
- **Fail-safe rule**: in case of doubt about visibility, the most restrictive rule prevails; an unauthorized node must never appear either in the answer or in the citations.

### 3.4 AI Behavior Configuration

Parameters that govern *how* the engine explores and answers. All with reasonable defaults and overridable (per domain, per user, per request):

| Parameter | What it controls | Suggested default |
|---|---|---|
| LLM provider and model | Answer generation + extraction | Local Ollama (\`LLM_DEFAULT_PROVIDER\`) |
| Embedding model | Vectorization of query and nodes | Ollama embedding (\`@Primary\`) |
| Retrieval strategy | Semantic only / graph only / hybrid / global | Adaptive hybrid |
| Semantic \`top_k\` | How many anchors to retrieve from Qdrant | Configurable (e.g. 8–20) |
| Traversal depth (hops) | How many jumps from the entry node | Limited (e.g. 2–3) for latency |
| Maximum fan-out per node | How many neighbors to expand per node | Limited to avoid explosion |
| Minimum weight threshold | Edges below threshold ignored in traversal | Configurable |
| Fusion/re-rank strategy | How to combine semantic and graph results | Reciprocal Rank Fusion + reranker |
| Confidence threshold to answer | Below which the AI declares "I don't know" | Configurable, high for enterprise |
| Privacy policy | What may go to a cloud provider | Default: nothing leaves the local environment |
| Citation template | Format of node/path citations | Bilingual IT/EN |
| Answer language | IT/EN or the question's language | Question's language |

### 3.5 Link Suggestion Configuration (Link Prediction)

- **Suggestion aggressiveness**: how many candidate edges to propose and with what minimum probability threshold.
- **Candidate relation types**: to avoid out-of-ontology proposals.
- **Signal sources**: embedding similarity, co-occurrence, structural graph patterns, LLM inference (configurable and weightable).
- **Application mode**: always in the curation queue (never silent auto-application in the MVP).

### 3.6 Feedback (Learning Loop)

Inputs that close the loop and improve the engine over time:

- **Answer evaluation**: thumbs up/down, "off context", "wrong source", indication of the correct source.
- **Path validation**: marking a path as useful/useless — a signal that feeds the weight of the traversed edges.
- **Acceptance/rejection of suggested links**: every decision feeds back into weights and the suggestion model.
- **Hallucination/contradiction reports**: trigger review and lowering of confidence on the sources involved.

### 3.7 Input Validation Rules

- The question is sanitized (length, content) and never used to build non-parameterized queries (injection prevention on both SQL and prompt).
- Configuration parameters are schema-validated with explicit ranges; out-of-range values fail fast with a clear message (IT/EN).
- The permission context is **always** applied before generation: no answer may contain unauthorized nodes.
- All feedback is immutable: it does not overwrite weights, it generates new events/versions from which weights are recomputed.
- By default **no data leaves the local infrastructure**; using cloud providers requires explicit and selective consent.

## 4. Activity Flow (Step-by-Step)

The flow describes the entire GraphRAG cycle, from the question to the cited answer, through to graph enrichment and feedback. It is designed for the MVP, with the evolution points indicated.

### Phase A — Question Understanding (Query Understanding)

1. **Reception and normalization.** The question arrives from the frontend (reusing the existing chat/SSE channel) at the \`/api/v1/knowledge/graph/ask\` controller (or equivalent). The language is detected, the text is sanitized, and the conversational context and the user's permission context are retrieved.
2. **Intent classification (adaptive routing).** A lightweight classifier (heuristics + local LLM) establishes the **query type** and thus the strategy: *pointed factual* (more semantics, little traversal), *relational multi-hop* (deep traversal driven by anchors), *global/overview* (per-community synthesis), *exploratory* (progressive expansion). This is the *Adaptive RAG* pattern: pipeline complexity is matched to question complexity, avoiding "paying" for the traversal when semantics alone suffice.
3. **Entity extraction and linking (entity linking).** The entities cited in the question are identified and mapped onto the real graph nodes (by name, glossary synonyms, embedding similarity). These nodes become the traversal **entry points**.

### Phase B — Hybrid Retrieval

4. **Semantic retrieval (anchors).** The question is vectorized (Ollama embedding) and **Qdrant** is queried for the \`top_k\` semantically closest chunks/nodes, already applying the type/domain/permission filters. These are the semantic anchors.
5. **Graph traversal (relational expansion).** From the entry points (linked entities + semantic anchors promoted to nodes), a weighted traversal is performed on **MySQL**: neighbors within N hops, following edges with weight above threshold, respecting maximum fan-out and maximum depth to control latency. Nodes, edges, and the **paths** that connect them to the question are collected.
6. **Fusion and re-ranking.** The two sets (semantic + graph) are merged with a rank fusion technique (e.g. Reciprocal Rank Fusion) and possibly passed to a reranker. This yields a **single, ordered, connected context**, in which every element carries its provenance and the path that ties it to the question.
7. **Permission filter (security gate).** Before composing the prompt, nodes/chunks/paths not authorized for the user are removed. Fail-safe rule: when in doubt, exclude. No unauthorized element can enter the context or the citations.
8. **Context budget.** The context is compressed within the model's window: higher-weight paths and more pertinent chunks are favored, always keeping the citation references.

### Phase C — Generation of the Cited Answer

9. **Construction of the grounded prompt.** A prompt is assembled containing the question, the connected context (nodes + paths + chunks), and explicit instructions: answer **only** on the basis of the context, **cite** the nodes/paths used, and declare uncertainty if the context is insufficient.
10. **Generation (local LLM by default).** \`LlmGatewayService\` routes to the provider (Ollama by default, optional and consensual cloud fallback). The answer is produced, streamed via SSE when appropriate.
11. **Verification and citation.** The answer's statements are associated with the source nodes/paths, producing **navigable citations**: each citation points to a node (with version, owner, freshness) and, where relevant, shows the **path** followed and the **weights** of the edges. If confidence is below threshold, the AI answers "I do not have sufficient elements" rather than making things up.
12. **Delivery.** The cited answer returns to the frontend; the user can expand each citation, jump to the node, or open the **graph visualization** centered on the answer's nodes.

### Phase D — Graph Enrichment (Link Prediction)

13. **Generation of candidate connections.** Asynchronously (or on request), the engine analyzes the nodes involved and proposes high-probability **missing edges**, combining: embedding similarity (nearby but unconnected nodes), structural patterns (triangle closure, co-occurrence), and LLM inference ("these two nodes seem to be in a relationship of type X").
14. **Scoring and threshold.** Each candidate receives a probability and a rationale; below threshold it is discarded.
15. **Curation queue.** Candidates above threshold land in the curator's queue, **never applied automatically** in the MVP. The curator confirms, corrects the type, or rejects.

### Phase E — Feedback and Learning

16. **User evaluation.** The user rates the answer and, if wrong, indicates the correct source.
17. **Feedback onto weights.** Paths that produced useful answers reinforce the weight of their edges; links confirmed in curation increase in weight, rejected ones are penalized/zeroed. The computation is immutable (a new weight version).
18. **Adaptation of routing and thresholds.** The metrics (usefulness, latency, "I don't know" rate) feed the optimization of thresholds and strategies. The graph becomes richer and the paths more reliable over time (learning from use).

### Synthetic Flow Diagram

\`\`\`text
User question (NL, IT/EN)
   │
   ▼
[A] Query understanding ─ detect language ─ classify intent (routing) ─ entity linking
   │
   ▼
[B] Hybrid retrieval
   ├─ Semantic (Qdrant, top_k)  ─┐
   ├─ Graph traversal (MySQL, N hop, weight≥threshold) ─┤─► Fusion (RRF) + re-rank
   │                                                 │
   └────────────────────────► Permission filter (fail-safe) ─► Context budget
   │
   ▼
[C] Generation (Ollama default) ─► CITED answer (nodes + path + weights + chunks)
   │                                   │
   │                                   └─► Graph visualization centered on the answer
   ▼
[D] Link prediction ─► candidates (semantics+structure+LLM) ─► CURATION queue (no auto-apply)
   │
   ▼
[E] Feedback (vote/correct source/path validation) ─► weight recomputation (immutable) ─► better graph
\`\`\`

## 5. Graph Model (Node Types, Relation Types, Weighting Criteria)

GraphRAG & AI is **domain-agnostic**: it reasons over any node/relation type defined by the core engine and the verticals. However, it introduces some **technical types of its own** needed to make the AI traceable, learning-capable, and governable. All types are enums **translated IT/EN** toward the frontend, as per project constraints.

### 5.1 Node Types Specific to the Area

In addition to the domain nodes (which the area consumes), the GraphRAG engine materializes "service" nodes to support retrieval, traceability, and learning:

| Node type | Description | Key attributes |
|---|---|---|
| **Chunk (Fragment)** | Vectorized text segment, bridge between graph (MySQL) and semantics (Qdrant) | embedding_id (Qdrant), parent node, position, language |
| **Query / Question** | Question posed, preserved for analysis and learning | text, language, classified intent, user, timestamp |
| **Answer** | Generated answer, with its references | text, confidence, provider/model, user vote |
| **Citation** | Link between a portion of the answer and the source node/path | source node, path, weight, position in the text |
| **Path** | Sequence of nodes/edges traversed to answer | ordered nodes, edges, overall weight, length (hops) |
| **Community** | Cluster of densely connected nodes, with synthesis | members, thematic synthesis, level/resolution |
| **Suggested Link** | Candidate edge proposed by the AI awaiting curation | nodes, proposed type, probability, rationale, status |
| **Concept / Term (glossary)** | Ontology entry to normalize entities and synonyms | definition, synonyms, acronyms, language |
| **Embedding Anchor** | Link between question entity and real node (entity linking) | term, anchored node, confidence |

### 5.2 Relation Types Specific to the Area

| Relation | From → To | Meaning |
|---|---|---|
| **represents** | Chunk → Domain node | the fragment belongs to/describes the node |
| **anchors_to** | Query → Node (entry point) | the question's entity is hooked to a real node |
| **traverses** | Path → Edge/Node | the path includes this edge/node |
| **cites** | Answer/Citation → Node/Path | the answer is grounded on this source |
| **generated** | Query → Answer | the question produced the answer |
| **evaluated_as** | User → Answer | feedback (useful/not useful/off context) |
| **suggests** | AI → Suggested Link | proposal of an edge to validate |
| **belongs_to_community** | Node → Community | membership in the cluster |
| **synonym_of / defines** | Concept → Node/Term | ontological normalization |
| **similar_to** | Node ↔ Node | semantic proximity (weight from embedding similarity) |

### 5.3 Criteria for Edge Weight

The weight (0–1) is the **compass of the traversal**: it determines which edges the AI follows first and which it ignores. It is a configurable combination of factors, each inspectable and recomputable immutably (a new computation creates a new version, it does not overwrite). For the area's technical edges, additional factors apply beyond those of the core engine:

| Factor | What it measures | Effect on weight |
|---|---|---|
| **Semantic similarity** | Closeness of embeddings between nodes | greater similarity → greater weight (for \`similar_to\`) |
| **Useful traversal frequency** | How many times the edge is in a \`Path\` that produced answers voted useful | useful use reinforces the weight |
| **Human validation** | Confirmation/rejection in curation of a \`Suggested Link\` | confirmation increases, rejection zeroes/penalizes |
| **Suggestion confidence** | Probability estimated by link prediction | high-probability candidates start with higher weight |
| **Authoritativeness/freshness of sources** | Quality and currency of connected nodes | official and fresh sources weigh more |
| **Answer feedback** | Votes on answers that used the edge | votes calibrate the weight |
| **Temporal decay** | Age of the edge/relation | old and unused connections decay |
| **Node centrality** | Structural importance (e.g. highly connected/critical node) | increases the weight of the relations involved |

Computation rules:
- Weights are **recomputable** in batch (job scheduled via the \`automation\` domain) and in real time upon feedback arrival.
- **Temporal decay** prevents old paths from staying dominant; recent usage frequency counteracts it.
- Each component remains **inspectable**, so the AI can explain *why* it followed a certain path ("edge confirmed by the owner, high similarity, path used 23 times with a positive outcome"). This explainability is an integral part of the cited answer.

## 6. Data Sources & Connectors (Ingestion)

GraphRAG & AI **does not ingest directly** from external sources: it consumes the graph produced by the ingestion areas (tourism, corporate knowledge base, etc.) and the existing document pipeline. Its "sources" are therefore the **internal artifacts** that feed the reasoning, plus the **feedback flows** that improve it.

| Internal source | Where it comes from | Role for GraphRAG |
|---|---|---|
| **Domain nodes and edges** | Core engine + verticals (consumer/enterprise) | Structure to traverse over |
| **Chunk/node embeddings** | \`DocumentIngestionPipelineService\` pipeline → Qdrant | Semantic anchors and similarity |
| **Metadata and provenance** | MySQL (JPA entities) | Citations, versions, freshness, ACL |
| **Glossary / ontology** | Domain configuration | Entity normalization (entity linking) |
| **Conversation history** | \`llm\` domain | Follow-up and multi-turn context |
| **User feedback** | Frontend (votes, correct source) | Learning of weights and routing |
| **Domain events** | \`common\`/\`event\` (SpringDomainEventPublisher) | Trigger for weight recomputation, indexing, link prediction |

Connectors and extension points specific to the area:
- **Pluggable retrieval strategies** (semantic only, graph only, hybrid, global) as extensions — natural candidates for a new PF4J extension point alongside the existing ones (\`LlmProviderExtension\`, \`VectorStoreExtension\`).
- **Exposing the graph as MCP tools**: the engine can publish "query the graph", "expand neighbors", "find path" as MCP tools, making GraphRAG usable by external agents (reuse of the \`mcp\` domain).
- **Incremental embedding sync**: when a node changes, the chunk and its embedding must be realigned (idempotency, no duplicates).

## 7. Features to Create, Develop, and Maintain (MVP → Evolution)

The table distinguishes **CREATE** (new), **DEVELOP/EXTEND** (on an existing base), and **MAINTAIN** (careful reuse). The area is the *core AI* that leans on the graph engine and the existing domains.

### 7.1 MVP (First Useful Release)

| Feature | Action | LocalMind components involved |
|---|---|---|
| GraphRAG orchestrator (question→cited answer pipeline) | CREATE | new domain service in \`knowledge\`/\`agent\`, port/in \`GraphRagUseCase\` |
| Query understanding: language + intent classification (adaptive routing) | CREATE | domain service + LLM via \`LlmGatewayService\` (Ollama) |
| Entity linking (hooking the question's entities to nodes) | CREATE | domain service + Qdrant similarity + glossary |
| Semantic retrieval (reuse) | MAINTAIN | \`QdrantVectorStoreAdapter\`, Ollama EmbeddingModel \`@Primary\` |
| Weighted graph traversal on MySQL (neighbors, paths, N hops) | CREATE | traversal port/out, repository adapter, Flyway indexes (one query/file) |
| Fusion + re-ranking (RRF) of semantic and graph results | CREATE | pure domain service |
| Pre-answer permission filter (inherited ACLs, fail-safe) | CREATE | authorization service + \`LocalAuthFilter\` |
| Answer generation with citation of nodes/paths/weights | CREATE/DEVELOP | \`LlmGatewayService\`, bilingual citation template, chat/SSE reuse |
| "I don't know" model below confidence threshold (anti-hallucination) | CREATE | logic in the orchestrator |
| API: \`ask\` (GraphRAG), \`expand\` (neighbors), \`path\`, \`subgraph\` | CREATE | controller \`/api/v1/knowledge/graph/*\`, DTO, mapper |
| Base link prediction (semantics + structure) with curation queue | CREATE | domain service + curation UI; no auto-application |
| Graph visualization centered on the answer (base) | CREATE | standalone Angular feature (lazy), Signal store |
| Feedback on answer and path → weight recomputation | CREATE | domain events + job (\`automation\`), immutable weights |
| Enums translated IT/EN (intent, node/relation type, suggestion status) | MAINTAIN | \`TranslatePipe\`, bilingual enums |

### 7.2 Evolutions (Subsequent Releases)

| Feature | Action | Notes |
|---|---|---|
| Community detection + per-community synthesis (global/overview questions) | CREATE | graph clustering (e.g. Leiden-like) + LLM synthesis; \`Community\` nodes |
| Advanced LLM-driven link prediction (zero/few-shot on triples) | DEVELOP | richer proposals with rationale; adaptive thresholds |
| Pluggable retrieval strategies via PF4J plugin | CREATE | new extension point alongside the existing ones |
| Exposing the graph as MCP tools for external agents | DEVELOP | reuse of the \`mcp\` domain (WebMVC server) |
| Agentic GraphRAG (multiple iterative search steps over the graph) | CREATE | reuse of the \`agent\` domain; deep multi-step search |
| Interactive path explanation (why this answer) | DEVELOP | UI: highlights path, weights, sources |
| Caching of frequent paths/answers | DEVELOP | existing Caffeine; invalidation on graph update |
| Dedicated re-ranker (local cross-encoder) | DEVELOP | higher fusion quality, always local-first |
| Contradiction detection between sources in answers | CREATE | comparison of cited sources + flagging |
| Automatic answer quality evaluation (offline eval) | CREATE | dataset of questions/answers, grounding metrics |
| Time-travel over the graph (answering "how it was") | DEVELOP | leverages immutable versioning of nodes/weights |

### 7.3 To Maintain Carefully (Known Risks)

- **MySQL traversal performance**: targeted indexes, limited depth and fan-out, cache; Neo4j stays out of scope but can be reassessed if the queries demand it.
- **MySQL UUID mapping** (\`@JdbcTypeCode(SqlTypes.CHAR)\`) on all new entities (Path, Citation, Community, Suggested Link).
- **Boundary between domains**: the orchestrator touches \`llm\`, \`knowledge\`, \`mcp\`, \`agent\` — use dedicated port/out, avoid direct cross-domain imports (see \`MODULE_BOUNDARIES.md\`).
- **Flyway one query per file**: the technical node schema requires many small, atomic migrations.
- **Wiring in \`DomainConfig\`**: the new services stay pure (zero Spring), registered as \`@Bean\`.
- **Prompt injection / SQL injection**: parameterized queries and prompt sanitization; the graph context must not be able to alter the system instructions.

## 8. AI / GraphRAG Use Cases

1. **Multi-hop dependency question (enterprise).** «If I update the payments API, which services and procedures are affected?» → anchoring on the API, traversal of \`depends_on\`/\`consumes\`/\`documents\`, answer with the list of touched nodes, their owners, and the **cited path**.
2. **Relational discovery (consumer/territory).** «What do I do Saturday evening near the center, with a vegan dinner and a free event?» → semantics for intent + traversal of \`near\`/\`takes_place_in\`/\`suitable_for\`, cited itinerary with the POI/event nodes.
3. **Grounded factual question.** «What is the valid procedure for the release?» → retrieval of the **valid** version (not the obsolete one), citation of document, section, version, owner, and freshness.
4. **Global / overview question.** «What are the recurring themes in the last quarter's incidents?» → community detection + per-community synthesis, without retrieving thousands of individual nodes.
5. **Suggestion of missing connections.** The AI proposes «this decision (ADR-42) seems to explain *why* this procedure exists: shall I link them?» with rationale and probability, to be validated in curation.
6. **Find the expert / point of reference.** «Who knows about OAuth authentication in our stack?» → traversal of \`is_expert_in\` with ranking by authoritativeness and recent activity.
7. **Path explanation (explainability).** On request, the AI makes explicit the path followed and the edge weights: «I followed the edge confirmed by the owner, an official source, a path recently used with a positive outcome».
8. **Answer with uncertainty declaration.** When the context is insufficient or below the confidence threshold, the AI answers «I do not have sufficient elements in the graph» instead of hallucinating, possibly flagging the gap to the curators.
9. **Progressive exploration.** From a question's outcome, the user expands a node's neighbors, filters by type/relation, and navigates the subgraph, reusing the traversal engine in interactive mode.
10. **GraphRAG via agent/MCP.** An external agent uses the MCP tools ("query the graph", "find path") to integrate graph reasoning into broader workflows, while staying local-first.

## 9. KPIs & Success Metrics

| Category | KPI | Goal / direction |
|---|---|---|
| AI effectiveness | % of answers rated useful (thumbs up) | high and growing |
| Grounding | % of answers with valid citations; appropriate "I don't know" rate vs hallucinations | high citations, declining hallucinations |
| Multi-hop | % of multi-hop questions resolved correctly | growth |
| Retrieval quality | Precision/recall of the retrieved context (offline eval) | high |
| Enrichment | No. of suggested links, % accepted in curation | growth with high precision |
| Learning | Improvement of answer usefulness over time (use → weights) | positive trend |
| Performance | End-to-end latency (p50/p95), average traversal depth | within thresholds |
| Privacy | % of queries served locally (Ollama) without cloud sending | maximize (≈100%) |
| Adoption | Questions/day, active users, % that explores the graph from citations | growth |
| Robustness | Error/timeout rate of MySQL traversal | low |

## 10. Risks & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| **LLM hallucinations** | Distrust, wrong answers | GraphRAG with mandatory citations, "I don't know" below threshold, showing path/weights |
| **MySQL traversal performance** | Latency on multi-hop | Targeted indexes, limited depth/fan-out, cache (Caffeine), efficient RRF; Neo4j reassessable |
| **Context explosion** (too many nodes) | Cost/latency, declining quality | Context budget, weight thresholds, top_k, re-ranking |
| **Exposure of unauthorized data in citations** | Severe (privacy/compliance) | Pre-answer permission filter, restrictive fail-safe, local-first default |
| **Noisy link prediction** | Polluted graph | Confidence thresholds, mandatory human curation, weight rewards validation |
| **Wrong intent routing** | Unsuitable strategy, weak answers | Fallback to full hybrid, learning from metrics |
| **Prompt/SQL injection** | Security | Sanitization, parameterized queries, separation of instructions/context |
| **Dependence on local Ollama model/quality** | Variable quality on-prem | Configurable models, consensual cloud fallback, eval to choose the model |
| **Boundary violation between domains** | Technical debt | Dedicated port/out, respect for \`MODULE_BOUNDARIES.md\`, pure services |
| **Curation cost** | Unsustainable maintenance | Prioritized queues, batch suggestions, focus on the most-used nodes |

## 11. Maintenance & Evolution

- **Continuous care of weights and paths.** Scheduled jobs (\`automation\` domain) recompute weights from feedback and use, apply temporal decay, and realign the embeddings of modified nodes. Retrieval quality improves with use (learning from use).
- **Systematic evaluation (eval).** Maintain a reference set of questions/answers and grounding/precision metrics to measure regressions at every evolution of the engine or model change, with JUnit tests (backend) and Vitest/Playwright (frontend).
- **Immutable versioning.** Every answer, citation, path, weight, and suggested link is versioned: the history is preserved for audit, explainability, and learning.
- **Governed extensibility.** New retrieval strategies, re-rankers, and connectors arrive as PF4J plugins; the technical ontology (intent, technical types) evolves in a controlled and bilingual way.
- **Observability.** Latency, depth, "I don't know" rate, % served locally, and citation quality metrics exposed via Actuator/Prometheus; a dashboard for AI engineers and curators.
- **Bilingual documentation.** Constant updating of the IT/EN documentation and tracking of developments in the \`Sviluppi/\` folder with dated nomenclature, as per CLAUDE.md.
- **AI roadmap.** From hybrid GraphRAG (MVP) → community/global search → iterative agentic GraphRAG → dedicated local re-ranker → time-travel over the graph, driven by metrics and real demand.

## 12. Integration with Existing LocalMind Modules

| Module / component | Role in the GraphRAG & AI area |
|---|---|
| **\`knowledge\` (domain)** | Natural home of the GraphRAG orchestrator and the graph queries; extends the core engine |
| **\`llm\` + \`LlmGatewayService\`** | Query understanding, answer generation, link prediction; local Ollama by default, optional cloud fallback |
| **Qdrant (\`vectorstore\`)** | Semantic retrieval, anchors, similarity for link prediction and \`similar_to\` |
| **MySQL + Flyway** | Graph structure, weighted traversal, technical nodes (Path/Citation/Community/Suggested Link); atomic migrations |
| **Document pipeline (\`document\`)** | Produces the chunks/embeddings that bridge semantics↔graph |
| **\`agent\`** | Agentic GraphRAG (iterative multi-step search) and orchestration of graph tools |
| **\`mcp\`** | Exposes the graph as MCP tools for external agents, while staying local-first |
| **\`auth\` + \`LocalAuthFilter\`** | Identity and basis for the permission filter on answers (security gate) |
| **\`automation\`** | Scheduled jobs: weight recomputation, decay, batch link prediction, embedding realignment |
| **\`common\` (events, analytics)** | Domain events for side-effects (weights, indexing) and effectiveness metrics |
| **\`messaging\`** | Publication of the GraphRAG assistant in external channels |
| **\`marketplace\` + PF4J plugins** | Installable retrieval strategies, re-rankers, and connectors (extensibility) |
| **Existing Chat/SSE** | Streaming response channel for cited answers |
| **Angular frontend (standalone feature)** | Assistant with navigable citations, graph visualization centered on the answer, exploration and curation; IT/EN i18n, Signal store |

The area **introduces no new infrastructure** (no Neo4j in this cycle): it reuses MySQL (structure and traversal) + Qdrant (semantics), respects the hexagonal architecture (pure domain services wired in \`DomainConfig\`), stays local-first with Ollama AI by default, applies the permission filter before every answer, and always produces **cited and traceable** answers, entirely bilingual IT/EN. It is the engine that makes LocalMind's central promise real: letting the AI navigate a weighted knowledge graph to answer complex questions and surface non-obvious connections, in any domain.
`;
