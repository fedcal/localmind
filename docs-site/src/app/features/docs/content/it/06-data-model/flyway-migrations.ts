export const content = `# Migrazioni Flyway

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09
**Directory migrazioni:** \`localmind-app/src/main/resources/db/migration/\`

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Configurazione](#2-configurazione)
3. [V1 - Tabella documents](#3-v1---tabella-documents)
4. [V2 - Tabella folder_configs](#4-v2---tabella-folder_configs)
5. [V3 - Tabella llm_usage](#5-v3---tabella-llm_usage)
6. [V4 - Tabelle conversations e chat_messages](#6-v4---tabelle-conversations-e-chat_messages)
7. [V5 - Tabella webhooks](#7-v5---tabella-webhooks)
8. [Riepilogo Migrazioni](#8-riepilogo-migrazioni)
9. [Procedure Operative](#9-procedure-operative)

---

## 1. Panoramica

Flyway e' il sistema di migrazione database adottato da LocalMind per gestire l'evoluzione dello schema MySQL in modo versionato e ripetibile. Ogni modifica allo schema e' tracciata in un file SQL con versione incrementale, garantendo:

- **Versionamento**: ogni migrazione ha un numero di versione univoco e sequenziale.
- **Ripetibilita'**: l'applicazione delle migrazioni e' idempotente; Flyway traccia le migrazioni gia' eseguite nella tabella interna \`flyway_schema_history\`.
- **Coerenza**: l'integrazione con \`ddl-auto: validate\` di Hibernate garantisce che le entita' JPA siano sempre allineate con lo schema.
- **Automazione**: le migrazioni vengono eseguite automaticamente all'avvio dell'applicazione Spring Boot.

### Convenzione di naming

I file di migrazione seguono la convenzione:

\`\`\`
V{numero}__{descrizione}.sql
\`\`\`

- \`V\`: prefisso obbligatorio per migrazioni versionati (non ripetibili).
- \`{numero}\`: numero di versione incrementale (1, 2, 3, ...).
- \`__\`: doppio underscore come separatore.
- \`{descrizione}\`: descrizione leggibile in snake_case.

### Directory

\`\`\`
localmind-app/
  src/
    main/
      resources/
        db/
          migration/
            V1__create_documents_table.sql
            V2__create_folder_configs_table.sql
            V3__create_llm_usage_table.sql
            V4__create_conversations_table.sql
            V5__create_webhooks_table.sql
\`\`\`

---

## 2. Configurazione

### application-dev.yml

\`\`\`yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
  jpa:
    hibernate:
      ddl-auto: validate
\`\`\`

### Parametri

| Parametro                       | Valore                     | Descrizione                                                          |
|---------------------------------|----------------------------|----------------------------------------------------------------------|
| \`spring.flyway.enabled\`         | \`true\`                     | Attiva l'esecuzione automatica delle migrazioni all'avvio            |
| \`spring.flyway.locations\`       | \`classpath:db/migration\`   | Percorso dei file di migrazione SQL                                  |
| \`spring.jpa.hibernate.ddl-auto\` | \`validate\`                 | Hibernate verifica la coerenza entita'-schema senza modificare il DB |

### Dipendenze Maven (localmind-app)

\`\`\`xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
\`\`\`

La dipendenza \`flyway-mysql\` e' necessaria per il supporto specifico di MySQL (es. gestione di tipi come \`JSON\`, funzioni come \`UUID()\`).

---

## 3. V1 - Tabella documents

### File: \`V1__create_documents_table.sql\`

### Motivazione

Crea la tabella principale per la gestione dei documenti caricati nel sistema. Questa tabella memorizza i metadati dei file (nome, percorso, tipo MIME, dimensione, hash), lo stato di elaborazione e metadati aggiuntivi in formato JSON.

### SQL

\`\`\`sql
CREATE TABLE documents (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    filename VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000),
    mime_type VARCHAR(100),
    file_size BIGINT,
    file_hash VARCHAR(64),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    metadata JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    indexed_at TIMESTAMP NULL
);

CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_documents_hash ON documents(file_hash);
\`\`\`

### Indici creati

| Indice                 | Colonna    | Motivazione                                                                                                        |
|------------------------|------------|--------------------------------------------------------------------------------------------------------------------|
| \`idx_documents_status\` | \`status\`   | Filtraggio per stato (PENDING, PROCESSING, INDEXED, ERROR); query frequente nella dashboard e nel batch processing |
| \`idx_documents_hash\`   | \`file_hash\`| Ricerca per hash SHA-256 per rilevare duplicati prima del caricamento                                              |

### Constraint

- \`id\`: PRIMARY KEY con valore di default generato da \`UUID()\`.
- \`filename\`: NOT NULL, ogni documento deve avere un nome file.
- \`status\`: NOT NULL con DEFAULT \`'PENDING'\`, ogni documento inizia nello stato PENDING.
- \`created_at\`: NOT NULL con DEFAULT \`CURRENT_TIMESTAMP\`.
- \`updated_at\`: NOT NULL con DEFAULT \`CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\`.

---

## 4. V2 - Tabella folder_configs

### File: \`V2__create_folder_configs_table.sql\`

### Motivazione

Crea la tabella per la configurazione delle cartelle locali monitorate dal sistema. Ogni record rappresenta una cartella del filesystem che il batch job di scansione periodica analizzera' per individuare nuovi documenti.

### SQL

\`\`\`sql
CREATE TABLE folder_configs (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    path VARCHAR(1000) NOT NULL,
    recursive BOOLEAN NOT NULL DEFAULT TRUE,
    watch_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    last_scan_at TIMESTAMP NULL,
    document_count INTEGER NOT NULL DEFAULT 0
);
\`\`\`

### Constraint

- \`id\`: PRIMARY KEY generato automaticamente.
- \`path\`: NOT NULL, percorso assoluto della cartella.
- \`recursive\`: NOT NULL con DEFAULT \`TRUE\`, scansione ricorsiva abilitata per default.
- \`watch_enabled\`: NOT NULL con DEFAULT \`FALSE\`, monitoraggio in tempo reale disabilitato per default.
- \`document_count\`: NOT NULL con DEFAULT \`0\`.

### Note

- Non sono presenti indici aggiuntivi poiche' il volume di dati previsto per questa tabella e' ridotto (ordine delle decine di record).
- Il campo \`path\` non ha un vincolo \`UNIQUE\` a livello di DDL, ma l'unicita' e' gestita a livello applicativo.

---

## 5. V3 - Tabella llm_usage

### File: \`V3__create_llm_usage_table.sql\`

### Motivazione

Crea la tabella per il tracking dell'utilizzo dei provider LLM. Ogni record rappresenta una singola chiamata a un provider, con informazioni su token consumati, costo stimato e latenza. Essenziale per il monitoraggio dei costi e l'analisi delle performance.

### SQL

\`\`\`sql
CREATE TABLE llm_usage (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    provider VARCHAR(20) NOT NULL,
    model VARCHAR(100),
    prompt_tokens INTEGER NOT NULL DEFAULT 0,
    completion_tokens INTEGER NOT NULL DEFAULT 0,
    total_tokens INTEGER NOT NULL DEFAULT 0,
    cost DOUBLE NOT NULL DEFAULT 0,
    latency_ms BIGINT NOT NULL DEFAULT 0,
    \`timestamp\` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_llm_usage_timestamp ON llm_usage(\`timestamp\`);
CREATE INDEX idx_llm_usage_provider ON llm_usage(provider);
\`\`\`

### Indici creati

| Indice                    | Colonna    | Motivazione                                                                                                               |
|---------------------------|------------|---------------------------------------------------------------------------------------------------------------------------|
| \`idx_llm_usage_timestamp\` | \`timestamp\`| Query per intervallo temporale (es. utilizzo nell'ultima settimana, nel mese corrente); essenziale per report e dashboard |
| \`idx_llm_usage_provider\`  | \`provider\` | Aggregazione per provider (es. costo totale per OpenAI vs Ollama); filtraggio nella dashboard                             |

### Constraint

- Tutti i campi numerici (\`prompt_tokens\`, \`completion_tokens\`, \`total_tokens\`, \`cost\`, \`latency_ms\`) sono NOT NULL con DEFAULT \`0\`.
- \`provider\`: NOT NULL, identificativo del provider LLM.
- \`timestamp\`: NOT NULL con DEFAULT \`CURRENT_TIMESTAMP\`.

### Nota sul tipo cost

Il campo \`cost\` utilizza \`DOUBLE\` (IEEE 754 a 64 bit). Per la versione 0.1.0 questa precisione e' sufficiente. In versioni future potrebbe essere migrato a \`DECIMAL(10,6)\` per calcoli finanziari esatti.

---

## 6. V4 - Tabelle conversations e chat_messages

### File: \`V4__create_conversations_table.sql\`

### Motivazione

Crea le tabelle per il sistema di chat conversazionale. La migrazione include due tabelle correlate: \`conversations\` (contenitore) e \`chat_messages\` (messaggi). La separazione consente di gestire conversazioni multiple e di mantenere la cronologia dei messaggi.

### SQL

\`\`\`sql
CREATE TABLE conversations (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    title VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE chat_messages (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    conversation_id CHAR(36) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
);

CREATE INDEX idx_chat_messages_conversation ON chat_messages(conversation_id);
\`\`\`

### Indici creati

| Indice                            | Colonna           | Motivazione                                          |
|-----------------------------------|-------------------|------------------------------------------------------|
| \`idx_chat_messages_conversation\`  | \`conversation_id\` | Join e filtraggio per conversazione; ogni apertura di conversazione richiede il caricamento di tutti i messaggi associati |

### Constraint

- \`conversations.title\`: nullable, il titolo puo' essere generato automaticamente dopo il primo messaggio.
- \`chat_messages.conversation_id\`: NOT NULL, REFERENCES \`conversations(id)\` con \`ON DELETE CASCADE\`.
- \`chat_messages.role\`: NOT NULL, valori previsti: \`USER\`, \`ASSISTANT\`.
- \`chat_messages.content\`: NOT NULL, tipo \`TEXT\` per contenuti di lunghezza variabile.

### ON DELETE CASCADE

La clausola \`ON DELETE CASCADE\` sulla chiave esterna \`conversation_id\` garantisce che l'eliminazione di una conversazione provochi l'eliminazione automatica di tutti i messaggi associati. Questa scelta semplifica la logica applicativa ed evita record orfani.

---

## 7. V5 - Tabella webhooks

### File: \`V5__create_webhooks_table.sql\`

### Motivazione

Crea la tabella per la configurazione dei webhook utilizzati per notifiche e automazioni esterne, in particolare l'integrazione con n8n. Ogni record definisce un endpoint di callback che viene invocato al verificarsi di un evento specifico.

### SQL

\`\`\`sql
CREATE TABLE webhooks (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(200) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
\`\`\`

### Constraint

- \`name\`: NOT NULL, nome descrittivo del webhook.
- \`url\`: NOT NULL, URL completo dell'endpoint di destinazione.
- \`event_type\`: NOT NULL, tipo di evento che attiva il webhook (es. \`DOCUMENT_UPLOADED\`, \`DOCUMENT_INDEXED\`, \`DOCUMENT_ERROR\`).
- \`active\`: NOT NULL con DEFAULT \`TRUE\`.

### Note

- Non sono presenti indici aggiuntivi poiche' il volume di dati previsto e' ridotto.
- Il campo \`event_type\` utilizza \`VARCHAR(30)\` anziche' un tipo \`ENUM\` MySQL per flessibilita' nell'aggiunta di nuovi tipi di evento.

---

## 8. Riepilogo Migrazioni

| Versione | File                                   | Tabelle create                   | Indici creati                                       |
|----------|----------------------------------------|----------------------------------|-----------------------------------------------------|
| V1       | \`V1__create_documents_table.sql\`       | \`documents\`                      | \`idx_documents_status\`, \`idx_documents_hash\`        |
| V2       | \`V2__create_folder_configs_table.sql\`  | \`folder_configs\`                 | (nessuno)                                           |
| V3       | \`V3__create_llm_usage_table.sql\`       | \`llm_usage\`                      | \`idx_llm_usage_timestamp\`, \`idx_llm_usage_provider\` |
| V4       | \`V4__create_conversations_table.sql\`   | \`conversations\`, \`chat_messages\` | \`idx_chat_messages_conversation\`                    |
| V5       | \`V5__create_webhooks_table.sql\`        | \`webhooks\`                       | (nessuno)                                           |

### Totali

- **6 tabelle** create
- **5 indici** B-tree
- **1 chiave esterna** con CASCADE delete
- **5 file** di migrazione SQL

---

## 9. Procedure Operative

### Verifica stato migrazioni

\`\`\`bash
# Accesso al database
mysql -h localhost -u localmind -plocalmind localmind

# Query sulla tabella di tracking Flyway
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;
\`\`\`

### Creazione di una nuova migrazione

1. Creare un nuovo file nella directory \`localmind-app/src/main/resources/db/migration/\`.
2. Seguire la convenzione di naming: \`V{numero_successivo}__{descrizione}.sql\`.
3. Scrivere il DDL SQL.
4. Riavviare l'applicazione: Flyway applichera' automaticamente la nuova migrazione.
5. Verificare che \`ddl-auto: validate\` non segnali errori (allineamento entita' JPA).

### Esempio

\`\`\`
V6__add_user_table.sql
\`\`\`

### Rollback

Flyway Community Edition non supporta il rollback automatico. In caso di errore:

1. Correggere manualmente lo schema nel database.
2. Aggiornare la tabella \`flyway_schema_history\` se necessario.
3. Creare una nuova migrazione correttiva (es. \`V6__fix_column_type.sql\`).
`;
