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

/** 从 scan-output.jsonl 中提取最后一条消息里的 JSON 数组（复刻原 python extract_from_output 逻辑） */
function extractFromOutput() {
  let lines;
  try {
    lines = stripBom(fs.readFileSync(SCAN_OUTPUT_PATH, 'utf8')).split(/\r?\n/);
  } catch {
    return null;
  }

  for (const rawLine of lines) {
    const line = rawLine.trim();
    if (!line.startsWith('{')) continue;

    let event;
    try {
      event = JSON.parse(line);
    } catch {
      continue;
    }
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
      } catch {
        continue;
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

  // ---------- 加载 findings ----------
  let findings = fs.existsSync(FINDINGS_PATH) ? readJson(FINDINGS_PATH) : null;
  if (findings === null) {
    console.log('⚠️  findings.json 缺失或无效，尝试从 scan-output.jsonl 提取');
    findings = extractFromOutput();
  }
  if (!findings || findings.length === 0) {
    console.log('未发现确凿 bug 或提取失败，结束');
    process.exit(0);
  }

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
  const reportItems = [];

  for (const f of findings) {
    const title = String(f.title || '').trim();
    if (!title) continue;

    const check = gh(['issue', 'list', '--state', 'open', '--label', 'bug-scan', '--search', `"${title}"`, '--json', 'number', '--limit', '1']);
    if (JSON.parse(check || '[]').length > 0) {
      console.log(`重复跳过: ${title}`);
      skipped++;
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
    } catch (e) {
      console.error(`创建失败: ${title}: ${String(e.stderr || e).trim()}`);
      continue;
    }
    reportItems.push(f);
  }

  // ---------- 报告 ----------
  const reportLines = reportItems.map(
    (f) => `- [${(f.severity || 'medium').toUpperCase()}] ${f.title || ''} (\`${f.file || '?'}:${f.line || '?'}\`)`,
  );
  fs.writeFileSync(
    REPORT_PATH,
    `# AI Bug Scan 报告\n\n共发现 ${findings.length} 条，新建 ${created} 条，重复跳过 ${skipped} 条。\n\n${reportLines.join('\n')}\n`,
    'utf8',
  );
  console.log(`共发现 ${findings.length} 条，新建 ${created} 条，重复跳过 ${skipped} 条`);
}

main();
