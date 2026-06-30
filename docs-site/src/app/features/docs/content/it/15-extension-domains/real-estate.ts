export const content = `# Real estate & immobiliare

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo documento descrive l'ambito di estensione **Real estate & immobiliare** (gruppo: *consumer*) del motore di Knowledge Graph universale di LocalMind. L'obiettivo è trasformare la ricerca immobiliare da una sequenza di filtri su una tabella di annunci a una **navigazione consapevole di un grafo pesato** che mette in relazione immobili, zone, servizi, prezzi e profili di vita, e che l'AI può percorrere (GraphRAG) per rispondere a domande complesse come "una casa a tre camere, sotto i 350.000 €, in una zona tranquilla ma con scuole buone raggiungibili a piedi e metro entro 10 minuti".

L'ambito riusa integralmente lo stack esistente (Spring Boot esagonale, Angular 21, MySQL 8.0 per struttura, Qdrant per la semantica, Ollama come AI locale di default) e viene veicolato come **modulo di dominio installabile** tramite il sistema plugin PF4J + marketplace, nel rispetto dei vincoli local-first, privacy e bilinguismo IT/EN.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema reale

La ricerca immobiliare oggi è frammentata, opaca e fortemente sbilanciata verso chi vende. Chi cerca casa (o un investimento) affronta tre dolori strutturali:

1. **Disallineamento tra il dato dell'annuncio e la decisione di vita.** I portali (immobiliare.it, idealista, Casa.it) descrivono l'immobile — metri quadri, vani, prezzo, classe energetica — ma quasi nulla del *contesto* che determina davvero la qualità della vita: quanto è realmente raggiungibile a piedi la fermata della metro, quanto sono buone e *accessibili* le scuole, quanto è rumorosa la via, se ci sono servizi essenziali (farmacia, supermercato, asilo, medico) a 10 minuti. Il filtro "vicino a" sui portali usa distanza in linea d'aria, che la letteratura di location intelligence dimostra essere un cattivo predittore: i tempi di percorrenza su rete pedonale/trasporto pubblico catturano valore che la distanza euclidea ignora.

2. **Nessuna spiegazione del prezzo.** L'utente vede un prezzo richiesto, ma non sa se è coerente con il mercato di *quella* microzona. In Italia esiste un riferimento pubblico e gratuito — le **quotazioni OMI** dell'Agenzia delle Entrate, semestrali, per zona omogenea e tipologia (€/m² min–max) — ma è isolato dagli annunci e illeggibile per il cittadino. Il divario tra prezzo richiesto e quotazione OMI della zona è un segnale di consapevolezza enorme che oggi nessuno mette in mano all'utente.

3. **Confronto impossibile e perdita di memoria.** Valutare 15 immobili in 4 quartieri diversi significa tenere a mente decine di trade-off (questo costa meno ma è lontano dal lavoro; quello è perfetto ma in una via senza servizi). Le persone lo fanno con fogli Excel improvvisati e screenshot. Non esiste uno strumento che modelli *il ragionamento* — zona ⇄ servizi ⇄ prezzo ⇄ vincoli personali — e lo renda navigabile e interrogabile.

### 1.2 La nostra risposta: il grafo zona–servizi–prezzi

LocalMind modella il dominio come **grafo pesato e interrogabile dall'AI**. Non una lista di annunci, ma una rete di relazioni:

- L'immobile è collegato alla sua **zona OMI**, alla **via**, all'**edificio**.
- La zona è collegata ai **servizi/POI** vicini (scuole, trasporti, sanità, commercio, verde) con archi **pesati dal tempo di percorrenza reale** (a piedi / in auto / con i mezzi), non dalla distanza in linea d'aria.
- L'immobile è collegato alla **quotazione di mercato** della sua zona/tipologia, abilitando il calcolo automatico dello scostamento prezzo richiesto ↔ mercato.
- Il **profilo dell'utente** (luoghi della vita: lavoro, scuola dei figli, palestra; priorità; budget; vincoli) diventa esso stesso un sottografo che pesa e ri-ordina tutto il resto.

Su questo grafo l'AI locale (GraphRAG) **naviga, spiega e suggerisce**: risponde a query in linguaggio naturale combinando vincoli relazionali (percorsi, vicinati) e semantica (descrizioni, recensioni), e — punto chiave del progetto — **fa emergere collegamenti non evidenti** ("questa zona costa il 20% in meno della confinante ma ha gli stessi tempi verso il centro e scuole meglio valutate").

### 1.3 Valore per tipo di utente

| Utente | Valore concreto |
|--------|-----------------|
| Acquirente / inquilino | Scelta consapevole: capisce *perché* un immobile è adatto alla sua vita, non solo se rientra nei filtri. Scopre zone alternative che non avrebbe cercato. |
| Investitore | Vede scostamenti prezzo↔OMI, rendimento da locazione potenziale, dinamica dei servizi (zona in miglioramento), rischio. |
| Agente immobiliare | Strumento di consulenza: presenta al cliente non un annuncio ma un dossier di contesto difendibile e tracciabile. |
| Community locale | Arricchisce il grafo con conoscenza qualitativa (rumore, sicurezza percepita, vivibilità) che nessun portale possiede. |

### 1.4 Perché LocalMind e non un portale

- **Local-first e privacy.** I luoghi della vita di una persona (dove lavoro, dove vanno a scuola i figli, quanto guadagno/spendo) sono dati estremamente sensibili. In LocalMind restano sul nodo dell'utente; l'AI che li elabora è Ollama in locale per default. Nessun portale può offrire questo.
- **Neutralità.** Non vendiamo annunci e non siamo pagati dagli inserzionisti: il grafo è ottimizzato per la *decisione dell'utente*, non per il click.
- **Universalità del motore.** Lo stesso engine che serve turismo ed enterprise serve l'immobiliare: i tipi di nodo/relazione cambiano, l'infrastruttura no. Questo abbatte il costo di sviluppo e manutenzione.

### 1.5 Cosa NON è (confini di valore)

Non è un portale di annunci concorrente, non gestisce transazioni/rogiti/contratti, non è una AVM (Automated Valuation Model) certificata per perizie legali. È uno **strato di intelligenza decisionale** sopra dati di mercato pubblici, annunci aggregati e conoscenza di contesto.

---

## 2. Personas & utenti target

| Persona | Profilo | Obiettivo principale | Bisogni dal grafo |
|---------|---------|----------------------|-------------------|
| **Giulia, 34 — prima casa** | Coppia con figlio piccolo, budget 280–340k, lavora in centro | Casa adatta alla famiglia senza svenarsi | Scuole/asili accessibili a piedi, tempo verso lavoro, prezzo onesto vs zona, zona tranquilla |
| **Marco, 41 — upgrade abitativo** | Vende e ricompra più grande, conosce già la città | Ottimizzare metratura/zona a parità di rata | Confronto fine tra microzone, scostamento OMI, trend di valore |
| **Sara, 29 — affitto/relocation** | Si trasferisce per lavoro in città nuova | Quartiere giusto in fretta, da remoto | Esplorazione zone sconosciute, sicurezza, commute, vita sociale |
| **Davide, 47 — investitore** | Compra per mettere a reddito | Rendimento e rischio | Yield potenziale, domanda locativa, scostamento prezzo, servizi che trainano valore |
| **Agente immobiliare** | Professionista che usa LocalMind come strumento | Consulenza differenziante al cliente | Dossier di zona spiegabile, confronto immobili, export report |
| **Contributor community** | Residente che conosce la zona | Migliorare la conoscenza condivisa | Aggiungere/valutare info qualitative su zone e servizi |
| **Self-hoster / analista dati** | Tecnico che installa LocalMind on-prem | Pipeline dati immobiliari privata e controllata | Connettori, API grafo, controllo totale dei dati |

**Anti-persona:** chi cerca semplicemente "l'annuncio più economico" senza interesse al contesto — meglio servito da un portale tradizionale. Il valore di LocalMind cresce con la complessità della decisione.

---

## 3. Requisiti in input

Questa sezione è deliberatamente dettagliata: definisce *tutto ciò che entra* nel grafo e *come* l'utente e i connettori lo alimentano. Gli input si dividono in: input dell'utente (cosa cerca e chi è), input degli immobili (gli oggetti), input di contesto (zona/servizi/mercato), input di configurazione (come pesare).

### 3.1 Input dell'utente — criteri di ricerca

Tutti i campi sono **opzionali e progressivi**: l'utente può partire da una frase in linguaggio naturale e raffinare. Ogni campo va validato al boundary (range, enum, coerenza).

| Categoria | Campo | Tipo | Note di validazione |
|-----------|-------|------|---------------------|
| Operazione | Acquisto / Affitto | enum (IT/EN) | obbligatorio per attivare il pricing corretto |
| Budget | min / max prezzo o canone | intero ≥ 0 | max ≥ min; valuta EUR |
| Tipologia | appartamento, villa, attico, monolocale… | enum (IT/EN) | mappa su tipologie OMI |
| Dimensione | m² min/max, n. locali, n. bagni | intero | range plausibili |
| Stato | nuovo, ristrutturato, da ristrutturare | enum | |
| Caratteristiche | ascensore, terrazzo, giardino, box, classe energetica | flag + enum | classe A–G |
| Localizzazione | città, quartieri/zone, raggio | testo + geo | almeno un'ancora geografica |
| Vincoli temporali | disponibilità entro data | data | futura |

### 3.2 Input dell'utente — profilo di vita (il differenziatore)

È il sottografo che personalizza i pesi. Massima sensibilità privacy: dati locali, mai inviati al cloud senza consenso esplicito.

- **Luoghi ancora (anchor points):** indirizzo/i di lavoro, scuola/asilo dei figli, casa di familiari, palestra, ecc., ciascuno con **modalità preferita** (a piedi / bici / mezzi / auto) e **tempo massimo accettabile**.
- **Composizione nucleo:** single, coppia, famiglia con figli (età), animali — guida quali servizi pesano.
- **Priorità ponderate:** l'utente assegna importanza relativa (es. slider 0–100) a dimensioni come *silenzio/quiete*, *vita sociale/locali*, *verde*, *sicurezza*, *commute breve*, *prezzo*, *scuole*. Questi pesi alimentano direttamente il ranking sul grafo.
- **Deal-breaker:** vincoli rigidi non negoziabili (es. "obbligatorio box auto", "vietato piano terra", "max 15 min a piedi dalla metro").

### 3.3 Input degli immobili (gli oggetti del grafo)

Per ciascun immobile, da connettore o inserimento manuale:

| Campo | Origine tipica | Obbligatorio |
|-------|----------------|--------------|
| Identificativo / URL annuncio | connettore portale | sì |
| Indirizzo + geocodifica (lat/lng) | annuncio + geocoder | sì (geo è critica) |
| Prezzo / canone richiesto | annuncio | sì |
| Tipologia, m², locali, bagni, piano | annuncio | sì (tip. + m²) |
| Classe energetica, anno, stato | annuncio | no |
| Caratteristiche (box, terrazzo, ascensore…) | annuncio (testo → estrazione) | no |
| Descrizione testuale | annuncio | no (→ embedding Qdrant) |
| Foto | annuncio | no (→ multimodale, evoluzione) |
| Data pubblicazione / storico prezzo | connettore | no (per trend) |

**Regola di qualità del dato:** un immobile senza coordinate valide *non entra nel grafo come nodo geolocalizzato* (degrada a nodo "non collocabile", escluso dalle query di prossimità). La geocodifica è il cardine dell'intero ambito.

### 3.4 Input di contesto — servizi, zone, mercato

Alimentati dai connettori (sezione 6), aggiornati periodicamente:

- **POI / servizi:** scuole, asili, università, fermate trasporto pubblico, supermercati, farmacie, ospedali/ASL, parchi/verde, ristoranti/locali, palestre, banche, uffici postali — ciascuno con categoria, geo, e attributi (es. ordine scolastico, linea/e di trasporto).
- **Rete di mobilità:** grafo stradale/pedonale (OpenStreetMap) e orari trasporto pubblico (GTFS dove disponibile) per calcolare **tempi di percorrenza reali**.
- **Zone OMI:** perimetri delle zone omogenee + quotazioni semestrali €/m² (min–max) per tipologia, da Agenzia delle Entrate (CSV gratuito).
- **Dati qualitativi (community + open data):** rumore, sicurezza percepita/statistiche, qualità dell'aria, indici di vivibilità.

### 3.5 Input di configurazione (come pesare il grafo)

Per consentire personalizzazione e self-hosting:

- **Pesi di default per categoria di servizio** (es. quanto conta una scuola vs un bar), sovrascrivibili dal profilo utente.
- **Funzioni di decadimento per distanza/tempo** (un servizio a 3 min pesa più che a 20 min — decadimento configurabile, es. esponenziale).
- **Parametri di freshness:** ogni quanto rinfrescare OMI (semestrale), POI (mensile), annunci (giornaliero/orario).
- **Soglie deal-breaker → filtri rigidi** vs **preferenze → pesi morbidi**.

### 3.6 Riepilogo flussi di input

| Sorgente | Cadenza | Destinazione nel sistema |
|----------|---------|--------------------------|
| Form ricerca + profilo utente | on-demand | sottografo utente (MySQL) + pesi runtime |
| Connettore annunci | giornaliera/oraria | nodi Immobile + embedding descrizioni (Qdrant) |
| Connettore POI (OSM/Overpass) | mensile | nodi Servizio + archi di prossimità |
| OMI CSV | semestrale | nodi Zona + nodi Quotazione |
| GTFS / routing engine | mensile / on-demand | pesi archi tempo-di-percorrenza |
| Contributi community | continua | attributi qualitativi su Zona/Servizio + voti |

---

## 4. Flusso dell'attività (step-by-step)

Il flusso descrive il percorso end-to-end, dall'ingestione dei dati alla decisione consapevole dell'utente. È diviso in **fase di costruzione del grafo** (asincrona, batch) e **fase di interazione** (sincrona, guidata dall'AI).

### 4.1 Fase A — Costruzione e arricchimento del grafo (batch)

**Step A1 — Ingestione immobili.** Il connettore annunci (o l'upload manuale) acquisisce le inserzioni. Riuso del modulo \`document\`/\`batch\` esistente per orchestrazione e scheduling. Ogni annuncio viene normalizzato in un nodo \`Immobile\`.

**Step A2 — Geocodifica e collocazione.** Ogni indirizzo viene geocodificato (lat/lng). L'immobile viene collegato con archi a \`Via\`/\`Edificio\` e ricondotto alla \`ZonaOMI\` il cui perimetro lo contiene (point-in-polygon). Senza geo valida → nodo marcato non collocabile.

**Step A3 — Estrazione semantica.** La descrizione testuale viene chunked ed embeddata su **Qdrant** (riuso pipeline esistente Tika/embedding). Caratteristiche implicite ("luminoso", "ristrutturato di recente", "vicino al parco") vengono estratte via LLM locale e promosse ad attributi/relazioni.

**Step A4 — Ingestione contesto.** I connettori POI (OSM/Overpass), OMI (CSV) e mobilità (OSM+GTFS) popolano nodi \`Servizio\`, \`ZonaOMI\`, \`Quotazione\` e la rete di mobilità.

**Step A5 — Calcolo degli archi pesati di prossimità.** Per ogni immobile/zona, si calcolano i **tempi di percorrenza reali** verso i servizi rilevanti (a piedi, mezzi, auto) usando il grafo di mobilità — non la distanza in linea d'aria. Si materializzano archi \`VICINO_A\` con peso = funzione(tempo, modalità, categoria). Questo è l'arco più costoso da calcolare: si fa in batch e si memorizza.

**Step A6 — Aggancio prezzo↔mercato.** Ogni immobile viene collegato alla \`Quotazione\` OMI della sua zona+tipologia; si calcola e materializza lo **scostamento %** prezzo richiesto vs intervallo di mercato (sotto/in linea/sopra).

**Step A7 — Indici di zona derivati.** Si calcolano per zona indici aggregati: *walkability* (densità e accessibilità servizi), *mix funzionale*, *verde*, *trasporto*, *prezzo medio*, *trend*. Diventano attributi del nodo \`ZonaOMI\`.

**Step A8 — Suggerimento collegamenti (GraphRAG building).** L'AI propone archi non evidenti (zone "gemelle" per profilo, servizi che trainano valore) — coerente con il requisito di progetto "far emergere collegamenti non evidenti".

### 4.2 Fase B — Interazione e decisione (sincrona)

**Step B1 — Espressione del bisogno.** L'utente scrive in linguaggio naturale ("3 camere sotto 350k, zona tranquilla, scuole buone a piedi, metro vicino") oppure compila il form progressivo (§3.1) e il profilo di vita (§3.2).

**Step B2 — Parsing e grounding.** L'AI locale traduce la richiesta in: filtri rigidi (deal-breaker) + pesi morbidi (preferenze) + ancore geografiche del profilo. La query in NL viene scomposta nelle componenti strutturate del grafo.

**Step B3 — Esecuzione ibrida sul grafo.** Il motore combina (a) **filtro relazionale/strutturale** sul grafo MySQL (vincoli, prossimità materializzata, scostamento prezzo) e (b) **ricerca semantica** su Qdrant (descrizioni affini al desiderata). Routing per tipo di query, come da best practice GraphRAG ibride 2026.

**Step B4 — Scoring e ranking personalizzato.** Ogni immobile candidato riceve un punteggio = somma pesata dei contributi: vicinanza ai luoghi ancora dell'utente, copertura servizi prioritari, allineamento prezzo↔mercato, match semantico. I pesi vengono dalle priorità dell'utente (§3.2).

**Step B5 — Spiegazione (explainable).** Per ciascun risultato l'AI genera una spiegazione **citando i nodi/percorsi del grafo** usati: "9 min a piedi dalla scuola X, metro Y a 6 min, prezzo 8% sotto OMI di zona, ma via trafficata (−)". Citazione delle fonti come da requisito di progetto.

**Step B6 — Esplorazione del grafo.** L'utente naviga visivamente: espande dall'immobile alla zona, dalla zona ai servizi, salta a zone "gemelle" suggerite, applica filtri per tipo di nodo/relazione. Navigazione esplorativa progressiva.

**Step B7 — Confronto e shortlist.** L'utente crea una shortlist; il sistema genera una **matrice di confronto** multi-criterio e un dossier per immobile.

**Step B8 — Decisione e follow-up.** Esportazione del dossier (PDF/report), salvataggio della ricerca, alert su nuovi immobili che entrano nel grafo e matchano il profilo (riuso modulo \`automation\`/\`messaging\`).

**Step B9 — Feedback loop.** Le azioni dell'utente (salva, scarta, "non mi interessa questa zona") ri-pesano il grafo personale e migliorano i suggerimenti — alimentando i pesi delle relazioni (requisito core del motore).

### 4.3 Diagramma sintetico del flusso

\`\`\`
[Connettori] → A1 ingest → A2 geocoding → A3 embedding(Qdrant)
                                 ↓
        A4 contesto (POI/OMI/GTFS) → A5 archi tempo-reale → A6 prezzo↔OMI → A7 indici zona → A8 link AI
                                 ↓
                         GRAFO PESATO (MySQL + Qdrant)
                                 ↓
[Utente] B1 NL/form → B2 parsing → B3 query ibrida → B4 scoring → B5 spiegazione
                                 ↓
              B6 esplorazione ⇄ B7 confronto → B8 dossier/alert → B9 feedback (ri-pesa)
\`\`\`

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa lo schema generico nodi/archi del motore (tabelle MySQL per la struttura, Qdrant per i vettori). Sotto, la specializzazione per l'ambito immobiliare. Tutti i tipi di nodo e relazione sono enum bilingui IT/EN veicolate al frontend.

### 5.1 Tipi di nodo

| Tipo nodo | Descrizione | Attributi principali | Vettore Qdrant |
|-----------|-------------|----------------------|----------------|
| \`Immobile\` | L'unità in vendita/affitto | prezzo, m², locali, tipologia, classe energetica, geo | sì (descrizione) |
| \`Edificio\` | Stabile che contiene immobili | anno, tipologia costruttiva, geo | no |
| \`Via\` | Asse stradale | nome, traffico/rumore stimato | no |
| \`ZonaOMI\` | Zona omogenea OMI | indici derivati (walkability, verde, prezzo medio, trend) | opzionale |
| \`Quartiere\` | Aggregazione informale/community | nome, identità percepita | sì (descrizione community) |
| \`Comune\` / \`Città\` | Livello amministrativo | popolazione, geo | no |
| \`Quotazione\` | Valore di mercato OMI | €/m² min–max, tipologia, semestre | no |
| \`Servizio\`/\`POI\` | Punto di interesse | categoria, geo, attributi specifici | opzionale |
| \`Scuola\` | Specializzazione di Servizio | ordine, valutazione | no |
| \`Trasporto\` | Fermata/stazione | modalità, linee | no |
| \`AreaVerde\` | Parco/giardino | superficie | no |
| \`ProfiloUtente\` | Sottografo dell'utente | priorità, nucleo, budget | no (privato) |
| \`LuogoAncora\` | Punto della vita dell'utente | tipo, geo, modalità, tempo max | no (privato) |
| \`Recensione\` | Contributo qualitativo community | testo, voto, dimensione | sì |
| \`RicercaSalvata\` | Query persistita | criteri, pesi | no |

### 5.2 Tipi di relazione (archi)

| Relazione | Da → A | Pesata? | Significato |
|-----------|--------|---------|-------------|
| \`SI_TROVA_IN\` | Immobile → Edificio/Via/ZonaOMI | no | collocazione strutturale |
| \`APPARTIENE_A\` | ZonaOMI → Comune | no | gerarchia amministrativa |
| \`VICINO_A\` | Immobile/Zona → Servizio | **sì** | accessibilità (tempo di percorrenza reale) |
| \`RAGGIUNGE\` | Immobile → LuogoAncora | **sì** | commute verso luoghi della vita utente |
| \`QUOTATO_DA\` | Immobile → Quotazione | **sì** | scostamento prezzo↔mercato |
| \`SIMILE_A\` | Immobile → Immobile | **sì** | similarità semantica/strutturale |
| \`ZONA_GEMELLA\` | ZonaOMI → ZonaOMI | **sì** | profilo equivalente, prezzo/diverso |
| \`SERVITA_DA\` | ZonaOMI → Trasporto | **sì** | connettività della zona |
| \`CERCA\` | ProfiloUtente → criteri | no | intento dell'utente |
| \`VIVE_A\` / \`LAVORA_A\` | ProfiloUtente → LuogoAncora | no | ancore personali |
| \`RECENSISCE\` | Recensione → Zona/Servizio | **sì** | conoscenza qualitativa pesata |
| \`PREFERISCE\` / \`SCARTA\` | ProfiloUtente → Immobile/Zona | **sì** | feedback che ri-pesa il grafo |

### 5.3 Criteri di peso degli archi

Il peso è il cuore del motore. Per l'immobiliare i criteri principali:

- **Tempo di percorrenza reale (non distanza euclidea).** Per \`VICINO_A\`/\`RAGGIUNGE\`: peso = funzione di decadimento del tempo porta-a-porta per la modalità scelta. Un servizio a 3 min ha peso ~1, a 20 min ~0,2 (decadimento esponenziale configurabile). Best practice di location intelligence: la rete pedonale/mezzi batte la linea d'aria.
- **Rilevanza per il profilo.** Lo stesso arco verso una scuola pesa molto per una famiglia, poco per un single: il peso effettivo nel ranking = peso base × priorità utente per quella categoria.
- **Scostamento prezzo↔mercato.** Per \`QUOTATO_DA\`: peso normalizzato dello scarto % tra prezzo richiesto e intervallo OMI (sotto mercato = segnale positivo).
- **Similarità semantica + strutturale.** Per \`SIMILE_A\`/\`ZONA_GEMELLA\`: combinazione di distanza coseno (Qdrant) e match attributi (tipologia, fascia prezzo, indici di zona).
- **Affidabilità/consenso community.** Per \`RECENSISCE\`: peso = funzione di numero di contributi concordi, reputazione del contributor, recency (ranking emergente, come da visione consumer).
- **Feedback dell'utente.** \`PREFERISCE\`/\`SCARTA\` aggiornano dinamicamente i pesi del grafo personale (apprendimento implicito).
- **Freshness.** Tutti i pesi decadono con l'obsolescenza del dato sorgente (es. quotazione vecchia di più semestri pesa meno).

I pesi sono **memorizzati** (materializzati in batch in MySQL) per le componenti costose (tempi di percorrenza) e **ricalcolati a runtime** per le componenti dipendenti dal profilo (rilevanza, feedback).

---

## 6. Fonti dati & connettori (ingestione)

Tutti i connettori sono implementati come **extension point PF4J** (\`DocumentParserExtension\`-like / nuovo \`DataSourceConnectorExtension\`) e orchestrati dal modulo \`batch\`. Ogni connettore è installabile/disinstallabile dal marketplace e configurabile per self-hosting.

| Fonte | Tipo | Cadenza | Licenza/Note | Output nel grafo |
|-------|------|---------|--------------|------------------|
| **OMI — Agenzia delle Entrate** | CSV (quotazioni + perimetri zone) | semestrale | Gratuito, ufficiale | nodi \`ZonaOMI\`, \`Quotazione\` |
| **OpenStreetMap / Overpass** | API/PBF | mensile | ODbL (attribuzione) | nodi \`Servizio\`/\`POI\`, rete stradale |
| **GTFS trasporto pubblico** | feed | mensile | Open per molte città IT | pesi tempo \`SERVITA_DA\`/\`VICINO_A\` |
| **Routing engine** (OSRM/Valhalla/GraphHopper self-host) | servizio | on-demand/batch | Open source, self-host | calcolo tempi di percorrenza |
| **Geocoder** (Nominatim self-host o API) | servizio | per ingest | rispetto policy d'uso | lat/lng degli immobili |
| **Annunci portali** | scraping/feed/import | giornaliera/oraria | **rispettare ToS e robots**; preferire feed/partner/import manuale | nodi \`Immobile\` |
| **Open data comunali** (aria, rumore, sicurezza) | CSV/API | variabile | aperti | attributi qualitativi \`ZonaOMI\` |
| **Contributi community** | UI interna | continua | propria | \`Recensione\`, voti, attributi |
| **Import utente** (CSV/manuale) | upload | on-demand | privata | \`Immobile\`, profilo |

**Note di responsabilità (importanti):**
- Lo scraping dei portali ha vincoli legali e di ToS: il connettore di default privilegia **import manuale / feed ufficiali / partnership**, e lo scraping è un'estensione opzionale a responsabilità del self-hoster.
- Tutto è progettato **local-first**: routing engine, geocoder e LLM girano in locale; nessun dato sensibile (profilo, ancore) lascia l'istanza senza consenso esplicito.
- I connettori validano e normalizzano al boundary (geo plausibile, prezzi coerenti, dedup immobili) prima di scrivere nel grafo.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

### 7.1 MVP (prima release dell'ambito)

| # | Funzionalità | Tipo | Moduli LocalMind coinvolti |
|---|--------------|------|----------------------------|
| 1 | Schema grafo immobiliare (nodi/relazioni §5) come modulo di dominio | CREARE | \`knowledge\`/grafo core, Flyway |
| 2 | Connettore OMI (CSV → ZonaOMI + Quotazione) | CREARE | \`batch\`, plugin PF4J |
| 3 | Connettore POI OSM/Overpass | CREARE | \`batch\`, plugin |
| 4 | Geocodifica + point-in-polygon zona | CREARE | infrastructure adapter |
| 5 | Import immobili (manuale/CSV) + embedding descrizioni | CREARE/RIUSO | \`document\`, Qdrant |
| 6 | Calcolo archi \`VICINO_A\` con tempi reali (routing self-host) | CREARE | \`batch\`, adapter routing |
| 7 | Aggancio prezzo↔OMI e scostamento % | CREARE | grafo core |
| 8 | Ricerca ibrida (filtri grafo + semantica) con scoring base | CREARE/RIUSO | grafo core, Qdrant, \`llm\` |
| 9 | Profilo utente + luoghi ancora + priorità ponderate | CREARE | nuovo dominio \`realestate\` |
| 10 | Spiegazione GraphRAG con citazione nodi/percorsi | CREARE/RIUSO | \`llm\` (Ollama), grafo |
| 11 | UI ricerca + risultati spiegati (feature Angular \`realestate\`) | CREARE | frontend feature lazy |
| 12 | Visualizzazione grafo zona–servizi–immobile (base) | CREARE | frontend |
| 13 | Enum bilingui IT/EN (tipologie, categorie servizi, relazioni) | CREARE | backend+frontend i18n |
| 14 | Migrazioni Flyway (una query per file) | CREARE | \`localmind-app\` |

### 7.2 Evoluzione (release successive)

| # | Funzionalità | Valore |
|---|--------------|--------|
| E1 | Indici di zona derivati (walkability, verde, mix, trend) | scelta consapevole, confronto zone |
| E2 | Zone gemelle e suggerimento collegamenti non evidenti (AI) | scoperta, requisito core del motore |
| E3 | Matrice di confronto multi-criterio + dossier PDF | decisione e consulenza agente |
| E4 | Alert su nuovi immobili matchanti (automation/messaging) | retention, follow-up |
| E5 | Feedback loop che ri-pesa il grafo personale | personalizzazione crescente |
| E6 | Contributi community + ranking emergente + moderazione | conoscenza qualitativa, visione consumer |
| E7 | Stima yield/rendimento locativo per investitori | persona investitore |
| E8 | Analisi foto immobili (multimodale Ollama) | estrazione attributi da immagini |
| E9 | Connettori feed/partner portali, dedup avanzato | copertura dati |
| E10 | Trend temporali prezzi/servizi (zona in miglioramento) | timing d'acquisto |
| E11 | Scenari "what-if" (cambio lavoro → ri-valuta zone) | simulazione decisionale |

### 7.3 Da mantenere (manutenzione continua)

- **Refresh dati**: OMI semestrale, POI mensile, annunci giornalieri; job batch monitorati con metriche (Actuator/Prometheus già presenti).
- **Qualità geo/dedup**: presidio costante sulla geocodifica e sulla deduplica immobili (è il punto più fragile).
- **Allineamento tassonomie**: mappatura tipologie annuncio ↔ tipologie OMI ↔ categorie POI quando le fonti cambiano.
- **Tuning pesi e decadimenti**: revisione periodica delle funzioni di peso sulla base del feedback.
- **Conformità legale connettori**: monitoraggio ToS/licenze delle fonti.
- **Aggiornamento traduzioni IT/EN** delle enum e dei testi UI.

---

## 8. Casi d'uso AI / GraphRAG

L'AI (Ollama locale di default; cloud opzionale con consenso) opera **sul grafo**, combinando navigazione relazionale e semantica, e cita sempre i nodi/percorsi usati.

1. **Ricerca conversazionale multi-vincolo.** "Trilocale entro 350k, max 12 min a piedi dalla metro, scuola elementare ben valutata vicina, prezzo non sopra mercato." → l'AI scompone in filtri+pesi, naviga il grafo, restituisce risultati ordinati con spiegazione.

2. **Spiegazione del prezzo.** "Perché questo costa più del vicino?" → confronto attributi, indici di zona, scostamento OMI, percorso causale sul grafo.

3. **Scoperta di zone alternative (link non evidenti).** "Mostrami zone più economiche con la stessa qualità di vita di Trastevere per la mia famiglia." → archi \`ZONA_GEMELLA\` pesati per profilo.

4. **Dossier di quartiere.** "Com'è vivere in zona X con due bambini?" → sintesi GraphRAG di servizi, scuole, verde, sicurezza, prezzo, con citazioni.

5. **Consulenza investitore.** "Dove conviene comprare per mettere a reddito sotto 200k?" → scostamento prezzo, domanda locativa, servizi che trainano valore.

6. **Confronto guidato.** "Confronta questi 3 immobili per la mia situazione." → matrice multi-criterio pesata sul profilo + raccomandazione motivata.

7. **What-if.** "Se cambio lavoro e mi sposto in zona Y, quali immobili della mia shortlist restano sensati?" → ricalcolo pesi \`RAGGIUNGE\`.

8. **Sintesi della community.** "Cosa dicono i residenti del rumore in questa via?" → aggregazione pesata delle \`Recensione\`.

---

## 9. KPI & metriche di successo

| Categoria | KPI | Target indicativo |
|-----------|-----|-------------------|
| Qualità dato | % immobili geocodificati correttamente | > 97% |
| Qualità dato | % immobili agganciati a zona OMI | > 95% |
| Copertura | n. POI/servizi per zona popolata | sufficiente a coprire categorie chiave |
| Performance | latenza ricerca ibrida (p95) | < 2 s su dataset cittadino |
| Performance | tempo refresh batch archi prossimità | entro finestra notturna |
| Efficacia AI | % risposte con citazione nodi corretta | > 90% |
| Efficacia AI | accuratezza scomposizione query NL | misurata su set di valutazione |
| Engagement | immobili in shortlist per sessione | crescente |
| Engagement | tasso ritorno + ricerche salvate attive | crescente |
| Valore | scostamento prezzo↔OMI mostrato e compreso (survey) | alta comprensione |
| Community | n. contributi qualitativi e copertura zone | crescente |
| Privacy/local | % elaborazioni profilo eseguite in locale | 100% di default |

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---------|---------|-------------|
| **Vincoli legali scraping portali** | Blocco fonte annunci | Default su import/feed/partnership; scraping come estensione opzionale a carico del self-hoster; rispetto ToS/robots |
| **Geocodifica imprecisa** | Prossimità e zona errate | Geocoder self-host + validazione + fallback manuale; escludere nodi senza geo valida |
| **Costo computazionale archi tempo-reale** | Batch lenti | Materializzare i pesi, routing engine self-host, calcolo incrementale solo sui delta |
| **Dati OMI a granularità di zona, non puntuale** | Stima prezzo approssimata | Comunicare il prezzo come *intervallo di mercato di zona*, non perizia; combinare con annunci comparabili |
| **Freschezza disallineata** (OMI semestrale vs annunci giornalieri) | Segnali incoerenti | Pesi con decadimento per freshness; mostrare la data del dato |
| **Privacy dei luoghi della vita** | Dato sensibilissimo | Local-first, AI Ollama locale, nessun invio cloud senza consenso esplicito, cifratura a riposo |
| **Bias/qualità contributi community** | Ranking distorto | Pesi per reputazione/consenso/recency, moderazione, soglie minime |
| **Qualità grafo MySQL su query complesse** | Performance/limiti | Materializzazione, indici, query ibrida; rivalutare datastore a grafo solo se necessario (out of scope ora) |
| **Eterogeneità tassonomie fonti** | Mapping fragile | Layer di normalizzazione versionato, test di regressione su mapping |

---

## 11. Manutenzione & evoluzione

- **Cicli di refresh governati**: scheduler batch con allarmi su fallimenti, metriche Prometheus/Grafana già disponibili nello stack; dashboard di salute dei connettori.
- **Versionamento dello schema grafo**: ogni evoluzione di nodi/relazioni passa da migrazione Flyway (una query per file) e aggiornamento enum bilingui.
- **Estensibilità via plugin**: nuove fonti dati (es. portale regionale, dataset comunale) si aggiungono come plugin PF4J senza toccare il core; pubblicabili sul marketplace.
- **Tuning continuo dei pesi**: revisione trimestrale delle funzioni di decadimento e dei pesi base, guidata dal feedback (\`PREFERISCE\`/\`SCARTA\`) e da KPI.
- **Curatela community**: workflow di moderazione dei contributi qualitativi, gestione reputazione, anti-abuso.
- **Documentazione bilingue**: ogni funzionalità documentata IT/EN (documentation/ + documentazione/); ogni sviluppo tracciato nella cartella \`Sviluppi/\` con nomenclatura datata.
- **Roadmap evolutiva**: MVP → indici di zona → zone gemelle/AI → community/ranking emergente → investimento/yield → multimodale foto → what-if/scenari.

---

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo esistente | Ruolo nell'ambito immobiliare |
|------------------|-------------------------------|
| **\`knowledge\` / grafo core** | Base del motore: schema generico nodi/archi specializzato per immobiliare. Punto di partenza per il grafo pesato. |
| **\`document\`** | Pipeline di estrazione testo (Tika) e ingestione descrizioni annunci; OCR per documenti/planimetrie. |
| **Qdrant (\`vectorstore\`)** | Embedding di descrizioni immobili, recensioni community, descrizioni di quartiere per la ricerca semantica. |
| **\`llm\` + Ollama** | GraphRAG: parsing NL, scoring, spiegazioni, estrazione attributi impliciti; fallback chain multi-provider opzionale. |
| **\`batch\`** | Orchestrazione job di ingestione e calcolo archi pesati (OMI, POI, routing). |
| **\`automation\` + \`messaging\`** | Alert su nuovi immobili matchanti, notifiche, ricerche salvate ricorrenti. |
| **\`marketplace\` + plugin PF4J** | Distribuzione del modulo immobiliare e dei connettori dati come estensioni installabili. |
| **\`auth\`** | Protezione del profilo utente e dei dati sensibili; multi-tenant local-first. |
| **\`agent\`** | Agente immobiliare AI che orchestra ricerca, confronto e dossier in autonomia. |
| **\`common\` (event/analytics)** | Eventi di dominio (immobile ingerito, ricerca salvata) e analytics di utilizzo. |
| **Frontend Angular (\`features/\`)** | Nuova feature lazy \`realestate\`: ricerca, risultati spiegati, visualizzazione grafo, confronto, profilo; Signal store; \`TranslatePipe\` IT/EN; \`language-switcher\`. |
| **MySQL + Flyway** | Struttura del grafo (nodi/archi/pesi materializzati) e migrazioni versionate (una query per file). |

**Nuovo dominio da introdurre:** \`realestate\` in \`localmind-domain\` (model/port-in/port-out/service, zero Spring), wired in \`DomainConfig.java\`, con controller \`/api/v1/realestate/*\`, adapter di persistenza e connettori in infrastructure, e feature Angular dedicata — seguendo esattamente il pattern "Where to Add New Code" della struttura del progetto.

---

### Fonti consultate

- [Transforming Real Estate Search with Knowledge Graphs (Medium, 2026)](https://medium.com/@elevatetrust.ai/transforming-real-estate-search-with-knowledge-graphs-a-technical-deep-dive-afadc50fc137)
- [Leveraging Knowledge Graphs in Real Estate Search (Zillow)](https://www.zillow.com/news/leveraging-knowledge-graphs-in-real-estate-search/)
- [GraphRAG and LightRAG in 2026 (CallSphere)](https://callsphere.ai/blog/vw6g-microsoft-graphrag-knowledge-graph-2026)
- [Building a Real Estate Knowledge Graph (ScrapingAnt)](https://scrapingant.com/blog/building-a-real-estate-knowledge-graph-scraped-entities)
- [How Location Intelligence Is Changing Property Valuation Software (GISuser, 2026)](https://gisuser.com/2026/05/how-location-intelligence-is-changing-property-valuation-software/)
- [Location intelligence for proptech platforms — 2026 NAR report (Local Logic)](https://locallogic.co/blog/location-intelligence-proptech-platforms-2026-nar-report/)
- [Osservatorio del Mercato Immobiliare OMI — Agenzia delle Entrate](https://www.agenziaentrate.gov.it/portale/aree-tematiche/osservatorio-del-mercato-immobiliare-omi)
- [Quotazioni immobiliari OMI — open data (ondata, GitHub)](https://github.com/ondata/quotazioni-immobiliari-agenzia-entrate)
`;
