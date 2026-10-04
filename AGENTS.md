# AGENTS.md

本仓库为 open-admin 框架本体（可嵌入后台管理系统，兼框架与示例应用两职）。开发时按需查阅：

- [README.md](README.md) — 项目概览 / 快速开始 / 开发命令与启动脚本 / 发版 / Skills
- [docs/open-admin/guide.md](docs/open-admin/guide.md) — 架构设计 / 核心架构模式 / 添加业务模块 / FAQ
- [docs/open-admin/development.md](docs/open-admin/development.md) — 开发规范（命名 / REST API / 前后端要点）
- [docs/open-admin/api.md](docs/open-admin/api.md) — 后端 + 前端 API 参考
- [docs/open-admin/config.md](docs/open-admin/config.md) — 全部配置项

## 关键约定

- 改动前先分清作用域：框架源码 `src/main/java` + 前端框架 `web/src/**`；`docs/open-admin/` 与 `.opencode/skills/oa-*` 会打进 release ZIP 并同步到业务项目，修改这些文件 = 修改框架对外 API
- 修改框架后需先 `mvn clean install -DskipTests`
- 新增业务模块六步流程见 guide.md「添加业务模块」

## 版本发布

项目提供了 release 脚本 `scripts/release.bat`（Windows），用于自动完成版本升级、构建验证、Git 提交打标签和推送。

### 前置条件

- 当前分支必须是 `main`
- 工作区干净（或只包含白名单文件 `*/pom.xml`、`web/package.json`）
- 工具：`git`、`node`、`mvn`、`npm`、`gh`（已登录）

### 使用方式

```cmd
scripts\release.bat <version> [options]
```

版本号格式为 `x.y.z`，前缀 `v` 可选，最终 tag 会统一为 `v<version>`。

常用选项：

| 选项 | 说明 |
|---|---|
| `--skip-tests` | 跳过 `mvn -B clean test` 和 `web` 目录的 `npm run build` |
| `--no-push` | 只在本地提交和打 tag，不推送到 origin |
| `--dry-run` | 预览执行流程，不修改任何文件或 Git 状态 |
| `--no-rollback` | 失败时保留版本变更 |

其他命令：

- `scripts\release.bat status` - 查看当前版本、tag 和工作区状态
- `scripts\release.bat check` - 输出 key=value 格式的发布信息（可用于自动化）

### 发布流程

脚本自动执行：

1. 预检（分支、工具、Git 状态）
2. 检查版本和 tag 冲突
3. 使用 `scripts/bump-version.js` 更新所有 `pom.xml`（根 POM 和子模块）及 `web/package.json`
4. 验证版本是否正确更新
5. 执行后端测试和前端构建（可跳过）
6. 白名单检查（仅允许修改版本文件）
7. Git 提交（`release: v<version>`）并创建带注释的 tag（`v<version>`）
8. 推送到 origin（`main` 分支和 tag）

> 注意：`docs/open-admin/` 和 `.opencode/skills/oa-*` 会打包进 GitHub Release 的 `framework-files.zip`，由 release 工作流在 tag 推送后自动处理。

### 示例

```cmd
# 预览发布 3.1.3
scripts\release.bat 3.1.3 --dry-run

# 发布 3.1.3（跳过测试）
scripts\release.bat 3.1.3 --skip-tests
```