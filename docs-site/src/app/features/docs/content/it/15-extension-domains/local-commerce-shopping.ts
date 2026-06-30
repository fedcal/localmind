export const content = `# Commercio & shopping locale

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema concreto

Il commercio di prossimità — negozi di vicinato, botteghe artigiane, mercati rionali, produttori di tipicità — è il tessuto economico e culturale dei territori, ma è anche il segmento più penalizzato dalla digitalizzazione attuale. Tre fenomeni convergono e producono un danno cumulativo:

1. **Invisibilità digitale.** La maggior parte dei piccoli esercenti non ha un sito, oppure ha una scheda Google Business Profile incompleta, dati di orario non aggiornati, nessun catalogo strutturato. Quando un cliente cerca "dove comprare formaggio di malga a 5 km da qui" o "calzolaio che ripara stivali in centro", i risultati premiano le grandi piattaforme e i marketplace internazionali, non la bottega a 300 metri. In Italia gli acquisti online supereranno i 40 miliardi di euro nel 2026 (+6% su base annua), ma nessun marketplace locale figura nella top 10: il valore generato dal territorio defluisce verso operatori extraterritoriali.

2. **Frammentazione dei dati.** Le informazioni su un negozio sono sparse e incoerenti: orari su Google, prezzi su un volantino cartaceo, disponibilità prodotti solo "chiedendo in negozio", recensioni su tre piattaforme diverse, eventi di mercato pubblicati su una pagina Facebook comunale. Nessuno tiene insieme *negozio → prodotto → artigiano → materia prima → evento → quartiere → cliente*. Mancano proprio le **relazioni** tra queste entità, che sono ciò che rende possibile la scoperta intelligente.

3. **Asimmetria tecnologica e di costi.** Le soluzioni esistenti (marketplace SaaS, gestionali cloud, agentic commerce dei big tech) impongono commissioni, lock-in del dato, invio dei dati commerciali a server esterni e una visibilità mediata da algoritmi opachi. Per un artigiano questo significa cedere il margine e perdere il controllo della relazione con il cliente. Per un Comune o una Pro Loco significa non poter offrire un servizio di valorizzazione del territorio senza dipendere da un fornitore esterno.

L'evoluzione 2026 dell'e-commerce verso l'**agentic commerce** e i **product knowledge graph** (la "scoperta prima della ricerca", dove un grafo decide quali prodotti emergono nelle risposte dell'AI) rende il problema urgente: se il commercio locale non è rappresentato in modo strutturato e relazionale, sarà letteralmente assente dalle risposte degli assistenti AI che i clienti useranno per decidere cosa comprare e dove.

### 1.2 La soluzione LocalMind

LocalMind affronta il problema non costruendo "l'ennesimo marketplace", ma applicando il suo **motore di knowledge graph universale, pesato e navigabile dall'AI** al dominio del commercio locale. La differenza è sostanziale:

- Invece di un catalogo piatto di prodotti, modelliamo un **grafo pesato** che collega negozi, artigiani, prodotti, materie prime, categorie merceologiche, mercati, eventi, quartieri, certificazioni e clienti. Il grafo cattura *perché* un prodotto è rilevante per un cliente: vicinanza, stagionalità, filiera corta, qualità artigianale, complementarità con altri acquisti, appartenenza a una tradizione locale.
- L'AI (GraphRAG) **naviga** questo grafo per rispondere a domande complesse e far emergere collegamenti non evidenti ("quali botteghe usano farine del mulino locale?", "comporre un cesto regalo di sole tipicità del mio quartiere sotto i 40 €", "trova un artigiano che ripara questo oggetto vintage").
- Tutto resta **local-first e self-hostable**: un Comune, un Distretto del Commercio, una rete di botteghe o una singola associazione possono ospitare la propria istanza, con AI locale (Ollama) di default. **I dati commerciali non lasciano mai l'infrastruttura senza consenso esplicito** — requisito decisivo per esercenti gelosi dei propri dati di vendita e per la conformità privacy.

### 1.3 Valore per stakeholder

| Stakeholder | Valore generato |
|-------------|-----------------|
| Piccolo esercente / artigiano | Visibilità digitale strutturata a costo zero di commissione, controllo del dato, scoperta tramite AI, vetrina relazionale (non solo "scheda") |
| Cliente / consumatore | Scoperta intelligente del commercio di prossimità, risposte AI a domande reali ("cosa compro qui di tipico?"), filiera trasparente |
| Comune / Distretto del Commercio / Pro Loco | Strumento di valorizzazione del territorio self-hosted, mappa viva del commercio locale, dati aggregati per politiche commerciali |
| Rete di botteghe / consorzio di tipicità | Promozione collettiva della filiera, cross-selling tra esercenti complementari, narrazione della tradizione |
| Community territoriale | Contributi dal basso (recensioni, segnalazioni, foto), "Wikipedia del commercio locale" |

### 1.4 Perché ora e perché diverso dai competitor

I modelli analizzati (Google Knowledge Panel + AI Overview per i local business, product knowledge graph dei big retailer, marketplace di prossimità in franchising tipo "Comuni a Domicilio", piattaforme di handmade tipo Etsy) hanno tutti almeno uno di questi limiti: sono **cloud-only**, **commissionali**, **proprietari del dato**, oppure **non relazionali** (catalogo piatto). LocalMind è l'unico approccio che combina: grafo pesato relazionale + AI locale + self-hosting + open source + bilingue + estendibilità a plugin. Non compete sul prezzo o sulla logistica, ma sulla **sovranità del dato territoriale e sulla qualità della scoperta**.

## 2. Personas & utenti target

| Persona | Ruolo | Obiettivi | Bisogni / frustrazioni |
|---------|-------|-----------|------------------------|
| **Giulia, bottegaia artigiana** (45) | Titolare di una bottega di ceramica | Farsi trovare, raccontare la lavorazione, vendere senza commissioni | Non ha tempo né competenze per gestire siti/marketplace; teme di cedere dati e margine |
| **Marco, norcino/produttore tipico** (52) | Produttore di salumi a filiera corta | Valorizzare la materia prima locale, vendere ai mercati e su prenotazione | Dati sparsi, stagionalità non comunicata, nessun legame visibile con i ristoranti che usano i suoi prodotti |
| **Sara, cliente consapevole** (34) | Consumatrice attenta a prossimità e sostenibilità | Trovare il negozio giusto vicino, comprare locale, regali tipici | Cerca su Google e trova solo grandi catene; non sa quali botteghe esistono nel quartiere |
| **Luca, assessore al commercio** (50) | Comune medio-piccolo | Valorizzare il commercio di prossimità, avere una mappa viva | Non vuole un fornitore SaaS che si appropria dei dati del territorio; budget limitato |
| **Elena, gestore Distretto del Commercio** (40) | Coordina una rete di esercenti | Promozione collettiva, eventi, cross-selling tra botteghe | Strumenti di marketing frammentati, nessuna vista d'insieme della filiera |
| **Davide, volontario Pro Loco / curatore community** (29) | Cura i contenuti del territorio | Mantenere aggiornati negozi, mercati, eventi | Aggiornamenti manuali, nessun flusso di contributi dalla community |
| **Anna, food blogger / creator locale** (31) | Racconta il territorio | Collegare i propri contenuti a negozi e prodotti reali | Nessun aggancio strutturato tra storytelling e dati commerciali |

**Utenti tecnici (deployer):** amministratori IT comunali, sviluppatori civic-tech, system integrator che installano e configurano l'istanza self-hosted; contributor open source che sviluppano connettori e plugin di dominio.

## 3. Requisiti in input

Questa sezione definisce in dettaglio **quali dati il sistema deve poter acquisire**, da chi, con quali vincoli di qualità, validazione e privacy. È il contratto di ingresso del dominio "Commercio & shopping locale".

### 3.1 Entità anagrafiche di base (negozio / esercente)

Per ogni esercizio commerciale il sistema deve poter raccogliere:

| Campo | Obbligatorietà | Tipo / vincolo | Note |
|-------|----------------|----------------|------|
| Ragione sociale / insegna | Obbligatorio | Stringa | Insegna pubblica + denominazione legale |
| Tipologia attività | Obbligatorio | Enum tradotta IT/EN | Es. alimentari, abbigliamento, artigianato, servizi, ferramenta… |
| Partita IVA / codice fiscale | Opzionale (consigliato) | Stringa validata | Per disambiguazione e verifica; mai esposta pubblicamente di default |
| Indirizzo completo | Obbligatorio | Struttura via/civico/CAP/città | Geocodificabile |
| Coordinate geografiche | Derivato | lat/lon (decimal) | Da geocoding dell'indirizzo o input manuale |
| Quartiere / zona / via commerciale | Opzionale | Riferimento a nodo Quartiere | Abilita query di prossimità |
| Contatti | Obbligatorio almeno uno | Telefono, email, sito, social | Validati per formato |
| Orari di apertura | Obbligatorio | Struttura settimanale + eccezioni | Festivi, chiusure stagionali, turni |
| Modalità di vendita | Obbligatorio | Enum multipla IT/EN | In negozio, asporto, consegna locale, prenotazione, spedizione |
| Lingue parlate | Opzionale | Lista | Utile per turismo |
| Accessibilità | Opzionale | Enum multipla IT/EN | Accesso disabili, parcheggio, pet-friendly |
| Metodi di pagamento | Opzionale | Enum multipla IT/EN | Contanti, carte, buoni locali |
| Descrizione / storia | Opzionale | Testo libero | Materiale per embedding semantico e storytelling |
| Foto / media | Opzionale | File (immagini) | Vetrina, prodotti, ambiente |
| Certificazioni / riconoscimenti | Opzionale | Riferimenti a nodi Certificazione | DOP, IGP, "Bottega storica", presidio Slow Food… |

### 3.2 Catalogo prodotti / servizi

| Campo | Obbligatorietà | Tipo / vincolo | Note |
|-------|----------------|----------------|------|
| Nome prodotto/servizio | Obbligatorio | Stringa | |
| Categoria merceologica | Obbligatorio | Riferimento a nodo Categoria (tassonomia) | Tassonomia gerarchica IT/EN |
| Descrizione | Opzionale | Testo libero | Embedding semantico |
| Prezzo / fascia di prezzo | Opzionale | Decimal o enum fascia | Il prezzo può essere "su richiesta" |
| Disponibilità | Opzionale | Enum IT/EN | Disponibile, su ordinazione, esaurito, stagionale |
| Stagionalità | Opzionale | Periodo / mesi | Cruciale per tipicità alimentari |
| Materie prime / ingredienti | Opzionale | Riferimenti a nodi Materia prima | Abilita filiera corta |
| Provenienza / filiera | Opzionale | Riferimento a Produttore/territorio | Filiera corta, km0 |
| Tag attributi | Opzionale | Lista (bio, vegano, fatto a mano, gluten-free…) | Enum IT/EN |
| Personalizzabile | Opzionale | Booleano | Per artigianato su misura |
| Media prodotto | Opzionale | File immagini | |

### 3.3 Mercati ed eventi commerciali

- Mercati rionali e fiere: nome, periodicità (cadenza settimanale/mensile/annuale), giorni e orari, ubicazione (piazza/area), elenco banchi/espositori presenti.
- Eventi: sagre, mercatini stagionali, "notti bianche del commercio", degustazioni, laboratori artigiani, saldi coordinati di distretto: data/e, luogo, esercenti partecipanti, prodotti/temi.

### 3.4 Contributi dalla community

- Recensioni e valutazioni (con punteggio e testo libero), con autore identificato o pseudonimo.
- Segnalazioni: nuovo negozio, dato errato, chiusura, cambio orario.
- Foto e media caricati dagli utenti.
- Voti/upvote su negozi, prodotti, recensioni (alimentano il ranking emergente).
- Tag e collegamenti suggeriti ("questo prodotto usa materie prime di…").

### 3.5 Regole di validazione e qualità del dato (boundary di ingresso)

Coerentemente con le regole di progetto (validazione a tutti i confini, fail-fast, mai fidarsi di dati esterni):

- **Validazione di formato:** P.IVA (checksum), email, telefono, URL, coordinate in range, prezzi non negativi, orari coerenti (apertura < chiusura).
- **Validazione semantica:** categoria appartenente alla tassonomia; enum ammesse; riferimenti a nodi esistenti (no archi pendenti).
- **Deduplica:** rilevamento di esercenti duplicati per somiglianza (nome + indirizzo + P.IVA) prima della creazione del nodo.
- **Stato di verifica:** ogni nodo ha uno stato (\`bozza\` / \`in revisione\` / \`verificato\` / \`rivendicato dall'esercente\` / \`segnalato\`) che pesa sulla fiducia.
- **Provenienza del dato:** ogni dato porta la sua fonte (esercente, community, connettore esterno, import) e timestamp, per tracciabilità e per il calcolo del peso.
- **Privacy by design:** dati sensibili (P.IVA, contatti privati, dati clienti) non esposti pubblicamente di default; consenso esplicito per qualsiasi sincronizzazione verso servizi esterni o provider LLM cloud. AI locale di default.
- **i18n:** ogni etichetta enum e ogni tassonomia disponibile in IT ed EN; i testi liberi conservano la lingua originale con flag di lingua.

### 3.6 Modalità di acquisizione

L'input può arrivare in quattro modi, in ordine crescente di automazione: (1) **inserimento manuale** via UI (esercente o curatore); (2) **rivendicazione e completamento** di un nodo pre-esistente da parte dell'esercente; (3) **import bulk** (CSV/JSON, es. elenco esercenti di un Comune); (4) **connettori automatici** (vedi §6) che generano nodi/archi da fonti esterne. Ogni modalità passa per le stesse regole di validazione (§3.5).

## 4. Flusso dell'attività (step-by-step)

Descriviamo i flussi principali end-to-end. Sono la base per definire endpoint, servizi di dominio e componenti frontend.

### 4.1 Flusso A — Onboarding e mappatura del territorio (deployer + curatore)

1. **Installazione istanza** self-hosted (Docker compose: MySQL + Qdrant + Ollama). Selezione del modulo di dominio "Commercio & shopping locale" dal marketplace plugin.
2. **Configurazione territorio:** definizione dell'area (Comune/quartieri), import della tassonomia merceologica IT/EN di base e delle certificazioni rilevanti.
3. **Seed iniziale:** import bulk di un elenco esercenti (CSV comunale) → pipeline di validazione (§3.5) → creazione nodi \`Negozio\` in stato \`bozza\`, geocoding, deduplica.
4. **Arricchimento via connettori:** esecuzione dei connettori (§6) per completare orari, recensioni pubbliche, link → creazione/aggiornamento archi.
5. **Indicizzazione semantica:** descrizioni e cataloghi vengono chunked + embeddati su Qdrant; il grafo strutturale è popolato su MySQL.
6. **Pubblicazione:** il curatore promuove i nodi da \`bozza\` a \`verificato\`; l'istanza è navigabile.

### 4.2 Flusso B — L'esercente rivendica e gestisce la sua vetrina

1. L'esercente trova la propria scheda (o la crea) e avvia la **rivendicazione** (claim) con verifica (codice, email del dominio, o approvazione del curatore).
2. Una volta verificato, accede a un'area di gestione: completa anagrafica, **carica il catalogo prodotti** (manuale, CSV, o connettore dal proprio gestionale/e-commerce — vedi §6), imposta orari, foto, stagionalità.
3. Definisce **relazioni di filiera**: collega i propri prodotti a materie prime/produttori ("uso farine del Mulino X"), ad altre botteghe complementari, a eventi a cui partecipa.
4. Ogni modifica passa per la **validazione** e genera un evento di dominio (\`NodoCommercioAggiornato\`) che innesca re-embedding e ricalcolo pesi.
5. L'esercente vede **analytics**: quante volte la sua vetrina è emersa nelle risposte AI, quali query l'hanno trovato, quali collegamenti la community gli ha suggerito.

### 4.3 Flusso C — Il cliente scopre il commercio locale (consumer, GraphRAG)

1. Il cliente pone una **domanda in linguaggio naturale** nella chat ("dove trovo pane a lievitazione naturale aperto adesso vicino a Porta Nuova?") oppure usa la **ricerca/navigazione del grafo**.
2. Il sistema combina: (a) **filtri strutturali** sul grafo (prossimità, orario di apertura corrente, categoria, disponibilità), (b) **ricerca semantica** su Qdrant (intento "lievitazione naturale, artigianale"), (c) **espansione sul grafo** per relazioni pesate (filiera, complementarità, qualità emergente).
3. GraphRAG seleziona i nodi/percorsi più rilevanti, l'AI locale **genera la risposta citando i nodi** (negozi/prodotti) e i criteri ("aperto ora, a 400 m, usa farine locali, valutazione community alta").
4. Il cliente esplora il **grafo interattivo**: dal negozio espande a prodotti, a eventi, a botteghe vicine complementari; filtra per tipo di nodo/relazione.
5. Azioni: salva, contatta l'esercente, aggiunge a un itinerario di shopping, lascia recensione/voto (→ Flusso D).

### 4.4 Flusso D — Contributo della community e ranking emergente

1. Un utente aggiunge recensione/voto/foto o segnala una correzione su un nodo.
2. Il contributo entra in stato \`in revisione\`; passa per **moderazione** (automatica su regole + coda di curatela per casi dubbi).
3. Una volta accettato, il contributo **aggiorna i pesi** degli archi (es. qualità percepita, popolarità) e la fiducia del nodo.
4. Il **ranking emergente** ricalcola: i negozi/prodotti con più segnali positivi e relazioni rilevanti emergono di più nelle risposte AI e nella navigazione.
5. I contributi controversi o segnalati vengono escalati al curatore; viene tenuto un audit trail immutabile (nuova versione, mai mutazione in-place).

### 4.5 Flusso E — Promozione collettiva e cross-selling (distretto/consorzio)

1. Il gestore del distretto crea un **evento** o una **campagna** (es. "Settimana del gusto locale") collegando esercenti e prodotti.
2. L'AI suggerisce **collegamenti non evidenti** per il cross-selling ("queste 3 botteghe hanno filiere complementari per un cesto regalo").
3. Vengono generati **itinerari di shopping** tematici sul grafo (a piedi, per quartiere, per filiera).
4. Le metriche di campagna (visibilità, scoperte generate) tornano al gestore e agli esercenti.

### 4.6 Gestione degli errori lungo i flussi

In ogni flusso: validazione fail-fast con messaggi utente chiari e bilingui; errori loggati lato server con contesto; nessun errore silenziato; rollback transazionale sulla creazione di nodi/archi; coda di retry per connettori esterni; degradazione elegante della risposta AI quando il grafo è scarno ("ho trovato pochi dati su questa zona, ecco cosa so").

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il dominio riusa il **motore a grafo universale** (nodi tipizzati + archi pesati su MySQL per la struttura, Qdrant per la semantica). Di seguito i tipi specifici dell'ambito. I tipi sono estendibili via schema modulare e tutte le enum sono tradotte IT/EN.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi chiave |
|--------------|-------------|------------------|
| \`Negozio\` (Esercizio) | Punto vendita fisico | insegna, tipologia, indirizzo, geo, orari, modalità vendita, stato verifica |
| \`Artigiano\` / \`Produttore\` | Persona/laboratorio che produce | nome, mestiere, tecniche, territorio |
| \`Prodotto\` | Bene venduto | nome, prezzo/fascia, disponibilità, stagionalità, attributi |
| \`Servizio\` | Prestazione (riparazione, su misura…) | nome, fascia prezzo, tempi |
| \`CategoriaMerceologica\` | Nodo di tassonomia gerarchica | nome IT/EN, parent |
| \`MateriaPrima\` / \`Ingrediente\` | Input della filiera | nome, origine, stagionalità |
| \`Mercato\` | Mercato rionale/fiera ricorrente | periodicità, luogo, giorni |
| \`Evento\` | Evento commerciale puntuale | data, luogo, tema |
| \`Quartiere\` / \`ZonaCommerciale\` | Area geografica | nome, confini/centro, via commerciale |
| \`Certificazione\` / \`Riconoscimento\` | Marchio di qualità | tipo (DOP, IGP, bottega storica…), ente |
| \`Cliente\` / \`Utente\` | Profilo consumatore (privacy-protetto) | preferenze, area, storico opt-in |
| \`Recensione\` | Contributo valutativo | punteggio, testo, autore, data |
| \`Tradizione\` / \`Tema\` | Filo narrativo (es. "ceramica locale") | descrizione, periodo |

### 5.2 Tipi di relazione (archi)

| Relazione | Da → A | Significato |
|-----------|--------|-------------|
| \`VENDE\` | Negozio → Prodotto/Servizio | Il negozio offre il prodotto |
| \`PRODOTTO_DA\` | Prodotto → Artigiano/Produttore | Chi lo realizza |
| \`USA_MATERIA_PRIMA\` | Prodotto → MateriaPrima | Filiera / ingredienti |
| \`FORNISCE\` | Produttore → Negozio | Rapporto di fornitura (filiera corta) |
| \`APPARTIENE_A_CATEGORIA\` | Prodotto/Negozio → CategoriaMerceologica | Classificazione |
| \`SITUATO_IN\` | Negozio/Mercato → Quartiere | Localizzazione |
| \`VICINO_A\` | Negozio ↔ Negozio | Prossimità geografica |
| \`COMPLEMENTARE_A\` | Negozio/Prodotto ↔ Negozio/Prodotto | Cross-selling / acquisti combinati |
| \`PARTECIPA_A\` | Negozio/Artigiano → Mercato/Evento | Presenza a mercati/eventi |
| \`HA_CERTIFICAZIONE\` | Negozio/Prodotto → Certificazione | Qualità riconosciuta |
| \`RECENSISCE\` | Cliente → Negozio/Prodotto (via Recensione) | Feedback |
| \`PREFERISCE\` / \`HA_ACQUISTATO\` | Cliente → Negozio/Prodotto | Personalizzazione (opt-in) |
| \`INCARNA_TRADIZIONE\` | Negozio/Prodotto → Tradizione/Tema | Narrazione territoriale |
| \`SIMILE_A\` | Prodotto ↔ Prodotto | Similarità semantica (da embedding) |

### 5.3 Criteri di peso degli archi

Il peso (valore normalizzato, es. 0–1) rende il grafo *navigabile con priorità*. È **derivato e ricalcolato**, mai inserito a mano arbitrariamente, da fattori configurabili per tipo di relazione:

| Fattore | Si applica a (esempi) | Logica di peso |
|---------|-----------------------|----------------|
| **Prossimità geografica** | \`VICINO_A\`, \`SITUATO_IN\` | Peso decrescente con la distanza (più vicino = più forte) |
| **Frequenza/co-occorrenza** | \`COMPLEMENTARE_A\`, \`HA_ACQUISTATO\` | Più spesso due nodi sono collegati/co-acquistati, più alto il peso |
| **Qualità community** | \`RECENSISCE\`, ranking Negozio/Prodotto | Media voti + numero + recency; aumenta peso e visibilità |
| **Affidabilità della fonte** | tutti | Esercente verificato/claim > connettore > community anonima; modula il peso |
| **Stato di verifica** | nodi e archi | \`verificato\`/\`rivendicato\` pesa più di \`bozza\` |
| **Recency / freschezza** | orari, disponibilità, eventi | Dato recente pesa più di dato vecchio (decadimento temporale) |
| **Forza di filiera** | \`USA_MATERIA_PRIMA\`, \`FORNISCE\` | Filiera corta/km0 e dichiarazione bilaterale aumentano il peso |
| **Rilevanza semantica** | \`SIMILE_A\`, match query | Cosine similarity da Qdrant tra descrizioni/intento |
| **Stagionalità** | \`VENDE\`, \`Prodotto\` | Boost dei prodotti in stagione nel periodo corrente |
| **Feedback d'uso AI** | tutti | Archi/nodi che hanno portato a risposte utili (click, salvataggi) si rafforzano |

Il peso finale di un arco è una combinazione (configurabile per dominio) di questi fattori. La funzione di peso è immutabile nel senso dello stile di progetto: ogni ricalcolo produce un nuovo valore versionato, l'audit conserva lo storico.

## 6. Fonti dati & connettori (ingestione)

L'ingestione riusa la pipeline esistente (Spring Batch, estrazione Tika/OCR, chunking, embedding, Qdrant) e il sistema di plugin PF4J per i connettori specifici del dominio.

| Fonte | Tipo connettore | Cosa estrae | Modalità |
|-------|-----------------|-------------|----------|
| Inserimento manuale UI | Nativo | Tutte le entità | On-demand |
| Import CSV/JSON (elenchi comunali, camere di commercio) | Import bulk | Anagrafiche esercenti, categorie | Batch on-demand |
| Open Data territoriali (Comuni, regioni, mercati pubblici) | Connettore plugin | Mercati, eventi, anagrafiche pubbliche | Schedulato |
| Schede attività pubbliche / mappe | Connettore plugin | Orari, contatti, recensioni pubbliche, geo | Schedulato (rispetto ToS/robots) |
| Gestionale / e-commerce dell'esercente (CSV, API, feed) | Connettore plugin per esercente | Catalogo prodotti, prezzi, disponibilità | Schedulato/webhook |
| Volantini, listini, menù (PDF/immagini) | Pipeline documenti (Tika + OCR Tesseract) | Prodotti, prezzi → estrazione + AI structuring | Batch |
| Social / pagine eventi | Connettore plugin | Eventi, mercatini, promozioni | Schedulato |
| Email (offerte, listini fornitori) | Dominio \`email\` (IMAP) | Listini, disponibilità | Schedulato |
| Contributi community | Nativo | Recensioni, foto, segnalazioni, voti | Event-driven |

Ogni connettore: produce nodi/archi candidati, passa per validazione/deduplica (§3.5), registra provenienza e timestamp, rispetta i vincoli legali/ToS delle fonti, e funziona **senza inviare dati a servizi esterni** (estrazione e structuring tramite AI locale Ollama di default).

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

Mappa concreta del lavoro, distinguendo MVP, evoluzioni e manutenzione. Le funzionalità riusano il motore a grafo core; qui sono elencate le parti **specifiche del dominio commercio**.

### 7.1 MVP (prima release del modulo)

| # | Funzionalità | Layer | Dettaglio |
|---|-------------|-------|-----------|
| 1 | Schema di dominio commercio | domain + Flyway | Tipi nodo/arco §5, enum IT/EN, tassonomia merceologica; migrazioni una-query-per-file |
| 2 | CRUD nodi Negozio/Prodotto/Artigiano | domain port/in + API + UI | Inserimento manuale + validazione §3.5 |
| 3 | Import bulk CSV esercenti | batch | Pipeline import + geocoding + deduplica |
| 4 | Catalogo prodotti per negozio | domain + API + UI | Con categoria, prezzo, disponibilità, stagionalità |
| 5 | Indicizzazione semantica del catalogo | infra (Qdrant) | Chunk + embedding descrizioni/prodotti |
| 6 | Ricerca + filtri base | API + UI | Per categoria, prossimità, "aperto ora" |
| 7 | Chat GraphRAG sul commercio | domain (knowledge) + UI | Domande NL → risposta con citazione nodi |
| 8 | Visualizzazione grafo interattivo | UI | Nodi/archi/peso, espansione, filtri per tipo |
| 9 | Recensioni e voti | domain + API + UI | Con stato moderazione |
| 10 | Calcolo peso archi (v1) | domain | Prossimità + qualità community + recency |
| 11 | Geocoding indirizzi | infra adapter | Da indirizzo a coordinate |
| 12 | i18n IT/EN completo del modulo | full-stack | Enum, tassonomia, UI |

### 7.2 Evoluzioni (release successive)

| # | Funzionalità | Valore |
|---|-------------|--------|
| 13 | Rivendicazione vetrina (claim) + area gestione esercente | Empowerment esercenti, dati di qualità |
| 14 | Connettori esterni (open data, gestionali, social) | Automazione ingestione |
| 15 | Estrazione catalogo da volantini/menù (Tika+OCR+AI structuring) | Onboarding senza sforzo |
| 16 | Ranking emergente avanzato + feedback d'uso AI | Qualità della scoperta |
| 17 | Relazioni di filiera e cross-selling AI | Promozione collettiva |
| 18 | Itinerari di shopping generati dall'AI | Esperienza consumer |
| 19 | Eventi/mercati e campagne di distretto | Strumento per Comuni/distretti |
| 20 | Analytics per esercente e per curatore | Misurazione del valore |
| 21 | Personalizzazione (opt-in) basata su preferenze cliente | Raccomandazioni |
| 22 | Stagionalità e disponibilità in tempo reale | Freschezza del dato |
| 23 | Moderazione assistita dall'AI locale | Scalabilità della curatela |
| 24 | Export/condivisione del sottografo territoriale | Interoperabilità open data |

### 7.3 Da mantenere (manutenzione continua)

- Tassonomia merceologica e enum IT/EN aggiornate.
- Funzioni di peso e parametri di ranking calibrati sui dati reali.
- Connettori allineati a cambiamenti delle fonti/ToS.
- Modelli di embedding e prompt GraphRAG aggiornati con l'evoluzione di Spring AI/Ollama.
- Pipeline di deduplica e qualità del dato.
- Migrazioni Flyway (una query per file) per evoluzioni di schema.
- Copertura test (unit/integration/E2E) ≥ 80% sul modulo.

## 8. Casi d'uso AI / GraphRAG

Esempi di query reali che il GraphRAG deve poter soddisfare combinando filtri strutturali, semantica e navigazione del grafo pesato:

1. **Scoperta di prossimità contestuale:** "Dove compro pane a lievitazione naturale aperto adesso entro 600 m?" → filtro geo + orario + intento semantico + ranking qualità.
2. **Filiera trasparente:** "Quali botteghe usano farine del mulino del paese?" → traversal \`VENDE\`→\`USA_MATERIA_PRIMA\`→\`MateriaPrima\`(mulino locale).
3. **Composizione intelligente:** "Crea un cesto regalo di sole tipicità del quartiere sotto i 40 €" → selezione \`Prodotto\` per categoria/prezzo/prossimità + complementarità.
4. **Riparazione/servizi rari:** "Chi ripara stivali in pelle in centro?" → categoria servizio + competenza artigiano + prossimità.
5. **Cross-selling per esercenti:** "Quali attività sono complementari alla mia per una campagna comune?" → archi \`COMPLEMENTARE_A\` pesati + filiera.
6. **Itinerario di shopping:** "Pomeriggio di shopping locale a piedi tra artigianato e gastronomia" → percorso sul grafo per quartiere/tema.
7. **Collegamenti non evidenti:** "Mostra connessioni nascoste tra i produttori di formaggio e i ristoranti del territorio" → suggerimento archi mancanti.
8. **Stagionalità:** "Cosa di tipico è di stagione adesso e dove lo trovo?" → boost stagionale + disponibilità.
9. **Q&A con citazioni:** ogni risposta cita i nodi/percorsi usati e i criteri (vicinanza, qualità, filiera), come richiesto dal core GraphRAG.

In tutti i casi l'AI locale (Ollama) è il default; i provider cloud restano opzionali e mai usati con dati commerciali sensibili senza consenso esplicito.

## 9. KPI & metriche di successo

| Categoria | KPI | Obiettivo / segnale |
|-----------|-----|---------------------|
| Copertura | N° esercenti/prodotti mappati per territorio | Crescita; % verificati |
| Qualità dato | % nodi \`verificato\`/\`rivendicato\`; % con orari/catalogo completi | Aumento nel tempo |
| Densità grafo | N° archi pesati per nodo; % filiere mappate | Grafo ricco e connesso |
| Scoperta | N° risposte AI che citano esercenti locali; query risolte con successo | Alta utilità |
| Engagement esercenti | % vetrine rivendicate; aggiornamenti per esercente | Adozione |
| Engagement community | N° recensioni/contributi accettati; tasso moderazione | Vitalità community |
| Impatto consumer | Scoperte → contatti/salvataggi/itinerari | Conversione alla visita |
| Performance | Latenza query grafo + GraphRAG; tempo ingestione | Esperienza fluida |
| Privacy/local-first | % elaborazioni in locale; zero dati sensibili verso cloud non consentito | Conformità |
| Affidabilità connettori | Tasso di successo job di ingestione | Stabilità |

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---------|---------|-------------|
| Dato sparso/incompleto nei territori poco coperti | Risposte AI deboli | Connettori + import bulk + contributi community; degradazione elegante della risposta |
| Qualità/affidabilità dei dati esterni | Nodi errati/duplicati | Validazione fail-fast, deduplica, stato di verifica, provenienza tracciata |
| Adozione lenta degli esercenti | Vetrine non rivendicate | Onboarding a basso attrito (claim, estrazione da volantini), valore mostrato via analytics |
| Spam/recensioni false | Ranking distorto | Moderazione (regole + AI locale), peso per affidabilità fonte, audit trail |
| Vincoli legali/ToS delle fonti | Rischio compliance | Connettori che rispettano robots/ToS, preferenza open data, consenso esplicito |
| Privacy dati commerciali e clienti | Fiducia compromessa | Local-first, AI locale default, nessun invio cloud senza consenso, dati sensibili non pubblici |
| Scalabilità query grafo su MySQL+Qdrant (no Neo4j) | Latenza su grafi grandi | Indici mirati, query a profondità limitata, caching (Caffeine), pre-calcolo pesi/vicini |
| Bias del ranking verso i più attivi | Esercenti penalizzati | Bilanciare recency/novità, "fairness boost" ai nuovi nodi verificati |
| Manutenzione tassonomia/enum IT/EN | Drift dei dati | Processo di revisione, test di coerenza enum bilingui |

## 11. Manutenzione & evoluzione

- **Schema evolutivo:** nuovi tipi di nodo/relazione si aggiungono via schema modulare e migrazioni Flyway (una query per file); le enum restano sincronizzate IT/EN verso il frontend.
- **Calibrazione continua del peso/ranking:** revisione periodica dei parametri sulla base dei KPI (§9) e del feedback d'uso AI; ogni modifica versionata.
- **Connettori come plugin PF4J:** ciclo di vita indipendente, aggiornabili senza toccare il core; pubblicabili sul marketplace.
- **Aggiornamento modelli AI:** prompt GraphRAG e modelli di embedding rivisti con le nuove versioni di Spring AI/Ollama; valutazioni di qualità delle risposte.
- **Qualità del dato:** job periodici di deduplica, verifica freschezza (orari/disponibilità), riconciliazione fonti.
- **Documentazione bilingue:** mantenere allineate le versioni IT (\`documentazione/\`) ed EN (\`documentation/\`); aggiornare la cartella \`Sviluppi/\` ad ogni intervento, come da regole di progetto.
- **Roadmap di dominio:** dal MVP consumer verso scenari ibridi (turismo + commercio + eventi) sullo stesso grafo, e verso pacchetti distrettuali/consortili installabili dal marketplace.

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo / dominio esistente | Ruolo nell'ambito commercio | Punto di integrazione |
|----------------------------|------------------------------|------------------------|
| \`knowledge\` | Cuore del motore a grafo e GraphRAG | Estensione con tipi nodo/arco del commercio; query e traversal pesati |
| \`llm\` (\`LlmGatewayService\`) | Generazione risposte, structuring, embedding | Ollama default; fallback chain; embedding catalogo |
| \`document\` + batch (Tika/OCR) | Estrazione da volantini/menù/listini | Pipeline ingestione → nodi Prodotto |
| Qdrant (\`vectorstore\`) | Similarità semantica prodotti/intento | Embedding e ricerca per \`SIMILE_A\` e match query |
| MySQL + Flyway | Struttura del grafo (nodi/archi/pesi) e anagrafiche | Nuove tabelle/migrazioni (una query per file), UUID \`CHAR(36)\` |
| \`plugin\` (PF4J) + \`marketplace\` | Connettori e pacchetti di dominio installabili | Connettori open data/gestionali; modulo commercio dal marketplace |
| \`email\` | Ingestione listini/offerte fornitori | Connettore IMAP → nodi Prodotto/disponibilità |
| \`calendar\` | Mercati ed eventi ricorrenti | Sincronizzazione eventi/mercati ↔ nodi Evento/Mercato |
| \`automation\` | Job schedulati di ingestione/ricalcolo pesi | Trigger periodici connettori e ranking |
| \`messaging\` / \`channels\` | Notifiche a esercenti/clienti, contatto | Avvisi su scoperte, claim, eventi |
| \`agent\` | Agenti AI per onboarding/curatela assistita | Estrazione, deduplica, moderazione assistita |
| \`auth\` | Identità esercenti/curatori/clienti | Ruoli e permessi (claim, moderazione) |
| \`common\` (eventi di dominio, analytics) | Eventi \`NodoCommercioAggiornato\`, metriche | Re-embedding, ricalcolo pesi, KPI §9 |
| Frontend Angular (Signals, i18n) | UI vetrine, ricerca, grafo interattivo, gestione esercente | Nuova feature \`commercio\` lazy-loaded, Signal store, switch lingua IT/EN |

L'ambito **non introduce nuova infrastruttura** (niente Neo4j): riusa MySQL + Qdrant, l'architettura esagonale (dominio puro wired in \`DomainConfig\`), il sistema di plugin e la pipeline esistente — coerentemente con i vincoli di progetto (local-first, AI locale di default, privacy, open source, bilingue IT/EN).

---

### Fonti (ricerca 2026)

- [Local Business Google Knowledge Panels: The 2026 Guide](https://www.instantpress.co/blog/google-knowledge-panel-features-for-local-businesses)
- [Product knowledge graphs: The new brain of AI commerce — Kantar](https://kriq.kantarretailiq.com/en/p-iq/insights/blogs/product-knowledge-graphs-the-new-brain-of-ai-commerce)
- [Knowledge Graphs for Retail — Ontotext](https://www.ontotext.com/blog/knowledge-graphs-for-retail/)
- [New tech and tools for retailers in an agentic shopping era — Google](https://blog.google/products/ads-commerce/agentic-commerce-ai-tools-protocol-retailers-platforms/)
- [Discovery Commerce in Retail Media 2026 — Pacvue](https://pacvue.com/blog/discovery-commerce-and-its-impact-in-retail-media-in-2026/)
- [Marketplace locale: il futuro del commercio di prossimità in Italia — Comuni a Domicilio](https://www.comuniadomicilio.it/marketplace-locale-il-futuro-del-commercio-di-prossimita-in-italia/)
- [The Impact of eCommerce on the Handmade Industry — Kadence](https://kadence.com/en-us/knowledge/the-impact-of-ecommerce-on-the-handmade-industry/)
</content>
</invoke>
`;
