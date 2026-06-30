@echo off
REM ============================================================
REM LocalMind - Setup MySQL Database
REM Crea il database 'localmind' e l'utente dedicato.
REM Richiede: MySQL 8.0+ e mysql.exe nel PATH.
REM ============================================================

set DB_NAME=localmind

REM --- Verifica client mysql ---
mysql --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERRORE: il comando 'mysql' non e' nel PATH.
    echo.
    echo Installa il client MySQL o aggiungi la cartella bin di MySQL al PATH.
    echo Esempio: C:\Program Files\MySQL\MySQL Server 8.0\bin
    pause
    exit /b 1
)

echo === LocalMind - Setup MySQL ===
echo.

REM --- Credenziali root ---
set /p MYSQL_ROOT_USER="Utente root MySQL [root]: "
if "%MYSQL_ROOT_USER%"=="" set MYSQL_ROOT_USER=root

set /p ROOT_PASS="Password per '%MYSQL_ROOT_USER%': "

set /p DB_HOST="Host MySQL [localhost]: "
if "%DB_HOST%"=="" set DB_HOST=localhost

set /p DB_PORT="Porta MySQL [3306]: "
if "%DB_PORT%"=="" set DB_PORT=3306

echo.

REM --- Verifica connessione root ---
echo Verifica connessione a MySQL...
mysql -h %DB_HOST% -P %DB_PORT% -u %MYSQL_ROOT_USER% -p%ROOT_PASS% -e "SELECT 1;" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERRORE: impossibile connettersi a MySQL su %DB_HOST%:%DB_PORT%.
    pause
    exit /b 1
)
echo Connessione OK.
echo.

REM --- Creazione database ---
echo Creazione database '%DB_NAME%'...
mysql -h %DB_HOST% -P %DB_PORT% -u %MYSQL_ROOT_USER% -p%ROOT_PASS% -e "CREATE DATABASE IF NOT EXISTS `%DB_NAME%` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo Database '%DB_NAME%' creato.
echo.

REM --- Utente applicativo ---
set /p USER_CHOICE="Vuoi creare un nuovo utente o usarne uno esistente? [nuovo/esistente]: "

if /i "%USER_CHOICE%"=="esistente" (
    set /p DB_USER="Nome utente esistente: "
    set /p DB_PASS="Password dell'utente: "

    echo Assegnazione privilegi su '%DB_NAME%'...
    mysql -h %DB_HOST% -P %DB_PORT% -u %MYSQL_ROOT_USER% -p%ROOT_PASS% -e "GRANT ALL PRIVILEGES ON `%DB_NAME%`.* TO '%DB_USER%'@'%%'; FLUSH PRIVILEGES;"
) else (
    set /p DB_USER="Nome del nuovo utente [localmind]: "
    if "%DB_USER%"=="" set DB_USER=localmind

    set /p DB_PASS="Password per il nuovo utente: "
    if "%DB_PASS%"=="" (
        echo ERRORE: la password non puo' essere vuota.
        pause
        exit /b 1
    )

    echo Creazione utente e assegnazione privilegi...
    mysql -h %DB_HOST% -P %DB_PORT% -u %MYSQL_ROOT_USER% -p%ROOT_PASS% -e "CREATE USER IF NOT EXISTS '%DB_USER%'@'localhost' IDENTIFIED BY '%DB_PASS%'; CREATE USER IF NOT EXISTS '%DB_USER%'@'%%' IDENTIFIED BY '%DB_PASS%'; GRANT ALL PRIVILEGES ON `%DB_NAME%`.* TO '%DB_USER%'@'localhost'; GRANT ALL PRIVILEGES ON `%DB_NAME%`.* TO '%DB_USER%'@'%%'; FLUSH PRIVILEGES;"
)

echo.

REM --- Test connessione ---
echo Test connessione con utente '%DB_USER%'...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% -e "SELECT 'Connessione OK!' AS status;" %DB_NAME%
if %ERRORLEVEL% NEQ 0 (
    echo ATTENZIONE: il test di connessione e' fallito.
    pause
    exit /b 1
)

echo.
echo === Setup completato ===
echo.
echo Riepilogo:
echo   Host:     %DB_HOST%:%DB_PORT%
echo   Database: %DB_NAME%
echo   Utente:   %DB_USER%
echo.
echo Aggiorna application-dev.yml se le credenziali sono diverse dai default.
pause
