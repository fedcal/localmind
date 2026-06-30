# Compliance & audit

> Documento parte di documentazione/15-ambiti-estensione/ — guida agli sviluppi LocalMind. Data: 2026-06-29.

Questo documento descrive il verticale **enterprise "Compliance & audit"** costruito sul motore di Knowledge Graph universale di LocalMind. L'obiettivo non è costruire l'ennesima piattaforma GRC monolitica e cloud-only, ma istanziare — tramite tipi di nodo, tipi di relazione e moduli installabili — un **grafo pesato norma → controllo → evidenza → asset → rischio**, navigabile dall'AI (GraphRAG), che renda la compliance *dimostrabile* e non solo *dichiarata*. Tutto resta local-first, self-hostable, con AI Ollama di default, riusando MySQL 8.0 (struttura del grafo) e Qdrant (semantica dei requisiti e delle evidenze), senza introdurre un database a grafo dedicato. La sovranità del dato qui non è un valore aggiunto: è un requisito non negoziabile, perché le evidenze di audit contengono i dati più sensibili dell'organizzazione (log di accesso, configurazioni, decisioni, contratti, incidenti).

---

## 1. Cosa risolviamo (problema & valore)

### 1.1 Il problema percepito dall'organizzazione

La compliance enterprise nel 2026 vive una contraddizione strutturale: la pressione normativa è esplosa (DORA in vigore dal 17 gennaio 2025, NIS2, EU AI Act con le obbligazioni sui sistemi ad alto rischio in vigore dal 2 agosto 2026, GDPR, ISO 27001:2022, SOC 2, PCI-DSS 4.0, sovrane settoriali), ma gli strumenti con cui le organizzazioni la gestiscono sono rimasti fermi a checklist, fogli Excel e audit "point-in-time". I problemi concreti che il responsabile compliance vive ogni giorno:

- **Frammentazione norma-controllo-evidenza.** I requisiti normativi vivono nei PDF dei regolatori; i controlli aziendali in un foglio di calcolo o in una piattaforma GRC; le evidenze (screenshot di console cloud, export di configurazioni, policy firmate, verbali, ticket) sono sparse tra SharePoint, mail, ticketing e file server. Nessuno strumento collega in modo *interrogabile* "questo articolo di legge" → "questo controllo che lo soddisfa" → "questa evidenza che dimostra che il controllo opera". La tracciabilità esiste solo nella testa di poche persone.
- **Duplicazione tra framework.** Le stesse evidenze (logging degli accessi, cifratura dei dati, gestione incidenti, audit trail) soddisfano requisiti di NIS2, DORA, AI Act, ISO 27001 e SOC 2 contemporaneamente, ma vengono raccolte da zero per ogni audit. Le ricerche di settore confermano che fino al 60-80% dei controlli si sovrappone tra framework: senza un *crosswalk* (mappa di equivalenze) navigabile, l'organizzazione paga più volte lo stesso lavoro.
- **Audit "fotografia", non "film".** L'audit tradizionale è una verifica puntuale: si raccoglie tutto la settimana prima della visita ispettiva, si dimostra la conformità a una data, poi si torna allo stato precedente. Il 2026 chiede il contrario — *continuous controls monitoring* e *continuous assurance*: la prova che un controllo opera deve essere continua, datata e fresca, non assemblata a posteriori.
- **Evidenze stantie e non tracciabili (evidence staleness).** Una policy approvata due anni fa, uno screenshot di una configurazione che nel frattempo è cambiata, un controllo "verde" su carta ma di fatto non più operante: le evidenze scadono, e senza un sistema che traccia la *freschezza* e il *ciclo di vita* dell'evidenza l'organizzazione scopre il buco solo davanti all'auditor.
- **Gap analysis manuale e cieca.** Capire *dove manca copertura* — requisiti senza controllo associato, controlli senza evidenza, evidenze scadute — è oggi un lavoro manuale di incrocio tabellare, error-prone e mai aggiornato in tempo reale.
- **Onere della dimostrabilità.** Le normative 2026 non chiedono più solo policy, chiedono *prova che le policy funzionino nei processi e nei sistemi*. La parola chiave dei regolatori è **demonstrable**: serve telemetria dei controlli ed evidenza del cambiamento che un auditor possa ricostruire senza congetture, con attribuzione coerente e audit trail a prova di manomissione.
- **Lock-in e sovranità.** Le piattaforme GRC SaaS leader (Drata, Vanta, MetricStream, ServiceNow GRC) sono cloud-only: i dati di compliance più sensibili dell'azienda — che includono le falle, i rischi accettati, gli incidenti — finiscono su infrastruttura di terzi. Per molti settori regolamentati (finance sotto DORA, PA sotto NIS2, sanità) questo è un problema di sovranità che spinge verso il *sovereign cloud* o l'on-premise.
- **Audit trail come bersaglio.** L'audit trail stesso deve essere *tamper-evident*: se chi commette una violazione può anche alterare il log che la registra, l'evidenza non vale nulla. Pochi strumenti GRC trattano l'integrità crittografica della catena di evidenze come requisito di primo livello.

### 1.2 La soluzione LocalMind

LocalMind tratta la compliance come un **grafo pesato di conoscenza normativa e operativa**, non come una checklist. Ogni requisito normativo, controllo, evidenza, asset, rischio, policy, processo e incidente è un **nodo tipizzato**; ogni connessione ("soddisfa", "mitiga", "dimostra", "si applica a", "equivale a", "viola") è un **arco con peso**, dove il peso codifica la *forza di copertura*, la *freschezza dell'evidenza*, il *grado di automazione* del controllo e l'*affidabilità della fonte*. Su questo grafo l'AI naviga (GraphRAG) per rispondere a domande di compliance complesse, generare gap analysis, preparare pacchetti di audit e spiegare *perché* un requisito è (o non è) coperto, citando i nodi e i percorsi usati.

Il valore differenziante si articola su sei assi:

| Asse di valore | Cosa offre LocalMind | Differenza vs GRC SaaS tradizionali |
|---|---|---|
| **Tracciabilità relazionale** | Catena navigabile norma → controllo → evidenza → asset → rischio, interrogabile in linguaggio naturale | Tabelle scollegate; la tracciabilità è ricostruita manualmente per ogni audit |
| **Crosswalk multi-framework** | Un'evidenza raccolta una volta soddisfa N requisiti tramite archi "equivale a"; coverage analysis automatica | Mapping statico, evidenze duplicate per ogni framework |
| **Continuous assurance** | Freschezza dell'evidenza come attributo di primo livello, ricontrollo continuo, alert su evidenze scadute | Audit point-in-time, evidenze assemblate prima della visita |
| **Dimostrabilità & integrità** | Audit trail tamper-evident (hash-chain), attribuzione coerente, citazione dei percorsi dall'AI | Log alterabili, attribuzione non garantita |
| **Sovranità del dato** | Local-first / self-hostable on-premise; le evidenze più sensibili non lasciano il perimetro | Cloud-only, dati di compliance su infrastruttura di terzi |
| **Spiegabilità AI** | Risposte GraphRAG con citazione di norme/controlli/evidenze e dei pesi degli archi | Dashboard a stato verde/rosso senza catena di prova ispezionabile |

### 1.3 Chi ne beneficia e perché conta

- **Il Compliance Officer / CISO** ottiene una vista unica e sempre aggiornata dello stato di copertura per ogni framework, con gap analysis automatica e la capacità di rispondere a una domanda di un regolatore in minuti anziché settimane.
- **L'auditor (interno ed esterno)** riceve un *workspace di collaborazione* dove ogni requisito è già collegato alle sue evidenze datate e verificabili, con audit trail a prova di manomissione: l'audit smette di essere una caccia al documento.
- **Il responsabile di processo / asset owner** sa esattamente quali controlli ricadono sul proprio perimetro e quali evidenze deve mantenere fresche, con notifiche automatiche di scadenza.
- **Il board / la direzione** ottiene accountability dimostrabile (richiesta esplicita da NIS2 e DORA a livello di organo amministrativo) con dashboard di rischio residuo e copertura, ispezionabili e difendibili.
- **L'organizzazione regolamentata** (finance, PA, sanità, energia, manifattura critica) ottiene uno strumento open-source e self-hostable che non espone i propri rischi a terzi, riducendo al contempo drasticamente il costo ricorrente delle piattaforme GRC SaaS.

### 1.4 Allineamento con la visione LocalMind

Questo verticale è una delle istanze **enterprise** più nette del motore di grafo universale descritto in `.planning/PROJECT.md`. Dimostra che lo stesso motore che serve la scoperta del territorio (turismo) può servire la conoscenza regolatoria interna cambiando solo schema dei nodi/relazioni e moduli installati. Riusa direttamente i domini esistenti `document` (ingestione policy/procedure), `knowledge` (motore a grafo), `email`/`calendar` (evidenze e scadenze), `mcp` (connettori verso fonti tecniche), `automation` (raccolta continua di evidenze) e `agent` (agenti di audit). Valida in modo particolarmente stringente i vincoli **local-first**, **privacy enterprise** e **AI Ollama di default**, perché qui il dato è regolamentato per definizione e non può uscire dal perimetro senza consenso esplicito.

---

## 2. Personas & utenti target

| Persona | Profilo | Obiettivi | Bisogni dal sistema |
|---|---|---|---|
| **Compliance Officer / GRC Manager** | Responsabile della conformità multi-framework | Garantire e dimostrare copertura, ridurre duplicazioni, anticipare i gap | Crosswalk, gap analysis automatica, dashboard di copertura, generazione pacchetti audit |
| **CISO / Security Manager** | Responsabile sicurezza e rischio ICT | Collegare controlli a rischi e asset, dimostrare resilienza (DORA/NIS2) | Grafo rischio-controllo-asset, telemetria controlli, integrazione SIEM |
| **Auditor interno** | Conduce verifiche periodiche | Pianificare audit, raccogliere evidenze, tracciare findings e remediation | Workspace audit, campionamento, tracciamento findings, audit trail immutabile |
| **Auditor esterno / certificatore** | Valida la conformità per ente terzo | Verificare evidenze in autonomia, fidarsi dell'integrità della catena | Accesso in sola lettura, evidenze datate e verificabili, export firmato |
| **Asset / Process Owner** | Responsabile operativo di un sistema o processo | Sapere cosa deve presidiare, mantenere fresche le evidenze | Vista per asset, task di rinnovo evidenze, notifiche di scadenza |
| **Data Protection Officer (DPO)** | Responsabile privacy (GDPR) | Mappare trattamenti, basi giuridiche, DPIA, data flow | Nodi trattamento/finalità, registro trattamenti, collegamento a evidenze |
| **Risk Manager** | Gestisce il registro rischi | Collegare rischi a controlli e rischio residuo | Heatmap di rischio, archi mitiga/accetta, scenari what-if |
| **Membro del board / Direzione** | Accountability di vertice | Avere prova difendibile dello stato di conformità | Dashboard executive, attestazioni, report di sintesi |
| **Sviluppatore / integratore** | Costruisce connettori sopra LocalMind | Automatizzare la raccolta di evidenze tecniche | API grafo, plugin PF4J, MCP, SDK, documentazione IT/EN |

Persona primaria dell'MVP: **Compliance Officer** (lato consumo e gap analysis) e **Auditor interno** (lato verifica e raccolta evidenze). Senza la prima il grafo non ha un proprietario; senza il secondo le evidenze non vengono validate. L'MVP deve chiudere il ciclo "definisco i framework → mappo i controlli → collego le evidenze → misuro la copertura → preparo l'audit".

---

## 3. Requisiti in input

Questa sezione definisce in dettaglio cosa il sistema deve ricevere, validare e ingerire per funzionare. Gli input si dividono in **input di configurazione/conoscenza** (cosa è la compliance per questa organizzazione) e **input operativi/di evidenza** (la prova che i controlli operano). Coerentemente con le regole di progetto, ogni input va validato al confine del sistema (validazione schema, fail-fast, nessun dato esterno fidato a priori).

### 3.1 Input di conoscenza normativa (framework e requisiti)

| Input | Formato/fonte | Contenuto atteso | Validazione |
|---|---|---|---|
| **Catalogo framework** | Import strutturato (JSON/CSV/YAML) o template precaricato | Identificativo framework (ISO 27001:2022, SOC 2, NIS2, DORA, GDPR, AI Act, PCI-DSS, NIST CSF/800-53), versione, lingua, autorità emittente | Schema obbligatorio; versione esplicita; framework duplicato rifiutato |
| **Requisiti normativi** | PDF/normativa testuale + import strutturato | Articolo/clausola/control ID, testo del requisito, categoria, obblighi, riferimenti incrociati | ID univoco per framework; testo non vuoto; lingua IT/EN |
| **Crosswalk / mappature di equivalenza** | Import (es. Secure Controls Framework, CSA CCM) o definite a mano | Coppie requisito↔requisito tra framework, con grado di equivalenza (esatta/parziale) | Coppie valide tra requisiti esistenti; grado in enum |
| **Tassonomie di rischio** | Import o predefinite | Categorie di rischio, scale di probabilità/impatto, soglie di accettazione | Scala coerente; soglie numeriche valide |

Note di dettaglio:
- I requisiti normativi vanno **versionati**: una nuova versione di un framework (es. ISO 27001:2013 → 2022) deve creare nuovi nodi requisito collegati ai precedenti tramite arco "sostituisce", non sovrascrivere — coerente con la regola di immutabilità del progetto.
- L'ingestione di un PDF normativo passa per il dominio `document` (Tika/OCR), poi un passo di *segmentazione in requisiti* (chunking semantico per articolo/clausola) e di *embedding* su Qdrant per la ricerca semantica del requisito.
- Per i framework più diffusi LocalMind fornisce **template precaricati** (catalogo dei controlli + crosswalk di base) come modulo installabile, in modo che l'organizzazione non parta da zero.

### 3.2 Input di configurazione dell'organizzazione (controlli, asset, processi)

| Input | Formato/fonte | Contenuto atteso | Validazione |
|---|---|---|---|
| **Catalogo controlli interni** | UI o import | ID controllo, descrizione, owner, tipo (preventivo/detettivo/correttivo), frequenza, grado di automazione, stato | Owner esistente; frequenza in enum; stato in enum |
| **Inventario asset** | Import (CMDB) o connettori | Sistemi, applicazioni, microservizi, database, repository, infrastruttura, fornitori | ID univoco; criticità valorizzata |
| **Registro processi** | UI o import | Processi aziendali, responsabili, criticità | Responsabile esistente |
| **Anagrafica persone/ruoli** | Import (LDAP/HR) o connettori | Persone, ruoli, responsabilità (RACI) | Email valida; ruolo in tassonomia |
| **Policy e procedure** | Upload documentale | Documenti di policy con versione, data approvazione, owner, scadenza | Versione e data obbligatorie; formato supportato da Tika |
| **Registro trattamenti (GDPR)** | UI o import | Trattamenti, finalità, basi giuridiche, categorie dati, data flow, DPIA | Base giuridica in enum; finalità non vuota |
| **Registro rischi** | UI o import | Rischi, probabilità, impatto, owner, strategia (mitiga/accetta/trasferisci) | Valori entro scale definite |

### 3.3 Input operativi e di evidenza (il cuore della dimostrabilità)

Sono gli input più critici e frequenti. Ogni **evidenza** è un nodo con metadati ricchi e va trattata come dato immutabile e datato.

| Input evidenza | Fonte tipica | Metadati obbligatori | Validazione |
|---|---|---|---|
| **Screenshot / export di configurazione** | Console cloud, IdP, firewall | Data raccolta, fonte, controllo collegato, raccoglitore, hash | Hash calcolato all'ingestione; data ≤ ora corrente |
| **Log e telemetria** | SIEM, sistemi, MCP/connettori | Periodo coperto, sistema, controllo, integrità | Periodo coerente; firma/hash |
| **Policy/procedure firmate** | Document store | Versione, approvatore, data, scadenza | Approvatore valido; scadenza futura |
| **Attestazioni** | Workflow di attestazione | Attestante, data, oggetto, esito | Attestante autorizzato |
| **Verbali, ticket, change record** | Ticketing, mail, calendar | Riferimento, data, attori, esito | Riferimento risolvibile |
| **Risultati di test/scansioni** | Tool di sicurezza, audit | Tool, data, target, findings | Findings strutturati |
| **Evidenze di incidente** | Incident response | Timeline, classificazione, notifica regolatore | Timeline coerente con SLA normativo |

Requisiti trasversali sulle evidenze:
- **Freschezza (evidence freshness).** Ogni tipo di evidenza ha una *validità temporale* configurabile (es. uno screenshot di configurazione scade in 90 giorni, una policy in 12 mesi). Il sistema deve calcolare e mantenere lo stato di freschezza e marcare le evidenze scadute.
- **Integrità (tamper-evidence).** All'ingestione si calcola un hash crittografico; le evidenze entrano in una **catena di hash** (hash-chain / Merkle) che rende rilevabile qualsiasi alterazione successiva. Questo è un requisito esplicito delle normative 2026 (audit trail a prova di manomissione).
- **Attribuzione.** Ogni evidenza registra chi/cosa l'ha prodotta e raccolta (umano o connettore automatico), con timestamp affidabile.
- **Catena di custodia.** Le modifiche di stato (raccolta → validata → scaduta → archiviata) sono append-only sull'audit trail, mai mutazioni in-place.

### 3.4 Input di automazione e connettori

| Input | Fonte | Descrizione |
|---|---|---|
| **Configurazione connettori** | UI/settings | Endpoint, credenziali (in secret manager, mai hardcoded), pianificazione raccolta |
| **Regole di raccolta continua** | Modulo automation | Trigger temporali/eventi che attivano la raccolta di evidenze |
| **Mapping evidenza→controllo** | UI/regole | Quale evidenza dimostra quale controllo, con eventuale logica di validazione automatica |

### 3.5 Input di interrogazione (utente e AI)

- **Query in linguaggio naturale** verso il GraphRAG ("Siamo coperti sull'art. X di DORA? Quali evidenze e quanto sono fresche?").
- **Filtri** per framework, dominio di controllo, asset, owner, stato di copertura, freschezza.
- **Parametri di audit** (perimetro, periodo, framework target) per generare un pacchetto di audit.

### 3.6 Vincoli di validazione e qualità del dato (regole di progetto)

- Validazione schema obbligatoria su ogni import; **fail-fast** con messaggi chiari e bilingui IT/EN.
- Tutte le **enum** (tipo controllo, stato evidenza, base giuridica, grado di equivalenza, strategia di rischio) tradotte IT/EN e reindirizzate al frontend in base allo switch lingua.
- Nessun dato esterno fidato a priori: i risultati dei connettori vanno normalizzati e validati prima di diventare nodi/evidenze.
- **Immutabilità**: requisiti, evidenze e voci di audit trail non si modificano; si creano nuove versioni collegate.
- **Privacy**: gli input possono contenere dati personali/regolamentati; l'invio a provider LLM cloud è bloccato di default e richiede consenso esplicito per perimetro.
- **Migrazioni Flyway** con una sola query per file per ogni nuova tabella/colonna del modello a grafo della compliance.

---

## 4. Flusso dell'attività (step-by-step)

Il flusso descrive il ciclo di vita completo, dalla configurazione iniziale alla preparazione dell'audit e al monitoraggio continuo. È organizzato in fasi; ogni fase indica attore, azione di sistema, output e dominio LocalMind coinvolto.

### Fase 0 — Setup del programma di compliance

1. **Selezione dei framework target.** Il Compliance Officer sceglie i framework applicabili (es. ISO 27001:2022 + SOC 2 + DORA). Il sistema istanzia, dai template precaricati o da import, i **nodi requisito** per ciascun framework e gli archi di crosswalk "equivale a" tra requisiti sovrapposti. *(domini: `knowledge`, `document`)*
2. **Definizione del perimetro.** Si importano/dichiarano asset, processi, persone e trattamenti GDPR. Diventano **nodi** collegati da archi "responsabile di", "si applica a". *(domini: `knowledge`, `mcp` per CMDB)*
3. **Definizione del catalogo controlli.** Si crea/importa il catalogo dei controlli interni e si tracciano gli archi "soddisfa" tra controllo e requisito (un controllo può soddisfare più requisiti di più framework — è qui che il crosswalk paga). *(dominio: `knowledge`)*
4. **Configurazione delle policy di freschezza** per ciascun tipo di evidenza e delle regole di raccolta automatica. *(domini: `automation`, settings)*

### Fase 1 — Ingestione della conoscenza normativa

5. Upload dei testi normativi/PDF → estrazione testo (Tika/OCR) → **segmentazione in requisiti** per articolo/clausola → embedding su Qdrant. *(domini: `document`, `knowledge`)*
6. L'AI propone una **bozza di mappatura** requisito→controllo esistente e requisito→requisito (crosswalk) basata su similarità semantica; il Compliance Officer valida o corregge (human-in-the-loop). Gli archi accettati ottengono peso più alto di quelli solo suggeriti. *(domini: `knowledge`, `agent`)*

### Fase 2 — Raccolta delle evidenze

7. **Raccolta automatica (continua).** I connettori (cloud, IdP, SIEM, repository, ticketing via MCP) raccolgono periodicamente evidenze secondo le regole definite; ogni evidenza diventa un **nodo evidenza** con metadati, hash e arco "dimostra" verso il controllo. *(domini: `automation`, `mcp`)*
8. **Raccolta manuale.** L'asset owner carica evidenze (screenshot, verbali, attestazioni) dalla UI; il sistema calcola hash, data e stato iniziale. *(domini: `document`, `knowledge`)*
9. **Validazione dell'evidenza.** Una regola automatica o un revisore valida l'evidenza (stato → validata). Lo stato è scritto in append-only sull'audit trail con attribuzione. *(domini: `knowledge`, automation)*
10. **Indicizzazione integrità.** L'evidenza viene inserita nella **hash-chain** dell'audit trail; qualsiasi alterazione successiva sarà rilevabile. *(infrastruttura: persistence + servizio integrità)*

### Fase 3 — Calcolo della copertura e gap analysis

11. Il motore calcola, per ogni requisito, lo **stato di copertura** in funzione di: presenza di controlli "soddisfa", presenza di evidenze "dimostra" valide, freschezza delle evidenze, grado di automazione del controllo. È il *peso aggregato* dei percorsi norma→controllo→evidenza. *(dominio: `knowledge`)*
12. La **coverage analysis** identifica i gap: requisiti senza controllo, controlli senza evidenza, evidenze scadute, controlli falliti. Produce una lista prioritizzata per rischio. *(domini: `knowledge`, `common`/analytics)*
13. L'AI genera una **gap analysis narrata** in linguaggio naturale con citazione dei nodi/percorsi e suggerimenti di remediation. *(domini: `agent`, `llm` con Ollama di default)*

### Fase 4 — Remediation

14. Per ogni gap si crea un **task di remediation** (nuovo controllo, evidenza mancante, policy da aggiornare) assegnato a un owner, con scadenza tracciata su `calendar`. *(domini: `automation`, `calendar`)*
15. Alla chiusura del task, la nuova evidenza richiude il ciclo (torna alla Fase 2) e la copertura si ricalcola. Ogni passaggio è registrato sull'audit trail.

### Fase 5 — Preparazione e conduzione dell'audit

16. **Pianificazione audit.** L'auditor definisce perimetro, periodo e framework. Il sistema genera l'**audit plan** e, opzionalmente, un campionamento delle evidenze. *(domini: `knowledge`, `agent`)*
17. **Generazione del pacchetto di audit (audit package).** Per ogni requisito nel perimetro, il sistema raccoglie automaticamente la catena norma→controllo→evidenza con date, hash e attribuzioni, ed esporta un dossier firmato (PDF/JSON) verificabile in modo indipendente. *(domini: `knowledge`, `document`, `common`/backup)*
18. **Workspace di collaborazione con l'auditor esterno.** Accesso in sola lettura al perimetro, con possibilità di registrare richieste, osservazioni e findings. Ogni interazione è tracciata. *(domini: `auth`, `knowledge`)*
19. **Tracciamento findings.** I findings dell'auditor diventano nodi collegati ai requisiti/controlli interessati, con stato e remediation associata (rientra in Fase 4).

### Fase 6 — Monitoraggio continuo (continuous assurance)

20. Il sistema esegue in background il **continuous controls monitoring**: ricontrolla freschezza delle evidenze, riattiva i connettori, rivaluta la copertura e genera **alert** su evidenze scadute, controlli falliti, nuovi requisiti (es. aggiornamento normativo) o scadenze imminenti. *(domini: `automation`, `messaging`, `calendar`)*
21. **Dashboard live** di copertura e rischio residuo per framework, dominio, asset e owner, sempre aggiornata. *(frontend: feature compliance)*
22. **Aggiornamento normativo.** Quando una nuova versione di un framework viene ingerita, l'AI evidenzia i requisiti nuovi/modificati e i controlli/evidenze impattati, aprendo automaticamente i gap. *(domini: `knowledge`, `agent`)*

### Diagramma sintetico del ciclo

```text
 [Framework/Requisiti] --soddisfa--> [Controlli] --dimostra--> [Evidenze]
        |                                |                          |
   si applica a                      mitiga                    freschezza+hash
        v                                v                          v
     [Asset/Processi] <---------- [Rischi] <------ coverage & gap analysis (AI/GraphRAG)
        |                                                            |
        +--------- audit package / continuous monitoring -----------+
```

---

## 5. Modello a grafo (tipi di nodo, tipi di relazione, criteri di peso)

Il modello riusa il motore a grafo universale (MySQL per struttura, Qdrant per semantica) istanziando tipi specifici del dominio compliance. Tutti i tipi sono estendibili per schema modulare.

### 5.1 Tipi di nodo

| Tipo di nodo | Descrizione | Attributi chiave | Indicizzazione semantica (Qdrant) |
|---|---|---|---|
| `Framework` | Standard/normativa | id, versione, autorità, lingua | No (metadato) |
| `Requisito` | Articolo/clausola/control ID | id, testo, categoria, obblighi, versione | Sì (testo del requisito) |
| `Controllo` | Controllo interno | id, tipo, frequenza, automazione, owner, stato | Sì (descrizione) |
| `Evidenza` | Prova che un controllo opera | tipo, data, fonte, hash, stato, validità | Sì (contenuto testuale, se presente) |
| `Asset` | Sistema/app/servizio/dato | tipo, criticità, owner | Sì (descrizione) |
| `Processo` | Processo aziendale | criticità, owner | Sì |
| `Rischio` | Voce del registro rischi | probabilità, impatto, residuo, strategia | Sì |
| `Policy` | Documento di policy/procedura | versione, approvatore, scadenza | Sì (testo) |
| `Trattamento` (GDPR) | Trattamento dati personali | finalità, base giuridica, categorie dati | Sì |
| `Persona/Ruolo` | Owner/responsabile | ruolo, RACI | No |
| `Fornitore` | Terza parte (DORA/NIS2 supply chain) | criticità, contratto | Sì |
| `Audit` | Verifica pianificata/condotta | perimetro, periodo, esito | No |
| `Finding` | Rilievo d'audit | severità, stato, requisito impattato | Sì |
| `Incidente` | Evento di sicurezza/violazione | timeline, classificazione, notifica | Sì |
| `RemediationTask` | Azione correttiva | owner, scadenza, stato | Sì |
| `AuditTrailEntry` | Voce immutabile del registro eventi | attore, azione, timestamp, hash prev/curr | No (append-only, hash-chain) |

### 5.2 Tipi di relazione (archi)

| Arco (direzione) | Significato | Esempio |
|---|---|---|
| `SODDISFA` (Controllo → Requisito) | Il controllo copre il requisito | Controllo "MFA su accessi" soddisfa A.5.17 ISO |
| `DIMOSTRA` (Evidenza → Controllo) | L'evidenza prova l'operatività | Log MFA dimostra "MFA su accessi" |
| `EQUIVALE_A` (Requisito ↔ Requisito) | Crosswalk tra framework | A.8.2 ISO equivale a CC6.1 SOC 2 |
| `SOSTITUISCE` (Requisito → Requisito) | Versione successiva | ISO 2022 sostituisce ISO 2013 |
| `SI_APPLICA_A` (Requisito → Asset/Processo) | Ambito di applicazione | DORA art. X si applica a "core banking" |
| `MITIGA` (Controllo → Rischio) | Il controllo riduce il rischio | "Backup cifrato" mitiga "perdita dati" |
| `MINACCIA` (Rischio → Asset) | Il rischio insiste sull'asset | "Ransomware" minaccia "file server" |
| `RESPONSABILE_DI` (Persona → Controllo/Asset) | Ownership/RACI | Owner del controllo |
| `GOVERNA` (Policy → Controllo/Processo) | La policy disciplina | Policy accessi governa controlli IAM |
| `VIOLA` (Incidente → Requisito/Controllo) | L'incidente rappresenta una violazione | Data breach viola GDPR art. 32 |
| `RILEVA` (Finding → Requisito/Controllo) | Il rilievo riguarda | Finding su "logging insufficiente" |
| `CORREGGE` (RemediationTask → Finding/Gap) | L'azione chiude il gap | Task corregge finding |
| `DIPENDE_DA` (Asset → Asset/Fornitore) | Dipendenza tecnica/supply chain | Microservizio dipende da fornitore cloud |
| `REGISTRA` (AuditTrailEntry → qualsiasi nodo) | Tracciamento evento | Entry registra "validazione evidenza" |

### 5.3 Criteri per il peso degli archi

Il peso (0–1, normalizzato) è il cuore della "compliance dimostrabile": codifica *quanto* una relazione è forte e affidabile, e alimenta il calcolo della copertura e il ranking GraphRAG.

| Arco | Fattori che determinano il peso | Logica |
|---|---|---|
| `SODDISFA` | Grado di copertura (totale/parziale), validazione umana vs solo suggerito dall'AI, specificità del controllo | Copertura totale validata → peso alto; suggerimento AI non confermato → peso basso |
| `DIMOSTRA` | **Freschezza** dell'evidenza (decadimento temporale), stato (validata/scaduta), grado di automazione della raccolta, integrità verificata | Evidenza fresca, validata, raccolta automaticamente e integra → peso massimo; evidenza scaduta → peso che decade verso 0 |
| `EQUIVALE_A` | Grado di equivalenza (esatta/parziale), fonte del crosswalk (standard riconosciuto vs manuale) | Equivalenza esatta da SCF/CCM → peso alto |
| `MITIGA` | Efficacia stimata del controllo, evidenza di funzionamento, copertura del rischio | Controllo efficace e dimostrato → riduce di più il rischio residuo |
| `SI_APPLICA_A` | Rilevanza/criticità dell'asset, esplicito vs inferito | Asset critico esplicitamente in scope → peso alto |
| `DIPENDE_DA` | Criticità della dipendenza, frequenza d'uso | Dipendenza critica → peso alto (rilevante per supply-chain risk) |

Principi di calcolo del peso (coerenti con il motore universale di `.planning/PROJECT.md`):
- **Decadimento temporale** della freschezza: il peso di `DIMOSTRA` decresce nel tempo fino alla scadenza configurata, poi marca l'evidenza come stantia.
- **Boost da validazione umana**: gli archi confermati da un revisore pesano più di quelli solo suggeriti dall'AI.
- **Boost da automazione**: evidenze raccolte automaticamente e con integrità verificata pesano più di quelle manuali (meno soggette a errore/manomissione).
- **Aggregazione di percorso**: la copertura di un requisito è funzione del *miglior percorso pesato* norma→controllo→evidenza, non della semplice esistenza di un arco.

---

## 6. Fonti dati & connettori (ingestione)

L'ingestione riusa massivamente i domini esistenti. Tutti i connettori rispettano il principio local-first: girano nel perimetro, le credenziali stanno in un secret manager, nessun dato esce verso il cloud senza consenso.

| Fonte | Tipo di dato → nodi/archi | Meccanismo LocalMind |
|---|---|---|
| **Testi normativi (PDF/HTML)** | Framework, Requisiti | Dominio `document` (Tika/OCR) + segmentazione + embedding Qdrant |
| **Template framework precaricati** | Requisiti + crosswalk `EQUIVALE_A` | Modulo installabile (plugin PF4J / marketplace) |
| **Crosswalk pubblici (SCF, CSA CCM)** | Archi `EQUIVALE_A` | Import strutturato |
| **CMDB / inventario asset** | Asset, Dipendenze | Connettore MCP / import |
| **IdP (LDAP/SSO)** | Persone, Ruoli, evidenze accessi | Connettore MCP + automation |
| **Cloud (config/posture)** | Evidenze di configurazione | Connettore MCP + automation (raccolta continua) |
| **SIEM / log** | Evidenze di telemetria, Incidenti | Connettore MCP + automation, export verso SIEM esterno |
| **Repository Git / CI-CD** | Asset (microservizi), evidenze di processo (review, scansioni) | Connettore MCP |
| **Ticketing / ITSM** | Evidenze di change/incident, RemediationTask | Connettore MCP / messaging |
| **Email / Calendar** | Evidenze (verbali, approvazioni), scadenze | Domini `email`, `calendar` |
| **Document store interno** | Policy, Procedure, Attestazioni | Dominio `document` |
| **Registro trattamenti / DPIA** | Trattamenti GDPR | UI + import |

Linee guida sui connettori:
- Ogni connettore normalizza l'output in **nodi evidenza** con metadati standard (data, fonte, hash, controllo target).
- La **pianificazione** della raccolta è gestita dal dominio `automation` (continuous collection).
- I connettori sono **plugin PF4J** quando possibile, così l'ecosistema può estenderli senza toccare il core.
- L'**export** verso GRC/SIEM esterni (richiesto da NIS2/DORA) avviene in formati standard e firmati.

---

## 7. Funzionalità da creare, sviluppare e mantenere (MVP → evoluzione)

### 7.1 MVP (primo rilascio utile)

| # | Funzionalità | Cosa fare | Domini/moduli |
|---|---|---|---|
| 1 | **Modello a grafo compliance** | Tabelle MySQL per nodi (Framework, Requisito, Controllo, Evidenza, Asset, Rischio) e archi pesati; migrazioni Flyway (una query per file) | `knowledge`, infrastructure, app (Flyway) |
| 2 | **CRUD framework/requisiti/controlli** | API port/in + controller `/api/v1/compliance/*` + DTO; enum IT/EN | `knowledge`, api |
| 3 | **Ingestione normativa** | Upload PDF → estrazione → segmentazione in requisiti → embedding Qdrant | `document`, `knowledge` |
| 4 | **Mappatura `SODDISFA`/`EQUIVALE_A`** | UI per collegare controllo↔requisito e crosswalk; suggerimenti AI human-in-the-loop | `knowledge`, `agent`, frontend |
| 5 | **Gestione evidenze** | Upload/registrazione evidenza con hash, data, stato, validità; arco `DIMOSTRA` | `knowledge`, `document` |
| 6 | **Freschezza evidenze** | Calcolo stato (valida/in scadenza/scaduta) e decadimento del peso | `knowledge`, `automation` |
| 7 | **Coverage & gap analysis** | Calcolo copertura per requisito + lista gap prioritizzata | `knowledge`, `common`/analytics |
| 8 | **GraphRAG compliance Q&A** | Risposte in NL con citazione norma→controllo→evidenza, Ollama di default | `agent`, `llm`, `knowledge` |
| 9 | **Dashboard copertura** | Vista per framework/dominio/owner con stato live | frontend (feature compliance) |
| 10 | **Audit trail tamper-evident (base)** | Registro append-only con hash-chain delle modifiche di stato | infrastructure, `knowledge` |
| 11 | **Template framework precaricati** | Almeno ISO 27001:2022 e SOC 2 come modulo installabile | marketplace/plugin |
| 12 | **i18n IT/EN** | Enum e UI bilingui | frontend, api |

### 7.2 Evoluzione (fasi successive)

| # | Funzionalità | Valore | Domini/moduli |
|---|---|---|---|
| 13 | **Connettori automatici (CCM)** | Raccolta continua evidenze da cloud/IdP/SIEM via MCP | `mcp`, `automation`, plugin |
| 14 | **Generazione audit package firmato** | Dossier verificabile per requisito con catena completa | `document`, `common`/backup |
| 15 | **Workspace auditor esterno** | Accesso read-only tracciato + findings | `auth`, `knowledge` |
| 16 | **Tracciamento findings & remediation** | Ciclo finding→task→evidenza con scadenze | `automation`, `calendar` |
| 17 | **Registro rischi & rischio residuo** | Archi `MITIGA`/`MINACCIA`, heatmap, what-if | `knowledge`, frontend |
| 18 | **Modulo GDPR** | Registro trattamenti, basi giuridiche, DPIA, data flow | `knowledge`, frontend |
| 19 | **Alert & continuous monitoring** | Notifiche su scadenze, controlli falliti, nuovi requisiti | `messaging`, `automation`, `calendar` |
| 20 | **Aggiornamento normativo assistito** | Diff tra versioni di framework, gap automatici | `agent`, `knowledge` |
| 21 | **Visualizzazione grafo interattiva** | Esplorazione norma-controllo-evidenza per relazioni e peso | frontend |
| 22 | **Merkle tree / firma crittografica avanzata** | Integrità a prova di manomissione di livello forense | infrastructure |
| 23 | **Crosswalk estesi (30+ framework)** | Import SCF/CCM, NIST 800-53, PCI-DSS, NIS2, DORA, AI Act | marketplace/plugin |
| 24 | **Agenti di audit autonomi** | Agenti che raccolgono, validano e propongono remediation | `agent`, `automation` |
| 25 | **Attestazioni & campagne** | Workflow di attestazione periodica con firma | `automation`, `messaging` |

### 7.3 Da mantenere (manutenzione continua)

- Aggiornamento dei **template framework** e dei **crosswalk** quando i regolatori pubblicano nuove versioni.
- Verifica periodica dell'**integrità della hash-chain** dell'audit trail.
- Aggiornamento dei **connettori** quando cambiano le API delle fonti tecniche.
- Manutenzione delle **policy di freschezza** per tipo di evidenza.
- Allineamento delle **traduzioni IT/EN** di enum e UI a ogni nuovo concetto.

---

## 8. Casi d'uso AI / GraphRAG

L'AI naviga il grafo pesato per rispondere a domande che le query tabellari non sanno affrontare. Default Ollama (local-first); provider cloud solo con consenso esplicito per perimetro, dato che gli input sono regolamentati.

| Caso d'uso | Domanda tipica | Come l'AI usa il grafo |
|---|---|---|
| **Q&A di copertura** | "Siamo conformi all'art. 9 di DORA? Con quali evidenze e quanto sono fresche?" | Naviga Requisito→Controlli→Evidenze, valuta pesi e freschezza, cita il percorso |
| **Gap analysis narrata** | "Dove abbiamo i buchi più gravi su ISO 27001?" | Trova requisiti con copertura debole/assente, ordina per rischio, spiega |
| **Crosswalk intelligente** | "Quali evidenze SOC 2 posso riusare per NIS2?" | Segue archi `EQUIVALE_A` e `DIMOSTRA`, propone riuso |
| **Impatto di un cambiamento** | "Se dismetto questo asset, quali controlli/requisiti restano scoperti?" | Esplora `SI_APPLICA_A`/`DIPENDE_DA`, segnala scoperture |
| **Preparazione audit** | "Genera il pacchetto per l'audit ISO sul perimetro core banking" | Raccoglie catene norma→controllo→evidenza nel perimetro |
| **Analisi di incidente** | "Quali requisiti viola questo data breach e quali notifiche scattano?" | Segue `VIOLA`, mappa obblighi di notifica e SLA |
| **Collegamenti non evidenti** | "Quali controlli sono single point of failure su più framework?" | Trova nodi controllo ad alta centralità nel grafo |
| **Aggiornamento normativo** | "Cosa cambia con la nuova versione del framework e cosa devo presidiare?" | Diff sui requisiti, propaga ai controlli/evidenze impattati |

In tutti i casi la risposta **cita i nodi e i percorsi** usati (spiegabilità), requisito imprescindibile in un contesto di audit: l'AI non deve "rassicurare", deve *dimostrare con la catena di prova*.

---

## 9. KPI & metriche di successo

| Categoria | KPI | Significato / target |
|---|---|---|
| **Copertura** | % requisiti con copertura piena (controllo + evidenza valida) per framework | Trend crescente; target per framework critico ≥ 95% |
| **Copertura** | N° requisiti senza alcun controllo (gap scoperti) | Tendere a 0 sui framework attivi |
| **Freschezza** | % evidenze fresche vs scadute | Indicatore di continuous assurance; target ≥ 90% fresche |
| **Efficienza** | % evidenze riusate su più framework (effetto crosswalk) | Misura il risparmio da mapping; più alto è meglio |
| **Automazione** | % evidenze raccolte automaticamente vs manuali | Maturità del CCM; trend crescente |
| **Audit** | Tempo medio di preparazione di un pacchetto di audit | Da settimane a ore/minuti |
| **Audit** | N° findings esterni per audit / % chiusi entro SLA | Qualità del programma e della remediation |
| **Rischio** | Rischio residuo medio dopo mitigazione | Trend decrescente |
| **Integrità** | Verifiche di integrità hash-chain superate | 100%; qualsiasi anomalia è un incidente |
| **Adozione** | N° controlli/evidenze attivi, utenti attivi (owner, auditor) | Crescita del grafo e dell'uso |
| **AI** | % risposte GraphRAG con citazione valida del percorso | Spiegabilità; target ~100% |
| **Sovranità** | % elaborazioni eseguite in locale (Ollama) senza uscita dati | Coerenza con local-first; target ~100% di default |

---

## 10. Rischi & mitigazioni

| Rischio | Impatto | Mitigazione |
|---|---|---|
| **Copertura "verde" ma evidenze stantie** | Falsa sicurezza, audit fallito | Freschezza come attributo di primo livello + decadimento del peso + alert automatici |
| **Manomissione dell'audit trail** | Evidenza priva di valore legale | Hash-chain append-only; evoluzione verso Merkle/firma; verifiche periodiche di integrità |
| **Mappature errate o eccessive (over-mapping)** | Copertura sovrastimata | Validazione human-in-the-loop; peso più basso per archi solo suggeriti dall'AI |
| **Allucinazioni AI su questioni normative** | Decisioni di compliance sbagliate | GraphRAG con citazione obbligatoria dei nodi/percorsi; mai risposte senza catena di prova |
| **Fuga di dati regolamentati verso LLM cloud** | Violazione privacy/GDPR | Ollama di default; blocco invio cloud salvo consenso esplicito per perimetro |
| **Drift normativo (framework obsoleti)** | Conformità verso versione vecchia | Versionamento requisiti + aggiornamento normativo assistito + manutenzione template |
| **Modello a grafo su MySQL non scala su query profonde** | Performance gap analysis | Indici mirati, denormalizzazione di copertura, cache; rivalutare graph DB solo se necessario (vincolo di progetto) |
| **Credenziali dei connettori esposte** | Compromissione fonti | Secret manager, mai hardcoded; least privilege; rotazione |
| **Sovraccarico di alert (alert fatigue)** | Alert ignorati | Prioritizzazione per rischio, soglie configurabili, raggruppamento |
| **Dipendenza da un singolo control owner** | Gap su uscita persona | RACI nel grafo, riassegnazione, evidenze automatizzate dove possibile |

---

## 11. Manutenzione & evoluzione

- **Aggiornamento dei cataloghi normativi**: processo ricorrente per recepire nuove versioni di framework e nuove normative (es. evoluzioni AI Act post-agosto 2026), creando nuovi nodi requisito collegati ai precedenti.
- **Cura dei crosswalk**: revisione periodica delle equivalenze tra framework, con priorità ai più usati per il riuso evidenze.
- **Salute dei connettori**: monitoraggio del fallimento dei connettori (un connettore rotto = evidenze che smettono di rinfrescarsi); allarme dedicato.
- **Integrità nel tempo**: job periodico di verifica della hash-chain; in evoluzione, firma crittografica e ancoraggio Merkle.
- **Estendibilità via plugin**: nuovi framework, connettori e tipi di evidenza come moduli PF4J pubblicabili sul marketplace, senza toccare il core.
- **Qualità del dato**: revisione delle policy di freschezza, deduplica delle evidenze, archiviazione di nodi obsoleti (mai cancellazione che rompa la tracciabilità storica).
- **i18n**: ogni nuovo concetto/enum tradotto IT/EN; documentazione doppia lingua aggiornata in `documentation/` e `documentazione/`.
- **Tracciamento sviluppi**: ogni intervento documentato nella cartella `Sviluppi/` con la nomenclatura datata richiesta dal progetto, in plan mode, con checkpoint per i task complessi.
- **Roadmap graph DB**: monitorare le performance delle query di copertura/percorso su MySQL; il passaggio a un graph DB resta fuori scope ma rivalutabile se le metriche lo impongono.

---

## 12. Integrazione con i moduli LocalMind esistenti

| Modulo/dominio esistente | Ruolo nel verticale Compliance & audit |
|---|---|
| **`knowledge`** | Cuore del modello a grafo: nodi tipizzati, archi pesati, query di copertura/percorso. È qui che vive lo schema compliance. |
| **`document`** | Ingestione di testi normativi, policy, procedure, evidenze documentali (Tika/OCR); segmentazione ed embedding. |
| **Qdrant (vector store)** | Ricerca semantica su requisiti, controlli ed evidenze; supporto al GraphRAG e ai suggerimenti di mappatura. |
| **MySQL 8.0** | Struttura del grafo (nodi/archi), audit trail append-only, stato di copertura; migrazioni Flyway (una query per file). |
| **`agent`** | Agenti di gap analysis, suggerimento mappature, preparazione audit, aggiornamento normativo; agenti di audit autonomi (evoluzione). |
| **`llm` (Ollama default)** | Motore di ragionamento GraphRAG; fallback chain provider; cloud solo con consenso per dati regolamentati. |
| **`mcp`** | Connettori verso fonti tecniche (cloud, IdP, SIEM, CMDB, repo, ticketing) per la raccolta automatica di evidenze. |
| **`automation`** | Continuous controls monitoring: pianificazione della raccolta, regole di validazione, ricalcolo copertura, task di remediation. |
| **`messaging`** | Notifiche e alert (evidenze scadute, controlli falliti, scadenze) sui canali configurati. |
| **`calendar`** | Scadenze di evidenze, audit pianificati, deadline di remediation; evidenze da approvazioni/eventi. |
| **`email`** | Evidenze da comunicazioni (approvazioni, verbali) e notifiche verso owner/auditor. |
| **`auth`** | Controllo accessi local-first; workspace read-only per auditor esterni; attribuzione delle azioni nell'audit trail. |
| **`common` (analytics/backup)** | Calcolo metriche/coverage, generazione e firma dei pacchetti di audit, export/backup verificabili. |
| **`marketplace` / `plugin` (PF4J)** | Distribuzione di template framework, crosswalk e connettori come moduli installabili. |
| **`finetuning`** | (Evoluzione) affinamento di modelli locali sul linguaggio normativo dell'organizzazione per migliorare mappatura e Q&A. |
| **Frontend Angular 21** | Feature `compliance`: dashboard di copertura, gestione requisiti/controlli/evidenze, visualizzazione grafo, workspace audit; UI bilingue IT/EN, Signal store. |

Vincoli rispettati lungo tutta l'integrazione: **local-first/self-hostable**, **AI Ollama di default** con privacy dei dati regolamentati, **riuso MySQL+Qdrant** senza graph DB dedicato, **estensibilità PF4J**, **immutabilità** (requisiti/evidenze/audit trail versionati e append-only), **migrazioni Flyway con una sola query**, **enum e documentazione bilingui IT/EN**.

---

*Documento di indirizzo per gli sviluppi del verticale enterprise "Compliance & audit" sul motore di Knowledge Graph universale di LocalMind. Data: 2026-06-29.*
