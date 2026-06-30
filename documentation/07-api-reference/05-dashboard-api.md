# Dashboard API

**Project:** LocalMind
**Version:** 0.1.0
**Date:** 2026-02-09
**Base URL:** `http://localhost:8080/api/v1`

---

## Table of Contents

1. [Overview](#1-overview)
2. [GET /api/v1/dashboard/health](#2-get-apiv1dashboardhealth)
3. [Data Models](#3-data-models)
4. [Future Endpoints](#4-future-endpoints)
5. [Examples](#5-examples)

---

## 1. Overview

The Dashboard API provides information about the health status of the services that make up the LocalMind infrastructure. In version 0.1.0, the only implemented endpoint is the health check.

| Property         | Value                                    |
|------------------|------------------------------------------|
| **Controller**   | `DashboardController`                    |
| **Package**      | `com.localmind.api.dashboard.controller` |
| **Base path**    | `/api/v1/dashboard`                      |
| **Content-Type** | `application/json`                       |

---

## 2. GET /api/v1/dashboard/health

Returns the overall health status of the system and individual infrastructure services.

### Request

| Property           | Value                                 |
|--------------------|---------------------------------------|
| **URL**            | `GET /api/v1/dashboard/health`        |
| **Content-Type**   | -                                     |
| **Authentication** | None                                  |

### Response (200 OK)

```json
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
```

### Response Fields

| Field      | Type                  | Description                                |
|------------|-----------------------|--------------------------------------------|
| `status`   | `String`              | Overall system status                      |
| `services` | `Map<String, String>` | Map of services with their respective status|

### Monitored Services

| Service       | Key            | Description                              | Verification Method                |
|---------------|----------------|------------------------------------------|------------------------------------|
| API Backend   | `api`          | Spring Boot application                  | Always `UP` if the endpoint responds|
| Database      | `database`     | MySQL 8.0                                | Planned: JDBC health check         |
| Vector Store  | `vectorStore`  | Qdrant                                   | Planned: REST health endpoint      |
| LLM Provider  | `ollama`       | Ollama                                   | Planned: HTTP health check         |
| Automation    | `n8n`          | n8n workflow engine                      | Planned: HTTP health check         |

### Status Values

| Value  | Description                              |
|--------|------------------------------------------|
| `UP`   | Service operational and reachable        |
| `DOWN` | Service unreachable or in error state    |

### Status Codes

| Code   | Description                    |
|--------|--------------------------------|
| 200    | OK - Health check completed    |

### Current Implementation

In version 0.1.0, the endpoint returns a static status that includes only the `api` service:

```java
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
```

> **Note**: in subsequent versions, the endpoint will be extended to dynamically verify the status of all services (database, Qdrant, Ollama, n8n) via HTTP and JDBC calls.

---

## 3. Data Models

### HealthStatusDto

**Package**: `com.localmind.api.dashboard.dto`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatusDto {
    private String status;
    private Map<String, String> services;
}
```

| Field      | Type                  | Description                               |
|------------|-----------------------|-------------------------------------------|
| `status`   | `String`              | Overall status: `UP` or `DOWN`            |
| `services` | `Map<String, String>` | Service -> status map                     |

---

## 4. Future Endpoints

The following endpoints are planned for subsequent versions:

### GET /api/v1/dashboard/stats

Aggregate system statistics:

```json
{
  "totalDocuments": 42,
  "indexedDocuments": 38,
  "pendingDocuments": 3,
  "errorDocuments": 1,
  "totalConversations": 15,
  "totalMessages": 234,
  "folderConfigs": 3
}
```

### GET /api/v1/dashboard/usage

LLM usage statistics:

```json
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
```

---

## 5. Examples

### Basic health check

```bash
curl -X GET http://localhost:8080/api/v1/dashboard/health
```

**Response**:

```json
{
  "status": "UP",
  "services": {
    "api": "UP"
  }
}
```

### Health check with jq parsing

```bash
# Overall status
curl -s http://localhost:8080/api/v1/dashboard/health | jq '.status'

# Specific service status
curl -s http://localhost:8080/api/v1/dashboard/health | jq '.services.api'

# Verify that all services are UP
curl -s http://localhost:8080/api/v1/dashboard/health | jq '.services | to_entries | all(.value == "UP")'
```

### Integration in monitoring script

```bash
#!/bin/bash
STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/dashboard/health)
if [ "$STATUS" -eq 200 ]; then
    echo "LocalMind backend is running"
else
    echo "LocalMind backend is DOWN (HTTP $STATUS)"
    exit 1
fi
```
