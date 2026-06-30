# Coding Conventions

**Analysis Date:** 2026-06-29

## Language Policy

All code comments, Swagger annotations, test descriptions, and log messages are **bilingual** (Italian first, English after `/`):
```java
// Pubblica evento di cancellazione / Publish deletion event
@Operation(summary = "Lista eventi calendario / List calendar events")
```

Frontend test names follow the same convention:
```typescript
it('dovrebbe essere creato / should be created', () => { ... });
```

Enums have `displayName` fields in English for the API surface.

---

## Naming Patterns

### Backend (Java)

**Classes:**
- Domain models: `PascalCase` — e.g., `Document`, `LlmRequest`, `ConversationContext`
- Enums: `PascalCase` — e.g., `DocumentStatus`, `LlmProvider`
- Port interfaces (inbound use cases): `*UseCase` suffix — e.g., `ChatUseCase`, `DocumentSearchUseCase`
- Port interfaces (outbound): `*Port` or `*Repository` suffix — e.g., `VectorStorePort`, `DocumentRepository`
- Infrastructure adapters: `*Adapter` suffix — e.g., `DocumentRepositoryAdapter`, `OpenAiLlmAdapter`
- JPA Spring Data repos: `Jpa*Repository` prefix — e.g., `JpaDocumentRepository`
- JPA entities: `*Entity` suffix — e.g., `DocumentEntity`, `ConversationEntity`
- REST controllers: `*Controller` suffix — e.g., `ChatController`, `CalendarController`
- DTOs (requests): `*Request` suffix — e.g., `ChatRequestDto`, `CreateCalendarEventRequest`
- DTOs (responses): `*Dto` suffix — e.g., `CalendarEventDto`, `ErrorResponseDto`
- Exception handler: `GlobalExceptionHandler` in `localmind-api/src/main/java/com/localmind/api/common/advice/`

**Methods and fields:** `camelCase`

**Constants:** `UPPER_SNAKE_CASE` — e.g., `OCR_FALLBACK_MIN_TEXT_LENGTH`

**Packages:**
```
com.localmind.domain.<feature>.model        # Domain entities/value objects
com.localmind.domain.<feature>.port.in      # Use case interfaces
com.localmind.domain.<feature>.port.out     # Repository/adapter interfaces
com.localmind.domain.<feature>.service      # Business logic implementations
com.localmind.infrastructure.<feature>.adapter    # Adapter implementations
com.localmind.infrastructure.persistence.entity   # JPA entities
com.localmind.infrastructure.persistence.adapter  # JPA adapter impls
com.localmind.api.<feature>.controller      # REST controllers
com.localmind.api.<feature>.dto             # Request/response DTOs
com.localmind.api.common.advice             # Cross-cutting exception handlers
```

### Frontend (TypeScript/Angular)

**Files:**
- Components: `feature-name.component.ts` (and `.html`, `.scss`)
- Services: `feature-name.service.ts`
- Tests: `feature-name.service.spec.ts` / `feature-name.component.spec.ts`
- Routes: `feature.routes.ts`
- Models: `feature.model.ts`
- Guards: `auth.guard.ts`
- Interceptors: `error.interceptor.ts`, `auth.interceptor.ts`
- Pipes: `file-size.pipe.ts`, `translate.pipe.ts`

**Classes and interfaces:** `PascalCase` — e.g., `ChatService`, `LlmProviderConfig`

**Functions and variables:** `camelCase`

**Angular Signals:** Use `signal()` and `computed()` for reactive state — e.g., `currentLang()` in `TranslationService`

---

## Code Style

**Frontend Formatting:**
- Tool: Prettier (configured inline in `localmind-frontend/package.json`)
- `printWidth`: 100
- `singleQuote`: true
- Angular HTML parser for `.html` files

**Backend Formatting:**
- Standard Java formatting (no explicit formatter config detected)
- Indentation: 4 spaces

---

## Import Organization

### Backend (Java)
```java
// 1. Static imports first (Mockito, AssertJ)
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

// 2. JDK standard library
import java.time.Instant;
import java.util.List;

// 3. Spring framework
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 4. Project domain imports
import com.localmind.domain.llm.model.*;
import com.localmind.domain.llm.port.in.ChatUseCase;
```

### Frontend (TypeScript)
```typescript
// 1. Angular framework imports
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

// 2. RxJS
import { Observable } from 'rxjs';

// 3. Local project imports (models, environment)
import { environment } from '../../../environments/environment';
import { ChatRequest, ChatResponse } from '../models/chat.model';
```

**No path aliases** detected — all imports use relative paths.

---

## Domain Model Pattern (Backend)

All domain model classes use the Lombok annotation stack:
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    private String id;
    private String filename;
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;
    @Builder.Default
    private Instant createdAt = Instant.now();
}
```

Located: `localmind-backend/localmind-domain/src/main/java/com/localmind/domain/*/model/*.java`

---

## JPA Entity Pattern (Infrastructure)

JPA entities require `@JdbcTypeCode(SqlTypes.CHAR)` on UUID `@Id` fields for MySQL compatibility:
```java
@Entity
@Table(name = "documents")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> metadata;

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }
}
```

Located: `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/persistence/entity/`

Enum values stored as **String** (not ordinal) in the DB:
```java
// Entity stores string
private String status;

// Adapter converts
DocumentStatus.valueOf(entity.getStatus())  // domain ← entity
doc.getStatus().name()                      // entity ← domain
```

---

## Wiring Pattern (Domain Services vs. Spring)

Domain services have **no Spring annotations**. They are pure Java classes wired by `DomainConfig.java` via `@Bean` methods:
```java
// WRONG: annotating domain service
@Service  // Never add this to domain services

// CORRECT: wired in DomainConfig (infrastructure module)
@Configuration
public class DomainConfig {
    @Bean
    public DocumentService documentService(...) {
        return new DocumentService(documentRepository, vectorStorePort, chunkRepository);
    }
}
```

Located: `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/config/DomainConfig.java`

Infrastructure adapters are `@Component` only:
```java
@Component
public class DocumentRepositoryAdapter implements DocumentRepository { ... }
```

---

## Adapter Mapping Pattern

Every JPA adapter (`*RepositoryAdapter`) contains private `toEntity()` and `toDomain()` methods:
```java
@Override
public Optional<Document> findById(String id) {
    return jpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
}

@Override
public Document save(Document document) {
    DocumentEntity entity = toEntity(document);
    DocumentEntity saved = jpaRepository.save(entity);
    return toDomain(saved);
}
```

Located: `localmind-backend/localmind-infrastructure/src/main/java/com/localmind/infrastructure/persistence/adapter/`

---

## REST Controller Pattern

```java
@RestController
@RequestMapping("/api/v1/calendar")
@Tag(name = "Calendar", description = "Calendar event management via CalDAV")
@ConditionalOnProperty(name = "localmind.calendar.enabled", havingValue = "true")
public class CalendarController {

    private final CalendarUseCase calendarUseCase;

    public CalendarController(CalendarUseCase calendarUseCase) {  // Constructor injection only
        this.calendarUseCase = calendarUseCase;
    }

    @GetMapping("/events")
    @Operation(summary = "Lista eventi / List events")
    @ApiResponse(responseCode = "200", description = "Lista / List")
    public ResponseEntity<List<CalendarEventDto>> listEvents(...) { ... }
}
```

Rules:
- Always use constructor injection (never `@Autowired` on fields)
- Always add `@Operation` and `@ApiResponse` Swagger annotations
- Optional modules use `@ConditionalOnProperty`
- All endpoints return `ResponseEntity<T>`

---

## Error Handling

### Backend

Domain exceptions are thrown by services and caught centrally by `GlobalExceptionHandler`:

| Exception | HTTP Status | Log Level |
|-----------|------------|-----------|
| `ResourceNotFoundException` | 404 | `log.warn` |
| `LlmProviderException` | 502 | `log.error` |
| `DocumentProcessingException` | 500 | `log.error` |
| `AuthenticationException` | 401 | `log.warn` |
| `MethodArgumentNotValidException` | 400 | `log.warn` |
| `Exception` (catch-all) | 500 | `log.error` |

Response shape is always `ErrorResponseDto`:
```java
{ "status": 404, "message": "...", "timestamp": "...", "path": "/api/v1/..." }
```

Located: `localmind-backend/localmind-api/src/main/java/com/localmind/api/common/advice/GlobalExceptionHandler.java`

### Frontend

HTTP errors propagate as RxJS stream errors via Angular's `HttpClient`. Feature services do not catch internally — callers handle via `subscribe({ error: () => { ... } })`.

Error interceptor: `localmind-frontend/src/app/core/interceptors/error.interceptor.ts`

---

## Logging (Backend)

Use Lombok `@Slf4j` on any class that needs logging:
```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    log.warn("Resource not found: {}", ex.getMessage());
    log.error("LLM provider error: {}", ex.getMessage(), ex);
}
```

Located: `localmind-backend/localmind-api/src/main/java/com/localmind/api/common/advice/GlobalExceptionHandler.java`

Never use `System.out.println()`. No `console.log` in frontend production code.

---

## Input Validation

**Backend:** Use Bean Validation on DTOs with `@Valid` in controller parameters:
```java
public ResponseEntity<CalendarEventDto> createEvent(@Valid @RequestBody CreateCalendarEventRequest request) { ... }
```

Validation errors are automatically caught by `GlobalExceptionHandler.handleValidation()` → 400.

**Frontend:** Angular reactive forms or template-driven with custom validators. Validator directives in `localmind-frontend/src/app/shared/validators/`:
- `url-validator.directive.ts`
- `json-validator.directive.ts`
- `date-after-validator.directive.ts`

---

## Enum Translation Pattern

Enums intended for UI display carry a `displayName` field (English) and optionally a `local` boolean:
```java
@Getter
public enum LlmProvider {
    OLLAMA("Ollama", true),
    OPENAI("OpenAI", false);

    private final String displayName;
    private final boolean local;
}
```

Located: `localmind-backend/localmind-domain/src/main/java/com/localmind/domain/llm/model/LlmProvider.java`

Frontend must translate enum keys using `TranslationService` (`localmind-frontend/src/app/core/i18n/translation.service.ts`) and the `TranslatePipe`.

---

## Angular Component Pattern

All components are **standalone** (no NgModules):
```typescript
@Component({
  selector: 'app-empty-state',
  standalone: true,          // always present
  imports: [...],
  template: `...`,
  styleUrl: '...'
})
export class EmptyStateComponent { ... }
```

Inputs use the `input()` signal API from Angular 21 (`fixture.componentRef.setInput(...)` in tests).

---

## Frontend Service Pattern

```typescript
@Injectable({ providedIn: 'root' })
export class ChatService {
  constructor(private api: ApiService) {}

  chat(request: ChatRequest): Observable<ChatResponse> {
    return this.api.post<ChatResponse>('/chat', request);
  }
}
```

All services inject `ApiService` (`localmind-frontend/src/app/core/services/api.service.ts`) as the base HTTP wrapper. Never inject `HttpClient` directly in feature services.

---

## File Size Guidelines

- Target: 200–400 lines per file
- Hard max: 800 lines
- Split by domain/feature, not by type
- High cohesion: each file owns one responsibility

---

## Comments

**When to comment:**
- Complex algorithms or non-obvious business rules
- Bilingual for all non-trivial logic: Italian first, then English after `/`
- Cascade order explanations (e.g., deletion order: `// Cascade delete: vector store -> MySQL chunks -> document`)

**No JSDoc/Javadoc** on simple getters, setters, or trivial constructors.

---

*Convention analysis: 2026-06-29*
