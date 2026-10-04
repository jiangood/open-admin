@echo off
setlocal
cd /d "%~dp0.."

set "LOG_DIR=%CD%\logs"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
set "PID_FILE=%LOG_DIR%\backend.pid"
set "LOG_FILE=%LOG_DIR%\backend.log"

set "ACTION=start"
if not "%~1"=="" set "ACTION=%~1"

if /I "%ACTION%"=="start" goto start
if /I "%ACTION%"=="stop" goto stop
if /I "%ACTION%"=="restart" goto restart
if /I "%ACTION%"=="status" goto status
echo 用法: %~nx0 {start^|stop^|restart^|status}
exit /b 1

:start
if exist "%PID_FILE%" goto start_check
goto do_start

:start_check
set /p RUNNING_PID=<"%PID_FILE%"
tasklist /FI "PID eq %RUNNING_PID%" 2>nul | find "%RUNNING_PID%" >nul
if not errorlevel 1 (
    echo 后端已在运行 (PID %RUNNING_PID%)
    exit /b 0
)

:do_start
echo 启动后端: mvn -Pdev spring-boot:run
powershell -NoProfile -Command "$p = Start-Process -FilePath 'cmd.exe' -ArgumentList @('/c','mvn -Pdev spring-boot:run 2>&1') -WorkingDirectory '%CD%' -WindowStyle Hidden -RedirectStandardOutput '%LOG_FILE%' -PassThru; Write-Output $p.Id" > "%PID_FILE%"
set /p NEW_PID=<"%PID_FILE%"
if not defined NEW_PID (
    echo 启动失败，请查看 %LOG_FILE%
    exit /b 1
)
echo 后端已启动 (PID %NEW_PID%)，日志: %LOG_FILE%
exit /b 0

:stop
if not exist "%PID_FILE%" goto stop_not_running
set /p RUNNING_PID=<"%PID_FILE%"
taskkill /PID %RUNNING_PID% /T /F >nul 2>&1
del /q "%PID_FILE%" >nul 2>&1
echo 后端已停止
exit /b 0

:stop_not_running
echo 后端未运行
exit /b 0

:restart
call :stop
call :start
exit /b 0

:status
if exist "%PID_FILE%" goto status_check
echo 后端未运行
exit /b 0

:status_check
set /p RUNNING_PID=<"%PID_FILE%"
tasklist /FI "PID eq %RUNNING_PID%" 2>nul | find "%RUNNING_PID%" >nul
if not errorlevel 1 (
    echo 后端运行中 (PID %RUNNING_PID%)
) else (
    echo 后端未运行
)
exit /b 0
