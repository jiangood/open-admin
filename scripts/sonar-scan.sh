#!/usr/bin/env bash
#
# 本地 SonarQube 扫描 — 跑全量测试（JaCoCo 覆盖率）+ sonar 分析，结果上传本地 SonarQube。
#
# 用法:
#   bash scripts/sonar-scan.sh                      # 扫描当前仓库
#   SONAR_HOST_URL=xxx SONAR_TOKEN=yyy bash scripts/sonar-scan.sh   # 或用环境变量覆盖
#
# 依赖: mvn（已配置）、本地 SonarQube（默认 http://localhost:9000）
# 说明: 先执行 mvn verify（测试 + jacoco report 生成 target/site/jacoco/jacoco.xml），
#       再执行 sonar:sonar 上传分析结果，报告地址见输出末尾。

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

SONAR_HOST_URL="${SONAR_HOST_URL:-http://localhost:9000}"
SONAR_TOKEN="${SONAR_TOKEN:-squ_156b1e2938c4f5cac460156c4881ff06c6209d5e}"

cd "$ROOT"

mvn -B -ntp \
  verify \
  sonar:sonar \
  -Dsonar.login="$SONAR_TOKEN" \
  -Dsonar.host.url="$SONAR_HOST_URL" \
  -Dsonar.projectKey=open-admin \
  -Dsonar.projectName=open-admin \
  -Dsonar.sources=src/main/java \
  -Dsonar.java.binaries=target/classes \
  -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
  -Dsonar.exclusions=**/docs/**,**/test/**
