export const content = `# Dashboard API

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09
**Base URL:** \`http://localhost:8080/api/v1\`

---

## Indice

1. [Panoramica](#1-panoramica)
2. [GET /api/v1/dashboard/health](#2-get-apiv1dashboardhealth)
3. [Modelli di Dati](#3-modelli-di-dati)
4. [Endpoint Futuri](#4-endpoint-futuri)
5. [Esempi](#5-esempi)

---

## 1. Panoramica

L'API Dashboard fornisce informazioni sullo stato di salute dei servizi che compongono l'infrastruttura di LocalMind. Nella versione 0.1.0, l'unico endpoint implementato e' l'health check.

| Proprieta'       | Valore                                   |
|------------------|------------------------------------------|
| **Controller**   | \`DashboardController\`                    |
| **Package**      | \`com.localmind.api.dashboard.controller\` |
| **Base path**    | \`/api/v1/dashboard\`                      |
| **Content-Type** | \`application/json\`                       |

---

## 2. GET /api/v1/dashboard/health

Restituisce lo stato di salute complessivo del sistema e dei singoli servizi infrastrutturali.

### Request

| Proprieta'         | Valore                                |
|--------------------|---------------------------------------|
| **URL**            | \`GET /api/v1/dashboard/health\`        |
| **Content-Type**   | -                                     |
| **Autenticazione** | Nessuna                               |

### Response (200 OK)

\`\`\`json
{
  "status": "UP",
  "services": {
    "api": "UP",
    "database": "UP",
    "vectorStore": "UP",
    "ollama": "UP",
    "n8n": "UP"
  }
}
\`\`\`

### Campi della risposta

| Campo      | Tipo                  | Descrizione                                |
|------------|-----------------------|--------------------------------------------|
| \`status\`   | \`String\`              | Stato complessivo del sistema              |
| \`services\` | \`Map<String, String>\` | Mappa dei servizi con il relativo stato    |

### Servizi monitorati

| Servizio      | Chiave         | Descrizione                              | Metodo di verifica                |
|---------------|----------------|------------------------------------------|-----------------------------------|
| API Backend   | \`api\`          | Applicazione Spring Boot                 | Sempre \`UP\` se l'endpoint risponde|
| Database      | \`database\`     | MySQL 8.0                                | Pianificato: JDBC health check    |
| Vector Store  | \`vectorStore\`  | Qdrant                                   | Pianificato: REST health endpoint |
| LLM Provider  | \`ollama\`       | Ollama                                   | Pianificato: HTTP health check    |
| Automazione   | \`n8n\`          | n8n workflow engine                      | Pianificato: HTTP health check    |

### Valori di stato

| Valore | Descrizione                              |
|--------|------------------------------------------|
| \`UP\`   | Servizio operativo e raggiungibile       |
| \`DOWN\` | Servizio non raggiungibile o in errore   |

### Status Codes

| Codice | Descrizione                    |
|--------|--------------------------------|
| 200    | OK - Health check completato   |

### Implementazione attuale

Nella versione 0.1.0, l'endpoint restituisce uno stato statico che include solo il servizio \`api\`:

\`\`\`java
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @GetMapping("/health")
    public ResponseEntity<HealthStatusDto> health() {
        return ResponseEntity.ok(HealthStatusDto.builder()
                .status("UP")
                .services(Map.of("api", "UP"))
                .build());
    }
}
\`\`\`

> **Nota**: nelle versioni successive, l'endpoint sara' ampliato per verificare dinamicamente lo stato di tutti i servizi (database, Qdrant, Ollama, n8n) tramite chiamate HTTP e JDBC.

---

## 3. Modelli di Dati

### HealthStatusDto

**Package**: \`com.localmind.api.dashboard.dto\`

\`\`\`java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatusDto {
    private String status;
    private Map<String, String> services;
}
\`\`\`

| Campo      | Tipo                  | Descrizione                               |
|------------|-----------------------|-------------------------------------------|
| \`status\`   | \`String\`              | Stato complessivo: \`UP\` o \`DOWN\`          |
| \`services\` | \`Map<String, String>\` | Mappa servizio -> stato                   |

---

## 4. Endpoint Futuri

I seguenti endpoint sono pianificati per versioni successive:

### GET /api/v1/dashboard/stats

Statistiche aggregate del sistema:

\`\`\`json
{
  "totalDocuments": 42,
  "indexedDocuments": 38,
  "pendingDocuments": 3,
  "errorDocuments": 1,
  "totalConversations": 15,
  "totalMessages": 234,
  "folderConfigs": 3
}
\`\`\`

### GET /api/v1/dashboard/usage

Statistiche di utilizzo LLM:

\`\`\`json
{
  "totalRequests": 156,
  "totalTokens": 245000,
  "estimatedCost": 1.23,
  "byProvider": {
    "OLLAMA": { "requests": 120, "tokens": 180000, "cost": 0.0 },
    "OPENAI": { "requests": 30, "tokens": 55000, "cost": 1.10 },
    "ANTHROPIC": { "requests": 6, "tokens": 10000, "cost": 0.13 }
  },
  "averageLatencyMs": {
    "OLLAMA": 2100,
    "OPENAI": 1800,
    "ANTHROPIC": 2400
  }
}
\`\`\`

---

## 5. Esempi

### Health check base

\`\`\`bash
curl -X GET http://localhost:8080/api/v1/dashboard/health
\`\`\`

**Response**:

\`\`\`json
{
  "status": "UP",
  "services": {
    "api": "UP"
  }
}
\`\`\`

### Health check con parsing jq

\`\`\`bash
# Stato complessivo
curl -s http://localhost:8080/api/v1/dashboard/health | jq '.status'

# Stato di un servizio specifico
curl -s http://localhost:8080/api/v1/dashboard/health | jq '.services.api'

# Verifica che tutti i servizi siano UP
curl -s http://localhost:8080/api/v1/dashboard/health | jq '.services | to_entries | all(.value == "UP")'
\`\`\`

### Integrazione in script di monitoraggio

\`\`\`bash
#!/bin/bash
STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/dashboard/health)
if [ "$STATUS" -eq 200 ]; then
    echo "LocalMind backend is running"
else
    echo "LocalMind backend is DOWN (HTTP $STATUS)"
    exit 1
fi
\`\`\`
`;
