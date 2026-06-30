# Test Unitari Backend (JUnit 5 + Mockito)

## Panoramica

Il backend Spring Boot include una suite completa di test unitari basati su **JUnit 5**, **Mockito 5.14** e **AssertJ 3.26**. Tutti i test sono puri unit test con mock delle dipendenze, non richiedono servizi esterni (MySQL, Qdrant, Ollama).

## Dipendenze

`spring-boot-starter-test` nel parent POM fornisce tutte le dipendenze necessarie (JUnit Jupiter, Mockito, AssertJ, Spring MockMvc).

## Esecuzione

```bash
cd localmind-backend

# Tutti i test di tutti i moduli
mvn test

# Solo test dominio
mvn -pl localmind-domain test

# Solo test controller
mvn -pl localmind-api test

# Solo test adapter infrastrutturali
mvn -pl localmind-infrastructure test

# Solo test batch
mvn -pl localmind-batch test
```

## Struttura

```
localmind-backend/
  localmind-domain/src/test/java/com/localmind/domain/
    document/service/
      ChunkingServiceTest.java                # 6 test - logica chunking pura
      DocumentServiceTest.java                # 5 test - CRUD, cascade delete
      DocumentSearchServiceTest.java          # 3 test - delega a VectorStorePort
      DocumentIngestionPipelineServiceTest.java # 13 test - pipeline ingest, dedup, extract, chunk
      FolderManagementServiceTest.java          # 8 test - CRUD cartelle, triggerSync
      PathValidationServiceTest.java            # 16 test - validazione path cross-OS
    llm/service/
      LlmGatewayServiceTest.java              # 8 test - fallback chain, usage tracking
      ModelManagementServiceTest.java         # 5 test - lista modelli, getModel
      ProviderConfigServiceTest.java          # 15 test - CRUD provider, testConnection
      CostTrackingServiceTest.java            # 4 test - aggregazione costi
      ConversationServiceTest.java            # 37 test - CRUD conversazioni, messaggi
    mcp/service/
      McpServerManagementServiceTest.java     # 10 test - register, reconnect
      McpToolOrchestratorServiceTest.java     # 4 test - discovery, execution
      RegexServiceTest.java                   # 18 test - regex match, replace, validate
      SnippetServiceTest.java                 # 12 test - CRUD snippet di codice
      HttpClientServiceTest.java              # 14 test - GET/POST/PUT/DELETE, headers
      ProjectScaffoldingServiceTest.java      # 19 test - scaffolding progetto
      TestGeneratorServiceTest.java           # 34 test - generazione test unitari
      CodeReviewServiceTest.java              # 39 test - analisi codice, suggerimenti
      DependencyAnalysisServiceTest.java      # 34 test - analisi dipendenze
      PerformanceProfilerServiceTest.java     # 32 test - profiling prestazioni
      CicdMonitorServiceTest.java             # 14 test - monitoraggio CI/CD
      DockerAnalysisServiceTest.java          # 36 test - analisi Docker Compose
      LogAnalyzerServiceTest.java             # 29 test - analisi log
      DataMockGeneratorServiceTest.java       # 28 test - generazione dati mock
      DbSchemaExplorerServiceTest.java        # 17 test - esplorazione schema DB
      ApiDocumentationServiceTest.java        # 23 test - documentazione API
      CodebaseKnowledgeServiceTest.java       # 23 test - knowledge base codebase
      AgileMetricsServiceTest.java            # 58 test - metriche agile
      ScrumBoardServiceTest.java              # 59 test - gestione scrum board
      TimeTrackingServiceTest.java            # 46 test - tracciamento tempo
      ProjectEconomicsServiceTest.java        # 36 test - economia progetto
      RetrospectiveServiceTest.java           # 55 test - retrospettive sprint
      StandupServiceTest.java                 # 29 test - standup giornalieri
      EnvironmentManagerServiceTest.java      # 35 test - gestione ambienti
      AccessPolicyServiceTest.java            # 27 test - policy di accesso
      DecisionLogServiceTest.java             # 28 test - log decisioni
      IncidentManagerServiceTest.java         # 41 test - gestione incidenti
      WorkflowOrchestratorServiceTest.java    # 26 test - orchestrazione workflow
      QualityGateServiceTest.java             # 31 test - quality gate
      McpInternalRegistryServiceTest.java     # 18 test - registry interno MCP
      DashboardServiceTest.java               # 12 test - dashboard MCP
      InsightEngineServiceTest.java           # 27 test - motore insight
    automation/service/
      AutomationServiceTest.java              # 13 test - trigger, CRUD webhook, test dispatch

  localmind-api/src/test/java/com/localmind/api/
    llm/controller/
      ChatControllerTest.java                 # 14 test - POST /chat, validazione, RAG
      ModelControllerTest.java                # 3 test - GET /models, 404
      ConversationControllerTest.java         # 26 test - CRUD conversazioni REST
    document/controller/
      DocumentControllerTest.java             # 5 test - upload, CRUD, 404
      DocumentSearchControllerTest.java       # 2 test - POST /search
      FolderControllerTest.java               # 8 test - CRUD cartelle REST
    settings/controller/
      SettingsControllerTest.java             # 9 test - provider CRUD, Ollama models
    mcp/controller/
      McpServerControllerTest.java            # 5 test - server CRUD, test/reconnect
      McpToolControllerTest.java              # 4 test - tools listing, execute
      McpScrumControllerTest.java             # 11 test - MockMvc standalone, CRUD scrum board
      McpIncidentControllerTest.java          # 8 test - MockMvc standalone, ciclo vita incidenti
      McpTimeControllerTest.java              # 6 test - MockMvc standalone, time tracking
    automation/controller/
      WebhookControllerTest.java              # 14 test - CRUD webhooks REST, validazione
    auth/controller/
      AuthControllerTest.java                 # 10 test - login, setup, status
    dashboard/controller/
      DashboardControllerTest.java            # 5 test - health check
    common/advice/
      GlobalExceptionHandlerTest.java         # 4 test - 404, 502, 500

  localmind-infrastructure/src/test/java/com/localmind/infrastructure/
    persistence/adapter/
      DocumentRepositoryAdapterTest.java             # 5 test - mapping domain/entity
      ProviderConfigRepositoryAdapterTest.java       # 5 test - CRUD delegazione
      FolderConfigRepositoryAdapterTest.java         # 4 test - CRUD delegazione
      LlmUsageRepositoryAdapterTest.java             # 3 test - save, query
      ConversationRepositoryAdapterTest.java         # 24 test - CRUD conversazioni
      DocumentChunkRepositoryAdapterTest.java        # 6 test - salvataggio chunk documenti
    persistence/adapter/mcp/
      SnippetRepositoryAdapterTest.java              # 10 test - CRUD snippet
      HttpRequestRepositoryAdapterTest.java          # 4 test - salvataggio richieste HTTP
      CodeReviewRepositoryAdapterTest.java           # 5 test - salvataggio review
      ScaffoldingRepositoryAdapterTest.java          # 4 test - salvataggio scaffold
      TestGeneratorRepositoryAdapterTest.java        # 7 test - salvataggio test generati
      DependencyAnalysisRepositoryAdapterTest.java   # 6 test - salvataggio analisi dipendenze
      PerformanceProfilerRepositoryAdapterTest.java  # 4 test - salvataggio profiling
      CicdMonitorRepositoryAdapterTest.java          # 4 test - salvataggio run CI/CD
      DockerAnalysisRepositoryAdapterTest.java       # 3 test - salvataggio analisi Docker
      LogAnalysisRepositoryAdapterTest.java          # 3 test - salvataggio analisi log
      DataMockRepositoryAdapterTest.java             # 3 test - salvataggio dataset
      SchemaExplorationRepositoryAdapterTest.java    # 3 test - salvataggio esplorazione schema
      ApiDocumentationRepositoryAdapterTest.java     # 3 test - salvataggio doc API
      CodebaseKnowledgeRepositoryAdapterTest.java    # 3 test - salvataggio knowledge
      AgileMetricRepositoryAdapterTest.java          # 3 test - salvataggio metriche
      ScrumBoardRepositoryAdapterTest.java           # 10 test - salvataggio scrum
      TimeTrackingRepositoryAdapterTest.java         # 6 test - salvataggio time entry
      ProjectEconomicsRepositoryAdapterTest.java     # 8 test - salvataggio budget/costi
      RetrospectiveRepositoryAdapterTest.java        # 9 test - salvataggio retrospettive
      StandupRepositoryAdapterTest.java              # 3 test - salvataggio standup
      AccessPolicyRepositoryAdapterTest.java         # 5 test - salvataggio policy
      DecisionLogRepositoryAdapterTest.java          # 5 test - salvataggio decisioni
      IncidentRepositoryAdapterTest.java             # 5 test - salvataggio incidenti
      WorkflowRepositoryAdapterTest.java             # 8 test - salvataggio workflow
      QualityGateRepositoryAdapterTest.java          # 5 test - salvataggio quality gate
      InsightRepositoryAdapterTest.java              # 3 test - salvataggio insight
    persistence/adapter/automation/
      WebhookRepositoryAdapterTest.java       # 9 test - mapping domain/entity webhook
    llm/adapter/
      OllamaLlmAdapterTest.java                     # 2 test - call(), isAvailable
      OllamaModelAdapterTest.java                   # 7 test - list, pull, delete
    document/adapter/
      TikaTextExtractorTest.java                     # 2 test - estrazione testo
    mcp/service/
      ToolCallingServiceTest.java                    # 19 test - invocazione tool MCP
    mcp/server/
      LocalMindMcpToolsTest.java                     # 3 test - registrazione tool MCP
    mcp/security/
      McpAccessInterceptorTest.java                  # 8 test - HandlerInterceptor accesso MCP

  localmind-batch/src/test/java/com/localmind/batch/
    scheduler/
      BatchSchedulerTest.java                 # 2 test - launch job, exception
```

**Totale: ~86 file, 1443 test cases**

## Pattern di Test

### Domain Services (Mockito puro)

```java
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    @Mock private DocumentRepository documentRepository;
    @Mock private VectorStorePort vectorStorePort;
    @Mock private DocumentChunkRepository documentChunkRepository;
    @InjectMocks private DocumentService documentService;

    @Test
    void deleteById_shouldCascadeDeleteInOrder() {
        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));
        documentService.deleteById("doc-1");
        InOrder inOrder = inOrder(vectorStorePort, documentChunkRepository, documentRepository);
        inOrder.verify(vectorStorePort).deleteByDocumentId("doc-1");
        inOrder.verify(documentChunkRepository).deleteByDocumentId("doc-1");
        inOrder.verify(documentRepository).deleteById("doc-1");
    }
}
```

### Controller (MockMvc standalone)

```java
class ChatControllerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ChatUseCase chatUseCase = mock(ChatUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ChatController(chatUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void chat_shouldReturnOkWithResponse() throws Exception {
        mockMvc.perform(post("/api/v1/chat")...)
                .andExpect(status().isOk());
    }
}
```

### Infrastructure Adapter

```java
@ExtendWith(MockitoExtension.class)
class DocumentRepositoryAdapterTest {
    @Mock private JpaDocumentRepository jpaRepository;
    @InjectMocks private DocumentRepositoryAdapter adapter;

    @Test
    void save_shouldMapDomainToEntityAndBack() {
        // Verifica mapping bidirezionale domain <-> entity
    }
}
```

### HandlerInterceptor (unit test puro)

```java
class McpAccessInterceptorTest {
    private McpAccessInterceptor interceptor;
    private AccessPolicyUseCase accessPolicyUseCase;

    @BeforeEach
    void setUp() {
        accessPolicyUseCase = mock(AccessPolicyUseCase.class);
        interceptor = new McpAccessInterceptor(accessPolicyUseCase);
    }

    @Test
    void nonPostRequest_shouldPassThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/mcp/tools/execute");
        // Verifica che richieste non-POST passino senza controllo
    }
}
```

## Riepilogo per Modulo

| Modulo | File | Test | Tempo |
|--------|------|------|-------|
| localmind-domain | 44 | 1060 | ~8s |
| localmind-api | 15 | 134 | ~4s |
| localmind-infrastructure | 39 | 247 | ~5s |
| localmind-batch | 1 | 2 | ~2s |
| **Totale** | **~86** | **1443** | **~19s** |
