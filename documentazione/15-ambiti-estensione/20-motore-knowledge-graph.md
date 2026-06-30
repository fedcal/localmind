# Motore Knowledge Graph

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo documento descrive il **cuore tecnologico** di LocalMind: il **Motore di Knowledge Graph universale e domain-agnostic**. Non è un verticale (turismo, eventi, enterprise…) ma il *core* su cui ogni verticale viene istanziato. L'obiettivo è trasformare LocalMind da gestore di documenti + ricerca semantica + chat multi-provider in un **grafo pesato e interattivo di conoscenza**, dove ogni nodo è un'informazione tipizzata e ogni arco è una relazione pesata, navigabile dall'AI tramite GraphRAG. Tutto deve restare **local-first**, self-hostable, con AI Ollama di default, **riusando MySQL 8.0 (struttura del grafo) e Qdrant (semantica)** — senza introdurre Neo4j o altri database a grafo dedicati. Il motore è la fondazione condivisa: cambiano solo i tipi di nodo, i tipi di relazione e i moduli installati, mentre il core di persistenza, query, pesatura e navigazione AI resta uno solo.

Il punto di partenza non è greenfield: nel dominio `knowledge` esistono già `KnowledgeEntity`, `KnowledgeRelation`, `EntityType`, `RelationType`, `KnowledgeSubgraph`, il `KnowledgeGraphUseCase`, il `KnowledgeGraphPort`, l'adapter `JdbcKnowledgeGraphAdapter`, l'estrattore `LlmEntityExtractorAdapter` e le tabelle Flyway `knowledge_entities` (V66) e `knowledge_relations` (V67). Questo documento parte da quella base, ne evidenzia i limiti (in primis l'**assenza del peso sugli archi** e la rigidità degli enum di tipo) e traccia l'evoluzione verso un motore completo, modulare e pesato.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema di fondo

Le piattaforme di conoscenza attuali — sia consumer (mappe, recensioni) sia enterprise (wiki, drive, ticketing) — soffrono tutte dello stesso limite strutturale: **trattano l'informazione come documenti isolati e indicizzati per parole chiave o per vicinanza semantica, ma non come una rete di relazioni esplicite e pesate**. Le conseguenze pratiche sono pesanti e ricorrenti in ogni dominio:

- **La conoscenza vive in silos scollegati.** Un documento, una mail, un microservizio, un luogo, una persona esistono ciascuno nel proprio sistema; le connessioni tra loro (chi dipende da cosa, cosa è collegato a cosa, perché) sono nella testa delle persone o sparse in PDF e thread, mai materializzate in una struttura interrogabile.
- **La ricerca semantica pura (RAG classico) non basta.** Il RAG vettoriale recupera i chunk *più simili* a una domanda, ma è cieco rispetto alle **relazioni multi-hop**: non sa rispondere a *"quali microservizi sarebbero impattati se cambio questa API, e chi sono i loro owner?"* o *"quali esperienze sono abbinabili a questo borgo raggiungibile senza autostrada e collegate a un evento questo weekend?"*. Queste domande richiedono di **percorrere archi**, non solo di confrontare embedding. È il limite ben documentato del RAG "flat" che GraphRAG nasce per superare.
- **Le connessioni non evidenti restano invisibili.** Il valore più alto della conoscenza sta nei collegamenti che nessuno ha esplicitato: due processi che condividono una dipendenza fragile, due luoghi accomunati dallo stesso produttore, due ticket sintomi della stessa causa radice. Senza un grafo, questi pattern non emergono mai.
- **Le risposte AI non sono spiegabili né tracciabili.** Un LLM che risponde "a memoria" o da un RAG flat non può citare *quale percorso di fatti* ha usato. In ambito enterprise (privacy, audit, compliance) e consumer (fiducia, trasparenza del ranking) questa opacità è inaccettabile.
- **Ogni dominio reinventa la ruota.** Costruire un grafo turistico, uno enterprise, uno educational come tre prodotti separati significa triplicare persistenza, query, pesatura e logica AI. Serve **un solo motore** parametrizzabile per dominio.

### 1.2 La soluzione: un motore a grafo pesato e domain-agnostic

LocalMind risponde con un **motore di Knowledge Graph universale** che modella qualsiasi informazione come **nodo tipizzato** e qualsiasi connessione come **arco pesato e tipizzato**. Su questa struttura l'AI naviga (GraphRAG) combinando due segnali complementari:

1. **Segnale strutturale** — le relazioni esplicite del grafo (percorsi, vicini, sottografi, dipendenze), persistite in MySQL.
2. **Segnale semantico** — la similarità vettoriale degli embedding di nodi e descrizioni, persistita in Qdrant.

La fusione dei due segnali (hybrid retrieval) è ciò che distingue un motore GraphRAG maturo da un RAG flat: si parte da un *seed* trovato per similarità semantica, si **espande lungo gli archi pesati** per raccogliere il contesto relazionale rilevante, e si fornisce all'LLM un sottografo coeso e citabile invece di un sacco di chunk scollegati.

Il **peso degli archi** è l'elemento differenziante centrale richiesto dalla visione di progetto. Il peso non è un numero arbitrario: codifica **intensità, qualità, affidabilità e attualità** di una relazione, derivato da fattori configurabili (frequenza d'uso, rilevanza/co-occorrenza, forza della dipendenza, feedback degli utenti, recency, confidenza dell'estrazione AI). Il peso guida la navigazione (l'AI esplora prima gli archi più forti), il ranking (i collegamenti più rilevanti emergono), la visualizzazione (archi spessi = relazioni forti) e il pruning del contesto (si tagliano gli archi deboli per restare nel budget di token).

### 1.3 Perché un solo motore per due (e più) ecosistemi

| Dimensione | RAG flat attuale | Motore Knowledge Graph LocalMind |
|---|---|---|
| Unità di conoscenza | Chunk di testo | Nodo tipizzato + arco pesato + chunk |
| Tipo di query | "Cosa è simile a X?" | "Cosa è collegato a X, come, e quanto fortemente?" (percorsi, vicini, sottografi) |
| Relazioni multi-hop | Non supportate | Native (traversal pesato a profondità configurabile) |
| Spiegabilità | Bassa (chunk scollegati) | Alta (nodi + percorsi citati nella risposta) |
| Connessioni non evidenti | Invisibili | Emergenti (link prediction, community detection) |
| Riuso cross-dominio | Per-prodotto | Un core, N moduli di dominio (schema modulare) |
| Infrastruttura | Solo Qdrant | MySQL (struttura) + Qdrant (semantica), già presenti |

Il motore è quindi **l'asse portante** del Core Value dichiarato in `.planning/PROJECT.md`: *"l'AI deve poter navigare un grafo pesato di conoscenza per rispondere a domande complesse e far emergere collegamenti non evidenti — in qualsiasi dominio, restando local-first"*. Se tutto il resto fallisce, questo deve funzionare.

### 1.4 Valore concreto per tipo di adozione

- **Consumer (turismo, eventi, cultura, sport…):** scoperta relazionale, itinerari generati dall'AI sul grafo, ranking emergente e trasparente, sovranità del dato per comuni e pro loco.
- **Enterprise (doc, processi, repo, microservizi, API, persone, mail):** mappa viva delle dipendenze, impact analysis, onboarding accelerato, risposte AI tracciabili e auditabili, privacy garantita dal local-first.
- **Sviluppatori/integratori:** un'unica API di grafo + plugin PF4J per definire nuovi tipi di nodo/relazione e connettori, senza toccare il core.

### 1.5 Stato attuale e gap da colmare (baseline brownfield)

Il dominio `knowledge` fornisce già una base, ma incompleta rispetto alla visione. I gap principali che questo motore deve colmare:

| Aspetto | Stato attuale (baseline) | Gap / evoluzione necessaria |
|---|---|---|
| Peso degli archi | **Assente** — `KnowledgeRelation` ha solo `id, from, to, type, properties` | Aggiungere `weight` (e fattori component) come cittadino di prima classe del modello, dello schema e delle query |
| Tipi di nodo/relazione | Enum hard-coded (`EntityType`: 7 valori; `RelationType`: 8 valori) | Schema **modulare** estendibile per dominio senza ricompilare il core |
| Operazioni di query | Solo `searchEntities`, `getEntitySubgraph(depth)` | Aggiungere **percorsi** (shortest/weighted path), **vicini** (k-hop pesati), **sottografi filtrati**, ricerca per relazione |
| CRUD nodi/relazioni | Solo `indexText` (estrazione AI) e `deleteEntity` | API CRUD complete e idempotenti per nodi e archi (creazione manuale + da connettori) |
| Persistenza semantica | Entità in MySQL; embedding non collegati ai nodi | Collegare i nodi a vettori Qdrant per il seed semantico del GraphRAG |
| Navigazione AI | `indexText` alimenta il grafo, ma il grafo non è ancora interrogato dall'AI | Strumento/tool GraphRAG che l'LLM usa per esplorare e citare |
| Visualizzazione | Assente lato frontend | Vista interattiva del grafo pesato (Angular) |

---

## 2. Personas & utenti target

| Persona | Profilo | Obiettivi rispetto al motore | Bisogni dal sistema |
|---|---|---|---|
| **Sviluppatore di dominio** | Costruisce un verticale (turismo, enterprise…) sopra il motore | Definire tipi di nodo/relazione, pesi, connettori | API grafo stabile, schema modulare, plugin PF4J, SDK, docs IT/EN |
| **Data/Knowledge engineer** | Cura la qualità del grafo | Ingestione, deduplica, merge, validazione dei pesi | Pipeline di ingestione, strumenti di entity resolution, audit |
| **Utente finale (consumer)** | Esploratore di un verticale consumer | Ottenere risposte e percorsi navigabili | Risposte GraphRAG con citazioni, navigazione visuale |
| **Knowledge worker (enterprise)** | Dipendente che cerca conoscenza interna | Capire dipendenze, owner, impatti | Impact analysis, ricerca relazionale, privacy garantita |
| **Amministratore / DevOps** | Gestisce l'istanza self-hosted | Performance, backup, tuning dei pesi | Configurazione pesi, metriche, limiti di profondità, MySQL+Qdrant |
| **AI / agente LLM** | Consumatore programmatico del grafo | Esplorare il grafo come tool durante il reasoning | Tool/funzioni di traversal, budget di token, formati citabili |
| **Moderatore / curatore** | Valida contributi e relazioni (consumer) o approva merge (enterprise) | Confermare/correggere archi e pesi | Code di revisione, feedback che alimenta il peso |

Persona primaria del motore (core): lo **Sviluppatore di dominio** e l'**AI/agente LLM**. Il motore è infrastruttura: il suo successo si misura su quanto è facile costruirci sopra un verticale e quanto bene l'AI lo sa navigare.

---

## 3. Requisiti in input

Questa sezione definisce in dettaglio **cosa serve in ingresso** al motore per funzionare: dati, configurazioni, contratti e vincoli. È deliberatamente esaustiva perché è il contratto da cui dipende ogni sviluppo successivo.

### 3.1 Input di dominio (definizione dello schema modulare)

Il motore è domain-agnostic: prima di ingerire dati, un dominio deve **dichiarare il proprio schema**. Questo è l'input fondante.

| Input | Descrizione | Forma attesa | Obbligatorio |
|---|---|---|---|
| **Catalogo tipi di nodo** | Insieme dei `NodeType` del dominio (es. `PLACE`, `MICROSERVICE`) con etichetta IT/EN | Definizione registrata (DB o plugin), non più enum hard-coded | Sì |
| **Catalogo tipi di relazione** | Insieme dei `RelationType` con etichetta IT/EN, direzionalità, e *coppie di tipi ammesse* (from→to) | Definizione registrata con vincoli di dominio/codominio | Sì |
| **Schema delle proprietà** | Per ogni tipo di nodo/arco, le proprietà attese (chiave, tipo, validazione) | Schema leggero (JSON Schema-like) su `properties` | Consigliato |
| **Politica di pesatura** | Quali fattori pesano gli archi e con quali coefficienti per quel dominio | Configurazione `WeightPolicy` (vedi §5) | Sì (con default) |
| **Mappatura embedding** | Quali campi del nodo concorrono all'embedding semantico | Configurazione di ingestione | Consigliato |

Vincolo: i cataloghi devono essere **bilingui IT/EN** (etichette tradotte e reindirizzate al frontend in base allo switch lingua, come da regola di progetto). I tipi non devono richiedere ricompilazione del core: vanno gestiti come dati o come estensioni PF4J.

### 3.2 Input di contenuto (alimentazione del grafo)

Sono i dati grezzi o strutturati da cui nascono nodi e archi.

| Sorgente | Esempi | Modalità di ingestione | Note |
|---|---|---|---|
| **Testo non strutturato** | Documenti (Tika/OCR già presenti), note, mail | Estrazione entità+relazioni via LLM (`LlmEntityExtractorAdapter` esistente) | Confidenza dell'estrazione → fattore di peso |
| **Dati strutturati** | CSV/JSON, open data (OSM, Wikidata), export da DB | Connettori dedicati che mappano record → nodi/archi | Deterministico, alta confidenza |
| **API esterne** | Repo Git, registri microservizi, ticketing, calendari | Connettori/plugin con polling o webhook | Enterprise; rispetto privacy |
| **Contributi manuali** | Utenti/curatori che creano nodi e relazioni da UI | API CRUD | Attribuzione + reputazione → feedback di peso |
| **Eventi di sistema** | Eventi di dominio LocalMind (conversazioni, ingestione doc) | `DomainEventPublisherPort` → listener che crea archi | Frequenza d'uso → fattore di peso |

Per ogni elemento ingerito sono richiesti almeno: **identità della sorgente** (`sourceDocumentId`/`sourceConnectorId`), **timestamp**, e — ove applicabile — **confidenza**. Questi metadati alimentano deduplica, audit e pesatura.

### 3.3 Input per la creazione/aggiornamento di un nodo (contratto API)

Per creare o aggiornare un nodo il chiamante deve fornire:

- `type` (NodeType valido nel catalogo del dominio) — **obbligatorio**, validato.
- `name` / label canonica — **obbligatorio**, usato per deduplica e display (max 200 char, coerente con lo schema attuale `VARCHAR(200)`).
- `properties` — mappa chiave/valore conforme allo schema del tipo (validata ai confini del sistema). Esempi: coordinate per `PLACE`, repository URL per `MICROSERVICE`.
- `sourceDocumentId` / origine — per tracciabilità.
- *(opzionale)* campi per l'embedding (testo descrittivo) usati per indicizzare il nodo su Qdrant.
- *(opzionale)* `externalId` — chiave naturale per upsert idempotente dai connettori.

Validazioni minime: tipo esistente nel catalogo, nome non vuoto, proprietà conformi, lunghezze rispettate, nessun input fidato senza validazione (regola di sicurezza di progetto).

### 3.4 Input per la creazione/aggiornamento di una relazione (contratto API)

- `fromEntityId`, `toEntityId` — **obbligatori**, devono riferire nodi esistenti (vincolo FK già presente in V67).
- `type` (RelationType valido) — **obbligatorio**; la coppia (tipoFrom, tipoTo) deve rispettare i vincoli di dominio/codominio dichiarati.
- `weight` e/o **fattori di peso** (vedi §5) — se non forniti, calcolati dalla `WeightPolicy` del dominio. **Gap rispetto alla baseline: il campo peso oggi non esiste e va introdotto.**
- `properties` — metadati (es. confidenza, sorgente, recency).
- `directed` — se la relazione è orientata o simmetrica (default per tipo).

Validazioni: nodi esistenti, tipo ammesso per la coppia, peso normalizzato in `[0,1]`, no auto-loop salvo tipi che lo consentono, idempotenza su (from, to, type) per evitare archi duplicati.

### 3.5 Input per le query sul grafo

| Query | Input richiesti | Default/vincoli |
|---|---|---|
| **Vicini (neighbors)** | `nodeId`, `depth` (k-hop), filtri (tipi nodo/arco), `minWeight`, `limit` | `depth` con cap (es. ≤3) per costo; `minWeight` per pruning |
| **Percorso (path)** | `fromId`, `toId`, strategia (shortest / max-weight), filtri di tipo | Limite di hop; pesi come costo/utilità |
| **Sottografo (subgraph)** | `seedNodeId(s)`, `depth`, filtri, `minWeight`, `maxNodes` | Cap su nodi/archi per protezione memoria e UI |
| **Ricerca per relazione** | `relationType`, filtri sui tipi di nodo, ordinamento per peso | Paginazione |
| **GraphRAG query** | domanda in linguaggio naturale, dominio, budget token, profondità | Combina seed semantico (Qdrant) + espansione pesata |

### 3.6 Input di configurazione e ambiente

- **Profilo LLM** per l'estrazione e per GraphRAG: provider (Ollama default), modello, temperatura — riusando la fallback chain `LlmGatewayService`.
- **Connessione Qdrant** e collezione dei nodi (riuso `QdrantVectorStoreAdapter`).
- **Datasource MySQL** e migrazioni Flyway (una query per file — regola di progetto).
- **Limiti operativi:** profondità massima di traversal, dimensione massima del sottografo, timeout query, budget di token GraphRAG.
- **WeightPolicy** per dominio (coefficienti, normalizzazione, decadimento temporale).
- **Lingua** (IT/EN) per etichette e risposte.

### 3.7 Vincoli e pre-condizioni (non funzionali)

- **Local-first / self-hostable:** nessun input deve richiedere servizi cloud obbligatori; l'AI di estrazione e GraphRAG deve girare con Ollama.
- **Privacy enterprise:** i dati ingeriti non lasciano l'istanza senza consenso esplicito; i connettori cloud sono opt-in.
- **Architettura esagonale:** gli input attraversano `port/in`; nessuna logica di framework nel dominio.
- **Riuso MySQL+Qdrant:** nessun nuovo datastore a grafo.
- **i18n:** ogni enum/tipo esposto al frontend è tradotto IT/EN.

---

## 4. Flusso dell'attività (step-by-step)

Il motore ha tre flussi macro: **(A) alimentazione del grafo (ingestione)**, **(B) interrogazione strutturale del grafo**, **(C) navigazione AI / GraphRAG**. Sono descritti nel dettaglio perché costituiscono il comportamento osservabile del sistema.

### 4.1 Flusso A — Alimentazione del grafo (ingestione → nodi/archi pesati)

```
Sorgente → Estrazione → Risoluzione identità → Costruzione nodi/archi →
Pesatura → Persistenza (MySQL + Qdrant) → Eventi/indicizzazione
```

1. **Acquisizione input.** Un documento/record/contributo arriva tramite un connettore o l'API CRUD. Il sistema valida l'input ai confini (tipo, proprietà, sorgente) e registra origine + timestamp.
2. **Estrazione di entità e relazioni.**
   - Per testo non strutturato: `LlmEntityExtractorAdapter` (Ollama default) estrae candidati nodi e archi con un **punteggio di confidenza** per ciascuno.
   - Per dati strutturati: il connettore mappa deterministicamente i campi a nodi/archi (confidenza = 1.0).
3. **Risoluzione dell'identità (entity resolution / deduplica).** Ogni candidato nodo viene confrontato con i nodi esistenti per nome canonico + tipo + similarità semantica (Qdrant) + `externalId`. Se esiste un match sopra soglia → **merge** (aggiorna proprietà, somma evidenze); altrimenti → **nuovo nodo**. Questo evita l'esplosione di duplicati, problema tipico dei knowledge graph alimentati da LLM.
4. **Costruzione degli archi.** Per ogni relazione candidata si verificano i vincoli di dominio/codominio (la coppia di tipi è ammessa?). Gli archi duplicati su (from, to, type) vengono consolidati invece di ricreati.
5. **Calcolo del peso.** Si applica la `WeightPolicy` del dominio combinando i fattori disponibili (confidenza estrazione, co-occorrenza, recency, eventuale feedback pregresso) in un peso normalizzato `[0,1]`. Se l'arco esisteva già, il peso viene **aggiornato in modo incrementale** (es. rinforzo per nuova evidenza, decadimento temporale).
6. **Persistenza duale.**
   - **MySQL:** upsert idempotente su `knowledge_entities` e `knowledge_relations` (con la nuova colonna `weight` e i fattori). Operazione transazionale.
   - **Qdrant:** upsert dell'embedding del nodo (testo descrittivo) con payload `nodeId, type, domain` per il seed semantico futuro.
7. **Eventi e side-effect.** Pubblicazione di un evento di dominio (`KnowledgeGraphUpdatedEvent`) via `DomainEventPublisherPort`; i listener possono aggiornare statistiche, code di moderazione (consumer) o trigger di impact analysis (enterprise).
8. **Tracciamento.** Ogni nodo/arco mantiene il legame con la sorgente per audit e per il calcolo del peso "frequenza d'uso" nel tempo.

**Gestione errori:** estrazione fallita → log + skip del singolo elemento senza abortire il batch; violazione FK/vincolo di tipo → rifiuto con messaggio chiaro; conflitto di merge → strategia configurabile (auto vs coda di revisione). Nessun errore viene silenziosamente ingoiato (regola di progetto).

### 4.2 Flusso B — Interrogazione strutturale del grafo

```
Richiesta query → Validazione/limiti → Traversal pesato su MySQL →
Pruning (minWeight, cap) → Assemblaggio sottografo → Risposta tipizzata
```

1. **Richiesta.** Il client chiama `port/in` con uno dei pattern: vicini, percorso, sottografo, ricerca per relazione (vedi §3.5).
2. **Validazione e applicazione limiti.** Profondità entro il cap, `maxNodes` rispettato, filtri di tipo validi, `minWeight` applicato.
3. **Esecuzione traversal.** Su MySQL, partendo dal/dai nodo/i seed, si espandono gli archi rispettando filtri e `minWeight`, ordinando per peso decrescente. Per i percorsi si applica shortest-path o max-weight-path entro il limite di hop. Le query usano gli indici esistenti (`idx_kr_from`, `idx_kr_to`) e indici aggiuntivi su `(relation_type, weight)`.
4. **Pruning e protezione.** Gli archi sotto soglia di peso vengono scartati; il sottografo è limitato in nodi/archi per proteggere memoria e UI.
5. **Assemblaggio.** Si costruisce un `KnowledgeSubgraph` (nodi + archi + pesi) tipizzato e immutabile (pattern immutabilità di progetto).
6. **Risposta.** Restituzione via DTO bilingue (etichette IT/EN dei tipi). Il frontend può renderizzare la vista interattiva.

### 4.3 Flusso C — Navigazione AI / GraphRAG (il flusso di punta)

```
Domanda NL → Seed semantico (Qdrant) → Espansione pesata (MySQL) →
Selezione contesto entro budget → Prompt con sottografo citabile →
LLM (Ollama) → Risposta + citazioni dei nodi/percorsi
```

1. **Domanda in linguaggio naturale.** L'utente (o un agente) pone una domanda complessa in un dominio.
2. **Identificazione dei seed (semantico).** La domanda viene embeddata e cercata su Qdrant → top-N nodi semanticamente più vicini (ancore di partenza). Questo collega il GraphRAG alla ricerca semantica esistente.
3. **Espansione relazionale (strutturale).** Dai nodi seed si esegue un traversal pesato (Flusso B) per raccogliere il sottografo rilevante: vicini forti, percorsi tra seed, dipendenze. Il **peso guida l'ordine di esplorazione** e il pruning.
4. **Selezione del contesto entro budget.** Il sottografo viene "appiattito" in un contesto testuale ordinato per rilevanza (peso + similarità), tagliato per rispettare il budget di token del modello locale. Si privilegiano nodi/archi ad alto peso e percorsi che connettono più seed.
5. **Costruzione del prompt.** Si fornisce all'LLM il sottografo come contesto strutturato e citabile (nodi con id, archi con tipo e peso), con istruzione esplicita di **citare i nodi/percorsi usati**.
6. **Generazione.** Chiamata a `LlmGatewayService` (Ollama default, fallback cloud opt-in) per produrre la risposta.
7. **Risposta con tracciabilità.** L'output include la risposta + l'elenco dei nodi e dei percorsi citati (spiegabilità). Lato frontend, i nodi citati sono cliccabili e aprono la vista grafo.
8. **Feedback loop.** L'eventuale feedback dell'utente (utile/non utile, conferma di una relazione) alimenta il fattore "feedback" del peso degli archi coinvolti, migliorando le risposte future.

### 4.4 Flusso trasversale — Suggerimento di collegamenti non evidenti (link prediction)

Periodicamente (batch) o on-demand, il motore analizza il grafo per **proporre archi mancanti**: nodi con molti vicini in comune, alta similarità semantica ma nessun arco diretto, pattern strutturali ricorrenti. I suggerimenti entrano in una coda di revisione (curatore/moderatore) o, sopra una soglia di confidenza alta, vengono creati con peso iniziale basso e rinforzati dal feedback. È la funzione che realizza la promessa "far emergere collegamenti non evidenti".

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

### 5.1 Principio: schema core + cataloghi di dominio

Il core definisce le **primitive** (Nodo, Arco, Peso, Sottografo) e un **catalogo minimo trasversale**; ogni dominio aggiunge i propri tipi tramite catalogo registrato o plugin PF4J. Oggi `EntityType` e `RelationType` sono enum hard-coded: l'evoluzione è renderli **estendibili** (registry di tipi + etichette IT/EN) mantenendo retrocompatibilità con i valori esistenti.

### 5.2 Tipi di nodo core (trasversali) — baseline e estensioni

| NodeType (core) | Descrizione | Stato |
|---|---|---|
| `DOCUMENT` | Documento/risorsa testuale | Esistente |
| `PERSON` | Persona | Esistente |
| `ORGANIZATION` | Organizzazione/ente | Esistente |
| `PLACE` | Luogo geografico | Esistente |
| `CONCEPT` | Concetto/tema/tag | Esistente |
| `EVENT` | Evento temporale | Esistente |
| `TECHNOLOGY` | Tecnologia/strumento | Esistente |

Esempi di estensioni **per dominio** (registrate dai moduli, non nel core):

| Dominio | Tipi di nodo aggiuntivi |
|---|---|
| Consumer / turismo | `POI`, `RESTAURANT`, `EXPERIENCE`, `ITINERARY`, `REVIEW` |
| Enterprise | `MICROSERVICE`, `API`, `REPOSITORY`, `DATABASE`, `PROCESS`, `PROCEDURE`, `TICKET`, `DECISION`, `FAQ`, `SKILL` |
| Education | `COURSE`, `TOPIC`, `LESSON`, `STUDENT` |

### 5.3 Tipi di relazione core — baseline e estensioni

| RelationType (core) | Semantica | Direzionata | Stato |
|---|---|---|---|
| `RELATED_TO` | Collegamento generico/tematico | No | Esistente |
| `PART_OF` | Composizione/appartenenza | Sì | Esistente |
| `LOCATED_IN` | Localizzazione | Sì | Esistente |
| `WORKS_AT` | Affiliazione persona→org | Sì | Esistente |
| `CREATED_BY` | Autoria | Sì | Esistente |
| `DEPENDS_ON` | Dipendenza | Sì | Esistente |
| `REFERENCES` | Riferimento/citazione | Sì | Esistente |
| `MENTIONED_IN` | Menzione in documento | Sì | Esistente |

Estensioni per dominio: consumer (`NEAR`, `PAIRED_WITH`, `STAGE_OF`, `SAME_PRODUCER`), enterprise (`CALLS`, `OWNED_BY`, `DEPLOYS_TO`, `CAUSED_BY`, `SUPERSEDES`, `DOCUMENTS`).

### 5.4 Il peso degli archi — modello e criteri

Il peso `w ∈ [0,1]` è una **combinazione normalizzata di fattori**, ciascuno con un coefficiente configurabile per dominio (`WeightPolicy`). I fattori previsti dalla visione di progetto:

| Fattore | Cosa misura | Come si calcola | Esempio d'uso |
|---|---|---|---|
| **Frequenza d'uso** | Quanto spesso la relazione è percorsa/co-occorre | Conteggio normalizzato di traversal/co-occorrenze nel tempo | Archi molto usati salgono di peso |
| **Rilevanza / co-occorrenza** | Forza dell'associazione tra i nodi | Co-occorrenza nei documenti, similarità semantica dei nodi (Qdrant) | Due concetti spesso citati insieme |
| **Forza della dipendenza** | Criticità della relazione strutturale | Esplicita dal connettore (es. dipendenza hard vs soft) | `DEPENDS_ON` critico vs opzionale |
| **Feedback utenti** | Validazione umana della relazione | Voti/conferme dei curatori/utenti, reputazione del contributore | Archi confermati rinforzati |
| **Confidenza estrazione** | Affidabilità dell'origine AI | Score dell'LLM o 1.0 per dati strutturati | Estrazioni incerte pesano meno |
| **Recency / decadimento** | Attualità della relazione | Decadimento temporale (half-life configurabile) | Relazioni vecchie si attenuano |

Formula di riferimento (lineare normalizzata): `w = clamp01( Σ (coeff_i · fattore_i) )`, con i coefficienti definiti nella `WeightPolicy` del dominio e i fattori normalizzati in `[0,1]`. I singoli fattori vanno **persistiti** (non solo il peso finale) per ricalcolare il peso quando cambiano i coefficienti o per spiegarlo nella UI ("perché questo arco pesa 0,82").

**Implicazioni di schema (gap da colmare):** la tabella `knowledge_relations` va estesa con almeno `weight DOUBLE` e i fattori component (o un blob JSON di fattori), più indice su `(relation_type, weight)` e su `from_entity_id, weight`. Coerente con la regola Flyway "una query per file": una migrazione per `ADD COLUMN weight`, una per ciascun indice/fattore.

### 5.5 Rappresentazione fisica (MySQL + Qdrant)

- **MySQL** = struttura del grafo: `knowledge_entities` (nodi), `knowledge_relations` (archi pesati). I traversal sono query SQL ricorsive/iterative con join sugli indici from/to. La parola `recursive` è riservata: va escapata con backtick nelle DDL/CTE (gotcha di progetto).
- **Qdrant** = semantica: un vettore per nodo (embedding del testo descrittivo) con payload `{nodeId, type, domain}`, usato per il seed semantico del GraphRAG e per l'entity resolution.
- **Mapping UUID:** tutti gli `@Id` UUID richiedono `@JdbcTypeCode(SqlTypes.CHAR)` (CHAR(36)), come già fatto in `KnowledgeRelationEntity`.

### 5.6 Diagramma concettuale

```
                 (semantica)                       (struttura)
        ┌───────────────────────┐        ┌────────────────────────────┐
        │        Qdrant         │        │           MySQL            │
        │  vettore per nodo     │        │  knowledge_entities (nodi) │
        │  payload: type,domain │◄──────►│  knowledge_relations       │
        └───────────────────────┘  link  │  (archi + weight + fattori)│
                  ▲   nodeId             └────────────────────────────┘
                  │                                   ▲
            seed semantico                     traversal pesato
                  │                                   │
                  └──────────► GraphRAG ◄─────────────┘
                       (Ollama default, citazioni)
```

---

## 6. Fonti dati & connettori (ingestione)

L'alimentazione del grafo riusa e estende l'infrastruttura di ingestione esistente. I connettori sono il punto naturale di estensione via plugin PF4J.

| Fonte | Connettore | Riuso esistente | Output nel grafo |
|---|---|---|---|
| Documenti (PDF, Office, immagini) | Pipeline doc + Tika/Tesseract + `LlmEntityExtractorAdapter` | `DocumentIngestionPipelineService`, batch folder scan | Nodi `DOCUMENT`/entità estratte + archi `MENTIONED_IN`/`REFERENCES` |
| Email (IMAP) | Connettore email | Dominio `email` (Angus Mail) | Nodi `PERSON`/`ORGANIZATION`/`DOCUMENT` + archi `CREATED_BY`, `REFERENCES` |
| Calendario | Connettore calendar | Dominio `calendar` | Nodi `EVENT` + archi `PART_OF`, `LOCATED_IN` |
| Repository Git / microservizi / API | Nuovo plugin enterprise | PF4J extension point | Nodi `REPOSITORY`/`MICROSERVICE`/`API` + archi `DEPENDS_ON`, `CALLS`, `OWNED_BY` |
| Open data (OSM, Wikidata) | Nuovo connettore consumer | — | Nodi `PLACE`/`POI` + archi `NEAR`, `LOCATED_IN` |
| Contributi manuali | API CRUD + UI | Frontend Angular | Qualsiasi nodo/arco, con attribuzione |
| Eventi di sistema | Listener su `DomainEventPublisherPort` | Event infrastructure esistente | Archi derivati + aggiornamento "frequenza d'uso" |
| MCP / tool esterni | Connettore MCP | Dominio `mcp` | Nodi/archi da tool federati |

Principi: ogni connettore è **idempotente** (upsert per `externalId`), **tracciabile** (origine + timestamp), e **opt-in per le fonti cloud** (privacy enterprise). I connettori si registrano come adapter di un `port/out` di ingestione, mantenendo il dominio puro.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

Legenda: **[C]** creare ex-novo, **[E]** estendere/evolvere l'esistente, **[M]** mantenere/hardening.

### 7.1 MVP del motore (fondamenta indispensabili)

| # | Funzionalità | Tipo | Note di implementazione |
|---|---|---|---|
| 1 | **Peso sugli archi** | [E] | Aggiungere `weight` (+ fattori) a `KnowledgeRelation`, `KnowledgeRelationEntity`, migrazioni Flyway (una query per file), mapper |
| 2 | **CRUD nodi e relazioni** | [E] | Estendere `KnowledgeGraphUseCase`/`KnowledgeGraphPort` con create/update/get/delete idempotenti e validati |
| 3 | **WeightPolicy configurabile** | [C] | Servizio di calcolo peso (fattori + coefficienti per dominio), nel dominio, wired in `DomainConfig` |
| 4 | **Query: vicini k-hop pesati** | [E] | Traversal su MySQL con `minWeight`, filtri di tipo, cap di profondità |
| 5 | **Query: percorso (shortest / max-weight)** | [C] | Path tra due nodi entro limite di hop |
| 6 | **Query: sottografo filtrato** | [E] | Evolvere `getEntitySubgraph` con filtri, `minWeight`, `maxNodes` |
| 7 | **Indicizzazione semantica dei nodi su Qdrant** | [E] | Collegare nodi a vettori per seed GraphRAG ed entity resolution |
| 8 | **Entity resolution / deduplica** | [C] | Merge per nome+tipo+similarità+externalId in fase di ingestione |
| 9 | **GraphRAG base** | [C] | Seed semantico → espansione pesata → contesto entro budget → risposta con citazioni (Ollama default) |
| 10 | **REST API + DTO bilingui** | [E] | `KnowledgeController` con endpoint CRUD/query sotto `/api/v1/knowledge`, enum tradotti IT/EN |
| 11 | **Schema modulare tipi (registry)** | [C] | Sostituire enum hard-coded con registry estendibile + cataloghi IT/EN, retrocompatibile |
| 12 | **Migrazioni Flyway** | [C] | `weight`, fattori, indici `(relation_type, weight)`; una query per file |

### 7.2 Evoluzione (post-MVP)

| # | Funzionalità | Tipo | Valore |
|---|---|---|---|
| 13 | **Visualizzazione interattiva del grafo (Angular)** | [C] | Nodi/archi/peso, espansione progressiva, filtri per tipo/dominio, nodi citati cliccabili |
| 14 | **Link prediction (collegamenti non evidenti)** | [C] | Suggerimenti di archi mancanti → coda di revisione/feedback |
| 15 | **Community detection** | [C] | Clustering del grafo (es. Leiden/Louvain) per riassunti tematici stile GraphRAG "global search" |
| 16 | **GraphRAG come tool dell'agente** | [E] | Esporre il traversal come tool LLM/MCP per il dominio `agent` |
| 17 | **Decadimento temporale dei pesi (batch)** | [C] | Job schedulato che applica recency/half-life |
| 18 | **Pesatura da feedback utenti** | [E] | Loop di feedback consumer/enterprise → fattore peso |
| 19 | **Connettori di dominio (repo/API/open data)** | [C] | Plugin PF4J per enterprise e consumer |
| 20 | **Versioning / audit del grafo** | [C] | Storico modifiche nodi/archi per compliance enterprise |
| 21 | **Moduli di dominio installabili** | [E] | Pacchetti consumer/enterprise via marketplace |
| 22 | **Ottimizzazione query / caching** | [M] | Caching sottografi caldi (Spring Cache/Caffeine già presente), indici |

### 7.3 Mantenimento continuo

- **[M]** Qualità del grafo: monitoraggio duplicati, archi orfani, pesi anomali; job di consistenza MySQL↔Qdrant.
- **[M]** Performance dei traversal al crescere del grafo (cap, indici, eventuale denormalizzazione di vicinato).
- **[M]** Coerenza dei cataloghi tipi IT/EN al variare dei moduli.
- **[M]** Test: unit (dominio puro), integrazione (Testcontainers MySQL), copertura ≥80% come da regole di progetto.
- **[M]** Documentazione IT/EN aggiornata ad ogni sviluppo + log in `Sviluppi/`.

---

## 8. Casi d'uso AI / GraphRAG

| Caso d'uso | Dominio | Come il grafo viene navigato | Output |
|---|---|---|---|
| **Impact analysis** | Enterprise | Da un `API`/`MICROSERVICE`, traversal `DEPENDS_ON`/`CALLS` k-hop pesato | "Se cambi X, impatti Y, Z; owner: …" con percorso citato |
| **Onboarding / domanda complessa** | Enterprise | Seed semantico su doc/processi + espansione `PART_OF`/`DOCUMENTS` | Risposta sintetica con nodi e procedure citati |
| **Root cause** | Enterprise | Traversal `CAUSED_BY` su ticket/incidenti | Catena causale probabile, pesata |
| **Itinerario su misura** | Consumer | Seed `PLACE` + espansione `NEAR`/`PAIRED_WITH`/`STAGE_OF` con vincoli | Itinerario come percorso nel grafo, spiegato |
| **Scoperta di gemme nascoste** | Consumer | Ranking emergente da peso + community, non popolarità | Suggerimenti trasparenti e citabili |
| **Collegamenti non evidenti** | Trasversale | Link prediction su vicini comuni + similarità | Proposte di nuovi archi |
| **Riassunto tematico globale** | Trasversale | Community detection + summary per cluster | Panoramica di un'area del grafo |
| **Agente con tool grafo** | Trasversale | L'LLM invoca traversal come tool durante il reasoning | Risposte multi-step tracciabili |

Tutti i casi d'uso producono risposte **con citazione dei nodi/percorsi**, girano con **Ollama di default** e rispettano i limiti di budget/token e profondità.

---

## 9. KPI & metriche di successo

| Categoria | KPI | Obiettivo indicativo |
|---|---|---|
| **Copertura del grafo** | N. nodi/archi, % nodi con ≥1 arco, % archi con peso calcolato | Crescita costante; <5% nodi isolati |
| **Qualità** | Tasso di duplicati post-resolution, archi orfani, % archi confermati da feedback | Duplicati <2%, zero orfani |
| **Qualità AI (GraphRAG)** | % risposte con citazioni valide, correttezza su set di valutazione, multi-hop recall vs RAG flat | Miglioramento misurabile vs baseline RAG |
| **Spiegabilità** | % risposte che citano nodi/percorsi realmente usati | ~100% |
| **Performance** | Latenza traversal (k-hop), latenza GraphRAG end-to-end, p95 | Traversal <300ms a depth 2 su grafo medio |
| **Adozione sviluppatori** | Tempo per istanziare un nuovo dominio, n. tipi/connettori custom | Nuovo dominio in giorni, non settimane |
| **Local-first** | % funzioni operative con sola Ollama (no cloud) | 100% delle funzioni core |
| **Privacy** | N. dati inviati a cloud senza consenso | 0 |

### 9.1 Strumentazione

Riuso di Actuator + Micrometer/Prometheus (già presenti) per metriche di latenza, dimensioni del grafo, hit/miss della cache sottografi, e tassi di errore di ingestione.

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---|---|---|
| **Traversal costosi su MySQL al crescere del grafo** | Latenza, carico DB | Cap di profondità/nodi, `minWeight` pruning, indici mirati, caching sottografi caldi, eventuale tabella di vicinato denormalizzata |
| **Esplosione di duplicati da estrazione LLM** | Grafo rumoroso, query inaffidabili | Entity resolution robusta (nome+tipo+similarità+externalId), soglie, coda di revisione |
| **Pesi arbitrari o non spiegabili** | Perdita di fiducia, ranking scadente | Persistere i fattori, formula trasparente, UI che spiega il peso, feedback loop |
| **Allucinazioni GraphRAG / citazioni inventate** | Risposte non affidabili | Fornire solo sottografo reale come contesto, validare le citazioni contro i nodi forniti |
| **Assenza nativa di operazioni a grafo in MySQL (no Neo4j)** | Complessità query | CTE ricorsive limitate + traversal applicativo iterativo; rivalutare grafo dedicato solo se i KPI lo impongono (Out of Scope attuale) |
| **Schema rigido (enum hard-coded)** | Blocca i nuovi domini | Migrazione a registry di tipi estendibile + plugin PF4J, mantenendo retrocompatibilità |
| **Incoerenza MySQL↔Qdrant** | Seed semantico errato | Job di riconciliazione, scrittura transazionale + outbox per gli upsert vettoriali |
| **Privacy enterprise** | Fuga di dati | Connettori cloud opt-in, AI locale default, nessun invio senza consenso |
| **Costo di calcolo embedding/estrazione in locale** | Lentezza su hardware modesto | Batch, modelli Ollama leggeri, calcolo asincrono, caching |
| **Migrazioni Flyway monolitiche** | Violazione regola di progetto | Una query per file, sempre |

---

## 11. Manutenzione & evoluzione

- **Hardening incrementale:** dopo l'MVP, prioritizzare performance dei traversal (indici, caching, denormalizzazione del vicinato) e qualità del grafo (job di consistenza, deduplica continua).
- **Evoluzione dello schema:** ogni nuovo dominio aggiunge tipi/connettori senza toccare il core; le migrazioni restano additive e con una sola query per file. I valori enum esistenti vanno preservati per retrocompatibilità durante il passaggio al registry.
- **Governance dei pesi:** rivedere periodicamente i coefficienti delle `WeightPolicy` sulla base delle metriche di qualità delle risposte; il decadimento temporale va calibrato per dominio.
- **Coerenza dei due store:** monitorare e riconciliare MySQL↔Qdrant; trattare Qdrant come indice derivato ricostruibile dal MySQL (fonte di verità della struttura).
- **Estensibilità via plugin:** i connettori e i tipi di dominio vivono come estensioni PF4J e moduli marketplace, testati in isolamento.
- **Test e copertura:** unit sul dominio puro, integrazione con Testcontainers MySQL, valutazione periodica della qualità GraphRAG con set di domande di riferimento; copertura ≥80%.
- **Documentazione:** aggiornamento costante IT/EN ad ogni sviluppo e log datato in `Sviluppi/` (regole di progetto). Le enum/tipi esposti restano bilingui verso il frontend.
- **Rivalutazione architetturale:** la scelta "no Neo4j" è valida ora; va riaperta solo se i KPI di latenza/scalabilità dei traversal lo richiedono, come previsto in `PROJECT.md`.

---

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo / dominio | Ruolo rispetto al motore | Integrazione concreta |
|---|---|---|
| **`knowledge`** | È il motore stesso (base brownfield) | Estendere modelli, port, service, adapter (`JdbcKnowledgeGraphAdapter`, `LlmEntityExtractorAdapter`) |
| **`llm` / `LlmGatewayService`** | Estrazione entità e generazione GraphRAG | Ollama default + fallback chain; nessuna logica LLM nel dominio knowledge |
| **`document`** | Sorgente primaria di nodi/archi | Pipeline Tika/OCR + estrazione → grafo; archi `MENTIONED_IN` |
| **Qdrant (`vectorstore`)** | Semantica del grafo | Embedding per nodo, seed GraphRAG, entity resolution (`QdrantVectorStoreAdapter`, `EmbeddingConfig` @Primary Ollama) |
| **MySQL + Flyway** | Struttura del grafo | Tabelle `knowledge_entities`/`knowledge_relations` estese con peso; migrazioni additive |
| **`email` / `calendar`** | Connettori di ingestione | Eventi, persone, organizzazioni → nodi/archi |
| **`mcp`** | Tool esterni e federazione | GraphRAG esposto come tool MCP; ingestione da tool |
| **`agent`** | Consumatore del grafo | L'agente usa il traversal come tool nel reasoning |
| **`automation` / event infra** | Aggiornamento incrementale | Listener su `DomainEventPublisherPort` → archi derivati + frequenza d'uso |
| **`plugin` (PF4J) / `marketplace`** | Estensibilità | Connettori e tipi di dominio come plugin/moduli installabili |
| **`auth`** | Sicurezza e privacy | Accesso al grafo dietro `LocalAuthFilter`; isolamento dati enterprise |
| **`common`** | Eventi, eccezioni, analytics | `KnowledgeGraphUpdatedEvent`, eccezioni tipizzate, statistiche |
| **Frontend Angular** | Visualizzazione e navigazione | Feature `knowledge` con vista grafo interattiva, Signals, i18n IT/EN |
| **`finetuning`** | Miglioramento estrazione | Dataset dal grafo per affinare i modelli di estrazione locali |

**Wiring architetturale:** tutti i nuovi servizi di dominio restano puri (zero Spring) e vengono registrati come `@Bean` in `DomainConfig.java`; gli adapter (MySQL, Qdrant, connettori) sono `@Component` che implementano i `port/out`; i controller espongono solo i `port/in`. Questo preserva l'architettura esagonale e abilita la futura estrazione a microservizio documentata in `MODULE_BOUNDARIES.md`.

---

*Documento di indirizzo per gli sviluppi del Motore Knowledge Graph (core, domain-agnostic). Da mantenere allineato a `.planning/PROJECT.md` e ai file in `.planning/codebase/`.*
