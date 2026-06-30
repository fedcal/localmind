export const content = `# Infrastruttura e Servizi

**Progetto:** LocalMind
**Versione:** 0.1.0
**Data:** 2026-02-09

---

## Indice

1. [Panoramica](#1-panoramica)
2. [MySQL 8.0](#2-mysql-80)
3. [Servizio Qdrant (opzionale)](#3-servizio-qdrant-opzionale)
4. [Servizio Ollama (opzionale)](#4-servizio-ollama-opzionale)
5. [Servizio n8n (opzionale)](#5-servizio-n8n-opzionale)
6. [Script di Avvio](#6-script-di-avvio)
7. [Volumi Persistenti (Docker)](#7-volumi-persistenti-docker)
8. [Comandi Operativi](#8-comandi-operativi)

---

## 1. Panoramica

L'infrastruttura di sviluppo locale di LocalMind non si basa piu' su un unico file \`docker-compose.yml\` per l'intero stack. Il backend e il frontend vengono eseguiti nativamente sull'host tramite script dedicati nella cartella \`scripts/\`.

I servizi infrastrutturali (Qdrant, Ollama, n8n) sono **opzionali** e possono essere eseguiti sia nativamente che tramite Docker, a seconda della preferenza dello sviluppatore. MySQL puo' essere installato nativamente o eseguito in Docker; lo script di setup rileva automaticamente la modalita' disponibile.

### Architettura dei servizi

\`\`\`
+------------------+     +------------------+     +------------------+
|   MySQL 8.0      |     |      Qdrant      |     |      Ollama      |
|   :3306          |     |  :6333 (REST)    |     |   :11434         |
|  (nativo/Docker) |     |  :6334 (gRPC)    |     |  (nativo/Docker) |
+--------+---------+     | (nativo/Docker)  |     +--------+---------+
         |               +--------+---------+              |
         +------------------------+------------------------+
                                  |
                        Comunicazione localhost
                                  |
         +------------------------+------------------------+
         |                                                 |
+--------+---------+                            +----------+---------+
|       n8n        |                            |  Spring Boot App   |
|   :5678          |                            |  :8080 (nativo)    |
| (nativo/Docker)  |                            +--------------------+
+------------------+
\`\`\`

---

## 2. MySQL 8.0

| Proprieta'       | Valore                           |
|------------------|----------------------------------|
| **Versione**     | 8.0                              |
| **Porta**        | \`3306\`                           |
| **Modalita'**    | Nativo o Docker (\`mysql:8.0\`)    |

### Variabili d'ambiente (se Docker)

| Variabile            | Valore      | Descrizione                    |
|----------------------|-------------|--------------------------------|
| \`MYSQL_DATABASE\`     | \`localmind\` | Nome del database              |
| \`MYSQL_USER\`         | \`localmind\` | Utente database                |
| \`MYSQL_PASSWORD\`     | \`localmind\` | Password database              |
| \`MYSQL_ROOT_PASSWORD\`| \`localmind\` | Password root                  |

### Healthcheck (se Docker)

\`\`\`yaml
healthcheck:
  test: ["CMD-SHELL", "mysqladmin ping -h localhost -u localmind -plocalmind"]
  interval: 10s
  timeout: 5s
  retries: 5
\`\`\`

Il healthcheck verifica la disponibilita' del database tramite il comando \`mysqladmin ping\`, con intervallo di 10 secondi tra i tentativi e un massimo di 5 retry.

### Installazione nativa

Per l'installazione nativa di MySQL 8.0 su Linux:

\`\`\`bash
# Debian/Ubuntu
sudo apt install mysql-server-8.0

# Verifica
mysql --version
\`\`\`

Lo script di setup del progetto rileva automaticamente se MySQL e' disponibile nativamente o se e' necessario avviarlo tramite Docker.

### Note

- La porta 3306 e' esposta sull'host per consentire la connessione diretta dal backend Spring Boot.
- Lo script di setup crea automaticamente il database e l'utente \`localmind\` se non esistenti.

---

## 3. Servizio Qdrant (opzionale)

| Proprieta'         | Valore                                  |
|--------------------|-----------------------------------------|
| **Image (Docker)** | \`qdrant/qdrant:latest\`                  |
| **Container name** | \`localmind-qdrant\`                      |
| **Porte esposte**  | \`6333:6333\` (REST), \`6334:6334\` (gRPC)  |
| **Volume**         | \`localmind-qdrant-data:/qdrant/storage\` |

### Porte

| Porta  | Protocollo | Descrizione                                                                                                 |
|--------|------------|-------------------------------------------------------------------------------------------------------------|
| \`6333\` | REST/HTTP  | API REST per operazioni di gestione e debug; dashboard web disponibile su \`http://localhost:6333/dashboard\` |
| \`6334\` | gRPC       | Protocollo ad alte prestazioni utilizzato da Spring AI per operazioni su vettori                            |

### Healthcheck (Docker)

\`\`\`yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:6333/healthz || exit 1"]
  interval: 10s
  timeout: 5s
  retries: 5
\`\`\`

### Note

- La porta gRPC 6334 e' quella configurata in Spring AI (\`spring.ai.vectorstore.qdrant.port=6334\`).
- La collection \`localmind-documents\` viene creata automaticamente da Spring AI al primo inserimento.
- La REST API sulla porta 6333 e' utile per ispezionare le collection e i punti vettoriali durante lo sviluppo.
- Qdrant puo' essere eseguito nativamente scaricando il binario da [qdrant.tech](https://qdrant.tech/).

---

## 4. Servizio Ollama (opzionale)

| Proprieta'         | Valore                                |
|--------------------|---------------------------------------|
| **Image (Docker)** | \`ollama/ollama:latest\`                |
| **Container name** | \`localmind-ollama\`                    |
| **Porta esposta**  | \`11434:11434\`                         |
| **Volume**         | \`localmind-ollama-data:/root/.ollama\` |

### Modelli da scaricare

Dopo l'avvio (nativo o Docker), e' necessario scaricare i modelli LLM richiesti:

| Modello            | Scopo                        | Comando (nativo)               | Comando (Docker)                                            |
|--------------------|------------------------------|--------------------------------|-------------------------------------------------------------|
| \`llama3.2\`         | Chat e generazione testo     | \`ollama pull llama3.2\`         | \`docker exec localmind-ollama ollama pull llama3.2\`         |
| \`nomic-embed-text\` | Generazione embedding        | \`ollama pull nomic-embed-text\` | \`docker exec localmind-ollama ollama pull nomic-embed-text\` |

### Supporto GPU (opzionale)

Per abilitare l'accelerazione GPU NVIDIA con Docker, aggiungere la sezione \`deploy\` al servizio:

\`\`\`yaml
deploy:
  resources:
    reservations:
      devices:
        - driver: nvidia
          count: 1
          capabilities: [gpu]
\`\`\`

### Prerequisiti per GPU

- Driver NVIDIA installati sull'host.
- NVIDIA Container Toolkit installato (\`nvidia-docker2\`) se si usa Docker.
- Per installazione nativa, Ollama rileva automaticamente la GPU disponibile.

### Note

- Senza GPU, i modelli funzionano in modalita' CPU-only con prestazioni ridotte.
- Il volume \`localmind-ollama-data\` conserva i modelli scaricati tra i riavvii (Docker).
- La porta 11434 corrisponde alla configurazione \`spring.ai.ollama.base-url=http://localhost:11434\`.
- Ollama puo' essere installato nativamente con \`curl -fsSL https://ollama.ai/install.sh | sh\`.

---

## 5. Servizio n8n (opzionale)

| Proprieta'         | Valore                               |
|--------------------|--------------------------------------|
| **Image (Docker)** | \`n8nio/n8n:latest\`                   |
| **Container name** | \`localmind-n8n\`                      |
| **Porta esposta**  | \`5678:5678\`                          |
| **Volume**         | \`localmind-n8n-data:/home/node/.n8n\` |

### Variabili d'ambiente (Docker)

| Variabile                | Valore                      | Descrizione                    |
|--------------------------|-----------------------------|--------------------------------|
| \`N8N_BASIC_AUTH_ACTIVE\`  | \`true\`                      | Abilita autenticazione basic   |
| \`N8N_BASIC_AUTH_USER\`    | \`admin\`                     | Username per accesso web UI    |
| \`N8N_BASIC_AUTH_PASSWORD\`| \`localmind\`                 | Password per accesso web UI    |
| \`WEBHOOK_URL\`            | \`http://localhost:5678/\`    | URL base per webhook           |

### Note

- n8n e' utilizzato per automazioni e workflow di integrazione (es. webhook per notifiche post-indicizzazione).
- L'interfaccia web e' accessibile su \`http://localhost:5678\`.
- La configurazione \`localmind.n8n.base-url=http://localhost:5678\` nel backend punta a questa istanza.
- n8n puo' essere installato nativamente tramite \`npm install -g n8n\`.

---

## 6. Script di Avvio

Il progetto utilizza script dedicati nella cartella \`scripts/\` per l'avvio dei servizi:

| Script                      | Descrizione                                                   |
|-----------------------------|---------------------------------------------------------------|
| \`scripts/setup.sh\`          | Setup iniziale: rileva MySQL (nativo/Docker), crea DB e utente|
| \`scripts/start-backend.sh\`  | Avvia il backend Spring Boot nativamente                      |
| \`scripts/start-frontend.sh\` | Avvia il frontend Angular nativamente                         |

> **Nota**: il file \`docker-compose.yml\` per lo stack completo non esiste piu'. I servizi infrastrutturali opzionali (Qdrant, Ollama, n8n) possono essere avviati singolarmente tramite Docker se necessario.

---

## 7. Volumi Persistenti (Docker)

Se i servizi infrastrutturali sono eseguiti tramite Docker, i seguenti volumi garantiscono la persistenza dei dati:

| Volume                     | Mount Point nel container            | Descrizione                          |
|----------------------------|--------------------------------------|--------------------------------------|
| \`localmind-qdrant-data\`    | \`/qdrant/storage\`                    | Collection e indici vettoriali       |
| \`localmind-ollama-data\`    | \`/root/.ollama\`                      | Modelli LLM scaricati                |
| \`localmind-n8n-data\`       | \`/home/node/.n8n\`                    | Workflow e configurazione n8n        |

> **Nota**: MySQL, se eseguito nativamente, conserva i dati nella directory predefinita del sistema operativo (\`/var/lib/mysql\` su Linux).

---

## 8. Comandi Operativi

### Avvio del progetto (nativo)

\`\`\`bash
# Setup iniziale (rileva MySQL, crea database)
./scripts/setup.sh

# Avvia il backend
./scripts/start-backend.sh

# Avvia il frontend (in un altro terminale)
./scripts/start-frontend.sh
\`\`\`

### Avvio servizi infrastrutturali opzionali (Docker)

\`\`\`bash
# Avvia Qdrant
docker run -d --name localmind-qdrant -p 6333:6333 -p 6334:6334 \\
  -v localmind-qdrant-data:/qdrant/storage qdrant/qdrant:latest

# Avvia Ollama
docker run -d --name localmind-ollama -p 11434:11434 \\
  -v localmind-ollama-data:/root/.ollama ollama/ollama:latest

# Avvia n8n
docker run -d --name localmind-n8n -p 5678:5678 \\
  -v localmind-n8n-data:/home/node/.n8n \\
  -e N8N_BASIC_AUTH_ACTIVE=true \\
  -e N8N_BASIC_AUTH_USER=admin \\
  -e N8N_BASIC_AUTH_PASSWORD=localmind \\
  n8nio/n8n:latest
\`\`\`

### Download modelli Ollama

\`\`\`bash
# Nativo
ollama pull llama3.2
ollama pull nomic-embed-text
ollama list

# Docker
docker exec localmind-ollama ollama pull llama3.2
docker exec localmind-ollama ollama pull nomic-embed-text
docker exec localmind-ollama ollama list
\`\`\`

### Accesso al database MySQL

\`\`\`bash
# Nativo
mysql -u localmind -plocalmind localmind

# Docker
docker exec -it localmind-mysql mysql -u localmind -plocalmind localmind
\`\`\`

### Arresto servizi Docker (se avviati)

\`\`\`bash
# Arresta i singoli container
docker stop localmind-qdrant localmind-ollama localmind-n8n

# Rimuovi i container (mantiene i volumi)
docker rm localmind-qdrant localmind-ollama localmind-n8n
\`\`\`
`;
