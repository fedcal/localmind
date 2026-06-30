export const content = `# Community & moderation

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This document describes the extension scope **Community & moderation**, belonging to the **core group** of LocalMind. It is a cross-cutting, enabling scope: it is not a domain vertical (tourism, events, enterprise…) but the **social and content governance system** that allows the universal Knowledge Graph to be fed, evaluated, and curated by a plurality of contributors without degrading in quality. Where the *Knowledge Graph Engine* (document 20) defines *how* weighted nodes and edges are persisted and navigated, this scope defines *who* creates them, *with what reliability*, *how the best content emerges*, and *how the graph is defended against spam, manipulation, and harmful content*.

The scope covers four tightly interconnected pillars, explicitly indicated in the focus: **user contributions**, **evaluations**, **emergent ranking**, **anti-spam and content moderation/curation**. These four pillars are the engine of the "community-driven" promise of the consumer vision ("Wikipedia of places") and, at the same time, the governance and quality safeguard required in the enterprise space (curation of internal knowledge, merge approval, relationship validation). A single social system, parameterized per domain, exactly like the underlying graph engine.

Everything remains **local-first**, self-hostable, with **Ollama AI by default** for automatic moderation, **reusing MySQL 8.0 and Qdrant** (no new datastores), with **bilingual IT/EN** UI/enum/documentation, extensibility via **PF4J plugins**, and **Flyway migrations with a single query per file**.

---

## 1. What we solve (problem & value)

### 1.1 The underlying problem

A knowledge graph fed by the community or by heterogeneous enterprise sources is powerful but inherently **fragile**: its usefulness depends entirely on the quality, reliability, and timeliness of the content flowing into it. Without a community and moderation system, any collaborative platform collapses due to recurring and well-documented causes:

- **The "anyone can contribute" vs "can it be trusted" dilemma.** The strength of a community-driven graph (the "Wikipedia of places") is the freedom of contribution; its weakness is that every unverified contribution is a potential vector for error, vandalism, or spam. Without a mechanism that distinguishes the reliable contribution from the noisy one, the graph fills up with duplicate nodes, fake reviews, invented relationships, and ghost POIs.
- **Spam, bots, and coordinated attacks.** Open platforms are a systematic target for commercial spam, link injection, fake review farms, sockpuppets (multiple accounts of the same actor), and coordinated "raids"/spam-waves. In a graph, these attacks degrade not just a single page but **poison the relationships**: a spam node with many false edges pollutes the traversals and the ranking of the entire neighborhood.
- **Ranking manipulation.** If the best content "emerges" from votes and contributions, then the voting system itself becomes a target: multiple voting, brigading (coordinated voting), reciprocity (groups that vote for each other), artificially inflated reputation. A naive ranking (one vote = one point) is trivially manipulable.
- **Harmful content and liability.** UGC (user-generated content) also means illegal, defamatory content, hate speech, exposed personal data. A self-hosted platform offloads onto the administrator the responsibility of moderating; without integrated tools, moderation is unmanageable manually even at modest volumes.
- **Enterprise curation without governance.** In the enterprise the problem is reversed: there is no anonymous spam, but there is the risk of **obsolete, contradictory, or unauthorized** knowledge. Who approves that an "architectural decision" becomes official? Who validates that a \`DEPENDS_ON\` relationship extracted by the AI is correct before it feeds a critical impact analysis? A flow of **review, approval, and attribution** with an audit trail is needed.
- **Opacity and loss of trust.** If users do not understand *why* a piece of content was removed, *why* their contribution is in the queue, or *why* a POI is at the top of the ranking, they lose trust and leave. Transparency (explanations, audit trail, lightweight appeals) is today a requirement, not an extra.
- **Non-scalable and non-sovereign moderation.** Relying on cloud trust & safety services contradicts the local-first principle and enterprise privacy. Automatic moderation must be able to run **locally**, with the Ollama AI.

### 1.2 The solution: a social layer of trust, quality, and governance on top of the graph

LocalMind responds with a **community & moderation layer** that natively grafts onto the graph engine and governs its feeding through four cooperating mechanisms:

1. **Tracked and attributed contributions.** Every node, edge, review, or modification originates from a **contribution** with author, timestamp, source, and status (draft → in review → published → rejected/withdrawn). The contribution is the atomic unit of governance: nothing enters the "published" graph without passing through this model.
2. **Reputation-weighted evaluations.** Users express evaluations (votes, reviews, confirmations/denials of relationships, reports). Each evaluation counts in proportion to the **reputation** of the evaluator, not uniformly: this neutralizes multiple voting and rewards historically reliable contributors — the central pattern of collaborative reputation systems.
3. **Transparent emergent ranking.** The best content rises through the **weighted aggregation** of evaluations, reputation, freshness, and quality signals — not through raw popularity. The ranking directly feeds the **edge weights** of the graph (see document 20, §5.4), closing the loop: the community improves the GraphRAG.
4. **Hybrid anti-spam and moderation (AI + human).** A moderation pipeline combines **local automatic filters** (Ollama for content classification, anti-spam heuristics on identity/behavior) as the first line, and **human review** (curators/moderators) for the gray areas. All with an audit trail, configurable thresholds, appeals, and review queues.

### 1.3 Why a single system for consumer and enterprise

| Dimension | Without community/moderation | With the LocalMind layer |
|---|---|---|
| Who feeds the graph | Only automatic ingestion or admin | Community + connectors + curators, all tracked |
| Reliability of a contribution | Indistinct | Weighted by the author's reputation |
| How content emerges | Raw popularity or insertion order | Weighted and explainable emergent ranking |
| Defense against spam/manipulation | Absent | Hybrid anti-spam + weighted voting + rate limiting |
| Enterprise governance | No approval flow | Review, approval, audit, attribution |
| Transparency | Opaque decisions | Explanations, audit trail, appeals |
| Sovereignty | Dependence on cloud T&S | Local AI moderation (Ollama) |

The value is twofold and symmetrical:

- **Consumer (tourism, events, culture, sport, dining…):** truly enables the "Wikipedia of places." Local people contribute POIs, reviews, itineraries; the best ones emerge on merit; commercial spam and fake reviews are filtered; municipalities and tourist boards retain data sovereignty and a curation panel.
- **Enterprise (docs, processes, repos, people, decisions…):** transforms the graph into a **governed knowledge base**. Internal knowledge has an owner, is approved, validated, dated, and audited; relationships extracted by the AI pass through human confirmation before becoming "facts" on which impact analysis is performed; internal reputation reflects expertise (who curates what).

### 1.4 Relationship with the Core Value

LocalMind's Core Value (\`.planning/PROJECT.md\`) is that *the AI navigates a weighted graph to answer complex questions and surface non-obvious connections, in any domain, while remaining local-first*. Community & moderation is the **guarantor of the graph's quality**: without it, the edge weights would be noisy and the GraphRAG unreliable. It is the "user feedback" factor of the edge weight (document 20, §5.4) turned into a complete system, and the safeguard that prevents spam and manipulation from corrupting the traversals.

---

## 2. Personas & target users

| Persona | Profile | Objectives with respect to the scope | Needs from the system |
|---|---|---|---|
| **Community contributor (consumer)** | Citizen/enthusiast who adds POIs, events, reviews | Share local knowledge, see their contribution recognized | Simple contribution form, attribution, badge/reputation, status feedback |
| **Reader/voter** | User who consults and evaluates | Find reliable content, vote, report abuse | Transparent ranking, one-click vote/report, ranking explanation |
| **Curator / moderator** | Volunteer or staff (tourist board, municipality, editorial team) | Validate, correct, remove, approve contributions | Review queue, bulk tools, audit trail, clear policies, AI suggestions |
| **Knowledge curator (enterprise)** | Employee who owns an area of knowledge | Approve official knowledge, validate AI relationships | Approval flow, owner attribution, versioning, compliance audit |
| **Administrator / DevOps** | Manages the self-hosted instance | Configure policies, thresholds, rate limits; sustainability | Policy panel, anti-spam configuration, metrics, local moderation |
| **Trust & Safety lead** | Responsible for content quality/safety | Define guidelines, manage escalations and coordinated attacks | Moderation dashboard, rules, spam-wave detection, reporting |
| **Reported / sanctioned user** | Contributor subject to a moderation action | Understand the decision, file an appeal | Notification with reason, lightweight appeal flow, transparency |
| **AI / moderation agent** | Local LLM classifier + heuristics | Filter in the first line, prioritize the queue | Classification tool (Ollama), thresholds, budget, structured formats |
| **AI / GraphRAG (downstream consumer)** | Consumes the curated graph | Use reliable content and clean weights | "Published" status, weight from reputation, exclusion of content under review/removed |

Primary personas of the scope: the **Community contributor** (consumer), the **Curator/moderator**, and the **Enterprise knowledge curator**. Success is measured by how easy and rewarding it is to contribute well and how efficiently it is to moderate the bad.

---

## 3. Input requirements

This section defines in detail **what is needed as input** to the community & moderation system: data, configurations, contracts, policies, and constraints. It is deliberately exhaustive because it is the contract on which every subsequent development depends. It relies on the contracts of the Knowledge Graph Engine (document 20, §3) for nodes and edges, and extends them with the social dimension.

### 3.1 Policy and governance input (founding configuration per domain)

Before accepting contributions, a domain/instance must **declare its own rules**. This is the founding input, analogous to the modular schema of the graph engine.

| Input | Description | Expected form | Mandatory |
|---|---|---|---|
| **Community guidelines** | What is acceptable/unacceptable, with examples, bilingual IT/EN | Document + machine-readable rules (forbidden categories) | Yes |
| **Moderation workflow** | Pre-moderation (approve before publishing) vs post-moderation (publish then check) vs reactive (only on report), per content type/domain | \`ModerationPolicy\` configuration | Yes |
| **Auto-approval thresholds** | Minimum reputation to publish without review; AI classifier confidence threshold for auto-actions | Numbers/thresholds per domain | Yes (with defaults) |
| **Reputation policy** | Events that assign/subtract reputation and their weights; levels/badges; decay | \`ReputationPolicy\` configuration | Yes (with defaults) |
| **Anti-spam policy** | Rate limits per role, identity/behavior rules, lists (allow/deny), spam-wave thresholds | \`AntiSpamPolicy\` configuration | Yes (with defaults) |
| **Roles and permissions** | Role→capability map (contribute, vote, moderate, approve, configure) | RBAC extended on existing \`auth\` | Yes |
| **Evaluation scale** | Allowed evaluation types (vote ±1, stars 1–5, relationship confirm/deny, report) per node type | Evaluation catalog per domain | Yes |

Constraint: all enums (contribution status, moderation outcome, report type, reputation level, evaluation type) must be **bilingual IT/EN** and redirected to the frontend according to the language switch (project rule). Policies must not require recompilation of the core: they are to be managed as data/configuration or as PF4J extensions.

### 3.2 Content input (the contributions)

These are the data produced by users that the system must accept, validate, and route.

| Contribution type | Examples | Source | Notes |
|---|---|---|---|
| **Node creation/modification** | New POI, event, FAQ, decision | UI + graph engine CRUD API | Passes through the moderation workflow |
| **Edge creation/modification** | \`NEAR\`, \`PAIRED_WITH\`, \`DEPENDS_ON\` relationship | UI + API | Human confirmation/denial → weight factor |
| **Review / free text** | Review of a venue, comment | UI | Subject to content classification |
| **Evaluation** | Vote, stars, "useful/not useful", relationship confirmation | One-click UI | Weighted by reputation |
| **Report (report/flag)** | "Spam", "offensive", "incorrect", "personal data" | One-click UI | Feeds the moderation queue |
| **Correction suggestion** | Proposed edit on someone else's content | UI | Wiki-style, with history |
| **Appeal** | Contestation of a moderation action | UI | Dedicated review flow |

For each contribution at least the following are required: **author identity** (user authenticated via \`auth\`), **timestamp**, **type**, **payload** (reference to node/edge or text), **domain/context**, and — where applicable — **source** and **language**.

### 3.3 Input for submitting a contribution (API contract)

To submit a contribution the caller must provide:

- \`authorId\` — **mandatory**, authenticated user (from \`LocalAuthFilter\`/\`auth\`).
- \`contributionType\` — **mandatory** (CREATE_NODE, UPDATE_NODE, CREATE_RELATION, REVIEW, VOTE, REPORT, EDIT_SUGGESTION, APPEAL), validated.
- \`targetRef\` — reference to the subject: existing \`nodeId\`/\`relationId\`, or new node/edge payload conforming to the graph engine schema (document 20, §3.3/§3.4).
- \`payload\` — content specific to the type (review text, vote value, report reason, node properties).
- \`domain\` / context — to apply the correct policy.
- \`language\` — IT/EN for free text (for classification and i18n).
- *(optional)* \`sourceConnectorId\`/\`sourceDocumentId\` — if the contribution derives from a source (traceability).

Minimum boundary validations (project security rule): author authenticated and authorized for the \`contributionType\`; payload conforming to the schema; lengths respected; no trusted input; rate limit not exceeded (see §3.6); idempotency (a user cannot vote twice on the same target — vote upsert).

### 3.4 Input for an evaluation (API contract)

- \`evaluatorId\` — **mandatory**, authenticated; must have the permission and minimum reputation to vote.
- \`targetRef\` — node, edge, or contribution being evaluated — **mandatory**, existing.
- \`evaluationType\` — **mandatory** (UPVOTE/DOWNVOTE, STAR 1–5, CONFIRM_RELATION/REJECT_RELATION, HELPFUL/NOT_HELPFUL).
- \`value\` — value conforming to the scale declared for that target/domain.
- *(derived, not in input)* \`weight\` of the evaluation = function of the evaluator's reputation at the time of the vote (computed server-side, never provided by the client to avoid manipulation).

Validations: only one active vote per (evaluator, target, type) — modification instead of duplication; no self-evaluation of one's own contribution (basic anti-reciprocity); reputation ≥ threshold; rate limit.

### 3.5 Input for moderation (API contract and queue)

Every item entering moderation (by workflow or by report) generates a **moderation case** with:

- \`caseId\`, \`targetRef\`, \`reason\` (category: SPAM, OFFENSIVE, WRONG, PRIVACY, OTHER), \`reporterId\` (if from a report) or \`system\` (if from AI).
- \`aiAssessment\` — outcome of the local classifier: category, \`confidence ∈ [0,1]\`, textual reasoning (for explainability).
- \`priority\` — derived from category severity + reporter reputation + volume of reports on the same target.
- \`status\` — OPEN → IN_REVIEW → RESOLVED(ACTION) → (possibly) APPEALED.

For the **moderator decision**, the following are needed: \`caseId\`, \`decision\` (APPROVE, REJECT, EDIT, HIDE, DELETE, WARN_USER, BAN_USER, NO_ACTION), \`note\` (mandatory for punitive actions, for audit), \`moderatorId\`. The decision **must** produce an immutable audit record.

### 3.6 Anti-spam input (signals and rules)

The anti-spam pipeline consumes signals on **identity** and **behavior** (2026 best practice):

| Signal | Examples | Use |
|---|---|---|
| **Identity** | Account age, verified email, reputation, history | New/unverified accounts → mandatory pre-moderation |
| **Behavior** | Contribution frequency, bursts, similarity between contributions, voting patterns | Bot, spam-wave, brigading detection |
| **Content** | Presence of links, commercial keywords, duplication, language | Local AI classification (Ollama) |
| **Network/relationship** | Accounts that vote for each other, suspicious clusters in the graph | Sockpuppet/reciprocity detection |
| **Lists** | Allowlist (trusted bots/sources), denylist (domains/users) | Fast bypass or block |

Required configuration: rate limit per role/action (e.g., N contributions/hour, M votes/hour), burst thresholds, similarity threshold for duplicates, confidence threshold for auto-hide, time window for spam-wave detection.

### 3.7 Configuration and environment input

- **Moderation LLM profile:** Ollama provider (default), classification model, low temperature, budget — reusing \`LlmGatewayService\` and the fallback chain (cloud opt-in).
- **MySQL datasource + Flyway** (one query per file) for contributions, evaluations, reputation, moderation cases, audit.
- **Qdrant** for similarity between contributions/reviews (duplicate and spam farm detection).
- **\`auth\` integration** for identity, roles, and permissions (RBAC).
- **Event infra integration** (\`DomainEventPublisherPort\`) to propagate outcomes to edge weights and statistics.
- **Language** (IT/EN) for UI, notifications, reasons, guidelines.

### 3.8 Constraints and pre-conditions (non-functional)

- **Local-first / self-hostable:** automatic moderation runs with Ollama; no mandatory dependency on cloud trust & safety services.
- **Privacy:** personal data in contributions managed according to policy; in enterprise, content does not leave the instance without consent.
- **Hexagonal architecture:** all inputs pass through \`port/in\`; no framework logic in the domain; new pure services wired in \`DomainConfig\`.
- **MySQL+Qdrant reuse:** no new datastore.
- **i18n and immutability:** bilingual enums; immutable audit records (project immutability pattern).
- **Security:** boundary validation, rate limiting on all endpoints, no leak of sensitive data in error messages (project security rules).

---

## 4. Activity flow (step-by-step)

The system has five macro flows: **(A) submission and moderation of a contribution**, **(B) evaluation and emergent ranking**, **(C) anti-spam pipeline**, **(D) reputation**, **(E) enterprise curation / approval**. They are described in detail because they constitute the observable behavior of the system.

### 4.1 Flow A — Submission and moderation of a contribution

\`\`\`
Contribution → Boundary validation → Anti-spam (gate) → AI classification (Ollama) →
Workflow decision (auto-publish | queue | auto-reject) → [Human review] →
State persistence → Effects on graph/reputation → Author notification
\`\`\`

1. **Submission.** The authenticated user submits a contribution (new POI, review, relationship…) via UI → \`port/in\`. The system validates at the boundaries (author, type, payload, lengths, permissions).
2. **Anti-spam gate (Flow C inline).** Rate limits and identity/behavior signals are checked. If the gate triggers (e.g., burst from a new account) → the contribution is marked \`HELD\` and routed to the queue with high priority, or rejected if on the denylist. No silent rejection: the author receives a reason.
3. **Local AI classification.** The text/content is passed to the Ollama classifier: category (ok, spam, offensive, personal data…), \`confidence\`, and reasoning. For nodes/edges the Qdrant similarity with the existing ones is also computed (duplicates).
4. **Workflow decision.** Based on the domain's \`ModerationPolicy\` and the author's reputation:
   - **Auto-publication**: author above the reputation threshold **and** high "clean" AI confidence → immediate \`PUBLISHED\` status (post-moderation: remains subject to subsequent reporting).
   - **Review queue**: gray area (intermediate confidence, author not yet trusted, domain in pre-moderation) → \`IN_REVIEW\` status, inserted into the queue with priority.
   - **Auto-reject/hide**: high AI confidence on a forbidden category → \`REJECTED\`/\`HIDDEN\` status with reason (appealable).
5. **Human review (if in queue).** The moderator sees the case with the AI suggestion, decides (APPROVE/EDIT/REJECT/HIDE…), adds a note. The decision generates an **immutable audit**.
6. **Persistence and effects.**
   - The contribution's status is persisted (MySQL).
   - If published: the node/edge becomes visible to the graph and the GraphRAG; confirmed edges feed the **weight feedback factor** (document 20, §5.4) via an event.
   - The author's reputation is updated (Flow D): + for an accepted contribution, − for rejection/spam.
7. **Notification and transparency.** The author receives the outcome with a reason and, if negative, the link to the **appeal**.

**Error handling:** classifier unavailable → fallback to the human queue (never blind auto-publication); constraint violation → clear message; concurrent edit conflict → optimistic strategy with versioning. No swallowed errors (project rule).

### 4.2 Flow B — Evaluation and emergent ranking

\`\`\`
Evaluation → Validation + evaluator reputation → Vote weight →
Weighted aggregation → Target quality score → Ranking update + edge weight
\`\`\`

1. **Evaluation.** The user expresses a vote/stars/confirmation on a target. Validation: permission, reputation ≥ threshold, no self-voting, one active vote per (user, target, type).
2. **Vote weight.** The system computes \`voteWeight = f(evaluator_reputation)\` server-side. A vote from an expert curator weighs more than that of a just-created account — neutralizes multiple voting and brigading.
3. **Aggregation.** The target's **quality score** is a **weighted** average/aggregation of the votes, combined with auxiliary signals: number of distinct evaluators (diversity), freshness, confirmation/denial rate for relationships.
4. **Emergent ranking.** Targets are ordered by quality score (not by raw popularity). The ranking is **explainable**: the UI shows "at the top because: high consensus of reliable curators, recent, confirmed N times".
5. **Closing the loop on the graph.** The score feeds the **weights of related edges**: a heavily confirmed relationship gains weight; a repeatedly denied one loses it. Thus the community directly improves traversals and the GraphRAG.
6. **Continuous anti-manipulation.** Anomalous voting patterns (reciprocal clusters, bursts) are sent to Flow C; suspicious votes are discounted or frozen pending review.

### 4.3 Flow C — Anti-spam pipeline

\`\`\`
Signals (identity + behavior + content + network) → Risk scoring →
Graduated action (allow | challenge | hold | block) → [Spam-wave detection] → Audit
\`\`\`

1. **Signal collection.** For each contribution/vote, identity signals (account age, verification, reputation), behavior (frequency, bursts, similarity), content (links, keywords, duplicates via Qdrant), and network (suspicious clusters in the voting graph) are collected.
2. **Risk scoring.** A combined score (heuristics + Ollama classifier) estimates the probability of spam/abuse.
3. **Graduated action.** Based on the risk and the policy: *allow* (low), *challenge* (medium: request verification/captcha-like), *hold* (high: review queue), *block* (denylist/clear violation). The actions are **proportionate** and configurable.
4. **Coordinated attack detection (spam-wave).** A time window monitors anomalous spikes (many similar contributions, from related accounts, on the same target/area). When the threshold is exceeded, an **area response** kicks in: temporarily reinforced rate limit, mass hold, alert to the Trust & Safety lead.
5. **Learning and lists.** Trusted bots/sources on the allowlist (whitelist good bots — 2026 best practice); confirmed malicious actors on the denylist. Outcomes feed the audit and refine the thresholds.

### 4.4 Flow D — Reputation (the trust engine)

\`\`\`
Event (accepted/rejected contribution, vote received, confirmed report) →
Reputation delta (weighted) → Temporal decay → Level/badge → Permissions/thresholds
\`\`\`

1. **Reputation events.** Every relevant outcome generates an event: published contribution (+), review voted useful (+), confirmed relationship (+), contribution rejected as spam (−), one's own report confirmed (+), one's own report unfounded (−).
2. **Delta computation.** Each event has a configurable weight (\`ReputationPolicy\`). Reputation is a **weighted aggregation**, not a simple count: events validated by reliable curators weigh more (transitive trust, in line with the literature on reputation systems).
3. **Temporal decay.** Reputation decays slowly over time (configurable half-life) to reward recent activity and avoid entrenched positions.
4. **Levels and badges.** Reputation thresholds unlock levels/badges (e.g., *Novice → Contributor → Trusted curator*) and capabilities (auto-publication, full-weight voting, access to the moderation queue). Bilingual IT/EN.
5. **Downstream effects.** Reputation feeds the vote weights (Flow B), the auto-approval thresholds (Flow A), and the anti-spam gate (Flow C). It is the connector between the four pillars.

### 4.5 Flow E — Enterprise curation / approval

\`\`\`
Candidate knowledge (AI-extracted or contributed) → Owner assignment →
Review/approval → Versioning → "Official" in the graph → Compliance audit
\`\`\`

1. **Candidacy.** A relationship extracted by the AI (e.g., \`DEPENDS_ON\` between microservices) or a contribution enters as a **candidate** not yet official.
2. **Owner and routing.** The system routes the candidate to the **owner** of the knowledge area (by domain/node type), leveraging internal reputation/expertise.
3. **Approval/validation.** The owner confirms, modifies, or rejects. The confirmation of an AI relationship is exactly the "human feedback" that raises the edge weight and promotes it to a reliable fact for impact analysis.
4. **Versioning and audit.** Each modification produces a new version with author, timestamp, reason: complete history for compliance (who approved what and when).
5. **Governed publication.** Only approved candidates are \`OFFICIAL\` and used with full weight by the enterprise GraphRAG; the others remain \`DRAFT\`/\`CANDIDATE\`, excluded or downweighted.

### 4.6 Cross-cutting flow — Appeal and transparency

Every negative moderation action is **appealable**: the user opens an appeal with a reason; the case returns to the queue with priority and (ideally) to a different moderator; the final decision is tracked. The UI always exposes the **status** of the contribution, the **reason** for the actions, and the **explanation** of the ranking, building trust (2026 best practice: explanations + audit trail + lightweight appeals).

---

## 5. Graph model (node types, relationship types, weighting criteria)

### 5.1 Principle: the social is (also) graph

Community & moderation is not just relational tables: its main objects (users, contributions, evaluations) are **first-class nodes** of the Knowledge Graph and connect to the domain nodes. This allows the GraphRAG to reason also about provenance and reliability ("who contributed this POI? with what reputation?") and enables sockpuppet/reciprocity detection as a graph pattern. The engine core is reused (document 20), adding the types of this scope through the extensible registry / PF4J plugins.

### 5.2 Node types of the scope

| NodeType | Description | Notes |
|---|---|---|
| \`USER\` / \`CONTRIBUTOR\` | User/contributor with reputation | Linked to \`auth\`; carries the reputation score |
| \`CONTRIBUTION\` | Act of contribution (creation/modification) with status | Unit of governance; links author→target |
| \`REVIEW\` | Review/free text on a target | Subject to classification and votes |
| \`EVALUATION\`/\`VOTE\` | Weighted evaluation | Links evaluator→target with value and weight |
| \`REPORT\`/\`FLAG\` | Report | Feeds moderation |
| \`MODERATION_CASE\` | Moderation case | Status, priority, outcome |
| \`MODERATION_ACTION\` | Moderation action (audit) | Immutable, traceable |
| \`BADGE\`/\`REPUTATION_LEVEL\` | Level/recognition | Bilingual IT/EN |
| \`CURATOR\`/\`OWNER\` (enterprise) | Owner of a knowledge area | Candidate routing |

### 5.3 Relationship types of the scope

| RelationType | Semantics | Directed |
|---|---|---|
| \`CONTRIBUTED\` | User → contribution/node/edge | Yes |
| \`AUTHORED\` | User → review | Yes |
| \`EVALUATED\` | User → target (with value/weight) | Yes |
| \`CONFIRMED\` / \`REJECTED\` | User → edge/relationship (confirmation/denial) | Yes |
| \`REPORTED\` | User → target (report) | Yes |
| \`MODERATED\` | Moderator → case/target (action) | Yes |
| \`OWNS\` (enterprise) | Owner → area/node type | Yes |
| \`TRUSTS\` / \`ENDORSES\` | User → user (transitive reputation) | Yes |
| \`SUSPECTED_SOCKPUPPET\` | User ↔ user (anomalous cluster, derived) | No |

The social relationships (\`EVALUATED\`, \`CONFIRMED\`, \`TRUSTS\`) are what close the loop with the domain edge weights.

### 5.4 Weighting criteria — how the social weighs the graph

The system produces **two** families of weight, both in \`[0,1]\`, normalized and explainable:

**(a) Domain edge weight (contribution to the engine algorithm, document 20 §5.4).** The community layer feeds in particular:

| Factor | What it measures | Where it comes from |
|---|---|---|
| **User feedback** | Human validation of the relationship | Confirmations/denials (\`CONFIRMED\`/\`REJECTED\`) weighted by reputation |
| **Consensus** | Agreement among distinct evaluators | Number and diversity of evaluators (anti-brigading) |
| **Source reliability** | Who contributed | Reputation of the contributor/owner |
| **Recency** | Timeliness | Temporal decay of votes and confirmations |

**(b) Internal social weights/scores** (not on the domain graph but on the scope's objects):

| Score | Reference formula | Use |
|---|---|---|
| **Vote weight** | \`w_vote = clamp01(g(evaluator_reputation))\` | Non-manipulable vote aggregation |
| **Target quality score** | weighted average of votes + diversity + freshness | Emergent ranking |
| **User reputation** | \`Σ (event_coeff · event_delta) · decay\`, with transitive trust | Thresholds, vote weight, anti-spam gate |
| **Spam risk** | combination of identity/behavior/content/network signals | Graduated action |

Key principle (best practice and literature): **votes weigh as a function of the voter's reputation**, reputation is a weighted aggregation with decay, and every weight is **explainable** (the factors are persisted, not just the result) to show "why" in the UI. Schema implications: tables \`contributions\`, \`evaluations\` (with derived \`weight\`), \`user_reputation\` (with factors), \`moderation_cases\`, \`moderation_actions\` (immutable audit); columns for the factors; additive Flyway migrations, **one query per file**.

### 5.5 Physical representation (MySQL + Qdrant)

- **MySQL** = state and governance: contributions, evaluations, reputation, moderation cases and actions (audit), versioning. UUID with \`@JdbcTypeCode(SqlTypes.CHAR)\`. Beware of reserved words (\`timestamp\`) to be escaped with backticks in the DDL.
- **Qdrant** = similarity: embeddings of textual reviews/contributions for **duplicate and spam farm detection** (nearly identical contributions from different accounts) and entity resolution of contributions.
- **Immutable audit**: the \`moderation_actions\` are append-only (immutability pattern).

---

## 6. Data sources & connectors (ingestion)

The community layer is fed both by direct human inputs and by system signals; the connectors are natural PF4J extension points.

| Source | Connector | Existing reuse | Output |
|---|---|---|---|
| **UI contributions/votes/reports** | Community CRUD API | Angular frontend + \`auth\` | \`CONTRIBUTION\`/\`REVIEW\`/\`VOTE\`/\`REPORT\` nodes, social edges |
| **System events** | Listener on \`DomainEventPublisherPort\` | Existing event infra | Usage frequency, reputation triggers |
| **Content classifier** | AI moderation adapter | \`LlmGatewayService\` (Ollama) | \`aiAssessment\` on contributions |
| **Contribution similarity** | Qdrant adapter | \`QdrantVectorStoreAdapter\` | Duplicate/spam farm detection |
| **Identity/roles** | \`auth\` integration | Existing RBAC extended | Anti-spam identity signals, permissions |
| **External moderation (opt-in)** | PF4J plugin (denylist, domain blocklist) | Marketplace | Lists, additional signals |
| **Candidate knowledge (enterprise)** | Graph engine AI extraction | \`LlmEntityExtractorAdapter\` | Candidates → owner approval queue |

Principles: every contribution is **traceable** (author + timestamp + source), the flow is **idempotent** (one vote = one updatable record), cloud moderation sources are **opt-in** (privacy, local-first). Connectors register as adapters of the \`port/out\`, keeping the domain pure.

---

## 7. Features to create, develop, and maintain (MVP → evolution)

Legend: **[C]** create from scratch, **[E]** extend/evolve the existing, **[M]** maintain/hardening.

### 7.1 MVP (indispensable foundations)

| # | Feature | Type | Implementation notes |
|---|---|---|---|
| 1 | **Contribution model + states** | [C] | \`community\` domain (pure, wired in \`DomainConfig\`): \`Contribution\`, states, \`port/in\`/\`port/out\` |
| 2 | **Configurable moderation workflow** | [C] | Pre/post/reactive per domain (\`ModerationPolicy\`); auto-approval thresholds |
| 3 | **Evaluations (vote/stars/confirmation)** | [C] | \`Evaluation\` with one active vote per (user,target,type), no self-voting |
| 4 | **Base reputation + weighted voting** | [C] | \`ReputationPolicy\`, delta per event, vote weight = f(reputation) |
| 5 | **Emergent ranking** | [C] | Weighted quality score; ordering + explanation |
| 6 | **Reports + moderation queue** | [C] | \`Report\`, \`ModerationCase\`, priority, states |
| 7 | **Local AI moderation (first line)** | [E] | Classifier adapter on \`LlmGatewayService\` (Ollama), confidence + reasoning |
| 8 | **Base anti-spam (rate limit + identity)** | [C] | Rate limit per role/action; gate on new/unverified accounts |
| 9 | **Immutable audit trail** | [C] | \`ModerationAction\` append-only; mandatory note on punitive actions |
| 10 | **Integration with graph edge weights** | [E] | Confirmations/denials → feedback factor (document 20) via event |
| 11 | **Extended RBAC (community roles)** | [E] | Contribute/vote/moderate/approve on \`auth\` |
| 12 | **REST API + bilingual DTOs** | [C] | \`/api/v1/community/*\`: contributions, votes, reports, cases; enum IT/EN |
| 13 | **UI: contribution, vote, report, moderation queue** | [C] | Angular feature \`community\`/\`moderation\`, Signals, i18n |
| 14 | **Flyway migrations** | [C] | Community/moderation/reputation tables; one query per file |

### 7.2 Evolution (post-MVP)

| # | Feature | Type | Value |
|---|---|---|---|
| 15 | **Duplicate/spam farm detection (Qdrant)** | [E] | Contribution similarity → spam farm and fake reviews |
| 16 | **Spam-wave / coordinated attacks** | [C] | Time window, area response, T&S alert |
| 17 | **Sockpuppet/reciprocity detection (graph)** | [C] | Anomalous voting clusters as a graph pattern |
| 18 | **Transitive reputation + decay** | [E] | Trust weighted by validator's reputation; half-life |
| 19 | **Badges, levels, gamification** | [C] | Engagement; capability unlocking; bilingual |
| 20 | **Appeal flow** | [C] | Transparency, different moderator, audit |
| 21 | **Enterprise curation / owner approval** | [C] | Candidates → owner → \`OFFICIAL\`; versioning |
| 22 | **Contribution versioning/history (wiki-style)** | [C] | Suggested edits, rollback, compliance |
| 23 | **Moderation dashboard + reporting** | [C] | KPIs, queues, trends, AI vs human outcomes |
| 24 | **Moderation/list connectors (PF4J)** | [C] | Domain denylist, blocklist, opt-in integrations |
| 25 | **Multi-channel notifications** | [E] | Outcomes/appeals via \`messaging\`/\`email\` domain |

### 7.3 Continuous maintenance

- **[M]** Continuous tuning of anti-spam thresholds and reputation coefficients based on metrics (false positives/negatives).
- **[M]** AI moderation quality: monitor AI↔human agreement, recalibrate Ollama prompts/models.
- **[M]** Consistency of the IT/EN enum catalogs as domains/policies vary.
- **[M]** Queue and ranking performance as volumes grow (indexes, caching of hot rankings via the already-present Caffeine).
- **[M]** Reputation health: prevent inflation/entrenchment, verify decay.
- **[M]** Tests: unit (pure domain), integration (Testcontainers MySQL), coverage ≥80%; IT/EN documentation + log in \`Sviluppi/\`.

---

## 8. AI / GraphRAG use cases

| Use case | Domain | Role of the AI / graph | Output |
|---|---|---|---|
| **First-line content classification** | Cross-cutting | Ollama classifies contributions (spam/offensive/privacy) with confidence | Auto-action or queue priority + reasoning |
| **Duplicate / fake review detection** | Consumer | Qdrant similarity between reviews/contributions | Suspicious clusters → moderation |
| **Sockpuppet / brigading as a graph pattern** | Cross-cutting | Traversal on \`EVALUATED\`/\`TRUSTS\` for reciprocal clusters | Discounted votes + alert |
| **Moderation queue summarization** | Cross-cutting | LLM summarizes and prioritizes open cases | Assisted triage for the moderator |
| **Ranking explanation** | Consumer | GraphRAG cites the factors (consensus, reputation, freshness) | Transparent "at the top because…" |
| **Moderation action suggestion** | Cross-cutting | LLM proposes a decision + applicable policy | Draft decision (human confirms) |
| **AI relationship validation (enterprise)** | Enterprise | Owner confirms candidate edges → full weight | Tracked official knowledge |
| **Reliability in GraphRAG answers** | Cross-cutting | The graph includes provenance and reputation | Answers that weigh reliable sources and cite contributors |

All cases run with **Ollama by default**, produce **explainable and auditable** outcomes, and keep the **human in the loop** on delicate decisions (hybrid moderation, 2026 best practice).

---

## 9. KPIs & success metrics

| Category | KPI | Indicative target |
|---|---|---|
| **Participation** | No. of active contributors, contributions/user, return rate | Steady growth of the community |
| **Contribution quality** | % contributions published vs rejected, % fake reviews intercepted | High acceptance of genuine contributions |
| **Moderation effectiveness** | Average time in queue, % cases resolved, AI↔human agreement | Queue cleared quickly; high agreement |
| **AI accuracy** | Classifier false positives/negatives | Low FP (no unfair censorship), low FN (no spam) |
| **Anti-spam** | % spam blocked, spam-waves intercepted, spam published (leakage) | Minimal leakage, coordinated attacks neutralized |
| **Ranking fairness** | Resistance to brigading (test), score↔real quality correlation | Ranking robust to manipulation |
| **Reputation** | Reputation distribution, mobility (newcomers rising) | No entrenched positions; meritocracy |
| **Transparency** | % actions with a reason, appeal rate, % appeals upheld | ~100% with reasons; appeals handled fairly |
| **Impact on the graph** | % edges with human feedback, GraphRAG quality improvement | More reliable weights, better answers |
| **Local-first / privacy** | % moderation executed locally; data sent to cloud without consent | 100% local possible; 0 unauthorized transmissions |

### 9.1 Instrumentation

Reuse of Actuator + Micrometer/Prometheus for queue latency, moderation throughput, AI rates, ranking caching hit/miss; Grafana dashboard (already present in the monitoring profile).

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| **Spam farm / fake reviews** | Polluted graph, distorted ranking | Qdrant similarity, reputation-weighted voting, rate limit, spam-wave detection |
| **Ranking manipulation (brigading, reciprocity)** | Poor content at the top | Weighted voting, evaluator diversity, reciprocal cluster detection, discounting of suspicious votes |
| **Sockpuppet (multiple accounts)** | Inflated reputation and votes | Identity signals, verification, graph patterns, thresholds for new accounts |
| **AI moderation false positives** | Unfair censorship, loss of trust | Human in the loop on gray areas, appeals, threshold calibration, reasons |
| **False negatives (spam gets through)** | Quality degradation | Post-moderation + community reports + continuous tuning |
| **Moderator overload** | Unmanageable queue | AI triage, priority, bulk actions, high-confidence auto-actions |
| **Reputation inflation/entrenchment** | Distorted meritocracy | Temporal decay, weighted aggregation, coefficient review |
| **Illegal content / personal data** | Legal liability | PRIVACY/illegal classification, fast removal, audit, clear guidelines |
| **Cultural/linguistic bias of the classifier** | Unfair moderation IT vs EN | Per-language models/prompts, cultural fluency, human review |
| **Dependence on cloud T&S** | Violation of local-first/privacy | Local Ollama moderation by default; cloud opt-in |
| **Opacity of decisions** | Loss of trust | Explanations, audit trail, lightweight appeals (2026 best practice) |
| **Monolithic Flyway migrations** | Violation of the project rule | One query per file, always |

---

## 11. Maintenance & evolution

- **Data-driven tuning:** anti-spam thresholds, reputation coefficients, and moderation prompts must be reviewed periodically based on FP/FN and AI↔human agreement. Moderation is a living system, not a static one.
- **Policy evolution:** each domain/instance can evolve \`ModerationPolicy\`, \`ReputationPolicy\`, \`AntiSpamPolicy\` without touching the core; the enums remain bilingual and backward-compatible.
- **Reputation governance:** calibrate decay per domain, monitor the distribution to avoid oligarchies of curators; introduce peer review for the high levels.
- **Extensibility via plugins:** classifiers, lists, moderation connectors, and new evaluation types live as PF4J extensions and marketplace modules, tested in isolation.
- **Consistency with the graph engine:** keep the "feedback/consensus/reliability" weight factors aligned with the algorithm of document 20; treat the internal scores as recomputable derivatives.
- **Audit and compliance:** moderation actions and enterprise versioning are append-only; preserve and make the history queryable.
- **Tests and coverage:** unit on the pure domain, integration with Testcontainers MySQL, ranking robustness tests (brigading simulations); coverage ≥80%.
- **Documentation:** constant IT/EN updates with every development, bilingual public guidelines, dated log in \`Sviluppi/\` (project rules).
- **Scalability:** if local moderation becomes a bottleneck on modest hardware, evaluate asynchronous batching, lighter Ollama models, and aggressive caching before considering cloud opt-in options.

---

## 12. Integration with existing LocalMind modules

| Module / domain | Role with respect to the scope | Concrete integration |
|---|---|---|
| **\`knowledge\` (Graph Engine)** | Substrate of the curated content | Contributions create/modify nodes/edges; confirmations feed the weight (document 20) |
| **New \`community\` domain** | Heart of the scope (to be created) | Models, \`port/in\`/\`out\`, pure services wired in \`DomainConfig\` |
| **\`auth\`** | Identity, roles, permissions, identity signals | Extended RBAC (contribute/vote/moderate/approve); account age/verification |
| **\`llm\` / \`LlmGatewayService\`** | First-line AI moderation | Content classifier with Ollama default + opt-in fallback |
| **Qdrant (\`vectorstore\`)** | Contribution similarity | Duplicate detection, spam farm, review entity resolution |
| **MySQL + Flyway** | State, reputation, audit | Community/moderation/reputation tables; additive one-query migrations |
| **Event infra (\`DomainEventPublisherPort\`)** | Outcome propagation | Confirmations→edge weights, contributions→reputation, statistics |
| **\`messaging\` / \`email\`** | Notifications | Moderation outcomes, appeals, badges to users |
| **\`agent\`** | Assisted moderation/triage | Agent that summarizes the queue and proposes actions |
| **\`mcp\`** | Tools and federation | External lists/classifiers opt-in as tools |
| **\`automation\`** | Rules and triggers | Auto-actions, escalation, spam-wave response |
| **\`marketplace\` / \`plugin\` (PF4J)** | Extensibility | Installable classifiers, lists, connectors, evaluation types |
| **\`common\`** | Events, exceptions, analytics | Moderation/reputation events, typed exceptions, statistics |
| **\`finetuning\`** | Moderation improvement | Dataset of cases (with human outcome) to refine the local classifier |
| **Angular frontend** | Social and moderation UI | Feature \`community\`/\`moderation\`: contribution, vote, report, queue, dashboard, appeal; Signals; i18n IT/EN |

**Architectural wiring:** the new \`community\` domain (and the moderation/reputation services) remain **pure** (zero Spring) and are registered as \`@Bean\` in \`DomainConfig.java\`; the adapters (MySQL, Qdrant, LLM classifier, lists) are \`@Component\` classes that implement the \`port/out\`; the controllers expose only the \`port/in\`. This preserves the hexagonal architecture and enables the future extraction to a microservice documented in \`MODULE_BOUNDARIES.md\`.

---

*Guidance document for the developments of the Community & moderation scope (core group). To be kept aligned with \`.planning/PROJECT.md\`, the files in \`.planning/codebase/\`, and document 20 (Knowledge Graph Engine).*
`;
