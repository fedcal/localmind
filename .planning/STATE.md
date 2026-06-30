# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-06-29)

**Core value:** L'AI naviga un grafo pesato di conoscenza per rispondere a domande complesse e far emergere collegamenti non evidenti, in qualsiasi dominio, restando local-first.
**Current focus:** Phase 1 — Schema Foundation

## Current Position

Phase: 1 of 10 (Schema Foundation)
Plan: 0 of TBD in current phase
Status: Ready to plan
Last activity: 2026-06-29 — Roadmap created; 10 phases defined covering all 41 v1 requirements

Progress: [░░░░░░░░░░] 0%

## Performance Metrics

**Velocity:**
- Total plans completed: 0
- Average duration: —
- Total execution time: 0 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| — | — | — | — |

**Recent Trend:**
- Last 5 plans: —
- Trend: —

*Updated after each plan completion*

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- Init: Extend `knowledge` domain — no parallel graph domain (avoids MODULE_BOUNDARIES violations)
- Init: MySQL 8 WITH RECURSIVE CTE replaces Java-side BFS in JdbcKnowledgeGraphAdapter
- Init: Separate Qdrant collection `localmind_graph_nodes` (never mix with document chunk collection)
- Init: Node/relation type vocabulary as VARCHAR strings validated by NodeTypeRegistry, not Java enums
- Init: Outbox table (graph_sync_event) in Phase 1 schema — dual-store desync mitigated from day one
- Init: privacyLevel in Phase 1 schema — GDPR risk if retrofitted after enterprise data ingestion
- Init: Phases 3 and 4 are independent of each other (both depend only on Phase 1); parallelisable

### Pending Todos

None yet.

### Blockers/Concerns

- Phase 2 planning: validate FIND_IN_SET vs JSON_CONTAINS performance for CTE cycle detection; design composite indexes (source_id, weight) and (target_id, weight)
- Phase 4 planning: research Flyway multi-instance configuration with per-module flyway_schema_history tables (non-standard — needs validation against existing auto-configuration)
- Phase 5 planning: design GraphRagContextBuilder token estimation and pruning heuristics; validate num_ctx per-request override through LlmGatewayService
- Phase 8 planning: Bayesian Laplace smoothing formula and trust score decay model to be decided
- Phase 9 planning: PII detection approach for local-first constraint (regex vs opennlp NER vs small Ollama model); Git connector library (JGit vs CLI subprocess)

## Deferred Items

| Category | Item | Status | Deferred At |
|----------|------|--------|-------------|
| *(none)* | | | |

## Session Continuity

Last session: 2026-06-29
Stopped at: Roadmap created — all 41 v1 requirements mapped to 10 phases; ROADMAP.md, STATE.md written; REQUIREMENTS.md traceability populated
Resume file: None
