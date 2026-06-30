export const content = `# Compliance & audit

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This document describes the **enterprise "Compliance & audit" vertical** built on top of LocalMind's universal Knowledge Graph engine. The goal is not to build yet another monolithic, cloud-only GRC platform, but to instantiate — through node types, relationship types and installable modules — a **weighted graph regulation → control → evidence → asset → risk**, navigable by AI (GraphRAG), that makes compliance *demonstrable* and not merely *declared*. Everything remains local-first, self-hostable, with Ollama AI by default, reusing MySQL 8.0 (graph structure) and Qdrant (semantics of requirements and evidence), without introducing a dedicated graph database. Data sovereignty here is not an added value: it is a non-negotiable requirement, because audit evidence contains the organization's most sensitive data (access logs, configurations, decisions, contracts, incidents).

---

## 1. What we solve (problem & value)

### 1.1 The problem as perceived by the organization

Enterprise compliance in 2026 lives a structural contradiction: regulatory pressure has exploded (DORA in force since 17 January 2025, NIS2, the EU AI Act with obligations on high-risk systems in force from 2 August 2026, GDPR, ISO 27001:2022, SOC 2, PCI-DSS 4.0, sector-specific sovereign regulations), but the tools organizations use to manage it have remained stuck at checklists, Excel spreadsheets and "point-in-time" audits. The concrete problems the compliance officer faces every day:

- **Regulation-control-evidence fragmentation.** Regulatory requirements live in regulators' PDFs; corporate controls in a spreadsheet or a GRC platform; evidence (cloud console screenshots, configuration exports, signed policies, minutes, tickets) is scattered across SharePoint, email, ticketing and file servers. No tool connects in a *queryable* way "this article of law" → "this control that satisfies it" → "this evidence that proves the control operates". Traceability exists only in the heads of a few people.
- **Duplication across frameworks.** The same evidence (access logging, data encryption, incident management, audit trail) satisfies requirements of NIS2, DORA, AI Act, ISO 27001 and SOC 2 simultaneously, but is collected from scratch for each audit. Industry research confirms that up to 60-80% of controls overlap across frameworks: without a navigable *crosswalk* (equivalence map), the organization pays multiple times for the same work.
- **Audit as a "snapshot", not a "movie".** The traditional audit is a point-in-time verification: everything is collected the week before the inspection visit, compliance is demonstrated as of a date, then things revert to the previous state. 2026 demands the opposite — *continuous controls monitoring* and *continuous assurance*: the proof that a control operates must be continuous, dated and fresh, not assembled after the fact.
- **Stale and non-traceable evidence (evidence staleness).** A policy approved two years ago, a screenshot of a configuration that has since changed, a control "green" on paper but in fact no longer operating: evidence expires, and without a system that tracks the *freshness* and *lifecycle* of evidence, the organization discovers the gap only in front of the auditor.
- **Manual and blind gap analysis.** Understanding *where coverage is missing* — requirements without an associated control, controls without evidence, expired evidence — is today a manual task of cross-tabulation, error-prone and never updated in real time.
- **The burden of demonstrability.** 2026 regulations no longer ask only for policies, they ask for *proof that policies work in processes and systems*. The regulators' keyword is **demonstrable**: it requires control telemetry and evidence of change that an auditor can reconstruct without guesswork, with consistent attribution and a tamper-proof audit trail.
- **Lock-in and sovereignty.** The leading SaaS GRC platforms (Drata, Vanta, MetricStream, ServiceNow GRC) are cloud-only: the company's most sensitive compliance data — which includes flaws, accepted risks, incidents — ends up on third-party infrastructure. For many regulated sectors (finance under DORA, public administration under NIS2, healthcare) this is a sovereignty problem that pushes toward *sovereign cloud* or on-premise.
- **The audit trail as a target.** The audit trail itself must be *tamper-evident*: if whoever commits a violation can also alter the log that records it, the evidence is worthless. Few GRC tools treat the cryptographic integrity of the evidence chain as a first-class requirement.

### 1.2 The LocalMind solution

LocalMind treats compliance as a **weighted graph of regulatory and operational knowledge**, not as a checklist. Every regulatory requirement, control, evidence, asset, risk, policy, process and incident is a **typed node**; every connection ("satisfies", "mitigates", "demonstrates", "applies to", "equivalent to", "violates") is a **weighted edge**, where the weight encodes the *strength of coverage*, the *freshness of the evidence*, the *degree of automation* of the control and the *reliability of the source*. On this graph the AI navigates (GraphRAG) to answer complex compliance questions, generate gap analysis, prepare audit packages and explain *why* a requirement is (or is not) covered, citing the nodes and paths used.

The differentiating value is articulated along six axes:

| Value axis | What LocalMind offers | Difference vs traditional SaaS GRC |
|---|---|---|
| **Relational traceability** | Navigable chain regulation → control → evidence → asset → risk, queryable in natural language | Disconnected tables; traceability is reconstructed manually for each audit |
| **Multi-framework crosswalk** | Evidence collected once satisfies N requirements via "equivalent to" edges; automatic coverage analysis | Static mapping, evidence duplicated for each framework |
| **Continuous assurance** | Evidence freshness as a first-class attribute, continuous re-checking, alerts on expired evidence | Point-in-time audit, evidence assembled before the visit |
| **Demonstrability & integrity** | Tamper-evident audit trail (hash-chain), consistent attribution, AI path citation | Alterable logs, attribution not guaranteed |
| **Data sovereignty** | Local-first / self-hostable on-premise; the most sensitive evidence does not leave the perimeter | Cloud-only, compliance data on third-party infrastructure |
| **AI explainability** | GraphRAG answers with citation of regulations/controls/evidence and edge weights | Green/red status dashboards without an inspectable proof chain |

### 1.3 Who benefits and why it matters

- **The Compliance Officer / CISO** gets a single, always-up-to-date view of the coverage status for each framework, with automatic gap analysis and the ability to answer a regulator's question in minutes rather than weeks.
- **The auditor (internal and external)** receives a *collaboration workspace* where every requirement is already linked to its dated and verifiable evidence, with a tamper-proof audit trail: the audit stops being a document hunt.
- **The process owner / asset owner** knows exactly which controls fall within their perimeter and which evidence they must keep fresh, with automatic expiry notifications.
- **The board / management** obtains demonstrable accountability (explicitly required by NIS2 and DORA at the administrative-body level) with residual-risk and coverage dashboards that are inspectable and defensible.
- **The regulated organization** (finance, public administration, healthcare, energy, critical manufacturing) gets an open-source, self-hostable tool that does not expose its risks to third parties, while drastically reducing the recurring cost of SaaS GRC platforms.

### 1.4 Alignment with the LocalMind vision

This vertical is one of the sharpest **enterprise** instances of the universal graph engine described in \`.planning/PROJECT.md\`. It demonstrates that the same engine serving territory discovery (tourism) can serve internal regulatory knowledge by changing only the node/relationship schema and the installed modules. It directly reuses the existing domains \`document\` (policy/procedure ingestion), \`knowledge\` (graph engine), \`email\`/\`calendar\` (evidence and deadlines), \`mcp\` (connectors to technical sources), \`automation\` (continuous evidence collection) and \`agent\` (audit agents). It validates particularly stringently the **local-first**, **enterprise privacy** and **Ollama AI by default** constraints, because here data is regulated by definition and cannot leave the perimeter without explicit consent.

---

## 2. Personas & target users

| Persona | Profile | Goals | Needs from the system |
|---|---|---|---|
| **Compliance Officer / GRC Manager** | Responsible for multi-framework compliance | Ensure and demonstrate coverage, reduce duplication, anticipate gaps | Crosswalk, automatic gap analysis, coverage dashboard, audit package generation |
| **CISO / Security Manager** | Responsible for ICT security and risk | Link controls to risks and assets, demonstrate resilience (DORA/NIS2) | Risk-control-asset graph, control telemetry, SIEM integration |
| **Internal auditor** | Conducts periodic reviews | Plan audits, collect evidence, track findings and remediation | Audit workspace, sampling, findings tracking, immutable audit trail |
| **External auditor / certifier** | Validates compliance for a third-party body | Verify evidence independently, trust the integrity of the chain | Read-only access, dated and verifiable evidence, signed export |
| **Asset / Process Owner** | Operationally responsible for a system or process | Know what to oversee, keep evidence fresh | Per-asset view, evidence renewal tasks, expiry notifications |
| **Data Protection Officer (DPO)** | Responsible for privacy (GDPR) | Map processing activities, legal bases, DPIA, data flows | Processing/purpose nodes, register of processing activities, link to evidence |
| **Risk Manager** | Manages the risk register | Link risks to controls and residual risk | Risk heatmap, mitigate/accept edges, what-if scenarios |
| **Board member / Management** | Top-level accountability | Have defensible proof of compliance status | Executive dashboard, attestations, summary reports |
| **Developer / integrator** | Builds connectors on top of LocalMind | Automate collection of technical evidence | Graph API, PF4J plugins, MCP, SDK, IT/EN documentation |

MVP primary persona: **Compliance Officer** (consumption and gap analysis side) and **Internal auditor** (verification and evidence collection side). Without the former the graph has no owner; without the latter the evidence is not validated. The MVP must close the loop "define the frameworks → map the controls → link the evidence → measure coverage → prepare the audit".

---

## 3. Input requirements

This section defines in detail what the system must receive, validate and ingest in order to function. Inputs are divided into **configuration/knowledge inputs** (what compliance is for this organization) and **operational/evidence inputs** (the proof that controls operate). Consistently with the project rules, every input must be validated at the system boundary (schema validation, fail-fast, no external data trusted a priori).

### 3.1 Regulatory knowledge inputs (frameworks and requirements)

| Input | Format/source | Expected content | Validation |
|---|---|---|---|
| **Framework catalog** | Structured import (JSON/CSV/YAML) or preloaded template | Framework identifier (ISO 27001:2022, SOC 2, NIS2, DORA, GDPR, AI Act, PCI-DSS, NIST CSF/800-53), version, language, issuing authority | Mandatory schema; explicit version; duplicate framework rejected |
| **Regulatory requirements** | PDF/textual regulation + structured import | Article/clause/control ID, requirement text, category, obligations, cross-references | Unique ID per framework; non-empty text; IT/EN language |
| **Crosswalk / equivalence mappings** | Import (e.g. Secure Controls Framework, CSA CCM) or manually defined | Requirement↔requirement pairs across frameworks, with degree of equivalence (exact/partial) | Valid pairs between existing requirements; degree in enum |
| **Risk taxonomies** | Import or predefined | Risk categories, probability/impact scales, acceptance thresholds | Consistent scale; valid numeric thresholds |

Detailed notes:
- Regulatory requirements must be **versioned**: a new version of a framework (e.g. ISO 27001:2013 → 2022) must create new requirement nodes linked to the previous ones via a "supersedes" edge, not overwrite — consistent with the project's immutability rule.
- Ingestion of a regulatory PDF goes through the \`document\` domain (Tika/OCR), then a *segmentation into requirements* step (semantic chunking by article/clause) and an *embedding* step on Qdrant for semantic search of the requirement.
- For the most widespread frameworks LocalMind provides **preloaded templates** (control catalog + base crosswalk) as an installable module, so the organization does not start from scratch.

### 3.2 Organization configuration inputs (controls, assets, processes)

| Input | Format/source | Expected content | Validation |
|---|---|---|---|
| **Internal control catalog** | UI or import | Control ID, description, owner, type (preventive/detective/corrective), frequency, degree of automation, status | Existing owner; frequency in enum; status in enum |
| **Asset inventory** | Import (CMDB) or connectors | Systems, applications, microservices, databases, repositories, infrastructure, suppliers | Unique ID; criticality populated |
| **Process register** | UI or import | Business processes, owners, criticality | Existing owner |
| **People/roles directory** | Import (LDAP/HR) or connectors | People, roles, responsibilities (RACI) | Valid email; role in taxonomy |
| **Policies and procedures** | Document upload | Policy documents with version, approval date, owner, expiry | Version and date mandatory; format supported by Tika |
| **Register of processing activities (GDPR)** | UI or import | Processing activities, purposes, legal bases, data categories, data flow, DPIA | Legal basis in enum; non-empty purpose |
| **Risk register** | UI or import | Risks, probability, impact, owner, strategy (mitigate/accept/transfer) | Values within defined scales |

### 3.3 Operational and evidence inputs (the heart of demonstrability)

These are the most critical and frequent inputs. Every **piece of evidence** is a node with rich metadata and must be treated as immutable, dated data.

| Evidence input | Typical source | Mandatory metadata | Validation |
|---|---|---|---|
| **Screenshot / configuration export** | Cloud console, IdP, firewall | Collection date, source, linked control, collector, hash | Hash computed at ingestion; date ≤ current time |
| **Logs and telemetry** | SIEM, systems, MCP/connectors | Period covered, system, control, integrity | Consistent period; signature/hash |
| **Signed policies/procedures** | Document store | Version, approver, date, expiry | Valid approver; future expiry |
| **Attestations** | Attestation workflow | Attester, date, subject, outcome | Authorized attester |
| **Minutes, tickets, change records** | Ticketing, email, calendar | Reference, date, actors, outcome | Resolvable reference |
| **Test/scan results** | Security tools, audit | Tool, date, target, findings | Structured findings |
| **Incident evidence** | Incident response | Timeline, classification, regulator notification | Timeline consistent with regulatory SLA |

Cross-cutting requirements on evidence:
- **Freshness (evidence freshness).** Each evidence type has a configurable *temporal validity* (e.g. a configuration screenshot expires in 90 days, a policy in 12 months). The system must compute and maintain the freshness status and mark expired evidence.
- **Integrity (tamper-evidence).** At ingestion a cryptographic hash is computed; the evidence enters a **hash chain** (hash-chain / Merkle) that makes any subsequent alteration detectable. This is an explicit requirement of 2026 regulations (tamper-proof audit trail).
- **Attribution.** Each piece of evidence records who/what produced and collected it (human or automated connector), with a reliable timestamp.
- **Chain of custody.** State changes (collected → validated → expired → archived) are append-only on the audit trail, never in-place mutations.

### 3.4 Automation and connector inputs

| Input | Source | Description |
|---|---|---|
| **Connector configuration** | UI/settings | Endpoint, credentials (in secret manager, never hardcoded), collection schedule |
| **Continuous collection rules** | Automation module | Time-/event-based triggers that activate evidence collection |
| **Evidence→control mapping** | UI/rules | Which evidence demonstrates which control, with optional automatic validation logic |

### 3.5 Query inputs (user and AI)

- **Natural-language queries** toward GraphRAG ("Are we covered on art. X of DORA? Which evidence and how fresh is it?").
- **Filters** by framework, control domain, asset, owner, coverage status, freshness.
- **Audit parameters** (perimeter, period, target framework) to generate an audit package.

### 3.6 Validation and data-quality constraints (project rules)

- Mandatory schema validation on every import; **fail-fast** with clear and bilingual IT/EN messages.
- All **enums** (control type, evidence status, legal basis, equivalence degree, risk strategy) translated IT/EN and routed to the frontend based on the language switch.
- No external data trusted a priori: connector results must be normalized and validated before becoming nodes/evidence.
- **Immutability**: requirements, evidence and audit trail entries are not modified; new linked versions are created.
- **Privacy**: inputs may contain personal/regulated data; sending to cloud LLM providers is blocked by default and requires explicit consent per perimeter.
- **Flyway migrations** with a single query per file for every new table/column of the compliance graph model.

---

## 4. Activity flow (step-by-step)

The flow describes the full lifecycle, from initial configuration to audit preparation and continuous monitoring. It is organized in phases; each phase indicates actor, system action, output and the LocalMind domain involved.

### Phase 0 — Compliance program setup

1. **Selection of target frameworks.** The Compliance Officer chooses the applicable frameworks (e.g. ISO 27001:2022 + SOC 2 + DORA). The system instantiates, from preloaded templates or import, the **requirement nodes** for each framework and the "equivalent to" crosswalk edges between overlapping requirements. *(domains: \`knowledge\`, \`document\`)*
2. **Perimeter definition.** Assets, processes, people and GDPR processing activities are imported/declared. They become **nodes** connected by "responsible for", "applies to" edges. *(domains: \`knowledge\`, \`mcp\` for CMDB)*
3. **Control catalog definition.** The internal control catalog is created/imported and "satisfies" edges are drawn between control and requirement (a control can satisfy multiple requirements across multiple frameworks — this is where the crosswalk pays off). *(domain: \`knowledge\`)*
4. **Configuration of freshness policies** for each evidence type and of automatic collection rules. *(domains: \`automation\`, settings)*

### Phase 1 — Regulatory knowledge ingestion

5. Upload of regulatory texts/PDFs → text extraction (Tika/OCR) → **segmentation into requirements** by article/clause → embedding on Qdrant. *(domains: \`document\`, \`knowledge\`)*
6. The AI proposes a **draft mapping** requirement→existing control and requirement→requirement (crosswalk) based on semantic similarity; the Compliance Officer validates or corrects (human-in-the-loop). Accepted edges get a higher weight than merely suggested ones. *(domains: \`knowledge\`, \`agent\`)*

### Phase 2 — Evidence collection

7. **Automatic (continuous) collection.** The connectors (cloud, IdP, SIEM, repository, ticketing via MCP) periodically collect evidence according to the defined rules; each piece of evidence becomes an **evidence node** with metadata, hash and a "demonstrates" edge toward the control. *(domains: \`automation\`, \`mcp\`)*
8. **Manual collection.** The asset owner uploads evidence (screenshots, minutes, attestations) from the UI; the system computes the hash, date and initial status. *(domains: \`document\`, \`knowledge\`)*
9. **Evidence validation.** An automatic rule or a reviewer validates the evidence (status → validated). The status is written append-only on the audit trail with attribution. *(domains: \`knowledge\`, automation)*
10. **Integrity indexing.** The evidence is inserted into the **hash chain** of the audit trail; any subsequent alteration will be detectable. *(infrastructure: persistence + integrity service)*

### Phase 3 — Coverage computation and gap analysis

11. The engine computes, for each requirement, the **coverage status** as a function of: presence of "satisfies" controls, presence of valid "demonstrates" evidence, evidence freshness, degree of control automation. It is the *aggregate weight* of the regulation→control→evidence paths. *(domain: \`knowledge\`)*
12. The **coverage analysis** identifies the gaps: requirements without a control, controls without evidence, expired evidence, failed controls. It produces a list prioritized by risk. *(domains: \`knowledge\`, \`common\`/analytics)*
13. The AI generates a **narrated gap analysis** in natural language with citation of the nodes/paths and remediation suggestions. *(domains: \`agent\`, \`llm\` with Ollama by default)*

### Phase 4 — Remediation

14. For each gap a **remediation task** is created (new control, missing evidence, policy to update) assigned to an owner, with a deadline tracked on \`calendar\`. *(domains: \`automation\`, \`calendar\`)*
15. Upon task closure, the new evidence closes the loop (back to Phase 2) and coverage is recomputed. Every step is recorded on the audit trail.

### Phase 5 — Audit preparation and conduct

16. **Audit planning.** The auditor defines perimeter, period and framework. The system generates the **audit plan** and, optionally, a sampling of the evidence. *(domains: \`knowledge\`, \`agent\`)*
17. **Audit package generation.** For each requirement in the perimeter, the system automatically collects the chain regulation→control→evidence with dates, hashes and attributions, and exports a signed dossier (PDF/JSON) verifiable independently. *(domains: \`knowledge\`, \`document\`, \`common\`/backup)*
18. **Collaboration workspace with the external auditor.** Read-only access to the perimeter, with the ability to record requests, observations and findings. Every interaction is tracked. *(domains: \`auth\`, \`knowledge\`)*
19. **Findings tracking.** The auditor's findings become nodes linked to the affected requirements/controls, with status and associated remediation (re-enters Phase 4).

### Phase 6 — Continuous monitoring (continuous assurance)

20. The system runs **continuous controls monitoring** in the background: it re-checks evidence freshness, reactivates connectors, reassesses coverage and generates **alerts** on expired evidence, failed controls, new requirements (e.g. regulatory update) or imminent deadlines. *(domains: \`automation\`, \`messaging\`, \`calendar\`)*
21. **Live dashboard** of coverage and residual risk by framework, domain, asset and owner, always up to date. *(frontend: compliance feature)*
22. **Regulatory update.** When a new version of a framework is ingested, the AI highlights the new/changed requirements and the impacted controls/evidence, automatically opening the gaps. *(domains: \`knowledge\`, \`agent\`)*

### Synthetic cycle diagram

\`\`\`text
 [Framework/Requirements] --satisfies--> [Controls] --demonstrates--> [Evidence]
        |                                |                          |
   applies to                        mitigates                 freshness+hash
        v                                v                          v
     [Asset/Processes] <---------- [Risks] <------ coverage & gap analysis (AI/GraphRAG)
        |                                                            |
        +--------- audit package / continuous monitoring -----------+
\`\`\`

---

## 5. Graph model (node types, relationship types, weighting criteria)

The model reuses the universal graph engine (MySQL for structure, Qdrant for semantics) by instantiating types specific to the compliance domain. All types are extensible via a modular schema.

### 5.1 Node types

| Node type | Description | Key attributes | Semantic indexing (Qdrant) |
|---|---|---|---|
| \`Framework\` | Standard/regulation | id, version, authority, language | No (metadata) |
| \`Requirement\` | Article/clause/control ID | id, text, category, obligations, version | Yes (requirement text) |
| \`Control\` | Internal control | id, type, frequency, automation, owner, status | Yes (description) |
| \`Evidence\` | Proof that a control operates | type, date, source, hash, status, validity | Yes (textual content, if present) |
| \`Asset\` | System/app/service/data | type, criticality, owner | Yes (description) |
| \`Process\` | Business process | criticality, owner | Yes |
| \`Risk\` | Risk register entry | probability, impact, residual, strategy | Yes |
| \`Policy\` | Policy/procedure document | version, approver, expiry | Yes (text) |
| \`Processing\` (GDPR) | Personal data processing | purpose, legal basis, data categories | Yes |
| \`Person/Role\` | Owner/responsible party | role, RACI | No |
| \`Supplier\` | Third party (DORA/NIS2 supply chain) | criticality, contract | Yes |
| \`Audit\` | Planned/conducted review | perimeter, period, outcome | No |
| \`Finding\` | Audit finding | severity, status, impacted requirement | Yes |
| \`Incident\` | Security event/breach | timeline, classification, notification | Yes |
| \`RemediationTask\` | Corrective action | owner, deadline, status | Yes |
| \`AuditTrailEntry\` | Immutable event-log entry | actor, action, timestamp, prev/curr hash | No (append-only, hash-chain) |

### 5.2 Relationship types (edges)

| Edge (direction) | Meaning | Example |
|---|---|---|
| \`SATISFIES\` (Control → Requirement) | The control covers the requirement | Control "MFA on access" satisfies A.5.17 ISO |
| \`DEMONSTRATES\` (Evidence → Control) | The evidence proves operation | MFA log demonstrates "MFA on access" |
| \`EQUIVALENT_TO\` (Requirement ↔ Requirement) | Crosswalk across frameworks | A.8.2 ISO equivalent to CC6.1 SOC 2 |
| \`SUPERSEDES\` (Requirement → Requirement) | Subsequent version | ISO 2022 supersedes ISO 2013 |
| \`APPLIES_TO\` (Requirement → Asset/Process) | Scope of application | DORA art. X applies to "core banking" |
| \`MITIGATES\` (Control → Risk) | The control reduces the risk | "Encrypted backup" mitigates "data loss" |
| \`THREATENS\` (Risk → Asset) | The risk weighs on the asset | "Ransomware" threatens "file server" |
| \`RESPONSIBLE_FOR\` (Person → Control/Asset) | Ownership/RACI | Control owner |
| \`GOVERNS\` (Policy → Control/Process) | The policy regulates | Access policy governs IAM controls |
| \`VIOLATES\` (Incident → Requirement/Control) | The incident represents a violation | Data breach violates GDPR art. 32 |
| \`DETECTS\` (Finding → Requirement/Control) | The finding concerns | Finding on "insufficient logging" |
| \`CORRECTS\` (RemediationTask → Finding/Gap) | The action closes the gap | Task corrects finding |
| \`DEPENDS_ON\` (Asset → Asset/Supplier) | Technical/supply-chain dependency | Microservice depends on cloud provider |
| \`RECORDS\` (AuditTrailEntry → any node) | Event tracking | Entry records "evidence validation" |

### 5.3 Edge weighting criteria

The weight (0–1, normalized) is the heart of "demonstrable compliance": it encodes *how* strong and reliable a relationship is, and feeds the coverage computation and the GraphRAG ranking.

| Edge | Factors that determine the weight | Logic |
|---|---|---|
| \`SATISFIES\` | Degree of coverage (total/partial), human validation vs AI-suggested only, control specificity | Validated total coverage → high weight; unconfirmed AI suggestion → low weight |
| \`DEMONSTRATES\` | **Freshness** of the evidence (temporal decay), status (validated/expired), degree of collection automation, verified integrity | Fresh, validated, automatically collected and intact evidence → maximum weight; expired evidence → weight decaying toward 0 |
| \`EQUIVALENT_TO\` | Degree of equivalence (exact/partial), crosswalk source (recognized standard vs manual) | Exact equivalence from SCF/CCM → high weight |
| \`MITIGATES\` | Estimated effectiveness of the control, evidence of operation, risk coverage | Effective and demonstrated control → reduces residual risk more |
| \`APPLIES_TO\` | Relevance/criticality of the asset, explicit vs inferred | Critical asset explicitly in scope → high weight |
| \`DEPENDS_ON\` | Criticality of the dependency, frequency of use | Critical dependency → high weight (relevant for supply-chain risk) |

Weight computation principles (consistent with the universal engine of \`.planning/PROJECT.md\`):
- **Temporal decay** of freshness: the weight of \`DEMONSTRATES\` decreases over time until the configured expiry, then marks the evidence as stale.
- **Boost from human validation**: edges confirmed by a reviewer weigh more than those merely suggested by the AI.
- **Boost from automation**: evidence collected automatically and with verified integrity weighs more than manual evidence (less subject to error/tampering).
- **Path aggregation**: the coverage of a requirement is a function of the *best weighted path* regulation→control→evidence, not the mere existence of an edge.

---

## 6. Data sources & connectors (ingestion)

Ingestion massively reuses the existing domains. All connectors respect the local-first principle: they run within the perimeter, credentials stay in a secret manager, no data leaves toward the cloud without consent.

| Source | Data type → nodes/edges | LocalMind mechanism |
|---|---|---|
| **Regulatory texts (PDF/HTML)** | Frameworks, Requirements | \`document\` domain (Tika/OCR) + segmentation + Qdrant embedding |
| **Preloaded framework templates** | Requirements + \`EQUIVALENT_TO\` crosswalk | Installable module (PF4J plugin / marketplace) |
| **Public crosswalks (SCF, CSA CCM)** | \`EQUIVALENT_TO\` edges | Structured import |
| **CMDB / asset inventory** | Assets, Dependencies | MCP connector / import |
| **IdP (LDAP/SSO)** | People, Roles, access evidence | MCP connector + automation |
| **Cloud (config/posture)** | Configuration evidence | MCP connector + automation (continuous collection) |
| **SIEM / logs** | Telemetry evidence, Incidents | MCP connector + automation, export to external SIEM |
| **Git / CI-CD repositories** | Assets (microservices), process evidence (reviews, scans) | MCP connector |
| **Ticketing / ITSM** | Change/incident evidence, RemediationTask | MCP connector / messaging |
| **Email / Calendar** | Evidence (minutes, approvals), deadlines | \`email\`, \`calendar\` domains |
| **Internal document store** | Policies, Procedures, Attestations | \`document\` domain |
| **Register of processing activities / DPIA** | GDPR processing activities | UI + import |

Connector guidelines:
- Each connector normalizes its output into **evidence nodes** with standard metadata (date, source, hash, target control).
- Collection **scheduling** is managed by the \`automation\` domain (continuous collection).
- Connectors are **PF4J plugins** where possible, so the ecosystem can extend them without touching the core.
- **Export** to external GRC/SIEM (required by NIS2/DORA) happens in standard and signed formats.

---

## 7. Features to create, develop and maintain (MVP → evolution)

### 7.1 MVP (first useful release)

| # | Feature | What to do | Domains/modules |
|---|---|---|---|
| 1 | **Compliance graph model** | MySQL tables for nodes (Framework, Requirement, Control, Evidence, Asset, Risk) and weighted edges; Flyway migrations (one query per file) | \`knowledge\`, infrastructure, app (Flyway) |
| 2 | **CRUD frameworks/requirements/controls** | port/in API + \`/api/v1/compliance/*\` controller + DTO; IT/EN enums | \`knowledge\`, api |
| 3 | **Regulatory ingestion** | PDF upload → extraction → segmentation into requirements → Qdrant embedding | \`document\`, \`knowledge\` |
| 4 | **\`SATISFIES\`/\`EQUIVALENT_TO\` mapping** | UI to link control↔requirement and crosswalk; human-in-the-loop AI suggestions | \`knowledge\`, \`agent\`, frontend |
| 5 | **Evidence management** | Evidence upload/registration with hash, date, status, validity; \`DEMONSTRATES\` edge | \`knowledge\`, \`document\` |
| 6 | **Evidence freshness** | Status computation (valid/expiring/expired) and weight decay | \`knowledge\`, \`automation\` |
| 7 | **Coverage & gap analysis** | Per-requirement coverage computation + prioritized gap list | \`knowledge\`, \`common\`/analytics |
| 8 | **GraphRAG compliance Q&A** | NL answers with regulation→control→evidence citation, Ollama by default | \`agent\`, \`llm\`, \`knowledge\` |
| 9 | **Coverage dashboard** | View by framework/domain/owner with live status | frontend (compliance feature) |
| 10 | **Tamper-evident audit trail (base)** | Append-only register with hash-chain of state changes | infrastructure, \`knowledge\` |
| 11 | **Preloaded framework templates** | At least ISO 27001:2022 and SOC 2 as an installable module | marketplace/plugin |
| 12 | **i18n IT/EN** | Bilingual enums and UI | frontend, api |

### 7.2 Evolution (subsequent phases)

| # | Feature | Value | Domains/modules |
|---|---|---|---|
| 13 | **Automatic connectors (CCM)** | Continuous evidence collection from cloud/IdP/SIEM via MCP | \`mcp\`, \`automation\`, plugin |
| 14 | **Signed audit package generation** | Verifiable per-requirement dossier with the complete chain | \`document\`, \`common\`/backup |
| 15 | **External auditor workspace** | Tracked read-only access + findings | \`auth\`, \`knowledge\` |
| 16 | **Findings & remediation tracking** | finding→task→evidence loop with deadlines | \`automation\`, \`calendar\` |
| 17 | **Risk register & residual risk** | \`MITIGATES\`/\`THREATENS\` edges, heatmap, what-if | \`knowledge\`, frontend |
| 18 | **GDPR module** | Register of processing activities, legal bases, DPIA, data flow | \`knowledge\`, frontend |
| 19 | **Alerts & continuous monitoring** | Notifications on deadlines, failed controls, new requirements | \`messaging\`, \`automation\`, \`calendar\` |
| 20 | **Assisted regulatory update** | Diff between framework versions, automatic gaps | \`agent\`, \`knowledge\` |
| 21 | **Interactive graph visualization** | Exploration of regulation-control-evidence by relationship and weight | frontend |
| 22 | **Merkle tree / advanced cryptographic signing** | Forensic-level tamper-proof integrity | infrastructure |
| 23 | **Extended crosswalks (30+ frameworks)** | Import SCF/CCM, NIST 800-53, PCI-DSS, NIS2, DORA, AI Act | marketplace/plugin |
| 24 | **Autonomous audit agents** | Agents that collect, validate and propose remediation | \`agent\`, \`automation\` |
| 25 | **Attestations & campaigns** | Periodic attestation workflow with signing | \`automation\`, \`messaging\` |

### 7.3 To maintain (continuous maintenance)

- Updating the **framework templates** and **crosswalks** when regulators publish new versions.
- Periodic verification of the **integrity of the audit trail hash-chain**.
- Updating the **connectors** when the APIs of the technical sources change.
- Maintenance of the **freshness policies** by evidence type.
- Alignment of the **IT/EN translations** of enums and UI for each new concept.

---

## 8. AI / GraphRAG use cases

The AI navigates the weighted graph to answer questions that tabular queries cannot address. Default Ollama (local-first); cloud providers only with explicit consent per perimeter, given that the inputs are regulated.

| Use case | Typical question | How the AI uses the graph |
|---|---|---|
| **Coverage Q&A** | "Are we compliant with art. 9 of DORA? With which evidence and how fresh is it?" | Navigates Requirement→Controls→Evidence, evaluates weights and freshness, cites the path |
| **Narrated gap analysis** | "Where do we have the most serious gaps on ISO 27001?" | Finds requirements with weak/absent coverage, sorts by risk, explains |
| **Smart crosswalk** | "Which SOC 2 evidence can I reuse for NIS2?" | Follows \`EQUIVALENT_TO\` and \`DEMONSTRATES\` edges, proposes reuse |
| **Impact of a change** | "If I decommission this asset, which controls/requirements remain uncovered?" | Explores \`APPLIES_TO\`/\`DEPENDS_ON\`, flags exposures |
| **Audit preparation** | "Generate the package for the ISO audit on the core banking perimeter" | Collects regulation→control→evidence chains within the perimeter |
| **Incident analysis** | "Which requirements does this data breach violate and which notifications are triggered?" | Follows \`VIOLATES\`, maps notification obligations and SLAs |
| **Non-obvious connections** | "Which controls are single points of failure across multiple frameworks?" | Finds control nodes with high centrality in the graph |
| **Regulatory update** | "What changes with the new version of the framework and what must I oversee?" | Diffs the requirements, propagates to the impacted controls/evidence |

In all cases the answer **cites the nodes and paths** used (explainability), an indispensable requirement in an audit context: the AI must not "reassure", it must *prove with the chain of evidence*.

---

## 9. KPIs & success metrics

| Category | KPI | Meaning / target |
|---|---|---|
| **Coverage** | % of requirements with full coverage (control + valid evidence) per framework | Rising trend; target for critical framework ≥ 95% |
| **Coverage** | Number of requirements with no control (uncovered gaps) | Tend toward 0 on active frameworks |
| **Freshness** | % of fresh vs expired evidence | Continuous-assurance indicator; target ≥ 90% fresh |
| **Efficiency** | % of evidence reused across multiple frameworks (crosswalk effect) | Measures the saving from mapping; higher is better |
| **Automation** | % of evidence collected automatically vs manually | CCM maturity; rising trend |
| **Audit** | Average preparation time of an audit package | From weeks to hours/minutes |
| **Audit** | Number of external findings per audit / % closed within SLA | Quality of the program and of remediation |
| **Risk** | Average residual risk after mitigation | Decreasing trend |
| **Integrity** | Hash-chain integrity checks passed | 100%; any anomaly is an incident |
| **Adoption** | Number of active controls/evidence, active users (owners, auditors) | Growth of the graph and of usage |
| **AI** | % of GraphRAG answers with valid path citation | Explainability; target ~100% |
| **Sovereignty** | % of processing executed locally (Ollama) without data egress | Consistency with local-first; target ~100% by default |

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| **"Green" coverage but stale evidence** | False sense of security, failed audit | Freshness as a first-class attribute + weight decay + automatic alerts |
| **Audit trail tampering** | Evidence with no legal value | Append-only hash-chain; evolution toward Merkle/signing; periodic integrity checks |
| **Wrong or excessive mappings (over-mapping)** | Overestimated coverage | Human-in-the-loop validation; lower weight for AI-suggested-only edges |
| **AI hallucinations on regulatory matters** | Wrong compliance decisions | GraphRAG with mandatory citation of nodes/paths; never answers without a chain of evidence |
| **Leakage of regulated data to cloud LLMs** | Privacy/GDPR violation | Ollama by default; blocking of cloud sending unless explicit consent per perimeter |
| **Regulatory drift (obsolete frameworks)** | Compliance toward an old version | Requirement versioning + assisted regulatory update + template maintenance |
| **Graph model on MySQL not scaling on deep queries** | Gap analysis performance | Targeted indexes, coverage denormalization, cache; reassess graph DB only if necessary (project constraint) |
| **Exposed connector credentials** | Source compromise | Secret manager, never hardcoded; least privilege; rotation |
| **Alert overload (alert fatigue)** | Ignored alerts | Risk prioritization, configurable thresholds, grouping |
| **Dependence on a single control owner** | Gap when the person leaves | RACI in the graph, reassignment, automated evidence where possible |

---

## 11. Maintenance & evolution

- **Updating regulatory catalogs**: a recurring process to incorporate new framework versions and new regulations (e.g. post-August-2026 AI Act developments), creating new requirement nodes linked to the previous ones.
- **Curation of crosswalks**: periodic review of equivalences across frameworks, with priority to the most used ones for evidence reuse.
- **Connector health**: monitoring of connector failures (a broken connector = evidence that stops refreshing); dedicated alarm.
- **Integrity over time**: periodic job verifying the hash-chain; in evolution, cryptographic signing and Merkle anchoring.
- **Extensibility via plugins**: new frameworks, connectors and evidence types as PF4J modules publishable on the marketplace, without touching the core.
- **Data quality**: review of freshness policies, evidence deduplication, archiving of obsolete nodes (never deletion that breaks historical traceability).
- **i18n**: every new concept/enum translated IT/EN; bilingual documentation kept up to date in \`documentation/\` and \`documentazione/\`.
- **Development tracking**: every intervention documented in the \`Sviluppi/\` folder with the dated naming required by the project, in plan mode, with checkpoints for complex tasks.
- **Graph DB roadmap**: monitor the performance of coverage/path queries on MySQL; the move to a graph DB remains out of scope but reassessable if metrics demand it.

---

## 12. Integration with existing LocalMind modules

| Existing module/domain | Role in the Compliance & audit vertical |
|---|---|
| **\`knowledge\`** | Heart of the graph model: typed nodes, weighted edges, coverage/path queries. This is where the compliance schema lives. |
| **\`document\`** | Ingestion of regulatory texts, policies, procedures, documentary evidence (Tika/OCR); segmentation and embedding. |
| **Qdrant (vector store)** | Semantic search on requirements, controls and evidence; support for GraphRAG and mapping suggestions. |
| **MySQL 8.0** | Graph structure (nodes/edges), append-only audit trail, coverage status; Flyway migrations (one query per file). |
| **\`agent\`** | Gap analysis agents, mapping suggestion, audit preparation, regulatory update; autonomous audit agents (evolution). |
| **\`llm\` (Ollama default)** | GraphRAG reasoning engine; provider fallback chain; cloud only with consent for regulated data. |
| **\`mcp\`** | Connectors to technical sources (cloud, IdP, SIEM, CMDB, repos, ticketing) for automatic evidence collection. |
| **\`automation\`** | Continuous controls monitoring: collection scheduling, validation rules, coverage recomputation, remediation tasks. |
| **\`messaging\`** | Notifications and alerts (expired evidence, failed controls, deadlines) on the configured channels. |
| **\`calendar\`** | Evidence deadlines, planned audits, remediation deadlines; evidence from approvals/events. |
| **\`email\`** | Evidence from communications (approvals, minutes) and notifications toward owners/auditors. |
| **\`auth\`** | Local-first access control; read-only workspace for external auditors; attribution of actions in the audit trail. |
| **\`common\` (analytics/backup)** | Computation of metrics/coverage, generation and signing of audit packages, verifiable export/backup. |
| **\`marketplace\` / \`plugin\` (PF4J)** | Distribution of framework templates, crosswalks and connectors as installable modules. |
| **\`finetuning\`** | (Evolution) fine-tuning of local models on the organization's regulatory language to improve mapping and Q&A. |
| **Angular 21 Frontend** | \`compliance\` feature: coverage dashboard, management of requirements/controls/evidence, graph visualization, audit workspace; bilingual IT/EN UI, Signal store. |

Constraints respected throughout the integration: **local-first/self-hostable**, **Ollama AI by default** with privacy of regulated data, **reuse of MySQL+Qdrant** without a dedicated graph DB, **PF4J extensibility**, **immutability** (versioned and append-only requirements/evidence/audit trail), **Flyway migrations with a single query**, **bilingual IT/EN enums and documentation**.

---

*Guidance document for the development of the enterprise "Compliance & audit" vertical on LocalMind's universal Knowledge Graph engine. Date: 2026-06-29.*
`;
