# Food Service & Venues

> Document part of documentation/15-extension-domains/ — LocalMind development guide. Date: 2026-06-29.

The **Food Service & Venues** scope is a vertical of the **consumer** group of LocalMind's universal Knowledge Graph engine. It extends the community-driven "Wikipedia of places" with a dedicated subgraph for restaurants, bars, trattorias, pizzerias, wine bars, cocktail bars, street food, pastry shops, and every other venue where people eat and drink. The goal is not to rebuild yet another reservation app or yet another review aggregator, but to make the territory's gastronomic knowledge **navigable by AI** through a weighted graph of nodes (venues, dishes, cuisines, people, events) and edges (relationships weighted by feedback, frequency, quality). Everything stays local-first, self-hostable, with Ollama AI by default and data that never leaves the instance without consent.

---

## 1. What We Solve (Problem & Value)

### 1.1 The Concrete Problem

Discovering where to eat well is today fragmented across silos that do not talk to each other and that reward marketing over real quality:

- **Fragmentation of sources.** Information about a single venue is scattered across maps (hours, location), review aggregators (judgments, photos), social media (trends, viral reels), the venues' own websites (menus, often outdated prices), messaging groups, and word of mouth. No tool recomposes them into a coherent, queryable view.
- **Unreliable and decontextualized reviews.** Mainstream platforms suffer from fake, incentivized, or vindictive reviews, from arithmetic averages that flatten everything ("3.8 stars" says nothing), and from a lack of context: an enthusiastic review from someone looking for cheap food is useless to someone looking for the atmosphere for an anniversary. The judgment is weighted neither for the reviewer's reliability nor for affinity with the reader.
- **Complex questions left unanswered.** Real searches are multi-constraint and relational: *"a tavern under €35 per person, near the theater, open Monday evening, with gluten-free options, a quiet atmosphere for talking, where they make a good cacio e pepe"*. Current engines poorly handle the intersection of geographic, price, availability, dietary, atmosphere, and single-dish quality filters.
- **Loss of local knowledge.** Knowledge about historic trattorias, niche venues, chefs who change their cuisine, seasonal dishes, and pairings with local events lives in people's heads and gets dispersed. There is no open, self-hostable digital commons to preserve it.
- **Dependence on Big Tech and loss of data sovereignty.** Restaurateurs and communities are prisoners of platforms that own the data, impose opaque rankings, and monetize visibility. A pro-loco, a merchants' association, or an independent gastronomic guide has no tool of its own, free and controllable.

### 1.2 The Value of LocalMind

LocalMind addresses these problems by transforming local food service into an **AI-navigable, weighted knowledge graph**, with three distinctive levers:

| Lever | What changes | Why it matters |
|------|-------------|--------------|
| **Relational graph** instead of flat lists | Venues, dishes, cuisines, people, places, and events are nodes connected by weighted edges | Enables answers to relational questions ("where to eat before tonight's concert") and surfaces non-obvious connections |
| **Weight emerging from the community** | Ranking arises from feedback, reviewer reliability, freshness, and consistency, not from marketing | Fights fake reviews and misleading averages; real quality emerges |
| **Local-first GraphRAG** | The AI navigates the graph + semantics (Qdrant) to answer and cite the nodes used | Explainable, contextualized answers, without sending data to external services |

The value plays out for each actor:

- **For those looking for where to eat:** a conversational, justified, and contextual answer ("I suggest X because it's near the theater, under budget, and three reliable reviewers with your same tastes praise the fresh pasta"), instead of a list to filter by hand.
- **For the community (foodies, reviewers, associations):** an open tool where quality contributions carry real weight and build a commons, not value for a third-party platform.
- **For restaurateurs:** a presence based on facts (updated menus, hours, allergens) and on merit, not on advertising spend.
- **For territorial bodies (pro-loco, municipalities, independent guides):** a self-hosted engine to enhance the local gastronomic offering, integrable with events, itineraries, and tourism.

### 1.3 Differentiation from the State of the Art

Unlike centralized aggregators (which own the data and govern its visibility) and generative search engines (which read others' structured data), LocalMind offers a **proprietary, self-hostable, open-source engine** in which the organization controls the schema, weights, moderation, and AI model. The 2026 trends confirm the direction: more than one-fifth of consumers already use AI to discover venues, discovery is shifting toward smart content and recommendations, and "AI-recommendability" depends on the quality of structured data. LocalMind brings this capability *inside* the user's instance, without lock-in.

---

## 2. Personas & Target Users

| Persona | Description | Main needs | How they use the scope |
|---------|-------------|--------------------|-------------------|
| **Gastronomic explorer** (consumer) | Citizen or tourist looking for where to eat now or on a specific occasion | Fast, contextual, reliable answers; filters for budget, diet, atmosphere | Conversational AI chat, search, graph navigation, saving favorites |
| **Foodie / community reviewer** | Enthusiast who contributes reviews, photos, detailed ratings | Recognition of quality contributions; reputation; granular review tools | Creates/updates venue and dish nodes, writes multi-dimensional reviews, votes |
| **Curator / moderator** | Trusted member who validates contributions, merges duplicates, handles reports | Moderation tools, anti-spam, reviewer reliability management | Moderation queue, node merge, weight and reputation management |
| **Restaurateur / venue manager** | Owner who claims and updates their venue's listing | Correct data (menu, hours, allergens), merit-based response | Claim of the venue node, updating attributes and menu, responding to reviews |
| **Territorial body / pro-loco / independent guide** | Organization that promotes the territory's gastronomic offering | Self-hosting, integration with events and itineraries, editorial control | Installs the instance, links food service to events/places, curates themed collections |
| **Event and group planner** | Whoever organizes group dinners, corporate events, recurrences | Intersection of constraints (large group, mixed diet, near a venue) | Complex GraphRAG queries, generation of justified proposals |
| **Instance administrator** (enterprise/technical) | Whoever installs and configures LocalMind on-premise | Domain schema configuration, connectors, AI model, privacy | PF4J module management, connector mapping, weight tuning |

The scope is predominantly **consumer**, but it reuses the enterprise infrastructure (connectors, ingestion, privacy) for hybrid scenarios such as restaurant chains that want an internal graph of their own offering.

---

## 3. Input Requirements

This section defines precisely *what* the system needs as input to populate and maintain the food-service subgraph. The requirements are grouped by category; each field indicates whether it is mandatory, its type, validation, and source.

### 3.1 Venue Master Data (node `Locale`)

| Field | Mand. | Type | Validation | Notes |
|-------|:------:|------|-------------|------|
| `nome` | Yes | text | 2–160 chars, no HTML | Trade name |
| `tipoLocale` | Yes | enum `TipoLocale` | value in the bilingual enum | restaurant, trattoria, tavern, pizzeria, bar, cocktail_bar, wine bar, pub, street_food, pastry shop, gelato shop, café, agritourism, bistro |
| `posizione` | Yes | geo (lat, lon) | lat ∈ [-90,90], lon ∈ [-180,180] | Geocoded address |
| `indirizzo` | Yes | structured | street, number, postal code, city, province, country | Normalized |
| `orariApertura` | Recommended | weekly structure | valid time slots, holiday handling | For "open now/Monday evening" queries |
| `fasciaPrezzo` | Recommended | enum `FasciaPrezzo` | budget/medium/high/luxury + €/person range | Dual symbolic and numeric representation |
| `cucineServite` | Yes | list of refs to `Cucina` nodes | at least 1 | Relationship to cuisine nodes |
| `contatti` | Optional | phone, email, website, social | valid format per channel | |
| `servizi` | Optional | set of flags | enum `ServizioLocale` | takeaway, delivery, reservation, accessibility, parking, outdoor seating, wifi, pet-friendly, accepts cards |
| `capienza` | Optional | integer | > 0 | For group queries |
| `statoAttivita` | Yes | enum | active, temp_closed, perm_closed, opening | Default: active |
| `lingua` | Yes | locale enum | IT/EN minimum | For content i18n |

### 3.2 Menus and Dishes (nodes `Menu`, `SezioneMenu`, `Piatto`)

Model inspired by the schema.org hierarchy (Menu → MenuSection → MenuItem → Offer), adapted to the graph:

| Field | Mand. | Type | Validation | Notes |
|-------|:------:|------|-------------|------|
| `nomePiatto` | Yes | text | 2–120 chars | |
| `sezione` | Recommended | enum/text | appetizer, first course, main course, dessert, beverage, pizza, cocktail… | |
| `descrizione` | Optional | text | max 600 chars | Indexed in Qdrant for semantic search |
| `prezzo` | Recommended | decimal + currency | ≥ 0 | Historicized to track variations |
| `ingredientiChiave` | Optional | list of refs to `Ingrediente` nodes | | For "where they make X with Y" queries |
| `allergeni` | Recommended | enum set `Allergene` | EU taxonomy of 14 allergens | Critical dietary filter |
| `adattoADieta` | Optional | enum set `Dieta` | vegetarian, vegan, gluten-free, halal, kosher, low-carb | Maps schema.org `suitableForDiet` |
| `stagionalita` | Optional | enum/period | | Seasonal dishes |
| `disponibilita` | Optional | enum | always, seasonal, on_reservation, sold_out | |

### 3.3 Reviews and Ratings (node `Recensione` + weighted edges)

Reviews are **multi-dimensional** (no single vote) to feed granular weights:

| Field | Mand. | Type | Validation | Notes |
|-------|:------:|------|-------------|------|
| `autore` | Yes | ref to `Persona` node | authenticated user | Links to reputation |
| `localeRiferito` | Yes | ref to `Locale` node | existing | |
| `piattoRiferito` | Optional | ref to `Piatto` node | existing | Dish-level review |
| `votoCibo` | Yes | scale 1–5 | integer/half point | Cuisine quality dimension |
| `votoPrezzo` | Recommended | scale 1–5 | | Value for money |
| `votoAtmosfera` | Recommended | scale 1–5 | | Ambiance, noise, decor |
| `votoServizio` | Recommended | scale 1–5 | | Hospitality |
| `testo` | Recommended | text | 0–4000 chars, sanitized | Indexed in Qdrant |
| `contestoVisita` | Optional | enum | couple, family, work, friends, solo | For reader affinity |
| `prezzoSpeso` | Optional | decimal | | Real price signal |
| `foto` | Optional | media | type/size/EXIF strip | Privacy: metadata removal |
| `dataVisita` | Recommended | date | ≤ today | Freshness of the judgment |

Anti-abuse validations: one review per author/venue within a time window (configurable), rate limiting, duplicate/spam text detection, reputation threshold for votes that weigh more.

### 3.4 Relationships with Places and Events (relational input)

- **Proximity/belonging to places**: linking the venue to `Luogo`/`POI`/`Quartiere`/`Itinerario` nodes already present in the consumer graph (e.g., "near the Duomo", "in district X").
- **Association with events**: linking to `Evento` nodes (concert, fair, festival) for "where to eat before/after the event" or "venues of the festival" queries.
- **Gastronomic itineraries**: input to build routes (cicchetti crawl, street food tour) as sequences of venue nodes.

### 3.5 Input from Connectors (see section 6)

- Structured files (CSV/JSON) of venue master data and menus.
- Unstructured documents (PDF menus, flyers) → extraction via existing Tika/OCR.
- Feeds from open georeferenced sources.
- Email/messages (`email`, `messaging` modules) with reports or updates.

### 3.6 Non-Functional Requirements on Inputs

- **Boundary validation**: every input validated with a schema before persistence (project rule), fail-fast with clear bilingual messages.
- **Immutability**: updates produce new versions of the node/relationship, not in-place mutations (historicization of prices, hours, votes).
- **Privacy**: personal data (authors, photos) handled locally; nothing sent to external providers without consent; EXIF stripping on images.
- **i18n**: every textual content can have IT/EN variants; enums are translated and routed to the frontend according to the language switch.
- **Provenance**: every node/edge tracks its source (manual, connector, AI-suggested) and timestamp for audit and weights.

---

## 4. Activity Flow (step-by-step)

The main end-to-end flows are described. Each flow indicates the actor, steps, LocalMind modules involved, and validation points.

### 4.1 Flow A — Ingestion/creation of a venue

1. **Trigger**: a foodie manually creates a venue from the frontend, or a connector/batch imports a file, or the AI proposes a node extracted from a document.
2. **Input validation** (API layer): the mandatory fields (3.1) are verified with a schema; geocoding and address normalization; duplicate check by proximity + name (fuzzy match) → if a duplicate is suspected, a merge is proposed.
3. **Creation of `Locale` node** (domain service `knowledge`/scope): immutable node with `draft` status if from the community, `published` if from a trusted source or after moderation.
4. **Linking edges**: creation of edges toward `Cucina`, `Luogo`/`POI` (proximity), and possibly `Evento` nodes.
5. **Semantic indexing**: description and textual attributes → embedding in Qdrant (reuse of the existing pipeline) for search and GraphRAG.
6. **Persistence**: node and edges on MySQL (graph structure), vectors on Qdrant; publication of a `DomainEvent` (e.g., `LocaleCreatoEvent`).
7. **Moderation** (if draft): it enters the curators' queue; upon approval the status becomes `published` and the initial weight of the edges is set.

### 4.2 Flow B — Adding menus and dishes

1. **Trigger**: a restaurateur/foodie adds a menu, or uploads a PDF menu.
2. **Extraction** (if a document): Tika/OCR extracts text; an AI step (Ollama) structures the text into candidate sections and dishes (name, description, price, inferred allergens) → presented for human confirmation.
3. **Validation**: dish fields (3.2); allergens and diets normalized against the taxonomy; price with currency.
4. **Creation of `Piatto` nodes** and `SERVE` (Locale→Piatto), `CONTIENE` (Piatto→Ingrediente), `ADATTO_A` (Piatto→Dieta) edges.
5. **Indexing**: dish descriptions in Qdrant.
6. **Historicization**: price variations create new versions; the current price is the most recent valid one.

### 4.3 Flow C — Community review (the heart of the weight)

1. **Authentication**: the user is logged in (`auth` module); their `Persona` node and reputation are retrieved.
2. **Compilation**: multi-dimensional votes (food, price, atmosphere, service), text, visit context, optional photos, visit date (3.3).
3. **Anti-abuse validation**: rate limiting, one review per window, spam/duplicate detection, photo EXIF stripping.
4. **Creation of `Recensione` node** and `RECENSISCE` edge (Persona→Locale, opt. →Piatto) with the vote payload.
5. **Weight recalculation** (asynchronous via `RecensioneCreataEvent`):
   - update of the weight of the `SERVE` edges / quality of the venue and of the reviewed dishes;
   - the review's contribution is **weighted** by the author's reputation, freshness (temporal decay), and consistency (outlier detection);
   - update of the author's reputation based on usefulness ("useful" votes from others) and consistency with the consensus.
6. **Indexing** of the review text in Qdrant (feeds GraphRAG and semantic search).
7. **Notification/moderation**: any reports enter the curators' queue.

### 4.4 Flow D — Conversational discovery (GraphRAG, the main user experience)

1. **Natural-language question**: e.g., *"a tavern under €35, near the Teatro Comunale, open tonight, with gluten-free first courses and a quiet atmosphere"*.
2. **Intent parsing** (Ollama default): extraction of constraints → reference location/POI, price range, time, diets, atmosphere attributes, venue type.
3. **Hybrid retrieval**:
   - **structural on the graph** (MySQL): filters venues by type, proximity to the "theater" POI, hours (open tonight), price range, presence of `ADATTO_A` gluten-free dishes;
   - **semantic** (Qdrant): matching on descriptions/reviews for "quiet atmosphere", quality of first courses;
   - **neighborhood expansion**: from the `Luogo`=theater node, the `VICINO_A`/`NEL_QUARTIERE` edges are explored.
4. **Weighted ranking**: combination of edge weights (emergent quality), affinity with the user's tastes (history/favorites), freshness, and satisfied constraints.
5. **Answer generation**: the AI composes a justified answer and **cites the nodes/paths** used (venue, relevant reviews, distance from the theater, gluten-free dishes).
6. **Interaction**: the user can refine ("cheaper", "outdoors"), save to favorites, open the graph view to explore the connections.

### 4.5 Flow E — Visual graph exploration

1. The user opens the **graph view** from a venue node or from an AI answer.
2. Progressive expansion by relationships: cuisines, dishes, reviewers, nearby places, linked events, "similar" venues.
3. Filters by node/relationship type and by minimum edge weight (show only strong connections).
4. From an event node → linked venues; from a dish → other venues that serve it well.

### 4.6 Flow F — Maintenance and curation

1. Moderation queue: drafts, reports, suspected duplicates.
2. Curator actions: approve/reject, **merge** of duplicate nodes (preserving edges and reviews), attribute correction, reputation management.
3. Periodic jobs: weight freshness decay, ranking recalculation, detection of likely-closed venues (absence of signals), updating of expired prices.

---

## 5. Graph Model (node types, relationship types, weighting criteria)

The subgraph reuses the generic engine (typed nodes + weighted edges on MySQL, semantics on Qdrant) introducing domain-specific types. No Neo4j: the structure lives in relational tables, the semantics in vectors.

### 5.1 Node Types

| Node type | Description | Key attributes |
|-----------|-------------|------------------|
| `Locale` | Restaurant/bar/venue | nome, tipoLocale, posizione, fasciaPrezzo, hours, services, status |
| `Cucina` | Cuisine type/tradition | nome, region/origin, descrizione |
| `Piatto` | Menu item | nome, sezione, prezzo, allergeni, diets, seasonality |
| `Ingrediente` | Component of a dish | nome, category, allergen? |
| `Menu` / `SezioneMenu` | Containers of dishes | nome, period (lunch/dinner), validity |
| `Recensione` | Multi-dimensional rating | votes, testo, context, dataVisita |
| `Persona` | Reviewer/foodie/curator/manager | reputation, tastes, role |
| `Luogo`/`POI` | Territorial point of interest (reuse) | nome, posizione, type |
| `Quartiere`/`Zona` | Geographic area (reuse) | nome, boundaries |
| `Evento` | Territory event (reuse) | nome, date, place |
| `Itinerario` | Gastronomic route | stops, theme |
| `Collezione`/`Lista` | Curated collection ("best pizzerias") | title, curator |
| `Tag`/`Attributo` | Cross-cutting labels (e.g., "romantic", "noisy", "sea view") | nome, category |

### 5.2 Relationship Types (edges)

| Relationship | From → To | Meaning | Weighted? |
|-----------|--------|-------------|:------:|
| `SERVE` | Locale → Piatto | the venue offers the dish | Yes (quality of the dish at the venue) |
| `APPARTIENE_A_CUCINA` | Locale/Piatto → Cucina | gastronomic classification | Yes (relevance) |
| `CONTIENE` | Piatto → Ingrediente | composition | No (or light) |
| `ADATTO_A` | Piatto → Dieta | dietary suitability | No (factual) |
| `RECENSISCE` | Persona → Locale/Piatto | review/rating | Yes (weighted contribution) |
| `VICINO_A` | Locale → Luogo/POI | geographic proximity | Yes (inverse distance) |
| `NEL_QUARTIERE` | Locale → Quartiere | zone membership | No |
| `COLLEGATO_A_EVENTO` | Locale → Evento | relevant to the event | Yes (pertinence) |
| `TAPPA_DI` | Locale → Itinerario | part of a route | Yes (order/relevance) |
| `INCLUSO_IN` | Locale → Collezione | curated in a list | Yes (position/curation) |
| `HA_ATTRIBUTO` | Locale → Tag/Attributo | atmosphere/characteristic | Yes (frequency in reviews) |
| `SIMILE_A` | Locale → Locale | similarity (AI/semantic) | Yes (similarity score) |
| `GESTITO_DA` | Locale → Persona | manager claim | No (factual) |
| `SEGUE`/`PREFERISCE` | Persona → Locale/Cucina | user's tastes | Yes (preference strength) |

### 5.3 Criteria for Edge Weight

Weight is the heart of the value: it makes quality *emergent* and the AI capable of ordering. For quality edges (e.g., `SERVE`, `HA_ATTRIBUTO`, overall venue quality) the weight is a configurable function of the following factors:

| Factor | Effect on weight | Notes |
|---------|------------------|------|
| **Reviewer reliability** | Reviews from high-reputation users weigh more | Reputation grows with usefulness and consistency of contributions |
| **Volume and consensus** | More concordant reviews → more stable and higher weight | Mitigates the single extreme judgment |
| **Freshness (temporal decay)** | Recent reviews weigh more than old ones | Configurable decay (e.g., half-life) |
| **Consistency / outlier detection** | Judgments anomalous with respect to the consensus weigh less | Anti-manipulation |
| **Dimensional specificity** | `food`/`price`/`atmosphere`/`service` votes update distinct edges | Avoids the single flattening average |
| **Usage/navigation frequency** | Frequently visited or saved nodes/edges reinforce the weight | Relevance signal |
| **Proximity (for `VICINO_A`)** | Weight inversely proportional to distance | Normalized on a threshold |
| **Event pertinence (for `COLLEGATO_A_EVENTO`)** | Temporal/spatial distance from the event | |
| **Curation** | Inclusion in collections curated by trusted curators raises the weight | |

Principles: weights are **derived and recomputable** (never a destructive mutation of the raw data: reviews remain, the weight is an aggregate), the formula is **configurable per instance** (one body may prioritize freshness, another consensus), and every weight is **explainable** (the AI can cite why a venue is at the top).

---

## 6. Data Sources & Connectors (ingestion)

Ingestion reuses the existing document and batch pipeline, adding specific connectors. Everything stays local-first.

| Source | Mode | Reused LocalMind module | Output in the graph |
|-------|----------|--------------------------|------------------|
| **Manual community contributions** | Frontend form (venues, dishes, reviews) | `knowledge`, `auth`, API | `Locale`/`Piatto`/`Recensione` nodes + edges |
| **Structured files** (CSV/JSON master data, menus) | Batch import | `batch`, dedicated connector | Nodes and edges in bulk |
| **Unstructured documents** (PDF menus, flyers, brochures) | Upload or folder watcher → Tika/OCR → AI structuring | `document`, `batch`, `llm` (Ollama) | Candidate dishes/menus for confirmation |
| **Open georeferenced feeds** (local public data) | Synchronization connector | new connector (PF4J plugin) | `Locale`/`POI` nodes |
| **Email/reports** | Existing IMAP ingestion | `email` | Updates/reports in queue |
| **Messaging channels** | Bot/channels | `messaging` | Contributions and questions |
| **Territory events** | Reuse of the consumer events subgraph | `knowledge`, `calendar` | `COLLEGATO_A_EVENTO` edges |
| **MCP tools** | External tools invokable by the AI | `mcp` | On-demand enrichment |

Ingestion guidelines:
- **Extensibility via PF4J plugins**: every new source is a connector-plugin installable from the marketplace, without touching the core.
- **Deduplication**: every connector passes through the name+geo fuzzy match before creating nodes.
- **Provenance and consent**: every imported node tracks the source; external sources require explicit consent (privacy).
- **Local structuring AI**: the document→nodes transformation uses Ollama by default, never the cloud without opt-in.

---

## 7. Features to Create, Develop, and Maintain (MVP → evolution)

A concrete map of the work, distinguishing what is **MVP** (necessary for the first value) from what is **evolution**. The columns indicate whether the feature is to be **created** (new), **developed** (extension of existing), or **maintained** (continuous operation).

### 7.1 MVP

| Feature | Type | Modules involved |
|--------------|------|------------------|
| Food-service domain schema (node/edge types, bilingual enums) | Create | `knowledge`/domain, Flyway |
| Flyway migrations for node/edge/review tables (one query per file) | Create | `app` |
| `Locale` node CRUD (API + service + JPA adapter) | Create/Develop | `knowledge`, `infrastructure`, `api` |
| `Piatto`/`Menu` CRUD with allergens and diets | Create | as above |
| Multi-dimensional reviews + `Persona` node / base reputation | Create | `auth`, `knowledge` |
| Weighting engine v1 (reputation + freshness + consensus) | Create | domain service + events |
| Semantic indexing of descriptions/reviews on Qdrant | Develop | `vectorstore`, existing pipeline |
| Structured search (filters: type, price, diet, hours, proximity) | Create | `api`, MySQL graph queries |
| GraphRAG v1: conversational chat over the subgraph with citations | Create/Develop | `llm` (Ollama), `knowledge` |
| Batch import of CSV/JSON venues and menus | Develop | `batch` |
| Menu extraction from PDF (Tika/OCR → AI structuring) | Develop | `document`, `llm` |
| Frontend: venue page, review form, search, chat | Create | Angular `ristorazione` feature |
| IT/EN i18n of UI and scope enums | Create | frontend + backend enums |
| Base moderation queue (approve/reject drafts) | Create | `knowledge`, `api`, frontend |

### 7.2 Evolution

| Feature | Type | Modules involved |
|--------------|------|------------------|
| Interactive graph view with expansion and weight filters | Create | frontend (graph viz) |
| Weighting engine v2: outlier detection, affinity with user tastes, configurable decay | Develop | domain |
| Personalized recommendations ("for you") based on `PREFERISCE` | Create | `llm`, domain |
| AI-generated gastronomic itineraries (cicchetti crawl, food tour) | Create | `knowledge`, `llm` |
| Venue↔event linking and contextual suggestions | Develop | `calendar`, `knowledge` |
| Venue claim by the manager + response to reviews | Create | `auth`, `knowledge` |
| Curated collections/lists ("best pizzerias downtown") | Create | `knowledge`, frontend |
| AI suggestion of missing connections between nodes (similar venues, cuisines) | Create | GraphRAG |
| Connector-plugins for open georeferenced feeds (marketplace) | Create | `plugin` PF4J, `marketplace` |
| Automatic detection of closed venues/expired prices | Create | `batch`/scheduler |
| Advanced reputation system (badges, levels, anti-sybil) | Develop | `auth`, domain |
| Multimodal: dish photo analysis (quality, pairing) | Develop | existing multimodal adapters |
| Export/sharing of collections and listings | Develop | `common`/backup |

### 7.3 Continuous Maintenance

- Updating taxonomies (cuisines, allergens, diets) and bilingual enums.
- Periodic tuning of weighting and reputation formulas.
- Management of the moderation queue, anti-spam, duplicate merging.
- Freshness/decay jobs and ranking recalculation.
- Updating connectors as sources change.
- Constant updating of the IT/EN documentation (project rule) and tracking of developments in the `Sviluppi/` folder.

---

## 8. AI / GraphRAG Use Cases

The AI navigates the weighted graph by combining structural retrieval (MySQL) and semantic retrieval (Qdrant), citing the nodes/paths used. Concrete examples:

| Use case | Sample question | How the AI answers on the graph |
|------------|--------------|------------------------------|
| **Multi-constraint discovery** | "Tavern under €35 near the theater, open tonight, gluten-free first courses" | Structural filters (price, hours, POI proximity, `ADATTO_A` gluten-free) + semantics on first-course quality; weighted ranking; cites venues and reviews |
| **Event-contextual recommendation** | "Where to dine before Friday's concert?" | From the `Evento` node it expands `COLLEGATO_A_EVENTO`/`VICINO_A` to the event's location; filters by pre-concert time |
| **Taste affinity** | "Recommend something new that I'll like" | Uses `PREFERISCE`/history, finds `SIMILE_A` to beloved venues, excludes already-visited ones |
| **Justified comparison** | "Better X or Y for a romantic dinner?" | Compares `HA_ATTRIBUTO`=romantic weights, atmosphere, "couple"-context reviews |
| **Itinerary** | "Organize a walking cicchetti crawl downtown" | Builds a sequence of `Locale` nodes by proximity and theme, optimizes the route |
| **Dish-centric** | "Where do they make the best cacio e pepe in the area?" | Navigates `SERVE`→`Piatto` edges with quality weight + dish-level reviews |
| **Group dietary constraints** | "Dinner for 8, one vegan and one celiac, near the office" | Intersects capacity, diets (`ADATTO_A`), proximity to the office POI |
| **Non-obvious connections** | "Suggest venues similar to one I love but in another district" | `SIMILE_A` + change of `NEL_QUARTIERE` constraint |
| **Explanation** | "Why are you recommending this?" | The AI cites weights, reliable reviews, distance, satisfied constraints |

All cases run with **Ollama by default**; cloud providers are optional and user-activatable. The answers are **explainable** thanks to the citation of nodes and weights.

---

## 9. KPIs & Success Metrics

| Category | KPI | Goal/signal |
|-----------|-----|-------------------|
| **Graph coverage** | No. of `Locale`/`Piatto` nodes with complete attributes | Steady growth; % "complete" listings |
| **Data quality** | % of venues with updated hours, prices, allergens (<90 days) | High freshness |
| **Community engagement** | No. of reviews/week; % recurring reviewers | Active community |
| **Reliability** | Rate of reported/removed reviews; outliers identified | Low spam |
| **AI effectiveness** | % of queries resolved without refinements; click/save on the suggested result | High pertinence |
| **Explainability** | % of answers with node/path citation | ~100% |
| **Satisfaction** | "Useful" vote on AI answers; user return | Positive trend |
| **Performance** | Hybrid retrieval latency (graph+vectors) | Within the UX threshold |
| **Local-first** | % of AI processing performed locally (Ollama) | Default 100% barring opt-in |
| **Privacy** | Events of external data transmission without consent | Zero |
| **Extensibility** | No. of connector-plugins installed | Marketplace adoption |

---

## 10. Risks & Mitigations

| Risk | Impact | Mitigation |
|---------|---------|-------------|
| **Fake reviews/manipulation** | Polluted ranking, loss of trust | Weighting by reputation, outlier detection, rate limiting, thresholds, moderation |
| **Stale data** (menus/prices/closures) | Wrong answers | Historicization, freshness decay, closed-venue detection jobs, manager claim |
| **Cold start / empty graph** | Little initial value | Batch import + connectors + PDF extraction for seeding; community incentives |
| **Node duplicates** | Fragmentation, diluted weights | Geo+name fuzzy match, curator merge |
| **Relational query performance on MySQL** (no Neo4j) | Latency on large subgraphs | Targeted indexes, neighborhood denormalization, caching, weight pre-aggregation |
| **Wrong allergens/diets** | Health risk | Taxonomy validation, human confirmation, disclaimer, no unverified inference published as certain |
| **AI bias in recommendations** | Homogenization | Diversity in ranking, transparency of criteria |
| **Personal data privacy** (photos, authors) | Compliance | EXIF stripping, local data, consent, minimization |
| **Moderation overload** | Backlog, declining quality | Anti-spam automation, reputation, distributed curation |
| **OCR menu extraction quality** | Wrong dishes | Mandatory human confirmation post-extraction |

---

## 11. Maintenance & Evolution

- **Evolving schema**: new node/edge types and attributes are added as extensions of the module, with incremental Flyway migrations (one query per file) and updated bilingual enums.
- **Weight tuning**: the weighting and reputation formulas are configurable per instance and should be reviewed periodically against the KPIs; every change documented in `Sviluppi/`.
- **Taxonomies**: cuisines, ingredients, allergens, diets, and tags must be kept updated and translated IT/EN.
- **Connectors**: updated as sources change; new connectors distributed as PF4J plugins via the marketplace without touching the core.
- **Graph quality**: recurring jobs for freshness, deduplication, closed-venue detection, expired prices.
- **AI models**: updating of local Ollama models; periodic evaluation of GraphRAG quality.
- **Documentation**: constant updating of the IT/EN docs with every development (project constraint); functional news is reflected in `documentation/` and `documentazione/`.
- **Architectural consistency**: the domain stays pure (no Spring), wiring in `DomainConfig`, small and cohesive files, immutable patterns.

---

## 12. Integration with Existing LocalMind Modules

| LocalMind module | Role in the food-service scope |
|------------------|-------------------------------|
| `knowledge` | Base of the graph engine: hosts node/edge types and logic of the food-service subgraph |
| `llm` (Ollama default + cloud opt.) | Intent parsing, GraphRAG, menu structuring from documents, recommendations; existing fallback chain |
| `document` + `batch` | Menu/master-data ingestion (Tika/OCR, folder watcher, import jobs) |
| Qdrant (`vectorstore`) | Semantic indexing of descriptions, dishes, and reviews for hybrid retrieval |
| MySQL + Flyway | Persistence of the graph structure (nodes, edges, reviews); incremental one-query migrations |
| `auth` | Identity of reviewers/curators/managers, reputation, permissions, venue claim |
| `calendar` | Linking to territory events for contextual suggestions |
| `email` + `messaging` | Channels for reports, updates, and conversational bots |
| `mcp` | External tools invokable by the AI to enrich on-demand |
| `plugin` (PF4J) + `marketplace` | Ingestion connectors and domain modules installable without touching the core |
| `agent` + `automation` | Jobs and agents for moderation, freshness, closure detection, weight recalculation |
| `common` | Domain events, analytics, backup/export of collections |
| Angular frontend (`ristorazione` feature) | Venue/menu pages, review form, search, GraphRAG chat, graph view; standalone + Signals; IT/EN i18n |

**Constraints respected**: local-first and self-hostable; Ollama AI by default with optional cloud and consent; reuse of MySQL + Qdrant (no Neo4j); extensibility via PF4J; privacy of personal data; bilingual IT/EN documentation and enums; hexagonal architecture with a pure domain; Flyway migrations with a single query per file.

---

### Reference Sources (2026 best practices)

- [Restaurant Trends Report U.S. 2026 — SevenRooms](https://sevenrooms.com/research/restaurant-trends/)
- [2026 Restaurant Technology Trends — Incentivio](https://incentivio.com/2026-restaurant-technology-trends-what-forward-thinking-operators-need-to-know/)
- [Structured data for restaurants: the complete 2026 guide — Malou](https://www.malou.io/en-us/blog/structured-data-for-restaurants)
- [Restaurant — Schema.org Type](https://schema.org/Restaurant)
- [Menu — Schema.org Type](https://schema.org/Menu)
- [7 Knowledge Graph Examples of 2026 — PuppyGraph](https://www.puppygraph.com/blog/knowledge-graph-examples)
