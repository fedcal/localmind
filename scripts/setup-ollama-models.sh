#!/usr/bin/env bash
set -euo pipefail

# ==========================================================
# LocalMind - Setup Modelli Ollama / Ollama Models Setup
#
# Scarica i modelli necessari per LocalMind:
# - llama3.2 (chat)
# - nomic-embed-text (embedding per RAG)
#
# Downloads required models for LocalMind:
# - llama3.2 (chat)
# - nomic-embed-text (embedding for RAG)
#
# Supporta sia Ollama nativo che Docker.
# Supports both native and Docker Ollama.
# ==========================================================

# --- Colori / Colors ---
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Carica .env se presente / Load .env if present
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
if [ -f "$PROJECT_DIR/.env" ]; then
    set -a
    source "$PROJECT_DIR/.env"
    set +a
fi

CHAT_MODEL="${OLLAMA_CHAT_MODEL:-llama3.2}"
EMBED_MODEL="${OLLAMA_EMBED_MODEL:-nomic-embed-text}"

# --- Rileva modalita' Ollama / Detect Ollama mode ---
OLLAMA_CMD=""
OLLAMA_MODE=""

# Verifica se Ollama e' in Docker / Check if Ollama is in Docker
if docker ps --format '{{.Names}}' 2>/dev/null | grep -q "localmind-ollama"; then
    OLLAMA_MODE="docker"
    OLLAMA_CMD="docker exec localmind-ollama ollama"
    echo -e "${BLUE}Ollama rilevato in Docker (localmind-ollama)${NC}"
    echo -e "${BLUE}Ollama detected in Docker (localmind-ollama)${NC}"
elif command -v ollama &> /dev/null; then
    OLLAMA_MODE="native"
    OLLAMA_CMD="ollama"
    echo -e "${BLUE}Ollama rilevato nativo / Ollama detected natively${NC}"
else
    echo "Ollama non trovato. Avvia Docker Compose o installa Ollama."
    echo "Ollama not found. Start Docker Compose or install Ollama."
    echo "  https://ollama.com/download"
    exit 1
fi

echo ""

# --- Funzione per scaricare un modello / Function to download a model ---
pull_model() {
    local model="$1"
    local description="$2"

    echo -e "  ${BLUE}$model${NC} ($description)"

    # Verifica se il modello e' gia' scaricato / Check if model is already downloaded
    if $OLLAMA_CMD list 2>/dev/null | grep -q "^${model}"; then
        echo -e "    ${GREEN}[OK]${NC} Gia' presente / Already downloaded"
        return 0
    fi

    echo -e "    ${YELLOW}Download in corso... / Downloading...${NC}"
    if $OLLAMA_CMD pull "$model"; then
        echo -e "    ${GREEN}[OK]${NC} Completato / Completed"
    else
        echo "    [!] Download fallito. Riprova manualmente: / Download failed. Retry manually:"
        if [ "$OLLAMA_MODE" = "docker" ]; then
            echo "        docker exec localmind-ollama ollama pull $model"
        else
            echo "        ollama pull $model"
        fi
        return 1
    fi
}

echo "Modelli richiesti / Required models:"
echo ""

pull_model "$CHAT_MODEL" "chat"
echo ""
pull_model "$EMBED_MODEL" "embedding RAG"

echo ""
echo -e "${GREEN}Modelli Ollama pronti! / Ollama models ready!${NC}"
