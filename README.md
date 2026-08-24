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

添加依赖后，用户管理、角色权限、数据字典、Quartz 调度、文件管理等功能开箱即用。框架的 skills 与文档通过 `oa-upgrade-docs` skill 从 GitHub Release 同步到项目根目录（详见 [Skills (opencode)](#skills-opencode)）。

## 快速开始

### 环境要求

- **JDK 21+** / **MySQL 8.0+** / **Node.js 18+**

### 后端启动

```bash
# 创建数据库
CREATE DATABASE open_admin;

# 修改 src/main/resources/application.yml 数据库连接
git clone https://github.com/jiangood/open-admin.git
cd open-admin
mvn clean compile
mvn -Pdev spring-boot:run   # 开发模式启动
```

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

**目录约定**（无需配置，自动识别）：

| 目录 | 路由前缀 | 是否需要登录 | 是否需要 AdminLayout |
|------|---------|-------------|-------------------|
| `pages/` | `/` | ✅ 是 | ✅ 是 |
| `pages/public/` | `/public/` | ❌ 否 | ❌ 否 |
| `pages/standalone/` | `/standalone/` | ✅ 是 | ❌ 否 |


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
node scripts/bump-version.js <新版本号>                     # 升级 pom.xml + web/package.json 版本号
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
- 后端脚本即 `mvn -Pdev spring-boot:run`（用 `application.yml`，需本地 MySQL 8+，连接参数见其中的 `db_*` 变量）；仅 E2E 用 `profiles=lib,e2e`（`application-e2e.yml` 切 H2 内存库）

## Skills (opencode)

### 内置 skills

| Skill | 用途 |
|-------|------|
| `oa-crud` | 创建 CRUD 业务模块 |
| `oa-upgrade` | 升级框架版本 |
| `oa-upgrade-docs` | 从 GitHub Release 同步框架文件（skills + docs + AGENTS.md） |
| `oa-sonar-scan` | SonarQube 扫描与问题修复 |

### 从 Release 同步到业务项目

框架发布时自动构建 `framework-files.zip`（含 `.opencode/skills/` 与 `docs/open-admin/`）并附到 GitHub Release。业务项目通过 **`oa-upgrade-docs` skill** 从 `https://github.com/jiangood/open-admin/releases/download/v{版本}/framework-files.zip` 下载并同步到项目根目录：

```
<项目根>/.opencode/skills/oa-crud/SKILL.md
<项目根>/.opencode/skills/oa-upgrade/SKILL.md
<项目根>/.opencode/skills/oa-upgrade-docs/SKILL.md
<项目根>/.opencode/skills/oa-sonar-scan/SKILL.md
<项目根>/docs/open-admin/*.md     # 即本文档（guide/api/config/development/AGENTS）
<项目根>/AGENTS.md                # opencode 开发指引（不存在时生成；已存在且不同时询问确认后更新）
```

- 同步按**内容比对**：无变更不写入
- `docs/open-admin/` 全量镜像（删除该目录下孤儿文件）；`.opencode/skills/` 仅覆盖框架 skill，不删除业务本地 skill
- 根目录 `AGENTS.md` 不存在时生成；已存在且与框架新版内容不同时，`oa-upgrade-docs` 会展示 diff 并询问开发者确认后再更新（避免无提示覆盖本地自定义）；框架更新版随 `docs/open-admin/AGENTS.md` 提供
- 升级框架后调用 `oa-upgrade` skill 会自动在末尾调用 `oa-upgrade-docs` 同步新版本框架文件
