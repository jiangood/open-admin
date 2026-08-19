# AGENTS.md

本仓库为 open-admin 框架本体（可嵌入后台管理系统，兼框架与示例应用两职）。开发时按需查阅：

- [README.md](README.md) — 项目概览 / 快速开始 / 开发命令与启动脚本 / Skills
- [docs/open-admin/guide.md](docs/open-admin/guide.md) — 架构设计 / 核心架构模式 / 添加业务模块 / FAQ
- [docs/open-admin/development.md](docs/open-admin/development.md) — 开发规范（命名 / REST API / 前后端要点）
- [docs/open-admin/api.md](docs/open-admin/api.md) — 后端 + 前端 API 参考
- [docs/open-admin/config.md](docs/open-admin/config.md) — 全部配置项

## 关键约定

- 改动前先分清作用域：框架源码 `src/main/java` + 前端框架 `web/src/**`；`docs/open-admin/` 与 `.opencode/skills/oa-*`（除 `oa-publishing-release`）会打进 release ZIP 并同步到业务项目，修改这些文件 = 修改框架对外 API
- 修改框架后需先 `mvn clean install -DskipTests`
- 新增业务模块六步流程见 guide.md「添加业务模块」