#!/usr/bin/env node
/**
 * 本地 SonarQube 扫描 — 跑全量测试（JaCoCo 覆盖率）+ sonar 分析，结果上传本地 SonarQube。
 *
 * 用法（Node 18+，跨平台 Windows/Linux/macOS）:
 *   node scripts/sonar-scan.js                      # 扫描当前仓库
 *   SONAR_HOST_URL=xxx SONAR_TOKEN=yyy node scripts/sonar-scan.js   # 或用环境变量覆盖
 *
 * 依赖: mvn（已配置）、本地 SonarQube（默认 http://localhost:9000）
 * 说明: 先执行 mvn verify（测试 + jacoco report 生成 target/site/jacoco/jacoco.xml），
 *       再执行 sonar:sonar 上传分析结果（后端 src/main/java + 前端 web/src），报告地址见输出末尾。
 */

const { spawnSync } = require('child_process');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const HOST = process.env.SONAR_HOST_URL || 'http://localhost:9000';
const TOKEN = process.env.SONAR_TOKEN || 'squ_156b1e2938c4f5cac460156c4881ff06c6209d5e';
const MVN = process.platform === 'win32' ? 'mvn.cmd' : 'mvn';

const args = [
  '-B', '-ntp',
  'verify',
  'sonar:sonar',
  `-Dsonar.login=${TOKEN}`,
  `-Dsonar.host.url=${HOST}`,
  '-Dsonar.projectKey=open-admin',
  '-Dsonar.projectName=open-admin',
  '-Dsonar.sources=src/main/java,web/src',
  '-Dsonar.java.binaries=target/classes',
  '-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml',
  '-Dsonar.exclusions=**/docs/**,**/test/**,web/dist/**,web/coverage/**,web/playwright-report/**',
];

const res = spawnSync(MVN, args, { cwd: ROOT, stdio: 'inherit' });
process.exit(res.status ?? 1);
