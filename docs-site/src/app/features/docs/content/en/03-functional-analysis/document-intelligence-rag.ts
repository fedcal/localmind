export const content = `# Functional Specification: Document Intelligence and RAG Pipeline

| Field        | Value                                     |
|--------------|-------------------------------------------|
| **Document** | Document Intelligence and RAG Pipeline    |
| **Version**  | 0.1.0                                     |
| **Date**     | 2026-02-09                                |
| **Project**  | LocalMind                                 |

---

## Table of Contents

1. [Component Description](#1-component-description)
2. [Document Ingestion](#2-document-ingestion)
3. [RAG Pipeline](#3-rag-pipeline)
4. [Folder Scanning](#4-folder-scanning)
5. [Involved Classes](#5-involved-classes)
6. [Status Workflow](#6-status-workflow)
7. [Flow Diagrams](#7-flow-diagrams)
8. [Configuration](#8-configuration)

---

## 1. Component Description

The Document Intelligence and RAG (Retrieval-Augmented Generation) module is responsible for the entire document lifecycle within LocalMind: from initial ingestion to vector indexing, through semantic search and response generation with source citations.

The module consists of three main sub-systems:

1. **Ingestion**: receiving documents through manual upload or automatic filesystem scanning
2. **Processing**: text extraction, chunking, embedding, and vector storage
3. **Retrieval**: similarity-based semantic search and response generation with document context

---

## 2. Document Ingestion

### 2.1 Ingestion Modes

LocalMind supports two document ingestion modes:

**Manual Upload**:
- Multipart REST API endpoint: \`POST /api/v1/documents/upload\`
- Supported formats: PDF, DOCX, TXT, EML
- Maximum file size: 50 MB (configurable)
- Single or multiple upload

**Automatic Filesystem Indexing**:
- Configuration of local folder paths to monitor
- Optional recursive scanning of subfolders
- Automatic scheduling via Spring Batch (configurable cron)
- Optional filesystem watcher detection for real-time indexing

### 2.2 Supported Formats

| Format  | Extension  | Extraction Library         | Notes                           |
|---------|------------|----------------------------|---------------------------------|
| PDF     | .pdf       | Apache Tika 2.9.2          | Text and metadata               |
| DOCX    | .docx      | Apache Tika 2.9.2          | Text, tables, metadata          |
| TXT     | .txt       | Direct reading             | UTF-8 encoding                  |
| EML     | .eml       | Apache Tika 2.9.2          | Email body, headers, attachments|

### 2.3 Deduplication

Deduplication is performed through SHA-256 hash computation of the file content:

- At the time of ingestion, the SHA-256 hash of the file is computed
- The hash is compared against hashes of documents already present in the database
- If a document with the same hash already exists, ingestion is skipped
- This mechanism prevents duplicate indexing from both manual upload and folder scanning

### 2.4 Validation

Before ingestion, the system performs the following validations:

| Check                 | Action on Failure                    |
|-----------------------|--------------------------------------|
| Supported format      | Rejection with error 400             |
| Size <= 50 MB         | Rejection with error 413             |
| File is readable      | Rejection with error 400             |
| Hash is not duplicate | Skip with informational log          |

---

## 3. RAG Pipeline

The RAG pipeline consists of five sequential phases:

### 3.1 Phase 1: Text Extraction

- **Technology**: Apache Tika 2.9.2
- **Input**: binary file (PDF, DOCX, TXT, EML)
- **Output**: extracted text in plain text format
- **Details**:
  - Full text extraction from the document
  - Removal of formatting, images, and non-textual elements
  - Metadata extraction (title, author, creation date, page count)
  - Encoding handling (UTF-8, ISO-8859-1, etc.)

### 3.2 Phase 2: Chunking

- **Technology**: custom \`ChunkingService\` implementation
- **Input**: extracted text from the document
- **Output**: list of chunks (text segments)
- **Configurable parameters**:
  - \`chunk-size\`: maximum chunk size in characters (default: 500)
  - \`chunk-overlap\`: overlap between consecutive chunks in characters (default: 50)
- **Strategy**: character-based splitting with overlap, respecting sentence boundaries when possible
- **Metadata per chunk**: \`documentId\`, \`chunkIndex\`, \`startOffset\`, \`endOffset\`

### 3.3 Phase 3: Embedding

- **Technology**: Ollama (nomic-embed-text) or cloud provider
- **Input**: chunk text
- **Output**: embedding vector (dimensionality depends on the model, typically 768 or 1536)
- **Default model**: \`nomic-embed-text\` (Ollama, local, free)
- **Alternative cloud models**: \`text-embedding-3-small\` (OpenAI), \`voyage-2\` (Anthropic)
- **Batch processing**: embeddings are generated in batch for efficiency

### 3.4 Phase 4: Vector Storage

- **Technology**: Qdrant vector database
- **Input**: embedding vectors with metadata
- **Output**: indexed and searchable documents
- **Protocol**: gRPC (port 6334) for performance, HTTP REST (port 6333) as fallback
- **Collection**: one Qdrant collection per workspace/tenant
- **Payload**: each point in the vector store includes chunk metadata (documentId, filename, chunkIndex, original text)

### 3.5 Phase 5: Semantic Search and Q&A

- **Input**: user text query
- **Process**:
  1. The query is converted to an embedding vector using the same model used for documents
  2. Cosine similarity search in the Qdrant vector store
  3. Retrieval of the top-K most similar chunks (default K=5)
  4. Construction of the prompt with document context
  5. Response generation through the LLM Gateway
- **Output**: generated response with source citations
- **Source citation**: each response includes references to source documents (\`documentId\`, \`filename\`, \`chunkIndex\`, \`similarityScore\`)

---

## 4. Folder Scanning

### 4.1 Features

Folder scanning allows automatic indexing of documents from local filesystem folders.

### 4.2 Configuration

\`\`\`yaml
localmind:
  documents:
    folders:
      - path: /home/utente/documenti/lavoro
        recursive: true
        enabled: true
      - path: /home/utente/documenti/personali
        recursive: false
        enabled: true
    scan:
      cron: "0 */15 * * * *"   # Every 15 minutes
      watcher-enabled: false    # Filesystem watcher disabled by default
\`\`\`

### 4.3 Operation

- **Multiple paths**: it is possible to configure multiple folders to monitor simultaneously
- **Recursive scanning**: optional, navigates all subfolders
- **Scheduling**: periodic execution via Spring Batch cron expression
- **Filesystem watcher**: real-time detection of new files through Java NIO \`WatchService\` (optional, disabled by default)
- **Incremental indexing**: only new files or files with modified hashes are processed
- **Extension filter**: only files with supported extensions (.pdf, .docx, .txt, .eml) are processed

### 4.4 Scan Flow

1. The Spring Batch job is triggered according to the configured cron
2. The \`FileSystemScannerPort\` scans the configured folders
3. For each file found, the SHA-256 hash is computed
4. The hash is compared with the database: if the file is new or modified, it is added to the ingestion queue
5. Files in the queue are processed by the standard RAG pipeline
6. Upon completion, the document status is updated to INDEXED or FAILED

---

## 5. Involved Classes

### 5.1 Class Architecture

\`\`\`
Domain Layer (localmind-domain)
+-- model/
|   +-- Document              # Entity: document with metadata
|   +-- DocumentChunk         # Value Object: document chunk
|   +-- DocumentStatus (enum) # PENDING, PROCESSING, INDEXED, FAILED, ARCHIVED
|   +-- SearchResult          # Value Object: search result with score
+-- port/
|   +-- in/
|   |   +-- DocumentIngestionUseCase   # Port in: document ingestion
|   |   +-- DocumentSearchUseCase      # Port in: semantic search
|   +-- out/
|       +-- DocumentRepository         # Port out: document persistence
|       +-- VectorStorePort            # Port out: vector store (Qdrant)
|       +-- TextExtractorPort          # Port out: text extraction
|       +-- FileSystemScannerPort      # Port out: filesystem scanning
+-- service/
    +-- DocumentService                # Domain service: document domain logic
    +-- ChunkingService                # Domain service: chunking

Infrastructure Layer (localmind-infrastructure)
+-- document/
|   +-- adapter/
|       +-- TikaTextExtractor          # Adapter: implements TextExtractorPort
|       +-- LocalFileSystemScanner     # Adapter: implements FileSystemScannerPort
+-- persistence/
|   +-- entity/
|   |   +-- DocumentEntity             # JPA entity for documents
|   +-- repository/
|   |   +-- JpaDocumentRepository      # Spring Data JPA repository
|   +-- adapter/
|       +-- DocumentPersistenceAdapter # Adapter: implements DocumentRepository
+-- vectorstore/
    +-- adapter/
        +-- QdrantVectorStoreAdapter   # Adapter: implements VectorStorePort

Batch Layer (localmind-batch)
+-- job/
    +-- DocumentIngestionJobConfig     # Job: batch document ingestion
    +-- FolderScanJobConfig            # Job: folder scanning and ingestion

API Layer (localmind-api)
+-- document/
    +-- controller/
    |   +-- DocumentController         # REST controller: /api/v1/documents
    |   +-- DocumentSearchController   # REST controller: /api/v1/documents/search
    +-- dto/
        +-- DocumentUploadDto          # Upload DTO
        +-- DocumentResponseDto        # Response DTO
        +-- SearchRequestDto           # Search request DTO
        +-- SearchResultDto            # Search result DTO
\`\`\`

### 5.2 Key Interfaces

**DocumentIngestionUseCase** (port in):
\`\`\`java
public interface DocumentIngestionUseCase {
    Document ingest(InputStream content, String filename, String contentType);
    List<Document> ingestFolder(String folderPath, boolean recursive);
}
\`\`\`

**DocumentSearchUseCase** (port in):
\`\`\`java
public interface DocumentSearchUseCase {
    List<SearchResult> search(String query, int topK);
    String questionAnswer(String question, int topK);
}
\`\`\`

**VectorStorePort** (port out):
\`\`\`java
public interface VectorStorePort {
    void store(List<DocumentChunk> chunks);
    List<SearchResult> search(float[] queryEmbedding, int topK);
    void delete(UUID documentId);
}
\`\`\`

**TextExtractorPort** (port out):
\`\`\`java
public interface TextExtractorPort {
    String extract(InputStream content, String contentType);
    Map<String, String> extractMetadata(InputStream content, String contentType);
}
\`\`\`

**FileSystemScannerPort** (port out):
\`\`\`java
public interface FileSystemScannerPort {
    List<Path> scan(String folderPath, boolean recursive, Set<String> extensions);
}
\`\`\`

---

## 6. Status Workflow

Documents in LocalMind follow a defined status workflow:

\`\`\`
                +--- FAILED
                |
PENDING --> PROCESSING --> INDEXED --> ARCHIVED
\`\`\`

| Status      | Description                                             | UI Badge       |
|-------------|---------------------------------------------------------|----------------|
| \`PENDING\`   | Document received, awaiting processing                  | Yellow         |
| \`PROCESSING\`| Document being processed (extract/chunk/embed)          | Blue           |
| \`INDEXED\`   | Document processed and indexed in the vector store      | Green          |
| \`FAILED\`    | Processing failed (extraction or embedding error)       | Red            |
| \`ARCHIVED\`  | Document archived, no longer searchable                 | Gray           |

### Transitions

| From        | To           | Trigger                                     |
|-------------|--------------|---------------------------------------------|
| -           | PENDING      | Upload or detection from folder scan        |
| PENDING     | PROCESSING   | Start of batch job processing               |
| PROCESSING  | INDEXED      | Successful completion of all phases         |
| PROCESSING  | FAILED       | Error in any phase                          |
| INDEXED     | ARCHIVED     | Manual user action                          |
| FAILED      | PENDING      | Manual user retry                           |

---

## 7. Flow Diagrams

### 7.1 Document Ingestion Flow (Upload)

\`\`\`
User         DocumentController   DocumentService    Batch Job     TikaExtractor   ChunkingService   VectorStorePort
  |                |                    |                |               |                |                |
  | POST /upload   |                    |                |               |                |                |
  |--------------->|                    |                |               |                |                |
  |                | ingest(file)       |                |               |                |                |
  |                |------------------->|                |               |                |                |
  |                |                    |                |               |                |                |
  |                |                    | calcHash(file) |               |                |                |
  |                |                    |----+           |               |                |                |
  |                |                    |    |           |               |                |                |
  |                |                    |<---+           |               |                |                |
  |                |                    |                |               |                |                |
  |                |                    | checkDuplicate |               |                |                |
  |                |                    |----+           |               |                |                |
  |                |                    |    |           |               |                |                |
  |                |                    |<---+           |               |                |                |
  |                |                    |                |               |                |                |
  |                |                    | save(PENDING)  |               |                |                |
  |                |                    |----+           |               |                |                |
  |                |                    |    |           |               |                |                |
  |                |                    |<---+           |               |                |                |
  |                |                    |                |               |                |                |
  |                | Document (PENDING) |                |               |                |                |
  |                |<-------------------|                |               |                |                |
  |  202 Accepted  |                    |                |               |                |                |
  |<---------------|                    |                |               |                |                |
  |                |                    |                |               |                |                |
  |                |                    |           [Batch Job Async Execution]           |                |
  |                |                    |                |               |                |                |
  |                |                    |                | update(PROCESSING)             |                |
  |                |                    |                |----+          |                |                |
  |                |                    |                |    |          |                |                |
  |                |                    |                |<---+          |                |                |
  |                |                    |                |               |                |                |
  |                |                    |                | extract(file) |                |                |
  |                |                    |                |-------------->|                |                |
  |                |                    |                |               |                |                |
  |                |                    |                | plainText     |                |                |
  |                |                    |                |<--------------|                |                |
  |                |                    |                |               |                |                |
  |                |                    |                | chunk(text)   |                |                |
  |                |                    |                |------------------------------->|                |
  |                |                    |                |               |                |                |
  |                |                    |                | List<Chunk>   |                |                |
  |                |                    |                |<-------------------------------|                |
  |                |                    |                |               |                |                |
  |                |                    |                | embed + store(chunks)          |                |
  |                |                    |                |------------------------------------------------>|
  |                |                    |                |               |                |                |
  |                |                    |                | success       |                |                |
  |                |                    |                |<------------------------------------------------|
  |                |                    |                |               |                |                |
  |                |                    |                | update(INDEXED)                |                |
  |                |                    |                |----+          |                |                |
  |                |                    |                |    |          |                |                |
  |                |                    |                |<---+          |                |                |
\`\`\`

### 7.2 Semantic Search Flow

\`\`\`
User         SearchController   DocumentSearchUseCase   VectorStorePort   LlmGateway
  |                |                     |                     |               |
  | GET /search    |                     |                     |               |
  | ?query=...     |                     |                     |               |
  |--------------->|                     |                     |               |
  |                | search(query, topK) |                     |               |
  |                |-------------------->|                     |               |
  |                |                     |                     |               |
  |                |                     | embedQuery(query)   |               |
  |                |                     |----+                |               |
  |                |                     |    |                |               |
  |                |                     |<---+                |               |
  |                |                     |                     |               |
  |                |                     | search(embedding,K) |               |
  |                |                     |-------------------->|               |
  |                |                     |                     |               |
  |                |                     |                     | Qdrant search |
  |                |                     |                     |----+          |
  |                |                     |                     |    |          |
  |                |                     |                     |<---+          |
  |                |                     |                     |               |
  |                |                     | List<SearchResult>  |               |
  |                |                     |<--------------------|               |
  |                |                     |                     |               |
  |                | List<SearchResult>  |                     |               |
  |                |<--------------------|                     |               |
  |  JSON results  |                     |                     |               |
  |<---------------|                     |                     |               |
\`\`\`

---

## 8. Configuration

\`\`\`yaml
localmind:
  documents:
    # Upload
    max-file-size: 52428800   # 50 MB in bytes
    supported-formats:
      - application/pdf
      - application/vnd.openxmlformats-officedocument.wordprocessingml.document
      - text/plain
      - message/rfc822

    # Chunking
    chunk-size: 500
    chunk-overlap: 50

    # Embedding
    embedding-model: nomic-embed-text
    embedding-provider: OLLAMA

    # Search
    default-top-k: 5
    similarity-threshold: 0.7

    # Folder scanning
    folders:
      - path: /home/utente/documenti
        recursive: true
        enabled: true
    scan:
      cron: "0 */15 * * * *"
      watcher-enabled: false

  # Qdrant
  vectorstore:
    qdrant:
      host: localhost
      grpc-port: 6334
      http-port: 6333
      collection-name: localmind_documents
\`\`\`
`;
