@echo off
REM ============================================================
REM LocalMind - Setup MySQL Database
REM Crea il database 'localmind' e l'utente dedicato.
REM Richiede: MySQL 8.0+ installato e mysql.exe nel PATH.
REM ============================================================

set DB_NAME=localmind
set DB_USER=localmind
set DB_PASS=localmind

echo === LocalMind - Setup MySQL ===
echo.

set /p ROOT_PASS="Inserisci la password di root MySQL: "

echo Creazione database e utente...

mysql -u root -p%ROOT_PASS% -e "CREATE DATABASE IF NOT EXISTS `%DB_NAME%` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if %ERRORLEVEL% NEQ 0 (
    echo ERRORE: impossibile creare il database.
    pause
    exit /b 1
)

mysql -u root -p%ROOT_PASS% -e "CREATE USER IF NOT EXISTS '%DB_USER%'@'localhost' IDENTIFIED BY '%DB_PASS%';"
mysql -u root -p%ROOT_PASS% -e "CREATE USER IF NOT EXISTS '%DB_USER%'@'%%' IDENTIFIED BY '%DB_PASS%';"
mysql -u root -p%ROOT_PASS% -e "GRANT ALL PRIVILEGES ON `%DB_NAME%`.* TO '%DB_USER%'@'localhost';"
mysql -u root -p%ROOT_PASS% -e "GRANT ALL PRIVILEGES ON `%DB_NAME%`.* TO '%DB_USER%'@'%%';"
mysql -u root -p%ROOT_PASS% -e "FLUSH PRIVILEGES;"

echo.
echo Database '%DB_NAME%' creato con successo.
echo Utente: %DB_USER% / Password: %DB_PASS%
echo.

echo Connessione di test...
mysql -u %DB_USER% -p%DB_PASS% -e "SELECT 'Connessione OK!' AS status;" %DB_NAME%
if %ERRORLEVEL% NEQ 0 (
    echo ERRORE: test di connessione fallito.
    pause
    exit /b 1
)

echo === Setup completato ===
pause
