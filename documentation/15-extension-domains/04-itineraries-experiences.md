# Itineraries & Experiences

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

## 1. What we solve (problem & value)

### 1.1 The concrete problem

Planning a trip, an excursion, or even just an afternoon of discovering the local area
is today a **fragmented, manual, and cognitively costly** activity. Anyone who wants to live
an authentic experience must manually cross-reference dozens of heterogeneous sources: blogs,
reviews, maps, event calendars, opening hours, weather, transport, local
social groups. The result is almost always one of these three failures:

1. **Generic and touristy itineraries** — mainstream platforms always propose
   the same ten "must-see" POIs, pushing everyone into the same places at the
   same times, with flattened experiences and overtourism at the hotspots while the
   real territory remains invisible.
2. **Information overload without synthesis** — the user finds *too much* data but no one
   who **connects** it into a coherent, feasible route tailored to their real constraints
   (time, budget, mode of transport, presence of children, accessibility, season).
3. **Loss of local knowledge** — the most valuable knowledge (the producer who
   opens the cellar only by reservation, the scenic trail known to locals, the
   village festival) lives in people's heads and in informal channels, and is never
   structured in a reusable way.

Current generative "AI trip planners" partly mitigate the problem but suffer from a
structural limitation: **they hallucinate**. A pure LLM invents times, distances, nonexistent places,
or events that never happened, because it reasons on the probability of text and not on a
verified and connected knowledge base. For trip planning — where a wrong time
or a closed POI ruins the day — this is unacceptable.

### 1.2 Our answer: itineraries native to the graph

LocalMind tackles the problem in a radically different way, leveraging its
**universal Knowledge Graph** engine. An itinerary is not randomly generated text, but a
**real, weighted path on the knowledge graph of the territory**: an ordered sequence
of nodes (places, POIs, events, experiences) connected by weighted edges (geographic proximity,
thematic affinity, walkability/traversability, historical co-visitation, complementarity). The AI (GraphRAG)
**navigates** this graph to build routes, and every proposed stop is **anchored to a
verified and citable node** — no inventions.

| Approach | How it generates the itinerary | Hallucination risk | Local knowledge |
|-----------|--------------------------|-----------------------|-------------------|
| Static tourist lists | Fixed, manual ranking | None but generic | Absent |
| Pure generative AI (LLM) | Probabilistic text | High | Unreliable |
| **LocalMind (GraphRAG on the graph)** | Weighted path on verified nodes | Low (each stop is a real node) | Structured and community-driven |

### 1.3 Generated value

- **For the traveler/citizen**: personalized itineraries in a few seconds, feasible,
  truly tailored to their own constraints and interests, with the discovery of authentic experiences
  off the beaten path.
- **For the territory**: redistribution of flows toward minor places, valorization of
  producers, artisans, small events; the fight against overtourism at the hotspots.
- **For the community**: local knowledge is structured once and reused
  infinite times; those who contribute see their own knowledge "live" inside the itineraries.
- **For LocalMind as a platform**: the "Itineraries & Experiences" scope is the **living
  proof of the consumer vertical** and the most expressive testing ground for the graph engine —
  an itinerary is literally a weighted, multi-constraint *path query*.
- **Consistency with project constraints**: everything runs **local-first** with **Ollama as
  default**; no user data (preferences, location, trips) leaves the
  self-hosted installation without consent. No costs for mandatory cloud APIs.

### 1.4 Why now and why us

2026 sees AI trip planners as a mature category but dominated by proprietary,
cloud-only SaaS that monetize users' travel data. LocalMind brings an alternative
that is **open source, self-hostable, and privacy-first**, ideal for: territorial bodies and Pro Loco offices
that want a proprietary discovery portal; communities and associations that map
their territory; individual enthusiasts who want a private planner without tracking.
The reuse of **MySQL (graph structure) + Qdrant (semantics)** avoids introducing a
dedicated graph database, staying within the existing stack.

## 2. Personas & target users

| Persona | Description | Main goal | Typical constraints |
|---------|-------------|----------------------|----------------|
| **Weekend explorer** | Citizen/tourist planning 1-3 day trips | Discover authentic experiences nearby, optimizing the little time available | Limited time, medium budget, own vehicle |
| **Slow/thematic traveler** | Enthusiast of a theme (food and wine, trekking, art, villages) | Coherent and in-depth thematic routes | Strong vertical interest, seasonality |
| **Family with children** | Parents organizing outings | Family-friendly stops, sustainable pace, safety | Accessibility, lunch/rest times, short distances |
| **Local contributor / curator** | Resident or enthusiast who knows the territory | Add and curate places, experiences, events in the graph | Simple editing tools, recognition |
| **Tourism operator / Pro Loco** | Body or association managing a self-hosted instance | Offer an itinerary portal for their own territory | Branding, moderation, on-premise data |
| **Accessibility-first traveler** | Person with mobility or sensory needs | Routes that are truly traversable and inclusive | Stringent accessibility constraints |
| **Power user / privacy self-hoster** | Technical user who rejects travel SaaS | Private planner, local AI, zero tracking | Everything offline/on-prem, Ollama |

The personas drive both the **personalization profiles** (section 3) and the **dynamic
weights** on the edges (section 5): e.g., the family penalizes edges with long travel
times, the thematic traveler amplifies thematic affinity edges.

## 3. Input requirements

The quality of an itinerary depends entirely on the quality and completeness of the inputs.
We distinguish **mandatory** inputs (minimum to generate), **optional** inputs (which improve
personalization), **derived/contextual** inputs (collected automatically), and **system/data
constraints**.

### 3.1 Mandatory inputs (minimum to generate an itinerary)

| Input | Description | Example | Validation |
|-------|-------------|---------|-------------|
| **Area/starting point** | Locality, address, or coordinates of the origin point | "Verona, Piazza Bra" / `45.43,10.99` | Mandatory geocoding; must resolve to a valid node or coordinate |
| **Time horizon** | Overall duration | "1 day", "weekend", "26-28 July" | Coherent dates (start ≤ end), duration > 0 |
| **At least one interest or theme** | What the user is looking for | "food and wine", "easy trekking" | Mapped onto the graph's thematic taxonomy |

### 3.2 Optional personalization inputs

| Category | Input | Effect on the algorithm |
|-----------|-------|------------------------|
| **Budget** | Total or per-stop spending cap, price range | Filter/penalty on nodes and edges with an associated cost |
| **Mode of transport** | On foot, bike, car, public transport | Chooses the time/distance matrix and the traversability of the edges |
| **Pace** | Relaxed / balanced / intense | Number of stops/day, buffer duration between stops |
| **Group composition** | Solo, couple, family (children's ages), group, pets | Activates family/pet-friendly filters, accessibility |
| **Accessibility** | Wheelchair, sensory needs, no stairs | Hard constraint on the accessibility attributes of nodes |
| **Dietary preferences** | Vegetarian, vegan, celiac, halal | Filter on dining nodes |
| **Style/personality** | Adventurous, cultural, gourmet, photographic, off-the-beaten-path | Modulates thematic weights and preference for "hidden" vs popular nodes |
| **Exclusions** | Places/categories to avoid, already seen | Hard exclusion of nodes or subcategories |
| **Must-include** | Stops imposed by the user | Nodes pinned as mandatory waypoints of the path |
| **Time windows** | Constraints (e.g., "back by 7 PM", "lunch 12:30-14") | Temporal constraints on the ordering of the stops |
| **Physical effort level** | Low/medium/high, max elevation gain (trekking) | Filter on the difficulty of routes/experiences |

### 3.3 Contextual inputs (collected automatically, with consent)

| Input | Source | Use |
|-------|-------|-----|
| **Season and weather** | Weather connector / current date | Penalizes outdoor activities in bad weather, favors indoor |
| **Node seasonality** | Graph attributes | Excludes out-of-season or closed POIs/events |
| **Opening hours** | Graph attributes | Temporal constraint: the node must be open within the visit window |
| **User history** | Local profile (if logged in) | Avoids repetitions, learns preferences over time |
| **Real-time location** | Device (opt-in) | Re-planning "from where I am now" |
| **Crowding/concurrent events** | Events graph | Avoids overlaps or recommends based on active events |

### 3.4 System constraints and data quality

- **Minimum node completeness**: a node is eligible as a stop only if it possesses at least
  geolocation, category, and (if applicable) hours. Incomplete nodes are usable only
  as context, not as a proposed stop.
- **Reliability**: each node/edge carries a reliability score (see section 5);
  below threshold, the node is excluded or flagged as "to be verified".
- **Language**: all free-text inputs are accepted in IT/EN; the thematic
  taxonomy is bilingual and the output respects the user's language.
- **Privacy**: profiling inputs remain local; nothing is sent to cloud providers
  without explicit consent. With Ollama as default, even inference is on-premise.
- **Boundary validation**: all inputs pass through schema validation on the REST DTO
  (`@Valid`) before reaching the domain; fail-fast with clear bilingual messages.

### 3.5 Request model (conceptual)

The set of inputs is normalized into an immutable `ItineraryRequest` object with,
among others, the fields: `origin`, `timeWindow`, `themes[]`, `budget`, `transportMode`,
`pace`, `groupProfile`, `accessibility`, `mustInclude[]`, `exclude[]`, `constraints[]`.
This object is the single contract between the API, the domain, and the GraphRAG engine; every
persona profile (section 2) is expressible as a preset of this object.

## 4. Activity flow (step-by-step)

The flow covers the entire cycle from the user's request to the delivery and adaptation
of the itinerary. It is designed to be **idempotent and immutable**: each regeneration
produces a new itinerary object, it does not mutate the previous one (consistency with the
project's immutability rules).

### 4.1 High-level flow diagram

```text
[1] User input (form/chat)          →  ItineraryRequest (validated)
        │
        ▼
[2] Normalization & profiling       →  dynamic weights + hard/soft constraints
        │
        ▼
[3] Candidate retrieval on graph    →  Qdrant (semantics) + MySQL (structure/neighborhood)
        │
        ▼
[4] Weighted subgraph construction  →  candidate nodes + edges with contextual weights
        │
        ▼
[5] Path optimization               →  TTDP/Orienteering: max value under constraints
        │
        ▼
[6] Narrative synthesis (LLM/Ollama) →  descriptions, rationales, node citation
        │
        ▼
[7] Presentation & interaction      →  timeline, map, graph; user modifications
        │
        ▼
[8] Persistence, export, feedback   →  saving, sharing, learning signals
        │
        ▼
[9] Real-time adaptation            →  re-planning on event/location/weather
```

### 4.2 Step detail

**Step 1 — Input acquisition.**
The user fills out a guided form (Angular frontend) or converses in natural language
with the chat ("I'd like a gastronomic weekend in the hills, without driving too much, medium
budget"). In chat mode, the LLM extracts the slots of the `ItineraryRequest` (slot filling) and
asks for clarification only on the missing mandatory fields. The output of the step is a
candidate `ItineraryRequest`.

**Step 2 — Validation, normalization, and profiling.**
The request is validated at the boundaries (DTO schema). The origin is geocoded,
dates/time windows are resolved, free interests are mapped onto the bilingual
thematic taxonomy. The **dynamic weights** to apply to the edges and the **constraints** are computed:
- *Hard constraints* (non-negotiable): accessibility, exclusions, opening hours,
  must-include, budget cap, time window.
- *Soft constraints* (weighted preferences): thematic affinity, pace, popularity vs discovery,
  season/weather.

**Step 3 — Candidate retrieval (GraphRAG, retrieval phase).**
Candidate nodes are retrieved by combining two signals, consistent with the stack:
- **Semantic (Qdrant)**: similarity between the request's embedding and the embeddings of the
  nodes (descriptions of places/experiences) — finds candidates by *meaning*.
- **Structural (MySQL)**: geographic neighborhood (within a radius/bounding box from the origin and
  along the route), filters by category, hours, hard attributes.
The result is a set of candidate nodes already filtered on the hard constraints.

**Step 4 — Weighted subgraph construction.**
From the candidates, a **subgraph** of the territory is built in memory: the nodes are the
possible stops, the edges represent the relationship between pairs of nodes (proximity,
travel time for the chosen mode, thematic affinity, historical co-visitation). Each edge
receives a **composite weight** computed with the dynamic weights from step 2. Edges that are not
traversable (for the chosen mode) or that violate hard constraints are pruned (pruning).

**Step 5 — Path optimization (algorithmic core).**
Selecting and ordering the stops is a **Tourist Trip Design / Orienteering
Problem**: maximize the "value" collected (sum of the scores of the nodes, modulated
by affinity with the profile) **while respecting** the time budget, opening hours
(time windows), the must-includes as mandatory waypoints, and the economic budget. Progressive
implementation strategy:
- **MVP**: greedy heuristic + local improvement (insertion of the node with the best
  value/cost ratio, 2-opt on the order), with transit buffer between stops.
- **Evolution**: metaheuristics (GRASP, variable neighborhood search) for large
  and multi-day instances, generation of **top-k alternative itineraries** rather than just one.
Output: one or more ordered sequences of stops with estimated arrival/departure times.

**Step 6 — Narrative synthesis (GraphRAG, generation phase).**
The LLM (Ollama by default) receives **only the selected nodes and edges** as structured
context and generates: the itinerary title, the description of each stop, the **rationale for
why it was chosen** ("added because it's nearby and on theme with your interest in
wine"), practical tips, and transitions between stops. Anti-hallucination constraint: the model
**cannot introduce stops not present in the selected subgraph**; every factual
statement is anchored to a citable node (provenance). The responses cite the nodes/paths
used.

**Step 7 — Presentation and interaction.**
The itinerary is shown on three synchronized views: **timeline** (day by day,
times), **map** (stops and route), **mini-graph** (stops as nodes and relationships — showcase
of the engine). The user can: remove/lock a stop, ask for an alternative for a
single stop ("replace the lunch with something cheaper"), drag to
reorder, lengthen/shorten the duration. Each modification re-triggers steps
4-6 in a targeted way (local recalculation, not from scratch).

**Step 8 — Persistence, export, and feedback.**
The itinerary can be saved (it becomes itself an **`Itinerary` node** in the graph,
reusable and recommendable to others), exported (PDF, .ics calendar toward the
calendar module, shareable link), and rated. The feedback (ratings, "I've been here", error
reports) feeds the edge weights and node reliability (sections 5 and 11).

**Step 9 — Real-time adaptation (evolution).**
During the trip, a change in weather, the unexpected closure of a POI, a delay, or the
user's current location trigger a **re-planning** of the rest of the day,
preserving the already-completed stops and the future must-includes.

### 4.3 Error handling and edge cases in the flow

| Situation | Behavior |
|------------|---------------|
| Few/no candidates (area poor in nodes) | Progressively widen radius/theme; suggests relaxing a soft constraint; clear bilingual message |
| Irreconcilable hard constraints (e.g., budget too low) | Explains which constraint is blocking and proposes the minimum necessary relaxation |
| Selected node turns out to be closed/unreliable | Excludes it and regenerates only the affected stop |
| LLM unavailable | Fallback in the provider chain; as a last resort, structured output without narrative prose |
| Ambiguous input in chat | Clarification question only on the mandatory fields |

## 5. Graph model (node types, relationship types, weighting criteria)

The model reuses the universal graph engine (MySQL for structure, Qdrant for semantics),
specializing its types for the "territory" consumer domain. The types are **extensible per
domain** according to the modular schema already foreseen.

### 5.1 Node types

| Node type | Description | Key attributes |
|--------------|-------------|------------------|
| **Place / Area** | Locality, village, neighborhood, geographic zone | name, geo (lat/lng/bbox), territorial hierarchy |
| **POI** | Point of interest (monument, museum, viewpoint, trail) | geo, category, hours, price, accessibility, seasonality |
| **Experience** | Activity to be lived (tasting, workshop, guided tour, excursion) | duration, cost, bookability, capacity, difficulty |
| **Event** | Time-bound happening (festival, concert, exhibition) | date/interval, place, recurrence, capacity |
| **Dining / Hospitality** | Restaurants, agriturismos, lodgings, wineries | cuisine type, price range, supported diets, hours |
| **Itinerary** | Saved route (also AI-generated or curated) | sequence of stops, theme, duration, author, rating |
| **Theme / Tag** | Cross-cutting concept (food and wine, art, family, slow) | bilingual IT/EN label, thematic hierarchy |
| **Person / Operator** | Guide, producer, artisan, host (privacy consent) | role, public contacts, experiences offered |
| **Review / Contribution** | Community rating or note | author, rating, text, date, moderation status |

### 5.2 Relationship types (edges)

| Relationship | Between | Meaning | Direction |
|-----------|-----|-------------|-----------|
| **NEAR_TO** | POI/Experience ↔ POI/Experience | Geographic proximity / travel time | Undirected |
| **TRAVERSABLE_TO** | Node → Node | Feasible transit with a mode (time, distance, elevation gain) | Directed (per mode) |
| **HAS_THEME** | Node → Theme | Thematic classification | Directed |
| **COMPLEMENTARY_TO** | Experience ↔ Experience | They pair well in the same route | Undirected |
| **CO_VISITED_WITH** | POI ↔ POI | Often visited together (behavioral signal) | Undirected |
| **PART_OF** | POI/Experience → Itinerary/Area | Belonging/composition | Directed |
| **OFFERED_BY** | Experience → Person/Operator | Who delivers the experience | Directed |
| **TAKES_PLACE_IN** | Event → Place/POI | Localization of the event | Directed |
| **SEASONAL_IN** | Node → Season/Period | Temporal validity | Directed |
| **REVIEWS** | Review → Node | Community rating | Directed |
| **SIMILAR_TO** | Node ↔ Node | Semantic similarity (from Qdrant embeddings) | Undirected |
| **ALTERNATIVE_OF** | Node ↔ Node | Substitutable in an itinerary (same role) | Undirected |

### 5.3 Edge weighting criteria

The weight is the heart of the engine: it governs what the AI chooses. Each edge carries a **composite
weight** normalized in [0,1], computed as a weighted average of factors; the coefficients
are **dynamic** and modulated by the request profile (section 3).

| Weight factor | Applies to | Rationale | Effect |
|-----------------|--------------|-----------|---------|
| **Distance / travel time** | NEAR_TO, TRAVERSABLE_TO | Close stops = efficient route | Closer ⇒ higher weight (preference) |
| **Thematic affinity** | HAS_THEME, COMPLEMENTARY_TO | Coherence with the chosen theme | More affine to the profile ⇒ higher weight |
| **Co-visitation / behavioral signal** | CO_VISITED_WITH | "The wisdom of the crowd" | Normalized co-visit frequency |
| **Relevance / popularity** | any → node | Intrinsic quality of the stop | Balanced with the "discovery" factor |
| **Community feedback** | REVIEWS, derived edges | Ratings, "I've been there", usefulness | Average rating weighted by voter reliability |
| **Reliability / freshness** | all | Verified and updated data | Penalty for old or unverified data |
| **Traversability per mode** | TRAVERSABLE_TO | Coherence with the chosen transport | Edge pruned if not traversable with the mode |
| **Temporal availability** | SEASONAL_IN, hours | Open/in season within the window | Hard constraint; zeroes eligibility otherwise |
| **Diversity / anti-overtourism** | selection | Avoid all-identical routes and saturated points | Penalty on already-saturated or repeated nodes |

Conceptual formula of the preference weight of an edge/node:

```text
weight = Σ (profile_coefficient_i × normalized_factor_i)   ,  with Σ coefficients = 1
```

The **hard constraints** (hours, accessibility, exclusions, budget cap, must-include) are **not**
weights: they act as filters/prunings before optimization. The **soft constraints**
become coefficients in the weight. This sharply separates "what is admissible" from "what is
preferable", making the algorithm predictable and explainable.

### 5.4 Mapping onto storage (reuse of MySQL + Qdrant)

- **MySQL**: tables `graph_node` (with `node_type`, attributes, geo) and `graph_edge` (with
  `edge_type`, `source_id`, `target_id`, `weight`, component factors). Geographic indexes and
  per-type indexes. UUIDs mapped with `@JdbcTypeCode(SqlTypes.CHAR)`. Each Flyway migration contains
  **a single query**.
- **Qdrant**: a collection with the embeddings of the node descriptions, payload with
  `node_id`, `node_type`, geo, and tags for hybrid filtering (vector + filter).
- The edge weights are materialized in MySQL but **recomputable**: the
  behavioral and feedback factors are updated by batch jobs/events (section 11).

## 6. Data sources & connectors (ingestion)

Ingestion populates and enriches the graph, reusing the existing document pipeline
(Tika/OCR → chunking → embedding → Qdrant) and the connector patterns. Each source has an
**adapter** that maps the raw data onto typed nodes/edges.

| Source | Connector type | Nodes/edges produced | Notes |
|-------|-----------------|---------------------|------|
| **Community contributions (UI)** | Manual editing + form | Places, POIs, Experiences, Reviews | Core of the "Wikipedia of places"; goes through moderation |
| **Territorial Open Data** | Importer (CSV/JSON/agency API) | POIs, Events, hours | Municipal/regional catalogs, open tourism datasets |
| **OpenStreetMap / open geo data** | Geo importer | POIs, geometries, NEAR_TO | Geographic base and proximity |
| **Event calendars** | Connector + `calendar` module | Events, TAKES_PLACE_IN | Festivals, concerts, exhibitions |
| **Documents/guides (PDF, sheets)** | Existing `document` pipeline | POIs, Experiences (extracted) + embedding | OCR for scanned material |
| **Sites/feeds (blogs, portals)** | Web fetch/scraping (opt-in) | Candidate POIs/Experiences | With verification and moderation |
| **Weather** | Real-time connector | Context (not persisted as a stop) | Outdoor penalty in step 2/9 |
| **Maps/routing** | Times-distances provider (local or API) | TRAVERSABLE_TO weights | Time matrix per mode |
| **User feedback** | Internal events | Updates weights/reliability | Learning loop |

Ingestion principles: **deduplication/entity resolution** (the same POI from different
sources collapses into a single node with multiple provenances); **incremental
enrichment** (nodes improve over time); **provenance tracking** (each attribute knows where it comes
from, for reliability and GraphRAG citation); **respect for the privacy/licenses** of the sources; everything
executable **on-premise**.

## 7. Features to create, develop, and maintain (MVP → evolution)

### 7.1 MVP (first vertical release "Itineraries & Experiences")

| # | Feature | Layer | Description |
|---|--------------|-------|-------------|
| 1 | Territory domain node/relationship types | Domain + Flyway | Extension of the graph schema with the types from sec. 5 |
| 2 | `ItineraryRequest` + validation | API + Domain | DTO, boundary validation, mapping onto bilingual taxonomy |
| 3 | Hybrid candidate retrieval | Domain + Infra | Qdrant (semantics) + MySQL (neighborhood/filters) |
| 4 | Weighted subgraph construction | Domain | In-memory subgraph with composite weights and pruning |
| 5 | Greedy + 2-opt optimizer (base TTDP) | Domain | Stop selection/ordering under time/budget/hours constraints |
| 6 | GraphRAG narrative synthesis | Domain + LLM | Prose with rationales and node citation, anti-hallucination |
| 7 | Itinerary REST API | API | `POST /api/v1/itineraries` (generate), `GET` (retrieve/saved) |
| 8 | Generation UI + timeline/map views | Frontend | Guided form, result on timeline and map, bilingual IT/EN |
| 9 | Saving itinerary as a node | Domain + Persistence | Itinerary becomes a reusable node |
| 10 | Manual node editing (base contributions) | API + Frontend | Creation/editing of POIs/Experiences with minimal moderation |
| 11 | Base feedback (rating, "I've been there") | Domain + Frontend | Initial signals for the weights |
| 12 | Export .ics / PDF / link | API + Frontend | Integration of the `calendar` module, sharing |

### 7.2 Evolutions (post-MVP)

| # | Feature | Value |
|---|--------------|--------|
| 1 | Full conversational generation (slot filling in chat) | Natural UX, reuse of the chat module |
| 2 | Top-k alternative itineraries + metaheuristics (GRASP/VNS) | Choice among variants, scaling on large areas |
| 3 | Multi-day itineraries with overnight stays | Complex trips |
| 4 | Real-time re-planning (weather/location/closures) | Reliability while traveling (step 9) |
| 5 | Interactive mini-graph view of the stops | Showcase of the engine, exploration |
| 6 | Adaptive personalization (learning from the profile) | Improves over time |
| 7 | Curated thematic routes and community collections | Editorial + community |
| 8 | Advanced open data / OSM / events connectors | Territorial coverage |
| 9 | Advanced moderation/curation + contributor reputation | Quality of the "Wikipedia of places" |
| 10 | Advanced accessibility and certified inclusive routes | Inclusivity |
| 11 | Module installable from the marketplace (PF4J) | Distributable "Territory" package |
| 12 | Itinerary collaboration (multiple users) | Group planning |

### 7.3 To maintain (ongoing maintenance)

- Periodic recalculation of weights (feedback, co-visitation, freshness) via batch/events.
- Data update from connectors and re-embedding of modified nodes.
- Tuning of profile coefficients and evaluation of itinerary quality (sec. 9).
- Update of the bilingual thematic taxonomy and enum translation IT/EN toward the frontend.
- Moderation of contributions and management of error reports.
- Maintenance of connector adapters as external sources change.

## 8. AI / GraphRAG use cases

| Use case | How it uses the graph | Example |
|------------|-------------------|---------|
| **Itinerary generation** | Hybrid retrieval → subgraph → optimization → synthesis | "Gastronomic weekend in the hills, no car, medium budget" |
| **Targeted replacement of a stop** | Path on ALTERNATIVE_OF / SIMILAR_TO edges in the neighborhood | "Replace the lunch with something cheaper" |
| **Complex questions about the territory** | Weighted multi-hop path query | "What do I pair with a cellar visit within 30 min by bike?" |
| **Non-obvious connections** | Suggestion of missing edges / discovery | "Few people know that 10 min away there's also an artisan workshop" |
| **Explainability (why this stop)** | Citation of nodes/paths and weight factors | "Chosen for thematic affinity + proximity + excellent ratings" |
| **Search for authentic experiences** | Boost of the "discovery" factor, overtourism penalty | "Experiences off the tourist circuits" |
| **Contextual re-planning** | Subgraph reconstruction with weather/location constraint | "It's raining: redo the afternoon indoors from where I am" |
| **Itineraries similar to a saved one** | Itinerary node as a seed, SIMILAR_TO | "A route like last year's but in autumn" |

All cases respect the **anti-hallucination** constraint: the LLM reasons only on the
retrieved subgraph and cites the nodes. Inference default **Ollama** (local-first), with
fallback to the existing provider chain when configured.

## 9. KPIs & success metrics

| Category | KPI | Indicative target |
|-----------|-----|-------------------|
| **Adoption** | Itineraries generated / active users | MoM growth |
| **Output quality** | % itineraries saved or exported (vs discarded) | > 40% |
| **Personalization** | Acceptance rate without manual modifications | > 50% |
| **Feasibility** | % valid stops (open, reachable in time) | > 95% |
| **Anti-hallucination** | % statements anchored to verified nodes | ~100% |
| **Discovery** | Share of "non-mainstream" stops proposed | Anti-overtourism KPI |
| **Feedback** | Average itinerary rating, confirmed "I've been there" | Upward trend |
| **Community** | New nodes/reviews per period, % approved | Growth |
| **Performance** | Itinerary generation time (P95) | < a few seconds |
| **Graph coverage** | Node/edge density per area, attribute completeness | Growing |
| **Privacy/local-first** | % generations served by local Ollama | Default 100% on-prem |
| **Data reliability** | % nodes above the reliability threshold | Growing |

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---------|---------|-------------|
| **LLM hallucinations** | False stops/times | GraphRAG with grounding on the nodes; the model cannot add stops outside the subgraph; mandatory citation |
| **Sparse/incomplete data (cold start)** | Poor itineraries in unmapped areas | Seed from open data/OSM; progressive widening; UX that invites contribution |
| **Quality of community contributions** | Spam, errors, obsolete data | Moderation, contributor reputation, reliability/freshness in the weights |
| **Computational complexity (TTDP NP-hard)** | Latency on large/multi-day areas | Pruning, scalable heuristics, cache, incremental computation for modifications |
| **Poorly calibrated weights** | Poorly relevant itineraries | Configurable coefficients, A/B on weights, feedback loop |
| **Amplified overtourism** | Concentration on the usual POIs | Diversity/anti-saturation factor in the weights |
| **Privacy of travel data** | Exposure of preferences/location | Local-first, Ollama by default, no cloud sending without consent |
| **Dependence on external sources/licenses** | Connector breakage, legal constraints | Isolated adapters, license respect, tracked provenance |
| **Travel time accuracy** | Infeasible itineraries | Reliable time matrix per mode; buffer; re-planning |
| **Accessibility treated as soft** | Exclusion of users | Accessibility as a hard constraint, not as a preference |

## 11. Maintenance & evolution

### 11.1 Graph maintenance cycles

- **Weight recalculation**: periodic batch jobs and event listeners update the
  behavioral factors (co-visitation), feedback factors (ratings, confirmations), and freshness factors. Consistent with
  the domain events pattern (`DomainEventPublisherPort` → infrastructure listener).
- **Re-embedding**: modified nodes are re-vectorized and updated on Qdrant.
- **Cleaning and deduplication**: periodic entity resolution to merge duplicates from different
  sources and archive obsolete/closed nodes.
- **Connector update**: monitoring of external sources and maintenance of the
  adapters; new connectors added as modules.

### 11.2 Continuous learning and improvement

- **Feedback loop**: each interaction (saving, modification, rating, "I've been there")
  becomes a signal that recalibrates weights and reliability — the graph improves with
  use.
- **Profile tuning**: the coefficients per persona/profile are configurable and
  evaluable with the KPIs of section 9; possibility of A/B testing on the weights.
- **Editorial curation**: curated thematic routes accompany the generated ones,
  feeding quality examples.

### 11.3 Synthetic evolutionary roadmap

1. **Foundations**: territory graph schema + MVP single-day generation.
2. **Experience**: conversational chat, map/graph views, export, feedback.
3. **Scale**: multi-day, top-k, metaheuristics, real-time re-planning.
4. **Community**: contributions, moderation, reputation, "Wikipedia of places".
5. **Package**: distributable "Territory" module via marketplace (PF4J).

### 11.4 Project maintainability constraints

Small and cohesive files (200-400 lines), immutability (each regeneration produces a new
object), a single query per Flyway migration, pure domain without Spring (wiring in
`DomainConfig`), enums translated IT/EN toward the frontend, bilingual documentation kept up to date,
and developments tracked in the `Sviluppi/` folder.

## 12. Integration with existing LocalMind modules

| Existing module | Role in the "Itineraries & Experiences" scope |
|------------------|--------------------------------------------|
| **knowledge** | Base of the graph engine: hosts territory nodes/edges and exposes the path/neighborhood/subgraph queries |
| **llm** | GraphRAG narrative synthesis and conversational slot filling; Ollama by default, fallback chain for the rest |
| **document** | Ingestion pipeline (Tika/OCR → chunking → embedding) for guides and sheets that become nodes |
| **vectorstore (Qdrant)** | Semantic retrieval of candidates and SIMILAR_TO relationships |
| **mcp** | Exposure of tools (POI search, routing, weather) and consumption of external servers as connectors |
| **calendar** | Export .ics and management of events/time windows in the stops |
| **email/messaging** | Sharing and notification of itineraries; sending channels |
| **agent** | Orchestration of multi-step flows (generate → verify → re-plan) |
| **automation** | Weight recalculation, re-embedding, connector update jobs |
| **marketplace + plugin (PF4J)** | Distribution of the "Territory" module and of connectors as installable extensions |
| **auth** | Local user profile for personalization and contributions, privacy-first |
| **common (events/analytics)** | Domain events for the feedback loop and metrics/KPIs |
| **finetuning** | (Evolution) adaptation of local models to the descriptive style of the territory |

### Technical extension points

- **Domain**: new `itinerary` package (or extension of `knowledge`) with `model/`,
  `port/in/` (e.g., `GenerateItineraryUseCase`), `port/out/` (e.g., `GraphQueryPort`,
  `RoutingPort`), `service/` (optimizer + GraphRAG orchestration), without Spring.
- **Infrastructure**: adapters for graph queries (MySQL), routing/times, data connectors;
  wiring in `DomainConfig`.
- **API**: `ItineraryController` under `/api/v1/itineraries` with validated DTOs.
- **Frontend**: `itineraries` feature (and enrichment of `knowledge`) with form, timeline,
  map, and mini-graph, bilingual IT/EN.
- **Persistence**: tables `graph_node`/`graph_edge` (and itinerary tables) via Flyway
  migrations with a single query each; UUID `CHAR(36)`.

---

*Guiding document for the developments of the "Itineraries & Experiences" scope (consumer
vertical of LocalMind's Knowledge Graph engine). To be updated at each phase advancement.*
