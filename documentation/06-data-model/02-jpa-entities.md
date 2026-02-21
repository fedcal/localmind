# JPA Entities

**Project:** LocalMind
**Version:** 0.1.0
**Date:** 2026-02-09
**Package:** `com.localmind.infrastructure.persistence.entity`

---

## Table of Contents

1. [Overview](#1-overview)
2. [DocumentEntity](#2-documententity)
3. [ConversationEntity](#3-conversationentity)
4. [ChatMessageEntity](#4-chatmessageentity)
5. [FolderConfigEntity](#5-folderconfigentity)
6. [LlmUsageEntity](#6-llmusageentity)
7. [Mapping Pattern: Hexagonal Adapter](#7-mapping-pattern-hexagonal-adapter)
8. [JPA Conventions](#8-jpa-conventions)

---

## 1. Overview

The JPA entities reside in the `localmind-infrastructure` module and represent the Object-Relational mapping between Java models and MySQL tables. Each entity is annotated with standard JPA annotations (`jakarta.persistence.*`) and uses Lombok for boilerplate reduction.

### Mapping Architecture

In the context of LocalMind's hexagonal architecture, JPA entities are adapters of the infrastructure layer. They do not coincide with domain models: the mapping between JPA entities and domain models occurs through adapter classes that implement the output ports defined in the domain.

```
Domain Model (localmind-domain)
       ^
       |  Mapping (adapter)
       v
JPA Entity (localmind-infrastructure)
       ^
       |  Hibernate ORM
       v
MySQL Table
```

### Package structure

```
com.localmind.infrastructure.persistence/
    entity/
        DocumentEntity.java
        ConversationEntity.java
        ChatMessageEntity.java
        FolderConfigEntity.java
        LlmUsageEntity.java
    repository/
        JpaDocumentRepository.java
        JpaConversationRepository.java
        JpaFolderConfigRepository.java
        JpaLlmUsageRepository.java
    adapter/
        LlmUsageRepositoryAdapter.java
        ...
```

---

## 2. DocumentEntity

### Class

```java
@Entity
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String filename;

    @Column(length = 1000)
    private String filePath;

    @Column(length = 100)
    private String mimeType;

    private Long fileSize;

    @Column(length = 64)
    private String fileHash;

    @Column(nullable = false, length = 20)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> metadata;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;
    private Instant indexedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
```

### Column Mapping

| Java Field   | DB Column    | Java Type              | SQL Type          | Notes                          |
|-------------|-------------|------------------------|-------------------|-------------------------------|
| `id`        | `id`        | `UUID`                 | `CHAR(36)`        | Generated with `GenerationType.UUID` |
| `filename`  | `filename`  | `String`               | `VARCHAR(500)`    | NOT NULL                       |
| `filePath`  | `file_path` | `String`               | `VARCHAR(1000)`   | Naming strategy: camelCase -> snake_case |
| `mimeType`  | `mime_type` | `String`               | `VARCHAR(100)`    | -                              |
| `fileSize`  | `file_size` | `Long`                 | `BIGINT`          | Wrapper type, nullable         |
| `fileHash`  | `file_hash` | `String`               | `VARCHAR(64)`     | SHA-256 hash                   |
| `status`    | `status`    | `String`               | `VARCHAR(20)`     | NOT NULL, default 'PENDING'    |
| `metadata`  | `metadata`  | `Map<String, Object>`  | `JSON`            | Hibernate `@JdbcTypeCode(SqlTypes.JSON)` |
| `createdAt` | `created_at`| `Instant`              | `TIMESTAMP`       | NOT NULL, managed by `@PrePersist` |
| `updatedAt` | `updated_at`| `Instant`              | `TIMESTAMP`       | Managed by `@PrePersist` and `@PreUpdate` |
| `indexedAt` | `indexed_at`| `Instant`              | `TIMESTAMP`       | Nullable, set after indexing   |

### Key Annotations

- **`@JdbcTypeCode(SqlTypes.JSON)`**: instructs Hibernate 6 to serialize/deserialize the `metadata` field as JSON, mapping it to the MySQL `JSON` type. Requires the imports `org.hibernate.annotations.JdbcTypeCode` and `org.hibernate.type.SqlTypes`.
- **`@Column(columnDefinition = "json")`**: specifies the DDL column type for Hibernate validation.
- **`@PrePersist`**: callback invoked before insertion; initializes `createdAt` and `updatedAt`.
- **`@PreUpdate`**: callback invoked before update; updates `updatedAt`.

### Note on the Status Field

The `status` field is mapped as `String` rather than `@Enumerated`. This choice allows:
- Flexibility in adding new statuses without modifying the Java enum.
- Direct compatibility with the `DEFAULT 'PENDING'` value defined in the DDL.
- Explicit mapping in the domain code via `Document.Status.valueOf(entity.getStatus())`.

### Mapping to the Domain Model

The mapping between `DocumentEntity` and the `Document` domain model occurs in controllers and adapters via `toDto()` / `toEntity()` methods. The `status` field is converted via `Document.Status.name()` (entity -> domain) and `Document.Status.valueOf()` (domain -> entity).

---

## 3. ConversationEntity

### Class

```java
@Entity
@Table(name = "conversations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<ChatMessageEntity> messages = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
```

### Relationship with ChatMessageEntity

The relationship is **bidirectional one-to-many**:

| JPA Property        | Value                      | Description                                                 |
|---------------------|----------------------------|-------------------------------------------------------------|
| `mappedBy`          | `"conversation"`           | The owning side is `ChatMessageEntity.conversation`         |
| `cascade`           | `CascadeType.ALL`          | All operations (persist, merge, remove) are propagated      |
| `orphanRemoval`     | `true`                     | Messages removed from the list are deleted from the DB      |
| `@OrderBy`          | `"createdAt ASC"`          | Messages are ordered chronologically                        |
| `@Builder.Default`  | `new ArrayList<>()`        | List initialization in the Lombok Builder pattern           |

### Notes on the Bidirectional Relationship

- **Owning side (ChatMessageEntity)**: contains the `@JoinColumn` and the `conversation_id` foreign key.
- **Inverse side (ConversationEntity)**: contains `@OneToMany(mappedBy = "conversation")`.
- **Cascade ALL**: inserting a conversation with messages in the list automatically persists them.
- **Orphan removal**: removing a message from the `messages` list causes a `DELETE` in the database.

---

## 4. ChatMessageEntity

### Class

```java
@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ConversationEntity conversation;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
```

### Column Mapping

| Java Field     | DB Column         | Java Type                | SQL Type       | Notes                               |
|----------------|-------------------|--------------------------|----------------|------------------------------------|
| `id`           | `id`              | `UUID`                   | `CHAR(36)`     | Generated with `GenerationType.UUID` |
| `role`         | `role`            | `String`                 | `VARCHAR(20)`  | Values: `USER`, `ASSISTANT`        |
| `content`      | `content`         | `String`                 | `TEXT`         | Message content                    |
| `conversation` | `conversation_id` | `ConversationEntity`     | `CHAR(36)` (FK)| Lazy loading                       |
| `createdAt`    | `created_at`      | `Instant`                | `TIMESTAMP`    | Managed by `@PrePersist`           |

### Key Annotations

- **`@ManyToOne(fetch = FetchType.LAZY)`**: loading of the associated conversation is lazy to avoid N+1 queries.
- **`@JoinColumn(name = "conversation_id", nullable = false)`**: specifies the join column name and the NOT NULL constraint.
- **`@Column(columnDefinition = "TEXT")`**: forces the SQL type `TEXT` for content without length limits.

---

## 5. FolderConfigEntity

### Class

```java
@Entity
@Table(name = "folder_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String path;

    @Builder.Default
    private boolean recursive = true;

    @Builder.Default
    private boolean watchEnabled = false;

    private Instant lastScanAt;
    private int documentCount;
}
```

### Notes

- **`@Builder.Default`**: essential for `boolean` fields with the Lombok Builder pattern. Without this annotation, `recursive` would be `false` (Java default for `boolean`) instead of `true` as defined in the DDL.
- **Primitive mapping**: `recursive` and `watchEnabled` are primitive types (`boolean`, `int`), not wrappers. This is consistent with the `NOT NULL` and `DEFAULT` constraints in the schema.

---

## 6. LlmUsageEntity

### Class

```java
@Entity
@Table(name = "llm_usage")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmUsageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(length = 100)
    private String model;

    private int promptTokens;
    private int completionTokens;
    private int totalTokens;

    private double cost;
    private long latencyMs;

    @Column(nullable = false)
    private Instant timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
```

### Column Mapping

| Java Field        | DB Column           | Java Type  | SQL Type            | Notes                                    |
|-------------------|---------------------|------------|---------------------|------------------------------------------|
| `id`              | `id`                | `UUID`     | `CHAR(36)`          | Generated PK                             |
| `provider`        | `provider`          | `String`   | `VARCHAR(20)`       | OLLAMA, OPENAI, ANTHROPIC, GOOGLE        |
| `model`           | `model`             | `String`   | `VARCHAR(100)`      | Model name (e.g., `llama3.2`)            |
| `promptTokens`    | `prompt_tokens`     | `int`      | `INTEGER`           | Tokens in the prompt                     |
| `completionTokens`| `completion_tokens` | `int`      | `INTEGER`           | Tokens in the response                   |
| `totalTokens`     | `total_tokens`      | `int`      | `INTEGER`           | Total tokens                             |
| `cost`            | `cost`              | `double`   | `DOUBLE`            | Estimated cost                           |
| `latencyMs`       | `latency_ms`        | `long`     | `BIGINT`            | Latency in ms                            |
| `timestamp`       | `timestamp`         | `Instant`  | `TIMESTAMP`         | NOT NULL, with fallback in `@PrePersist` |

### Note on the @PrePersist Callback

The `@PrePersist` callback of `LlmUsageEntity` includes an `if (timestamp == null)` condition: this allows setting the timestamp externally before persist. If not set, `Instant.now()` is used as a fallback.

---

## 7. Mapping Pattern: Hexagonal Adapter

### LlmUsageRepositoryAdapter

The `LlmUsageRepositoryAdapter` is a concrete example of the hexagonal adapter pattern used in the project to separate the domain from the infrastructure.

#### Structure

```
Domain Port (localmind-domain)             Adapter (localmind-infrastructure)
+-----------------------------+            +--------------------------------+
| LlmUsageRepository          |            | LlmUsageRepositoryAdapter      |
| (interface)                 |  <------   | implements LlmUsageRepository  |
|                             |            |                                |
| + save(UsageRecord)         |            | + save(UsageRecord)            |
| + findByDateRange(from, to) |            | + findByDateRange(from, to)    |
| + findAll()                 |            | + findAll()                    |
+-----------------------------+            +-----+--------------------------+
                                                 |
                                                 | uses
                                                 v
                                           +--------------------------------+
                                           | JpaLlmUsageRepository          |
                                           | extends JpaRepository          |
                                           | <LlmUsageEntity, UUID>         |
                                           +--------------------------------+
```

#### Adapter Code

```java
@Component
public class LlmUsageRepositoryAdapter implements LlmUsageRepository {

    private final JpaLlmUsageRepository jpaRepository;

    public LlmUsageRepositoryAdapter(JpaLlmUsageRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(UsageRecord record) {
        LlmUsageEntity entity = LlmUsageEntity.builder()
                .provider(record.getProvider().name())
                .model(record.getModel())
                .promptTokens(record.getTokenUsage() != null
                    ? record.getTokenUsage().getPromptTokens() : 0)
                .completionTokens(record.getTokenUsage() != null
                    ? record.getTokenUsage().getCompletionTokens() : 0)
                .totalTokens(record.getTokenUsage() != null
                    ? record.getTokenUsage().getTotalTokens() : 0)
                .cost(record.getCost())
                .latencyMs(record.getLatencyMs())
                .timestamp(record.getTimestamp())
                .build();
        jpaRepository.save(entity);
    }

    @Override
    public List<UsageRecord> findByDateRange(Instant from, Instant to) {
        return jpaRepository.findByTimestampBetween(from, to).stream()
                .map(this::toUsageRecord)
                .toList();
    }

    @Override
    public List<UsageRecord> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toUsageRecord)
                .toList();
    }

    private UsageRecord toUsageRecord(LlmUsageEntity entity) {
        return UsageRecord.builder()
                .id(entity.getId().toString())
                .provider(LlmProvider.valueOf(entity.getProvider()))
                .model(entity.getModel())
                .tokenUsage(TokenUsage.builder()
                        .promptTokens(entity.getPromptTokens())
                        .completionTokens(entity.getCompletionTokens())
                        .totalTokens(entity.getTotalTokens())
                        .build())
                .cost(entity.getCost())
                .latencyMs(entity.getLatencyMs())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
```

#### Conversion Flow

| Direction                         | From                | To                   | Method            |
|-----------------------------------|---------------------|----------------------|-------------------|
| Domain -> Infrastructure          | `UsageRecord`       | `LlmUsageEntity`    | `save()` inline   |
| Infrastructure -> Domain          | `LlmUsageEntity`   | `UsageRecord`        | `toUsageRecord()` |

#### Key Points of the Pattern

- **The `LlmUsageRepository` interface** is defined in the `localmind-domain` module (output port).
- **The `LlmUsageRepositoryAdapter` adapter** is defined in the `localmind-infrastructure` module (port implementation).
- **The domain does not know about JPA**: it has no dependencies on `jakarta.persistence`, Hibernate, or Spring Data.
- **Conversion is explicit**: `LlmProvider.valueOf(entity.getProvider())` for enums, `entity.getId().toString()` for UUID -> String.
- **Null safety**: `record.getTokenUsage() != null` check before accessing token fields.

---

## 8. JPA Conventions

### Naming Strategy

Hibernate uses Spring Boot's `PhysicalNamingStrategyStandardImpl` which automatically converts:
- Java `camelCase` -> SQL `snake_case` (e.g., `filePath` -> `file_path`).
- Class names -> table names (e.g., `DocumentEntity` -> explicitly mapped with `@Table(name = "documents")`).

### UUID Generation

All entities use `@GeneratedValue(strategy = GenerationType.UUID)` from JPA 3.1+, which delegates generation to the provider (Hibernate generates UUID v4 on the application side or uses `UUID()` on the MySQL database side).

### Lifecycle Callbacks

| Callback       | Entities                                                                  | Operation                               |
|----------------|---------------------------------------------------------------------------|----------------------------------------|
| `@PrePersist`  | DocumentEntity, ConversationEntity, ChatMessageEntity, LlmUsageEntity     | Initialization of creation timestamp   |
| `@PreUpdate`   | DocumentEntity, ConversationEntity                                        | Update of modification timestamp       |

### Common Lombok Annotations

All entities share the following Lombok annotations:

| Annotation             | Purpose                                                       |
|------------------------|---------------------------------------------------------------|
| `@Data`                | Generates getter, setter, equals, hashCode, toString          |
| `@Builder`             | Builder pattern for fluent construction                       |
| `@NoArgsConstructor`   | No-argument constructor (required by JPA)                     |
| `@AllArgsConstructor`  | All-argument constructor (required by `@Builder`)             |
| `@Builder.Default`     | Default values in builders                                    |
