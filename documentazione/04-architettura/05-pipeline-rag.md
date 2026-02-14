# Pipeline RAG - LocalMind

## Panoramica della Pipeline RAG

La pipeline RAG (Retrieval-Augmented Generation) di LocalMind fornisce ricerca semantica sui documenti e injection di contesto nei prompt LLM per risposte più accurate e contestuali.

### Flusso di Ingestione Documenti

1. **Upload/Scansione**: L'utente carica un documento o una cartella viene scansionata dal batch job
2. **Calcolo SHA-256**: Il file viene hashed per deduplicazione
3. **Deduplicazione**: Se l'hash esiste e il documento è INDEXED, viene saltato
4. **Estrazione Testo**: Tika estrae il testo da PDF, DOCX, TXT, EMail, HTML e altri formati
5. **Chunking**: Il testo viene diviso con sliding window (default: 500 caratteri, overlap 50)
6. **Embedding**: Ogni chunk viene convertito in vettore usando `nomic-embed-text` via Ollama
7. **Storage**: I chunk vengono salvati in MySQL (`document_chunks`) e Qdrant (vector store)

### Flusso di Ricerca e Chat RAG

1. **Query Utente**: L'utente invia un messaggio con flag `enableRag: true`
2. **Embedding Query**: Il messaggio viene convertito in embedding usando lo stesso modello
3. **Similarity Search**: Qdrant ricerca i top-K chunk più simili (default: K=5)
4. **Context Injection**: I chunk trovati vengono aggiunti come messaggio SYSTEM nel prompt LLM
5. **Risposta RAG**: La risposta include metadati dei chunk usati (`ragSources`)

---

## Architettura Hexagonal

### Port In (Use Cases)

| Use Case | Descrizione |
|----------|-------------|
| `DocumentIngestionPipelineUseCase` | Ingestione documento/cartella, orchestrazione pipeline |
| `FolderManagementUseCase` | CRUD cartelle monitorate, trigger sync |
| `DocumentSearchUseCase` | Ricerca semantica e retrieval documenti |

### Port Out (Adattatori)

| Port | Implementazione | Ruolo |
|------|-----------------|-------|
| `DocumentRepository` | JPA Repository | Salva/recupera metadati documenti |
| `DocumentChunkRepository` | JPA Repository | Persiste chunk testuali |
| `VectorStorePort` | QdrantVectorStoreAdapter | Immagazzina/ricerca embedding |
| `TextExtractorPort` | TikaTextExtractor | Estrae testo da formati vari |
| `FileSystemScannerPort` | LocalFileSystemScanner | Scansiona file system |
| `FolderConfigRepository` | JPA Repository | Salva configurazioni cartelle |

### Servizi Domain

```
DocumentIngestionPipelineService
  ├─ ingestFile()        # Carica singolo documento
  └─ ingestFromFolder()  # Ricorsivo da cartella

FolderManagementService
  ├─ addFolder()         # Registra cartella monitorata
  ├─ removeFolder()      # Rimuove cartella
  └─ triggerSync()       # Forza scansione cartella

DocumentSearchService
  └─ search()            # Ricerca top-K chunk rilevanti

ChunkingService
  └─ chunk()             # Divide testo con sliding window

PathValidationService
  └─ validatePath()      # Valida path cross-OS
```

---

## Gestione Cartelle Monitorate

### REST API

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| GET | `/api/v1/folders` | Elenca cartelle monitorate |
| POST | `/api/v1/folders` | Crea nuova cartella monitorata |
| DELETE | `/api/v1/folders/{id}` | Rimuove cartella monitorata |
| POST | `/api/v1/folders/{id}/sync` | Scansiona immediatamente cartella |

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

Il job batch `FolderScanJobConfig` scansiona periodicamente cartelle con `watchEnabled=true`:

- **Trigger**: CRON `0 */15 * * * *` (ogni 15 minuti)
- **Validazione Path**: `PathValidationService` valida percorsi su Windows/Linux/MacOS
- **Deduplicazione**: Skip documenti già INDEXED
- **Stato**: Campo `status` traccia ACTIVE/SUSPENDED

---

## Configurazione

### application-dev.yml

```yaml
localmind:
  batch:
    chunk-size: 500           # Caratteri per chunk
    chunk-overlap: 50         # Overlap tra chunk (sliding window)
    cron-folder-scan: "0 */15 * * * *"
  document:
    supported-types: pdf,docx,txt,eml
    max-file-size: 50MB
```

### Embedding Model

- **Provider**: Ollama (default)
- **Modello**: `nomic-embed-text`
- **URL**: `http://localhost:11434`
- **Dimensioni**: 768-dim embeddings

### Vector Store

- **Provider**: Qdrant
- **Collection**: `localmind-documents`
- **URL**: `http://localhost:6334`
- **Metadata**: documentId, chunkIndex, filename

---

## Integrazione Chat

### ChatController - Flusso RAG

```java
// 1. Abilita RAG con flag request
if (request.isEnableRag()) {
    ragResults = documentSearchUseCase.search(
        request.getMessage(),
        RAG_TOP_K  // = 5 (costante)
    );
}

// 2. Costruisci messaggio SYSTEM con context
List<ChatMessage> allMessages = buildMessageList(
    conversation,
    toolsSystemPrompt,
    ragResults  // Chunk rilevanti
);

// 3. LLM riceve context e genera risposta
response = chatUseCase.chat(llmRequest);

// 4. Ritorna ragSources nel response
response.setRagSources(toRagSourceDtos(ragResults));
```

### ChatResponseDto

```java
{
  "conversationId": "...",
  "content": "Risposta LLM con contesto dai documenti",
  "ragSources": [
    {
      "filename": "documento.pdf",
      "chunkIndex": 0,
      "snippet": "Estratto di 100 caratteri..."
    }
  ]
}
```

### Frontend - Angular Toggle RAG

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

### Tabella document_chunks (Flyway V57-V58)

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

### Tabella folder_configs (Flyway V59-V60)

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
  "filename": "documento.pdf"
}
```

---

## Stato Documento

| Status | Descrizione |
|--------|-------------|
| PENDING | In coda per ingestione |
| PROCESSING | In corso di elaborazione |
| INDEXED | Completato e disponibile per ricerca |
| FAILED | Errore durante ingestione |

---

## Best Practices

1. **Chunk Size**: 500 caratteri equilibra granularità e contesto
2. **Overlap**: 50 caratteri evita loss di informazione tra boundary
3. **Top-K**: 5 risultati balanciano precisione e lunghezza prompt
4. **Deduplicazione**: Sempre controllare SHA-256 prima di re-ingestire
5. **Cartelle**: Abilitare batch job solo su cartelle stabili (non temporanee)
6. **Formato**: Supportati: PDF, DOCX, TXT, EMail (via Tika)

