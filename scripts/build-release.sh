#!/usr/bin/env bash
# ==========================================================
# Build Release - Costruisce tutti gli artefatti di rilascio
# Build Release - Builds all release artifacts
# ==========================================================

set -euo pipefail

# Colori / Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
RELEASE_DIR="$PROJECT_ROOT/release"

# Versione dal parametro o dal tag git
# Version from parameter or git tag
VERSION="${1:-}"
if [ -z "$VERSION" ]; then
    VERSION=$(git -C "$PROJECT_ROOT" describe --tags --abbrev=0 2>/dev/null | sed 's/^v//' || echo "")
    if [ -z "$VERSION" ]; then
        echo -e "${RED}Errore: specificare la versione come parametro / Error: specify version as parameter${NC}"
        echo "Uso / Usage: $0 <version>"
        echo "Esempio / Example: $0 1.0.0"
        exit 1
    fi
fi

echo -e "${GREEN}=== Build Release v$VERSION ===${NC}"
echo ""

# Pulizia / Clean
rm -rf "$RELEASE_DIR"
mkdir -p "$RELEASE_DIR"

# ── 1. Backend JAR ──
echo -e "${YELLOW}[1/5] Building backend JAR...${NC}"
cd "$PROJECT_ROOT/localmind-backend"
mvn install -DskipTests -B -q
cp localmind-app/target/*.jar "$RELEASE_DIR/localmind-backend-$VERSION.jar"
echo -e "${GREEN}  Backend JAR creato / Backend JAR built${NC}"

# ── 2. SDK Java ──
echo -e "${YELLOW}[2/5] Building SDK Java...${NC}"
cd "$PROJECT_ROOT/localmind-sdk-java"
mvn package -B -q
cp target/localmind-sdk-java-*.jar "$RELEASE_DIR/localmind-sdk-java-$VERSION.jar"
echo -e "${GREEN}  SDK Java creato / SDK Java built${NC}"

# ── 3. Frontend ──
echo -e "${YELLOW}[3/5] Building frontend...${NC}"
cd "$PROJECT_ROOT/localmind-frontend"
npm ci --silent
npx ng build --configuration production
tar -czf "$RELEASE_DIR/localmind-frontend-$VERSION.tar.gz" -C dist/localmind-frontend/browser .
echo -e "${GREEN}  Frontend creato / Frontend built${NC}"

# ── 4. SDK JS ──
echo -e "${YELLOW}[4/5] Building SDK JS...${NC}"
cd "$PROJECT_ROOT/localmind-sdk-js"
npm ci --silent
npm run build
npm pack
mv *.tgz "$RELEASE_DIR/localmind-sdk-js-$VERSION.tgz"
echo -e "${GREEN}  SDK JS creato / SDK JS built${NC}"

# ── 5. SDK Python ──
echo -e "${YELLOW}[5/5] Building SDK Python...${NC}"
cd "$PROJECT_ROOT/localmind-sdk-python"
pip install build --quiet 2>/dev/null || pip install build
python -m build --quiet 2>/dev/null || python -m build
cp dist/*.tar.gz "$RELEASE_DIR/localmind-sdk-python-$VERSION.tar.gz"
cp dist/*.whl "$RELEASE_DIR/localmind-sdk-python-$VERSION.whl"
echo -e "${GREEN}  SDK Python creato / SDK Python built${NC}"

echo ""
echo -e "${GREEN}=== Release v$VERSION completata / Release v$VERSION complete ===${NC}"
echo ""
echo "Artefatti in / Artifacts in: $RELEASE_DIR/"
ls -lh "$RELEASE_DIR/"
echo ""
echo "Prossimi passi / Next steps:"
echo "  git tag v$VERSION"
echo "  git push origin v$VERSION"
