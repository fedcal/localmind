export const content = `# Configurazione Spring Security

|               |                                |
|---------------|--------------------------------|
| **Documento** | Configurazione Spring Security |
| **Versione**  | 0.1.0                          |
| **Data**      | 2026-02-09                     |
| **Progetto**  | LocalMind                      |

---

## Indice

- [Configurazione Spring Security](#configurazione-spring-security)
  - [Indice](#indice)
  - [1. Panoramica](#1-panoramica)
  - [2. SecurityConfig Class](#2-securityconfig-class)
  - [3. Configurazione Attuale (v0.1.0)](#3-configurazione-attuale-v010)
    - [3.1 CSRF](#31-csrf)
    - [3.2 CORS](#32-cors)
    - [3.3 Session Management](#33-session-management)
    - [3.4 Autorizzazione Endpoint](#34-autorizzazione-endpoint)
    - [3.5 Autenticazione](#35-autenticazione)
  - [4. Headers di Sicurezza](#4-headers-di-sicurezza)
    - [Headers pianificati per versioni future](#headers-pianificati-per-versioni-future)
  - [5. CORS Policy Dettagliata](#5-cors-policy-dettagliata)
    - [Origini consentite](#origini-consentite)
    - [Metodi HTTP consentiti](#metodi-http-consentiti)
    - [Headers consentiti](#headers-consentiti)
    - [Credenziali](#credenziali)
    - [Cache Preflight](#cache-preflight)
  - [6. Rate Limiting](#6-rate-limiting)
    - [Piano di implementazione](#piano-di-implementazione)
    - [Limiti pianificati per endpoint](#limiti-pianificati-per-endpoint)
  - [7. Evoluzione Pianificata](#7-evoluzione-pianificata)
    - [7.1 v0.2.0 - Basic Auth](#71-v020---basic-auth)
    - [7.2 v0.3.0 - JWT Authentication](#72-v030---jwt-authentication)
    - [7.3 v0.4.0 - RBAC](#73-v040---rbac)
    - [7.4 v1.0.0 - OAuth2 / OIDC](#74-v100---oauth2--oidc)

---

## 1. Panoramica

La configurazione di sicurezza di LocalMind e' gestita tramite Spring Security 6.x, integrato nel backend Spring Boot 3.x. Nella versione corrente (v0.1.0), la configurazione adotta un approccio **permissivo** orientato allo sviluppo rapido, con un piano di evoluzione progressiva verso un modello di sicurezza completo.

**Classe di riferimento:**

\`\`\`
it.localmind.infrastructure.config.SecurityConfig
\`\`\`

**Modulo Maven:**

\`\`\`
localmind-infrastructure
\`\`\`

---

## 2. SecurityConfig Class

La classe \`SecurityConfig\` e' annotata con \`@Configuration\` e \`@EnableWebSecurity\` e definisce il \`SecurityFilterChain\` principale dell'applicazione.

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

## 3. Configurazione Attuale (v0.1.0)

### 3.1 CSRF

\`\`\`java
.csrf(csrf -> csrf.disable())
\`\`\`

**Stato:** Disabilitato.

**Motivazione:** LocalMind espone esclusivamente API REST stateless. Le API REST utilizzano token (o in futuro JWT) per l'autenticazione, non cookie di sessione. La protezione CSRF e' rilevante per applicazioni che utilizzano cookie di sessione per l'autenticazione, il che non si applica al modello architetturale di LocalMind.

**Nota:** Anche nelle versioni future, la protezione CSRF rimarra' disabilitata poiche' l'architettura restera' stateless e token-based.

### 3.2 CORS

\`\`\`java
.cors(cors -> cors.configurationSource(corsConfigurationSource()))
\`\`\`

**Stato:** Abilitato per \`http://localhost:4200\`.

**Motivazione:** Il frontend Angular, durante lo sviluppo, viene servito dal dev server Angular CLI sulla porta 4200. Il backend Spring Boot opera sulla porta 8080. Essendo origini diverse (porte differenti), il browser applica la Same-Origin Policy e blocca le richieste cross-origin senza una configurazione CORS esplicita.

### 3.3 Session Management

\`\`\`java
.sessionManagement(session ->
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
\`\`\`

**Stato:** STATELESS.

**Motivazione:** Nessuna sessione HTTP viene creata o mantenuta dal server. Ogni richiesta e' indipendente e auto-contenuta. Questo approccio:

- Elimina la necessita' di session storage lato server.
- Semplifica la scalabilita' orizzontale (nessuno stato da condividere tra istanze).
- Si allinea con le best practice per API REST.
- Riduce la superficie d'attacco (nessun session ID da rubare o fissare).

### 3.4 Autorizzazione Endpoint

\`\`\`java
.authorizeHttpRequests(auth ->
    auth.requestMatchers("/api/v1/**").permitAll()
        .anyRequest().permitAll()
)
\`\`\`

**Stato:** Tutti gli endpoint sono aperti (\`permitAll\`).

**Motivazione:** Nella v0.1.0, LocalMind opera esclusivamente in ambiente locale su \`localhost\`. Non essendoci esposizione su rete pubblica, l'autenticazione non e' strettamente necessaria. Questa scelta consente uno sviluppo rapido senza overhead di gestione credenziali durante le fasi iniziali.

**Importante:** Questa configurazione e' appropriata **esclusivamente** per lo sviluppo locale. Qualora il sistema venisse esposto su rete, e' necessario abilitare l'autenticazione come descritto nella sezione [Evoluzione Pianificata](#7-evoluzione-pianificata).

### 3.5 Autenticazione

**Stato:** Disabilitata.

**Motivazione:** Nessun meccanismo di autenticazione e' configurato nella v0.1.0. Non sono presenti:

- \`AuthenticationManager\`
- \`UserDetailsService\`
- \`PasswordEncoder\`
- Filtri di autenticazione personalizzati

L'assenza di autenticazione e' una scelta deliberata per la fase di sviluppo iniziale, non una dimenticanza. L'autenticazione verra' introdotta progressivamente secondo il piano descritto nella sezione [Evoluzione Pianificata](#7-evoluzione-pianificata).

---

## 4. Headers di Sicurezza

Anche in assenza di autenticazione, Spring Security applica automaticamente una serie di headers di sicurezza sulle risposte HTTP:

| Header | Valore | Funzione |
|---|---|---|
| \`X-Content-Type-Options\` | \`nosniff\` | Impedisce al browser di interpretare il content type in modo diverso da quello dichiarato (MIME sniffing prevention) |
| \`X-Frame-Options\` | \`DENY\` | Impedisce l'inclusione della pagina in iframe, proteggendo da attacchi clickjacking |
| \`Cache-Control\` | \`no-cache, no-store, max-age=0, must-revalidate\` | Previene il caching di risposte sensibili |
| \`Pragma\` | \`no-cache\` | Compatibilita' HTTP/1.0 per prevenzione caching |
| \`X-XSS-Protection\` | \`0\` | Disabilitato (sostituito da CSP nelle versioni moderne dei browser) |

### Headers pianificati per versioni future

| Header | Valore pianificato | Versione target |
|---|---|---|
| \`Strict-Transport-Security\` | \`max-age=31536000; includeSubDomains\` | v0.4.0 (con HTTPS) |
| \`Content-Security-Policy\` | Policy restrittiva personalizzata | v0.4.0 |
| \`Referrer-Policy\` | \`strict-origin-when-cross-origin\` | v0.3.0 |
| \`Permissions-Policy\` | Restrizione API browser non necessarie | v0.4.0 |

---

## 5. CORS Policy Dettagliata

La configurazione CORS corrente e' definita nel metodo \`corsConfigurationSource()\`:

### Origini consentite

\`\`\`java
configuration.setAllowedOrigins(List.of("http://localhost:4200"));
\`\`\`

| Ambiente | Origini |
|---|---|
| Sviluppo | \`http://localhost:4200\` |
| Produzione (pianificato) | Configurabile via property \`localmind.cors.allowed-origins\` |

### Metodi HTTP consentiti

\`\`\`java
configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
\`\`\`

| Metodo | Utilizzo in LocalMind |
|---|---|
| \`GET\` | Recupero risorse (documenti, conversazioni, configurazioni) |
| \`POST\` | Creazione risorse, invio messaggi chat, avvio indicizzazione |
| \`PUT\` | Aggiornamento completo risorse |
| \`DELETE\` | Eliminazione risorse |
| \`PATCH\` | Aggiornamento parziale risorse |
| \`OPTIONS\` | Preflight CORS (gestito automaticamente) |

### Headers consentiti

\`\`\`java
configuration.setAllowedHeaders(List.of("*"));
\`\`\`

Tutti gli headers sono attualmente consentiti. In versioni future, la policy sara' ristretta ai soli headers necessari:

- \`Content-Type\`
- \`Authorization\`
- \`Accept\`
- \`X-Requested-With\`

### Credenziali

\`\`\`java
configuration.setAllowCredentials(true);
\`\`\`

Le credenziali (cookie, authorization headers) sono consentite nelle richieste cross-origin. Questo sara' necessario quando verra' introdotta l'autenticazione JWT.

### Cache Preflight

\`\`\`java
configuration.setMaxAge(3600L);
\`\`\`

Le risposte preflight (\`OPTIONS\`) vengono cachate dal browser per 3600 secondi (1 ora), riducendo il numero di richieste preflight ripetute.

---

## 6. Rate Limiting

**Stato attuale:** Non implementato nella v0.1.0.

**Motivazione:** Operando esclusivamente su \`localhost\`, il rate limiting non e' necessario per la protezione da attacchi DoS.

### Piano di implementazione

Il rate limiting e' pianificato per la v0.3.0 con una delle seguenti strategie:

**Opzione A: Bucket4j + Spring Boot Starter**

\`\`\`xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
</dependency>
\`\`\`

Configurazione tramite filtro Spring:

\`\`\`yaml
localmind:
  rate-limit:
    enabled: true
    requests-per-minute: 60
    burst-capacity: 10
\`\`\`

**Opzione B: Spring Cloud Gateway**

Applicabile se l'architettura evolvera' verso un API Gateway pattern.

### Limiti pianificati per endpoint

| Endpoint | Rate limit pianificato | Motivazione |
|---|---|---|
| \`/api/v1/chat/**\` | 30 req/min | Ogni richiesta invoca il LLM (costoso) |
| \`/api/v1/documents/**\` | 60 req/min | Operazioni CRUD standard |
| \`/api/v1/search/**\` | 30 req/min | Ogni richiesta interroga il vector store |
| \`/api/v1/dashboard/**\` | 120 req/min | Operazioni leggere di lettura |

---

## 7. Evoluzione Pianificata

### 7.1 v0.2.0 - Basic Auth

**Obiettivo:** Protezione base degli endpoint API.

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

Caratteristiche:

- HTTP Basic Authentication su tutti gli endpoint \`/api/v1/**\` (tranne health check).
- Credenziali configurabili via variabili d'ambiente.
- \`PasswordEncoder\` con BCrypt.
- \`InMemoryUserDetailsManager\` per utente singolo.

### 7.2 v0.3.0 - JWT Authentication

**Obiettivo:** Autenticazione stateless tramite JSON Web Token.

Caratteristiche pianificate:

- Endpoint \`/api/v1/auth/login\` per ottenere il JWT.
- Endpoint \`/api/v1/auth/refresh\` per il refresh del token.
- JWT firmato con chiave asimmetrica (RS256).
- Token expiry configurabile (default: 1 ora access, 7 giorni refresh).
- \`JwtAuthenticationFilter\` custom nel filter chain di Spring Security.
- Blacklist token su logout (memorizzata in MySQL).

### 7.3 v0.4.0 - RBAC

**Obiettivo:** Controllo accessi basato su ruoli.

Ruoli pianificati:

| Ruolo | Permessi |
|---|---|
| \`ADMIN\` | Accesso completo, gestione utenti, configurazione sistema |
| \`USER\` | Chat, ricerca documenti, visualizzazione dashboard |
| \`VIEWER\` | Solo lettura: visualizzazione documenti e conversazioni |

Implementazione:

- \`@PreAuthorize("hasRole('ADMIN')")\` su endpoint sensibili.
- Tabelle \`users\`, \`roles\`, \`user_roles\` in MySQL.
- \`JpaUserDetailsService\` per caricamento utenti dal database.

### 7.4 v1.0.0 - OAuth2 / OIDC

**Obiettivo:** Integrazione con identity provider esterni.

Caratteristiche pianificate:

- Support per OAuth2 Authorization Code flow.
- Integrazione con OpenID Connect (OIDC).
- Provider supportati: Google, GitHub, Keycloak (self-hosted).
- Possibilita' di utilizzare Keycloak locale per mantenere il principio self-hosted.
- Spring Security OAuth2 Client e Resource Server.

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
