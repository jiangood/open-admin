@echo off
rem 本地 SonarQube 扫描 — 跑全量测试（JaCoCo 覆盖率）+ sonar 分析，结果上传本地 SonarQube。
rem 用法: scripts\sonar-scan.bat                     扫描当前仓库
rem       set SONAR_HOST_URL=xxx && set SONAR_TOKEN=yyy && scripts\sonar-scan.bat   或用环境变量覆盖
rem 依赖: mvn（已配置）、本地 SonarQube（默认 http://localhost:9000）
rem 说明: 先执行 mvn verify（测试 + jacoco report 生成 target/site/jacoco/jacoco.xml），
rem       再执行 sonar:sonar 上传分析结果（后端 src/main/java + 前端 web/src），报告地址见输出末尾。
setlocal
cd /d "%~dp0.."

set "HOST=%SONAR_HOST_URL%"
if "%HOST%"=="" set "HOST=http://localhost:9000"
set "TOKEN=%SONAR_TOKEN%"
if "%TOKEN%"=="" set "TOKEN=squ_156b1e2938c4f5cac460156c4881ff06c6209d5e"

mvn -B -ntp verify sonar:sonar "-Dsonar.login=%TOKEN%" "-Dsonar.host.url=%HOST%" -Dsonar.projectKey=open-admin -Dsonar.projectName=open-admin -Dsonar.sources=src/main/java,web/src -Dsonar.java.binaries=target/classes -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml "-Dsonar.exclusions=**/docs/**,**/test/**,web/dist/**,web/coverage/**,web/playwright-report/**"
