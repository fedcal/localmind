# Knowledge base aziendale

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema della conoscenza aziendale frammentata

In qualsiasi organizzazione di dimensioni non banali la conoscenza esiste, ma è **dispersa, duplicata, contraddittoria e non navigabile**. Una procedura operativa vive su Confluence, la sua versione "vera ma non aggiornata" su un PDF nel SharePoint, la spiegazione di *perché* si fa così solo in un thread Slack di otto mesi fa, l'eccezione che vale per un cliente specifico in una mail, e il dettaglio decisivo soltanto nella testa del collega che era presente alla riunione. Il risultato è un insieme di sintomi ricorrenti e costosi:

- **Frammentazione tra strumenti.** La conoscenza è sparpagliata tra wiki (Confluence, Notion), file system (SharePoint, Google Drive), chat (Slack, Teams), ticketing (Jira, ServiceNow), repository (Git), email e calendari. Nessuno di questi sistemi conosce gli altri: la stessa informazione esiste in più copie che divergono nel tempo.
- **Knowledge tacito mai scritto.** La maggior parte della conoscenza istituzionale non è documentata: vive in conversazioni, registrazioni di call, note sparse e nell'esperienza delle persone. Quando una persona se ne va, quella conoscenza esce dall'azienda con lei.
- **Documentazione stantia e non fidabile.** Una pagina wiki non ha modo di dire "sono obsoleta". L'utente non sa se la procedura che sta leggendo è ancora valida; nel dubbio chiede a un collega, generando interruzioni e perpetuando il passaparola.
- **Collegamenti impliciti e invisibili.** "Quali servizi dipendono da questa API?", "Quale procedura è impattata se cambio questo processo?", "Chi è l'esperto di questo modulo?" — sono domande di relazione, ma i sistemi documentali sono fatti di pagine isolate con qualche link manuale, non di un grafo navigabile. La relazione tra due informazioni esiste solo nella mente di chi le ha collegate.
- **Ricerca per parola chiave che non capisce il significato.** La ricerca testuale dei wiki trova le pagine che contengono le parole digitate, non le pagine che rispondono alla domanda. Domande complesse e multi-passo ("perché abbiamo deciso di usare X invece di Y, e quali servizi ne sono toccati?") restano senza risposta diretta.
- **Onboarding lento e ridondante.** Un nuovo assunto impiega settimane a capire "come funzionano le cose qui" perché deve ricostruire a mano una mappa che nessuno ha mai disegnato. Le stesse domande vengono poste e riposte all'infinito.
- **Rischio di compliance e perdita di tracciabilità.** Senza una rappresentazione governata di chi-sa-cosa e di quale-versione-è-valida, audit, certificazioni e gestione del rischio diventano esercizi manuali e fragili.

### 1.2 La nostra risposta: un grafo unico della conoscenza aziendale

LocalMind, nella sua evoluzione a **motore di knowledge graph universale**, affronta esattamente questo problema. La proposta di valore per l'ambito enterprise è costruire un **grafo unificato della conoscenza aziendale**: una rete pesata in cui i nodi sono le unità di conoscenza (documenti, procedure, processi, FAQ, decisioni, persone, sistemi, ticket…) e gli archi rappresentano relazioni operativamente significative — "documenta", "è prerequisito di", "sostituisce", "dipende da", "è esperto di", "contraddice", "deriva dalla decisione". Su questo grafo si innestano tre capacità che nessun wiki tradizionale offre insieme:

1. **Unificazione e de-frammentazione.** I contenuti provenienti da fonti eterogenee (wiki, drive, chat, ticket, repo, mail) vengono ingeriti, segmentati, vettorializzati su Qdrant e collegati nel grafo. La stessa informazione presente in più copie viene riconosciuta, messa in relazione e — dove possibile — riconciliata in un'unica unità di conoscenza con le sue varianti tracciate.
2. **Assistente GraphRAG sulla conoscenza interna.** L'AI (Ollama in locale per default) risponde alle domande dei dipendenti navigando il grafo e recuperando i frammenti semanticamente pertinenti, **citando sempre la fonte esatta** (documento, sezione, versione, autore). Le risposte combinano relazioni del grafo + ricerca semantica: domande multi-hop diventano traversate, non tentativi. Nessun dato aziendale lascia l'infrastruttura senza consenso esplicito.
3. **Conoscenza viva e governata.** Il grafo conosce la freschezza di ogni nodo, la sua autorevolezza, chi lo presidia e quante volte è stato realmente usato. Può segnalare documentazione obsoleta, contraddizioni tra fonti, lacune ("nessuna procedura copre questo processo") e collegamenti mancanti, trasformando la knowledge base da archivio statico a sistema che si mantiene.

### 1.3 Perché un grafo, e non l'ennesimo wiki

Il differenziatore non è "un altro posto dove scrivere documenti", ma il **livello di relazione** sopra i documenti che già esistono. Confluence e Notion sono ottimi editor di pagine ma modellano la conoscenza come alberi di pagine con link manuali: la struttura è gerarchica e statica, le relazioni sono povere e non pesate, la ricerca è lessicale. Un grafo pesato rende le entità e le relazioni **oggetti di prima classe** con proprietà, timestamp e punteggi di confidenza, e abilita ragionamenti che un albero di pagine non può fare: percorsi di dipendenza, vicinato, sottografi tematici, rilevamento di contraddizioni e di obsolescenza.

| Esigenza enterprise | Limite di Confluence/Notion | Come LocalMind la soddisfa |
|---|---|---|
| Trovare *risposte*, non pagine | Ricerca per parole chiave | **GraphRAG**: semantica (Qdrant) + relazioni del grafo, con citazione delle fonti |
| Domande multi-hop ("cosa dipende da X?") | Richiede navigazione manuale dei link | Traversata del grafo pesato lato AI |
| Sapere se un contenuto è ancora valido | Nessun segnale di freschezza | Peso/freschezza/autorevolezza sul nodo, alert su contenuti stantii |
| Privacy del dato aziendale | Spesso SaaS cloud-only | **Local-first / self-hosted**, AI Ollama locale di default |
| Costi di licenza per utente | Pricing per seat | **Open source puro**, nessun paywall |
| Unire fonti eterogenee senza migrazione | Silos separati, connettori a pagamento | Connettori di ingestione verso il grafo, le fonti restano dove sono |
| Estendere a sistemi proprietari | Marketplace chiuso | **Plugin PF4J** + marketplace, connettori custom |
| Bilingue IT/EN | Variabile | Piattaforma bilingue by design, enum tradotte |

In sintesi: LocalMind non sostituisce gli strumenti dove la conoscenza nasce, ma costruisce sopra di essi il **tessuto connettivo navigabile dall'AI** che oggi manca, riducendo la frammentazione senza imporre una migrazione e senza esfiltrare i dati.

## 2. Personas & utenti target

| Persona | Profilo | Bisogni primari | Come usa LocalMind |
|---|---|---|---|
| **Marta, nuova assunta (onboarding)** | Entrata da 2 settimane, deve diventare produttiva | Capire "come funzionano le cose qui", a chi rivolgersi, dove sono le procedure | Fa domande in linguaggio naturale all'assistente; esplora il grafo per orientarsi tra processi e team |
| **Luca, knowledge worker senior** | 8 anni in azienda, depositario di molto sapere tacito | Smettere di rispondere sempre alle stesse domande; lasciare traccia delle decisioni | Risponde una volta, l'assistente riusa; cura i nodi di sua competenza; valida i collegamenti suggeriti |
| **Sara, knowledge manager / documentalist** | Responsabile della qualità della knowledge base | Ridurre duplicati e contenuti stantii, colmare lacune, governare l'ontologia | Usa cruscotti su freschezza/contraddizioni/lacune; cura tipi di nodo e relazione; gestisce la curatela |
| **Davide, IT / platform engineer** | Mantiene servizi, API, infrastruttura | Mappare dipendenze, capire l'impatto di un cambiamento | Collega repo/servizi/API nel grafo; interroga "cosa dipende da…"; usa ADR e runbook collegati |
| **Elena, IT admin / self-hoster** | Installa e gestisce LocalMind on-prem | Deploy local-first, controllo accessi, connettori, privacy | Configura connettori, permessi, provider AI locale; sorveglia ingestione e sicurezza |
| **Giovanni, team lead / manager** | Coordina un team, prende decisioni | Visione d'insieme, trovare l'esperto giusto, tracciabilità delle decisioni | Esplora il grafo di competenze e decisioni; chiede sintesi tematiche all'AI |
| **Operatore di supporto / help desk** | Risponde a clienti o colleghi | Risposte rapide e fondate da FAQ e procedure | Usa l'assistente come fonte unica; segnala risposte sbagliate o lacune |
| **Auditor / compliance (secondario)** | Verifica conformità e tracciabilità | Sapere quale versione è valida, chi è responsabile | Consulta storia, versioni e responsabilità dei nodi |

L'utente primario per l'MVP è la coppia **knowledge worker + nuovo assunto** mediata dal **knowledge manager**: massimo dolore (frammentazione, domande ripetute, onboarding lento) e massimo valore immediato. Le altre personas guidano le evoluzioni (dipendenze IT, compliance, supporto).

## 3. Requisiti in input

Questa sezione definisce in dettaglio **cosa deve poter entrare nel sistema** perché la knowledge base aziendale funzioni. Gli input si dividono in: contenuti da ingerire, metadati di struttura e governance, identità e permessi, configurazione e feedback. Ogni input va validato al confine del sistema (principio "never trust external data") e trattato in modo immutabile: ogni revisione genera una nuova versione, mai una mutazione in place.

### 3.1 Contenuti da ingerire (unità di conoscenza)

| Tipo di contenuto | Fonti / formati tipici | Estrazione | Note |
|---|---|---|---|
| Pagine wiki | Confluence, Notion, MediaWiki (HTML/export/API) | Fetch + estrazione testo, conserva gerarchia spazi/pagine | Mantenere link interni per dedurre archi |
| Documenti d'ufficio | PDF, DOCX, XLSX, PPTX, ODT | Tika; OCR (Tesseract) per scansioni e immagini | Conservare numero pagina/sezione per citazione precisa |
| Procedure / SOP / runbook | PDF, DOCX, Markdown, wiki | Tika + segmentazione per step | Diventano nodi "Procedura" con step ordinati |
| FAQ | CSV/JSON, pagine wiki, export help desk | Parsing coppie domanda-risposta | Nodo "FAQ" collegato a procedure/processi/documenti |
| Decisioni architetturali (ADR) | Markdown nei repo, wiki | Tika + estrazione contesto/decisione/conseguenze | Nodo "Decisione" collegato a sistemi/processi |
| Codice e documentazione tecnica | Repository Git (README, docstring, OpenAPI/Swagger) | Parser repo + estrazione API/servizi | Nodi "Repository", "Servizio", "API", "Database" |
| Conversazioni / chat | Slack, Teams (export/API) | Estrazione thread, deduplica rumore | Knowledge tacito; peso autorevolezza inferiore |
| Ticket / issue | Jira, ServiceNow, GitHub Issues | Estrazione titolo/descrizione/risoluzione | Nodo "Ticket" collegato a procedure/sistemi |
| Email | IMAP via `email` esistente (Angus Mail) | Estrazione corpo/allegati, threading | Knowledge tacito, attenzione massima alla privacy |
| Registrazioni riunioni | Audio/video (MP3, WAV, MP4) | Trascrizione via `WhisperTranscriptionAdapter` | Timestamp conservati per citazione al minuto |
| Calendario / eventi | Modulo `calendar` esistente | Estrazione riunioni/scadenze | Collega decisioni e persone a eventi |
| Pagine intranet / web | URL HTML | Fetch + estrazione, snapshot per riproducibilità | Salvataggio locale per coerenza local-first |

Requisiti trasversali sui contenuti:
- **Dimensione massima per file** configurabile (default ragionevole, es. 100 MB) con messaggio d'errore chiaro al superamento.
- **Lingua** rilevata automaticamente (IT/EN e oltre) per scegliere il modello di embedding e la lingua di risposta dell'assistente.
- **Deduplica e near-duplicate detection**: riconoscere contenuti identici (hash) e simili (similarità di embedding) per evitare nodi duplicati e per riconciliare copie divergenti.
- **Provenienza sempre tracciata**: ogni chunk e ogni nodo deve risalire a fonte, posizione (pagina/sezione/minuto), versione e autore d'origine.
- **Permessi d'origine preservati**: l'ACL della fonte (chi poteva leggere la pagina) deve viaggiare con il contenuto e vincolare chi può vederlo in LocalMind.

### 3.2 Metadati di struttura e governance

Per dare senso ai contenuti servono metadati, forniti dall'utente o dedotti e confermati:

- **Tassonomia / aree**: dipartimento, team, prodotto, dominio funzionale a cui appartiene il contenuto.
- **Tipo e ciclo di vita**: bozza / in revisione / approvato / obsoleto; data di creazione, ultima modifica, **data di prossima revisione**.
- **Responsabile (owner) e revisore**: la persona o il team che presidia il nodo (essenziale per freschezza e per "chiedi all'esperto").
- **Autorevolezza della fonte**: ufficiale (wiki approvata) vs informale (thread Slack) — alimenta il peso degli archi e la fiducia nelle risposte.
- **Classificazione di riservatezza**: pubblico interno / riservato / confidenziale — guida visibilità e politiche AI.
- **Versione**: ogni contenuto è versionato; il grafo conosce qual è la versione valida e quali la sostituiscono.
- **Glossario / ontologia di dominio**: termini, sinonimi, acronimi aziendali, da usare per normalizzare entità e migliorare l'estrazione.

### 3.3 Identità, permessi e organizzazione

Sono input che rendono la knowledge base sicura e "consapevole di chi-sa-cosa":

- **Directory delle persone**: nome, ruolo, team, competenze dichiarate (integrabile con LDAP/SSO in evoluzione, manuale nell'MVP).
- **Struttura organizzativa**: team, dipartimenti, relazioni gerarchiche, per contestualizzare permessi e responsabilità.
- **Mappa dei permessi**: chi può vedere cosa (ACL ereditate dalle fonti + regole locali), chi può modificare il grafo, chi cura l'ontologia.
- **Mappa dei sistemi**: inventario di servizi, API, database, infrastrutture e loro proprietari, base per il grafo di dipendenze.

### 3.4 Configurazione di sistema

- **Provider LLM e modello** (default Ollama locale; cloud opzionale con consenso esplicito), modello di embedding, lingua interfaccia (IT/EN).
- **Connettori e sorgenti**: cartelle locali monitorate (folder watcher esistente), connettori wiki/drive/chat/ticket/repo (vedi §6), con frequenza di sincronizzazione (one-shot, periodica, manuale).
- **Politiche di privacy**: cosa può essere inviato a un provider cloud (per default nulla), cosa è anonimizzabile, quali fonti sono escluse dall'AI.
- **Parametri del grafo**: pesi dei fattori di rilevanza, soglie di freschezza/obsolescenza, soglia di similarità per la deduplica, aggressività dei suggerimenti di collegamento.
- **Politiche di retention**: quanto conservare versioni storiche, come gestire contenuti cancellati alla fonte.

### 3.5 Feedback degli utenti (loop continuo)

- **Correzioni al grafo**: aggiungere/rimuovere/rietichettare nodi e archi, confermare o rifiutare i collegamenti suggeriti dall'AI — questo feedback alimenta il peso degli archi (§5).
- **Valutazione delle risposte dell'assistente**: pollice su/giù, segnalazione di risposte fuori contesto o non fondate, indicazione della fonte corretta.
- **Segnalazione di obsolescenza/contraddizione**: "questa procedura è superata", "queste due pagine si contraddicono" — innesca curatela.
- **Marcatura di autorevolezza**: validazione di un contenuto come "ufficiale" da parte di un owner.

### 3.6 Validazione e regole sugli input

- Tutti i file passano per validazione di tipo MIME, dimensione e integrità (PDF/Office non corrotti) prima dell'ingestione.
- I metadati minimi obbligatori (almeno tipo e, dove possibile, owner) sono richiesti; il resto è progressivamente arricchibile.
- **Nessun input viene mai mutato in place**: ogni revisione (correzione di un arco, nuova versione di un documento) crea una nuova versione immutabile, preservando la storia per audit e per il calcolo del peso.
- Gli input dichiarativi (autovalutazioni di autorevolezza, etichette) sono sempre rivedibili e incrociati con segnali oggettivi (uso reale, feedback).
- I permessi della fonte sono vincolanti: in caso di dubbio, prevale la regola più restrittiva (fail-safe sulla riservatezza).

## 4. Flusso dell'attività (step-by-step)

Il flusso descrive l'esperienza end-to-end, dal collegamento delle fonti all'uso quotidiano e alla manutenzione. È pensato per l'MVP ma indica i punti di evoluzione.

### Fase A — Setup e collegamento delle fonti

1. **Installazione e configurazione local-first.** L'IT admin (Elena) installa LocalMind on-prem o self-hosted. Sceglie lingua interfaccia (IT/EN) e provider AI (default Ollama locale), configura MySQL + Qdrant esistenti.
2. **Definizione dell'ambito e dell'ontologia di base.** Il knowledge manager (Sara) seleziona il dominio "Knowledge base aziendale", che precarica i tipi di nodo e relazione di §5. Può adattare la tassonomia e caricare il glossario aziendale.
3. **Collegamento delle fonti.** Si configurano i connettori (§6): cartelle locali, export/API di wiki, drive, chat, ticket, repo, mail (modulo `email` esistente), calendario (modulo `calendar`). Per ogni fonte si imposta frequenza di sync e mappatura dei permessi.
4. **Censimento di persone e sistemi (MVP minimale).** Si importa o si inserisce manualmente la directory delle persone e l'inventario dei sistemi/API/repo, base per i nodi relazionali.

### Fase B — Ingestione e costruzione del grafo

5. **Estrazione del contenuto.** La pipeline esistente (`DocumentIngestionPipelineService`) estrae testo via Tika, applica OCR (Tesseract) alle scansioni e trascrive audio/video via Whisper. Ogni contenuto conserva provenienza, posizione, versione e permessi.
6. **Chunking ed embedding.** Il contenuto è segmentato (`ChunkingService`) e vettorializzato su **Qdrant**; i metadati relazionali sono persistiti su **MySQL**.
7. **Deduplica e riconciliazione.** Hash + similarità di embedding individuano copie identiche e near-duplicate: si crea un'unica unità di conoscenza con le varianti collegate, evitando frammentazione.
8. **Estrazione di entità e relazioni (AI).** L'AI locale analizza i chunk ed estrae entità candidate (procedure, sistemi, persone, decisioni…) e relazioni candidate ("dipende da", "documenta", "sostituisce"), bootstrappando il grafo dai contenuti non strutturati.
9. **Calcolo dei pesi iniziali.** Gli archi ricevono un peso iniziale dai fattori di §5 (autorevolezza della fonte, co-occorrenza, link espliciti, freschezza). I nodi ricevono punteggi di freschezza e autorevolezza.
10. **Validazione umana (curatela).** Il knowledge manager e gli owner rivedono i collegamenti suggeriti in coda di revisione: confermano, correggono o rifiutano. Ogni decisione retroagisce sul peso.

### Fase C — Uso quotidiano (interrogazione)

11. **Domanda in linguaggio naturale.** Un dipendente (Marta, Luca, supporto) pone una domanda all'assistente — "Qual è la procedura per il rilascio in produzione e quali servizi tocca?".
12. **Recupero GraphRAG.** Il sistema esegue ricerca semantica su Qdrant per i frammenti pertinenti, poi **arricchisce** i risultati con le relazioni del grafo (dipendenze, versioni valide, owner, contraddizioni note). Domande multi-hop diventano traversate del grafo.
13. **Filtro permessi.** Prima di comporre la risposta, il sistema filtra nodi e chunk in base ai permessi dell'utente (ACL ereditate): l'utente vede solo ciò a cui ha diritto.
14. **Risposta fondata e citata.** L'AI (Ollama locale) genera la risposta citando le fonti esatte (documento, sezione, versione, owner) e segnalando se una fonte è potenzialmente obsoleta o in conflitto con un'altra.
15. **Esplorazione del grafo.** L'utente può aprire la visualizzazione interattiva: parte dal nodo "procedura di rilascio", espande i vicini (servizi dipendenti, decisioni correlate, esperti), filtra per tipo di nodo/relazione.
16. **Azioni di follow-up.** Dalla risposta l'utente può saltare al documento, contattare l'owner, aprire un ticket, o segnalare un'inesattezza.

### Fase D — Feedback e manutenzione continua

17. **Valutazione delle risposte.** L'utente vota la risposta e, se errata, indica la fonte corretta: segnale per il ranking e per la curatela.
18. **Rilevamento di obsolescenza e lacune.** Il sistema monitora freschezza (data di revisione superata), uso reale (nodi mai consultati vs molto richiesti), contraddizioni e processi senza procedura, generando alert per il knowledge manager.
19. **Cura periodica.** Sara lavora su una coda prioritaria: aggiorna contenuti stantii, riconcilia contraddizioni, colma lacune, raffina l'ontologia. Gli owner ricevono richieste di revisione sui propri nodi.
20. **Sincronizzazione incrementale.** I connettori rilevano le modifiche alla fonte (delta) e aggiornano nodi/chunk/versioni in minuti, non ricaricando tutto; i contenuti cancellati alla fonte sono marcati secondo la policy di retention.

### Diagramma sintetico del flusso

```text
Fonti (wiki/drive/chat/ticket/repo/mail) ─► Connettori (sync)
        │
        ▼
Estrazione (Tika/OCR/Whisper) ─► Chunking ─► Embedding (Qdrant) + Metadati (MySQL)
        │
        ▼
Deduplica/Riconciliazione ─► Estrazione entità & relazioni (AI) ─► Pesi iniziali
        │
        ▼
Curatela umana (conferma/correzione archi) ◄──── feedback
        │
        ▼
GRAFO DELLA CONOSCENZA (nodi + archi pesati)
        │
        ├─► Assistente GraphRAG (domanda ► semantica + traversata ► filtro permessi ► risposta citata)
        ├─► Visualizzazione interattiva (esplorazione per relazioni)
        └─► Governance (freschezza, contraddizioni, lacune, owner) ─► alert ─► cura
```

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa lo schema generico del motore (nodi tipizzati + archi pesati su MySQL per la struttura, Qdrant per la semantica) e lo specializza con i tipi di questo dominio. Tutti i tipi sono enum **tradotte IT/EN** verso il frontend, coerentemente coi vincoli di progetto.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi chiave specifici |
|---|---|---|
| **Documento** | Unità documentale generica (PDF, DOCX, pagina wiki) | titolo, fonte, versione, owner, riservatezza, freschezza |
| **Procedura / SOP** | Sequenza di passi operativi | step ordinati, prerequisiti, sistema/processo coperto |
| **Processo** | Flusso aziendale di alto livello | input/output, owner, procedure collegate |
| **FAQ** | Coppia domanda-risposta | domanda, risposta, frequenza d'uso |
| **Decisione (ADR)** | Decisione e suo razionale | contesto, decisione, conseguenze, data |
| **Persona** | Membro dell'organizzazione | ruolo, team, competenze, contatti |
| **Team / Dipartimento** | Unità organizzativa | responsabile, ambito |
| **Repository** | Repo di codice | url, linguaggio, owner |
| **Servizio / Microservizio** | Componente software | endpoint, owner, criticità |
| **API** | Interfaccia esposta/consumata | spec (OpenAPI), versione, consumatori |
| **Database / Datastore** | Archivio dati | tecnologia, owner, dati contenuti |
| **Sistema / Infrastruttura** | Componente infrastrutturale | ambiente, owner |
| **Ticket / Issue** | Richiesta o problema | stato, risoluzione, sistema collegato |
| **Cliente / Fornitore** | Entità esterna di business | settore, referente interno |
| **Competenza / Skill** | Area di expertise | livello, persone che la possiedono |
| **Concetto / Termine** | Voce di glossario / ontologia | definizione, sinonimi, acronimi |
| **Riunione / Evento** | Incontro con esiti rilevanti | data, partecipanti, decisioni prodotte |
| **Conversazione** | Thread chat/mail rilevante | partecipanti, sintesi, autorevolezza bassa |
| **Frammento (Chunk)** | Segmento vettorializzato | embedding (Qdrant), posizione, documento padre |

### 5.2 Tipi di relazione (archi, direzionati e pesati)

| Relazione | Da → A | Significato |
|---|---|---|
| **documenta** | Documento/FAQ → Processo/Procedura/Sistema | il contenuto descrive l'entità |
| **è_prerequisito_di** | Procedura/Concetto → Procedura | va conosciuto/eseguito prima |
| **fa_parte_di** | Procedura → Processo; Sezione → Documento | composizione gerarchica |
| **sostituisce / è_sostituito_da** | Documento(vN) → Documento(vN-1) | versionamento e supersession |
| **contraddice** | Documento → Documento | conflitto di contenuto da risolvere |
| **dipende_da** | Servizio/API → Servizio/API/Database | dipendenza tecnica |
| **espone / consuma** | Servizio → API | relazione produttore/consumatore |
| **è_esperto_di** | Persona → Competenza/Sistema/Processo | ownership di conoscenza |
| **è_owner_di** | Persona/Team → Documento/Servizio/Processo | responsabilità formale |
| **deriva_da** | Procedura/Sistema → Decisione | conseguenza di una scelta |
| **menziona / riferisce** | Conversazione/Ticket → entità | citazione informale |
| **risolve** | Ticket/Procedura → Problema/Ticket | risoluzione |
| **correlato_a** | qualsiasi ↔ qualsiasi | relazione semantica generica (peso da similarità) |
| **appartiene_a** | Persona → Team; Servizio → Dominio | aggregazione organizzativa |
| **definisce** | Concetto → Documento/Procedura | aggancio all'ontologia/glossario |

### 5.3 Criteri per il peso degli archi

Il peso è un valore normalizzato (0–1) calcolato come **combinazione configurabile** di fattori; ogni fattore è tracciabile e ricalcolabile in modo immutabile (un nuovo calcolo produce una nuova versione del peso, non sovrascrive). Fattori principali:

| Fattore | Cosa misura | Effetto sul peso |
|---|---|---|
| **Autorevolezza della fonte** | Ufficiale (wiki approvata) vs informale (chat) | fonti ufficiali pesano di più |
| **Esplicità del collegamento** | Link esplicito vs dedotto dall'AI | i link confermati pesano più dei suggeriti |
| **Validazione umana** | Conferma/rifiuto di un owner/curatore | conferma aumenta, rifiuto azzera/penalizza |
| **Co-occorrenza & similarità semantica** | Quanto due nodi compaiono o sono vicini negli embedding | maggiore similarità → peso maggiore (per `correlato_a`) |
| **Frequenza d'uso reale** | Quante volte l'arco è percorso nelle risposte utili | l'uso rinforza il peso (apprendimento dall'uso) |
| **Freschezza / decadimento temporale** | Età e data di revisione del nodo/relazione | contenuti stantii decadono nel peso |
| **Feedback sulle risposte** | Voto positivo/negativo quando l'arco contribuisce a una risposta | i voti calibrano il peso |
| **Criticità / centralità** | Importanza del nodo (es. servizio critico, processo core) | aumenta il peso delle relazioni che lo coinvolgono |

Regole di calcolo:
- I pesi sono **ricalcolabili** in batch (job schedulato) e in tempo reale all'arrivo di nuovo feedback.
- Il **decadimento temporale** evita che vecchi collegamenti restino dominanti per sempre; la frequenza d'uso recente contrasta il decadimento.
- Ogni componente del peso resta ispezionabile, così l'AI può spiegare *perché* ha seguito un certo percorso ("relazione confermata dall'owner, fonte ufficiale, usata 14 volte").

## 6. Fonti dati & connettori (ingestione)

La strategia è **non imporre migrazioni**: le fonti restano dove sono, i connettori le portano nel grafo e le tengono sincronizzate, preservando permessi e provenienza. Si riusa la pipeline esistente (Tika/OCR/Whisper, chunking, Qdrant) e si aggiungono connettori come **plugin PF4J** o adapter di dominio.

| Fonte | Modalità di accesso | Stato suggerito | Note di privacy/permessi |
|---|---|---|---|
| **Cartelle locali / file server** | Folder watcher batch esistente | MVP | Già presente; rispettare permessi file system |
| **Confluence / Notion / wiki** | API o export periodico | MVP→Evoluzione | Ereditare ACL spazi/pagine; sync incrementale (delta) |
| **SharePoint / Google Drive** | API (OAuth) o sync cartelle | Evoluzione | Permission inheritance obbligatoria |
| **Repository Git** | Clone/pull + parser (README, OpenAPI, docstring) | MVP→Evoluzione | Estrae Servizi/API/Repo/ADR |
| **Slack / Teams** | Export o API | Evoluzione | Knowledge tacito; autorevolezza bassa; consenso esplicito |
| **Jira / ServiceNow / issue tracker** | API | Evoluzione | Nodi Ticket collegati a sistemi/procedure |
| **Email** | Modulo `email` esistente (IMAP, Angus Mail) | MVP (riuso) | Massima cautela privacy; opt-in per mailbox |
| **Calendario** | Modulo `calendar` esistente | MVP (riuso) | Riunioni/eventi come nodi |
| **Pagine intranet / web** | Fetch HTTP + snapshot | Evoluzione | Snapshot locale per riproducibilità |
| **Database / sistemi proprietari** | Connettori custom via plugin PF4J | Evoluzione | Estensione marketplace |

Requisiti dei connettori:
- **Sincronizzazione** one-shot, periodica e manuale; **incrementale** (rilevamento delta) per aggiornare in minuti.
- **Preservazione dei permessi** della fonte (ACL) come metadato vincolante.
- **Provenienza** completa (fonte, posizione, versione, autore) su ogni unità ingerita.
- **Idempotenza e deduplica**: ri-sincronizzare non crea duplicati.
- **Gestione delle cancellazioni** alla fonte secondo policy di retention.
- **Osservabilità**: log e metriche di ingestione (Actuator/Prometheus esistenti), stato per connettore in UI.

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

La tabella distingue **CREARE** (nuovo), **SVILUPPARE/ESTENDERE** (su base esistente) e **MANTENERE** (riuso con cura). L'ambito si appoggia al motore a grafo core (in costruzione) e ai domini esistenti.

### 7.1 MVP (primo rilascio utile)

| Funzionalità | Azione | Componenti LocalMind coinvolti |
|---|---|---|
| Dominio "Knowledge base aziendale" con tipi di nodo/relazione di §5 | CREARE | nuovo package dominio + enum IT/EN; estende dominio `knowledge` |
| Schema grafo su MySQL (nodi, archi, pesi, versioni) | CREARE | migrazioni Flyway (una query per file), entità JPA con UUID `@JdbcTypeCode(CHAR)` |
| Ingestione documenti enterprise riusando pipeline | SVILUPPARE | `DocumentIngestionPipelineService`, Tika, Tesseract, Whisper, `ChunkingService` |
| Embedding e ricerca semantica | MANTENERE | `QdrantVectorStoreAdapter`, EmbeddingModel Ollama `@Primary` |
| Estrazione AI di entità/relazioni candidate | CREARE | nuovo servizio dominio + LLM via `LlmGatewayService` (Ollama default) |
| Deduplica/riconciliazione contenuti | CREARE | servizio dominio (hash + similarità embedding) |
| API CRUD nodi/archi e query di grafo (vicini, percorsi, sottografi) | CREARE | controller `/api/v1/knowledge/graph`, port/in, port/out |
| Assistente GraphRAG con citazione fonti | CREARE/SVILUPPARE | nuovo orchestratore GraphRAG che combina Qdrant + traversata grafo; riusa chat/SSE |
| Filtro permessi sulle risposte (ACL ereditate) | CREARE | servizio di autorizzazione su nodi/chunk + `LocalAuthFilter` esistente |
| Calcolo peso archi (fattori base + validazione umana) | CREARE | servizio dominio dei pesi |
| Coda di curatela (conferma/correzione collegamenti) | CREARE | UI Angular + API; feedback verso i pesi |
| Visualizzazione interattiva del grafo (base) | CREARE | nuova feature Angular standalone (lazy), Signal store |
| Connettori MVP: cartelle locali, email, calendario, Git (base) | SVILUPPARE/MANTENERE | folder watcher, moduli `email`/`calendar`, nuovo parser repo |
| i18n IT/EN dei contenuti e delle enum | MANTENERE | `TranslatePipe`, enum tradotte |

### 7.2 Evoluzioni (rilasci successivi)

| Funzionalità | Azione | Note |
|---|---|---|
| Connettori wiki/drive/chat/ticket (Confluence, Notion, SharePoint, Drive, Slack, Teams, Jira) | CREARE (plugin PF4J) | sync incrementale + permission inheritance |
| Rilevamento obsolescenza, contraddizioni e lacune | CREARE | job schedulati + alert; cruscotto knowledge manager |
| Suggerimento proattivo di collegamenti mancanti | SVILUPPARE | AI propone archi ad alta probabilità per la curatela |
| Community detection / sintesi tematiche (domande globali) | CREARE | clustering del grafo + sintesi per comunità |
| Grafo di dipendenze IT con analisi d'impatto ("cosa si rompe se…") | SVILUPPARE | parser OpenAPI/repo avanzato, traversate di dipendenza |
| "Trova l'esperto" e mappa delle competenze | CREARE | nodi Persona/Competenza + ranking |
| Integrazione SSO/LDAP per identità e permessi | SVILUPPARE | oltre l'auth local-first MVP |
| Pubblicazione assistente in canali (Slack/Teams/web widget) | SVILUPPARE | riusa dominio `messaging`/`agent` |
| Versionamento avanzato e audit/compliance | SVILUPPARE | storia immutabile, report di tracciabilità |
| Pacchetti dominio installabili (verticali per settore) | SVILUPPARE | marketplace + plugin PF4J |
| Visualizzazione grafo avanzata (filtri, time-travel, heatmap di freschezza) | SVILUPPARE | feature Angular evoluta |

### 7.3 Da mantenere con cura (rischi noti)

- **Mapping UUID MySQL** (`@JdbcTypeCode(SqlTypes.CHAR)`) su tutte le nuove entità grafo.
- **Boundary tra domini**: evitare import cross-dominio diretti; usare port/out dedicati (vedi `MODULE_BOUNDARIES.md`).
- **Flyway una query per file**: lo schema grafo richiede molte migrazioni piccole e atomiche.
- **Wiring in `DomainConfig`**: i nuovi servizi dominio restano puri, registrati come `@Bean`.

## 8. Casi d'uso AI / GraphRAG

1. **Domanda procedurale fondata.** "Come si fa il rilascio in produzione?" → ricerca semantica + recupero della **procedura valida** (non la versione obsoleta), con citazione di documento, sezione, versione e owner.
2. **Domanda multi-hop di dipendenza.** "Se aggiorno l'API pagamenti, quali servizi e procedure sono impattati?" → traversata di `dipendenza_da`/`consuma`/`documenta`, risposta con elenco dei nodi toccati e relativi owner.
3. **Onboarding conversazionale.** Un nuovo assunto chiede "come funziona il processo di deploy e chi lo presidia?" → l'AI ricostruisce processo, procedure, sistemi ed esperti dal grafo.
4. **Trova l'esperto.** "Chi sa di autenticazione OAuth nel nostro stack?" → traversata `è_esperto_di` con ranking per autorevolezza e attività recente.
5. **Rilevamento di contraddizioni.** L'AI segnala due documenti che descrivono lo stesso processo in modo divergente e propone quale sia la fonte autorevole/recente.
6. **Sintesi tematica globale (community).** "Quali sono i temi ricorrenti nei ticket di incidenti dell'ultimo trimestre?" → community detection + sintesi per comunità senza recuperare migliaia di ticket.
7. **Suggerimento di collegamenti mancanti.** L'AI propone "questa decisione (ADR-42) sembra spiegare *perché* questa procedura esiste: collegarle?" da validare in curatela.
8. **Risposta con tracciabilità e freschezza.** Ogni risposta indica le fonti e avvisa se una fonte è oltre la data di revisione o in conflitto, restando local-first (Ollama) salvo consenso al cloud.
9. **Spiegazione del percorso.** L'AI può esplicitare il percorso seguito nel grafo e i pesi degli archi ("ho seguito la relazione confermata dall'owner, fonte ufficiale, usata di recente").

## 9. KPI & metriche di successo

| Categoria | KPI | Obiettivo / direzione |
|---|---|---|
| Adozione | Utenti attivi settimanali, n. domande/giorno | crescita |
| Efficacia AI | % risposte valutate utili (pollice su), tasso di citazione corretta | alto e crescente |
| Riduzione frammentazione | % duplicati riconciliati, n. fonti unificate nel grafo | crescita |
| Qualità conoscenza | % nodi con owner, % nodi entro data di revisione, n. contraddizioni aperte | freschezza alta, contraddizioni in calo |
| Copertura | % processi con procedura collegata, lacune individuate vs colmate | copertura crescente |
| Produttività | Tempo medio per trovare una risposta, riduzione domande ripetute ai senior | in calo |
| Onboarding | Tempo a produttività dei nuovi assunti | in calo |
| Grafo | N. nodi/archi, densità, % archi validati dall'uomo | crescita controllata |
| Operatività | Latenza risposta GraphRAG, freschezza sync connettori | entro soglie |
| Privacy | % query servite in locale (Ollama) senza invio cloud | massimizzare |

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---|---|---|
| **Estrazione AI imprecisa** (entità/relazioni sbagliate) | Grafo rumoroso, risposte errate | Curatela umana obbligatoria sugli archi suggeriti; pesi che premiano la validazione; soglie di confidenza |
| **Perdita di controllo permessi** (esposizione dati riservati) | Grave (privacy/compliance) | ACL ereditate vincolanti, filtro permessi pre-risposta, fail-safe restrittivo, default local-first |
| **Documentazione stantia** che mantiene autorevolezza | Risposte obsolete | Decadimento temporale del peso, alert di freschezza, owner e date di revisione |
| **Performance del grafo su MySQL** per query multi-hop | Latenza | Indici mirati, query di traversata limitate in profondità, cache (Caffeine esistente), valutare ottimizzazioni; Neo4j fuori scope ma rivalutabile |
| **Sovraccarico di curatela** | Manutenzione insostenibile | Code prioritarie, suggerimenti batch, automazioni, focus sui nodi più usati |
| **Connettori fragili / API che cambiano** | Sync rotta | Connettori come plugin isolati, retry (Spring Retry), osservabilità, sync incrementale |
| **Resistenza al cambiamento** (ennesimo strumento) | Bassa adozione | Non sostituisce le fonti; valore immediato via assistente; onboarding come use case faro |
| **Allucinazioni dell'LLM** | Sfiducia | GraphRAG con citazioni obbligatorie, rifiuto se nessuna fonte, mostrare percorso/pesi |
| **Boundary violation tra domini** | Debito tecnico | Port/out dedicati, rispetto di `MODULE_BOUNDARIES.md` |
| **Scalabilità embedding/storage** | Costi/risorse | Deduplica, retention configurabile, batch di ingestione |

## 11. Manutenzione & evoluzione

- **Cura continua del grafo.** Job schedulati ricalcolano pesi, freschezza e individuano contraddizioni/lacune; il knowledge manager lavora su code prioritarie. La curatela è parte del prodotto, non un'attività occasionale.
- **Sincronizzazione incrementale.** I connettori applicano delta e gestiscono le cancellazioni secondo policy di retention; metriche di ingestione esposte via Actuator/Prometheus.
- **Versionamento immutabile.** Ogni revisione di nodo/arco/peso crea una nuova versione; la storia è preservata per audit e per spiegare le risposte.
- **Evoluzione dell'ontologia.** Tipi di nodo/relazione estendibili per settore; pacchetti dominio installabili via marketplace e plugin PF4J.
- **Osservabilità e qualità.** Test (JUnit backend, Vitest/Playwright frontend), coverage, monitoraggio della % di risposte utili e della freschezza.
- **Documentazione bilingue.** Aggiornamento costante della documentazione IT/EN e tracciamento degli sviluppi nella cartella `Sviluppi/` con nomenclatura datata, come da CLAUDE.md.
- **Roadmap connettori.** Espansione progressiva (wiki → drive → chat → ticket → sistemi proprietari) guidata dal valore e dalla domanda degli utenti.

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo / componente | Ruolo nell'ambito Knowledge base aziendale |
|---|---|
| **`knowledge` (dominio)** | Base da estendere verso il grafo enterprise: tipi di nodo/relazione, query di grafo |
| **`document` + pipeline ingestione** | Estrazione (Tika), OCR (Tesseract), chunking — riuso diretto |
| **`llm` + `LlmGatewayService`** | Estrazione entità/relazioni e generazione risposte; Ollama locale di default, fallback cloud opzionale |
| **Qdrant (`vectorstore`)** | Ricerca semantica e similarità per deduplica e GraphRAG |
| **MySQL + Flyway** | Persistenza di nodi, archi, pesi, versioni (migrazioni atomiche) |
| **`email` (Angus Mail)** | Connettore di ingestione mail (knowledge tacito) — riuso |
| **`calendar`** | Riunioni/eventi come nodi; aggancio a decisioni e persone |
| **`mcp`** | Esposizione del grafo/strumenti come tool MCP per agenti esterni |
| **`agent` + `messaging`** | Pubblicazione dell'assistente in canali (Slack/Teams/web widget) |
| **`auth` + `LocalAuthFilter`** | Identità e base per il filtro permessi sulle risposte |
| **`marketplace` + plugin PF4J** | Connettori e pacchetti dominio installabili (estensibilità) |
| **`automation`** | Sync schedulata, job di curatela e alert di freschezza/contraddizione |
| **`common` (eventi, analytics)** | Eventi di dominio per side-effect (ricalcolo pesi, indicizzazione) e metriche |
| **Frontend Angular (feature standalone)** | Nuova feature `knowledge`/grafo: visualizzazione interattiva, curatela, assistente; i18n IT/EN, Signal store |
| **Chat/SSE esistente** | Riuso del canale di risposta in streaming per l'assistente GraphRAG |

L'ambito **non introduce nuova infrastruttura** (niente Neo4j in questo ciclo): riusa MySQL + Qdrant, rispetta l'architettura esagonale (servizi dominio puri wired in `DomainConfig`), resta local-first con AI Ollama di default, preserva la privacy del dato enterprise ed è interamente bilingue IT/EN.
