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

REM Carica variabili d'ambiente da .env
if exist "%PROJECT_ROOT%\.env" (
    for /f "usebackq tokens=1,* delims==" %%a in ("%PROJECT_ROOT%\.env") do (
        set "%%a=%%b" 2>nul
    )
    echo Variabili caricate da .env
) else (
    echo ATTENZIONE: file .env non trovato.
    echo Copia .env.example in .env e configura le credenziali.
)
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

echo Compilazione e installazione moduli...
echo.

mvn install -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo ERRORE: compilazione fallita.
    pause
    exit /b 1
)

echo Avvio Spring Boot (profilo: dev)...
echo.

mvn -pl localmind-app spring-boot:run -Dspring-boot.run.profiles=dev
