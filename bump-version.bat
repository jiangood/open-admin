@echo off
setlocal enabledelayedexpansion

if "%~1"=="" (
  echo Usage: bump-version.bat ^<version^>
  echo Example: bump-version.bat 2.3.0
  exit /b 1
)

set "VERSION=%~1"

echo %VERSION%| findstr /R "^[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*$" >nul
if errorlevel 1 (
  echo Error: Version must be in semver format ^(e.g., 2.3.0^)
  exit /b 1
)

set "POM_FILE=pom.xml"
set "PKG_FILE=web\package.json"

if not exist "%POM_FILE%" (
  echo Error: %POM_FILE% not found
  exit /b 1
)
if not exist "%PKG_FILE%" (
  echo Error: %PKG_FILE% not found
  exit /b 1
)

for /f "tokens=2 delims=<> usebackq" %%v in (`findstr /R "<version>[0-9]" "%POM_FILE%"`) do (
  set "CURRENT_POM=%%v"
  goto :pom_done
)
:pom_done

for /f "tokens=2 delims=:, usebackq" %%v in (`findstr /R "\"version\":" "%PKG_FILE%"`) do (
  set "RAW=%%v"
  goto :npm_done
)
:npm_done
set "CURRENT_NPM=%RAW: =%"
set "CURRENT_NPM=%CURRENT_NPM:"=%"

if not "%CURRENT_POM%"=="%CURRENT_NPM%" (
  echo Error: pom.xml ^(%CURRENT_POM%^) and package.json ^(%CURRENT_NPM%^) versions are out of sync
  exit /b 1
)

if "%CURRENT_POM%"=="%VERSION%" (
  echo Already at version %VERSION%, nothing to do
  exit /b 0
)

powershell -Command "(Get-Content '%POM_FILE%') -replace '<version>%CURRENT_POM%</version>', '<version>%VERSION%</version>' | Set-Content '%POM_FILE%'"
powershell -Command "(Get-Content '%PKG_FILE%') -replace '\"version\": \"%CURRENT_NPM%\"', '\"version\": \"%VERSION%\"' | Set-Content '%PKG_FILE%'"

echo Bumped version: %CURRENT_POM% -^> %VERSION%
echo   pom.xml:       %VERSION%
echo   package.json:  %VERSION%
