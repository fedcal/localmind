# Specifica Funzionale: AI Agents

| Campo        | Valore                          |
|--------------|---------------------------------|
| **Documento**| Specifica Funzionale AI Agents  |
| **Versione** | 0.1.0                          |
| **Data**     | 2026-02-09                      |
| **Progetto** | LocalMind                       |

---

## Indice

1. [Descrizione del Componente](#1-descrizione-del-componente)
2. [Tipi di Agente](#2-tipi-di-agente)
3. [Architettura degli Agenti](#3-architettura-degli-agenti)
4. [Modello dei Dati](#4-modello-dei-dati)
5. [Flusso di Esecuzione](#5-flusso-di-esecuzione)
6. [Classi Coinvolte](#6-classi-coinvolte)
7. [Configurazione e System Prompt](#7-configurazione-e-system-prompt)

---

## 1. Descrizione del Componente

Il modulo AI Agents di LocalMind implementa un sistema di agenti intelligenti specializzati, ciascuno progettato per un dominio applicativo specifico. Ogni agente combina tre capacita' fondamentali:

1. **LLM Gateway**: accesso a modelli di linguaggio per generazione e comprensione del testo
2. **RAG**: accesso alla knowledge base documentale dell'utente per risposte contestualizzate
3. **Tool Calling**: capacita' di invocare strumenti esterni per azioni specifiche (ricerca, calcolo, formattazione)

Gli agenti sono configurabili dall'utente e possono essere estesi con nuovi tool senza modifiche al dominio.

---

## 2. Tipi di Agente

LocalMind fornisce quattro agenti specializzati predefiniti:

### 2.1 Tech Agent

| Attributo       | Valore                                                  |
|----------------|----------------------------------------------------------|
| **Tipo**        | TECH                                                    |
| **Destinatari** | Sviluppatori, DevOps, architetti                        |
| **Competenze**  | Debug, code review, analisi tecnica, spiegazione codice |
| **Temperatura** | 0.2 (output deterministico e preciso)                   |
| **Max Tokens**  | 4096                                                    |

**Funzionalita' specifiche**:
- Analisi di stack trace e log di errore con identificazione della causa probabile
- Revisione di codice con suggerimenti su qualita', sicurezza, performance e pattern
- Spiegazione di architetture, librerie e framework con esempi di codice
- Generazione di documentazione tecnica da codice sorgente
- Suggerimento di refactoring con codice correttivo

### 2.2 Business Agent

| Attributo       | Valore                                                    |
|----------------|-----------------------------------------------------------|
| **Tipo**        | BUSINESS                                                  |
| **Destinatari** | Manager, analisti, consulenti                             |
| **Competenze**  | Report, sintesi, analisi dati, presentazioni              |
| **Temperatura** | 0.5 (bilanciamento tra creativita' e struttura)           |
| **Max Tokens**  | 4096                                                      |

**Funzionalita' specifiche**:
- Generazione di report strutturati con tabelle, grafici testuali e KPI
- Sintesi esecutiva di documenti lunghi con punti chiave
- Analisi di dati con identificazione di trend e anomalie
- Creazione di presentazioni strutturate per slide
- Confronto tra documenti con evidenziazione differenze

### 2.3 Legal Agent

| Attributo       | Valore                                                    |
|----------------|-----------------------------------------------------------|
| **Tipo**        | LEGAL                                                     |
| **Destinatari** | Avvocati, consulenti legali, compliance officer           |
| **Competenze**  | Clausole contrattuali, riferimenti normativi, compliance  |
| **Temperatura** | 0.1 (massima precisione e determinismo)                   |
| **Max Tokens**  | 4096                                                      |

**Funzionalita' specifiche**:
- Analisi di contratti con identificazione clausole critiche
- Identificazione di rischi contrattuali e incoerenze
- Riferimenti normativi con citazione di leggi e regolamenti
- Verifica di compliance rispetto a standard e regolamenti
- Generazione di checklist di conformita'

### 2.4 Personal Agent

| Attributo       | Valore                                                    |
|----------------|-----------------------------------------------------------|
| **Tipo**        | PERSONAL                                                  |
| **Destinatari** | Studenti, freelancer, utenti comuni                       |
| **Competenze**  | Spiegazioni semplici, assistenza quotidiana, scrittura    |
| **Temperatura** | 0.7 (output naturale e conversazionale)                   |
| **Max Tokens**  | 2048                                                      |

**Funzionalita' specifiche**:
- Spiegazioni di concetti complessi con linguaggio accessibile
- Assistenza nella scrittura di email, documenti e contenuti
- Traduzione e riformulazione di testi
- Organizzazione di informazioni e creazione di riassunti
- Risposte a domande generali con tono conversazionale

---

## 3. Architettura degli Agenti

### 3.1 Componenti di un Agente

Ogni agente e' composto da:

```
+------------------------------------------------------+
|                      Agent                            |
|                                                       |
|  +------------------+  +---------------------------+  |
|  |   System Prompt  |  |   Agent Configuration     |  |
|  |                  |  |   - type                   |  |
|  |  Istruzioni      |  |   - temperature            |  |
|  |  specializzate   |  |   - maxTokens              |  |
|  |  per il ruolo    |  |   - model                  |  |
|  +------------------+  +---------------------------+  |
|                                                       |
|  +------------------+  +---------------------------+  |
|  |   LLM Gateway    |  |   RAG Context             |  |
|  |                  |  |                            |  |
|  |  Generazione     |  |   Documenti rilevanti     |  |
|  |  risposte        |  |   per la query            |  |
|  +------------------+  +---------------------------+  |
|                                                       |
|  +------------------------------------------------+  |
|  |              Tools (AgentTool[])                 |  |
|  |                                                 |  |
|  |  - document_search: ricerca nel RAG             |  |
|  |  - web_search: ricerca web (futuro)             |  |
|  |  - calculator: calcoli matematici               |  |
|  |  - formatter: formattazione output              |  |
|  +------------------------------------------------+  |
+------------------------------------------------------+
```

### 3.2 Pattern di Esecuzione

L'agente segue il pattern ReAct (Reasoning + Acting):

1. **Reasoning**: l'agente analizza la richiesta e decide se necessita di tool o contesto RAG
2. **Acting**: se necessario, l'agente invoca i tool appropriati
3. **Observation**: l'agente osserva i risultati dei tool
4. **Response**: l'agente genera la risposta finale integrando il contesto raccolto

---

## 4. Modello dei Dati

### 4.1 Agent (Entity)

| Campo          | Tipo             | Descrizione                              |
|---------------|------------------|------------------------------------------|
| `id`          | UUID             | Identificativo univoco dell'agente       |
| `name`        | String           | Nome visualizzato (es. "Tech Agent")     |
| `type`        | AgentType (enum) | TECH, BUSINESS, LEGAL, PERSONAL          |
| `systemPrompt`| String           | Prompt di sistema specializzato          |
| `tools`       | List<AgentTool>  | Lista di tool disponibili per l'agente   |
| `temperature` | Double           | Temperatura LLM per questo agente        |
| `maxTokens`   | Integer          | Max token in risposta per questo agente  |
| `model`       | String           | Modello LLM preferito (opzionale)        |
| `enabled`     | boolean          | Agente abilitato/disabilitato            |
| `createdAt`   | LocalDateTime    | Data di creazione                        |
| `updatedAt`   | LocalDateTime    | Data ultimo aggiornamento                |

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

| Campo              | Tipo   | Descrizione                                     |
|-------------------|--------|--------------------------------------------------|
| `name`            | String | Nome del tool (es. "document_search")            |
| `description`     | String | Descrizione del tool per l'LLM                  |
| `parametersSchema`| String | JSON Schema dei parametri del tool               |

Esempio di parametersSchema per il tool `document_search`:

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

| Campo        | Tipo                | Descrizione                              |
|-------------|---------------------|------------------------------------------|
| `response`  | String              | Risposta generata dall'agente            |
| `citations` | List<Citation>      | Citazioni dai documenti RAG utilizzati   |
| `toolCalls` | List<ToolCallResult> | Risultati delle invocazioni di tool      |
| `latencyMs` | long                | Tempo totale di esecuzione in ms         |
| `provider`  | LlmProvider         | Provider LLM utilizzato                  |
| `model`     | String              | Modello LLM utilizzato                   |
| `tokenUsage`| TokenUsage          | Conteggio token utilizzati               |

### 4.5 Citation (Value Object)

| Campo           | Tipo    | Descrizione                              |
|----------------|---------|------------------------------------------|
| `documentId`   | UUID    | ID del documento sorgente                |
| `filename`     | String  | Nome del file sorgente                   |
| `chunkIndex`   | int     | Indice del chunk citato                  |
| `text`         | String  | Testo del chunk citato                   |
| `similarityScore` | double | Score di similarita' (0.0 - 1.0)      |

---

## 5. Flusso di Esecuzione

### 5.1 Diagramma di Sequenza

```
Utente      AgentController   AgentExecutionUseCase   AgentService    LlmGateway    VectorStorePort
  |               |                   |                    |               |               |
  | POST /agents  |                   |                    |               |               |
  | /execute      |                   |                    |               |               |
  |-------------->|                   |                    |               |               |
  |               | execute(request)  |                    |               |               |
  |               |------------------>|                    |               |               |
  |               |                   | execute(agentId,   |               |               |
  |               |                   |   query)           |               |               |
  |               |                   |------------------>|               |               |
  |               |                   |                    |               |               |
  |               |                   |                    | loadAgent()   |               |
  |               |                   |                    |----+          |               |
  |               |                   |                    |    |          |               |
  |               |                   |                    |<---+          |               |
  |               |                   |                    |               |               |
  |               |                   |                    | [Se RAG abilitato]           |
  |               |                   |                    | searchContext  |               |
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
  |               |                   |                    | [Se tool call richiesto]      |
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

### 5.2 Dettaglio Fasi

**Fase 1 - Caricamento Agente**: il servizio carica la configurazione dell'agente (system prompt, tools, parametri LLM) dal repository.

**Fase 2 - Contesto RAG** (opzionale): se l'agente ha tool di ricerca documentale, il servizio esegue una ricerca semantica nel vector store per recuperare i chunk piu' rilevanti alla query dell'utente.

**Fase 3 - Costruzione Prompt**: il system prompt dell'agente viene combinato con il contesto RAG e la query dell'utente per formare il prompt completo.

**Fase 4 - Generazione Risposta**: il prompt viene inviato al LLM Gateway che gestisce routing, fallback e retry.

**Fase 5 - Tool Execution** (opzionale): se il modello richiede l'invocazione di tool, il servizio esegue i tool e reintegra i risultati nel contesto.

**Fase 6 - Costruzione Risultato**: la risposta finale viene assemblata con citazioni, risultati tool e metriche.

---

## 6. Classi Coinvolte

### 6.1 Architettura

```
Domain Layer (localmind-domain)
+-- model/
|   +-- Agent                  # Entity: agente con configurazione
|   +-- AgentType (enum)       # TECH, BUSINESS, LEGAL, PERSONAL
|   +-- AgentTool              # Value Object: tool dell'agente
|   +-- AgentExecutionResult   # Value Object: risultato esecuzione
|   +-- Citation               # Value Object: citazione fonte
+-- port/
|   +-- in/
|   |   +-- AgentExecutionUseCase  # Port in: esecuzione agente
|   +-- out/
|       +-- AgentConfigRepository  # Port out: persistenza config agente
+-- service/
    +-- AgentService               # Domain service: logica agenti

Infrastructure Layer (localmind-infrastructure)
+-- agent/
    +-- persistence/
        +-- entity/
        |   +-- AgentEntity            # JPA entity
        +-- repository/
        |   +-- JpaAgentRepository     # Spring Data JPA
        +-- adapter/
            +-- AgentConfigPersistenceAdapter  # Implementa AgentConfigRepository

API Layer (localmind-api)
+-- agent/
    +-- controller/
    |   +-- AgentController            # REST: /api/v1/agents
    +-- dto/
        +-- AgentExecutionRequestDto   # DTO richiesta
        +-- AgentExecutionResponseDto  # DTO risposta
```

---

## 7. Configurazione e System Prompt

### 7.1 Esempio System Prompt - Tech Agent

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

### 7.2 Esempio System Prompt - Legal Agent

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
