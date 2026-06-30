export const content = `# Sport & outdoor

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

## 1. Cosa risolviamo (problema & valore)

L'ambito **Sport & outdoor** porta nel motore di Knowledge Graph universale di LocalMind tutta la conoscenza che oggi vive frammentata, dispersa e poco interrogabile riguardo a **sentieri, attività all'aperto, impianti sportivi ed eventi**. È un verticale del gruppo **consumer** (scoperta del territorio) e ne condivide la filosofia "Wikipedia dei luoghi" community-driven, ma con un focus specifico: l'esperienza praticabile sul campo, governata da tre variabili che ne determinano la fattibilità reale — **difficoltà**, **stagione/condizioni** e **luogo**.

**Il problema concreto.** Chi pratica o vuole avvicinarsi a un'attività outdoor (escursionismo, trail running, MTB, arrampicata, sci alpinismo, kayak, vie ferrate, nuoto in acque libere, parapendio, ecc.) deve oggi mettere insieme manualmente informazioni che provengono da fonti eterogenee e spesso contraddittorie:

- **Dati cartografici grezzi** (OpenStreetMap, tracce GPX scaricate, mappe IGM/catastali) che descrivono la geometria del percorso ma non l'esperienza;
- **Portali verticali chiusi** (Komoot, Outdooractive, AllTrails, Wikiloc, Strava) che concentrano i dati nei loro silos, espongono API limitate o a pagamento e non sono self-hostable;
- **Recensioni e relazioni testuali** sparse su blog, forum, gruppi social e relazioni dei club alpini, ricche di sapere "tacito" (condizioni recenti, passaggi pericolosi, finestre stagionali) ma non strutturate;
- **Bollettini e dati dinamici** (meteo, valanghe AINEVA/EAWS, portate fluviali, chiusure stagionali di impianti e rifugi, allerte protezione civile) che cambiano l'agibilità di un luogo da un giorno all'altro;
- **Calendari eventi** (gare, raduni, manifestazioni, aperture stagionali, escursioni guidate) pubblicati su mille canali diversi.

Il risultato è che la domanda apparentemente semplice — *"cosa posso fare questo weekend, vicino a me, compatibile con il mio livello e con le condizioni attuali?"* — richiede oggi ore di ricerca incrociata e produce comunque risposte incerte.

**Cosa risolve LocalMind.** Trasformiamo questo ecosistema in un **grafo pesato e navigabile dall'AI** dove un sentiero, un'attività, un impianto, un evento, un punto d'interesse, una stagione e un livello di difficoltà sono **nodi tipizzati** collegati da **relazioni pesate** (es. *un sentiero È_PRATICABILE_IN una stagione con peso 0.9*, *parte da un rifugio*, *è adatto a un livello di difficoltà*, *attraversa un'area protetta*, *è collegato a un evento*). Su questo grafo, l'AI in GraphRAG combina **navigazione delle relazioni** e **ricerca semantica** (Qdrant) per rispondere a domande complesse e far emergere collegamenti non evidenti.

Il valore differenziale rispetto ai portali esistenti:

| Dimensione | Portali outdoor esistenti | LocalMind Sport & outdoor |
|------------|---------------------------|---------------------------|
| Proprietà dei dati | Silos chiuso, lock-in | Self-hosted, local-first, dati dell'utente/comunità |
| Privacy tracce/posizione | Dati inviati al cloud del fornitore | Tracce GPS restano in locale; AI Ollama di default |
| Interrogazione | Filtri rigidi, parole chiave | Domande in linguaggio naturale su grafo (GraphRAG) |
| Collegamenti impliciti | Assenti o opachi | Il grafo fa emergere catene sentiero→rifugio→evento→stagione |
| Estensibilità | Chiusa | Plugin PF4J + marketplace, nuovi tipi di nodo per disciplina |
| Costo | Freemium/abbonamento | Open source puro |
| Contesto territoriale | Isolato dall'altra conoscenza | Integrato col grafo consumer (turismo, eventi, esperienze) |

**Valore per i diversi attori.** Per il singolo praticante: trovare l'attività giusta riducendo il rischio (la difficoltà reale e le condizioni stagionali sono di prima classe nel modello, non note a margine). Per le comunità locali (proloco, club alpini, ASD, federazioni): pubblicare e curare la conoscenza del proprio territorio senza dipendere da piattaforme esterne. Per gli enti (comuni, parchi, consorzi turistici): mantenere un catalogo autorevole, aggiornato e auto-ospitato dell'offerta outdoor. Per l'ecosistema LocalMind: il verticale Sport & outdoor è il banco di prova ideale del motore a grafo perché è intrinsecamente **relazionale** (tutto è connesso a luogo, tempo e abilità) e **dinamico** (le condizioni cambiano), quindi mette alla prova sia la parte strutturale (MySQL) sia quella semantica (Qdrant) sia l'ingestione di fonti vive.

**Cosa NON è.** Non è un GPS da navigazione turn-by-turn in tempo reale, non sostituisce i bollettini valanghe ufficiali (li integra e cita), non è un social network di fitness. È il **livello di conoscenza** che sta sotto e che rende interrogabile, sicuro e personalizzato l'accesso all'outdoor.

## 2. Personas & utenti target

| Persona | Profilo | Obiettivo principale | Domande tipiche al sistema |
|---------|---------|----------------------|-----------------------------|
| **Escursionista occasionale** ("Giulia") | Famiglia, principiante, cerca gite facili e sicure | Trovare percorsi a bassa difficoltà adatti ai bambini e percorribili oggi | "Sentieri T1 con fontana e adatti a un passeggino entro 30 km da casa?" |
| **Hiker/alpinista esperto** ("Marco") | Esperto, valuta esposizione, stagionalità, attrezzatura | Pianificare uscite impegnative valutando rischio e condizioni | "Vie ferrate EEA aperte a luglio sopra i 2000 m con avvicinamento breve?" |
| **Trail runner / ciclista MTB** ("Sara") | Atleta, interessata a dislivello, fondo, anello | Allenamenti e percorsi su distanza/dislivello target | "Anelli MTB di 40 km, scala S2, asciutti in autunno, vicino a un punto ristoro?" |
| **Organizzatore di eventi sportivi** ("Luca") | ASD / proloco, pubblica gare e raduni | Censire e promuovere eventi legati a percorsi e impianti | (lato editor) Creare evento "Gara di trail" collegandolo a un sentiero e a una stagione |
| **Gestore di impianto/rifugio** ("Anna") | Gestisce impianto risalita, palestra di roccia, rifugio, piscina | Mantenere aperture stagionali, servizi, collegamenti ai percorsi | (lato editor) Aggiornare aperture, servizi, sentieri collegati |
| **Curatore/moderatore di comunità** ("CAI locale") | Club alpino, gruppo escursionistico | Validare contributi, garantire accuratezza e sicurezza | Moderare segnalazioni, aggiornare condizioni, correggere difficoltà |
| **Ente territoriale** ("Comune / Parco") | Pubblica amministrazione, consorzio turistico | Catalogo autorevole self-hosted dell'offerta outdoor | Pubblicare la rete sentieristica ufficiale, monitorare chiusure |
| **Soccorso/prevenzione** ("Volontario CNSAS") | Profilo sicurezza | Capire afflusso e criticità di percorsi rischiosi | "Quali percorsi EE con esposizione elevata hanno avuto segnalazioni recenti?" |

I primi tre sono i **consumatori** del grafo (fruiscono di ricerca e GraphRAG); gli ultimi cinque sono prevalentemente **produttori/curatori** (contribuiscono nodi e archi, moderano, mantengono dati dinamici). Il sistema deve servire entrambi i lati con la stessa qualità: senza produttori il grafo si svuota, senza consumatori non genera valore.

## 3. Requisiti in input

Questa sezione definisce, in modo esaustivo, **cosa deve poter entrare nel sistema** affinché il grafo Sport & outdoor sia ricco e affidabile. Gli input sono raggruppati per natura. Ogni input deve essere **validato al confine** (schema-based, fail-fast) e, ove possibile, normalizzato verso vocabolari standard.

### 3.1 Input geospaziali (geometria e tracciato)

- **Tracce GPX/KML/GeoJSON/TCX**: file caricati dall'utente o importati da fonte esterna. Devono contenere almeno la sequenza di punti (lat, lon); opzionalmente quota, timestamp, frequenza cardiaca, cadenza (per dati di allenamento). Validazione: file ben formato, coordinate plausibili (range WGS84), lunghezza minima, no salti impossibili tra punti consecutivi.
- **Geometria del percorso**: linea (LineString) per sentieri/itinerari, punto per POI/impianti, poligono per aree (parchi, zone protette, falesie, comprensori). Sistema di riferimento normalizzato a **WGS84 (EPSG:4326)**.
- **Profilo altimetrico**: derivato dalla traccia o da DEM; necessario per calcolare dislivello positivo/negativo, pendenza media e massima.
- **Metriche derivate**: lunghezza, dislivello +/-, quota minima/massima, pendenza, esposizione prevalente (versante), tempo di percorrenza stimato.

### 3.2 Input descrittivi dell'attività e del luogo

- **Anagrafica del nodo**: nome, descrizione (bilingue IT/EN), tipo di attività (escursionismo, trail, MTB, arrampicata, sci alpinismo/fondo/pista, vie ferrate, kayak/canoa, SUP, parapendio, nuoto in acque libere, equitazione, speleologia, ecc.), categoria del punto (sentiero, impianto, rifugio, falesia, palestra, piscina, area attrezzata, parcheggio di partenza).
- **Punto di partenza/arrivo, waypoint notevoli**: rifugi, fonti d'acqua, bivi, punti panoramici, passaggi chiave (guado, cengia, tratto attrezzato).
- **Servizi e dotazioni**: parcheggio, trasporto pubblico, acqua potabile, ristoro, noleggio, accessibilità (passeggino/carrozzina), copertura segnale, possibilità per cani.
- **Riferimenti ufficiali**: numero/segnavia CAI, sigla del sentiero, codice catastale del comprensorio, link a fonte autoritativa.

### 3.3 Input di difficoltà (variabile critica)

La difficoltà deve essere modellata con **scale standard riconosciute**, non con etichette libere, perché è un fattore di sicurezza. Il sistema deve accettare e mappare:

| Disciplina | Scala standard supportata | Esempio valori |
|-----------|---------------------------|----------------|
| Escursionismo | Scala CAI | T (Turistico), E (Escursionistico), EE (Esperti), EEA (con Attrezzatura) |
| Escursionismo/alpino | SAC Hiking Scale (OSM \`sac_scale\`) | T1 → T6 |
| MTB | MTB scale (OSM \`mtb:scale\`) | S0 → S5 |
| Sci di pista | Difficoltà piste (OSM \`piste:difficulty\`) | novice, easy, intermediate, advanced, expert |
| Arrampicata | UIAA / Francese / Yosemite (YDS) | UIAA I–XII, Fr 3a–9c, YDS 5.x |
| Vie ferrate | Scala ferrate | F (facile) → EX (estremamente difficile) |
| Sci alpinismo | Scala Blachère / Toponeige | F, PD, AD, D, TD… |

- **Fattori che concorrono alla difficoltà** (da catturare separatamente per consentire ricalcolo e spiegabilità): esposizione/esposizione a vuoti, qualità del fondo, segnaletica/visibilità traccia (OSM \`trail_visibility\`), presenza di passaggi attrezzati, lunghezza e dislivello, quota, necessità di attrezzatura/competenze specifiche.
- **Validazione**: ogni valore di difficoltà deve appartenere a una scala dichiarata; conversioni tra scale gestite da tabelle di mapping versionate; in assenza di scala ufficiale, etichetta "non classificato" esplicita (mai un default silenzioso).

### 3.4 Input stagionali, temporali e di condizione (variabile critica)

- **Finestra stagionale praticabile**: mesi/periodi consigliati e sconsigliati (es. percorso innevato in inverno, guado impraticabile a fine primavera per disgelo).
- **Condizioni dinamiche**: stato attuale (aperto/chiuso, innevato, fangoso, allagato), bollettino meteo locale, bollettino valanghe (scala EAWS/AINEVA 1–5), portata fluviale, allerte protezione civile.
- **Aperture/chiusure**: calendario di apertura di impianti, rifugi, comprensori; chiusure per nidificazione/caccia/lavori; orari.
- **Eventi temporizzati**: data/ora/durata di gare, raduni, escursioni guidate, manifestazioni, con eventuale ricorrenza.
- **Validazione**: date in formato ISO 8601, fusi orari espliciti, coerenza intervallo (inizio ≤ fine), ricorrenze espresse con regole verificabili.

### 3.5 Input community-driven (contributi e feedback)

- **Creazione/modifica nodi e archi** da parte di utenti registrati (editor), con tracciamento autore e versione.
- **Recensioni, valutazioni (rating), segnalazioni**: voto numerico, testo libero, foto, segnalazione di condizioni recenti ("ieri trovato ghiaccio al passaggio chiave"), report di problemi (frana, segnaletica mancante).
- **Voti di qualità/affidabilità**: utili per il ranking emergente e per il peso degli archi.
- **Validazione e sicurezza**: sanitizzazione del testo (anti-XSS), moderazione/curatela prima della pubblicazione per i campi che impattano la sicurezza (difficoltà, condizioni), rate limiting sui contributi, controllo coordinate plausibili.

### 3.6 Input documentali e non strutturati

- **Documenti** (PDF di relazioni di gita, guide, regolamenti gara, schede tecniche): ingeriti tramite il dominio \`document\` esistente (Tika + OCR), chunking ed embedding su Qdrant.
- **Pagine web / feed** di portali, blog, calendari eventi: tramite connettori/plugin.
- **Email e messaggi** (dominio \`email\`/\`messaging\`): es. comunicazioni di chiusura impianti, conferme eventi.

### 3.7 Requisiti non funzionali sugli input

- **Bilinguismo IT/EN** su tutti i campi descrittivi e su tutte le enum (difficoltà, stagione, tipo attività) — requisito di progetto.
- **Privacy**: tracce GPS e dati di posizione personali restano in locale; nessun invio a servizi esterni senza consenso esplicito.
- **Provenienza e licenza**: ogni input importato deve registrare fonte e licenza (es. OSM ODbL) per conformità.
- **Idempotenza dell'ingestione**: reimportare la stessa fonte non deve duplicare nodi (deduplica per identificatore esterno/geometria).

## 4. Flusso dell'attività (step-by-step)

Descriviamo i flussi principali end-to-end. Sono pensati per guidare direttamente l'implementazione (controller → use case → service → port → adapter, secondo l'architettura esagonale).

### 4.1 Flusso A — Ingestione di un percorso (da GPX o da fonte esterna)

1. **Acquisizione**: l'editor carica un file GPX dalla UI (feature \`knowledge\`/nuova feature \`outdoor\`) oppure un connettore importa da OSM/portale. Il controller riceve il file e lo passa allo use case di ingestione.
2. **Validazione al confine**: parsing del file, verifica schema, coordinate plausibili, lunghezza minima. Fail-fast con messaggio bilingue in caso di errore.
3. **Normalizzazione geometrica**: conversione a WGS84, semplificazione della traccia (Douglas-Peucker) per la visualizzazione, calcolo profilo altimetrico da DEM se mancante.
4. **Calcolo metriche derivate**: lunghezza, dislivello +/-, pendenza, quota min/max, tempo stimato, esposizione.
5. **Inferenza/normalizzazione della difficoltà**: se la fonte fornisce un valore (es. OSM \`sac_scale\`), si mappa sulla scala canonica; altrimenti si propone una stima dai fattori (dislivello, quota, esposizione) marcata come "stimata, da validare".
6. **Estrazione semantica**: la descrizione testuale viene chunked ed embeddata su Qdrant; l'AI locale (Ollama) può estrarre entità (rifugi citati, passaggi chiave, stagionalità menzionata) per arricchire i nodi.
7. **Costruzione di nodi e archi**: creazione del nodo \`Sentiero/Percorso\`, dei nodi \`PuntoDiInteresse\`/\`Rifugio\`/\`PuntoDiPartenza\` collegati, e degli archi pesati (\`PARTE_DA\`, \`ATTRAVERSA\`, \`HA_DIFFICOLTÀ\`, \`PRATICABILE_IN\`).
8. **Deduplica**: confronto con nodi esistenti per identificatore esterno o prossimità geometrica; in caso di match, merge/aggiornamento invece di duplicazione (pattern immutabile: nuova versione del nodo).
9. **Persistenza**: struttura del grafo su MySQL (tabelle nodi/archi), vettori su Qdrant, file/metadati tramite dominio \`document\`.
10. **Indicizzazione e disponibilità**: il percorso diventa interrogabile via ricerca e GraphRAG; pubblicazione soggetta a stato (bozza → in revisione → pubblicato) secondo moderazione.

### 4.2 Flusso B — Ricerca e scoperta da parte del consumatore (GraphRAG)

1. **Domanda in linguaggio naturale** dalla UI: es. *"Cosa posso fare domenica vicino a Bergamo, livello facile, se non ha piovuto?"*.
2. **Interpretazione**: l'AI estrae vincoli (luogo+raggio, finestra temporale, difficoltà, condizione meteo) e li traduce in **filtri sul grafo** + **query semantica**.
3. **Recupero ibrido (GraphRAG)**:
   - parte **strutturale**: selezione candidati su MySQL filtrando per area geografica, difficoltà compatibile, stagione/condizioni attuali, archi di praticabilità;
   - parte **semantica**: ricerca su Qdrant sui chunk descrittivi per intento e sfumature ("adatto ai bambini", "panoramico");
   - **espansione sul grafo**: dai candidati si esplorano i vicini (rifugi, eventi nello stesso periodo, percorsi collegati) seguendo gli archi pesati.
4. **Ranking**: combinazione di pertinenza semantica, peso degli archi, ranking emergente community (voti/affidabilità), prossimità e compatibilità con i vincoli.
5. **Verifica condizioni dinamiche**: per i top candidati si controllano dati vivi (meteo, valanghe, aperture); i percorsi non agibili oggi vengono declassati o segnalati.
6. **Risposta generata con citazioni**: l'AI risponde elencando le proposte, **citando i nodi e i percorsi del grafo usati** (sentiero X → rifugio Y → praticabile in primavera) e spiegando il perché (es. "scelto perché T1, asciutto, con fontana"). Trasparenza = fiducia.
7. **Navigazione interattiva**: l'utente esplora il grafo a partire da una proposta (espansione progressiva: vicini, eventi correlati, alternative più facili/difficili), applica filtri per tipo di nodo/relazione.

### 4.3 Flusso C — Contributo e moderazione community

1. **Proposta di contributo**: l'utente crea/modifica un nodo (nuovo sentiero, evento, segnalazione di condizione) o lascia una recensione/voto. Input validato e sanitizzato.
2. **Classificazione del contributo**: contributi che impattano la **sicurezza** (difficoltà, condizioni di pericolo, chiusure) entrano in coda di moderazione; contributi minori possono essere pubblicati con curatela leggera.
3. **Revisione**: un curatore (es. CAI locale) approva, corregge o respinge, con motivazione. Ogni cambiamento è versionato (storia immutabile).
4. **Aggiornamento del grafo e dei pesi**: l'approvazione crea/aggiorna nodi e archi; i voti e l'affidabilità ricalcolano i pesi degli archi e il ranking emergente.
5. **Feedback all'autore** e riconoscimento del contributo (reputazione), per alimentare il circolo virtuoso community-driven.

### 4.4 Flusso D — Aggiornamento dei dati dinamici (condizioni)

1. **Polling/ricezione** da connettori (meteo, valanghe, aperture impianti, allerte) tramite il dominio \`automation\` (job schedulati) o webhook/\`messaging\`.
2. **Normalizzazione** del dato dinamico verso le enum di condizione (scala valanghe 1–5, stato aperto/chiuso, livello allerta).
3. **Aggiornamento degli archi \`PRATICABILE_IN\`/\`HA_CONDIZIONE\`** con timestamp e validità (i dati dinamici hanno scadenza: un meteo di 3 giorni fa non deve pesare come uno odierno).
4. **Ricalcolo dei pesi** dipendenti dalle condizioni e invalidazione delle cache di ricerca.
5. **Notifiche** (opzionali) agli utenti interessati a un percorso che cambia stato (es. apertura impianto, miglioramento condizioni).

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa l'infrastruttura del motore a grafo universale (tabelle nodi/archi su MySQL + vettori su Qdrant). Qui si definisce lo **schema di dominio** Sport & outdoor: tipi di nodo, tipi di arco e criteri di peso. Tutti i tipi sono estendibili via schema modulare e plugin.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi chiave |
|--------------|-------------|------------------|
| \`Sentiero\`/\`Percorso\` | Itinerario lineare percorribile | geometria, lunghezza, dislivello+/-, quota min/max, segnavia, tipo attività |
| \`Itinerario\`/\`Anello\` | Composizione di più percorsi/tappe | tappe ordinate, durata totale, anello sì/no |
| \`Attività\` | Disciplina praticabile (escursionismo, MTB, arrampicata…) | categoria, requisiti, attrezzatura |
| \`PuntoDiInteresse (POI)\` | Punto notevole lungo o presso un percorso | tipo (panoramico, fonte, guado), coordinate |
| \`Impianto\` | Impianto sportivo/risalita/struttura | tipo, capacità, orari, aperture stagionali |
| \`Rifugio\`/\`Bivacco\` | Punto di appoggio e ristoro | posti letto, servizi, periodo apertura, contatti |
| \`Falesia\`/\`SettoreArrampicata\` | Area di arrampicata | numero vie, esposizione, gradi presenti |
| \`EventoSportivo\` | Gara, raduno, escursione guidata | data/ora, disciplina, iscrizioni, organizzatore |
| \`Difficoltà\` | Livello su una scala standard | scala (CAI/SAC/MTB/UIAA…), valore, fattori |
| \`Stagione\`/\`FinestraTemporale\` | Periodo di praticabilità | mesi, condizioni tipiche |
| \`Condizione\` | Stato dinamico corrente | meteo, neve, valanghe (1–5), aperto/chiuso, validità |
| \`Luogo\`/\`Area\` | Località, comune, valle, area protetta, comprensorio | confini, gerarchia amministrativa |
| \`PuntoDiPartenza\`/\`Parcheggio\` | Accesso al percorso | coordinate, trasporto pubblico, capienza |
| \`Servizio\` | Dotazione (acqua, ristoro, noleggio) | tipo, disponibilità |
| \`Organizzatore\`/\`Gestore\` | ASD, proloco, club, gestore impianto | tipo ente, contatti |
| \`Persona\`/\`Contributore\` | Utente che contribuisce o pratica | reputazione, ruolo |
| \`Recensione\`/\`Segnalazione\` | Feedback su un nodo | rating, testo, foto, data |
| \`Documento\` | Relazione, guida, regolamento (ponte col dominio \`document\`) | riferimento, embedding |

### 5.2 Tipi di relazione (archi)

| Relazione (arco) | Da → A | Significato | Pesata da |
|------------------|--------|-------------|-----------|
| \`HA_DIFFICOLTÀ\` | Sentiero → Difficoltà | Classificazione di difficoltà | affidabilità della classificazione, consenso community |
| \`PRATICABILE_IN\` | Sentiero/Attività → Stagione | Finestra stagionale consigliata | quanto è netta la stagionalità, conferme recenti |
| \`HA_CONDIZIONE\` | Sentiero/Impianto → Condizione | Stato dinamico corrente | freschezza del dato, autorevolezza fonte |
| \`PARTE_DA\` | Sentiero → PuntoDiPartenza | Punto di accesso | — (strutturale) |
| \`ATTRAVERSA\`/\`PASSA_PER\` | Sentiero → POI/Luogo/Area | Percorre/tocca | rilevanza del POI sul percorso |
| \`COLLEGA\` | Sentiero ↔ Sentiero/Rifugio | Connessione di rete | frequenza d'uso della connessione |
| \`SI_PRATICA_CON\` | Sentiero → Attività | Attività ammesse sul percorso | idoneità (es. MTB sì/no) |
| \`OSPITA_EVENTO\` | Sentiero/Impianto/Luogo → EventoSportivo | Sede di un evento | imminenza, importanza evento |
| \`OFFRE_SERVIZIO\` | Rifugio/Impianto → Servizio | Servizi disponibili | affidabilità del dato |
| \`VICINO_A\` | Nodo ↔ Nodo | Prossimità geografica | inverso della distanza |
| \`ADATTO_A\` | Sentiero → Persona/Livello/Persona-tipo | Idoneità a profilo/abilità | match abilità↔difficoltà |
| \`SIMILE_A\` | Sentiero ↔ Sentiero | Similarità (semantica + caratteristiche) | similarità embedding + metriche |
| \`GESTITO_DA\` | Impianto/Rifugio/Evento → Gestore | Responsabilità | — (strutturale) |
| \`RECENSITO_DA\` | Nodo → Recensione/Persona | Feedback ricevuto | rating, reputazione autore |
| \`DESCRITTO_IN\` | Nodo → Documento | Documentazione collegata | pertinenza semantica |
| \`FA_PARTE_DI\` | Sentiero → Itinerario/Rete | Composizione | — (strutturale) |

### 5.3 Criteri per il peso degli archi

Il peso (0–1, o score normalizzato) è ciò che rende il grafo "navigabile con intelligenza". Per Sport & outdoor i fattori chiave sono:

- **Freschezza temporale** (decisivo per \`HA_CONDIZIONE\`/\`PRATICABILE_IN\`): un dato di condizione decade nel tempo; il peso scende con l'età del dato. Una segnalazione di ghiaccio di ieri pesa molto più di una di tre settimane fa.
- **Affidabilità della fonte**: dato ufficiale (ente, bollettino) > contributo di curatore esperto > contributo utente non verificato. Le scale di difficoltà ufficiali pesano più delle stime automatiche.
- **Consenso/affidabilità community**: numero e concordanza di voti/segnalazioni che confermano l'arco (es. molti concordano che il sentiero è T2 → peso alto su \`HA_DIFFICOLTÀ\`).
- **Frequenza d'uso/popolarità**: archi attraversati spesso (\`COLLEGA\`, percorsi popolari) acquistano peso, ma bilanciato per non penalizzare gioielli poco noti.
- **Forza della relazione fisica**: per \`VICINO_A\`, peso inversamente proporzionale alla distanza; per \`ATTRAVERSA\`, rilevanza del POI rispetto al percorso.
- **Compatibilità di profilo** (per \`ADATTO_A\`): match tra livello dichiarato dall'utente e difficoltà del percorso.
- **Similarità ibrida** (per \`SIMILE_A\`): combinazione di similarità degli embedding (Qdrant) e vicinanza delle metriche (dislivello, difficoltà, durata).

I pesi sono **ricalcolabili** (job in \`automation\`) e **spiegabili**: ogni peso deve poter essere scomposto nei suoi fattori per la trasparenza delle risposte AI.

## 6. Fonti dati & connettori (ingestione)

| Fonte | Tipo dato | Connettore / meccanismo | Note |
|-------|-----------|--------------------------|------|
| **OpenStreetMap** (Overpass API) | Sentieri, POI, tag \`sac_scale\`/\`mtb:scale\`/\`piste:difficulty\`/\`trail_visibility\` | Plugin connettore PF4J + job \`automation\` | Licenza ODbL da tracciare; deduplica per OSM id |
| **File GPX/KML/GeoJSON/TCX** | Tracce, waypoint, allenamenti | Upload UI → use case ingestione | Restano in locale (privacy) |
| **DEM / dati altimetrici** | Quote, profilo altimetrico | Servizio locale/raster | Per calcolo dislivello/pendenza |
| **Portali outdoor** (ove API/feed disponibili) | Percorsi, recensioni | Plugin connettore dedicato | Rispetto ToS e licenze |
| **Bollettini meteo** | Previsioni locali | Connettore meteo (job schedulato) | Dato dinamico con scadenza |
| **Bollettini valanghe** (EAWS/AINEVA) | Pericolo valanghe 1–5 | Connettore dedicato | Citare fonte ufficiale; non sostituirla |
| **Aperture impianti/rifugi** | Calendari, orari, chiusure | Connettore + contributi gestori | Misto automatico/manuale |
| **Calendari eventi** (ICS, federazioni, proloco) | Gare, raduni, escursioni guidate | Dominio \`calendar\` + connettori ICS | Eventi temporizzati/ricorrenti |
| **Documenti** (PDF guide, relazioni, regolamenti) | Testo non strutturato | Dominio \`document\` (Tika + OCR) → Qdrant | Chunking + embedding |
| **Email / messaggi** | Comunicazioni di servizio | Domini \`email\`/\`messaging\` | Es. avvisi chiusura |
| **Contributi community** | Nodi, archi, recensioni, segnalazioni | UI editor + API | Moderazione e versioning |

Linee guida di ingestione: ogni connettore è un **plugin PF4J** (riuso del marketplace), espone metadati di provenienza/licenza, è **idempotente** (no duplicati) e rispetta i vincoli local-first (i dati sensibili non escono senza consenso). I job ricorrenti si appoggiano al dominio \`automation\`; i flussi event-driven al sistema di domain events.

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

### 7.1 MVP (prima release del verticale)

| # | Funzionalità | Tipo | Moduli coinvolti |
|---|--------------|------|------------------|
| M1 | Schema di dominio outdoor sul motore a grafo (tipi nodo/arco della sez. 5) come schema modulare | CREARE | \`knowledge\`, MySQL (Flyway), Qdrant |
| M2 | Enum bilingui IT/EN per difficoltà (CAI/SAC/MTB), stagioni, tipi attività, condizioni | CREARE | \`knowledge\`, API, frontend i18n |
| M3 | Ingestione GPX/GeoJSON con validazione, calcolo metriche e creazione nodi/archi | CREARE | nuovo dominio/feature \`outdoor\`, \`document\` |
| M4 | Connettore OSM (Overpass) base con deduplica e tracciamento licenza | CREARE | plugin PF4J, \`automation\` |
| M5 | API CRUD nodi/archi outdoor + query di vicinato e filtri (difficoltà, stagione, area) | CREARE | \`localmind-api\`, \`knowledge\` |
| M6 | Ricerca GraphRAG: domanda NL → filtri grafo + semantica → risposta con citazioni | SVILUPPARE (estende GraphRAG core) | \`llm\` (Ollama default), Qdrant, \`knowledge\` |
| M7 | UI feature "Outdoor": ricerca, scheda percorso (metriche, difficoltà, stagione), mappa/profilo altimetrico | CREARE | frontend Angular standalone |
| M8 | Contributi community base: creazione percorso, recensione, voto; sanitizzazione input | CREARE | \`knowledge\`, \`auth\`, API |
| M9 | Moderazione base dei contributi che impattano la sicurezza (coda + approvazione versionata) | CREARE | \`knowledge\`, \`auth\` |
| M10 | Pesi degli archi v1 (freschezza, affidabilità fonte, consenso) ricalcolabili | CREARE | \`knowledge\`, \`automation\` |

### 7.2 Evoluzioni (release successive)

| # | Funzionalità | Tipo | Note |
|---|--------------|------|------|
| E1 | Dati dinamici: connettori meteo, valanghe, aperture; archi \`HA_CONDIZIONE\` con scadenza | SVILUPPARE | dominio \`automation\`/\`messaging\` |
| E2 | Visualizzazione interattiva del grafo (nodi/archi pesati) con espansione progressiva e filtri | SVILUPPARE | frontend, parte del core grafo |
| E3 | Itinerari personalizzati generati dall'AI sul grafo (multi-tappa, vincoli abilità/stagione) | SVILUPPARE | GraphRAG + \`agent\` |
| E4 | Suggerimento collegamenti mancanti (es. percorsi simili, rifugi non collegati) | SVILUPPARE | core grafo (link prediction) |
| E5 | Conversione automatica tra scale di difficoltà + stima spiegabile dai fattori | SVILUPPARE | \`knowledge\` |
| E6 | Ranking emergente avanzato (reputazione contributori, qualità) | SVILUPPARE | \`knowledge\`, \`auth\` |
| E7 | Connettori aggiuntivi (portali, federazioni, ICS eventi, DEM ad alta risoluzione) | CREARE | plugin PF4J/marketplace |
| E8 | Notifiche su cambi di condizione/apertura per percorsi seguiti | CREARE | \`messaging\`, \`automation\` |
| E9 | Import allenamenti (TCX/HR) e analisi prestazioni personali (local-first) | SVILUPPARE | \`outdoor\`, privacy-by-design |
| E10 | Pacchetto/modulo "Sport & outdoor" installabile dal marketplace | CREARE | \`marketplace\`, \`plugin\` |
| E11 | Multimodalità: riconoscimento da foto (es. segnaletica, condizioni) via Ollama multimodale | SVILUPPARE | \`llm\` (adapter multimodale) |

### 7.3 Manutenzione continua

- Aggiornamento delle **tabelle di mapping** tra scale di difficoltà (versionate).
- **Freschezza dei dati dinamici**: monitoraggio scadenza, purge dati obsoleti, healthcheck dei connettori.
- **Deduplica e qualità del grafo**: job periodici di rilevamento duplicati e archi orfani.
- **Moderazione**: gestione coda, audit delle modifiche, ripristino versioni.
- **i18n**: mantenimento traduzioni IT/EN di enum e contenuti.
- **Compatibilità licenze** delle fonti importate.

## 8. Casi d'uso AI / GraphRAG

- **Scoperta condizionata**: *"Cosa posso fare domenica entro 40 km, livello E, se le condizioni sono buone?"* → filtri grafo (area, difficoltà, condizioni dinamiche) + semantica + ranking, risposta con citazioni dei nodi.
- **Pianificazione itinerario multi-tappa**: *"Trek di 3 giorni con rifugi, dislivello max 1000 m/giorno, fattibile a settembre"* → l'AI compone un itinerario navigando archi \`COLLEGA\`/\`PARTE_DA\`/\`PRATICABILE_IN\`.
- **Adeguamento al profilo**: *"Sono principiante con due bambini: alternativa più facile a questo percorso?"* → segue \`SIMILE_A\` + \`ADATTO_A\` declassando difficoltà.
- **Spiegazione del rischio**: *"Perché questo sentiero è EE?"* → l'AI scompone l'arco \`HA_DIFFICOLTÀ\` nei fattori (esposizione, fondo, attrezzatura) citando la fonte.
- **Collegamenti non evidenti**: far emergere che un evento, un rifugio appena aperto e un percorso poco noto convergono nello stesso weekend e area.
- **Sintesi delle condizioni**: aggregare segnalazioni community recenti + bollettini in un quadro sintetico aggiornato di un percorso.
- **Suggerimento collegamenti mancanti**: proporre connessioni di rete tra sentieri o associazioni percorso↔evento non ancora modellate.

Tutti i casi rispettano il principio di **risposta con citazione dei nodi/percorsi** e funzionano con **Ollama in locale** di default.

## 9. KPI & metriche di successo

| Categoria | KPI | Obiettivo indicativo |
|-----------|-----|----------------------|
| Copertura grafo | N. nodi \`Sentiero\`/\`POI\`/\`Evento\` per area | Crescita costante; ≥ soglia minima per "area servita" |
| Qualità difficoltà | % percorsi con difficoltà su scala standard (non "stimata") | > 80% sui percorsi pubblicati |
| Freschezza condizioni | Età media dei dati dinamici attivi | < 24–48h per meteo/valanghe |
| Rilevanza ricerca | Tasso di click/uso delle prime 3 proposte GraphRAG | Alto e in crescita |
| Spiegabilità | % risposte AI con citazioni di nodi/percorsi | ~100% |
| Community | N. contributi/recensioni; % contributi approvati | Trend positivo; bassa percentuale di respinti per errore |
| Affidabilità | Discrepanza tra difficoltà community e reale (segnalazioni di "più difficile del previsto") | In calo |
| Privacy | % flussi che restano local-first | 100% per dati di posizione personali |
| Estensibilità | N. connettori/plugin attivi dal marketplace | In crescita |
| Performance | Latenza query GraphRAG su grafo di dimensione target | Entro soglie UX accettabili |

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---------|---------|-------------|
| **Dato di difficoltà errato** (sicurezza) | Alto — rischio per l'incolumità | Scale standard obbligatorie, moderazione dei campi sicurezza, etichetta "stimata" esplicita, citazione fonte |
| **Condizioni obsolete presentate come attuali** | Alto | Scadenza esplicita dei dati dinamici, decadimento del peso, indicazione data del dato in UI |
| **Performance del grafo su MySQL** (no Neo4j) | Medio | Indici geo/relazionali, query a profondità limitata, pre-aggregazioni/cache, GraphRAG con espansione controllata |
| **Licenze delle fonti** (OSM ODbL, portali) | Medio-legale | Tracciamento provenienza/licenza per nodo, rispetto ToS, attribuzioni in UI |
| **Spam/contributi inaffidabili** | Medio | Moderazione, reputazione, rate limiting, sanitizzazione, ranking per affidabilità |
| **Privacy dei dati di posizione** | Alto | Local-first di default, nessun invio cloud senza consenso, Ollama locale |
| **Frammentazione delle scale di difficoltà** | Medio | Tabelle di mapping versionate, conversioni esplicite e spiegabili |
| **Dipendenza da connettori esterni instabili** | Medio | Plugin isolati, healthcheck, degradazione elegante (il grafo resta utile anche senza dati vivi) |
| **Allucinazioni AI su sicurezza** | Alto | Risposte ancorate al grafo con citazioni; disclaimer; mai sostituire bollettini ufficiali |

## 11. Manutenzione & evoluzione

- **Schema modulare versionato**: i tipi di nodo/arco outdoor evolvono con migrazioni Flyway (una query per file) e schema versioning, senza rompere il grafo esistente.
- **Connettori come plugin**: nuove fonti si aggiungono via PF4J/marketplace senza toccare il core; ogni plugin ha proprio ciclo di vita e versione.
- **Pipeline dati vivi**: monitoraggio continuo di freschezza e salute dei connettori; purge automatica dei dati scaduti; ricalcolo periodico dei pesi (\`automation\`).
- **Curatela community**: processi e strumenti di moderazione, audit delle modifiche, gestione reputazione; coinvolgimento di curatori autorevoli (CAI, parchi).
- **Qualità del grafo**: job di deduplica, rilevamento archi orfani/incoerenti, validazione geometrica.
- **i18n e documentazione**: aggiornamento costante di traduzioni IT/EN e della documentazione bilingue (requisito di progetto); ogni sviluppo tracciato nella cartella \`Sviluppi/\` con nomenclatura datata.
- **Estensione a nuove discipline**: aggiunta di scale di difficoltà e tipi attività (es. nuove discipline) tramite estensione delle enum bilingui e dello schema, senza riscrittura.
- **Roadmap evolutiva**: dai dati statici (MVP) ai dati dinamici (E1), alla visualizzazione interattiva (E2), agli itinerari AI (E3) e al modulo marketplace (E10), in fasi incrementali coerenti con la roadmap GSD.

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo esistente | Ruolo nel verticale Sport & outdoor |
|------------------|-------------------------------------|
| \`knowledge\` | **Cuore del verticale**: ospita lo schema di dominio (nodi/archi outdoor) sul motore a grafo universale; punto di estensione principale |
| \`llm\` (Ollama default) | Motore GraphRAG, estrazione entità da testo, generazione risposte con citazioni, multimodalità (foto) — locale di default, cloud opzionale |
| \`document\` | Ingestione di guide/relazioni/regolamenti (Tika + OCR), chunking ed embedding su Qdrant per la parte semantica |
| Qdrant (vector store) | Ricerca semantica e similarità (\`SIMILE_A\`) su descrizioni e documenti |
| MySQL + Flyway | Struttura del grafo (nodi/archi/pesi), metriche, enum; migrazioni con una sola query per file |
| \`automation\` | Job schedulati: ingestione OSM, polling dati dinamici (meteo/valanghe/aperture), ricalcolo pesi, purge dati scaduti |
| \`calendar\` | Eventi sportivi temporizzati/ricorrenti, aperture stagionali, escursioni guidate (import ICS) |
| \`messaging\` | Notifiche su cambi di condizione/apertura per percorsi seguiti; ingestione avvisi event-driven |
| \`email\` | Ingestione comunicazioni di servizio (chiusure, conferme eventi) |
| \`mcp\` | Esposizione di tool MCP per interrogare il grafo outdoor da agenti/strumenti esterni |
| \`agent\` | Agenti per pianificazione itinerari multi-tappa e workflow di scoperta |
| \`auth\` | Identità dei contributori, ruoli editor/curatore/moderatore, reputazione, privacy |
| \`plugin\` (PF4J) + \`marketplace\` | Connettori di fonte come plugin installabili; pacchetto "Sport & outdoor" distribuibile dal marketplace |
| \`finetuning\` | (Evoluzione) adattamento di modelli locali alla terminologia outdoor/territoriale |
| Frontend Angular 21 | Nuova feature lazy-loaded "outdoor": ricerca, schede percorso, mappa/profilo altimetrico, visualizzazione grafo, editor contributi — standalone components, Signals, i18n IT/EN |

**Vincoli rispettati end-to-end**: local-first e self-hostable (tracce e dati di posizione restano in locale), **AI Ollama di default** (cloud opzionale e con consenso), riuso di **MySQL + Qdrant** senza introdurre Neo4j, **privacy** dei dati (nessun invio esterno senza consenso esplicito), estensibilità via **plugin PF4J/marketplace**, **bilinguismo IT/EN** di UI, documentazione ed enum, e migrazioni **Flyway con una sola query** per file. Il verticale Sport & outdoor non riscrive nulla: **estende** il motore a grafo universale aggiungendo tipi di nodo, relazioni, connettori e UI di dominio.
`;
