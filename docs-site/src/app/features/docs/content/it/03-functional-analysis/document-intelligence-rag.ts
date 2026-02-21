export const content = `# Specifica Funzionale: Document Intelligence e RAG Pipeline

| Campo        | Valore                                    |
|--------------|-------------------------------------------|
| **Documento**| Document Intelligence e RAG Pipeline      |
| **Versione** | 0.1.0                                     |
| **Data**     | 2026-02-09                                |
| **Progetto** | LocalMind                                 |

---

## Indice

1. [Descrizione del Componente](#1-descrizione-del-componente)
2. [Ingestione Documenti](#2-ingestione-documenti)
3. [Pipeline RAG](#3-pipeline-rag)
4. [Folder Scanning](#4-folder-scanning)
5. [Classi Coinvolte](#5-classi-coinvolte)
6. [Status Workflow](#6-status-workflow)
7. [Diagrammi di Flusso](#7-diagrammi-di-flusso)
8. [Configurazione](#8-configurazione)

---

## 1. Descrizione del Componente

Il modulo Document Intelligence e RAG (Retrieval-Augmented Generation) e' responsabile dell'intero ciclo di vita dei documenti all'interno di LocalMind: dall'ingestione iniziale all'indicizzazione vettoriale, fino alla ricerca semantica e alla generazione di risposte con citazione delle fonti.

Il modulo si compone di tre sotto-sistemi principali:

1. **Ingestione**: ricezione dei documenti tramite upload manuale o scansione automatica del filesystem
2. **Processing**: estrazione testo, chunking, embedding e storage vettoriale
3. **Retrieval**: ricerca semantica per similarita' e generazione risposte con contesto documentale

---

## 2. Ingestione Documenti

### 2.1 Modalita' di Ingestione

LocalMind supporta due modalita' di ingestione documenti:

**Upload Manuale**:
- Endpoint REST API multipart: \`POST /api/v1/documents/upload\`
- Formati supportati: PDF, DOCX, TXT, EML
- Dimensione massima file: 50 MB (configurabile)
- Upload singolo o multiplo

**Indicizzazione Automatica da Filesystem**:
- Configurazione di percorsi di cartelle locali da monitorare
- Scansione ricorsiva opzionale delle sottocartelle
- Scheduling automatico tramite Spring Batch (cron configurabile)
- Rilevamento filesystem watcher opzionale per indicizzazione in tempo reale

### 2.2 Formati Supportati

| Formato | Estensione | Libreria di Estrazione | Note                           |
|---------|------------|------------------------|--------------------------------|
| PDF     | .pdf       | Apache Tika 2.9.2      | Testo e metadati               |
| DOCX    | .docx      | Apache Tika 2.9.2      | Testo, tabelle, metadati       |
| TXT     | .txt       | Lettura diretta        | Encoding UTF-8                 |
| EML     | .eml       | Apache Tika 2.9.2      | Corpo email, header, allegati  |

### 2.3 Deduplicazione

La deduplicazione avviene tramite calcolo dell'hash SHA-256 del contenuto del file:

- Al momento dell'ingestione, viene calcolato l'hash SHA-256 del file
- L'hash viene confrontato con gli hash dei documenti gia' presenti nel database
- Se un documento con lo stesso hash esiste gia', l'ingestione viene saltata
- Questo meccanismo previene l'indicizzazione duplicata sia da upload manuale che da folder scanning

### 2.4 Validazione

Prima dell'ingestione, il sistema esegue le seguenti validazioni:

| Controllo             | Azione in caso di fallimento         |
|-----------------------|--------------------------------------|
| Formato supportato    | Rifiuto con errore 400               |
| Dimensione <= 50 MB   | Rifiuto con errore 413               |
| File leggibile        | Rifiuto con errore 400               |
| Hash non duplicato    | Skip con log informativo             |

---

## 3. Pipeline RAG

Il pipeline RAG si compone di cinque fasi sequenziali:

### 3.1 Fase 1: Text Extraction

- **Tecnologia**: Apache Tika 2.9.2
- **Input**: file binario (PDF, DOCX, TXT, EML)
- **Output**: testo estratto in formato plain text
- **Dettagli**:
  - Estrazione del testo completo dal documento
  - Rimozione di formattazione, immagini e elementi non testuali
  - Estrazione dei metadati (titolo, autore, data creazione, numero pagine)
  - Gestione degli encoding (UTF-8, ISO-8859-1, etc.)

### 3.2 Fase 2: Chunking

- **Tecnologia**: implementazione custom \`ChunkingService\`
- **Input**: testo estratto dal documento
- **Output**: lista di chunk (segmenti di testo)
- **Parametri configurabili**:
  - \`chunk-size\`: dimensione massima del chunk in caratteri (default: 500)
  - \`chunk-overlap\`: sovrapposizione tra chunk consecutivi in caratteri (default: 50)
- **Strategia**: suddivisione per caratteri con overlap, rispettando i confini di frase quando possibile
- **Metadati per chunk**: \`documentId\`, \`chunkIndex\`, \`startOffset\`, \`endOffset\`

### 3.3 Fase 3: Embedding

- **Tecnologia**: Ollama (nomic-embed-text) o provider cloud
- **Input**: testo del chunk
- **Output**: vettore embedding (dimensionalita' dipendente dal modello, tipicamente 768 o 1536)
- **Modello predefinito**: \`nomic-embed-text\` (Ollama, locale, gratuito)
- **Modelli cloud alternativi**: \`text-embedding-3-small\` (OpenAI), \`voyage-2\` (Anthropic)
- **Batch processing**: gli embedding vengono generati in batch per efficienza

### 3.4 Fase 4: Vector Storage

- **Tecnologia**: Qdrant vector database
- **Input**: vettori embedding con metadati
- **Output**: documenti indicizzati e ricercabili
- **Protocollo**: gRPC (porta 6334) per performance, HTTP REST (porta 6333) come fallback
- **Collection**: una collection Qdrant per workspace/tenant
- **Payload**: ogni punto nel vector store include i metadati del chunk (documentId, filename, chunkIndex, testo originale)

### 3.5 Fase 5: Semantic Search e Q&A

- **Input**: query testuale dell'utente
- **Processo**:
  1. La query viene convertita in vettore embedding con lo stesso modello usato per i documenti
  2. Ricerca per similarita' coseno nel vector store Qdrant
  3. Recupero dei top-K chunk piu' simili (default K=5)
  4. Costruzione del prompt con contesto documentale
  5. Generazione della risposta tramite LLM Gateway
- **Output**: risposta generata con citazione delle fonti
- **Citazione fonti**: ogni risposta include riferimenti ai documenti sorgente (\`documentId\`, \`filename\`, \`chunkIndex\`, \`similarityScore\`)

---

## 4. Folder Scanning

### 4.1 Funzionalita'

Il folder scanning permette l'indicizzazione automatica di documenti da cartelle del filesystem locale.

### 4.2 Configurazione

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
      cron: "0 */15 * * * *"   # Ogni 15 minuti
      watcher-enabled: false    # Watcher filesystem disabilitato di default
\`\`\`

### 4.3 Funzionamento

- **Path multipli**: e' possibile configurare piu' cartelle da monitorare contemporaneamente
- **Scansione ricorsiva**: opzionale, navigazione di tutte le sottocartelle
- **Scheduling**: esecuzione periodica tramite cron expression di Spring Batch
- **Watcher filesystem**: rilevamento in tempo reale di nuovi file tramite \`WatchService\` di Java NIO (opzionale, disabilitato di default)
- **Indicizzazione incrementale**: solo i file nuovi o con hash modificato vengono processati
- **Filtro estensioni**: vengono processati solo i file con estensioni supportate (.pdf, .docx, .txt, .eml)

### 4.4 Flusso di Scansione

1. Il job Spring Batch si attiva secondo il cron configurato
2. Il \`FileSystemScannerPort\` scansiona le cartelle configurate
3. Per ogni file trovato, calcola l'hash SHA-256
4. Confronta l'hash con il database: se il file e' nuovo o modificato, lo inserisce nella coda di ingestione
5. I file in coda vengono processati dal pipeline RAG standard
6. Al completamento, lo stato del documento viene aggiornato a INDEXED o FAILED

---

## 5. Classi Coinvolte

### 5.1 Architettura delle Classi

\`\`\`
Domain Layer (localmind-domain)
+-- model/
|   +-- Document              # Entity: documento con metadati
|   +-- DocumentChunk         # Value Object: chunk di documento
|   +-- DocumentStatus (enum) # PENDING, PROCESSING, INDEXED, FAILED, ARCHIVED
|   +-- SearchResult          # Value Object: risultato ricerca con score
+-- port/
|   +-- in/
|   |   +-- DocumentIngestionUseCase   # Port in: ingestione documenti
|   |   +-- DocumentSearchUseCase      # Port in: ricerca semantica
|   +-- out/
|       +-- DocumentRepository         # Port out: persistenza documenti
|       +-- VectorStorePort            # Port out: vector store (Qdrant)
|       +-- TextExtractorPort          # Port out: estrazione testo
|       +-- FileSystemScannerPort      # Port out: scansione filesystem
+-- service/
    +-- DocumentService                # Domain service: logica di dominio documenti
    +-- ChunkingService                # Domain service: suddivisione in chunk

Infrastructure Layer (localmind-infrastructure)
+-- document/
|   +-- adapter/
|       +-- TikaTextExtractor          # Adapter: implementa TextExtractorPort
|       +-- LocalFileSystemScanner     # Adapter: implementa FileSystemScannerPort
+-- persistence/
|   +-- entity/
|   |   +-- DocumentEntity             # JPA entity per documents
|   +-- repository/
|   |   +-- JpaDocumentRepository      # Spring Data JPA repository
|   +-- adapter/
|       +-- DocumentPersistenceAdapter # Adapter: implementa DocumentRepository
+-- vectorstore/
    +-- adapter/
        +-- QdrantVectorStoreAdapter   # Adapter: implementa VectorStorePort

Batch Layer (localmind-batch)
+-- job/
    +-- DocumentIngestionJobConfig     # Job: ingestione batch documenti
    +-- FolderScanJobConfig            # Job: scansione cartelle e ingestione

API Layer (localmind-api)
+-- document/
    +-- controller/
    |   +-- DocumentController         # REST controller: /api/v1/documents
    |   +-- DocumentSearchController   # REST controller: /api/v1/documents/search
    +-- dto/
        +-- DocumentUploadDto          # DTO upload
        +-- DocumentResponseDto        # DTO risposta
        +-- SearchRequestDto           # DTO richiesta ricerca
        +-- SearchResultDto            # DTO risultato ricerca
\`\`\`

### 5.2 Interfacce Chiave

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

I documenti in LocalMind seguono un workflow di stato definito:

\`\`\`
                +--- FAILED
                |
PENDING --> PROCESSING --> INDEXED --> ARCHIVED
\`\`\`

| Stato       | Descrizione                                             | Badge UI       |
|-------------|---------------------------------------------------------|----------------|
| \`PENDING\`   | Documento ricevuto, in attesa di elaborazione           | Giallo         |
| \`PROCESSING\`| Documento in fase di elaborazione (extract/chunk/embed) | Blu            |
| \`INDEXED\`   | Documento elaborato e indicizzato nel vector store      | Verde          |
| \`FAILED\`    | Elaborazione fallita (errore estrazione o embedding)    | Rosso          |
| \`ARCHIVED\`  | Documento archiviato, non piu' ricercabile              | Grigio         |

### Transizioni

| Da          | A            | Trigger                                     |
|-------------|--------------|---------------------------------------------|
| -           | PENDING      | Upload o rilevamento da folder scan         |
| PENDING     | PROCESSING   | Inizio elaborazione batch job               |
| PROCESSING  | INDEXED      | Completamento con successo di tutte le fasi |
| PROCESSING  | FAILED       | Errore in una qualsiasi fase                |
| INDEXED     | ARCHIVED     | Azione manuale dell'utente                  |
| FAILED      | PENDING      | Retry manuale dell'utente                   |

---

## 7. Diagrammi di Flusso

### 7.1 Flusso Ingestione Documento (Upload)

\`\`\`
Utente       DocumentController   DocumentService    Batch Job     TikaExtractor   ChunkingService   VectorStorePort
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
  |                |                    |           [Batch Job Esecuzione Asincrona]      |                |
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

### 7.2 Flusso Ricerca Semantica

\`\`\`
Utente       SearchController   DocumentSearchUseCase   VectorStorePort   LlmGateway
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

## 8. Configurazione

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
