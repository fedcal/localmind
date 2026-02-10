# Functional Specification: AI Agents

| Field        | Value                              |
|--------------|------------------------------------|
| **Document** | Functional Specification AI Agents |
| **Version**  | 0.1.0                              |
| **Date**     | 2026-02-09                         |
| **Project**  | LocalMind                          |

---

## Table of Contents

1. [Component Description](#1-component-description)
2. [Agent Types](#2-agent-types)
3. [Agent Architecture](#3-agent-architecture)
4. [Data Model](#4-data-model)
5. [Execution Flow](#5-execution-flow)
6. [Involved Classes](#6-involved-classes)
7. [Configuration and System Prompt](#7-configuration-and-system-prompt)

---

## 1. Component Description

The LocalMind AI Agents module implements a system of specialized intelligent agents, each designed for a specific application domain. Each agent combines three fundamental capabilities:

1. **LLM Gateway**: access to language models for text generation and understanding
2. **RAG**: access to the user's document knowledge base for contextualized responses
3. **Tool Calling**: ability to invoke external tools for specific actions (search, calculation, formatting)

Agents are configurable by the user and can be extended with new tools without modifications to the domain.

---

## 2. Agent Types

LocalMind provides four predefined specialized agents:

### 2.1 Tech Agent

| Attribute       | Value                                                   |
|-----------------|---------------------------------------------------------|
| **Type**        | TECH                                                    |
| **Target Users**| Developers, DevOps, architects                          |
| **Skills**      | Debug, code review, technical analysis, code explanation|
| **Temperature** | 0.2 (deterministic and precise output)                  |
| **Max Tokens**  | 4096                                                    |

**Specific features**:
- Stack trace and error log analysis with probable cause identification
- Code review with suggestions on quality, security, performance, and patterns
- Explanation of architectures, libraries, and frameworks with code examples
- Technical documentation generation from source code
- Refactoring suggestions with corrective code

### 2.2 Business Agent

| Attribute       | Value                                                     |
|-----------------|-----------------------------------------------------------|
| **Type**        | BUSINESS                                                  |
| **Target Users**| Managers, analysts, consultants                           |
| **Skills**      | Reports, summaries, data analysis, presentations          |
| **Temperature** | 0.5 (balance between creativity and structure)            |
| **Max Tokens**  | 4096                                                      |

**Specific features**:
- Generation of structured reports with tables, text charts, and KPIs
- Executive summary of long documents with key points
- Data analysis with trend and anomaly identification
- Creation of structured slide presentations
- Document comparison with difference highlighting

### 2.3 Legal Agent

| Attribute       | Value                                                     |
|-----------------|-----------------------------------------------------------|
| **Type**        | LEGAL                                                     |
| **Target Users**| Lawyers, legal consultants, compliance officers           |
| **Skills**      | Contract clauses, regulatory references, compliance       |
| **Temperature** | 0.1 (maximum precision and determinism)                   |
| **Max Tokens**  | 4096                                                      |

**Specific features**:
- Contract analysis with critical clause identification
- Identification of contractual risks and inconsistencies
- Regulatory references with citation of laws and regulations
- Compliance verification against standards and regulations
- Generation of compliance checklists

### 2.4 Personal Agent

| Attribute       | Value                                                     |
|-----------------|-----------------------------------------------------------|
| **Type**        | PERSONAL                                                  |
| **Target Users**| Students, freelancers, general users                      |
| **Skills**      | Simple explanations, daily assistance, writing            |
| **Temperature** | 0.7 (natural and conversational output)                   |
| **Max Tokens**  | 2048                                                      |

**Specific features**:
- Explanation of complex concepts in accessible language
- Assistance with writing emails, documents, and content
- Translation and text reformulation
- Information organization and summary creation
- Answers to general questions in a conversational tone

---

## 3. Agent Architecture

### 3.1 Agent Components

Each agent is composed of:

```
+------------------------------------------------------+
|                      Agent                            |
|                                                       |
|  +------------------+  +---------------------------+  |
|  |   System Prompt  |  |   Agent Configuration     |  |
|  |                  |  |   - type                  |  |
|  |  Specialized     |  |   - temperature           |  |
|  |  instructions    |  |   - maxTokens             |  |
|  |  for the role    |  |   - model                 |  |
|  +------------------+  +---------------------------+  |
|                                                       |
|  +------------------+  +---------------------------+  |
|  |   LLM Gateway    |  |   RAG Context             |  |
|  |                  |  |                           |  |
|  |  Response         |  |   Relevant documents      |  |
|  |  generation       |  |   for the query           |  |
|  +------------------+  +---------------------------+  |
|                                                       |
|  +-------------------------------------------------+  |
|  |              Tools (AgentTool[])                |  |
|  |                                                 |  |
|  |  - document_search: search in RAG              |  |
|  |  - web_search: web search (future)             |  |
|  |  - calculator: mathematical calculations       |  |
|  |  - formatter: output formatting                |  |
|  +-------------------------------------------------+  |
+------------------------------------------------------+
```

### 3.2 Execution Pattern

The agent follows the ReAct (Reasoning + Acting) pattern:

1. **Reasoning**: the agent analyzes the request and decides whether it needs tools or RAG context
2. **Acting**: if necessary, the agent invokes the appropriate tools
3. **Observation**: the agent observes the tool results
4. **Response**: the agent generates the final response integrating the collected context

---

## 4. Data Model

### 4.1 Agent (Entity)

| Field         | Type             | Description                              |
|---------------|------------------|------------------------------------------|
| `id`          | UUID             | Unique agent identifier                  |
| `name`        | String           | Display name (e.g. "Tech Agent")         |
| `type`        | AgentType (enum) | TECH, BUSINESS, LEGAL, PERSONAL          |
| `systemPrompt`| String           | Specialized system prompt                |
| `tools`       | List<AgentTool>  | List of tools available for the agent    |
| `temperature` | Double           | LLM temperature for this agent           |
| `maxTokens`   | Integer          | Max response tokens for this agent       |
| `model`       | String           | Preferred LLM model (optional)           |
| `enabled`     | boolean          | Agent enabled/disabled                   |
| `createdAt`   | LocalDateTime    | Creation date                            |
| `updatedAt`   | LocalDateTime    | Last update date                         |

### 4.2 AgentType (Enum)

```java
public enum AgentType {
    TECH,
    BUSINESS,
    LEGAL,
    PERSONAL
}
```

### 4.3 AgentTool (Value Object)

| Field             | Type   | Description                                      |
|-------------------|--------|--------------------------------------------------|
| `name`            | String | Tool name (e.g. "document_search")               |
| `description`     | String | Tool description for the LLM                     |
| `parametersSchema`| String | JSON Schema of the tool parameters               |

Example of parametersSchema for the `document_search` tool:

```json
{
  "type": "object",
  "properties": {
    "query": {
      "type": "string",
      "description": "La query di ricerca semantica"
    },
    "topK": {
      "type": "integer",
      "description": "Numero massimo di risultati",
      "default": 5
    }
  },
  "required": ["query"]
}
```

### 4.4 AgentExecutionResult (Value Object)

| Field       | Type                | Description                              |
|-------------|---------------------|------------------------------------------|
| `response`  | String              | Response generated by the agent          |
| `citations` | List<Citation>      | Citations from the RAG documents used    |
| `toolCalls` | List<ToolCallResult>| Results of tool invocations              |
| `latencyMs` | long                | Total execution time in ms               |
| `provider`  | LlmProvider         | LLM provider used                        |
| `model`     | String              | LLM model used                           |
| `tokenUsage`| TokenUsage          | Token usage count                        |

### 4.5 Citation (Value Object)

| Field             | Type    | Description                              |
|-------------------|---------|------------------------------------------|
| `documentId`      | UUID    | Source document ID                       |
| `filename`        | String  | Source file name                         |
| `chunkIndex`      | int     | Cited chunk index                        |
| `text`            | String  | Cited chunk text                         |
| `similarityScore` | double  | Similarity score (0.0 - 1.0)            |

---

## 5. Execution Flow

### 5.1 Sequence Diagram

```
User        AgentController   AgentExecutionUseCase   AgentService    LlmGateway    VectorStorePort
  |               |                   |                    |               |               |
  | POST /agents  |                   |                    |               |               |
  | /execute      |                   |                    |               |               |
  |-------------->|                   |                    |               |               |
  |               | execute(request)  |                    |               |               |
  |               |------------------>|                    |               |               |
  |               |                   | execute(agentId,   |               |               |
  |               |                   |   query)           |               |               |
  |               |                   |------------------->|               |               |
  |               |                   |                    |               |               |
  |               |                   |                    | loadAgent()   |               |
  |               |                   |                    |----+          |               |
  |               |                   |                    |    |          |               |
  |               |                   |                    |<---+          |               |
  |               |                   |                    |               |               |
  |               |                   |                    | [If RAG enabled]              |
  |               |                   |                    | searchContext |               |
  |               |                   |                    |------------------------------>|
  |               |                   |                    |               |               |
  |               |                   |                    | List<Chunk>   |               |
  |               |                   |                    |<------------------------------|
  |               |                   |                    |               |               |
  |               |                   |                    | buildPrompt() |               |
  |               |                   |                    | (system +     |               |
  |               |                   |                    |  context +    |               |
  |               |                   |                    |  query)       |               |
  |               |                   |                    |----+          |               |
  |               |                   |                    |    |          |               |
  |               |                   |                    |<---+          |               |
  |               |                   |                    |               |               |
  |               |                   |                    | chat(request) |               |
  |               |                   |                    |-------------->|               |
  |               |                   |                    |               |               |
  |               |                   |                    | ChatResponse  |               |
  |               |                   |                    |<--------------|               |
  |               |                   |                    |               |               |
  |               |                   |                    | [If tool call required]       |
  |               |                   |                    | executeTool() |               |
  |               |                   |                    |----+          |               |
  |               |                   |                    |    |          |               |
  |               |                   |                    |<---+          |               |
  |               |                   |                    |               |               |
  |               |                   |                    | buildResult() |               |
  |               |                   |                    |----+          |               |
  |               |                   |                    |    |          |               |
  |               |                   |                    |<---+          |               |
  |               |                   |                    |               |               |
  |               |                   | AgentExecResult    |               |               |
  |               |                   |<-------------------|               |               |
  |               | AgentExecResult   |                    |               |               |
  |               |<------------------|                    |               |               |
  |  JSON result  |                   |                    |               |               |
  |<--------------|                   |                    |               |               |
```

### 5.2 Phase Details

**Phase 1 - Agent Loading**: the service loads the agent configuration (system prompt, tools, LLM parameters) from the repository.

**Phase 2 - RAG Context** (optional): if the agent has document search tools, the service performs a semantic search in the vector store to retrieve the most relevant chunks to the user's query.

**Phase 3 - Prompt Construction**: the agent's system prompt is combined with the RAG context and the user's query to form the complete prompt.

**Phase 4 - Response Generation**: the prompt is sent to the LLM Gateway which handles routing, fallback, and retry.

**Phase 5 - Tool Execution** (optional): if the model requests tool invocation, the service executes the tools and reintegrates the results into the context.

**Phase 6 - Result Construction**: the final response is assembled with citations, tool results, and metrics.

---

## 6. Involved Classes

### 6.1 Architecture

```
Domain Layer (localmind-domain)
+-- model/
|   +-- Agent                  # Entity: agent with configuration
|   +-- AgentType (enum)       # TECH, BUSINESS, LEGAL, PERSONAL
|   +-- AgentTool              # Value Object: agent tool
|   +-- AgentExecutionResult   # Value Object: execution result
|   +-- Citation               # Value Object: source citation
+-- port/
|   +-- in/
|   |   +-- AgentExecutionUseCase  # Port in: agent execution
|   +-- out/
|       +-- AgentConfigRepository  # Port out: agent config persistence
+-- service/
    +-- AgentService               # Domain service: agent logic

Infrastructure Layer (localmind-infrastructure)
+-- agent/
    +-- persistence/
        +-- entity/
        |   +-- AgentEntity            # JPA entity
        +-- repository/
        |   +-- JpaAgentRepository     # Spring Data JPA
        +-- adapter/
            +-- AgentConfigPersistenceAdapter  # Implements AgentConfigRepository

API Layer (localmind-api)
+-- agent/
    +-- controller/
    |   +-- AgentController            # REST: /api/v1/agents
    +-- dto/
        +-- AgentExecutionRequestDto   # Request DTO
        +-- AgentExecutionResponseDto  # Response DTO
```

---

## 7. Configuration and System Prompt

### 7.1 System Prompt Example - Tech Agent

```
Sei un assistente tecnico specializzato per sviluppatori software.

Le tue competenze includono:
- Debug e analisi di errori e stack trace
- Code review con focus su qualita', sicurezza e performance
- Spiegazione di architetture, pattern e librerie
- Generazione di documentazione tecnica

Regole:
- Rispondi sempre con codice formattato quando appropriato
- Cita le fonti documentali quando utilizzi il contesto RAG
- Usa terminologia tecnica precisa
- Fornisci esempi di codice quando utile
- Identifica potenziali problemi di sicurezza e performance
```

### 7.2 System Prompt Example - Legal Agent

```
Sei un assistente legale specializzato in analisi contrattuale e normativa.

Le tue competenze includono:
- Analisi di clausole contrattuali
- Identificazione di rischi e incoerenze
- Riferimenti normativi con citazione di leggi e regolamenti
- Verifica di compliance

Regole:
- Cita sempre il numero di articolo e la pagina quando fai riferimento a clausole
- Indica chiaramente quando un'opinione non e' un parere legale formale
- Utilizza terminologia giuridica appropriata
- Identifica sempre i rischi potenziali
- Fornisci checklist di conformita' quando richiesto
```
