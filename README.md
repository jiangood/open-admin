# open-admin

![Maven Central](https://img.shields.io/maven-central/v/io.github.jiangood/open-admin)
![npm](https://img.shields.io/npm/v/@jiangood/open-admin)

open-admin 是一个后台管理系统框架（脚手架），**业务项目无需从零搭建用户管理、角色权限、数据字典等功能。

## 快速集成

```xml
<dependency>
    <groupId>io.github.jiangood</groupId>
    <artifactId>open-admin</artifactId>
    <version>${open-admin.version}</version>
</dependency>
```

```json
{
  "dependencies": { "@jiangood/open-admin": "版本" }
}
```

添加依赖后，用户管理、角色权限、数据字典、Quartz 调度、文件管理、代码生成等功能开箱即用。框架的 skills 与文档通过 `oa-upgrade-docs` skill 从 GitHub Release 同步到项目根目录（详见 [Skills (opencode)](#skills-opencode)）。

## 快速开始

### 环境要求

- **JDK 21+** / **Node.js 18+**（数据库内置 H2，无需安装；可选 MySQL 8.0+）

### 后端启动

```bash
git clone https://github.com/jiangood/open-admin.git
cd open-admin
mvn clean compile
mvn -Pdev spring-boot:run   # 开发模式启动（内置 H2，首次启动自动建表 + 初始化数据）
```

数据库文件默认在 `/data/db/open-admin`（本地开发可用 `-Ddb_path=./data/db/open-admin` 覆盖）；
切换 MySQL 见 `application-mysql.yml`：`mvn -Pdev spring-boot:run -Dspring-boot.run.profiles=mysql`。

### 前端启动

```bash
cd web
npm install
npm run dev                    # 默认 http://localhost:3000
```

### 默认登录

| 账号 | 密码 |
|------|------|
| admin | Open@1234 |

### Docker 一键部署

镜像内置 H2 数据库，无需外部 MySQL，数据、日志、上传文件持久化在 `/data`：

```bash
# docker compose（首次自动构建镜像）
docker compose -f docker-compose/docker-compose.yml up -d --build

# 或手动构建 / 运行
docker build -t open-admin .
docker run -d --name open-admin -p 8080:8080 -v ./data:/data open-admin
```

### 集成到已有项目

**后端**：业务项目 `pom.xml` 添加依赖，`application.yml` 配置数据源，通过 `spring.config.import: classpath:application-lib.yml` 引入框架默认配置。

**前端**：按以下清单配置（完整可运行示例见 [open-admin-example](https://github.com/jiangood/open-admin-example)）：

1. `package.json` 添加 `@jiangood/open-admin` 及 peer 依赖（react / react-dom / antd / @ant-design/icons / axios / dayjs / lodash / qs）
2. `vite.config.js`：
   - 注册插件 `openAdmin()`（来自 `@jiangood/open-admin/vite-plugin`，负责扫描 `src/pages` 生成路由）

3. `.env` 配置 `VITE_SERVER_SERVLET_CONTEXT_PATH`（必须与后端 `server.servlet.context-path` 一致）
4. 入口 `main.jsx` 引入虚拟路由表并渲染布局：

```jsx
import routes from 'virtual:open-admin/routes';
import {registerRoutes, PageLoading, Layouts} from '@jiangood/open-admin';

registerRoutes(routes);
createRoot(document.getElementById('root')).render(
    <React.Suspense fallback={<PageLoading/>}><Layouts/></React.Suspense>
);
```

**页面约定**（vite-plugin 扫描规则）：

- 页面文件放在 `src/pages/` 下，扩展名 `.jsx` 或 `.tsx`，文件名首字母小写（大写开头视为普通组件不注册路由）
- `src/pages/product/index.jsx` → 路由 `/product`；`$code.jsx` → 动态段 `/:code`
- 业务页面与框架页面路由冲突时业务页面优先（可覆盖框架页面）
- 页面组件可实现 `onShow()` 方法，在首次加载或 Tab 切换激活时自动调用（详见[页面生命周期](docs/open-admin/api.md#页面生命周期)）

**目录约定**（无需配置，自动识别）：`src/pages/` 下按目录区分页面类型——`pages/` 后台页（需登录 + 后台布局）、`pages/public/` 免登录无布局（如登录页）、`pages/standalone/` 需登录无布局（如强制改密页），详见 [development.md](docs/open-admin/development.md#页面目录约定)。


## 文档

| 文档 | 内容 |
|------|------|
| [docs/open-admin/guide.md](docs/open-admin/guide.md) | 架构设计 / 核心功能 / 添加业务模块 / 内置模块 / FAQ |
| [docs/open-admin/api.md](docs/open-admin/api.md) | 后端（Spec/注解/工具类/定时任务）+ 前端（组件/生命周期/字段组件/文件上传/工具类）API 参考 |
| [docs/open-admin/config.md](docs/open-admin/config.md) | 全部 `sys.*` 配置 / 文件存储 / 未认领文件清理 / context-path / 主题定制 |
| [docs/open-admin/development.md](docs/open-admin/development.md) | 后端命名 / REST API 规范 / 前后端开发要点 |

## 开发（框架本仓库）

### 双项目工作流

```
D:/ws/
├── open-admin/              # 框架项目（本仓库）
│   ├── src/main/java/
│   ├── web/src/framework/   # 前端框架源码 (npm publish)
│   └── pom.xml
└── open-admin-example/      # 示例业务项目（依赖框架）
```

修改框架后需先执行 `mvn clean install -DskipTests`。

### 开发命令

```bash
mvn clean compile                                          # 编译
mvn test -Dtest=BeanToolTest                               # 运行单个测试
mvn test -Dtest='!*RepositoryTest,!*ServiceTest'           # 仅纯单元测试，跳过 SpringBootTest 集成测试（更快）
mvn clean package                                          # 打包
mvn -Pdev spring-boot:run                                  # 独立应用启动
mvn clean install -DskipTests                              # 安装到本地仓库
node scripts/bump-version.js <新版本号>                     # 仅升级 pom.xml + web/package.json 版本号
scripts\release.bat <新版本号>                              # 一键发版：bump + 全量测试 + commit + tag + push（见「发版」）
cd web && npm install                                         # 前端安装依赖
cd web && npm run dev                                         # 前端开发模式
cd web && npm run build                                       # 前端构建
cd web && npm run test:e2e                                    # Playwright 端到端测试
```

测试使用 H2 内存数据库，无需 MySQL。RepositoryTest 和 ServiceTest 等集成测试同样使用 H2，可通过 `mvn test -Dtest='!*RepositoryTest,!*ServiceTest'` 跳过以加速。

E2E（`web/e2e/`）自动拉起后端（`mvn spring-boot:run` profiles=lib,e2e，端口 8080）与前端（端口 3000），运行前需释放这两个端口。

### 启动脚本

日常开发优先用 `scripts/` 下的脚本（后台 nohup 运行，日志落 `logs/`，PID 在 `logs/*.pid`，`logs/` 已被 gitignore）：

```bash
scripts/start-all.sh                                    # 一键后台启动前后端
scripts/start-backend.sh {start|stop|restart|status}    # 后端: mvn -Pdev spring-boot:run（devtools 热重载）
scripts/start-frontend.sh {start|stop|restart|status}   # 前端: npm run dev（端口 3000，缺 node_modules 自动安装）
scripts/bug-scan.sh [模型]                              # 本地 AI bug 扫描（opencode + gh），产物在 target/bug-scan/
```

Windows 下用同名 `.bat`（cmd/双击），用法与 `.sh` 一致，日志同样落 `logs/`：

```bat
scripts\start-all.bat
scripts\start-backend.bat start|stop|restart|status
scripts\start-frontend.bat start|stop|restart|status
```

- 前后端脚本均支持 `start|stop|restart|status`，参数缺省为 `start`；日志 `logs/backend.log`、`logs/frontend.log`
- Windows 版内调 PowerShell `Start-Process cmd.exe` 后台启动、`taskkill /T` 结束整棵进程树，PID 同样记录在 `logs/*.pid`
- 后端脚本即 `mvn -Pdev spring-boot:run`（用 `application.yml`，默认内置 H2，无需 MySQL；切 MySQL 用 `profiles=mysql`，连接参数见 `application-mysql.yml` 中的 `db_*` 变量）；仅 E2E 用 `profiles=lib,e2e`（`application-e2e.yml` 切 H2 内存库）

### 发版

发版流程固化在 `scripts\release.bat`（Windows / cmd，输出纯 ASCII 避免乱码），日志落 `logs\release-v<版本>-<时间戳>.log`（UTF-8，已被 gitignore）。**只给一个版本号参数**即可：

```bat
scripts\release.bat 3.1.3                    # bump + 全量测试 + commit + tag + push，触发 CI 发布
scripts\release.bat v3.1.3                   # 版本号带不带 v 都行（推的 tag 一律带 v）
scripts\release.bat 3.1.3 --dry-run          # 只跑检查、打印计划，不改动仓库文件（仅写 logs/ 日志）
scripts\release.bat 3.1.3 --no-push          # 本地演练：commit + tag 但不推送
scripts\release.bat 3.1.3 --skip-tests       # 跳过 mvn test 与 npm build（发版不建议）
scripts\release.bat status                   # 当前分支 / 版本 / tag / 工作区状态
scripts\release.bat check                    # 只读检查，输出 KEY=VALUE（当前版本 + 候选版本号）
scripts\release.bat --help                   # 全部参数与退出码
```

- 版本号可带或不带 `v` 前缀（`3.1.3` / `v3.1.3` 等价）；推送到远端的 tag 一律带 `v`（`v3.1.3`），`publish.yml` 正是按 `v*` 触发
- 前置条件：`gh` 已登录、当前分支为 `main`、工作区干净
- 白名单：一次发版只允许修改 `*/pom.xml` 与 `web/package.json`，出现别的改动直接中止（退出码 3）
- 退出码：`0` 成功 / `1` 参数或前置条件 / `2` 测试失败 / `3` 工作区不干净 / `4` git 失败 / `5` 版本或 tag 冲突
- 失败自动回滚版本号改动；若 commit + tag 已建、只是推送失败，重跑同一条命令会进入 resume 模式只补推送
- push tag 后由 `.github/workflows/publish.yml` 自动发布 Maven Central + npm 并创建 GitHub Release（Release Notes 由 CI 生成）
- 排障：失败时会打印恢复命令与日志路径
- 手工等价命令见 README「开发命令」；从别的 `.bat` 里调用请用 `call scripts\release.bat ...`

## Skills (opencode)

### 内置 skills

| Skill | 用途 |
|-------|------|
| `oa-crud` | 创建 CRUD 业务模块 |
| `oa-upgrade` | 升级框架版本 |
| `oa-upgrade-docs` | 从 GitHub Release 同步框架文件（skills + docs） |
| `oa-sonar-scan` | SonarQube 扫描与问题修复 |

### 从 Release 同步到业务项目

框架发布时自动构建 `framework-files.zip`（含 `.opencode/skills/` 与 `docs/open-admin/`）并附到 GitHub Release。业务项目调用 **`oa-upgrade-docs` skill** 下载并同步到项目根目录：内容比对无变更不写入；`docs/open-admin/` 全量镜像（删除孤儿文件），`.opencode/skills/` 仅覆盖框架 skill（不删业务本地 skill）。同步细节与验证步骤见 `oa-upgrade-docs` skill；升级框架后调用 `oa-upgrade` skill 会自动在末尾触发同步。
