# People & Skills

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

## 1. What we solve (problem & value)

### 1.1 The enterprise problem: knowledge lives in people, but it is invisible

In any organization beyond a few dozen employees, an uncomfortable truth emerges: **the wealth of skills exists, but nobody knows where it is**. The knowledge of "who knows how to do what" lives in people's heads, in chat threads, in code reviews, in emails, in meeting minutes, in the commits of a repository — and almost never in a queryable system. The result is a series of recurring and costly symptoms:

- **The question "who knows about…?" has no answer.** When you need someone who knows Kafka, GDPR regulation applied to healthcare data, the legacy management system written in COBOL, or the client "Acme S.p.A.", the search happens by word of mouth: you ask a colleague, who asks another, until — maybe — you reach the right person. This process is slow, depends on the social network of whoever is searching (and therefore penalizes new hires and those who work remotely), and often stops at the "most visible person" rather than the most competent one.

- **CVs and org charts lie or grow stale.** The CV photographs the moment of hiring; the HR profile contains the job title and contractual classification, not the real skills. The skills a person has developed on the field over the last two years — perhaps the most valuable ones — are not recorded anywhere. Self-declared skills inventories, where they exist, suffer from two opposite and well-documented distortions: there are those who declare themselves expert in ten areas where they are merely competent, and there is the database that, a year after being filled in, is no longer updated by anyone.

- **Skills gaps are discovered too late.** The company decides to adopt a new technology, to enter a new market, or to respond to a tender, and only then discovers it does not have — or has in a single person — the necessary skills. There is no anticipated view of *where the holes are* between the skills possessed and those required by the strategy.

- **The "bus factor" is a hidden risk.** How many critical areas depend on a single person? If that colleague falls ill, goes on holiday, or leaves the company, an entire system, process, or client is left uncovered. This risk of knowledge concentration (single point of knowledge) is rarely measured, yet it is one of the main factors of operational fragility.

- **Internal mobility is blocked.** When a position opens up or a project is born, you look outside before looking inside, because internal talents with transferable skills are not known. Qualified people remain "invisible" and end up leaving, while the company recruits externally skills it already possessed.

- **Training is disconnected from real needs.** L&D (Learning & Development) plans are often catalog-based, the same for everyone, disconnected from the actual gaps. There is no knowledge of which skills are worth developing internally, in whom, and with what priority relative to objectives.

- **Onboarding is extremely slow.** A new hire takes months to understand "who is who", whom to ask for what, how the company's knowledge is structured. Today, this map is built only with time and informal relationships.

### 1.2 Our answer: the living skill graph of the organization

LocalMind, in its evolution into a **universal knowledge graph engine**, tackles this problem by building the organization's **skill graph**: a weighted and living network in which the nodes are people, skills, roles, teams, projects, and contexts, and the edges represent significant and quantified relationships — "possesses the skill", "has worked on", "is expert in", "has mentor", "requires", "belongs to the team". Unlike a static taxonomy or an Excel sheet of skills, this graph is **continuously fed and weighted by the evidence** already present in the corporate systems. On this basis, four capabilities are grafted:

1. **Automatic mapping of "who-knows-what".** Skills are not (only) self-declared: they are **inferred** from the signals people already produce — drafted documents, emails and messaging threads, resolved tickets, contributions to repositories, project participation — using local AI to extract skills and contexts from the text. Every inference is traceable to its evidence, so that Mario's "Kafka" skill is not an empty assertion but is anchored to the five documents and twelve tickets that demonstrate it.

2. **Expert localization via GraphRAG.** The AI (Ollama locally by default) answers natural-language questions — "who can help me integrate a Stripe payment?", "who knows the Acme client and speaks German?" — by navigating the skill graph, combining the structural match (person→skill relationships) with the semantic match (search on Qdrant in the content produced by people), and always citing the evidence that justifies the recommendation.

3. **Gap and risk analysis.** By comparing the skills possessed with those required (by roles, projects, strategy), the system surfaces the **skills gaps**, the **single points of knowledge** (bus factor), the skills becoming obsolete and those growing, suggesting actions: hire, train, redistribute, document.

4. **Enabling internal mobility and targeted training.** The graph becomes the basis of an internal talent marketplace: people with suitable or transferable skills emerge for projects and open positions, and training plans hook onto the real gaps of the individual person and of the organization.

### 1.3 Why this scope is strategic

The "People & skills" domain is not just any vertical: it is one of the highest-value enterprise areas because it sits at the **intersection of all the other graphs**. People are the nodes that connect documents, processes, repositories, microservices, clients, decisions: who wrote that document, who maintains that service, who manages that client. Building the skill graph well means giving a "human owner" to every piece of corporate knowledge, multiplying the value of all the other enterprise areas (documents, repositories, processes, APIs). It is, in essence, the **connective tissue** of the enterprise knowledge graph.

### 1.4 Why LocalMind is the right platform

Data about people and skills are among the most sensitive an organization possesses: profiles, implicit evaluations, "who knows little about what", hierarchical relationships. Entrusting them to a cloud HR SaaS is, for many companies (public administration, healthcare, defense, finance, professional firms), simply impractical for regulatory and trust reasons. Here LocalMind has a structural advantage.

| Organization's need | LocalMind feature that satisfies it |
|---|---|
| Data on people/skills never exfiltrated to third-party clouds | **Local-first / self-hosted**: everything runs on-premise; no external transmission without explicit consent |
| GDPR compliance and worker privacy | **Local Ollama AI by default**: skill inference does not leave the corporate perimeter |
| Skill extraction from heterogeneous content | Existing ingestion pipeline (**Tika + Tesseract OCR**), chunking, embedding, already operational |
| Answers grounded on internal evidence | **GraphRAG** on skill graph + **Qdrant** semantic search, with source citation |
| People connect all the other graphs (doc, repo, mail) | Existing domains `document`, `email`, `messaging`, `calendar`, `mcp` already ingest those signals |
| Connectors to company-specific HR/IT systems | **PF4J plugin** system + marketplace for connectors (HRIS, AD/LDAP, Git, Jira) |
| Bilingual interface and taxonomies | **Bilingual IT/EN** platform by design, translated enums |
| No lock-in, no cost per employee | **Pure open source**, no paywall, no per-seat pricing |

The competitive differentiator versus HR tech suites (Gloat, 365Talents, Eightfold, Workday Skills Cloud) is clear-cut: those platforms offer powerful skill graphs but **cloud-only, paid per employee, and with data outside the corporate perimeter**. LocalMind offers the same paradigm — weighted skill graph, AI inference, expert localization, gap analysis — while remaining **on-premise, free, and data-sovereign**, and integrating it natively with the rest of the corporate knowledge instead of treating it as an isolated HR silo.

## 2. Personas & target users

| Persona | Profile | Primary needs | How they use LocalMind |
|---|---|---|---|
| **Laura, HR Manager / People Operations** | Head of people development in a company of ~300 employees | Map real skills, identify gaps, plan training and succession | Explores the skill graph, runs gap analysis by area, manages the skills taxonomy, plans targeted L&D |
| **Marco, Resource/Delivery Manager** | Composes project teams in a consulting firm | Quickly find people with the right and available skills | Uses the "find the expert" tutor, composes teams by required skills, verifies project skill coverage |
| **Sara, Team Lead / Engineering Manager** | Leads a technical team of 8 people | Understand the team's strengths/weaknesses, reduce the bus factor, grow people | Visualizes the team skills matrix, identifies single points of knowledge, plans mentoring and cross-training |
| **Davide, employee / knowledge worker** | Developer, wants to grow and be found | Surface his own skills, find projects/mentors, understand what to learn | Reviews and confirms his own (inferred) skills profile, applies to internal opportunities, receives growth suggestions |
| **Giulia, new hire** | In the company for 2 weeks | Understand "who is who" and whom to turn to | Asks the GraphRAG tutor who the expert on X is, navigates the team/people/skills graph |
| **Antonio, CTO / Management** | Strategic decisions on skills and organization | Overall view of corporate capabilities, concentration risks, skill↔strategy alignment | Consults aggregated and anonymized dashboards, evaluates capacity vs roadmap, decides make-or-buy of skills |
| **DPO / Privacy Officer** | Guardian of compliance on the processing of workers' data | Ensure legal basis, transparency, minimization, data subject rights | Configures inference/visibility policies, audits the evidence, manages consent and opt-out |

The primary and priority user for the MVP is the pair **Laura (HR) + Marco (Resource Manager)**: they carry the most acute need ("who knows what", gaps, team composition) and are the adoption decision-makers. **Davide (the employee)** is, however, an indispensable co-primary user: without his participation — profile confirmation, control over visibility — the system is ethically and legally unsustainable. The other personas drive the evolutions.

## 3. Input requirements

This section defines in detail **what must be able to enter the system** for the People & skills scope to work. The inputs are divided into: personnel records and organizational structure, skills taxonomy, evidence signals from which to infer the skills, declarative and feedback data, requirements (roles/projects), and privacy configuration. Every input must be validated at the system boundary (the "never trust external data" principle) and treated immutably: no inferred or declared skill is mutated in place, but versioned, so as to preserve the history for auditing, for weight calculation, and for the data subject's rights.

> **Cardinal principle (privacy by design).** Unlike the other scopes, here the input concerns **physical persons**. Every signal ingestion must have a legal basis, a visibility level, and the possibility of opt-out. No data of a person is made visible to others before review/consent according to the configured policies (§3.7).

### 3.1 Personnel records and organizational structure

The first input is the "backbone" of the graph: who exists and how they are organized. Typically importable from HRIS, Active Directory/LDAP, or files.

| Data | Field examples | Typical source | Mandatory status |
|---|---|---|---|
| Person | name, internal identifier, email, status (active/terminated), join date | HRIS, AD/LDAP, CSV | Mandatory (at least identifier + name) |
| Role / position | job title, level/grade, professional family | HRIS | Recommended |
| Organizational unit / team | department, team, cost center, location | HRIS, org chart | Recommended |
| Hierarchy | manager, reports | HRIS, AD | Optional |
| Location / geographic area | office, city, time zone, languages spoken | HRIS, AD | Optional but valuable for search |
| Availability | current allocation, residual capacity | project/resource management systems | Evolution |

### 3.2 Skills taxonomy / ontology

To give structure and comparability to skills, a **taxonomy** is needed (preferably an ontology with hierarchy and synonyms). LocalMind must be able to import, build, or hybridize it:

- **Import from open standards**: ESCO (European multilingual classification, ~14,000 skills and ~3,000 occupations, ideal for the IT/EN requirement) and/or O*NET (USA). They provide categories (core, technical, transversal skills), synonyms, and ready-made hierarchical relationships.
- **Custom company taxonomy**: the company defines its own skills (technologies, products, processes, clients, soft skills) with optional mapping to the standard.
- **Emergent ontology**: new skills can arise from AI inference on content and then be normalized/merged with existing ones (synonym management: "JS" = "JavaScript", "GDPR" = "Data Protection Regulation").

Minimum attributes of a skill: name (IT/EN), description, category/domain, hierarchical relationships (more general/more specific), related or prerequisite skills, and — fundamentally — the **scale of mastery levels** (e.g., 1 Novice → 2 Basic → 3 Competent → 4 Advanced → 5 Expert, modeled on the Dreyfus logic). These scales must be **bilingual IT/EN** enums redirected to the frontend according to the language switch, consistent with the project constraint.

### 3.3 Evidence signals (input for skill inference)

This is the core of the value: instead of relying only on self-declaration (notoriously unreliable and quickly obsolete), the system **infers** skills from the signals people already produce. These signals come, where possible, from the existing LocalMind domains.

| Signal | What it indicates | LocalMind domain / connector | Indicative weight |
|---|---|---|---|
| Drafted/signed documents | author of documentation on a topic → skill on the topic | `document` (Tika/OCR) | High if author, medium if only cited |
| Emails and messaging threads | active participation in technical discussions on a topic | `email`, `messaging` | Medium (modulated by role in the thread) |
| Events/calendar | participation/speaker at meetings, training, conferences on a topic | `calendar` | Low-medium |
| Contributions to repositories | commits, PRs, files touched → skill on languages/modules | Git connector (plugin) | High (strong and dated evidence) |
| Resolved tickets/issues | resolution of problems in an area → operational skill | Jira/issue tracker connector (plugin) | High |
| Certifications and training | certificates, completed courses | HRIS/LMS, upload | High but to be validated (formal ≠ practical) |
| Projects carried out | experience on technologies/clients/domains of a project | resource/project management | High |
| CV / professional profiles | historical basis of skills | upload (PDF), import | Medium (self-declared) |
| Self-declaration and endorsement | the person declares or colleagues confirm a skill | LocalMind UI | Variable: declaration low, endorsement medium-high |

Cross-cutting requirements on signals:
- **Provenance always tracked**: every inferred skill must trace back to the exact evidence (document, email, commit, ticket) with a timestamp. No "black boxes".
- **Recency / decay**: signals have a date; the weight decays over time (a skill demonstrated 5 years ago weighs less than one from 6 months ago), to model obsolescence.
- **Deduplication and normalization**: the same content must not generate multiple pieces of evidence; the extracted skills must be normalized against the taxonomy (synonyms, IT/EN linguistic variants).
- **Minimization**: only what is needed to infer skills is ingested; non-pertinent sensitive content must be excluded or filtered.

### 3.4 Skill requirements (the "demand")

To perform gap analysis and matching, you need to define **what is needed**, not just what exists:

- **Role profiles**: for each role, the required skills and the target level (e.g., "Senior Backend" requires Java≥4, Kafka≥3, SQL≥3).
- **Project requirements**: skills needed for a project, with level and number of people, time window, language/location.
- **Open positions**: requirements of a vacancy for internal matching.
- **Strategic objectives/roadmap**: capabilities the company wants to develop (e.g., "generative AI", "Kubernetes") for the anticipated planning of gaps.

### 3.5 Declarative and feedback data of the person

These are the inputs that keep the system correct and ethical (the employee is not a passive object of the graph):

- **Confirmation/correction of the inferred profile**: the person validates, downgrades, or removes the skills the system has attributed to them (a fundamental loop: it feeds weight and accuracy).
- **Self-declaration of level and interests**: skills possessed, perceived level, **aspirations** (skills they want to develop — a key input for mobility).
- **Endorsement among colleagues**: mutual confirmation of skills, with weight modulated by the competence of who endorses.
- **Visibility preferences**: what to make visible, to whom, and what to keep private.

### 3.6 System configuration

- **LLM provider and model** for inference (default local Ollama; cloud only with explicit consent), embedding model, interface language (IT/EN).
- **Enabled ingestion sources** and their connectors (HRIS, AD/LDAP, Git, Jira, existing folder watcher).
- **Inference and weighting parameters**: confidence thresholds to accept/propose a skill, temporal decay formula, relative weights of the signal types (§5).
- **Gap analysis policies**: bus factor threshold (e.g., "critical if ≤1 person at level ≥4"), skills considered strategic/critical.

### 3.7 Validation, privacy, and input rules

This scope has stricter compliance requirements than any other:

- **Legal basis and transparency**: the ingestion of personal signals must be configured with an explicit legal basis; people must know which sources are analyzed.
- **Granular consent and opt-out**: per source (e.g., "analyze my documents but not my emails") and per visibility.
- **Technical validation**: MIME type and size of files, integrity, schema of the imported HRIS data; fail early with clear messages.
- **Human-in-the-loop model**: skills inferred with non-maximal confidence enter as **"proposals"** (not as facts) until confirmed by the person or by a curator.
- **Immutability and auditability**: every change (inference, confirmation, correction, decay) creates a new tracked version; the history is the basis both for auditing and for the data subject's rights (access, rectification, erasure).
- **Anonymization/aggregation** for management views: sensitive individual data (e.g., "who is weak in what") must not be exposed in plain text in strategic dashboards.

## 4. Activity flow (step-by-step)

The flow describes the end-to-end experience, from the initial configuration to recurring use. It is designed for the MVP but also indicates the points of evolution. It crosses three main actors: the **administrator/HR** who configures, the **system/AI** that infers, and the **person** who validates and uses.

### Phase A — Setup and foundation of the graph

1. **Domain initialization.** The administrator selects the enterprise domain "People & skills", chooses the interface language (IT/EN) and the AI provider (default local Ollama). Configures the base privacy policies (§3.7): legal bases, enabled sources, visibility defaults.
2. **Import of personnel records and org chart.** People, roles, teams, hierarchy, and locations are imported from HRIS/AD/LDAP or CSV (§3.1). The system creates the `Persona`, `Ruolo`, `Team`, `Sede` nodes and the structural edges (`APPARTIENE_A`, `RIPORTA_A`, `RICOPRE_RUOLO`). Schema validation and report of rejected records.
3. **Loading the skills taxonomy.** A standard base (ESCO/O*NET) is imported and/or the company taxonomy is loaded (§3.2). The system creates the `Competenza` nodes with hierarchy, synonyms, and level scales (IT/EN enums). In the absence of a taxonomy, you start from an emergent one that will be normalized along the way.
4. **Definition of requirements.** HR/managers define the role profiles and any project/position requirements (§3.4), creating the `RICHIEDE` edges (with target level) between `Ruolo`/`Progetto` and `Competenza`. This is what will make gap analysis possible.

### Phase B — Signal ingestion and skill inference

5. **Connection of evidence sources.** The administrator enables the connectors (documents via the `document` domain, email/`messaging`, `calendar`, Git, Jira) respecting the consent policies. The existing folder watcher and ingestion pipeline begin collecting the content.
6. **Extraction and attribution (the inference engine).** For each ingested content item, the pipeline performs: text extraction (Tika/OCR) → chunking → embedding on Qdrant → **extraction of skills and context** via the local AI (reuse/extension of `EntityExtractorPort` of the `knowledge` domain). The system connects the author/participant person to the detected skills, generating candidate `POSSIEDE_COMPETENZA` edges with a **confidence level** and a reference to the evidence (provenance + timestamp).
7. **Weight and level calculation.** For each person↔skill pair, the system aggregates all the evidence, applies the temporal decay and the weighting by signal type (§5), and computes an **overall weight** and an **estimated level** on the mastery scale. Skills below the confidence threshold remain "proposals".
8. **Resolution and normalization.** Synonyms and variants (IT/EN, abbreviations) are merged against the taxonomy; any new emergent skills are proposed for inclusion in the taxonomy (curation).

### Phase C — Human-in-the-loop validation

9. **Profile review by the person.** Each employee sees their own inferred skills profile, with the supporting evidence ("we attribute *Kafka* to you because you wrote these 3 documents and resolved these 5 tickets"). They can **confirm, correct the level, remove** a skill or add self-declared ones, and indicate their own **aspirations**. This feedback feeds back into the weight (§5) and improves the model.
10. **Endorsement and curation.** Colleagues can confirm skills (weighted endorsement); a curator/HR can validate emergent skills and maintain the quality of the taxonomy. The visibility policies set by the person are applied.

### Phase D — Use: expert localization and team composition

11. **Find the expert (GraphRAG).** A user poses a natural-language question ("who knows the pricing engine and has worked with the Acme client?"). The AI translates the question into a query on the graph (structural match person→skill/project/client) combined with semantic search on Qdrant in the content, orders the candidates by weight/relevance/recency/availability, and returns a **motivated answer with citation of the evidence** and of the graph paths. It always respects visibility.
12. **Team composition.** Given a set of required skills (from a project), the system proposes combinations of people that cover the requirements, highlighting partial coverage and gaps, with the possibility of constraints (location, language, availability).
13. **Onboarding and navigation.** A new hire navigates the people↔team↔skills graph to orient themselves, and uses the tutor to know "whom to ask for what".

### Phase E — Analysis: gaps, risks, and planning

14. **Gap analysis.** HR/managers compare skills possessed vs required (by person, team, role, organization) and visualize the **holes**: missing, undersized skills, or present but below the target level.
15. **Risk analysis (bus factor).** The system highlights the **single points of knowledge**: critical skills staffed by a single person (or few), suggesting mitigation actions (document, train a second staffing point, cross-training).
16. **Training and mobility planning.** On the identified gaps, the system suggests targeted L&D plans (per person and aggregated) and internal mobility opportunities (people with skills transferable to uncovered roles/projects). Integrable with the `automation` domain for workflows (e.g., propose a course, open an application).

### Phase F — Maintenance and continuous loop

17. **Incremental update.** As new documents, commits, tickets arrive, the graph updates: new evidence strengthens skills, decay weakens those no longer demonstrated. The profile stays alive without periodic manual fill-ins.
18. **Quality monitoring.** Inference accuracy (confirmation/rejection rate), graph coverage, data freshness are measured; the feedback refines thresholds and weights.
19. **Person lifecycle management.** Hires, role changes, terminations are reflected in the graph; for terminated employees, retention/erasure policies are activated and, above all, the **capture of knowledge before departure** (who collects the legacy of critical skills).

## 5. Graph model (node types, relationship types, weighting criteria)

The model extends the existing `knowledge` domain (`KnowledgeEntity`, `KnowledgeRelation`, `EntityType`, `RelationType`) with scope-specific types, reusing MySQL for the structure and Qdrant for the semantics (no Neo4j). The current `EntityType`/`RelationType` (`PERSON`, `ORGANIZATION`, `CONCEPT`, `WORKS_AT`, `PART_OF`, `RELATED_TO`…) are the base to specialize.

### 5.1 Node types

| Node type | Description | Key attributes | Maps to `EntityType` |
|---|---|---|---|
| `Persona` | Employee/collaborator | internal id, name, email, status, join date, location, languages | `PERSON` |
| `Competenza` (Skill) | Capability/knowledge (technical, domain, soft) | name IT/EN, category, level scale, synonyms | `CONCEPT` (specialized) |
| `Ruolo` | Position/job role | title, level, professional family | new / `CONCEPT` |
| `Team` / Org unit | Department, team, cost center | name, type, location | `ORGANIZATION` |
| `Progetto` | Initiative with a skill need | name, period, status, client | `EVENT`/new |
| `Certificazione` | Formal certificate | body, date, expiry | new |
| `Evidenza` | Source that demonstrates a skill | type (doc/email/commit/ticket), reference, date | linked to `DOCUMENT` |
| `Cliente` / Domain | Business context of a skill | name, sector | `ORGANIZATION`/`CONCEPT` |
| `Obiettivo/Capability` | Desired strategic capability | description, priority | `CONCEPT` |

### 5.2 Relationship types (edges)

| Relationship | From → To | Meaning | Notes |
|---|---|---|---|
| `POSSIEDE_COMPETENZA` | Person → Skill | Skill possessed at a certain level | **Central weighted edge** (level + confidence) |
| `RICHIEDE` | Role/Project → Skill | Skill required at a target level | Basis of gap analysis |
| `ASPIRA_A` | Person → Skill | Skill the person wants to develop | Mobility/training |
| `RICOPRE_RUOLO` | Person → Role | Current (or historical) position | Temporal |
| `APPARTIENE_A` | Person → Team | Organizational membership | `PART_OF` |
| `RIPORTA_A` | Person → Person | Hierarchical relationship | |
| `HA_LAVORATO_A` | Person → Project | Project experience | Source of skills |
| `CONOSCE_CLIENTE` | Person → Client | Relationship/experience with a client | |
| `DIMOSTRATA_DA` | Skill(of Person) → Evidence | Link skill↔proof | Provenance/auditability |
| `MENTORE_DI` | Person → Person | Mentoring relationship (on a skill) | Knowledge transfer |
| `ENDORSED_BY` | Skill(of Person) → Person | Confirmation by a colleague | Increases the weight |
| `PREREQUISITO_DI` / `CORRELATA_A` | Skill → Skill | Taxonomy structure | For paths and transferability |
| `COLLABORA_CON` | Person → Person | Frequent co-authoring/co-participation | Inferred from signals |

### 5.3 Criteria for edge weighting

The weight is what distinguishes this graph from a simple list. The **weight of a `POSSIEDE_COMPETENZA` edge** (and similarly of the others) is a configurable and transparent function of the following factors:

- **Quantity and quality of evidence.** The more distinct content demonstrates the skill, the greater the weight. The *role* in the evidence counts: author of a document > cited in a document; resolver of a ticket > commenter.
- **Signal type (reliability).** Differentiated weights by source (§3.3): commits/resolved tickets and projects weigh more than participation in a meeting or a simple self-declaration. Certifications weigh a lot but are "formal" (they must be cross-referenced with practice).
- **Recency and temporal decay.** Evidence has a date; the contribution to the weight decays over time with a configurable function (e.g., half-life of N months). It models obsolescence and keeps the graph "alive".
- **Human confirmations.** The explicit confirmation by the person and the **endorsements** of colleagues (themselves weighted by the competence of who endorses) increase the weight and confidence; a correction/rejection reduces it drastically.
- **AI inference confidence.** The LLM extraction score contributes: below threshold the skill remains "proposed", not "confirmed".
- **Contextual consistency and density.** Skills consistent with the role, the team, and other possessed skills (e.g., presence of prerequisites) strengthen plausibility.

The weight is then normalized onto a **level scale** (1–5) for human reading, internally keeping the continuous score. All parameters (weights by type, half-life, thresholds) are configurable (§3.6) and every weight is **explainable**: the user can see "why" a value is what it is, a necessary condition for trust and for compliance.

## 6. Data sources & connectors (ingestion)

| Source | What it brings | Mechanism | Status |
|---|---|---|---|
| HRIS / personnel system | Records, roles, org chart, certifications | CSV/API import → plugin connector | MVP (file import), evolution (API) |
| Active Directory / LDAP | People, groups, hierarchy, locations | Plugin connector | Evolution |
| Corporate documents | Authorship → skills | Existing `document` domain (Tika/OCR, folder watcher) | Direct reuse (MVP) |
| Email | Participation in thematic discussions | Existing `email` domain (IMAP) | Reuse (with consent) |
| Messaging / channels | Technical threads, internal Q&A | Existing `messaging` domain | Reuse (with consent) |
| Calendar | Meetings/training/speaker | Existing `calendar` domain | Reuse |
| Git repositories | Commits, PRs, files → technical skills | PF4J plugin connector (GitHub/GitLab/Bitbucket) | Evolution (high value) |
| Issue tracker (Jira, etc.) | Resolved tickets → operational skills | PF4J plugin connector | Evolution |
| LMS / training platforms | Completed courses, certifications | Plugin connector | Evolution |
| CV / profiles (upload) | Self-declared historical basis | PDF upload via `document` | MVP |
| UI input | Self-declaration, confirmations, endorsements, aspirations | Angular frontend | MVP |
| Skill standards (ESCO/O*NET) | Multilingual base taxonomy | Dataset import | MVP/evolution |

All specific external connectors (Git, Jira, LMS, HRIS) must be built as **PF4J plugins** publishable on the marketplace, consistent with the existing extensibility architecture, so as not to bloat the core and to respect the different corporate realities.

## 7. Features to create, develop, and maintain (MVP → evolution)

### 7.1 MVP (foundation: graph, base inference, find-expert, base gap)

| # | Feature | Description | LocalMind components involved |
|---|---|---|---|
| 1 | Skill graph data model | Extend `EntityType`/`RelationType` with scope types; JPA entities + Flyway migrations (one query per file) | `knowledge` domain, `localmind-infrastructure`, Flyway |
| 2 | Records + taxonomy import | Import CSV of people/org and taxonomy (custom + ESCO base) with validation | new domain/extension, API, frontend |
| 3 | Skill extraction from documents | Extend `EntityExtractorPort` to extract skills+context from already-ingested content, with local Ollama AI | `knowledge`, `document`, Qdrant |
| 4 | Inferred skills profile + provenance | Weight/level calculation + evidence tracking for each person↔skill pair | `knowledge` service, MySQL |
| 5 | Human-in-the-loop validation | UI to confirm/correct/add skills and set visibility | Angular frontend (Signals), API |
| 6 | Find the expert (GraphRAG) | NL query → graph+semantics, motivated answer with citations and visibility respect | `llm` (GraphRAG), `knowledge`, Qdrant |
| 7 | Base gap analysis | Comparison possessed vs required by role/team, holes view | `knowledge` service, frontend |
| 8 | Graph visualization (subset) | Navigable and filterable people↔skills↔team view | frontend (graph component) |
| 9 | Base privacy & consent | Visibility policies, opt-out per source, evidence audit | auth/`common`, config |

### 7.2 Evolution (connectors, advanced analytics, mobility)

| # | Feature | Description |
|---|---|---|
| 10 | Git/Jira/LMS connectors (PF4J plugins) | Skill inference from commits, resolved tickets, courses; high evidence value |
| 11 | HRIS/AD connectors via API | Continuous synchronization of records/org/hierarchy |
| 12 | Temporal decay & freshness | Obsolescence modeling, graph freshness dashboard |
| 13 | Bus factor / single point of knowledge | Identification and alert on mono-staffed critical skills |
| 14 | Automatic team composition | Suggestion of people combinations to cover project requirements |
| 15 | Endorsement & mentoring | Weighted confirmations among colleagues; mentor↔mentee matching on skills |
| 16 | Internal talent marketplace | Matching people↔internal opportunities/positions with transferable skills |
| 17 | Targeted training plans | L&D recommendations per person and aggregated, hooked to gaps |
| 18 | Strategic workforce planning | Capability vs roadmap comparison, "what-if" simulations on future gaps |
| 19 | Aggregated/anonymous management dashboards | Strategic views with anonymization and aggregation |
| 20 | Pre-departure knowledge capture | Workflow to capture/transfer critical skills before terminations |
| 21 | Advanced ontological normalization | Synonym merging, multi-standard mapping ESCO↔O*NET↔custom |

### 7.3 To maintain (operational continuity)

- **Taxonomy quality**: continuous curation of skills, synonyms, hierarchies; deduplication.
- **Inference accuracy**: monitoring of confirmation/rejection rate, recalibration of thresholds and weights.
- **Data freshness**: incremental ingestion, decay management, realignment with HRIS.
- **Compliance**: periodic review of legal bases, consents, retention policies; handling of GDPR requests (access, rectification, erasure).
- **IT/EN translations**: enums (levels, categories), UI labels, documentation always bilingual.
- **Graph performance**: MySQL indexes and Qdrant collections optimized as the graph grows.

## 8. AI / GraphRAG use cases

1. **Natural-language expert localization.** "Who can help me with a Stripe integration and has already worked in fintech?" → the AI combines structural match (Skill *Stripe/Payments*, Domain *fintech*) and semantic match (produced content), orders by weight/recency/availability, answers with the names and the **evidence** that justifies them.
2. **Team composition.** "Compose a team for a project that requires senior React, mid Java, UX, and knowledge of the Acme client" → proposes combinations that cover the requirements, highlighting residual gaps.
3. **Explanation of an attribution.** "Why do you attribute the Kafka skill to me at level 4?" → the AI lists the weighted evidence (documents, tickets, commits, endorsements) with dates.
4. **Conversational gap analysis.** "Which skills do we lack to tackle a Kubernetes project in the next 6 months?" → compares required vs possessed capabilities and suggests training/hiring/redistributing.
5. **Risk analysis.** "Which critical skills depend on a single person?" → lists the single points of knowledge with mitigation suggestions.
6. **Suggestion of non-obvious connections.** The AI proposes that two people in different teams share a rare skill and could collaborate/mentor (emergent edge `COLLABORA_CON`/`MENTORE_DI`).
7. **Personal growth path.** "I want to become Tech Lead: what am I missing?" → compares the profile with the target role, proposes skills to develop and internal mentors.
8. **Assisted onboarding.** "I'm new to the payments team: whom do I turn to for what?" → navigates the graph and returns the "who knows what" map.

All use cases respect the **visibility policies** and cite the sources; the inference runs on **local Ollama** by default, without data exfiltration.

## 9. KPIs & success metrics

| Category | Metric | Why it matters |
|---|---|---|
| Coverage | % of people with a populated skills profile; average no. of skills/person | Measures how "complete" the graph is |
| Inference quality | Confirmation vs rejection rate of proposed skills; precision/recall on a sample | Reliability of the engine |
| Freshness | % of skills with recent evidence (< N months); average age of the evidence | The graph is alive, not a fossil |
| Adoption | "Find the expert" queries/week; active users; % of profiles reviewed by employees | Perceived value and participation |
| Efficiency | Average time to find an expert (before vs after) | Direct operational ROI |
| Mobility | No. of successful internal matches; % of positions covered internally | Valorization of internal talents |
| Risk | No. of critical single points of knowledge; trend over time | Reduction of organizational fragility |
| Gap | % coverage of required skills by role/project | Capacity↔need alignment |
| Compliance | % of ingestions with valid legal basis and consent; GDPR request fulfillment time | Legal sustainability of the system |

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| **Privacy/compliance** (processing of worker data) | High — legal and trust | Local-first, explicit legal basis, granular consent/opt-out, human-in-the-loop, audit, anonymization in aggregated views |
| **Perceived surveillance** of employees | High — cultural rejection | Total transparency (the person sees and controls their profile), framing on growth/opportunity not control, shared governance |
| **Wrong inferences/bias** | Medium-high | Explicit confidence, "proposals" vs "confirmed", human validation, weight explainability, bias monitoring |
| **Data obsolescence** | Medium | Temporal decay, incremental ingestion, freshness metrics |
| **Inconsistent taxonomy** (synonyms, granularity) | Medium | Standard import (ESCO/O*NET), normalization, curation, IT/EN synonym management |
| **Reducing people to a score** | Medium — ethical | Never use the graph as the sole criterion for HR decisions; competence ≠ value of the person; human-in-the-loop in decisions |
| **Source quality** (noisy signals) | Medium | Weighting by signal reliability, minimization, deduplication |
| **Graph scalability on MySQL+Qdrant** | Medium | Targeted indexes, subgraph queries, denormalizations where needed; reconsider a graph datastore only if necessary |
| **Low adoption** | Medium | Immediate value (find-expert), zero friction (automatic inference), benefits visible to the employee |

## 11. Maintenance & evolution

- **Taxonomy governance.** Appoint curators (HR + technical referents) who maintain skills, synonyms, and hierarchies; a process to accept the skills emerging from the AI. Periodic alignment with the standards (ESCO/O*NET) and with market reality.
- **Continuous engine calibration.** Periodically review weights by signal type, decay half-life, and confidence thresholds, based on the confirmation/rejection rate and feedback.
- **Data lifecycle.** Retention/erasure policies consistent with the person's lifecycle (hire, role change, termination) and with GDPR rights; capture of knowledge before departures.
- **Connector extension.** Progressively add connectors (Git, Jira, LMS, HRIS) as PF4J plugins, publishing them on the marketplace; each connector with its own consent configuration.
- **Permanent bilingualism.** Maintain enums, labels, and documentation in IT/EN; every new skill/category is born with both languages.
- **Evolution toward workforce planning.** From the current "who knows what" toward "what-if" scenarios and the anticipated planning of gaps relative to the corporate roadmap.
- **Development tracking.** Every intervention documented in the `Sviluppi/` folder with the dated nomenclature foreseen by the project, in plan mode, constantly updating the IT/EN documentation.

## 12. Integration with the existing LocalMind modules

| Module / domain | Role in the People & skills scope |
|---|---|
| `knowledge` | **Foundation**: the skill graph extends `KnowledgeEntity`/`KnowledgeRelation`; `EntityType`/`RelationType` are specialized and `EntityExtractorPort` and `KnowledgeGraphPort`/`KnowledgeGraphUseCase` are reused/extended |
| `document` | Primary source of evidence: drafted documents → skills (Tika/OCR, folder watcher, chunking already operational) |
| `email` / `messaging` | Sources of evidence from discussions; thematic participation → skills (with consent) |
| `calendar` | Evidence from meetings/training/speaker; integration with availability for team composition |
| `llm` | Inference engine (skill extraction) and GraphRAG (find-expert), with fallback chain and **local Ollama by default** |
| `mcp` | Exposure of tools ("find expert", "gap analysis") to external agents/LLMs via Model Context Protocol |
| `agent` | Agents that orchestrate multi-step flows (e.g., compose a team, prepare an L&D plan) |
| `automation` | Workflows triggered by graph events (gap detected → propose a course; new mono-staffed critical skill → alert) |
| `marketplace` / `plugin` (PF4J) | Distribution of connectors (HRIS, AD, Git, Jira, LMS) and of taxonomy packages (ESCO/O*NET) |
| `finetuning` | Possible specialization of a local model for skill extraction in the corporate jargon |
| `auth` / `common` | Security, visibility policies, audit, domain events for tracking changes to the graph |
| Angular frontend | New `people-skills` feature (standalone, Signals, IT/EN): skills profile, find-expert, gap analysis, graph visualization |

From an implementation point of view, the scope follows the project's architectural pattern: pure domain in `localmind-domain` (models, port/in, port/out, services without Spring), wiring in `DomainConfig`, adapters in `localmind-infrastructure`, controllers/DTOs in `localmind-api`, Flyway migrations with **only one query per file**, UUIDs with `@JdbcTypeCode(SqlTypes.CHAR)`, lazy-loaded frontend feature. The People & skills scope is the **connective tissue** of the enterprise knowledge graph: it gives a human owner to knowledge and multiplies the value of all the other scopes.

---

### Sources and references (2026 best practices)

- [What Is a Skills Graph? The 2026 Guide for HR Leaders — 365Talents](https://365talents.com/en/resources/skills-graph-guide-hr-leaders/)
- [Skills ontology framework: Why You need it in 2026 — Gloat](https://gloat.com/blog/skills-ontology-framework/)
- [Skills Ontology: What Is It & How To Build One? — AIHR](https://www.aihr.com/blog/skills-ontology/)
- [Open Skills and Talent Graphs: Guide to Skills-Based Hiring — JobsPikr](https://www.jobspikr.com/blog/open-skills-and-talent-graphs-2025/)
- [Expertise Locators and Ask the Expert — Stan Garfield](https://stangarfield.medium.com/expertise-locators-and-ask-the-expert-f273db1e227c)
- [An Enterprise Knowledge Graph Approach (skills & expertise) — CEUR-WS](https://ceur-ws.org/Vol-3780/paper9.pdf)
- [Knowledge Graph (Employee Data) — ChangeEngine](https://www.changeengine.com/glossary/what-is-knowledge-graph-employee-data)
- [Understanding O*NET and ESCO: Standards for Skills — Pexelle](https://pexelle.com/understanding-onet-and-esco-standards-for-skills-in-the-modern-workforce/)
