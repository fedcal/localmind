# Onboarding & formazione interna

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema dell'onboarding e della formazione nelle organizzazioni

L'inserimento di una nuova persona in un'azienda — e più in generale la formazione continua di chi già lavora — è uno dei processi a più alto costo nascosto e a più bassa industrializzazione. Un nuovo assunto si trova davanti a una realtà che esiste, ma che non è scritta da nessuna parte in modo navigabile: chi fa cosa, dove sta la documentazione aggiornata, qual è il processo "vero" (non quello descritto in un wiki obsoleto), quali strumenti deve installare, quali sistemi compongono l'architettura, a chi chiedere quando si blocca. Il sapere operativo dell'organizzazione vive in tre luoghi tutti inadeguati per chi arriva: **nelle teste delle persone** (e svanisce quando vanno in ferie o cambiano azienda), **in repository documentali frammentati** (wiki, Confluence, Google Drive, SharePoint, Notion, repository Git, ticket, thread di chat) che nessuno tiene allineati, e **in processi impliciti** mai formalizzati. Il risultato concreto è una serie di sintomi ricorrenti:

- **Time-to-productivity lunghissimo.** Le posizioni entry-level raggiungono la piena operatività in circa 30 giorni, ma i ruoli tecnici e senior richiedono 60-90 giorni o più, e mediamente un nuovo assunto impiega 6-7 mesi per sentirsi davvero a proprio agio nel ruolo. Ogni settimana di ritardo è costo puro.
- **Costo economico elevato e per lo più invisibile.** Le organizzazioni spendono tipicamente tra 3.000 e 7.000 dollari per assunzione considerando accessi ai sistemi, contenuti formativi, tempo dei manager e mancata produttività iniziale; il costo complessivo di sostituire una persona e attenderne la piena produttività supera i 30.000 dollari per ruoli intermedi. La voce più pesante non è il corso, ma il tempo rubato a colleghi e manager che fanno da "tutor umani" rispondendo sempre alle stesse domande.
- **Conoscenza frammentata e non collegata.** La documentazione di processo sta nel wiki, il codice e le sue convenzioni in Git, le decisioni architetturali nei thread o nelle ADR, le persone e i loro ruoli nell'organigramma, gli strumenti nei runbook, i clienti nel CRM. Nessuno di questi sistemi conosce gli altri, e soprattutto **nessuno modella le relazioni** che contano per chi impara: "questo servizio dipende da quell'altro", "questo processo è di competenza di quel team", "per fare X devi prima sapere Y".
- **Onboarding uguale per tutti, lineare e statico.** I percorsi tradizionali sono checklist identiche per chiunque, indipendentemente dal ruolo, dal team, dal livello di seniority e da ciò che la persona già sa. Un senior developer e un junior marketing ricevono spesso lo stesso "pacchetto benvenuto". Manca un **percorso adattivo** che parta da ciò che la persona conosce già e la porti dove serve, rispettando l'ordine dei prerequisiti.
- **Domande senza risposta contestuale.** Quando il nuovo assunto si blocca, le opzioni sono: cercare nel wiki (risultati obsoleti o assenti), interrompere un collega (che perde il filo), aprire un ticket (lento), o chiedere a un chatbot generalista che non conosce *quel* sistema, *quei* processi, *quel* gergo aziendale. Manca un tutor che risponda **basandosi sulla conoscenza interna reale** e che sappia citare il documento, il commit o il runbook esatto.
- **Conoscenza che si perde quando le persone vanno via.** Quando un dipendente esperto lascia l'azienda, porta con sé un patrimonio di relazioni implicite ("quel modulo lo ha scritto Marco, e il motivo per cui è fatto così è...") che nessun documento cattura. L'offboarding è un'emorragia di sapere.
- **Formazione continua scollegata dal lavoro reale.** Oltre all'onboarding iniziale, la formazione ricorrente (nuovo sistema introdotto, nuova policy, nuovo processo, upskilling, compliance) soffre degli stessi mali: corsi generici, scollegati dagli strumenti che la persona usa davvero e privi di un sistema di ripasso ancorato alla mappa delle competenze.

### 1.2 La nostra risposta: percorsi di onboarding e formazione costruiti sul grafo della conoscenza interna

LocalMind, nella sua evoluzione a **motore di knowledge graph universale**, affronta esattamente questo problema lato enterprise. La proposta di valore è trasformare il caos documentale e tacito dell'azienda in un **grafo della conoscenza interna pesato e navigabile**, e usare quel grafo come spina dorsale di **percorsi di onboarding e formazione personalizzati**. I nodi sono persone, team, documenti, processi, sistemi, strumenti, competenze, runbook, decisioni; gli archi rappresentano relazioni operative — "è prerequisito di", "è responsabile di", "documenta", "dipende da", "fa parte del processo", "è esperto di". Su questo grafo si innestano tre capacità:

1. **Ingestione e mappatura automatica della conoscenza interna.** Documenti, wiki, repository, ticket, email e processi vengono ingeriti, segmentati, vettorializzati su Qdrant ed estratti in entità e relazioni che popolano il grafo, con i collegamenti dedotti automaticamente dall'AI e poi rifiniti dagli esperti di dominio (knowledge owner). La conoscenza tacita viene resa esplicita e strutturata.
2. **Tutor GraphRAG sulla conoscenza aziendale.** L'AI (Ollama in locale per default) risponde alle domande del nuovo assunto navigando il grafo interno e recuperando i frammenti semanticamente pertinenti, **citando sempre la fonte esatta** (documento, sezione, commit, runbook). Nessun dato aziendale lascia l'infrastruttura on-premise senza consenso esplicito — requisito non negoziabile in ambito enterprise.
3. **Percorsi di onboarding e formazione adattivi.** Partendo dal ruolo, dal team, dal livello e da ciò che la persona già sa, il sistema genera un percorso che rispetta l'ordine dei prerequisiti, assegna materiali e attività concrete, traccia i progressi e pianifica il ripasso delle competenze critiche (compliance, sicurezza, processi chiave).

### 1.3 Il valore misurabile

Un onboarding strutturato non è un costo, è un investimento ad alto ritorno: i programmi efficaci migliorano la retention fino al 52%, la produttività fino al 60% e la soddisfazione del 53%; le persone che attraversano un onboarding strutturato hanno il 58% di probabilità in più di restare in azienda dopo tre anni. La tendenza 2026 è netta: secondo Gartner, entro fine 2026 il 40% delle applicazioni enterprise userà agenti AI task-specifici per orchestrare il lavoro tra sistemi, e circa il 30% delle grandi aziende (IDC) ha già adottato onboarding potenziato dall'AI su scala, con time-to-productivity più rapido e minore attrito precoce come payoff misurabili. LocalMind porta questo valore **senza lock-in cloud, senza canoni e senza esfiltrazione di dati sensibili**.

### 1.4 Perché LocalMind è la piattaforma giusta

| Esigenza dell'organizzazione | Caratteristica LocalMind che la soddisfa |
|---|---|
| Privacy assoluta della conoscenza interna (codice, processi, persone, clienti) | **Local-first / self-hostable**: tutto gira on-premise; nessun invio a cloud senza consenso esplicito |
| Nessun canone ricorrente per ogni dipendente | **Open source puro**, AI locale Ollama di default, nessun paywall |
| Conoscenza in formati e fonti eterogenei | Pipeline di ingestione esistente (**Tika + Tesseract OCR + Whisper**), chunking, embedding; connettori plugin |
| Risposte fondate sulla conoscenza *reale* dell'azienda | **GraphRAG** su grafo interno + ricerca semantica Qdrant, con citazione delle fonti |
| Collegamento tra sistemi già presenti (mail, calendario, repo, ticket) | Domini esistenti `email`, `calendar`, `mcp`, `document`, `automation` |
| Estendibilità verso fonti aziendali (LMS, HRIS, Confluence, Jira, Git) | Sistema **plugin PF4J** + marketplace |
| Interfaccia e contenuti in più lingue | Piattaforma **bilingue IT/EN** by design, enum tradotte |
| Automazione dei trigger di onboarding/formazione | Dominio `automation` + `agent` per orchestrare attività |

Il differenziatore competitivo rispetto a strumenti esistenti (LMS come 360Learning/D2L, knowledge base come Bloomfire/Confluence, assistenti di onboarding cloud) è la combinazione di tre fattori che nessun concorrente offre insieme: **grafo pesato esplicito della conoscenza interna + GraphRAG eseguito interamente in locale + privacy enterprise totale a costo zero di licenza**. Gli LMS gestiscono i corsi ma non modellano le relazioni tra conoscenze, sistemi e persone; le knowledge base hanno la ricerca ma non il grafo navigabile né percorsi adattivi; gli assistenti AI di onboarding sono quasi sempre cloud-only e inviano i dati aziendali a terzi. LocalMind unisce questi mondi rimanendo sovrano sui dati.

## 2. Personas & utenti target

| Persona | Profilo | Bisogni primari | Come usa LocalMind |
|---|---|---|---|
| **Luca, nuovo assunto tecnico (developer)** | 28 anni, entra in un team che mantiene microservizi; deve diventare produttivo in 60-90 giorni | Capire l'architettura, le convenzioni di codice, i processi di deploy, a chi chiedere | Segue il percorso di onboarding del proprio ruolo, naviga il grafo di sistemi/servizi, usa il tutor sul codice e sui runbook, completa checklist e autoverifiche |
| **Sofia, nuova assunta non-tecnica (sales/marketing/ops)** | 26 anni, deve imparare prodotti, processi commerciali, strumenti CRM e policy | Onboarding chiaro, ripasso delle policy, sapere chi contatta per cosa | Percorso guidato per ruolo, tutor su processi e prodotti, mappa di persone e responsabilità |
| **Marta, hiring manager / team lead** | Responsabile dell'inserimento dei nuovi membri del team | Ridurre il tempo speso a rispondere alle stesse domande; vedere i progressi dei nuovi | Definisce/personalizza i percorsi del team, monitora l'avanzamento (in forma rispettosa della privacy), delega al tutor le domande ripetitive |
| **Giorgio, HR / L&D specialist** | Disegna i programmi di onboarding e formazione a livello aziendale | Standardizzare l'onboarding, misurare l'efficacia, gestire la compliance | Crea template di percorso per ruolo/dipartimento, definisce moduli di compliance con ripasso, analizza KPI aggregati e anonimi |
| **Elena, knowledge owner / esperto di dominio (SME)** | Senior che possiede conoscenza critica su un'area | Trasferire il proprio sapere senza essere interrotta di continuo | Cura il sotto-grafo della propria area, conferma/corregge i collegamenti suggeriti dall'AI, "deposita" la conoscenza una volta sola |
| **Paolo, dipendente in formazione continua** | Persona già in azienda che deve apprendere un nuovo sistema/processo | Upskilling mirato, ripasso delle competenze, reskilling | Attiva percorsi di formazione su nuovi argomenti, riceve ripasso pianificato delle competenze critiche |
| **CTO / IT / Security** | Decide l'adozione e ne governa privacy e sicurezza | Self-hosting, controllo dati, audit, integrazione con sistemi interni | Configura l'istanza on-premise, gestisce permessi e visibilità del grafo, verifica che nulla esca senza consenso |

L'utente primario e prioritario per l'MVP è **Luca / Sofia (nuovo assunto)** affiancato da **Marta (manager)** e **Elena (knowledge owner)**: i primi sono i beneficiari diretti del percorso e del tutor, i secondi sono indispensabili per popolare e validare il grafo. Le altre personas guidano le evoluzioni (HR/L&D per la standardizzazione e i KPI, formazione continua per l'upskilling).

## 3. Requisiti in input

Questa sezione definisce in dettaglio **cosa deve poter entrare nel sistema** perché l'ambito Onboarding & formazione funzioni. Gli input si dividono in: conoscenza aziendale da ingerire, struttura organizzativa, definizione dei percorsi, dati personali del discente, configurazione e feedback. Ogni input va validato al confine del sistema (principio "never trust external data") e trattato in modo immutabile, con particolare attenzione ai permessi di visibilità (un nuovo assunto non deve vedere ciò che non gli compete).

### 3.1 Conoscenza aziendale (contenuto da ingerire)

| Tipo di conoscenza | Formati / fonti supportati | Estrazione | Note |
|---|---|---|---|
| Documentazione di processo / policy | PDF, DOCX, Markdown, wiki (Confluence/Notion export, HTML) | Tika | Mantenere struttura in sezioni per citazione precisa |
| Procedure operative / runbook | Markdown, PDF, pagine wiki | Tika | Diventano nodi "Procedura/Runbook" collegati a sistemi e ruoli |
| Codice sorgente e convenzioni | Repository Git (README, CONTRIBUTING, ADR, commenti, struttura) | Parser dedicato + Tika | Conservare path/commit per citazione; estrarre dipendenze tra moduli |
| Decisioni architetturali (ADR) | Markdown, PDF | Tika | Nodi "Decisione" collegati a sistemi e motivazioni |
| Materiale formativo esistente | Slide (PPTX/PDF), corsi LMS, video tutorial | Tika + OCR + Whisper (trascrizione) | Conservare slide/minuto per citazione |
| Ticket / issue / knowledge base di supporto | Export Jira/GitHub Issues, FAQ, KB | Estrazione testo + metadati | Fonte ricca di problemi reali e soluzioni |
| Email e annunci interni | Modulo `email` (IMAP) | Tika + estrazione allegati | Avvisi, comunicazioni di processo (con permessi) |
| Organigramma / anagrafica persone | CSV/HRIS export, directory | Parser strutturato | Popola nodi Persona, Team, Ruolo |
| Glossario / gergo aziendale | CSV, Markdown, wiki | Parser | Cruciale per disambiguare il linguaggio interno |
| Registrazioni di sessioni / onboarding precedenti | Audio/video | `WhisperTranscriptionAdapter` (esistente) | Trascrizione con timestamp |

Requisiti trasversali sulla conoscenza:
- **Permessi e visibilità** dichiarati per ogni fonte: chi può vedere cosa. Il grafo deve poter filtrare i nodi/archi in base al ruolo e al livello del discente (un nuovo assunto non vede documenti riservati o aree non pertinenti).
- **Provenienza** sempre tracciata: ogni frammento e ogni entità deve poter risalire al file/commit/ticket e alla posizione di origine (sezione, riga, minuto).
- **Freschezza/versionamento**: ogni documento porta una data; il sistema deve segnalare conoscenza potenzialmente obsoleta (es. runbook non aggiornato da mesi mentre il sistema è cambiato).
- **Deduplica**: riconoscere conoscenza già ingerita (hash del contenuto) per evitare entità duplicate.
- **Lingua** rilevata automaticamente (IT/EN e oltre) per scegliere modello di embedding e lingua di risposta del tutor.
- **Dimensione massima per file** configurabile, con messaggio d'errore chiaro al superamento.

### 3.2 Struttura organizzativa (contesto)

Per dare senso alla conoscenza servono i metadati di struttura, forniti da HR/IT o dedotti e confermati:

- **Persone**: nome, ruolo, team, seniority, competenze, manager, area di expertise (knowledge owner).
- **Team / Dipartimenti**: nome, missione, responsabilità, persone afferenti, gerarchia.
- **Ruoli / Job profile**: descrizione del ruolo, responsabilità, competenze richieste — base per i template di percorso.
- **Sistemi / Servizi / Strumenti**: catalogo dei sistemi (microservizi, applicazioni, tool), con owner e dipendenze.
- **Processi aziendali**: passi, owner, sistemi coinvolti, documentazione collegata.
- **Calendario**: date di inizio (start date), sessioni formative, scadenze di compliance (integrabile col modulo `calendar`).

### 3.3 Definizione dei percorsi (template e personalizzazione)

Sono gli input che trasformano il grafo in un'esperienza guidata:

- **Template di percorso per ruolo/dipartimento**: la sequenza di tappe che un certo profilo deve completare (es. "Onboarding Developer Backend": setup ambiente → architettura → convenzioni codice → primo task → processo di deploy).
- **Tappe e attività**: ogni tappa collega concetti/competenze del grafo a materiali, autoverifiche, attività pratiche ("apri il repo X e fai girare i test") e persone di riferimento (buddy/mentor).
- **Prerequisiti tra tappe**: ordine obbligato (un argomento non viene proposto prima dei suoi prerequisiti).
- **Moduli di compliance/obbligatori**: con eventuale scadenza e ripasso periodico forzato (sicurezza, privacy, normative).
- **Assegnazione del mentor/buddy**: persona di riferimento collegata al percorso.

### 3.4 Dati personali del discente (stato di competenza)

Sono gli input che rendono il sistema *personale* e *adattivo*. Vanno trattati con la massima riservatezza (local-first, mai esfiltrati, accesso ristretto):

- **Profilo di ingresso**: ruolo assegnato, team, seniority, data di inizio, competenze pregresse dichiarate (per non far ripetere ciò che la persona già sa).
- **Obiettivo / traguardo**: "diventare operativo sul servizio X entro 60 giorni", "completare il modulo compliance entro la scadenza".
- **Stato di avanzamento**: tappe completate, in corso, da fare; autoverifiche superate.
- **Risultati di pratica**: esiti di quiz, checklist completate, attività pratiche convalidate — alimentano la stima dello stato di competenza (knowledge tracing) e quindi il ripasso.
- **Preferenze**: stile (pratico vs teorico), lingua delle risposte, ritmo.
- **Tempo disponibile**: ore/giorno dedicabili (utile per pianificare il carico).

### 3.5 Configurazione di sistema

- **Provider LLM e modello** (default Ollama locale; opzionale cloud con consenso esplicito), modello di embedding, lingua interfaccia (IT/EN).
- **Sorgenti di ingestione**: cartelle locali monitorate (folder watcher esistente), connettori plugin (Confluence, Jira, Git, LMS, HRIS, Drive).
- **Politiche di privacy/visibilità**: modello di permessi sul grafo (per ruolo, team, livello); cosa è pubblico interno, cosa è ristretto.
- **Parametri dei percorsi**: algoritmo di spaced repetition per la compliance, soglie di completamento, aggressività dei promemoria.
- **Integrazione con strumenti aziendali**: calendario, email, sistemi di notifica (`messaging`).

### 3.6 Feedback (loop continuo)

- **Correzioni al grafo** da parte dei knowledge owner: aggiungere/rimuovere/rietichettare nodi e archi, confermare o rifiutare i collegamenti suggeriti dall'AI (alimenta il peso degli archi, §5).
- **Valutazione delle risposte del tutor** (pollice su/giù, segnalazione di risposte fuori contesto, non fondate o basate su materiale obsoleto).
- **Segnalazione di conoscenza mancante o obsoleta** da parte dei discenti ("ho cercato X e non c'era / era sbagliato"), che genera task per i knowledge owner.
- **Feedback sui percorsi**: tappe poco chiare, mancanti, fuori ordine — retroagisce sul template.

### 3.7 Validazione e regole sugli input

- Tutti i file passano per validazione di tipo MIME, dimensione e integrità.
- I metadati obbligatori (es. ruolo per un percorso, owner per un sistema) sono richiesti; il resto è progressivamente arricchibile.
- Nessun input viene mai mutato in place: ogni revisione (es. correzione di un arco, aggiornamento di un runbook) crea una nuova versione, preservando la storia per audit e per il calcolo del peso.
- I permessi sono applicati **a livello di query sul grafo**: il discente non deve mai poter recuperare (nemmeno via tutor) contenuti fuori dal suo perimetro di visibilità.
- Le autovalutazioni di competenza sono sempre sovrascrivibili e incrociate con i dati di pratica, mai considerate verità assoluta.

## 4. Flusso dell'attività (step-by-step)

Il flusso descrive l'esperienza end-to-end, dalla preparazione del grafo aziendale fino all'onboarding del singolo e alla formazione ricorrente. È pensato per l'MVP ma indica anche i punti di evoluzione.

### Fase A — Preparazione della base di conoscenza (lato azienda, una tantum + manutenzione)

1. **Configurazione dell'istanza.** IT/Security installa LocalMind on-premise (self-hosting documentato), configura provider AI (default Ollama locale), lingua (IT/EN) e il modello di permessi/visibilità.
2. **Definizione della struttura organizzativa.** HR/IT importa o inserisce persone, team, ruoli, sistemi e processi (anche via CSV/HRIS). Questi diventano i nodi "scheletro" del grafo.
3. **Ingestione della conoscenza.** Si collegano le fonti: cartelle locali monitorate (folder watcher esistente), repository Git, export wiki/Confluence, ticket, materiale formativo, email (con permessi). Ogni fonte dichiara visibilità e proprietà.
4. **Estrazione e validazione.** Il sistema valida (tipo, dimensione), estrae il testo (Tika), applica OCR (Tesseract) e trascrive audio/video (Whisper). Gli errori sono riportati con messaggi chiari; un file non leggibile non blocca gli altri.
5. **Segmentazione ed embedding.** Il contenuto viene suddiviso in chunk (ChunkingService), vettorializzato e indicizzato su Qdrant con metadati di provenienza e di permesso; i documenti e i chunk sono persistiti su MySQL.

### Fase B — Costruzione del grafo della conoscenza interna

6. **Estrazione di entità e concetti.** Un job AI analizza i chunk e propone i nodi (Concetto/Competenza, Procedura, Sistema, Decisione, ecc.), deduplicando sinonimi e gergo.
7. **Deduzione delle relazioni.** L'AI propone gli archi: prerequisiti ("conoscere Git è prerequisito del processo di deploy"), responsabilità ("il team Pagamenti è owner del servizio Billing"), dipendenze ("il servizio Ordini dipende da Billing"), documentazione ("il runbook R documenta il sistema S"), expertise ("Elena è esperta di autenticazione"). Ogni arco nasce con un peso iniziale di confidenza.
8. **Revisione umana (human-in-the-loop).** I knowledge owner e i manager rivedono il grafo proposto e confermano, correggono, aggiungono o rimuovono nodi e archi. Le conferme aumentano il peso; i rifiuti lo riducono o eliminano. Passaggio cruciale per qualità e fiducia: la conoscenza tacita viene resa esplicita una volta sola.
9. **Visualizzazione interattiva.** Si naviga il grafo: dal nodo di un ruolo/sistema si espandono i vicini, si filtra per tipo di nodo/relazione, si evidenziano i percorsi di prerequisito e le mappe di responsabilità.

### Fase C — Definizione dei percorsi di onboarding/formazione

10. **Creazione dei template per ruolo.** HR/L&D e i manager definiscono i percorsi-tipo (es. "Developer Backend", "Sales Junior", "Compliance annuale"), collegando ogni tappa a concetti/competenze del grafo, materiali, autoverifiche, attività pratiche e persone di riferimento (buddy/mentor).
11. **Ordinamento per prerequisiti.** Il sistema usa gli archi `è_prerequisito_di` per validare e suggerire l'ordine delle tappe, evitando incoerenze.
12. **Marcatura dei moduli obbligatori.** I moduli di compliance ricevono scadenze e regole di ripasso periodico.

### Fase D — Onboarding del singolo (esperienza del nuovo assunto)

13. **Attivazione del percorso.** Alla creazione del profilo (ruolo, team, start date, competenze pregresse), il sistema istanzia il percorso adattato: salta ciò che la persona già padroneggia, ordina per prerequisiti, assegna il buddy.
14. **Esecuzione guidata, tappa per tappa.** Per ogni tappa il discente vede: spiegazione del tutor, materiali pertinenti (con citazione), attività pratiche concrete e l'autoverifica. La checklist mostra sempre "a che punto sono".
15. **Domanda al tutor in ogni momento.** Il discente pone domande in linguaggio naturale (es. "come faccio il deploy in staging?").
16. **Recupero GraphRAG.** Il sistema individua i nodi pertinenti, naviga il grafo per raccogliere il sotto-grafo rilevante (concetto + prerequisiti + sistema + owner) e recupera i chunk semanticamente più vicini da Qdrant, **filtrati per i permessi del discente**.
17. **Risposta fondata e citata.** L'AI (Ollama di default) genera la risposta usando *solo* la conoscenza interna pertinente, **citando le fonti esatte** (documento/sezione, commit, runbook, minuto) e i nodi/percorsi del grafo. Se la conoscenza non esiste o è fuori perimetro, lo dichiara e suggerisce a chi rivolgersi (nodo Persona esperto).
18. **Aggiornamento dello stato.** Gli esiti di autoverifiche e attività convalidate aggiornano la stima di competenza (knowledge tracing): le tappe deboli restano, quelle solide vengono chiuse.
19. **Feedback continuo.** Il discente valuta risposte e tappe; segnala conoscenza mancante/obsoleta, generando task per i knowledge owner.

### Fase E — Manager e knowledge owner nel ciclo

20. **Monitoraggio rispettoso della privacy.** Il manager vede l'avanzamento del percorso (tappe completate, blocchi ricorrenti) in forma utile all'accompagnamento, non come sorveglianza; le domande più frequenti dei nuovi assunti evidenziano lacune nella documentazione.
21. **Manutenzione del grafo.** I task generati dai feedback ("manca il runbook per X", "il processo Y è cambiato") arrivano ai knowledge owner, che aggiornano la conoscenza alla fonte; il grafo si arricchisce in modo incrementale.

### Fase F — Formazione continua e ripasso (oltre l'onboarding iniziale)

22. **Attivazione di percorsi di formazione ricorrente.** Per nuovi sistemi, processi, policy o upskilling, si attivano nuovi percorsi sugli stessi binari (grafo + tutor + autoverifiche).
23. **Ripasso delle competenze critiche (spaced repetition).** Le competenze obbligatorie/critiche (compliance, sicurezza) entrano in un calendario di ripetizione dilazionata; gli esiti riaggiustano gli intervalli. I promemoria passano per `messaging`/`calendar`/`email`.
24. **Re-onboarding e offboarding.** Quando una persona cambia ruolo, si attiva un percorso di transizione; quando una persona esce, il sistema aiuta a estrarre e formalizzare la sua conoscenza tacita prima che vada persa.

### Fase G — Evoluzione e governance

25. **Aggiornamento incrementale.** Nuova conoscenza ingerita estende il grafo senza ricostruirlo; il sistema propone i nuovi collegamenti e segnala conoscenza obsoleta.
26. **Audit e conformità.** Tutte le azioni (chi ha completato cosa, quando) sono tracciate per scopi di compliance, restando interne e local-first.

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa l'infrastruttura del **motore di knowledge graph core** (nodi tipizzati + archi pesati su MySQL per la struttura, Qdrant per la semantica). Di seguito i tipi specifici dell'ambito Onboarding & formazione.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi chiave |
|---|---|---|
| **Persona** | Dipendente, nuovo assunto, mentor, knowledge owner | nome, ruolo, team, seniority, aree di expertise |
| **Team / Dipartimento** | Unità organizzativa | nome, missione, responsabilità, gerarchia |
| **Ruolo / Job profile** | Profilo professionale | descrizione, responsabilità, competenze richieste |
| **Competenza / Skill** | Capacità o conoscenza richiesta da un ruolo | nome, livello (base/intermedio/avanzato), area |
| **Concetto** | Unità atomica di conoscenza interna | nome, definizione breve, sinonimi/gergo |
| **Documento / Materiale** | Risorsa di conoscenza ingerita | tipo, titolo, lingua, hash, data, provenienza, visibilità |
| **Chunk / Frammento** | Segmento di materiale (collegato ai vettori Qdrant) | testo, posizione, id vettore, permessi |
| **Procedura / Runbook** | Istruzioni operative | passi, sistema collegato, owner, ultima revisione |
| **Processo aziendale** | Flusso di lavoro end-to-end | passi, owner, sistemi coinvolti |
| **Sistema / Servizio / Strumento** | Componente tecnico o tool | nome, owner, dipendenze, ambiente |
| **Decisione (ADR)** | Decisione architetturale/organizzativa | titolo, contesto, scelta, motivazione |
| **Percorso di onboarding/formazione** | Sequenza ordinata di tappe | ruolo target, tappe, stato, scadenze |
| **Tappa / Modulo** | Unità di un percorso | titolo, materiali, attività, autoverifica, prerequisiti |
| **Autoverifica / Quiz** | Strumento di verifica competenza | domande, soluzioni, competenza misurata |
| **Evento / Scadenza** | Start date, sessione formativa, scadenza compliance | data, tipo, percorso collegato |
| **Glossario / Termine** | Voce di gergo aziendale | termine, definizione, sinonimi |

### 5.2 Tipi di relazione (archi)

| Relazione | Da → A | Significato | Direzionata |
|---|---|---|---|
| **è_prerequisito_di** | Competenza/Concetto → Competenza/Concetto/Tappa | A richiede la padronanza di B | Sì |
| **richiede_competenza** | Ruolo → Competenza | Il ruolo necessita di quella skill | Sì |
| **documenta** | Documento/Runbook → Sistema/Processo/Concetto | La risorsa spiega/descrive l'elemento | Sì |
| **è_responsabile_di / owner_di** | Persona/Team → Sistema/Processo/Documento | Proprietà/responsabilità operativa | Sì |
| **è_esperto_di** | Persona → Competenza/Sistema/Area | Punto di contatto per quell'area | Sì |
| **dipende_da** | Sistema → Sistema | Dipendenza tecnica/architetturale | Sì |
| **fa_parte_di** | Sistema/Persona → Team / Tappa → Percorso | Appartenenza/composizione | Sì |
| **copre / insegna** | Tappa/Materiale → Competenza/Concetto | La tappa sviluppa quella competenza | Sì |
| **verifica** | Quiz/Autoverifica → Competenza | Misura la padronanza | Sì |
| **mentor_di / buddy_di** | Persona → Persona | Affiancamento nell'onboarding | Sì |
| **fa_parte_del_processo** | Procedura/Sistema → Processo | Step di un processo | Sì |
| **succede_a / passo_dopo** | Tappa → Tappa | Ordine sequenziale nel percorso | Sì |
| **deriva_da / motiva** | Decisione → Sistema/Decisione | Razionale di una scelta | Sì |
| **collegato_a / correlato** | Concetto → Concetto | Affinità tematica (cross-team) | No |
| **obsoleto_rispetto_a** | Documento → Documento/Sistema | Segnala disallineamento/versione superata | Sì |

### 5.3 Criteri per il peso degli archi

Il peso (valore normalizzato, es. 0–1) esprime la **forza/affidabilità** della relazione e guida sia la visualizzazione (archi più spessi) sia il GraphRAG (priorità di esplorazione) sia l'ordinamento delle tappe. Il peso è calcolato come combinazione configurabile dei seguenti fattori, coerente con il principio core "peso derivato da fattori configurabili":

| Fattore | Effetto sul peso | Esempio |
|---|---|---|
| **Confidenza dell'estrazione AI** | Base iniziale dell'arco | L'LLM è molto sicuro che il servizio Ordini dipenda da Billing |
| **Conferma del knowledge owner** | Aumenta forte | Un SME conferma il collegamento → peso alto e "stabile" |
| **Rifiuto umano** | Azzera/rimuove | Un esperto rifiuta una dipendenza errata |
| **Autorevolezza della fonte** | Pesa "documenta"/"è_responsabile_di" | ADR ufficiale > thread di chat; documento firmato dall'owner > appunto |
| **Co-occorrenza nei materiali** | Aumenta | Due concetti/sistemi compaiono spesso negli stessi documenti |
| **Similarità semantica (Qdrant)** | Aumenta | Vicinanza vettoriale elevata tra i contenuti collegati |
| **Frequenza d'uso nei percorsi** | Aumenta | Arco attraversato spesso dai nuovi assunti del ruolo |
| **Frequenza nelle domande al tutor** | Aumenta | Molte domande seguono quel collegamento (rilevanza reale) |
| **Esiti di pratica / completamento** | Modula prerequisiti | Fallire la tappa Y quando manca la competenza X rafforza il prerequisito |
| **Freschezza / decadimento** | Riduce nel tempo | Runbook non aggiornato e non confermato decade; alimenta `obsoleto_rispetto_a` |

Regola di immutabilità: il peso non viene mutato in place sulla relazione; ogni rivalutazione produce una nuova versione del valore (con timestamp e fattori contribuenti), così da poter spiegare *perché* un arco ha quel peso (interpretabilità e audit, requisiti enterprise). La freschezza ha un ruolo speciale in questo dominio: la conoscenza aziendale invecchia, e un arco che decade può attivare la relazione `obsoleto_rispetto_a` e un task di revisione per il knowledge owner.

## 6. Fonti dati & connettori (ingestione)

| Fonte | Modalità | Stato | Note |
|---|---|---|---|
| **Upload manuale di file** | Drag&drop / selezione | MVP | Riusa `DocumentController.upload` e pipeline esistente |
| **Cartelle locali monitorate** | Folder watcher batch | MVP | `LocalFileSystemScanner` + Spring Batch già presenti |
| **Anagrafica/organigramma (CSV)** | Import strutturato | MVP | Popola Persona/Team/Ruolo |
| **Audio/video di sessioni** | Trascrizione | MVP/early | `WhisperTranscriptionAdapter` esistente |
| **Calendario (start date, sessioni, scadenze)** | Modulo `calendar` | Early | Eventi e scadenze come nodi Evento |
| **Email interne (avvisi, allegati)** | Modulo `email` (IMAP) | Early | Con permessi; estrazione allegati |
| **Repository Git** | Connettore plugin (PF4J) | Evoluzione | README/ADR/struttura, dipendenze tra moduli, commit per citazione |
| **Wiki aziendale (Confluence, Notion)** | Connettore plugin | Evoluzione | Sincronizzazione spazi/pagine con permessi |
| **Issue tracker (Jira, GitHub Issues)** | Connettore plugin | Evoluzione | Problemi reali, soluzioni, FAQ implicite |
| **LMS aziendale** | Connettore plugin | Evoluzione | Import corsi esistenti e completamenti |
| **HRIS / directory aziendale** | Connettore plugin | Evoluzione | Sincronizzazione persone, ruoli, team |
| **Cloud storage (Drive, SharePoint, OneDrive)** | Connettore plugin | Evoluzione | Cartelle documentali condivise |
| **Chat aziendale (Slack/Teams export)** | Connettore plugin | Evoluzione | Conoscenza tacita nei thread (con privacy stringente) |

Tutti i connettori esterni passano per il sistema **plugin PF4J + marketplace**, così da non gonfiare il core e rispettare la modularità per dominio. Ogni connettore deve dichiarare quali dati legge, con quali permessi e dove finiscono, coerentemente con la privacy local-first e con il requisito enterprise di non esfiltrazione.

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

### 7.1 MVP (prima release dell'ambito Onboarding & formazione)

| # | Funzionalità | Cosa comporta (backend / frontend) | Moduli toccati |
|---|---|---|---|
| 1 | **Dominio `onboarding` (o estensione di `knowledge`)** | Nuovi modelli di nodo/arco specifici (Persona, Ruolo, Competenza, Percorso, Tappa…), port in/out, service; wiring in `DomainConfig` | domain, infrastructure |
| 2 | **Gestione struttura organizzativa** | CRUD persone/team/ruoli/sistemi; import CSV; controller `/api/v1/onboarding/*`; UI feature standalone (Signals) | api, frontend, MySQL (Flyway, una query/file) |
| 3 | **Ingestione conoscenza interna con permessi** | Riuso pipeline Tika/OCR/Whisper + chunking + Qdrant, con metadati di provenienza e di visibilità | infrastructure, batch |
| 4 | **Estrazione entità/competenze & deduzione archi (AI)** | Job AI che produce nodi e archi con peso di confidenza; deduplica sinonimi/gergo | domain, infrastructure (Ollama) |
| 5 | **Revisione human-in-the-loop del grafo (knowledge owner)** | API e UI per confermare/correggere nodi e archi; aggiornamento pesi; coda di task di curatela | api, frontend |
| 6 | **Visualizzazione interattiva del grafo interno** | Vista grafo con espansione progressiva, filtri per tipo nodo/relazione, mappe di responsabilità e dipendenze | frontend |
| 7 | **Percorsi di onboarding per ruolo (template + istanza)** | Definizione template, istanziazione adattata al profilo, ordinamento per prerequisiti, tracciamento avanzamento, checklist | domain, api, frontend |
| 8 | **Tutor GraphRAG sulla conoscenza interna (con permessi)** | Recupero sotto-grafo + chunk Qdrant filtrati per visibilità; risposta con citazione fonti; "non lo so / chiedi a X"; integrazione chat esistente | domain (knowledge/llm), frontend (chat) |
| 9 | **Autoverifiche e quiz base** | Generazione quiz dai concetti/competenze; registrazione esiti; aggiornamento stato tappa | domain, api, frontend |
| 10 | **Modello di permessi/visibilità sul grafo** | Filtro per ruolo/team/livello applicato a query e tutor | domain, infrastructure, api |
| 11 | **i18n IT/EN** | Tutte le enum (tipi nodo/arco, stati percorso, ruoli) tradotte e instradate al frontend secondo lo switch lingua | api, frontend |

### 7.2 Evoluzioni (release successive)

| # | Funzionalità | Valore aggiunto |
|---|---|---|
| 12 | **Percorso pienamente adattivo (knowledge tracing)** | Stato di competenza per skill, aggiornato dagli esiti; percorso che salta ciò che è già padroneggiato |
| 13 | **Spaced repetition per compliance/competenze critiche** | Ripasso pianificato delle competenze obbligatorie con promemoria |
| 14 | **Connettori aziendali (Git, Confluence, Jira, LMS, HRIS) via PF4J** | Ingestione automatica e sincronizzazione continua delle fonti |
| 15 | **Cruscotto manager / HR (analytics aggregati e anonimi)** | Time-to-productivity, tappe critiche, lacune documentali, completamenti compliance |
| 16 | **Mentor/buddy matching** | Suggerimento del miglior referente in base a expertise nel grafo |
| 17 | **Rilevamento conoscenza obsoleta** | L'AI segnala runbook/documenti disallineati rispetto ai sistemi cambiati |
| 18 | **Suggerimento di lacune e collegamenti mancanti** | L'AI propone documentazione mancante (domande senza fonte) e link non evidenti tra team/sistemi |
| 19 | **Offboarding / cattura della conoscenza tacita** | Estrazione guidata del sapere di chi esce prima della perdita |
| 20 | **Marketplace di percorsi/grafi-template** | Template di onboarding per ruoli comuni condivisibili come moduli |
| 21 | **Agente di onboarding (orchestrazione via `agent`/`mcp`)** | Tutor-agente che orchestra ricerca, quiz, promemoria e attività |

### 7.3 Da mantenere (manutenzione continua)

- Pipeline di ingestione (parser Tika, lingue OCR, modelli Whisper) e connettori plugin verso API esterne che cambiano (Confluence, Jira, Git, HRIS).
- Prompt e logica GraphRAG (qualità estrazione entità/relazioni, citazione fonti, rispetto dei permessi).
- Schema del grafo e migrazioni Flyway (una query per file), con evoluzione retro-compatibile.
- Modello di permessi/visibilità, fondamentale e sensibile: ogni nuova feature deve rispettarlo.
- Traduzioni IT/EN di enum e UI a ogni nuova feature.
- Tuning dei fattori di peso, del decadimento (freschezza) e degli algoritmi di spaced repetition sulla base del feedback reale.
- Igiene del grafo: nodi orfani, duplicati, conoscenza obsoleta segnalata da `obsoleto_rispetto_a`.

## 8. Casi d'uso AI / GraphRAG

1. **Tutor fondato sulla conoscenza interna.** "Come faccio il deploy del servizio Ordini in staging?" → L'AI naviga i nodi Sistema (Ordini), Procedura (runbook di deploy) e i prerequisiti, recupera i chunk pertinenti e risponde citando "Runbook deploy, sezione Staging" e il commit di riferimento. Tutto in locale con Ollama, filtrato dai permessi.
2. **Mappa delle responsabilità.** "Chi è l'esperto dell'autenticazione?" → L'AI segue gli archi `è_esperto_di`/`owner_di` e indica la persona (e il team) di riferimento, con i sistemi che presidia.
3. **Onboarding adattivo.** "Sono un nuovo backend developer, da dove comincio?" → GraphRAG costruisce/recupera il percorso del ruolo, salta ciò che il profilo dichiara di conoscere, ordina per prerequisiti e propone la prima tappa con materiali e attività.
4. **Domanda multi-hop cross-team.** "Se cambio il formato dei pagamenti, cosa impatto?" → L'AI naviga gli archi `dipende_da` e mostra i sistemi a valle e i team owner da coinvolgere.
5. **Individuazione di lacune documentali.** Per HR/knowledge owner: "Quali domande dei nuovi assunti restano senza fonte?" → L'AI aggrega le query del tutor prive di risposta fondata e propone la documentazione mancante.
6. **Rilevamento conoscenza obsoleta.** L'AI segnala: "Il runbook R cita una variabile rimossa nel servizio S tre mesi fa: probabile obsolescenza, vuoi aprire un task di revisione?".
7. **Generazione di quiz e checklist** dalle competenze richieste dal ruolo, con difficoltà calibrata, per le autoverifiche delle tappe.
8. **Riassunto strutturato di un'area** seguendo la gerarchia `fa_parte_di` e i prerequisiti (es. "panoramica dell'architettura per il nuovo team"), con citazioni puntuali.
9. **Suggerimento di mentor/buddy** in base alla vicinanza nel grafo tra le competenze da acquisire e gli esperti disponibili.
10. **Cattura della conoscenza in offboarding.** L'AI intervista chi esce sui nodi di cui è owner privo di documentazione, e propone bozze di runbook da validare.

## 9. KPI & metriche di successo

| Categoria | Metrica | Obiettivo indicativo |
|---|---|---|
| **Efficacia onboarding** | Time-to-productivity (giorni al raggiungimento della piena operatività) | Riduzione misurabile vs baseline (es. da 90 a 60 giorni per ruoli tecnici) |
| **Autonomia** | Domande risolte dal tutor senza interruzione di colleghi | Quota crescente; riduzione del tempo-tutor umano |
| **Qualità del tutor** | % risposte valutate utili e fondate (con citazione corretta) | ≥ 80% pollice su |
| **Fondatezza** | % risposte con almeno una citazione verificabile (quando la fonte esiste) | ≥ 95% |
| **Qualità del grafo** | % archi suggeriti confermati dai knowledge owner | ≥ 60% accettazione al netto delle correzioni |
| **Copertura documentale** | % domande del tutor con fonte disponibile | In crescita; lacune che si chiudono nel tempo |
| **Avanzamento** | % tappe completate / percorsi conclusi nei tempi | Alta aderenza ai template |
| **Compliance** | % moduli obbligatori completati entro scadenza; aderenza al ripasso | ≥ 95% completamento |
| **Retention precoce** | Permanenza dei nuovi assunti a 6/12 mesi | Miglioramento vs baseline |
| **Freschezza** | Quota di conoscenza segnalata obsoleta e poi aggiornata | Tendenza alla riduzione dello stock obsoleto |
| **Performance** | Latenza risposta tutor in locale (Ollama) | Accettabile su hardware on-premise (target pochi secondi al primo token) |
| **Privacy** | Dati inviati a cloud senza consenso | Zero (vincolo, non obiettivo) |

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---|---|---|
| **Conoscenza obsoleta nel grafo** | Il tutor risponde con informazioni superate, danno operativo | Tracciamento freschezza; decadimento dei pesi; relazione `obsoleto_rispetto_a`; task di revisione ai knowledge owner; preferenza a fonti autorevoli e recenti |
| **Estrazione rumorosa di entità/relazioni** | Grafo poco affidabile, sfiducia | Human-in-the-loop obbligatorio per i knowledge owner; deduplica; soglie di confidenza; rigenerazione |
| **Allucinazioni del tutor** | Risposte sbagliate, errori operativi | GraphRAG vincolato alla conoscenza interna; citazione obbligatoria; risposta "non presente / chiedi a X" quando manca la fonte |
| **Violazione dei permessi** (un assunto vede ciò che non deve) | Fuga di informazioni interne, rischio compliance | Permessi applicati a livello di query e di tutor; test dedicati; default restrittivo; audit degli accessi |
| **Privacy e sovranità dei dati** | Blocco all'adozione enterprise | Local-first rigoroso; self-hosting; nessuna telemetria; cifratura a riposo; consenso esplicito per qualsiasi provider cloud |
| **Costo computazionale locale** (LLM/embedding on-premise) | Lentezza, frustrazione | Modelli Ollama dimensionati; ingestione in batch in background; caching; opzione cloud con consenso |
| **Curatela trascurata** (knowledge owner non revisionano) | Grafo non rifinito, conoscenza non depositata | Suggerimenti a basso attrito (un clic accetta/rifiuta); coda di task prioritizzata; incentivi e riconoscimento; valori di default sensati |
| **Sovraccarico cognitivo del grafo** (troppi nodi) | Mappa illeggibile | Espansione progressiva, filtri, clustering per team/area, viste focalizzate per ruolo |
| **Affidabilità di MySQL+Qdrant su query di grafo profonde** | Percorsi/dipendenze lenti su grafi grandi | Indicizzazione mirata, materializzazione di percorsi frequenti, limiti di profondità; rivalutare grafo dedicato solo se necessario (vincolo di progetto) |
| **Resistenza al cambiamento** (preferenza al "chiedere al collega") | Bassa adozione | UX a basso attrito integrata nel flusso di lavoro; sponsorship del management; dimostrare valore con quick win |
| **Multilingua** (conoscenza mista IT/EN) | Entità non collegate tra lingue | Embedding multilingue; mapping di sinonimi/gergo cross-lingua; enum e UI bilingui |

## 11. Manutenzione & evoluzione

- **Aggiornamento incrementale del grafo.** Nuova conoscenza ingerita estende il grafo esistente; un job periodico ricalcola i collegamenti candidati e propone aggiunte senza ricostruzioni distruttive.
- **Gestione attiva della freschezza.** Routine periodiche confrontano la data dei documenti con i cambiamenti nei sistemi/processi e segnalano obsolescenza (`obsoleto_rispetto_a`), generando task di revisione mirati.
- **Decadimento e igiene del grafo.** Archi non confermati e non usati decadono; routine segnalano nodi orfani, duplicati e collegamenti deboli da rivedere.
- **Versionamento dello schema.** Ogni evoluzione dei tipi di nodo/arco passa per migrazioni Flyway retro-compatibili (una query per file), con strategia di backfill documentata.
- **Tuning di modelli e prompt.** Aggiornamento periodico dei prompt di estrazione e GraphRAG, dei modelli Ollama consigliati e dei parametri di chunking, guidato dalle metriche di §9.
- **Calibrazione di pesi e spaced repetition.** I fattori di peso (§5.3) e gli intervalli di ripasso delle competenze critiche vengono affinati sui dati di utilizzo reali, mantenendo l'interpretabilità per l'audit.
- **Governance dei permessi.** Revisione periodica del modello di visibilità man mano che ruoli e team evolvono; ogni nuova feature deve superare i test di non-fuga di informazioni.
- **Compatibilità connettori.** Monitoraggio delle API esterne (Git, Confluence, Jira, LMS, HRIS) e aggiornamento dei plugin PF4J corrispondenti.
- **Documentazione bilingue.** Ogni feature aggiorna documentazione IT/EN e i log in `Sviluppi/` secondo le convenzioni di progetto.
- **Roadmap di valutazione.** Introdurre nel tempo un set di valutazione (golden questions per ruolo/area) per misurare regressioni nella qualità del tutor e dell'estrazione.

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo esistente | Ruolo nell'ambito Onboarding & formazione |
|---|---|
| **knowledge** | Base del motore a grafo: estensione con tipi di nodo/arco di onboarding; punto naturale dove innestare il dominio |
| **document** | Ingestione conoscenza interna (upload, Tika, OCR Tesseract), chunking, metadati di provenienza e visibilità |
| **llm** | Tutor ed estrazione entità/relazioni via `LlmGatewayService`; Ollama default, fallback cloud opzionale con consenso |
| **(Qdrant) vectorstore** | Indice semantico dei chunk per il recupero GraphRAG, con metadati di permesso |
| **batch** | Job di ingestione e folder watcher; job periodici di ricalcolo del grafo e di rilevamento obsolescenza |
| **calendar** | Start date, sessioni formative e scadenze di compliance come nodi Evento; pianificazione di percorso e ripasso |
| **email** | Ingestione di avvisi e allegati interni (con permessi); invio di promemoria di onboarding/compliance |
| **messaging** | Notifiche e promemoria su canali aziendali (tappe, scadenze, ripasso) |
| **mcp** | Esposizione di tool (interrogare il grafo, generare quiz, cercare l'esperto) ad agenti esterni |
| **agent** | Agente di onboarding che orchestra ricerca sul grafo, quiz, promemoria e attività pratiche |
| **automation** | Trigger automatici: "nuovo assunto creato → istanzia percorso", "scadenza compliance tra 7 giorni → ripasso", "nuovo documento → estrai entità" |
| **marketplace + plugin (PF4J)** | Connettori Git/Confluence/Jira/LMS/HRIS/Drive e template di percorso/grafo condivisibili come moduli installabili |
| **finetuning** | Eventuale adattamento di modelli locali al lessico e ai processi aziendali (avanzato) |
| **auth** | Identità local-first e, soprattutto, base per il modello di permessi/visibilità del grafo |
| **common** | Eventi di dominio ("conoscenza ingerita", "tappa completata", "competenza acquisita"), analytics aggregati e anonimi, gestione errori |
| **Frontend (Angular 21)** | Nuova feature `onboarding` standalone con Signal store, vista grafo interattiva, tutor, percorsi e checklist, cruscotti manager/HR; i18n IT/EN |

L'ambito Onboarding & formazione interna è quindi un **verticale enterprise** del motore universale: riusa interamente l'infrastruttura esistente (ingestione, embedding, LLM, grafo, plugin, automazione, calendario, email) e aggiunge solo i tipi di nodo/relazione, le funzionalità di percorso e l'esperienza utente specifiche dell'inserimento e della formazione — coerente con il principio "una piattaforma, più ecosistemi", restando local-first, gratuito, privato (privacy enterprise non negoziabile), auditabile e bilingue.

---

### Fonti & riferimenti di ricerca

- D2L — *LMS Platforms for Effective Employee Onboarding in 2026*: https://www.d2l.com/blog/lms-for-employee-onboarding/
- 360Learning — *The 10 Best Employee Onboarding LMS Solutions for 2026*: https://360learning.com/blog/employee-onboarding-lms/
- Kairntech — *Employee Onboarding AI: The Complete Guide for 2026* (RAG su knowledge base, risposte source-backed): https://kairntech.com/blog/articles/employee-onboarding-ai-the-complete-guide-for-2026/
- Enboarder — *AI Onboarding Tools 2026* / *Onboarding Trends 2026*: https://enboarder.com/blog/ai-onboarding-tool-guide-2026/ , https://enboarder.com/blog/future-onboarding-trends/
- Medium (Tongbing) — *GraphRAG in 2026: A Practical Buyer's Guide to Knowledge-Graph–Augmented RAG* (multi-hop, cross-document, grounding strutturato): https://medium.com/@tongbing00/graphrag-in-2026-a-practical-buyers-guide-to-knowledge-graph-augmented-rag-43e5e72d522d
- MDPI Electronics — *Personalized Learning Path Recommendation Based on Knowledge Graphs: A Survey*: https://www.mdpi.com/2079-9292/15/1/238
- arXiv 2506.22303 / AAAI — *GraphRAG-Induced Dual Knowledge Structure Graphs for Personalized Learning Path Recommendation* (relazioni di prerequisito e similarità): https://arxiv.org/abs/2506.22303
- CGS Immersive — *Measure Onboarding With Time to Productivity*: https://cgsimmersive.com/blog/measure-onboarding-effectiveness-with-employee-time-to-productivity
- AllenComm — *Successful Onboarding: Time-to-Productivity + Early Performance Signals*: https://www.allencomm.com/2026/04/successful-onboarding-time-to-productivity-early-performance-signals/
- AIHR — *Employee Onboarding Statistics & Trends 2026* (retention +52%, produttività +60%, +58% permanenza a 3 anni): https://www.aihr.com/blog/employee-onboarding-statistics/
- Phenom — *15 Onboarding Trends for 2026: AI, Skills & New Hire Success*: https://www.phenom.com/blog/onboarding-trends-ai-skills
- KMSlh — *Top Knowledge Management Tools for Onboarding in 2026*: https://kmslh.com/blog/knowledge-management-software-for-employee-onboarding/
</content>
</invoke>
