# Architettura software

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo ambito appartiene al **gruppo enterprise** del motore di knowledge graph universale di LocalMind. Mentre i verticali consumer rispondono alla domanda *"cosa posso fare in questo territorio?"*, l'ambito **Architettura software** risponde alla domanda strutturale che ogni organizzazione tecnologica si pone ogni giorno: *"com'è fatto davvero il nostro sistema, come sono connessi i pezzi, e cosa si rompe se cambio questo?"*. È la rappresentazione, come grafo pesato e navigabile dall'AI, dell'intero patrimonio tecnico: repository Git, servizi e microservizi, API, database, code e bus di messaggi, infrastruttura, pipeline CI/CD, librerie e dipendenze, team proprietari e decisioni architetturali. Il grafo collega questi elementi tra loro e con il resto della conoscenza enterprise (documentazione, processi, ticket, persone) già modellata da LocalMind.

L'ambito è deliberatamente costruito attorno a **standard di settore consolidati** — il **modello C4** per la descrizione architetturale, il **modello a entità di Backstage** (Component, System, API, Resource, Domain) per il catalogo software, **CycloneDX/SBOM** per la distinta dei componenti e delle vulnerabilità, **OpenAPI/AsyncAPI** per i contratti di interfaccia, **OpenTelemetry** per la topologia runtime e **grafi derivati da AST** per la struttura del codice. Tutto resta però **local-first**: il codice sorgente, i segreti e la topologia interna non lasciano mai l'istanza self-hosted senza consenso esplicito, e l'AI di default è Ollama in locale — requisito non negoziabile per chi non vuole far transitare la propria proprietà intellettuale su servizi cloud di terzi.

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema concreto

In ogni organizzazione che produce software oltre una certa soglia di complessità, **la conoscenza dell'architettura vive nelle teste delle persone e in artefatti scollegati**, mai in un'unica rappresentazione interrogabile e sempre aggiornata. Ne derivano problemi ricorrenti e costosi:

- **Erosione del modello mentale.** Nessuno conosce più l'intero sistema. Il diagramma su Confluence è fermo a due anni fa, il README di ogni repo racconta una verità parziale, e la "vera" architettura emerge solo leggendo il codice, i file Terraform e i manifest Kubernetes. La conoscenza si frammenta tra decine di repository, wiki, dashboard di osservabilità e chat.
- **Dipendenze invisibili e accoppiamento occulto.** Le dipendenze più pericolose sono quelle non documentate: il servizio A chiama l'API di B che a sua volta legge una tabella condivisa con C; una libreria interna è usata da quaranta servizi ma nessuno sa quali; un job notturno scrive su una coda consumata da un sistema che si credeva dismesso. Queste relazioni esistono nel sistema reale ma non in nessuna mappa.
- **Analisi di impatto impossibile o costosa ("blast radius").** Prima di modificare un endpoint, deprecare un campo di un evento, aggiornare una libreria con una CVE o migrare un database, la domanda è sempre la stessa: *"chi viene impattato?"*. Oggi la risposta richiede ore di archeologia del codice, riunioni e tribal knowledge, e spesso è sbagliata — con incidenti in produzione come conseguenza.
- **Onboarding lentissimo.** Un nuovo sviluppatore impiega settimane o mesi per costruire il modello mentale del sistema. Non esiste un punto di ingresso che spieghi "questo dominio è fatto di questi servizi, che parlano queste API, possiedono questi dati, sono di questo team".
- **Drift tra architettura intesa e architettura reale.** Le decisioni architetturali (ADR) descrivono l'intenzione; la realtà del codice diverge silenziosamente. Senza un confronto continuo tra il modello "as-designed" e quello "as-built" (derivato da codice, contratti e tracce runtime), le violazioni architetturali si accumulano senza che nessuno se ne accorga.
- **Sicurezza e compliance reattive.** Quando esce una nuova CVE, capire quali servizi includono la libreria vulnerabile, quanto sono esposti e qual è la catena di propagazione è un esercizio manuale. La distinta dei componenti (SBOM) esiste a silos, non collegata al grafo di chi usa cosa.
- **Debito tecnico non misurabile.** L'accoppiamento eccessivo, i cicli di dipendenza, i "god service" con grado di nodo abnorme, le API senza consumatori (codice morto) o con troppi consumatori (single point of failure) sono tutti pattern visibili in un grafo, invisibili altrimenti.

Tutti questi problemi hanno una radice comune: **l'architettura è intrinsecamente un grafo** (componenti come nodi, dipendenze come archi pesati), ma viene gestita con strumenti che non sono grafi — documenti, fogli di calcolo, diagrammi statici, conoscenza orale.

### 1.2 La soluzione LocalMind

LocalMind modella l'intero sistema software come un **grafo pesato di conoscenza tecnica**: i nodi rappresentano repository, servizi, API, database, code, librerie, risorse infrastrutturali, pipeline, team, ADR; gli archi rappresentano relazioni pesate (un servizio *chiama* un'API con una certa frequenza, *dipende da* una libreria a una certa versione, *legge/scrive* una tabella, *è di proprietà di* un team, *è deployato su* un cluster). Su questo grafo opera l'AI in modalità **GraphRAG**: combina la ricerca semantica (Qdrant, sulla documentazione e i commenti) con la navigazione deterministica delle relazioni (MySQL) per rispondere a domande complesse, calcolare l'impatto di un cambiamento e far emergere collegamenti non evidenti.

Il punto architetturale chiave, supportato dalla ricerca più recente (cfr. confronto tra grafi derivati da AST e grafi estratti da LLM, §6), è che **la struttura tecnica va estratta in modo deterministico** dai sorgenti di verità (codice via AST, contratti OpenAPI, file IaC, SBOM, tracce OTel), non "indovinata" dall'LLM. L'LLM legge e ragiona *sul* grafo già costruito con precisione; non è lui a costruirlo. Questo rende le risposte di impact analysis affidabili e riproducibili, non stocastiche.

Il valore differenziale rispetto agli strumenti esistenti:

| Capacità | Wiki / diagrammi statici | Catalogo servizi (es. portali IDP) | LocalMind (grafo + GraphRAG) |
|----------|--------------------------|------------------------------------|------------------------------|
| Aggiornamento | Manuale, sempre obsoleto | Semi-automatico (catalog-info) | Ingestione continua da codice, contratti, tracce |
| Analisi di impatto multi-hop | Assente | Limitata (1 hop) | Percorsi pesati N-hop sul grafo |
| Ragionamento in linguaggio naturale | Assente | Ricerca testuale | GraphRAG: domanda → navigazione → risposta citata |
| As-designed vs as-built | Solo as-designed | Prevalentemente as-designed | Confronto esplicito design/reale (drift) |
| Sicurezza (SBOM/CVE) | Scollegata | Plugin separato | CVE → componenti → servizi → blast radius nel grafo |
| Privacy del codice | Variabile | Spesso cloud | Local-first, AI Ollama in locale |
| Estensibilità a nuovi tipi | Nulla | Schema fisso | Tipi di nodo/arco modulari via plugin PF4J |

### 1.3 Il valore per gli stakeholder

- **Per lo sviluppatore:** può chiedere a parole *"chi consuma questo endpoint?"*, *"se cambio questa colonna cosa si rompe?"*, *"qual è il percorso da questo servizio al database degli ordini?"* e ottenere risposte fondate sul grafo, con citazione dei nodi e dei percorsi.
- **Per l'architetto / tech lead:** ha una vista vivente del sistema, può individuare accoppiamenti eccessivi, cicli di dipendenza, hotspot e divergenze rispetto alle ADR, e simulare l'impatto di refactoring e decomposizioni.
- **Per il team di piattaforma / SRE:** ottiene il blast radius di un incidente o di un deploy, la mappa runtime delle dipendenze (da OTel) e la correlazione tra topologia e affidabilità.
- **Per la sicurezza (AppSec):** dalla comparsa di una CVE risale in pochi secondi a tutti i servizi impattati attraverso il grafo SBOM, prioritizzando la remediation per esposizione reale.
- **Per il management tecnico:** misura il debito architetturale con metriche di grafo, monitora l'ownership e identifica i single point of failure organizzativi e tecnici.
- **Per chi fa onboarding:** parte dal grafo del proprio dominio e lo esplora progressivamente, con l'AI come guida narrativa fondata sulla realtà del codice.

### 1.4 Confini di responsabilità (cosa NON è)

- **Non è un APM/osservabilità in tempo reale.** LocalMind ingerisce la topologia derivata dalle tracce (chi chiama chi) per arricchire il grafo, ma non sostituisce Prometheus/Grafana/Jaeger per metriche, alert e tracing live.
- **Non è una CI/CD né un orchestratore.** Legge i manifest di pipeline e IaC per costruire nodi e relazioni, ma non esegue deploy né gestisce cluster.
- **Non è un sostituto del compilatore o dell'IDE.** L'analisi AST serve a estrarre struttura e dipendenze per il grafo, non a compilare o a fornire autocompletamento.
- **Non esfiltra codice.** Il sorgente resta in locale; gli embedding e l'analisi avvengono on-premise. L'invio a provider cloud è opt-in esplicito e granulare.

---

## 2. Personas & utenti target

| Persona | Descrizione | Obiettivo primario | Esigenza chiave |
|---------|-------------|--------------------|-----------------|
| **Sviluppatore backend/frontend** | Lavora su uno o più servizi | Capire dipendenze e impatto prima di modificare | Risposte rapide su consumatori/produttori, percorsi |
| **Architetto / Tech lead** | Responsabile del disegno di sistema | Vista d'insieme, controllo del drift, refactoring | Confronto as-designed/as-built, metriche di accoppiamento |
| **Platform engineer** | Costruisce e mantiene l'IDP interno | Catalogo servizi vivente e self-hosted | Connettori, schema estendibile, automazione ingestione |
| **SRE / On-call** | Garantisce affidabilità | Blast radius di incidenti e deploy | Topologia runtime, percorsi critici, ownership |
| **Security engineer (AppSec)** | Gestisce vulnerabilità e compliance | Da CVE a servizi impattati | Grafo SBOM, propagazione, prioritizzazione |
| **Engineering manager** | Gestisce team e priorità | Misurare debito e ownership | Metriche di grafo, single point of failure |
| **Nuovo assunto** | Deve costruire il modello mentale | Onboarding rapido sul dominio | Esplorazione guidata, narrazione AI |
| **Data engineer** | Gestisce pipeline e database | Lineage di dati e dipendenze tra dataset | Relazioni read/write, lineage |
| **Amministratore dell'istanza** | Tecnico che gestisce LocalMind | Self-hosting, connettori, privacy | Configurazione, controllo accessi, scheduling ingestione |

Distinzione di ruoli (dominio `auth`): **lettori** (la maggior parte degli sviluppatori) interrogano il grafo in sola lettura tramite l'assistente; **curatori** (architetti, platform) possono correggere/annotare nodi e relazioni e definire le ADR; **amministratori** gestiscono connettori, scheduling e politiche di privacy. La segmentazione guida anche la visibilità: alcuni sottografi (es. infrastruttura di sicurezza) possono essere ristretti.

---

## 3. Requisiti in input

Questa sezione definisce in dettaglio **cosa deve entrare nel sistema** affinché il grafo dell'architettura sia accurato, sempre aggiornato e affidabile per l'analisi di impatto. Si distinguono input di **dominio** (gli artefatti tecnici da cui si deriva il grafo), input **dell'utente** (le domande e le annotazioni) e input di **configurazione/governance**.

### 3.1 Input di dominio: le fonti di verità tecniche

Il principio guida è **derivare, non dichiarare**: ovunque possibile il grafo si costruisce da artefatti già esistenti e versionati, non da inserimento manuale che diventerebbe subito obsoleto. Le fonti primarie:

| Fonte | Cosa fornisce al grafo | Formato / standard |
|-------|------------------------|--------------------|
| **Repository Git** | Repo, moduli, file, struttura del codice, storia, ownership (CODEOWNERS) | Git (clone/fetch locale), CODEOWNERS |
| **Codice sorgente (AST)** | Classi, funzioni, chiamate cross-file, import, ereditarietà, package | Parser AST per linguaggio (Java, TS, Python…) |
| **Manifest di build/dipendenze** | Librerie e versioni, dipendenze dirette/transitive | `pom.xml`, `package.json`/lockfile, `requirements.txt`, `go.mod` |
| **SBOM** | Distinta completa dei componenti + vulnerabilità note | CycloneDX, SPDX, VEX |
| **Contratti API** | Endpoint, schema, operazioni, versioni; eventi e topic | OpenAPI/Swagger, AsyncAPI, gRPC/protobuf |
| **Descrittori di catalogo** | Metadati dichiarati: Component, System, API, Resource, Domain, owner | `catalog-info.yaml` (modello Backstage) |
| **Infrastructure as Code** | Risorse cloud, reti, cluster, code, bucket, secret (riferimenti) | Terraform/HCL, Helm, manifest Kubernetes |
| **CI/CD** | Pipeline, stage, artefatti, ambienti di deploy | GitHub Actions, GitLab CI, Jenkinsfile |
| **Schema database** | Tabelle, colonne, chiavi esterne, indici | DDL, migrazioni (es. Flyway), introspezione JDBC |
| **Topologia runtime** | Chi chiama chi effettivamente, frequenza, latenza | OpenTelemetry (Service Graph Connector, spanmetrics) |
| **Documentazione & ADR** | Descrizioni, razionale, decisioni architetturali | Markdown, ADR (formato MADR), wiki |

Per ogni **artefatto** ingerito il modello richiede un set minimo e uno esteso:

| Categoria | Campi minimi (MVP) | Campi estesi (evoluzione) |
|-----------|--------------------|---------------------------|
| Identità | tipo di nodo, nome canonico, namespace/dominio | alias, descrizione, tag, criticità di business |
| Proprietà | repo/percorso sorgente, team owner | gerarchia organizzativa, on-call, ciclo di vita (active/deprecated) |
| Versione | versione corrente, commit/SHA di riferimento | storico versioni, changelog, data ultimo aggiornamento |
| Relazioni | nodi collegati, tipo di relazione | direzione, cardinalità, contratto associato |
| Provenienza | fonte (AST/contratto/IaC/OTel/manuale), data ingestione | livello di confidenza, metodo di estrazione |

### 3.2 Vincoli di qualità sugli input

Coerentemente con le regole di progetto sull'input validation e sull'immutabilità, il sistema deve **validare al confine**:

- **Identità canonica e deduplica.** Lo stesso servizio referenziato da Git, OpenAPI, OTel e catalog-info deve risolversi in **un unico nodo** tramite chiavi canoniche (es. `domain.system.component`), con regole di matching deterministiche e disambiguazione tracciata.
- **Determinismo dell'estrazione strutturale.** Le relazioni strutturali (chiamate, import, FK, dipendenze) si estraggono con parser deterministici (AST, schema, contratti), non con l'LLM, per garantire risultati riproducibili e affidabili per l'impact analysis.
- **Normalizzazione delle versioni.** Versioni semantiche normalizzate; le dipendenze transitive distinte da quelle dirette.
- **Freschezza e ciclo di vita.** Ogni nodo/arco ha una data di osservazione e una fonte; oltre soglie configurabili l'informazione è marcata "stale" e il suo peso decade (§5). Un nodo non più osservato per N ingestioni è candidato a "deprecato/rimosso".
- **Provenienza e confidenza.** Ogni elemento traccia *da dove* proviene e con quale metodo; le relazioni runtime (osservate) hanno confidenza diversa da quelle dichiarate (catalog-info) o inferite.
- **Sensibilità e segreti.** Mai ingerire valori di secret/credenziali: si modellano solo i *riferimenti* (esiste un secret X usato da Y), mai il contenuto. Filtri anti-segreto obbligatori in ingestione.

### 3.3 Input dell'utente

L'utente fornisce due tipi di input:

1. **Domande in linguaggio naturale** (IT/EN), che l'assistente traduce in interrogazioni sul grafo. Esempi: *"Quali servizi consumano l'API Pagamenti v2?"*, *"Se aggiorno la libreria auth-core alla 3.0, cosa devo testare?"*, *"Mostrami i cicli di dipendenza nel dominio ordini"*, *"Qual è il blast radius della tabella `customers`?"*.
2. **Annotazioni e correzioni** (curatori): aggiungere contesto non derivabile (criticità di business, deprecazioni pianificate, ADR, ownership corretta), confermare o smentire relazioni inferite, collegare un servizio a un processo o a un ticket. Ogni annotazione è immutabile e versionata, con autore e data.

### 3.4 Input di configurazione & governance

- **Scope e connettori:** quali repository/organizzazioni/registry/cluster ingerire, con quali credenziali (in locale), con quale schedulazione.
- **Politiche di privacy:** quali sottografi possono usare provider cloud (default: nessuno), regole di redazione, allowlist/denylist di percorsi.
- **Schema di dominio:** quali tipi di nodo/arco abilitare e con quali pesi (un'organizzazione può attivare solo il sottoinsieme rilevante).
- **Regole architetturali (fitness functions):** vincoli che il grafo deve rispettare (es. "il dominio A non deve dipendere dal dominio B", "nessun ciclo tra bounded context"), per rilevare violazioni.

---

## 4. Flusso dell'attività (step-by-step)

Il flusso si articola in due macro-cicli: il **ciclo di ingestione & costruzione del grafo** (batch/schedulato, prevalentemente automatico) e il **ciclo di interrogazione & ragionamento** (interattivo, guidato dall'utente). Si aggiunge il **ciclo di curatela & governance** (continuo).

### 4.1 Ciclo di ingestione e costruzione del grafo

1. **Registrazione delle fonti.** L'amministratore configura i connettori (repository Git, registry SBOM, endpoint OpenAPI, esportatore OTel, cartelle IaC) con credenziali locali e schedulazione. Riuso del dominio `automation` per la pianificazione e del pattern batch (`localmind-batch`) per l'esecuzione.
2. **Acquisizione locale.** I sorgenti vengono clonati/scaricati in locale (mai inviati altrove). Per ogni fonte si calcola un delta rispetto all'ingestione precedente (commit SHA, hash dei file) per processare solo ciò che è cambiato (ingestione incrementale).
3. **Estrazione deterministica.** Per ciascuna fonte un *extractor* dedicato produce nodi e relazioni candidati:
   - parser AST per la struttura del codice e le chiamate cross-file;
   - parser dei manifest di build per le dipendenze e le versioni;
   - parser SBOM (CycloneDX) per componenti e CVE;
   - parser OpenAPI/AsyncAPI per endpoint, schema, eventi;
   - parser IaC/Kubernetes per risorse infrastrutturali;
   - introspezione/DDL per schema database e foreign key;
   - aggregazione delle tracce OTel per le relazioni runtime "chiama".
4. **Risoluzione delle identità (entity resolution).** I nodi candidati provenienti da fonti diverse vengono fusi sul nodo canonico tramite chiavi e regole deterministiche; i conflitti vengono registrati e, se non risolvibili automaticamente, messi in coda di revisione per i curatori.
5. **Calcolo dei pesi.** Per ogni arco si calcola il peso secondo i criteri configurati (§5): frequenza runtime, numero di punti di chiamata statici, criticità, freschezza, confidenza della fonte. Il peso è un valore derivato e ricalcolabile, mai mutato in-place (immutabilità).
6. **Persistenza.** I nodi e gli archi sono scritti su MySQL (struttura, attributi, pesi) con migrazioni Flyway (una query per file). In parallelo, le descrizioni testuali (documentazione, commenti significativi, descrizioni di endpoint, ADR) vengono "chunked", embeddate (Ollama come modello primario) e indicizzate su Qdrant, con i metadati che puntano al nodo del grafo. Questo è il ponte struttura↔semantica per il GraphRAG.
7. **Validazione delle fitness function.** Dopo l'aggiornamento, le regole architetturali configurate vengono valutate sul grafo; eventuali violazioni (cicli, dipendenze proibite, API senza owner) generano segnalazioni.
8. **Rilevamento del drift e degli orfani.** Si confrontano relazioni *dichiarate* (catalog-info, ADR) con quelle *osservate* (AST, OTel); le divergenze vengono marcate. I nodi non più osservati decadono e vengono proposti per la deprecazione.
9. **Pubblicazione degli eventi.** Le modifiche al grafo emettono eventi di dominio (riuso del `DomainEventPublisherPort`) che alimentano notifiche, ricalcolo metriche e aggiornamento cache.

### 4.2 Ciclo di interrogazione e ragionamento (GraphRAG)

1. **Domanda dell'utente.** Lo sviluppatore pone una domanda in linguaggio naturale (IT/EN) dall'assistente conversazionale (riuso del dominio `llm`/chat).
2. **Comprensione dell'intento.** L'AI classifica la domanda: ricerca puntuale, analisi di impatto, ricerca di percorso, rilevamento pattern, spiegazione architetturale.
3. **Recupero ibrido.** Il sistema combina: (a) **ricerca semantica** su Qdrant per individuare i nodi d'ingresso pertinenti (per descrizione/documentazione); (b) **navigazione deterministica** del grafo su MySQL a partire da quei nodi — vicini, percorsi pesati, sottografi, espansione bidirezionale N-hop.
4. **Costruzione del contesto.** Si seleziona il sottografo rilevante (nodi + archi + attributi + estratti documentali collegati), ordinato per peso e pertinenza, entro il budget di contesto del modello.
5. **Ragionamento dell'AI.** L'LLM ragiona *sul sottografo fornito* per produrre la risposta — l'impact analysis, i consumatori, il percorso, la spiegazione. La struttura è già deterministica: l'LLM la interpreta e la verbalizza, non la inventa.
6. **Risposta con citazioni.** La risposta cita esplicitamente i nodi e i percorsi usati (es. "Servizio A → API Pagamenti v2 → DB ordini"), consentendo all'utente di aprire i nodi nel visualizzatore e verificare.
7. **Esplorazione visuale.** L'utente può passare dal testo al grafo interattivo, espandere progressivamente dai nodi citati, filtrare per tipo/dominio/peso e affinare la domanda.
8. **Azioni a valle (evoluzione).** Da una risposta di impact analysis si possono generare una checklist di test, una bozza di ADR, un ticket o una notifica ai team owner impattati (riuso dei domini `automation`, `messaging`, `agent`).

### 4.3 Ciclo di curatela e governance

1. **Triage delle anomalie.** I curatori esaminano la coda di conflitti di identità, drift e violazioni delle fitness function.
2. **Annotazione.** Aggiungono contesto non derivabile (criticità, deprecazioni, ADR) e confermano/smentiscono relazioni inferite; ogni intervento è versionato.
3. **Definizione delle regole.** Aggiornano le fitness function e i pesi di dominio man mano che l'architettura evolve.
4. **Revisione periodica.** Verificano la copertura dei connettori, la freschezza del grafo e la qualità delle risposte AI, alimentando il miglioramento continuo.

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa MySQL per la **struttura** (nodi, archi, attributi, pesi) e Qdrant per la **semantica** (embedding delle descrizioni collegate ai nodi). Lo schema dei tipi è **modulare ed estendibile** (un'organizzazione attiva solo ciò che le serve), coerente con il modello C4 e con il modello a entità di Backstage.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Fonte primaria |
|--------------|-------------|----------------|
| `Repository` | Repo Git | Git |
| `System` | Insieme coeso di componenti (C4 / Backstage System) | catalog-info, inferenza |
| `Service` / `Component` | Microservizio, applicazione, libreria, sito (C4 Container/Component) | AST, catalog-info |
| `Api` | Interfaccia esposta (REST/gRPC/eventi) | OpenAPI/AsyncAPI |
| `Endpoint` / `Operation` | Singola operazione di un'API | OpenAPI |
| `Event` / `Topic` | Messaggio/argomento su bus/coda | AsyncAPI, IaC |
| `Database` / `Datastore` | DB relazionale, cache, vector store, bucket | IaC, introspezione |
| `Table` / `Dataset` | Tabella/collezione/dataset | DDL, introspezione |
| `Library` / `Dependency` | Dipendenza software (interna/esterna) | manifest build, SBOM |
| `Vulnerability` / `CVE` | Vulnerabilità nota associata a un componente | SBOM/VEX |
| `InfraResource` | Risorsa infrastrutturale (cluster, namespace, queue, secret-ref) | IaC, Kubernetes |
| `Pipeline` / `Deployment` | Pipeline CI/CD, ambiente di deploy | CI/CD, IaC |
| `Domain` / `BoundedContext` | Raggruppamento logico di business (C4 / DDD) | catalog-info, inferenza |
| `Team` / `Group` | Team/gruppo proprietario | CODEOWNERS, catalog-info |
| `Person` | Sviluppatore/maintainer | Git, directory |
| `Adr` / `Decision` | Decisione architetturale | Markdown/MADR |
| `Document` | Documentazione tecnica collegata | dominio `document` esistente |

Molti di questi tipi (`Document`, `Person`, `Team`) si **condividono con altri ambiti enterprise** (documentazione, processi, mail): è il valore del motore universale, lo stesso nodo "Persona" o "Team" collega architettura e organizzazione.

### 5.2 Tipi di relazione (archi, direzionati)

| Relazione | Da → A | Semantica |
|-----------|--------|-----------|
| `CALLS` | Service → Api/Endpoint | Invoca a runtime / staticamente |
| `PROVIDES_API` | Service → Api | Espone l'interfaccia |
| `CONSUMES_API` | Service → Api | Consuma l'interfaccia |
| `DEPENDS_ON` | Component → Library | Dipendenza di build (diretta/transitiva) |
| `READS` / `WRITES` | Service → Table/Dataset | Accesso ai dati (lineage) |
| `PUBLISHES` / `SUBSCRIBES` | Service → Event/Topic | Produzione/consumo eventi |
| `IMPORTS` / `CALLS_FN` | File/Class → File/Class | Relazione strutturale da AST |
| `DEPLOYED_ON` | Service → InfraResource | Collocazione runtime |
| `DEPLOYED_BY` | Service → Pipeline | Pipeline di rilascio |
| `OWNED_BY` | qualsiasi → Team/Person | Proprietà/responsabilità |
| `PART_OF` | Component → System → Domain | Appartenenza gerarchica (C4) |
| `STORES_IN` | Service → Database | Persistenza |
| `AFFECTED_BY` | Component → Vulnerability | Esposizione a CVE |
| `REFERENCES` | Service → Secret-ref/Config | Uso di configurazione/segreto (solo riferimento) |
| `DECIDES` / `GOVERNS` | Adr → Component/Domain | Decisione che vincola un elemento |
| `DOCUMENTED_BY` | qualsiasi → Document | Collegamento alla documentazione |
| `SIMILAR_TO` | nodo ↔ nodo | Affinità semantica (da Qdrant) per suggerire collegamenti |

### 5.3 Criteri di peso degli archi

Il peso è un valore derivato e ricalcolabile (mai mutato in-place) che esprime la **forza/importanza** della relazione e guida sia il ranking nel GraphRAG sia l'analisi di impatto. Fattori combinati:

| Fattore | Contributo al peso | Esempio |
|---------|--------------------|---------|
| **Frequenza runtime** | Più una dipendenza è osservata (OTel), più pesa | A chiama B 10k volte/min ≫ 1 volta/giorno |
| **Molteplicità statica** | N. di punti di chiamata/import nel codice | 50 call site ≫ 1 |
| **Criticità del nodo** | Nodi business-critical alzano il peso degli archi incidenti | DB pagamenti |
| **Cardinalità dei consumatori** | Un'API con molti consumatori → archi più "pesanti" (SPOF) | API auth usata da 40 servizi |
| **Tipo di relazione** | `WRITES` pesa più di `READS`; dipendenza diretta più di transitiva | accoppiamento dati |
| **Confidenza della fonte** | Relazione osservata (OTel) > dichiarata (catalog) > inferita | provenienza |
| **Freschezza** | Il peso decade nel tempo se la relazione non è più osservata | decadimento configurabile |
| **Severità (per CVE)** | `AFFECTED_BY` pesato per CVSS/exploitability | priorità remediation |
| **Feedback dei curatori** | Conferme/smentite manuali aggiustano il peso | apprendimento |

I pesi sono **normalizzati per tipo di relazione** e configurabili per dominio: un'organizzazione può privilegiare la frequenza runtime, un'altra la criticità di business. L'analisi di impatto usa il peso per ordinare il blast radius (i percorsi a peso maggiore = impatto più probabile/grave).

---

## 6. Fonti dati & connettori (ingestione)

L'ingestione è il cuore dell'ambito: il grafo vale quanto la qualità e la freschezza dei suoi connettori. Architetturalmente, ogni connettore è un **extractor** che implementa una porta di dominio (`ArchitectureSourceConnectorPort`) e un'adapter in infrastruttura, idealmente impacchettabile come **plugin PF4J** (riuso del marketplace per distribuire connettori per nuovi linguaggi/strumenti).

### 6.1 Principio di estrazione: deterministico prima, semantico dopo

La scelta progettuale fondante — supportata dalla ricerca 2026 sul confronto tra grafi derivati da AST e grafi estratti da LLM — è che **la struttura tecnica si estrae deterministicamente** (parser AST, schema, contratti, SBOM, tracce), perché l'estrazione via LLM introduce stocasticità, errori di schema e costi variabili, amplificati nei contesti agentici e inaccettabili per l'impact analysis. L'LLM interviene *dopo*, per: ragionare sul grafo, generare embedding semantici delle descrizioni, proporre collegamenti `SIMILAR_TO` e arricchire (con conferma umana) i nodi privi di descrizione. Determinismo per la struttura, intelligenza per il significato.

### 6.2 Catalogo dei connettori

| Connettore | Estrae | Cadenza | Priorità |
|------------|--------|---------|----------|
| **Git** | Repo, file, struttura, storia, CODEOWNERS | A ogni push/schedulato | MVP |
| **AST multi-linguaggio** | Classi, funzioni, import, chiamate cross-file | Incrementale per commit | MVP (Java/TS) |
| **Build/Dependency** | Librerie, versioni, transitorietà | A ogni cambio lockfile | MVP |
| **OpenAPI/AsyncAPI** | API, endpoint, eventi, schema | A ogni cambio contratto | MVP |
| **SBOM (CycloneDX/SPDX/VEX)** | Componenti, CVE, severità | Schedulato / da scanner | Evoluzione vicina |
| **catalog-info (Backstage-like)** | Metadati dichiarati, owner, dominio | A ogni cambio file | MVP |
| **IaC (Terraform/Helm/K8s)** | Risorse infra, code, secret-ref | Schedulato | Evoluzione |
| **CI/CD** | Pipeline, ambienti, artefatti | A ogni cambio workflow | Evoluzione |
| **Schema DB (DDL/JDBC)** | Tabelle, FK, lineage | Schedulato | Evoluzione |
| **OpenTelemetry** | Topologia runtime, frequenza, latenza | Streaming/aggregato | Evoluzione |
| **ADR/Docs** | Decisioni, documentazione (→ dominio `document`) | A ogni cambio | MVP base |

Tutti i connettori operano in locale, con filtri anti-segreto obbligatori e tracciamento di provenienza. Le credenziali risiedono nell'istanza self-hosted; nessun dato transita verso l'esterno senza opt-in.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

Questa sezione mappa concretamente le funzionalità, distinguendo ciò che si **crea ex novo**, ciò che si **sviluppa estendendo** l'esistente e ciò che si **mantiene**. Si riusa l'architettura esagonale: nuovo dominio `architecture` in `localmind-domain`, adapter in `localmind-infrastructure`, controller in `localmind-api`, feature Angular in `localmind-frontend`, job in `localmind-batch`, migrazioni Flyway.

### 7.1 MVP (fondamenta del grafo tecnico)

| # | Funzionalità | Tipo | Moduli LocalMind coinvolti |
|---|--------------|------|----------------------------|
| 1 | **Modello a grafo core** (nodi/archi/pesi tipizzati) condiviso con gli altri ambiti enterprise | Creare | nuovo dominio `graph`/`architecture`, MySQL, Flyway |
| 2 | **API CRUD nodi/relazioni** + query (vicini, percorsi, sottografi) | Creare | `architecture` port/in, `localmind-api` |
| 3 | **Connettore Git + AST (Java/TS)** per struttura e chiamate | Creare | `localmind-batch`, infrastructure adapter |
| 4 | **Connettore Build/Dependency** (Maven/npm) | Creare | infrastructure adapter |
| 5 | **Connettore OpenAPI/AsyncAPI** | Creare | infrastructure adapter |
| 6 | **Connettore catalog-info** (modello Backstage-like) | Creare | infrastructure adapter |
| 7 | **Entity resolution & identità canonica** | Creare | `architecture` service |
| 8 | **Calcolo pesi base** (frequenza statica, confidenza, freschezza) | Creare | `architecture` service |
| 9 | **Indicizzazione semantica** descrizioni → Qdrant collegate ai nodi | Sviluppare (riuso embedding/Qdrant) | `vectorstore`, `EmbeddingConfig` |
| 10 | **GraphRAG di base**: domanda → recupero ibrido → risposta citata | Sviluppare (riuso `llm`/chat) | dominio `llm`, `knowledge` |
| 11 | **Impact analysis 1–N hop** ("chi è impattato da X") | Creare | `architecture` service + GraphRAG |
| 12 | **Visualizzatore grafo interattivo** (nodi/archi/peso, filtri, espansione) | Creare | feature Angular `architecture` |
| 13 | **Ingestione incrementale schedulata** | Sviluppare (riuso `automation`/batch) | `automation`, `localmind-batch` |
| 14 | **i18n IT/EN** di UI, enum (tipi nodo/relazione) e documentazione | Sviluppare (vincolo di progetto) | frontend i18n, enum bilingui |

### 7.2 Evoluzione (intelligenza e copertura)

| # | Funzionalità | Tipo |
|---|--------------|------|
| 15 | **Connettore SBOM (CycloneDX/VEX)** + grafo CVE → blast radius di sicurezza | Creare |
| 16 | **Connettore OpenTelemetry** per topologia runtime e pesi da frequenza reale | Creare |
| 17 | **Connettori IaC, CI/CD, Schema DB** per copertura infrastruttura e lineage dati | Creare |
| 18 | **Fitness functions** (regole architetturali) + rilevamento violazioni | Creare |
| 19 | **Drift detection** as-designed vs as-built (catalog/ADR vs AST/OTel) | Creare |
| 20 | **Metriche di grafo** (grado, accoppiamento, cicli, SPOF, hotspot, debito) | Creare |
| 21 | **Suggerimento collegamenti** `SIMILAR_TO` e relazioni mancanti | Sviluppare (riuso semantica) |
| 22 | **Azioni a valle**: genera checklist test, bozza ADR, ticket, notifica owner | Sviluppare (riuso `automation`/`messaging`/`agent`) |
| 23 | **Plugin PF4J per connettori** (nuovi linguaggi/strumenti) via marketplace | Sviluppare (riuso PF4J/marketplace) |
| 24 | **Agente architetto** che esegue audit periodici e produce report | Sviluppare (riuso `agent`) |
| 25 | **Diff temporale del grafo** ("com'è cambiata l'architettura nell'ultimo trimestre") | Creare |
| 26 | **Export C4/diagrammi** e SBOM aggregato del dominio | Creare |

### 7.3 Manutenzione (continuo)

- Aggiornamento dei parser AST alle nuove versioni dei linguaggi e aggiunta di linguaggi.
- Tenuta degli adapter dei connettori al passo con i formati (OpenAPI, CycloneDX, OTel) in evoluzione.
- Ricalcolo e taratura periodica dei pesi e delle fitness function.
- Pulizia dei nodi orfani/deprecati e gestione del decadimento.
- Aggiornamento costante della documentazione IT/EN e dei log in `Sviluppi/` (vincolo di progetto).

---

## 8. Casi d'uso AI / GraphRAG

I casi d'uso sfruttano la combinazione recupero semantico (Qdrant) + navigazione deterministica (MySQL) + ragionamento dell'LLM sul sottografo. Tutti restituiscono risposte **con citazione dei nodi e percorsi**.

| Caso d'uso | Domanda tipica | Come il grafo risponde |
|------------|----------------|------------------------|
| **Impact analysis / blast radius** | "Cosa si rompe se cambio l'endpoint Pagamenti v2?" | Espansione N-hop dai consumatori, ordinata per peso |
| **Tracciamento consumatori/produttori** | "Chi consuma questo evento? Chi scrive su `orders`?" | Query su `CONSUMES_API`/`WRITES` |
| **Ricerca di percorso** | "Come arriva una richiesta dal gateway al DB ordini?" | Cammino pesato tra due nodi |
| **Sicurezza** | "Quali servizi sono esposti alla CVE-XXXX?" | `Vulnerability` → `AFFECTED_BY` → servizi |
| **Onboarding guidato** | "Spiegami il dominio ordini" | Sottografo del dominio narrato dall'AI |
| **Audit architetturale** | "Ci sono cicli di dipendenza o god service?" | Metriche di grafo + fitness functions |
| **Drift** | "Dove la realtà diverge dalle ADR?" | Confronto dichiarato vs osservato |
| **Lineage dati** | "Da dove arriva il dato cliente in questo report?" | Catena `READS`/`WRITES`/`PUBLISHES` |
| **Pianificazione refactoring** | "Come scorporo questo monolite in bounded context?" | Analisi di accoppiamento e clustering |
| **Suggerimento collegamenti** | "Quali servizi dovrebbero forse condividere una libreria?" | `SIMILAR_TO` + pattern di dipendenza |

---

## 9. KPI & metriche di successo

| Categoria | KPI | Target indicativo |
|-----------|-----|-------------------|
| **Copertura** | % di servizi/repo presenti nel grafo | > 90% del patrimonio |
| **Freschezza** | Ritardo medio tra commit e aggiornamento del grafo | < 1 ora (incrementale) |
| **Accuratezza** | Precisione delle relazioni vs verifica manuale a campione | > 95% per relazioni deterministiche |
| **Impact analysis** | Tempo per ottenere il blast radius di un cambiamento | da ore a < 1 minuto |
| **Adozione** | Query/utente attivo settimana; % team che usano il grafo | crescente, > 60% team |
| **Qualità AI** | % risposte con citazioni corrette e utili (feedback) | > 85% |
| **Sicurezza** | Tempo da pubblicazione CVE a lista servizi impattati | < 5 minuti |
| **Debito** | Trend di cicli di dipendenza / SPOF nel tempo | in calo |
| **Onboarding** | Tempo medio per autonomia di un nuovo assunto | riduzione misurabile |
| **Privacy** | % ingestioni con dati usciti dall'istanza senza consenso | 0% |

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---------|---------|-------------|
| **Estrazione strutturale errata via LLM** | Impact analysis inaffidabile | Estrazione deterministica (AST/schema/contratti); LLM solo per ragionamento e semantica |
| **Identità ambigue / nodi duplicati** | Grafo incoerente | Entity resolution con chiavi canoniche, coda di disambiguazione per i curatori |
| **Grafo obsoleto (stale)** | Risposte sbagliate | Ingestione incrementale frequente, decadimento dei pesi, marcatura "stale" |
| **Scalabilità delle query su MySQL** | Lentezza su grafi grandi | Indici mirati, tabelle di adiacenza/closure, cache (Caffeine), pre-calcolo dei percorsi caldi; rivalutare un graph store solo se necessario (vincolo di progetto) |
| **Fuga di codice/segreti verso il cloud** | Violazione privacy IP | Local-first, AI Ollama default, filtri anti-segreto, opt-in cloud granulare |
| **Sovraccarico cognitivo della visualizzazione** | Grafo illeggibile | Espansione progressiva, filtri per tipo/dominio/peso, viste C4 a livelli |
| **Manutenzione dei connettori** | Erosione della copertura | Connettori come plugin PF4J, test di contratto, monitoraggio della freschezza |
| **Rumore degli archi a basso valore** | Grafo "spaghetti" | Soglie di peso, pruning, decadimento, focus sulle relazioni rilevanti |
| **Allucinazioni AI nelle risposte** | Sfiducia | Risposte ancorate al sottografo con citazioni verificabili; niente affermazioni senza nodo a supporto |

---

## 11. Manutenzione & evoluzione

La manutenzione segue i vincoli di progetto: file piccoli e coesi, immutabilità (pesi e annotazioni ricalcolati/aggiunti, mai mutati in-place), migrazioni Flyway con una sola query, documentazione IT/EN sempre aggiornata e log in `Sviluppi/` con nomenclatura datata.

**Linee di evoluzione:**

1. **Copertura linguaggi e strumenti.** Aggiungere parser AST (Go, C#, Python, Rust…) e connettori (nuovi formati IaC, registry, scanner) come plugin PF4J distribuiti via marketplace.
2. **Dalla descrizione alla predizione.** Dall'impact analysis reattiva alla *predizione* del rischio di un cambiamento e al suggerimento proattivo di refactoring, sfruttando il diff temporale del grafo.
3. **Agente architetto autonomo.** Audit periodici, rilevamento di anti-pattern emergenti, proposta di ADR — riuso del dominio `agent`.
4. **Convergenza con gli altri ambiti enterprise.** Collegare l'architettura a processi, ticket, mail e persone per un grafo enterprise unificato (es. "questo incidente → questo servizio → questo team → questa decisione").
5. **Rivalutazione del datastore.** Se la dimensione del grafo e la complessità delle query lo richiederanno, valutare (fuori dal ciclo attuale) un graph store dedicato, mantenendo l'astrazione di porta che oggi nasconde MySQL.

---

## 12. Integrazione con i moduli LocalMind esistenti

L'ambito non nasce isolato: si innesta sull'architettura esagonale e sui domini già presenti, massimizzando il riuso.

| Modulo / dominio esistente | Ruolo nell'ambito Architettura software |
|----------------------------|------------------------------------------|
| **`knowledge`** | Base del motore a grafo: i tipi nodo/relazione tecnici estendono il dominio knowledge |
| **`document`** | I nodi `Document`/`Adr` riusano l'ingestione, Tika e il chunking esistenti |
| **`llm` / chat** | Motore GraphRAG: fallback chain provider, Ollama default, streaming SSE |
| **`vectorstore` (Qdrant)** | Indicizzazione semantica delle descrizioni collegate ai nodi |
| **Embedding (`EmbeddingConfig`)** | Ollama `@Primary` per gli embedding locali delle descrizioni tecniche |
| **`automation`** | Schedulazione dei connettori e dei ricalcoli del grafo |
| **`localmind-batch`** | Esecuzione dei job di ingestione/estrazione incrementale |
| **`messaging`** | Notifiche ai team owner impattati da cambiamenti/CVE |
| **`agent`** | Agente architetto per audit e azioni a valle |
| **`mcp`** | Esposizione del grafo come tool MCP per altri agenti/IDE; ingestione da tool MCP |
| **`plugin` (PF4J) + `marketplace`** | Connettori e parser distribuiti come plugin installabili |
| **`auth`** | Ruoli lettore/curatore/admin e visibilità dei sottografi sensibili |
| **`common` (eventi, eccezioni)** | Eventi di dominio sul cambiamento del grafo, gestione errori tipizzata |
| **`finetuning`** | (Futuro) fine-tuning di modelli locali sul gergo architetturale dell'organizzazione |
| **Frontend Angular 21** | Feature `architecture` standalone, Signal store, visualizzatore grafo, i18n IT/EN |
| **MySQL + Flyway** | Persistenza struttura/pesi del grafo, migrazioni con una query per file |
| **Spring AI + provider** | Astrazione LLM multi-provider con AI locale di default |

In sintesi, l'ambito **Architettura software** trasforma il patrimonio tecnico di un'organizzazione in un grafo pesato, vivo e interrogabile dall'AI, riusando integralmente l'infrastruttura LocalMind (esagonale, MySQL+Qdrant, Ollama, PF4J) e rispettando i vincoli di local-first, privacy, open source e bilinguismo IT/EN. È il tassello enterprise che dimostra come "un unico motore a grafo, domini come moduli" si applichi non solo alla conoscenza documentale ma alla struttura stessa dei sistemi software.
