# Cultura, arte & musei

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo documento descrive l'estensione di LocalMind verso il dominio **Cultura, arte & musei** (gruppo consumer), declinando il motore di Knowledge Graph universale in un **grafo culturale** che collega musei, opere, artisti, movimenti, periodi storici, materiali, tecniche, luoghi e mostre, e che alimenta **percorsi tematici** navigabili dall'AI (GraphRAG). L'obiettivo è offrire a visitatori, appassionati, studenti e operatori culturali un modo nuovo di esplorare il patrimonio: non per cataloghi piatti ma per relazioni pesate, facendo emergere collegamenti non evidenti (influenze tra artisti, dialoghi tra opere, fili tematici che attraversano secoli e collezioni diverse).

L'ambito riusa interamente lo stack e i vincoli di LocalMind: **local-first / self-hostable**, **AI locale Ollama di default** (provider cloud opzionali), **MySQL 8.0** per la struttura del grafo e **Qdrant** per la semantica (niente Neo4j), estensibilità via **plugin PF4J + marketplace**, interfaccia e documentazione **bilingui IT/EN**, migrazioni **Flyway con una sola query per file**.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema oggi

Il patrimonio culturale è ricchissimo ma **frammentato e poco navigabile per relazioni**. Chi vuole avvicinarsi all'arte si scontra con almeno cinque ostacoli concreti:

1. **Cataloghi a silos.** Ogni museo pubblica le proprie collezioni in formati e schede diverse; le opere vivono come record isolati, senza un tessuto di relazioni che li colleghi tra istituzioni, epoche e movimenti. Un visitatore non può chiedersi facilmente "quali altre opere dello stesso periodo, influenzate dallo stesso maestro, posso vedere entro 30 km da qui?".
2. **Ricerca per parole chiave, non per significato.** I motori di ricerca dei siti museali rispondono a stringhe esatte (titolo, autore, inventario), non a domande tematiche o esplorative del tipo "mostrami il passaggio dal Gotico al Rinascimento attraverso la rappresentazione della luce".
3. **Conoscenza inaccessibile ai non esperti.** Le schede storico-artistiche sono spesso tecniche e autoreferenziali; mancano percorsi guidati che adattino il livello di approfondimento al visitatore (bambino, turista occasionale, studente, studioso).
4. **Visita scollegata dal contesto.** L'esperienza in sala è disgiunta dalla preparazione prima della visita e dall'approfondimento dopo; non esiste un filo conduttore personalizzato che accompagni l'utente lungo le sale, né un modo per "salvare" un percorso e riprenderlo.
5. **Dato culturale chiuso o disperso.** Le iniziative di Linked Open Data (Europeana, Wikidata, vocabolari Getty AAT/ULAN/TGN, lo standard CIDOC-CRM) esistono ma sono difficili da consumare per piccole istituzioni, collezioni private, archivi locali e community territoriali, che restano fuori dal grafo globale.

### 1.2 Cosa risolve LocalMind

LocalMind trasforma il patrimonio in un **grafo culturale pesato, interrogabile in linguaggio naturale e navigabile visivamente**. Il valore si articola su più piani:

- **Dal record alla rete.** Ogni opera, artista, museo, movimento diventa un **nodo tipizzato**; influenze, appartenenze, committenze, citazioni, provenienze diventano **archi pesati**. Il valore non è la singola scheda ma la trama di relazioni che permette di "viaggiare" nel patrimonio.
- **Domande complesse, risposte motivate.** Grazie a GraphRAG, l'AI combina la **semantica** (embedding su descrizioni, testi critici, didascalie in Qdrant) con le **relazioni strutturali** del grafo (MySQL) per rispondere a domande aperte, citando i nodi e i percorsi usati ("questa influenza è attestata da X, l'opera Y appartiene al movimento Z").
- **Percorsi tematici personalizzati.** L'AI genera itinerari su misura — per livello, tempo disponibile, interessi, accessibilità, prossimità geografica — sia dentro un singolo museo sia tra musei e città diverse, sul modello dei "trail tematici" già sperimentati da istituzioni internazionali (Smithsonian American Art Museum, Rijksmuseum Art Explorer, Harvard Art Museums AI Explorer).
- **Apertura a tutti i custodi di cultura.** Un piccolo museo civico, un archivio parrocchiale, una pro-loco, un collezionista o una community territoriale possono **contribuire nodi e relazioni** restando padroni dei propri dati (local-first), e possono importare/allineare contenuti dai grandi grafi aperti (Wikidata, Europeana) senza dover gestire infrastruttura RDF complessa.
- **Sovranità e privacy.** L'intero motore gira on-premise con AI locale: nessun dato sensibile (es. provenienze, valutazioni assicurative, collocazioni di opere private) lascia l'istituzione senza consenso esplicito.

### 1.3 Perché ha valore (per chi)

| Beneficiario | Valore concreto |
|--------------|-----------------|
| **Visitatore / appassionato** | Esplorazione per significato, itinerari su misura, approfondimenti adattati al proprio livello, scoperta di collegamenti inaspettati tra opere e luoghi |
| **Studente / docente** | Strumento didattico che mostra movimenti, influenze e contesti storici come una mappa navigabile; supporto a tesi e ricerche con citazione delle fonti |
| **Studioso / curatore** | Capacità di far emergere relazioni non evidenti tra opere e fondi diversi; base per ricerca attributiva, studi di provenienza e curatela di mostre tematiche |
| **Piccolo museo / archivio** | Pubblicazione e arricchimento delle proprie collezioni con sforzo minimo, allineamento ai vocabolari standard, presenza in un grafo culturale condiviso senza cedere la sovranità del dato |
| **Operatore turistico / territorio** | Connessione tra patrimonio culturale e offerta territoriale (eventi, esperienze, itinerari), in sinergia con gli altri ambiti consumer di LocalMind |

### 1.4 Valore differenziante rispetto alle alternative

Rispetto ai cataloghi online dei musei, alle audioguide e ai portali di Linked Open Data, LocalMind offre la combinazione — oggi rara — di: **grafo pesato + AI conversazionale locale + visualizzazione interattiva + contributi community + self-hosting**. Non sostituisce Europeana o Wikidata: li **consuma e li arricchisce**, fungendo da "motore di scoperta" locale e federabile che chiunque può installare e popolare con il proprio patrimonio.

---

## 2. Personas & utenti target

| Persona | Profilo | Obiettivi | Bisogni dal sistema |
|---------|---------|-----------|---------------------|
| **Giulia, turista culturale** | 34 anni, in viaggio per un weekend, conoscenza media dell'arte | Visitare i luoghi migliori nel tempo disponibile, capire cosa sta guardando | Itinerario tematico breve, spiegazioni semplici, prossimità geografica, lingua a scelta |
| **Marco, studente di storia dell'arte** | 22 anni, prepara un esame su un movimento | Capire influenze, contesto storico, relazioni tra opere e artisti | Navigazione del grafo per movimento/periodo, citazioni delle fonti, esportazione percorso |
| **Prof.ssa Bianchi, docente** | 48 anni, insegna alle superiori | Costruire lezioni e percorsi didattici tematici | Generazione di percorsi adattati a un livello scolastico, contenuti bilingui, condivisione |
| **Dott. Ferri, curatore museale** | 55 anni, cura collezioni e mostre | Far emergere relazioni tra opere di fondi diversi, preparare mostre tematiche | Query avanzate sul grafo, suggerimento collegamenti mancanti, editing curato dei nodi |
| **Anna, responsabile piccolo museo civico** | 40 anni, poche risorse IT | Pubblicare e valorizzare la collezione locale | Ingestione semplice (CSV/foto/schede), allineamento a Wikidata/Getty, moderazione contributi |
| **Luca, appassionato/contributor community** | 29 anni, conosce bene il patrimonio locale | Arricchire il grafo con opere e storie del territorio | Creazione nodi/relazioni guidata, riconoscimento del contributo, revisione |
| **Sofia, visitatrice con esigenze di accessibilità** | 60 anni, ipovedente | Vivere la visita in autonomia | Percorsi accessibili, descrizioni audio, livello di dettaglio regolabile |
| **Operatore del territorio (cross-ambito)** | — | Collegare cultura, eventi ed esperienze | Interoperabilità con gli ambiti turismo/eventi del grafo |

Le persone si distribuiscono su due assi: **livello di competenza** (occasionale → esperto) e **ruolo** (fruitore → contributore → curatore). Il sistema deve adattare profondità dei contenuti e strumenti disponibili lungo entrambi gli assi.

---

## 3. Requisiti in input

Questa sezione definisce in dettaglio **tutto ciò che il sistema riceve in ingresso** per costruire, alimentare e interrogare il grafo culturale. Gli input si dividono in: (A) dati di dominio da ingestire, (B) input dell'utente in fase d'uso, (C) configurazione e schema, (D) input di contributo/curatela. Per ogni input indichiamo formato, validazione e destinazione nel grafo.

### 3.1 Dati di dominio da ingestire (popolamento del grafo)

| Input | Formato accettato | Campi/segnali chiave | Validazione | Destinazione |
|-------|-------------------|----------------------|-------------|--------------|
| **Schede opera** | CSV, JSON, XML, foglio Excel, schede catalografiche (es. ICCD scheda OA), record CIDOC-CRM/LIDO | titolo, autore, datazione, tecnica, materiale, dimensioni, soggetto, collocazione, inventario | Campi obbligatori minimi (titolo o inventario + museo); normalizzazione date; deduplica per inventario | Nodo `Opera` + archi verso `Artista`, `Museo`, `Tecnica`, `Materiale`, `Movimento`, `Periodo` |
| **Anagrafiche artisti** | CSV/JSON, allineamento ULAN/Wikidata (QID), VIAF | nome, varianti del nome, date e luoghi nascita/morte, nazionalità, ruoli | Disambiguazione omonimi; riconciliazione con ULAN/Wikidata | Nodo `Artista` + archi `nato_a`, `attivo_in`, `appartiene_a` (movimento) |
| **Anagrafiche musei/luoghi** | CSV/JSON, geocoordinate, TGN (Getty) | nome, indirizzo, coordinate, orari, sito, tipologia | Geocodifica obbligatoria; coordinate valide | Nodo `Museo`/`Luogo` + arco `situato_in` |
| **Testi critici, descrizioni, schede storiche** | PDF, DOCX, HTML, TXT, Markdown | testo libero, didascalie, bibliografia | Estrazione testo (Tika), OCR (Tesseract) per scansioni | Embedding in Qdrant collegati al nodo; estrazione entità/relazioni via LLM |
| **Immagini delle opere** | JPG, PNG, TIFF, IIIF manifest | immagine, metadati EXIF/IPTC, IIIF metadata | Formato/dimensione; collegamento a nodo opera | Allegato al nodo `Opera`; (evoluzione) embedding visuale multimodale |
| **Movimenti, periodi, stili, soggetti iconografici** | Vocabolari controllati: Getty AAT, Iconclass, Wikidata | termine, definizione, gerarchia, intervallo temporale | Mappatura su tassonomia interna; lingua IT/EN | Nodi `Movimento`, `Periodo`, `Tecnica`, `Soggetto` |
| **Mostre ed eventi espositivi** | CSV/JSON, feed eventi | titolo, sede, date, opere esposte, tema | Date coerenti; collegamento opere/musei | Nodo `Mostra` + archi `espone`, `si_tiene_in`, `tratta_di` |
| **Dataset Linked Open Data esterni** | SPARQL endpoint, RDF/Turtle, JSON-LD, dump (Europeana, Wikidata, DBpedia, Getty) | triple soggetto-predicato-oggetto | Mapping ontologico (CIDOC-CRM → schema interno); filtraggio per ambito | Nodi e archi tipizzati con provenienza |

### 3.2 Input dell'utente in fase d'uso (interrogazione & navigazione)

| Input | Esempio | Vincoli/validazione |
|-------|---------|---------------------|
| **Domanda in linguaggio naturale** | "Quali opere di Caravaggio mostrano l'uso drammatico della luce e dove posso vederle in Italia?" | Lunghezza max; sanificazione; lingua rilevata (IT/EN) |
| **Filtri di esplorazione** | tipo nodo (opera/artista/museo), movimento, periodo (slider temporale), area geografica, tecnica | Valori da enum controllate; range temporali validi |
| **Parametri di percorso tematico** | tema, tempo disponibile, livello (base/intermedio/avanzato), accessibilità, raggio km, punto di partenza | Coerenza tra tempo e numero tappe; geolocalizzazione opzionale |
| **Nodo di partenza per navigazione** | clic su un nodo del grafo per espandere i vicini | ID nodo valido ed esistente |
| **Preferenze di profilo** | interessi (movimenti/artisti preferiti), lingua, livello predefinito, salvataggi | Persistite per utente; modificabili |
| **Feedback** | voto/like su opera o percorso, "utile/non utile" su una risposta AI, segnalazione errore | Anti-abuso; un voto per utente/oggetto |

### 3.3 Configurazione e schema (input di sistema/amministratore)

- **Definizione dello schema di dominio** (tipi di nodo, tipi di relazione, attributi) come modulo installabile dal marketplace: file di configurazione versionato che estende lo schema base del motore a grafo.
- **Mappature ontologiche**: tabelle di corrispondenza CIDOC-CRM / LIDO / AAT / Iconclass → schema interno, per l'ingestione da fonti standard.
- **Pesi e formule**: configurazione dei fattori e dei coefficienti di calcolo del peso degli archi (vedi §5.3).
- **Provider LLM ed embedding**: scelta del modello locale Ollama (default) per chat ed embedding; eventuale fallback cloud, disattivabile per garantire privacy.
- **Policy di moderazione**: soglie e regole per contributi community (auto-pubblicazione vs revisione).
- **Lingue attive**: IT/EN obbligatorie; enum e label tradotte; testi liberi indicizzati per lingua.

### 3.4 Input di contributo e curatela (community/curatori)

- **Creazione/modifica nodi**: form guidato con suggerimento automatico di tipo, attributi e possibili collegamenti (assistito da AI).
- **Proposta di relazioni**: l'utente o l'AI propongono archi ("questa opera è influenzata da…"), con possibilità di indicare fonte/evidenza.
- **Allegati**: immagini, documenti, link a fonti autorevoli.
- **Revisioni e versioni**: ogni modifica è immutabile e versionata (coerente con la regola di immutabilità del progetto); storico consultabile.

### 3.5 Requisiti non funzionali sugli input

- **Validazione ai confini** (regola di progetto): ogni input — file, API esterna, contributo utente — è validato prima dell'elaborazione, con messaggi d'errore chiari e bilingui.
- **Provenienza tracciata**: ogni nodo/arco conserva la fonte (file importato, dataset LOD, utente, AI) e il timestamp.
- **Idempotenza e deduplica**: re-importare la stessa fonte non duplica nodi; la riconciliazione usa identificatori esterni (QID Wikidata, ULAN, inventario museo).
- **Local-first**: nessun input richiede servizi cloud obbligatori; le fonti LOD esterne sono opzionali e cacheabili in locale.

---

## 4. Flusso dell'attività (step-by-step)

Il dominio prevede tre flussi principali, descritti in dettaglio: **(A) Ingestione e costruzione del grafo**, **(B) Esplorazione e domanda dell'utente (GraphRAG)**, **(C) Contributo e curatela**. Ogni passo indica l'attore, il modulo LocalMind coinvolto e l'esito.

### 4.1 Flusso A — Ingestione e costruzione del grafo culturale

1. **Selezione fonte.** L'amministratore o curatore sceglie la fonte: upload file (CSV/JSON/PDF/immagini), cartella monitorata, connettore LOD (Wikidata/Europeana/Getty), o feed mostre. *(UI: feature `documents`/nuova feature `culture`; backend: dominio `document` + nuovo dominio `knowledge`/grafo.)*
2. **Estrazione e parsing.** Per documenti non strutturati, la pipeline esistente estrae il testo (Apache Tika) e applica OCR (Tesseract) alle scansioni; per fonti strutturate si applica il parser/mapping dedicato. *(Riuso `DocumentIngestionPipelineService`, `TikaTextExtractor`, `TesseractOcrExtractor`.)*
3. **Normalizzazione e validazione.** I campi vengono normalizzati (date, nomi, unità di misura), validati ai confini e tradotti/etichettati per lingua (IT/EN). Gli input non validi vengono respinti con messaggio chiaro.
4. **Riconciliazione entità.** Il sistema cerca corrispondenze con nodi esistenti e con identificatori esterni (QID Wikidata, ULAN per artisti, TGN per luoghi, inventario per opere) per evitare duplicati. I dubbi di disambiguazione vengono segnalati per revisione.
5. **Estrazione di nodi e relazioni.** Per i testi liberi, un LLM locale (Ollama) estrae triple (entità → relazione → entità) secondo lo schema di dominio, ispirandosi all'approccio CIDOC-CRM + LLM. Per le fonti strutturate, il mapping genera direttamente i nodi/archi.
6. **Calcolo dei pesi.** A ogni arco viene assegnato un peso iniziale secondo le formule configurate (forza dell'evidenza, numero di fonti, autorevolezza, frequenza — vedi §5.3).
7. **Persistenza duale.** La **struttura** (nodi, archi, attributi, pesi, provenienza) è salvata in **MySQL** via JPA; le **descrizioni e i testi** sono incorporati (embedding Ollama) e salvati in **Qdrant**, con riferimento incrociato all'ID del nodo. *(Riuso `QdrantVectorStoreAdapter`, `EmbeddingConfig` Ollama @Primary.)*
8. **Indicizzazione e collegamento.** Vengono creati gli indici per query su grafo (vicini, percorsi, sottografi) e i collegamenti semantici tra nodi simili (similarità di embedding sopra soglia → arco candidato `simile_a`).
9. **Suggerimento collegamenti mancanti.** L'AI propone archi non ancora presenti (es. influenze probabili, opere dello stesso ciclo) come **candidati** in coda di revisione, mai pubblicati automaticamente senza policy.
10. **Tracciamento sviluppo.** Coerentemente con le regole di progetto, l'attività di sviluppo viene registrata nella cartella `Sviluppi/` e la documentazione IT/EN aggiornata.

### 4.2 Flusso B — Esplorazione e domanda dell'utente (GraphRAG)

1. **Punto d'ingresso.** L'utente entra dalla feature **Cultura** o dalla **Chat**: può porre una domanda in linguaggio naturale, applicare filtri o partire da un nodo (opera/artista/museo).
2. **Comprensione dell'intento.** Il sistema rileva lingua (IT/EN), classifica l'intento (ricerca puntuale, esplorazione tematica, generazione itinerario, confronto) ed estrae le entità menzionate. *(Backend: dominio `llm`/`knowledge`.)*
3. **Recupero ibrido (GraphRAG).**
   a. **Semantico**: ricerca dei nodi più rilevanti per significato in Qdrant (descrizioni, testi critici).
   b. **Strutturale**: a partire dai nodi trovati, espansione nel grafo MySQL lungo gli archi più pesati (vicini, percorsi, sottografo tematico).
   c. **Fusione**: combinazione dei due insiemi in un contesto coerente, ordinato per peso e rilevanza.
4. **Ragionamento dell'AI.** L'LLM locale riceve il sottografo + i passaggi testuali e genera una risposta che **cita i nodi e i percorsi** usati, con livello di dettaglio adattato al profilo/livello dell'utente.
5. **Presentazione.** La risposta è accompagnata da: scheda dei nodi citati, mini-visualizzazione del sottografo, e azioni rapide ("espandi", "crea percorso", "salva", "approfondisci").
6. **Navigazione interattiva.** L'utente clicca su un nodo per espandere progressivamente i vicini; può filtrare per tipo, movimento, periodo (slider temporale) e area geografica. La visualizzazione mostra il **peso degli archi** (spessore/colore).
7. **Generazione percorso tematico.** Se l'utente chiede un itinerario, l'AI compone una sequenza ordinata di tappe (opere/sale/musei) ottimizzata per tema, tempo, livello, accessibilità e prossimità; il percorso è salvabile, condivisibile ed esportabile.
8. **Feedback e apprendimento.** L'utente valuta risposta e percorso; i feedback alimentano il ricalcolo dei pesi (frequenza d'uso, utilità) e migliorano i suggerimenti futuri.
9. **Continuità della visita.** Prima della visita: preparazione e itinerario; in sala: percorso passo-passo (anche audio/accessibile); dopo: approfondimenti e salvataggio nel profilo.

### 4.3 Flusso C — Contributo e curatela (community/curatori)

1. **Avvio contributo.** Un contributore o curatore crea/modifica un nodo (es. una nuova opera del museo locale) tramite form guidato.
2. **Assistenza AI.** Il sistema suggerisce tipo di nodo, attributi mancanti, possibili collegamenti e allineamenti a vocabolari standard (AAT, Wikidata).
3. **Proposta relazioni con evidenza.** L'utente o l'AI propongono archi indicando fonte/evidenza; il peso iniziale riflette la forza dell'evidenza.
4. **Moderazione.** Secondo la policy: auto-pubblicazione per curatori fidati, oppure coda di revisione per contributi community. Un curatore approva, modifica o rifiuta.
5. **Versionamento immutabile.** Ogni modifica genera una nuova versione (no mutazione in-place); lo storico resta consultabile e reversibile.
6. **Ranking emergente.** I contributi più validati e usati emergono nel grafo (peso e visibilità maggiori), in linea con la logica di ranking emergente del verticale consumer.

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello specializza lo schema base del motore a grafo per il dominio culturale, ispirandosi a CIDOC-CRM e ai vocabolari Getty (AAT/ULAN/TGN) e Iconclass, ma mantenendolo pragmatico e mappabile su MySQL + Qdrant.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi principali | Mappatura standard |
|--------------|-------------|----------------------|--------------------|
| `Opera` | Opera d'arte / oggetto culturale | titolo, datazione, tecnica, materiale, dimensioni, soggetto, inventario, immagine | CIDOC-CRM E22; LIDO |
| `Artista` | Autore/creatore | nome, varianti, nascita/morte, nazionalità, ruoli | CIDOC-CRM E21; ULAN |
| `Museo` | Istituzione che conserva opere | nome, tipologia, coordinate, orari, sito | CIDOC-CRM E40/E53 |
| `Luogo` | Luogo geografico (città, sito) | nome, coordinate, gerarchia geografica | TGN; CIDOC-CRM E53 |
| `Movimento` | Movimento/corrente artistica | nome, intervallo temporale, descrizione | AAT |
| `Periodo` | Periodo storico (es. Rinascimento) | nome, intervallo temporale | AAT; CIDOC-CRM E4 |
| `Tecnica` | Tecnica esecutiva (es. olio su tela) | nome, descrizione | AAT |
| `Materiale` | Materiale costitutivo | nome | AAT |
| `Soggetto` | Soggetto iconografico/tema | nome, codice | Iconclass |
| `Mostra` | Evento espositivo | titolo, sede, date, tema | CIDOC-CRM E5/E7 |
| `Collezione` | Raccolta/fondo | nome, museo, criterio | — |
| `Committente` | Persona/ente che commissiona | nome, periodo | CIDOC-CRM E21/E74 |
| `FonteCritica` | Testo critico/bibliografia | titolo, autore, anno | — |
| `PercorsoTematico` | Itinerario curato/generato | tema, tappe, livello | — |

### 5.2 Tipi di relazione (archi orientati e pesati)

| Relazione | Da → A | Significato | Note di peso |
|-----------|--------|-------------|--------------|
| `creata_da` | Opera → Artista | Attribuzione autoriale | Peso alto se attestata; ridotto se "attribuita/cerchia di" |
| `conservata_in` | Opera → Museo | Collocazione attuale | Fattuale, peso alto |
| `appartiene_a` | Opera/Artista → Movimento | Adesione a corrente | Peso da consenso delle fonti |
| `datata_in` | Opera → Periodo | Collocazione temporale | — |
| `realizzata_con` | Opera → Tecnica/Materiale | Tecnica/materiale | Fattuale |
| `raffigura` | Opera → Soggetto | Soggetto iconografico | — |
| `influenzato_da` | Artista → Artista | Influenza stilistica | Peso da forza e numero evidenze |
| `maestro_di` | Artista → Artista | Rapporto bottega/allievo | — |
| `dialoga_con` | Opera → Opera | Affinità tematica/visiva | Peso da similarità + curatela |
| `esposta_in` | Opera → Mostra | Presenza in mostra | — |
| `commissionata_da` | Opera → Committente | Committenza | — |
| `situato_in` | Museo/Luogo → Luogo | Gerarchia geografica | Fattuale |
| `parte_di` | Opera → Collezione | Appartenenza a fondo | — |
| `simile_a` | Opera → Opera | Similarità semantica/visuale | Peso = punteggio similarità |
| `tappa_di` | Opera/Museo → PercorsoTematico | Inclusione in itinerario | Peso da rilevanza tematica |
| `documentata_in` | Opera/Artista → FonteCritica | Riferimento bibliografico | Peso = autorevolezza fonte |

### 5.3 Criteri di peso degli archi

Il peso (valore continuo normalizzato, es. 0–1) esprime **quanto una relazione è forte/affidabile/rilevante** e guida sia la navigazione (espansione dai più pesati) sia il ragionamento AI. Si calcola come combinazione configurabile dei seguenti fattori:

| Fattore | Descrizione | Esempio |
|---------|-------------|---------|
| **Forza dell'evidenza** | Quanto la relazione è certa/attestata | "creata_da" certa > "attribuita a" |
| **Numero e accordo delle fonti** | Più fonti indipendenti concordi → peso maggiore | 3 cataloghi confermano l'influenza |
| **Autorevolezza della fonte** | Fonte istituzionale/peer-reviewed > generica | Getty/Europeana > blog |
| **Similarità semantica/visuale** | Per `simile_a`/`dialoga_con`, punteggio embedding | distanza coseno in Qdrant |
| **Frequenza d'uso** | Quanto la relazione è percorsa dagli utenti/AI | archi più navigati salgono |
| **Feedback utenti** | Voti "utile", validazioni curatori | curatela aumenta il peso |
| **Recenza/decadimento** | Il peso può decadere se non confermato | relazioni vecchie non validate |
| **Rilevanza tematica** | Per percorsi, aderenza al tema richiesto | — |

Il peso è **ricalcolato in modo incrementale** (immutabilità: ogni ricalcolo produce un nuovo valore versionato, non muta lo storico) e la formula/coefficienti sono parametri di configurazione, così da adattarsi a contesti diversi (museo singolo, grafo territoriale, ricerca scientifica).

---

## 6. Fonti dati & connettori (ingestione)

| Fonte | Tipo | Connettore | Modalità | Note |
|-------|------|-----------|----------|------|
| **Wikidata** | LOD globale | SPARQL/REST + riconciliazione QID | Pull batch + on-demand | Allineamento artisti/opere/musei; arricchimento |
| **Europeana** | Aggregatore CH europeo | API REST | Pull batch | Metadati e media del patrimonio europeo |
| **Getty Vocabularies (AAT/ULAN/TGN)** | Vocabolari controllati | LOD/SPARQL | Pull + cache locale | Normalizzazione termini, artisti, luoghi |
| **Iconclass** | Vocabolario iconografico | API/dump | Pull | Classificazione soggetti |
| **CIDOC-CRM / LIDO / record museali** | Standard catalografici | Parser/mapping dedicato | Import file | Mapping ontologico → schema interno |
| **Schede ICCD / cataloghi nazionali** | Catalogazione (IT) | Parser CSV/XML | Import file | Patrimonio italiano |
| **IIIF** | Immagini ad alta risoluzione | IIIF manifest | Link/embedding | Visualizzazione e (evoluzione) embedding visuale |
| **File locali** (CSV/Excel/JSON/PDF/DOCX/immagini) | Fonti proprie | Upload + folder watcher (batch) | Pull/push | Riuso pipeline documenti esistente |
| **Feed mostre/eventi** | Eventi espositivi | API/CSV | Pull periodico | Sinergia con ambito eventi |
| **Contributi community** | UGC | Form + API | Push | Con moderazione e versioning |

**Riuso infrastrutturale:** l'ingestione poggia sulla pipeline documenti esistente (`DocumentIngestionPipelineService`, Tika, Tesseract, folder watcher batch), su Qdrant per gli embedding e su MySQL/Flyway per la struttura. I connettori LOD esterni sono **opzionali**, cacheabili in locale e disattivabili per scenari completamente offline/privati. Ogni connettore implementa il pattern Repository/port-out del dominio ed è impacchettabile come **plugin PF4J** distribuito via marketplace.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

### 7.1 MVP (prima release del modulo Cultura)

| # | Funzionalità | Tipo | Moduli LocalMind | Esito atteso |
|---|--------------|------|------------------|--------------|
| 1 | **Schema di dominio culturale** (nodi/relazioni §5) come modulo installabile | CREARE | knowledge/grafo, marketplace, Flyway | Tipi di nodo/arco disponibili nel motore |
| 2 | **Ingestione strutturata** (CSV/JSON/Excel) di opere/artisti/musei con validazione e riconciliazione base | CREARE | document, knowledge, batch | Grafo popolato da fonti proprie |
| 3 | **Ingestione testi non strutturati** + estrazione triple via LLM locale | CREARE | document, llm, knowledge | Nodi/archi da schede e testi critici |
| 4 | **Persistenza duale** struttura (MySQL) + semantica (Qdrant) con provenienza | SVILUPPARE (estende motore grafo) | infrastructure, vectorstore | Nodi interrogabili per relazione e significato |
| 5 | **Calcolo pesi archi** con formula configurabile (§5.3) | CREARE | knowledge | Archi pesati |
| 6 | **API grafo** (CRUD nodi/archi, vicini, percorsi, sottografi) | CREARE | api, knowledge | Endpoint REST `/api/v1/...` |
| 7 | **GraphRAG culturale**: recupero ibrido + risposta con citazione nodi/percorsi | CREARE | llm, knowledge, vectorstore | Risposte motivate in chat |
| 8 | **Feature frontend "Cultura"**: ricerca, filtri (tipo/movimento/periodo/area), schede nodo | CREARE | frontend (standalone, Signals) | Esplorazione di base |
| 9 | **Visualizzazione grafo interattiva** con peso archi ed espansione progressiva | CREARE | frontend | Navigazione visiva |
| 10 | **Generatore di percorsi tematici** (tema/tempo/livello/area) | CREARE | llm, knowledge, frontend | Itinerari salvabili |
| 11 | **Bilingue IT/EN** (UI, enum tradotte, contenuti per lingua) | SVILUPPARE | frontend, api, domain | Conformità vincolo i18n |
| 12 | **Connettore Wikidata** (riconciliazione/arricchimento) | CREARE | plugin PF4J, knowledge | Allineamento al grafo globale |
| 13 | **Contributi e moderazione base** con versioning immutabile | CREARE | knowledge, api, frontend | UGC controllato |

### 7.2 Evoluzioni future

| # | Funzionalità | Tipo | Valore |
|---|--------------|------|--------|
| E1 | **Embedding visuale multimodale** delle opere (similarità per immagine) | CREARE | `simile_a`/`dialoga_con` su base visiva; riuso adapter multimodali Ollama esistenti |
| E2 | **Connettori Europeana + Getty + Iconclass + IIIF** | CREARE | Arricchimento e standardizzazione su larga scala |
| E3 | **Suggerimento automatico collegamenti mancanti** (link prediction) con coda di revisione | CREARE | Far emergere relazioni non evidenti |
| E4 | **Percorsi geolocalizzati multi-museo / territoriali** integrati con ambito turismo/eventi | SVILUPPARE | Continuità cultura–territorio |
| E5 | **Modalità in-sala / audioguida accessibile** (descrizioni audio, livello regolabile) | CREARE | Esperienza durante la visita |
| E6 | **Strumenti curatoriali avanzati** (query complesse, analisi provenienza, preparazione mostre) | CREARE | Valore per curatori/studiosi |
| E7 | **Federazione tra istanze LocalMind** (grafi locali interrogabili in rete, privacy-preserving) | CREARE | "Wikipedia dei luoghi" distribuita |
| E8 | **Allineamento CIDOC-CRM completo + export RDF/JSON-LD** | SVILUPPARE | Interoperabilità con ecosistema LOD |
| E9 | **Ranking emergente e reputazione contributori** | SVILUPPARE | Qualità guidata dalla community |
| E10 | **Pacchetti di dominio specializzati** (arte contemporanea, archeologia, musica, fotografia) via marketplace | CREARE | Estensione del modello |

### 7.3 Da mantenere (manutenzione continua)

- **Mappature ontologiche** (CIDOC-CRM/AAT/ULAN/TGN/Iconclass): aggiornamento al variare dei vocabolari.
- **Connettori LOD**: adeguamento a cambi di API/endpoint esterni.
- **Formule di peso**: tuning periodico su feedback ed evidenze.
- **Qualità del grafo**: deduplica, riconciliazione, correzione attribuzioni errate, decadimento relazioni non confermate.
- **Migrazioni Flyway** (una query per file) per ogni evoluzione di schema.
- **Documentazione IT/EN** e log in `Sviluppi/` aggiornati a ogni sviluppo.
- **Modelli LLM/embedding locali**: aggiornamento e valutazione qualità su Ollama.

---

## 8. Casi d'uso AI / GraphRAG

1. **Domanda esplorativa tematica.** "Come si rappresenta la luce dal Caravaggio agli impressionisti?" → l'AI recupera nodi semanticamente rilevanti, percorre archi `influenzato_da`/`appartiene_a`/`raffigura`, e costruisce una narrazione che cita opere, artisti e movimenti coinvolti, mostrando il sottografo.
2. **Itinerario personalizzato.** "Ho 2 ore al Louvre, mi interessa il Rinascimento italiano, livello base" → percorso ordinato di tappe ottimizzato per tema/tempo/livello, con spiegazioni adattate.
3. **Scoperta di collegamenti non evidenti.** "Cosa accomuna questa opera ad altre della collezione?" → l'AI evidenzia archi `dialoga_con`/`simile_a` e propone connessioni tematiche/visive non ovvie.
4. **Confronto.** "Confronta lo stile di Tiziano e Tintoretto" → l'AI estrae sottografi dei due artisti e ne confronta movimenti, tecniche, soggetti, influenze, citando le evidenze.
5. **Ricerca con citazione delle fonti (studio).** "Quali fonti attestano l'attribuzione di quest'opera?" → risposta con archi `documentata_in` e `creata_da` pesati per autorevolezza.
6. **Assistenza alla curatela.** "Proponi un percorso per una mostra sul rapporto maestro-allievo nel Cinquecento veneziano" → l'AI seleziona artisti via `maestro_di`, opere correlate e suggerisce una sequenza espositiva.
7. **Suggerimento collegamenti mancanti (link prediction).** L'AI propone archi probabili (influenze, cicli, dialoghi) come candidati in revisione, motivando ciascuna proposta.
8. **Q&A in-sala accessibile.** Durante la visita, domande vocali con risposte brevi e descrizioni audio, livello di dettaglio regolabile.

Tutti i casi d'uso rispettano il principio **local-first**: il ragionamento gira su Ollama di default; i provider cloud sono opzionali e disattivabili. Le risposte **citano sempre i nodi e i percorsi** usati, per trasparenza e verificabilità.

---

## 9. KPI & metriche di successo

| Categoria | KPI | Target indicativo |
|-----------|-----|-------------------|
| **Copertura grafo** | N. nodi/archi; % opere con ≥3 relazioni; % nodi riconciliati a Wikidata/AAT | Crescita costante; >70% nodi con ≥3 archi |
| **Qualità relazioni** | % archi con fonte/provenienza; tasso di archi corretti dopo revisione | >90% archi con provenienza |
| **Efficacia AI** | % risposte valutate "utili"; % risposte con citazioni valide; tasso di allucinazioni | >80% utili; allucinazioni <5% |
| **Engagement** | N. domande/sessione; profondità navigazione (hop medi); percorsi generati e salvati | Crescente |
| **Percorsi tematici** | % percorsi completati; soddisfazione percorso | >60% completati |
| **Contributi community** | N. contributi; % approvati; tempo medio di revisione | Crescente; revisione <48h |
| **Performance** | Latenza query grafo e GraphRAG; tempo di ingestione per 1k record | Query <2s; ingestione efficiente |
| **Privacy/local-first** | % deployment 100% offline funzionanti; zero data leak verso cloud non consentito | 100% conformità |
| **i18n** | Copertura traduzioni IT/EN UI ed enum | 100% |

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---------|---------|-------------|
| **Allucinazioni dell'AI** (relazioni/attribuzioni inventate) | Alto (credibilità) | GraphRAG vincolato al grafo; risposte con citazione obbligatoria; archi proposti vanno in revisione, non auto-pubblicati |
| **Qualità/eterogeneità delle fonti** | Medio | Validazione ai confini, riconciliazione a vocabolari standard, peso per autorevolezza |
| **Duplicati e disambiguazione** (omonimi artisti, opere) | Medio | Riconciliazione su identificatori esterni (QID/ULAN/inventario), coda di disambiguazione |
| **Scalabilità query su grafo in MySQL** (no Neo4j) | Medio-alto | Indici dedicati, profondità di espansione limitata, sottografi precompilati/cache; rivalutare datastore solo se necessario |
| **Diritti/licenze su immagini e testi** | Alto (legale) | Tracciamento licenze, uso IIIF/metadati, rispetto policy delle fonti, opt-in per contenuti riservati |
| **Abuso nei contributi community** | Medio | Moderazione, reputazione, versioning immutabile, soglie anti-abuso |
| **Costo/latenza embedding e LLM locali** | Medio | Batch di ingestione, cache, modelli Ollama dimensionati; cloud opzionale solo se consentito |
| **Privacy dati riservati** (provenienze, valutazioni) | Alto | Local-first by default, nessun invio cloud senza consenso, segregazione contenuti riservati |
| **Drift dei vocabolari/API esterne** | Basso-medio | Cache locale, connettori versionati, manutenzione pianificata |
| **Bias culturale nei dati** | Medio | Trasparenza fonti, pluralità di prospettive, curatela, possibilità di correzione community |

---

## 11. Manutenzione & evoluzione

- **Igiene del grafo**: job periodici di deduplica, riconciliazione, decadimento dei pesi non confermati e segnalazione di incoerenze; riuso del layer batch.
- **Aggiornamento fonti esterne**: sincronizzazione periodica con Wikidata/Europeana/Getty con cache locale e diff incrementale.
- **Tuning dei pesi**: revisione periodica di formule e coefficienti sulla base di feedback e metriche.
- **Evoluzione schema**: ogni nuovo tipo di nodo/relazione passa per migrazione Flyway (una query per file) e aggiornamento del modulo di dominio nel marketplace.
- **Qualità AI**: valutazione periodica delle risposte (utilità, citazioni, allucinazioni); aggiornamento prompt e modelli Ollama.
- **Documentazione bilingue**: ogni sviluppo aggiorna `documentation/` (EN) e `documentazione/` (IT) e registra un file in `Sviluppi/` con nomenclatura datata e checkpoint per i task complessi.
- **Estensibilità**: nuovi domini culturali (archeologia, musica, fotografia, arte contemporanea) introdotti come pacchetti installabili senza toccare il core.
- **Federazione (evoluzione)**: protocollo per interrogare grafi di altre istanze LocalMind preservando la privacy, verso una rete distribuita di patrimoni.

---

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo/Dominio LocalMind | Ruolo nell'ambito Cultura |
|--------------------------|---------------------------|
| **knowledge** | Base del motore a grafo culturale: tipi di nodo/relazione, pesi, query (vicini/percorsi/sottografi) |
| **document** | Ingestione di schede, testi critici, PDF/immagini; estrazione testo (Tika) e OCR (Tesseract) |
| **batch** | Folder watcher e job di ingestione/igiene del grafo schedulati |
| **llm** | GraphRAG, estrazione triple, generazione percorsi e risposte; routing provider con Ollama default e fallback |
| **vectorstore (Qdrant)** | Embedding di descrizioni/testi e similarità semantica/visuale tra nodi |
| **persistence (MySQL/JPA/Flyway)** | Struttura del grafo, attributi, pesi, provenienza, versioning; UUID `CHAR(36)`; una query per migrazione |
| **mcp** | Esposizione del grafo come tool MCP per agenti esterni; integrazione di tool culturali |
| **agent** | Agenti che orchestrano esplorazione, generazione itinerari e arricchimento |
| **marketplace + plugin (PF4J)** | Distribuzione dello schema di dominio e dei connettori (Wikidata, Europeana, Getty, IIIF) come moduli installabili |
| **auth** | Profili utente, ruoli (fruitore/contributore/curatore), permessi di moderazione |
| **common (analytics/eventi)** | KPI, eventi di dominio (nodo creato, percorso generato), feedback |
| **calendar / email / messaging** | Notifiche su nuove mostre, percorsi salvati, promemoria visita; sinergia con eventi territoriali |
| **finetuning** | (Evoluzione) adattamento di modelli locali al lessico storico-artistico |
| **Frontend (Angular 21, Signals)** | Feature "Cultura": ricerca, filtri, schede nodo, visualizzazione grafo, generatore percorsi; bilingue IT/EN con enum tradotte |

**Sinergia con gli altri ambiti consumer.** Il grafo culturale condivide nodi `Luogo`/`Evento` con gli ambiti turismo ed eventi: un'opera o un museo possono essere tappe di un itinerario territoriale più ampio, realizzando la visione di un **unico motore, più ecosistemi**. La specializzazione avviene solo a livello di tipi di nodo/relazione e moduli installati, nel pieno rispetto dei vincoli di progetto (local-first, AI Ollama default, MySQL+Qdrant, plugin PF4J, privacy, open source, IT/EN).
