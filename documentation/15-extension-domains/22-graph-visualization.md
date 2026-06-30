# Graph Visualization

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This scope belongs to the **core group** of LocalMind's universal knowledge graph engine. While the consumer verticals (tourism, events, dining) and enterprise verticals (software architecture, knowledge base, mail) define *which* nodes and relationships populate the graph, the **Graph Visualization** scope defines *how the human being sees, navigates, and explores* that weighted graph. It is the visual and interactive surface of the engine: the place where the abstract graph — typed nodes, weighted edges, subgraphs, paths — becomes a navigable, understandable, and actionable experience for the user, and where the AI's answers (GraphRAG) find a verifiable visual counterpart.

Visualization is not an aesthetic accessory but a **component of trust and understanding**: when the AI answers "service A is impacted by the change to API B through the orders database," the user must be able to see that path, expand it, filter it, and confirm it. Visualization is the bridge between the AI's reasoning and human verification, and it is cross-cutting across all domains: the same viewer renders a tourist itinerary, a software dependency chain, a thread of related emails, or a decision tree, changing only the color scheme, the icons, and the type filters. Everything stays **local-first**: rendering happens entirely in the user's browser, the graph data never leaves the self-hosted instance, and no third-party cloud visualization service is involved.

The central technical challenge of the scope is **performance at scale**: an enterprise knowledge graph can contain tens or hundreds of thousands of nodes, and naive visualization (SVG, DOM) collapses already at a few thousand elements. The most recent industry research (see §6) converges on **WebGL/GPU** rendering, **level-of-detail (LoD)**, **progressive expansion**, and **layout off the main thread (Web Worker)** as non-negotiable pillars for staying interactive beyond the threshold of tens of thousands of nodes.

---

## 1. What we solve (problem & value)

### 1.1 The concrete problem

A knowledge graph is powerful precisely because it captures relationships — but a relationship structure is also **intrinsically difficult to represent and to explore**. Without a carefully designed visualization, the value of the graph engine remains invisible and unusable. The concrete problems this scope addresses are:

- **The opacity of the graph.** A graph stored in MySQL + Qdrant is perfectly queryable by the AI, but for a human being it remains a black box: clusters, hubs, paths, isolated nodes, and communities cannot be perceived. The user has no way to *see* the shape of their own knowledge, to discover non-obvious connections, or to notice gaps. Visualization makes tangible what is otherwise just a data structure.
- **Cognitive overload ("hairball").** The opposite and equally serious problem: showing the *entire* graph at once produces the infamous "hairball" — an unreadable mass of overlapping nodes and edges in which no information emerges. Without complexity-reduction techniques (filters, LoD, progressive expansion, clustering), more data means less understanding, not more.
- **Performance collapse at scale.** Traditional web rendering technologies (SVG, DOM manipulation) degrade dramatically already around 1,000–5,000 elements: frame rates below 10 fps, laggy interactions, browsers that freeze. For an enterprise graph of 100,000+ nodes this approach is simply unusable. A rendering architecture designed for scale from day one is needed.
- **Disorienting navigation.** Exploring a large graph means moving through a space in which it is easy to "get lost": the user expands one node, then another, and after five steps no longer knows where they are or how to go back. Without a minimap, breadcrumbs, navigation history, and anchor points, exploration becomes frustrating.
- **Lack of visual relevance.** Showing all edges with the same thickness and all nodes with the same size wastes the engine's most precious signal: the **weight** of relationships. If visualization does not translate weight (frequency, relevance, criticality, confidence) into visual attributes (edge thickness, node size, opacity, rendering order), the user cannot distinguish the crucial connection from the marginal one.
- **The disconnect between AI and verification.** When the AI in GraphRAG mode answers by citing nodes and paths, the user must be able to move immediately from the textual answer to the visual representation of those nodes and paths, in order to verify and dig deeper. Without this bridge, the AI's citations remain unverifiable strings and trust collapses.
- **Blindness to types and domains.** In a universal engine, the same graph can mix nodes from different domains (a tourist place, a review, a person, an event). Without filters by node type, relationship type, and domain, the user cannot isolate the view they need at that moment.
- **Fragmentation across tools.** Today, anyone who wants to visualize relational knowledge uses separate tools (Gephi, yEd, BI tools, Neo4j plugins), disconnected from their knowledge base and from the AI. LocalMind integrates visualization *inside* the platform, synchronized with ingestion, search, and chat.

The common root of all these problems: **the graph is a high relational-dimensionality structure that the human mind comprehends only through a well-designed spatial representation**, and such a representation must scale, filter, and connect to the AI to be truly useful.

### 1.2 The LocalMind solution

LocalMind provides an **interactive, integrated, local-first graph viewer**, built as a standalone Angular 21 feature with state managed via Signals. Its foundational characteristics:

1. **High-performance rendering (WebGL/GPU).** The rendering engine offloads the drawing of nodes and edges to the GPU, staying fluid (target ≥ 30–60 fps in pan/zoom) up to tens of thousands of visible elements, with a Canvas 2D fallback for environments without WebGL.
2. **Progressive disclosure.** It never starts from the entire graph: it starts from a node or a small subgraph (e.g., the results of a search or the AI's citations) and **expands progressively** by relationships, one hop at a time, keeping complexity under control.
3. **Level-of-Detail (LoD) and clustering.** At low zoom levels, nodes are aggregated into clusters/communities (group labels); as you approach (zoom-in), the clusters "open" revealing the detailed nodes and edges. Detail is a function of the zoom level and the rendering budget.
4. **Weights as a first-class visual signal.** Edge weight and node centrality drive size, thickness, opacity, color, and rendering priority: the eye is guided toward what matters.
5. **Multidimensional filters.** By node type, relationship type, domain, weight range, freshness, provenance — applicable and combinable in real time, with the ability to save reusable "views."
6. **Intelligent and off-thread layouts.** Force-directed (and hierarchical/radial/circular on demand) algorithms computed in a **Web Worker** so as not to block the interface, with progressive stabilization and the ability to "pin" nodes.
7. **Native integration with GraphRAG.** From the AI chat you "jump" to the graph highlighting the cited nodes and paths; from the graph you start a contextual question to the AI about the selected subgraph. Visualization and reasoning are two faces of the same experience.
8. **Local-first and bilingual.** Everything runs in the browser, on the self-hosted instance's data; labels, legends, tooltips, and node/relationship types are bilingual IT/EN according to the project constraints.

### 1.3 The value for stakeholders

| Stakeholder | Value of graph visualization |
|-------------|----------------------------------------|
| **Consumer user (tourism/events)** | Visually explores connected places, itineraries, and experiences, discovers nearby and related POIs by following the edges |
| **Developer/architect (enterprise)** | Sees dependencies, blast radius, cycles, and critical paths; visually verifies the impact-analysis answers |
| **Knowledge worker** | Navigates related documents, emails, decisions, and people; finds non-obvious connections across information silos |
| **Graph curator/editor** | Spots orphan nodes, duplicates, and missing relationships; corrects and annotates directly on the graph |
| **AI user** | Verifies and digs deeper into GraphRAG citations by moving from text to graph and back |
| **Instance administrator** | Monitors the shape, density, and growth of the graph; assesses ingestion coverage and quality |
| **Open source contributor** | Has a reusable, documented, and bilingual visualization component, extensible to new domains |

### 1.4 Responsibility boundaries (what it is NOT)

- **It is not the graph engine.** Visualization *consumes* the graph (nodes, edges, weights, neighborhood and path queries) exposed by the core engine; it does not build it, does not persist it, and does not define the types. It is a consumer of the graph APIs.
- **It is not a BI/dashboard tool.** It does not replace charts, KPIs, and tabular reports; it focuses on *relational* representation (nodes and edges), possibly accompanied by detail panels.
- **It is not a CAD diagram editor.** Although it allows annotation and node pinning, it is not a free-drawing tool: the layout derives from data and algorithms, not from arbitrary manual placement (except for pointed overrides).
- **It does not perform heavy client-side analytics on the whole graph.** Structural queries (paths, N-hop neighborhood, metrics) are delegated to the backend (MySQL); the client receives and renders manageable subgraphs, not the entire dataset.
- **It does not exfiltrate data.** Rendering is entirely browser-side on the instance's data; no library sends the graph to external services.

---

## 2. Personas & target users

| Persona | Description | Primary goal | Key need from visualization |
|---------|-------------|--------------------|---------------------------------------|
| **Consumer explorer** | End user discovering the territory | Find connected places/events/itineraries | Simple, mobile-friendly navigation, map+graph |
| **Enterprise analyst** | Developer/architect | Understand dependencies and impact | Filters by type/domain, weighted paths, N-hop expansion |
| **Knowledge worker** | Professional working on internal knowledge | Connect documents, emails, people, decisions | Search → graph, highlighting, detail panels |
| **Graph curator** | Content editor/moderator | Clean, annotate, correct the graph | Spotting orphans/duplicates, inline editing, visual audit |
| **AI assistant user** | Anyone using GraphRAG chat | Verify the AI's answers | Chat↔graph jump, highlighting of cited nodes/paths |
| **Power user / data explorer** | Advanced analytical user | Discover patterns and communities | Multiple layouts, clustering, visual metrics, saved views |
| **Instance administrator** | Technician managing LocalMind | Monitor graph health and growth | Graph statistics, density, coverage, performance |
| **Frontend developer / contributor** | Extends LocalMind | Reuse/extend the viewer | Standalone component, clear APIs, themes per domain, i18n |
| **User with accessibility needs** | Users with visual/motor limitations | Understand the graph in an accessible way | Keyboard navigation, contrast, alternative textual descriptions |

**Role segmentation** (`auth` domain): **readers** explore in read-only mode; **curators** can annotate, pin, and correct nodes/relationships from the graph; **administrators** configure themes, default layouts, performance thresholds, and the visibility of sensitive subgraphs. The visibility of certain types/domains can be restricted by role (e.g., reserved enterprise subgraphs).

---

## 3. Input requirements

This section defines in detail **what the viewer must receive** to produce a correct, performant experience faithful to the graph. Unlike the ingestion scopes (where the input is the raw data to extract), here the input is threefold: the **already-structured graph data** from the core engine, the **user interactions**, and the **rendering/domain configuration**.

### 3.1 Data input: the subgraph to represent

The viewer **never loads the entire graph**: it operates on **subgraphs** requested from the backend via the graph APIs. Each rendering request receives a normalized and immutable payload with the following minimal and extended structure:

| Category | Minimal fields (MVP) | Extended fields (evolution) |
|-----------|--------------------|---------------------------|
| **Node — identity** | canonical id, node type, label (IT/EN) | alias, short description, namespace/domain |
| **Node — visual** | domain (for color), weight/centrality (for size) | icon, status badge (stale/orphan/new), thumbnail |
| **Node — position** | (optional) pre-computed x,y coordinates | cluster level, anchoring (pin) |
| **Node — metadata** | provenance, last-observation date | domain-specific attributes (key-value) |
| **Edge — identity** | id, source node, target node, relationship type | direction, relationship label (IT/EN) |
| **Edge — visual** | normalized weight (for thickness/opacity) | source confidence, freshness, multiplicity |
| **Subgraph — meta** | total no. of available nodes/edges, no. returned, "truncated" flag | expansion suggestions, pre-computed clusters |
| **Pagination/expansion** | cursor/token to load the next neighborhood | depth reached, remaining budget |

**Principles on data input:**

- **Payload immutability.** Each received subgraph is treated as immutable; transformations (filters, layout, highlights) produce *new* derived views, never in-place mutations — consistent with the project's immutability rule.
- **Weight normalization.** Edge weight arrives already normalized by relationship type (or accompanied by the parameters to normalize it client-side), so that visual thickness is comparable.
- **Payload budget.** Each response declares whether it was **truncated** (more nodes/edges available than returned) and provides the tokens for progressive expansion, so the user always knows that "there is more."
- **Bilingualism of visual data.** Node labels, relationship labels, and type descriptions arrive in both languages (or with a resolvable i18n key), to respect the IT/EN constraint.
- **Validation at the boundary.** The client validates the payload schema (ids present, edges referencing nodes existing in the subgraph, numeric weights in the expected range) and fails/degrades in a controlled manner on malformed data, never blocking the entire UI.

### 3.2 Interaction input: the user's actions

The experience is driven by interactions, which the viewer translates into view updates or into new requests to the backend:

| Interaction | Effect | Requires backend? |
|-------------|---------|-------------------|
| **Pan / zoom** | Moves and scales the view; activates LoD | No (client) |
| **Click on node** | Selection + opening of the detail panel | Possibly (extended detail) |
| **Double click / "expand"** | Loads and adds the node's neighborhood | Yes (neighborhood query) |
| **Hover** | Tooltip + highlighting of incident edges | No |
| **Text search** | Finds and centers the matching nodes | Yes (semantic/structural search) |
| **Filter application** | Hides/shows by type/domain/weight/freshness | No if already in view; Yes if it requires new data |
| **Path selection (A→B)** | Highlights the weighted path between two nodes | Yes (path query) |
| **Collapse/cluster** | Aggregates a subset into a super-node | No (client) |
| **Pin / move node** | Fixes the position, excluding it from the layout | No |
| **"Ask the AI about the subgraph"** | Sends the selected subgraph as GraphRAG context | Yes (chat/LLM) |
| **Annotation/correction (curator)** | Modifies node/relationship | Yes (write to the graph) |
| **Save view** | Persists filters+layout+focus as a reusable view | Yes (preferences persistence) |
| **Export** | Generates PNG/SVG/JSON of the current subgraph | No (client) |
| **Keyboard navigation** | Moves focus between adjacent nodes (accessibility) | No |

### 3.3 Configuration & themes input

- **Domain theme.** Maps node-type → color/icon and relationship-type → edge style, specific per domain (consumer vs enterprise) and customizable by the administrator. Reuse of the theme system (light/dark) already present in the frontend.
- **Default layout.** Initial algorithm (force-directed, hierarchical, radial, circular) and its parameters (forces, distances, iterations), configurable per domain and per view.
- **Performance thresholds.** Maximum number of nodes/edges rendered simultaneously, clustering/LoD activation threshold, target fps, expansion budget size — adaptable to the device.
- **Visibility policies.** Which types/domains are visible per role (`auth`), which subgraphs are restricted.
- **User preferences.** Language (IT/EN), theme, preferred layout, saved views, label density.

### 3.4 Quality constraints on the input

- **Referential consistency of the subgraph:** every edge must point to nodes present in the payload or explicitly marked as "frontier" (expandable).
- **Layout determinism on identical input** (given the same seed): with the same subgraph and parameters, the layout is reproducible, so that the view does not "jump" between refreshes.
- **Controlled degradation:** payload beyond the threshold → automatic activation of clustering/LoD instead of full rendering; absence of WebGL → Canvas fallback; partial data → rendering of what is available with a signal, never a blocking error.
- **Privacy by default:** no graph data sent to external libraries/CDNs; all rendering resources are self-hosted.

---

## 4. Activity flow (step-by-step)

The flow is organized into three macro-cycles: the **access and initial loading cycle** (how you enter the graph), the **interactive exploration cycle** (the heart of the experience), and the **AI integration cycle** (chat↔graph). The **visual curation** cycle for curators is added.

### 4.1 Access and initial loading cycle

1. **Entry point.** The user arrives at the graph in one of four ways: (a) from the dedicated "Graph" feature in the menu; (b) from a search result ("see in the graph"); (c) from an AI answer ("open the cited nodes"); (d) from a domain entity's record (a document, a place, a service → "explore connections"). The entry point determines the **seed node or subgraph**.
2. **Seed request.** The frontend asks the backend for the initial subgraph: the seed node + its first-level neighborhood (1-hop), filtered by the default types/domains and limited by the node budget. Never "the entire graph."
3. **Validation and normalization.** The received payload is validated (schema, referential consistency, weights) and normalized into immutable structures ready for rendering; labels are resolved in the current language (IT/EN).
4. **Layout computation (Web Worker).** Node positions are computed off the main thread (force-directed or declared layout), with progressive stabilization: the user sees the graph "settle" without the interface freezing. If the payload includes pre-computed coordinates, the first cycles are skipped.
5. **First rendering (GPU).** Nodes and edges are drawn via WebGL; weight drives size/thickness/opacity, domain drives color, type drives the icon. The bilingual legend is shown.
6. **Orientation.** The minimap, zoom/fit controls, seed breadcrumb, and the "N nodes shown of M available" counter appear. The user knows where they are and how much more exists.

### 4.2 Interactive exploration cycle (the heart)

1. **Overview observation.** At low zoom, the user sees the *shape* of the subgraph: clusters, hubs (large/highly connected nodes), thick edges (strong relationships), peripheral nodes. The LoD shows cluster labels, not individual node labels.
2. **Focusing (hover/zoom).** As you approach an area, clusters open progressively; hovering over a node highlights its edges and dims the rest (highlight + context fade), reducing visual noise.
3. **Selection and detail.** Clicking on a node opens a side panel with its attributes (type, domain, description, provenance, freshness) and the available actions (expand, pin, ask the AI, go to the domain record).
4. **Progressive expansion.** The "expand" command loads the node's neighborhood (new query to the backend), which is **integrated** into the existing graph with fluid animation; the new nodes enter respecting filters and budget. The user thus builds, hop by hop, exactly the subgraph they need — the antidote to the hairball.
5. **Dynamic filtering.** At any time the user applies/combines filters: by **node type** (e.g., only "Document" and "Person"), **relationship type** (e.g., only "CITES"), **domain** (e.g., only enterprise), **weight range** (e.g., edges with weight > threshold, to reduce noise), **freshness** (hide the stale ones). Filters transform the view instantly (client) or require new data when necessary.
6. **Path search.** By selecting two nodes and asking for a "path," the backend computes the weighted path and the viewer highlights it (the other elements fade), visually answering "how is A connected to B?".
7. **Complexity reduction.** When the view gets crowded, the user can **collapse** branches into super-nodes, activate **clustering** by community, raise the weight threshold, or go back in the **navigation history** (undo/redo of the exploration). The minimap and "fit to screen" prevent disorientation.
8. **Layout customization.** The user can change the algorithm (radial for hierarchies, circular for cycles, force for free exploration), pin key nodes and exclude them from the simulation to stabilize the view.
9. **Saving and sharing.** The current configuration (focus + filters + layout) can be **saved as a reusable view** or **exported** (PNG/SVG for reports, JSON for reuse), all locally.

### 4.3 AI integration cycle (chat ↔ graph)

1. **From graph to AI.** The user selects a node or a subgraph and asks "explain this / what is impacted / summarize the connections": the selected subgraph is passed as **context** to the GraphRAG assistant (reuse of the `llm`/chat domain).
2. **From AI to graph.** When the AI answers by citing nodes and paths, each citation is clickable: the click opens the viewer with those nodes **already highlighted** and the path traced, allowing immediate verification.
3. **Guided exploration.** The user alternates textual questions and visual exploration: asks, sees, expands, re-asks about the new subgraph. The visualization becomes the shared "workbench" between user and AI.
4. **Downstream actions.** From a view (e.g., a blast radius), actions can be triggered: create a ticket, notify the owners, generate a report (reuse of the `automation`, `messaging`, `agent` domains).

### 4.4 Visual curation cycle (curators)

1. **Visual identification of anomalies.** The curator uses filters and metrics to highlight orphan nodes (degree 0), suspicious duplicates (`SIMILAR_TO` semantic closeness), anomalous hubs, low-confidence relationships.
2. **Inline correction.** Directly from the graph, they annotate, merge duplicates, confirm/refute inferred relationships, correct types/labels — every intervention is versioned and immutable (new revision, not mutation).
3. **Validation.** The changes update the graph via API; domain events propagate the update to other views and caches.

---

## 5. Graph model (node types, relationship types, weighting criteria)

Unlike the domain scopes, visualization **does not introduce its own node or relationship types**: it is agnostic and represents *any* type defined by the domains. Its "model" is therefore a **visual mapping model**: how each node type, relationship type, and weight translates into graphical attributes. It also defines some visual constructs (cluster, frontier, super-node) that do not exist in the graph but serve the representation.

### 5.1 Mapping of node types → visual attributes

| Data dimension | Visual attribute | Criterion |
|---------------------|------------------|----------|
| **Domain** (consumer/enterprise/…) | Color (hue) | Palette per domain, consistent with the themes |
| **Node type** (Place, Document, Person, Service…) | Icon / shape | Iconography per type, bilingual legend |
| **Node centrality / weight** | Size | More connected/critical nodes are larger |
| **Status** (new / stale / orphan / selected) | Border / badge / opacity | Immediate visual signaling |
| **Cluster membership** | Halo / spatial grouping | At low zoom (LoD) |
| **Relevance to the current query** | Saturation / fade | Results and AI citations stand out, context dimmed |

### 5.2 Mapping of relationship types → visual attributes

| Data dimension | Visual attribute | Criterion |
|---------------------|------------------|----------|
| **Edge weight** | Thickness + opacity | Strong relationships thicker/more opaque |
| **Relationship type** | Edge color / dashing | Style per type (e.g., solid vs dashed) |
| **Direction** | Arrow / curvature | Directed edges with an arrowhead; bidirectional ones curved |
| **Source confidence** | Opacity / style | Inferred relationships fainter than observed ones |
| **Freshness** | Desaturation | Stale edges visually "faded" |
| **Multiplicity** (multiple A→B edges) | Curvature/offset | Parallel edges separated for readability |

### 5.3 Purely visual constructs (not present in the graph)

| Construct | Description | Purpose |
|-----------|-------------|-------|
| **Cluster / Community** | Aggregation of related nodes into a labeled super-node | Reduce complexity at low zoom (LoD) |
| **Frontier node** | Node at the edge of the subgraph with neighbors not yet loaded | Indicate expandability ("there is more") |
| **Super-node (collapse)** | Branch collapsed by the user into a single node | Manual density control |
| **Ghost node / placeholder** | Reference to a node not included in the payload | Maintain consistency of frontier edges |
| **Highlighted path** | Sequence of nodes/edges of the A→B path | Visual answer to path queries |

### 5.4 Weighting criteria in visualization

The viewer **does not compute** the weight (it is derived from the engine), but it **translates and uses** it according to these criteria:

- **Non-linear mapping.** Thickness/size follows a scale (logarithmic or percentile) to prevent a few dominant edges from crushing all the others; perception remains readable across wide ranges.
- **Weight threshold as a filter.** The user raises a minimum weight threshold to "prune" the marginal edges and let the load-bearing structure emerge (noise reduction).
- **Rendering priority by weight.** Under budget conditions (too many elements), higher-weight edges/nodes are rendered first; the marginal ones are the first to be aggregated or hidden by the LoD.
- **Weight as a layout guide.** Higher-weight edges exert stronger attraction forces in the force-directed layout, spatially bringing strongly correlated nodes closer (clusters emerge from weight).
- **Normalization per type.** Thickness comparison is meaningful *within* the same relationship type; different types can use distinct scales signaled in the legend.

---

## 6. Data sources & connectors (ingestion)

Visualization **has no ingestion connectors of its own**: its "data source" is LocalMind's **graph engine**, consumed via APIs. This section therefore describes the **consumption interfaces** and the **rendering technology choices** that are the real heart of the scope.

### 6.1 The consumed graph APIs

| API (graph engine port/in) | What it provides to visualization | Use |
|--------------------------------|------------------------------------|-----|
| **Get node + neighborhood (1-hop)** | Seed subgraph for initial loading | Exploration start |
| **Expand node (N-hop, filtered)** | Next neighborhood for progressive expansion | Interactive exploration |
| **Path query (A→B)** | Weighted path between two nodes | Path highlighting |
| **Subgraph by query/search** | Nodes relevant to a semantic/structural search | Search → graph |
| **Pre-computed clusters/communities** | Aggregations for the LoD at low zoom | Complexity reduction |
| **Graph metrics** (degree, centrality) | Attributes for node size/priority | Visual weighting |
| **Node/relationship detail** | Extended attributes for the side panel | Drill-down |
| **Write (curation)** | Annotations/corrections from the graph | Curation cycle |

All queries respect a **budget** (max nodes/edges, depth) negotiated with the client and return truncation metadata for progressive expansion. Heavy structural queries remain on the backend (MySQL, indexes, possible adjacency/closure tables, Caffeine cache); the client never downloads the entire graph.

### 6.2 Choice of rendering technology (the founding decision)

The 2025–2026 industry research on web graph visualization converges on a few solid principles, which guide LocalMind's technology choice:

- **WebGL/GPU beats SVG and Canvas at scale.** Recent comparative studies on datasets from 100 to 200,000 nodes show that SVG (DOM) rendering collapses already at a few thousand elements, Canvas 2D holds better but remains CPU-bound, while **WebGL** (instanced rendering on the GPU) maintains interactivity across tens/hundreds of thousands of elements. For LocalMind, **WebGL is the primary choice**, with **Canvas 2D as a fallback** where WebGL is not available.
- **Mature libraries.** The ecosystem offers battle-tested options: **Sigma.js** (on graphology, WebGL, designed precisely to "render large graphs in the browser"), **Cosmograph** (GPU, hundreds of thousands of nodes/edges), **react-force-graph/force-graph** (WebGL/Canvas, 2D/3D), **G6** and **Cytoscape.js** (feature-rich, excellent at medium scales). The choice favors a **WebGL, open source, framework-agnostic, and self-hostable** library (no mandatory CDNs), integrable into a standalone Angular 21 component — consistent with the local-first and open source constraints. Sigma.js + graphology is the reference candidate for the MVP, with an evaluation of Cosmograph for extreme scales.
- **Layout off the main thread.** Computing the force-directed layout is expensive: it must be run in a **Web Worker** (or GPU-accelerated) so as not to block the interface, with progressive stabilization and the possibility of interruption.
- **Level-of-Detail and clustering.** The literature on "interactive level-of-detail rendering of large graphs" and on filtering/aggregation confirms that showing everything is counterproductive: the graph is organized hierarchically by centrality/community and detail is revealed only where the user looks (viewport-based rendering, out-of-view pruning, canvas layering).
- **Data-driven progressive expansion.** Starting from a seed and expanding on demand is both a UX choice (avoids the hairball) and a performance choice (only what is needed is rendered).

### 6.3 Client-side rendering architecture

| Component | Responsibility | Technology |
|------------|----------------|------------|
| **Graph data store (Signal)** | Immutable state of the subgraph, filters, selection, history | Angular Signals |
| **Render engine** | GPU drawing of nodes/edges, pan/zoom, picking | WebGL (Sigma.js) + Canvas fallback |
| **Layout worker** | Force-directed/hierarchical position computation | Web Worker (graphology layout / ForceAtlas2) |
| **LoD/clustering controller** | Aggregation and detail based on zoom/budget | Client logic + clusters from the backend |
| **Interaction layer** | Events (hover, click, drag, keyboard), tooltips, panels | Angular standalone components |
| **API client** | Subgraph/expansion/path requests | `ApiService` (core) |
| **Theme/i18n provider** | Palette per domain, legends, IT/EN labels | Theme system + `TranslatePipe` |

---

## 7. Features to create, develop, and maintain (MVP → evolution)

This section concretely maps the features, distinguishing what is **created from scratch**, what is **developed by extending** the existing, and what is **maintained**. The feature lives predominantly in the frontend (`localmind-frontend/src/app/features/graph/`), consuming the graph engine APIs; a minimum of backend serves the queries optimized for visualization and for preferences/saved views.

### 7.1 MVP (basic exploratory viewer)

| # | Feature | Type | LocalMind modules involved |
|---|--------------|------|----------------------------|
| 1 | **Standalone Angular `graph` feature** with lazy routing and Signal store | Create | `localmind-frontend` features, layout, app.routes |
| 2 | **WebGL render engine** (nodes/edges/weights) with Canvas fallback | Create | self-hosted WebGL library (Sigma.js/graphology) |
| 3 | **Force-directed layout in Web Worker** with stabilization | Create | Web Worker, graphology layout |
| 4 | **Seed loading** (node + 1-hop neighborhood) | Develop (reuse graph API) | graph engine API, `ApiService` |
| 5 | **Progressive expansion** (expand node, integrate neighborhood) | Create | neighborhood expansion API |
| 6 | **Pan/zoom, fit, minimap, breadcrumb, node counter** | Create | render engine, UI |
| 7 | **Selection + node detail panel** | Create | UI, detail API |
| 8 | **Hover highlight** (incident edges + context fade) | Create | render engine |
| 9 | **Filters by node type / relationship type / domain** | Create | Signal store, filter UI |
| 10 | **Filter by weight range** (anti-noise threshold) | Create | Signal store |
| 11 | **Visual mapping of weights → thickness/size** (non-linear scale) | Create | render engine |
| 12 | **Theme per domain + bilingual IT/EN legend** | Develop (reuse themes + i18n) | theme system, `TranslatePipe`, bilingual enums |
| 13 | **Search → node centering in the graph** | Develop (reuse search) | `search`/graph domain |
| 14 | **AI chat → graph jump** (highlight cited nodes) | Develop (reuse `llm`/chat) | `llm` domain, chat |
| 15 | **Controlled degradation** (no WebGL → Canvas; large payload → basic LoD) | Create | render engine |
| 16 | **Basic accessibility** (keyboard navigation, contrast, textual alts) | Create | UI |

### 7.2 Evolution (scale, intelligence, collaboration)

| # | Feature | Type |
|---|--------------|------|
| 17 | **Advanced Level-of-Detail + clustering by community** | Create |
| 18 | **Multiple layouts** (hierarchical, radial, circular) selectable | Create |
| 19 | **Node pinning + manual position override** | Create |
| 20 | **Path query (A→B) with highlighting** | Create |
| 21 | **Collapse/expand of branches into super-nodes** | Create |
| 22 | **Navigation history (undo/redo of the exploration)** | Create |
| 23 | **Saved views** (focus+filters+layout) and shareable | Develop (reuse preferences) |
| 24 | **Export** PNG/SVG/JSON of the subgraph | Create |
| 25 | **Graph → AI** (sending the selected subgraph as GraphRAG context) | Develop (reuse `llm`) |
| 26 | **Visual curation** (annotation, duplicate merging, inline correction) | Develop (reuse graph write API + `auth`) |
| 27 | **Visual graph metrics** (centrality, communities, SPOF, density) | Create |
| 28 | **3D / VR mode** for immersive exploration of dense graphs | Create |
| 29 | **Visual temporal diff** ("how the graph changed over time") | Create |
| 30 | **Hybrid graph+geographic map view** (consumer/territory vertical) | Create |
| 31 | **Per-domain visualization plugin** (themes/iconographies via PF4J/marketplace) | Develop (reuse PF4J/marketplace) |
| 32 | **GPU layout (compute shader)** for extreme scales (100k+ nodes) | Create |

### 7.3 Maintenance (ongoing)

- Updating the rendering library and its bindings in step with the versions (security, performance, API).
- Periodic tuning of the performance thresholds (node budget, LoD activation, target fps) on real devices.
- Keeping the palettes/iconographies and bilingual legends in step with the new node/relationship types introduced by the domains.
- Continuous profiling and optimization (frame rate, GPU memory, layout stabilization time).
- Recurring cross-browser and accessibility testing; updating the IT/EN documentation and the logs in `Sviluppi/` (project constraint).

---

## 8. AI / GraphRAG use cases

Visualization is the **visual complement of GraphRAG**: it makes verifiable what the AI states and provides the AI with a context selected by the user. All the following cases close the text↔graph loop.

| Use case | User↔AI↔graph interaction | Role of visualization |
|------------|------------------------------|-----------------------------|
| **Citation verification** | The AI answers by citing nodes/paths | Click on the citation → graph with highlighted nodes/path |
| **Question about the selected subgraph** | The user selects nodes and asks "explain/summarize" | The selected subgraph becomes GraphRAG context |
| **Visual impact analysis** | "What is impacted by X?" | The blast radius (weighted N-hop expansion) is shown as a highlighted subgraph |
| **Discovery of non-obvious connections** | "What connects A and B?" | Weighted A→B path traced visually |
| **AI-guided exploration** | "Guide me through the orders domain" | The AI proposes the entry nodes, the user explores them visually |
| **Suggestion of missing connections** | The AI proposes `SIMILAR_TO` relationships | The suggestions appear as dashed edges to confirm in the graph |
| **Cluster summary** | "What does this cluster represent?" | The selected cluster is summarized by the AI, with citation of the member nodes |
| **Onboarding narration** | "Explain this part of the system to me" | The AI narrates while the graph highlights the cited nodes step by step |
| **Anomaly detection** | "Are there orphan nodes or anomalous hubs?" | The metrics visually highlight the outliers in the graph |

The local AI (Ollama by default, multi-provider fallback) generates explanations, summaries, and suggestions; semantic search (Qdrant) identifies the entry nodes; deterministic navigation (MySQL) builds the subgraphs; visualization makes them explorable and verifiable — all local-first.

---

## 9. KPIs & success metrics

| Category | KPI | Indicative target |
|-----------|-----|-------------------|
| **Performance** | Frame rate in pan/zoom on a "typical" graph (≤ 5k visible nodes) | ≥ 45–60 fps |
| **Scalability** | Nodes/edges renderable while staying interactive (WebGL) | ≥ 50,000 elements |
| **Responsiveness** | Time to first rendering of the seed (1-hop) | < 1 s |
| **Responsiveness** | Time to expand a node (neighborhood) | < 500 ms |
| **Layout** | Layout stabilization time (typical graph) | < 2 s, without UI freeze |
| **Usability** | Completion rate of the task "find the connection between A and B" | > 85% |
| **Adoption** | % of sessions that use the graph / that move from chat to graph | growing |
| **AI verification** | % of AI citations opened and verified in the graph | > 50% |
| **Accessibility** | Conformance to WCAG criteria (contrast, keyboard) | level AA on the key flows |
| **Privacy** | Graph data sent to external rendering services | 0% |
| **i18n** | Coverage of labels/legends/types translated IT/EN | 100% |

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---------|---------|-------------|
| **Performance collapse on large graphs** | Unusable UI, abandonment | WebGL/GPU rendering, LoD, progressive expansion, viewport culling, node budget |
| **Cognitive overload ("hairball")** | Unreadable graph, no insight | Start from a seed, never from the entire graph; filters, clustering, weight thresholds, context fade |
| **Layout that blocks the main thread** | Frozen interface | Layout computation in a Web Worker, progressive stabilization, interruption |
| **Disorientation during exploration** | Frustration, loss of context | Minimap, breadcrumb, undo/redo history, fit-to-screen, pin |
| **Absence/instability of WebGL** | Blank screen on certain devices | Canvas 2D fallback, capability detection, controlled degradation |
| **Dependency on an external library** | Lock-in, vulnerabilities, upstream abandonment | Self-hosted open source library behind a replaceable abstraction (render engine port) |
| **Inconsistency between graph and view** | Distrust | Immutable payloads, validation at the boundary, deterministic layout, controlled refreshes |
| **Data leakage to external CDNs/services** | Local-first/privacy violation | All resources self-hosted, no external calls, 100% client-side rendering |
| **Backend load from frequent expansions** | Slowness, DB overload | Query budget, cache (Caffeine), debounce, targeted pre-fetch of hot neighbors |
| **Neglected accessibility** | User exclusion, non-conformance | Keyboard navigation, contrast, textual descriptions, non-visual-only alternatives |
| **Explosion of indistinguishable types/colors** | Unreadable legend | Curated palettes per domain, type grouping, filters, interactive legend |

---

## 11. Maintenance & evolution

Maintenance follows the project constraints: small, cohesive files (focused standalone Angular components, < 400 typical lines), state immutability (Signals with derived views, never in-place mutations), always up-to-date IT/EN documentation, and logs in `Sviluppi/` with dated nomenclature. The render engine must be kept behind an abstraction (port) so as to be able to replace the underlying library without rewriting the feature.

**Evolution lines:**

1. **From medium scale to extreme scale.** From standard WebGL rendering to **GPU layout** (compute shader) and partitioning, to exceed the threshold of 100k+ nodes while maintaining interactivity — re-evaluating libraries such as Cosmograph for extreme cases.
2. **From 2D to immersion.** 3D/VR mode for dense graphs, where the third dimension reduces overlap and improves cluster perception.
3. **Domain-specialized visualizations.** Hybrid graph+geographic map view for the territory (consumer), C4-level view for software architecture (enterprise), timeline for email threads — distributable as **visualization plugins** via PF4J/marketplace.
4. **From representation to collaboration.** Shared views, collaborative annotations, multi-user exploration sessions on the same subgraph.
5. **Proactive visual intelligence.** The AI automatically proposes the most useful view for a question (layout, filters, focus), highlights anomalies, and suggests the next nodes to explore.
6. **Temporal diff.** Visualization of the graph's evolution over time (what appeared/disappeared/changed), useful both for the territory (events) and for the enterprise (architectural drift).

---

## 12. Integration with existing LocalMind modules

The scope is by nature **cross-cutting**: it is the visual surface of all domains and plugs into the hexagonal architecture and the feature-driven frontend already present.

| Existing module / domain | Role in the Graph Visualization scope |
|----------------------------|------------------------------------------|
| **`knowledge` / core graph engine** | Single source of data: nodes, edges, weights, neighborhood/path queries |
| **`llm` / chat** | Chat↔graph cycle: clickable citations, questions about the subgraph, narration (Ollama default) |
| **`vectorstore` (Qdrant)** | Semantic search that identifies the entry nodes to center in the graph |
| **`search`** | Search → centering/highlighting of the nodes in the viewer |
| **`auth`** | Reader/curator/admin roles and visibility of sensitive types/subgraphs |
| **`automation` / `messaging` / `agent`** | Downstream actions triggered by a view (tickets, owner notifications, reports) |
| **`document`, `email`, `calendar`, `mcp`, …** | Provide the domain nodes; from an entity's record you open "explore in the graph" |
| **`plugin` (PF4J) + `marketplace`** | Themes, iconographies, and specialized visualizations distributed as plugins |
| **`common` (events, exceptions)** | Graph update events that invalidate views/caches; typed error handling |
| **Angular 21 frontend** | Standalone `graph` feature, Signal store, WebGL render engine, Web Worker, IT/EN i18n |
| **Theme system + `TranslatePipe`** | Palette per domain (light/dark) and bilingual legends/labels |
| **`core/services/api.service.ts`** | HTTP client for the graph APIs (seed, expansion, path, detail) |
| **MySQL + indexes/cache** | Efficient execution of the neighborhood/path queries supporting expansion |
| **Caffeine cache** | Cache of hot subgraphs/neighborhoods to reduce the load during expansion |

In summary, the **Graph Visualization** scope is the **user's window onto the universal knowledge graph engine**: it transforms an otherwise opaque relational data structure into an exploratory, performant, and verifiable experience, valid indifferently for the consumer territory and for enterprise knowledge. It fully reuses the LocalMind infrastructure (Angular 21 frontend, graph APIs on MySQL+Qdrant, local Ollama AI, PF4J, themes, and i18n) and respects the constraints of local-first, privacy, open source, and IT/EN bilingualism. It is the core piece that makes the graph not only queryable by the AI, but also **understandable, navigable, and trustworthy for the human being**.
