# Module Boundaries Report / Report Confini Moduli

## Data / Date: 2026-02-21

## Struttura Attuale / Current Structure

### Moduli Maven / Maven Modules

| Modulo / Module | Responsabilita' / Responsibility | Dipendenze Spring / Spring Dependencies |
|-----------------|-----------------------------------|-----------------------------------------|
| localmind-shared-types | Tipi condivisi per futura decomposizione / Shared types for future decomposition | Nessuna (solo Lombok) / None (Lombok only) |
| localmind-domain | Logica di business, modelli, porte / Business logic, models, ports | Nessuna (pure Java) / None (pure Java) |
| localmind-plugin-api | API per plugin esterni / External plugin API | Nessuna / None |
| localmind-infrastructure | Adapter JPA, LLM, vector store / JPA, LLM, vector store adapters | Spring Boot, Spring AI, Spring Data JPA |
| localmind-api | Controller REST, DTO / REST controllers, DTOs | Spring Web |
| localmind-batch | Job Spring Batch / Spring Batch jobs | Spring Batch |
| localmind-app | Entry point, Flyway, profili / Entry point, Flyway, profiles | Spring Boot |

### Domini nel Domain Layer / Domain Layer Domains

| Dominio / Domain | Servizi / Services | Porte In / In Ports | Porte Out / Out Ports |
|------------------|---------------------|----------------------|----------------------|
| llm | LlmGatewayService, ConversationService, ConversationExportService, ProviderConfigService, ModelManagementService, CostTrackingService | ChatUseCase, StreamingChatUseCase, ConversationUseCase, ConversationExportUseCase, ConversationImportUseCase, ProviderConfigUseCase, ModelManagementUseCase | LlmClient, StreamingLlmClient, ConversationRepository, ProviderConfigRepository, LlmUsageRepository, OllamaModelPort, ConversationExportPort, ConversationImportPort |
| document | DocumentService, DocumentIngestionPipelineService, DocumentSearchService, ChunkingService, FolderManagementService, PathValidationService | DocumentIngestionUseCase, DocumentIngestionPipelineUseCase, DocumentSearchUseCase, FolderManagementUseCase | DocumentRepository, DocumentChunkRepository, VectorStorePort, TextExtractorPort, OcrExtractorPort, FileSystemScannerPort, FolderConfigRepository |
| mcp | McpServerManagementService, McpToolOrchestratorService, McpInternalRegistryService, RegexService, SnippetService, HttpClientService, TestGeneratorService, ProjectScaffoldingService, CodeReviewService, DependencyAnalysisService, PerformanceProfilerService, CicdMonitorService, DockerAnalysisService, DbSchemaExplorerService, DataMockGeneratorService, LogAnalyzerService, CodebaseKnowledgeService, ScrumBoardService, AgileMetricsService, TimeTrackingService, ProjectEconomicsService, RetrospectiveService, ApiDocumentationService, StandupService, EnvironmentManagerService, AccessPolicyService, DecisionLogService, WorkflowOrchestratorService, QualityGateService, IncidentManagerService, DashboardService, InsightEngineService | McpServerManagementUseCase, McpToolDiscoveryUseCase, McpToolExecutionUseCase, McpInternalRegistryUseCase, ToolCallingPort, + 20 tool use cases | McpServerRegistrationRepository, McpClientPort, LocalToolDiscoveryPort, + 20 tool repositories |
| auth | LocalAuthService | LocalAuthUseCase | LocalAuthRepository, PasswordHasherPort |
| automation | AutomationService | AutomationUseCase | WebhookRepository, WebhookClientPort |
| common | AnalyticsService, BackupService | AnalyticsUseCase | (usa porte di altri domini / uses ports from other domains) |
| plugin | PluginManagementService | - | PluginRegistryPort |
| agent | - | AgentExecutionUseCase | AgentConfigRepository |
| calendar | CalendarService | - | - |
| email | EmailService | - | - |
| knowledge | KnowledgeGraphService | - | - |
| finetuning | FineTuningService | - | - |
| marketplace | MarketplaceService | - | - |

## Violazioni Cross-Module / Cross-Module Violations

### 1. AnalyticsService (MEDIA / MEDIUM)

**File**: `localmind-domain/src/main/java/com/localmind/domain/common/service/AnalyticsService.java`

**Problema / Issue**: Il servizio `AnalyticsService` nel package `common` importa direttamente classi da `document` e `llm`:
```java
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.port.out.DocumentRepository;
import com.localmind.domain.llm.model.UsageRecord;
import com.localmind.domain.llm.port.out.ConversationRepository;
import com.localmind.domain.llm.port.out.LlmUsageRepository;
```

**Impatto / Impact**: Nella futura decomposizione, `AnalyticsService` non puo' essere isolato in un singolo microservizio senza dipendere da entrambi chat-service e document-service. In a future decomposition, AnalyticsService cannot be isolated into a single microservice without depending on both chat-service and document-service.

**Raccomandazione / Recommendation**: Creare un `AnalyticsDataPort` nel package `common` che astragga i dati necessari, con implementazioni negli adapter infrastructure che delegano ai rispettivi repository. Create an `AnalyticsDataPort` in the `common` package that abstracts the required data, with infrastructure adapter implementations that delegate to respective repositories.

### 2. Valutazione Positiva / Positive Assessment

I seguenti domini rispettano correttamente i confini dei moduli. The following domains correctly respect module boundaries:

- **llm**: Tutti i servizi importano solo da `com.localmind.domain.llm.*` e `com.localmind.domain.common.*`. All services import only from `llm` and `common` packages.
- **document**: Tutti i servizi importano solo da `com.localmind.domain.document.*` e `com.localmind.domain.common.*`. All services import only from `document` and `common` packages.
- **mcp**: Tutti i servizi importano solo da `com.localmind.domain.mcp.*`. All services import only from `mcp` package. Nessuna dipendenza diretta verso llm o document. No direct dependency on llm or document.
- **auth**: Completamente isolato. Fully isolated.
- **automation**: Completamente isolato. Fully isolated.

## Mappatura Futuri Microservizi / Future Microservice Mapping

| Microservizio / Microservice | Domini / Domains | Porta / Port | Dipendenze Infra / Infra Dependencies |
|------------------------------|------------------|--------------|---------------------------------------|
| chat-service | llm, conversation | 8081 | MySQL, Ollama, OpenAI, Anthropic, Google |
| document-service | document, search, folder | 8082 | MySQL, Qdrant, Tika, OCR (opzionale) |
| mcp-service | mcp (server esterni + tool interni) | 8083 | MySQL, server MCP esterni |
| auth-service | auth | 8084 | MySQL |
| settings-service | settings, provider-config | 8085 | MySQL |
| analytics-service | common/analytics | 8086 | MySQL (read-only, dati aggregati) |
| automation-service | automation | 8087 | MySQL, webhook endpoints esterni |

## Database Condiviso vs Separato / Shared vs Separate Database

Attualmente tutti i domini condividono un unico database MySQL `localmind`. Per la decomposizione in microservizi si raccomanda il pattern **Database per Service**:

Currently all domains share a single MySQL database `localmind`. For microservice decomposition the **Database per Service** pattern is recommended:

| Microservizio / Microservice | Tabelle / Tables |
|------------------------------|------------------|
| chat-service | conversations, chat_messages, llm_usage, llm_provider_configs |
| document-service | documents, document_chunks, folder_configs |
| mcp-service | mcp_server_registrations, mcp_* (tool-specific tables) |
| auth-service | local_auth |
| automation-service | webhooks |

## Raccomandazioni / Recommendations

### Prima della Decomposizione / Before Decomposition

1. **Risolvere la violazione AnalyticsService**: Introdurre un `AnalyticsDataPort` nel domain `common` per disaccoppiare i dati analytics dalle implementazioni specifiche. / Resolve the AnalyticsService violation: Introduce an `AnalyticsDataPort` in the `common` domain to decouple analytics data from specific implementations.

2. **Event-driven communication**: Introdurre un sistema di eventi di dominio (es. `DomainEvent`) per la comunicazione asincrona tra domini. Questo facilita la transizione verso messaggistica (Kafka/RabbitMQ) in architettura microservizi. / Introduce a domain event system (e.g., `DomainEvent`) for asynchronous cross-domain communication. This facilitates the transition to messaging (Kafka/RabbitMQ) in microservice architecture.

3. **API Gateway readiness**: La configurazione Nginx preparata in `docker/nginx/nginx.conf` definisce gia' il routing per dominio. In produzione si consiglia Spring Cloud Gateway o Kong. / The Nginx configuration prepared in `docker/nginx/nginx.conf` already defines domain-based routing. For production, Spring Cloud Gateway or Kong is recommended.

4. **Shared types module**: Il modulo `localmind-shared-types` contiene i tipi condivisi (PageRequest, PageResponse, ErrorResponse, ServiceInfo) e i contratti API. Questo modulo sara' la dipendenza comune di tutti i futuri microservizi. / The `localmind-shared-types` module contains shared types (PageRequest, PageResponse, ErrorResponse, ServiceInfo) and API contracts. This module will be the common dependency for all future microservices.

5. **Health check standardization**: Ogni futuro microservizio deve esporre `/actuator/health` con formato consistente. La classe `ServiceInfo` nel modulo shared-types fornisce il modello di risposta. / Each future microservice must expose `/actuator/health` with consistent format. The `ServiceInfo` class in the shared-types module provides the response model.

6. **Configuration externalization**: Le configurazioni specifiche per ambiente (DB, Qdrant, Ollama) sono gia' esternalizzate via `.env`. Per microservizi, migrare a Spring Cloud Config o HashiCorp Vault. / Environment-specific configurations (DB, Qdrant, Ollama) are already externalized via `.env`. For microservices, migrate to Spring Cloud Config or HashiCorp Vault.

### Ordine di Decomposizione Consigliato / Recommended Decomposition Order

1. **MCP Service** (minimo accoppiamento / minimal coupling) - Il dominio MCP e' gia' completamente isolato.
2. **Auth Service** (nessuna dipendenza cross-domain / no cross-domain dependencies)
3. **Document Service** (dipendenze solo infrastrutturali: Qdrant, Tika)
4. **Chat Service** (dipendenze complesse: multi-provider LLM)
5. **Analytics Service** (richiede prima la risoluzione della violazione cross-module)
