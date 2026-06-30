export const content = `# Visualizzazione grafo

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo ambito appartiene al **gruppo core** del motore di knowledge graph universale di LocalMind. Mentre i verticali consumer (turismo, eventi, ristorazione) ed enterprise (architettura software, knowledge base, mail) definiscono *quali* nodi e relazioni popolano il grafo, l'ambito **Visualizzazione grafo** definisce *come l'essere umano vede, naviga ed esplora* quel grafo pesato. È la superficie visiva e interattiva del motore: il luogo in cui il grafo astratto — nodi tipizzati, archi pesati, sottografi, percorsi — diventa un'esperienza navigabile, comprensibile e azionabile per l'utente, e in cui le risposte dell'Ai (GraphRAG) trovano un riscontro visivo verificabile.

La visualizzazione non è un accessorio estetico ma un **componente di fiducia e di comprensione**: quando l'AI risponde "il servizio A è impattato dal cambiamento dell'API B attraverso il database ordini", l'utente deve poter vedere quel percorso, espanderlo, filtrarlo e confermarlo. La visualizzazione è il ponte tra il ragionamento dell'AI e la verifica umana, ed è trasversale a tutti i domini: lo stesso visualizzatore rende un itinerario turistico, una catena di dipendenze software, un thread di mail correlate o un albero decisionale, cambiando solo lo schema cromatico, le icone e i filtri di tipo. Tutto resta **local-first**: il rendering avviene interamente nel browser dell'utente, i dati del grafo non lasciano l'istanza self-hosted, e nessun servizio di visualizzazione cloud di terzi è coinvolto.

La sfida tecnica centrale dell'ambito è la **performance su scala**: un grafo di conoscenza enterprise può contenere decine o centinaia di migliaia di nodi, e la visualizzazione ingenua (SVG, DOM) collassa già a poche migliaia di elementi. La ricerca di settore più recente (cfr. §6) converge su rendering **WebGL/GPU**, **level-of-detail (LoD)**, **espansione progressiva** e **layout fuori dal thread principale (Web Worker)** come pilastri non negoziabili per restare interattivi oltre la soglia delle decine di migliaia di nodi.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema concreto

Un grafo di conoscenza è potente proprio perché cattura relazioni — ma una struttura a relazioni è anche **intrinsecamente difficile da rappresentare e da esplorare**. Senza una visualizzazione progettata con cura, il valore del motore a grafo resta invisibile e inutilizzabile. I problemi concreti che questo ambito affronta sono:

- **L'opacità del grafo.** Un grafo memorizzato in MySQL + Qdrant è perfettamente interrogabile dall'AI, ma per l'essere umano resta una scatola nera: non si percepiscono i cluster, gli hub, i percorsi, i nodi isolati, le comunità. L'utente non ha modo di *vedere* la forma della propria conoscenza, di scoprire collegamenti non evidenti o di accorgersi di lacune. La visualizzazione rende tangibile ciò che altrimenti è solo struttura dati.
- **Il sovraccarico cognitivo ("hairball").** Il problema opposto e altrettanto grave: mostrare *tutto* il grafo contemporaneamente produce il famigerato "groviglio" (hairball) — un ammasso illeggibile di nodi e archi sovrapposti in cui nessuna informazione emerge. Senza tecniche di riduzione della complessità (filtri, LoD, espansione progressiva, clustering), più dati significano meno comprensione, non più.
- **Il collasso di performance su scala.** Le tecnologie di rendering tradizionali del web (SVG, manipolazione DOM) degradano drammaticamente già intorno ai 1.000–5.000 elementi: frame rate sotto i 10 fps, interazioni lagose, browser che si bloccano. Per un grafo enterprise da 100.000+ nodi questo approccio è semplicemente inutilizzabile. Serve un'architettura di rendering pensata per la scala fin dal primo giorno.
- **La navigazione disorientante.** Esplorare un grafo grande significa muoversi in uno spazio in cui è facile "perdersi": l'utente espande un nodo, poi un altro, e dopo cinque passi non sa più dove si trova né come tornare indietro. Senza minimappa, breadcrumb, cronologia di navigazione e punti di ancoraggio, l'esplorazione diventa frustrante.
- **La mancanza di pertinenza visiva.** Mostrare tutti gli archi con lo stesso spessore e tutti i nodi con la stessa dimensione spreca il segnale più prezioso del motore: il **peso** delle relazioni. Se la visualizzazione non traduce il peso (frequenza, rilevanza, criticità, confidenza) in attributi visivi (spessore arco, dimensione nodo, opacità, ordine di rendering), l'utente non distingue il collegamento cruciale da quello marginale.
- **Lo scollamento tra AI e verifica.** Quando l'AI in modalità GraphRAG risponde citando nodi e percorsi, l'utente deve poter passare immediatamente dalla risposta testuale alla rappresentazione visiva di quei nodi e percorsi, per verificarli e approfondirli. Senza questo ponte, le citazioni dell'AI restano stringhe non verificabili e la fiducia crolla.
- **La cecità ai tipi e ai domini.** In un motore universale, lo stesso grafo può mescolare nodi di domini diversi (un luogo turistico, una recensione, una persona, un evento). Senza filtri per tipo di nodo, tipo di relazione e dominio, l'utente non può isolare la vista che gli serve in quel momento.
- **La frammentazione tra strumenti.** Oggi chi vuole visualizzare conoscenza relazionale usa strumenti separati (Gephi, yEd, tool di BI, plugin Neo4j), scollegati dalla propria base di conoscenza e dall'AI. LocalMind integra la visualizzazione *dentro* la piattaforma, sincronizzata con l'ingestione, la ricerca e la chat.

La radice comune di tutti questi problemi: **il grafo è una struttura ad alta dimensionalità relazionale che la mente umana comprende solo attraverso una rappresentazione spaziale ben progettata**, e tale rappresentazione deve scalare, filtrare e collegarsi all'AI per essere davvero utile.

### 1.2 La soluzione LocalMind

LocalMind fornisce un **visualizzatore di grafo interattivo, integrato e local-first**, costruito come feature standalone Angular 21 con stato gestito via Signal. Le sue caratteristiche fondanti:

1. **Rendering ad alte prestazioni (WebGL/GPU).** Il motore di rendering offloada il disegno di nodi e archi alla GPU, restando fluido (target ≥ 30–60 fps in pan/zoom) fino a decine di migliaia di elementi visibili, con fallback Canvas 2D per ambienti senza WebGL.
2. **Esplorazione progressiva (progressive disclosure).** Non si parte mai dal grafo intero: si parte da un nodo o da un piccolo sottografo (es. i risultati di una ricerca o le citazioni dell'AI) e si **espande progressivamente** per relazioni, un salto alla volta, mantenendo il controllo della complessità.
3. **Level-of-Detail (LoD) e clustering.** A bassi livelli di zoom i nodi vengono aggregati in cluster/comunità (etichette di gruppo); avvicinandosi (zoom-in) i cluster si "aprono" mostrando i nodi e gli archi di dettaglio. Il dettaglio è funzione del livello di zoom e del budget di rendering.
4. **Pesi come segnale visivo di primo livello.** Il peso degli archi e la centralità dei nodi guidano dimensione, spessore, opacità, colore e priorità di rendering: l'occhio è guidato verso ciò che conta.
5. **Filtri multidimensionali.** Per tipo di nodo, tipo di relazione, dominio, intervallo di peso, freschezza, provenienza — applicabili e combinabili in tempo reale, con la possibilità di salvare "viste" riutilizzabili.
6. **Layout intelligenti e fuori-thread.** Algoritmi force-directed (e gerarchici/radiali/circolari su richiesta) calcolati in **Web Worker** per non bloccare l'interfaccia, con stabilizzazione progressiva e possibilità di "fissare" (pin) i nodi.
7. **Integrazione nativa con il GraphRAG.** Dalla chat dell'AI si "salta" al grafo evidenziando i nodi e i percorsi citati; dal grafo si avvia una domanda contestuale all'AI sul sottografo selezionato. Visualizzazione e ragionamento sono due facce della stessa esperienza.
8. **Local-first e bilingue.** Tutto gira nel browser, sui dati dell'istanza self-hosted; etichette, legende, tooltip e tipi di nodo/relazione sono bilingui IT/EN secondo i vincoli di progetto.

### 1.3 Il valore per gli stakeholder

| Stakeholder | Valore della visualizzazione del grafo |
|-------------|----------------------------------------|
| **Utente consumer (turismo/eventi)** | Esplora visivamente luoghi, itinerari ed esperienze collegate, scopre POI vicini e correlati seguendo gli archi |
| **Sviluppatore/architetto (enterprise)** | Vede dipendenze, blast radius, cicli e percorsi critici; verifica visivamente le risposte di impact analysis |
| **Knowledge worker** | Naviga documenti, mail, decisioni e persone correlate; trova collegamenti non evidenti tra silos informativi |
| **Curatore/editor del grafo** | Individua nodi orfani, duplicati e relazioni mancanti; corregge e annota direttamente sul grafo |
| **Utente dell'AI** | Verifica e approfondisce le citazioni del GraphRAG passando dal testo al grafo e viceversa |
| **Amministratore dell'istanza** | Monitora la forma, la densità e la crescita del grafo; valuta copertura e qualità dell'ingestione |
| **Contributor open source** | Dispone di un componente di visualizzazione riusabile, documentato e bilingue, estendibile a nuovi domini |

### 1.4 Confini di responsabilità (cosa NON è)

- **Non è il motore a grafo.** La visualizzazione *consuma* il grafo (nodi, archi, pesi, query di vicinato e percorso) esposto dal motore core; non lo costruisce, non lo persiste e non definisce i tipi. È un consumatore delle API di grafo.
- **Non è uno strumento di BI/dashboard.** Non sostituisce grafici, KPI e report tabellari; si concentra sulla rappresentazione *relazionale* (nodi e archi), eventualmente affiancata a pannelli di dettaglio.
- **Non è un editor CAD di diagrammi.** Pur consentendo annotazione e pin dei nodi, non è uno strumento di disegno libero: il layout deriva dai dati e dagli algoritmi, non da posizionamento manuale arbitrario (salvo override puntuali).
- **Non esegue analitica pesante lato client su tutto il grafo.** Le query strutturali (percorsi, vicinato N-hop, metriche) sono delegate al backend (MySQL); il client riceve e renderizza sottografi gestibili, non l'intero dataset.
- **Non esfiltra dati.** Il rendering è interamente browser-side sui dati dell'istanza; nessuna libreria invia il grafo a servizi esterni.

---

## 2. Personas & utenti target

| Persona | Descrizione | Obiettivo primario | Esigenza chiave dalla visualizzazione |
|---------|-------------|--------------------|---------------------------------------|
| **Esploratore consumer** | Utente finale che scopre il territorio | Trovare luoghi/eventi/itinerari collegati | Navigazione semplice, mobile-friendly, mappa+grafo |
| **Analista enterprise** | Sviluppatore/architetto | Capire dipendenze e impatto | Filtri per tipo/dominio, percorsi pesati, espansione N-hop |
| **Knowledge worker** | Professionista su conoscenza interna | Collegare documenti, mail, persone, decisioni | Ricerca → grafo, evidenziazione, pannelli di dettaglio |
| **Curatore del grafo** | Editor/moderatore dei contenuti | Pulire, annotare, correggere il grafo | Individuazione orfani/duplicati, editing inline, audit visivo |
| **Utente dell'assistente AI** | Chiunque usi la chat GraphRAG | Verificare le risposte dell'AI | Salto chat↔grafo, evidenziazione dei nodi/percorsi citati |
| **Power user / data explorer** | Utente avanzato analitico | Scoprire pattern e comunità | Layout multipli, clustering, metriche visive, viste salvate |
| **Amministratore dell'istanza** | Tecnico che gestisce LocalMind | Monitorare salute e crescita del grafo | Statistiche di grafo, densità, copertura, performance |
| **Sviluppatore frontend / contributor** | Estende LocalMind | Riusare/estendere il visualizzatore | Componente standalone, API chiare, temi per dominio, i18n |
| **Utente con esigenze di accessibilità** | Utenza con limitazioni visive/motorie | Comprendere il grafo in modo accessibile | Navigazione da tastiera, contrasto, descrizioni testuali alternative |

**Segmentazione dei ruoli** (dominio \`auth\`): i **lettori** esplorano in sola lettura; i **curatori** possono annotare, fissare e correggere nodi/relazioni dal grafo; gli **amministratori** configurano temi, layout di default, soglie di performance e visibilità dei sottografi sensibili. La visibilità di alcuni tipi/domini può essere ristretta per ruolo (es. sottografi enterprise riservati).

---

## 3. Requisiti in input

Questa sezione definisce in dettaglio **cosa deve ricevere il visualizzatore** per produrre un'esperienza corretta, performante e fedele al grafo. A differenza degli ambiti di ingestione (dove l'input è il dato grezzo da estrarre), qui l'input è triplice: i **dati di grafo già strutturati** dal motore core, le **interazioni dell'utente** e la **configurazione di rendering/dominio**.

### 3.1 Input di dati: il sottografo da rappresentare

Il visualizzatore **non carica mai l'intero grafo**: opera su **sottografi** richiesti al backend tramite le API di grafo. Ogni richiesta di rendering riceve un payload normalizzato e immutabile con la seguente struttura minima ed estesa:

| Categoria | Campi minimi (MVP) | Campi estesi (evoluzione) |
|-----------|--------------------|---------------------------|
| **Nodo — identità** | id canonico, tipo di nodo, label (IT/EN) | alias, descrizione breve, namespace/dominio |
| **Nodo — visuale** | dominio (per colore), peso/centralità (per dimensione) | icona, badge di stato (stale/orfano/nuovo), thumbnail |
| **Nodo — posizione** | (opzionale) coordinate pre-calcolate x,y | livello di cluster, ancoraggio (pin) |
| **Nodo — metadati** | provenienza, data ultima osservazione | attributi specifici di dominio (chiave-valore) |
| **Arco — identità** | id, nodo sorgente, nodo destinazione, tipo relazione | direzione, etichetta relazione (IT/EN) |
| **Arco — visuale** | peso normalizzato (per spessore/opacità) | confidenza della fonte, freschezza, multiplicità |
| **Sottografo — meta** | n. totale nodi/archi disponibili, n. restituiti, flag "troncato" | suggerimenti di espansione, cluster pre-calcolati |
| **Paginazione/espansione** | cursore/token per caricare il vicinato successivo | profondità raggiunta, budget residuo |

**Principi sull'input dati:**

- **Immutabilità del payload.** Ogni sottografo ricevuto è trattato come immutabile; le trasformazioni (filtri, layout, evidenziazioni) producono *nuove* viste derivate, mai mutazioni in-place — coerente con la regola di immutabilità di progetto.
- **Normalizzazione dei pesi.** Il peso degli archi arriva già normalizzato per tipo di relazione (o accompagnato dai parametri per normalizzarlo lato client), così che lo spessore visivo sia confrontabile.
- **Budget di payload.** Ogni risposta dichiara se è stata **troncata** (più nodi/archi disponibili di quanti restituiti) e fornisce i token per l'espansione progressiva, così l'utente sa sempre che "c'è dell'altro".
- **Bilinguismo dei dati visivi.** Label di nodi, etichette di relazioni e descrizioni dei tipi arrivano nelle due lingue (o con chiave i18n risolvibile), per rispettare il vincolo IT/EN.
- **Validazione al confine.** Il client valida lo schema del payload (id presenti, archi che referenziano nodi esistenti nel sottografo, pesi numerici nel range atteso) e fallisce/degrada in modo controllato su dati malformati, senza mai bloccare l'intera UI.

### 3.2 Input di interazione: le azioni dell'utente

L'esperienza è guidata dalle interazioni, che il visualizzatore traduce in aggiornamenti di vista o in nuove richieste al backend:

| Interazione | Effetto | Richiede backend? |
|-------------|---------|-------------------|
| **Pan / zoom** | Sposta e scala la vista; attiva LoD | No (client) |
| **Click su nodo** | Selezione + apertura pannello di dettaglio | Eventuale (dettaglio esteso) |
| **Doppio click / "espandi"** | Carica e aggiunge il vicinato del nodo | Sì (query vicinato) |
| **Hover** | Tooltip + evidenziazione archi incidenti | No |
| **Ricerca testuale** | Trova e centra i nodi corrispondenti | Sì (ricerca semantica/strutturale) |
| **Applicazione filtro** | Nasconde/mostra per tipo/dominio/peso/freschezza | No se già in vista; Sì se richiede dati nuovi |
| **Selezione percorso (A→B)** | Evidenzia il cammino pesato tra due nodi | Sì (query di percorso) |
| **Collapse/cluster** | Aggrega un sottoinsieme in un super-nodo | No (client) |
| **Pin / sposta nodo** | Fissa la posizione, escludendolo dal layout | No |
| **"Chiedi all'AI sul sottografo"** | Invia il sottografo selezionato come contesto GraphRAG | Sì (chat/LLM) |
| **Annotazione/correzione (curatore)** | Modifica nodo/relazione | Sì (scrittura sul grafo) |
| **Salva vista** | Persiste filtri+layout+focus come vista riutilizzabile | Sì (persistenza preferenze) |
| **Esporta** | Genera PNG/SVG/JSON del sottografo corrente | No (client) |
| **Navigazione da tastiera** | Sposta il focus tra nodi adiacenti (accessibilità) | No |

### 3.3 Input di configurazione & temi

- **Tema di dominio.** Mappa tipo-di-nodo → colore/icona e tipo-di-relazione → stile di arco, specifica per dominio (consumer vs enterprise) e personalizzabile dall'amministratore. Riuso del sistema di temi (light/dark) già presente nel frontend.
- **Layout di default.** Algoritmo iniziale (force-directed, gerarchico, radiale, circolare) e suoi parametri (forze, distanze, iterazioni), configurabili per dominio e per vista.
- **Soglie di performance.** Numero massimo di nodi/archi renderizzati simultaneamente, soglia di attivazione del clustering/LoD, fps target, dimensione del budget di espansione — adattabili al dispositivo.
- **Politiche di visibilità.** Quali tipi/domini sono visibili per ruolo (\`auth\`), quali sottografi sono ristretti.
- **Preferenze utente.** Lingua (IT/EN), tema, layout preferito, viste salvate, densità di etichette.

### 3.4 Vincoli di qualità sull'input

- **Coerenza referenziale del sottografo:** ogni arco deve puntare a nodi presenti nel payload o esplicitamente marcati come "frontiera" (espandibili).
- **Determinismo del layout su input identico** (a parità di seed): a parità di sottografo e parametri, il layout è riproducibile, così che la vista non "salti" tra refresh.
- **Degradazione controllata:** payload oltre la soglia → attivazione automatica di clustering/LoD invece di rendering integrale; assenza di WebGL → fallback Canvas; dati parziali → rendering del disponibile con segnalazione, mai errore bloccante.
- **Privacy by default:** nessun dato del grafo inviato a librerie/CDN esterne; tutte le risorse di rendering sono self-hostate.

---

## 4. Flusso dell'attività (step-by-step)

Il flusso si articola in tre macro-cicli: il **ciclo di accesso e caricamento iniziale** (come si entra nel grafo), il **ciclo di esplorazione interattiva** (il cuore dell'esperienza) e il **ciclo di integrazione con l'AI** (chat↔grafo). Si aggiunge il ciclo di **curatela visiva** per i curatori.

### 4.1 Ciclo di accesso e caricamento iniziale

1. **Punto di ingresso.** L'utente arriva al grafo in uno di quattro modi: (a) dalla feature dedicata "Grafo" nel menu; (b) da un risultato di ricerca ("vedi nel grafo"); (c) da una risposta dell'AI ("apri i nodi citati"); (d) dalla scheda di un'entità di dominio (un documento, un luogo, un servizio → "esplora collegamenti"). Il punto di ingresso determina il **nodo o sottografo seme**.
2. **Richiesta del seme.** Il frontend chiede al backend il sottografo iniziale: il nodo seme + il suo vicinato di primo livello (1-hop), filtrato per i tipi/domini di default e limitato dal budget di nodi. Mai "tutto il grafo".
3. **Validazione e normalizzazione.** Il payload ricevuto viene validato (schema, coerenza referenziale, pesi) e normalizzato in strutture immutabili pronte per il rendering; le label vengono risolte nella lingua corrente (IT/EN).
4. **Calcolo del layout (Web Worker).** Le posizioni dei nodi vengono calcolate fuori dal thread principale (force-directed o layout dichiarato), con stabilizzazione progressiva: l'utente vede il grafo "assestarsi" senza che l'interfaccia si blocchi. Se il payload include coordinate pre-calcolate, si saltano i primi cicli.
5. **Primo rendering (GPU).** Nodi e archi vengono disegnati via WebGL; il peso guida dimensione/spessore/opacità, il dominio guida il colore, il tipo guida l'icona. Viene mostrata la legenda bilingue.
6. **Orientamento.** Compaiono minimappa, controlli di zoom/fit, breadcrumb del seme e contatore "N nodi mostrati di M disponibili". L'utente sa dove si trova e quanto altro esiste.

### 4.2 Ciclo di esplorazione interattiva (il cuore)

1. **Osservazione d'insieme.** A basso zoom l'utente vede la *forma* del sottografo: cluster, hub (nodi grandi/molto connessi), archi spessi (relazioni forti), nodi periferici. Il LoD mostra etichette di cluster, non di singoli nodi.
2. **Messa a fuoco (hover/zoom).** Avvicinandosi a un'area, i cluster si aprono progressivamente; l'hover su un nodo evidenzia i suoi archi e attenua il resto (highlight + fade del contesto), riducendo il rumore visivo.
3. **Selezione e dettaglio.** Il click su un nodo apre un pannello laterale con i suoi attributi (tipo, dominio, descrizione, provenienza, freschezza) e le azioni disponibili (espandi, fissa, chiedi all'AI, vai alla scheda di dominio).
4. **Espansione progressiva.** Il comando "espandi" carica il vicinato del nodo (nuova query al backend), che viene **integrato** nel grafo esistente con animazione fluida; i nuovi nodi entrano rispettando filtri e budget. L'utente costruisce così, salto dopo salto, esattamente il sottografo che gli serve — l'antidoto all'hairball.
5. **Filtraggio dinamico.** In qualsiasi momento l'utente applica/combina filtri: per **tipo di nodo** (es. solo "Documento" e "Persona"), **tipo di relazione** (es. solo "CITES"), **dominio** (es. solo enterprise), **intervallo di peso** (es. archi con peso > soglia, per ridurre il rumore), **freschezza** (nascondi gli stale). I filtri trasformano la vista istantaneamente (client) o richiedono nuovi dati quando necessario.
6. **Ricerca di percorso.** Selezionando due nodi e chiedendo "percorso", il backend calcola il cammino pesato e il visualizzatore lo evidenzia (gli altri elementi sfumano), rispondendo visivamente a "come è collegato A a B?".
7. **Riduzione della complessità.** Quando la vista si affolla, l'utente può **collassare** rami in super-nodi, attivare il **clustering** per comunità, alzare la soglia di peso o tornare indietro nella **cronologia di navigazione** (undo/redo dell'esplorazione). La minimappa e il "fit to screen" prevengono il disorientamento.
8. **Personalizzazione del layout.** L'utente può cambiare algoritmo (radiale per gerarchie, circolare per cicli, force per esplorazione libera), fissare nodi chiave (pin) ed escluderli dalla simulazione per stabilizzare la vista.
9. **Salvataggio e condivisione.** La configurazione corrente (focus + filtri + layout) può essere **salvata come vista** riutilizzabile o **esportata** (PNG/SVG per report, JSON per riuso), il tutto in locale.

### 4.3 Ciclo di integrazione con l'AI (chat ↔ grafo)

1. **Dal grafo all'AI.** L'utente seleziona un nodo o un sottografo e chiede "spiega questo / cosa è impattato / riassumi i collegamenti": il sottografo selezionato viene passato come **contesto** all'assistente GraphRAG (riuso del dominio \`llm\`/chat).
2. **Dalla AI al grafo.** Quando l'AI risponde citando nodi e percorsi, ogni citazione è cliccabile: il click apre il visualizzatore con quei nodi **già evidenziati** e il percorso tracciato, permettendo la verifica immediata.
3. **Esplorazione guidata.** L'utente alterna domanda testuale ed esplorazione visiva: chiede, vede, espande, ri-chiede sul nuovo sottografo. La visualizzazione diventa il "tavolo di lavoro" condiviso tra utente e AI.
4. **Azioni a valle.** Da una vista (es. un blast radius) si possono innescare azioni: creare un ticket, notificare gli owner, generare un report (riuso dei domini \`automation\`, \`messaging\`, \`agent\`).

### 4.4 Ciclo di curatela visiva (curatori)

1. **Individuazione visiva delle anomalie.** Il curatore usa filtri e metriche per evidenziare nodi orfani (grado 0), duplicati sospetti (vicinanza semantica \`SIMILAR_TO\`), hub anomali, relazioni a bassa confidenza.
2. **Correzione inline.** Direttamente dal grafo, annota, fonde duplicati, conferma/smentisce relazioni inferite, corregge tipi/etichette — ogni intervento è versionato e immutabile (nuova revisione, non mutazione).
3. **Validazione.** Le modifiche aggiornano il grafo via API; eventi di dominio propagano l'aggiornamento ad altre viste e cache.

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

A differenza degli ambiti di dominio, la visualizzazione **non introduce tipi di nodo o relazione propri**: è agnostica e rappresenta *qualsiasi* tipo definito dai domini. Il suo "modello" è quindi un **modello di mappatura visiva**: come ogni tipo di nodo, tipo di relazione e peso si traduce in attributi grafici. Definisce inoltre alcuni costrutti visivi (cluster, frontiera, super-nodo) che non esistono nel grafo ma servono alla rappresentazione.

### 5.1 Mappatura dei tipi di nodo → attributi visivi

| Dimensione del dato | Attributo visivo | Criterio |
|---------------------|------------------|----------|
| **Dominio** (consumer/enterprise/…) | Colore (tinta) | Palette per dominio, coerente con i temi |
| **Tipo di nodo** (Luogo, Documento, Persona, Servizio…) | Icona / forma | Iconografia per tipo, legenda bilingue |
| **Centralità / peso del nodo** | Dimensione | Nodi più connessi/critici più grandi |
| **Stato** (nuovo / stale / orfano / selezionato) | Bordo / badge / opacità | Segnalazione visiva immediata |
| **Cluster di appartenenza** | Alone / raggruppamento spaziale | A basso zoom (LoD) |
| **Pertinenza alla query corrente** | Saturazione / fade | Risultati e citazioni AI in risalto, contesto attenuato |

### 5.2 Mappatura dei tipi di relazione → attributi visivi

| Dimensione del dato | Attributo visivo | Criterio |
|---------------------|------------------|----------|
| **Peso dell'arco** | Spessore + opacità | Relazioni forti più spesse/opache |
| **Tipo di relazione** | Colore / tratteggio dell'arco | Stile per tipo (es. continuo vs tratteggiato) |
| **Direzione** | Freccia / curvatura | Archi direzionati con punta; bidirezionali curvi |
| **Confidenza della fonte** | Opacità / stile | Relazioni inferite più tenui di quelle osservate |
| **Freschezza** | Desaturazione | Archi stale visivamente "sbiaditi" |
| **Molteplicità** (più archi A→B) | Curvatura/offset | Archi paralleli separati per leggibilità |

### 5.3 Costrutti puramente visivi (non presenti nel grafo)

| Costrutto | Descrizione | Scopo |
|-----------|-------------|-------|
| **Cluster / Comunità** | Aggregazione di nodi affini in un super-nodo etichettato | Ridurre la complessità a basso zoom (LoD) |
| **Nodo frontiera** | Nodo al bordo del sottografo con vicini non ancora caricati | Indicare l'espandibilità ("c'è dell'altro") |
| **Super-nodo (collapse)** | Ramo collassato dall'utente in un singolo nodo | Controllo manuale della densità |
| **Nodo fantasma / placeholder** | Riferimento a un nodo non incluso nel payload | Mantenere la coerenza degli archi di frontiera |
| **Percorso evidenziato** | Sequenza di nodi/archi del cammino A→B | Risposta visiva alle query di percorso |

### 5.4 Criteri di peso nella visualizzazione

Il visualizzatore **non calcola** il peso (è derivato dal motore), ma lo **traduce e lo usa** secondo questi criteri:

- **Mappatura non lineare.** Lo spessore/dimensione segue una scala (logaritmica o percentile) per evitare che pochi archi dominanti schiaccino tutti gli altri; la percezione resta leggibile su range ampi.
- **Soglia di peso come filtro.** L'utente alza una soglia minima di peso per "potare" gli archi marginali e far emergere la struttura portante (riduzione del rumore).
- **Priorità di rendering per peso.** In condizioni di budget (troppi elementi), si renderizzano per primi gli archi/nodi a peso maggiore; quelli marginali sono i primi a essere aggregati o nascosti dal LoD.
- **Peso come guida del layout.** Gli archi a peso maggiore esercitano forze di attrazione più forti nel layout force-directed, avvicinando spazialmente i nodi fortemente correlati (i cluster emergono dal peso).
- **Normalizzazione per tipo.** Il confronto di spessore è significativo *all'interno* dello stesso tipo di relazione; tipi diversi possono usare scale distinte segnalate in legenda.

---

## 6. Fonti dati & connettori (ingestione)

La visualizzazione **non ha connettori di ingestione propri**: la sua "fonte dati" è il **motore a grafo** di LocalMind, consumato tramite API. Questa sezione descrive perciò le **interfacce di consumo** e le **scelte tecnologiche di rendering** che sono il vero cuore dell'ambito.

### 6.1 Le API di grafo consumate

| API (port/in del motore grafo) | Cosa fornisce alla visualizzazione | Uso |
|--------------------------------|------------------------------------|-----|
| **Get nodo + vicinato (1-hop)** | Sottografo seme per il caricamento iniziale | Avvio esplorazione |
| **Espandi nodo (N-hop, filtrato)** | Vicinato successivo per espansione progressiva | Esplorazione interattiva |
| **Query di percorso (A→B)** | Cammino pesato tra due nodi | Evidenziazione percorso |
| **Sottografo per query/ricerca** | Nodi pertinenti a una ricerca semantica/strutturale | Ricerca → grafo |
| **Cluster/comunità pre-calcolati** | Aggregazioni per il LoD a basso zoom | Riduzione complessità |
| **Metriche di grafo** (grado, centralità) | Attributi per dimensione/priorità dei nodi | Pesatura visiva |
| **Dettaglio nodo/relazione** | Attributi estesi per il pannello laterale | Drill-down |
| **Scrittura (curatela)** | Annotazioni/correzioni dal grafo | Ciclo di curatela |

Tutte le query rispettano un **budget** (max nodi/archi, profondità) negoziato col client e restituiscono metadati di troncamento per l'espansione progressiva. Le query strutturali pesanti restano sul backend (MySQL, indici, eventuali tabelle di adiacenza/closure, cache Caffeine); il client non scarica mai il grafo intero.

### 6.2 Scelta della tecnologia di rendering (la decisione fondante)

La ricerca di settore 2025–2026 sulla visualizzazione di grafi su web converge su alcuni principi solidi, che guidano la scelta tecnologica di LocalMind:

- **WebGL/GPU batte SVG e Canvas su scala.** Studi comparativi recenti su dataset da 100 a 200.000 nodi mostrano che il rendering SVG (DOM) collassa già a qualche migliaio di elementi, Canvas 2D regge meglio ma resta CPU-bound, mentre **WebGL** (rendering instanziato sulla GPU) mantiene l'interattività su decine/centinaia di migliaia di elementi. Per LocalMind, **WebGL è la scelta primaria**, con **Canvas 2D come fallback** dove WebGL non è disponibile.
- **Librerie mature.** L'ecosistema offre opzioni battle-tested: **Sigma.js** (su graphology, WebGL, pensata proprio per "render large graphs in the browser"), **Cosmograph** (GPU, centinaia di migliaia di nodi/archi), **react-force-graph/force-graph** (WebGL/Canvas, 2D/3D), **G6** e **Cytoscape.js** (ricche di funzionalità, ottime su scale medie). La scelta privilegia una libreria **WebGL, open source, framework-agnostica e self-hostabile** (no CDN obbligatorie), integrabile in un componente standalone Angular 21 — coerente con i vincoli local-first e open source. Sigma.js + graphology è il candidato di riferimento per l'MVP, con valutazione di Cosmograph per le scale estreme.
- **Layout fuori dal main thread.** Il calcolo del layout force-directed è costoso: va eseguito in **Web Worker** (o accelerato su GPU) per non bloccare l'interfaccia, con stabilizzazione progressiva e possibilità di interruzione.
- **Level-of-Detail e clustering.** La letteratura su "interactive level-of-detail rendering of large graphs" e su filtering/aggregazione conferma che mostrare tutto è controproducente: si organizza il grafo gerarchicamente per centralità/comunità e si rivela il dettaglio solo dove l'utente guarda (viewport-based rendering, out-of-view pruning, canvas layering).
- **Espansione progressiva guidata dai dati.** Partire da un seme ed espandere a richiesta è sia una scelta UX (evita l'hairball) sia una scelta di performance (si renderizza solo ciò che serve).

### 6.3 Architettura di rendering lato client

| Componente | Responsabilità | Tecnologia |
|------------|----------------|------------|
| **Graph data store (Signal)** | Stato immutabile del sottografo, filtri, selezione, cronologia | Angular Signals |
| **Render engine** | Disegno GPU di nodi/archi, pan/zoom, picking | WebGL (Sigma.js) + fallback Canvas |
| **Layout worker** | Calcolo posizioni force-directed/gerarchico | Web Worker (graphology layout / ForceAtlas2) |
| **LoD/clustering controller** | Aggregazione e dettaglio in base a zoom/budget | Logica client + cluster dal backend |
| **Interaction layer** | Eventi (hover, click, drag, tastiera), tooltip, pannelli | Angular standalone components |
| **API client** | Richieste sottografo/espansione/percorso | \`ApiService\` (core) |
| **Theme/i18n provider** | Palette per dominio, legende, etichette IT/EN | Sistema temi + \`TranslatePipe\` |

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

Questa sezione mappa concretamente le funzionalità, distinguendo ciò che si **crea ex novo**, ciò che si **sviluppa estendendo** l'esistente e ciò che si **mantiene**. La feature vive prevalentemente nel frontend (\`localmind-frontend/src/app/features/graph/\`), consumando le API del motore a grafo; un minimo di backend serve per le query ottimizzate per la visualizzazione e per le preferenze/viste salvate.

### 7.1 MVP (visualizzatore esplorativo di base)

| # | Funzionalità | Tipo | Moduli LocalMind coinvolti |
|---|--------------|------|----------------------------|
| 1 | **Feature Angular \`graph\` standalone** con routing lazy e Signal store | Creare | \`localmind-frontend\` features, layout, app.routes |
| 2 | **Render engine WebGL** (nodi/archi/pesi) con fallback Canvas | Creare | libreria WebGL self-hostata (Sigma.js/graphology) |
| 3 | **Layout force-directed in Web Worker** con stabilizzazione | Creare | Web Worker, graphology layout |
| 4 | **Caricamento del seme** (nodo + vicinato 1-hop) | Sviluppare (riuso API grafo) | API motore grafo, \`ApiService\` |
| 5 | **Espansione progressiva** (espandi nodo, integra vicinato) | Creare | API espansione vicinato |
| 6 | **Pan/zoom, fit, minimappa, breadcrumb, contatore nodi** | Creare | render engine, UI |
| 7 | **Selezione + pannello di dettaglio** del nodo | Creare | UI, API dettaglio |
| 8 | **Hover highlight** (archi incidenti + fade contesto) | Creare | render engine |
| 9 | **Filtri per tipo di nodo / tipo di relazione / dominio** | Creare | Signal store, UI filtri |
| 10 | **Filtro per intervallo di peso** (soglia anti-rumore) | Creare | Signal store |
| 11 | **Mappatura visiva pesi → spessore/dimensione** (scala non lineare) | Creare | render engine |
| 12 | **Tema per dominio + legenda bilingue IT/EN** | Sviluppare (riuso temi + i18n) | sistema temi, \`TranslatePipe\`, enum bilingui |
| 13 | **Ricerca → centratura nodo nel grafo** | Sviluppare (riuso ricerca) | dominio \`search\`/grafo |
| 14 | **Salto chat AI → grafo** (evidenzia nodi citati) | Sviluppare (riuso \`llm\`/chat) | dominio \`llm\`, chat |
| 15 | **Degradazione controllata** (no WebGL → Canvas; payload grande → LoD base) | Creare | render engine |
| 16 | **Accessibilità base** (navigazione tastiera, contrasto, alt testuali) | Creare | UI |

### 7.2 Evoluzione (scala, intelligenza, collaborazione)

| # | Funzionalità | Tipo |
|---|--------------|------|
| 17 | **Level-of-Detail avanzato + clustering per comunità** | Creare |
| 18 | **Layout multipli** (gerarchico, radiale, circolare) selezionabili | Creare |
| 19 | **Pin nodi + override manuale di posizione** | Creare |
| 20 | **Query di percorso (A→B) con evidenziazione** | Creare |
| 21 | **Collapse/expand di rami in super-nodi** | Creare |
| 22 | **Cronologia di navigazione (undo/redo dell'esplorazione)** | Creare |
| 23 | **Viste salvate** (focus+filtri+layout) e condivisibili | Sviluppare (riuso preferenze) |
| 24 | **Esportazione** PNG/SVG/JSON del sottografo | Creare |
| 25 | **Grafo → AI** (invio sottografo selezionato come contesto GraphRAG) | Sviluppare (riuso \`llm\`) |
| 26 | **Curatela visiva** (annotazione, fusione duplicati, correzione inline) | Sviluppare (riuso API scrittura grafo + \`auth\`) |
| 27 | **Metriche di grafo visive** (centralità, comunità, SPOF, densità) | Creare |
| 28 | **Modalità 3D / VR** per esplorazione immersiva di grafi densi | Creare |
| 29 | **Diff temporale visivo** ("come è cambiato il grafo nel tempo") | Creare |
| 30 | **Vista ibrida grafo+mappa geografica** (verticale consumer/territorio) | Creare |
| 31 | **Plugin di visualizzazione per dominio** (temi/iconografie via PF4J/marketplace) | Sviluppare (riuso PF4J/marketplace) |
| 32 | **GPU layout (compute shader)** per scale estreme (100k+ nodi) | Creare |

### 7.3 Manutenzione (continuo)

- Aggiornamento della libreria di rendering e dei suoi binding al passo con le versioni (sicurezza, performance, API).
- Taratura periodica delle soglie di performance (budget nodi, attivazione LoD, fps target) sui dispositivi reali.
- Tenuta delle palette/iconografie e delle legende bilingui al passo con i nuovi tipi di nodo/relazione introdotti dai domini.
- Profiling e ottimizzazione continua (frame rate, memoria GPU, tempo di stabilizzazione del layout).
- Test cross-browser e di accessibilità ricorrenti; aggiornamento della documentazione IT/EN e dei log in \`Sviluppi/\` (vincolo di progetto).

---

## 8. Casi d'uso AI / GraphRAG

La visualizzazione è il **complemento visivo del GraphRAG**: rende verificabile ciò che l'AI afferma e fornisce all'AI un contesto selezionato dall'utente. Tutti i casi seguenti chiudono il ciclo testo↔grafo.

| Caso d'uso | Interazione utente↔AI↔grafo | Ruolo della visualizzazione |
|------------|------------------------------|-----------------------------|
| **Verifica delle citazioni** | L'AI risponde citando nodi/percorsi | Click sulla citazione → grafo con nodi/percorso evidenziati |
| **Domanda sul sottografo selezionato** | L'utente seleziona nodi e chiede "spiega/riassumi" | Il sottografo selezionato diventa contesto GraphRAG |
| **Impact analysis visiva** | "Cosa è impattato da X?" | Il blast radius (espansione N-hop pesata) è mostrato come sottografo evidenziato |
| **Scoperta di collegamenti non evidenti** | "Cosa collega A e B?" | Percorso pesato A→B tracciato visivamente |
| **Esplorazione guidata dall'AI** | "Guidami nel dominio ordini" | L'AI propone i nodi d'ingresso, l'utente li esplora visivamente |
| **Suggerimento di collegamenti mancanti** | L'AI propone relazioni \`SIMILAR_TO\` | I suggerimenti appaiono come archi tratteggiati da confermare nel grafo |
| **Riassunto di un cluster** | "Cosa rappresenta questo cluster?" | Il cluster selezionato è riassunto dall'AI, con citazione dei nodi membri |
| **Narrazione di onboarding** | "Spiegami questa parte del sistema" | L'AI racconta mentre il grafo evidenzia i nodi citati passo-passo |
| **Individuazione di anomalie** | "Ci sono nodi orfani o hub anomali?" | Le metriche evidenziano visivamente gli outlier nel grafo |

L'AI locale (Ollama di default, fallback multi-provider) genera spiegazioni, riassunti e suggerimenti; la ricerca semantica (Qdrant) individua i nodi d'ingresso; la navigazione deterministica (MySQL) costruisce i sottografi; la visualizzazione li rende esplorabili e verificabili — il tutto local-first.

---

## 9. KPI & metriche di successo

| Categoria | KPI | Target indicativo |
|-----------|-----|-------------------|
| **Performance** | Frame rate in pan/zoom su grafo "tipico" (≤ 5k nodi visibili) | ≥ 45–60 fps |
| **Scalabilità** | Nodi/archi renderizzabili restando interattivi (WebGL) | ≥ 50.000 elementi |
| **Reattività** | Tempo di primo rendering del seme (1-hop) | < 1 s |
| **Reattività** | Tempo di espansione di un nodo (vicinato) | < 500 ms |
| **Layout** | Tempo di stabilizzazione del layout (grafo tipico) | < 2 s, senza freeze UI |
| **Usabilità** | Tasso di completamento del task "trova il collegamento tra A e B" | > 85% |
| **Adozione** | % sessioni che usano il grafo / che passano da chat a grafo | crescente |
| **Verifica AI** | % citazioni dell'AI aperte e verificate nel grafo | > 50% |
| **Accessibilità** | Conformità a criteri WCAG (contrasto, tastiera) | livello AA sui flussi chiave |
| **Privacy** | Dati del grafo inviati a servizi esterni di rendering | 0% |
| **i18n** | Copertura etichette/legende/tipi tradotti IT/EN | 100% |

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---------|---------|-------------|
| **Collasso di performance su grafi grandi** | UI inutilizzabile, abbandono | Rendering WebGL/GPU, LoD, espansione progressiva, viewport culling, budget di nodi |
| **Sovraccarico cognitivo ("hairball")** | Grafo illeggibile, nessun insight | Si parte da un seme, mai dal grafo intero; filtri, clustering, soglie di peso, fade del contesto |
| **Layout che blocca il main thread** | Interfaccia congelata | Calcolo del layout in Web Worker, stabilizzazione progressiva, interruzione |
| **Disorientamento durante l'esplorazione** | Frustrazione, perdita di contesto | Minimappa, breadcrumb, cronologia undo/redo, fit-to-screen, pin |
| **Assenza/instabilità di WebGL** | Schermo vuoto su certi dispositivi | Fallback Canvas 2D, rilevamento capacità, degradazione controllata |
| **Dipendenza da libreria esterna** | Lock-in, vulnerabilità, abbandono upstream | Libreria open source self-hostata dietro un'astrazione (render engine port) sostituibile |
| **Incoerenza tra grafo e vista** | Sfiducia | Payload immutabili, validazione al confine, layout deterministico, refresh controllati |
| **Fuga di dati a CDN/servizi esterni** | Violazione local-first/privacy | Tutte le risorse self-hostate, nessuna chiamata esterna, rendering 100% client-side |
| **Carico sul backend per espansioni frequenti** | Lentezza, sovraccarico DB | Budget di query, cache (Caffeine), debounce, pre-fetch mirato dei vicini caldi |
| **Accessibilità trascurata** | Esclusione di utenti, non conformità | Navigazione da tastiera, contrasto, descrizioni testuali, alternative non solo visive |
| **Esplosione di tipi/colori indistinguibili** | Legenda illeggibile | Palette curate per dominio, raggruppamento dei tipi, filtri, legenda interattiva |

---

## 11. Manutenzione & evoluzione

La manutenzione segue i vincoli di progetto: file piccoli e coesi (componenti Angular standalone focalizzati, < 400 righe tipiche), immutabilità dello stato (Signal con viste derivate, mai mutazioni in-place), documentazione IT/EN sempre aggiornata e log in \`Sviluppi/\` con nomenclatura datata. Il render engine va tenuto dietro un'astrazione (porta) per poter sostituire la libreria sottostante senza riscrivere la feature.

**Linee di evoluzione:**

1. **Dalla scala media alla scala estrema.** Dal rendering WebGL standard al **GPU layout** (compute shader) e al partizionamento, per superare la soglia dei 100k+ nodi mantenendo l'interattività — rivalutando librerie come Cosmograph per i casi estremi.
2. **Dalla 2D all'immersione.** Modalità 3D/VR per grafi densi, dove la terza dimensione riduce la sovrapposizione e migliora la percezione dei cluster.
3. **Visualizzazioni specializzate per dominio.** Vista ibrida grafo+mappa geografica per il territorio (consumer), vista a livelli C4 per l'architettura software (enterprise), timeline per i thread di mail — distribuibili come **plugin di visualizzazione** via PF4J/marketplace.
4. **Dalla rappresentazione alla collaborazione.** Viste condivise, annotazioni collaborative, sessioni di esplorazione multi-utente sullo stesso sottografo.
5. **Intelligenza visiva proattiva.** L'AI propone automaticamente la vista più utile per una domanda (layout, filtri, focus), evidenzia anomalie e suggerisce i prossimi nodi da esplorare.
6. **Diff temporale.** Visualizzazione dell'evoluzione del grafo nel tempo (cosa è apparso/scomparso/cambiato), utile sia al territorio (eventi) sia all'enterprise (drift architetturale).

---

## 12. Integrazione con i moduli LocalMind esistenti

L'ambito è per natura **trasversale**: è la superficie visiva di tutti i domini e si innesta sull'architettura esagonale e sul frontend feature-driven già presenti.

| Modulo / dominio esistente | Ruolo nell'ambito Visualizzazione grafo |
|----------------------------|------------------------------------------|
| **\`knowledge\` / motore a grafo core** | Fonte unica dei dati: nodi, archi, pesi, query di vicinato/percorso |
| **\`llm\` / chat** | Ciclo chat↔grafo: citazioni cliccabili, domande sul sottografo, narrazione (Ollama default) |
| **\`vectorstore\` (Qdrant)** | Ricerca semantica che individua i nodi d'ingresso da centrare nel grafo |
| **\`search\`** | Ricerca → centratura/evidenziazione dei nodi nel visualizzatore |
| **\`auth\`** | Ruoli lettore/curatore/admin e visibilità dei tipi/sottografi sensibili |
| **\`automation\` / \`messaging\` / \`agent\`** | Azioni a valle innescate da una vista (ticket, notifiche owner, report) |
| **\`document\`, \`email\`, \`calendar\`, \`mcp\`, …** | Forniscono i nodi di dominio; dalla scheda di un'entità si apre "esplora nel grafo" |
| **\`plugin\` (PF4J) + \`marketplace\`** | Temi, iconografie e visualizzazioni specializzate distribuiti come plugin |
| **\`common\` (eventi, eccezioni)** | Eventi di aggiornamento del grafo che invalidano viste/cache; gestione errori tipizzata |
| **Frontend Angular 21** | Feature \`graph\` standalone, Signal store, render engine WebGL, Web Worker, i18n IT/EN |
| **Sistema temi + \`TranslatePipe\`** | Palette per dominio (light/dark) e legende/etichette bilingui |
| **\`core/services/api.service.ts\`** | Client HTTP per le API di grafo (seme, espansione, percorso, dettaglio) |
| **MySQL + indici/cache** | Esecuzione efficiente delle query di vicinato/percorso a supporto dell'espansione |
| **Caffeine cache** | Cache dei sottografi/vicinati caldi per ridurre il carico in espansione |

In sintesi, l'ambito **Visualizzazione grafo** è la **finestra dell'utente sul motore di knowledge graph universale**: trasforma una struttura dati relazionale altrimenti opaca in un'esperienza esplorativa, performante e verificabile, valida indifferentemente per il territorio consumer e per la conoscenza enterprise. Riusa integralmente l'infrastruttura LocalMind (frontend Angular 21, API di grafo su MySQL+Qdrant, AI Ollama locale, PF4J, temi e i18n) e rispetta i vincoli di local-first, privacy, open source e bilinguismo IT/EN. È il tassello core che rende il grafo non solo interrogabile dall'AI, ma anche **comprensibile, navigabile e degno di fiducia per l'essere umano**.
`;
