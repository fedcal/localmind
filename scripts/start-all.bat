@echo off
REM ============================================================
REM LocalMind - Avvia tutto (Backend + Frontend)
REM Apre backend e frontend in finestre separate.
REM Chiudi le finestre per fermare i servizi.
REM ============================================================

set SCRIPT_DIR=%~dp0

echo === LocalMind - Avvio completo ===
echo.
echo Backend  -^> http://localhost:8080
echo Frontend -^> http://localhost:4200
echo.

start "LocalMind Backend" cmd /c "%SCRIPT_DIR%start-backend.bat"

REM Attendi qualche secondo per il backend
timeout /t 5 /nobreak >nul

start "LocalMind Frontend" cmd /c "%SCRIPT_DIR%start-frontend.bat"

echo Servizi avviati in finestre separate.
echo Chiudi le finestre per fermare i servizi.
pause
