@echo off
node "%~dp0bump-version.js" bump "%~dp0pom.xml" "%~dp0web\package.json" %1