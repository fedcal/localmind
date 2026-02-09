# Fasi di Sviluppo

| | |
|---|---|
| **Documento** | Roadmap - Fasi di Sviluppo |
| **Versione** | 0.1.0 |
| **Data** | 2026-02-09 |
| **Progetto** | LocalMind |

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Fase 1: LLM Gateway + Ollama (CORRENTE)](#2-fase-1-llm-gateway--ollama-corrente)
3. [Fase 2: File System Ingestion](#3-fase-2-file-system-ingestion)
4. [Fase 3: RAG Base](#4-fase-3-rag-base)
5. [Fase 4: UI Angular Completa](#5-fase-4-ui-angular-completa)
6. [Fase 5: Automazioni n8n](#6-fase-5-automazioni-n8n)
7. [Fase 6: AI Agents](#7-fase-6-ai-agents)
8. [Riepilogo Roadmap](#8-riepilogo-roadmap)

---

## 1. Panoramica

Lo sviluppo di LocalMind segue un approccio **incrementale e progressivo**, articolato in 6 fasi principali. Ogni fase produce un insieme di deliverable funzionanti e testabili, costruendo progressivamente sulle fondamenta poste dalle fasi precedenti.

### Principi guida

| Principio | Descrizione |
|---|---|
| **Vertical slicing** | Ogni fase produce funzionalita' complete end-to-end (backend + frontend) |
| **Incrementalita'** | Ogni fase estende le precedenti senza riscritture |
| **Testabilita'** | Ogni fase ha criteri di completamento verificabili |
| **Indipendenza** | Le fasi possono essere rilasciate indipendentemente |

### Diagramma delle dipendenze

```
Fase 1: LLM Gateway + Ollama
    │
    ├──► Fase 2: File System Ingestion
    │        │
    │        └──► Fase 3: RAG Base
    │                 │
    │                 ├──► Fase 5: Automazioni n8n
    │                 │
    │                 └──► Fase 6: AI Agents
    │
    └──► Fase 4: UI Angular Completa (trasversale)
```

---

## 2. Fase 1: LLM Gateway + Ollama (CORRENTE)

### Obiettivo

Realizzare una chat funzionante con Ollama locale, implementando il layer di astrazione multi-provider LLM (LLM Gateway) e l'interfaccia utente di base.

### Status

**In corso** - Scaffolding architetturale completato, implementazione dei componenti in corso.

### Deliverable

#### Backend

| Componente | Modulo | Descrizione | Status |
|---|---|---|---|
| `LlmPort` | `localmind-domain` | Interfaccia porta per l'accesso ai provider LLM | Completato |
| `LlmGatewayService` | `localmind-application` | Servizio applicativo con routing multi-provider, fallback chain, cost tracking | In corso |
| `OllamaLlmAdapter` | `localmind-infrastructure` | Adapter per Ollama tramite Spring AI | In corso |
| `OpenAiLlmAdapter` | `localmind-infrastructure` | Adapter per OpenAI tramite Spring AI (condizionale) | Pianificato |
| `AnthropicLlmAdapter` | `localmind-infrastructure` | Adapter per Anthropic tramite Spring AI (condizionale) | Pianificato |
| `ChatController` | `localmind-app` | REST API per la gestione delle conversazioni | In corso |
| `DashboardController` | `localmind-app` | REST API per health check e metriche base | Completato |
| `SecurityConfig` | `localmind-infrastructure` | Configurazione Spring Security (permissiva per v0.1.0) | Completato |
| `CorsConfig` | `localmind-infrastructure` | Configurazione CORS per Angular dev server | Completato |

#### Frontend

| Componente | Tipo | Descrizione | Status |
|---|---|---|---|
| `ChatPage` | Component (Smart) | Pagina principale della chat | In corso |
| `ChatStore` | Signal Store | Stato reattivo della chat basato su Angular Signals | In corso |
| `ChatService` | Service | Comunicazione HTTP con il backend `/api/v1/chat` | In corso |
| `MessageBubble` | Component (Dumb) | Visualizzazione singolo messaggio (utente/assistente) | Pianificato |
| `ChatInput` | Component (Dumb) | Input per l'invio dei messaggi | Pianificato |
| `SidebarLayout` | Component | Layout con sidebar navigazione | Pianificato |

#### Infrastruttura

| Componente | Descrizione | Status |
|---|---|---|
| `docker-compose.yml` | Orchestrazione PostgreSQL, Qdrant, Ollama, n8n | Completato |
| `.env.example` | Template variabili d'ambiente | Completato |
| `application.yml` | Configurazione Spring Boot (dev) | Completato |
| `application-prod.yml` | Configurazione Spring Boot (prod) | Completato |

### Dettaglio tecnico: LlmGatewayService

Il `LlmGatewayService` e' il componente centrale della Fase 1. Le sue responsabilita' includono:

1. **Routing multi-provider**: selezione del provider LLM appropriato in base alla configurazione utente e alla disponibilita'.
2. **Fallback chain configurabile**: se il provider primario non e' disponibile, il sistema tenta automaticamente il successivo nella catena.
3. **Cost tracking**: registrazione dei token consumati per ogni richiesta, differenziati per provider e modello.
4. **Usage metrics**: metriche di utilizzo aggregate (richieste totali, token totali, costo stimato).

```
Richiesta utente
    │
    ▼
LlmGatewayService
    │
    ├──► Provider primario (es. Ollama)
    │        │
    │        ├── Successo ──► Risposta
    │        │
    │        └── Fallimento ──► Fallback
    │                              │
    │                              ├──► Provider secondario (es. OpenAI)
    │                              │        │
    │                              │        └── Successo ──► Risposta
    │                              │
    │                              └──► Provider terziario (es. Anthropic)
    │
    └──► Cost tracking (asincrono)
```

### Stima effort

| Attivita' | Effort stimato |
|---|---|
| Backend: LlmGatewayService + adapter | 3-4 settimane |
| Backend: ChatController + persistence | 2 settimane |
| Frontend: Chat page + store | 2-3 settimane |
| Testing e integrazione | 1-2 settimane |
| **Totale Fase 1** | **8-11 settimane** |

### Criteri di completamento

- [ ] Chat funzionante con Ollama locale (invio messaggio, ricezione risposta).
- [ ] Persistenza conversazioni su PostgreSQL.
- [ ] Health check endpoint funzionante (`/api/v1/dashboard/health`).
- [ ] Cost tracking base (token consumati per richiesta).
- [ ] Fallback chain configurabile (almeno 2 provider).
- [ ] UI Angular con pagina chat funzionante.
- [ ] Test unitari per LlmGatewayService.
- [ ] Test di integrazione per OllamaLlmAdapter.

---

## 3. Fase 2: File System Ingestion

### Obiettivo

Indicizzare documenti da cartelle locali del file system dell'utente, creando una base di conoscenza strutturata e consultabile.

### Dipendenze

- **Fase 1 completata**: infrastruttura base, persistence, API REST.

### Deliverable

#### Backend

| Componente | Modulo | Descrizione |
|---|---|---|
| `FileSystemScannerPort` | `localmind-domain` | Interfaccia porta per lo scanning del file system |
| `LocalFileSystemScanner` | `localmind-infrastructure` | Adapter che implementa lo scanning di cartelle locali |
| `FolderConfig` | `localmind-domain` | Entita' di dominio per la configurazione delle cartelle da monitorare |
| `FolderConfigRepository` | `localmind-domain` | Porta repository per la persistenza delle configurazioni cartelle |
| `FolderScanJob` | `localmind-infrastructure` | Job Spring Batch per la scansione periodica delle cartelle |
| `FileHashService` | `localmind-application` | Servizio per il calcolo dell'hash SHA-256 dei file (deduplicazione) |
| `DocumentIngestionService` | `localmind-application` | Servizio per l'ingestion dei documenti nel sistema |
| `FolderConfigController` | `localmind-app` | REST API per la gestione delle configurazioni cartelle |

#### Frontend

| Componente | Tipo | Descrizione |
|---|---|---|
| `FoldersPage` | Component (Smart) | Pagina per la gestione delle cartelle monitorate |
| `FolderConfigStore` | Signal Store | Stato reattivo delle configurazioni cartelle |
| `FolderConfigService` | Service | Comunicazione HTTP con `/api/v1/folders` |
| `FolderCard` | Component (Dumb) | Visualizzazione singola cartella configurata |
| `AddFolderDialog` | Component | Dialog per aggiunta nuova cartella |

#### Formati file supportati

| Formato | Estensione | Parser |
|---|---|---|
| PDF | `.pdf` | Apache PDFBox |
| Microsoft Word | `.docx` | Apache POI |
| Microsoft Excel | `.xlsx` | Apache POI |
| Microsoft PowerPoint | `.pptx` | Apache POI |
| Testo semplice | `.txt` | Java NIO |
| Markdown | `.md` | Java NIO |
| CSV | `.csv` | Apache Commons CSV |
| JSON | `.json` | Jackson |
| HTML | `.html`, `.htm` | Jsoup |

### Dettaglio tecnico: Deduplicazione via SHA-256

Per evitare la duplicazione di documenti nel sistema, viene calcolato un hash SHA-256 per ogni file:

```
File nuovo rilevato
    │
    ▼
Calcolo SHA-256
    │
    ├── Hash gia' presente nel DB ──► Skip (file gia' indicizzato)
    │
    └── Hash non presente ──► Ingestione del documento
                                  │
                                  ├── Estrazione testo
                                  ├── Creazione metadati
                                  └── Salvataggio su PostgreSQL
```

### Stima effort

| Attivita' | Effort stimato |
|---|---|
| Backend: File system scanner + parser | 3-4 settimane |
| Backend: Spring Batch job + scheduling | 2 settimane |
| Backend: Deduplicazione + REST API | 1-2 settimane |
| Frontend: Folders page + store | 2 settimane |
| Testing e integrazione | 1-2 settimane |
| **Totale Fase 2** | **9-12 settimane** |

### Criteri di completamento

- [ ] Configurazione cartelle da monitorare via API REST.
- [ ] Scansione periodica automatica (Spring Batch + scheduling).
- [ ] Supporto per almeno 5 formati file (PDF, DOCX, TXT, MD, CSV).
- [ ] Deduplicazione basata su hash SHA-256.
- [ ] UI Angular con pagina gestione cartelle.
- [ ] Metadati documento: nome, path, dimensione, data modifica, hash, stato indicizzazione.
- [ ] Test unitari e di integrazione.

---

## 4. Fase 3: RAG Base

### Obiettivo

Implementare una pipeline RAG (Retrieval-Augmented Generation) completa che permetta la ricerca semantica sui documenti indicizzati e il Q&A con citazione delle fonti.

### Dipendenze

- **Fase 1 completata**: LLM Gateway funzionante.
- **Fase 2 completata**: documenti indicizzati nel sistema.

### Deliverable

#### Backend

| Componente | Modulo | Descrizione |
|---|---|---|
| `TextExtractorPort` | `localmind-domain` | Interfaccia porta per l'estrazione del testo dai documenti |
| `ChunkingService` | `localmind-application` | Servizio per la suddivisione del testo in chunk con overlap |
| `EmbeddingPort` | `localmind-domain` | Interfaccia porta per la generazione degli embedding |
| `OllamaEmbeddingAdapter` | `localmind-infrastructure` | Adapter per generazione embedding via Ollama (nomic-embed-text) |
| `VectorStorePort` | `localmind-domain` | Interfaccia porta per il vector store |
| `QdrantVectorStoreAdapter` | `localmind-infrastructure` | Adapter per Qdrant |
| `SemanticSearchService` | `localmind-application` | Servizio per la ricerca semantica con similarity score |
| `RagService` | `localmind-application` | Servizio che orchestra il flusso RAG completo |
| `SearchController` | `localmind-app` | REST API per ricerca e Q&A |

#### Pipeline RAG

```
Documento originale
    │
    ▼
[Extract] ──► Testo estratto dal documento
    │
    ▼
[Chunk] ──► Frammenti di testo (500-1000 token con overlap)
    │
    ▼
[Embed] ──► Vettori numerici (via Ollama nomic-embed-text)
    │
    ▼
[Store] ──► Salvataggio su Qdrant con metadati


Query utente
    │
    ▼
[Embed query] ──► Vettore della query
    │
    ▼
[Search] ──► Top-K chunk piu' simili da Qdrant
    │
    ▼
[Augment] ──► Prompt = query + chunk rilevanti come contesto
    │
    ▼
[Generate] ──► Risposta LLM con citazione delle fonti
```

#### Frontend

| Componente | Tipo | Descrizione |
|---|---|---|
| `SearchPage` | Component (Smart) | Pagina per ricerca semantica e Q&A |
| `SearchStore` | Signal Store | Stato reattivo della ricerca |
| `SearchService` | Service | Comunicazione HTTP con `/api/v1/search` |
| `SearchResultCard` | Component (Dumb) | Visualizzazione risultato con snippet e score |
| `SourceCitation` | Component (Dumb) | Visualizzazione citazione fonte (documento, pagina) |

### Stima effort

| Attivita' | Effort stimato |
|---|---|
| Backend: Pipeline extract-chunk-embed-store | 3-4 settimane |
| Backend: Semantic search + RAG service | 2-3 settimane |
| Backend: SearchController + REST API | 1 settimana |
| Frontend: Search page + risultati | 2-3 settimane |
| Testing e integrazione | 2 settimane |
| **Totale Fase 3** | **10-13 settimane** |

### Criteri di completamento

- [ ] Pipeline completa funzionante: extract, chunk, embed, store.
- [ ] Ricerca semantica con risultati ordinati per similarity score.
- [ ] Q&A con citazione delle fonti (documento, pagina/sezione).
- [ ] Chunking configurabile (dimensione chunk, overlap).
- [ ] UI Angular con pagina ricerca funzionante.
- [ ] Test unitari e di integrazione per ogni componente della pipeline.

---

## 5. Fase 4: UI Angular Completa

### Obiettivo

Realizzare un'interfaccia utente professionale, reattiva e completa, che copra tutte le funzionalita' del sistema con un design system coerente.

### Dipendenze

- **Fase 1 completata**: chat funzionante.
- **Indipendente** dalle fasi 2-3 per i componenti UI generici, ma richiede le fasi 2-3 per le pagine specifiche (documenti, ricerca).

### Deliverable

| Componente | Tipo | Descrizione |
|---|---|---|
| **Design System** | Globale | Tema dark/light, palette colori, tipografia, spacing, componenti base |
| **Document Library** | Page | Upload documenti, preview, gestione metadati, filtri e ordinamento |
| **Dashboard** | Page | Grafici utilizzo (token, costi, documenti), overview sistema |
| **Settings** | Page | Configurazione provider LLM, gestione cartelle, preferenze utente |
| **Responsive Design** | Globale | Layout adattivo per desktop, tablet, mobile |
| **Modalita' Semplice/Avanzata** | Globale | Toggle tra interfaccia semplificata e interfaccia completa |
| **Chart Components** | Components | Grafici con chart.js o ngx-charts per dashboard |
| **Notification System** | Service + Component | Toast notifications per feedback azioni utente |
| **Loading States** | Components | Skeleton loaders, spinner, progress bar |
| **Error Handling** | Service + Components | Gestione errori globale con messaggi user-friendly |

### Design system

| Aspetto | Specifica |
|---|---|
| **Framework CSS** | Angular Material o Tailwind CSS |
| **Tema dark** | Colori scuri, riduzione affaticamento visivo |
| **Tema light** | Colori chiari, alta leggibilita' |
| **Toggle tema** | Persistenza preferenza utente su localStorage |
| **Tipografia** | Font system (Inter o Roboto) |
| **Spacing** | Sistema a 4px (4, 8, 12, 16, 24, 32, 48, 64) |
| **Breakpoints** | Mobile (<768px), Tablet (768-1024px), Desktop (>1024px) |

### Stima effort

| Attivita' | Effort stimato |
|---|---|
| Design system + tema dark/light | 2-3 settimane |
| Document library page | 2-3 settimane |
| Dashboard con grafici | 2 settimane |
| Settings page | 1-2 settimane |
| Responsive design | 2 settimane |
| Modalita' Semplice/Avanzata | 1 settimana |
| Polish e QA | 1-2 settimane |
| **Totale Fase 4** | **11-15 settimane** |

### Criteri di completamento

- [ ] Design system completo con tema dark e light.
- [ ] Tutte le pagine funzionanti (chat, documenti, ricerca, dashboard, settings).
- [ ] Dashboard con grafici utilizzo.
- [ ] Layout responsive funzionante su desktop, tablet, mobile.
- [ ] Modalita' Semplice e Avanzata con toggle persistente.
- [ ] Gestione errori e loading states su tutte le pagine.

---

## 6. Fase 5: Automazioni n8n

### Obiettivo

Integrare workflow automatizzati trigger-based tramite n8n, permettendo all'utente di automatizzare flussi di lavoro basati su eventi interni di LocalMind.

### Dipendenze

- **Fase 1 completata**: backend funzionante.
- **Fase 3 completata**: RAG per arricchimento workflow con dati dai documenti.

### Deliverable

#### Backend

| Componente | Modulo | Descrizione |
|---|---|---|
| `AutomationPort` | `localmind-domain` | Interfaccia porta per il sistema di automazione |
| `N8nAutomationAdapter` | `localmind-infrastructure` | Adapter per l'integrazione bidirezionale con n8n |
| `WebhookService` | `localmind-application` | Servizio per l'invio di webhook trigger su eventi interni |
| `AutomationController` | `localmind-app` | REST API per la gestione delle automazioni |

#### Integrazione bidirezionale

```
LocalMind ──► n8n (Webhook trigger)
    │
    │  Eventi:
    │  - Nuovo documento indicizzato
    │  - Nuova conversazione completata
    │  - Soglia costi superata
    │  - Errore sistema
    │
n8n ──► LocalMind (API callback)
    │
    │  Azioni:
    │  - Avviare indicizzazione cartella
    │  - Inviare messaggio chat
    │  - Recuperare statistiche
    │  - Eseguire ricerca semantica
```

#### Template workflow predefiniti

| Workflow | Trigger | Azione |
|---|---|---|
| **Daily Summary** | Cron (ogni giorno alle 08:00) | Genera un riepilogo delle attivita' del giorno precedente via LLM |
| **New Document Alert** | Webhook (nuovo documento) | Notifica (email/Telegram) quando un nuovo documento viene indicizzato |
| **Cost Alert** | Webhook (soglia costi) | Notifica quando i costi dei provider cloud superano una soglia |
| **Auto-Categorize** | Webhook (nuovo documento) | Categorizza automaticamente il documento tramite LLM |
| **Backup Reminder** | Cron (settimanale) | Promemoria per eseguire il backup dei dati |

#### Frontend

| Componente | Tipo | Descrizione |
|---|---|---|
| `AutomationsPage` | Component (Smart) | Pagina per la gestione delle automazioni |
| `AutomationStore` | Signal Store | Stato reattivo delle automazioni |
| `WorkflowCard` | Component (Dumb) | Visualizzazione singolo workflow con stato e ultimo run |
| `WorkflowLog` | Component | Visualizzazione log esecuzioni workflow |

### Stima effort

| Attivita' | Effort stimato |
|---|---|
| Backend: Webhook service + adapter n8n | 2-3 settimane |
| Backend: AutomationController + REST API | 1-2 settimane |
| Template workflow n8n | 2 settimane |
| Frontend: Automations page | 2 settimane |
| Testing e integrazione | 1-2 settimane |
| **Totale Fase 5** | **8-11 settimane** |

### Criteri di completamento

- [ ] Webhook trigger funzionanti per almeno 3 tipi di eventi.
- [ ] Integrazione bidirezionale LocalMind-n8n funzionante.
- [ ] Almeno 3 template workflow predefiniti e testati.
- [ ] UI Angular con pagina automazioni.
- [ ] Test di integrazione per i webhook.

---

## 7. Fase 6: AI Agents

### Obiettivo

Implementare agenti AI specializzati con capacita' di tool calling, in grado di eseguire azioni complesse combinando LLM, RAG e strumenti esterni.

### Dipendenze

- **Fase 1 completata**: LLM Gateway.
- **Fase 3 completata**: RAG per knowledge retrieval.

### Deliverable

#### Backend

| Componente | Modulo | Descrizione |
|---|---|---|
| `AgentPort` | `localmind-domain` | Interfaccia porta per gli agenti AI |
| `AgentService` | `localmind-application` | Servizio per l'orchestrazione degli agenti |
| `ToolCallingFramework` | `localmind-application` | Framework per la definizione e l'esecuzione di tool calling |
| `AgentController` | `localmind-app` | REST API per l'interazione con gli agenti |

#### Agenti specializzati

| Agente | Specializzazione | Tool disponibili |
|---|---|---|
| **Tech Agent** | Analisi tecnica, code review, debugging | Ricerca documenti tecnici, esecuzione snippet, analisi log |
| **Business Agent** | Analisi business, report, KPI | Ricerca documenti business, calcolo metriche, generazione report |
| **Legal Agent** | Analisi documenti legali, compliance | Ricerca contratti, verifica clausole, confronto normative |
| **Personal Agent** | Assistente personale, organizzazione | Ricerca appunti, gestione task, riepilogo giornaliero |

#### Tool Calling Framework

```
Query utente
    │
    ▼
Agent selezionato
    │
    ▼
LLM decide se usare tool
    │
    ├── No tool necessario ──► Risposta diretta
    │
    └── Tool necessario ──► Esecuzione tool
                                │
                                ├── RAG search (ricerca semantica)
                                ├── Database query (metriche, statistiche)
                                ├── File system (lettura file)
                                └── Calcolo (operazioni matematiche)
                                │
                                ▼
                           Risultato tool integrato nella risposta
```

#### Frontend

| Componente | Tipo | Descrizione |
|---|---|---|
| `AgentsPage` | Component (Smart) | Pagina per la selezione e interazione con gli agenti |
| `AgentStore` | Signal Store | Stato reattivo degli agenti |
| `AgentSelector` | Component | Selezione dell'agente specializzato |
| `AgentConfigDialog` | Component | Configurazione personalizzata dell'agente |
| `ToolExecutionLog` | Component | Visualizzazione dei tool chiamati dall'agente |

### Stima effort

| Attivita' | Effort stimato |
|---|---|
| Backend: Tool calling framework | 3-4 settimane |
| Backend: Agent service + orchestrazione | 2-3 settimane |
| Backend: Implementazione 4 agenti | 4-6 settimane |
| Frontend: Agent pages + configuration | 3 settimane |
| Testing e integrazione | 2 settimane |
| **Totale Fase 6** | **14-18 settimane** |

### Criteri di completamento

- [ ] Tool calling framework funzionante.
- [ ] Almeno 2 agenti specializzati operativi (Tech, Business).
- [ ] RAG-augmented agent responses funzionanti.
- [ ] UI Angular con selezione agente e visualizzazione tool execution.
- [ ] Test unitari e di integrazione.

---

## 8. Riepilogo Roadmap

### Timeline complessiva

| Fase | Descrizione | Effort stimato | Dipendenze |
|---|---|---|---|
| **Fase 1** | LLM Gateway + Ollama | 8-11 settimane | Nessuna |
| **Fase 2** | File System Ingestion | 9-12 settimane | Fase 1 |
| **Fase 3** | RAG Base | 10-13 settimane | Fase 1 + Fase 2 |
| **Fase 4** | UI Angular Completa | 11-15 settimane | Fase 1 (parziale Fase 2-3) |
| **Fase 5** | Automazioni n8n | 8-11 settimane | Fase 1 + Fase 3 |
| **Fase 6** | AI Agents | 14-18 settimane | Fase 1 + Fase 3 |

**Nota:** Le fasi 4, 5 e 6 possono essere sviluppate in parziale parallelismo, riducendo il tempo totale.

### Effort totale stimato

| Scenario | Settimane | Mesi |
|---|---|---|
| Sviluppo sequenziale | 60-80 settimane | 15-20 mesi |
| Sviluppo con parallelismo | 40-55 settimane | 10-14 mesi |

### Versioning

| Versione | Fasi incluse | Milestone |
|---|---|---|
| **v0.1.0** | Fase 1 (parziale) | Scaffolding + chat base |
| **v0.2.0** | Fase 1 (completa) | Chat funzionante con Ollama |
| **v0.3.0** | Fase 2 | Indicizzazione documenti |
| **v0.4.0** | Fase 3 | RAG funzionante |
| **v0.5.0** | Fase 4 | UI completa |
| **v0.6.0** | Fase 5 | Automazioni |
| **v1.0.0** | Fase 6 | AI Agents + stabilizzazione |
