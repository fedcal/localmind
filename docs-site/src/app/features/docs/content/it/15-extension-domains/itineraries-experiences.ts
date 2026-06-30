export const content = `# Itinerari & esperienze

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema concreto

Pianificare un viaggio, una gita o anche solo un pomeriggio di scoperta del territorio
è oggi un'attività **frammentata, manuale e cognitivamente costosa**. Chi vuole vivere
un'esperienza autentica deve incrociare a mano decine di fonti eterogenee: blog,
recensioni, mappe, calendari di eventi, orari di apertura, meteo, trasporti, gruppi
social locali. Il risultato è quasi sempre uno di questi tre fallimenti:

1. **Itinerari generici e turistici** — le piattaforme mainstream propongono sempre
   gli stessi dieci POI "da non perdere", spingendo tutti negli stessi luoghi alle
   stesse ore, con esperienze appiattite e overtourism nei punti caldi mentre il
   territorio reale resta invisibile.
2. **Sovraccarico informativo senza sintesi** — l'utente trova *troppi* dati ma nessuno
   che li **colleghi** in un percorso coerente, fattibile e cucito sui suoi vincoli reali
   (tempo, budget, mezzo di trasporto, presenza di bambini, accessibilità, stagione).
3. **Perdita della conoscenza locale** — la conoscenza più preziosa (il produttore che
   apre la cantina solo su prenotazione, il sentiero panoramico noto agli abitanti, la
   sagra di paese) vive nella testa delle persone e in canali informali, e non viene mai
   strutturata in modo riutilizzabile.

Gli attuali "AI trip planner" generativi mitigano in parte il problema ma soffrono di un
limite strutturale: **allucinano**. Un LLM puro inventa orari, distanze, luoghi inesistenti
o eventi mai accaduti, perché ragiona sulla probabilità del testo e non su una base di
conoscenza verificata e collegata. Per la pianificazione di viaggi — dove un orario
sbagliato o un POI chiuso rovinano la giornata — questo è inaccettabile.

### 1.2 La nostra risposta: itinerari nativi sul grafo

LocalMind affronta il problema in modo radicalmente diverso, sfruttando il suo motore di
**Knowledge Graph universale**. Un itinerario non è un testo generato a caso, ma un
**cammino reale e pesato sul grafo della conoscenza del territorio**: una sequenza ordinata
di nodi (luoghi, POI, eventi, esperienze) connessi da archi pesati (vicinanza geografica,
affinità tematica, percorribilità, co-visita storica, complementarità). L'AI (GraphRAG)
**naviga** questo grafo per costruire percorsi, e ogni tappa proposta è **ancorata a un
nodo verificato e citabile** — niente invenzioni.

| Approccio | Come genera l'itinerario | Rischio allucinazione | Conoscenza locale |
|-----------|--------------------------|-----------------------|-------------------|
| Liste turistiche statiche | Ranking fisso, manuale | Nullo ma generico | Assente |
| AI generativa pura (LLM) | Testo probabilistico | Alto | Inaffidabile |
| **LocalMind (GraphRAG sul grafo)** | Cammino pesato su nodi verificati | Basso (ogni tappa è un nodo reale) | Strutturata e community-driven |

### 1.3 Valore generato

- **Per il viaggiatore/cittadino**: itinerari personalizzati in pochi secondi, fattibili,
  realmente cuciti sui propri vincoli e interessi, con scoperta di esperienze autentiche
  fuori dai circuiti di massa.
- **Per il territorio**: redistribuzione dei flussi verso luoghi minori, valorizzazione di
  produttori, artigiani, piccoli eventi; lotta all'overtourism dei punti caldi.
- **Per la community**: la conoscenza locale viene strutturata una volta e riutilizzata
  infinite volte; chi contribuisce vede il proprio sapere "vivere" dentro gli itinerari.
- **Per LocalMind come piattaforma**: l'ambito "Itinerari & esperienze" è la **prova
  vivente del verticale consumer** e il banco di prova più espressivo del motore a grafo —
  un itinerario è letteralmente un *path query* pesato e multi-vincolo.
- **Coerenza con i vincoli di progetto**: tutto gira **local-first** con **Ollama di
  default**; nessun dato dell'utente (preferenze, posizione, viaggi) lascia
  l'installazione self-hosted senza consenso. Niente costi per API cloud obbligatorie.

### 1.4 Perché ora e perché noi

Il 2026 vede gli AI trip planner come categoria matura ma dominata da SaaS proprietari,
cloud-only, che monetizzano i dati di viaggio degli utenti. LocalMind porta un'alternativa
**open source, self-hostable e privacy-first**, ideale per: enti territoriali e Pro Loco
che vogliono un portale di scoperta proprietario; comunità e associazioni che mappano il
loro territorio; singoli appassionati che vogliono un planner privato senza tracciamento.
Il riuso di **MySQL (struttura del grafo) + Qdrant (semantica)** evita di introdurre un
database a grafo dedicato, restando dentro lo stack esistente.

## 2. Personas & utenti target

| Persona | Descrizione | Obiettivo principale | Vincoli tipici |
|---------|-------------|----------------------|----------------|
| **Esploratore weekend** | Cittadino/turista che pianifica gite di 1-3 giorni | Scoprire esperienze autentiche vicine, ottimizzando il poco tempo | Tempo limitato, budget medio, mezzo proprio |
| **Viaggiatore slow/tematico** | Appassionato di un tema (enogastronomia, trekking, arte, borghi) | Percorsi tematici coerenti e approfonditi | Interesse verticale forte, stagionalità |
| **Famiglia con bambini** | Genitori che organizzano uscite | Tappe family-friendly, ritmi sostenibili, sicurezza | Accessibilità, orari pranzo/riposo, distanze brevi |
| **Local contributor / curatore** | Abitante o appassionato che conosce il territorio | Inserire e curare luoghi, esperienze, eventi nel grafo | Strumenti di editing semplici, riconoscimento |
| **Operatore turistico / Pro Loco** | Ente o associazione che gestisce un'istanza self-hosted | Offrire un portale di itinerari sul proprio territorio | Branding, moderazione, dati on-premise |
| **Viaggiatore accessibilità-first** | Persona con esigenze di mobilità o sensoriali | Percorsi realmente percorribili e inclusivi | Vincoli di accessibilità stringenti |
| **Power user / self-hoster privacy** | Utente tecnico che rifiuta i SaaS di viaggio | Planner privato, AI locale, zero tracciamento | Tutto offline/on-prem, Ollama |

Le personas guidano sia i **profili di personalizzazione** (sezione 3) sia i **pesi
dinamici** sugli archi (sezione 5): es. la famiglia penalizza archi con tempi di percorrenza
lunghi, il viaggiatore tematico amplifica gli archi di affinità tematica.

## 3. Requisiti in input

La qualità di un itinerario dipende interamente dalla qualità e completezza degli input.
Distinguiamo input **obbligatori** (minimo per generare), **opzionali** (migliorano la
personalizzazione), **derivati/contestuali** (raccolti automaticamente) e **vincoli di
sistema/dati**.

### 3.1 Input obbligatori (minimo per generare un itinerario)

| Input | Descrizione | Esempio | Validazione |
|-------|-------------|---------|-------------|
| **Area/punto di partenza** | Località, indirizzo o coordinate del punto di origine | "Verona, Piazza Bra" / \`45.43,10.99\` | Geocodifica obbligatoria; deve risolvere a un nodo o coordinata valida |
| **Orizzonte temporale** | Durata complessiva | "1 giorno", "weekend", "26-28 luglio" | Date coerenti (inizio ≤ fine), durata > 0 |
| **Almeno un interesse o un tema** | Cosa cerca l'utente | "enogastronomia", "trekking facile" | Mappato su tassonomia tematica del grafo |

### 3.2 Input opzionali di personalizzazione

| Categoria | Input | Effetto sull'algoritmo |
|-----------|-------|------------------------|
| **Budget** | Tetto di spesa totale o per tappa, fascia di prezzo | Filtro/penalità su nodi e archi con costo associato |
| **Mezzo di trasporto** | A piedi, bici, auto, mezzi pubblici | Sceglie la matrice tempi/distanze e la percorribilità degli archi |
| **Ritmo** | Rilassato / equilibrato / intenso | Numero di tappe/giorno, durata buffer tra tappe |
| **Composizione gruppo** | Solo, coppia, famiglia (età bimbi), gruppo, animali | Attiva filtri family/pet-friendly, accessibilità |
| **Accessibilità** | Sedia a rotelle, esigenze sensoriali, no scale | Vincolo hard su attributi di accessibilità dei nodi |
| **Preferenze alimentari** | Vegetariano, vegano, celiaco, halal | Filtro sui nodi ristorazione |
| **Stile/personalità** | Avventuroso, culturale, gourmet, fotografico, off-the-beaten-path | Modula pesi tematici e preferenza per nodi "nascosti" vs popolari |
| **Esclusioni** | Luoghi/categorie da evitare, già visti | Esclusione hard di nodi o sottocategorie |
| **Must-include** | Tappe imposte dall'utente | Nodi fissati come waypoint obbligatori del cammino |
| **Finestre orarie** | Vincoli (es. "rientro entro le 19", "pranzo 12:30-14") | Vincoli temporali sull'ordinamento delle tappe |
| **Livello sforzo fisico** | Basso/medio/alto, dislivello max (trekking) | Filtro su difficoltà dei percorsi/esperienze |

### 3.3 Input contestuali (raccolti automaticamente, con consenso)

| Input | Fonte | Uso |
|-------|-------|-----|
| **Stagione e meteo** | Connettore meteo / data corrente | Penalizza attività outdoor con maltempo, privilegia indoor |
| **Stagionalità del nodo** | Attributi del grafo | Esclude POI/eventi fuori stagione o chiusi |
| **Orari di apertura** | Attributi del grafo | Vincolo temporale: il nodo deve essere aperto nella finestra di visita |
| **Storico utente** | Profilo locale (se loggato) | Evita ripetizioni, impara preferenze nel tempo |
| **Posizione real-time** | Device (opt-in) | Ri-pianificazione "da dove sono ora" |
| **Affluenza/eventi concomitanti** | Grafo eventi | Evita sovrapposizioni o consiglia in base agli eventi attivi |

### 3.4 Vincoli di sistema e qualità dei dati

- **Completezza minima del nodo**: un nodo è eleggibile come tappa solo se possiede almeno
  geolocalizzazione, categoria e (se applicabile) orari. Nodi incompleti sono usabili solo
  come contesto, non come tappa proposta.
- **Affidabilità**: ogni nodo/arco porta un punteggio di affidabilità (vedi sezione 5);
  sotto soglia, il nodo è escluso o segnalato come "da verificare".
- **Lingua**: tutti gli input testuali liberi sono accettati in IT/EN; la tassonomia
  tematica è bilingue e l'output rispetta la lingua dell'utente.
- **Privacy**: gli input di profilazione restano locali; nessun invio a provider cloud
  senza consenso esplicito. Con Ollama di default, anche l'inferenza è on-premise.
- **Validazione ai boundary**: tutti gli input passano da validazione schema sul DTO REST
  (\`@Valid\`) prima di raggiungere il dominio; fail-fast con messaggi chiari bilingui.

### 3.5 Modello di richiesta (concettuale)

L'insieme degli input viene normalizzato in un oggetto immutabile \`ItineraryRequest\` con,
tra gli altri, i campi: \`origin\`, \`timeWindow\`, \`themes[]\`, \`budget\`, \`transportMode\`,
\`pace\`, \`groupProfile\`, \`accessibility\`, \`mustInclude[]\`, \`exclude[]\`, \`constraints[]\`.
Questo oggetto è il contratto unico tra API, dominio e motore GraphRAG; ogni profilo
persona (sezione 2) è esprimibile come preset di questo oggetto.

## 4. Flusso dell'attività (step-by-step)

Il flusso copre l'intero ciclo dalla richiesta dell'utente alla consegna e adattamento
dell'itinerario. È pensato per essere **idempotente e immutabile**: ogni rigenerazione
produce un nuovo oggetto itinerario, non muta il precedente (coerenza con le regole di
immutabilità di progetto).

### 4.1 Diagramma di flusso ad alto livello

\`\`\`text
[1] Input utente (form/chat)        →  ItineraryRequest (validato)
        │
        ▼
[2] Normalizzazione & profilazione  →  pesi dinamici + vincoli hard/soft
        │
        ▼
[3] Retrieval candidati sul grafo   →  Qdrant (semantica) + MySQL (struttura/vicinato)
        │
        ▼
[4] Costruzione sottografo pesato   →  nodi candidati + archi con pesi contestuali
        │
        ▼
[5] Ottimizzazione del cammino      →  TTDP/Orienteering: max valore sotto i vincoli
        │
        ▼
[6] Sintesi narrativa (LLM/Ollama)  →  descrizioni, motivazioni, citazione nodi
        │
        ▼
[7] Presentazione & interazione     →  timeline, mappa, grafo; modifiche utente
        │
        ▼
[8] Persistenza, export, feedback   →  salvataggio, condivisione, segnali di apprendimento
        │
        ▼
[9] Adattamento real-time           →  ri-pianificazione su evento/posizione/meteo
\`\`\`

### 4.2 Dettaglio degli step

**Step 1 — Acquisizione input.**
L'utente compila un form guidato (frontend Angular) oppure dialoga in linguaggio naturale
con la chat ("vorrei un weekend gastronomico tra i colli, senza guidare troppo, budget
medio"). In modalità chat, l'LLM estrae gli slot della \`ItineraryRequest\` (slot filling) e
chiede chiarimenti solo sui campi obbligatori mancanti. L'output dello step è una
\`ItineraryRequest\` candidata.

**Step 2 — Validazione, normalizzazione e profilazione.**
La richiesta è validata ai boundary (schema DTO). Si geocodifica l'origine, si risolvono
date/finestre temporali, si mappano gli interessi liberi sulla tassonomia tematica
bilingue. Si calcolano i **pesi dinamici** da applicare agli archi e i **vincoli**:
- *Vincoli hard* (non negoziabili): accessibilità, esclusioni, orari di apertura,
  must-include, budget tetto, finestra temporale.
- *Vincoli soft* (preferenze pesate): affinità tematica, ritmo, popolarità vs scoperta,
  stagione/meteo.

**Step 3 — Retrieval dei candidati (GraphRAG, fase di recupero).**
Si recuperano i nodi candidati combinando due segnali, coerenti con lo stack:
- **Semantico (Qdrant)**: similarità tra l'embedding della richiesta e gli embedding dei
  nodi (descrizioni di luoghi/esperienze) — trova candidati per *significato*.
- **Strutturale (MySQL)**: vicinato geografico (entro un raggio/bounding box dall'origine e
  lungo la direttrice), filtri per categoria, orari, attributi hard.
Il risultato è un insieme di nodi candidati già filtrati sui vincoli hard.

**Step 4 — Costruzione del sottografo pesato.**
Dai candidati si costruisce in memoria un **sottografo** del territorio: i nodi sono le
tappe possibili, gli archi rappresentano la relazione tra coppie di nodi (vicinanza,
tempo di percorrenza per il mezzo scelto, affinità tematica, co-visita storica). Ogni arco
riceve un **peso composito** calcolato con i pesi dinamici dello step 2. Gli archi non
percorribili (per il mezzo scelto) o che violano vincoli hard vengono potati (pruning).

**Step 5 — Ottimizzazione del cammino (cuore algoritmico).**
Selezionare e ordinare le tappe è un problema di **Tourist Trip Design / Orienteering
Problem**: massimizzare il "valore" raccolto (somma dei punteggi dei nodi, modulati
dall'affinità con il profilo) **rispettando** il budget di tempo, gli orari di apertura
(time windows), i must-include come waypoint obbligatori, e il budget economico. Strategia
implementativa progressiva:
- **MVP**: euristica greedy + miglioramento locale (inserzione del nodo a miglior
  rapporto valore/costo, 2-opt sull'ordine), con buffer di transito tra tappe.
- **Evoluzione**: metaeuristiche (GRASP, ricerca a vicinato variabile) per istanze grandi
  e multi-giorno, generazione di **top-k itinerari alternativi** anziché uno solo.
Output: una o più sequenze ordinate di tappe con orari stimati di arrivo/partenza.

**Step 6 — Sintesi narrativa (GraphRAG, fase di generazione).**
L'LLM (Ollama di default) riceve **solo i nodi e gli archi selezionati** come contesto
strutturato e genera: titolo dell'itinerario, descrizione di ogni tappa, **motivazione del
perché è stata scelta** ("inserita perché vicina e a tema con il tuo interesse per il
vino"), consigli pratici e transizioni tra tappe. Vincolo anti-allucinazione: il modello
**non può introdurre tappe non presenti nel sottografo selezionato**; ogni affermazione
fattuale è ancorata a un nodo citabile (provenienza). Le risposte citano i nodi/percorsi
usati.

**Step 7 — Presentazione e interazione.**
L'itinerario è mostrato su tre viste sincronizzate: **timeline** (giorno per giorno,
orari), **mappa** (tappe e tracciato), **mini-grafo** (tappe come nodi e relazioni — vetrina
del motore). L'utente può: rimuovere/bloccare una tappa, chiedere un'alternativa per una
singola tappa ("sostituisci il pranzo con qualcosa di più economico"), trascinare per
riordinare, allungare/accorciare la durata. Ogni modifica rilancia in modo mirato gli step
4-6 (ricalcolo locale, non da zero).

**Step 8 — Persistenza, export e feedback.**
L'itinerario può essere salvato (diventa esso stesso un **nodo \`Itinerario\`** nel grafo,
riutilizzabile e raccomandabile ad altri), esportato (PDF, calendario .ics verso il modulo
calendar, link condivisibile) e valutato. Il feedback (voti, "sono stato qui", segnalazioni
di errore) alimenta i pesi degli archi e l'affidabilità dei nodi (sezione 5 e 11).

**Step 9 — Adattamento in tempo reale (evoluzione).**
Durante il viaggio, un cambio di meteo, la chiusura imprevista di un POI, un ritardo o la
posizione corrente dell'utente innescano una **ri-pianificazione** del resto della giornata,
preservando le tappe già completate e i must-include futuri.

### 4.3 Gestione degli errori e dei casi limite nel flusso

| Situazione | Comportamento |
|------------|---------------|
| Pochi/nessun candidato (area povera di nodi) | Allarga progressivamente raggio/tema; suggerisce di rilassare un vincolo soft; messaggio chiaro bilingue |
| Vincoli hard inconciliabili (es. budget troppo basso) | Spiega quale vincolo blocca e propone il rilassamento minimo necessario |
| Nodo selezionato risulta chiuso/inaffidabile | Lo esclude e rigenera la sola tappa interessata |
| LLM non disponibile | Fallback nella catena provider; in ultima istanza output strutturato senza prosa narrativa |
| Input ambiguo in chat | Domanda di chiarimento solo sui campi obbligatori |

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa il motore a grafo universale (MySQL per struttura, Qdrant per semantica),
specializzandone i tipi per il dominio consumer "territorio". I tipi sono **estendibili per
dominio** secondo lo schema modulare già previsto.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi chiave |
|--------------|-------------|------------------|
| **Luogo / Area** | Località, borgo, quartiere, zona geografica | nome, geo (lat/lng/bbox), gerarchia territoriale |
| **POI** | Punto di interesse (monumento, museo, panorama, sentiero) | geo, categoria, orari, prezzo, accessibilità, stagionalità |
| **Esperienza** | Attività vivibile (degustazione, laboratorio, tour guidato, escursione) | durata, costo, prenotabilità, capienza, difficoltà |
| **Evento** | Manifestazione a tempo (sagra, concerto, mostra) | data/intervallo, luogo, ricorrenza, capienza |
| **Ristorazione / Hospitality** | Ristoranti, agriturismi, alloggi, cantine | tipo cucina, fascia prezzo, diete supportate, orari |
| **Itinerario** | Percorso salvato (anche generato dall'AI o curato) | sequenza tappe, tema, durata, autore, rating |
| **Tema / Tag** | Concetto trasversale (enogastronomia, arte, family, slow) | etichetta bilingue IT/EN, gerarchia tematica |
| **Persona / Operatore** | Guida, produttore, artigiano, host (consenso privacy) | ruolo, contatti pubblici, esperienze offerte |
| **Recensione / Contributo** | Valutazione o nota della community | autore, voto, testo, data, stato moderazione |

### 5.2 Tipi di relazione (archi)

| Relazione | Tra | Significato | Direzione |
|-----------|-----|-------------|-----------|
| **VICINO_A** | POI/Esperienza ↔ POI/Esperienza | Prossimità geografica / tempo di percorrenza | Non orientata |
| **PERCORRIBILE_VERSO** | Nodo → Nodo | Transito fattibile con un mezzo (tempo, distanza, dislivello) | Orientata (per mezzo) |
| **HA_TEMA** | Nodo → Tema | Classificazione tematica | Orientata |
| **COMPLEMENTARE_A** | Esperienza ↔ Esperienza | Si abbinano bene nello stesso percorso | Non orientata |
| **CO_VISITATO_CON** | POI ↔ POI | Spesso visitati insieme (segnale comportamentale) | Non orientata |
| **PARTE_DI** | POI/Esperienza → Itinerario/Area | Appartenenza/composizione | Orientata |
| **OFFERTO_DA** | Esperienza → Persona/Operatore | Chi eroga l'esperienza | Orientata |
| **SI_SVOLGE_IN** | Evento → Luogo/POI | Localizzazione dell'evento | Orientata |
| **STAGIONALE_IN** | Nodo → Stagione/Periodo | Validità temporale | Orientata |
| **RECENSISCE** | Recensione → Nodo | Valutazione della community | Orientata |
| **SIMILE_A** | Nodo ↔ Nodo | Similarità semantica (da embedding Qdrant) | Non orientata |
| **ALTERNATIVA_DI** | Nodo ↔ Nodo | Sostituibile in un itinerario (stesso ruolo) | Non orientata |

### 5.3 Criteri di peso degli archi

Il peso è il cuore del motore: governa cosa l'AI sceglie. Ogni arco porta un **peso
composito** normalizzato in [0,1], calcolato come media pesata di fattori; i coefficienti
sono **dinamici** e modulati dal profilo della richiesta (sezione 3).

| Fattore di peso | Si applica a | Razionale | Effetto |
|-----------------|--------------|-----------|---------|
| **Distanza / tempo di percorrenza** | VICINO_A, PERCORRIBILE_VERSO | Tappe vicine = percorso efficiente | Più vicino ⇒ peso (preferenza) più alto |
| **Affinità tematica** | HA_TEMA, COMPLEMENTARE_A | Coerenza col tema scelto | Più affine al profilo ⇒ peso più alto |
| **Co-visita / segnale comportamentale** | CO_VISITATO_CON | "La saggezza della folla" | Frequenza co-visita normalizzata |
| **Rilevanza / popolarità** | qualsiasi → nodo | Qualità intrinseca della tappa | Bilanciato con il fattore "scoperta" |
| **Feedback della community** | RECENSISCE, archi derivati | Voti, "ci sono stato", utilità | Rating medio pesato per affidabilità votante |
| **Affidabilità / freschezza** | tutti | Dati verificati e aggiornati | Penalità per dati vecchi o non verificati |
| **Percorribilità per mezzo** | PERCORRIBILE_VERSO | Coerenza col trasporto scelto | Arco potato se non percorribile col mezzo |
| **Disponibilità temporale** | STAGIONALE_IN, orari | Aperto/in stagione nella finestra | Vincolo hard; azzera l'eleggibilità altrimenti |
| **Diversità / anti-overtourism** | selezione | Evitare percorsi tutti uguali e punti saturi | Penalità su nodi già saturi o ripetuti |

Formula concettuale del peso di preferenza di un arco/nodo:

\`\`\`text
peso = Σ (coefficiente_profilo_i × fattore_normalizzato_i)   ,  con Σ coefficienti = 1
\`\`\`

I **vincoli hard** (orari, accessibilità, esclusioni, budget tetto, must-include) **non**
sono pesi: agiscono come filtri/potature prima dell'ottimizzazione. I **vincoli soft**
diventano coefficienti nel peso. Questo separa nettamente "cosa è ammissibile" da "cosa è
preferibile", rendendo l'algoritmo prevedibile e spiegabile.

### 5.4 Mappatura sullo storage (riuso MySQL + Qdrant)

- **MySQL**: tabelle \`graph_node\` (con \`node_type\`, attributi, geo) e \`graph_edge\` (con
  \`edge_type\`, \`source_id\`, \`target_id\`, \`weight\`, fattori componenti). Indici geografici e
  per tipo. UUID mappati con \`@JdbcTypeCode(SqlTypes.CHAR)\`. Ogni migrazione Flyway contiene
  **una sola query**.
- **Qdrant**: una collection con gli embedding delle descrizioni dei nodi, payload con
  \`node_id\`, \`node_type\`, geo e tag per il filtraggio ibrido (vettore + filtro).
- I pesi degli archi sono materializzati in MySQL ma **ricalcolabili**: i fattori
  comportamentali e di feedback sono aggiornati da job batch/eventi (sezione 11).

## 6. Fonti dati & connettori (ingestione)

L'ingestione popola e arricchisce il grafo, riusando la pipeline documentale esistente
(Tika/OCR → chunking → embedding → Qdrant) e i pattern dei connettori. Ogni fonte ha un
**adapter** che mappa i dati grezzi su nodi/archi tipizzati.

| Fonte | Tipo connettore | Nodi/archi prodotti | Note |
|-------|-----------------|---------------------|------|
| **Contributi community (UI)** | Editing manuale + form | Luoghi, POI, Esperienze, Recensioni | Cuore "Wikipedia dei luoghi"; passa da moderazione |
| **Open Data territoriali** | Importer (CSV/JSON/API enti) | POI, Eventi, orari | Cataloghi comunali/regionali, dataset turistici aperti |
| **OpenStreetMap / dati geo aperti** | Importer geo | POI, geometrie, VICINO_A | Base geografica e prossimità |
| **Calendari eventi** | Connettore + modulo \`calendar\` | Eventi, SI_SVOLGE_IN | Sagre, concerti, mostre |
| **Documenti/guide (PDF, schede)** | Pipeline \`document\` esistente | POI, Esperienze (estratti) + embedding | OCR per materiale scansionato |
| **Siti/feed (blog, portali)** | Web fetch/scraping (opt-in) | Candidati POI/Esperienze | Con verifica e moderazione |
| **Meteo** | Connettore real-time | Contesto (non persistito come tappa) | Penalità outdoor in step 2/9 |
| **Mappe/routing** | Provider tempi-distanze (locale o API) | Pesi PERCORRIBILE_VERSO | Matrice tempi per mezzo |
| **Feedback utenti** | Eventi interni | Aggiorna pesi/affidabilità | Loop di apprendimento |

Principi di ingestione: **deduplicazione/entity resolution** (lo stesso POI da fonti diverse
collassa in un nodo con provenienze multiple); **arricchimento incrementale** (i nodi
migliorano nel tempo); **tracciamento provenienza** (ogni attributo sa da dove viene, per
affidabilità e citazione GraphRAG); **rispetto privacy/licenze** delle fonti; tutto
eseguibile **on-premise**.

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

### 7.1 MVP (prima release verticale "Itinerari & esperienze")

| # | Funzionalità | Layer | Descrizione |
|---|--------------|-------|-------------|
| 1 | Tipi di nodo/relazione del dominio territorio | Domain + Flyway | Estensione schema grafo con i tipi della sez. 5 |
| 2 | \`ItineraryRequest\` + validazione | API + Domain | DTO, validazione boundary, mapping su tassonomia bilingue |
| 3 | Retrieval ibrido candidati | Domain + Infra | Qdrant (semantica) + MySQL (vicinato/filtri) |
| 4 | Costruzione sottografo pesato | Domain | Sottografo in memoria con pesi compositi e pruning |
| 5 | Ottimizzatore greedy + 2-opt (TTDP base) | Domain | Selezione/ordinamento tappe sotto vincoli tempo/budget/orari |
| 6 | Sintesi narrativa GraphRAG | Domain + LLM | Prosa con motivazioni e citazione nodi, anti-allucinazione |
| 7 | API REST itinerari | API | \`POST /api/v1/itineraries\` (genera), \`GET\` (recupera/salvati) |
| 8 | UI generazione + viste timeline/mappa | Frontend | Form guidato, risultato su timeline e mappa, bilingue IT/EN |
| 9 | Salvataggio itinerario come nodo | Domain + Persistence | Itinerario diventa nodo riusabile |
| 10 | Editing manuale nodi (contributi base) | API + Frontend | Creazione/modifica POI/Esperienze con moderazione minima |
| 11 | Feedback base (voto, "ci sono stato") | Domain + Frontend | Segnali iniziali per i pesi |
| 12 | Export .ics / PDF / link | API + Frontend | Integrazione modulo \`calendar\`, condivisione |

### 7.2 Evoluzioni (post-MVP)

| # | Funzionalità | Valore |
|---|--------------|--------|
| 1 | Generazione conversazionale completa (slot filling in chat) | UX naturale, riuso modulo chat |
| 2 | Top-k itinerari alternativi + metaeuristiche (GRASP/VNS) | Scelta tra varianti, scala su aree grandi |
| 3 | Itinerari multi-giorno con pernottamenti | Viaggi complessi |
| 4 | Ri-pianificazione real-time (meteo/posizione/chiusure) | Affidabilità in viaggio (step 9) |
| 5 | Vista mini-grafo interattiva delle tappe | Vetrina del motore, esplorazione |
| 6 | Personalizzazione adattiva (apprendimento dal profilo) | Migliora nel tempo |
| 7 | Percorsi tematici curati e collezioni community | Editoriale + community |
| 8 | Connettori open data / OSM / eventi avanzati | Copertura territoriale |
| 9 | Moderazione/curatela avanzata + reputazione contributor | Qualità della "Wikipedia dei luoghi" |
| 10 | Accessibilità avanzata e percorsi inclusivi certificati | Inclusività |
| 11 | Modulo installabile dal marketplace (PF4J) | Pacchetto "Territorio" distribuibile |
| 12 | Collaborazione su itinerario (più utenti) | Pianificazione di gruppo |

### 7.3 Da mantenere (manutenzione continua)

- Ricalcolo periodico dei pesi (feedback, co-visita, freschezza) via batch/eventi.
- Aggiornamento dati da connettori e re-embedding dei nodi modificati.
- Tuning dei coefficienti di profilo e valutazione qualità itinerari (sez. 9).
- Aggiornamento tassonomia tematica bilingue e traduzione enum IT/EN verso il frontend.
- Moderazione contributi e gestione segnalazioni di errore.
- Manutenzione adapter dei connettori al variare delle fonti esterne.

## 8. Casi d'uso AI / GraphRAG

| Caso d'uso | Come usa il grafo | Esempio |
|------------|-------------------|---------|
| **Generazione itinerario** | Retrieval ibrido → sottografo → ottimizzazione → sintesi | "Weekend gastronomico sui colli, no auto, budget medio" |
| **Sostituzione mirata di una tappa** | Cammino su archi ALTERNATIVA_DI / SIMILE_A nel vicinato | "Sostituisci il pranzo con qualcosa di più economico" |
| **Domande complesse sul territorio** | Path query multi-hop pesata | "Cosa abbino a una visita in cantina entro 30 min in bici?" |
| **Collegamenti non evidenti** | Suggerimento di archi mancanti / scoperta | "Pochi sanno che a 10 min c'è anche un laboratorio artigiano" |
| **Spiegabilità (perché questa tappa)** | Citazione di nodi/percorsi e fattori di peso | "Scelta per affinità tematica + vicinanza + ottimi voti" |
| **Ricerca esperienze autentiche** | Boost fattore "scoperta", penalità overtourism | "Esperienze fuori dai circuiti turistici" |
| **Ri-pianificazione contestuale** | Ricostruzione sottografo con vincolo meteo/posizione | "Piove: rifai il pomeriggio al coperto da dove sono" |
| **Itinerari simili a uno salvato** | Nodo Itinerario come seme, SIMILE_A | "Un percorso come quello dell'anno scorso ma in autunno" |

Tutti i casi rispettano il vincolo **anti-allucinazione**: l'LLM ragiona solo sul
sottografo recuperato e cita i nodi. Default di inferenza **Ollama** (local-first), con
fallback alla catena provider esistente quando configurata.

## 9. KPI & metriche di successo

| Categoria | KPI | Target indicativo |
|-----------|-----|-------------------|
| **Adozione** | Itinerari generati / utenti attivi | Crescita MoM |
| **Qualità output** | % itinerari salvati o esportati (vs scartati) | > 40% |
| **Personalizzazione** | Tasso di accettazione senza modifiche manuali | > 50% |
| **Fattibilità** | % tappe valide (aperte, raggiungibili nei tempi) | > 95% |
| **Anti-allucinazione** | % affermazioni ancorate a nodi verificati | ~100% |
| **Scoperta** | Quota di tappe "non mainstream" proposte | KPI anti-overtourism |
| **Feedback** | Rating medio itinerari, "ci sono stato" confermati | Trend in salita |
| **Community** | Nuovi nodi/recensioni per periodo, % approvati | Crescita |
| **Performance** | Tempo di generazione itinerario (P95) | < pochi secondi |
| **Copertura grafo** | Densità nodi/archi per area, completezza attributi | In crescita |
| **Privacy/local-first** | % generazioni servite da Ollama locale | Default 100% on-prem |
| **Affidabilità dati** | % nodi sopra soglia affidabilità | In crescita |

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---------|---------|-------------|
| **Allucinazioni LLM** | Tappe/orari falsi | GraphRAG con grounding sui nodi; il modello non può aggiungere tappe fuori dal sottografo; citazione obbligatoria |
| **Dati sparsi/incompleti (cold start)** | Itinerari poveri in aree non mappate | Seed da open data/OSM; allargamento progressivo; UX che invita a contribuire |
| **Qualità dei contributi community** | Spam, errori, dati obsoleti | Moderazione, reputazione contributor, affidabilità/freschezza nei pesi |
| **Complessità computazionale (TTDP NP-hard)** | Latenza su aree grandi/multi-giorno | Pruning, euristiche scalabili, cache, calcolo incrementale per le modifiche |
| **Pesi mal calibrati** | Itinerari poco rilevanti | Coefficienti configurabili, A/B sui pesi, loop di feedback |
| **Overtourism amplificato** | Concentrazione sui soliti POI | Fattore diversità/anti-saturazione nei pesi |
| **Privacy dei dati di viaggio** | Esposizione preferenze/posizione | Local-first, Ollama di default, nessun invio cloud senza consenso |
| **Dipendenza da fonti/licenze esterne** | Rottura connettori, vincoli legali | Adapter isolati, rispetto licenze, provenienza tracciata |
| **Accuratezza tempi di percorrenza** | Itinerari non fattibili | Matrice tempi affidabile per mezzo; buffer; ri-pianificazione |
| **Accessibilità trattata come soft** | Esclusione utenti | Accessibilità come vincolo hard, non come preferenza |

## 11. Manutenzione & evoluzione

### 11.1 Cicli di manutenzione del grafo

- **Ricalcolo dei pesi**: job batch periodici e listener di eventi aggiornano i fattori
  comportamentali (co-visita), di feedback (voti, conferme) e di freschezza. Coerente con
  il pattern eventi di dominio (\`DomainEventPublisherPort\` → listener infrastruttura).
- **Re-embedding**: i nodi modificati vengono ri-vettorializzati e aggiornati su Qdrant.
- **Pulizia e deduplica**: entity resolution periodica per fondere duplicati da fonti
  diverse e archiviare nodi obsoleti/chiusi.
- **Aggiornamento connettori**: monitoraggio delle fonti esterne e manutenzione degli
  adapter; nuovi connettori aggiunti come moduli.

### 11.2 Apprendimento e miglioramento continuo

- **Loop di feedback**: ogni interazione (salvataggio, modifica, voto, "ci sono stato")
  diventa segnale che ricalibra pesi e affidabilità — il grafo migliora con l'uso.
- **Tuning dei profili**: i coefficienti per persona/profilo sono configurabili e
  valutabili con i KPI di sezione 9; possibilità di A/B test sui pesi.
- **Curatela editoriale**: percorsi tematici curati affiancano quelli generati,
  alimentando esempi di qualità.

### 11.3 Roadmap evolutiva sintetica

1. **Fondamenta**: schema grafo territorio + generazione MVP single-day.
2. **Esperienza**: chat conversazionale, viste mappa/grafo, export, feedback.
3. **Scala**: multi-giorno, top-k, metaeuristiche, ri-pianificazione real-time.
4. **Community**: contributi, moderazione, reputazione, "Wikipedia dei luoghi".
5. **Pacchetto**: modulo "Territorio" distribuibile via marketplace (PF4J).

### 11.4 Vincoli di manutenibilità di progetto

File piccoli e coesi (200-400 righe), immutabilità (ogni rigenerazione produce un nuovo
oggetto), una sola query per migrazione Flyway, dominio puro senza Spring (wiring in
\`DomainConfig\`), enum tradotte IT/EN verso il frontend, documentazione bilingue aggiornata
e sviluppi tracciati nella cartella \`Sviluppi/\`.

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo esistente | Ruolo nell'ambito "Itinerari & esperienze" |
|------------------|--------------------------------------------|
| **knowledge** | Base del motore a grafo: ospita nodi/archi territorio ed espone le query di percorso/vicinato/sottografo |
| **llm** | Sintesi narrativa GraphRAG e slot filling conversazionale; Ollama di default, catena di fallback per il resto |
| **document** | Pipeline di ingestione (Tika/OCR → chunking → embedding) per guide e schede che diventano nodi |
| **vectorstore (Qdrant)** | Retrieval semantico dei candidati e relazioni SIMILE_A |
| **mcp** | Esposizione di tool (ricerca POI, routing, meteo) e consumo di server esterni come connettori |
| **calendar** | Export .ics e gestione degli eventi/finestre temporali nelle tappe |
| **email/messaging** | Condivisione e notifica itinerari; canali di invio |
| **agent** | Orchestrazione di flussi multi-step (genera → verifica → ri-pianifica) |
| **automation** | Job di ricalcolo pesi, re-embedding, aggiornamento connettori |
| **marketplace + plugin (PF4J)** | Distribuzione del modulo "Territorio" e di connettori come estensioni installabili |
| **auth** | Profilo utente locale per personalizzazione e contributi, privacy-first |
| **common (eventi/analytics)** | Eventi di dominio per il loop di feedback e metriche/KPI |
| **finetuning** | (Evoluzione) adattamento di modelli locali allo stile descrittivo del territorio |

### Punti di estensione tecnici

- **Domain**: nuovo package \`itinerary\` (o estensione di \`knowledge\`) con \`model/\`,
  \`port/in/\` (es. \`GenerateItineraryUseCase\`), \`port/out/\` (es. \`GraphQueryPort\`,
  \`RoutingPort\`), \`service/\` (ottimizzatore + orchestrazione GraphRAG), senza Spring.
- **Infrastructure**: adapter per query grafo (MySQL), routing/tempi, connettori dati;
  wiring in \`DomainConfig\`.
- **API**: \`ItineraryController\` sotto \`/api/v1/itineraries\` con DTO validati.
- **Frontend**: feature \`itineraries\` (e arricchimento di \`knowledge\`) con form, timeline,
  mappa e mini-grafo, bilingue IT/EN.
- **Persistence**: tabelle \`graph_node\`/\`graph_edge\` (e tabelle itinerario) via migrazioni
  Flyway con una sola query ciascuna; UUID \`CHAR(36)\`.

---

*Documento di indirizzo per gli sviluppi dell'ambito "Itinerari & esperienze" (verticale
consumer del motore Knowledge Graph di LocalMind). Da aggiornare ad ogni avanzamento di fase.*
`;
