#!/usr/bin/env python3
"""release.py - open-admin release helper (cross-platform)

用法:
    python scripts/release.py <version> [--skip-tests] [--no-push]
                              [--dry-run] [--no-rollback]
    python scripts/release.py status
    python scripts/release.py check

"run" 可省略: python scripts/release.py 3.1.3 与 python scripts/release.py run 3.1.3 等价。
版本号可带或可不带前缀 v；git tag 一律创建/推送为 v<version>，例如 v3.1.3。

选项:
    --skip-tests   跳过 "mvn -B clean test" 和 "npm run build"
    --no-push      只在本地 commit + tag，不推送到 origin
    --dry-run      只检查/打印计划，不修改任何仓库文件
    --no-rollback  失败时保留版本号改动

退出码:
    0 成功 / 1 用法或前置检查失败 / 2 测试失败 / 3 工作区不干净 / 4 git 失败 / 5 版本或 tag 冲突

说明:
    - 完整日志写入 logs/release-v<version>-<stamp>.log（UTF-8）
    - 白名单: 一次发版只允许修改 */pom.xml 与 web/package.json
"""
import json
import locale
import os
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET
from datetime import datetime
from pathlib import Path

# Windows cmd 默认使用本地代码页（如 GBK），子进程输出里的 emoji/特殊字符
# 可能无法编码；保留原编码仅把不可编码字符替换为 ?，避免 print 抛异常。
for _stream in (sys.stdout, sys.stderr):
    try:
        _stream.reconfigure(errors="replace")
    except (AttributeError, ValueError):
        pass

ROOT = Path(__file__).resolve().parent.parent
LOG_DIR = ROOT / "logs"
EXIT_OK = 0
EXIT_USAGE = 1
EXIT_TESTS = 2
EXIT_WORKSPACE = 3
EXIT_GIT = 4
EXIT_CONFLICT = 5

VERSION_RE = re.compile(r"^\d+\.\d+\.\d+$")
WHITELIST_RE = re.compile(r"^.. ((.*/)?pom\.xml|web/package\.json)$")


def _local(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def run_cmd(cmd, cwd=None, encoding="utf-8", errors="replace"):
    proc = subprocess.run(cmd, cwd=str(cwd or ROOT), capture_output=True)
    out = proc.stdout.decode(encoding, errors)
    err = proc.stderr.decode(encoding, errors)
    return proc.returncode, out, err


class Release:
    def __init__(self):
        self.version = None
        self.tag = None
        self.branch = ""
        self.pom_version = ""
        self.pkg_version = ""
        self.local_tag = ""
        self.remote_tag = ""
        self.remote_check_ok = False
        self.tag_local = False
        self.tag_remote = False
        self.tag_at_head = False
        self.versions_at_target = False
        self.dirty_lines = []
        self.ws_whitelist_ok = True
        self.opt_skip_tests = False
        self.opt_no_push = False
        self.opt_dry_run = False
        self.opt_no_rollback = False
        self.baked = False
        self.resume = False
        self.log_path = None
        self.log_fh = None

    # ---------------------------------------------------------------- 输出
    def msg(self, text=""):
        print(text)

    def log(self, text=""):
        print(text)
        if self.log_fh:
            self.log_fh.write(text + "\n")
            self.log_fh.flush()

    def step(self, index, title):
        self.log("")
        self.log(f"[{index}] {title}")

    def ok(self, text):
        self.log(f"    OK - {text}")

    def init_log(self):
        LOG_DIR.mkdir(parents=True, exist_ok=True)
        stamp = datetime.now().strftime("%Y%m%d-%H%M%S")
        self.log_path = LOG_DIR / f"release-v{self.version}-{stamp}.log"
        self.log_fh = open(self.log_path, "w", encoding="utf-8")

    def report_fail(self, text):
        self.log("")
        self.log(f"!! {text}")
        self.rollback()
        if self.log_path:
            self.log(f"   log: {self.log_path}")

    def fail(self, code, text):
        self.report_fail(text)
        return code

    # ---------------------------------------------------------------- git
    def git(self, *args):
        return run_cmd(["git", *args])

    def git_logged(self, *args) -> int:
        rc, out, err = run_cmd(["git", *args])
        for line in (out + err).splitlines():
            self.log(f"    {line}")
        return rc

    # ---------------------------------------------------------------- 状态读取
    def read_versions(self) -> bool:
        self.pom_version = ""
        self.pkg_version = ""
        try:
            root = ET.parse(ROOT / "pom.xml").getroot()
            for child in root:
                if _local(child.tag) == "version":
                    self.pom_version = (child.text or "").strip()
                    break
        except (OSError, ET.ParseError):
            self.pom_version = ""
        pkg = ROOT / "web" / "package.json"
        if pkg.exists():
            try:
                self.pkg_version = str(
                    json.loads(pkg.read_text(encoding="utf-8")).get("version", "")
                )
            except (OSError, ValueError):
                self.pkg_version = ""
        return bool(self.pom_version) and bool(self.pkg_version)

    def read_tags(self):
        rc, out, _ = self.git("describe", "--tags", "--abbrev=0")
        self.local_tag = out.strip() if rc == 0 else ""

        rc, out, _ = self.git(
            "ls-remote", "--tags", "--refs", "--sort=-v:refname", "origin", "refs/tags/v*"
        )
        self.remote_check_ok = rc == 0
        self.remote_tag = ""
        if rc == 0:
            for line in out.splitlines():
                parts = line.split()
                if len(parts) >= 2 and parts[1].startswith("refs/tags/"):
                    self.remote_tag = parts[1][len("refs/tags/"):]
                    break

    def read_dirty(self):
        _, out, _ = self.git("status", "--porcelain")
        self.dirty_lines = [line for line in out.splitlines() if line.strip()]
        return len(self.dirty_lines)

    def check_whitelist(self) -> bool:
        self.ws_whitelist_ok = all(
            WHITELIST_RE.match(line) for line in self.dirty_lines
        )
        return self.ws_whitelist_ok

    def bad_lines(self):
        return [line for line in self.dirty_lines if not WHITELIST_RE.match(line)]

    def get_branch(self):
        _, out, _ = self.git("rev-parse", "--abbrev-ref", "HEAD")
        self.branch = out.strip()

    def short_sha(self) -> str:
        _, out, _ = self.git("rev-parse", "--short", "HEAD")
        return out.strip()

    def compute_next(self):
        base = self.remote_tag or self.local_tag
        if base[:1] in ("v", "V"):
            base = base[1:]
        if not base:
            base = self.pom_version
        if not VERSION_RE.match(base):
            return None
        major, minor, patch = (int(x) for x in base.split("."))
        return {
            "base": base,
            "patch": f"{major}.{minor}.{patch + 1}",
            "minor": f"{major}.{minor + 1}.0",
            "major": f"{major + 1}.0.0",
        }

    def normalize_version(self):
        if self.version is None:
            return
        self.version = self.version.replace(" ", "")
        if self.version[:1] in ("v", "V"):
            self.version = self.version[1:]

    # ---------------------------------------------------------------- tag 检查
    def tag_exists_local(self) -> bool:
        rc, _, _ = self.git("rev-parse", "-q", "--verify", f"refs/tags/{self.tag}")
        return rc == 0

    def compute_tag_at_head(self) -> bool:
        if not self.tag_local:
            return False
        rc, tag_sha, _ = self.git("rev-list", "-n", "1", self.tag)
        if rc != 0 or not tag_sha.strip():
            return False
        _, head_sha, _ = self.git("rev-parse", "HEAD")
        return tag_sha.strip() == head_sha.strip()

    def tag_exists_remote(self) -> bool:
        rc, out, _ = self.git("ls-remote", "--tags", "--refs", "origin", f"refs/tags/{self.tag}")
        self.remote_check_ok = rc == 0
        return rc == 0 and bool(out.strip())

    # ---------------------------------------------------------------- 命令执行 + tee
    def run_logged(self, cmd, encoding: str) -> int:
        if isinstance(cmd, (list, tuple)):
            popen_args, use_shell = list(cmd), False
        else:
            popen_args, use_shell = cmd, True
        proc = subprocess.Popen(
            popen_args,
            cwd=str(ROOT),
            shell=use_shell,
            stdin=subprocess.DEVNULL,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
        )
        assert proc.stdout is not None
        for raw in proc.stdout:
            line = raw.decode(encoding, "replace").rstrip("\r\n")
            print(line)
            if self.log_fh:
                self.log_fh.write(line + "\n")
                self.log_fh.flush()
        proc.wait()
        return proc.returncode

    # ---------------------------------------------------------------- 预检
    def preflight(self) -> bool:
        self.log(f"    repository: {ROOT}")
        for cmd in ("git", "node", "mvn", "npm", "gh"):
            if shutil.which(cmd) is None:
                self.log(f"    ERROR: required command not found: {cmd}")
                return False
        if run_cmd(["gh", "auth", "status"])[0] != 0:
            self.log("    ERROR: gh is not authenticated, run: gh auth login")
            return False
        if self.git("rev-parse", "--git-dir")[0] != 0:
            self.log("    ERROR: not a git repository")
            return False
        if self.git("remote", "get-url", "origin")[0] != 0:
            self.log("    ERROR: git remote 'origin' is not configured")
            return False
        self.get_branch()
        if self.branch != "main":
            self.log(f"    ERROR: current branch is {self.branch}, expected main")
            return False

        _, gitdir, _ = self.git("rev-parse", "--git-dir")
        gitdir_path = Path(gitdir.strip())
        if not gitdir_path.is_absolute():
            gitdir_path = ROOT / gitdir_path
        for marker in ("MERGE_HEAD", "CHERRY_PICK_HEAD", "REVERT_HEAD"):
            if (gitdir_path / marker).exists():
                self.log(f"    ERROR: unfinished git operation: {marker}")
                return False
        if (gitdir_path / "rebase-merge").exists() or (gitdir_path / "rebase-apply").exists():
            self.log("    ERROR: a rebase/am is in progress")
            return False
        self.ok("git, node, mvn, npm, gh ready; branch=main; no unfinished git operation")
        return True

    # ---------------------------------------------------------------- 回滚
    def rollback(self):
        if not self.baked or self.resume:
            return
        if self.opt_no_rollback:
            self.log("   version bump kept (--no-rollback)")
            return
        self.read_dirty()
        if not self.check_whitelist():
            self.log("   rollback skipped: workspace has files outside the whitelist")
            return
        self.log("   rolling back the version bump")
        for line in self.dirty_lines:
            self.git("checkout", "--", line[3:])
        self.baked = False

    # ---------------------------------------------------------------- status
    def cmd_status(self) -> int:
        self.msg("")
        self.msg("==================== open-admin release status ====================")
        if not self.read_versions():
            return self.fail(EXIT_USAGE, "cannot read versions from pom.xml / web/package.json")
        self.read_tags()
        self.read_dirty()
        self.check_whitelist()
        self.get_branch()
        self.msg(f"  repository      : {ROOT}")
        self.msg(f"  branch          : {self.branch}")
        self.msg(f"  pom.xml version : {self.pom_version}")
        self.msg(f"  npm version     : {self.pkg_version}")
        self.msg(f"  latest tag local: {self.local_tag}")
        self.msg(f"  latest tag origin: {self.remote_tag}")
        self.msg(f"  modified files  : {len(self.dirty_lines)}")
        if not self.ws_whitelist_ok:
            self.msg("  workspace       : dirty (files outside whitelist)")
        elif self.dirty_lines:
            self.msg("  workspace       : dirty (version files only)")
        else:
            self.msg("  workspace       : clean")
        self.msg("==================================================================")
        self.msg("")
        return EXIT_OK

    # ---------------------------------------------------------------- check
    def cmd_check(self) -> int:
        if not self.read_versions():
            return self.fail(EXIT_USAGE, "cannot read versions from pom.xml / web/package.json")
        self.read_tags()
        self.read_dirty()
        self.check_whitelist()
        self.get_branch()

        nxt = self.compute_next()
        if nxt is None:
            self.msg("")
            self.msg(f"  ERROR: cannot parse latest tag: {self.remote_tag or self.local_tag or self.pom_version}")
            return EXIT_USAGE

        if not self.ws_whitelist_ok:
            workspace = "dirty-outside-whitelist"
        elif self.dirty_lines:
            workspace = "dirty-version-files"
        else:
            workspace = "clean"

        print(f"REPO_ROOT={ROOT}")
        print(f"BRANCH={self.branch}")
        print(f"POM_VERSION={self.pom_version}")
        print(f"NPM_VERSION={self.pkg_version}")
        print(f"LATEST_TAG_LOCAL={self.local_tag}")
        print(f"LATEST_TAG_REMOTE={self.remote_tag}")
        print(f"BASE_VERSION={nxt['base']}")
        print(f"NEXT_PATCH={nxt['patch']}")
        print(f"NEXT_MINOR={nxt['minor']}")
        print(f"NEXT_MAJOR={nxt['major']}")
        print(f"WORKSPACE={workspace}")
        if not self.remote_check_ok:
            print("TAGS_IN_SYNC=unknown")
            print("WARN=cannot reach origin, remote tag check skipped")
        elif self.local_tag == self.remote_tag:
            print("TAGS_IN_SYNC=yes")
        else:
            print("TAGS_IN_SYNC=no")
        return EXIT_OK

    # ---------------------------------------------------------------- run
    def cmd_run(self) -> int:
        if not self.version:
            self.msg("")
            self.msg("  ERROR: no version given")
            self.msg("  use: python scripts/release.py 3.1.3")
            return usage()
        self.normalize_version()
        if not VERSION_RE.match(self.version or ""):
            self.msg("")
            self.msg(f"  ERROR: invalid version '{self.version}', expected x.y.z")
            self.msg("  the leading v is optional, the tag is always v-prefixed")
            return EXIT_USAGE
        self.tag = "v" + self.version

        self.init_log()
        self.log("==================================================================")
        self.log(f" open-admin release v{self.version}")
        self.log(f" root : {ROOT}")
        self.log(f" log  : {self.log_path}")
        self.log("==================================================================")

        # step 1
        self.step("1/8", "Preflight checks")
        if not self.preflight():
            return self.fail(EXIT_USAGE, "preflight checks failed")

        # step 2
        self.step("2/8", "Version, tag and workspace checks")
        if not self.read_versions():
            return self.fail(EXIT_USAGE, "cannot read versions from pom.xml / web/package.json")
        self.tag_local = self.tag_exists_local()
        self.tag_at_head = self.compute_tag_at_head()
        self.versions_at_target = (
            self.pom_version == self.version and self.pkg_version == self.version
        )
        self.log(
            f"    pom.xml={self.pom_version}  web/package.json={self.pkg_version}  target={self.version}"
        )

        if self.tag_local:
            if self.tag_at_head and self.versions_at_target:
                self.resume = True
                self.ok(f"tag {self.tag} already at HEAD - resume mode, only the push is left")
            else:
                self.log(f"    local tag {self.tag} already exists and does not match the current state")
                self.log(f"    to start over: git tag -d {self.tag}")
                return self.fail(EXIT_CONFLICT, f"local tag {self.tag} already exists")
        else:
            self.tag_remote = self.tag_exists_remote()
            if not self.remote_check_ok:
                self.log("    WARN: cannot reach origin, remote tag check skipped")
            if self.tag_remote:
                self.log(f"    tag {self.tag} already exists on origin")
                return self.fail(EXIT_CONFLICT, f"tag {self.tag} already exists on origin")

        self.read_dirty()
        if not self.check_whitelist():
            return self.ws_violation()
        self.log(f"    workspace: {len(self.dirty_lines)} modified file(s)")
        if self.dirty_lines:
            if self.versions_at_target:
                self.baked = True
                self.ok(f"version files already at v{self.version}, bump will be skipped")
            elif not self.opt_dry_run:
                self.log("    resetting leftover version changes from a previous run")
                for line in self.dirty_lines:
                    self.git("checkout", "--", line[3:])
            else:
                self.log("    [dry-run] would reset leftover version changes")

        # dry-run
        if self.opt_dry_run:
            self.log("")
            self.log("[dry-run] would execute:")
            self.log(f"    1) python scripts/bump-version.py {self.version}")
            self.log("    2) mvn -B clean test")
            self.log("    3) npm run build, run inside web")
            self.log(f"    4) git add -u ; git commit ; git tag -a {self.tag}")
            self.log(f"    5) git push origin {self.branch} ; git push origin {self.tag}")
            if self.resume:
                self.log("    state: resume, only step 5 would run")
            if self.baked:
                self.log("    state: version already at target, step 1 would be skipped")
            self.log("[dry-run] no repo file was modified")
            self.log("")
            return EXIT_OK

        # step 3 bump
        if not self.baked and not self.resume:
            self.step("3/8", f"Bump version to v{self.version}")
            self.baked = True
            bump_cmd = [sys.executable, str(ROOT / "scripts" / "bump-version.py"), self.version]
            if self.run_logged(bump_cmd, "utf-8") != 0:
                return self.fail(EXIT_USAGE, "bump-version.py failed")

        # step 4 verify
        self.step("4/8", "Verify pom.xml and web/package.json")
        if not self.read_versions():
            return self.fail(EXIT_USAGE, "cannot read versions from pom.xml / web/package.json")
        if self.pom_version != self.version or self.pkg_version != self.version:
            return self.fail(
                EXIT_CONFLICT,
                f"version mismatch after bump: pom.xml={self.pom_version} "
                f"npm={self.pkg_version} expected={self.version}",
            )
        self.ok(f"pom.xml and web/package.json are v{self.version}")

        # step 5 tests
        self.step("5/8", "Tests")
        if self.resume:
            pass
        elif self.opt_skip_tests:
            self.log("    WARN: tests skipped (--skip-tests)")
        else:
            self.log("    mvn -B clean test")
            pref = locale.getpreferredencoding(False) or "utf-8"
            if self.run_logged("mvn -B clean test", pref) != 0:
                return self.fail(EXIT_TESTS, "backend tests failed (mvn -B clean test)")
            self.ok("backend tests passed")
            if not (ROOT / "web" / "node_modules").exists():
                self.log("    web/node_modules missing, running npm install first")
                if self.run_logged("npm --prefix web install", "utf-8") != 0:
                    return self.fail(EXIT_TESTS, "npm install failed")
            self.log("    npm run build in web folder")
            if self.run_logged("npm --prefix web run build", "utf-8") != 0:
                return self.fail(EXIT_TESTS, "frontend build failed (npm run build)")
            self.ok("frontend build passed")

        # step 6 whitelist
        self.step("6/8", "Workspace whitelist check")
        self.read_dirty()
        if not self.check_whitelist():
            return self.ws_violation()
        self.log(f"    modified: {len(self.dirty_lines)} files, all inside the whitelist")
        self.ok("no unexpected artifacts")

        # step 7 commit + tag
        if not self.resume:
            self.step("7/8", f"Commit and tag {self.tag}")
            if self.git_logged("add", "-u") != 0:
                return self.fail(EXIT_GIT, "git add failed")
            if self.git_logged("commit", "-m", f"release: v{self.version}") != 0:
                return self.fail(EXIT_GIT, "git commit failed")
            if self.git_logged("tag", "-a", self.tag, "-m", f"release: v{self.version}") != 0:
                return self.fail(EXIT_GIT, "git tag failed")
            self.ok("commit and tag created")

        # step 8 push
        if self.opt_no_push:
            sha = self.short_sha()
            self.log("")
            self.log("==================================================================")
            self.log(f" Release v{self.version} committed and tagged, not pushed because of --no-push")
            self.log(f" tag    : {self.tag}")
            self.log(f" commit : {sha}")
            self.log(f" log    : {self.log_path}")
            self.log(" to finish, run:")
            self.log(f"   git push origin {self.branch}")
            self.log(f"   git push origin {self.tag}")
            self.log(" to undo, run:")
            self.log(f"   git tag -d {self.tag}")
            self.log(f"   git reset --hard origin/{self.branch}")
            self.log("==================================================================")
            self.log("")
            return EXIT_OK

        self.step("8/8", f"Push {self.branch} and {self.tag} to origin")
        if self.git_logged("push", "origin", self.branch) != 0:
            return self.fail(EXIT_GIT, "git push branch failed")
        if self.git_logged("push", "origin", self.tag) != 0:
            return self.fail(EXIT_GIT, "git push tag failed")
        self.ok("pushed to origin")

        sha = self.short_sha()
        self.log("")
        self.log("==================================================================")
        self.log(f" Release v{self.version} finished")
        self.log(f" tag    : {self.tag}")
        self.log(f" commit : {sha}")
        self.log(f" log    : {self.log_path}")
        self.log(" next   : CI publish.yml runs on the tag push, see gh run list --limit 3")
        self.log("==================================================================")
        self.log("")
        return EXIT_OK

    def ws_violation(self) -> int:
        self.log("")
        self.log("!! workspace has files outside the whitelist:")
        for line in self.bad_lines():
            self.log(f"   BAD: {line}")
        self.log("   whitelist: */pom.xml, web/package.json")
        return self.fail(EXIT_WORKSPACE, "workspace is not clean")


def usage() -> int:
    print("")
    print("  open-admin release helper")
    print("")
    print("  Usage:")
    print("    python scripts/release.py <version> [--skip-tests] [--no-push] [--dry-run] [--no-rollback]")
    print("    python scripts/release.py status")
    print("    python scripts/release.py check")
    print("")
    print("  the leading v is optional, e.g. python scripts/release.py v3.1.3")
    print("  the git tag is always created and pushed as v3.1.3")
    print("")
    print("  Exit codes: 0 ok / 1 usage / 2 tests / 3 workspace / 4 git / 5 conflict")
    print("")
    return EXIT_USAGE


def main(argv) -> int:
    rel = Release()

    # ---------- 解析命令 ----------
    if not argv:
        rel.msg("")
        rel.msg("  ERROR: no arguments")
        return usage()

    first = argv[0]
    rest = argv[1:]
    if first in ("--help", "-h"):
        return usage()
    if first in ("status", "check", "run"):
        cmd = first
    elif first.startswith("-"):
        cmd = "run"
        rest = argv
    else:
        cmd = "run"
        rel.version = first

    # ---------- 解析选项 ----------
    for arg in rest:
        if arg == "--skip-tests":
            rel.opt_skip_tests = True
        elif arg == "--no-push":
            rel.opt_no_push = True
        elif arg == "--dry-run":
            rel.opt_dry_run = True
        elif arg == "--no-rollback":
            rel.opt_no_rollback = True
        elif arg in ("--help", "-h"):
            return usage()
        elif arg.startswith("-"):
            rel.msg("")
            rel.msg(f"  ERROR: unknown argument: {arg}")
            return usage()
        elif rel.version is None:
            rel.version = arg
        else:
            rel.msg("")
            rel.msg(f"  ERROR: unknown argument: {arg}")
            return usage()

    if cmd == "status":
        return rel.cmd_status()
    if cmd == "check":
        return rel.cmd_check()
    if cmd == "run":
        try:
            return rel.cmd_run()
        except KeyboardInterrupt:
            rel.report_fail("interrupted")
            return 130
        except Exception as e:  # noqa: BLE001
            rel.report_fail(f"unexpected error: {e}")
            return EXIT_USAGE

    rel.msg("")
    rel.msg(f"  ERROR: unknown command: {cmd}")
    return usage()


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
