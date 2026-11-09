@echo off
title Khoi dong BeeAcademy (Fullstack)
echo ===================================================
echo   Dang khoi dong he thong BeeAcademy...
echo ===================================================
start "BeeAcademy Backend (8080)" "%~dp0start_backend.bat"
timeout /t 3 /nobreak >nul
start "BeeAcademy Frontend (3000)" "%~dp0start_frontend.bat"
echo.
echo ===================================================
echo   Da mo 2 cua so Backend va Frontend thanh cong!
echo   - Backend:  http://localhost:8080
echo   - Frontend: http://localhost:3000
echo ===================================================
timeout /t 5 >nul
