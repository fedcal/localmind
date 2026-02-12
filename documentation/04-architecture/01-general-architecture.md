# General Architecture

| Field        | Value                           |
|--------------|---------------------------------|
| **Document** | General Architecture            |
| **Version**  | 0.1.0                           |
| **Date**     | 2026-02-09                      |
| **Project**  | LocalMind                       |

---

## Table of Contents

- [General Architecture](#general-architecture)
  - [Table of Contents](#table-of-contents)
  - [1. Architectural Overview](#1-architectural-overview)
  - [2. 3-Tier Architecture](#2-3-tier-architecture)
  - [3. C4 Context Diagram](#3-c4-context-diagram)
    - [3.1 Interaction Description](#31-interaction-description)
  - [4. C4 Container Diagram](#4-c4-container-diagram)
    - [4.1 Service List](#41-service-list)
  - [5. Communication Protocols](#5-communication-protocols)
    - [5.1 REST API (JSON)](#51-rest-api-json)
    - [5.2 gRPC (Qdrant)](#52-grpc-qdrant)
    - [5.3 Future Communication](#53-future-communication)
  - [6. Service Execution](#6-service-execution)
    - [6.1 Execution Modes](#61-execution-modes)
    - [6.2 Database Configuration](#62-database-configuration)

---

## 1. Architectural Overview

LocalMind adopts a three-tier architecture (3-tier) with a clear separation between presentation, application logic, and data persistence. Backend and frontend are executed natively via dedicated scripts in the `scripts/` folder, while optional infrastructure services (Qdrant, Ollama, n8n) can be started natively or via Docker.

The architecture is designed for:

- **Modularity**: each component is independent and replaceable
- **Scalability**: services can be scaled independently
- **Testability**: layer separation enables unit, integration, and end-to-end testing
- **Simplified deployment**: setup and startup scripts automate environment configuration

---

## 2. 3-Tier Architecture

```
+-----------------------------------------------------------------------+
|                        PRESENTATION TIER                              |
|                                                                       |
|  +----------------------------------------------------------------+   |
|  |                    LocalMind UI (Angular 21)                   |   |
|  |                                                                |   |
|  |  - Standalone components                                       |   |
|  |  - Signal-based state management                               |   |
|  |  - REST client (HttpClient)                                    |   |
|  |  - Port: 4200 (dev) / 80 (prod via nginx)                      |   |
|  +----------------------------------------------------------------+   |
|                              |                                        |
|                         HTTP REST (JSON)                              |
|                              |                                        |
+-----------------------------------------------------------------------+
|                        APPLICATION TIER                               |
|                                                                       |
|  +-----------------------------------------------------------------+  |
|  |                 LocalMind API (Spring Boot 3.4)                 |  |
|  |                                                                 |  |
|  |  +------------------+  +------------------+  +---------------+  |  |
|  |  | localmind-api    |  | localmind-domain |  | localmind-    |  |  |
|  |  | (controllers,    |  | (entities,       |  | infrastructure|  |  |
|  |  |  DTOs, mapping)  |  |  services, ports)|  | (adapters,    |  |  |
|  |  +------------------+  +------------------+  |  JPA, AI)     |  |  |
|  |                                              +---------------+  |  |
|  |  +------------------+  +------------------+                     |  |
|  |  | localmind-batch  |  | localmind-app    |                     |  |
|  |  | (Spring Batch    |  | (bootstrap,      |                     |  |
|  |  |  jobs, config)   |  |  configuration)  |                     |  |
|  |  +------------------+  +------------------+                     |  |
|  |                                                                 |  |
|  |  Port: 8080                                                     |  |
|  +-----------------------------------------------------------------+  |
|                              |                                        |
+-----------------------------------------------------------------------+
|                          DATA TIER                                    |
|                                                                       |
|  +-----------------------+  +------------------------------------+    |
|  |    MySQL 8.0          |  |           Qdrant                   |    |
|  |                       |  |                                    |    |
|  |  - Relational data    |  |  - Vector store                    |    |
|  |  - Document metadata  |  |  - Document embeddings             |    |
|  |  - User config        |  |  - Semantic search                 |    |
|  |  - Usage/cost data    |  |                                    |    |
|  |  - Batch job metadata |  |  Ports: 6333 (HTTP), 6334 (gRPC)   |    |
|  |                       |  |                                    |    |
|  |  Port: 3306           |  |                                    |    |
|  +-----------------------+  +------------------------------------+    |
|                                                                       |
+-----------------------------------------------------------------------+
```

---

## 3. C4 Context Diagram

The C4 Context diagram shows LocalMind in the context of its external environment, highlighting interactions with users and external systems.

```
                                +-------------------+
                                |                   |
                                |       User        |
                                |                   |
                                +--------+----------+
                                         |
                                    Browser
                                         |
                                         v
                        +----------------+-------------------+
                        |                                    |
                        |       LocalMind UI (Angular)       |
                        |                                    |
                        +----------------+-------------------+
                                         |
                                    REST API
                                         |
                                         v
                        +----------------+-------------------+
                        |                                    |
                        |     LocalMind API (Spring Boot)    |
                        |                                    |
                        +--+------+------+------+------+-----+
                           |      |      |      |      |
              +------------+  +---+--+ +-+----+ | +----+--------+
              |               |      | |      | | |             |
              v               v      v v      v v v             v
    +---------+---+  +--------+-+ +--+----+ +-+-+-----+ +------+------+
    |             |  |          | |       | |         | |             |
    |   Ollama    |  |  OpenAI  | |Anthro.| | Google  | | File System |
    |   (local)   |  |  (cloud) | |(cloud)| | (cloud) | |  (local)    |
    |             |  |          | |       | |         | |             |
    +-------------+  +----------+ +-------+ +---------+ +-------------+

              +---------------+  +-----------------+
              |               |  |                 |
              |    MySQL      |  |     Qdrant      |
              |   (local)     |  |    (local)      |
              |               |  |                 |
              +---------------+  +-----------------+

                        +-------------------+
                        |                   |
                        |       n8n         |
                        |   (self-hosted)   |
                        |                   |
                        +-------------------+
```

### 3.1 Interaction Description

| Source           | Destination        | Protocol      | Description                           |
|------------------|--------------------|---------------|---------------------------------------|
| User             | LocalMind UI       | HTTPS         | Web interface via browser             |
| LocalMind UI     | LocalMind API      | HTTP REST     | JSON API calls                        |
| LocalMind API    | Ollama             | HTTP REST     | Local LLM requests (port 11434)       |
| LocalMind API    | OpenAI             | HTTPS REST    | Cloud LLM requests                    |
| LocalMind API    | Anthropic          | HTTPS REST    | Cloud LLM requests                    |
| LocalMind API    | Google             | HTTPS REST    | Cloud LLM requests                    |
| LocalMind API    | MySQL              | JDBC          | Relational data persistence (3306)    |
| LocalMind API    | Qdrant             | gRPC/HTTP     | Vector store (6334/6333)              |
| LocalMind API    | n8n                | HTTP POST     | Webhook for automations (5678)        |
| LocalMind API    | File System        | Java NIO      | Local document reading                |
| n8n              | LocalMind API      | HTTP REST     | Callback and API invocation           |
| n8n              | External Services  | HTTPS         | Email, Slack, Drive, etc.             |

---

## 4. C4 Container Diagram

The C4 Container diagram shows the containers (processes) that make up LocalMind and their interactions.

```
+-------------------------------------------------------------------+
|                     Native Execution (host)                       |
|                                                                   |
|  +-----------------------------+  +----------------------------+  |
|  |   localmind-frontend        |  |   localmind-backend        |  |
|  |   (Angular CLI / ng serve)  |  |   (Spring Boot fat JAR)    |  |
|  |                             |  |                            |  |
|  |   Port: 4200 (dev)          |  |   Port: 8080               |  |
|  +-----------------------------+  +---+------+------+------+---+  |
|                                       |      |      |      |      |
|                          +------------+  +---+--+ +-+----+ |      |
|                          |               |      | |      | |      |
|                          v               v      v v      v v      |
|  +-----------------------------+  +----------------------------+  |
|  |   localmind-db              |  |   localmind-qdrant         |  |
|  |   (MySQL 8.0)               |  |   (Qdrant) [optional]      |  |
|  |                             |  |                            |  |
|  |   Port: 3306                |  |   Port: 6333 (HTTP)        |  |
|  |   native or Docker          |  |         6334 (gRPC)        |  |
|  +-----------------------------+  +----------------------------+  |
|                                                                   |
|  +-----------------------------+  +----------------------------+  |
|  |   localmind-ollama          |  |   localmind-n8n            |  |
|  |   (Ollama) [optional]       |  |   (n8n) [optional]         |  |
|  |                             |  |                            |  |
|  |   Port: 11434               |  |   Port: 5678               |  |
|  |   native or Docker          |  |   native or Docker         |  |
|  |   GPU: nvidia (optional)    |  |   Auth: Basic              |  |
|  +-----------------------------+  +----------------------------+  |
|                                                                   |
+-------------------------------------------------------------------+
```

### 4.1 Service List

| Service             | Execution                      | Port      | Function                 |
|---------------------|--------------------------------|-----------|--------------------------|
| localmind-frontend  | Native (Angular CLI)           | 4200      | Angular UI               |
| localmind-backend   | Native (Java JAR)              | 8080      | Spring Boot API          |
| localmind-db        | Native or Docker (`mysql:8.0`) | 3306      | Relational database      |
| localmind-qdrant    | Native or Docker (optional)    | 6333/6334 | Vector store             |
| localmind-ollama    | Native or Docker (optional)    | 11434     | Local LLM                |
| localmind-n8n       | Native or Docker (optional)    | 5678      | Automations              |

---

## 5. Communication Protocols

### 5.1 REST API (JSON)

Communication between frontend and backend occurs via REST APIs with JSON payloads:

- **Content-Type**: `application/json`
- **Encoding**: UTF-8
- **Authentication**: JWT (future) / Basic Auth (initial phase)
- **Versioning**: URL path (`/api/v1/...`)
- **Error handling**: RFC 7807 Problem Details

### 5.2 gRPC (Qdrant)

Communication between backend and Qdrant preferably uses the gRPC protocol for maximum performance:

- **Port**: 6334
- **Protocol**: HTTP/2 + Protocol Buffers
- **Fallback**: HTTP REST (port 6333) in case of gRPC issues

### 5.3 Future Communication

| Protocol   | Use Case                        | Status    |
|------------|---------------------------------|-----------|
| SSE        | LLM response streaming          | Planned   |
| WebSocket  | Bidirectional real-time chat    | Planned   |
| gRPC       | Inter-service communication     | Evaluation|

---

## 6. Service Execution

### 6.1 Execution Modes

The project no longer uses a `docker-compose.yml` file for the full stack. Backend and frontend are executed natively via scripts in the `scripts/` folder.

| Service        | Mode                                           | Startup                        |
|----------------|------------------------------------------------|--------------------------------|
| Backend        | Native (Java JAR)                              | `./scripts/start-backend.sh`   |
| Frontend       | Native (Angular CLI)                           | `./scripts/start-frontend.sh`  |
| MySQL          | Native or Docker (auto-detected by setup.sh)   | `./scripts/setup.sh`           |
| Qdrant         | Native or Docker (optional)                    | Manual startup                 |
| Ollama         | Native or Docker (optional)                    | Manual startup                 |
| n8n            | Native or Docker (optional)                    | Manual startup                 |

### 6.2 Database Configuration

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/localmind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: localmind
    password: localmind
    driver-class-name: com.mysql.cj.jdbc.Driver
```

> **Note**: for details on starting optional infrastructure services via Docker, see the [Infrastructure and Services](../05-technology-stack/03-docker-infrastructure.md) section.
