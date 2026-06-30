export const content = `# Script di Setup e Avvio

|               |                                        |
|---------------|----------------------------------------|
| **Documento** | Documentazione Script di Setup e Avvio |
| **Versione**  | 0.1.0                                  |
| **Data**      | 2026-02-09                             |
| **Progetto**  | LocalMind                              |

---

## Indice

1. [Panoramica](#1-panoramica)
2. [Prerequisiti](#2-prerequisiti)
3. [Script Disponibili](#3-script-disponibili)
   - 3.1 [setup-mysql (.sh / .bat)](#31-setup-mysql-sh--bat)
   - 3.2 [start-backend (.sh / .bat)](#32-start-backend-sh--bat)
   - 3.3 [start-frontend (.sh / .bat)](#33-start-frontend-sh--bat)
   - 3.4 [start-all (.sh / .bat)](#34-start-all-sh--bat)
4. [Flusso di Primo Avvio](#4-flusso-di-primo-avvio)
5. [Configurazione Database](#5-configurazione-database)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Panoramica

La cartella \`scripts/\` contiene script per il setup e l'avvio di LocalMind, disponibili sia per **Linux/macOS** (\`.sh\`) che per **Windows** (\`.bat\`).

\`\`\`
scripts/
├── setup-mysql.sh       # Setup database MySQL (Linux/macOS)
├── setup-mysql.bat      # Setup database MySQL (Windows)
├── start-backend.sh     # Avvio backend Spring Boot (Linux/macOS)
├── start-backend.bat    # Avvio backend Spring Boot (Windows)
├── start-frontend.sh    # Avvio frontend Angular (Linux/macOS)
├── start-frontend.bat   # Avvio frontend Angular (Windows)
├── start-all.sh         # Avvio backend + frontend (Linux/macOS)
└── start-all.bat        # Avvio backend + frontend (Windows)
\`\`\`

### Rendere eseguibili gli script (Linux/macOS)

\`\`\`bash
chmod +x scripts/*.sh
\`\`\`

Su Windows i file \`.bat\` sono eseguibili di default.

---

## 2. Prerequisiti

| Software     | Versione minima | Verifica           | Necessario per   |
|--------------|-----------------|--------------------|------------------|
| **Java JDK** | 17+             | \`java -version\`    | Backend          |
| **Maven**    | 3.9+            | \`mvn -version\`     | Backend          |
| **Node.js**  | 22+             | \`node -v\`          | Frontend         |
| **npm**      | 10+             | \`npm -v\`           | Frontend         |
| **MySQL**    | 8.0+            | \`mysql --version\`  | Database         |
| **Docker**   | 24+ (opzionale) | \`docker --version\` | MySQL via Docker |

**Nota:** MySQL puo' essere installato nativamente oppure eseguito in un container Docker. Lo script \`setup-mysql\` rileva automaticamente entrambe le modalita'.

---

## 3. Script Disponibili

### 3.1 setup-mysql (.sh / .bat)

Crea il database \`localmind\` e configura l'utente applicativo.

#### Esecuzione

\`\`\`bash
# Linux/macOS
./scripts/setup-mysql.sh

# Windows
scripts\\setup-mysql.bat
\`\`\`

#### Flusso interattivo

Lo script guida l'utente attraverso i seguenti step:

\`\`\`
1. Rilevamento MySQL
   ├── Client MySQL locale (nativo)
   ├── Container Docker con MySQL
   └── Entrambi → chiede quale usare

2. Credenziali amministratore
   ├── Utente root MySQL (default: root)
   ├── Password root
   ├── Host (solo se locale, default: 127.0.0.1)
   └── Porta (solo se locale, default: 3306)

3. Verifica connessione root

4. Creazione database 'localmind'
   └── charset: utf8mb4, collation: utf8mb4_unicode_ci

5. Configurazione utente applicativo
   ├── Opzione "nuovo"     → crea utente + password + grant
   └── Opzione "esistente" → solo grant privilegi

6. Test connessione con utente applicativo

7. Riepilogo configurazione
\`\`\`

#### Rilevamento automatico MySQL

Lo script verifica automaticamente:

| Controllo                                 | Descrizione                                         |
|-------------------------------------------|-----------------------------------------------------|
| \`command -v mysql\`                        | Verifica se il client MySQL nativo e' nel PATH      |
| \`docker ps --filter "ancestor=mysql"\`     | Cerca container Docker basati sull'immagine \`mysql\` |
| \`docker exec <container> mysql --version\` | Verifica che il container abbia il client MySQL     |

Se rileva MySQL in Docker, usa \`docker exec -i <container> mysql\` per eseguire i comandi SQL.

#### Esempio di output (Docker)

\`\`\`
=== LocalMind - Setup MySQL ===

MySQL rilevato in Docker (container: mysql-db-root).

Utente root MySQL [root]:
Password per 'root':

Verifica connessione a MySQL...
Connessione OK.

Creazione database 'localmind'...
Database 'localmind' creato.

Vuoi creare un nuovo utente o usarne uno esistente? [nuovo/esistente]: esistente
Nome utente esistente: root
Password dell'utente 'root':
Assegnazione privilegi su 'localmind' all'utente 'root'...

Test connessione con utente 'root'...
+----------------+
| status         |
+----------------+
| Connessione OK!|
+----------------+

=== Setup completato ===

Riepilogo:
  Modalita': Docker (container: mysql-db-root)
  Host app:  localhost:3306 (porta esposta dal container)
  Database:  localmind
  Utente:    root
\`\`\`

#### Configurazione risultante

Dopo l'esecuzione, verificare che \`application-dev.yml\` rifletta le credenziali scelte:

\`\`\`yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/localmind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: <utente scelto>
    password: <password scelta>
\`\`\`

---

### 3.2 start-backend (.sh / .bat)

Compila e avvia il backend Spring Boot con profilo \`dev\`.

#### Esecuzione

\`\`\`bash
# Linux/macOS
./scripts/start-backend.sh

# Windows
scripts\\start-backend.bat
\`\`\`

#### Cosa fa lo script

| Step              | Comando                                                                | Descrizione                                                         |
|-------------------|------------------------------------------------------------------------|---------------------------------------------------------------------|
| 1. Verifica Java  | \`java -version\`                                                        | Controlla che Java 17+ sia installato                               |
| 2. Verifica Maven | \`mvn -version\`                                                         | Controlla che Maven sia installato                                  |
| 3. Compilazione   | \`mvn install -DskipTests -q\`                                           | Compila tutti i moduli e installa i JAR nel repository Maven locale |
| 4. Avvio          | \`mvn -pl localmind-app spring-boot:run -Dspring-boot.run.profiles=dev\` | Avvia il modulo \`localmind-app\` con profilo \`dev\`                   |

#### Profilo dev

Il profilo \`dev\` (definito in \`application-dev.yml\`) configura:

| Parametro   | Valore                                  |
|-------------|-----------------------------------------|
| Database    | \`jdbc:mysql://localhost:3306/localmind\` |
| Ollama      | \`http://localhost:11434\`                |
| Qdrant      | \`localhost:6334\`                        |
| n8n         | \`http://localhost:5678\`                 |
| Log level   | \`DEBUG\` per \`com.localmind\`             |
| SQL logging | Abilitato con formattazione             |

#### Output atteso

\`\`\`
=== LocalMind - Avvio Backend ===
Directory: /path/to/localmind/localmind-backend

Java version: 21
Compilazione e installazione moduli...

Avvio Spring Boot (profilo: dev)...

  .   ____          _            __ _ _
 /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\
( ( )\\___ | '_ | '_| | '_ \\/ _\` | \\ \\ \\ \\
 \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.4.2)

...
Tomcat started on port 8080 (http)
Started LocalMindApplication in X.XXX seconds
\`\`\`

#### Verifica

\`\`\`bash
curl http://localhost:8080/actuator/health
\`\`\`

---

### 3.3 start-frontend (.sh / .bat)

Installa le dipendenze e avvia il dev server Angular.

#### Esecuzione

\`\`\`bash
# Linux/macOS
./scripts/start-frontend.sh

# Windows
scripts\\start-frontend.bat
\`\`\`

#### Cosa fa lo script

| Step                   | Comando                           | Descrizione                              |
|------------------------|-----------------------------------|------------------------------------------|
| 1. Verifica Node.js    | \`node -v\`                         | Controlla che Node.js 22+ sia installato |
| 2. Installa dipendenze | \`npm ci\`                          | Solo se \`node_modules/\` non esiste       |
| 3. Avvio               | \`npm start\` (alias di \`ng serve\`) | Avvia il dev server Angular              |

#### Output atteso

\`\`\`
=== LocalMind - Avvio Frontend ===
Directory: /path/to/localmind/localmind-frontend

Node.js version: v22.x.x
Avvio Angular dev server su http://localhost:4200 ...

** Angular Live Development Server is listening on localhost:4200 **

✔ Compiled successfully.
\`\`\`

#### Funzionalita' del dev server

| Funzionalita'              | Descrizione                                                          |
|----------------------------|----------------------------------------------------------------------|
| **Live reload**            | Ricarica automatica del browser ad ogni modifica dei file sorgente   |
| **Hot Module Replacement** | Aggiornamento parziale senza ricaricamento completo della pagina     |
| **Source maps**            | Mapping tra codice compilato e sorgente per il debugging nel browser |

#### Verifica

Aprire **http://localhost:4200** nel browser.

---

### 3.4 start-all (.sh / .bat)

Avvia backend e frontend insieme.

#### Esecuzione

\`\`\`bash
# Linux/macOS
./scripts/start-all.sh

# Windows
scripts\\start-all.bat
\`\`\`

#### Comportamento per piattaforma

| Piattaforma     | Comportamento                                                           | Come fermare                 |
|-----------------|-------------------------------------------------------------------------|------------------------------|
| **Linux/macOS** | Avvia backend e frontend come processi paralleli nello stesso terminale | \`Ctrl+C\` ferma entrambi      |
| **Windows**     | Apre backend e frontend in **finestre cmd separate**                    | Chiudere le singole finestre |

#### Flusso di avvio

\`\`\`
1. Avvia start-backend.sh/bat
2. Attende 5 secondi (tempo per la compilazione iniziale)
3. Avvia start-frontend.sh/bat
4. Entrambi i processi restano attivi
\`\`\`

#### Porte risultanti

| Servizio    | URL                   |
|-------------|-----------------------|
| Backend API | http://localhost:8080 |
| Frontend UI | http://localhost:4200 |

---

## 4. Flusso di Primo Avvio

Sequenza completa per avviare LocalMind dalla prima volta:

\`\`\`
Step 1: Assicurarsi che MySQL sia attivo
        (nativo o Docker)
          │
Step 2: ./scripts/setup-mysql.sh
        Crea database 'localmind'
        Configura utente
          │
Step 3: Verificare application-dev.yml
        Le credenziali devono corrispondere
          │
Step 4: Avviare servizi esterni (opzionali)
        - Ollama: ollama serve
        - Qdrant: docker run qdrant/qdrant
        - n8n:    n8n start
          │
Step 5: ./scripts/start-all.sh
        Oppure avviare backend e frontend separatamente
          │
Step 6: Aprire http://localhost:4200
\`\`\`

### Avvii successivi

Dopo il primo setup, e' sufficiente:

\`\`\`bash
# Assicurarsi che MySQL sia attivo, poi:
./scripts/start-all.sh
\`\`\`

---

## 5. Configurazione Database

### File di configurazione Spring Boot

| File                   | Ambiente   | Utilizzo                               |
|------------------------|------------|----------------------------------------|
| \`application.yml\`      | Base       | Profilo attivo, porta server           |
| \`application-dev.yml\`  | Sviluppo   | Credenziali localhost, logging DEBUG   |
| \`application-prod.yml\` | Produzione | Credenziali via env vars, logging WARN |

### Parametri di connessione MySQL

\`\`\`yaml
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
\`\`\`

| Parametro URL JDBC             | Descrizione                                                   |
|--------------------------------|---------------------------------------------------------------|
| \`useSSL=false\`                 | Disabilita SSL (sviluppo locale)                              |
| \`allowPublicKeyRetrieval=true\` | Consente il recupero della chiave pubblica per autenticazione |
| \`serverTimezone=UTC\`           | Imposta il timezone del server a UTC                          |

### Flyway Migrations

Le tabelle vengono create automaticamente da Flyway al primo avvio del backend:

| Migration | Tabella                          | Descrizione                        |
|-----------|----------------------------------|------------------------------------|
| \`V1\`      | \`documents\`                      | Documenti caricati e indicizzati   |
| \`V2\`      | \`folder_configs\`                 | Configurazioni cartelle monitorate |
| \`V3\`      | \`llm_usage\`                      | Tracking utilizzo e costi LLM      |
| \`V4\`      | \`conversations\`, \`chat_messages\` | Conversazioni chat e messaggi      |
| \`V5\`      | \`webhooks\`                       | Configurazioni webhook per eventi  |
| \`V6\`      | \`mcp_servers\`                    | Registrazioni server MCP           |

---

## 6. Troubleshooting

### 6.1 mysql: comando non trovato

**Causa:** Il client MySQL non e' installato o non e' nel PATH.

**Soluzione - Client locale:**
\`\`\`bash
# Ubuntu/Debian
sudo apt-get install -y mysql-client

# Fedora/RHEL
sudo dnf install -y mysql

# macOS
brew install mysql-client

# Arch
sudo pacman -S mariadb-clients
\`\`\`

**Soluzione - MySQL in Docker:**
Lo script rileva automaticamente MySQL in Docker. Se il container e' attivo, usa \`docker exec\` senza bisogno del client locale.

### 6.2 Impossibile connettersi a MySQL

**Causa 1:** MySQL non e' attivo.
\`\`\`bash
# Verifica MySQL nativo
sudo systemctl status mysql

# Verifica MySQL in Docker
docker ps | grep mysql
\`\`\`

**Causa 2:** Usando \`localhost\` invece di \`127.0.0.1\` (MySQL Docker).
Quando MySQL gira in Docker, \`localhost\` usa il socket Unix che non e' disponibile. Lo script rileva automaticamente Docker e usa \`docker exec\` per connettersi.

**Causa 3:** Password root errata.
Verificare le variabili d'ambiente del container Docker:
\`\`\`bash
docker inspect <container> --format '{{.Config.Env}}' | tr ' ' '\\n' | grep MYSQL
\`\`\`

### 6.3 BUILD FAILURE: Unable to find a suitable main class

**Causa:** Lo script tenta di eseguire \`spring-boot:run\` sul modulo parent (pom).

**Soluzione:** Lo script e' stato corretto per eseguire \`mvn install -DskipTests\` (compila tutti i moduli) e poi \`mvn -pl localmind-app spring-boot:run\` (avvia solo il modulo app).

### 6.4 Could not resolve dependencies

**Causa:** I moduli interni non sono installati nel repository Maven locale.

**Soluzione:** Lo script esegue \`mvn install -DskipTests\` che installa tutti i JAR. Se il problema persiste:
\`\`\`bash
cd localmind-backend
mvn clean install -DskipTests
\`\`\`

### 6.5 Angular: node_modules non trovato

**Causa:** Dipendenze npm non installate.

**Soluzione:** Lo script \`start-frontend\` esegue automaticamente \`npm ci\` se \`node_modules/\` non esiste. Per forzare la reinstallazione:
\`\`\`bash
cd localmind-frontend
rm -rf node_modules
npm ci
\`\`\`

### 6.6 Porta gia' in uso

**Causa:** Un altro processo occupa la porta 8080 (backend) o 4200 (frontend).

**Soluzione:**
\`\`\`bash
# Identificare il processo
sudo lsof -i :8080
sudo lsof -i :4200

# Terminare il processo
kill <PID>
\`\`\`

### 6.7 Java version troppo vecchia

**Causa:** Lo script verifica che Java sia >= 17. Se hai una versione precedente:

**Soluzione:**
\`\`\`bash
# Con SDKMAN (consigliato)
sdk install java 17.0.11-tem
sdk use java 17.0.11-tem

# Con apt (Ubuntu)
sudo apt-get install -y openjdk-17-jdk
sudo update-alternatives --config java
\`\`\`
`;
