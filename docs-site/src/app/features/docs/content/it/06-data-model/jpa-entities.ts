export const content = `# Entita' JPA

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09
**Package:** \`com.localmind.infrastructure.persistence.entity\`

---

## Indice

1. [Panoramica](#1-panoramica)
2. [DocumentEntity](#2-documententity)
3. [ConversationEntity](#3-conversationentity)
4. [ChatMessageEntity](#4-chatmessageentity)
5. [FolderConfigEntity](#5-folderconfigentity)
6. [LlmUsageEntity](#6-llmusageentity)
7. [Pattern di Mapping: Adapter Esagonale](#7-pattern-di-mapping-adapter-esagonale)
8. [Convenzioni JPA](#8-convenzioni-jpa)

---

## 1. Panoramica

Le entita' JPA risiedono nel modulo \`localmind-infrastructure\` e rappresentano il mapping Object-Relational tra i modelli Java e le tabelle MySQL. Ogni entita' e' annotata con le annotazioni JPA standard (\`jakarta.persistence.*\`) e utilizza Lombok per la riduzione del boilerplate.

### Architettura di mapping

Nel contesto dell'architettura esagonale di LocalMind, le entita' JPA sono adapter del layer infrastrutturale. Non coincidono con i modelli di dominio: il mapping tra entita' JPA e modelli di dominio avviene tramite adapter classes che implementano le porte di output definite nel dominio.

\`\`\`
Domain Model (localmind-domain)
       ^
       |  Mapping (adapter)
       v
JPA Entity (localmind-infrastructure)
       ^
       |  Hibernate ORM
       v
MySQL Table
\`\`\`

### Package structure

\`\`\`
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
\`\`\`

---

## 2. DocumentEntity

### Classe

\`\`\`java
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
\`\`\`

### Mapping colonne

| Campo Java   | Colonna DB   | Tipo Java             | Tipo SQL          | Note                          |
|-------------|-------------|------------------------|-------------------|-------------------------------|
| \`id\`        | \`id\`        | \`UUID\`                 | \`CHAR(36)\`        | Generato con \`GenerationType.UUID\` |
| \`filename\`  | \`filename\`  | \`String\`               | \`VARCHAR(500)\`    | NOT NULL                       |
| \`filePath\`  | \`file_path\` | \`String\`               | \`VARCHAR(1000)\`   | Naming strategy: camelCase -> snake_case |
| \`mimeType\`  | \`mime_type\` | \`String\`               | \`VARCHAR(100)\`    | -                              |
| \`fileSize\`  | \`file_size\` | \`Long\`                 | \`BIGINT\`          | Wrapper type, nullable         |
| \`fileHash\`  | \`file_hash\` | \`String\`               | \`VARCHAR(64)\`     | Hash SHA-256                   |
| \`status\`    | \`status\`    | \`String\`               | \`VARCHAR(20)\`     | NOT NULL, default 'PENDING'    |
| \`metadata\`  | \`metadata\`  | \`Map<String, Object>\`  | \`JSON\`            | Hibernate \`@JdbcTypeCode(SqlTypes.JSON)\` |
| \`createdAt\` | \`created_at\`| \`Instant\`              | \`TIMESTAMP\`       | NOT NULL, gestito da \`@PrePersist\` |
| \`updatedAt\` | \`updated_at\`| \`Instant\`              | \`TIMESTAMP\`       | Gestito da \`@PrePersist\` e \`@PreUpdate\` |
| \`indexedAt\` | \`indexed_at\`| \`Instant\`              | \`TIMESTAMP\`       | Nullable, impostato post-indicizzazione |

### Annotazioni chiave

- **\`@JdbcTypeCode(SqlTypes.JSON)\`**: istruisce Hibernate 6 a serializzare/deserializzare il campo \`metadata\` come JSON, mappandolo al tipo \`JSON\` di MySQL. Richiede l'import \`org.hibernate.annotations.JdbcTypeCode\` e \`org.hibernate.type.SqlTypes\`.
- **\`@Column(columnDefinition = "json")\`**: specifica il tipo di colonna DDL per la validazione di Hibernate.
- **\`@PrePersist\`**: callback invocato prima dell'inserimento; inizializza \`createdAt\` e \`updatedAt\`.
- **\`@PreUpdate\`**: callback invocato prima dell'aggiornamento; aggiorna \`updatedAt\`.

### Nota sul campo status

Il campo \`status\` e' mappato come \`String\` anziche' come \`@Enumerated\`. Questa scelta consente:
- Flessibilita' nell'aggiunta di nuovi stati senza modificare l'enum Java.
- Compatibilita' diretta con il valore \`DEFAULT 'PENDING'\` definito nel DDL.
- Mapping esplicito nel codice di dominio tramite \`Document.Status.valueOf(entity.getStatus())\`.

### Mapping verso il modello di dominio

Il mapping tra \`DocumentEntity\` e il modello di dominio \`Document\` avviene nei controller e negli adapter tramite metodi \`toDto()\` / \`toEntity()\`. Il campo \`status\` viene convertito tramite \`Document.Status.name()\` (entita' -> dominio) e \`Document.Status.valueOf()\` (dominio -> entita').

---

## 3. ConversationEntity

### Classe

\`\`\`java
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
\`\`\`

### Relazione con ChatMessageEntity

La relazione e' di tipo **bidirezionale one-to-many**:

| Proprieta' JPA      | Valore                     | Descrizione                                                 |
|---------------------|----------------------------|-------------------------------------------------------------|
| \`mappedBy\`          | \`"conversation"\`           | Il lato owning e' \`ChatMessageEntity.conversation\`          |
| \`cascade\`           | \`CascadeType.ALL\`          | Tutte le operazioni (persist, merge, remove) sono propagate |
| \`orphanRemoval\`     | \`true\`                     | I messaggi rimossi dalla lista vengono eliminati dal DB     |
| \`@OrderBy\`          | \`"createdAt ASC"\`          | I messaggi sono ordinati cronologicamente                   |
| \`@Builder.Default\`  | \`new ArrayList<>()\`        | Inizializzazione della lista nel pattern Builder di Lombok  |

### Note sulla relazione bidirezionale

- **Lato owning (ChatMessageEntity)**: contiene la \`@JoinColumn\` e la chiave esterna \`conversation_id\`.
- **Lato inverso (ConversationEntity)**: contiene \`@OneToMany(mappedBy = "conversation")\`.
- **Cascade ALL**: l'inserimento di una conversazione con messaggi nella lista li persiste automaticamente.
- **Orphan removal**: la rimozione di un messaggio dalla lista \`messages\` causa il \`DELETE\` nel database.

---

## 4. ChatMessageEntity

### Classe

\`\`\`java
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
\`\`\`

### Mapping colonne

| Campo Java     | Colonna DB        | Tipo Java                | Tipo SQL       | Note                               |
|----------------|-------------------|--------------------------|----------------|------------------------------------|
| \`id\`           | \`id\`              | \`UUID\`                   | \`CHAR(36)\`     | Generato con \`GenerationType.UUID\` |
| \`role\`         | \`role\`            | \`String\`                 | \`VARCHAR(20)\`  | Valori: \`USER\`, \`ASSISTANT\`        |
| \`content\`      | \`content\`         | \`String\`                 | \`TEXT\`         | Contenuto del messaggio            |
| \`conversation\` | \`conversation_id\` | \`ConversationEntity\`     | \`CHAR(36)\` (FK)| Lazy loading                       |
| \`createdAt\`    | \`created_at\`      | \`Instant\`                | \`TIMESTAMP\`    | Gestito da \`@PrePersist\`           |

### Annotazioni chiave

- **\`@ManyToOne(fetch = FetchType.LAZY)\`**: il caricamento della conversazione associata e' lazy per evitare query N+1.
- **\`@JoinColumn(name = "conversation_id", nullable = false)\`**: specifica il nome della colonna di join e il vincolo NOT NULL.
- **\`@Column(columnDefinition = "TEXT")\`**: forza il tipo SQL \`TEXT\` per contenuti senza limite di lunghezza.

---

## 5. FolderConfigEntity

### Classe

\`\`\`java
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
\`\`\`

### Note

- **\`@Builder.Default\`**: essenziale per i campi \`boolean\` con il pattern Builder di Lombok. Senza questa annotazione, \`recursive\` sarebbe \`false\` (default Java per \`boolean\`) anziche' \`true\` come definito nel DDL.
- **Mapping primitivi**: \`recursive\` e \`watchEnabled\` sono tipi primitivi (\`boolean\`, \`int\`), non wrapper. Questo e' coerente con i vincoli \`NOT NULL\` e \`DEFAULT\` nello schema.

---

## 6. LlmUsageEntity

### Classe

\`\`\`java
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
\`\`\`

### Mapping colonne

| Campo Java        | Colonna DB          | Tipo Java  | Tipo SQL            | Note                                    |
|-------------------|---------------------|------------|---------------------|-----------------------------------------|
| \`id\`              | \`id\`                | \`UUID\`     | \`CHAR(36)\`          | PK generato                             |
| \`provider\`        | \`provider\`          | \`String\`   | \`VARCHAR(20)\`       | OLLAMA, OPENAI, ANTHROPIC, GOOGLE       |
| \`model\`           | \`model\`             | \`String\`   | \`VARCHAR(100)\`      | Nome modello (es. \`llama3.2\`)           |
| \`promptTokens\`    | \`prompt_tokens\`     | \`int\`      | \`INTEGER\`           | Token nel prompt                        |
| \`completionTokens\`| \`completion_tokens\` | \`int\`      | \`INTEGER\`           | Token nella risposta                    |
| \`totalTokens\`     | \`total_tokens\`      | \`int\`      | \`INTEGER\`           | Token totali                            |
| \`cost\`            | \`cost\`              | \`double\`   | \`DOUBLE\`            | Costo stimato                           |
| \`latencyMs\`       | \`latency_ms\`        | \`long\`     | \`BIGINT\`            | Latenza in ms                           |
| \`timestamp\`       | \`timestamp\`         | \`Instant\`  | \`TIMESTAMP\`         | NOT NULL, con fallback in \`@PrePersist\` |

### Nota sul callback @PrePersist

Il callback \`@PrePersist\` di \`LlmUsageEntity\` include una condizione \`if (timestamp == null)\`: questo consente di impostare il timestamp esternamente prima del persist. Se non impostato, viene utilizzato \`Instant.now()\` come fallback.

---

## 7. Pattern di Mapping: Adapter Esagonale

### LlmUsageRepositoryAdapter

Il \`LlmUsageRepositoryAdapter\` e' un esempio concreto del pattern adapter esagonale utilizzato nel progetto per separare il dominio dall'infrastruttura.

#### Struttura

\`\`\`
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
                                                 | utilizza
                                                 v
                                           +--------------------------------+
                                           | JpaLlmUsageRepository          |
                                           | extends JpaRepository          |
                                           | <LlmUsageEntity, UUID>         |
                                           +--------------------------------+
\`\`\`

#### Codice dell'adapter

\`\`\`java
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
\`\`\`

#### Flusso di conversione

| Direzione                         | Da                  | A                    | Metodo            |
|-----------------------------------|---------------------|----------------------|-------------------|
| Dominio -> Infrastruttura         | \`UsageRecord\`       | \`LlmUsageEntity\`     | \`save()\` inline   |
| Infrastruttura -> Dominio         | \`LlmUsageEntity\`    | \`UsageRecord\`        | \`toUsageRecord()\` |

#### Punti chiave del pattern

- **L'interfaccia \`LlmUsageRepository\`** e' definita nel modulo \`localmind-domain\` (porta di output).
- **L'adapter \`LlmUsageRepositoryAdapter\`** e' definito nel modulo \`localmind-infrastructure\` (implementazione della porta).
- **Il dominio non conosce JPA**: non ha dipendenze da \`jakarta.persistence\`, Hibernate o Spring Data.
- **La conversione e' esplicita**: \`LlmProvider.valueOf(entity.getProvider())\` per enum, \`entity.getId().toString()\` per UUID -> String.
- **Null safety**: controllo \`record.getTokenUsage() != null\` prima dell'accesso ai campi token.

---

## 8. Convenzioni JPA

### Naming strategy

Hibernate utilizza la \`PhysicalNamingStrategyStandardImpl\` di Spring Boot che converte automaticamente:
- \`camelCase\` Java -> \`snake_case\` SQL (es. \`filePath\` -> \`file_path\`).
- Nomi di classe -> nomi di tabella (es. \`DocumentEntity\` -> mappato esplicitamente con \`@Table(name = "documents")\`).

### Generazione UUID

Tutte le entita' utilizzano \`@GeneratedValue(strategy = GenerationType.UUID)\` di JPA 3.1+, che delega la generazione al provider (Hibernate genera UUID v4 lato applicazione oppure utilizza \`UUID()\` lato database MySQL).

### Lifecycle callbacks

| Callback       | Entita'                                                               | Operazione                              |
|----------------|-----------------------------------------------------------------------|-----------------------------------------|
| \`@PrePersist\`  | DocumentEntity, ConversationEntity, ChatMessageEntity, LlmUsageEntity | Inizializzazione timestamp di creazione |
| \`@PreUpdate\`   | DocumentEntity, ConversationEntity                                    | Aggiornamento timestamp di modifica     |

### Annotazioni Lombok comuni

Tutte le entita' condividono le seguenti annotazioni Lombok:

| Annotazione            | Scopo                                                         |
|------------------------|---------------------------------------------------------------|
| \`@Data\`                | Genera getter, setter, equals, hashCode, toString             |
| \`@Builder\`             | Pattern builder per costruzione fluente                       |
| \`@NoArgsConstructor\`   | Costruttore senza argomenti (richiesto da JPA)                |
| \`@AllArgsConstructor\`  | Costruttore con tutti gli argomenti (richiesto da \`@Builder\`) |
| \`@Builder.Default\`     | Valori di default nei builder                                 |
`;
