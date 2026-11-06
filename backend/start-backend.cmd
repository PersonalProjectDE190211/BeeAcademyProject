@echo off
title Bee Academy Backend
echo ============================================
echo   Bee Academy Backend - Spring Boot 3.2
echo ============================================
echo.

cd /d "%~dp0"

echo Starting backend on http://localhost:8080 ...
echo Press Ctrl+C to stop.
echo.

if exist "%~dp0mvnw.cmd" (
  echo Using bundled Maven Wrapper (mvnw.cmd)...
  call "%~dp0mvnw.cmd" spring-boot:run
) else (
  where mvn.cmd >nul 2>&1
  if errorlevel 1 (
    echo ERROR: Maven was not found in PATH and mvnw.cmd is missing.
    echo Install Maven or add its bin directory to PATH, then try again.
    pause
    exit /b 1
  )
  call mvn.cmd spring-boot:run
)

pause
