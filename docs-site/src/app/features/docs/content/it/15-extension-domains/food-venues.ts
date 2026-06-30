export const content = `# Ristorazione & locali

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

L'ambito **Ristorazione & locali** è un verticale del gruppo **consumer** del motore di Knowledge Graph universale di LocalMind. Estende la "Wikipedia dei luoghi" community-driven con un sottografo dedicato a ristoranti, bar, trattorie, pizzerie, enoteche, cocktail bar, street food, pasticcerie e ogni altro locale dove si mangia e si beve. L'obiettivo non è ricostruire l'ennesima app di prenotazioni o l'ennesimo aggregatore di recensioni, ma rendere la conoscenza gastronomica del territorio **navigabile dall'AI** attraverso un grafo pesato di nodi (locali, piatti, cucine, persone, eventi) e archi (relazioni pesate da feedback, frequenza, qualità). Tutto resta local-first, self-hostable, con AI Ollama di default e dati che non lasciano l'istanza senza consenso.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema concreto

La scoperta di dove mangiare bene è oggi spezzata tra silos che non parlano fra loro e che premiano il marketing più della qualità reale:

- **Frammentazione delle fonti.** Le informazioni su un singolo locale sono sparse tra mappe (orari, posizione), aggregatori di recensioni (giudizi, foto), social (trend, reel virali), siti propri (menù, prezzi spesso non aggiornati), gruppi di messaggistica e passaparola. Nessuno strumento le ricompone in una vista coerente e interrogabile.
- **Recensioni inaffidabili e non contestualizzate.** Le piattaforme mainstream soffrono di recensioni false, incentivate o vendicative, di medie aritmetiche che appiattiscono ("3,8 stelle" non dice nulla), e di assenza di contesto: una recensione entusiasta di chi cerca cibo economico è inutile a chi cerca atmosfera per un anniversario. Il giudizio non è pesato per affidabilità del recensore né per affinità con chi legge.
- **Domande complesse senza risposta.** Le ricerche reali sono multi-vincolo e relazionali: *"un'osteria a meno di 35€ a persona, vicino al teatro, aperta lunedì sera, con opzioni senza glutine, atmosfera tranquilla per parlare, dove fanno una buona cacio e pepe"*. I motori attuali gestiscono male l'incrocio di filtri geografici, di prezzo, di disponibilità, dietetici, di atmosfera e di qualità del singolo piatto.
- **Perdita della conoscenza locale.** Il sapere su trattorie storiche, locali di nicchia, chef che cambiano cucina, piatti stagionali e abbinamenti con eventi del territorio vive nelle teste delle persone e si disperde. Non esiste un bene comune digitale, aperto e auto-ospitabile, che lo conservi.
- **Dipendenza da Big Tech e perdita di sovranità sui dati.** Ristoratori e community sono prigionieri di piattaforme che possiedono i dati, impongono ranking opachi e monetizzano la visibilità. Una pro-loco, un'associazione di commercianti o una guida gastronomica indipendente non hanno uno strumento proprio, gratuito e controllabile.

### 1.2 Il valore di LocalMind

LocalMind affronta questi problemi trasformando la ristorazione locale in un **grafo di conoscenza pesato e navigabile dall'AI**, con tre leve distintive:

| Leva | Cosa cambia | Perché conta |
|------|-------------|--------------|
| **Grafo relazionale** invece di liste piatte | Locali, piatti, cucine, persone, luoghi ed eventi sono nodi collegati da archi pesati | Permette risposte a domande relazionali ("dove mangiare prima del concerto di stasera") e fa emergere collegamenti non evidenti |
| **Peso emergente dalla community** | Il ranking nasce da feedback, affidabilità del recensore, freschezza e coerenza, non dal marketing | Combatte recensioni false e medie ingannevoli; la qualità reale emerge |
| **GraphRAG local-first** | L'AI naviga il grafo + la semantica (Qdrant) per rispondere e citare i nodi usati | Risposte spiegabili, contestualizzate, senza inviare dati a servizi esterni |

Il valore si declina per ciascun attore:

- **Per chi cerca dove mangiare:** una risposta conversazionale, motivata e contestuale ("ti suggerisco X perché vicino al teatro, sotto budget, e tre recensori affidabili con i tuoi stessi gusti lodano la pasta fresca"), invece di una lista da filtrare a mano.
- **Per la community (foodie, recensori, associazioni):** uno strumento aperto dove il contributo di qualità ha peso reale e costruisce un bene comune, non valore per una piattaforma terza.
- **Per i ristoratori:** una presenza basata sui fatti (menù, orari, allergeni aggiornati) e sul merito, non sulla spesa pubblicitaria.
- **Per enti territoriali (pro-loco, comuni, guide indipendenti):** un motore self-hosted per valorizzare l'offerta gastronomica locale, integrabile con eventi, itinerari e turismo.

### 1.3 Differenziazione rispetto allo stato dell'arte

A differenza degli aggregatori centralizzati (che possiedono i dati e ne governano la visibilità) e dei motori di ricerca generativi (che leggono dati strutturati altrui), LocalMind offre un **motore proprietario, auto-ospitabile e open source** in cui l'organizzazione controlla schema, pesi, moderazione e modello AI. Le tendenze 2026 confermano la direzione: oltre un quinto dei consumatori usa già l'AI per scoprire locali, la discovery si sposta su contenuti e raccomandazioni intelligenti, e la "recommendabilità da parte dell'AI" dipende dalla qualità del dato strutturato. LocalMind porta questa capacità *dentro* l'istanza dell'utente, senza lock-in.

---

## 2. Personas & utenti target

| Persona | Descrizione | Bisogni principali | Come usa l'ambito |
|---------|-------------|--------------------|-------------------|
| **Esploratore gastronomico** (consumer) | Cittadino o turista che cerca dove mangiare ora o in un'occasione specifica | Risposte rapide, contestuali, affidabili; filtri per budget, dieta, atmosfera | Chat AI conversazionale, ricerca, navigazione grafo, salvataggio preferiti |
| **Foodie / recensore della community** | Appassionato che contribuisce con recensioni, foto, valutazioni dettagliate | Riconoscimento del contributo di qualità; reputazione; strumenti di recensione granulari | Crea/aggiorna nodi locale e piatto, scrive recensioni multi-dimensione, vota |
| **Curatore / moderatore** | Membro fidato che valida contributi, fonde duplicati, gestisce segnalazioni | Strumenti di moderazione, anti-spam, gestione affidabilità recensori | Coda di moderazione, merge nodi, gestione pesi e reputazione |
| **Ristoratore / gestore del locale** | Titolare che rivendica e aggiorna la scheda del proprio locale | Dati corretti (menù, orari, allergeni), risposta basata sul merito | Claim del nodo locale, aggiornamento attributi e menù, risposta a recensioni |
| **Ente territoriale / pro-loco / guida indipendente** | Organizzazione che valorizza l'offerta gastronomica del territorio | Self-hosting, integrazione con eventi e itinerari, controllo editoriale | Installa l'istanza, collega ristorazione a eventi/luoghi, cura collezioni tematiche |
| **Pianificatore di eventi e gruppi** | Chi organizza cene di gruppo, aziendali, ricorrenze | Incrocio di vincoli (gruppo numeroso, dieta mista, vicino a una sede) | Query complesse GraphRAG, generazione proposte motivate |
| **Amministratore dell'istanza** (enterprise/tecnico) | Chi installa e configura LocalMind on-premise | Configurazione schema dominio, connettori, modello AI, privacy | Gestione modulo PF4J, mappatura connettori, tuning pesi |

L'ambito è prevalentemente **consumer**, ma riusa l'infrastruttura enterprise (connettori, ingestione, privacy) per scenari ibridi come catene di ristorazione che vogliono un grafo interno della propria offerta.

---

## 3. Requisiti in input

Questa sezione definisce con precisione *cosa* serve in ingresso al sistema per popolare e mantenere il sottografo ristorazione. I requisiti sono raggruppati per categoria; ogni campo indica obbligatorietà, tipo, validazione e sorgente.

### 3.1 Dati anagrafici del locale (nodo \`Locale\`)

| Campo | Obblig. | Tipo | Validazione | Note |
|-------|:------:|------|-------------|------|
| \`nome\` | Sì | testo | 2–160 char, no HTML | Nome commerciale |
| \`tipoLocale\` | Sì | enum \`TipoLocale\` | valore nell'enum bilingue | ristorante, trattoria, osteria, pizzeria, bar, cocktail_bar, enoteca, pub, street_food, pasticceria, gelateria, caffetteria, agriturismo, bistrot |
| \`posizione\` | Sì | geo (lat, lon) | lat ∈ [-90,90], lon ∈ [-180,180] | Indirizzo geocodificato |
| \`indirizzo\` | Sì | strutturato | via, civico, CAP, città, provincia, paese | Normalizzato |
| \`orariApertura\` | Consigliato | struttura settimanale | fasce orarie valide, gestione festivi | Per query "aperto ora/lunedì sera" |
| \`fasciaPrezzo\` | Consigliato | enum \`FasciaPrezzo\` | economico/medio/alto/lusso + range €/persona | Doppia rappresentazione simbolica e numerica |
| \`cucineServite\` | Sì | lista rif. nodi \`Cucina\` | almeno 1 | Relazione verso nodi cucina |
| \`contatti\` | Opzionale | telefono, email, sito, social | formato valido per canale | |
| \`servizi\` | Opzionale | set di flag | enum \`ServizioLocale\` | asporto, delivery, prenotazione, accessibilità, parcheggio, dehors, wifi, pet-friendly, accetta carte |
| \`capienza\` | Opzionale | intero | > 0 | Per query gruppi |
| \`statoAttivita\` | Sì | enum | attivo, chiuso_temp, chiuso_def, in_apertura | Default: attivo |
| \`lingua\` | Sì | enum locale | IT/EN minimo | Per i18n dei contenuti |

### 3.2 Menù e piatti (nodi \`Menu\`, \`SezioneMenu\`, \`Piatto\`)

Modello ispirato alla gerarchia schema.org (Menu → MenuSection → MenuItem → Offer), adattata al grafo:

| Campo | Obblig. | Tipo | Validazione | Note |
|-------|:------:|------|-------------|------|
| \`nomePiatto\` | Sì | testo | 2–120 char | |
| \`sezione\` | Consigliato | enum/testo | antipasto, primo, secondo, dolce, bevanda, pizza, cocktail… | |
| \`descrizione\` | Opzionale | testo | max 600 char | Indicizzata in Qdrant per ricerca semantica |
| \`prezzo\` | Consigliato | decimale + valuta | ≥ 0 | Storicizzato per tracciare variazioni |
| \`ingredientiChiave\` | Opzionale | lista rif. nodi \`Ingrediente\` | | Per query "dove fanno X con Y" |
| \`allergeni\` | Consigliato | set enum \`Allergene\` | tassonomia UE 14 allergeni | Filtro dietetico critico |
| \`adattoADieta\` | Opzionale | set enum \`Dieta\` | vegetariano, vegano, senza glutine, halal, kosher, low-carb | Mappa schema.org \`suitableForDiet\` |
| \`stagionalita\` | Opzionale | enum/periodo | | Piatti stagionali |
| \`disponibilita\` | Opzionale | enum | sempre, stagionale, su_prenotazione, esaurito | |

### 3.3 Recensioni e valutazioni (nodo \`Recensione\` + archi pesati)

Le recensioni sono **multi-dimensione** (no voto unico) per alimentare pesi granulari:

| Campo | Obblig. | Tipo | Validazione | Note |
|-------|:------:|------|-------------|------|
| \`autore\` | Sì | rif. nodo \`Persona\` | utente autenticato | Lega alla reputazione |
| \`localeRiferito\` | Sì | rif. nodo \`Locale\` | esistente | |
| \`piattoRiferito\` | Opzionale | rif. nodo \`Piatto\` | esistente | Recensione a livello piatto |
| \`votoCibo\` | Sì | scala 1–5 | intero/mezzo punto | Dimensione qualità cucina |
| \`votoPrezzo\` | Consigliato | scala 1–5 | | Rapporto qualità/prezzo |
| \`votoAtmosfera\` | Consigliato | scala 1–5 | | Ambiente, rumore, arredo |
| \`votoServizio\` | Consigliato | scala 1–5 | | Accoglienza |
| \`testo\` | Consigliato | testo | 0–4000 char, sanitizzato | Indicizzato in Qdrant |
| \`contestoVisita\` | Opzionale | enum | coppia, famiglia, lavoro, amici, da_solo | Per affinità lettore |
| \`prezzoSpeso\` | Opzionale | decimale | | Segnale prezzo reale |
| \`foto\` | Opzionale | media | tipo/size/EXIF strip | Privacy: rimozione metadati |
| \`dataVisita\` | Consigliato | data | ≤ oggi | Freschezza del giudizio |

Validazioni anti-abuso: una recensione per autore/locale entro finestra temporale (aggiornabile), rate limiting, rilevamento testo duplicato/spam, soglia di reputazione per voti che pesano di più.

### 3.4 Relazioni con luoghi ed eventi (input relazionale)

- **Prossimità/appartenenza a luoghi**: collegamento del locale a nodi \`Luogo\`/\`POI\`/\`Quartiere\`/\`Itinerario\` già presenti nel grafo consumer (es. "vicino al Duomo", "nel quartiere X").
- **Associazione a eventi**: collegamento a nodi \`Evento\` (concerto, fiera, sagra) per query "dove mangiare prima/dopo l'evento" o "locali della sagra".
- **Itinerari gastronomici**: input per costruire percorsi (giro di cicchetti, tour street food) come sequenze di nodi locale.

### 3.5 Input dai connettori (vedi sezione 6)

- File strutturati (CSV/JSON) di anagrafiche locali e menù.
- Documenti non strutturati (PDF menù, volantini) → estrazione via Tika/OCR esistente.
- Feed da fonti aperte georeferenziate.
- Email/messaggi (moduli \`email\`, \`messaging\`) con segnalazioni o aggiornamenti.

### 3.6 Requisiti non funzionali sugli input

- **Validazione al boundary**: ogni input validato con schema prima della persistenza (regola di progetto), fail-fast con messaggi chiari bilingui.
- **Immutabilità**: aggiornamenti producono nuove versioni del nodo/relazione, non mutazioni in place (storicizzazione prezzi, orari, voti).
- **Privacy**: dati personali (autori, foto) trattati localmente; nessun invio a provider esterni senza consenso; strip EXIF sulle immagini.
- **i18n**: ogni contenuto testuale può avere varianti IT/EN; le enum sono tradotte e instradate al frontend secondo lo switch lingua.
- **Provenienza**: ogni nodo/arco traccia sorgente (manuale, connettore, AI-suggerito) e timestamp per audit e pesi.

---

## 4. Flusso dell'attività (step-by-step)

Si descrivono i flussi end-to-end principali. Ogni flusso indica attore, passi, moduli LocalMind coinvolti e punti di validazione.

### 4.1 Flusso A — Ingestione/creazione di un locale

1. **Trigger**: un foodie crea manualmente un locale dal frontend, oppure un connettore/batch importa un file, oppure l'AI propone un nodo estratto da un documento.
2. **Validazione input** (API layer): i campi obbligatori (3.1) sono verificati con schema; geocodifica e normalizzazione indirizzo; controllo duplicati per prossimità + nome (fuzzy match) → se sospetto duplicato, si propone merge.
3. **Creazione nodo \`Locale\`** (domain service \`knowledge\`/ambito): nodo immutabile con stato \`bozza\` se da community, \`pubblicato\` se da fonte fidata o dopo moderazione.
4. **Collegamento archi**: creazione archi verso nodi \`Cucina\`, \`Luogo\`/\`POI\` (prossimità), eventuali \`Evento\`.
5. **Indicizzazione semantica**: descrizione e attributi testuali → embedding in Qdrant (riuso pipeline esistente) per ricerca e GraphRAG.
6. **Persistenza**: nodo e archi su MySQL (struttura grafo), vettori su Qdrant; pubblicazione \`DomainEvent\` (es. \`LocaleCreatoEvent\`).
7. **Moderazione** (se bozza): entra nella coda curatori; all'approvazione lo stato passa a \`pubblicato\` e il peso iniziale degli archi viene impostato.

### 4.2 Flusso B — Aggiunta di menù e piatti

1. **Trigger**: ristoratore/foodie aggiunge un menù, oppure upload di un PDF menù.
2. **Estrazione** (se documento): Tika/OCR estrae testo; un passo AI (Ollama) struttura il testo in sezioni e piatti candidati (nome, descrizione, prezzo, allergeni inferiti) → presentati per conferma umana.
3. **Validazione**: campi piatto (3.2); allergeni e diete normalizzati su tassonomia; prezzo con valuta.
4. **Creazione nodi \`Piatto\`** e archi \`SERVE\` (Locale→Piatto), \`CONTIENE\` (Piatto→Ingrediente), \`ADATTO_A\` (Piatto→Dieta).
5. **Indicizzazione**: descrizioni piatti in Qdrant.
6. **Storicizzazione**: variazioni di prezzo creano nuove versioni; il prezzo corrente è quello più recente valido.

### 4.3 Flusso C — Recensione community (il cuore del peso)

1. **Autenticazione**: l'utente è loggato (modulo \`auth\`); recupero del suo nodo \`Persona\` e reputazione.
2. **Compilazione**: voti multi-dimensione (cibo, prezzo, atmosfera, servizio), testo, contesto visita, foto opzionali, data visita (3.3).
3. **Validazione anti-abuso**: rate limiting, una recensione per finestra, rilevamento spam/duplicati, strip EXIF foto.
4. **Creazione nodo \`Recensione\`** e arco \`RECENSISCE\` (Persona→Locale, opz. →Piatto) con payload dei voti.
5. **Ricalcolo pesi** (asincrono via evento \`RecensioneCreataEvent\`):
   - aggiornamento del peso degli archi \`SERVE\`/qualità del locale e dei piatti recensiti;
   - il contributo della recensione è **ponderato** per reputazione dell'autore, freschezza (decadimento temporale) e coerenza (outlier detection);
   - aggiornamento reputazione dell'autore in base a utilità (voti "utile" da altri) e coerenza con il consenso.
6. **Indicizzazione** del testo recensione in Qdrant (alimenta GraphRAG e ricerca semantica).
7. **Notifica/moderazione**: segnalazioni eventuali entrano in coda curatori.

### 4.4 Flusso D — Scoperta conversazionale (GraphRAG, l'esperienza utente principale)

1. **Domanda in linguaggio naturale**: es. *"osteria sotto i 35€, vicino al teatro Comunale, aperta stasera, con primi senza glutine e atmosfera tranquilla"*.
2. **Parsing dell'intento** (Ollama default): estrazione vincoli → posizione/POI di riferimento, fascia prezzo, orario, diete, attributi atmosfera, tipo locale.
3. **Recupero ibrido**:
   - **strutturale sul grafo** (MySQL): filtra locali per tipo, prossimità al POI "teatro", orari (aperto stasera), fascia prezzo, presenza di piatti \`ADATTO_A\` senza_glutine;
   - **semantico** (Qdrant): matching su descrizioni/recensioni per "atmosfera tranquilla", qualità dei primi;
   - **espansione di vicinato**: dal nodo \`Luogo\`=teatro si esplorano gli archi \`VICINO_A\`/\`NEL_QUARTIERE\`.
4. **Ranking pesato**: combinazione di pesi archi (qualità emergente), affinità con i gusti dell'utente (storico/preferiti), freschezza e vincoli soddisfatti.
5. **Generazione risposta**: l'AI compone una risposta motivata e **cita i nodi/percorsi** usati (locale, recensioni rilevanti, distanza dal teatro, piatti senza glutine).
6. **Interazione**: l'utente può raffinare ("più economico", "all'aperto"), salvare nei preferiti, aprire la vista grafo per esplorare i collegamenti.

### 4.5 Flusso E — Esplorazione visuale del grafo

1. L'utente apre la **vista grafo** da un nodo locale o da una risposta AI.
2. Espansione progressiva per relazioni: cucine, piatti, recensori, luoghi vicini, eventi collegati, locali "simili".
3. Filtri per tipo nodo/relazione e per peso minimo dell'arco (mostra solo collegamenti forti).
4. Da un nodo evento → locali collegati; da un piatto → altri locali che lo servono bene.

### 4.6 Flusso F — Manutenzione e curatela

1. Coda di moderazione: bozze, segnalazioni, sospetti duplicati.
2. Azioni curatore: approva/rifiuta, **merge** di nodi duplicati (preservando archi e recensioni), correzione attributi, gestione reputazione.
3. Job periodici: decadimento freschezza pesi, ricalcolo ranking, rilevamento locali probabilmente chiusi (assenza di segnali), aggiornamento prezzi scaduti.

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il sottografo riusa il motore generico (nodi tipizzati + archi pesati su MySQL, semantica su Qdrant) introducendo tipi specifici di dominio. Niente Neo4j: la struttura vive in tabelle relazionali, la semantica nei vettori.

### 5.1 Tipi di nodo

| Tipo nodo | Descrizione | Attributi chiave |
|-----------|-------------|------------------|
| \`Locale\` | Ristorante/bar/locale | nome, tipoLocale, posizione, fasciaPrezzo, orari, servizi, stato |
| \`Cucina\` | Tipo di cucina/tradizione | nome, regione/origine, descrizione |
| \`Piatto\` | Voce di menù | nome, sezione, prezzo, allergeni, diete, stagionalità |
| \`Ingrediente\` | Componente di un piatto | nome, categoria, allergene? |
| \`Menu\` / \`SezioneMenu\` | Contenitori di piatti | nome, periodo (pranzo/cena), validità |
| \`Recensione\` | Valutazione multi-dimensione | voti, testo, contesto, dataVisita |
| \`Persona\` | Recensore/foodie/curatore/gestore | reputazione, gusti, ruolo |
| \`Luogo\`/\`POI\` | Punto di interesse territoriale (riuso) | nome, posizione, tipo |
| \`Quartiere\`/\`Zona\` | Area geografica (riuso) | nome, confini |
| \`Evento\` | Evento del territorio (riuso) | nome, data, luogo |
| \`Itinerario\` | Percorso gastronomico | tappe, tema |
| \`Collezione\`/\`Lista\` | Raccolta curata ("migliori pizzerie") | titolo, curatore |
| \`Tag\`/\`Attributo\` | Etichette trasversali (es. "romantico", "rumoroso", "vista mare") | nome, categoria |

### 5.2 Tipi di relazione (archi)

| Relazione | Da → A | Significato | Pesata? |
|-----------|--------|-------------|:------:|
| \`SERVE\` | Locale → Piatto | il locale propone il piatto | Sì (qualità del piatto nel locale) |
| \`APPARTIENE_A_CUCINA\` | Locale/Piatto → Cucina | classificazione gastronomica | Sì (rilevanza) |
| \`CONTIENE\` | Piatto → Ingrediente | composizione | No (o leggera) |
| \`ADATTO_A\` | Piatto → Dieta | idoneità dietetica | No (fattuale) |
| \`RECENSISCE\` | Persona → Locale/Piatto | recensione/valutazione | Sì (contributo pesato) |
| \`VICINO_A\` | Locale → Luogo/POI | prossimità geografica | Sì (inverso distanza) |
| \`NEL_QUARTIERE\` | Locale → Quartiere | appartenenza zona | No |
| \`COLLEGATO_A_EVENTO\` | Locale → Evento | rilevante per l'evento | Sì (pertinenza) |
| \`TAPPA_DI\` | Locale → Itinerario | parte di un percorso | Sì (ordine/rilevanza) |
| \`INCLUSO_IN\` | Locale → Collezione | curato in una lista | Sì (posizione/cura) |
| \`HA_ATTRIBUTO\` | Locale → Tag/Attributo | atmosfera/caratteristica | Sì (frequenza nelle recensioni) |
| \`SIMILE_A\` | Locale → Locale | similarità (AI/semantica) | Sì (score similarità) |
| \`GESTITO_DA\` | Locale → Persona | rivendicazione gestore | No (fattuale) |
| \`SEGUE\`/\`PREFERISCE\` | Persona → Locale/Cucina | gusti dell'utente | Sì (forza preferenza) |

### 5.3 Criteri per il peso degli archi

Il peso è il cuore del valore: rende la qualità *emergente* e l'AI capace di ordinare. Per gli archi di qualità (es. \`SERVE\`, \`HA_ATTRIBUTO\`, qualità complessiva del locale) il peso è una funzione configurabile dei seguenti fattori:

| Fattore | Effetto sul peso | Note |
|---------|------------------|------|
| **Affidabilità del recensore** | Recensioni di utenti con reputazione alta pesano di più | Reputazione cresce con utilità e coerenza dei contributi |
| **Volume e consenso** | Più recensioni concordi → peso più stabile e alto | Mitiga il singolo giudizio estremo |
| **Freschezza (decadimento temporale)** | Recensioni recenti pesano più delle vecchie | Decadimento configurabile (es. half-life) |
| **Coerenza / outlier detection** | Giudizi anomali rispetto al consenso pesano meno | Anti-manipolazione |
| **Specificità dimensionale** | Voti \`cibo\`/\`prezzo\`/\`atmosfera\`/\`servizio\` aggiornano archi distinti | Evita la media unica appiattente |
| **Frequenza d'uso/navigazione** | Nodi/archi spesso visitati o salvati rafforzano il peso | Segnale di rilevanza |
| **Prossimità (per \`VICINO_A\`)** | Peso inversamente proporzionale alla distanza | Normalizzato su soglia |
| **Pertinenza all'evento (per \`COLLEGATO_A_EVENTO\`)** | Distanza temporale/spaziale dall'evento | |
| **Curatela** | Inclusione in collezioni curate da curatori fidati alza il peso | |

Principi: i pesi sono **derivati e ricalcolabili** (mai mutazione distruttiva del dato grezzo: le recensioni restano, il peso è aggregato), la formula è **configurabile per istanza** (un ente può privilegiare freschezza, un altro il consenso), e ogni peso è **spiegabile** (l'AI può citare perché un locale è in cima).

---

## 6. Fonti dati & connettori (ingestione)

L'ingestione riusa la pipeline documentale e batch esistente, aggiungendo connettori specifici. Tutto resta local-first.

| Fonte | Modalità | Modulo LocalMind riusato | Output nel grafo |
|-------|----------|--------------------------|------------------|
| **Contributi manuali community** | Form frontend (locali, piatti, recensioni) | \`knowledge\`, \`auth\`, API | Nodi \`Locale\`/\`Piatto\`/\`Recensione\` + archi |
| **File strutturati** (CSV/JSON anagrafiche, menù) | Import batch | \`batch\`, connettore dedicato | Nodi e archi in bulk |
| **Documenti non strutturati** (PDF menù, volantini, brochure) | Upload o folder watcher → Tika/OCR → strutturazione AI | \`document\`, \`batch\`, \`llm\` (Ollama) | Piatti/menù candidati per conferma |
| **Feed aperti georeferenziati** (dati pubblici locali) | Connettore di sincronizzazione | nuovo connettore (plugin PF4J) | Nodi \`Locale\`/\`POI\` |
| **Email/segnalazioni** | Ingestione IMAP esistente | \`email\` | Aggiornamenti/segnalazioni in coda |
| **Canali di messaggistica** | Bot/canali | \`messaging\` | Contributi e domande |
| **Eventi del territorio** | Riuso sottografo eventi consumer | \`knowledge\`, \`calendar\` | Archi \`COLLEGATO_A_EVENTO\` |
| **MCP tools** | Tool esterni invocabili dall'AI | \`mcp\` | Arricchimento on-demand |

Linee guida ingestione:
- **Estensibilità via plugin PF4J**: ogni nuova fonte è un connettore-plugin installabile dal marketplace, senza toccare il core.
- **Deduplica**: ogni connettore passa per il match fuzzy nome+geo prima di creare nodi.
- **Provenienza e consenso**: ogni nodo importato traccia la sorgente; fonti esterne richiedono consenso esplicito (privacy).
- **AI di strutturazione locale**: la trasformazione documento→nodi usa Ollama di default, mai cloud senza opt-in.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

Mappa concreta del lavoro, distinguendo ciò che è **MVP** (necessario per il primo valore) da ciò che è **evoluzione**. Le colonne indicano se la funzionalità è da **creare** (nuova), **sviluppare** (estensione di esistente) o **mantenere** (operatività continua).

### 7.1 MVP

| Funzionalità | Tipo | Moduli coinvolti |
|--------------|------|------------------|
| Schema dominio ristorazione (tipi nodo/arco, enum bilingui) | Creare | \`knowledge\`/domain, Flyway |
| Migrazioni Flyway per tabelle nodi/archi/recensioni (una query per file) | Creare | \`app\` |
| CRUD nodo \`Locale\` (API + service + adapter JPA) | Creare/Sviluppare | \`knowledge\`, \`infrastructure\`, \`api\` |
| CRUD \`Piatto\`/\`Menu\` con allergeni e diete | Creare | come sopra |
| Recensioni multi-dimensione + nodo \`Persona\`/reputazione base | Creare | \`auth\`, \`knowledge\` |
| Motore di peso v1 (reputazione + freschezza + consenso) | Creare | domain service + eventi |
| Indicizzazione semantica descrizioni/recensioni su Qdrant | Sviluppare | \`vectorstore\`, pipeline esistente |
| Ricerca strutturata (filtri: tipo, prezzo, dieta, orari, prossimità) | Creare | \`api\`, query grafo MySQL |
| GraphRAG v1: chat conversazionale sul sottografo con citazioni | Creare/Sviluppare | \`llm\` (Ollama), \`knowledge\` |
| Import batch CSV/JSON locali e menù | Sviluppare | \`batch\` |
| Estrazione menù da PDF (Tika/OCR → strutturazione AI) | Sviluppare | \`document\`, \`llm\` |
| Frontend: pagina locale, form recensione, ricerca, chat | Creare | Angular feature \`ristorazione\` |
| i18n IT/EN di UI ed enum dell'ambito | Creare | frontend + backend enum |
| Coda di moderazione base (approva/rifiuta bozze) | Creare | \`knowledge\`, \`api\`, frontend |

### 7.2 Evoluzione

| Funzionalità | Tipo | Moduli coinvolti |
|--------------|------|------------------|
| Vista grafo interattiva con espansione e filtri per peso | Creare | frontend (graph viz) |
| Motore di peso v2: outlier detection, affinità con gusti utente, decadimento configurabile | Sviluppare | domain |
| Raccomandazioni personalizzate ("per te") basate su \`PREFERISCE\` | Creare | \`llm\`, domain |
| Itinerari gastronomici generati dall'AI (giro cicchetti, food tour) | Creare | \`knowledge\`, \`llm\` |
| Collegamento locali↔eventi e suggerimenti contestuali | Sviluppare | \`calendar\`, \`knowledge\` |
| Claim del locale da parte del gestore + risposta alle recensioni | Creare | \`auth\`, \`knowledge\` |
| Collezioni/liste curate ("migliori pizzerie del centro") | Creare | \`knowledge\`, frontend |
| Suggerimento AI di collegamenti mancanti tra nodi (locali simili, cucine) | Creare | GraphRAG |
| Connettori-plugin per feed aperti georeferenziati (marketplace) | Creare | \`plugin\` PF4J, \`marketplace\` |
| Rilevamento automatico locali chiusi/prezzi scaduti | Creare | \`batch\`/scheduler |
| Sistema reputazione avanzato (badge, livelli, anti-sybil) | Sviluppare | \`auth\`, domain |
| Multimodale: analisi foto piatti (qualità, abbinamento) | Sviluppare | adapter multimodali esistenti |
| Export/condivisione collezioni e schede | Sviluppare | \`common\`/backup |

### 7.3 Manutenzione continua

- Aggiornamento tassonomie (cucine, allergeni, diete) e enum bilingui.
- Tuning periodico delle formule di peso e reputazione.
- Gestione coda moderazione, anti-spam, merge duplicati.
- Job di freschezza/decadimento e ricalcolo ranking.
- Aggiornamento connettori al variare delle fonti.
- Aggiornamento costante della documentazione IT/EN (regola di progetto) e tracciamento sviluppi in cartella \`Sviluppi/\`.

---

## 8. Casi d'uso AI / GraphRAG

L'AI naviga il grafo pesato combinando recupero strutturale (MySQL) e semantico (Qdrant), citando i nodi/percorsi usati. Esempi concreti:

| Caso d'uso | Domanda tipo | Come l'AI risponde sul grafo |
|------------|--------------|------------------------------|
| **Scoperta multi-vincolo** | "Osteria sotto 35€ vicino al teatro, aperta stasera, primi senza glutine" | Filtri strutturali (prezzo, orari, prossimità POI, \`ADATTO_A\` senza_glutine) + semantica su qualità primi; ranking pesato; cita locali e recensioni |
| **Raccomandazione contestuale a evento** | "Dove cenare prima del concerto di venerdì?" | Dal nodo \`Evento\` espande \`COLLEGATO_A_EVENTO\`/\`VICINO_A\` al luogo dell'evento; filtra per orario pre-concerto |
| **Affinità di gusto** | "Consigliami qualcosa di nuovo che mi piacerà" | Usa \`PREFERISCE\`/storico, trova \`SIMILE_A\` a locali amati, esclude già visitati |
| **Confronto motivato** | "Meglio X o Y per una cena romantica?" | Confronta pesi \`HA_ATTRIBUTO\`=romantico, atmosfera, recensioni di contesto "coppia" |
| **Itinerario** | "Organizza un giro di cicchetti a piedi nel centro" | Costruisce sequenza di nodi \`Locale\` per prossimità e tema, ottimizza percorso |
| **Piatto-centrico** | "Dove fanno la migliore cacio e pepe in zona?" | Naviga archi \`SERVE\`→\`Piatto\` con peso qualità + recensioni a livello piatto |
| **Vincoli dietetici di gruppo** | "Cena per 8, uno vegano e uno celiaco, vicino all'ufficio" | Incrocia capienza, diete (\`ADATTO_A\`), prossimità POI ufficio |
| **Collegamenti non evidenti** | "Suggerisci locali simili a uno che amo ma in un altro quartiere" | \`SIMILE_A\` + cambio vincolo \`NEL_QUARTIERE\` |
| **Spiegazione** | "Perché mi consigli questo?" | L'AI cita pesi, recensioni affidabili, distanza, vincoli soddisfatti |

Tutti i casi girano con **Ollama di default**; i provider cloud sono opzionali e attivabili dall'utente. Le risposte sono **spiegabili** grazie alla citazione dei nodi e dei pesi.

---

## 9. KPI & metriche di successo

| Categoria | KPI | Obiettivo/segnale |
|-----------|-----|-------------------|
| **Copertura del grafo** | N. nodi \`Locale\`/\`Piatto\` con attributi completi | Crescita costante; % schede "complete" |
| **Qualità del dato** | % locali con orari, prezzi, allergeni aggiornati (<90 gg) | Alta freschezza |
| **Coinvolgimento community** | N. recensioni/settimana; % recensori ricorrenti | Community attiva |
| **Affidabilità** | Tasso recensioni segnalate/rimosse; outlier individuati | Basso spam |
| **Efficacia AI** | % query risolte senza raffinamenti; click/salvataggio sul risultato suggerito | Alta pertinenza |
| **Spiegabilità** | % risposte con citazione nodi/percorsi | ~100% |
| **Soddisfazione** | Voto "utile" su risposte AI; ritorno utenti | Trend positivo |
| **Performance** | Latenza recupero ibrido (grafo+vettori) | Entro soglia UX |
| **Local-first** | % elaborazioni AI eseguite in locale (Ollama) | Default 100% salvo opt-in |
| **Privacy** | Eventi di invio dati esterni senza consenso | Zero |
| **Estensibilità** | N. connettori-plugin installati | Adozione marketplace |

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---------|---------|-------------|
| **Recensioni false/manipolazione** | Ranking inquinato, perdita fiducia | Peso per reputazione, outlier detection, rate limiting, soglie, moderazione |
| **Dati obsoleti** (menù/prezzi/chiusure) | Risposte sbagliate | Storicizzazione, decadimento freschezza, job rilevamento chiusi, claim gestore |
| **Cold start / grafo vuoto** | Poco valore iniziale | Import batch + connettori + estrazione PDF per seed; incentivi community |
| **Duplicati di nodi** | Frammentazione, pesi diluiti | Match fuzzy geo+nome, merge curatore |
| **Performance query relazionali su MySQL** (no Neo4j) | Latenza su sottografi grandi | Indici mirati, denormalizzazione di vicinato, caching, pre-aggregazione pesi |
| **Allergeni/diete errati** | Rischio salute | Validazione su tassonomia, conferma umana, disclaimer, niente inferenza non verificata pubblicata come certa |
| **Bias dell'AI nelle raccomandazioni** | Omologazione | Diversità nel ranking, trasparenza dei criteri |
| **Privacy dati personali** (foto, autori) | Compliance | Strip EXIF, dati locali, consenso, minimizzazione |
| **Sovraccarico moderazione** | Backlog, qualità cala | Automazione anti-spam, reputazione, curatela distribuita |
| **Qualità estrazione OCR menù** | Piatti errati | Conferma umana obbligatoria post-estrazione |

---

## 11. Manutenzione & evoluzione

- **Schema evolutivo**: nuovi tipi di nodo/arco e attributi si aggiungono come estensioni del modulo, con migrazioni Flyway incrementali (una query per file) e enum bilingui aggiornate.
- **Tuning dei pesi**: le formule di peso e reputazione sono configurabili per istanza e vanno riviste periodicamente sui KPI; ogni modifica documentata in \`Sviluppi/\`.
- **Tassonomie**: cucine, ingredienti, allergeni, diete e tag vanno mantenuti aggiornati e tradotti IT/EN.
- **Connettori**: aggiornati al variare delle fonti; nuovi connettori distribuiti come plugin PF4J via marketplace senza toccare il core.
- **Qualità del grafo**: job ricorrenti per freschezza, deduplica, rilevamento locali chiusi, prezzi scaduti.
- **Modelli AI**: aggiornamento dei modelli Ollama locali; valutazione periodica della qualità GraphRAG.
- **Documentazione**: aggiornamento costante della doc IT/EN a ogni sviluppo (vincolo di progetto); le novità funzionali si riflettono in \`documentation/\` e \`documentazione/\`.
- **Coerenza architetturale**: il dominio resta puro (no Spring), wiring in \`DomainConfig\`, file piccoli e coesi, pattern immutabili.

---

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo LocalMind | Ruolo nell'ambito ristorazione |
|------------------|-------------------------------|
| \`knowledge\` | Base del motore a grafo: ospita tipi di nodo/arco e logiche del sottografo ristorazione |
| \`llm\` (Ollama default + cloud opz.) | Parsing intento, GraphRAG, strutturazione menù da documento, raccomandazioni; fallback chain esistente |
| \`document\` + \`batch\` | Ingestione menù/anagrafiche (Tika/OCR, folder watcher, job di import) |
| Qdrant (\`vectorstore\`) | Indicizzazione semantica di descrizioni, piatti e recensioni per il recupero ibrido |
| MySQL + Flyway | Persistenza della struttura del grafo (nodi, archi, recensioni); migrazioni incrementali una-query |
| \`auth\` | Identità recensori/curatori/gestori, reputazione, permessi, claim del locale |
| \`calendar\` | Collegamento a eventi del territorio per suggerimenti contestuali |
| \`email\` + \`messaging\` | Canali di segnalazione, aggiornamenti e bot conversazionali |
| \`mcp\` | Tool esterni invocabili dall'AI per arricchire on-demand |
| \`plugin\` (PF4J) + \`marketplace\` | Connettori di ingestione e moduli dominio installabili senza toccare il core |
| \`agent\` + \`automation\` | Job e agenti per moderazione, freschezza, rilevamento chiusure, ricalcolo pesi |
| \`common\` | Eventi di dominio, analytics, backup/export delle collezioni |
| Frontend Angular (feature \`ristorazione\`) | Pagine locale/menù, form recensione, ricerca, chat GraphRAG, vista grafo; standalone + Signals; i18n IT/EN |

**Vincoli rispettati**: local-first e self-hostable; AI Ollama di default con cloud opzionale e consenso; riuso di MySQL + Qdrant (niente Neo4j); estensibilità via PF4J; privacy dei dati personali; documentazione ed enum bilingui IT/EN; architettura esagonale con dominio puro; migrazioni Flyway con una sola query per file.

---

### Fonti di riferimento (best practice 2026)

- [Restaurant Trends Report U.S. 2026 — SevenRooms](https://sevenrooms.com/research/restaurant-trends/)
- [2026 Restaurant Technology Trends — Incentivio](https://incentivio.com/2026-restaurant-technology-trends-what-forward-thinking-operators-need-to-know/)
- [Structured data for restaurants: the complete 2026 guide — Malou](https://www.malou.io/en-us/blog/structured-data-for-restaurants)
- [Restaurant — Schema.org Type](https://schema.org/Restaurant)
- [Menu — Schema.org Type](https://schema.org/Menu)
- [7 Knowledge Graph Examples of 2026 — PuppyGraph](https://www.puppygraph.com/blog/knowledge-graph-examples)
`;
