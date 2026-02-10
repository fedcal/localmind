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
      ChunkingServiceTest.java          # 6 test - logica chunking pura
      DocumentServiceTest.java          # 7 test - ingest, CRUD documenti
      DocumentSearchServiceTest.java    # 3 test - delega a VectorStorePort
    llm/service/
      LlmGatewayServiceTest.java        # 8 test - fallback chain, usage tracking
      ModelManagementServiceTest.java   # 5 test - lista modelli, getModel
      ProviderConfigServiceTest.java    # 12 test - CRUD provider, testConnection
      CostTrackingServiceTest.java      # 4 test - aggregazione costi
    mcp/service/
      McpServerManagementServiceTest.java  # 10 test - register, reconnect
      McpToolOrchestratorServiceTest.java  # 4 test - discovery, execution

  localmind-api/src/test/java/com/localmind/api/
    llm/controller/
      ChatControllerTest.java           # 2 test - POST /chat ok/502
      ModelControllerTest.java          # 3 test - GET /models, 404
    document/controller/
      DocumentControllerTest.java       # 5 test - upload, CRUD, 404
      DocumentSearchControllerTest.java # 2 test - POST /search
    settings/controller/
      SettingsControllerTest.java       # 6 test - provider CRUD, Ollama models
    mcp/controller/
      McpServerControllerTest.java      # 5 test - server CRUD, test/reconnect
      McpToolControllerTest.java        # 4 test - tools listing, execute
    dashboard/controller/
      DashboardControllerTest.java      # 2 test - health check
    common/advice/
      GlobalExceptionHandlerTest.java   # 4 test - 404, 502, 500

  localmind-infrastructure/src/test/java/com/localmind/infrastructure/
    persistence/adapter/
      DocumentRepositoryAdapterTest.java       # 5 test - mapping domain/entity
      ProviderConfigRepositoryAdapterTest.java # 5 test - CRUD delegazione
      FolderConfigRepositoryAdapterTest.java   # 4 test - CRUD delegazione
      LlmUsageRepositoryAdapterTest.java       # 3 test - save, query
    llm/adapter/
      OllamaLlmAdapterTest.java        # 2 test - call(), isAvailable
      OllamaModelAdapterTest.java      # 4 test - list, pull, delete
    document/adapter/
      TikaTextExtractorTest.java        # 2 test - estrazione testo

  localmind-batch/src/test/java/com/localmind/batch/
    scheduler/
      BatchSchedulerTest.java           # 2 test - launch job, exception
```

**Totale: 26 file, 119 test cases**

## Pattern di Test

### Domain Services (Mockito puro)

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

## Riepilogo per Modulo

| Modulo | File | Test | Tempo |
|--------|------|------|-------|
| localmind-domain | 9 | 59 | ~2s |
| localmind-api | 9 | 33 | ~2.5s |
| localmind-infrastructure | 7 | 25 | ~3s |
| localmind-batch | 1 | 2 | ~1.5s |
| **Totale** | **26** | **119** | **~9s** |
