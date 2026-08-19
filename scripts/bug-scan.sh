#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
OUT_DIR="target/bug-scan"
MODEL="${1:-${BUG_SCAN_MODEL:-qwen3.8-27b}}"
# API 配置：优先环境变量，其次 ~/.config/open-admin.env（框架统一环境变量文件）
if [ -f "$HOME/.config/open-admin.env" ]; then
  . "$HOME/.config/open-admin.env"
fi
API_BASE="${BUG_SCAN_API_BASE:-http://10.207.114.33:3000/v1}"
if [ -z "${BUG_SCAN_API_KEY:-}" ]; then
  echo "❌ 未设置 BUG_SCAN_API_KEY"
  echo "   方式一: export BUG_SCAN_API_KEY=sk-xxx"
  echo "   方式二: 将 BUG_SCAN_API_KEY=sk-xxx 写入 ~/.config/open-admin.env"
  exit 1
fi
API_KEY="$BUG_SCAN_API_KEY"
for cmd in gh node curl; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "❌ 缺少依赖: $cmd"; exit 1
  fi
done
echo "🔍 前置检查通过: gh / node / curl"
echo "🤖 扫描模型: $MODEL"
echo "📁 产物目录: $OUT_DIR"
mkdir -p "$OUT_DIR"
gh label create bug-scan --description 'AI Bug Scan workflow 自动发现' --force >/dev/null 2>&1 || true
echo "📋 拉取已有 open bug-scan issues..."
gh issue list --state open --label bug-scan --json number,title,body > "$OUT_DIR/existing-issues.json"
echo "  已有 open bug-scan issues: $(node -e 'const a=require("./target/bug-scan/existing-issues.json");console.log(Array.isArray(a)?a.length:0)')"

# ---------------------------------------------------------------- 分片定义
# 每个分片: "目录1,目录2,..." — 总字符数 < 84K
# 后端分片
BACKEND_CHUNKS=(
  "framework/config,framework/common,framework/enums,framework/file"
  "framework/validator,framework/data,framework/dict"
  "framework/auth,framework/log,framework/perm,framework/ratelimit,framework/spi"
  "modules/system/service"
  "modules/system/controller,modules/system/dto"
  "modules/system/entity,modules/system/repository,modules/system/file,modules/system/job,modules/system/enums,modules/system/provider"
  "modules/job"
  "util/excel,util/dto,util/tree"
  "util/datetime,util/range,util/annotation,util/field"
  "util/Assert.java,util/BusinessException.java,util/ClassTool.java,util/ConvertTool.java,util/DurationTool.java,util/FileTool.java,util/FileTypeTool.java,util/FontTool.java,util/FriendlyTool.java,util/GoogleTool.java,util/HttpServletTool.java,util/IdTool.java,util/IpTool.java,util/MathTool.java,util/MapTool.java,util/NumberTool.java,util/PasswordTool.java,util/ReflectTool.java,util/ReflectionTool.java,util/RuntimeTool.java,util/ThreadTool.java"
  "util/ArrayTool.java,util/AmtTool.java,util/BeanTool.java,util/ContentTypeTool.java,util/DownloadTool.java,util/ExceptionToMessageTool.java,util/JsonTool.java,util/PageTool.java,util/RequestTool.java,util/ResponseTool.java,util/ResourceTool.java,util/SpringTool.java,util/StringTool.java,util/URLTool.java"
)

# 前端分片
FRONTEND_CHUNKS=(
  "framework/fields,framework/config,framework/biz"
  "framework/components,framework/utils"
  "framework/router,framework/views"
  "pages"
)

SCAN_DIR="src/main/java/io/github/jiangood/openadmin"
WEB_DIR="web/src"
MAX_CHARS=84000
MAX_TOKENS="${BUG_SCAN_MAX_TOKENS:-16000}"

# ---------------------------------------------------------------- 分片内容构建
# 递归收集目录下全部代码，按 84K 上限整文件跳过（避免截断产生半截代码）
# 用法: build_content <base_dir> <java|web> <目录或文件...>
build_content() {
  local base="$1" lang="$2"
  shift 2
  local content="" file_block full_path rel f limit="$MAX_CHARS"
  for d in "$@"; do
    full_path="$base/$d"
    if [ -d "$full_path" ]; then
      while IFS= read -r -d '' f; do
        case "$lang" in
          java) [ "${f##*.}" = "java" ] || continue ;;
          web)  case "$f" in *.ts|*.tsx|*.js|*.jsx) ;; *) continue ;; esac ;;
        esac
        rel="${f#$ROOT/}"
        file_block="--- $rel ---
$(cat "$f")
"
        if [ $(( ${#content} + ${#file_block} )) -gt "$limit" ]; then
          echo "    ⚠️  超过 ${limit} 字符上限，跳过剩余文件..." >&2
          return 0
        fi
        content+="$file_block"
      done < <(find "$full_path" -type f -print0)
    elif [ -f "$full_path" ]; then
      file_block="--- $full_path ---
$(cat "$full_path")
"
      if [ $(( ${#content} + ${#file_block} )) -gt "$limit" ]; then
        echo "    ⚠️  超过 ${limit} 字符上限，跳过该文件..." >&2
      else
        content+="$file_block"
      fi
    fi
  done
  printf '%s' "$content"
}

# ---------------------------------------------------------------- API 调用函数
call_api() {
  local chunk_idx="$1"
  local chunk_name="$2"
  local files_content="$3"
  local category="$4"
  local prompt="$5"
  local out_file="$OUT_DIR/chunk-${chunk_idx}-${chunk_name}-findings.json"

  local payload
  payload=$(node -e '
    const content = process.argv[1];
    const model = process.argv[2];
    const prompt = process.argv[3];
    const maxTokens = parseInt(process.argv[4], 10) || 16000;
    const body = JSON.stringify({
      model: model,
      messages: [
        { role: "system", content: "You are a senior code reviewer. Find real bugs only. High confidence only. Output pure JSON array." },
        { role: "user", content: prompt + "\n\n## 代码内容\n" + content }
      ],
      temperature: 0,
      max_tokens: maxTokens
    });
    console.log(body);
  ' -- "$files_content" "$MODEL" "$prompt" "$MAX_TOKENS")

  echo "  📡 调用 API..." >&2
  local response
  response=$(curl -s -w "\n%{http_code}" \
    -X POST "$API_BASE/chat/completions" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $API_KEY" \
    -d "$payload" 2>/dev/null) || { echo "  ❌ API 调用失败" >&2; return 1; }

  local http_code
  http_code=$(echo "$response" | tail -1)
  local body
  body=$(echo "$response" | sed '$d')

  if [ "$http_code" != "200" ]; then
    echo "  ❌ HTTP $http_code" >&2
    echo "$body" > "$OUT_DIR/chunk-${chunk_idx}-error.txt"
    return 1
  fi

  # 提取 assistant 消息中的 JSON
  node -e '
    const data = JSON.parse(process.argv[1]);
    const category = process.argv[2];
    const outFile = process.argv[3];
    const msg = (data.choices && data.choices[0] && data.choices[0].message) || {};
    let text = (msg.content || "").trim();
    const fence = String.fromCharCode(96).repeat(3);
    if (text.startsWith(fence)) {
      const lines = text.split("\n");
      lines.shift();
      if (lines[lines.length - 1] === fence) lines.pop();
      text = lines.join("\n");
    }
    try {
      const arr = JSON.parse(text);
      if (!Array.isArray(arr)) throw new Error("not array");
      arr.forEach(f => { if (!f.category) f.category = category; });
      require("fs").writeFileSync(outFile, JSON.stringify(arr, null, 2), "utf8");
      console.log(arr.length);
    } catch (e) {
      console.warn("解析失败: " + e.message);
      require("fs").writeFileSync(outFile, "[]", "utf8");
      console.log(0);
    }
  ' -- "$body" "$category" "$out_file"
}

# ---------------------------------------------------------------- 后端扫描
BACKEND_PROMPT='
# 任务：扫描以下 Java 代码中的逻辑 bug

## 范围（只找这些）
- 空指针（NPE）风险：未判空即调用
- 事务边界错误：@Transactional 误用/缺失
- 查询条件错误：Spec 拼接错误、WHERE 条件遗漏
- 状态流转错误：状态机不一致
- 分页边界：越界、排序错误
- 权限校验缺失：@HasPermission 遗漏
- 并发问题：竞态条件
- 数据一致性：级联、软删除遗漏

## 质量要求
- 宁缺毋滥，仅输出确凿 bug（高置信度），不确定则跳过
- 每个 bug 必须包含准确的 file 和 line

## 输出格式
输出纯 JSON 数组（不要 markdown 代码块包裹）：
[{"title":"标题","severity":"high|medium|low","file":"相对路径","line":行号,"description":"描述","suggestion":"建议"}]
无 bug 时输出 []
'

echo "🚀 开始扫描后端..."
idx=0
for chunk in "${BACKEND_CHUNKS[@]}"; do
  idx=$((idx + 1))
  IFS=',' read -ra dirs <<< "$chunk"
  echo "  [后端 $idx/${#BACKEND_CHUNKS[@]}] ${dirs[*]}"

  content=$(build_content "$SCAN_DIR" "java" "${dirs[@]}")
  echo "    字符数: ${#content}"

  count=$(call_api "$idx" "backend" "$content" "logic" "$BACKEND_PROMPT") || true
  echo "    发现: ${count:-0} 条"
done
# ---------------------------------------------------------------- 前端扫描
FRONTEND_PROMPT='
# 任务：扫描以下前端代码中的交互 bug

## 范围（只找这些）
- URL 路径/方法不匹配：前端请求路径与后端 @RequestMapping 不一致
- 参数名/类型不匹配：前端请求参数与后端 DTO 字段不一致
- 响应字段名不匹配：前端取值与后端返回字段不一致
- 分页参数约定不匹配：page/size 命名不一致
- 权限码不一致：@HasPermission 与前端 PermActions 不匹配
- 字典值不一致：@DictItem 枚举值与前端硬编码不一致

## 质量要求
- 宁缺毋滥，仅输出确凿 bug（高置信度），不确定则跳过
- 每个 bug 必须包含准确的 file 和 line

## 输出格式
输出纯 JSON 数组（不要 markdown 代码块包裹）：
[{"title":"标题","severity":"high|medium|low","file":"相对路径","line":行号,"description":"描述","suggestion":"建议"}]
无 bug 时输出 []
'

echo "🚀 开始扫描前端..."
idx=0
for chunk in "${FRONTEND_CHUNKS[@]}"; do
  idx=$((idx + 1))
  IFS=',' read -ra dirs <<< "$chunk"
  echo "  [前端 $idx/${#FRONTEND_CHUNKS[@]}] ${dirs[*]}"

  content=$(build_content "$WEB_DIR" "web" "${dirs[@]}")
  echo "    字符数: ${#content}"

  count=$(call_api "$idx" "frontend" "$content" "interaction" "$FRONTEND_PROMPT") || true
  echo "    发现: ${count:-0} 条"
done

# ---------------------------------------------------------------- 合并结果
echo "📊 合并扫描结果..."
node scripts/bug-scan.js "$OUT_DIR" "$MODEL"

echo "✅ 扫描完成！产物在 $OUT_DIR/"
echo "   查看报告: target/bug-scan/bug-scan-report.md"
