export const content = `# Clienti & fornitori

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo documento descrive l'ambito di estensione **Clienti & fornitori** (gruppo: *enterprise*) del motore di Knowledge Graph universale di LocalMind. L'obiettivo è trasformare il rapporto commerciale dell'azienda — con chi compra (clienti) e con chi vende all'azienda (fornitori) — da una collezione di record sparsi in CRM, gestionale, email e fogli di calcolo a un **grafo pesato, unico e navigabile dall'AI** (GraphRAG): organizzazioni, persone, contratti, ordini, interazioni e dipendenze commerciali diventano nodi e archi pesati, su cui l'AI locale risponde a domande complesse ("quali clienti strategici sono gestiti da una sola persona che sta per andarsene?", "se questo fornitore salta, quali prodotti e quali clienti restano scoperti?") e fa emergere collegamenti non evidenti.

L'ambito riusa integralmente lo stack esistente (Spring Boot esagonale, Angular 21, MySQL 8.0 per la struttura del grafo, Qdrant per la semantica, Ollama come AI locale di default) ed è veicolato come **modulo di dominio installabile** tramite il sistema plugin PF4J + marketplace. È pensato per il vincolo più stringente dell'enterprise: i dati commerciali — pipeline, margini, listini, contatti, contratti — sono fra i più sensibili dell'azienda e **non devono mai lasciare il perimetro** senza consenso esplicito. Local-first, AI in locale di default e bilinguismo IT/EN ne sono i pilastri.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema reale

La conoscenza commerciale di un'azienda è ovunque tranne che in un posto solo. Esiste un CRM (Salesforce, HubSpot, Dynamics, Pipedrive…) per la pipeline di vendita; un gestionale/ERP per ordini, fatture e anagrafiche; una casella email e un calendario dove vivono migliaia di interazioni con clienti e fornitori; cartelle condivise piene di contratti PDF; e — quasi sempre — la parte più preziosa nella testa delle singole persone (chi è il vero decisore in quel cliente, perché quel fornitore è critico, quale promessa è stata fatta a voce in una call). Questa frammentazione genera dolori strutturali e costosi:

1. **Nessuna vista unica della relazione (né lato cliente né lato fornitore).** Il CRM conosce le opportunità, l'ERP conosce gli ordini, l'email conosce le conversazioni, ma nessuno li unisce. La stessa organizzazione compare come "Rossi S.p.A.", "Rossi SpA", "ROSSI S.P.A." e "Rossi spa - sede Milano" in quattro sistemi diversi. Senza **entity resolution** (riconciliazione delle identità) non esiste un *Customer 360* né un *Supplier 360*: si lavora su frammenti, si prendono decisioni su dati parziali e si fanno errori di consapevolezza enormi (offrire uno sconto a un cliente che è già in ritardo di pagamento; trattare come "nuovo" un fornitore con cui c'è già un contenzioso aperto in un'altra divisione).

2. **Le dipendenze commerciali sono invisibili finché non esplodono.** È la lezione più cara della supply chain moderna: la visibilità si ferma al Tier 1, mentre il rischio nasce nei sotto-livelli che nessuno ha mai mappato. *Quanto fatturato dipende da un singolo cliente?* *Quale prodotto vendibile dipende da un componente che arriva da un solo fornitore in una sola area geografica?* *Se quel fornitore salta, quali contratti verso i nostri clienti diventano inadempibili?* Queste catene — cliente → prodotto → componente → fornitore → area geografica — esistono nei dati ma non sono modellate come relazioni navigabili. La **concentrazione di rischio si accumula in silenzio** finché un evento la attiva. La letteratura 2026 stima che una singola interruzione possa costare fino al 42% dell'EBITDA annuo.

3. **La conoscenza relazionale è ostaggio delle persone (key-person risk).** Chi conosce davvero il decisore in un cliente strategico? Spesso una sola persona. Quando quella persona si ammala, va in ferie o si licenzia, la relazione si interrompe e il valore evapora. Lo stesso vale per i fornitori: il "rapporto storico" con un fornitore critico vive nella memoria di un buyer. Nessuno strumento oggi rende esplicito e interrogabile *chi presidia cosa* e *dove l'azienda è esposta a un singolo punto di rottura umano*.

4. **Le interazioni non diventano mai conoscenza.** Migliaia di email, call, riunioni, ticket di supporto: ogni interazione contiene segnali (un cliente insoddisfatto, una promessa, una richiesta ricorrente, un fornitore che inizia a ritardare), ma resta sepolta nelle caselle individuali. Non viene collegata all'entità, non pesa la salute della relazione, non innesca azioni. Il risultato è che i segnali deboli di *churn* (abbandono cliente) o di *deterioramento fornitore* vengono notati troppo tardi.

5. **Nessuna spiegabilità delle decisioni commerciali.** Un'AI che dicesse "questo cliente è a rischio" senza mostrare *perché* (quali interazioni, quali pagamenti in ritardo, quale contratto in scadenza) è inutile e pericolosa in un contesto enterprise. Allo stesso modo, le risposte basate solo su ricerca semantica (vector RAG) producono risposte fluenti ma spesso sbagliate, perché ignorano le relazioni esplicite. Il 2026 ha eletto **GraphRAG** (grafo + vettori) a standard per l'AI enterprise affidabile proprio perché àncora le risposte a entità e percorsi reali, citabili.

### 1.2 La nostra risposta: il grafo CRM/SRM unico

LocalMind modella clienti e fornitori come **un unico grafo pesato di relazioni commerciali**, non due silos separati. La stessa organizzazione può essere contemporaneamente cliente e fornitore (relazione *reciproca*, frequentissima nel B2B), e il motore lo rappresenta naturalmente.

- Ogni **organizzazione** (cliente, fornitore, prospect, partner) è un nodo, collegato alle sue **persone** (referenti, decisori, buyer), alle sue **sedi**, alle sue **gerarchie societarie** (gruppo → controllata → divisione).
- Ogni **contratto**, **opportunità**, **ordine** e **fattura** è un nodo collegato all'organizzazione e alle persone coinvolte, con date, valori e stato.
- Ogni **interazione** (email, call, riunione, ticket, nota) è un nodo collegato all'entità e alla persona, con un **sentiment** e un peso che alimentano la "salute della relazione".
- Le **dipendenze commerciali** sono archi espliciti: cliente *dipende da* prodotto, prodotto *richiede* componente, componente *fornito da* fornitore, fornitore *localizzato in* area, persona *presidia* cliente.
- Il tutto è **pesato**: peso del legame cliente↔azienda (fatturato, marginalità, anzianità), criticità del fornitore (insostituibilità, lead time, % di spesa), forza della relazione interpersonale (frequenza e qualità delle interazioni).

Su questo grafo l'AI locale (GraphRAG) **naviga, spiega e anticipa**: risponde in linguaggio naturale combinando vincoli relazionali (percorsi, vicinati, sottografi) e semantica (contenuto di email e contratti via Qdrant), citando i nodi e i percorsi usati. E — punto chiave del progetto — **fa emergere collegamenti non evidenti**: una concentrazione di rischio a tre salti di distanza, un cliente "orfano" di presidio, un fornitore che compare anche come cliente con un credito scaduto.

### 1.3 Il valore in tre leve

| Leva di valore | Cosa abilita | Esempio concreto |
|----------------|--------------|------------------|
| **Vista unica (360°)** | Customer 360 e Supplier 360 riconciliati da fonti multiple | "Mostrami tutto su Rossi S.p.A.": opportunità aperte, ordini, fatture scadute, ultime 10 interazioni, contratti in scadenza, persone di contatto e chi le presidia — in un solo schermo, citabile |
| **Visibilità delle dipendenze** | Mappa rischio concentrazione clienti e fornitori, single point of failure | "Se ACME Components fallisce, quali prodotti, contratti e clienti restano scoperti, per quale fatturato?" risposto attraversando il grafo a più livelli |
| **Anticipo dei segnali** | Salute della relazione, churn cliente e deterioramento fornitore | Allerta proattiva: "3 clienti strategici mostrano calo di interazioni + ritardi di pagamento + contratto in scadenza nei prossimi 60 giorni" |

### 1.4 Perché LocalMind e non il solo CRM/ERP

- **Local-first e privacy assoluta.** Pipeline di vendita, margini, listini, contratti e rubrica clienti/fornitori sono fra i dati più sensibili dell'azienda. In LocalMind restano *on-premise*; l'AI che li elabora è Ollama in locale per default. Nessun dato commerciale parte verso il cloud senza consenso esplicito e configurabile. Questo è il requisito che blocca l'adozione di molte AI commerciali "as-a-service".
- **Unificazione, non sostituzione.** LocalMind non rimpiazza il CRM o l'ERP: li **ingerisce e li collega**. Diventa lo strato di intelligenza relazionale *sopra* i sistemi esistenti, colmando il vuoto che nessuno di essi riempie (la rete di relazioni e dipendenze trasversali).
- **Spiegabilità e fiducia.** Ogni risposta dell'AI cita i nodi e i percorsi: una decisione commerciale supportata da prove tracciabili, non da una "scatola nera".
- **Universalità del motore.** Lo stesso engine del turismo e degli altri ambiti enterprise serve clienti & fornitori: cambiano i tipi di nodo/relazione, non l'infrastruttura. Costo di sviluppo e manutenzione drasticamente ridotto.

### 1.5 Cosa NON è (confini di valore)

Non è un CRM completo (non gestisce campagne marketing, automazioni di vendita, configuratori d'offerta) né un ERP/gestionale (non emette fatture, non tiene la contabilità, non gestisce il magazzino). Non è una piattaforma transazionale. È uno **strato di knowledge graph e intelligenza decisionale** che si nutre di quei sistemi e restituisce vista unica, mappa delle dipendenze e segnali anticipati. La fonte di verità transazionale resta CRM/ERP; LocalMind è la fonte di verità *relazionale*.

---

## 2. Personas & utenti target

| Persona | Profilo | Obiettivo principale | Bisogni dal grafo |
|---------|---------|----------------------|-------------------|
| **Direttore commerciale / Sales manager** | Guida il team vendite, ragiona su pipeline e clienti chiave | Capire dove sta il valore e il rischio nel portafoglio clienti | Concentrazione fatturato, clienti a rischio churn, presidio dei key account, scostamenti su contratti in scadenza |
| **Account / Sales rep** | Gestisce un set di clienti | Arrivare preparato a ogni interazione | Customer 360 istantaneo, storico interazioni, prossime azioni, mappa dei decisori |
| **Responsabile acquisti / Procurement** | Gestisce i fornitori e la spesa | Ridurre il rischio di fornitura e ottimizzare la spesa | Supplier 360, criticità e single point of failure, alternative possibili, scadenze contrattuali |
| **Buyer / Category manager** | Presidia categorie di acquisto | Salute dei fornitori per categoria | Concentrazione spesa, performance/ritardi, dipendenze prodotto↔componente↔fornitore |
| **Risk / Compliance manager** | Sorveglia rischi ed esposizioni | Mappare e mitigare le concentrazioni | Single point of failure clienti e fornitori, esposizione geografica, conflitti (stessa entità cliente e fornitore con contenzioso) |
| **CFO / Controllo di gestione** | Visione economico-finanziaria | Esposizione e marginalità della relazione | Crediti scaduti per cliente, % spesa per fornitore, valore contrattualizzato, rischio di concentrazione |
| **Customer Success / Supporto** | Gestisce la relazione post-vendita | Prevenire l'abbandono | Sentiment delle interazioni, ticket, salute della relazione, segnali deboli |
| **Self-hoster / Data engineer** | Installa LocalMind on-prem | Pipeline dati commerciali privata e governata | Connettori (CRM/ERP/email), API grafo, controllo totale dei dati, audit |

**Anti-persona:** la micro-attività con dieci clienti e tre fornitori gestiti a memoria — il valore di LocalMind cresce con il numero di entità, di fonti dati e con la complessità delle dipendenze; sotto una certa soglia un foglio di calcolo basta.

---

## 3. Requisiti in input

Questa sezione è deliberatamente dettagliata: definisce *tutto ciò che entra* nel grafo, *da dove* e *con quali regole di qualità e validazione*. Gli input si dividono in: entità anagrafiche (chi), input transazionali (cosa succede economicamente), input relazionali/interazioni (cosa accade nel rapporto), input di dipendenza (la catena prodotto/componente), e input di configurazione (come pesare e riconciliare). Tutto va validato al boundary (sistema sorgente sospetto = dato non fidato).

### 3.1 Entità anagrafiche — organizzazioni

L'organizzazione è il nodo cardine. Può essere cliente, fornitore, prospect, partner o **più ruoli insieme**.

| Campo | Origine tipica | Obbligatorio | Note di validazione |
|-------|----------------|--------------|---------------------|
| Ragione sociale | CRM / ERP | sì | usata per entity resolution (normalizzazione) |
| Identificativo fiscale (P.IVA / VAT / Cod. fiscale / DUNS) | ERP / registri | fortemente consigliato | **chiave forte** per la riconciliazione; validare formato |
| Ruolo/i commerciale/i | derivato | sì | enum IT/EN: Cliente, Fornitore, Prospect, Partner, Cliente+Fornitore |
| Sede legale + sedi operative (indirizzo, paese, geo) | CRM / ERP | sede legale sì | il paese alimenta l'esposizione geografica |
| Gerarchia societaria (gruppo / controllante / divisione) | ERP / inserimento | no | abilita il roll-up del rischio a livello gruppo |
| Settore / categoria merceologica | CRM | no | enum/tassonomia IT/EN |
| Dimensione (fatturato, dipendenti) | CRM / arricchimento | no | per segmentazione |
| Stato relazione (attivo, sospeso, cessato, in contenzioso) | derivato/manuale | sì | enum IT/EN |

**Regola di qualità:** un'organizzazione senza alcuna chiave forte (P.IVA/VAT/DUNS) entra nel grafo ma viene marcata come *candidata a duplicato* e sottoposta a entity resolution con soglia di confidenza più alta prima di fondersi con un nodo esistente.

### 3.2 Entità anagrafiche — persone (referenti)

Le persone sono il tessuto della relazione e la chiave del *key-person risk* (sia lato cliente, sia lato team interno).

- **Referenti esterni:** nome, ruolo/funzione, email, telefono, organizzazione di appartenenza, **ruolo decisionale** (decisore, influenzatore, utente, gatekeeper), sede.
- **Persone interne (owner):** chi nell'azienda *presidia* una relazione (account owner, buyer responsabile), con il proprio ruolo e team.
- **Validazione privacy (GDPR):** i dati personali sono soggetti a minimizzazione, base giuridica e diritto all'oblio; il modulo deve consentire cancellazione/anonimizzazione del nodo persona senza distruggere la storia aggregata della relazione.

### 3.3 Input transazionali — contratti, opportunità, ordini, fatture

Provengono in larga parte da CRM (lato vendita) ed ERP (lato esecuzione/amministrazione).

| Oggetto | Campi chiave | Origine | Note |
|---------|--------------|---------|------|
| **Opportunità / Trattativa** | valore, fase, probabilità, data prevista chiusura | CRM | lato cliente; alimenta pipeline e previsioni |
| **Contratto** | controparte, oggetto, valore, decorrenza, **scadenza/rinnovo**, SLA/penali | CRM/ERP/PDF | sia cliente (vendita) sia fornitore (acquisto); la scadenza è critica per gli alert |
| **Ordine** | controparte, prodotti/righe, importo, data, stato | ERP | collega organizzazione ↔ prodotti |
| **Fattura / Pagamento** | importo, emissione, scadenza, **stato pagamento**, ritardo gg | ERP | i ritardi alimentano la salute relazione e l'esposizione |
| **Listino / Condizioni** | prezzi, sconti, termini di pagamento | ERP/CRM | sensibile; resta on-prem |

**Regola di qualità:** ogni oggetto transazionale deve agganciarsi a un'organizzazione *risolta*; un ordine/fattura "orfano" (controparte non riconciliabile) finisce in una coda di *data quality* per intervento umano, non viene scartato silenziosamente.

### 3.4 Input relazionali — interazioni

Sono il flusso che mantiene "vivo" il grafo e che alimenta i pesi di salute della relazione. Provengono dai moduli LocalMind esistenti **email** e **calendar**, oltre che da CRM e ticketing.

- **Email:** mittente/destinatari, oggetto, corpo (→ embedding Qdrant + estrazione entità), thread, direzione (in/out), data.
- **Riunioni/Call:** partecipanti, data, durata, eventuale trascrizione (via Whisper, già presente), note.
- **Ticket di supporto / reclami:** controparte, categoria, priorità, stato, esito.
- **Note manuali:** appunti dell'account/buyer ("il decisore cambia a settembre").
- **Sentiment derivato:** ciascuna interazione riceve un sentiment (positivo/neutro/negativo) calcolato dall'AI locale, che pesa la salute della relazione.

**Regola di privacy/sensibilità:** le interazioni sono il dato più sensibile. L'estrazione di entità e sentiment avviene **in locale** (Ollama) per default; il collegamento email↔organizzazione richiede consenso e rispetta i confini di visibilità per ruolo.

### 3.5 Input di dipendenza — la catena commerciale

È il differenziatore del modulo: ciò che permette l'analisi di rischio. Spesso questi dati non esistono in un singolo sistema e vanno **costruiti collegando** ERP, distinte base (BOM) e conoscenza umana.

- **Prodotto/servizio venduto** → richiede → **componente/materia prima/servizio** → fornito da → **fornitore** → localizzato in → **area geografica**.
- **Cliente** → acquista → **prodotto** (dagli ordini), chiudendo la catena cliente↔fornitore.
- **Contratto cliente** → garantisce → **prodotto/SLA** che a sua volta dipende da fornitori: così un rischio fornitore si propaga fino al contratto cliente esposto.

**Profondità multi-livello (multi-tier):** dove possibile, modellare il fornitore del fornitore (sub-tier), perché la concentrazione di rischio nasce proprio sotto il Tier 1. Anche una mappatura parziale dei sotto-livelli ha valore enorme.

### 3.6 Input di configurazione — pesi, soglie, riconciliazione

Definisce *come* il motore interpreta i dati. Tutto configurabile, con default ragionevoli e bilingue IT/EN.

- **Pesi del legame cliente:** quanto contano fatturato, marginalità, anzianità, frequenza di interazione, recency.
- **Pesi di criticità fornitore:** quanto contano % di spesa, insostituibilità (n. di alternative), lead time, performance/ritardi, esposizione geografica.
- **Soglie di rischio:** es. "cliente strategico = top 10% fatturato"; "single point of failure = unico fornitore di un componente in un solo paese"; "concentrazione critica = >X% fatturato su un cliente".
- **Regole di entity resolution:** chiavi forti (P.IVA/VAT/DUNS) e deboli (nome normalizzato, dominio email, indirizzo), soglie di auto-merge vs revisione umana.
- **Politiche di privacy/visibilità:** quali ruoli vedono margini, listini, contatti; cosa può essere inviato a un eventuale provider cloud (default: nulla).

### 3.7 Riepilogo qualità del dato (cardini)

| Cardine | Perché è critico | Conseguenza se mancante |
|---------|------------------|--------------------------|
| Entity resolution (identità) | Senza, niente vista unica e il grafo dà risposte sbagliate ma sicure | Duplicati, 360° impossibile |
| Aggancio transazioni↔entità risolta | Lega valore economico alla relazione | Esposizione e rischio non calcolabili |
| Catena di dipendenza | Abilita l'analisi di single point of failure | Rischio concentrazione invisibile |
| Owner interno (presidio) | Abilita il key-person risk | Clienti "orfani" non rilevabili |

---

## 4. Flusso dell'attività (step-by-step)

Il flusso descrive il ciclo di vita completo: dalla connessione delle fonti alla costruzione del grafo, fino all'uso quotidiano e agli alert proattivi. È pensato per essere **incrementale**: ogni step porta valore anche da solo.

### 4.1 Fase 0 — Installazione e configurazione del modulo

1. L'amministratore installa il modulo **Clienti & fornitori** dal *marketplace* (plugin PF4J).
2. Sceglie la lingua (IT/EN) e conferma le impostazioni di privacy: **AI locale (Ollama) di default**, nessun invio al cloud senza consenso esplicito per fonte/campo.
3. Configura i pesi e le soglie di base (sezione 3.6) oppure accetta i default ragionevoli.
4. Il modulo crea i propri tipi di nodo/relazione nello schema del grafo (estensione modulare) e le migrazioni Flyway necessarie (una query per file).

### 4.2 Fase 1 — Connessione delle fonti e ingestione

1. L'utente collega le fonti tramite i **connettori** (sezione 6): CRM, ERP/gestionale, caselle email/calendario (moduli esistenti), cartelle di contratti, eventuali CSV/Excel.
2. Per ciascuna fonte sceglie **scope e frequenza** (es. solo organizzazioni attive; sync notturna incrementale).
3. L'ingestione gira come **job batch** (Spring Batch, riuso dell'infrastruttura folder/document): estrae anagrafiche, transazioni, interazioni; i PDF dei contratti passano per Tika/OCR per estrarre testo e clausole chiave (scadenza, valore, penali).
4. I contenuti testuali (email, contratti, note) vengono **chunked ed embeddati su Qdrant** per la ricerca semantica; i metadati strutturati finiscono su MySQL come nodi/archi.
5. Ogni record sorgente conserva la **provenienza** (sistema, id originale, timestamp) per audit e per la spiegabilità.

### 4.3 Fase 2 — Entity resolution e costruzione del grafo

Questo è il cuore della qualità. Senza, tutto il resto crolla.

1. **Normalizzazione:** ragioni sociali, indirizzi e nomi vengono normalizzati (rimozione forme societarie, maiuscole, spazi, alias).
2. **Matching:** confronto su chiavi forti (P.IVA/VAT/DUNS) e deboli (nome normalizzato, dominio email, indirizzo, telefono). L'AI locale può assistere il matching fuzzy.
3. **Risoluzione:** match ad alta confidenza → **auto-merge** in un unico nodo organizzazione (con record sorgente come provenienza); match incerti → **coda di revisione umana** con suggerimento e motivazione.
4. **Gerarchie e ruoli:** si costruiscono gli archi gruppo→controllata, organizzazione→persone, organizzazione→ruolo (cliente/fornitore/entrambi).
5. **Transazioni e interazioni** vengono agganciate alle entità risolte; gli orfani vanno in coda *data quality*.
6. **Catena di dipendenza:** da ordini, BOM e input manuali si costruiscono gli archi prodotto→componente→fornitore→area e cliente→prodotto.

### 4.4 Fase 3 — Calcolo dei pesi e degli indicatori

1. Per ogni arco **cliente↔azienda** si calcola il peso (fatturato, marginalità, anzianità, frequenza/recency interazioni).
2. Per ogni **fornitore** si calcola la **criticità** (% spesa, insostituibilità = n. alternative nel grafo, lead time, performance/ritardi, esposizione geografica).
3. Per ogni relazione si calcola la **salute** (trend di interazioni, sentiment medio, ritardi di pagamento, ticket aperti, scadenze imminenti).
4. Si calcolano gli **indicatori di rischio strutturale** percorrendo il grafo: concentrazione fatturato per cliente, concentrazione spesa per fornitore, **single point of failure** (componenti/prodotti con un solo fornitore, in una sola area), **key-person risk** (entità presidiate da un solo owner).
5. Tutti i pesi sono **ricalcolati** all'arrivo di nuovi dati (incrementale) e spiegabili (si può sempre risalire ai fattori).

### 4.5 Fase 4 — Uso quotidiano (esplorazione, ricerca, 360°)

1. L'utente apre la **vista 360°** di un cliente o fornitore: anagrafica risolta, persone e chi le presidia, transazioni, interazioni recenti, contratti in scadenza, indicatori di salute e rischio — tutto su un nodo, con link ai vicini.
2. **Esplora il grafo** partendo da un nodo ed espandendo per relazioni (clienti→prodotti→fornitori), con filtri per tipo di nodo/relazione e per peso/rischio.
3. **Interroga in linguaggio naturale** (GraphRAG): l'AI locale traduce la domanda in attraversamento del grafo + ricerca semantica, e risponde **citando nodi e percorsi**.
4. **Confronta** entità (es. due fornitori alternativi per lo stesso componente) o **simula** ("se rimuovo questo fornitore, cosa resta scoperto?").

### 4.6 Fase 5 — Alert proattivi e azioni

1. Il modulo monitora il grafo e genera **alert** quando una soglia viene superata o un trend si deteriora: contratto in scadenza, cliente strategico con calo interazioni + ritardi pagamento (segnale di churn), fornitore critico con ritardi crescenti (deterioramento), nuova concentrazione di rischio emersa.
2. Gli alert sono **spiegati** (quali fattori, quali nodi) e instradabili tramite il modulo **messaging/automation** esistente (notifica all'owner, creazione di un task).
3. L'utente agisce; l'azione/feedback torna nel grafo (es. "rischio mitigato: aggiunto fornitore alternativo"), migliorando i pesi futuri.

### 4.7 Fase 6 — Manutenzione continua del grafo

1. Sync incrementali periodici riallineano il grafo alle fonti (nuove transazioni, nuovi contatti, contratti rinnovati).
2. La **coda di data quality / entity resolution** viene presidiata: gli umani confermano i merge incerti, correggono gli orfani.
3. La **provenienza** garantisce che ogni nodo/arco sia tracciabile e cancellabile (GDPR: diritto all'oblio per le persone).

### 4.8 Diagramma sintetico del flusso

\`\`\`
Fonti (CRM, ERP, Email/Cal, Contratti PDF, CSV)
   │  connettori + batch (Tika/OCR)
   ▼
Ingestione → testo→Qdrant (semantica) | struttura→MySQL (nodi/archi)
   │
   ▼
Entity Resolution (chiavi forti/deboli, auto-merge / revisione umana)
   │
   ▼
Grafo CRM/SRM unico  ── pesi & indicatori (legame, criticità, salute, rischio)
   │
   ├─► Vista 360° + Esplorazione grafo + Confronto/Simulazione
   ├─► GraphRAG (AI locale, risposte citate)
   └─► Alert proattivi → messaging/automation → azione → feedback nel grafo
\`\`\`

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa la convenzione del motore universale: **nodi tipizzati** + **archi pesati**, struttura su MySQL e semantica su Qdrant. I tipi seguenti sono specifici dell'ambito ed estendibili.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi principali |
|--------------|-------------|----------------------|
| \`Organizzazione\` | Entità commerciale (cliente/fornitore/prospect/partner) | ragione sociale, P.IVA/VAT/DUNS, ruoli, paese, settore, stato |
| \`Persona\` | Referente esterno o owner interno | nome, ruolo, ruolo decisionale, email, organizzazione |
| \`Sede\` | Sede legale/operativa | indirizzo, paese, geo |
| \`Contratto\` | Accordo di vendita o acquisto | oggetto, valore, decorrenza, scadenza, SLA/penali |
| \`Opportunità\` | Trattativa di vendita | valore, fase, probabilità, data chiusura prevista |
| \`Ordine\` | Ordine di acquisto/vendita | righe, importo, data, stato |
| \`Fattura\` | Documento amministrativo | importo, scadenza, stato pagamento, ritardo |
| \`Interazione\` | Email, call, riunione, ticket, nota | tipo, data, direzione, sentiment, sintesi |
| \`Prodotto/Servizio\` | Ciò che si vende/acquista | nome, categoria, codice |
| \`Componente/Materia prima\` | Input di un prodotto | nome, codice, criticità |
| \`AreaGeografica\` | Paese/regione di un fornitore | nome, codice, livello rischio geo |
| \`CategoriaSpesa\` | Categoria merceologica acquisti | nome, tassonomia |
| \`RischioConcentrazione\` | Nodo derivato che reifica un'esposizione | tipo, livello, fattori |

### 5.2 Tipi di relazione (archi)

| Relazione | Da → A | Significato | Direzione |
|-----------|--------|-------------|-----------|
| \`HA_RUOLO\` | Organizzazione → Ruolo | cliente / fornitore / entrambi | — |
| \`CONTROLLA\` | Organizzazione → Organizzazione | gerarchia societaria (gruppo→controllata) | orientata |
| \`HA_REFERENTE\` | Organizzazione → Persona | persona appartiene all'org | orientata |
| \`PRESIDIA\` | Persona(interna) → Organizzazione | owner che gestisce la relazione | orientata |
| \`RIPORTA_A\` | Persona → Persona | gerarchia decisionale interna al cliente | orientata |
| \`HA_CONTRATTO\` | Organizzazione → Contratto | contratto attivo/storico | orientata |
| \`HA_OPPORTUNITÀ\` | Organizzazione → Opportunità | trattativa in pipeline | orientata |
| \`HA_ORDINE\` / \`HA_FATTURA\` | Organizzazione → Ordine/Fattura | transazione | orientata |
| \`HA_INTERAZIONE\` | Organizzazione/Persona → Interazione | contatto avvenuto | orientata |
| \`ACQUISTA\` | Cliente → Prodotto | il cliente compra il prodotto | orientata |
| \`RICHIEDE\` | Prodotto → Componente | distinta/dipendenza tecnica | orientata |
| \`FORNISCE\` | Fornitore → Componente/Prodotto | il fornitore fornisce l'input | orientata |
| \`LOCALIZZATO_IN\` | Fornitore → AreaGeografica | esposizione geografica | orientata |
| \`GARANTISCE\` | Contratto(cliente) → Prodotto/SLA | impegno verso il cliente | orientata |
| \`ALTERNATIVO_A\` | Fornitore → Fornitore | fornitori sostituibili per stesso componente | reciproca |
| \`ESPONE_A\` | (catena) → RischioConcentrazione | reifica un single point of failure | orientata |
| \`SIMILE_A\` | nodo → nodo | similarità semantica (da Qdrant) | reciproca |

### 5.3 Criteri di peso degli archi

Il peso è ciò che rende il grafo *navigabile con priorità* dall'AI. Ogni categoria di arco ha una formula configurabile e **spiegabile** (sempre scomponibile nei fattori).

| Arco / indicatore | Fattori di peso | Logica |
|-------------------|-----------------|--------|
| Legame **cliente↔azienda** | fatturato, marginalità, anzianità, frequenza+recency interazioni | peso alto = cliente strategico; cresce con valore e relazione viva |
| **Criticità fornitore** | % di spesa, insostituibilità (1/numero alternative), lead time, esposizione geografica, performance | peso alto = fornitore critico/insostituibile |
| Forza relazione **interpersonale** | frequenza interazioni, sentiment medio, recency, ruolo decisionale del referente | misura quanto la relazione è presidiata e sana |
| **Salute** della relazione | trend interazioni, sentiment, ritardi pagamento, ticket aperti, scadenze | cala = segnale di churn/deterioramento |
| **Rischio concentrazione** | quota su singolo nodo, n. percorsi alternativi (densità/clustering), esposizione geo | alto = single point of failure |
| \`SIMILE_A\` (semantico) | distanza coseno embedding (Qdrant) | per suggerimento di collegamenti non evidenti |

**Principi di pesatura (coerenti col motore universale):** pesi normalizzati e configurabili; ricalcolo incrementale all'arrivo di nuovi dati; decadimento temporale (interazioni vecchie pesano meno); ogni peso è tracciabile ai fattori per la spiegabilità GraphRAG; il feedback umano (alert gestito, merge confermato) ri-alimenta i pesi.

---

## 6. Fonti dati & connettori (ingestione)

L'ingestione riusa l'infrastruttura batch (Spring Batch), il text extraction (Tika/OCR), gli embedding (Qdrant) e i moduli email/calendar esistenti. I connettori sono **plugin PF4J** installabili dal marketplace, ciascuno con scope e frequenza configurabili.

| Fonte | Connettore | Cosa estrae | Note local-first |
|-------|------------|-------------|------------------|
| **CRM** (Salesforce, HubSpot, Dynamics, Pipedrive…) | plugin API/REST | organizzazioni, contatti, opportunità, contratti, attività | credenziali on-prem; pull schedulato |
| **ERP / gestionale** (SAP, Dynamics, Odoo, gestionali IT) | plugin DB/API | anagrafiche, ordini, fatture, pagamenti, listini, BOM | la fonte di verità transazionale |
| **Email** (IMAP) | modulo \`email\` esistente | interazioni, thread, allegati | estrazione/sentiment in locale (Ollama) |
| **Calendario** | modulo \`calendar\` esistente | riunioni, partecipanti | collegamento interazioni↔entità |
| **Contratti / documenti** | modulo \`document\` + folder watcher | PDF contratti → testo, clausole (scadenza, valore, penali) | Tika + OCR + chunk→Qdrant |
| **CSV / Excel** | importer generico | anagrafiche e dati legacy | per dati non in sistema |
| **Arricchimento esterno** (registri imprese, DUNS, rischio geo) | plugin opzionale | chiavi forti, dati societari, rischio paese | **opt-in**: richiede consenso (esce dal perimetro) |

**Principi di ingestione:** incrementale e idempotente (riparte senza duplicare); provenienza su ogni record; tutto ciò che è testo → Qdrant, tutto ciò che è struttura → MySQL; l'arricchimento esterno è sempre opt-in e tracciato, perché è l'unico flusso che può far uscire dati dal perimetro.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

Mappa concreta delle funzionalità, distinguendo **MVP** (primo valore spedibile), **evoluzione** (fasi successive) e **manutenzione** (ciò che va presidiato nel tempo). Ogni voce si àncora alla struttura esagonale: dominio (\`crm\` o \`relations\`), porte in/out, adapter infrastruttura, controller \`/api/v1/...\`, feature Angular.

### 7.1 MVP — primo valore spedibile

| Funzionalità | Cosa comprende | Note tecniche |
|--------------|----------------|---------------|
| Modello a grafo CRM/SRM | Nodi/archi della sez. 5 su MySQL, semantica su Qdrant | nuovo dominio + migrazioni Flyway (una query/file) |
| Connettore CSV/Excel + 1 connettore CRM | Ingestione anagrafiche + transazioni base | riuso batch; plugin PF4J |
| Entity resolution base | Normalizzazione + match su chiavi forti/deboli, auto-merge + coda revisione | servizio dominio; assist AI locale opzionale |
| Ingestione interazioni email | Collegamento email↔organizzazione + sentiment locale | riuso modulo email + Ollama |
| Vista 360° (cliente/fornitore) | Pannello unico: anagrafica, persone, transazioni, interazioni, scadenze | controller + feature Angular |
| Esplorazione grafo interattiva | Navigazione per relazioni, filtri per tipo/peso | riuso visualizzazione grafo del motore |
| Pesi base + indicatori | Legame cliente, criticità fornitore, salute | servizio dominio configurabile |
| GraphRAG sul grafo | Q&A in linguaggio naturale con citazione nodi/percorsi | riuso pipeline LLM/RAG, AI locale default |
| Alert scadenze contratti | Notifica contratti in scadenza | riuso messaging/automation |
| Bilinguismo IT/EN | UI ed enum tradotte | vincolo di progetto |

### 7.2 Evoluzione — fasi successive

| Funzionalità | Valore aggiunto |
|--------------|-----------------|
| Connettori ERP nativi (SAP, Odoo, gestionali IT) | dati transazionali completi, BOM per la catena dipendenze |
| Catena di dipendenza multi-livello (sub-tier) | analisi di concentrazione e single point of failure profonda |
| Motore di rischio strutturale | concentrazione fatturato/spesa, SPOF, key-person risk, esposizione geografica |
| Simulazione "what-if" | "se questo fornitore/cliente salta, cosa resta scoperto?" |
| Churn/deterioramento predittivo | segnali deboli anticipati (interazioni+pagamenti+sentiment) |
| Entity resolution avanzata | householding/gerarchie, survivorship, arricchimento DUNS opt-in |
| Suggerimento collegamenti non evidenti | fornitori alternativi, cross-sell, conflitti cliente↔fornitore |
| Dashboard direzionali | concentrazione portafoglio, salute fornitori per categoria |
| Multimodale su contratti | estrazione clausole avanzata, confronto versioni |
| Automazioni avanzate | playbook su alert (task, escalation) via automation |

### 7.3 Manutenzione — da presidiare nel tempo

- **Qualità dei connettori:** API di CRM/ERP cambiano; i plugin vanno versionati e testati.
- **Coda entity resolution / data quality:** richiede presidio umano continuo (merge incerti, orfani).
- **Taratura di pesi e soglie:** rivedere periodicamente con i referenti commerciali/acquisti.
- **Privacy & GDPR:** gestione diritto all'oblio, base giuridica, audit degli accessi e dei flussi cloud opt-in.
- **Migrazioni Flyway:** una query per file; schema del grafo evolutivo e retro-compatibile.
- **Documentazione bilingue:** IT/EN sempre allineate; tracciamento sviluppi nella cartella \`Sviluppi\`.

---

## 8. Casi d'uso AI / GraphRAG

Il GraphRAG combina attraversamento del grafo (relazioni esplicite, pesi) e ricerca semantica (Qdrant su email/contratti/note), con AI locale (Ollama) di default. Ogni risposta **cita nodi e percorsi**.

| # | Domanda dell'utente | Come l'AI risponde (grafo + semantica) |
|---|---------------------|----------------------------------------|
| 1 | "Dammi tutto su Rossi S.p.A." | Vista 360°: aggrega nodi collegati (contratti, ordini, fatture scadute, interazioni, persone, owner) e sintetizza |
| 2 | "Quali clienti strategici sono presidiati da una sola persona?" | Percorre \`PRESIDIA\`, incrocia con peso legame cliente; emerge il key-person risk |
| 3 | "Se ACME Components fallisce, cosa resta scoperto?" | Attraversa Fornitore→Componente→Prodotto→Cliente/Contratto; somma fatturato esposto |
| 4 | "Quali fornitori sono single point of failure?" | Trova componenti con un solo \`FORNISCE\` e nessun \`ALTERNATIVO_A\`, pesati per spesa |
| 5 | "Quali clienti stanno per abbandonarci?" | Combina calo interazioni, sentiment negativo, ritardi pagamento, contratto in scadenza |
| 6 | "Questo prospect è già fornitore con cui abbiamo un contenzioso?" | Entity resolution + ruoli multipli + stato relazione; segnala il conflitto |
| 7 | "Quanto fatturato dipende dai miei top 5 clienti?" | Concentrazione: somma pesi legame; valuta rischio di concentrazione |
| 8 | "Trova un fornitore alternativo per il componente X in un'altra area" | \`FORNISCE\` + \`ALTERNATIVO_A\` + \`LOCALIZZATO_IN\` diverso dall'area a rischio |
| 9 | "Quali contratti scadono nei prossimi 90 giorni e quanto valgono?" | Filtra \`Contratto\` per scadenza, ordina per valore, raggruppa per owner |
| 10 | "Prepara il brief per la call con il decisore di Beta Srl" | Sintetizza interazioni recenti, opportunità aperte, ruolo decisionale, ultimi segnali |

**Suggerimento di collegamenti non evidenti** (cuore del progetto): l'AI propone link mancanti — la stessa persona referente in due clienti diversi, un fornitore concentrato che alimenta più prodotti strategici, un cliente con potenziale di cross-sell simile (via \`SIMILE_A\`) a un cliente già acquisito.

---

## 9. KPI & metriche di successo

| Categoria | KPI | Target indicativo |
|-----------|-----|-------------------|
| Qualità del grafo | % entità con chiave forte risolta (no duplicati) | > 95% |
| Qualità del grafo | % transazioni agganciate a entità risolta | > 98% |
| Copertura | % clienti/fornitori attivi importati e collegati | > 90% |
| Copertura interazioni | % interazioni email collegate a un'entità | > 80% |
| Visibilità rischio | % spesa coperta da mappa dipendenze (almeno Tier 1) | > 85% |
| Anticipo segnali | preavviso medio su churn/deterioramento | settimane prima dell'evento |
| Efficacia AI | % risposte GraphRAG con citazioni corrette (no allucinazioni) | > 90% |
| Adozione | n. viste 360° / query grafo per utente attivo / settimana | crescente |
| Valore operativo | contratti rinnovati in tempo grazie agli alert | ↑ tasso rinnovo |
| Valore di rischio | n. single point of failure individuati e mitigati | tracciato nel tempo |
| Privacy | % flussi che restano on-prem (no cloud) | 100% di default |

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---------|---------|-------------|
| **Entity resolution errata** (merge sbagliati o duplicati) | Vista 360° inaffidabile, risposte sicure ma sbagliate | Chiavi forti prioritarie, soglie prudenti, coda revisione umana, provenienza tracciata, reversibilità dei merge |
| **Dati commerciali sensibili esposti** | Danno enorme, perdita fiducia | Local-first di default, cloud solo opt-in tracciato, visibilità per ruolo, cifratura a riposo |
| **GDPR / dati personali** | Sanzioni, problemi legali | Minimizzazione, base giuridica, diritto all'oblio sul nodo persona senza distruggere aggregati, audit |
| **Catena dipendenze incompleta (solo Tier 1)** | Rischio sotto-livelli invisibile | Ingestione BOM, input manuale dei sub-tier, segnalare esplicitamente le zone non mappate |
| **API CRM/ERP instabili o limitate** | Ingestione fragile | Connettori versionati, sync idempotente, fallback CSV, retry |
| **Allucinazioni dell'AI** | Decisioni sbagliate | GraphRAG con citazione obbligatoria di nodi/percorsi; risposte ancorate al grafo, non solo ai vettori |
| **Pesi mal tarati** | Falsi allarmi o segnali persi | Pesi configurabili e spiegabili, revisione periodica con i referenti, feedback loop |
| **Resistenza all'adozione** (sembra "un altro sistema") | Basso utilizzo | Posizionamento chiaro: non sostituisce, *unifica*; valore immediato dalla vista 360° |
| **Qualità dato sorgente scadente** | Garbage in, garbage out | Coda data quality, regole di validazione al boundary, indicatori di copertura visibili |

---

## 11. Manutenzione & evoluzione

- **Presidio della qualità del grafo:** la coda di entity resolution e di data quality è un processo continuo, non un'attività una tantum; serve un owner interno.
- **Sync incrementali e idempotenti:** schedulati (es. notturni), con monitoraggio degli errori e della copertura; ogni run conserva la provenienza.
- **Versionamento dei connettori:** le API esterne cambiano; i plugin PF4J vanno mantenuti e testati contro le nuove versioni di CRM/ERP.
- **Evoluzione dello schema del grafo:** nuovi tipi di nodo/relazione si aggiungono in modo retro-compatibile; migrazioni Flyway con una sola query per file.
- **Taratura di pesi e soglie:** revisione periodica con commerciale e acquisti per mantenere gli indicatori (strategico, critico, SPOF) allineati alla realtà.
- **Privacy operativa:** audit periodico degli accessi e dei (rari) flussi cloud opt-in; gestione delle richieste GDPR.
- **Documentazione e tracciamento:** documentazione bilingue IT/EN sempre aggiornata; ogni sviluppo tracciato nella cartella \`Sviluppi\` con la nomenclatura datata; enum tradotte e veicolate al frontend.
- **Roadmap di crescita:** dai connettori ERP nativi, alla catena multi-tier, al motore di rischio strutturale e alla simulazione what-if, fino al predittivo su churn/deterioramento.

---

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo / dominio | Ruolo nell'ambito Clienti & fornitori |
|------------------|----------------------------------------|
| \`knowledge\` | Base del motore a grafo: l'ambito vi aggiunge i propri tipi di nodo/relazione (estensione modulare) |
| \`llm\` | GraphRAG e sentiment via fallback chain (Ollama default → cloud opt-in); spiegazioni e sintesi |
| \`document\` | Ingestione contratti/documenti: Tika + OCR + chunking → Qdrant per la semantica |
| \`email\` | Ingestione interazioni email (IMAP), collegate alle entità; estrazione locale |
| \`calendar\` | Riunioni/call come interazioni collegate alle entità e alle persone |
| \`mcp\` | Esposizione del grafo come tool MCP e/o consumo di tool esterni per arricchimento |
| \`automation\` | Playbook su alert (task, escalation, rinnovi) |
| \`messaging\` | Notifiche degli alert agli owner sui canali configurati |
| \`marketplace\` + \`plugin\` (PF4J) | Distribuzione del modulo e dei connettori CRM/ERP come plugin installabili |
| \`agent\` | Agenti che interrogano il grafo ed eseguono azioni (preparare brief, monitorare rischi) |
| \`auth\` | Visibilità per ruolo sui dati sensibili (margini, listini, contatti) |
| \`finetuning\` | Adattamento locale dei modelli su terminologia commerciale aziendale (evoluzione) |
| \`common\` (analytics/backup) | Metriche di adozione e backup del grafo commerciale |
| **Infrastruttura** | MySQL (struttura grafo) + Qdrant (semantica) + Spring Batch (ingestione) + architettura esagonale (dominio puro, wiring \`DomainConfig\`) |
| **Frontend Angular 21** | Nuova feature lazy-loaded: vista 360°, esplorazione grafo, dashboard rischio, Signal store, IT/EN |

In sintesi, l'ambito **Clienti & fornitori** non introduce nuova infrastruttura: è un **modulo di dominio** che riusa il motore a grafo, la pipeline LLM/RAG locale, i connettori plugin e l'ingestione esistenti, aggiungendo i tipi di nodo/relazione, i pesi e gli indicatori specifici del CRM/SRM, nel pieno rispetto dei vincoli local-first, privacy, open source e bilinguismo IT/EN di LocalMind.

---

*Documento redatto il 2026-06-29 come guida agli sviluppi dell'ambito Clienti & fornitori (gruppo enterprise) del motore di Knowledge Graph universale di LocalMind.*
`;
