# Specifica Funzionale: Dashboard e Monitoraggio

| Campo        | Valore                            |
|--------------|-----------------------------------|
| **Documento**| Dashboard e Monitoraggio          |
| **Versione** | 0.1.0                             |
| **Data**     | 2026-02-09                        |
| **Progetto** | LocalMind                         |

---

## Indice

1. [Descrizione del Componente](#1-descrizione-del-componente)
2. [Health Check Endpoint](#2-health-check-endpoint)
3. [Metriche Monitorate](#3-metriche-monitorate)
4. [Spring Boot Actuator](#4-spring-boot-actuator)
5. [Frontend Dashboard](#5-frontend-dashboard)
6. [Configurazione](#6-configurazione)

---

## 1. Descrizione del Componente

Il modulo Dashboard e Monitoraggio di LocalMind fornisce una visione centralizzata dello stato dell'intero sistema, delle metriche di utilizzo e delle informazioni operative. Il modulo si compone di:

- **Backend**: endpoint REST per health check e metriche, integrazione Spring Boot Actuator
- **Frontend**: pagina Dashboard Angular con stat card reattive basate su Signal

---

## 2. Health Check Endpoint

### 2.1 Endpoint Principale

```
GET /api/v1/dashboard/health
```

### 2.2 Risposta

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

## 3. Metriche Monitorate

### 3.1 Stato Servizi

| Servizio    | Metodo di Verifica             | Timeout | Frequenza |
|-------------|--------------------------------|---------|-----------|
| MySQL       | SELECT 1 (JDBC)                | 5s      | 30s       |
| Qdrant      | GET /health (HTTP)             | 5s      | 30s       |
| Ollama      | GET /api/tags (HTTP)           | 10s     | 30s       |
| n8n         | GET / (HTTP)                   | 5s      | 60s       |

Ogni servizio puo' trovarsi in uno dei seguenti stati:

| Stato      | Descrizione                                | Colore UI |
|------------|--------------------------------------------|-----------|
| `UP`       | Servizio raggiungibile e funzionante       | Verde     |
| `DOWN`     | Servizio non raggiungibile o in errore     | Rosso     |
| `DEGRADED` | Servizio raggiungibile ma con problemi     | Giallo    |

### 3.2 Utilizzo Token per Provider

| Metrica             | Descrizione                              | Aggregazione                  |
|---------------------|------------------------------------------|-------------------------------|
| `promptTokens`      | Token utilizzati nei prompt (input)      | Per provider, modello, giorno |
| `completionTokens`  | Token generati nelle risposte (output)   | Per provider, modello, giorno |
| `totalTokens`       | Somma prompt + completion                | Per provider, modello, giorno |

### 3.3 Costi Aggregati

| Metrica             | Descrizione                              | Aggregazione            |
|---------------------|------------------------------------------|-------------------------|
| `dailyCost`         | Costo giornaliero per provider           | Per provider, giorno    |
| `weeklyCost`        | Costo settimanale per provider           | Per provider, settimana |
| `monthlyCost`       | Costo mensile per provider               | Per provider, mese      |
| `totalCost`         | Costo totale cumulativo                  | Per provider            |

### 3.4 Latenza per Provider

| Metrica             | Descrizione                              |
|---------------------|------------------------------------------|
| `averageLatencyMs`  | Latenza media in millisecondi            |
| `p50LatencyMs`      | 50esimo percentile (mediana)             |
| `p95LatencyMs`      | 95esimo percentile                       |
| `p99LatencyMs`      | 99esimo percentile                       |
| `maxLatencyMs`      | Latenza massima registrata               |

### 3.5 Documenti Indicizzati

| Metrica             | Descrizione                              |
|---------------------|------------------------------------------|
| `totalDocuments`    | Numero totale di documenti nel sistema   |
| `indexedDocuments`  | Documenti con stato INDEXED              |
| `pendingDocuments`  | Documenti con stato PENDING              |
| `processingDocuments` | Documenti con stato PROCESSING         |
| `failedDocuments`   | Documenti con stato FAILED               |
| `archivedDocuments` | Documenti con stato ARCHIVED             |
| `totalChunks`       | Numero totale di chunk generati          |

### 3.6 Job Batch

| Metrica             | Descrizione                              |
|---------------------|------------------------------------------|
| `completedJobs`     | Job completati con successo              |
| `failedJobs`        | Job completati con errore                |
| `runningJobs`       | Job attualmente in esecuzione            |
| `lastJobExecution`  | Timestamp dell'ultimo job eseguito       |
| `lastJobDuration`   | Durata dell'ultimo job in secondi        |

---

## 4. Spring Boot Actuator

LocalMind espone le metriche infrastrutturali tramite Spring Boot Actuator:

### 4.1 Endpoint Actuator

| Endpoint                       | Descrizione                              |
|--------------------------------|------------------------------------------|
| `/actuator/health`             | Health check complessivo                 |
| `/actuator/info`               | Informazioni applicazione                |
| `/actuator/metrics`            | Metriche JVM e applicative               |
| `/actuator/metrics/{name}`     | Metrica specifica                        |
| `/actuator/prometheus`         | Metriche in formato Prometheus (futuro)  |

### 4.2 Metriche JVM

| Metrica                       | Descrizione                              |
|-------------------------------|------------------------------------------|
| `jvm.memory.used`             | Memoria JVM utilizzata                   |
| `jvm.memory.max`              | Memoria JVM massima                      |
| `jvm.threads.live`            | Thread attivi                            |
| `jvm.gc.pause`                | Pause garbage collector                  |
| `process.cpu.usage`           | Utilizzo CPU del processo                |
| `process.uptime`              | Uptime dell'applicazione                 |

### 4.3 Metriche HTTP

| Metrica                       | Descrizione                              |
|-------------------------------|------------------------------------------|
| `http.server.requests`        | Richieste HTTP totali                    |
| `http.server.requests.active` | Richieste HTTP attualmente in corso      |

### 4.4 Metriche Database

| Metrica                       | Descrizione                              |
|-------------------------------|------------------------------------------|
| `hikaricp.connections.active` | Connessioni JDBC attive                  |
| `hikaricp.connections.idle`   | Connessioni JDBC idle                    |
| `hikaricp.connections.max`    | Connessioni JDBC massime                 |

---

## 5. Frontend Dashboard

### 5.1 Architettura Reattiva

La dashboard Angular utilizza il pattern Signal-based per aggiornamenti reattivi:

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

Ogni stat card e' un componente standalone che riceve dati tramite input signal:

| Card              | Dati                                | Colore        |
|-------------------|-------------------------------------|---------------|
| Stato Servizi     | UP/DOWN per servizio con latenza    | Verde/Rosso   |
| Token Totali      | Conteggio token con trend           | Blu           |
| Costo Totale      | Importo USD con breakdown provider  | Arancione     |
| Documenti         | Conteggio per stato                 | Verde         |
| Batch Jobs        | Completati/Falliti/In corso         | Viola         |
| Latenza Media     | ms con indicatore trend             | Ciano         |

---

## 6. Configurazione

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
