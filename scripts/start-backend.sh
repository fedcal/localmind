#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# LocalMind - Avvia Backend
# Compila e avvia il backend Spring Boot con profilo dev.
# Richiede: Java 17+, Maven 3.9+
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$PROJECT_ROOT/localmind-backend"

echo "=== LocalMind - Avvio Backend ==="
echo "Directory: $BACKEND_DIR"
echo ""

cd "$BACKEND_DIR"

# Verifica prerequisiti
if ! command -v java &> /dev/null; then
    echo "ERRORE: Java non trovato. Installare Java 17+."
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "ERRORE: Maven non trovato. Installare Maven 3.9+."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "Java version: $JAVA_VERSION"

if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "ERRORE: Richiesto Java 17+, trovato Java $JAVA_VERSION."
    exit 1
fi

echo "Avvio Spring Boot (profilo: dev)..."
echo ""

mvn -pl localmind-app -am spring-boot:run -Dspring-boot.run.profiles=dev
