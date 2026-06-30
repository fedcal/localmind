# GraphRAG & AI

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il cuore della piattaforma: far ragionare l'AI sul grafo

Questo ambito non è un verticale di dominio (turismo, eventi, knowledge base…): è il **motore trasversale** che dà senso a tutti gli altri. È la capacità definita come *core value* del progetto in `PROJECT.md`: «l'AI deve poter navigare un grafo pesato di conoscenza per rispondere a domande complesse e far emergere collegamenti non evidenti — in qualsiasi dominio, restando local-first. Se tutto il resto fallisce, questo deve funzionare». GraphRAG & AI è quel "questo". Ogni verticale consumer o enterprise produce nodi e archi; questo ambito è il modo in cui l'intelligenza artificiale **legge, attraversa, arricchisce e cita** quel grafo per generare risposte fondate.

Il problema che risolviamo nasce dai limiti, oggi ben documentati, dei due paradigmi di retrieval più diffusi quando vengono usati da soli:

- **La sola ricerca vettoriale (RAG classico) non basta.** LocalMind ha già una pipeline di embedding e ricerca semantica su Qdrant, ottima per trovare i frammenti *più simili* a una domanda. Ma la similarità semantica è cieca rispetto alle **relazioni**. Trova i chunk che "parlano di" un argomento, non i chunk che, messi in catena, *rispondono* a una domanda multi-passo. Domande come «se aggiorno l'API pagamenti, quali servizi e quali procedure ne risentono?» oppure «quali ristoranti vicini all'evento di sabato fanno cucina vegana e accettano cani?» non sono domande di somiglianza: sono domande di **percorso** in un grafo. Il RAG vettoriale puro recupera frammenti scollegati, lascia all'LLM il compito (fragile) di ricucirli, e spesso allucina la connessione mancante.
- **Il solo grafo (query strutturate) non basta.** Un grafo di nodi e archi risponde benissimo a «quali nodi sono collegati a X con relazione Y», ma non capisce il **linguaggio naturale** né la sfumatura semantica della domanda. Non sa che "rilascio in produzione" e "deploy" sono la stessa cosa, né che "posto romantico per cena" mappa su un sottoinsieme di POI. Da solo, il grafo richiede che l'utente conosca già la struttura e la terminologia esatta.

La risposta consolidata nel 2026 — e la nostra — è l'**approccio ibrido GraphRAG**: combinare retrieval semantico (Qdrant) e traversata del grafo pesato (MySQL), fondere i due insiemi di risultati e dare all'LLM un contesto **connesso e tracciabile** invece di un sacchetto di frammenti scollegati. La ricerca vettoriale individua i **punti d'ingresso** giusti nel grafo (gli "ancoraggi" semantici); la traversata espande quei punti lungo le relazioni rilevanti, seguendo i pesi; la fusione e il re-ranking producono un contesto compatto e pertinente; l'LLM genera la risposta **citando i nodi e i percorsi** effettivamente usati.

### 1.2 Le tre capacità che questo ambito porta

L'ambito GraphRAG & AI consegna tre capacità strettamente intrecciate, tutte esplicitamente richieste dalla sezione "AI sul grafo (GraphRAG)" di `PROJECT.md`:

1. **Esplorazione ibrida del grafo (traversal + retrieval semantico).** L'AI risponde a domande complesse combinando relazioni del grafo e ricerca semantica. Le domande multi-hop diventano **traversate**, non tentativi. Il sistema sceglie automaticamente quanta semantica e quanto grafo servono in base alla domanda (routing adattivo).
2. **Suggerimento di collegamenti mancanti / non evidenti.** L'AI non si limita a leggere il grafo: lo **arricchisce**. Propone archi probabili tra nodi che oggi non sono collegati ("questa decisione architetturale sembra spiegare perché esiste questa procedura: le collego?", "questo evento e questo locale ricorrono insieme nelle recensioni: relazione?"). È *link prediction* guidata da LLM + segnali strutturali, sempre mediata dalla curatela umana.
3. **Risposte con citazione di nodi e percorsi.** Ogni risposta è **fondata e tracciabile**: indica i nodi sorgente, il percorso seguito nel grafo, i pesi degli archi attraversati e il livello di confidenza. Se non c'è fondamento, l'AI lo dice invece di inventare. Questo abbatte le allucinazioni e rende la risposta verificabile — requisito tanto consumer (fiducia) quanto enterprise (compliance, audit).

### 1.3 Perché è strategico (e perché ibrido, e perché local-first)

| Esigenza | Limite del solo RAG vettoriale | Limite del solo grafo | Come GraphRAG & AI la soddisfa |
|---|---|---|---|
| Domande multi-hop ("cosa dipende da X?") | Frammenti scollegati, connessione allucinata | Servono query strutturate e terminologia esatta | Ancoraggio semantico + traversata pesata + fusione |
| Comprensione del linguaggio naturale | Buona | Assente | Embedding per mappare la domanda sui nodi giusti |
| Risposte verificabili / anti-allucinazione | Cita chunk, non relazioni | Cita nodi, non testo | Cita **nodi + percorso + pesi + chunk** |
| Far emergere collegamenti non evidenti | Non previsto | Solo collegamenti già inseriti | Link prediction (semantica + struttura + LLM) |
| Domande "globali" / panoramiche | Frammenti locali | Nessuna sintesi | Community detection + sintesi per comunità |
| Privacy del dato | Dipende dal modello | — | **Ollama locale di default**, nessun invio cloud senza consenso |
| Riuso infrastruttura | Vector DB | Spesso richiede Neo4j | **Riuso MySQL (struttura) + Qdrant (semantica)**, niente Neo4j |

Il differenziatore di LocalMind rispetto agli stack GraphRAG mainstream (Microsoft GraphRAG, LightRAG e simili) è la combinazione: **ibrido + local-first + open source + bilingue + senza nuova infrastruttura a grafo**. Gli stack di riferimento spesso assumono LLM cloud e/o un database a grafo dedicato; noi otteniamo la stessa potenza ragionando su MySQL + Qdrant già presenti, con AI Ollama locale di default e fallback cloud solo opzionale e consensuale. Questo è ciò che rende l'esplorazione del grafo praticabile *on-premise*, dove i dati enterprise non possono uscire, e *a costo zero di licenza* per gli scenari consumer community-driven.

## 2. Personas & utenti target

GraphRAG & AI è infrastruttura: i suoi utenti diretti sono in parte gli **utenti finali** dei verticali (che consumano risposte), in parte i **ruoli tecnici e di governance** che lo configurano e lo curano.

| Persona | Profilo | Bisogni primari | Come usa GraphRAG & AI |
|---|---|---|---|
| **Utente finale (consumer/enterprise)** | Cerca risposte, non sa nulla del grafo | Risposte rapide, fondate, in linguaggio naturale | Fa domande in chat; riceve risposte citate; esplora il grafo dai link della risposta |
| **Power user / analista** | Vuole capire *perché* una risposta | Tracciabilità, esplorazione del percorso | Ispeziona nodi/percorsi/pesi citati; pone domande multi-hop complesse |
| **Curatore / knowledge manager** | Governa la qualità del grafo | Validare collegamenti suggeriti, colmare lacune | Lavora la coda dei link suggeriti dall'AI (conferma/rifiuto), monitora confidenza |
| **Sviluppatore / integratore** | Costruisce verticali sul motore | API stabili di retrieval e traversata, estensibilità | Usa le port/in GraphRAG; espone il grafo via MCP; aggiunge strategie via plugin PF4J |
| **Prompt/AI engineer** | Ottimizza qualità delle risposte | Controllo su routing, prompt, soglie, re-ranking | Configura strategie di retrieval, parametri di fusione, template di citazione |
| **IT admin / self-hoster** | Installa e governa on-prem | Local-first, privacy, costi modello, osservabilità | Sceglie provider (Ollama default), imposta policy privacy, sorveglia latenza/costi |
| **Auditor / compliance (enterprise)** | Verifica fondatezza e tracciabilità | Sapere da quali fonti nasce una risposta | Consulta citazioni, percorso, versioni dei nodi |
| **Contributor open source** | Estende il motore | Documentazione chiara, punti di estensione | Contribuisce strategie di retrieval, connettori, miglioramenti GraphRAG |

L'utente faro per l'MVP è la coppia **utente finale che fa domande multi-hop** + **curatore che valida i collegamenti**: insieme chiudono il ciclo "domanda → risposta citata → feedback → grafo migliore".

## 3. Requisiti in input

Questa sezione definisce in dettaglio **cosa deve ricevere il motore GraphRAG** per funzionare. A differenza dei verticali, qui gli input non sono principalmente documenti da ingerire (quelli arrivano già come nodi/archi dagli ambiti di ingestione), ma **la domanda, il grafo su cui ragionare, la configurazione del comportamento AI, il contesto utente e il feedback**. Tutti gli input vanno validati al confine (principio "never trust external data") e trattati in modo immutabile.

### 3.1 La domanda dell'utente (query)

È l'input scatenante. Va normalizzato e analizzato prima del retrieval:

| Elemento | Descrizione | Validazione / trattamento |
|---|---|---|
| Testo della domanda | Linguaggio naturale (IT/EN e oltre) | Lunghezza massima, sanitizzazione, rilevamento lingua |
| Lingua rilevata | Per scegliere embedding e lingua di risposta | Auto-detect con override manuale |
| Tipo/intento di query | Fattuale puntuale, multi-hop, globale/panoramica, esplorativa | Classificata dal router (vedi §4) per scegliere la strategia |
| Entità menzionate | Nodi candidati citati nella domanda | Estratte via NER/LLM, agganciate ai nodi del grafo (entity linking) |
| Filtri espliciti | Tipo di nodo/relazione, dominio, intervallo temporale, area geografica | Schema-validati; applicati come vincoli di traversata |
| Conversazione precedente | Storico per domande di follow-up | Riuso della conversazione (dominio `llm`), finestra limitata |

### 3.2 Il grafo su cui ragionare

Il motore consuma — non produce — la struttura del grafo, fornita dagli ambiti di ingestione e dal motore core:

- **Nodi tipizzati** con attributi, metadati di provenienza, versione, freschezza, autorevolezza e **ACL/visibilità** (chi può vedere il nodo).
- **Archi pesati e direzionati** con tipo di relazione, peso normalizzato (0–1) e componenti del peso ispezionabili (vedi §5 e il documento del motore core).
- **Embedding dei nodi/chunk su Qdrant**, allineati ai nodi su MySQL tramite identificatore comune (UUID con `@JdbcTypeCode(SqlTypes.CHAR)`), così che un risultato semantico possa essere "promosso" a punto d'ingresso della traversata.
- **Indici di traversata** su MySQL (su `source_node_id`, `target_node_id`, `relation_type`, `weight`) per rendere praticabili i percorsi multi-hop senza un DB a grafo dedicato.

### 3.3 Contesto utente, identità e permessi

GraphRAG deve produrre risposte **filtrate per permessi**: un input critico è quindi il contesto di autorizzazione.

- **Identità dell'utente** dal `LocalAuthFilter` esistente (token JWT-like).
- **ACL effettive**: l'insieme di nodi/archi visibili all'utente, ereditato dalle fonti (enterprise) o dalle policy di pubblicazione (consumer).
- **Regola fail-safe**: in caso di dubbio sulla visibilità, prevale la regola più restrittiva; un nodo non autorizzato non deve mai comparire né nella risposta né nelle citazioni.

### 3.4 Configurazione del comportamento AI

Parametri che governano *come* il motore esplora e risponde. Tutti con default ragionevoli e sovrascrivibili (per dominio, per utente, per richiesta):

| Parametro | Cosa controlla | Default suggerito |
|---|---|---|
| Provider LLM e modello | Generazione risposta + estrazione | Ollama locale (`LLM_DEFAULT_PROVIDER`) |
| Modello di embedding | Vettorializzazione query e nodi | Ollama embedding (`@Primary`) |
| Strategia di retrieval | Solo semantica / solo grafo / ibrida / globale | Ibrida adattiva |
| `top_k` semantico | Quanti ancoraggi recuperare da Qdrant | Configurabile (es. 8–20) |
| Profondità di traversata (hop) | Quanti salti dal nodo d'ingresso | Limitata (es. 2–3) per latenza |
| Fan-out massimo per nodo | Quanti vicini espandere per nodo | Limitato per evitare esplosione |
| Soglia di peso minimo | Archi sotto soglia ignorati nella traversata | Configurabile |
| Strategia di fusione/re-rank | Come combinare risultati semantici e di grafo | Reciprocal Rank Fusion + reranker |
| Soglia di confidenza per rispondere | Sotto la quale l'AI dichiara "non so" | Configurabile, alta per enterprise |
| Politica privacy | Cosa può andare a un provider cloud | Default: nulla esce dal locale |
| Template di citazione | Formato delle citazioni nodi/percorsi | Bilingue IT/EN |
| Lingua di risposta | IT/EN o lingua della domanda | Lingua della domanda |

### 3.5 Configurazione del suggerimento di collegamenti (link prediction)

- **Aggressività dei suggerimenti**: quanti archi candidati proporre e con quale soglia di probabilità minima.
- **Tipi di relazione candidabili**: per evitare proposte fuori ontologia.
- **Fonti del segnale**: similarità di embedding, co-occorrenza, pattern strutturali del grafo, inferenza LLM (configurabili e pesabili).
- **Modalità di applicazione**: sempre in coda di curatela (mai auto-applicazione silenziosa nell'MVP).

### 3.6 Feedback (loop di apprendimento)

Input che chiudono il ciclo e migliorano il motore nel tempo:

- **Valutazione della risposta**: pollice su/giù, "fuori contesto", "fonte sbagliata", indicazione della fonte corretta.
- **Validazione dei percorsi**: marcare un percorso come utile/inutile — segnale che alimenta il peso degli archi percorsi.
- **Accettazione/rifiuto dei link suggeriti**: ogni decisione retroagisce su pesi e modello di suggerimento.
- **Segnalazioni di allucinazione/contraddizione**: innescano revisione e abbassamento di confidenza sulle fonti coinvolte.

### 3.7 Regole di validazione sugli input

- La domanda è sanitizzata (lunghezza, contenuto) e mai usata per costruire query non parametrizzate (prevenzione injection sia su SQL sia su prompt).
- I parametri di configurazione sono schema-validati con range espliciti; valori fuori range falliscono fast con messaggio chiaro (IT/EN).
- Il contesto permessi è **sempre** applicato prima della generazione: nessuna risposta può contenere nodi non autorizzati.
- Tutto il feedback è immutabile: non sovrascrive i pesi, genera nuovi eventi/versioni da cui i pesi sono ricalcolati.
- Per default **nessun dato lascia l'infrastruttura locale**; l'uso di provider cloud richiede consenso esplicito e selettivo.

## 4. Flusso dell'attività (step-by-step)

Il flusso descrive l'intero ciclo GraphRAG, dalla domanda alla risposta citata, fino all'arricchimento del grafo e al feedback. È pensato per l'MVP, con i punti di evoluzione indicati.

### Fase A — Comprensione della domanda (query understanding)

1. **Ricezione e normalizzazione.** La domanda arriva dal frontend (riusando il canale chat/SSE esistente) al controller `/api/v1/knowledge/graph/ask` (o equivalente). Si rileva la lingua, si sanitizza il testo, si recupera il contesto conversazionale e il contesto permessi dell'utente.
2. **Classificazione dell'intento (routing adattivo).** Un classificatore leggero (euristiche + LLM locale) stabilisce il **tipo di query** e quindi la strategia: *fattuale puntuale* (più semantica, poca traversata), *multi-hop relazionale* (traversata profonda guidata da ancoraggi), *globale/panoramica* (sintesi per comunità), *esplorativa* (espansione progressiva). Questo è il pattern *Adaptive RAG*: si fa corrispondere la complessità della pipeline alla complessità della domanda, evitando di "pagare" la traversata quando basta la semantica.
3. **Estrazione e agganciamento delle entità (entity linking).** Si individuano le entità citate nella domanda e si mappano sui nodi reali del grafo (per nome, sinonimi del glossario, similarità di embedding). Questi nodi diventano i **punti d'ingresso** della traversata.

### Fase B — Retrieval ibrido

4. **Recupero semantico (ancoraggi).** Si vettorializza la domanda (embedding Ollama) e si interroga **Qdrant** per i `top_k` chunk/nodi semanticamente più vicini, applicando già i filtri di tipo/dominio/permessi. Questi sono gli ancoraggi semantici.
5. **Traversata del grafo (espansione relazionale).** Dai punti d'ingresso (entità agganciate + ancoraggi semantici promossi a nodi), si esegue una traversata pesata su **MySQL**: vicini entro N hop, seguendo gli archi con peso sopra soglia, rispettando fan-out massimo e profondità massima per controllare la latenza. Si raccolgono nodi, archi e i **percorsi** che li collegano alla domanda.
6. **Fusione e re-ranking.** I due insiemi (semantico + grafo) vengono uniti con una tecnica di fusione del ranking (es. Reciprocal Rank Fusion) ed eventualmente passati a un reranker. Si ottiene un **contesto unico, ordinato e connesso**, in cui ogni elemento porta con sé la sua provenienza e il percorso che lo lega alla domanda.
7. **Filtro permessi (gate di sicurezza).** Prima di comporre il prompt, si rimuovono nodi/chunk/percorsi non autorizzati per l'utente. Regola fail-safe: nel dubbio si esclude. Nessun elemento non autorizzato può entrare nel contesto né nelle citazioni.
8. **Budget di contesto.** Si comprime il contesto entro la finestra del modello: si privilegiano i percorsi a peso più alto e i chunk più pertinenti, mantenendo sempre i riferimenti di citazione.

### Fase C — Generazione della risposta citata

9. **Costruzione del prompt fondato.** Si assembla un prompt che contiene la domanda, il contesto connesso (nodi + percorsi + chunk) e istruzioni esplicite: rispondere **solo** sulla base del contesto, **citare** i nodi/percorsi usati, e dichiarare l'incertezza se il contesto è insufficiente.
10. **Generazione (LLM locale di default).** `LlmGatewayService` instrada al provider (Ollama di default, fallback cloud opzionale e consensuale). La risposta viene prodotta, in streaming via SSE quando opportuno.
11. **Verifica e citazione.** Si associano le affermazioni della risposta ai nodi/percorsi sorgente, producendo **citazioni navigabili**: ogni citazione punta a un nodo (con versione, owner, freschezza) e, dove rilevante, mostra il **percorso** seguito e i **pesi** degli archi. Se la confidenza è sotto soglia, l'AI risponde "non ho elementi sufficienti" anziché inventare.
12. **Consegna.** La risposta citata torna al frontend; l'utente può espandere ogni citazione, saltare al nodo, o aprire la **visualizzazione del grafo** centrata sui nodi della risposta.

### Fase D — Arricchimento del grafo (link prediction)

13. **Generazione di collegamenti candidati.** In modo asincrono (o su richiesta), il motore analizza i nodi coinvolti e propone **archi mancanti** ad alta probabilità, combinando: similarità di embedding (nodi vicini ma non collegati), pattern strutturali (chiusura di triangoli, co-occorrenza), e inferenza LLM ("questi due nodi sembrano in relazione di tipo X").
14. **Punteggio e soglia.** Ogni candidato riceve una probabilità e una motivazione; sotto soglia viene scartato.
15. **Coda di curatela.** I candidati sopra soglia finiscono nella coda del curatore, **mai applicati in automatico** nell'MVP. Il curatore conferma, corregge il tipo, o rifiuta.

### Fase E — Feedback e apprendimento

16. **Valutazione utente.** L'utente vota la risposta e, se errata, indica la fonte corretta.
17. **Retroazione sui pesi.** I percorsi che hanno prodotto risposte utili rinforzano il peso dei loro archi; i link confermati in curatela aumentano di peso, quelli rifiutati vengono penalizzati/azzerati. Il calcolo è immutabile (nuova versione del peso).
18. **Adattamento del routing e delle soglie.** Le metriche (utilità, latenza, tasso di "non so") alimentano l'ottimizzazione di soglie e strategie. Il grafo diventa più ricco e i percorsi più affidabili nel tempo (apprendimento dall'uso).

### Diagramma sintetico del flusso

```text
Domanda utente (NL, IT/EN)
   │
   ▼
[A] Query understanding ─ rileva lingua ─ classifica intento (routing) ─ entity linking
   │
   ▼
[B] Retrieval ibrido
   ├─ Semantico (Qdrant, top_k)  ─┐
   ├─ Traversata grafo (MySQL, N hop, peso≥soglia) ─┤─► Fusione (RRF) + re-rank
   │                                                 │
   └────────────────────────► Filtro permessi (fail-safe) ─► Budget di contesto
   │
   ▼
[C] Generazione (Ollama default) ─► Risposta CITATA (nodi + percorso + pesi + chunk)
   │                                   │
   │                                   └─► Visualizzazione grafo centrata sulla risposta
   ▼
[D] Link prediction ─► candidati (semantica+struttura+LLM) ─► coda CURATELA (no auto-apply)
   │
   ▼
[E] Feedback (voto/fonte corretta/validazione percorso) ─► ricalcolo pesi (immutabile) ─► grafo migliore
```

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

GraphRAG & AI è **agnostico rispetto al dominio**: ragiona su qualunque tipo di nodo/relazione definito dal motore core e dai verticali. Tuttavia introduce alcuni **tipi tecnici propri** necessari a rendere l'AI tracciabile, apprendente e governabile. Tutti i tipi sono enum **tradotte IT/EN** verso il frontend, come da vincoli di progetto.

### 5.1 Tipi di nodo specifici dell'ambito

Oltre ai nodi di dominio (che l'ambito consuma), il motore GraphRAG materializza nodi "di servizio" per supportare retrieval, tracciabilità e apprendimento:

| Tipo di nodo | Descrizione | Attributi chiave |
|---|---|---|
| **Chunk (Frammento)** | Segmento di testo vettorializzato, ponte tra grafo (MySQL) e semantica (Qdrant) | embedding_id (Qdrant), nodo padre, posizione, lingua |
| **Query / Domanda** | Domanda posta, conservata per analisi e apprendimento | testo, lingua, intento classificato, utente, timestamp |
| **Answer (Risposta)** | Risposta generata, con i suoi riferimenti | testo, confidenza, provider/modello, voto utente |
| **Citation (Citazione)** | Legame tra una porzione di risposta e il nodo/percorso sorgente | nodo sorgente, percorso, peso, posizione nel testo |
| **Path (Percorso)** | Sequenza di nodi/archi attraversata per rispondere | nodi ordinati, archi, peso complessivo, lunghezza (hop) |
| **Community (Comunità)** | Cluster di nodi densamente connessi, con sintesi | membri, sintesi tematica, livello/risoluzione |
| **Suggested Link (Collegamento suggerito)** | Arco candidato proposto dall'AI in attesa di curatela | nodi, tipo proposto, probabilità, motivazione, stato |
| **Concept / Term (glossario)** | Voce di ontologia per normalizzare entità e sinonimi | definizione, sinonimi, acronimi, lingua |
| **Embedding Anchor** | Aggancio tra entità della domanda e nodo reale (entity linking) | termine, nodo agganciato, confidenza |

### 5.2 Tipi di relazione specifici dell'ambito

| Relazione | Da → A | Significato |
|---|---|---|
| **rappresenta** | Chunk → Nodo di dominio | il frammento appartiene/descrive il nodo |
| **ancora_a** | Query → Nodo (punto d'ingresso) | l'entità della domanda è agganciata a un nodo reale |
| **attraversa** | Path → Arco/Nodo | il percorso include questo arco/nodo |
| **cita** | Answer/Citation → Nodo/Path | la risposta si fonda su questa fonte |
| **ha_generato** | Query → Answer | la domanda ha prodotto la risposta |
| **valutata_come** | Utente → Answer | feedback (utile/non utile/fuori contesto) |
| **suggerisce** | AI → Suggested Link | proposta di arco da validare |
| **appartiene_a_comunità** | Nodo → Community | membership nel cluster |
| **sinonimo_di / definisce** | Concept → Nodo/Termine | normalizzazione ontologica |
| **simile_a** | Nodo ↔ Nodo | vicinanza semantica (peso da similarità embedding) |

### 5.3 Criteri per il peso degli archi

Il peso (0–1) è la **bussola della traversata**: determina quali archi l'AI segue per primi e quali ignora. È una combinazione configurabile di fattori, ognuno ispezionabile e ricalcolabile in modo immutabile (un nuovo calcolo crea una nuova versione, non sovrascrive). Per gli archi tecnici dell'ambito valgono fattori aggiuntivi rispetto a quelli del motore core:

| Fattore | Cosa misura | Effetto sul peso |
|---|---|---|
| **Similarità semantica** | Vicinanza degli embedding tra i nodi | maggiore similarità → peso maggiore (per `simile_a`) |
| **Frequenza di traversata utile** | Quante volte l'arco è in un `Path` che ha prodotto risposte votate utili | l'uso utile rinforza il peso |
| **Validazione umana** | Conferma/rifiuto in curatela di un `Suggested Link` | conferma aumenta, rifiuto azzera/penalizza |
| **Confidenza del suggerimento** | Probabilità stimata dalla link prediction | candidati ad alta probabilità partono con peso maggiore |
| **Autorevolezza/freschezza delle fonti** | Qualità e attualità dei nodi collegati | fonti ufficiali e fresche pesano di più |
| **Feedback sulle risposte** | Voti su risposte che hanno usato l'arco | i voti calibrano il peso |
| **Decadimento temporale** | Età dell'arco/relazione | collegamenti vecchi e inutilizzati decadono |
| **Centralità del nodo** | Importanza strutturale (es. nodo molto connesso/critico) | aumenta il peso delle relazioni coinvolte |

Regole di calcolo:
- I pesi sono **ricalcolabili** in batch (job schedulato via dominio `automation`) e in tempo reale all'arrivo di feedback.
- Il **decadimento temporale** evita che vecchi percorsi restino dominanti; la frequenza d'uso recente lo contrasta.
- Ogni componente resta **ispezionabile**, così l'AI può spiegare *perché* ha seguito un certo percorso ("arco confermato dall'owner, alta similarità, percorso usato 23 volte con esito positivo"). Questa spiegabilità è parte integrante della risposta citata.

## 6. Fonti dati & connettori (ingestione)

GraphRAG & AI **non ingerisce direttamente** dalle fonti esterne: consuma il grafo prodotto dagli ambiti di ingestione (turismo, knowledge base aziendale, ecc.) e dalla pipeline documentale esistente. Le sue "fonti" sono quindi gli **artefatti interni** che alimentano il ragionamento, più i **flussi di feedback** che lo migliorano.

| Fonte interna | Da dove arriva | Ruolo per GraphRAG |
|---|---|---|
| **Nodi e archi di dominio** | Motore core + verticali (consumer/enterprise) | Struttura su cui traversare |
| **Embedding dei chunk/nodi** | Pipeline `DocumentIngestionPipelineService` → Qdrant | Ancoraggi semantici e similarità |
| **Metadati e provenienza** | MySQL (entità JPA) | Citazioni, versioni, freschezza, ACL |
| **Glossario / ontologia** | Configurazione di dominio | Normalizzazione entità (entity linking) |
| **Storico conversazioni** | Dominio `llm` | Follow-up e contesto multi-turn |
| **Feedback utente** | Frontend (voti, fonte corretta) | Apprendimento pesi e routing |
| **Eventi di dominio** | `common`/`event` (SpringDomainEventPublisher) | Trigger di ricalcolo pesi, indicizzazione, link prediction |

Connettori e punti di estensione propri dell'ambito:
- **Strategie di retrieval pluggabili** (solo semantica, solo grafo, ibrida, globale) come estensioni — naturali candidate per un nuovo extension point PF4J accanto a quelli esistenti (`LlmProviderExtension`, `VectorStoreExtension`).
- **Esposizione del grafo come tool MCP**: il motore può pubblicare "interroga il grafo", "espandi i vicini", "trova percorso" come strumenti MCP, rendendo il GraphRAG usabile da agenti esterni (riuso del dominio `mcp`).
- **Sync incrementale degli embedding**: quando un nodo cambia, il chunk e il suo embedding vanno riallineati (idempotenza, niente duplicati).

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

La tabella distingue **CREARE** (nuovo), **SVILUPPARE/ESTENDERE** (su base esistente) e **MANTENERE** (riuso con cura). L'ambito è il *core AI* che si appoggia al motore a grafo e ai domini esistenti.

### 7.1 MVP (primo rilascio utile)

| Funzionalità | Azione | Componenti LocalMind coinvolti |
|---|---|---|
| Orchestratore GraphRAG (pipeline domanda→risposta citata) | CREARE | nuovo servizio dominio in `knowledge`/`agent`, port/in `GraphRagUseCase` |
| Query understanding: lingua + classificazione intento (routing adattivo) | CREARE | servizio dominio + LLM via `LlmGatewayService` (Ollama) |
| Entity linking (aggancio entità della domanda ai nodi) | CREARE | servizio dominio + similarità Qdrant + glossario |
| Retrieval semantico (riuso) | MANTENERE | `QdrantVectorStoreAdapter`, EmbeddingModel Ollama `@Primary` |
| Traversata del grafo pesata su MySQL (vicini, percorsi, N hop) | CREARE | port/out di traversata, repository adapter, indici Flyway (una query/file) |
| Fusione + re-ranking (RRF) dei risultati semantici e di grafo | CREARE | servizio dominio puro |
| Filtro permessi pre-risposta (ACL ereditate, fail-safe) | CREARE | servizio di autorizzazione + `LocalAuthFilter` |
| Generazione risposta con citazione di nodi/percorsi/pesi | CREARE/SVILUPPARE | `LlmGatewayService`, template citazione bilingue, riuso chat/SSE |
| Modello "non so" sotto soglia di confidenza (anti-allucinazione) | CREARE | logica nell'orchestratore |
| API: `ask` (GraphRAG), `expand` (vicini), `path` (percorso), `subgraph` | CREARE | controller `/api/v1/knowledge/graph/*`, DTO, mapper |
| Link prediction base (semantica + struttura) con coda di curatela | CREARE | servizio dominio + UI curatela; nessuna auto-applicazione |
| Visualizzazione grafo centrata sulla risposta (base) | CREARE | feature Angular standalone (lazy), Signal store |
| Feedback su risposta e percorso → ricalcolo pesi | CREARE | eventi dominio + job (`automation`), pesi immutabili |
| Enum tradotte IT/EN (intento, tipo nodo/relazione, stato suggerimento) | MANTENERE | `TranslatePipe`, enum bilingui |

### 7.2 Evoluzioni (rilasci successivi)

| Funzionalità | Azione | Note |
|---|---|---|
| Community detection + sintesi per comunità (domande globali/panoramiche) | CREARE | clustering del grafo (es. Leiden-like) + sintesi LLM; nodi `Community` |
| Link prediction avanzata guidata da LLM (zero/few-shot su triple) | SVILUPPARE | proposte più ricche con motivazione; soglie adattive |
| Strategie di retrieval pluggabili via plugin PF4J | CREARE | nuovo extension point accanto a quelli esistenti |
| Esposizione del grafo come tool MCP per agenti esterni | SVILUPPARE | riuso dominio `mcp` (server WebMVC) |
| Agentic GraphRAG (più passi di ricerca iterativa sul grafo) | CREARE | riuso dominio `agent`; ricerca deep multi-step |
| Spiegazione interattiva del percorso (perché questa risposta) | SVILUPPARE | UI: evidenzia percorso, pesi, fonti |
| Caching dei percorsi/risposte frequenti | SVILUPPARE | Caffeine esistente; invalidazione su update grafo |
| Re-ranker dedicato (cross-encoder locale) | SVILUPPARE | qualità di fusione superiore, sempre local-first |
| Rilevamento contraddizioni tra fonti nelle risposte | CREARE | confronto fonti citate + segnalazione |
| Valutazione automatica qualità risposte (eval offline) | CREARE | dataset di domande/risposte, metriche di fondatezza |
| Time-travel sul grafo (rispondere "com'era") | SVILUPPARE | sfrutta versionamento immutabile dei nodi/pesi |

### 7.3 Da mantenere con cura (rischi noti)

- **Performance traversata su MySQL**: indici mirati, profondità e fan-out limitati, cache; Neo4j resta fuori scope ma rivalutabile se le query lo imporranno.
- **Mapping UUID MySQL** (`@JdbcTypeCode(SqlTypes.CHAR)`) su tutte le nuove entità (Path, Citation, Community, Suggested Link).
- **Boundary tra domini**: l'orchestratore tocca `llm`, `knowledge`, `mcp`, `agent` — usare port/out dedicati, evitare import cross-dominio diretti (vedi `MODULE_BOUNDARIES.md`).
- **Flyway una query per file**: lo schema dei nodi tecnici richiede molte migrazioni piccole e atomiche.
- **Wiring in `DomainConfig`**: i nuovi servizi restano puri (zero Spring), registrati come `@Bean`.
- **Prompt injection / SQL injection**: query parametrizzate e sanitizzazione del prompt; il contesto del grafo non deve poter alterare le istruzioni di sistema.

## 8. Casi d'uso AI / GraphRAG

1. **Domanda multi-hop di dipendenza (enterprise).** «Se aggiorno l'API pagamenti, quali servizi e procedure ne risentono?» → ancoraggio sull'API, traversata `dipende_da`/`consuma`/`documenta`, risposta con elenco dei nodi toccati, relativi owner e **percorso citato**.
2. **Scoperta relazionale (consumer/territorio).** «Cosa faccio sabato sera vicino al centro, con cena vegana e un evento gratuito?» → semantica per intento + traversata su `vicino_a`/`si_svolge_in`/`adatto_a`, itinerario citato con i nodi POI/evento.
3. **Domanda fattuale fondata.** «Qual è la procedura valida per il rilascio?» → recupero della versione **valida** (non l'obsoleta), citazione di documento, sezione, versione, owner e freschezza.
4. **Domanda globale / panoramica.** «Quali sono i temi ricorrenti negli incidenti dell'ultimo trimestre?» → community detection + sintesi per comunità, senza recuperare migliaia di nodi singoli.
5. **Suggerimento di collegamenti mancanti.** L'AI propone «questa decisione (ADR-42) sembra spiegare *perché* esiste questa procedura: le collego?» con motivazione e probabilità, da validare in curatela.
6. **Trova l'esperto / il punto di riferimento.** «Chi sa di autenticazione OAuth nel nostro stack?» → traversata `è_esperto_di` con ranking per autorevolezza e attività recente.
7. **Spiegazione del percorso (explainability).** Su richiesta, l'AI esplicita il percorso seguito e i pesi degli archi: «ho seguito l'arco confermato dall'owner, fonte ufficiale, percorso usato di recente con esito positivo».
8. **Risposta con dichiarazione di incertezza.** Quando il contesto è insufficiente o sotto soglia di confidenza, l'AI risponde «non ho elementi sufficienti nel grafo» invece di allucinare, eventualmente segnalando la lacuna ai curatori.
9. **Esplorazione progressiva.** Dall'esito di una domanda, l'utente espande i vicini di un nodo, filtra per tipo/relazione e naviga il sottografo, riusando il motore di traversata in modalità interattiva.
10. **GraphRAG via agente/MCP.** Un agente esterno usa gli strumenti MCP ("interroga il grafo", "trova percorso") per integrare il ragionamento sul grafo in workflow più ampi, restando local-first.

## 9. KPI & metriche di successo

| Categoria | KPI | Obiettivo / direzione |
|---|---|---|
| Efficacia AI | % risposte valutate utili (pollice su) | alto e crescente |
| Fondatezza | % risposte con citazioni valide; tasso di "non so" appropriato vs allucinazioni | citazioni alte, allucinazioni in calo |
| Multi-hop | % di domande multi-hop risolte correttamente | crescita |
| Qualità retrieval | Precision/recall del contesto recuperato (eval offline) | alto |
| Arricchimento | N. link suggeriti, % accettati in curatela | crescita con alta precisione |
| Apprendimento | Miglioramento utilità risposte nel tempo (uso → pesi) | trend positivo |
| Performance | Latenza end-to-end (p50/p95), profondità media traversata | entro soglie |
| Privacy | % query servite in locale (Ollama) senza invio cloud | massimizzare (≈100%) |
| Adozione | Domande/giorno, utenti attivi, % che esplora il grafo dalle citazioni | crescita |
| Robustezza | Tasso di errori/timeout della traversata su MySQL | basso |

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---|---|---|
| **Allucinazioni dell'LLM** | Sfiducia, risposte errate | GraphRAG con citazioni obbligatorie, "non so" sotto soglia, mostrare percorso/pesi |
| **Performance traversata su MySQL** | Latenza su multi-hop | Indici mirati, profondità/fan-out limitati, cache (Caffeine), RRF efficiente; Neo4j rivalutabile |
| **Esplosione del contesto** (troppi nodi) | Costo/latenza, qualità calante | Budget di contesto, soglie di peso, top_k, re-ranking |
| **Esposizione di dati non autorizzati nelle citazioni** | Grave (privacy/compliance) | Filtro permessi pre-risposta, fail-safe restrittivo, default local-first |
| **Link prediction rumorosa** | Grafo inquinato | Soglie di confidenza, curatela umana obbligatoria, peso premia la validazione |
| **Routing sbagliato dell'intento** | Strategia inadatta, risposte deboli | Fallback verso ibrido completo, apprendimento dalle metriche |
| **Prompt/SQL injection** | Sicurezza | Sanitizzazione, query parametrizzate, separazione istruzioni/contesto |
| **Dipendenza da modello/qualità Ollama locale** | Qualità variabile on-prem | Modelli configurabili, fallback cloud consensuale, eval per scegliere il modello |
| **Boundary violation tra domini** | Debito tecnico | Port/out dedicati, rispetto di `MODULE_BOUNDARIES.md`, servizi puri |
| **Costo di curatela** | Manutenzione insostenibile | Code prioritarie, suggerimenti batch, focus sui nodi più usati |

## 11. Manutenzione & evoluzione

- **Cura continua dei pesi e dei percorsi.** Job schedulati (dominio `automation`) ricalcolano i pesi da feedback e uso, applicano il decadimento temporale e riallineano gli embedding dei nodi modificati. La qualità del retrieval migliora con l'uso (apprendimento dall'uso).
- **Valutazione sistematica (eval).** Mantenere un set di domande/risposte di riferimento e metriche di fondatezza/precisione per misurare le regressioni a ogni evoluzione del motore o cambio di modello, con test JUnit (backend) e Vitest/Playwright (frontend).
- **Versionamento immutabile.** Ogni risposta, citazione, percorso, peso e link suggerito è versionato: la storia è preservata per audit, spiegabilità e apprendimento.
- **Estensibilità governata.** Nuove strategie di retrieval, re-ranker e connettori arrivano come plugin PF4J; l'ontologia tecnica (intento, tipi tecnici) evolve in modo controllato e bilingue.
- **Osservabilità.** Metriche di latenza, profondità, tasso di "non so", % servita in locale e qualità delle citazioni esposte via Actuator/Prometheus; cruscotto per AI engineer e curatori.
- **Documentazione bilingue.** Aggiornamento costante della documentazione IT/EN e tracciamento degli sviluppi nella cartella `Sviluppi/` con nomenclatura datata, come da CLAUDE.md.
- **Roadmap AI.** Da GraphRAG ibrido (MVP) → community/global search → agentic GraphRAG iterativo → re-ranker locale dedicato → time-travel sul grafo, guidata da metriche e domanda reale.

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo / componente | Ruolo nell'ambito GraphRAG & AI |
|---|---|
| **`knowledge` (dominio)** | Casa naturale dell'orchestratore GraphRAG e delle query di grafo; estende il motore core |
| **`llm` + `LlmGatewayService`** | Query understanding, generazione risposte, link prediction; Ollama locale di default, fallback cloud opzionale |
| **Qdrant (`vectorstore`)** | Retrieval semantico, ancoraggi, similarità per link prediction e `simile_a` |
| **MySQL + Flyway** | Struttura del grafo, traversata pesata, nodi tecnici (Path/Citation/Community/Suggested Link); migrazioni atomiche |
| **Pipeline documentale (`document`)** | Produce i chunk/embedding che fanno da ponte semantica↔grafo |
| **`agent`** | Agentic GraphRAG (ricerca iterativa multi-step) e orchestrazione di tool sul grafo |
| **`mcp`** | Espone il grafo come strumenti MCP per agenti esterni, restando local-first |
| **`auth` + `LocalAuthFilter`** | Identità e base per il filtro permessi sulle risposte (gate di sicurezza) |
| **`automation`** | Job schedulati: ricalcolo pesi, decadimento, link prediction batch, riallineamento embedding |
| **`common` (eventi, analytics)** | Eventi di dominio per side-effect (pesi, indicizzazione) e metriche di efficacia |
| **`messaging`** | Pubblicazione dell'assistente GraphRAG in canali esterni |
| **`marketplace` + plugin PF4J** | Strategie di retrieval, re-ranker e connettori installabili (estensibilità) |
| **Chat/SSE esistente** | Canale di risposta in streaming per le risposte citate |
| **Frontend Angular (feature standalone)** | Assistente con citazioni navigabili, visualizzazione del grafo centrata sulla risposta, esplorazione e curatela; i18n IT/EN, Signal store |

L'ambito **non introduce nuova infrastruttura** (niente Neo4j in questo ciclo): riusa MySQL (struttura e traversata) + Qdrant (semantica), rispetta l'architettura esagonale (servizi dominio puri wired in `DomainConfig`), resta local-first con AI Ollama di default, applica il filtro permessi prima di ogni risposta e produce sempre risposte **citate e tracciabili**, interamente bilingui IT/EN. È il motore che rende reale la promessa centrale di LocalMind: far navigare all'AI un grafo pesato di conoscenza per rispondere a domande complesse e far emergere collegamenti non evidenti, in qualsiasi dominio.
