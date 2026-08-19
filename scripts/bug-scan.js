/**
 * 本地 AI Bug Scan — 解析 opencode 扫描输出、去重并创建 GitHub Issue
 *
 * 由 scripts/bug-scan.sh 调用，也可单独运行：
 *   node scripts/bug-scan.js               # 解析 target/bug-scan/ 下产物并建 Issue
 *   node scripts/bug-scan.js --dry-run     # 只产出 findings.json，不建 Issue
 *
 * 依赖: gh（已登录）、node（无需 jq / python3，兼容 CentOS 7）
 */

const { execFileSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const OUT_DIR = path.join(ROOT, 'target', 'bug-scan');
const FINDINGS_PATH = path.join(OUT_DIR, 'findings.json');
const BACKEND_FINDINGS_PATH = path.join(OUT_DIR, 'backend-findings.json');
const FRONTEND_FINDINGS_PATH = path.join(OUT_DIR, 'frontend-findings.json');
const BACKEND_SCAN_PATH = path.join(OUT_DIR, 'backend-scan.jsonl');
const FRONTEND_SCAN_PATH = path.join(OUT_DIR, 'frontend-scan.jsonl');
const SCAN_OUTPUT_PATH = path.join(OUT_DIR, 'scan-output.jsonl');
const REPORT_PATH = path.join(OUT_DIR, 'bug-scan-report.md');
const DRY_RUN = process.argv.includes('--dry-run');

function stripBom(text) {
  return text.charCodeAt(0) === 0xfeff ? text.slice(1) : text;
}

function readJson(file) {
  try {
    const data = JSON.parse(stripBom(fs.readFileSync(file, 'utf8')));
    return Array.isArray(data) ? data : null;
  } catch (e) {
    console.warn(`⚠️  ${path.basename(file)} 解析失败: ${e.message}`);
    return null;
  }
}

/** 从 scan-output.jsonl 或 {backend,frontend}-scan.jsonl 中提取 JSON 数组 */
function extractFromOutput(scanPath) {
  let lines;
  try {
    lines = stripBom(fs.readFileSync(scanPath, 'utf8')).split(/\r?\n/);
  } catch {
    return null;
  }

  for (const rawLine of lines) {
    const line = rawLine.trim();
    if (!line.startsWith('[') && !line.startsWith('{')) continue;

    // Direct JSON array
    if (line.startsWith('[')) {
      try {
        const parsed = JSON.parse(line);
        if (Array.isArray(parsed)) return parsed;
      } catch { continue; }
    }

    // JSONL event
    if (line.startsWith('{')) {
      let event;
      try { event = JSON.parse(line); } catch { continue; }
      if (event.type !== 'text') continue;

      let stripped = ((event.part || {}).text || '').trim();
      if (!stripped) continue;

      if (stripped.startsWith('```')) {
        stripped = stripped.replace(/^`+|`+$/g, '');
        for (const prefix of ['json', 'JSON']) {
          if (stripped.startsWith(prefix)) {
            stripped = stripped.slice(prefix.length).replace(/^\s+/, '');
          }
        }
      }
      stripped = stripped.trim();

      if (stripped.startsWith('[') && stripped.endsWith(']')) {
        try {
          const parsed = JSON.parse(stripped);
          if (Array.isArray(parsed)) return parsed;
        } catch { continue; }
      }
    }
  }
  return null;
}

function gh(args) {
  return execFileSync('gh', args, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] }).trim();
}

function main() {
  console.log('📂 输出目录:', OUT_DIR);

  // ---------- 加载 findings（合并后端 + 前端分片） ----------
  let findings = [];

  // 优先读分片 findings（chunk-*-backend/frontend-findings.json，兼容单文件 backend/frontend-findings.json）
  const singleFindings = new Set([path.basename(BACKEND_FINDINGS_PATH), path.basename(FRONTEND_FINDINGS_PATH)]);
  const findingsFiles = fs.existsSync(OUT_DIR)
    ? fs
        .readdirSync(OUT_DIR)
        .filter((f) => /^chunk-\d+-(backend|frontend)-findings\.json$/.test(f) || singleFindings.has(f))
        .sort()
    : [];
  for (const ff of findingsFiles) {
    const arr = readJson(path.join(OUT_DIR, ff));
    if (arr) findings = findings.concat(arr);
  }

  // 兼容旧版：无分片时尝试 findings.json
  if (findings.length === 0 && fs.existsSync(FINDINGS_PATH)) {
    findings = findings.concat(readJson(FINDINGS_PATH) || []);
  }

  // 最后兜底：从 jsonl 提取
  if (findings.length === 0) {
    console.log('⚠️  findings 缺失或无效，尝试从 scan-output.jsonl 提取');
    for (const p of [BACKEND_SCAN_PATH, FRONTEND_SCAN_PATH, SCAN_OUTPUT_PATH]) {
      const extracted = extractFromOutput(p);
      if (extracted) findings = findings.concat(extracted);
    }
  }

  if (findings.length === 0) {
    console.log('未发现确凿 bug 或提取失败，结束');
    process.exit(0);
  }

  // 合并写入 findings.json 供后续使用
  fs.writeFileSync(FINDINGS_PATH, JSON.stringify(findings, null, 2), 'utf8');

  if (DRY_RUN) {
    console.log(`🚫 dry-run 模式：共发现 ${findings.length} 条，不创建 Issue`);
    for (const f of findings) {
      console.log(`   - [${f.severity || 'medium'}] ${f.title || ''} (\`${f.file || '?'}:${f.line || '?'}\`)`);
    }
    process.exit(0);
  }

  // ---------- 去重 + 建 Issue ----------
  let created = 0;
  let skipped = 0;
  const createdItems = [];
  const skippedItems = [];
  const failedItems = [];

  for (const f of findings) {
    const title = String(f.title || '').trim();
    if (!title) continue;

    const searchTitle = title.replace(/"/g, '');
    let check = '[]';
    try {
      check = gh(['issue', 'list', '--state', 'open', '--label', 'bug-scan', '--search', `"${searchTitle}"`, '--json', 'number', '--limit', '1']);
    } catch (e) {
      console.warn(`⚠️  去重检查失败，跳过: ${title} (${String(e.stderr || e).trim()})`);
    }
    if (JSON.parse(check || '[]').length > 0) {
      console.log(`重复跳过: ${title}`);
      skipped++;
      skippedItems.push(f);
      continue;
    }

    const body =
      `**严重级别**: ${f.severity || 'medium'}\n` +
      `**类别**: ${f.category || 'logic'}\n` +
      `**位置**: ${f.file || '?'}:${f.line || '?'}\n\n` +
      `**问题描述**:\n${f.description || ''}\n\n` +
      `**修复建议**:\n${f.suggestion || ''}\n\n` +
      '---\n*由 AI Bug Scan workflow 自动创建*';

    try {
      const url = gh(['issue', 'create', '--label', 'bug-scan', '--title', title, '--body', body]);
      console.log(`已创建: ${url}`);
      created++;
      createdItems.push(f);
    } catch (e) {
      console.error(`创建失败: ${title}: ${String(e.stderr || e).trim()}`);
      failedItems.push(f);
      continue;
    }
  }

  // ---------- 报告 ----------
  const reportLine = (f, status) =>
    `- [${(f.severity || 'medium').toUpperCase()}] ${f.title || ''} (\`${f.file || '?'}:${f.line || '?'}\`) — ${status}`;
  const reportLines = [
    ...createdItems.map((f) => reportLine(f, '已创建')),
    ...skippedItems.map((f) => reportLine(f, '重复跳过')),
    ...failedItems.map((f) => reportLine(f, '创建失败')),
  ];
  fs.writeFileSync(
    REPORT_PATH,
    `# AI Bug Scan 报告\n\n共发现 ${findings.length} 条，新建 ${created} 条，重复跳过 ${skipped} 条，创建失败 ${failedItems.length} 条。\n\n${reportLines.join('\n')}\n`,
    'utf8',
  );
  console.log(`共发现 ${findings.length} 条，新建 ${created} 条，重复跳过 ${skipped} 条，创建失败 ${failedItems.length} 条`);
}

main();
