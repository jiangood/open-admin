@echo off
chcp 65001 >nul
echo ========================================
echo open-admin local build ^& run script
echo ========================================

echo [0/4] Checking port 3000...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :3000 ^| findstr LISTEN') do (
    echo Port 3000 is in use by PID %%a, stopping...
    taskkill /f /pid %%a >nul 2>&1
    if errorlevel 1 (
        echo Failed to stop process %%a
    ) else (
        echo Process %%a stopped.
    )
    timeout /t 2 /nobreak >nul
)

echo [1/4] Building backend JAR...
call .\mvnw clean package -DskipTests -Papp -B -q
if errorlevel 1 (
    echo Backend build failed!
    exit /b 1
)

echo [2/4] Building frontend...
cd web
call npm run build
if errorlevel 1 (
    echo Frontend build failed!
    exit /b 1
)
cd ..

echo [3/4] Copying frontend static files...
if exist target\static rmdir /s /q target\static
xcopy /E /I /Q web\dist target\static >nul

echo [4/4] Starting app on port 3000...
echo.
echo Open http://localhost:3000
echo.
cd target
java -jar open-admin-2.0.1.jar --server.port=3000
