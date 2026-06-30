# Testing Patterns

**Analysis Date:** 2026-06-29

## Overview

The project has two distinct testing stacks: JUnit 5 + Mockito for the backend (Spring Boot), and Vitest + Playwright for the frontend (Angular 21). Integration and E2E tests are separated from unit tests by tagging and separate Playwright projects.

---

## Backend Test Framework

**Runner:** JUnit 5 (JUnit Jupiter), provided via `spring-boot-starter-test`

**Assertion Library:** AssertJ (`org.assertj.core.api.Assertions.assertThat`)

**Mocking:** Mockito (included in `spring-boot-starter-test`)

**Coverage:** JaCoCo 0.8.12 — configured in parent `localmind-backend/pom.xml`

**Integration Tests:** Testcontainers 1.20.4 with `MySQLContainer`

**Config:** `localmind-backend/pom.xml` (parent)

### Run Commands

```bash
# Unit tests only (default — integration tests excluded via surefire config)
cd localmind-backend && mvn test

# Integration tests only (requires running infra)
cd localmind-backend && mvn verify -Pintegration

# Coverage report (generated during mvn test)
# Output: target/site/jacoco/index.html in each module
```

---

## Backend Test File Organization

**Location:** Tests are in `src/test/java/` mirroring the `src/main/java/` package structure.

**Naming:** `*Test.java` suffix for unit tests, `*IT.java` suffix for integration tests.

```
localmind-backend/
├── localmind-api/src/test/java/com/localmind/api/
│   ├── common/advice/GlobalExceptionHandlerTest.java
│   ├── llm/controller/ChatControllerTest.java
│   ├── document/controller/DocumentControllerTest.java
│   └── ...
├── localmind-infrastructure/src/test/java/com/localmind/infrastructure/
│   ├── security/RateLimitFilterTest.java
│   ├── security/LocalAuthFilterTest.java
│   └── ...
├── localmind-shared-types/src/test/java/com/localmind/shared/model/
│   ├── PageResponseTest.java
│   └── ...
└── localmind-app/src/test/java/com/localmind/integration/
    └── AbstractIntegrationTest.java    # Base class for integration tests
```

---

## Backend Test Structure

### Controller Unit Tests (MockMvc Standalone)

Controller tests use `MockMvcBuilders.standaloneSetup()` — **no Spring application context is loaded**. All dependencies are Mockito mocks injected via constructor.

```java
class ChatControllerTest {

    private MockMvc mockMvc;
    private ChatUseCase chatUseCase;
    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        chatUseCase = mock(ChatUseCase.class);
        conversationService = mock(ConversationService.class);
        // Construct controller directly with mocks
        ChatController controller = new ChatController(chatUseCase, conversationService, ...);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())  // Always include
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void chat_shouldReturnOkWithResponse() throws Exception {
        // Arrange: configure mock returns
        when(chatUseCase.chat(any())).thenReturn(llmResponse);

        // Act + Assert: perform request and verify JSON response
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello from AI"))
                .andExpect(jsonPath("$.provider").value("OLLAMA"));

        // Verify: interaction assertions
        verify(chatUseCase).chat(any());
    }
}
```

Key: Always add `setControllerAdvice(new GlobalExceptionHandler())` to test error responses.

Located: `localmind-backend/localmind-api/src/test/java/com/localmind/api/`

### Exception Handler Tests

Tests for `GlobalExceptionHandler` use a disposable inner `@RestController` that throws exceptions:

```java
class GlobalExceptionHandlerTest {

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        public void notFound() {
            throw new ResourceNotFoundException("...");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
```

Located: `localmind-backend/localmind-api/src/test/java/com/localmind/api/common/advice/GlobalExceptionHandlerTest.java`

### Filter Unit Tests

Servlet filters are tested directly via Spring Mock objects — no web context needed:

```java
class RateLimitFilterTest {
    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(3, 60);  // Direct instantiation
    }

    @Test
    void requestsBeyondLimit_shouldReturn429() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/chat");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
    }
}
```

Located: `localmind-backend/localmind-infrastructure/src/test/java/com/localmind/infrastructure/security/`

### Domain Model Tests

Pure Java tests with Lombok builder pattern, no mocks needed:

```java
class PageResponseTest {

    @Test
    void testBuilder_setsAllFields() {
        PageResponse<String> response = PageResponse.<String>builder()
                .content(List.of("a", "b", "c"))
                .page(2)
                .totalElements(53)
                .build();

        assertThat(response.getContent()).containsExactly("a", "b", "c");
        assertThat(response.getPage()).isEqualTo(2);
    }
}
```

Located: `localmind-backend/localmind-shared-types/src/test/java/com/localmind/shared/model/`

### Integration Tests (Testcontainers)

All integration tests extend `AbstractIntegrationTest` which uses `@Testcontainers` with a MySQL container:

```java
@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("localmind_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    protected TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        // ...
    }
}
```

Located: `localmind-backend/localmind-app/src/test/java/com/localmind/integration/AbstractIntegrationTest.java`

Integration tests are **excluded from `mvn test`** by `maven-surefire-plugin`:
```xml
<excludedGroups>integration</excludedGroups>
```

Run integration tests with: `mvn verify -Pintegration`

---

## Backend Mocking

**Framework:** Mockito (via `mock()` static method — no `@ExtendWith(MockitoExtension.class)` needed in standalone controller tests)

```java
// Instantiate mocks directly
ChatUseCase chatUseCase = mock(ChatUseCase.class);

// Stub return values
when(chatUseCase.chat(any())).thenReturn(llmResponse);
when(chatUseCase.chat(any())).thenReturn(firstResponse, finalResponse);  // multiple returns

// Capture arguments
ArgumentCaptor<LlmRequest> requestCaptor = ArgumentCaptor.forClass(LlmRequest.class);
when(chatUseCase.chat(requestCaptor.capture())).thenReturn(llmResponse);
LlmRequest captured = requestCaptor.getValue();

// Verify interactions
verify(chatUseCase).chat(any());
verify(chatUseCase, times(2)).chat(any());
verify(toolCallingPort, never()).executeToolCall(any());
```

**What to mock:** All dependencies injected into the controller (use case interfaces, domain services).

**What NOT to mock:** `GlobalExceptionHandler` — always instantiate it directly.

---

## Backend Test Naming Convention

Test methods use `methodName_condition_expectedOutcome` pattern:
```java
void chat_shouldReturnOkWithResponse()
void chat_withExistingConversationId_shouldPassItThrough()
void chat_shouldReturn502OnLlmProviderException()
void requestsBeyondLimit_shouldReturn429()
void testBuilder_setsAllFields()
```

---

## Frontend Test Framework

**Runner:** Vitest 4.x via Angular CLI builder (`@angular/build:unit-test`)

**Config:** `localmind-frontend/tsconfig.spec.json` — declares `"types": ["vitest/globals"]`

**Assertion Library:** Vitest built-in `expect` (compatible with Jest API)

**HTTP Mocking:** Angular `HttpTestingController` (`provideHttpClientTesting()`)

**E2E:** Playwright 1.58.x (`localmind-frontend/playwright.config.ts`)

### Run Commands

```bash
cd localmind-frontend

# Unit tests (Vitest via Angular CLI)
ng test

# E2E - UI tests (no live backend needed)
npx playwright test --project=ui

# E2E - Integration tests (requires live backend + frontend)
npx playwright test --project=integration

# E2E - Headed mode for debugging
npx playwright test --headed

# E2E - Show HTML report
npx playwright show-report
```

---

## Frontend Test File Organization

**Location:** Tests are **co-located** with source files in `src/app/`.

**Naming:** `*.spec.ts` suffix.

```
localmind-frontend/src/app/
├── app.spec.ts
├── layout/layout.component.spec.ts
├── core/
│   ├── services/api.service.spec.ts
│   ├── services/auth.service.spec.ts
│   ├── services/theme.service.spec.ts
│   └── i18n/translation.service.spec.ts
├── features/
│   ├── chat/services/chat.service.spec.ts
│   ├── chat/services/conversation.service.spec.ts
│   ├── documents/services/document.service.spec.ts
│   ├── search/services/search.service.spec.ts
│   ├── folders/services/folder.service.spec.ts
│   └── settings/services/settings.service.spec.ts
└── shared/components/
    ├── empty-state/empty-state.component.spec.ts
    ├── language-switcher/language-switcher.component.spec.ts
    └── theme-toggle/theme-toggle.component.spec.ts
```

E2E tests are in `localmind-frontend/e2e/` (separated by Playwright convention):
```
localmind-frontend/e2e/
├── chat.spec.ts                   # UI tests (no backend)
├── documents.spec.ts
├── settings.spec.ts
├── i18n.spec.ts
└── integration/
    ├── chat.integration.spec.ts   # Integration tests (live backend)
    ├── documents.integration.spec.ts
    └── ...
```

---

## Frontend Test Structure

### Service Unit Tests

All service tests use `TestBed` with `provideHttpClient()` + `provideHttpClientTesting()`:

```typescript
describe('ChatService', () => {
  let service: ChatService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ChatService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(ChatService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());   // Always verify no outstanding requests

  it('dovrebbe essere creato / should be created', () => {
    expect(service).toBeTruthy();
  });

  it('chat() chiama POST su /chat / chat() calls POST on /chat', () => {
    const request: ChatRequest = { message: 'Test' };

    service.chat(request).subscribe(data => {
      expect(data).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/chat`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);                 // Always flush to resolve Observable
  });
});
```

### Component Unit Tests

```typescript
describe('EmptyStateComponent', () => {
  let component: EmptyStateComponent;
  let fixture: ComponentFixture<EmptyStateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmptyStateComponent]    // Import standalone component directly
    }).compileComponents();

    fixture = TestBed.createComponent(EmptyStateComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('title', 'Nessun risultato');  // Signal input setter
    fixture.detectChanges();
  });

  it('mostra il titolo / shows title', () => {
    const titleEl = fixture.nativeElement.querySelector('.empty-title');
    expect(titleEl.textContent).toContain('Nessun risultato');
  });
});
```

Key points:
- Import standalone components via `imports: [ComponentClass]` in `configureTestingModule`
- Use `fixture.componentRef.setInput('name', value)` to set signal-based inputs
- Always call `fixture.detectChanges()` after setting inputs

---

## Frontend Mocking

**HTTP:** Use `HttpTestingController` — always pair with `provideHttpClientTesting()`:
```typescript
// Expect exactly one request
const req = httpMock.expectOne(`${environment.apiUrl}/documents`);
// Verify the HTTP method
expect(req.request.method).toBe('GET');
// Respond with mock data (required to resolve Observable)
req.flush(mockData);
```

**Service mocking in component tests:** Use Angular's `TestBed` providers to override:
```typescript
TestBed.configureTestingModule({
  imports: [MyComponent],
  providers: [
    { provide: MyService, useValue: { method: vi.fn().mockReturnValue(of(mockData)) } }
  ]
});
```

**Local storage:** Clear in `beforeEach`/`afterEach` when testing services that use it:
```typescript
beforeEach(() => { localStorage.clear(); });
afterEach(() => { localStorage.clear(); });
```

---

## E2E Tests (Playwright)

**Config:** `localmind-frontend/playwright.config.ts`

**Two Playwright projects:**
| Project | `testMatch` | Timeout | Requires Backend |
|---------|------------|---------|-----------------|
| `ui` | `*.spec.ts` (excluding `integration`) | 30s | No |
| `integration` | `*.integration.spec.ts` | 120s | Yes |

### UI E2E Pattern (Page Object Model)

E2E tests use Page Object classes in `localmind-frontend/e2e/pages/`:
```typescript
// e2e/chat.spec.ts
import { test, expect } from '@playwright/test';
import { ChatPage } from './pages/chat.page';

test.describe('Chat', () => {
  let chat: ChatPage;

  test.beforeEach(async ({ page }) => {
    await page.goto('/chat');
    chat = new ChatPage(page);
  });

  test('send button is disabled when textarea is empty', async () => {
    await expect(chat.sendBtn).toBeDisabled();
  });
});
```

### Integration E2E Pattern

Integration tests interact with live backend and tolerate slow responses:
```typescript
test('send a message and receive a response or error', async ({ page }) => {
  await page.goto('/chat');
  await page.locator('textarea').fill('Test message');
  await page.locator('.send-btn').click();

  // Wait for typing indicator to appear then disappear
  await page.locator('.typing-indicator').waitFor({ state: 'visible', timeout: 10_000 }).catch(() => {});
  await page.locator('.typing-indicator').waitFor({ state: 'hidden', timeout: 90_000 });

  // Accept either a response or an error (backend may be unavailable)
  const gotResponse = await page.locator('.message.assistant').isVisible().catch(() => false);
  const gotError = await page.locator('.error-bar').isVisible().catch(() => false);
  expect(gotResponse || gotError).toBeTruthy();
});
```

---

## Frontend Test Naming Convention

Bilingual naming — Italian description first, English after `/`:
```typescript
it('dovrebbe essere creato / should be created', () => { ... })
it('chat() chiama POST su /chat con il messaggio / chat() calls POST on /chat with message', () => { ... })
it('gestisce errori HTTP senza crash / handles HTTP errors without crashing', () => { ... })
```

---

## Error Testing Patterns

### Backend (verify HTTP error codes)
```java
mockMvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON).content(json))
       .andExpect(status().isBadGateway())        // 502
       .andExpect(jsonPath("$.status").value(502))
       .andExpect(jsonPath("$.message").value("LLM provider error: Connection refused"));
```

### Frontend (verify RxJS error propagation)
```typescript
service.chat(request).subscribe({
  error: () => { errorReceived = true; }
});
const req = httpMock.expectOne(`${environment.apiUrl}/chat`);
req.flush('Server Error', { status: 500, statusText: 'Internal Server Error' });
expect(errorReceived).toBe(true);
```

---

## Coverage

**Backend:** JaCoCo configured in parent `localmind-backend/pom.xml`. Reports generated at `target/site/jacoco/index.html` in each module during `mvn test`.

**Frontend:** No explicit coverage threshold configured. Vitest coverage can be run (tool available but no `package.json` script defined for it).

**Minimum target per project guidelines:** 80% (enforced by convention, not by gate currently).

---

## Test Types Coverage

**Unit Tests (backend):**
- Controller tests via MockMvc standalone: `localmind-api/src/test/`
- Infrastructure filter tests: `localmind-infrastructure/src/test/`
- Domain model builder tests: `localmind-shared-types/src/test/`

**Unit Tests (frontend):**
- Service tests: all feature services in `src/app/features/*/services/`
- Core service tests: `src/app/core/services/`
- Component tests: `src/app/shared/components/`
- Root component: `src/app/app.spec.ts`

**Integration Tests (backend):**
- `AbstractIntegrationTest` base with Testcontainers MySQL
- Tagged `@Tag("integration")`, excluded from default `mvn test`

**E2E Tests (frontend):**
- UI E2E: 14 test files covering all major routes
- Integration E2E: 6 test files in `e2e/integration/` requiring live backend

**What is NOT tested:**
- Domain service business logic (e.g., `DocumentIngestionPipelineService`, `LlmGatewayService`) — no unit tests found for domain services
- Infrastructure adapter mapping (`toEntity`/`toDomain` conversions)
- Batch jobs (`localmind-batch`)

---

*Testing analysis: 2026-06-29*
