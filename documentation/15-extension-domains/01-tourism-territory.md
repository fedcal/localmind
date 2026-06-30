# Tourism & Territory

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This document describes the **consumer vertical "Tourism & Territory"** built on top of LocalMind's universal Knowledge Graph engine. The goal is not to create a monolithic tourism app, but to instantiate — through node types, relationship types and installable modules — a weighted graph of places and geographic/thematic relationships, navigable by the AI (GraphRAG), which surfaces non-obvious connections between places, experiences and events. Everything stays local-first, self-hostable, with Ollama AI by default, reusing MySQL 8.0 (structure) and Qdrant (semantics), without introducing a dedicated graph database.

---

## 1. What we solve (problem & value)

### 1.1 The problem perceived by the user

Discovering a territory today is dominated by centralized platforms (Google Maps, TripAdvisor, Booking, Instagram) that suffer from deep structural limitations:

- **Homogenization and overtourism.** Ranking engines reward already-popular places: the top results are always the same monuments, restaurants reviewed by thousands of tourists, "Instagrammable" attractions. The result is the concentration of flows onto a few saturated POIs, while 90% of the territory (villages, trails, workshops, local events, authentic experiences) remains invisible.
- **Opaque and gamed ranking.** Ordering is determined by proprietary, non-inspectable algorithms, influenced by advertising, fake reviews and engagement logic. The user does not know *why* a place is suggested, nor can they trust the signal.
- **Keyword search, not relationship search.** Current platforms respond well to "restaurants near me", but fail on relational and contextual queries: *"a weekend motorcycle itinerary among medieval villages with organic wineries and few crowds, reachable without highways"*. The graph of relationships between places simply does not exist for the end user.
- **Closed and non-portable data.** Reviews, lists and preferences are imprisoned within the platform. The user does not own their own data and cannot self-host their own map of territorial knowledge.
- **Lack of structured local knowledge.** The knowledge of those who live in a territory (tourist boards, guides, residents, associations) is fragmented across PDFs, social groups, word of mouth; it is neither queryable nor connected.

### 1.2 The LocalMind solution

LocalMind treats the territory as a **weighted knowledge graph**, not as a list of markers on a map. Every place, event, experience, itinerary is a **typed node**; every connection (proximity, thematic similarity, "paired with", "stop of", "same producer") is a **weighted edge**, where the weight encodes the intensity, quality and reliability of the relationship. On top of this graph the AI navigates (GraphRAG) to answer complex questions, build itineraries and surface the connections that keyword searches cannot see.

The differentiating value is articulated along five axes:

| Value axis | What LocalMind offers | Difference vs Google Maps / TripAdvisor |
|---|---|---|
| **Discovery of hidden places** | Ranking emerging from the community and from graph relationships, not from absolute popularity; ability to make the "hidden gem" dimension explicit | The big players structurally reward already-saturated POIs |
| **Relational navigation** | Exploration of the graph by geographic and thematic relationships; AI answers that explain the path | Keyword search and rigid filters, no navigable graph |
| **Transparency** | Inspectable ranking, explainable edge weight, citation of the nodes/paths used by the AI | Opaque proprietary algorithm, influenced by advertising |
| **Data sovereignty** | Local-first / self-hostable; the municipality, the tourist board or the individual owns and hosts their own graph | Data imprisoned in the cloud platform |
| **Structured local knowledge** | Ingestion of open sources (OpenStreetMap, Wikidata, tourism open data) + community contributions into a queryable graph | Local knowledge scattered and not queryable |

### 1.3 Who benefits and why it matters

- **The curious traveler** finds authentic experiences outside the mass circuits, with itineraries tailor-built by the AI starting from real constraints (time, transport, season, interests).
- **Local communities** (municipalities, tourist boards, DMOs, associations) obtain an open-source and self-hostable tool to enhance their own territory without depending on external platforms or paying commissions, retaining ownership of the data — consistent with the European Open Data Destination projects.
- **The ecosystem** benefits from a community-driven "Wikipedia of places": open data, transparent ranking, reduction of overtourism through redistribution of flows toward lesser-known places.

### 1.4 Alignment with the LocalMind vision

This vertical is the first **consumer** instance of the universal graph engine described in `.planning/PROJECT.md`. It demonstrates that the same engine that serves enterprise knowledge (docs, processes, microservices) can serve territory discovery by changing only the node/relationship schema and the installed modules. It validates the thesis "one platform, many ecosystems" and the reusability of MySQL+Qdrant+GraphRAG.

---

## 2. Personas & target users

| Persona | Profile | Goals | Needs from the system |
|---|---|---|---|
| **Explorer / slow traveler** | Independent tourist, avoids mass destinations, seeks authenticity | Discover villages, trails, workshops, little-known experiences; build tailor-made itineraries | Relational search, AI itineraries, "low-crowd" filter, offline/self-host while traveling |
| **Local explorer / resident** | Lives in the territory, wants to rediscover their own area | Find events, festivals, seasonal openings near home | Event updates, geographic radius, quick contributions |
| **Contributor / local curator** | Guide, blogger, enthusiast, member of a tourist board | Enrich the graph with hidden places, correct data, report relationships | Contribution UI, moderation, attribution, reputation |
| **DMO / Municipality / tourist board operator** | Entity that promotes the territory | Publish official POIs/events, enhance the offering, retain data ownership | Self-hosted instance, open data ingestion, export, branding |
| **Experiential operator** | Winery, agriturismo, hiking guide, craftsman | Make their own experience visible within the territorial context | Experience node profile, relationships with nearby places, events |
| **Developer / integrator** | Builds on top of LocalMind | Extend node types, write connectors and plugins | Graph API, PF4J plugins, SDK, IT/EN documentation |

Primary persona of the MVP: **Explorer** (consumption side) and **Contributor/local curator** (graph feeding side). Without contributors the graph does not grow; without explorers it has no value. The MVP must close both sides of the cycle.

---

## 3. Input requirements

This section is deliberately detailed: it defines *what* must be able to enter the system, from whom, in what form and with which validation constraints, because the quality of the graph depends entirely on the quality of the inputs.

### 3.1 Input from the consuming user (queries and preferences)

The explorer user queries the graph. The inputs to collect and validate:

| Input | Type | Example | Mandatory | Validation |
|---|---|---|---|---|
| Natural language query | free text | "medieval villages with wineries near Siena, few crowds" | Yes (for AI search) | Max length, sanitization, detected language (IT/EN) |
| Reference position | coordinates or place name | GPS lat/lon, "Florence", address | No | Geocoding, valid coordinate range |
| Radius / area of interest | number + unit or bbox | "within 30 km", bounding box on map | No | Positive range, maximum cap |
| Time window | dates / season | "this weekend", "spring" | No | Coherent dates, not in the past for events |
| Means of transport | enum | on foot, bicycle, motorcycle, car, public transport | No | Value in IT/EN-translated enum |
| Available time | duration | "half a day", "3 days" | No | Positive duration |
| Interests / themes | multi-select tags | food & wine, nature, art, family | No | Tags existing in the thematic graph |
| Accessibility constraints | flag | wheelchair accessible, pet-friendly | No | Booleans |
| "Off the beaten path" level | scale 0–100 | preference for hidden gems | No | Range 0–100 |
| Indicative budget | enum/range | free, economical, premium | No | Translated enum |
| Interface language | enum | IT / EN | Yes (default from profile) | IT/EN supported |

All inputs must be validated at the boundary (API controller + Angular reactive form), with localized error messages and fail-fast, as per project rules.

### 3.2 Contributor input (feeding the graph)

The contributor creates and enriches nodes/edges. Input requirements for the creation of a **Place/POI node**:

| Field | Type | Mandatory | Validation notes |
|---|---|---|---|
| Name | text | Yes | Not empty, dedup against nearby nodes |
| Node type | enum (Place, POI, Event, Experience, Itinerary…) | Yes | IT/EN-translated enum |
| Category/subtype | taxonomy | Yes | From controlled taxonomy (aligned to schema.org/OSM) |
| Geographic coordinates | lat/lon | Yes for physical places | Valid range, spatial dedup |
| Description (IT) and (EN) | long text | At least one language | HTML sanitization, length |
| Photo/media | file or URL | No | MIME type, size, declared license |
| Opening hours / seasonality | hours structure | No | Hours format, coherence |
| Thematic tags | multi-tag | No | From thematic taxonomy |
| "Hidden place" indicator | flag/scale | No | 0–100 |
| Accessibility | multiple flags | No | Booleans |
| Source / attribution | text/URL/license | Yes if imported | Compatible license (CC, ODbL…) |
| Proposed relationships | list of typed edges | No | Valid relationship type, existing target node |

Requirements for the creation/proposal of an **edge (relationship)**: source node, destination node, relationship type (enum), direction, any attributes (e.g. distance, travel duration, textual rationale), source. The weight is not entered manually as an arbitrary value but is **derived** (see §5).

Requirements for **reviews/ratings**: target node, score (defined scale), optional IT/EN text, sentiment tag, contributor identity (for reputation and anti-spam), timestamp. Anti-abuse validation: rate limiting, deduplication, reputation thresholds.

### 3.3 Input from automatic sources (ingestion connectors)

See §6 for the connector details. At the input-requirements level, each connector must provide for each record: stable source identifier, type mappable to a LocalMind node type, coordinates (where relevant), mappable attributes, data license, last-update timestamp. The pipeline must handle deduplication (the same place from different sources), reconciliation (attribute merge) and conflicts (priority based on source reliability).

### 3.4 Cross-cutting constraints on inputs

- **Privacy and local-first.** The user's location data and queries do not leave the instance without explicit consent; embedding and AI inference run on local Ollama by default.
- **IT/EN bilingualism.** Every relevant text field and every enum has an IT and EN representation; the enum is translated on the backend and redirected to the frontend based on the language switch.
- **Licenses.** Every imported datum carries its license; the system rejects or flags sources with a license incompatible with the open nature of the project.
- **Minimum quality.** A node does not pass validation if the mandatory fields of its type are missing; ambiguous inputs end up in a moderation queue rather than being silently discarded.

---

## 4. Activity flow (step-by-step)

This section is detailed by explicit request. The three cornerstone flows are described — **discovery/consumption**, **contribution** and **automatic ingestion** — and how they converge into the graph.

### 4.1 Discovery and consumption flow (explorer → AI → graph)

1. **Startup and context.** The user opens the "Discover territory" feature in the Angular frontend. The system detects the language (IT/EN) and, upon consent, the position. State is managed by a dedicated Signal store (`ChatStore` pattern).
2. **Formulating the request.** The user types a natural language query and/or sets the filters of §3.1 (radius, time window, transport, themes, hidden-gem level). The reactive form validates each field at the boundary.
3. **Submission and routing.** The frontend calls `POST /api/v1/territory/discover` (or the graph endpoint) via `ApiService`. The controller delegates to the domain's in-port (e.g. `TerritoryDiscoveryUseCase`).
4. **AI understanding of the query.** The domain service invokes the LLM (Ollama default, existing fallback chain) to extract intent, entities, constraints and themes from the query, normalizing them against the graph taxonomy.
5. **Hybrid retrieval (GraphRAG).**
   a. **Semantic seed:** the query is embedded and searched on **Qdrant** to identify the semantically closest candidate nodes (descriptions, tags, reviews).
   b. **Graph expansion:** from the seed nodes the engine navigates the **edges on MySQL** (geographic proximity, thematic similarity, "stop of", "paired with"), expanding the subgraph within N hops and applying the filters (geo, time, accessibility).
   c. **Weighted ranking:** candidates are ordered by combining edge weight, semantic relevance, community signal (reviews/votes) and the user's hidden-gem preference.
6. **AI synthesis.** The LLM receives the selected subgraph as structured context and generates an answer: place/experience suggestions and/or an itinerary, **with explicit citation of the graph nodes and paths** used (transparency).
7. **Presentation.** The frontend shows the results across three synchronized views: list/cards, map (node markers) and **interactive graph view** (nodes, edges, weight). The user sees *why* a place is suggested.
8. **Progressive exploration.** The user clicks a node to expand its neighbors (related places, connected events, paired experiences), navigates the relationships and refines the filters; each interaction can re-run the GraphRAG query on the subgraph.
9. **User action.** Saving an itinerary, adding to favorites, exporting (e.g. GPX/GeoJSON), sharing. These actions generate **interaction signals** that feed the recalculation of weights (§5).
10. **Feedback loop.** Votes, saves, dwell time and user corrections return as signals that, in batch, update the edge weights and the emergent ranking.

```
NL Query + filters
      │
      ▼
[LLM intent/entities]  ──►  [Qdrant: semantic seed]
                                   │
                                   ▼
                         [MySQL: N-hop graph expansion + filters]
                                   │
                                   ▼
                         [Weighted ranking: edge weight + semantics + community + hidden-gem]
                                   │
                                   ▼
                         [LLM synthesis + node/path citation]
                                   │
                                   ▼
              [List | Map | Interactive graph]  ──►  actions ──► signals ──► weight recalculation
```

### 4.2 Contribution flow (contributor → moderation → graph)

1. **Authentication.** The contributor logs in (existing local-first auth). Their reputation is known to the system.
2. **Node creation.** They fill in the form of a new Place/POI/Event/Experience with the fields of §3.2; the system performs **spatial and semantic dedup** in real time (searches for similar nodes by coordinates and embedding) to avoid duplicates.
3. **Relationship proposal.** The contributor suggests typed edges toward existing nodes (e.g. "this agriturismo is *paired with* this winery", "this village is a *stop of* this itinerary").
4. **Boundary validation.** Mandatory fields, taxonomy, media licenses and geographic coherence are validated; errors are localized and blocking.
5. **Moderation / curation queue.** Depending on the contributor's reputation and the sensitivity of the change, the contribution is published directly or enters a moderation queue. A curator (or automatic rules + AI) approves, modifies or rejects with a rationale.
6. **Immutable persistence.** Upon approval, the node/edge is written to MySQL (structure) and its description embedded on Qdrant (semantics), following the immutability pattern (new versions, no in-place mutations; revision history).
7. **Attribution and reputation.** The contribution is attributed to the author; approval and subsequent positive votes increase their reputation, which weighs on future contributions (§5).
8. **Weight propagation.** The addition of nodes/edges and the votes recalculate the weights of the involved relationships and update the emergent ranking.

### 4.3 Automatic ingestion flow (open sources → pipeline → graph)

1. **Trigger.** A Spring Batch job (reuse of the existing folder-scan/document-ingestion pattern) starts on schedule or on-demand for a connector (e.g. OpenStreetMap of an area, Wikidata, DMO open data).
2. **Extraction.** The connector retrieves the raw records (Overpass API, Wikidata SPARQL, GeoJSON/CSV files, schema.org feeds).
3. **Mapping.** Each record is mapped to a LocalMind node type and to a category of the controlled taxonomy; the attributes are normalized (IT/EN).
4. **Deduplication and reconciliation.** Spatial + semantic comparison with existing nodes; attribute merge with priority based on source reliability; preservation of all sources/licenses.
5. **Relationship inference.** Automatic generation of edges: geographic proximity (radius/travel distance), thematic similarity (embedding), membership (POI inside a city/area).
6. **Embedding and persistence.** Descriptions embedded on Qdrant via Ollama (EmbeddingModel `@Primary`); nodes/edges persisted on MySQL.
7. **Initial weight computation.** Weights derived from source factors (data completeness, authoritativeness, distance) as a baseline, then refined by community signals.
8. **Ingestion report.** Logs and metrics (new nodes, merges, conflicts, discards) exposed via Actuator/dashboard.

### 4.4 Convergence into the graph and virtuous cycle

The three flows feed the **same weighted graph**. Ingestion provides the coverage base; the community adds the hidden places and the qualitative judgment; consumption generates the interaction signals that make the best content emerge. The AI leverages all of this to answer and suggest, and every useful answer reinforces the cycle. It is the mechanism that allows hidden places to emerge *without* mechanically rewarding absolute popularity.

---

## 5. Graph model (node types, relationship types, weighting criteria)

The model reuses the engine's generic schema (typed nodes + weighted edges on MySQL, semantics on Qdrant) and specializes it for the territory domain. The taxonomy is aligned, where possible, to open standards (schema.org `TouristAttraction`/`TouristTrip`/`Event`, OpenStreetMap, Wikidata) to foster interoperability and ingestion.

### 5.1 Node types

| Node type | Description | Key attributes | Standard alignment |
|---|---|---|---|
| **Place / Area** | Container geographic entity (city, village, district, valley, park) | name, geometry/centroid, administrative hierarchy | OSM boundary, schema.org `Place` |
| **POI / Attraction** | Point-level point of interest (monument, museum, viewpoint, church, natural site) | coordinates, category, hours, hidden-gem score | schema.org `TouristAttraction` |
| **Establishment / Venue** | Restaurant, winery, agriturismo, workshop, bar | coordinates, cuisine/product, price range, hours | schema.org `FoodEstablishment`/`LocalBusiness` |
| **Event** | Festival, fair, exhibition, concert, market | date/time window, recurrence, location | schema.org `Event` |
| **Experience / Activity** | Tasting, guided hike, workshop, tour | duration, difficulty, seasonality, transport | schema.org `TouristTrip`/activity |
| **Itinerary / Route** | Ordered sequence of stops (trail, motorcycle/bike route) | length, duration, elevation gain, track geometry | schema.org `TouristTrip`, GPX |
| **Theme / Tag** | Cross-cutting concept (food & wine, medieval, family, slow) | IT/EN label, hierarchy | controlled taxonomy |
| **Local product** | Food & wine or craft specialty of the territory | name, category, provenance | schema.org `Product` |
| **Person / Operator** | Guide, producer, craftsman, contributor | role, reputation, contacts (privacy-aware) | schema.org `Person`/`Organization` |
| **Review / Rating** | Judgment on a node | score, IT/EN text, author, date | schema.org `Review` |
| **Media** | Photos/videos/audio associated with a node | URL, license, author | — |

The types are **extensible per domain** (modular schema): a "mountain territory" module can add "Refuge", "Via ferrata"; a "wine tourism" module can add "Denomination/DOC".

### 5.2 Relationship types (edges)

| Relationship type | From → To | Direction | Semantics | Example |
|---|---|---|---|---|
| **NEAR** | any → any | bidirectional | geographic / travel proximity | POI near another POI |
| **LOCATED_IN** | POI/Establishment/Event → Place/Area | directional | membership/containment | Museum inside a city |
| **STOP_OF** | POI/Establishment → Itinerary | directional | the node is a stop of the route | Village as a stop of a route |
| **PAIRED_WITH** | Establishment/Experience ↔ Establishment/Experience | bidirectional | experiential complementarity | Winery paired with an agriturismo |
| **SIMILAR_TO** | any ↔ any | bidirectional | thematic/semantic similarity | Two similar medieval villages |
| **HAS_THEME** | any → Theme/Tag | directional | thematic classification | POI with theme "art" |
| **HOSTS_EVENT** | Place/POI → Event | directional | the place hosts the event | Square that hosts a festival |
| **PRODUCES / OFFERS** | Establishment/Operator → Product/Experience | directional | offering | Winery that produces a wine |
| **MANAGED_BY** | Establishment/Experience → Person/Operator | directional | ownership | Tour managed by a guide |
| **REVIEWS** | Review → target node | directional | rating | Review of a restaurant |
| **CONTRIBUTED_BY** | node/edge → Person (contributor) | directional | attribution/provenance | POI created by a curator |
| **REACHABLE_BY** | POI/Itinerary → transport (attribute/node) | directional | transport accessibility | Trail reachable on foot |

### 5.3 Criteria for edge weight

The weight is a normalized value (e.g. 0–1) **derived** from configurable factors, never entered arbitrarily. It is explainable: the system can show the decomposition of the weight (transparency requirement). Factors by relationship type:

| Factor | Applicable to | Effect on the weight |
|---|---|---|
| **Geographic / travel distance** | NEAR, STOP_OF, REACHABLE_BY | closer ⇒ higher weight (decay with distance) |
| **Semantic similarity** (cosine on Qdrant) | SIMILAR_TO, HAS_THEME, PAIRED_WITH | greater similarity ⇒ higher weight |
| **Co-occurrence in itineraries/sessions** | PAIRED_WITH, NEAR | places often visited/saved together ⇒ higher weight |
| **Community signal** (votes, saves, positive reviews) | REVIEWS, any | positive feedback ⇒ reinforcement |
| **Contributor reputation** | CONTRIBUTED_BY, proposed edges | contributions from reliable authors ⇒ higher weight and trust |
| **Source reliability/completeness** | edges from ingestion | authoritative source and complete datum ⇒ higher baseline weight |
| **Freshness / seasonality** | HOSTS_EVENT, seasonal experiences | relevance decays outside the time window |
| **Usage / navigation frequency** | any | edges more traversed during exploration ⇒ reinforcement |
| **Anti-popularity penalty (hidden-gem boost)** | final ranking | down-weight of absolute popularity alone to surface hidden places, modulated by user preference |

The weight is recalculated in **batch** (scheduled job) starting from the accumulated signals, and used both in navigation/ranking and as context for the GraphRAG. The formula for combining the factors is configurable per domain (factor weights exposed in config), so wine tourism can prioritize pairing while hiking prioritizes travel proximity.

### 5.4 Persistence on MySQL + Qdrant (no Neo4j)

- **MySQL** stores the structure: `graph_node` table (id, type, typed/JSON attributes, geo, language), `graph_edge` table (source, destination, type, direction, weight, attributes, source), plus support tables (taxonomy, reputation, revisions). Neighborhood/path queries rely on indexes on type/geo and on depth-controlled recursive queries (CTE), consistent with the "one query per Flyway migration" constraint.
- **Qdrant** stores the embeddings of the nodes' descriptions/tags for the semantic seed and the computation of SIMILAR_TO.
- The UUID mapping follows the project convention (`@JdbcTypeCode(SqlTypes.CHAR)`); MySQL reserved words must be escaped in DDLs.

---

## 6. Data sources & connectors (ingestion)

Ingestion reuses the Spring Batch pattern (folder-scan / document-ingestion) and the PF4J plugin extension points, adding connectors specific to the territory. Each connector is a replaceable component, ideally packageable as a marketplace plugin.

| Source | Type | What it provides | License / note | Priority |
|---|---|---|---|---|
| **OpenStreetMap (Overpass API)** | open | POIs, establishments, trails, geometries, tags | ODbL (attribution) | MVP |
| **Wikidata / Wikipedia** | open | entities, multilingual descriptions, relationships, images | CC0 / CC BY-SA | MVP |
| **Regional / DMO tourism open data** | open | official POIs/events/tours (schema.org, GeoJSON, CSV) | various open | MVP/evolution |
| **Event feeds (iCal, schema.org Event, festivals)** | semi-open | events and recurrences | various | Evolution |
| **Community contributions** | internal | hidden places, reviews, relationships | instance ownership | MVP |
| **GPX / itinerary tracks** | file | hiking/cycling routes | user | Evolution |
| **Local documents (PDF, brochures)** | file | guides, tourist-board materials → entity extraction | existing Tika/OCR | Evolution |
| **Email / calendar (existing modules)** | internal | events, bookings, territorial communications | privacy-aware | Evolution |
| **Weather / seasonality (API)** | optional external | contextual enrichment | optional, consent | Evolution |

Common requirements of the connectors: mapping to the taxonomy, IT/EN normalization, deduplication/reconciliation, license and provenance preservation, incremental handling (only changed records), respect for local-first (no unnecessary external data transmission). The existing connectors (Tika, OCR, folder watcher, email/calendar) are reused; the new ones (OSM, Wikidata, open data) are to be created.

---

## 7. Features to create, develop and maintain (MVP → evolution)

A concrete map of the work, distinguishing MVP, evolutions and continuous maintenance. The "Status" column indicates whether something is **created** from scratch, **extends** an existing module, or is **reused**.

### 7.1 MVP (first release of the vertical)

| # | Feature | Layer | Status | Notes |
|---|---|---|---|---|
| 1 | Territory graph data model (typed nodes/edges) | domain + persistence | Create (based on `knowledge`) | `graph_node`/`graph_edge` tables, Flyway one query/file |
| 2 | CRUD API for nodes and edges | api + domain | Create | `/api/v1/territory` or `/api/v1/graph` |
| 3 | Basic graph queries (neighbors, subgraph, by relationship) | domain + persistence | Create | MySQL CTE + geo/type indexes |
| 4 | Node embedding on Qdrant | infrastructure | Reuse/extend | Ollama EmbeddingModel `@Primary` |
| 5 | Hybrid GraphRAG search (Qdrant seed + graph expansion) | domain | Create | Core of the vertical |
| 6 | NL discovery endpoint + filters | api | Create | Boundary validation of inputs §3.1 |
| 7 | AI synthesis with node/path citation | domain | Extend LLM gateway | Reuse of fallback chain |
| 8 | OpenStreetMap connector (Overpass) | batch/plugin | Create | Ingestion of POIs/trails for an area |
| 9 | Wikidata connector | batch/plugin | Create | Multilingual descriptions |
| 10 | Ingestion pipeline (mapping, dedup, baseline weights) | batch | Extend | Reuse of document-ingestion pattern |
| 11 | Node/edge contribution UI | frontend | Create | Reactive forms, live dedup |
| 12 | Search UI + result list/cards | frontend | Create | Signal store |
| 13 | Map view with node markers | frontend | Create | Open maps library |
| 14 | Interactive graph view (nodes/edges/weight) | frontend | Create | Progressive expansion |
| 15 | Reviews/ratings + basic reputation | domain + api + fe | Create | Anti-spam, rate limiting |
| 16 | Edge weight computation (batch) with base factors | domain + batch | Create | Distance + semantics + community |
| 17 | Contribution moderation/curation | domain + api + fe | Create | Queue, approve/reject |
| 18 | IT/EN i18n of enums, taxonomy, UI | all | Extend | Enums translated toward the frontend |
| 19 | IT+EN documentation of the vertical | docs | Create | `documentation/` + `documentazione/` |

### 7.2 Future evolutions

| Feature | Value | Notes |
|---|---|---|
| Optimized multi-stop itinerary generator | advanced discovery | time/transport/elevation constraints, route optimization |
| Regional DMO open data connectors and event feeds | coverage | schema.org/GeoJSON/iCal |
| Configurable and explainable hidden-gem boost | anti-overtourism | de-correlation from popularity |
| Personalization based on the user's historical interests | relevance | local profile, privacy-first |
| AI suggestion of missing relationships between nodes | graph enrichment | link prediction on embeddings |
| GPX/GeoJSON export and itinerary sharing | data portability | user sovereignty |
| Offline mode / synchronization for travel use | local-first on the move | instance cache |
| Vertical modules (mountain, wine tourism, villages) | extensibility | new node types via plugin |
| Contributor gamification (badges, advanced reputation) | community growth | contribution quality |
| Weather/seasonality integration in the ranking | context | optional API with consent |
| White-label DMO instance publishing | adoption by entities | self-hosted branding |

### 7.3 Continuous maintenance

- Periodic recalculation of weights and emergent ranking; tuning of factors to avoid drift.
- Incremental update of connectors (OSM/Wikidata change often); version and license management.
- Data quality: recurring dedup, merge, orphan-node cleanup, taxonomy revision.
- Continuous moderation and anti-abuse (spam, fake reviews, ranking manipulation).
- Update of bilingual documentation and logs in `Sviluppi/` for each development.
- Metrics monitoring (Actuator/Prometheus) and performance of graph queries.

---

## 8. AI / GraphRAG use cases

| Use case | Example query | How the AI uses the graph |
|---|---|---|
| **Relational discovery** | "Medieval villages with organic wineries near Siena, few crowds" | Semantic seed → NEAR/HAS_THEME/PAIRED_WITH expansion → ranking with hidden-gem boost → node citation |
| **Tailor-made itinerary** | "Motorcycle weekend among villages and wineries, no highways" | Selects POIs/establishments, orders them as STOP_OF of an itinerary, optimizes for transport and time |
| **Non-obvious connections** | "What should I pair with this tasting in the surroundings?" | Navigates PAIRED_WITH/NEAR/SIMILAR_TO from the node, suggests complementary experiences |
| **Contextual questions** | "What's happening this weekend within 30 km?" | Temporal filter on HOSTS_EVENT + geo radius, event synthesis |
| **Relationship suggestion (curation)** | (contributor side) "Which relationships are missing for this POI?" | Link prediction on embeddings + proximity, proposes edges to validate |
| **Transparent explanation** | "Why are you recommending this place?" | Shows the path of nodes/edges and the decomposition of the weight |
| **Conversational exploration** | multi-turn dialogue about the territory | Keeps the subgraph as context, expands progressively |

All use cases run on local Ollama by default (privacy), with fallback to the configured cloud providers. The answers always cite the nodes/paths used, a transparency requirement that distinguishes LocalMind from the opaque rankings of competitors.

---

## 9. KPIs & success metrics

| Category | KPI | Goal / signal |
|---|---|---|
| **Discovery** | % of suggested places outside the top-popularity (hidden-gem rate) | High: measures the redistribution of flows |
| **AI quality** | Rate of answers with valid graph citation; hallucination rate | High citations, low hallucination |
| **Relevance** | CTR on suggestions, saves/itinerary, itinerary completion | Growing |
| **Graph growth** | New nodes/edges per period; geographic coverage | Sustained growth |
| **Community** | Active contributors, approved contributions, average moderation time | Active and growing, fast moderation |
| **Data quality** | Duplicate rate, nodes with complete mandatory fields, % bilingual IT/EN | Low duplicates, high completeness |
| **Performance** | Graph query latency (neighbors/subgraph), end-to-end GraphRAG latency | Within defined thresholds |
| **Sovereignty** | % of inferences served locally (Ollama) vs cloud | Local by default predominant |
| **Adoption** | Self-hosted instances (DMOs/municipalities), explorer retention | Growing |

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| **Graph cold start** (little initial coverage) | Low value at launch | Bootstrap via OSM/Wikidata/open data ingestion before the community launch |
| **Poor community participation** | Graph does not grow, no hidden gems | Simple contribution UX, reputation/gamification, DMO onboarding |
| **Data quality and duplicates** | Noisy graph, imprecise AI | Spatial+semantic dedup, reconciliation, moderation, boundary validation |
| **Ranking manipulation / fake reviews** | Loss of trust | Reputation, anti-spam, rate limiting, hidden-gem boost, explainable weight |
| **Graph query performance on MySQL** (no Neo4j) | High latency on large graphs | Geo/type indexes, depth-limited CTE, caching, targeted denormalizations; reassess the datastore only if necessary |
| **AI hallucination** | Invented suggestions | GraphRAG with context constrained to the subgraph, mandatory node citation |
| **Incompatible data licenses** | Legal/ethical risk | License tracking per source, rejection of incompatible sources, attribution |
| **User location/query privacy** | Trust violation | Local-first, local Ollama inference, explicit consent for external data |
| **Induced overtourism** if the ranking rewards the usual suspects | Effect contrary to the mission | Configurable anti-popularity penalty, hidden-gem rate monitoring |
| **Taxonomy fragmentation** | Incoherent graph | Controlled taxonomy aligned to schema.org/OSM, governance of extensions |

---

## 11. Maintenance & evolution

- **Weight and ranking recalculation:** scheduled batch jobs, with versioned factor parameters and monitored tuning to avoid ranking drift.
- **Connector updates:** incremental sync of OSM/Wikidata/open data, management of schema changes and licenses; each connector evolvable as an independent PF4J plugin.
- **Graph hygiene:** recurring dedup, node merges, orphan removal, periodic revision of the taxonomy and low-weight relationships.
- **Community governance:** moderation policies, reputation management, abuse handling; community health metrics.
- **Per-domain extensibility:** new modules (mountain, wine tourism, villages, accessibility) add node/relationship types without touching the core; distributed via marketplace.
- **Documentation:** constant update of IT (`documentazione/`) and EN (`documentation/`), plus dated development logs in `Sviluppi/` (daily progressive numbering) as per CLAUDE.md.
- **Observability:** Actuator/Prometheus metrics on graph latency, AI quality, data growth; dedicated dashboard.
- **Datastore evolution:** the "no Neo4j" constraint is reassessable in future cycles only if the graph queries demonstrably require it based on the performance metrics.

---

## 12. Integration with existing LocalMind modules

| Existing module | Role in the territory vertical |
|---|---|
| **`knowledge`** | Base of the graph engine: the territory node/relationship types extend this domain (modular schema) |
| **`llm` + `LlmGatewayService`** | Query understanding, GraphRAG synthesis, relationship suggestion; Ollama default + fallback chain |
| **Qdrant (`vectorstore`)** | Node embedding for semantic seed and SIMILAR_TO; Ollama EmbeddingModel `@Primary` |
| **MySQL + Flyway** | Graph structure persistence (`graph_node`/`graph_edge`), taxonomy, reputation; one query/file migrations |
| **`document` + Tika/OCR + batch** | Ingestion of local guides/brochures into nodes; reuse of folder-scan/document-ingestion pattern |
| **`plugin` (PF4J) + `marketplace`** | Connectors (OSM, Wikidata, open data) and vertical modules distributed as installable plugins |
| **`auth`** | Contributor identity, reputation, moderation; local-first |
| **`email` + `calendar`** | Ingestion of territorial events/communications (evolution), privacy-aware |
| **`automation`** | Ingestion triggers, weight recalculation, notifications on new events/places |
| **`messaging` / channels** | Notifications and sharing of itineraries/events toward channels |
| **`agent`** | Conversational graph exploration agent (multi-turn) |
| **`common` (domain events, analytics)** | Events on node creation/votes for side-effects (weight recalculation, analytics) |
| **Angular frontend (feature-driven, Signals)** | New lazy-loaded "territory" feature with list/map/graph views, Signal store, IT/EN i18n |
| **API `/api/v1/*` + GlobalExceptionHandler** | New discovery/CRUD graph endpoints, boundary validation, localized errors |

The vertical **introduces no new infrastructure**: it extends the existing domains, reuses MySQL+Qdrant+Ollama and the plugin system, and respects all project constraints (local-first, privacy, IT/EN, immutability, Flyway one query, small and cohesive files). It is the first consumer demonstration of the universal graph engine, twin and symmetrical to the enterprise verticals.

---

### Sources and industry references

- TravelRAG — multi-layer GraphRAG framework for the retrieval of tourist attractions (MDPI ISPRS IJGI, 2024).
- "Exploring the Landscape of Tourism Knowledge Graphs: A Systematic Literature Review" (IEEE).
- Knowledge graph-driven personalized attractions recommendation with long/short-term interest modeling (ScienceDirect, 2025).
- Tyrolean Tourism Knowledge Graph — schema.org-based ecosystem (arXiv 1805.05744).
- Open Data Tourism Alliance (ODTA) and Open Data Destination Germany project — standardization of tourism content on schema.org (POI, Tour, Event).
- schema.org (`TouristAttraction`, `TouristTrip`, `Event`), OpenStreetMap (ODbL), Wikidata (CC0) as bases for taxonomy and ingestion.
