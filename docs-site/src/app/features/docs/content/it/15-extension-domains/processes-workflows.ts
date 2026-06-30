export const content = `# Processi & workflow

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo documento descrive l'ambito di estensione **Processi & workflow** (gruppo: *enterprise*) del motore di Knowledge Graph universale di LocalMind. L'obiettivo è trasformare la conoscenza dei processi aziendali — oggi dispersa tra diagrammi BPMN dimenticati, procedure operative (SOP) in PDF, regole non scritte nella testa delle persone e tracce di esecuzione sparse in decine di sistemi — in un **grafo pesato, vivo e interrogabile dall'AI** che collega *passi*, *ruoli*, *sistemi*, *documenti*, *decisioni* e *dati*, e che l'AI può percorrere (GraphRAG) per rispondere a domande come "chi approva una nota spese sopra i 5.000 € e quale sistema usa?", "dove si blocca davvero il processo di onboarding?" oppure "se va via Maria, quali processi restano senza un approvatore?".

L'ambito riusa integralmente lo stack esistente (Spring Boot esagonale, Angular 21, MySQL 8.0 per la struttura del grafo, Qdrant per la semantica, Ollama come AI locale di default) e viene veicolato come **modulo di dominio installabile** tramite il sistema plugin PF4J + marketplace, nel pieno rispetto dei vincoli LocalMind: local-first / self-hostable, privacy assoluta dei dati di processo aziendali, open source e bilinguismo IT/EN. È il primo ambito in cui il grafo non descrive *oggetti* (luoghi, immobili, documenti) ma **comportamento organizzato nel tempo**: il *come* l'azienda lavora.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema reale: la conoscenza di processo è invisibile e frammentata

In quasi ogni organizzazione il "come si fa una cosa" esiste in almeno cinque forme **non collegate tra loro**, e nessuna è la verità completa:

1. **Il processo *progettato* (to-be).** Vive in diagrammi BPMN, mappe Visio, slide, manuali della qualità ISO 9001, procedure operative standard (SOP) e policy. È formale ma quasi sempre **obsoleto**: descrive come il processo *dovrebbe* funzionare, raramente come funziona oggi. È scritto in linguaggi diagrammatici che, come rileva la letteratura sui knowledge graph, non sono direttamente utilizzabili dall'AI senza una trasformazione esplicita in nodi e relazioni.

2. **Il processo *eseguito* (as-is).** Vive nei log di esecuzione dei sistemi (ERP, CRM, ticketing, gestionale HR, workflow engine, mail, calendario). È la verità *fattuale* — chi ha fatto cosa, quando, in quale ordine — ma è illeggibile per un essere umano e disperso su decine di applicativi che non si parlano. È il territorio del *process mining*.

3. **Il processo *vissuto* (tribal knowledge).** Vive nella testa delle persone: "la fattura sopra 10k la deve vedere anche il CFO", "se il cliente è strategico salti il passo X", "Marco sa come sbloccare il sistema legacy". È la conoscenza più preziosa e la più **fragile**: se ne va con chi lascia l'azienda, non è cercabile, non è verificabile.

4. **Il processo *automatizzato*.** Vive in script, RPA, workflow engine, integrazioni, cron job, regole di routing. Funziona finché funziona, ma **nessuno ha la mappa** di quali automazioni toccano quali passi e cosa succede se una si rompe.

5. **Il processo *normato*.** Vincoli di compliance, separazione dei compiti (SoD), audit trail, GDPR, requisiti contrattuali. Spesso scollegato dall'esecuzione reale, quindi le violazioni si scoprono solo durante un audit.

Il dolore strutturale è che **queste cinque viste non sono collegate**. Quando qualcuno chiede "come funziona il processo di approvazione acquisti?", la risposta richiede di triangolare a mano un diagramma vecchio, l'esperienza di tre colleghi, i log dell'ERP e il manuale qualità — un lavoro da giorni che produce una risposta parziale e non tracciabile.

### 1.2 Le conseguenze misurabili

| Sintomo | Costo per l'organizzazione |
|---------|----------------------------|
| **Knowledge loss / bus factor.** La conoscenza di processo se ne va con le persone | Onboarding lentissimo, dipendenza da singoli, paralisi quando manca una persona chiave |
| **Onboarding lento.** Un nuovo assunto impiega settimane a capire "chi fa cosa e con quale sistema" | Tempo di produttività ritardato, carico sui senior |
| **Bottleneck invisibili.** Nessuno sa dove il processo si impalla davvero | Lead time gonfiati, ritardi cronici accettati come normali |
| **Disallineamento to-be / as-is.** Il processo reale diverge da quello documentato | Rischio compliance, audit dolorosi, decisioni su mappe sbagliate |
| **Automazioni opache.** Nessuna mappa delle dipendenze tra automazioni e passi | Effetti a catena imprevisti quando si cambia un sistema |
| **Onboarding di un cambiamento.** "Se introduco questo nuovo step / cambio approvatore, cosa impatto?" senza risposta | Cambiamenti rischiosi, resistenza al miglioramento |

### 1.3 La nostra risposta: il grafo processo–ruolo–sistema

LocalMind modella i processi come **grafo pesato e interrogabile dall'AI** che unifica le cinque viste in un'unica rete navigabile. Non un nuovo diagramma BPMN da mantenere a mano, ma un **digital twin leggero dell'organizzazione che lavora**:

- Ogni **Processo** è composto da **Passi/Attività** legati da relazioni di sequenza, condizione e parallelismo (\`PRECEDE\`, \`INNESCA\`, \`CONDIZIONATO_DA\`).
- Ogni passo è collegato al **Ruolo** che lo esegue, lo approva o ne è informato — con semantica **RACI** (Responsible, Accountable, Consulted, Informed) sugli archi.
- Ogni passo è collegato al **Sistema/Applicativo** in cui viene svolto (\`ESEGUITO_IN\`), ai **Dati/Documenti** che produce o consuma (\`PRODUCE\`, \`RICHIEDE\`), alle **Regole/Policy** che lo vincolano (\`GOVERNATO_DA\`) e alle **Automazioni** che lo eseguono in tutto o in parte (\`AUTOMATIZZATO_DA\`).
- Le **persone** sono collegate ai ruoli (\`RICOPRE\`), così il grafo sa rispondere non solo "quale ruolo approva" ma "*chi*, concretamente, oggi".
- I **pesi degli archi** derivano da fattori fattuali ed esperienziali: frequenza reale di esecuzione (da process mining), tempo medio di attraversamento, criticità, frequenza di blocco, livello di automazione, affidabilità della fonte.

Su questo grafo l'AI locale (GraphRAG) **naviga, spiega, simula e fa emergere collegamenti non evidenti**: combina la struttura del grafo (percorsi, dipendenze, ruoli) con la semantica (descrizioni di SOP, mail, ticket embeddati su Qdrant), e cita sempre i nodi/percorsi usati. Questo è esattamente l'approccio descritto dalla ricerca recente su **GraphRAG applicato al process mining** (dual indexing: recupero su grafo + recupero semantico vettoriale, *workflow-aware*).

### 1.4 Le quattro fonti di verità, riconciliate

Il valore distintivo è **riconciliare il to-be con l'as-is**. LocalMind non si limita a digitalizzare il diagramma: confronta il processo progettato con quello realmente eseguito (ricavato dai log dei sistemi) e fa emergere lo scarto — la *conformance*. Il grafo diventa così l'unico luogo dove convivono:

- la **norma** (to-be: SOP, BPMN, policy) → cosa dovrebbe succedere;
- il **fatto** (as-is: event log) → cosa succede davvero;
- l'**esperienza** (tribal: contributi delle persone) → perché succede e come si gestiscono le eccezioni;
- l'**automazione** (script/RPA) → cosa è già senza intervento umano.

### 1.5 Valore per tipo di utente

| Utente | Valore concreto |
|--------|-----------------|
| Process owner / responsabile operations | Vede il processo reale, non quello immaginato; individua bottleneck e scarti dalla norma; simula i cambiamenti prima di farli |
| Nuovo assunto | Chiede in linguaggio naturale "come faccio X, chi devo coinvolgere, quale sistema uso" e ottiene risposta tracciabile, non un PDF di 80 pagine |
| Auditor / compliance / qualità | Verifica la separazione dei compiti, l'audit trail, lo scostamento as-is/to-be; prepara audit ISO/GDPR con evidenze dal grafo |
| Manager / continuous improvement | Quantifica colli di bottiglia, costi di handoff, lead time; prioritizza le automazioni dove pesano di più |
| Knowledge manager / HR | Cattura e preserva il tribal knowledge prima che le persone lascino; misura il bus factor |
| Team IT / enterprise architect | Mappa quali sistemi sostengono quali processi; valuta l'impatto della dismissione/migrazione di un applicativo |
| Self-hoster / DPO | Tutto on-prem: dati di processo sensibilissimi restano in casa, AI locale di default |

### 1.6 Perché LocalMind e non un BPM suite / process mining tradizionale

- **Local-first e privacy assoluta.** I processi aziendali e i log di esecuzione sono tra i dati più sensibili che esistano (rivelano organizzazione, costi, persone, debolezze). Le suite di process mining cloud (Celonis, ecc.) richiedono di esportare questi dati. LocalMind li tiene **interamente on-prem**, con l'AI Ollama in locale di default. Nessun dato lascia l'istanza senza consenso esplicito.
- **Un grafo unificato, non quattro silos.** Le suite BPM modellano il to-be, i tool di process mining l'as-is, i wiki il tribal knowledge: nessuno li unisce. LocalMind è l'unico strato dove convivono e si interrogano insieme.
- **AI conversazionale nativa sul processo.** Non dashboard da imparare, ma domande in linguaggio naturale con risposte spiegate e citate.
- **Universalità ed economia del motore.** Lo stesso engine che serve turismo, immobiliare ed enterprise serve i processi: cambiano i tipi di nodo/relazione, non l'infrastruttura. Costo di sviluppo e manutenzione abbattuto.
- **Open source ed estensibile.** Connettori a ERP/CRM/ticketing come plugin PF4J pubblicabili sul marketplace; nessun lock-in.

### 1.7 Cosa NON è (confini di valore)

Non è un **workflow engine esecutivo** che orchestra e *esegue* i processi al posto dei sistemi aziendali (quello è, in parte, il dominio \`automation\` esistente, con cui ci si integra). Non è una **BPM suite di modellazione** che sostituisce gli strumenti di disegno BPMN. Non è una **piattaforma di process mining enterprise certificata** per audit legali. È uno **strato di conoscenza e intelligenza** *sopra* processi progettati, eseguiti e vissuti: li mappa, li collega, li spiega e ne fa emergere problemi e collegamenti — restando local-first.

---

## 2. Personas & utenti target

| Persona | Profilo | Obiettivo principale | Bisogni dal grafo |
|---------|---------|----------------------|-------------------|
| **Laura, 44 — Process Owner Operations** | Responsabile dei processi order-to-cash di una PMI manifatturiera | Capire dove il processo perde tempo e renderlo più snello | Bottleneck reali, lead time per passo, scarto as-is/to-be, simulazione "what-if" |
| **Paolo, 29 — nuovo assunto** | Appena entrato nell'ufficio acquisti | Diventare operativo in fretta senza disturbare i colleghi | "Come faccio un ordine, chi approva, in quale sistema, quali documenti servono" |
| **Anna, 51 — Auditor interno / Qualità** | Gestisce certificazione ISO 9001 e compliance GDPR | Dimostrare che i processi sono seguiti e i compiti separati | Audit trail, separazione dei compiti (SoD), evidenze di conformità, deviazioni |
| **Marco, 38 — Continuous Improvement / Lean** | Black belt che guida progetti di efficienza | Prioritizzare dove intervenire e misurare l'impatto | Colli di bottiglia quantificati, costi di handoff, candidati ad automazione |
| **Giulia, 47 — HR / Knowledge Manager** | Preoccupata dal turnover dei senior | Catturare la conoscenza prima che le persone vadano via | Bus factor per processo/passo, tribal knowledge mappato, ruoli scoperti |
| **Davide, 41 — Enterprise Architect / IT** | Pianifica la migrazione di un ERP legacy | Sapere quali processi dipendono da quale sistema | Mappa processo↔sistema, impatto della dismissione di un applicativo |
| **CFO / Risk Manager** | Sponsor del progetto | Ridurre rischio operativo e dipendenza da singoli | Vista d'insieme dei rischi: bottleneck, bus factor, violazioni SoD |
| **Self-hoster / DPO** | Tecnico che installa LocalMind on-prem | Pipeline di conoscenza di processo privata e controllata | Connettori, API grafo, controllo totale e residenza dei dati |

**Anti-persona:** la microimpresa con due persone e processi banali, dove la mappatura formale non ripaga lo sforzo. Il valore di LocalMind cresce con la **complessità organizzativa**: più ruoli, più sistemi, più handoff, più turnover, più compliance.

---

## 3. Requisiti in input

Questa sezione è deliberatamente dettagliata: definisce *tutto ciò che entra* nel grafo dei processi, *da quali fonti*, *in quale forma* e *come viene validato al boundary*. Gli input si dividono in cinque famiglie: input strutturali (la mappa del processo), input fattuali (l'esecuzione reale), input esperienziali (il tribal knowledge), input organizzativi (ruoli/persone/sistemi) e input di configurazione (come pesare e governare il grafo). Per ognuno valgono i principi LocalMind: validazione fail-fast al boundary, schema-based validation, mai fidarsi del dato esterno, e nessun dato sensibile fuori dall'istanza senza consenso.

### 3.1 Input strutturali — la definizione del processo (to-be)

Descrivono come il processo *dovrebbe* funzionare. Possono arrivare da import, da editor interno o da estrazione AI da documenti.

| Input | Forma / origine tipica | Validazione al boundary |
|-------|------------------------|-------------------------|
| **Definizione processo** | nome, dominio/area, owner, versione, obiettivo | nome non vuoto, owner risolvibile a un ruolo |
| **Passi / attività** | elenco ordinato di attività con descrizione | almeno 2 passi; descrizione presente |
| **Sequenza e flusso** | relazioni passo→passo, gateway/condizioni, rami paralleli | nessun ciclo non intenzionale non segnalato; un solo start, almeno un end |
| **Eventi** | start, end, eventi intermedi, timer, eccezioni | tipizzazione coerente (start/intermediate/end) |
| **Decisioni / gateway** | condizioni di diramazione (es. "importo > 5.000 €") | espressione parsabile o testo libero etichettato |
| **Input/output di passo** | documenti/dati richiesti e prodotti per ogni passo | riferimento a un tipo di Dato/Documento esistente |
| **SLA / tempi attesi** | durata target per passo o per processo | numerico ≥ 0, unità di tempo |
| **Diagramma BPMN/XML** | file \`.bpmn\` / \`.xml\` importato | XML ben formato, conforme a BPMN 2.0 (validazione schema) |

**Regola chiave:** ogni passo, prima di entrare nel grafo, deve avere almeno *un attore* (ruolo) e *un contesto* (sistema o "manuale"). Un passo "orfano" (senza chi lo fa né dove) entra come **nodo da arricchire**, evidenziato come lacuna di conoscenza.

### 3.2 Input fattuali — l'esecuzione reale (as-is, event log)

È la materia prima del **process mining**: tracce di esecuzione estratte dai sistemi. Senza questi input il grafo resta una bella mappa teorica; con essi diventa un digital twin verificato.

Un *event log* minimale richiede tre colonne (il "MXML/XES minimale"):

| Campo evento | Significato | Obbligatorio |
|--------------|-------------|--------------|
| **Case ID** | identificativo dell'istanza di processo (es. n. ordine, ticket, pratica) | sì — senza questo non si ricostruiscono le tracce |
| **Activity** | nome dell'attività/passo eseguito | sì |
| **Timestamp** | quando è avvenuto l'evento (inizio e/o fine) | sì — è ciò che dà l'ordine e i tempi |
| **Resource / attore** | chi (persona/ruolo/sistema) ha eseguito l'evento | no (ma fortissimo valore per ruoli e SoD) |
| **Attributi di caso** | importo, cliente, priorità, esito… | no (abilitano segmentazione e condizioni) |

| Sorgente di event log | Esempi | Note |
|-----------------------|--------|------|
| ERP / gestionale | ordini, fatture, movimenti magazzino | spesso la sorgente più ricca |
| CRM | lead, opportunità, ticket commerciali | per processi vendita |
| Ticketing / ITSM | Jira, ServiceNow, GLPI, Zammad | per processi IT/supporto |
| Workflow engine / BPM | log del motore esistente | as-is "nativo" |
| Mail e calendario | thread, inviti, approvazioni via mail | riuso moduli \`email\`/\`calendar\` |
| Automazioni / RPA | log di script ed esecuzioni | per la vista automazione |
| Log applicativi generici | CSV/DB esportati | normalizzati dal connettore |

**Validazione e qualità del log:** dedup eventi, normalizzazione timestamp e fuso orario, mapping dei nomi-attività eterogenei verso le attività canoniche del processo, gestione delle tracce incomplete. Un caso senza Case ID valido **non entra** nel calcolo delle tracce (degrada a evento isolato).

### 3.3 Input esperienziali — il tribal knowledge (contributi delle persone)

È il differenziatore che nessun process mining puro cattura. Le persone arricchiscono il grafo con conoscenza qualitativa, in linguaggio naturale, che l'AI struttura.

- **Annotazioni sui passi:** "questo step in pratica lo salta sempre il commerciale senior", "se il cliente è pubblica amministrazione serve la marca da bollo".
- **Eccezioni e workaround non documentati:** come si gestiscono i casi fuori standard.
- **Regole implicite:** soglie, deroghe, gerarchie informali ("sopra 10k vuole vedere anche il CFO").
- **Punti di dolore percepiti:** "qui si perde sempre un giorno perché aspettiamo l'IT".
- **Conoscenza di chi fa davvero cosa:** spesso diversa dall'organigramma formale.

Questi contributi entrano come nodi/attributi \`Annotazione\`/\`Eccezione\`/\`Regola\` collegati ai passi, **embeddati su Qdrant** per la ricerca semantica, e pesati per affidabilità (chi contribuisce, consenso, recency). Massima sensibilità privacy: restano nell'istanza.

### 3.4 Input organizzativi — ruoli, persone, sistemi (il contesto del processo)

Il cuore del focus "relazioni step–ruolo–sistema". Possono arrivare da import HR/IT o essere costruiti incrementalmente.

| Input | Forma / origine | Note |
|-------|-----------------|------|
| **Ruoli / funzioni** | catalogo ruoli aziendali (es. Buyer, Approver L1, CFO) | base della semantica RACI |
| **Persone** | anagrafica (eventualmente da HR/AD/LDAP) | collegate ai ruoli con \`RICOPRE\`; dato personale → privacy |
| **Organigramma** | gerarchia di riporto | per escalation e impatto |
| **Sistemi / applicativi** | catalogo IT (ERP, CRM, tool) | nodi \`Sistema\`; collegati ai passi con \`ESEGUITO_IN\` |
| **Dati / documenti** | tipi di documento/dato trattati | collegati con \`PRODUCE\`/\`RICHIEDE\`; riuso dominio \`document\` |
| **Policy / regole / SOP** | manuali qualità, regolamenti, vincoli compliance | nodi \`Regola\`; collegati con \`GOVERNATO_DA\` |
| **Automazioni** | script, RPA, integrazioni, cron | nodi \`Automazione\`; riuso dominio \`automation\` |
| **Matrice RACI** | assegnazione attività↔ruolo↔responsabilità | tradotta in archi pesati \`RESPONSABILE\`/\`APPROVA\`/\`CONSULTATO\`/\`INFORMATO\` |

### 3.5 Input di configurazione (come pesare e governare il grafo)

Per personalizzazione, self-hosting e governance:

- **Fattori e funzioni di peso** degli archi: quanto contano frequenza d'uso, criticità, tempo, automazione, affidabilità della fonte (vedi §5.3); ognuno configurabile.
- **Funzioni di decadimento per freshness:** un dato di esecuzione di un anno fa pesa meno di uno di ieri; una SOP non aggiornata da tre versioni perde peso.
- **Cadenze di refresh:** event log (giornaliero/orario), documenti SOP (on-change), organigramma (mensile).
- **Soglie di conformance:** quanto scostamento as-is/to-be far scattare un alert.
- **Regole di SoD (separazione dei compiti):** coppie di attività che non devono essere svolte dalla stessa persona (es. "chi crea il fornitore non può approvare il pagamento").
- **Livelli di sensibilità e accesso:** quali processi/passi sono riservati, mappati sul modello auth/multi-tenant.
- **Default RACI di dominio** sovrascrivibili per processo.

### 3.6 Riepilogo dei flussi di input

| Sorgente | Cadenza | Destinazione nel sistema |
|----------|---------|--------------------------|
| Import BPMN / editor interno | on-change | nodi \`Processo\`/\`Passo\` + archi di sequenza (MySQL) |
| Connettore event log (ERP/CRM/ticketing/mail) | giornaliera/oraria | tracce → frequenze, tempi, archi \`PRECEDE\` pesati |
| Documenti SOP / policy (upload) | on-change | estrazione AI passi/ruoli + embedding (Qdrant) |
| Contributi tribal (UI) | continua | \`Annotazione\`/\`Eccezione\`/\`Regola\` + embedding |
| Import HR/AD/LDAP | mensile | \`Persona\`, \`Ruolo\`, \`RICOPRE\` (con privacy) |
| Catalogo IT / CMDB | on-change | \`Sistema\`, archi \`ESEGUITO_IN\` |
| Modulo \`automation\` | continua | \`Automazione\`, archi \`AUTOMATIZZATO_DA\` |
| Config pesi/SoD/freshness | on-demand | parametri di pesatura e governance |

---

## 4. Flusso dell'attività (step-by-step)

Il flusso descrive il percorso end-to-end, dalla costruzione del grafo alla decisione/azione consapevole. È diviso in **Fase A — costruzione e riconciliazione del grafo** (asincrona, batch + AI) e **Fase B — interazione, interrogazione e governance** (sincrona, guidata dall'AI). È pensato per essere *incrementale*: il grafo funziona già con la sola vista to-be, e migliora man mano che si aggiungono event log e tribal knowledge.

### 4.1 Fase A — Costruzione e riconciliazione del grafo

**Step A1 — Acquisizione della definizione di processo (to-be).** Si parte da una delle tre vie: (a) import di un file **BPMN 2.0/XML**, trasformato automaticamente in nodi \`Processo\`/\`Passo\`/\`Evento\`/\`Gateway\` e archi di sequenza (sulla scia degli approcci *BPMN2KG* in letteratura); (b) **estrazione AI da SOP/policy**: i documenti procedurali (riuso pipeline \`document\` Tika/OCR) vengono letti dall'LLM locale che propone passi, ruoli, sistemi e sequenza, sottoposti a validazione umana; (c) **editor interno** in cui l'utente disegna il processo. In tutti i casi nasce lo scheletro to-be.

**Step A2 — Aggancio del contesto organizzativo.** Ogni passo viene collegato a \`Ruolo\` (chi), \`Sistema\` (dove), \`Dato/Documento\` (con cosa) e \`Regola/Policy\` (sotto quali vincoli). Se è disponibile una **matrice RACI**, le assegnazioni diventano archi tipizzati (\`RESPONSABILE\`, \`APPROVA\`, \`CONSULTATO\`, \`INFORMATO\`). Passi senza attore o senza contesto vengono marcati come **lacune da arricchire**.

**Step A3 — Ingestione semantica.** Descrizioni dei passi, testi delle SOP, annotazioni e regole vengono chunked ed embeddati su **Qdrant** (riuso pipeline esistente), abilitando la ricerca semantica e il recupero ibrido del GraphRAG. Le persone/ruoli/sistemi diventano entità ancorabili nelle query in linguaggio naturale.

**Step A4 — Ingestione dell'esecuzione reale (event log).** I connettori (§6) acquisiscono i log dai sistemi. Il modulo \`batch\` orchestra estrazione, normalizzazione (timestamp, fuso, dedup) e **mapping dei nomi-attività** eterogenei verso le attività canoniche del processo. Si ricostruiscono le **tracce per Case ID**.

**Step A5 — Process discovery (as-is).** Dalle tracce si deriva il modello *realmente eseguito*: quali transizioni passo→passo avvengono davvero e con quale **frequenza**, quali percorsi alternativi esistono, quali rami non vengono mai usati. Si materializzano/aggiornano gli archi \`PRECEDE\` con peso = frequenza reale, e i tempi medi di attraversamento per passo e per transizione.

**Step A6 — Conformance checking (riconciliazione to-be vs as-is).** Si confronta il processo progettato (A1) con quello eseguito (A5): si individuano **deviazioni** (passi saltati, attività non previste, ordini violati, approvazioni mancanti). Le deviazioni diventano attributi/archi sul grafo (\`DEVIA_DA\`) ed eventi per la governance. È il cuore del valore: rendere visibile lo scarto tra norma e realtà.

**Step A7 — Calcolo di bottleneck e indici di processo.** Usando i timestamp si arricchisce il grafo con: **tempo di attesa e di servizio per passo**, **colli di bottiglia** (dove il tempo si accumula), **frequenza di blocco/rework**, **lead time** end-to-end, **livello di automazione** per ramo. Diventano attributi dei nodi \`Passo\`/\`Processo\`.

**Step A8 — Analisi organizzativa.** Dal log con la colonna *resource* si ricava la **rete sociale di handoff** (chi passa il lavoro a chi), il **carico per ruolo/persona**, e — incrociando con l'organigramma — il **bus factor** (quanti passi dipendono da una sola persona) e le **violazioni di SoD** (stessa persona su attività incompatibili).

**Step A9 — Arricchimento esperienziale.** Le annotazioni tribal (§3.3) vengono collegate ai passi e pesate per affidabilità; spiegano il *perché* delle deviazioni emerse in A6 ("il passo X si salta perché in pratica…").

**Step A10 — Suggerimento di collegamenti non evidenti (GraphRAG building).** L'AI propone archi e insight non ovvi: processi che condividono lo stesso sistema/ruolo critico (rischio sistemico), passi candidati all'automazione (alta frequenza + manuale + ben definiti), processi "gemelli" duplicati in reparti diversi, dipendenze nascoste — coerente con il requisito di progetto "far emergere collegamenti non evidenti".

### 4.2 Fase B — Interazione, interrogazione e governance

**Step B1 — Espressione del bisogno.** L'utente scrive in linguaggio naturale ("chi approva un acquisto sopra 5.000 € e in quale sistema?", "dove si blocca l'onboarding?", "cosa succede se va via Maria?") oppure naviga visivamente il grafo.

**Step B2 — Parsing e grounding.** L'AI locale traduce la richiesta in componenti del grafo: entità (ruoli, sistemi, processi, persone), tipo di query (percorso, vicinato, conformance, impatto, bottleneck), filtri e condizioni.

**Step B3 — Esecuzione ibrida sul grafo.** Il motore combina (a) **query relazionale/strutturale** sul grafo MySQL (percorsi, dipendenze, RACI, prossimità, deviazioni materializzate) e (b) **ricerca semantica** su Qdrant (descrizioni di SOP, annotazioni, regole affini). Routing per tipo di query, secondo le best practice GraphRAG ibride *workflow-aware*.

**Step B4 — Sintesi e spiegazione (explainable).** L'AI compone la risposta **citando i nodi/percorsi del grafo** usati: "L'approvazione sopra 5.000 € è svolta dal ruolo *Approver L2* nel sistema *ERP-Acquisti* (passo 'Autorizzazione ordine'); sopra 20.000 € interviene anche il *CFO* come *Accountable*. Fonte: SOP-Acquisti v4 + 1.240 casi reali di esecuzione." Citazione delle fonti come da requisito di progetto.

**Step B5 — Esplorazione del grafo.** L'utente naviga visivamente: dal processo ai passi, dal passo ai ruoli/sistemi, espande verso le dipendenze e le deviazioni, filtra per tipo di nodo/relazione, evidenzia bottleneck e bus factor con codifiche cromatiche sul peso. Navigazione esplorativa progressiva.

**Step B6 — Simulazione "what-if" e analisi d'impatto.** L'utente chiede l'effetto di un cambiamento: rimozione di un passo, cambio di approvatore, dismissione di un sistema, indisponibilità di una persona. L'AI percorre il grafo e restituisce l'**impatto** (processi/passi/ruoli toccati) e i rischi. È il valore per process owner ed enterprise architect.

**Step B7 — Governance e alerting.** Deviazioni di conformance, violazioni di SoD, bottleneck oltre soglia e bus factor critici generano **alert** (riuso \`automation\`/\`messaging\`). Si producono **report di audit** con evidenze tracciabili (per ISO/GDPR) e cruscotti di salute dei processi.

**Step B8 — Azione e onboarding.** Il grafo alimenta esperienze d'uso: **guida onboarding** generata per ruolo ("ecco come fai X, chi coinvolgi, quale sistema"), **runbook** per le eccezioni, **export** di mappa/dossier di processo, suggerimenti di automazione verso il modulo \`automation\`.

**Step B9 — Feedback loop.** Le correzioni umane (confermare/smentire una deviazione, validare un'estrazione AI, segnalare un'annotazione utile/inutile) **ri-pesano il grafo** e migliorano estrazioni e suggerimenti — alimentando i pesi delle relazioni, requisito core del motore.

### 4.3 Diagramma sintetico del flusso

\`\`\`
[Connettori + import]
   A1 def. processo (BPMN/SOP/editor) → A2 contesto (ruolo/sistema/dato/regola, RACI) → A3 embedding(Qdrant)
                                            ↓
   A4 event log → A5 discovery (as-is, frequenze) → A6 conformance (to-be vs as-is) → A7 bottleneck/indici
                                            ↓
   A8 analisi org. (handoff, bus factor, SoD) → A9 tribal knowledge → A10 link non evidenti (AI)
                                            ↓
                        GRAFO PESATO PROCESSO–RUOLO–SISTEMA (MySQL + Qdrant)
                                            ↓
[Utente] B1 NL/navigazione → B2 parsing → B3 query ibrida → B4 risposta spiegata+citata
                                            ↓
   B5 esplorazione ⇄ B6 what-if/impatto → B7 governance/alert/audit → B8 onboarding/azione → B9 feedback (ri-pesa)
\`\`\`

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa lo schema generico nodi/archi del motore (tabelle MySQL per la struttura, Qdrant per i vettori). Sotto, la specializzazione per i processi. Tutti i tipi di nodo e relazione sono **enum bilingui IT/EN** veicolate al frontend secondo lo switch lingua.

### 5.1 Tipi di nodo

| Tipo nodo | Descrizione | Attributi principali | Vettore Qdrant |
|-----------|-------------|----------------------|----------------|
| \`Processo\` | Processo aziendale end-to-end | nome, dominio, owner, versione, lead time, livello automazione | sì (descrizione) |
| \`Passo\`/\`Attività\` | Singola attività del processo | descrizione, durata media, attesa media, frequenza, criticità | sì (descrizione) |
| \`Evento\` | Start / intermedio / end / timer / errore | tipo, trigger | no |
| \`Gateway\`/\`Decisione\` | Punto di diramazione | condizione, tipo (XOR/AND/OR) | opzionale |
| \`Ruolo\`/\`Funzione\` | Ruolo organizzativo | nome, livello, responsabilità | opzionale |
| \`Persona\` | Individuo che ricopre ruoli | nome (dato personale → privacy), unità | no (privato) |
| \`Sistema\`/\`Applicativo\` | Strumento dove avviene il passo | nome, tipo, criticità, owner IT | opzionale |
| \`Dato\`/\`Documento\` | Input/output di un passo | tipo, formato, sensibilità | sì (se documento) |
| \`Regola\`/\`Policy\`/\`SOP\` | Vincolo normativo o procedura | testo, fonte, versione, ambito compliance | sì |
| \`Automazione\` | Script/RPA/integrazione | tipo, stato, copertura | opzionale |
| \`Deviazione\` | Scarto as-is vs to-be rilevato | tipo (skip/extra/order), frequenza, gravità | opzionale |
| \`Eccezione\` | Caso fuori standard / workaround | descrizione, gestione | sì |
| \`Annotazione\` | Contributo tribal su un passo | testo, autore, affidabilità | sì |
| \`IstanzaProcesso\`/\`Caso\` | Singola esecuzione (case) | Case ID, esito, durata, attributi | opzionale |
| \`KPI\`/\`Metrica\` | Indicatore di processo | nome, valore, target, trend | no |

### 5.2 Tipi di relazione (archi)

| Relazione | Da → A | Pesata? | Significato |
|-----------|--------|---------|-------------|
| \`PRECEDE\` | Passo → Passo | **sì** | sequenza; peso = frequenza reale della transizione (da log) |
| \`INNESCA\` | Evento/Passo → Passo | **sì** | trigger di avvio |
| \`CONDIZIONATO_DA\` | Passo → Gateway/Regola | no | dipendenza da una condizione/decisione |
| \`RESPONSABILE\` (R) | Ruolo → Passo | **sì** | esegue il passo (RACI: Responsible) |
| \`APPROVA\` (A) | Ruolo → Passo | **sì** | è accountable / autorizza (RACI: Accountable) |
| \`CONSULTATO\` (C) | Ruolo → Passo | **sì** | viene consultato (RACI: Consulted) |
| \`INFORMATO\` (I) | Ruolo → Passo | no | viene informato (RACI: Informed) |
| \`RICOPRE\` | Persona → Ruolo | **sì** | chi ricopre quel ruolo oggi; peso = grado/copertura |
| \`ESEGUITO_IN\` | Passo → Sistema | **sì** | sistema in cui avviene il passo |
| \`PRODUCE\` / \`RICHIEDE\` | Passo → Dato/Documento | **sì** | output / input informativo |
| \`GOVERNATO_DA\` | Passo/Processo → Regola/Policy | **sì** | vincolo normativo applicabile |
| \`AUTOMATIZZATO_DA\` | Passo → Automazione | **sì** | copertura di automazione del passo |
| \`DEVIA_DA\` | IstanzaProcesso/Passo → Passo(to-be) | **sì** | deviazione di conformance; peso = frequenza/gravità |
| \`PASSA_A\` (handoff) | Ruolo/Persona → Ruolo/Persona | **sì** | passaggio di lavoro (rete sociale, da log) |
| \`DIPENDE_DA\` | Processo → Processo/Sistema | **sì** | dipendenza inter-processo o da sistema |
| \`SIMILE_A\` | Processo/Passo → Processo/Passo | **sì** | similarità semantica/strutturale (duplicazioni) |
| \`ANNOTA\` | Annotazione/Eccezione → Passo | **sì** | conoscenza esperienziale pesata |
| \`MISURA\` | KPI → Processo/Passo | no | indicatore collegato |

### 5.3 Criteri di peso degli archi

Il peso è il cuore del motore: trasforma una mappa statica in una rete che riflette *come l'azienda lavora davvero*. Per i processi i criteri principali:

- **Frequenza reale di esecuzione (process mining).** Per \`PRECEDE\`/\`INNESCA\`/\`PASSA_A\`: peso = quante volte la transizione/handoff avviene davvero nelle tracce. Distingue il percorso principale (peso alto) dai rami eccezionali (peso basso). È il segnale che ribalta il diagramma teorico.
- **Tempo e criticità.** Per \`PRECEDE\`/\`ESEGUITO_IN\`: archi/passi dove il tempo si accumula (alta attesa/servizio) vengono pesati come **bottleneck**; la criticità per il business amplifica il peso.
- **Forza della responsabilità (RACI).** \`APPROVA\` (Accountable) pesa più di \`RESPONSABILE\`, che pesa più di \`CONSULTATO\`/\`INFORMATO\`; il peso riflette il grado di coinvolgimento e l'autorità decisionale.
- **Copertura di ruolo / bus factor.** Per \`RICOPRE\`: se un solo individuo ricopre un ruolo critico, l'arco segnala **alto rischio** (basso bus factor); più persone abbassano il rischio.
- **Livello di automazione.** Per \`AUTOMATIZZATO_DA\`: peso = quota del passo coperta da automazione (utile per prioritizzare manuali ad alta frequenza).
- **Gravità e frequenza della deviazione.** Per \`DEVIA_DA\`: peso = quanto spesso e quanto gravemente l'esecuzione si scosta dalla norma (segnale per audit e miglioramento).
- **Affidabilità/consenso esperienziale.** Per \`ANNOTA\`: peso = reputazione del contributor, consenso tra più annotazioni, recency (ranking emergente della conoscenza tribale).
- **Forza della dipendenza.** Per \`DIPENDE_DA\`/\`ESEGUITO_IN\`: quanto un processo è vincolato a un sistema/altro processo (impatto in caso di guasto/dismissione).
- **Similarità.** Per \`SIMILE_A\`: combinazione di distanza coseno (Qdrant) su descrizioni e match strutturale (stessi ruoli/sistemi/sequenza) — per scovare processi duplicati tra reparti.
- **Freshness.** Tutti i pesi decadono con l'obsolescenza della fonte: log vecchi pesano meno dei recenti; una SOP non aggiornata da più versioni perde peso a favore dell'evidenza fattuale.

I pesi sono **materializzati in batch** in MySQL per le componenti costose e fattuali (frequenze, tempi, bottleneck, bus factor) e **ricalcolati a runtime** per le componenti dipendenti dal contesto della query (rilevanza per il ruolo che chiede, what-if). La materializzazione è la strategia che consente di restare su MySQL+Qdrant senza un datastore a grafo dedicato.

---

## 6. Fonti dati & connettori (ingestione)

Tutti i connettori sono implementati come **extension point PF4J** (nuovo \`DataSourceConnectorExtension\`, accanto a quelli esistenti) e orchestrati dal modulo \`batch\`. Ogni connettore è installabile/disinstallabile dal marketplace e configurabile per il self-hosting. Il principio guida è **local-first**: i connettori parlano con i sistemi *dentro* il perimetro aziendale; nessun dato di processo lascia l'istanza.

| Fonte | Tipo | Cadenza | Note | Output nel grafo |
|-------|------|---------|------|------------------|
| **File BPMN 2.0 / XML** | import | on-change | standard OMG | \`Processo\`/\`Passo\`/\`Gateway\`/\`Evento\` + sequenza |
| **Documenti SOP / policy / manuali qualità** | upload (PDF/DOCX) | on-change | riuso pipeline \`document\` (Tika/OCR) | estrazione AI passi/ruoli + embedding |
| **ERP / gestionale** (event log) | DB/API/CSV export | giornaliera/oraria | sorgente as-is più ricca | tracce → \`PRECEDE\` pesati, tempi |
| **CRM** (event log) | API/export | giornaliera | processi vendita | tracce, attributi di caso |
| **Ticketing / ITSM** (Jira, ServiceNow, GLPI, Zammad) | API/export | oraria | processi IT/supporto | tracce, handoff |
| **Workflow / BPM engine** | log nativo | continua | as-is "nativo" | tracce ad alta fedeltà |
| **Mail & calendario** | IMAP / API | continua | riuso moduli \`email\`/\`calendar\` | approvazioni/handoff via mail |
| **Automazioni / RPA / cron** | log | continua | riuso dominio \`automation\` | \`Automazione\` + \`AUTOMATIZZATO_DA\` |
| **HR / AD / LDAP** | directory/API | mensile | dato personale → privacy/consenso | \`Persona\`, \`Ruolo\`, \`RICOPRE\`, organigramma |
| **CMDB / catalogo IT** | API/CSV | on-change | mappa applicativi | \`Sistema\` + \`ESEGUITO_IN\`/\`DIPENDE_DA\` |
| **Matrice RACI** (foglio/CSV) | import | on-change | spesso esistente in qualità | archi RACI pesati |
| **Contributi tribal** | UI interna | continua | conoscenza esperienziale | \`Annotazione\`/\`Eccezione\`/\`Regola\` |

**Note di responsabilità (importanti):**
- **Privacy by design.** Gli event log e l'anagrafica HR sono dati altamente sensibili (rivelano persone, performance, organizzazione). Restano on-prem; l'elaborazione AI è Ollama locale per default; eventuale uso cloud solo con consenso esplicito e configurabile per processo. Pseudonimizzazione opzionale della colonna *resource* per analisi aggregate.
- **Normalizzazione al boundary.** Ogni connettore valida e normalizza prima di scrivere nel grafo: mapping nomi-attività eterogenei → attività canoniche, normalizzazione timestamp/fuso, dedup eventi, dedup persone/ruoli, coerenza Case ID.
- **Estensibilità.** Nuove sorgenti (un ERP verticale, un gestionale di settore) si aggiungono come plugin PF4J senza toccare il core e si pubblicano sul marketplace.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

### 7.1 MVP (prima release dell'ambito)

| # | Funzionalità | Tipo | Moduli LocalMind coinvolti |
|---|--------------|------|----------------------------|
| 1 | Schema grafo processi (nodi/relazioni §5) come modulo di dominio | CREARE | \`knowledge\`/grafo core, Flyway |
| 2 | Nuovo dominio \`process\` in \`localmind-domain\` (model/port/service, zero Spring) | CREARE | \`localmind-domain\`, \`DomainConfig\` |
| 3 | Import BPMN 2.0 → nodi/archi (BPMN2KG-like) | CREARE | infrastructure adapter, \`batch\` |
| 4 | Estrazione AI di passi/ruoli/sistemi da SOP/policy | CREARE/RIUSO | \`document\` (Tika/OCR), \`llm\` (Ollama), Qdrant |
| 5 | Editor/CRUD processo, passi, ruoli, sistemi, RACI | CREARE | dominio \`process\`, frontend |
| 6 | Connettore event log generico (CSV/DB) + normalizzazione | CREARE | \`batch\`, plugin PF4J |
| 7 | Process discovery (as-is): frequenze archi \`PRECEDE\`, tempi | CREARE | dominio \`process\`, \`batch\` |
| 8 | Conformance checking base (deviazioni to-be vs as-is) | CREARE | dominio \`process\` |
| 9 | Analisi bottleneck e lead time per passo | CREARE | dominio \`process\` |
| 10 | Bus factor e mappa step–ruolo–sistema | CREARE | dominio \`process\` |
| 11 | Ricerca/Q&A conversazionale ibrida con citazioni (GraphRAG) | CREARE/RIUSO | grafo core, Qdrant, \`llm\` |
| 12 | UI feature Angular \`process\`: editor, esplorazione, risposte spiegate | CREARE | frontend feature lazy |
| 13 | Visualizzazione grafo processo–ruolo–sistema (base) | CREARE | frontend |
| 14 | Contributi tribal (annotazioni su passi) | CREARE | dominio \`process\`, Qdrant |
| 15 | Enum bilingui IT/EN (tipi nodo/relazione, RACI, stati) | CREARE | backend + frontend i18n |
| 16 | Migrazioni Flyway (una query per file) | CREARE | \`localmind-app\` |

### 7.2 Evoluzione (release successive)

| # | Funzionalità | Valore |
|---|--------------|--------|
| E1 | Connettori nativi (ERP/CRM/ticketing/Jira/ServiceNow) | copertura as-is ricca, minor sforzo |
| E2 | Conformance avanzata + alert su deviazioni oltre soglia | governance proattiva, audit |
| E3 | Separazione dei compiti (SoD) e controlli di compliance automatici | rischio, ISO/GDPR/SOX |
| E4 | Rete sociale di handoff e analisi del carico per ruolo/persona | ottimizzazione organizzativa |
| E5 | Simulazione "what-if" e analisi d'impatto (rimozione passo/sistema/persona) | decisioni di change sicure |
| E6 | Suggerimento automazioni (passi candidati) → handoff a \`automation\` | efficienza, riduzione lavoro manuale |
| E7 | Rilevamento processi duplicati tra reparti (\`SIMILE_A\`) | razionalizzazione, risparmio |
| E8 | Generazione automatica di guide onboarding e runbook per ruolo | time-to-productivity |
| E9 | Cruscotti di salute processi + KPI/trend nel tempo | continuous improvement misurato |
| E10 | Agente AI di processo (orchestra discovery, audit, report in autonomia) | riuso dominio \`agent\` |
| E11 | Versionamento e diff dei processi nel tempo (evoluzione del to-be) | tracciabilità del cambiamento |
| E12 | Export audit-ready (evidenze tracciabili per certificazioni) | compliance |
| E13 | Conformance predittiva / anticipo bottleneck | da reattivo a predittivo |

### 7.3 Da mantenere (manutenzione continua)

- **Refresh dei log e ricalcolo pesi:** job batch monitorati con metriche (Actuator/Prometheus già presenti); finestra notturna per i ricalcoli pesanti.
- **Mapping nomi-attività:** presidio costante sul mapping log↔attività canoniche quando i sistemi cambiano etichette (è il punto più fragile dell'ingestione as-is).
- **Allineamento tassonomie:** ruoli, sistemi e tipi di documento evolvono; il catalogo va mantenuto.
- **Tuning di pesi, decadimenti e soglie:** revisione periodica guidata dal feedback e dai KPI.
- **Qualità del tribal knowledge:** moderazione dei contributi, gestione reputazione, anti-obsolescenza.
- **Privacy e accessi:** revisione periodica dei livelli di sensibilità e dei diritti d'accesso ai processi riservati.
- **Aggiornamento traduzioni IT/EN** delle enum e dei testi UI.

---

## 8. Casi d'uso AI / GraphRAG

L'AI (Ollama locale di default; cloud opzionale con consenso) opera **sul grafo**, combinando navigazione relazionale e semantica, e cita sempre i nodi/percorsi usati.

1. **Q&A operativa per l'onboarding.** "Come faccio un ordine di acquisto, chi lo approva e in quale sistema?" → l'AI percorre \`Processo→Passo→ESEGUITO_IN/APPROVA\` e risponde con passi, ruoli, sistemi e documenti, citando SOP e casi reali.

2. **Scoperta dei bottleneck reali.** "Dove si blocca davvero il processo di onboarding clienti?" → analisi dei tempi sugli archi \`PRECEDE\`, evidenziazione del passo collo di bottiglia con il peso e la causa (annotazioni tribal collegate).

3. **Conformance e audit.** "In quali casi l'approvazione del CFO è stata saltata negli ultimi 6 mesi?" → query sulle \`Deviazione\`/\`DEVIA_DA\` con evidenze per istanza, pronta per l'auditor.

4. **Analisi d'impatto / what-if.** "Se dismettiamo l'ERP legacy, quali processi e passi vengono colpiti?" → navigazione di \`ESEGUITO_IN\`/\`DIPENDE_DA\` e lista d'impatto con criticità.

5. **Bus factor e continuità.** "Se va via Maria, quali processi restano senza approvatore?" → da \`Persona→RICOPRE→Ruolo→APPROVA→Passo\`, rischio di continuità con suggerimento di backup.

6. **Separazione dei compiti (SoD).** "Esistono casi in cui la stessa persona ha creato il fornitore e approvato il pagamento?" → controllo su archi e istanze, segnalazione violazioni.

7. **Razionalizzazione.** "Quali processi sono sostanzialmente duplicati tra le filiali?" → archi \`SIMILE_A\` pesati per similarità semantica+strutturale.

8. **Candidati ad automazione.** "Quali passi conviene automatizzare?" → passi ad alta frequenza, manuali e ben definiti, con stima del beneficio; handoff verso il modulo \`automation\`.

9. **Spiegazione delle eccezioni.** "Perché in questo caso il processo è andato diversamente?" → sintesi GraphRAG che incrocia deviazioni fattuali e annotazioni tribal ("regola implicita: cliente PA → passo extra").

10. **Generazione di runbook/guide.** "Genera la guida operativa per il ruolo Buyer." → l'AI compila procedura, sistemi, contatti ed eccezioni dal grafo, bilingue IT/EN.

---

## 9. KPI & metriche di successo

| Categoria | KPI | Target indicativo |
|-----------|-----|-------------------|
| Qualità dato | % passi con ruolo + sistema assegnati (no orfani) | > 95% |
| Qualità dato | % eventi log mappati ad attività canoniche | > 97% |
| Copertura | % processi critici con as-is (event log) ingerito | crescente |
| Performance | latenza Q&A ibrida (p95) | < 2 s su dataset aziendale tipico |
| Performance | tempo ricalcolo batch frequenze/bottleneck | entro finestra notturna |
| Efficacia AI | % risposte con citazione nodi corretta | > 90% |
| Efficacia AI | accuratezza estrazione AI passi/ruoli da SOP (validata) | misurata su set di valutazione |
| Valore business | riduzione lead time sui processi ottimizzati | misurabile post-intervento |
| Valore business | riduzione tempo di onboarding nuovo assunto | misurabile |
| Rischio | n. processi con bus factor = 1 individuato e mitigato | decrescente |
| Compliance | n. violazioni SoD / deviazioni rilevate e risolte | tracciato |
| Knowledge | n. annotazioni tribal e copertura passi critici | crescente |
| Privacy/local | % elaborazioni eseguite in locale | 100% di default |

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---------|---------|-------------|
| **Qualità ed eterogeneità degli event log** | As-is inaffidabile | Layer di normalizzazione versionato, mapping nomi-attività, dedup, test di regressione sul mapping |
| **Privacy dei dati di processo/HR** | Dato sensibilissimo, rischio legale | Local-first, AI Ollama locale, nessun invio cloud senza consenso, pseudonimizzazione resource, cifratura a riposo, controlli d'accesso |
| **Estrazione AI da SOP imprecisa** | Grafo to-be errato | Sempre validazione umana sulle estrazioni; confidenza esplicita; feedback loop che migliora |
| **To-be obsoleto vs as-is** | Conformance fuorviante | Priorità all'evidenza fattuale (peso freshness); il grafo *evidenzia* lo scarto invece di nasconderlo |
| **Resistenza organizzativa / paura del controllo** | Bassa adozione, dati falsati | Framing su miglioramento e onboarding (non sorveglianza); analisi aggregate; trasparenza; pseudonimizzazione |
| **Costo computazionale discovery/conformance** | Batch lenti | Materializzazione pesi, calcolo incrementale sui delta, finestra notturna |
| **Limiti query complesse su MySQL** | Performance | Materializzazione, indici, query ibrida; rivalutare datastore a grafo solo se necessario (out of scope ora) |
| **Tribal knowledge errato/obsoleto** | Conoscenza distorta | Pesi per affidabilità/consenso/recency, moderazione, confronto con i fatti |
| **Connettori a sistemi proprietari** | Copertura limitata | Connettore generico CSV/DB come base; connettori nativi come plugin evolutivi |
| **Bias da "happy path"** | Si modella solo il percorso ideale | Il process mining sui log fa emergere i percorsi reali e le eccezioni |

---

## 11. Manutenzione & evoluzione

- **Cicli di refresh governati:** scheduler batch con allarmi su fallimenti; metriche Prometheus/Grafana già nello stack; dashboard di salute dei connettori e dei job di discovery/conformance.
- **Versionamento dello schema grafo:** ogni evoluzione di nodi/relazioni passa da migrazione Flyway (una query per file) e aggiornamento delle enum bilingui IT/EN.
- **Versionamento dei processi:** il to-be evolve nel tempo; conservare versioni e diff per tracciare il cambiamento e correlarlo agli effetti sull'as-is.
- **Estensibilità via plugin:** nuovi connettori (ERP/CRM verticali) come plugin PF4J senza toccare il core; pubblicabili sul marketplace.
- **Tuning continuo:** revisione trimestrale di pesi, funzioni di decadimento, soglie di conformance e regole SoD, guidata da feedback (\`B9\`) e KPI.
- **Curatela del tribal knowledge:** workflow di moderazione, gestione reputazione, anti-obsolescenza, archiviazione delle annotazioni superate.
- **Governance privacy/accessi:** revisione periodica dei livelli di sensibilità e dei diritti d'accesso, allineata al modello auth/multi-tenant.
- **Documentazione bilingue:** ogni funzionalità documentata IT/EN (documentation/ + documentazione/); ogni sviluppo tracciato nella cartella \`Sviluppi/\` con nomenclatura datata.
- **Roadmap evolutiva:** MVP (to-be + discovery + Q&A) → conformance/SoD → analisi organizzativa/what-if → automazione/razionalizzazione → agente di processo e predittività.

---

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo esistente | Ruolo nell'ambito Processi & workflow |
|------------------|----------------------------------------|
| **\`knowledge\` / grafo core** | Base del motore: schema generico nodi/archi specializzato per i processi. Punto di partenza per il grafo pesato. |
| **\`document\`** | Pipeline di estrazione testo (Tika) e OCR per ingerire SOP, manuali qualità, policy e procedure; base per l'estrazione AI dei passi. |
| **Qdrant (\`vectorstore\`)** | Embedding di descrizioni passi, SOP, annotazioni e regole per la ricerca semantica e il recupero ibrido del GraphRAG. |
| **\`llm\` + Ollama** | GraphRAG: parsing NL, estrazione passi/ruoli da documenti, scoring, spiegazioni, generazione runbook; fallback chain multi-provider opzionale. |
| **\`batch\`** | Orchestrazione dei job di ingestione event log, process discovery, conformance e ricalcolo pesi (frequenze, tempi, bottleneck). |
| **\`automation\`** | Doppio legame: sorgente (log delle automazioni → vista "automatizzato") e destinazione (suggerimenti di automazione dei passi). |
| **\`messaging\`** | Notifiche e alert su deviazioni, violazioni SoD, bottleneck e bus factor critici. |
| **\`calendar\` + \`email\`** | Sorgenti di event log "soft" (approvazioni, handoff, riunioni) per i processi che vivono in mail/calendario. |
| **\`marketplace\` + plugin PF4J** | Distribuzione del modulo processi e dei connettori (ERP/CRM/ticketing) come estensioni installabili. |
| **\`auth\`** | Protezione dei dati di processo sensibili, controlli d'accesso per processi riservati, multi-tenant local-first. |
| **\`agent\`** | Agente di processo AI che orchestra discovery, audit, report e suggerimenti in autonomia (multi-agent: ricerca, verifica, sintesi, governance). |
| **\`common\` (event/analytics)** | Eventi di dominio (processo ingerito, deviazione rilevata) e analytics di utilizzo. |
| **Frontend Angular (\`features/\`)** | Nuova feature lazy \`process\`: editor processo/RACI, esplorazione del grafo, Q&A spiegata, what-if, cruscotti; Signal store; \`TranslatePipe\` IT/EN; \`language-switcher\`. |
| **MySQL + Flyway** | Struttura del grafo (nodi/archi/pesi materializzati, tracce, deviazioni) e migrazioni versionate (una query per file). |

**Nuovo dominio da introdurre:** \`process\` in \`localmind-domain\` (model / port-in / port-out / service, zero Spring), wired in \`DomainConfig.java\`, con controller \`/api/v1/process/*\`, adapter di persistenza, connettori event log e import BPMN in infrastructure, job di discovery/conformance in \`batch\`, e feature Angular dedicata — seguendo esattamente il pattern "Where to Add New Code" della struttura del progetto. Da valutare la convivenza/sinergia con il dominio \`automation\` esistente: \`process\` *descrive e analizza* (conoscenza), \`automation\` *esegue* (orchestrazione); i due si alimentano a vicenda.

---

### Fonti consultate

- [Automated Process Knowledge Graph Construction from BPMN Models (Springer / DEXA 2022)](https://link.springer.com/chapter/10.1007/978-3-031-12423-5_3)
- [GRAG4PM: Graph Retrieval Augmented Generation Framework Adapted for Process Mining (Applied Sciences, 2026)](https://doi.org/10.3390/app16105152)
- [The Next Frontier of RAG: How Enterprise Knowledge Systems Will Evolve 2026–2030 (NStarX)](https://nstarxinc.com/blog/the-next-frontier-of-rag-how-enterprise-knowledge-systems-will-evolve-2026-2030/)
- [Enterprise Knowledge Graph: Architecture, Use Cases & Implementation Guide 2026 (Improvado)](https://improvado.io/blog/enterprise-knowledge-graph)
- [Procedure Model for Building Knowledge Graphs for Industry Applications (arXiv 2409.13425)](https://arxiv.org/html/2409.13425v1)
- [RACI Matrix: Your Ultimate Guide in 2026 (Project-Management.com)](https://project-management.com/understanding-responsibility-assignment-matrix-raci-matrix/)
- [Process Mining: Discovery, Conformance and Enhancement of Business Processes (W. van der Aalst)](https://www.researchgate.net/publication/275535045_Process_Mining_Discovery_Conformance_and_Enhancement_of_Business_Processes)
- [What is Process Mining? (IBM Think)](https://www.ibm.com/think/topics/process-mining)
- [Business process mining: Conformance checking and bottleneck identification (ResearchGate)](https://www.researchgate.net/publication/351141867_Business_process_mining_from_e-commerce_event_web_logs_Conformance_checking_and_bottleneck_identification)
</content>
</invoke>
`;
