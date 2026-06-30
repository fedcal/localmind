# Servizi & sanità locale

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo ambito appartiene al **gruppo consumer** del motore di knowledge graph universale di LocalMind. Mentre il verticale "scoperta del territorio" (turismo, eventi, esperienze) risponde alla domanda *"cosa posso fare qui?"*, l'ambito **Servizi & sanità locale** risponde alla domanda complementare e più urgente: *"di quale servizio ho bisogno, dove lo trovo, come ci accedo e a chi mi rivolgo?"*. È il ponte tra il bisogno di un cittadino e l'offerta — pubblica, sanitaria e privata-professionale — del territorio, modellata come grafo pesato navigabile dall'AI.

L'ambito è deliberatamente costruito attorno a uno standard di settore consolidato — **HSDS (Human Services Data Specification) di Open Referral** e il relativo **FHIR Human Services Directory** — per garantire interoperabilità con cataloghi esistenti, e attorno alle iniziative italiane di digitalizzazione (FSE 2.0, SPID/CIE, PagoPA, HL7 CDA2). Tutto resta però **local-first**: i dati sensibili dei cittadini non lasciano mai l'istanza self-hosted senza consenso esplicito, e l'AI di default è Ollama in locale.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema concreto

L'accesso ai servizi pubblici, sanitari e professionali locali è oggi **frammentato, opaco e cognitivamente costoso** per il cittadino. Le informazioni esistono ma sono disperse su decine di portali eterogenei (sito del Comune, ASL, Regione, sportelli, pagine Facebook di associazioni, volantini cartacei, passaparola). Ne derivano problemi ricorrenti:

- **Frammentazione delle fonti.** Per capire come ottenere un certificato, prenotare una visita, accedere a un sostegno economico o trovare un professionista accreditato, il cittadino deve consultare fonti diverse, ciascuna con un suo linguaggio burocratico e una sua struttura.
- **Distanza tra bisogno e servizio.** Il cittadino esprime un *bisogno* in linguaggio naturale ("ho bisogno di aiuto per mia madre anziana non autosufficiente", "ho perso il lavoro e devo pagare l'affitto", "mio figlio ha bisogno di un logopedista"), ma i cataloghi sono organizzati per *ente erogatore* o per *categoria amministrativa*, non per bisogno. Manca il livello di traduzione bisogno → servizio.
- **Informazioni obsolete o incoerenti.** Orari, requisiti di accesso (ISEE, residenza, fascia d'età), documenti necessari, costi (ticket, esenzioni), tempi di attesa cambiano spesso e non sono allineati tra le fonti.
- **Catene di prerequisiti invisibili.** Molti servizi richiedono passi propedeutici (SPID/CIE per accedere ai portali, scelta del medico di base prima della prenotazione, attestazione ISEE prima del bonus, impegnativa prima della visita specialistica). Queste dipendenze sono raramente esplicitate e generano viaggi a vuoto.
- **Barriere di accesso digitale e linguistico.** Anziani, persone fragili, stranieri e caregiver fanno fatica con portali progettati per "addetti ai lavori". Manca un'interfaccia conversazionale che guidi passo-passo.
- **Sovraccarico degli sportelli.** URP, segreterie ASL, CAF e patronati ricevono un volume enorme di richieste informative di primo livello ("a chi devo rivolgermi per...") che potrebbero essere risolte da un assistente intelligente, liberando gli operatori per i casi complessi.

### 1.2 La soluzione LocalMind

LocalMind modella l'intero ecosistema di servizi come un **grafo pesato di conoscenza**: i nodi rappresentano servizi, enti, sedi, professionisti, prestazioni, bisogni, requisiti e procedure; gli archi rappresentano relazioni pesate (un bisogno *è soddisfatto da* un servizio con un certo grado di pertinenza, un servizio *richiede come prerequisito* un altro, una prestazione *è erogata presso* una sede, ecc.). Su questo grafo opera l'AI in modalità **GraphRAG**: combina la ricerca semantica (Qdrant) con la navigazione delle relazioni (MySQL) per rispondere a domande complesse e far emergere il **percorso completo** dal bisogno alla fruizione del servizio.

Il valore differenziale rispetto a un semplice motore di ricerca o a un chatbot FAQ:

| Capacità | Motore di ricerca / FAQ tradizionale | LocalMind (grafo + GraphRAG) |
|----------|--------------------------------------|------------------------------|
| Traduzione bisogno → servizio | Match per parole chiave | Reasoning sul grafo bisogno↔servizio con pesi di pertinenza |
| Catene di prerequisiti | Non gestite | Percorso multi-hop esplicito (impegnativa → prenotazione → ticket → esenzione) |
| Orientamento personalizzato | Genericо | Filtrato per requisiti del cittadino (residenza, ISEE, età, fragilità) |
| Aggiornamento dati | Manuale, statico | Ingestione da open data + verifica community + scadenze |
| Spiegabilità | Assente | Risposta con citazione dei nodi/percorsi usati |
| Privacy dati cittadino | Spesso cloud | Local-first, AI Ollama in locale |

### 1.3 Il valore per gli stakeholder

- **Per il cittadino:** un unico punto di accesso conversazionale, bilingue (IT/EN, estendibile), che parte dal bisogno espresso a parole e restituisce *cosa fare, dove, con quali documenti, a quali costi e con quali alternative*, includendo la catena dei prerequisiti.
- **Per la Pubblica Amministrazione locale (Comuni, Unioni di Comuni):** riduzione del carico sugli sportelli, un catalogo dei servizi sempre coerente e self-hosted (sovranità del dato), apertura del proprio catalogo in formato standard (HSDS) riutilizzabile.
- **Per il sistema sanitario locale (ASL, distretti, MMG/PLS, farmacie):** orientamento del paziente verso il setting corretto (riducendo accessi impropri al pronto soccorso), informazioni sempre allineate su prestazioni, esenzioni e percorsi.
- **Per il terzo settore e i professionisti:** visibilità strutturata e community-driven dei servizi offerti (associazioni, CAF, patronati, studi professionali accreditati), con ranking emergente basato su qualità e utilità reale.

### 1.4 Confini di responsabilità (cosa NON è)

Per evitare ambiguità e rischi normativi, l'ambito si definisce anche per esclusione:

- **Non è uno strumento di diagnosi o triage clinico.** Non fornisce pareri medici; orienta verso il servizio/professionista corretto e segnala sempre quando è necessario rivolgersi a un sanitario o ai numeri di emergenza.
- **Non sostituisce i sistemi ufficiali di prenotazione/pagamento.** Si integra con essi (deep link a CUP, FSE, PagoPA) ma resta un livello di orientamento e conoscenza, non un sistema transazionale sanitario certificato.
- **Non è un registro sanitario.** Non conserva cartelle cliniche; eventuali dati personali del cittadino restano in locale, transitori e sotto controllo dell'utente.

---

## 2. Personas & utenti target

| Persona | Descrizione | Obiettivo primario | Esigenza chiave |
|---------|-------------|--------------------|-----------------|
| **Cittadino generico** | Adulto che cerca un servizio occasionale (certificato, bonus, prenotazione) | Risolvere una pratica senza perdere tempo | Linguaggio semplice, percorso passo-passo |
| **Caregiver familiare** | Figlio/coniuge che assiste anziano o persona fragile | Trovare assistenza domiciliare, ADI, sollievo, indennità | Visione d'insieme dei servizi sociosanitari integrati |
| **Persona fragile / anziano** | Bassa alfabetizzazione digitale | Capire "a chi mi rivolgo" | Interfaccia conversazionale, eventualmente vocale, IT chiaro |
| **Cittadino straniero / neoarrivato** | Conosce poco la lingua e il sistema | Orientarsi su sanità, residenza, scuola | Multilingua, traduzione del gergo burocratico |
| **Paziente cronico** | Gestisce patologia di lungo periodo | Prestazioni ricorrenti, esenzioni, PDTA | Percorsi (PDTA), esenzioni, continuità |
| **Operatore URP / sportello sociale** | Dipendente PA che assiste al front-office | Rispondere rapidamente e in modo coerente | Strumento interno affidabile, citazioni di fonte |
| **Operatore terzo settore (CAF, patronato, associazione)** | Accompagna utenti fragili | Mappare l'offerta e indirizzare | Catalogo aggiornato, segnalazione lacune |
| **Redattore / curatore del catalogo** | Funzionario o volontario che mantiene i dati | Tenere il grafo aggiornato e corretto | Strumenti di editing, moderazione, validazione |
| **Amministratore dell'istanza** | Tecnico del Comune/ASL che gestisce LocalMind | Self-hosting, ingestione fonti, privacy | Connettori, configurazione, controllo accessi |
| **Contributore community** | Cittadino attivo che segnala/corregge informazioni | Migliorare l'accuratezza collettiva | Contributi semplici, peso/reputazione |

Distinzione importante: gli **utenti finali consumer** (prime cinque righe) interagiscono in sola lettura tramite l'assistente conversazionale; gli **operatori e curatori** hanno permessi di editing/moderazione; l'**amministratore** gestisce ingestione e configurazione. Questa segmentazione guida i ruoli di autorizzazione (dominio `auth`).

---

## 3. Requisiti in input

Questa sezione definisce in dettaglio **cosa deve entrare nel sistema** affinché il grafo dei servizi sia utile, accurato e manutenibile. Si distinguono input di **dominio** (i dati che descrivono servizi e bisogni), input **dell'utente finale** (la richiesta) e input di **configurazione/governance**.

### 3.1 Input di dominio (i dati del catalogo)

Per ogni **servizio** o **prestazione** il modello richiede un set di attributi minimi e uno esteso:

| Categoria | Campi minimi (MVP) | Campi estesi (evoluzione) |
|-----------|--------------------|---------------------------|
| Identità | nome, descrizione sintetica, categoria/tassonomia, lingue erogazione | descrizione estesa, parole chiave, sinonimi colloquiali |
| Erogatore | ente/organizzazione responsabile, tipo (pubblico/privato accreditato/terzo settore) | gerarchia organizzativa, partita IVA/codice ente |
| Localizzazione | sede/i fisica/e (indirizzo, coordinate), bacino territoriale | accessibilità (barriere architettoniche), mezzi pubblici |
| Accesso | requisiti (residenza, età, ISEE, condizione), documenti necessari | prerequisiti propedeutici (altri servizi), canali (online/sportello/telefono) |
| Tempi & costi | orari, costo/ticket, esenzioni applicabili | tempi di attesa medi, calendario aperture/chiusure |
| Contatti | telefono, email, sito/portale, link prenotazione | referente, PEC, social, chat |
| Validità | data ultimo aggiornamento, stato (attivo/sospeso) | data scadenza informazione, fonte autorevole, livello di verifica |

Per i **professionisti / studi** (medici di base, specialisti privati accreditati, farmacie, psicologi, fisioterapisti, ecc.) si aggiungono: specializzazione, accreditamento/convenzione SSN, ordine professionale di iscrizione, disponibilità a nuovi pazienti, prestazioni offerte.

Per i **bisogni** (il lato domanda del grafo) si richiede una **tassonomia dei bisogni** in linguaggio cittadino, con sinonimi e formulazioni colloquiali, mappata alle categorie di servizio. Esempi: "non autosufficienza anziano", "difficoltà economica/affitto", "supporto psicologico", "violenza domestica", "disabilità minore", "primo accesso sanitario per straniero".

### 3.2 Vincoli di qualità sugli input di dominio

Il sistema deve **validare al confine** (coerentemente con le regole di progetto sull'input validation):

- **Obbligatorietà:** i campi minimi sono obbligatori; un servizio senza erogatore o senza modalità d'accesso non è pubblicabile.
- **Normalizzazione:** indirizzi geocodificati (lat/long), telefoni in formato canonico, categorie agganciate alla tassonomia controllata (no testo libero per la categoria).
- **Freschezza:** ogni informazione ha una data di aggiornamento; oltre una soglia configurabile l'informazione è marcata "da verificare" e il suo peso decade (vedi §5).
- **Provenienza:** ogni nodo/arco traccia la fonte (open data ufficiale, contributo community, redazione interna) e il livello di verifica.
- **Sensibilità:** nessun dato personale di cittadini nei nodi di dominio; il dominio descrive *l'offerta*, non gli utenti.

### 3.3 Standard e formati di interscambio supportati

Per massimizzare interoperabilità e riuso, gli input di dominio devono poter essere importati/esportati nei formati di settore:

- **HSDS (Human Services Data Specification) 3.x di Open Referral** — formato di riferimento per cataloghi di servizi alla persona (organizzazioni, servizi, sedi, contatti, requisiti, orari). È il modello di interscambio primario dell'ambito.
- **FHIR Human Services Directory IG** — per interoperabilità con sistemi sanitari (provider directory) e con FSE 2.0.
- **Open data PA / formati tabellari** — CSV/JSON/Excel pubblicati da Comuni, Regioni, ASL; CKAN/DCAT-AP_IT per i portali open data.
- **HL7 CDA2 / FSE 2.0** — riferimento per i metadati di prestazioni sanitarie (solo a livello di catalogo prestazioni, non di documenti clinici personali).
- **Schema.org (GovernmentService, MedicalOrganization)** — per arricchimento e SEO dei nodi pubblici.

### 3.4 Input dell'utente finale (la richiesta)

L'assistente conversazionale accetta:

- **Bisogno in linguaggio naturale** (testo, in IT/EN ed estendibile), eventualmente vago o emotivo.
- **Contesto opzionale e volontario** per personalizzare l'orientamento: comune/quartiere di residenza, fascia ISEE, fascia d'età, presenza di disabilità/fragilità, condizione (es. caregiver, straniero). Tutto **opt-in**, trattato in locale e non persistito senza consenso.
- **Filtri espliciti**: tipo di servizio, distanza massima, solo servizi gratuiti/esenti, solo online, solo accessibili.
- **Documenti caricati** (opzionale): un volantino, una lettera della PA, un referto — gestiti dal dominio `document` (Tika/OCR) per estrarne il contesto e orientare meglio; restano locali.

### 3.5 Input di configurazione e governance

- **Perimetro territoriale** dell'istanza (un Comune, un'Unione, un distretto ASL): definisce il bacino dei dati.
- **Tassonomie attive** (categorie servizi, tassonomia bisogni) e relativi mapping.
- **Connettori abilitati** e relative credenziali/endpoint (portali open data, CUP, FSE — ove disponibili API).
- **Politiche di moderazione**: chi può creare/modificare/approvare nodi; soglie di reputazione community.
- **Soglie dei pesi** e politiche di decadimento (freschezza, fonte, feedback).
- **Disclaimer e testi legali** (limiti dell'orientamento, rinvio all'emergenza, privacy).

---

## 4. Flusso dell'attività (step-by-step)

Si descrivono due flussi complementari: il **flusso di ingestione/costruzione del grafo** (lato dati) e il **flusso di consultazione** (lato utente). Sono il cuore operativo dell'ambito e vanno implementati con cura.

### 4.1 Flusso di ingestione e costruzione del grafo

```
Fonte → Connettore → Normalizzazione → Mapping a nodi/archi → Validazione →
Deduplica/Entity resolution → Pesatura iniziale → Embedding → Persistenza (MySQL+Qdrant) → Indicizzazione grafo
```

1. **Acquisizione dalla fonte.** Un connettore (dominio `document`/batch o connettore dedicato) preleva i dati: API open data, file CSV/JSON, pagina web, documento PDF/volantino (Tika + OCR Tesseract), o feed HSDS/FHIR. L'esecuzione è schedulata (batch `localmind-batch`) o on-demand.
2. **Normalizzazione.** Pulizia dei campi, geocodifica indirizzi, normalizzazione telefoni/orari, riconoscimento della categoria rispetto alla tassonomia controllata. Gli input invalidi vengono respinti con messaggio chiaro e loggati.
3. **Mapping a nodi e archi.** Ogni record diventa uno o più nodi tipizzati (Servizio, Ente, Sede, Prestazione, …) e gli archi che li collegano (eroga, ha sede, richiede prerequisito, soddisfa bisogno). Il mapping è guidato dallo schema di dominio (HSDS → modello a grafo LocalMind).
4. **Validazione di dominio.** Verifica obbligatorietà, coerenza referenziale (ogni Servizio ha un Ente e almeno una modalità d'accesso) e qualità (freschezza, provenienza).
5. **Deduplica / entity resolution.** Riconoscimento di nodi già esistenti (stesso ente/sede/servizio) tramite chiavi naturali + similarità semantica (embedding) per evitare duplicati. In caso di conflitto, merge con tracciamento delle fonti.
6. **Pesatura iniziale degli archi.** Calcolo del peso di partenza in base a fonte (ufficiale > community > inferito), completezza e freschezza (vedi §5).
7. **Embedding semantico.** Generazione degli embedding (Ollama `@Primary`) delle descrizioni di servizi e bisogni e indicizzazione su Qdrant per la ricerca semantica.
8. **Persistenza.** Nodi e archi su MySQL (struttura del grafo, pesi, metadati, provenienza); vettori su Qdrant; il tutto via adapter dell'infrastruttura esagonale.
9. **Suggerimento collegamenti.** L'AI propone archi mancanti non evidenti (es. un bisogno "supporto a caregiver" collegabile a un servizio finora non mappato su quel bisogno); i suggerimenti entrano in coda di moderazione.
10. **Disponibilità.** Il grafo aggiornato è subito interrogabile; eventi di dominio notificano l'aggiornamento (riuso del `DomainEventPublisherPort`).

### 4.2 Flusso di consultazione (cittadino / operatore)

```
Bisogno NL → Comprensione intento → Recupero ibrido (semantico+grafo) →
Espansione multi-hop (prerequisiti, alternative) → Filtri personali → Ranking →
Risposta guidata con citazioni → Azioni (deep link, salva, contatta) → Feedback
```

1. **Espressione del bisogno.** L'utente scrive (o detta) il proprio bisogno in linguaggio naturale, eventualmente con contesto volontario e filtri.
2. **Comprensione dell'intento.** L'AI (Ollama in locale) interpreta il bisogno, lo disambigua se necessario (domanda di chiarimento mirata, es. "intende l'assistenza domiciliare sanitaria o l'aiuto economico?") e lo mappa sui nodi-bisogno della tassonomia.
3. **Recupero ibrido (GraphRAG).** Ricerca semantica su Qdrant per i servizi pertinenti + ingresso nel grafo dai nodi-bisogno individuati. I due segnali si combinano.
4. **Espansione multi-hop.** Dal servizio candidato l'AI naviga le relazioni: prerequisiti (cosa serve prima), sedi (dove), alternative (servizi equivalenti se quello principale non è accessibile), prestazioni correlate, ente erogatore. Costruisce così il **percorso completo**.
5. **Applicazione dei filtri personali.** Esclude/penalizza i servizi non compatibili con i requisiti dichiarati (residenza fuori bacino, ISEE oltre soglia, età non idonea), valorizzando esenzioni e gratuità quando rilevanti.
6. **Ranking.** Ordina i risultati per pertinenza del bisogno, peso degli archi, prossimità geografica, freschezza e reputazione community.
7. **Risposta guidata e spiegabile.** L'assistente restituisce: il servizio (o i 2-3 migliori), *cosa fare passo-passo*, documenti necessari, costi/esenzioni, dove e quando, alternative, e **le citazioni dei nodi/percorsi** usati (trasparenza). Include sempre i disclaimer (non è parere medico; numeri di emergenza dove pertinente).
8. **Azioni.** Deep link ai sistemi ufficiali (prenotazione CUP, FSE, PagoPA per pagamenti, sito dell'ente), salvataggio del percorso, copia dei contatti, eventuale generazione di un promemoria (riuso dominio `calendar`) o invio via messaggistica (dominio `messaging`/`email`).
9. **Feedback.** L'utente segnala se l'informazione era utile/corretta/aggiornata. Il feedback alimenta i pesi degli archi e la coda di curatela (chiusura del ciclo verso §4.1).

### 4.3 Flusso di curatela e moderazione

1. Contributi e segnalazioni della community (nuovo servizio, correzione, "informazione obsoleta") entrano in **coda di moderazione**.
2. Il curatore/operatore esamina, confronta con la fonte autorevole, approva/rifiuta/richiede modifica.
3. L'approvazione aggiorna nodi/archi e ricalcola i pesi; il rifiuto viene tracciato con motivazione.
4. Le segnalazioni ripetute su uno stesso nodo ne abbassano il peso fino alla verifica (meccanismo anti-degrado).

### 4.4 Gestione dei casi limite

- **Bisogno non mappato:** se nessun servizio copre il bisogno, l'AI lo dichiara, propone il canale generico (URP, numero unico sociale) e registra la lacuna per la curatela.
- **Emergenza rilevata:** se il testo dell'utente indica urgenza sanitaria o pericolo (es. parole chiave di rischio), l'assistente interrompe l'orientamento ordinario e rinvia immediatamente a 112/118/numeri dedicati.
- **Ambiguità irrisolvibile:** massimo N domande di chiarimento, poi risposta con le migliori ipotesi etichettate come tali.
- **Dato in conflitto tra fonti:** mostra la versione più autorevole/recente e segnala la discordanza.

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa il **motore a grafo core** di LocalMind (nodi tipizzati + archi pesati su MySQL, semantica su Qdrant). Di seguito i tipi specifici dell'ambito.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi chiave |
|--------------|-------------|------------------|
| `Bisogno` | Necessità del cittadino in linguaggio naturale | nome, sinonimi, area (sanitario/sociale/amministrativo) |
| `Servizio` | Servizio erogato (pubblico/privato/terzo settore) | nome, categoria, descrizione, lingue, stato |
| `Prestazione` | Unità erogabile specifica (es. visita cardiologica, certificato anagrafico) | tipo, ticket/costo, esenzioni, tempi attesa |
| `Ente` / `Organizzazione` | Soggetto erogatore | tipo, natura giuridica, gerarchia |
| `Sede` / `PuntoDiAccesso` | Luogo fisico o canale digitale | indirizzo, coordinate, accessibilità, orari |
| `Professionista` | Medico, specialista, psicologo, ecc. | specializzazione, accreditamento, ordine, disponibilità |
| `Requisito` | Condizione d'accesso | tipo (residenza/ISEE/età/condizione), valore/soglia |
| `Documento` / `Modulo` | Documento necessario o prodotto | nome, dove ottenerlo, formato |
| `Procedura` / `Percorso` | Iter o PDTA (percorso diagnostico-terapeutico) | passi ordinati, durata stimata |
| `Categoria` / `Tassonomia` | Nodo di classificazione | livello, parent, mapping standard (HSDS) |
| `Area territoriale` | Comune, quartiere, distretto, bacino | tipo, codice ISTAT |
| `Contatto` | Recapito | canale, valore, orari |
| `FonteDato` | Provenienza dell'informazione | tipo, URL, data, autorevolezza |

### 5.2 Tipi di relazione (archi)

| Relazione | Da → A | Significato |
|-----------|--------|-------------|
| `SODDISFA` | Bisogno → Servizio/Prestazione | Il servizio risponde al bisogno (con grado di pertinenza) |
| `EROGATO_DA` | Servizio/Prestazione → Ente/Professionista | Chi eroga |
| `HA_SEDE_PRESSO` | Servizio → Sede/PuntoDiAccesso | Dove si accede |
| `RICHIEDE_PREREQUISITO` | Servizio → Servizio/Procedura | Passo propedeutico (es. impegnativa prima della visita) |
| `RICHIEDE_REQUISITO` | Servizio → Requisito | Condizione d'accesso |
| `RICHIEDE_DOCUMENTO` | Servizio → Documento/Modulo | Documenti necessari |
| `ALTERNATIVA_A` | Servizio → Servizio | Servizio equivalente/sostitutivo |
| `FA_PARTE_DI` | Prestazione → Servizio / Sede → Ente | Composizione/gerarchia |
| `APPARTIENE_A_CATEGORIA` | Servizio → Categoria | Classificazione |
| `SERVE_AREA` | Servizio/Sede → Area territoriale | Bacino di competenza |
| `STEP_DI` | Procedura → Servizio/Prestazione | Tappa di un percorso/PDTA |
| `CORRELATO_A` | Servizio → Servizio | Affinità tematica (utile per suggerimenti) |
| `HA_CONTATTO` | Ente/Sede/Servizio → Contatto | Recapiti |
| `ATTESTATO_DA` | Nodo/Arco → FonteDato | Provenienza e verifica |

### 5.3 Criteri di peso degli archi

Il peso (0–1, o scala configurabile) è **derivato e dinamico**, coerente con i fattori configurabili del motore core (frequenza d'uso, rilevanza, dipendenze, feedback). Per questo ambito i fattori sono:

| Fattore | Effetto sul peso | Note |
|---------|------------------|------|
| **Autorevolezza della fonte** | Open data ufficiale > redazione interna > community > inferito AI | Tracciato via `ATTESTATO_DA` |
| **Freschezza** | Decadimento nel tempo dall'ultimo aggiornamento/verifica | Soglia configurabile; oltre soglia → "da verificare" |
| **Pertinenza semantica** | Per `SODDISFA`: similarità embedding bisogno↔servizio | Da Qdrant |
| **Feedback utente** | Voti "utile/corretto" aumentano, segnalazioni di errore diminuiscono | Reputazione community |
| **Frequenza d'uso** | Servizi effettivamente consultati/seguiti pesano di più | Segnale d'uso aggregato e anonimo |
| **Completezza del nodo** | Nodi con campi minimi completi pesano di più | Penalità per dati mancanti |
| **Prossimità / copertura territoriale** | Per ranking: servizi nel bacino dell'utente | Applicato in fase di query, non solo statico |
| **Conferma multi-fonte** | Informazione confermata da più fonti indipendenti | Bonus di affidabilità |

Il peso così calcolato guida sia il **ranking** delle risposte sia il **reasoning multi-hop** dell'AI (preferisce percorsi ad alto peso) sia il **decadimento controllato** dei dati obsoleti.

---

## 6. Fonti dati & connettori (ingestione)

| Fonte | Tipo | Connettore | Priorità |
|-------|------|-----------|----------|
| Portali open data PA (Comuni/Regioni) — CKAN/DCAT-AP_IT | API/CSV/JSON | Connettore open data + batch | MVP |
| Cataloghi HSDS / Open Referral | JSON/API | Connettore HSDS (mapping nativo) | MVP |
| FHIR Human Services Directory / provider directory | FHIR REST | Connettore FHIR | Evoluzione |
| Siti web istituzionali (Comune, ASL, distretto) | Web scraping/HTML | Connettore web + Tika | MVP |
| Documenti/volantini PDF, brochure | File | Dominio `document` (Tika + OCR Tesseract) | MVP |
| FSE 2.0 / cataloghi prestazioni sanitarie (HL7 CDA2) | API/metadati | Connettore sanitario (solo catalogo) | Evoluzione |
| CUP regionali (catalogo prestazioni, tempi d'attesa) | API ove disponibili | Connettore CUP | Evoluzione |
| Contributi community (form interno) | UI | Editor + moderazione | MVP |
| Inserimento/editing redazionale | UI | Editor curatore | MVP |
| Tabelle Excel/CSV legacy degli enti | File | Importatore tabellare con mapping | MVP |

Principi di ingestione: ogni connettore è isolato (riuso del pattern **plugin PF4J** e degli extension point — un `ServiceDirectoryConnectorExtension` permette di aggiungere connettori senza toccare il core); l'ingestione passa sempre dalla pipeline §4.1 (normalizzazione → mapping → validazione → deduplica → pesatura → embedding → persistenza); ogni dato porta con sé la sua `FonteDato`. I batch riusano `localmind-batch` (folder scan, scheduling) e gli adapter l'infrastruttura esagonale.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

### 7.1 MVP (prima release dell'ambito)

| # | Funzionalità | Cosa comporta | Moduli coinvolti |
|---|--------------|---------------|------------------|
| 1 | **Schema di dominio "servizi"** | Tipi di nodo/relazione §5 sul motore a grafo core; migrazioni Flyway (una query per file) | `knowledge`/grafo core, MySQL |
| 2 | **Tassonomia bisogni + categorie servizi** | Tassonomia controllata IT/EN con sinonimi, mapping HSDS | `knowledge`, i18n |
| 3 | **Connettore HSDS + importatore tabellare** | Import cataloghi standard e CSV/Excel degli enti | batch, infrastructure, plugin |
| 4 | **Pipeline di ingestione** | Normalizzazione, validazione, deduplica, pesatura iniziale, embedding | batch, `document`, Qdrant |
| 5 | **API CRUD nodi/archi servizi** | Endpoint `/api/v1/...` per gestione catalogo; DTO IT/EN | `localmind-api`, domain |
| 6 | **Editor catalogo + moderazione** | UI per creare/modificare servizi e approvare contributi | frontend feature `servizi` |
| 7 | **Assistente conversazionale bisogno→servizio (GraphRAG)** | Recupero ibrido, espansione multi-hop, risposta con citazioni | `llm`/GraphRAG, Qdrant, grafo |
| 8 | **Ricerca/filtri catalogo** | Ricerca testuale + filtri (categoria, area, gratuito, online) | frontend, search |
| 9 | **Scheda servizio completa** | Vista con percorso, prerequisiti, documenti, costi, sedi, contatti | frontend |
| 10 | **Contributi & feedback community** | Form contributo, voto utilità, segnalazione obsoleto | frontend, domain, `auth` |
| 11 | **Disclaimer & gestione emergenza** | Testi legali, rilevamento urgenza, rinvio a 112/118 | `llm` guardrail, config |
| 12 | **Bilinguismo IT/EN** | UI, enum tradotte, contenuti chiave | i18n (vincolo di progetto) |

### 7.2 Evoluzioni future

| Funzionalità | Valore aggiunto |
|--------------|-----------------|
| **Connettore FHIR / FSE 2.0** | Interoperabilità sanitaria, allineamento prestazioni e provider directory |
| **Connettore CUP + tempi d'attesa** | Orientamento dinamico verso sedi con minore attesa |
| **Visualizzazione interattiva del grafo** | Esplorazione visiva dei percorsi servizio↔bisogno↔prerequisiti |
| **Orientamento personalizzato avanzato** | Profilo cittadino opt-in (ISEE, fragilità) per matching fine |
| **PDTA / percorsi guidati** | Procedure multi-step per pazienti cronici e pratiche complesse |
| **Suggerimento automatico di collegamenti mancanti** | L'AI propone archi non evidenti tra bisogni e servizi |
| **Reputazione & ranking emergente** | I servizi migliori emergono da uso reale e feedback |
| **Canali in/out (WhatsApp, email, vocale)** | Accesso multicanale per persone fragili (dominio `messaging`/`email`) |
| **Promemoria e follow-up** | Scadenze (rinnovo esenzione, richiamo visita) via `calendar` |
| **Pacchetto/modulo installabile "Servizi & sanità locale"** | Distribuzione via marketplace per altri Comuni/ASL |
| **Analisi delle lacune (gap analysis)** | Report sui bisogni non coperti per i decisori pubblici |
| **Accessibilità avanzata (WCAG, voce)** | Inclusione di anziani e persone con disabilità |

### 7.3 Da mantenere (manutenzione evolutiva continua)

- Aggiornamento dei connettori al variare di API/formati delle fonti (HSDS, FHIR, open data PA).
- Aggiornamento di tassonomie bisogni/categorie e dei mapping agli standard.
- Ricalcolo periodico dei pesi e applicazione del decadimento per freschezza.
- Moderazione continua dei contributi e gestione segnalazioni.
- Aggiornamento dei disclaimer e dei riferimenti normativi (FSE 2.0, scadenze regolatorie).
- Sincronizzazione delle enum tradotte IT/EN verso il frontend (vincolo di progetto).
- Migrazioni Flyway incrementali (una query per file) all'evolvere dello schema.

---

## 8. Casi d'uso AI / GraphRAG

1. **Orientamento dal bisogno (caso principe).** *"Mia madre è anziana e non autosufficiente, vivo a [comune], cosa posso fare?"* → l'AI mappa il bisogno, recupera ADI, assistenza domiciliare, indennità di accompagnamento, centri diurni; espande prerequisiti (valutazione UVM, ISEE) e restituisce il percorso con documenti, sedi e contatti, citando i nodi.
2. **Catena di prerequisiti.** *"Come prenoto una visita cardiologica?"* → percorso: scelta MMG → impegnativa → prenotazione CUP (deep link) → ticket/esenzione (PagoPA) → sede e preparazione, con alternative private accreditate.
3. **Domanda comparativa cross-grafo.** *"Quali servizi gratuiti per il supporto psicologico ci sono entro 5 km?"* → query con filtri (gratuito, distanza) e ranking pesato.
4. **Disambiguazione guidata.** Bisogno vago → l'AI pone una domanda mirata e poi instrada (vedi §4.2 step 2).
5. **Collegamenti non evidenti.** L'AI suggerisce a un caregiver servizi correlati che non avrebbe cercato (es. "sportello sollievo", gruppi di supporto) tramite relazioni `CORRELATO_A`/`SODDISFA`.
6. **Sintesi multi-documento.** Da volantini/PDF caricati estrae e collega le informazioni al grafo (riuso `document` + GraphRAG).
7. **Assistente per l'operatore di sportello.** Risposte rapide e tracciabili con citazione della fonte, per uniformare il front-office.
8. **Gap analysis per decisori.** *"Quali bisogni risultano non coperti nel nostro territorio?"* → l'AI naviga i `Bisogno` senza archi `SODDISFA` ad alto peso.

In tutti i casi: AI **Ollama in locale** di default, risposte con **citazione dei nodi/percorsi** (spiegabilità) e **guardrail** (no diagnosi, rinvio all'emergenza).

---

## 9. KPI & metriche di successo

| Categoria | KPI | Obiettivo / direzione |
|-----------|-----|-----------------------|
| Copertura | % bisogni della tassonomia con almeno un servizio collegato | Crescente |
| Copertura | N. servizi/enti/sedi mappati nel bacino | Crescente |
| Qualità dati | % nodi con campi minimi completi | > 90% |
| Freschezza | % informazioni aggiornate entro la soglia | > 80% |
| Efficacia AI | Tasso di risposte giudicate utili (feedback) | Crescente |
| Efficacia AI | % richieste risolte senza intervento umano (deflection) | Crescente |
| Efficacia AI | Lunghezza media del percorso restituito vs. completo (recall prerequisiti) | Alta |
| Esperienza | Tempo medio dal bisogno alla risposta utile | Decrescente |
| Community | N. contributi/correzioni approvati per periodo | Crescente |
| Community | Tempo medio di moderazione | Decrescente |
| Impatto PA | Riduzione richieste di primo livello agli sportelli | Decrescente |
| Affidabilità | % risposte con citazione delle fonti | ~100% |
| Sicurezza | % corretto rilevamento dei casi di emergenza | ~100% |

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---------|---------|-------------|
| **Informazioni obsolete/errate** | Cittadino indirizzato male | Freschezza nel peso, decadimento, "da verificare", multi-fonte, feedback |
| **Responsabilità su consigli sanitari** | Rischio legale/etico | Disclaimer chiari, no diagnosi, rinvio all'emergenza, citazione fonti |
| **Privacy dati cittadino** | Violazione GDPR | Local-first, AI Ollama locale, contesto opt-in non persistito, nessun dato personale nei nodi |
| **Allucinazioni dell'AI** | Risposte inventate | GraphRAG ancorato ai nodi, risposte solo da grafo, citazioni obbligatorie |
| **Frammentazione/instabilità delle fonti** | Ingestione fragile | Connettori isolati (plugin), validazione robusta, monitoraggio fonti |
| **Duplicati e incoerenze** | Grafo sporco | Entity resolution, deduplica, moderazione |
| **Bassa alfabetizzazione digitale degli utenti** | Esclusione | UI conversazionale semplice, multilingua, futuro canale vocale/WhatsApp |
| **Bias/lacune nella tassonomia bisogni** | Bisogni non riconosciuti | Gap analysis, revisione periodica, contributi community |
| **Sovraccarico di moderazione** | Curatela non sostenibile | Code prioritizzate, soglie di reputazione, automazioni di pre-validazione |
| **Disallineamento normativo (FSE 2.0, scadenze)** | Dati/processi non conformi | Manutenzione evolutiva, monitoraggio regolatorio |

---

## 11. Manutenzione & evoluzione

- **Ciclo dati continuo:** ingestione schedulata (batch), ricalcolo pesi, decadimento freschezza, moderazione e feedback chiudono il ciclo §4.1↔§4.2.
- **Governance del catalogo:** ruoli chiari (admin, curatore, contributore) via dominio `auth`; policy di moderazione versionate.
- **Evoluzione dello schema:** nuove tipologie di nodo/relazione introdotte con migrazioni Flyway incrementali (una query per file) e mantenendo retrocompatibilità.
- **Aggiornamento standard:** allineamento a nuove versioni di HSDS/FHIR e alle iniziative italiane (FSE 2.0, DCAT-AP_IT) tramite connettori versionati.
- **Distribuzione come modulo:** impacchettamento dell'ambito come modulo installabile via **marketplace**, riusabile da altri Comuni/ASL con configurazione del perimetro territoriale.
- **Documentazione bilingue:** mantenimento della doc IT (`documentazione/`) ed EN (`documentation/`) e delle enum tradotte verso il frontend.
- **Osservabilità:** metriche §9 esposte (Actuator/Prometheus già presenti) per monitorare copertura, qualità ed efficacia.
- **Sviluppi tracciati:** ogni intervento documentato nella cartella `Sviluppi/` con nomenclatura datata, come da regole di progetto.

---

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo esistente | Ruolo nell'ambito "Servizi & sanità locale" |
|------------------|---------------------------------------------|
| `knowledge` / motore a grafo core | Base del grafo: nodi tipizzati, archi pesati, query multi-hop |
| `llm` (LlmGatewayService) | Comprensione del bisogno, GraphRAG, generazione risposta; **Ollama default** con fallback opzionale |
| Qdrant (vectorstore) | Ricerca semantica su descrizioni di servizi e bisogni; entity resolution |
| MySQL + Flyway | Struttura del grafo, pesi, metadati, provenienza; migrazioni (una query per file) |
| `document` (Tika/OCR) | Ingestione di PDF/volantini e documenti caricati dall'utente |
| `localmind-batch` | Ingestione schedulata, folder scan, job di sincronizzazione fonti |
| `plugin` (PF4J) + extension point | Connettori di ingestione isolati (`ServiceDirectoryConnectorExtension`) |
| `marketplace` | Distribuzione del modulo "Servizi & sanità locale" ad altri enti |
| `auth` | Ruoli e permessi: cittadino (read), operatore/curatore (edit/moderazione), admin |
| `calendar` | Promemoria e scadenze (rinnovo esenzioni, richiami visite) |
| `messaging` / `email` | Canali multicanale di accesso e notifica (WhatsApp/Telegram, email) |
| `mcp` | Esposizione di tool (query catalogo, lookup servizio) verso agenti/assistenti esterni |
| `automation` | Regole automatiche (es. marcatura "da verificare" oltre soglia, alert lacune) |
| `agent` | Agenti che orchestrano percorsi complessi (orientamento + promemoria + notifica) |
| `finetuning` | Adattamento del modello locale al lessico burocratico/sanitario del territorio |
| Frontend Angular (feature `servizi`) | Editor catalogo, scheda servizio, assistente conversazionale, contributi, filtri |
| `common` (eventi, analytics) | Eventi di dominio per aggiornamenti grafo; metriche di copertura/efficacia |

L'ambito **non introduce nuova infrastruttura**: riusa MySQL + Qdrant (niente Neo4j), l'AI locale Ollama, l'architettura esagonale (dominio puro wired in `DomainConfig`), il sistema di plugin e il marketplace, rispettando i vincoli di progetto (local-first, privacy, open source, bilinguismo IT/EN, Flyway con una sola query per file).
