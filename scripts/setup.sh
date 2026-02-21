#!/usr/bin/env bash
set -euo pipefail

# ==========================================================
# LocalMind - Setup Iniziale / Initial Setup
#
# Questo script configura l'ambiente LocalMind:
# 1. Copia .env.example -> .env (se non esiste)
# 2. Avvia i servizi infra con Docker Compose
# 3. Attende che i servizi siano pronti
# 4. Configura il database MySQL
# 5. Scarica i modelli Ollama necessari
#
# This script sets up the LocalMind environment:
# 1. Copies .env.example -> .env (if not exists)
# 2. Starts infrastructure services with Docker Compose
# 3. Waits for services to be ready
# 4. Configures the MySQL database
# 5. Downloads required Ollama models
# ==========================================================

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SCRIPT_DIR="$PROJECT_DIR/scripts"

# --- Colori / Colors ---
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo ""
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""
}

print_ok() {
    echo -e "${GREEN}[OK]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[!]${NC} $1"
}

print_err() {
    echo -e "${RED}[ERRORE/ERROR]${NC} $1"
}

# ==========================================================
# Selezione lingua / Language selection
# ==========================================================
print_header "LocalMind - Setup"

echo "Seleziona lingua / Select language:"
echo "  1) Italiano"
echo "  2) English"
echo ""
read -p "Scelta/Choice [1]: " LANG_CHOICE
LANG_CHOICE="${LANG_CHOICE:-1}"

if [ "$LANG_CHOICE" = "2" ]; then
    LANG="en"
else
    LANG="it"
fi

# --- Messaggi bilingui / Bilingual messages ---
msg() {
    local key="$1"
    case "$key" in
        "prereq_check")
            [ "$LANG" = "it" ] && echo "Verifica prerequisiti..." || echo "Checking prerequisites...";;
        "docker_not_found")
            [ "$LANG" = "it" ] && echo "Docker non trovato. Installa Docker: https://docs.docker.com/get-docker/" || echo "Docker not found. Install Docker: https://docs.docker.com/get-docker/";;
        "docker_compose_not_found")
            [ "$LANG" = "it" ] && echo "Docker Compose non trovato. Installa Docker Compose v2." || echo "Docker Compose not found. Install Docker Compose v2.";;
        "docker_not_running")
            [ "$LANG" = "it" ] && echo "Docker non e' in esecuzione. Avvia Docker e riprova." || echo "Docker is not running. Start Docker and try again.";;
        "env_setup")
            [ "$LANG" = "it" ] && echo "Configurazione ambiente..." || echo "Setting up environment...";;
        "env_exists")
            [ "$LANG" = "it" ] && echo "File .env gia' presente." || echo ".env file already exists.";;
        "env_created")
            [ "$LANG" = "it" ] && echo "File .env creato da .env.example. Modificalo con le tue impostazioni." || echo ".env file created from .env.example. Edit it with your settings.";;
        "env_edit_prompt")
            [ "$LANG" = "it" ] && echo "Vuoi modificare .env ora? [s/N]: " || echo "Do you want to edit .env now? [y/N]: ";;
        "starting_infra")
            [ "$LANG" = "it" ] && echo "Avvio servizi infrastruttura (MySQL, Qdrant, Ollama)..." || echo "Starting infrastructure services (MySQL, Qdrant, Ollama)...";;
        "waiting_services")
            [ "$LANG" = "it" ] && echo "Attesa avvio servizi..." || echo "Waiting for services to start...";;
        "service_ready")
            [ "$LANG" = "it" ] && echo "pronto" || echo "ready";;
        "service_timeout")
            [ "$LANG" = "it" ] && echo "Timeout: il servizio non risponde dopo" || echo "Timeout: service not responding after";;
        "all_services_ready")
            [ "$LANG" = "it" ] && echo "Tutti i servizi infrastruttura sono pronti!" || echo "All infrastructure services are ready!";;
        "setup_db")
            [ "$LANG" = "it" ] && echo "Configurazione database MySQL..." || echo "Setting up MySQL database...";;
        "db_ready")
            [ "$LANG" = "it" ] && echo "Database MySQL configurato." || echo "MySQL database configured.";;
        "pulling_models")
            [ "$LANG" = "it" ] && echo "Download modelli Ollama..." || echo "Downloading Ollama models...";;
        "setup_complete")
            [ "$LANG" = "it" ] && echo "Setup completato con successo!" || echo "Setup completed successfully!";;
        "next_steps")
            [ "$LANG" = "it" ] && echo "Prossimi passi:" || echo "Next steps:";;
        "next_native")
            [ "$LANG" = "it" ] && echo "  Modalita' nativa (sviluppo):" || echo "  Native mode (development):";;
        "next_docker")
            [ "$LANG" = "it" ] && echo "  Modalita' Docker completa:" || echo "  Full Docker mode:";;
        "seconds")
            [ "$LANG" = "it" ] && echo "secondi" || echo "seconds";;
    esac
}

# ==========================================================
# STEP 1: Verifica prerequisiti / Check prerequisites
# ==========================================================
print_header "$(msg prereq_check)"

# Docker
if ! command -v docker &> /dev/null; then
    print_err "$(msg docker_not_found)"
    exit 1
fi
print_ok "Docker $(docker --version | cut -d' ' -f3 | tr -d ',')"

# Docker Compose v2
if ! docker compose version &> /dev/null; then
    print_err "$(msg docker_compose_not_found)"
    exit 1
fi
print_ok "Docker Compose $(docker compose version --short)"

# Docker daemon in esecuzione
if ! docker info &> /dev/null 2>&1; then
    print_err "$(msg docker_not_running)"
    exit 1
fi
print_ok "Docker daemon"

# ==========================================================
# STEP 2: Configurazione .env / Configure .env
# ==========================================================
print_header "$(msg env_setup)"

cd "$PROJECT_DIR"

if [ -f ".env" ]; then
    print_ok "$(msg env_exists)"
else
    cp .env.example .env
    print_ok "$(msg env_created)"

    echo ""
    read -p "$(msg env_edit_prompt)" EDIT_ENV
    EDIT_ENV="${EDIT_ENV:-n}"

    if [[ "$EDIT_ENV" =~ ^[sySY]$ ]]; then
        if command -v nano &> /dev/null; then
            nano .env
        elif command -v vim &> /dev/null; then
            vim .env
        else
            print_warn "Editor non trovato. Modifica .env manualmente. / Editor not found. Edit .env manually."
        fi
    fi
fi

# Carica variabili da .env / Load variables from .env
set -a
source .env
set +a

# ==========================================================
# STEP 3: Avvio servizi infra / Start infrastructure services
# ==========================================================
print_header "$(msg starting_infra)"

docker compose up -d

echo ""
echo "$(msg waiting_services)"
echo ""

# Funzione per attendere un servizio / Function to wait for a service
wait_for_service() {
    local service_name="$1"
    local max_wait="${2:-120}"
    local elapsed=0

    printf "  %-15s " "$service_name"

    while [ $elapsed -lt $max_wait ]; do
        if docker compose ps "$service_name" 2>/dev/null | grep -q "healthy"; then
            echo -e "${GREEN}$(msg service_ready)${NC} (${elapsed}s)"
            return 0
        fi
        sleep 2
        elapsed=$((elapsed + 2))
        printf "."
    done

    echo -e "${RED}$(msg service_timeout) ${max_wait} $(msg seconds)${NC}"
    return 1
}

wait_for_service "mysql" 120
wait_for_service "qdrant" 60
wait_for_service "ollama" 60

echo ""
print_ok "$(msg all_services_ready)"

# ==========================================================
# STEP 4: Configurazione database / Configure database
# ==========================================================
print_header "$(msg setup_db)"

# Attendi che MySQL accetti connessioni / Wait for MySQL to accept connections
sleep 3

# Crea database se non esiste (usa il container Docker)
# Create database if not exists (uses Docker container)
docker exec localmind-mysql mysql -h 127.0.0.1 \
    -u root -p"${DB_PASSWORD:-localmind}" \
    -e "CREATE DATABASE IF NOT EXISTS \`${DB_NAME:-localmind}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" \
    2>/dev/null

print_ok "$(msg db_ready)"

# ==========================================================
# STEP 5: Download modelli Ollama / Download Ollama models
# ==========================================================
print_header "$(msg pulling_models)"

"$SCRIPT_DIR/setup-ollama-models.sh"

# ==========================================================
# STEP 6: Riepilogo / Summary
# ==========================================================
print_header "$(msg setup_complete)"

echo "$(msg next_steps)"
echo ""
echo "$(msg next_native)"
echo "    ./scripts/start-all.sh"
echo ""
echo "$(msg next_docker)"
echo "    docker compose --profile full up -d --build"
echo ""
echo "  Frontend: http://localhost:4200"
echo "  API:      http://localhost:8080/api/v1"
echo "  Health:   http://localhost:8080/api/v1/dashboard/health"
echo ""
