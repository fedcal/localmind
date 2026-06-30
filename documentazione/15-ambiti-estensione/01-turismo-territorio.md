# Turismo & territorio

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo documento descrive il verticale **consumer "Turismo & territorio"** costruito sul motore di Knowledge Graph universale di LocalMind. L'obiettivo non è creare un'app turistica monolitica, ma istanziare — tramite tipi di nodo, tipi di relazione e moduli installabili — un grafo pesato di luoghi e relazioni geografiche/tematiche, navigabile dall'AI (GraphRAG), che fa emergere collegamenti non evidenti tra luoghi, esperienze ed eventi. Tutto resta local-first, self-hostable, con AI Ollama di default, riusando MySQL 8.0 (struttura) e Qdrant (semantica), senza introdurre un database a grafo dedicato.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema percepito dall'utente

La scoperta del territorio oggi è dominata da piattaforme centralizzate (Google Maps, TripAdvisor, Booking, Instagram) che soffrono di limiti strutturali profondi:

- **Omogeneizzazione e overtourism.** I motori di ranking premiano i luoghi già popolari: i primi risultati sono sempre gli stessi monumenti, ristoranti recensiti da migliaia di turisti, attrazioni "instagrammabili". Il risultato è la concentrazione dei flussi su pochi POI saturi, mentre il 90% del territorio (borghi, sentieri, botteghe, eventi locali, esperienze autentiche) resta invisibile.
- **Ranking opaco e drogato.** L'ordinamento è determinato da algoritmi proprietari non ispezionabili, influenzati da pubblicità, recensioni false e logiche di engagement. L'utente non sa *perché* un luogo è suggerito né può fidarsi del segnale.
- **Ricerca per parole chiave, non per relazioni.** Le piattaforme attuali rispondono bene a "ristoranti vicino a me", ma falliscono su query relazionali e contestuali: *"un itinerario di un weekend in moto tra borghi medievali con cantine biologiche e poca folla, raggiungibili senza autostrada"*. Il grafo delle relazioni tra luoghi semplicemente non esiste per l'utente finale.
- **Dati chiusi e non portabili.** Recensioni, liste e preferenze sono prigioniere della piattaforma. L'utente non possiede i propri dati e non può self-hostare la propria mappa della conoscenza territoriale.
- **Mancanza di conoscenza locale strutturata.** La conoscenza di chi vive un territorio (pro loco, guide, residenti, associazioni) è frammentata in PDF, gruppi social, passaparola; non è interrogabile né collegata.

### 1.2 La soluzione LocalMind

LocalMind tratta il territorio come un **grafo pesato di conoscenza**, non come un elenco di marker su una mappa. Ogni luogo, evento, esperienza, itinerario è un **nodo tipizzato**; ogni connessione (vicinanza, similarità tematica, "abbinato a", "tappa di", "stesso produttore") è un **arco con peso**, dove il peso codifica intensità, qualità e affidabilità della relazione. Su questo grafo l'AI naviga (GraphRAG) per rispondere a domande complesse, costruire itinerari e far emergere i collegamenti che le ricerche keyword non vedono.

Il valore differenziante si articola su cinque assi:

| Asse di valore | Cosa offre LocalMind | Differenza vs Google Maps / TripAdvisor |
|---|---|---|
| **Scoperta dei luoghi nascosti** | Ranking emergente dalla community e dalle relazioni del grafo, non dalla popolarità assoluta; possibilità di esplicitare la dimensione "hidden gem" | I big player premiano strutturalmente i POI già saturi |
| **Navigazione relazionale** | Esplorazione del grafo per relazioni geografiche e tematiche; risposte AI che spiegano il percorso | Ricerca per keyword e filtri rigidi, nessun grafo navigabile |
| **Trasparenza** | Ranking ispezionabile, peso degli archi spiegabile, citazione dei nodi/percorsi usati dall'AI | Algoritmo proprietario opaco, influenzato da advertising |
| **Sovranità dei dati** | Local-first / self-hostable; il comune, la pro loco o il singolo possiede e ospita il proprio grafo | Dati prigionieri della piattaforma cloud |
| **Conoscenza locale strutturata** | Ingestione di fonti aperte (OpenStreetMap, Wikidata, open data turistici) + contributi community in un grafo interrogabile | Conoscenza locale dispersa e non interrogabile |

### 1.3 Chi ne beneficia e perché conta

- **Il viaggiatore curioso** trova esperienze autentiche fuori dai circuiti di massa, con itinerari costruiti su misura dall'AI a partire da vincoli reali (tempo, mezzo, stagione, interessi).
- **Le comunità locali** (comuni, pro loco, DMO, associazioni) ottengono uno strumento open-source e self-hostable per valorizzare il proprio territorio senza dipendere da piattaforme esterne né pagare commissioni, mantenendo la proprietà del dato — coerente con i progetti europei di Open Data Destination.
- **L'ecosistema** beneficia di una "Wikipedia dei luoghi" community-driven: dati aperti, ranking trasparente, riduzione dell'overtourism tramite redistribuzione dei flussi verso luoghi meno noti.

### 1.4 Allineamento con la visione LocalMind

Questo verticale è la prima istanza **consumer** del motore di grafo universale descritto in `.planning/PROJECT.md`. Dimostra che lo stesso motore che serve la conoscenza enterprise (doc, processi, microservizi) può servire la scoperta del territorio cambiando solo schema dei nodi/relazioni e moduli installati. Valida la tesi "una piattaforma, più ecosistemi" e la riusabilità di MySQL+Qdrant+GraphRAG.

---

## 2. Personas & utenti target

| Persona | Profilo | Obiettivi | Bisogni dal sistema |
|---|---|---|---|
| **Esploratore / viaggiatore lento** | Turista indipendente, evita le mete di massa, cerca autenticità | Scoprire borghi, sentieri, botteghe, esperienze poco note; costruire itinerari su misura | Ricerca relazionale, itinerari AI, filtro "low-crowd", offline/self-host in viaggio |
| **Local explorer / residente** | Vive il territorio, vuole riscoprire la propria zona | Trovare eventi, sagre, aperture stagionali vicino a casa | Aggiornamenti eventi, raggio geografico, contributi rapidi |
| **Contributor / curatore locale** | Guida, blogger, appassionato, membro di pro loco | Arricchire il grafo con luoghi nascosti, correggere dati, segnalare relazioni | UI di contribuzione, moderazione, attribuzione, reputazione |
| **Operatore DMO / Comune / pro loco** | Ente che promuove il territorio | Pubblicare POI/eventi ufficiali, valorizzare l'offerta, mantenere proprietà del dato | Istanza self-hosted, ingestione open data, export, branding |
| **Operatore esperienziale** | Cantina, agriturismo, guida escursionistica, artigiano | Rendere visibile la propria esperienza nel contesto territoriale | Scheda nodo esperienza, relazioni con luoghi vicini, eventi |
| **Sviluppatore / integratore** | Costruisce sopra LocalMind | Estendere tipi di nodo, scrivere connettori e plugin | API grafo, plugin PF4J, SDK, documentazione IT/EN |

Persona primaria dell'MVP: **Esploratore** (lato consumo) e **Contributor/curatore locale** (lato alimentazione del grafo). Senza contributori il grafo non cresce; senza esploratori non ha valore. L'MVP deve chiudere entrambi i lati del ciclo.

---

## 3. Requisiti in input

Questa sezione è volutamente dettagliata: definisce *cosa* deve poter entrare nel sistema, da chi, in che forma e con quali vincoli di validazione, perché la qualità del grafo dipende interamente dalla qualità degli input.

### 3.1 Input dell'utente che consuma (query e preferenze)

L'utente esploratore interroga il grafo. Gli input da raccogliere e validare:

| Input | Tipo | Esempio | Obbligatorio | Validazione |
|---|---|---|---|---|
| Query in linguaggio naturale | testo libero | "borghi medievali con cantine vicino a Siena, poca folla" | Sì (per la ricerca AI) | Lunghezza max, sanitizzazione, lingua rilevata (IT/EN) |
| Posizione di riferimento | coordinate o toponimo | lat/lon GPS, "Firenze", indirizzo | No | Geocoding, range coordinate valide |
| Raggio / area di interesse | numero + unità o bbox | "entro 30 km", bounding box su mappa | No | Range positivo, cap massimo |
| Finestra temporale | date / stagione | "questo weekend", "primavera" | No | Date coerenti, non passate per eventi |
| Mezzo di trasporto | enum | a piedi, bici, moto, auto, mezzi pubblici | No | Valore in enum tradotta IT/EN |
| Tempo a disposizione | durata | "mezza giornata", "3 giorni" | No | Durata positiva |
| Interessi / temi | tag multi-selezione | enogastronomia, natura, arte, famiglia | No | Tag esistenti nel grafo tematico |
| Vincoli di accessibilità | flag | accessibile in sedia a rotelle, pet-friendly | No | Booleani |
| Livello "fuori dai sentieri battuti" | scala 0–100 | preferenza per hidden gems | No | Range 0–100 |
| Budget indicativo | enum/range | gratuito, economico, premium | No | Enum tradotta |
| Lingua dell'interfaccia | enum | IT / EN | Sì (default da profilo) | IT/EN supportate |

Tutti gli input devono essere validati al boundary (controller API + form Angular reattivo), con messaggi d'errore localizzati e fail-fast, come da regole di progetto.

### 3.2 Input del contributor (alimentazione del grafo)

Il contributor crea e arricchisce nodi/archi. Requisiti di input per la creazione di un **nodo Luogo/POI**:

| Campo | Tipo | Obbligatorio | Note di validazione |
|---|---|---|---|
| Nome | testo | Sì | Non vuoto, dedup contro nodi vicini |
| Tipo di nodo | enum (Luogo, POI, Evento, Esperienza, Itinerario…) | Sì | Enum tradotta IT/EN |
| Categoria/sottotipo | tassonomia | Sì | Da tassonomia controllata (allineata a schema.org/OSM) |
| Coordinate geografiche | lat/lon | Sì per luoghi fisici | Range valido, dedup spaziale |
| Descrizione (IT) e (EN) | testo lungo | Almeno una lingua | Sanitizzazione HTML, lunghezza |
| Foto/media | file o URL | No | Tipo MIME, dimensione, licenza dichiarata |
| Orari di apertura / stagionalità | struttura oraria | No | Formato orari, coerenza |
| Tag tematici | multi-tag | No | Da tassonomia tematica |
| Indicatore "luogo nascosto" | flag/scala | No | 0–100 |
| Accessibilità | flag multipli | No | Booleani |
| Fonte / attribuzione | testo/URL/licenza | Sì se importato | Licenza compatibile (CC, ODbL…) |
| Relazioni proposte | lista archi tipizzati | No | Tipo relazione valido, nodo target esistente |

Requisiti per la creazione/proposta di un **arco (relazione)**: nodo sorgente, nodo destinazione, tipo di relazione (enum), direzione, eventuali attributi (es. distanza, durata di percorrenza, motivazione testuale), fonte. Il peso non è inserito manualmente come valore arbitrario ma **derivato** (vedi §5).

Requisiti per **recensioni/valutazioni**: nodo target, punteggio (scala definita), testo opzionale IT/EN, tag di sentiment, identità del contributor (per reputazione e anti-spam), timestamp. Validazione anti-abuso: rate limiting, deduplica, soglie di reputazione.

### 3.3 Input da fonti automatiche (connettori di ingestione)

Vedi §6 per il dettaglio dei connettori. A livello di requisiti di input, ogni connettore deve fornire per ciascun record: identificatore stabile della fonte, tipo mappabile a un tipo di nodo LocalMind, coordinate (dove pertinenti), attributi mappabili, licenza dei dati, timestamp di ultimo aggiornamento. La pipeline deve gestire deduplica (stesso luogo da fonti diverse), riconciliazione (merge attributi) e conflitti (priorità per affidabilità della fonte).

### 3.4 Vincoli trasversali sugli input

- **Privacy e local-first.** I dati di posizione e le query dell'utente non lasciano l'istanza senza consenso esplicito; l'embedding e l'inferenza AI girano su Ollama locale di default.
- **Bilinguismo IT/EN.** Ogni campo testuale rilevante e ogni enum hanno rappresentazione IT ed EN; l'enum è tradotta lato backend e reindirizzata al frontend in base allo switch lingua.
- **Licenze.** Ogni dato importato porta con sé la licenza; il sistema rifiuta o segnala fonti con licenza incompatibile con la natura open del progetto.
- **Qualità minima.** Un nodo non supera la validazione se mancano i campi obbligatori del suo tipo; gli input ambigui finiscono in coda di moderazione anziché essere scartati silenziosamente.

---

## 4. Flusso dell'attività (step-by-step)

Questa sezione è dettagliata su richiesta esplicita. Si descrivono i tre flussi cardine — **scoperta/consumo**, **contribuzione** e **ingestione automatica** — e come confluiscono nel grafo.

### 4.1 Flusso di scoperta e consumo (esploratore → AI → grafo)

1. **Avvio e contesto.** L'utente apre la feature "Scopri territorio" nel frontend Angular. Il sistema rileva lingua (IT/EN) e, su consenso, posizione. Lo stato è gestito da un Signal store dedicato (pattern `ChatStore`).
2. **Formulazione della richiesta.** L'utente digita una query in linguaggio naturale e/o imposta i filtri di §3.1 (raggio, finestra temporale, mezzo, temi, livello hidden-gem). Il form reattivo valida ogni campo al boundary.
3. **Invio e routing.** Il frontend chiama `POST /api/v1/territory/discover` (o l'endpoint del grafo) via `ApiService`. Il controller delega alla porta in del dominio (es. `TerritoryDiscoveryUseCase`).
4. **Comprensione AI della query.** Il servizio di dominio invoca l'LLM (Ollama default, fallback chain esistente) per estrarre intent, entità, vincoli e temi dalla query, normalizzandoli rispetto alla tassonomia del grafo.
5. **Recupero ibrido (GraphRAG).**
   a. **Seed semantico:** la query viene embeddata e cercata su **Qdrant** per individuare i nodi candidati semanticamente più vicini (descrizioni, tag, recensioni).
   b. **Espansione sul grafo:** dai nodi seed il motore naviga gli **archi su MySQL** (vicinanza geografica, similarità tematica, "tappa di", "abbinato a"), espandendo il sottografo entro N hop e applicando i filtri (geo, tempo, accessibilità).
   c. **Ranking pesato:** i candidati sono ordinati combinando peso degli archi, rilevanza semantica, segnale community (recensioni/voti) e preferenza hidden-gem dell'utente.
6. **Sintesi AI.** L'LLM riceve il sottografo selezionato come contesto strutturato e genera una risposta: suggerimenti di luoghi/esperienze e/o un itinerario, **con citazione esplicita dei nodi e dei percorsi del grafo** usati (trasparenza).
7. **Presentazione.** Il frontend mostra i risultati su tre viste sincronizzate: lista/schede, mappa (marker dei nodi) e **vista grafo interattiva** (nodi, archi, peso). L'utente vede *perché* un luogo è suggerito.
8. **Esplorazione progressiva.** L'utente clicca un nodo per espandere i vicini (luoghi correlati, eventi collegati, esperienze abbinate), naviga le relazioni e affina i filtri; ogni interazione può rieseguire la query GraphRAG sul sottografo.
9. **Azione utente.** Salvataggio di un itinerario, aggiunta ai preferiti, esportazione (es. GPX/GeoJSON), condivisione. Queste azioni generano **segnali di interazione** che alimentano il ricalcolo dei pesi (§5).
10. **Feedback loop.** Voti, salvataggi, tempo di permanenza e correzioni dell'utente rientrano come segnali che, in batch, aggiornano i pesi degli archi e il ranking emergente.

```
Query NL + filtri
      │
      ▼
[LLM intent/entità]  ──►  [Qdrant: seed semantico]
                                   │
                                   ▼
                         [MySQL: espansione grafo N-hop + filtri]
                                   │
                                   ▼
                         [Ranking pesato: peso archi + semantica + community + hidden-gem]
                                   │
                                   ▼
                         [LLM sintesi + citazione nodi/percorsi]
                                   │
                                   ▼
              [Lista | Mappa | Grafo interattivo]  ──►  azioni ──► segnali ──► ricalcolo pesi
```

### 4.2 Flusso di contribuzione (contributor → moderazione → grafo)

1. **Autenticazione.** Il contributor accede (auth local-first esistente). La sua reputazione è nota al sistema.
2. **Creazione nodo.** Compila il form di un nuovo Luogo/POI/Evento/Esperienza con i campi di §3.2; il sistema esegue **dedup spaziale e semantica** in tempo reale (cerca nodi simili per coordinate ed embedding) per evitare duplicati.
3. **Proposta relazioni.** Il contributor suggerisce archi tipizzati verso nodi esistenti (es. "questo agriturismo è *abbinato a* questa cantina", "questo borgo è *tappa di* questo itinerario").
4. **Validazione boundary.** Campi obbligatori, tassonomia, licenze media e coerenza geografica sono validati; gli errori sono localizzati e bloccanti.
5. **Coda di moderazione / curatela.** A seconda della reputazione del contributor e della sensibilità del cambiamento, il contributo è pubblicato direttamente o entra in coda di moderazione. Un curatore (o regole automatiche + AI) approva, modifica o respinge con motivazione.
6. **Persistenza immutabile.** All'approvazione, il nodo/arco è scritto su MySQL (struttura) e la sua descrizione embeddata su Qdrant (semantica), seguendo il pattern di immutabilità (nuove versioni, niente mutazioni in place; storico delle revisioni).
7. **Attribuzione e reputazione.** Il contributo è attribuito all'autore; l'approvazione e i voti positivi successivi aumentano la sua reputazione, che pesa sui contributi futuri (§5).
8. **Propagazione pesi.** L'aggiunta di nodi/archi e i voti ricalcolano i pesi delle relazioni coinvolte e aggiornano il ranking emergente.

### 4.3 Flusso di ingestione automatica (fonti aperte → pipeline → grafo)

1. **Trigger.** Un job Spring Batch (riuso del pattern folder-scan/document-ingestion esistente) parte schedulato o on-demand per un connettore (es. OpenStreetMap di un'area, Wikidata, open data DMO).
2. **Estrazione.** Il connettore recupera i record grezzi (Overpass API, SPARQL Wikidata, file GeoJSON/CSV, feed schema.org).
3. **Mappatura.** Ogni record è mappato a un tipo di nodo LocalMind e a una categoria della tassonomia controllata; gli attributi sono normalizzati (IT/EN).
4. **Deduplica e riconciliazione.** Confronto spaziale + semantico con i nodi esistenti; merge degli attributi con priorità per affidabilità della fonte; conservazione di tutte le fonti/licenze.
5. **Inferenza relazioni.** Generazione automatica di archi: vicinanza geografica (raggio/distanza di percorrenza), similarità tematica (embedding), appartenenza (POI dentro una città/area).
6. **Embedding e persistenza.** Descrizioni embeddate su Qdrant via Ollama (EmbeddingModel `@Primary`); nodi/archi persistiti su MySQL.
7. **Calcolo pesi iniziali.** Pesi derivati da fattori della fonte (completezza dati, autorevolezza, distanza) come baseline, poi raffinati dai segnali community.
8. **Report di ingestione.** Log e metriche (nuovi nodi, merge, conflitti, scarti) esposti via Actuator/dashboard.

### 4.4 Confluenza nel grafo e ciclo virtuoso

I tre flussi alimentano lo **stesso grafo pesato**. L'ingestione fornisce la base di copertura; la community aggiunge i luoghi nascosti e il giudizio qualitativo; il consumo genera i segnali di interazione che fanno emergere i contenuti migliori. L'AI sfrutta tutto questo per rispondere e suggerire, e ogni risposta utile rinforza il ciclo. È il meccanismo che permette ai luoghi nascosti di emergere *senza* premiare meccanicamente la popolarità assoluta.

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa lo schema generico del motore (nodi tipizzati + archi pesati su MySQL, semantica su Qdrant) e lo specializza per il dominio territorio. La tassonomia è allineata, dove possibile, a standard aperti (schema.org `TouristAttraction`/`TouristTrip`/`Event`, OpenStreetMap, Wikidata) per favorire interoperabilità e ingestione.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi chiave | Allineamento standard |
|---|---|---|---|
| **Luogo / Area** | Entità geografica contenitore (città, borgo, quartiere, valle, parco) | nome, geometria/centroide, gerarchia amministrativa | OSM boundary, schema.org `Place` |
| **POI / Attrazione** | Punto di interesse puntuale (monumento, museo, panorama, chiesa, sito naturale) | coordinate, categoria, orari, hidden-gem score | schema.org `TouristAttraction` |
| **Esercizio / Locale** | Ristorante, cantina, agriturismo, bottega, bar | coordinate, cucina/prodotto, fascia prezzo, orari | schema.org `FoodEstablishment`/`LocalBusiness` |
| **Evento** | Sagra, festival, mostra, concerto, mercato | data/finestra temporale, ricorrenza, luogo | schema.org `Event` |
| **Esperienza / Attività** | Degustazione, escursione guidata, laboratorio, tour | durata, difficoltà, stagionalità, mezzo | schema.org `TouristTrip`/activity |
| **Itinerario / Percorso** | Sequenza ordinata di tappe (sentiero, route in moto/bici) | lunghezza, durata, dislivello, geometria tracciato | schema.org `TouristTrip`, GPX |
| **Tema / Tag** | Concetto trasversale (enogastronomia, medievale, family, slow) | etichetta IT/EN, gerarchia | tassonomia controllata |
| **Prodotto tipico** | Specialità enogastronomica o artigianale del territorio | nome, categoria, provenienza | schema.org `Product` |
| **Persona / Operatore** | Guida, produttore, artigiano, contributor | ruolo, reputazione, contatti (privacy-aware) | schema.org `Person`/`Organization` |
| **Recensione / Valutazione** | Giudizio su un nodo | punteggio, testo IT/EN, autore, data | schema.org `Review` |
| **Media** | Foto/video/audio associati a un nodo | URL, licenza, autore | — |

I tipi sono **estendibili per dominio** (schema modulare): un modulo "territorio montano" può aggiungere "Rifugio", "Via ferrata"; un modulo "enoturismo" può aggiungere "Denominazione/DOC".

### 5.2 Tipi di relazione (archi)

| Tipo di relazione | Da → A | Direzione | Semantica | Esempio |
|---|---|---|---|---|
| **VICINO_A** | qualsiasi → qualsiasi | bidirezionale | prossimità geografica / di percorrenza | POI vicino a un altro POI |
| **SI_TROVA_IN** | POI/Esercizio/Evento → Luogo/Area | direzionale | appartenenza/contenimento | Museo dentro una città |
| **TAPPA_DI** | POI/Esercizio → Itinerario | direzionale | il nodo è una tappa del percorso | Borgo come tappa di una route |
| **ABBINATO_A** | Esercizio/Esperienza ↔ Esercizio/Esperienza | bidirezionale | complementarità esperienziale | Cantina abbinata ad agriturismo |
| **SIMILE_A** | qualsiasi ↔ qualsiasi | bidirezionale | similarità tematica/semantica | Due borghi medievali simili |
| **HA_TEMA** | qualsiasi → Tema/Tag | direzionale | classificazione tematica | POI con tema "arte" |
| **OSPITA_EVENTO** | Luogo/POI → Evento | direzionale | il luogo ospita l'evento | Piazza che ospita una sagra |
| **PRODUCE / OFFRE** | Esercizio/Operatore → Prodotto/Esperienza | direzionale | offerta | Cantina che produce un vino |
| **GESTITO_DA** | Esercizio/Esperienza → Persona/Operatore | direzionale | titolarità | Tour gestito da una guida |
| **RECENSISCE** | Recensione → nodo target | direzionale | valutazione | Recensione di un ristorante |
| **CONTRIBUITO_DA** | nodo/arco → Persona (contributor) | direzionale | attribuzione/provenienza | POI creato da un curatore |
| **RAGGIUNGIBILE_CON** | POI/Itinerario → mezzo (attributo/nodo) | direzionale | accessibilità di trasporto | Sentiero raggiungibile a piedi |

### 5.3 Criteri per il peso degli archi

Il peso è un valore normalizzato (es. 0–1) **derivato** da fattori configurabili, mai inserito arbitrariamente. È spiegabile: il sistema può mostrare la scomposizione del peso (requisito di trasparenza). Fattori per tipo di relazione:

| Fattore | Applicabile a | Effetto sul peso |
|---|---|---|
| **Distanza geografica / di percorrenza** | VICINO_A, TAPPA_DI, RAGGIUNGIBILE_CON | più vicino ⇒ peso maggiore (decadimento con la distanza) |
| **Similarità semantica** (cosine su Qdrant) | SIMILE_A, HA_TEMA, ABBINATO_A | maggiore similarità ⇒ peso maggiore |
| **Co-occorrenza negli itinerari/sessioni** | ABBINATO_A, VICINO_A | luoghi spesso visitati/salvati insieme ⇒ peso maggiore |
| **Segnale community** (voti, salvataggi, recensioni positive) | RECENSISCE, qualsiasi | feedback positivo ⇒ rinforzo |
| **Reputazione del contributor** | CONTRIBUITO_DA, archi proposti | contributi da autori affidabili ⇒ peso e fiducia maggiori |
| **Affidabilità/completezza della fonte** | archi da ingestione | fonte autorevole e dato completo ⇒ peso baseline maggiore |
| **Freschezza / stagionalità** | OSPITA_EVENTO, esperienze stagionali | rilevanza decade fuori finestra temporale |
| **Frequenza d'uso / navigazione** | qualsiasi | archi più percorsi nell'esplorazione ⇒ rinforzo |
| **Penalità anti-popolarità (hidden-gem boost)** | ranking finale | down-weight della sola popolarità assoluta per far emergere i luoghi nascosti, modulato dalla preferenza utente |

Il peso è ricalcolato in **batch** (job schedulato) a partire dai segnali accumulati, e usato sia nella navigazione/ranking sia come contesto per il GraphRAG. La formula di combinazione dei fattori è configurabile per dominio (pesi dei fattori esposti a config), così l'enoturismo può privilegiare l'abbinamento mentre l'escursionismo privilegia la prossimità di percorrenza.

### 5.4 Persistenza su MySQL + Qdrant (no Neo4j)

- **MySQL** memorizza la struttura: tabella `graph_node` (id, tipo, attributi tipizzati/JSON, geo, lingua), tabella `graph_edge` (sorgente, destinazione, tipo, direzione, peso, attributi, fonte), più tabelle di supporto (tassonomia, reputazione, revisioni). Le query di vicinato/percorso si appoggiano a indici su tipo/geo e a query ricorsive (CTE) controllate per profondità, coerenti con il vincolo "una sola query per migrazione Flyway".
- **Qdrant** memorizza gli embedding delle descrizioni/tag dei nodi per il seed semantico e il calcolo di SIMILE_A.
- Il mapping UUID segue la convenzione di progetto (`@JdbcTypeCode(SqlTypes.CHAR)`); le parole riservate MySQL vanno escapate nelle DDL.

---

## 6. Fonti dati & connettori (ingestione)

L'ingestione riusa il pattern Spring Batch (folder-scan / document-ingestion) e gli extension point dei plugin PF4J, aggiungendo connettori specifici per il territorio. Ogni connettore è un componente sostituibile, idealmente impacchettabile come plugin del marketplace.

| Fonte | Tipo | Cosa fornisce | Licenza / nota | Priorità |
|---|---|---|---|---|
| **OpenStreetMap (Overpass API)** | aperta | POI, esercizi, sentieri, geometrie, tag | ODbL (attribuzione) | MVP |
| **Wikidata / Wikipedia** | aperta | entità, descrizioni multilingua, relazioni, immagini | CC0 / CC BY-SA | MVP |
| **Open data turistici regionali / DMO** | aperta | POI/eventi/tour ufficiali (schema.org, GeoJSON, CSV) | varie aperte | MVP/evoluzione |
| **Feed eventi (iCal, schema.org Event, sagre)** | semi-aperta | eventi e ricorrenze | varie | Evoluzione |
| **Contributi community** | interna | luoghi nascosti, recensioni, relazioni | proprietà istanza | MVP |
| **GPX / tracce itinerari** | file | percorsi escursionistici/cicloturistici | utente | Evoluzione |
| **Documenti locali (PDF, brochure)** | file | guide, materiali pro loco → estrazione entità | Tika/OCR esistenti | Evoluzione |
| **Email / calendario (moduli esistenti)** | interna | eventi, prenotazioni, comunicazioni territoriali | privacy-aware | Evoluzione |
| **Meteo / stagionalità (API)** | esterna opzionale | arricchimento contestuale | opzionale, consenso | Evoluzione |

Requisiti comuni dei connettori: mappatura alla tassonomia, normalizzazione IT/EN, deduplica/riconciliazione, conservazione licenza e provenienza, gestione incrementale (solo record cambiati), rispetto del local-first (nessun invio dati esterni non necessario). I connettori esistenti (Tika, OCR, folder watcher, email/calendar) sono riusati; i nuovi (OSM, Wikidata, open data) sono da creare.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

Mappa concreta del lavoro, distinguendo MVP, evoluzioni e manutenzione continua. La colonna "Stato" indica se si **crea** ex-novo, si **estende** un modulo esistente o si **riusa**.

### 7.1 MVP (primo rilascio del verticale)

| # | Funzionalità | Layer | Stato | Note |
|---|---|---|---|---|
| 1 | Modello dati a grafo territorio (nodi/archi tipizzati) | domain + persistence | Creare (su base `knowledge`) | Tabelle `graph_node`/`graph_edge`, Flyway una query/file |
| 2 | API CRUD nodi e archi | api + domain | Creare | `/api/v1/territory` o `/api/v1/graph` |
| 3 | Query di grafo base (vicini, sottografo, per relazione) | domain + persistence | Creare | CTE MySQL + indici geo/tipo |
| 4 | Embedding nodi su Qdrant | infrastructure | Riusare/estendere | EmbeddingModel Ollama `@Primary` |
| 5 | Ricerca ibrida GraphRAG (seed Qdrant + espansione grafo) | domain | Creare | Cuore del verticale |
| 6 | Endpoint discovery NL + filtri | api | Creare | Validazione boundary input §3.1 |
| 7 | Sintesi AI con citazione nodi/percorsi | domain | Estendere LLM gateway | Riuso fallback chain |
| 8 | Connettore OpenStreetMap (Overpass) | batch/plugin | Creare | Ingestione POI/sentieri area |
| 9 | Connettore Wikidata | batch/plugin | Creare | Descrizioni multilingua |
| 10 | Pipeline ingestione (mappatura, dedup, pesi baseline) | batch | Estendere | Riuso pattern document-ingestion |
| 11 | UI contribuzione nodi/archi | frontend | Creare | Form reattivi, dedup live |
| 12 | UI ricerca + lista/schede risultati | frontend | Creare | Signal store |
| 13 | Vista mappa con marker dei nodi | frontend | Creare | Libreria mappe open |
| 14 | Vista grafo interattiva (nodi/archi/peso) | frontend | Creare | Espansione progressiva |
| 15 | Recensioni/valutazioni + reputazione base | domain + api + fe | Creare | Anti-spam, rate limiting |
| 16 | Calcolo pesi archi (batch) con fattori base | domain + batch | Creare | Distanza + semantica + community |
| 17 | Moderazione/curatela contributi | domain + api + fe | Creare | Coda, approva/respingi |
| 18 | i18n IT/EN di enum, tassonomia, UI | tutti | Estendere | Enum tradotte verso frontend |
| 19 | Documentazione IT+EN del verticale | docs | Creare | `documentation/` + `documentazione/` |

### 7.2 Evoluzioni future

| Funzionalità | Valore | Note |
|---|---|---|
| Generatore di itinerari multi-tappa ottimizzati | scoperta avanzata | vincoli tempo/mezzo/dislivello, ottimizzazione percorso |
| Connettori open data DMO regionali e feed eventi | copertura | schema.org/GeoJSON/iCal |
| Hidden-gem boost configurabile e spiegabile | anti-overtourism | de-correlazione dalla popolarità |
| Personalizzazione su interessi storici dell'utente | rilevanza | profilo locale, privacy-first |
| Suggerimento AI di relazioni mancanti tra nodi | arricchimento grafo | link prediction su embedding |
| Export GPX/GeoJSON e condivisione itinerari | portabilità dati | sovranità utente |
| Modalità offline / sincronizzazione per uso in viaggio | local-first in mobilità | cache istanza |
| Moduli verticali (montagna, enoturismo, borghi) | estensibilità | nuovi tipi nodo via plugin |
| Gamification contributor (badge, reputazione avanzata) | crescita community | qualità contributi |
| Integrazione meteo/stagionalità nel ranking | contesto | API opzionale con consenso |
| Pubblicazione istanza DMO white-label | adozione enti | branding self-hosted |

### 7.3 Manutenzione continua

- Ricalcolo periodico dei pesi e ranking emergente; tuning dei fattori per evitare derive.
- Aggiornamento incrementale dei connettori (OSM/Wikidata cambiano spesso); gestione versioni e licenze.
- Qualità dati: dedup ricorrente, merge, pulizia nodi orfani, revisione tassonomia.
- Moderazione continua e anti-abuso (spam, fake review, manipolazione ranking).
- Aggiornamento documentazione bilingue e log in `Sviluppi/` per ogni sviluppo.
- Monitoraggio metriche (Actuator/Prometheus) e performance delle query di grafo.

---

## 8. Casi d'uso AI / GraphRAG

| Caso d'uso | Query esempio | Come l'AI usa il grafo |
|---|---|---|
| **Scoperta relazionale** | "Borghi medievali con cantine bio vicino a Siena, poca folla" | Seed semantico → espansione VICINO_A/HA_TEMA/ABBINATO_A → ranking con hidden-gem boost → citazione nodi |
| **Itinerario su misura** | "Weekend in moto tra borghi e cantine, no autostrada" | Seleziona POI/esercizi, ordina come TAPPA_DI di un itinerario, ottimizza per mezzo e tempo |
| **Collegamenti non evidenti** | "Cosa abbinare a questa degustazione nei dintorni?" | Naviga ABBINATO_A/VICINO_A/SIMILE_A dal nodo, suggerisce esperienze complementari |
| **Domande contestuali** | "Cosa succede questo weekend entro 30 km?" | Filtro temporale su OSPITA_EVENTO + raggio geo, sintesi eventi |
| **Suggerimento relazioni (curatela)** | (lato contributor) "Quali relazioni mancano per questo POI?" | Link prediction su embedding + prossimità, propone archi da validare |
| **Spiegazione trasparente** | "Perché mi consigli questo posto?" | Mostra il percorso di nodi/archi e la scomposizione del peso |
| **Esplorazione conversazionale** | dialogo multi-turno sul territorio | Mantiene il sottografo come contesto, espande progressivamente |

Tutti i casi d'uso girano su Ollama in locale di default (privacy), con fallback ai provider cloud configurati. Le risposte citano sempre i nodi/percorsi usati, requisito di trasparenza che distingue LocalMind dai ranking opachi dei competitor.

---

## 9. KPI & metriche di successo

| Categoria | KPI | Obiettivo / segnale |
|---|---|---|
| **Scoperta** | % di luoghi suggeriti fuori dalla top-popolarità (hidden-gem rate) | Alto: misura la redistribuzione dei flussi |
| **Qualità AI** | Tasso di risposte con citazione valida del grafo; tasso di hallucination | Citazioni alte, hallucination bassa |
| **Rilevanza** | CTR sui suggerimenti, salvataggi/itinerario, completion degli itinerari | In crescita |
| **Crescita grafo** | Nuovi nodi/archi per periodo; copertura geografica | In crescita sostenuta |
| **Community** | Contributor attivi, contributi approvati, tempo medio di moderazione | Attivi in crescita, moderazione rapida |
| **Qualità dati** | Tasso di duplicati, nodi con campi obbligatori completi, % bilingue IT/EN | Duplicati bassi, completezza alta |
| **Performance** | Latenza query di grafo (vicini/sottografo), latenza GraphRAG end-to-end | Entro soglie definite |
| **Sovranità** | % inferenze servite in locale (Ollama) vs cloud | Locale di default prevalente |
| **Adozione** | Istanze self-hosted (DMO/comuni), retention esploratori | In crescita |

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---|---|---|
| **Cold start del grafo** (poca copertura iniziale) | Valore basso all'avvio | Bootstrap via ingestione OSM/Wikidata/open data prima del lancio community |
| **Scarsa partecipazione community** | Grafo non cresce, niente hidden gems | UX di contribuzione semplice, reputazione/gamification, onboarding DMO |
| **Qualità e duplicati dei dati** | Grafo rumoroso, AI imprecisa | Dedup spaziale+semantica, riconciliazione, moderazione, validazione boundary |
| **Manipolazione del ranking / fake review** | Perdita di fiducia | Reputazione, anti-spam, rate limiting, hidden-gem boost, peso spiegabile |
| **Performance query di grafo su MySQL** (no Neo4j) | Latenza alta su grafi grandi | Indici geo/tipo, CTE con profondità limitata, caching, denormalizzazioni mirate; rivalutare datastore solo se necessario |
| **Hallucination AI** | Suggerimenti inventati | GraphRAG con contesto vincolato al sottografo, citazione obbligatoria dei nodi |
| **Licenze dati non compatibili** | Rischio legale/etico | Tracciamento licenza per fonte, rifiuto fonti incompatibili, attribuzione |
| **Privacy posizione/query utente** | Violazione fiducia | Local-first, inferenza Ollama locale, consenso esplicito per dati esterni |
| **Overtourism indotto** se il ranking premia i soliti noti | Effetto contrario alla missione | Penalità anti-popolarità configurabile, monitoraggio hidden-gem rate |
| **Frammentazione tassonomia** | Grafo incoerente | Tassonomia controllata allineata a schema.org/OSM, governance delle estensioni |

---

## 11. Manutenzione & evoluzione

- **Ricalcolo pesi e ranking:** job batch schedulati, con parametri dei fattori versionati e tuning monitorato per evitare derive del ranking.
- **Aggiornamento connettori:** sync incrementale OSM/Wikidata/open data, gestione cambi di schema e licenze; ciascun connettore evolvibile come plugin PF4J indipendente.
- **Igiene del grafo:** dedup ricorrente, merge nodi, rimozione orfani, revisione periodica della tassonomia e delle relazioni a basso peso.
- **Governance community:** policy di moderazione, gestione reputazione, gestione abusi; metriche di salute della community.
- **Estensibilità per dominio:** nuovi moduli (montagna, enoturismo, borghi, accessibilità) aggiungono tipi di nodo/relazione senza toccare il core; distribuiti via marketplace.
- **Documentazione:** aggiornamento costante IT (`documentazione/`) ed EN (`documentation/`), più log di sviluppo datati in `Sviluppi/` (numerazione progressiva giornaliera) come da CLAUDE.md.
- **Osservabilità:** metriche Actuator/Prometheus su latenza grafo, qualità AI, crescita dati; dashboard dedicata.
- **Evoluzione datastore:** il vincolo "no Neo4j" è rivalutabile in cicli futuri solo se le query di grafo lo richiedono in modo dimostrato dalle metriche di performance.

---

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo esistente | Ruolo nel verticale territorio |
|---|---|
| **`knowledge`** | Base del motore a grafo: i tipi di nodo/relazione territorio estendono questo dominio (schema modulare) |
| **`llm` + `LlmGatewayService`** | Comprensione query, sintesi GraphRAG, suggerimento relazioni; Ollama default + fallback chain |
| **Qdrant (`vectorstore`)** | Embedding dei nodi per seed semantico e SIMILE_A; EmbeddingModel Ollama `@Primary` |
| **MySQL + Flyway** | Persistenza struttura grafo (`graph_node`/`graph_edge`), tassonomia, reputazione; migrazioni una query/file |
| **`document` + Tika/OCR + batch** | Ingestione di guide/brochure locali in nodi; riuso pattern folder-scan/document-ingestion |
| **`plugin` (PF4J) + `marketplace`** | Connettori (OSM, Wikidata, open data) e moduli verticali distribuiti come plugin installabili |
| **`auth`** | Identità contributor, reputazione, moderazione; local-first |
| **`email` + `calendar`** | Ingestione eventi/comunicazioni territoriali (evoluzione), privacy-aware |
| **`automation`** | Trigger di ingestione, ricalcolo pesi, notifiche su nuovi eventi/luoghi |
| **`messaging` / channels** | Notifiche e condivisione di itinerari/eventi verso canali |
| **`agent`** | Agente di esplorazione conversazionale del grafo (multi-turno) |
| **`common` (eventi di dominio, analytics)** | Eventi su creazione nodi/voti per side-effects (ricalcolo pesi, analytics) |
| **Frontend Angular (feature-driven, Signals)** | Nuova feature lazy-loaded "territorio" con viste lista/mappa/grafo, Signal store, i18n IT/EN |
| **API `/api/v1/*` + GlobalExceptionHandler** | Nuovi endpoint discovery/CRUD grafo, validazione boundary, errori localizzati |

Il verticale **non introduce nuova infrastruttura**: estende i domini esistenti, riusa MySQL+Qdrant+Ollama e il sistema di plugin, e rispetta tutti i vincoli di progetto (local-first, privacy, IT/EN, immutabilità, Flyway una query, file piccoli e coesi). È la prima dimostrazione consumer del motore di grafo universale, gemella e simmetrica rispetto ai verticali enterprise.

---

### Fonti e riferimenti di settore

- TravelRAG — framework GraphRAG multi-layer per il retrieval di attrazioni turistiche (MDPI ISPRS IJGI, 2024).
- "Exploring the Landscape of Tourism Knowledge Graphs: A Systematic Literature Review" (IEEE).
- Knowledge graph-driven personalized attractions recommendation con modellazione interessi long/short-term (ScienceDirect, 2025).
- Tyrolean Tourism Knowledge Graph — ecosistema basato su schema.org (arXiv 1805.05744).
- Open Data Tourism Alliance (ODTA) e progetto Open Data Destination Germany — standardizzazione contenuti turistici su schema.org (POI, Tour, Event).
- schema.org (`TouristAttraction`, `TouristTrip`, `Event`), OpenStreetMap (ODbL), Wikidata (CC0) come basi di tassonomia e ingestione.
