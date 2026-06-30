export const content = `# Diagrammi di Flusso dei Processi Chiave

| Campo        | Valore                          |
|--------------|---------------------------------|
| **Documento**| Diagrammi di Flusso             |
| **Versione** | 0.1.0                           |
| **Data**     | 2026-02-09                      |
| **Progetto** | LocalMind                       |

---

## Indice

1. [Introduzione](#1-introduzione)
2. [Flusso 1: Chat Request](#2-flusso-1-chat-request)
3. [Flusso 2: Document Ingestion](#3-flusso-2-document-ingestion)
4. [Flusso 3: Semantic Search](#4-flusso-3-semantic-search)
5. [Flusso 4: Folder Scan](#5-flusso-4-folder-scan)
6. [Flusso 5: Automation Trigger](#6-flusso-5-automation-trigger)

---

## 1. Introduzione

Il presente documento illustra i cinque flussi operativi principali di LocalMind attraverso diagrammi di sequenza dettagliati. Ogni flusso descrive l'interazione tra i componenti del sistema dal punto di ingresso (utente o scheduler) fino al completamento dell'operazione.

Per ogni flusso vengono specificati:
- I componenti coinvolti con il relativo layer architetturale
- La sequenza temporale delle interazioni
- I dati scambiati tra i componenti
- I punti di decisione e le gestioni di errore

---

## 2. Flusso 1: Chat Request

### 2.1 Descrizione

Il flusso Chat Request descrive il percorso completo di un messaggio dall'utente fino alla risposta generata dall'LLM, includendo routing del provider, retry, fallback e cost tracking.

### 2.2 Componenti Coinvolti

| Componente           | Layer          | Responsabilita'                          |
|----------------------|----------------|------------------------------------------|
| Utente               | Esterno        | Invio messaggio                          |
| ChatController       | API            | Ricezione HTTP, validazione, mapping DTO |
| ChatUseCase          | Domain (port)  | Interfaccia use case                     |
| LlmGatewayService    | Domain         | Routing, fallback, orchestrazione        |
| LlmClient            | Domain (port)  | Interfaccia provider LLM                 |
| OllamaLlmAdapter     | Infrastructure | Comunicazione con Ollama                 |
| OpenAiLlmAdapter     | Infrastructure | Comunicazione con OpenAI (fallback)      |
| CostTrackingService  | Domain         | Calcolo e registrazione costi            |
| LlmUsageRepository   | Domain (port)  | Persistenza metriche                     |

### 2.3 Diagramma di Sequenza

\`\`\`
Utente       ChatController    LlmGatewayService    OllamaLlmAdapter    OpenAiLlmAdapter    CostTrackingService   LlmUsageRepo
  |               |                   |                    |                    |                    |                   |
  | POST          |                   |                    |                    |                    |                   |
  | /api/v1/chat  |                   |                    |                    |                    |                   |
  | {message,     |                   |                    |                    |                    |                   |
  |  provider?,   |                   |                    |                    |                    |                   |
  |  model?,      |                   |                    |                    |                    |                   |
  |  temperature?}|                   |                    |                    |                    |                   |
  |-------------->|                   |                    |                    |                    |                   |
  |               |                   |                    |                    |                    |                   |
  |               | validate(dto)     |                    |                    |                    |                   |
  |               |----+              |                    |                    |                    |                   |
  |               |    |              |                    |                    |                    |                   |
  |               |<---+              |                    |                    |                    |                   |
  |               |                   |                    |                    |                    |                   |
  |               | map(dto->domain)  |                    |                    |                    |                   |
  |               |----+              |                    |                    |                    |                   |
  |               |    |              |                    |                    |                    |                   |
  |               |<---+              |                    |                    |                    |                   |
  |               |                   |                    |                    |                    |                   |
  |               | chat(ChatRequest) |                    |                    |                    |                   |
  |               |------------------>|                    |                    |                    |                   |
  |               |                   |                    |                    |                    |                   |
  |               |                   | selectProvider()   |                    |                    |                   |
  |               |                   | [provider da       |                    |                    |                   |
  |               |                   |  request o default]|                    |                    |                   |
  |               |                   |----+               |                    |                    |                   |
  |               |                   |    | -> OLLAMA     |                    |                    |                   |
  |               |                   |<---+               |                    |                    |                   |
  |               |                   |                    |                    |                    |                   |
  |               |                   | isAvailable()      |                    |                    |                   |
  |               |                   |------------------->|                    |                    |                   |
  |               |                   |        true        |                    |                    |                   |
  |               |                   |<-------------------|                    |                    |                   |
  |               |                   |                    |                    |                    |                   |
  |               |                   | generate(request)  |                    |                    |                   |
  |               |                   |------------------->|                    |                    |                   |
  |               |                   |                    |                    |                    |                   |
  |               |                   |                    | POST /api/chat     |                    |                   |
  |               |                   |                    | -> Ollama:11434    |                    |                   |
  |               |                   |                    |----+               |                    |                   |
  |               |                   |                    |    |               |                    |                   |
  |               |                   |                    |<---+               |                    |                   |
  |               |                   |                    |                    |                    |                   |
  |               |                   | ChatResponse       |                    |                    |                   |
  |               |                   | {content, tokens,  |                    |                    |                   |
  |               |                   |  latencyMs}        |                    |                    |                   |
  |               |                   |<-------------------|                    |                    |                   |
  |               |                   |                    |                    |                    |                   |
  |               |                   | trackCost(response)|                    |                    |                   |
  |               |                   |------------------------------------------------------------->|                   |
  |               |                   |                    |                    |                    |                   |
  |               |                   |                    |                    |                    | calculateCost()   |
  |               |                   |                    |                    |                    |----+              |
  |               |                   |                    |                    |                    |    |              |
  |               |                   |                    |                    |                    |<---+              |
  |               |                   |                    |                    |                    |                   |
  |               |                   |                    |                    |                    | save(LlmUsage)    |
  |               |                   |                    |                    |                    |------------------>|
  |               |                   |                    |                    |                    |       OK          |
  |               |                   |                    |                    |                    |<------------------|
  |               |                   |                    |                    |                    |                   |
  |               |                   | ChatResponse       |                    |                    |                   |
  |               |                   | (con estimatedCost)|                    |                    |                   |
  |               |<------------------|                    |                    |                    |                   |
  |               |                   |                    |                    |                    |                   |
  |               | map(domain->dto)  |                    |                    |                    |                   |
  |               |----+              |                    |                    |                    |                   |
  |               |    |              |                    |                    |                    |                   |
  |               |<---+              |                    |                    |                    |                   |
  |               |                   |                    |                    |                    |                   |
  | 200 OK        |                   |                    |                    |                    |                   |
  | ChatResponseDto                   |                    |                    |                    |                   |
  |<--------------|                   |                    |                    |                    |                   |
\`\`\`

### 2.4 Flusso con Fallback (Ollama non disponibile)

\`\`\`
LlmGatewayService    OllamaLlmAdapter    OpenAiLlmAdapter    CostTrackingService
       |                    |                    |                    |
       | isAvailable()      |                    |                    |
       |------------------->|                    |                    |
       |      true          |                    |                    |
       |<-------------------|                    |                    |
       |                    |                    |                    |
       | generate(request)  |                    |                    |
       |------------------->|                    |                    |
       |   TIMEOUT (120s)   |                    |                    |
       |<-------------------|                    |                    |
       |                    |                    |                    |
       | [retry 1/3 - attesa 1000ms]             |                    |
       |------------------->|                    |                    |
       |   TIMEOUT          |                    |                    |
       |<-------------------|                    |                    |
       |                    |                    |                    |
       | [retry 2/3 - attesa 2000ms]             |                    |
       |------------------->|                    |                    |
       |   TIMEOUT          |                    |                    |
       |<-------------------|                    |                    |
       |                    |                    |                    |
       | [retry 3/3 - attesa 4000ms]             |                    |
       |------------------->|                    |                    |
       |   TIMEOUT          |                    |                    |
       |<-------------------|                    |                    |
       |                    |                    |                    |
       | [max retry - FALLBACK attivato]         |                    |
       | [next provider: OPENAI]                 |                    |
       |                    |                    |                    |
       | isAvailable()      |                    |                    |
       |---------------------------------------->|                    |
       |                true|                    |                    |
       |<----------------------------------------|                    |
       |                    |                    |                    |
       | generate(request)  |                    |                    |
       |---------------------------------------->|                    |
       |                    |                    |                    |
       |                    |                    | POST /v1/chat/     |
       |                    |                    | completions        |
       |                    |                    | -> api.openai.com  |
       |                    |                    |----+               |
       |                    |                    |    |               |
       |                    |                    |<---+               |
       |                    |                    |                    |
       | ChatResponse       |                    |                    |
       | {fallbackUsed:true}|                    |                    |
       |<----------------------------------------|                    |
       |                    |                    |                    |
       | trackCost()        |                    |                    |
       | [provider: OPENAI] |                    |                    |
       |------------------------------------------------------------->|
       |                    |                    |                    |
\`\`\`

---

## 3. Flusso 2: Document Ingestion

### 3.1 Descrizione

Il flusso Document Ingestion descrive il processo completo dall'upload di un documento fino alla sua indicizzazione nel vector store Qdrant, passando attraverso estrazione testo, chunking ed embedding.

### 3.2 Componenti Coinvolti

| Componente              | Layer          | Responsabilita'                          |
|-------------------------|----------------|------------------------------------------|
| Utente                  | Esterno        | Upload del documento                     |
| DocumentController      | API            | Ricezione multipart, validazione         |
| DocumentService         | Domain         | Hash, dedup, salvataggio PENDING         |
| DocumentRepository      | Domain (port)  | Persistenza metadati documento           |
| Batch Job               | Batch          | Orchestrazione processing asincrono      |
| TikaTextExtractor       | Infrastructure | Estrazione testo dal file                |
| ChunkingService         | Domain         | Suddivisione in chunk                    |
| QdrantVectorStoreAdapter| Infrastructure | Storage embedding in Qdrant              |

### 3.3 Diagramma di Sequenza

\`\`\`
Utente    DocController   DocumentService   DocRepository   BatchJob    TikaExtractor   ChunkingService   QdrantAdapter
  |            |               |                |              |              |                |               |
  | POST       |               |                |              |              |                |               |
  | /upload    |               |                |              |              |                |               |
  | (multipart)|               |                |              |              |                |               |
  |----------->|               |                |              |              |                |               |
  |            |               |                |              |              |                |               |
  |            | validate      |                |              |              |                |               |
  |            | (format,size) |                |              |              |                |               |
  |            |----+          |                |              |              |                |               |
  |            |    |          |                |              |              |                |               |
  |            |<---+          |                |              |              |                |               |
  |            |               |                |              |              |                |               |
  |            | ingest(file,  |                |              |              |                |               |
  |            |  name, type)  |                |              |              |                |               |
  |            |-------------->|                |              |              |                |               |
  |            |               |                |              |              |                |               |
  |            |               | hash = SHA-256 |              |              |                |               |
  |            |               | (file content) |              |              |                |               |
  |            |               |----+           |              |              |                |               |
  |            |               |    |           |              |              |                |               |
  |            |               |<---+           |              |              |                |               |
  |            |               |                |              |              |                |               |
  |            |               | existsByHash   |              |              |                |               |
  |            |               | (hash)?        |              |              |                |               |
  |            |               |--------------->|              |              |                |               |
  |            |               |    false       |              |              |                |               |
  |            |               |<---------------|              |              |                |               |
  |            |               |                |              |              |                |               |
  |            |               | doc = new      |              |              |                |               |
  |            |               | Document(      |              |              |                |               |
  |            |               |  PENDING)      |              |              |                |               |
  |            |               |----+           |              |              |                |               |
  |            |               |    |           |              |              |                |               |
  |            |               |<---+           |              |              |                |               |
  |            |               |                |              |              |                |               |
  |            |               | save(doc)      |              |              |                |               |
  |            |               |--------------->|              |              |                |               |
  |            |               |    docId       |              |              |                |               |
  |            |               |<---------------|              |              |                |               |
  |            |               |                |              |              |                |               |
  |            | Document      |                |              |              |                |               |
  |            | (PENDING)     |                |              |              |                |               |
  |            |<--------------|                |              |              |                |               |
  |  202       |               |                |              |              |                |               |
  |  Accepted  |               |                |              |              |                |               |
  |<-----------|               |                |              |              |                |               |
  |            |               |                |              |              |                |               |
  |            |               |                |  ======== ELABORAZIONE ASINCRONA ========    |               |
  |            |               |                |              |              |                |               |
  |            |               |                |  [Batch Job  |              |                |               |
  |            |               |                |   attivato   |              |                |               |
  |            |               |                |   da cron o  |              |                |               |
  |            |               |                |   trigger]   |              |                |               |
  |            |               |                |              |              |                |               |
  |            |               |                | findPending()|              |                |               |
  |            |               |                |<-------------|              |                |               |
  |            |               |                | List<Doc>    |              |                |               |
  |            |               |                |------------->|              |                |               |
  |            |               |                |              |              |                |               |
  |            |               |                |              | [Per ogni documento PENDING]  |               |
  |            |               |                |              |              |                |               |
  |            |               |                | update       |              |                |               |
  |            |               |                | (PROCESSING) |              |                |               |
  |            |               |                |<-------------|              |                |               |
  |            |               |                |              |              |                |               |
  |            |               |                |              | extract      |                |               |
  |            |               |                |              | (fileContent)|                |               |
  |            |               |                |              |------------->|                |               |
  |            |               |                |              |              |                |               |
  |            |               |                |              |              | Apache Tika    |               |
  |            |               |                |              |              | parse(PDF/     |               |
  |            |               |                |              |              | DOCX/TXT/EML)  |               |
  |            |               |                |              |              |----+           |               |
  |            |               |                |              |              |    |           |               |
  |            |               |                |              |              |<---+           |               |
  |            |               |                |              |              |                |               |
  |            |               |                |              | plainText    |                |               |
  |            |               |                |              |<-------------|                |               |
  |            |               |                |              |              |                |               |
  |            |               |                |              | chunk(text,  |                |               |
  |            |               |                |              |  500, 50)    |                |               |
  |            |               |                |              |------------------------------>|               |
  |            |               |                |              |              |                |               |
  |            |               |                |              |              |  split text    |               |
  |            |               |                |              |              |  into chunks   |               |
  |            |               |                |              |              |  with overlap  |               |
  |            |               |                |              |              |                |               |
  |            |               |                |              | List<Chunk>  |                |               |
  |            |               |                |              |<------------------------------|               |
  |            |               |                |              |              |                |               |
  |            |               |                |              | embed + store|                |               |
  |            |               |                |              | (chunks)     |                |               |
  |            |               |                |              |---------------------------------------------->|
  |            |               |                |              |              |                |               |
  |            |               |                |              |              |                | embed(chunk)  |
  |            |               |                |              |              |                | -> Ollama     |
  |            |               |                |              |              |                | nomic-embed   |
  |            |               |                |              |              |                |               |
  |            |               |                |              |              |                | upsert points |
  |            |               |                |              |              |                | -> Qdrant     |
  |            |               |                |              |              |                |               |
  |            |               |                |              |    OK        |                |               |
  |            |               |                |              |<----------------------------------------------|
  |            |               |                |              |              |                |               |
  |            |               |                | update       |              |                |               |
  |            |               |                | (INDEXED,    |              |                |               |
  |            |               |                |  chunkCount) |              |                |               |
  |            |               |                |<-------------|              |                |               |
  |            |               |                |              |              |                |               |
  |            |               |                |              | [Se errore in qualsiasi fase] |               |
  |            |               |                | update       |              |                |               |
  |            |               |                | (FAILED,     |              |                |               |
  |            |               |                |  errorMsg)   |              |                |               |
  |            |               |                |<-------------|              |                |               |
\`\`\`

---

## 4. Flusso 3: Semantic Search

### 4.1 Descrizione

Il flusso Semantic Search descrive il processo di ricerca semantica nei documenti indicizzati, dall'embedding della query alla restituzione dei risultati ordinati per similarita'.

### 4.2 Componenti Coinvolti

| Componente              | Layer          | Responsabilita'                          |
|-------------------------|----------------|------------------------------------------|
| Utente                  | Esterno        | Invio query di ricerca                   |
| DocumentSearchController| API            | Ricezione query, validazione             |
| DocumentSearchUseCase   | Domain (port)  | Interfaccia ricerca                      |
| DocumentService         | Domain         | Orchestrazione ricerca                   |
| VectorStorePort         | Domain (port)  | Interfaccia vector store                 |
| QdrantVectorStoreAdapter| Infrastructure| Ricerca similarita' in Qdrant             |

### 4.3 Diagramma di Sequenza

\`\`\`
Utente    SearchController   DocumentService   QdrantAdapter        Qdrant         OllamaAdapter
  |            |                   |                |                   |                |
  | GET        |                   |                |                   |                |
  | /api/v1/   |                   |                |                   |                |
  | documents/ |                   |                |                   |                |
  | search     |                   |                |                   |                |
  | ?query=... |                   |                |                   |                |
  | &topK=5    |                   |                |                   |                |
  |----------->|                   |                |                   |                |
  |            |                   |                |                   |                |
  |            | search(query, 5)  |                |                   |                |
  |            |------------------>|                |                   |                |
  |            |                   |                |                   |                |
  |            |                   | embedQuery     |                   |                |
  |            |                   | (query)        |                   |                |
  |            |                   |---------------------------------------------------->|
  |            |                   |                |                   |                |
  |            |                   |                |                   |                | POST
  |            |                   |                |                   |                | /api/embeddings
  |            |                   |                |                   |                | -> Ollama
  |            |                   |                |                   |                | model:
  |            |                   |                |                   |                | nomic-embed-text
  |            |                   |                |                   |                |----+
  |            |                   |                |                   |                |    |
  |            |                   |                |                   |                |<---+
  |            |                   |                |                   |                |
  |            |                   | float[]        |                   |                |
  |            |                   | queryEmbedding |                   |                |
  |            |                   |<----------------------------------------------------|
  |            |                   |                |                   |                |
  |            |                   | search         |                   |                |
  |            |                   | (embedding, 5) |                   |                |
  |            |                   |--------------->|                   |                |
  |            |                   |                |                   |                |
  |            |                   |                | gRPC:SearchPoints |                |
  |            |                   |                | collection:       |                |
  |            |                   |                | localmind_docs    |                |
  |            |                   |                | vector: embedding |                |
  |            |                   |                | limit: 5          |                |
  |            |                   |                |------------------>|                |
  |            |                   |                |                   |                |
  |            |                   |                |                   | cosine         |
  |            |                   |                |                   | similarity     |
  |            |                   |                |                   | search         |
  |            |                   |                |                   |----+           |
  |            |                   |                |                   |    |           |
  |            |                   |                |                   |<---+           |
  |            |                   |                |                   |                |
  |            |                   |                | ScoredPoints      |                |
  |            |                   |                | [{id, score,      |                |
  |            |                   |                |   payload}]       |                |
  |            |                   |                |<------------------|                |
  |            |                   |                |                   |                |
  |            |                   |                | map to            |                |
  |            |                   |                | SearchResult[]    |                |
  |            |                   |                |----+              |                |
  |            |                   |                |    |              |                |
  |            |                   |                |<---+              |                |
  |            |                   |                |                   |                |
  |            |                   | List<SearchResult>                 |                |
  |            |                   | [{documentId,  |                   |                |
  |            |                   |   filename,    |                   |                |
  |            |                   |   chunkIndex,  |                   |                |
  |            |                   |   text,        |                   |                |
  |            |                   |   score}]      |                   |                |
  |            |                   |<---------------|                   |                |
  |            |                   |                |                   |                |
  |            | List<SearchResultDto>              |                   |                |
  |            |<------------------|                |                   |                |
  |            |                   |                |                   |                |
  | 200 OK     |                   |                |                   |                |
  | JSON array |                   |                |                   |                |
  | of results |                   |                |                   |                |
  |<-----------|                   |                |                   |                |
\`\`\`

---

## 5. Flusso 4: Folder Scan

### 5.1 Descrizione

Il flusso Folder Scan descrive il processo automatico di scansione delle cartelle configurate, rilevamento di nuovi file e invio al pipeline RAG per l'indicizzazione.

### 5.2 Componenti Coinvolti

| Componente             | Layer          | Responsabilita'                          |
|------------------------|----------------|------------------------------------------|
| BatchScheduler         | Batch          | Trigger cron per scansione               |
| FolderScanJobConfig    | Batch          | Configurazione job Spring Batch          |
| FileSystemScannerPort  | Domain (port)  | Interfaccia scansione filesystem         |
| LocalFileSystemScanner | Infrastructure | Scansione ricorsiva filesystem           |
| DocumentService        | Domain         | Hash, dedup, ingestione                  |
| DocumentRepository     | Domain (port)  | Persistenza metadati                     |

### 5.3 Diagramma di Sequenza

\`\`\`
Cron Scheduler    FolderScanJob    FileSystemScanner   DocumentService    DocRepository    RAG Pipeline
      |                |                  |                  |                 |                |
      | trigger        |                  |                  |                 |                |
      | (cron:         |                  |                  |                 |                |
      |  ogni 15 min)  |                  |                  |                 |                |
      |--------------->|                  |                  |                 |                |
      |                |                  |                  |                 |                |
      |                | loadFolderConfig |                  |                 |                |
      |                |----+             |                  |                 |                |
      |                |    | folders:    |                  |                 |                |
      |                |    | - /docs/work|                  |                 |                |
      |                |    |   recursive |                  |                 |                |
      |                |    | - /docs/pers|                  |                 |                |
      |                |    |   non-recurs|                  |                 |                |
      |                |<---+             |                  |                 |                |
      |                |                  |                  |                 |                |
      |                | [Per ogni folder configurata]       |                 |                |
      |                |                  |                  |                 |                |
      |                | scan(path,       |                  |                 |                |
      |                |  recursive,      |                  |                 |                |
      |                |  extensions)     |                  |                 |                |
      |                |----------------->|                  |                 |                |
      |                |                  |                  |                 |                |
      |                |                  | Files.walk()     |                 |                |
      |                |                  | o Files.list()   |                 |                |
      |                |                  | filter by ext    |                 |                |
      |                |                  | (.pdf,.docx,     |                 |                |
      |                |                  |  .txt,.eml)      |                 |                |
      |                |                  |----+             |                 |                |
      |                |                  |    |             |                 |                |
      |                |                  |<---+             |                 |                |
      |                |                  |                  |                 |                |
      |                | List<Path>       |                  |                 |                |
      |                | [file1.pdf,      |                  |                 |                |
      |                |  file2.docx,...] |                  |                 |                |
      |                |<-----------------|                  |                 |                |
      |                |                  |                  |                 |                |
      |                | [Per ogni file trovato]             |                 |                |
      |                |                  |                  |                 |                |
      |                | hash = SHA-256   |                  |                 |                |
      |                | (file content)   |                  |                 |                |
      |                |----+             |                  |                 |                |
      |                |    |             |                  |                 |                |
      |                |<---+             |                  |                 |                |
      |                |                  |                  |                 |                |
      |                |                  |                  | existsByHash    |                |
      |                |                  |                  | (hash)?         |                |
      |                |------------------------------------------------------>|                |
      |                |                  |                  |                 |                |
      |                |                  |                  |   [Se false: nuovo file]         |
      |                |                  |                  |                 |                |
      |                | ingest(file)     |                  |                 |                |
      |                |------------------------------------>|                 |                |
      |                |                  |                  |                 |                |
      |                |                  |                  | save(PENDING)   |                |
      |                |                  |                  |---------------->|                |
      |                |                  |                  |                 |                |
      |                |                  |                  |   [Se true: file gia' indicizzato]
      |                |                  |                  |   -> skip con log                |
      |                |                  |                  |                 |                |
      |                | [Fine loop file] |                  |                 |                |
      |                |                  |                  |                 |                |
      |                | [I documenti PENDING vengono processati               |                |
      |                |  dal DocumentIngestionJob (Flusso 2)]                 |                |
      |                |                  |                  |                 |                |
      |                | triggerIngestionJob()               |                 |                |
      |                |----------------------------------------------------------------------->|
      |                |                  |                  |                 |                |
      |                | jobCompleted     |                  |                 |                |
      |                | (scanned: 42,    |                  |                 |                |
      |                |  new: 3,         |                  |                 |                |
      |                |  skipped: 39)    |                  |                 |                |
      |                |----+             |                  |                 |                |
      |                |    |             |                  |                 |                |
      |                |<---+             |                  |                 |                |
\`\`\`

---

## 6. Flusso 5: Automation Trigger

### 6.1 Descrizione

Il flusso Automation Trigger descrive il processo di generazione di un evento interno in LocalMind e il suo inoltro a n8n tramite webhook HTTP per l'esecuzione di workflow automatici.

### 6.2 Componenti Coinvolti

| Componente             | Layer          | Responsabilita'                          |
|------------------------|----------------|------------------------------------------|
| Event Source           | Domain         | Componente che genera l'evento           |
| AutomationService      | Domain         | Gestione eventi e dispatching            |
| WebhookRepository      | Domain (port)  | Recupero webhook registrati              |
| WebhookClientPort      | Domain (port)  | Invio HTTP webhook                       |
| N8nWebhookClient       | Infrastructure | Client HTTP per n8n                      |
| n8n                    | Esterno        | Piattaforma automazione                  |

### 6.3 Diagramma di Sequenza

\`\`\`
EventSource      AutomationService    WebhookRepository   N8nWebhookClient         n8n           Servizi Esterni
(DocumentService)|                   |                    |                         |                    |
      |          |                   |                    |                         |                    |
      | [Documento indicizzato       |                    |                         |                    |
      |  con successo]               |                    |                         |                    |
      |          |                   |                    |                         |                    |
      | fireEvent|                   |                    |                         |                    |
      | (DOCUMENT|                   |                    |                         |                    |
      | _INDEXED,|                   |                    |                         |                    |
      |  {docId, |                   |                    |                         |                    |
      |   name,  |                   |                    |                         |                    |
      |  chunks})|                   |                    |                         |                    |
      |--------->|                   |                    |                         |                    |
      |          |                   |                    |                         |                    |
      |          | buildPayload      |                    |                         |                    |
      |          | (event, data)     |                    |                         |                    |
      |          |----+              |                    |                         |                    |
      |          |    |              |                    |                         |                    |
      |          |<---+              |                    |                         |                    |
      |          |                   |                    |                         |                    |
      |          | findByEventType   |                    |                         |                    |
      |          | (DOCUMENT_INDEXED)|                    |                         |                    |
      |          |------------------>|                    |                         |                    |
      |          |                   |                    |                         |                    |
      |          | List<Webhook>     |                    |                         |                    |
      |          | [{url: "http://   |                    |                         |                    |
      |          |   n8n:5678/       |                    |                         |                    |
      |          |   webhook/abc"}]  |                    |                         |                    |
      |          |<------------------|                    |                         |                    |
      |          |                   |                    |                         |                    |
      |          | [Per ogni webhook trovato (enabled)]   |                         |                    |
      |          |                   |                    |                         |                    |
      |          | send(url, payload)|                    |                         |                    |
      |          |--------------------------------------->|                         |                    |
      |          |                   |                    |                         |                    |
      |          |                   |                    | POST                    |                    |
      |          |                   |                    | http://n8n:5678/        |                    |
      |          |                   |                    | webhook/abc             |                    |
      |          |                   |                    | Content-Type:           |                    |
      |          |                   |                    | application/json        |                    |
      |          |                   |                    | Body: {                 |                    |
      |          |                   |                    |   eventType:            |                    |
      |          |                   |                    |   "DOCUMENT_INDEXED",   |                    |
      |          |                   |                    |   timestamp: "...",     |                    |
      |          |                   |                    |   data: {               |                    |
      |          |                   |                    |     documentId: "...",  |                    |
      |          |                   |                    |     filename: "...",    |                    |
      |          |                   |                    |     chunkCount: 12      |                    |
      |          |                   |                    |   }                     |                    |
      |          |                   |                    | }                       |                    |
      |          |                   |                    |------------------------>|                    |
      |          |                   |                    |                         |                    |
      |          |                   |                    |                         | [n8n workflow      |
      |          |                   |                    |                         |  esecuzione]       |
      |          |                   |                    |                         |                    |
      |          |                   |                    |                         | Azione 1:          |
      |          |                   |                    |                         | GET /api/v1/       |
      |          |                   |                    |                         | documents/{id}     |
      |          |                   |                    |                         | -> LocalMind API   |
      |          |                   |                    |                         |                    |
      |          |                   |                    |                         | Azione 2:          |
      |          |                   |                    |                         | POST /api/v1/      |
      |          |                   |                    |                         | agents/execute     |
      |          |                   |                    |                         | {type: BUSINESS,   |
      |          |                   |                    |                         |  query: "Genera    |
      |          |                   |                    |                         |  una sintesi..."}  |
      |          |                   |                    |                         |                    |
      |          |                   |                    |                         | Azione 3:          |
      |          |                   |                    |                         | Invio email        |
      |          |                   |                    |                         | con sintesi        |
      |          |                   |                    |                         |------------------->|
      |          |                   |                    |                         |                    |
      |          |                   |                    |                         |           OK       |
      |          |                   |                    |                         |<-------------------|
      |          |                   |                    |                         |                    |
      |          |                   |                    |  200 OK                 |                    |
      |          |                   |                    |<------------------------|                    |
      |          |                   |                    |                         |                    |
      |          |  webhookSent: OK  |                    |                         |                    |
      |          |<---------------------------------------|                         |                    |
      |          |                   |                    |                         |                    |
      |          | [Se errore: retry con backoff]         |                         |                    |
      |          | [Max 3 tentativi, backoff 2000ms]      |                         |                    |
      |          |                   |                    |                         |                    |
      |  OK      |                   |                    |                         |                    |
      |<---------|                   |                    |                         |                    |
\`\`\`

### 6.4 Gestione Errori Webhook

| Scenario                    | Azione                                              |
|-----------------------------|-----------------------------------------------------|
| n8n non raggiungibile       | Retry (3 tentativi, backoff 2000ms), poi log errore |
| n8n risponde 4xx            | Log errore, nessun retry (errore client)            |
| n8n risponde 5xx            | Retry (3 tentativi, backoff 2000ms)                 |
| Timeout (30s)               | Retry (3 tentativi, backoff 2000ms)                 |
| Tutti i retry falliti       | Log errore, evento marcato come non consegnato      |

L'errore nella consegna del webhook non influisce sul flusso principale dell'applicazione. L'invio del webhook avviene in modo asincrono e non-blocking.
`;
