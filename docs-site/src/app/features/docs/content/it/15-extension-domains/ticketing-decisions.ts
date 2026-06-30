export const content = `# Ticketing & decisioni (ADR)

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema della memoria decisionale che si dissolve

In ogni organizzazione che sviluppa software o gestisce processi complessi accade lo stesso fenomeno silenzioso: **le decisioni vengono prese, eseguite e poi dimenticate**, mentre il *perché* di quelle decisioni — il razionale, il contesto, le alternative scartate, i vincoli del momento — evapora. Sei mesi dopo, nessuno ricorda perché si è scelto PostgreSQL invece di MongoDB, perché quel microservizio è stato spezzato in due, perché quel bug è stato "risolto" disabilitando una feature anziché correggendola. La conoscenza esiste, ma è **frammentata, non collegata e non interrogabile**:

- **I ticket vivono in silos.** Le issue stanno su Jira, GitHub Issues, GitLab, Azure DevOps o Redmine. Ogni ticket racconta un pezzo di storia (un bug, una richiesta, un task), ma i collegamenti tra ticket — "questo bug è causato da quella modifica", "questa feature dipende da quell'epic" — sono deboli, manuali o inesistenti. Il sistema di ticketing conosce gli stati e gli assegnatari, ma non sa *ragionare* sulla rete di cause ed effetti.
- **Le decisioni architetturali (ADR) sono documenti morti.** Quando esistono, gli Architecture Decision Records sono file Markdown sparsi in una cartella \`docs/adr/\` o pagine di Confluence. Catturano la singola decisione, ma non sono collegati ai ticket che li hanno motivati, ai servizi che impattano, al codice che ne deriva, né alle decisioni successive che li hanno superati. Un ADR "Superseded" non punta quasi mai esplicitamente all'ADR che lo sostituisce in modo navigabile.
- **Il razionale (rationale) non è tracciato.** Il valore di una decisione non è la scelta in sé, ma il **ragionamento**: il contesto, le forze in gioco, i trade-off, le alternative valutate e perché sono state respinte. Questo razionale, oggi, sopravvive nella testa di chi era presente alla riunione, in thread di chat persi, in commit message laconici. Quando quella persona lascia l'azienda, il razionale se ne va con lei.
- **La catena causa-effetto è invisibile.** "Perché il deploy di venerdì ha rotto il checkout?" La risposta richiede di collegare: l'incident → il ticket di bug → la PR → il commit → la decisione architetturale che ha introdotto quel componente → l'epic che richiedeva quella feature. Oggi questa catena va ricostruita a mano, scavando in cinque strumenti diversi, e spesso non si ricostruisce affatto.
- **Le decisioni si ripetono e si contraddicono.** Senza una memoria decisionale navigabile, team diversi (o lo stesso team in momenti diversi) prendono decisioni incoerenti o ridiscutono questioni già risolte. Si reinventa la ruota, oppure si introduce un'incoerenza architetturale perché nessuno sapeva che esisteva già una decisione contraria.
- **L'onboarding è doloroso.** Un nuovo assunto, o un team che eredita un sistema, non ha modo di capire "come siamo arrivati qui". Deve fare archeologia su ticket chiusi, leggere migliaia di righe di documentazione disallineata e intervistare i veterani. Il costo è settimane di produttività persa e decisioni prese senza il contesto storico.
- **L'audit e la compliance sono fragili.** In contesti regolati (finance, sanità, pubblica amministrazione) serve dimostrare *perché* è stata presa una decisione, chi l'ha approvata, su quali basi. Senza un tracciamento strutturato del razionale, l'audit diventa un esercizio di ricostruzione a posteriori, costoso e inaffidabile.

Il filo conduttore è uno solo: **esiste un enorme patrimonio di conoscenza causale e decisionale che resta inerte** perché non è modellato come una rete di nodi e relazioni pesate, e perché nessuno strumento permette all'AI di navigarlo per rispondere a domande del tipo "perché", "cosa succede se", "cosa è collegato a".

### 1.2 La nostra risposta: il grafo causale di decisioni e ticket

LocalMind, nella sua evoluzione a **motore di knowledge graph universale**, affronta esattamente questo problema per il gruppo enterprise. La proposta di valore dell'ambito "Ticketing & decisioni (ADR)" è costruire un **grafo causale e decisionale** dell'organizzazione: una rete pesata in cui i nodi sono ticket, issue, epic, bug, incident, decisioni architetturali (ADR), alternative, requisiti, persone e artefatti tecnici (servizi, repository, commit, PR, componenti), e gli archi rappresentano relazioni semanticamente ricche — "causa", "blocca", "dipende-da", "motiva", "implementa", "supera", "scarta-alternativa", "impatta". Su questo grafo si innestano tre capacità:

1. **Cattura strutturata del razionale.** Ogni decisione (ADR) non è più un file isolato ma un nodo del grafo, collegato al contesto che l'ha generata (i ticket, gli incident, i requisiti), alle alternative considerate (nodi a sé, con i motivi di esclusione), e alle conseguenze (i servizi creati, le decisioni successive). Il *perché* diventa un cittadino di prima classe, navigabile e versionato.
2. **Tracciamento causa-effetto navigabile.** I ticket vengono ingeriti dai sistemi esistenti (Jira, GitHub, GitLab…) e collegati tra loro e agli artefatti tecnici. La catena incident → bug → commit → decisione diventa un percorso esplicito sul grafo, percorribile in entrambe le direzioni (dall'effetto alla causa e viceversa).
3. **AI che ragiona sulla rete decisionale (GraphRAG).** L'AI (Ollama in locale per default) risponde a domande complesse — "perché abbiamo scelto X?", "cosa rischiamo se rimuoviamo Y?", "quali decisioni dipendono da questo ADR ormai superato?" — navigando il grafo e recuperando i frammenti semanticamente pertinenti da Qdrant, **citando sempre i nodi e i percorsi usati** (ticket, ADR, commit). Nessun dato aziendale lascia l'infrastruttura senza consenso esplicito.

In sintesi: trasformiamo la conoscenza decisionale da **documentazione passiva e silos** a **grafo causale interrogabile**, dove il razionale è preservato, le catene causa-effetto sono esplicite e l'AI fa da memoria istituzionale viva dell'organizzazione.

### 1.3 Perché LocalMind è la piattaforma giusta

| Esigenza enterprise | Caratteristica LocalMind che la soddisfa |
|---|---|
| Riservatezza assoluta di decisioni, incident, dati di progetto | **Local-first / self-hosted**: tutto gira on-premise; nessun invio a cloud senza consenso esplicito |
| Nessun lock-in, costi prevedibili | **Open source puro**, AI locale Ollama di default, nessun paywall |
| Ticket e ADR in formati e strumenti eterogenei | Pipeline di ingestione esistente (**Tika**, parser) + connettori plugin per Jira/GitHub/GitLab |
| Risposte fondate sui *propri* ticket e decisioni | **GraphRAG** sul grafo causale + ricerca semantica Qdrant, con citazione delle fonti |
| Privacy by design su dati sensibili (audit, compliance) | Dati mai esfiltrati; tracciamento immutabile e versionato delle decisioni |
| Documentazione e interfaccia in più lingue | Piattaforma **bilingue IT/EN** by design, enum tradotte |
| Estensibilità verso nuovi tool aziendali | Sistema **plugin PF4J** + marketplace |
| Integrazione con il resto della conoscenza interna | Stesso motore a grafo che serve documenti, repo, microservizi, persone, mail |

Il differenziatore competitivo è netto. Strumenti come Jira tracciano lo *stato* dei ticket ma non il razionale né le catene causali profonde; i plugin "dependency graph" per Jira visualizzano i link ma non li rendono interrogabili da un'AI; i tool ADR (adr-tools, Log4brains) producono documenti ma non un grafo navigabile collegato ai ticket e al codice; le soluzioni di root-cause analysis (es. su Grafana) coprono l'osservabilità runtime ma non la *memoria decisionale*. La ricerca 2025-2026 conferma la direzione: framework come **ADR-E** (Explainable ADR) introducono razionale strutturato, alternative scartate e link di tracciabilità ispirati ai principi di explainability, e approcci GraphRAG su ticket Jira modellano relazioni esplicite come \`caused_by\`, \`clone_of\`, \`related_to\`. LocalMind unisce questi mondi — **grafo causale esplicito + GraphRAG locale + privacy totale a costo zero** — in un'unica piattaforma self-hostable, cosa che nessun concorrente offre insieme.

## 2. Personas & utenti target

| Persona | Profilo | Bisogni primari | Come usa LocalMind |
|---|---|---|---|
| **Elena, Software Architect / Tech Lead** | Responsabile delle scelte architetturali di uno o più team | Documentare decisioni con razionale, evitare incoerenze, mostrare l'evoluzione | Crea e collega ADR sul grafo, naviga le dipendenze tra decisioni e servizi, usa l'AI per verificare coerenza con decisioni passate |
| **Marco, Engineering Manager** | Coordina più team e backlog | Capire perché siamo arrivati a una situazione, stato delle dipendenze tra epic | Interroga il grafo su catene causa-effetto, priorità bloccate, decisioni in sospeso |
| **Sara, Senior Developer** | Implementa feature e risolve bug | Capire il contesto storico di un componente prima di modificarlo | Parte da un servizio/repo e risale a decisioni e ticket che lo hanno plasmato; collega PR e commit alle decisioni |
| **Luca, SRE / On-call** | Gestisce incident in produzione | Root-cause rapida: cosa ha causato l'incident, quali decisioni sono coinvolte | Durante l'incident segue gli archi \`causa\`/\`impatta\` dall'incident al commit/decisione responsabile |
| **Giulia, Product Owner** | Gestisce requisiti e backlog di prodotto | Tracciare requisiti → decisioni → implementazione, giustificare scelte agli stakeholder | Collega requisiti agli ADR e ai ticket, mostra il razionale alle revisioni di prodotto |
| **Davide, Auditor / Compliance Officer** | Verifica conformità e tracciabilità | Dimostrare chi ha deciso cosa, quando e perché | Esplora il log decisionale versionato, esporta la catena di approvazioni e razionali |
| **Anna, Nuovo assunto / team ereditario** | Deve capire un sistema esistente | Onboarding rapido sul "come siamo arrivati qui" | Chiede all'AI la storia decisionale di un'area, naviga il grafo per orientarsi |
| **Team distribuito / open source maintainer** | Collaboratori in fusi e momenti diversi | Memoria condivisa e asincrona delle decisioni | Consultano e arricchiscono il grafo decisionale come fonte di verità comune |

L'utente primario per l'MVP è **Elena (Architect/Tech Lead)** affiancata da **Sara (Developer)**: sono i produttori e i primi consumatori del razionale, con il massimo bisogno di collegare decisioni, ticket e codice. Le altre personas guidano le evoluzioni (incident/SRE, compliance, onboarding).

## 3. Requisiti in input

Questa sezione definisce in dettaglio **cosa deve poter entrare nel sistema** perché l'ambito Ticketing & decisioni funzioni. Gli input si dividono in: ticket e issue, decisioni architetturali, artefatti tecnici collegabili, metadati di relazione, persone/organizzazione, configurazione e feedback. Ogni input va validato al confine del sistema (principio "never trust external data") e trattato in modo immutabile (ogni revisione crea una nuova versione, non muta l'esistente).

### 3.1 Ticket, issue e item di lavoro (contenuto da ingerire)

| Tipo di item | Campi essenziali | Fonte tipica | Note |
|---|---|---|---|
| **Bug** | titolo, descrizione, passi per riprodurre, severità, stato, assegnatario, componente | Jira, GitHub/GitLab Issues, Redmine | Cuore del tracciamento causale (cosa si è rotto e perché) |
| **Feature / Story** | titolo, descrizione, criteri di accettazione, stato, epic di appartenenza | Jira, Azure DevOps | Collega requisiti e implementazione |
| **Epic / Iniziativa** | titolo, obiettivo, ambito, stato, story collegate | Jira, GitHub Projects | Nodo aggregante di alto livello |
| **Task / Sotto-task** | titolo, descrizione, stato, parent | Tutti | Granularità operativa |
| **Incident / Postmortem** | descrizione, timeline, severità, servizi impattati, azioni correttive | PagerDuty, Opsgenie, doc interni | Punto di partenza tipico della root-cause |
| **Change Request** | descrizione, motivazione, rischio, approvazioni | ITSM / ServiceNow | Rilevante per compliance |
| **Commento / Discussione** | autore, testo, timestamp, ticket di riferimento | Tutti | Spesso contiene il razionale "nascosto" |

Requisiti trasversali sui ticket:
- **Provenienza sempre tracciata**: ogni nodo deve risalire al sistema d'origine, all'ID esterno e all'URL (per il deep-link e l'aggiornamento incrementale).
- **Stati e tipi normalizzati**: gli stati eterogenei dei vari tool (es. "In Progress", "Doing", "WIP") vanno mappati su un insieme canonico di stati, con enum **tradotta IT/EN** verso il frontend.
- **Storia degli stati**: le transizioni di stato (created → in progress → resolved → closed) sono input preziosi per i pesi e per le timeline causali; vanno conservate come eventi datati.
- **Deduplica**: riconoscere lo stesso ticket importato più volte (per ID esterno + sistema) per evitare nodi duplicati.
- **Lingua** rilevata automaticamente (IT/EN e oltre) per embedding e risposte dell'AI.

### 3.2 Decisioni architetturali (ADR) e razionale

Il razionale è il cuore dell'ambito. Una decisione, per essere un nodo utile, deve poter contenere (sul modello MADR / ADR-E):

- **Titolo e ID** della decisione (es. "ADR-0042: Adottare event sourcing per il modulo ordini").
- **Stato**: Proposed, Accepted, Rejected, Deprecated, Superseded — enum **bilingue IT/EN**.
- **Contesto / Problema**: la situazione e le forze in gioco che hanno reso necessaria la decisione.
- **Decisione**: cosa si è scelto di fare.
- **Razionale / Conseguenze**: perché, con i trade-off positivi e negativi.
- **Alternative considerate**: ciascuna come elemento distinto, con il motivo dell'esclusione (input chiave per le domande "perché non X?").
- **Stakeholder e approvatori**: chi ha proposto, chi ha deciso, chi ha approvato (per audit).
- **Riferimenti**: ticket, incident, requisiti, documenti, commit/PR che motivano o derivano dalla decisione.
- **Data e versione**: ogni modifica del contenuto produce una nuova versione (immutabilità, audit trail).

Modalità di input degli ADR:
1. **Creazione guidata da UI** (form strutturato con i campi sopra, in stile MADR) — modalità nativa MVP.
2. **Import da file Markdown** esistenti (cartelle \`docs/adr/\`, formato Nygard/MADR) con parsing dei campi.
3. **Proposta assistita dall'AI**: a partire da un ticket/incident, l'AI suggerisce una bozza di ADR da rivedere (evoluzione).

### 3.3 Artefatti tecnici collegabili (contesto enterprise)

Il valore causale emerge collegando ticket e decisioni al mondo tecnico, riusando gli altri ambiti enterprise del grafo:

- **Repository Git, commit, Pull/Merge Request** (collegamento bidirezionale ticket↔codice tramite ID nei messaggi di commit/PR).
- **Microservizi, API, componenti, moduli** (cosa una decisione crea, modifica o deprecca).
- **Documenti** (specifiche, RFC, design doc già ingeriti dal dominio \`document\`).
- **Database, infrastruttura, ambienti** (cosa un cambio impatta).

Questi artefatti possono già esistere come nodi nel grafo (provenienti da altri ambiti enterprise) oppure essere creati on-demand come nodi "leggeri" referenziati.

### 3.4 Metadati di relazione (i collegamenti da catturare)

Oltre ai contenuti, l'input più prezioso sono le **relazioni esplicite** già presenti nei sistemi d'origine, da importare e poi arricchire:
- Link nativi di Jira/GitHub: \`blocks\`, \`is blocked by\`, \`relates to\`, \`duplicates\`, \`caused by\`, \`clones\`, parent/child (epic→story→task).
- Riferimenti incrociati nei testi (es. "vedi #123", "fixes JIRA-456") da estrarre con parsing.
- Collegamenti commit↔ticket (smart commits) e PR↔issue.
- Collegamenti decisione↔decisione (supersedes / superseded by).

### 3.5 Persone e organizzazione

- **Persone**: autori, assegnatari, reviewer, approvatori, reporter — come nodi \`Persona\` (riusabili da altri ambiti enterprise, es. mail/HR).
- **Team / unità organizzative**: proprietà di servizi e decisioni.
- **Ruoli**: chi può approvare un ADR, chi può modificare il grafo (governance).

### 3.6 Configurazione di sistema

- **Provider LLM e modello** (default Ollama locale; cloud opzionale con consenso esplicito), modello di embedding, lingua interfaccia (IT/EN).
- **Connettori attivi e credenziali** (Jira/GitHub/GitLab…): URL, token, progetti/repo da sincronizzare, frequenza di sync.
- **Mapping di normalizzazione**: stati, tipi e link dei sistemi esterni → enum canoniche LocalMind.
- **Politiche di privacy/visibilità**: quali progetti sono privati, chi vede cosa, cosa è escluso dall'ingestione.
- **Pesi e governance**: chi può approvare ADR, soglie di confidenza per i link suggeriti dall'AI, regole di decadimento.

### 3.7 Feedback (loop continuo)

- **Conferma/rifiuto dei link suggeriti dall'AI** (es. "questo bug è davvero causato da quel commit?") — alimenta il peso degli archi (§5).
- **Correzioni al grafo**: aggiungere/rimuovere/rietichettare nodi e archi.
- **Valutazione delle risposte dell'AI** (utile/fuori contesto, citazione corretta).
- **Marcatura di superamento**: dichiarare un ADR come superato da un altro, con creazione automatica dell'arco \`supera\`.

### 3.8 Validazione e regole sugli input

- Validazione di tipo, schema e integrità su tutti gli input (DTO con Bean Validation lato API; "fail fast" con messaggi chiari).
- Campi obbligatori minimi: per un ticket almeno titolo + sistema d'origine + ID; per un ADR almeno titolo + stato + decisione.
- **Immutabilità**: nessun input mutato in place; ogni revisione (es. cambio stato di un ADR, ri-pesatura di un arco) crea una nuova versione con timestamp e autore, preservando l'intera storia per audit.
- **Idempotenza dell'ingestione**: ri-sincronizzare la stessa fonte non deve duplicare nodi/archi, ma aggiornare le versioni.
- **Privacy**: nessun contenuto inviato a provider cloud senza consenso esplicito; i token dei connettori cifrati a riposo.

## 4. Flusso dell'attività (step-by-step)

Il flusso descrive l'esperienza end-to-end, dalla connessione delle fonti alla manutenzione della memoria decisionale. È pensato per l'MVP ma indica anche i punti di evoluzione.

### Fase A — Setup e connessione delle fonti

1. **Accesso e selezione del dominio.** L'utente accede a LocalMind (auth local-first esistente), seleziona/abilita il dominio enterprise "Ticketing & decisioni" e sceglie lingua interfaccia (IT/EN) e provider AI (default Ollama locale).
2. **Configurazione dei connettori.** L'amministratore collega le fonti (Jira, GitHub/GitLab, file ADR locali) tramite i connettori plugin (PF4J): inserisce URL, token (cifrati a riposo) e seleziona i progetti/repository da sincronizzare. Definisce la frequenza di sync e le politiche di visibilità.
3. **Mapping di normalizzazione.** Il sistema propone (e l'utente conferma) la mappatura tra stati/tipi/link dei sistemi esterni e le enum canoniche di LocalMind, con etichette bilingui IT/EN.

### Fase B — Ingestione di ticket e decisioni

4. **Sincronizzazione iniziale.** Un job batch importa ticket, issue, epic, incident e i loro link nativi dai sistemi connessi, oltre ai file ADR Markdown presenti. Ogni elemento viene validato (tipo, campi minimi, integrità) e deduplicato per ID esterno + sistema.
5. **Estrazione del testo e dei riferimenti.** Per ogni item si estraggono titolo, descrizione, commenti e — tramite parsing — i riferimenti incrociati nei testi ("fixes #123", "vedi JIRA-456", "supersedes ADR-0007"). Gli errori su un elemento non bloccano gli altri e vengono riportati con messaggi chiari.
6. **Segmentazione ed embedding.** I contenuti testuali vengono suddivisi in chunk (ChunkingService), vettorializzati e indicizzati su Qdrant con metadati di provenienza (sistema, ID, URL, timestamp). Ticket e decisioni sono persistiti su MySQL come nodi del grafo.
7. **Conferma di ingestione.** L'utente vede il riepilogo: quanti ticket/ADR importati, quanti link nativi acquisiti, eventuali errori, e una prima stima dei collegamenti causali dedotti.

### Fase C — Costruzione del grafo causale e decisionale

8. **Creazione dei nodi.** Ogni ticket, ADR, alternativa, incident, requisito e persona diventa un nodo tipizzato del grafo, con i suoi attributi e la sua provenienza.
9. **Importazione dei link espliciti.** I link nativi (blocks, caused by, duplicates, parent/child, supersedes) diventano archi con peso alto (sono dichiarati esplicitamente, quindi affidabili).
10. **Deduzione AI dei collegamenti impliciti.** Un job AI analizza i contenuti e propone archi mancanti ma probabili: un bug il cui testo descrive un sintomo coerente con una modifica recente (\`causa\`), un ticket che motiva una decisione (\`motiva\`), una decisione che impatta un servizio (\`impatta\`), commit↔ticket via ID. Ogni arco nasce con un **peso di confidenza**.
11. **Revisione umana (human-in-the-loop).** L'utente vede i collegamenti proposti e li conferma, corregge o rifiuta. Le conferme aumentano il peso, i rifiuti lo riducono o eliminano l'arco. Questo passaggio è cruciale per affidabilità e fiducia, specie sui legami causali.

### Fase D — Cattura strutturata di una nuova decisione (ADR)

12. **Innesco della decisione.** Durante il lavoro emerge la necessità di una scelta architetturale (es. da un incident ricorrente o da una nuova feature). L'utente apre il form ADR direttamente dal ticket/incident di contesto.
13. **Compilazione guidata.** Compila contesto/problema, decisione, razionale e conseguenze. Aggiunge le **alternative considerate** come elementi distinti, ognuna con il motivo dell'esclusione. Indica stakeholder e approvatori.
14. **Assistenza AI (opzionale).** L'AI può pre-compilare una bozza a partire dal contesto collegato (ticket, incident, decisioni correlate) e suggerire alternative tipiche e rischi noti, che l'utente rivede.
15. **Collegamento automatico al contesto.** Salvando l'ADR, il sistema crea automaticamente gli archi: \`motiva\` (dai ticket/incident), \`scarta-alternativa\` (verso i nodi alternativa), \`impatta\` (verso i servizi indicati), \`decisa-da\` (verso le persone). Se l'ADR sostituisce uno precedente, si crea l'arco \`supera\`.
16. **Workflow di approvazione.** L'ADR passa per gli stati Proposed → Accepted/Rejected (con approvatori registrati). Ogni transizione è un evento datato e immutabile (audit trail).

### Fase E — Interrogazione e ragionamento (GraphRAG)

17. **Domanda in linguaggio naturale.** L'utente chiede, ad esempio, "Perché abbiamo abbandonato il monolite per il modulo ordini?" oppure "Cosa rischiamo se rimuoviamo il servizio di caching?".
18. **Recupero GraphRAG.** Il sistema individua i nodi pertinenti, naviga il grafo per raccogliere il sotto-grafo rilevante (decisione + alternative + ticket motivanti + servizi impattati + decisioni successive) e recupera i chunk semanticamente più vicini da Qdrant.
19. **Risposta fondata e citata.** L'AI (Ollama di default) genera la risposta usando il materiale dell'organizzazione, **citando i nodi e i percorsi usati** (ADR-0042, JIRA-456, commit abc123) con deep-link al sistema d'origine. L'utente può aprire ogni fonte con un clic.
20. **Esplorazione visuale.** In parallelo, l'utente può aprire la vista grafo: partire dall'incident e risalire visivamente la catena \`causa\`/\`motiva\`/\`supera\`, espandendo progressivamente i vicini e filtrando per tipo di nodo/relazione.

### Fase F — Root-cause e analisi di impatto

21. **Dall'effetto alla causa.** Davanti a un incident, l'utente parte dal nodo Incident e segue gli archi \`causa\`/\`impatta\` a ritroso fino al commit/decisione responsabile, con l'AI che riassume la catena e i punti critici.
22. **Analisi di impatto (what-if).** Prima di un cambiamento, l'utente chiede "cosa dipende da X?": il sistema percorre in avanti gli archi \`dipende-da\`/\`blocca\`/\`impatta\` e mostra il raggio d'impatto, evidenziando decisioni e servizi coinvolti.
23. **Verifica di coerenza.** Quando si propone una nuova decisione, l'AI controlla il grafo per rilevare conflitti con decisioni esistenti ("questo contraddice ADR-0019") o duplicazioni di discussioni già risolte.

### Fase G — Manutenzione ed evoluzione della memoria

24. **Aggiornamento incrementale.** I job di sync periodici aggiornano stati dei ticket, nuovi ticket e nuovi link senza ricostruire il grafo; le modifiche generano nuove versioni dei nodi/archi (immutabilità).
25. **Igiene del grafo.** Routine periodiche segnalano nodi orfani, link deboli non confermati, ADR "Accepted" da tempo senza collegamento all'implementazione, alternative mai collegate.
26. **Esportazione e audit.** Su richiesta (compliance), il sistema esporta la catena decisionale di un'area — decisioni, razionali, approvatori, timeline — in formato leggibile (Markdown/JSON), tutto in locale.

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa l'infrastruttura del **motore di knowledge graph core** (nodi tipizzati + archi pesati su MySQL per la struttura, Qdrant per la semantica). Di seguito i tipi specifici dell'ambito Ticketing & decisioni.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi chiave |
|---|---|---|
| **Ticket / Issue** | Item di lavoro generico | id esterno, sistema, titolo, descrizione, stato, tipo, assegnatario, URL |
| **Bug** | Difetto da correggere | severità, passi per riprodurre, componente, stato |
| **Feature / Story** | Funzionalità richiesta | criteri di accettazione, epic, stato |
| **Epic / Iniziativa** | Aggregato di alto livello | obiettivo, ambito, stato |
| **Task / Sotto-task** | Unità operativa | parent, stato |
| **Incident / Postmortem** | Evento di produzione | severità, timeline, servizi impattati, azioni correttive |
| **Decisione (ADR)** | Decisione architetturale | id, titolo, stato (Proposed/Accepted/…), contesto, decisione, razionale, versione |
| **Alternativa** | Opzione considerata e (di solito) scartata | descrizione, motivo dell'esclusione, decisione di riferimento |
| **Requisito** | Esigenza funzionale/non funzionale | descrizione, tipo, priorità |
| **Change Request** | Richiesta di modifica controllata | motivazione, rischio, approvazioni |
| **Repository / Servizio / Componente** | Artefatto tecnico (riusato da altri ambiti) | nome, tipo, owner |
| **Commit / Pull Request** | Artefatto di codice | hash/numero, autore, messaggio, repo |
| **Persona** | Autore, assegnatario, approvatore | nome, ruolo, team |
| **Team / Unità** | Gruppo organizzativo | nome, ambito |
| **Commento / Discussione** | Testo di discussione (spesso razionale nascosto) | autore, testo, ticket di riferimento |
| **Chunk / Frammento** | Segmento di testo collegato ai vettori Qdrant | testo, id vettore, provenienza |

### 5.2 Tipi di relazione (archi)

| Relazione | Da → A | Significato | Direzionata |
|---|---|---|---|
| **causa** | Commit/Ticket/Decisione → Bug/Incident | A è all'origine di B | Sì |
| **blocca / è_bloccato_da** | Ticket → Ticket | A impedisce l'avanzamento di B | Sì |
| **dipende_da** | Ticket/Servizio → Ticket/Servizio | A richiede B | Sì |
| **duplica** | Ticket → Ticket | Stesso problema | No |
| **è_correlato_a** | Ticket → Ticket | Collegamento generico | No |
| **parent_di / sotto_task_di** | Epic → Story → Task | Gerarchia di scomposizione | Sì |
| **motiva** | Ticket/Incident/Requisito → Decisione | Ha innescato la decisione | Sì |
| **scarta_alternativa** | Decisione → Alternativa | Opzione valutata e respinta | Sì |
| **supera / superato_da** | Decisione → Decisione | A sostituisce B | Sì |
| **contraddice** | Decisione → Decisione | Tesi/scelte in conflitto | No |
| **implementa** | Commit/PR/Servizio → Decisione/Story | Realizza la scelta/feature | Sì |
| **impatta** | Decisione/Change → Servizio/Componente | Effetto su un artefatto | Sì |
| **risolve** | Commit/PR → Bug/Ticket | Chiude il problema | Sì |
| **decisa_da / approvata_da** | Decisione → Persona | Paternità/approvazione | Sì |
| **assegnato_a / segnalato_da** | Ticket → Persona | Responsabilità | Sì |
| **soddisfa** | Decisione/Feature → Requisito | Copre un requisito | Sì |
| **discusso_in** | Commento → Ticket/Decisione | Razionale catturato in discussione | Sì |

### 5.3 Criteri per il peso degli archi

Il peso (valore normalizzato, es. 0–1) esprime la **forza/affidabilità** della relazione e guida sia la visualizzazione (archi più spessi) sia il GraphRAG (priorità di esplorazione e di citazione). Il peso è una combinazione configurabile dei fattori seguenti, coerente con il principio core "peso derivato da fattori configurabili":

| Fattore | Effetto sul peso | Esempio |
|---|---|---|
| **Origine esplicita** | Base molto alta | Link nativo Jira \`caused by\` o riferimento \`fixes #123\` nel commit |
| **Confidenza dell'estrazione AI** | Base iniziale (medio-bassa) | L'LLM ipotizza che il bug sia causato da quel commit |
| **Conferma umana** | Aumenta forte e stabilizza | Un dev conferma il legame causale → peso alto, "verificato" |
| **Rifiuto umano** | Azzera/rimuove | Il legame proposto viene rifiutato |
| **Prossimità temporale** | Aumenta (per \`causa\`) | Il bug è apparso subito dopo quel deploy/commit |
| **Co-occorrenza / co-citazione** | Aumenta | Due ticket citati spesso insieme, o nello stesso commit |
| **Similarità semantica (Qdrant)** | Aumenta | Descrizioni del bug e della modifica molto vicine nello spazio vettoriale |
| **Frequenza di navigazione** | Aumenta | Percorso causale attraversato spesso durante le analisi |
| **Autorevolezza/ruolo** | Pesa \`decisa_da\`/\`approvata_da\` | Decisione approvata da un architetto senior |
| **Stato della decisione** | Modula visibilità | ADR \`Superseded\` perde peso nelle risposte correnti, ma resta per la storia |
| **Recenza / decadimento** | Riduce nel tempo | Link non confermati e non usati decadono lentamente |

Regola di immutabilità: il peso non viene mutato in place; ogni rivalutazione produce una nuova versione del valore (con timestamp e fattori contribuenti), così da poter spiegare *perché* un arco ha quel peso — requisito di interpretabilità essenziale in contesti di audit e compliance.

## 6. Fonti dati & connettori (ingestione)

| Fonte | Modalità | Stato | Note |
|---|---|---|---|
| **File ADR Markdown** (Nygard/MADR) | Parsing cartelle \`docs/adr/\` + folder watcher | MVP | Riusa pipeline \`document\` (Tika) e \`LocalFileSystemScanner\` |
| **Creazione ADR da UI** | Form strutturato | MVP | Modalità nativa, nessun import necessario |
| **Jira** | Connettore plugin (PF4J) + REST API | MVP/early | Issue, epic, link nativi, transizioni di stato |
| **GitHub Issues / Projects** | Connettore plugin + REST/GraphQL API | MVP/early | Issue, PR, link \`fixes #\`, label |
| **GitLab Issues / Merge Requests** | Connettore plugin + API | Evoluzione | Analogo a GitHub |
| **Azure DevOps / Redmine** | Connettore plugin | Evoluzione | Work item e relazioni |
| **Repository Git (commit/PR)** | Connettore + parsing messaggi | Early | Collegamento commit↔ticket↔decisione |
| **Incident management (PagerDuty, Opsgenie)** | Connettore plugin | Evoluzione | Incident come nodi, root-cause |
| **ITSM (ServiceNow)** | Connettore plugin | Evoluzione | Change request, approvazioni |
| **Confluence / Wiki** | Connettore + estrazione | Evoluzione | ADR e razionali documentati su wiki |
| **Email** (decisioni via thread) | Modulo \`email\` esistente | Evoluzione | Estrazione di decisioni discusse via mail |

Tutti i connettori esterni passano per il sistema **plugin PF4J + marketplace**, così da non gonfiare il core e rispettare la modularità per dominio. Ogni connettore deve dichiarare quali dati legge, dove finiscono e con quali credenziali (cifrate a riposo), coerentemente con la privacy local-first. La sincronizzazione è **incrementale e idempotente**: ri-eseguirla aggiorna le versioni senza duplicare nodi/archi.

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

### 7.1 MVP (prima release dell'ambito)

| # | Funzionalità | Cosa comporta (backend / frontend) | Moduli toccati |
|---|---|---|---|
| 1 | **Dominio \`decisions\` (o estensione di \`knowledge\`)** | Modelli di nodo/arco specifici (Ticket, ADR, Alternativa, relazioni causali), port in/out, service; wiring in \`DomainConfig\` | domain, infrastructure |
| 2 | **CRUD ADR strutturato (MADR)** | Form e API per creare/leggere/aggiornare ADR con contesto, decisione, razionale, alternative, stato; versionamento immutabile; controller \`/api/v1/decisions/*\`; UI feature standalone (Signals) | api, frontend, MySQL (Flyway, una query/file) |
| 3 | **Import ADR Markdown** | Parser file \`docs/adr/\` (Nygard/MADR) → nodi ADR + archi \`supera\` | infrastructure, batch |
| 4 | **Ingestione ticket (Jira/GitHub)** | Connettori plugin PF4J + sync incrementale; normalizzazione stati/tipi/link; deduplica | infrastructure (plugin), batch |
| 5 | **Costruzione grafo: nodi + link espliciti** | Creazione nodi tipizzati e import dei link nativi (blocks, caused by, parent/child) con peso alto | domain, infrastructure |
| 6 | **Deduzione AI dei collegamenti impliciti** | Job AI che propone archi \`causa\`/\`motiva\`/\`impatta\` con peso di confidenza | domain, infrastructure (Ollama) |
| 7 | **Revisione human-in-the-loop** | API e UI per confermare/correggere nodi e archi; aggiornamento pesi | api, frontend |
| 8 | **Visualizzazione interattiva del grafo causale** | Vista grafo con espansione progressiva, filtri per tipo nodo/relazione, evidenziazione catene causali | frontend |
| 9 | **GraphRAG "perché/cosa-se" con citazioni** | Recupero sotto-grafo + chunk Qdrant; risposta con citazione nodi/percorsi e deep-link; integrazione chat esistente | domain (knowledge/llm), frontend (chat) |
| 10 | **Collegamento automatico ADR↔contesto** | Alla creazione ADR, archi automatici \`motiva\`/\`scarta-alternativa\`/\`impatta\`/\`decisa-da\` | domain, api |
| 11 | **i18n IT/EN** | Enum (tipi nodo/arco, stati ticket/ADR) tradotte e instradate al frontend secondo lo switch lingua | api, frontend |

### 7.2 Evoluzioni (release successive)

| # | Funzionalità | Valore aggiunto |
|---|---|---|
| 12 | **Workflow di approvazione ADR** | Stati Proposed→Accepted/Rejected con approvatori registrati e audit trail |
| 13 | **Root-cause assistita** | Navigazione automatica effetto→causa con riassunto AI della catena |
| 14 | **Analisi di impatto (what-if)** | "Cosa dipende da X?" — propagazione in avanti su \`dipende-da\`/\`impatta\` |
| 15 | **Verifica di coerenza decisionale** | L'AI rileva contraddizioni con decisioni esistenti e discussioni duplicate |
| 16 | **Proposta AI di bozze ADR** | Genera bozza di decisione (con alternative e rischi) a partire da ticket/incident |
| 17 | **Connettori aggiuntivi** (GitLab, Azure DevOps, PagerDuty, ServiceNow, Confluence) | Copertura completa delle fonti enterprise via plugin PF4J |
| 18 | **Collegamento commit/PR↔decisione** | Tracciabilità decisione→implementazione end-to-end |
| 19 | **Export per audit/compliance** | Catena decisionale (razionale, approvatori, timeline) in Markdown/JSON |
| 20 | **Timeline e analytics decisionali (privati)** | Tempo Proposed→Accepted, decisioni superate, hot-spot di incoerenza |
| 21 | **Suggerimento di link e lacune** | L'AI propone collegamenti causali non evidenti e ADR "orfani" da rivedere |
| 22 | **Esposizione MCP** | Tool MCP per interrogare il grafo decisionale da agenti/IDE esterni |

### 7.3 Da mantenere (manutenzione continua)

- Connettori plugin (compatibilità con le API esterne Jira/GitHub/GitLab che cambiano nel tempo).
- Mapping di normalizzazione di stati/tipi/link al variare delle configurazioni dei tool.
- Prompt e logica GraphRAG (qualità della deduzione causale e della citazione fonti).
- Schema del grafo e migrazioni Flyway (una query per file), con evoluzione retro-compatibile.
- Traduzioni IT/EN di enum e UI a ogni nuova feature.
- Tuning dei fattori di peso (specie temporali e semantici) sulla base del feedback reale.
- Igiene periodica del grafo (link deboli, nodi orfani, ADR non collegati all'implementazione).

## 8. Casi d'uso AI / GraphRAG

1. **Memoria del "perché".** "Perché abbiamo scelto event sourcing per gli ordini?" → L'AI naviga il nodo ADR-0042, le sue alternative scartate e i ticket motivanti, e risponde citando "ADR-0042", "JIRA-456" e il postmortem collegato. Tutto in locale con Ollama.
2. **Root-cause di un incident.** "Cosa ha causato l'outage del checkout di venerdì?" → L'AI parte dall'Incident, risale gli archi \`causa\`/\`risolve\` fino al commit responsabile e alla decisione che ha introdotto il componente, riassumendo la catena con deep-link.
3. **Analisi di impatto pre-cambiamento.** "Cosa rischiamo se rimuoviamo il servizio di caching?" → L'AI percorre \`dipende-da\`/\`impatta\` e mostra servizi, decisioni e ticket coinvolti.
4. **Verifica di coerenza.** "Questa nuova decisione è coerente con il passato?" → L'AI rileva che contraddice ADR-0019 e segnala una discussione già risolta in JIRA-321.
5. **Onboarding decisionale.** "Raccontami la storia architetturale del modulo pagamenti." → L'AI ricostruisce la sequenza di ADR (incluse quelle superate), i ticket chiave e le persone coinvolte.
6. **Decisioni superate ancora in uso.** "Quali decisioni dipendono da ADR ormai superati?" → L'AI segue \`supera\`/\`dipende-da\` ed evidenzia debito architetturale.
7. **Bozza assistita di ADR.** A partire da un incident ricorrente, l'AI propone una bozza di decisione con contesto, alternative tipiche e rischi noti, da rivedere.
8. **Sintesi di backlog causale.** "Quali bug aperti sono bloccati da decisioni non ancora prese?" → L'AI incrocia stato ticket e stato ADR collegati.
9. **Domanda multi-hop cross-dominio.** "Quali clienti/feature sono impattati dai servizi che dipendono dalla decisione X?" → ragionamento multi-salto sul grafo, combinando ambiti enterprise.

## 9. KPI & metriche di successo

| Categoria | Metrica | Obiettivo indicativo |
|---|---|---|
| **Adozione** | ADR creati/collegati per team attivo | Crescita costante; ogni decisione rilevante tracciata |
| **Copertura** | % ticket/ADR con almeno un collegamento causale | ≥ 70% dei nodi non orfani |
| **Qualità del grafo** | % link suggeriti dall'AI confermati | ≥ 60% accettazione al netto delle correzioni |
| **Fondatezza** | % risposte AI con almeno una citazione verificabile | ≥ 95% quando la fonte esiste |
| **Qualità delle risposte** | % risposte valutate utili e fondate | ≥ 80% pollice su |
| **Efficacia root-cause** | Tempo medio per ricostruire una catena causale | Riduzione marcata vs ricerca manuale |
| **Memoria istituzionale** | % decisioni con razionale + alternative compilati | ≥ 80% degli ADR |
| **Onboarding** | Tempo per orientarsi su un'area nuova | Riduzione misurabile |
| **Performance** | Latenza risposta AI in locale (Ollama) | Accettabile su hardware on-prem (target < pochi secondi al primo token) |
| **Privacy** | Dati inviati a cloud senza consenso | Zero (vincolo, non obiettivo) |
| **Compliance** | Decisioni con approvatori e timeline tracciati | 100% per i progetti regolati |

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---|---|---|
| **Deduzione causale rumorosa** (link errati \`causa\`) | Catene fuorvianti, sfiducia | Human-in-the-loop obbligatorio sui legami causali; pesi distinti per link espliciti vs dedotti; soglie di confidenza |
| **Allucinazioni dell'AI** | Risposte sbagliate su decisioni critiche | GraphRAG vincolato al grafo; citazione obbligatoria; risposta "non presente nei dati" quando manca la fonte |
| **Razionale non compilato** (ADR vuoti di motivazione) | Memoria povera | Form guidato che incoraggia contesto/alternative; assistenza AI alla bozza; KPI di completezza |
| **Drift dei connettori** (API esterne che cambiano) | Sync rotta, dati obsoleti | Connettori isolati come plugin PF4J versionati; monitoraggio; sync idempotente |
| **Privacy dei dati enterprise** | Violazione fiducia/compliance | Local-first rigoroso; token cifrati; nessun invio cloud senza consenso; nessuna telemetria |
| **Sovraccarico del grafo** (troppi nodi/archi) | Vista illeggibile, query lente | Espansione progressiva, filtri, clustering per area/servizio; limiti di profondità |
| **Query di grafo profonde su MySQL+Qdrant** | Percorsi causali lenti su grafi grandi | Indicizzazione mirata, materializzazione di percorsi frequenti, limiti di profondità; rivalutare grafo dedicato solo se necessario (vincolo di progetto) |
| **Normalizzazione eterogenea** (stati/link diversi tra tool) | Mapping errati, link persi | Mapping configurabile e confermato dall'utente; default ragionevoli; enum bilingui |
| **Resistenza culturale** (team che non documentano) | Adozione bassa | Basso attrito (ADR da ticket in un clic), valore immediato (risposte "perché"), integrazione nel flusso esistente |
| **Ambiguità del razionale storico** | Decisioni mal interpretate | Conservazione immutabile e versionata; citazione delle discussioni originali; nessuna riscrittura della storia |

## 11. Manutenzione & evoluzione

- **Aggiornamento incrementale e idempotente.** I job di sync aggiornano stati e nuovi link senza ricostruire il grafo; ogni modifica genera una nuova versione (immutabilità) e preserva l'audit trail.
- **Igiene del grafo.** Routine periodiche segnalano nodi orfani, link deboli non confermati, ADR \`Accepted\` senza implementazione collegata e alternative mai associate a una decisione.
- **Versionamento dello schema.** Ogni evoluzione dei tipi di nodo/arco passa per migrazioni Flyway retro-compatibili (una query per file), con strategia di backfill documentata.
- **Tuning dei modelli e dei prompt.** Aggiornamento periodico dei prompt di deduzione causale e GraphRAG, dei modelli Ollama consigliati e dei parametri di chunking, guidato dalle metriche di §9.
- **Calibrazione dei pesi.** I fattori di peso (§5.3) — in particolare prossimità temporale, similarità semantica e origine esplicita — vengono affinati sui dati reali, mantenendo l'interpretabilità per l'audit.
- **Compatibilità connettori.** Monitoraggio delle API esterne (Jira, GitHub, GitLab, PagerDuty…) e aggiornamento dei plugin PF4J corrispondenti, con test di regressione.
- **Documentazione bilingue.** Ogni feature aggiorna documentazione IT/EN e i log in \`Sviluppi/\` secondo le convenzioni di progetto.
- **Roadmap di valutazione.** Introdurre un set di "golden questions" decisionali (es. catene causali note) per misurare regressioni nella qualità delle risposte e della deduzione.

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo esistente | Ruolo nell'ambito Ticketing & decisioni |
|---|---|
| **knowledge** | Base del motore a grafo: estensione con tipi di nodo/arco di ticketing e decisioni; punto naturale dove innestare il dominio |
| **document** | Ingestione e parsing dei file ADR Markdown, RFC, design doc (Tika), chunking e provenienza |
| **llm** | Deduzione causale, bozze ADR e risposte GraphRAG via \`LlmGatewayService\`; Ollama default, cloud opzionale con consenso |
| **(Qdrant) vectorstore** | Indice semantico dei testi di ticket/ADR per il recupero GraphRAG |
| **batch** | Job di sync incrementale dei connettori e job periodici di ricalcolo/igiene del grafo |
| **mcp** | Esposizione di tool (es. interrogare il grafo decisionale, creare un ADR) ad agenti/IDE esterni |
| **agent** | Agente "memoria decisionale" che orchestra root-cause, analisi di impatto e verifica di coerenza |
| **automation** | Trigger automatici: "nuovo incident → proponi bozza ADR", "ADR superato → segnala dipendenze", "bug bloccato da decisione → notifica" |
| **email** | Estrazione di decisioni e razionali discussi via thread di posta (evoluzione) |
| **calendar** | Date di review architetturali e scadenze di decisione come contesto |
| **marketplace + plugin (PF4J)** | Connettori Jira/GitHub/GitLab/PagerDuty e pacchetti di dominio come moduli installabili |
| **messaging** | Notifiche su decisioni in attesa di approvazione, link causali da confermare |
| **auth** | Identità local-first, ruoli e governance (chi approva ADR, chi modifica il grafo) |
| **common** | Eventi di dominio (es. "ADR accettato", "link confermato"), analytics privati, gestione errori |
| **finetuning** | Eventuale adattamento di modelli locali al lessico decisionale dell'organizzazione (avanzato) |
| **Frontend (Angular 21)** | Nuova feature standalone con Signal store, vista grafo causale interattiva, form ADR (MADR), chat GraphRAG con citazioni; i18n IT/EN |

L'ambito Ticketing & decisioni è quindi un **verticale enterprise** del motore universale: riusa interamente l'infrastruttura esistente (ingestione, embedding, LLM, grafo, plugin, eventi) e aggiunge solo i tipi di nodo/relazione causali e decisionali, le funzionalità di cattura del razionale e di root-cause, e l'esperienza utente specifica di architetti, sviluppatori e team — coerente con il principio "una piattaforma, più ecosistemi", restando local-first, gratuito, privato e bilingue.
`;
