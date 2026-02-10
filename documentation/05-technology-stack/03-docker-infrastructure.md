# Infrastructure and Services

**Project:** LocalMind
**Version:** 0.1.0
**Date:** 2026-02-09

---

## Table of Contents

1. [Overview](#1-overview)
2. [MySQL 8.0](#2-mysql-80)
3. [Qdrant Service (optional)](#3-qdrant-service-optional)
4. [Ollama Service (optional)](#4-ollama-service-optional)
5. [n8n Service (optional)](#5-n8n-service-optional)
6. [Startup Scripts](#6-startup-scripts)
7. [Persistent Volumes (Docker)](#7-persistent-volumes-docker)
8. [Operational Commands](#8-operational-commands)

---

## 1. Overview

The LocalMind local development infrastructure no longer relies on a single `docker-compose.yml` file for the entire stack. The backend and frontend are run natively on the host via dedicated scripts in the `scripts/` folder.

The infrastructure services (Qdrant, Ollama, n8n) are **optional** and can be run either natively or via Docker, depending on the developer's preference. MySQL can be installed natively or run in Docker; the setup script automatically detects the available mode.

### Service Architecture

```
+------------------+     +------------------+     +------------------+
|   MySQL 8.0      |     |      Qdrant      |     |      Ollama      |
|   :3306          |     |  :6333 (REST)    |     |   :11434         |
|  (native/Docker) |     |  :6334 (gRPC)    |     |  (native/Docker) |
+--------+---------+     | (native/Docker)  |     +--------+---------+
         |               +--------+---------+              |
         +------------------------+------------------------+
                                  |
                        localhost communication
                                  |
         +------------------------+------------------------+
         |                                                 |
+--------+---------+                            +----------+---------+
|       n8n        |                            |  Spring Boot App   |
|   :5678          |                            |  :8080 (native)    |
| (native/Docker)  |                            +--------------------+
+------------------+
```

---

## 2. MySQL 8.0

| Property         | Value                            |
|------------------|----------------------------------|
| **Version**      | 8.0                              |
| **Port**         | `3306`                           |
| **Mode**         | Native or Docker (`mysql:8.0`)   |

### Environment Variables (if Docker)

| Variable             | Value       | Description                    |
|----------------------|-------------|--------------------------------|
| `MYSQL_DATABASE`     | `localmind` | Database name                  |
| `MYSQL_USER`         | `localmind` | Database user                  |
| `MYSQL_PASSWORD`     | `localmind` | Database password              |
| `MYSQL_ROOT_PASSWORD`| `localmind` | Root password                  |

### Healthcheck (if Docker)

```yaml
healthcheck:
  test: ["CMD-SHELL", "mysqladmin ping -h localhost -u localmind -plocalmind"]
  interval: 10s
  timeout: 5s
  retries: 5
```

The healthcheck verifies database availability via the `mysqladmin ping` command, with a 10-second interval between attempts and a maximum of 5 retries.

### Native Installation

For native MySQL 8.0 installation on Linux:

```bash
# Debian/Ubuntu
sudo apt install mysql-server-8.0

# Verify
mysql --version
```

The project setup script automatically detects whether MySQL is available natively or needs to be started via Docker.

### Notes

- Port 3306 is exposed on the host to allow direct connection from the Spring Boot backend.
- The setup script automatically creates the database and the `localmind` user if they do not exist.

---

## 3. Qdrant Service (optional)

| Property           | Value                                   |
|--------------------|-----------------------------------------|
| **Image (Docker)** | `qdrant/qdrant:latest`                  |
| **Container name** | `localmind-qdrant`                      |
| **Exposed ports**  | `6333:6333` (REST), `6334:6334` (gRPC)  |
| **Volume**         | `localmind-qdrant-data:/qdrant/storage` |

### Ports

| Port   | Protocol   | Description                                                                                                 |
|--------|------------|-------------------------------------------------------------------------------------------------------------|
| `6333` | REST/HTTP  | REST API for management and debugging operations; web dashboard available at `http://localhost:6333/dashboard` |
| `6334` | gRPC       | High-performance protocol used by Spring AI for vector operations                                           |

### Healthcheck (Docker)

```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:6333/healthz || exit 1"]
  interval: 10s
  timeout: 5s
  retries: 5
```

### Notes

- The gRPC port 6334 is the one configured in Spring AI (`spring.ai.vectorstore.qdrant.port=6334`).
- The `localmind-documents` collection is automatically created by Spring AI on first insertion.
- The REST API on port 6333 is useful for inspecting collections and vector points during development.
- Qdrant can be run natively by downloading the binary from [qdrant.tech](https://qdrant.tech/).

---

## 4. Ollama Service (optional)

| Property           | Value                                 |
|--------------------|---------------------------------------|
| **Image (Docker)** | `ollama/ollama:latest`                |
| **Container name** | `localmind-ollama`                    |
| **Exposed port**   | `11434:11434`                         |
| **Volume**         | `localmind-ollama-data:/root/.ollama` |

### Models to Download

After startup (native or Docker), the required LLM models need to be downloaded:

| Model              | Purpose                      | Command (native)               | Command (Docker)                                            |
|--------------------|------------------------------|--------------------------------|-------------------------------------------------------------|
| `llama3.2`         | Chat and text generation     | `ollama pull llama3.2`         | `docker exec localmind-ollama ollama pull llama3.2`         |
| `nomic-embed-text` | Embedding generation         | `ollama pull nomic-embed-text` | `docker exec localmind-ollama ollama pull nomic-embed-text` |

### GPU Support (optional)

To enable NVIDIA GPU acceleration with Docker, add the `deploy` section to the service:

```yaml
deploy:
  resources:
    reservations:
      devices:
        - driver: nvidia
          count: 1
          capabilities: [gpu]
```

### GPU Prerequisites

- NVIDIA drivers installed on the host.
- NVIDIA Container Toolkit installed (`nvidia-docker2`) if using Docker.
- For native installation, Ollama automatically detects the available GPU.

### Notes

- Without a GPU, models run in CPU-only mode with reduced performance.
- The `localmind-ollama-data` volume preserves downloaded models between restarts (Docker).
- Port 11434 corresponds to the configuration `spring.ai.ollama.base-url=http://localhost:11434`.
- Ollama can be installed natively with `curl -fsSL https://ollama.ai/install.sh | sh`.

---

## 5. n8n Service (optional)

| Property           | Value                                |
|--------------------|--------------------------------------|
| **Image (Docker)** | `n8nio/n8n:latest`                   |
| **Container name** | `localmind-n8n`                      |
| **Exposed port**   | `5678:5678`                          |
| **Volume**         | `localmind-n8n-data:/home/node/.n8n` |

### Environment Variables (Docker)

| Variable                 | Value                       | Description                    |
|--------------------------|-----------------------------|--------------------------------|
| `N8N_BASIC_AUTH_ACTIVE`  | `true`                      | Enables basic authentication   |
| `N8N_BASIC_AUTH_USER`    | `admin`                     | Username for web UI access     |
| `N8N_BASIC_AUTH_PASSWORD`| `localmind`                 | Password for web UI access     |
| `WEBHOOK_URL`            | `http://localhost:5678/`    | Base URL for webhooks          |

### Notes

- n8n is used for automations and integration workflows (e.g., webhooks for post-indexing notifications).
- The web interface is accessible at `http://localhost:5678`.
- The `localmind.n8n.base-url=http://localhost:5678` configuration in the backend points to this instance.
- n8n can be installed natively via `npm install -g n8n`.

---

## 6. Startup Scripts

The project uses dedicated scripts in the `scripts/` folder for starting services:

| Script                      | Description                                                    |
|-----------------------------|----------------------------------------------------------------|
| `scripts/setup.sh`          | Initial setup: detects MySQL (native/Docker), creates DB and user |
| `scripts/start-backend.sh`  | Starts the Spring Boot backend natively                        |
| `scripts/start-frontend.sh` | Starts the Angular frontend natively                           |

> **Note**: the `docker-compose.yml` file for the full stack no longer exists. Optional infrastructure services (Qdrant, Ollama, n8n) can be started individually via Docker if needed.

---

## 7. Persistent Volumes (Docker)

If infrastructure services are run via Docker, the following volumes ensure data persistence:

| Volume                     | Mount Point in Container         | Description                          |
|----------------------------|----------------------------------|--------------------------------------|
| `localmind-qdrant-data`    | `/qdrant/storage`                | Vector collections and indexes       |
| `localmind-ollama-data`    | `/root/.ollama`                  | Downloaded LLM models                |
| `localmind-n8n-data`       | `/home/node/.n8n`                | n8n workflows and configuration      |

> **Note**: MySQL, when run natively, stores data in the operating system's default directory (`/var/lib/mysql` on Linux).

---

## 8. Operational Commands

### Starting the Project (native)

```bash
# Initial setup (detects MySQL, creates database)
./scripts/setup.sh

# Start the backend
./scripts/start-backend.sh

# Start the frontend (in another terminal)
./scripts/start-frontend.sh
```

### Starting Optional Infrastructure Services (Docker)

```bash
# Start Qdrant
docker run -d --name localmind-qdrant -p 6333:6333 -p 6334:6334 \
  -v localmind-qdrant-data:/qdrant/storage qdrant/qdrant:latest

# Start Ollama
docker run -d --name localmind-ollama -p 11434:11434 \
  -v localmind-ollama-data:/root/.ollama ollama/ollama:latest

# Start n8n
docker run -d --name localmind-n8n -p 5678:5678 \
  -v localmind-n8n-data:/home/node/.n8n \
  -e N8N_BASIC_AUTH_ACTIVE=true \
  -e N8N_BASIC_AUTH_USER=admin \
  -e N8N_BASIC_AUTH_PASSWORD=localmind \
  n8nio/n8n:latest
```

### Download Ollama Models

```bash
# Native
ollama pull llama3.2
ollama pull nomic-embed-text
ollama list

# Docker
docker exec localmind-ollama ollama pull llama3.2
docker exec localmind-ollama ollama pull nomic-embed-text
docker exec localmind-ollama ollama list
```

### Accessing the MySQL Database

```bash
# Native
mysql -u localmind -plocalmind localmind

# Docker
docker exec -it localmind-mysql mysql -u localmind -plocalmind localmind
```

### Stopping Docker Services (if started)

```bash
# Stop individual containers
docker stop localmind-qdrant localmind-ollama localmind-n8n

# Remove containers (preserves volumes)
docker rm localmind-qdrant localmind-ollama localmind-n8n
```
