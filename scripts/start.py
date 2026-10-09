#!/usr/bin/env python3
"""open-admin 前后端进程管理（跨平台，替代 start-*.sh / start-*.bat）

用法:
    python scripts/start.py [all|backend|frontend] [start|stop|restart|status]

    target 缺省为 all，action 缺省为 start。
    例:
        python scripts/start.py                 # 后台启动前后端
        python scripts/start.py backend start
        python scripts/start.py frontend status
        python scripts/start.py stop            # 停止前后端

日志落 logs/{backend,frontend}.log，PID 落 logs/{backend,frontend}.pid（logs/ 已被 gitignore）。
"""
import os
import signal
import shutil
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LOG_DIR = ROOT / "logs"
IS_WINDOWS = os.name == "nt"

for _stream in (sys.stdout, sys.stderr):
    try:
        _stream.reconfigure(errors="replace")
    except (AttributeError, ValueError):
        pass

TARGETS = {
    "backend": {
        "label": "后端",
        "cmd": "mvn -Pdev spring-boot:run",
        "cwd": ROOT,
        "pid": LOG_DIR / "backend.pid",
        "log": LOG_DIR / "backend.log",
        "deps": ["mvn"],
    },
    "frontend": {
        "label": "前端",
        "cmd": "npm run dev",
        "cwd": ROOT / "web",
        "pid": LOG_DIR / "frontend.pid",
        "log": LOG_DIR / "frontend.log",
        "deps": ["npm"],
    },
}

TARGET_NAMES = ("all", "backend", "frontend")
ACTIONS = ("start", "stop", "restart", "status")


def read_pid(pid_file: Path):
    try:
        text = pid_file.read_text(encoding="utf-8").strip()
    except OSError:
        return None
    return int(text) if text.isdigit() else None


def is_running(pid) -> bool:
    if not pid:
        return False
    if IS_WINDOWS:
        try:
            out = subprocess.run(
                ["tasklist", "/FI", f"PID eq {pid}", "/NH"],
                capture_output=True,
                text=True,
                errors="replace",
            ).stdout
            return str(pid) in out
        except OSError:
            return False
    try:
        os.kill(pid, 0)
    except ProcessLookupError:
        return False
    except PermissionError:
        return True
    return True


def spawn_detached(cmd: str, cwd: Path, log_file: Path) -> int:
    """后台启动命令，stdout+stderr 重定向到 log_file，返回新进程 PID。"""
    LOG_DIR.mkdir(parents=True, exist_ok=True)
    log_f = open(log_file, "a", encoding="utf-8", errors="replace")
    try:
        if IS_WINDOWS:
            flags = getattr(subprocess, "CREATE_NEW_PROCESS_GROUP", 0) | getattr(
                subprocess, "CREATE_NO_WINDOW", 0
            )
            proc = subprocess.Popen(
                ["cmd.exe", "/c", cmd],
                cwd=str(cwd),
                stdin=subprocess.DEVNULL,
                stdout=log_f,
                stderr=subprocess.STDOUT,
                creationflags=flags,
                close_fds=True,
            )
        else:
            proc = subprocess.Popen(
                cmd,
                cwd=str(cwd),
                stdin=subprocess.DEVNULL,
                stdout=log_f,
                stderr=subprocess.STDOUT,
                shell=True,
                start_new_session=True,
            )
    finally:
        log_f.close()
    return proc.pid


def start(key: str) -> int:
    t = TARGETS[key]
    pid = read_pid(t["pid"])
    if pid and is_running(pid):
        print(f"{t['label']}已在运行 (PID {pid})")
        return 0

    for dep in t["deps"]:
        if shutil.which(dep) is None:
            print(f"❌ 缺少依赖: {dep}")
            return 1

    if key == "frontend" and not (t["cwd"] / "node_modules").exists():
        print("安装前端依赖...")
        if subprocess.call("npm install", cwd=str(t["cwd"]), shell=True) != 0:
            print("❌ npm install 失败")
            return 1

    print(f"启动{t['label']}: {t['cmd']}")
    new_pid = spawn_detached(t["cmd"], t["cwd"], t["log"])
    if not new_pid:
        print(f"❌ 启动失败，请查看 {t['log']}")
        return 1
    t["pid"].write_text(str(new_pid), encoding="utf-8")
    print(f"{t['label']}已启动 (PID {new_pid})，日志: {t['log']}")
    return 0


def stop(key: str) -> int:
    t = TARGETS[key]
    pid = read_pid(t["pid"])
    if not pid or not is_running(pid):
        print(f"{t['label']}未运行")
        t["pid"].unlink(missing_ok=True)
        return 0

    if IS_WINDOWS:
        subprocess.run(
            ["taskkill", "/PID", str(pid), "/T", "/F"],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
    else:
        pgid = None
        try:
            pgid = os.getpgid(pid)
            os.killpg(pgid, signal.SIGTERM)
        except ProcessLookupError:
            pass
        for _ in range(50):
            if not is_running(pid):
                break
            time.sleep(0.1)
        if is_running(pid) and pgid is not None:
            try:
                os.killpg(pgid, signal.SIGKILL)
            except ProcessLookupError:
                pass

    t["pid"].unlink(missing_ok=True)
    print(f"{t['label']}已停止")
    return 0


def status(key: str) -> int:
    t = TARGETS[key]
    pid = read_pid(t["pid"])
    if pid and is_running(pid):
        print(f"{t['label']}运行中 (PID {pid})")
    else:
        print(f"{t['label']}未运行")
    return 0


def usage() -> None:
    print("用法: python scripts/start.py [all|backend|frontend] [start|stop|restart|status]")
    print("      target 缺省 all，action 缺省 start")


def main(argv) -> int:
    target, action = "all", "start"
    for arg in argv:
        if arg in TARGET_NAMES:
            target = arg
        elif arg in ACTIONS:
            action = arg
        else:
            print(f"未知参数: {arg}")
            usage()
            return 1

    keys = ["backend", "frontend"] if target == "all" else [target]
    rc = 0
    for key in keys:
        if action == "start":
            rc |= start(key)
        elif action == "stop":
            rc |= stop(key)
        elif action == "status":
            rc |= status(key)
        elif action == "restart":
            stop(key)
            rc |= start(key)

    if target == "all" and action in ("start", "restart"):
        print()
        print("前端: http://localhost:3000  后端: http://localhost:8080")
        print(f"日志: {LOG_DIR}{os.sep}")
    return rc


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
