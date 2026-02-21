# Backend Unit Tests (JUnit 5 + Mockito)

## Overview

The Spring Boot backend includes a comprehensive unit test suite based on **JUnit 5**, **Mockito 5.14**, and **AssertJ 3.26**. All tests are pure unit tests with mocked dependencies, requiring no external services (MySQL, Qdrant, Ollama).

## Dependencies

`spring-boot-starter-test` in the parent POM provides all necessary dependencies (JUnit Jupiter, Mockito, AssertJ, Spring MockMvc).

## Running Tests

```bash
cd localmind-backend

# All tests across all modules
mvn test

# Domain tests only
mvn -pl localmind-domain test

# Controller tests only
mvn -pl localmind-api test

# Infrastructure adapter tests only
mvn -pl localmind-infrastructure test

# Batch tests only
mvn -pl localmind-batch test
```

## Structure

```
localmind-backend/
  localmind-domain/src/test/java/com/localmind/domain/
    document/service/
      ChunkingServiceTest.java                # 6 tests - pure chunking logic
      DocumentServiceTest.java                # 5 tests - CRUD, cascade delete
      DocumentSearchServiceTest.java          # 3 tests - delegates to VectorStorePort
      DocumentIngestionPipelineServiceTest.java # 13 tests - pipeline ingest, dedup, extract, chunk
      FolderManagementServiceTest.java          # 8 tests - folder CRUD, triggerSync
      PathValidationServiceTest.java            # 16 tests - cross-OS path validation
    llm/service/
      LlmGatewayServiceTest.java              # 8 tests - fallback chain, usage tracking
      ModelManagementServiceTest.java         # 5 tests - list models, getModel
      ProviderConfigServiceTest.java          # 15 tests - provider CRUD, testConnection
      CostTrackingServiceTest.java            # 4 tests - cost aggregation
      ConversationServiceTest.java            # 37 tests - conversation CRUD, messages
    mcp/service/
      McpServerManagementServiceTest.java     # 10 tests - register, reconnect
      McpToolOrchestratorServiceTest.java     # 4 tests - discovery, execution
      RegexServiceTest.java                   # 18 tests - regex match, replace, validate
      SnippetServiceTest.java                 # 12 tests - code snippet CRUD
      HttpClientServiceTest.java              # 14 tests - GET/POST/PUT/DELETE, headers
      ProjectScaffoldingServiceTest.java      # 19 tests - project scaffolding
      TestGeneratorServiceTest.java           # 34 tests - unit test generation
      CodeReviewServiceTest.java              # 39 tests - code analysis, suggestions
      DependencyAnalysisServiceTest.java      # 34 tests - dependency analysis
      PerformanceProfilerServiceTest.java     # 32 tests - performance profiling
      CicdMonitorServiceTest.java             # 14 tests - CI/CD monitoring
      DockerAnalysisServiceTest.java          # 36 tests - Docker Compose analysis
      LogAnalyzerServiceTest.java             # 29 tests - log analysis
      DataMockGeneratorServiceTest.java       # 28 tests - mock data generation
      DbSchemaExplorerServiceTest.java        # 17 tests - DB schema exploration
      ApiDocumentationServiceTest.java        # 23 tests - API documentation
      CodebaseKnowledgeServiceTest.java       # 23 tests - codebase knowledge base
      AgileMetricsServiceTest.java            # 58 tests - agile metrics
      ScrumBoardServiceTest.java              # 59 tests - scrum board management
      TimeTrackingServiceTest.java            # 46 tests - time tracking
      ProjectEconomicsServiceTest.java        # 36 tests - project economics
      RetrospectiveServiceTest.java           # 55 tests - sprint retrospectives
      StandupServiceTest.java                 # 29 tests - daily standups
      EnvironmentManagerServiceTest.java      # 35 tests - environment management
      AccessPolicyServiceTest.java            # 27 tests - access policies
      DecisionLogServiceTest.java             # 28 tests - decision log
      IncidentManagerServiceTest.java         # 41 tests - incident management
      WorkflowOrchestratorServiceTest.java    # 26 tests - workflow orchestration
      QualityGateServiceTest.java             # 31 tests - quality gates
      McpInternalRegistryServiceTest.java     # 18 tests - MCP internal registry
      DashboardServiceTest.java               # 12 tests - MCP dashboard
      InsightEngineServiceTest.java           # 27 tests - insight engine
    automation/service/
      AutomationServiceTest.java              # 13 tests - trigger, webhook CRUD, test dispatch

  localmind-api/src/test/java/com/localmind/api/
    llm/controller/
      ChatControllerTest.java                 # 14 tests - POST /chat, validation, RAG
      ModelControllerTest.java                # 3 tests - GET /models, 404
      ConversationControllerTest.java         # 26 tests - conversation REST CRUD
    document/controller/
      DocumentControllerTest.java             # 5 tests - upload, CRUD, 404
      DocumentSearchControllerTest.java       # 2 tests - POST /search
      FolderControllerTest.java               # 8 tests - folder REST CRUD
    settings/controller/
      SettingsControllerTest.java             # 9 tests - provider CRUD, Ollama models
    mcp/controller/
      McpServerControllerTest.java            # 5 tests - server CRUD, test/reconnect
      McpToolControllerTest.java              # 4 tests - tools listing, execute
      McpScrumControllerTest.java             # 11 tests - MockMvc standalone, scrum board CRUD
      McpIncidentControllerTest.java          # 8 tests - MockMvc standalone, incident lifecycle
      McpTimeControllerTest.java              # 6 tests - MockMvc standalone, time tracking
    automation/controller/
      WebhookControllerTest.java              # 14 tests - webhook REST CRUD, validation
    auth/controller/
      AuthControllerTest.java                 # 10 tests - login, setup, status
    dashboard/controller/
      DashboardControllerTest.java            # 5 tests - health check
    common/advice/
      GlobalExceptionHandlerTest.java         # 4 tests - 404, 502, 500

  localmind-infrastructure/src/test/java/com/localmind/infrastructure/
    persistence/adapter/
      DocumentRepositoryAdapterTest.java             # 5 tests - domain/entity mapping
      ProviderConfigRepositoryAdapterTest.java       # 5 tests - CRUD delegation
      FolderConfigRepositoryAdapterTest.java         # 4 tests - CRUD delegation
      LlmUsageRepositoryAdapterTest.java             # 3 tests - save, query
      ConversationRepositoryAdapterTest.java         # 24 tests - conversation CRUD
      DocumentChunkRepositoryAdapterTest.java        # 6 tests - document chunk persistence
    persistence/adapter/mcp/
      SnippetRepositoryAdapterTest.java              # 10 tests - snippet CRUD
      HttpRequestRepositoryAdapterTest.java          # 4 tests - HTTP request persistence
      CodeReviewRepositoryAdapterTest.java           # 5 tests - review persistence
      ScaffoldingRepositoryAdapterTest.java          # 4 tests - scaffold persistence
      TestGeneratorRepositoryAdapterTest.java        # 7 tests - generated tests persistence
      DependencyAnalysisRepositoryAdapterTest.java   # 6 tests - dependency analysis persistence
      PerformanceProfilerRepositoryAdapterTest.java  # 4 tests - profiling persistence
      CicdMonitorRepositoryAdapterTest.java          # 4 tests - CI/CD run persistence
      DockerAnalysisRepositoryAdapterTest.java       # 3 tests - Docker analysis persistence
      LogAnalysisRepositoryAdapterTest.java          # 3 tests - log analysis persistence
      DataMockRepositoryAdapterTest.java             # 3 tests - dataset persistence
      SchemaExplorationRepositoryAdapterTest.java    # 3 tests - schema exploration persistence
      ApiDocumentationRepositoryAdapterTest.java     # 3 tests - API doc persistence
      CodebaseKnowledgeRepositoryAdapterTest.java    # 3 tests - knowledge persistence
      AgileMetricRepositoryAdapterTest.java          # 3 tests - metric persistence
      ScrumBoardRepositoryAdapterTest.java           # 10 tests - scrum persistence
      TimeTrackingRepositoryAdapterTest.java         # 6 tests - time entry persistence
      ProjectEconomicsRepositoryAdapterTest.java     # 8 tests - budget/cost persistence
      RetrospectiveRepositoryAdapterTest.java        # 9 tests - retrospective persistence
      StandupRepositoryAdapterTest.java              # 3 tests - standup persistence
      AccessPolicyRepositoryAdapterTest.java         # 5 tests - policy persistence
      DecisionLogRepositoryAdapterTest.java          # 5 tests - decision persistence
      IncidentRepositoryAdapterTest.java             # 5 tests - incident persistence
      WorkflowRepositoryAdapterTest.java             # 8 tests - workflow persistence
      QualityGateRepositoryAdapterTest.java          # 5 tests - quality gate persistence
      InsightRepositoryAdapterTest.java              # 3 tests - insight persistence
    persistence/adapter/automation/
      WebhookRepositoryAdapterTest.java       # 9 tests - webhook domain/entity mapping
    llm/adapter/
      OllamaLlmAdapterTest.java                     # 2 tests - call(), isAvailable
      OllamaModelAdapterTest.java                   # 7 tests - list, pull, delete
    document/adapter/
      TikaTextExtractorTest.java                     # 2 tests - text extraction
    mcp/service/
      ToolCallingServiceTest.java                    # 19 tests - MCP tool invocation
    mcp/server/
      LocalMindMcpToolsTest.java                     # 3 tests - MCP tool registration
    mcp/security/
      McpAccessInterceptorTest.java                  # 8 tests - HandlerInterceptor MCP access

  localmind-batch/src/test/java/com/localmind/batch/
    scheduler/
      BatchSchedulerTest.java                 # 2 tests - launch job, exception
```

**Total: ~86 files, 1443 test cases**

## Test Patterns

### Domain Services (Pure Mockito)

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

### Controllers (Standalone MockMvc)

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

### Infrastructure Adapters

```java
@ExtendWith(MockitoExtension.class)
class DocumentRepositoryAdapterTest {
    @Mock private JpaDocumentRepository jpaRepository;
    @InjectMocks private DocumentRepositoryAdapter adapter;

    @Test
    void save_shouldMapDomainToEntityAndBack() {
        // Verifies bidirectional domain <-> entity mapping
    }
}
```

### HandlerInterceptor (Pure Unit Test)

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
        // Verifies non-POST requests pass through without access check
    }
}
```

## Summary by Module

| Module | Files | Tests | Time |
|--------|-------|-------|------|
| localmind-domain | 44 | 1060 | ~8s |
| localmind-api | 15 | 134 | ~4s |
| localmind-infrastructure | 39 | 247 | ~5s |
| localmind-batch | 1 | 2 | ~2s |
| **Total** | **~86** | **1443** | **~19s** |
