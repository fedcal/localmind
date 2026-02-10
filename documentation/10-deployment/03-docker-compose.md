# Docker Compose Configuration (Optional)

| | |
|---|---|
| **Document** | Docker Compose Configuration Documentation |
| **Version** | 0.1.0 |
| **Date** | 2026-02-09 |
| **Project** | LocalMind |

---

## Table of Contents

1. [Overview](#1-overview)
2. [File Structure](#2-file-structure)
3. [Detailed Services](#3-detailed-services)
   - 3.1 [Qdrant](#31-qdrant)
   - 3.2 [Ollama](#32-ollama)
   - 3.3 [n8n](#33-n8n)
4. [Network](#4-network)
5. [Volumes](#5-volumes)
6. [Useful Commands](#6-useful-commands)
7. [GPU Configuration for Ollama](#7-gpu-configuration-for-ollama)
8. [Customization](#8-customization)

---

## 1. Overview

Docker Compose is used in LocalMind in an **optional** capacity for infrastructure services only. The backend (Spring Boot) and the frontend (Angular) are run natively via the scripts in the `scripts/` directory. The MySQL database is installed and managed natively (or in Docker, auto-detected by the `setup-mysql.sh` script).

The `docker-compose.yml` file is located in the **project root directory** and manages three optional infrastructure services:

| Service | Function | Image |
|---|---|---|
| **qdrant** | Vector store for embeddings and semantic search | `qdrant/qdrant:latest` |
| **ollama** | Local LLM inference | `ollama/ollama:latest` |
| **n8n** | Workflow automation | `n8nio/n8n:latest` |

**Note:** All these services can also be run natively without Docker. Docker Compose is provided as a convenience option for those who prefer the containerized approach.

All Docker services are configured with:

- **Persistent volumes**: data survives container restarts.
- **Health checks**: Docker periodically verifies the health status.
- **Restart policy**: containers are automatically restarted in case of crash.
- **Port mapping**: each service is accessible from `localhost`.

---

## 2. File Structure

```yaml
# docker-compose.yml
# LocalMind - Infrastructure Services (optional)
# Compose V2 format (no version field)
# Note: backend, frontend and MySQL are run natively

services:
  qdrant:
    # ... Qdrant configuration
  ollama:
    # ... Ollama configuration
  n8n:
    # ... n8n configuration

volumes:
  localmind-qdrant-data:
  localmind-ollama-data:
  localmind-n8n-data:
```

---

## 3. Detailed Services

### 3.1 Qdrant

Qdrant is the vector store used to store document embeddings and to perform high-performance semantic searches.

```yaml
qdrant:
  image: qdrant/qdrant:latest
  container_name: localmind-qdrant
  ports:
    - "6333:6333"
    - "6334:6334"
  volumes:
    - localmind-qdrant-data:/qdrant/storage
  healthcheck:
    test: ["CMD-SHELL", "curl -f http://localhost:6333/healthz || exit 1"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 20s
  restart: unless-stopped
```

#### Configuration Details

| Parameter | Value | Description |
|---|---|---|
| `image` | `qdrant/qdrant:latest` | Latest stable version of Qdrant |
| `container_name` | `localmind-qdrant` | Fixed container name |
| `ports` (REST) | `6333:6333` | REST API for CRUD operations and search |
| `ports` (gRPC) | `6334:6334` | gRPC API for high-performance communication (used by Spring AI) |
| `volumes` | `localmind-qdrant-data:/qdrant/storage` | Volume for collection and index persistence |
| `restart` | `unless-stopped` | Automatic restart |

#### Exposed Ports

| Port | Protocol | Usage |
|---|---|---|
| `6333` | HTTP/REST | REST API for collection management, point upload, search. Web interface at `http://localhost:6333/dashboard` |
| `6334` | gRPC | High-performance communication. Used by Spring AI for bulk operations and search |

#### Connection from the Spring Boot Application

| Environment | Host | Port |
|---|---|---|
| Development | `localhost` | `6334` (gRPC) |
| Production | `localhost` (configurable) | `6334` (gRPC) |

#### Storage Resources

Qdrant stores data in the `localmind-qdrant-data` volume:

- **Collections**: structures that group vectors by domain.
- **Segments**: internal subdivisions for search optimization.
- **HNSW indexes**: data structures for approximate nearest neighbor search.

---

### 3.2 Ollama

Ollama provides local LLM inference, allowing language models to be run without a connection to cloud services.

```yaml
ollama:
  image: ollama/ollama:latest
  container_name: localmind-ollama
  ports:
    - "11434:11434"
  volumes:
    - localmind-ollama-data:/root/.ollama
  restart: unless-stopped
  # For NVIDIA GPU support, uncomment the following section:
  # deploy:
  #   resources:
  #     reservations:
  #       devices:
  #         - driver: nvidia
  #           count: all
  #           capabilities: [gpu]
```

#### Configuration Details

| Parameter | Value | Description |
|---|---|---|
| `image` | `ollama/ollama:latest` | Latest stable version of Ollama |
| `container_name` | `localmind-ollama` | Fixed container name |
| `ports` | `11434:11434` | REST API for inference (compatible with OpenAI API format) |
| `volumes` | `localmind-ollama-data:/root/.ollama` | Volume for downloaded model persistence |
| `restart` | `unless-stopped` | Automatic restart |

#### Models to Download Manually

After the first container startup (or native service start), you need to download models manually:

```bash
# If Ollama is in Docker:
docker exec -it localmind-ollama ollama pull llama3.2
docker exec -it localmind-ollama ollama pull nomic-embed-text
docker exec localmind-ollama ollama list

# If Ollama is installed natively:
ollama pull llama3.2
ollama pull nomic-embed-text
ollama list
```

**Important note:** Models are saved in the `localmind-ollama-data` volume (Docker) or in `~/.ollama/models` (native) and persist across restarts. It is not necessary to re-download them after a service restart.

#### Ollama API

The Ollama API is accessible at `http://localhost:11434` and supports the following main endpoints:

| Endpoint | Method | Description |
|---|---|---|
| `/api/tags` | GET | List of installed models |
| `/api/generate` | POST | Text generation (completion) |
| `/api/chat` | POST | Chat with message history |
| `/api/embeddings` | POST | Embedding generation |
| `/api/pull` | POST | Model download |
| `/api/show` | POST | Model details |

#### Hardware Resources

| Resource | Without GPU | With NVIDIA GPU |
|---|---|---|
| CPU | Used for inference (slow) | Used for preprocessing |
| RAM | 3-8 GB (depends on model) | 2-4 GB (model resides in VRAM) |
| VRAM | N/A | 3-8 GB (depends on model) |
| Speed | ~5-15 tokens/s | ~30-100 tokens/s |

---

### 3.3 n8n

n8n is the workflow automation platform used by LocalMind to manage trigger-based automated flows.

```yaml
n8n:
  image: n8nio/n8n:latest
  container_name: localmind-n8n
  ports:
    - "5678:5678"
  environment:
    - N8N_BASIC_AUTH_ACTIVE=true
    - N8N_BASIC_AUTH_USER=${N8N_BASIC_AUTH_USER:-admin}
    - N8N_BASIC_AUTH_PASSWORD=${N8N_BASIC_AUTH_PASSWORD:-admin}
    - N8N_HOST=localhost
    - N8N_PORT=5678
    - N8N_PROTOCOL=http
    - WEBHOOK_URL=http://localhost:5678/
  volumes:
    - localmind-n8n-data:/home/node/.n8n
  restart: unless-stopped
```

#### Configuration Details

| Parameter | Value | Description |
|---|---|---|
| `image` | `n8nio/n8n:latest` | Latest stable version of n8n |
| `container_name` | `localmind-n8n` | Fixed container name |
| `ports` | `5678:5678` | Web interface and webhook API |
| `N8N_BASIC_AUTH_ACTIVE` | `true` | Enables Basic Auth authentication |
| `N8N_BASIC_AUTH_USER` | `${N8N_BASIC_AUTH_USER:-admin}` | Username from `.env`, default `admin` |
| `N8N_BASIC_AUTH_PASSWORD` | `${N8N_BASIC_AUTH_PASSWORD:-admin}` | Password from `.env`, default `admin` |
| `N8N_HOST` | `localhost` | Hostname for webhook URL generation |
| `WEBHOOK_URL` | `http://localhost:5678/` | Base URL for webhooks |
| `volumes` | `localmind-n8n-data:/home/node/.n8n` | Volume for workflow and credential persistence |
| `restart` | `unless-stopped` | Automatic restart |

#### Web Interface

Accessible at `http://localhost:5678` with the credentials configured in the environment variables.

The n8n web interface allows you to:

- Create and modify workflows visually (drag & drop).
- Configure triggers (webhook, cron, events).
- Test workflows in manual mode.
- Monitor executions and view logs.

---

## 4. Network

Docker Compose automatically creates a dedicated bridge network for the services defined in the file:

```
localmind_default (bridge)
```

### Communication Between Services

Infrastructure services in Docker communicate with each other through the internal network. The Spring Boot backend (which runs natively) connects to services via `localhost` and mapped ports:

| From | To | Hostname | Port |
|---|---|---|---|
| Spring Boot (native) | MySQL (native) | `localhost` | `3306` |
| Spring Boot (native) | Qdrant (Docker) | `localhost` | `6334` (gRPC) |
| Spring Boot (native) | Ollama (Docker or native) | `localhost` | `11434` |
| Spring Boot (native) | n8n (Docker or native) | `localhost` | `5678` |

### Binding to localhost (Secure Configuration)

To restrict service access exclusively to the local machine:

```yaml
ports:
  - "127.0.0.1:6333:6333"  # Localhost only
  # instead of
  - "6333:6333"             # All interfaces
```

---

## 5. Volumes

LocalMind uses Docker **named volumes** for infrastructure service data persistence:

```yaml
volumes:
  localmind-qdrant-data:
  localmind-ollama-data:
  localmind-n8n-data:
```

### Volume Details

| Volume | Mount Point in Container | Contents |
|---|---|---|
| `localmind-qdrant-data` | `/qdrant/storage` | Qdrant data (collections, segments, HNSW indexes) |
| `localmind-ollama-data` | `/root/.ollama` | Downloaded LLM models |
| `localmind-n8n-data` | `/home/node/.n8n` | Workflows, credentials, n8n configurations |

**Note:** The MySQL database is managed natively and its data resides in the standard system directory (`/var/lib/mysql` or equivalent).

### Volume Management

```bash
# List volumes
docker volume ls | grep localmind

# Inspect a volume (e.g., qdrant)
docker volume inspect localmind_localmind-qdrant-data

# Calculate used space
docker system df -v | grep localmind
```

### Volume Lifecycle

| Operation | Effect on Volumes |
|---|---|
| `docker compose stop` | Containers stopped, volumes **preserved** |
| `docker compose down` | Containers removed, volumes **preserved** |
| `docker compose down -v` | Containers removed, volumes **deleted** |
| `docker volume prune` | Orphan volumes **deleted** |

**Warning:** `docker compose down -v` deletes **all data** of the Docker services (Ollama models, Qdrant data, n8n workflows). Use with extreme caution and only after performing a backup. The MySQL database is not affected by this command as it is managed natively.

---

## 6. Useful Commands

### Lifecycle Management

```bash
# Start all infrastructure services in the background
docker compose up -d

# Start a single service
docker compose up -d qdrant

# Stop all services (preserves volumes)
docker compose stop

# Stop and remove containers (preserves volumes)
docker compose down

# Stop, remove containers AND volumes (DESTRUCTIVE)
docker compose down -v

# Restart a single service
docker compose restart ollama

# Recreate a service (e.g., after modifying docker-compose.yml)
docker compose up -d --force-recreate ollama
```

### Monitoring

```bash
# Status of all services
docker compose ps

# Logs of all services (live)
docker compose logs -f

# Logs of a single service (last 100 lines, live)
docker compose logs -f --tail=100 qdrant

# Resource statistics (CPU, RAM, I/O)
docker stats localmind-qdrant localmind-ollama localmind-n8n
```

### Container Access

```bash
# Interactive shell in a container
docker exec -it localmind-ollama bash
docker exec -it localmind-n8n sh

# Execute a specific command
docker exec localmind-ollama ollama list
```

### Maintenance

```bash
# Update images to the latest version
docker compose pull

# Update and recreate containers
docker compose pull && docker compose up -d

# Remove unused images
docker image prune

# Full Docker cleanup (unused images, containers, networks)
docker system prune
```

---

## 7. GPU Configuration for Ollama

To enable NVIDIA GPU support in Ollama (both Docker and native), the following is required:

### Prerequisites

1. **NVIDIA GPU** with CUDA support.
2. **NVIDIA Driver** version 525 or higher.
3. **NVIDIA Container Toolkit** installed (Docker only).

### NVIDIA Container Toolkit Installation (Ubuntu/Debian) - Docker Only

```bash
# Add the NVIDIA repository
distribution=$(. /etc/os-release;echo $ID$VERSION_ID) \
  && curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey | sudo gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg \
  && curl -s -L https://nvidia.github.io/libnvidia-container/$distribution/libnvidia-container.list | \
    sed 's#deb https://#deb [signed-by=/usr/share/keyrings/nvidia-container-toolkit-keyring.gpg] https://#g' | \
    sudo tee /etc/apt/sources.list.d/nvidia-container-toolkit.list

# Install the toolkit
sudo apt-get update && sudo apt-get install -y nvidia-container-toolkit

# Configure Docker to use the NVIDIA runtime
sudo nvidia-ctk runtime configure --runtime=docker
sudo systemctl restart docker
```

**Note:** If Ollama is installed natively, it automatically detects the NVIDIA GPU without needing the Container Toolkit.

### docker-compose.yml Configuration

Uncomment the `deploy` section in the `ollama` service:

```yaml
ollama:
  image: ollama/ollama:latest
  container_name: localmind-ollama
  ports:
    - "11434:11434"
  volumes:
    - localmind-ollama-data:/root/.ollama
  restart: unless-stopped
  deploy:
    resources:
      reservations:
        devices:
          - driver: nvidia
            count: all
            capabilities: [gpu]
```

### GPU Support Verification

```bash
# If in Docker:
docker exec localmind-ollama nvidia-smi
docker compose logs ollama | grep -i cuda

# If native:
nvidia-smi
ollama run llama3.2 "Hello, what GPU are you using?"
```

---

## 8. Customization

### Modifying Ports

To avoid conflicts with other services on the machine, you can modify the exposed ports:

```yaml
# Example: move Qdrant REST to port 16333
qdrant:
  ports:
    - "16333:6333"
    - "16334:6334"
```

**Note:** After modifying the ports, also update the Spring Boot `application.yml` file to reflect the new ports.

### Adding Resource Limits

```yaml
qdrant:
  deploy:
    resources:
      limits:
        cpus: "2.0"
        memory: 2G
      reservations:
        cpus: "0.5"
        memory: 512M
```

### Adding a Custom Service

To add a new service to the infrastructure (e.g., Redis for caching):

```yaml
redis:
  image: redis:7-alpine
  container_name: localmind-redis
  ports:
    - "6379:6379"
  volumes:
    - localmind-redis-data:/data
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 10s
    timeout: 5s
    retries: 5
  restart: unless-stopped

# Add the volume to the volumes section:
volumes:
  localmind-redis-data:
```
