# Functional Specification: Dashboard and Monitoring

| Field        | Value                             |
|--------------|-----------------------------------|
| **Document** | Dashboard and Monitoring          |
| **Version**  | 0.1.0                             |
| **Date**     | 2026-02-09                        |
| **Project**  | LocalMind                         |

---

## Table of Contents

1. [Component Description](#1-component-description)
2. [Health Check Endpoint](#2-health-check-endpoint)
3. [Monitored Metrics](#3-monitored-metrics)
4. [Spring Boot Actuator](#4-spring-boot-actuator)
5. [Frontend Dashboard](#5-frontend-dashboard)
6. [Configuration](#6-configuration)

---

## 1. Component Description

The LocalMind Dashboard and Monitoring module provides a centralized view of the overall system status, usage metrics, and operational information. The module consists of:

- **Backend**: REST endpoints for health checks and metrics, Spring Boot Actuator integration
- **Frontend**: Angular Dashboard page with reactive stat cards based on Signals

---

## 2. Health Check Endpoint

### 2.1 Main Endpoint

```
GET /api/v1/dashboard/health
```

### 2.2 Response

```json
{
  "status": "UP",
  "timestamp": "2026-02-09T15:30:00",
  "services": {
    "mysql": {
      "status": "UP",
      "latencyMs": 23,
      "details": {
        "version": "8.0",
        "database": "localmind",
        "activeConnections": 5
      }
    },
    "qdrant": {
      "status": "UP",
      "latencyMs": 15,
      "details": {
        "version": "1.7.4",
        "collectionsCount": 1,
        "pointsCount": 15234
      }
    },
    "ollama": {
      "status": "UP",
      "latencyMs": 45,
      "details": {
        "version": "0.3.6",
        "modelsLoaded": ["llama3.2", "nomic-embed-text"],
        "gpuAvailable": true
      }
    },
    "n8n": {
      "status": "DOWN",
      "latencyMs": null,
      "details": {
        "error": "Connection refused"
      }
    }
  },
  "metrics": {
    "totalTokens": 1234567,
    "totalCostUsd": 2.45,
    "totalRequests": 342,
    "averageLatencyMs": 1230
  },
  "documents": {
    "total": 156,
    "indexed": 142,
    "pending": 8,
    "processing": 0,
    "failed": 4,
    "archived": 2
  },
  "batchJobs": {
    "completed": 45,
    "failed": 2,
    "running": 1,
    "lastExecution": "2026-02-09T15:30:00"
  }
}
```

---

## 3. Monitored Metrics

### 3.1 Service Status

| Service     | Verification Method              | Timeout | Frequency |
|-------------|----------------------------------|---------|-----------|
| MySQL       | SELECT 1 (JDBC)                  | 5s      | 30s       |
| Qdrant      | GET /health (HTTP)               | 5s      | 30s       |
| Ollama      | GET /api/tags (HTTP)             | 10s     | 30s       |
| n8n         | GET / (HTTP)                     | 5s      | 60s       |

Each service can be in one of the following states:

| Status     | Description                                 | UI Color  |
|------------|---------------------------------------------|-----------|
| `UP`       | Service reachable and functioning           | Green     |
| `DOWN`     | Service unreachable or in error             | Red       |
| `DEGRADED` | Service reachable but with issues           | Yellow    |

### 3.2 Token Usage per Provider

| Metric              | Description                              | Aggregation                   |
|---------------------|------------------------------------------|-------------------------------|
| `promptTokens`      | Tokens used in prompts (input)           | Per provider, model, day      |
| `completionTokens`  | Tokens generated in responses (output)   | Per provider, model, day      |
| `totalTokens`       | Sum of prompt + completion               | Per provider, model, day      |

### 3.3 Aggregated Costs

| Metric              | Description                              | Aggregation             |
|---------------------|------------------------------------------|-------------------------|
| `dailyCost`         | Daily cost per provider                  | Per provider, day       |
| `weeklyCost`        | Weekly cost per provider                 | Per provider, week      |
| `monthlyCost`       | Monthly cost per provider                | Per provider, month     |
| `totalCost`         | Total cumulative cost                    | Per provider            |

### 3.4 Latency per Provider

| Metric              | Description                              |
|---------------------|------------------------------------------|
| `averageLatencyMs`  | Average latency in milliseconds          |
| `p50LatencyMs`      | 50th percentile (median)                 |
| `p95LatencyMs`      | 95th percentile                          |
| `p99LatencyMs`      | 99th percentile                          |
| `maxLatencyMs`      | Maximum recorded latency                 |

### 3.5 Indexed Documents

| Metric                | Description                              |
|-----------------------|------------------------------------------|
| `totalDocuments`      | Total number of documents in the system  |
| `indexedDocuments`    | Documents with INDEXED status            |
| `pendingDocuments`    | Documents with PENDING status            |
| `processingDocuments` | Documents with PROCESSING status         |
| `failedDocuments`     | Documents with FAILED status             |
| `archivedDocuments`   | Documents with ARCHIVED status           |
| `totalChunks`         | Total number of generated chunks         |

### 3.6 Batch Jobs

| Metric              | Description                              |
|---------------------|------------------------------------------|
| `completedJobs`     | Successfully completed jobs              |
| `failedJobs`        | Jobs completed with errors               |
| `runningJobs`       | Jobs currently running                   |
| `lastJobExecution`  | Timestamp of the last executed job       |
| `lastJobDuration`   | Duration of the last job in seconds      |

---

## 4. Spring Boot Actuator

LocalMind exposes infrastructure metrics through Spring Boot Actuator:

### 4.1 Actuator Endpoints

| Endpoint                       | Description                              |
|--------------------------------|------------------------------------------|
| `/actuator/health`             | Overall health check                     |
| `/actuator/info`               | Application information                  |
| `/actuator/metrics`            | JVM and application metrics              |
| `/actuator/metrics/{name}`     | Specific metric                          |
| `/actuator/prometheus`         | Metrics in Prometheus format (future)    |

### 4.2 JVM Metrics

| Metric                        | Description                              |
|-------------------------------|------------------------------------------|
| `jvm.memory.used`             | JVM memory used                          |
| `jvm.memory.max`              | Maximum JVM memory                       |
| `jvm.threads.live`            | Active threads                           |
| `jvm.gc.pause`                | Garbage collector pauses                 |
| `process.cpu.usage`           | Process CPU usage                        |
| `process.uptime`              | Application uptime                       |

### 4.3 HTTP Metrics

| Metric                        | Description                              |
|-------------------------------|------------------------------------------|
| `http.server.requests`        | Total HTTP requests                      |
| `http.server.requests.active` | Currently active HTTP requests           |

### 4.4 Database Metrics

| Metric                        | Description                              |
|-------------------------------|------------------------------------------|
| `hikaricp.connections.active` | Active JDBC connections                  |
| `hikaricp.connections.idle`   | Idle JDBC connections                    |
| `hikaricp.connections.max`    | Maximum JDBC connections                 |

---

## 5. Frontend Dashboard

### 5.1 Reactive Architecture

The Angular dashboard uses the Signal-based pattern for reactive updates:

```typescript
// Esempio concettuale di signal-based dashboard
export class DashboardComponent {
  private healthService = inject(HealthService);

  health = signal<HealthStatus | null>(null);
  loading = signal<boolean>(true);

  constructor() {
    // Polling ogni 30 secondi
    interval(30000).pipe(
      switchMap(() => this.healthService.getHealth())
    ).subscribe(data => {
      this.health.set(data);
      this.loading.set(false);
    });
  }

  // Computed signals per derivare dati
  servicesUp = computed(() => {
    const h = this.health();
    if (!h) return 0;
    return Object.values(h.services).filter(s => s.status === 'UP').length;
  });
}
```

### 5.2 Stat Cards

Each stat card is a standalone component that receives data through input signals:

| Card              | Data                                | Color         |
|-------------------|-------------------------------------|---------------|
| Service Status    | UP/DOWN per service with latency    | Green/Red     |
| Total Tokens      | Token count with trend              | Blue          |
| Total Cost        | USD amount with provider breakdown  | Orange        |
| Documents         | Count by status                     | Green         |
| Batch Jobs        | Completed/Failed/Running            | Purple        |
| Average Latency   | ms with trend indicator             | Cyan          |

---

## 6. Configuration

```yaml
localmind:
  dashboard:
    # Frequenza health check (secondi)
    health-check-interval: 30

    # Timeout per health check servizi
    health-check-timeout: 5s

    # Abilitazione metriche dettagliate
    detailed-metrics: true

  # Spring Boot Actuator
  management:
    endpoints:
      web:
        exposure:
          include: health,info,metrics
    endpoint:
      health:
        show-details: when-authorized
```
