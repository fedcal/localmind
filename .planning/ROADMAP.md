# Roadmap: LocalMind — Universal Knowledge Graph Engine

## Overview

LocalMind evolves from a document/semantic-search platform into a universal knowledge graph engine that surfaces non-obvious connections across any domain while staying local-first. The build follows a strict three-act shape:

1. **Engine (Phases 1-7)** — the shared, domain-agnostic core, built in dependency order: schema foundation, weighted graph core API, semantic layer, plugin domain schemas + NodeTypeRegistry, the GraphRAG service, and the two frontend surfaces (graph visualization + GraphRAG chat). Nothing downstream is possible until this is complete. The six cross-cutting ingest domains (KGCORE, GRAG, VIZX, MOD, INGX, SEC) enrich these phases rather than adding new ones.
2. **Application verticals (Phases 8-26)** — nineteen isolated PF4J domain modules, one phase each, each declaring its own node/relation vocabulary over the shared engine. Ten consumer verticals (Phases 8-17) and nine enterprise verticals (Phases 18-26). The first vertical of each ecosystem is the seed that carries the original CONS-* (Turismo) and ENT-* (Knowledge base aziendale) requirements; the other seventeen are promoted granular verticals (each its own requirement family). Because every vertical is an independent module, the verticals are largely parallelisable once the engine is complete.
3. **Closing (Phase 27)** — bilingual IT+EN documentation and i18n for every new vertical, with bilingual enum labels surfaced to the frontend and zero untranslated keys.

The existing `knowledge` domain is extended throughout — no parallel graph domain is ever created. All 179 v1 requirements map to exactly one of these 27 phases.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

### Engine (shared core)

- [ ] **Phase 1: Schema Foundation** - Flyway migrations add domainId, weight, privacyLevel, embeddingId, confidenceScore, outbox table — the non-negotiable prerequisite for everything
- [ ] **Phase 2: Weighted Graph Core API** - CRUD for nodes/edges, BFS traversal, shortest path, subgraph extraction with cycle guard, async weight update, SecurityContext filtering, keyword search
- [ ] **Phase 3: Graph Semantic Layer** - Dedicated Qdrant localmind_graph_nodes collection, document-to-node auto-conversion, MySQL-Qdrant reconciliation job
- [ ] **Phase 4: Plugin Domain Schemas & NodeTypeRegistry** - PF4J GraphDomainSchemaExtension, NodeTypeRegistry validation gate, isolated Flyway per module, domain-module JAR skeletons
- [ ] **Phase 5: GraphRAG Service** - 6-step GraphRAG pipeline, cited-path answers, entity extraction batch job, token budget context builder, privacy guard for cloud LLMs, async weight reinforcement
- [ ] **Phase 6: Frontend Graph Visualization** - Force-directed canvas, pan/zoom/expand, weight-encoded edges, type icons, filter panel, server-side pagination
- [ ] **Phase 7: Frontend GraphRAG Chat** - Streaming SSE GraphRAG chat, collapsible "path used" panel, in-flight submit guard, IT/EN labels

### Application verticals — Consumer

- [ ] **Phase 8: Turismo & territorio (TUR)** - SEED consumer vertical: Place/POI/Esercizio/Evento/Esperienza/Itinerario nodes, geolocation, reviews, photos, OSM + Wikidata connectors, moderation, hidden-gem ranking
- [ ] **Phase 9: Eventi & spettacoli (EVT)** - schema.org/Event graph, ICS/JSON-LD ingestion, dedup, nearby-events API, discovery feed + map, GraphRAG, follow feed
- [ ] **Phase 10: Ristorazione & locali (RIST)** - Dining graph with menus/allergens/diets, multi-dimension reviews + weight engine, PDF menu OCR, semantic search, GraphRAG chat
- [ ] **Phase 11: Itinerari & esperienze (ITIN)** - Hallucination-free itinerary generation: TTDP optimizer over weighted subgraph, grounded narrative synthesis, .ics/PDF export
- [ ] **Phase 12: Education & studenti (EDU)** - Personal student knowledge graph, material ingestion (Whisper/OCR), AI concept extraction, GraphRAG tutor, flashcards
- [ ] **Phase 13: Cultura, arte & musei (CULT)** - Cultural KG (CIDOC-CRM/IIIF aligned), structured + LLM-triple ingestion, thematic itineraries, Wikidata connector, immutable versioning
- [ ] **Phase 14: Sport & outdoor (SPORT)** - Trails/facilities/events graph, standard difficulty scales (CAI/SAC/UIAA), GPX ingestion, OSM connector, safety moderation
- [ ] **Phase 15: Commercio & shopping locale (COMM)** - Local commerce graph, bulk merchant import + geocoding, semantic catalog, GraphRAG chat, reviews + weights
- [ ] **Phase 16: Real estate & immobiliare (RE)** - Explainable home search: OMI + OSM connectors, travel-time edges via self-hosted routing, profile-aware hybrid search, GraphRAG explanation
- [ ] **Phase 17: Servizi & sanità locale (SAN)** - Need-to-service KG, HSDS/FHIR/DCAT-AP_IT alignment, catalog editor, conversational assistant, emergency handling

### Application verticals — Enterprise

- [ ] **Phase 18: Knowledge base aziendale (ENT-KB)** - SEED enterprise vertical: Process/Git/Microservice/API/Database/Team/Decision/Runbook + KB nodes, Git + OpenAPI ingestion, gap detection, IMAP mail, multi-hop GraphRAG
- [ ] **Phase 19: Architettura software (ARCH)** - Software estate as navigable KG (C4/Backstage/SBOM), Git+AST + Maven/npm + OpenAPI/AsyncAPI connectors, impact analysis 1-N hop
- [ ] **Phase 20: Processi & workflow (PROC)** - Process-mining KG (BPMN 2.0 + event logs), RACI editor, discovery + conformance, bottleneck/bus-factor analysis, GraphRAG Q&A
- [ ] **Phase 21: Persone & competenze (PERS)** - Organizational skill graph (ESCO/Dreyfus), skill extraction, inferred profiles, find-the-expert GraphRAG, gap analysis, GDPR consent
- [ ] **Phase 22: Clienti & fornitori / CRM-SRM (CRM)** - Customer/Supplier 360 graph, CRM connector + CSV/Excel, entity resolution (P.IVA/VAT/DUNS), email sentiment, contract-expiry alerts
- [ ] **Phase 23: Mail & comunicazioni (MAIL)** - IMAP mailbox to people-communications-topics KG, MIME/thread reconstruction, attachment ingestion, commitment extraction, Ollama-only policy
- [ ] **Phase 24: Ticketing & decisioni / ADR (TICK)** - Causal/decisional KG over tickets + ADRs (MADR), Jira/GitHub ingestion, implicit-link AI deduction, why/what-if GraphRAG
- [ ] **Phase 25: Onboarding & formazione (ONB)** - Enterprise onboarding/training KG, permissioned ingestion, role-based adaptive paths, GraphRAG tutor, quizzes, visibility model
- [ ] **Phase 26: Compliance & audit (COMP)** - Demonstrable compliance KG (ISO 27001/SOC 2/NIS2/DORA/GDPR), normative ingestion, control mapping, evidence + tamper-evident audit trail

### Closing

- [ ] **Phase 27: Documentation & i18n** - Bilingual IT+EN docs for every new vertical, bilingual enum labels surfaced to frontend, no untranslated keys

## Phase Details

### Phase 1: Schema Foundation
**Goal**: The database schema is extended with the complete, non-retrofittable foundation that every downstream graph feature depends on — typed node/edge columns, all must-do-from-day-one fields (weight, domainId, privacyLevel, embeddingId, confidenceScore), composite indexes, and the outbox table for dual-store sync.
**Mode:** mvp
**Depends on**: Nothing (first phase)
**Requirements**: GRAPH-01, GRAPH-02, PRIV-01
**Success Criteria** (what must be TRUE):
  1. Flyway applies all new migrations cleanly on a fresh MySQL 8 instance (no manual DDL needed)
  2. Every knowledge_entity row carries domainId, embeddingId, and privacyLevel columns
  3. Every knowledge_relation row carries weight, domainId, confidenceScore, and usageCount columns with the required composite indexes
  4. The graph_sync_event outbox table exists and accepts insert, read, and delete operations
  5. The schema family naming convention (V79 onward) is documented before V79 is written, preventing future migration conflicts
**Plans**: TBD
**Enrichment from ingest**: KGCORE — core domain-agnostic node types (DOCUMENT, PERSON, ORGANIZATION, PLACE, CONCEPT, EVENT, TECHNOLOGY) + core relations, and the [0,1] weighted-edge model with its per-domain factor set, are laid down here (KGCORE-01/02, KGCORE-06 brownfield baseline). SEC — the privacyLevel field plus the principal/tenant + at-rest encryption foundation (SEC-01/05) is seeded here for ReBAC to build on.

### Phase 2: Weighted Graph Core API
**Goal**: Users can create, read, update, and delete typed nodes and weighted edges via REST, traverse the graph (BFS neighbors, shortest path, subgraph) without infinite loops, receive weight updates asynchronously, and get only the nodes their security context allows.
**Mode:** mvp
**Depends on**: Phase 1
**Requirements**: GRAPH-03, GRAPH-04, GRAPH-05, GRAPH-06, GRAPH-07, GRAPH-08, SEARCH-01, SEARCH-03, PRIV-03
**Success Criteria** (what must be TRUE):
  1. User can create, read, update, and delete nodes via /api/v1/graph/nodes with all required fields (domainId, privacyLevel, type)
  2. User can create, read, update, and delete typed weighted edges via /api/v1/graph/edges
  3. User can retrieve BFS neighbors (filtered by relation type), the shortest weighted path between two nodes, and a typed subgraph — all complete without infinite recursion on cyclic data (cycle guard and cte_max_recursion_depth active)
  4. Edge weight updates are applied asynchronously and do not delay the API response to the caller
  5. Graph query results include only nodes the requesting user is authorised to see (SecurityContext filter applied in KnowledgeGraphPort)
  6. User can search nodes by keyword and filter results by node type, relation type, and domain
**Plans**: TBD
**Enrichment from ingest**: KGCORE — the graph query operations (weighted k-hop neighbors, shortest/max-weight path, filtered subgraph, search-by-relation) formalise KGCORE-03. SEC — the SecurityContext filter is upgraded toward ReBAC node/edge authorization with a deny-by-default pre-retrieval filter on MySQL (SEC-01/02).

### Phase 3: Graph Semantic Layer
**Goal**: Node embeddings live in a dedicated Qdrant collection separate from document chunks, existing document ingestion automatically produces graph nodes, and a reconciliation job keeps MySQL and Qdrant consistent.
**Mode:** mvp
**Depends on**: Phase 1
**Requirements**: SEARCH-02, INGEST-01, INGEST-02
**Success Criteria** (what must be TRUE):
  1. User can search graph nodes by semantic similarity (the localmind_graph_nodes Qdrant collection exists and returns ranked results)
  2. Ingesting a document automatically creates a corresponding KnowledgeEntity in MySQL and a node embedding in Qdrant within the same transaction flow
  3. The Spring Batch reconciliation job detects MySQL-Qdrant inconsistencies (nodes in MySQL with no embedding, or orphaned Qdrant points) and corrects them
  4. Semantic search respects domain scoping (a query for domainId=consumer does not return enterprise nodes)
**Plans**: TBD
**Enrichment from ingest**: INGX — the generalized IngestionPipeline with SourceConnector / IngestionState / RawRecord abstractions, the FILE_SYSTEM/IMAP/CSV MVP connectors, incremental sync cursors (Airbyte model FULL_REFRESH/INCREMENTAL_APPEND/INCREMENTAL_DEDUPED) with base dedup, and source_lineage tracking (INGX-01..04/06) are the foundation every vertical connector reuses. Note: INGEST-01 graph-node creation applies to any Tika-parseable document (no format whitelist — conflict resolved against codebase).

### Phase 4: Plugin Domain Schemas & NodeTypeRegistry
**Goal**: A PF4J domain module can declare its own node and relation type vocabulary at runtime; the NodeTypeRegistry rejects any write that uses an unregistered type; and plugin Flyway migrations run against an isolated schema history so they cannot corrupt the core migration log.
**Mode:** mvp
**Depends on**: Phase 1
**Requirements**: SCHEMA-01, SCHEMA-02, SCHEMA-03
**Success Criteria** (what must be TRUE):
  1. A PF4J plugin implementing GraphDomainSchemaExtension registers its node and relation types on application startup without a change to core source code
  2. Attempting to create a node with a type not declared by any loaded module returns a 422 error with a descriptive message (NodeTypeRegistry validation gate active)
  3. Installing or uninstalling a domain module does not modify the core flyway_schema_history table (each module has its own flyway_schema_history_{module} table)
  4. Vertical module JAR skeletons load without error and their type vocabularies appear in the schema registry
**Plans**: TBD
**Enrichment from ingest**: KGCORE — the modular type registry replacing hard-coded enums (KGCORE-04) is exactly this phase. INGX — per-connector scheduling registration hangs off the same plugin lifecycle. Every one of the 19 application verticals (Phases 8-26) is an installable domain module declaring its node/relation vocabulary through this extension point.

### Phase 5: GraphRAG Service
**Goal**: Users can pose natural-language questions answered by navigating the weighted graph; the response cites the nodes and edges used; a Spring Batch job populates the graph from existing documents; the LLM context is token-budget capped; private nodes are excluded from cloud LLM calls; and traversed edge weights increase asynchronously.
**Mode:** mvp
**Depends on**: Phase 2, Phase 3, Phase 4
**Requirements**: RAG-01, RAG-02, RAG-03, RAG-04, PRIV-02
**Success Criteria** (what must be TRUE):
  1. Posting a question to /api/v1/graph-rag/query returns an answer with a non-empty sourceNodeIds and traversedEdges list
  2. The answer context never exceeds the configured token budget (GraphRagContextBuilder prunes at 70 % of num_ctx with a WARN log)
  3. The Spring Batch entity-extraction job processes existing documents and writes KnowledgeEntity rows validated by NodeTypeRegistry before any persist
  4. Nodes with privacyLevel=LOCAL are absent from the context sent to any cloud LLM provider (OLLAMA-only path is untouched)
  5. Traversed edge weights increment asynchronously after the answer is returned (GraphQueryCompletedEvent; response latency is unaffected)
**Plans**: TBD
**Enrichment from ingest**: GRAG — the orchestrator (Qdrant semantic seed + MySQL weighted traversal) with adaptive query routing/intent classification, entity linking + RRF fusion + re-ranking, cited answers (nodes + path + weights + chunk) with an anti-hallucination "non so" threshold, and the ask/expand/path/subgraph APIs under /api/v1/knowledge/graph/* (GRAG-01/02/04/06) define this service. The ACL fail-safe permission gate (GRAG-03) and SEC's AI-routing policy (Ollama-default) + context minimization (SEC-02/03) harden PRIV-02/PRIV-03. GraphRAG calls inherit the LLM Gateway retry/timeout/fallback policy — no separate retry layer.

### Phase 6: Frontend Graph Visualization
**Goal**: Users can explore the knowledge graph in an interactive force-directed canvas — panning, zooming, progressively expanding from a focal node, reading edge weights visually, filtering by type and domain, and inspecting node details — without browser freeze.
**Mode:** mvp
**Depends on**: Phase 2
**Requirements**: VIZ-01, VIZ-02, VIZ-03, VIZ-04, VIZ-05, VIZ-06
**Success Criteria** (what must be TRUE):
  1. User sees a force-directed canvas rendering typed nodes (color/icon by type) and edges whose visual thickness encodes the relation weight
  2. User can pan, zoom, and progressively expand any node to reveal its neighbors without a full page reload
  3. User can filter the visible graph by node type, relation type, and domain in real time using the filter panel
  4. Clicking a node opens a side detail panel showing its properties, relation count, and adjacent edges
  5. Loading a large subgraph does not freeze the browser (server paginates to max 200 nodes per response; force simulation terminates on convergence)
**Plans**: TBD
**UI hint**: yes
**Enrichment from ingest**: VIZX — standalone Angular feature (lazy routing + Signal store) with a WebGL render engine and force-directed layout in a Web Worker, weight→thickness/size mapping + weight-range/type/domain filters, selection + detail panel + hover highlight, per-domain theme + bilingual IT/EN legend + search→center + chat→graph jump, controlled degradation + basic accessibility, server-pagination budget with truncation metadata (VIZX-01..06). **OPEN DECISION**: rendering library — existing ROADMAP names Cytoscape.js; PRD doc 22 (HIGH confidence) specifies Sigma.js + graphology (Canvas fallback). Both non-locked; planner picks during Phase 6 planning (see INGEST-CONFLICTS.md [INFO] "Visualization library divergence").

### Phase 7: Frontend GraphRAG Chat
**Goal**: Users can ask graph-aware natural-language questions from the UI, receive a streaming answer, and see the graph traversal path in a collapsible panel — with the submit button protected against duplicate in-flight requests.
**Mode:** mvp
**Depends on**: Phase 5, Phase 6
**Requirements**: RAG-05
**Success Criteria** (what must be TRUE):
  1. User can type a question in the GraphRAG chat page and receive a streaming answer via SSE
  2. The response includes a collapsible "path used" panel listing the cited nodes and traversed edges
  3. The submit button is disabled while a query is in flight; a typing/loading indicator is visible
  4. All UI labels in the GraphRAG chat interface are available in Italian and English via TranslatePipe
**Plans**: TBD
**UI hint**: yes
**Enrichment from ingest**: VIZX-05 — "AI chat → graph jump" links the cited path from a GraphRAG answer directly into the Phase 6 visualizer.

### Phase 8: Turismo & territorio (TUR)
**Goal**: A consumer Tourism domain module (the seed of the consumer ecosystem) lets users discover places, POIs, businesses, events, experiences and itineraries over the weighted KG, contribute reviews and photos, and surface hidden gems — with community contributions gated by moderation.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5, Phase 6
**Requirements**: CONS-01, CONS-02, CONS-03, CONS-04, CONS-05, CONS-06
**Success Criteria** (what must be TRUE):
  1. User can create and view Luogo/Area, POI, Esercizio, Evento, Esperienza and Itinerario nodes; Place/POI nodes store latitude/longitude shown in the detail panel and on a map view
  2. User can submit a star rating and textual review on any tourism node; the submission is visible to others only after moderation approval
  3. User can upload photos to a node reusing the existing document upload pipeline; the media appears in the node detail
  4. OpenStreetMap (Overpass/ODbL) and Wikidata/Wikipedia (CC0/CC BY-SA) connectors ingest and enrich POIs/places with mapping, dedup and baseline edge weights
  5. Community contributions enter a PENDING_MODERATION state with spam/profanity auto-flag before publication
  6. Each node displays an emergent ranking recomputed by a scheduled batch job, including an anti-popularity hidden-gem boost
**Plans**: TBD
**UI hint**: yes
**Enrichment from ingest**: TUR-01..10 (intel) folded into this seed — tourism node/edge vocabulary (VICINO_A, TAPPA_DI, OSPITA_EVENTO, RAGGIUNGIBILE_CON…), NL discovery with theme/proximity/season filters + cited synthesis, interactive graph + map UI, and reviews→edge-weight recalculation. MOD — the configurable ModerationPolicy/ReputationPolicy, weighted voting, emergent ranking, local-Ollama moderation + anti-spam, and the /api/v1/community/* API + Angular moderation UI (MOD-01..06) are realised here and reused by every other community vertical.

### Phase 9: Eventi & spettacoli (EVT)
**Goal**: An events/shows discovery vertical, schema.org/Event-aligned, lets users find what's on nearby, follow artists and venues, and ask event questions answered with citations.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5, Phase 6
**Requirements**: EVT-01, EVT-02, EVT-03, EVT-04, EVT-05, EVT-06, EVT-07, EVT-08, EVT-09
**Success Criteria** (what must be TRUE):
  1. Events graph types (Evento, Venue, Artista, Organizzatore, Rassegna, Genere, Data/Periodo, Utente, Recensione) exist with schema.org/Event-aligned fields (dataInizio/Fine, EventStatusType, EventAttendanceMode, luogoRef, performerRefs, offers) and Flyway migrations
  2. The ICS/iCal PF4J EventSource connector and schema.org JSON-LD ingestion validate, embed (Ollama) and persist events to Qdrant
  3. Entity resolution de-duplicates events and venues during ingestion
  4. /api/v1/events exposes CRUD and a nearby-events query returning geo-filtered results
  5. A discovery feed with filters and a map view render in the Angular events feature
  6. GraphRAG chat answers event questions with node/path citations; users can follow artists/venues for a personalized feed; enums are IT/EN
**Plans**: TBD
**UI hint**: yes

### Phase 10: Ristorazione & locali (RIST)
**Goal**: A community-driven dining vertical lets users explore restaurants, menus, dishes and dietary suitability, leave multi-dimension reviews, and ask GraphRAG questions over the dining graph.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5, Phase 6
**Requirements**: RIST-01, RIST-02, RIST-03, RIST-04, RIST-05, RIST-06, RIST-07, RIST-08
**Success Criteria** (what must be TRUE):
  1. Dining schema (Locale, Cucina, Piatto, Ingrediente, Menu/SezioneMenu, Recensione, Persona, Quartiere, Itinerario, Collezione, Tag) with edges SERVE/APPARTIENE_A_CUCINA/CONTIENE/ADATTO_A/NEL_QUARTIERE and bilingual enums + Flyway
  2. User can CRUD a Locale and CRUD Piatto/Menu entries capturing allergens and diets
  3. Multi-dimension reviews feed a weight engine v1 combining reviewer reliability, freshness and consensus
  4. CSV/JSON batch import and PDF menu extraction (Tika/OCR) populate the catalog
  5. Qdrant semantic indexing plus structured search filters return matching locali/piatti; GraphRAG chat answers with citations
  6. The Angular ristorazione feature ships with IT/EN i18n and a base moderation queue
**Plans**: TBD
**UI hint**: yes

### Phase 11: Itinerari & esperienze (ITIN)
**Goal**: A GraphRAG vertical generates hallucination-free itineraries over a territory KG — optimizing real routes and themes while avoiding overtourism — and exports them.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5, Phase 6
**Requirements**: ITIN-01, ITIN-02, ITIN-03, ITIN-04, ITIN-05, ITIN-06, ITIN-07, ITIN-08
**Success Criteria** (what must be TRUE):
  1. Itinerary node/relation types (Luogo, POI, Esperienza, Evento, Hospitality, Itinerario, Tema, Persona, Recensione) with edges VICINO_A/PERCORRIBILE_VERSO/HA_TEMA/COMPLEMENTARE_A/CO_VISITATO_CON/STAGIONALE_IN/ALTERNATIVA_DI + Flyway
  2. An ItineraryRequest DTO is validated and drives hybrid candidate retrieval (Qdrant + MySQL) that builds a weighted subgraph
  3. A greedy + 2-opt TTDP optimizer balances distance/time, theme affinity and anti-overtourism diversity
  4. GraphRAG narrative synthesis is grounded only on the selected nodes/paths (no invented stops)
  5. POST/GET /api/v1/itineraries persists each itinerary as a node; a timeline/map UI allows manual node editing and basic feedback
  6. An itinerary can be exported to .ics, PDF and a shareable link
**Plans**: TBD
**UI hint**: yes

### Phase 12: Education & studenti (EDU)
**Goal**: A personal student knowledge graph ingests study material, extracts concepts and prerequisites, and offers a GraphRAG tutor over the learner's own sources.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5, Phase 6
**Requirements**: EDU-01, EDU-02, EDU-03, EDU-04, EDU-05, EDU-06, EDU-07, EDU-08
**Success Criteria** (what must be TRUE):
  1. Education model (Corso, Argomento, Concetto, Materiale, Chunk, Esercizio/Quiz, Flashcard, Obiettivo, Percorso, Esame, Persona, Nota) with edges e_prerequisito_di/spiega/approfondisce/copre/verifica/annota wired into the domain
  2. Course/material CRUD is available at /api/v1/education/* with an Angular feature
  3. Didactic material ingestion (Tika/OCR/Whisper + chunking + Qdrant with provenance) works for documents, audio and video
  4. AI concept extraction + edge deduction assigns a confidence weight and is reviewable via a human-in-the-loop graph review
  5. The interactive personal graph visualization renders, and the GraphRAG tutor answers over own material with source citation
  6. Flashcards + basic self-checks are generated; enums are IT/EN
**Plans**: TBD
**UI hint**: yes

### Phase 13: Cultura, arte & musei (CULT)
**Goal**: A cultural knowledge graph (CIDOC-CRM/IIIF aligned) supports thematic itineraries, structured and unstructured ingestion, and community contributions with immutable versioning.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5, Phase 6
**Requirements**: CULT-01, CULT-02, CULT-03, CULT-04, CULT-05, CULT-06, CULT-07, CULT-08
**Success Criteria** (what must be TRUE):
  1. Cultural schema module (Opera, Artista, Museo, Luogo, Movimento, Periodo, Tecnica, Materiale, Soggetto, Mostra, Collezione, Committente, FonteCritica, PercorsoTematico) with weighted directed relations (creata_da/conservata_in/raffigura/influenzato_da/esposta_in)
  2. Structured ingestion (CSV/JSON/Excel) and unstructured text ingestion with LLM triple extraction both persist to MySQL + Qdrant with provenance and computed edge weights
  3. Graph CRUD / neighbors / paths / subgraph API responds for cultural nodes
  4. Cultural GraphRAG (semantic + structural expansion) answers with cited nodes/paths
  5. The Angular Cultura feature renders an interactive graph visualization
  6. A thematic itinerary generator filters by theme/time/level/accessibility/geo-proximity; the Wikidata connector and moderated community contributions with immutable versioning work; IT/EN bilingual
**Plans**: TBD
**UI hint**: yes

### Phase 14: Sport & outdoor (SPORT)
**Goal**: An outdoor vertical maps trails, activities, facilities and sport events with standard difficulty scales, GPX ingestion and safety-focused moderation.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5, Phase 6
**Requirements**: SPORT-01, SPORT-02, SPORT-03, SPORT-04, SPORT-05, SPORT-06, SPORT-07, SPORT-08
**Success Criteria** (what must be TRUE):
  1. Outdoor schema (Sentiero/Percorso, Itinerario, Attivita, POI, Impianto, Rifugio, Falesia, EventoSportivo, Difficolta, Stagione, Condizione, PuntoPartenza, Servizio, Gestore, Recensione) with edges HA_DIFFICOLTA/PRATICABILE_IN/PARTE_DA/ATTRAVERSA/OFFRE_SERVIZIO/ADATTO_A
  2. Bilingual IT/EN enums encode standard scales (CAI T/E/EE/EEA, SAC T1-T6, MTB S0-S5, UIAA/French/YDS, via ferrata, ski mountaineering, EAWS/AINEVA avalanche 1-5)
  3. GPX/GeoJSON ingestion computes metrics and the OSM Overpass connector ingests with dedup
  4. A CRUD API serves nodes/edges with proximity + filter queries
  5. GraphRAG search returns citations and the Angular Outdoor UI renders results
  6. Community contributions pass through a safety-oriented moderation queue and edge weights v1 are computed
**Plans**: TBD
**UI hint**: yes

### Phase 15: Commercio & shopping locale (COMM)
**Goal**: A local commerce vertical maps shops, artisans, products and services, supports bulk merchant import, and offers semantic catalog search plus a GraphRAG chat.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5, Phase 6
**Requirements**: COMM-01, COMM-02, COMM-03, COMM-04, COMM-05, COMM-06, COMM-07
**Success Criteria** (what must be TRUE):
  1. Commerce schema (Negozio, Artigiano/Produttore, Prodotto, Servizio, CategoriaMerceologica, MateriaPrima, Mercato, Evento, Quartiere, Certificazione, Cliente, Recensione, Tradizione) with edges VENDE/PRODOTTO_DA/USA_MATERIA_PRIMA/HA_CERTIFICAZIONE/INCARNA_TRADIZIONE + Flyway
  2. CRUD for Negozio/Prodotto/Artigiano nodes works
  3. Bulk CSV merchant import with geocoding and dedup plus per-shop product catalog populate the graph
  4. Semantic catalog indexing (Qdrant) supports search/filters by category, proximity and open-now
  5. A commerce GraphRAG chat and an interactive graph visualization are available
  6. Reviews and votes feed edge weight calculation v1; full IT/EN i18n with address geocoding
**Plans**: TBD
**UI hint**: yes

### Phase 16: Real estate & immobiliare (RE)
**Goal**: An explainable, profile-aware home-search KG combines OMI valuations, real travel times, and semantic matching to justify why a property fits a buyer.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5, Phase 6
**Requirements**: RE-01, RE-02, RE-03, RE-04, RE-05, RE-06, RE-07, RE-08
**Success Criteria** (what must be TRUE):
  1. Real-estate schema (Immobile, Edificio, Via, ZonaOMI, Quartiere, Comune, Quotazione, Servizio/POI, Scuola, Trasporto, AreaVerde, ProfiloUtente, LuogoAncora, RicercaSalvata) with edges SI_TROVA_IN/VICINO_A/RAGGIUNGE/QUOTATO_DA/ZONA_GEMELLA/CERCA/PREFERISCE + Flyway
  2. The OMI Agenzia Entrate CSV connector (ZonaOMI + Quotazione) and OSM/Overpass POI connector ingest data; geocoding + point-in-polygon assigns zones
  3. Manual/CSV property import generates description embeddings; VICINO_A weighted edges are computed via self-hosted routing (OSRM/Valhalla/GraphHopper) and price-vs-OMI deviation is linked
  4. Hybrid search (graph filters + semantic) scores results against the user profile, anchor places and weighted priorities
  5. A GraphRAG explanation cites the nodes/paths behind a match; the Angular realestate search/results UI renders with a base graph visualization
  6. Enums are bilingual IT/EN
**Plans**: TBD
**UI hint**: yes

### Phase 17: Servizi & sanità locale (SAN)
**Goal**: A need-to-service KG for local public, health and professional services helps users go from a stated need to the right service, aligned to interoperability standards.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5, Phase 6
**Requirements**: SAN-01, SAN-02, SAN-03, SAN-04, SAN-05, SAN-06, SAN-07, SAN-08
**Success Criteria** (what must be TRUE):
  1. Services schema (Bisogno, Servizio, Prestazione, Ente, Sede, Professionista, Requisito, Documento/Modulo, Procedura, Categoria, AreaTerritoriale, Contatto, FonteDato) with edges SODDISFA/EROGATO_DA/RICHIEDE_REQUISITO/ALTERNATIVA_A/SERVE_AREA/STEP_DI + Flyway
  2. A bilingual needs+services taxonomy plus the HSDS connector and tabular importer feed the ingestion pipeline
  3. A services node/edge CRUD API with a catalog editor and moderation is available
  4. A need-to-service GraphRAG conversational assistant plus catalog search/filters return a full service card
  5. Community contributions & feedback work, and disclaimer & emergency handling is enforced
  6. The model aligns with interchange standards (HSDS 3.x, FHIR Human Services Directory, CKAN/DCAT-AP_IT, HL7 CDA2/FSE 2.0, Schema.org GovernmentService); IT/EN bilingual
**Plans**: TBD
**UI hint**: yes

### Phase 18: Knowledge base aziendale (ENT-KB)
**Goal**: An enterprise knowledge base module (the seed of the enterprise ecosystem) unifies documents, processes, technical assets, people and decisions into one graph, with a multi-hop GraphRAG assistant and ACL-filtered answers.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5
**Requirements**: ENT-01, ENT-02, ENT-03, ENT-04, ENT-05
**Success Criteria** (what must be TRUE):
  1. User can view Process, GitRepository, Microservice, ApiEndpoint, Database, Team, Decision and Runbook nodes (plus KB node types Documento/SOP/FAQ/ADR/Persona/Skill/Concetto) with typed relations between them
  2. Configuring a Git repository URL automatically creates GitRepository nodes populated with file-structure metadata
  3. Importing an OpenAPI spec creates Microservice, ApiEndpoint and Schema nodes with PROVIDES/DEPENDS_ON relations
  4. A gap-detection report identifies orphan services (no owner team), undocumented APIs (no spec) and processes with no assigned owner
  5. IMAP email ingestion creates communication-person-theme nodes without sending person names or email content to external LLM providers
  6. The GraphRAG assistant cites sources and supports multi-hop dependency/impact, find-the-expert and contradiction detection, with an ACL permission filter, curation queue and an Angular Signal-store interactive graph visualization
**Plans**: TBD
**UI hint**: yes
**Enrichment from ingest**: ENT-KB-01..08 (intel) folded into this seed — enterprise KB node vocabulary, document-ingestion reuse (Tika/Tesseract/Whisper/Chunking) + Qdrant semantic search, AI entity/relation extraction + dedup/reconciliation, /api/v1/knowledge/graph CRUD, ACL + edge-weight + curation, and MVP connectors (local folders, email, calendar, Git).

### Phase 19: Architettura software (ARCH)
**Goal**: The software estate becomes an AI-navigable weighted KG for impact and dependency analysis across repos, services, APIs, infrastructure and decisions.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5
**Requirements**: ARCH-01, ARCH-02, ARCH-03, ARCH-04, ARCH-05, ARCH-06, ARCH-07, ARCH-08
**Success Criteria** (what must be TRUE):
  1. Software graph core model (Repository, System, Service, Api, Endpoint, Event/Topic, Database, Table, Library, Vulnerability/CVE, InfraResource, Pipeline, Domain, Team, Person, Adr, Document) with relations CALLS/PROVIDES_API/CONSUMES_API/DEPENDS_ON/PUBLISHES/DEPLOYED_ON/AFFECTED_BY/GOVERNS
  2. A CRUD + query API serves neighbors/paths/subgraphs
  3. A Git + AST connector (Java/TS) populates code structure
  4. Build/Dependency (Maven/npm), OpenAPI/AsyncAPI, and catalog-info (Backstage) connectors enrich the graph
  5. Entity resolution / canonical identity plus base weight calculation deduplicate assets; semantic indexing to Qdrant and base GraphRAG answer questions
  6. Impact analysis over 1-N hops renders in an interactive graph visualizer; scheduled incremental ingestion runs; IT/EN i18n
**Plans**: TBD
**UI hint**: yes

### Phase 20: Processi & workflow (PROC)
**Goal**: A process-mining KG unifies designed, executed, tribal and automated process views to expose bottlenecks, bus-factors and conformance gaps.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5
**Requirements**: PROC-01, PROC-02, PROC-03, PROC-04, PROC-05, PROC-06, PROC-07, PROC-08
**Success Criteria** (what must be TRUE):
  1. A 'process' domain (Processo, Passo/Attivita, Evento, Gateway, Ruolo, Persona, Sistema, Dato/Documento, Regola/SOP, Automazione, Deviazione, Eccezione, IstanzaProcesso, KPI) with edges PRECEDE/INNESCA/RACI/PRODUCE/GOVERNATO_DA/AUTOMATIZZATO_DA/DEVIA_DA + Flyway
  2. BPMN 2.0 import (BPMN2KG) and AI extraction of steps/roles/systems from SOP/policy both build the graph
  3. A process/steps/roles/systems/RACI editor offers CRUD, and a generic event-log connector (CSV/DB) normalizes runs
  4. Process discovery computes as-is frequencies/times and base conformance checking compares to-be vs as-is
  5. Bottleneck/lead-time analysis plus a bus-factor and step-role-system map are produced
  6. A hybrid GraphRAG Q&A answers with citations; the Angular 'process' UI renders a graph visualization with tribal annotations and bilingual enums
**Plans**: TBD
**UI hint**: yes

### Phase 21: Persone & competenze (PERS)
**Goal**: An organizational skill graph locates experts, infers skill profiles with provenance, and supports gap analysis under GDPR-compliant consent.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5
**Requirements**: PERS-01, PERS-02, PERS-03, PERS-04, PERS-05, PERS-06, PERS-07, PERS-08
**Success Criteria** (what must be TRUE):
  1. A skill graph model extends the knowledge domain (Persona, Competenza, Ruolo, Team, Progetto, Certificazione, Evidenza, Cliente/Dominio, Obiettivo) with edges POSSIEDE_COMPETENZA/RICHIEDE/ASPIRA_A/RICOPRE_RUOLO/HA_LAVORATO_A/MENTORE_DI/ENDORSED_BY
  2. Anagrafica import plus a taxonomy (custom + ESCO) loads, and skill extraction from documents runs via EntityExtractorPort + local Ollama
  3. An inferred skill profile carries provenance and a weight on the Dreyfus 1-5 mastery scale
  4. A human-in-the-loop validation UI lets reviewers confirm/correct inferred skills
  5. A find-the-expert GraphRAG answers expertise queries and a basic gap analysis renders over a graph visualization subset
  6. Privacy & consent base is enforced (GDPR legal basis, granular per-source opt-out)
**Plans**: TBD
**UI hint**: yes

### Phase 22: Clienti & fornitori / CRM-SRM (CRM)
**Goal**: A unified weighted CRM/SRM KG delivers Customer 360 / Supplier 360 views with entity resolution, interaction sentiment, and contract-expiry awareness.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5
**Requirements**: CRM-01, CRM-02, CRM-03, CRM-04, CRM-05, CRM-06, CRM-07
**Success Criteria** (what must be TRUE):
  1. A CRM/SRM model (Organizzazione, Persona, Sede, Contratto, Opportunita, Ordine, Fattura, Interazione, Prodotto/Servizio, Componente, AreaGeografica, CategoriaSpesa, RischioConcentrazione) with edges HA_RUOLO/CONTROLLA/HA_CONTRATTO/HA_ORDINE/ACQUISTA/FORNISCE/ESPONE_A/ALTERNATIVO_A on MySQL+Qdrant
  2. CSV/Excel import plus one CRM connector load data
  3. Base entity resolution uses strong keys (P.IVA/VAT/DUNS) + weak keys with auto-merge and a human review queue
  4. Email interaction ingestion runs with local sentiment scoring
  5. A Vista 360 with interactive graph exploration plus base weights/indicators and a GraphRAG Q&A answer with citations
  6. Contract-expiry alerts fire and the UI/enums are bilingual IT/EN
**Plans**: TBD
**UI hint**: yes

### Phase 23: Mail & comunicazioni (MAIL)
**Goal**: A self-hosted vertical turns IMAP mailboxes into a weighted people-communications-topics KG with thread reconstruction, commitment extraction and an Ollama-only privacy policy.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5
**Requirements**: MAIL-01, MAIL-02, MAIL-03, MAIL-04, MAIL-05, MAIL-06, MAIL-07, MAIL-08
**Success Criteria** (what must be TRUE):
  1. An IMAP connector performs incremental UID/UIDVALIDITY sync with encrypted credentials
  2. The EmailMessage model extension (Message-ID/In-Reply-To/References/Cc/attachments/language) plus MIME parsing, de-quoting, signature removal and thread reconstruction produce clean threads
  3. Attachment extraction runs through the document pipeline (Tika/OCR to Qdrant)
  4. Local AI enrichment (NER, message/thread summary, classification) plus person/org entity resolution annotate messages
  5. A communications graph (Persona, Organizzazione, Casella, Messaggio, Thread, Tema, Allegato, Impegno, Decisione, Evento) with edges HA_SCRITTO/DESTINATARIO_DI/RISPONDE_A/COMUNICA_CON/CONTIENE_IMPEGNO persists with Qdrant embeddings under perimeter filters; the REST API + GraphRAG answer with citations
  6. A UI for account config / persona-org dossier / graph chat plus commitment-deadline extraction with a mail to-do view works under visibility perimeters and an Ollama-only LLM policy
**Plans**: TBD
**UI hint**: yes

### Phase 24: Ticketing & decisioni / ADR (TICK)
**Goal**: A causal/decisional KG over tickets and ADRs answers why/what-if questions and links decisions to their context automatically.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5
**Requirements**: TICK-01, TICK-02, TICK-03, TICK-04, TICK-05, TICK-06, TICK-07, TICK-08
**Success Criteria** (what must be TRUE):
  1. A decisions/knowledge extension (Ticket, Bug, Feature, Epic, Task, Incident, Decisione(ADR), Alternativa, Requisito, ChangeRequest, Repository/Servizio, Commit/PR, Persona, Team, Commento, Chunk) with edges causa/blocca/dipende_da/duplica/motiva/scarta_alternativa/supera/contraddice/implementa/risolve
  2. Structured ADR CRUD (MADR) plus import of ADR Markdown works
  3. Ticket ingestion from Jira/GitHub populates the graph
  4. Graph construction from explicit links plus AI deduction of implicit links runs, reviewed via human-in-the-loop
  5. An interactive causal graph visualization renders
  6. A GraphRAG why/what-if answers with citations and automatic ADR-context linking; IT/EN i18n
**Plans**: TBD
**UI hint**: yes

### Phase 25: Onboarding & formazione (ONB)
**Goal**: An enterprise onboarding/training KG drives role-based adaptive paths with a permissioned GraphRAG tutor and self-check quizzes.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5
**Requirements**: ONB-01, ONB-02, ONB-03, ONB-04, ONB-05, ONB-06, ONB-07, ONB-08
**Success Criteria** (what must be TRUE):
  1. An onboarding/knowledge extension (Persona, Team, Ruolo, Competenza, Concetto, Documento, Chunk, Procedura/Runbook, Processo, Sistema, Decisione(ADR), Percorso, Tappa/Modulo, Quiz, Evento, Glossario) with edges e_prerequisito_di/richiede_competenza/documenta/copre/verifica/mentor_di/obsoleto_rispetto_a
  2. Org-structure CRUD + CSV import plus permissioned internal-knowledge ingestion (Tika/Tesseract/Whisper + chunking + Qdrant + provenance/visibility) work
  3. AI entity/competence extraction + edge deduction is reviewed via a human-in-the-loop graph review
  4. An interactive graph visualization renders, and role-based onboarding paths (template + instance) can be created
  5. A GraphRAG tutor answers with permissions and source citation, with base quizzes/self-checks
  6. A graph permission/visibility model is enforced; IT/EN i18n
**Plans**: TBD
**UI hint**: yes

### Phase 26: Compliance & audit (COMP)
**Goal**: A demonstrable, local-first compliance KG maps frameworks to controls and evidence, performs coverage/gap analysis, and keeps a tamper-evident audit trail.
**Mode:** mvp
**Depends on**: Phase 4, Phase 5
**Requirements**: COMP-01, COMP-02, COMP-03, COMP-04, COMP-05, COMP-06, COMP-07, COMP-08
**Success Criteria** (what must be TRUE):
  1. A compliance model (Framework, Requisito, Controllo, Evidenza, Asset, Processo, Rischio, Policy, Trattamento(GDPR), Persona/Ruolo, Fornitore, Audit, Finding, Incidente, RemediationTask, AuditTrailEntry) with edges SODDISFA/DIMOSTRA/EQUIVALE_A/MITIGA/VIOLA/RILEVA/CORREGGE/REGISTRA on MySQL + weighted edges + Flyway
  2. CRUD for framework/requirements/controls at /api/v1/compliance/* plus normative ingestion (PDF → segmentation → Qdrant) work
  3. SODDISFA/EQUIVALE_A mapping runs with AI human-in-the-loop, and evidence management stores a hash + evidence freshness
  4. Coverage & gap analysis plus a GraphRAG compliance Q&A (Ollama default) render in a coverage dashboard
  5. A tamper-evident audit trail (hash-chain base) records changes
  6. Preloaded framework templates (ISO 27001:2022, SOC 2) ship; IT/EN i18n
**Plans**: TBD
**UI hint**: yes

### Phase 27: Documentation & i18n
**Goal**: All new graph features and all nineteen verticals have bilingual documentation (IT+EN) for operators and contributors; every new node type, relation type, and privacyLevel enum exposes Italian and English labels that the frontend renders via TranslatePipe; and all new UI screens are fully bilingual with no untranslated fallback keys.
**Mode:** mvp
**Depends on**: Phases 8-26 (all vertical phases)
**Requirements**: DOCS-01, DOCS-02, DOCS-03
**Success Criteria** (what must be TRUE):
  1. Documentation for every graph extension scope and every vertical (Turismo, Eventi, Ristorazione, Itinerari, Education, Cultura, Sport, Commercio, Real estate, Servizi/Sanità, KB aziendale, Architettura, Processi, Persone, CRM, Mail, Ticketing, Onboarding, Compliance) is present with both a .it.md and .en.md (or equivalent bilingual structure)
  2. All new enums (node types, relation types, privacyLevel) return an IT and EN label pair when queried; the Angular TranslatePipe renders the correct language without fallback to the raw enum key
  3. Switching the UI language between Italian and English produces no untranslated string in any new graph, visualization, GraphRAG, or vertical screen
**Plans**: TBD
**UI hint**: yes

## Progress

**Execution Order:**
The engine phases (1-7) execute in dependency order: Phases 3 and 4 are independent of each other and can be parallelised (both depend only on Phase 1); Phase 5 needs 2+3+4; Phase 6 needs 2; Phase 7 needs 5+6. Once the engine (Phases 1-7) is complete, the nineteen vertical phases (8-26) are **largely independent of one another** — each is an isolated PF4J domain module declaring its own node/relation vocabulary over the shared engine — and can therefore be **parallelised** or sequenced freely by priority. Consumer verticals (8-17) depend on Phase 4 + Phase 5 + Phase 6; enterprise verticals (18-26) depend on Phase 4 + Phase 5. Phase 27 (Documentation & i18n) depends on all vertical phases and runs last.

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Schema Foundation | 0/TBD | Not started | - |
| 2. Weighted Graph Core API | 0/TBD | Not started | - |
| 3. Graph Semantic Layer | 0/TBD | Not started | - |
| 4. Plugin Domain Schemas & NodeTypeRegistry | 0/TBD | Not started | - |
| 5. GraphRAG Service | 0/TBD | Not started | - |
| 6. Frontend Graph Visualization | 0/TBD | Not started | - |
| 7. Frontend GraphRAG Chat | 0/TBD | Not started | - |
| 8. Turismo & territorio (TUR) | 0/TBD | Not started | - |
| 9. Eventi & spettacoli (EVT) | 0/TBD | Not started | - |
| 10. Ristorazione & locali (RIST) | 0/TBD | Not started | - |
| 11. Itinerari & esperienze (ITIN) | 0/TBD | Not started | - |
| 12. Education & studenti (EDU) | 0/TBD | Not started | - |
| 13. Cultura, arte & musei (CULT) | 0/TBD | Not started | - |
| 14. Sport & outdoor (SPORT) | 0/TBD | Not started | - |
| 15. Commercio & shopping locale (COMM) | 0/TBD | Not started | - |
| 16. Real estate & immobiliare (RE) | 0/TBD | Not started | - |
| 17. Servizi & sanità locale (SAN) | 0/TBD | Not started | - |
| 18. Knowledge base aziendale (ENT-KB) | 0/TBD | Not started | - |
| 19. Architettura software (ARCH) | 0/TBD | Not started | - |
| 20. Processi & workflow (PROC) | 0/TBD | Not started | - |
| 21. Persone & competenze (PERS) | 0/TBD | Not started | - |
| 22. Clienti & fornitori / CRM-SRM (CRM) | 0/TBD | Not started | - |
| 23. Mail & comunicazioni (MAIL) | 0/TBD | Not started | - |
| 24. Ticketing & decisioni / ADR (TICK) | 0/TBD | Not started | - |
| 25. Onboarding & formazione (ONB) | 0/TBD | Not started | - |
| 26. Compliance & audit (COMP) | 0/TBD | Not started | - |
| 27. Documentation & i18n | 0/TBD | Not started | - |
