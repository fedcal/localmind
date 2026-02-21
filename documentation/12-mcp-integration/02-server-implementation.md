# MCP Server Implementation in LocalMind

**Project:** LocalMind - Local-First AI Platform
**Version:** 0.1.0
**Last updated:** 2026-02-13
**Reference module:** localmind-infrastructure (`infrastructure.mcp.server`)

---

## Table of Contents

1. [Overview](#1-overview)
2. [MCP server architecture](#2-mcp-server-architecture)
3. [Exposed tools](#3-exposed-tools)
4. [Exposed resources](#4-exposed-resources)
5. [Prompt templates](#5-prompt-templates)
6. [Implementation with Spring AI](#6-implementation-with-spring-ai)
7. [Delegation to domain use cases](#7-delegation-to-domain-use-cases)
8. [Source file map](#8-source-file-map)

---

## 1. Overview

LocalMind acts as an **MCP Server**, exposing its RAG knowledge base and multi-provider
LLM gateway through the MCP protocol. This allows any compatible MCP client (Claude Desktop,
other AI agents, IDEs with MCP support) to use LocalMind's capabilities as external tools.

The server exposes **135 native tools** distributed across **12 @Tool classes**, organized by
functional domain: core AI, utility, code, test, DevOps, database, documentation, project
management, communication, governance, operations, and quality.

```
+------------------------------------------------------+
|                   LocalMind Backend                   |
|                                                       |
|  12 @Tool classes (135 tools total)                   |
|  +--------------------+    +-----------------------+  |
|  | LocalMindMcpTools  |--->| DocumentSearchUseCase |  |
|  | LocalMindCodeTools |--->| CodeReviewUseCase     |  |
|  | LocalMindTestTools |--->| TestGeneratorUseCase  |  |
|  | ... (9 more)       |--->| ... (other use cases) |  |
|  +--------------------+    +-----------------------+  |
|  +--------------------+         Domain Layer          |
|  | LocalMindMcpRes.   |                               |
|  +--------------------+                               |
|  +--------------------+                               |
|  | LocalMindMcpPrompts|                               |
|  +--------------------+                               |
|         |                                             |
|         v                                             |
|  Spring AI MCP Server WebMVC                          |
|  (spring-ai-starter-mcp-server-webmvc)                |
+------------------------------------------------------+
         |
         | HTTP/SSE (JSON-RPC 2.0)
         v
  External MCP Client (Claude Desktop, IDE, etc.)
```

The MCP server is enabled by the property `localmind.mcp.server.enabled=true` (default: `true`)
and is configured in `application-dev.yml`:

```yaml
spring:
  ai:
    mcp:
      server:
        name: localmind
        version: 0.1.0
```

---

## 2. MCP server architecture

The LocalMind MCP server is composed of 12 tool classes plus resource and prompt classes,
all in the package `com.localmind.infrastructure.mcp.server`:

| Class                         | Responsibility                                       | Tools | MCP Primitive |
|-------------------------------|------------------------------------------------------|-------|---------------|
| `LocalMindMcpTools`           | Core AI: RAG search, LLM chat, model listing         | 3     | Tool          |
| `LocalMindUtilityTools`       | Regex, HTTP client, code snippets                    | 13    | Tool          |
| `LocalMindCodeTools`          | Code review, dependency analysis, scaffolding        | 9     | Tool          |
| `LocalMindTestTools`          | Test generation, performance analysis                | 6     | Tool          |
| `LocalMindDevOpsTools`        | Docker, log analysis, CI/CD                          | 12    | Tool          |
| `LocalMindDatabaseTools`      | DB schema exploration, mock data generation          | 8     | Tool          |
| `LocalMindDocTools`           | API documentation, codebase knowledge                | 8     | Tool          |
| `LocalMindProjectTools`       | Scrum board, agile metrics, time tracking, economics | 31    | Tool          |
| `LocalMindCommTools`          | Standup notes, environment management                | 8     | Tool          |
| `LocalMindGovernanceTools`    | Access policies, decision log                        | 10    | Tool          |
| `LocalMindOpsTools`           | Incident management, workflow orchestration          | 11    | Tool          |
| `LocalMindQualityTools`       | Quality gates, insight engine, dashboard, MCP registry | 16  | Tool          |
| `LocalMindMcpResources`       | Readable resources (provider config)                 | -     | Resource      |
| `LocalMindMcpPrompts`         | Parameterized prompt templates                       | -     | Prompt        |
| **Total**                     |                                                      | **135** |             |

All tool classes are annotated with:
- `@Component` - automatic registration in the Spring context
- `@ConditionalOnProperty(name = "localmind.mcp.server.enabled", havingValue = "true", matchIfMissing = true)`

The `matchIfMissing = true` flag ensures the MCP server is active by default.

---

## 3. Exposed tools

### 3.1 Core AI - `LocalMindMcpTools` (3 tools)

Fundamental tools for interacting with the RAG knowledge base and the multi-provider LLM gateway.

| # | Method | Description |
|---|--------|-------------|
| 1 | `documentSearch(query, topK)` | Search documents in the LocalMind knowledge base using RAG. Returns relevant chunks with similarity scores. |
| 2 | `chat(message, provider, model, temperature, conversationId)` | Send a message to an LLM through the multi-provider gateway. Supports multi-turn conversations via conversationId. Providers: OLLAMA, OPENAI, ANTHROPIC, GOOGLE. |
| 3 | `listModels()` | List available LLM providers and their status in LocalMind. |

**Domain use cases:** `DocumentSearchUseCase`, `ChatUseCase`, `ConversationService`

---

### 3.2 Utility - `LocalMindUtilityTools` (13 tools)

General-purpose tools for regex, HTTP requests, and code snippet management.

#### Regex Builder (5 tools)

| # | Method | Description |
|---|--------|-------------|
| 1 | `testRegex(pattern, testStrings, flags)` | Test a regex pattern against a list of strings. Returns match results with captured groups. |
| 2 | `explainRegex(pattern)` | Explain a regex pattern in natural language, component by component. |
| 3 | `buildRegex(description)` | Build a regex pattern from a description or keyword (email, url, ipv4, uuid, etc.). |
| 4 | `optimizeRegex(pattern)` | Analyze a regex pattern and suggest optimizations (redundant classes, unnecessary groups, greedy quantifiers). |
| 5 | `convertRegex(pattern, toFormat)` | Convert a regex pattern between formats: java, python, javascript, pcre. |

#### HTTP Client (3 tools)

| # | Method | Description |
|---|--------|-------------|
| 6 | `sendHttpRequest(url, method, headers, body, queryParams, timeoutMs)` | Send an HTTP request and return status code, headers, body, and response time in ms. |
| 7 | `compareHttpResponses(baselineUrl, currentUrl, method)` | Compare HTTP responses from two URLs. Shows differences in status code, time, body, and headers. |
| 8 | `generateCurl(method, url, headers, body)` | Generate an equivalent cURL command from HTTP request parameters. |

#### Snippet Manager (5 tools)

| # | Method | Description |
|---|--------|-------------|
| 9 | `saveSnippet(title, code, language, description, tags)` | Save a reusable code snippet with metadata (title, language, description, tags). |
| 10 | `searchSnippets(keyword, tag, language)` | Search snippets by keyword, tag, or programming language. Filters can be combined. |
| 11 | `getSnippet(id)` | Retrieve a specific code snippet by its ID. |
| 12 | `deleteSnippet(id)` | Delete a code snippet by its ID. |
| 13 | `listSnippetTags()` | List all tags used across code snippets with their usage count. |

**Domain use cases:** `RegexUseCase`, `HttpClientUseCase`, `SnippetUseCase`

---

### 3.3 Code - `LocalMindCodeTools` (9 tools)

Tools for code review, dependency analysis, and project scaffolding.

#### Code Review (3 tools)

| # | Method | Description |
|---|--------|-------------|
| 1 | `analyzeDiff(diff)` | Analyze a git diff string for common issues: console.log, TODO, debugger, hardcoded credentials, empty catch blocks. |
| 2 | `checkComplexity(code, language)` | Calculate cyclomatic complexity of a code snippet. Returns score, rating, and breakdown. Languages: java, python, javascript, typescript, rust. |
| 3 | `suggestImprovements(code, language)` | Suggest improvements: magic numbers, long functions (>30 lines), deep nesting (>4 levels), duplicate patterns, unused variables. |

#### Dependency Manager (3 tools)

| # | Method | Description |
|---|--------|-------------|
| 4 | `checkVulnerabilities(projectPath, projectType)` | Scan project dependencies for known vulnerabilities. Supports Maven (pom.xml) and npm (package.json). |
| 5 | `findUnusedDependencies(projectPath, projectType)` | Find declared dependencies not imported or used in source code. |
| 6 | `licenseAudit(projectPath, projectType)` | Audit dependency licenses, flagging copyleft licenses (GPL, AGPL, LGPL, MPL-2.0). |

#### Project Scaffolding (3 tools)

| # | Method | Description |
|---|--------|-------------|
| 7 | `listProjectTemplates()` | List available templates: spring-boot-api, angular-app, maven-multi-module, mcp-server, react-app. |
| 8 | `scaffoldProject(template, projectName, outputDir, author, description, license)` | Generate a complete project structure from a template with placeholder substitution. |
| 9 | `scaffoldComponent(name, type, language, outputDir)` | Generate a single component/service/controller/model file. |

**Domain use cases:** `CodeReviewUseCase`, `DependencyAnalysisUseCase`, `ProjectScaffoldingUseCase`

---

### 3.4 Test & Performance - `LocalMindTestTools` (6 tools)

Tools for test generation and performance profiling.

#### Test Generator (3 tools)

| # | Method | Description |
|---|--------|-------------|
| 1 | `generateUnitTests(code, language, framework)` | Generate unit test skeletons from source code. Supports JUnit (Java), Vitest (TS/JS), Pytest (Python). |
| 2 | `findEdgeCases(code)` | Analyze code to identify edge cases: null/empty values, boundary conditions, division by zero, async errors, I/O failures. |
| 3 | `analyzeCoverage(sourceCode, testCode)` | Analyze test coverage by matching function names between source code and test code. |

#### Performance Profiler (3 tools)

| # | Method | Description |
|---|--------|-------------|
| 4 | `analyzeBundle(code, filePath)` | Analyze source code imports for bundle size impact. Detects heavy dependencies (moment.js, lodash, aws-sdk) and suggests alternatives. |
| 5 | `findBottlenecks(code, language)` | Static analysis for performance anti-patterns: nested loops O(n^2), sync I/O, linear search in loops, missing pagination. |
| 6 | `benchmarkCompare(codeA, codeB, iterations, language)` | Generate a ready-to-run benchmark template comparing two snippets. Includes warmup, measurement, and statistical analysis. |

**Domain use cases:** `TestGeneratorUseCase`, `PerformanceProfilerUseCase`

---

### 3.5 DevOps - `LocalMindDevOpsTools` (12 tools)

Tools for Docker analysis, log analysis, and CI/CD monitoring.

#### Docker Analysis (4 tools)

| # | Method | Description |
|---|--------|-------------|
| 1 | `parseCompose(content)` | Parse docker-compose YAML and extract services, networks, volumes. Performs validation checks (:latest tags, privileged mode, etc.). |
| 2 | `analyzeDockerfile(content)` | Analyze a Dockerfile for best-practice violations: :latest tags, large base images, consecutive RUN instructions, ADD vs COPY, missing HEALTHCHECK. |
| 3 | `listDockerServices()` | List common Docker services with metadata: name, image, default ports, and description. |
| 4 | `generateCompose(services)` | Generate docker-compose YAML from a list of service definitions. |

#### Log Analyzer (4 tools)

| # | Method | Description |
|---|--------|-------------|
| 5 | `analyzeLogFile(content, format)` | Analyze log file content: count log levels (INFO, WARN, ERROR, DEBUG), top errors, time range, detected format. |
| 6 | `findErrorPatterns(content, minCount)` | Find recurring error patterns in logs by normalizing and grouping by pattern. Sorted by frequency. |
| 7 | `tailLog(content, lines, filter)` | Return the last N lines of log content, with optional case-insensitive keyword filter. Simulates Unix `tail` with grep. |
| 8 | `generateLogSummary(content)` | Generate a human-readable log summary: total lines, level distribution, error and warning rates. |

#### CI/CD Monitor (4 tools)

| # | Method | Description |
|---|--------|-------------|
| 9 | `listPipelines(owner, repo)` | List all saved pipeline runs for a GitHub repository. |
| 10 | `getPipelineStatus(runId)` | Retrieve status and details of a specific pipeline run by runId. |
| 11 | `savePipelineRun(owner, repo, runId, title, branch, status, conclusion, workflow, url)` | Save a pipeline run record to the local database for tracking. |
| 12 | `detectFlakyTests(runs)` | Analyze test run results to detect flaky tests. Calculates flakiness rate and persists results. |

**Domain use cases:** `DockerAnalysisUseCase`, `LogAnalyzerUseCase`, `CicdMonitorUseCase`

---

### 3.6 Database - `LocalMindDatabaseTools` (8 tools)

Tools for database schema exploration and mock data generation.

#### DB Schema Explorer (4 tools)

| # | Method | Description |
|---|--------|-------------|
| 1 | `exploreSchema(jdbcUrl)` | Explore the full database schema via JDBC metadata. Lists all tables with columns and primary keys. Supports MySQL, H2, SQLite. |
| 2 | `describeTable(jdbcUrl, tableName)` | Describe a single table in detail: columns with types and nullability, indexes, foreign keys, approximate row count. |
| 3 | `suggestIndexes(jdbcUrl)` | Analyze the schema and suggest missing indexes, particularly on foreign key columns that are not indexed. |
| 4 | `generateErd(jdbcUrl)` | Generate a Mermaid erDiagram from the database schema, including tables, columns, and FK relationships. |

#### Data Mock Generator (4 tools)

| # | Method | Description |
|---|--------|-------------|
| 5 | `generateMockData(schema, count, name)` | Generate mock data based on a typed field schema. Available types: firstName, lastName, email, phone, address, company, date, integer, float, boolean, uuid, sentence, paragraph, url, ipv4, hexColor. Max 10000 rows. |
| 6 | `generateMockJson(jsonSchema, count, name)` | Generate mock data from a JSON Schema definition with 'properties' defining types and formats. |
| 7 | `generateMockCsv(columns, count, delimiter, name)` | Generate mock data in CSV format with configurable columns, count, and delimiter. |
| 8 | `listMockGenerators()` | List all available mock data generators with their name and description. |

**Domain use cases:** `DbSchemaExplorerUseCase`, `DataMockGeneratorUseCase`

---

### 3.7 Documentation - `LocalMindDocTools` (8 tools)

Tools for API documentation and codebase knowledge.

#### API Documentation (3 tools)

| # | Method | Description |
|---|--------|-------------|
| 1 | `extractEndpoints(filePath)` | Extract REST API endpoints from a source file. Supports Spring MVC, Express, and NestJS. |
| 2 | `generateOpenApi(endpoints, title, version)` | Generate an OpenAPI 3.0.3 specification skeleton from a list of endpoints. Includes operationId, tags, path parameters, request body, and standard responses. |
| 3 | `findUndocumented(filePath)` | Find undocumented exports and public declarations. Checks for JavaDoc/JSDoc before functions, classes, interfaces, types, enums. |

#### Codebase Knowledge (5 tools)

| # | Method | Description |
|---|--------|-------------|
| 4 | `searchCode(directory, pattern, fileExtensions, maxResults)` | Search for a pattern (string or regex) across files in a directory. Skips non-source directories (node_modules, .git, dist, target). |
| 5 | `explainModule(filePath)` | Analyze a source file and provide a structural summary: imports, exports, functions, classes, interfaces, type aliases. |
| 6 | `architectureMap(directory, maxDepth)` | Generate an architecture map (text tree) of a directory with file counts and types. |
| 7 | `dependencyGraph(directory)` | Create a dependency graph between internal modules by analyzing import/require statements. Generates a Mermaid diagram. |
| 8 | `trackChanges(modulePath, changeType, description, filesChanged, author, commitRef, historyLimit)` | Track changes to codebase modules over time, or view change history. Types: feature, bugfix, refactor, dependency-update, performance, security. |

**Domain use cases:** `ApiDocumentationUseCase`, `CodebaseKnowledgeUseCase`

---

### 3.8 Project Management - `LocalMindProjectTools` (31 tools)

The largest tool set, covering scrum board, agile metrics, time tracking, project economics, and retrospectives.

#### Scrum Board (7 tools)

| # | Method | Description |
|---|--------|-------------|
| 1 | `createSprint(name, startDate, endDate, goals)` | Create a new sprint with name, date range, and goals. |
| 2 | `createStory(title, description, acceptanceCriteria, storyPoints, priority, sprintId)` | Create a new user story, optionally assigning it to a sprint. |
| 3 | `createTask(title, description, storyId, assignee)` | Create a new task under a user story. Initial status: 'todo'. |
| 4 | `updateTaskStatus(taskId, status)` | Update a task's status. Statuses: todo, in_progress, in_review, done, blocked. |
| 5 | `getSprint(sprintId)` | Retrieve a sprint with its user stories and tasks. |
| 6 | `sprintBoard(sprintId)` | View the sprint board with tasks organized in columns by status. |
| 7 | `getBacklog()` | Retrieve the product backlog: user stories not assigned to any sprint. |

#### Agile Metrics (6 tools)

| # | Method | Description |
|---|--------|-------------|
| 8 | `calculateVelocity(sprints)` | Calculate team velocity from sprint completion data. Returns average, trend, highest, lowest. |
| 9 | `generateBurndown(totalPoints, sprintDays, dailyProgress)` | Generate burndown chart data with ideal vs actual lines. |
| 10 | `calculateCycleTime(tasks)` | Calculate cycle time statistics from task start/completion dates. Returns average, median, p95, min, max. |
| 11 | `forecastCompletion(remainingPoints, velocityHistory)` | Monte Carlo simulation to forecast sprints needed to complete remaining work. 1000 simulations, returns p50, p85, p95. |
| 12 | `predictRisk(sprintId)` | Predict sprint risk level based on historical velocity and completion data. |
| 13 | `correlateFactors(factorA, factorB, correlation, sampleSize, description)` | Record or view correlations between velocity and external factors. |

#### Time Tracking (6 tools)

| # | Method | Description |
|---|--------|-------------|
| 14 | `startTimer(taskId, description)` | Start a timer to track time spent on a task. |
| 15 | `stopTimer()` | Stop the active timer and save as a time entry. |
| 16 | `logTime(taskId, durationMinutes, description, date)` | Manually log time spent on a task. |
| 17 | `getTimesheet(startDate, endDate, userId)` | Retrieve time entries and totals for a date range. |
| 18 | `detectAnomalies(userId, days)` | Detect anomalous time tracking patterns: excessive hours, weekend work, duplicate entries. |
| 19 | `estimateVsActual(taskId, estimateMinutes, description)` | Compare time estimates with actual time spent on a task. |

#### Project Economics (5 tools)

| # | Method | Description |
|---|--------|-------------|
| 20 | `setBudget(projectName, totalBudget, currency)` | Set or update the total budget for a project. |
| 21 | `logCost(projectName, category, amount, costDescription, date)` | Log a cost entry against a project budget. Categories: development, infrastructure, design, testing, management, other. |
| 22 | `getBudgetStatus(projectName)` | Get current budget status: total, spent, remaining, percentage, and breakdown by category. |
| 23 | `forecastBudget(projectName)` | Forecast when the project budget will be exhausted based on historical burn rate. |
| 24 | `costPerFeature(featureId, projectName, hoursSpent, hourlyRate, featureDescription, currency)` | Track cost per feature/ticket. |

#### Retrospective Manager (7 tools)

| # | Method | Description |
|---|--------|-------------|
| 25 | `createRetro(sprintId, format)` | Create a new retrospective session. Formats: mad-sad-glad, 4ls, start-stop-continue. |
| 26 | `addRetroItem(retroId, category, content)` | Add an item to a retrospective in a specific category. |
| 27 | `voteRetroItem(itemId)` | Vote on a retrospective item to increase its priority. |
| 28 | `generateActionItems(retroId, topN)` | Generate action items from the top-voted retrospective items. |
| 29 | `getRetro(retroId)` | Retrieve the complete retrospective with all items and action items. |
| 30 | `detectPatterns()` | Analyze recurring themes and patterns across all retrospectives. |
| 31 | `suggestItems(limit)` | Get auto-generated retrospective item suggestions based on common themes. |

**Domain use cases:** `ScrumBoardUseCase`, `AgileMetricsUseCase`, `TimeTrackingUseCase`, `ProjectEconomicsUseCase`, `RetrospectiveUseCase`

---

### 3.9 Communication - `LocalMindCommTools` (8 tools)

Tools for daily standup notes and environment management.

#### Standup Notes (3 tools)

| # | Method | Description |
|---|--------|-------------|
| 1 | `logStandup(yesterday, today, blockers)` | Log a daily standup entry with yesterday's work, today's plan, and blockers. |
| 2 | `getStandupHistory(days)` | Get standup history for the last N days. |
| 3 | `generateStatusReport(days)` | Generate a status report aggregating standups over a period. |

#### Environment Manager (5 tools)

| # | Method | Description |
|---|--------|-------------|
| 4 | `listEnvironments(directory, recursive)` | List environment files (.env) in a directory. |
| 5 | `getEnvVars(filePath, fileContent, showSecrets, filter)` | Parse and display environment variables from .env file content. Masks sensitive values (PASSWORD, SECRET, KEY, TOKEN). |
| 6 | `compareEnvironments(filePathA, contentA, filePathB, contentB, showValues)` | Compare two sets of environment variables. Shows variables only in A, only in B, with different values, and common variables. |
| 7 | `validateEnv(envContent, templateContent, strict)` | Validate a .env file against a template. Returns missing, extra, empty values, and errors. |
| 8 | `generateEnvTemplate(sourceContent, preserveDefaults)` | Generate an .env template from source content, stripping secret values. |

**Domain use cases:** `StandupUseCase`, `EnvironmentManagerUseCase`

---

### 3.10 Governance - `LocalMindGovernanceTools` (10 tools)

Tools for access policies and architectural decision logging.

#### Access Policy (5 tools)

| # | Method | Description |
|---|--------|-------------|
| 1 | `createPolicy(name, effect, rulesJson)` | Create a new access policy with allow/deny effect and JSON rules. |
| 2 | `checkAccess(userId, server, tool)` | Check whether a user has access to a specific MCP server/tool. |
| 3 | `listPolicies()` | List all defined access policies. |
| 4 | `assignRole(userId, roleName)` | Assign a role to a user by creating an allow policy. |
| 5 | `auditAccess(userId, server, limit)` | Retrieve access audit entries for a user, optionally filtered by server. |

#### Decision Log (5 tools)

| # | Method | Description |
|---|--------|-------------|
| 6 | `recordDecision(title, context, decision, alternativesJson, consequences, status)` | Record a new architectural or technical decision. Statuses: proposed, accepted, deprecated, superseded. |
| 7 | `listDecisions(status, search, limit)` | List decisions with optional filters by status and/or search text. |
| 8 | `getDecision(id)` | Retrieve a single decision with its associated links. |
| 9 | `supersedeDecision(id, supersededById)` | Mark a decision as superseded by another decision. |
| 10 | `linkDecision(decisionId, linkType, targetId, description)` | Create a link between a decision and an external artifact. Types: ticket, commit, impact, related. |

**Domain use cases:** `AccessPolicyUseCase`, `DecisionLogUseCase`

---

### 3.11 Operations - `LocalMindOpsTools` (11 tools)

Tools for incident management and workflow orchestration.

#### Incident Manager (6 tools)

| # | Method | Description |
|---|--------|-------------|
| 1 | `openIncident(title, severity, description, affectedSystemsJson)` | Open a new incident. Severity: critical, high, medium, low. |
| 2 | `updateIncident(id, status, note)` | Update an incident's status and/or add a note to its timeline. Statuses: open, investigating, mitigating, resolved, postmortem. |
| 3 | `addTimelineEntry(incidentId, description, source)` | Add a timeline entry to an existing incident. |
| 4 | `resolveIncident(id, resolution, rootCause)` | Resolve an incident, recording the resolution and root cause. |
| 5 | `generatePostmortem(id)` | Generate a post-mortem report in Markdown format for an incident. Includes details, timeline, resolution, root cause, and suggested action items. |
| 6 | `listIncidents(status, severity, limit)` | List incidents with optional filters for status and severity. |

#### Workflow Orchestrator (5 tools)

| # | Method | Description |
|---|--------|-------------|
| 7 | `createWorkflow(name, description, triggerEvent, stepsJson)` | Create a new workflow definition with trigger event and steps. Active by default. |
| 8 | `listWorkflows(activeFilter)` | List all workflow definitions with optional active/inactive filter. |
| 9 | `triggerWorkflow(workflowId, payloadJson)` | Trigger execution of a workflow. Simulates step execution and returns run results. |
| 10 | `getWorkflowRun(runId)` | Get details of a specific workflow run. |
| 11 | `toggleWorkflow(workflowId, active)` | Toggle a workflow's active/inactive state. |

**Domain use cases:** `IncidentManagerUseCase`, `WorkflowOrchestratorUseCase`

---

### 3.12 Quality - `LocalMindQualityTools` (16 tools)

Tools for quality gates, insight engine, dashboard, and internal MCP registry.

#### Quality Gate (4 tools)

| # | Method | Description |
|---|--------|-------------|
| 1 | `defineGate(name, projectName, checksJson)` | Define a quality gate with metric checks. Each check specifies metric, operator (>=, <=, >, <, ==, !=), and threshold. |
| 2 | `evaluateGate(gateId, metricsJson)` | Evaluate a quality gate against provided metrics. All checks must pass for the gate to pass. |
| 3 | `listGates(projectName)` | List all defined quality gates, optionally filtered by project. |
| 4 | `getGateHistory(gateId, limit)` | Get the evaluation history of a quality gate. |

#### Insight Engine (4 tools)

| # | Method | Description |
|---|--------|-------------|
| 5 | `queryInsight(question)` | Query using natural language to get insights about velocity, budget, sprint, incidents, or quality. |
| 6 | `correlateMetrics(metricsJson, period)` | Correlate multiple metrics to find relationships. Generates correlation analysis (strong/moderate/weak/negligible). |
| 7 | `explainTrend(metric, direction, period)` | Explain an observed trend in a specific metric. Provides analysis of why a metric is increasing, decreasing, or stable. |
| 8 | `healthDashboard()` | Generate an aggregated project health dashboard. Returns overall health (good/warning/critical) and area-level breakdowns. |

#### Dashboard API (4 tools)

| # | Method | Description |
|---|--------|-------------|
| 9 | `getOverview()` | Dashboard overview with aggregated sprint progress, velocity trend, time tracking utilization, and budget status. |
| 10 | `getServerStatus(serverName)` | MCP server status with optional name filter. |
| 11 | `getRecentActivity(limit)` | Recent project activity entries with type, description, and timestamp. |
| 12 | `getProjectSummary(project)` | Comprehensive project summary: sprint status, velocity, budget, incidents, quality metrics. |

#### MCP Internal Registry (4 tools)

| # | Method | Description |
|---|--------|-------------|
| 13 | `registerInternalServer(name, url, transport, capabilitiesJson)` | Register an internal MCP server in the in-memory registry. |
| 14 | `discoverServers(status, transport)` | Discover registered MCP servers, optionally filtered by status and/or transport. |
| 15 | `mcpHealthCheck(serverId)` | Perform a health check on a registered MCP server. |
| 16 | `getServerCapabilities(serverId)` | Get the capabilities of a registered MCP server. |

**Domain use cases:** `QualityGateUseCase`, `InsightEngineUseCase`, `DashboardUseCase`, `McpInternalRegistryUseCase`

---

### Overall tool summary

| Category | Class | Tools | Domain Use Cases |
|----------|-------|-------|------------------|
| Core AI | `LocalMindMcpTools` | 3 | `DocumentSearchUseCase`, `ChatUseCase`, `ConversationService` |
| Utility | `LocalMindUtilityTools` | 13 | `RegexUseCase`, `HttpClientUseCase`, `SnippetUseCase` |
| Code | `LocalMindCodeTools` | 9 | `CodeReviewUseCase`, `DependencyAnalysisUseCase`, `ProjectScaffoldingUseCase` |
| Test & Performance | `LocalMindTestTools` | 6 | `TestGeneratorUseCase`, `PerformanceProfilerUseCase` |
| DevOps | `LocalMindDevOpsTools` | 12 | `DockerAnalysisUseCase`, `LogAnalyzerUseCase`, `CicdMonitorUseCase` |
| Database | `LocalMindDatabaseTools` | 8 | `DbSchemaExplorerUseCase`, `DataMockGeneratorUseCase` |
| Documentation | `LocalMindDocTools` | 8 | `ApiDocumentationUseCase`, `CodebaseKnowledgeUseCase` |
| Project Management | `LocalMindProjectTools` | 31 | `ScrumBoardUseCase`, `AgileMetricsUseCase`, `TimeTrackingUseCase`, `ProjectEconomicsUseCase`, `RetrospectiveUseCase` |
| Communication | `LocalMindCommTools` | 8 | `StandupUseCase`, `EnvironmentManagerUseCase` |
| Governance | `LocalMindGovernanceTools` | 10 | `AccessPolicyUseCase`, `DecisionLogUseCase` |
| Operations | `LocalMindOpsTools` | 11 | `IncidentManagerUseCase`, `WorkflowOrchestratorUseCase` |
| Quality | `LocalMindQualityTools` | 16 | `QualityGateUseCase`, `InsightEngineUseCase`, `DashboardUseCase`, `McpInternalRegistryUseCase` |
| **Total** | **12 classes** | **135** | **26 use cases** |

---

## 4. Exposed resources

Resources are implemented in `LocalMindMcpResources.java`. Currently, resources are
registered programmatically via `McpConfiguration` since support for `@McpResource`
annotations depends on the Spring AI version.

### 4.1 `config://providers`

**URI:** `config://providers`
**MIME Type:** `application/json`
**Description:** Returns the complete configuration of available LLM providers.

```json
{
  "providers": [
    {"name": "OLLAMA", "local": true, "defaultModel": "llama3.2"},
    {"name": "OPENAI", "local": false, "defaultModel": "gpt-4o"},
    {"name": "ANTHROPIC", "local": false, "defaultModel": "claude-sonnet-4-20250514"},
    {"name": "GOOGLE", "local": false, "defaultModel": "gemini-pro"}
  ],
  "defaultProvider": "OLLAMA"
}
```

### 4.2 `document://{id}` (planned)

**URI:** `document://{id}`
**MIME Type:** `text/plain`
**Description:** Returns the complete content of an indexed document, identified
by its UUID. This resource will be implemented when document retrieval by ID is
available in the domain layer.

---

## 5. Prompt templates

Prompt templates are implemented in `LocalMindMcpPrompts.java` and provide predefined
patterns for common interactions with the knowledge base.

### 5.1 `rag-query`

**Description:** Query with precompiled RAG context. Combines semantic search results
with the user's query in a structured prompt.

**Parameters:**
- `query` (String) - The user's question
- `context` (String) - The context extracted from the knowledge base

### 5.2 `summarize-document`

**Description:** Generates a structured summary of a document.

**Parameters:**
- `content` (String) - The content of the document to summarize

### Prompt summary

| Prompt                | Parameters         | Use case                                |
|-----------------------|--------------------|-----------------------------------------|
| `rag-query`           | query, context     | Q&A with context from the knowledge base |
| `summarize-document`  | content            | Automatic document summary               |

---

## 6. Implementation with Spring AI

### 6.1 `@Tool` annotation

Spring AI 1.0.0 provides the `@Tool` annotation to declare methods as MCP tools.
The annotation automatically generates the JSON schema for parameters and registers
the tool in the MCP server.

```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

@Tool(description = "Tool description for the LLM model")
public ReturnType methodName(
    @ToolParam(description = "Parameter description") ParamType param) {
    // implementation
}
```

### 6.2 Automatic registration

The `spring-ai-starter-mcp-server-webmvc` starter automatically scans all beans
containing `@Tool` annotated methods and registers them as MCP tools. The configuration
in `application-dev.yml` sets the server name and version:

```yaml
spring:
  ai:
    mcp:
      server:
        name: localmind        # MCP server name
        version: 0.1.0         # Server version
```

### 6.3 Exposed endpoints

With the WebMVC starter, the MCP server automatically exposes:

| Endpoint          | Method | Description                           |
|-------------------|--------|---------------------------------------|
| `/mcp/sse`        | GET    | SSE stream for server->client notifications |
| `/mcp/message`    | POST   | JSON-RPC messages client->server      |

These endpoints are separate from the LocalMind REST APIs (`/api/v1/*`).

---

## 7. Delegation to domain use cases

A key principle of LocalMind's hexagonal architecture is that MCP server components
(in the `infrastructure` module) delegate business logic to **domain use cases**:

```
+-------------------------------------+       +-----------------------------+
|   Infrastructure Layer (12 classes) |       |     Domain Layer            |
|                                     |       |                             |
| LocalMindMcpTools                   |       | DocumentSearchUseCase       |
|   .documentSearch() ----------------|------>|   .search(query, topK)      |
|   .chat() --------------------------|------>| ChatUseCase                 |
| LocalMindCodeTools                  |       | CodeReviewUseCase           |
|   .analyzeDiff() -------------------|------>|   .analyzeDiff(diff)        |
| LocalMindProjectTools               |       | ScrumBoardUseCase           |
|   .createSprint() ------------------|------>|   .createSprint(...)        |
| ... (9 more classes)                |       | ... (other use cases)       |
+-------------------------------------+       +-----------------------------+
```

This ensures that:
1. Business logic remains in the domain, not in infrastructure
2. MCP tools are a simple facade (adapter) to the domain
3. Testability is guaranteed by injecting use case mocks
4. Adding new tools is simple: just add a `@Tool` method that delegates
5. The 12 tool classes are organized by functional domain, improving maintainability

---

## 8. Source file map

```
localmind-infrastructure/
  src/main/java/com/localmind/infrastructure/mcp/
    server/
      LocalMindMcpTools.java            # @Tool (3): document_search, chat, list_models
      LocalMindUtilityTools.java        # @Tool (13): regex, http client, snippet manager
      LocalMindCodeTools.java           # @Tool (9): code review, dependency analysis, scaffolding
      LocalMindTestTools.java           # @Tool (6): test generator, performance profiler
      LocalMindDevOpsTools.java         # @Tool (12): docker analysis, log analyzer, CI/CD monitor
      LocalMindDatabaseTools.java       # @Tool (8): DB schema explorer, data mock generator
      LocalMindDocTools.java            # @Tool (8): API documentation, codebase knowledge
      LocalMindProjectTools.java        # @Tool (31): scrum, agile metrics, time, economics, retro
      LocalMindCommTools.java           # @Tool (8): standup notes, environment manager
      LocalMindGovernanceTools.java     # @Tool (10): access policy, decision log
      LocalMindOpsTools.java            # @Tool (11): incident manager, workflow orchestrator
      LocalMindQualityTools.java        # @Tool (16): quality gate, insight, dashboard, MCP registry
      LocalMindMcpResources.java        # Resources: config://providers
      LocalMindMcpPrompts.java          # Prompts: rag-query, summarize-document
    config/
      McpConfiguration.java             # Bean definitions for MCP client/server
```

**Maven dependencies (localmind-infrastructure/pom.xml):**

```xml
<!-- MCP Server (WebMVC) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

---

> **Documentation navigation:**
> - Previous: [01-mcp-protocol-overview.md](01-mcp-protocol-overview.md)
> - Next: [03-client-implementation.md](03-client-implementation.md)
> - Configuration: [04-configuration.md](04-configuration.md)
