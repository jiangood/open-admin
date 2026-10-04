#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="${LOG_DIR:-$ROOT/logs}"
PID_FILE="$LOG_DIR/backend.pid"
LOG_FILE="$LOG_DIR/backend.log"

mkdir -p "$LOG_DIR"

start() {
    if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
        echo "后端已在运行 (PID $(cat "$PID_FILE"))"
        exit 0
    fi

    echo "启动后端: mvn -Pdev spring-boot:run"
    nohup mvn -Pdev spring-boot:run > "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"
    echo "后端已启动 (PID $(cat "$PID_FILE"))，日志: $LOG_FILE"
}

stop() {
    if [[ ! -f "$PID_FILE" ]] || ! kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
        echo "后端未运行"
        return
    fi
    kill "$(cat "$PID_FILE")"
    rm -f "$PID_FILE"
    echo "后端已停止"
}

status() {
    if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
        echo "后端运行中 (PID $(cat "$PID_FILE"))"
    else
        echo "后端未运行"
    fi
}

case "${1:-start}" in
    start) start ;;
    stop) stop ;;
    restart) stop; start ;;
    status) status ;;
    *) echo "用法: $0 {start|stop|restart|status}" ;;
esac