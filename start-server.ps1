# Digital Health Repository - Startup Script (PowerShell)
# This script starts the API server with all required dependencies

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Digital Health Repository" -ForegroundColor Cyan
Write-Host "  Starting API Server..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"

# Build the project
Write-Host "[1/2] Building project..." -ForegroundColor Yellow
mvn clean package -DskipTests -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "[2/2] Starting server..." -ForegroundColor Yellow
Write-Host ""

# Run with maven exec to include all dependencies
mvn exec:java -Dexec.mainClass="com.digitalhealth.api.ApiServer"
