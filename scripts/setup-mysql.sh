#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# LocalMind - Setup MySQL Database
# Crea il database 'localmind' e l'utente dedicato.
# Supporta MySQL nativo e MySQL in Docker.
# ============================================================

DB_NAME="localmind"
MYSQL_CMD=""
DOCKER_CONTAINER=""

echo "=== LocalMind - Setup MySQL ==="
echo ""

# --- Rilevamento modalita' MySQL ---
HAS_LOCAL_MYSQL=false
HAS_DOCKER_MYSQL=false

if command -v mysql &> /dev/null; then
    HAS_LOCAL_MYSQL=true
fi

if command -v docker &> /dev/null; then
    DOCKER_CONTAINER=$(docker ps --filter "ancestor=mysql" --filter "status=running" --format "{{.Names}}" 2>/dev/null | head -1)
    if [ -z "$DOCKER_CONTAINER" ]; then
        DOCKER_CONTAINER=$(docker ps --filter "status=running" --format "{{.Names}}" 2>/dev/null | while read name; do
            if docker exec "$name" mysql --version &>/dev/null 2>&1; then
                echo "$name"
                break
            fi
        done)
    fi
    if [ -n "$DOCKER_CONTAINER" ]; then
        HAS_DOCKER_MYSQL=true
    fi
fi

if [ "$HAS_LOCAL_MYSQL" = true ] && [ "$HAS_DOCKER_MYSQL" = true ]; then
    echo "Rilevati entrambi: client MySQL locale e container Docker '$DOCKER_CONTAINER'."
    read -p "Quale vuoi usare? [locale/docker]: " MODE
    MODE="${MODE:-docker}"
elif [ "$HAS_DOCKER_MYSQL" = true ]; then
    echo "MySQL rilevato in Docker (container: $DOCKER_CONTAINER)."
    MODE="docker"
elif [ "$HAS_LOCAL_MYSQL" = true ]; then
    echo "Client MySQL locale rilevato."
    MODE="locale"
else
    echo "ERRORE: MySQL non trovato."
    echo ""
    echo "Opzione 1 - Installa il client MySQL:"
    echo "  Ubuntu/Debian:  sudo apt-get install -y mysql-client"
    echo "  Fedora/RHEL:    sudo dnf install -y mysql"
    echo "  macOS:          brew install mysql-client"
    echo ""
    echo "Opzione 2 - Avvia MySQL in Docker:"
    echo "  docker run -d --name mysql-localmind -e MYSQL_ROOT_PASSWORD=password -p 3306:3306 mysql:8.0"
    exit 1
fi

# --- Funzione wrapper per eseguire comandi mysql ---
run_mysql() {
    if [ "$MODE" = "docker" ]; then
        # -h 127.0.0.1 forza TCP, evita problemi con auth_socket su localhost
        docker exec -i "$DOCKER_CONTAINER" mysql -h 127.0.0.1 "$@"
    else
        mysql -h "$DB_HOST" -P "$DB_PORT" "$@"
    fi
}

echo ""

# --- Credenziali root ---
read -p "Utente root MySQL [root]: " MYSQL_ROOT_USER
MYSQL_ROOT_USER="${MYSQL_ROOT_USER:-root}"

read -sp "Password per '$MYSQL_ROOT_USER': " ROOT_PASS
echo ""

if [ "$MODE" = "locale" ]; then
    read -p "Host MySQL [127.0.0.1]: " DB_HOST
    DB_HOST="${DB_HOST:-127.0.0.1}"

    read -p "Porta MySQL [3306]: " DB_PORT
    DB_PORT="${DB_PORT:-3306}"
fi

echo ""

# --- Verifica connessione root ---
echo "Verifica connessione a MySQL..."
if ! run_mysql -u "$MYSQL_ROOT_USER" -p"$ROOT_PASS" -e "SELECT 1;" &> /dev/null; then
    if [ "$MODE" = "docker" ]; then
        echo "ERRORE: impossibile connettersi a MySQL nel container '$DOCKER_CONTAINER'."
    else
        echo "ERRORE: impossibile connettersi a MySQL su $DB_HOST:$DB_PORT."
    fi
    exit 1
fi
echo "Connessione OK."
echo ""

# --- Creazione database ---
echo "Creazione database '$DB_NAME'..."
run_mysql -u "$MYSQL_ROOT_USER" -p"$ROOT_PASS" \
    -e "CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo "Database '$DB_NAME' creato."
echo ""

# --- Utente applicativo ---
read -p "Vuoi creare un nuovo utente o usarne uno esistente? [nuovo/esistente]: " USER_CHOICE

if [ "$USER_CHOICE" = "esistente" ]; then
    read -p "Nome utente esistente: " DB_USER
    read -sp "Password dell'utente '$DB_USER': " DB_PASS
    echo ""

    echo "Assegnazione privilegi su '$DB_NAME' all'utente '$DB_USER'..."
    run_mysql -u "$MYSQL_ROOT_USER" -p"$ROOT_PASS" <<EOF
GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'%';
FLUSH PRIVILEGES;
EOF

else
    read -p "Nome del nuovo utente [localmind]: " DB_USER
    DB_USER="${DB_USER:-localmind}"

    read -sp "Password per il nuovo utente '$DB_USER': " DB_PASS
    echo ""

    if [ -z "$DB_PASS" ]; then
        echo "ERRORE: la password non puo' essere vuota."
        exit 1
    fi

    echo "Creazione utente '$DB_USER' e assegnazione privilegi..."
    run_mysql -u "$MYSQL_ROOT_USER" -p"$ROOT_PASS" <<EOF
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';
CREATE USER IF NOT EXISTS '${DB_USER}'@'%' IDENTIFIED BY '${DB_PASS}';
GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'localhost';
GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'%';
FLUSH PRIVILEGES;
EOF
fi

echo ""

# --- Test connessione ---
echo "Test connessione con utente '$DB_USER'..."
if run_mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "SELECT 'Connessione OK!' AS status;"; then
    echo ""
    echo "=== Setup completato ==="
    echo ""
    echo "Riepilogo:"
    if [ "$MODE" = "docker" ]; then
        echo "  Modalita': Docker (container: $DOCKER_CONTAINER)"
        echo "  Host app:  localhost:3306 (porta esposta dal container)"
    else
        echo "  Host:      $DB_HOST:$DB_PORT"
    fi
    echo "  Database:  $DB_NAME"
    echo "  Utente:    $DB_USER"
    echo ""
    echo "Verifica che application-dev.yml abbia queste credenziali:"
    echo "  url: jdbc:mysql://localhost:3306/$DB_NAME?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    echo "  username: $DB_USER"
    echo "  password: <la password scelta>"
else
    echo ""
    echo "ATTENZIONE: il test di connessione e' fallito. Verifica le credenziali."
    exit 1
fi
