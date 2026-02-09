# Evoluzione Futura

| | |
|---|---|
| **Documento** | Evoluzione Futura e Visione |
| **Versione** | 0.1.0 |
| **Data** | 2026-02-09 |
| **Progetto** | LocalMind |

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Feature Pianificate Post-v1.0](#2-feature-pianificate-post-v10)
   - 2.1 [SSE/WebSocket Streaming](#21-ssewebsocket-streaming)
   - 2.2 [OCR Integrato](#22-ocr-integrato)
   - 2.3 [Multi-Utente con Autenticazione](#23-multi-utente-con-autenticazione)
   - 2.4 [Plugin System](#24-plugin-system)
   - 2.5 [Mobile Responsive / PWA](#25-mobile-responsive--pwa)
   - 2.6 [API SDK per Integrazione Esterna](#26-api-sdk-per-integrazione-esterna)
   - 2.7 [Supporto Modelli Multimodali](#27-supporto-modelli-multimodali)
   - 2.8 [Knowledge Graph](#28-knowledge-graph)
   - 2.9 [Fine-Tuning Locale](#29-fine-tuning-locale)
   - 2.10 [Marketplace Agenti e Workflow](#210-marketplace-agenti-e-workflow)
   - 2.11 [Backup e Restore Configurazione](#211-backup-e-restore-configurazione)
   - 2.12 [Import/Export Conversazioni](#212-importexport-conversazioni)
   - 2.13 [Supporto Multi-Lingua UI](#213-supporto-multi-lingua-ui)
   - 2.14 [Metriche Avanzate e Analytics](#214-metriche-avanzate-e-analytics)
   - 2.15 [Integrazione Calendario e Email](#215-integrazione-calendario-e-email)
3. [Evoluzione Architetturale](#3-evoluzione-architetturale)
   - 3.1 [Microservizi](#31-microservizi)
   - 3.2 [Event-Driven Architecture](#32-event-driven-architecture)
   - 3.3 [CQRS](#33-cqrs)
   - 3.4 [Kubernetes Deployment](#34-kubernetes-deployment)
4. [Community e Open Source](#4-community-e-open-source)
5. [Matrice Priorita' e Impatto](#5-matrice-priorita-e-impatto)

---

## 1. Panoramica

Questo documento descrive la visione a lungo termine di LocalMind, delineando le feature pianificate per le versioni successive alla v1.0.0, le evoluzioni architetturali previste e la strategia per la community e l'open source.

Le feature elencate rappresentano un'aspirazione progettuale e non un impegno vincolante. La loro implementazione sara' guidata da:

- **Feedback degli utenti**: le feature piu' richieste avranno priorita' maggiore.
- **Maturita' tecnologica**: alcune feature dipendono dall'evoluzione di tecnologie esterne (modelli LLM, framework, librerie).
- **Risorse disponibili**: lo sviluppo sara' calibrato sulle risorse effettivamente disponibili.
- **Coerenza architetturale**: ogni feature deve integrarsi armoniosamente con l'architettura esistente.

---

## 2. Feature Pianificate Post-v1.0

### 2.1 SSE/WebSocket Streaming

**Versione target:** v1.1.0

**Descrizione:** Implementazione dello streaming delle risposte LLM in tempo reale, permettendo all'utente di visualizzare la risposta token per token durante la generazione.

**Motivazione:** Attualmente, la risposta LLM viene restituita integralmente al termine della generazione. Con modelli grandi o prompt complessi, questo comporta tempi di attesa di diversi secondi durante i quali l'utente non riceve alcun feedback.

**Dettaglio tecnico:**

| Tecnologia | Approccio | Vantaggi |
|---|---|---|
| **Server-Sent Events (SSE)** | Flusso unidirezionale server-to-client | Semplice, supportato nativamente dai browser, compatibile con HTTP/2 |
| **WebSocket** | Connessione bidirezionale full-duplex | Comunicazione bidirezionale, minor overhead per messaggi frequenti |

**Implementazione prevista:**

```
Client (Angular)                    Server (Spring Boot)
     │                                      │
     │──── POST /api/v1/chat/stream ────►   │
     │                                      │──► Ollama (streaming)
     │◄──── SSE: token 1 ─────────────     │
     │◄──── SSE: token 2 ─────────────     │
     │◄──── SSE: token 3 ─────────────     │
     │◄──── SSE: [DONE] ──────────────     │
     │                                      │
```

Spring Boot supporta nativamente SSE tramite `SseEmitter` o il tipo reattivo `Flux<ServerSentEvent>`. Spring AI fornisce gia' API di streaming per i provider supportati.

---

### 2.2 OCR Integrato

**Versione target:** v1.2.0

**Descrizione:** Integrazione di un motore OCR (Optical Character Recognition) per l'estrazione di testo da documenti scansionati, immagini e PDF non testuali.

**Motivazione:** Molti documenti aziendali e personali sono disponibili esclusivamente come scansioni o immagini (fatture, contratti cartacei digitalizzati, appunti fotografati). Senza OCR, questi documenti non possono essere indicizzati dalla pipeline RAG.

**Tecnologia proposta:**

| Motore | Licenza | Lingue | Self-hosted |
|---|---|---|---|
| **Tesseract** | Apache 2.0 | 100+ lingue | Si' (container Docker) |
| **EasyOCR** | Apache 2.0 | 80+ lingue | Si' (Python) |
| **PaddleOCR** | Apache 2.0 | 80+ lingue | Si' (Python) |

**Flusso previsto:**

```
Documento scansionato (immagine/PDF)
    │
    ▼
[OCR] ──► Testo estratto
    │
    ▼
[Pipeline RAG standard] ──► Chunk, embed, store
```

---

### 2.3 Multi-Utente con Autenticazione

**Versione target:** v1.3.0

**Descrizione:** Supporto per utenti multipli con autenticazione JWT/OAuth2 e isolamento dei dati per utente.

**Motivazione:** Nella versione 1.0, LocalMind e' progettato per un singolo utente. Il supporto multi-utente consente l'utilizzo in contesti familiari o di piccoli team, mantenendo il principio self-hosted.

**Dettaglio:**

| Aspetto | Implementazione |
|---|---|
| **Autenticazione** | JWT (locale) o OAuth2/OIDC (Keycloak self-hosted) |
| **Isolamento dati** | Ogni utente vede solo i propri documenti e conversazioni |
| **Ruoli** | Admin, User, Viewer |
| **Gestione utenti** | Pagina admin per creazione/modifica/eliminazione utenti |
| **Quota** | Limiti configurabili per utente (storage, token LLM) |

---

### 2.4 Plugin System

**Versione target:** v1.4.0

**Descrizione:** Sistema di plugin che consenta alla community di estendere le funzionalita' di LocalMind senza modificare il codice core.

**Motivazione:** Un sistema di plugin favorisce l'adozione e la personalizzazione, permettendo a sviluppatori terzi di aggiungere funzionalita' specifiche per i propri casi d'uso.

**Architettura proposta:**

```
LocalMind Core
    │
    ├── Plugin API (interfacce stabili)
    │       │
    │       ├── LlmProviderPlugin     (nuovi provider LLM)
    │       ├── ParserPlugin          (nuovi formati file)
    │       ├── VectorStorePlugin     (nuovi vector store)
    │       ├── AgentPlugin           (nuovi agenti specializzati)
    │       └── UIPlugin              (nuovi componenti UI)
    │
    ├── Plugin Registry (discovery e lifecycle)
    │
    └── Plugin Sandbox (isolamento e sicurezza)
```

**Meccanismo di caricamento:**

- JAR plugin nella directory `/plugins`.
- Class loader isolato per ogni plugin.
- Interfacce stabili e versionate (backward compatibility).
- Configurazione plugin tramite file YAML.

---

### 2.5 Mobile Responsive / PWA

**Versione target:** v1.5.0

**Descrizione:** Trasformazione del frontend Angular in una Progressive Web App (PWA) con supporto completo per dispositivi mobile.

**Funzionalita' PWA:**

| Funzionalita' | Descrizione |
|---|---|
| **Installabile** | Aggiunta alla home screen del dispositivo |
| **Offline first** | Funzionamento base anche senza connessione (cache locale) |
| **Responsive** | Layout ottimizzato per smartphone e tablet |
| **Push notifications** | Notifiche per eventi (documento indicizzato, workflow completato) |
| **Service Worker** | Caching intelligente delle risorse statiche |

---

### 2.6 API SDK per Integrazione Esterna

**Versione target:** v1.6.0

**Descrizione:** Software Development Kit (SDK) per integrare le funzionalita' di LocalMind in applicazioni esterne.

**SDK previsti:**

| SDK | Linguaggio | Utilizzo |
|---|---|---|
| `localmind-sdk-java` | Java/Kotlin | Integrazione in applicazioni JVM |
| `localmind-sdk-python` | Python | Integrazione in script e applicazioni Python |
| `localmind-sdk-js` | JavaScript/TypeScript | Integrazione in applicazioni web e Node.js |

**Funzionalita' esposte:**

```python
# Esempio: localmind-sdk-python
from localmind import LocalMindClient

client = LocalMindClient(base_url="http://localhost:8080")

# Chat
response = client.chat("Riassumi il documento sulle vendite Q4")

# Ricerca semantica
results = client.search("politica ferie aziendali", top_k=5)

# Indicizzazione documento
client.documents.index("/path/to/document.pdf")
```

---

### 2.7 Supporto Modelli Multimodali

**Versione target:** v2.0.0

**Descrizione:** Supporto per modelli LLM multimodali capaci di elaborare immagini, audio e video oltre al testo.

**Casi d'uso:**

| Input | Modello | Caso d'uso |
|---|---|---|
| **Immagini** | LLaVA, GPT-4V | Analisi foto, descrizione immagini, estrazione dati da screenshot |
| **Audio** | Whisper | Trascrizione riunioni, note vocali, podcast |
| **Video** | Futuro | Analisi contenuti video, estrazione frame chiave |

**Impatto architetturale:**

- Estensione dell'interfaccia `LlmPort` per supportare input multimodali.
- Nuovi adapter per modelli multimodali.
- Storage per file multimediali (immagini, audio, video).
- Pipeline di pre-processing per ogni tipo di media.

---

### 2.8 Knowledge Graph

**Versione target:** v2.1.0

**Descrizione:** Integrazione di un knowledge graph in aggiunta al vector store, per rappresentare relazioni strutturate tra entita' estratte dai documenti.

**Motivazione:** Il vector store eccelle nella ricerca per similarita' semantica, ma non cattura le relazioni esplicite tra entita' (persone, organizzazioni, concetti). Un knowledge graph complementa il vector store offrendo navigazione relazionale.

**Tecnologia proposta:**

| Componente | Tecnologia | Self-hosted |
|---|---|---|
| Graph database | **Neo4j Community** | Si' (container Docker) |
| NER (Named Entity Recognition) | spaCy o modello LLM | Si' |
| Relation extraction | Modello LLM | Si' |

**Flusso:**

```
Documento
    │
    ├──► [Pipeline RAG] ──► Embedding (Qdrant)
    │
    └──► [Pipeline KG] ──► Entita' e relazioni (Neo4j)
             │
             ├── NER: estrazione entita' (persone, luoghi, org)
             └── RE: estrazione relazioni tra entita'
```

---

### 2.9 Fine-Tuning Locale

**Versione target:** v2.2.0

**Descrizione:** Possibilita' di eseguire fine-tuning locale di modelli LLM sui documenti dell'utente, utilizzando tecniche a basso consumo di risorse come LoRA (Low-Rank Adaptation) e QLoRA (Quantized LoRA).

**Motivazione:** Il fine-tuning consente di specializzare un modello generico sul dominio specifico dell'utente, migliorando significativamente la qualita' delle risposte per il proprio contesto.

**Requisiti hardware:**

| Tecnica | VRAM minima | Tempo stimato (1000 documenti) |
|---|---|---|
| LoRA (7B params) | 8 GB | 2-4 ore |
| QLoRA (7B params) | 4 GB | 4-8 ore |
| LoRA (13B params) | 16 GB | 6-12 ore |

**Flusso:**

```
Documenti utente ──► Dataset di training (automatico)
    │
    ▼
Modello base (es. llama3.2) + LoRA adapter
    │
    ▼
Fine-tuning locale (GPU)
    │
    ▼
Modello personalizzato (base + LoRA weights)
    │
    ▼
Deploy su Ollama locale
```

---

### 2.10 Marketplace Agenti e Workflow

**Versione target:** v2.3.0

**Descrizione:** Piattaforma per la condivisione e distribuzione di agenti specializzati e workflow n8n creati dalla community.

**Funzionalita':**

| Aspetto | Descrizione |
|---|---|
| **Catalogo** | Lista di agenti e workflow disponibili con descrizione, rating, download |
| **Installazione** | One-click install da UI LocalMind |
| **Versionamento** | Gestione versioni degli agenti e workflow |
| **Review** | Sistema di recensioni e rating da parte della community |
| **Publishing** | Possibilita' per gli utenti di pubblicare i propri agenti e workflow |

---

### 2.11 Backup e Restore Configurazione

**Versione target:** v1.2.0

**Descrizione:** Funzionalita' integrata per il backup e il restore dell'intera configurazione di LocalMind, inclusi database, vector store, workflow e impostazioni utente.

**Funzionalita':**

- Backup completo in un singolo file compresso (.tar.gz).
- Backup selettivo (solo database, solo configurazione, solo workflow).
- Backup schedulato (giornaliero, settimanale).
- Restore con verifica di integrita'.
- Export/Import configurazione tra installazioni diverse.

---

### 2.12 Import/Export Conversazioni

**Versione target:** v1.1.0

**Descrizione:** Possibilita' di esportare le conversazioni in formati standard e di importare conversazioni da altri strumenti.

**Formati supportati:**

| Formato | Export | Import |
|---|---|---|
| JSON | Si' | Si' |
| Markdown | Si' | No |
| PDF | Si' | No |
| ChatGPT export | No | Si' |
| Claude export | No | Si' |

---

### 2.13 Supporto Multi-Lingua UI

**Versione target:** v1.3.0

**Descrizione:** Internazionalizzazione (i18n) dell'interfaccia utente con supporto per lingue multiple.

**Lingue pianificate:**

| Lingua | Codice | Priorita' |
|---|---|---|
| Italiano | `it` | Alta (lingua principale) |
| Inglese | `en` | Alta |
| Francese | `fr` | Media |
| Tedesco | `de` | Media |
| Spagnolo | `es` | Media |

**Implementazione:** Angular i18n o ngx-translate per la gestione delle traduzioni con file JSON per lingua.

---

### 2.14 Metriche Avanzate e Analytics

**Versione target:** v1.4.0

**Descrizione:** Dashboard di analytics avanzata con metriche dettagliate sull'utilizzo del sistema.

**Metriche previste:**

| Categoria | Metriche |
|---|---|
| **LLM Usage** | Token consumati per provider/modello/giorno, costo stimato, tempi di risposta |
| **RAG Performance** | Qualita' retrieval (precision, recall), coverage documenti |
| **Document Stats** | Documenti per tipo, dimensione media, tasso di aggiornamento |
| **User Activity** | Conversazioni per giorno, query piu' frequenti, orari di utilizzo |
| **System Health** | CPU, RAM, storage, uptime, errori per tipo |

---

### 2.15 Integrazione Calendario e Email

**Versione target:** v2.0.0

**Descrizione:** Integrazione con calendari (CalDAV, Google Calendar) e client email (IMAP/SMTP) per un assistente personale completo.

**Funzionalita':**

| Integrazione | Funzionalita' |
|---|---|
| **Calendario** | Consultazione appuntamenti, creazione eventi, promemoria intelligenti |
| **Email** | Lettura email, riepilogo inbox, bozze di risposta generate da LLM |

**Nota:** Queste integrazioni rispetteranno il principio local-first. Le credenziali email e calendario saranno gestite localmente, e i dati non transiteranno su servizi terzi. Per il calendario, si privilegera' il protocollo CalDAV (locale/self-hosted). Per l'email, si utilizzera' IMAP/SMTP diretto.

---

## 3. Evoluzione Architetturale

### 3.1 Microservizi

**Trigger:** Quando il monolite diventa troppo complesso per essere gestito efficacemente, o quando emerge la necessita' di scalare componenti individualmente.

**Decomposizione prevista:**

| Microservizio | Responsabilita' |
|---|---|
| `localmind-chat-service` | Gestione conversazioni e interazione LLM |
| `localmind-document-service` | Indicizzazione, parsing, gestione documenti |
| `localmind-search-service` | Ricerca semantica e RAG |
| `localmind-agent-service` | Orchestrazione agenti AI |
| `localmind-automation-service` | Integrazione n8n e workflow |
| `localmind-gateway` | API Gateway (routing, autenticazione, rate limiting) |

**Nota importante:** La migrazione a microservizi NON e' prevista nella roadmap a breve termine. L'architettura Hexagonal adottata fin dalla v0.1.0 facilita una futura decomposizione, ma il monolite modulare e' adeguato per le esigenze attuali e prevedibili.

### 3.2 Event-Driven Architecture

**Trigger:** Quando la comunicazione sincrona tra componenti diventa un collo di bottiglia, o quando emerge la necessita' di processamento asincrono su larga scala.

**Tecnologia proposta:**

| Componente | Tecnologia | Self-hosted |
|---|---|---|
| Message broker | **Apache Kafka** | Si' (container Docker) |
| Alternativa leggera | **RabbitMQ** | Si' (container Docker) |
| Alternativa embedded | **Spring Events** (gia' in uso) | Si' (in-process) |

**Eventi previsti:**

| Evento | Producer | Consumer |
|---|---|---|
| `DocumentIndexed` | Document Service | Search Service, Automation Service |
| `ConversationCompleted` | Chat Service | Analytics Service, Automation Service |
| `EmbeddingGenerated` | Search Service | Vector Store |
| `AgentTaskCompleted` | Agent Service | Chat Service, Automation Service |
| `CostThresholdExceeded` | Analytics Service | Automation Service (alert) |

**Evoluzione progressiva:**

```
v1.0: Spring Events (in-process, sincrono)
    │
    ▼
v1.x: Spring Events + @Async (in-process, asincrono)
    │
    ▼
v2.x: Apache Kafka / RabbitMQ (distribuito, asincrono)
```

### 3.3 CQRS

**Trigger:** Quando i pattern di lettura e scrittura divergono significativamente, o quando le query di lettura diventano complesse e richiedono modelli di dati ottimizzati.

**Descrizione:** Il pattern CQRS (Command Query Responsibility Segregation) separa il modello di lettura dal modello di scrittura, permettendo ottimizzazioni indipendenti.

**Applicabilita' in LocalMind:**

| Dominio | Command (Scrittura) | Query (Lettura) |
|---|---|---|
| **Documenti** | Indicizzazione, aggiornamento metadati | Ricerca, listing, filtri complessi |
| **Conversazioni** | Creazione messaggi, aggiornamento stato | Cronologia, ricerca, analytics |
| **Metriche** | Registrazione token/costi | Dashboard, report, grafici |

**Implementazione prevista:**

- **Modello di scrittura:** Entita' di dominio normalizzate su MySQL.
- **Modello di lettura:** Viste materializzate o tabelle denormalizzate ottimizzate per le query.
- **Sincronizzazione:** Eventi di dominio che aggiornano il modello di lettura.

### 3.4 Kubernetes Deployment

**Trigger:** Quando LocalMind viene adottato in contesti enterprise con requisiti di alta disponibilita', scalabilita' e gestione centralizzata.

**Componenti Kubernetes previsti:**

| Risorsa K8s | Utilizzo |
|---|---|
| **Deployment** | Per ogni microservizio (quando applicabile) |
| **StatefulSet** | Per MySQL e Qdrant (dati persistenti) |
| **Service** | Per comunicazione inter-pod |
| **Ingress** | Per esposizione API e frontend |
| **ConfigMap** | Per configurazioni non sensibili |
| **Secret** | Per credenziali e API key |
| **PersistentVolumeClaim** | Per storage persistente |
| **HorizontalPodAutoscaler** | Per scaling automatico |

**Helm Chart:** Distribuzione tramite Helm chart per installazione semplificata su qualsiasi cluster Kubernetes.

```bash
# Installazione futura via Helm
helm repo add localmind https://charts.localmind.io
helm install localmind localmind/localmind \
  --set ollama.gpu.enabled=true \
  --set mysql.persistence.size=50Gi
```

---

## 4. Community e Open Source

### Licenza

La licenza per il progetto LocalMind e' ancora da definire. Le opzioni in valutazione sono:

| Licenza | Caratteristiche | Pro | Contro |
|---|---|---|---|
| **MIT** | Permissiva, minimale | Massima adozione, semplicita' | Nessuna protezione copyleft |
| **Apache 2.0** | Permissiva con patent grant | Protezione brevetti, adozione enterprise | Leggermente piu' complessa |
| **AGPL 3.0** | Copyleft forte | Protegge il codice da appropriazione proprietaria | Puo' scoraggiare l'adozione enterprise |
| **BSL** | Source available con timer | Monetizzazione possibile, diventa open dopo X anni | Non e' open source puro |

**Decisione prevista:** Entro la v1.0.0.

### Contributing Guidelines

Linee guida per i contributori della community:

| Aspetto | Regola |
|---|---|
| **Code style** | Google Java Style Guide (backend), Angular Style Guide (frontend) |
| **Commit convention** | Conventional Commits (feat, fix, docs, refactor, test) |
| **Branch strategy** | GitFlow (main, develop, feature/*, release/*, hotfix/*) |
| **Pull request** | Template obbligatorio, almeno 1 review, CI green |
| **Issue tracker** | GitHub Issues con template (bug report, feature request) |
| **Code of conduct** | Contributor Covenant |

### Plugin Development Kit

Per facilitare lo sviluppo di plugin da parte della community:

| Risorsa | Descrizione |
|---|---|
| `localmind-plugin-api` | Artefatto Maven con le interfacce stabili per i plugin |
| `localmind-plugin-archetype` | Maven archetype per lo scaffolding di un nuovo plugin |
| Plugin documentation | Guida completa allo sviluppo di plugin |
| Plugin examples | Repository con esempi di plugin per ogni tipo |
| Plugin testing framework | Strumenti per il testing dei plugin in isolamento |

### Documentation Site

Pianificazione di un sito di documentazione dedicato:

| Aspetto | Tecnologia |
|---|---|
| **Framework** | Docusaurus o MkDocs |
| **Hosting** | GitHub Pages (gratuito) |
| **Contenuti** | Guida utente, guida sviluppatore, API reference, tutorial |
| **Versioning** | Documentazione versionata per ogni major release |
| **Search** | Ricerca full-text integrata |
| **Internazionalizzazione** | Italiano e inglese (minimo) |

---

## 5. Matrice Priorita' e Impatto

La seguente matrice classifica le feature per priorita' di implementazione e impatto sull'esperienza utente:

### Alta priorita', alto impatto

| # | Feature | Versione target |
|---|---|---|
| 1 | SSE/WebSocket Streaming | v1.1.0 |
| 12 | Import/Export Conversazioni | v1.1.0 |
| 11 | Backup e Restore | v1.2.0 |
| 3 | Multi-Utente | v1.3.0 |

### Alta priorita', medio impatto

| # | Feature | Versione target |
|---|---|---|
| 2 | OCR Integrato | v1.2.0 |
| 13 | Multi-Lingua UI | v1.3.0 |
| 14 | Metriche Avanzate | v1.4.0 |

### Media priorita', alto impatto

| # | Feature | Versione target |
|---|---|---|
| 4 | Plugin System | v1.4.0 |
| 5 | PWA | v1.5.0 |
| 7 | Modelli Multimodali | v2.0.0 |

### Media priorita', medio impatto

| # | Feature | Versione target |
|---|---|---|
| 6 | API SDK | v1.6.0 |
| 15 | Calendario e Email | v2.0.0 |
| 8 | Knowledge Graph | v2.1.0 |

### Bassa priorita' (lungo termine)

| # | Feature | Versione target |
|---|---|---|
| 9 | Fine-Tuning Locale | v2.2.0 |
| 10 | Marketplace | v2.3.0 |

### Evoluzione architetturale (on-demand)

| Feature | Trigger |
|---|---|
| Microservizi | Complessita' monolite insostenibile |
| Event-Driven (Kafka) | Necessita' di processamento asincrono distribuito |
| CQRS | Divergenza significativa pattern lettura/scrittura |
| Kubernetes | Adozione enterprise con requisiti HA |

Queste evoluzioni architetturali non hanno una versione target fissa, poiche' saranno attivate in base alle effettive necessita' del progetto e della sua community.
