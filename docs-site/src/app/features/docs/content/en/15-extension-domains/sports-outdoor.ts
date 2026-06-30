export const content = `# Sport & outdoor

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

## 1. What we solve (problem & value)

The **Sport & outdoor** domain brings into LocalMind's universal Knowledge Graph engine all the knowledge that today lives fragmented, scattered and hard to query about **trails, outdoor activities, sports facilities and events**. It is a vertical of the **consumer** group (discovery of the territory) and shares its community-driven "Wikipedia of places" philosophy, but with a specific focus: the experience that can be practiced in the field, governed by three variables that determine its real feasibility — **difficulty**, **season/conditions** and **place**.

**The concrete problem.** Those who practice or want to take up an outdoor activity (hiking, trail running, MTB, climbing, ski mountaineering, kayaking, via ferrata, open-water swimming, paragliding, etc.) must today manually piece together information that comes from heterogeneous and often contradictory sources:

- **Raw cartographic data** (OpenStreetMap, downloaded GPX tracks, IGM/cadastral maps) that describe the geometry of the route but not the experience;
- **Closed vertical portals** (Komoot, Outdooractive, AllTrails, Wikiloc, Strava) that concentrate data in their silos, expose limited or paid APIs and are not self-hostable;
- **Reviews and textual reports** scattered across blogs, forums, social groups and Alpine club reports, rich in "tacit" knowledge (recent conditions, dangerous passages, seasonal windows) but not structured;
- **Bulletins and dynamic data** (weather, avalanche AINEVA/EAWS, river flow rates, seasonal closures of facilities and refuges, civil protection alerts) that change the accessibility of a place from one day to the next;
- **Event calendars** (races, gatherings, events, seasonal openings, guided excursions) published across a thousand different channels.

The result is that the apparently simple question — *"what can I do this weekend, near me, compatible with my level and with current conditions?"* — today requires hours of cross-referenced research and still produces uncertain answers.

**What LocalMind solves.** We transform this ecosystem into a **weighted graph navigable by AI** where a trail, an activity, a facility, an event, a point of interest, a season and a difficulty level are **typed nodes** connected by **weighted relationships** (e.g. *a trail IS_PRACTICABLE_IN a season with weight 0.9*, *starts from a refuge*, *is suited to a difficulty level*, *crosses a protected area*, *is linked to an event*). On this graph, the AI in GraphRAG combines **relationship navigation** and **semantic search** (Qdrant) to answer complex questions and surface non-obvious connections.

The differentiating value compared to existing portals:

| Dimension | Existing outdoor portals | LocalMind Sport & outdoor |
|------------|---------------------------|---------------------------|
| Data ownership | Closed silo, lock-in | Self-hosted, local-first, user/community data |
| Track/location privacy | Data sent to the provider's cloud | GPS tracks stay local; Ollama AI by default |
| Querying | Rigid filters, keywords | Natural language questions over the graph (GraphRAG) |
| Implicit connections | Absent or opaque | The graph surfaces chains trail→refuge→event→season |
| Extensibility | Closed | PF4J plugins + marketplace, new node types per discipline |
| Cost | Freemium/subscription | Pure open source |
| Territorial context | Isolated from other knowledge | Integrated with the consumer graph (tourism, events, experiences) |

**Value for the different actors.** For the individual practitioner: finding the right activity while reducing risk (the real difficulty and seasonal conditions are first-class in the model, not marginal notes). For local communities (proloco, Alpine clubs, ASD amateur sports associations, federations): publishing and curating the knowledge of their own territory without depending on external platforms. For institutions (municipalities, parks, tourism consortia): maintaining an authoritative, up-to-date and self-hosted catalog of the outdoor offering. For the LocalMind ecosystem: the Sport & outdoor vertical is the ideal testbed for the graph engine because it is intrinsically **relational** (everything is connected to place, time and skill) and **dynamic** (conditions change), so it stresses the structural part (MySQL), the semantic part (Qdrant) and the ingestion of live sources alike.

**What it is NOT.** It is not a real-time turn-by-turn tourist GPS navigator, it does not replace official avalanche bulletins (it integrates and cites them), it is not a fitness social network. It is the **knowledge layer** that sits underneath and makes access to the outdoors queryable, safe and personalized.

## 2. Personas & target users

| Persona | Profile | Main objective | Typical questions to the system |
|---------|---------|----------------------|-----------------------------|
| **Occasional hiker** ("Giulia") | Family, beginner, looking for easy and safe outings | Find low-difficulty routes suitable for children and walkable today | "T1 trails with a water fountain and suitable for a stroller within 30 km of home?" |
| **Expert hiker/mountaineer** ("Marco") | Expert, evaluates exposure, seasonality, equipment | Plan demanding outings assessing risk and conditions | "EEA via ferrata open in July above 2000 m with a short approach?" |
| **Trail runner / MTB cyclist** ("Sara") | Athlete, interested in elevation gain, terrain, loop | Training sessions and routes on target distance/elevation | "40 km MTB loops, S2 scale, dry in autumn, near a refreshment point?" |
| **Sports event organizer** ("Luca") | ASD / proloco, publishes races and gatherings | Catalog and promote events linked to routes and facilities | (editor side) Create a "Trail race" event linking it to a trail and a season |
| **Facility/refuge manager** ("Anna") | Manages a ski lift, climbing gym, refuge, swimming pool | Maintain seasonal openings, services, links to routes | (editor side) Update openings, services, linked trails |
| **Community curator/moderator** ("local CAI") | Alpine club, hiking group | Validate contributions, ensure accuracy and safety | Moderate reports, update conditions, correct difficulty |
| **Territorial institution** ("Municipality / Park") | Public administration, tourism consortium | Authoritative self-hosted catalog of the outdoor offering | Publish the official trail network, monitor closures |
| **Rescue/prevention** ("CNSAS volunteer") | Safety profile | Understand the influx and criticality of risky routes | "Which EE routes with high exposure have had recent reports?" |

The first three are the **consumers** of the graph (they use search and GraphRAG); the last five are predominantly **producers/curators** (they contribute nodes and edges, moderate, maintain dynamic data). The system must serve both sides with the same quality: without producers the graph empties out, without consumers it generates no value.

## 3. Input requirements

This section defines, exhaustively, **what must be able to enter the system** so that the Sport & outdoor graph is rich and reliable. Inputs are grouped by nature. Each input must be **validated at the boundary** (schema-based, fail-fast) and, where possible, normalized toward standard vocabularies.

### 3.1 Geospatial inputs (geometry and route)

- **GPX/KML/GeoJSON/TCX tracks**: files uploaded by the user or imported from an external source. They must contain at least the sequence of points (lat, lon); optionally elevation, timestamp, heart rate, cadence (for training data). Validation: well-formed file, plausible coordinates (WGS84 range), minimum length, no impossible jumps between consecutive points.
- **Route geometry**: line (LineString) for trails/itineraries, point for POI/facilities, polygon for areas (parks, protected zones, crags, ski domains). Reference system normalized to **WGS84 (EPSG:4326)**.
- **Elevation profile**: derived from the track or from DEM; needed to compute positive/negative elevation gain, average and maximum slope.
- **Derived metrics**: length, elevation +/-, minimum/maximum altitude, slope, prevailing exposure (aspect), estimated travel time.

### 3.2 Descriptive inputs of the activity and place

- **Node registry**: name, description (bilingual IT/EN), type of activity (hiking, trail, MTB, climbing, ski mountaineering/cross-country/downhill, via ferrata, kayak/canoe, SUP, paragliding, open-water swimming, horseback riding, caving, etc.), point category (trail, facility, refuge, crag, gym, pool, equipped area, starting parking lot).
- **Start/finish point, notable waypoints**: refuges, water sources, junctions, panoramic points, key passages (ford, ledge, equipped section).
- **Services and amenities**: parking, public transport, drinking water, refreshment, rental, accessibility (stroller/wheelchair), signal coverage, dog-friendly.
- **Official references**: CAI number/trail marker, trail code, cadastral code of the ski domain, link to an authoritative source.

### 3.3 Difficulty inputs (critical variable)

Difficulty must be modeled with **recognized standard scales**, not with free-form labels, because it is a safety factor. The system must accept and map:

| Discipline | Supported standard scale | Example values |
|-----------|---------------------------|----------------|
| Hiking | CAI scale | T (Tourist), E (Hiking), EE (Expert hikers), EEA (with Equipment) |
| Hiking/alpine | SAC Hiking Scale (OSM \`sac_scale\`) | T1 → T6 |
| MTB | MTB scale (OSM \`mtb:scale\`) | S0 → S5 |
| Downhill skiing | Piste difficulty (OSM \`piste:difficulty\`) | novice, easy, intermediate, advanced, expert |
| Climbing | UIAA / French / Yosemite (YDS) | UIAA I–XII, Fr 3a–9c, YDS 5.x |
| Via ferrata | Via ferrata scale | F (easy) → EX (extremely difficult) |
| Ski mountaineering | Blachère / Toponeige scale | F, PD, AD, D, TD… |

- **Factors contributing to difficulty** (to be captured separately to allow recalculation and explainability): exposure/exposure to drops, terrain quality, signage/track visibility (OSM \`trail_visibility\`), presence of equipped passages, length and elevation gain, altitude, need for specific equipment/skills.
- **Validation**: every difficulty value must belong to a declared scale; conversions between scales managed by versioned mapping tables; in the absence of an official scale, an explicit "unclassified" label (never a silent default).

### 3.4 Seasonal, temporal and condition inputs (critical variable)

- **Practicable seasonal window**: recommended and discouraged months/periods (e.g. snow-covered route in winter, ford impassable in late spring due to thaw).
- **Dynamic conditions**: current state (open/closed, snow-covered, muddy, flooded), local weather bulletin, avalanche bulletin (EAWS/AINEVA scale 1–5), river flow rate, civil protection alerts.
- **Openings/closures**: opening calendar of facilities, refuges, ski domains; closures for nesting/hunting/works; hours.
- **Timed events**: date/time/duration of races, gatherings, guided excursions, events, with optional recurrence.
- **Validation**: dates in ISO 8601 format, explicit time zones, interval consistency (start ≤ end), recurrences expressed with verifiable rules.

### 3.5 Community-driven inputs (contributions and feedback)

- **Creation/modification of nodes and edges** by registered users (editors), with author and version tracking.
- **Reviews, ratings, reports**: numeric vote, free text, photos, reporting of recent conditions ("found ice at the key passage yesterday"), problem reports (landslide, missing signage).
- **Quality/reliability votes**: useful for emergent ranking and for edge weights.
- **Validation and security**: text sanitization (anti-XSS), moderation/curation before publication for fields that impact safety (difficulty, conditions), rate limiting on contributions, plausible-coordinate checks.

### 3.6 Document and unstructured inputs

- **Documents** (PDF of trip reports, guides, race regulations, technical sheets): ingested through the existing \`document\` domain (Tika + OCR), chunking and embedding on Qdrant.
- **Web pages / feeds** of portals, blogs, event calendars: via connectors/plugins.
- **Email and messages** (\`email\`/\`messaging\` domain): e.g. facility closure communications, event confirmations.

### 3.7 Non-functional requirements on inputs

- **IT/EN bilingualism** on all descriptive fields and on all enums (difficulty, season, activity type) — project requirement.
- **Privacy**: GPS tracks and personal location data stay local; no transmission to external services without explicit consent.
- **Provenance and license**: every imported input must record source and license (e.g. OSM ODbL) for compliance.
- **Ingestion idempotency**: re-importing the same source must not duplicate nodes (deduplication by external identifier/geometry).

## 4. Activity flow (step-by-step)

We describe the main end-to-end flows. They are designed to directly guide the implementation (controller → use case → service → port → adapter, following the hexagonal architecture).

### 4.1 Flow A — Ingestion of a route (from GPX or from an external source)

1. **Acquisition**: the editor uploads a GPX file from the UI (\`knowledge\` feature / new \`outdoor\` feature) or a connector imports from OSM/portal. The controller receives the file and passes it to the ingestion use case.
2. **Boundary validation**: file parsing, schema verification, plausible coordinates, minimum length. Fail-fast with a bilingual message in case of error.
3. **Geometric normalization**: conversion to WGS84, track simplification (Douglas-Peucker) for visualization, computation of the elevation profile from DEM if missing.
4. **Computation of derived metrics**: length, elevation +/-, slope, min/max altitude, estimated time, exposure.
5. **Difficulty inference/normalization**: if the source provides a value (e.g. OSM \`sac_scale\`), it is mapped onto the canonical scale; otherwise an estimate from the factors (elevation gain, altitude, exposure) is proposed, marked as "estimated, to be validated".
6. **Semantic extraction**: the textual description is chunked and embedded on Qdrant; the local AI (Ollama) can extract entities (refuges mentioned, key passages, seasonality referenced) to enrich the nodes.
7. **Construction of nodes and edges**: creation of the \`Trail/Route\` node, of the linked \`PointOfInterest\`/\`Refuge\`/\`StartingPoint\` nodes, and of the weighted edges (\`STARTS_FROM\`, \`CROSSES\`, \`HAS_DIFFICULTY\`, \`PRACTICABLE_IN\`).
8. **Deduplication**: comparison with existing nodes by external identifier or geometric proximity; in case of a match, merge/update instead of duplication (immutable pattern: new version of the node).
9. **Persistence**: graph structure on MySQL (node/edge tables), vectors on Qdrant, files/metadata through the \`document\` domain.
10. **Indexing and availability**: the route becomes queryable via search and GraphRAG; publication subject to state (draft → under review → published) according to moderation.

### 4.2 Flow B — Search and discovery by the consumer (GraphRAG)

1. **Natural language question** from the UI: e.g. *"What can I do on Sunday near Bergamo, easy level, if it hasn't rained?"*.
2. **Interpretation**: the AI extracts constraints (place+radius, time window, difficulty, weather condition) and translates them into **graph filters** + **semantic query**.
3. **Hybrid retrieval (GraphRAG)**:
   - **structural** part: candidate selection on MySQL filtering by geographic area, compatible difficulty, current season/conditions, practicability edges;
   - **semantic** part: search on Qdrant over descriptive chunks for intent and nuances ("suitable for children", "panoramic");
   - **graph expansion**: from the candidates, neighbors are explored (refuges, events in the same period, linked routes) following the weighted edges.
4. **Ranking**: combination of semantic relevance, edge weight, emergent community ranking (votes/reliability), proximity and compatibility with the constraints.
5. **Verification of dynamic conditions**: for the top candidates, live data (weather, avalanches, openings) is checked; routes not accessible today are downgraded or flagged.
6. **Response generated with citations**: the AI responds by listing the proposals, **citing the graph nodes and routes used** (trail X → refuge Y → practicable in spring) and explaining why (e.g. "chosen because it is T1, dry, with a fountain"). Transparency = trust.
7. **Interactive navigation**: the user explores the graph starting from a proposal (progressive expansion: neighbors, related events, easier/harder alternatives), applies filters by node/relationship type.

### 4.3 Flow C — Community contribution and moderation

1. **Contribution proposal**: the user creates/modifies a node (new trail, event, condition report) or leaves a review/vote. Input validated and sanitized.
2. **Contribution classification**: contributions that impact **safety** (difficulty, dangerous conditions, closures) enter the moderation queue; minor contributions can be published with light curation.
3. **Review**: a curator (e.g. local CAI) approves, corrects or rejects, with a justification. Every change is versioned (immutable history).
4. **Update of the graph and weights**: approval creates/updates nodes and edges; votes and reliability recompute edge weights and emergent ranking.
5. **Feedback to the author** and recognition of the contribution (reputation), to feed the community-driven virtuous circle.

### 4.4 Flow D — Update of dynamic data (conditions)

1. **Polling/reception** from connectors (weather, avalanches, facility openings, alerts) through the \`automation\` domain (scheduled jobs) or webhook/\`messaging\`.
2. **Normalization** of the dynamic datum toward the condition enums (avalanche scale 1–5, open/closed state, alert level).
3. **Update of the \`PRACTICABLE_IN\`/\`HAS_CONDITION\` edges** with timestamp and validity (dynamic data have an expiry: a 3-day-old weather report must not weigh the same as today's).
4. **Recalculation of weights** dependent on conditions and invalidation of search caches.
5. **Notifications** (optional) to users interested in a route that changes state (e.g. facility opening, improving conditions).

## 5. Graph model (node types, relationship types, weighting criteria)

The model reuses the infrastructure of the universal graph engine (node/edge tables on MySQL + vectors on Qdrant). Here we define the Sport & outdoor **domain schema**: node types, edge types and weighting criteria. All types are extensible via modular schema and plugins.

### 5.1 Node types

| Node type | Description | Key attributes |
|--------------|-------------|------------------|
| \`Trail\`/\`Route\` | Linear walkable itinerary | geometry, length, elevation+/-, min/max altitude, trail marker, activity type |
| \`Itinerary\`/\`Loop\` | Composition of multiple routes/stages | ordered stages, total duration, loop yes/no |
| \`Activity\` | Practicable discipline (hiking, MTB, climbing…) | category, requirements, equipment |
| \`PointOfInterest (POI)\` | Notable point along or near a route | type (panoramic, source, ford), coordinates |
| \`Facility\` | Sports facility/ski lift/structure | type, capacity, hours, seasonal openings |
| \`Refuge\`/\`Bivouac\` | Support and refreshment point | beds, services, opening period, contacts |
| \`Crag\`/\`ClimbingSector\` | Climbing area | number of routes, exposure, grades present |
| \`SportsEvent\` | Race, gathering, guided excursion | date/time, discipline, registrations, organizer |
| \`Difficulty\` | Level on a standard scale | scale (CAI/SAC/MTB/UIAA…), value, factors |
| \`Season\`/\`TimeWindow\` | Practicability period | months, typical conditions |
| \`Condition\` | Current dynamic state | weather, snow, avalanche (1–5), open/closed, validity |
| \`Place\`/\`Area\` | Locality, municipality, valley, protected area, ski domain | boundaries, administrative hierarchy |
| \`StartingPoint\`/\`Parking\` | Access to the route | coordinates, public transport, capacity |
| \`Service\` | Amenity (water, refreshment, rental) | type, availability |
| \`Organizer\`/\`Manager\` | ASD, proloco, club, facility manager | entity type, contacts |
| \`Person\`/\`Contributor\` | User who contributes or practices | reputation, role |
| \`Review\`/\`Report\` | Feedback on a node | rating, text, photos, date |
| \`Document\` | Report, guide, regulation (bridge with the \`document\` domain) | reference, embedding |

### 5.2 Relationship types (edges)

| Relationship (edge) | From → To | Meaning | Weighted by |
|------------------|--------|-------------|-----------|
| \`HAS_DIFFICULTY\` | Trail → Difficulty | Difficulty classification | reliability of the classification, community consensus |
| \`PRACTICABLE_IN\` | Trail/Activity → Season | Recommended seasonal window | how sharp the seasonality is, recent confirmations |
| \`HAS_CONDITION\` | Trail/Facility → Condition | Current dynamic state | freshness of the datum, source authority |
| \`STARTS_FROM\` | Trail → StartingPoint | Access point | — (structural) |
| \`CROSSES\`/\`PASSES_THROUGH\` | Trail → POI/Place/Area | Traverses/touches | relevance of the POI to the route |
| \`CONNECTS\` | Trail ↔ Trail/Refuge | Network connection | usage frequency of the connection |
| \`IS_PRACTICED_WITH\` | Trail → Activity | Activities allowed on the route | suitability (e.g. MTB yes/no) |
| \`HOSTS_EVENT\` | Trail/Facility/Place → SportsEvent | Venue of an event | imminence, event importance |
| \`OFFERS_SERVICE\` | Refuge/Facility → Service | Available services | reliability of the datum |
| \`NEAR\` | Node ↔ Node | Geographic proximity | inverse of the distance |
| \`SUITABLE_FOR\` | Trail → Person/Level/Person-type | Suitability for a profile/skill | skill↔difficulty match |
| \`SIMILAR_TO\` | Trail ↔ Trail | Similarity (semantic + characteristics) | embedding similarity + metrics |
| \`MANAGED_BY\` | Facility/Refuge/Event → Manager | Responsibility | — (structural) |
| \`REVIEWED_BY\` | Node → Review/Person | Feedback received | rating, author reputation |
| \`DESCRIBED_IN\` | Node → Document | Linked documentation | semantic relevance |
| \`IS_PART_OF\` | Trail → Itinerary/Network | Composition | — (structural) |

### 5.3 Criteria for edge weighting

The weight (0–1, or normalized score) is what makes the graph "intelligently navigable". For Sport & outdoor the key factors are:

- **Temporal freshness** (decisive for \`HAS_CONDITION\`/\`PRACTICABLE_IN\`): a condition datum decays over time; the weight drops with the age of the datum. An ice report from yesterday weighs much more than one from three weeks ago.
- **Source reliability**: official datum (institution, bulletin) > expert curator contribution > unverified user contribution. Official difficulty scales weigh more than automatic estimates.
- **Community consensus/reliability**: number and agreement of votes/reports confirming the edge (e.g. many agree the trail is T2 → high weight on \`HAS_DIFFICULTY\`).
- **Usage frequency/popularity**: frequently traversed edges (\`CONNECTS\`, popular routes) gain weight, but balanced so as not to penalize little-known gems.
- **Strength of the physical relationship**: for \`NEAR\`, weight inversely proportional to distance; for \`CROSSES\`, relevance of the POI relative to the route.
- **Profile compatibility** (for \`SUITABLE_FOR\`): match between the level declared by the user and the route's difficulty.
- **Hybrid similarity** (for \`SIMILAR_TO\`): combination of embedding similarity (Qdrant) and closeness of metrics (elevation gain, difficulty, duration).

Weights are **recomputable** (jobs in \`automation\`) and **explainable**: every weight must be decomposable into its factors for the transparency of AI responses.

## 6. Data sources & connectors (ingestion)

| Source | Data type | Connector / mechanism | Notes |
|-------|-----------|--------------------------|------|
| **OpenStreetMap** (Overpass API) | Trails, POI, tags \`sac_scale\`/\`mtb:scale\`/\`piste:difficulty\`/\`trail_visibility\` | PF4J connector plugin + \`automation\` job | ODbL license to track; deduplication by OSM id |
| **GPX/KML/GeoJSON/TCX files** | Tracks, waypoints, training | UI upload → ingestion use case | Stay local (privacy) |
| **DEM / elevation data** | Altitudes, elevation profile | Local/raster service | For elevation gain/slope computation |
| **Outdoor portals** (where APIs/feeds available) | Routes, reviews | Dedicated connector plugin | Respect of ToS and licenses |
| **Weather bulletins** | Local forecasts | Weather connector (scheduled job) | Dynamic datum with expiry |
| **Avalanche bulletins** (EAWS/AINEVA) | Avalanche danger 1–5 | Dedicated connector | Cite the official source; do not replace it |
| **Facility/refuge openings** | Calendars, hours, closures | Connector + manager contributions | Mixed automatic/manual |
| **Event calendars** (ICS, federations, proloco) | Races, gatherings, guided excursions | \`calendar\` domain + ICS connectors | Timed/recurring events |
| **Documents** (PDF guides, reports, regulations) | Unstructured text | \`document\` domain (Tika + OCR) → Qdrant | Chunking + embedding |
| **Email / messages** | Service communications | \`email\`/\`messaging\` domains | E.g. closure notices |
| **Community contributions** | Nodes, edges, reviews, reports | Editor UI + API | Moderation and versioning |

Ingestion guidelines: every connector is a **PF4J plugin** (reuse of the marketplace), exposes provenance/license metadata, is **idempotent** (no duplicates) and respects local-first constraints (sensitive data does not leave without consent). Recurring jobs rely on the \`automation\` domain; event-driven flows on the domain events system.

## 7. Features to create, develop and maintain (MVP → evolution)

### 7.1 MVP (first release of the vertical)

| # | Feature | Type | Modules involved |
|---|--------------|------|------------------|
| M1 | Outdoor domain schema on the graph engine (node/edge types of sec. 5) as a modular schema | CREATE | \`knowledge\`, MySQL (Flyway), Qdrant |
| M2 | Bilingual IT/EN enums for difficulty (CAI/SAC/MTB), seasons, activity types, conditions | CREATE | \`knowledge\`, API, frontend i18n |
| M3 | GPX/GeoJSON ingestion with validation, metric computation and node/edge creation | CREATE | new \`outdoor\` domain/feature, \`document\` |
| M4 | Base OSM connector (Overpass) with deduplication and license tracking | CREATE | PF4J plugin, \`automation\` |
| M5 | CRUD API for outdoor nodes/edges + neighborhood query and filters (difficulty, season, area) | CREATE | \`localmind-api\`, \`knowledge\` |
| M6 | GraphRAG search: NL question → graph filters + semantics → response with citations | DEVELOP (extends GraphRAG core) | \`llm\` (Ollama default), Qdrant, \`knowledge\` |
| M7 | "Outdoor" UI feature: search, route card (metrics, difficulty, season), map/elevation profile | CREATE | Angular standalone frontend |
| M8 | Base community contributions: route creation, review, vote; input sanitization | CREATE | \`knowledge\`, \`auth\`, API |
| M9 | Base moderation of contributions that impact safety (queue + versioned approval) | CREATE | \`knowledge\`, \`auth\` |
| M10 | Edge weights v1 (freshness, source reliability, consensus) recomputable | CREATE | \`knowledge\`, \`automation\` |

### 7.2 Evolutions (subsequent releases)

| # | Feature | Type | Notes |
|---|--------------|------|------|
| E1 | Dynamic data: weather, avalanche, opening connectors; \`HAS_CONDITION\` edges with expiry | DEVELOP | \`automation\`/\`messaging\` domain |
| E2 | Interactive graph visualization (weighted nodes/edges) with progressive expansion and filters | DEVELOP | frontend, part of the graph core |
| E3 | Personalized itineraries generated by AI on the graph (multi-stage, skill/season constraints) | DEVELOP | GraphRAG + \`agent\` |
| E4 | Suggestion of missing links (e.g. similar routes, unconnected refuges) | DEVELOP | graph core (link prediction) |
| E5 | Automatic conversion between difficulty scales + explainable estimate from factors | DEVELOP | \`knowledge\` |
| E6 | Advanced emergent ranking (contributor reputation, quality) | DEVELOP | \`knowledge\`, \`auth\` |
| E7 | Additional connectors (portals, federations, ICS events, high-resolution DEM) | CREATE | PF4J plugin/marketplace |
| E8 | Notifications on condition/opening changes for followed routes | CREATE | \`messaging\`, \`automation\` |
| E9 | Training import (TCX/HR) and personal performance analysis (local-first) | DEVELOP | \`outdoor\`, privacy-by-design |
| E10 | Installable "Sport & outdoor" package/module from the marketplace | CREATE | \`marketplace\`, \`plugin\` |
| E11 | Multimodality: recognition from photos (e.g. signage, conditions) via multimodal Ollama | DEVELOP | \`llm\` (multimodal adapter) |

### 7.3 Continuous maintenance

- Updating of the **mapping tables** between difficulty scales (versioned).
- **Freshness of dynamic data**: expiry monitoring, purge of stale data, connector healthchecks.
- **Graph deduplication and quality**: periodic jobs for duplicate detection and orphan edges.
- **Moderation**: queue management, change auditing, version restoration.
- **i18n**: maintenance of IT/EN translations of enums and content.
- **License compatibility** of imported sources.

## 8. AI / GraphRAG use cases

- **Conditional discovery**: *"What can I do on Sunday within 40 km, level E, if conditions are good?"* → graph filters (area, difficulty, dynamic conditions) + semantics + ranking, response with node citations.
- **Multi-stage itinerary planning**: *"3-day trek with refuges, max elevation gain 1000 m/day, feasible in September"* → the AI composes an itinerary by navigating \`CONNECTS\`/\`STARTS_FROM\`/\`PRACTICABLE_IN\` edges.
- **Profile adaptation**: *"I'm a beginner with two children: an easier alternative to this route?"* → follows \`SIMILAR_TO\` + \`SUITABLE_FOR\` downgrading difficulty.
- **Risk explanation**: *"Why is this trail EE?"* → the AI decomposes the \`HAS_DIFFICULTY\` edge into the factors (exposure, terrain, equipment) citing the source.
- **Non-obvious connections**: surfacing that an event, a newly opened refuge and a little-known route converge in the same weekend and area.
- **Conditions synthesis**: aggregating recent community reports + bulletins into a synthetic, up-to-date picture of a route.
- **Missing-link suggestion**: proposing network connections between trails or route↔event associations not yet modeled.

All cases respect the principle of **response with node/route citation** and work with **Ollama locally** by default.

## 9. KPIs & success metrics

| Category | KPI | Indicative target |
|-----------|-----|----------------------|
| Graph coverage | No. of \`Trail\`/\`POI\`/\`Event\` nodes per area | Steady growth; ≥ minimum threshold for "served area" |
| Difficulty quality | % of routes with difficulty on a standard scale (not "estimated") | > 80% on published routes |
| Conditions freshness | Average age of active dynamic data | < 24–48h for weather/avalanches |
| Search relevance | Click/usage rate of the top 3 GraphRAG proposals | High and growing |
| Explainability | % of AI responses with node/route citations | ~100% |
| Community | No. of contributions/reviews; % of approved contributions | Positive trend; low percentage rejected by mistake |
| Reliability | Discrepancy between community and real difficulty (reports of "harder than expected") | Declining |
| Privacy | % of flows that stay local-first | 100% for personal location data |
| Extensibility | No. of active connectors/plugins from the marketplace | Growing |
| Performance | GraphRAG query latency on a target-size graph | Within acceptable UX thresholds |

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---------|---------|-------------|
| **Incorrect difficulty datum** (safety) | High — risk to personal safety | Mandatory standard scales, moderation of safety fields, explicit "estimated" label, source citation |
| **Stale conditions presented as current** | High | Explicit expiry of dynamic data, weight decay, indication of the datum's date in the UI |
| **Graph performance on MySQL** (no Neo4j) | Medium | Geo/relational indexes, depth-limited queries, pre-aggregations/cache, GraphRAG with controlled expansion |
| **Source licenses** (OSM ODbL, portals) | Medium-legal | Provenance/license tracking per node, ToS compliance, attributions in the UI |
| **Spam/unreliable contributions** | Medium | Moderation, reputation, rate limiting, sanitization, reliability-based ranking |
| **Location data privacy** | High | Local-first by default, no cloud transmission without consent, local Ollama |
| **Fragmentation of difficulty scales** | Medium | Versioned mapping tables, explicit and explainable conversions |
| **Dependence on unstable external connectors** | Medium | Isolated plugins, healthchecks, graceful degradation (the graph stays useful even without live data) |
| **AI hallucinations on safety** | High | Responses anchored to the graph with citations; disclaimers; never replace official bulletins |

## 11. Maintenance & evolution

- **Versioned modular schema**: outdoor node/edge types evolve with Flyway migrations (one query per file) and schema versioning, without breaking the existing graph.
- **Connectors as plugins**: new sources are added via PF4J/marketplace without touching the core; each plugin has its own lifecycle and version.
- **Live data pipeline**: continuous monitoring of freshness and connector health; automatic purge of expired data; periodic recalculation of weights (\`automation\`).
- **Community curation**: moderation processes and tools, change auditing, reputation management; involvement of authoritative curators (CAI, parks).
- **Graph quality**: deduplication jobs, detection of orphan/inconsistent edges, geometric validation.
- **i18n and documentation**: constant updating of IT/EN translations and bilingual documentation (project requirement); every development tracked in the \`Sviluppi/\` folder with dated naming.
- **Extension to new disciplines**: addition of difficulty scales and activity types (e.g. new disciplines) through extension of the bilingual enums and the schema, without rewriting.
- **Evolutionary roadmap**: from static data (MVP) to dynamic data (E1), to interactive visualization (E2), to AI itineraries (E3) and to the marketplace module (E10), in incremental phases consistent with the GSD roadmap.

## 12. Integration with existing LocalMind modules

| Existing module | Role in the Sport & outdoor vertical |
|------------------|-------------------------------------|
| \`knowledge\` | **Heart of the vertical**: hosts the domain schema (outdoor nodes/edges) on the universal graph engine; main extension point |
| \`llm\` (Ollama default) | GraphRAG engine, entity extraction from text, response generation with citations, multimodality (photos) — local by default, cloud optional |
| \`document\` | Ingestion of guides/reports/regulations (Tika + OCR), chunking and embedding on Qdrant for the semantic part |
| Qdrant (vector store) | Semantic search and similarity (\`SIMILAR_TO\`) over descriptions and documents |
| MySQL + Flyway | Graph structure (nodes/edges/weights), metrics, enums; migrations with a single query per file |
| \`automation\` | Scheduled jobs: OSM ingestion, polling of dynamic data (weather/avalanches/openings), weight recalculation, purge of expired data |
| \`calendar\` | Timed/recurring sports events, seasonal openings, guided excursions (ICS import) |
| \`messaging\` | Notifications on condition/opening changes for followed routes; event-driven ingestion of notices |
| \`email\` | Ingestion of service communications (closures, event confirmations) |
| \`mcp\` | Exposure of MCP tools to query the outdoor graph from external agents/tools |
| \`agent\` | Agents for multi-stage itinerary planning and discovery workflows |
| \`auth\` | Contributor identity, editor/curator/moderator roles, reputation, privacy |
| \`plugin\` (PF4J) + \`marketplace\` | Source connectors as installable plugins; distributable "Sport & outdoor" package from the marketplace |
| \`finetuning\` | (Evolution) adaptation of local models to outdoor/territorial terminology |
| Angular 21 frontend | New lazy-loaded "outdoor" feature: search, route cards, map/elevation profile, graph visualization, contribution editor — standalone components, Signals, IT/EN i18n |

**Constraints respected end-to-end**: local-first and self-hostable (tracks and location data stay local), **Ollama AI by default** (cloud optional and with consent), reuse of **MySQL + Qdrant** without introducing Neo4j, data **privacy** (no external transmission without explicit consent), extensibility via **PF4J/marketplace plugins**, **IT/EN bilingualism** of UI, documentation and enums, and **Flyway migrations with a single query** per file. The Sport & outdoor vertical rewrites nothing: it **extends** the universal graph engine by adding node types, relationships, connectors and domain UI.
`;
