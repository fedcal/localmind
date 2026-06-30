export const content = `# Database Schema

**Project:** LocalMind
**Version:** 0.1.0
**Date:** 2026-02-09
**Database:** MySQL 8.0

---

## Table of Contents

1. [Overview](#1-overview)
2. [Table documents](#2-table-documents)
3. [Table folder_configs](#3-table-folder_configs)
4. [Table llm_usage](#4-table-llm_usage)
5. [Table conversations](#5-table-conversations)
6. [Table chat_messages](#6-table-chat_messages)
7. [Table webhooks](#7-table-webhooks)
8. [ER Diagram](#8-er-diagram)
9. [Conventions](#9-conventions)

---

## 1. Overview

The LocalMind database consists of 6 MySQL tables, created and managed via Flyway migrations. The schema is designed to support the platform's core features: document management, chat conversations, LLM usage tracking, folder configuration, and webhook automations.

### Table Summary

| Table            | Migration  | Description                                    | Relationships       |
|------------------|------------|------------------------------------------------|---------------------|
| \`documents\`      | V1         | Uploaded and indexed documents                 | None (standalone)   |
| \`folder_configs\` | V2         | Monitored folder configuration                 | None (standalone)   |
| \`llm_usage\`      | V3         | LLM provider usage tracking                    | None (standalone)   |
| \`conversations\`  | V4         | Chat conversations                             | 1:N -> chat_messages|
| \`chat_messages\`  | V4         | Messages within conversations                  | N:1 -> conversations|
| \`webhooks\`       | V5         | Webhooks for external automations              | None (standalone)   |

---

## 2. Table documents

Stores metadata for documents uploaded to the system, including processing status and possible vector indexing.

### Structure

| Column      | Type           | Nullable | Default              | Description                           |
|-------------|----------------|----------|----------------------|---------------------------------------|
| \`id\`        | \`CHAR(36)\`     | NO       | \`(UUID())\`           | Unique identifier                     |
| \`filename\`  | \`VARCHAR(500)\` | NO       | -                    | Original file name                    |
| \`file_path\` | \`VARCHAR(1000)\`| YES      | -                    | Filesystem storage path               |
| \`mime_type\` | \`VARCHAR(100)\` | YES      | -                    | File MIME type (e.g., \`application/pdf\`) |
| \`file_size\` | \`BIGINT\`       | YES      | -                    | File size in bytes                    |
| \`file_hash\` | \`VARCHAR(64)\`  | YES      | -                    | SHA-256 hash for deduplication        |
| \`status\`    | \`VARCHAR(20)\`  | NO       | \`'PENDING'\`          | Document processing status            |
| \`metadata\`  | \`JSON\`         | YES      | -                    | Additional metadata in JSON format    |
| \`created_at\`| \`TIMESTAMP\`    | NO       | \`CURRENT_TIMESTAMP\`  | Creation date/time                    |
| \`updated_at\`| \`TIMESTAMP\`    | NO       | \`CURRENT_TIMESTAMP\`  | Last update date/time                 |
| \`indexed_at\`| \`TIMESTAMP\`    | YES      | -                    | Indexing completion date/time         |

### Primary Key

- \`id\` (CHAR(36), automatically generated)

### Indexes

| Index Name             | Column(s)  | Type    | Rationale                            |
|------------------------|------------|---------|--------------------------------------|
| \`idx_documents_status\` | \`status\`   | B-tree  | Filtering documents by status        |
| \`idx_documents_hash\`   | \`file_hash\`| B-tree  | Fast lookup for deduplication        |

### Status Field Values

| Value        | Description                                              |
|--------------|----------------------------------------------------------|
| \`PENDING\`    | Document uploaded, awaiting processing                   |
| \`PROCESSING\` | Processing in progress (text extraction, chunking)      |
| \`INDEXED\`    | Indexing completed in the vector store                   |
| \`ERROR\`      | Error during processing                                  |

### Metadata Field (JSON)

The \`metadata\` field is of type JSON and allows flexible storage of heterogeneous metadata. Content examples:

\`\`\`json
{
  "author": "Author Name",
  "pageCount": 42,
  "language": "it",
  "extractedTitle": "Document Title",
  "chunkCount": 15
}
\`\`\`

### Creation SQL

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

---

## 3. Table folder_configs

Stores the configuration of local folders monitored by the system for automatic scanning of new documents.

### Structure

| Column           | Type            | Nullable | Default              | Description                            |
|------------------|-----------------|----------|----------------------|----------------------------------------|
| \`id\`             | \`CHAR(36)\`      | NO       | \`(UUID())\`           | Unique identifier                      |
| \`path\`           | \`VARCHAR(1000)\` | NO       | -                    | Absolute folder path                   |
| \`recursive\`      | \`BOOLEAN\`       | NO       | \`TRUE\`               | Recursive scanning of subfolders       |
| \`watch_enabled\`  | \`BOOLEAN\`       | NO       | \`FALSE\`              | Real-time monitoring enabled           |
| \`last_scan_at\`   | \`TIMESTAMP\`     | YES      | -                    | Date/time of last scan                 |
| \`document_count\` | \`INTEGER\`       | NO       | \`0\`                  | Number of documents found              |

### Primary Key

- \`id\` (CHAR(36), automatically generated)

### Creation SQL

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

---

## 4. Table llm_usage

Stores LLM provider usage records for cost tracking, performance monitoring, and usage analysis.

### Structure

| Column              | Type               | Nullable | Default              | Description                                           |
|---------------------|--------------------|----------|----------------------|-------------------------------------------------------|
| \`id\`                | \`CHAR(36)\`         | NO       | \`(UUID())\`           | Unique identifier                                     |
| \`provider\`          | \`VARCHAR(20)\`      | NO       | -                    | Provider name (OLLAMA, OPENAI, ANTHROPIC, GOOGLE)     |
| \`model\`             | \`VARCHAR(100)\`     | YES      | -                    | Name of the model used                                |
| \`prompt_tokens\`     | \`INTEGER\`          | NO       | \`0\`                  | Tokens in the prompt                                  |
| \`completion_tokens\` | \`INTEGER\`          | NO       | \`0\`                  | Tokens in the response                                |
| \`total_tokens\`      | \`INTEGER\`          | NO       | \`0\`                  | Total tokens (prompt + completion)                    |
| \`cost\`              | \`DOUBLE\`           | NO       | \`0\`                  | Estimated call cost                                   |
| \`latency_ms\`        | \`BIGINT\`           | NO       | \`0\`                  | Latency in milliseconds                               |
| \`timestamp\`         | \`TIMESTAMP\`        | NO       | \`CURRENT_TIMESTAMP\`  | Call date/time                                        |

### Primary Key

- \`id\` (CHAR(36), automatically generated)

### Indexes

| Index Name                | Column(s)   | Type    | Rationale                                |
|---------------------------|-------------|---------|------------------------------------------|
| \`idx_llm_usage_timestamp\` | \`timestamp\` | B-tree  | Queries by time range                    |
| \`idx_llm_usage_provider\`  | \`provider\`  | B-tree  | Aggregation and filtering by provider    |

### Creation SQL

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

---

## 5. Table conversations

Stores the user's chat conversations. Each conversation can contain multiple messages.

### Structure

| Column      | Type            | Nullable | Default              | Description                           |
|-------------|-----------------|----------|----------------------|---------------------------------------|
| \`id\`        | \`CHAR(36)\`      | NO       | \`(UUID())\`           | Unique identifier                     |
| \`title\`     | \`VARCHAR(500)\`  | YES      | -                    | Conversation title                    |
| \`created_at\`| \`TIMESTAMP\`     | NO       | \`CURRENT_TIMESTAMP\`  | Creation date/time                    |
| \`updated_at\`| \`TIMESTAMP\`     | NO       | \`CURRENT_TIMESTAMP\`  | Last update date/time                 |

### Primary Key

- \`id\` (CHAR(36), automatically generated)

### Creation SQL

\`\`\`sql
CREATE TABLE conversations (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    title VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
\`\`\`

---

## 6. Table chat_messages

Stores individual messages within conversations. Each message is associated with a conversation and has a role (user or assistant).

### Structure

| Column            | Type           | Nullable | Default              | Description                           |
|-------------------|----------------|----------|----------------------|---------------------------------------|
| \`id\`              | \`CHAR(36)\`     | NO       | \`(UUID())\`           | Unique identifier                     |
| \`conversation_id\` | \`CHAR(36)\`     | NO       | -                    | Reference to the conversation         |
| \`role\`            | \`VARCHAR(20)\`  | NO       | -                    | Sender role (USER, ASSISTANT)         |
| \`content\`         | \`TEXT\`         | NO       | -                    | Text content of the message           |
| \`created_at\`      | \`TIMESTAMP\`    | NO       | \`CURRENT_TIMESTAMP\`  | Creation date/time                    |

### Primary Key

- \`id\` (CHAR(36), automatically generated)

### Foreign Key

| Column            | Referenced Table | Referenced Column | On Delete |
|-------------------|------------------|-------------------|-----------|
| \`conversation_id\` | \`conversations\`  | \`id\`              | \`CASCADE\` |

The \`ON DELETE CASCADE\` clause ensures that deleting a conversation automatically deletes all associated messages.

### Indexes

| Index Name                       | Column(s)        | Type    | Rationale                                |
|----------------------------------|------------------|---------|------------------------------------------|
| \`idx_chat_messages_conversation\` | \`conversation_id\`| B-tree  | Join and filtering by conversation       |

### Creation SQL

\`\`\`sql
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

---

## 7. Table webhooks

Stores webhook configuration for notifications and external automations (integration with n8n).

### Structure

| Column      | Type           | Nullable | Default              | Description                           |
|-------------|----------------|----------|----------------------|---------------------------------------|
| \`id\`        | \`CHAR(36)\`     | NO       | \`(UUID())\`           | Unique identifier                     |
| \`name\`      | \`VARCHAR(200)\` | NO       | -                    | Descriptive webhook name              |
| \`url\`       | \`VARCHAR(1000)\`| NO       | -                    | Webhook destination URL               |
| \`event_type\`| \`VARCHAR(30)\`  | NO       | -                    | Event type that triggers the webhook  |
| \`active\`    | \`BOOLEAN\`      | NO       | \`TRUE\`               | Activation status                     |

### Primary Key

- \`id\` (CHAR(36), automatically generated)

### Event Type Field Values

| Value                  | Description                                         |
|------------------------|-----------------------------------------------------|
| \`DOCUMENT_UPLOADED\`    | Document uploaded to the system                     |
| \`DOCUMENT_INDEXED\`     | Document indexed in the vector store                |
| \`DOCUMENT_ERROR\`       | Error during document processing                    |

### Creation SQL

\`\`\`sql
CREATE TABLE webhooks (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(200) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
\`\`\`

---

## 8. ER Diagram

\`\`\`
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
\`\`\`

### Relationships

- **conversations -> chat_messages**: 1:N relationship. A conversation contains zero or more messages. Deleting a conversation causes cascade deletion of all associated messages (\`ON DELETE CASCADE\`).

The \`documents\`, \`folder_configs\`, \`llm_usage\`, and \`webhooks\` tables are standalone entities with no direct relationships to other tables.

---

## 9. Conventions

### Naming

- **Tables**: \`snake_case\`, plural (e.g., \`documents\`, \`chat_messages\`).
- **Columns**: \`snake_case\` (e.g., \`file_size\`, \`created_at\`).
- **Indexes**: \`idx_{table}_{column}\` (e.g., \`idx_documents_status\`).
- **Primary keys**: \`id\` of type \`CHAR(36)\` for all tables.
- **Timestamps**: \`TIMESTAMP\` type with \`DEFAULT CURRENT_TIMESTAMP\` for audit columns.

### Data Types

- **Identifiers**: \`CHAR(36)\` with database-side generation via \`UUID()\`.
- **Strings**: \`VARCHAR(n)\` with specified maximum length.
- **Long text**: \`TEXT\` for content without length limits (e.g., chat messages).
- **JSON**: \`JSON\` for semi-structured data (document metadata).
- **Booleans**: \`BOOLEAN\` with \`DEFAULT TRUE\` or \`DEFAULT FALSE\`.
- **Numeric**: \`INTEGER\`, \`BIGINT\`, \`DOUBLE\` based on required precision.
`;
