@echo off
title BeeAcademy Frontend (Port 3000)
cd /d "%~dp0frontend"
set "PATH=D:\Vo_Van_Thanh_Dat\Check;%PATH%"
echo ===================================================
echo   Dang khoi dong BeeAcademy Frontend tren cong 3000...
echo ===================================================
call node node_modules\vite\bin\vite.js --port=3000 --host=0.0.0.0 --strictPort
pause

