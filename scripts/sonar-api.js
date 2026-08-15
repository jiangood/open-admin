#!/usr/bin/env node
/**
 * SonarQube 查询辅助 — 封装 oa-sonar-scan skill 常用的只读查询，跨平台（Windows/Linux/macOS 均可用）。
 *
 * 用法（node 18+ 即可，无第三方依赖）:
 *   node scripts/sonar-api.js metrics                 # 总指标 bugs/vulns/smells/coverage
 *   node scripts/sonar-api.js newcode                 # New Code 指标（period）
 *   node scripts/sonar-api.js gate                    # Quality Gate 状态（含逐条件）
 *   node scripts/sonar-api.js bugs                    # 未解决 BUG（按规则统计 + 明细）
 *   node scripts/sonar-api.js vulns                   # 未解决 VULNERABILITY
 *   node scripts/sonar-api.js issues <rule>           # 指定规则未解决问题（如 typescript:S6756）
 *   node scripts/sonar-api.js issues-after <time>     # createdAfter 之后的问题（定位 new code 引入）
 *   node scripts/sonar-api.js history                 # 指标历史趋势（bugs/smells/coverage）
 *   node scripts/sonar-api.js analyses                # 分析历史（找 leak period 起点）
 *
 * 支持环境变量 SONAR_HOST_URL / SONAR_TOKEN / SONAR_PROJECT 覆盖。
 * 默认 projectKey=open-admin，token 与 scripts/sonar-scan.sh 一致。
 * 注意: issues-after 的 ISO 时间里的 "+" 需编码，如 2026-08-15T07:00:00%2B0000（本脚本也会自动处理）。
 */

const HOST = process.env.SONAR_HOST_URL || 'http://localhost:9000';
const TOKEN = process.env.SONAR_TOKEN || 'squ_156b1e2938c4f5cac460156c4881ff06c6209d5e';
const PROJECT = process.env.SONAR_PROJECT || 'open-admin';

const [,, cmd, arg] = process.argv;

async function get(path) {
  const url = `${HOST}${path}`;
  const res = await fetch(url, { headers: { Authorization: `Basic ${Buffer.from(`${TOKEN}:`).toString('base64')}` } });
  if (!res.ok) {
    console.error(`❌ HTTP ${res.status} ${url}`);
    process.exit(1);
  }
  return res.json();
}

function short(name) {
  return name.replace(`${PROJECT}:`, '');
}

const commands = {
  async metrics() {
    const d = await get(`/api/measures/search?projectKeys=${PROJECT}&metricKeys=bugs,vulnerabilities,code_smells,coverage,duplicated_lines_density&ps=20`);
    for (const m of d.measures) console.log(`${m.metric} = ${m.value}`);
  },

  async newcode() {
    const d = await get(`/api/measures/component?component=${PROJECT}&metricKeys=new_violations,new_bugs,new_vulnerabilities,new_code_smells,new_lines,new_coverage`);
    for (const m of d.component.measures) {
      console.log(`${m.metric} = ${m.period?.value ?? ''}${m.period?.bestValue === true ? ' (best)' : ''}`);
    }
  },

  async gate() {
    const d = await get(`/api/qualitygates/project_status?projectKey=${PROJECT}`);
    const s = d.projectStatus;
    console.log(`status: ${s.status}`);
    for (const c of s.conditions) {
      console.log(` - ${c.metricKey} actual=${c.actualValue} error=${c.errorThreshold} | ${c.status}`);
    }
  },

  bugs() { return this.issuesByType('BUG'); },
  vulns() { return this.issuesByType('VULNERABILITY'); },

  async issuesByType(type) {
    const d = await get(`/api/issues/search?projectKeys=${PROJECT}&types=${type}&resolved=false&ps=200`);
    console.log(`total ${d.total}`);
    const c = {};
    for (const i of d.issues) c[i.rule] = (c[i.rule] || 0) + 1;
    console.log(JSON.stringify(c));
    for (const i of d.issues) console.log(` - ${i.rule} | ${short(i.component)} : ${i.line}`);
  },

  async issues() {
    if (!arg) {
      console.error('用法: node scripts/sonar-api.js issues <rule>   (如 typescript:S6756)');
      process.exit(1);
    }
    const d = await get(`/api/issues/search?projectKeys=${PROJECT}&rules=${encodeURIComponent(arg)}&resolved=false&ps=200`);
    console.log(`total ${d.total}`);
    for (const i of d.issues) {
      console.log(` - ${i.rule} | ${short(i.component)} : ${i.line} | ${i.message.replace(/\n/g, ' ').slice(0, 100)}`);
    }
  },

  'issues-after': async function issuesAfter() {
    if (!arg) {
      console.error('用法: node scripts/sonar-api.js issues-after <ISO时间>   (如 2026-08-15T07:00:00+0000，+ 会被自动编码)');
      process.exit(1);
    }
    const after = encodeURIComponent(arg);
    const d = await get(`/api/issues/search?projectKeys=${PROJECT}&createdAfter=${after}&resolved=false&ps=200`);
    console.log(`total ${d.total}`);
    const c = {};
    for (const i of d.issues) c[i.rule] = (c[i.rule] || 0) + 1;
    console.log(JSON.stringify(c));
    for (const i of d.issues) {
      console.log(` - ${i.creationDate.slice(11, 19)} | ${i.rule} | ${short(i.component)} : ${i.line}`);
    }
  },

  async history() {
    const d = await get(`/api/measures/search_history?component=${PROJECT}&metrics=bugs,code_smells,coverage&ps=30`);
    for (const h of d.measures) {
      console.log(h.metric);
      for (const x of h.history.slice(-8)) console.log(`   ${x.date.slice(0, 16)} ${x.value}`);
    }
  },

  async analyses() {
    const d = await get(`/api/project_analyses/search?project=${PROJECT}&ps=10`);
    for (const a of d.analyses) {
      const v = a.events.find((e) => e.category === 'VERSION')?.name;
      console.log(`${a.date} | version: ${v || '-'}`);
    }
  },
};

(async () => {
  const fn = commands[cmd];
  if (!fn) {
    console.error('用法: node scripts/sonar-api.js <metrics|newcode|gate|bugs|vulns|issues|issues-after|history|analyses>');
    process.exit(1);
  }
  await fn.call(commands);
})().catch((e) => {
  console.error('❌', e.message);
  process.exit(1);
});
