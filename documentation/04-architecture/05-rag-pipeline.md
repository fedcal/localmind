# RAG Pipeline - LocalMind

## RAG Pipeline Overview

The RAG (Retrieval-Augmented Generation) pipeline in LocalMind provides semantic search over documents and context injection into LLM prompts for more accurate and contextual responses.

### Document Ingestion Flow

1. **Upload/Scan**: User uploads a document or a batch job scans a monitored folder
2. **SHA-256 Hash**: File is hashed for deduplication detection
3. **Deduplication**: If hash exists and document is INDEXED, skip processing
4. **Text Extraction**: Tika extracts text from PDF, DOCX, TXT, Email, HTML and other formats
5. **Chunking**: Text is split with sliding window (default: 500 characters, overlap 50)
6. **Embedding**: Each chunk is converted to a vector using `nomic-embed-text` via Ollama
7. **Storage**: Chunks are saved to MySQL (`document_chunks`) and Qdrant (vector store)

### Search and RAG Chat Flow

1. **User Query**: User sends a message with flag `enableRag: true`
2. **Query Embedding**: Message is converted to embedding using the same model
3. **Similarity Search**: Qdrant searches top-K most similar chunks (default: K=5)
4. **Context Injection**: Found chunks are added as SYSTEM message in LLM prompt
5. **RAG Response**: Response includes metadata of chunks used (`ragSources`)

---

## Hexagonal Architecture

### Ports In (Use Cases)

| Use Case | Description |
|----------|-------------|
| `DocumentIngestionPipelineUseCase` | Document/folder ingestion, pipeline orchestration |
| `FolderManagementUseCase` | Monitored folder CRUD, trigger sync |
| `DocumentSearchUseCase` | Semantic search and document retrieval |

### Ports Out (Adapters)

| Port | Implementation | Role |
|------|-----------------|------|
| `DocumentRepository` | JPA Repository | Save/retrieve document metadata |
| `DocumentChunkRepository` | JPA Repository | Persist text chunks |
| `VectorStorePort` | QdrantVectorStoreAdapter | Store/search embeddings |
| `TextExtractorPort` | TikaTextExtractor | Extract text from various formats |
| `FileSystemScannerPort` | LocalFileSystemScanner | Scan file system |
| `FolderConfigRepository` | JPA Repository | Save folder configurations |

### Domain Services

```
DocumentIngestionPipelineService
  ├─ ingestFile()        # Upload single document
  └─ ingestFromFolder()  # Recursive from folder

FolderManagementService
  ├─ addFolder()         # Register monitored folder
  ├─ removeFolder()      # Remove folder
  └─ triggerSync()       # Force folder scan

DocumentSearchService
  └─ search()            # Search top-K relevant chunks

ChunkingService
  └─ chunk()             # Split text with sliding window

PathValidationService
  └─ validatePath()      # Validate path cross-OS
```

---

## Monitored Folder Management

### REST API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/folders` | List monitored folders |
| POST | `/api/v1/folders` | Create new monitored folder |
| DELETE | `/api/v1/folders/{id}` | Remove monitored folder |
| POST | `/api/v1/folders/{id}/sync` | Scan folder immediately |

### Request/Response

```json
POST /api/v1/folders
{
  "path": "/home/user/documents",
  "recursive": true,
  "watchEnabled": true
}

Response 200:
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "path": "/home/user/documents",
  "recursive": true,
  "watchEnabled": true,
  "status": "ACTIVE",
  "documentCount": 12
}
```

### Batch Job - FolderScanJobConfig

The batch job `FolderScanJobConfig` periodically scans folders with `watchEnabled=true`:

- **Trigger**: CRON `0 */15 * * * *` (every 15 minutes)
- **Path Validation**: `PathValidationService` validates paths on Windows/Linux/MacOS
- **Deduplication**: Skip documents already INDEXED
- **Status**: Field `status` tracks ACTIVE/SUSPENDED

---

## Configuration

### application-dev.yml

```yaml
localmind:
  batch:
    chunk-size: 500           # Characters per chunk
    chunk-overlap: 50         # Overlap between chunks (sliding window)
    cron-folder-scan: "0 */15 * * * *"
  document:
    supported-types: pdf,docx,txt,eml
    max-file-size: 50MB
```

### Embedding Model

- **Provider**: Ollama (default)
- **Model**: `nomic-embed-text`
- **URL**: `http://localhost:11434`
- **Dimensions**: 768-dim embeddings

### Vector Store

- **Provider**: Qdrant
- **Collection**: `localmind-documents`
- **URL**: `http://localhost:6334`
- **Metadata**: documentId, chunkIndex, filename

---

## Chat Integration

### ChatController - RAG Flow

```java
// 1. Enable RAG with request flag
if (request.isEnableRag()) {
    ragResults = documentSearchUseCase.search(
        request.getMessage(),
        RAG_TOP_K  // = 5 (constant)
    );
}

// 2. Build SYSTEM message with context
List<ChatMessage> allMessages = buildMessageList(
    conversation,
    toolsSystemPrompt,
    ragResults  // Relevant chunks
);

// 3. LLM receives context and generates response
response = chatUseCase.chat(llmRequest);

// 4. Return ragSources in response
response.setRagSources(toRagSourceDtos(ragResults));
```

### ChatResponseDto

```java
{
  "conversationId": "...",
  "content": "LLM response with context from documents",
  "ragSources": [
    {
      "filename": "document.pdf",
      "chunkIndex": 0,
      "snippet": "Extract of 100 characters..."
    }
  ]
}
```

### Frontend - Angular RAG Toggle

```typescript
// chat.store.ts - Signal-based state
export const chatStore = signalStore({
  providedIn: 'root',
  state: () => ({
    enableRag: true,   // Toggle RAG
    ragSources: []
  })
});

// chat-page.component.ts
sendMessage(text: string) {
  this.chatService.chat({
    message: text,
    enableRag: this.store.enableRag(),
    conversationId: this.conversationId
  });
}
```

---

## Database

### Table document_chunks (Flyway V57-V58)

```sql
CREATE TABLE document_chunks (
  id CHAR(36) PRIMARY KEY,
  document_id CHAR(36) NOT NULL,
  content LONGTEXT NOT NULL,
  chunk_index INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
  KEY idx_doc_id (document_id),
  KEY idx_chunk_idx (chunk_index)
);
```

### Table folder_configs (Flyway V59-V60)

```sql
ALTER TABLE folder_configs ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
ALTER TABLE folder_configs ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
```

### Vector Store - Qdrant

Collection `localmind-documents`:

```json
{
  "document_id": "f47ac10b-...",
  "chunk_index": 0,
  "filename": "document.pdf"
}
```

---

## Document Status

| Status | Description |
|--------|-------------|
| PENDING | Queued for ingestion |
| PROCESSING | Currently being processed |
| INDEXED | Complete and available for search |
| FAILED | Error during ingestion |

---

## Best Practices

1. **Chunk Size**: 500 characters balance granularity and context
2. **Overlap**: 50 characters prevent information loss at boundaries
3. **Top-K**: 5 results balance precision and prompt length
4. **Deduplication**: Always check SHA-256 before re-ingesting
5. **Folders**: Enable batch job only on stable folders (not temporary)
6. **Formats**: Supported: PDF, DOCX, TXT, Email (via Tika)

