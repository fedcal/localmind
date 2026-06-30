export const content = `# Persone & competenze

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema enterprise: la conoscenza è nelle persone, ma è invisibile

In qualsiasi organizzazione oltre le poche decine di dipendenti emerge una verità scomoda: **il patrimonio di competenze esiste, ma nessuno sa dove sia**. La conoscenza di "chi sa fare cosa" vive nelle teste delle persone, nei thread di chat, nelle code review, nelle email, nei verbali di riunione, nei commit di un repository — e quasi mai in un sistema interrogabile. Il risultato è una serie di sintomi ricorrenti e costosi:

- **La domanda "chi sa di…?" non ha risposta.** Quando serve qualcuno che conosca Kafka, la normativa GDPR applicata ai dati sanitari, il legacy gestionale scritto in COBOL o il cliente "Acme S.p.A.", la ricerca avviene per passaparola: si chiede a un collega, che chiede a un altro, finché — forse — si arriva alla persona giusta. Questo processo è lento, dipende dalla rete sociale di chi cerca (e quindi penalizza i nuovi assunti e chi lavora da remoto) e spesso si ferma alla "persona più visibile" anziché alla più competente.

- **I CV e gli organigrammi mentono o invecchiano.** Il CV fotografa il momento dell'assunzione; il profilo HR contiene job title e inquadramento contrattuale, non le competenze reali. Le skill che una persona ha sviluppato sul campo negli ultimi due anni — magari proprio quelle più preziose — non sono registrate da nessuna parte. Gli inventari di competenze auto-dichiarate, dove esistono, soffrono di due distorsioni opposte e ben documentate: c'è chi si dichiara esperto in dieci aree in cui è solo competente, e c'è il database che, un anno dopo la compilazione, non è più aggiornato da nessuno.

- **I gap di competenze sono scoperti troppo tardi.** L'azienda decide di adottare una nuova tecnologia, di entrare in un nuovo mercato o di rispondere a un bando, e solo allora scopre di non avere — o di avere in una sola persona — le competenze necessarie. Manca una visione anticipata di *dove sono i buchi* tra le competenze possedute e quelle richieste dalla strategia.

- **Il "bus factor" è un rischio nascosto.** Quante aree critiche dipendono da una sola persona? Se quel collega si ammala, va in ferie o lascia l'azienda, un intero sistema, processo o cliente resta scoperto. Questo rischio di concentrazione della conoscenza (single point of knowledge) raramente è misurato, eppure è uno dei principali fattori di fragilità operativa.

- **La mobilità interna è bloccata.** Quando si apre una posizione o nasce un progetto, si guarda fuori prima che dentro, perché non si conoscono i talenti interni con competenze trasferibili. Persone qualificate restano "invisibili" e finiscono per andarsene, mentre l'azienda recluta all'esterno competenze che già possedeva.

- **La formazione è scollegata dai bisogni reali.** I piani di L&D (Learning & Development) sono spesso a catalogo, uguali per tutti, scollegati dai gap effettivi. Non si sa quali competenze conviene sviluppare internamente, in chi, e con quale priorità rispetto agli obiettivi.

- **L'onboarding è lentissimo.** Un nuovo assunto impiega mesi a capire "chi è chi", chi chiedere per cosa, come è strutturata la conoscenza dell'azienda. Questa mappa, oggi, si costruisce solo con il tempo e le relazioni informali.

### 1.2 La nostra risposta: lo skill graph vivo dell'organizzazione

LocalMind, nella sua evoluzione a **motore di knowledge graph universale**, affronta questo problema costruendo lo **skill graph** dell'organizzazione: una rete pesata e vivente in cui i nodi sono le persone, le competenze, i ruoli, i team, i progetti e i contesti, e gli archi rappresentano relazioni significative e quantificate — "possiede la competenza", "ha lavorato a", "è esperto di", "ha mentore", "richiede", "appartiene al team". A differenza di una tassonomia statica o di un foglio Excel di skill, questo grafo è **continuamente alimentato e pesato dall'evidenza** già presente nei sistemi aziendali. Su questa base si innestano quattro capacità:

1. **Mappatura automatica di "chi-sa-cosa".** Le competenze non sono (solo) auto-dichiarate: vengono **inferite** dai segnali che le persone già producono — documenti redatti, email e thread di messaggistica, ticket risolti, contributi a repository, partecipazione a progetti — usando l'AI locale per estrarre competenze e contesti dal testo. Ogni inferenza è tracciabile alla sua evidenza, così che la skill "Kafka" di Mario non sia un'asserzione vuota ma sia ancorata ai cinque documenti e ai dodici ticket che la dimostrano.

2. **Localizzazione dell'esperto via GraphRAG.** L'AI (Ollama in locale per default) risponde a domande in linguaggio naturale — "chi può aiutarmi a integrare un pagamento Stripe?", "chi conosce il cliente Acme e parla tedesco?" — navigando lo skill graph, combinando il match strutturale (relazioni persona→competenza) con il match semantico (ricerca su Qdrant nei contenuti prodotti dalle persone) e citando sempre l'evidenza che giustifica la raccomandazione.

3. **Analisi dei gap e del rischio.** Confrontando le competenze possedute con quelle richieste (da ruoli, progetti, strategia), il sistema fa emergere i **gap di competenze**, i **single point of knowledge** (bus factor), le competenze in via di obsolescenza e quelle in crescita, suggerendo azioni: assumere, formare, ridistribuire, documentare.

4. **Abilitazione di mobilità interna e formazione mirata.** Il grafo diventa la base di un marketplace interno dei talenti: persone con competenze adatte o trasferibili emergono per progetti e posizioni aperte, e i piani di formazione si agganciano ai gap reali della singola persona e dell'organizzazione.

### 1.3 Perché questo ambito è strategico

Il dominio "Persone & competenze" non è un verticale qualsiasi: è uno degli ambiti enterprise a più alto valore perché si colloca all'**intersezione di tutti gli altri grafi**. Le persone sono i nodi che collegano documenti, processi, repository, microservizi, clienti, decisioni: chi ha scritto quel documento, chi mantiene quel servizio, chi gestisce quel cliente. Costruire bene lo skill graph significa dare un "proprietario umano" a ogni pezzo di conoscenza aziendale, moltiplicando il valore di tutti gli altri ambiti enterprise (documenti, repository, processi, API). È, in sostanza, il **tessuto connettivo** del knowledge graph enterprise.

### 1.4 Perché LocalMind è la piattaforma giusta

I dati su persone e competenze sono tra i più sensibili che un'organizzazione possiede: profili, valutazioni implicite, "chi sa poco di cosa", relazioni gerarchiche. Affidarli a un SaaS HR cloud è, per molte aziende (PA, sanità, difesa, finanza, studi professionali), semplicemente impraticabile per ragioni normative e di fiducia. Qui LocalMind ha un vantaggio strutturale.

| Esigenza dell'organizzazione | Caratteristica LocalMind che la soddisfa |
|---|---|
| Dati su persone/competenze mai esfiltrati verso cloud terzi | **Local-first / self-hosted**: tutto gira on-premise; nessun invio esterno senza consenso esplicito |
| Conformità GDPR e privacy del lavoratore | AI **Ollama locale di default**: l'inferenza delle competenze non lascia il perimetro aziendale |
| Estrazione competenze da contenuti eterogenei | Pipeline di ingestione esistente (**Tika + Tesseract OCR**), chunking, embedding, già operativa |
| Risposte fondate sull'evidenza interna | **GraphRAG** su skill graph + ricerca semantica **Qdrant**, con citazione delle fonti |
| Le persone collegano tutti gli altri grafi (doc, repo, mail) | Domini esistenti \`document\`, \`email\`, \`messaging\`, \`calendar\`, \`mcp\` già ingeriscono quei segnali |
| Connettori a sistemi HR/IT specifici dell'azienda | Sistema **plugin PF4J** + marketplace per connettori (HRIS, AD/LDAP, Git, Jira) |
| Interfaccia e tassonomie bilingui | Piattaforma **bilingue IT/EN** by design, enum tradotte |
| Nessun lock-in, nessun costo per dipendente | **Open source puro**, nessun paywall, nessun pricing per-seat |

Il differenziatore competitivo rispetto alle suite HR tech (Gloat, 365Talents, Eightfold, Workday Skills Cloud) è netto: quelle piattaforme offrono skill graph potenti ma **cloud-only, a pagamento per dipendente e con i dati fuori dal perimetro aziendale**. LocalMind offre lo stesso paradigma — skill graph pesato, inferenza AI, localizzazione esperti, gap analysis — restando **on-premise, gratuito e sovrano sui dati**, e integrandolo nativamente con il resto della conoscenza aziendale invece di trattarlo come un silo HR isolato.

## 2. Personas & utenti target

| Persona | Profilo | Bisogni primari | Come usa LocalMind |
|---|---|---|---|
| **Laura, HR Manager / People Operations** | Responsabile sviluppo persone in azienda da ~300 dipendenti | Mappare le competenze reali, individuare gap, pianificare formazione e successioni | Esplora lo skill graph, lancia gap analysis per area, gestisce la tassonomia delle competenze, pianifica L&D mirato |
| **Marco, Resource/Delivery Manager** | Compone team di progetto in una società di consulenza | Trovare velocemente persone con le competenze giuste e disponibili | Usa il tutor "trova l'esperto", compone team per skill richieste, verifica copertura competenze del progetto |
| **Sara, Team Lead / Engineering Manager** | Guida un team tecnico di 8 persone | Capire i punti di forza/debolezza del team, ridurre il bus factor, far crescere le persone | Visualizza la matrice competenze del team, individua single point of knowledge, pianifica mentoring e cross-training |
| **Davide, dipendente / knowledge worker** | Sviluppatore, vuole crescere e farsi trovare | Far emergere le proprie competenze, trovare progetti/mentori, capire cosa imparare | Rivede e conferma il proprio profilo competenze (inferito), si candida a opportunità interne, riceve suggerimenti di crescita |
| **Giulia, nuova assunta** | In azienda da 2 settimane | Capire "chi è chi" e a chi rivolgersi | Chiede al tutor GraphRAG chi è l'esperto di X, naviga il grafo team/persone/competenze |
| **Antonio, CTO / Direzione** | Decisioni strategiche su skill e organizzazione | Visione d'insieme delle capability aziendali, rischi di concentrazione, allineamento skill↔strategia | Consulta dashboard aggregate e anonimizzate, valuta capacità vs roadmap, decide make-or-buy delle competenze |
| **DPO / Responsabile privacy** | Garante della conformità sul trattamento dei dati dei lavoratori | Garantire base giuridica, trasparenza, minimizzazione, diritti dell'interessato | Configura policy di inferenza/visibilità, audita le evidenze, gestisce consenso e opt-out |

L'utente primario e prioritario per l'MVP è la coppia **Laura (HR) + Marco (Resource Manager)**: sono i portatori del bisogno più acuto ("chi sa cosa", gap, composizione team) e i decisori dell'adozione. **Davide (il dipendente)** è però un utente co-primario imprescindibile: senza la sua partecipazione — conferma del profilo, controllo sulla visibilità — il sistema è eticamente e legalmente insostenibile. Le altre personas guidano le evoluzioni.

## 3. Requisiti in input

Questa sezione definisce in dettaglio **cosa deve poter entrare nel sistema** perché l'ambito Persone & competenze funzioni. Gli input si dividono in: anagrafica e struttura organizzativa, tassonomia delle competenze, segnali di evidenza da cui inferire le competenze, dati dichiarativi e di feedback, requisiti (ruoli/progetti) e configurazione di privacy. Ogni input va validato al confine del sistema (principio "never trust external data") e trattato in modo immutabile: nessuna competenza inferita o dichiarata viene mutata in place, ma versionata, così da preservare la storia per audit, per il calcolo del peso e per i diritti dell'interessato.

> **Principio cardine (privacy by design).** A differenza degli altri ambiti, qui l'input riguarda **persone fisiche**. Ogni ingestione di segnale deve avere una base giuridica, un livello di visibilità e la possibilità di opt-out. Nessun dato di una persona viene reso visibile ad altri prima della revisione/consenso secondo le policy configurate (§3.7).

### 3.1 Anagrafica persone e struttura organizzativa

Il primo input è la "spina dorsale" del grafo: chi esiste e come è organizzato. Tipicamente importabile da HRIS, Active Directory/LDAP o file.

| Dato | Esempi di campi | Fonte tipica | Obbligatorietà |
|---|---|---|---|
| Persona | nome, identificativo interno, email, stato (attivo/cessato), data ingresso | HRIS, AD/LDAP, CSV | Obbligatorio (almeno identificativo + nome) |
| Ruolo / posizione | job title, livello/inquadramento, famiglia professionale | HRIS | Consigliato |
| Unità organizzativa / team | reparto, team, centro di costo, sede | HRIS, organigramma | Consigliato |
| Gerarchia | responsabile (manager), riporti | HRIS, AD | Opzionale |
| Sede / area geografica | ufficio, città, fuso orario, lingue parlate | HRIS, AD | Opzionale ma prezioso per la ricerca |
| Disponibilità | allocazione corrente, capacità residua | sistemi di project/resource management | Evoluzione |

### 3.2 Tassonomia / ontologia delle competenze

Per dare struttura e confrontabilità alle competenze serve una **tassonomia** (preferibilmente un'ontologia con gerarchia e sinonimi). LocalMind deve poterla importare, costruire o ibridare:

- **Import da standard aperti**: ESCO (classificazione europea multilingue, ~14.000 skill e ~3.000 occupazioni, ideale per il requisito IT/EN) e/o O*NET (USA). Forniscono categorie (competenze core, tecniche, trasversali), sinonimi e relazioni gerarchiche pronte.
- **Tassonomia aziendale custom**: l'azienda definisce le proprie competenze (tecnologie, prodotti, processi, clienti, soft skill) con eventuale mapping verso lo standard.
- **Ontologia emergente**: nuove competenze possono nascere dall'inferenza AI sui contenuti e poi essere normalizzate/fuse con quelle esistenti (gestione sinonimi: "JS" = "JavaScript", "GDPR" = "Data Protection Regulation").

Attributi minimi di una competenza: nome (IT/EN), descrizione, categoria/dominio, relazioni gerarchiche (più generale/più specifica), competenze correlate o prerequisite, e — fondamentale — la **scala dei livelli di padronanza** (es. 1 Novizio → 2 Base → 3 Competente → 4 Avanzato → 5 Esperto, modellata sulla logica Dreyfus). Queste scale devono essere enum **bilingui IT/EN** reindirizzate al frontend secondo lo switch di lingua, coerentemente col vincolo di progetto.

### 3.3 Segnali di evidenza (input per l'inferenza delle competenze)

È il cuore del valore: invece di affidarsi solo all'auto-dichiarazione (notoriamente inaffidabile e velocemente obsoleta), il sistema **inferisce** le competenze dai segnali che le persone già producono. Questi segnali provengono, ove possibile, dai domini LocalMind già esistenti.

| Segnale | Cosa indica | Dominio LocalMind / connettore | Peso indicativo |
|---|---|---|---|
| Documenti redatti/firmati | autore di documentazione su un tema → competenza sul tema | \`document\` (Tika/OCR) | Alto se autore, medio se solo citato |
| Email e thread di messaggistica | partecipazione attiva a discussioni tecniche su un tema | \`email\`, \`messaging\` | Medio (modulato da ruolo nel thread) |
| Eventi/calendario | partecipazione/relatore a meeting, formazioni, conferenze su un tema | \`calendar\` | Basso-medio |
| Contributi a repository | commit, PR, file toccati → competenza su linguaggi/moduli | connettore Git (plugin) | Alto (evidenza forte e datata) |
| Ticket/issue risolti | risoluzione di problemi su un'area → competenza operativa | connettore Jira/issue tracker (plugin) | Alto |
| Certificazioni e formazione | attestati, corsi completati | HRIS/LMS, upload | Alto ma da validare (formale ≠ pratico) |
| Progetti svolti | esperienza su tecnologie/clienti/domini di un progetto | resource/project management | Alto |
| CV / profili professionali | base storica delle competenze | upload (PDF), import | Medio (auto-dichiarato) |
| Auto-dichiarazione e endorsement | la persona dichiara o i colleghi confermano una skill | UI LocalMind | Variabile: dichiarazione bassa, endorsement medio-alto |

Requisiti trasversali sui segnali:
- **Provenienza tracciata sempre**: ogni competenza inferita deve risalire all'evidenza esatta (documento, email, commit, ticket) con timestamp. Niente "scatole nere".
- **Recency / decadimento**: i segnali hanno una data; il peso decade nel tempo (una competenza dimostrata 5 anni fa pesa meno di una di 6 mesi fa), per modellare l'obsolescenza.
- **Deduplica e normalizzazione**: lo stesso contenuto non deve generare evidenze multiple; le competenze estratte vanno normalizzate sulla tassonomia (sinonimi, varianti linguistiche IT/EN).
- **Minimizzazione**: si ingerisce solo ciò che serve a inferire competenze; i contenuti sensibili non pertinenti vanno esclusi o filtrati.

### 3.4 Requisiti di competenza (la "domanda")

Per fare gap analysis e matching serve definire **cosa serve**, non solo cosa c'è:

- **Profili di ruolo**: per ogni ruolo, le competenze richieste e il livello-target (es. "Backend Senior" richiede Java≥4, Kafka≥3, SQL≥3).
- **Requisiti di progetto**: competenze necessarie a un progetto, con livello e numero di persone, finestra temporale, lingua/sede.
- **Posizioni aperte**: requisiti di una vacancy per il matching interno.
- **Obiettivi strategici/roadmap**: capability che l'azienda vuole sviluppare (es. "AI generativa", "Kubernetes") per la pianificazione anticipata dei gap.

### 3.5 Dati dichiarativi e di feedback della persona

Sono gli input che mantengono il sistema corretto ed etico (il dipendente non è un oggetto passivo del grafo):

- **Conferma/correzione del profilo inferito**: la persona valida, declassa o rimuove le competenze che il sistema le ha attribuito (loop fondamentale: alimenta il peso e l'accuratezza).
- **Auto-dichiarazione di livello e interessi**: skill possedute, livello percepito, **aspirazioni** (competenze che vuole sviluppare — input chiave per la mobilità).
- **Endorsement tra colleghi**: conferma reciproca di competenze, con peso modulato dalla competenza di chi endorsa.
- **Preferenze di visibilità**: cosa rendere visibile, a chi, e cosa tenere privato.

### 3.6 Configurazione di sistema

- **Provider LLM e modello** per l'inferenza (default Ollama locale; cloud solo con consenso esplicito), modello di embedding, lingua interfaccia (IT/EN).
- **Sorgenti di ingestione abilitate** e relativi connettori (HRIS, AD/LDAP, Git, Jira, folder watcher esistente).
- **Parametri di inferenza e pesatura**: soglie di confidenza per accettare/proporre una competenza, formula di decadimento temporale, pesi relativi dei tipi di segnale (§5).
- **Politiche di gap analysis**: soglia di bus factor (es. "critico se ≤1 persona a livello ≥4"), competenze considerate strategiche/critiche.

### 3.7 Validazione, privacy e regole sugli input

Questo ambito ha requisiti di compliance più stringenti di ogni altro:

- **Base giuridica e trasparenza**: l'ingestione di segnali personali deve essere configurata con una base giuridica esplicita; le persone devono sapere quali fonti vengono analizzate.
- **Consenso e opt-out granulare**: per fonte (es. "analizza i miei documenti ma non le mie email") e per visibilità.
- **Validazione tecnica**: tipo MIME e dimensione dei file, integrità, schema dei dati HRIS importati; fallire presto con messaggi chiari.
- **Modello human-in-the-loop**: le competenze inferite con confidenza non massima entrano come **"proposte"** (non come fatti) finché non confermate dalla persona o da un curatore.
- **Immutabilità e auditabilità**: ogni modifica (inferenza, conferma, correzione, decadimento) crea una nuova versione tracciata; lo storico è la base sia per l'audit sia per i diritti dell'interessato (accesso, rettifica, cancellazione).
- **Anonimizzazione/aggregazione** per le viste di direzione: i dati individuali sensibili (es. "chi è debole in cosa") non devono essere esposti in chiaro in dashboard strategiche.

## 4. Flusso dell'attività (step-by-step)

Il flusso descrive l'esperienza end-to-end, dalla configurazione iniziale all'uso ricorrente. È pensato per l'MVP ma indica anche i punti di evoluzione. Attraversa tre attori principali: l'**amministratore/HR** che configura, il **sistema/AI** che inferisce, e la **persona** che valida e usa.

### Fase A — Setup e fondazione del grafo

1. **Inizializzazione del dominio.** L'amministratore seleziona il dominio enterprise "Persone & competenze", sceglie lingua interfaccia (IT/EN) e provider AI (default Ollama locale). Configura le policy di privacy di base (§3.7): basi giuridiche, fonti abilitate, default di visibilità.
2. **Import dell'anagrafica e dell'organigramma.** Si importano persone, ruoli, team, gerarchia e sedi da HRIS/AD/LDAP o CSV (§3.1). Il sistema crea i nodi \`Persona\`, \`Ruolo\`, \`Team\`, \`Sede\` e gli archi strutturali (\`APPARTIENE_A\`, \`RIPORTA_A\`, \`RICOPRE_RUOLO\`). Validazione dello schema e report degli scarti.
3. **Caricamento della tassonomia delle competenze.** Si importa una base standard (ESCO/O*NET) e/o si carica la tassonomia aziendale (§3.2). Il sistema crea i nodi \`Competenza\` con gerarchia, sinonimi e scale di livello (enum IT/EN). In assenza di tassonomia, si parte da una emergente che verrà normalizzata strada facendo.
4. **Definizione dei requisiti.** HR/manager definiscono i profili di ruolo e gli eventuali requisiti di progetto/posizione (§3.4), creando gli archi \`RICHIEDE\` (con livello-target) tra \`Ruolo\`/\`Progetto\` e \`Competenza\`. Questo è ciò che renderà possibile la gap analysis.

### Fase B — Ingestione dei segnali e inferenza delle competenze

5. **Collegamento delle sorgenti di evidenza.** L'amministratore abilita i connettori (documenti via dominio \`document\`, email/\`messaging\`, \`calendar\`, Git, Jira) rispettando le policy di consenso. Il folder watcher e la pipeline di ingestione esistenti iniziano a raccogliere i contenuti.
6. **Estrazione e attribuzione (il motore di inferenza).** Per ogni contenuto ingerito, la pipeline esegue: estrazione testo (Tika/OCR) → chunking → embedding su Qdrant → **estrazione di competenze e contesto** tramite l'AI locale (riuso/estensione di \`EntityExtractorPort\` del dominio \`knowledge\`). Il sistema collega la persona-autore/partecipante alle competenze rilevate, generando archi candidati \`POSSIEDE_COMPETENZA\` con un **livello di confidenza** e un riferimento all'evidenza (provenienza + timestamp).
7. **Calcolo del peso e del livello.** Per ogni coppia persona↔competenza, il sistema aggrega tutte le evidenze, applica il decadimento temporale e la pesatura per tipo di segnale (§5), e calcola un **peso complessivo** e un **livello stimato** sulla scala di padronanza. Le competenze sotto la soglia di confidenza restano "proposte".
8. **Risoluzione e normalizzazione.** Sinonimi e varianti (IT/EN, abbreviazioni) vengono fusi sulla tassonomia; eventuali nuove competenze emergenti vengono proposte per l'inserimento in tassonomia (curatela).

### Fase C — Validazione human-in-the-loop

9. **Revisione del profilo da parte della persona.** Ogni dipendente vede il proprio profilo competenze inferito, con le evidenze a supporto ("ti attribuiamo *Kafka* perché hai scritto questi 3 documenti e risolto questi 5 ticket"). Può **confermare, correggere il livello, rimuovere** una competenza o aggiungerne di auto-dichiarate, e indicare le proprie **aspirazioni**. Questo feedback retroagisce sul peso (§5) e migliora il modello.
10. **Endorsement e curatela.** I colleghi possono confermare competenze (endorsement pesato); un curatore/HR può validare le competenze emergenti e mantenere la qualità della tassonomia. Le policy di visibilità impostate dalla persona vengono applicate.

### Fase D — Uso: localizzazione esperti e composizione team

11. **Trova l'esperto (GraphRAG).** Un utente pone una domanda in linguaggio naturale ("chi conosce il pricing engine e ha lavorato col cliente Acme?"). L'AI traduce la domanda in una query sul grafo (match strutturale persona→competenza/progetto/cliente) combinata con ricerca semantica su Qdrant nei contenuti, ordina i candidati per peso/pertinenza/recency/disponibilità e restituisce una **risposta motivata con citazione delle evidenze** e dei percorsi del grafo. Rispetta sempre le visibilità.
12. **Composizione di team.** Dato un set di competenze richieste (da un progetto), il sistema propone combinazioni di persone che coprono i requisiti, evidenziando coperture parziali e gap, con possibilità di vincoli (sede, lingua, disponibilità).
13. **Onboarding e navigazione.** Un nuovo assunto naviga il grafo persone↔team↔competenze per orientarsi, e usa il tutor per sapere "a chi chiedere per cosa".

### Fase E — Analisi: gap, rischi e pianificazione

14. **Gap analysis.** HR/manager confrontano competenze possedute vs richieste (per persona, team, ruolo, organizzazione) e visualizzano i **buchi**: competenze mancanti, sottodimensionate, o presenti ma sotto il livello-target.
15. **Analisi del rischio (bus factor).** Il sistema evidenzia i **single point of knowledge**: competenze critiche presidiate da una sola persona (o poche), suggerendo azioni di mitigazione (documentare, formare un secondo presidio, cross-training).
16. **Pianificazione formazione e mobilità.** Sui gap individuati, il sistema suggerisce piani di L&D mirati (per persona e aggregati) e opportunità di mobilità interna (persone con competenze trasferibili verso ruoli/progetti scoperti). Integrabile con il dominio \`automation\` per workflow (es. proporre un corso, aprire una candidatura).

### Fase F — Mantenimento e loop continuo

17. **Aggiornamento incrementale.** Man mano che arrivano nuovi documenti, commit, ticket, il grafo si aggiorna: nuove evidenze rafforzano competenze, il decadimento indebolisce quelle non più dimostrate. Il profilo resta vivo senza compilazioni manuali periodiche.
18. **Monitoraggio della qualità.** Si misurano accuratezza delle inferenze (tasso di conferma/rifiuto), copertura del grafo, freschezza dei dati; il feedback rifinisce soglie e pesi.
19. **Gestione del ciclo di vita della persona.** Ingressi, cambi ruolo, cessazioni si riflettono nel grafo; per i cessati si attivano le policy di conservazione/cancellazione e, soprattutto, la **cattura della conoscenza prima dell'uscita** (chi raccoglie l'eredità delle competenze critiche).

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello estende il dominio \`knowledge\` esistente (\`KnowledgeEntity\`, \`KnowledgeRelation\`, \`EntityType\`, \`RelationType\`) con tipi specifici dell'ambito, riusando MySQL per la struttura e Qdrant per la semantica (nessun Neo4j). Gli \`EntityType\`/\`RelationType\` attuali (\`PERSON\`, \`ORGANIZATION\`, \`CONCEPT\`, \`WORKS_AT\`, \`PART_OF\`, \`RELATED_TO\`…) sono la base da specializzare.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi chiave | Mappa su \`EntityType\` |
|---|---|---|---|
| \`Persona\` | Dipendente/collaboratore | id interno, nome, email, stato, data ingresso, sede, lingue | \`PERSON\` |
| \`Competenza\` (Skill) | Capacità/conoscenza (tecnica, dominio, soft) | nome IT/EN, categoria, scala livelli, sinonimi | \`CONCEPT\` (specializzato) |
| \`Ruolo\` | Posizione/job role | titolo, livello, famiglia professionale | nuovo / \`CONCEPT\` |
| \`Team\` / Unità org. | Reparto, team, centro di costo | nome, tipo, sede | \`ORGANIZATION\` |
| \`Progetto\` | Iniziativa con fabbisogno di competenze | nome, periodo, stato, cliente | \`EVENT\`/nuovo |
| \`Certificazione\` | Attestato formale | ente, data, scadenza | nuovo |
| \`Evidenza\` | Fonte che dimostra una competenza | tipo (doc/email/commit/ticket), riferimento, data | collegato a \`DOCUMENT\` |
| \`Cliente\` / Dominio | Contesto di business di una competenza | nome, settore | \`ORGANIZATION\`/\`CONCEPT\` |
| \`Obiettivo/Capability\` | Capacità strategica desiderata | descrizione, priorità | \`CONCEPT\` |

### 5.2 Tipi di relazione (archi)

| Relazione | Da → A | Significato | Note |
|---|---|---|---|
| \`POSSIEDE_COMPETENZA\` | Persona → Competenza | Competenza posseduta a un certo livello | **Arco centrale pesato** (livello + confidenza) |
| \`RICHIEDE\` | Ruolo/Progetto → Competenza | Competenza richiesta a un livello-target | Base della gap analysis |
| \`ASPIRA_A\` | Persona → Competenza | Competenza che la persona vuole sviluppare | Mobilità/formazione |
| \`RICOPRE_RUOLO\` | Persona → Ruolo | Posizione attuale (o storica) | Temporale |
| \`APPARTIENE_A\` | Persona → Team | Membership organizzativa | \`PART_OF\` |
| \`RIPORTA_A\` | Persona → Persona | Relazione gerarchica | |
| \`HA_LAVORATO_A\` | Persona → Progetto | Esperienza progettuale | Sorgente di competenze |
| \`CONOSCE_CLIENTE\` | Persona → Cliente | Relazione/esperienza con un cliente | |
| \`DIMOSTRATA_DA\` | Competenza(di Persona) → Evidenza | Aggancio competenza↔prova | Provenienza/auditabilità |
| \`MENTORE_DI\` | Persona → Persona | Relazione di mentoring (su una competenza) | Trasferimento conoscenza |
| \`ENDORSED_BY\` | Competenza(di Persona) → Persona | Conferma da un collega | Aumenta il peso |
| \`PREREQUISITO_DI\` / \`CORRELATA_A\` | Competenza → Competenza | Struttura della tassonomia | Per percorsi e trasferibilità |
| \`COLLABORA_CON\` | Persona → Persona | Co-autoraggio/co-partecipazione frequente | Inferita dai segnali |

### 5.3 Criteri per il peso degli archi

Il peso è ciò che distingue questo grafo da un semplice elenco. Il **peso di un arco \`POSSIEDE_COMPETENZA\`** (e analogamente degli altri) è una funzione configurabile e trasparente dei seguenti fattori:

- **Quantità e qualità delle evidenze.** Più contenuti distinti dimostrano la competenza, maggiore il peso. Conta il *ruolo* nell'evidenza: autore di un documento > citato in un documento; risolutore di un ticket > commentatore.
- **Tipo di segnale (affidabilità).** Pesi differenziati per fonte (§3.3): commit/ticket risolti e progetti pesano più di una partecipazione a un meeting o di una semplice auto-dichiarazione. Le certificazioni pesano molto ma sono "formali" (vanno incrociate con la pratica).
- **Recency e decadimento temporale.** Le evidenze hanno una data; il contributo al peso decade nel tempo con una funzione configurabile (es. half-life di N mesi). Modella obsolescenza e mantiene il grafo "vivo".
- **Conferme umane.** La conferma esplicita della persona e gli **endorsement** dei colleghi (a loro volta pesati dalla competenza di chi endorsa) aumentano il peso e la confidenza; una correzione/rifiuto lo riduce drasticamente.
- **Confidenza dell'inferenza AI.** Lo score di estrazione dell'LLM contribuisce: sotto soglia la competenza resta "proposta", non "confermata".
- **Coerenza e densità contestuale.** Competenze coerenti con il ruolo, il team e altre competenze possedute (es. presenza di prerequisiti) rafforzano la plausibilità.

Il peso si normalizza poi su una **scala di livello** (1–5) per la lettura umana, mantenendo internamente lo score continuo. Tutti i parametri (pesi per tipo, half-life, soglie) sono configurabili (§3.6) e ogni peso è **spiegabile**: l'utente può vedere "perché" un valore è quello che è, condizione necessaria per la fiducia e per la compliance.

## 6. Fonti dati & connettori (ingestione)

| Fonte | Cosa apporta | Meccanismo | Stato |
|---|---|---|---|
| HRIS / sistema del personale | Anagrafica, ruoli, organigramma, certificazioni | Import CSV/API → connettore plugin | MVP (import file), evoluzione (API) |
| Active Directory / LDAP | Persone, gruppi, gerarchia, sedi | Connettore plugin | Evoluzione |
| Documenti aziendali | Autoria → competenze | Dominio \`document\` esistente (Tika/OCR, folder watcher) | Riuso diretto (MVP) |
| Email | Partecipazione a discussioni tematiche | Dominio \`email\` esistente (IMAP) | Riuso (con consenso) |
| Messaggistica / canali | Thread tecnici, Q&A interni | Dominio \`messaging\` esistente | Riuso (con consenso) |
| Calendario | Meeting/formazioni/relatore | Dominio \`calendar\` esistente | Riuso |
| Repository Git | Commit, PR, file → competenze tecniche | Connettore plugin PF4J (GitHub/GitLab/Bitbucket) | Evoluzione (alto valore) |
| Issue tracker (Jira, ecc.) | Ticket risolti → competenze operative | Connettore plugin PF4J | Evoluzione |
| LMS / piattaforme formazione | Corsi completati, certificazioni | Connettore plugin | Evoluzione |
| CV / profili (upload) | Base storica auto-dichiarata | Upload PDF via \`document\` | MVP |
| Input UI | Auto-dichiarazione, conferme, endorsement, aspirazioni | Frontend Angular | MVP |
| Standard skill (ESCO/O*NET) | Tassonomia base multilingue | Import dataset | MVP/evoluzione |

Tutti i connettori esterni specifici (Git, Jira, LMS, HRIS) vanno realizzati come **plugin PF4J** pubblicabili sul marketplace, coerentemente con l'architettura di estensibilità esistente, così da non gonfiare il core e da rispettare le diverse realtà aziendali.

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

### 7.1 MVP (fondazione: grafo, inferenza base, trova-esperto, gap base)

| # | Funzionalità | Descrizione | Componenti LocalMind coinvolti |
|---|---|---|---|
| 1 | Modello dati skill graph | Estendere \`EntityType\`/\`RelationType\` con tipi dell'ambito; entità JPA + migrazioni Flyway (una query per file) | \`knowledge\` domain, \`localmind-infrastructure\`, Flyway |
| 2 | Import anagrafica + tassonomia | Import CSV persone/org e tassonomia (custom + ESCO base) con validazione | nuovo dominio/estensione, API, frontend |
| 3 | Estrazione competenze da documenti | Estendere \`EntityExtractorPort\` per estrarre competenze+contesto dai contenuti già ingeriti, con AI Ollama locale | \`knowledge\`, \`document\`, Qdrant |
| 4 | Profilo competenze inferito + provenienza | Calcolo peso/livello + tracciamento evidenza per ogni coppia persona↔competenza | \`knowledge\` service, MySQL |
| 5 | Validazione human-in-the-loop | UI per confermare/correggere/aggiungere competenze e impostare visibilità | frontend Angular (Signals), API |
| 6 | Trova l'esperto (GraphRAG) | Query NL → grafo+semantica, risposta motivata con citazioni e rispetto visibilità | \`llm\` (GraphRAG), \`knowledge\`, Qdrant |
| 7 | Gap analysis base | Confronto possedute vs richieste per ruolo/team, vista buchi | \`knowledge\` service, frontend |
| 8 | Visualizzazione grafo (sottoinsieme) | Vista persone↔competenze↔team navigabile e filtrabile | frontend (componente grafo) |
| 9 | Privacy & consenso base | Policy di visibilità, opt-out per fonte, audit delle evidenze | auth/\`common\`, config |

### 7.2 Evoluzione (connettori, analisi avanzate, mobilità)

| # | Funzionalità | Descrizione |
|---|---|---|
| 10 | Connettori Git/Jira/LMS (plugin PF4J) | Inferenza competenze da commit, ticket risolti, corsi; alto valore di evidenza |
| 11 | Connettori HRIS/AD via API | Sincronizzazione continua anagrafica/org/gerarchia |
| 12 | Decadimento temporale & freschezza | Modellazione obsolescenza, dashboard freschezza del grafo |
| 13 | Bus factor / single point of knowledge | Identificazione e alert su competenze critiche mono-presidiate |
| 14 | Composizione team automatica | Suggerimento combinazioni persone per coprire requisiti di progetto |
| 15 | Endorsement & mentoring | Conferme tra colleghi pesate; matching mentore↔allievo su skill |
| 16 | Marketplace interno talenti | Matching persone↔opportunità/posizioni interne con competenze trasferibili |
| 17 | Piani di formazione mirati | Raccomandazioni L&D per persona e aggregate, agganciate ai gap |
| 18 | Workforce planning strategico | Confronto capability vs roadmap, simulazioni "what-if" sui gap futuri |
| 19 | Dashboard direzionali aggregate/anonime | Viste strategiche con anonimizzazione e aggregazione |
| 20 | Knowledge capture pre-uscita | Workflow per catturare/trasferire competenze critiche prima delle cessazioni |
| 21 | Normalizzazione ontologica avanzata | Fusione sinonimi, mapping multi-standard ESCO↔O*NET↔custom |

### 7.3 Da mantenere (continuità operativa)

- **Qualità della tassonomia**: curatela continua di competenze, sinonimi, gerarchie; deduplica.
- **Accuratezza dell'inferenza**: monitoraggio tasso conferma/rifiuto, ricalibrazione soglie e pesi.
- **Freschezza dei dati**: ingestione incrementale, gestione decadimento, riallineamento con HRIS.
- **Compliance**: revisione periodica di basi giuridiche, consensi, policy di conservazione; gestione richieste GDPR (accesso, rettifica, cancellazione).
- **Traduzioni IT/EN**: enum (livelli, categorie), etichette UI, documentazione sempre bilingui.
- **Performance del grafo**: indici MySQL e collezioni Qdrant ottimizzati man mano che il grafo cresce.

## 8. Casi d'uso AI / GraphRAG

1. **Localizzazione esperto in linguaggio naturale.** "Chi può aiutarmi con un'integrazione Stripe e ha già lavorato in ambito fintech?" → l'AI combina match strutturale (Competenza *Stripe/Payments*, Dominio *fintech*) e semantico (contenuti prodotti), ordina per peso/recency/disponibilità, risponde con i nomi e le **evidenze** che li giustificano.
2. **Composizione di team.** "Componi un team per un progetto che richiede React senior, Java mid, UX e conoscenza del cliente Acme" → propone combinazioni che coprono i requisiti, evidenziando gap residui.
3. **Spiegazione di un'attribuzione.** "Perché mi attribuite la competenza Kafka a livello 4?" → l'AI elenca le evidenze pesate (documenti, ticket, commit, endorsement) con date.
4. **Gap analysis conversazionale.** "Quali competenze ci mancano per affrontare un progetto Kubernetes nei prossimi 6 mesi?" → confronta capability richieste vs possedute e suggerisce formare/assumere/ridistribuire.
5. **Analisi del rischio.** "Quali competenze critiche dipendono da una sola persona?" → elenca i single point of knowledge con suggerimenti di mitigazione.
6. **Suggerimento di collegamenti non evidenti.** L'AI propone che due persone in team diversi condividono una competenza rara e potrebbero collaborare/fare mentoring (arco emergente \`COLLABORA_CON\`/\`MENTORE_DI\`).
7. **Percorso di crescita personale.** "Voglio diventare Tech Lead: cosa mi manca?" → confronta profilo con il ruolo target, propone competenze da sviluppare e mentori interni.
8. **Onboarding assistito.** "Sono nuovo nel team pagamenti: a chi mi rivolgo per cosa?" → naviga il grafo e restituisce la mappa di "chi sa cosa".

Tutti i casi d'uso rispettano le **policy di visibilità** e citano le fonti; l'inferenza gira su **Ollama locale** per default, senza esfiltrazione di dati.

## 9. KPI & metriche di successo

| Categoria | Metrica | Perché conta |
|---|---|---|
| Copertura | % persone con profilo competenze popolato; n. medio competenze/persona | Misura quanto il grafo è "completo" |
| Qualità inferenza | Tasso di conferma vs rifiuto delle competenze proposte; precisione/recall su campione | Affidabilità del motore |
| Freschezza | % competenze con evidenza recente (< N mesi); età media delle evidenze | Il grafo è vivo, non un fossile |
| Adozione | Query "trova l'esperto"/settimana; utenti attivi; % profili rivisti dai dipendenti | Valore percepito e partecipazione |
| Efficienza | Tempo medio per trovare un esperto (prima vs dopo) | ROI operativo diretto |
| Mobilità | N. match interni andati a buon fine; % posizioni coperte internamente | Valorizzazione talenti interni |
| Rischio | N. single point of knowledge critici; trend nel tempo | Riduzione fragilità organizzativa |
| Gap | % copertura competenze richieste per ruolo/progetto | Allineamento capacità↔fabbisogno |
| Compliance | % ingestioni con base giuridica e consenso validi; tempo di evasione richieste GDPR | Sostenibilità legale del sistema |

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---|---|---|
| **Privacy/conformità** (trattamento dati lavoratori) | Alto — legale e di fiducia | Local-first, base giuridica esplicita, consenso/opt-out granulare, human-in-the-loop, audit, anonimizzazione nelle viste aggregate |
| **Sorveglianza percepita** dei dipendenti | Alto — rifiuto culturale | Trasparenza totale (la persona vede e controlla il suo profilo), framing su crescita/opportunità non su controllo, governance condivisa |
| **Inferenze errate/bias** | Medio-alto | Confidenza esplicita, "proposte" vs "confermate", validazione umana, spiegabilità del peso, monitoraggio del bias |
| **Obsolescenza dei dati** | Medio | Decadimento temporale, ingestione incrementale, metriche di freschezza |
| **Tassonomia incoerente** (sinonimi, granularità) | Medio | Import standard (ESCO/O*NET), normalizzazione, curatela, gestione sinonimi IT/EN |
| **Riduzione delle persone a un punteggio** | Medio — etico | Mai usare il grafo come unico criterio per decisioni HR; competenza ≠ valore della persona; human-in-the-loop nelle decisioni |
| **Qualità delle fonti** (segnali rumorosi) | Medio | Pesatura per affidabilità del segnale, minimizzazione, deduplica |
| **Scalabilità del grafo su MySQL+Qdrant** | Medio | Indici mirati, query a sottografo, denormalizzazioni dove serve; rivalutare datastore a grafo solo se necessario |
| **Bassa adozione** | Medio | Valore immediato (trova-esperto), zero attrito (inferenza automatica), benefici visibili al dipendente |

## 11. Manutenzione & evoluzione

- **Governance della tassonomia.** Nominare curatori (HR + referenti tecnici) che mantengono competenze, sinonimi e gerarchie; processo per accettare le competenze emergenti dall'AI. Allineamento periodico con gli standard (ESCO/O*NET) e con la realtà del mercato.
- **Calibrazione continua del motore.** Rivedere periodicamente pesi per tipo di segnale, half-life del decadimento e soglie di confidenza, sulla base del tasso di conferma/rifiuto e del feedback.
- **Ciclo di vita dei dati.** Politiche di conservazione/cancellazione coerenti con il ciclo di vita della persona (ingresso, cambio ruolo, cessazione) e con i diritti GDPR; cattura della conoscenza prima delle uscite.
- **Estensione dei connettori.** Aggiungere progressivamente connettori (Git, Jira, LMS, HRIS) come plugin PF4J, pubblicandoli sul marketplace; ogni connettore con la propria configurazione di consenso.
- **Bilinguismo permanente.** Mantenere enum, etichette e documentazione IT/EN; ogni nuova competenza/categoria nasce con entrambe le lingue.
- **Evoluzione verso il workforce planning.** Dal "chi sa cosa" attuale verso scenari "what-if" e pianificazione anticipata dei gap rispetto alla roadmap aziendale.
- **Tracciamento sviluppi.** Ogni intervento documentato nella cartella \`Sviluppi/\` con la nomenclatura datata prevista dal progetto, in plan mode, aggiornando costantemente la documentazione IT/EN.

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo / dominio | Ruolo nell'ambito Persone & competenze |
|---|---|
| \`knowledge\` | **Fondamento**: lo skill graph estende \`KnowledgeEntity\`/\`KnowledgeRelation\`; si specializzano \`EntityType\`/\`RelationType\` e si riusa/estende \`EntityExtractorPort\` e \`KnowledgeGraphPort\`/\`KnowledgeGraphUseCase\` |
| \`document\` | Sorgente primaria di evidenza: documenti redatti → competenze (Tika/OCR, folder watcher, chunking già operativi) |
| \`email\` / \`messaging\` | Sorgenti di evidenza dalle discussioni; partecipazione tematica → competenze (con consenso) |
| \`calendar\` | Evidenza da meeting/formazioni/relatore; integrazione con disponibilità per la composizione team |
| \`llm\` | Motore di inferenza (estrazione competenze) e GraphRAG (trova-esperto), con fallback chain e **Ollama locale di default** |
| \`mcp\` | Esposizione di tool ("trova esperto", "gap analysis") ad agenti/LLM esterni via Model Context Protocol |
| \`agent\` | Agenti che orchestrano flussi multi-step (es. comporre team, preparare piano L&D) |
| \`automation\` | Workflow scatenati da eventi del grafo (gap rilevato → proponi corso; nuova competenza critica mono-presidiata → alert) |
| \`marketplace\` / \`plugin\` (PF4J) | Distribuzione dei connettori (HRIS, AD, Git, Jira, LMS) e di pacchetti di tassonomia (ESCO/O*NET) |
| \`finetuning\` | Eventuale specializzazione di un modello locale per l'estrazione di competenze nel gergo aziendale |
| \`auth\` / \`common\` | Sicurezza, policy di visibilità, audit, eventi di dominio per il tracciamento delle modifiche al grafo |
| Frontend Angular | Nuova feature \`people-skills\` (standalone, Signals, IT/EN): profilo competenze, trova-esperto, gap analysis, visualizzazione grafo |

Dal punto di vista realizzativo, l'ambito segue il pattern architetturale del progetto: dominio puro in \`localmind-domain\` (modelli, port/in, port/out, service senza Spring), wiring in \`DomainConfig\`, adapter in \`localmind-infrastructure\`, controller/DTO in \`localmind-api\`, migrazioni Flyway con **una sola query per file**, UUID con \`@JdbcTypeCode(SqlTypes.CHAR)\`, feature frontend lazy-loaded. L'ambito Persone & competenze è il **tessuto connettivo** del knowledge graph enterprise: dà un proprietario umano alla conoscenza e moltiplica il valore di tutti gli altri ambiti.

---

### Fonti e riferimenti (best practice 2026)

- [What Is a Skills Graph? The 2026 Guide for HR Leaders — 365Talents](https://365talents.com/en/resources/skills-graph-guide-hr-leaders/)
- [Skills ontology framework: Why You need it in 2026 — Gloat](https://gloat.com/blog/skills-ontology-framework/)
- [Skills Ontology: What Is It & How To Build One? — AIHR](https://www.aihr.com/blog/skills-ontology/)
- [Open Skills and Talent Graphs: Guide to Skills-Based Hiring — JobsPikr](https://www.jobspikr.com/blog/open-skills-and-talent-graphs-2025/)
- [Expertise Locators and Ask the Expert — Stan Garfield](https://stangarfield.medium.com/expertise-locators-and-ask-the-expert-f273db1e227c)
- [An Enterprise Knowledge Graph Approach (skills & expertise) — CEUR-WS](https://ceur-ws.org/Vol-3780/paper9.pdf)
- [Knowledge Graph (Employee Data) — ChangeEngine](https://www.changeengine.com/glossary/what-is-knowledge-graph-employee-data)
- [Understanding O*NET and ESCO: Standards for Skills — Pexelle](https://pexelle.com/understanding-onet-and-esco-standards-for-skills-in-the-modern-workforce/)
`;
