@echo off
node "%~dp0scripts\bump-version.js" bump "%~dp0pom.xml" "%~dp0web\package.json" %1