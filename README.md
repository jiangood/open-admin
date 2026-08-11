# open-admin

![Maven Central](https://img.shields.io/maven-central/v/io.github.jiangood/open-admin)
![npm](https://img.shields.io/npm/v/@jiangood/open-admin)

open-admin 是一个可嵌入的后台管理系统框架（脚手架），**业务项目通过添加 Maven 和 npm 依赖即可获得完整的后台管理能力**，无需从零搭建用户管理、角色权限、数据字典等功能。

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

添加依赖后，用户管理、角色权限、数据字典、Quartz 调度、文件管理等功能开箱即用。**首次启动后端时，框架会自动生成 `.opencode/skills/` 与 `docs/open-admin/` 到项目根目录**（详见 [Skills (opencode)](#skills-opencode)）。

## 快速开始

### 环境要求

- **JDK 21+** / **MySQL 8.0+** / **Node.js 18+**

### 后端启动

```bash
# 创建数据库
CREATE DATABASE open_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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

**按需使用**：可只加后端依赖（REST API 访问管理功能），或只加前端依赖（对接自有后端 API）。

## 文档

| 文档 | 内容 |
|------|------|
| [docs/open-admin/guide.md](docs/open-admin/guide.md) | 架构设计 / 核心功能 / 添加业务模块 / 内置模块 / FAQ |
| [docs/open-admin/api.md](docs/open-admin/api.md) | 后端（Spec/注解/工具类/定时任务）+ 前端（组件/生命周期/字段组件/文件上传/工具类）API 参考 |
| [docs/open-admin/config.md](docs/open-admin/config.md) | 全部 `sys.*` 配置 / 文件存储 / 未认领文件清理 / context-path / 主题定制 |
| [docs/open-admin/development.md](docs/open-admin/development.md) | 后端命名 / REST API 规范 / 前后端开发要点 |

## Skills (opencode)

### 内置 skills

| Skill | 用途 |
|-------|------|
| `oa-crud` | 创建 CRUD 业务模块 |
| `oa-upgrade` | 升级框架版本 |

### 自动同步到业务项目

框架 JAR 内置业务侧 skills 与文档（`META-INF/open-admin/framework-files/`）。业务项目**启动后端时自动同步**到项目根目录：

```
<项目根>/.opencode/skills/oa-crud/SKILL.md
<项目根>/.opencode/skills/oa-upgrade/SKILL.md
<项目根>/docs/open-admin/*.md     # 即本文档（guide/api/config/development/AGENTS）
<项目根>/AGENTS.md                # opencode 开发指引（仅首次生成，不覆盖本地自定义）
```

- 同步按**内容比对**：无变更不写入，升级新版本后首次启动自动覆盖更新
- `docs/open-admin/` 全量镜像（删除该目录下孤儿文件）；`.opencode/skills/` 仅覆盖框架 skill，不删除业务本地 skill
- 根目录 `AGENTS.md` 仅在不存在时生成（便于 opencode 开发，业务可自定义）；框架更新版随 `docs/open-admin/AGENTS.md` 提供
- 目标目录为项目根（向上查找最近 `pom.xml`），生产部署目录无 `pom.xml` 时仅生成 `AGENTS.md` 副本
- 无需任何配置，默认开启

> 首次接入无需手动复制，启动一次后端即可；升级后下次启动即自动更新。
