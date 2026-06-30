# Development Environment

| | |
|---|---|
| **Document** | Development Environment Configuration Guide |
| **Version** | 0.1.0 |
| **Date** | 2026-02-09 |
| **Project** | LocalMind |

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Step 1: Clone the Repository](#2-step-1-clone-the-repository)
3. [Step 2: Configure Environment Variables](#3-step-2-configure-environment-variables)
4. [Step 3: Configure MySQL](#4-step-3-configure-mysql)
5. [Step 4: Download Ollama Models](#5-step-4-download-ollama-models)
6. [Step 5: Compile and Start the Backend](#6-step-5-compile-and-start-the-backend)
7. [Step 6: Install Dependencies and Start the Frontend](#7-step-6-install-dependencies-and-start-the-frontend)
8. [Installation Verification](#8-installation-verification)
9. [Troubleshooting](#9-troubleshooting)
   - 9.1 [Port Already in Use](#91-port-already-in-use)
   - 9.2 [Ollama Not Responding](#92-ollama-not-responding)
   - 9.3 [MySQL Connection Refused](#93-mysql-connection-refused)
   - 9.4 [Angular Build Errors](#94-angular-build-errors)
   - 9.5 [Memory Issues with Ollama](#95-memory-issues-with-ollama)

---

## 1. Prerequisites

Before proceeding with the development environment configuration, verify that the following tools are installed and correctly configured on your machine.

### Mandatory Software

| Software | Minimum Version | Installation Verification | Notes |
|---|---|---|---|
| **Java JDK** | 17+ | `java -version` | The full JDK is required, not just the JRE |
| **Maven** | 3.9+ | `mvn -version` | Maven Wrapper (`mvnw`) included in the project |
| **Node.js** | 22+ | `node -v` | Runtime for the Angular frontend |
| **npm** | 11+ | `npm -v` | Package manager for Node.js |
| **MySQL** | 8.0 | `mysql --version` | Relational database (native or via Docker) |
| **Git** | 2.40+ | `git --version` | Version control |

### Optional Software (Recommended)

| Software | Usage | Notes |
|---|---|---|
| **IntelliJ IDEA** (Ultimate or Community) | IDE for Java/Spring Boot backend development | Recommended plugins: Spring Boot, Lombok |
| **VS Code** | Alternative IDE or for frontend development | Recommended extensions: Angular Language Service, ESLint |
| **Postman** or **Insomnia** | REST API testing | Alternative: `curl` from terminal |
| **DBeaver** | GUI client for MySQL | Alternative: `mysql` from terminal |
| **Docker** (optional) | To run infrastructure services (Qdrant, Ollama, n8n) in containers | Not necessary if services are installed natively |

### Quick Prerequisites Verification

Run the following script to verify all prerequisites in a single operation:

```bash
echo "=== LocalMind Prerequisites Verification ==="
echo ""

echo "Java:"
java -version 2>&1 | head -1
echo ""

echo "Maven:"
mvn -version 2>&1 | head -1
echo ""

echo "Node.js:"
node -v
echo ""

echo "npm:"
npm -v
echo ""

echo "MySQL:"
mysql --version
echo ""

echo "Git:"
git --version
echo ""

echo "=== Verification complete ==="
```

---

## 2. Step 1: Clone the Repository

```bash
git clone <repository-url> localmind
cd localmind
```

### Repository Structure After Cloning

```
localmind/
├── localmind-backend/          # Spring Boot backend (Maven multi-module)
│   ├── localmind-app/          # Application module (main, controller)
│   ├── localmind-domain/       # Domain module (entities, ports)
│   ├── localmind-application/  # Application services module
│   ├── localmind-infrastructure/ # Infrastructure module (adapters, config)
│   └── pom.xml                 # Parent POM
├── localmind-frontend/         # Angular frontend
│   ├── src/
│   ├── angular.json
│   └── package.json
├── scripts/                    # Setup and startup scripts
│   ├── setup-mysql.sh          # MySQL database setup (Linux/macOS)
│   ├── setup-mysql.bat         # MySQL database setup (Windows)
│   ├── start-backend.sh        # Backend startup (Linux/macOS)
│   ├── start-backend.bat       # Backend startup (Windows)
│   ├── start-frontend.sh       # Frontend startup (Linux/macOS)
│   ├── start-frontend.bat      # Frontend startup (Windows)
│   ├── start-all.sh            # Full startup (Linux/macOS)
│   └── start-all.bat           # Full startup (Windows)
├── .env.example                # Environment variables template
├── .gitignore
└── README.md
```

---

## 3. Step 2: Configure Environment Variables

```bash
cp .env.example .env
```

Edit the `.env` file with a text editor:

```bash
# With VS Code
code .env

# With nano
nano .env

# With vim
vim .env
```

### .env File Contents

```env
# ==================================================
# LocalMind - Environment Variables
# ==================================================

# MySQL
DB_USERNAME=localmind
DB_PASSWORD=localmind

# LLM Providers (optional - leave empty for local-only mode)
OPENAI_API_KEY=
ANTHROPIC_API_KEY=
GOOGLE_API_KEY=

# n8n
N8N_BASIC_AUTH_USER=admin
N8N_BASIC_AUTH_PASSWORD=admin
```

**Note:** For local development, the default values for MySQL and n8n are sufficient. Cloud provider API keys are optional and can be added later.

---

## 4. Step 3: Configure MySQL

The `setup-mysql.sh` script (or `setup-mysql.bat` on Windows) automatically detects whether MySQL is installed natively or available via Docker, and configures the database accordingly.

```bash
./scripts/setup-mysql.sh
```

Alternatively, it is possible to configure MySQL manually:

### Manual MySQL Configuration

```bash
# Access MySQL as root
mysql -u root -p

# Create the database and user
CREATE DATABASE localmind CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'localmind'@'localhost' IDENTIFIED BY 'localmind';
GRANT ALL PRIVILEGES ON localmind.* TO 'localmind'@'localhost';
FLUSH PRIVILEGES;
```

### Connection Verification

```bash
mysql -h localhost -u localmind -plocalmind localmind -e "SELECT 1;"
```

### Optional Infrastructure Services

Qdrant, Ollama, and n8n services can be run natively or via Docker. To start via Docker (optional):

```bash
# Start only infrastructure services (without MySQL)
docker compose up -d qdrant ollama n8n
```

### Services and Ports

| Service | Local Port | Protocol | Verification |
|---|---|---|---|
| MySQL | `3306` | TCP | `mysql -h localhost -u localmind -plocalmind localmind` |
| Qdrant REST | `6333` | HTTP | `curl http://localhost:6333/healthz` |
| Qdrant gRPC | `6334` | gRPC | - |
| Ollama | `11434` | HTTP | `curl http://localhost:11434/api/tags` |
| n8n | `5678` | HTTP | Open `http://localhost:5678` in the browser |

### Quick Infrastructure Verification

```bash
echo "MySQL:"
mysqladmin ping -h localhost -u localmind -plocalmind && echo "OK" || echo "ERROR"

echo ""
echo "Qdrant:"
curl -s http://localhost:6333/healthz && echo " OK" || echo "ERROR"

echo ""
echo "Ollama:"
curl -s http://localhost:11434/api/tags > /dev/null && echo "OK" || echo "ERROR"

echo ""
echo "n8n:"
curl -s -o /dev/null -w "%{http_code}" http://localhost:5678 && echo " OK" || echo "ERROR"
```

---

## 5. Step 4: Download Ollama Models

After starting Ollama for the first time (native or in Docker), you need to download the LLM models that will be used by LocalMind.

### Chat Model (mandatory)

```bash
ollama pull llama3.2
```

Estimated time: 5-15 minutes (depends on internet connection and model size).

### Embedding Model (mandatory for RAG)

```bash
ollama pull nomic-embed-text
```

Estimated time: 1-3 minutes.

### Verify Installed Models

```bash
ollama list
```

Expected output:

```
NAME                    ID              SIZE      MODIFIED
llama3.2:latest         xxxxxxxxx       2.0 GB    x minutes ago
nomic-embed-text:latest xxxxxxxxx       274 MB    x minutes ago
```

### Alternative Models (optional)

| Model | Size | Usage | Command |
|---|---|---|---|
| `llama3.2` | ~2 GB | Chat (default, good balance) | `ollama pull llama3.2` |
| `llama3.2:1b` | ~1.3 GB | Chat (lightweight, for machines with limited RAM) | `ollama pull llama3.2:1b` |
| `mistral` | ~4 GB | Chat (alternative to Llama) | `ollama pull mistral` |
| `codellama` | ~3.8 GB | Code generation | `ollama pull codellama` |
| `nomic-embed-text` | ~274 MB | Embedding (default) | `ollama pull nomic-embed-text` |
| `mxbai-embed-large` | ~670 MB | Embedding (higher quality) | `ollama pull mxbai-embed-large` |

**Note:** Models are saved in the local Ollama directory (`~/.ollama/models`) and persist across service restarts.

---

## 6. Step 5: Compile and Start the Backend

### Compilation

```bash
cd localmind-backend
mvn clean compile
```

Expected output at the end of compilation:

```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO]
[INFO] localmind-backend ................................. SUCCESS
[INFO] localmind-domain .................................. SUCCESS
[INFO] localmind-application ............................. SUCCESS
[INFO] localmind-infrastructure .......................... SUCCESS
[INFO] localmind-app ..................................... SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Starting the Application

Via the provided script:

```bash
./scripts/start-backend.sh
```

Or manually:

```bash
cd localmind-backend
mvn spring-boot:run -pl localmind-app
```

Expected output at startup:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.x.x)

...
Tomcat started on port 8080 (http)
Started LocalMindApplication in X.XXX seconds
```

### Backend Verification

```bash
curl http://localhost:8080/api/v1/dashboard/health
```

Expected response:

```json
{
  "status": "UP",
  "components": {
    "ollama": { "status": "UP" },
    "mysql": { "status": "UP" },
    "qdrant": { "status": "UP" }
  }
}
```

---

## 7. Step 6: Install Dependencies and Start the Frontend

### Installing Dependencies

```bash
cd localmind-frontend
npm install
```

Estimated time: 1-3 minutes (first run). Dependencies are cached in `node_modules/`.

### Starting the Angular Dev Server

Via the provided script:

```bash
./scripts/start-frontend.sh
```

Or manually:

```bash
cd localmind-frontend
ng serve
```

Or via npm script:

```bash
npm start
```

Expected output:

```
** Angular Live Development Server is listening on localhost:4200 **

✔ Compiled successfully.
```

### Quick Full Startup

To start backend and frontend together:

```bash
./scripts/start-all.sh
```

### Frontend Verification

Open in the browser: **http://localhost:4200**

The main page of the LocalMind application should be displayed. The Angular dev server supports live reload: any modification to source files will cause the page to automatically reload in the browser.

---

## 8. Installation Verification

After completing all steps, verify that the entire stack is functional:

### Verification Checklist

| Component | URL/Command | Expected Status |
|---|---|---|
| MySQL | `mysqladmin ping -h localhost -u localmind -plocalmind` | `mysqld is alive` |
| Qdrant | `curl http://localhost:6333/healthz` | `OK` (HTTP 200) |
| Ollama | `curl http://localhost:11434/api/tags` | JSON with model list |
| n8n | `http://localhost:5678` (browser) | n8n web interface |
| Backend | `curl http://localhost:8080/api/v1/dashboard/health` | JSON with status UP |
| Frontend | `http://localhost:4200` (browser) | LocalMind UI |

### Port Map

```
┌─────────────────────────────────────────────┐
│                  localhost                     │
│                                               │
│   :4200  ─── Angular Dev Server (Frontend)    │
│   :8080  ─── Spring Boot (Backend API)        │
│   :3306  ─── MySQL (Database)                 │
│   :6333  ─── Qdrant REST API (Vector Store)   │
│   :6334  ─── Qdrant gRPC (Vector Store)       │
│   :11434 ─── Ollama (LLM Inference)           │
│   :5678  ─── n8n (Workflow Automation)         │
│                                               │
└─────────────────────────────────────────────┘
```

---

## 9. Troubleshooting

### 9.1 Port Already in Use

**Symptom:** Error when starting a service with message `Address already in use` or `port is already allocated`.

**Cause:** Another process is already using the required port.

**Solution:**

```bash
# Identify the process occupying the port (example: port 8080)
sudo lsof -i :8080

# Or with netstat
sudo netstat -tlnp | grep 8080

# Terminate the process (replace PID with the found value)
kill -9 <PID>
```

**Alternative:** Modify the port in the configuration file:

- Spring Boot backend: modify `server.port` in `application.yml`
- MySQL: modify the port in the `my.cnf` configuration file

### 9.2 Ollama Not Responding

**Symptom:** `curl http://localhost:11434/api/tags` returns `Connection refused` or timeout.

**Possible causes and solutions:**

1. **Service not started:**
   ```bash
   # If Ollama is installed natively:
   sudo systemctl status ollama
   # If not active:
   sudo systemctl start ollama
   ```

2. **Service starting up (model loading):**
   ```bash
   # Check the logs
   journalctl -u ollama -f
   # Wait for the log to show "Listening on 0.0.0.0:11434"
   ```

3. **Insufficient resources:**
   ```bash
   # Check memory usage
   free -h
   # If memory is at the limit, consider a lighter model
   ```

4. **Port conflict:**
   ```bash
   # Check if another process is using port 11434
   sudo lsof -i :11434
   ```

### 9.3 MySQL Connection Refused

**Symptom:** The Spring Boot application cannot connect to MySQL with error `Connection refused`.

**Possible causes and solutions:**

1. **Service not started:**
   ```bash
   # Check MySQL service status
   sudo systemctl status mysql
   # If not active:
   sudo systemctl start mysql
   ```

2. **Wrong credentials:**
   ```bash
   # Check credentials in .env
   cat .env | grep DB_
   # Attempt manual connection
   mysql -h localhost -u localmind -plocalmind localmind -e "SELECT 1;"
   ```

3. **Database not yet created:**
   ```bash
   # Run the setup script
   ./scripts/setup-mysql.sh
   # Or create manually:
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS localmind CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   ```

4. **Database reset:**
   ```bash
   # WARNING: this deletes all database data!
   mysql -u root -p -e "DROP DATABASE localmind; CREATE DATABASE localmind CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   ```

### 9.4 Angular Build Errors

**Symptom:** `ng serve` returns compilation errors.

**Possible causes and solutions:**

1. **Dependencies not installed:**
   ```bash
   cd localmind-frontend
   rm -rf node_modules package-lock.json
   npm install
   ```

2. **Incompatible Node.js version:**
   ```bash
   node -v
   # If the version is lower than 22, update Node.js
   # With nvm:
   nvm install 22
   nvm use 22
   ```

3. **Corrupted Angular cache:**
   ```bash
   cd localmind-frontend
   rm -rf .angular/cache
   ng serve
   ```

4. **TypeScript type errors:**
   ```bash
   # Check types
   npx tsc --noEmit
   ```

### 9.5 Memory Issues with Ollama

**Symptom:** Ollama responds slowly or the process is terminated due to Out Of Memory (OOM).

**Causes and solutions:**

1. **Model too large for available RAM:**
   ```bash
   # Check available RAM
   free -h
   # Use a lighter model
   ollama pull llama3.2:1b
   ```

2. **Check and increase available RAM:**
   - Close unnecessary applications to free up memory.
   - Consider a RAM upgrade if using large models.

3. **Use GPU (if available):**
   Ollama automatically detects NVIDIA GPUs with installed CUDA drivers. Verify with:
   ```bash
   nvidia-smi
   ```
