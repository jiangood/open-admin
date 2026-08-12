#!/usr/bin/env bash
#
# 本地 AI Bug Scan — 在本地跑 opencode 全量扫描 open-admin 前后端代码，
# 解析扫描结果、去重后通过 gh 创建 bug-scan 标签的 GitHub Issue。
#
# 用法:
#   bash scripts/bug-scan.sh                       # 默认模型 opencode/deepseek-v4-flash-free
#   bash scripts/bug-scan.sh <模型>                # 指定模型
#   BUG_SCAN_MODEL=xxx bash scripts/bug-scan.sh    # 或用环境变量指定模型
#
# 依赖: gh（已登录）、node、opencode（缺失时提示安装）
# 产物: 全部落在 target/bug-scan/（已被 gitignore，不污染 git status）

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

OUT_DIR="target/bug-scan"
MODEL="${1:-${BUG_SCAN_MODEL:-opencode/deepseek-v4-flash-free}}"

# ---------------------------------------------------------------- 前置检查
for cmd in gh node opencode; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "❌ 缺少依赖: $cmd"
    if [ "$cmd" = "opencode" ]; then
      echo "   安装: npm i -g opencode-ai"
    fi
    exit 1
  fi
done

echo "🔍 前置检查通过: gh / node / opencode"
echo "🤖 扫描模型: $MODEL"
echo "📁 产物目录: $OUT_DIR"
mkdir -p "$OUT_DIR"

# ---------------------------------------------------------------- 创建标签（幂等）
echo "🏷️  确保 bug-scan 标签存在..."
gh label create bug-scan --description 'AI Bug Scan workflow 自动发现' --force >/dev/null 2>&1 || true

# ---------------------------------------------------------------- 拉取已有 Issue 供去重
echo "📋 拉取已有 open bug-scan issues..."
gh issue list --state open --label bug-scan --json number,title,body > "$OUT_DIR/existing-issues.json"
echo "  已有 open bug-scan issues: $(node -e 'const a=require("./target/bug-scan/existing-issues.json");console.log(Array.isArray(a)?a.length:0)')"

# ---------------------------------------------------------------- opencode 全量扫描
echo "🚀 开始全量扫描（逻辑 bug + 前后端交互 bug），可能耗时数分钟..."
set +e
opencode run -m "$MODEL" --auto --format json "$(cat <<'PROMPT'
请对 open-admin 框架仓库做一次全面的 bug 扫描。重点找【逻辑 bug】与【前后端交互 bug】，不找优化、性能、代码风格、重构类问题。
扫描范围：
1) 后端 src/main/java 全部代码
2) 前端 web/src 全部代码
要找的问题：
1. 逻辑 bug：空指针、事务边界、查询条件错误、状态流转、分页边界、权限校验、并发、数据一致性等
2. 前后端交互 bug：URL 路径与方法不匹配、请求参数名/类型与后端 DTO 不一致、响应字段名与前端取值不一致、分页参数（pageNum/pageSize）约定不匹配、权限码（后端 @HasPermission 与前端 PermActions）不一致、字典值不一致等
输出要求（严格遵守，这是自动化的核心契约）：
1. 只报告高置信度的确凿 bug，宁缺毋滥；没有确凿 bug 就输出空数组 []
2. 先用 Write 工具把结果写入 target/bug-scan/findings.json，格式为 JSON 数组（不要 Markdown 代码块包裹），每条记录字段：title(string)、severity(high|medium|low)、category(logic|interaction)、file(相对路径)、line(行号，尽量精确)、description(为什么是 bug)、suggestion(修复建议)
3. 去重：先读取 target/bug-scan/existing-issues.json，对每个候选 bug 由 AI 按问题本质判断（而非仅标题文本）是否与已有 open issue 重复；重复的跳过，不写入 findings.json
4. 最后一条消息必须是原始 JSON 数组本身（与 findings.json 内容完全一致，不要任何解释文字、不要 Markdown 代码块围栏）。如果无法调用 Write 工具，则仅把 JSON 数组作为最后一条消息输出即可。
PROMPT
)" 2>/dev/null | tee "$OUT_DIR/scan-output.jsonl"
PIPE_STATUS=${PIPESTATUS[0]}
set -e

if [ "$PIPE_STATUS" -ne 0 ]; then
  echo "❌ opencode 扫描失败（退出码 $PIPE_STATUS）"
  exit 1
fi

# ---------------------------------------------------------------- 解析 + 去重 + 建 Issue
echo "🧠 解析扫描结果并创建 Issue..."
node scripts/bug-scan.js

echo "✅ 扫描完成，报告见 $OUT_DIR/bug-scan-report.md"
