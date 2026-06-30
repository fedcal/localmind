# Local Services & Healthcare

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

This domain belongs to the **consumer group** of LocalMind's universal knowledge graph engine. While the "territory discovery" vertical (tourism, events, experiences) answers the question *"what can I do here?"*, the **Local Services & Healthcare** domain answers the complementary and more urgent question: *"which service do I need, where can I find it, how do I access it, and who do I turn to?"*. It is the bridge between a citizen's need and the offering — public, healthcare-related, and private/professional — of the territory, modeled as a weighted graph navigable by the AI.

The domain is deliberately built around a consolidated industry standard — **HSDS (Human Services Data Specification) by Open Referral** and the related **FHIR Human Services Directory** — to guarantee interoperability with existing catalogs, and around the Italian digitalization initiatives (FSE 2.0, SPID/CIE, PagoPA, HL7 CDA2). Everything nonetheless remains **local-first**: citizens' sensitive data never leaves the self-hosted instance without explicit consent, and the AI defaults to Ollama running locally.

---

## 1. What we solve (problem & value)

### 1.1 The concrete problem

Access to local public, healthcare, and professional services today is **fragmented, opaque, and cognitively costly** for the citizen. The information exists but is scattered across dozens of heterogeneous portals (the municipality's website, the ASL, the Region, help desks, associations' Facebook pages, paper flyers, word of mouth). This gives rise to recurring problems:

- **Source fragmentation.** To understand how to obtain a certificate, book a visit, access financial support, or find an accredited professional, the citizen has to consult different sources, each with its own bureaucratic language and its own structure.
- **Distance between need and service.** The citizen expresses a *need* in natural language ("I need help for my elderly mother who is no longer self-sufficient", "I lost my job and have to pay the rent", "my son needs a speech therapist"), but catalogs are organized by *providing entity* or by *administrative category*, not by need. The need → service translation layer is missing.
- **Outdated or inconsistent information.** Opening hours, access requirements (ISEE, residency, age range), required documents, costs (co-payments, exemptions), and waiting times change frequently and are not aligned across sources.
- **Invisible chains of prerequisites.** Many services require preparatory steps (SPID/CIE to access portals, choosing a primary care physician before booking, ISEE certification before a benefit, a referral before a specialist visit). These dependencies are rarely made explicit and lead to wasted trips.
- **Digital and language access barriers.** The elderly, vulnerable people, foreigners, and caregivers struggle with portals designed for "insiders". A conversational interface that guides them step by step is missing.
- **Help desk overload.** Public Relations Offices (URP), ASL secretariats, CAFs, and patronage offices receive an enormous volume of first-level information requests ("who should I turn to for...") that could be resolved by an intelligent assistant, freeing operators for complex cases.

### 1.2 The LocalMind solution

LocalMind models the entire service ecosystem as a **weighted knowledge graph**: nodes represent services, entities, locations, professionals, deliverables, needs, requirements, and procedures; edges represent weighted relationships (a need *is satisfied by* a service with a certain degree of relevance, a service *requires as a prerequisite* another one, a deliverable *is provided at* a location, etc.). The AI operates on this graph in **GraphRAG** mode: it combines semantic search (Qdrant) with relationship navigation (MySQL) to answer complex questions and surface the **complete path** from the need to using the service.

The differentiating value compared to a simple search engine or a FAQ chatbot:

| Capability | Traditional search engine / FAQ | LocalMind (graph + GraphRAG) |
|----------|--------------------------------------|------------------------------|
| Need → service translation | Keyword match | Reasoning on the need↔service graph with relevance weights |
| Chains of prerequisites | Not handled | Explicit multi-hop path (referral → booking → co-payment → exemption) |
| Personalized guidance | Generic | Filtered by the citizen's requirements (residency, ISEE, age, vulnerability) |
| Data updating | Manual, static | Ingestion from open data + community verification + expiry dates |
| Explainability | Absent | Answer with citation of the nodes/paths used |
| Citizen data privacy | Often cloud | Local-first, Ollama AI running locally |

### 1.3 The value for stakeholders

- **For the citizen:** a single conversational access point, bilingual (IT/EN, extendable), that starts from the need expressed in words and returns *what to do, where, with which documents, at which costs, and with which alternatives*, including the chain of prerequisites.
- **For local Public Administration (Municipalities, Unions of Municipalities):** reduced load on help desks, an always-consistent and self-hosted service catalog (data sovereignty), and the opening of its own catalog in a reusable standard format (HSDS).
- **For the local healthcare system (ASL, districts, GPs/PLS, pharmacies):** guiding the patient toward the correct setting (reducing improper emergency room access), with always-aligned information on deliverables, exemptions, and pathways.
- **For the third sector and professionals:** structured, community-driven visibility of the services offered (associations, CAFs, patronage offices, accredited professional practices), with emergent ranking based on quality and real usefulness.

### 1.4 Boundaries of responsibility (what it is NOT)

To avoid ambiguity and regulatory risks, the domain is also defined by exclusion:

- **It is not a clinical diagnosis or triage tool.** It does not provide medical opinions; it guides toward the correct service/professional and always flags when it is necessary to turn to a healthcare professional or to emergency numbers.
- **It does not replace official booking/payment systems.** It integrates with them (deep links to CUP, FSE, PagoPA) but remains a layer of guidance and knowledge, not a certified transactional healthcare system.
- **It is not a health record.** It does not keep medical records; any of the citizen's personal data stays local, transient, and under the user's control.

---

## 2. Personas & target users

| Persona | Description | Primary goal | Key need |
|---------|-------------|--------------------|-----------------|
| **Generic citizen** | Adult looking for an occasional service (certificate, benefit, booking) | Resolve a procedure without wasting time | Simple language, step-by-step path |
| **Family caregiver** | Child/spouse assisting an elderly or vulnerable person | Find home care, ADI, respite care, allowances | Overview of integrated socio-healthcare services |
| **Vulnerable person / elderly** | Low digital literacy | Understand "who do I turn to" | Conversational interface, possibly voice-based, clear IT |
| **Foreign / newly arrived citizen** | Knows little of the language and the system | Get oriented on healthcare, residency, school | Multilingual, translation of bureaucratic jargon |
| **Chronic patient** | Manages a long-term condition | Recurring deliverables, exemptions, PDTA | Pathways (PDTA), exemptions, continuity |
| **URP / social help desk operator** | PA employee handling the front office | Respond quickly and consistently | Reliable internal tool, source citations |
| **Third-sector operator (CAF, patronage office, association)** | Accompanies vulnerable users | Map the offering and direct people | Up-to-date catalog, flagging of gaps |
| **Catalog editor / curator** | Official or volunteer who maintains the data | Keep the graph up to date and correct | Editing, moderation, validation tools |
| **Instance administrator** | Municipality/ASL technician managing LocalMind | Self-hosting, source ingestion, privacy | Connectors, configuration, access control |
| **Community contributor** | Active citizen who reports/corrects information | Improve collective accuracy | Simple contributions, weight/reputation |

Important distinction: the **consumer end users** (first five rows) interact in read-only mode through the conversational assistant; the **operators and curators** have editing/moderation permissions; the **administrator** manages ingestion and configuration. This segmentation drives the authorization roles (`auth` domain).

---

## 3. Input requirements

This section defines in detail **what must enter the system** so that the service graph is useful, accurate, and maintainable. We distinguish **domain** inputs (the data describing services and needs), **end-user** inputs (the request), and **configuration/governance** inputs.

### 3.1 Domain inputs (the catalog data)

For each **service** or **deliverable**, the model requires a minimal set of attributes and an extended one:

| Category | Minimal fields (MVP) | Extended fields (evolution) |
|-----------|--------------------|---------------------------|
| Identity | name, brief description, category/taxonomy, delivery languages | extended description, keywords, colloquial synonyms |
| Provider | responsible entity/organization, type (public/accredited private/third sector) | organizational hierarchy, VAT number/entity code |
| Location | physical location(s) (address, coordinates), territorial catchment area | accessibility (architectural barriers), public transport |
| Access | requirements (residency, age, ISEE, condition), required documents | preparatory prerequisites (other services), channels (online/help desk/phone) |
| Times & costs | hours, cost/co-payment, applicable exemptions | average waiting times, opening/closing calendar |
| Contacts | phone, email, website/portal, booking link | reference person, PEC, social media, chat |
| Validity | last update date, status (active/suspended) | information expiry date, authoritative source, verification level |

For **professionals / practices** (primary care physicians, accredited private specialists, pharmacies, psychologists, physiotherapists, etc.) the following are added: specialization, accreditation/SSN agreement, professional registry membership, availability for new patients, and services offered.

For **needs** (the demand side of the graph), a **taxonomy of needs** in citizen language is required, with synonyms and colloquial phrasings, mapped to the service categories. Examples: "elderly non-self-sufficiency", "economic hardship/rent", "psychological support", "domestic violence", "minor disability", "first healthcare access for a foreigner".

### 3.2 Quality constraints on domain inputs

The system must **validate at the boundary** (consistent with the project rules on input validation):

- **Mandatoriness:** the minimal fields are mandatory; a service without a provider or without an access method cannot be published.
- **Normalization:** geocoded addresses (lat/long), phone numbers in canonical format, categories tied to the controlled taxonomy (no free text for the category).
- **Freshness:** every piece of information has an update date; beyond a configurable threshold the information is marked "to be verified" and its weight decays (see §5).
- **Provenance:** every node/edge tracks the source (official open data, community contribution, internal editorial) and the verification level.
- **Sensitivity:** no citizens' personal data in the domain nodes; the domain describes *the offering*, not the users.

### 3.3 Supported interchange standards and formats

To maximize interoperability and reuse, domain inputs must be importable/exportable in industry formats:

- **HSDS (Human Services Data Specification) 3.x by Open Referral** — the reference format for catalogs of services to the person (organizations, services, locations, contacts, requirements, hours). It is the domain's primary interchange model.
- **FHIR Human Services Directory IG** — for interoperability with healthcare systems (provider directory) and with FSE 2.0.
- **PA open data / tabular formats** — CSV/JSON/Excel published by Municipalities, Regions, ASLs; CKAN/DCAT-AP_IT for open data portals.
- **HL7 CDA2 / FSE 2.0** — reference for healthcare deliverable metadata (only at the deliverable catalog level, not for personal clinical documents).
- **Schema.org (GovernmentService, MedicalOrganization)** — for enrichment and SEO of public nodes.

### 3.4 End-user inputs (the request)

The conversational assistant accepts:

- **Need in natural language** (text, in IT/EN and extendable), possibly vague or emotional.
- **Optional and voluntary context** to personalize the guidance: municipality/neighborhood of residence, ISEE bracket, age range, presence of disability/vulnerability, condition (e.g., caregiver, foreigner). Everything is **opt-in**, processed locally and not persisted without consent.
- **Explicit filters**: type of service, maximum distance, only free/exempt services, only online, only accessible.
- **Uploaded documents** (optional): a flyer, a letter from the PA, a medical report — handled by the `document` domain (Tika/OCR) to extract context and provide better guidance; they stay local.

### 3.5 Configuration and governance inputs

- **Territorial perimeter** of the instance (a Municipality, a Union, an ASL district): defines the data catchment.
- **Active taxonomies** (service categories, needs taxonomy) and their mappings.
- **Enabled connectors** and their credentials/endpoints (open data portals, CUP, FSE — where APIs are available).
- **Moderation policies**: who can create/modify/approve nodes; community reputation thresholds.
- **Weight thresholds** and decay policies (freshness, source, feedback).
- **Disclaimers and legal texts** (limits of the guidance, referral to emergency, privacy).

---

## 4. Activity flow (step-by-step)

We describe two complementary flows: the **ingestion/graph-building flow** (data side) and the **consultation flow** (user side). They are the operational heart of the domain and must be implemented carefully.

### 4.1 Ingestion and graph-building flow

```
Source → Connector → Normalization → Mapping to nodes/edges → Validation →
Deduplication/Entity resolution → Initial weighting → Embedding → Persistence (MySQL+Qdrant) → Graph indexing
```

1. **Acquisition from the source.** A connector (`document`/batch domain or a dedicated connector) retrieves the data: open data API, CSV/JSON file, web page, PDF/flyer document (Tika + Tesseract OCR), or HSDS/FHIR feed. Execution is scheduled (`localmind-batch` batch) or on-demand.
2. **Normalization.** Cleaning of fields, geocoding of addresses, normalization of phones/hours, recognition of the category against the controlled taxonomy. Invalid inputs are rejected with a clear message and logged.
3. **Mapping to nodes and edges.** Each record becomes one or more typed nodes (Service, Entity, Location, Deliverable, …) and the edges connecting them (provides, has location, requires prerequisite, satisfies need). The mapping is driven by the domain schema (HSDS → LocalMind graph model).
4. **Domain validation.** Verification of mandatoriness, referential consistency (every Service has an Entity and at least one access method), and quality (freshness, provenance).
5. **Deduplication / entity resolution.** Recognition of already-existing nodes (same entity/location/service) through natural keys + semantic similarity (embedding) to avoid duplicates. In case of conflict, merge with source tracking.
6. **Initial edge weighting.** Calculation of the starting weight based on source (official > community > inferred), completeness, and freshness (see §5).
7. **Semantic embedding.** Generation of embeddings (Ollama `@Primary`) of the descriptions of services and needs and indexing on Qdrant for semantic search.
8. **Persistence.** Nodes and edges on MySQL (graph structure, weights, metadata, provenance); vectors on Qdrant; all via the hexagonal infrastructure adapters.
9. **Link suggestion.** The AI proposes missing, non-obvious edges (e.g., a "caregiver support" need linkable to a service so far unmapped to that need); the suggestions enter a moderation queue.
10. **Availability.** The updated graph is immediately queryable; domain events notify the update (reuse of `DomainEventPublisherPort`).

### 4.2 Consultation flow (citizen / operator)

```
NL need → Intent understanding → Hybrid retrieval (semantic+graph) →
Multi-hop expansion (prerequisites, alternatives) → Personal filters → Ranking →
Guided answer with citations → Actions (deep link, save, contact) → Feedback
```

1. **Expression of the need.** The user writes (or dictates) their need in natural language, possibly with voluntary context and filters.
2. **Intent understanding.** The AI (Ollama running locally) interprets the need, disambiguates it if necessary (a targeted clarifying question, e.g., "do you mean home healthcare assistance or financial aid?") and maps it onto the need-nodes of the taxonomy.
3. **Hybrid retrieval (GraphRAG).** Semantic search on Qdrant for relevant services + entry into the graph from the identified need-nodes. The two signals combine.
4. **Multi-hop expansion.** From the candidate service the AI navigates the relationships: prerequisites (what is needed first), locations (where), alternatives (equivalent services if the main one is not accessible), related deliverables, providing entity. It thus builds the **complete path**.
5. **Application of personal filters.** Excludes/penalizes services incompatible with the declared requirements (residency outside the catchment, ISEE over threshold, ineligible age), highlighting exemptions and free services when relevant.
6. **Ranking.** Orders the results by need relevance, edge weight, geographic proximity, freshness, and community reputation.
7. **Guided and explainable answer.** The assistant returns: the service (or the best 2-3), *what to do step by step*, required documents, costs/exemptions, where and when, alternatives, and **the citations of the nodes/paths** used (transparency). It always includes the disclaimers (it is not a medical opinion; emergency numbers where relevant).
8. **Actions.** Deep links to official systems (CUP booking, FSE, PagoPA for payments, the entity's website), saving the path, copying the contacts, possible generation of a reminder (reuse of the `calendar` domain) or sending via messaging (`messaging`/`email` domain).
9. **Feedback.** The user reports whether the information was useful/correct/up to date. The feedback feeds the edge weights and the curation queue (closing the loop back to §4.1).

### 4.3 Curation and moderation flow

1. Community contributions and reports (new service, correction, "outdated information") enter a **moderation queue**.
2. The curator/operator reviews, compares with the authoritative source, approves/rejects/requests changes.
3. Approval updates nodes/edges and recalculates the weights; rejection is tracked with a reason.
4. Repeated reports on the same node lower its weight until verification (anti-degradation mechanism).

### 4.4 Handling edge cases

- **Unmapped need:** if no service covers the need, the AI declares it, proposes the generic channel (URP, single social helpline) and records the gap for curation.
- **Detected emergency:** if the user's text indicates a healthcare urgency or danger (e.g., risk keywords), the assistant interrupts ordinary guidance and immediately refers to 112/118/dedicated numbers.
- **Unresolvable ambiguity:** at most N clarifying questions, then an answer with the best hypotheses labeled as such.
- **Data in conflict between sources:** shows the most authoritative/recent version and flags the discrepancy.

---

## 5. Graph model (node types, relationship types, weighting criteria)

The model reuses LocalMind's **core graph engine** (typed nodes + weighted edges on MySQL, semantics on Qdrant). Below are the types specific to this domain.

### 5.1 Node types

| Node type | Description | Key attributes |
|--------------|-------------|------------------|
| `Bisogno` | Citizen's need in natural language | name, synonyms, area (healthcare/social/administrative) |
| `Servizio` | Provided service (public/private/third sector) | name, category, description, languages, status |
| `Prestazione` | Specific deliverable unit (e.g., cardiology visit, registry certificate) | type, co-payment/cost, exemptions, waiting times |
| `Ente` / `Organizzazione` | Providing party | type, legal nature, hierarchy |
| `Sede` / `PuntoDiAccesso` | Physical place or digital channel | address, coordinates, accessibility, hours |
| `Professionista` | Doctor, specialist, psychologist, etc. | specialization, accreditation, registry, availability |
| `Requisito` | Access condition | type (residency/ISEE/age/condition), value/threshold |
| `Documento` / `Modulo` | Required or produced document | name, where to obtain it, format |
| `Procedura` / `Percorso` | Process or PDTA (diagnostic-therapeutic pathway) | ordered steps, estimated duration |
| `Categoria` / `Tassonomia` | Classification node | level, parent, standard mapping (HSDS) |
| `Area territoriale` | Municipality, neighborhood, district, catchment | type, ISTAT code |
| `Contatto` | Contact detail | channel, value, hours |
| `FonteDato` | Information provenance | type, URL, date, authoritativeness |

### 5.2 Relationship types (edges)

| Relationship | From → To | Meaning |
|-----------|--------|-------------|
| `SODDISFA` | Bisogno → Servizio/Prestazione | The service responds to the need (with a degree of relevance) |
| `EROGATO_DA` | Servizio/Prestazione → Ente/Professionista | Who provides it |
| `HA_SEDE_PRESSO` | Servizio → Sede/PuntoDiAccesso | Where it is accessed |
| `RICHIEDE_PREREQUISITO` | Servizio → Servizio/Procedura | Preparatory step (e.g., referral before the visit) |
| `RICHIEDE_REQUISITO` | Servizio → Requisito | Access condition |
| `RICHIEDE_DOCUMENTO` | Servizio → Documento/Modulo | Required documents |
| `ALTERNATIVA_A` | Servizio → Servizio | Equivalent/substitute service |
| `FA_PARTE_DI` | Prestazione → Servizio / Sede → Ente | Composition/hierarchy |
| `APPARTIENE_A_CATEGORIA` | Servizio → Categoria | Classification |
| `SERVE_AREA` | Servizio/Sede → Area territoriale | Catchment of competence |
| `STEP_DI` | Procedura → Servizio/Prestazione | Stage of a pathway/PDTA |
| `CORRELATO_A` | Servizio → Servizio | Thematic affinity (useful for suggestions) |
| `HA_CONTATTO` | Ente/Sede/Servizio → Contatto | Contact details |
| `ATTESTATO_DA` | Node/Edge → FonteDato | Provenance and verification |

### 5.3 Edge weighting criteria

The weight (0–1, or a configurable scale) is **derived and dynamic**, consistent with the configurable factors of the core engine (usage frequency, relevance, dependencies, feedback). For this domain the factors are:

| Factor | Effect on the weight | Notes |
|---------|------------------|------|
| **Source authoritativeness** | Official open data > internal editorial > community > AI-inferred | Tracked via `ATTESTATO_DA` |
| **Freshness** | Decay over time since the last update/verification | Configurable threshold; beyond threshold → "to be verified" |
| **Semantic relevance** | For `SODDISFA`: need↔service embedding similarity | From Qdrant |
| **User feedback** | "Useful/correct" votes increase it, error reports decrease it | Community reputation |
| **Usage frequency** | Services actually consulted/followed weigh more | Aggregated and anonymous usage signal |
| **Node completeness** | Nodes with complete minimal fields weigh more | Penalty for missing data |
| **Proximity / territorial coverage** | For ranking: services within the user's catchment | Applied at query time, not only statically |
| **Multi-source confirmation** | Information confirmed by multiple independent sources | Reliability bonus |

The weight thus calculated drives both the **ranking** of answers, the AI's **multi-hop reasoning** (it prefers high-weight paths), and the **controlled decay** of outdated data.

---

## 6. Data sources & connectors (ingestion)

| Source | Type | Connector | Priority |
|-------|------|-----------|----------|
| PA open data portals (Municipalities/Regions) — CKAN/DCAT-AP_IT | API/CSV/JSON | Open data connector + batch | MVP |
| HSDS / Open Referral catalogs | JSON/API | HSDS connector (native mapping) | MVP |
| FHIR Human Services Directory / provider directory | FHIR REST | FHIR connector | Evolution |
| Institutional websites (Municipality, ASL, district) | Web scraping/HTML | Web connector + Tika | MVP |
| PDF documents/flyers, brochures | File | `document` domain (Tika + Tesseract OCR) | MVP |
| FSE 2.0 / healthcare deliverable catalogs (HL7 CDA2) | API/metadata | Healthcare connector (catalog only) | Evolution |
| Regional CUPs (deliverable catalog, waiting times) | API where available | CUP connector | Evolution |
| Community contributions (internal form) | UI | Editor + moderation | MVP |
| Editorial entry/editing | UI | Curator editor | MVP |
| Entities' legacy Excel/CSV tables | File | Tabular importer with mapping | MVP |

Ingestion principles: each connector is isolated (reuse of the **PF4J plugin** pattern and the extension points — a `ServiceDirectoryConnectorExtension` allows adding connectors without touching the core); ingestion always goes through the §4.1 pipeline (normalization → mapping → validation → deduplication → weighting → embedding → persistence); each piece of data carries its own `FonteDato`. The batches reuse `localmind-batch` (folder scan, scheduling) and the adapters reuse the hexagonal infrastructure.

---

## 7. Features to create, develop, and maintain (MVP → evolution)

### 7.1 MVP (first release of the domain)

| # | Feature | What it entails | Modules involved |
|---|--------------|---------------|------------------|
| 1 | **"Services" domain schema** | Node/relationship types §5 on the core graph engine; Flyway migrations (one query per file) | `knowledge`/core graph, MySQL |
| 2 | **Needs taxonomy + service categories** | Controlled IT/EN taxonomy with synonyms, HSDS mapping | `knowledge`, i18n |
| 3 | **HSDS connector + tabular importer** | Import of standard catalogs and entities' CSV/Excel | batch, infrastructure, plugin |
| 4 | **Ingestion pipeline** | Normalization, validation, deduplication, initial weighting, embedding | batch, `document`, Qdrant |
| 5 | **Service node/edge CRUD API** | `/api/v1/...` endpoints for catalog management; IT/EN DTOs | `localmind-api`, domain |
| 6 | **Catalog editor + moderation** | UI to create/modify services and approve contributions | frontend `servizi` feature |
| 7 | **Conversational need→service assistant (GraphRAG)** | Hybrid retrieval, multi-hop expansion, answer with citations | `llm`/GraphRAG, Qdrant, graph |
| 8 | **Catalog search/filters** | Text search + filters (category, area, free, online) | frontend, search |
| 9 | **Complete service detail sheet** | View with path, prerequisites, documents, costs, locations, contacts | frontend |
| 10 | **Community contributions & feedback** | Contribution form, usefulness vote, outdated report | frontend, domain, `auth` |
| 11 | **Disclaimer & emergency handling** | Legal texts, urgency detection, referral to 112/118 | `llm` guardrail, config |
| 12 | **IT/EN bilingualism** | UI, translated enums, key content | i18n (project constraint) |

### 7.2 Future evolutions

| Feature | Added value |
|--------------|-----------------|
| **FHIR / FSE 2.0 connector** | Healthcare interoperability, alignment of deliverables and provider directory |
| **CUP connector + waiting times** | Dynamic guidance toward locations with shorter waits |
| **Interactive graph visualization** | Visual exploration of service↔need↔prerequisite paths |
| **Advanced personalized guidance** | Opt-in citizen profile (ISEE, vulnerability) for fine-grained matching |
| **PDTA / guided pathways** | Multi-step procedures for chronic patients and complex procedures |
| **Automatic suggestion of missing links** | The AI proposes non-obvious edges between needs and services |
| **Reputation & emergent ranking** | The best services emerge from real usage and feedback |
| **In/out channels (WhatsApp, email, voice)** | Multi-channel access for vulnerable people (`messaging`/`email` domain) |
| **Reminders and follow-up** | Deadlines (exemption renewal, visit recall) via `calendar` |
| **Installable "Local Services & Healthcare" package/module** | Distribution via marketplace for other Municipalities/ASLs |
| **Gap analysis** | Report on uncovered needs for public decision-makers |
| **Advanced accessibility (WCAG, voice)** | Inclusion of the elderly and people with disabilities |

### 7.3 To maintain (continuous evolutionary maintenance)

- Updating the connectors as the APIs/formats of the sources change (HSDS, FHIR, PA open data).
- Updating the needs/categories taxonomies and their mappings to standards.
- Periodic recalculation of weights and application of freshness decay.
- Continuous moderation of contributions and handling of reports.
- Updating the disclaimers and regulatory references (FSE 2.0, regulatory deadlines).
- Synchronization of the translated IT/EN enums toward the frontend (project constraint).
- Incremental Flyway migrations (one query per file) as the schema evolves.

---

## 8. AI / GraphRAG use cases

1. **Guidance from the need (flagship case).** *"My mother is elderly and no longer self-sufficient, I live in [municipality], what can I do?"* → the AI maps the need, retrieves ADI, home care, attendance allowance, day centers; expands prerequisites (UVM assessment, ISEE) and returns the path with documents, locations, and contacts, citing the nodes.
2. **Chain of prerequisites.** *"How do I book a cardiology visit?"* → path: choosing a GP → referral → CUP booking (deep link) → co-payment/exemption (PagoPA) → location and preparation, with accredited private alternatives.
3. **Cross-graph comparative question.** *"Which free psychological support services are there within 5 km?"* → query with filters (free, distance) and weighted ranking.
4. **Guided disambiguation.** Vague need → the AI asks a targeted question and then routes (see §4.2 step 2).
5. **Non-obvious links.** The AI suggests to a caregiver related services they would not have searched for (e.g., "respite desk", support groups) through `CORRELATO_A`/`SODDISFA` relationships.
6. **Multi-document synthesis.** From uploaded flyers/PDFs it extracts and links the information to the graph (reuse of `document` + GraphRAG).
7. **Assistant for the help desk operator.** Quick and traceable answers with source citation, to standardize the front office.
8. **Gap analysis for decision-makers.** *"Which needs turn out to be uncovered in our territory?"* → the AI navigates the `Bisogno` nodes without high-weight `SODDISFA` edges.

In all cases: **Ollama AI running locally** by default, answers with **citation of the nodes/paths** (explainability), and **guardrails** (no diagnosis, referral to emergency).

---

## 9. KPIs & success metrics

| Category | KPI | Target / direction |
|-----------|-----|-----------------------|
| Coverage | % of taxonomy needs with at least one linked service | Increasing |
| Coverage | No. of services/entities/locations mapped in the catchment | Increasing |
| Data quality | % of nodes with complete minimal fields | > 90% |
| Freshness | % of information updated within the threshold | > 80% |
| AI effectiveness | Rate of answers judged useful (feedback) | Increasing |
| AI effectiveness | % of requests resolved without human intervention (deflection) | Increasing |
| AI effectiveness | Average length of the returned path vs. complete (prerequisite recall) | High |
| Experience | Average time from need to useful answer | Decreasing |
| Community | No. of approved contributions/corrections per period | Increasing |
| Community | Average moderation time | Decreasing |
| PA impact | Reduction of first-level requests at help desks | Decreasing |
| Reliability | % of answers with source citation | ~100% |
| Safety | % of correct detection of emergency cases | ~100% |

---

## 10. Risks & mitigations

| Risk | Impact | Mitigation |
|---------|---------|-------------|
| **Outdated/incorrect information** | Citizen misdirected | Freshness in the weight, decay, "to be verified", multi-source, feedback |
| **Liability for healthcare advice** | Legal/ethical risk | Clear disclaimers, no diagnosis, referral to emergency, source citation |
| **Citizen data privacy** | GDPR violation | Local-first, local Ollama AI, opt-in context not persisted, no personal data in the nodes |
| **AI hallucinations** | Made-up answers | GraphRAG anchored to the nodes, answers only from the graph, mandatory citations |
| **Source fragmentation/instability** | Fragile ingestion | Isolated connectors (plugins), robust validation, source monitoring |
| **Duplicates and inconsistencies** | Dirty graph | Entity resolution, deduplication, moderation |
| **Users' low digital literacy** | Exclusion | Simple conversational UI, multilingual, future voice/WhatsApp channel |
| **Bias/gaps in the needs taxonomy** | Unrecognized needs | Gap analysis, periodic review, community contributions |
| **Moderation overload** | Unsustainable curation | Prioritized queues, reputation thresholds, pre-validation automations |
| **Regulatory misalignment (FSE 2.0, deadlines)** | Non-compliant data/processes | Evolutionary maintenance, regulatory monitoring |

---

## 11. Maintenance & evolution

- **Continuous data cycle:** scheduled ingestion (batch), weight recalculation, freshness decay, moderation, and feedback close the §4.1↔§4.2 loop.
- **Catalog governance:** clear roles (admin, curator, contributor) via the `auth` domain; versioned moderation policies.
- **Schema evolution:** new node/relationship types introduced with incremental Flyway migrations (one query per file) and maintaining backward compatibility.
- **Standards updating:** alignment with new versions of HSDS/FHIR and with the Italian initiatives (FSE 2.0, DCAT-AP_IT) through versioned connectors.
- **Distribution as a module:** packaging of the domain as an installable module via **marketplace**, reusable by other Municipalities/ASLs with configuration of the territorial perimeter.
- **Bilingual documentation:** maintenance of the IT docs (`documentazione/`) and EN docs (`documentation/`) and of the translated enums toward the frontend.
- **Observability:** §9 metrics exposed (Actuator/Prometheus already present) to monitor coverage, quality, and effectiveness.
- **Tracked developments:** every intervention documented in the `Sviluppi/` folder with dated naming, as per the project rules.

---

## 12. Integration with existing LocalMind modules

| Existing module | Role in the "Local Services & Healthcare" domain |
|------------------|---------------------------------------------|
| `knowledge` / core graph engine | Graph foundation: typed nodes, weighted edges, multi-hop queries |
| `llm` (LlmGatewayService) | Need understanding, GraphRAG, answer generation; **Ollama default** with optional fallback |
| Qdrant (vectorstore) | Semantic search on service and need descriptions; entity resolution |
| MySQL + Flyway | Graph structure, weights, metadata, provenance; migrations (one query per file) |
| `document` (Tika/OCR) | Ingestion of PDFs/flyers and documents uploaded by the user |
| `localmind-batch` | Scheduled ingestion, folder scan, source synchronization jobs |
| `plugin` (PF4J) + extension point | Isolated ingestion connectors (`ServiceDirectoryConnectorExtension`) |
| `marketplace` | Distribution of the "Local Services & Healthcare" module to other entities |
| `auth` | Roles and permissions: citizen (read), operator/curator (edit/moderation), admin |
| `calendar` | Reminders and deadlines (exemption renewals, visit recalls) |
| `messaging` / `email` | Multi-channel access and notification channels (WhatsApp/Telegram, email) |
| `mcp` | Exposure of tools (catalog query, service lookup) to external agents/assistants |
| `automation` | Automatic rules (e.g., "to be verified" marking beyond threshold, gap alerts) |
| `agent` | Agents that orchestrate complex paths (guidance + reminder + notification) |
| `finetuning` | Adaptation of the local model to the bureaucratic/healthcare lexicon of the territory |
| Angular frontend (`servizi` feature) | Catalog editor, service detail sheet, conversational assistant, contributions, filters |
| `common` (events, analytics) | Domain events for graph updates; coverage/effectiveness metrics |

The domain **introduces no new infrastructure**: it reuses MySQL + Qdrant (no Neo4j), the local Ollama AI, the hexagonal architecture (pure domain wired in `DomainConfig`), the plugin system, and the marketplace, respecting the project constraints (local-first, privacy, open source, IT/EN bilingualism, Flyway with a single query per file).
