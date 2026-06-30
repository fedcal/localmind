# ADR-0001: Datastore del Knowledge Graph — MySQL + Qdrant (no Neo4j)

- **Status:** Accepted (locked)
- **Date:** 2026-06-30
- **Deciders:** Federico (maintainer LocalMind)
- **Supersedes:** `documentazione/11-roadmap/02-evoluzione-futura.md` §2.7 (proposta storica di Neo4j per il Knowledge Graph, target v2.1.0, feb 2026)

---

## 🇮🇹 Italiano

### Contesto

LocalMind evolve verso un motore di Knowledge Graph universale (grafo pesato e interattivo + GraphRAG). Un documento di roadmap storico (`documentazione/11-roadmap/02-evoluzione-futura.md`, §2.7) proponeva **Neo4j Community** come database a grafo. Questa proposta precede la formalizzazione della strategia attuale e contraddice le fonti di pianificazione autorevoli (`.planning/PROJECT.md`, `.planning/ROADMAP.md`, `.planning/research/SUMMARY.md`), che adottano MySQL + Qdrant ed escludono esplicitamente Neo4j.

La ricerca di progetto ha confermato che, sotto ~1M di nodi, MySQL 8.0 con liste di adiacenza e `WITH RECURSIVE` CTE è adeguato, e che il dominio `knowledge` esistente (`KnowledgeEntity`, `KnowledgeRelation`, `KnowledgeGraphService`, `KnowledgeGraphPort`) va **esteso**, non sostituito.

### Decisione

Il Knowledge Graph di LocalMind usa, per questo ciclo (milestone Universal Knowledge Graph):

- **MySQL 8.0** per la struttura del grafo: nodi e archi tipizzati come liste di adiacenza, traversal via `WITH RECURSIVE` CTE con guardia anti-ciclo (MySQL non ha clausola `CYCLE`).
- **Qdrant** per la semantica: collezione dedicata `localmind_graph_nodes` per gli embedding dei nodi, separata dalla collezione `localmind` dei chunk documentali.
- **Nessun database a grafo dedicato (Neo4j/ArangoDB)** come dipendenza infrastrutturale.

Algoritmi complessi (Dijkstra, PageRank) restano in servizi Java puri nel layer dominio, alimentati via JDBC, preservando il confine esagonale.

### Conseguenze

- **Positive:** nessuna terza infrastruttura; riuso di stack e competenze esistenti; coerenza local-first/self-hosted; estensione del dominio `knowledge` già presente.
- **Negative / rischi:** le CTE ricorsive MySQL richiedono guardia anti-ciclo manuale e profiling delle prestazioni; oltre una certa scala (>100K nodi) potrebbe servire una closure/materialized path table.
- **Rivalutazione:** la scelta è bloccata per questo ciclo. Un eventuale passaggio a un graph DB dedicato richiederà una nuova ADR motivata da evidenze di prestazioni reali.

---

## 🇬🇧 English

### Context

LocalMind is evolving into a universal Knowledge Graph engine (weighted interactive graph + GraphRAG). A historical roadmap document (`documentazione/11-roadmap/02-evoluzione-futura.md`, §2.7) proposed **Neo4j Community** as the graph database. That proposal predates the current strategy and contradicts the authoritative planning sources (`.planning/PROJECT.md`, `.planning/ROADMAP.md`, `.planning/research/SUMMARY.md`), which adopt MySQL + Qdrant and explicitly exclude Neo4j.

Project research confirmed that, below ~1M nodes, MySQL 8.0 with adjacency lists and `WITH RECURSIVE` CTEs is adequate, and that the existing `knowledge` domain (`KnowledgeEntity`, `KnowledgeRelation`, `KnowledgeGraphService`, `KnowledgeGraphPort`) should be **extended**, not replaced.

### Decision

For this cycle (Universal Knowledge Graph milestone), LocalMind's Knowledge Graph uses:

- **MySQL 8.0** for graph structure: typed nodes and edges as adjacency lists, traversal via `WITH RECURSIVE` CTEs with a manual cycle guard (MySQL has no `CYCLE` clause).
- **Qdrant** for semantics: a dedicated `localmind_graph_nodes` collection for node embeddings, separate from the `localmind` document-chunk collection.
- **No dedicated graph database (Neo4j/ArangoDB)** as an infrastructure dependency.

Complex algorithms (Dijkstra, PageRank) stay in pure-Java domain services fed via JDBC, preserving the hexagonal boundary.

### Consequences

- **Positive:** no third infrastructure component; reuse of existing stack and skills; local-first/self-hosted consistency; extends the existing `knowledge` domain.
- **Negative / risks:** MySQL recursive CTEs require a manual cycle guard and performance profiling; beyond a certain scale (>100K nodes) a closure/materialized path table may be needed.
- **Re-evaluation:** the choice is locked for this cycle. Switching to a dedicated graph DB will require a new ADR justified by real performance evidence.

---

> Note (related, WARNING #2 from ingest): document-format support is delegated to Apache Tika via `TikaTextExtractor.parseToString()` (no hardcoded whitelist; `DocumentController` does not restrict content type). The "9 formats" list (PDF, DOCX, XLSX, PPTX, TXT, MD, CSV, JSON, HTML) is closer to reality than the older "4 formats" spec. Confirm acceptance rules when implementing INGEST-01.
