# Setup and Startup Scripts

| | |
|---|---|
| **Document** | Setup and Startup Scripts Documentation |
| **Version** | 0.1.0 |
| **Date** | 2026-02-09 |
| **Project** | LocalMind |

---

## Table of Contents

1. [Overview](#1-overview)
2. [Prerequisites](#2-prerequisites)
3. [Available Scripts](#3-available-scripts)
   - 3.1 [setup-mysql (.sh / .bat)](#31-setup-mysql-sh--bat)
   - 3.2 [start-backend (.sh / .bat)](#32-start-backend-sh--bat)
   - 3.3 [start-frontend (.sh / .bat)](#33-start-frontend-sh--bat)
   - 3.4 [start-all (.sh / .bat)](#34-start-all-sh--bat)
4. [First Startup Flow](#4-first-startup-flow)
5. [Database Configuration](#5-database-configuration)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Overview

The `scripts/` folder contains scripts for setting up and starting LocalMind, available for both **Linux/macOS** (`.sh`) and **Windows** (`.bat`).

```
scripts/
├── setup-mysql.sh       # MySQL database setup (Linux/macOS)
├── setup-mysql.bat      # MySQL database setup (Windows)
├── start-backend.sh     # Spring Boot backend startup (Linux/macOS)
├── start-backend.bat    # Spring Boot backend startup (Windows)
├── start-frontend.sh    # Angular frontend startup (Linux/macOS)
├── start-frontend.bat   # Angular frontend startup (Windows)
├── start-all.sh         # Backend + frontend startup (Linux/macOS)
└── start-all.bat        # Backend + frontend startup (Windows)
```

### Making Scripts Executable (Linux/macOS)

```bash
chmod +x scripts/*.sh
```

On Windows, `.bat` files are executable by default.

---

## 2. Prerequisites

| Software | Minimum Version | Verification | Required For |
|---|---|---|---|
| **Java JDK** | 17+ | `java -version` | Backend |
| **Maven** | 3.9+ | `mvn -version` | Backend |
| **Node.js** | 22+ | `node -v` | Frontend |
| **npm** | 10+ | `npm -v` | Frontend |
| **MySQL** | 8.0+ | `mysql --version` | Database |
| **Docker** | 24+ (optional) | `docker --version` | MySQL via Docker |

**Note:** MySQL can be installed natively or run in a Docker container. The `setup-mysql` script automatically detects both modes.

---

## 3. Available Scripts

### 3.1 setup-mysql (.sh / .bat)

Creates the `localmind` database and configures the application user.

#### Execution

```bash
# Linux/macOS
./scripts/setup-mysql.sh

# Windows
scripts\setup-mysql.bat
```

#### Interactive Flow

The script guides the user through the following steps:

```
1. MySQL Detection
   ├── Local MySQL client (native)
   ├── Docker container with MySQL
   └── Both → asks which one to use

2. Administrator Credentials
   ├── MySQL root user (default: root)
   ├── Root password
   ├── Host (only if local, default: 127.0.0.1)
   └── Port (only if local, default: 3306)

3. Root Connection Verification

4. Create 'localmind' database
   └── charset: utf8mb4, collation: utf8mb4_unicode_ci

5. Application User Configuration
   ├── "new" option     → create user + password + grant
   └── "existing" option → grant privileges only

6. Connection Test with Application User

7. Configuration Summary
```

#### Automatic MySQL Detection

The script automatically checks:

| Check | Description |
|---|---|
| `command -v mysql` | Verifies if the native MySQL client is in the PATH |
| `docker ps --filter "ancestor=mysql"` | Searches for Docker containers based on the `mysql` image |
| `docker exec <container> mysql --version` | Verifies that the container has the MySQL client |

If it detects MySQL in Docker, it uses `docker exec -i <container> mysql` to execute SQL commands.

#### Example Output (Docker)

```
=== LocalMind - MySQL Setup ===

MySQL detected in Docker (container: mysql-db-root).

MySQL root user [root]:
Password for 'root':

Verifying connection to MySQL...
Connection OK.

Creating 'localmind' database...
Database 'localmind' created.

Do you want to create a new user or use an existing one? [new/existing]: existing
Existing username: root
Password for user 'root':
Assigning privileges on 'localmind' to user 'root'...

Connection test with user 'root'...
+----------------+
| status         |
+----------------+
| Connection OK! |
+----------------+

=== Setup complete ===

Summary:
  Mode:     Docker (container: mysql-db-root)
  App host: localhost:3306 (port exposed by container)
  Database: localmind
  User:     root
```

#### Resulting Configuration

After execution, verify that `application-dev.yml` reflects the chosen credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/localmind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: <chosen user>
    password: <chosen password>
```

---

### 3.2 start-backend (.sh / .bat)

Compiles and starts the Spring Boot backend with the `dev` profile.

#### Execution

```bash
# Linux/macOS
./scripts/start-backend.sh

# Windows
scripts\start-backend.bat
```

#### What the Script Does

| Step | Command | Description |
|---|---|---|
| 1. Verify Java | `java -version` | Checks that Java 17+ is installed |
| 2. Verify Maven | `mvn -version` | Checks that Maven is installed |
| 3. Compilation | `mvn install -DskipTests -q` | Compiles all modules and installs JARs in the local Maven repository |
| 4. Startup | `mvn -pl localmind-app spring-boot:run -Dspring-boot.run.profiles=dev` | Starts the `localmind-app` module with the `dev` profile |

#### Dev Profile

The `dev` profile (defined in `application-dev.yml`) configures:

| Parameter | Value |
|---|---|
| Database | `jdbc:mysql://localhost:3306/localmind` |
| Ollama | `http://localhost:11434` |
| Qdrant | `localhost:6334` |
| n8n | `http://localhost:5678` |
| Log level | `DEBUG` for `com.localmind` |
| SQL logging | Enabled with formatting |

#### Expected Output

```
=== LocalMind - Backend Startup ===
Directory: /path/to/localmind/localmind-backend

Java version: 21
Compiling and installing modules...

Starting Spring Boot (profile: dev)...

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.2)

...
Tomcat started on port 8080 (http)
Started LocalMindApplication in X.XXX seconds
```

#### Verification

```bash
curl http://localhost:8080/actuator/health
```

---

### 3.3 start-frontend (.sh / .bat)

Installs dependencies and starts the Angular dev server.

#### Execution

```bash
# Linux/macOS
./scripts/start-frontend.sh

# Windows
scripts\start-frontend.bat
```

#### What the Script Does

| Step | Command | Description |
|---|---|---|
| 1. Verify Node.js | `node -v` | Checks that Node.js 22+ is installed |
| 2. Install dependencies | `npm ci` | Only if `node_modules/` does not exist |
| 3. Startup | `npm start` (alias for `ng serve`) | Starts the Angular dev server |

#### Expected Output

```
=== LocalMind - Frontend Startup ===
Directory: /path/to/localmind/localmind-frontend

Node.js version: v22.x.x
Starting Angular dev server at http://localhost:4200 ...

** Angular Live Development Server is listening on localhost:4200 **

✔ Compiled successfully.
```

#### Dev Server Features

| Feature | Description |
|---|---|
| **Live reload** | Automatic browser reload on every source file modification |
| **Hot Module Replacement** | Partial update without full page reload |
| **Source maps** | Mapping between compiled code and source for browser debugging |

#### Verification

Open **http://localhost:4200** in the browser.

---

### 3.4 start-all (.sh / .bat)

Starts backend and frontend together.

#### Execution

```bash
# Linux/macOS
./scripts/start-all.sh

# Windows
scripts\start-all.bat
```

#### Behavior by Platform

| Platform | Behavior | How to Stop |
|---|---|---|
| **Linux/macOS** | Starts backend and frontend as parallel processes in the same terminal | `Ctrl+C` stops both |
| **Windows** | Opens backend and frontend in **separate cmd windows** | Close individual windows |

#### Startup Flow

```
1. Start start-backend.sh/bat
2. Wait 5 seconds (time for initial compilation)
3. Start start-frontend.sh/bat
4. Both processes remain active
```

#### Resulting Ports

| Service | URL |
|---|---|
| Backend API | http://localhost:8080 |
| Frontend UI | http://localhost:4200 |

---

## 4. First Startup Flow

Complete sequence for starting LocalMind for the first time:

```
Step 1: Ensure MySQL is active
        (native or Docker)
          │
Step 2: ./scripts/setup-mysql.sh
        Create 'localmind' database
        Configure user
          │
Step 3: Verify application-dev.yml
        Credentials must match
          │
Step 4: Start external services (optional)
        - Ollama: ollama serve
        - Qdrant: docker run qdrant/qdrant
        - n8n:    n8n start
          │
Step 5: ./scripts/start-all.sh
        Or start backend and frontend separately
          │
Step 6: Open http://localhost:4200
```

### Subsequent Startups

After the initial setup, it is sufficient to:

```bash
# Ensure MySQL is active, then:
./scripts/start-all.sh
```

---

## 5. Database Configuration

### Spring Boot Configuration Files

| File | Environment | Usage |
|---|---|---|
| `application.yml` | Base | Active profile, server port |
| `application-dev.yml` | Development | Localhost credentials, DEBUG logging |
| `application-prod.yml` | Production | Credentials via env vars, WARN logging |

### MySQL Connection Parameters

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/localmind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: localmind
    password: localmind
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

| JDBC URL Parameter | Description |
|---|---|
| `useSSL=false` | Disables SSL (local development) |
| `allowPublicKeyRetrieval=true` | Allows public key retrieval for authentication |
| `serverTimezone=UTC` | Sets the server timezone to UTC |

### Flyway Migrations

Tables are automatically created by Flyway on the first backend startup:

| Migration | Table | Description |
|---|---|---|
| `V1` | `documents` | Uploaded and indexed documents |
| `V2` | `folder_configs` | Monitored folder configurations |
| `V3` | `llm_usage` | LLM usage and cost tracking |
| `V4` | `conversations`, `chat_messages` | Chat conversations and messages |
| `V5` | `webhooks` | Webhook configurations for events |
| `V6` | `mcp_servers` | MCP server registrations |

---

## 6. Troubleshooting

### 6.1 mysql: Command Not Found

**Cause:** The MySQL client is not installed or is not in the PATH.

**Solution - Local client:**
```bash
# Ubuntu/Debian
sudo apt-get install -y mysql-client

# Fedora/RHEL
sudo dnf install -y mysql

# macOS
brew install mysql-client

# Arch
sudo pacman -S mariadb-clients
```

**Solution - MySQL in Docker:**
The script automatically detects MySQL in Docker. If the container is active, it uses `docker exec` without needing the local client.

### 6.2 Cannot Connect to MySQL

**Cause 1:** MySQL is not active.
```bash
# Check native MySQL
sudo systemctl status mysql

# Check MySQL in Docker
docker ps | grep mysql
```

**Cause 2:** Using `localhost` instead of `127.0.0.1` (Docker MySQL).
When MySQL runs in Docker, `localhost` uses the Unix socket which is not available. The script automatically detects Docker and uses `docker exec` to connect.

**Cause 3:** Wrong root password.
Check the Docker container environment variables:
```bash
docker inspect <container> --format '{{.Config.Env}}' | tr ' ' '\n' | grep MYSQL
```

### 6.3 BUILD FAILURE: Unable to find a suitable main class

**Cause:** The script attempts to execute `spring-boot:run` on the parent module (pom).

**Solution:** The script has been corrected to execute `mvn install -DskipTests` (compiles all modules) and then `mvn -pl localmind-app spring-boot:run` (starts only the app module).

### 6.4 Could not resolve dependencies

**Cause:** Internal modules are not installed in the local Maven repository.

**Solution:** The script executes `mvn install -DskipTests` which installs all JARs. If the problem persists:
```bash
cd localmind-backend
mvn clean install -DskipTests
```

### 6.5 Angular: node_modules Not Found

**Cause:** npm dependencies not installed.

**Solution:** The `start-frontend` script automatically runs `npm ci` if `node_modules/` does not exist. To force reinstallation:
```bash
cd localmind-frontend
rm -rf node_modules
npm ci
```

### 6.6 Port Already in Use

**Cause:** Another process is occupying port 8080 (backend) or 4200 (frontend).

**Solution:**
```bash
# Identify the process
sudo lsof -i :8080
sudo lsof -i :4200

# Terminate the process
kill <PID>
```

### 6.7 Java Version Too Old

**Cause:** The script checks that Java is >= 17. If you have an older version:

**Solution:**
```bash
# With SDKMAN (recommended)
sdk install java 17.0.11-tem
sdk use java 17.0.11-tem

# With apt (Ubuntu)
sudo apt-get install -y openjdk-17-jdk
sudo update-alternatives --config java
```
