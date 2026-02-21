export const content = `# Gestione Credenziali

|               |                      |
|---------------|----------------------|
| **Documento** | Gestione Credenziali |
| **Versione**  | 0.1.0                |
| **Data**      | 2026-02-09           |
| **Progetto**  | LocalMind            |

---

## Indice

1. [Panoramica](#1-panoramica)
2. [API Key Management](#2-api-key-management)
   - 2.1 [Strategia di Gestione](#21-strategia-di-gestione)
   - 2.2 [File .env e .env.example](#22-file-env-e-envexample)
   - 2.3 [Docker Secrets (Produzione)](#23-docker-secrets-produzione)
3. [Variabili d'Ambiente Definite](#3-variabili-dambiente-definite)
4. [Spring Boot Property Resolution](#4-spring-boot-property-resolution)
5. [Abilitazione Condizionale Provider](#5-abilitazione-condizionale-provider)
6. [Best Practice](#6-best-practice)
7. [Cifratura API Key](#7-cifratura-api-key)

---

## 1. Panoramica

LocalMind gestisce diversi tipi di credenziali necessarie al funzionamento del sistema:

| Tipo | Obbligatorieta' | Esempio |
|---|---|---|
| Credenziali database | Obbligatorie | Username/password MySQL |
| API key provider LLM | Opzionali | OpenAI, Anthropic, Google |
| Credenziali servizi interni | Obbligatorie | Basic auth n8n |

Il principio guida nella gestione delle credenziali e' la **separazione tra codice e configurazione**: nessuna credenziale deve essere presente nel codice sorgente o nel repository Git. Tutte le credenziali sono gestite tramite variabili d'ambiente, caricate dal file \`.env\` in fase di sviluppo e da meccanismi sicuri (Docker secrets, environment variables del sistema operativo) in produzione.

---

## 2. API Key Management

### 2.1 Strategia di Gestione

Le API key dei provider LLM cloud (OpenAI, Anthropic, Google) seguono una strategia di gestione a tre livelli:

|             Livello                 |           Meccanismo           |        Ambiente           |
|-------------------------------------|--------------------------------|---------------------------|
| 1. Variabili d'ambiente del sistema | \`export OPENAI_API_KEY=sk-...\` | Produzione                |
| 2. File \`.env\`                      | \`OPENAI_API_KEY=sk-...\`        | Sviluppo locale           | 
| 3. Docker secrets                   | \`/run/secrets/openai_api_key\`  | Docker Swarm / Kubernetes |

La priorita' di risoluzione e':

1. Variabile d'ambiente del sistema operativo (massima priorita').
2. Variabile definita nel file \`.env\` (caricata da Spring Boot).
3. Valore di default definito in \`application.yml\` (tipicamente vuoto).

### 2.2 File .env e .env.example

**File \`.env\`** (gitignored, presente solo localmente):

\`\`\`env
# ==================================================
# LocalMind - Environment Variables
# ==================================================

# MySQL
DB_USERNAME=localmind
DB_PASSWORD=localmind_secret_password

# LLM Providers (opzionali - lasciare vuoti per modalita' solo locale)
OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
ANTHROPIC_API_KEY=sk-ant-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
GOOGLE_API_KEY=AIzaXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

# n8n
N8N_BASIC_AUTH_USER=admin
N8N_BASIC_AUTH_PASSWORD=n8n_secret_password
\`\`\`

**File \`.env.example\`** (committato nel repository, senza valori reali):

\`\`\`env
# ==================================================
# LocalMind - Environment Variables Template
# ==================================================
# Copiare questo file in .env e compilare con i propri valori:
#   cp .env.example .env
#
# ATTENZIONE: Non committare mai il file .env nel repository!
# ==================================================

# MySQL
DB_USERNAME=localmind
DB_PASSWORD=changeme

# LLM Providers (opzionali - lasciare vuoti per modalita' solo locale con Ollama)
OPENAI_API_KEY=
ANTHROPIC_API_KEY=
GOOGLE_API_KEY=

# n8n
N8N_BASIC_AUTH_USER=admin
N8N_BASIC_AUTH_PASSWORD=changeme
\`\`\`

**Regola \`.gitignore\`:**

\`\`\`gitignore
# Environment variables
.env
.env.local
.env.*.local
!.env.example
\`\`\`

### 2.3 Secrets Management (Produzione)

Per ambienti di produzione, le credenziali possono essere gestite tramite variabili d'ambiente del sistema operativo o tramite un secrets manager:

\`\`\`bash
# Variabili d'ambiente di sistema (Linux)
export DB_PASSWORD=my_secret_password
export OPENAI_API_KEY=sk-...

# Oppure tramite file di systemd environment
# /etc/localmind/env
DB_PASSWORD=my_secret_password
OPENAI_API_KEY=sk-...
\`\`\`

Per deployment Kubernetes, le credenziali possono essere gestite tramite Kubernetes Secrets:

\`\`\`yaml
apiVersion: v1
kind: Secret
metadata:
  name: localmind-secrets
type: Opaque
data:
  db-password: <base64-encoded>
  openai-api-key: <base64-encoded>
\`\`\`

---

## 3. Variabili d'Ambiente Definite

La tabella seguente elenca tutte le variabili d'ambiente riconosciute da LocalMind nella v0.1.0:

| Variabile | Obbligatoria | Default | Descrizione |
|---------------------------|-----|-------------|---------------------------------------|
| \`DB_USERNAME\`             | Si' | \`localmind\` | Username per la connessione a MySQL   |
| \`DB_PASSWORD\`             | Si' | \`localmind\` | Password per la connessione a MySQL   |
| \`OPENAI_API_KEY\`          | No  | *(vuoto)*   | API key per il provider OpenAI        |
| \`ANTHROPIC_API_KEY\`       | No  | *(vuoto)*   | API key per il provider Anthropic     |
| \`GOOGLE_API_KEY\`          | No  | *(vuoto)*   | API key per il provider Google AI     |
| \`N8N_BASIC_AUTH_USER\`     | Si' | \`admin\`     | Username per l'interfaccia web di n8n |
| \`N8N_BASIC_AUTH_PASSWORD\` | Si' | \`admin\`     | Password per l'interfaccia web di n8n |

### Variabili pianificate per versioni future

|          Variabile       | Versione target |                   Descrizione                      |
|--------------------------|-----------------|----------------------------------------------------|
| \`JWT_SECRET_KEY\`         |      v0.3.0     | Chiave segreta per la firma dei JWT                |
| \`JWT_EXPIRATION_MS\`      |      v0.3.0     | Durata del token JWT in millisecondi               |
| \`ENCRYPTION_KEY\`         |      v0.3.0     | Chiave per la cifratura delle API key nel database |
| \`KEYCLOAK_CLIENT_SECRET\` |      v1.0.0     | Client secret per integrazione Keycloak            |

---

## 4. Spring Boot Property Resolution

Spring Boot risolve le variabili d'ambiente all'interno dei file di configurazione YAML tramite la sintassi \`\${VARIABILE:default}\`:

**application.yml:**

\`\`\`yaml
spring:
  datasource:
    username: \${DB_USERNAME:localmind}
    password: \${DB_PASSWORD:localmind}

localmind:
  llm:
    openai:
      api-key: \${OPENAI_API_KEY:}
      enabled: false
    anthropic:
      api-key: \${ANTHROPIC_API_KEY:}
      enabled: false
    google:
      api-key: \${GOOGLE_API_KEY:}
      enabled: false
\`\`\`

### Meccanismo di risoluzione

1. Spring Boot cerca la variabile d'ambiente \`OPENAI_API_KEY\` nel sistema.
2. Se presente, utilizza il valore trovato.
3. Se assente, utilizza il valore di default dopo i due punti (\`:\`).
4. Se il default e' vuoto (\`\${OPENAI_API_KEY:}\`), la property risulta una stringa vuota.

**Esempio di risoluzione:**

\`\`\`
# Con variabile d'ambiente impostata:
export OPENAI_API_KEY=sk-abc123
# Risultato: localmind.llm.openai.api-key = "sk-abc123"

# Senza variabile d'ambiente:
# Risultato: localmind.llm.openai.api-key = ""
\`\`\`

### application-prod.yml

Il profilo di produzione sovrascrive le impostazioni di default:

\`\`\`yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/localmind
    username: \${DB_USERNAME}
    password: \${DB_PASSWORD}
\`\`\`

**Nota:** Nel profilo di produzione, le variabili d'ambiente \`DB_USERNAME\` e \`DB_PASSWORD\` sono **obbligatorie** (nessun valore di default). L'applicazione non si avviera' se non sono definite.

---

## 5. Abilitazione Condizionale Provider

I provider LLM cloud sono gestiti tramite un meccanismo di abilitazione condizionale basato su \`@ConditionalOnProperty\` di Spring Boot:

### Configurazione

\`\`\`yaml
# application.yml
localmind:
  llm:
    openai:
      enabled: false    # Disabilitato di default
      api-key: \${OPENAI_API_KEY:}
      model: gpt-4
    anthropic:
      enabled: false    # Disabilitato di default
      api-key: \${ANTHROPIC_API_KEY:}
      model: claude-sonnet-4-20250514
    google:
      enabled: false    # Disabilitato di default
      api-key: \${GOOGLE_API_KEY:}
      model: gemini-pro
    ollama:
      enabled: true     # Abilitato di default
      base-url: http://localhost:11434
      model: llama3.2
\`\`\`

### Implementazione Adapter

\`\`\`java
@Component
@ConditionalOnProperty(
    name = "localmind.llm.openai.enabled",
    havingValue = "true"
)
public class OpenAiLlmAdapter implements LlmPort {

    private final String apiKey;

    public OpenAiLlmAdapter(
            @Value("\${localmind.llm.openai.api-key}") String apiKey) {
        this.apiKey = apiKey;
        // Validazione: se la key e' vuota, il bean non dovrebbe essere creato
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "OpenAI API key is required when openai provider is enabled. " +
                "Set OPENAI_API_KEY environment variable."
            );
        }
    }

    // ... implementazione metodi LlmPort
}
\`\`\`

### Comportamento

| \`enabled\` | \`api-key\` | Risultato |
|---|---|---|
| \`false\` | *(qualsiasi)* | Bean **non creato**. Provider non disponibile. |
| \`true\` | *(vuoto)* | **Errore all'avvio.** \`IllegalStateException\` con messaggio esplicativo. |
| \`true\` | *(valido)* | Bean creato. Provider disponibile nel \`LlmGatewayService\`. |

Questo meccanismo garantisce che:

1. I provider cloud non vengano mai attivati accidentalmente.
2. L'abilitazione richieda un'azione esplicita dell'utente (\`enabled: true\` + API key).
3. Configurazioni incoerenti (provider abilitato senza API key) vengano intercettate immediatamente all'avvio.

---

## 6. Best Practice

### 6.1 Non committare mai credenziali nel repository

\`\`\`bash
# Verificare che .env sia nel .gitignore
grep -q "^\\.env$" .gitignore || echo ".env" >> .gitignore

# Verificare che non ci siano credenziali nei file tracciati
git log --all --full-history -S "sk-" -- "*.yml" "*.yaml" "*.properties" "*.java"
\`\`\`

**Regola aurea:** Se un file contiene credenziali reali, **non deve mai** essere tracciato da Git.

### 6.2 Utilizzare .gitignore per file sensibili

\`\`\`gitignore
# Credenziali e configurazioni sensibili
.env
.env.local
.env.*.local
*.pem
*.key
*.p12
*.jks

# IDE con possibili configurazioni sensibili
.idea/
.vscode/settings.json
\`\`\`

### 6.3 Ruotare le API key periodicamente

| Provider | Frequenza consigliata | Procedura |
|---|---|---|
| OpenAI | Ogni 90 giorni | Dashboard OpenAI -> API Keys -> Create new secret key |
| Anthropic | Ogni 90 giorni | Console Anthropic -> API Keys -> Generate Key |
| Google | Ogni 90 giorni | Google Cloud Console -> Credentials -> Create credentials |

**Procedura di rotazione:**

1. Generare una nuova API key dal dashboard del provider.
2. Aggiornare il file \`.env\` locale con la nuova key.
3. Riavviare l'applicazione Spring Boot.
4. Verificare il funzionamento con una richiesta di test.
5. Revocare la vecchia API key dal dashboard del provider.

### 6.4 Utilizzare API key con permessi minimi

| Provider | Permesso consigliato | Motivazione |
|---|---|---|
| OpenAI | Solo Chat Completions | LocalMind non necessita di accesso a DALL-E, Whisper, ecc. |
| Anthropic | Solo Messages API | Accesso limitato alla sola API di conversazione |
| Google | Solo Gemini API | Limitare l'accesso alle sole funzionalita' utilizzate |

### 6.5 Monitorare l'utilizzo delle API key

Verificare periodicamente i dashboard dei provider per:

- Utilizzo anomalo (picchi di richieste inattesi).
- Costi imprevisti.
- Richieste da IP non riconosciuti (se il provider lo consente).

---

## 7. Cifratura API Key

**Stato attuale:** Non implementata nella v0.1.0. Le API key sono memorizzate in chiaro nelle variabili d'ambiente.

**Piano per v0.3.0:** Implementazione della cifratura delle API key memorizzate nel database tramite una delle seguenti soluzioni:

### Opzione A: Jasypt (Java Simplified Encryption)

\`\`\`xml
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
\`\`\`

**Utilizzo in application.yml:**

\`\`\`yaml
localmind:
  llm:
    openai:
      api-key: ENC(cifrato_base64_qui)
\`\`\`

**Cifratura:**

\`\`\`bash
java -cp jasypt.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \\
  input="sk-la-mia-api-key" \\
  password="master-password" \\
  algorithm=PBEWithMD5AndTripleDES
\`\`\`

La master password per la decifratura viene fornita tramite variabile d'ambiente:

\`\`\`bash
export JASYPT_ENCRYPTOR_PASSWORD=master-password
\`\`\`

### Opzione B: Spring Vault

Per ambienti che richiedono una gestione centralizzata dei secrets:

\`\`\`xml
<dependency>
    <groupId>org.springframework.vault</groupId>
    <artifactId>spring-vault-core</artifactId>
</dependency>
\`\`\`

**Configurazione:**

\`\`\`yaml
spring:
  cloud:
    vault:
      uri: http://localhost:8200
      token: \${VAULT_TOKEN}
      kv:
        backend: secret
        default-context: localmind
\`\`\`

**Nota:** Spring Vault richiede un'istanza di HashiCorp Vault in esecuzione. Per mantenere il principio self-hosted, Vault verrebbe eseguito localmente (nativamente o tramite Docker).

### Confronto soluzioni

| Criterio | Jasypt | Spring Vault |
|---|---|---|
| Complessita' setup | Bassa | Media-Alta |
| Dipendenze esterne | Nessuna | HashiCorp Vault |
| Rotazione chiavi | Manuale | Automatizzabile |
| Audit log | No | Si' |
| Self-hosted | Si' | Si' (container Docker) |
| Raccomandato per | Singolo utente | Multi-utente / enterprise |

La scelta tra le due opzioni verra' definita in base all'evoluzione del progetto verso il supporto multi-utente.
`;
