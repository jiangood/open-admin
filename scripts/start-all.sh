#!/usr/bin/env bash
# 一键启动前后端（后台异步）
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

bash "$ROOT/scripts/start-backend.sh" start
bash "$ROOT/scripts/start-frontend.sh" start

echo
echo "前端: http://localhost:3000  后端: http://localhost:8080"
echo "日志: $ROOT/logs/"