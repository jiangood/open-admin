@echo off
echo === Starting open-admin frontend ===
cd /d "%~dp0web"
npm run dev
pause
