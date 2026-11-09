@echo off
title BeeAcademy Backend (Port 8080)
cd /d "%~dp0backend"
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo ===================================================
echo   Dang khoi dong BeeAcademy Backend tren cong 8080...
echo ===================================================
call mvnw.cmd spring-boot:run
pause

