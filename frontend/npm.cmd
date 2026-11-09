@echo off
if "%1"=="run" if "%2"=="dev" (
    "D:\Vo_Van_Thanh_Dat\Check\node.exe" "%~dp0node_modules\vite\bin\vite.js" --port=3000 --host=0.0.0.0 --strictPort
    exit /b %errorlevel%
)
if "%1"=="dev" (
    "D:\Vo_Van_Thanh_Dat\Check\node.exe" "%~dp0node_modules\vite\bin\vite.js" --port=3000 --host=0.0.0.0 --strictPort
    exit /b %errorlevel%
)
"D:\Vo_Van_Thanh_Dat\Check\node.exe" "%~dp0node_modules\vite\bin\vite.js" %*

