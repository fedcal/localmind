export const content = `# Community & moderazione

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo documento descrive l'ambito di estensione **Community & moderazione**, appartenente al **gruppo core** di LocalMind. È un ambito trasversale e abilitante: non è un verticale di dominio (turismo, eventi, enterprise…) ma il **sistema sociale e di governance dei contenuti** che permette al Knowledge Graph universale di essere alimentato, valutato e curato da una pluralità di contributori senza degradare in qualità. Dove il *Motore Knowledge Graph* (documento 20) definisce *come* nodi e archi pesati vengono persistiti e navigati, questo ambito definisce *chi* li crea, *con quale affidabilità*, *come emergono i contenuti migliori* e *come si difende il grafo da spam, manipolazione e contenuti dannosi*.

L'ambito copre quattro pilastri strettamente interconnessi, indicati esplicitamente nel focus: **contributi utenti**, **valutazioni**, **ranking emergente**, **anti-spam e moderazione/curatela dei contenuti**. Questi quattro pilastri sono il motore della promessa "community-driven" della visione consumer ("Wikipedia dei luoghi") e, allo stesso tempo, il presidio di governance e qualità necessario in ambito enterprise (curatela della conoscenza interna, approvazione di merge, validazione di relazioni). Un unico sistema sociale, parametrizzato per dominio, esattamente come il motore a grafo sottostante.

Tutto resta **local-first**, self-hostable, con **AI Ollama di default** per la moderazione automatica, **riusando MySQL 8.0 e Qdrant** (niente nuovi datastore), con UI/enum/documentazione **bilingui IT/EN**, estensibilità via **plugin PF4J** e migrazioni **Flyway con una sola query per file**.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema di fondo

Un grafo di conoscenza alimentato dalla community o da fonti enterprise eterogenee è potente ma intrinsecamente **fragile**: la sua utilità dipende interamente dalla qualità, dall'affidabilità e dall'attualità dei contenuti che vi confluiscono. Senza un sistema di community e moderazione, qualsiasi piattaforma collaborativa collassa per cause ricorrenti e ben documentate:

- **Il dilemma "chiunque può contribuire" vs "ci si può fidare".** La forza di un grafo community-driven (la "Wikipedia dei luoghi") è la libertà di contribuzione; la sua debolezza è che ogni contributo non verificato è un potenziale vettore di errore, vandalismo o spam. Senza un meccanismo che distingua il contributo affidabile da quello rumoroso, il grafo si riempie di nodi duplicati, recensioni false, relazioni inventate e POI fantasma.
- **Spam, bot e attacchi coordinati.** Le piattaforme aperte sono bersaglio sistematico di spam commerciale, link injection, fake review farm, sockpuppet (account multipli dello stesso attore) e "raid"/spam-wave coordinati. In un grafo, questi attacchi non degradano solo una pagina ma **avvelenano le relazioni**: un nodo spam con molti archi falsi inquina i traversal e il ranking di tutto il vicinato.
- **Manipolazione del ranking.** Se i contenuti migliori "emergono" da voti e contributi, allora il sistema di voto diventa esso stesso un bersaglio: voto multiplo, brigading (voto coordinato), reciprocità (gruppi che si votano a vicenda), reputazione gonfiata artificialmente. Un ranking ingenuo (un voto = un punto) è banalmente manipolabile.
- **Contenuti dannosi e responsabilità.** UGC (user-generated content) significa anche contenuti illeciti, diffamatori, hate speech, dati personali esposti. Una piattaforma self-hosted scarica sull'amministratore la responsabilità di moderare; senza strumenti integrati, la moderazione è ingestibile manualmente già a volumi modesti.
- **Curatela enterprise senza governance.** In azienda il problema si ribalta: non c'è spam anonimo, ma c'è il rischio di conoscenza **obsoleta, contraddittoria o non autorizzata**. Chi approva che una "decisione architetturale" diventi ufficiale? Chi valida che una relazione \`DEPENDS_ON\` estratta dall'AI sia corretta prima che alimenti un'impact analysis critica? Serve un flusso di **revisione, approvazione e attribuzione** con audit trail.
- **Opacità e perdita di fiducia.** Se gli utenti non capiscono *perché* un contenuto è stato rimosso, *perché* il loro contributo è in coda, o *perché* un POI è in cima al ranking, perdono fiducia e se ne vanno. La trasparenza (spiegazioni, audit trail, appelli leggeri) è oggi un requisito, non un di più.
- **Moderazione non scalabile e non sovrana.** Affidarsi a servizi cloud di trust & safety contraddice il principio local-first e la privacy enterprise. La moderazione automatica deve poter girare **in locale**, con l'AI Ollama.

### 1.2 La soluzione: un livello sociale di fiducia, qualità e governance sopra il grafo

LocalMind risponde con un **livello di community & moderazione** che si innesta nativamente sul motore a grafo e ne governa l'alimentazione attraverso quattro meccanismi cooperanti:

1. **Contributi tracciati e attribuiti.** Ogni nodo, arco, recensione o modifica nasce da un **contributo** con autore, timestamp, sorgente e stato (bozza → in revisione → pubblicato → rifiutato/ritirato). Il contributo è l'unità atomica di governance: nulla entra nel grafo "pubblicato" senza passare per questo modello.
2. **Valutazioni pesate dalla reputazione.** Gli utenti esprimono valutazioni (voti, recensioni, conferme/smentite di relazioni, segnalazioni). Ogni valutazione vale in proporzione alla **reputazione** del valutatore, non in modo uniforme: questo neutralizza il voto multiplo e premia i contributori storicamente affidabili — il pattern centrale dei sistemi di reputazione collaborativi.
3. **Ranking emergente trasparente.** I contenuti migliori salgono per **aggregazione pesata** di valutazioni, reputazione, freschezza e segnali di qualità — non per popolarità grezza. Il ranking alimenta direttamente il **peso degli archi** del grafo (vedi documento 20, §5.4), chiudendo il cerchio: la community migliora il GraphRAG.
4. **Anti-spam e moderazione ibrida (AI + umano).** Un pipeline di moderazione combina **filtri automatici locali** (Ollama per classificazione contenuti, euristiche anti-spam su identità/comportamento) come prima linea, e **revisione umana** (curatori/moderatori) per le zone grigie. Tutto con audit trail, soglie configurabili, appelli e code di revisione.

### 1.3 Perché un solo sistema per consumer ed enterprise

| Dimensione | Senza community/moderazione | Con il livello LocalMind |
|---|---|---|
| Chi alimenta il grafo | Solo ingestione automatica o admin | Community + connettori + curatori, tutti tracciati |
| Affidabilità di un contributo | Indistinta | Pesata dalla reputazione dell'autore |
| Come emergono i contenuti | Popolarità grezza o ordine d'inserimento | Ranking emergente pesato e spiegabile |
| Difesa da spam/manipolazione | Assente | Anti-spam ibrido + voto pesato + rate limiting |
| Governance enterprise | Nessun flusso di approvazione | Revisione, approvazione, audit, attribuzione |
| Trasparenza | Decisioni opache | Spiegazioni, audit trail, appelli |
| Sovranità | Dipendenza da cloud T&S | Moderazione AI locale (Ollama) |

Il valore è duplice e simmetrico:

- **Consumer (turismo, eventi, cultura, sport, ristorazione…):** abilita davvero la "Wikipedia dei luoghi". Le persone del territorio contribuiscono POI, recensioni, itinerari; i migliori emergono per merito; lo spam commerciale e le fake review sono filtrati; comuni e pro loco mantengono la sovranità del dato e un pannello di curatela.
- **Enterprise (doc, processi, repo, persone, decisioni…):** trasforma il grafo in una **base di conoscenza governata**. La conoscenza interna ha owner, viene approvata, validata, datata e auditata; le relazioni estratte dall'AI passano per conferma umana prima di diventare "fatti" su cui si fa impact analysis; la reputazione interna riflette l'expertise (chi cura cosa).

### 1.4 Relazione con il Core Value

Il Core Value di LocalMind (\`.planning/PROJECT.md\`) è che *l'AI navighi un grafo pesato per rispondere a domande complesse e far emergere collegamenti non evidenti, in qualsiasi dominio, restando local-first*. La community & moderazione è il **garante della qualità del grafo**: senza di essa il peso degli archi sarebbe rumoroso e il GraphRAG inaffidabile. È il fattore "feedback utenti" del peso degli archi (documento 20, §5.4) reso sistema completo, e il presidio che impedisce a spam e manipolazione di corrompere i traversal.

---

## 2. Personas & utenti target

| Persona | Profilo | Obiettivi rispetto all'ambito | Bisogni dal sistema |
|---|---|---|---|
| **Contributore community (consumer)** | Cittadino/appassionato che aggiunge POI, eventi, recensioni | Condividere conoscenza locale, vedere riconosciuto il proprio contributo | Form di contributo semplice, attribuzione, badge/reputazione, feedback sullo stato |
| **Lettore/votante** | Utente che consulta e valuta | Trovare contenuti affidabili, votare, segnalare abusi | Ranking trasparente, voto/segnalazione a un clic, spiegazione del ranking |
| **Curatore / moderatore** | Volontario o staff (pro loco, comune, redazione) | Validare, correggere, rimuovere, approvare contributi | Coda di revisione, strumenti bulk, audit trail, policy chiare, suggerimenti AI |
| **Knowledge curator (enterprise)** | Dipendente owner di un'area di conoscenza | Approvare conoscenza ufficiale, validare relazioni AI | Flusso di approvazione, attribuzione owner, versioning, audit di compliance |
| **Amministratore / DevOps** | Gestisce l'istanza self-hosted | Configurare policy, soglie, rate limit; sostenibilità | Pannello policy, configurazione anti-spam, metriche, moderazione locale |
| **Trust & Safety lead** | Responsabile qualità/sicurezza dei contenuti | Definire guidelines, gestire escalation e attacchi coordinati | Dashboard moderazione, regole, rilevamento spam-wave, reportistica |
| **Utente segnalato / sanzionato** | Contributore oggetto di azione moderativa | Capire la decisione, fare appello | Notifica con motivazione, flusso di appello leggero, trasparenza |
| **AI / agente di moderazione** | Classificatore LLM locale + euristiche | Filtrare in prima linea, assegnare priorità alla coda | Tool di classificazione (Ollama), soglie, budget, formati strutturati |
| **AI / GraphRAG (consumatore a valle)** | Consuma il grafo curato | Usare contenuti affidabili e pesi puliti | Stato "pubblicato", peso da reputazione, esclusione contenuti in revisione/rimossi |

Persone primarie dell'ambito: il **Contributore community** (consumer), il **Curatore/moderatore** e il **Knowledge curator enterprise**. Il successo si misura su quanto è facile e gratificante contribuire bene e quanto è efficiente moderare male.

---

## 3. Requisiti in input

Questa sezione definisce in dettaglio **cosa serve in ingresso** al sistema di community & moderazione: dati, configurazioni, contratti, policy e vincoli. È deliberatamente esaustiva perché è il contratto da cui dipende ogni sviluppo successivo. Si appoggia ai contratti del Motore Knowledge Graph (documento 20, §3) per nodi e archi, e li estende con la dimensione sociale.

### 3.1 Input di policy e governance (configurazione fondante per dominio)

Prima di accettare contributi, un dominio/istanza deve **dichiarare le proprie regole**. È l'input fondante, analogo allo schema modulare del motore a grafo.

| Input | Descrizione | Forma attesa | Obbligatorio |
|---|---|---|---|
| **Community guidelines** | Cosa è accettabile/inaccettabile, con esempi, bilingui IT/EN | Documento + regole machine-readable (categorie vietate) | Sì |
| **Workflow di moderazione** | Pre-moderazione (approva prima di pubblicare) vs post-moderazione (pubblica poi controlla) vs reactive (solo su segnalazione), per tipo di contenuto/dominio | Configurazione \`ModerationPolicy\` | Sì |
| **Soglie di auto-approvazione** | Reputazione minima per pubblicare senza revisione; soglia di confidenza del classificatore AI per auto-azioni | Numeri/soglie per dominio | Sì (con default) |
| **Policy di reputazione** | Eventi che assegnano/sottraggono reputazione e relativi pesi; livelli/badge; decadimento | Configurazione \`ReputationPolicy\` | Sì (con default) |
| **Policy anti-spam** | Rate limit per ruolo, regole identità/comportamento, liste (allow/deny), soglie spam-wave | Configurazione \`AntiSpamPolicy\` | Sì (con default) |
| **Ruoli e permessi** | Mappa ruolo→capacità (contribuire, votare, moderare, approvare, configurare) | RBAC esteso su \`auth\` esistente | Sì |
| **Scala di valutazione** | Tipi di valutazione ammessi (voto ±1, stelle 1–5, conferma/smentita relazione, segnalazione) per tipo di nodo | Catalogo valutazioni per dominio | Sì |

Vincolo: tutte le enum (stato contributo, esito moderazione, tipo segnalazione, livello reputazione, tipo valutazione) devono essere **bilingui IT/EN** e reindirizzate al frontend in base allo switch lingua (regola di progetto). Le policy non devono richiedere ricompilazione del core: vanno gestite come dati/configurazione o come estensioni PF4J.

### 3.2 Input di contenuto (i contributi)

Sono i dati prodotti dagli utenti che il sistema deve accettare, validare e instradare.

| Tipo di contributo | Esempi | Sorgente | Note |
|---|---|---|---|
| **Creazione/modifica nodo** | Nuovo POI, evento, FAQ, decisione | UI + API CRUD del motore a grafo | Passa per workflow di moderazione |
| **Creazione/modifica arco** | Relazione \`NEAR\`, \`PAIRED_WITH\`, \`DEPENDS_ON\` | UI + API | Conferma/smentita umana → fattore peso |
| **Recensione / testo libero** | Recensione di un locale, commento | UI | Soggetta a classificazione contenuti |
| **Valutazione** | Voto, stelle, "utile/non utile", conferma relazione | UI a un clic | Pesata dalla reputazione |
| **Segnalazione (report/flag)** | "Spam", "offensivo", "errato", "dato personale" | UI a un clic | Alimenta la coda di moderazione |
| **Suggerimento di correzione** | Proposta di edit su contenuto altrui | UI | Stile wiki, con storico |
| **Appello** | Contestazione di un'azione moderativa | UI | Flusso di revisione dedicato |

Per ogni contributo sono richiesti almeno: **identità autore** (utente autenticato via \`auth\`), **timestamp**, **tipo**, **payload** (riferimento a nodo/arco o testo), **dominio/contesto**, e — ove applicabile — **sorgente** e **lingua**.

### 3.3 Input per la sottomissione di un contributo (contratto API)

Per sottomettere un contributo il chiamante deve fornire:

- \`authorId\` — **obbligatorio**, utente autenticato (da \`LocalAuthFilter\`/\`auth\`).
- \`contributionType\` — **obbligatorio** (CREATE_NODE, UPDATE_NODE, CREATE_RELATION, REVIEW, VOTE, REPORT, EDIT_SUGGESTION, APPEAL), validato.
- \`targetRef\` — riferimento al soggetto: \`nodeId\`/\`relationId\` esistente, o payload di nuovo nodo/arco conforme allo schema del motore a grafo (documento 20, §3.3/§3.4).
- \`payload\` — contenuto specifico del tipo (testo recensione, valore voto, motivo segnalazione, proprietà del nodo).
- \`domain\` / contesto — per applicare la policy corretta.
- \`language\` — IT/EN per testo libero (per la classificazione e l'i18n).
- *(opzionale)* \`sourceConnectorId\`/\`sourceDocumentId\` — se il contributo deriva da una fonte (tracciabilità).

Validazioni minime ai confini (regola di sicurezza di progetto): autore autenticato e autorizzato per il \`contributionType\`; payload conforme allo schema; lunghezze rispettate; nessun input fidato; rate limit non superato (vedi §3.6); idempotenza (un utente non può votare due volte lo stesso target — upsert del voto).

### 3.4 Input per una valutazione (contratto API)

- \`evaluatorId\` — **obbligatorio**, autenticato; deve avere il permesso e la reputazione minima per votare.
- \`targetRef\` — nodo, arco o contributo valutato — **obbligatorio**, esistente.
- \`evaluationType\` — **obbligatorio** (UPVOTE/DOWNVOTE, STAR 1–5, CONFIRM_RELATION/REJECT_RELATION, HELPFUL/NOT_HELPFUL).
- \`value\` — valore conforme alla scala dichiarata per quel target/dominio.
- *(derivato, non in input)* \`weight\` della valutazione = funzione della reputazione del valutatore al momento del voto (calcolato server-side, mai fornito dal client per evitare manipolazione).

Validazioni: un solo voto attivo per (evaluator, target, type) — modifica anziché duplicazione; no auto-valutazione del proprio contributo (anti-reciprocità di base); reputazione ≥ soglia; rate limit.

### 3.5 Input per la moderazione (contratto API e coda)

Ogni elemento che entra in moderazione (per workflow o per segnalazione) genera un **caso di moderazione** con:

- \`caseId\`, \`targetRef\`, \`reason\` (categoria: SPAM, OFFENSIVE, WRONG, PRIVACY, OTHER), \`reporterId\` (se da segnalazione) o \`system\` (se da AI).
- \`aiAssessment\` — esito del classificatore locale: categoria, \`confidence ∈ [0,1]\`, motivazione testuale (per spiegabilità).
- \`priority\` — derivata da gravità categoria + reputazione del segnalante + volume di segnalazioni sullo stesso target.
- \`status\` — OPEN → IN_REVIEW → RESOLVED(ACTION) → (eventuale) APPEALED.

Per la **decisione del moderatore** servono: \`caseId\`, \`decision\` (APPROVE, REJECT, EDIT, HIDE, DELETE, WARN_USER, BAN_USER, NO_ACTION), \`note\` (obbligatoria per azioni punitive, per audit), \`moderatorId\`. La decisione **deve** produrre un record di audit immutabile.

### 3.6 Input anti-spam (segnali e regole)

Il pipeline anti-spam consuma segnali su **identità** e **comportamento** (best practice 2026):

| Segnale | Esempi | Uso |
|---|---|---|
| **Identità** | Età account, email verificata, reputazione, storico | Account nuovi/non verificati → pre-moderazione obbligatoria |
| **Comportamento** | Frequenza di contributi, burst, similarità tra contributi, pattern di voto | Rilevamento bot, spam-wave, brigading |
| **Contenuto** | Presenza link, keyword commerciali, duplicazione, lingua | Classificazione AI locale (Ollama) |
| **Rete/relazione** | Account che si votano a vicenda, cluster sospetti nel grafo | Rilevamento sockpuppet/reciprocità |
| **Liste** | Allowlist (bot/fonti fidate), denylist (domini/utenti) | Bypass o blocco rapido |

Configurazione richiesta: rate limit per ruolo/azione (es. N contributi/ora, M voti/ora), soglie di burst, soglia di similarità per duplicati, soglia di confidenza per auto-hide, finestra temporale per spam-wave detection.

### 3.7 Input di configurazione e ambiente

- **Profilo LLM di moderazione:** provider Ollama (default), modello di classificazione, temperatura bassa, budget — riusando \`LlmGatewayService\` e la fallback chain (cloud opt-in).
- **Datasource MySQL + Flyway** (una query per file) per contributi, valutazioni, reputazione, casi di moderazione, audit.
- **Qdrant** per la similarità tra contributi/recensioni (rilevamento duplicati e spam farm).
- **Integrazione \`auth\`** per identità, ruoli e permessi (RBAC).
- **Integrazione event infra** (\`DomainEventPublisherPort\`) per propagare esiti al peso degli archi e alle statistiche.
- **Lingua** (IT/EN) per UI, notifiche, motivazioni, guidelines.

### 3.8 Vincoli e pre-condizioni (non funzionali)

- **Local-first / self-hostable:** la moderazione automatica gira con Ollama; nessuna dipendenza obbligatoria da servizi cloud di trust & safety.
- **Privacy:** dati personali nei contributi gestiti secondo policy; in enterprise i contenuti non lasciano l'istanza senza consenso.
- **Architettura esagonale:** tutti gli input passano per \`port/in\`; nessuna logica di framework nel dominio; nuovi servizi puri wired in \`DomainConfig\`.
- **Riuso MySQL+Qdrant:** nessun nuovo datastore.
- **i18n e immutabilità:** enum bilingui; record di audit immutabili (pattern immutabilità di progetto).
- **Sicurezza:** validazione ai confini, rate limiting su tutti gli endpoint, nessun leak di dati sensibili nei messaggi d'errore (regole di sicurezza di progetto).

---

## 4. Flusso dell'attività (step-by-step)

Il sistema ha cinque flussi macro: **(A) sottomissione e moderazione di un contributo**, **(B) valutazione e ranking emergente**, **(C) pipeline anti-spam**, **(D) reputazione**, **(E) curatela enterprise / approvazione**. Sono descritti in dettaglio perché costituiscono il comportamento osservabile del sistema.

### 4.1 Flusso A — Sottomissione e moderazione di un contributo

\`\`\`
Contributo → Validazione confini → Anti-spam (gate) → Classificazione AI (Ollama) →
Decisione workflow (auto-pubblica | coda | auto-rifiuta) → [Revisione umana] →
Persistenza stato → Effetti su grafo/reputazione → Notifica autore
\`\`\`

1. **Sottomissione.** L'utente autenticato invia un contributo (nuovo POI, recensione, relazione…) via UI → \`port/in\`. Il sistema valida ai confini (autore, tipo, payload, lunghezze, permessi).
2. **Gate anti-spam (Flusso C in linea).** Si verificano rate limit e segnali identità/comportamento. Se il gate scatta (es. burst da account nuovo) → il contributo è marcato \`HELD\` e instradato in coda con priorità alta, oppure rifiutato se in denylist. Nessun rifiuto silenzioso: l'autore riceve motivazione.
3. **Classificazione AI locale.** Il testo/contenuto è passato al classificatore Ollama: categoria (ok, spam, offensivo, dato personale…), \`confidence\` e motivazione. Per i nodi/archi si calcola anche la similarità Qdrant con l'esistente (duplicati).
4. **Decisione di workflow.** In base alla \`ModerationPolicy\` del dominio e alla reputazione dell'autore:
   - **Auto-pubblicazione**: autore sopra soglia di reputazione **e** AI confidence "pulita" alta → stato \`PUBLISHED\` immediato (post-moderazione: resta soggetto a segnalazione successiva).
   - **Coda di revisione**: zona grigia (confidence intermedia, autore non ancora fidato, dominio in pre-moderazione) → stato \`IN_REVIEW\`, inserito in coda con priorità.
   - **Auto-rifiuto/hide**: AI confidence alta su categoria vietata → stato \`REJECTED\`/\`HIDDEN\` con motivazione (appellabile).
5. **Revisione umana (se in coda).** Il moderatore vede il caso con il suggerimento AI, decide (APPROVE/EDIT/REJECT/HIDE…), aggiunge nota. La decisione genera **audit immutabile**.
6. **Persistenza ed effetti.**
   - Lo stato del contributo è persistito (MySQL).
   - Se pubblicato: il nodo/arco diventa visibile al grafo e al GraphRAG; gli archi confermati alimentano il **fattore feedback del peso** (documento 20, §5.4) via evento.
   - La reputazione dell'autore è aggiornata (Flusso D): +per contributo accettato, −per rifiuto/spam.
7. **Notifica e trasparenza.** L'autore riceve l'esito con motivazione e, se negativo, il link all'**appello**.

**Gestione errori:** classificatore non disponibile → fallback a coda umana (mai auto-pubblicazione cieca); violazione di vincolo → messaggio chiaro; conflitto di edit concorrente → strategia ottimistica con versioning. Nessun errore ingoiato (regola di progetto).

### 4.2 Flusso B — Valutazione e ranking emergente

\`\`\`
Valutazione → Validazione + reputazione valutatore → Peso del voto →
Aggregazione pesata → Score di qualità del target → Aggiornamento ranking + peso arco
\`\`\`

1. **Valutazione.** L'utente esprime un voto/stelle/conferma su un target. Validazione: permesso, reputazione ≥ soglia, no auto-voto, un voto attivo per (utente, target, tipo).
2. **Peso del voto.** Il sistema calcola \`voteWeight = f(reputazione_valutatore)\` server-side. Un voto di un curatore esperto pesa più di quello di un account appena creato — neutralizza il voto multiplo e il brigading.
3. **Aggregazione.** Lo **score di qualità** del target è una media/aggregazione **pesata** dei voti, combinata con segnali ausiliari: numero di valutatori distinti (diversità), freschezza, tasso di conferme/smentite per le relazioni.
4. **Ranking emergente.** I target sono ordinati per score di qualità (non per popolarità grezza). Il ranking è **spiegabile**: la UI mostra "in cima perché: alto consenso di curatori affidabili, recente, confermato N volte".
5. **Chiusura del cerchio sul grafo.** Lo score alimenta il **peso degli archi** correlati: una relazione molto confermata aumenta di peso; una smentita ripetutamente lo perde. Così la community migliora direttamente i traversal e il GraphRAG.
6. **Anti-manipolazione continua.** Pattern di voto anomali (cluster reciproci, burst) sono inviati al Flusso C; i voti sospetti vengono scontati o congelati in attesa di revisione.

### 4.3 Flusso C — Pipeline anti-spam

\`\`\`
Segnali (identità + comportamento + contenuto + rete) → Scoring di rischio →
Azione graduata (allow | challenge | hold | block) → [Spam-wave detection] → Audit
\`\`\`

1. **Raccolta segnali.** Ad ogni contributo/voto si raccolgono segnali di identità (età account, verifica, reputazione), comportamento (frequenza, burst, similarità), contenuto (link, keyword, duplicati via Qdrant) e rete (cluster sospetti nel grafo dei voti).
2. **Scoring di rischio.** Un punteggio combinato (euristiche + classificatore Ollama) stima la probabilità di spam/abuso.
3. **Azione graduata.** In base al rischio e alla policy: *allow* (basso), *challenge* (medio: richiesta verifica/captcha-like), *hold* (alto: coda di revisione), *block* (denylist/violazione netta). Le azioni sono **proporzionate** e configurabili.
4. **Rilevamento attacchi coordinati (spam-wave).** Una finestra temporale monitora picchi anomali (molti contributi simili, da account correlati, sullo stesso target/area). Al superamento della soglia scatta una **risposta d'area**: rate limit temporaneo rafforzato, hold di massa, alert al Trust & Safety lead.
5. **Apprendimento e liste.** Bot/fonti fidate in allowlist (whitelist good bots — best practice 2026); attori confermati malevoli in denylist. Gli esiti alimentano l'audit e affinano le soglie.

### 4.4 Flusso D — Reputazione (motore della fiducia)

\`\`\`
Evento (contributo accettato/rifiutato, voto ricevuto, segnalazione confermata) →
Delta di reputazione (pesato) → Decadimento temporale → Livello/badge → Permessi/soglie
\`\`\`

1. **Eventi di reputazione.** Ogni esito rilevante genera un evento: contributo pubblicato (+), recensione votata utile (+), relazione confermata (+), contributo rifiutato come spam (−), segnalazione propria confermata (+), segnalazione propria infondata (−).
2. **Calcolo del delta.** Ogni evento ha un peso configurabile (\`ReputationPolicy\`). La reputazione è un'**aggregazione pesata**, non un semplice conteggio: gli eventi validati da curatori affidabili pesano di più (trust transitivo, in linea con la letteratura sui sistemi di reputazione).
3. **Decadimento temporale.** La reputazione decade lentamente nel tempo (half-life configurabile) per premiare l'attività recente ed evitare rendite di posizione.
4. **Livelli e badge.** Soglie di reputazione sbloccano livelli/badge (es. *Novizio → Contributore → Curatore di fiducia*) e capacità (auto-pubblicazione, voto a peso pieno, accesso alla coda di moderazione). Bilingui IT/EN.
5. **Effetti a valle.** La reputazione alimenta il peso dei voti (Flusso B), le soglie di auto-approvazione (Flusso A) e il gate anti-spam (Flusso C). È il connettore tra i quattro pilastri.

### 4.5 Flusso E — Curatela enterprise / approvazione

\`\`\`
Conoscenza candidata (estratta AI o contribuita) → Assegnazione owner →
Revisione/approvazione → Versioning → "Ufficiale" nel grafo → Audit di compliance
\`\`\`

1. **Candidatura.** Una relazione estratta dall'AI (es. \`DEPENDS_ON\` tra microservizi) o un contributo entra come **candidato** non ancora ufficiale.
2. **Owner e routing.** Il sistema instrada il candidato all'**owner** dell'area di conoscenza (per dominio/tipo di nodo), sfruttando reputazione/expertise interna.
3. **Approvazione/validazione.** L'owner conferma, modifica o rifiuta. La conferma di una relazione AI è esattamente il "feedback umano" che alza il peso dell'arco e lo promuove a fatto affidabile per l'impact analysis.
4. **Versioning e audit.** Ogni modifica produce una nuova versione con autore, timestamp, motivazione: storico completo per compliance (chi ha approvato cosa e quando).
5. **Pubblicazione governata.** Solo i candidati approvati sono \`OFFICIAL\` e usati con peso pieno dal GraphRAG enterprise; gli altri restano \`DRAFT\`/\`CANDIDATE\`, esclusi o pesati al ribasso.

### 4.6 Flusso trasversale — Appello e trasparenza

Ogni azione moderativa negativa è **appellabile**: l'utente apre un appello con motivazione; il caso torna in coda con priorità e (idealmente) a un moderatore diverso; la decisione finale è tracciata. La UI espone sempre lo **stato** del contributo, la **motivazione** delle azioni e la **spiegazione** del ranking, costruendo fiducia (best practice 2026: spiegazioni + audit trail + appelli leggeri).

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

### 5.1 Principio: il sociale è (anche) grafo

Community & moderazione non è solo tabelle relazionali: i suoi oggetti principali (utenti, contributi, valutazioni) sono **nodi di prima classe** del Knowledge Graph e si collegano ai nodi di dominio. Questo permette al GraphRAG di ragionare anche sulla provenienza e sull'affidabilità ("chi ha contribuito questo POI? con quale reputazione?") e abilita il rilevamento di sockpuppet/reciprocità come pattern di grafo. Si riusa il core del motore (documento 20), aggiungendo i tipi di questo ambito tramite il registry estendibile / plugin PF4J.

### 5.2 Tipi di nodo dell'ambito

| NodeType | Descrizione | Note |
|---|---|---|
| \`USER\` / \`CONTRIBUTOR\` | Utente/contributore con reputazione | Collegato a \`auth\`; porta lo score di reputazione |
| \`CONTRIBUTION\` | Atto di contribuzione (creazione/modifica) con stato | Unità di governance; collega autore→target |
| \`REVIEW\` | Recensione/testo libero su un target | Soggetto a classificazione e a voti |
| \`EVALUATION\`/\`VOTE\` | Valutazione pesata | Collega valutatore→target con valore e peso |
| \`REPORT\`/\`FLAG\` | Segnalazione | Alimenta la moderazione |
| \`MODERATION_CASE\` | Caso di moderazione | Stato, priorità, esito |
| \`MODERATION_ACTION\` | Azione moderativa (audit) | Immutabile, tracciabile |
| \`BADGE\`/\`REPUTATION_LEVEL\` | Livello/riconoscimento | Bilingue IT/EN |
| \`CURATOR\`/\`OWNER\` (enterprise) | Owner di area di conoscenza | Routing dei candidati |

### 5.3 Tipi di relazione dell'ambito

| RelationType | Semantica | Direzionata |
|---|---|---|
| \`CONTRIBUTED\` | Utente → contributo/nodo/arco | Sì |
| \`AUTHORED\` | Utente → recensione | Sì |
| \`EVALUATED\` | Utente → target (con valore/peso) | Sì |
| \`CONFIRMED\` / \`REJECTED\` | Utente → arco/relazione (conferma/smentita) | Sì |
| \`REPORTED\` | Utente → target (segnalazione) | Sì |
| \`MODERATED\` | Moderatore → caso/target (azione) | Sì |
| \`OWNS\` (enterprise) | Owner → area/tipo di nodo | Sì |
| \`TRUSTS\` / \`ENDORSES\` | Utente → utente (reputazione transitiva) | Sì |
| \`SUSPECTED_SOCKPUPPET\` | Utente ↔ utente (cluster anomalo, derivato) | No |

Le relazioni sociali (\`EVALUATED\`, \`CONFIRMED\`, \`TRUSTS\`) sono ciò che chiude il cerchio con il peso degli archi di dominio.

### 5.4 Criteri di peso — come il sociale pesa il grafo

Il sistema produce **due** famiglie di peso, entrambe in \`[0,1]\`, normalizzate e spiegabili:

**(a) Peso degli archi di dominio (contributo all'algoritmo del motore, documento 20 §5.4).** Il livello community alimenta in particolare:

| Fattore | Cosa misura | Da dove arriva |
|---|---|---|
| **Feedback utenti** | Validazione umana della relazione | Conferme/smentite (\`CONFIRMED\`/\`REJECTED\`) pesate dalla reputazione |
| **Consenso** | Accordo tra valutatori distinti | N. e diversità dei valutatori (anti-brigading) |
| **Affidabilità della fonte** | Chi ha contribuito | Reputazione del contributore/owner |
| **Recency** | Attualità | Decadimento temporale di voti e conferme |

**(b) Peso/score interni del sociale** (non sul grafo di dominio ma sugli oggetti dell'ambito):

| Score | Formula di riferimento | Uso |
|---|---|---|
| **Peso del voto** | \`w_vote = clamp01(g(reputazione_valutatore))\` | Aggregazione voti non manipolabile |
| **Score di qualità del target** | media pesata dei voti + diversità + freschezza | Ranking emergente |
| **Reputazione utente** | \`Σ (coeff_evento · delta_evento) · decadimento\`, con trust transitivo | Soglie, peso voto, gate anti-spam |
| **Rischio spam** | combinazione segnali identità/comportamento/contenuto/rete | Azione graduata |

Principio chiave (best practice e letteratura): **i voti pesano in funzione della reputazione del votante**, la reputazione è un'aggregazione pesata con decadimento, e ogni peso è **spiegabile** (si persistono i fattori, non solo il risultato) per mostrare in UI "perché". Implicazioni di schema: tabelle \`contributions\`, \`evaluations\` (con \`weight\` derivato), \`user_reputation\` (con fattori), \`moderation_cases\`, \`moderation_actions\` (audit immutabile); colonne per i fattori; migrazioni Flyway additive, **una query per file**.

### 5.5 Rappresentazione fisica (MySQL + Qdrant)

- **MySQL** = stato e governance: contributi, valutazioni, reputazione, casi e azioni di moderazione (audit), versioning. UUID con \`@JdbcTypeCode(SqlTypes.CHAR)\`. Attenzione alle parole riservate (\`timestamp\`) da escapare con backtick nelle DDL.
- **Qdrant** = similarità: embedding di recensioni/contributi testuali per **rilevamento duplicati e spam farm** (contributi quasi identici da account diversi) ed entity resolution dei contributi.
- **Audit immutabile**: le \`moderation_actions\` sono append-only (pattern immutabilità).

---

## 6. Fonti dati & connettori (ingestione)

Il livello community si alimenta sia da input umani diretti sia da segnali di sistema; i connettori sono punti naturali di estensione PF4J.

| Fonte | Connettore | Riuso esistente | Output |
|---|---|---|---|
| **Contributi/voti/segnalazioni UI** | API CRUD community | Frontend Angular + \`auth\` | Nodi \`CONTRIBUTION\`/\`REVIEW\`/\`VOTE\`/\`REPORT\`, archi sociali |
| **Eventi di sistema** | Listener su \`DomainEventPublisherPort\` | Event infra esistente | Frequenza d'uso, trigger reputazione |
| **Classificatore contenuti** | Adapter di moderazione AI | \`LlmGatewayService\` (Ollama) | \`aiAssessment\` su contributi |
| **Similarità contributi** | Adapter Qdrant | \`QdrantVectorStoreAdapter\` | Rilevamento duplicati/spam farm |
| **Identità/ruoli** | Integrazione \`auth\` | RBAC esistente esteso | Segnali identità anti-spam, permessi |
| **Moderazione esterna (opt-in)** | Plugin PF4J (denylist, blocklist domini) | Marketplace | Liste, segnali aggiuntivi |
| **Conoscenza candidata (enterprise)** | Estrazione AI del motore a grafo | \`LlmEntityExtractorAdapter\` | Candidati → coda di approvazione owner |

Principi: ogni contributo è **tracciabile** (autore + timestamp + sorgente), il flusso è **idempotente** (un voto = un record aggiornabile), le fonti di moderazione cloud sono **opt-in** (privacy, local-first). I connettori si registrano come adapter dei \`port/out\`, mantenendo il dominio puro.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

Legenda: **[C]** creare ex-novo, **[E]** estendere/evolvere l'esistente, **[M]** mantenere/hardening.

### 7.1 MVP (fondamenta indispensabili)

| # | Funzionalità | Tipo | Note di implementazione |
|---|---|---|---|
| 1 | **Modello Contributo + stati** | [C] | Dominio \`community\` (puro, wired in \`DomainConfig\`): \`Contribution\`, stati, \`port/in\`/\`port/out\` |
| 2 | **Workflow di moderazione configurabile** | [C] | Pre/post/reactive per dominio (\`ModerationPolicy\`); soglie auto-approvazione |
| 3 | **Valutazioni (voto/stelle/conferma)** | [C] | \`Evaluation\` con un voto attivo per (utente,target,tipo), no auto-voto |
| 4 | **Reputazione base + voto pesato** | [C] | \`ReputationPolicy\`, delta per evento, peso voto = f(reputazione) |
| 5 | **Ranking emergente** | [C] | Score di qualità pesato; ordinamento + spiegazione |
| 6 | **Segnalazioni + coda di moderazione** | [C] | \`Report\`, \`ModerationCase\`, priorità, stati |
| 7 | **Moderazione AI locale (prima linea)** | [E] | Adapter classificatore su \`LlmGatewayService\` (Ollama), confidence + motivazione |
| 8 | **Anti-spam base (rate limit + identità)** | [C] | Rate limit per ruolo/azione; gate su account nuovi/non verificati |
| 9 | **Audit trail immutabile** | [C] | \`ModerationAction\` append-only; nota obbligatoria su azioni punitive |
| 10 | **Integrazione con peso archi del grafo** | [E] | Conferme/smentite → fattore feedback (documento 20) via evento |
| 11 | **RBAC esteso (ruoli community)** | [E] | Contribuire/votare/moderare/approvare su \`auth\` |
| 12 | **REST API + DTO bilingui** | [C] | \`/api/v1/community/*\`: contributi, voti, segnalazioni, casi; enum IT/EN |
| 13 | **UI: contributo, voto, segnalazione, coda moderazione** | [C] | Feature Angular \`community\`/\`moderation\`, Signals, i18n |
| 14 | **Migrazioni Flyway** | [C] | Tabelle community/moderazione/reputazione; una query per file |

### 7.2 Evoluzione (post-MVP)

| # | Funzionalità | Tipo | Valore |
|---|---|---|---|
| 15 | **Rilevamento duplicati/spam farm (Qdrant)** | [E] | Similarità contributi → spam farm e fake review |
| 16 | **Spam-wave / attacchi coordinati** | [C] | Finestra temporale, risposta d'area, alert T&S |
| 17 | **Rilevamento sockpuppet/reciprocità (grafo)** | [C] | Cluster anomali di voto come pattern di grafo |
| 18 | **Reputazione transitiva + decadimento** | [E] | Trust pesato dalla reputazione del validatore; half-life |
| 19 | **Badge, livelli, gamification** | [C] | Engagement; sblocco capacità; bilingue |
| 20 | **Flusso di appello** | [C] | Trasparenza, moderatore diverso, audit |
| 21 | **Curatela enterprise / approvazione owner** | [C] | Candidati → owner → \`OFFICIAL\`; versioning |
| 22 | **Versioning/storico contributi (stile wiki)** | [C] | Edit suggeriti, rollback, compliance |
| 23 | **Dashboard moderazione + reportistica** | [C] | KPI, code, trend, esiti AI vs umani |
| 24 | **Connettori moderazione/liste (PF4J)** | [C] | Denylist domini, blocklist, integrazioni opt-in |
| 25 | **Notifiche multi-canale** | [E] | Esiti/appelli via dominio \`messaging\`/\`email\` |

### 7.3 Mantenimento continuo

- **[M]** Tuning continuo di soglie anti-spam e coefficienti reputazione sulla base delle metriche (falsi positivi/negativi).
- **[M]** Qualità della moderazione AI: monitorare l'accordo AI↔umano, ricalibrare i prompt/modelli Ollama.
- **[M]** Coerenza dei cataloghi enum IT/EN al variare dei domini/policy.
- **[M]** Performance di coda e ranking al crescere dei volumi (indici, caching dei ranking caldi via Caffeine già presente).
- **[M]** Salute della reputazione: prevenire inflazione/rendite, verificare il decadimento.
- **[M]** Test: unit (dominio puro), integrazione (Testcontainers MySQL), copertura ≥80%; documentazione IT/EN + log in \`Sviluppi/\`.

---

## 8. Casi d'uso AI / GraphRAG

| Caso d'uso | Dominio | Ruolo dell'AI / del grafo | Output |
|---|---|---|---|
| **Classificazione contenuti in prima linea** | Trasversale | Ollama classifica contributi (spam/offensivo/privacy) con confidence | Auto-azione o priorità in coda + motivazione |
| **Rilevamento duplicati / fake review** | Consumer | Similarità Qdrant tra recensioni/contributi | Cluster sospetti → moderazione |
| **Sockpuppet / brigading come pattern di grafo** | Trasversale | Traversal su \`EVALUATED\`/\`TRUSTS\` per cluster reciproci | Voti scontati + alert |
| **Riassunto della coda di moderazione** | Trasversale | LLM riassume e prioritizza i casi aperti | Triage assistito per il moderatore |
| **Spiegazione del ranking** | Consumer | GraphRAG cita i fattori (consenso, reputazione, freschezza) | "In cima perché…" trasparente |
| **Suggerimento di azione moderativa** | Trasversale | LLM propone decisione + policy applicabile | Bozza di decisione (umano conferma) |
| **Validazione relazioni AI (enterprise)** | Enterprise | Owner conferma archi candidati → peso pieno | Conoscenza ufficiale tracciata |
| **Affidabilità nelle risposte GraphRAG** | Trasversale | Il grafo include provenienza e reputazione | Risposte che pesano fonti affidabili e citano contributori |

Tutti i casi girano con **Ollama di default**, producono esiti **spiegabili e auditabili**, e mantengono **l'umano nel loop** sulle decisioni delicate (moderazione ibrida, best practice 2026).

---

## 9. KPI & metriche di successo

| Categoria | KPI | Obiettivo indicativo |
|---|---|---|
| **Partecipazione** | N. contributori attivi, contributi/utente, tasso di ritorno | Crescita costante della community |
| **Qualità contributi** | % contributi pubblicati vs rifiutati, % fake review intercettate | Alta accettazione di contributi genuini |
| **Efficacia moderazione** | Tempo medio in coda, % casi risolti, accordo AI↔umano | Coda smaltita rapidamente; alto accordo |
| **Accuratezza AI** | Falsi positivi/negativi del classificatore | FP bassi (no censura ingiusta), FN bassi (no spam) |
| **Anti-spam** | % spam bloccato, spam-wave intercettate, spam pubblicato (leakage) | Leakage minimo, attacchi coordinati neutralizzati |
| **Equità del ranking** | Resistenza a brigading (test), correlazione score↔qualità reale | Ranking robusto alla manipolazione |
| **Reputazione** | Distribuzione reputazione, mobilità (nuovi che salgono) | Nessuna rendita di posizione; meritocrazia |
| **Trasparenza** | % azioni con motivazione, tasso di appelli, % appelli accolti | ~100% motivate; appelli gestiti equamente |
| **Impatto sul grafo** | % archi con feedback umano, miglioramento qualità GraphRAG | Pesi più affidabili, risposte migliori |
| **Local-first / privacy** | % moderazione eseguita in locale; dati inviati a cloud senza consenso | 100% locale possibile; 0 invii non consentiti |

### 9.1 Strumentazione

Riuso di Actuator + Micrometer/Prometheus per latenza di coda, throughput di moderazione, tassi AI, hit/miss caching dei ranking; dashboard Grafana (già presente nel profilo monitoring).

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---|---|---|
| **Spam farm / fake review** | Grafo inquinato, ranking falsato | Similarità Qdrant, voto pesato da reputazione, rate limit, spam-wave detection |
| **Manipolazione del ranking (brigading, reciprocità)** | Contenuti scadenti in cima | Voto pesato, diversità dei valutatori, rilevamento cluster reciproci, scontatura voti sospetti |
| **Sockpuppet (account multipli)** | Reputazione e voti gonfiati | Segnali identità, verifica, pattern di grafo, soglie per account nuovi |
| **Falsi positivi della moderazione AI** | Censura ingiusta, perdita fiducia | Umano nel loop sulle zone grigie, appelli, calibrazione soglie, motivazioni |
| **Falsi negativi (spam passa)** | Degrado qualità | Post-moderazione + segnalazioni community + tuning continuo |
| **Sovraccarico dei moderatori** | Coda ingestibile | Triage AI, priorità, azioni bulk, auto-azioni ad alta confidence |
| **Inflazione/rendite di reputazione** | Meritocrazia falsata | Decadimento temporale, aggregazione pesata, revisione coefficienti |
| **Contenuti illeciti / dati personali** | Responsabilità legale | Classificazione PRIVACY/illecito, rimozione rapida, audit, guidelines chiare |
| **Bias culturale/linguistico del classificatore** | Moderazione iniqua IT vs EN | Modelli/prompt per lingua, fluenza culturale, revisione umana |
| **Dipendenza da cloud T&S** | Violazione local-first/privacy | Moderazione Ollama locale di default; cloud opt-in |
| **Opacità delle decisioni** | Perdita di fiducia | Spiegazioni, audit trail, appelli leggeri (best practice 2026) |
| **Migrazioni Flyway monolitiche** | Violazione regola di progetto | Una query per file, sempre |

---

## 11. Manutenzione & evoluzione

- **Tuning data-driven:** soglie anti-spam, coefficienti di reputazione e prompt di moderazione vanno rivisti periodicamente sulla base di FP/FN e dell'accordo AI↔umano. La moderazione è un sistema vivo, non statico.
- **Evoluzione delle policy:** ogni dominio/istanza può evolvere \`ModerationPolicy\`, \`ReputationPolicy\`, \`AntiSpamPolicy\` senza toccare il core; le enum restano bilingui e retrocompatibili.
- **Governance della reputazione:** calibrare il decadimento per dominio, monitorare la distribuzione per evitare oligarchie di curatori; introdurre revisione tra pari per i livelli alti.
- **Estensibilità via plugin:** classificatori, liste, connettori di moderazione e nuovi tipi di valutazione vivono come estensioni PF4J e moduli marketplace, testati in isolamento.
- **Coerenza con il motore a grafo:** mantenere allineati i fattori di peso "feedback/consenso/affidabilità" con l'algoritmo del documento 20; trattare gli score interni come derivati ricalcolabili.
- **Audit e compliance:** le azioni di moderazione e il versioning enterprise sono append-only; conservare e rendere interrogabile lo storico.
- **Test e copertura:** unit sul dominio puro, integrazione con Testcontainers MySQL, test di robustezza del ranking (simulazioni di brigading); copertura ≥80%.
- **Documentazione:** aggiornamento costante IT/EN ad ogni sviluppo, guidelines pubbliche bilingui, log datato in \`Sviluppi/\` (regole di progetto).
- **Scalabilità:** se la moderazione locale diventa collo di bottiglia su hardware modesto, valutare batch asincrono, modelli Ollama più leggeri e caching aggressivo prima di considerare opzioni cloud opt-in.

---

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo / dominio | Ruolo rispetto all'ambito | Integrazione concreta |
|---|---|---|
| **\`knowledge\` (Motore Graph)** | Substrato dei contenuti curati | Contributi creano/modificano nodi/archi; conferme alimentano il peso (documento 20) |
| **Nuovo dominio \`community\`** | Cuore dell'ambito (da creare) | Modelli, \`port/in\`/\`out\`, service puri wired in \`DomainConfig\` |
| **\`auth\`** | Identità, ruoli, permessi, segnali identità | RBAC esteso (contribuire/votare/moderare/approvare); età/verifica account |
| **\`llm\` / \`LlmGatewayService\`** | Moderazione AI in prima linea | Classificatore contenuti con Ollama default + fallback opt-in |
| **Qdrant (\`vectorstore\`)** | Similarità contributi | Rilevamento duplicati, spam farm, entity resolution recensioni |
| **MySQL + Flyway** | Stato, reputazione, audit | Tabelle community/moderazione/reputazione; migrazioni additive una-query |
| **Event infra (\`DomainEventPublisherPort\`)** | Propagazione esiti | Conferme→peso archi, contributi→reputazione, statistiche |
| **\`messaging\` / \`email\`** | Notifiche | Esiti moderazione, appelli, badge agli utenti |
| **\`agent\`** | Moderazione/triage assistito | Agente che riassume la coda e propone azioni |
| **\`mcp\`** | Tool e federazione | Liste/classificatori esterni opt-in come tool |
| **\`automation\`** | Regole e trigger | Auto-azioni, escalation, risposta a spam-wave |
| **\`marketplace\` / \`plugin\` (PF4J)** | Estensibilità | Classificatori, liste, connettori, tipi di valutazione installabili |
| **\`common\`** | Eventi, eccezioni, analytics | Eventi di moderazione/reputazione, eccezioni tipizzate, statistiche |
| **\`finetuning\`** | Miglioramento moderazione | Dataset di casi (con esito umano) per affinare il classificatore locale |
| **Frontend Angular** | UI sociale e di moderazione | Feature \`community\`/\`moderation\`: contributo, voto, segnalazione, coda, dashboard, appello; Signals; i18n IT/EN |

**Wiring architetturale:** il nuovo dominio \`community\` (e i servizi di moderazione/reputazione) restano **puri** (zero Spring) e sono registrati come \`@Bean\` in \`DomainConfig.java\`; gli adapter (MySQL, Qdrant, classificatore LLM, liste) sono \`@Component\` che implementano i \`port/out\`; i controller espongono solo i \`port/in\`. Questo preserva l'architettura esagonale e abilita la futura estrazione a microservizio documentata in \`MODULE_BOUNDARIES.md\`.

---

*Documento di indirizzo per gli sviluppi dell'ambito Community & moderazione (gruppo core). Da mantenere allineato a \`.planning/PROJECT.md\`, ai file in \`.planning/codebase/\` e al documento 20 (Motore Knowledge Graph).*
`;
