# Local Authentication

## Overview
LocalMind implements an optional local password-based authentication system. Authentication protects access to the web interface and REST APIs.

## How It Works

### Initial Setup
On first access, if no password is configured in the database, the user is automatically redirected to the setup page (`/setup`) where they can set an initial password.

**Flow**:
1. Frontend calls `GET /api/v1/auth/status`
2. Backend responds with `{"passwordConfigured": false}`
3. Frontend redirects to `/setup`
4. User enters password (minimum 8 characters)
5. Frontend calls `POST /api/v1/auth/setup` with `{"password": "..."}`
6. Backend:
   - Generates BCrypt hash (cost 12)
   - Saves to `local_auth` table (`password_hash` field)
   - Generates bearer token
   - Responds with `{"token": "...", "expiresAt": "..."}`

### Login
- The user enters the password on the login page (`/login`)
- The backend verifies the password with BCrypt
- If correct, a token with 24-hour expiry is generated
- The token is saved in the browser's localStorage (`authToken` key)

**Flow**:
1. User enters password on `/login`
2. Frontend calls `POST /api/v1/auth/login` with `{"password": "..."}`
3. Backend:
   - Retrieves `password_hash` from `local_auth` table
   - Verifies with `BCryptPasswordEncoder.matches()`
   - If match:
     - Generates token: `UUID` + HMAC-SHA256 signature with secret
     - Saves token in-memory with 24h expiry
     - Responds `{"token": "...", "expiresAt": "2026-02-15T20:30:00Z"}`
   - If no match: `401 Unauthorized`
4. Frontend saves token in localStorage
5. Every subsequent request includes `Authorization: Bearer <token>` header

### Token
- **Format**: `UUID-v4` + HMAC-SHA256 signature (e.g., `a1b2c3d4-...-hmac123abc`)
- **Expiry**: 24 hours from generation
- **Storage**: In-memory cache (backend) + localStorage (frontend)
- **Transmission**: Via `Authorization: Bearer <token>` header
- **Validation**: `LocalAuthFilter` intercepts every API request and verifies:
  1. Token present in header
  2. Token valid in cache
  3. Token not expired

### Public Endpoints
The following endpoints **do not require** authentication (whitelist in `LocalAuthFilter`):

**API**:
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/setup`
- `GET /api/v1/auth/status`
- `GET /api/v1/dashboard/health`

**Monitoring & Docs**:
- `/actuator/**` (Spring Boot Actuator)
- `/swagger-ui/**`, `/v3/api-docs/**` (Swagger/OpenAPI)

All other endpoints require a valid token, otherwise return `401 Unauthorized`.

## Rate Limiting
To prevent brute force attacks on login, a rate limiter is implemented:

- **Limit**: 100 requests per minute per IP (configurable)
- **Response**: 429 (Too Many Requests) when exceeded
- **Header**: `X-RateLimit-Remaining` in responses (e.g., `95`)
- **Implementation**: Token Bucket in-memory with Caffeine cache
- **Reset**: Automatic after 60 seconds

**Example**:
```http
POST /api/v1/auth/login
X-Forwarded-For: 192.168.1.100

# Response after 101 requests in 1 minute:
HTTP/1.1 429 Too Many Requests
X-RateLimit-Remaining: 0
Content-Type: application/json

{
  "error": "Too many requests. Please try again later."
}
```

## Configuration
Variables in `.env`:

```env
# Rate Limiting
LOCALMIND_SECURITY_RATE_LIMIT_MAX_REQUESTS=100
LOCALMIND_SECURITY_RATE_LIMIT_WINDOW_SECONDS=60

# JWT-like Token
LOCALMIND_SECURITY_JWT_SECRET=your-secret-key-change-in-production
LOCALMIND_SECURITY_JWT_EXPIRATION_HOURS=24
```

**IMPORTANT**: Change `JWT_SECRET` in production to a random value of at least 32 characters.

## Architecture

### Backend — Domain Layer
**Entity**:
```java
// com.localmind.domain.security.model.LocalAuth
public class LocalAuth {
    private UUID id;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Service**:
```java
// com.localmind.domain.security.service.LocalAuthService
public class LocalAuthService implements LocalAuthUseCase {
    public void setupPassword(String password); // BCrypt hash
    public String login(String password); // Generate token
    public boolean isPasswordConfigured();
    public void validateToken(String token);
}
```

### Backend — Infrastructure Layer
**Filter**:
```java
// com.localmind.infrastructure.security.filter.LocalAuthFilter
public class LocalAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        String token = extractToken(request);
        authService.validateToken(token); // Throws AuthenticationException if invalid
        filterChain.doFilter(request, response);
    }
}
```

**Adapter**:
```java
// com.localmind.infrastructure.security.adapter.BCryptPasswordHashAdapter
public class BCryptPasswordHashAdapter implements PasswordHashPort {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public String hash(String password) { return encoder.encode(password); }
    public boolean matches(String raw, String hash) { return encoder.matches(raw, hash); }
}
```

### Backend — API Layer
**Controller**:
```java
// com.localmind.api.security.controller.AuthController
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @PostMapping("/setup")
    public AuthResponseDto setup(@Valid @RequestBody SetupRequestDto request);

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request);

    @GetMapping("/status")
    public AuthStatusDto status();
}
```

### Frontend — Services
```typescript
// src/app/core/services/auth.service.ts
@Injectable({ providedIn: 'root' })
export class AuthService {
  login(password: string): Observable<AuthResponse>;
  setup(password: string): Observable<AuthResponse>;
  logout(): void;
  isAuthenticated(): boolean;
  getToken(): string | null;
}
```

### Frontend — Guards
```typescript
// src/app/core/guards/auth.guard.ts
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  if (authService.isAuthenticated()) return true;

  const router = inject(Router);
  router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
  return false;
};
```

## Security

### Password Hashing
- **Algorithm**: BCrypt
- **Cost**: 12 (2^12 = 4096 iterations)
- **Salt**: Automatically generated by BCrypt
- **Output**: 60 characters (e.g., `$2a$12$...`)

### Token
- **Generation**: `UUID.randomUUID()` + HMAC-SHA256
- **Secret**: Configurable via `LOCALMIND_SECURITY_JWT_SECRET`
- **Expiry**: 24 hours (non-renewable, requires re-login)
- **Backend storage**: In-memory (Caffeine cache, lost on restart)
- **Frontend storage**: localStorage (vulnerable to XSS, but Angular sanitizes)

### Protections
✅ **Implemented**:
- BCrypt password hashing
- Rate limiting to prevent brute force
- Token with 24h expiry
- CORS configured
- X-Correlation-Id header for tracing

❌ **Not implemented** (consider for future):
- Refresh token (requires re-login every 24h)
- Multi-factor authentication (MFA)
- Password reset via email (no email configured)
- Audit log for login/logout
- IP whitelisting

## Testing

### Backend Unit Tests
```bash
cd localmind-backend
mvn test -Dtest=LocalAuthServiceTest
mvn test -Dtest=LocalAuthFilterTest
mvn test -Dtest=AuthControllerTest
```

### Frontend E2E Tests
```bash
cd localmind-frontend
npm run test:e2e -- auth.spec.ts
```

Test coverage:
- Setup password with validation
- Login with valid/invalid credentials
- Token expired (mock)
- Rate limiting (mock)
- Redirect to /login if unauthenticated

## Troubleshooting

### Error: "Password not configured"
**Symptom**: Backend returns `401` on all endpoints.

**Cause**: No password set in database.

**Solution**:
1. Navigate to `http://localhost:4200/setup`
2. Set a password (minimum 8 characters)

### Error: "Token expired"
**Symptom**: Frontend redirects to `/login` after 24 hours.

**Cause**: Token expired.

**Solution**: Login again.

### Error: "Too many requests"
**Symptom**: Backend returns `429` on `/login`.

**Cause**: Rate limit exceeded (100 req/min).

**Solution**: Wait 60 seconds, then retry.

### Error: "Invalid token"
**Symptom**: Backend returns `401` even with token present.

**Cause**: Backend restarted (in-memory tokens lost).

**Solution**: Login again.

## References
- [BCrypt](https://en.wikipedia.org/wiki/Bcrypt)
- [Token Bucket Algorithm](https://en.wikipedia.org/wiki/Token_bucket)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
