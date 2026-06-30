export const content = `# Production Environment

| | |
|---|---|
| **Document** | Production Environment Documentation |
| **Version** | 0.1.0 |
| **Date** | 2026-02-09 |
| **Project** | LocalMind |

---

## Table of Contents

1. [Overview](#1-overview)
2. [Differences Between Development and Production](#2-differences-between-development-and-production)
3. [Spring Boot Production Profile](#3-spring-boot-production-profile)
4. [Production Build - Backend](#4-production-build---backend)
5. [Production Build - Frontend](#5-production-build---frontend)
6. [Starting in Production](#6-starting-in-production)
7. [Hardware Requirements](#7-hardware-requirements)
8. [Monitoring with Spring Boot Actuator](#8-monitoring-with-spring-boot-actuator)
9. [Nginx Configuration as Reverse Proxy](#9-nginx-configuration-as-reverse-proxy)
10. [Backup and Restore](#10-backup-and-restore)

---

## 1. Overview

The LocalMind production environment is designed for execution on the end user's machine in self-hosted mode. Unlike a traditional cloud deployment, "production" in LocalMind corresponds to the user's personal machine, where the software runs in a stable and optimized manner.

The main differences compared to the development environment are:

- Configurations optimized for stability and performance.
- Credentials managed through secure environment variables.
- Reduced logging to avoid overhead.
- Database schema validation (not automatic creation).
- Optimized and minified builds for backend and frontend.

---

## 2. Differences Between Development and Production

| Aspect | Development (\`dev\`) | Production (\`prod\`) |
|---|---|---|
| **Database hostname** | \`localhost\` | \`localhost\` (native MySQL) |
| **Ollama hostname** | \`localhost\` | \`localhost\` (native or configurable) |
| **Qdrant hostname** | \`localhost\` | \`localhost\` (native or configurable) |
| **n8n hostname** | \`localhost\` | \`localhost\` (native or configurable) |
| **Credentials** | Hardcoded in defaults | Mandatory environment variables |
| **DDL auto** | \`create\` (creates tables automatically) | \`validate\` (verifies existing schema) |
| **SQL logging** | \`true\` (shows queries in log) | \`false\` (no queries in logs) |
| **Log level** | \`DEBUG\` | \`INFO\` |
| **CORS origins** | \`http://localhost:4200\` | Configurable (same host or domain) |
| **Frontend** | Angular dev server (live reload) | Static build served by nginx |
| **Hot reload** | Enabled (Spring DevTools) | Disabled |

---

## 3. Spring Boot Production Profile

The production profile is defined in the \`application-prod.yml\` file:

\`\`\`yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/localmind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: \${DB_USERNAME}
    password: \${DB_PASSWORD}
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
\`\`\`

### Key Differences Compared to application.yml (dev)

| Property | Dev | Prod | Rationale |
|---|---|---|---|
| \`spring.datasource.url\` | \`jdbc:mysql://localhost:3306/localmind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC\` | \`jdbc:mysql://localhost:3306/localmind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC\` | In production a different host can be configured if needed |
| \`spring.jpa.hibernate.ddl-auto\` | \`create\` | \`validate\` | In production the schema must be managed through migrations (Flyway/Liquibase) |
| \`spring.jpa.show-sql\` | \`true\` | \`false\` | SQL queries in logs consume resources and can expose sensitive data |
| \`logging.level.root\` | \`DEBUG\` | \`INFO\` | Reduction of log volume for performance and readability |
| \`DB_USERNAME\`, \`DB_PASSWORD\` | Default (\`localmind\`) | No default (mandatory) | In production credentials must be explicitly provided |

---

## 4. Production Build - Backend

### Compilation and Packaging

\`\`\`bash
cd localmind-backend
mvn clean package -DskipTests
\`\`\`

**Note:** \`-DskipTests\` is used to speed up the production build. Tests should be run separately in the CI/CD pipeline or before manual deployment.

### Generated Artifact

\`\`\`
localmind-app/target/localmind-app-0.1.0-SNAPSHOT.jar
\`\`\`

This file is a **fat JAR** (uber JAR) that contains:

- All compiled classes from all Maven modules.
- All dependencies (Spring Boot, Spring AI, MySQL driver, etc.).
- The embedded Tomcat server.
- Configuration files (\`application.yml\`, \`application-prod.yml\`).

### Expected JAR Size

| Component | Estimated Size |
|---|---|
| Application code | ~5 MB |
| Spring Boot dependencies | ~40 MB |
| Spring AI dependencies | ~15 MB |
| Drivers and libraries | ~10 MB |
| **Estimated total** | **~70-80 MB** |

### JAR Verification

\`\`\`bash
# Verify that the JAR is valid
java -jar localmind-app/target/localmind-app-0.1.0-SNAPSHOT.jar --help

# Verify the contents
jar tf localmind-app/target/localmind-app-0.1.0-SNAPSHOT.jar | head -20
\`\`\`

---

## 5. Production Build - Frontend

### Optimized Compilation

\`\`\`bash
cd localmind-frontend
ng build --configuration production
\`\`\`

### Optimizations Applied in Production Mode

| Optimization | Description |
|---|---|
| **AOT Compilation** | Ahead-of-Time compilation of Angular templates |
| **Tree shaking** | Removal of unused code |
| **Minification** | Reduction of JavaScript and CSS file sizes |
| **Bundling** | Grouping of modules into few files |
| **Source maps** | Disabled (size reduction) |
| **Cache busting** | Hashes in file names for cache invalidation |

### Build Output

\`\`\`
dist/localmind-frontend/
├── browser/
│   ├── index.html
│   ├── main-[hash].js
│   ├── polyfills-[hash].js
│   ├── styles-[hash].css
│   ├── assets/
│   └── ...
\`\`\`

### Expected Size

| File | Estimated Size |
|---|---|
| \`main-[hash].js\` | ~200-400 KB (gzipped) |
| \`polyfills-[hash].js\` | ~30 KB (gzipped) |
| \`styles-[hash].css\` | ~20-50 KB (gzipped) |
| **Estimated total** | **~300-500 KB (gzipped)** |

### Serving Static Files

The static files generated by the build must be served by a web server. The recommended options are:

1. **Nginx** (recommended): high-performance web server, configurable as a reverse proxy.
2. **Apache HTTP Server**: established alternative.
3. **Caddy**: modern web server with automatic HTTPS.

---

## 6. Starting in Production

### Starting with Production Profile

\`\`\`bash
java -jar \\
  -Dspring.profiles.active=prod \\
  localmind-app/target/localmind-app-0.1.0-SNAPSHOT.jar
\`\`\`

### Starting with Environment Variables

\`\`\`bash
DB_USERNAME=localmind \\
DB_PASSWORD=secure_password_here \\
java -jar \\
  -Dspring.profiles.active=prod \\
  -Xms512m \\
  -Xmx2g \\
  localmind-app/target/localmind-app-0.1.0-SNAPSHOT.jar
\`\`\`

### Recommended JVM Parameters for Production

| Parameter | Recommended Value | Description |
|---|---|---|
| \`-Xms\` | \`512m\` | Initial heap memory |
| \`-Xmx\` | \`2g\` | Maximum heap memory |
| \`-XX:+UseG1GC\` | *(enabled by default in Java 17+)* | G1 garbage collector |
| \`-XX:MaxGCPauseMillis\` | \`200\` | Maximum target GC pause |
| \`-Dspring.profiles.active\` | \`prod\` | Spring Boot profile |

### Starting as a systemd Service (Linux)

To run LocalMind as a system service on Linux:

\`\`\`ini
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
ExecStart=/usr/bin/java \\
    -jar \\
    -Dspring.profiles.active=prod \\
    -Xms512m \\
    -Xmx2g \\
    /opt/localmind/localmind-app.jar
Restart=on-failure
RestartSec=10
EnvironmentFile=/opt/localmind/.env

[Install]
WantedBy=multi-user.target
\`\`\`

Management commands:

\`\`\`bash
# Enable the service at boot
sudo systemctl enable localmind

# Start the service
sudo systemctl start localmind

# Check the status
sudo systemctl status localmind

# View the logs
journalctl -u localmind -f
\`\`\`

---

## 7. Hardware Requirements

### Minimum Requirements

| Resource | Minimum | Recommended | Notes |
|---|---|---|---|
| **CPU** | 4 cores | 8 cores | 8 cores recommended for Ollama with large models |
| **RAM** | 8 GB | 16 GB | 16 GB with LLM models larger than 7B |
| **Storage** | 20 GB | 50+ GB | Space for documents, LLM models, database |
| **GPU** | Not required | NVIDIA with CUDA | Significantly accelerates LLM inference |

### RAM Allocation Breakdown

| Component | Estimated RAM (minimum) | Estimated RAM (recommended) |
|---|---|---|
| Operating system | 2 GB | 2 GB |
| MySQL | 0.5 GB | 1 GB |
| Qdrant | 0.5 GB | 1 GB |
| Ollama + LLM model | 3 GB | 8 GB |
| Spring Boot (JVM) | 1 GB | 2 GB |
| Angular / Nginx | 0.1 GB | 0.1 GB |
| n8n | 0.5 GB | 0.5 GB |
| **Total** | **~8 GB** | **~15 GB** |

### Storage Allocation Breakdown

| Component | Estimated Space | Notes |
|---|---|---|
| Operating system | 10 GB | - |
| Ollama models | 2-20 GB | Depends on the number and size of models |
| MySQL database | 1-5 GB | Depends on the volume of documents and conversations |
| Qdrant vector store | 1-5 GB | Depends on the number of embeddings |
| User documents | Variable | Depends on usage |
| **Base total** | **~20 GB** | Without user documents and with a single model |

### GPU Support

Using an NVIDIA GPU with CUDA support is **optional** but strongly recommended for:

- Reducing LLM response times (from 30-60 seconds to 2-5 seconds per response).
- Ability to use larger models (13B, 30B parameters).
- Improved response quality thanks to more capable models.

**GPU requirements:**

| Requirement | Value |
|---|---|
| Brand | NVIDIA |
| Minimum VRAM | 4 GB |
| Recommended VRAM | 8+ GB |
| Driver | NVIDIA Driver 525+ |
| CUDA | 11.8+ |

---

## 8. Monitoring with Spring Boot Actuator

Spring Boot Actuator provides built-in monitoring endpoints, enabled in the production profile.

### Available Endpoints

| Endpoint | URL | Description |
|---|---|---|
| Health | \`/actuator/health\` | Health status of the application and its dependencies |
| Info | \`/actuator/info\` | Application information (version, build) |
| Metrics | \`/actuator/metrics\` | JVM, HTTP, database metrics |
| Prometheus | \`/actuator/prometheus\` | Metrics in Prometheus format (for Grafana) |

### Detailed Health Check

\`\`\`bash
curl http://localhost:8080/actuator/health | jq
\`\`\`

Expected response:

\`\`\`json
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
\`\`\`

### Useful Metrics

\`\`\`bash
# JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/jvm.threads.live

# HTTP metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Database metrics
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
\`\`\`

### Grafana Integration (optional)

For advanced visual monitoring, it is possible to integrate Prometheus and Grafana. These services can be run via Docker (optional):

\`\`\`yaml
# docker-compose.yml (optional, monitoring only)
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
\`\`\`

---

## 9. Nginx Configuration as Reverse Proxy

For production, using Nginx as a reverse proxy is recommended for:

- Serving the Angular frontend static files.
- Proxying API requests to the Spring Boot backend.
- Managing HTTPS (optional).
- Handling gzip compression.

### nginx.conf Configuration

\`\`\`nginx
server {
    listen 80;
    server_name localhost;

    # Angular frontend - static files
    root /usr/share/nginx/html;
    index index.html;

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
    gzip_min_length 1000;

    # Angular routing - fallback to index.html for SPA
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy API to Spring Boot backend
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Timeout for LLM requests (can be slow)
        proxy_read_timeout 120s;
        proxy_send_timeout 120s;
    }

    # Proxy Actuator endpoints
    location /actuator/ {
        proxy_pass http://localhost:8080;
        # Restrict access in production
        # allow 127.0.0.1;
        # deny all;
    }

    # Security headers
    add_header X-Content-Type-Options nosniff;
    add_header X-Frame-Options DENY;
    add_header X-XSS-Protection "0";
    add_header Referrer-Policy strict-origin-when-cross-origin;
}
\`\`\`

---

## 10. Backup and Restore

### MySQL Backup

\`\`\`bash
# Full database backup
mysqldump -u localmind -plocalmind localmind > backup_$(date +%Y%m%d_%H%M%S).sql

# Compressed backup
mysqldump -u localmind -plocalmind localmind | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
\`\`\`

### MySQL Restore

\`\`\`bash
# Restore from SQL file
mysql -u localmind -plocalmind localmind < backup_20260209_120000.sql

# Restore from compressed file
gunzip -c backup_20260209_120000.sql.gz | mysql -u localmind -plocalmind localmind
\`\`\`

### Qdrant Backup

\`\`\`bash
# If Qdrant is in Docker, data is in the Docker volume
docker run --rm -v localmind_localmind-qdrant-data:/source -v $(pwd)/backups:/backup \\
    alpine tar czf /backup/qdrant_backup_$(date +%Y%m%d).tar.gz -C /source .

# If Qdrant is native, data is in the local storage directory
tar czf backups/qdrant_backup_$(date +%Y%m%d).tar.gz -C /path/to/qdrant/storage .
\`\`\`

### Full Backup

\`\`\`bash
#!/bin/bash
# LocalMind full backup script
BACKUP_DIR="./backups/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo "Backing up MySQL..."
mysqldump -u localmind -plocalmind localmind | gzip > "$BACKUP_DIR/mysql.sql.gz"

echo "Backing up Qdrant..."
# Adjust the path based on the installation (Docker or native)
if docker volume inspect localmind_localmind-qdrant-data > /dev/null 2>&1; then
    docker run --rm -v localmind_localmind-qdrant-data:/source -v "$BACKUP_DIR":/backup \\
        alpine tar czf /backup/qdrant.tar.gz -C /source .
else
    echo "Qdrant not in Docker, perform manual backup of the storage directory"
fi

echo "Backup completed in: $BACKUP_DIR"
ls -lh "$BACKUP_DIR"
\`\`\`

### Recommended Backup Frequency

| Data | Frequency | Rationale |
|---|---|---|
| MySQL | Daily | Contains conversations, metadata, configurations |
| Qdrant | Weekly | Embeddings can be regenerated from documents |
| n8n | Weekly | Workflows change rarely |
| Ollama models | Not necessary | Can be downloaded again via \`ollama pull\` |
`;
