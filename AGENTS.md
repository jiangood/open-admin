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
- **前端 UI 定位**：面向正常业务人员的**桌面端**后台，**不把无障碍（a11y）与移动端适配**作为设计/评审关注点；样式优先走 `--oa-*` CSS 变量与 antd token，禁止硬编码颜色（否则暗色主题割裂）

## 版本发布

项目提供了 release 脚本 `scripts/release.py`（跨平台，需 Python 3），用于自动完成版本升级、构建验证、Git 提交打标签和推送。

### 前置条件

- 当前分支必须是 `main`
- 工作区干净（或只包含白名单文件 `*/pom.xml`、`web/package.json`）
- 工具：`python`、`git`、`node`、`mvn`、`npm`、`gh`（已登录）

### 使用方式

```bash
python scripts/release.py <version> [options]
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

- `python scripts/release.py status` - 查看当前版本、tag 和工作区状态
- `python scripts/release.py check` - 输出 key=value 格式的发布信息（可用于自动化）

### 发布流程

脚本自动执行：

1. 预检（分支、工具、Git 状态）
2. 检查版本和 tag 冲突
3. 使用 `scripts/bump-version.py` 更新所有 `pom.xml`（根 POM 和子模块）及 `web/package.json`
4. 验证版本是否正确更新
5. 执行后端测试和前端构建（可跳过）
6. 白名单检查（仅允许修改版本文件）
7. Git 提交（`release: v<version>`）并创建带注释的 tag（`v<version>`）
8. 推送到 origin（`main` 分支和 tag）

> 注意：`docs/open-admin/` 和 `.opencode/skills/oa-*` 会打包进 GitHub Release 的 `framework-files.zip`，由 release 工作流在 tag 推送后自动处理。

### 示例

```bash
# 预览发布 3.1.3
python scripts/release.py 3.1.3 --dry-run

# 发布 3.1.3（跳过测试）
python scripts/release.py 3.1.3 --skip-tests
```

### 常见问题

- **工作区不干净**：release 脚本只允许修改 `*/pom.xml` 和 `web/package.json`。若改动了其他文件（如 `AGENTS.md`、`docs/`、`.opencode/skills/`），请先 `git add && git commit` 再发布。
- **gh 未登录**：运行 `gh auth login` 后再发布。
- **push 失败自动回滚**：默认情况下，push 失败会自动回滚版本变更、提交和 tag。若要保留本地变更，请加 `--no-rollback`。
- **使用了 --no-push**：本地已提交和打 tag，需手动执行 `git push origin main && git push origin v<version>` 完成发布。
- **发布后校验**：
  - `npm view @jiangood/open-admin version` - 检查 npm 最新版本
  - 查看 Maven 最新版本：`https://repo1.maven.org/maven2/io/github/jiangood/open-admin/maven-metadata.xml`
  - `gh release view v<version>` - 检查 GitHub Release
- **CI 状态**：`gh run list --limit 5 --repo jiangood/open-admin` - 查看 CI 流水线状态