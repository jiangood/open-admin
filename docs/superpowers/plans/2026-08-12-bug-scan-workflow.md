# AI Bug Scan Workflow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建一个 `.github/workflows/bug-scan.yml` GitHub Actions，用 opencode 每日自动全量扫描 open-admin 代码中的逻辑 bug 与前后端交互 bug，AI 判断去重后以 GitHub Issue 产出。

**Architecture:** 单个 job，先 checkout 源码并安装 opencode，再拉取已有 open Issue 列表，然后 `opencode run --auto` 单次全扫（后端 `src/main/java` + 前端 `web/src`），把发现的 bug 写入 `findings.json`，最后由 bash 步骤解析 JSON 用 `gh` 创建 Issue（标题兜底查重），并上传报告 artifact。

**Tech Stack:** GitHub Actions (ubuntu-latest)、opencode CLI（`npm i -g opencode-ai`）、`gh` CLI、Python 3（解析 findings.json）、`jq`。

## Global Constraints

- 触发：`schedule` cron `0 3 * * *` + `workflow_dispatch`（输入项 `model`，默认 `opencode/deepseek-v4-flash-free`）
- `concurrency` 组 `ai-bug-scan`，`cancel-in-progress: false`
- `permissions`: `contents: read`、`issues: write`
- `timeout-minutes: 60`
- 扫描范围：后端 `src/main/java` + 前端 `web/src`
- 只找**逻辑 bug**与**前后端交互 bug**，排除优化/性能/风格/重构
- findings.json 每条记录字段：`title`、`severity`(high/medium/low)、`category`(logic/interaction)、`file`、`line`、`description`、`suggestion`
- AI 判断去重：先读 `existing-issues.json`，按问题本质判断是否与 open Issue 重复，重复不写入 findings.json
- 无 findings 或为空：正常结束 exit 0
- 单条 Issue 创建失败不中断整体
- 参考 `.github/workflows/upgrade-repos.yml` 的 opencode 安装与调用方式（`npm i -g opencode-ai`、`opencode run -m "$OPENCODE_MODEL" --auto`、`OPENCODE_MODEL` 环境变量）

---

### Task 1: 创建 workflow 扫描阶段（检出 + 安装 opencode + 拉取已有 Issue + opencode 全扫）

**Files:**
- Create: `.github/workflows/bug-scan.yml`

**Interfaces:**
- Consumes: 无
- Produces: 工作流文件，含触发/权限/concurrency 与扫描步骤；opencode 运行后产出 `findings.json`（Task 2 消费）

- [ ] **Step 1: 创建 `.github/workflows/bug-scan.yml`（扫描阶段）**

```yaml
name: AI Bug Scan

on:
  schedule:
    - cron: '0 3 * * *'
  workflow_dispatch:
    inputs:
      model:
        description: 'opencode 模型（默认免费模型，无需 API Key）'
        required: false
        type: string
        default: 'opencode/deepseek-v4-flash-free'

concurrency:
  group: ai-bug-scan
  cancel-in-progress: false

permissions:
  contents: read
  issues: write

jobs:
  bug-scan:
    name: AI Bug Scan
    runs-on: ubuntu-latest
    timeout-minutes: 60
    steps:
      - name: 检出源码
        uses: actions/checkout@v5
        with:
          fetch-depth: 1

      - name: 安装 opencode
        run: npm i -g opencode-ai

      - name: 获取已有 open Issue（供 AI 去重参考）
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          gh issue list --state open --label bug-scan --json number,title,body > existing-issues.json
          echo "已有 open bug-scan issues: $(jq length existing-issues.json)"

      - name: opencode 全量扫描（逻辑 bug + 前后端交互 bug）
        env:
          OPENCODE_MODEL: ${{ inputs.model || 'opencode/deepseek-v4-flash-free' }}
        run: |
          opencode run -m "$OPENCODE_MODEL" --auto "请对 open-admin 框架仓库做一次全面的 bug 扫描。重点找【逻辑 bug】与【前后端交互 bug】，不找优化、性能、代码风格、重构类问题。
          扫描范围：
          1) 后端 src/main/java 全部代码
          2) 前端 web/src 全部代码
          要找的问题：
          1. 逻辑 bug：空指针、事务边界、查询条件错误、状态流转、分页边界、权限校验、并发、数据一致性等
          2. 前后端交互 bug：URL 路径与方法不匹配、请求参数名/类型与后端 DTO 不一致、响应字段名与前端取值不一致、分页参数（pageNum/pageSize）约定不匹配、权限码（后端 @HasPermission 与前端 PermActions）不一致、字典值不一致等
          输出要求：
          1. 只报告高置信度的确凿 bug，宁缺毋滥；没有确凿 bug 就写入空数组 []
          2. 用 Write 工具把结果写入 findings.json，格式为 JSON 数组，每条记录字段：title(string)、severity(high|medium|low)、category(logic|interaction)、file(相对路径)、line(行号，尽量精确)、description(为什么是 bug)、suggestion(修复建议)
          3. 去重：先读取 existing-issues.json，对每个候选 bug 由 AI 按问题本质判断（而非仅标题文本）是否与已有 open issue 重复；重复的跳过，不写入 findings.json
          最后在回复中总结扫描结果。"
```

- [ ] **Step 2: 校验 YAML 语法**

Run:
```powershell
python -c "import yaml; d=yaml.safe_load(open(r'.github/workflows/bug-scan.yml', encoding='utf-8')); assert 'jobs' in d and 'on' in d; print('YAML OK')"
```
Expected: `YAML OK`

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/bug-scan.yml
git commit -m "ci: 新增 AI bug 扫描 workflow（扫描阶段）"
```

---

### Task 2: 添加 Issue 创建与报告上传步骤

**Files:**
- Modify: `.github/workflows/bug-scan.yml`（在扫描步骤后追加两步）

**Interfaces:**
- Consumes: Task 1 的 `findings.json`（opencode 产出）
- Produces: 完整可运行的 workflow；Issue 创建步骤用 `gh issue create`（标题兜底查重），并生成 `bug-scan-report.md`

- [ ] **Step 1: 在 `opencode 全量扫描` 步骤后追加 Issue 创建与上传步骤**

```yaml
      - name: 解析 findings.json 并创建 Issue
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          python3 - <<'PY'
          import json, subprocess, sys
          from pathlib import Path

          if not Path('findings.json').exists():
              print('findings.json 不存在，跳过')
              sys.exit(0)
          findings = json.loads(Path('findings.json').read_text(encoding='utf-8'))
          if not findings:
              print('findings.json 为空，跳过')
              sys.exit(0)

          created = 0
          skipped = 0
          for f in findings:
              title = str(f.get('title', '')).strip()
              if not title:
                  continue
              check = subprocess.run(
                  ['gh', 'issue', 'list', '--state', 'open', '--label', 'bug-scan', '--search', f'"{title}"', '--json', 'number', '--limit', '1'],
                  capture_output=True, text=True)
              if json.loads(check.stdout or '[]'):
                  print(f'重复跳过: {title}')
                  skipped += 1
                  continue
              body = (
                  f"**严重级别**: {f.get('severity', 'medium')}\n"
                  f"**类别**: {f.get('category', 'logic')}\n"
                  f"**位置**: {f.get('file', '?')}:{f.get('line', '?')}\n\n"
                  f"**问题描述**:\n{f.get('description', '')}\n\n"
                  f"**修复建议**:\n{f.get('suggestion', '')}\n\n"
                  "---\n*由 AI Bug Scan workflow 自动创建*"
              )
              r = subprocess.run(
                  ['gh', 'issue', 'create', '--label', 'bug-scan', '--title', title, '--body', body],
                  capture_output=True, text=True)
              if r.returncode == 0:
                  print(f'已创建: {r.stdout.strip()}')
                  created += 1
              else:
                  print(f'创建失败: {title}: {r.stderr.strip()}')

          report = Path('bug-scan-report.md')
          report.write_text(
              f"# AI Bug Scan 报告\n\n共发现 {len(findings)} 条，新建 {created} 条，重复跳过 {skipped} 条。\n\n"
              + "\n".join(
                  f"- [{f.get('severity','medium').upper()}] {f.get('title','')} (`{f.get('file','?')}:{f.get('line','?')}`)"
                  for f in findings
              ),
              encoding='utf-8'
          )
          print(f'共发现 {len(findings)} 条，新建 {created} 条，重复跳过 {skipped} 条')
          PY

      - name: 上传扫描报告
        uses: actions/upload-artifact@v4
        with:
          name: bug-scan-report
          path: |
            findings.json
            bug-scan-report.md
          if-no-files-found: warn
```

- [ ] **Step 2: 校验 YAML 语法**

Run:
```powershell
python -c "import yaml; d=yaml.safe_load(open(r'.github/workflows/bug-scan.yml', encoding='utf-8')); assert 'jobs' in d and 'on' in d; print('YAML OK')"
```
Expected: `YAML OK`

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/bug-scan.yml
git commit -m "ci: AI bug 扫描 workflow 增加 Issue 创建与报告上传"
```

---

### Task 3: 端到端验证

**Files:**
- 无源码修改（验证性任务）

**Interfaces:**
- Consumes: Task 2 的完整 workflow
- Produces: 验证结论

- [ ] **Step 1: 检查 workflow 中所有 `gh`/`opencode`/`python3`/`jq` 调用语法**

Run:
```powershell
python -c "import yaml; d=yaml.safe_load(open(r'.github/workflows/bug-scan.yml', encoding='utf-8')); print([s['name'] for s in d['jobs']['bug-scan']['steps']])"
```
Expected: 6 个步骤：`检出源码`、`安装 opencode`、`获取已有 open Issue`、`opencode 全量扫描`、`解析 findings.json 并创建 Issue`、`上传扫描报告`

- [ ] **Step 2: 校验内嵌 Python 脚本语法**

Workflow 中 `解析 findings.json 并创建 Issue` 步骤的 Python 脚本无法直接在 GH 外运行，但可提取后做语法编译检查。提取 heredoc 内容到临时文件并 `py_compile`：

Run:
```powershell
$content = Get-Content -Raw ".github/workflows/bug-scan.yml"
$m = [regex]::Match($content, "python3 - <<'PY'\r?\n(?<py>[\s\S]*?)\r?\n          PY")
if (-not $m.Success) { throw "未找到 python heredoc" }
$py = $m.Groups['py'].Value
Set-Content -Path "$env:TEMP\bugscan_script.py" -Value $py -Encoding UTF8
python -m py_compile "$env:TEMP\bugscan_script.py"
if ($?) { Write-Output "PYTHON OK" }
```
Expected: `PYTHON OK`（无语法错误）

- [ ] **Step 3: 在 GitHub 上手动触发 `workflow_dispatch` 验证**

在仓库 Actions → `AI Bug Scan` → `Run workflow`，观察：
- opencode 扫描成功并生成 `findings.json`
- 无新 bug 时 job 正常结束（exit 0）
- 有新 bug 时创建带 `bug-scan` 标签的 Issue
- artifact `bug-scan-report` 可下载
- 二次触发同标题 bug 不会重复创建

Expected: 上述各项全部通过

- [ ] **Step 4: 完成**

无提交（验证通过即收尾）。
