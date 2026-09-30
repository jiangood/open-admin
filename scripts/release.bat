@echo off
rem =====================================================================
rem  release.bat - open-admin release helper for Windows / cmd
rem
rem  Usage:
rem    scripts\release.bat status
rem    scripts\release.bat check
rem    scripts\release.bat run <x.y.z> [--skip-tests] [--no-push]
rem                                    [--dry-run] [--no-rollback]
rem
rem  run options:
rem    --skip-tests   skip "mvn -B clean test" and "npm run build"
rem    --no-push      commit and tag locally, do not push to origin
rem    --dry-run      inspect only, change no repo file
rem    --no-rollback  keep the version bump when the run fails
rem
rem  Exit codes:
rem    0 success
rem    1 usage error or preflight check failed
rem    2 tests failed (mvn test / npm run build)
rem    3 workspace has files outside the whitelist
rem    4 git commit / tag / push failed
rem    5 version or tag conflict
rem
rem  Notes:
rem    - only ASCII output, so the console never shows mojibake
rem    - full log is written to logs\release-v<version>-<stamp>.log (UTF-8)
rem    - whitelist: */pom.xml and web/package.json are the only files a
rem      release may modify
rem =====================================================================

for /f "tokens=2 delims=:" %%c in ('chcp') do set "OLDCP=%%c"
chcp 65001 >nul
setlocal
cd /d "%~dp0.."

set "ROOT=%CD%"
set "TMPDIR=%TEMP%\open-admin-release"
set "LOG="
set "CMD="
set "VERSION="
set "TAG="
set "BRANCH="
set "GITDIR=.git"
set "POM_VERSION="
set "PKG_VERSION="
set "LOCAL_TAG="
set "REMOTE_TAG="
set "FIRST_REMOTE="
set "TAG_LOCAL=0"
set "TAG_REMOTE=0"
set "REMOTE_CHECK_OK=0"
set "TAG_AT_HEAD=0"
set "VERSIONS_AT_TARGET=0"
set "DIRTY_COUNT=0"
set "WS_WHITELIST_OK=1"
set "OPT_SKIP_TESTS="
set "OPT_NO_PUSH="
set "OPT_DRY_RUN="
set "OPT_NO_ROLLBACK="
set "BAKED="
set "RESUME="
set "SHA="
set "FAIL_CODE=1"
set "FAIL_MSG=release failed"

call :main %*
set "RC=%ERRORLEVEL%"
if defined OLDCP chcp %OLDCP% >nul
exit /b %RC%

rem =====================================================================
rem  entry
rem =====================================================================
:main
if "%~1"=="" goto usage
if /I "%~1"=="--help" goto usage
if /I "%~1"=="-h" goto usage
set "CMD=%~1"
shift

:parse_args
if "%~1"=="" goto args_done
if /I "%~1"=="--skip-tests" goto opt_skip_tests
if /I "%~1"=="--no-push" goto opt_no_push
if /I "%~1"=="--dry-run" goto opt_dry_run
if /I "%~1"=="--no-rollback" goto opt_no_rollback
if /I "%~1"=="--help" goto usage
if /I "%~1"=="-h" goto usage
if not "%VERSION%"=="" goto unknown_arg
if /I "%CMD%"=="run" goto opt_version
goto unknown_arg

:opt_skip_tests
set "OPT_SKIP_TESTS=1"
shift
goto parse_args

:opt_no_push
set "OPT_NO_PUSH=1"
shift
goto parse_args

:opt_dry_run
set "OPT_DRY_RUN=1"
shift
goto parse_args

:opt_no_rollback
set "OPT_NO_ROLLBACK=1"
shift
goto parse_args

:opt_version
set "VERSION=%~1"
shift
goto parse_args

:unknown_arg
call :msg ""
call :msg "  ERROR: unknown argument: %~1"
goto usage

:args_done
if /I "%CMD%"=="status" goto cmd_status
if /I "%CMD%"=="check" goto cmd_check
if /I "%CMD%"=="run" goto cmd_run
call :msg ""
call :msg "  ERROR: unknown command: %CMD%"
goto usage

:usage
call :msg ""
call :msg "  open-admin release helper"
call :msg ""
call :msg "  Usage:"
call :msg "    scripts\release.bat status"
call :msg "    scripts\release.bat check"
call :msg "    scripts\release.bat run x.y.z [--skip-tests] [--no-push] [--dry-run] [--no-rollback]"
call :msg ""
call :msg "  Exit codes: 0 ok / 1 usage / 2 tests / 3 workspace / 4 git / 5 conflict"
call :msg ""
exit /b 1

rem =====================================================================
rem  status - print current state
rem =====================================================================
:cmd_status
call :msg ""
call :msg "==================== open-admin release status ===================="
call :read_versions
if errorlevel 1 goto fail_versions
call :read_tags
call :read_dirty
call :get_branch
call :check_whitelist
if errorlevel 1 set "WS_WHITELIST_OK=0"
call :msg "  repository      : %ROOT%"
call :msg "  branch          : %BRANCH%"
call :msg "  pom.xml version : %POM_VERSION%"
call :msg "  npm version     : %PKG_VERSION%"
call :msg "  latest tag local: %LOCAL_TAG%"
call :msg "  latest tag origin: %REMOTE_TAG%"
call :msg "  modified files  : %DIRTY_COUNT%"
if "%WS_WHITELIST_OK%"=="0" call :msg "  workspace       : dirty (files outside whitelist)"
if "%WS_WHITELIST_OK%"=="1" if not "%DIRTY_COUNT%"=="0" call :msg "  workspace       : dirty (version files only)"
if "%WS_WHITELIST_OK%"=="1" if "%DIRTY_COUNT%"=="0" call :msg "  workspace       : clean"
call :msg "=================================================================="
call :msg ""
exit /b 0

rem =====================================================================
rem  check - read-only, KEY=VALUE output for tooling
rem =====================================================================
:cmd_check
call :read_versions
if errorlevel 1 goto fail_versions
call :read_tags
call :read_dirty
call :get_branch
call :check_whitelist
if errorlevel 1 set "WS_WHITELIST_OK=0"

set "BASE=%REMOTE_TAG%"
if "%BASE%"=="" set "BASE=%LOCAL_TAG%"
set "BASE=%BASE:v=%"
if "%BASE%"=="" set "BASE=%POM_VERSION%"

echo %BASE%| findstr /r "^[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*$" >nul
if errorlevel 1 goto check_bad_base
set "V_MAJOR=0"
set "V_MINOR=0"
set "V_PATCH=0"
for /f "tokens=1,2,3 delims=." %%a in ("%BASE%") do (
  set "V_MAJOR=%%a"
  set "V_MINOR=%%b"
  set "V_PATCH=%%c"
)
set /a NEXT_PATCH=%V_PATCH%+1 >nul
set /a NEXT_MINOR=%V_MINOR%+1 >nul
set /a NEXT_MAJOR=%V_MAJOR%+1 >nul

set "WORKSPACE=clean"
if not "%WS_WHITELIST_OK%"=="1" set "WORKSPACE=dirty-outside-whitelist"
if "%WS_WHITELIST_OK%"=="1" if not "%DIRTY_COUNT%"=="0" set "WORKSPACE=dirty-version-files"

echo REPO_ROOT=%ROOT%
echo BRANCH=%BRANCH%
echo POM_VERSION=%POM_VERSION%
echo NPM_VERSION=%PKG_VERSION%
echo LATEST_TAG_LOCAL=%LOCAL_TAG%
echo LATEST_TAG_REMOTE=%REMOTE_TAG%
echo BASE_VERSION=%BASE%
echo NEXT_PATCH=%V_MAJOR%.%V_MINOR%.%NEXT_PATCH%
echo NEXT_MINOR=%V_MAJOR%.%NEXT_MINOR%.0
echo NEXT_MAJOR=%NEXT_MAJOR%.0.0
echo WORKSPACE=%WORKSPACE%
if not "%REMOTE_CHECK_OK%"=="1" echo TAGS_IN_SYNC=unknown
if "%REMOTE_CHECK_OK%"=="1" if "%LOCAL_TAG%"=="%REMOTE_TAG%" echo TAGS_IN_SYNC=yes
if "%REMOTE_CHECK_OK%"=="1" if not "%LOCAL_TAG%"=="%REMOTE_TAG%" echo TAGS_IN_SYNC=no
if not "%REMOTE_CHECK_OK%"=="1" echo WARN=cannot reach origin, remote tag check skipped
exit /b 0

:check_bad_base
call :msg ""
call :msg "  ERROR: cannot parse latest tag: %BASE%"
exit /b 1

rem =====================================================================
rem  run - the release itself
rem =====================================================================
:cmd_run
if "%VERSION%"=="" goto run_no_version

node -e "process.exit(/^\d+\.\d+\.\d+$/.test(process.argv[1])?0:1)" "%VERSION%" 2>nul
if errorlevel 1 goto run_bad_version
set "TAG=v%VERSION%"

call :init_log
call :log "=================================================================="
call :log " open-admin release v%VERSION%"
call :log " root : %ROOT%"
call :log " log  : %LOG%"
call :log "=================================================================="

rem ---------- step 1: preflight ----------
call :step "1/8" "Preflight checks"
call :preflight
if errorlevel 1 goto fail_preflight

rem ---------- step 2: conflict and workspace checks ----------
call :step "2/8" "Version, tag and workspace checks"
call :read_versions
if errorlevel 1 goto fail_versions
call :tag_exists_local
call :tag_at_head
set "VERSIONS_AT_TARGET=0"
if "%POM_VERSION%"=="%VERSION%" if "%PKG_VERSION%"=="%VERSION%" set "VERSIONS_AT_TARGET=1"
call :log "    pom.xml=%POM_VERSION%  web/package.json=%PKG_VERSION%  target=%VERSION%"

if "%TAG_LOCAL%"=="1" goto local_tag_found
call :tag_exists_remote
if "%REMOTE_CHECK_OK%"=="0" call :log "    WARN: cannot reach origin, remote tag check skipped"
if "%TAG_REMOTE%"=="1" goto remote_tag_found
goto check_workspace

:local_tag_found
if "%TAG_AT_HEAD%"=="1" if "%VERSIONS_AT_TARGET%"=="1" goto set_resume
call :log "    local tag %TAG% already exists and does not match the current state"
call :log "    to start over: git tag -d %TAG%"
set "FAIL_CODE=5"
set "FAIL_MSG=local tag %TAG% already exists"
goto finish_fail

:remote_tag_found
call :log "    tag %TAG% already exists on origin"
set "FAIL_CODE=5"
set "FAIL_MSG=tag %TAG% already exists on origin"
goto finish_fail

:set_resume
set "RESUME=1"
call :ok "tag %TAG% already at HEAD - resume mode, only the push is left"

:check_workspace
call :read_dirty
call :check_whitelist
if errorlevel 1 goto ws_violation
call :log "    workspace: %DIRTY_COUNT% modified file(s)"
if "%DIRTY_COUNT%"=="0" goto after_workspace
if "%VERSIONS_AT_TARGET%"=="1" goto ws_already_baked
call :log "    resetting leftover version changes from a previous run"
for /f "tokens=1,*" %%a in ('git status --porcelain') do call :revert "%%b"
goto after_workspace

:ws_already_baked
set "BAKED=1"
call :ok "version files already at v%VERSION%, bump will be skipped"

:after_workspace
if not defined OPT_DRY_RUN goto do_bump
call :log ""
call :log "[dry-run] would execute:"
call :log "    1) node scripts/bump-version.js %VERSION%"
call :log "    2) mvn -B clean test"
call :log "    3) npm run build, run inside web"
call :log "    4) git add -u ; git commit ; git tag -a %TAG%"
call :log "    5) git push origin %BRANCH% ; git push origin %TAG%"
if defined RESUME call :log "    state: resume, only step 5 would run"
if defined BAKED call :log "    state: version already at target, step 1 would be skipped"
call :log "[dry-run] no repo file was modified"
call :log ""
exit /b 0

rem ---------- step 3: bump ----------
:do_bump
if defined BAKED goto step_verify
if defined RESUME goto step_verify
call :step "3/8" "Bump version to v%VERSION%"
set "BAKED=1"
set "REL_CMD=node scripts/bump-version.js %VERSION%"
call :run_logged
if errorlevel 1 goto fail_bump

rem ---------- step 4: verify ----------
:step_verify
call :step "4/8" "Verify pom.xml and web/package.json"
call :read_versions
if errorlevel 1 goto fail_versions
if not "%POM_VERSION%"=="%VERSION%" goto version_mismatch
if not "%PKG_VERSION%"=="%VERSION%" goto version_mismatch
call :ok "pom.xml and web/package.json are v%VERSION%"

rem ---------- step 5: tests ----------
call :step "5/8" "Tests"
if defined RESUME goto tests_done
if defined OPT_SKIP_TESTS goto tests_skipped
call :log "    mvn -B clean test"
set "REL_CMD=mvn -B clean test"
call :run_logged
if errorlevel 1 goto fail_backend_tests
call :ok "backend tests passed"
if exist "web\node_modules" goto frontend_build
call :log "    web\node_modules missing, running npm install first"
pushd "web"
set "REL_CMD=npm install"
call :run_logged
set "RC=%ERRORLEVEL%"
popd
if not "%RC%"=="0" goto fail_npm_install
:frontend_build
call :log "    npm run build in web folder"
pushd "web"
set "REL_CMD=npm run build"
call :run_logged
set "RC=%ERRORLEVEL%"
popd
if not "%RC%"=="0" goto fail_frontend_build
call :ok "frontend build passed"
goto tests_done

:tests_skipped
call :log "    WARN: tests skipped (--skip-tests)"
goto tests_done

:tests_done

rem ---------- step 6: whitelist ----------
call :step "6/8" "Workspace whitelist check"
call :read_dirty
call :check_whitelist
if errorlevel 1 goto ws_violation
call :log "    modified: %DIRTY_COUNT% files, all inside the whitelist"
call :ok "no unexpected artifacts"

rem ---------- step 7: commit + tag ----------
if defined RESUME goto step_push
call :step "7/8" "Commit and tag %TAG%"
set "REL_CMD=git add -u"
call :run_logged
if errorlevel 1 goto fail_git_add
set "REL_CMD=git commit -m "release: v%VERSION%""
call :run_logged
if errorlevel 1 goto fail_git_commit
set "REL_CMD=git tag -a "%TAG%" -m "release: v%VERSION%""
call :run_logged
if errorlevel 1 goto fail_git_tag
call :ok "commit and tag created"

rem ---------- step 8: push ----------
:step_push
if defined OPT_NO_PUSH goto no_push
call :step "8/8" "Push %BRANCH% and %TAG% to origin"
set "REL_CMD=git push origin "%BRANCH%""
call :run_logged
if errorlevel 1 goto fail_push_branch
set "REL_CMD=git push origin "%TAG%""
call :run_logged
if errorlevel 1 goto fail_push_tag
call :ok "pushed to origin"

rem ---------- done ----------
call :short_sha
call :log ""
call :log "=================================================================="
call :log " Release v%VERSION% finished"
call :log " tag    : %TAG%"
call :log " commit : %SHA%"
call :log " log    : %LOG%"
call :log " next   : CI publish.yml runs on the tag push, see gh run list --limit 3"
call :log "=================================================================="
call :log ""
exit /b 0

:no_push
call :short_sha
call :log ""
call :log "=================================================================="
call :log " Release v%VERSION% committed and tagged, not pushed because of --no-push"
call :log " tag    : %TAG%"
call :log " commit : %SHA%"
call :log " log    : %LOG%"
call :log " to finish, run:"
call :log "   git push origin %BRANCH%"
call :log "   git push origin %TAG%"
call :log " to undo, run:"
call :log "   git tag -d %TAG%"
call :log "   git reset --hard origin/%BRANCH%"
call :log "=================================================================="
call :log ""
exit /b 0

rem =====================================================================
rem  failures
rem =====================================================================
:run_no_version
call :msg ""
call :msg "  ERROR: run needs a version, e.g. scripts\release.bat run 3.1.3"
goto usage

:run_bad_version
call :msg ""
call :msg "  ERROR: invalid version '%VERSION%', expected x.y.z"
exit /b 1

:fail_preflight
call :report_fail "preflight checks failed"
exit /b 1

:fail_versions
call :report_fail "cannot read versions from pom.xml / web/package.json"
exit /b 1

:version_mismatch
call :report_fail "version mismatch after bump: pom.xml=%POM_VERSION% npm=%PKG_VERSION% expected=%VERSION%"
exit /b 5

:ws_violation
call :log ""
call :log "!! workspace has files outside the whitelist:"
call :print_bad
call :log "   whitelist: */pom.xml, web/package.json"
set "FAIL_CODE=3"
set "FAIL_MSG=workspace is not clean"
goto finish_fail

:fail_bump
set "FAIL_CODE=1"
set "FAIL_MSG=bump-version.js failed"
goto finish_fail

:fail_backend_tests
set "FAIL_CODE=2"
set "FAIL_MSG=backend tests failed (mvn -B clean test)"
goto finish_fail

:fail_npm_install
set "FAIL_CODE=2"
set "FAIL_MSG=npm install failed"
goto finish_fail

:fail_frontend_build
set "FAIL_CODE=2"
set "FAIL_MSG=frontend build failed (npm run build)"
goto finish_fail

:fail_git_add
set "FAIL_CODE=4"
set "FAIL_MSG=git add failed"
goto finish_fail

:fail_git_commit
set "FAIL_CODE=4"
set "FAIL_MSG=git commit failed"
goto finish_fail

:fail_git_tag
set "FAIL_CODE=4"
set "FAIL_MSG=git tag failed"
goto finish_fail

:fail_push_branch
set "FAIL_CODE=4"
set "FAIL_MSG=git push branch failed"
goto finish_fail

:fail_push_tag
set "FAIL_CODE=4"
set "FAIL_MSG=git push tag failed"
goto finish_fail

:finish_fail
call :report_fail "%FAIL_MSG%"
exit /b %FAIL_CODE%

rem =====================================================================
rem  helpers
rem =====================================================================

rem %~1 = message, console only
:msg
if "%~1"=="" goto msg_blank
echo %~1
exit /b 0
:msg_blank
echo.
exit /b 0

rem %~1 = message, console + log (empty string prints a blank line)
:log
if "%~1"=="" goto log_blank
echo %~1
if defined LOG >>"%LOG%" echo %~1
exit /b 0
:log_blank
echo.
if defined LOG >>"%LOG%" echo.
exit /b 0

rem %~1 = index, %~2 = title
:step
call :log ""
call :log "[%~1] %~2"
exit /b 0

rem %~1 = message
:ok
call :log "    OK - %~1"
exit /b 0

:init_log
if not exist "%TMPDIR%" mkdir "%TMPDIR%" >nul 2>&1
if not exist "logs" mkdir "logs" >nul 2>&1
set "STAMP="
for /f "delims=" %%d in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmmss"') do set "STAMP=%%d"
set "LOG=%ROOT%\logs\release-v%VERSION%-%STAMP%.log"
echo open-admin release v%VERSION% > "%LOG%"
exit /b 0

rem run %REL_CMD%, tee stdout+stderr to console and %LOG%, return its exit code
:run_logged
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Console]::OutputEncoding=[Text.UTF8Encoding]::new($false); $w=New-Object System.IO.StreamWriter($env:LOG,$true,[Text.UTF8Encoding]::new($false)); cmd.exe /c $env:REL_CMD 2>&1 | ForEach-Object { Write-Host $_; $w.WriteLine($_) }; $w.Close(); exit $LASTEXITCODE"
exit /b %ERRORLEVEL%

rem during run: abort with message %~1 before modifying files
:preflight
call :log "    repository: %ROOT%"
for %%c in (git node mvn npm gh powershell) do (
  where %%c >nul 2>&1
  if errorlevel 1 (
    call :log "    ERROR: required command not found: %%c"
    exit /b 1
  )
)
gh auth status >nul 2>&1
if errorlevel 1 (
  call :log "    ERROR: gh is not authenticated, run: gh auth login"
  exit /b 1
)
git rev-parse --git-dir >nul 2>&1
if errorlevel 1 (
  call :log "    ERROR: not a git repository"
  exit /b 1
)
git remote get-url origin >nul 2>&1
if errorlevel 1 (
  call :log "    ERROR: git remote 'origin' is not configured"
  exit /b 1
)
call :get_branch
if /I not "%BRANCH%"=="main" (
  call :log "    ERROR: current branch is %BRANCH%, expected main"
  exit /b 1
)
for /f "delims=" %%d in ('git rev-parse --git-dir') do set "GITDIR=%%d"
for %%f in (MERGE_HEAD CHERRY_PICK_HEAD REVERT_HEAD) do if exist "%GITDIR%\%%f" (
  call :log "    ERROR: unfinished git operation: %%f"
  exit /b 1
)
if exist "%GITDIR%\rebase-merge" (
  call :log "    ERROR: a rebase is in progress"
  exit /b 1
)
if exist "%GITDIR%\rebase-apply" (
  call :log "    ERROR: a rebase or am is in progress"
  exit /b 1
)
call :ok "git, node, mvn, npm, gh ready; branch=main; no unfinished git operation"
exit /b 0

:get_branch
set "BRANCH="
git rev-parse --abbrev-ref HEAD > "%TMPDIR%\branch.txt" 2>nul
set /p BRANCH=<"%TMPDIR%\branch.txt"
exit /b 0

:short_sha
set "SHA="
git rev-parse --short HEAD > "%TMPDIR%\sha.txt" 2>nul
set /p SHA=<"%TMPDIR%\sha.txt"
exit /b 0

:read_versions
if not exist "%TMPDIR%" mkdir "%TMPDIR%" >nul 2>&1
set "POM_VERSION="
set "PKG_VERSION="
powershell -NoProfile -Command "$x=[xml](Get-Content -Raw -Encoding UTF8 'pom.xml'); Write-Output $x.project.version" > "%TMPDIR%\pomver.txt" 2>nul
set /p POM_VERSION=<"%TMPDIR%\pomver.txt"
if exist "web\package.json" powershell -NoProfile -Command "Write-Output (Get-Content -Raw -Encoding UTF8 'web/package.json' | ConvertFrom-Json).version" > "%TMPDIR%\pkgver.txt" 2>nul
set /p PKG_VERSION=<"%TMPDIR%\pkgver.txt"
if not defined POM_VERSION exit /b 1
if not defined PKG_VERSION exit /b 1
exit /b 0

:read_tags
set "LOCAL_TAG="
set "REMOTE_TAG="
set "FIRST_REMOTE="
set "REMOTE_CHECK_OK=0"
git describe --tags --abbrev=0 > "%TMPDIR%\local_tag.txt" 2>nul
set /p LOCAL_TAG=<"%TMPDIR%\local_tag.txt"
git ls-remote --tags --refs --sort=-v:refname origin "refs/tags/v*" > "%TMPDIR%\remote_tags.txt" 2>nul
if not errorlevel 1 set "REMOTE_CHECK_OK=1"
for /f "tokens=2" %%t in ('type "%TMPDIR%\remote_tags.txt" 2^>nul') do (
  set "FIRST_REMOTE=%%t"
  goto remote_tag_got
)
:remote_tag_got
if defined FIRST_REMOTE set "REMOTE_TAG=%FIRST_REMOTE:refs/tags/=%"
exit /b 0

:read_dirty
set "DIRTY_COUNT=0"
for /f "delims=" %%l in ('git status --porcelain 2^>nul') do set /a DIRTY_COUNT+=1
exit /b 0

rem errorlevel 0 = only whitelisted files are modified, 1 = something else is there
:check_whitelist
set "WS_WHITELIST_OK=1"
if not exist "%TMPDIR%" mkdir "%TMPDIR%" >nul 2>&1
powershell -NoProfile -Command "@(git status --porcelain | Where-Object { $_ -notmatch '^.. ((.*/)?pom\.xml|web/package\.json)$' }) | ForEach-Object { 'BAD: ' + $_ }" > "%TMPDIR%\bad.txt" 2>nul
findstr /c:"BAD:" "%TMPDIR%\bad.txt" >nul 2>&1
if errorlevel 1 exit /b 0
set "WS_WHITELIST_OK=0"
exit /b 1

:print_bad
if not exist "%TMPDIR%\bad.txt" exit /b 0
for /f "delims=" %%l in ('type "%TMPDIR%\bad.txt" 2^>nul') do call :log "   %%l"
exit /b 0

:tag_exists_local
set "TAG_LOCAL=0"
git rev-parse -q --verify "refs/tags/%TAG%" >nul 2>&1
if not errorlevel 1 set "TAG_LOCAL=1"
exit /b 0

:tag_at_head
set "TAG_AT_HEAD=0"
set "TAG_SHA="
set "HEAD_SHA="
if "%TAG_LOCAL%"=="0" exit /b 0
git rev-list -n 1 "%TAG%" > "%TMPDIR%\tag_sha.txt" 2>nul
set /p TAG_SHA=<"%TMPDIR%\tag_sha.txt"
git rev-parse HEAD > "%TMPDIR%\head_sha.txt" 2>nul
set /p HEAD_SHA=<"%TMPDIR%\head_sha.txt"
if not defined TAG_SHA exit /b 0
if "%TAG_SHA%"=="%HEAD_SHA%" set "TAG_AT_HEAD=1"
exit /b 0

:tag_exists_remote
set "TAG_REMOTE=0"
set "REMOTE_CHECK_OK=0"
if not exist "%TMPDIR%" mkdir "%TMPDIR%" >nul 2>&1
git ls-remote --tags --refs origin "refs/tags/%TAG%" > "%TMPDIR%\remote_tag.txt" 2>nul
if not errorlevel 1 set "REMOTE_CHECK_OK=1"
for %%A in ("%TMPDIR%\remote_tag.txt") do if %%~zA GTR 0 set "TAG_REMOTE=1"
exit /b 0

rem abort: log the message, roll the version bump back
:report_fail
call :log ""
call :log "!! %~1"
call :rollback
if defined LOG call :log "   log: %LOG%"
exit /b 0

rem undo the version bump (only version files, only before commit)
:rollback
if not defined BAKED exit /b 0
if defined RESUME exit /b 0
if defined OPT_NO_ROLLBACK goto rollback_skipped
call :check_whitelist
if errorlevel 1 goto rollback_unsafe
call :log "   rolling back the version bump"
for /f "tokens=1,*" %%a in ('git status --porcelain') do call :revert "%%b"
set "BAKED="
exit /b 0
:rollback_skipped
call :log "   version bump kept (--no-rollback)"
exit /b 0
:rollback_unsafe
call :log "   rollback skipped: workspace has files outside the whitelist"
exit /b 0

rem %1 = quoted path, restore it from HEAD
:revert
if "%~1"=="" exit /b 0
git checkout -- %1 >nul 2>&1
exit /b 0
