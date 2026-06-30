# Mail & Communications

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This domain belongs to the **enterprise group** of LocalMind's universal knowledge graph engine. While the consumer verticals (tourism, events, experiences) answer the question *"what can I do here?"*, the **Mail & communications** domain answers one of the most expensive questions in the life of an organization: *"who knows what, who decided what, with whom, when and why?"*. Email and corporate communication channels are the **richest and least structured reservoir of tacit knowledge** of any enterprise: they contain decisions, commitments, deadlines, relationships between people, project context, operational attachments and the historical memory of "how we got here". Today all of this is held prisoner in individual mailboxes, endless threads and keyword searches that do not understand relationships.

LocalMind transforms this flow into a **weighted graph of communications-people-topics**, navigable by the AI in GraphRAG mode, **without a single email leaving the self-hosted instance**. This is the differentiating point compared to Gmail, Outlook/Copilot, Superhuman or the various cloud "AI email assistants": the knowledge extracted from communications — by definition the most sensitive and regulated data of a company — remains sovereign, on-premise, with local Ollama AI as the default. Ingestion happens via **IMAP** (the universal standard, already present in the codebase through Eclipse Angus Mail in the `email` domain), reusing MySQL for the graph structure and Qdrant for semantics, without introducing any dedicated graph database.

---

## 1. What we solve (problem & value)

### 1.1 The concrete problem

Email remains, in 2026, the *de facto* operating system of corporate collaboration — especially toward the outside (customers, suppliers, consultants, public administration) where internal chats do not reach. Yet it is a tool designed in the 1980s for message delivery, not for knowledge management. This results in chronic and quantifiable problems:

- **Knowledge buried in individual mailboxes (silos).** The memory of a negotiation, an architectural decision, an agreement with a supplier or the history of a customer lives in the mailbox of one or two people. When that person is absent, changes role or leaves the company, the knowledge vanishes. There is no overall view: each mailbox is an island.
- **Search that does not understand relationships.** The native search of mail clients is based on keywords and sender/subject. It does not answer questions like *"what is the status of all the commitments we made with customer Rossi on the Alfa project?"*, *"who is the right person to contact for the invoicing of supplier Beta?"*, *"what decisions were made via email about the contract renewal and by whom?"*. These are problems of **multi-hop reasoning** on a graph, not full-text search — and it is precisely here that GraphRAG shows measured accuracy leaps (in the literature, from ~23% to ~87% on multi-hop tasks compared to traditional RAG).
- **Information overload (email overload).** The average knowledge worker spends hours a day reading, sorting, searching and reconstructing the context of emails. Much of this work is manual reconstruction of a context that the system could provide automatically.
- **Fragmented and unreadable threads.** Conversations break across multiple threads, with forks, cc's coming in and out, nested quotes, out-of-order replies. Reconstructing "what really happened" requires re-reading dozens of messages and quoting.
- **Lost commitments and deadlines.** Promises ("I'll send you the quote by Friday"), requests, action items and agreed dates remain in the body of the text, never extracted in a structured way, never monitored. They translate into dropped balls and unhappy customers.
- **Attachments as a black hole.** Contracts, offers, invoices, specifications, minutes travel as attachments. The "good" version of a document is "the one Tizio sent me in that March email" — untraceable and disconnected from the rest of the corporate knowledge base.
- **Slow onboarding and operational continuity.** A new hire or a substitute has no way to quickly absorb the history of relationships and dossiers. The "handover" is a manual, partial and subjective act.
- **Invisible relationship map.** No one has an objective view of *who communicates with whom*, *with what intensity*, *on which topics* — valuable information for understanding who oversees a customer, where there are human single points of failure, which teams really collaborate.

### 1.2 The LocalMind solution

LocalMind ingests corporate mailboxes via IMAP and builds a **weighted knowledge graph** in which the nodes are **people**, **messages**, **threads/conversations**, **topics/subjects**, **organizations**, **attachments/documents**, **commitments/deadlines** and **decisions**, and the edges represent weighted relationships: a person *wrote* a message, a message *belongs to* a thread, a thread *deals with* a topic, two people *communicate with each other* with a certain intensity, a message *contains* a commitment with a deadline, an attachment *is cited in* multiple threads. On this graph the AI operates in **GraphRAG** mode, combining semantic search (Qdrant, already used for documents) with relationship navigation (MySQL), to answer complex questions and surface non-obvious connections — *citing the messages, threads and people* used as sources.

The differential value compared to existing tools:

| Capability | Mail client / native search | Cloud email assistants (Copilot, Gemini, Superhuman) | LocalMind (graph + GraphRAG) |
|----------|----------------------------------|------------------------------------------------------|------------------------------|
| Data model | Isolated messages | Messages + summaries | Weighted people-communications-topics graph |
| Multi-hop questions | No | Limited to the single account | Yes, traversing people/threads/topics/attachments |
| Commitment/decision extraction | No | Partial | Monitorable structured nodes |
| Attachments as knowledge | Disconnected | Disconnected | Nodes linked to the document KB |
| Answer explainability | — | Often opaque | Citation of messages/threads/paths |
| Data sovereignty | Depends on the provider | **Data sent to the vendor's cloud** | **Local-first, local Ollama AI** |
| Privacy/compliance | Delegated to the provider | Critical for GDPR | Full on-premise control |

### 1.3 The value for stakeholders

- **For the knowledge worker / account manager:** a "second brain" that remembers every interaction with every customer/supplier, prepares the context before a call, flags upcoming commitments and answers "what is the status of…" in natural language.
- **For teams and managers:** an overall view of dossiers (customers, suppliers, projects) independent of individual mailboxes; operational continuity when a person is absent; the ability to understand where knowledge is concentrated in a few people (bus factor risk).
- **For the company:** tacit knowledge becomes a persistent and queryable organizational asset, no longer tied to individuals; faster onboarding; lower risk of forgotten commitments; historical memory of decisions.
- **For the IT/security manager and the DPO:** a solution that, unlike cloud competitors, **does not exfiltrate data**; the entire processing (entity extraction, embedding, LLM inference) happens on controlled infrastructure, with native tools for minimization, retention and the right to be forgotten — fundamental for GDPR and for data covered by confidentiality.

### 1.4 Boundaries of responsibility (what it is NOT)

To avoid ambiguity, regulatory risks and usage drift, the domain is also defined by exclusion:

- **It is not a replacement mail client.** LocalMind does not replace Outlook/Thunderbird/webmail for writing and managing daily mail; it is a *read* knowledge layer (with possible assisted drafting of replies, never automatic sending without confirmation).
- **It is not an employee surveillance tool.** The communications graph serves to recover knowledge, not to monitor people. Governance (section 10) imposes perimeters, consents and explicit purposes; individual monitoring for disciplinary purposes is out of scope and must be excluded by policy.
- **It does not send mail autonomously.** Any outgoing action (reply, forward) requires explicit human confirmation; the MVP is read/ingestion only.
- **It is not a legally-binding archiving system with evidentiary value.** It can store and cite messages, but it does not replace compliant retention / certified journaling systems.

---

## 2. Personas & target users

| Persona | Description | Primary objective | Key need |
|---------|-------------|--------------------|-----------------|
| **Account / sales manager** | Manages customer relationships via email | Always have the complete customer context | View per person/organization, open commitments |
| **Project manager** | Coordinates projects that live largely via email | Status of decisions, action items, blockers | Project timeline, deadlines, responsible parties |
| **Procurement / supplier manager** | Communicates with suppliers and public administration | History of orders, offers, contracts | Supplier dossier, linked contractual attachments |
| **Founder / executive** | Many relationships, little time | Prepare for calls and decisions quickly | Concise briefing with citations |
| **Generic knowledge worker** | Suffers from email overload | Quickly find "that email/decision" | Multi-hop conversational search |
| **New hire / substitute** | Must absorb dossiers and relationships | Quick onboarding on a customer/project | Historical summaries, map of people |
| **Assistant / secretary** | Manages shared mailboxes | Sort, reconstruct context, drafts | Shared mailboxes, commitments, delegations |
| **IT manager / instance administrator** | Configures ingestion and access | Self-hosting, IMAP connectors, security | Account configuration, perimeters, audit |
| **DPO / compliance officer** | Oversees privacy and purposes | Minimization, retention, forgetting | Granular controls, logs, consents |

Operational distinction that drives the roles of the `auth` domain:
- **End users** query the graph in read mode, **limited to the perimeter they are entitled to** (their own mailbox, the shared mailboxes/dossiers assigned to them). Respecting the visibility perimeter is a security requirement, not an option.
- **Instance administrator** configures IMAP accounts, ingestion rules, perimeters and retention.
- **DPO/compliance** has access to audit logs, minimization policies and deletion tools, but not necessarily to the content.

---

## 3. Input requirements

This section defines in detail **what must enter the system** so that the communications graph is useful, accurate, secure and maintainable. We distinguish **connection/configuration** inputs, **domain** inputs (the raw email data and what is extracted from it), **end-user** inputs (the request) and **governance/privacy** inputs. Given the extremely sensitive nature of the data, privacy requirements are not an appendix but a first-level input.

### 3.1 Connection and configuration inputs (IMAP account)

For each mailbox to be ingested the administrator must be able to provide:

| Category | Minimum fields (MVP) | Extended fields (evolution) |
|-----------|--------------------|---------------------------|
| Connection | IMAP host, port, TLS/STARTTLS, username | provider profile (Gmail/Microsoft 365/generic), timeout |
| Authentication | application password / credential | **OAuth2/XOAUTH2** (Google, Microsoft), token refresh |
| Mailbox identity | email address, descriptive label, owner(s) | type (personal, shared, functional info@/sales@) |
| Ingestion perimeter | folders to include/exclude (e.g. only INBOX+Sent), start date | filters by sender/domain, exclusion of lists/newsletters |
| Scheduling | synchronization frequency | time window, incremental vs full mode |
| Default privacy | PII redaction level, consent to processing | per-mailbox rules, exclusion of sensitive domains |

Key technical requirements of IMAP ingestion:
- **Credentials never in clear text in the DB**: at-rest encryption, preferably with OAuth2 support for the main providers (so that passwords are not managed).
- **Incremental and idempotent synchronization** based on `UID`/`UIDVALIDITY` per folder, so as not to reprocess the entire mailbox on every run and to correctly handle moves/deletions.
- **Resilience**: handling of disconnections, server rate limits, very large mailboxes (hundreds of thousands of messages) through pagination and resumption from checkpoint.

### 3.2 Domain inputs (the single email and its decomposition)

Each IMAP message is decomposed into its RFC 5322/MIME elements and enriched. The `EmailMessage` model currently present in the codebase (`id, from, to, subject, body, receivedAt, read`) is the minimum starting point and must be **extended** to feed the graph:

| Element | Minimum fields (MVP) | Extended fields (evolution) |
|----------|--------------------|---------------------------|
| Identity header | `Message-ID`, `In-Reply-To`, `References` (for threading), `Date` | `Return-Path`, authentication headers (SPF/DKIM) |
| Participants | `From`, `To`, `Cc` | `Bcc` (only if in Sent), `Reply-To`, distribution list |
| Content | subject, textual body (de-quoted), detected language | normalized HTML body, signature separated from content |
| Attachments | file name, MIME type, size, hash | extracted text (Tika/OCR), versioning, preview |
| Mailbox metadata | folder, read flag, direction (in/out) | labels, importance, native thread ID |
| AI enrichments | entities (people/org/topics), message summary | sentiment/tone, language, classification (request/FYI/decision) |
| Structured extractions | commitments/action items with deadline, open questions | decisions made, references to documents/tickets |

Processing required on the message body before insertion into the graph:
- **De-quoting / quote separation**: separate the *new* text from the quoted text (the nested `>`, the "On … wrote:" blocks), so as not to duplicate content and to correctly attribute the author of each fragment.
- **Removal of repetitive signatures and disclaimers** (noise that pollutes embedding and extraction).
- **HTML→text normalization** and handling of multipart mail.
- **Language detection** (IT/EN and beyond) to correctly direct prompts and embeddings.

### 3.3 End-user inputs (the conversational request)

The user queries the graph in natural language. The system must accept and handle:
- **Retrieval questions**: "find the email in which customer Rossi confirmed the price to us".
- **Synthesis/status questions**: "summarize everything that happened with the Alfa project in the last two weeks".
- **Relational (multi-hop) questions**: "who, besides me, is in contact with supplier Beta and on which topics?".
- **Commitment/deadline questions**: "what promises have I made via email and not yet kept?".
- **Preparation requests**: "prepare me a briefing before tomorrow's call with Gamma".
- **Assisted composition requests** (evolution): "write a draft reply to this last email taking into account the history" — always with human confirmation, never automatic sending.

Each request carries with it an **authorization context** (who is asking, on which perimeter they are entitled): it is a mandatory input that filters the accessible nodes upstream.

### 3.4 Governance and privacy inputs (first-level)

Processing personal and potentially sensitive data, the following are mandatory inputs:
- **Consent and purpose**: for which mailboxes/people ingestion is active and with what declared purpose.
- **Minimization/redaction policies**: which categories of PII to redact before any sending to an LLM (especially if cloud), which domains/senders to exclude a priori (e.g. union, health, personal communications).
- **Retention and forgetting**: how long to keep messages and enrichments, automatic deletion rules, handling of deletion requests on a person/address.
- **Visibility perimeters**: user → accessible mailboxes/dossiers mapping.
- **LLM policy**: whether the use of cloud providers is allowed for this domain or whether Ollama-only is imposed (recommended default for communications).

---

## 4. Activity flow (step-by-step)

The flow is articulated in two major pipelines: the **ingestion pipeline** (batch/asynchronous, from mailbox to graph) and the **query pipeline** (synchronous, from question to GraphRAG answer). Both reuse components already present in LocalMind.

### 4.1 Ingestion pipeline (from IMAP to the graph)

**Step 0 — Configuration and consent.** The administrator registers the IMAP account (section 3.1), defines the perimeter, scheduling and privacy policy. Credentials are encrypted at-rest; the purpose of processing is recorded.

**Step 1 — Connection and incremental discovery.** The scheduler (reuse of the `localmind-batch` pattern + `@EnableScheduling`) starts a job that connects via IMAP (Angus Mail), reads `UIDVALIDITY` and the last processed `UID` per folder and downloads **only new/modified messages** (idempotent incremental synchronization). Disconnections and rate limits are handled with resumption from checkpoint.

**Step 2 — MIME parsing and decomposition.** Each message is decomposed: headers (including `Message-ID`, `In-Reply-To`, `References`), participants, body (multipart), attachments. HTML is normalized to text, the language is detected.

**Step 3 — Content cleaning.** De-quoting of the citation, removal of signatures/disclaimers, separation of new text from quoted text. This step is critical for the quality of embedding and extraction.

**Step 4 — Attachment extraction.** Each attachment is saved, hashed (deduplication), and its text extracted **reusing the existing document pipeline** (`DocumentIngestionPipelineService`, Tika, OCR Tesseract). The attachment thus becomes a **first-class document** in the KB, linked to the message and the thread.

**Step 5 — AI enrichment (local by default).** Through the `LlmGatewayService` with the **Ollama** provider locally (respecting the domain's LLM policy):
- **NER / entity extraction**: people, organizations, topics/subjects, projects, products, places.
- **Summary** of the message and, at a higher level, of the thread.
- **Structured extraction** of commitments/action items with deadline, open questions, decisions made, message classification (request / FYI / decision / reminder).
- Possible tone/sentiment for the evolutions.

**Step 6 — Entity resolution.** A decisive step for the quality of the graph: the same individual can appear as `mario.rossi@acme.com`, `m.rossi@acme.com`, "Mario Rossi", "Mario" in the signature. The same applies to organizations (by email domain). The system reconciles the aliases into a single **Person**/**Organization** node (matching on address, domain, display name, signature). In the literature this phase significantly improves the accuracy of queries (industry references speak of +34%): it is an investment, not a detail.

**Step 7 — Thread reconstruction.** Messages are linked into **conversations** using `In-Reply-To`/`References` (and, as a fallback, subject normalization + temporal proximity + set of participants). Forks, merges and out-of-order replies are handled, producing a tree/DAG of the conversation.

**Step 8 — Graph construction/update.** Nodes (Person, Message, Thread, Topic, Organization, Attachment, Commitment, Decision) and weighted edges (section 5) are created/updated in MySQL; the textual contents (de-quoted body, summaries, attachment text) are **chunked and embedded on Qdrant** for semantic search. The edge weights are calculated/updated (frequency, recency, relevance).

**Step 9 — Application of privacy policies.** Before persistence, PII redaction (where applicable), exclusion of sensitive domains/senders and tagging of confidentiality levels are applied. The ingestion audit is recorded.

**Step 10 — Indexing and availability.** The updated graph is now queryable. A domain event (`DomainEventPublisherPort`) is emitted for side-effects (dashboard update, notifications of new commitments/deadlines).

```text
[IMAP account]
   │  (incremental, UID/UIDVALIDITY)
   ▼
[Fetch & parse MIME] → [De-quote/clean] → [Attachments → document pipeline (Tika/OCR → Qdrant)]
   │                                                  │
   ▼                                                  ▼
[Ollama AI enrichment: NER, summaries,          [Entity resolution people/org]
 commitments, decisions, classification]             │
   │                                                  ▼
   └──────────────► [Thread reconstruction] ──► [Graph construction: nodes+weighted edges]
                                                      │  (MySQL structure + Qdrant semantics)
                                                      ▼
                                          [Privacy/redaction/retention + audit]
                                                      ▼
                                              [Queryable graph]
```

### 4.2 Query pipeline (GraphRAG)

**Step 1 — Question + authorization context.** The user asks a question in the chat; the system attaches the visibility perimeter (which mailboxes/dossiers they can see). This filter is applied **before** retrieval.

**Step 2 — Understanding and planning.** The LLM interprets the intent (retrieval / synthesis / relational / commitments / preparation) and identifies the anchor entities (people, organizations, topics, time interval).

**Step 3 — Hybrid retrieval.** In parallel: (a) **semantic search** on Qdrant over the relevant contents (filtered by perimeter); (b) **graph navigation** on MySQL starting from the anchor nodes (neighbors, paths, subgraphs: e.g. all the threads that connect person X to organization Y on topic Z).

**Step 4 — Multi-hop expansion and ranking.** The retrieved subgraph is expanded along the most heavily weighted edges (recency, frequency, relevance) within a hop budget, and the candidates (messages, summaries, commitments) are ranked by relevance and weight.

**Step 5 — Synthesis with citations.** The selected context (always within the perimeter) is passed to the LLM (Ollama by default) which generates the answer **citing the messages/threads/people** used, with a link to the original message and dates.

**Step 6 — Optional action (with confirmation).** If the request calls for it (e.g. "prepare a draft reply for me"), the system proposes a draft through the `email`/`agent` domain, which the user reviews and sends manually. No automatic sending.

**Step 7 — Feedback.** The user can validate/correct the answer; the feedback feeds the edge weights and the reputation of the extractions (improvement loop).

---

## 5. Graph model (node types, relationship types, weighting criteria)

The model reuses the framework of the universal graph engine (typed nodes + weighted edges on MySQL, semantics on Qdrant) and specializes it for the communications domain.

### 5.1 Node types

| Node type | Description | Main attributes |
|--------------|-------------|----------------------|
| **Person** | Individual who communicates (internal or external) | canonical name, aliases/addresses, organization, role (inferred) |
| **Organization** | Company/entity, derived from email domains | name, domain(s), type (customer/supplier/PA/internal) |
| **Mailbox / Account** | Ingested mailbox | address, type (personal/shared/functional), owner |
| **Message** | Single email | message-id, subject, date, direction, language, summary |
| **Thread / Conversation** | Set of linked messages | normalized subject, participants, interval, summary |
| **Topic / Subject** | Recurring concept (project, product, case) | label, synonyms, description |
| **Attachment / Document** | Attached file (linked to the document KB) | name, MIME, hash, extracted text, version |
| **Commitment / Action item** | Promise/task extracted from the text | description, responsible party, deadline, status |
| **Decision** | Decision made in a conversation | description, date, participants, outcome |
| **Event/Deadline** | Relevant date (bridge with the `calendar` domain) | type, date, link to the message/commitment |

### 5.2 Relationship types (edges)

| Relationship (edge) | From → To | Meaning |
|------------------|--------|-------------|
| `HA_SCRITTO` | Person → Message | author of the message |
| `DESTINATARIO_DI` (to/cc) | Person → Message | participation as recipient (different weight for To vs Cc) |
| `APPARTIENE_A` | Message → Thread | the message is part of the conversation |
| `RISPONDE_A` | Message → Message | `In-Reply-To`/`References` chain |
| `COMUNICA_CON` | Person ↔ Person | aggregated communication relationship (weighted) |
| `TRATTA` | Thread/Message → Topic | the conversation concerns a subject |
| `APPARTIENE_A_ORG` | Person → Organization | affiliation (by email domain) |
| `COINVOLGE_ORG` | Thread → Organization | the conversation involves an external company |
| `CONTIENE_ALLEGATO` | Message → Attachment | attachment carried by the message |
| `CITA_DOCUMENTO` | Message/Thread → Document | reference to a document in the KB |
| `CONTIENE_IMPEGNO` | Message → Commitment | extracted action item |
| `RESPONSABILE_DI` | Person → Commitment | who must fulfill it |
| `HA_DECISO` | Thread/Person → Decision | decision made |
| `SCADE_IL` | Commitment → Event/Deadline | temporal link (`calendar` domain) |
| `SIMILE_A` | Message ↔ Message / Topic ↔ Topic | semantic proximity (from Qdrant) |

### 5.3 Criteria for edge weighting

The weight is the heart of the graph: it determines what the AI considers most relevant during multi-hop expansion. For this domain the main factors:

| Factor | Applies to | Logic |
|---------|--------------|--------|
| **Frequency** | `COMUNICA_CON`, `TRATTA`, `APPARTIENE_A_ORG` | more messages exchanged → stronger edge |
| **Recency (time decay)** | almost all | recent communications weigh more than old ones (exponential decay) |
| **Directionality/role** | `DESTINATARIO_DI` | To weighs more than Cc; sender more than passive recipient |
| **Reciprocity** | `COMUNICA_CON` | bidirectional exchange (back-and-forth) weighs more than one-way sending alone |
| **Semantic relevance** | `TRATTA`, `SIMILE_A`, `CITA_DOCUMENTO` | embedding similarity between content and topic/document |
| **Thread density** | `APPARTIENE_A` | long and well-attended threads indicate hot topics |
| **Extraction confidence** | `CONTIENE_IMPEGNO`, `HA_DECISO`, NER | how confident the LLM is of the extracted entity/commitment |
| **User feedback** | all | user confirmations/corrections reinforce or weaken the edge |
| **Participant importance** | `COMUNICA_CON` | communications with key decision-makers/customers weigh more (configurable) |

The weights are **recalculated incrementally** at each ingestion and periodically normalized; time-decay imposes a scheduled reassessment so as not to let now-inactive relationships "fossilize".

---

## 6. Data sources & connectors (ingestion)

| Source | Protocol / mechanism | Status in the codebase | Notes |
|-------|-------------------------|--------------------|------|
| **Corporate email mailboxes** | IMAP (Eclipse Angus Mail) | **Existing** (`email` domain, `EmailPort`/`EmailService`) | Primary MVP source; UID-based incremental sync |
| **Gmail / Google Workspace** | IMAP + **OAuth2/XOAUTH2** | To be extended | Avoids application passwords; respects API limits |
| **Microsoft 365 / Exchange** | IMAP + OAuth2 (or Graph in the future) | To be extended | Graph API as an evolution for rich metadata |
| **Attachments** | Document pipeline (Tika, OCR) | **Existing** (`DocumentIngestionPipelineService`) | Direct reuse: attachment → KB document |
| **Shared / functional mailboxes** (info@, sales@) | IMAP | To be configured | Multiple perimeters and ownership |
| **Messaging channels** | `messaging` domain connectors | **Existing** (Slack/Telegram/etc.) | Evolution: unify mail + chat communications in the graph |
| **Calendar** | `calendar` domain | **Existing** | Commitments/deadlines ↔ calendar events |
| **`.mbox`/`.eml`/PST export files** | File import | To be created | One-time/offline historical ingestion |
| **Third-party plugins** | PF4J extension point | Existing (framework) | New extension point for communication connectors |

The architecture exposes ingestion as a **port out** of the domain (e.g. extension of `EmailPort`) with adapters in infrastructure, so that connectors (OAuth2, Graph, mbox, messaging) can be added without touching the domain logic, consistently with the hexagonal architecture. A new **PF4J extension point** (`CommunicationSourceExtension`) enables community connectors.

---

## 7. Features to create, develop and maintain (MVP → evolution)

Legend: **CREATE** = does not exist; **DEVELOP** = extends something existing; **MAINTAIN** = reuse with maintenance.

### 7.1 MVP (foundations useful right away)

| # | Feature | Type | Module(s) involved |
|---|--------------|------|--------------------|
| 1 | IMAP connector with incremental sync (UID/UIDVALIDITY), encrypted credentials | DEVELOP | `email` (port/adapter), infrastructure, `batch` |
| 2 | Extension of the `EmailMessage` model (Message-ID, In-Reply-To, References, Cc, attachments, language) | DEVELOP | `email` domain, persistence, Flyway |
| 3 | MIME parsing + de-quoting + signature removal | CREATE | infrastructure (adapter) |
| 4 | Thread reconstruction (References/In-Reply-To + subject/time/participant fallback) | CREATE | `email`/`knowledge` service |
| 5 | Attachment extraction via document pipeline (Tika/OCR → Qdrant) | MAINTAIN/DEVELOP | `document`, `vectorstore` |
| 6 | Local AI enrichment: NER, message/thread summary, classification | CREATE | `llm` (Ollama), `knowledge` |
| 7 | Entity resolution of people/organizations | CREATE | `knowledge` service |
| 8 | Communications graph schema (nodes/edges/weights) on MySQL | CREATE | `knowledge`, Flyway (one query/file) |
| 9 | Content embedding on Qdrant with perimeter filters | DEVELOP | `vectorstore` |
| 10 | Base edge weights (frequency, recency, directionality) | CREATE | `knowledge` service |
| 11 | REST API: IMAP account management, ingestion status, graph query | CREATE/DEVELOP | `localmind-api` (`email`, `knowledge`) |
| 12 | GraphRAG over communications with cited answers | DEVELOP | `knowledge`/`llm`, chat |
| 13 | UI: account configuration, person/organization dossier, chat over the graph | CREATE | frontend (`email`, `knowledge`, `chat`) |
| 14 | Commitment/deadline extraction with "to-do from emails" view | CREATE | `knowledge`, `calendar` |
| 15 | Base privacy controls: visibility perimeters, Ollama-only policy per domain | CREATE | `auth`, `email`, config |

### 7.2 Evolution (after the MVP)

| # | Feature | Type | Notes |
|---|--------------|------|------|
| 16 | OAuth2/XOAUTH2 for Gmail and Microsoft 365 | DEVELOP | removes application passwords |
| 17 | Microsoft Graph / native APIs for rich metadata | CREATE | beyond IMAP |
| 18 | Historical import `.mbox`/`.eml`/PST | CREATE | onboarding of legacy archives |
| 19 | Decision extraction and project timeline | DEVELOP | Decision nodes + temporal views |
| 20 | Sentiment/tone and relationship risk signals | CREATE | early warning on customers |
| 21 | Interactive visualization of the communications graph | DEVELOP | reuse of the universal graph visualizer |
| 22 | Assisted composition of reply drafts (with confirmation) | DEVELOP | `email`/`agent`, never automatic sending |
| 23 | Unification of mail + `messaging` channels + `calendar` in the graph | DEVELOP | omni-channel communication graph |
| 24 | Suggestion of missing links / experts by topic | DEVELOP | "who knows about X?" |
| 25 | Advanced PII redaction and anonymization proxy for cloud LLM | CREATE | enables opt-in cloud use safely |
| 26 | Automated retention/forgetting + per-person deletion workflow | CREATE | GDPR compliance |
| 27 | Proactive notifications (upcoming commitments, forgotten follow-ups) | DEVELOP | `automation` |
| 28 | Community connectors via PF4J (`CommunicationSourceExtension`) | CREATE | marketplace |

### 7.3 To maintain (ongoing maintenance)

- IMAP/OAuth2 connectors (token rotation, provider evolutions, deprecations).
- Extraction prompts and models (NER/commitments/decisions quality over time, drift).
- Entity resolution and cleaning rules (new signature/disclaimer formats).
- Graph schema, Flyway migrations (one query per file) and weight/time-decay recalculation.
- Privacy, retention and audit policies; regulatory updates.

---

## 8. AI / GraphRAG use cases

Concrete examples of questions that the graph + GraphRAG enables, with the type of navigation involved:

| Use case | Example question | Navigation |
|------------|--------------------|-------------|
| **Customer dossier status** | "What is the status of everything we have with customer Rossi?" | Org→People→Threads→Commitments/Decisions (multi-hop) |
| **Contextual retrieval** | "Find the email in which they confirmed the final price" | Semantic (Qdrant) + thread/person filter |
| **Who knows about X / internal expert** | "Who in the company has handled supplier Beta?" | Topic/Org→`COMUNICA_CON`→People (weight) |
| **Commitments and follow-ups** | "What have I promised and not yet done?" | Person→`RESPONSABILE_DI`→Commitments (status/deadline) |
| **Pre-call briefing** | "Prepare me a briefing before the call with Gamma" | Person/org subgraph + summaries + latest threads |
| **History of a decision** | "How and when did we decide to renew the contract?" | Decision→Thread→cited Messages |
| **Non-obvious links** | "Are there topics that connect customer A and supplier B?" | Paths between two Orgs via Topics/People |
| **Continuity (absent person)** | "What was Tizio working on with customers?" | Person→open Threads→pending Commitments |
| **Onboarding** | "Summarize the history of the Alfa project for me" | Topic→Threads→timeline+decisions |
| **The right attachment** | "What is the latest version of the contract sent to Delta?" | Org→Messages→Attachments (version/hash) |

All answers include **citations** of the messages/threads/people (with date and link to the original message), respecting the user's visibility perimeter. The execution default is **local Ollama**; the use of cloud providers is allowed only if the domain policy permits it and, ideally, after PII redaction.

---

## 9. KPIs & success metrics

| Category | KPI | How it is measured | Indicative target |
|-----------|-----|----------------|-------------------|
| **Adoption** | Mailboxes ingested / active users | Account count + queries/user/week | Steady growth |
| **Coverage** | % of messages processed successfully | Messages ingested / total in perimeter | > 98% |
| **Ingestion quality** | Thread reconstruction accuracy | Manually validated sample | > 95% |
| **AI quality** | NER and commitment precision/recall | Labeled evaluation set | Precision > 0.85 |
| **Entity resolution** | % of aliases correctly unified | Sample of people/org | > 0.9 |
| **GraphRAG effectiveness** | Accuracy on multi-hop questions | Reference question suite | Clear advantage vs flat RAG |
| **Usefulness** | Rate of "useful" answers (feedback) | Thumbs up/down in chat | > 80% useful |
| **Time savings** | Average time to "find/reconstruct context" | Survey + analytics | Marked reduction |
| **Commitments** | % of follow-ups respected thanks to notifications | Commitments closed on time / total | Increasing |
| **Privacy** | Data exfiltration incidents | Monitoring (must be zero) | 0 |
| **Performance** | Average GraphRAG response latency | Tracing | Acceptable for interactive use |
| **Freshness** | Average ingestion delay | Reception timestamp → availability in the graph | Minutes, not hours |

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---------|---------|-------------|
| **Privacy / GDPR (extremely sensitive data)** | High | Local-first by design; Ollama-only by default for the domain; PII redaction; visibility perimeters; consent and purpose; retention/forgetting; audit |
| **Perception of employee surveillance** | High | Declared purposes, exclusion of personal/sensitive communications, no individual monitoring, transparency toward users |
| **Access beyond perimeter** | High | Authorization filter applied upstream of retrieval (`auth`), dedicated security tests |
| **Entity resolution errors** | Medium | Multi-signal strategies + manual review/merge + feedback; confidence thresholds |
| **Hallucinations / incorrect extractions (commitments, decisions)** | Medium | Answers always cited; confidence exposed; human validation for actions; never automatic sending |
| **Imprecise threading (reused subjects, forks)** | Medium | Standard headers + robust fallbacks; validation on a sample |
| **Volume/scalability (huge mailboxes)** | Medium | Incremental sync, batch, checkpoint, pagination; incremental weight recalculation |
| **Compromised credentials** | High | OAuth2 where possible; at-rest encryption; rotation; least privilege |
| **Noise (newsletters, automated, spam)** | Low | Filters by sender/domain, classification, list exclusion |
| **LLM cost/latency on large volumes** | Medium | Local Ollama processing, nightly batching, hierarchical summaries, caching |
| **Multilingual** | Low | Language detection + appropriate prompts/embeddings (IT/EN and beyond) |
| **Dependence on the IMAP provider** | Low | Port/adapter abstraction; multiple connectors; mbox import as fallback |

---

## 11. Maintenance & evolution

- **Connectors and authentication**: monitor the evolutions of providers (Gmail/Microsoft), progressively migrate from application passwords to OAuth2, manage token rotation and API deprecations. Keep the port/adapter abstraction clean to add Graph/EWS/mbox without touching the domain.
- **Quality of extraction models**: the Ollama prompts and models for NER, summaries, commitments and decisions must be periodically evaluated with a labeled set to intercept *drift*; version the prompts and track the quality metrics (section 9).
- **Cleaning and entity resolution rules**: new signature/disclaimer formats and new aliases require continuous updating of the heuristics; provide manual merge/split tools for Person/Organization nodes.
- **Graph weights**: incremental recalculation at each ingestion and scheduled job for time-decay and normalization; periodically review the factors and their coefficients based on feedback.
- **Schema and migrations**: every evolution of the graph schema goes through Flyway with **a single query per file** (project constraint) and UUID mapping `@JdbcTypeCode(SqlTypes.CHAR)`.
- **Operational privacy**: periodic review of perimeters, retention policies, actual execution of forgetting, audit; alignment with regulatory updates (GDPR and sectoral).
- **i18n**: UI, documentation and enums (e.g. mailbox type, message classification, commitment status, organization type) translated and routed IT/EN according to the switch, consistently with the project rules.
- **Community extensibility**: keep the PF4J `CommunicationSourceExtension` extension point stable and document it for contributors; publish connectors on the marketplace.
- **Evolutionary roadmap**: from the IMAP MVP → OAuth2/Graph → historical import → omni-channel (mail + `messaging` + `calendar`) → proactive suggestions (`automation`) → assisted draft composition.

---

## 12. Integration with existing LocalMind modules

| Module / domain | Role in the domain | Reuse vs extension |
|------------------|-------------------|---------------------|
| **`email`** | Heart of IMAP ingestion; `EmailMessage`, `EmailPort`, `EmailService` already present | **Extend** model and port; add incremental sync and advanced parsing |
| **`knowledge`** | Graph domain: nodes/edges/weights, entity resolution, GraphRAG | **Extend** with node/relationship types of the communications domain |
| **`llm` + Ollama** | Enrichment (NER, summaries, commitments) and answer generation | **Reuse** of the `LlmGatewayService`; Ollama default, per-domain policy |
| **`document` + Tika/OCR** | Text extraction of attachments → KB documents | **Reuse** of the `DocumentIngestionPipelineService` |
| **`vectorstore` (Qdrant)** | Embedding and semantic search of contents | **Reuse/extension** with perimeter filters |
| **MySQL + Flyway** | Graph structure and persistence | **Extension** of schema (one query/file, UUID CHAR(36)) |
| **`localmind-batch` + scheduler** | Scheduled incremental ingestion jobs | **Reuse** of the batch/folder-scan pattern |
| **`auth`** | Visibility perimeters, roles (user/admin/DPO) | **Extend** with mailbox/dossier-level authorization |
| **`calendar`** | Commitments/deadlines ↔ events | **Bidirectional integration** |
| **`messaging`** | Chat channels (Slack/Telegram/...) | **Evolution**: omni-channel communication graph |
| **`automation`** | Proactive notifications (follow-ups, deadlines) and webhooks | **Reuse** for triggers on new commitments/events |
| **`agent`** | Assisted composition of drafts, orchestrated actions | **Evolution**, always with human confirmation |
| **`marketplace` + PF4J** | Third-party communication connectors | **Extension**: new `CommunicationSourceExtension` |
| **`common` (events/exception)** | Domain events (ingestion completed, new commitment) | **Reuse** of `DomainEventPublisherPort` |
| **Angular frontend** | UI for accounts, dossiers, chat over the graph, commitment views | **Create** `email`/`knowledge` feature; reuse `ChatStore`, i18n |

The domain therefore grafts naturally onto the existing hexagonal architecture: the domain stays pure (wiring via `DomainConfig`), the IMAP/OAuth2/mbox adapters live in infrastructure as implementations of port out, the API exposes the endpoints under `/api/v1/` and the frontend adds the features in lazy-loading. No new datastore: **MySQL for the graph structure, Qdrant for semantics**, AI **Ollama locally** as default — fully consistent with the project's constraints of local-first, enterprise privacy, open source and IT/EN bilingualism.

---

*Reference sources for 2026 best practices: Microsoft GraphRAG, Gartner (GraphRAG among the 2026 data trends), literature on entity resolution for enterprise knowledge graphs, local-first email architectures with PII redaction and explicit consent for sending to external LLMs.*
