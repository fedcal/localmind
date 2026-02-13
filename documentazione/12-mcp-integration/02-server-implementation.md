# Implementazione MCP Server in LocalMind

**Progetto:** LocalMind - Piattaforma AI Local-First
**Versione:** 0.1.0
**Ultimo aggiornamento:** 2026-02-13
**Modulo di riferimento:** localmind-infrastructure (`infrastructure.mcp.server`)

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Architettura del server MCP](#2-architettura-del-server-mcp)
3. [Tool esposti](#3-tool-esposti)
4. [Risorse esposte](#4-risorse-esposte)
5. [Prompt template](#5-prompt-template)
6. [Implementazione con Spring AI](#6-implementazione-con-spring-ai)
7. [Delegazione ai domain use case](#7-delegazione-ai-domain-use-case)
8. [Mappa dei file sorgente](#8-mappa-dei-file-sorgente)

---

## 1. Panoramica

LocalMind agisce come **MCP Server**, esponendo la propria knowledge base RAG e il gateway
multi-provider LLM attraverso il protocollo MCP. Questo consente a qualsiasi MCP client
compatibile (Claude Desktop, altri agenti AI, IDE con supporto MCP) di utilizzare le capacita'
di LocalMind come strumenti esterni.

Il server espone **135 tool nativi** distribuiti su **12 classi @Tool**, organizzate per dominio
funzionale: core AI, utility, codice, test, DevOps, database, documentazione, gestione progetto,
comunicazione, governance, operazioni e qualita'.

```
+------------------------------------------------------+
|                   LocalMind Backend                   |
|                                                       |
|  12 classi @Tool (135 tool totali)                    |
|  +--------------------+    +-----------------------+  |
|  | LocalMindMcpTools  |--->| DocumentSearchUseCase |  |
|  | LocalMindCodeTools |--->| CodeReviewUseCase     |  |
|  | LocalMindTestTools |--->| TestGeneratorUseCase  |  |
|  | ... (altre 9)      |--->| ... (altri use case)  |  |
|  +--------------------+    +-----------------------+  |
|  +--------------------+         Domain Layer          |
|  | LocalMindMcpRes.   |                               |
|  +--------------------+                               |
|  +--------------------+                               |
|  | LocalMindMcpPrompts|                               |
|  +--------------------+                               |
|         |                                             |
|         v                                             |
|  Spring AI MCP Server WebMVC                          |
|  (spring-ai-starter-mcp-server-webmvc)                |
+------------------------------------------------------+
         |
         | HTTP/SSE (JSON-RPC 2.0)
         v
  MCP Client esterno (Claude Desktop, IDE, etc.)
```

Il server MCP e' abilitato dalla property `localmind.mcp.server.enabled=true` (default: `true`)
e si configura in `application-dev.yml`:

```yaml
spring:
  ai:
    mcp:
      server:
        name: localmind
        version: 0.1.0
```

---

## 2. Architettura del server MCP

Il server MCP di LocalMind e' composto da 12 classi di tool piu' le classi per risorse e prompt,
tutte nel package `com.localmind.infrastructure.mcp.server`:

| Classe                        | Responsabilita'                                      | Tool | Primitiva MCP |
|-------------------------------|------------------------------------------------------|------|---------------|
| `LocalMindMcpTools`           | Core AI: ricerca RAG, chat LLM, elenco modelli       | 3    | Tool          |
| `LocalMindUtilityTools`       | Regex, HTTP client, snippet di codice                 | 13   | Tool          |
| `LocalMindCodeTools`          | Code review, analisi dipendenze, scaffolding          | 9    | Tool          |
| `LocalMindTestTools`          | Generazione test, analisi performance                 | 6    | Tool          |
| `LocalMindDevOpsTools`        | Docker, analisi log, CI/CD                            | 12   | Tool          |
| `LocalMindDatabaseTools`      | Esplorazione schema DB, generazione dati mock         | 8    | Tool          |
| `LocalMindDocTools`           | Documentazione API, conoscenza codebase               | 8    | Tool          |
| `LocalMindProjectTools`       | Scrum board, metriche agili, time tracking, economia  | 31   | Tool          |
| `LocalMindCommTools`          | Standup notes, gestione ambienti                      | 8    | Tool          |
| `LocalMindGovernanceTools`    | Policy di accesso, decision log                       | 10   | Tool          |
| `LocalMindOpsTools`           | Gestione incidenti, orchestrazione workflow            | 11   | Tool          |
| `LocalMindQualityTools`       | Quality gate, insight engine, dashboard, MCP registry | 16   | Tool          |
| `LocalMindMcpResources`       | Risorse leggibili (config provider)                   | -    | Resource      |
| `LocalMindMcpPrompts`         | Template di prompt parametrizzati                     | -    | Prompt        |
| **Totale**                    |                                                       | **135** |            |

Tutte le classi di tool sono annotate con:
- `@Component` - registrazione automatica nel contesto Spring
- `@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)`

Il flag `matchIfMissing = true` garantisce che il server MCP sia attivo di default.

---

## 3. Tool esposti

### 3.1 Core AI - `LocalMindMcpTools` (3 tool)

Tool fondamentali per l'interazione con la knowledge base RAG e il gateway LLM multi-provider.

| # | Metodo | Descrizione |
|---|--------|-------------|
| 1 | `documentSearch(query, topK)` | Ricerca documenti nella knowledge base LocalMind usando RAG. Restituisce chunk rilevanti con punteggi di similarita'. |
| 2 | `chat(message, provider, model, temperature, conversationId)` | Invia un messaggio a un LLM tramite il gateway multi-provider. Supporta conversazioni multi-turno tramite conversationId. Provider: OLLAMA, OPENAI, ANTHROPIC, GOOGLE. |
| 3 | `listModels()` | Elenca i provider LLM disponibili e il loro stato in LocalMind. |

**Use case di dominio:** `DocumentSearchUseCase`, `ChatUseCase`, `ConversationService`

---

### 3.2 Utility - `LocalMindUtilityTools` (13 tool)

Strumenti generici per regex, richieste HTTP e gestione snippet di codice.

#### Regex Builder (5 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 1 | `testRegex(pattern, testStrings, flags)` | Testa un pattern regex su una lista di stringhe. Restituisce risultati con gruppi catturati. |
| 2 | `explainRegex(pattern)` | Spiega un pattern regex in linguaggio naturale, componente per componente. |
| 3 | `buildRegex(description)` | Costruisce un pattern regex da una descrizione o parola chiave (email, url, ipv4, uuid, ecc.). |
| 4 | `optimizeRegex(pattern)` | Analizza un pattern regex e suggerisce ottimizzazioni (classi ridondanti, gruppi non necessari, quantificatori greedy). |
| 5 | `convertRegex(pattern, toFormat)` | Converte un pattern regex tra formati: java, python, javascript, pcre. |

#### HTTP Client (3 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 6 | `sendHttpRequest(url, method, headers, body, queryParams, timeoutMs)` | Invia una richiesta HTTP e restituisce status code, header, body e tempo di risposta in ms. |
| 7 | `compareHttpResponses(baselineUrl, currentUrl, method)` | Confronta le risposte HTTP di due URL. Mostra differenze in status code, tempo, body e header. |
| 8 | `generateCurl(method, url, headers, body)` | Genera un comando cURL equivalente dai parametri della richiesta HTTP. |

#### Snippet Manager (5 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 9 | `saveSnippet(title, code, language, description, tags)` | Salva uno snippet di codice riutilizzabile con metadati (titolo, linguaggio, descrizione, tag). |
| 10 | `searchSnippets(keyword, tag, language)` | Cerca snippet per parola chiave, tag o linguaggio di programmazione. I filtri sono combinabili. |
| 11 | `getSnippet(id)` | Recupera uno snippet specifico tramite il suo ID. |
| 12 | `deleteSnippet(id)` | Elimina uno snippet tramite il suo ID. |
| 13 | `listSnippetTags()` | Elenca tutti i tag usati negli snippet con il conteggio d'uso. |

**Use case di dominio:** `RegexUseCase`, `HttpClientUseCase`, `SnippetUseCase`

---

### 3.3 Code - `LocalMindCodeTools` (9 tool)

Strumenti per revisione del codice, analisi delle dipendenze e scaffolding di progetto.

#### Code Review (3 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 1 | `analyzeDiff(diff)` | Analizza una stringa git diff per problemi comuni: console.log, TODO, debugger, credenziali hardcoded, catch vuoti. |
| 2 | `checkComplexity(code, language)` | Calcola la complessita' ciclomatica di un frammento di codice. Restituisce punteggio, valutazione e breakdown. Linguaggi: java, python, javascript, typescript, rust. |
| 3 | `suggestImprovements(code, language)` | Suggerisce miglioramenti: magic number, funzioni lunghe (>30 righe), nesting profondo (>4 livelli), pattern duplicati, variabili inutilizzate. |

#### Dependency Manager (3 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 4 | `checkVulnerabilities(projectPath, projectType)` | Scansiona le dipendenze per vulnerabilita' note. Supporta Maven (pom.xml) e npm (package.json). |
| 5 | `findUnusedDependencies(projectPath, projectType)` | Trova dipendenze dichiarate ma non importate/usate nel codice sorgente. |
| 6 | `licenseAudit(projectPath, projectType)` | Audit delle licenze delle dipendenze, segnalando licenze copyleft (GPL, AGPL, LGPL, MPL-2.0). |

#### Project Scaffolding (3 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 7 | `listProjectTemplates()` | Elenca i template disponibili: spring-boot-api, angular-app, maven-multi-module, mcp-server, react-app. |
| 8 | `scaffoldProject(template, projectName, outputDir, author, description, license)` | Genera una struttura di progetto completa da un template con sostituzione dei placeholder. |
| 9 | `scaffoldComponent(name, type, language, outputDir)` | Genera un singolo componente/service/controller/model. |

**Use case di dominio:** `CodeReviewUseCase`, `DependencyAnalysisUseCase`, `ProjectScaffoldingUseCase`

---

### 3.4 Test & Performance - `LocalMindTestTools` (6 tool)

Strumenti per generazione di test e profilazione delle performance.

#### Test Generator (3 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 1 | `generateUnitTests(code, language, framework)` | Genera scheletri di test unitari dal codice sorgente. Supporta JUnit (Java), Vitest (TS/JS), Pytest (Python). |
| 2 | `findEdgeCases(code)` | Analizza il codice per identificare edge case: null/empty, condizioni limite, divisione per zero, errori async, I/O. |
| 3 | `analyzeCoverage(sourceCode, testCode)` | Analizza la copertura dei test confrontando i nomi delle funzioni tra sorgente e test. |

#### Performance Profiler (3 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 4 | `analyzeBundle(code, filePath)` | Analizza gli import per impatto sulla dimensione del bundle. Rileva dipendenze pesanti (moment.js, lodash, aws-sdk) e suggerisce alternative. |
| 5 | `findBottlenecks(code, language)` | Analisi statica per anti-pattern di performance: loop annidati O(n^2), I/O sincrono, ricerca lineare nei loop, paginazione mancante. |
| 6 | `benchmarkCompare(codeA, codeB, iterations, language)` | Genera un template di benchmark pronto per l'esecuzione per confrontare due snippet. Include warmup, misurazione e analisi statistica. |

**Use case di dominio:** `TestGeneratorUseCase`, `PerformanceProfilerUseCase`

---

### 3.5 DevOps - `LocalMindDevOpsTools` (12 tool)

Strumenti per analisi Docker, analisi dei log e monitoraggio CI/CD.

#### Docker Analysis (4 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 1 | `parseCompose(content)` | Analizza YAML docker-compose ed estrae servizi, reti, volumi. Esegue controlli di validazione (tag :latest, modalita' privileged, ecc.). |
| 2 | `analyzeDockerfile(content)` | Analizza un Dockerfile per violazioni delle best practice: tag :latest, immagini base pesanti, RUN consecutivi, ADD vs COPY, HEALTHCHECK mancante. |
| 3 | `listDockerServices()` | Elenca servizi Docker comuni con metadati: nome, immagine, porte default e descrizione. |
| 4 | `generateCompose(services)` | Genera YAML docker-compose da una lista di definizioni di servizio. |

#### Log Analyzer (4 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 5 | `analyzeLogFile(content, format)` | Analizza il contenuto di un file di log: conteggio livelli (INFO, WARN, ERROR, DEBUG), errori principali, intervallo temporale, formato rilevato. |
| 6 | `findErrorPatterns(content, minCount)` | Trova pattern di errore ricorrenti nei log normalizzando e raggruppando per pattern. Ordina per frequenza. |
| 7 | `tailLog(content, lines, filter)` | Restituisce le ultime N righe del log, con filtro opzionale per parola chiave (case-insensitive). Simula `tail` Unix con grep. |
| 8 | `generateLogSummary(content)` | Genera un riepilogo leggibile dei log: righe totali, distribuzione livelli, percentuali errore e warning. |

#### CI/CD Monitor (4 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 9 | `listPipelines(owner, repo)` | Elenca tutti i run di pipeline salvati per un repository GitHub. |
| 10 | `getPipelineStatus(runId)` | Recupera stato e dettagli di un run di pipeline specifico tramite runId. |
| 11 | `savePipelineRun(owner, repo, runId, title, branch, status, conclusion, workflow, url)` | Salva un record di pipeline run nel database locale per il tracciamento. |
| 12 | `detectFlakyTests(runs)` | Analizza risultati di test per rilevare test instabili (flaky). Calcola il tasso di instabilita' e persiste i risultati. |

**Use case di dominio:** `DockerAnalysisUseCase`, `LogAnalyzerUseCase`, `CicdMonitorUseCase`

---

### 3.6 Database - `LocalMindDatabaseTools` (8 tool)

Strumenti per esplorazione schema database e generazione di dati mock.

#### DB Schema Explorer (4 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 1 | `exploreSchema(jdbcUrl)` | Esplora lo schema completo del database tramite JDBC metadata. Elenca tutte le tabelle con colonne e chiavi primarie. Supporta MySQL, H2, SQLite. |
| 2 | `describeTable(jdbcUrl, tableName)` | Descrive una singola tabella in dettaglio: colonne con tipi e nullabilita', indici, chiavi esterne, conteggio righe approssimativo. |
| 3 | `suggestIndexes(jdbcUrl)` | Analizza lo schema e suggerisce indici mancanti, in particolare su colonne foreign key non indicizzate. |
| 4 | `generateErd(jdbcUrl)` | Genera un diagramma Mermaid erDiagram dallo schema del database, includendo tabelle, colonne e relazioni FK. |

#### Data Mock Generator (4 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 5 | `generateMockData(schema, count, name)` | Genera dati mock basati su uno schema di campi tipizzati. Tipi disponibili: firstName, lastName, email, phone, address, company, date, integer, float, boolean, uuid, sentence, paragraph, url, ipv4, hexColor. Max 10000 righe. |
| 6 | `generateMockJson(jsonSchema, count, name)` | Genera dati mock da una definizione JSON Schema con 'properties' che definiscono tipi e formati. |
| 7 | `generateMockCsv(columns, count, delimiter, name)` | Genera dati mock in formato CSV con colonne, conteggio e delimitatore configurabili. |
| 8 | `listMockGenerators()` | Elenca tutti i generatori di dati mock disponibili con nome e descrizione. |

**Use case di dominio:** `DbSchemaExplorerUseCase`, `DataMockGeneratorUseCase`

---

### 3.7 Documentazione - `LocalMindDocTools` (8 tool)

Strumenti per documentazione API e conoscenza della codebase.

#### API Documentation (3 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 1 | `extractEndpoints(filePath)` | Estrae endpoint REST API da un file sorgente. Supporta Spring MVC, Express e NestJS. |
| 2 | `generateOpenApi(endpoints, title, version)` | Genera uno scheletro di specifica OpenAPI 3.0.3 da una lista di endpoint. Include operationId, tag, parametri path, request body e risposte standard. |
| 3 | `findUndocumented(filePath)` | Trova export e dichiarazioni pubbliche non documentate. Controlla JavaDoc/JSDoc prima di funzioni, classi, interfacce, tipi, enum. |

#### Codebase Knowledge (5 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 4 | `searchCode(directory, pattern, fileExtensions, maxResults)` | Cerca un pattern (stringa o regex) nei file di una directory. Ignora directory non-sorgente (node_modules, .git, dist, target). |
| 5 | `explainModule(filePath)` | Analizza un file sorgente e fornisce un riepilogo strutturale: import, export, funzioni, classi, interfacce, type alias. |
| 6 | `architectureMap(directory, maxDepth)` | Genera una mappa architetturale (albero testuale) di una directory con conteggi file e tipi. |
| 7 | `dependencyGraph(directory)` | Crea un grafo delle dipendenze tra moduli interni analizzando le istruzioni import/require. Genera un diagramma Mermaid. |
| 8 | `trackChanges(modulePath, changeType, description, filesChanged, author, commitRef, historyLimit)` | Traccia le modifiche ai moduli della codebase nel tempo, o visualizza la cronologia. Tipi: feature, bugfix, refactor, dependency-update, performance, security. |

**Use case di dominio:** `ApiDocumentationUseCase`, `CodebaseKnowledgeUseCase`

---

### 3.8 Gestione Progetto - `LocalMindProjectTools` (31 tool)

Il piu' ampio set di tool, che copre scrum board, metriche agili, time tracking, economia di progetto e retrospettive.

#### Scrum Board (7 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 1 | `createSprint(name, startDate, endDate, goals)` | Crea un nuovo sprint con nome, intervallo date e obiettivi. |
| 2 | `createStory(title, description, acceptanceCriteria, storyPoints, priority, sprintId)` | Crea una nuova user story, opzionalmente assegnandola a uno sprint. |
| 3 | `createTask(title, description, storyId, assignee)` | Crea un nuovo task sotto una user story. Stato iniziale: 'todo'. |
| 4 | `updateTaskStatus(taskId, status)` | Aggiorna lo stato di un task. Stati: todo, in_progress, in_review, done, blocked. |
| 5 | `getSprint(sprintId)` | Recupera uno sprint con le sue user story e task. |
| 6 | `sprintBoard(sprintId)` | Visualizza la board dello sprint con task organizzati in colonne per stato. |
| 7 | `getBacklog()` | Recupera il product backlog: user story non assegnate a nessuno sprint. |

#### Agile Metrics (6 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 8 | `calculateVelocity(sprints)` | Calcola la velocita' del team dai dati di completamento sprint. Restituisce media, trend, massimo, minimo. |
| 9 | `generateBurndown(totalPoints, sprintDays, dailyProgress)` | Genera dati per burndown chart con linee ideale vs effettiva. |
| 10 | `calculateCycleTime(tasks)` | Calcola statistiche cycle time dalle date di inizio/completamento task. Restituisce media, mediana, p95, min, max. |
| 11 | `forecastCompletion(remainingPoints, velocityHistory)` | Simulazione Monte Carlo per prevedere quanti sprint servono per completare il lavoro rimanente. 1000 simulazioni, restituisce p50, p85, p95. |
| 12 | `predictRisk(sprintId)` | Predice il livello di rischio di uno sprint basandosi su velocita' storica e dati di completamento. |
| 13 | `correlateFactors(factorA, factorB, correlation, sampleSize, description)` | Registra o visualizza correlazioni tra velocita' e fattori esterni. |

#### Time Tracking (6 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 14 | `startTimer(taskId, description)` | Avvia un timer per tracciare il tempo su un task. |
| 15 | `stopTimer()` | Ferma il timer attivo e salva come voce di tempo. |
| 16 | `logTime(taskId, durationMinutes, description, date)` | Registra manualmente il tempo speso su un task. |
| 17 | `getTimesheet(startDate, endDate, userId)` | Recupera le voci di tempo e i totali per un intervallo di date. |
| 18 | `detectAnomalies(userId, days)` | Rileva pattern anomali nel time tracking: ore eccessive, lavoro nel weekend, voci duplicate. |
| 19 | `estimateVsActual(taskId, estimateMinutes, description)` | Confronta le stime di tempo con il tempo effettivo speso su un task. |

#### Project Economics (5 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 20 | `setBudget(projectName, totalBudget, currency)` | Imposta o aggiorna il budget totale per un progetto. |
| 21 | `logCost(projectName, category, amount, costDescription, date)` | Registra una voce di costo rispetto al budget di progetto. Categorie: development, infrastructure, design, testing, management, other. |
| 22 | `getBudgetStatus(projectName)` | Ottiene lo stato corrente del budget: totale, speso, rimanente, percentuale e breakdown per categoria. |
| 23 | `forecastBudget(projectName)` | Prevede quando il budget sara' esaurito in base al burn rate storico. |
| 24 | `costPerFeature(featureId, projectName, hoursSpent, hourlyRate, featureDescription, currency)` | Traccia il costo per feature/ticket. |

#### Retrospective Manager (7 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 25 | `createRetro(sprintId, format)` | Crea una nuova sessione di retrospettiva. Formati: mad-sad-glad, 4ls, start-stop-continue. |
| 26 | `addRetroItem(retroId, category, content)` | Aggiunge un elemento a una retrospettiva in una categoria specifica. |
| 27 | `voteRetroItem(itemId)` | Vota un elemento della retrospettiva per aumentarne la priorita'. |
| 28 | `generateActionItems(retroId, topN)` | Genera action item dagli elementi piu' votati della retrospettiva. |
| 29 | `getRetro(retroId)` | Recupera la retrospettiva completa con tutti gli elementi e action item. |
| 30 | `detectPatterns()` | Analizza temi e pattern ricorrenti attraverso tutte le retrospettive. |
| 31 | `suggestItems(limit)` | Ottiene suggerimenti auto-generati per elementi di retrospettiva basati su temi comuni. |

**Use case di dominio:** `ScrumBoardUseCase`, `AgileMetricsUseCase`, `TimeTrackingUseCase`, `ProjectEconomicsUseCase`, `RetrospectiveUseCase`

---

### 3.9 Comunicazione - `LocalMindCommTools` (8 tool)

Strumenti per standup notes giornaliere e gestione degli ambienti.

#### Standup Notes (3 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 1 | `logStandup(yesterday, today, blockers)` | Registra una voce di standup giornaliero con il lavoro di ieri, il piano di oggi e i blocchi. |
| 2 | `getStandupHistory(days)` | Recupera la cronologia degli standup degli ultimi N giorni. |
| 3 | `generateStatusReport(days)` | Genera un report di stato aggregando gli standup su un periodo. |

#### Environment Manager (5 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 4 | `listEnvironments(directory, recursive)` | Elenca i file di ambiente (.env) in una directory. |
| 5 | `getEnvVars(filePath, fileContent, showSecrets, filter)` | Analizza e visualizza le variabili d'ambiente dal contenuto di un file .env. Maschera valori sensibili (PASSWORD, SECRET, KEY, TOKEN). |
| 6 | `compareEnvironments(filePathA, contentA, filePathB, contentB, showValues)` | Confronta due insiemi di variabili d'ambiente. Mostra variabili solo in A, solo in B, con valori diversi e comuni. |
| 7 | `validateEnv(envContent, templateContent, strict)` | Valida un file .env rispetto a un template. Restituisce variabili mancanti, extra, valori vuoti ed errori. |
| 8 | `generateEnvTemplate(sourceContent, preserveDefaults)` | Genera un template .env dal contenuto sorgente, rimuovendo i valori segreti. |

**Use case di dominio:** `StandupUseCase`, `EnvironmentManagerUseCase`

---

### 3.10 Governance - `LocalMindGovernanceTools` (10 tool)

Strumenti per policy di accesso e log delle decisioni architetturali.

#### Access Policy (5 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 1 | `createPolicy(name, effect, rulesJson)` | Crea una nuova policy di accesso con effetto allow/deny e regole JSON. |
| 2 | `checkAccess(userId, server, tool)` | Verifica se un utente ha accesso a un server/tool MCP specifico. |
| 3 | `listPolicies()` | Elenca tutte le policy di accesso definite. |
| 4 | `assignRole(userId, roleName)` | Assegna un ruolo a un utente creando una policy allow. |
| 5 | `auditAccess(userId, server, limit)` | Recupera le voci di audit degli accessi per un utente, con filtro opzionale per server. |

#### Decision Log (5 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 6 | `recordDecision(title, context, decision, alternativesJson, consequences, status)` | Registra una nuova decisione architetturale o tecnica. Stati: proposed, accepted, deprecated, superseded. |
| 7 | `listDecisions(status, search, limit)` | Elenca le decisioni con filtri opzionali per stato e/o testo di ricerca. |
| 8 | `getDecision(id)` | Recupera una singola decisione con i link associati. |
| 9 | `supersedeDecision(id, supersededById)` | Segna una decisione come superata da un'altra decisione. |
| 10 | `linkDecision(decisionId, linkType, targetId, description)` | Crea un link tra una decisione e un artefatto esterno. Tipi: ticket, commit, impact, related. |

**Use case di dominio:** `AccessPolicyUseCase`, `DecisionLogUseCase`

---

### 3.11 Operazioni - `LocalMindOpsTools` (11 tool)

Strumenti per gestione incidenti e orchestrazione workflow.

#### Incident Manager (6 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 1 | `openIncident(title, severity, description, affectedSystemsJson)` | Apre un nuovo incidente. Severita': critical, high, medium, low. |
| 2 | `updateIncident(id, status, note)` | Aggiorna lo stato di un incidente e/o aggiunge una nota alla timeline. Stati: open, investigating, mitigating, resolved, postmortem. |
| 3 | `addTimelineEntry(incidentId, description, source)` | Aggiunge una voce alla timeline di un incidente esistente. |
| 4 | `resolveIncident(id, resolution, rootCause)` | Risolve un incidente, registrando la risoluzione e la root cause. |
| 5 | `generatePostmortem(id)` | Genera un report post-mortem in formato Markdown per un incidente. Include dettagli, timeline, risoluzione, root cause e action item suggeriti. |
| 6 | `listIncidents(status, severity, limit)` | Elenca gli incidenti con filtri opzionali per stato e severita'. |

#### Workflow Orchestrator (5 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 7 | `createWorkflow(name, description, triggerEvent, stepsJson)` | Crea una nuova definizione di workflow con evento trigger e step. Attivo di default. |
| 8 | `listWorkflows(activeFilter)` | Elenca tutte le definizioni di workflow con filtro opzionale attivo/inattivo. |
| 9 | `triggerWorkflow(workflowId, payloadJson)` | Attiva l'esecuzione di un workflow. Simula l'esecuzione degli step e restituisce i risultati del run. |
| 10 | `getWorkflowRun(runId)` | Recupera i dettagli di un run di workflow specifico. |
| 11 | `toggleWorkflow(workflowId, active)` | Alterna lo stato attivo/inattivo di un workflow. |

**Use case di dominio:** `IncidentManagerUseCase`, `WorkflowOrchestratorUseCase`

---

### 3.12 Qualita' - `LocalMindQualityTools` (16 tool)

Strumenti per quality gate, insight engine, dashboard e registro MCP interno.

#### Quality Gate (4 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 1 | `defineGate(name, projectName, checksJson)` | Definisce un quality gate con controlli metrici. Ogni controllo specifica metrica, operatore (>=, <=, >, <, ==, !=) e soglia. |
| 2 | `evaluateGate(gateId, metricsJson)` | Valuta un quality gate rispetto a metriche fornite. Tutti i controlli devono passare per il successo. |
| 3 | `listGates(projectName)` | Elenca tutti i quality gate definiti, con filtro opzionale per progetto. |
| 4 | `getGateHistory(gateId, limit)` | Recupera la cronologia delle valutazioni di un quality gate. |

#### Insight Engine (4 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 5 | `queryInsight(question)` | Interroga con linguaggio naturale per ottenere insight su velocita', budget, sprint, incidenti o qualita'. |
| 6 | `correlateMetrics(metricsJson, period)` | Correla piu' metriche per trovare relazioni tra di esse. Genera analisi di correlazione (forte/moderata/debole/trascurabile). |
| 7 | `explainTrend(metric, direction, period)` | Spiega un trend osservato in una metrica specifica. Fornisce analisi del perche' una metrica sta aumentando, diminuendo o e' stabile. |
| 8 | `healthDashboard()` | Genera un dashboard di salute aggregato del progetto. Restituisce salute complessiva (good/warning/critical) e breakdown per area. |

#### Dashboard API (4 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 9 | `getOverview()` | Panoramica dashboard con progresso sprint aggregato, trend velocita', utilizzo time tracking e stato budget. |
| 10 | `getServerStatus(serverName)` | Stato dei server MCP con filtro opzionale per nome. |
| 11 | `getRecentActivity(limit)` | Voci di attivita' recenti del progetto con tipo, descrizione e timestamp. |
| 12 | `getProjectSummary(project)` | Riepilogo completo del progetto: stato sprint, velocita', budget, incidenti, metriche di qualita'. |

#### MCP Internal Registry (4 tool)

| # | Metodo | Descrizione |
|---|--------|-------------|
| 13 | `registerInternalServer(name, url, transport, capabilitiesJson)` | Registra un server MCP interno nel registro in-memory. |
| 14 | `discoverServers(status, transport)` | Scopre server MCP registrati, con filtri opzionali per stato e/o trasporto. |
| 15 | `mcpHealthCheck(serverId)` | Esegue un health check su un server MCP registrato. |
| 16 | `getServerCapabilities(serverId)` | Recupera le capabilities di un server MCP registrato. |

**Use case di dominio:** `QualityGateUseCase`, `InsightEngineUseCase`, `DashboardUseCase`, `McpInternalRegistryUseCase`

---

### Riepilogo generale tool

| Categoria | Classe | Tool | Use Case di dominio |
|-----------|--------|------|---------------------|
| Core AI | `LocalMindMcpTools` | 3 | `DocumentSearchUseCase`, `ChatUseCase`, `ConversationService` |
| Utility | `LocalMindUtilityTools` | 13 | `RegexUseCase`, `HttpClientUseCase`, `SnippetUseCase` |
| Code | `LocalMindCodeTools` | 9 | `CodeReviewUseCase`, `DependencyAnalysisUseCase`, `ProjectScaffoldingUseCase` |
| Test & Performance | `LocalMindTestTools` | 6 | `TestGeneratorUseCase`, `PerformanceProfilerUseCase` |
| DevOps | `LocalMindDevOpsTools` | 12 | `DockerAnalysisUseCase`, `LogAnalyzerUseCase`, `CicdMonitorUseCase` |
| Database | `LocalMindDatabaseTools` | 8 | `DbSchemaExplorerUseCase`, `DataMockGeneratorUseCase` |
| Documentazione | `LocalMindDocTools` | 8 | `ApiDocumentationUseCase`, `CodebaseKnowledgeUseCase` |
| Gestione Progetto | `LocalMindProjectTools` | 31 | `ScrumBoardUseCase`, `AgileMetricsUseCase`, `TimeTrackingUseCase`, `ProjectEconomicsUseCase`, `RetrospectiveUseCase` |
| Comunicazione | `LocalMindCommTools` | 8 | `StandupUseCase`, `EnvironmentManagerUseCase` |
| Governance | `LocalMindGovernanceTools` | 10 | `AccessPolicyUseCase`, `DecisionLogUseCase` |
| Operazioni | `LocalMindOpsTools` | 11 | `IncidentManagerUseCase`, `WorkflowOrchestratorUseCase` |
| Qualita' | `LocalMindQualityTools` | 16 | `QualityGateUseCase`, `InsightEngineUseCase`, `DashboardUseCase`, `McpInternalRegistryUseCase` |
| **Totale** | **12 classi** | **135** | **26 use case** |

---

## 4. Risorse esposte

Le risorse sono implementate in `LocalMindMcpResources.java`. Attualmente le risorse vengono
registrate programmaticamente tramite `McpConfiguration` poiche' il supporto per annotazioni
`@McpResource` dipende dalla versione di Spring AI.

### 4.1 `config://providers`

**URI:** `config://providers`
**Tipo MIME:** `application/json`
**Descrizione:** Restituisce la configurazione completa dei provider LLM disponibili.

```json
{
  "providers": [
    {"name": "OLLAMA", "local": true, "defaultModel": "llama3.2"},
    {"name": "OPENAI", "local": false, "defaultModel": "gpt-4o"},
    {"name": "ANTHROPIC", "local": false, "defaultModel": "claude-sonnet-4-20250514"},
    {"name": "GOOGLE", "local": false, "defaultModel": "gemini-pro"}
  ],
  "defaultProvider": "OLLAMA"
}
```

### 4.2 `document://{id}` (pianificato)

**URI:** `document://{id}`
**Tipo MIME:** `text/plain`
**Descrizione:** Restituisce il contenuto completo di un documento indicizzato, identificato
dal suo UUID. Questa risorsa sara' implementata quando il document retrieval by ID sara'
disponibile nel domain layer.

---

## 5. Prompt template

I prompt template sono implementati in `LocalMindMcpPrompts.java` e forniscono pattern
predefiniti per interazioni comuni con la knowledge base.

### 5.1 `rag-query`

**Descrizione:** Query con contesto RAG precompilato. Combina i risultati della ricerca
semantica con la query dell'utente in un prompt strutturato.

**Parametri:**
- `query` (String) - La domanda dell'utente
- `context` (String) - Il contesto estratto dalla knowledge base

### 5.2 `summarize-document`

**Descrizione:** Genera un riassunto strutturato di un documento.

**Parametri:**
- `content` (String) - Il contenuto del documento da riassumere

### Riepilogo prompt

| Prompt                | Parametri          | Caso d'uso                               |
|-----------------------|--------------------|------------------------------------------|
| `rag-query`           | query, context     | Q&A con contesto dalla knowledge base    |
| `summarize-document`  | content            | Riassunto automatico di documenti        |

---

## 6. Implementazione con Spring AI

### 6.1 Annotazione `@Tool`

Spring AI 1.0.0 fornisce l'annotazione `@Tool` per dichiarare metodi come tool MCP.
L'annotazione genera automaticamente lo schema JSON per i parametri e registra il tool
nel server MCP.

```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

@Tool(description = "Descrizione del tool per il modello LLM")
public ReturnType methodName(
    @ToolParam(description = "Descrizione parametro") ParamType param) {
    // implementazione
}
```

### 6.2 Registrazione automatica

Il starter `spring-ai-starter-mcp-server-webmvc` scansiona automaticamente tutti i bean
contenenti metodi annotati `@Tool` e li registra come tool MCP. La configurazione in
`application-dev.yml` imposta il nome e la versione del server:

```yaml
spring:
  ai:
    mcp:
      server:
        name: localmind        # Nome del server MCP
        version: 0.1.0         # Versione del server
```

### 6.3 Endpoint esposti

Con il starter WebMVC, il server MCP espone automaticamente:

| Endpoint          | Metodo | Descrizione                            |
|-------------------|--------|----------------------------------------|
| `/mcp/sse`        | GET    | Stream SSE per notifiche server->client|
| `/mcp/message`    | POST   | Messaggi JSON-RPC client->server       |

Questi endpoint sono separati dalle API REST di LocalMind (`/api/v1/*`).

---

## 7. Delegazione ai domain use case

Un principio chiave dell'architettura esagonale di LocalMind e' che i componenti MCP server
(nel modulo `infrastructure`) delegano la logica di business ai **domain use case**:

```
+-------------------------------------+       +-----------------------------+
|   Infrastructure Layer (12 classi)  |       |     Domain Layer            |
|                                     |       |                             |
| LocalMindMcpTools                   |       | DocumentSearchUseCase       |
|   .documentSearch() ----------------|------>|   .search(query, topK)      |
|   .chat() --------------------------|------>| ChatUseCase                 |
| LocalMindCodeTools                  |       | CodeReviewUseCase           |
|   .analyzeDiff() -------------------|------>|   .analyzeDiff(diff)        |
| LocalMindProjectTools               |       | ScrumBoardUseCase           |
|   .createSprint() ------------------|------>|   .createSprint(...)        |
| ... (altre 9 classi)               |       | ... (altri use case)        |
+-------------------------------------+       +-----------------------------+
```

Questo garantisce che:
1. La logica di business rimane nel dominio, non nell'infrastruttura
2. I tool MCP sono una semplice facciata (adapter) verso il dominio
3. La testabilita' e' garantita iniettando mock dei use case
4. Aggiungere nuovi tool e' semplice: basta aggiungere un metodo `@Tool` che delega
5. Le 12 classi di tool sono organizzate per dominio funzionale, migliorando la manutenibilita'

---

## 8. Mappa dei file sorgente

```
localmind-infrastructure/
  src/main/java/com/localmind/infrastructure/mcp/
    server/
      LocalMindMcpTools.java            # @Tool (3): document_search, chat, list_models
      LocalMindUtilityTools.java        # @Tool (13): regex, http client, snippet manager
      LocalMindCodeTools.java           # @Tool (9): code review, dependency analysis, scaffolding
      LocalMindTestTools.java           # @Tool (6): test generator, performance profiler
      LocalMindDevOpsTools.java         # @Tool (12): docker analysis, log analyzer, CI/CD monitor
      LocalMindDatabaseTools.java       # @Tool (8): DB schema explorer, data mock generator
      LocalMindDocTools.java            # @Tool (8): API documentation, codebase knowledge
      LocalMindProjectTools.java        # @Tool (31): scrum, agile metrics, time, economics, retro
      LocalMindCommTools.java           # @Tool (8): standup notes, environment manager
      LocalMindGovernanceTools.java     # @Tool (10): access policy, decision log
      LocalMindOpsTools.java            # @Tool (11): incident manager, workflow orchestrator
      LocalMindQualityTools.java        # @Tool (16): quality gate, insight, dashboard, MCP registry
      LocalMindMcpResources.java        # Resources: config://providers
      LocalMindMcpPrompts.java          # Prompts: rag-query, summarize-document
    config/
      McpConfiguration.java             # Bean definitions per MCP client/server
```

**Dipendenze Maven (localmind-infrastructure/pom.xml):**

```xml
<!-- MCP Server (WebMVC) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

---

> **Navigazione documentazione:**
> - Precedente: [01-panoramica-protocollo-mcp.md](01-panoramica-protocollo-mcp.md)
> - Prossimo: [03-client-implementation.md](03-client-implementation.md)
> - Configurazione: [04-configurazione.md](04-configurazione.md)
