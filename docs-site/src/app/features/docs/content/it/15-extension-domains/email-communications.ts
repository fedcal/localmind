export const content = `# Mail & comunicazioni

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo ambito appartiene al **gruppo enterprise** del motore di knowledge graph universale di LocalMind. Mentre i verticali consumer (turismo, eventi, esperienze) rispondono alla domanda *"cosa posso fare qui?"*, l'ambito **Mail & comunicazioni** risponde a una delle domande più costose nella vita di un'organizzazione: *"chi sa cosa, chi ha deciso cosa, con chi, quando e perché?"*. La posta elettronica e i canali di comunicazione aziendali sono il **giacimento di conoscenza tacita** più ricco e meno strutturato di qualsiasi impresa: contengono decisioni, impegni, scadenze, relazioni tra persone, contesto di progetti, allegati operativi e la memoria storica di "come si è arrivati qui". Tutto questo oggi è prigioniero di caselle individuali, thread interminabili e ricerche per parola chiave che non capiscono le relazioni.

LocalMind trasforma questo flusso in un **grafo pesato di comunicazioni-persone-temi**, navigabile dall'AI in modalità GraphRAG, **senza che una sola mail lasci l'istanza self-hosted**. Questo è il punto differenziante rispetto a Gmail, Outlook/Copilot, Superhuman o ai vari "AI email assistant" cloud: la conoscenza estratta dalle comunicazioni — per definizione il dato più sensibile e regolamentato di un'azienda — resta sovrana, on-premise, con AI Ollama in locale come default. L'ingestione avviene via **IMAP** (lo standard universale, già presente nel codebase tramite Eclipse Angus Mail nel dominio \`email\`), riusando MySQL per la struttura del grafo e Qdrant per la semantica, senza introdurre alcun database a grafo dedicato.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema concreto

La posta elettronica resta, nel 2026, il sistema operativo *de facto* della collaborazione aziendale — soprattutto verso l'esterno (clienti, fornitori, consulenti, PA) dove le chat interne non arrivano. Eppure è uno strumento progettato negli anni '80 per il recapito di messaggi, non per la gestione della conoscenza. Ne derivano problemi cronici e quantificabili:

- **Conoscenza sepolta nelle caselle individuali (silos).** La memoria di una trattativa, di una decisione architetturale, di un accordo con un fornitore o della storia di un cliente vive nella casella di una o due persone. Quando quella persona è assente, cambia ruolo o lascia l'azienda, la conoscenza svanisce. Non esiste una vista d'insieme: ogni casella è un'isola.
- **Ricerca che non capisce le relazioni.** La ricerca nativa dei client di posta è basata su parole chiave e mittente/oggetto. Non risponde a domande del tipo *"qual è lo stato di tutti gli impegni che abbiamo preso col cliente Rossi sul progetto Alfa?"*, *"chi è la persona giusta da contattare per la fatturazione del fornitore Beta?"*, *"quali decisioni sono state prese via mail sul rinnovo del contratto e da chi?"*. Questi sono problemi di **multi-hop reasoning** su un grafo, non di full-text search — e proprio su questo il GraphRAG mostra salti di accuratezza misurati (in letteratura, dal ~23% a ~87% su task multi-hop rispetto al RAG tradizionale).
- **Sovraccarico informativo (email overload).** Il knowledge worker medio dedica ore al giorno a leggere, smistare, cercare e ricostruire il contesto delle email. Gran parte di questo lavoro è ricostruzione manuale di un contesto che il sistema potrebbe fornire automaticamente.
- **Thread frammentati e illeggibili.** Le conversazioni si spezzano su più thread, con fork, cc che entrano ed escono, citazioni annidate, risposte fuori ordine. Ricostruire "cosa è successo davvero" richiede di rileggere decine di messaggi e quoting.
- **Impegni e scadenze persi.** Promesse ("ti mando il preventivo entro venerdì"), richieste, action item e date concordate restano nel corpo del testo, mai estratti in modo strutturato, mai monitorati. Si traducono in palle perse e clienti scontenti.
- **Allegati come buco nero.** Contratti, offerte, fatture, specifiche, verbali viaggiano come allegati. La versione "buona" di un documento è "quella che mi ha mandato Tizio in quella mail di marzo" — introvabile e scollegata dal resto della knowledge base aziendale.
- **Onboarding e continuità operativa lenti.** Un nuovo assunto o un sostituto non ha modo di assorbire rapidamente la storia delle relazioni e dei dossier. Il "passaggio di consegne" è un atto manuale, parziale e soggettivo.
- **Mappa delle relazioni invisibile.** Nessuno ha una vista oggettiva di *chi comunica con chi*, *con quale intensità*, *su quali temi* — informazione preziosa per capire chi presidia un cliente, dove ci sono single point of failure umani, quali team collaborano davvero.

### 1.2 La soluzione LocalMind

LocalMind ingerisce le caselle aziendali via IMAP e costruisce un **grafo pesato di conoscenza** in cui i nodi sono **persone**, **messaggi**, **thread/conversazioni**, **temi/argomenti**, **organizzazioni**, **allegati/documenti**, **impegni/scadenze** e **decisioni**, e gli archi rappresentano relazioni pesate: una persona *ha scritto* un messaggio, un messaggio *appartiene a* un thread, un thread *tratta* un tema, due persone *comunicano tra loro* con una certa intensità, un messaggio *contiene* un impegno con una scadenza, un allegato *è citato in* più thread. Su questo grafo opera l'AI in modalità **GraphRAG**, combinando la ricerca semantica (Qdrant, già usato per i documenti) con la navigazione delle relazioni (MySQL), per rispondere a domande complesse e far emergere collegamenti non evidenti — *citando i messaggi, i thread e le persone* usati come fonte.

Il valore differenziale rispetto agli strumenti esistenti:

| Capacità | Client di posta / ricerca nativa | Assistenti email cloud (Copilot, Gemini, Superhuman) | LocalMind (grafo + GraphRAG) |
|----------|----------------------------------|------------------------------------------------------|------------------------------|
| Modello dei dati | Messaggi isolati | Messaggi + riassunti | Grafo persone-comunicazioni-temi pesato |
| Domande multi-hop | No | Limitate al singolo account | Sì, attraversando persone/thread/temi/allegati |
| Estrazione impegni/decisioni | No | Parziale | Nodi strutturati monitorabili |
| Allegati come conoscenza | Scollegati | Scollegati | Nodi collegati alla KB documentale |
| Spiegabilità delle risposte | — | Spesso opaca | Citazione di messaggi/thread/percorsi |
| Sovranità del dato | Dipende dal provider | **Dati inviati al cloud del vendor** | **Local-first, AI Ollama in locale** |
| Privacy/compliance | Demandata al provider | Critica per GDPR | Controllo totale on-premise |

### 1.3 Il valore per gli stakeholder

- **Per il knowledge worker / account manager:** un "secondo cervello" che ricorda ogni interazione con ogni cliente/fornitore, prepara il contesto prima di una call, segnala impegni in scadenza e risponde a "qual è lo stato di…" in linguaggio naturale.
- **Per i team e i manager:** vista d'insieme dei dossier (clienti, fornitori, progetti) indipendente dalle singole caselle; continuità operativa quando una persona è assente; capacità di capire dove la conoscenza è concentrata su poche persone (rischio di bus factor).
- **Per l'azienda:** la conoscenza tacita diventa un asset organizzativo persistente e interrogabile, non più legata ai singoli; onboarding più rapido; minor rischio di impegni dimenticati; memoria storica delle decisioni.
- **Per il responsabile IT/sicurezza e il DPO:** una soluzione che, a differenza dei concorrenti cloud, **non esfiltra dati**; l'intera elaborazione (estrazione entità, embedding, inferenza LLM) avviene su infrastruttura controllata, con strumenti nativi di minimizzazione, retention e diritto all'oblio — fondamentale per GDPR e per dati coperti da segreto.

### 1.4 Confini di responsabilità (cosa NON è)

Per evitare ambiguità, rischi normativi e derive d'uso, l'ambito si definisce anche per esclusione:

- **Non è un client di posta sostitutivo.** LocalMind non rimpiazza Outlook/Thunderbird/webmail per scrivere e gestire la posta quotidiana; è un layer di conoscenza in *lettura* (con eventuale composizione assistita di bozze, mai invio automatico senza conferma).
- **Non è uno strumento di sorveglianza dei dipendenti.** Il grafo delle comunicazioni serve a recuperare conoscenza, non a monitorare le persone. La governance (sezione 10) impone perimetri, consensi e finalità esplicite; il monitoraggio individuale a fini disciplinari è fuori scope e va escluso per policy.
- **Non invia mail in autonomia.** Qualsiasi azione in uscita (risposta, inoltro) richiede conferma umana esplicita; l'MVP è in sola lettura/ingestione.
- **Non è un sistema di archiviazione legale a valore probatorio.** Può conservare e citare messaggi, ma non sostituisce sistemi di conservazione a norma / journaling certificato.

---

## 2. Personas & utenti target

| Persona | Descrizione | Obiettivo primario | Esigenza chiave |
|---------|-------------|--------------------|-----------------|
| **Account / sales manager** | Gestisce relazioni con clienti via mail | Avere sempre il contesto completo del cliente | Vista per persona/organizzazione, impegni aperti |
| **Project manager** | Coordina progetti che vivono molto via mail | Stato di decisioni, action item, blocchi | Timeline del progetto, scadenze, responsabili |
| **Responsabile acquisti / fornitori** | Comunica con fornitori e PA | Storia ordini, offerte, contratti | Dossier fornitore, allegati contrattuali collegati |
| **Founder / dirigente** | Molte relazioni, poco tempo | Prepararsi a call e decisioni rapidamente | Briefing sintetico con citazioni |
| **Knowledge worker generico** | Subisce l'email overload | Trovare velocemente "quella mail/decisione" | Ricerca conversazionale multi-hop |
| **Nuovo assunto / sostituto** | Deve assorbire dossier e relazioni | Onboarding rapido su un cliente/progetto | Riassunti storici, mappa delle persone |
| **Assistente / segreteria** | Gestisce caselle condivise | Smistare, ricostruire contesto, bozze | Caselle condivise, impegni, deleghe |
| **Responsabile IT / amministratore istanza** | Configura ingestione e accessi | Self-hosting, connettori IMAP, sicurezza | Configurazione account, perimetri, audit |
| **DPO / responsabile compliance** | Vigila su privacy e finalità | Minimizzazione, retention, oblio | Controlli granulari, log, consensi |

Distinzione operativa che guida i ruoli del dominio \`auth\`:
- **Utenti finali** interrogano il grafo in lettura, **limitati al perimetro a cui hanno diritto** (la propria casella, le caselle/dossier condivisi loro assegnati). Il rispetto del perimetro di visibilità è un requisito di sicurezza, non un'opzione.
- **Amministratore dell'istanza** configura account IMAP, regole di ingestione, perimetri e retention.
- **DPO/compliance** ha accesso ai log di audit, alle policy di minimizzazione e agli strumenti di cancellazione, ma non necessariamente al contenuto.

---

## 3. Requisiti in input

Questa sezione definisce in dettaglio **cosa deve entrare nel sistema** affinché il grafo delle comunicazioni sia utile, accurato, sicuro e manutenibile. Si distinguono input di **connessione/configurazione**, input di **dominio** (i dati grezzi delle mail e ciò che se ne estrae), input **dell'utente finale** (la richiesta) e input di **governance/privacy**. Data la natura estremamente sensibile del dato, i requisiti di privacy non sono un'appendice ma un input di primo livello.

### 3.1 Input di connessione e configurazione (account IMAP)

Per ogni casella da ingerire l'amministratore deve poter fornire:

| Categoria | Campi minimi (MVP) | Campi estesi (evoluzione) |
|-----------|--------------------|---------------------------|
| Connessione | host IMAP, porta, TLS/STARTTLS, username | profilo provider (Gmail/Microsoft 365/generico), timeout |
| Autenticazione | password applicativa / credenziale | **OAuth2/XOAUTH2** (Google, Microsoft), token refresh |
| Identità casella | indirizzo email, etichetta descrittiva, proprietario/i | tipo (personale, condivisa, funzionale info@/sales@) |
| Perimetro ingestione | cartelle da includere/escludere (es. solo INBOX+Sent), data di partenza | filtri per mittente/dominio, esclusione liste/newsletter |
| Pianificazione | frequenza di sincronizzazione | finestra oraria, modalità incrementale vs full |
| Privacy di default | livello di redazione PII, consenso al trattamento | regole per-casella, esclusione domini sensibili |

Requisiti tecnici chiave dell'ingestione IMAP:
- **Credenziali mai in chiaro nel DB**: cifratura at-rest, preferibilmente con supporto a OAuth2 per i provider principali (così non si gestiscono password).
- **Sincronizzazione incrementale e idempotente** basata su \`UID\`/\`UIDVALIDITY\` per cartella, così da non riprocessare l'intera casella a ogni run e da gestire correttamente spostamenti/cancellazioni.
- **Resilienza**: gestione di disconnessioni, rate limit del server, caselle molto grandi (centinaia di migliaia di messaggi) tramite paginazione e ripresa da checkpoint.

### 3.2 Input di dominio (la singola email e la sua decomposizione)

Ogni messaggio IMAP viene decomposto nei suoi elementi RFC 5322/MIME e arricchito. Il modello \`EmailMessage\` oggi presente nel codebase (\`id, from, to, subject, body, receivedAt, read\`) è il punto di partenza minimo e va **esteso** per alimentare il grafo:

| Elemento | Campi minimi (MVP) | Campi estesi (evoluzione) |
|----------|--------------------|---------------------------|
| Header di identità | \`Message-ID\`, \`In-Reply-To\`, \`References\` (per il threading), \`Date\` | \`Return-Path\`, header di autenticazione (SPF/DKIM) |
| Partecipanti | \`From\`, \`To\`, \`Cc\` | \`Bcc\` (solo se in Sent), \`Reply-To\`, lista distribuzione |
| Contenuto | oggetto, corpo testuale (de-quotato), lingua rilevata | corpo HTML normalizzato, firma separata dal contenuto |
| Allegati | nome file, MIME type, dimensione, hash | testo estratto (Tika/OCR), versioning, anteprima |
| Metadati casella | cartella, flag letto, direzione (in/out) | etichette, importanza, thread ID nativo |
| Arricchimenti AI | entità (persone/org/temi), riassunto messaggio | sentiment/tono, lingua, classificazione (richiesta/FYI/decisione) |
| Estrazioni strutturate | impegni/action item con scadenza, domande aperte | decisioni prese, riferimenti a documenti/ticket |

Elaborazioni necessarie sul corpo del messaggio prima dell'inserimento nel grafo:
- **De-quoting / scorporo della citazione**: separare il testo *nuovo* dal testo citato (le \`>\` annidate, i blocchi "On … wrote:"), così da non duplicare contenuto e da attribuire correttamente l'autore di ogni frammento.
- **Rimozione di firme e disclaimer** ripetitivi (rumore che inquina embedding ed estrazione).
- **Normalizzazione HTML→testo** e gestione di mail multipart.
- **Rilevamento lingua** (IT/EN e oltre) per indirizzare correttamente prompt ed embedding.

### 3.3 Input dell'utente finale (la richiesta conversazionale)

L'utente interroga il grafo in linguaggio naturale. Il sistema deve accettare e gestire:
- **Domande di recupero**: "trova la mail in cui il cliente Rossi ci ha confermato il prezzo".
- **Domande di sintesi/stato**: "riassumimi tutto quello che è successo col progetto Alfa nelle ultime due settimane".
- **Domande relazionali (multi-hop)**: "chi, oltre a me, è in contatto con il fornitore Beta e su quali temi?".
- **Domande su impegni/scadenze**: "quali promesse ho fatto via mail e non ho ancora mantenuto?".
- **Richieste di preparazione**: "preparami un briefing prima della call con Gamma di domani".
- **Richieste di composizione assistita** (evoluzione): "scrivi una bozza di risposta a quest'ultima mail tenendo conto dello storico" — sempre con conferma umana, mai invio automatico.

Ogni richiesta porta con sé un **contesto di autorizzazione** (chi sta chiedendo, su quale perimetro ha diritto): è un input obbligatorio che filtra a monte i nodi accessibili.

### 3.4 Input di governance e privacy (di primo livello)

Trattando dati personali e potenzialmente sensibili, sono input obbligatori:
- **Consenso e finalità**: per quali caselle/persone è attiva l'ingestione e con quale finalità dichiarata.
- **Politiche di minimizzazione/redazione**: quali categorie di PII redarre prima dell'eventuale invio a un LLM (specie se cloud), quali domini/mittenti escludere a priori (es. comunicazioni sindacali, sanitarie, personali).
- **Retention e oblio**: per quanto tempo conservare messaggi e arricchimenti, regole di cancellazione automatica, gestione di richieste di cancellazione su una persona/indirizzo.
- **Perimetri di visibilità**: mappatura utente → caselle/dossier accessibili.
- **Policy LLM**: se è ammesso l'uso di provider cloud per questo dominio o se è imposto Ollama-only (default consigliato per le comunicazioni).

---

## 4. Flusso dell'attività (step-by-step)

Il flusso si articola in due grandi pipeline: la **pipeline di ingestione** (batch/asincrona, da casella a grafo) e la **pipeline di interrogazione** (sincrona, da domanda a risposta GraphRAG). Entrambe riusano componenti già presenti in LocalMind.

### 4.1 Pipeline di ingestione (da IMAP al grafo)

**Step 0 — Configurazione e consenso.** L'amministratore registra l'account IMAP (sezione 3.1), definisce perimetro, pianificazione e policy di privacy. Le credenziali vengono cifrate at-rest; viene registrata la finalità del trattamento.

**Step 1 — Connessione e scoperta incrementale.** Lo scheduler (riuso del pattern \`localmind-batch\` + \`@EnableScheduling\`) avvia un job che si connette via IMAP (Angus Mail), legge \`UIDVALIDITY\` e l'ultimo \`UID\` processato per cartella e scarica **solo i messaggi nuovi/modificati** (sincronizzazione incrementale idempotente). Disconnessioni e rate limit sono gestiti con ripresa da checkpoint.

**Step 2 — Parsing e decomposizione MIME.** Ogni messaggio viene decomposto: header (incluso \`Message-ID\`, \`In-Reply-To\`, \`References\`), partecipanti, corpo (multipart), allegati. Si normalizza l'HTML in testo, si rileva la lingua.

**Step 3 — Pulizia del contenuto.** De-quoting della citazione, rimozione di firme/disclaimer, scorporo del testo nuovo da quello citato. Questo passo è critico per la qualità di embedding ed estrazione.

**Step 4 — Estrazione allegati.** Ogni allegato viene salvato, hashato (deduplica), e il suo testo estratto **riusando la pipeline documentale esistente** (\`DocumentIngestionPipelineService\`, Tika, OCR Tesseract). L'allegato diventa così un **documento di prima classe** nella KB, collegato al messaggio e al thread.

**Step 5 — Arricchimento AI (locale di default).** Tramite il \`LlmGatewayService\` con provider **Ollama** in locale (rispettando la policy LLM del dominio):
- **NER / estrazione entità**: persone, organizzazioni, temi/argomenti, progetti, prodotti, luoghi.
- **Riassunto** del messaggio e, a livello superiore, del thread.
- **Estrazione strutturata** di impegni/action item con scadenza, domande aperte, decisioni prese, classificazione del messaggio (richiesta / FYI / decisione / sollecito).
- Eventuale tono/sentiment per le evoluzioni.

**Step 6 — Risoluzione delle entità (entity resolution).** Passo determinante per la qualità del grafo: lo stesso individuo può comparire come \`mario.rossi@acme.com\`, \`m.rossi@acme.com\`, "Mario Rossi", "Mario" in firma. Lo stesso vale per le organizzazioni (per dominio email). Il sistema riconcilia gli alias in un unico nodo **Persona**/**Organizzazione** (matching su indirizzo, dominio, display name, firma). In letteratura questa fase migliora sensibilmente l'accuratezza delle query (riferimenti di settore parlano di +34%): è un investimento, non un dettaglio.

**Step 7 — Ricostruzione del thread.** I messaggi vengono collegati in **conversazioni** usando \`In-Reply-To\`/\`References\` (e, come fallback, normalizzazione dell'oggetto + vicinanza temporale + insieme dei partecipanti). Si gestiscono fork, merge e risposte fuori ordine, producendo un albero/DAG della conversazione.

**Step 8 — Costruzione/aggiornamento del grafo.** Si creano/aggiornano i nodi (Persona, Messaggio, Thread, Tema, Organizzazione, Allegato, Impegno, Decisione) e gli archi pesati (sezione 5) in MySQL; i contenuti testuali (corpo de-quotato, riassunti, testo allegati) vengono **chunkati ed embeddati su Qdrant** per la ricerca semantica. I pesi degli archi vengono calcolati/aggiornati (frequenza, recenza, rilevanza).

**Step 9 — Applicazione delle policy di privacy.** Prima della persistenza, si applicano redazione PII (ove previsto), esclusione domini/mittenti sensibili e tagging dei livelli di confidenzialità. Si registra l'audit dell'ingestione.

**Step 10 — Indicizzazione e disponibilità.** Il grafo aggiornato è ora interrogabile. Viene emesso un evento di dominio (\`DomainEventPublisherPort\`) per side-effect (aggiornamento dashboard, notifiche di nuovi impegni/scadenze).

\`\`\`text
[IMAP account]
   │  (incrementale, UID/UIDVALIDITY)
   ▼
[Fetch & parse MIME] → [De-quote/clean] → [Allegati → pipeline documentale (Tika/OCR → Qdrant)]
   │                                                  │
   ▼                                                  ▼
[Arricchimento AI Ollama: NER, riassunti,      [Entity resolution persone/org]
 impegni, decisioni, classificazione]                │
   │                                                  ▼
   └──────────────► [Ricostruzione thread] ──► [Costruzione grafo: nodi+archi pesati]
                                                      │  (MySQL struttura + Qdrant semantica)
                                                      ▼
                                          [Privacy/redazione/retention + audit]
                                                      ▼
                                              [Grafo interrogabile]
\`\`\`

### 4.2 Pipeline di interrogazione (GraphRAG)

**Step 1 — Domanda + contesto di autorizzazione.** L'utente pone una domanda nella chat; il sistema allega il perimetro di visibilità (quali caselle/dossier può vedere). Questo filtro è applicato **prima** del retrieval.

**Step 2 — Comprensione e pianificazione.** L'LLM interpreta l'intento (recupero / sintesi / relazionale / impegni / preparazione) e individua le entità di ancoraggio (persone, organizzazioni, temi, intervallo temporale).

**Step 3 — Retrieval ibrido.** In parallelo: (a) **ricerca semantica** su Qdrant sui contenuti pertinenti (filtrata per perimetro); (b) **navigazione del grafo** su MySQL a partire dai nodi di ancoraggio (vicini, percorsi, sottografi: es. tutti i thread che collegano la persona X all'organizzazione Y sul tema Z).

**Step 4 — Espansione multi-hop e ranking.** Il sottografo recuperato viene espanso lungo gli archi più pesati (recenza, frequenza, rilevanza) entro un budget di hop, e i candidati (messaggi, riassunti, impegni) vengono ordinati per pertinenza e peso.

**Step 5 — Sintesi con citazioni.** Il contesto selezionato (sempre nel perimetro) viene passato all'LLM (Ollama di default) che genera la risposta **citando i messaggi/thread/persone** usati, con link al messaggio originale e date.

**Step 6 — Azione opzionale (con conferma).** Se la richiesta lo prevede (es. "preparami una bozza di risposta"), il sistema propone una bozza tramite il dominio \`email\`/\`agent\`, che l'utente rivede e invia manualmente. Nessun invio automatico.

**Step 7 — Feedback.** L'utente può validare/correggere la risposta; il feedback alimenta i pesi degli archi e la reputazione delle estrazioni (loop di miglioramento).

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa l'impianto del motore a grafo universale (nodi tipizzati + archi pesati su MySQL, semantica su Qdrant) e lo specializza per il dominio comunicazioni.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi principali |
|--------------|-------------|----------------------|
| **Persona** | Individuo che comunica (interno o esterno) | nome canonico, alias/indirizzi, organizzazione, ruolo (inferito) |
| **Organizzazione** | Azienda/ente, derivata dai domini email | nome, dominio/i, tipo (cliente/fornitore/PA/interno) |
| **Casella / Account** | Mailbox ingerita | indirizzo, tipo (personale/condivisa/funzionale), proprietario |
| **Messaggio** | Singola email | message-id, oggetto, data, direzione, lingua, riassunto |
| **Thread / Conversazione** | Insieme di messaggi collegati | oggetto normalizzato, partecipanti, intervallo, riassunto |
| **Tema / Argomento** | Concetto ricorrente (progetto, prodotto, pratica) | etichetta, sinonimi, descrizione |
| **Allegato / Documento** | File allegato (collegato alla KB documentale) | nome, MIME, hash, testo estratto, versione |
| **Impegno / Action item** | Promessa/compito estratto dal testo | descrizione, responsabile, scadenza, stato |
| **Decisione** | Decisione presa in una conversazione | descrizione, data, partecipanti, esito |
| **Evento/Scadenza** | Data rilevante (ponte col dominio \`calendar\`) | tipo, data, collegamento al messaggio/impegno |

### 5.2 Tipi di relazione (archi)

| Relazione (arco) | Da → A | Significato |
|------------------|--------|-------------|
| \`HA_SCRITTO\` | Persona → Messaggio | autore del messaggio |
| \`DESTINATARIO_DI\` (to/cc) | Persona → Messaggio | partecipazione come destinatario (peso diverso per To vs Cc) |
| \`APPARTIENE_A\` | Messaggio → Thread | il messaggio fa parte della conversazione |
| \`RISPONDE_A\` | Messaggio → Messaggio | catena \`In-Reply-To\`/\`References\` |
| \`COMUNICA_CON\` | Persona ↔ Persona | relazione aggregata di comunicazione (pesata) |
| \`TRATTA\` | Thread/Messaggio → Tema | la conversazione riguarda un argomento |
| \`APPARTIENE_A_ORG\` | Persona → Organizzazione | affiliazione (per dominio email) |
| \`COINVOLGE_ORG\` | Thread → Organizzazione | la conversazione coinvolge un'azienda esterna |
| \`CONTIENE_ALLEGATO\` | Messaggio → Allegato | allegato veicolato dal messaggio |
| \`CITA_DOCUMENTO\` | Messaggio/Thread → Documento | riferimento a un documento della KB |
| \`CONTIENE_IMPEGNO\` | Messaggio → Impegno | action item estratto |
| \`RESPONSABILE_DI\` | Persona → Impegno | chi deve adempiere |
| \`HA_DECISO\` | Thread/Persona → Decisione | decisione presa |
| \`SCADE_IL\` | Impegno → Evento/Scadenza | collegamento temporale (dominio \`calendar\`) |
| \`SIMILE_A\` | Messaggio ↔ Messaggio / Tema ↔ Tema | vicinanza semantica (da Qdrant) |

### 5.3 Criteri per il peso degli archi

Il peso è il cuore del grafo: determina cosa l'AI considera più rilevante durante l'espansione multi-hop. Per questo dominio i fattori principali:

| Fattore | Si applica a | Logica |
|---------|--------------|--------|
| **Frequenza** | \`COMUNICA_CON\`, \`TRATTA\`, \`APPARTIENE_A_ORG\` | più messaggi scambiati → arco più forte |
| **Recenza (time decay)** | quasi tutti | comunicazioni recenti pesano più di quelle vecchie (decadimento esponenziale) |
| **Direzionalità/ruolo** | \`DESTINATARIO_DI\` | To pesa più di Cc; mittente più di destinatario passivo |
| **Reciprocità** | \`COMUNICA_CON\` | scambio bidirezionale (botta e risposta) pesa più del solo invio unidirezionale |
| **Rilevanza semantica** | \`TRATTA\`, \`SIMILE_A\`, \`CITA_DOCUMENTO\` | similarità di embedding tra contenuto e tema/documento |
| **Densità del thread** | \`APPARTIENE_A\` | thread lunghi e partecipati indicano temi caldi |
| **Confidenza dell'estrazione** | \`CONTIENE_IMPEGNO\`, \`HA_DECISO\`, NER | quanto l'LLM è sicuro dell'entità/impegno estratto |
| **Feedback utente** | tutti | conferme/correzioni dell'utente rinforzano o indeboliscono l'arco |
| **Importanza dei partecipanti** | \`COMUNICA_CON\` | comunicazioni con decisori/clienti chiave pesano di più (configurabile) |

I pesi sono **ricalcolati incrementalmente** a ogni ingestione e periodicamente normalizzati; il time-decay impone una rivalutazione schedulata per non lasciare "fossilizzare" relazioni ormai inattive.

---

## 6. Fonti dati & connettori (ingestione)

| Fonte | Protocollo / meccanismo | Stato nel codebase | Note |
|-------|-------------------------|--------------------|------|
| **Caselle email aziendali** | IMAP (Eclipse Angus Mail) | **Esistente** (dominio \`email\`, \`EmailPort\`/\`EmailService\`) | Fonte primaria MVP; sync incrementale UID-based |
| **Gmail / Google Workspace** | IMAP + **OAuth2/XOAUTH2** | Da estendere | Evita password applicative; rispetta i limiti API |
| **Microsoft 365 / Exchange** | IMAP + OAuth2 (o Graph in futuro) | Da estendere | Graph API come evoluzione per metadati ricchi |
| **Allegati** | Pipeline documentale (Tika, OCR) | **Esistente** (\`DocumentIngestionPipelineService\`) | Riuso diretto: allegato → documento KB |
| **Caselle condivise / funzionali** (info@, sales@) | IMAP | Da configurare | Perimetri e proprietà multipli |
| **Canali di messaggistica** | Connettori dominio \`messaging\` | **Esistente** (Slack/Telegram/ecc.) | Evoluzione: unificare comunicazioni mail + chat nel grafo |
| **Calendario** | Dominio \`calendar\` | **Esistente** | Impegni/scadenze ↔ eventi calendario |
| **File \`.mbox\`/\`.eml\`/PST export** | Import file | Da creare | Ingestione storica una-tantum/offline |
| **Plugin di terze parti** | PF4J extension point | Esistente (framework) | Nuovo extension point per connettori di comunicazione |

L'architettura espone l'ingestione come **port out** del dominio (es. estensione di \`EmailPort\`) con adapter in infrastruttura, così da poter aggiungere connettori (OAuth2, Graph, mbox, messaging) senza toccare la logica di dominio, coerentemente con l'esagonale. Un nuovo **extension point PF4J** (\`CommunicationSourceExtension\`) consente connettori community.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

Legenda: **CREARE** = non esiste; **SVILUPPARE** = estende qualcosa di esistente; **MANTENERE** = riuso con manutenzione.

### 7.1 MVP (fondamenta utili da subito)

| # | Funzionalità | Tipo | Modulo/i coinvolti |
|---|--------------|------|--------------------|
| 1 | Connettore IMAP con sync incrementale (UID/UIDVALIDITY), credenziali cifrate | SVILUPPARE | \`email\` (port/adapter), infrastruttura, \`batch\` |
| 2 | Estensione modello \`EmailMessage\` (Message-ID, In-Reply-To, References, Cc, allegati, lingua) | SVILUPPARE | \`email\` domain, persistenza, Flyway |
| 3 | Parsing MIME + de-quoting + rimozione firme | CREARE | infrastruttura (adapter) |
| 4 | Ricostruzione thread (References/In-Reply-To + fallback oggetto/tempo/partecipanti) | CREARE | \`email\`/\`knowledge\` service |
| 5 | Estrazione allegati via pipeline documentale (Tika/OCR → Qdrant) | MANTENERE/SVILUPPARE | \`document\`, \`vectorstore\` |
| 6 | Arricchimento AI locale: NER, riassunto messaggio/thread, classificazione | CREARE | \`llm\` (Ollama), \`knowledge\` |
| 7 | Entity resolution persone/organizzazioni | CREARE | \`knowledge\` service |
| 8 | Schema grafo comunicazioni (nodi/archi/pesi) su MySQL | CREARE | \`knowledge\`, Flyway (una query/file) |
| 9 | Embedding contenuti su Qdrant con filtri di perimetro | SVILUPPARE | \`vectorstore\` |
| 10 | Pesi archi base (frequenza, recenza, direzionalità) | CREARE | \`knowledge\` service |
| 11 | API REST: gestione account IMAP, stato ingestione, query grafo | CREARE/SVILUPPARE | \`localmind-api\` (\`email\`, \`knowledge\`) |
| 12 | GraphRAG su comunicazioni con risposte citate | SVILUPPARE | \`knowledge\`/\`llm\`, chat |
| 13 | UI: configurazione account, dossier persona/organizzazione, chat sul grafo | CREARE | frontend (\`email\`, \`knowledge\`, \`chat\`) |
| 14 | Estrazione impegni/scadenze con vista "to-do dalle mail" | CREARE | \`knowledge\`, \`calendar\` |
| 15 | Controlli privacy base: perimetri di visibilità, policy Ollama-only per dominio | CREARE | \`auth\`, \`email\`, config |

### 7.2 Evoluzione (dopo l'MVP)

| # | Funzionalità | Tipo | Note |
|---|--------------|------|------|
| 16 | OAuth2/XOAUTH2 per Gmail e Microsoft 365 | SVILUPPARE | rimuove le password applicative |
| 17 | Microsoft Graph / API native per metadati ricchi | CREARE | oltre IMAP |
| 18 | Import storico \`.mbox\`/\`.eml\`/PST | CREARE | onboarding di archivi pregressi |
| 19 | Estrazione decisioni e timeline di progetto | SVILUPPARE | nodi Decisione + viste temporali |
| 20 | Sentiment/tono e segnali di rischio relazione | CREARE | early warning su clienti |
| 21 | Visualizzazione interattiva del grafo comunicazioni | SVILUPPARE | riuso del visualizzatore grafo universale |
| 22 | Composizione assistita di bozze di risposta (con conferma) | SVILUPPARE | \`email\`/\`agent\`, mai invio automatico |
| 23 | Unificazione mail + canali \`messaging\` + \`calendar\` nel grafo | SVILUPPARE | grafo di comunicazione omni-canale |
| 24 | Suggerimento di collegamenti mancanti / esperti per tema | SVILUPPARE | "chi sa di X?" |
| 25 | Redazione PII avanzata e proxy di anonimizzazione per LLM cloud | CREARE | abilita uso cloud opt-in in sicurezza |
| 26 | Retention/oblio automatizzati + workflow di cancellazione per persona | CREARE | compliance GDPR |
| 27 | Notifiche proattive (impegni in scadenza, follow-up dimenticati) | SVILUPPARE | \`automation\` |
| 28 | Connettori community via PF4J (\`CommunicationSourceExtension\`) | CREARE | marketplace |

### 7.3 Da mantenere (manutenzione continua)

- Connettori IMAP/OAuth2 (rotazione token, evoluzioni provider, deprecazioni).
- Prompt e modelli di estrazione (qualità NER/impegni/decisioni nel tempo, drift).
- Regole di entity resolution e di pulizia (firme/disclaimer nuovi formati).
- Schema grafo, migrazioni Flyway (una query per file) e ricalcolo pesi/time-decay.
- Policy di privacy, retention e audit; aggiornamenti normativi.

---

## 8. Casi d'uso AI / GraphRAG

Esempi concreti di domande che il grafo + GraphRAG abilita, con il tipo di navigazione coinvolta:

| Caso d'uso | Esempio di domanda | Navigazione |
|------------|--------------------|-------------|
| **Stato dossier cliente** | "Qual è lo stato di tutto ciò che abbiamo con il cliente Rossi?" | Org→Persone→Thread→Impegni/Decisioni (multi-hop) |
| **Recupero contestuale** | "Trova la mail in cui hanno confermato il prezzo finale" | Semantico (Qdrant) + filtro thread/persona |
| **Chi sa di X / esperto interno** | "Chi in azienda ha gestito il fornitore Beta?" | Tema/Org→\`COMUNICA_CON\`→Persone (peso) |
| **Impegni e follow-up** | "Cosa ho promesso e non ancora fatto?" | Persona→\`RESPONSABILE_DI\`→Impegni (stato/scadenza) |
| **Briefing pre-call** | "Preparami un briefing prima della call con Gamma" | Sottografo persona/org + riassunti + ultimi thread |
| **Storia di una decisione** | "Come e quando abbiamo deciso di rinnovare il contratto?" | Decisione→Thread→Messaggi citati |
| **Collegamenti non evidenti** | "Ci sono temi che collegano il cliente A e il fornitore B?" | Percorsi tra due Org via Temi/Persone |
| **Continuità (persona assente)** | "Su cosa stava lavorando Tizio coi clienti?" | Persona→Thread aperti→Impegni pendenti |
| **Onboarding** | "Riassumimi la storia del progetto Alfa" | Tema→Thread→timeline+decisioni |
| **Allegato giusto** | "Qual è l'ultima versione del contratto inviata a Delta?" | Org→Messaggi→Allegati (versione/hash) |

Tutte le risposte includono **citazioni** ai messaggi/thread/persone (con data e link al messaggio originale), nel rispetto del perimetro di visibilità dell'utente. Il default di esecuzione è **Ollama in locale**; l'uso di provider cloud è ammesso solo se la policy del dominio lo consente e, idealmente, dopo redazione PII.

---

## 9. KPI & metriche di successo

| Categoria | KPI | Come si misura | Target indicativo |
|-----------|-----|----------------|-------------------|
| **Adozione** | Caselle ingerite / utenti attivi | Conteggio account + query/utente/settimana | Crescita costante |
| **Copertura** | % messaggi processati con successo | Messaggi ingeriti / totali in perimetro | > 98% |
| **Qualità ingestione** | Accuratezza ricostruzione thread | Campione validato manualmente | > 95% |
| **Qualità AI** | Precisione/recall NER e impegni | Set di valutazione etichettato | Precision > 0,85 |
| **Entity resolution** | % alias correttamente unificati | Campione di persone/org | > 0,9 |
| **Efficacia GraphRAG** | Accuratezza su domande multi-hop | Suite di domande di riferimento | Netto vantaggio vs RAG flat |
| **Utilità** | Tasso di risposte "utili" (feedback) | Pollice su/giù in chat | > 80% utili |
| **Risparmio tempo** | Tempo medio per "trovare/ricostruire contesto" | Survey + analytics | Riduzione marcata |
| **Impegni** | % follow-up rispettati grazie alle notifiche | Impegni chiusi in tempo / totale | In aumento |
| **Privacy** | Incidenti di esfiltrazione dati | Monitoraggio (deve essere zero) | 0 |
| **Performance** | Latenza media risposta GraphRAG | Tracing | Accettabile per uso interattivo |
| **Freschezza** | Ritardo medio di ingestione | Timestamp ricezione → disponibilità nel grafo | Minuti, non ore |

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---------|---------|-------------|
| **Privacy / GDPR (dato sensibilissimo)** | Alto | Local-first by design; Ollama-only di default per il dominio; redazione PII; perimetri di visibilità; consenso e finalità; retention/oblio; audit |
| **Percezione di sorveglianza dei dipendenti** | Alto | Finalità dichiarate, esclusione comunicazioni personali/sensibili, no monitoraggio individuale, trasparenza verso gli utenti |
| **Accesso oltre perimetro** | Alto | Filtro di autorizzazione applicato a monte del retrieval (\`auth\`), test di sicurezza dedicati |
| **Errori di entity resolution** | Medio | Strategie multi-segnale + revisione/merge manuale + feedback; soglie di confidenza |
| **Allucinazioni / estrazioni errate (impegni, decisioni)** | Medio | Risposte sempre citate; confidenza esposta; validazione umana per azioni; mai invio automatico |
| **Threading impreciso (oggetti riusati, fork)** | Medio | Header standard + fallback robusti; validazione su campione |
| **Volume/scalabilità (caselle enormi)** | Medio | Sync incrementale, batch, checkpoint, paginazione; ricalcolo pesi incrementale |
| **Credenziali compromesse** | Alto | OAuth2 dove possibile; cifratura at-rest; rotazione; least privilege |
| **Rumore (newsletter, automatici, spam)** | Basso | Filtri per mittente/dominio, classificazione, esclusione liste |
| **Costo/latency LLM su grandi volumi** | Medio | Elaborazione locale Ollama, batching notturno, riassunti gerarchici, caching |
| **Multilingua** | Basso | Rilevamento lingua + prompt/embedding adeguati (IT/EN e oltre) |
| **Dipendenza dal provider IMAP** | Basso | Astrazione port/adapter; più connettori; import mbox come fallback |

---

## 11. Manutenzione & evoluzione

- **Connettori e autenticazione**: monitorare le evoluzioni dei provider (Gmail/Microsoft), migrare progressivamente da password applicative a OAuth2, gestire rotazione token e deprecazioni API. Tenere l'astrazione port/adapter pulita per aggiungere Graph/EWS/mbox senza toccare il dominio.
- **Qualità dei modelli di estrazione**: gli prompt e i modelli Ollama per NER, riassunti, impegni e decisioni vanno valutati periodicamente con un set etichettato per intercettare il *drift*; versionare i prompt e tracciare le metriche di qualità (sezione 9).
- **Regole di pulizia ed entity resolution**: nuovi formati di firma/disclaimer e nuovi alias richiedono aggiornamento continuo delle euristiche; prevedere strumenti di merge/split manuale dei nodi Persona/Organizzazione.
- **Pesi del grafo**: ricalcolo incrementale a ogni ingestione e job schedulato per il time-decay e la normalizzazione; rivedere periodicamente i fattori e i loro coefficienti in base al feedback.
- **Schema e migrazioni**: ogni evoluzione dello schema grafo passa per Flyway con **una sola query per file** (vincolo di progetto) e mapping UUID \`@JdbcTypeCode(SqlTypes.CHAR)\`.
- **Privacy operativa**: revisione periodica di perimetri, policy di retention, esecuzione effettiva dell'oblio, audit; allineamento agli aggiornamenti normativi (GDPR e settoriali).
- **i18n**: UI, documentazione ed enum (es. tipo casella, classificazione messaggio, stato impegno, tipo organizzazione) tradotte e instradate IT/EN secondo lo switch, coerentemente con le regole di progetto.
- **Estensibilità community**: mantenere stabile l'extension point PF4J \`CommunicationSourceExtension\` e documentarlo per i contributor; pubblicare connettori sul marketplace.
- **Roadmap evolutiva**: dall'MVP IMAP → OAuth2/Graph → import storico → omni-canale (mail + \`messaging\` + \`calendar\`) → suggerimenti proattivi (\`automation\`) → composizione assistita di bozze.

---

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo / dominio | Ruolo nell'ambito | Riuso vs estensione |
|------------------|-------------------|---------------------|
| **\`email\`** | Cuore dell'ingestione IMAP; \`EmailMessage\`, \`EmailPort\`, \`EmailService\` già presenti | **Estendere** modello e port; aggiungere sync incrementale e parsing avanzato |
| **\`knowledge\`** | Dominio del grafo: nodi/archi/pesi, entity resolution, GraphRAG | **Estendere** con tipi di nodo/relazione del dominio comunicazioni |
| **\`llm\` + Ollama** | Arricchimento (NER, riassunti, impegni) e generazione risposte | **Riuso** del \`LlmGatewayService\`; Ollama default, policy per-dominio |
| **\`document\` + Tika/OCR** | Estrazione testo degli allegati → documenti KB | **Riuso** della \`DocumentIngestionPipelineService\` |
| **\`vectorstore\` (Qdrant)** | Embedding e ricerca semantica dei contenuti | **Riuso/estensione** con filtri di perimetro |
| **MySQL + Flyway** | Struttura del grafo e persistenza | **Estensione** schema (una query/file, UUID CHAR(36)) |
| **\`localmind-batch\` + scheduler** | Job di ingestione incrementale schedulati | **Riuso** del pattern batch/folder-scan |
| **\`auth\`** | Perimetri di visibilità, ruoli (utente/admin/DPO) | **Estendere** con autorizzazione a livello di casella/dossier |
| **\`calendar\`** | Impegni/scadenze ↔ eventi | **Integrazione** bidirezionale |
| **\`messaging\`** | Canali chat (Slack/Telegram/...) | **Evoluzione**: grafo di comunicazione omni-canale |
| **\`automation\`** | Notifiche proattive (follow-up, scadenze) e webhook | **Riuso** per trigger su nuovi impegni/eventi |
| **\`agent\`** | Composizione assistita di bozze, azioni orchestrate | **Evoluzione**, sempre con conferma umana |
| **\`marketplace\` + PF4J** | Connettori di comunicazione di terze parti | **Estensione**: nuovo \`CommunicationSourceExtension\` |
| **\`common\` (eventi/exception)** | Eventi di dominio (ingestione completata, nuovo impegno) | **Riuso** di \`DomainEventPublisherPort\` |
| **Frontend Angular** | UI account, dossier, chat sul grafo, viste impegni | **Creare** feature \`email\`/\`knowledge\`; riuso \`ChatStore\`, i18n |

L'ambito si innesta quindi in modo naturale sull'architettura esagonale esistente: il dominio resta puro (wiring via \`DomainConfig\`), gli adapter IMAP/OAuth2/mbox vivono in infrastruttura come implementazioni di port out, l'API espone gli endpoint sotto \`/api/v1/\` e il frontend aggiunge le feature in lazy-loading. Nessun nuovo datastore: **MySQL per la struttura del grafo, Qdrant per la semantica**, AI **Ollama in locale** come default — pienamente coerente con i vincoli di local-first, privacy enterprise, open source e bilinguismo IT/EN del progetto.

---

*Fonti di riferimento per best practice 2026: Microsoft GraphRAG, Gartner (GraphRAG tra i trend dati 2026), letteratura su entity resolution per knowledge graph enterprise, architetture email local-first con redazione PII e consenso esplicito per l'invio a LLM esterni.*
`;
