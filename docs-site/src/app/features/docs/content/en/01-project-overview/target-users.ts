export const content = `# Target User Analysis

| Field        | Value                           |
|--------------|---------------------------------|
| **Document** | Target User Analysis            |
| **Version**  | 0.1.0                           |
| **Date**     | 2026-02-09                      |
| **Project**  | LocalMind                       |

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [User Macro-Categories](#2-user-macro-categories)
3. [Developers and Technical Teams](#3-developers-and-technical-teams)
4. [Business and Legal Professionals](#4-business-and-legal-professionals)
5. [Common Users](#5-common-users)
6. [User Interface Modes](#6-user-interface-modes)
7. [Use Case Summary by Target](#7-use-case-summary-by-target)

---

## 1. Introduction

This document analyzes the three macro-categories of users that LocalMind targets, detailing for each the specific needs, concrete usage scenarios, and platform features that address those needs. The analysis is preparatory to defining development priorities and designing the user interface.

---

## 2. User Macro-Categories

LocalMind identifies three main macro-categories of users:

| Category                     | Profile                                       | Primary Agent          |
|------------------------------|-----------------------------------------------|------------------------|
| **Developers**               | Developers, DevOps, technical teams           | Tech Agent             |
| **Business Professionals**   | Managers, analysts, lawyers, consultants      | Business / Legal Agent |
| **Common Users**             | Students, freelancers, home users             | Personal Agent         |

Each category requires a different level of interface complexity, different priority features, and different interaction patterns with the platform.

---

## 3. Developers and Technical Teams

### 3.1 Profile

Software developers, DevOps engineers, system architects, and technical teams who need AI assistance for daily development activities.

### 3.2 Primary Needs

- **Assisted debugging**: analysis of stack traces, error logs, and anomalous behaviors
- **Code review**: code review with suggestions on quality, security, and performance
- **Technical analysis**: understanding of architectures, libraries, and patterns
- **Documentation**: automatic generation of technical documentation from source code
- **Code explanation**: understanding legacy or unfamiliar code

### 3.3 Valued Features

Developers are the target that most appreciates LocalMind's architectural choices:

- **Hexagonal architecture**: clean separation between domain and infrastructure, testability
- **Documented REST APIs**: programmatic integration with development tools
- **Extensibility**: ability to add new adapters, providers, and tools
- **Multi-module Maven**: clean and modular project structure
- **Startup scripts**: native execution of backend and frontend via scripts in the \`scripts/\` folder

### 3.4 Concrete Usage Scenarios

**Scenario 1 - Production Debugging**
A developer receives a \`NullPointerException\` in production. They copy the stack trace into the LocalMind chat with the Tech Agent active. The agent analyzes the trace, identifies the problematic line, suggests the probable cause (unhandled nullable field), and proposes a fix with code.

**Scenario 2 - Pre-Commit Code Review**
Before a merge request, the developer submits a Java class to the Tech Agent. The agent identifies: a potential memory leak (unclosed stream), a Single Responsibility principle violation, and a non-thread-safe field. It provides suggestions with corrective code.

**Scenario 3 - Library Understanding**
The team needs to integrate an unfamiliar open-source library. The developer loads the library documentation into LocalMind's RAG and queries the Tech Agent to understand usage patterns, best practices, and potential issues.

**Scenario 4 - Documentation Generation**
The developer indexes a project's source code in the RAG system. They use the Tech Agent to automatically generate API documentation, flow diagrams, and architecture guides based on the actual code.

---

## 4. Business and Legal Professionals

### 4.1 Profile

Managers, business analysts, corporate consultants, lawyers, and legal professionals who need AI assistance for document analysis and reporting.

### 4.2 Primary Needs

- **Automatic reports**: generation of structured reports from data and documents
- **Document summaries**: executive summaries of long and complex documents
- **Contract analysis**: identification of critical clauses, risks, and inconsistencies
- **Regulatory references**: search and citation of relevant regulations
- **Compliance**: verification of conformity to regulations and standards

### 4.3 Dedicated Agents

**Business Agent**: specialized in business analysis, reporting, and synthesis. The system prompt is calibrated to produce structured, professional, and decision-oriented output.

**Legal Agent**: specialized in legal analysis, contract law, and compliance. The system prompt includes instructions to cite regulatory sources, identify contractual risks, and produce output compliant with legal standards.

### 4.4 Concrete Usage Scenarios

**Scenario 1 - Quarterly Report Summary**
A manager uploads the quarterly financial report (PDF, 120 pages) to the RAG system. They ask the Business Agent for an executive summary of the main KPIs, variations compared to the previous quarter, and identified risks. The agent produces a structured summary with tables and bullet points.

**Scenario 2 - Supply Contract Analysis**
A lawyer uploads a supply contract to the RAG system. They ask the Legal Agent to identify: limitation of liability clauses, contractual penalties, termination clauses, and potential risks. The agent produces a structured analysis with references to specific clauses (article number, page).

**Scenario 3 - Document Due Diligence**
A consultant needs to analyze 50 corporate documents for due diligence. They index all documents via folder scanning. They use semantic search to quickly identify clauses related to intellectual property, pending litigation, and financial obligations.

**Scenario 4 - Automatic Weekly Report**
The manager configures an n8n automation: every Friday, LocalMind automatically generates a weekly report based on documents indexed during the week, with summaries, classification, and aggregated metrics. The report is sent via email through n8n.

---

## 5. Common Users

### 5.1 Profile

Students, freelancers, home users, and non-technical professionals who want a personal AI assistant without the complexity of enterprise solutions.

### 5.2 Primary Needs

- **Simple interface**: intuitive user experience, without exposed technical parameters
- **Accessible explanations**: clear, understandable answers, free of technical jargon
- **Daily assistance**: help with writing, translation, organization, research
- **Privacy**: assurance that personal data is not sent to cloud services

### 5.3 Dedicated Agent

**Personal Agent**: specialized in clear and accessible communication. The system prompt is calibrated to produce simple answers, use concrete examples, and avoid unnecessary technical terminology.

### 5.4 Concrete Usage Scenarios

**Scenario 1 - Study Assistant**
A university student uploads course materials into the RAG system. They ask the Personal Agent to explain complex concepts in simple language, generate summaries by topic, and create self-assessment quizzes.

**Scenario 2 - Personal Document Management**
A user indexes their personal document folder (bills, contracts, receipts). They use semantic search to quickly find specific documents ("2024 rental contract", "January electricity bill").

**Scenario 3 - Assisted Writing**
A freelancer uses the Personal Agent for assistance in writing professional emails, business proposals, and social media content. The agent adapts tone and style to the requested context.

---

## 6. User Interface Modes

LocalMind offers three interface modes to adapt to different levels of user technical expertise:

### 6.1 Simple Mode

- Essential interface with only fundamental features
- No visible technical parameters (temperature, top_p, max_tokens hidden)
- Agent selection with natural language descriptions
- Document upload with drag-and-drop
- Search with simple text bar
- Intended for: common users, non-technical professionals

### 6.2 Advanced Mode

- All LLM parameters configurable (temperature, top_p, max_tokens, presence_penalty, frequency_penalty)
- Visible RAG configuration (chunk size, overlap, similarity threshold, top_k)
- Explicit LLM provider and model selection
- Detailed metrics display (token usage, latency, costs)
- Full access to logs and diagnostics
- Intended for: developers, technical users

### 6.3 Role Presets

- Predefined configurations optimized for specific roles
- Each preset activates the appropriate agent, sets optimal LLM parameters, and configures the UI
- Available presets:
  - **Developer**: Tech Agent, low temperature (0.2), high max_tokens (4096), advanced mode
  - **Business**: Business Agent, medium temperature (0.5), structured output, simple mode
  - **Legal**: Legal Agent, low temperature (0.1), mandatory citations, simple mode
  - **Personal**: Personal Agent, medium temperature (0.7), accessible language, simple mode

---

## 7. Use Case Summary by Target

| Target          | Primary Use Case                      | Agent    | UI Mode      |
|-----------------|---------------------------------------|----------|--------------|
| Developer       | Debug, code review, technical analysis| Tech     | Advanced     |
| Developer       | Documentation generation              | Tech     | Advanced     |
| Business        | Automatic reports, summaries          | Business | Simple       |
| Legal           | Contract analysis, compliance         | Legal    | Simple       |
| Legal           | Regulatory references                 | Legal    | Simple       |
| Common User     | Study assistance                      | Personal | Simple       |
| Common User     | Personal document management          | Personal | Simple       |
| Common User     | Assisted writing                      | Personal | Simple       |
`;
