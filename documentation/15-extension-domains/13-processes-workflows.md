# Processes & Workflows

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This document describes the **Processes & workflows** extension scope (group: *enterprise*) of LocalMind's universal Knowledge Graph engine. The goal is to transform business process knowledge — today scattered across forgotten BPMN diagrams, standard operating procedures (SOPs) in PDF, unwritten rules in people's heads, and execution traces spread across dozens of systems — into a **weighted, living, AI-queryable graph** that connects *steps*, *roles*, *systems*, *documents*, *decisions* and *data*, and that the AI can traverse (GraphRAG) to answer questions such as "who approves an expense report above €5,000 and which system do they use?", "where does the onboarding process really get stuck?" or "if Maria leaves, which processes are left without an approver?".

The scope fully reuses the existing stack (hexagonal Spring Boot, Angular 21, MySQL 8.0 for the graph structure, Qdrant for semantics, Ollama as the default local AI) and is delivered as an **installable domain module** through the PF4J plugin system + marketplace, in full compliance with LocalMind constraints: local-first / self-hostable, absolute privacy of corporate process data, open source, and IT/EN bilingualism. It is the first scope in which the graph describes not *objects* (places, real estate, documents) but **behavior organized over time**: *how* the company works.

---

## 1. What we solve (problem & value)

### 1.1 The real problem: process knowledge is invisible and fragmented

In almost every organization, "how something is done" exists in at least five **disconnected** forms, and none of them is the complete truth:

1. **The *designed* process (to-be).** It lives in BPMN diagrams, Visio maps, slides, ISO 9001 quality manuals, standard operating procedures (SOPs) and policies. It is formal but almost always **obsolete**: it describes how the process *should* work, rarely how it works today. It is written in diagrammatic languages that, as the knowledge graph literature notes, are not directly usable by AI without an explicit transformation into nodes and relationships.

2. **The *executed* process (as-is).** It lives in the execution logs of systems (ERP, CRM, ticketing, HR management, workflow engine, mail, calendar). It is the *factual* truth — who did what, when, in what order — but it is unreadable for a human being and scattered across dozens of applications that don't talk to each other. This is the territory of *process mining*.

3. **The *lived* process (tribal knowledge).** It lives in people's heads: "an invoice above 10k must also be seen by the CFO", "if the customer is strategic, skip step X", "Marco knows how to unblock the legacy system". It is the most precious knowledge and the most **fragile**: it leaves with whoever leaves the company, it is not searchable, it is not verifiable.

4. **The *automated* process.** It lives in scripts, RPA, workflow engines, integrations, cron jobs, routing rules. It works until it doesn't, but **nobody has the map** of which automations touch which steps and what happens if one breaks.

5. **The *regulated* process.** Compliance constraints, separation of duties (SoD), audit trail, GDPR, contractual requirements. Often disconnected from the actual execution, so violations are only discovered during an audit.

The structural pain is that **these five views are not connected**. When someone asks "how does the purchase approval process work?", the answer requires manually triangulating an old diagram, the experience of three colleagues, the ERP logs and the quality manual — a job that takes days and produces a partial, untraceable answer.

### 1.2 The measurable consequences

| Symptom | Cost to the organization |
|---------|----------------------------|
| **Knowledge loss / bus factor.** Process knowledge leaves with people | Extremely slow onboarding, dependence on individuals, paralysis when a key person is missing |
| **Slow onboarding.** A new hire takes weeks to understand "who does what and with which system" | Delayed time to productivity, burden on senior staff |
| **Invisible bottlenecks.** Nobody knows where the process really gets stuck | Inflated lead times, chronic delays accepted as normal |
| **To-be / as-is misalignment.** The real process diverges from the documented one | Compliance risk, painful audits, decisions based on wrong maps |
| **Opaque automations.** No map of dependencies between automations and steps | Unexpected ripple effects when a system is changed |
| **Onboarding a change.** "If I introduce this new step / change approver, what do I impact?" left unanswered | Risky changes, resistance to improvement |

### 1.3 Our answer: the process–role–system graph

LocalMind models processes as a **weighted, AI-queryable graph** that unifies the five views into a single navigable network. Not a new BPMN diagram to maintain by hand, but a **lightweight digital twin of the working organization**:

- Each **Process** is composed of **Steps/Activities** linked by sequence, condition and parallelism relationships (`PRECEDE`, `INNESCA`, `CONDIZIONATO_DA`).
- Each step is connected to the **Role** that performs it, approves it, or is informed about it — with **RACI** semantics (Responsible, Accountable, Consulted, Informed) on the edges.
- Each step is connected to the **System/Application** in which it is carried out (`ESEGUITO_IN`), to the **Data/Documents** it produces or consumes (`PRODUCE`, `RICHIEDE`), to the **Rules/Policies** that constrain it (`GOVERNATO_DA`), and to the **Automations** that execute it in whole or in part (`AUTOMATIZZATO_DA`).
- **People** are connected to roles (`RICOPRE`), so the graph can answer not only "which role approves" but "*who*, concretely, today".
- **Edge weights** derive from factual and experiential factors: real execution frequency (from process mining), average throughput time, criticality, blocking frequency, automation level, source reliability.

On this graph the local AI (GraphRAG) **navigates, explains, simulates and surfaces non-obvious connections**: it combines the graph structure (paths, dependencies, roles) with semantics (descriptions of SOPs, mails, tickets embedded on Qdrant), and always cites the nodes/paths used. This is exactly the approach described by recent research on **GraphRAG applied to process mining** (dual indexing: graph retrieval + semantic vector retrieval, *workflow-aware*).

### 1.4 The four sources of truth, reconciled

The distinctive value is **reconciling the to-be with the as-is**. LocalMind does not merely digitize the diagram: it compares the designed process with the one actually executed (derived from system logs) and surfaces the gap — the *conformance*. The graph thus becomes the only place where the following coexist:

- the **norm** (to-be: SOP, BPMN, policy) → what should happen;
- the **fact** (as-is: event log) → what actually happens;
- the **experience** (tribal: people's contributions) → why it happens and how exceptions are handled;
- the **automation** (script/RPA) → what already runs without human intervention.

### 1.5 Value by user type

| User | Concrete value |
|--------|-----------------|
| Process owner / operations manager | Sees the real process, not the imagined one; identifies bottlenecks and gaps from the norm; simulates changes before making them |
| New hire | Asks in natural language "how do I do X, who do I need to involve, which system do I use" and gets a traceable answer, not an 80-page PDF |
| Auditor / compliance / quality | Verifies separation of duties, audit trail, as-is/to-be deviation; prepares ISO/GDPR audits with evidence from the graph |
| Manager / continuous improvement | Quantifies bottlenecks, handoff costs, lead time; prioritizes automations where they matter most |
| Knowledge manager / HR | Captures and preserves tribal knowledge before people leave; measures the bus factor |
| IT team / enterprise architect | Maps which systems support which processes; assesses the impact of decommissioning/migrating an application |
| Self-hoster / DPO | Everything on-prem: highly sensitive process data stays in-house, local AI by default |

### 1.6 Why LocalMind and not a traditional BPM suite / process mining tool

- **Local-first and absolute privacy.** Business processes and execution logs are among the most sensitive data in existence (they reveal organization, costs, people, weaknesses). Cloud process mining suites (Celonis, etc.) require exporting this data. LocalMind keeps it **entirely on-prem**, with local Ollama AI by default. No data leaves the instance without explicit consent.
- **One unified graph, not four silos.** BPM suites model the to-be, process mining tools the as-is, wikis the tribal knowledge: nobody unifies them. LocalMind is the only layer where they coexist and are queried together.
- **Native conversational AI on the process.** Not dashboards to learn, but natural-language questions with explained and cited answers.
- **Universality and economy of the engine.** The same engine that serves tourism, real estate and enterprise serves processes: the node/relationship types change, not the infrastructure. Development and maintenance cost slashed.
- **Open source and extensible.** Connectors to ERP/CRM/ticketing as publishable PF4J plugins on the marketplace; no lock-in.

### 1.7 What it is NOT (value boundaries)

It is not an **executable workflow engine** that orchestrates and *runs* processes in place of the corporate systems (that is, in part, the existing `automation` domain, with which it integrates). It is not a **BPM modeling suite** that replaces BPMN drawing tools. It is not a **certified enterprise process mining platform** for legal audits. It is a **knowledge and intelligence layer** *on top of* designed, executed and lived processes: it maps them, connects them, explains them and surfaces problems and connections — while staying local-first.

---

## 2. Personas & target users

| Persona | Profile | Main goal | Needs from the graph |
|---------|---------|----------------------|-------------------|
| **Laura, 44 — Operations Process Owner** | Responsible for the order-to-cash processes of a manufacturing SME | Understand where the process loses time and make it leaner | Real bottlenecks, lead time per step, as-is/to-be gap, "what-if" simulation |
| **Paolo, 29 — new hire** | Just joined the purchasing department | Become operational quickly without bothering colleagues | "How do I place an order, who approves, in which system, which documents are needed" |
| **Anna, 51 — Internal Auditor / Quality** | Manages ISO 9001 certification and GDPR compliance | Demonstrate that processes are followed and duties separated | Audit trail, separation of duties (SoD), conformity evidence, deviations |
| **Marco, 38 — Continuous Improvement / Lean** | Black belt leading efficiency projects | Prioritize where to intervene and measure impact | Quantified bottlenecks, handoff costs, automation candidates |
| **Giulia, 47 — HR / Knowledge Manager** | Concerned about senior staff turnover | Capture knowledge before people leave | Bus factor per process/step, mapped tribal knowledge, uncovered roles |
| **Davide, 41 — Enterprise Architect / IT** | Plans the migration of a legacy ERP | Know which processes depend on which system | Process↔system map, impact of decommissioning an application |
| **CFO / Risk Manager** | Project sponsor | Reduce operational risk and dependence on individuals | Overview of risks: bottlenecks, bus factor, SoD violations |
| **Self-hoster / DPO** | Technician who installs LocalMind on-prem | Private and controlled process knowledge pipeline | Connectors, graph API, full control and data residency |

**Anti-persona:** the micro-business with two people and trivial processes, where formal mapping does not repay the effort. LocalMind's value grows with **organizational complexity**: more roles, more systems, more handoffs, more turnover, more compliance.

---

## 3. Input requirements

This section is deliberately detailed: it defines *everything that enters* the process graph, *from which sources*, *in what form* and *how it is validated at the boundary*. Inputs fall into five families: structural inputs (the process map), factual inputs (the actual execution), experiential inputs (the tribal knowledge), organizational inputs (roles/people/systems) and configuration inputs (how to weight and govern the graph). For each, the LocalMind principles apply: fail-fast validation at the boundary, schema-based validation, never trust external data, and no sensitive data outside the instance without consent.

### 3.1 Structural inputs — the process definition (to-be)

They describe how the process *should* work. They can come from import, from an internal editor, or from AI extraction from documents.

| Input | Typical form / origin | Boundary validation |
|-------|------------------------|-------------------------|
| **Process definition** | name, domain/area, owner, version, objective | non-empty name, owner resolvable to a role |
| **Steps / activities** | ordered list of activities with description | at least 2 steps; description present |
| **Sequence and flow** | step→step relationships, gateways/conditions, parallel branches | no unintended, unflagged cycle; a single start, at least one end |
| **Events** | start, end, intermediate events, timers, exceptions | consistent typing (start/intermediate/end) |
| **Decisions / gateways** | branching conditions (e.g. "amount > €5,000") | parsable expression or labeled free text |
| **Step input/output** | documents/data required and produced for each step | reference to an existing Data/Document type |
| **SLA / expected times** | target duration per step or per process | numeric ≥ 0, time unit |
| **BPMN/XML diagram** | imported `.bpmn` / `.xml` file | well-formed XML, BPMN 2.0 compliant (schema validation) |

**Key rule:** every step, before entering the graph, must have at least *one actor* (role) and *one context* (system or "manual"). An "orphan" step (without who does it or where) enters as a **node to be enriched**, highlighted as a knowledge gap.

### 3.2 Factual inputs — the actual execution (as-is, event log)

This is the raw material of **process mining**: execution traces extracted from systems. Without these inputs the graph remains a nice theoretical map; with them it becomes a verified digital twin.

A minimal *event log* requires three columns (the "minimal MXML/XES"):

| Event field | Meaning | Mandatory |
|--------------|-------------|--------------|
| **Case ID** | identifier of the process instance (e.g. order no., ticket, case) | yes — without this, traces cannot be reconstructed |
| **Activity** | name of the executed activity/step | yes |
| **Timestamp** | when the event occurred (start and/or end) | yes — it is what provides order and timing |
| **Resource / actor** | who (person/role/system) performed the event | no (but very high value for roles and SoD) |
| **Case attributes** | amount, customer, priority, outcome… | no (enable segmentation and conditions) |

| Event log source | Examples | Notes |
|-----------------------|--------|------|
| ERP / management system | orders, invoices, warehouse movements | often the richest source |
| CRM | leads, opportunities, sales tickets | for sales processes |
| Ticketing / ITSM | Jira, ServiceNow, GLPI, Zammad | for IT/support processes |
| Workflow engine / BPM | log of the existing engine | "native" as-is |
| Mail and calendar | threads, invitations, approvals via mail | reuse of `email`/`calendar` modules |
| Automations / RPA | logs of scripts and executions | for the automation view |
| Generic application logs | exported CSV/DB | normalized by the connector |

**Log validation and quality:** event dedup, timestamp and timezone normalization, mapping of heterogeneous activity names to the process's canonical activities, handling of incomplete traces. A case without a valid Case ID **does not enter** the trace computation (it degrades to an isolated event).

### 3.3 Experiential inputs — tribal knowledge (people's contributions)

This is the differentiator that no pure process mining captures. People enrich the graph with qualitative knowledge, in natural language, which the AI structures.

- **Annotations on steps:** "in practice the senior salesperson always skips this step", "if the customer is a public administration, a stamp duty is needed".
- **Undocumented exceptions and workarounds:** how non-standard cases are handled.
- **Implicit rules:** thresholds, exemptions, informal hierarchies ("above 10k the CFO also wants to see it").
- **Perceived pain points:** "here we always lose a day because we wait for IT".
- **Knowledge of who really does what:** often different from the formal org chart.

These contributions enter as `Annotazione`/`Eccezione`/`Regola` nodes/attributes linked to the steps, **embedded on Qdrant** for semantic search, and weighted for reliability (who contributes, consensus, recency). Maximum privacy sensitivity: they stay within the instance.

### 3.4 Organizational inputs — roles, people, systems (the process context)

The heart of the "step–role–system relationships" focus. They can come from HR/IT import or be built incrementally.

| Input | Form / origin | Notes |
|-------|-----------------|------|
| **Roles / functions** | catalog of corporate roles (e.g. Buyer, Approver L1, CFO) | basis of RACI semantics |
| **People** | personnel records (possibly from HR/AD/LDAP) | linked to roles with `RICOPRE`; personal data → privacy |
| **Org chart** | reporting hierarchy | for escalation and impact |
| **Systems / applications** | IT catalog (ERP, CRM, tools) | `Sistema` nodes; linked to steps with `ESEGUITO_IN` |
| **Data / documents** | document/data types handled | linked with `PRODUCE`/`RICHIEDE`; reuse of `document` domain |
| **Policy / rules / SOP** | quality manuals, regulations, compliance constraints | `Regola` nodes; linked with `GOVERNATO_DA` |
| **Automations** | scripts, RPA, integrations, cron | `Automazione` nodes; reuse of `automation` domain |
| **RACI matrix** | activity↔role↔responsibility assignment | translated into weighted edges `RESPONSABILE`/`APPROVA`/`CONSULTATO`/`INFORMATO` |

### 3.5 Configuration inputs (how to weight and govern the graph)

For customization, self-hosting and governance:

- **Edge weight factors and functions:** how much usage frequency, criticality, time, automation, source reliability count (see §5.3); each configurable.
- **Decay functions for freshness:** an execution datum from a year ago weighs less than one from yesterday; a SOP not updated for three versions loses weight.
- **Refresh cadences:** event log (daily/hourly), SOP documents (on-change), org chart (monthly).
- **Conformance thresholds:** how much as-is/to-be deviation triggers an alert.
- **SoD rules (separation of duties):** pairs of activities that must not be performed by the same person (e.g. "whoever creates the supplier cannot approve the payment").
- **Sensitivity and access levels:** which processes/steps are confidential, mapped onto the auth/multi-tenant model.
- **Domain RACI defaults** overridable per process.

### 3.6 Summary of input flows

| Source | Cadence | Destination in the system |
|----------|---------|--------------------------|
| BPMN import / internal editor | on-change | `Processo`/`Passo` nodes + sequence edges (MySQL) |
| Event log connector (ERP/CRM/ticketing/mail) | daily/hourly | traces → frequencies, times, weighted `PRECEDE` edges |
| SOP / policy documents (upload) | on-change | AI extraction of steps/roles + embedding (Qdrant) |
| Tribal contributions (UI) | continuous | `Annotazione`/`Eccezione`/`Regola` + embedding |
| HR/AD/LDAP import | monthly | `Persona`, `Ruolo`, `RICOPRE` (with privacy) |
| IT catalog / CMDB | on-change | `Sistema`, `ESEGUITO_IN` edges |
| `automation` module | continuous | `Automazione`, `AUTOMATIZZATO_DA` edges |
| Weights/SoD/freshness config | on-demand | weighting and governance parameters |

---

## 4. Activity flow (step-by-step)

The flow describes the end-to-end path, from building the graph to the informed decision/action. It is divided into **Phase A — building and reconciling the graph** (asynchronous, batch + AI) and **Phase B — interaction, querying and governance** (synchronous, AI-driven). It is designed to be *incremental*: the graph already works with just the to-be view, and improves as event logs and tribal knowledge are added.

### 4.1 Phase A — Building and reconciling the graph

**Step A1 — Acquisition of the process definition (to-be).** It starts from one of three paths: (a) import of a **BPMN 2.0/XML** file, automatically transformed into `Processo`/`Passo`/`Evento`/`Gateway` nodes and sequence edges (following the *BPMN2KG* approaches in the literature); (b) **AI extraction from SOP/policy**: the procedural documents (reuse of the `document` Tika/OCR pipeline) are read by the local LLM, which proposes steps, roles, systems and sequence, subject to human validation; (c) **internal editor** in which the user draws the process. In all cases the to-be skeleton is created.

**Step A2 — Attaching the organizational context.** Each step is connected to `Ruolo` (who), `Sistema` (where), `Dato/Documento` (with what) and `Regola/Policy` (under which constraints). If a **RACI matrix** is available, the assignments become typed edges (`RESPONSABILE`, `APPROVA`, `CONSULTATO`, `INFORMATO`). Steps without an actor or without a context are marked as **gaps to be enriched**.

**Step A3 — Semantic ingestion.** Step descriptions, SOP texts, annotations and rules are chunked and embedded on **Qdrant** (reuse of the existing pipeline), enabling semantic search and the GraphRAG hybrid retrieval. People/roles/systems become entities that can be anchored in natural-language queries.

**Step A4 — Ingestion of the actual execution (event log).** The connectors (§6) acquire the logs from the systems. The `batch` module orchestrates extraction, normalization (timestamp, timezone, dedup) and **mapping of heterogeneous activity names** to the process's canonical activities. The **traces per Case ID** are reconstructed.

**Step A5 — Process discovery (as-is).** From the traces, the *actually executed* model is derived: which step→step transitions actually occur and with what **frequency**, which alternative paths exist, which branches are never used. The `PRECEDE` edges are materialized/updated with weight = real frequency, and the average throughput times per step and per transition.

**Step A6 — Conformance checking (to-be vs as-is reconciliation).** The designed process (A1) is compared with the executed one (A5): **deviations** are identified (skipped steps, unplanned activities, violated orders, missing approvals). The deviations become attributes/edges on the graph (`DEVIA_DA`) and events for governance. It is the heart of the value: making the gap between norm and reality visible.

**Step A7 — Computation of bottlenecks and process indices.** Using the timestamps, the graph is enriched with: **waiting and service time per step**, **bottlenecks** (where time accumulates), **blocking/rework frequency**, end-to-end **lead time**, **automation level** per branch. They become attributes of the `Passo`/`Processo` nodes.

**Step A8 — Organizational analysis.** From the log with the *resource* column, the **social handoff network** is derived (who passes work to whom), the **workload per role/person**, and — cross-referencing with the org chart — the **bus factor** (how many steps depend on a single person) and the **SoD violations** (the same person on incompatible activities).

**Step A9 — Experiential enrichment.** The tribal annotations (§3.3) are linked to the steps and weighted for reliability; they explain the *why* of the deviations that emerged in A6 ("step X is skipped because in practice…").

**Step A10 — Suggesting non-obvious connections (GraphRAG building).** The AI proposes non-obvious edges and insights: processes that share the same critical system/role (systemic risk), steps that are candidates for automation (high frequency + manual + well defined), "twin" processes duplicated in different departments, hidden dependencies — consistent with the project requirement to "surface non-obvious connections".

### 4.2 Phase B — Interaction, querying and governance

**Step B1 — Expressing the need.** The user writes in natural language ("who approves a purchase above €5,000 and in which system?", "where does onboarding get stuck?", "what happens if Maria leaves?") or navigates the graph visually.

**Step B2 — Parsing and grounding.** The local AI translates the request into graph components: entities (roles, systems, processes, people), query type (path, neighborhood, conformance, impact, bottleneck), filters and conditions.

**Step B3 — Hybrid execution on the graph.** The engine combines (a) **relational/structural query** on the MySQL graph (paths, dependencies, RACI, proximity, materialized deviations) and (b) **semantic search** on Qdrant (descriptions of SOPs, annotations, related rules). Routing by query type, according to hybrid *workflow-aware* GraphRAG best practices.

**Step B4 — Synthesis and explanation (explainable).** The AI composes the answer **citing the graph nodes/paths** used: "Approval above €5,000 is performed by the role *Approver L2* in the system *ERP-Purchasing* (step 'Order authorization'); above €20,000 the *CFO* also intervenes as *Accountable*. Source: SOP-Purchasing v4 + 1,240 real execution cases." Source citation as per the project requirement.

**Step B5 — Graph exploration.** The user navigates visually: from the process to the steps, from the step to the roles/systems, expands toward dependencies and deviations, filters by node/relationship type, highlights bottlenecks and bus factor with color coding on the weight. Progressive exploratory navigation.

**Step B6 — "What-if" simulation and impact analysis.** The user asks for the effect of a change: removal of a step, change of approver, decommissioning of a system, unavailability of a person. The AI traverses the graph and returns the **impact** (processes/steps/roles touched) and the risks. This is the value for process owners and enterprise architects.

**Step B7 — Governance and alerting.** Conformance deviations, SoD violations, bottlenecks beyond threshold and critical bus factors generate **alerts** (reuse of `automation`/`messaging`). **Audit reports** are produced with traceable evidence (for ISO/GDPR) and process health dashboards.

**Step B8 — Action and onboarding.** The graph feeds usage experiences: **onboarding guide** generated per role ("here's how you do X, who you involve, which system"), **runbooks** for exceptions, **export** of process map/dossier, automation suggestions toward the `automation` module.

**Step B9 — Feedback loop.** Human corrections (confirm/refute a deviation, validate an AI extraction, flag an annotation as useful/useless) **re-weight the graph** and improve extractions and suggestions — feeding the relationship weights, the engine's core requirement.

### 4.3 Synthetic flow diagram

```
[Connectors + import]
   A1 process def. (BPMN/SOP/editor) → A2 context (role/system/data/rule, RACI) → A3 embedding(Qdrant)
                                            ↓
   A4 event log → A5 discovery (as-is, frequencies) → A6 conformance (to-be vs as-is) → A7 bottleneck/indices
                                            ↓
   A8 org. analysis (handoff, bus factor, SoD) → A9 tribal knowledge → A10 non-obvious links (AI)
                                            ↓
                        WEIGHTED PROCESS–ROLE–SYSTEM GRAPH (MySQL + Qdrant)
                                            ↓
[User] B1 NL/navigation → B2 parsing → B3 hybrid query → B4 explained+cited answer
                                            ↓
   B5 exploration ⇄ B6 what-if/impact → B7 governance/alert/audit → B8 onboarding/action → B9 feedback (re-weight)
```

---

## 5. Graph model (node types, relationship types, weighting criteria)

The model reuses the engine's generic node/edge schema (MySQL tables for the structure, Qdrant for the vectors). Below, the specialization for processes. All node and relationship types are **bilingual IT/EN enums** conveyed to the frontend according to the language switch.

### 5.1 Node types

| Node type | Description | Main attributes | Qdrant vector |
|-----------|-------------|----------------------|----------------|
| `Processo` | End-to-end business process | name, domain, owner, version, lead time, automation level | yes (description) |
| `Passo`/`Attività` | Single process activity | description, average duration, average wait, frequency, criticality | yes (description) |
| `Evento` | Start / intermediate / end / timer / error | type, trigger | no |
| `Gateway`/`Decisione` | Branching point | condition, type (XOR/AND/OR) | optional |
| `Ruolo`/`Funzione` | Organizational role | name, level, responsibility | optional |
| `Persona` | Individual who holds roles | name (personal data → privacy), unit | no (private) |
| `Sistema`/`Applicativo` | Tool where the step takes place | name, type, criticality, IT owner | optional |
| `Dato`/`Documento` | Input/output of a step | type, format, sensitivity | yes (if document) |
| `Regola`/`Policy`/`SOP` | Regulatory constraint or procedure | text, source, version, compliance scope | yes |
| `Automazione` | Script/RPA/integration | type, status, coverage | optional |
| `Deviazione` | Detected as-is vs to-be gap | type (skip/extra/order), frequency, severity | optional |
| `Eccezione` | Non-standard case / workaround | description, handling | yes |
| `Annotazione` | Tribal contribution on a step | text, author, reliability | yes |
| `IstanzaProcesso`/`Caso` | Single execution (case) | Case ID, outcome, duration, attributes | optional |
| `KPI`/`Metrica` | Process indicator | name, value, target, trend | no |

### 5.2 Relationship types (edges)

| Relationship | From → To | Weighted? | Meaning |
|-----------|--------|---------|-------------|
| `PRECEDE` | Step → Step | **yes** | sequence; weight = real frequency of the transition (from log) |
| `INNESCA` | Event/Step → Step | **yes** | start trigger |
| `CONDIZIONATO_DA` | Step → Gateway/Rule | no | dependency on a condition/decision |
| `RESPONSABILE` (R) | Role → Step | **yes** | performs the step (RACI: Responsible) |
| `APPROVA` (A) | Role → Step | **yes** | is accountable / authorizes (RACI: Accountable) |
| `CONSULTATO` (C) | Role → Step | **yes** | is consulted (RACI: Consulted) |
| `INFORMATO` (I) | Role → Step | no | is informed (RACI: Informed) |
| `RICOPRE` | Person → Role | **yes** | who holds that role today; weight = degree/coverage |
| `ESEGUITO_IN` | Step → System | **yes** | system in which the step takes place |
| `PRODUCE` / `RICHIEDE` | Step → Data/Document | **yes** | informational output / input |
| `GOVERNATO_DA` | Step/Process → Rule/Policy | **yes** | applicable regulatory constraint |
| `AUTOMATIZZATO_DA` | Step → Automation | **yes** | automation coverage of the step |
| `DEVIA_DA` | ProcessInstance/Step → Step(to-be) | **yes** | conformance deviation; weight = frequency/severity |
| `PASSA_A` (handoff) | Role/Person → Role/Person | **yes** | work handover (social network, from log) |
| `DIPENDE_DA` | Process → Process/System | **yes** | inter-process or system dependency |
| `SIMILE_A` | Process/Step → Process/Step | **yes** | semantic/structural similarity (duplications) |
| `ANNOTA` | Annotation/Exception → Step | **yes** | weighted experiential knowledge |
| `MISURA` | KPI → Process/Step | no | linked indicator |

### 5.3 Edge weighting criteria

The weight is the heart of the engine: it transforms a static map into a network that reflects *how the company really works*. For processes, the main criteria:

- **Real execution frequency (process mining).** For `PRECEDE`/`INNESCA`/`PASSA_A`: weight = how many times the transition/handoff actually occurs in the traces. It distinguishes the main path (high weight) from the exceptional branches (low weight). It is the signal that overturns the theoretical diagram.
- **Time and criticality.** For `PRECEDE`/`ESEGUITO_IN`: edges/steps where time accumulates (high wait/service) are weighted as **bottlenecks**; business criticality amplifies the weight.
- **Strength of responsibility (RACI).** `APPROVA` (Accountable) weighs more than `RESPONSABILE`, which weighs more than `CONSULTATO`/`INFORMATO`; the weight reflects the degree of involvement and decision-making authority.
- **Role coverage / bus factor.** For `RICOPRE`: if a single individual holds a critical role, the edge signals **high risk** (low bus factor); more people lower the risk.
- **Automation level.** For `AUTOMATIZZATO_DA`: weight = share of the step covered by automation (useful for prioritizing high-frequency manual steps).
- **Severity and frequency of deviation.** For `DEVIA_DA`: weight = how often and how severely the execution departs from the norm (signal for audit and improvement).
- **Experiential reliability/consensus.** For `ANNOTA`: weight = contributor reputation, consensus among multiple annotations, recency (emergent ranking of tribal knowledge).
- **Strength of dependency.** For `DIPENDE_DA`/`ESEGUITO_IN`: how much a process is bound to a system/another process (impact in case of failure/decommissioning).
- **Similarity.** For `SIMILE_A`: combination of cosine distance (Qdrant) on descriptions and structural match (same roles/systems/sequence) — to detect processes duplicated across departments.
- **Freshness.** All weights decay with the obsolescence of the source: old logs weigh less than recent ones; a SOP not updated for several versions loses weight in favor of factual evidence.

The weights are **materialized in batch** in MySQL for the costly and factual components (frequencies, times, bottlenecks, bus factor) and **recomputed at runtime** for the components that depend on the query context (relevance for the asking role, what-if). Materialization is the strategy that allows staying on MySQL+Qdrant without a dedicated graph datastore.

---

## 6. Data sources & connectors (ingestion)

All connectors are implemented as **PF4J extension points** (a new `DataSourceConnectorExtension`, alongside the existing ones) and orchestrated by the `batch` module. Each connector is installable/uninstallable from the marketplace and configurable for self-hosting. The guiding principle is **local-first**: the connectors talk to systems *inside* the corporate perimeter; no process data leaves the instance.

| Source | Type | Cadence | Notes | Output in the graph |
|-------|------|---------|------|------------------|
| **BPMN 2.0 / XML file** | import | on-change | OMG standard | `Processo`/`Passo`/`Gateway`/`Evento` + sequence |
| **SOP / policy documents / quality manuals** | upload (PDF/DOCX) | on-change | reuse of `document` pipeline (Tika/OCR) | AI extraction of steps/roles + embedding |
| **ERP / management system** (event log) | DB/API/CSV export | daily/hourly | richest as-is source | traces → weighted `PRECEDE`, times |
| **CRM** (event log) | API/export | daily | sales processes | traces, case attributes |
| **Ticketing / ITSM** (Jira, ServiceNow, GLPI, Zammad) | API/export | hourly | IT/support processes | traces, handoff |
| **Workflow / BPM engine** | native log | continuous | "native" as-is | high-fidelity traces |
| **Mail & calendar** | IMAP / API | continuous | reuse of `email`/`calendar` modules | approvals/handoffs via mail |
| **Automations / RPA / cron** | log | continuous | reuse of `automation` domain | `Automazione` + `AUTOMATIZZATO_DA` |
| **HR / AD / LDAP** | directory/API | monthly | personal data → privacy/consent | `Persona`, `Ruolo`, `RICOPRE`, org chart |
| **CMDB / IT catalog** | API/CSV | on-change | application map | `Sistema` + `ESEGUITO_IN`/`DIPENDE_DA` |
| **RACI matrix** (sheet/CSV) | import | on-change | often already existing in quality | weighted RACI edges |
| **Tribal contributions** | internal UI | continuous | experiential knowledge | `Annotazione`/`Eccezione`/`Regola` |

**Responsibility notes (important):**
- **Privacy by design.** Event logs and HR records are highly sensitive data (they reveal people, performance, organization). They stay on-prem; AI processing is local Ollama by default; any cloud use only with explicit consent, configurable per process. Optional pseudonymization of the *resource* column for aggregate analysis.
- **Normalization at the boundary.** Each connector validates and normalizes before writing to the graph: mapping of heterogeneous activity names → canonical activities, timestamp/timezone normalization, event dedup, person/role dedup, Case ID consistency.
- **Extensibility.** New sources (a vertical ERP, an industry-specific management system) are added as PF4J plugins without touching the core and are published on the marketplace.

---

## 7. Features to create, develop and maintain (MVP → evolution)

### 7.1 MVP (first release of the scope)

| # | Feature | Type | LocalMind modules involved |
|---|--------------|------|----------------------------|
| 1 | Process graph schema (nodes/relationships §5) as a domain module | CREATE | `knowledge`/graph core, Flyway |
| 2 | New `process` domain in `localmind-domain` (model/port/service, zero Spring) | CREATE | `localmind-domain`, `DomainConfig` |
| 3 | BPMN 2.0 import → nodes/edges (BPMN2KG-like) | CREATE | infrastructure adapter, `batch` |
| 4 | AI extraction of steps/roles/systems from SOP/policy | CREATE/REUSE | `document` (Tika/OCR), `llm` (Ollama), Qdrant |
| 5 | Process, steps, roles, systems, RACI editor/CRUD | CREATE | `process` domain, frontend |
| 6 | Generic event log connector (CSV/DB) + normalization | CREATE | `batch`, PF4J plugin |
| 7 | Process discovery (as-is): `PRECEDE` edge frequencies, times | CREATE | `process` domain, `batch` |
| 8 | Basic conformance checking (to-be vs as-is deviations) | CREATE | `process` domain |
| 9 | Bottleneck and lead time analysis per step | CREATE | `process` domain |
| 10 | Bus factor and step–role–system map | CREATE | `process` domain |
| 11 | Hybrid conversational Q&A/search with citations (GraphRAG) | CREATE/REUSE | graph core, Qdrant, `llm` |
| 12 | Angular `process` UI feature: editor, exploration, explained answers | CREATE | lazy frontend feature |
| 13 | Process–role–system graph visualization (basic) | CREATE | frontend |
| 14 | Tribal contributions (annotations on steps) | CREATE | `process` domain, Qdrant |
| 15 | Bilingual IT/EN enums (node/relationship types, RACI, states) | CREATE | backend + frontend i18n |
| 16 | Flyway migrations (one query per file) | CREATE | `localmind-app` |

### 7.2 Evolution (subsequent releases)

| # | Feature | Value |
|---|--------------|--------|
| E1 | Native connectors (ERP/CRM/ticketing/Jira/ServiceNow) | rich as-is coverage, less effort |
| E2 | Advanced conformance + alerts on deviations beyond threshold | proactive governance, audit |
| E3 | Separation of duties (SoD) and automatic compliance controls | risk, ISO/GDPR/SOX |
| E4 | Social handoff network and workload analysis per role/person | organizational optimization |
| E5 | "What-if" simulation and impact analysis (removal of step/system/person) | safe change decisions |
| E6 | Automation suggestion (candidate steps) → handoff to `automation` | efficiency, reduction of manual work |
| E7 | Detection of duplicate processes across departments (`SIMILE_A`) | rationalization, savings |
| E8 | Automatic generation of onboarding guides and runbooks per role | time-to-productivity |
| E9 | Process health dashboards + KPI/trends over time | measured continuous improvement |
| E10 | AI process agent (autonomously orchestrates discovery, audit, report) | reuse of `agent` domain |
| E11 | Versioning and diff of processes over time (to-be evolution) | change traceability |
| E12 | Audit-ready export (traceable evidence for certifications) | compliance |
| E13 | Predictive conformance / bottleneck anticipation | from reactive to predictive |

### 7.3 To maintain (continuous maintenance)

- **Log refresh and weight recomputation:** monitored batch jobs with metrics (Actuator/Prometheus already present); nightly window for heavy recomputations.
- **Activity name mapping:** constant oversight of the log↔canonical activity mapping when systems change labels (it is the most fragile point of as-is ingestion).
- **Taxonomy alignment:** roles, systems and document types evolve; the catalog must be maintained.
- **Tuning of weights, decays and thresholds:** periodic review driven by feedback and KPIs.
- **Tribal knowledge quality:** moderation of contributions, reputation management, anti-obsolescence.
- **Privacy and access:** periodic review of sensitivity levels and access rights to confidential processes.
- **IT/EN translation updates** of enums and UI texts.

---

## 8. AI / GraphRAG use cases

The AI (local Ollama by default; cloud optional with consent) operates **on the graph**, combining relational and semantic navigation, and always cites the nodes/paths used.

1. **Operational Q&A for onboarding.** "How do I place a purchase order, who approves it, and in which system?" → the AI traverses `Processo→Passo→ESEGUITO_IN/APPROVA` and answers with steps, roles, systems and documents, citing SOPs and real cases.

2. **Discovery of real bottlenecks.** "Where does the customer onboarding process really get stuck?" → analysis of times on `PRECEDE` edges, highlighting the bottleneck step with the weight and the cause (linked tribal annotations).

3. **Conformance and audit.** "In which cases was CFO approval skipped in the last 6 months?" → query on `Deviazione`/`DEVIA_DA` with per-instance evidence, ready for the auditor.

4. **Impact analysis / what-if.** "If we decommission the legacy ERP, which processes and steps are affected?" → navigation of `ESEGUITO_IN`/`DIPENDE_DA` and impact list with criticality.

5. **Bus factor and continuity.** "If Maria leaves, which processes are left without an approver?" → from `Persona→RICOPRE→Ruolo→APPROVA→Passo`, continuity risk with a backup suggestion.

6. **Separation of duties (SoD).** "Are there cases where the same person created the supplier and approved the payment?" → check on edges and instances, flagging violations.

7. **Rationalization.** "Which processes are substantially duplicated across branches?" → `SIMILE_A` edges weighted by semantic+structural similarity.

8. **Automation candidates.** "Which steps are worth automating?" → high-frequency, manual and well-defined steps, with benefit estimate; handoff toward the `automation` module.

9. **Explanation of exceptions.** "Why did the process go differently in this case?" → GraphRAG synthesis that cross-references factual deviations and tribal annotations ("implicit rule: PA customer → extra step").

10. **Runbook/guide generation.** "Generate the operational guide for the Buyer role." → the AI compiles procedure, systems, contacts and exceptions from the graph, bilingual IT/EN.

---

## 9. KPIs & success metrics

| Category | KPI | Indicative target |
|-----------|-----|-------------------|
| Data quality | % steps with role + system assigned (no orphans) | > 95% |
| Data quality | % log events mapped to canonical activities | > 97% |
| Coverage | % critical processes with as-is (event log) ingested | increasing |
| Performance | hybrid Q&A latency (p95) | < 2 s on a typical corporate dataset |
| Performance | batch recomputation time for frequencies/bottlenecks | within the nightly window |
| AI effectiveness | % answers with correct node citation | > 90% |
| AI effectiveness | accuracy of AI extraction of steps/roles from SOP (validated) | measured on an evaluation set |
| Business value | reduction of lead time on optimized processes | measurable post-intervention |
| Business value | reduction of new hire onboarding time | measurable |
| Risk | no. of processes with bus factor = 1 identified and mitigated | decreasing |
| Compliance | no. of SoD violations / deviations detected and resolved | tracked |
| Knowledge | no. of tribal annotations and coverage of critical steps | increasing |
| Privacy/local | % processing performed locally | 100% by default |

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---------|---------|-------------|
| **Quality and heterogeneity of event logs** | Unreliable as-is | Versioned normalization layer, activity name mapping, dedup, regression tests on the mapping |
| **Privacy of process/HR data** | Highly sensitive data, legal risk | Local-first, local Ollama AI, no cloud transmission without consent, resource pseudonymization, encryption at rest, access controls |
| **Inaccurate AI extraction from SOP** | Incorrect to-be graph | Always human validation on extractions; explicit confidence; improving feedback loop |
| **Obsolete to-be vs as-is** | Misleading conformance | Priority to factual evidence (freshness weight); the graph *highlights* the gap instead of hiding it |
| **Organizational resistance / fear of control** | Low adoption, falsified data | Framing on improvement and onboarding (not surveillance); aggregate analysis; transparency; pseudonymization |
| **Computational cost of discovery/conformance** | Slow batches | Weight materialization, incremental computation on deltas, nightly window |
| **Limits of complex queries on MySQL** | Performance | Materialization, indexes, hybrid query; reconsider a graph datastore only if necessary (out of scope now) |
| **Incorrect/obsolete tribal knowledge** | Distorted knowledge | Weights for reliability/consensus/recency, moderation, comparison with facts |
| **Connectors to proprietary systems** | Limited coverage | Generic CSV/DB connector as a baseline; native connectors as evolutionary plugins |
| **"Happy path" bias** | Only the ideal path is modeled | Process mining on logs surfaces real paths and exceptions |

---

## 11. Maintenance & evolution

- **Governed refresh cycles:** batch scheduler with alarms on failures; Prometheus/Grafana metrics already in the stack; health dashboard of connectors and discovery/conformance jobs.
- **Graph schema versioning:** every evolution of nodes/relationships goes through a Flyway migration (one query per file) and an update of the bilingual IT/EN enums.
- **Process versioning:** the to-be evolves over time; keep versions and diffs to track the change and correlate it with effects on the as-is.
- **Extensibility via plugins:** new connectors (vertical ERP/CRM) as PF4J plugins without touching the core; publishable on the marketplace.
- **Continuous tuning:** quarterly review of weights, decay functions, conformance thresholds and SoD rules, driven by feedback (`B9`) and KPIs.
- **Tribal knowledge curation:** moderation workflow, reputation management, anti-obsolescence, archiving of outdated annotations.
- **Privacy/access governance:** periodic review of sensitivity levels and access rights, aligned with the auth/multi-tenant model.
- **Bilingual documentation:** every feature documented in IT/EN (documentation/ + documentazione/); every development tracked in the `Sviluppi/` folder with dated naming.
- **Evolutionary roadmap:** MVP (to-be + discovery + Q&A) → conformance/SoD → organizational/what-if analysis → automation/rationalization → process agent and predictivity.

---

## 12. Integration with existing LocalMind modules

| Existing module | Role in the Processes & workflows scope |
|------------------|----------------------------------------|
| **`knowledge` / graph core** | Foundation of the engine: generic node/edge schema specialized for processes. Starting point for the weighted graph. |
| **`document`** | Text extraction (Tika) and OCR pipeline to ingest SOPs, quality manuals, policies and procedures; basis for AI extraction of steps. |
| **Qdrant (`vectorstore`)** | Embedding of step descriptions, SOPs, annotations and rules for semantic search and GraphRAG hybrid retrieval. |
| **`llm` + Ollama** | GraphRAG: NL parsing, extraction of steps/roles from documents, scoring, explanations, runbook generation; optional multi-provider fallback chain. |
| **`batch`** | Orchestration of event log ingestion, process discovery, conformance and weight recomputation jobs (frequencies, times, bottlenecks). |
| **`automation`** | Dual bond: source (automation logs → "automated" view) and destination (step automation suggestions). |
| **`messaging`** | Notifications and alerts on deviations, SoD violations, bottlenecks and critical bus factors. |
| **`calendar` + `email`** | Sources of "soft" event log (approvals, handoffs, meetings) for processes that live in mail/calendar. |
| **`marketplace` + PF4J plugins** | Distribution of the process module and connectors (ERP/CRM/ticketing) as installable extensions. |
| **`auth`** | Protection of sensitive process data, access controls for confidential processes, local-first multi-tenant. |
| **`agent`** | AI process agent that autonomously orchestrates discovery, audit, report and suggestions (multi-agent: research, verification, synthesis, governance). |
| **`common` (event/analytics)** | Domain events (process ingested, deviation detected) and usage analytics. |
| **Angular frontend (`features/`)** | New lazy `process` feature: process/RACI editor, graph exploration, explained Q&A, what-if, dashboards; Signal store; `TranslatePipe` IT/EN; `language-switcher`. |
| **MySQL + Flyway** | Graph structure (nodes/edges/materialized weights, traces, deviations) and versioned migrations (one query per file). |

**New domain to introduce:** `process` in `localmind-domain` (model / port-in / port-out / service, zero Spring), wired in `DomainConfig.java`, with `/api/v1/process/*` controller, persistence adapter, event log connectors and BPMN import in infrastructure, discovery/conformance jobs in `batch`, and a dedicated Angular feature — following exactly the "Where to Add New Code" pattern of the project structure. The coexistence/synergy with the existing `automation` domain should be assessed: `process` *describes and analyzes* (knowledge), `automation` *executes* (orchestration); the two feed each other.

---

### Sources consulted

- [Automated Process Knowledge Graph Construction from BPMN Models (Springer / DEXA 2022)](https://link.springer.com/chapter/10.1007/978-3-031-12423-5_3)
- [GRAG4PM: Graph Retrieval Augmented Generation Framework Adapted for Process Mining (Applied Sciences, 2026)](https://doi.org/10.3390/app16105152)
- [The Next Frontier of RAG: How Enterprise Knowledge Systems Will Evolve 2026–2030 (NStarX)](https://nstarxinc.com/blog/the-next-frontier-of-rag-how-enterprise-knowledge-systems-will-evolve-2026-2030/)
- [Enterprise Knowledge Graph: Architecture, Use Cases & Implementation Guide 2026 (Improvado)](https://improvado.io/blog/enterprise-knowledge-graph)
- [Procedure Model for Building Knowledge Graphs for Industry Applications (arXiv 2409.13425)](https://arxiv.org/html/2409.13425v1)
- [RACI Matrix: Your Ultimate Guide in 2026 (Project-Management.com)](https://project-management.com/understanding-responsibility-assignment-matrix-raci-matrix/)
- [Process Mining: Discovery, Conformance and Enhancement of Business Processes (W. van der Aalst)](https://www.researchgate.net/publication/275535045_Process_Mining_Discovery_Conformance_and_Enhancement_of_Business_Processes)
- [What is Process Mining? (IBM Think)](https://www.ibm.com/think/topics/process-mining)
- [Business process mining: Conformance checking and bottleneck identification (ResearchGate)](https://www.researchgate.net/publication/351141867_Business_process_mining_from_e-commerce_event_web_logs_Conformance_checking_and_bottleneck_identification)
