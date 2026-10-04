@echo off
call "%~dp0start-backend.bat" start
call "%~dp0start-frontend.bat" start

echo.
echo 前端: http://localhost:3000  后端: http://localhost:8080
echo 日志: %~dp0..\logs\
