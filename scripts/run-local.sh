#!/bin/bash
set -e
cd "$(dirname "$0")/.."

VERSION=$(node scripts/bump-version.js pom pom.xml)

echo "========================================"
echo "open-admin $VERSION local build & run"
echo "========================================"

echo "[0/4] Checking port 3000..."
if command -v lsof &>/dev/null; then
  PID=$(lsof -ti:3000 2>/dev/null || true)
  if [ -n "$PID" ]; then
    echo "Port 3000 is in use by PID $PID, stopping..."
    kill $PID 2>/dev/null || true
    sleep 2
  fi
fi

echo "[1/4] Building backend JAR..."
./mvnw clean package -DskipTests -Papp -B -q

echo "[2/4] Building frontend..."
cd web
npm run build
cd ..

echo "[3/4] Copying frontend static files..."
rm -rf target/static
cp -r web/dist target/static

echo "[4/4] Starting app on port 3000..."
echo ""
echo "Open http://localhost:3000"
echo ""
cd target
java -jar open-admin-$VERSION.jar --server.port=3000