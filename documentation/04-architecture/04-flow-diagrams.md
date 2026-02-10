# Key Process Flow Diagrams

| Field        | Value                           |
|--------------|---------------------------------|
| **Document** | Flow Diagrams                   |
| **Version**  | 0.1.0                           |
| **Date**     | 2026-02-09                      |
| **Project**  | LocalMind                       |

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Flow 1: Chat Request](#2-flow-1-chat-request)
3. [Flow 2: Document Ingestion](#3-flow-2-document-ingestion)
4. [Flow 3: Semantic Search](#4-flow-3-semantic-search)
5. [Flow 4: Folder Scan](#5-flow-4-folder-scan)
6. [Flow 5: Automation Trigger](#6-flow-5-automation-trigger)

---

## 1. Introduction

This document illustrates the five main operational flows of LocalMind through detailed sequence diagrams. Each flow describes the interaction between system components from the entry point (user or scheduler) to the completion of the operation.

For each flow, the following are specified:
- The components involved with their respective architectural layer
- The temporal sequence of interactions
- The data exchanged between components
- Decision points and error handling

---

## 2. Flow 1: Chat Request

### 2.1 Description

The Chat Request flow describes the complete path of a message from the user to the LLM-generated response, including provider routing, retry, fallback, and cost tracking.

### 2.2 Components Involved

| Component            | Layer          | Responsibility                           |
|----------------------|----------------|------------------------------------------|
| User                 | External       | Message sending                          |
| ChatController       | API            | HTTP reception, validation, DTO mapping  |
| ChatUseCase          | Domain (port)  | Use case interface                       |
| LlmGatewayService    | Domain         | Routing, fallback, orchestration         |
| LlmClient            | Domain (port)  | LLM provider interface                   |
| OllamaLlmAdapter     | Infrastructure | Communication with Ollama                |
| OpenAiLlmAdapter     | Infrastructure | Communication with OpenAI (fallback)     |
| CostTrackingService  | Domain         | Cost calculation and recording           |
| LlmUsageRepository   | Domain (port)  | Metrics persistence                      |

### 2.3 Sequence Diagram

```
User         ChatController    LlmGatewayService    OllamaLlmAdapter    OpenAiLlmAdapter    CostTrackingService   LlmUsageRepo
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
  |               |                   | [provider from     |                    |                    |                   |
  |               |                   |  request or default]                    |                    |                   |
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
  |               |                   | (with estimatedCost)                    |                    |                   |
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
```

### 2.4 Flow with Fallback (Ollama unavailable)

```
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
       | [retry 1/3 - wait 1000ms]               |                    |
       |------------------->|                    |                    |
       |   TIMEOUT          |                    |                    |
       |<-------------------|                    |                    |
       |                    |                    |                    |
       | [retry 2/3 - wait 2000ms]               |                    |
       |------------------->|                    |                    |
       |   TIMEOUT          |                    |                    |
       |<-------------------|                    |                    |
       |                    |                    |                    |
       | [retry 3/3 - wait 4000ms]               |                    |
       |------------------->|                    |                    |
       |   TIMEOUT          |                    |                    |
       |<-------------------|                    |                    |
       |                    |                    |                    |
       | [max retry - FALLBACK activated]        |                    |
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
```

---

## 3. Flow 2: Document Ingestion

### 3.1 Description

The Document Ingestion flow describes the complete process from uploading a document to its indexing in the Qdrant vector store, going through text extraction, chunking, and embedding.

### 3.2 Components Involved

| Component               | Layer          | Responsibility                           |
|-------------------------|----------------|------------------------------------------|
| User                    | External       | Document upload                          |
| DocumentController      | API            | Multipart reception, validation          |
| DocumentService         | Domain         | Hash, dedup, PENDING save                |
| DocumentRepository      | Domain (port)  | Document metadata persistence            |
| Batch Job               | Batch          | Asynchronous processing orchestration    |
| TikaTextExtractor       | Infrastructure | Text extraction from file                |
| ChunkingService         | Domain         | Splitting into chunks                    |
| QdrantVectorStoreAdapter| Infrastructure | Embedding storage in Qdrant              |

### 3.3 Sequence Diagram

```
User      DocController   DocumentService   DocRepository   BatchJob    TikaExtractor   ChunkingService   QdrantAdapter
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
  |            |               |                |  ======== ASYNCHRONOUS PROCESSING ========   |               |
  |            |               |                |              |              |                |               |
  |            |               |                |  [Batch Job  |              |                |               |
  |            |               |                |   triggered  |              |                |               |
  |            |               |                |   by cron or |              |                |               |
  |            |               |                |   trigger]   |              |                |               |
  |            |               |                |              |              |                |               |
  |            |               |                | findPending()|              |                |               |
  |            |               |                |<-------------|              |                |               |
  |            |               |                | List<Doc>    |              |                |               |
  |            |               |                |------------->|              |                |               |
  |            |               |                |              |              |                |               |
  |            |               |                |              | [For each PENDING document]   |               |
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
  |            |               |                |              | [If error in any phase]       |               |
  |            |               |                | update       |              |                |               |
  |            |               |                | (FAILED,     |              |                |               |
  |            |               |                |  errorMsg)   |              |                |               |
  |            |               |                |<-------------|              |                |               |
```

---

## 4. Flow 3: Semantic Search

### 4.1 Description

The Semantic Search flow describes the process of semantic search within indexed documents, from query embedding to returning results sorted by similarity.

### 4.2 Components Involved

| Component               | Layer          | Responsibility                           |
|-------------------------|----------------|------------------------------------------|
| User                    | External       | Search query submission                  |
| DocumentSearchController| API            | Query reception, validation              |
| DocumentSearchUseCase   | Domain (port)  | Search interface                         |
| DocumentService         | Domain         | Search orchestration                     |
| VectorStorePort         | Domain (port)  | Vector store interface                   |
| QdrantVectorStoreAdapter| Infrastructure | Similarity search in Qdrant              |

### 4.3 Sequence Diagram

```
User      SearchController   DocumentService   QdrantAdapter        Qdrant         OllamaAdapter
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
```

---

## 5. Flow 4: Folder Scan

### 5.1 Description

The Folder Scan flow describes the automatic process of scanning configured folders, detecting new files, and sending them to the RAG pipeline for indexing.

### 5.2 Components Involved

| Component              | Layer          | Responsibility                           |
|------------------------|----------------|------------------------------------------|
| BatchScheduler         | Batch          | Cron trigger for scanning                |
| FolderScanJobConfig    | Batch          | Spring Batch job configuration           |
| FileSystemScannerPort  | Domain (port)  | Filesystem scanning interface            |
| LocalFileSystemScanner | Infrastructure | Recursive filesystem scanning            |
| DocumentService        | Domain         | Hash, dedup, ingestion                   |
| DocumentRepository     | Domain (port)  | Metadata persistence                     |

### 5.3 Sequence Diagram

```
Cron Scheduler    FolderScanJob    FileSystemScanner   DocumentService    DocRepository    RAG Pipeline
      |                |                  |                  |                 |                |
      | trigger        |                  |                  |                 |                |
      | (cron:         |                  |                  |                 |                |
      |  every 15 min) |                  |                  |                 |                |
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
      |                | [For each configured folder]        |                 |                |
      |                |                  |                  |                 |                |
      |                | scan(path,       |                  |                 |                |
      |                |  recursive,      |                  |                 |                |
      |                |  extensions)     |                  |                 |                |
      |                |----------------->|                  |                 |                |
      |                |                  |                  |                 |                |
      |                |                  | Files.walk()     |                 |                |
      |                |                  | or Files.list()  |                 |                |
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
      |                | [For each found file]               |                 |                |
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
      |                |                  |                  |   [If false: new file]           |
      |                |                  |                  |                 |                |
      |                | ingest(file)     |                  |                 |                |
      |                |------------------------------------>|                 |                |
      |                |                  |                  |                 |                |
      |                |                  |                  | save(PENDING)   |                |
      |                |                  |                  |---------------->|                |
      |                |                  |                  |                 |                |
      |                |                  |                  |   [If true: file already indexed]|
      |                |                  |                  |   -> skip with log               |
      |                |                  |                  |                 |                |
      |                | [End file loop]  |                  |                 |                |
      |                |                  |                  |                 |                |
      |                | [PENDING documents are processed                      |                |
      |                |  by DocumentIngestionJob (Flow 2)]                   |                |
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
```

---

## 6. Flow 5: Automation Trigger

### 6.1 Description

The Automation Trigger flow describes the process of generating an internal event in LocalMind and forwarding it to n8n via HTTP webhook for the execution of automated workflows.

### 6.2 Components Involved

| Component              | Layer          | Responsibility                           |
|------------------------|----------------|------------------------------------------|
| Event Source           | Domain         | Component that generates the event       |
| AutomationService      | Domain         | Event management and dispatching         |
| WebhookRepository      | Domain (port)  | Retrieval of registered webhooks         |
| WebhookClientPort      | Domain (port)  | HTTP webhook sending                     |
| N8nWebhookClient       | Infrastructure | HTTP client for n8n                      |
| n8n                    | External       | Automation platform                      |

### 6.3 Sequence Diagram

```
EventSource      AutomationService    WebhookRepository   N8nWebhookClient         n8n           External Services
(DocumentService)|                   |                    |                         |                    |
      |          |                   |                    |                         |                    |
      | [Document indexed            |                    |                         |                    |
      |  successfully]               |                    |                         |                    |
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
      |          | [For each found webhook (enabled)]     |                         |                    |
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
      |          |                   |                    |                         | [n8n workflow       |
      |          |                   |                    |                         |  execution]        |
      |          |                   |                    |                         |                    |
      |          |                   |                    |                         | Action 1:          |
      |          |                   |                    |                         | GET /api/v1/       |
      |          |                   |                    |                         | documents/{id}     |
      |          |                   |                    |                         | -> LocalMind API   |
      |          |                   |                    |                         |                    |
      |          |                   |                    |                         | Action 2:          |
      |          |                   |                    |                         | POST /api/v1/      |
      |          |                   |                    |                         | agents/execute     |
      |          |                   |                    |                         | {type: BUSINESS,   |
      |          |                   |                    |                         |  query: "Generate  |
      |          |                   |                    |                         |  a summary..."}    |
      |          |                   |                    |                         |                    |
      |          |                   |                    |                         | Action 3:          |
      |          |                   |                    |                         | Send email         |
      |          |                   |                    |                         | with summary       |
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
      |          | [If error: retry with backoff]         |                         |                    |
      |          | [Max 3 attempts, backoff 2000ms]       |                         |                    |
      |          |                   |                    |                         |                    |
      |  OK      |                   |                    |                         |                    |
      |<---------|                   |                    |                         |                    |
```

### 6.4 Webhook Error Handling

| Scenario                    | Action                                              |
|-----------------------------|-----------------------------------------------------|
| n8n unreachable             | Retry (3 attempts, backoff 2000ms), then log error  |
| n8n responds 4xx            | Log error, no retry (client error)                  |
| n8n responds 5xx            | Retry (3 attempts, backoff 2000ms)                  |
| Timeout (30s)               | Retry (3 attempts, backoff 2000ms)                  |
| All retries failed          | Log error, event marked as undelivered              |

Webhook delivery errors do not affect the main application flow. Webhook sending occurs asynchronously and non-blocking.
