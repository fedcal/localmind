#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# LocalMind - Avvia tutto (Backend + Frontend)
# Avvia backend e frontend in processi paralleli.
# Usa Ctrl+C per fermare entrambi.
#
# LocalMind - Start all (Backend + Frontend)
# Starts backend and frontend in parallel processes.
# Use Ctrl+C to stop both.
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

# --- Colori / Colors ---
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo ""
echo -e "${BLUE}=== LocalMind - Avvio completo / Full startup ===${NC}"
echo ""

# --- Verifica servizi infra / Check infrastructure services ---
INFRA_RUNNING=false

if command -v docker &> /dev/null && docker compose ps --status running 2>/dev/null | grep -q "localmind-mysql"; then
    INFRA_RUNNING=true
    echo -e "${GREEN}[OK]${NC} Servizi Docker attivi (MySQL, Qdrant, Ollama) / Docker services running"
else
    echo -e "${YELLOW}[!]${NC} Servizi Docker non rilevati. / Docker services not detected."
    echo "    Avvia con: docker compose up -d"
    echo "    Start with: docker compose up -d"
    echo ""

    read -p "Vuoi avviarli ora? / Start them now? [S/n]: " START_DOCKER
    START_DOCKER="${START_DOCKER:-s}"

    if [[ "$START_DOCKER" =~ ^[sySY]$ ]]; then
        echo ""
        echo "Avvio servizi infra... / Starting infrastructure services..."
        cd "$PROJECT_DIR" && docker compose up -d
        echo ""

        # Attendi health check / Wait for health check
        echo "Attesa servizi... / Waiting for services..."
        sleep 10

        INFRA_RUNNING=true
        echo -e "${GREEN}[OK]${NC} Servizi avviati / Services started"
    else
        echo ""
        echo -e "${YELLOW}[!]${NC} Assicurati che MySQL, Qdrant e Ollama siano raggiungibili."
        echo "    Make sure MySQL, Qdrant and Ollama are reachable."
    fi
fi

echo ""
echo "  Backend  -> http://localhost:8080"
echo "  Frontend -> http://localhost:4200"
echo ""
echo "  Premi Ctrl+C per fermare tutto. / Press Ctrl+C to stop all."
echo ""

# Trap per terminare entrambi i processi / Trap to terminate both processes
cleanup() {
    echo ""
    echo "Arresto in corso... / Shutting down..."
    kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
    wait $BACKEND_PID $FRONTEND_PID 2>/dev/null
    echo "Tutti i servizi fermati. / All services stopped."
}
trap cleanup EXIT INT TERM

# Avvia backend in background / Start backend in background
"$SCRIPT_DIR/start-backend.sh" &
BACKEND_PID=$!

# Attendi qualche secondo per il backend / Wait a few seconds for backend
sleep 5

# Avvia frontend in background / Start frontend in background
"$SCRIPT_DIR/start-frontend.sh" &
FRONTEND_PID=$!

# Attendi entrambi / Wait for both
wait $BACKEND_PID $FRONTEND_PID
