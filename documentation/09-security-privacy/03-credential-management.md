# Credential Management

| | |
|---|---|
| **Document** | Credential Management |
| **Version** | 0.1.0 |
| **Date** | 2026-02-09 |
| **Project** | LocalMind |

---

## Table of Contents

1. [Overview](#1-overview)
2. [API Key Management](#2-api-key-management)
   - 2.1 [Management Strategy](#21-management-strategy)
   - 2.2 [.env and .env.example Files](#22-env-and-envexample-files)
   - 2.3 [Secrets Management (Production)](#23-secrets-management-production)
3. [Defined Environment Variables](#3-defined-environment-variables)
4. [Spring Boot Property Resolution](#4-spring-boot-property-resolution)
5. [Conditional Provider Enablement](#5-conditional-provider-enablement)
6. [Best Practices](#6-best-practices)
7. [API Key Encryption](#7-api-key-encryption)

---

## 1. Overview

LocalMind manages several types of credentials necessary for the system to function:

| Type | Mandatory | Example |
|---|---|---|
| Database credentials | Mandatory | MySQL username/password |
| LLM provider API keys | Optional | OpenAI, Anthropic, Google |
| Internal service credentials | Mandatory | n8n basic auth |

The guiding principle in credential management is the **separation of code and configuration**: no credentials should be present in the source code or the Git repository. All credentials are managed through environment variables, loaded from the `.env` file during development and through secure mechanisms (Docker secrets, operating system environment variables) in production.

---

## 2. API Key Management

### 2.1 Management Strategy

The API keys for cloud LLM providers (OpenAI, Anthropic, Google) follow a three-level management strategy:

| Level | Mechanism | Environment |
|---|---|---|
| 1. System environment variables | `export OPENAI_API_KEY=sk-...` | Production |
| 2. `.env` file | `OPENAI_API_KEY=sk-...` | Local development |
| 3. Docker secrets | `/run/secrets/openai_api_key` | Docker Swarm / Kubernetes |

The resolution priority is:

1. Operating system environment variable (highest priority).
2. Variable defined in the `.env` file (loaded by Spring Boot).
3. Default value defined in `application.yml` (typically empty).

### 2.2 .env and .env.example Files

**`.env` file** (gitignored, present only locally):

```env
# ==================================================
# LocalMind - Environment Variables
# ==================================================

# MySQL
DB_USERNAME=localmind
DB_PASSWORD=localmind_secret_password

# LLM Providers (optional - leave empty for local-only mode)
OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
ANTHROPIC_API_KEY=sk-ant-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
GOOGLE_API_KEY=AIzaXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

# n8n
N8N_BASIC_AUTH_USER=admin
N8N_BASIC_AUTH_PASSWORD=n8n_secret_password
```

**`.env.example` file** (committed to the repository, without real values):

```env
# ==================================================
# LocalMind - Environment Variables Template
# ==================================================
# Copy this file to .env and fill in with your values:
#   cp .env.example .env
#
# WARNING: Never commit the .env file to the repository!
# ==================================================

# MySQL
DB_USERNAME=localmind
DB_PASSWORD=changeme

# LLM Providers (optional - leave empty for local-only mode with Ollama)
OPENAI_API_KEY=
ANTHROPIC_API_KEY=
GOOGLE_API_KEY=

# n8n
N8N_BASIC_AUTH_USER=admin
N8N_BASIC_AUTH_PASSWORD=changeme
```

**`.gitignore` rule:**

```gitignore
# Environment variables
.env
.env.local
.env.*.local
!.env.example
```

### 2.3 Secrets Management (Production)

For production environments, credentials can be managed through operating system environment variables or through a secrets manager:

```bash
# System environment variables (Linux)
export DB_PASSWORD=my_secret_password
export OPENAI_API_KEY=sk-...

# Or via systemd environment file
# /etc/localmind/env
DB_PASSWORD=my_secret_password
OPENAI_API_KEY=sk-...
```

For Kubernetes deployments, credentials can be managed through Kubernetes Secrets:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: localmind-secrets
type: Opaque
data:
  db-password: <base64-encoded>
  openai-api-key: <base64-encoded>
```

---

## 3. Defined Environment Variables

The following table lists all environment variables recognized by LocalMind in v0.1.0:

| Variable | Mandatory | Default | Description |
|---|---|---|---|
| `DB_USERNAME` | Yes | `localmind` | Username for MySQL connection |
| `DB_PASSWORD` | Yes | `localmind` | Password for MySQL connection |
| `OPENAI_API_KEY` | No | *(empty)* | API key for the OpenAI provider |
| `ANTHROPIC_API_KEY` | No | *(empty)* | API key for the Anthropic provider |
| `GOOGLE_API_KEY` | No | *(empty)* | API key for the Google AI provider |
| `N8N_BASIC_AUTH_USER` | Yes | `admin` | Username for the n8n web interface |
| `N8N_BASIC_AUTH_PASSWORD` | Yes | `admin` | Password for the n8n web interface |

### Variables Planned for Future Versions

| Variable | Target Version | Description |
|---|---|---|
| `JWT_SECRET_KEY` | v0.3.0 | Secret key for JWT signing |
| `JWT_EXPIRATION_MS` | v0.3.0 | JWT token duration in milliseconds |
| `ENCRYPTION_KEY` | v0.3.0 | Key for encrypting API keys in the database |
| `KEYCLOAK_CLIENT_SECRET` | v1.0.0 | Client secret for Keycloak integration |

---

## 4. Spring Boot Property Resolution

Spring Boot resolves environment variables within YAML configuration files using the `${VARIABLE:default}` syntax:

**application.yml:**

```yaml
spring:
  datasource:
    username: ${DB_USERNAME:localmind}
    password: ${DB_PASSWORD:localmind}

localmind:
  llm:
    openai:
      api-key: ${OPENAI_API_KEY:}
      enabled: false
    anthropic:
      api-key: ${ANTHROPIC_API_KEY:}
      enabled: false
    google:
      api-key: ${GOOGLE_API_KEY:}
      enabled: false
```

### Resolution Mechanism

1. Spring Boot looks for the `OPENAI_API_KEY` environment variable in the system.
2. If present, it uses the found value.
3. If absent, it uses the default value after the colon (`:`).
4. If the default is empty (`${OPENAI_API_KEY:}`), the property results in an empty string.

**Resolution example:**

```
# With environment variable set:
export OPENAI_API_KEY=sk-abc123
# Result: localmind.llm.openai.api-key = "sk-abc123"

# Without environment variable:
# Result: localmind.llm.openai.api-key = ""
```

### application-prod.yml

The production profile overrides default settings:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/localmind
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

**Note:** In the production profile, the `DB_USERNAME` and `DB_PASSWORD` environment variables are **mandatory** (no default value). The application will not start if they are not defined.

---

## 5. Conditional Provider Enablement

Cloud LLM providers are managed through a conditional enablement mechanism based on Spring Boot's `@ConditionalOnProperty`:

### Configuration

```yaml
# application.yml
localmind:
  llm:
    openai:
      enabled: false    # Disabled by default
      api-key: ${OPENAI_API_KEY:}
      model: gpt-4
    anthropic:
      enabled: false    # Disabled by default
      api-key: ${ANTHROPIC_API_KEY:}
      model: claude-sonnet-4-20250514
    google:
      enabled: false    # Disabled by default
      api-key: ${GOOGLE_API_KEY:}
      model: gemini-pro
    ollama:
      enabled: true     # Enabled by default
      base-url: http://localhost:11434
      model: llama3.2
```

### Adapter Implementation

```java
@Component
@ConditionalOnProperty(
    name = "localmind.llm.openai.enabled",
    havingValue = "true"
)
public class OpenAiLlmAdapter implements LlmPort {

    private final String apiKey;

    public OpenAiLlmAdapter(
            @Value("${localmind.llm.openai.api-key}") String apiKey) {
        this.apiKey = apiKey;
        // Validation: if the key is empty, the bean should not be created
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "OpenAI API key is required when openai provider is enabled. " +
                "Set OPENAI_API_KEY environment variable."
            );
        }
    }

    // ... LlmPort method implementations
}
```

### Behavior

| `enabled` | `api-key` | Result |
|---|---|---|
| `false` | *(any)* | Bean **not created**. Provider not available. |
| `true` | *(empty)* | **Startup error.** `IllegalStateException` with explanatory message. |
| `true` | *(valid)* | Bean created. Provider available in `LlmGatewayService`. |

This mechanism ensures that:

1. Cloud providers are never accidentally activated.
2. Enablement requires an explicit user action (`enabled: true` + API key).
3. Inconsistent configurations (provider enabled without API key) are caught immediately at startup.

---

## 6. Best Practices

### 6.1 Never Commit Credentials to the Repository

```bash
# Verify that .env is in .gitignore
grep -q "^\.env$" .gitignore || echo ".env" >> .gitignore

# Verify that there are no credentials in tracked files
git log --all --full-history -S "sk-" -- "*.yml" "*.yaml" "*.properties" "*.java"
```

**Golden rule:** If a file contains real credentials, it must **never** be tracked by Git.

### 6.2 Use .gitignore for Sensitive Files

```gitignore
# Credentials and sensitive configurations
.env
.env.local
.env.*.local
*.pem
*.key
*.p12
*.jks

# IDE with potentially sensitive configurations
.idea/
.vscode/settings.json
```

### 6.3 Rotate API Keys Periodically

| Provider | Recommended Frequency | Procedure |
|---|---|---|
| OpenAI | Every 90 days | OpenAI Dashboard -> API Keys -> Create new secret key |
| Anthropic | Every 90 days | Anthropic Console -> API Keys -> Generate Key |
| Google | Every 90 days | Google Cloud Console -> Credentials -> Create credentials |

**Rotation procedure:**

1. Generate a new API key from the provider's dashboard.
2. Update the local `.env` file with the new key.
3. Restart the Spring Boot application.
4. Verify operation with a test request.
5. Revoke the old API key from the provider's dashboard.

### 6.4 Use API Keys with Minimum Permissions

| Provider | Recommended Permission | Rationale |
|---|---|---|
| OpenAI | Chat Completions only | LocalMind does not need access to DALL-E, Whisper, etc. |
| Anthropic | Messages API only | Access limited to the conversation API only |
| Google | Gemini API only | Limit access to only the features being used |

### 6.5 Monitor API Key Usage

Periodically check provider dashboards for:

- Anomalous usage (unexpected request spikes).
- Unexpected costs.
- Requests from unrecognized IPs (if the provider allows it).

---

## 7. API Key Encryption

**Current status:** Not implemented in v0.1.0. API keys are stored in plain text in environment variables.

**Plan for v0.3.0:** Implementation of encryption for API keys stored in the database through one of the following solutions:

### Option A: Jasypt (Java Simplified Encryption)

```xml
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
```

**Usage in application.yml:**

```yaml
localmind:
  llm:
    openai:
      api-key: ENC(encrypted_base64_here)
```

**Encryption:**

```bash
java -cp jasypt.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
  input="sk-my-api-key" \
  password="master-password" \
  algorithm=PBEWithMD5AndTripleDES
```

The master password for decryption is provided via environment variable:

```bash
export JASYPT_ENCRYPTOR_PASSWORD=master-password
```

### Option B: Spring Vault

For environments that require centralized secrets management:

```xml
<dependency>
    <groupId>org.springframework.vault</groupId>
    <artifactId>spring-vault-core</artifactId>
</dependency>
```

**Configuration:**

```yaml
spring:
  cloud:
    vault:
      uri: http://localhost:8200
      token: ${VAULT_TOKEN}
      kv:
        backend: secret
        default-context: localmind
```

**Note:** Spring Vault requires a running instance of HashiCorp Vault. To maintain the self-hosted principle, Vault would be run locally (natively or via Docker).

### Solution Comparison

| Criterion | Jasypt | Spring Vault |
|---|---|---|
| Setup complexity | Low | Medium-High |
| External dependencies | None | HashiCorp Vault |
| Key rotation | Manual | Automatable |
| Audit log | No | Yes |
| Self-hosted | Yes | Yes (Docker container) |
| Recommended for | Single user | Multi-user / enterprise |

The choice between the two options will be defined based on the project's evolution toward multi-user support.
