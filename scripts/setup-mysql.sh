#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# LocalMind - Setup MySQL Database
# Crea il database 'localmind' e l'utente dedicato.
# Richiede: MySQL 8.0+ installato e il client mysql nel PATH.
# ============================================================

DB_NAME="localmind"
DB_USER="localmind"
DB_PASS="localmind"
MYSQL_ROOT_USER="${MYSQL_ROOT_USER:-root}"

echo "=== LocalMind - Setup MySQL ==="
echo ""

read -sp "Inserisci la password di root MySQL: " ROOT_PASS
echo ""

echo "Creazione database e utente..."

mysql -u "$MYSQL_ROOT_USER" -p"$ROOT_PASS" <<EOF
CREATE DATABASE IF NOT EXISTS \`${DB_NAME}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';
CREATE USER IF NOT EXISTS '${DB_USER}'@'%' IDENTIFIED BY '${DB_PASS}';
GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'localhost';
GRANT ALL PRIVILEGES ON \`${DB_NAME}\`.* TO '${DB_USER}'@'%';
FLUSH PRIVILEGES;
EOF

echo ""
echo "Database '${DB_NAME}' creato con successo."
echo "Utente: ${DB_USER} / Password: ${DB_PASS}"
echo ""
echo "Connessione di test..."
mysql -u "$DB_USER" -p"$DB_PASS" -e "SELECT 'Connessione OK!' AS status;" "$DB_NAME"
echo "=== Setup completato ==="
