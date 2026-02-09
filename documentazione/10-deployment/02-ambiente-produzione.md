# Ambiente di Produzione

| | |
|---|---|
| **Documento** | Documentazione Ambiente di Produzione |
| **Versione** | 0.1.0 |
| **Data** | 2026-02-09 |
| **Progetto** | LocalMind |

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Differenze tra Sviluppo e Produzione](#2-differenze-tra-sviluppo-e-produzione)
3. [Profilo Spring Boot di Produzione](#3-profilo-spring-boot-di-produzione)
4. [Build di Produzione - Backend](#4-build-di-produzione---backend)
5. [Build di Produzione - Frontend](#5-build-di-produzione---frontend)
6. [Avvio in Produzione](#6-avvio-in-produzione)
7. [Requisiti Hardware](#7-requisiti-hardware)
8. [Monitoraggio con Spring Boot Actuator](#8-monitoraggio-con-spring-boot-actuator)
9. [Configurazione Nginx come Reverse Proxy](#9-configurazione-nginx-come-reverse-proxy)
10. [Backup e Restore](#10-backup-e-restore)

---

## 1. Panoramica

L'ambiente di produzione di LocalMind e' progettato per l'esecuzione sulla macchina dell'utente finale in modalita' self-hosted. A differenza di un tradizionale deployment cloud, la "produzione" in LocalMind corrisponde alla macchina personale dell'utente, dove il software viene eseguito in modo stabile e ottimizzato.

Le principali differenze rispetto all'ambiente di sviluppo riguardano:

- Configurazioni ottimizzate per stabilita' e performance.
- Credenziali gestite tramite variabili d'ambiente sicure.
- Logging ridotto per evitare overhead.
- Validazione dello schema database (non creazione automatica).
- Build ottimizzate e minificate per backend e frontend.

---

## 2. Differenze tra Sviluppo e Produzione

| Aspetto | Sviluppo (`dev`) | Produzione (`prod`) |
|---|---|---|
| **Database hostname** | `localhost` | `localhost` (MySQL nativo) |
| **Ollama hostname** | `localhost` | `localhost` (nativo o configurabile) |
| **Qdrant hostname** | `localhost` | `localhost` (nativo o configurabile) |
| **n8n hostname** | `localhost` | `localhost` (nativo o configurabile) |
| **Credenziali** | Hardcoded nei default | Variabili d'ambiente obbligatorie |
| **DDL auto** | `create` (crea tabelle automaticamente) | `validate` (verifica schema esistente) |
| **SQL logging** | `true` (mostra query nel log) | `false` (nessuna query nei log) |
| **Livello log** | `DEBUG` | `INFO` |
| **CORS origins** | `http://localhost:4200` | Configurabile (stesso host o dominio) |
| **Frontend** | Angular dev server (live reload) | Build statica servita da nginx |
| **Hot reload** | Abilitato (Spring DevTools) | Disabilitato |

---

## 3. Profilo Spring Boot di Produzione

Il profilo di produzione e' definito nel file `application-prod.yml`:

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/localmind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: false

logging:
  level:
    root: INFO
    it.localmind: INFO
    org.springframework: WARN
    org.hibernate: WARN

localmind:
  llm:
    ollama:
      base-url: http://localhost:11434
  vectorstore:
    qdrant:
      host: localhost
      port: 6334
  automation:
    n8n:
      base-url: http://localhost:5678

server:
  tomcat:
    max-threads: 200
    accept-count: 100

management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      show-details: always
```

### Differenze chiave rispetto a application.yml (dev)

| Property | Dev | Prod | Motivazione |
|---|---|---|---|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/localmind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | `jdbc:mysql://localhost:3306/localmind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | In produzione si puo' configurare un host diverso se necessario |
| `spring.jpa.hibernate.ddl-auto` | `create` | `validate` | In produzione lo schema deve essere gestito tramite migration (Flyway/Liquibase) |
| `spring.jpa.show-sql` | `true` | `false` | Le query SQL nei log consumano risorse e possono esporre dati sensibili |
| `logging.level.root` | `DEBUG` | `INFO` | Riduzione del volume di log per performance e leggibilita' |
| `DB_USERNAME`, `DB_PASSWORD` | Default (`localmind`) | Nessun default (obbligatorio) | In produzione le credenziali devono essere fornite esplicitamente |

---

## 4. Build di Produzione - Backend

### Compilazione e packaging

```bash
cd localmind-backend
mvn clean package -DskipTests
```

**Nota:** `-DskipTests` viene utilizzato per velocizzare la build di produzione. I test devono essere eseguiti separatamente nella pipeline CI/CD o prima del deploy manuale.

### Artefatto generato

```
localmind-app/target/localmind-app-0.1.0-SNAPSHOT.jar
```

Questo file e' un **fat JAR** (uber JAR) che contiene:

- Tutte le classi compilate di tutti i moduli Maven.
- Tutte le dipendenze (Spring Boot, Spring AI, driver MySQL, ecc.).
- Il server Tomcat embedded.
- I file di configurazione (`application.yml`, `application-prod.yml`).

### Dimensione attesa del JAR

| Componente | Dimensione stimata |
|---|---|
| Codice applicativo | ~5 MB |
| Dipendenze Spring Boot | ~40 MB |
| Dipendenze Spring AI | ~15 MB |
| Driver e librerie | ~10 MB |
| **Totale stimato** | **~70-80 MB** |

### Verifica del JAR

```bash
# Verifica che il JAR sia valido
java -jar localmind-app/target/localmind-app-0.1.0-SNAPSHOT.jar --help

# Verifica il contenuto
jar tf localmind-app/target/localmind-app-0.1.0-SNAPSHOT.jar | head -20
```

---

## 5. Build di Produzione - Frontend

### Compilazione ottimizzata

```bash
cd localmind-frontend
ng build --configuration production
```

### Ottimizzazioni applicate in modalita' production

| Ottimizzazione | Descrizione |
|---|---|
| **AOT Compilation** | Ahead-of-Time compilation dei template Angular |
| **Tree shaking** | Rimozione del codice non utilizzato |
| **Minification** | Riduzione dimensione file JavaScript e CSS |
| **Bundling** | Raggruppamento dei moduli in pochi file |
| **Source maps** | Disabilitate (riduzione dimensione) |
| **Cache busting** | Hash nei nomi dei file per invalidazione cache |

### Output della build

```
dist/localmind-frontend/
├── browser/
│   ├── index.html
│   ├── main-[hash].js
│   ├── polyfills-[hash].js
│   ├── styles-[hash].css
│   ├── assets/
│   └── ...
```

### Dimensione attesa

| File | Dimensione stimata |
|---|---|
| `main-[hash].js` | ~200-400 KB (gzipped) |
| `polyfills-[hash].js` | ~30 KB (gzipped) |
| `styles-[hash].css` | ~20-50 KB (gzipped) |
| **Totale stimato** | **~300-500 KB (gzipped)** |

### Servire i file statici

I file statici generati dalla build devono essere serviti da un web server. Le opzioni raccomandate sono:

1. **Nginx** (raccomandato): web server ad alte prestazioni, configurabile come reverse proxy.
2. **Apache HTTP Server**: alternativa consolidata.
3. **Caddy**: web server moderno con HTTPS automatico.

---

## 6. Avvio in Produzione

### Avvio con profilo di produzione

```bash
java -jar \
  -Dspring.profiles.active=prod \
  localmind-app/target/localmind-app-0.1.0-SNAPSHOT.jar
```

### Avvio con variabili d'ambiente

```bash
DB_USERNAME=localmind \
DB_PASSWORD=secure_password_here \
java -jar \
  -Dspring.profiles.active=prod \
  -Xms512m \
  -Xmx2g \
  localmind-app/target/localmind-app-0.1.0-SNAPSHOT.jar
```

### Parametri JVM raccomandati per produzione

| Parametro | Valore consigliato | Descrizione |
|---|---|---|
| `-Xms` | `512m` | Memoria heap iniziale |
| `-Xmx` | `2g` | Memoria heap massima |
| `-XX:+UseG1GC` | *(abilitato di default in Java 17+)* | Garbage collector G1 |
| `-XX:MaxGCPauseMillis` | `200` | Pausa GC massima target |
| `-Dspring.profiles.active` | `prod` | Profilo Spring Boot |

### Avvio come servizio systemd (Linux)

Per eseguire LocalMind come servizio di sistema su Linux:

```ini
# /etc/systemd/system/localmind.service
[Unit]
Description=LocalMind Backend
After=mysql.service
Requires=mysql.service

[Service]
Type=simple
User=localmind
Group=localmind
WorkingDirectory=/opt/localmind
ExecStart=/usr/bin/java \
    -jar \
    -Dspring.profiles.active=prod \
    -Xms512m \
    -Xmx2g \
    /opt/localmind/localmind-app.jar
Restart=on-failure
RestartSec=10
EnvironmentFile=/opt/localmind/.env

[Install]
WantedBy=multi-user.target
```

Comandi di gestione:

```bash
# Abilitare il servizio all'avvio
sudo systemctl enable localmind

# Avviare il servizio
sudo systemctl start localmind

# Verificare lo stato
sudo systemctl status localmind

# Consultare i log
journalctl -u localmind -f
```

---

## 7. Requisiti Hardware

### Requisiti minimi

| Risorsa | Minimo | Raccomandato | Note |
|---|---|---|---|
| **CPU** | 4 core | 8 core | 8 core raccomandati per Ollama con modelli grandi |
| **RAM** | 8 GB | 16 GB | 16 GB con modelli LLM di dimensioni superiori a 7B |
| **Storage** | 20 GB | 50+ GB | Spazio per documenti, modelli LLM, database |
| **GPU** | Non richiesta | NVIDIA con CUDA | Accelera significativamente l'inference LLM |

### Ripartizione della memoria RAM

| Componente | RAM stimata (minimo) | RAM stimata (raccomandato) |
|---|---|---|
| Sistema operativo | 2 GB | 2 GB |
| MySQL | 0.5 GB | 1 GB |
| Qdrant | 0.5 GB | 1 GB |
| Ollama + modello LLM | 3 GB | 8 GB |
| Spring Boot (JVM) | 1 GB | 2 GB |
| Angular / Nginx | 0.1 GB | 0.1 GB |
| n8n | 0.5 GB | 0.5 GB |
| **Totale** | **~8 GB** | **~15 GB** |

### Ripartizione dello storage

| Componente | Spazio stimato | Note |
|---|---|---|
| Sistema operativo | 10 GB | - |
| Modelli Ollama | 2-20 GB | Dipende dal numero e dimensione dei modelli |
| Database MySQL | 1-5 GB | Dipende dal volume di documenti e conversazioni |
| Vector store Qdrant | 1-5 GB | Dipende dal numero di embedding |
| Documenti utente | Variabile | Dipende dall'utilizzo |
| **Totale base** | **~20 GB** | Senza documenti utente e con un solo modello |

### Supporto GPU

L'utilizzo di una GPU NVIDIA con supporto CUDA e' **opzionale** ma fortemente raccomandato per:

- Riduzione dei tempi di risposta LLM (da 30-60 secondi a 2-5 secondi per risposta).
- Possibilita' di utilizzare modelli piu' grandi (13B, 30B parametri).
- Miglioramento della qualita' delle risposte grazie a modelli piu' capaci.

**Requisiti GPU:**

| Requisito | Valore |
|---|---|
| Brand | NVIDIA |
| VRAM minima | 4 GB |
| VRAM raccomandata | 8+ GB |
| Driver | NVIDIA Driver 525+ |
| CUDA | 11.8+ |

---

## 8. Monitoraggio con Spring Boot Actuator

Spring Boot Actuator fornisce endpoint di monitoraggio integrati, abilitati nel profilo di produzione.

### Endpoint disponibili

| Endpoint | URL | Descrizione |
|---|---|---|
| Health | `/actuator/health` | Stato di salute dell'applicazione e delle sue dipendenze |
| Info | `/actuator/info` | Informazioni sull'applicazione (versione, build) |
| Metrics | `/actuator/metrics` | Metriche JVM, HTTP, database |
| Prometheus | `/actuator/prometheus` | Metriche in formato Prometheus (per Grafana) |

### Health Check dettagliato

```bash
curl http://localhost:8080/actuator/health | jq
```

Risposta attesa:

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 107374182400,
        "free": 53687091200,
        "threshold": 10485760
      }
    },
    "ollama": {
      "status": "UP",
      "details": {
        "url": "http://localhost:11434",
        "models": ["llama3.2", "nomic-embed-text"]
      }
    },
    "qdrant": {
      "status": "UP",
      "details": {
        "url": "http://localhost:6334",
        "collections": 1
      }
    }
  }
}
```

### Metriche utili

```bash
# Metriche JVM
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/jvm.threads.live

# Metriche HTTP
curl http://localhost:8080/actuator/metrics/http.server.requests

# Metriche database
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

### Integrazione con Grafana (opzionale)

Per un monitoraggio visuale avanzato, e' possibile integrare Prometheus e Grafana. Questi servizi possono essere eseguiti tramite Docker (opzionale):

```yaml
# docker-compose.yml (opzionale, solo per monitoraggio)
services:
  prometheus:
    image: prom/prometheus:latest
    container_name: localmind-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana:latest
    container_name: localmind-grafana
    ports:
      - "3000:3000"
    depends_on:
      - prometheus
```

---

## 9. Configurazione Nginx come Reverse Proxy

Per la produzione, si raccomanda l'utilizzo di Nginx come reverse proxy per:

- Servire i file statici del frontend Angular.
- Proxare le richieste API verso il backend Spring Boot.
- Gestire HTTPS (opzionale).
- Gestire la compressione gzip.

### Configurazione nginx.conf

```nginx
server {
    listen 80;
    server_name localhost;

    # Frontend Angular - file statici
    root /usr/share/nginx/html;
    index index.html;

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
    gzip_min_length 1000;

    # Angular routing - fallback a index.html per SPA
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy API verso Spring Boot backend
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Timeout per richieste LLM (possono essere lente)
        proxy_read_timeout 120s;
        proxy_send_timeout 120s;
    }

    # Proxy Actuator endpoints
    location /actuator/ {
        proxy_pass http://localhost:8080;
        # Limitare l'accesso in produzione
        # allow 127.0.0.1;
        # deny all;
    }

    # Security headers
    add_header X-Content-Type-Options nosniff;
    add_header X-Frame-Options DENY;
    add_header X-XSS-Protection "0";
    add_header Referrer-Policy strict-origin-when-cross-origin;
}
```

---

## 10. Backup e Restore

### Backup MySQL

```bash
# Backup completo del database
mysqldump -u localmind -plocalmind localmind > backup_$(date +%Y%m%d_%H%M%S).sql

# Backup compresso
mysqldump -u localmind -plocalmind localmind | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
```

### Restore MySQL

```bash
# Restore da file SQL
mysql -u localmind -plocalmind localmind < backup_20260209_120000.sql

# Restore da file compresso
gunzip -c backup_20260209_120000.sql.gz | mysql -u localmind -plocalmind localmind
```

### Backup Qdrant

```bash
# Se Qdrant e' in Docker, i dati sono nel volume Docker
docker run --rm -v localmind_localmind-qdrant-data:/source -v $(pwd)/backups:/backup \
    alpine tar czf /backup/qdrant_backup_$(date +%Y%m%d).tar.gz -C /source .

# Se Qdrant e' nativo, i dati sono nella directory di storage locale
tar czf backups/qdrant_backup_$(date +%Y%m%d).tar.gz -C /path/to/qdrant/storage .
```

### Backup completo

```bash
#!/bin/bash
# Script di backup completo LocalMind
BACKUP_DIR="./backups/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo "Backup MySQL..."
mysqldump -u localmind -plocalmind localmind | gzip > "$BACKUP_DIR/mysql.sql.gz"

echo "Backup Qdrant..."
# Adattare il percorso in base all'installazione (Docker o nativa)
if docker volume inspect localmind_localmind-qdrant-data > /dev/null 2>&1; then
    docker run --rm -v localmind_localmind-qdrant-data:/source -v "$BACKUP_DIR":/backup \
        alpine tar czf /backup/qdrant.tar.gz -C /source .
else
    echo "Qdrant non in Docker, eseguire backup manuale della directory di storage"
fi

echo "Backup completato in: $BACKUP_DIR"
ls -lh "$BACKUP_DIR"
```

### Frequenza di backup raccomandata

| Dato | Frequenza | Motivazione |
|---|---|---|
| MySQL | Giornaliero | Contiene conversazioni, metadati, configurazioni |
| Qdrant | Settimanale | Gli embedding possono essere rigenerati dai documenti |
| n8n | Settimanale | I workflow cambiano raramente |
| Modelli Ollama | Non necessario | Scaricabili nuovamente da `ollama pull` |
