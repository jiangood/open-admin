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

添加依赖后，用户管理、角色权限、数据字典、Quartz 调度、文件管理等功能开箱即用。

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

**前端**：业务项目 `package.json` 添加依赖，`config.js` 注册 `common-plugin.js`（详见 `web/config/common-plugin.js`），自动获得路由注册、代理、主题等功能。

**按需使用**：可只加后端依赖（REST API 访问管理功能），或只加前端依赖（对接自有后端 API）。

## 架构设计

```
┌─────────────────────────────────────────────────────┐
│  前端: React 19 + Ant Design 6 + UmiJS 4            │
│  ┌─────────────────────────────────────────────┐    │
│  │ @jiangood/open-admin (组件库 + 管理页面)     │    │
│  └─────────────────────────────────────────────┘    │
├─────────────────── HTTP API ────────────────────────┤
│  后端: Java 21 + Spring Boot 4.0 + JPA + Security   │
│  ┌──────────┐ ┌──────────┐ ┌────────┐ ┌──────────┐ │
│  │ modules  │ │framework │ │  util  │ │  config  │ │
│  │ (业务层) │ │  (框架层) │ │ (工具) │ │  (配置)  │ │
│  └──────────┘ └──────────┘ └────────┘ └──────────┘ │
├─────────────────── JDBC ────────────────────────────┤
│                    MySQL 8+                          │
└─────────────────────────────────────────────────────┘
```

### 项目结构

```
src/main/java/io/github/jiangood/openadmin/
├── framework/          # 框架基础层
│   ├── config/         # Spring 配置（Security, JPA, Jackson）
│   ├── data/           # BaseEntity, BaseRepository, Spec
│   ├── perm/           # @HasPermission 注解 + 切面
│   ├── log/            # @Log 操作日志注解 + 切面
│   └── common/         # 通用（登录/认证/站点信息）
├── util/               # 工具类库（BeanTool, JsonTool, TreeTool, ExcelTool 等）
└── modules/
    ├── system/         # 用户/角色/菜单/组织/字典/文件/日志
    └── job/            # Quartz 定时任务
web/
├── src/framework/      # @jiangood/open-admin 框架组件库
├── src/pages/          # 业务页面
└── src/layouts/        # 布局组件
```

### 自动配置机制

框架通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 `OpenAdminConfiguration`（含 `@ComponentScan` / `@EntityScan` / `@EnableJpaRepositories`，扫描包 `io.github.jiangood.openadmin`）。默认配置在 `application-lib.yml`，业务项目通过 `spring.config.import` 引入。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | React 19, Ant Design 6, UmiJS 4, TypeScript |
| 后端 | Java 21, Spring Boot 4.0, JPA (Hibernate), Spring Security, Quartz |
| 数据库 | MySQL 8+ |
| 构建 | Maven (后端), npm (前端) |

## 核心功能

### 用户权限管理

- **用户管理**：列表/创建/编辑/重置密码/授权数据
- **角色管理**：列表/创建/编辑/分配权限（菜单 + 按钮）
- **权限控制**：后端 `@HasPermission("resource:action")` 注解 + AOP 切面，支持 SpEL；前端 `<Button perm="xxx:yyy" />` 和 `<HasPerm perm="xxx">` 组件
- **权限码格式**：全小写两段式 `{资源}:{操作}`，资源 kebab-case（如 `sys-user:read`、`sys-role:grant-permission`）
- **YAML 定义**：`data/menu.yml` 中用 `perms` 对象列表定义

### 数据字典

- **预设字典**：`orgType`、`approveStatus`、`sex`、`yesNo`、`dataPermType`、`statusColor`
- **前端使用**：`<FieldDictSelect typeCode="sex" />` 字典选择器；`DictUtils.dictList("sex")` / `DictUtils.dictLabel("sex", "MALE")` / `DictUtils.dictTag("approveStatus", "APPROVED")`
- **扩展**：通过管理界面或 `DictDataInitializer` 钩子添加

### 其他内置功能

| 功能 | 说明 |
|------|------|
| 作业调度 | 基于 Quartz，动态创建/暂停/恢复，继承 `BaseJob` + `@JobDescription` |
| 文件管理 | `sys.file.store-type` 配置（`local` / `s3` / `custom`），统一上传下载预览 |
| 操作日志 | `@Log` 注解 + AOP 切面，异步记录（独立线程池 `operationLogExecutor`） |
| 运行日志查看 | 在线查看日志文件 |

## 开发规范

### 后端命名

| 项 | 规范 |
|----|------|
| Entity | 大驼峰单数，继承 `BaseEntity`，`@Table(name = "t_xxx")` |
| Repository | 继承 `BaseRepository<T, String>`，简单条件用派生查询，复杂用 `Spec` |
| Service | 继承 `BaseService<T>`，构造器注入，`@Transactional(readOnly = true)`，VO 不暴露 Entity |
| Controller | `admin/` 前缀 + kebab-case 复数，`@HasPermission` 控制权限，统一返回 `AjaxResult` |
| DTO | `XxxCreateReq` / `XxxUpdateReq` / `XxxPageQuery` / `XxxVO` |

### REST API 规范

| 操作 | HTTP | URL | 方法 |
|------|------|-----|------|
| 分页查询 | GET | `admin/xxx/page` | `page(Pageable)` |
| 详情 | GET | `admin/xxx/{id}` | `getById(@PathVariable id)` |
| 创建 | POST | `admin/xxx/create` | `create(@RequestBody dto)` |
| 更新 | POST | `admin/xxx/update` | `update(@RequestBody dto, RequestBodyKeys keys)` |
| 删除 | POST | `admin/xxx/delete` | `delete(@Valid @RequestBody IdReq req)` |

### 后端要点

- 强制构造器注入，禁止 `@Autowired` 字段注入
- 业务异常抛 `ServiceException`，Controller 不做 try-catch
- 使用 Java 21 Record / Pattern Matching / Switch 表达式 / Text Block
- 方法参数校验用 `@Valid` / `@Validated`

### 前端要点

- 组件大驼峰，页面文件 kebab-case（UmiJS 路由约定）
- 使用 ES6+，强制 `const`/`let`，解构赋值
- 优先使用框架组件：`ProTable`、`Page`、`FieldDictSelect` 等
- 权限控制：`<Button perm="...">`、`<HasPerm perm="...">`、`<ButtonList>`

## API 参考

### 后端

#### Spec 动态查询

```java
Spec<User> spec = Spec.of()
    .eq("status", 1).like("name", "张")
    .between("createTime", start, end)
    .or(Spec.of().like("name", "张"), Spec.of().like("name", "李"))
    .eq("user.id", userId);  // 关联查询
repository.findAll(spec, pageable);
```

#### 注解

| 注解 | 用途 |
|------|------|
| `@HasPermission("resource:action")` | 权限控制 |
| `@Log` | 操作日志 |
| `@RateLimit(count=10, duration=60)` | IP 限流 |
| `@JobDescription` | 定时任务定义 |
| `@ValidateMobile` / `@ValidateIdCard` / ... | 字段格式校验 |

#### 工具类

| 类 | 主要方法 |
|----|---------|
| `ExcelTool` | `importExcel` / `exportExcel` |
| `DbTool` (JdbcUtils) | `findOne` / `findAll` / `insert` / `updateById` |
| `LoginTool` | `getUserId` / `getUser` / `getPermissions` / `isAdmin` |
| `TreeTool` | `buildTree` / `walk` / `treeToList` / `getLeafs` |
| `BeanTool` / `JsonTool` / `StringTool` | 常用对象/JSON/字符串操作 |
| `PasswordTool` / `AesTool` | 密码加密 / AES 加解密 |

#### 定时任务

```java
@JobDescription(label = "数据同步", params = {
    @FieldDescription(name = "syncType", label = "同步类型", required = true)
})
public class DataSyncJob extends BaseJob {
    public String execute(JobDataMap data, Logger logger) { ... }
}
```

### 前端

#### 组件

| 组件 | 用途 |
|------|------|
| `PageHeader` | 页面头部组件（面包屑导航/页面标题/右侧操作区/自定义内容） |
| `ProTable` | 数据表格，分页/筛选/工具栏，`request`/`columns`/`toolBarRender` |
| `Page` | 页面容器 |
| `Ellipsis` | 文本省略 |
| `LinkButton` | 链接跳转按钮 |
| `NamedIcon` | 通过名称渲染 Ant Design 图标 |
| `ButtonList` | 权限控制按钮组 |
| `HasPerm` | 权限控制容器 |
| `ViewEllipsis` / `ViewFile` / `ViewImage` / `ViewBooleanEnableDisable` | 展示组件 |
| `DownloadModal` | 下载弹框，提供静态 `download()` 方法，支持进度追踪/取消/重试 |

#### 下载弹框

通过静态方法 `DownloadModal.download(options)` 调用，弹框自动管理显隐和状态：

```jsx
import { DownloadModal } from '@jiangood/open-admin';

// 默认 GET
DownloadModal.download({
  url: '/admin/report/export',
  params: { type: 'monthly', year: 2026, month: 7 },
});

// POST 请求，指定文件名
DownloadModal.download({
  url: '/admin/report/export',
  method: 'POST',
  data: { ids: ['1', '2', '3'] },
  fileName: '批量导出.xlsx',
});
```

弹框展示三种状态：下载中（进度条 + 已下载/总计 + 速度）、已完成（✅ + 文件大小）、失败（❌ + 错误消息）。下载中不可关闭弹框，失败后可重试。

#### 字段组件

| 组件 | 用途 |
|------|------|
| `FieldRemoteSelect` | 远程搜索选择框 |
| `FieldDictSelect` | 字典选择 |
| `FieldBoolean` | 布尔值选择（select/radio/checkbox/switch） |
| `FieldDate` / `FieldDateRange` | 日期/日期范围 |
| `FieldSysOrgTreeSelect` | 系统组织树选择 |
| `FieldUploadFile` | 文件上传（`/admin/sysFile/upload`） |
| `FieldEditor` | 富文本编辑器 |
| `FieldPercent` | 百分比输入 |
| `FieldTable` / `FieldTableSelect` | 表格字段/选择 |

#### 文件上传预览

`/preview/{fileId}` 原图，`/preview/{fileId}?w=400` 缩略图（懒生成 + 缓存）。

#### 工具类

| 类 | 主要方法 |
|----|---------|
| `HttpUtils` | `get` / `post` / `postForm`（axios 封装，自动 context-path） |
| `DownloadModal` | `download` 静态方法，弹框显示下载进度和状态，支持取消/重试 |
| `SysUtils` | `contextPath` / `getSiteInfo` / `setSiteInfo` |
| `DictUtils` | `dictList` / `dictLabel` / `dictOptions` / `dictTag` |
| `TreeUtils` | `buildTree` / `treeToList` / `walk` |
| `DateUtils` | `formatDate` / `formatTime` / `formatDateTime` |
| `MessageUtils` | `success` / `error` / `confirm` |

## 配置参考

### 系统配置 (`sys.*` in `application.yml`)

| 配置 | 说明 | 默认值 |
|------|------|--------|
| `sys.title` | 系统标题（必填） | 管理系统 |
| `sys.captcha-enable` | 登录验证码 | true |
| `sys.default-password` | 默认密码 | Open@1234 |
| `sys.show-logo` | 是否显示 Logo | true |
| `sys.file.store-type` | 文件存储 (`local`/`s3`/`custom`) | local |
| `sys.file.upload-path` | 本地上传路径 | /home/files |
| `sys.file.s3.*` | S3 兼容存储配置 | — |
| `sys.session-idle-time` | Session 超时（分钟） | 180 |
| `sys.job-enable` | 定时任务开关 | true |

### 文件存储

- `local` — 本地文件系统，保存到 `sys.file.upload-path`
- `s3` — S3 兼容存储（Minio / AWS S3 / R2 / 阿里云 OSS 等），配置 `sys.file.s3.{endpoint,region,accessKey,secretKey,bucketName,pathStyleAccess}`
- `custom` — 自定义实现，注册 `FileOperator` bean

完整配置项见 `SystemProperties.java`。

### Servlet Context-Path

| 位置 | 配置 |
|------|------|
| 后端 `application.yml` | `server.servlet.context-path` |
| 前端 `web/.env` | `SERVLET_CONTEXT` |

前端 `HttpUtils` 自动带上 context-path 前缀；硬编码 URL 用 `SysUtils.contextPath(path)` 拼接。

### 主题定制

`web/.env` 配置：

```
THEME_PRIMARY_COLOR=#1961AC
THEME_SUCCESS_COLOR=#52c41a
THEME_WARNING_COLOR=#faad14
THEME_ERROR_COLOR=#ff4d4f
THEME_BACKGROUND_COLOR=#f5f5f5
```

## 添加业务模块

1. **Entity** — 继承 `BaseEntity`，JPA 自动建表
2. **Repository** — 继承 `BaseRepository<T, String>`，通用 CRUD + 动态查询
3. **Service** — 继承 `BaseService<T>`，通用业务逻辑
4. **Controller** — RESTful，返回 `AjaxResult`，`@HasPermission` 控制权限
5. **菜单** — `src/main/resources/data/menu*.yml` 定义菜单树
6. **前端** — 使用 `ProTable` + `Field*` 组件快速搭建 CRUD 页面

## 内置模块

| 模块 | 包路径 | 功能 |
|------|--------|------|
| system | `modules/system/` | 用户/角色/菜单/组织/字典/文件/日志管理 |
| job | `modules/job/` | Quartz 定时任务 |
| logviewer | `modules/logviewer/` | 运行日志在线查看 |

## FAQ

**种子数据如何管理？** 框架使用 Flyway 管理种子数据的版本化迁移。框架内置的种子数据位于 `classpath:db/migration/open-admin/V1__seed__init_data.sql`，首次启动时自动执行。

**业务项目如何添加自己的种子数据？** 在 `src/main/resources/db/migration/` 目录下放置 Flyway 迁移脚本即可：

```
src/main/resources/
└── db/migration/
    └── V1__seed__init_biz_data.sql
```

脚本使用 `INSERT IGNORE` 确保幂等性。框架的 seed 脚本与业务项目的脚本互不干扰（不同目录）。

**MySQL 5.7 兼容？** 添加 `hibernate-community-dialects` 依赖，配置 `spring.jpa.properties.hibernate.dialect=org.hibernate.community.dialect.MySQLLegacyDialect`。

**前端依赖安装失败？** `npm install --registry=https://registry.npmmirror.com`

**端口被占用？** 后端默认 8080，前端默认 8000，可通过环境变量 `SERVER_PORT` 修改后端端口。

## Claude Code 集成

在业务项目中使用 Claude Code 创建 CRUD 模块前，请先安装 open-admin skill：

> 请帮我安装 skill，地址为 https://raw.githubusercontent.com/jiangood/open-admin/main/.claude/skills/open-admin.md

安装后使用 `/open-admin` 指令创建业务模块，Claude 会按框架规范自动生成 Entity → Repository → Service → Controller → 前端页面 → 菜单配置。更详细的 AI 指引见 [CLAUDE.md](CLAUDE.md)。
