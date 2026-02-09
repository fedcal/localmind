#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# LocalMind - Avvia tutto (Backend + Frontend)
# Avvia backend e frontend in processi paralleli.
# Usa Ctrl+C per fermare entrambi.
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== LocalMind - Avvio completo ==="
echo ""
echo "Backend  -> http://localhost:8080"
echo "Frontend -> http://localhost:4200"
echo ""
echo "Premi Ctrl+C per fermare tutto."
echo ""

# Trap per terminare entrambi i processi
cleanup() {
    echo ""
    echo "Arresto in corso..."
    kill $BACKEND_PID $FRONTEND_PID 2>/dev/null
    wait $BACKEND_PID $FRONTEND_PID 2>/dev/null
    echo "Tutti i servizi fermati."
}
trap cleanup EXIT INT TERM

# Avvia backend in background
"$SCRIPT_DIR/start-backend.sh" &
BACKEND_PID=$!

# Attendi qualche secondo per il backend
sleep 5

# Avvia frontend in background
"$SCRIPT_DIR/start-frontend.sh" &
FRONTEND_PID=$!

# Attendi entrambi
wait $BACKEND_PID $FRONTEND_PID
