export const content = `# Flyway Migrations

**Project:** LocalMind
**Version:** 0.1.0
**Date:** 2026-02-09
**Migration directory:** \`localmind-app/src/main/resources/db/migration/\`

---

## Table of Contents

1. [Overview](#1-overview)
2. [Configuration](#2-configuration)
3. [V1 - Table documents](#3-v1---table-documents)
4. [V2 - Table folder_configs](#4-v2---table-folder_configs)
5. [V3 - Table llm_usage](#5-v3---table-llm_usage)
6. [V4 - Tables conversations and chat_messages](#6-v4---tables-conversations-and-chat_messages)
7. [V5 - Table webhooks](#7-v5---table-webhooks)
8. [Migration Summary](#8-migration-summary)
9. [Operational Procedures](#9-operational-procedures)

---

## 1. Overview

Flyway is the database migration system adopted by LocalMind to manage the evolution of the MySQL schema in a versioned and repeatable manner. Each schema modification is tracked in an SQL file with an incremental version, ensuring:

- **Versioning**: each migration has a unique and sequential version number.
- **Repeatability**: migration application is idempotent; Flyway tracks already executed migrations in the internal \`flyway_schema_history\` table.
- **Consistency**: integration with Hibernate's \`ddl-auto: validate\` ensures that JPA entities are always aligned with the schema.
- **Automation**: migrations are automatically executed at Spring Boot application startup.

### Naming Convention

Migration files follow the convention:

\`\`\`
V{number}__{description}.sql
\`\`\`

- \`V\`: mandatory prefix for versioned (non-repeatable) migrations.
- \`{number}\`: incremental version number (1, 2, 3, ...).
- \`__\`: double underscore as separator.
- \`{description}\`: human-readable description in snake_case.

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

## 2. Configuration

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

### Parameters

| Parameter                       | Value                      | Description                                                            |
|---------------------------------|----------------------------|------------------------------------------------------------------------|
| \`spring.flyway.enabled\`         | \`true\`                     | Activates automatic migration execution at startup                     |
| \`spring.flyway.locations\`       | \`classpath:db/migration\`   | Path to SQL migration files                                            |
| \`spring.jpa.hibernate.ddl-auto\` | \`validate\`                 | Hibernate verifies entity-schema consistency without modifying the DB  |

### Maven Dependencies (localmind-app)

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

The \`flyway-mysql\` dependency is required for MySQL-specific support (e.g., handling types like \`JSON\`, functions like \`UUID()\`).

---

## 3. V1 - Table documents

### File: \`V1__create_documents_table.sql\`

### Rationale

Creates the main table for managing documents uploaded to the system. This table stores file metadata (name, path, MIME type, size, hash), processing status, and additional metadata in JSON format.

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

### Indexes Created

| Index                  | Column     | Rationale                                                                                                          |
|------------------------|------------|--------------------------------------------------------------------------------------------------------------------|
| \`idx_documents_status\` | \`status\`   | Filtering by status (PENDING, PROCESSING, INDEXED, ERROR); frequent query in the dashboard and batch processing    |
| \`idx_documents_hash\`   | \`file_hash\`| Lookup by SHA-256 hash to detect duplicates before upload                                                          |

### Constraints

- \`id\`: PRIMARY KEY with default value generated by \`UUID()\`.
- \`filename\`: NOT NULL, every document must have a file name.
- \`status\`: NOT NULL with DEFAULT \`'PENDING'\`, every document starts in the PENDING state.
- \`created_at\`: NOT NULL with DEFAULT \`CURRENT_TIMESTAMP\`.
- \`updated_at\`: NOT NULL with DEFAULT \`CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\`.

---

## 4. V2 - Table folder_configs

### File: \`V2__create_folder_configs_table.sql\`

### Rationale

Creates the table for configuring local folders monitored by the system. Each record represents a filesystem folder that the periodic scanning batch job will analyze to find new documents.

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

### Constraints

- \`id\`: PRIMARY KEY automatically generated.
- \`path\`: NOT NULL, absolute folder path.
- \`recursive\`: NOT NULL with DEFAULT \`TRUE\`, recursive scanning enabled by default.
- \`watch_enabled\`: NOT NULL with DEFAULT \`FALSE\`, real-time monitoring disabled by default.
- \`document_count\`: NOT NULL with DEFAULT \`0\`.

### Notes

- No additional indexes are present since the expected data volume for this table is small (order of tens of records).
- The \`path\` field does not have a \`UNIQUE\` constraint at the DDL level, but uniqueness is managed at the application level.

---

## 5. V3 - Table llm_usage

### File: \`V3__create_llm_usage_table.sql\`

### Rationale

Creates the table for tracking LLM provider usage. Each record represents a single call to a provider, with information on tokens consumed, estimated cost, and latency. Essential for cost monitoring and performance analysis.

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

### Indexes Created

| Index                     | Column     | Rationale                                                                                                                 |
|---------------------------|------------|---------------------------------------------------------------------------------------------------------------------------|
| \`idx_llm_usage_timestamp\` | \`timestamp\`| Queries by time range (e.g., usage in the last week, current month); essential for reports and dashboard                   |
| \`idx_llm_usage_provider\`  | \`provider\` | Aggregation by provider (e.g., total cost for OpenAI vs Ollama); filtering in the dashboard                               |

### Constraints

- All numeric fields (\`prompt_tokens\`, \`completion_tokens\`, \`total_tokens\`, \`cost\`, \`latency_ms\`) are NOT NULL with DEFAULT \`0\`.
- \`provider\`: NOT NULL, LLM provider identifier.
- \`timestamp\`: NOT NULL with DEFAULT \`CURRENT_TIMESTAMP\`.

### Note on the Cost Type

The \`cost\` field uses \`DOUBLE\` (IEEE 754 64-bit). For version 0.1.0, this precision is sufficient. In future versions, it could be migrated to \`DECIMAL(10,6)\` for exact financial calculations.

---

## 6. V4 - Tables conversations and chat_messages

### File: \`V4__create_conversations_table.sql\`

### Rationale

Creates the tables for the conversational chat system. The migration includes two related tables: \`conversations\` (container) and \`chat_messages\` (messages). The separation allows managing multiple conversations and maintaining message history.

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

### Indexes Created

| Index                             | Column            | Rationale                                            |
|-----------------------------------|-------------------|------------------------------------------------------|
| \`idx_chat_messages_conversation\`  | \`conversation_id\` | Join and filtering by conversation; every conversation opening requires loading all associated messages |

### Constraints

- \`conversations.title\`: nullable, the title can be automatically generated after the first message.
- \`chat_messages.conversation_id\`: NOT NULL, REFERENCES \`conversations(id)\` with \`ON DELETE CASCADE\`.
- \`chat_messages.role\`: NOT NULL, expected values: \`USER\`, \`ASSISTANT\`.
- \`chat_messages.content\`: NOT NULL, type \`TEXT\` for variable-length content.

### ON DELETE CASCADE

The \`ON DELETE CASCADE\` clause on the \`conversation_id\` foreign key ensures that deleting a conversation triggers the automatic deletion of all associated messages. This choice simplifies application logic and avoids orphaned records.

---

## 7. V5 - Table webhooks

### File: \`V5__create_webhooks_table.sql\`

### Rationale

Creates the table for configuring webhooks used for notifications and external automations, particularly integration with n8n. Each record defines a callback endpoint that is invoked when a specific event occurs.

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

### Constraints

- \`name\`: NOT NULL, descriptive webhook name.
- \`url\`: NOT NULL, full URL of the destination endpoint.
- \`event_type\`: NOT NULL, event type that triggers the webhook (e.g., \`DOCUMENT_UPLOADED\`, \`DOCUMENT_INDEXED\`, \`DOCUMENT_ERROR\`).
- \`active\`: NOT NULL with DEFAULT \`TRUE\`.

### Notes

- No additional indexes are present since the expected data volume is small.
- The \`event_type\` field uses \`VARCHAR(30)\` instead of a MySQL \`ENUM\` type for flexibility in adding new event types.

---

## 8. Migration Summary

| Version  | File                                   | Tables Created                   | Indexes Created                                     |
|----------|----------------------------------------|----------------------------------|-----------------------------------------------------|
| V1       | \`V1__create_documents_table.sql\`       | \`documents\`                      | \`idx_documents_status\`, \`idx_documents_hash\`        |
| V2       | \`V2__create_folder_configs_table.sql\`  | \`folder_configs\`                 | (none)                                              |
| V3       | \`V3__create_llm_usage_table.sql\`       | \`llm_usage\`                      | \`idx_llm_usage_timestamp\`, \`idx_llm_usage_provider\` |
| V4       | \`V4__create_conversations_table.sql\`   | \`conversations\`, \`chat_messages\` | \`idx_chat_messages_conversation\`                    |
| V5       | \`V5__create_webhooks_table.sql\`        | \`webhooks\`                       | (none)                                              |

### Totals

- **6 tables** created
- **5 indexes** B-tree
- **1 foreign key** with CASCADE delete
- **5 migration** SQL files

---

## 9. Operational Procedures

### Checking Migration Status

\`\`\`bash
# Access the database
mysql -h localhost -u localmind -plocalmind localmind

# Query the Flyway tracking table
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;
\`\`\`

### Creating a New Migration

1. Create a new file in the \`localmind-app/src/main/resources/db/migration/\` directory.
2. Follow the naming convention: \`V{next_number}__{description}.sql\`.
3. Write the DDL SQL.
4. Restart the application: Flyway will automatically apply the new migration.
5. Verify that \`ddl-auto: validate\` does not report errors (JPA entity alignment).

### Example

\`\`\`
V6__add_user_table.sql
\`\`\`

### Rollback

Flyway Community Edition does not support automatic rollback. In case of error:

1. Manually correct the schema in the database.
2. Update the \`flyway_schema_history\` table if necessary.
3. Create a new corrective migration (e.g., \`V6__fix_column_type.sql\`).
`;
