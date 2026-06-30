export const content = `# Real estate

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This document describes the **Real estate** extension domain (group: *consumer*) of LocalMind's universal Knowledge Graph engine. The goal is to transform real estate search from a sequence of filters over a table of listings into **conscious navigation of a weighted graph** that relates properties, areas, services, prices and life profiles, and that the AI can traverse (GraphRAG) to answer complex questions such as "a three-bedroom house, under €350,000, in a quiet area but with good schools reachable on foot and a metro within 10 minutes".

The domain fully reuses the existing stack (hexagonal Spring Boot, Angular 21, MySQL 8.0 for structure, Qdrant for semantics, Ollama as the default local AI) and is delivered as an **installable domain module** through the PF4J plugin system + marketplace, respecting the local-first, privacy and IT/EN bilingualism constraints.

---

## 1. What we solve (problem & value)

### 1.1 The real problem

Real estate search today is fragmented, opaque and heavily skewed in favor of sellers. Whoever is looking for a home (or an investment) faces three structural pain points:

1. **Misalignment between the listing's data and the life decision.** Portals (immobiliare.it, idealista, Casa.it) describe the property — square meters, rooms, price, energy class — but almost nothing about the *context* that truly determines quality of life: how reachable the metro stop really is on foot, how good and *accessible* the schools are, how noisy the street is, whether there are essential services (pharmacy, supermarket, kindergarten, doctor) within 10 minutes. The "near to" filter on portals uses straight-line distance, which location intelligence literature shows to be a poor predictor: travel times over pedestrian/public-transport networks capture value that Euclidean distance ignores.

2. **No explanation of the price.** The user sees an asking price but does not know whether it is consistent with the market of *that* micro-area. In Italy there is a public and free reference — the **OMI quotations** of the Agenzia delle Entrate, half-yearly, by homogeneous area and type (€/m² min–max) — but it is isolated from listings and unreadable for the average citizen. The gap between asking price and the area's OMI quotation is a huge awareness signal that today nobody puts into the user's hands.

3. **Impossible comparison and loss of memory.** Evaluating 15 properties in 4 different neighborhoods means keeping dozens of trade-offs in mind (this one costs less but is far from work; that one is perfect but on a street with no services). People do it with improvised Excel sheets and screenshots. There is no tool that models *the reasoning* — area ⇄ services ⇄ price ⇄ personal constraints — and makes it navigable and queryable.

### 1.2 Our answer: the area–services–prices graph

LocalMind models the domain as an **AI-queryable weighted graph**. Not a list of listings, but a network of relationships:

- The property is connected to its **OMI area**, to the **street**, to the **building**.
- The area is connected to the nearby **services/POIs** (schools, transport, healthcare, retail, green spaces) with edges **weighted by real travel time** (on foot / by car / by public transport), not by straight-line distance.
- The property is connected to the **market quotation** of its area/type, enabling automatic computation of the deviation between asking price ↔ market.
- The **user profile** (life places: work, children's school, gym; priorities; budget; constraints) itself becomes a subgraph that weighs and re-ranks everything else.

On this graph the local AI (GraphRAG) **navigates, explains and suggests**: it answers natural-language queries by combining relational constraints (paths, neighborhoods) and semantics (descriptions, reviews), and — the key point of the project — **surfaces non-obvious connections** ("this area costs 20% less than the adjacent one but has the same travel times to the center and better-rated schools").

### 1.3 Value by user type

| User | Concrete value |
|--------|-----------------|
| Buyer / tenant | Conscious choice: understands *why* a property fits their life, not just whether it matches the filters. Discovers alternative areas they would not have searched. |
| Investor | Sees price↔OMI deviations, potential rental yield, dynamics of services (improving area), risk. |
| Real estate agent | Advisory tool: presents the client not a listing but a defensible and traceable context dossier. |
| Local community | Enriches the graph with qualitative knowledge (noise, perceived safety, livability) that no portal possesses. |

### 1.4 Why LocalMind and not a portal

- **Local-first and privacy.** A person's life places (where I work, where the children go to school, how much I earn/spend) are extremely sensitive data. In LocalMind they stay on the user's node; the AI that processes them is Ollama running locally by default. No portal can offer this.
- **Neutrality.** We do not sell listings and we are not paid by advertisers: the graph is optimized for the *user's decision*, not for the click.
- **Universality of the engine.** The same engine that serves tourism and enterprise serves real estate: the node/relationship types change, the infrastructure does not. This cuts development and maintenance cost.

### 1.5 What it is NOT (value boundaries)

It is not a competing listings portal, it does not handle transactions/deeds/contracts, it is not a certified AVM (Automated Valuation Model) for legal appraisals. It is a **decision intelligence layer** on top of public market data, aggregated listings and contextual knowledge.

---

## 2. Personas & target users

| Persona | Profile | Main goal | Needs from the graph |
|---------|---------|----------------------|-------------------|
| **Giulia, 34 — first home** | Couple with a young child, budget 280–340k, works downtown | Family-suitable home without breaking the bank | Schools/kindergartens reachable on foot, time to work, fair price vs area, quiet area |
| **Marco, 41 — housing upgrade** | Sells and rebuys bigger, already knows the city | Optimize size/area for the same mortgage installment | Fine comparison between micro-areas, OMI deviation, value trend |
| **Sara, 29 — rent/relocation** | Relocating for work to a new city | The right neighborhood quickly, remotely | Exploration of unknown areas, safety, commute, social life |
| **Davide, 47 — investor** | Buys for rental income | Yield and risk | Potential yield, rental demand, price deviation, services that drive value |
| **Real estate agent** | Professional who uses LocalMind as a tool | Differentiating advisory for the client | Explainable area dossier, property comparison, report export |
| **Community contributor** | Resident who knows the area | Improve shared knowledge | Add/rate qualitative info about areas and services |
| **Self-hoster / data analyst** | Technician who installs LocalMind on-prem | Private and controlled real estate data pipeline | Connectors, graph API, total control of data |

**Anti-persona:** someone who simply looks for "the cheapest listing" without interest in context — better served by a traditional portal. LocalMind's value grows with the complexity of the decision.

---

## 3. Input requirements

This section is deliberately detailed: it defines *everything that enters* the graph and *how* the user and the connectors feed it. Inputs are divided into: user input (what they are looking for and who they are), property input (the objects), context input (area/services/market), configuration input (how to weight).

### 3.1 User input — search criteria

All fields are **optional and progressive**: the user can start from a natural-language sentence and refine. Each field must be validated at the boundary (range, enum, consistency).

| Category | Field | Type | Validation notes |
|-----------|-------|------|---------------------|
| Operation | Purchase / Rent | enum (IT/EN) | required to activate the correct pricing |
| Budget | min / max price or rent | integer ≥ 0 | max ≥ min; currency EUR |
| Type | apartment, villa, penthouse, studio… | enum (IT/EN) | maps to OMI types |
| Size | m² min/max, n. rooms, n. bathrooms | integer | plausible ranges |
| Condition | new, renovated, to be renovated | enum | |
| Features | elevator, terrace, garden, garage, energy class | flag + enum | class A–G |
| Location | city, neighborhoods/areas, radius | text + geo | at least one geographic anchor |
| Time constraints | availability by date | date | future |

### 3.2 User input — life profile (the differentiator)

It is the subgraph that personalizes the weights. Maximum privacy sensitivity: local data, never sent to the cloud without explicit consent.

- **Anchor places (anchor points):** work address(es), children's school/kindergarten, relatives' home, gym, etc., each with a **preferred mode** (on foot / bike / public transport / car) and a **maximum acceptable time**.
- **Household composition:** single, couple, family with children (ages), pets — guides which services matter.
- **Weighted priorities:** the user assigns relative importance (e.g. slider 0–100) to dimensions such as *quiet/silence*, *social life/venues*, *green*, *safety*, *short commute*, *price*, *schools*. These weights feed directly into the ranking on the graph.
- **Deal-breakers:** rigid non-negotiable constraints (e.g. "garage mandatory", "no ground floor", "max 15 min on foot from the metro").

### 3.3 Property input (the objects of the graph)

For each property, from a connector or manual entry:

| Field | Typical source | Required |
|-------|----------------|--------------|
| Identifier / listing URL | portal connector | yes |
| Address + geocoding (lat/lng) | listing + geocoder | yes (geo is critical) |
| Asking price / rent | listing | yes |
| Type, m², rooms, bathrooms, floor | listing | yes (type + m²) |
| Energy class, year, condition | listing | no |
| Features (garage, terrace, elevator…) | listing (text → extraction) | no |
| Textual description | listing | no (→ Qdrant embedding) |
| Photos | listing | no (→ multimodal, future) |
| Publication date / price history | connector | no (for trends) |

**Data quality rule:** a property without valid coordinates *does not enter the graph as a geolocated node* (it degrades to a "non-locatable" node, excluded from proximity queries). Geocoding is the linchpin of the entire domain.

### 3.4 Context input — services, areas, market

Fed by the connectors (section 6), updated periodically:

- **POIs / services:** schools, kindergartens, universities, public transport stops, supermarkets, pharmacies, hospitals/ASL, parks/green spaces, restaurants/venues, gyms, banks, post offices — each with category, geo, and attributes (e.g. school level, transport line(s)).
- **Mobility network:** road/pedestrian graph (OpenStreetMap) and public transport schedules (GTFS where available) to compute **real travel times**.
- **OMI areas:** perimeters of homogeneous areas + half-yearly quotations €/m² (min–max) by type, from the Agenzia delle Entrate (free CSV).
- **Qualitative data (community + open data):** noise, perceived safety/statistics, air quality, livability indices.

### 3.5 Configuration input (how to weight the graph)

To enable customization and self-hosting:

- **Default weights per service category** (e.g. how much a school counts vs a bar), overridable by the user profile.
- **Decay functions for distance/time** (a service 3 min away counts more than one 20 min away — configurable decay, e.g. exponential).
- **Freshness parameters:** how often to refresh OMI (half-yearly), POIs (monthly), listings (daily/hourly).
- **Deal-breaker thresholds → rigid filters** vs **preferences → soft weights**.

### 3.6 Summary of input flows

| Source | Cadence | Destination in the system |
|----------|---------|--------------------------|
| Search form + user profile | on-demand | user subgraph (MySQL) + runtime weights |
| Listings connector | daily/hourly | Property nodes + description embeddings (Qdrant) |
| POI connector (OSM/Overpass) | monthly | Service nodes + proximity edges |
| OMI CSV | half-yearly | Area nodes + Quotation nodes |
| GTFS / routing engine | monthly / on-demand | travel-time edge weights |
| Community contributions | continuous | qualitative attributes on Area/Service + votes |

---

## 4. Activity flow (step-by-step)

The flow describes the end-to-end path, from data ingestion to the user's conscious decision. It is divided into a **graph-building phase** (asynchronous, batch) and an **interaction phase** (synchronous, AI-driven).

### 4.1 Phase A — Graph construction and enrichment (batch)

**Step A1 — Property ingestion.** The listings connector (or manual upload) acquires the listings. Reuse of the existing \`document\`/\`batch\` module for orchestration and scheduling. Each listing is normalized into an \`Immobile\` (Property) node.

**Step A2 — Geocoding and placement.** Each address is geocoded (lat/lng). The property is connected via edges to \`Via\`/\`Edificio\` (Street/Building) and traced back to the \`ZonaOMI\` (OMI Area) whose perimeter contains it (point-in-polygon). Without valid geo → node marked as non-locatable.

**Step A3 — Semantic extraction.** The textual description is chunked and embedded into **Qdrant** (reuse of the existing Tika/embedding pipeline). Implicit features ("bright", "recently renovated", "near the park") are extracted via local LLM and promoted to attributes/relationships.

**Step A4 — Context ingestion.** The POI (OSM/Overpass), OMI (CSV) and mobility (OSM+GTFS) connectors populate \`Servizio\` (Service), \`ZonaOMI\`, \`Quotazione\` (Quotation) nodes and the mobility network.

**Step A5 — Computation of weighted proximity edges.** For each property/area, the **real travel times** toward the relevant services are computed (on foot, public transport, car) using the mobility graph — not straight-line distance. \`VICINO_A\` (NEAR_TO) edges are materialized with weight = function(time, mode, category). This is the most expensive edge to compute: it is done in batch and stored.

**Step A6 — Price↔market linkage.** Each property is connected to the OMI \`Quotazione\` of its area+type; the **percentage deviation** of asking price vs the market interval (below/in line/above) is computed and materialized.

**Step A7 — Derived area indices.** Aggregated indices are computed per area: *walkability* (density and accessibility of services), *functional mix*, *green*, *transport*, *average price*, *trend*. They become attributes of the \`ZonaOMI\` node.

**Step A8 — Connection suggestion (GraphRAG building).** The AI proposes non-obvious edges (areas that are "twins" by profile, services that drive value) — consistent with the project requirement to "surface non-obvious connections".

### 4.2 Phase B — Interaction and decision (synchronous)

**Step B1 — Expressing the need.** The user writes in natural language ("3 bedrooms under 350k, quiet area, good schools on foot, metro nearby") or fills in the progressive form (§3.1) and the life profile (§3.2).

**Step B2 — Parsing and grounding.** The local AI translates the request into: rigid filters (deal-breakers) + soft weights (preferences) + the profile's geographic anchors. The NL query is decomposed into the structured components of the graph.

**Step B3 — Hybrid execution on the graph.** The engine combines (a) **relational/structural filtering** on the MySQL graph (constraints, materialized proximity, price deviation) and (b) **semantic search** on Qdrant (descriptions matching the desiderata). Routing by query type, following 2026 hybrid GraphRAG best practices.

**Step B4 — Personalized scoring and ranking.** Each candidate property receives a score = weighted sum of contributions: proximity to the user's anchor places, coverage of priority services, price↔market alignment, semantic match. The weights come from the user's priorities (§3.2).

**Step B5 — Explanation (explainable).** For each result the AI generates an explanation **citing the graph nodes/paths** used: "9 min on foot from school X, metro Y 6 min away, price 8% below the area OMI, but busy street (−)". Source citation as per project requirement.

**Step B6 — Graph exploration.** The user navigates visually: expands from the property to the area, from the area to the services, jumps to suggested "twin" areas, applies filters by node/relationship type. Progressive exploratory navigation.

**Step B7 — Comparison and shortlist.** The user creates a shortlist; the system generates a multi-criteria **comparison matrix** and a dossier per property.

**Step B8 — Decision and follow-up.** Export of the dossier (PDF/report), saving the search, alerts on new properties that enter the graph and match the profile (reuse of the \`automation\`/\`messaging\` module).

**Step B9 — Feedback loop.** The user's actions (save, discard, "I'm not interested in this area") re-weight the personal graph and improve the suggestions — feeding the relationship weights (the engine's core requirement).

### 4.3 Synthetic flow diagram

\`\`\`
[Connectors] → A1 ingest → A2 geocoding → A3 embedding(Qdrant)
                                 ↓
        A4 context (POI/OMI/GTFS) → A5 real-time edges → A6 price↔OMI → A7 area indices → A8 AI links
                                 ↓
                         WEIGHTED GRAPH (MySQL + Qdrant)
                                 ↓
[User] B1 NL/form → B2 parsing → B3 hybrid query → B4 scoring → B5 explanation
                                 ↓
              B6 exploration ⇄ B7 comparison → B8 dossier/alert → B9 feedback (re-weight)
\`\`\`

---

## 5. Graph model (node types, relationship types, weighting criteria)

The model reuses the engine's generic node/edge schema (MySQL tables for the structure, Qdrant for the vectors). Below, the specialization for the real estate domain. All node and relationship types are IT/EN bilingual enums delivered to the frontend.

### 5.1 Node types

| Node type | Description | Main attributes | Qdrant vector |
|-----------|-------------|----------------------|----------------|
| \`Immobile\` | The unit for sale/rent | price, m², rooms, type, energy class, geo | yes (description) |
| \`Edificio\` | Building containing properties | year, construction type, geo | no |
| \`Via\` | Street axis | name, estimated traffic/noise | no |
| \`ZonaOMI\` | OMI homogeneous area | derived indices (walkability, green, average price, trend) | optional |
| \`Quartiere\` | Informal/community aggregation | name, perceived identity | yes (community description) |
| \`Comune\` / \`Città\` | Administrative level | population, geo | no |
| \`Quotazione\` | OMI market value | €/m² min–max, type, half-year | no |
| \`Servizio\`/\`POI\` | Point of interest | category, geo, specific attributes | optional |
| \`Scuola\` | Specialization of Service | level, rating | no |
| \`Trasporto\` | Stop/station | mode, lines | no |
| \`AreaVerde\` | Park/garden | surface area | no |
| \`ProfiloUtente\` | User's subgraph | priorities, household, budget | no (private) |
| \`LuogoAncora\` | Point of the user's life | type, geo, mode, max time | no (private) |
| \`Recensione\` | Qualitative community contribution | text, vote, dimension | yes |
| \`RicercaSalvata\` | Persisted query | criteria, weights | no |

### 5.2 Relationship types (edges)

| Relationship | From → To | Weighted? | Meaning |
|-----------|--------|---------|-------------|
| \`SI_TROVA_IN\` | Property → Building/Street/OMI Area | no | structural placement |
| \`APPARTIENE_A\` | OMI Area → Municipality | no | administrative hierarchy |
| \`VICINO_A\` | Property/Area → Service | **yes** | accessibility (real travel time) |
| \`RAGGIUNGE\` | Property → Anchor Place | **yes** | commute to user's life places |
| \`QUOTATO_DA\` | Property → Quotation | **yes** | price↔market deviation |
| \`SIMILE_A\` | Property → Property | **yes** | semantic/structural similarity |
| \`ZONA_GEMELLA\` | OMI Area → OMI Area | **yes** | equivalent profile, different price |
| \`SERVITA_DA\` | OMI Area → Transport | **yes** | connectivity of the area |
| \`CERCA\` | User Profile → criteria | no | user's intent |
| \`VIVE_A\` / \`LAVORA_A\` | User Profile → Anchor Place | no | personal anchors |
| \`RECENSISCE\` | Review → Area/Service | **yes** | weighted qualitative knowledge |
| \`PREFERISCE\` / \`SCARTA\` | User Profile → Property/Area | **yes** | feedback that re-weights the graph |

### 5.3 Edge weighting criteria

The weight is the heart of the engine. For real estate the main criteria are:

- **Real travel time (not Euclidean distance).** For \`VICINO_A\`/\`RAGGIUNGE\`: weight = decay function of door-to-door time for the chosen mode. A service 3 min away has weight ~1, at 20 min ~0.2 (configurable exponential decay). Location intelligence best practice: the pedestrian/transit network beats straight-line distance.
- **Relevance to the profile.** The same edge toward a school weighs a lot for a family, little for a single person: the effective weight in the ranking = base weight × user priority for that category.
- **Price↔market deviation.** For \`QUOTATO_DA\`: normalized weight of the % gap between asking price and the OMI interval (below market = positive signal).
- **Semantic + structural similarity.** For \`SIMILE_A\`/\`ZONA_GEMELLA\`: combination of cosine distance (Qdrant) and attribute match (type, price band, area indices).
- **Community reliability/consensus.** For \`RECENSISCE\`: weight = function of the number of concordant contributions, contributor reputation, recency (emergent ranking, as per the consumer vision).
- **User feedback.** \`PREFERISCE\`/\`SCARTA\` dynamically update the weights of the personal graph (implicit learning).
- **Freshness.** All weights decay with the obsolescence of the source data (e.g. a quotation several half-years old counts less).

The weights are **stored** (materialized in batch in MySQL) for the expensive components (travel times) and **recomputed at runtime** for the profile-dependent components (relevance, feedback).

---

## 6. Data sources & connectors (ingestion)

All connectors are implemented as **PF4J extension points** (\`DocumentParserExtension\`-like / new \`DataSourceConnectorExtension\`) and orchestrated by the \`batch\` module. Each connector is installable/uninstallable from the marketplace and configurable for self-hosting.

| Source | Type | Cadence | License/Notes | Output in the graph |
|-------|------|---------|--------------|------------------|
| **OMI — Agenzia delle Entrate** | CSV (quotations + area perimeters) | half-yearly | Free, official | \`ZonaOMI\`, \`Quotazione\` nodes |
| **OpenStreetMap / Overpass** | API/PBF | monthly | ODbL (attribution) | \`Servizio\`/\`POI\` nodes, road network |
| **GTFS public transport** | feed | monthly | Open for many Italian cities | \`SERVITA_DA\`/\`VICINO_A\` time weights |
| **Routing engine** (OSRM/Valhalla/GraphHopper self-host) | service | on-demand/batch | Open source, self-host | travel time computation |
| **Geocoder** (Nominatim self-host or API) | service | per ingest | respect usage policy | properties' lat/lng |
| **Portal listings** | scraping/feed/import | daily/hourly | **respect ToS and robots**; prefer feed/partner/manual import | \`Immobile\` nodes |
| **Municipal open data** (air, noise, safety) | CSV/API | variable | open | qualitative attributes \`ZonaOMI\` |
| **Community contributions** | internal UI | continuous | proprietary | \`Recensione\`, votes, attributes |
| **User import** (CSV/manual) | upload | on-demand | private | \`Immobile\`, profile |

**Responsibility notes (important):**
- Scraping of portals has legal and ToS constraints: the default connector favors **manual import / official feeds / partnerships**, and scraping is an optional extension under the self-hoster's responsibility.
- Everything is designed **local-first**: routing engine, geocoder and LLM run locally; no sensitive data (profile, anchors) leaves the instance without explicit consent.
- The connectors validate and normalize at the boundary (plausible geo, consistent prices, property dedup) before writing to the graph.

---

## 7. Features to create, develop and maintain (MVP → evolution)

### 7.1 MVP (first release of the domain)

| # | Feature | Type | LocalMind modules involved |
|---|--------------|------|----------------------------|
| 1 | Real estate graph schema (nodes/relationships §5) as a domain module | CREATE | \`knowledge\`/graph core, Flyway |
| 2 | OMI connector (CSV → ZonaOMI + Quotazione) | CREATE | \`batch\`, PF4J plugin |
| 3 | POI OSM/Overpass connector | CREATE | \`batch\`, plugin |
| 4 | Geocoding + area point-in-polygon | CREATE | infrastructure adapter |
| 5 | Property import (manual/CSV) + description embedding | CREATE/REUSE | \`document\`, Qdrant |
| 6 | Computation of \`VICINO_A\` edges with real times (routing self-host) | CREATE | \`batch\`, routing adapter |
| 7 | Price↔OMI linkage and % deviation | CREATE | graph core |
| 8 | Hybrid search (graph filters + semantics) with base scoring | CREATE/REUSE | graph core, Qdrant, \`llm\` |
| 9 | User profile + anchor places + weighted priorities | CREATE | new \`realestate\` domain |
| 10 | GraphRAG explanation with node/path citation | CREATE/REUSE | \`llm\` (Ollama), graph |
| 11 | Search UI + explained results (Angular \`realestate\` feature) | CREATE | lazy frontend feature |
| 12 | Area–services–property graph visualization (base) | CREATE | frontend |
| 13 | IT/EN bilingual enums (types, service categories, relationships) | CREATE | backend+frontend i18n |
| 14 | Flyway migrations (one query per file) | CREATE | \`localmind-app\` |

### 7.2 Evolution (subsequent releases)

| # | Feature | Value |
|---|--------------|--------|
| E1 | Derived area indices (walkability, green, mix, trend) | conscious choice, area comparison |
| E2 | Twin areas and suggestion of non-obvious connections (AI) | discovery, engine's core requirement |
| E3 | Multi-criteria comparison matrix + PDF dossier | decision and agent advisory |
| E4 | Alerts on new matching properties (automation/messaging) | retention, follow-up |
| E5 | Feedback loop that re-weights the personal graph | growing personalization |
| E6 | Community contributions + emergent ranking + moderation | qualitative knowledge, consumer vision |
| E7 | Yield/rental return estimate for investors | investor persona |
| E8 | Property photo analysis (multimodal Ollama) | attribute extraction from images |
| E9 | Portal feed/partner connectors, advanced dedup | data coverage |
| E10 | Temporal trends of prices/services (improving area) | purchase timing |
| E11 | "What-if" scenarios (job change → re-evaluate areas) | decision simulation |

### 7.3 To maintain (continuous maintenance)

- **Data refresh**: OMI half-yearly, POI monthly, listings daily; batch jobs monitored with metrics (Actuator/Prometheus already present).
- **Geo/dedup quality**: constant oversight of geocoding and property deduplication (it is the most fragile point).
- **Taxonomy alignment**: mapping of listing types ↔ OMI types ↔ POI categories when sources change.
- **Tuning of weights and decays**: periodic review of the weight functions based on feedback.
- **Connector legal compliance**: monitoring of source ToS/licenses.
- **Update of IT/EN translations** of the enums and UI texts.

---

## 8. AI / GraphRAG use cases

The AI (local Ollama by default; cloud optional with consent) operates **on the graph**, combining relational and semantic navigation, and always cites the nodes/paths used.

1. **Multi-constraint conversational search.** "Three-room under 350k, max 12 min on foot from the metro, well-rated elementary school nearby, price not above market." → the AI decomposes into filters+weights, navigates the graph, returns ordered results with explanation.

2. **Price explanation.** "Why does this cost more than the neighboring one?" → attribute comparison, area indices, OMI deviation, causal path on the graph.

3. **Discovery of alternative areas (non-obvious links).** "Show me cheaper areas with the same quality of life as Trastevere for my family." → \`ZONA_GEMELLA\` edges weighted by profile.

4. **Neighborhood dossier.** "What is it like to live in area X with two children?" → GraphRAG synthesis of services, schools, green, safety, price, with citations.

5. **Investor advisory.** "Where is it worth buying to rent out under 200k?" → price deviation, rental demand, services that drive value.

6. **Guided comparison.** "Compare these 3 properties for my situation." → multi-criteria matrix weighted on the profile + motivated recommendation.

7. **What-if.** "If I change jobs and move to area Y, which properties in my shortlist still make sense?" → recomputation of \`RAGGIUNGE\` weights.

8. **Community synthesis.** "What do residents say about noise on this street?" → weighted aggregation of the \`Recensione\`.

---

## 9. KPIs & success metrics

| Category | KPI | Indicative target |
|-----------|-----|-------------------|
| Data quality | % of correctly geocoded properties | > 97% |
| Data quality | % of properties linked to an OMI area | > 95% |
| Coverage | n. POIs/services per populated area | sufficient to cover key categories |
| Performance | hybrid search latency (p95) | < 2 s on a city-scale dataset |
| Performance | proximity-edge batch refresh time | within the nightly window |
| AI effectiveness | % of responses with correct node citation | > 90% |
| AI effectiveness | NL query decomposition accuracy | measured on an evaluation set |
| Engagement | properties in shortlist per session | increasing |
| Engagement | return rate + active saved searches | increasing |
| Value | price↔OMI deviation shown and understood (survey) | high comprehension |
| Community | n. of qualitative contributions and area coverage | increasing |
| Privacy/local | % of profile processing executed locally | 100% by default |

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---------|---------|-------------|
| **Legal constraints on portal scraping** | Listings source blocked | Default to import/feed/partnership; scraping as an optional extension borne by the self-hoster; respect ToS/robots |
| **Imprecise geocoding** | Wrong proximity and area | Self-host geocoder + validation + manual fallback; exclude nodes without valid geo |
| **Computational cost of real-time edges** | Slow batch | Materialize the weights, self-host routing engine, incremental computation only on deltas |
| **OMI data at area granularity, not point-level** | Approximate price estimate | Communicate the price as an *area market interval*, not an appraisal; combine with comparable listings |
| **Misaligned freshness** (OMI half-yearly vs daily listings) | Inconsistent signals | Weights with freshness decay; show the data's date |
| **Privacy of life places** | Highly sensitive data | Local-first, local Ollama AI, no cloud transmission without explicit consent, encryption at rest |
| **Bias/quality of community contributions** | Distorted ranking | Weights for reputation/consensus/recency, moderation, minimum thresholds |
| **MySQL graph quality on complex queries** | Performance/limits | Materialization, indices, hybrid query; re-evaluate a graph datastore only if necessary (out of scope now) |
| **Heterogeneity of source taxonomies** | Fragile mapping | Versioned normalization layer, regression tests on mapping |

---

## 11. Maintenance & evolution

- **Governed refresh cycles**: batch scheduler with alarms on failures, Prometheus/Grafana metrics already available in the stack; connector health dashboard.
- **Graph schema versioning**: every evolution of nodes/relationships goes through a Flyway migration (one query per file) and an update of the bilingual enums.
- **Extensibility via plugins**: new data sources (e.g. a regional portal, a municipal dataset) are added as PF4J plugins without touching the core; publishable on the marketplace.
- **Continuous weight tuning**: quarterly review of the decay functions and base weights, driven by feedback (\`PREFERISCE\`/\`SCARTA\`) and by KPIs.
- **Community curation**: moderation workflow for qualitative contributions, reputation management, anti-abuse.
- **Bilingual documentation**: every feature documented IT/EN (documentation/ + documentazione/); every development tracked in the \`Sviluppi/\` folder with dated naming.
- **Evolutionary roadmap**: MVP → area indices → twin areas/AI → community/emergent ranking → investment/yield → multimodal photos → what-if/scenarios.

---

## 12. Integration with existing LocalMind modules

| Existing module | Role in the real estate domain |
|------------------|-------------------------------|
| **\`knowledge\` / graph core** | Base of the engine: generic node/edge schema specialized for real estate. Starting point for the weighted graph. |
| **\`document\`** | Text extraction pipeline (Tika) and ingestion of listing descriptions; OCR for documents/floor plans. |
| **Qdrant (\`vectorstore\`)** | Embedding of property descriptions, community reviews, neighborhood descriptions for semantic search. |
| **\`llm\` + Ollama** | GraphRAG: NL parsing, scoring, explanations, extraction of implicit attributes; optional multi-provider fallback chain. |
| **\`batch\`** | Orchestration of ingestion jobs and computation of weighted edges (OMI, POI, routing). |
| **\`automation\` + \`messaging\`** | Alerts on new matching properties, notifications, recurring saved searches. |
| **\`marketplace\` + PF4J plugin** | Distribution of the real estate module and the data connectors as installable extensions. |
| **\`auth\`** | Protection of the user profile and sensitive data; multi-tenant local-first. |
| **\`agent\`** | AI real estate agent that orchestrates search, comparison and dossier autonomously. |
| **\`common\` (event/analytics)** | Domain events (property ingested, search saved) and usage analytics. |
| **Angular frontend (\`features/\`)** | New lazy \`realestate\` feature: search, explained results, graph visualization, comparison, profile; Signal store; \`TranslatePipe\` IT/EN; \`language-switcher\`. |
| **MySQL + Flyway** | Structure of the graph (nodes/edges/materialized weights) and versioned migrations (one query per file). |

**New domain to introduce:** \`realestate\` in \`localmind-domain\` (model/port-in/port-out/service, zero Spring), wired in \`DomainConfig.java\`, with \`/api/v1/realestate/*\` controllers, persistence adapters and connectors in infrastructure, and a dedicated Angular feature — following exactly the project structure's "Where to Add New Code" pattern.

---

### Sources consulted

- [Transforming Real Estate Search with Knowledge Graphs (Medium, 2026)](https://medium.com/@elevatetrust.ai/transforming-real-estate-search-with-knowledge-graphs-a-technical-deep-dive-afadc50fc137)
- [Leveraging Knowledge Graphs in Real Estate Search (Zillow)](https://www.zillow.com/news/leveraging-knowledge-graphs-in-real-estate-search/)
- [GraphRAG and LightRAG in 2026 (CallSphere)](https://callsphere.ai/blog/vw6g-microsoft-graphrag-knowledge-graph-2026)
- [Building a Real Estate Knowledge Graph (ScrapingAnt)](https://scrapingant.com/blog/building-a-real-estate-knowledge-graph-scraped-entities)
- [How Location Intelligence Is Changing Property Valuation Software (GISuser, 2026)](https://gisuser.com/2026/05/how-location-intelligence-is-changing-property-valuation-software/)
- [Location intelligence for proptech platforms — 2026 NAR report (Local Logic)](https://locallogic.co/blog/location-intelligence-proptech-platforms-2026-nar-report/)
- [Osservatorio del Mercato Immobiliare OMI — Agenzia delle Entrate](https://www.agenziaentrate.gov.it/portale/aree-tematiche/osservatorio-del-mercato-immobiliare-omi)
- [Quotazioni immobiliari OMI — open data (ondata, GitHub)](https://github.com/ondata/quotazioni-immobiliari-agenzia-entrate)
`;
