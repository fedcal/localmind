export const content = `# Events & Shows

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This document describes the **"Events & Shows"** extension scope (group: consumer) within the vision of LocalMind as a *universal knowledge graph engine*. The goal is to transform the disorganized flow of information about local events, concerts, festivals and fairs into a **weighted graph that is navigable by the AI**, where every event is a node connected to venues, dates, artists, genres and communities, and where discovery happens through relationships and not only through text search. The scope is conceived as an **installable domain module** (PF4J plugin) that reuses the existing infrastructure — MySQL for the graph structure, Qdrant for semantics, Ollama as the default local AI — without introducing new infrastructural dependencies and in full compliance with the local-first, open source and bilingual IT/EN constraints.

---

## 1. What we solve (problem & value)

### 1.1 The concrete problem

Information about local events is today **fragmented, ephemeral and devoid of relationships**. Anyone who wants to discover "what to do tonight near me" must traverse a multitude of silos that do not communicate with each other:

- **Source fragmentation**: venues and theaters publish on their own websites, municipalities on institutional portals, organizers on ticketing platforms (Ticketmaster, Eventbrite, DICE), artists on social media, the local tourist boards (pro-loco) on PDF flyers, local newspapers in news articles. None of these sources has an overall view.
- **Ephemerality and decay**: an event has a very short life cycle. Once the date has passed, the information loses its immediate value but retains *relational* value (the same artist, the same series, the same venue will return). Current platforms throw away this historical memory.
- **Duplication and inconsistency**: the same concert appears on 5 platforms with slightly different titles, times and descriptions. The user does not know whether they are the same event or distinct events.
- **Lack of relational context**: knowing that "X plays tonight" does not tell whether X matches the user's tastes, whether they have already played in the city, whether they are part of a festival, whether the venue is reachable, whether there are similar events nearby or on alternative dates.
- **Poor discovery**: keyword search or category filtering do not surface non-obvious connections ("jazz festival within 30 km this weekend, with artists similar to those I have enjoyed, in outdoor venues").
- **Privacy and dependence on third parties**: commercial platforms profile the user and monetize their data. There is no local-first alternative that allows personalized discovery **without sending the user's tastes to the cloud**.

### 1.2 The LocalMind solution

LocalMind tackles the problem by modeling the events ecosystem as a **weighted knowledge graph** in which:

- every **event** is a first-class node, connected to **venue**, **date/period**, **artist/performer**, **organizer**, **genre/theme**, **series/festival** and **community** nodes;
- **relationships are weighted** (a "headliner" artist weighs more than a "support" act; a venue that recurrently hosts a genre builds affinity; a user who repeatedly attends a genre strengthens the interest edge);
- the **AI navigates the graph (GraphRAG)** combining structural relationships and semantic similarity to answer complex questions and generate explainable recommendations and itineraries;
- everything runs **locally**: ingestion, embedding, ranking and recommendation do not require external services; cloud providers remain optional.

### 1.3 Value generated per type of stakeholder

| Stakeholder | Value generated |
|---|---|
| **Citizen / discoverer** | Personalized "what to do" discovery by date, place, taste; evening itineraries; no external profiling |
| **Tourist** | Local events integrated with POIs, restaurants and itineraries (synergy with the tourism scope) |
| **Niche enthusiast** | Follows artists, genres, series; alerts on new dates; discovers non-obvious related events |
| **Organizer / venue** | Publishes and maintains its own events; sees audience affinity; archives its own programming |
| **Municipality / tourist board** | Aggregates the territory's cultural offering into a single navigable, self-hosted graph |
| **Community** | Contributes, corrects, votes and curates content; the best ranking emerges from the bottom up |

### 1.4 Why a graph (and not yet another calendar)

A calendar answers the question *"what happens on day X"*. A weighted graph answers **multi-relational** questions that no calendar can handle: *"festivals with artists similar to those I have enjoyed, reachable in a day, in an outdoor venue, on a free weekend, with food-and-wine side events"*. The distinctive value is the ability to **surface non-obvious connections** — the heart of LocalMind's proposition — and to **reuse historical knowledge** (artists, venues, recurring series) that ephemeral platforms dissipate. The graph's memory transforms every edition of a festival, every passage of an artist, every series at a venue into a persistent signal for future discovery and recommendation.

### 1.5 Differentiators compared to the alternatives

- **Local-first and privacy-by-design**: the user's tastes and attendance history remain on the self-hosted node; no mandatory sending to the cloud.
- **A single engine, many domains**: events share the graph engine with tourism, enterprise documents, etc. — maximum reuse, no isolated vertical.
- **Explainability**: every recommendation cites the nodes and paths used ("I suggest this to you because you follow artist A, who is similar to headliner B, in a venue you have already visited").
- **Extensibility via plugins**: new connectors (a new municipal portal, a new ticketing platform) are added as PF4J plugins without touching the core.
- **Open source and bilingual**: no paywall; UI, enums and documentation in IT/EN.

---

## 2. Personas & target users

| Persona | Description | Primary needs | Prevailing mode of use |
|---|---|---|---|
| **Giulia, the urban discoverer** | 29 years old, lives in the city, goes out often, decides at the last minute | "What do I do tonight near me?"; quick suggestions matching her tastes | AI chat + "tonight" feed, date/place filters |
| **Marco, the niche enthusiast** | 41 years old, follows jazz and electronic music | Alerts on new dates of followed artists/genres; non-obvious related discovery | Following artists/genres, notifications, graph exploration |
| **The Rossi family, weekend planners** | Couple with children, plans the weekend in advance | Family-friendly, outdoor, reachable, free or inexpensive events | Filters (target, price, distance), weekend itineraries |
| **Sofia, the tourist** | Visiting for a few days | What is unmissable right now, integrated with POIs and restaurants | AI territory+events itinerary |
| **Luca, organizer / venue manager** | Manages a club's programming | Publish and maintain events; understand audience affinity | Event editor, affinity dashboard, archiving |
| **Municipal culture office** | Local authority, tourist board | Aggregate the territory's cultural offering, self-hosted, privacy | Institutional connectors, curation, aggregated view |
| **Anna, the community curator** | Volunteer, active contributor | Moderation tools, duplicate merging, data correction | Moderation queue, assisted entity resolution tools |
| **Developer / contributor** | Technical self-hoster | Add connectors and node types, extend the module | PF4J plugins, graph API, documentation |

**Usage segmentation**: Giulia and Marco represent **reactive and personalized discovery** (the consumer core); the Rossi family and Sofia represent **contextual planning** (synergy with tourism); Luca and the culture office represent **production and curation of the offering** (the contributor side); Anna and the developer represent **graph sustainability** (data quality and extensibility).

---

## 3. Input requirements

This section defines in detail **what must enter the system** so that the events graph is rich, reliable and navigable. The requirements are organized into: event data, related entities, behavioral data, quality constraints, non-functional requirements.

### 3.1 Event master data (Event node)

Aligned with the **schema.org/Event** standard to ensure interoperability and direct mapping from connectors. Mandatory fields (\`O\`), recommended (\`R\`), optional (\`F\`):

| Field | Mand. | Description | Validation notes |
|---|---|---|---|
| \`titolo\` | O | Event name | Trim, casing normalization, min 3 char |
| \`descrizione\` | R | Descriptive text | Used for semantic embedding; language detected |
| \`dataInizio\` (start) | O | Start date/time with timezone | ISO-8601, timezone mandatory for de-dup |
| \`dataFine\` (end) | R | End date/time | If absent, estimated from typical duration per type |
| \`tipoEvento\` | O | Concert, festival, fair, show, exhibition, food festival, conference, sport… | Enum translated IT/EN |
| \`stato\` | O | Scheduled, rescheduled, cancelled, postponed, sold-out, ended | \`EventStatusType\` schema.org; enum IT/EN |
| \`modalita\` | O | In person, online, hybrid | \`EventAttendanceMode\`; enum IT/EN |
| \`luogoRef\` | O (if in person) | Reference to the Venue node | FK or entity resolution |
| \`organizzatoreRef\` | R | Reference to the Organizer node | Entity resolution |
| \`performerRefs\` | R | List of Artist/Performer nodes | Headliner/support distinction |
| \`generi\` / \`temi\` | R | Thematic tags (jazz, rock, contemporary art, gastronomy…) | Controlled taxonomy + free tags |
| \`prezzo\` / \`offers\` | R | Price range, free, range, currency | schema.org/Offer; currency normalization |
| \`ticketUrl\` | F | Ticket purchase link | URL validation |
| \`targetPubblico\` | F | Family, 18+, professional, accessible… | Enum IT/EN |
| \`capienza\` / \`disponibilita\` | F | Total / remaining seats | Integer ≥ 0 |
| \`immagini\` / \`media\` | F | Poster, photos, video | URL or local asset; alt-text for accessibility |
| \`lingua\` | F | Event language | ISO 639 |
| \`accessibilita\` | F | Accessible to disabilities, subtitles, sign language… | Multiple enum IT/EN |
| \`sorgente\` | O | Connector/source of provenance | For provenance and trust |
| \`idEsterno\` | R | Native ID on the origin platform | Key for sync/idempotency |
| \`licenzaContenuto\` | R | License/rights of the imported content | Open source compliance |

### 3.2 Related entity data

- **Venue**: name, address, geo coordinates (lat/long), type (theater, club, square, fairground, arena, outdoor/indoor), capacity, accessibility, opening hours, contacts. Coordinates are **mandatory** for proximity discovery and itineraries.
- **Artist / Performer**: canonical name, aliases, role (musician, band, DJ, company, speaker), genres, external links (\`sameAs\` → Wikidata/MusicBrainz for disambiguation), short biography (for embedding).
- **Organizer**: name, type (venue, municipality, association, promoter, tourist board), contacts, historical reliability.
- **Series / Festival**: name, edition/year, period, child events, theme, recurrence.
- **Genre / Theme**: controlled hierarchical taxonomy (e.g. Music → Jazz → Free jazz) with IT/EN synonyms.

### 3.3 Behavioral and community data (input for the weights)

These inputs feed the **dynamic edge weight** and personalization, and remain **strictly local**:

- **User interactions**: view, save/favorite, "I'm interested", confirmed attendance, ticket click.
- **Explicit follows**: followed artist, genre, venue, organizer, series.
- **Ratings and reviews**: numeric vote, textual post-event review (with embedding for semantic affinity).
- **Community contributions**: creation/correction of nodes, duplicate reporting, incorrect-data reporting, voting on others' contributions.
- **Recommendation feedback**: accepted/ignored/hidden, to refine the ranking.

### 3.4 Data quality requirements

Since the graph aggregates heterogeneous sources, **input quality is a first-class requirement**:

- **Normalization**: dates in UTC with explicit timezone; normalized currencies; geocoded addresses; cleaned-up titles.
- **Entity resolution / deduplication**: the same event from multiple sources must collapse into **a single node**, with a strategy of *fuzzy matching* on title (similarity ~80–85%), time window (±90 minutes), geographic proximity (~500–800 m) and performer match. Pattern aligned with 2026 event-aggregation best practices.
- **Provenance**: every node and attribute preserves its source (\`sorgente\`, \`idEsterno\`, ingestion timestamp) for traceability and conflict resolution.
- **Conflict resolution**: in case of discordant values across sources, a configurable policy (most reliable source wins, or most recent, or manual curation).
- **Boundary validation**: schema-based validation of every incoming record (project constraint); invalid records go to a *dead-letter queue* with detailed logging, never silently discarded.
- **Temporal decay**: past events transition to the \`terminato\` (ended) state but remain in the graph as historical memory; "freshness" weights decay over time.

### 3.5 Non-functional requirements

| Requirement | Specification |
|---|---|
| **Local-first** | All ingestion, embedding and recommendation must work offline with Ollama; no mandatory cloud dependency |
| **Privacy** | Behavioral data and user tastes never sent to third parties without explicit consent |
| **i18n** | All enums (event type, status, mode, target, accessibility) translated IT/EN and routed to the frontend according to the language switch |
| **Idempotency** | Re-ingestion of the same source does not create duplicates (key \`sorgente\` + \`idEsterno\`) |
| **Extensibility** | New sources as PF4J plugins without modifying the core |
| **Persistence** | Flyway migrations with a single query per file; UUID \`@JdbcTypeCode(SqlTypes.CHAR)\` |
| **Discovery performance** | "Nearby events in the next N days" query below the interactive threshold thanks to geo+temporal indexes |
| **Data license** | Compliance with the terms of use and licenses of the imported sources |

---

## 4. Activity flow (step-by-step)

The flow describes the complete life cycle: from **ingestion** to **graph construction**, from **discovery** to **recommendation**, up to **community contribution** and **historical maintenance**. It is divided into distinct but interconnected pipelines.

### 4.1 Pipeline A — Ingestion and normalization

1. **Ingestion trigger**: scheduled (Spring Batch / existing scheduler, analogous to the folder watcher) or on-demand from the UI. Each connector is a PF4J plugin that exposes an *extension point* of type "EventSource".
2. **Raw data fetch**: the connector retrieves events from the source (ticketing REST APIs, ICS/iCal feeds, RSS, municipal portal scraping, flyer PDFs via Tika/Tesseract already present, newsletter emails via the \`email\` domain).
3. **Parsing and mapping to schema.org/Event**: the raw data is mapped to a canonical DTO \`EventoImport\`.
4. **Boundary validation**: schema-based validation; invalid records → dead-letter queue with logging; valid records proceed.
5. **Normalization**: dates in UTC+timezone, address geocoding (venue → coordinates), currency/price normalization, text cleanup, language detection.
6. **Semantic embedding**: title + description + genres are transformed into a vector via the **Ollama EmbeddingModel** (\`@Primary\`) and stored in **Qdrant** (reuse of the documents pipeline).
7. **Entity resolution**:
   - for each related entity (venue, artist, organizer) an existing node is searched (exact match on \`sameAs\`/external ID, then fuzzy match on name+geo);
   - for the event, de-dup is applied (title ~82% + window ±90 min + geo ~600 m + performer): if a match → **merge** into the existing node enriching the attributes and recording the new provenance; otherwise → **new node**.
8. **Graph persistence**: Event node + edges toward Venue, Date/Period, Performer, Organizer, Genre, Series saved in MySQL (graph structure); vectors in Qdrant (semantics).
9. **Initial weight calculation**: edge weights are initialized from static factors (performer role, source reliability, data completeness). See section 5.
10. **Domain event**: publication of \`EventoIngeritoEvent\` via \`DomainEventPublisherPort\` to trigger indexing, follower notifications and affinity recalculation (existing events pattern).

### 4.2 Pipeline B — Discovery

1. **User entry**: the user opens the "discover" feed or asks a question in chat ("what do I do tonight within 20 km, something musical and outdoor").
2. **Context resolution**: the system gathers explicit constraints (date/period, geographic radius, type, price, target) and implicit context (location, tastes from the local profile, active follows).
3. **Hybrid query on the graph**:
   - **structural filter** on MySQL: future events in a valid state, within geo radius and time window, consistent with the filters;
   - **semantic expansion** on Qdrant: events semantically similar to the user's interests or to the natural-language query;
   - **graph traversal**: from the followed nodes (artists, genres, venues) it expands toward events connected at 1–2 hops, weighting by edge strength.
4. **Ranking**: combination of proximity (geo+temporal), affinity (semantic + follow), edge weight, freshness/novelty and community signals (rating, popularity). Configurable ranking function.
5. **Presentation**: ordered feed/list with explanatory badges ("why I suggest it to you"), quick filters and map view; possibility of switching to the **graph visualization** to explore by relationships.
6. **Interaction**: save, follow, "I'm interested", open ticket → each interaction generates a behavioral signal (Pipeline D).

### 4.3 Pipeline C — Recommendation and itineraries (GraphRAG)

1. **Complex question in chat** (e.g. "organize a Saturday evening for me: aperitif, jazz concert and something afterwards, all on foot").
2. **GraphRAG retrieval**: the AI queries the graph retrieving the relevant subgraph (Saturday's events, nearby venues, genres matching tastes, side events) combining Qdrant semantic search and weighted MySQL traversal.
3. **Reasoning and composition**: the LLM (Ollama by default) composes an itinerary that is coherent in time and space, respecting constraints (times, distances, budget, accessibility).
4. **Explainability**: the response **cites the nodes and paths** used (followed artist → event → venue → side event), so the user understands the "why".
5. **Conversational refinement**: the user refines ("I prefer outdoors", "lower budget") and the AI recalculates on the graph.
6. **Actions**: save itinerary, add to calendar (\`calendar\` domain), share, activate alerts.

### 4.4 Pipeline D — Community contribution and curation

1. **Contribution**: a user or organizer creates/corrects an event, adds a missing venue, votes, reviews.
2. **Validation**: same quality checks as automatic ingestion.
3. **Assisted entity resolution**: the system proposes possible duplicates; the contributor or curator confirms the merge.
4. **Moderation queue**: sensitive contributions (new nodes, merges, reports) enter a queue; curators approve/reject.
5. **Emergent ranking**: edge weights and node reputation update based on votes and validated contributions — the best content emerges from the bottom up.
6. **Feedback loop**: corrections strengthen trust in the sources and improve future entity resolution.

### 4.5 Pipeline E — Weight update and historical maintenance

1. **Incremental weight update**: at each interaction/contribution, recalculation (event-driven or batch) of the affected edge weights.
2. **Temporal decay**: events move to \`terminato\` (ended); "freshness" weights decay with a configurable curve; the history remains as a signal for affinity and recurrences.
3. **Recurrence recognition**: recurring events/festivals are linked to previous editions, building time series (e.g. "Festival X — 2026 edition" → 2025 → 2024).
4. **Sync and idempotency**: periodic re-ingestions update states (cancelled, sold-out, rescheduled) without duplicating, thanks to \`sorgente\`+\`idEsterno\`.
5. **Cleanup and audit**: data quality reports, orphan nodes, residual duplicates, no-longer-reliable sources.

### 4.6 Textual diagram of the end-to-end flow

\`\`\`text
[Sources: ticketing API, ICS, RSS, portals, PDF/flyers, email]
        │  (connectors = PF4J plugins "EventSource")
        ▼
[Pipeline A: fetch → map schema.org → validate → normalize → embedding(Ollama)
             → entity resolution/de-dup → graph persistence(MySQL+Qdrant) → initial weights]
        │  EventoIngeritoEvent
        ▼
[WEIGHTED EVENTS GRAPH]  ←── [Pipeline D: community contributions/curation]
        │                          ▲
        │                          │ votes, reviews, merge
        ├──► [Pipeline B: discovery — hybrid query + ranking]
        ├──► [Pipeline C: GraphRAG — explainable recommendations & itineraries]
        └──► [Pipeline E: dynamic weights, decay, recurrences, sync]
\`\`\`

---

## 5. Graph model (node types, relationship types, weighting criteria)

The model reuses the **core graph engine** (typed nodes + weighted edges on MySQL, semantics on Qdrant) specializing it with domain types installed by the "Events & Shows" module.

### 5.1 Node types

| Node type | Description | Key attributes | Embedding source (Qdrant) |
|---|---|---|---|
| \`Evento\` | The single event | title, dates, type, status, price, mode | title + description + genres |
| \`Luogo\` (Venue) | Physical/online location | name, geo, type, capacity, accessibility | name + description |
| \`Artista\`/\`Performer\` | Who performs | name, alias, role, genres, sameAs | bio + genres |
| \`Organizzatore\` | Who organizes | name, type, reliability | description |
| \`Rassegna\`/\`Festival\` | Container of events | name, edition, period, theme | description |
| \`Genere\`/\`Tema\` | Thematic category | name, hierarchy, IT/EN synonyms | name + synonyms |
| \`Data\`/\`Periodo\` | Temporal node | day, week, season | — (structural) |
| \`Utente\` | Local profile | preferences, follows, history | — (private/local) |
| \`Recensione\` | Post-event feedback | vote, text, author | review text |
| \`Itinerario\` | Generated sequence of events/POIs | stops, times, constraints | description |

The \`Luogo\`, \`Itinerario\`, \`Genere\` and \`Utente\` nodes are **shared** with the tourism/territory scope: the reuse is explicit and intentional (a single engine, many domains).

### 5.2 Relationship types (edges)

| Relationship (edge) | From → To | Meaning | Weighted by |
|---|---|---|---|
| \`SI_TIENE_IN\` | Event → Venue | The event takes place at the venue | historical venue-genre affinity |
| \`SI_TIENE_IL\` | Event → Date/Period | Temporal placement | freshness/proximity |
| \`ESEGUITO_DA\` | Event → Artist | Performer of the event | role (headliner > support) |
| \`ORGANIZZATO_DA\` | Event → Organizer | Who organizes | organizer reliability |
| \`APPARTIENE_A\` | Event → Series/Festival | Child event of a series | centrality in the program |
| \`HA_GENERE\` | Event/Artist → Genre | Thematic classification | classification confidence |
| \`EDIZIONE_DI\` | Series(year) → Series(year-1) | Historical continuity | recurrence/seniority |
| \`SIMILE_A\` | Event↔Event, Artist↔Artist | Semantic/behavioral similarity | cosine similarity + co-attendance |
| \`SEGUE\` | User → Artist/Genre/Venue/Organizer | Explicit interest | recency + frequency |
| \`INTERESSATO_A\` / \`PARTECIPA_A\` | User → Event | Intent/attendance | intent level (click<save<confirm) |
| \`HA_RECENSITO\` | User → Event (via Review) | Feedback | vote + community usefulness |
| \`VICINO_A\` | Venue ↔ Venue | Geographic proximity | inverse distance |
| \`COLLATERALE_A\` | Event ↔ Event | Complementary events (same evening/area) | space-time overlap |
| \`INCLUDE_TAPPA\` | Itinerary → Event/Venue | Itinerary composition | order + coherence |

### 5.3 Criteria for edge weight

The weight is a normalized value (e.g. 0–1) computed as a **configurable combination** of factors, in line with the core principle "weight derived from configurable factors (frequency, relevance, dependencies, feedback)":

- **Intrinsic relevance**: role (headliner vs support), centrality in a series program, data completeness.
- **Frequency / recurrence**: how many times a venue hosts a genre, how many editions a series has, how often a user interacts with a genre.
- **Recency and freshness**: imminent events weigh more than distant ones; interest edges decay over time if not renewed.
- **Proximity**: geographic distance (inversely proportional weight) and temporal distance.
- **Semantic affinity**: cosine similarity of the Qdrant vectors (event↔event, event↔user tastes, artist↔artist).
- **Community signals**: votes, average ratings, number of confirmed attendances, contributor reputation.
- **Source reliability**: edges derived from more reliable sources or from manual curation weigh more; provenance tracked.
- **Intent level**: for user→event edges, an attendance confirmation weighs more than a simple click.

Conceptual formula (configurable weights \`α…θ\` per domain):

\`\`\`
weight(edge) = α·relevance + β·frequency + γ·recency
             + δ·proximity + ε·semantic_affinity
             + ζ·community_signals + η·source_reliability + θ·intent
\`\`\`

The weights are **recalculated incrementally** (event-driven) and periodically consolidated (batch), avoiding costly global recalculations.

### 5.4 Representation on MySQL + Qdrant (no Neo4j)

- **MySQL**: tables \`nodes\` (id UUID \`CHAR(36)\`, type, JSON attributes, domain) and \`edges\` (id, from_node, to_node, type, weight, JSON attributes, provenance), with indexes on type, geo (lat/long) and time window. Few-hop traversals are expressed with joins/recursive CTEs (mind the escaping of \`recursive\`/\`timestamp\`, MySQL reserved words).
- **Qdrant**: collection for the embeddings of textual nodes (event, artist, venue, review, genre) for the semantic component and the \`SIMILE_A\` edge.
- **Consistency**: the node id is the key that links the MySQL record and the Qdrant point; the pipeline keeps the two stores aligned.

---

## 6. Data sources & connectors (ingestion)

Each source is encapsulated in a **PF4J plugin connector** that implements a dedicated extension point (\`EventSourceExtension\`), so as to add new sources without touching the core. The connectors reuse the existing infrastructure (Spring Batch/scheduler, Tika/Tesseract, \`email\` domain, \`DomainEventPublisherPort\`).

| Source | Connector type | Infrastructure reuse | Compliance notes |
|---|---|---|---|
| **ICS/iCal feed** | Scheduled pull | Dedicated ICS parser | Open standard; ideal for municipalities/theaters |
| **RSS / Atom** | Scheduled pull | Feed parser | For local outlets and cultural blogs |
| **Ticketing platforms** (Eventbrite, Ticketmaster, DICE…) | Pull via REST API | Existing WebClient | Respect ToS, rate limits and licenses; keys via settings/DB |
| **Municipal / institutional portals** | Respectful scraping or API | Tika for HTML | Public data only; robots/ToS |
| **PDF flyers / brochures** | Upload or folder watcher | Tika + Tesseract OCR | Text extraction + LLM for structuring |
| **Newsletters / organizer emails** | Email ingestion | \`email\` domain (IMAP) | User consent; parsing events from mail |
| **User calendar** | Bidirectional sync | \`calendar\` domain | Privacy: local data |
| **Manual / community contributions** | UI editor + API | Graph API | Validation + moderation |
| **schema.org JSON-LD from web pages** | Structured extraction | Fetch + JSON-LD parser | Leverages already-published \`Event\` markup |

**Cross-cutting ingestion strategies**:
- **Idempotency** via \`sorgente\`+\`idEsterno\`; sync that updates instead of duplicating.
- **Entity resolution** centralized and reusable across all connectors.
- **Provenance** for every attribute, for audit and conflict resolution.
- **License and ToS compliance**: every connector declares the license of the data and the usage limits; compliance with the project's open source soul.
- **Local-first**: all connectors work on-premise; no mandatory cloud dependency.

---

## 7. Features to create, develop and maintain (MVP → evolution)

A concrete map of the features, distinguishing **MVP** (first useful end-to-end release), **Evolution** (subsequent increments) and **Maintenance** (recurring activities).

### 7.1 MVP — Minimal but complete vertical slice

Goal: demonstrate the "from source to explainable discovery" value on at least one real source.

| # | Feature | Layer | Reuse/Notes |
|---|---|---|---|
| M1 | Event node/edge types in the graph engine (modular schema) | domain + Flyway | Extends the \`knowledge\` domain |
| M2 | Flyway migrations for event nodes/edges (one query per file) | app | One-query constraint |
| M3 | ICS/iCal connector (1 real source) as a PF4J plugin | infra/plugin | Open standard, low friction |
| M4 | Ingestion pipeline: map schema.org → validate → normalize → Ollama embedding → Qdrant | domain + infra | Reuses documents pipeline |
| M5 | Basic entity resolution / de-dup (title+geo+time) | domain | Configurable fuzzy match |
| M6 | CRUD API for event nodes/edges + "nearby events in the next N days" query | api | \`/api/v1/events\` |
| M7 | Basic discovery: list/feed with date, place (radius), type, price filters | frontend | Standalone Angular feature |
| M8 | Events map view | frontend | Venue geo |
| M9 | Basic GraphRAG chat: "what do I do tonight near me" with node citation | domain + frontend | Reuses ChatUseCase/LLM gateway |
| M10 | Enums translated IT/EN (type, status, mode, target) routed to the frontend | domain + frontend | i18n constraint |
| M11 | Basic follow (artist/genre/venue) + personalized feed | domain + frontend | Signals for weights |
| M12 | IT/EN documentation of the module and connectors | documentation/documentazione | Bilingual constraint |

### 7.2 Evolution — Subsequent increments

| # | Feature | Value |
|---|---|---|
| E1 | Additional connectors (RSS, ticketing API, portals, PDF/OCR, email) | Source coverage |
| E2 | Interactive visualization of the events graph (nodes/edges/weight, progressive expansion) | Discovery by relationships |
| E3 | Evening/weekend itineraries generated by AI (advanced GraphRAG, space-time-budget constraints) | Planning |
| E4 | Full dynamic weights + temporal decay + recurrences/editions | Emergent ranking |
| E5 | Advanced personalized recommendations + rich "why I suggest it to you" | Personalization |
| E6 | Alerts/notifications on new dates of followed artists/genres/venues | Retention |
| E7 | Reviews, ratings and emergent ranking from the community | Quality from the bottom up |
| E8 | Curation/moderation tools + assisted entity resolution | Graph sustainability |
| E9 | Calendar integration (add event/itinerary) and messaging (sharing) | Actionability |
| E10 | Synergy with tourism: events within territory itineraries (POIs+events) | Cross-domain |
| E11 | Recurrence recognition and historical festival series | Historical memory |
| E12 | Advanced accessibility (accessibility filters, inclusive content) | Inclusivity |

### 7.3 Maintenance — Recurring activities

- **Connector updates** as the sources' APIs/ToS change; plugin version management.
- **Data quality monitoring**: reports of residual duplicates, orphan nodes, degraded sources; tuning of entity resolution parameters.
- **Tuning of weights and ranking** based on feedback and metrics.
- **Historical cleanup**: decay management, archiving of ended events, integrity of recurring series.
- **Taxonomy updates** for genres/themes and IT/EN synonyms.
- **Embedding maintenance**: re-embedding on Ollama model change; MySQL↔Qdrant consistency.
- **Documentation and i18n** constantly kept up to date (project constraint).

---

## 8. AI / GraphRAG use cases

The AI navigates the weighted graph combining **structural traversal** (MySQL) and **semantic similarity** (Qdrant), with Ollama as the default local engine. Concrete examples:

1. **Conversational discovery**: *"What do I do tonight within 15 km, something musical and not too expensive?"* → geo-temporal filter + semantic affinity + weights; response with node citation.
2. **Explainable evening itinerary**: *"Organize Saturday for me: aperitif, jazz concert, after-party, all on foot"* → subgraph of nearby events+venues, space-temporal composition, path citation.
3. **Related recommendation**: *"Events similar to the one I went to last month"* → \`SIMILE_A\` edge (semantic + co-attendance).
4. **Non-obvious connections**: *"Why should I go to this festival?"* → the AI shows the path (follow artist A → A similar to headliner B → B at festival X → venue already visited).
5. **Multi-hop question on the territory**: *"Outdoor festivals within 30 km, on July weekends, with food side events"* → traversal \`APPARTIENE_A\` + \`COLLATERALE_A\` + filters.
6. **Smart alerts**: the AI summarizes "news for you" on active follows (new dates, reschedules).
7. **Historical Q&A**: *"How many editions has Festival X had and who played?"* → traversal \`EDIZIONE_DI\` + \`ESEGUITO_DA\`.
8. **Structuring from unstructured sources**: the LLM extracts events from PDF/OCR flyers and emails, populating the nodes (with validation).
9. **Curation assistance**: the AI proposes duplicate merges and corrections, explaining the confidence.
10. **Systematic explainability**: every response cites the nodes and paths used — a core requirement of the graph engine.

---

## 9. KPIs & success metrics

| Category | KPI | Goal / direction |
|---|---|---|
| **Graph coverage** | No. of active events, no. of connected sources, attribute completeness | Increasing |
| **Data quality** | Residual duplicate rate, % of geocoded events, % with performer/genre | Duplicates ↓, completeness ↑ |
| **Entity resolution** | Merge precision/recall, false merges | Precision ↑, false merges ↓ |
| **Discovery** | Events viewed/saved per session, CTR on recommendations | Increasing |
| **Personalization** | % of recommendations accepted vs ignored/hidden | Acceptance ↑ |
| **GraphRAG** | Rate of responses with correct node citation, user satisfaction | ↑ |
| **Community engagement** | No. of contributions/corrections/votes, active contributors | Increasing |
| **Itineraries** | No. of itineraries generated and saved, completion rate | Increasing |
| **Performance** | "Nearby next N days" query latency, GraphRAG response latency | Below the interactive threshold |
| **Privacy/local-first** | % of functions operational offline, zero non-consented sending | 100% offline core |
| **i18n** | IT/EN translation coverage of enums and UI | 100% |

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| **Quality/duplicates from heterogeneous sources** | "Dirty" graph, degraded discovery | Robust entity resolution + provenance + curation + quality reports |
| **Terms of use / source licenses** | Legal risk, source blocking | Connectors that declare license/ToS; preference for open feeds (ICS/RSS/JSON-LD) |
| **Ephemerality of events** | Obsolete data shown | Life-cycle states + periodic sync + freshness decay |
| **Limits of graph queries on MySQL (no Neo4j)** | Slow deep traversals | Geo/temporal indexes, CTEs limited to few hops, targeted denormalization; future reassessment |
| **MySQL↔Qdrant consistency** | Inconsistent results | Transactional/compensating pipeline, controlled re-embedding, consistency audit |
| **Personalization cold start** | Poor recommendations at the start | Fallback on popularity/proximity + interest onboarding |
| **Privacy of user tastes** | Loss of trust | Behavioral data local only; no cloud sending without consent |
| **Offline geocoding** | Missing coordinates | Local/self-hosted geocoder; manual fallback in curation |
| **Community moderation overload** | Backlog, incorrect data | AI-assisted entity resolution + reputation + automations |
| **Type/taxonomy explosion** | Unmanageable schema | Controlled taxonomy + governance + IT/EN synonyms |

---

## 11. Maintenance & evolution

- **Domain schema governance**: event node/edge types evolve additively; changes via Flyway migrations (one query per file) and versioning of the plugin module.
- **Connector life cycle**: each PF4J connector has a declared version and compatibility; updates as the APIs/ToS change; the LocalMind marketplace distributes the connectors.
- **Continuous tuning of weights and ranking**: the coefficients of the weight function are configurable and calibrated on real metrics; internal A/B via feedback.
- **Semantic maintenance**: planned re-embedding on Ollama model change; consistency verification between MySQL and Qdrant.
- **Curation and data quality**: quality dashboards, moderation queues, merge tools; contributor reputation.
- **Historical memory**: archiving of ended events while keeping the useful edges (recurrences, historical affinities).
- **Living documentation**: constant updating of the IT/EN documentation and the logs in \`Sviluppi/\` (project constraint), with developments conducted in plan mode.
- **Evolutionary roadmap**: from consumer discovery toward cross-domain integrations (tourism), advanced accessibility, and — only if queries require it — reassessment of a dedicated graph datastore (today explicitly out of scope).

---

## 12. Integration with the existing LocalMind modules

| Existing module | Role in the Events & Shows scope |
|---|---|
| **\`knowledge\`** | Base of the graph engine; hosts the event node/edge types (modular schema) |
| **\`llm\`** | Chat and GraphRAG; \`LlmGatewayService\` with Ollama default and optional cloud fallback; \`@Primary\` Ollama embedding |
| **\`document\`** | Reuse of Tika/Tesseract to ingest PDF flyers and unstructured content; chunking/embedding pipeline |
| **vector store / Qdrant** | \`QdrantVectorStoreAdapter\` for node semantics and the \`SIMILE_A\` edge |
| **persistence / MySQL + Flyway** | \`nodes\`/\`edges\` tables, UUID \`CHAR(36)\`, one-query migrations |
| **\`email\`** | Connector for ingesting events from newsletters/organizers (IMAP) |
| **\`calendar\`** | Adding events/itineraries to the calendar; user calendar sync |
| **\`messaging\` / \`channels\`** | Notifications and alerts on follows; sharing of events and itineraries |
| **\`automation\`** | Scheduled ingestion/sync pipelines and event-driven weight recalculation |
| **\`agent\`** | Agents for proactive discovery, assisted curation, structuring from unstructured sources |
| **\`mcp\`** | Exposure of MCP tools (e.g. event search) to external agents/tools |
| **\`plugin\` (PF4J)** | \`EventSourceExtension\` connectors and domain types installable without touching the core |
| **\`marketplace\`** | Distribution of the "Events & Shows" module and of the individual connectors |
| **\`auth\`** | Local user profiles, follows and history with a privacy local-first focus |
| **\`common\` (domain events)** | \`DomainEventPublisherPort\` for \`EventoIngeritoEvent\`, weight recalculation, notifications |
| **Angular 21 frontend** | New lazy-loaded standalone feature (feed, map, graph, chat), Signal store, \`TranslatePipe\` IT/EN |

**Guiding principle of the integration**: the Events & Shows scope **is not a separate app**, but a **vertical module that specializes the universal graph engine** reusing the existing infrastructure to the maximum and respecting the project constraints (local-first, default Ollama AI, MySQL+Qdrant without Neo4j, PF4J plugins, privacy, open source, bilingual IT/EN, Flyway migrations with a single query).

---

> **Note on developments**: every development related to this scope must be conducted in plan mode and tracked in the \`Sviluppi/\` folder with the naming convention \`YYYY-MM-DD_NN_FeatureName\`, keeping the bilingual IT/EN documentation up to date.
`;
