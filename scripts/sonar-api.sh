#!/usr/bin/env bash
#
# SonarQube 查询辅助 — 封装 oa-sonar-scan skill 常用的只读查询，减少重复 curl+node。
#
# 用法:
#   bash scripts/sonar-api.sh metrics                 # 总指标 bugs/vulns/smells/coverage
#   bash scripts/sonar-api.sh newcode                 # New Code 指标（period）
#   bash scripts/sonar-api.sh gate                    # Quality Gate 状态（含逐条件）
#   bash scripts/sonar-api.sh bugs                    # 未解决 BUG（按规则统计 + 明细）
#   bash scripts/sonar-api.sh vulns                   # 未解决 VULNERABILITY
#   bash scripts/sonar-api.sh issues <rule>           # 指定规则未解决问题（如 S6756）
#   bash scripts/sonar-api.sh issues-after <time>     # createdAfter 指定时间之后的问题（定位 new code）
#   bash scripts/sonar-api.sh history                 # 指标历史趋势（bugs/smells/coverage）
#   bash scripts/sonar-api.sh analyses                # 分析历史（找 leak period 起点）
#
# 支持环境变量 SONAR_HOST_URL / SONAR_TOKEN / SONAR_PROJECT 覆盖。
# 默认 projectKey=open-admin，token 与 scripts/sonar-scan.sh 一致。

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SONAR_HOST_URL="${SONAR_HOST_URL:-http://localhost:9000}"
SONAR_TOKEN="${SONAR_TOKEN:-squ_156b1e2938c4f5cac460156c4881ff06c6209d5e}"
SONAR_PROJECT="${SONAR_PROJECT:-open-admin}"

node_one() {
  node -e "$1"
}

case "${1:-}" in
  metrics)
    curl -s "$SONAR_HOST_URL/api/measures/search?projectKeys=$SONAR_PROJECT&metricKeys=bugs,vulnerabilities,code_smells,coverage,duplicated_lines_density&ps=20" -u "$SONAR_TOKEN:" \
      | node_one "const d=JSON.parse(require('fs').readFileSync(0)); for(const m of d.measures) console.log(m.metric,'=',m.value);"
    ;;
  newcode)
    curl -s "$SONAR_HOST_URL/api/measures/component?component=$SONAR_PROJECT&metricKeys=new_violations,new_bugs,new_vulnerabilities,new_code_smells,new_lines,new_coverage" -u "$SONAR_TOKEN:" \
      | node_one "const d=JSON.parse(require('fs').readFileSync(0)); for(const m of d.component.measures) console.log(m.metric,'=',m.period?.value, m.period?.bestValue===true?'(best)':'');"
    ;;
  gate)
    curl -s "$SONAR_HOST_URL/api/qualitygates/project_status?projectKey=$SONAR_PROJECT" -u "$SONAR_TOKEN:" \
      | node_one "const s=JSON.parse(require('fs').readFileSync(0)).projectStatus; console.log('status:',s.status); for(const c of s.conditions) console.log(' -',c.metricKey,'actual='+c.actualValue,'error='+c.errorThreshold,'|',c.status);"
    ;;
  bugs|vulns)
    TYPE=$([ "$1" = "bugs" ] && echo BUG || echo VULNERABILITY)
    curl -s "$SONAR_HOST_URL/api/issues/search?projectKeys=$SONAR_PROJECT&types=$TYPE&resolved=false&ps=200" -u "$SONAR_TOKEN:" \
      | node_one "const d=JSON.parse(require('fs').readFileSync(0)); console.log('total',d.total); const c={}; for(const i of d.issues) c[i.rule]=(c[i.rule]||0)+1; console.log(c); for(const i of d.issues) console.log(' -',i.rule,'|',i.component.replace('$SONAR_PROJECT:',''),':',i.line);"
    ;;
  issues)
    RULE="${2:-}"
    if [ -z "$RULE" ]; then echo "用法: $0 issues <rule>" >&2; exit 1; fi
    curl -s "$SONAR_HOST_URL/api/issues/search?projectKeys=$SONAR_PROJECT&rules=$RULE&resolved=false&ps=200" -u "$SONAR_TOKEN:" \
      | node_one "const d=JSON.parse(require('fs').readFileSync(0)); console.log('total',d.total); for(const i of d.issues) console.log(' -',i.rule,'|',i.component.replace('$SONAR_PROJECT:',''),':',i.line,'|',i.message.replace(/\n/g,' ').slice(0,100));"
    ;;
  issues-after)
    AFTER="${2:-}"
    if [ -z "$AFTER" ]; then echo "用法: $0 issues-after <ISO时间>  (如 2026-08-15T07:00:00%2B0000)" >&2; exit 1; fi
    curl -s "$SONAR_HOST_URL/api/issues/search?projectKeys=$SONAR_PROJECT&createdAfter=$AFTER&resolved=false&ps=200" -u "$SONAR_TOKEN:" \
      | node_one "const d=JSON.parse(require('fs').readFileSync(0)); console.log('total',d.total); const c={}; for(const i of d.issues) c[i.rule]=(c[i.rule]||0)+1; console.log(c); for(const i of d.issues) console.log(' -',i.creationDate.slice(11,19),'|',i.rule,'|',i.component.replace('$SONAR_PROJECT:',''),':',i.line);"
    ;;
  history)
    curl -s "$SONAR_HOST_URL/api/measures/search_history?component=$SONAR_PROJECT&metrics=bugs,code_smells,coverage&ps=30" -u "$SONAR_TOKEN:" \
      | node_one "const d=JSON.parse(require('fs').readFileSync(0)); for(const h of d.measures){ console.log(h.metric); for(const x of h.history.slice(-8)) console.log('  ',x.date.slice(0,16),x.value); }"
    ;;
  analyses)
    curl -s "$SONAR_HOST_URL/api/project_analyses/search?project=$SONAR_PROJECT&ps=10" -u "$SONAR_TOKEN:" \
      | node_one "const d=JSON.parse(require('fs').readFileSync(0)); for(const a of d.analyses){ const v=a.events.find(e=>e.category==='VERSION')?.name; console.log(a.date,'| version:',v||'-'); }"
    ;;
  *)
    sed -n '1,18p' "$0" | grep '^#   bash'
    exit 1
    ;;
esac
