# Autenticazione Locale

## Panoramica
LocalMind implementa un sistema di autenticazione locale opzionale basato su password. L'autenticazione protegge l'accesso all'interfaccia web e alle API REST.

## Funzionamento

### Setup Iniziale
Al primo accesso, se nessuna password è configurata nel database, l'utente viene reindirizzato automaticamente alla pagina di setup (`/setup`) dove può impostare una password iniziale.

**Flusso**:
1. Frontend chiama `GET /api/v1/auth/status`
2. Backend risponde con `{"passwordConfigured": false}`
3. Frontend reindirizza a `/setup`
4. Utente inserisce password (minimo 8 caratteri)
5. Frontend chiama `POST /api/v1/auth/setup` con `{"password": "..."}`
6. Backend:
   - Genera hash BCrypt (costo 12)
   - Salva in tabella `local_auth` (campo `password_hash`)
   - Genera token bearer
   - Risponde con `{"token": "...", "expiresAt": "..."}`

### Login
- L'utente inserisce la password nella pagina di login (`/login`)
- Il backend verifica la password con BCrypt
- Se corretta, viene generato un token con scadenza 24 ore
- Il token viene salvato nel localStorage del browser (`authToken` key)

**Flusso**:
1. Utente inserisce password in `/login`
2. Frontend chiama `POST /api/v1/auth/login` con `{"password": "..."}`
3. Backend:
   - Recupera `password_hash` da tabella `local_auth`
   - Verifica con `BCryptPasswordEncoder.matches()`
   - Se match:
     - Genera token: `UUID` + firma HMAC-SHA256 con secret
     - Salva token in memoria con scadenza 24h
     - Risponde `{"token": "...", "expiresAt": "2026-02-15T20:30:00Z"}`
   - Se no match: `401 Unauthorized`
4. Frontend salva token in localStorage
5. Ogni richiesta successiva include header `Authorization: Bearer <token>`

### Token
- **Formato**: `UUID-v4` + firma HMAC-SHA256 (es. `a1b2c3d4-...-hmac123abc`)
- **Scadenza**: 24 ore dalla generazione
- **Storage**: In-memory cache (backend) + localStorage (frontend)
- **Trasmissione**: Via header `Authorization: Bearer <token>`
- **Validazione**: `LocalAuthFilter` intercetta ogni richiesta API e verifica:
  1. Token presente nel header
  2. Token valido in cache
  3. Token non scaduto

### Endpoint Pubblici
I seguenti endpoint **non richiedono** autenticazione (whitelist in `LocalAuthFilter`):

**API**:
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/setup`
- `GET /api/v1/auth/status`
- `GET /api/v1/dashboard/health`

**Monitoring & Docs**:
- `/actuator/**` (Spring Boot Actuator)
- `/swagger-ui/**`, `/v3/api-docs/**` (Swagger/OpenAPI)

Tutti gli altri endpoint richiedono token valido, altrimenti risposta `401 Unauthorized`.

## Rate Limiting
Per prevenire attacchi brute force sul login, è implementato un rate limiter:

- **Limite**: 100 richieste per minuto per IP (configurabile)
- **Risposta**: 429 (Too Many Requests) quando superato
- **Header**: `X-RateLimit-Remaining` nelle risposte (es. `95`)
- **Implementazione**: Token Bucket in-memory con Caffeine cache
- **Reset**: Automatico dopo 60 secondi

**Esempio**:
```http
POST /api/v1/auth/login
X-Forwarded-For: 192.168.1.100

# Risposta dopo 101 richieste in 1 minuto:
HTTP/1.1 429 Too Many Requests
X-RateLimit-Remaining: 0
Content-Type: application/json

{
  "error": "Too many requests. Please try again later."
}
```

## Configurazione
Variabili in `.env`:

```env
# Rate Limiting
LOCALMIND_SECURITY_RATE_LIMIT_MAX_REQUESTS=100
LOCALMIND_SECURITY_RATE_LIMIT_WINDOW_SECONDS=60

# Token JWT-like
LOCALMIND_SECURITY_JWT_SECRET=your-secret-key-change-in-production
LOCALMIND_SECURITY_JWT_EXPIRATION_HOURS=24
```

**IMPORTANTE**: Cambiare `JWT_SECRET` in produzione con un valore casuale di almeno 32 caratteri.

## Architettura

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
    public String login(String password); // Genera token
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
        authService.validateToken(token); // Lancia AuthenticationException se invalido
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

## Sicurezza

### Hash Password
- **Algoritmo**: BCrypt
- **Costo**: 12 (2^12 = 4096 iterazioni)
- **Salt**: Generato automaticamente da BCrypt
- **Output**: 60 caratteri (es. `$2a$12$...`)

### Token
- **Generazione**: `UUID.randomUUID()` + HMAC-SHA256
- **Secret**: Configurabile via `LOCALMIND_SECURITY_JWT_SECRET`
- **Scadenza**: 24 ore (non rinnovabile, richiede re-login)
- **Storage backend**: In-memory (Caffeine cache, perso al restart)
- **Storage frontend**: localStorage (vulnerabile a XSS, ma Angular sanitizza)

### Protezioni
✅ **Implementate**:
- Hash BCrypt per password
- Rate limiting per prevenire brute force
- Token con scadenza 24h
- CORS configurato
- Header X-Correlation-Id per tracciamento

❌ **Non implementate** (da considerare in futuro):
- Refresh token (richiede re-login ogni 24h)
- Multi-factor authentication (MFA)
- Password reset via email (no email configurata)
- Audit log per login/logout
- IP whitelisting

## Testing

### Unit Test Backend
```bash
cd localmind-backend
mvn test -Dtest=LocalAuthServiceTest
mvn test -Dtest=LocalAuthFilterTest
mvn test -Dtest=AuthControllerTest
```

### E2E Test Frontend
```bash
cd localmind-frontend
npm run test:e2e -- auth.spec.ts
```

Test coverage:
- Setup password con validazione
- Login con credenziali valide/invalide
- Token expired (mock)
- Rate limiting (mock)
- Redirect a /login se non autenticato

## Troubleshooting

### Errore: "Password non configurata"
**Sintomo**: Backend risponde `401` su tutti gli endpoint.

**Causa**: Nessuna password impostata nel database.

**Soluzione**:
1. Naviga a `http://localhost:4200/setup`
2. Imposta una password (minimo 8 caratteri)

### Errore: "Token expired"
**Sintomo**: Frontend reindirizza a `/login` dopo 24 ore.

**Causa**: Token scaduto.

**Soluzione**: Effettua nuovamente il login.

### Errore: "Too many requests"
**Sintomo**: Backend risponde `429` su `/login`.

**Causa**: Rate limit superato (100 req/min).

**Soluzione**: Attendi 60 secondi, poi riprova.

### Errore: "Invalid token"
**Sintomo**: Backend risponde `401` anche con token presente.

**Causa**: Backend riavviato (token in-memory persi).

**Soluzione**: Effettua nuovamente il login.

## Riferimenti
- [BCrypt](https://en.wikipedia.org/wiki/Bcrypt)
- [Token Bucket Algorithm](https://en.wikipedia.org/wiki/Token_bucket)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
