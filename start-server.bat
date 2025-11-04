@echo off
REM Digital Health Repository - Startup Script
REM This script starts the API server with all required dependencies

echo ========================================
echo   Digital Health Repository
echo   Starting API Server...
echo ========================================
echo.

REM Set JAVA_HOME if not already set
if "%JAVA_HOME%"=="" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25"
)

REM Build the project first
echo [1/2] Building project...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo [2/2] Starting server...
echo.

REM Run with maven exec to include all dependencies
call mvn exec:java -Dexec.mainClass="com.digitalhealth.api.ApiServer" -q

pause
