@echo off
cd /d "%~dp0.."
node scripts/bump-version.js bump pom.xml web/package.json %1