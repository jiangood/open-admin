@echo off
cd /d "%~dp0.."
cd web
call npm install
call npm run build