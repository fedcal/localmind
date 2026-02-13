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
      ChunkingServiceTest.java          # 6 tests - pure chunking logic
      DocumentServiceTest.java          # 7 tests - ingest, document CRUD
      DocumentSearchServiceTest.java    # 3 tests - delegates to VectorStorePort
    llm/service/
      LlmGatewayServiceTest.java        # 8 tests - fallback chain, usage tracking
      ModelManagementServiceTest.java   # 5 tests - list models, getModel
      ProviderConfigServiceTest.java    # 12 tests - provider CRUD, testConnection
      CostTrackingServiceTest.java      # 4 tests - cost aggregation
    mcp/service/
      McpServerManagementServiceTest.java  # 10 tests - register, reconnect
      McpToolOrchestratorServiceTest.java  # 4 tests - discovery, execution

  localmind-api/src/test/java/com/localmind/api/
    llm/controller/
      ChatControllerTest.java           # 2 tests - POST /chat ok/502
      ModelControllerTest.java          # 3 tests - GET /models, 404
    document/controller/
      DocumentControllerTest.java       # 5 tests - upload, CRUD, 404
      DocumentSearchControllerTest.java # 2 tests - POST /search
    settings/controller/
      SettingsControllerTest.java       # 6 tests - provider CRUD, Ollama models
    mcp/controller/
      McpServerControllerTest.java      # 5 tests - server CRUD, test/reconnect
      McpToolControllerTest.java        # 4 tests - tools listing, execute
    dashboard/controller/
      DashboardControllerTest.java      # 2 tests - health check
    common/advice/
      GlobalExceptionHandlerTest.java   # 4 tests - 404, 502, 500

  localmind-infrastructure/src/test/java/com/localmind/infrastructure/
    persistence/adapter/
      DocumentRepositoryAdapterTest.java       # 5 tests - domain/entity mapping
      ProviderConfigRepositoryAdapterTest.java # 5 tests - CRUD delegation
      FolderConfigRepositoryAdapterTest.java   # 4 tests - CRUD delegation
      LlmUsageRepositoryAdapterTest.java       # 3 tests - save, query
    llm/adapter/
      OllamaLlmAdapterTest.java        # 2 tests - call(), isAvailable
      OllamaModelAdapterTest.java      # 4 tests - list, pull, delete
    document/adapter/
      TikaTextExtractorTest.java        # 2 tests - text extraction

  localmind-batch/src/test/java/com/localmind/batch/
    scheduler/
      BatchSchedulerTest.java           # 2 tests - launch job, exception
```

**Total: 26 files, 119 test cases**

## Test Patterns

### Domain Services (Pure Mockito)

```java
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    @Mock private DocumentRepository documentRepository;
    @InjectMocks private DocumentService documentService;

    @Test
    void ingest_shouldSaveDocumentWithPendingStatus() {
        when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Document result = documentService.ingest("test.pdf", ...);
        assertThat(result.getStatus()).isEqualTo(DocumentStatus.PENDING);
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

## Summary by Module

| Module | Files | Tests | Time |
|--------------------------|--------|---------|---------|
| localmind-domain         | 9      | 59      | ~2s     |
| localmind-api            | 9      | 33      | ~2.5s   |
| localmind-infrastructure | 7      | 25      | ~3s     |
| localmind-batch          | 1      | 2       | ~1.5s   |
| **Total**                | **26** | **119** | **~9s** |
