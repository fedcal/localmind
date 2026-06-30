export const content = `# Culture, Art & Museums

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This document describes the extension of LocalMind into the **Culture, Art & Museums** domain (consumer group), applying the universal Knowledge Graph engine to a **cultural graph** that connects museums, artworks, artists, movements, historical periods, materials, techniques, places and exhibitions, and that feeds **thematic itineraries** navigable by the AI (GraphRAG). The goal is to offer visitors, enthusiasts, students and cultural operators a new way of exploring heritage: not through flat catalogues but through weighted relationships, surfacing non-obvious connections (influences between artists, dialogues between artworks, thematic threads that cross centuries and different collections).

The domain fully reuses the LocalMind stack and constraints: **local-first / self-hostable**, **local Ollama AI by default** (optional cloud providers), **MySQL 8.0** for the graph structure and **Qdrant** for semantics (no Neo4j), extensibility via **PF4J plugins + marketplace**, **bilingual IT/EN** interface and documentation, **Flyway migrations with a single query per file**.

---

## 1. What we solve (problem & value)

### 1.1 The problem today

Cultural heritage is extremely rich but **fragmented and hard to navigate by relationships**. Anyone who wants to approach art runs into at least five concrete obstacles:

1. **Siloed catalogues.** Each museum publishes its own collections in different formats and record schemas; artworks live as isolated records, without a fabric of relationships connecting them across institutions, eras and movements. A visitor cannot easily ask "which other works from the same period, influenced by the same master, can I see within 30 km of here?".
2. **Keyword search, not meaning-based search.** The search engines on museum websites respond to exact strings (title, author, inventory number), not to thematic or exploratory questions such as "show me the transition from Gothic to Renaissance through the representation of light".
3. **Knowledge inaccessible to non-experts.** Art-historical records are often technical and self-referential; there is a lack of guided itineraries that adapt the level of depth to the visitor (child, casual tourist, student, scholar).
4. **Visit disconnected from context.** The in-gallery experience is separate from preparation before the visit and from deeper exploration afterwards; there is no personalized connecting thread to accompany the user through the rooms, nor a way to "save" an itinerary and resume it.
5. **Cultural data closed or scattered.** Linked Open Data initiatives (Europeana, Wikidata, the Getty AAT/ULAN/TGN vocabularies, the CIDOC-CRM standard) exist but are hard to consume for small institutions, private collections, local archives and community organizations, which remain outside the global graph.

### 1.2 What LocalMind solves

LocalMind turns heritage into a **weighted cultural graph, queryable in natural language and visually navigable**. The value plays out on multiple levels:

- **From record to network.** Every artwork, artist, museum, movement becomes a **typed node**; influences, memberships, commissions, citations, provenances become **weighted edges**. The value is not the single record but the web of relationships that lets you "travel" through heritage.
- **Complex questions, reasoned answers.** Thanks to GraphRAG, the AI combines **semantics** (embeddings on descriptions, critical texts, captions in Qdrant) with the **structural relationships** of the graph (MySQL) to answer open-ended questions, citing the nodes and paths used ("this influence is attested by X, work Y belongs to movement Z").
- **Personalized thematic itineraries.** The AI generates tailored itineraries — by level, available time, interests, accessibility, geographic proximity — both within a single museum and across different museums and cities, modeled on the "thematic trails" already piloted by international institutions (Smithsonian American Art Museum, Rijksmuseum Art Explorer, Harvard Art Museums AI Explorer).
- **Openness to all custodians of culture.** A small civic museum, a parish archive, a local tourist board, a collector or a community organization can **contribute nodes and relationships** while remaining owners of their own data (local-first), and can import/align content from the large open graphs (Wikidata, Europeana) without having to manage complex RDF infrastructure.
- **Sovereignty and privacy.** The entire engine runs on-premise with local AI: no sensitive data (e.g. provenances, insurance valuations, locations of private works) leaves the institution without explicit consent.

### 1.3 Why it has value (and for whom)

| Beneficiary | Concrete value |
|--------------|-----------------|
| **Visitor / enthusiast** | Exploration by meaning, tailored itineraries, deeper dives adapted to one's own level, discovery of unexpected connections between works and places |
| **Student / teacher** | Educational tool that shows movements, influences and historical contexts as a navigable map; support for theses and research with source citation |
| **Scholar / curator** | Ability to surface non-obvious relationships between works and different collections; basis for attribution research, provenance studies and curating thematic exhibitions |
| **Small museum / archive** | Publication and enrichment of one's own collections with minimal effort, alignment to standard vocabularies, presence in a shared cultural graph without surrendering data sovereignty |
| **Tourism operator / territory** | Connection between cultural heritage and local offerings (events, experiences, itineraries), in synergy with the other LocalMind consumer domains |

### 1.4 Differentiating value versus alternatives

Compared to museum online catalogues, audio guides and Linked Open Data portals, LocalMind offers the — today rare — combination of: **weighted graph + local conversational AI + interactive visualization + community contributions + self-hosting**. It does not replace Europeana or Wikidata: it **consumes and enriches** them, acting as a local and federable "discovery engine" that anyone can install and populate with their own heritage.

---

## 2. Personas & target users

| Persona | Profile | Goals | Needs from the system |
|---------|---------|-------|-----------------------|
| **Giulia, cultural tourist** | 34 years old, traveling for a weekend, average knowledge of art | Visit the best places in the available time, understand what she is looking at | Short thematic itinerary, simple explanations, geographic proximity, language of choice |
| **Marco, art history student** | 22 years old, preparing an exam on a movement | Understand influences, historical context, relationships between works and artists | Graph navigation by movement/period, source citations, itinerary export |
| **Prof. Bianchi, teacher** | 48 years old, teaches in high school | Build lessons and thematic educational itineraries | Generation of itineraries adapted to a school level, bilingual content, sharing |
| **Dr. Ferri, museum curator** | 55 years old, curates collections and exhibitions | Surface relationships between works from different collections, prepare thematic exhibitions | Advanced graph queries, suggestion of missing connections, curated node editing |
| **Anna, head of small civic museum** | 40 years old, few IT resources | Publish and showcase the local collection | Simple ingestion (CSV/photos/records), alignment to Wikidata/Getty, contribution moderation |
| **Luca, enthusiast / community contributor** | 29 years old, knows local heritage well | Enrich the graph with works and stories of the territory | Guided node/relationship creation, recognition of the contribution, review |
| **Sofia, visitor with accessibility needs** | 60 years old, visually impaired | Experience the visit independently | Accessible itineraries, audio descriptions, adjustable level of detail |
| **Territory operator (cross-domain)** | — | Connect culture, events and experiences | Interoperability with the tourism/events domains of the graph |

People are distributed along two axes: **level of expertise** (casual → expert) and **role** (consumer → contributor → curator). The system must adapt the depth of content and the available tools along both axes.

---

## 3. Input requirements

This section defines in detail **everything the system receives as input** to build, feed and query the cultural graph. Inputs fall into: (A) domain data to be ingested, (B) user input during use, (C) configuration and schema, (D) contribution/curation input. For each input we indicate format, validation and destination in the graph.

### 3.1 Domain data to be ingested (graph population)

| Input | Accepted format | Key fields/signals | Validation | Destination |
|-------|-------------------|----------------------|-------------|--------------|
| **Artwork records** | CSV, JSON, XML, Excel spreadsheet, catalographic records (e.g. ICCD scheda OA), CIDOC-CRM/LIDO records | title, author, dating, technique, material, dimensions, subject, location, inventory number | Minimum mandatory fields (title or inventory number + museum); date normalization; deduplication by inventory number | \`Opera\` node + edges toward \`Artista\`, \`Museo\`, \`Tecnica\`, \`Materiale\`, \`Movimento\`, \`Periodo\` |
| **Artist records** | CSV/JSON, ULAN/Wikidata (QID) alignment, VIAF | name, name variants, birth/death dates and places, nationality, roles | Homonym disambiguation; reconciliation with ULAN/Wikidata | \`Artista\` node + edges \`nato_a\`, \`attivo_in\`, \`appartiene_a\` (movement) |
| **Museum/place records** | CSV/JSON, geocoordinates, TGN (Getty) | name, address, coordinates, opening hours, website, type | Mandatory geocoding; valid coordinates | \`Museo\`/\`Luogo\` node + edge \`situato_in\` |
| **Critical texts, descriptions, historical records** | PDF, DOCX, HTML, TXT, Markdown | free text, captions, bibliography | Text extraction (Tika), OCR (Tesseract) for scans | Embedding in Qdrant linked to the node; entity/relationship extraction via LLM |
| **Artwork images** | JPG, PNG, TIFF, IIIF manifest | image, EXIF/IPTC metadata, IIIF metadata | Format/size; link to artwork node | Attachment to the \`Opera\` node; (evolution) multimodal visual embedding |
| **Movements, periods, styles, iconographic subjects** | Controlled vocabularies: Getty AAT, Iconclass, Wikidata | term, definition, hierarchy, time interval | Mapping to internal taxonomy; IT/EN language | \`Movimento\`, \`Periodo\`, \`Tecnica\`, \`Soggetto\` nodes |
| **Exhibitions and exhibition events** | CSV/JSON, event feed | title, venue, dates, exhibited works, theme | Consistent dates; link to works/museums | \`Mostra\` node + edges \`espone\`, \`si_tiene_in\`, \`tratta_di\` |
| **External Linked Open Data datasets** | SPARQL endpoint, RDF/Turtle, JSON-LD, dump (Europeana, Wikidata, DBpedia, Getty) | subject-predicate-object triples | Ontological mapping (CIDOC-CRM → internal schema); filtering by domain | Typed nodes and edges with provenance |

### 3.2 User input during use (querying & navigation)

| Input | Example | Constraints/validation |
|-------|---------|---------------------|
| **Natural language question** | "Which works by Caravaggio show the dramatic use of light and where can I see them in Italy?" | Max length; sanitization; detected language (IT/EN) |
| **Exploration filters** | node type (artwork/artist/museum), movement, period (time slider), geographic area, technique | Values from controlled enums; valid time ranges |
| **Thematic itinerary parameters** | theme, available time, level (basic/intermediate/advanced), accessibility, radius in km, starting point | Consistency between time and number of stops; optional geolocation |
| **Starting node for navigation** | click on a graph node to expand its neighbors | Valid and existing node ID |
| **Profile preferences** | interests (favorite movements/artists), language, default level, saved items | Persisted per user; editable |
| **Feedback** | rating/like on an artwork or itinerary, "useful/not useful" on an AI answer, error report | Anti-abuse; one vote per user/object |

### 3.3 Configuration and schema (system/administrator input)

- **Definition of the domain schema** (node types, relationship types, attributes) as a module installable from the marketplace: a versioned configuration file that extends the base schema of the graph engine.
- **Ontological mappings**: correspondence tables CIDOC-CRM / LIDO / AAT / Iconclass → internal schema, for ingestion from standard sources.
- **Weights and formulas**: configuration of the factors and coefficients for computing the edge weight (see §5.3).
- **LLM and embedding providers**: choice of the local Ollama model (default) for chat and embeddings; optional cloud fallback, which can be disabled to guarantee privacy.
- **Moderation policy**: thresholds and rules for community contributions (auto-publishing vs review).
- **Active languages**: IT/EN mandatory; translated enums and labels; free texts indexed per language.

### 3.4 Contribution and curation input (community/curators)

- **Node creation/editing**: guided form with automatic suggestion of type, attributes and possible connections (AI-assisted).
- **Relationship proposal**: the user or the AI propose edges ("this work is influenced by…"), with the ability to indicate source/evidence.
- **Attachments**: images, documents, links to authoritative sources.
- **Revisions and versions**: every change is immutable and versioned (consistent with the project's immutability rule); browsable history.

### 3.5 Non-functional requirements on inputs

- **Validation at the boundaries** (project rule): every input — file, external API, user contribution — is validated before processing, with clear and bilingual error messages.
- **Tracked provenance**: every node/edge retains the source (imported file, LOD dataset, user, AI) and the timestamp.
- **Idempotency and deduplication**: re-importing the same source does not duplicate nodes; reconciliation uses external identifiers (Wikidata QID, ULAN, museum inventory number).
- **Local-first**: no input requires mandatory cloud services; external LOD sources are optional and cacheable locally.

---

## 4. Activity flow (step-by-step)

The domain involves three main flows, described in detail: **(A) Ingestion and graph construction**, **(B) User exploration and query (GraphRAG)**, **(C) Contribution and curation**. Each step indicates the actor, the LocalMind module involved and the outcome.

### 4.1 Flow A — Ingestion and construction of the cultural graph

1. **Source selection.** The administrator or curator chooses the source: file upload (CSV/JSON/PDF/images), monitored folder, LOD connector (Wikidata/Europeana/Getty), or exhibition feed. *(UI: \`documents\` feature / new \`culture\` feature; backend: \`document\` domain + new \`knowledge\`/graph domain.)*
2. **Extraction and parsing.** For unstructured documents, the existing pipeline extracts the text (Apache Tika) and applies OCR (Tesseract) to scans; for structured sources the dedicated parser/mapping is applied. *(Reuse of \`DocumentIngestionPipelineService\`, \`TikaTextExtractor\`, \`TesseractOcrExtractor\`.)*
3. **Normalization and validation.** Fields are normalized (dates, names, units of measure), validated at the boundaries and translated/labeled per language (IT/EN). Invalid inputs are rejected with a clear message.
4. **Entity reconciliation.** The system looks for matches with existing nodes and with external identifiers (Wikidata QID, ULAN for artists, TGN for places, inventory number for works) to avoid duplicates. Disambiguation doubts are flagged for review.
5. **Node and relationship extraction.** For free texts, a local LLM (Ollama) extracts triples (entity → relationship → entity) according to the domain schema, inspired by the CIDOC-CRM + LLM approach. For structured sources, the mapping generates the nodes/edges directly.
6. **Weight computation.** Each edge is assigned an initial weight according to the configured formulas (strength of evidence, number of sources, authoritativeness, frequency — see §5.3).
7. **Dual persistence.** The **structure** (nodes, edges, attributes, weights, provenance) is saved in **MySQL** via JPA; the **descriptions and texts** are embedded (Ollama embedding) and saved in **Qdrant**, with a cross-reference to the node ID. *(Reuse of \`QdrantVectorStoreAdapter\`, \`EmbeddingConfig\` Ollama @Primary.)*
8. **Indexing and linking.** Indexes are created for graph queries (neighbors, paths, subgraphs) and semantic links between similar nodes (embedding similarity above threshold → candidate edge \`simile_a\`).
9. **Suggestion of missing connections.** The AI proposes edges not yet present (e.g. probable influences, works from the same cycle) as **candidates** in a review queue, never published automatically without a policy.
10. **Development tracking.** Consistent with the project rules, the development activity is recorded in the \`Sviluppi/\` folder and the IT/EN documentation is updated.

### 4.2 Flow B — User exploration and query (GraphRAG)

1. **Entry point.** The user enters from the **Culture** feature or from the **Chat**: they can ask a question in natural language, apply filters or start from a node (artwork/artist/museum).
2. **Intent understanding.** The system detects the language (IT/EN), classifies the intent (specific search, thematic exploration, itinerary generation, comparison) and extracts the mentioned entities. *(Backend: \`llm\`/\`knowledge\` domain.)*
3. **Hybrid retrieval (GraphRAG).**
   a. **Semantic**: search for the most relevant nodes by meaning in Qdrant (descriptions, critical texts).
   b. **Structural**: starting from the found nodes, expansion in the MySQL graph along the most heavily weighted edges (neighbors, paths, thematic subgraph).
   c. **Fusion**: combination of the two sets into a coherent context, ordered by weight and relevance.
4. **AI reasoning.** The local LLM receives the subgraph + the textual passages and generates an answer that **cites the nodes and paths** used, with a level of detail adapted to the user's profile/level.
5. **Presentation.** The answer is accompanied by: a card of the cited nodes, a mini-visualization of the subgraph, and quick actions ("expand", "create itinerary", "save", "go deeper").
6. **Interactive navigation.** The user clicks on a node to progressively expand the neighbors; they can filter by type, movement, period (time slider) and geographic area. The visualization shows the **edge weight** (thickness/color).
7. **Thematic itinerary generation.** If the user requests an itinerary, the AI composes an ordered sequence of stops (works/rooms/museums) optimized for theme, time, level, accessibility and proximity; the itinerary can be saved, shared and exported.
8. **Feedback and learning.** The user rates the answer and the itinerary; the feedback feeds the recomputation of weights (usage frequency, usefulness) and improves future suggestions.
9. **Visit continuity.** Before the visit: preparation and itinerary; in the gallery: step-by-step itinerary (also audio/accessible); afterwards: deeper exploration and saving to the profile.

### 4.3 Flow C — Contribution and curation (community/curators)

1. **Starting a contribution.** A contributor or curator creates/edits a node (e.g. a new work of the local museum) through a guided form.
2. **AI assistance.** The system suggests the node type, missing attributes, possible connections and alignments to standard vocabularies (AAT, Wikidata).
3. **Relationship proposal with evidence.** The user or the AI propose edges indicating source/evidence; the initial weight reflects the strength of the evidence.
4. **Moderation.** According to the policy: auto-publishing for trusted curators, or a review queue for community contributions. A curator approves, edits or rejects.
5. **Immutable versioning.** Every change generates a new version (no in-place mutation); the history remains browsable and reversible.
6. **Emergent ranking.** The most validated and most used contributions emerge in the graph (greater weight and visibility), in line with the emergent ranking logic of the consumer vertical.

---

## 5. Graph model (node types, relationship types, weighting criteria)

The model specializes the base schema of the graph engine for the cultural domain, inspired by CIDOC-CRM and the Getty vocabularies (AAT/ULAN/TGN) and Iconclass, but keeping it pragmatic and mappable onto MySQL + Qdrant.

### 5.1 Node types

| Node type | Description | Main attributes | Standard mapping |
|--------------|-------------|----------------------|--------------------|
| \`Opera\` | Artwork / cultural object | title, dating, technique, material, dimensions, subject, inventory number, image | CIDOC-CRM E22; LIDO |
| \`Artista\` | Author/creator | name, variants, birth/death, nationality, roles | CIDOC-CRM E21; ULAN |
| \`Museo\` | Institution that preserves works | name, type, coordinates, opening hours, website | CIDOC-CRM E40/E53 |
| \`Luogo\` | Geographic place (city, site) | name, coordinates, geographic hierarchy | TGN; CIDOC-CRM E53 |
| \`Movimento\` | Artistic movement/current | name, time interval, description | AAT |
| \`Periodo\` | Historical period (e.g. Renaissance) | name, time interval | AAT; CIDOC-CRM E4 |
| \`Tecnica\` | Execution technique (e.g. oil on canvas) | name, description | AAT |
| \`Materiale\` | Constitutive material | name | AAT |
| \`Soggetto\` | Iconographic subject/theme | name, code | Iconclass |
| \`Mostra\` | Exhibition event | title, venue, dates, theme | CIDOC-CRM E5/E7 |
| \`Collezione\` | Collection/holding | name, museum, criterion | — |
| \`Committente\` | Person/entity that commissions | name, period | CIDOC-CRM E21/E74 |
| \`FonteCritica\` | Critical text/bibliography | title, author, year | — |
| \`PercorsoTematico\` | Curated/generated itinerary | theme, stops, level | — |

### 5.2 Relationship types (directed and weighted edges)

| Relationship | From → To | Meaning | Weighting notes |
|-----------|--------|-------------|--------------|
| \`creata_da\` | Opera → Artista | Authorship attribution | High weight if attested; reduced if "attributed to / circle of" |
| \`conservata_in\` | Opera → Museo | Current location | Factual, high weight |
| \`appartiene_a\` | Opera/Artista → Movimento | Adherence to a current | Weight from source consensus |
| \`datata_in\` | Opera → Periodo | Temporal placement | — |
| \`realizzata_con\` | Opera → Tecnica/Materiale | Technique/material | Factual |
| \`raffigura\` | Opera → Soggetto | Iconographic subject | — |
| \`influenzato_da\` | Artista → Artista | Stylistic influence | Weight from strength and number of evidences |
| \`maestro_di\` | Artista → Artista | Workshop/pupil relationship | — |
| \`dialoga_con\` | Opera → Opera | Thematic/visual affinity | Weight from similarity + curation |
| \`esposta_in\` | Opera → Mostra | Presence in an exhibition | — |
| \`commissionata_da\` | Opera → Committente | Commission | — |
| \`situato_in\` | Museo/Luogo → Luogo | Geographic hierarchy | Factual |
| \`parte_di\` | Opera → Collezione | Belonging to a holding | — |
| \`simile_a\` | Opera → Opera | Semantic/visual similarity | Weight = similarity score |
| \`tappa_di\` | Opera/Museo → PercorsoTematico | Inclusion in an itinerary | Weight from thematic relevance |
| \`documentata_in\` | Opera/Artista → FonteCritica | Bibliographic reference | Weight = source authoritativeness |

### 5.3 Edge weighting criteria

The weight (a normalized continuous value, e.g. 0–1) expresses **how strong/reliable/relevant a relationship is** and drives both navigation (expansion from the most heavily weighted) and AI reasoning. It is computed as a configurable combination of the following factors:

| Factor | Description | Example |
|---------|-------------|---------|
| **Strength of evidence** | How certain/attested the relationship is | certain "creata_da" > "attributed to" |
| **Number and agreement of sources** | More independent, agreeing sources → greater weight | 3 catalogues confirm the influence |
| **Source authoritativeness** | Institutional/peer-reviewed source > generic | Getty/Europeana > blog |
| **Semantic/visual similarity** | For \`simile_a\`/\`dialoga_con\`, embedding score | cosine distance in Qdrant |
| **Usage frequency** | How often the relationship is traversed by users/AI | more navigated edges rise |
| **User feedback** | "Useful" votes, curator validations | curation increases the weight |
| **Recency/decay** | The weight can decay if not confirmed | old unvalidated relationships |
| **Thematic relevance** | For itineraries, adherence to the requested theme | — |

The weight is **recomputed incrementally** (immutability: each recomputation produces a new versioned value, it does not mutate the history) and the formula/coefficients are configuration parameters, so as to adapt to different contexts (single museum, territorial graph, scientific research).

---

## 6. Data sources & connectors (ingestion)

| Source | Type | Connector | Mode | Notes |
|-------|------|-----------|----------|------|
| **Wikidata** | Global LOD | SPARQL/REST + QID reconciliation | Batch pull + on-demand | Alignment of artists/works/museums; enrichment |
| **Europeana** | European CH aggregator | REST API | Batch pull | Metadata and media of European heritage |
| **Getty Vocabularies (AAT/ULAN/TGN)** | Controlled vocabularies | LOD/SPARQL | Pull + local cache | Normalization of terms, artists, places |
| **Iconclass** | Iconographic vocabulary | API/dump | Pull | Subject classification |
| **CIDOC-CRM / LIDO / museum records** | Catalographic standards | Dedicated parser/mapping | File import | Ontological mapping → internal schema |
| **ICCD records / national catalogues** | Cataloguing (IT) | CSV/XML parser | File import | Italian heritage |
| **IIIF** | High-resolution images | IIIF manifest | Link/embedding | Visualization and (evolution) visual embedding |
| **Local files** (CSV/Excel/JSON/PDF/DOCX/images) | Own sources | Upload + folder watcher (batch) | Pull/push | Reuse of existing document pipeline |
| **Exhibition/event feeds** | Exhibition events | API/CSV | Periodic pull | Synergy with the events domain |
| **Community contributions** | UGC | Form + API | Push | With moderation and versioning |

**Infrastructure reuse:** ingestion relies on the existing document pipeline (\`DocumentIngestionPipelineService\`, Tika, Tesseract, batch folder watcher), on Qdrant for embeddings and on MySQL/Flyway for the structure. The external LOD connectors are **optional**, cacheable locally and disablable for fully offline/private scenarios. Each connector implements the domain's Repository/port-out pattern and is packageable as a **PF4J plugin** distributed via the marketplace.

---

## 7. Features to create, develop and maintain (MVP → evolution)

### 7.1 MVP (first release of the Culture module)

| # | Feature | Type | LocalMind modules | Expected outcome |
|---|--------------|------|------------------|--------------|
| 1 | **Cultural domain schema** (nodes/relationships §5) as an installable module | CREATE | knowledge/graph, marketplace, Flyway | Node/edge types available in the engine |
| 2 | **Structured ingestion** (CSV/JSON/Excel) of works/artists/museums with validation and basic reconciliation | CREATE | document, knowledge, batch | Graph populated from own sources |
| 3 | **Unstructured text ingestion** + triple extraction via local LLM | CREATE | document, llm, knowledge | Nodes/edges from records and critical texts |
| 4 | **Dual persistence** structure (MySQL) + semantics (Qdrant) with provenance | DEVELOP (extends graph engine) | infrastructure, vectorstore | Nodes queryable by relationship and by meaning |
| 5 | **Edge weight computation** with configurable formula (§5.3) | CREATE | knowledge | Weighted edges |
| 6 | **Graph API** (CRUD nodes/edges, neighbors, paths, subgraphs) | CREATE | api, knowledge | REST endpoints \`/api/v1/...\` |
| 7 | **Cultural GraphRAG**: hybrid retrieval + answer with node/path citation | CREATE | llm, knowledge, vectorstore | Reasoned answers in chat |
| 8 | **"Culture" frontend feature**: search, filters (type/movement/period/area), node cards | CREATE | frontend (standalone, Signals) | Basic exploration |
| 9 | **Interactive graph visualization** with edge weights and progressive expansion | CREATE | frontend | Visual navigation |
| 10 | **Thematic itinerary generator** (theme/time/level/area) | CREATE | llm, knowledge, frontend | Saveable itineraries |
| 11 | **Bilingual IT/EN** (UI, translated enums, content per language) | DEVELOP | frontend, api, domain | Compliance with the i18n constraint |
| 12 | **Wikidata connector** (reconciliation/enrichment) | CREATE | PF4J plugin, knowledge | Alignment to the global graph |
| 13 | **Basic contributions and moderation** with immutable versioning | CREATE | knowledge, api, frontend | Controlled UGC |

### 7.2 Future evolutions

| # | Feature | Type | Value |
|---|--------------|------|--------|
| E1 | **Multimodal visual embedding** of works (similarity by image) | CREATE | \`simile_a\`/\`dialoga_con\` on a visual basis; reuse of existing Ollama multimodal adapters |
| E2 | **Europeana + Getty + Iconclass + IIIF connectors** | CREATE | Enrichment and standardization at scale |
| E3 | **Automatic suggestion of missing connections** (link prediction) with a review queue | CREATE | Surfacing non-obvious relationships |
| E4 | **Geolocated multi-museum / territorial itineraries** integrated with the tourism/events domain | DEVELOP | Culture–territory continuity |
| E5 | **In-gallery mode / accessible audio guide** (audio descriptions, adjustable level) | CREATE | Experience during the visit |
| E6 | **Advanced curatorial tools** (complex queries, provenance analysis, exhibition preparation) | CREATE | Value for curators/scholars |
| E7 | **Federation between LocalMind instances** (local graphs queryable over the network, privacy-preserving) | CREATE | Distributed "Wikipedia of places" |
| E8 | **Full CIDOC-CRM alignment + RDF/JSON-LD export** | DEVELOP | Interoperability with the LOD ecosystem |
| E9 | **Emergent ranking and contributor reputation** | DEVELOP | Community-driven quality |
| E10 | **Specialized domain packages** (contemporary art, archaeology, music, photography) via marketplace | CREATE | Extension of the model |

### 7.3 To maintain (ongoing maintenance)

- **Ontological mappings** (CIDOC-CRM/AAT/ULAN/TGN/Iconclass): updating as the vocabularies change.
- **LOD connectors**: adaptation to changes in external APIs/endpoints.
- **Weight formulas**: periodic tuning based on feedback and evidence.
- **Graph quality**: deduplication, reconciliation, correction of erroneous attributions, decay of unconfirmed relationships.
- **Flyway migrations** (one query per file) for every schema evolution.
- **IT/EN documentation** and logs in \`Sviluppi/\` updated with every development.
- **Local LLM/embedding models**: updating and quality evaluation on Ollama.

---

## 8. AI / GraphRAG use cases

1. **Exploratory thematic question.** "How is light represented from Caravaggio to the Impressionists?" → the AI retrieves semantically relevant nodes, traverses \`influenzato_da\`/\`appartiene_a\`/\`raffigura\` edges, and builds a narrative that cites the works, artists and movements involved, showing the subgraph.
2. **Personalized itinerary.** "I have 2 hours at the Louvre, I'm interested in the Italian Renaissance, basic level" → an ordered sequence of stops optimized for theme/time/level, with adapted explanations.
3. **Discovery of non-obvious connections.** "What does this work have in common with others in the collection?" → the AI highlights \`dialoga_con\`/\`simile_a\` edges and proposes non-obvious thematic/visual connections.
4. **Comparison.** "Compare the style of Titian and Tintoretto" → the AI extracts subgraphs of the two artists and compares their movements, techniques, subjects, influences, citing the evidence.
5. **Research with source citation (study).** "Which sources attest the attribution of this work?" → answer with \`documentata_in\` and \`creata_da\` edges weighted by authoritativeness.
6. **Curation assistance.** "Propose an itinerary for an exhibition on the master-pupil relationship in 16th-century Venice" → the AI selects artists via \`maestro_di\`, related works and suggests an exhibition sequence.
7. **Suggestion of missing connections (link prediction).** The AI proposes probable edges (influences, cycles, dialogues) as candidates for review, motivating each proposal.
8. **Accessible in-gallery Q&A.** During the visit, voice questions with short answers and audio descriptions, with an adjustable level of detail.

All use cases respect the **local-first** principle: reasoning runs on Ollama by default; cloud providers are optional and disablable. Answers **always cite the nodes and paths** used, for transparency and verifiability.

---

## 9. KPIs & success metrics

| Category | KPI | Indicative target |
|-----------|-----|-------------------|
| **Graph coverage** | No. of nodes/edges; % of works with ≥3 relationships; % of nodes reconciled to Wikidata/AAT | Steady growth; >70% of nodes with ≥3 edges |
| **Relationship quality** | % of edges with source/provenance; rate of edges corrected after review | >90% of edges with provenance |
| **AI effectiveness** | % of answers rated "useful"; % of answers with valid citations; hallucination rate | >80% useful; hallucinations <5% |
| **Engagement** | No. of questions/session; navigation depth (average hops); itineraries generated and saved | Increasing |
| **Thematic itineraries** | % of completed itineraries; itinerary satisfaction | >60% completed |
| **Community contributions** | No. of contributions; % approved; average review time | Increasing; review <48h |
| **Performance** | Graph query and GraphRAG latency; ingestion time per 1k records | Query <2s; efficient ingestion |
| **Privacy/local-first** | % of fully offline working deployments; zero data leak toward non-allowed cloud | 100% compliance |
| **i18n** | IT/EN translation coverage of UI and enums | 100% |

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---------|---------|-------------|
| **AI hallucinations** (invented relationships/attributions) | High (credibility) | GraphRAG constrained to the graph; answers with mandatory citation; proposed edges go to review, not auto-published |
| **Source quality/heterogeneity** | Medium | Validation at the boundaries, reconciliation to standard vocabularies, weighting by authoritativeness |
| **Duplicates and disambiguation** (artist homonyms, works) | Medium | Reconciliation on external identifiers (QID/ULAN/inventory), disambiguation queue |
| **Graph query scalability in MySQL** (no Neo4j) | Medium-high | Dedicated indexes, limited expansion depth, precompiled subgraphs/cache; reconsider the datastore only if necessary |
| **Rights/licenses on images and texts** | High (legal) | License tracking, use of IIIF/metadata, respect for source policies, opt-in for restricted content |
| **Abuse in community contributions** | Medium | Moderation, reputation, immutable versioning, anti-abuse thresholds |
| **Cost/latency of local embeddings and LLMs** | Medium | Ingestion in batch, cache, properly sized Ollama models; optional cloud only if allowed |
| **Privacy of confidential data** (provenances, valuations) | High | Local-first by default, no cloud transmission without consent, segregation of confidential content |
| **Drift of external vocabularies/APIs** | Low-medium | Local cache, versioned connectors, planned maintenance |
| **Cultural bias in the data** | Medium | Source transparency, plurality of perspectives, curation, possibility of community correction |

---

## 11. Maintenance & evolution

- **Graph hygiene**: periodic jobs for deduplication, reconciliation, decay of unconfirmed weights and flagging of inconsistencies; reuse of the batch layer.
- **Updating external sources**: periodic synchronization with Wikidata/Europeana/Getty with local cache and incremental diff.
- **Weight tuning**: periodic review of formulas and coefficients based on feedback and metrics.
- **Schema evolution**: every new node/relationship type goes through a Flyway migration (one query per file) and an update of the domain module in the marketplace.
- **AI quality**: periodic evaluation of answers (usefulness, citations, hallucinations); update of prompts and Ollama models.
- **Bilingual documentation**: every development updates \`documentation/\` (EN) and \`documentazione/\` (IT) and records a file in \`Sviluppi/\` with dated naming and checkpoints for complex tasks.
- **Extensibility**: new cultural domains (archaeology, music, photography, contemporary art) introduced as installable packages without touching the core.
- **Federation (evolution)**: a protocol to query graphs of other LocalMind instances while preserving privacy, toward a distributed network of heritages.

---

## 12. Integration with existing LocalMind modules

| LocalMind module/domain | Role in the Culture domain |
|--------------------------|---------------------------|
| **knowledge** | Foundation of the cultural graph engine: node/relationship types, weights, queries (neighbors/paths/subgraphs) |
| **document** | Ingestion of records, critical texts, PDFs/images; text extraction (Tika) and OCR (Tesseract) |
| **batch** | Folder watcher and scheduled ingestion/graph-hygiene jobs |
| **llm** | GraphRAG, triple extraction, itinerary and answer generation; provider routing with Ollama default and fallback |
| **vectorstore (Qdrant)** | Embedding of descriptions/texts and semantic/visual similarity between nodes |
| **persistence (MySQL/JPA/Flyway)** | Graph structure, attributes, weights, provenance, versioning; UUID \`CHAR(36)\`; one query per migration |
| **mcp** | Exposure of the graph as an MCP tool for external agents; integration of cultural tools |
| **agent** | Agents that orchestrate exploration, itinerary generation and enrichment |
| **marketplace + plugin (PF4J)** | Distribution of the domain schema and connectors (Wikidata, Europeana, Getty, IIIF) as installable modules |
| **auth** | User profiles, roles (consumer/contributor/curator), moderation permissions |
| **common (analytics/events)** | KPIs, domain events (node created, itinerary generated), feedback |
| **calendar / email / messaging** | Notifications about new exhibitions, saved itineraries, visit reminders; synergy with territorial events |
| **finetuning** | (Evolution) adaptation of local models to art-historical vocabulary |
| **Frontend (Angular 21, Signals)** | "Culture" feature: search, filters, node cards, graph visualization, itinerary generator; bilingual IT/EN with translated enums |

**Synergy with the other consumer domains.** The cultural graph shares \`Luogo\`/\`Evento\` nodes with the tourism and events domains: a work or a museum can be stops on a broader territorial itinerary, realizing the vision of a **single engine, multiple ecosystems**. Specialization happens only at the level of node/relationship types and installed modules, in full compliance with the project constraints (local-first, default Ollama AI, MySQL+Qdrant, PF4J plugins, privacy, open source, IT/EN).
`;
