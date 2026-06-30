# Requirements: LocalMind — Universal Knowledge Graph

**Defined:** 2026-06-29
**Last restructured:** 2026-06-30 (merge-restructure-from-ingest — 19 verticals promoted to granular v1 phases)
**Core Value:** L'AI naviga un grafo pesato di conoscenza per rispondere a domande complesse e far emergere collegamenti non evidenti, in qualsiasi dominio, restando local-first.

> Brownfield: si **estende** il dominio `knowledge` esistente (`KnowledgeEntity`, `KnowledgeRelation`, `KnowledgeGraphService`, `KnowledgeGraphPort`), non si crea un dominio parallelo. Datastore: MySQL 8.0 (struttura, CTE ricorsive) + Qdrant (semantica). Nessun Neo4j.

> **Struttura v1**: un motore condiviso (famiglie GRAPH/SCHEMA/SEARCH/VIZ/RAG/INGEST/PRIV/DOCS, Fasi 1-7 + 27) più **diciannove verticali applicativi** (Fasi 8-26), ciascuno un modulo di dominio PF4J indipendente. Il primo verticale di ogni ecosistema è il *seed* che porta i requisiti originali CONS-* (Turismo, Fase 8) ed ENT-* (Knowledge base aziendale, Fase 18); gli altri diciassette verticali sono promossi da v2 a v1 e hanno una propria famiglia di requisiti. I sei domini trasversali dell'ingest (KGCORE/GRAG/VIZX/MOD/INGX/SEC) **non** sono nuove famiglie: arricchiscono le fasi 1-7 esistenti (vedi note "Enrichment from ingest" in ROADMAP.md).

## v1 Requirements

### Schema & Modello a Grafo (GRAPH)

- [ ] **GRAPH-01**: Le migrazioni Flyway (una query per file) estendono lo schema con nodi tipizzati, archi tipizzati e i campi obbligatori `domainId`, `weight`, `privacyLevel`, `embeddingId`, `confidenceScore`
- [ ] **GRAPH-02**: Esiste una tabella outbox (`graph_sync_event`) per la sincronizzazione asincrona MySQL↔Qdrant
- [ ] **GRAPH-03**: L'utente può creare, leggere, aggiornare ed eliminare un nodo via `/api/v1/graph/nodes`
- [ ] **GRAPH-04**: L'utente può creare, leggere, aggiornare ed eliminare una relazione (arco pesato e tipizzato) via `/api/v1/graph/edges`
- [ ] **GRAPH-05**: L'utente può ottenere i vicini di un nodo (BFS) filtrando per tipo di relazione
- [ ] **GRAPH-06**: L'utente può ottenere il percorso più breve tra due nodi
- [ ] **GRAPH-07**: L'utente può estrarre un sottografo per tipo di nodo/relazione, con guardia anti-ciclo sulle CTE ricorsive MySQL
- [ ] **GRAPH-08**: Il peso di un arco si aggiorna in modo asincrono da segnali (frequenza d'uso, feedback) senza bloccare la query

> *Enrichment (ingest):* KGCORE-01 (tipi nodo/relazione core domain-agnostic), KGCORE-02 (pesi in [0,1] + WeightPolicy per dominio), KGCORE-03 (operazioni query: k-hop pesato, shortest/max-weight path, subgraph filtrato), KGCORE-06 (baseline brownfield: estendere KnowledgeEntity/Relation, non forkare).

### Schema di Dominio Modulare (SCHEMA)

- [ ] **SCHEMA-01**: Un modulo di dominio (plugin PF4J) può dichiarare i propri tipi di nodo e relazione con etichette IT/EN tramite un extension point dedicato
- [ ] **SCHEMA-02**: Un `NodeTypeRegistry` valida ogni scrittura rifiutando tipi di nodo/relazione non dichiarati
- [ ] **SCHEMA-03**: Le migrazioni Flyway dei plugin sono isolate dallo storico migrazioni del core (SchemaHistory separato per modulo)

> *Enrichment (ingest):* KGCORE-04 (type registry modulare al posto di enum hard-coded). Ciascuno dei 19 verticali (Fasi 8-26) si registra tramite questo extension point.

### Ricerca sul Grafo (SEARCH)

- [ ] **SEARCH-01**: L'utente può cercare nodi per testo (keyword) sul grafo
- [ ] **SEARCH-02**: L'utente può cercare nodi per similarità semantica riusando Qdrant (collezione dedicata `localmind_graph_nodes`)
- [ ] **SEARCH-03**: L'utente può filtrare i risultati per tipo di nodo, tipo di relazione e dominio

### Visualizzazione del Grafo (VIZ)

- [ ] **VIZ-01**: L'utente vede un canvas interattivo del grafo con layout force-directed
- [ ] **VIZ-02**: L'utente può fare pan, zoom ed espandere progressivamente i nodi a partire da un nodo focale
- [ ] **VIZ-03**: Lo spessore/encoding visivo dell'arco riflette il peso della relazione
- [ ] **VIZ-04**: Il colore/icona del nodo riflette il suo tipo; tooltip su hover e click-to-detail in pannello laterale
- [ ] **VIZ-05**: Il backend pagina i sottografi (max 200 nodi per risposta) per evitare freeze del browser
- [ ] **VIZ-06**: L'utente può filtrare la vista per tipo di nodo/relazione e dominio

> *Enrichment (ingest):* VIZX-01..06 (Angular standalone + Signal store, motore WebGL con fallback Canvas, layout force-directed in Web Worker, mappatura peso→spessore, filtri, pannello dettaglio, legenda bilingue, degradazione controllata + accessibilità). **Decisione aperta:** libreria di rendering (Cytoscape.js vs Sigma.js+graphology) — scelta in fase di pianificazione della Fase 6.

### GraphRAG & AI (RAG)

- [ ] **RAG-01**: L'utente può porre domande in linguaggio naturale e ricevere risposte generate navigando il grafo (retrieval semantico Qdrant + traversal a 1+ hop)
- [ ] **RAG-02**: La risposta cita i nodi usati e gli archi attraversati (`usedNodes[]`, `traversedEdges[]`)
- [ ] **RAG-03**: Una pipeline (job Spring Batch) estrae entità e relazioni dai documenti esistenti per popolare il grafo, con validazione via `NodeTypeRegistry`
- [ ] **RAG-04**: Il contesto inviato all'LLM è limitato da un budget di token (`GraphRagContextBuilder`)
- [ ] **RAG-05**: La chat GraphRAG è fruibile in UI con streaming SSE e pannello "percorso usato"

> *Enrichment (ingest):* GRAG-01 (orchestratore semantico+strutturale, routing adattivo), GRAG-02 (entity linking + RRF fusion + re-ranking), GRAG-04 (risposte citate + soglia anti-allucinazione "non so"), GRAG-06 (feedback loop + API ask/expand/path/subgraph). GRAG-03 (gate ACL fail-safe) → PRIV.

### Ingestione & Popolamento (INGEST)

- [ ] **INGEST-01**: L'ingestione di un documento esistente crea anche un nodo del grafo, oltre all'embedding Qdrant (qualsiasi documento parsabile da Tika — nessuna whitelist di formati)
- [ ] **INGEST-02**: Un job di riconciliazione consuma l'outbox e mantiene coerenti MySQL e Qdrant

> *Enrichment (ingest):* INGX-01 (IngestionPipeline generalizzata: SourceConnector/IngestionState/RawRecord), INGX-02 (connettori MVP FILE_SYSTEM/IMAP/CSV), INGX-03 (sync incrementale + cursori modello Airbyte + dedup base), INGX-04 (lineage + secrets cifrati + scheduling per connettore), INGX-06 (tabelle Flyway ingestion_* + observability base).

### Privacy & Sicurezza (PRIV)

- [ ] **PRIV-01**: Ogni nodo ha un `privacyLevel` (LOCAL / SHARED / PUBLIC)
- [ ] **PRIV-02**: Il GraphRAG esclude i nodi privati dalle chiamate a LLM cloud (consenso esplicito richiesto)
- [ ] **PRIV-03**: Le query sul grafo applicano il `SecurityContext` filtrando i nodi non accessibili all'utente

> *Enrichment (ingest):* SEC-01 (modello principal/tenant + ReBAC nodo/arco), SEC-02 (access decision deny-by-default + filtro pre-retrieval su MySQL+Qdrant), SEC-03 (data classification + label PII + policy routing AI Ollama-default + minimizzazione contesto), SEC-04 (audit trail tamper-evident + difesa prompt-injection base), SEC-05 (secret management + cifratura at-rest/in-transit), SEC-06 (dashboard sicurezza + GDPR consent).

### Documentazione & i18n (DOCS)

- [ ] **DOCS-01**: La documentazione per ogni ambito di estensione (motore + 19 verticali) è disponibile e mantenuta in `documentation/14-ambiti-estensione/` (IT+EN)
- [ ] **DOCS-02**: Tutte le nuove enum del grafo (tipi nodo/relazione, privacyLevel) espongono etichette IT/EN verso il frontend
- [ ] **DOCS-03**: UI e documentazione utente delle nuove feature sono bilingui IT/EN

---

### Verticale Consumer — Turismo & territorio (CONS) — *seed, Fase 8*

> Famiglia originale CONS-* mantenuta come requisito della Fase 8 (Turismo). I requisiti intel TUR-01..10 sono **ripiegati** come dettaglio di accettazione/criteri di successo (vedi ROADMAP.md Fase 8), per evitare doppia copertura.

- [ ] **CONS-01**: Sono disponibili i tipi di nodo Place, POI, Restaurant/Esercizio, Event, Experience, Itinerary (modulo turismo)
- [ ] **CONS-02**: Un nodo Place/POI memorizza la geolocalizzazione (lat/lon) come proprietà
- [ ] **CONS-03**: L'utente può inviare una valutazione a stelle e una recensione testuale su un nodo
- [ ] **CONS-04**: L'utente può caricare foto su un nodo riusando la pipeline di upload documenti esistente
- [ ] **CONS-05**: I contributi della community passano da una coda di moderazione con auto-flag (spam/profanità) prima della pubblicazione
- [ ] **CONS-06**: Il ranking dei contenuti usa una media bayesiana calcolata da un job schedulato (incl. boost hidden-gem anti-popolarità)

> *Enrichment (ingest):* TUR-01..10 (connettori OSM/Overpass + Wikidata, discovery NL con filtri tema/prossimità/stagione, mappa + grafo interattivo, ricalcolo pesi da recensioni). MOD-01..06 (ModerationPolicy/ReputationPolicy, voto pesato, ranking emergente, moderazione Ollama, API /api/v1/community/* + UI) realizzati qui e riusati dagli altri verticali community.

### Verticale Consumer — Eventi & spettacoli (EVT) — *Fase 9*

- [ ] **EVT-01**: Tipi nodo/arco eventi (Evento, Venue, Artista, Organizzatore, Rassegna, Genere, Data/Periodo, Utente, Recensione, Itinerario; archi SI_TIENE_IN/IL, ESEGUITO_DA, ORGANIZZATO_DA, HA_GENERE, EDIZIONE_DI, SEGUE, PARTECIPA_A) + Flyway
- [ ] **EVT-02**: Connettore PF4J EventSource ICS/iCal
- [ ] **EVT-03**: Ingestione JSON-LD schema.org + validazione + embedding Ollama + pipeline Qdrant
- [ ] **EVT-04**: Entity resolution / deduplica per eventi e venue
- [ ] **EVT-05**: API CRUD eventi + query eventi-vicini a /api/v1/events
- [ ] **EVT-06**: Feed discovery con filtri + vista mappa
- [ ] **EVT-07**: Chat GraphRAG sugli eventi con citazioni
- [ ] **EVT-08**: Traduzione enum IT/EN, follow + feed personalizzato, docs bilingui
- [ ] **EVT-09**: Modello evento allineato a schema.org/Event (dataInizio/Fine, EventStatusType, EventAttendanceMode, luogoRef, performerRefs, offers, sorgente, idEsterno)

### Verticale Consumer — Ristorazione & locali (RIST) — *Fase 10*

- [ ] **RIST-01**: Schema dining (Locale, Cucina, Piatto, Ingrediente, Menu/SezioneMenu, Recensione, Persona, Luogo, Quartiere, Evento, Itinerario, Collezione, Tag; archi SERVE, APPARTIENE_A_CUCINA, CONTIENE, ADATTO_A, RECENSISCE, VICINO_A, NEL_QUARTIERE, TAPPA_DI, HA_ATTRIBUTO, GESTITO_DA) + enum bilingui + Flyway
- [ ] **RIST-02**: CRUD Locale; CRUD Piatto/Menu con allergeni e diete
- [ ] **RIST-03**: Recensioni multi-dimensione + base Persona/reputazione
- [ ] **RIST-04**: Weight engine v1 (affidabilità recensore + freshness + consenso)
- [ ] **RIST-05**: Indicizzazione semantica Qdrant + filtri di ricerca strutturati
- [ ] **RIST-06**: Chat GraphRAG v1 con citazioni
- [ ] **RIST-07**: Import batch CSV/JSON + estrazione menu PDF (Tika/OCR)
- [ ] **RIST-08**: Frontend Angular ristorazione, i18n IT/EN, coda di moderazione base

### Verticale Consumer — Itinerari & esperienze (ITIN) — *Fase 11*

- [ ] **ITIN-01**: Tipi nodo/relazione itinerari (Luogo, POI, Esperienza, Evento, Hospitality, Itinerario, Tema, Persona, Recensione; archi VICINO_A, PERCORRIBILE_VERSO, HA_TEMA, COMPLEMENTARE_A, CO_VISITATO_CON, PARTE_DI, SI_SVOLGE_IN, STAGIONALE_IN, ALTERNATIVA_DI) + Flyway
- [ ] **ITIN-02**: DTO ItineraryRequest + validazione
- [ ] **ITIN-03**: Retrieval ibrido di candidati (Qdrant + MySQL) e costruzione sottografo pesato
- [ ] **ITIN-04**: Ottimizzatore TTDP greedy + 2-opt (distanza/tempo, affinità tematica, diversità anti-overtourism)
- [ ] **ITIN-05**: Sintesi narrativa GraphRAG ancorata ai nodi/percorsi selezionati
- [ ] **ITIN-06**: API REST POST/GET /api/v1/itineraries; persistenza itinerario-come-nodo
- [ ] **ITIN-07**: UI timeline/mappa + editing manuale nodi + feedback base
- [ ] **ITIN-08**: Export in .ics / PDF / link condivisibile

### Verticale Consumer — Education & studenti (EDU) — *Fase 12*

- [ ] **EDU-01**: Modello dominio education + wiring (Corso, Argomento, Concetto, Materiale, Chunk, Esercizio/Quiz, Flashcard, Obiettivo, Percorso, Esame, Persona, Nota; archi e_prerequisito_di, spiega, approfondisce, appartiene_a, verifica, copre, annota, fa_parte_del_percorso)
- [ ] **EDU-02**: CRUD corso/materiale + /api/v1/education/* + feature Angular
- [ ] **EDU-03**: Ingestione materiale didattico (Tika/OCR/Whisper + chunking + Qdrant con provenance)
- [ ] **EDU-04**: Estrazione concetti AI + deduzione archi con peso di confidenza
- [ ] **EDU-05**: Revisione grafo human-in-the-loop
- [ ] **EDU-06**: Visualizzazione grafo personale interattiva
- [ ] **EDU-07**: Tutor GraphRAG sul proprio materiale con citazione fonti
- [ ] **EDU-08**: Flashcard + self-check base; traduzione enum IT/EN

### Verticale Consumer — Cultura, arte & musei (CULT) — *Fase 13*

- [ ] **CULT-01**: Modulo schema dominio culturale (Opera, Artista, Museo, Luogo, Movimento, Periodo, Tecnica, Materiale, Soggetto, Mostra, Collezione, Committente, FonteCritica, PercorsoTematico; relazioni pesate dirette creata_da, conservata_in, datata_in, raffigura, influenzato_da, esposta_in, tappa_di, documentata_in)
- [ ] **CULT-02**: Ingestione strutturata (CSV/JSON/Excel) + testo non strutturato con estrazione triple LLM
- [ ] **CULT-03**: Doppia persistenza (MySQL + Qdrant) con provenance + calcolo peso archi
- [ ] **CULT-04**: API graph CRUD / neighbors / paths / subgraph
- [ ] **CULT-05**: GraphRAG culturale (espansione semantica + strutturale) con nodi/percorsi citati
- [ ] **CULT-06**: Feature frontend Cultura + visualizzazione grafo interattiva
- [ ] **CULT-07**: Generatore itinerari tematici (tema/tempo/livello/accessibilità/prossimità geo)
- [ ] **CULT-08**: Connettore Wikidata; contributi community + moderazione con versioning immutabile; bilingue IT/EN

### Verticale Consumer — Sport & outdoor (SPORT) — *Fase 14*

- [ ] **SPORT-01**: Schema grafo outdoor (Sentiero/Percorso, Itinerario, Attivita, POI, Impianto, Rifugio, Falesia, EventoSportivo, Difficolta, Stagione, Condizione, Luogo, PuntoPartenza, Servizio, Gestore, Contributore, Recensione, Documento; archi HA_DIFFICOLTA, PRATICABILE_IN, PARTE_DA, ATTRAVERSA, COLLEGA, OFFRE_SERVIZIO, VICINO_A, ADATTO_A, GESTITO_DA)
- [ ] **SPORT-02**: Enum bilingui IT/EN per difficoltà/stagione/attività/condizione con scale standard (CAI T/E/EE/EEA, SAC T1-T6, MTB S0-S5, UIAA/French/YDS, via ferrata, scialpinismo)
- [ ] **SPORT-03**: Ingestione GPX/GeoJSON con metriche calcolate
- [ ] **SPORT-04**: Connettore OSM Overpass con dedup
- [ ] **SPORT-05**: API CRUD per nodi/archi con query prossimità + filtri
- [ ] **SPORT-06**: Ricerca GraphRAG con citazioni
- [ ] **SPORT-07**: Feature UI Angular Outdoor
- [ ] **SPORT-08**: Contributi community + coda moderazione sicurezza + pesi archi v1

### Verticale Consumer — Commercio & shopping locale (COMM) — *Fase 15*

- [ ] **COMM-01**: Schema dominio commercio (Negozio, Artigiano/Produttore, Prodotto, Servizio, CategoriaMerceologica, MateriaPrima, Mercato, Evento, Quartiere, Certificazione, Cliente, Recensione, Tradizione; archi VENDE, PRODOTTO_DA, USA_MATERIA_PRIMA, FORNISCE, APPARTIENE_A_CATEGORIA, SITUATO_IN, VICINO_A, COMPLEMENTARE_A, HA_CERTIFICAZIONE, RECENSISCE, INCARNA_TRADIZIONE) + Flyway
- [ ] **COMM-02**: CRUD nodi Negozio/Prodotto/Artigiano
- [ ] **COMM-03**: Import bulk CSV commercianti + geocoding + dedup; catalogo prodotti per negozio
- [ ] **COMM-04**: Indicizzazione semantica catalogo (Qdrant) + ricerca/filtri base (categoria/prossimità/aperto-ora)
- [ ] **COMM-05**: Chat GraphRAG commercio + visualizzazione grafo interattiva
- [ ] **COMM-06**: Recensioni e voti + calcolo peso archi v1
- [ ] **COMM-07**: Geocoding indirizzi + i18n IT/EN completo

### Verticale Consumer — Real estate & immobiliare (RE) — *Fase 16*

- [ ] **RE-01**: Schema grafo real-estate (Immobile, Edificio, Via, ZonaOMI, Quartiere, Comune, Quotazione, Servizio/POI, Scuola, Trasporto, AreaVerde, ProfiloUtente, LuogoAncora, Recensione, RicercaSalvata; archi SI_TROVA_IN, VICINO_A tempo-reale, RAGGIUNGE, QUOTATO_DA, ZONA_GEMELLA, SERVITA_DA, CERCA, PREFERISCE/SCARTA) + Flyway
- [ ] **RE-02**: Connettore CSV OMI Agenzia Entrate (ZonaOMI + Quotazione) + connettore POI OSM/Overpass
- [ ] **RE-03**: Geocoding + assegnazione zona point-in-polygon
- [ ] **RE-04**: Import immobili manuale/CSV con embedding descrizioni
- [ ] **RE-05**: Archi pesati VICINO_A via routing self-hosted (OSRM/Valhalla/GraphHopper); linkage scostamento prezzo-vs-OMI
- [ ] **RE-06**: Ricerca ibrida (filtri grafo + semantica) con scoring base + profilo utente/luoghi-ancora/priorità pesate
- [ ] **RE-07**: Spiegazione GraphRAG con citazione nodi/percorsi + UI Angular realestate ricerca/risultati + visualizzazione grafo base
- [ ] **RE-08**: Enum bilingui IT/EN

### Verticale Consumer — Servizi & sanità locale (SAN) — *Fase 17*

- [ ] **SAN-01**: Schema dominio servizi (Bisogno, Servizio, Prestazione, Ente, Sede, Professionista, Requisito, Documento/Modulo, Procedura, Categoria, AreaTerritoriale, Contatto, FonteDato; archi SODDISFA, EROGATO_DA, HA_SEDE_PRESSO, RICHIEDE_REQUISITO/DOCUMENTO, ALTERNATIVA_A, APPARTIENE_A_CATEGORIA, SERVE_AREA, STEP_DI, HA_CONTATTO) + Flyway
- [ ] **SAN-02**: Tassonomia bisogni + servizi IT/EN
- [ ] **SAN-03**: Connettore HSDS + importer tabellare + pipeline ingestione
- [ ] **SAN-04**: API CRUD nodi/archi servizi + editor catalogo + moderazione
- [ ] **SAN-05**: Assistente conversazionale GraphRAG bisogno-servizio + ricerca/filtri catalogo + scheda servizio completa
- [ ] **SAN-06**: Contributi community & feedback
- [ ] **SAN-07**: Disclaimer & gestione emergenze; bilinguismo IT/EN
- [ ] **SAN-08**: Allineamento standard di interscambio (HSDS 3.x, FHIR Human Services Directory, CKAN/DCAT-AP_IT, HL7 CDA2/FSE 2.0, Schema.org GovernmentService)

---

### Verticale Enterprise — Knowledge base aziendale (ENT) — *seed, Fase 18*

> Famiglia originale ENT-* mantenuta come requisito della Fase 18 (Knowledge base aziendale). I requisiti intel ENT-KB-01..08 sono **ripiegati** come dettaglio di accettazione/criteri di successo (vedi ROADMAP.md Fase 18), per evitare doppia copertura.

- [ ] **ENT-01**: Sono disponibili i tipi di nodo Process, GitRepository, Microservice, ApiEndpoint, Database, Team, Decision, Runbook (modulo enterprise) + nodi KB (Documento, SOP, FAQ, ADR, Persona, Skill, Concetto)
- [ ] **ENT-02**: Un connettore Git crea nodi GitRepository e struttura file dai metadati del repository
- [ ] **ENT-03**: L'ingestione di una spec OpenAPI crea nodi Microservice, ApiEndpoint e Schema con relazioni tipizzate
- [ ] **ENT-04**: Il sistema rileva automaticamente dipendenze e lacune (servizi orfani, API non documentate, processi senza owner)
- [ ] **ENT-05**: L'ingestione mail (IMAP) popola il grafo con nodi comunicazione-persona-tema rispettando la privacy

> *Enrichment (ingest):* ENT-KB-01..08 (vocabolario KB enterprise, riuso pipeline documenti Tika/Tesseract/Whisper + Qdrant, estrazione entità/relazioni AI + dedup, CRUD /api/v1/knowledge/graph, GraphRAG multi-hop dependency/impact/find-the-expert/contradiction, filtro ACL + curation queue, visualizzazione grafo Angular Signal store, connettori MVP).

### Verticale Enterprise — Architettura software (ARCH) — *Fase 19*

- [ ] **ARCH-01**: Modello core grafo software (Repository, System, Service, Api, Endpoint, Event/Topic, Database, Table, Library, Vulnerability/CVE, InfraResource, Pipeline, Domain, Team, Person, Adr, Document; relazioni CALLS, PROVIDES_API, CONSUMES_API, DEPENDS_ON, READS/WRITES, PUBLISHES/SUBSCRIBES, DEPLOYED_ON, OWNED_BY, PART_OF, AFFECTED_BY, DECIDES/GOVERNS)
- [ ] **ARCH-02**: API CRUD + query (neighbors/paths/subgraphs)
- [ ] **ARCH-03**: Connettore Git + AST (Java/TS)
- [ ] **ARCH-04**: Connettore Build/Dependency (Maven/npm) + connettore OpenAPI/AsyncAPI + connettore catalog-info
- [ ] **ARCH-05**: Entity resolution / identità canonica + calcolo peso base
- [ ] **ARCH-06**: Indicizzazione semantica su Qdrant + GraphRAG base
- [ ] **ARCH-07**: Analisi d'impatto 1-N hop + visualizzatore grafo interattivo
- [ ] **ARCH-08**: Ingestione incrementale schedulata + i18n IT/EN

### Verticale Enterprise — Processi & workflow (PROC) — *Fase 20*

- [ ] **PROC-01**: Schema grafo processi + nuovo dominio 'process' in localmind-domain (Processo, Passo/Attivita, Evento, Gateway, Ruolo, Persona, Sistema, Dato/Documento, Regola/SOP, Automazione, Deviazione, Eccezione, IstanzaProcesso, KPI; archi PRECEDE, INNESCA, RESPONSABILE/APPROVA/CONSULTATO/INFORMATO (RACI), PRODUCE, RICHIEDE, GOVERNATO_DA, AUTOMATIZZATO_DA, DEVIA_DA, PASSA_A)
- [ ] **PROC-02**: Import BPMN 2.0 (BPMN2KG)
- [ ] **PROC-03**: Estrazione AI di step/ruoli/sistemi da SOP/policy
- [ ] **PROC-04**: CRUD editor processi/step/ruoli/sistemi/RACI
- [ ] **PROC-05**: Connettore event-log generico (CSV/DB) + normalizzazione
- [ ] **PROC-06**: Process discovery (frequenze/tempi as-is) + conformance checking base (to-be vs as-is)
- [ ] **PROC-07**: Analisi bottleneck/lead-time + bus-factor e mappa step-ruolo-sistema
- [ ] **PROC-08**: Q&A conversazionale GraphRAG ibrido con citazioni + UI Angular 'process' + visualizzazione grafo + annotazioni tribali + enum bilingui + Flyway

### Verticale Enterprise — Persone & competenze (PERS) — *Fase 21*

- [ ] **PERS-01**: Modello dati skill graph che estende EntityType/RelationType del dominio knowledge (Persona, Competenza, Ruolo, Team, Progetto, Certificazione, Evidenza, Cliente/Dominio, Obiettivo; archi POSSIEDE_COMPETENZA, RICHIEDE, ASPIRA_A, RICOPRE_RUOLO, RIPORTA_A, HA_LAVORATO_A, DIMOSTRATA_DA, MENTORE_DI, ENDORSED_BY)
- [ ] **PERS-02**: Import anagrafica + tassonomia (custom + ESCO)
- [ ] **PERS-03**: Estrazione skill dai documenti via EntityExtractorPort + Ollama locale
- [ ] **PERS-04**: Profilo skill inferito + provenance/peso (scala mastery Dreyfus 1-5)
- [ ] **PERS-05**: UI di validazione human-in-the-loop
- [ ] **PERS-06**: Find-the-expert GraphRAG
- [ ] **PERS-07**: Gap analysis base + sottoinsieme visualizzazione grafo
- [ ] **PERS-08**: Privacy & consenso base (base giuridica GDPR, opt-out granulare per sorgente)

### Verticale Enterprise — Clienti & fornitori / CRM-SRM (CRM) — *Fase 22*

- [ ] **CRM-01**: Modello grafo CRM/SRM su MySQL+Qdrant (Organizzazione, Persona, Sede, Contratto, Opportunita, Ordine, Fattura, Interazione, Prodotto/Servizio, Componente, AreaGeografica, CategoriaSpesa, RischioConcentrazione; archi HA_RUOLO, CONTROLLA, HA_REFERENTE, HA_CONTRATTO, HA_ORDINE, ACQUISTA, FORNISCE, LOCALIZZATO_IN, ESPONE_A, ALTERNATIVO_A)
- [ ] **CRM-02**: CSV/Excel + 1 connettore CRM
- [ ] **CRM-03**: Entity resolution base (chiavi forti P.IVA/VAT/DUNS + chiavi deboli, auto-merge + coda revisione umana)
- [ ] **CRM-04**: Ingestione interazioni email + sentiment locale
- [ ] **CRM-05**: Vista 360 + esplorazione grafo interattiva
- [ ] **CRM-06**: Pesi/indicatori base + Q&A GraphRAG con citazioni
- [ ] **CRM-07**: Alert scadenza contratti + bilinguismo IT/EN

### Verticale Enterprise — Mail & comunicazioni (MAIL) — *Fase 23*

- [ ] **MAIL-01**: Connettore IMAP con sync incrementale UID/UIDVALIDITY + credenziali cifrate
- [ ] **MAIL-02**: Estensione modello EmailMessage (Message-ID/In-Reply-To/References/Cc/allegati/lingua) + parsing MIME + de-quoting + rimozione firma + ricostruzione thread
- [ ] **MAIL-03**: Estrazione allegati via pipeline documenti (Tika/OCR a Qdrant)
- [ ] **MAIL-04**: Arricchimento AI locale (NER, riassunto messaggio/thread, classificazione) + entity resolution persona/org
- [ ] **MAIL-05**: Schema grafo comunicazioni su MySQL (Persona, Organizzazione, Casella, Messaggio, Thread, Tema, Allegato, Impegno, Decisione, Evento; archi HA_SCRITTO, DESTINATARIO_DI, RISPONDE_A, COMUNICA_CON, TRATTA, CONTIENE_IMPEGNO, HA_DECISO) + embedding Qdrant con filtri perimetro + pesi archi base
- [ ] **MAIL-06**: API REST gestione account IMAP / stato ingestione / query grafo + GraphRAG con risposte citate
- [ ] **MAIL-07**: UI config account / dossier persona-org / chat grafo + estrazione scadenze impegni con vista to-do mail
- [ ] **MAIL-08**: Controlli privacy base (perimetri di visibilità, policy LLM solo-Ollama)

### Verticale Enterprise — Ticketing & decisioni / ADR (TICK) — *Fase 24*

- [ ] **TICK-01**: Estensione dominio decisions/knowledge (Ticket, Bug, Feature, Epic, Task, Incident, Decisione(ADR), Alternativa, Requisito, ChangeRequest, Repository/Servizio, Commit/PR, Persona, Team, Commento, Chunk; archi causa, blocca, dipende_da, duplica, motiva, scarta_alternativa, supera/superato_da, contraddice, implementa, impatta, risolve, decisa_da)
- [ ] **TICK-02**: CRUD ADR strutturato (MADR) + import ADR Markdown
- [ ] **TICK-03**: Ingestione ticket Jira/GitHub
- [ ] **TICK-04**: Costruzione grafo da link espliciti + deduzione AI di link impliciti
- [ ] **TICK-05**: Revisione human-in-the-loop
- [ ] **TICK-06**: Visualizzazione grafo causale interattiva
- [ ] **TICK-07**: GraphRAG why/what-if con citazioni + linking automatico ADR-contesto
- [ ] **TICK-08**: i18n IT/EN

### Verticale Enterprise — Onboarding & formazione (ONB) — *Fase 25*

- [ ] **ONB-01**: Estensione dominio onboarding/knowledge (Persona, Team, Ruolo, Competenza, Concetto, Documento, Chunk, Procedura/Runbook, Processo, Sistema, Decisione(ADR), Percorso, Tappa/Modulo, Quiz, Evento, Glossario; archi e_prerequisito_di, richiede_competenza, documenta, e_responsabile_di, dipende_da, copre/insegna, verifica, mentor_di, fa_parte_del_processo, obsoleto_rispetto_a)
- [ ] **ONB-02**: CRUD struttura organizzativa + import CSV
- [ ] **ONB-03**: Ingestione conoscenza interna con permessi (Tika/Tesseract/Whisper + chunking + Qdrant + provenance/visibilità)
- [ ] **ONB-04**: Estrazione AI entità/competenze + deduzione archi + revisione grafo human-in-the-loop
- [ ] **ONB-05**: Visualizzazione grafo interattiva
- [ ] **ONB-06**: Percorsi onboarding role-based (template + istanza)
- [ ] **ONB-07**: Tutor GraphRAG con permessi e citazione fonti + quiz/self-check base
- [ ] **ONB-08**: Modello permessi/visibilità grafo + i18n IT/EN

### Verticale Enterprise — Compliance & audit (COMP) — *Fase 26*

- [ ] **COMP-01**: Modello grafo compliance (Framework, Requisito, Controllo, Evidenza, Asset, Processo, Rischio, Policy, Trattamento(GDPR), Persona/Ruolo, Fornitore, Audit, Finding, Incidente, RemediationTask, AuditTrailEntry; archi SODDISFA, DIMOSTRA, EQUIVALE_A, SOSTITUISCE, SI_APPLICA_A, MITIGA, MINACCIA, RESPONSABILE_DI, GOVERNA, VIOLA, RILEVA, CORREGGE, REGISTRA) su MySQL + archi pesati + Flyway
- [ ] **COMP-02**: CRUD framework/requisiti/controlli a /api/v1/compliance/*
- [ ] **COMP-03**: Ingestione normativa (PDF -> segmentazione -> embedding Qdrant)
- [ ] **COMP-04**: Mapping SODDISFA/EQUIVALE_A con AI human-in-the-loop
- [ ] **COMP-05**: Gestione evidenze con hash + freshness evidenza
- [ ] **COMP-06**: Coverage & gap analysis + Q&A compliance GraphRAG (Ollama default) + dashboard coverage
- [ ] **COMP-07**: Audit trail tamper-evident (hash-chain base)
- [ ] **COMP-08**: Template framework precaricati (ISO 27001:2022, SOC 2) + i18n IT/EN

## v2 Requirements

Differenziatori avanzati e capacità future, tracciati ma non nella roadmap corrente.

### AI Avanzata

- **RAGX-01**: Suggerimento di collegamenti mancanti tra nodi non connessi (link prediction con coda di curation; cfr. GRAG-05 intel)
- **RAGX-02**: Generazione di itinerari personalizzati via traversal del grafo consumer
- **RAGX-03**: Query relazionali cross-dominio in linguaggio naturale (enterprise)
- **RAGX-04**: Community detection gerarchica (Leiden) per grafi >10K nodi

### Community Avanzata

- **COMMX-01**: Trust score del contributore che pesa il valore delle valutazioni
- **COMMX-02**: Storico revisioni completo con diff/rollback dei nodi

> **Rimossi in v2 (promossi a v1):** ~~AMBIT-01~~ (verticali consumer aggiuntivi) e ~~AMBIT-02~~ (verticali enterprise aggiuntivi) sono stati **eliminati** e sostituiti dai gruppi granulari per dominio promossi a fasi v1 (Fasi 9-17 consumer, 19-26 enterprise; Fasi 8 e 18 sono i seed CONS-*/ENT-*).

## Out of Scope

Escluso esplicitamente, per prevenire scope creep (dalle anti-feature di ricerca).

| Feature | Reason |
|---------|--------|
| Database a grafo dedicato (Neo4j/ArangoDB) | MySQL CTE + Qdrant sufficienti sotto ~1M nodi; terza dipendenza non giustificata |
| Motore di reasoning OWL/SPARQL | Overhead proibitivo su hardware commodity; nessun utente reale del dominio lo usa |
| Editor di ontologie manuale (stile Protégé) | "Ontology hell"; schemi code-first via plugin PF4J |
| Editing collaborativo real-time (CRDT/OT) | Scope enorme per valore marginale in v1; last-write-wins con notifica conflitto |
| Rendering mappa geografica a tile | Competere con Google Maps non è fattibile; eventuale Leaflet+OSM solo per contesto |
| Funzioni social (follow/feed/DM) | Cambierebbe natura del prodotto e onere di moderazione |
| Gamification oltre badge minimi | Incentivi perversi; trust score come segnale di qualità |
| SaaS a pagamento / open-core | Escluso da PROJECT.md; open source puro |
| App mobile native | Web-first; PWA responsive su browser mobile |
| Ricerca full-text che sostituisce Qdrant | Duplicherebbe Qdrant; riuso via GraphNodeSearchPort |

## Traceability

Ogni requisito v1 mappa a esattamente una delle 27 fasi.

| Requirement | Phase | Status |
|-------------|-------|--------|
| GRAPH-01 | Phase 1 — Schema Foundation | Pending |
| GRAPH-02 | Phase 1 — Schema Foundation | Pending |
| GRAPH-03 | Phase 2 — Weighted Graph Core API | Pending |
| GRAPH-04 | Phase 2 — Weighted Graph Core API | Pending |
| GRAPH-05 | Phase 2 — Weighted Graph Core API | Pending |
| GRAPH-06 | Phase 2 — Weighted Graph Core API | Pending |
| GRAPH-07 | Phase 2 — Weighted Graph Core API | Pending |
| GRAPH-08 | Phase 2 — Weighted Graph Core API | Pending |
| SCHEMA-01 | Phase 4 — Plugin Domain Schemas & NodeTypeRegistry | Pending |
| SCHEMA-02 | Phase 4 — Plugin Domain Schemas & NodeTypeRegistry | Pending |
| SCHEMA-03 | Phase 4 — Plugin Domain Schemas & NodeTypeRegistry | Pending |
| SEARCH-01 | Phase 2 — Weighted Graph Core API | Pending |
| SEARCH-02 | Phase 3 — Graph Semantic Layer | Pending |
| SEARCH-03 | Phase 2 — Weighted Graph Core API | Pending |
| VIZ-01 | Phase 6 — Frontend Graph Visualization | Pending |
| VIZ-02 | Phase 6 — Frontend Graph Visualization | Pending |
| VIZ-03 | Phase 6 — Frontend Graph Visualization | Pending |
| VIZ-04 | Phase 6 — Frontend Graph Visualization | Pending |
| VIZ-05 | Phase 6 — Frontend Graph Visualization | Pending |
| VIZ-06 | Phase 6 — Frontend Graph Visualization | Pending |
| RAG-01 | Phase 5 — GraphRAG Service | Pending |
| RAG-02 | Phase 5 — GraphRAG Service | Pending |
| RAG-03 | Phase 5 — GraphRAG Service | Pending |
| RAG-04 | Phase 5 — GraphRAG Service | Pending |
| RAG-05 | Phase 7 — Frontend GraphRAG Chat | Pending |
| INGEST-01 | Phase 3 — Graph Semantic Layer | Pending |
| INGEST-02 | Phase 3 — Graph Semantic Layer | Pending |
| PRIV-01 | Phase 1 — Schema Foundation | Pending |
| PRIV-02 | Phase 5 — GraphRAG Service | Pending |
| PRIV-03 | Phase 2 — Weighted Graph Core API | Pending |
| CONS-01 | Phase 8 — Turismo & territorio | Pending |
| CONS-02 | Phase 8 — Turismo & territorio | Pending |
| CONS-03 | Phase 8 — Turismo & territorio | Pending |
| CONS-04 | Phase 8 — Turismo & territorio | Pending |
| CONS-05 | Phase 8 — Turismo & territorio | Pending |
| CONS-06 | Phase 8 — Turismo & territorio | Pending |
| EVT-01 | Phase 9 — Eventi & spettacoli | Pending |
| EVT-02 | Phase 9 — Eventi & spettacoli | Pending |
| EVT-03 | Phase 9 — Eventi & spettacoli | Pending |
| EVT-04 | Phase 9 — Eventi & spettacoli | Pending |
| EVT-05 | Phase 9 — Eventi & spettacoli | Pending |
| EVT-06 | Phase 9 — Eventi & spettacoli | Pending |
| EVT-07 | Phase 9 — Eventi & spettacoli | Pending |
| EVT-08 | Phase 9 — Eventi & spettacoli | Pending |
| EVT-09 | Phase 9 — Eventi & spettacoli | Pending |
| RIST-01 | Phase 10 — Ristorazione & locali | Pending |
| RIST-02 | Phase 10 — Ristorazione & locali | Pending |
| RIST-03 | Phase 10 — Ristorazione & locali | Pending |
| RIST-04 | Phase 10 — Ristorazione & locali | Pending |
| RIST-05 | Phase 10 — Ristorazione & locali | Pending |
| RIST-06 | Phase 10 — Ristorazione & locali | Pending |
| RIST-07 | Phase 10 — Ristorazione & locali | Pending |
| RIST-08 | Phase 10 — Ristorazione & locali | Pending |
| ITIN-01 | Phase 11 — Itinerari & esperienze | Pending |
| ITIN-02 | Phase 11 — Itinerari & esperienze | Pending |
| ITIN-03 | Phase 11 — Itinerari & esperienze | Pending |
| ITIN-04 | Phase 11 — Itinerari & esperienze | Pending |
| ITIN-05 | Phase 11 — Itinerari & esperienze | Pending |
| ITIN-06 | Phase 11 — Itinerari & esperienze | Pending |
| ITIN-07 | Phase 11 — Itinerari & esperienze | Pending |
| ITIN-08 | Phase 11 — Itinerari & esperienze | Pending |
| EDU-01 | Phase 12 — Education & studenti | Pending |
| EDU-02 | Phase 12 — Education & studenti | Pending |
| EDU-03 | Phase 12 — Education & studenti | Pending |
| EDU-04 | Phase 12 — Education & studenti | Pending |
| EDU-05 | Phase 12 — Education & studenti | Pending |
| EDU-06 | Phase 12 — Education & studenti | Pending |
| EDU-07 | Phase 12 — Education & studenti | Pending |
| EDU-08 | Phase 12 — Education & studenti | Pending |
| CULT-01 | Phase 13 — Cultura, arte & musei | Pending |
| CULT-02 | Phase 13 — Cultura, arte & musei | Pending |
| CULT-03 | Phase 13 — Cultura, arte & musei | Pending |
| CULT-04 | Phase 13 — Cultura, arte & musei | Pending |
| CULT-05 | Phase 13 — Cultura, arte & musei | Pending |
| CULT-06 | Phase 13 — Cultura, arte & musei | Pending |
| CULT-07 | Phase 13 — Cultura, arte & musei | Pending |
| CULT-08 | Phase 13 — Cultura, arte & musei | Pending |
| SPORT-01 | Phase 14 — Sport & outdoor | Pending |
| SPORT-02 | Phase 14 — Sport & outdoor | Pending |
| SPORT-03 | Phase 14 — Sport & outdoor | Pending |
| SPORT-04 | Phase 14 — Sport & outdoor | Pending |
| SPORT-05 | Phase 14 — Sport & outdoor | Pending |
| SPORT-06 | Phase 14 — Sport & outdoor | Pending |
| SPORT-07 | Phase 14 — Sport & outdoor | Pending |
| SPORT-08 | Phase 14 — Sport & outdoor | Pending |
| COMM-01 | Phase 15 — Commercio & shopping locale | Pending |
| COMM-02 | Phase 15 — Commercio & shopping locale | Pending |
| COMM-03 | Phase 15 — Commercio & shopping locale | Pending |
| COMM-04 | Phase 15 — Commercio & shopping locale | Pending |
| COMM-05 | Phase 15 — Commercio & shopping locale | Pending |
| COMM-06 | Phase 15 — Commercio & shopping locale | Pending |
| COMM-07 | Phase 15 — Commercio & shopping locale | Pending |
| RE-01 | Phase 16 — Real estate & immobiliare | Pending |
| RE-02 | Phase 16 — Real estate & immobiliare | Pending |
| RE-03 | Phase 16 — Real estate & immobiliare | Pending |
| RE-04 | Phase 16 — Real estate & immobiliare | Pending |
| RE-05 | Phase 16 — Real estate & immobiliare | Pending |
| RE-06 | Phase 16 — Real estate & immobiliare | Pending |
| RE-07 | Phase 16 — Real estate & immobiliare | Pending |
| RE-08 | Phase 16 — Real estate & immobiliare | Pending |
| SAN-01 | Phase 17 — Servizi & sanità locale | Pending |
| SAN-02 | Phase 17 — Servizi & sanità locale | Pending |
| SAN-03 | Phase 17 — Servizi & sanità locale | Pending |
| SAN-04 | Phase 17 — Servizi & sanità locale | Pending |
| SAN-05 | Phase 17 — Servizi & sanità locale | Pending |
| SAN-06 | Phase 17 — Servizi & sanità locale | Pending |
| SAN-07 | Phase 17 — Servizi & sanità locale | Pending |
| SAN-08 | Phase 17 — Servizi & sanità locale | Pending |
| ENT-01 | Phase 18 — Knowledge base aziendale | Pending |
| ENT-02 | Phase 18 — Knowledge base aziendale | Pending |
| ENT-03 | Phase 18 — Knowledge base aziendale | Pending |
| ENT-04 | Phase 18 — Knowledge base aziendale | Pending |
| ENT-05 | Phase 18 — Knowledge base aziendale | Pending |
| ARCH-01 | Phase 19 — Architettura software | Pending |
| ARCH-02 | Phase 19 — Architettura software | Pending |
| ARCH-03 | Phase 19 — Architettura software | Pending |
| ARCH-04 | Phase 19 — Architettura software | Pending |
| ARCH-05 | Phase 19 — Architettura software | Pending |
| ARCH-06 | Phase 19 — Architettura software | Pending |
| ARCH-07 | Phase 19 — Architettura software | Pending |
| ARCH-08 | Phase 19 — Architettura software | Pending |
| PROC-01 | Phase 20 — Processi & workflow | Pending |
| PROC-02 | Phase 20 — Processi & workflow | Pending |
| PROC-03 | Phase 20 — Processi & workflow | Pending |
| PROC-04 | Phase 20 — Processi & workflow | Pending |
| PROC-05 | Phase 20 — Processi & workflow | Pending |
| PROC-06 | Phase 20 — Processi & workflow | Pending |
| PROC-07 | Phase 20 — Processi & workflow | Pending |
| PROC-08 | Phase 20 — Processi & workflow | Pending |
| PERS-01 | Phase 21 — Persone & competenze | Pending |
| PERS-02 | Phase 21 — Persone & competenze | Pending |
| PERS-03 | Phase 21 — Persone & competenze | Pending |
| PERS-04 | Phase 21 — Persone & competenze | Pending |
| PERS-05 | Phase 21 — Persone & competenze | Pending |
| PERS-06 | Phase 21 — Persone & competenze | Pending |
| PERS-07 | Phase 21 — Persone & competenze | Pending |
| PERS-08 | Phase 21 — Persone & competenze | Pending |
| CRM-01 | Phase 22 — Clienti & fornitori / CRM-SRM | Pending |
| CRM-02 | Phase 22 — Clienti & fornitori / CRM-SRM | Pending |
| CRM-03 | Phase 22 — Clienti & fornitori / CRM-SRM | Pending |
| CRM-04 | Phase 22 — Clienti & fornitori / CRM-SRM | Pending |
| CRM-05 | Phase 22 — Clienti & fornitori / CRM-SRM | Pending |
| CRM-06 | Phase 22 — Clienti & fornitori / CRM-SRM | Pending |
| CRM-07 | Phase 22 — Clienti & fornitori / CRM-SRM | Pending |
| MAIL-01 | Phase 23 — Mail & comunicazioni | Pending |
| MAIL-02 | Phase 23 — Mail & comunicazioni | Pending |
| MAIL-03 | Phase 23 — Mail & comunicazioni | Pending |
| MAIL-04 | Phase 23 — Mail & comunicazioni | Pending |
| MAIL-05 | Phase 23 — Mail & comunicazioni | Pending |
| MAIL-06 | Phase 23 — Mail & comunicazioni | Pending |
| MAIL-07 | Phase 23 — Mail & comunicazioni | Pending |
| MAIL-08 | Phase 23 — Mail & comunicazioni | Pending |
| TICK-01 | Phase 24 — Ticketing & decisioni / ADR | Pending |
| TICK-02 | Phase 24 — Ticketing & decisioni / ADR | Pending |
| TICK-03 | Phase 24 — Ticketing & decisioni / ADR | Pending |
| TICK-04 | Phase 24 — Ticketing & decisioni / ADR | Pending |
| TICK-05 | Phase 24 — Ticketing & decisioni / ADR | Pending |
| TICK-06 | Phase 24 — Ticketing & decisioni / ADR | Pending |
| TICK-07 | Phase 24 — Ticketing & decisioni / ADR | Pending |
| TICK-08 | Phase 24 — Ticketing & decisioni / ADR | Pending |
| ONB-01 | Phase 25 — Onboarding & formazione | Pending |
| ONB-02 | Phase 25 — Onboarding & formazione | Pending |
| ONB-03 | Phase 25 — Onboarding & formazione | Pending |
| ONB-04 | Phase 25 — Onboarding & formazione | Pending |
| ONB-05 | Phase 25 — Onboarding & formazione | Pending |
| ONB-06 | Phase 25 — Onboarding & formazione | Pending |
| ONB-07 | Phase 25 — Onboarding & formazione | Pending |
| ONB-08 | Phase 25 — Onboarding & formazione | Pending |
| COMP-01 | Phase 26 — Compliance & audit | Pending |
| COMP-02 | Phase 26 — Compliance & audit | Pending |
| COMP-03 | Phase 26 — Compliance & audit | Pending |
| COMP-04 | Phase 26 — Compliance & audit | Pending |
| COMP-05 | Phase 26 — Compliance & audit | Pending |
| COMP-06 | Phase 26 — Compliance & audit | Pending |
| COMP-07 | Phase 26 — Compliance & audit | Pending |
| COMP-08 | Phase 26 — Compliance & audit | Pending |
| DOCS-01 | Phase 27 — Documentation & i18n | Pending |
| DOCS-02 | Phase 27 — Documentation & i18n | Pending |
| DOCS-03 | Phase 27 — Documentation & i18n | Pending |

**Coverage:**
- v1 requirements: 179 total
- Mapped to phases: 179 (100%) ✓
- Unmapped: 0 ✓

Breakdown: engine spine 33 (GRAPH 8 + SCHEMA 3 + SEARCH 3 + VIZ 6 + RAG 5 + INGEST 2 + PRIV 3 + DOCS 3) + consumer 78 (CONS 6 seed + EVT 9 + RIST 8 + ITIN 8 + EDU 8 + CULT 8 + SPORT 8 + COMM 7 + RE 8 + SAN 8) + enterprise 68 (ENT 5 seed + ARCH 8 + PROC 8 + PERS 8 + CRM 7 + MAIL 8 + TICK 8 + ONB 8 + COMP 8) = 179. The six cross-cutting ingest groups (KGCORE/GRAG/VIZX/MOD/INGX/SEC) are folded as enrichment notes on the engine families, not counted as separate requirements.

---
*Requirements defined: 2026-06-29*
*Last updated: 2026-06-30 — merge-restructure-from-ingest: AMBIT-01/AMBIT-02 removed; CONS-*/ENT-* folded into seed Phases 8/18; 17 verticals promoted to v1; 27-phase traceability rebuilt by roadmapper*
