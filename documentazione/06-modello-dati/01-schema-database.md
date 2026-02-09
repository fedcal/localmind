# Schema Database

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09
**Database:** MySQL 8.0

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Tabella documents](#2-tabella-documents)
3. [Tabella folder_configs](#3-tabella-folder_configs)
4. [Tabella llm_usage](#4-tabella-llm_usage)
5. [Tabella conversations](#5-tabella-conversations)
6. [Tabella chat_messages](#6-tabella-chat_messages)
7. [Tabella webhooks](#7-tabella-webhooks)
8. [Diagramma ER](#8-diagramma-er)
9. [Convenzioni](#9-convenzioni)

---

## 1. Panoramica

Il database di LocalMind e' composto da 6 tabelle MySQL, create e gestite tramite migrazioni Flyway. Lo schema e' progettato per supportare le funzionalita' core della piattaforma: gestione documenti, conversazioni chat, tracciamento dell'utilizzo LLM, configurazione cartelle e automazioni webhook.

### Riepilogo tabelle

| Tabella          | Migrazione | Descrizione                                    | Relazioni           |
|------------------|------------|------------------------------------------------|---------------------|
| `documents`      | V1         | Documenti caricati e indicizzati               | Nessuna (standalone)|
| `folder_configs` | V2         | Configurazione cartelle monitorate             | Nessuna (standalone)|
| `llm_usage`      | V3         | Tracking utilizzo provider LLM                 | Nessuna (standalone)|
| `conversations`  | V4         | Conversazioni chat                             | 1:N -> chat_messages|
| `chat_messages`  | V4         | Messaggi nelle conversazioni                   | N:1 -> conversations|
| `webhooks`       | V5         | Webhook per automazioni esterne                | Nessuna (standalone)|

---

## 2. Tabella documents

Memorizza i metadati dei documenti caricati nel sistema, incluso lo stato di elaborazione e l'eventuale indicizzazione vettoriale.

### Struttura

| Colonna     | Tipo           | Nullable | Default              | Descrizione                           |
|-------------|----------------|----------|----------------------|---------------------------------------|
| `id`        | `CHAR(36)`     | NO       | `(UUID())`           | Identificatore univoco                |
| `filename`  | `VARCHAR(500)` | NO       | -                    | Nome originale del file               |
| `file_path` | `VARCHAR(1000)`| SI       | -                    | Percorso di storage su filesystem     |
| `mime_type` | `VARCHAR(100)` | SI       | -                    | Tipo MIME del file (es. `application/pdf`) |
| `file_size` | `BIGINT`       | SI       | -                    | Dimensione del file in byte           |
| `file_hash` | `VARCHAR(64)`  | SI       | -                    | Hash SHA-256 per deduplicazione       |
| `status`    | `VARCHAR(20)`  | NO       | `'PENDING'`          | Stato di elaborazione del documento   |
| `metadata`  | `JSON`         | SI       | -                    | Metadati aggiuntivi in formato JSON   |
| `created_at`| `TIMESTAMP`    | NO       | `CURRENT_TIMESTAMP`  | Data/ora di creazione                 |
| `updated_at`| `TIMESTAMP`    | NO       | `CURRENT_TIMESTAMP`  | Data/ora ultimo aggiornamento         |
| `indexed_at`| `TIMESTAMP`    | SI       | -                    | Data/ora completamento indicizzazione |

### Chiave primaria

- `id` (CHAR(36), generato automaticamente)

### Indici

| Nome indice            | Colonna(e) | Tipo    | Motivazione                          |
|------------------------|------------|---------|--------------------------------------|
| `idx_documents_status` | `status`   | B-tree  | Filtraggio documenti per stato       |
| `idx_documents_hash`   | `file_hash`| B-tree  | Ricerca rapida per deduplicazione    |

### Valori del campo status

| Valore       | Descrizione                                              |
|--------------|----------------------------------------------------------|
| `PENDING`    | Documento caricato, in attesa di elaborazione            |
| `PROCESSING` | Elaborazione in corso (estrazione testo, chunking)       |
| `INDEXED`    | Indicizzazione completata nel vector store               |
| `ERROR`      | Errore durante l'elaborazione                            |

### Campo metadata (JSON)

Il campo `metadata` e' di tipo JSON e consente lo storage flessibile di metadati eterogenei. Esempi di contenuto:

```json
{
  "author": "Nome Autore",
  "pageCount": 42,
  "language": "it",
  "extractedTitle": "Titolo del Documento",
  "chunkCount": 15
}
```

### SQL di creazione

```sql
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
```

---

## 3. Tabella folder_configs

Memorizza la configurazione delle cartelle locali monitorate dal sistema per la scansione automatica di nuovi documenti.

### Struttura

| Colonna          | Tipo            | Nullable | Default              | Descrizione                            |
|------------------|-----------------|----------|----------------------|----------------------------------------|
| `id`             | `CHAR(36)`      | NO       | `(UUID())`           | Identificatore univoco                 |
| `path`           | `VARCHAR(1000)` | NO       | -                    | Percorso assoluto della cartella       |
| `recursive`      | `BOOLEAN`       | NO       | `TRUE`               | Scansione ricorsiva delle sottocartelle|
| `watch_enabled`  | `BOOLEAN`       | NO       | `FALSE`              | Monitoraggio in tempo reale abilitato  |
| `last_scan_at`   | `TIMESTAMP`     | SI       | -                    | Data/ora dell'ultima scansione         |
| `document_count` | `INTEGER`       | NO       | `0`                  | Numero di documenti trovati            |

### Chiave primaria

- `id` (CHAR(36), generato automaticamente)

### SQL di creazione

```sql
CREATE TABLE folder_configs (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    path VARCHAR(1000) NOT NULL,
    recursive BOOLEAN NOT NULL DEFAULT TRUE,
    watch_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    last_scan_at TIMESTAMP NULL,
    document_count INTEGER NOT NULL DEFAULT 0
);
```

---

## 4. Tabella llm_usage

Memorizza i record di utilizzo dei provider LLM per tracking dei costi, monitoraggio delle performance e analisi dell'uso.

### Struttura

| Colonna             | Tipo               | Nullable | Default              | Descrizione                                           |
|---------------------|--------------------|----------|----------------------|-------------------------------------------------------|
| `id`                | `CHAR(36)`         | NO       | `(UUID())`           | Identificatore univoco                                |
| `provider`          | `VARCHAR(20)`      | NO       | -                    | Nome del provider (OLLAMA, OPENAI, ANTHROPIC, GOOGLE) |
| `model`             | `VARCHAR(100)`     | SI       | -                    | Nome del modello utilizzato                           |
| `prompt_tokens`     | `INTEGER`          | NO       | `0`                  | Token nel prompt                                      |
| `completion_tokens` | `INTEGER`          | NO       | `0`                  | Token nella risposta                                  |
| `total_tokens`      | `INTEGER`          | NO       | `0`                  | Token totali (prompt + completion)                    |
| `cost`              | `DOUBLE`           | NO       | `0`                  | Costo stimato della chiamata                          |
| `latency_ms`        | `BIGINT`           | NO       | `0`                  | Latenza in millisecondi                               |
| `timestamp`         | `TIMESTAMP`        | NO       | `CURRENT_TIMESTAMP`  | Data/ora della chiamata                               |

### Chiave primaria

- `id` (CHAR(36), generato automaticamente)

### Indici

| Nome indice               | Colonna(e)  | Tipo    | Motivazione                              |
|---------------------------|-------------|---------|------------------------------------------|
| `idx_llm_usage_timestamp` | `timestamp` | B-tree  | Query per intervallo temporale           |
| `idx_llm_usage_provider`  | `provider`  | B-tree  | Aggregazione e filtraggio per provider   |

### SQL di creazione

```sql
CREATE TABLE llm_usage (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    provider VARCHAR(20) NOT NULL,
    model VARCHAR(100),
    prompt_tokens INTEGER NOT NULL DEFAULT 0,
    completion_tokens INTEGER NOT NULL DEFAULT 0,
    total_tokens INTEGER NOT NULL DEFAULT 0,
    cost DOUBLE NOT NULL DEFAULT 0,
    latency_ms BIGINT NOT NULL DEFAULT 0,
    `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_llm_usage_timestamp ON llm_usage(`timestamp`);
CREATE INDEX idx_llm_usage_provider ON llm_usage(provider);
```

---

## 5. Tabella conversations

Memorizza le conversazioni chat dell'utente. Ogni conversazione puo' contenere piu' messaggi.

### Struttura

| Colonna      | Tipo           | Nullable | Default              | Descrizione                           |
|-------------|-----------------|----------|----------------------|---------------------------------------|
| `id`        | `CHAR(36)`      | NO       | `(UUID())`           | Identificatore univoco                |
| `title`     | `VARCHAR(500)`  | SI       | -                    | Titolo della conversazione            |
| `created_at`| `TIMESTAMP`     | NO       | `CURRENT_TIMESTAMP`  | Data/ora di creazione                 |
| `updated_at`| `TIMESTAMP`     | NO       | `CURRENT_TIMESTAMP`  | Data/ora ultimo aggiornamento         |

### Chiave primaria

- `id` (CHAR(36), generato automaticamente)

### SQL di creazione

```sql
CREATE TABLE conversations (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    title VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## 6. Tabella chat_messages

Memorizza i singoli messaggi all'interno delle conversazioni. Ogni messaggio e' associato a una conversazione e ha un ruolo (utente o assistente).

### Struttura

| Colonna           | Tipo           | Nullable | Default              | Descrizione                           |
|-------------------|----------------|----------|----------------------|---------------------------------------|
| `id`              | `CHAR(36)`     | NO       | `(UUID())`           | Identificatore univoco                |
| `conversation_id` | `CHAR(36)`     | NO       | -                    | Riferimento alla conversazione        |
| `role`            | `VARCHAR(20)`  | NO       | -                    | Ruolo del mittente (USER, ASSISTANT)  |
| `content`         | `TEXT`         | NO       | -                    | Contenuto testuale del messaggio      |
| `created_at`      | `TIMESTAMP`    | NO       | `CURRENT_TIMESTAMP`  | Data/ora di creazione                 |

### Chiave primaria

- `id` (CHAR(36), generato automaticamente)

### Chiave esterna

| Colonna           | Tabella riferita | Colonna riferita | On Delete |
|-------------------|------------------|------------------|-----------|
| `conversation_id` | `conversations`  | `id`             | `CASCADE` |

La clausola `ON DELETE CASCADE` garantisce che l'eliminazione di una conversazione comporti automaticamente l'eliminazione di tutti i messaggi associati.

### Indici

| Nome indice                      | Colonna(e)       | Tipo    | Motivazione                              |
|----------------------------------|------------------|---------|------------------------------------------|
| `idx_chat_messages_conversation` | `conversation_id`| B-tree  | Join e filtraggio per conversazione      |

### SQL di creazione

```sql
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
```

---

## 7. Tabella webhooks

Memorizza la configurazione dei webhook per notifiche e automazioni esterne (integrazione con n8n).

### Struttura

| Colonna     | Tipo           | Nullable | Default              | Descrizione                           |
|-------------|----------------|----------|----------------------|---------------------------------------|
| `id`        | `CHAR(36)`     | NO       | `(UUID())`           | Identificatore univoco                |
| `name`      | `VARCHAR(200)` | NO       | -                    | Nome descrittivo del webhook          |
| `url`       | `VARCHAR(1000)`| NO       | -                    | URL di destinazione del webhook       |
| `event_type`| `VARCHAR(30)`  | NO       | -                    | Tipo di evento che attiva il webhook  |
| `active`    | `BOOLEAN`      | NO       | `TRUE`               | Stato di attivazione                  |

### Chiave primaria

- `id` (CHAR(36), generato automaticamente)

### Valori del campo event_type

| Valore                 | Descrizione                                         |
|------------------------|-----------------------------------------------------|
| `DOCUMENT_UPLOADED`    | Documento caricato nel sistema                      |
| `DOCUMENT_INDEXED`     | Documento indicizzato nel vector store              |
| `DOCUMENT_ERROR`       | Errore durante l'elaborazione di un documento       |

### SQL di creazione

```sql
CREATE TABLE webhooks (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(200) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
```

---

## 8. Diagramma ER

```
+-------------------+          +---------------------+
|    documents      |          |    folder_configs   |
+-------------------+          +---------------------+
| id          (PK)  |          | id            (PK)  |
| filename          |          | path                |
| file_path         |          | recursive           |
| mime_type         |          | watch_enabled       |
| file_size         |          | last_scan_at        |
| file_hash         |          | document_count      |
| status            |          +---------------------+
| metadata (JSON)   |
| created_at        |          +---------------------+
| updated_at        |          |    llm_usage        |
| indexed_at        |          +---------------------+
+-------------------+          | id            (PK)  |
                               | provider            |
                               | model               |
+-------------------+          | prompt_tokens       |
|  conversations    |          | completion_tokens   |
+-------------------+          | total_tokens        |
| id          (PK)  |          | cost                |
| title             |          | latency_ms          |
| created_at        |          | timestamp           |
| updated_at        |          +---------------------+
+--------+----------+
         |
         | 1:N (ON DELETE CASCADE)
         |
+--------+----------+          +---------------------+
|  chat_messages    |          |     webhooks        |
+-------------------+          +---------------------+
| id          (PK)  |          | id            (PK)  |
| conversation_id(FK)|         | name                |
| role              |          | url                 |
| content           |          | event_type          |
| created_at        |          | active              |
+-------------------+          +---------------------+
```

### Relazioni

- **conversations -> chat_messages**: relazione 1:N. Una conversazione contiene zero o piu' messaggi. L'eliminazione di una conversazione causa l'eliminazione a cascata di tutti i messaggi associati (`ON DELETE CASCADE`).

Le tabelle `documents`, `folder_configs`, `llm_usage` e `webhooks` sono entita' standalone senza relazioni dirette con altre tabelle.

---

## 9. Convenzioni

### Naming

- **Tabelle**: `snake_case`, plurale (es. `documents`, `chat_messages`).
- **Colonne**: `snake_case` (es. `file_size`, `created_at`).
- **Indici**: `idx_{tabella}_{colonna}` (es. `idx_documents_status`).
- **Chiavi primarie**: `id` di tipo `CHAR(36)` per tutte le tabelle.
- **Timestamp**: tipo `TIMESTAMP` con `DEFAULT CURRENT_TIMESTAMP` per le colonne di audit.

### Tipi di dato

- **Identificatori**: `CHAR(36)` con generazione lato database tramite `UUID()`.
- **Stringhe**: `VARCHAR(n)` con lunghezza massima specificata.
- **Testo lungo**: `TEXT` per contenuti senza limite di lunghezza (es. messaggi chat).
- **JSON**: `JSON` per dati semi-strutturati (metadati documento).
- **Booleani**: `BOOLEAN` con `DEFAULT TRUE` o `DEFAULT FALSE`.
- **Numerici**: `INTEGER`, `BIGINT`, `DOUBLE` in base alla precisione richiesta.
