---
name: oa-sonar-scan
description: Use when scanning the open-admin framework repo (or code it produces) with the local SonarQube — run the scan, fetch results, triage/fix findings one by one with user confirmation, and verify with re-scans. Do not use for configuring SonarQube itself or scanning external projects.
---

# OA Sonar Scan

## Overview

Runs the open-admin repo through the local SonarQube (http://localhost:9000) with real JaCoCo coverage, then triages and fixes findings (bugs / vulnerabilities / code smells) one at a time, verifying each fix with a re-scan before committing.

## When to Use

- After a batch of changes to the framework backend (`src/main/java`)
- Before a release, to confirm no new Bugs / Vulnerabilities
- When SonarQube reports issues on the `open-admin` project

## Prerequisites

- Local SonarQube running (port 9000, docker-compose under `/ws/sonarqube`)
- Maven + local SonarQube token (defaults baked into `scripts/sonar-scan.sh`)
- H2 test DB — no MySQL needed (Repository/Service tests use H2)

## Immediate Execution

**When invoked, begin executing immediately — do NOT ask "What would you like me to do?".** Run the scan first, then present the metrics, then triage findings **one by one**, pausing for the user to confirm each fix (per user's working style).

## Workflow

### 1. Run the Scan

Framework 仓库（脚本封装好 token / projectKey / 覆盖率路径）：

```bash
bash scripts/sonar-scan.sh
```

业务项目（无该脚本，直接用 mvn 命令，按需替换 `projectKey`）：

```bash
mvn -B -ntp verify sonar:sonar \
  -Dsonar.login="$SONAR_TOKEN" \
  -Dsonar.host.url="$SONAR_HOST_URL" \
  -Dsonar.projectKey=<业务项目Key> \
  -Dsonar.projectName=<业务项目名> \
  -Dsonar.sources=src/main/java \
  -Dsonar.java.binaries=target/classes \
  -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
```

This runs `mvn verify` (all tests + JaCoCo report) then `sonar:sonar`, output ends with `ANALYSIS SUCCESSFUL` and the dashboard URL.

### 2. Fetch Metrics

```bash
TOKEN=squ_156b1e2938c4f5cac460156c4881ff06c6209d5e
curl -s "http://localhost:9000/api/measures/search?projectKeys=open-admin&metricKeys=bugs,vulnerabilities,code_smells,coverage&ps=20" -u "$TOKEN:" \
  | node -e "const d=JSON.parse(require('fs').readFileSync(0)); for(const m of d.measures) console.log(m.metric,'=',m.value)"
```

Wait ~10s after `ANALYSIS SUCCESSFUL` before querying (server-side processing).

### 3. List Unresolved Issues

Always filter `resolved=false` — default search **includes already-fixed (CLOSED) issues** and will confuse the counts:

```bash
curl -s "http://localhost:9000/api/issues/search?projectKeys=open-admin&types=BUG&resolved=false&ps=100" -u "$TOKEN:" \
  | node -e "const d=JSON.parse(require('fs').readFileSync(0)); console.log('total',d.total); const c={}; for(const i of d.issues) c[i.rule]=(c[i.rule]||0)+1; console.log(c)"
```

Also list the full details (rule / line / message) before fixing anything.

### 4. Triage & Fix One By One

**Follow the user's confirm-per-fix workflow**: for each finding, show the exact code + proposed diff, let the user confirm, then edit → test → commit → re-scan before moving to the next.

Guidance by finding type:

| Rule | Nature | Approach |
|---|---|---|
| S2095 (resource leak) | real | `try-with-resources` / `finally` |
| S2259 (NPE) | often real | null check + fail-fast; if it's a `.orElse(...)` false positive, use enhanced-for loop or `Objects.requireNonNull` — **not** `if (x == null) continue;` (triggers S135 in loops) |
| S5122 / S4507 | real | remove wildcard CORS header / replace `printStackTrace` with SLF4J |
| S6218 (record + array) | real | override `equals/hashCode/toString` with `Arrays.*`; prefer record pattern `instanceof PayloadFile(String a, byte[] b)` |
| S3077 (volatile lazy) | real | static holder singleton |
| S2184 (int overflow) | mostly theoretical | cast to `long`; explain honestly when it can't actually overflow |
| S3752 (no HTTP method) | mechanical | `@GetMapping`/`@PostMapping`; check the **frontend** call site method first |
| S4684 (entity as request body) | real, large | introduce request DTO whitelist, `BeanUtil.copyProperties(req, Entity.class)` in controller; services unchanged |
| S2077 / S4502 | confirmed-safe design | add `// NOSONAR` with the security rationale (identifier whitelist / SPA-CSRF-immune) |
| S1872 (test) | mechanical | `X.isAssignableFrom(cls)` |

Test commands after fixes:

```bash
mvn test -Dtest='<AffectedTestClass>'           # targeted
mvn test -Dtest='!*RepositoryTest,!*ServiceTest' # fast unit set
mvn test                                        # full 468-test suite before committing big changes
```

Commit style (matches repo):

```bash
fix(excel): ... (sonar S2095)
fix(npe): ... (sonar S2259)
```

### 5. Re-scan to Verify

Re-run `bash scripts/sonar-scan.sh`, then confirm with the **unresolved** counts (Step 2/3). Iterate until `Bugs=0`, `Vulnerabilities=0`, and `new_violations=0`.

## Quality Gate Notes

- The gate's `new_coverage` (threshold 80%) will almost always stay ERROR for this framework — that's expected, not a code problem; user decides whether to relax it in SonarQube config.
- `new_violations` counts only issues introduced since the leak period; clean it up to 0.

## Common Mistakes

- Forgetting `resolved=false` → counts include already-fixed issues
- Trusting `api/issues/search` totals while server is still indexing (wait ~10s; if counts disagree with `api/measures`, re-query)
- Fixing a "false positive" by adding a `continue` (S135) or dead null-check — use `Objects.requireNonNull` / enhanced-for / `NOSONAR` instead
- Changing `@RequestMapping` to `@GetMapping` without verifying the frontend actually calls GET
- Removing a `throws Exception` without checking the service methods it calls can still throw checked exceptions
- Running `sonar:sonar` without `verify` → no JaCoCo report → coverage stays 0

## Repository-Specific

- Framework 仓库内可用封装脚本 `scripts/sonar-scan.sh`（自定位、仅扫本仓库）；业务项目直接用上面的 mvn 命令
- Skill 随 `oa-crud`/`oa-upgrade` 一起打包进框架 JAR，并在业务项目启动时同步到其根目录 `.opencode/skills/`（修改 = 修改框架对外 API）；`oa-publishing-release` 才是不进 JAR 的仓库专属 skill
