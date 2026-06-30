export const content = `# Spring Security Configuration

| | |
|---|---|
| **Document** | Spring Security Configuration |
| **Version** | 0.1.0 |
| **Date** | 2026-02-09 |
| **Project** | LocalMind |

---

## Table of Contents

1. [Overview](#1-overview)
2. [SecurityConfig Class](#2-securityconfig-class)
3. [Current Configuration (v0.1.0)](#3-current-configuration-v010)
   - 3.1 [CSRF](#31-csrf)
   - 3.2 [CORS](#32-cors)
   - 3.3 [Session Management](#33-session-management)
   - 3.4 [Endpoint Authorization](#34-endpoint-authorization)
   - 3.5 [Authentication](#35-authentication)
4. [Security Headers](#4-security-headers)
5. [Detailed CORS Policy](#5-detailed-cors-policy)
6. [Rate Limiting](#6-rate-limiting)
7. [Planned Evolution](#7-planned-evolution)
   - 7.1 [v0.2.0 - Basic Auth](#71-v020---basic-auth)
   - 7.2 [v0.3.0 - JWT Authentication](#72-v030---jwt-authentication)
   - 7.3 [v0.4.0 - RBAC](#73-v040---rbac)
   - 7.4 [v1.0.0 - OAuth2 / OIDC](#74-v100---oauth2--oidc)

---

## 1. Overview

The security configuration of LocalMind is managed through Spring Security 6.x, integrated into the Spring Boot 3.x backend. In the current version (v0.1.0), the configuration adopts a **permissive** approach oriented towards rapid development, with a progressive evolution plan toward a complete security model.

**Reference class:**

\`\`\`
it.localmind.infrastructure.config.SecurityConfig
\`\`\`

**Maven module:**

\`\`\`
localmind-infrastructure
\`\`\`

---

## 2. SecurityConfig Class

The \`SecurityConfig\` class is annotated with \`@Configuration\` and \`@EnableWebSecurity\` and defines the main \`SecurityFilterChain\` of the application.

\`\`\`java
package it.localmind.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth ->
                auth.requestMatchers("/api/v1/**").permitAll()
                    .anyRequest().permitAll()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
\`\`\`

---

## 3. Current Configuration (v0.1.0)

### 3.1 CSRF

\`\`\`java
.csrf(csrf -> csrf.disable())
\`\`\`

**Status:** Disabled.

**Rationale:** LocalMind exclusively exposes stateless REST APIs. REST APIs use tokens (or JWT in the future) for authentication, not session cookies. CSRF protection is relevant for applications that use session cookies for authentication, which does not apply to LocalMind's architectural model.

**Note:** Even in future versions, CSRF protection will remain disabled since the architecture will remain stateless and token-based.

### 3.2 CORS

\`\`\`java
.cors(cors -> cors.configurationSource(corsConfigurationSource()))
\`\`\`

**Status:** Enabled for \`http://localhost:4200\`.

**Rationale:** The Angular frontend, during development, is served by the Angular CLI dev server on port 4200. The Spring Boot backend operates on port 8080. Since these are different origins (different ports), the browser applies the Same-Origin Policy and blocks cross-origin requests without an explicit CORS configuration.

### 3.3 Session Management

\`\`\`java
.sessionManagement(session ->
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
\`\`\`

**Status:** STATELESS.

**Rationale:** No HTTP session is created or maintained by the server. Each request is independent and self-contained. This approach:

- Eliminates the need for server-side session storage.
- Simplifies horizontal scalability (no state to share between instances).
- Aligns with best practices for REST APIs.
- Reduces the attack surface (no session ID to steal or fixate).

### 3.4 Endpoint Authorization

\`\`\`java
.authorizeHttpRequests(auth ->
    auth.requestMatchers("/api/v1/**").permitAll()
        .anyRequest().permitAll()
)
\`\`\`

**Status:** All endpoints are open (\`permitAll\`).

**Rationale:** In v0.1.0, LocalMind operates exclusively in a local environment on \`localhost\`. Since there is no exposure on a public network, authentication is not strictly necessary. This choice enables rapid development without credential management overhead during the initial phases.

**Important:** This configuration is appropriate **exclusively** for local development. Should the system be exposed on a network, it is necessary to enable authentication as described in the [Planned Evolution](#7-planned-evolution) section.

### 3.5 Authentication

**Status:** Disabled.

**Rationale:** No authentication mechanism is configured in v0.1.0. The following are not present:

- \`AuthenticationManager\`
- \`UserDetailsService\`
- \`PasswordEncoder\`
- Custom authentication filters

The absence of authentication is a deliberate choice for the initial development phase, not an oversight. Authentication will be introduced progressively according to the plan described in the [Planned Evolution](#7-planned-evolution) section.

---

## 4. Security Headers

Even in the absence of authentication, Spring Security automatically applies a set of security headers on HTTP responses:

| Header | Value | Function |
|---|---|---|
| \`X-Content-Type-Options\` | \`nosniff\` | Prevents the browser from interpreting the content type differently from what is declared (MIME sniffing prevention) |
| \`X-Frame-Options\` | \`DENY\` | Prevents the page from being included in iframes, protecting against clickjacking attacks |
| \`Cache-Control\` | \`no-cache, no-store, max-age=0, must-revalidate\` | Prevents caching of sensitive responses |
| \`Pragma\` | \`no-cache\` | HTTP/1.0 compatibility for caching prevention |
| \`X-XSS-Protection\` | \`0\` | Disabled (replaced by CSP in modern browser versions) |

### Headers Planned for Future Versions

| Header | Planned Value | Target Version |
|---|---|---|
| \`Strict-Transport-Security\` | \`max-age=31536000; includeSubDomains\` | v0.4.0 (with HTTPS) |
| \`Content-Security-Policy\` | Custom restrictive policy | v0.4.0 |
| \`Referrer-Policy\` | \`strict-origin-when-cross-origin\` | v0.3.0 |
| \`Permissions-Policy\` | Restriction of unnecessary browser APIs | v0.4.0 |

---

## 5. Detailed CORS Policy

The current CORS configuration is defined in the \`corsConfigurationSource()\` method:

### Allowed Origins

\`\`\`java
configuration.setAllowedOrigins(List.of("http://localhost:4200"));
\`\`\`

| Environment | Origins |
|---|---|
| Development | \`http://localhost:4200\` |
| Production (planned) | Configurable via property \`localmind.cors.allowed-origins\` |

### Allowed HTTP Methods

\`\`\`java
configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
\`\`\`

| Method | Usage in LocalMind |
|---|---|
| \`GET\` | Resource retrieval (documents, conversations, configurations) |
| \`POST\` | Resource creation, sending chat messages, starting indexing |
| \`PUT\` | Full resource update |
| \`DELETE\` | Resource deletion |
| \`PATCH\` | Partial resource update |
| \`OPTIONS\` | CORS preflight (handled automatically) |

### Allowed Headers

\`\`\`java
configuration.setAllowedHeaders(List.of("*"));
\`\`\`

All headers are currently allowed. In future versions, the policy will be restricted to only the necessary headers:

- \`Content-Type\`
- \`Authorization\`
- \`Accept\`
- \`X-Requested-With\`

### Credentials

\`\`\`java
configuration.setAllowCredentials(true);
\`\`\`

Credentials (cookies, authorization headers) are allowed in cross-origin requests. This will be necessary when JWT authentication is introduced.

### Preflight Cache

\`\`\`java
configuration.setMaxAge(3600L);
\`\`\`

Preflight responses (\`OPTIONS\`) are cached by the browser for 3600 seconds (1 hour), reducing the number of repeated preflight requests.

---

## 6. Rate Limiting

**Current status:** Not implemented in v0.1.0.

**Rationale:** Operating exclusively on \`localhost\`, rate limiting is not necessary for DoS attack protection.

### Implementation Plan

Rate limiting is planned for v0.3.0 with one of the following strategies:

**Option A: Bucket4j + Spring Boot Starter**

\`\`\`xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
</dependency>
\`\`\`

Configuration via Spring filter:

\`\`\`yaml
localmind:
  rate-limit:
    enabled: true
    requests-per-minute: 60
    burst-capacity: 10
\`\`\`

**Option B: Spring Cloud Gateway**

Applicable if the architecture evolves toward an API Gateway pattern.

### Planned Rate Limits per Endpoint

| Endpoint | Planned Rate Limit | Rationale |
|---|---|---|
| \`/api/v1/chat/**\` | 30 req/min | Each request invokes the LLM (expensive) |
| \`/api/v1/documents/**\` | 60 req/min | Standard CRUD operations |
| \`/api/v1/search/**\` | 30 req/min | Each request queries the vector store |
| \`/api/v1/dashboard/**\` | 120 req/min | Lightweight read operations |

---

## 7. Planned Evolution

### 7.1 v0.2.0 - Basic Auth

**Objective:** Basic protection of API endpoints.

\`\`\`java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .authorizeHttpRequests(auth ->
            auth.requestMatchers("/api/v1/dashboard/health").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().denyAll()
        )
        .httpBasic(Customizer.withDefaults());

    return http.build();
}
\`\`\`

Features:

- HTTP Basic Authentication on all \`/api/v1/**\` endpoints (except health check).
- Credentials configurable via environment variables.
- \`PasswordEncoder\` with BCrypt.
- \`InMemoryUserDetailsManager\` for single user.

### 7.2 v0.3.0 - JWT Authentication

**Objective:** Stateless authentication via JSON Web Token.

Planned features:

- Endpoint \`/api/v1/auth/login\` to obtain the JWT.
- Endpoint \`/api/v1/auth/refresh\` for token refresh.
- JWT signed with asymmetric key (RS256).
- Configurable token expiry (default: 1 hour access, 7 days refresh).
- Custom \`JwtAuthenticationFilter\` in the Spring Security filter chain.
- Token blacklist on logout (stored in MySQL).

### 7.3 v0.4.0 - RBAC

**Objective:** Role-Based Access Control.

Planned roles:

| Role | Permissions |
|---|---|
| \`ADMIN\` | Full access, user management, system configuration |
| \`USER\` | Chat, document search, dashboard viewing |
| \`VIEWER\` | Read-only: viewing documents and conversations |

Implementation:

- \`@PreAuthorize("hasRole('ADMIN')")\` on sensitive endpoints.
- \`users\`, \`roles\`, \`user_roles\` tables in MySQL.
- \`JpaUserDetailsService\` for loading users from the database.

### 7.4 v1.0.0 - OAuth2 / OIDC

**Objective:** Integration with external identity providers.

Planned features:

- Support for OAuth2 Authorization Code flow.
- Integration with OpenID Connect (OIDC).
- Supported providers: Google, GitHub, Keycloak (self-hosted).
- Ability to use local Keycloak to maintain the self-hosted principle.
- Spring Security OAuth2 Client and Resource Server.

\`\`\`yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: localmind
            client-secret: \${KEYCLOAK_CLIENT_SECRET}
            scope: openid, profile, email
        provider:
          keycloak:
            issuer-uri: http://localhost:8180/realms/localmind
\`\`\`
`;
