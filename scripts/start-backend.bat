@echo off
REM ============================================================
REM LocalMind - Avvia Backend
REM Compila e avvia il backend Spring Boot con profilo dev.
REM Richiede: Java 17+, Maven 3.9+
REM ============================================================

set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..
set BACKEND_DIR=%PROJECT_ROOT%\localmind-backend

echo === LocalMind - Avvio Backend ===
echo Directory: %BACKEND_DIR%
echo.

cd /d "%BACKEND_DIR%"

REM Verifica Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERRORE: Java non trovato. Installare Java 17+.
    pause
    exit /b 1
)

REM Verifica Maven
mvn -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERRORE: Maven non trovato. Installare Maven 3.9+.
    pause
    exit /b 1
)

echo Avvio Spring Boot (profilo: dev)...
echo.

mvn -pl localmind-app -am spring-boot:run -Dspring-boot.run.profiles=dev
