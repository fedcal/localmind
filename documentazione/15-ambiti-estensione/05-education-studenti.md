# Education & studenti

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema dello studente moderno

Lo studente — universitario, di scuola superiore, dottorando o autodidatta in formazione continua — vive in una condizione paradossale: ha accesso a una quantità di materiale didattico mai vista prima (slide, dispense PDF, registrazioni di lezione, video, paper, appunti propri e altrui, manuali, MOOC, forum), ma non possiede uno strumento che trasformi questa massa frammentata in **conoscenza strutturata, navigabile e interrogabile**. Il risultato concreto è una serie di sintomi ricorrenti:

- **Frammentazione dei materiali.** Le slide del professore stanno su un LMS (Moodle, Google Classroom), gli appunti su Notion o su carta, i PDF in cartelle locali disordinate, i video su YouTube o sulle piattaforme dell'ateneo, le formule su fogli sparsi. Nessuno di questi sistemi conosce gli altri.
- **Perdita dei collegamenti concettuali.** Il valore dello studio non è memorizzare fatti isolati, ma capire *come i concetti si collegano* — quali sono i prerequisiti di un argomento, quali idee derivano da quali, dove una nozione vista in un corso ricompare in un altro. Questi collegamenti, oggi, esistono solo nella testa dello studente (e svaniscono con l'oblio) oppure non esistono affatto.
- **Studio non personalizzato.** Ogni studente parte da una base diversa, dimentica a velocità diverse, ha obiettivi diversi (superare un esame, padroneggiare a fondo, fare un progetto). I materiali, però, sono uguali per tutti e lineari: capitolo 1, 2, 3. Manca un **percorso di studio adattivo** che parta da ciò che lo studente già sa e lo porti dove vuole arrivare rispettando l'ordine dei prerequisiti.
- **Domande senza risposta contestuale.** Quando lo studente non capisce un passaggio, le opzioni sono: cercare su Google (risposte generiche, fuori contesto), chiedere a un chatbot generalista (che non conosce *il suo* corso, *le sue* slide, *la sua* notazione) o aspettare il ricevimento del docente. Manca un tutor che risponda **basandosi sui materiali effettivi del corso** e che sappia citare la slide o il paragrafo esatto.
- **Mancanza di una visione d'insieme.** Dopo mesi di studio, lo studente fatica a "vedere" la disciplina nel suo complesso: quali sono i nodi centrali, cosa è collaterale, dove sono le lacune. Una mappa concettuale fatta a mano invecchia subito e copre solo una porzione del programma.
- **Ripasso inefficiente.** Senza un sistema di ripetizione dilazionata (spaced repetition) ancorato alla struttura della materia, il ripasso è casuale: si ripassa ciò che si ricorda (e che quindi serve meno) e si trascura ciò che si sta dimenticando.

### 1.2 La nostra risposta: il grafo della conoscenza personale dello studente

LocalMind, nella sua evoluzione a **motore di knowledge graph universale**, affronta esattamente questo problema. La proposta di valore per l'ambito Education è costruire, per ogni studente, un **grafo della conoscenza personale**: una rete pesata in cui i nodi sono i corsi, i materiali, i concetti, gli esercizi e gli obiettivi, e gli archi rappresentano relazioni didatticamente significative — "è prerequisito di", "spiega", "approfondisce", "è esempio di", "contraddice", "ricompare in". Su questo grafo si innestano tre capacità:

1. **Organizzazione automatica.** I materiali caricati vengono ingeriti, segmentati, vettorializzati su Qdrant ed estratti in concetti che diventano nodi del grafo, con i collegamenti dedotti automaticamente dall'AI e poi rifiniti dallo studente.
2. **Tutor GraphRAG sul proprio materiale.** L'AI (Ollama in locale per default) risponde alle domande navigando il grafo personale e recuperando i chunk semanticamente pertinenti, citando sempre la fonte esatta (slide N, paragrafo, minuto del video). Nessun dato lascia il dispositivo dello studente, a meno di consenso esplicito.
3. **Percorsi di studio adattivi e ripasso intelligente.** Partendo dallo stato di conoscenza dello studente (cosa ha già padroneggiato, cosa sta dimenticando) e da un obiettivo (un esame in data X), il sistema genera un percorso che rispetta l'ordine dei prerequisiti e pianifica il ripasso con spaced repetition ancorato ai nodi del grafo.

### 1.3 Perché LocalMind è la piattaforma giusta

| Esigenza dello studente | Caratteristica LocalMind che la soddisfa |
|---|---|
| Privacy di appunti, voti, lacune personali | **Local-first**: tutto gira on-device/self-hosted; nessun invio a cloud senza consenso |
| Zero costi ricorrenti (gli studenti hanno budget limitati) | **Open source puro**, AI locale Ollama di default, nessun paywall |
| Materiali in formati eterogenei | Pipeline di ingestione esistente (**Tika + Tesseract OCR**), chunking, embedding |
| Risposte fondate sul *proprio* materiale | **GraphRAG** su grafo personale + ricerca semantica Qdrant, con citazione delle fonti |
| Più lingue (testi in IT ed EN) | Piattaforma **bilingue IT/EN** by design, enum tradotte |
| Estendibilità (plugin per LMS, Anki, ecc.) | Sistema **plugin PF4J** + marketplace |

Il differenziatore competitivo rispetto a strumenti esistenti (Obsidian con i suoi plugin, Notion, Recall, NotebookLM) è la combinazione di tre fattori che nessun concorrente offre insieme: **grafo pesato esplicito + GraphRAG locale + privacy totale a costo zero**. Obsidian ha il grafo ma i link sono manuali e non pesati, e l'AI è un plugin esterno spesso cloud; NotebookLM è potente ma cloud-only e senza grafo navigabile; Recall combina AI e grafo ma è un servizio chiuso. LocalMind unisce questi mondi rimanendo sovrano sui dati e gratuito.

## 2. Personas & utenti target

| Persona | Profilo | Bisogni primari | Come usa LocalMind |
|---|---|---|---|
| **Giulia, studentessa universitaria (triennale Informatica)** | 20 anni, 5-6 esami a sessione, materiale digitale abbondante e disordinato | Preparare esami in tempo, capire i collegamenti tra corsi, ripassare bene | Carica slide e appunti per corso, genera mappe concettuali, usa il tutor sul materiale, segue il percorso di ripasso pre-esame |
| **Marco, studente magistrale / tesista** | 24 anni, legge molti paper, deve integrare conoscenze da fonti diverse | Collegare paper e teoria, individuare lacune, costruire la base teorica della tesi | Crea un grafo cross-corso e cross-paper, usa GraphRAG per domande complesse multi-fonte, esporta bibliografia e mappe |
| **Sara, studentessa scuola superiore** | 17 anni, prepara la maturità, meno autonoma | Schema chiaro per materia, ripasso guidato, spiegazioni semplici | Usa mappe concettuali predefinite, flashcard automatiche, tutor con linguaggio adattato al livello |
| **Davide, autodidatta / lifelong learner** | 35 anni, lavoratore che studia una nuova disciplina (es. data science) | Percorso strutturato partendo da zero, gestione del tempo limitato | Definisce un obiettivo di apprendimento, il sistema genera un percorso adattivo dai materiali raccolti |
| **Prof.ssa Bianchi, docente (utente secondario)** | Crea o cura materiali per i propri studenti | Pubblicare un grafo-corso di riferimento, vedere dove gli studenti si bloccano (in forma aggregata e anonima) | Pubblica un "grafo-corso" curato come modulo condivisibile; opzionalmente analizza punti critici aggregati |
| **Gruppo di studio (3-5 studenti)** | Studenti che condividono un corso | Condividere appunti, mappe e domande/risposte | Condivisione opt-in di sotto-grafi e materiali in un'istanza self-hosted comune |

L'utente primario e prioritario per l'MVP è **Giulia / Marco (universitario)**: massimo volume di materiale digitale, massima sofisticazione del bisogno, massima disponibilità a self-hostare. Le altre personas guidano le evoluzioni.

## 3. Requisiti in input

Questa sezione definisce in dettaglio **cosa deve poter entrare nel sistema** perché l'ambito Education funzioni. Gli input si dividono in: materiali didattici, metadati di struttura, dati personali dello studente, configurazione e feedback. Ogni input va validato al confine del sistema (principio "never trust external data") e trattato in modo immutabile.

### 3.1 Materiali didattici (contenuto da ingerire)

| Tipo di materiale | Formati supportati | Estrazione | Note |
|---|---|---|---|
| Slide di lezione | PDF, PPTX | Tika (testo) + Tesseract OCR (slide-immagine, formule scansionate) | Conservare il numero di slide per citazione precisa |
| Dispense / manuali / capitoli | PDF, DOCX, EPUB, TXT, Markdown | Tika | Mantenere struttura in capitoli/paragrafi se presente |
| Appunti personali | Markdown, TXT, DOCX, immagini di appunti manoscritti | Tika + OCR (manoscritto: best-effort) | Gli appunti hanno peso "fiducia" inferiore rispetto al materiale ufficiale |
| Paper / articoli scientifici | PDF | Tika + estrazione metadati (titolo, autori, DOI, abstract, riferimenti) | Importante per magistrali/tesisti |
| Registrazioni di lezione | Audio (MP3, WAV), video (MP4) | Trascrizione via `WhisperTranscriptionAdapter` (già presente) | Conservare timestamp per citazione al minuto |
| Pagine web / risorse online | URL (HTML), MOOC | Fetch + estrazione testo | Salvare snapshot per riproducibilità local-first |
| Esercizi / quiz / temi d'esame | PDF, immagini, testo | Tika + OCR | Diventano nodi "Esercizio" collegati ai concetti |

Requisiti trasversali sui materiali:
- **Dimensione massima per file** configurabile (default ragionevole, es. 100 MB) con messaggio d'errore chiaro al superamento.
- **Lingua** rilevata automaticamente (IT/EN e oltre) per scegliere il modello di embedding e la lingua di risposta del tutor.
- **Deduplica**: il sistema deve riconoscere materiali già caricati (hash del contenuto) per evitare nodi duplicati.
- **Provenienza** sempre tracciata: ogni chunk e ogni concetto deve poter risalire al file e alla posizione di origine (slide, pagina, minuto).

### 3.2 Metadati di struttura (organizzazione accademica)

Per dare senso ai materiali servono metadati che lo studente fornisce (o che vengono dedotti e confermati):

- **Corso / insegnamento**: nome, codice, anno accademico, docente, ateneo/istituto, CFU/crediti, lingua.
- **Argomento / modulo**: suddivisione interna del corso (es. "Capitolo 3 — Alberi di ricerca").
- **Materiale → corso**: associazione di ogni file a uno o più corsi/argomenti.
- **Calendario accademico**: date di esame, scadenze di consegna, lezioni programmate (integrabile col modulo `calendar`).
- **Programma d'esame**: lista degli argomenti che "fanno parte" dell'esame, per delimitare il percorso di studio.

### 3.3 Dati personali dello studente (stato di conoscenza)

Sono gli input che rendono il sistema *personale* e *adattivo*. Vanno trattati con la massima riservatezza (local-first, mai esfiltrati):

- **Obiettivo di apprendimento**: "superare l'esame di Analisi I il 15/07", oppure "padroneggiare il backpropagation", con livello-target (superficiale, operativo, padronanza).
- **Stato di partenza / autovalutazione**: per ciascun concetto/argomento lo studente può dichiarare il proprio livello (non visto / visto / capito / padroneggio). Inizialmente opzionale; col tempo dedotto dal comportamento.
- **Tempo disponibile**: ore/giorno o ore/settimana dedicabili allo studio, vincolo essenziale per la pianificazione del percorso.
- **Risultati di pratica**: esiti di flashcard, quiz, autoverifiche — alimentano la stima dello stato di conoscenza (knowledge tracing) e quindi la ripetizione dilazionata.
- **Preferenze di studio**: stile (esempi vs teoria, sintetico vs dettagliato), lingua delle risposte, livello di difficoltà del tutor.

### 3.4 Configurazione di sistema

- **Provider LLM e modello** (default Ollama locale; opzionale cloud con consenso esplicito), modello di embedding, lingua interfaccia (IT/EN).
- **Sorgenti di ingestione**: cartelle locali monitorate (folder watcher esistente), connettori LMS/Drive (evoluzione, vedi §6).
- **Politiche di privacy/condivisione**: cosa è privato, cosa è condivisibile in un gruppo di studio, cosa è pubblicabile come grafo-corso.
- **Parametri del percorso**: algoritmo di spaced repetition, soglie di padronanza, aggressività della pianificazione.

### 3.5 Feedback dello studente (loop continuo)

- **Correzioni al grafo**: aggiungere/rimuovere/rietichettare nodi e archi, confermare o rifiutare i collegamenti suggeriti dall'AI (questo feedback alimenta il peso degli archi, §5).
- **Valutazione delle risposte del tutor**: pollice su/giù, segnalazione di risposte fuori contesto o non fondate.
- **Marcatura di padronanza**: "questo concetto l'ho capito" / "rivedere", che retroagisce sul percorso e sul ripasso.

### 3.6 Validazione e regole sugli input

- Tutti i file passano per validazione di tipo MIME, dimensione e, dove applicabile, integrità (PDF non corrotti).
- I metadati obbligatori per un corso (almeno nome) sono richiesti; il resto è progressivamente arricchibile.
- Nessun input viene mai mutato in place: ogni revisione (es. correzione di un arco) crea una nuova versione, preservando la storia per audit e per il calcolo del peso.
- Gli input dichiarativi dello studente (autovalutazioni) sono sempre sovrascrivibili e mai considerati "verità assoluta": il sistema li incrocia con i dati di pratica.

## 4. Flusso dell'attività (step-by-step)

Il flusso descrive l'esperienza end-to-end, dall'onboarding al ripasso ricorrente. È pensato per l'MVP ma indica anche i punti di evoluzione.

### Fase A — Onboarding e definizione del contesto

1. **Creazione dello spazio di studio.** Lo studente accede a LocalMind (auth local-first esistente) e crea un nuovo "spazio" o seleziona il dominio Education. Sceglie lingua interfaccia (IT/EN) e provider AI (default Ollama locale).
2. **Definizione dei corsi.** Crea uno o più corsi inserendo i metadati minimi (nome corso; opzionalmente codice, docente, data esame). Il calendario accademico può essere importato o inserito a mano (integrazione `calendar`).
3. **Dichiarazione dell'obiettivo (opzionale ma consigliata).** Indica cosa vuole ottenere (es. "preparare l'esame entro il 15/07") e il tempo disponibile. Questo abilita il percorso adattivo.

### Fase B — Ingestione dei materiali

4. **Caricamento.** Lo studente carica file (upload diretto) oppure indica una cartella locale da monitorare (folder watcher batch esistente). Ogni file viene associato a un corso/argomento.
5. **Estrazione e validazione.** Il sistema valida (tipo, dimensione), estrae il testo (Tika), applica OCR alle immagini/scansioni (Tesseract), trascrive audio/video (Whisper). Gli errori sono riportati con messaggi chiari; un file non leggibile non blocca gli altri.
6. **Segmentazione ed embedding.** Il contenuto viene suddiviso in chunk (ChunkingService), ciascuno vettorializzato e indicizzato su Qdrant, con metadati di provenienza (file, slide/pagina, minuto). I materiali e i chunk sono persistiti su MySQL.
7. **Conferma di ingestione.** Lo studente vede l'elenco dei materiali ingeriti, lo stato (completato/in errore) e una prima stima dei concetti individuati.

### Fase C — Costruzione del grafo della conoscenza

8. **Estrazione dei concetti.** Un job AI analizza i chunk e propone i **nodi Concetto** (es. "ricorsione", "complessità ammortizzata"), deduplicando sinonimi e varianti.
9. **Deduzione delle relazioni.** L'AI propone gli **archi**: prerequisiti ("la ricorsione è prerequisito degli alberi"), spiegazioni ("la slide 12 spiega la ricorsione"), approfondimenti, esempi, ricorrenze cross-corso. Ogni arco nasce con un peso iniziale di confidenza.
10. **Revisione umana (human-in-the-loop).** Lo studente vede il grafo proposto e può confermare, correggere, aggiungere o rimuovere nodi e archi. Le conferme aumentano il peso degli archi; i rifiuti lo riducono o li eliminano. Questo passaggio è cruciale per la qualità e per la fiducia.
11. **Visualizzazione interattiva.** Lo studente naviga il grafo: parte da un concetto, espande i vicini, filtra per tipo di nodo/relazione, evidenzia i percorsi di prerequisito. La mappa concettuale è ora viva e sempre aggiornata.

### Fase D — Studio attivo con il tutor GraphRAG

12. **Domanda al tutor.** Lo studente pone una domanda in linguaggio naturale (es. "perché la quicksort è O(n²) nel caso peggiore?").
13. **Recupero GraphRAG.** Il sistema individua i nodi pertinenti, naviga il grafo per raccogliere il sotto-grafo rilevante (concetto + prerequisiti + esempi), e recupera i chunk semanticamente più vicini da Qdrant.
14. **Risposta fondata e citata.** L'AI (Ollama di default) genera la risposta usando *solo* il materiale dello studente quando disponibile, **citando le fonti esatte** (slide, pagina, minuto) e i nodi/percorsi del grafo usati. Lo studente può aprire la fonte con un clic.
15. **Feedback.** Lo studente valuta la risposta (utile / fuori contesto). Il feedback affina i pesi e segnala materiali da arricchire.

### Fase E — Percorso di studio adattivo

16. **Generazione del percorso.** Dato l'obiettivo, lo stato di conoscenza e il tempo disponibile, il sistema produce un **percorso ordinato** che rispetta i prerequisiti (un argomento non viene proposto prima dei suoi prerequisiti) e che parte da ciò che lo studente non padroneggia ancora.
17. **Sessioni di studio.** Per ogni tappa il sistema propone i materiali pertinenti, una spiegazione del tutor e un'autoverifica (flashcard/quiz generati dai concetti).
18. **Aggiornamento dello stato.** Gli esiti delle autoverifiche aggiornano la stima di padronanza (knowledge tracing): i concetti deboli restano nel percorso, quelli solidi escono.

### Fase F — Ripasso e mantenimento (spaced repetition)

19. **Pianificazione del ripasso.** I concetti padroneggiati entrano in un calendario di ripetizione dilazionata: vengono riproposti poco prima del momento stimato di oblio, con priorità ai nodi centrali e ai prerequisiti di argomenti futuri.
20. **Sessioni di ripasso.** Lo studente esegue brevi sessioni; gli esiti riaggiustano gli intervalli (più lunghi se ricorda, più corti se dimentica).
21. **Pre-esame.** All'avvicinarsi della data d'esame, il sistema concentra il ripasso sugli argomenti del programma, evidenzia le lacune residue e propone un "ripasso d'insieme" guidato dalla mappa.

### Fase G — Evoluzione e condivisione (opzionale)

22. **Aggiornamento incrementale.** Nuovi materiali caricati estendono il grafo senza ricostruirlo; il sistema propone i nuovi collegamenti.
23. **Condivisione opt-in.** Lo studente può condividere un sotto-grafo o un grafo-corso con un gruppo di studio (istanza self-hosted comune) o esportarlo (Markdown/JSON, Anki per le flashcard). Niente lascia il sistema senza azione esplicita.

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa l'infrastruttura del **motore di knowledge graph core** (nodi tipizzati + archi pesati su MySQL per la struttura, Qdrant per la semantica). Di seguito i tipi specifici dell'ambito Education.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi chiave |
|---|---|---|
| **Corso** | Insegnamento / disciplina | nome, codice, docente, anno, CFU, lingua |
| **Argomento / Modulo** | Suddivisione tematica di un corso | titolo, ordine, corso di appartenenza |
| **Concetto** | Unità atomica di conoscenza | nome, definizione breve, sinonimi, livello (base/intermedio/avanzato) |
| **Materiale** | Risorsa didattica ingerita | tipo (slide/dispensa/paper/video…), titolo, lingua, hash, provenienza |
| **Chunk / Frammento** | Segmento di materiale (collegato ai vettori Qdrant) | testo, posizione (slide/pagina/minuto), id vettore |
| **Esercizio / Quiz** | Problema o domanda di verifica | testo, soluzione, difficoltà, concetti coinvolti |
| **Flashcard** | Coppia domanda/risposta per ripasso | fronte, retro, concetto, stato spaced-repetition |
| **Obiettivo di apprendimento** | Traguardo dello studente | descrizione, livello-target, scadenza |
| **Percorso di studio** | Sequenza ordinata di tappe | obiettivo, ordine delle tappe, stato |
| **Esame / Scadenza** | Evento accademico | data, programma, corso |
| **Persona** | Docente, autore, compagno di studio | nome, ruolo |
| **Nota personale** | Annotazione/insight dello studente | testo, concetto collegato |

### 5.2 Tipi di relazione (archi)

| Relazione | Da → A | Significato | Direzionata |
|---|---|---|---|
| **è_prerequisito_di** | Concetto → Concetto | A richiede la padronanza di B | Sì |
| **spiega** | Materiale/Chunk → Concetto | La risorsa spiega il concetto | Sì |
| **approfondisce** | Concetto → Concetto | Estende/dettaglia un concetto | Sì |
| **è_esempio_di** | Esercizio/Chunk → Concetto | Istanza applicativa del concetto | Sì |
| **appartiene_a** | Concetto → Argomento → Corso | Gerarchia di contenuto | Sì |
| **ricorre_in** | Concetto → Corso | Stesso concetto in più corsi (cross-corso) | No |
| **verifica** | Esercizio/Quiz/Flashcard → Concetto | Misura la padronanza del concetto | Sì |
| **contraddice / dibatte** | Materiale → Materiale | Fonti con tesi divergenti | No |
| **deriva_da** | Concetto → Concetto | Relazione di derivazione logica/teorica | Sì |
| **copre** | Esame → Argomento/Concetto | Il concetto è in programma | Sì |
| **annota** | Nota personale → Concetto/Materiale | Insight dello studente | Sì |
| **fa_parte_del_percorso** | Concetto → Percorso di studio | Tappa di un percorso | Sì |
| **autore_di** | Persona → Materiale | Paternità della risorsa | Sì |

### 5.3 Criteri per il peso degli archi

Il peso (valore normalizzato, es. 0–1) esprime la **forza/affidabilità** della relazione e guida sia la visualizzazione (archi più spessi) sia il GraphRAG (priorità di esplorazione). Il peso è calcolato come combinazione configurabile dei seguenti fattori, coerente con il principio core "peso derivato da fattori configurabili":

| Fattore | Effetto sul peso | Esempio |
|---|---|---|
| **Confidenza dell'estrazione AI** | Base iniziale dell'arco | L'LLM è molto sicuro che X sia prerequisito di Y |
| **Conferma umana** | Aumenta forte | Lo studente conferma il collegamento → peso alto e "stabile" |
| **Rifiuto umano** | Azzera/rimuove | Lo studente rifiuta il collegamento |
| **Co-occorrenza nei materiali** | Aumenta | Due concetti compaiono spesso negli stessi chunk |
| **Similarità semantica (Qdrant)** | Aumenta | Vicinanza vettoriale elevata tra i contenuti collegati |
| **Frequenza d'uso nello studio** | Aumenta | Percorso/arco attraversato spesso nelle sessioni |
| **Esiti di pratica** | Modula prerequisiti | Sbagliare Y quando X è debole rafforza il legame prerequisito |
| **Autorevolezza della fonte** | Pesa "spiega" | Materiale ufficiale del docente > appunti di terzi |
| **Recenza / decadimento** | Riduce nel tempo | Collegamenti non confermati e non usati decadono lentamente |

Regola di immutabilità: il peso non viene mutato in place sulla relazione; ogni rivalutazione produce una nuova versione del valore (con timestamp e fattori contribuenti), così da poter spiegare *perché* un arco ha quel peso (interpretabilità, requisito ricorrente nella ricerca su knowledge tracing).

## 6. Fonti dati & connettori (ingestione)

| Fonte | Modalità | Stato | Note |
|---|---|---|---|
| **Upload manuale di file** | Drag&drop / selezione | MVP | Riusa `DocumentController.upload` e pipeline esistente |
| **Cartelle locali monitorate** | Folder watcher batch | MVP | `LocalFileSystemScanner` + Spring Batch già presenti |
| **Audio/video di lezione** | Trascrizione | MVP/early | `WhisperTranscriptionAdapter` esistente |
| **Pagine web / URL** | Fetch + estrazione | Evoluzione | Snapshot locale per riproducibilità |
| **LMS (Moodle, Google Classroom, Canvas)** | Connettore plugin (PF4J) | Evoluzione | Sincronizzazione corsi/materiali/scadenze |
| **Cloud storage (Google Drive, OneDrive, Nextcloud)** | Connettore plugin | Evoluzione | Importazione cartelle condivise dei corsi |
| **Reference manager (Zotero, Mendeley)** | Connettore plugin | Evoluzione | Per paper, metadati e citazioni (tesisti/magistrali) |
| **Calendario accademico** | Modulo `calendar` | Early | Date esami/scadenze come nodi Esame |
| **Email (avvisi docente, dispense via mail)** | Modulo `email` | Evoluzione | Estrazione allegati e annunci |
| **Anki / flashcard esistenti** | Import/Export | Evoluzione | Bidirezionale per il ripasso |

Tutti i connettori esterni passano per il sistema **plugin PF4J + marketplace**, così da non gonfiare il core e rispettare la modularità per dominio. Ogni connettore deve dichiarare quali dati legge e dove finiscono, coerentemente con la privacy local-first.

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

### 7.1 MVP (prima release dell'ambito Education)

| # | Funzionalità | Cosa comporta (backend / frontend) | Moduli toccati |
|---|---|---|---|
| 1 | **Dominio `education` (o estensione di `knowledge`)** | Nuovi modelli di nodo/arco specifici, port in/out, service; wiring in `DomainConfig` | domain, infrastructure |
| 2 | **Gestione corsi e materiali** | CRUD corsi/argomenti; associazione materiale→corso; controller `/api/v1/education/*`; UI feature `education` (standalone, Signals) | api, frontend, MySQL (Flyway, una query/file) |
| 3 | **Ingestione materiali didattici** | Riuso pipeline Tika/OCR/Whisper + chunking + Qdrant, con metadati di provenienza (slide/pagina/minuto) | infrastructure, batch |
| 4 | **Estrazione concetti & deduzione archi (AI)** | Job AI che produce nodi Concetto e archi con peso di confidenza; deduplica sinonimi | domain, infrastructure (Ollama) |
| 5 | **Revisione human-in-the-loop del grafo** | API e UI per confermare/correggere nodi e archi; aggiornamento pesi | api, frontend |
| 6 | **Visualizzazione interattiva del grafo personale** | Vista grafo con espansione progressiva, filtri per tipo nodo/relazione, evidenziazione prerequisiti | frontend |
| 7 | **Tutor GraphRAG sul materiale** | Recupero sotto-grafo + chunk Qdrant; risposta con citazione fonti; integrazione chat esistente | domain (knowledge/llm), frontend (chat) |
| 8 | **Flashcard e autoverifiche base** | Generazione flashcard dai concetti; quiz semplici; registrazione esiti | domain, api, frontend |
| 9 | **i18n IT/EN** | Tutte le enum (tipi nodo/arco, stati) tradotte e instradate al frontend secondo lo switch lingua | api, frontend |

### 7.2 Evoluzioni (release successive)

| # | Funzionalità | Valore aggiunto |
|---|---|---|
| 10 | **Percorso di studio adattivo** | Generazione percorso che rispetta i prerequisiti, parte dallo stato dello studente e dal tempo disponibile |
| 11 | **Knowledge tracing / stima padronanza** | Stato di conoscenza per concetto, aggiornato dagli esiti di pratica; interpretabile |
| 12 | **Spaced repetition ancorato al grafo** | Pianificazione del ripasso con priorità ai nodi centrali e ai prerequisiti futuri |
| 13 | **Connettori LMS / Drive / Zotero (plugin PF4J)** | Ingestione automatica da fonti esterne, sincronizzazione corsi/scadenze |
| 14 | **Grafo-corso condivisibile / marketplace** | Docenti o studenti pubblicano grafi-corso curati come moduli installabili |
| 15 | **Gruppi di studio (condivisione opt-in)** | Sotto-grafi e materiali condivisi su istanza self-hosted comune |
| 16 | **Export Anki / Markdown / JSON** | Interoperabilità con strumenti esistenti |
| 17 | **Suggerimento di lacune e collegamenti mancanti** | L'AI propone concetti deboli e link non evidenti tra corsi/paper |
| 18 | **Analytics di studio (privato)** | Tempo per argomento, andamento padronanza, previsione di prontezza all'esame |

### 7.3 Da mantenere (manutenzione continua)

- Pipeline di ingestione (aggiornamento parser Tika, lingue OCR, modelli Whisper).
- Prompt e logica GraphRAG (qualità dell'estrazione concetti e della citazione fonti).
- Schema del grafo e migrazioni Flyway (una query per file), con evoluzione retro-compatibile.
- Traduzioni IT/EN di enum e UI a ogni nuova feature.
- Compatibilità dei connettori plugin con le API esterne (LMS, Drive) che cambiano nel tempo.
- Tuning dei fattori di peso e degli algoritmi di spaced repetition sulla base del feedback reale.

## 8. Casi d'uso AI / GraphRAG

1. **Tutor fondato sul materiale.** "Spiegami la differenza tra BFS e DFS usando le slide del corso." → L'AI naviga i nodi Concetto (BFS, DFS) e i loro prerequisiti, recupera i chunk delle slide pertinenti e risponde citando "Slide 23, Lezione 7". Tutto in locale con Ollama.
2. **Domanda multi-fonte (cross-corso/paper).** "Dove ho già visto il concetto di entropia?" → L'AI segue gli archi `ricorre_in` e mostra che l'entropia compare in Teoria dell'Informazione e in Machine Learning, collegando i materiali dei due corsi.
3. **Individuazione di lacune.** "Sono pronto per l'esame di Algoritmi?" → L'AI confronta il programma (`copre`) con lo stato di padronanza, individua i concetti deboli e i prerequisiti non solidi, e propone un piano di recupero.
4. **Generazione del percorso di studio.** "Ho 10 giorni e 2 ore al giorno: come mi preparo?" → GraphRAG costruisce un percorso ordinato per prerequisiti, bilanciato sul tempo, partendo dalle lacune.
5. **Spiegazione adattiva al livello.** L'AI adatta la risposta al livello dichiarato (superiore vs magistrale), usando esempi più o meno formali presenti nel materiale.
6. **Suggerimento di collegamenti non evidenti.** L'AI propone: "Il concetto di ricorsione (Algoritmi) è prerequisito implicito per la programmazione dinamica (Ottimizzazione): vuoi collegarli?".
7. **Generazione automatica di flashcard e quiz** dai concetti centrali del grafo, con difficoltà calibrata.
8. **Riassunto strutturato di un argomento** seguendo la gerarchia `appartiene_a` e i prerequisiti, con citazioni puntuali.
9. **Ripasso conversazionale pre-esame** guidato dalla mappa: l'AI interroga lo studente sui nodi a rischio oblio e aggiorna gli intervalli di ripetizione.

## 9. KPI & metriche di successo

| Categoria | Metrica | Obiettivo indicativo |
|---|---|---|
| **Adozione** | Materiali ingeriti per studente attivo | Crescita costante; ≥ 20 materiali/corso |
| **Qualità del grafo** | % archi suggeriti confermati dallo studente | ≥ 60% accettazione al netto delle correzioni |
| **Qualità del tutor** | % risposte valutate utili e fondate (con citazione corretta) | ≥ 80% pollice su |
| **Fondatezza** | % risposte con almeno una citazione verificabile | ≥ 95% quando il materiale esiste |
| **Efficacia di studio** | Miglioramento padronanza stimata pre/post sessione | Tendenza positiva misurabile |
| **Ripasso** | Aderenza alle sessioni di spaced repetition pianificate | ≥ 50% sessioni completate |
| **Esiti percepiti** | Esito esame / autovalutazione di prontezza | Correlazione positiva con uso del percorso |
| **Performance** | Latenza risposta tutor in locale (Ollama) | Accettabile su hardware consumer (target < pochi secondi al primo token) |
| **Privacy** | Dati inviati a cloud senza consenso | Zero (vincolo, non obiettivo) |
| **Retention** | Studenti attivi a fine sessione d'esame | Misurare ritorno alla sessione successiva |

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---|---|---|
| **Estrazione concetti rumorosa** (concetti errati o duplicati) | Grafo poco affidabile, sfiducia | Human-in-the-loop obbligatorio in revisione; deduplica per similarità; soglie di confidenza; possibilità di rigenerare |
| **Allucinazioni del tutor** | Risposte sbagliate, danno didattico | GraphRAG vincolato al materiale; citazione obbligatoria; risposta "non presente nei tuoi materiali" quando manca la fonte |
| **Costo computazionale locale** (LLM/embedding su hardware studente) | Lentezza, frustrazione | Modelli Ollama leggeri di default; batch in background; opzione cloud con consenso; caching |
| **Qualità OCR su appunti manoscritti** | Materiale illeggibile | Best-effort dichiarato; preferire materiale digitale; consentire correzione manuale del testo estratto |
| **Sovraccarico cognitivo del grafo** (troppi nodi) | Mappa illeggibile | Espansione progressiva, filtri, clustering per argomento, viste focalizzate |
| **Pigrizia/curatela** (studente non revisiona) | Grafo non rifinito | Suggerimenti a basso attrito (un clic accetta/rifiuta); revisione incrementale; valori di default sensati |
| **Privacy dei dati personali** (voti, lacune) | Violazione fiducia | Local-first rigoroso; condivisione solo opt-in; cifratura a riposo; nessuna telemetria |
| **Affidabilità su MySQL+Qdrant per query di grafo profonde** | Percorsi/prerequisiti lenti su grafi grandi | Indicizzazione mirata, materializzazione di percorsi frequenti, limiti di profondità; rivalutare grafo dedicato solo se necessario (vincolo di progetto) |
| **Disallineamento con il programma reale del corso** | Percorso non pertinente | Ancoraggio al programma d'esame fornito; feedback dello studente; grafo-corso del docente come riferimento |
| **Multilingua** (materiali misti IT/EN) | Concetti non collegati tra lingue | Embedding multilingue; mapping di sinonimi cross-lingua; enum e UI bilingui |

## 11. Manutenzione & evoluzione

- **Aggiornamento incrementale del grafo.** Nuovi materiali estendono il grafo esistente; un job periodico ricalcola i collegamenti candidati e propone aggiunte senza ricostruzioni distruttive.
- **Decadimento e igiene del grafo.** Archi non confermati e non usati decadono; routine periodiche segnalano nodi orfani, duplicati e collegamenti deboli da rivedere.
- **Versionamento dello schema.** Ogni evoluzione dei tipi di nodo/arco passa per migrazioni Flyway retro-compatibili (una query per file), con strategia di backfill documentata.
- **Tuning dei modelli e dei prompt.** Aggiornamento periodico dei prompt di estrazione e GraphRAG, dei modelli Ollama consigliati e dei parametri di chunking, guidato dalle metriche di §9.
- **Calibrazione di pesi e spaced repetition.** I fattori di peso (§5.3) e gli intervalli di ripasso vengono affinati sui dati di utilizzo reali, mantenendo l'interpretabilità.
- **Compatibilità connettori.** Monitoraggio delle API esterne (LMS, Drive, Zotero) e aggiornamento dei plugin PF4J corrispondenti.
- **Documentazione bilingue.** Ogni feature aggiorna documentazione IT/EN e i log in `Sviluppi/` secondo le convenzioni di progetto.
- **Roadmap di valutazione.** Introdurre nel tempo un set di valutazione (golden questions per corso) per misurare regressioni nella qualità del tutor e dell'estrazione.

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo esistente | Ruolo nell'ambito Education |
|---|---|
| **knowledge** | Base del motore a grafo: estensione con tipi di nodo/arco Education; punto naturale dove innestare il dominio |
| **document** | Ingestione materiali (upload, Tika, OCR Tesseract), chunking, metadati di provenienza |
| **llm** | Tutor e estrazione concetti via `LlmGatewayService`; Ollama default, fallback cloud opzionale con consenso |
| **(Qdrant) vectorstore** | Indice semantico dei chunk per il recupero GraphRAG |
| **batch** | Job di ingestione e folder watcher; job periodici di ricalcolo del grafo |
| **calendar** | Date di esame/scadenze come nodi Esame; pianificazione del percorso e del ripasso |
| **email** | Ingestione di avvisi e allegati del docente (evoluzione) |
| **mcp** | Esposizione di tool (es. interrogare il grafo, generare flashcard) ad agenti esterni |
| **agent** | Tutor-agente che orchestra ricerca sul grafo, generazione quiz e pianificazione |
| **automation** | Trigger automatici: "nuovo materiale → estrai concetti", "esame tra 7 giorni → intensifica ripasso" |
| **marketplace + plugin (PF4J)** | Connettori LMS/Drive/Zotero e grafi-corso condivisibili come moduli installabili |
| **finetuning** | Eventuale adattamento di modelli locali sul dominio/lessico dello studente (avanzato) |
| **auth** | Identità local-first dello studente; separazione dei dati personali |
| **common** | Eventi di dominio (es. "materiale ingerito", "concetto padroneggiato"), analytics privati, gestione errori |
| **Frontend (Angular 21)** | Nuova feature `education` standalone con Signal store, vista grafo interattiva, tutor, percorso, ripasso; i18n IT/EN |

L'ambito Education è quindi un **verticale consumer** del motore universale: riusa interamente l'infrastruttura esistente (ingestione, embedding, LLM, grafo, plugin) e aggiunge solo i tipi di nodo/relazione, le funzionalità di studio e l'esperienza utente specifici dello studente — coerente con il principio "una piattaforma, più ecosistemi", restando local-first, gratuito, privato e bilingue.
