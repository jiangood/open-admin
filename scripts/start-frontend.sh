#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="${LOG_DIR:-$ROOT/logs}"
PID_FILE="$LOG_DIR/frontend.pid"
LOG_FILE="$LOG_DIR/frontend.log"

start() {
    if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
        echo "前端已在运行 (PID $(cat "$PID_FILE"))"
        exit 0
    fi

    if [[ ! -d "$ROOT/web/node_modules" ]]; then
        echo "安装前端依赖..."
        (cd "$ROOT/web" && npm install)
    fi

    echo "启动前端: npm run dev (端口 3000)"
    nohup npm run dev --prefix "$ROOT/web" > "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"
    echo "前端已启动 (PID $(cat "$PID_FILE"))，日志: $LOG_FILE"
}

stop() {
    if [[ ! -f "$PID_FILE" ]] || ! kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
        echo "前端未运行"
        return
    fi
    kill "$(cat "$PID_FILE")"
    rm -f "$PID_FILE"
    echo "前端已停止"
}

status() {
    if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
        echo "前端运行中 (PID $(cat "$PID_FILE"))"
    else
        echo "前端未运行"
    fi
}

case "${1:-start}" in
    start) start ;;
    stop) stop ;;
    restart) stop; start ;;
    status) status ;;
    *) echo "用法: $0 {start|stop|restart|status}" ;;
esac