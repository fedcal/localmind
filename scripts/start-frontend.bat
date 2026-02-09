@echo off
REM ============================================================
REM LocalMind - Avvia Frontend
REM Installa dipendenze e avvia il dev server Angular.
REM Richiede: Node.js 22+, npm
REM ============================================================

set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..
set FRONTEND_DIR=%PROJECT_ROOT%\localmind-frontend

echo === LocalMind - Avvio Frontend ===
echo Directory: %FRONTEND_DIR%
echo.

cd /d "%FRONTEND_DIR%"

REM Verifica Node.js
node -v >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERRORE: Node.js non trovato. Installare Node.js 22+.
    pause
    exit /b 1
)

REM Installa dipendenze se necessario
if not exist "node_modules" (
    echo Installazione dipendenze...
    npm ci
    echo.
)

echo Avvio Angular dev server su http://localhost:4200 ...
echo.

npm start
