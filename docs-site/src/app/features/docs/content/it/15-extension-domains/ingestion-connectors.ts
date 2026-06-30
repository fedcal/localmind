export const content = `# Ingestione & connettori

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo documento descrive l'ambito **Ingestione & connettori** (gruppo: *core*): l'insieme di pipeline, adattatori e connettori che trasformano qualsiasi **fonte** (documenti, mail, repository Git, API esterne, cartelle monitorate, calendari, canali di messaggistica, database) in **nodi e archi pesati** del Knowledge Graph universale di LocalMind. È il *cuore operativo* che alimenta il motore a grafo descritto in \`20-motore-knowledge-graph-it.md\`: senza un'ingestione robusta, incrementale e tracciabile, il grafo resta vuoto o, peggio, si riempie di duplicati e rumore. L'ambito è trasversale a tutti i verticali (consumer ed enterprise): cambia solo *quali* connettori si installano e *quali* tipi di nodo/relazione vengono prodotti, ma l'ossatura della pipeline — estrazione, normalizzazione, chunking, embedding, estrazione di entità/relazioni, deduplica, upsert nel grafo, sincronizzazione incrementale — è una sola.

Il punto di partenza **non è greenfield**. LocalMind possiede già una pipeline di ingestione documenti matura (\`DocumentIngestionPipelineService\`), un layer batch Spring (\`localmind-batch\` con \`DocumentIngestionJobConfig\`, \`FolderScanJobConfig\`, \`BatchScheduler\`), connettori di fatto verso il file system (\`LocalFileSystemScanner\` / \`FileSystemScannerPort\`), estrazione testo (Apache Tika via \`TikaTextExtractor\`) e OCR (Tesseract via \`TesseractOcrExtractor\`), chunking (\`ChunkingService\`), embedding ed indicizzazione vettoriale su Qdrant (\`QdrantVectorStoreAdapter\`), oltre a domini di sorgente già attivi: \`email\` (IMAP/SMTP via Angus Mail), \`calendar\`, \`messaging\` (canali in/out), \`mcp\`. Esiste inoltre l'estrattore di entità AI (\`EntityExtractorPort\` / \`LlmEntityExtractorAdapter\`) e il \`KnowledgeGraphPort\`. Questo ambito **riusa e generalizza** tale base: trasforma la pipeline "documento → chunk → vettore" in una pipeline "fonte → nodi+archi pesati → grafo + vettore", introducendo un'**astrazione di connettore** uniforme e la **sincronizzazione incrementale**.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema di fondo

Un Knowledge Graph vale quanto i dati che lo popolano. Il collo di bottiglia reale di ogni piattaforma GraphRAG non è l'algoritmo di traversal: è **portare dentro la conoscenza dalle fonti dove vive realmente** — e mantenerla aggiornata nel tempo senza intervento manuale. Oggi LocalMind sa ingerire bene *un tipo* di fonte (file/documenti), ma il salto verso il "motore universale" richiede di risolvere problemi strutturali ben precisi:

- **Le fonti sono eterogenee e disperse.** La conoscenza enterprise sta in Drive/SharePoint, in mailbox IMAP, in repository Git, in Confluence/Notion/wiki, in database applicativi, in OpenAPI/Swagger di microservizi, in ticketing (Jira/GitHub Issues), in calendari. Quella consumer sta in CSV/Excel di pro loco, in feed eventi (ICS, RSS), in OpenStreetMap/POI, in form di contributo community. Ogni fonte ha protocollo, autenticazione, formato e semantica diversi. Senza un'astrazione di connettore comune, ogni integrazione diventa un progetto a sé.

- **L'ingestione "one-shot" non basta: serve l'incrementale.** Caricare una volta i documenti è facile; il problema vero è **mantenere il grafo allineato** quando le fonti cambiano. Reindicizzare tutto a ogni ciclo è costoso (CPU, embedding, chiamate LLM per l'estrazione entità) e produce grafi instabili. Lo stato dell'arte 2026 (Airbyte, Cognee, connettori CDC) ha reso *incrementalità, cursori/watermark e checkpoint* funzionalità di prima classe: si sincronizza **solo ciò che è nuovo o modificato**, con ripresa dopo errori transitori senza ripartire da zero.

- **I duplicati e le entità ambigue degradano il grafo.** La stessa persona ("Mario Rossi", "M. Rossi", "mario.rossi@…"), lo stesso luogo, lo stesso microservizio compaiono in fonti diverse. Senza **deduplica e entity resolution**, il grafo si frammenta in cloni scollegati e i pesi degli archi si diluiscono. È il problema di *entity ambiguity / relation noise* segnalato dalla letteratura GraphRAG come principale causa di bassa qualità.

- **L'estrazione AI è costosa e va governata.** Estrarre entità e relazioni con un LLM su *tutto* il corpus a ogni esecuzione è insostenibile in locale (Ollama). Serve estrazione **solo sul delta**, con caching, batching, e modalità "sequenziale per tipo" (prima persone, poi luoghi, poi organizzazioni…) che la ricerca 2026 indica come più accurata della joint extraction.

- **Manca tracciabilità e provenienza.** In ambito enterprise (audit, compliance, privacy) ogni nodo/arco deve poter rispondere a *"da quale fonte, quale documento, quale riga/offset, quando, con quale confidenza proviene questa informazione?"*. Senza **lineage** la risposta GraphRAG non è auditabile e non si può fare *right-to-be-forgotten* (cancellare a cascata tutto ciò che derivava da una fonte rimossa).

- **Privacy e local-first sono vincoli, non opzioni.** Le credenziali delle fonti (token Git, password IMAP, API key) e i dati ingeriti non devono mai lasciare l'istanza self-hosted senza consenso. L'estrazione AI deve poter girare interamente su Ollama. Un connettore che "telefona a casa" è inaccettabile.

- **Ogni dominio rischia di reinventare la pipeline.** Senza un framework di connettori, il verticale turismo, quello enterprise e quello education costruirebbero tre ingestioni separate. Serve **un solo motore di ingestione** parametrizzabile, con i connettori specifici come plugin PF4J installabili dal marketplace.

### 1.2 La soluzione: un framework di ingestione a connettori, incrementale e tracciabile

LocalMind risponde con un **framework di ingestione unificato** costruito attorno a tre astrazioni nuove e al riuso di quelle esistenti:

1. **Connettore (\`SourceConnector\`)** — interfaccia che astrae *come* si legge una fonte: autenticazione, enumerazione delle risorse, lettura del contenuto, rilevamento delle modifiche (cursore/watermark). Implementazioni concrete (FileSystem, IMAP, Git, REST/OpenAPI, ICS/RSS, CSV, Drive…) sono **adapter** in infrastruttura o **plugin PF4J** dal marketplace.

2. **Pipeline di ingestione generalizzata (\`IngestionPipeline\`)** — orchestratore domain-agnostic che riceve un *RawRecord* dal connettore e lo porta fino al grafo attraverso stadi componibili: normalizzazione → estrazione testo (Tika/OCR) → chunking → embedding (Qdrant) → estrazione entità/relazioni (LLM) → deduplica/entity resolution → **upsert pesato di nodi e archi** nel \`KnowledgeGraphPort\` → registrazione del lineage. Estende la \`DocumentIngestionPipelineService\` esistente, non la sostituisce.

3. **Stato di sincronizzazione (\`IngestionState\` / cursori)** — persistenza dei watermark per fonte (timestamp, ETag, commit SHA, UID IMAP, change token Drive…) che abilita la **sync incrementale** e i checkpoint di ripresa, sul modello Airbyte (cursor field + state message).

Su questa base, l'ingestione diventa un **servizio continuo e idempotente**: i connettori, schedulati dal \`BatchScheduler\` o attivati da webhook/eventi, portano dentro solo il delta, lo trasformano in nodi/archi pesati e mantengono il grafo vivo e tracciabile.

### 1.3 Perché un solo framework per tutte le fonti

| Dimensione | Ingestione attuale (solo documenti) | Framework Ingestione & connettori |
|---|---|---|
| Fonti supportate | File system + upload | File, mail, Git, REST/API, ICS/RSS, CSV, Drive, DB, messaging… (estendibile via plugin) |
| Output | Chunk + vettore | Nodi tipizzati + archi pesati + chunk + vettore + lineage |
| Aggiornamento | Riscansione cartella (full) | Incrementale con cursori/watermark + checkpoint di ripresa |
| Deduplica | Hash file (\`fileHash\`) | Hash + entity resolution semantica (merge di entità equivalenti) |
| Estrazione AI | Non collegata alla pipeline doc | Integrata, solo sul delta, batched, sequenziale per tipo, Ollama-first |
| Tracciabilità | Metadati documento | Lineage completo fonte→nodo→arco con confidenza e timestamp |
| Estensibilità | Codice nel core | Connettori come plugin PF4J dal marketplace |
| Privacy | Local | Credenziali cifrate, estrazione locale, nessun invio esterno senza consenso |

### 1.4 Valore concreto per tipo di adozione

- **Consumer (turismo, eventi, cultura, sport…):** una pro loco collega un CSV di POI, un feed ICS di eventi e un export OSM; il framework li trasforma in nodi *Luogo/Evento/Esperienza* con archi *SI_TROVA_IN / SI_SVOLGE_PRESSO / ABBINABILE_A* pesati per rilevanza e attualità, mantenuti aggiornati ad ogni nuovo feed. I contributi community entrano come una fonte tra le altre, con moderazione.
- **Enterprise (doc, processi, repo, microservizi, API, persone, mail):** si collegano Drive, mailbox, repository Git e OpenAPI dei microservizi; il framework costruisce la mappa viva delle dipendenze (*DIPENDE_DA, CHIAMA, OWNER_DI, MENZIONATO_IN*) con lineage auditabile e privacy garantita. L'incrementale tiene il grafo sincronizzato a ogni commit/mail/documento.
- **Integratori/sviluppatori:** scrivono un nuovo connettore implementando \`SourceConnector\` (o lo prendono dal marketplace) e il grafo si popola, senza toccare il core della pipeline.

### 1.5 Stato attuale e gap da colmare (baseline brownfield)

| Aspetto | Stato attuale (baseline) | Gap / evoluzione necessaria |
|---|---|---|
| Astrazione fonte | \`FileSystemScannerPort\` (solo FS) | Introdurre \`SourceConnector\` generico (auth, list, read, delta) |
| Pipeline | \`DocumentIngestionPipelineService\` (file→chunk→vettore) | Generalizzare a \`IngestionPipeline\` con stadio "estrazione entità/relazioni → upsert grafo" |
| Collegamento al grafo | Pipeline doc **non** alimenta \`knowledge\` | Aggiungere stadio che invoca \`EntityExtractorPort\` + \`KnowledgeGraphPort\` con peso |
| Incrementalità | \`FolderScanJobConfig\` riscansiona (hash per skip) | Cursori/watermark persistiti per fonte + checkpoint Spring Batch |
| Deduplica | Solo \`fileHash\` a livello documento | Entity resolution (match esatto + semantico) sui nodi |
| Lineage/provenienza | Metadati base nel documento | Tabella di provenienza fonte→nodo/arco con confidenza, offset, timestamp |
| Connettori non-file | Domini \`email\`/\`calendar\`/\`messaging\` isolati | Wrapparli come \`SourceConnector\` che producono nodi/archi |
| Estensibilità connettori | Plugin API ha solo \`DocumentParserExtension\` | Aggiungere extension point \`SourceConnectorExtension\` (PF4J) |
| Scheduling | \`BatchScheduler\` (folder scan) | Generalizzare a scheduling per-connettore + trigger webhook/evento |
| Osservabilità | Log + \`DocumentJobListener\` | Metriche per connettore (record letti/dedup/falliti, lag), stato sync in UI |

---

## 2. Personas & utenti target

| Persona | Profilo | Obiettivi rispetto all'ingestione | Bisogni dal sistema |
|---|---|---|---|
| **Amministratore / DevOps** | Gestisce l'istanza self-hosted | Configurare fonti, schedulazioni, credenziali; monitorare lo stato sync | UI connettori, gestione segreti cifrata, metriche, log, retry |
| **Knowledge / Data engineer** | Cura la qualità del grafo | Mappare fonti→tipi di nodo, regole di deduplica, soglie di confidenza | Mapping configurabile, strumenti di entity resolution, preview/dry-run |
| **Sviluppatore di connettori** | Estende LocalMind con nuove fonti | Scrivere un \`SourceConnector\` come plugin | SPI stabile, SDK, docs IT/EN, esempi, test harness |
| **Sviluppatore di dominio** | Costruisce un verticale | Definire quali nodi/archi produrre da ciascuna fonte | Pipeline parametrizzabile, schema modulare, hook per arricchimento |
| **Curatore / Moderatore (consumer)** | Valida contributi e fonti community | Approvare/rifiutare record ingeriti prima del merge nel grafo | Coda di staging, anteprima, audit dei contributi |
| **Compliance / Security officer (enterprise)** | Garantisce privacy e auditabilità | Sapere provenienza dei dati, cancellare a cascata | Lineage, retention, right-to-be-forgotten, consensi |
| **Utente finale** | Beneficia indirettamente | Avere un grafo aggiornato e completo | Freschezza dei dati, copertura delle fonti |
| **AI / agente LLM** | Consuma il grafo popolato | Ottenere contesto ricco e tracciabile | Nodi/archi con lineage e confidenza per citazioni |

---

## 3. Requisiti in input

Questa sezione è volutamente dettagliata: definisce *cosa serve in ingresso* perché un connettore e la pipeline funzionino in modo robusto, sicuro e incrementale. I requisiti sono raggruppati per area.

### 3.1 Configurazione della fonte (per ogni connettore)

| Campo | Tipo | Obbligatorio | Descrizione | Validazione |
|---|---|---|---|---|
| \`id\` | UUID | sì (gen.) | Identificativo della sorgente configurata | \`CHAR(36)\` |
| \`connectorType\` | enum | sì | FILE_SYSTEM, IMAP, GIT, REST_API, OPENAPI, ICS, RSS, CSV, GDRIVE, DATABASE, MESSAGING… | Deve esistere un connettore registrato |
| \`name\` | string | sì | Nome leggibile della fonte | non vuoto, max 200 |
| \`domainProfile\` | enum | sì | Profilo di dominio (CONSUMER_TURISMO, ENTERPRISE_DOCS…) che determina il mapping nodi/archi | tra i profili installati |
| \`connectionParams\` | JSON | sì | Parametri specifici (path, host, porta, URL base, repo URL, query SQL…) | schema per tipo |
| \`credentialsRef\` | string | dipende | Riferimento al segreto cifrato (mai in chiaro) | richiesto se la fonte è autenticata |
| \`schedule\` | cron | no | Pianificazione della sync (se non event-driven) | espressione cron valida |
| \`incrementalMode\` | enum | sì | FULL_REFRESH, INCREMENTAL_APPEND, INCREMENTAL_DEDUPED | coerente con le capacità del connettore |
| \`cursorField\` | string | dipende | Campo usato come watermark (es. \`updatedAt\`, \`commitDate\`, UID) | richiesto se INCREMENTAL |
| \`filters\` | JSON | no | Inclusioni/esclusioni (glob, label IMAP, branch Git, range date) | sintassi per tipo |
| \`mappingConfig\` | JSON | no | Override del mapping fonte→tipi di nodo/relazione | valido vs schema di dominio |
| \`enabled\` | bool | sì | Attiva/disattiva la sync | default false |

### 3.2 Credenziali e segreti (vincolo privacy/local-first)

- **Mai in chiaro nel DB né nei log.** Le credenziali (password IMAP, token PAT Git, API key REST, OAuth refresh token, service account Drive) vanno cifrate at-rest (riuso del pattern già adottato per le chiavi LLM in \`llm_provider_configs\`, evoluto verso cifratura simmetrica con chiave da \`.env\`).
- **Scope minimo.** Ogni connettore dichiara i permessi richiesti (read-only di default). L'ingestione non deve mai modificare le fonti.
- **Consenso esplicito per fonti cloud.** Connettori che escono dalla rete locale (Drive, REST esterni) richiedono opt-in e sono evidenziati nell'UI; di default tutto resta on-premise.
- **Rotazione e revoca.** Possibilità di ruotare/revocare un segreto invalidando la sync senza perdere lo storico del grafo.

### 3.3 Requisiti sul contenuto (per record)

Ogni unità ingerita è un **\`RawRecord\`** con almeno:

| Campo | Descrizione |
|---|---|
| \`sourceId\` + \`externalId\` | Identità stabile del record nella fonte (per dedup/upsert e cancellazione) |
| \`contentType\` / \`mimeType\` | Per scegliere estrattore (Tika, OCR, parser strutturato) |
| \`payload\` | InputStream/bytes o testo già estratto |
| \`metadata\` | Mappa chiave-valore (autore, data, percorso, label, partecipanti…) |
| \`cursorValue\` | Valore del watermark del record (per avanzare lo stato) |
| \`checksum\`/\`hash\` | Per rilevare modifiche e deduplicare |
| \`acl\` (opz.) | Permessi/visibilità originali, da propagare al grafo (enterprise) |

Vincoli di validazione: dimensione massima configurabile per record; encoding rilevato/normalizzato a UTF-8; MIME verificato e non solo fidato dall'estensione; payload binari sospetti messi in quarantena.

### 3.4 Requisiti di mapping (fonte → grafo)

- **Profilo di dominio attivo.** Determina quali \`EntityType\`/\`RelationType\` sono ammessi e come i campi del \`RawRecord\` si proiettano su nodi/archi (es. mittente mail → nodo \`PERSON\`, thread → relazioni \`MENTIONED_IN\`).
- **Schema modulare estendibile.** I tipi di nodo/relazione devono poter essere estesi per dominio senza ricompilare il core (coerente con il gap evidenziato in \`20-motore-knowledge-graph-it.md\`).
- **Regole di pesatura iniziale.** Per ogni tipo di arco generato, una formula/peso di default configurabile (vedi §5).

### 3.5 Requisiti non funzionali

- **Idempotenza:** rieseguire la sync sullo stesso delta non deve creare duplicati né alterare i pesi in modo non deterministico.
- **Ripresa (resumability):** checkpoint per riprendere dopo crash/timeout (riuso dello state di Spring Batch + cursori persistiti).
- **Backpressure e limiti:** rate limit verso le fonti e verso Ollama (estrazione entità), per non saturare CPU/GPU locali.
- **Osservabilità:** per ogni run, conteggi di record letti/nuovi/aggiornati/deduplicati/scartati/falliti, durata, lag rispetto alla fonte.
- **i18n:** messaggi, stati ed enum (connectorType, syncStatus, incrementalMode) esposti bilingui IT/EN al frontend.
- **Persistenza Flyway:** nuove tabelle (\`ingestion_sources\`, \`ingestion_runs\`, \`ingestion_state\`, \`source_lineage\`) con una sola query per migrazione e UUID \`CHAR(36)\`.

### 3.6 Precondizioni di sistema

- MySQL e Qdrant raggiungibili; Ollama disponibile (o provider cloud abilitato con consenso) per estrazione entità ed embedding.
- Dominio \`knowledge\` esteso con peso sugli archi e CRUD nodi/relazioni (dipendenza dall'ambito *Motore Knowledge Graph*).
- Connettore registrato (core o plugin PF4J caricato).

---

## 4. Flusso dell'attività (step-by-step)

Il flusso descrive un ciclo completo di sincronizzazione di una fonte, dalla configurazione all'aggiornamento del grafo. È pensato per essere **incrementale, idempotente e ripristinabile**, riusando il layer batch esistente.

### 4.1 Fase 0 — Configurazione e validazione della fonte

1. L'amministratore crea una sorgente dall'UI (feature Angular \`ingestion\`/\`connectors\`) scegliendo \`connectorType\` e profilo di dominio.
2. Inserisce \`connectionParams\` e credenziali; queste ultime vengono cifrate e salvate come segreto referenziato (\`credentialsRef\`).
3. Il backend esegue una **validazione di connettività** (\`SourceConnector.testConnection()\`): autentica, verifica i permessi read-only, conta le risorse disponibili. Errori restituiti con messaggi user-friendly bilingui.
4. Si configura \`incrementalMode\`, \`cursorField\`, \`schedule\`, \`filters\` e \`mappingConfig\`. La sorgente viene persistita in \`ingestion_sources\` (stato \`CONFIGURED\`).

### 4.2 Fase 1 — Trigger della sincronizzazione

La sync parte in uno di tre modi:
1. **Schedulato** — il \`BatchScheduler\` (generalizzato) lancia il job alla cadenza cron della sorgente.
2. **Manuale** — l'utente preme "Sincronizza ora" dall'UI (POST \`/api/v1/ingestion/sources/{id}/sync\`).
3. **Event-driven** — un webhook esterno (es. push Git, nuova mail) o un evento di dominio interno innesca la sync (riuso del \`DomainEventPublisherPort\`).

All'avvio si crea un record \`ingestion_runs\` (stato \`RUNNING\`, timestamp, trigger). Si applica un **lock per sorgente** per evitare run concorrenti sulla stessa fonte.

### 4.3 Fase 2 — Rilevamento del delta (incrementale)

5. Il connettore legge l'ultimo **cursore/watermark** da \`ingestion_state\` (es. \`last commit SHA\`, \`max(updatedAt)\`, \`IMAP UIDNEXT\`, Drive \`changeToken\`).
6. \`SourceConnector.listChanges(cursor)\` enumera **solo** le risorse nuove/modificate/eliminate dal cursore in poi (per FULL_REFRESH, enumera tutto).
   - Le **eliminazioni** alla fonte vengono propagate come tombstone (per la cancellazione a cascata nel grafo).
7. Si produce uno stream di \`RawRecord\` con il rispettivo \`cursorValue\`, in batch dimensionati per backpressure.

> Pattern di riferimento (Airbyte): cursor field + state message; il cursore avanza **solo dopo** che il batch è stato persistito con successo, così un crash riprende dall'ultimo checkpoint senza riprocessare tutto.

### 4.4 Fase 3 — Estrazione e normalizzazione del contenuto

8. Per ogni \`RawRecord\`, in base al \`mimeType\`:
   - testo/documenti → \`TextExtractorPort\` (Tika);
   - immagini/scan → \`OcrExtractorPort\` (Tesseract);
   - formati strutturati (CSV, JSON, OpenAPI, ICS, email MIME) → parser dedicati del connettore;
9. Il testo viene normalizzato (encoding UTF-8, pulizia, lingua rilevata) e arricchito con i metadati della fonte. Si calcola/conferma l'hash per la deduplica.

### 4.5 Fase 4 — Chunking ed embedding (riuso)

10. Il testo normalizzato passa al \`ChunkingService\` esistente (chunk dimensionati con overlap).
11. Ogni chunk viene embeddato e fatto upsert su **Qdrant** (\`QdrantVectorStoreAdapter\`), con riferimento a \`sourceId\`/\`externalId\`. Questo fornisce il **seed semantico** del GraphRAG.

### 4.6 Fase 5 — Estrazione di entità e relazioni (AI, solo sul delta)

12. Sul testo del delta si invoca \`EntityExtractorPort.extractEntities()\` e \`extractRelations()\` (adapter \`LlmEntityExtractorAdapter\`, Ollama-first).
    - **Solo sul delta**, batched, con strategia **sequenziale per tipo** (prima persone, poi luoghi, poi organizzazioni…) per ridurre le entità mancate, e con caching per testi invariati.
13. Le entità/relazioni grezze vengono **mappate** sui tipi del profilo di dominio attivo (\`mappingConfig\`), scartando ciò che non rientra nello schema.

### 4.7 Fase 6 — Deduplica & entity resolution

14. Per ogni entità candidata si cerca un nodo esistente equivalente:
    - **match esatto** su chiavi naturali (email, URL repo, external id, hash);
    - **match semantico** (similarità embedding sopra soglia) per varianti testuali ("Mario Rossi" ≈ "M. Rossi").
15. Se trovato → **merge** (aggiorna proprietà, accumula provenienza, rinforza il peso); se non trovato → nuovo nodo. La risoluzione è registrata per audit ed è reversibile.

### 4.8 Fase 7 — Upsert pesato di nodi e archi nel grafo

16. Nodi e archi vengono inseriti/aggiornati via \`KnowledgeGraphPort\` con **peso** calcolato (vedi §5): confidenza dell'estrazione AI × forza del segnale × recency, accumulato sulle co-occorrenze ripetute.
17. L'operazione è **idempotente**: lo stesso record reingerito rinforza (non duplica) gli archi; le proprietà sono aggiornate in modo immutabile (nuova versione, non mutazione in-place — coerente con le regole di coding del progetto).

### 4.9 Fase 8 — Lineage, cancellazioni e avanzamento dello stato

18. Per ogni nodo/arco prodotto si scrive una riga di **provenienza** in \`source_lineage\` (sorgente, externalId, offset, confidenza, timestamp, runId).
19. Le **tombstone** (Fase 2) attivano la cancellazione a cascata: si rimuovono/decadono i nodi/archi la cui *unica* provenienza era la risorsa eliminata (right-to-be-forgotten).
20. Dopo il commit del batch, si **avanza il cursore** in \`ingestion_state\` e si aggiorna \`ingestion_runs\` (stato \`SUCCESS\`, conteggi, durata, lag).

### 4.10 Fase 9 — Esito, errori e osservabilità

21. In caso di errore su un record, si applica retry con backoff (Spring Retry); i record irrecuperabili finiscono in una **dead-letter** con motivazione, senza bloccare l'intero run.
22. Si pubblicano metriche (record letti/nuovi/aggiornati/deduplicati/scartati/falliti, durata, lag) e un \`IngestionCompletedEvent\`.
23. L'UI mostra lo storico run, lo stato di freschezza per fonte, e gli errori in lingua. In caso di crash, il run successivo riprende dall'ultimo cursore/checkpoint.

### 4.11 Diagramma sintetico del flusso

\`\`\`text
[Fonte] → SourceConnector(testConnection/listChanges/read)
   │  (cursor/watermark da ingestion_state)
   ▼
RawRecord(batch) → Tika/OCR/parser → normalize → ChunkingService → embed → Qdrant
   │
   ├──→ EntityExtractor (LLM, delta only, sequenziale per tipo)
   │        ▼
   │     map → deduplica/entity resolution → upsert pesato → KnowledgeGraphPort (MySQL)
   │                                                              │
   │                                                              ▼
   │                                                        source_lineage
   ▼
commit batch → avanza cursore (ingestion_state) → ingestion_runs(SUCCESS) → metriche/eventi
\`\`\`

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

L'ambito *Ingestione* non inventa un suo schema di dominio: **alimenta** quello del motore a grafo. Tuttavia introduce nodi e relazioni **di provenienza** (lineage) che vivono accanto a quelli di dominio, e produce i tipi di dominio a partire dalle fonti.

### 5.1 Tipi di nodo prodotti / introdotti

| Categoria | Tipo di nodo | Origine tipica | Note |
|---|---|---|---|
| Provenienza | \`SOURCE\` | Sorgente configurata | Nodo che rappresenta la fonte (mailbox, repo, cartella…) |
| Provenienza | \`INGESTION_RUN\` | Esecuzione sync | Per audit/lineage temporale |
| Provenienza | \`RAW_RECORD\` | Documento/mail/file/commit | Unità ingerita, ancora al lineage |
| Dominio (riuso) | \`PERSON\` | Mail, repo, doc | Mittenti, autori, owner |
| Dominio (riuso) | \`ORGANIZATION\` | Doc, mail | Aziende, team |
| Dominio (riuso) | \`PLACE\` | CSV/OSM/ICS (consumer) | Luoghi, POI |
| Dominio (riuso) | \`EVENT\` | ICS/RSS, calendar | Eventi |
| Dominio (riuso) | \`DOCUMENT\` | File, Drive | Documenti |
| Dominio (riuso) | \`TECHNOLOGY\` | Repo, OpenAPI | Microservizi, API, librerie |
| Dominio (riuso) | \`CONCEPT\` | Testo libero | Temi, argomenti |
| Dominio (ext) | \`API_ENDPOINT\`, \`REPOSITORY\`, \`TICKET\`, \`EXPERIENCE\`… | Connettori specifici | Tipi estesi per dominio (schema modulare) |

### 5.2 Tipi di relazione prodotti / introdotti

| Categoria | Tipo di relazione | Significato | Origine |
|---|---|---|---|
| Provenienza | \`EXTRACTED_FROM\` | Nodo/arco di dominio deriva da un \`RAW_RECORD\` | Lineage |
| Provenienza | \`INGESTED_BY\` | \`RAW_RECORD\` ingerito da un \`INGESTION_RUN\` | Lineage |
| Provenienza | \`PROVIDED_BY\` | \`RAW_RECORD\` fornito da una \`SOURCE\` | Lineage |
| Dominio (riuso) | \`MENTIONED_IN\` | Entità citata in un record | Estrazione AI |
| Dominio (riuso) | \`CREATED_BY\` / \`WORKS_AT\` | Autore/affiliazione | Mail, doc, repo |
| Dominio (riuso) | \`LOCATED_IN\` | Localizzazione | Consumer |
| Dominio (riuso) | \`DEPENDS_ON\` / \`REFERENCES\` | Dipendenze tecniche | Repo, OpenAPI |
| Dominio (riuso) | \`PART_OF\` / \`RELATED_TO\` | Aggregazione/affinità | Generico |
| Dominio (ext) | \`CALLS\`, \`OWNER_OF\`, \`REPLIES_TO\`, \`ATTENDS\`… | Relazioni estese | Connettori specifici |

### 5.3 Criteri di peso degli archi

Il peso prodotto in ingestione è il **valore iniziale** che il motore poi evolve. Si compone di fattori normalizzati e configurabili:

| Fattore | Significato per l'ingestione | Come si calcola |
|---|---|---|
| **Confidenza estrazione** | Quanto l'LLM è sicuro della relazione | Score dell'estrattore (\`EntityExtractorPort\`), 0–1 |
| **Forza del segnale** | Quanto è "esplicita" la relazione nella fonte | Es. dipendenza dichiarata in \`pom.xml\` > co-occorrenza testuale |
| **Frequenza/co-occorrenza** | Ripetizione della relazione tra più record/fonti | Accumulo incrementale ad ogni reingestione |
| **Recency** | Attualità dell'informazione | Decadimento temporale basato sul \`cursorValue\`/data del record |
| **Affidabilità della fonte** | Fiducia nella sorgente | Peso per \`SOURCE\` (es. repo ufficiale > mail informale) |
| **Feedback/curatela** | Conferme o correzioni umane | Aumenta/diminuisce il peso (consumer: voti; enterprise: validazione) |

Regole operative:
- Peso finale = funzione configurabile (di default media pesata normalizzata in 0–1) dei fattori sopra.
- **Accumulo idempotente:** reingerire la stessa relazione rinforza il peso (saturazione asintotica), non lo raddoppia.
- **Decadimento:** archi non più confermati da nuove ingestioni decadono nel tempo, fino al pruning.
- Ogni arco conserva i **fattori componenti** (non solo il peso finale) per spiegabilità e ricalcolo.

---

## 6. Fonti dati & connettori (ingestione)

### 6.1 Astrazione \`SourceConnector\`

Tutti i connettori implementano un'unica porta (port/out del dominio \`ingestion\`), con capacità dichiarate:

\`\`\`text
SourceConnector
  ConnectorType type()
  ConnectionTest testConnection(SourceConfig cfg)
  Stream<RawRecord> listChanges(SourceConfig cfg, Cursor cursor)   // delta o full
  InputStream read(RawRecord ref)
  Set<Capability> capabilities()   // INCREMENTAL, DELETES, ACL, WEBHOOK...
\`\`\`

I connettori sono **adapter** in \`localmind-infrastructure/ingestion/adapter/\` oppure **plugin PF4J** caricati a runtime. Il \`FileSystemScannerPort\` esistente viene adattato come primo \`SourceConnector\` (FILE_SYSTEM).

### 6.2 Catalogo connettori

| Connettore | Fonte | Incrementale (cursore) | Riuso esistente | Priorità |
|---|---|---|---|---|
| **FILE_SYSTEM** | Cartelle locali | mtime + hash file | \`LocalFileSystemScanner\`, \`FolderScanJobConfig\` | MVP |
| **UPLOAD** | Upload manuale | n/a (one-shot) | \`DocumentController.upload\` | MVP |
| **IMAP/Email** | Mailbox | UID / UIDNEXT, INTERNALDATE | dominio \`email\` (Angus Mail) | MVP→core |
| **ICS/Calendar** | Calendari | LAST-MODIFIED / SEQUENCE | dominio \`calendar\` | Evoluzione |
| **Git** | Repository | commit SHA / data | nuovo (JGit) | Evoluzione (enterprise) |
| **OpenAPI/REST** | API microservizi | ETag / versione spec | nuovo | Evoluzione (enterprise) |
| **CSV/Excel** | Dataset tabellari | hash riga / colonna data | parser nuovo | MVP (consumer) |
| **RSS/Atom** | Feed | GUID / pubDate | nuovo | Evoluzione (consumer) |
| **Google Drive / SharePoint** | Storage cloud | changeToken / delta | nuovo (OAuth) | Evoluzione |
| **Messaging** | Canali in/out | message id / timestamp | dominio \`messaging\` | Evoluzione |
| **Database/JDBC** | DB applicativi | colonna cursore / CDC | nuovo | Futuro |
| **Wiki (Confluence/Notion)** | Knowledge base | versione pagina | plugin | Futuro |
| **Ticketing (Jira/GitHub Issues)** | Ticket | updated / id | plugin | Futuro |

### 6.3 Modalità di sincronizzazione (modello Airbyte)

| Modalità | Quando usarla | Comportamento |
|---|---|---|
| **FULL_REFRESH** | Fonti piccole o senza cursore affidabile | Rilegge tutto; upsert idempotente |
| **INCREMENTAL_APPEND** | Fonti append-only (log, feed) | Solo record dopo il cursore |
| **INCREMENTAL_DEDUPED** | Fonti mutabili (mail, doc, repo) | Solo delta + deduplica per chiave primaria, tiene la versione più recente |

### 6.4 Estensibilità via plugin

Si introduce l'extension point **\`SourceConnectorExtension\`** in \`localmind-plugin-api\` (accanto a \`DocumentParserExtension\`, \`LlmProviderExtension\`, \`VectorStoreExtension\`). I connettori di terze parti si installano dal **marketplace** come JAR PF4J, dichiarando capacità e parametri; il core li scopre e li espone nell'UI senza ricompilazione.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

### 7.1 MVP (fondamenta del framework)

| # | Funzionalità | Tipo | Dettaglio |
|---|---|---|---|
| 1 | Dominio \`ingestion\` | **Creare** | \`model/\` (SourceConfig, RawRecord, Cursor, IngestionRun, ConnectorType, SyncStatus enum), \`port/in\` (\`IngestionUseCase\`), \`port/out\` (\`SourceConnector\`, \`IngestionStateRepository\`, \`LineageRepository\`) |
| 2 | \`IngestionPipeline\` generalizzata | **Creare** (estende doc) | Orchestratore stadi: normalize→chunk→embed→extract→dedup→upsert→lineage |
| 3 | Stadio "grafo" nella pipeline | **Sviluppare** | Collegare la pipeline doc esistente a \`EntityExtractorPort\` + \`KnowledgeGraphPort\` (oggi scollegati) |
| 4 | Connettore FILE_SYSTEM | **Sviluppare** (riuso) | Adattare \`FileSystemScannerPort\` a \`SourceConnector\` con cursore mtime+hash |
| 5 | Connettore IMAP | **Sviluppare** (riuso) | Wrappare dominio \`email\` come \`SourceConnector\` con cursore UID |
| 6 | Connettore CSV | **Creare** | Parser tabellare + mapping colonne→nodi (consumer) |
| 7 | Sync incrementale + cursori | **Creare** | \`ingestion_state\` + checkpoint Spring Batch; avanzamento solo a commit |
| 8 | Deduplica base | **Creare** | Match esatto su chiavi naturali + hash (entity resolution semantica in evoluzione) |
| 9 | Lineage/provenienza | **Creare** | Tabella \`source_lineage\` + scrittura per ogni nodo/arco |
| 10 | Gestione segreti cifrata | **Creare** | Cifratura credenziali connettori (evoluzione del pattern \`llm_provider_configs\`) |
| 11 | Scheduling per-connettore | **Sviluppare** (riuso) | Generalizzare \`BatchScheduler\` oltre il folder scan |
| 12 | API REST \`/api/v1/ingestion/*\` | **Creare** | CRUD sorgenti, test connessione, sync now, run history, stato |
| 13 | UI Angular \`connectors\`/\`ingestion\` | **Creare** | Lista fonti, wizard connettore, stato sync, log errori (IT/EN) |
| 14 | Migrazioni Flyway | **Creare** | \`ingestion_sources\`, \`ingestion_runs\`, \`ingestion_state\`, \`source_lineage\` (una query/file) |
| 15 | Osservabilità base | **Sviluppare** | Conteggi run + \`IngestionCompletedEvent\` + metriche Micrometer |

### 7.2 Evoluzioni (post-MVP)

| # | Funzionalità | Tipo | Dettaglio |
|---|---|---|---|
| 16 | Entity resolution semantica | Creare | Merge nodi via similarità embedding + soglie + audit reversibile |
| 17 | Estrazione AI ottimizzata | Sviluppare | Sequenziale per tipo, batching, caching su delta, budget Ollama |
| 18 | Cancellazione a cascata (tombstone) | Creare | Right-to-be-forgotten guidato dal lineage |
| 19 | Connettori Git / OpenAPI / RSS / ICS | Creare | Verticali enterprise/consumer |
| 20 | \`SourceConnectorExtension\` (PF4J) | Creare | Connettori dal marketplace |
| 21 | Ingestione event-driven (webhook) | Creare | Push Git/mail → sync immediata |
| 22 | Staging & moderazione (consumer) | Creare | Coda di revisione prima del merge nel grafo |
| 23 | Dead-letter & retry avanzato | Sviluppare | Quarantena record, replay |
| 24 | Connettori Drive/SharePoint/DB/CDC | Creare | Fonti cloud e database, anche streaming |
| 25 | Dashboard freschezza & lag | Creare | KPI per fonte, allarmi su staleness |

### 7.3 Manutenzione continua

- Aggiornare parser/estrattori (Tika, OCR) e adattare i connettori alle API delle fonti che cambiano.
- Mantenere i cursori robusti (gestire reset, clock skew, riallineamenti).
- Monitorare costi/tempi dell'estrazione AT locale e tarare batch/soglie.
- Tenere allineato lo schema modulare di nodi/archi quando nascono nuovi domini.
- Aggiornare documentazione IT/EN e gli sviluppi nella cartella \`Sviluppi/\`.

---

## 8. Casi d'uso AI / GraphRAG

L'ingestione è abilitante: più è ricca e fresca, più il GraphRAG è potente. Casi concreti:

- **Impact analysis (enterprise):** "Se cambio l'endpoint \`/orders\` quali microservizi e owner sono impattati?" — il connettore OpenAPI+Git ha prodotto \`API_ENDPOINT\`, \`REPOSITORY\`, archi \`CALLS\`/\`DEPENDS_ON\` pesati e \`OWNER_OF\`; l'AI percorre gli archi forti e cita le fonti.
- **Onboarding accelerato:** un nuovo assunto chiede "chi sa di X e dove sta la doc?" — il grafo, popolato da mail+Drive+repo, risponde con persone, documenti e percorsi, con lineage.
- **Scoperta consumer:** "esperienze abbinabili a questo borgo per questo weekend" — i connettori CSV/ICS hanno prodotto \`PLACE\`/\`EVENT\`/\`EXPERIENCE\` con archi \`LOCATED_IN\`/\`SI_SVOLGE_PRESSO\` aggiornati al feed più recente.
- **Connessioni non evidenti:** l'AI suggerisce link mancanti tra entità ingerite da fonti diverse (link prediction sui pesi accumulati).
- **Risposte tracciabili:** ogni affermazione AI è ancorata al lineage (\`EXTRACTED_FROM\` → \`RAW_RECORD\` → \`SOURCE\`), requisito per audit/compliance.
- **AI come orchestratore di ingestione:** un agente che, su richiesta, lancia la sync di una fonte specifica via tool MCP prima di rispondere.

---

## 9. KPI & metriche di successo

| Categoria | KPI | Target indicativo |
|---|---|---|
| Copertura | N. tipi di connettore attivi | ≥ 5 (MVP), ≥ 10 (evoluzione) |
| Freschezza | Lag medio fonte→grafo | < 15 min (event-driven), < 24h (schedulato) |
| Efficienza | % record processati in incrementale (non full) | > 90% dopo la prima sync |
| Qualità | Tasso di duplicati nel grafo | < 2% nodi |
| Qualità | Precisione entity resolution (match corretti) | > 90% su set di validazione |
| Affidabilità | % run completati senza intervento | > 98% |
| Affidabilità | % record in dead-letter | < 1% |
| Costo locale | Tempo medio estrazione AI per MB di delta | tracciato e in calo nel tempo |
| Tracciabilità | % nodi/archi con lineage completo | 100% |
| Privacy | Credenziali in chiaro nel DB/log | 0 (verificato in security review) |
| Adozione | N. connettori installati dal marketplace | crescente |

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---|---|---|
| Esplosione di duplicati senza buona entity resolution | Grafo frammentato, pesi diluiti | Dedup esatta da MVP; resolution semantica con soglie + audit reversibile |
| Costo dell'estrazione AI in locale (Ollama) | Sync lente, CPU/GPU sature | Estrazione solo sul delta, batching, caching, sequenziale per tipo, rate limit |
| Cursori inaffidabili (clock skew, reset fonte) | Record persi o riprocessati all'infinito | Cursori robusti + fallback hash + run di riconciliazione periodici |
| Credenziali esposte | Violazione privacy/security | Cifratura at-rest, scope read-only, nessun log dei segreti, security review |
| Fonti che cambiano API/formato | Connettori rotti | Capacità versionate, test di connessione, connettori come plugin aggiornabili |
| Reingestione non idempotente | Pesi gonfiati, grafo instabile | Upsert idempotente con accumulo asintotico + chiavi stabili |
| Dati cancellati alla fonte restano nel grafo | Compliance/right-to-be-forgotten | Tombstone + cancellazione a cascata guidata dal lineage |
| Connettore plugin malevolo dal marketplace | Esfiltrazione dati | Sandbox PF4J, permessi dichiarati, revisione marketplace, default offline |
| Backpressure assente verso fonti esterne | Ban/rate-limit dalla fonte | Throttling configurabile e rispetto dei rate limit |
| Pipeline monolitica difficile da estendere | Debito tecnico | Stadi componibili, file piccoli e coesi (regole di progetto) |

---

## 11. Manutenzione & evoluzione

- **Versionamento dei connettori:** ogni connettore dichiara versione e capacità; le rotture delle fonti si gestiscono aggiornando il plugin senza toccare il core.
- **Riconciliazione periodica:** oltre all'incrementale, un job di full-refresh schedulato (basso ritmo) intercetta drift e cursori persi.
- **Tuning continuo:** soglie di dedup, formule di peso, dimensioni batch e budget AI vanno monitorati e tarati via configurazione (no hardcoding).
- **Schema modulare vivo:** quando nasce un nuovo dominio, si aggiungono tipi di nodo/relazione e mapping senza ricompilare il motore.
- **Osservabilità come prima cittadina:** dashboard di freschezza/lag, allarmi su staleness e dead-letter, audit del lineage.
- **Documentazione e sviluppi:** ogni connettore documentato IT/EN (uso, parametri, permessi); ogni sviluppo tracciato in \`Sviluppi/\` con nomenclatura datata; sviluppo in plan mode.
- **Test:** unit (parser, cursori, dedup), integrazione (Testcontainers MySQL, mock fonti), E2E (Playwright sull'UI connettori); copertura ≥ 80%.

---

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo / componente | Ruolo nell'ingestione | Punto di integrazione |
|---|---|---|
| **\`document\`** | Base della pipeline (estrazione, chunking, vettore) | \`DocumentIngestionPipelineService\` → generalizzato in \`IngestionPipeline\`; riuso di \`TextExtractorPort\`, \`OcrExtractorPort\`, \`ChunkingService\`, \`VectorStorePort\` |
| **\`knowledge\`** | Destinazione: nodi e archi pesati | \`EntityExtractorPort\` (estrazione) + \`KnowledgeGraphPort\` (upsert); dipende dall'aggiunta del peso (ambito Motore KG) |
| **\`localmind-batch\`** | Orchestrazione e scheduling | \`DocumentIngestionJobConfig\`/\`FolderScanJobConfig\` generalizzati; \`BatchScheduler\` per-connettore; checkpoint/state di Spring Batch per la ripresa |
| **\`email\`** | Connettore IMAP | \`EmailPort\` (Angus Mail) wrappato come \`SourceConnector\` |
| **\`calendar\`** | Connettore ICS | Eventi come nodi \`EVENT\` |
| **\`messaging\`** | Connettore canali | \`MessagingClientPort\` come fonte (in evoluzione) |
| **\`mcp\`** | Ingestione via tool / AI orchestratrice | L'AI può lanciare sync o leggere fonti via MCP |
| **\`plugin\` (PF4J) + \`localmind-plugin-api\`** | Connettori di terze parti | Nuovo \`SourceConnectorExtension\` accanto a \`DocumentParserExtension\` |
| **\`marketplace\`** | Distribuzione connettori | Installazione/aggiornamento connettori come JAR PF4J |
| **\`llm\` / Ollama** | Estrazione entità/relazioni ed embedding | \`EntityExtractorPort\` (Ollama-first), embedding \`@Primary\` Ollama |
| **\`common\` (eventi)** | Eventi e analytics di ingestione | \`DomainEventPublisherPort\` per \`IngestionCompletedEvent\`; trigger event-driven |
| **\`auth\` / security** | Protezione API e segreti | \`LocalAuthFilter\`; cifratura credenziali connettori |
| **Qdrant** | Indice semantico (seed GraphRAG) | \`QdrantVectorStoreAdapter\` con riferimento a \`sourceId\`/\`externalId\` |
| **MySQL + Flyway** | Stato, sorgenti, lineage | Nuove tabelle (una query per migrazione, UUID \`CHAR(36)\`) |
| **Frontend Angular** | UI connettori e monitoraggio | Nuova feature \`ingestion\`/\`connectors\` (standalone, Signals, i18n IT/EN) |

### 12.1 Coerenza con i vincoli di progetto

- **Local-first / privacy:** connettori read-only, credenziali cifrate, estrazione su Ollama, nessun invio esterno senza consenso esplicito (fonti cloud opt-in).
- **Riuso MySQL + Qdrant:** nessun nuovo datastore; lo stato di sync e il lineage vivono in MySQL, gli embedding in Qdrant.
- **Architettura esagonale:** dominio \`ingestion\` puro (no Spring), connettori come adapter/plugin, wiring in \`DomainConfig\`.
- **i18n IT/EN:** enum (\`ConnectorType\`, \`SyncStatus\`, \`IncrementalMode\`) tradotte e instradate al frontend secondo lo switch lingua.
- **Flyway:** una sola query per file; immutabilità dei record (upsert come nuova versione, non mutazione in-place).
- **File piccoli e coesi:** ogni connettore e ogni stadio della pipeline in moduli separati (200–400 righe tipiche).

---

*Documento di indirizzo per gli sviluppi dell'ambito "Ingestione & connettori" (core). Le scelte di dettaglio (firme esatte, nomi tabelle, formule di peso) vanno consolidate in plan mode prima dell'implementazione, aggiornando la cartella \`Sviluppi/\` e la documentazione bilingue.*
`;
