@echo off
chcp 65001 >nul
cd /d "%~dp0.."

echo Stopping backend (port 8080)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTEN') do (
    if not "%%a"=="0" (
        taskkill /f /pid %%a >nul 2>&1 && echo Killed backend process %%a
    )
)

echo Stopping frontend (port 3000)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :3000 ^| findstr LISTEN') do (
    if not "%%a"=="0" (
        taskkill /f /pid %%a >nul 2>&1 && echo Killed frontend process %%a
    )
)

echo All services stopped.
pause
