@echo off
cd /d "%~dp0.."

for /f "usebackq delims=" %%v in (`node scripts/bump-version.js npm web/package.json`) do set "NPM_VERSION=%%v"

echo Current npm version: %NPM_VERSION%

cd web
call npm version prerelease --preid=beta
call npm run build
call npm publish --access public --no-git-checks --tag beta --registry https://packages.aliyun.com/62d39be70065edd3d51c1984/npm/npm-registry/