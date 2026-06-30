export const content = `# Eventi & spettacoli

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo documento descrive l'ambito di estensione **"Eventi & spettacoli"** (gruppo: consumer) all'interno della visione di LocalMind come *motore di knowledge graph universale*. L'obiettivo è trasformare il flusso disordinato di informazioni su eventi locali, concerti, festival e fiere in un **grafo pesato e navigabile dall'AI**, dove ogni evento è un nodo collegato a luoghi, date, artisti, generi e community, e dove la scoperta avviene per relazioni e non solo per ricerca testuale. L'ambito è pensato come **modulo di dominio installabile** (plugin PF4J) che riusa l'infrastruttura esistente — MySQL per la struttura del grafo, Qdrant per la semantica, Ollama come AI locale di default — senza introdurre nuove dipendenze infrastrutturali e nel pieno rispetto dei vincoli local-first, open source e bilingue IT/EN.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema concreto

Le informazioni sugli eventi locali sono oggi **frammentate, effimere e prive di relazioni**. Chi vuole scoprire "cosa fare stasera vicino a me" deve attraversare una pluralità di silos che non comunicano tra loro:

- **Frammentazione delle fonti**: locali e teatri pubblicano sui propri siti, i comuni su portali istituzionali, gli organizzatori su piattaforme di ticketing (Ticketmaster, Eventbrite, DICE), gli artisti sui social, le pro-loco su volantini PDF, i giornali locali in articoli di cronaca. Nessuna di queste fonti ha una visione d'insieme.
- **Effimerità e decadenza**: un evento ha un ciclo di vita brevissimo. Una volta passata la data, l'informazione perde valore immediato ma conserva valore *relazionale* (lo stesso artista, la stessa rassegna, lo stesso luogo torneranno). Le piattaforme attuali buttano via questa memoria storica.
- **Duplicazione e incoerenza**: lo stesso concerto compare su 5 piattaforme con titoli, orari e descrizioni leggermente diversi. L'utente non sa se sono lo stesso evento o eventi distinti.
- **Mancanza di contesto relazionale**: sapere che "stasera suona X" non dice se X è affine ai gusti dell'utente, se ha già suonato in città, se fa parte di un festival, se il luogo è raggiungibile, se ci sono eventi simili nelle vicinanze o in date alternative.
- **Scoperta povera**: la ricerca per parola chiave o il filtro per categoria non fanno emergere collegamenti non evidenti ("festival jazz nel raggio di 30 km nel weekend, con artisti simili a quelli che ho apprezzato, in luoghi all'aperto").
- **Privacy e dipendenza da terze parti**: le piattaforme commerciali profilano l'utente e monetizzano i suoi dati. Manca un'alternativa local-first che permetta scoperta personalizzata **senza inviare i gusti dell'utente al cloud**.

### 1.2 La soluzione LocalMind

LocalMind affronta il problema modellando l'ecosistema degli eventi come un **grafo di conoscenza pesato** in cui:

- ogni **evento** è un nodo di prima classe, collegato a nodi **luogo**, **data/periodo**, **artista/performer**, **organizzatore**, **genere/tema**, **rassegna/festival** e **community**;
- le **relazioni sono pesate** (un artista "headliner" pesa più di un "supporto"; un luogo che ospita ricorrentemente un genere costruisce affinità; un utente che partecipa ripetutamente a un genere rafforza l'arco di interesse);
- l'**AI naviga il grafo (GraphRAG)** combinando relazioni strutturali e similarità semantica per rispondere a domande complesse e generare raccomandazioni e itinerari spiegabili;
- tutto gira **in locale**: l'ingestione, l'embedding, il ranking e la raccomandazione non richiedono servizi esterni; i provider cloud restano opzionali.

### 1.3 Valore generato per tipo di portatore d'interesse

| Portatore d'interesse | Valore generato |
|---|---|
| **Cittadino / scopritore** | Scoperta personalizzata "cosa fare" per data, luogo, gusto; itinerari serata; nessuna profilazione esterna |
| **Turista** | Eventi del territorio integrati con POI, ristoranti e itinerari (sinergia con l'ambito turismo) |
| **Appassionato di nicchia** | Segue artisti, generi, rassegne; alert su nuove date; scopre eventi affini non evidenti |
| **Organizzatore / locale** | Pubblica e mantiene i propri eventi; vede affinità di pubblico; storicizza la propria programmazione |
| **Comune / pro-loco** | Aggrega l'offerta culturale del territorio in un unico grafo navigabile e self-hosted |
| **Community** | Contribuisce, corregge, vota e cura i contenuti; il ranking migliore emerge dal basso |

### 1.4 Perché un grafo (e non l'ennesimo calendario)

Un calendario risponde alla domanda *"cosa succede il giorno X"*. Un grafo pesato risponde a domande **multi-relazionali** che nessun calendario può evadere: *"festival con artisti simili a quelli che ho apprezzato, raggiungibili in giornata, in un luogo all'aperto, in un weekend libero, con eventi collaterali enogastronomici"*. Il valore distintivo è la capacità di **far emergere collegamenti non evidenti** — il cuore della proposta di LocalMind — e di **riusare la conoscenza storica** (artisti, luoghi, rassegne ricorrenti) che le piattaforme effimere dissipano. La memoria del grafo trasforma ogni edizione di un festival, ogni passaggio di un artista, ogni serie di un locale in segnale persistente per scoperta e raccomandazione future.

### 1.5 Differenziatori rispetto alle alternative

- **Local-first e privacy-by-design**: i gusti e la cronologia di partecipazione dell'utente restano sul nodo self-hosted; nessun invio obbligatorio al cloud.
- **Un solo motore, molti domini**: gli eventi condividono il motore a grafo con turismo, documenti enterprise, ecc. — riuso massimo, nessun verticale isolato.
- **Spiegabilità**: ogni raccomandazione cita i nodi e i percorsi usati ("ti propongo questo perché segui l'artista A, che è simile a B headliner, in un luogo che hai già visitato").
- **Estendibilità via plugin**: nuovi connettori (un nuovo portale comunale, una nuova piattaforma di ticketing) si aggiungono come plugin PF4J senza toccare il core.
- **Open source e bilingue**: nessun paywall; UI, enum e documentazione in IT/EN.

---

## 2. Personas & utenti target

| Persona | Descrizione | Bisogni primari | Modalità d'uso prevalente |
|---|---|---|---|
| **Giulia, la scopritrice urbana** | 29 anni, vive in città, esce spesso, decide all'ultimo | "Cosa faccio stasera vicino a me?"; suggerimenti rapidi e affini ai gusti | Chat AI + feed "stasera", filtri data/luogo |
| **Marco, l'appassionato di nicchia** | 41 anni, segue il jazz e la musica elettronica | Alert su nuove date di artisti/generi seguiti; scoperta affine non ovvia | Follow di artisti/generi, notifiche, esplorazione grafo |
| **Famiglia Rossi, pianificatori weekend** | Coppia con figli, pianifica il weekend in anticipo | Eventi family-friendly, all'aperto, raggiungibili, gratuiti o economici | Filtri (target, prezzo, distanza), itinerari weekend |
| **Sofia, la turista** | In visita per pochi giorni | Cosa c'è di imperdibile ora, integrato con POI e ristoranti | Itinerario AI territorio+eventi |
| **Luca, organizzatore / gestore di locale** | Gestisce la programmazione di un club | Pubblicare e mantenere eventi; capire l'affinità di pubblico | Editor eventi, dashboard affinità, storicizzazione |
| **Ufficio cultura comunale** | Ente locale, pro-loco | Aggregare l'offerta culturale del territorio, self-hosted, privacy | Connettori istituzionali, curatela, vista aggregata |
| **Anna, curatrice della community** | Volontaria, contributor attivo | Strumenti di moderazione, merge dei duplicati, correzione dati | Coda di moderazione, strumenti di entity resolution assistita |
| **Sviluppatore / contributor** | Self-hoster tecnico | Aggiungere connettori e tipi di nodo, estendere il modulo | Plugin PF4J, API grafo, documentazione |

**Segmentazione d'uso**: Giulia e Marco rappresentano la **scoperta reattiva e personalizzata** (il cuore consumer); la famiglia Rossi e Sofia la **pianificazione contestuale** (sinergia con turismo); Luca e l'ufficio cultura la **produzione e curatela dell'offerta** (lato contributore); Anna e lo sviluppatore la **sostenibilità del grafo** (qualità dati ed estensibilità).

---

## 3. Requisiti in input

Questa sezione definisce in dettaglio **cosa deve entrare nel sistema** perché il grafo eventi sia ricco, affidabile e navigabile. I requisiti sono organizzati in: dati dell'evento, entità correlate, dati comportamentali, vincoli di qualità, requisiti non funzionali.

### 3.1 Dati anagrafici dell'evento (nodo Evento)

Allineati allo standard **schema.org/Event** per garantire interoperabilità e mapping diretto dai connettori. Campi obbligatori (\`O\`), raccomandati (\`R\`), opzionali (\`F\`):

| Campo | Obblig. | Descrizione | Note di validazione |
|---|---|---|---|
| \`titolo\` | O | Nome dell'evento | Trim, normalizzazione casing, min 3 char |
| \`descrizione\` | R | Testo descrittivo | Usato per embedding semantico; lingua rilevata |
| \`dataInizio\` (start) | O | Data/ora di inizio con timezone | ISO-8601, timezone obbligatorio per de-dup |
| \`dataFine\` (end) | R | Data/ora di fine | Se assente, stimata da durata tipica per tipo |
| \`tipoEvento\` | O | Concerto, festival, fiera, spettacolo, mostra, sagra, conferenza, sportivo… | Enum tradotta IT/EN |
| \`stato\` | O | Programmato, riprogrammato, annullato, posticipato, sold-out, terminato | \`EventStatusType\` schema.org; enum IT/EN |
| \`modalita\` | O | In presenza, online, ibrido | \`EventAttendanceMode\`; enum IT/EN |
| \`luogoRef\` | O (se in presenza) | Riferimento al nodo Luogo | FK o entity resolution |
| \`organizzatoreRef\` | R | Riferimento al nodo Organizzatore | Entity resolution |
| \`performerRefs\` | R | Lista di nodi Artista/Performer | Distinzione headliner/supporto |
| \`generi\` / \`temi\` | R | Tag tematici (jazz, rock, arte contemporanea, gastronomia…) | Tassonomia controllata + tag liberi |
| \`prezzo\` / \`offers\` | R | Fascia di prezzo, gratuito, range, valuta | schema.org/Offer; normalizzazione valuta |
| \`ticketUrl\` | F | Link all'acquisto biglietti | Validazione URL |
| \`targetPubblico\` | F | Family, 18+, professionale, accessibile… | Enum IT/EN |
| \`capienza\` / \`disponibilita\` | F | Posti totali / residui | Intero ≥ 0 |
| \`immagini\` / \`media\` | F | Locandina, foto, video | URL o asset locale; alt-text per accessibilità |
| \`lingua\` | F | Lingua dell'evento | ISO 639 |
| \`accessibilita\` | F | Accessibile a disabilità, sottotitoli, LIS… | Enum multipla IT/EN |
| \`sorgente\` | O | Connettore/fonte di provenienza | Per provenance e fiducia |
| \`idEsterno\` | R | ID nativo sulla piattaforma origine | Chiave per sync/idempotenza |
| \`licenzaContenuto\` | R | Licenza/diritti del contenuto importato | Conformità open source |

### 3.2 Dati delle entità correlate

- **Luogo (Venue)**: nome, indirizzo, coordinate geo (lat/long), tipo (teatro, club, piazza, fiera, arena, all'aperto/al chiuso), capienza, accessibilità, orari, contatti. Le coordinate sono **obbligatorie** per scoperta per prossimità e itinerari.
- **Artista / Performer**: nome canonico, alias, ruolo (musicista, band, DJ, compagnia, relatore), generi, link esterni (\`sameAs\` → Wikidata/MusicBrainz per disambiguazione), biografia breve (per embedding).
- **Organizzatore**: nome, tipo (locale, comune, associazione, promoter, pro-loco), contatti, affidabilità storica.
- **Rassegna / Festival**: nome, edizione/anno, periodo, eventi figli, tema, ricorrenza.
- **Genere / Tema**: tassonomia gerarchica controllata (es. Musica → Jazz → Free jazz) con sinonimi IT/EN.

### 3.3 Dati comportamentali e di community (input per i pesi)

Questi input alimentano il **peso dinamico degli archi** e la personalizzazione, e restano **strettamente locali**:

- **Interazioni utente**: visualizzazione, salvataggio/preferito, "mi interessa", partecipazione confermata, click su biglietto.
- **Follow espliciti**: artista, genere, luogo, organizzatore, rassegna seguiti.
- **Valutazioni e recensioni**: voto numerico, recensione testuale post-evento (con embedding per affinità semantica).
- **Contributi community**: creazione/correzione di nodi, segnalazione duplicati, segnalazione dati errati, voto sui contributi altrui.
- **Feedback sulle raccomandazioni**: accettato/ignorato/nascosto, per affinare il ranking.

### 3.4 Requisiti di qualità del dato (data quality)

Poiché il grafo aggrega fonti eterogenee, la **qualità in ingresso è un requisito di prima classe**:

- **Normalizzazione**: date in UTC con timezone esplicito; valute normalizzate; indirizzi geocodificati; titoli ripuliti.
- **Entity resolution / deduplicazione**: lo stesso evento da più fonti deve collassare in **un unico nodo**, con strategia di *fuzzy matching* su titolo (similarità ~80–85%), finestra temporale (±90 minuti), prossimità geografica (~500–800 m) e match di performer. Pattern allineato alle best practice di aggregazione eventi 2026.
- **Provenance**: ogni nodo e attributo conserva la fonte (\`sorgente\`, \`idEsterno\`, timestamp di ingestione) per tracciabilità e risoluzione dei conflitti.
- **Risoluzione dei conflitti**: in caso di valori discordanti tra fonti, politica configurabile (fonte più affidabile vince, oppure più recente, oppure curatela manuale).
- **Validazione ai confini**: schema-based validation di ogni record in ingresso (vincolo di progetto); record non validi vanno in *dead-letter queue* con log dettagliato, mai scartati silenziosamente.
- **Decadenza temporale**: gli eventi passati transitano in stato \`terminato\` ma restano nel grafo come memoria storica; i pesi "freschezza" decadono nel tempo.

### 3.5 Requisiti non funzionali

| Requisito | Specifica |
|---|---|
| **Local-first** | Tutta l'ingestione, l'embedding e la raccomandazione devono funzionare offline con Ollama; nessuna dipendenza cloud obbligatoria |
| **Privacy** | Dati comportamentali e gusti utente mai inviati a terzi senza consenso esplicito |
| **i18n** | Tutte le enum (tipo evento, stato, modalità, target, accessibilità) tradotte IT/EN e instradate al frontend secondo lo switch lingua |
| **Idempotenza** | Re-ingestione della stessa fonte non crea duplicati (chiave \`sorgente\` + \`idEsterno\`) |
| **Estensibilità** | Nuove fonti come plugin PF4J senza modifica del core |
| **Persistenza** | Migrazioni Flyway con una sola query per file; UUID \`@JdbcTypeCode(SqlTypes.CHAR)\` |
| **Performance scoperta** | Query "eventi vicini nei prossimi N giorni" sotto soglia interattiva grazie a indici geo+temporali |
| **Licenza dati** | Rispetto dei termini d'uso e delle licenze delle fonti importate |

---

## 4. Flusso dell'attività (step-by-step)

Il flusso descrive il ciclo di vita completo: dall'**ingestione** alla **costruzione del grafo**, dalla **scoperta** alla **raccomandazione**, fino al **contributo della community** e alla **manutenzione storica**. È diviso in pipeline distinte ma interconnesse.

### 4.1 Pipeline A — Ingestione e normalizzazione

1. **Trigger di ingestione**: schedulato (Spring Batch / scheduler esistente, analogo al folder watcher) o on-demand da UI. Ogni connettore è un plugin PF4J che espone un *extension point* di tipo "EventSource".
2. **Fetch dei dati grezzi**: il connettore recupera eventi dalla fonte (API REST di ticketing, feed ICS/iCal, RSS, scraping di portale comunale, PDF di locandine via Tika/Tesseract già presenti, email di newsletter via dominio \`email\`).
3. **Parsing e mapping a schema.org/Event**: i dati grezzi sono mappati su un DTO canonico \`EventoImport\`.
4. **Validazione ai confini**: validazione schema-based; record invalidi → dead-letter queue con log; record validi proseguono.
5. **Normalizzazione**: date in UTC+timezone, geocoding dell'indirizzo (luogo → coordinate), normalizzazione valuta/prezzo, pulizia testo, rilevamento lingua.
6. **Embedding semantico**: titolo + descrizione + generi vengono trasformati in vettore tramite **Ollama EmbeddingModel** (\`@Primary\`) e archiviati in **Qdrant** (riuso della pipeline documenti).
7. **Entity resolution**:
   - per ogni entità correlata (luogo, artista, organizzatore) si cerca un nodo esistente (match esatto su \`sameAs\`/ID esterno, poi fuzzy match su nome+geo);
   - per l'evento si applica de-dup (titolo ~82% + finestra ±90 min + geo ~600 m + performer): se match → **merge** nel nodo esistente arricchendo gli attributi e registrando la nuova provenance; altrimenti → **nuovo nodo**.
8. **Persistenza nel grafo**: nodo Evento + archi verso Luogo, Data/Periodo, Performer, Organizzatore, Genere, Rassegna salvati in MySQL (struttura grafo); vettori in Qdrant (semantica).
9. **Calcolo pesi iniziali**: i pesi degli archi sono inizializzati da fattori statici (ruolo performer, affidabilità fonte, completezza dati). Vedi sezione 5.
10. **Evento di dominio**: pubblicazione di \`EventoIngeritoEvent\` via \`DomainEventPublisherPort\` per innescare indicizzazione, notifiche ai follower e ricalcolo affinità (pattern eventi esistente).

### 4.2 Pipeline B — Scoperta (discovery)

1. **Ingresso utente**: l'utente apre il feed "scopri" o pone una domanda in chat ("cosa faccio stasera entro 20 km, qualcosa di musicale all'aperto").
2. **Risoluzione del contesto**: il sistema raccoglie i vincoli espliciti (data/periodo, raggio geografico, tipo, prezzo, target) e il contesto implicito (posizione, gusti dal profilo locale, follow attivi).
3. **Query ibrida sul grafo**:
   - **filtro strutturale** su MySQL: eventi futuri nello stato valido, entro raggio geo e finestra temporale, coerenti con i filtri;
   - **espansione semantica** su Qdrant: eventi semanticamente simili agli interessi dell'utente o alla query in linguaggio naturale;
   - **traversata del grafo**: dai nodi seguiti (artisti, generi, luoghi) si espande verso eventi collegati a 1–2 hop, pesando per forza dell'arco.
4. **Ranking**: combinazione di prossimità (geo+temporale), affinità (semantica + follow), peso degli archi, freschezza/novità e segnali community (rating, popolarità). Funzione di ranking configurabile.
5. **Presentazione**: feed/lista ordinata con badge esplicativi ("perché te lo propongo"), filtri rapidi e vista mappa; possibilità di passare alla **visualizzazione grafo** per esplorare per relazioni.
6. **Interazione**: salva, segui, "mi interessa", apri biglietto → ogni interazione genera segnale comportamentale (Pipeline D).

### 4.3 Pipeline C — Raccomandazione e itinerari (GraphRAG)

1. **Domanda complessa in chat** (es. "organizzami una serata sabato: aperitivo, concerto jazz e qualcosa dopo, tutto a piedi").
2. **GraphRAG retrieval**: l'AI interroga il grafo recuperando il sottografo rilevante (eventi del sabato, luoghi vicini, generi affini ai gusti, eventi collaterali) combinando ricerca semantica Qdrant e traversata pesata MySQL.
3. **Ragionamento e composizione**: l'LLM (Ollama di default) compone un itinerario coerente nel tempo e nello spazio, rispettando vincoli (orari, distanze, budget, accessibilità).
4. **Spiegabilità**: la risposta **cita i nodi e i percorsi** usati (artista seguito → evento → luogo → evento collaterale), così l'utente capisce il "perché".
5. **Raffinamento conversazionale**: l'utente affina ("preferisco all'aperto", "budget più basso") e l'AI ricalcola sul grafo.
6. **Azioni**: salva itinerario, aggiungi al calendario (dominio \`calendar\`), condividi, attiva alert.

### 4.4 Pipeline D — Contributo e curatela community

1. **Contributo**: un utente o organizzatore crea/corregge un evento, aggiunge un luogo mancante, vota, recensisce.
2. **Validazione**: stessi controlli di qualità dell'ingestione automatica.
3. **Entity resolution assistita**: il sistema propone possibili duplicati; il contributore o il curatore conferma il merge.
4. **Coda di moderazione**: contributi sensibili (nuovi nodi, merge, segnalazioni) entrano in una coda; i curatori approvano/rifiutano.
5. **Ranking emergente**: i pesi degli archi e la reputazione dei nodi si aggiornano in base ai voti e ai contributi validati — i contenuti migliori emergono dal basso.
6. **Feedback loop**: le correzioni rafforzano la fiducia nelle fonti e migliorano l'entity resolution futura.

### 4.5 Pipeline E — Aggiornamento pesi e manutenzione storica

1. **Aggiornamento incrementale dei pesi**: a ogni interazione/contributo, ricalcolo (event-driven o batch) dei pesi degli archi interessati.
2. **Decadenza temporale**: gli eventi passano a \`terminato\`; i pesi "freschezza" decadono con curva configurabile; lo storico resta come segnale per affinità e ricorrenze.
3. **Riconoscimento ricorrenze**: eventi/festival che si ripetono vengono collegati alle edizioni precedenti, costruendo serie temporali (es. "Festival X — edizione 2026" → 2025 → 2024).
4. **Sync e idempotenza**: ri-ingestioni periodiche aggiornano stati (annullato, sold-out, riprogrammato) senza duplicare, grazie a \`sorgente\`+\`idEsterno\`.
5. **Pulizia e audit**: report di qualità dati, nodi orfani, duplicati residui, fonti non più affidabili.

### 4.6 Diagramma testuale del flusso end-to-end

\`\`\`text
[Fonti: ticketing API, ICS, RSS, portali, PDF/locandine, email]
        │  (connettori = plugin PF4J "EventSource")
        ▼
[Pipeline A: fetch → map schema.org → valida → normalizza → embedding(Ollama)
             → entity resolution/de-dup → persistenza grafo(MySQL+Qdrant) → pesi iniziali]
        │  EventoIngeritoEvent
        ▼
[GRAFO PESATO EVENTI]  ←── [Pipeline D: contributi/curatela community]
        │                          ▲
        │                          │ voti, recensioni, merge
        ├──► [Pipeline B: scoperta — query ibrida + ranking]
        ├──► [Pipeline C: GraphRAG — raccomandazioni & itinerari spiegabili]
        └──► [Pipeline E: pesi dinamici, decadenza, ricorrenze, sync]
\`\`\`

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa il **motore a grafo core** (nodi tipizzati + archi pesati su MySQL, semantica su Qdrant) specializzandolo con tipi di dominio installati dal modulo "Eventi & spettacoli".

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi chiave | Sorgente embedding (Qdrant) |
|---|---|---|---|
| \`Evento\` | L'evento singolo | titolo, date, tipo, stato, prezzo, modalità | titolo + descrizione + generi |
| \`Luogo\` (Venue) | Sede fisica/online | nome, geo, tipo, capienza, accessibilità | nome + descrizione |
| \`Artista\`/\`Performer\` | Chi si esibisce | nome, alias, ruolo, generi, sameAs | bio + generi |
| \`Organizzatore\` | Chi organizza | nome, tipo, affidabilità | descrizione |
| \`Rassegna\`/\`Festival\` | Contenitore di eventi | nome, edizione, periodo, tema | descrizione |
| \`Genere\`/\`Tema\` | Categoria tematica | nome, gerarchia, sinonimi IT/EN | nome + sinonimi |
| \`Data\`/\`Periodo\` | Nodo temporale | giorno, settimana, stagione | — (strutturale) |
| \`Utente\` | Profilo locale | preferenze, follow, cronologia | — (privato/locale) |
| \`Recensione\` | Feedback post-evento | voto, testo, autore | testo recensione |
| \`Itinerario\` | Sequenza di eventi/POI generata | tappe, orari, vincoli | descrizione |

I nodi \`Luogo\`, \`Itinerario\`, \`Genere\` e \`Utente\` sono **condivisi** con l'ambito turismo/territorio: il riuso è esplicito e voluto (un unico motore, molti domini).

### 5.2 Tipi di relazione (archi)

| Relazione (arco) | Da → A | Significato | Pesata da |
|---|---|---|---|
| \`SI_TIENE_IN\` | Evento → Luogo | L'evento si svolge nel luogo | affinità storica luogo-genere |
| \`SI_TIENE_IL\` | Evento → Data/Periodo | Collocazione temporale | freschezza/prossimità |
| \`ESEGUITO_DA\` | Evento → Artista | Performer dell'evento | ruolo (headliner > supporto) |
| \`ORGANIZZATO_DA\` | Evento → Organizzatore | Chi organizza | affidabilità organizzatore |
| \`APPARTIENE_A\` | Evento → Rassegna/Festival | Evento figlio di rassegna | centralità nel programma |
| \`HA_GENERE\` | Evento/Artista → Genere | Classificazione tematica | confidenza classificazione |
| \`EDIZIONE_DI\` | Rassegna(anno) → Rassegna(anno-1) | Continuità storica | ricorrenza/anzianità |
| \`SIMILE_A\` | Evento↔Evento, Artista↔Artista | Similarità semantica/comportamentale | cosine similarity + co-partecipazione |
| \`SEGUE\` | Utente → Artista/Genere/Luogo/Organizzatore | Interesse esplicito | recenza + frequenza |
| \`INTERESSATO_A\` / \`PARTECIPA_A\` | Utente → Evento | Intento/partecipazione | livello di intento (click<salva<conferma) |
| \`HA_RECENSITO\` | Utente → Evento (via Recensione) | Feedback | voto + utilità community |
| \`VICINO_A\` | Luogo ↔ Luogo | Prossimità geografica | distanza inversa |
| \`COLLATERALE_A\` | Evento ↔ Evento | Eventi complementari (stessa serata/area) | sovrapposizione spazio-tempo |
| \`INCLUDE_TAPPA\` | Itinerario → Evento/Luogo | Composizione itinerario | ordine + coerenza |

### 5.3 Criteri per il peso degli archi

Il peso è un valore normalizzato (es. 0–1) calcolato come **combinazione configurabile** di fattori, in linea con il principio core "peso derivato da fattori configurabili (frequenza, rilevanza, dipendenze, feedback)":

- **Rilevanza intrinseca**: ruolo (headliner vs supporto), centralità nel programma di una rassegna, completezza del dato.
- **Frequenza / ricorrenza**: quante volte un luogo ospita un genere, quante edizioni ha una rassegna, quanto spesso un utente interagisce con un genere.
- **Recenza e freschezza**: eventi imminenti pesano più di quelli lontani; gli archi di interesse decadono nel tempo se non rinnovati.
- **Prossimità**: distanza geografica (peso inversamente proporzionale) e distanza temporale.
- **Affinità semantica**: cosine similarity dei vettori Qdrant (evento↔evento, evento↔gusti utente, artista↔artista).
- **Segnali community**: voti, rating medi, numero di partecipazioni confermate, reputazione del contributore.
- **Affidabilità della fonte**: archi derivati da fonti più affidabili o da curatela manuale pesano di più; provenance tracciata.
- **Livello di intento**: per gli archi utente→evento, una conferma di partecipazione pesa più di un semplice click.

Formula concettuale (pesi \`α…θ\` configurabili per dominio):

\`\`\`
peso(arco) = α·rilevanza + β·frequenza + γ·recenza
           + δ·prossimità + ε·affinità_semantica
           + ζ·segnali_community + η·affidabilità_fonte + θ·intento
\`\`\`

I pesi sono **ricalcolati in modo incrementale** (event-driven) e periodicamente consolidati (batch), evitando ricalcoli globali costosi.

### 5.4 Rappresentazione su MySQL + Qdrant (no Neo4j)

- **MySQL**: tabelle \`nodes\` (id UUID \`CHAR(36)\`, tipo, attributi JSON, dominio) e \`edges\` (id, from_node, to_node, tipo, peso, attributi JSON, provenance), con indici su tipo, geo (lat/long) e finestra temporale. Le traversate a pochi hop sono espresse con join/CTE ricorsive (attenzione all'escaping di \`recursive\`/\`timestamp\`, parole riservate MySQL).
- **Qdrant**: collezione per gli embedding dei nodi testuali (evento, artista, luogo, recensione, genere) per la componente semantica e l'arco \`SIMILE_A\`.
- **Coerenza**: l'id del nodo è la chiave che lega record MySQL e punto Qdrant; la pipeline mantiene i due store allineati.

---

## 6. Fonti dati & connettori (ingestione)

Ogni fonte è incapsulata in un **connettore plugin PF4J** che implementa un extension point dedicato (\`EventSourceExtension\`), così da aggiungere nuove fonti senza toccare il core. I connettori riusano l'infrastruttura esistente (Spring Batch/scheduler, Tika/Tesseract, dominio \`email\`, \`DomainEventPublisherPort\`).

| Fonte | Tipo connettore | Riuso infrastruttura | Note di conformità |
|---|---|---|---|
| **Feed ICS/iCal** | Pull schedulato | Parser ICS dedicato | Standard aperto; ideale per comuni/teatri |
| **RSS / Atom** | Pull schedulato | Parser feed | Per testate locali e blog culturali |
| **Piattaforme ticketing** (Eventbrite, Ticketmaster, DICE…) | Pull via API REST | WebClient esistente | Rispettare ToS, rate limit e licenze; chiavi via settings/DB |
| **Portali comunali / istituzionali** | Scraping rispettoso o API | Tika per HTML | Solo dati pubblici; robots/ToS |
| **PDF locandine / brochure** | Upload o folder watcher | Tika + Tesseract OCR | Estrazione testo + LLM per structuring |
| **Newsletter / email organizzatori** | Ingestione email | Dominio \`email\` (IMAP) | Consenso utente; parsing eventi da mail |
| **Calendario utente** | Sync bidirezionale | Dominio \`calendar\` | Privacy: dati locali |
| **Contributi manuali / community** | Editor UI + API | API grafo | Validazione + moderazione |
| **schema.org JSON-LD da pagine web** | Estrazione strutturata | Fetch + parser JSON-LD | Sfrutta markup \`Event\` già pubblicato |

**Strategie trasversali di ingestione**:
- **Idempotenza** via \`sorgente\`+\`idEsterno\`; sync che aggiorna invece di duplicare.
- **Entity resolution** centralizzata e riusabile tra tutti i connettori.
- **Provenance** per ogni attributo, per audit e risoluzione conflitti.
- **Rispetto licenze e ToS**: ogni connettore dichiara la licenza dei dati e i limiti d'uso; conformità all'anima open source del progetto.
- **Local-first**: tutti i connettori funzionano on-premise; nessuna dipendenza cloud obbligatoria.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

Mappa concreta delle funzionalità, distinguendo **MVP** (primo rilascio utile end-to-end), **Evoluzione** (incrementi successivi) e **Manutenzione** (attività ricorrenti).

### 7.1 MVP — Vertical slice minima ma completa

Obiettivo: dimostrare il valore "dalla fonte alla scoperta spiegabile" su almeno una fonte reale.

| # | Funzionalità | Layer | Riuso/Note |
|---|---|---|---|
| M1 | Tipi di nodo/arco eventi nel motore a grafo (schema modulare) | domain + Flyway | Estende dominio \`knowledge\` |
| M2 | Migrazioni Flyway per nodi/archi eventi (una query per file) | app | Vincolo una-query |
| M3 | Connettore ICS/iCal (1 fonte reale) come plugin PF4J | infra/plugin | Standard aperto, basso attrito |
| M4 | Pipeline di ingestione: map schema.org → valida → normalizza → embedding Ollama → Qdrant | domain + infra | Riusa pipeline documenti |
| M5 | Entity resolution / de-dup base (titolo+geo+tempo) | domain | Fuzzy match configurabile |
| M6 | API CRUD nodi/archi eventi + query "eventi vicini nei prossimi N giorni" | api | \`/api/v1/events\` |
| M7 | Scoperta base: lista/feed con filtri data, luogo (raggio), tipo, prezzo | frontend | Feature Angular standalone |
| M8 | Vista mappa eventi | frontend | Geo dei luoghi |
| M9 | Chat GraphRAG base: "cosa faccio stasera vicino a me" con citazione nodi | domain + frontend | Riusa ChatUseCase/LLM gateway |
| M10 | Enum tradotte IT/EN (tipo, stato, modalità, target) instradate al frontend | domain + frontend | Vincolo i18n |
| M11 | Follow base (artista/genere/luogo) + feed personalizzato | domain + frontend | Segnali per pesi |
| M12 | Documentazione IT/EN del modulo e dei connettori | documentation/documentazione | Vincolo bilingue |

### 7.2 Evoluzione — Incrementi successivi

| # | Funzionalità | Valore |
|---|---|---|
| E1 | Connettori aggiuntivi (RSS, ticketing API, portali, PDF/OCR, email) | Copertura fonti |
| E2 | Visualizzazione interattiva del grafo eventi (nodi/archi/peso, espansione progressiva) | Scoperta per relazioni |
| E3 | Itinerari serata/weekend generati da AI (GraphRAG avanzato, vincoli spazio-tempo-budget) | Pianificazione |
| E4 | Pesi dinamici completi + decadenza temporale + ricorrenze/edizioni | Ranking emergente |
| E5 | Raccomandazioni personalizzate avanzate + "perché te lo propongo" ricco | Personalizzazione |
| E6 | Alert/notifiche su nuove date di artisti/generi/luoghi seguiti | Retention |
| E7 | Recensioni, rating e ranking emergente dalla community | Qualità dal basso |
| E8 | Strumenti di curatela/moderazione + entity resolution assistita | Sostenibilità grafo |
| E9 | Integrazione calendario (aggiungi evento/itinerario) e messaging (condivisione) | Azionabilità |
| E10 | Sinergia con turismo: eventi dentro itinerari territorio (POI+eventi) | Cross-dominio |
| E11 | Riconoscimento ricorrenze e serie storiche dei festival | Memoria storica |
| E12 | Accessibilità avanzata (filtri accessibilità, contenuti inclusivi) | Inclusività |

### 7.3 Manutenzione — Attività ricorrenti

- **Aggiornamento connettori** al variare delle API/ToS delle fonti; gestione versioni plugin.
- **Monitoraggio qualità dati**: report duplicati residui, nodi orfani, fonti degradate; tuning dei parametri di entity resolution.
- **Tuning dei pesi e del ranking** in base ai feedback e alle metriche.
- **Pulizia storica**: gestione decadenza, archiviazione eventi terminati, integrità delle serie ricorrenti.
- **Aggiornamento tassonomie** generi/temi e sinonimi IT/EN.
- **Manutenzione embedding**: re-embedding al cambio di modello Ollama; coerenza MySQL↔Qdrant.
- **Documentazione e i18n** costantemente aggiornati (vincolo di progetto).

---

## 8. Casi d'uso AI / GraphRAG

L'AI naviga il grafo pesato combinando **traversata strutturale** (MySQL) e **similarità semantica** (Qdrant), con Ollama come motore locale di default. Esempi concreti:

1. **Scoperta conversazionale**: *"Cosa faccio stasera entro 15 km, qualcosa di musicale e non troppo caro?"* → filtro geo-temporale + affinità semantica + pesi; risposta con citazione dei nodi.
2. **Itinerario serata spiegabile**: *"Organizzami sabato: aperitivo, concerto jazz, after, tutto a piedi"* → sottografo eventi+luoghi vicini, composizione spazio-temporale, citazione del percorso.
3. **Raccomandazione affine**: *"Eventi simili a quello a cui sono andato il mese scorso"* → arco \`SIMILE_A\` (semantico + co-partecipazione).
4. **Collegamenti non evidenti**: *"Perché dovrei andare a questo festival?"* → l'AI mostra il percorso (segui artista A → A simile a headliner B → B a festival X → luogo già visitato).
5. **Domanda multi-hop sul territorio**: *"Festival all'aperto nel raggio di 30 km, nei weekend di luglio, con eventi gastronomici collaterali"* → traversata \`APPARTIENE_A\` + \`COLLATERALE_A\` + filtri.
6. **Alert intelligenti**: l'AI riassume "novità per te" sui follow attivi (nuove date, riprogrammazioni).
7. **Q&A storico**: *"Quante edizioni ha avuto il Festival X e chi ha suonato?"* → traversata \`EDIZIONE_DI\` + \`ESEGUITO_DA\`.
8. **Strutturazione da fonti non strutturate**: l'LLM estrae eventi da locandine PDF/OCR ed email, popolando i nodi (con validazione).
9. **Assistenza alla curatela**: l'AI propone merge di duplicati e correzioni, spiegando la confidenza.
10. **Spiegabilità sistematica**: ogni risposta cita nodi e percorsi usati — requisito core del motore a grafo.

---

## 9. KPI & metriche di successo

| Categoria | KPI | Obiettivo / direzione |
|---|---|---|
| **Copertura grafo** | N° eventi attivi, N° fonti connesse, completezza attributi | Crescente |
| **Qualità dati** | Tasso duplicati residui, % eventi geocodificati, % con performer/genere | Duplicati ↓, completezza ↑ |
| **Entity resolution** | Precision/recall del merge, falsi merge | Precision ↑, falsi merge ↓ |
| **Scoperta** | Eventi visualizzati/salvati per sessione, CTR sulle raccomandazioni | Crescente |
| **Personalizzazione** | % raccomandazioni accettate vs ignorate/nascoste | Accettazione ↑ |
| **GraphRAG** | Tasso di risposte con citazione nodi corretta, soddisfazione utente | ↑ |
| **Engagement community** | N° contributi/correzioni/voti, contributori attivi | Crescente |
| **Itinerari** | N° itinerari generati e salvati, tasso di completamento | Crescente |
| **Performance** | Latenza query "vicini prossimi N giorni", latenza risposta GraphRAG | Sotto soglia interattiva |
| **Privacy/local-first** | % funzioni operative offline, zero invii non consensati | 100% offline core |
| **i18n** | Copertura traduzioni IT/EN enum e UI | 100% |

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---|---|---|
| **Qualità/duplicati dalle fonti eterogenee** | Grafo "sporco", scoperta degradata | Entity resolution robusta + provenance + curatela + report qualità |
| **Termini d'uso / licenze delle fonti** | Rischio legale, blocco fonte | Connettori che dichiarano licenza/ToS; preferenza per feed aperti (ICS/RSS/JSON-LD) |
| **Effimerità degli eventi** | Dati obsoleti mostrati | Stati ciclo di vita + sync periodico + decadenza freschezza |
| **Limiti delle query a grafo su MySQL (no Neo4j)** | Traversate profonde lente | Indici geo/temporali, CTE limitate a pochi hop, denormalizzazione mirata; rivalutazione futura |
| **Coerenza MySQL↔Qdrant** | Risultati incoerenti | Pipeline transazionale/compensativa, re-embedding controllato, audit di coerenza |
| **Cold start personalizzazione** | Raccomandazioni povere all'inizio | Fallback su popolarità/prossimità + onboarding interessi |
| **Privacy dei gusti utente** | Perdita di fiducia | Dati comportamentali solo locali; nessun invio cloud senza consenso |
| **Geocoding offline** | Mancanza coordinate | Geocoder locale/self-hosted; fallback manuale in curatela |
| **Sovraccarico moderazione community** | Backlog, dati errati | Entity resolution assistita da AI + reputazione + automazioni |
| **Esplosione tipi/tassonomie** | Schema ingestibile | Tassonomia controllata + governance + sinonimi IT/EN |

---

## 11. Manutenzione & evoluzione

- **Governance dello schema di dominio**: i tipi di nodo/arco eventi evolvono in modo additivo; modifiche tramite migrazioni Flyway (una query per file) e versionamento del modulo plugin.
- **Ciclo di vita dei connettori**: ogni connettore PF4J ha versione e compatibilità dichiarata; aggiornamenti al variare delle API/ToS; il marketplace LocalMind distribuisce i connettori.
- **Tuning continuo di pesi e ranking**: i coefficienti della funzione di peso sono configurabili e calibrati su metriche reali; A/B interno tramite feedback.
- **Manutenzione semantica**: re-embedding pianificato al cambio del modello Ollama; verifica di coerenza tra MySQL e Qdrant.
- **Curatela e qualità dati**: cruscotti di qualità, code di moderazione, strumenti di merge; reputazione dei contributori.
- **Memoria storica**: archiviazione eventi terminati mantenendo gli archi utili (ricorrenze, affinità storiche).
- **Documentazione viva**: aggiornamento costante della documentazione IT/EN e dei log in \`Sviluppi/\` (vincolo di progetto), con sviluppi condotti in plan mode.
- **Roadmap evolutiva**: dalla scoperta consumer verso integrazioni cross-dominio (turismo), accessibilità avanzata, e — solo se le query lo imporranno — rivalutazione di un datastore a grafo dedicato (oggi esplicitamente fuori scope).

---

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo esistente | Ruolo nell'ambito Eventi & spettacoli |
|---|---|
| **\`knowledge\`** | Base del motore a grafo; ospita i tipi di nodo/arco eventi (schema modulare) |
| **\`llm\`** | Chat e GraphRAG; \`LlmGatewayService\` con Ollama default e fallback cloud opzionale; embedding \`@Primary\` Ollama |
| **\`document\`** | Riuso di Tika/Tesseract per ingerire locandine PDF e contenuti non strutturati; pipeline chunking/embedding |
| **vector store / Qdrant** | \`QdrantVectorStoreAdapter\` per la semantica dei nodi e l'arco \`SIMILE_A\` |
| **persistenza / MySQL + Flyway** | Tabelle \`nodes\`/\`edges\`, UUID \`CHAR(36)\`, migrazioni una-query |
| **\`email\`** | Connettore di ingestione eventi da newsletter/organizzatori (IMAP) |
| **\`calendar\`** | Aggiunta di eventi/itinerari al calendario; sync calendario utente |
| **\`messaging\` / \`channels\`** | Notifiche e alert sui follow; condivisione di eventi e itinerari |
| **\`automation\`** | Pipeline schedulate di ingestione/sync e ricalcolo pesi event-driven |
| **\`agent\`** | Agenti per scoperta proattiva, curatela assistita, structuring da fonti non strutturate |
| **\`mcp\`** | Esposizione di tool MCP (es. ricerca eventi) ad agenti/strumenti esterni |
| **\`plugin\` (PF4J)** | Connettori \`EventSourceExtension\` e tipi di dominio installabili senza toccare il core |
| **\`marketplace\`** | Distribuzione del modulo "Eventi & spettacoli" e dei singoli connettori |
| **\`auth\`** | Profili utente locali, follow e cronologia in ottica privacy local-first |
| **\`common\` (eventi di dominio)** | \`DomainEventPublisherPort\` per \`EventoIngeritoEvent\`, ricalcolo pesi, notifiche |
| **Frontend Angular 21** | Nuova feature standalone lazy-loaded (feed, mappa, grafo, chat), Signal store, \`TranslatePipe\` IT/EN |

**Principio guida dell'integrazione**: l'ambito Eventi & spettacoli **non è un'app separata**, ma un **modulo verticale che specializza il motore a grafo universale** riusando al massimo l'infrastruttura esistente e rispettando i vincoli di progetto (local-first, AI Ollama default, MySQL+Qdrant senza Neo4j, plugin PF4J, privacy, open source, bilingue IT/EN, migrazioni Flyway con una sola query).

---

> **Nota sugli sviluppi**: ogni sviluppo relativo a questo ambito va condotto in plan mode e tracciato nella cartella \`Sviluppi/\` con nomenclatura \`YYYY-MM-DD_NN_NomeFunzionalità\`, mantenendo aggiornata la documentazione bilingue IT/EN.
`;
