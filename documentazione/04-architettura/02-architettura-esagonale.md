# Architettura Esagonale

| Campo        | Valore                          |
|--------------|---------------------------------|
| **Documento**| Architettura Esagonale          |
| **Versione** | 1.0.0                           |
| **Data**     | 2026-02-09                      |
| **Progetto** | LocalMind                       |

---

## Indice

1. [Principi Fondamentali](#1-principi-fondamentali)
2. [I Tre Layer Concentrici](#2-i-tre-layer-concentrici)
3. [Struttura dei Package](#3-struttura-dei-package)
4. [Regola delle Dipendenze](#4-regola-delle-dipendenze)
5. [Vantaggi dell'Architettura](#5-vantaggi-dellarchitettura)
6. [Esempio Pratico: Modulo LLM](#6-esempio-pratico-modulo-llm)
7. [Esempio Pratico: Modulo Document](#7-esempio-pratico-modulo-document)

---

## 1. Principi Fondamentali

L'architettura esagonale (nota anche come Ports & Adapters, ideata da Alistair Cockburn) si fonda su tre principi cardine che LocalMind implementa rigorosamente:

### 1.1 Separazione del Dominio dal Framework

Il codice di dominio (entita', value objects, servizi di dominio, regole di business) non contiene alcuna dipendenza da framework o librerie infrastrutturali. Il modulo `localmind-domain` compila e funziona senza Spring Boot, JPA, Spring AI o qualsiasi altra libreria esterna (l'unica eccezione e' Lombok per riduzione del boilerplate).

### 1.2 Inversione delle Dipendenze

Il dominio definisce le interfacce (porte) che l'infrastruttura deve implementare, e non viceversa. Il dominio non dipende dall'infrastruttura: e' l'infrastruttura che dipende dal dominio.

```
ERRATO:  Domain --> Infrastructure (dominio conosce JPA, Spring AI, etc.)
CORRETTO: Infrastructure --> Domain  (infrastruttura implementa le porte del dominio)
```

### 1.3 Testabilita'

Grazie alla separazione, i servizi di dominio possono essere testati unitariamente con mock delle porte, senza necessita' di avviare il contesto Spring, il database o servizi esterni. Questo riduce drasticamente i tempi di esecuzione dei test e incrementa l'affidabilita' della suite.

---

## 2. I Tre Layer Concentrici

L'architettura si organizza in tre layer concentrici, dal centro (piu' stabile) verso l'esterno (piu' volatile):

```
+---------------------------------------------------------------------+
|                                                                     |
|   INFRASTRUCTURE LAYER (Adapters)                                   |
|   - REST Controllers                                                |
|   - JPA Repositories                                                |
|   - Spring AI Clients                                               |
|   - Apache Tika Adapters                                            |
|   - Spring Batch Jobs                                               |
|   - HTTP Clients (n8n, Qdrant)                                      |
|                                                                     |
|   +--------------------------------------------------------------+  |
|   |                                                              |  |
|   |   APPLICATION LAYER (Ports)                                  |  |
|   |   - Use Case interfaces (ports in)                           |  |
|   |   - Repository interfaces (ports out)                        |  |
|   |   - Client interfaces (ports out)                            |  |
|   |                                                              |  |
|   |   +------------------------------------------------------+   |  |
|   |   |                                                      |   |  |
|   |   |   DOMAIN LAYER (Core)                                |   |  |
|   |   |   - Entities (Document, Agent, Webhook, etc.)        |   |  |
|   |   |   - Value Objects (ChatRequest, ChatResponse, etc.)  |   |  |
|   |   |   - Domain Services (LlmGatewayService, etc.)        |   |  |
|   |   |   - Enums (LlmProvider, DocumentStatus, etc.)        |   |  |
|   |   |   - Domain Exceptions                                |   |  |
|   |   |   - ZERO dipendenze framework                        |   |  |
|   |   |                                                      |   |  |
|   |   +------------------------------------------------------+   |  |
|   |                                                              |  |
|   +--------------------------------------------------------------+  |
|                                                                     |
+---------------------------------------------------------------------+
```

### 2.1 Domain Layer (Centro)

Il cuore dell'applicazione. Contiene la logica di business pura:

- **Entities**: oggetti con identita' persistente (Document, Agent, Webhook, LlmUsage)
- **Value Objects**: oggetti immutabili senza identita' (ChatRequest, ChatResponse, SearchResult, DocumentChunk)
- **Domain Services**: servizi che implementano logica di business complessa che non appartiene a una singola entita' (LlmGatewayService, DocumentService, ChunkingService, CostTrackingService, AutomationService, AgentService, StreamingChatService)
- **Enums**: enumerazioni di dominio (LlmProvider, DocumentStatus, AgentType, AutomationEvent)
- **Exceptions**: eccezioni specifiche del dominio (ProviderUnavailableException, DocumentProcessingException)

**Dipendenze**: ZERO dipendenze framework. Solo Lombok (compile-only).

### 2.2 Application Layer (Porte)

Definisce i contratti tra dominio e mondo esterno:

- **Ports In (Use Case)**: interfacce che il mondo esterno (controllers) invoca per interagire con il dominio
  - `ChatUseCase`: invio messaggi chat
  - `StreamingChatUseCase`: streaming della chat con callback per token
  - `DocumentIngestionUseCase`: ingestione documenti
  - `DocumentSearchUseCase`: ricerca semantica
  - `AgentExecutionUseCase`: esecuzione agenti
  - `AutomationUseCase`: CRUD webhook, trigger eventi e test webhook

- **Ports Out (SPI)**: interfacce che il dominio invoca per accedere a risorse esterne
  - `LlmClient`: comunicazione con provider LLM
  - `StreamingLlmClient`: streaming LLM, ritorna `Flux<String>` per i token
  - `DocumentRepository`: persistenza documenti
  - `VectorStorePort`: vector store per embedding
  - `TextExtractorPort`: estrazione testo da file
  - `FileSystemScannerPort`: scansione filesystem
  - `LlmUsageRepository`: persistenza metriche utilizzo
  - `AgentConfigRepository`: persistenza configurazione agenti
  - `WebhookRepository`: persistenza webhook (save, findById, findAll, findByEventType, deleteById)
  - `WebhookClientPort`: invocazione HTTP verso webhook esterni

### 2.3 Infrastructure Layer (Adapter)

Implementazioni concrete delle porte:

- **Adapter In (Driving)**: controller REST che invocano i use case
  - `ChatController` -> `ChatUseCase`
  - `StreamingChatController` -> `StreamingChatUseCase` (`POST /api/v1/chat/stream` con `SseEmitter`)
  - `DocumentController` -> `DocumentIngestionUseCase`
  - `DocumentSearchController` -> `DocumentSearchUseCase`
  - `AgentController` -> `AgentExecutionUseCase`
  - `AutomationController` -> `AutomationUseCase`
  - `WebhookController` -> `AutomationUseCase` (CRUD REST su `/api/v1/webhooks`)

- **Adapter Out (Driven)**: implementazioni che il dominio utilizza tramite le porte
  - `OllamaLlmAdapter` -> `LlmClient`
  - `OpenAiLlmAdapter` -> `LlmClient`
  - `AnthropicLlmAdapter` -> `LlmClient`
  - `OllamaStreamingLlmAdapter` -> `StreamingLlmClient` (streaming via Spring AI `StreamingChatModel` per Ollama)
  - `OpenAiStreamingLlmAdapter` -> `StreamingLlmClient` (streaming per OpenAI)
  - `AnthropicStreamingLlmAdapter` -> `StreamingLlmClient` (streaming per Anthropic)
  - `DocumentPersistenceAdapter` -> `DocumentRepository`
  - `QdrantVectorStoreAdapter` -> `VectorStorePort`
  - `TikaTextExtractor` -> `TextExtractorPort`
  - `LocalFileSystemScanner` -> `FileSystemScannerPort`
  - `WebhookRepositoryAdapter` -> `WebhookRepository` (mapping domain Webhook <-> JPA WebhookEntity)
  - `N8nWebhookClient` -> `WebhookClientPort` (implementazione con Spring WebClient)

---

## 3. Struttura dei Package

### 3.1 Modulo Domain

```
com.localmind.domain
+-- llm/
|   +-- model/
|   |   +-- ChatRequest.java          # Value Object
|   |   +-- ChatResponse.java         # Value Object
|   |   +-- LlmProvider.java          # Enum
|   |   +-- LlmUsage.java             # Entity
|   +-- port/
|   |   +-- in/
|   |   |   +-- ChatUseCase.java       # Port In
|   |   |   +-- StreamingChatUseCase.java # Port In
|   |   +-- out/
|   |       +-- LlmClient.java         # Port Out
|   |       +-- StreamingLlmClient.java # Port Out (Flux<String>)
|   |       +-- LlmUsageRepository.java # Port Out
|   +-- service/
|       +-- LlmGatewayService.java     # Domain Service (implements ChatUseCase)
|       +-- StreamingChatService.java  # Domain Service (implements StreamingChatUseCase)
|       +-- CostTrackingService.java   # Domain Service
+-- document/
|   +-- model/
|   |   +-- Document.java             # Entity
|   |   +-- DocumentChunk.java        # Value Object
|   |   +-- DocumentStatus.java       # Enum
|   |   +-- SearchResult.java         # Value Object
|   +-- port/
|   |   +-- in/
|   |   |   +-- DocumentIngestionUseCase.java  # Port In
|   |   |   +-- DocumentSearchUseCase.java     # Port In
|   |   +-- out/
|   |       +-- DocumentRepository.java         # Port Out
|   |       +-- VectorStorePort.java            # Port Out
|   |       +-- TextExtractorPort.java          # Port Out
|   |       +-- FileSystemScannerPort.java      # Port Out
|   +-- service/
|       +-- DocumentService.java       # Domain Service
|       +-- ChunkingService.java       # Domain Service
+-- agent/
|   +-- model/
|   |   +-- Agent.java                 # Entity
|   |   +-- AgentType.java            # Enum
|   |   +-- AgentTool.java            # Value Object
|   |   +-- AgentExecutionResult.java  # Value Object
|   |   +-- Citation.java             # Value Object
|   +-- port/
|   |   +-- in/
|   |   |   +-- AgentExecutionUseCase.java  # Port In
|   |   +-- out/
|   |       +-- AgentConfigRepository.java  # Port Out
|   +-- service/
|       +-- AgentService.java          # Domain Service
+-- automation/
    +-- model/
    |   +-- Webhook.java               # Entity
    |   +-- AutomationEvent.java       # Enum
    |   +-- WebhookPayload.java        # Value Object
    +-- port/
    |   +-- in/
    |   |   +-- AutomationUseCase.java  # Port In
    |   +-- out/
    |       +-- WebhookRepository.java   # Port Out
    |       +-- WebhookClientPort.java   # Port Out
    +-- service/
        +-- AutomationService.java      # Domain Service
```

### 3.2 Modulo Infrastructure

```
com.localmind.infrastructure
+-- llm/
|   +-- adapter/
|       +-- OllamaLlmAdapter.java          # Implements LlmClient
|       +-- OpenAiLlmAdapter.java          # Implements LlmClient
|       +-- AnthropicLlmAdapter.java       # Implements LlmClient
|       +-- GoogleLlmAdapter.java          # Implements LlmClient
|       +-- OllamaStreamingLlmAdapter.java    # Implements StreamingLlmClient
|       +-- OpenAiStreamingLlmAdapter.java    # Implements StreamingLlmClient
|       +-- AnthropicStreamingLlmAdapter.java # Implements StreamingLlmClient
+-- persistence/
|   +-- entity/
|   |   +-- DocumentEntity.java             # JPA Entity
|   |   +-- LlmUsageEntity.java            # JPA Entity
|   |   +-- AgentEntity.java               # JPA Entity
|   |   +-- WebhookEntity.java             # JPA Entity
|   +-- repository/
|   |   +-- JpaDocumentRepository.java      # Spring Data JPA
|   |   +-- JpaLlmUsageRepository.java     # Spring Data JPA
|   |   +-- JpaAgentRepository.java        # Spring Data JPA
|   |   +-- JpaWebhookRepository.java      # Spring Data JPA
|   +-- adapter/
|       +-- DocumentPersistenceAdapter.java # Implements DocumentRepository
|       +-- LlmUsagePersistenceAdapter.java # Implements LlmUsageRepository
|       +-- AgentConfigPersistenceAdapter.java # Implements AgentConfigRepository
|       +-- WebhookRepositoryAdapter.java   # Implements WebhookRepository
+-- document/
|   +-- adapter/
|       +-- TikaTextExtractor.java          # Implements TextExtractorPort
|       +-- LocalFileSystemScanner.java     # Implements FileSystemScannerPort
+-- vectorstore/
|   +-- adapter/
|       +-- QdrantVectorStoreAdapter.java   # Implements VectorStorePort
+-- automation/
    +-- adapter/
        +-- N8nWebhookClient.java           # Implements WebhookClientPort
```

### 3.3 Modulo API

```
com.localmind.api
+-- llm/
|   +-- controller/
|   |   +-- ChatController.java             # REST Controller
|   |   +-- StreamingChatController.java    # REST Controller (SSE)
|   +-- dto/
|       +-- ChatRequestDto.java             # DTO
|       +-- ChatResponseDto.java            # DTO
+-- document/
|   +-- controller/
|   |   +-- DocumentController.java         # REST Controller
|   |   +-- DocumentSearchController.java   # REST Controller
|   +-- dto/
|       +-- DocumentUploadDto.java          # DTO
|       +-- DocumentResponseDto.java        # DTO
|       +-- SearchRequestDto.java           # DTO
|       +-- SearchResultDto.java            # DTO
+-- agent/
|   +-- controller/
|   |   +-- AgentController.java            # REST Controller
|   +-- dto/
|       +-- AgentExecutionRequestDto.java   # DTO
|       +-- AgentExecutionResponseDto.java  # DTO
+-- automation/
|   +-- controller/
|   |   +-- AutomationController.java       # REST Controller
|   |   +-- WebhookController.java          # REST Controller (CRUD /api/v1/webhooks)
|   +-- dto/
|       +-- WebhookDto.java                 # DTO
|       +-- WebhookCreateDto.java           # DTO
+-- dashboard/
    +-- controller/
    |   +-- DashboardController.java        # REST Controller
    +-- dto/
        +-- HealthStatusDto.java            # DTO
```

---

## 4. Regola delle Dipendenze

La regola fondamentale dell'architettura esagonale e':

> **Le dipendenze puntano solo verso il centro, mai verso l'esterno.**

```
Infrastructure --depends on--> Domain    (CORRETTO)
API            --depends on--> Domain    (CORRETTO)
Batch          --depends on--> Domain    (CORRETTO)

Domain         --depends on--> Infrastructure  (VIETATO)
Domain         --depends on--> API             (VIETATO)
Domain         --depends on--> Spring Boot     (VIETATO)
Domain         --depends on--> JPA             (VIETATO)
```

### 4.1 Verifica della Regola

La regola viene garantita strutturalmente dai moduli Maven:

- `localmind-domain`: il POM non dichiara dipendenze verso Spring, JPA o altri framework
- `localmind-infrastructure`: il POM dichiara dipendenza verso `localmind-domain`
- `localmind-api`: il POM dichiara dipendenza verso `localmind-domain`
- `localmind-batch`: il POM dichiara dipendenza verso `localmind-domain` e `localmind-infrastructure`

Questa separazione a livello di modulo Maven rende **impossibile a tempo di compilazione** l'introduzione di dipendenze inverse.

### 4.2 Direzione del Flusso

```
Controller (API) --> Use Case (Port In) --> Domain Service --> Port Out --> Adapter (Infrastructure)

ChatController    --> ChatUseCase        --> LlmGatewayService --> LlmClient --> OllamaLlmAdapter
```

Il controller conosce solo l'interfaccia Use Case. Il domain service conosce solo l'interfaccia Port Out. L'adapter implementa l'interfaccia Port Out. Nessun componente conosce le implementazioni concrete degli altri.

---

## 5. Vantaggi dell'Architettura

### 5.1 Il Dominio Compila senza Spring Boot

```bash
cd localmind-domain
mvn compile
# SUCCESSO: nessuna dipendenza Spring necessaria
```

Questo significa che la logica di business e' completamente indipendente dal framework applicativo.

### 5.2 Gli Adapter sono Sostituibili

Per passare da Ollama a un provider LLM completamente diverso, e' sufficiente:

1. Creare un nuovo adapter che implementi `LlmClient`
2. Registrarlo come bean Spring
3. Il dominio continua a funzionare senza alcuna modifica

Lo stesso principio si applica a: database (JPA -> MongoDB), vector store (Qdrant -> Pinecone), text extractor (Tika -> custom), etc.

### 5.3 I Test Unitari non Necessitano di Contesto Spring

```java
// Test del domain service con mock delle porte
@Test
void shouldRouteToDefaultProvider() {
    // Mock della porta out
    LlmClient mockClient = mock(LlmClient.class);
    when(mockClient.getProvider()).thenReturn(LlmProvider.OLLAMA);
    when(mockClient.isAvailable()).thenReturn(true);
    when(mockClient.generate(any())).thenReturn(expectedResponse);

    // Creazione del service con mock (NO Spring context)
    LlmGatewayService service = new LlmGatewayService(List.of(mockClient));

    // Esecuzione
    ChatResponse response = service.chat(request);

    // Verifica
    assertThat(response).isEqualTo(expectedResponse);
}
```

### 5.4 Manutenibilita' a Lungo Termine

La separazione netta tra layer consente:

- Modifica dell'infrastruttura senza toccare il dominio
- Aggiunta di nuove funzionalita' con impatto circoscritto
- Refactoring sicuro con garanzia che i test unitari del dominio continuano a passare
- Onboarding rapido di nuovi sviluppatori grazie alla struttura predicibile

---

## 6. Esempio Pratico: Modulo LLM

Flusso completo di una richiesta chat attraverso i tre layer:

```
1. ChatController (API layer)
   - Riceve POST /api/v1/chat con ChatRequestDto
   - Mappa ChatRequestDto -> ChatRequest (domain model)
   - Invoca chatUseCase.chat(request)

2. LlmGatewayService (Domain layer, implements ChatUseCase)
   - Seleziona il provider tramite logica di dominio
   - Invoca llmClient.generate(request) tramite la porta out
   - Se errore, applica retry e fallback (logica di dominio)
   - Invoca costTrackingService.track(usage)
   - Restituisce ChatResponse

3. OllamaLlmAdapter (Infrastructure layer, implements LlmClient)
   - Riceve la richiesta dal domain service tramite la porta
   - Utilizza Spring AI OllamaChatModel per comunicare con Ollama
   - Costruisce la risposta e la restituisce come ChatResponse (domain model)
```

---

## 7. Esempio Pratico: Modulo Document

Flusso completo di ingestione documento:

```
1. DocumentController (API layer)
   - Riceve POST /api/v1/documents/upload con file multipart
   - Invoca documentIngestionUseCase.ingest(content, filename, contentType)

2. DocumentService (Domain layer, implements DocumentIngestionUseCase)
   - Calcola hash SHA-256 del file (logica di dominio)
   - Verifica deduplicazione tramite documentRepository.existsByHash(hash)
   - Crea entita' Document con stato PENDING
   - Salva tramite documentRepository.save(document)

3. DocumentIngestionJobConfig (Batch layer)
   - Job Spring Batch processa documenti PENDING
   - Per ogni documento:
     a. textExtractorPort.extract(content) -> TikaTextExtractor
     b. chunkingService.chunk(text) -> ChunkingService (domain)
     c. vectorStorePort.store(chunks) -> QdrantVectorStoreAdapter
     d. documentRepository.updateStatus(INDEXED)
```
