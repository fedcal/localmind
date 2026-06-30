# LocalMind

## What This Is

LocalMind è una piattaforma open-source, local-first, che evolve da gestore di documenti / ricerca semantica / chat multi-provider LLM verso un **motore di Knowledge Graph universale**: un grafo pesato e interattivo dove ogni nodo è un'informazione (luoghi, documenti, processi, servizi, persone, mail, FAQ…) e gli archi descrivono relazioni pesate, navigabili dall'AI. Un unico motore serve due ecosistemi — **consumer** (scoperta del territorio: turismo, eventi, esperienze, "Wikipedia dei luoghi" community-driven) e **enterprise** (conoscenza interna: documentazione, processi, repository, microservizi, API, persone) — cambiando solo tipi di nodo, relazioni e moduli installati.

## Core Value

L'AI deve poter **navigare un grafo pesato di conoscenza per rispondere a domande complesse e far emergere collegamenti non evidenti** — in qualsiasi dominio, restando local-first. Se tutto il resto fallisce, questo deve funzionare.

## Requirements

### Validated

<!-- Inferiti dal codebase esistente (brownfield) — già spediti e funzionanti. -->

- ✓ Chat LLM multi-provider con fallback chain (OLLAMA → OPENAI → ANTHROPIC → GOOGLE → DeepSeek/Mistral/XAI) — existing
- ✓ Streaming chat via SSE — existing
- ✓ Ingestione documenti (upload + folder watcher batch) con estrazione testo (Tika) e OCR (Tesseract) — existing
- ✓ Chunking, embedding e ricerca semantica su Qdrant (vector store) — existing
- ✓ Persistenza relazionale su MySQL 8.0 con migrazioni Flyway (V1–V78) — existing
- ✓ Gestione provider LLM da UI con chiavi salvate in DB (`llm_provider_configs`) — existing
- ✓ Integrazione MCP (server + client + esecuzione tool) — existing
- ✓ Autenticazione local-first (filtro token JWT-like, sessioni stateless) — existing
- ✓ Dominio `knowledge` già presente nel layer domain — existing (base da estendere verso il grafo)
- ✓ Ingestione email (IMAP/SMTP via Angus Mail) e integrazione calendario — existing (in evoluzione)
- ✓ Fine-tuning, automazione, messaging, agent come domini dedicati — existing (in evoluzione)
- ✓ Sistema di plugin estensibile (PF4J) con extension points (LLM provider, document parser, vector store) — existing
- ✓ Marketplace di estensioni/moduli — existing
- ✓ UI Angular 21 feature-driven, standalone components, Signal store, multilingua IT/EN — existing
- ✓ Architettura esagonale (Ports & Adapters) Maven multi-module, dominio puro senza Spring — existing

### Active

<!-- Visione di questo ciclo. Ipotesi finché non spedite e validate. La roadmap le suddivide in fasi. -->

**Motore Knowledge Graph (core, domain-agnostic)**
- [ ] Modello dati a grafo: nodi tipizzati + archi (relazioni) con peso, riusando MySQL (struttura) + Qdrant (semantica)
- [ ] Peso delle relazioni derivato da fattori configurabili (frequenza d'uso, rilevanza, dipendenze, feedback utenti)
- [ ] API per creare/leggere/aggiornare/eliminare nodi e relazioni
- [ ] Query sul grafo: percorsi, vicini, sottografi, ricerca per relazioni
- [ ] Tipi di nodo e relazione estendibili per dominio (schema modulare)

**AI sul grafo (GraphRAG)**
- [ ] L'AI esplora il grafo per rispondere a domande complesse combinando relazioni + semantica
- [ ] Suggerimento di collegamenti mancanti / non evidenti tra nodi
- [ ] Risposte con citazione dei nodi/percorsi usati

**Visualizzazione e navigazione**
- [ ] Visualizzazione interattiva del grafo pesato (nodi, archi, peso)
- [ ] Navigazione esplorativa per relazioni (espansione progressiva dal nodo)
- [ ] Filtri per tipo di nodo/relazione e dominio

**Verticale Consumer (territorio/turismo)**
- [ ] Tipi di nodo: luoghi, POI, ristoranti/locali, eventi, itinerari, esperienze
- [ ] Contributi community: creazione nodi, recensioni, valutazioni
- [ ] Ranking emergente: i contenuti migliori emergono da contributi e voti
- [ ] Itinerari personalizzati generati dall'AI sul grafo
- [ ] Moderazione/curatela dei contributi

**Verticale Enterprise (conoscenza interna)**
- [ ] Tipi di nodo: documenti, procedure, processi, repository Git, microservizi, API, database, infrastruttura, clienti, fornitori, competenze, persone, workflow, ticket, decisioni architetturali, FAQ
- [ ] Ingestione automatica da fonti (doc, mail, repo, API) → nodi/archi
- [ ] Mappatura dipendenze e individuazione collegamenti tra elementi
- [ ] Privacy: dati aziendali mai inviati a servizi esterni senza consenso

**Piattaforma & estensibilità**
- [ ] Moduli/pacchetti per dominio installabili (consumer, enterprise, nuovi settori: eventi, education, ecc.)
- [ ] Documentazione open-source dettagliata e bilingue (IT/EN) per utenti e contributor
- [ ] Self-hosting completo (local-first) documentato

### Out of Scope

<!-- Confini espliciti con motivazione, per evitare di reintrodurli. -->

- Modelli di pagamento / SaaS hosted a pagamento — il progetto è **open source puro**; nessun open-core o paywall in questo ciclo
- Sostituzione dell'architettura esistente — si **estende** l'app attuale, non si riscrive
- Dipendenza obbligatoria da AI cloud — l'AI locale (Ollama) resta il default; il cloud è opzionale
- App mobile native — fuori scope finché il web non è maturo
- Database a grafo dedicato (es. Neo4j) — escluso in questo ciclo: si riusa MySQL + Qdrant (rivalutabile in futuro se le query lo richiedono)

## Context

- **Brownfield maturo:** l'app esiste già con domini `llm`, `document`, `mcp`, `auth`, `automation`, `messaging`, `calendar`, `email`, `knowledge`, `finetuning`, `marketplace`, `common`, `plugin`, `agent`. La mappa è in `.planning/codebase/`.
- **Stack:** Spring Boot 3.4.2, Spring AI 1.0.0, Java 17, Maven multi-module (hexagonal); Angular 21 (standalone, Signals); MySQL 8.0 + Qdrant; Flyway V1–V78; provider LLM Ollama/OpenAI/Anthropic/DeepSeek/Mistral/XAI/Google.
- **Estensibilità già presente:** plugin PF4J con extension points e marketplace — base ideale per i moduli di dominio.
- **Dominio `knowledge` esistente:** punto di partenza per il motore a grafo.
- **Lavoro in corso non committato:** modifiche a `CalendarController`, `EmailController`, `FineTuningController` e alla pagina frontend di backup (ingestione mail/documentazione lato enterprise).
- **Vincoli di progetto (CLAUDE.md):** documentazione IT+EN, enum tradotte IT/EN verso il frontend, ogni migrazione Flyway con una sola query, file piccoli e coesi, immutabilità, sviluppi tracciati in cartella "Sviluppi" con nomenclatura datata.
- **Concerns noti:** vedi `.planning/codebase/CONCERNS.md` (es. violazioni di boundary tra domini in `MODULE_BOUNDARIES.md`, mapping UUID MySQL fragile).

## Constraints

- **Architettura**: Estendere l'app esistente mantenendo l'architettura esagonale (dominio puro, wiring via `DomainConfig`) — coerenza e riuso
- **Tech stack**: Riuso di MySQL 8.0 (struttura grafo) + Qdrant (semantica) — niente nuovo datastore a grafo in questo ciclo
- **Local-first**: Tutto deve poter girare interamente in locale / self-hosted on-premise — privacy e controllo dati
- **AI locale di default**: Ollama come provider predefinito; provider cloud opzionali — sovranità sui dati
- **Privacy enterprise**: Dati aziendali mai inviati a servizi esterni senza consenso esplicito — requisito enterprise
- **i18n**: UI, documentazione ed enum bilingui IT/EN — requisito di progetto
- **Licenza/modello**: Open source puro, documentazione dettagliata per contributor — natura community del progetto
- **Persistenza**: Migrazioni Flyway con una sola query per file; UUID con `@JdbcTypeCode(SqlTypes.CHAR)` — vincoli MySQL/Hibernate documentati

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Estendere l'app esistente invece di riscrivere | Codebase brownfield maturo, architettura esagonale solida e riusabile | — Pending |
| Un unico motore a grafo, domini come moduli | "Una piattaforma, due (e più) ecosistemi"; massimizza riuso ed estensibilità | — Pending |
| Riuso MySQL + Qdrant per il grafo (no Neo4j ora) | Evitare nuova infrastruttura; sfruttare stack e competenze esistenti | — Pending |
| Open source puro, nessun paywall | Crescita guidata dalla community | — Pending |
| Local-first con AI Ollama di default | Privacy, self-hosting, sovranità sui dati | — Pending |
| GraphRAG come approccio AI sul grafo | Combina relazioni del grafo + ricerca semantica esistente | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd:complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-06-29 after initialization*
