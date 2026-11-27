# AGENTS.md

本仓库为 open-admin 框架本体（可嵌入后台管理系统，兼框架与示例应用两职）。开发时按需查阅：

- [README.md](README.md) — 项目概览 / 快速开始 / 开发命令与启动脚本 / 发版 / Skills
- [docs/open-admin/guide.md](docs/open-admin/guide.md) — 架构设计 / 核心架构模式 / 添加业务模块 / FAQ
- [docs/open-admin/development.md](docs/open-admin/development.md) — 开发规范（命名 / REST API / 前后端要点）
- [docs/open-admin/api.md](docs/open-admin/api.md) — 后端 + 前端 API 参考
- [docs/open-admin/config.md](docs/open-admin/config.md) — 全部配置项

## 关键约定

- 改动前先分清作用域：框架源码 `src/main/java` + 前端框架 `web/src/**`；`docs/framework-sync.json` 清单声明的框架文件（当前含 `docs/open-admin/` 与 `.opencode/skills/oa-*`）会按 tag 归档同步到业务项目，修改这些文件/清单 = 修改框架对外同步内容
- 修改框架后需先 `mvn clean install -DskipTests`
- 新增业务模块六步流程见 guide.md「添加业务模块」
- **前端 UI 定位**：面向正常业务人员的**桌面端**后台，**不把无障碍（a11y）与移动端适配**作为设计/评审关注点；样式优先走 `--oa-*` CSS 变量与 antd token，禁止硬编码颜色（否则暗色主题割裂）

## 版本发布

项目提供了 release 脚本 `scripts/release.mjs`（跨平台，需 Node.js 18+），用于自动完成版本升级、构建验证、Git 提交打标签和推送。

### 前置条件

- 当前分支必须是 `main`
- 工作区干净（或只包含白名单文件 `*/pom.xml`、`web/package.json`）
- 工具：`node`（18+）、`git`、`mvn`、`npm`、`gh`（已登录）

### 发版额度（必读）

Maven Central 自 **2026-10-01** 起按组织统计每月发布额度，超限会影响发布：

- 每个自然月最多 **7 个 release**（组织级，可能包含多个 namespace）
- 同时限制文件数 **1,167 个/月**、发布体积 **78 MB/月**
- 超限进入 30 天宽限期；宽限期结束后，会拒绝继续推高该指标的新 release
- 正式发版前必须登录 [Usage Center](https://central.sonatype.com/publishing/usage) 确认当月用量
- 快照（`-SNAPSHOT`）发布**不计入**上述发布额度；只有正式 release 才占用 release 数、文件数和体积
- 不要只按本地 git tag 数判断：失败的发布也会留下 tag，但仍以 Usage Center 为准
- 同一自然月内合并改动，避免为小修复连续发版
- 发布失败不要重复打同一个版本 tag（Central 版本不可变），修好后用下一个版本号
- 紧急安全修复先联系 `central-support@sonatype.com`，不要直接硬发
- 仅需联调验证时优先用快照通道（`release.mjs --snapshot`），它与正式 release 的发布流程分开；快照不计入发布额度，可放心多次发布验证

官方说明：<https://central.sonatype.org/publish/maven-central-publishing-limits>

### 使用方式

```bash
node scripts/release.mjs <version> [options]      # 正式版 x.y.z（v 前缀可选）
node scripts/release.mjs <version> --snapshot     # 非正式版，见 README「非正式版（SNAPSHOT）」
```

完整参数、退出码、发布流程与排障见 [README「发版」](README.md#发版)（README 为发版权威文档）；脚本内置 `node scripts/release.mjs --help`。

关键约束（agent 必读）：

- 仅允许修改 `*/pom.xml` 与 `web/package.json`；改动其他文件（如 `AGENTS.md`、`docs/`、`.opencode/skills/`）先 `git add && git commit`
- 失败自动回滚版本号改动；commit + tag 已建、仅推送失败时重跑同一条命令进入 resume 模式；保留未提交版本变更加 `--no-rollback`
- 不要重复打同一个版本 tag（Central 版本不可变）
- 需要 `gh` 已登录、当前分支 `main`、工作区干净

> `docs/open-admin/`、`.opencode/skills/oa-*` 等框架同步文件由 `docs/framework-sync.json` 清单声明；`oa-upgrade-docs` skill 按清单从对应 tag 源码归档同步到业务项目。修改同步范围时需同步更新清单。