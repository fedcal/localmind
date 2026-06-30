# Internal Onboarding & Training

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

## 1. What We Solve (Problem & Value)

### 1.1 The Problem of Onboarding and Training in Organizations

Bringing a new person into a company — and more generally the continuous training of those already working there — is one of the processes with the highest hidden cost and the lowest degree of industrialization. A new hire faces a reality that exists but is not written down anywhere in a navigable way: who does what, where the up-to-date documentation lives, what the "real" process is (not the one described in an obsolete wiki), which tools they need to install, which systems make up the architecture, and whom to ask when they get stuck. The organization's operational knowledge lives in three places, all inadequate for a newcomer: **in people's heads** (and it vanishes when they go on vacation or change jobs), **in fragmented document repositories** (wikis, Confluence, Google Drive, SharePoint, Notion, Git repositories, tickets, chat threads) that nobody keeps aligned, and **in implicit processes** that were never formalized. The concrete result is a series of recurring symptoms:

- **Extremely long time-to-productivity.** Entry-level positions reach full operability in about 30 days, but technical and senior roles require 60-90 days or more, and on average a new hire takes 6-7 months to feel truly comfortable in the role. Every week of delay is pure cost.
- **High and mostly invisible economic cost.** Organizations typically spend between $3,000 and $7,000 per hire, considering system access, training content, manager time, and lost initial productivity; the overall cost of replacing a person and waiting for their full productivity exceeds $30,000 for mid-level roles. The heaviest item is not the course, but the time stolen from colleagues and managers who act as "human tutors," repeatedly answering the same questions.
- **Fragmented and disconnected knowledge.** Process documentation lives in the wiki, code and its conventions in Git, architectural decisions in threads or ADRs, people and their roles in the org chart, tools in runbooks, customers in the CRM. None of these systems knows about the others, and above all **none of them model the relationships** that matter for someone learning: "this service depends on that one," "this process is the responsibility of that team," "to do X you first need to know Y."
- **One-size-fits-all, linear, static onboarding.** Traditional paths are identical checklists for everyone, regardless of role, team, seniority level, and what the person already knows. A senior developer and a junior marketer often receive the same "welcome package." What is missing is an **adaptive path** that starts from what the person already knows and brings them where they need to be, respecting the order of prerequisites.
- **Questions without contextual answers.** When the new hire gets stuck, the options are: search the wiki (obsolete or missing results), interrupt a colleague (who loses focus), open a ticket (slow), or ask a generalist chatbot that doesn't know *that* system, *those* processes, *that* company jargon. What is missing is a tutor that answers **based on real internal knowledge** and that can cite the exact document, commit, or runbook.
- **Knowledge that is lost when people leave.** When an experienced employee leaves the company, they take with them a wealth of implicit relationships ("Marco wrote that module, and the reason it's built this way is...") that no document captures. Offboarding is a hemorrhage of knowledge.
- **Continuous training disconnected from real work.** Beyond initial onboarding, recurring training (a newly introduced system, a new policy, a new process, upskilling, compliance) suffers from the same ills: generic courses, disconnected from the tools the person actually uses and lacking a review system anchored to the skills map.

### 1.2 Our Answer: Onboarding and Training Paths Built on the Internal Knowledge Graph

LocalMind, in its evolution into a **universal knowledge graph engine**, addresses exactly this problem on the enterprise side. The value proposition is to transform the company's document chaos and tacit knowledge into a **weighted, navigable internal knowledge graph**, and to use that graph as the backbone of **personalized onboarding and training paths**. The nodes are people, teams, documents, processes, systems, tools, skills, runbooks, decisions; the edges represent operational relationships — "is a prerequisite of," "is responsible for," "documents," "depends on," "is part of the process," "is an expert in." Three capabilities are grafted onto this graph:

1. **Automatic ingestion and mapping of internal knowledge.** Documents, wikis, repositories, tickets, emails, and processes are ingested, segmented, vectorized on Qdrant, and extracted into entities and relationships that populate the graph, with the connections inferred automatically by the AI and then refined by domain experts (knowledge owners). Tacit knowledge is made explicit and structured.
2. **GraphRAG tutor on company knowledge.** The AI (Ollama locally by default) answers the new hire's questions by navigating the internal graph and retrieving the semantically relevant fragments, **always citing the exact source** (document, section, commit, runbook). No company data leaves the on-premise infrastructure without explicit consent — a non-negotiable requirement in the enterprise context.
3. **Adaptive onboarding and training paths.** Starting from the role, team, level, and what the person already knows, the system generates a path that respects the order of prerequisites, assigns concrete materials and activities, tracks progress, and schedules the review of critical skills (compliance, security, key processes).

### 1.3 The Measurable Value

Structured onboarding is not a cost, it is a high-return investment: effective programs improve retention by up to 52%, productivity by up to 60%, and satisfaction by 53%; people who go through structured onboarding are 58% more likely to stay with the company after three years. The 2026 trend is clear: according to Gartner, by the end of 2026, 40% of enterprise applications will use task-specific AI agents to orchestrate work across systems, and about 30% of large companies (IDC) have already adopted AI-augmented onboarding at scale, with faster time-to-productivity and reduced early attrition as measurable payoffs. LocalMind delivers this value **without cloud lock-in, without subscription fees, and without exfiltration of sensitive data**.

### 1.4 Why LocalMind Is the Right Platform

| Organization's need | LocalMind feature that satisfies it |
|---|---|
| Absolute privacy of internal knowledge (code, processes, people, customers) | **Local-first / self-hostable**: everything runs on-premise; nothing is sent to the cloud without explicit consent |
| No recurring fee per employee | **Pure open source**, local Ollama AI by default, no paywall |
| Knowledge in heterogeneous formats and sources | Existing ingestion pipeline (**Tika + Tesseract OCR + Whisper**), chunking, embedding; plugin connectors |
| Answers grounded in the *real* knowledge of the company | **GraphRAG** on the internal graph + Qdrant semantic search, with source citation |
| Connection between already-present systems (mail, calendar, repo, tickets) | Existing domains `email`, `calendar`, `mcp`, `document`, `automation` |
| Extensibility toward company sources (LMS, HRIS, Confluence, Jira, Git) | **PF4J plugin system** + marketplace |
| Multilingual interface and content | **Bilingual IT/EN platform** by design, translated enums |
| Automation of onboarding/training triggers | `automation` + `agent` domains to orchestrate activities |

The competitive differentiator compared to existing tools (LMSs such as 360Learning/D2L, knowledge bases such as Bloomfire/Confluence, cloud onboarding assistants) is the combination of three factors that no competitor offers together: **an explicit weighted graph of internal knowledge + GraphRAG executed entirely locally + total enterprise privacy at zero license cost**. LMSs manage courses but do not model the relationships between knowledge, systems, and people; knowledge bases have search but neither the navigable graph nor adaptive paths; AI onboarding assistants are almost always cloud-only and send company data to third parties. LocalMind unites these worlds while remaining sovereign over the data.

## 2. Personas & Target Users

| Persona | Profile | Primary needs | How they use LocalMind |
|---|---|---|---|
| **Luca, new technical hire (developer)** | 28 years old, joins a team that maintains microservices; must become productive in 60-90 days | Understand the architecture, code conventions, deploy processes, whom to ask | Follows the onboarding path for their role, navigates the systems/services graph, uses the tutor on code and runbooks, completes checklists and self-assessments |
| **Sofia, new non-technical hire (sales/marketing/ops)** | 26 years old, must learn products, commercial processes, CRM tools, and policies | Clear onboarding, policy review, knowing whom to contact for what | Guided path by role, tutor on processes and products, map of people and responsibilities |
| **Marta, hiring manager / team lead** | Responsible for bringing in new team members | Reduce time spent answering the same questions; see new hires' progress | Defines/customizes team paths, monitors progress (in a privacy-respecting form), delegates repetitive questions to the tutor |
| **Giorgio, HR / L&D specialist** | Designs onboarding and training programs at the company level | Standardize onboarding, measure effectiveness, manage compliance | Creates path templates per role/department, defines compliance modules with review, analyzes aggregated and anonymous KPIs |
| **Elena, knowledge owner / domain expert (SME)** | Senior who holds critical knowledge about an area | Transfer her knowledge without being constantly interrupted | Curates the sub-graph of her area, confirms/corrects the connections suggested by the AI, "deposits" knowledge only once |
| **Paolo, employee in continuous training** | Person already in the company who must learn a new system/process | Targeted upskilling, skills review, reskilling | Activates training paths on new topics, receives scheduled review of critical skills |
| **CTO / IT / Security** | Decides on adoption and governs its privacy and security | Self-hosting, data control, audit, integration with internal systems | Configures the on-premise instance, manages graph permissions and visibility, verifies that nothing leaves without consent |

The primary and priority user for the MVP is **Luca / Sofia (new hire)** alongside **Marta (manager)** and **Elena (knowledge owner)**: the former are the direct beneficiaries of the path and the tutor, the latter are indispensable for populating and validating the graph. The other personas drive the evolutions (HR/L&D for standardization and KPIs, continuous training for upskilling).

## 3. Input Requirements

This section defines in detail **what must be able to enter the system** for the Onboarding & Training scope to work. The inputs are divided into: company knowledge to be ingested, organizational structure, path definition, the learner's personal data, configuration, and feedback. Each input must be validated at the system boundary (the "never trust external data" principle) and treated immutably, with particular attention to visibility permissions (a new hire must not see what is not their concern).

### 3.1 Company Knowledge (Content to Be Ingested)

| Type of knowledge | Supported formats / sources | Extraction | Notes |
|---|---|---|---|
| Process documentation / policy | PDF, DOCX, Markdown, wiki (Confluence/Notion export, HTML) | Tika | Preserve section structure for precise citation |
| Operational procedures / runbooks | Markdown, PDF, wiki pages | Tika | Become "Procedure/Runbook" nodes linked to systems and roles |
| Source code and conventions | Git repositories (README, CONTRIBUTING, ADR, comments, structure) | Dedicated parser + Tika | Preserve path/commit for citation; extract dependencies between modules |
| Architectural decisions (ADR) | Markdown, PDF | Tika | "Decision" nodes linked to systems and rationale |
| Existing training material | Slides (PPTX/PDF), LMS courses, video tutorials | Tika + OCR + Whisper (transcription) | Preserve slide/minute for citation |
| Tickets / issues / support knowledge base | Jira/GitHub Issues export, FAQ, KB | Text + metadata extraction | A rich source of real problems and solutions |
| Internal emails and announcements | `email` module (IMAP) | Tika + attachment extraction | Notices, process communications (with permissions) |
| Org chart / people directory | CSV/HRIS export, directory | Structured parser | Populates Person, Team, Role nodes |
| Glossary / company jargon | CSV, Markdown, wiki | Parser | Crucial for disambiguating internal language |
| Recordings of sessions / past onboardings | Audio/video | `WhisperTranscriptionAdapter` (existing) | Transcription with timestamps |

Cross-cutting requirements on knowledge:
- **Permissions and visibility** declared for each source: who can see what. The graph must be able to filter nodes/edges based on the learner's role and level (a new hire does not see confidential documents or irrelevant areas).
- **Provenance** always tracked: every fragment and every entity must be traceable back to the file/commit/ticket and to its position of origin (section, line, minute).
- **Freshness/versioning**: every document carries a date; the system must flag potentially obsolete knowledge (e.g., a runbook not updated for months while the system has changed).
- **Deduplication**: recognize already-ingested knowledge (content hash) to avoid duplicate entities.
- **Language** detected automatically (IT/EN and beyond) to choose the embedding model and the tutor's response language.
- **Maximum file size** configurable, with a clear error message when exceeded.

### 3.2 Organizational Structure (Context)

To give meaning to the knowledge, structure metadata is needed, provided by HR/IT or inferred and confirmed:

- **People**: name, role, team, seniority, skills, manager, area of expertise (knowledge owner).
- **Teams / Departments**: name, mission, responsibilities, member people, hierarchy.
- **Roles / Job profiles**: role description, responsibilities, required skills — the basis for path templates.
- **Systems / Services / Tools**: catalog of systems (microservices, applications, tools), with owner and dependencies.
- **Business processes**: steps, owner, systems involved, linked documentation.
- **Calendar**: start dates, training sessions, compliance deadlines (integrable with the `calendar` module).

### 3.3 Path Definition (Templates and Customization)

These are the inputs that transform the graph into a guided experience:

- **Path templates per role/department**: the sequence of stages that a certain profile must complete (e.g., "Backend Developer Onboarding": environment setup → architecture → code conventions → first task → deploy process).
- **Stages and activities**: each stage links graph concepts/skills to materials, self-assessments, practical activities ("open repo X and run the tests"), and reference people (buddy/mentor).
- **Prerequisites between stages**: mandatory order (a topic is not proposed before its prerequisites).
- **Compliance/mandatory modules**: with an optional deadline and forced periodic review (security, privacy, regulations).
- **Mentor/buddy assignment**: reference person linked to the path.

### 3.4 The Learner's Personal Data (Skill State)

These are the inputs that make the system *personal* and *adaptive*. They must be treated with the utmost confidentiality (local-first, never exfiltrated, restricted access):

- **Entry profile**: assigned role, team, seniority, start date, declared prior skills (so the person doesn't repeat what they already know).
- **Goal / target**: "become operational on service X within 60 days," "complete the compliance module by the deadline."
- **Progress state**: stages completed, in progress, to do; self-assessments passed.
- **Practice results**: outcomes of quizzes, completed checklists, validated practical activities — they feed the skill-state estimate (knowledge tracing) and therefore the review.
- **Preferences**: style (practical vs. theoretical), response language, pace.
- **Available time**: hours/day that can be dedicated (useful for planning the load).

### 3.5 System Configuration

- **LLM provider and model** (default local Ollama; optional cloud with explicit consent), embedding model, interface language (IT/EN).
- **Ingestion sources**: monitored local folders (existing folder watcher), plugin connectors (Confluence, Jira, Git, LMS, HRIS, Drive).
- **Privacy/visibility policies**: permission model on the graph (per role, team, level); what is internal-public, what is restricted.
- **Path parameters**: spaced-repetition algorithm for compliance, completion thresholds, reminder aggressiveness.
- **Integration with company tools**: calendar, email, notification systems (`messaging`).

### 3.6 Feedback (Continuous Loop)

- **Corrections to the graph** by knowledge owners: add/remove/relabel nodes and edges, confirm or reject the connections suggested by the AI (feeds the edge weights, §5).
- **Evaluation of the tutor's answers** (thumbs up/down, flagging out-of-context, ungrounded, or obsolete-material-based answers).
- **Reporting of missing or obsolete knowledge** by learners ("I searched for X and it wasn't there / it was wrong"), which generates tasks for knowledge owners.
- **Feedback on paths**: unclear, missing, or out-of-order stages — feeds back into the template.

### 3.7 Input Validation and Rules

- All files pass through MIME type, size, and integrity validation.
- Mandatory metadata (e.g., role for a path, owner for a system) is required; the rest can be progressively enriched.
- No input is ever mutated in place: every revision (e.g., correction of an edge, update of a runbook) creates a new version, preserving the history for audit and for weight calculation.
- Permissions are applied **at the graph query level**: the learner must never be able to retrieve (not even via the tutor) content outside their visibility perimeter.
- Skill self-assessments are always overridable and cross-checked with practice data, never considered absolute truth.

## 4. Activity Flow (Step-by-Step)

The flow describes the end-to-end experience, from the preparation of the company graph to the individual's onboarding and recurring training. It is designed for the MVP but also indicates the points of evolution.

### Phase A — Preparation of the Knowledge Base (Company side, one-time + maintenance)

1. **Instance configuration.** IT/Security installs LocalMind on-premise (documented self-hosting), configures AI providers (default local Ollama), language (IT/EN), and the permission/visibility model.
2. **Definition of the organizational structure.** HR/IT imports or enters people, teams, roles, systems, and processes (also via CSV/HRIS). These become the "skeleton" nodes of the graph.
3. **Knowledge ingestion.** Sources are connected: monitored local folders (existing folder watcher), Git repositories, wiki/Confluence exports, tickets, training material, email (with permissions). Each source declares its visibility and ownership.
4. **Extraction and validation.** The system validates (type, size), extracts the text (Tika), applies OCR (Tesseract), and transcribes audio/video (Whisper). Errors are reported with clear messages; an unreadable file does not block the others.
5. **Segmentation and embedding.** The content is split into chunks (ChunkingService), vectorized, and indexed on Qdrant with provenance and permission metadata; documents and chunks are persisted on MySQL.

### Phase B — Building the Internal Knowledge Graph

6. **Extraction of entities and concepts.** An AI job analyzes the chunks and proposes the nodes (Concept/Skill, Procedure, System, Decision, etc.), deduplicating synonyms and jargon.
7. **Inference of relationships.** The AI proposes the edges: prerequisites ("knowing Git is a prerequisite of the deploy process"), responsibilities ("the Payments team is the owner of the Billing service"), dependencies ("the Orders service depends on Billing"), documentation ("runbook R documents system S"), expertise ("Elena is an expert in authentication"). Each edge is born with an initial confidence weight.
8. **Human review (human-in-the-loop).** Knowledge owners and managers review the proposed graph and confirm, correct, add, or remove nodes and edges. Confirmations increase the weight; rejections reduce or eliminate it. A crucial step for quality and trust: tacit knowledge is made explicit only once.
9. **Interactive visualization.** The graph is navigated: from the node of a role/system, neighbors are expanded, filtered by node/relationship type, and prerequisite paths and responsibility maps are highlighted.

### Phase C — Definition of Onboarding/Training Paths

10. **Creation of templates per role.** HR/L&D and managers define the standard paths (e.g., "Backend Developer," "Sales Junior," "Annual Compliance"), linking each stage to graph concepts/skills, materials, self-assessments, practical activities, and reference people (buddy/mentor).
11. **Ordering by prerequisites.** The system uses the `is_prerequisite_of` edges to validate and suggest the order of the stages, avoiding inconsistencies.
12. **Marking of mandatory modules.** Compliance modules receive deadlines and periodic review rules.

### Phase D — Individual Onboarding (the new hire's experience)

13. **Path activation.** Upon profile creation (role, team, start date, prior skills), the system instantiates the adapted path: it skips what the person already masters, orders by prerequisites, and assigns the buddy.
14. **Guided execution, stage by stage.** For each stage, the learner sees: the tutor's explanation, relevant materials (with citation), concrete practical activities, and the self-assessment. The checklist always shows "where I am."
15. **Ask the tutor at any moment.** The learner asks questions in natural language (e.g., "how do I deploy to staging?").
16. **GraphRAG retrieval.** The system identifies the relevant nodes, navigates the graph to gather the relevant sub-graph (concept + prerequisites + system + owner), and retrieves the semantically closest chunks from Qdrant, **filtered by the learner's permissions**.
17. **Grounded and cited answer.** The AI (Ollama by default) generates the answer using *only* the relevant internal knowledge, **citing the exact sources** (document/section, commit, runbook, minute) and the graph nodes/paths. If the knowledge does not exist or is out of perimeter, it states so and suggests whom to turn to (expert Person node).
18. **State update.** The outcomes of self-assessments and validated activities update the skill estimate (knowledge tracing): weak stages remain, solid ones are closed.
19. **Continuous feedback.** The learner evaluates answers and stages; reports missing/obsolete knowledge, generating tasks for knowledge owners.

### Phase E — Managers and Knowledge Owners in the Loop

20. **Privacy-respecting monitoring.** The manager sees the path's progress (completed stages, recurring blocks) in a form useful for support, not as surveillance; the most frequent questions from new hires highlight gaps in the documentation.
21. **Graph maintenance.** The tasks generated by feedback ("the runbook for X is missing," "process Y has changed") reach the knowledge owners, who update the knowledge at the source; the graph is enriched incrementally.

### Phase F — Continuous Training and Review (beyond initial onboarding)

22. **Activation of recurring training paths.** For new systems, processes, policies, or upskilling, new paths are activated on the same rails (graph + tutor + self-assessments).
23. **Review of critical skills (spaced repetition).** Mandatory/critical skills (compliance, security) enter a spaced-repetition calendar; outcomes readjust the intervals. Reminders go through `messaging`/`calendar`/`email`.
24. **Re-onboarding and offboarding.** When a person changes roles, a transition path is activated; when a person leaves, the system helps extract and formalize their tacit knowledge before it is lost.

### Phase G — Evolution and Governance

25. **Incremental update.** Newly ingested knowledge extends the graph without rebuilding it; the system proposes new connections and flags obsolete knowledge.
26. **Audit and compliance.** All actions (who completed what, when) are tracked for compliance purposes, remaining internal and local-first.

## 5. Graph Model (Node Types, Relationship Types, Weight Criteria)

The model reuses the infrastructure of the **core knowledge graph engine** (typed nodes + weighted edges on MySQL for the structure, Qdrant for the semantics). Below are the types specific to the Onboarding & Training scope.

### 5.1 Node Types

| Node type | Description | Key attributes |
|---|---|---|
| **Person** | Employee, new hire, mentor, knowledge owner | name, role, team, seniority, areas of expertise |
| **Team / Department** | Organizational unit | name, mission, responsibilities, hierarchy |
| **Role / Job profile** | Professional profile | description, responsibilities, required skills |
| **Skill / Competency** | Capability or knowledge required by a role | name, level (basic/intermediate/advanced), area |
| **Concept** | Atomic unit of internal knowledge | name, brief definition, synonyms/jargon |
| **Document / Material** | Ingested knowledge resource | type, title, language, hash, date, provenance, visibility |
| **Chunk / Fragment** | Segment of material (linked to Qdrant vectors) | text, position, vector id, permissions |
| **Procedure / Runbook** | Operational instructions | steps, linked system, owner, last revision |
| **Business process** | End-to-end workflow | steps, owner, systems involved |
| **System / Service / Tool** | Technical component or tool | name, owner, dependencies, environment |
| **Decision (ADR)** | Architectural/organizational decision | title, context, choice, rationale |
| **Onboarding/training path** | Ordered sequence of stages | target role, stages, state, deadlines |
| **Stage / Module** | Unit of a path | title, materials, activities, self-assessment, prerequisites |
| **Self-assessment / Quiz** | Skill verification tool | questions, solutions, measured skill |
| **Event / Deadline** | Start date, training session, compliance deadline | date, type, linked path |
| **Glossary / Term** | Company jargon entry | term, definition, synonyms |

### 5.2 Relationship Types (Edges)

| Relationship | From → To | Meaning | Directed |
|---|---|---|---|
| **is_prerequisite_of** | Skill/Concept → Skill/Concept/Stage | A requires mastery of B | Yes |
| **requires_skill** | Role → Skill | The role needs that skill | Yes |
| **documents** | Document/Runbook → System/Process/Concept | The resource explains/describes the element | Yes |
| **is_responsible_for / owner_of** | Person/Team → System/Process/Document | Ownership/operational responsibility | Yes |
| **is_expert_in** | Person → Skill/System/Area | Point of contact for that area | Yes |
| **depends_on** | System → System | Technical/architectural dependency | Yes |
| **is_part_of** | System/Person → Team / Stage → Path | Membership/composition | Yes |
| **covers / teaches** | Stage/Material → Skill/Concept | The stage develops that skill | Yes |
| **verifies** | Quiz/Self-assessment → Skill | Measures mastery | Yes |
| **mentor_of / buddy_of** | Person → Person | Support during onboarding | Yes |
| **is_part_of_process** | Procedure/System → Process | Step of a process | Yes |
| **succeeds / step_after** | Stage → Stage | Sequential order in the path | Yes |
| **derives_from / motivates** | Decision → System/Decision | Rationale of a choice | Yes |
| **linked_to / related** | Concept → Concept | Thematic affinity (cross-team) | No |
| **obsolete_relative_to** | Document → Document/System | Flags misalignment/superseded version | Yes |

### 5.3 Edge Weight Criteria

The weight (a normalized value, e.g., 0–1) expresses the **strength/reliability** of the relationship and guides both the visualization (thicker edges), the GraphRAG (exploration priority), and the ordering of the stages. The weight is calculated as a configurable combination of the following factors, consistent with the core principle "weight derived from configurable factors":

| Factor | Effect on weight | Example |
|---|---|---|
| **Confidence of the AI extraction** | Initial baseline of the edge | The LLM is very confident that the Orders service depends on Billing |
| **Knowledge owner confirmation** | Increases strongly | An SME confirms the connection → high and "stable" weight |
| **Human rejection** | Zeroes out/removes | An expert rejects an incorrect dependency |
| **Source authority** | Weights "documents"/"is_responsible_for" | Official ADR > chat thread; document signed by the owner > note |
| **Co-occurrence in materials** | Increases | Two concepts/systems often appear in the same documents |
| **Semantic similarity (Qdrant)** | Increases | High vector proximity between the linked content |
| **Frequency of use in paths** | Increases | Edge frequently traversed by new hires in the role |
| **Frequency in tutor questions** | Increases | Many questions follow that connection (real relevance) |
| **Practice / completion outcomes** | Modulates prerequisites | Failing stage Y when skill X is missing reinforces the prerequisite |
| **Freshness / decay** | Reduces over time | An unupdated and unconfirmed runbook decays; feeds `obsolete_relative_to` |

Immutability rule: the weight is not mutated in place on the relationship; each reassessment produces a new version of the value (with timestamp and contributing factors), so that it is possible to explain *why* an edge has that weight (interpretability and audit, enterprise requirements). Freshness has a special role in this domain: company knowledge ages, and an edge that decays can activate the `obsolete_relative_to` relationship and a review task for the knowledge owner.

## 6. Data Sources & Connectors (Ingestion)

| Source | Mode | Status | Notes |
|---|---|---|---|
| **Manual file upload** | Drag&drop / selection | MVP | Reuses `DocumentController.upload` and the existing pipeline |
| **Monitored local folders** | Folder watcher batch | MVP | `LocalFileSystemScanner` + Spring Batch already present |
| **People directory/org chart (CSV)** | Structured import | MVP | Populates Person/Team/Role |
| **Audio/video of sessions** | Transcription | MVP/early | `WhisperTranscriptionAdapter` existing |
| **Calendar (start dates, sessions, deadlines)** | `calendar` module | Early | Events and deadlines as Event nodes |
| **Internal emails (notices, attachments)** | `email` module (IMAP) | Early | With permissions; attachment extraction |
| **Git repository** | Plugin connector (PF4J) | Evolution | README/ADR/structure, dependencies between modules, commit for citation |
| **Company wiki (Confluence, Notion)** | Plugin connector | Evolution | Synchronization of spaces/pages with permissions |
| **Issue tracker (Jira, GitHub Issues)** | Plugin connector | Evolution | Real problems, solutions, implicit FAQs |
| **Company LMS** | Plugin connector | Evolution | Import of existing courses and completions |
| **HRIS / company directory** | Plugin connector | Evolution | Synchronization of people, roles, teams |
| **Cloud storage (Drive, SharePoint, OneDrive)** | Plugin connector | Evolution | Shared document folders |
| **Company chat (Slack/Teams export)** | Plugin connector | Evolution | Tacit knowledge in threads (with strict privacy) |

All external connectors pass through the **PF4J plugin system + marketplace**, so as not to bloat the core and to respect modularity per domain. Each connector must declare which data it reads, with which permissions, and where it ends up, consistent with local-first privacy and with the enterprise requirement of no exfiltration.

## 7. Features to Create, Develop, and Maintain (MVP → Evolution)

### 7.1 MVP (first release of the Onboarding & Training scope)

| # | Feature | What it involves (backend / frontend) | Modules touched |
|---|---|---|---|
| 1 | **`onboarding` domain (or extension of `knowledge`)** | New node/edge models specific to it (Person, Role, Skill, Path, Stage…), in/out ports, service; wiring in `DomainConfig` | domain, infrastructure |
| 2 | **Organizational structure management** | CRUD of people/teams/roles/systems; CSV import; `/api/v1/onboarding/*` controller; standalone UI feature (Signals) | api, frontend, MySQL (Flyway, one query/file) |
| 3 | **Internal knowledge ingestion with permissions** | Reuse of the Tika/OCR/Whisper pipeline + chunking + Qdrant, with provenance and visibility metadata | infrastructure, batch |
| 4 | **Entity/skill extraction & edge inference (AI)** | AI job that produces nodes and edges with confidence weight; deduplicates synonyms/jargon | domain, infrastructure (Ollama) |
| 5 | **Human-in-the-loop graph review (knowledge owner)** | API and UI to confirm/correct nodes and edges; weight update; curation task queue | api, frontend |
| 6 | **Interactive visualization of the internal graph** | Graph view with progressive expansion, filters by node/relationship type, responsibility and dependency maps | frontend |
| 7 | **Onboarding paths per role (template + instance)** | Template definition, profile-adapted instantiation, ordering by prerequisites, progress tracking, checklist | domain, api, frontend |
| 8 | **GraphRAG tutor on internal knowledge (with permissions)** | Sub-graph retrieval + Qdrant chunks filtered by visibility; answer with source citation; "I don't know / ask X"; integration with existing chat | domain (knowledge/llm), frontend (chat) |
| 9 | **Basic self-assessments and quizzes** | Quiz generation from concepts/skills; outcome recording; stage state update | domain, api, frontend |
| 10 | **Permission/visibility model on the graph** | Filter by role/team/level applied to queries and tutor | domain, infrastructure, api |
| 11 | **i18n IT/EN** | All enums (node/edge types, path states, roles) translated and routed to the frontend according to the language switch | api, frontend |

### 7.2 Evolutions (subsequent releases)

| # | Feature | Added value |
|---|---|---|
| 12 | **Fully adaptive path (knowledge tracing)** | Skill state per competency, updated from outcomes; path that skips what is already mastered |
| 13 | **Spaced repetition for compliance/critical skills** | Scheduled review of mandatory skills with reminders |
| 14 | **Company connectors (Git, Confluence, Jira, LMS, HRIS) via PF4J** | Automatic ingestion and continuous synchronization of sources |
| 15 | **Manager / HR dashboard (aggregated and anonymous analytics)** | Time-to-productivity, critical stages, documentation gaps, compliance completions |
| 16 | **Mentor/buddy matching** | Suggestion of the best reference person based on expertise in the graph |
| 17 | **Obsolete knowledge detection** | The AI flags runbooks/documents misaligned with changed systems |
| 18 | **Suggestion of gaps and missing connections** | The AI proposes missing documentation (questions without a source) and non-obvious links between teams/systems |
| 19 | **Offboarding / tacit knowledge capture** | Guided extraction of the knowledge of those leaving before it is lost |
| 20 | **Marketplace of path/graph templates** | Onboarding templates for common roles, shareable as modules |
| 21 | **Onboarding agent (orchestration via `agent`/`mcp`)** | Tutor-agent that orchestrates search, quizzes, reminders, and activities |

### 7.3 To Maintain (ongoing maintenance)

- Ingestion pipeline (Tika parsers, OCR languages, Whisper models) and plugin connectors to external APIs that change (Confluence, Jira, Git, HRIS).
- GraphRAG prompts and logic (quality of entity/relationship extraction, source citation, respect for permissions).
- Graph schema and Flyway migrations (one query per file), with backward-compatible evolution.
- Permission/visibility model, fundamental and sensitive: every new feature must respect it.
- IT/EN translations of enums and UI with every new feature.
- Tuning of the weight factors, of decay (freshness), and of the spaced-repetition algorithms based on real feedback.
- Graph hygiene: orphan nodes, duplicates, obsolete knowledge flagged by `obsolete_relative_to`.

## 8. AI / GraphRAG Use Cases

1. **Tutor grounded in internal knowledge.** "How do I deploy the Orders service to staging?" → The AI navigates the System (Orders), Procedure (deploy runbook), and prerequisite nodes, retrieves the relevant chunks, and answers by citing "Deploy runbook, Staging section" and the reference commit. All locally with Ollama, filtered by permissions.
2. **Responsibility map.** "Who is the authentication expert?" → The AI follows the `is_expert_in`/`owner_of` edges and indicates the reference person (and team), with the systems they oversee.
3. **Adaptive onboarding.** "I'm a new backend developer, where do I start?" → GraphRAG builds/retrieves the role's path, skips what the profile declares to know, orders by prerequisites, and proposes the first stage with materials and activities.
4. **Multi-hop cross-team question.** "If I change the payment format, what do I impact?" → The AI navigates the `depends_on` edges and shows the downstream systems and owner teams to involve.
5. **Identification of documentation gaps.** For HR/knowledge owners: "Which new-hire questions remain without a source?" → The AI aggregates the tutor queries lacking a grounded answer and proposes the missing documentation.
6. **Obsolete knowledge detection.** The AI flags: "Runbook R cites a variable removed from service S three months ago: likely obsolescence, do you want to open a review task?".
7. **Generation of quizzes and checklists** from the skills required by the role, with calibrated difficulty, for stage self-assessments.
8. **Structured summary of an area** following the `is_part_of` hierarchy and the prerequisites (e.g., "architecture overview for the new team"), with precise citations.
9. **Mentor/buddy suggestion** based on the proximity in the graph between the skills to acquire and the available experts.
10. **Knowledge capture during offboarding.** The AI interviews those leaving about the nodes they own that lack documentation, and proposes runbook drafts to validate.

## 9. KPIs & Success Metrics

| Category | Metric | Indicative target |
|---|---|---|
| **Onboarding effectiveness** | Time-to-productivity (days to reach full operability) | Measurable reduction vs. baseline (e.g., from 90 to 60 days for technical roles) |
| **Autonomy** | Questions resolved by the tutor without interrupting colleagues | Growing share; reduction of human tutor time |
| **Tutor quality** | % of answers rated useful and grounded (with correct citation) | ≥ 80% thumbs up |
| **Groundedness** | % of answers with at least one verifiable citation (when the source exists) | ≥ 95% |
| **Graph quality** | % of suggested edges confirmed by knowledge owners | ≥ 60% acceptance net of corrections |
| **Documentation coverage** | % of tutor questions with an available source | Growing; gaps that close over time |
| **Progress** | % of stages completed / paths finished on time | High adherence to templates |
| **Compliance** | % of mandatory modules completed by the deadline; adherence to review | ≥ 95% completion |
| **Early retention** | Retention of new hires at 6/12 months | Improvement vs. baseline |
| **Freshness** | Share of knowledge flagged obsolete and then updated | Trend toward reduction of the obsolete stock |
| **Performance** | Local tutor response latency (Ollama) | Acceptable on on-premise hardware (target a few seconds to first token) |
| **Privacy** | Data sent to the cloud without consent | Zero (a constraint, not an objective) |

## 10. Risks & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| **Obsolete knowledge in the graph** | The tutor answers with outdated information, operational harm | Freshness tracking; weight decay; `obsolete_relative_to` relationship; review tasks for knowledge owners; preference for authoritative and recent sources |
| **Noisy entity/relationship extraction** | Unreliable graph, distrust | Mandatory human-in-the-loop for knowledge owners; deduplication; confidence thresholds; regeneration |
| **Tutor hallucinations** | Wrong answers, operational errors | GraphRAG constrained to internal knowledge; mandatory citation; "not present / ask X" answer when the source is missing |
| **Permission violation** (a hire sees what they shouldn't) | Leak of internal information, compliance risk | Permissions applied at the query and tutor level; dedicated tests; restrictive default; access audit |
| **Data privacy and sovereignty** | Block to enterprise adoption | Strict local-first; self-hosting; no telemetry; encryption at rest; explicit consent for any cloud provider |
| **Local computational cost** (on-premise LLM/embedding) | Slowness, frustration | Right-sized Ollama models; background batch ingestion; caching; cloud option with consent |
| **Neglected curation** (knowledge owners don't review) | Unrefined graph, knowledge not deposited | Low-friction suggestions (one click accepts/rejects); prioritized task queue; incentives and recognition; sensible defaults |
| **Cognitive overload of the graph** (too many nodes) | Unreadable map | Progressive expansion, filters, clustering by team/area, role-focused views |
| **Reliability of MySQL+Qdrant on deep graph queries** | Slow paths/dependencies on large graphs | Targeted indexing, materialization of frequent paths, depth limits; reassess a dedicated graph only if necessary (project constraint) |
| **Resistance to change** (preference for "asking a colleague") | Low adoption | Low-friction UX integrated into the workflow; management sponsorship; demonstrate value with quick wins |
| **Multilingualism** (mixed IT/EN knowledge) | Entities not linked across languages | Multilingual embedding; cross-language synonym/jargon mapping; bilingual enums and UI |

## 11. Maintenance & Evolution

- **Incremental graph update.** Newly ingested knowledge extends the existing graph; a periodic job recomputes the candidate connections and proposes additions without destructive rebuilds.
- **Active freshness management.** Periodic routines compare the document date with changes in systems/processes and flag obsolescence (`obsolete_relative_to`), generating targeted review tasks.
- **Graph decay and hygiene.** Unconfirmed and unused edges decay; routines flag orphan nodes, duplicates, and weak connections to review.
- **Schema versioning.** Every evolution of the node/edge types passes through backward-compatible Flyway migrations (one query per file), with a documented backfill strategy.
- **Tuning of models and prompts.** Periodic update of the extraction and GraphRAG prompts, of the recommended Ollama models, and of the chunking parameters, driven by the metrics of §9.
- **Calibration of weights and spaced repetition.** The weight factors (§5.3) and the review intervals of critical skills are refined on real usage data, maintaining interpretability for the audit.
- **Permission governance.** Periodic review of the visibility model as roles and teams evolve; every new feature must pass the no-information-leak tests.
- **Connector compatibility.** Monitoring of external APIs (Git, Confluence, Jira, LMS, HRIS) and updating of the corresponding PF4J plugins.
- **Bilingual documentation.** Every feature updates IT/EN documentation and the logs in `Sviluppi/` according to project conventions.
- **Evaluation roadmap.** Introduce over time an evaluation set (golden questions per role/area) to measure regressions in the quality of the tutor and of the extraction.

## 12. Integration with Existing LocalMind Modules

| Existing module | Role in the Onboarding & Training scope |
|---|---|
| **knowledge** | Base of the graph engine: extension with onboarding node/edge types; the natural point to graft the domain |
| **document** | Internal knowledge ingestion (upload, Tika, Tesseract OCR), chunking, provenance and visibility metadata |
| **llm** | Tutor and entity/relationship extraction via `LlmGatewayService`; Ollama default, optional cloud fallback with consent |
| **(Qdrant) vectorstore** | Semantic index of chunks for GraphRAG retrieval, with permission metadata |
| **batch** | Ingestion jobs and folder watcher; periodic graph-recomputation and obsolescence-detection jobs |
| **calendar** | Start dates, training sessions, and compliance deadlines as Event nodes; path and review scheduling |
| **email** | Ingestion of internal notices and attachments (with permissions); sending of onboarding/compliance reminders |
| **messaging** | Notifications and reminders on company channels (stages, deadlines, review) |
| **mcp** | Exposure of tools (query the graph, generate quizzes, find the expert) to external agents |
| **agent** | Onboarding agent that orchestrates graph search, quizzes, reminders, and practical activities |
| **automation** | Automatic triggers: "new hire created → instantiate path," "compliance deadline in 7 days → review," "new document → extract entities" |
| **marketplace + plugin (PF4J)** | Git/Confluence/Jira/LMS/HRIS/Drive connectors and path/graph templates shareable as installable modules |
| **finetuning** | Possible adaptation of local models to company lexicon and processes (advanced) |
| **auth** | Local-first identity and, above all, the basis for the graph's permission/visibility model |
| **common** | Domain events ("knowledge ingested," "stage completed," "skill acquired"), aggregated and anonymous analytics, error handling |
| **Frontend (Angular 21)** | New standalone `onboarding` feature with Signal store, interactive graph view, tutor, paths and checklists, manager/HR dashboards; i18n IT/EN |

The Internal Onboarding & Training scope is therefore an **enterprise vertical** of the universal engine: it entirely reuses the existing infrastructure (ingestion, embedding, LLM, graph, plugins, automation, calendar, email) and adds only the node/relationship types, the path features, and the user experience specific to onboarding and training — consistent with the "one platform, multiple ecosystems" principle, remaining local-first, free, private (non-negotiable enterprise privacy), auditable, and bilingual.

---

### Research Sources & References

- D2L — *LMS Platforms for Effective Employee Onboarding in 2026*: https://www.d2l.com/blog/lms-for-employee-onboarding/
- 360Learning — *The 10 Best Employee Onboarding LMS Solutions for 2026*: https://360learning.com/blog/employee-onboarding-lms/
- Kairntech — *Employee Onboarding AI: The Complete Guide for 2026* (RAG on knowledge base, source-backed answers): https://kairntech.com/blog/articles/employee-onboarding-ai-the-complete-guide-for-2026/
- Enboarder — *AI Onboarding Tools 2026* / *Onboarding Trends 2026*: https://enboarder.com/blog/ai-onboarding-tool-guide-2026/ , https://enboarder.com/blog/future-onboarding-trends/
- Medium (Tongbing) — *GraphRAG in 2026: A Practical Buyer's Guide to Knowledge-Graph–Augmented RAG* (multi-hop, cross-document, structured grounding): https://medium.com/@tongbing00/graphrag-in-2026-a-practical-buyers-guide-to-knowledge-graph-augmented-rag-43e5e72d522d
- MDPI Electronics — *Personalized Learning Path Recommendation Based on Knowledge Graphs: A Survey*: https://www.mdpi.com/2079-9292/15/1/238
- arXiv 2506.22303 / AAAI — *GraphRAG-Induced Dual Knowledge Structure Graphs for Personalized Learning Path Recommendation* (prerequisite and similarity relationships): https://arxiv.org/abs/2506.22303
- CGS Immersive — *Measure Onboarding With Time to Productivity*: https://cgsimmersive.com/blog/measure-onboarding-effectiveness-with-employee-time-to-productivity
- AllenComm — *Successful Onboarding: Time-to-Productivity + Early Performance Signals*: https://www.allencomm.com/2026/04/successful-onboarding-time-to-productivity-early-performance-signals/
- AIHR — *Employee Onboarding Statistics & Trends 2026* (retention +52%, productivity +60%, +58% retention at 3 years): https://www.aihr.com/blog/employee-onboarding-statistics/
- Phenom — *15 Onboarding Trends for 2026: AI, Skills & New Hire Success*: https://www.phenom.com/blog/onboarding-trends-ai-skills
- KMSlh — *Top Knowledge Management Tools for Onboarding in 2026*: https://kmslh.com/blog/knowledge-management-software-for-employee-onboarding/
