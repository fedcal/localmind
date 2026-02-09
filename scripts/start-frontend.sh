#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# LocalMind - Avvia Frontend
# Installa dipendenze e avvia il dev server Angular.
# Richiede: Node.js 22+, npm
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
FRONTEND_DIR="$PROJECT_ROOT/localmind-frontend"

echo "=== LocalMind - Avvio Frontend ==="
echo "Directory: $FRONTEND_DIR"
echo ""

cd "$FRONTEND_DIR"

# Verifica prerequisiti
if ! command -v node &> /dev/null; then
    echo "ERRORE: Node.js non trovato. Installare Node.js 22+."
    exit 1
fi

NODE_MAJOR=$(node -v | cut -d'.' -f1 | tr -d 'v')
echo "Node.js version: $(node -v)"

if [ "$NODE_MAJOR" -lt 22 ]; then
    echo "ERRORE: Richiesto Node.js 22+, trovato v$NODE_MAJOR."
    exit 1
fi

# Installa dipendenze se necessario
if [ ! -d "node_modules" ]; then
    echo "Installazione dipendenze..."
    npm ci
    echo ""
fi

echo "Avvio Angular dev server su http://localhost:4200 ..."
echo ""

npm start
