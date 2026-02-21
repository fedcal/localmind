# Installation Guide

| | |
|---|---|
| **Document** | Installation and Deployment Guide |
| **Version** | 1.0.0 |
| **Date** | 2026-02-14 |
| **Project** | LocalMind |

---

## Table of Contents

1. [Overview](#1-overview)
2. [Prerequisites](#2-prerequisites)
3. [Quick Installation](#3-quick-installation)
4. [Full Docker Installation](#4-full-docker-installation)
5. [Developer Installation](#5-developer-installation)
6. [Environment Configuration (.env)](#6-environment-configuration-env)
7. [NVIDIA GPU Support](#7-nvidia-gpu-support-optional)
8. [Troubleshooting](#8-troubleshooting)
9. [Useful Commands](#9-useful-commands)

---

## 1. Overview

LocalMind is a local-first AI platform for document management, semantic search, and multi-provider LLM chat. Users download the repository and run it entirely on their own PC without external cloud dependencies (unless cloud LLM providers are explicitly configured).

### Installation Options

LocalMind supports three deployment modes:

| Mode | Backend | Frontend | Database | Infrastructure | Use Case |
|---|---|---|---|---|---|
| **Quick Install** | Native | Native | Docker | Docker | First-time users, development |
| **Full Docker** | Docker | Docker | Docker | Docker | Production-ready, isolated environment |
| **Developer** | Native | Native | Docker | Docker or native | Backend/frontend development, fast iteration |

---

## 2. Prerequisites

### Docker + Docker Compose (Required for all modes)

Docker and Docker Compose v2 are required to run the database and optional infrastructure services.

```bash
# Verify Docker installation
docker --version
# Expected: Docker version 24+ (any 24.x, 25.x, etc.)

# Verify Docker Compose v2
docker compose version
# Expected: Docker Compose version 2.x.x
```

**Installation:**

- **Ubuntu/Debian**: `sudo apt-get install -y docker.io docker-compose-plugin`
- **Fedora/RHEL**: `sudo dnf install -y docker docker-compose-plugin`
- **macOS**: Install [Docker Desktop](https://www.docker.com/products/docker-desktop)
- **Windows**: Install [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop)

### Native Mode Prerequisites (Optional, for development)

If running backend and frontend natively instead of in containers:

| Software | Minimum Version | Verification | Purpose |
|---|---|---|---|
| **Java JDK** | 17+ | `java -version` | Spring Boot backend compilation |
| **Maven** | 3.9+ | `mvn -version` | Dependency management and build |
| **Node.js** | 22+ | `node -v` | Angular frontend runtime |
| **npm** | 11+ | `npm -v` | Frontend package management |

**Installation:**

- **Java 17+**: Use [SDKMAN](https://sdkman.io/) or package manager (`apt install openjdk-17-jdk`)
- **Maven 3.9+**: Use SDKMAN or download from [apache.org](https://maven.apache.org/)
- **Node.js 22+**: Use [nvm](https://github.com/nvm-sh/nvm) or download from [nodejs.org](https://nodejs.org/)

### NVIDIA GPU (Optional)

For GPU-accelerated Ollama inference (10-20x faster than CPU):

| Component | Requirement |
|---|---|
| **GPU** | NVIDIA GPU with CUDA Compute Capability 3.0+ |
| **Driver** | NVIDIA Driver 525+ |
| **CUDA Toolkit** | 11.2+ (auto-installed with driver) |
| **NVIDIA Container Toolkit** | Required only for Docker Ollama |

**Verification:**

```bash
nvidia-smi
# Should show GPU model, memory, and driver version
```

---

## 3. Quick Installation

The quickest way to get LocalMind running with automated setup.

### Step 1: Clone the Repository

```bash
git clone <repository-url> localmind
cd localmind
```

### Step 2: Prepare Environment

```bash
# Copy environment template to .env
cp .env.example .env
```

Edit `.env` with your preferred settings (see Section 6 for details). For most users, the defaults work fine.

### Step 3: Start Infrastructure

```bash
# Start MySQL, Qdrant, and Ollama in Docker
docker compose up -d
```

Wait 30 seconds for services to initialize, then verify:

```bash
docker compose ps
# All services should show "Up" status
```

### Step 4: Setup Database

```bash
# Create the database and configure the application user
./scripts/setup-mysql.sh
```

The script will:
- Detect MySQL (native or Docker)
- Prompt for root credentials
- Create the `localmind` database
- Configure the application user
- Test the connection

### Step 5: Download Ollama Models

```bash
# Download chat model (mandatory)
docker exec -it localmind-ollama ollama pull llama3.2

# Download embedding model (mandatory for RAG)
docker exec -it localmind-ollama ollama pull nomic-embed-text
```

Estimated time: 10-20 minutes (one-time, depends on internet speed).

**Verify installed models:**

```bash
docker exec localmind-ollama ollama list
```

### Step 6: Start Backend and Frontend

```bash
# Option A: Start both at once
./scripts/start-all.sh

# Option B: Start separately in different terminals
# Terminal 1 - Backend
./scripts/start-backend.sh

# Terminal 2 - Frontend
./scripts/start-frontend.sh
```

### Step 7: Access LocalMind

Open http://localhost:4200 in your web browser.

The application is ready when:
- Frontend loads without errors
- Backend health check passes: `curl http://localhost:8080/api/v1/dashboard/health`

---

## 4. Full Docker Installation

Run everything in Docker containers for a production-ready, fully isolated environment.

### Prerequisites

- Docker Desktop or Docker Engine with Docker Compose v2
- 4+ GB RAM available (8+ GB recommended)
- 50 GB free disk space (for Ollama models)

### Step 1: Clone and Configure

```bash
git clone <repository-url> localmind
cd localmind
cp .env.example .env
```

Edit `.env` to configure:

```env
# For full Docker mode, change infrastructure hosts to Docker service names:
DB_HOST=mysql
OLLAMA_HOST=ollama
QDRANT_HOST=qdrant
N8N_HOST=n8n

# Other settings (can keep defaults or customize)
DB_USERNAME=root
DB_PASSWORD=secure-password-here
OLLAMA_CHAT_MODEL=llama3.2
OLLAMA_EMBED_MODEL=nomic-embed-text
LLM_DEFAULT_PROVIDER=OLLAMA

# Optional: Cloud LLM providers
OPENAI_API_KEY=
ANTHROPIC_API_KEY=
GOOGLE_API_KEY=
```

### Step 2: Build and Start Containers

```bash
# Build all images and start containers in the background
docker compose --profile full up -d --build
```

This profile includes:
- **mysql**: MySQL 8.0 database
- **qdrant**: Vector store for semantic search
- **ollama**: Local LLM inference engine
- **n8n**: Workflow automation platform
- **backend**: Spring Boot API (when profile includes it)
- **frontend**: Angular UI (when profile includes it)

### Step 3: Initialize Database

Wait 30 seconds for MySQL to be ready, then:

```bash
# Run Flyway migrations automatically
# The backend container will execute them on startup

# Verify database is created
docker compose exec mysql mysql -u root -psecure-password-here -e \
  "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='localmind';"
```

### Step 4: Download Ollama Models

```bash
# Chat model (mandatory)
docker compose exec ollama ollama pull llama3.2

# Embedding model (mandatory for RAG)
docker compose exec ollama ollama pull nomic-embed-text

# Verify installation
docker compose exec ollama ollama list
```

### Step 5: Verify All Services

```bash
# Check all containers are running
docker compose ps

# Verify each service is responding
echo "Backend:" && curl -s http://localhost:8080/api/v1/dashboard/health | head -c 100
echo ""
echo "Frontend:" && curl -s http://localhost:4200 | head -c 100
echo ""
echo "Qdrant:" && curl -s http://localhost:6333/healthz
echo ""
echo "Ollama:" && curl -s http://localhost:11434/api/tags | head -c 100
```

### Step 6: Access LocalMind

- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080/api/v1
- **Qdrant Dashboard**: http://localhost:6333/dashboard
- **n8n Workflows**: http://localhost:5678

### Stopping and Cleaning Up

```bash
# Stop all containers (data preserved)
docker compose down

# Stop and delete all data (WARNING: destructive)
docker compose down -v

# View logs
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f ollama
```

---

## 5. Developer Installation

Optimal setup for developers: backend and frontend run natively for fast iteration, infrastructure runs in Docker.

### Step 1: Prerequisites Check

```bash
# Verify all prerequisites
echo "=== LocalMind Prerequisites ===" && \
echo "Java: $(java -version 2>&1 | head -1)" && \
echo "Maven: $(mvn -version 2>&1 | head -1)" && \
echo "Node.js: $(node -v)" && \
echo "npm: $(npm -v)" && \
echo "Docker: $(docker --version)" && \
echo "Docker Compose: $(docker compose version | head -1)"
```

All must show versions >= minimum required.

### Step 2: Clone and Configure

```bash
git clone <repository-url> localmind
cd localmind
cp .env.example .env
```

Edit `.env` to use localhost for native services:

```env
DB_HOST=localhost
DB_PORT=3306
OLLAMA_HOST=localhost
OLLAMA_PORT=11434
QDRANT_HOST=localhost
QDRANT_PORT=6334
N8N_HOST=localhost
N8N_PORT=5678
```

### Step 3: Start Infrastructure Services

```bash
# Start MySQL, Qdrant, Ollama in Docker
docker compose up -d
```

Wait 30 seconds, then verify:

```bash
docker compose ps
```

### Step 4: Setup Database

```bash
# Create database and configure user
./scripts/setup-mysql.sh
```

### Step 5: Download Ollama Models

**Option A: If Ollama is running in Docker**

```bash
docker exec -it localmind-ollama ollama pull llama3.2
docker exec -it localmind-ollama ollama pull nomic-embed-text
```

**Option B: If Ollama is installed natively**

```bash
ollama pull llama3.2
ollama pull nomic-embed-text
ollama list
```

### Step 6: Start Backend

Open a terminal and run:

```bash
./scripts/start-backend.sh
```

Expected output:

```
Tomcat started on port 8080 (http)
Started LocalMindApplication in X.XXX seconds
```

Verify backend is ready:

```bash
curl http://localhost:8080/api/v1/dashboard/health
```

### Step 7: Start Frontend

Open a new terminal and run:

```bash
./scripts/start-frontend.sh
```

Expected output:

```
Angular Live Development Server is listening on localhost:4200
Compiled successfully.
```

### Step 8: Development Workflow

Open http://localhost:4200 in your browser.

The application now supports live reload:
- **Frontend**: Any change to TypeScript/HTML/SCSS automatically recompiles and reloads the browser
- **Backend**: Must manually restart (Ctrl+C then re-run `./scripts/start-backend.sh`)

### Stopping Developer Environment

```bash
# Stop backend
# In the backend terminal, press Ctrl+C

# Stop frontend
# In the frontend terminal, press Ctrl+C

# Stop infrastructure
docker compose down

# Stop and delete infrastructure data (clean slate)
docker compose down -v
```

---

## 6. Environment Configuration (.env)

The `.env` file controls all deployment settings. Copy `.env.example` to `.env` and customize values.

### Database Configuration

```env
# MySQL Connection
DB_HOST=localhost          # 'localhost' for native, 'mysql' for full Docker
DB_PORT=3306              # Standard MySQL port
DB_NAME=localmind          # Database name (created automatically by setup script)
DB_USERNAME=root           # Application username (created by setup script)
DB_PASSWORD=               # Application password (leave blank for default)
```

### LLM Configuration

```env
# Ollama (Local LLM Provider)
OLLAMA_HOST=localhost      # 'localhost' for native, 'ollama' for full Docker
OLLAMA_PORT=11434          # Standard Ollama port
OLLAMA_CHAT_MODEL=llama3.2          # Chat model name (must be pulled via 'ollama pull')
OLLAMA_EMBED_MODEL=nomic-embed-text # Embedding model for RAG

# Provider Selection
LLM_DEFAULT_PROVIDER=OLLAMA          # Default provider: OLLAMA, OPENAI, ANTHROPIC, GOOGLE
LLM_OLLAMA_ENABLED=true              # Enable Ollama
LLM_OPENAI_ENABLED=false             # Enable OpenAI (requires OPENAI_API_KEY)
LLM_ANTHROPIC_ENABLED=false          # Enable Anthropic (requires ANTHROPIC_API_KEY)
LLM_GOOGLE_ENABLED=false             # Enable Google (requires GOOGLE_API_KEY)

# Cloud Provider API Keys (Optional, leave blank for local-only mode)
OPENAI_API_KEY=                      # OpenAI API key
OPENAI_MODEL=gpt-4o                  # OpenAI model to use
ANTHROPIC_API_KEY=                   # Anthropic API key
ANTHROPIC_MODEL=claude-sonnet-4-20250514  # Anthropic model to use
GOOGLE_API_KEY=                      # Google API key
```

### Vector Store Configuration

```env
# Qdrant (Vector Store for Semantic Search)
QDRANT_HOST=localhost                # 'localhost' for native, 'qdrant' for full Docker
QDRANT_PORT=6334                     # gRPC port (used by Spring AI)
QDRANT_COLLECTION=localmind-documents # Collection name
```

### Server Configuration

```env
# Spring Boot Server
SERVER_PORT=8080            # Backend API port
```

### Document Management

```env
# Document Upload Settings
DOCUMENT_UPLOAD_DIR=~/.localmind/uploads  # Local directory for uploaded documents
MAX_FILE_SIZE=50MB                        # Maximum single file size
```

### Workflow Automation (n8n)

```env
# n8n (Workflow Automation Platform)
N8N_HOST=localhost                   # 'localhost' for native, 'n8n' for full Docker
N8N_PORT=5678                        # n8n web interface port
N8N_BASIC_AUTH_USER=admin            # n8n login username
N8N_BASIC_AUTH_PASSWORD=localmind    # n8n login password
```

### Host Mapping for Different Deployment Modes

When switching deployment modes, update host values:

| Service | Native Mode | Full Docker Mode |
|---|---|---|
| MySQL | `localhost` | `mysql` |
| Ollama | `localhost` | `ollama` |
| Qdrant | `localhost` | `qdrant` |
| n8n | `localhost` | `n8n` |

---

## 7. NVIDIA GPU Support (Optional)

GPU acceleration makes Ollama 10-20x faster than CPU-only inference.

### Verify GPU Availability

```bash
nvidia-smi
```

Output should show:
- GPU model name and VRAM amount
- NVIDIA Driver version (must be 525+)
- CUDA compute capability (3.0+)

### Enable GPU in docker-compose.yml

Edit `docker-compose.yml` in the `ollama` service section:

```yaml
ollama:
  image: ollama/ollama:latest
  container_name: localmind-ollama
  ports:
    - "11434:11434"
  volumes:
    - localmind-ollama-data:/root/.ollama
  restart: unless-stopped
  # Uncomment the following section to enable GPU:
  deploy:
    resources:
      reservations:
        devices:
          - driver: nvidia
            count: all
            capabilities: [gpu]
```

Save the file and restart Ollama:

```bash
docker compose up -d --force-recreate ollama
```

### Verify GPU Usage

```bash
# Check GPU is available to Ollama
docker exec localmind-ollama nvidia-smi

# Run a model and monitor GPU usage
docker exec localmind-ollama ollama run llama3.2 "Hello, what GPU are you using?"

# In another terminal, monitor GPU
watch nvidia-smi
```

### Install NVIDIA Container Toolkit (Docker Only)

If GPU is not detected in Docker, install the container toolkit:

**Ubuntu/Debian:**

```bash
distribution=$(. /etc/os-release;echo $ID$VERSION_ID) && \
curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey | \
  sudo gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg && \
curl -s -L https://nvidia.github.io/libnvidia-container/$distribution/libnvidia-container.list | \
  sed 's#deb https://#deb [signed-by=/usr/share/keyrings/nvidia-container-toolkit-keyring.gpg] https://#g' | \
  sudo tee /etc/apt/sources.list.d/nvidia-container-toolkit.list

sudo apt-get update && sudo apt-get install -y nvidia-container-toolkit
sudo nvidia-ctk runtime configure --runtime=docker
sudo systemctl restart docker
```

**Fedora/RHEL:**

```bash
distribution=$(. /etc/os-release;echo $ID$VERSION_ID) && \
sudo dnf config-manager --add-repo \
  https://nvidia.github.io/libnvidia-container/$distribution/libnvidia-container.repo && \
sudo dnf install -y nvidia-container-toolkit

sudo nvidia-ctk runtime configure --runtime=docker
sudo systemctl restart docker
```

---

## 8. Troubleshooting

### MySQL Service Won't Start

**Symptom:** `docker compose up -d` fails or MySQL container exits immediately.

**Solution:**

```bash
# Check if port 3306 is already in use
sudo lsof -i :3306

# If port is free, check MySQL logs
docker compose logs mysql

# Verify .env has correct DB_PASSWORD (can be empty)
grep DB_PASSWORD .env

# Reset MySQL volume (WARNING: deletes database data)
docker compose down -v
docker compose up -d
```

### Ollama Models Download is Very Slow

**Symptom:** `ollama pull llama3.2` takes hours or stalls.

**Cause:** Large model file (2-4 GB) transfers are network-dependent.

**Solution:**

```bash
# Check download progress
docker compose logs -f ollama

# If stalled for >5 minutes, cancel and retry
docker exec localmind-ollama ollama pull llama3.2

# Try a smaller model if network is unreliable
docker exec localmind-ollama ollama pull llama3.2:1b
```

### Backend Cannot Connect to MySQL

**Symptom:** Backend fails to start with error `java.sql.SQLException: Cannot get a connection, pool error Timeout waiting for an idle object`.

**Solution:**

```bash
# Check MySQL is running and responsive
docker compose exec mysql mysql -u root -p -e "SELECT 1;"

# Verify .env credentials are correct
grep "DB_" .env

# Check backend logs for connection URL
docker compose logs -f backend | grep "jdbc:mysql"

# If Docker MySQL: use 'mysql' as host, not 'localhost'
# If native MySQL: use 'localhost' as host, ensure MySQL service is running
sudo systemctl status mysql
```

### Frontend Shows "Cannot Reach Backend" Error

**Symptom:** Browser shows error connecting to backend API.

**Solution:**

```bash
# Verify backend is running
curl http://localhost:8080/api/v1/dashboard/health

# Check if backend port 8080 is blocked by firewall
sudo lsof -i :8080

# Verify backend health endpoint
# Should return JSON like: { "status": "UP", ... }

# Check frontend is trying correct backend URL
# In browser DevTools, Network tab should show requests to http://localhost:8080/api/v1/*
```

### Qdrant Vector Store Connection Fails

**Symptom:** Backend logs show `Failed to connect to Qdrant` or `Connection refused`.

**Solution:**

```bash
# Verify Qdrant is running
docker compose ps qdrant

# Check if port 6334 is responding
curl -v telnet://localhost:6334
# Or: nc -zv localhost 6334

# Verify Qdrant REST API (port 6333)
curl http://localhost:6333/healthz

# Check Qdrant logs
docker compose logs -f qdrant

# If using full Docker: ensure .env has QDRANT_HOST=qdrant (not localhost)
```

### Docker Compose Command Not Found

**Symptom:** `bash: docker-compose: command not found`

**Solution:** Docker Compose v2 uses `docker compose` (not `docker-compose`):

```bash
# Check installed version
docker compose version

# If not found, install Docker Compose v2
sudo apt-get install -y docker-compose-plugin  # Ubuntu/Debian
sudo dnf install -y docker-compose-plugin       # Fedora/RHEL

# Or install latest standalone version
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### Out of Memory (OOM) Killer Terminates Services

**Symptom:** Services crash with no error in logs, or `Killed` message in `docker compose logs`.

**Cause:** Insufficient RAM. Ollama models require 3-8 GB, plus database and frontend RAM usage.

**Solution:**

```bash
# Check available memory
free -h

# Check memory usage by containers
docker stats

# Use smaller Ollama model
docker exec localmind-ollama ollama pull llama3.2:1b

# Increase system RAM (buy more RAM, or close other applications)

# Enable GPU to reduce RAM usage
# Edit docker-compose.yml to enable NVIDIA GPU as shown in Section 7
```

### Ports Already in Use

**Symptom:** Error `Bind for 0.0.0.0:8080 failed: port is already allocated`.

**Solution:**

```bash
# Find process using port 8080
sudo lsof -i :8080
sudo netstat -tlnp | grep 8080

# Kill the process (replace PID with the number shown)
kill -9 <PID>

# Or change port in .env or docker-compose.yml
# Edit SERVER_PORT or ports: section
```

### GPU Not Detected in Docker Ollama

**Symptom:** `nvidia-smi` works natively but `docker exec ollama nvidia-smi` fails.

**Solution:**

```bash
# Verify NVIDIA Container Toolkit is installed
dpkg -l | grep nvidia-container

# If not installed, install it (see Section 7)

# Verify Docker daemon configuration
cat /etc/docker/daemon.json | grep nvidia

# Restart Docker
sudo systemctl restart docker

# Recreate Ollama container to pick up GPU support
docker compose up -d --force-recreate ollama

# Verify GPU in container
docker exec localmind-ollama nvidia-smi
```

### Node.js or npm Version Incorrect

**Symptom:** Frontend fails to compile with error about Node version.

**Solution:**

```bash
# Check current versions
node -v
npm -v

# Update Node.js to 22+ (using nvm, recommended)
nvm install 22
nvm use 22
nvm alias default 22

# Verify versions
node -v
npm -v

# Clean npm cache and reinstall dependencies
cd localmind-frontend
rm -rf node_modules package-lock.json
npm install
```

### Java Version Too Old

**Symptom:** Backend won't compile with error about Java version.

**Solution:**

```bash
# Check current Java version
java -version

# Install Java 17+ using SDKMAN (recommended)
sdk install java 21.0.1-tem
sdk use java 21.0.1-tem

# Or use package manager
sudo apt-get install -y openjdk-21-jdk  # Ubuntu/Debian
sudo dnf install -y java-21-openjdk      # Fedora/RHEL

# Verify new version
java -version
```

---

## 9. Useful Commands

### Container Lifecycle Management

```bash
# Start all services in background
docker compose up -d

# Start only specific services
docker compose up -d mysql ollama

# Stop all services (data preserved)
docker compose stop

# Restart a service
docker compose restart ollama

# Stop and remove containers (data preserved in volumes)
docker compose down

# Stop, remove containers, AND delete data (DESTRUCTIVE)
docker compose down -v

# View service status
docker compose ps

# View logs (all services)
docker compose logs -f

# View logs for specific service
docker compose logs -f ollama

# View last N lines of logs
docker compose logs --tail=50 mysql
```

### Service Health Verification

```bash
# Backend health
curl http://localhost:8080/api/v1/dashboard/health

# Qdrant REST API
curl http://localhost:6333/healthz

# Ollama API
curl http://localhost:11434/api/tags

# MySQL connectivity
docker compose exec mysql mysql -u root -p -e "SELECT 1;"

# n8n web interface
curl http://localhost:5678
```

### Ollama Model Management

```bash
# List installed models
docker exec localmind-ollama ollama list

# Download a model
docker exec localmind-ollama ollama pull llama3.2

# Remove a model
docker exec localmind-ollama ollama rm llama3.2

# Run a model for testing
docker exec -it localmind-ollama ollama run llama3.2 "Hello!"
```

### Database Management

```bash
# Execute SQL query
docker compose exec mysql mysql -u root -p -e "SELECT * FROM documents LIMIT 1;"

# Backup database
docker compose exec mysql mysqldump -u root -p localmind > backup.sql

# Restore database
docker compose exec -T mysql mysql -u root -p localmind < backup.sql

# View migration status
# Flyway migrations are logged during backend startup
docker compose logs backend | grep "Flyway"
```

### Resource Monitoring

```bash
# Real-time CPU, memory, network usage
docker stats

# Show disk usage
docker system df

# Show specific volume disk usage
docker volume inspect localmind_localmind-ollama-data

# Total disk usage of localmind project
du -sh ~/.localmind/uploads
du -sh ~/.ollama/models
docker system df
```

### Cleanup and Maintenance

```bash
# Remove unused images
docker image prune

# Remove unused containers
docker container prune

# Remove unused volumes
docker volume prune

# Full cleanup (WARNING: removes all unused Docker objects)
docker system prune

# Pull latest images
docker compose pull

# Update and rebuild containers
docker compose pull && docker compose up -d --build
```

### Network Diagnostics

```bash
# Check if port is in use
sudo lsof -i :8080
sudo netstat -tlnp | grep 8080

# Check Docker network
docker network ls

# Inspect docker network
docker network inspect localmind_default

# Test connectivity within Docker
docker run --rm -it --network localmind_default curlimages/curl curl http://mysql:3306
```

---

## Next Steps

After successful installation:

1. **Access LocalMind**: Open http://localhost:4200
2. **Configure LLM Providers**: Settings > LLM Providers (optional cloud providers)
3. **Upload Documents**: Features > Documents > Upload (for semantic search)
4. **Start Chat**: Features > Chat (uses configured LLM provider)
5. **Explore MCP Tools**: Features > MCP (AI-powered workflows)

For detailed feature documentation, see:
- Development Environment: `documentation/10-deployment/01-development-environment.md`
- Docker Compose: `documentation/10-deployment/03-docker-compose.md`
- Backend Testing: `documentation/05-technology-stack/04-backend-testing.md`

