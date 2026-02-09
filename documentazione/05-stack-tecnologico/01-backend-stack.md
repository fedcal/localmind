# Stack Tecnologico Backend

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09
**Modulo:** localmind-backend

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Java 17](#2-java-17)
3. [Spring Boot 3.4.2](#3-spring-boot-342)
4. [Spring AI 1.0.0](#4-spring-ai-100)
5. [Spring Data JPA / Hibernate](#5-spring-data-jpa--hibernate)
6. [Spring Security](#6-spring-security)
7. [Spring Batch](#7-spring-batch)
8. [Spring Retry](#8-spring-retry)
9. [PostgreSQL 16](#9-postgresql-16)
10. [Qdrant](#10-qdrant)
11. [Apache Tika 2.9.2](#11-apache-tika-292)
12. [Flyway](#12-flyway)
13. [MapStruct 1.6.3](#13-mapstruct-163)
14. [Lombok 1.18.36](#14-lombok-11836)
15. [Maven](#15-maven)
16. [Tabella Riepilogativa Dipendenze](#16-tabella-riepilogativa-dipendenze)

---

## 1. Panoramica

Il backend di LocalMind e' costruito su un'architettura multi-modulo Maven basata su Spring Boot 3.4.2 con Java 17. Il progetto adotta un'architettura esagonale (Hexagonal Architecture) con separazione netta tra dominio, infrastruttura e layer API. Le dipendenze sono gestite centralmente nel POM parent tramite `dependencyManagement` e BOM (Bill of Materials) per Spring AI.

La struttura multi-modulo comprende:

| Modulo                    | Descrizione                                              |
|---------------------------|----------------------------------------------------------|
| `localmind-domain`        | Entita' di dominio, porte e logica di business (zero dipendenze framework) |
| `localmind-infrastructure`| Adapter: database, client LLM, vector store, filesystem  |
| `localmind-api`           | Controller REST, DTO e mapper                            |
| `localmind-batch`         | Job Spring Batch per elaborazione documenti              |
| `localmind-app`           | Modulo applicativo eseguibile (aggregatore)              |

---

## 2. Java 17

| Proprieta'   | Valore                          |
|-------------|----------------------------------|
| **Nome**    | Java Development Kit (JDK)       |
| **Versione**| 17 (LTS)                        |
| **Scopo**   | Linguaggio e runtime di esecuzione |

### Motivazione della scelta

Java 17 e' stato selezionato in quanto versione Long-Term Support (LTS), garantendo stabilita' e supporto a lungo termine. Le funzionalita' chiave utilizzate nel progetto includono:

- **Records**: utilizzati per modelli di dominio immutabili e DTO dove appropriato.
- **Sealed Classes**: per definire gerarchie di tipo chiuse e sicure nel dominio.
- **Pattern Matching per instanceof**: semplifica il codice di type-checking e casting nei service layer.
- **Text Blocks**: per query SQL inline e template di testo multi-riga.
- **Enhanced Switch Expressions**: per logica condizionale concisa nella gestione dei provider LLM.

### Alternative considerate

| Alternativa | Motivo del rifiuto                                                |
|-------------|------------------------------------------------------------------|
| Java 21     | Funzionalita' avanzate (virtual threads) non necessarie per v0.1.0; compatibilita' con Spring AI 1.0.0 non completamente verificata al momento dello sviluppo |
| Java 11     | Mancanza di records, sealed classes e altre funzionalita' moderne essenziali per il design del dominio |

---

## 3. Spring Boot 3.4.2

| Proprieta'   | Valore                                        |
|-------------|------------------------------------------------|
| **Nome**    | Spring Boot                                    |
| **Versione**| 3.4.2                                          |
| **Scopo**   | Framework applicativo con auto-configurazione  |

### Motivazione della scelta

Spring Boot 3.4.2 e' il parent POM del progetto (`spring-boot-starter-parent`). Le ragioni principali della scelta sono:

- **Auto-configurazione**: configurazione automatica di datasource, JPA, sicurezza, batch e client AI tramite starter.
- **Ecosistema maturo**: integrazione nativa con Spring Data, Spring Security, Spring Batch, Spring AI.
- **Production-ready**: Actuator per metriche e health check, gestione profili (dev/prod), supporto nativo per container.
- **Convention over configuration**: riduzione drastica del boilerplate di configurazione.
- **Jakarta EE 10**: basato su Jakarta EE, con namespace `jakarta.*` per servlet, persistence e validation.

### Alternative considerate

| Alternativa  | Motivo del rifiuto                                                |
|--------------|------------------------------------------------------------------|
| Quarkus      | Ottimo per microservizi cloud-native e GraalVM, ma ecosistema AI meno maturo; l'integrazione con Spring AI non e' disponibile |
| Micronaut    | Performance comparabili, ma community e documentazione meno estese; assenza di integrazione con Spring AI |

---

## 4. Spring AI 1.0.0

| Proprieta'   | Valore                                              |
|-------------|------------------------------------------------------|
| **Nome**    | Spring AI                                            |
| **Versione**| 1.0.0 (gestita tramite BOM `spring-ai-bom`)         |
| **Scopo**   | API unificata per provider LLM e vector store        |

### Motivazione della scelta

Spring AI fornisce un'astrazione unificata per l'interazione con diversi provider LLM e vector store, con auto-configurazione nativa per Spring Boot. Gli starter utilizzati nel progetto sono:

- `spring-ai-starter-model-ollama` - Client per Ollama (esecuzione locale di modelli LLM)
- `spring-ai-starter-model-openai` - Client per OpenAI (GPT-4o)
- `spring-ai-starter-model-anthropic` - Client per Anthropic (Claude)
- `spring-ai-starter-vector-store-qdrant` - Client per Qdrant (vector database)

### Vantaggi architetturali

- **API uniforme `ChatClient`**: ogni provider espone la stessa interfaccia, rendendo il sistema agnostico rispetto al provider LLM.
- **Auto-configurazione per provider**: ogni starter configura automaticamente il client in base alle proprieta' in `application.yml`.
- **Supporto embedding nativo**: generazione di embedding vettoriali tramite la stessa API.
- **Integrazione con vector store**: operazioni CRUD sui documenti vettorializzati tramite `VectorStore` interface.

### Alternative considerate

| Alternativa   | Motivo del rifiuto                                                |
|---------------|------------------------------------------------------------------|
| LangChain4j   | Libreria valida e matura, ma priva dell'integrazione nativa con lo stack Spring (auto-configurazione, starter, profili); richiede piu' codice di configurazione manuale |

---

## 5. Spring Data JPA / Hibernate

| Proprieta'   | Valore                                        |
|-------------|------------------------------------------------|
| **Nome**    | Spring Data JPA con Hibernate ORM              |
| **Versione**| Gestita da Spring Boot 3.4.2 (Hibernate 6.x)  |
| **Scopo**   | Object-Relational Mapping e accesso dati       |

### Motivazione della scelta

Spring Data JPA e' lo standard de facto per l'accesso dati in applicazioni Spring. Nel progetto e' utilizzato nel modulo `localmind-infrastructure` tramite lo starter `spring-boot-starter-data-jpa`.

Caratteristiche utilizzate:
- **Repository interface**: `JpaRepository<Entity, UUID>` per operazioni CRUD automatiche.
- **Hibernate 6.x**: ORM con supporto nativo per Jakarta EE 10 e mapping JSONB PostgreSQL.
- **`@JdbcTypeCode(SqlTypes.JSON)`**: per mapping nativo di campi JSONB PostgreSQL a `Map<String, Object>`.
- **Lifecycle callbacks**: `@PrePersist`, `@PreUpdate` per gestione automatica dei timestamp.
- **DDL validation**: `ddl-auto: validate` in combinazione con Flyway per garantire coerenza schema-entita'.

### Alternative considerate

| Alternativa | Motivo del rifiuto                                                |
|-------------|------------------------------------------------------------------|
| jOOQ        | Eccellente per query SQL complesse e type-safe; non necessario per il livello di complessita' delle query di LocalMind v0.1.0 |
| MyBatis     | Buon controllo SQL, ma richiede piu' codice boilerplate rispetto a Spring Data JPA per operazioni CRUD standard |

---

## 6. Spring Security

| Proprieta'   | Valore                                        |
|-------------|------------------------------------------------|
| **Nome**    | Spring Security                                |
| **Versione**| Gestita da Spring Boot 3.4.2                   |
| **Scopo**   | Autenticazione, autorizzazione, CORS           |

### Utilizzo nel progetto

Nella versione 0.1.0, Spring Security e' configurato in modalita' permissiva per lo sviluppo locale:

- **CORS**: abilitato per `http://localhost:4200` (frontend Angular).
- **CSRF**: disabilitato per API REST stateless.
- **Autenticazione**: nessuna autenticazione richiesta (tutte le rotte sono `permitAll`).
- **Session management**: stateless (`SessionCreationPolicy.STATELESS`).

> **Nota**: l'autenticazione completa (JWT, OAuth2) e' pianificata per versioni successive.

---

## 7. Spring Batch

| Proprieta'   | Valore                                        |
|-------------|------------------------------------------------|
| **Nome**    | Spring Batch                                   |
| **Versione**| Gestita da Spring Boot 3.4.2                   |
| **Scopo**   | Pipeline di elaborazione documenti e scheduling|

### Utilizzo nel progetto

Il modulo `localmind-batch` utilizza Spring Batch per la pipeline di elaborazione documenti:

- **Document Processing Pipeline**: lettura documenti -> estrazione testo (Tika) -> chunking -> generazione embedding -> salvataggio in Qdrant.
- **Scheduling**: scansione periodica delle cartelle configurate tramite cron expression (`0 */15 * * * *`).
- **Configurazione**: `spring.batch.job.enabled=false` di default; i job sono attivati programmaticamente o da trigger.

### Alternative considerate

| Alternativa       | Motivo del rifiuto                                                |
|-------------------|------------------------------------------------------------------|
| Custom Scheduler  | Meno robusto; Spring Batch offre gestione transazioni, retry, skip, restart e monitoring integrati |

---

## 8. Spring Retry

| Proprieta'   | Valore                                        |
|-------------|------------------------------------------------|
| **Nome**    | Spring Retry                                   |
| **Versione**| Gestita da Spring Boot 3.4.2                   |
| **Scopo**   | Logica di retry per chiamate LLM              |

### Utilizzo nel progetto

Spring Retry e' utilizzato nel modulo `localmind-infrastructure` per gestire errori transitori nelle chiamate ai provider LLM:

- **Max attempts**: 3 tentativi (configurabile via `localmind.llm.retry.max-attempts`).
- **Backoff**: 1000ms tra i tentativi (configurabile via `localmind.llm.retry.backoff-ms`).
- **Fallback**: supporto per fallback automatico tra provider (`OLLAMA -> OPENAI -> ANTHROPIC -> GOOGLE`).

---

## 9. PostgreSQL 16

| Proprieta'   | Valore                                        |
|-------------|------------------------------------------------|
| **Nome**    | PostgreSQL                                     |
| **Versione**| 16 (Alpine)                                   |
| **Scopo**   | Database relazionale principale               |

### Motivazione della scelta

PostgreSQL 16 e' il database relazionale primario del sistema. E' distribuito tramite Docker (`postgres:16-alpine`).

Caratteristiche utilizzate:
- **JSONB**: per il campo `metadata` nella tabella `documents`, consentendo storage flessibile di metadati eterogenei.
- **`gen_random_uuid()`**: generazione UUID lato database per chiavi primarie.
- **Indici B-tree**: su colonne ad alta cardinalita' per query performanti.
- **TIMESTAMP**: gestione temporale precisa per audit trail.

### Configurazione

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/localmind
    username: localmind
    password: localmind
    driver-class-name: org.postgresql.Driver
```

### Alternative considerate

| Alternativa | Motivo del rifiuto                                                |
|-------------|------------------------------------------------------------------|
| MySQL       | Supporto JSONB meno maturo; funzionalita' avanzate (gen_random_uuid, JSONB indexing) non native |
| H2          | Adatto solo per test; non adeguato per produzione con JSONB e UUID |

---

## 10. Qdrant

| Proprieta'   | Valore                                        |
|-------------|------------------------------------------------|
| **Nome**    | Qdrant                                         |
| **Versione**| latest                                         |
| **Scopo**   | Vector database per ricerca semantica          |

### Motivazione della scelta

Qdrant e' il vector database scelto per lo storage e la ricerca di embedding vettoriali generati dai documenti.

Caratteristiche determinanti:
- **Supporto gRPC**: comunicazione ad alte prestazioni tramite porta 6334.
- **Spring AI starter dedicato**: `spring-ai-starter-vector-store-qdrant` con auto-configurazione.
- **REST API**: interfaccia REST su porta 6333 per operazioni di gestione e debug.
- **Collection management**: gestione automatica delle collection tramite Spring AI.
- **Performance**: ottimizzato per ricerca nearest-neighbor su grandi volumi di vettori.

### Configurazione

```yaml
spring:
  ai:
    vectorstore:
      qdrant:
        host: localhost
        port: 6334
        collection-name: localmind-documents
```

### Alternative considerate

| Alternativa | Motivo del rifiuto                                                |
|-------------|------------------------------------------------------------------|
| Chroma      | Community piu' piccola; assenza di starter Spring AI dedicato al momento dello sviluppo |
| Milvus      | Piu' complesso da configurare e operare; overhead per un progetto single-node |
| Weaviate    | Funzionalita' avanzate non necessarie; starter Spring AI non disponibile |
| Pinecone    | Servizio cloud-only; incompatibile con l'approccio local-first di LocalMind |

---

## 11. Apache Tika 2.9.2

| Proprieta'   | Valore                                        |
|-------------|------------------------------------------------|
| **Nome**    | Apache Tika                                    |
| **Versione**| 2.9.2                                          |
| **Scopo**   | Estrazione testo multi-formato                 |

### Motivazione della scelta

Apache Tika e' la libreria di riferimento per l'estrazione di testo da formati eterogenei. Nel progetto sono utilizzati due artefatti:

- `tika-core` (2.9.2): API base per detection MIME type e parsing.
- `tika-parsers-standard-package` (2.9.2): parser per PDF, DOCX, TXT, EML e altri formati.

### Formati supportati

| Formato | MIME Type                                                      |
|---------|----------------------------------------------------------------|
| PDF     | `application/pdf`                                              |
| DOCX    | `application/vnd.openxmlformats-officedocument.wordprocessingml.document` |
| TXT     | `text/plain`                                                   |
| EML     | `message/rfc822`                                               |

### Alternative considerate

| Alternativa | Motivo del rifiuto                                                |
|-------------|------------------------------------------------------------------|
| PDFBox      | Supporta solo PDF; Tika offre un'API unificata per formati multipli |
| iText       | Licenza commerciale (AGPL); orientato alla generazione PDF piu' che all'estrazione testo |

---

## 12. Flyway

| Proprieta'   | Valore                                        |
|-------------|------------------------------------------------|
| **Nome**    | Flyway                                         |
| **Versione**| Gestita da Spring Boot 3.4.2                   |
| **Scopo**   | Migrazione schema database versionata          |

### Utilizzo nel progetto

Flyway gestisce le migrazioni del database PostgreSQL in modo versionato e ripetibile:

- **Artefatti**: `flyway-core`, `flyway-database-postgresql`.
- **Directory migrazioni**: `localmind-app/src/main/resources/db/migration/`.
- **Convenzione naming**: `V{numero}__{descrizione}.sql`.
- **Integrazione**: attivato automaticamente all'avvio dell'applicazione Spring Boot.

### Configurazione

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
  jpa:
    hibernate:
      ddl-auto: validate
```

> **Nota**: `ddl-auto: validate` garantisce che Hibernate verifichi la coerenza tra entita' JPA e schema database senza mai modificare lo schema autonomamente.

### Alternative considerate

| Alternativa | Motivo del rifiuto                                                |
|-------------|------------------------------------------------------------------|
| Liquibase   | Piu' flessibile (formato XML/YAML/JSON), ma maggiore complessita'; Flyway e' sufficiente per migrazioni SQL pure |

---

## 13. MapStruct 1.6.3

| Proprieta'   | Valore                                        |
|-------------|------------------------------------------------|
| **Nome**    | MapStruct                                      |
| **Versione**| 1.6.3                                          |
| **Scopo**   | Mapping DTO compile-time                       |

### Motivazione della scelta

MapStruct genera codice di mapping tra DTO e modelli di dominio a tempo di compilazione, garantendo:

- **Type safety**: errori di mapping rilevati in fase di compilazione.
- **Performance**: nessun overhead di reflection a runtime.
- **Integrazione Lombok**: configurato come annotation processor insieme a Lombok nel `maven-compiler-plugin`.

### Configurazione Maven

```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${mapstruct.version}</version>
    </path>
</annotationProcessorPaths>
```

### Alternative considerate

| Alternativa   | Motivo del rifiuto                                                |
|---------------|------------------------------------------------------------------|
| ModelMapper   | Mapping basato su reflection a runtime; meno performante e type-safe rispetto a MapStruct |

---

## 14. Lombok 1.18.36

| Proprieta'   | Valore                                        |
|-------------|------------------------------------------------|
| **Nome**    | Project Lombok                                 |
| **Versione**| 1.18.36                                        |
| **Scopo**   | Riduzione boilerplate Java                     |

### Utilizzo nel progetto

Lombok e' una dipendenza condivisa (`<optional>true</optional>`) tra tutti i moduli del progetto. Le annotazioni utilizzate includono:

- `@Data`: genera getter, setter, `equals()`, `hashCode()`, `toString()`.
- `@Builder`: pattern builder per costruzione fluente di oggetti.
- `@NoArgsConstructor`, `@AllArgsConstructor`: costruttori richiesti da JPA e Jackson.
- `@Builder.Default`: valori di default nei builder (es. `recursive = true`).
- `@Slf4j`: logger SLF4J iniettato automaticamente.

---

## 15. Maven

| Proprieta'   | Valore                                        |
|-------------|------------------------------------------------|
| **Nome**    | Apache Maven                                   |
| **Versione**| 3.x (wrapper non incluso)                     |
| **Scopo**   | Build tool e gestione dipendenze               |

### Motivazione della scelta

Maven e' il build tool del progetto, scelto per:

- **Multi-module support**: struttura nativa per progetti multi-modulo con POM parent.
- **Dependency management centralizzato**: versioni gestite nel POM parent tramite `<dependencyManagement>` e BOM.
- **Plugin ecosystem maturo**: `maven-compiler-plugin` con annotation processor per Lombok e MapStruct.
- **Convenzioni standard**: struttura directory standard (`src/main/java`, `src/main/resources`).
- **Spring Boot plugin**: `spring-boot-maven-plugin` per packaging del fat JAR eseguibile.

### Alternative considerate

| Alternativa | Motivo del rifiuto                                                |
|-------------|------------------------------------------------------------------|
| Gradle      | Build incrementali piu' veloci, ma DSL Groovy/Kotlin aggiunge complessita'; Maven e' piu' prevedibile per team eterogenei |

---

## 16. Tabella Riepilogativa Dipendenze

La tabella seguente elenca tutte le dipendenze con le rispettive versioni, come definite nei file POM del progetto.

### Dipendenze gestite dal POM Parent

| Dipendenza                              | Versione     | Modulo                    | Scope     |
|-----------------------------------------|-------------|---------------------------|-----------|
| `spring-boot-starter-parent`            | 3.4.2       | Parent POM                | -         |
| `spring-ai-bom`                         | 1.0.0       | Parent POM (BOM)          | import    |
| `lombok`                                | 1.18.36     | Tutti i moduli            | optional  |
| `spring-boot-starter-test`              | 3.4.2*      | Tutti i moduli            | test      |
| `mapstruct` / `mapstruct-processor`     | 1.6.3       | infrastructure, api       | compile   |

### Dipendenze per Modulo

#### localmind-domain
| Dipendenza     | Versione | Note                    |
|---------------|----------|--------------------------|
| (nessuna)     | -        | Modulo Java puro, zero dipendenze framework |

#### localmind-infrastructure
| Dipendenza                                  | Versione   | Note                      |
|---------------------------------------------|-----------|----------------------------|
| `spring-boot-starter-data-jpa`              | 3.4.2*    | ORM e accesso dati         |
| `spring-boot-starter-security`              | 3.4.2*    | Autenticazione e CORS      |
| `spring-boot-starter-webflux`               | 3.4.2*    | WebClient per chiamate HTTP|
| `spring-ai-starter-model-ollama`            | 1.0.0*    | Client Ollama              |
| `spring-ai-starter-model-openai`            | 1.0.0*    | Client OpenAI              |
| `spring-ai-starter-model-anthropic`         | 1.0.0*    | Client Anthropic           |
| `spring-ai-starter-vector-store-qdrant`     | 1.0.0*    | Client Qdrant              |
| `postgresql`                                | runtime*  | Driver JDBC PostgreSQL     |
| `tika-core`                                 | 2.9.2     | Estrazione testo (core)    |
| `tika-parsers-standard-package`             | 2.9.2     | Parser multi-formato       |
| `spring-retry`                              | 3.4.2*    | Retry per chiamate LLM     |

#### localmind-api
| Dipendenza                          | Versione | Note                    |
|-------------------------------------|---------|--------------------------|
| `spring-boot-starter-web`           | 3.4.2*  | Web MVC e REST            |
| `spring-boot-starter-validation`    | 3.4.2*  | Bean Validation (Jakarta) |

#### localmind-batch
| Dipendenza                          | Versione | Note                    |
|-------------------------------------|---------|--------------------------|
| `spring-boot-starter-batch`         | 3.4.2*  | Spring Batch framework    |

#### localmind-app
| Dipendenza                          | Versione | Note                    |
|-------------------------------------|---------|--------------------------|
| `spring-boot-starter-actuator`      | 3.4.2*  | Health check e metriche   |
| `flyway-core`                       | 3.4.2*  | Migrazioni database       |
| `flyway-database-postgresql`        | 3.4.2*  | Supporto PostgreSQL       |

> **Nota**: le versioni contrassegnate con `*` sono gestite automaticamente dal parent POM (`spring-boot-starter-parent` 3.4.2) o dal BOM Spring AI 1.0.0 e non sono specificate esplicitamente nei POM dei moduli.
