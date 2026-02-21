export const content = `# Models API

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09
**Base URL:** \`http://localhost:8080/api/v1\`

---

## Indice

1. [Panoramica](#1-panoramica)
2. [GET /api/v1/models](#2-get-apiv1models)
3. [GET /api/v1/models/{id}](#3-get-apiv1modelsid)
4. [Modelli di Dati](#4-modelli-di-dati)
5. [Esempi](#5-esempi)

---

## 1. Panoramica

L'API Models consente di interrogare i modelli LLM disponibili nel sistema. Ogni modello e' associato a un provider (Ollama, OpenAI, Anthropic, Google) e ha proprieta' come la dimensione della finestra di contesto e lo stato di disponibilita'.

| Proprieta'       | Valore                                |
|------------------|---------------------------------------|
| **Controller**   | \`ModelController\`                     |
| **Package**      | \`com.localmind.api.llm.controller\`    |
| **Base path**    | \`/api/v1/models\`                      |
| **Use case**     | \`ModelManagementUseCase\`              |
| **Content-Type** | \`application/json\`                    |

---

## 2. GET /api/v1/models

Restituisce la lista di tutti i modelli LLM disponibili nel sistema, indipendentemente dal provider.

### Request

| Proprieta'         | Valore                                |
|--------------------|---------------------------------------|
| **URL**            | \`GET /api/v1/models\`                  |
| **Content-Type**   | -                                     |
| **Autenticazione** | Nessuna                               |

### Response (200 OK)

\`\`\`json
[
  {
    "id": "ollama:llama3.2",
    "name": "llama3.2",
    "provider": "OLLAMA",
    "contextWindow": 131072,
    "available": true
  },
  {
    "id": "ollama:nomic-embed-text",
    "name": "nomic-embed-text",
    "provider": "OLLAMA",
    "contextWindow": 8192,
    "available": true
  },
  {
    "id": "openai:gpt-4o",
    "name": "gpt-4o",
    "provider": "OPENAI",
    "contextWindow": 128000,
    "available": false
  },
  {
    "id": "anthropic:claude-sonnet-4-20250514",
    "name": "claude-sonnet-4-20250514",
    "provider": "ANTHROPIC",
    "contextWindow": 200000,
    "available": false
  }
]
\`\`\`

### Status Codes

| Codice | Descrizione                              |
|--------|------------------------------------------|
| 200    | OK - Lista restituita (puo' essere vuota)|

### Esempio

\`\`\`bash
curl -X GET http://localhost:8080/api/v1/models
\`\`\`

---

## 3. GET /api/v1/models/{id}

Restituisce i dettagli di un singolo modello identificato dal suo ID.

### Request

| Proprieta'         | Valore                                |
|--------------------|---------------------------------------|
| **URL**            | \`GET /api/v1/models/{id}\`             |
| **Content-Type**   | -                                     |
| **Autenticazione** | Nessuna                               |

### Path Parameters

| Parametro | Tipo     | Descrizione                                        |
|-----------|----------|----------------------------------------------------|
| \`id\`      | \`String\` | Identificatore del modello (es. \`ollama:llama3.2\`) |

### Response (200 OK)

\`\`\`json
{
  "id": "ollama:llama3.2",
  "name": "llama3.2",
  "provider": "OLLAMA",
  "contextWindow": 131072,
  "available": true
}
\`\`\`

### Status Codes

| Codice | Descrizione                    |
|--------|--------------------------------|
| 200    | OK - Modello trovato           |
| 404    | Not Found - Modello non trovato|

### Esempio di errore (404)

\`\`\`json
{
  "status": 404,
  "message": "Model not found with id: unknown-model",
  "timestamp": "2026-02-09T14:30:00Z",
  "path": "/api/v1/models/unknown-model"
}
\`\`\`

### Esempio

\`\`\`bash
curl -X GET http://localhost:8080/api/v1/models/ollama:llama3.2
\`\`\`

---

## 4. Modelli di Dati

### ModelDto

**Package**: \`com.localmind.api.llm.dto\`

\`\`\`java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelDto {
    private String id;
    private String name;
    private String provider;
    private int contextWindow;
    private boolean available;
}
\`\`\`

| Campo          | Tipo      | Descrizione                                          |
|----------------|-----------|------------------------------------------------------|
| \`id\`           | \`String\`  | Identificatore univoco (formato \`provider:nome\`)     |
| \`name\`         | \`String\`  | Nome del modello                                     |
| \`provider\`     | \`String\`  | Provider: \`OLLAMA\`, \`OPENAI\`, \`ANTHROPIC\`, \`GOOGLE\`  |
| \`contextWindow\`| \`int\`     | Dimensione della finestra di contesto in token       |
| \`available\`    | \`boolean\` | \`true\` se il modello e' raggiungibile e utilizzabile |

### Mapping dal dominio

Il controller esegue la conversione manuale dal modello di dominio \`LlmModel\` al DTO:

\`\`\`java
private ModelDto toDto(LlmModel model) {
    return ModelDto.builder()
            .id(model.getId())
            .name(model.getName())
            .provider(model.getProvider().name())
            .contextWindow(model.getContextWindow())
            .available(model.isAvailable())
            .build();
}
\`\`\`

### Modello di dominio - LlmModel

| Campo          | Tipo          | Descrizione                                     |
|----------------|---------------|-------------------------------------------------|
| \`id\`           | \`String\`      | Identificatore univoco                          |
| \`name\`         | \`String\`      | Nome del modello                                |
| \`provider\`     | \`LlmProvider\` | Enum: \`OLLAMA\`, \`OPENAI\`, \`ANTHROPIC\`, \`GOOGLE\` |
| \`contextWindow\`| \`int\`         | Dimensione della finestra di contesto           |
| \`available\`    | \`boolean\`     | Stato di disponibilita'                         |

---

## 5. Esempi

### Lista modelli disponibili

\`\`\`bash
curl -X GET http://localhost:8080/api/v1/models
\`\`\`

### Dettaglio modello specifico

\`\`\`bash
curl -X GET http://localhost:8080/api/v1/models/ollama:llama3.2
\`\`\`

### Verifica disponibilita' di un modello prima dell'uso nella chat

\`\`\`bash
# 1. Verificare che il modello sia disponibile
curl -s http://localhost:8080/api/v1/models/ollama:llama3.2 | jq '.available'

# 2. Se disponibile, inviare un messaggio
curl -X POST http://localhost:8080/api/v1/chat \\
  -H "Content-Type: application/json" \\
  -d '{
    "message": "Ciao!",
    "provider": "OLLAMA",
    "model": "llama3.2"
  }'
\`\`\`
`;
