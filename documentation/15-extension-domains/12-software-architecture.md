# Software architecture

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This scope belongs to the **enterprise group** of LocalMind's universal knowledge graph engine. While the consumer verticals answer the question *"what can I do in this territory?"*, the **Software architecture** scope answers the structural question that every technology organization asks itself every day: *"what does our system really look like, how are the pieces connected, and what breaks if I change this?"*. It is the representation, as a weighted graph navigable by the AI, of the entire technical estate: Git repositories, services and microservices, APIs, databases, queues and message buses, infrastructure, CI/CD pipelines, libraries and dependencies, owning teams and architectural decisions. The graph connects these elements to one another and to the rest of the enterprise knowledge (documentation, processes, tickets, people) already modeled by LocalMind.

The scope is deliberately built around **consolidated industry standards** — the **C4 model** for architectural description, the **Backstage entity model** (Component, System, API, Resource, Domain) for the software catalog, **CycloneDX/SBOM** for the bill of components and vulnerabilities, **OpenAPI/AsyncAPI** for interface contracts, **OpenTelemetry** for the runtime topology, and **AST-derived graphs** for code structure. Everything remains, however, **local-first**: source code, secrets, and internal topology never leave the self-hosted instance without explicit consent, and the default AI is Ollama running locally — a non-negotiable requirement for anyone who does not want to route their intellectual property through third-party cloud services.

---

## 1. What we solve (problem & value)

### 1.1 The concrete problem

In every organization that produces software beyond a certain complexity threshold, **knowledge of the architecture lives in people's heads and in disconnected artifacts**, never in a single queryable and always up-to-date representation. This gives rise to recurring and costly problems:

- **Erosion of the mental model.** No one knows the entire system anymore. The diagram on Confluence is two years out of date, each repo's README tells a partial truth, and the "real" architecture only emerges by reading the code, the Terraform files, and the Kubernetes manifests. Knowledge is fragmented across dozens of repositories, wikis, observability dashboards, and chats.
- **Invisible dependencies and hidden coupling.** The most dangerous dependencies are the undocumented ones: service A calls B's API, which in turn reads a table shared with C; an internal library is used by forty services but no one knows which; a nightly job writes to a queue consumed by a system that was thought to be decommissioned. These relationships exist in the real system but on no map.
- **Impossible or costly impact analysis ("blast radius").** Before modifying an endpoint, deprecating a field of an event, updating a library with a CVE, or migrating a database, the question is always the same: *"who is impacted?"*. Today the answer requires hours of code archaeology, meetings, and tribal knowledge, and it is often wrong — with production incidents as the consequence.
- **Extremely slow onboarding.** A new developer takes weeks or months to build the mental model of the system. There is no entry point that explains "this domain is made up of these services, which speak these APIs, own this data, belong to this team".
- **Drift between intended architecture and actual architecture.** Architectural decisions (ADRs) describe the intent; the reality of the code silently diverges. Without a continuous comparison between the "as-designed" model and the "as-built" one (derived from code, contracts, and runtime traces), architectural violations accumulate without anyone noticing.
- **Reactive security and compliance.** When a new CVE comes out, figuring out which services include the vulnerable library, how exposed they are, and what the propagation chain is, is a manual exercise. The bill of components (SBOM) exists in silos, not connected to the graph of who uses what.
- **Unmeasurable technical debt.** Excessive coupling, dependency cycles, "god services" with an abnormal node degree, APIs without consumers (dead code) or with too many consumers (single point of failure) are all patterns visible in a graph, invisible otherwise.

All these problems share a common root: **architecture is intrinsically a graph** (components as nodes, dependencies as weighted edges), but it is managed with tools that are not graphs — documents, spreadsheets, static diagrams, oral knowledge.

### 1.2 The LocalMind solution

LocalMind models the entire software system as a **weighted graph of technical knowledge**: the nodes represent repositories, services, APIs, databases, queues, libraries, infrastructure resources, pipelines, teams, ADRs; the edges represent weighted relationships (a service *calls* an API with a certain frequency, *depends on* a library at a certain version, *reads/writes* a table, *is owned by* a team, *is deployed on* a cluster). On this graph the AI operates in **GraphRAG** mode: it combines semantic search (Qdrant, over documentation and comments) with deterministic navigation of the relationships (MySQL) to answer complex questions, compute the impact of a change, and surface non-obvious connections.

The key architectural point, supported by the most recent research (cf. the comparison between AST-derived graphs and LLM-extracted graphs, §6), is that **the technical structure must be extracted deterministically** from the sources of truth (code via AST, OpenAPI contracts, IaC files, SBOM, OTel traces), not "guessed" by the LLM. The LLM reads and reasons *over* the graph already built with precision; it is not the one that builds it. This makes impact analysis answers reliable and reproducible, not stochastic.

The differentiating value compared to existing tools:

| Capability | Wiki / static diagrams | Service catalog (e.g. IDP portals) | LocalMind (graph + GraphRAG) |
|----------|--------------------------|------------------------------------|------------------------------|
| Update | Manual, always outdated | Semi-automatic (catalog-info) | Continuous ingestion from code, contracts, traces |
| Multi-hop impact analysis | Absent | Limited (1 hop) | Weighted N-hop paths over the graph |
| Natural-language reasoning | Absent | Text search | GraphRAG: question → navigation → cited answer |
| As-designed vs as-built | Only as-designed | Predominantly as-designed | Explicit design/real comparison (drift) |
| Security (SBOM/CVE) | Disconnected | Separate plugin | CVE → components → services → blast radius in the graph |
| Code privacy | Variable | Often cloud | Local-first, Ollama AI running locally |
| Extensibility to new types | None | Fixed schema | Modular node/edge types via PF4J plugins |

### 1.3 The value for stakeholders

- **For the developer:** they can ask in plain words *"who consumes this endpoint?"*, *"if I change this column what breaks?"*, *"what is the path from this service to the orders database?"* and get answers grounded in the graph, with citation of the nodes and the paths.
- **For the architect / tech lead:** they have a living view of the system, can identify excessive coupling, dependency cycles, hotspots, and divergences from the ADRs, and simulate the impact of refactorings and decompositions.
- **For the platform / SRE team:** they obtain the blast radius of an incident or a deploy, the runtime map of dependencies (from OTel), and the correlation between topology and reliability.
- **For security (AppSec):** from the appearance of a CVE they trace, in a few seconds, all the impacted services through the SBOM graph, prioritizing remediation by real exposure.
- **For technical management:** they measure architectural debt with graph metrics, monitor ownership, and identify organizational and technical single points of failure.
- **For those onboarding:** they start from the graph of their domain and explore it progressively, with the AI as a narrative guide grounded in the reality of the code.

### 1.4 Boundaries of responsibility (what it is NOT)

- **It is not an APM/real-time observability tool.** LocalMind ingests the topology derived from traces (who calls whom) to enrich the graph, but it does not replace Prometheus/Grafana/Jaeger for metrics, alerts, and live tracing.
- **It is not a CI/CD nor an orchestrator.** It reads pipeline manifests and IaC to build nodes and relationships, but it does not perform deploys nor manage clusters.
- **It is not a replacement for the compiler or the IDE.** AST analysis serves to extract structure and dependencies for the graph, not to compile or provide autocompletion.
- **It does not exfiltrate code.** The source stays local; embeddings and analysis happen on-premise. Sending to cloud providers is explicit and granular opt-in.

---

## 2. Personas & target users

| Persona | Description | Primary goal | Key need |
|---------|-------------|--------------------|-----------------|
| **Backend/frontend developer** | Works on one or more services | Understand dependencies and impact before modifying | Quick answers on consumers/producers, paths |
| **Architect / Tech lead** | Responsible for system design | Overall view, drift control, refactoring | As-designed/as-built comparison, coupling metrics |
| **Platform engineer** | Builds and maintains the internal IDP | Living, self-hosted service catalog | Connectors, extensible schema, ingestion automation |
| **SRE / On-call** | Ensures reliability | Blast radius of incidents and deploys | Runtime topology, critical paths, ownership |
| **Security engineer (AppSec)** | Manages vulnerabilities and compliance | From CVE to impacted services | SBOM graph, propagation, prioritization |
| **Engineering manager** | Manages teams and priorities | Measure debt and ownership | Graph metrics, single points of failure |
| **New hire** | Must build the mental model | Fast onboarding on the domain | Guided exploration, AI narration |
| **Data engineer** | Manages pipelines and databases | Data lineage and dependencies between datasets | Read/write relationships, lineage |
| **Instance administrator** | Technician who manages LocalMind | Self-hosting, connectors, privacy | Configuration, access control, ingestion scheduling |

Role distinction (`auth` domain): **readers** (the majority of developers) query the graph read-only through the assistant; **curators** (architects, platform) can correct/annotate nodes and relationships and define the ADRs; **administrators** manage connectors, scheduling, and privacy policies. The segmentation also drives visibility: certain subgraphs (e.g. security infrastructure) can be restricted.

---

## 3. Input requirements

This section defines in detail **what must enter the system** so that the architecture graph is accurate, always up-to-date, and reliable for impact analysis. A distinction is made between **domain** inputs (the technical artifacts from which the graph is derived), **user** inputs (the questions and annotations), and **configuration/governance** inputs.

### 3.1 Domain inputs: the technical sources of truth

The guiding principle is **derive, do not declare**: wherever possible the graph is built from already-existing and versioned artifacts, not from manual entry that would immediately become obsolete. The primary sources:

| Source | What it provides to the graph | Format / standard |
|-------|------------------------|--------------------|
| **Git repository** | Repo, modules, files, code structure, history, ownership (CODEOWNERS) | Git (local clone/fetch), CODEOWNERS |
| **Source code (AST)** | Classes, functions, cross-file calls, imports, inheritance, packages | AST parser per language (Java, TS, Python…) |
| **Build/dependency manifests** | Libraries and versions, direct/transitive dependencies | `pom.xml`, `package.json`/lockfile, `requirements.txt`, `go.mod` |
| **SBOM** | Complete bill of components + known vulnerabilities | CycloneDX, SPDX, VEX |
| **API contracts** | Endpoints, schema, operations, versions; events and topics | OpenAPI/Swagger, AsyncAPI, gRPC/protobuf |
| **Catalog descriptors** | Declared metadata: Component, System, API, Resource, Domain, owner | `catalog-info.yaml` (Backstage model) |
| **Infrastructure as Code** | Cloud resources, networks, clusters, queues, buckets, secrets (references) | Terraform/HCL, Helm, Kubernetes manifests |
| **CI/CD** | Pipelines, stages, artifacts, deploy environments | GitHub Actions, GitLab CI, Jenkinsfile |
| **Database schema** | Tables, columns, foreign keys, indexes | DDL, migrations (e.g. Flyway), JDBC introspection |
| **Runtime topology** | Who actually calls whom, frequency, latency | OpenTelemetry (Service Graph Connector, spanmetrics) |
| **Documentation & ADR** | Descriptions, rationale, architectural decisions | Markdown, ADR (MADR format), wiki |

For each ingested **artifact** the model requires a minimal set and an extended one:

| Category | Minimal fields (MVP) | Extended fields (evolution) |
|-----------|--------------------|---------------------------|
| Identity | node type, canonical name, namespace/domain | aliases, description, tags, business criticality |
| Ownership | repo/source path, owning team | organizational hierarchy, on-call, lifecycle (active/deprecated) |
| Version | current version, reference commit/SHA | version history, changelog, last update date |
| Relationships | connected nodes, relationship type | direction, cardinality, associated contract |
| Provenance | source (AST/contract/IaC/OTel/manual), ingestion date | confidence level, extraction method |

### 3.2 Quality constraints on inputs

Consistently with the project rules on input validation and immutability, the system must **validate at the boundary**:

- **Canonical identity and deduplication.** The same service referenced by Git, OpenAPI, OTel, and catalog-info must resolve to **a single node** via canonical keys (e.g. `domain.system.component`), with deterministic matching rules and traced disambiguation.
- **Determinism of structural extraction.** Structural relationships (calls, imports, FKs, dependencies) are extracted with deterministic parsers (AST, schema, contracts), not with the LLM, to guarantee reproducible and reliable results for impact analysis.
- **Version normalization.** Semantic versions normalized; transitive dependencies distinguished from direct ones.
- **Freshness and lifecycle.** Every node/edge has an observation date and a source; beyond configurable thresholds the information is marked "stale" and its weight decays (§5). A node no longer observed for N ingestions is a candidate for "deprecated/removed".
- **Provenance and confidence.** Every element tracks *where* it comes from and by which method; runtime (observed) relationships have a different confidence from declared ones (catalog-info) or inferred ones.
- **Sensitivity and secrets.** Never ingest secret/credential values: only the *references* are modeled (a secret X used by Y exists), never the content. Anti-secret filters are mandatory at ingestion.

### 3.3 User inputs

The user provides two types of input:

1. **Natural-language questions** (IT/EN), which the assistant translates into graph queries. Examples: *"Which services consume the Payments v2 API?"*, *"If I update the auth-core library to 3.0, what do I need to test?"*, *"Show me the dependency cycles in the orders domain"*, *"What is the blast radius of the `customers` table?"*.
2. **Annotations and corrections** (curators): adding non-derivable context (business criticality, planned deprecations, ADRs, correct ownership), confirming or refuting inferred relationships, linking a service to a process or a ticket. Every annotation is immutable and versioned, with author and date.

### 3.4 Configuration & governance inputs

- **Scope and connectors:** which repositories/organizations/registries/clusters to ingest, with which credentials (locally), with which scheduling.
- **Privacy policies:** which subgraphs may use cloud providers (default: none), redaction rules, allowlist/denylist of paths.
- **Domain schema:** which node/edge types to enable and with which weights (an organization can activate only the relevant subset).
- **Architectural rules (fitness functions):** constraints the graph must respect (e.g. "domain A must not depend on domain B", "no cycle between bounded contexts"), to detect violations.

---

## 4. Activity flow (step-by-step)

The flow is articulated into two macro-cycles: the **ingestion & graph construction cycle** (batch/scheduled, predominantly automatic) and the **query & reasoning cycle** (interactive, user-driven). Added to these is the **curation & governance cycle** (continuous).

### 4.1 Ingestion and graph construction cycle

1. **Source registration.** The administrator configures the connectors (Git repository, SBOM registry, OpenAPI endpoint, OTel exporter, IaC folders) with local credentials and scheduling. Reuse of the `automation` domain for scheduling and of the batch pattern (`localmind-batch`) for execution.
2. **Local acquisition.** The sources are cloned/downloaded locally (never sent elsewhere). For each source a delta is computed relative to the previous ingestion (commit SHA, file hashes) to process only what has changed (incremental ingestion).
3. **Deterministic extraction.** For each source a dedicated *extractor* produces candidate nodes and relationships:
   - AST parser for code structure and cross-file calls;
   - build manifest parser for dependencies and versions;
   - SBOM parser (CycloneDX) for components and CVEs;
   - OpenAPI/AsyncAPI parser for endpoints, schema, events;
   - IaC/Kubernetes parser for infrastructure resources;
   - introspection/DDL for database schema and foreign keys;
   - aggregation of OTel traces for the runtime "calls" relationships.
4. **Identity resolution (entity resolution).** The candidate nodes coming from different sources are merged onto the canonical node via keys and deterministic rules; conflicts are recorded and, if not automatically resolvable, placed in a review queue for the curators.
5. **Weight computation.** For each edge the weight is computed according to the configured criteria (§5): runtime frequency, number of static call sites, criticality, freshness, source confidence. The weight is a derived and recomputable value, never mutated in-place (immutability).
6. **Persistence.** The nodes and edges are written to MySQL (structure, attributes, weights) with Flyway migrations (one query per file). In parallel, the textual descriptions (documentation, significant comments, endpoint descriptions, ADRs) are "chunked", embedded (Ollama as the primary model), and indexed into Qdrant, with metadata pointing to the graph node. This is the structure↔semantics bridge for GraphRAG.
7. **Fitness function validation.** After the update, the configured architectural rules are evaluated on the graph; any violations (cycles, prohibited dependencies, APIs without owner) generate alerts.
8. **Drift and orphan detection.** *Declared* relationships (catalog-info, ADR) are compared with *observed* ones (AST, OTel); the divergences are flagged. Nodes no longer observed decay and are proposed for deprecation.
9. **Event publication.** Changes to the graph emit domain events (reuse of the `DomainEventPublisherPort`) that feed notifications, metric recomputation, and cache updates.

### 4.2 Query and reasoning cycle (GraphRAG)

1. **User question.** The developer asks a natural-language question (IT/EN) from the conversational assistant (reuse of the `llm`/chat domain).
2. **Intent understanding.** The AI classifies the question: point lookup, impact analysis, path search, pattern detection, architectural explanation.
3. **Hybrid retrieval.** The system combines: (a) **semantic search** on Qdrant to identify the relevant entry nodes (by description/documentation); (b) **deterministic navigation** of the graph on MySQL starting from those nodes — neighbors, weighted paths, subgraphs, bidirectional N-hop expansion.
4. **Context construction.** The relevant subgraph is selected (nodes + edges + attributes + linked documentary excerpts), ordered by weight and relevance, within the model's context budget.
5. **AI reasoning.** The LLM reasons *over the provided subgraph* to produce the answer — the impact analysis, the consumers, the path, the explanation. The structure is already deterministic: the LLM interprets and verbalizes it, it does not invent it.
6. **Answer with citations.** The answer explicitly cites the nodes and paths used (e.g. "Service A → Payments v2 API → orders DB"), allowing the user to open the nodes in the viewer and verify.
7. **Visual exploration.** The user can move from text to the interactive graph, progressively expand from the cited nodes, filter by type/domain/weight, and refine the question.
8. **Downstream actions (evolution).** From an impact analysis answer one can generate a test checklist, a draft ADR, a ticket, or a notification to the impacted owning teams (reuse of the `automation`, `messaging`, `agent` domains).

### 4.3 Curation and governance cycle

1. **Anomaly triage.** The curators examine the queue of identity conflicts, drift, and fitness function violations.
2. **Annotation.** They add non-derivable context (criticality, deprecations, ADRs) and confirm/refute inferred relationships; every intervention is versioned.
3. **Rule definition.** They update the fitness functions and the domain weights as the architecture evolves.
4. **Periodic review.** They verify connector coverage, graph freshness, and the quality of the AI answers, feeding continuous improvement.

---

## 5. Graph model (node types, relationship types, weight criteria)

The model reuses MySQL for the **structure** (nodes, edges, attributes, weights) and Qdrant for the **semantics** (embeddings of the descriptions linked to the nodes). The type schema is **modular and extensible** (an organization activates only what it needs), consistent with the C4 model and the Backstage entity model.

### 5.1 Node types

| Node type | Description | Primary source |
|--------------|-------------|----------------|
| `Repository` | Git repo | Git |
| `System` | Cohesive set of components (C4 / Backstage System) | catalog-info, inference |
| `Service` / `Component` | Microservice, application, library, site (C4 Container/Component) | AST, catalog-info |
| `Api` | Exposed interface (REST/gRPC/events) | OpenAPI/AsyncAPI |
| `Endpoint` / `Operation` | Single operation of an API | OpenAPI |
| `Event` / `Topic` | Message/topic on bus/queue | AsyncAPI, IaC |
| `Database` / `Datastore` | Relational DB, cache, vector store, bucket | IaC, introspection |
| `Table` / `Dataset` | Table/collection/dataset | DDL, introspection |
| `Library` / `Dependency` | Software dependency (internal/external) | build manifest, SBOM |
| `Vulnerability` / `CVE` | Known vulnerability associated with a component | SBOM/VEX |
| `InfraResource` | Infrastructure resource (cluster, namespace, queue, secret-ref) | IaC, Kubernetes |
| `Pipeline` / `Deployment` | CI/CD pipeline, deploy environment | CI/CD, IaC |
| `Domain` / `BoundedContext` | Logical business grouping (C4 / DDD) | catalog-info, inference |
| `Team` / `Group` | Owning team/group | CODEOWNERS, catalog-info |
| `Person` | Developer/maintainer | Git, directory |
| `Adr` / `Decision` | Architectural decision | Markdown/MADR |
| `Document` | Linked technical documentation | existing `document` domain |

Many of these types (`Document`, `Person`, `Team`) are **shared with other enterprise scopes** (documentation, processes, mail): this is the value of the universal engine, the same "Person" or "Team" node connects architecture and organization.

### 5.2 Relationship types (edges, directed)

| Relationship | From → To | Semantics |
|-----------|--------|-----------|
| `CALLS` | Service → Api/Endpoint | Invokes at runtime / statically |
| `PROVIDES_API` | Service → Api | Exposes the interface |
| `CONSUMES_API` | Service → Api | Consumes the interface |
| `DEPENDS_ON` | Component → Library | Build dependency (direct/transitive) |
| `READS` / `WRITES` | Service → Table/Dataset | Data access (lineage) |
| `PUBLISHES` / `SUBSCRIBES` | Service → Event/Topic | Event production/consumption |
| `IMPORTS` / `CALLS_FN` | File/Class → File/Class | Structural relationship from AST |
| `DEPLOYED_ON` | Service → InfraResource | Runtime placement |
| `DEPLOYED_BY` | Service → Pipeline | Release pipeline |
| `OWNED_BY` | any → Team/Person | Ownership/responsibility |
| `PART_OF` | Component → System → Domain | Hierarchical membership (C4) |
| `STORES_IN` | Service → Database | Persistence |
| `AFFECTED_BY` | Component → Vulnerability | Exposure to CVE |
| `REFERENCES` | Service → Secret-ref/Config | Use of configuration/secret (reference only) |
| `DECIDES` / `GOVERNS` | Adr → Component/Domain | Decision that constrains an element |
| `DOCUMENTED_BY` | any → Document | Link to the documentation |
| `SIMILAR_TO` | node ↔ node | Semantic affinity (from Qdrant) to suggest links |

### 5.3 Edge weight criteria

The weight is a derived and recomputable value (never mutated in-place) that expresses the **strength/importance** of the relationship and drives both the ranking in GraphRAG and the impact analysis. Combined factors:

| Factor | Contribution to the weight | Example |
|---------|--------------------|---------|
| **Runtime frequency** | The more a dependency is observed (OTel), the more it weighs | A calls B 10k times/min ≫ 1 time/day |
| **Static multiplicity** | Number of call/import sites in the code | 50 call sites ≫ 1 |
| **Node criticality** | Business-critical nodes raise the weight of incident edges | payments DB |
| **Consumer cardinality** | An API with many consumers → "heavier" edges (SPOF) | auth API used by 40 services |
| **Relationship type** | `WRITES` weighs more than `READS`; direct dependency more than transitive | data coupling |
| **Source confidence** | Observed relationship (OTel) > declared (catalog) > inferred | provenance |
| **Freshness** | The weight decays over time if the relationship is no longer observed | configurable decay |
| **Severity (for CVE)** | `AFFECTED_BY` weighted by CVSS/exploitability | remediation priority |
| **Curator feedback** | Manual confirmations/refutations adjust the weight | learning |

The weights are **normalized by relationship type** and configurable by domain: one organization may prioritize runtime frequency, another business criticality. Impact analysis uses the weight to order the blast radius (the higher-weight paths = more probable/severe impact).

---

## 6. Data sources & connectors (ingestion)

Ingestion is the heart of the scope: the graph is worth as much as the quality and freshness of its connectors. Architecturally, each connector is an **extractor** that implements a domain port (`ArchitectureSourceConnectorPort`) and an adapter in infrastructure, ideally packageable as a **PF4J plugin** (reuse of the marketplace to distribute connectors for new languages/tools).

### 6.1 Extraction principle: deterministic first, semantic after

The foundational design choice — supported by the 2026 research on the comparison between AST-derived graphs and LLM-extracted graphs — is that **the technical structure is extracted deterministically** (AST parser, schema, contracts, SBOM, traces), because LLM-based extraction introduces stochasticity, schema errors, and variable costs, amplified in agentic contexts and unacceptable for impact analysis. The LLM intervenes *afterward*, to: reason over the graph, generate semantic embeddings of the descriptions, propose `SIMILAR_TO` links, and enrich (with human confirmation) the nodes lacking a description. Determinism for the structure, intelligence for the meaning.

### 6.2 Connector catalog

| Connector | Extracts | Cadence | Priority |
|------------|--------|---------|----------|
| **Git** | Repo, files, structure, history, CODEOWNERS | On every push/scheduled | MVP |
| **Multi-language AST** | Classes, functions, imports, cross-file calls | Incremental per commit | MVP (Java/TS) |
| **Build/Dependency** | Libraries, versions, transitivity | On every lockfile change | MVP |
| **OpenAPI/AsyncAPI** | APIs, endpoints, events, schema | On every contract change | MVP |
| **SBOM (CycloneDX/SPDX/VEX)** | Components, CVEs, severity | Scheduled / from scanner | Near-term evolution |
| **catalog-info (Backstage-like)** | Declared metadata, owner, domain | On every file change | MVP |
| **IaC (Terraform/Helm/K8s)** | Infra resources, queues, secret-refs | Scheduled | Evolution |
| **CI/CD** | Pipelines, environments, artifacts | On every workflow change | Evolution |
| **DB schema (DDL/JDBC)** | Tables, FKs, lineage | Scheduled | Evolution |
| **OpenTelemetry** | Runtime topology, frequency, latency | Streaming/aggregated | Evolution |
| **ADR/Docs** | Decisions, documentation (→ `document` domain) | On every change | Basic MVP |

All connectors operate locally, with mandatory anti-secret filters and provenance tracking. Credentials reside in the self-hosted instance; no data transits outward without opt-in.

---

## 7. Features to create, develop, and maintain (MVP → evolution)

This section concretely maps the features, distinguishing what is **created from scratch**, what is **developed by extending** the existing, and what is **maintained**. The hexagonal architecture is reused: new `architecture` domain in `localmind-domain`, adapters in `localmind-infrastructure`, controllers in `localmind-api`, Angular feature in `localmind-frontend`, jobs in `localmind-batch`, Flyway migrations.

### 7.1 MVP (foundations of the technical graph)

| # | Feature | Type | LocalMind modules involved |
|---|--------------|------|----------------------------|
| 1 | **Core graph model** (typed nodes/edges/weights) shared with the other enterprise scopes | Create | new `graph`/`architecture` domain, MySQL, Flyway |
| 2 | **Node/relationship CRUD API** + queries (neighbors, paths, subgraphs) | Create | `architecture` port/in, `localmind-api` |
| 3 | **Git + AST connector (Java/TS)** for structure and calls | Create | `localmind-batch`, infrastructure adapter |
| 4 | **Build/Dependency connector** (Maven/npm) | Create | infrastructure adapter |
| 5 | **OpenAPI/AsyncAPI connector** | Create | infrastructure adapter |
| 6 | **catalog-info connector** (Backstage-like model) | Create | infrastructure adapter |
| 7 | **Entity resolution & canonical identity** | Create | `architecture` service |
| 8 | **Base weight computation** (static frequency, confidence, freshness) | Create | `architecture` service |
| 9 | **Semantic indexing** of descriptions → Qdrant linked to the nodes | Develop (reuse embedding/Qdrant) | `vectorstore`, `EmbeddingConfig` |
| 10 | **Base GraphRAG**: question → hybrid retrieval → cited answer | Develop (reuse `llm`/chat) | `llm` domain, `knowledge` |
| 11 | **Impact analysis 1–N hop** ("who is impacted by X") | Create | `architecture` service + GraphRAG |
| 12 | **Interactive graph viewer** (nodes/edges/weight, filters, expansion) | Create | Angular `architecture` feature |
| 13 | **Scheduled incremental ingestion** | Develop (reuse `automation`/batch) | `automation`, `localmind-batch` |
| 14 | **IT/EN i18n** of UI, enums (node/relationship types), and documentation | Develop (project constraint) | frontend i18n, bilingual enums |

### 7.2 Evolution (intelligence and coverage)

| # | Feature | Type |
|---|--------------|------|
| 15 | **SBOM connector (CycloneDX/VEX)** + CVE graph → security blast radius | Create |
| 16 | **OpenTelemetry connector** for runtime topology and weights from real frequency | Create |
| 17 | **IaC, CI/CD, DB Schema connectors** for infrastructure coverage and data lineage | Create |
| 18 | **Fitness functions** (architectural rules) + violation detection | Create |
| 19 | **Drift detection** as-designed vs as-built (catalog/ADR vs AST/OTel) | Create |
| 20 | **Graph metrics** (degree, coupling, cycles, SPOF, hotspots, debt) | Create |
| 21 | **Link suggestion** `SIMILAR_TO` and missing relationships | Develop (reuse semantics) |
| 22 | **Downstream actions**: generate test checklist, draft ADR, ticket, owner notification | Develop (reuse `automation`/`messaging`/`agent`) |
| 23 | **PF4J plugins for connectors** (new languages/tools) via marketplace | Develop (reuse PF4J/marketplace) |
| 24 | **Architect agent** that runs periodic audits and produces reports | Develop (reuse `agent`) |
| 25 | **Temporal graph diff** ("how the architecture changed in the last quarter") | Create |
| 26 | **C4/diagram export** and aggregated domain SBOM | Create |

### 7.3 Maintenance (continuous)

- Updating the AST parsers to the new versions of the languages and adding languages.
- Keeping the connector adapters in step with the evolving formats (OpenAPI, CycloneDX, OTel).
- Periodic recomputation and tuning of the weights and the fitness functions.
- Cleanup of orphan/deprecated nodes and management of decay.
- Constant updating of the IT/EN documentation and of the logs in `Sviluppi/` (project constraint).

---

## 8. AI / GraphRAG use cases

The use cases leverage the combination of semantic retrieval (Qdrant) + deterministic navigation (MySQL) + LLM reasoning over the subgraph. All return answers **with citation of nodes and paths**.

| Use case | Typical question | How the graph answers |
|------------|----------------|------------------------|
| **Impact analysis / blast radius** | "What breaks if I change the Payments v2 endpoint?" | N-hop expansion from the consumers, ordered by weight |
| **Consumer/producer tracing** | "Who consumes this event? Who writes to `orders`?" | Query on `CONSUMES_API`/`WRITES` |
| **Path search** | "How does a request get from the gateway to the orders DB?" | Weighted path between two nodes |
| **Security** | "Which services are exposed to CVE-XXXX?" | `Vulnerability` → `AFFECTED_BY` → services |
| **Guided onboarding** | "Explain the orders domain to me" | Subgraph of the domain narrated by the AI |
| **Architectural audit** | "Are there dependency cycles or god services?" | Graph metrics + fitness functions |
| **Drift** | "Where does reality diverge from the ADRs?" | Declared vs observed comparison |
| **Data lineage** | "Where does the customer data in this report come from?" | `READS`/`WRITES`/`PUBLISHES` chain |
| **Refactoring planning** | "How do I split this monolith into bounded contexts?" | Coupling analysis and clustering |
| **Link suggestion** | "Which services should perhaps share a library?" | `SIMILAR_TO` + dependency patterns |

---

## 9. KPIs & success metrics

| Category | KPI | Indicative target |
|-----------|-----|-------------------|
| **Coverage** | % of services/repos present in the graph | > 90% of the estate |
| **Freshness** | Average delay between commit and graph update | < 1 hour (incremental) |
| **Accuracy** | Precision of relationships vs sample manual verification | > 95% for deterministic relationships |
| **Impact analysis** | Time to obtain the blast radius of a change | from hours to < 1 minute |
| **Adoption** | Queries/active user per week; % of teams using the graph | growing, > 60% of teams |
| **AI quality** | % of answers with correct and useful citations (feedback) | > 85% |
| **Security** | Time from CVE publication to list of impacted services | < 5 minutes |
| **Debt** | Trend of dependency cycles / SPOF over time | declining |
| **Onboarding** | Average time to autonomy for a new hire | measurable reduction |
| **Privacy** | % of ingestions with data leaving the instance without consent | 0% |

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---------|---------|-------------|
| **Incorrect structural extraction via LLM** | Unreliable impact analysis | Deterministic extraction (AST/schema/contracts); LLM only for reasoning and semantics |
| **Ambiguous identities / duplicate nodes** | Incoherent graph | Entity resolution with canonical keys, disambiguation queue for the curators |
| **Outdated (stale) graph** | Wrong answers | Frequent incremental ingestion, weight decay, "stale" marking |
| **Scalability of queries on MySQL** | Slowness on large graphs | Targeted indexes, adjacency/closure tables, cache (Caffeine), pre-computation of hot paths; re-evaluate a graph store only if necessary (project constraint) |
| **Leak of code/secrets to the cloud** | IP privacy violation | Local-first, Ollama AI default, anti-secret filters, granular cloud opt-in |
| **Cognitive overload of the visualization** | Illegible graph | Progressive expansion, filters by type/domain/weight, layered C4 views |
| **Connector maintenance** | Erosion of coverage | Connectors as PF4J plugins, contract tests, freshness monitoring |
| **Noise from low-value edges** | "Spaghetti" graph | Weight thresholds, pruning, decay, focus on the relevant relationships |
| **AI hallucinations in the answers** | Distrust | Answers anchored to the subgraph with verifiable citations; no claims without a supporting node |

---

## 11. Maintenance & evolution

Maintenance follows the project constraints: small and cohesive files, immutability (weights and annotations recomputed/added, never mutated in-place), Flyway migrations with a single query, IT/EN documentation always up-to-date, and logs in `Sviluppi/` with dated nomenclature.

**Lines of evolution:**

1. **Language and tool coverage.** Add AST parsers (Go, C#, Python, Rust…) and connectors (new IaC formats, registries, scanners) as PF4J plugins distributed via the marketplace.
2. **From description to prediction.** From reactive impact analysis to the *prediction* of the risk of a change and the proactive suggestion of refactoring, leveraging the temporal graph diff.
3. **Autonomous architect agent.** Periodic audits, detection of emerging anti-patterns, ADR proposal — reuse of the `agent` domain.
4. **Convergence with the other enterprise scopes.** Connect the architecture to processes, tickets, mail, and people for a unified enterprise graph (e.g. "this incident → this service → this team → this decision").
5. **Datastore re-evaluation.** If the size of the graph and the complexity of the queries require it, evaluate (outside the current cycle) a dedicated graph store, maintaining the port abstraction that today hides MySQL.

---

## 12. Integration with the existing LocalMind modules

The scope is not born isolated: it grafts onto the hexagonal architecture and the already-present domains, maximizing reuse.

| Existing module / domain | Role in the Software architecture scope |
|----------------------------|------------------------------------------|
| **`knowledge`** | Base of the graph engine: the technical node/relationship types extend the knowledge domain |
| **`document`** | The `Document`/`Adr` nodes reuse the existing ingestion, Tika, and chunking |
| **`llm` / chat** | GraphRAG engine: provider fallback chain, Ollama default, SSE streaming |
| **`vectorstore` (Qdrant)** | Semantic indexing of the descriptions linked to the nodes |
| **Embedding (`EmbeddingConfig`)** | Ollama `@Primary` for the local embeddings of the technical descriptions |
| **`automation`** | Scheduling of the connectors and the graph recomputations |
| **`localmind-batch`** | Execution of the incremental ingestion/extraction jobs |
| **`messaging`** | Notifications to the owning teams impacted by changes/CVEs |
| **`agent`** | Architect agent for audits and downstream actions |
| **`mcp`** | Exposing the graph as an MCP tool for other agents/IDEs; ingestion from MCP tools |
| **`plugin` (PF4J) + `marketplace`** | Connectors and parsers distributed as installable plugins |
| **`auth`** | Reader/curator/admin roles and visibility of sensitive subgraphs |
| **`common` (events, exceptions)** | Domain events on graph change, typed error handling |
| **`finetuning`** | (Future) fine-tuning of local models on the organization's architectural jargon |
| **Angular 21 frontend** | Standalone `architecture` feature, Signal store, graph viewer, IT/EN i18n |
| **MySQL + Flyway** | Persistence of the graph structure/weights, migrations with one query per file |
| **Spring AI + providers** | Multi-provider LLM abstraction with local AI by default |

In summary, the **Software architecture** scope transforms an organization's technical estate into a weighted, living graph queryable by the AI, integrally reusing the LocalMind infrastructure (hexagonal, MySQL+Qdrant, Ollama, PF4J) and respecting the constraints of local-first, privacy, open source, and IT/EN bilingualism. It is the enterprise building block that demonstrates how "a single graph engine, domains as modules" applies not only to documentary knowledge but to the very structure of software systems.
