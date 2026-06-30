# Customers & Suppliers

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This document describes the **Customers & Suppliers** extension scope (group: *enterprise*) of LocalMind's universal Knowledge Graph engine. The goal is to transform the company's commercial relationships — with those who buy (customers) and those who sell to the company (suppliers) — from a collection of records scattered across CRM, ERP, email and spreadsheets into a **weighted, unified graph navigable by AI** (GraphRAG): organizations, people, contracts, orders, interactions and commercial dependencies become nodes and weighted edges, on which the local AI answers complex questions ("which strategic customers are managed by a single person who is about to leave?", "if this supplier fails, which products and which customers are left exposed?") and surfaces non-obvious connections.

The scope fully reuses the existing stack (hexagonal Spring Boot, Angular 21, MySQL 8.0 for the graph structure, Qdrant for semantics, Ollama as the default local AI) and is delivered as an **installable domain module** via the PF4J plugin system + marketplace. It is designed for the most stringent enterprise constraint: commercial data — pipeline, margins, price lists, contacts, contracts — are among the company's most sensitive assets and **must never leave the perimeter** without explicit consent. Local-first, local AI by default and IT/EN bilingualism are its pillars.

---

## 1. What we solve (problem & value)

### 1.1 The real problem

A company's commercial knowledge is everywhere except in one single place. There is a CRM (Salesforce, HubSpot, Dynamics, Pipedrive…) for the sales pipeline; an ERP/management system for orders, invoices and master data; an email inbox and a calendar where thousands of interactions with customers and suppliers live; shared folders full of PDF contracts; and — almost always — the most valuable part inside the heads of individual people (who is the real decision-maker at that customer, why that supplier is critical, which promise was made verbally on a call). This fragmentation generates structural and costly pains:

1. **No unified view of the relationship (neither customer-side nor supplier-side).** The CRM knows the opportunities, the ERP knows the orders, email knows the conversations, but no one unites them. The same organization appears as "Rossi S.p.A.", "Rossi SpA", "ROSSI S.P.A." and "Rossi spa - Milan office" across four different systems. Without **entity resolution** (identity reconciliation) there is neither a *Customer 360* nor a *Supplier 360*: you work on fragments, you make decisions on partial data, and you make enormous awareness errors (offering a discount to a customer who is already overdue on payment; treating as "new" a supplier with whom there is already an open dispute in another division).

2. **Commercial dependencies are invisible until they explode.** This is the costliest lesson of the modern supply chain: visibility stops at Tier 1, while risk arises in the sub-levels that no one has ever mapped. *How much revenue depends on a single customer?* *Which sellable product depends on a component that comes from a single supplier in a single geographic area?* *If that supplier fails, which contracts toward our customers become impossible to fulfill?* These chains — customer → product → component → supplier → geographic area — exist in the data but are not modeled as navigable relationships. **Risk concentration accumulates silently** until an event triggers it. The 2026 literature estimates that a single disruption can cost up to 42% of annual EBITDA.

3. **Relational knowledge is hostage to people (key-person risk).** Who really knows the decision-maker at a strategic customer? Often a single person. When that person gets sick, goes on vacation or resigns, the relationship breaks down and the value evaporates. The same applies to suppliers: the "historical relationship" with a critical supplier lives in a buyer's memory. No tool today makes explicit and queryable *who oversees what* and *where the company is exposed to a single human point of failure*.

4. **Interactions never become knowledge.** Thousands of emails, calls, meetings, support tickets: each interaction contains signals (an unhappy customer, a promise, a recurring request, a supplier starting to fall behind), but it remains buried in individual mailboxes. It is not linked to the entity, it does not weigh on the health of the relationship, it does not trigger actions. The result is that weak signals of *churn* (customer abandonment) or *supplier deterioration* are noticed too late.

5. **No explainability of commercial decisions.** An AI that said "this customer is at risk" without showing *why* (which interactions, which late payments, which expiring contract) is useless and dangerous in an enterprise context. Likewise, answers based solely on semantic search (vector RAG) produce fluent but often wrong answers, because they ignore explicit relationships. 2026 has elected **GraphRAG** (graph + vectors) as the standard for reliable enterprise AI precisely because it anchors answers to real, citable entities and paths.

### 1.2 Our answer: the unified CRM/SRM graph

LocalMind models customers and suppliers as **a single weighted graph of commercial relationships**, not two separate silos. The same organization can be both customer and supplier at once (a *reciprocal* relationship, very common in B2B), and the engine represents it naturally.

- Every **organization** (customer, supplier, prospect, partner) is a node, linked to its **people** (contacts, decision-makers, buyers), to its **sites**, to its **corporate hierarchies** (group → subsidiary → division).
- Every **contract**, **opportunity**, **order** and **invoice** is a node linked to the organization and the people involved, with dates, values and status.
- Every **interaction** (email, call, meeting, ticket, note) is a node linked to the entity and the person, with a **sentiment** and a weight that feed the "relationship health".
- **Commercial dependencies** are explicit edges: customer *depends on* product, product *requires* component, component *supplied by* supplier, supplier *located in* area, person *oversees* customer.
- Everything is **weighted**: weight of the customer↔company bond (revenue, margin, seniority), supplier criticality (irreplaceability, lead time, % of spend), strength of the interpersonal relationship (frequency and quality of interactions).

On this graph the local AI (GraphRAG) **navigates, explains and anticipates**: it answers in natural language by combining relational constraints (paths, neighborhoods, subgraphs) and semantics (content of emails and contracts via Qdrant), citing the nodes and paths used. And — a key point of the project — it **surfaces non-obvious connections**: a risk concentration three hops away, a customer "orphaned" of oversight, a supplier that also appears as a customer with an overdue receivable.

### 1.3 Value in three levers

| Value lever | What it enables | Concrete example |
|----------------|--------------|------------------|
| **Unified view (360°)** | Customer 360 and Supplier 360 reconciled from multiple sources | "Show me everything about Rossi S.p.A.": open opportunities, orders, overdue invoices, last 10 interactions, expiring contracts, contact people and who oversees them — on a single screen, citable |
| **Dependency visibility** | Map of customer and supplier concentration risk, single point of failure | "If ACME Components fails, which products, contracts and customers are left exposed, for how much revenue?" answered by traversing the graph across multiple levels |
| **Signal anticipation** | Relationship health, customer churn and supplier deterioration | Proactive alert: "3 strategic customers show a drop in interactions + late payments + contract expiring in the next 60 days" |

### 1.4 Why LocalMind and not just the CRM/ERP

- **Local-first and absolute privacy.** Sales pipeline, margins, price lists, contracts and the customer/supplier address book are among the company's most sensitive data. In LocalMind they stay *on-premise*; the AI that processes them is Ollama running locally by default. No commercial data leaves toward the cloud without explicit and configurable consent. This is the requirement that blocks the adoption of many commercial "as-a-service" AIs.
- **Unification, not replacement.** LocalMind does not replace the CRM or the ERP: it **ingests and connects them**. It becomes the relational intelligence layer *on top of* the existing systems, filling the gap that none of them fills (the network of cross-cutting relationships and dependencies).
- **Explainability and trust.** Every AI answer cites the nodes and paths: a commercial decision supported by traceable evidence, not by a "black box".
- **Engine universality.** The same engine used for tourism and other enterprise scopes serves customers & suppliers: the node/relationship types change, not the infrastructure. Development and maintenance cost drastically reduced.

### 1.5 What it is NOT (value boundaries)

It is not a complete CRM (it does not manage marketing campaigns, sales automations, quote configurators) nor an ERP/management system (it does not issue invoices, does not keep accounting, does not manage the warehouse). It is not a transactional platform. It is a **knowledge graph and decision intelligence layer** that feeds on those systems and returns a unified view, a dependency map and anticipated signals. The transactional source of truth remains the CRM/ERP; LocalMind is the *relational* source of truth.

---

## 2. Personas & target users

| Persona | Profile | Main goal | Needs from the graph |
|---------|---------|----------------------|-------------------|
| **Commercial director / Sales manager** | Leads the sales team, reasons about pipeline and key customers | Understand where value and risk lie in the customer portfolio | Revenue concentration, churn-risk customers, key-account oversight, deviations on expiring contracts |
| **Account / Sales rep** | Manages a set of customers | Arrive prepared for every interaction | Instant Customer 360, interaction history, next actions, decision-maker map |
| **Procurement manager** | Manages suppliers and spend | Reduce supply risk and optimize spend | Supplier 360, criticality and single point of failure, possible alternatives, contract deadlines |
| **Buyer / Category manager** | Oversees purchasing categories | Supplier health by category | Spend concentration, performance/delays, product↔component↔supplier dependencies |
| **Risk / Compliance manager** | Monitors risks and exposures | Map and mitigate concentrations | Customer and supplier single point of failure, geographic exposure, conflicts (same entity as customer and supplier with a dispute) |
| **CFO / Management control** | Economic-financial view | Exposure and margin of the relationship | Overdue receivables per customer, % of spend per supplier, contracted value, concentration risk |
| **Customer Success / Support** | Manages the post-sale relationship | Prevent abandonment | Sentiment of interactions, tickets, relationship health, weak signals |
| **Self-hoster / Data engineer** | Installs LocalMind on-prem | Private and governed commercial data pipeline | Connectors (CRM/ERP/email), graph API, full data control, audit |

**Anti-persona:** the micro-business with ten customers and three suppliers managed from memory — LocalMind's value grows with the number of entities, data sources and the complexity of dependencies; below a certain threshold, a spreadsheet is enough.

---

## 3. Input requirements

This section is deliberately detailed: it defines *everything that enters* the graph, *from where* and *with which quality and validation rules*. Inputs are divided into: master-data entities (who), transactional inputs (what happens economically), relational/interaction inputs (what happens in the relationship), dependency inputs (the product/component chain), and configuration inputs (how to weight and reconcile). Everything must be validated at the boundary (untrusted source system = untrusted data).

### 3.1 Master-data entities — organizations

The organization is the cornerstone node. It can be customer, supplier, prospect, partner or **several roles at once**.

| Field | Typical origin | Mandatory | Validation notes |
|-------|----------------|--------------|---------------------|
| Company name | CRM / ERP | yes | used for entity resolution (normalization) |
| Tax identifier (VAT / Tax code / DUNS) | ERP / registries | strongly recommended | **strong key** for reconciliation; validate format |
| Commercial role(s) | derived | yes | IT/EN enum: Customer, Supplier, Prospect, Partner, Customer+Supplier |
| Registered office + operating sites (address, country, geo) | CRM / ERP | registered office yes | the country feeds geographic exposure |
| Corporate hierarchy (group / parent / division) | ERP / entry | no | enables risk roll-up at group level |
| Sector / merchandise category | CRM | no | IT/EN enum/taxonomy |
| Size (revenue, employees) | CRM / enrichment | no | for segmentation |
| Relationship status (active, suspended, terminated, in dispute) | derived/manual | yes | IT/EN enum |

**Quality rule:** an organization without any strong key (VAT/DUNS) enters the graph but is flagged as a *duplicate candidate* and subjected to entity resolution with a higher confidence threshold before being merged with an existing node.

### 3.2 Master-data entities — people (contacts)

People are the fabric of the relationship and the key to *key-person risk* (both customer-side and internal-team-side).

- **External contacts:** name, role/function, email, phone, organization of belonging, **decision-making role** (decision-maker, influencer, user, gatekeeper), site.
- **Internal people (owners):** who in the company *oversees* a relationship (account owner, responsible buyer), with their own role and team.
- **Privacy validation (GDPR):** personal data are subject to minimization, legal basis and the right to be forgotten; the module must allow deletion/anonymization of the person node without destroying the aggregated history of the relationship.

### 3.3 Transactional inputs — contracts, opportunities, orders, invoices

They come largely from the CRM (sales-side) and the ERP (execution/administration-side).

| Object | Key fields | Origin | Notes |
|---------|--------------|---------|------|
| **Opportunity / Deal** | value, stage, probability, expected close date | CRM | customer-side; feeds pipeline and forecasts |
| **Contract** | counterparty, subject, value, start date, **expiry/renewal**, SLA/penalties | CRM/ERP/PDF | both customer (sale) and supplier (purchase); the expiry is critical for alerts |
| **Order** | counterparty, products/lines, amount, date, status | ERP | links organization ↔ products |
| **Invoice / Payment** | amount, issuance, due date, **payment status**, delay days | ERP | delays feed relationship health and exposure |
| **Price list / Conditions** | prices, discounts, payment terms | ERP/CRM | sensitive; stays on-prem |

**Quality rule:** every transactional object must attach to a *resolved* organization; an "orphan" order/invoice (non-reconcilable counterparty) ends up in a *data quality* queue for human intervention, it is not silently discarded.

### 3.4 Relational inputs — interactions

They are the flow that keeps the graph "alive" and that feeds the relationship-health weights. They come from the existing LocalMind modules **email** and **calendar**, as well as from CRM and ticketing.

- **Email:** sender/recipients, subject, body (→ Qdrant embedding + entity extraction), thread, direction (in/out), date.
- **Meetings/Calls:** participants, date, duration, optional transcription (via Whisper, already present), notes.
- **Support tickets / complaints:** counterparty, category, priority, status, outcome.
- **Manual notes:** account/buyer notes ("the decision-maker changes in September").
- **Derived sentiment:** each interaction receives a sentiment (positive/neutral/negative) computed by the local AI, which weighs the relationship health.

**Privacy/sensitivity rule:** interactions are the most sensitive data. Entity and sentiment extraction happens **locally** (Ollama) by default; the email↔organization link requires consent and respects role-based visibility boundaries.

### 3.5 Dependency inputs — the commercial chain

It is the module's differentiator: what enables risk analysis. Often these data do not exist in a single system and must be **built by connecting** ERP, bills of materials (BOM) and human knowledge.

- **Product/service sold** → requires → **component/raw material/service** → supplied by → **supplier** → located in → **geographic area**.
- **Customer** → buys → **product** (from orders), closing the customer↔supplier chain.
- **Customer contract** → guarantees → **product/SLA** which in turn depends on suppliers: thus a supplier risk propagates up to the exposed customer contract.

**Multi-level depth (multi-tier):** where possible, model the supplier's supplier (sub-tier), because risk concentration arises precisely below Tier 1. Even a partial mapping of the sub-levels has enormous value.

### 3.6 Configuration inputs — weights, thresholds, reconciliation

It defines *how* the engine interprets the data. Everything configurable, with reasonable defaults and IT/EN bilingual.

- **Customer bond weights:** how much revenue, margin, seniority, interaction frequency, recency count.
- **Supplier criticality weights:** how much % of spend, irreplaceability (number of alternatives), lead time, performance/delays, geographic exposure count.
- **Risk thresholds:** e.g. "strategic customer = top 10% of revenue"; "single point of failure = sole supplier of a component in a single country"; "critical concentration = >X% revenue on one customer".
- **Entity resolution rules:** strong keys (VAT/DUNS) and weak keys (normalized name, email domain, address), auto-merge vs human-review thresholds.
- **Privacy/visibility policies:** which roles see margins, price lists, contacts; what can be sent to a possible cloud provider (default: nothing).

### 3.7 Data quality summary (cornerstones)

| Cornerstone | Why it is critical | Consequence if missing |
|---------|------------------|--------------------------|
| Entity resolution (identity) | Without it, no unified view and the graph gives wrong but confident answers | Duplicates, 360° impossible |
| Attaching transactions↔resolved entity | Ties economic value to the relationship | Exposure and risk not computable |
| Dependency chain | Enables single point of failure analysis | Concentration risk invisible |
| Internal owner (oversight) | Enables key-person risk | "Orphan" customers undetectable |

---

## 4. Activity flow (step-by-step)

The flow describes the complete life cycle: from connecting the sources to building the graph, through to daily use and proactive alerts. It is designed to be **incremental**: each step brings value even on its own.

### 4.1 Phase 0 — Module installation and configuration

1. The administrator installs the **Customers & Suppliers** module from the *marketplace* (PF4J plugin).
2. They choose the language (IT/EN) and confirm the privacy settings: **local AI (Ollama) by default**, no cloud transmission without explicit consent per source/field.
3. They configure the base weights and thresholds (section 3.6) or accept the reasonable defaults.
4. The module creates its own node/relationship types in the graph schema (modular extension) and the necessary Flyway migrations (one query per file).

### 4.2 Phase 1 — Source connection and ingestion

1. The user connects the sources through the **connectors** (section 6): CRM, ERP/management system, email/calendar inboxes (existing modules), contract folders, optional CSV/Excel.
2. For each source they choose **scope and frequency** (e.g. only active organizations; incremental nightly sync).
3. Ingestion runs as a **batch job** (Spring Batch, reusing the folder/document infrastructure): it extracts master data, transactions, interactions; contract PDFs go through Tika/OCR to extract text and key clauses (expiry, value, penalties).
4. Textual content (emails, contracts, notes) is **chunked and embedded into Qdrant** for semantic search; structured metadata ends up in MySQL as nodes/edges.
5. Every source record retains its **provenance** (system, original id, timestamp) for audit and explainability.

### 4.3 Phase 2 — Entity resolution and graph construction

This is the heart of quality. Without it, everything else collapses.

1. **Normalization:** company names, addresses and names are normalized (removal of corporate forms, capitalization, spaces, aliases).
2. **Matching:** comparison on strong keys (VAT/DUNS) and weak keys (normalized name, email domain, address, phone). The local AI can assist fuzzy matching.
3. **Resolution:** high-confidence match → **auto-merge** into a single organization node (with the source record as provenance); uncertain matches → **human-review queue** with a suggestion and rationale.
4. **Hierarchies and roles:** the group→subsidiary, organization→people, organization→role (customer/supplier/both) edges are built.
5. **Transactions and interactions** are attached to the resolved entities; orphans go to the *data quality* queue.
6. **Dependency chain:** from orders, BOM and manual inputs the product→component→supplier→area and customer→product edges are built.

### 4.4 Phase 3 — Computing weights and indicators

1. For each **customer↔company** edge the weight is computed (revenue, margin, seniority, interaction frequency/recency).
2. For each **supplier** the **criticality** is computed (% of spend, irreplaceability = number of alternatives in the graph, lead time, performance/delays, geographic exposure).
3. For each relationship the **health** is computed (interaction trend, average sentiment, payment delays, open tickets, imminent deadlines).
4. The **structural risk indicators** are computed by traversing the graph: revenue concentration per customer, spend concentration per supplier, **single point of failure** (components/products with a single supplier, in a single area), **key-person risk** (entities overseen by a single owner).
5. All weights are **recomputed** when new data arrives (incremental) and explainable (you can always trace back to the factors).

### 4.5 Phase 4 — Daily use (exploration, search, 360°)

1. The user opens the **360° view** of a customer or supplier: resolved master data, people and who oversees them, transactions, recent interactions, expiring contracts, health and risk indicators — all on one node, with links to neighbors.
2. They **explore the graph** starting from a node and expanding by relationships (customers→products→suppliers), with filters by node/relationship type and by weight/risk.
3. They **query in natural language** (GraphRAG): the local AI translates the question into graph traversal + semantic search, and answers **citing nodes and paths**.
4. They **compare** entities (e.g. two alternative suppliers for the same component) or **simulate** ("if I remove this supplier, what is left exposed?").

### 4.6 Phase 5 — Proactive alerts and actions

1. The module monitors the graph and generates **alerts** when a threshold is exceeded or a trend deteriorates: contract expiring, strategic customer with a drop in interactions + payment delays (churn signal), critical supplier with increasing delays (deterioration), new risk concentration emerged.
2. The alerts are **explained** (which factors, which nodes) and routable through the existing **messaging/automation** module (notify the owner, create a task).
3. The user acts; the action/feedback returns into the graph (e.g. "risk mitigated: alternative supplier added"), improving future weights.

### 4.7 Phase 6 — Continuous graph maintenance

1. Periodic incremental syncs realign the graph to the sources (new transactions, new contacts, renewed contracts).
2. The **data quality / entity resolution queue** is overseen: humans confirm uncertain merges, fix orphans.
3. **Provenance** guarantees that every node/edge is traceable and deletable (GDPR: right to be forgotten for people).

### 4.8 Synthetic flow diagram

```
Sources (CRM, ERP, Email/Cal, Contract PDFs, CSV)
   │  connectors + batch (Tika/OCR)
   ▼
Ingestion → text→Qdrant (semantics) | structure→MySQL (nodes/edges)
   │
   ▼
Entity Resolution (strong/weak keys, auto-merge / human review)
   │
   ▼
Unified CRM/SRM graph  ── weights & indicators (bond, criticality, health, risk)
   │
   ├─► 360° view + Graph exploration + Comparison/Simulation
   ├─► GraphRAG (local AI, cited answers)
   └─► Proactive alerts → messaging/automation → action → feedback into the graph
```

---

## 5. Graph model (node types, relationship types, weighting criteria)

The model reuses the universal engine's convention: **typed nodes** + **weighted edges**, structure on MySQL and semantics on Qdrant. The following types are specific to the scope and extensible.

### 5.1 Node types

| Node type | Description | Main attributes |
|--------------|-------------|----------------------|
| `Organizzazione` | Commercial entity (customer/supplier/prospect/partner) | company name, VAT/DUNS, roles, country, sector, status |
| `Persona` | External contact or internal owner | name, role, decision-making role, email, organization |
| `Sede` | Registered/operating site | address, country, geo |
| `Contratto` | Sales or purchase agreement | subject, value, start date, expiry, SLA/penalties |
| `Opportunità` | Sales deal | value, stage, probability, expected close date |
| `Ordine` | Purchase/sales order | lines, amount, date, status |
| `Fattura` | Administrative document | amount, due date, payment status, delay |
| `Interazione` | Email, call, meeting, ticket, note | type, date, direction, sentiment, summary |
| `Prodotto/Servizio` | What is sold/purchased | name, category, code |
| `Componente/Materia prima` | Input of a product | name, code, criticality |
| `AreaGeografica` | Country/region of a supplier | name, code, geo risk level |
| `CategoriaSpesa` | Purchasing merchandise category | name, taxonomy |
| `RischioConcentrazione` | Derived node that reifies an exposure | type, level, factors |

### 5.2 Relationship types (edges)

| Relationship | From → To | Meaning | Direction |
|-----------|--------|-------------|-----------|
| `HA_RUOLO` | Organization → Role | customer / supplier / both | — |
| `CONTROLLA` | Organization → Organization | corporate hierarchy (group→subsidiary) | directed |
| `HA_REFERENTE` | Organization → Person | person belongs to the org | directed |
| `PRESIDIA` | Person(internal) → Organization | owner who manages the relationship | directed |
| `RIPORTA_A` | Person → Person | internal decision-making hierarchy at the customer | directed |
| `HA_CONTRATTO` | Organization → Contract | active/historical contract | directed |
| `HA_OPPORTUNITÀ` | Organization → Opportunity | deal in pipeline | directed |
| `HA_ORDINE` / `HA_FATTURA` | Organization → Order/Invoice | transaction | directed |
| `HA_INTERAZIONE` | Organization/Person → Interaction | contact that occurred | directed |
| `ACQUISTA` | Customer → Product | the customer buys the product | directed |
| `RICHIEDE` | Product → Component | bill of materials/technical dependency | directed |
| `FORNISCE` | Supplier → Component/Product | the supplier provides the input | directed |
| `LOCALIZZATO_IN` | Supplier → GeographicArea | geographic exposure | directed |
| `GARANTISCE` | Contract(customer) → Product/SLA | commitment toward the customer | directed |
| `ALTERNATIVO_A` | Supplier → Supplier | substitutable suppliers for the same component | reciprocal |
| `ESPONE_A` | (chain) → ConcentrationRisk | reifies a single point of failure | directed |
| `SIMILE_A` | node → node | semantic similarity (from Qdrant) | reciprocal |

### 5.3 Edge weighting criteria

The weight is what makes the graph *navigable with priority* by the AI. Each edge category has a configurable and **explainable** formula (always decomposable into factors).

| Edge / indicator | Weight factors | Logic |
|-------------------|-----------------|--------|
| **Customer↔company** bond | revenue, margin, seniority, interaction frequency+recency | high weight = strategic customer; grows with value and a living relationship |
| **Supplier criticality** | % of spend, irreplaceability (1/number of alternatives), lead time, geographic exposure, performance | high weight = critical/irreplaceable supplier |
| **Interpersonal** relationship strength | interaction frequency, average sentiment, recency, contact's decision-making role | measures how well the relationship is overseen and healthy |
| Relationship **health** | interaction trend, sentiment, payment delays, open tickets, deadlines | falling = churn/deterioration signal |
| **Concentration risk** | share on a single node, number of alternative paths (density/clustering), geo exposure | high = single point of failure |
| `SIMILE_A` (semantic) | embedding cosine distance (Qdrant) | for suggesting non-obvious connections |

**Weighting principles (consistent with the universal engine):** normalized and configurable weights; incremental recomputation when new data arrives; temporal decay (old interactions weigh less); every weight is traceable to its factors for GraphRAG explainability; human feedback (handled alert, confirmed merge) re-feeds the weights.

---

## 6. Data sources & connectors (ingestion)

Ingestion reuses the batch infrastructure (Spring Batch), text extraction (Tika/OCR), embeddings (Qdrant) and the existing email/calendar modules. The connectors are **PF4J plugins** installable from the marketplace, each with configurable scope and frequency.

| Source | Connector | What it extracts | Local-first notes |
|-------|------------|-------------|------------------|
| **CRM** (Salesforce, HubSpot, Dynamics, Pipedrive…) | API/REST plugin | organizations, contacts, opportunities, contracts, activities | credentials on-prem; scheduled pull |
| **ERP / management system** (SAP, Dynamics, Odoo, Italian ERPs) | DB/API plugin | master data, orders, invoices, payments, price lists, BOM | the transactional source of truth |
| **Email** (IMAP) | existing `email` module | interactions, threads, attachments | local extraction/sentiment (Ollama) |
| **Calendar** | existing `calendar` module | meetings, participants | linking interactions↔entities |
| **Contracts / documents** | `document` module + folder watcher | contract PDFs → text, clauses (expiry, value, penalties) | Tika + OCR + chunk→Qdrant |
| **CSV / Excel** | generic importer | master data and legacy data | for data not in a system |
| **External enrichment** (company registries, DUNS, geo risk) | optional plugin | strong keys, corporate data, country risk | **opt-in**: requires consent (leaves the perimeter) |

**Ingestion principles:** incremental and idempotent (restarts without duplicating); provenance on every record; everything that is text → Qdrant, everything that is structure → MySQL; external enrichment is always opt-in and tracked, because it is the only flow that can let data leave the perimeter.

---

## 7. Features to create, develop and maintain (MVP → evolution)

A concrete map of the features, distinguishing **MVP** (first shippable value), **evolution** (later phases) and **maintenance** (what must be overseen over time). Each item anchors to the hexagonal structure: domain (`crm` or `relations`), in/out ports, infrastructure adapters, `/api/v1/...` controllers, Angular features.

### 7.1 MVP — first shippable value

| Feature | What it includes | Technical notes |
|--------------|----------------|---------------|
| CRM/SRM graph model | Nodes/edges from section 5 on MySQL, semantics on Qdrant | new domain + Flyway migrations (one query/file) |
| CSV/Excel connector + 1 CRM connector | Master-data + basic transaction ingestion | batch reuse; PF4J plugin |
| Basic entity resolution | Normalization + match on strong/weak keys, auto-merge + review queue | domain service; optional local AI assist |
| Email interaction ingestion | Email↔organization link + local sentiment | reuse of the email module + Ollama |
| 360° view (customer/supplier) | Unified panel: master data, people, transactions, interactions, deadlines | controller + Angular feature |
| Interactive graph exploration | Navigation by relationships, filters by type/weight | reuse of the engine's graph visualization |
| Basic weights + indicators | Customer bond, supplier criticality, health | configurable domain service |
| GraphRAG on the graph | Natural-language Q&A with node/path citation | reuse of the LLM/RAG pipeline, local AI by default |
| Contract expiry alerts | Notification of expiring contracts | reuse of messaging/automation |
| IT/EN bilingualism | Translated UI and enums | project constraint |

### 7.2 Evolution — later phases

| Feature | Added value |
|--------------|-----------------|
| Native ERP connectors (SAP, Odoo, Italian ERPs) | complete transactional data, BOM for the dependency chain |
| Multi-level dependency chain (sub-tier) | deep concentration and single point of failure analysis |
| Structural risk engine | revenue/spend concentration, SPOF, key-person risk, geographic exposure |
| "What-if" simulation | "if this supplier/customer fails, what is left exposed?" |
| Predictive churn/deterioration | anticipated weak signals (interactions+payments+sentiment) |
| Advanced entity resolution | householding/hierarchies, survivorship, opt-in DUNS enrichment |
| Non-obvious connection suggestion | alternative suppliers, cross-sell, customer↔supplier conflicts |
| Executive dashboards | portfolio concentration, supplier health by category |
| Multimodal on contracts | advanced clause extraction, version comparison |
| Advanced automations | playbooks on alerts (task, escalation) via automation |

### 7.3 Maintenance — to be overseen over time

- **Connector quality:** CRM/ERP APIs change; plugins must be versioned and tested.
- **Entity resolution / data quality queue:** requires continuous human oversight (uncertain merges, orphans).
- **Weight and threshold tuning:** review periodically with the commercial/procurement contacts.
- **Privacy & GDPR:** handling the right to be forgotten, legal basis, audit of accesses and opt-in cloud flows.
- **Flyway migrations:** one query per file; evolving and backward-compatible graph schema.
- **Bilingual documentation:** IT/EN always aligned; development tracking in the `Sviluppi` folder.

---

## 8. AI / GraphRAG use cases

GraphRAG combines graph traversal (explicit relationships, weights) and semantic search (Qdrant on emails/contracts/notes), with local AI (Ollama) by default. Every answer **cites nodes and paths**.

| # | User question | How the AI answers (graph + semantics) |
|---|---------------------|----------------------------------------|
| 1 | "Give me everything about Rossi S.p.A." | 360° view: aggregates linked nodes (contracts, orders, overdue invoices, interactions, people, owner) and synthesizes |
| 2 | "Which strategic customers are overseen by a single person?" | Traverses `PRESIDIA`, cross-references with the customer bond weight; key-person risk emerges |
| 3 | "If ACME Components fails, what is left exposed?" | Traverses Supplier→Component→Product→Customer/Contract; sums exposed revenue |
| 4 | "Which suppliers are single points of failure?" | Finds components with a single `FORNISCE` and no `ALTERNATIVO_A`, weighted by spend |
| 5 | "Which customers are about to leave us?" | Combines drop in interactions, negative sentiment, payment delays, expiring contract |
| 6 | "Is this prospect already a supplier with whom we have a dispute?" | Entity resolution + multiple roles + relationship status; flags the conflict |
| 7 | "How much revenue depends on my top 5 customers?" | Concentration: sums bond weights; assesses concentration risk |
| 8 | "Find an alternative supplier for component X in another area" | `FORNISCE` + `ALTERNATIVO_A` + `LOCALIZZATO_IN` different from the at-risk area |
| 9 | "Which contracts expire in the next 90 days and how much are they worth?" | Filters `Contratto` by expiry, sorts by value, groups by owner |
| 10 | "Prepare the brief for the call with Beta Srl's decision-maker" | Synthesizes recent interactions, open opportunities, decision-making role, latest signals |

**Non-obvious connection suggestion** (heart of the project): the AI proposes missing links — the same contact person at two different customers, a concentrated supplier that feeds multiple strategic products, a customer with cross-sell potential similar (via `SIMILE_A`) to an already-acquired customer.

---

## 9. KPIs & success metrics

| Category | KPI | Indicative target |
|-----------|-----|-------------------|
| Graph quality | % of entities with a resolved strong key (no duplicates) | > 95% |
| Graph quality | % of transactions attached to a resolved entity | > 98% |
| Coverage | % of active customers/suppliers imported and linked | > 90% |
| Interaction coverage | % of email interactions linked to an entity | > 80% |
| Risk visibility | % of spend covered by the dependency map (at least Tier 1) | > 85% |
| Signal anticipation | average lead time on churn/deterioration | weeks before the event |
| AI effectiveness | % of GraphRAG answers with correct citations (no hallucinations) | > 90% |
| Adoption | number of 360° views / graph queries per active user / week | growing |
| Operational value | contracts renewed on time thanks to alerts | ↑ renewal rate |
| Risk value | number of single points of failure identified and mitigated | tracked over time |
| Privacy | % of flows that stay on-prem (no cloud) | 100% by default |

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---------|---------|-------------|
| **Incorrect entity resolution** (wrong merges or duplicates) | Unreliable 360° view, confident but wrong answers | Strong keys prioritized, prudent thresholds, human-review queue, tracked provenance, reversibility of merges |
| **Sensitive commercial data exposed** | Enormous damage, loss of trust | Local-first by default, cloud only opt-in and tracked, role-based visibility, encryption at rest |
| **GDPR / personal data** | Sanctions, legal problems | Minimization, legal basis, right to be forgotten on the person node without destroying aggregates, audit |
| **Incomplete dependency chain (only Tier 1)** | Sub-level risk invisible | BOM ingestion, manual input of sub-tiers, explicitly flag the unmapped areas |
| **Unstable or limited CRM/ERP APIs** | Fragile ingestion | Versioned connectors, idempotent sync, CSV fallback, retry |
| **AI hallucinations** | Wrong decisions | GraphRAG with mandatory node/path citation; answers anchored to the graph, not only to vectors |
| **Poorly tuned weights** | False alarms or missed signals | Configurable and explainable weights, periodic review with the contacts, feedback loop |
| **Adoption resistance** (it looks like "another system") | Low usage | Clear positioning: it does not replace, it *unifies*; immediate value from the 360° view |
| **Poor source data quality** | Garbage in, garbage out | Data quality queue, boundary validation rules, visible coverage indicators |

---

## 11. Maintenance & evolution

- **Graph quality oversight:** the entity resolution and data quality queue is a continuous process, not a one-off activity; it needs an internal owner.
- **Incremental and idempotent syncs:** scheduled (e.g. nightly), with monitoring of errors and coverage; each run retains provenance.
- **Connector versioning:** external APIs change; PF4J plugins must be maintained and tested against new CRM/ERP versions.
- **Graph schema evolution:** new node/relationship types are added in a backward-compatible way; Flyway migrations with a single query per file.
- **Weight and threshold tuning:** periodic review with commercial and procurement to keep the indicators (strategic, critical, SPOF) aligned with reality.
- **Operational privacy:** periodic audit of accesses and of the (rare) opt-in cloud flows; handling of GDPR requests.
- **Documentation and tracking:** bilingual IT/EN documentation always up to date; every development tracked in the `Sviluppi` folder with the dated naming convention; enums translated and delivered to the frontend.
- **Growth roadmap:** from native ERP connectors, to the multi-tier chain, to the structural risk engine and what-if simulation, up to churn/deterioration prediction.

---

## 12. Integration with existing LocalMind modules

| Module / domain | Role in the Customers & Suppliers scope |
|------------------|----------------------------------------|
| `knowledge` | Base of the graph engine: the scope adds its own node/relationship types to it (modular extension) |
| `llm` | GraphRAG and sentiment via the fallback chain (Ollama default → cloud opt-in); explanations and synthesis |
| `document` | Contract/document ingestion: Tika + OCR + chunking → Qdrant for semantics |
| `email` | Email interaction ingestion (IMAP), linked to entities; local extraction |
| `calendar` | Meetings/calls as interactions linked to entities and people |
| `mcp` | Exposure of the graph as an MCP tool and/or consumption of external tools for enrichment |
| `automation` | Playbooks on alerts (task, escalation, renewals) |
| `messaging` | Alert notifications to owners on the configured channels |
| `marketplace` + `plugin` (PF4J) | Distribution of the module and of the CRM/ERP connectors as installable plugins |
| `agent` | Agents that query the graph and execute actions (prepare briefs, monitor risks) |
| `auth` | Role-based visibility on sensitive data (margins, price lists, contacts) |
| `finetuning` | Local adaptation of the models to the company's commercial terminology (evolution) |
| `common` (analytics/backup) | Adoption metrics and backup of the commercial graph |
| **Infrastructure** | MySQL (graph structure) + Qdrant (semantics) + Spring Batch (ingestion) + hexagonal architecture (pure domain, `DomainConfig` wiring) |
| **Angular 21 frontend** | New lazy-loaded feature: 360° view, graph exploration, risk dashboard, Signal store, IT/EN |

In summary, the **Customers & Suppliers** scope introduces no new infrastructure: it is a **domain module** that reuses the graph engine, the local LLM/RAG pipeline, the plugin connectors and the existing ingestion, adding the node/relationship types, weights and indicators specific to CRM/SRM, in full compliance with LocalMind's local-first, privacy, open source and IT/EN bilingualism constraints.

---

*Document written on 2026-06-29 as a guide to the developments of the Customers & Suppliers scope (enterprise group) of LocalMind's universal Knowledge Graph engine.*
