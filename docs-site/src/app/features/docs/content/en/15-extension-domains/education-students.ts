export const content = `# Education & Students

> Document part of documentation/15-extension-domains/ — guide to LocalMind developments. Date: 2026-06-29.

## 1. What We Solve (Problem & Value)

### 1.1 The Problem of the Modern Student

The student — university student, high schooler, PhD candidate, or self-taught lifelong learner — lives in a paradoxical condition: they have access to an amount of learning material never seen before (slides, PDF handouts, lecture recordings, videos, papers, their own notes and others', textbooks, MOOCs, forums), yet they lack a tool that transforms this fragmented mass into **structured, navigable, and queryable knowledge**. The concrete result is a series of recurring symptoms:

- **Fragmentation of materials.** The professor's slides live on an LMS (Moodle, Google Classroom), notes on Notion or paper, PDFs in messy local folders, videos on YouTube or the university's platforms, formulas on scattered sheets. None of these systems knows about the others.
- **Loss of conceptual connections.** The value of studying is not memorizing isolated facts, but understanding *how concepts connect* — which are the prerequisites of a topic, which ideas derive from which, where a notion seen in one course reappears in another. These connections, today, exist only in the student's head (and vanish with forgetting) or do not exist at all.
- **Non-personalized study.** Every student starts from a different baseline, forgets at different rates, has different goals (passing an exam, mastering deeply, doing a project). The materials, however, are the same for everyone and linear: chapter 1, 2, 3. What is missing is an **adaptive study path** that starts from what the student already knows and takes them where they want to go, respecting the order of prerequisites.
- **Questions without contextual answers.** When the student does not understand a passage, the options are: searching on Google (generic, out-of-context answers), asking a generalist chatbot (which does not know *their* course, *their* slides, *their* notation), or waiting for the professor's office hours. What is missing is a tutor that answers **based on the actual materials of the course** and that can cite the exact slide or paragraph.
- **Lack of a big picture.** After months of study, the student struggles to "see" the discipline as a whole: which are the central nodes, what is peripheral, where the gaps are. A hand-drawn concept map ages immediately and covers only a portion of the syllabus.
- **Inefficient review.** Without a spaced repetition system anchored to the structure of the subject, review is random: you review what you remember (and which therefore needs it less) and neglect what you are forgetting.

### 1.2 Our Answer: the Student's Personal Knowledge Graph

LocalMind, in its evolution into a **universal knowledge graph engine**, addresses exactly this problem. The value proposition for the Education domain is to build, for every student, a **personal knowledge graph**: a weighted network in which the nodes are courses, materials, concepts, exercises, and goals, and the edges represent didactically meaningful relationships — "is prerequisite of", "explains", "elaborates on", "is example of", "contradicts", "reappears in". On top of this graph, three capabilities are grafted:

1. **Automatic organization.** Uploaded materials are ingested, segmented, vectorized on Qdrant, and extracted into concepts that become nodes of the graph, with the connections deduced automatically by the AI and then refined by the student.
2. **GraphRAG tutor on your own material.** The AI (Ollama locally by default) answers questions by navigating the personal graph and retrieving the semantically relevant chunks, always citing the exact source (slide N, paragraph, video minute). No data leaves the student's device, unless explicit consent is given.
3. **Adaptive study paths and smart review.** Starting from the student's knowledge state (what they have already mastered, what they are forgetting) and from a goal (an exam on date X), the system generates a path that respects the order of prerequisites and plans review with spaced repetition anchored to the graph nodes.

### 1.3 Why LocalMind Is the Right Platform

| Student's need | LocalMind feature that satisfies it |
|---|---|
| Privacy of notes, grades, personal gaps | **Local-first**: everything runs on-device/self-hosted; nothing sent to the cloud without consent |
| Zero recurring costs (students have limited budgets) | **Pure open source**, local Ollama AI by default, no paywall |
| Materials in heterogeneous formats | Existing ingestion pipeline (**Tika + Tesseract OCR**), chunking, embedding |
| Answers grounded in *your own* material | **GraphRAG** on personal graph + Qdrant semantic search, with source citation |
| Multiple languages (texts in IT and EN) | **Bilingual IT/EN** platform by design, translated enums |
| Extensibility (plugins for LMS, Anki, etc.) | **PF4J plugin** system + marketplace |

The competitive differentiator compared to existing tools (Obsidian with its plugins, Notion, Recall, NotebookLM) is the combination of three factors that no competitor offers together: **explicit weighted graph + local GraphRAG + total privacy at zero cost**. Obsidian has the graph but the links are manual and unweighted, and the AI is an external plugin often cloud-based; NotebookLM is powerful but cloud-only and without a navigable graph; Recall combines AI and graph but is a closed service. LocalMind unites these worlds while remaining sovereign over the data and free.

## 2. Personas & Target Users

| Persona | Profile | Primary needs | How they use LocalMind |
|---|---|---|---|
| **Giulia, university student (BSc in Computer Science)** | 20 years old, 5-6 exams per session, abundant and disorganized digital material | Prepare for exams in time, understand connections between courses, review well | Uploads slides and notes per course, generates concept maps, uses the tutor on the material, follows the pre-exam review path |
| **Marco, master's student / thesis writer** | 24 years old, reads many papers, must integrate knowledge from different sources | Connect papers and theory, identify gaps, build the theoretical basis of the thesis | Creates a cross-course and cross-paper graph, uses GraphRAG for complex multi-source questions, exports bibliography and maps |
| **Sara, high school student** | 17 years old, preparing for the final exam (maturità), less autonomous | Clear outline per subject, guided review, simple explanations | Uses predefined concept maps, automatic flashcards, a tutor with language adapted to her level |
| **Davide, self-taught / lifelong learner** | 35 years old, a worker studying a new discipline (e.g. data science) | Structured path starting from scratch, management of limited time | Defines a learning goal, the system generates an adaptive path from the collected materials |
| **Prof. Bianchi, lecturer (secondary user)** | Creates or curates materials for her own students | Publish a reference course-graph, see where students get stuck (in aggregated and anonymous form) | Publishes a curated "course-graph" as a shareable module; optionally analyzes aggregated pain points |
| **Study group (3-5 students)** | Students who share a course | Share notes, maps, and questions/answers | Opt-in sharing of sub-graphs and materials on a common self-hosted instance |

The primary and priority user for the MVP is **Giulia / Marco (university student)**: maximum volume of digital material, maximum sophistication of need, maximum willingness to self-host. The other personas drive the evolutions.

## 3. Input Requirements

This section defines in detail **what must be able to enter the system** for the Education domain to work. Inputs are divided into: learning materials, structural metadata, the student's personal data, configuration, and feedback. Every input must be validated at the system boundary ("never trust external data" principle) and treated immutably.

### 3.1 Learning Materials (content to ingest)

| Type of material | Supported formats | Extraction | Notes |
|---|---|---|---|
| Lecture slides | PDF, PPTX | Tika (text) + Tesseract OCR (image-slides, scanned formulas) | Preserve the slide number for precise citation |
| Handouts / textbooks / chapters | PDF, DOCX, EPUB, TXT, Markdown | Tika | Keep chapter/paragraph structure if present |
| Personal notes | Markdown, TXT, DOCX, images of handwritten notes | Tika + OCR (handwriting: best-effort) | Notes have a lower "trust" weight compared to official material |
| Papers / scientific articles | PDF | Tika + metadata extraction (title, authors, DOI, abstract, references) | Important for master's/thesis students |
| Lecture recordings | Audio (MP3, WAV), video (MP4) | Transcription via \`WhisperTranscriptionAdapter\` (already present) | Preserve timestamps for citation to the minute |
| Web pages / online resources | URL (HTML), MOOC | Fetch + text extraction | Save a snapshot for local-first reproducibility |
| Exercises / quizzes / exam papers | PDF, images, text | Tika + OCR | Become "Exercise" nodes connected to concepts |

Cross-cutting requirements on materials:
- **Maximum file size** configurable (reasonable default, e.g. 100 MB) with a clear error message when exceeded.
- **Language** detected automatically (IT/EN and beyond) to choose the embedding model and the tutor's response language.
- **Deduplication**: the system must recognize already-uploaded materials (content hash) to avoid duplicate nodes.
- **Provenance** always tracked: every chunk and every concept must be traceable to the source file and position (slide, page, minute).

### 3.2 Structural Metadata (academic organization)

To give meaning to the materials, metadata is needed that the student provides (or that is deduced and confirmed):

- **Course / subject**: name, code, academic year, lecturer, university/institute, ECTS/credits, language.
- **Topic / module**: internal subdivision of the course (e.g. "Chapter 3 — Search Trees").
- **Material → course**: association of each file with one or more courses/topics.
- **Academic calendar**: exam dates, submission deadlines, scheduled lectures (integratable with the \`calendar\` module).
- **Exam syllabus**: list of topics that "are part of" the exam, to delimit the study path.

### 3.3 Student's Personal Data (knowledge state)

These are the inputs that make the system *personal* and *adaptive*. They must be treated with the utmost confidentiality (local-first, never exfiltrated):

- **Learning goal**: "pass the Calculus I exam on 07/15", or "master backpropagation", with a target level (superficial, operational, mastery).
- **Starting state / self-assessment**: for each concept/topic the student can declare their own level (not seen / seen / understood / mastered). Initially optional; over time deduced from behavior.
- **Available time**: hours/day or hours/week that can be dedicated to study, an essential constraint for path planning.
- **Practice results**: outcomes of flashcards, quizzes, self-checks — they feed the estimate of the knowledge state (knowledge tracing) and thus spaced repetition.
- **Study preferences**: style (examples vs theory, concise vs detailed), response language, tutor difficulty level.

### 3.4 System Configuration

- **LLM provider and model** (default local Ollama; optional cloud with explicit consent), embedding model, interface language (IT/EN).
- **Ingestion sources**: monitored local folders (existing folder watcher), LMS/Drive connectors (evolution, see §6).
- **Privacy/sharing policies**: what is private, what is shareable in a study group, what is publishable as a course-graph.
- **Path parameters**: spaced repetition algorithm, mastery thresholds, planning aggressiveness.

### 3.5 Student Feedback (continuous loop)

- **Corrections to the graph**: add/remove/relabel nodes and edges, confirm or reject the connections suggested by the AI (this feedback feeds the edge weight, §5).
- **Evaluation of the tutor's answers**: thumbs up/down, flagging of out-of-context or ungrounded answers.
- **Mastery marking**: "I understood this concept" / "review", which feeds back into the path and the review.

### 3.6 Input Validation and Rules

- All files go through validation of MIME type, size, and, where applicable, integrity (uncorrupted PDFs).
- The mandatory metadata for a course (at least the name) is required; the rest is progressively enrichable.
- No input is ever mutated in place: every revision (e.g. correction of an edge) creates a new version, preserving the history for audit and for weight computation.
- The student's declarative inputs (self-assessments) are always overwritable and never considered "absolute truth": the system cross-references them with practice data.

## 4. Activity Flow (step-by-step)

The flow describes the end-to-end experience, from onboarding to recurring review. It is designed for the MVP but also indicates the evolution points.

### Phase A — Onboarding and Context Definition

1. **Creation of the study space.** The student logs into LocalMind (existing local-first auth) and creates a new "space" or selects the Education domain. They choose the interface language (IT/EN) and the AI provider (default local Ollama).
2. **Definition of courses.** They create one or more courses by entering the minimum metadata (course name; optionally code, lecturer, exam date). The academic calendar can be imported or entered manually (\`calendar\` integration).
3. **Declaration of the goal (optional but recommended).** They indicate what they want to achieve (e.g. "prepare for the exam by 07/15") and the available time. This enables the adaptive path.

### Phase B — Material Ingestion

4. **Upload.** The student uploads files (direct upload) or points to a local folder to monitor (existing batch folder watcher). Each file is associated with a course/topic.
5. **Extraction and validation.** The system validates (type, size), extracts the text (Tika), applies OCR to images/scans (Tesseract), transcribes audio/video (Whisper). Errors are reported with clear messages; an unreadable file does not block the others.
6. **Segmentation and embedding.** The content is divided into chunks (ChunkingService), each vectorized and indexed on Qdrant, with provenance metadata (file, slide/page, minute). The materials and chunks are persisted on MySQL.
7. **Ingestion confirmation.** The student sees the list of ingested materials, the status (completed/in error), and a first estimate of the identified concepts.

### Phase C — Construction of the Knowledge Graph

8. **Concept extraction.** An AI job analyzes the chunks and proposes the **Concept nodes** (e.g. "recursion", "amortized complexity"), deduplicating synonyms and variants.
9. **Relationship deduction.** The AI proposes the **edges**: prerequisites ("recursion is a prerequisite of trees"), explanations ("slide 12 explains recursion"), elaborations, examples, cross-course recurrences. Every edge is born with an initial confidence weight.
10. **Human review (human-in-the-loop).** The student sees the proposed graph and can confirm, correct, add, or remove nodes and edges. Confirmations increase the edge weight; rejections reduce or eliminate them. This step is crucial for quality and trust.
11. **Interactive visualization.** The student navigates the graph: starting from a concept, expands the neighbors, filters by node/relationship type, highlights the prerequisite paths. The concept map is now alive and always up to date.

### Phase D — Active Study with the GraphRAG Tutor

12. **Question to the tutor.** The student asks a question in natural language (e.g. "why is quicksort O(n²) in the worst case?").
13. **GraphRAG retrieval.** The system identifies the relevant nodes, navigates the graph to gather the relevant sub-graph (concept + prerequisites + examples), and retrieves the semantically closest chunks from Qdrant.
14. **Grounded and cited answer.** The AI (Ollama by default) generates the answer using *only* the student's material when available, **citing the exact sources** (slide, page, minute) and the graph nodes/paths used. The student can open the source with a click.
15. **Feedback.** The student rates the answer (useful / out of context). The feedback refines the weights and flags materials to be enriched.

### Phase E — Adaptive Study Path

16. **Path generation.** Given the goal, the knowledge state, and the available time, the system produces an **ordered path** that respects the prerequisites (a topic is not proposed before its prerequisites) and that starts from what the student does not yet master.
17. **Study sessions.** For each stage the system proposes the relevant materials, a tutor explanation, and a self-check (flashcards/quizzes generated from the concepts).
18. **State update.** The outcomes of the self-checks update the mastery estimate (knowledge tracing): weak concepts remain in the path, solid ones exit.

### Phase F — Review and Maintenance (spaced repetition)

19. **Review planning.** Mastered concepts enter a spaced repetition calendar: they are re-proposed shortly before the estimated moment of forgetting, with priority given to central nodes and to the prerequisites of future topics.
20. **Review sessions.** The student runs short sessions; the outcomes readjust the intervals (longer if they remember, shorter if they forget).
21. **Pre-exam.** As the exam date approaches, the system concentrates the review on the syllabus topics, highlights the residual gaps, and proposes a map-guided "overall review".

### Phase G — Evolution and Sharing (optional)

22. **Incremental update.** Newly uploaded materials extend the graph without rebuilding it; the system proposes the new connections.
23. **Opt-in sharing.** The student can share a sub-graph or a course-graph with a study group (common self-hosted instance) or export it (Markdown/JSON, Anki for the flashcards). Nothing leaves the system without an explicit action.

## 5. Graph Model (node types, relationship types, weighting criteria)

The model reuses the infrastructure of the **core knowledge graph engine** (typed nodes + weighted edges on MySQL for the structure, Qdrant for the semantics). Below are the types specific to the Education domain.

### 5.1 Node Types

| Node type | Description | Key attributes |
|---|---|---|
| **Course** | Subject / discipline | name, code, lecturer, year, ECTS, language |
| **Topic / Module** | Thematic subdivision of a course | title, order, parent course |
| **Concept** | Atomic unit of knowledge | name, short definition, synonyms, level (basic/intermediate/advanced) |
| **Material** | Ingested learning resource | type (slides/handout/paper/video…), title, language, hash, provenance |
| **Chunk / Fragment** | Segment of material (linked to Qdrant vectors) | text, position (slide/page/minute), vector id |
| **Exercise / Quiz** | Problem or verification question | text, solution, difficulty, concepts involved |
| **Flashcard** | Question/answer pair for review | front, back, concept, spaced-repetition state |
| **Learning Goal** | Student's target | description, target level, deadline |
| **Study Path** | Ordered sequence of stages | goal, stage order, status |
| **Exam / Deadline** | Academic event | date, syllabus, course |
| **Person** | Lecturer, author, study mate | name, role |
| **Personal Note** | Annotation/insight of the student | text, linked concept |

### 5.2 Relationship Types (edges)

| Relationship | From → To | Meaning | Directed |
|---|---|---|---|
| **is_prerequisite_of** | Concept → Concept | A requires the mastery of B | Yes |
| **explains** | Material/Chunk → Concept | The resource explains the concept | Yes |
| **elaborates_on** | Concept → Concept | Extends/details a concept | Yes |
| **is_example_of** | Exercise/Chunk → Concept | Applied instance of the concept | Yes |
| **belongs_to** | Concept → Topic → Course | Content hierarchy | Yes |
| **recurs_in** | Concept → Course | Same concept in multiple courses (cross-course) | No |
| **verifies** | Exercise/Quiz/Flashcard → Concept | Measures the mastery of the concept | Yes |
| **contradicts / debates** | Material → Material | Sources with diverging theses | No |
| **derives_from** | Concept → Concept | Logical/theoretical derivation relationship | Yes |
| **covers** | Exam → Topic/Concept | The concept is in the syllabus | Yes |
| **annotates** | Personal Note → Concept/Material | Student's insight | Yes |
| **is_part_of_path** | Concept → Study Path | Stage of a path | Yes |
| **author_of** | Person → Material | Authorship of the resource | Yes |

### 5.3 Criteria for Edge Weighting

The weight (normalized value, e.g. 0–1) expresses the **strength/reliability** of the relationship and guides both the visualization (thicker edges) and the GraphRAG (exploration priority). The weight is computed as a configurable combination of the following factors, consistent with the core principle "weight derived from configurable factors":

| Factor | Effect on weight | Example |
|---|---|---|
| **Confidence of the AI extraction** | Initial base of the edge | The LLM is very confident that X is a prerequisite of Y |
| **Human confirmation** | Strongly increases | The student confirms the connection → high and "stable" weight |
| **Human rejection** | Zeroes out/removes | The student rejects the connection |
| **Co-occurrence in materials** | Increases | Two concepts often appear in the same chunks |
| **Semantic similarity (Qdrant)** | Increases | High vector proximity between the connected contents |
| **Frequency of use in study** | Increases | Path/edge traversed often in sessions |
| **Practice outcomes** | Modulates prerequisites | Getting Y wrong when X is weak strengthens the prerequisite link |
| **Source authoritativeness** | Weights "explains" | Official lecturer material > third-party notes |
| **Recency / decay** | Reduces over time | Unconfirmed and unused connections slowly decay |

Immutability rule: the weight is not mutated in place on the relationship; every re-evaluation produces a new version of the value (with timestamp and contributing factors), so as to be able to explain *why* an edge has that weight (interpretability, a recurring requirement in knowledge tracing research).

## 6. Data Sources & Connectors (ingestion)

| Source | Mode | Status | Notes |
|---|---|---|---|
| **Manual file upload** | Drag&drop / selection | MVP | Reuses \`DocumentController.upload\` and the existing pipeline |
| **Monitored local folders** | Batch folder watcher | MVP | \`LocalFileSystemScanner\` + Spring Batch already present |
| **Lecture audio/video** | Transcription | MVP/early | Existing \`WhisperTranscriptionAdapter\` |
| **Web pages / URL** | Fetch + extraction | Evolution | Local snapshot for reproducibility |
| **LMS (Moodle, Google Classroom, Canvas)** | Plugin connector (PF4J) | Evolution | Synchronization of courses/materials/deadlines |
| **Cloud storage (Google Drive, OneDrive, Nextcloud)** | Plugin connector | Evolution | Import of shared course folders |
| **Reference manager (Zotero, Mendeley)** | Plugin connector | Evolution | For papers, metadata, and citations (thesis/master's students) |
| **Academic calendar** | \`calendar\` module | Early | Exam/deadline dates as Exam nodes |
| **Email (lecturer notices, handouts via mail)** | \`email\` module | Evolution | Extraction of attachments and announcements |
| **Existing Anki / flashcards** | Import/Export | Evolution | Bidirectional for review |

All external connectors go through the **PF4J plugin + marketplace** system, so as not to bloat the core and to respect modularity by domain. Every connector must declare which data it reads and where it ends up, consistently with local-first privacy.

## 7. Features to Create, Develop, and Maintain (MVP → evolution)

### 7.1 MVP (first release of the Education domain)

| # | Feature | What it entails (backend / frontend) | Modules touched |
|---|---|---|---|
| 1 | **\`education\` domain (or extension of \`knowledge\`)** | New node/edge models specific to it, in/out ports, service; wiring in \`DomainConfig\` | domain, infrastructure |
| 2 | **Course and material management** | CRUD of courses/topics; material→course association; \`/api/v1/education/*\` controller; \`education\` UI feature (standalone, Signals) | api, frontend, MySQL (Flyway, one query/file) |
| 3 | **Learning material ingestion** | Reuse of the Tika/OCR/Whisper pipeline + chunking + Qdrant, with provenance metadata (slide/page/minute) | infrastructure, batch |
| 4 | **Concept extraction & edge deduction (AI)** | AI job that produces Concept nodes and edges with confidence weight; deduplicates synonyms | domain, infrastructure (Ollama) |
| 5 | **Human-in-the-loop graph review** | API and UI to confirm/correct nodes and edges; weight update | api, frontend |
| 6 | **Interactive visualization of the personal graph** | Graph view with progressive expansion, filters by node/relationship type, prerequisite highlighting | frontend |
| 7 | **GraphRAG tutor on the material** | Sub-graph + Qdrant chunk retrieval; answer with source citation; integration with the existing chat | domain (knowledge/llm), frontend (chat) |
| 8 | **Basic flashcards and self-checks** | Flashcard generation from concepts; simple quizzes; recording of outcomes | domain, api, frontend |
| 9 | **i18n IT/EN** | All enums (node/edge types, states) translated and routed to the frontend according to the language switch | api, frontend |

### 7.2 Evolutions (subsequent releases)

| # | Feature | Added value |
|---|---|---|
| 10 | **Adaptive study path** | Generation of a path that respects prerequisites, starts from the student's state and the available time |
| 11 | **Knowledge tracing / mastery estimation** | Knowledge state per concept, updated from practice outcomes; interpretable |
| 12 | **Spaced repetition anchored to the graph** | Review planning with priority to central nodes and future prerequisites |
| 13 | **LMS / Drive / Zotero connectors (PF4J plugin)** | Automatic ingestion from external sources, course/deadline synchronization |
| 14 | **Shareable course-graph / marketplace** | Lecturers or students publish curated course-graphs as installable modules |
| 15 | **Study groups (opt-in sharing)** | Sub-graphs and materials shared on a common self-hosted instance |
| 16 | **Anki / Markdown / JSON export** | Interoperability with existing tools |
| 17 | **Gap and missing-connection suggestion** | The AI proposes weak concepts and non-obvious links between courses/papers |
| 18 | **Study analytics (private)** | Time per topic, mastery trend, exam-readiness prediction |

### 7.3 To Maintain (ongoing maintenance)

- Ingestion pipeline (update of Tika parsers, OCR languages, Whisper models).
- GraphRAG prompts and logic (quality of concept extraction and source citation).
- Graph schema and Flyway migrations (one query per file), with backward-compatible evolution.
- IT/EN translations of enums and UI for each new feature.
- Compatibility of plugin connectors with external APIs (LMS, Drive) that change over time.
- Tuning of weight factors and spaced repetition algorithms based on real feedback.

## 8. AI / GraphRAG Use Cases

1. **Tutor grounded in the material.** "Explain to me the difference between BFS and DFS using the course slides." → The AI navigates the Concept nodes (BFS, DFS) and their prerequisites, retrieves the relevant slide chunks, and answers citing "Slide 23, Lecture 7". All locally with Ollama.
2. **Multi-source question (cross-course/paper).** "Where have I already seen the concept of entropy?" → The AI follows the \`recurs_in\` edges and shows that entropy appears in Information Theory and in Machine Learning, connecting the materials of the two courses.
3. **Gap identification.** "Am I ready for the Algorithms exam?" → The AI compares the syllabus (\`covers\`) with the mastery state, identifies the weak concepts and the prerequisites that are not solid, and proposes a recovery plan.
4. **Study path generation.** "I have 10 days and 2 hours a day: how do I prepare?" → GraphRAG builds a path ordered by prerequisites, balanced over the time, starting from the gaps.
5. **Level-adaptive explanation.** The AI adapts the answer to the declared level (high school vs master's), using more or less formal examples present in the material.
6. **Suggestion of non-obvious connections.** The AI proposes: "The concept of recursion (Algorithms) is an implicit prerequisite for dynamic programming (Optimization): do you want to connect them?".
7. **Automatic generation of flashcards and quizzes** from the central concepts of the graph, with calibrated difficulty.
8. **Structured summary of a topic** following the \`belongs_to\` hierarchy and the prerequisites, with precise citations.
9. **Conversational pre-exam review** guided by the map: the AI quizzes the student on the nodes at risk of forgetting and updates the repetition intervals.

## 9. KPIs & Success Metrics

| Category | Metric | Indicative target |
|---|---|---|
| **Adoption** | Materials ingested per active student | Steady growth; ≥ 20 materials/course |
| **Graph quality** | % of suggested edges confirmed by the student | ≥ 60% acceptance, net of corrections |
| **Tutor quality** | % of answers rated useful and grounded (with correct citation) | ≥ 80% thumbs up |
| **Groundedness** | % of answers with at least one verifiable citation | ≥ 95% when the material exists |
| **Study effectiveness** | Improvement in estimated mastery pre/post session | Measurable positive trend |
| **Review** | Adherence to the planned spaced repetition sessions | ≥ 50% sessions completed |
| **Perceived outcomes** | Exam outcome / readiness self-assessment | Positive correlation with use of the path |
| **Performance** | Tutor response latency locally (Ollama) | Acceptable on consumer hardware (target < a few seconds to first token) |
| **Privacy** | Data sent to cloud without consent | Zero (constraint, not a goal) |
| **Retention** | Active students at the end of the exam session | Measure return in the following session |

## 10. Risks & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| **Noisy concept extraction** (wrong or duplicate concepts) | Unreliable graph, mistrust | Mandatory human-in-the-loop in review; deduplication by similarity; confidence thresholds; ability to regenerate |
| **Tutor hallucinations** | Wrong answers, didactic harm | GraphRAG constrained to the material; mandatory citation; "not present in your materials" answer when the source is missing |
| **Local computational cost** (LLM/embedding on the student's hardware) | Slowness, frustration | Lightweight Ollama models by default; background batch; cloud option with consent; caching |
| **OCR quality on handwritten notes** | Illegible material | Best-effort declared; prefer digital material; allow manual correction of the extracted text |
| **Graph cognitive overload** (too many nodes) | Illegible map | Progressive expansion, filters, clustering by topic, focused views |
| **Laziness/curation** (student does not review) | Unrefined graph | Low-friction suggestions (one click to accept/reject); incremental review; sensible default values |
| **Privacy of personal data** (grades, gaps) | Breach of trust | Strict local-first; sharing only opt-in; encryption at rest; no telemetry |
| **Reliability on MySQL+Qdrant for deep graph queries** | Slow paths/prerequisites on large graphs | Targeted indexing, materialization of frequent paths, depth limits; reconsider a dedicated graph only if necessary (project constraint) |
| **Misalignment with the real course syllabus** | Irrelevant path | Anchoring to the provided exam syllabus; student feedback; the lecturer's course-graph as a reference |
| **Multilingualism** (mixed IT/EN materials) | Concepts not connected across languages | Multilingual embedding; cross-language synonym mapping; bilingual enums and UI |

## 11. Maintenance & Evolution

- **Incremental graph update.** New materials extend the existing graph; a periodic job recomputes the candidate connections and proposes additions without destructive rebuilds.
- **Graph decay and hygiene.** Unconfirmed and unused edges decay; periodic routines flag orphan nodes, duplicates, and weak connections to review.
- **Schema versioning.** Every evolution of node/edge types goes through backward-compatible Flyway migrations (one query per file), with a documented backfill strategy.
- **Model and prompt tuning.** Periodic update of the extraction and GraphRAG prompts, of the recommended Ollama models, and of the chunking parameters, guided by the metrics in §9.
- **Calibration of weights and spaced repetition.** The weight factors (§5.3) and the review intervals are refined on real usage data, maintaining interpretability.
- **Connector compatibility.** Monitoring of external APIs (LMS, Drive, Zotero) and update of the corresponding PF4J plugins.
- **Bilingual documentation.** Every feature updates IT/EN documentation and the logs in \`Sviluppi/\` according to project conventions.
- **Evaluation roadmap.** Introduce over time an evaluation set (golden questions per course) to measure regressions in the quality of the tutor and the extraction.

## 12. Integration with Existing LocalMind Modules

| Existing module | Role in the Education domain |
|---|---|
| **knowledge** | Basis of the graph engine: extension with Education node/edge types; the natural point where to graft the domain |
| **document** | Material ingestion (upload, Tika, Tesseract OCR), chunking, provenance metadata |
| **llm** | Tutor and concept extraction via \`LlmGatewayService\`; Ollama default, optional cloud fallback with consent |
| **(Qdrant) vectorstore** | Semantic index of the chunks for GraphRAG retrieval |
| **batch** | Ingestion jobs and folder watcher; periodic graph recomputation jobs |
| **calendar** | Exam/deadline dates as Exam nodes; path and review planning |
| **email** | Ingestion of lecturer notices and attachments (evolution) |
| **mcp** | Exposure of tools (e.g. query the graph, generate flashcards) to external agents |
| **agent** | Tutor-agent that orchestrates graph search, quiz generation, and planning |
| **automation** | Automatic triggers: "new material → extract concepts", "exam in 7 days → intensify review" |
| **marketplace + plugin (PF4J)** | LMS/Drive/Zotero connectors and shareable course-graphs as installable modules |
| **finetuning** | Possible adaptation of local models to the student's domain/lexicon (advanced) |
| **auth** | Local-first identity of the student; separation of personal data |
| **common** | Domain events (e.g. "material ingested", "concept mastered"), private analytics, error handling |
| **Frontend (Angular 21)** | New standalone \`education\` feature with Signal store, interactive graph view, tutor, path, review; i18n IT/EN |

The Education domain is therefore a **consumer vertical** of the universal engine: it entirely reuses the existing infrastructure (ingestion, embedding, LLM, graph, plugins) and adds only the node/relationship types, the study features, and the student-specific user experience — consistent with the "one platform, multiple ecosystems" principle, remaining local-first, free, private, and bilingual.
`;
