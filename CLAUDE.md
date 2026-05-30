# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

open-admin 是一个可嵌入的后台管理系统框架（脚手架），业务项目通过 Maven/pnpm 添加依赖即可获得完整的后台管理能力，无需从零搭建用户管理、角色权限、数据字典等功能。框架发布在 Maven Central (`io.github.jiangood:open-admin`) 和 npm (`@jiangood/open-admin`)。

同级目录 `D:\ws\open-admin-example` 是示例业务项目，修改框架后需在示例项目中验证。

## Tech Stack

- **Backend**: Java 21, Spring Boot 4.0.6, JPA (Hibernate), Spring Security, Quartz, MySQL 8+
- **Frontend**: React 19, Ant Design 6, UmiJS 4, TypeScript
- **Build**: Maven (backend), npm (frontend)

## Two-Project Workflow

```
D:/ws/
├── open-admin/              # 框架项目（本仓库）
│   ├── src/main/java/       # 框架源码
│   ├── web/src/framework/   # 前端框架源码 (npm publish)
│   └── pom.xml
└── open-admin-example/      # 示例业务项目（依赖框架）
    ├── src/main/java/       # 极少量业务代码
    ├── web/                 # 前端（依赖 @jiangood/open-admin）
    └── pom.xml              # 依赖 io.github.jiangood:open-admin
```

修改框架后，需先在框架项目执行 `mvnw install` 或 `mvnw package`，然后在示例项目更新依赖版本验证。

## Auto-Configuration Mechanism

框架通过 Spring Boot 自动配置机制注入：

- **AutoConfiguration.imports**: `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册 `OpenAdminConfiguration` 和 `SpringTool`
- **OpenAdminConfiguration.java**: 包含 `@ComponentScan`、`@EntityScan`、`@EnableJpaRepositories`，扫描包 `io.github.jiangood.openadmin`
- **application-lib.yml**: 框架默认配置（框架 JAR 包内的 `src/main/resources/application-lib.yml`），业务项目通过 `spring.config.import: classpath:application-lib.yml` 引入，业务配置优先级更高可覆盖

## Maven Profiles

| Profile | Command | Purpose |
|---------|---------|---------|
| (default) | `mvnw package` | 编译打包框架 JAR（不含 Spring Boot 可执行，供其他项目依赖） |
| app | `mvnw -Papp spring-boot:run` | 以独立应用运行（含 Spring Boot plugin，开发用） |
| publish | `mvnw -Ppublish package` | 发布到 Maven Central（含 sources、javadoc、GPG signing） |

## Development Commands

```bash
# Backend - 编译（必须用 mvnw，不能用系统 mvn）
./mvnw clean compile

# Backend - 运行测试（单个测试）
./mvnw test -Dtest=BeanToolTest

# Backend - 运行测试（排除需要 MySQL 的集成测试）
./mvnw test -Dtest='!*RepositoryTest,!*ServiceTest,!MenuLoadingIntegrationTest'

# Backend - 打包（供业务项目依赖）
./mvnw clean package

# Backend - 以独立应用启动（含完整 Spring Boot 入口）
./mvnw -Papp spring-boot:run

# Backend - 安装到本地 Maven 仓库（供同级 open-admin-example 使用）
./mvnw clean install -DskipTests

# Backend - 发布到 Maven Central
./mvnw -Ppublish clean package

# Frontend - 安装依赖
cd web && npm install

# Frontend - 开发模式
cd web && npm run dev

# Frontend - 构建
cd web && npm run build
```

测试使用 H2 内存数据库（配置在 `src/test/resources/application.yml`），无需 MySQL。集成测试（Repository 测试、Service 测试）需要 MySQL，默认被排除。

## Frontend Architecture

### Framework Module (`web/src/framework/`)

`web/src/framework` 目录发布为独立 npm 包 `@jiangood/open-admin`，业务项目通过 npm 依赖引用。核心导出：

- **components/**: ProTable（通用列表）、ProModal（弹窗表单）、Page、OrgTree、RoleTree、NamedIcon 等
- **fields/**: FieldDictSelect、FieldRemoteSelect、FieldDate、FieldDateRange、FieldUploadFile、FieldBoolean 等表单字段
- **views/**: ViewFile、ViewImage、ViewBoolean、ViewApproveStatus、ViewPassword 等展示组件
- **utils/**: HttpUtils（axios 封装，自动 context-path）、SysUtils、DictUtils、TreeUtils、ThemeUtils、DateUtils 等

### UmiJS Plugin（`common-plugin.js`）

`web/config/common-plugin.js` 是 UmiJS 插件，业务项目 `config.js` 只需注册此插件即可。功能：

1. **构建配置自动注入**：`SERVLET_CONTEXT`（context-path）、`publicPath`、`hash`、`history`（hash 模式）、`mfsu`、`esbuildMinifyIIFE` 等默认值
2. **开发代理**：自动配置 proxy 到后端，支持 `SERVER_PORT` 环境变量（默认 8080），支持 WebSocket
3. **主题配置**：读取 `THEME_*` 环境变量注入为 `OPEN_ADMIN_THEME`，框架 `ThemeUtils` 动态合并
4. **路由自动注册**：扫描 `node_modules/@jiangood/open-admin/src/pages/` 下的 `.jsx` 文件，自动注册为路由
5. **表单组件注册**：扫描业务项目 `src/forms/` 目录，自动注册自定义表单组件到 `FormRegistryUtils`

### Layout

`web/src/layouts/` 包含完整后台布局：侧边菜单、顶部 Header、TabPage 多标签页、PageRender 页面渲染器。

## API Response Format

所有 API 统一返回 `AjaxResult`（JSON）：

```json
{"code": 200, "msg": "成功", "data": {...}}
```

异常由 `GlobalExceptionHandler` 统一处理，返回 `BusinessException` 或校验错误信息。

## Key Architecture Patterns

- **BaseEntity/BaseRepository**: 所有实体继承 BaseEntity（UUIDv7 id, createTime/createUser, updateTime/updateUser, delFlag），Repository 继承 BaseRepository 获得通用 CRUD + 批量操作 + 动态查询
- **Spec 动态查询**: `Spec<T>` 链式构建 JPA Specification（eq/like/in/between/or/groupBy/having 及关联查询），通过 `SpecImpl` + `ExpressionTool` 执行
- **PageExt**: 扩展 PageImpl，支持返回额外数据（如汇总行 summary）
- **菜单加载**: 通过 `classpath*:data/menu*.yml` YAML 文件定义菜单树（`MenuYamlLoader`），也支持数据库存储（`SysMenuRepositoryYamlImpl`）
- **权限控制**: `@HasPermission` 注解 + AOP 切面，支持 SpEL 表达式
- **数据字典**: 首次启动 `dict-init.sql` 自动导入初始字典数据
- **ID 生成**: 默认 UUIDv7（时间排序，MySQL 友好），也支持前缀序列 ID 和日表序列 ID
- **文件存储**: 支持本地文件系统和 Minio，通过 `FileOperator` 接口抽象，`sys.file.store-type` 配置
- **操作日志**: `@Log` 注解 + AOP 切面，异步记录（独立线程池 `operationLogExecutor`）

## Built-in System Modules

框架在 `io.github.jiangood.openadmin.modules.*` 中已实现完整后台管理功能：

| 模块 | 包路径 | 功能 |
|------|--------|------|
| system | `modules/system/` | 用户/角色/菜单/组织/字典/文件/日志管理 |
| job | `modules/job/` | Quartz 定时任务（动态创建/暂停/恢复） |
| logviewer | `modules/logviewer/` | 运行日志在线查看 |

## Utility Classes

框架提供 77+ 工具类，位于 `io.github.jiangood.openadmin.util`：

- **BeanTool/JsonTool/StringTool**: 常用对象/JSON/字符串操作
- **TreeTool**: 树结构构建与操作（含拖拽排序 `TreeDropTool`）
- **ExcelTool**: Excel 导入导出（基于 Apache POI）
- **FileTool/FileTypeTool/FontTool/ImgTool**: 文件操作
- **PasswordTool/AesTool**: 密码加密/AES 加解密
- **IpTool/IpRegionTool**: IP 地址解析
- **DateTool/RangeTool**: 日期和时间范围处理
- **DbTool/SqlBuilder**: JDBC 工具和 SQL 构建
- **ReflectTool/ClassTool/AnnotationTool**: 反射和注解处理

## Important Configurations (`sys.*` in application.yml)

| 配置 | 说明 | 默认值 |
|------|------|--------|
| `sys.title` | 系统标题（必填） | 管理系统 |
| `sys.captcha-enable` | 登录验证码 | true |
| `sys.default-password` | 默认密码 | open-admin@1234 |
| `sys.logo-url` | Logo 路径 | /admin/public/logo.svg |
| `sys.file.store-type` | 文件存储 (local/minio) | local |
| `sys.file.upload-path` | 本地上传路径 | /home/files |
| `sys.session-idle-time` | Session 超时（分钟） | 180 |
| `sys.job-enable` | 定时任务开关 | true |

完整配置项见 `SystemProperties.java`。

## Adding a Business Module

1. **Entity**: 继承 `BaseEntity`，JPA 自动建表
2. **Repository**: 继承 `BaseRepository<T, String>`，获得通用 CRUD 和动态查询
3. **Service**: 继承 `BaseService<T>`，获得通用业务逻辑
4. **Controller**: RESTful，返回 `AjaxResult`，使用 `@HasPermission` 控制权限
5. **菜单**: `src/main/resources/data/menu*.yml` 定义菜单树
6. **前端**: 使用 ProTable + Field* 组件快速搭建 CRUD 页面

## Context-Path Configuration

| 位置 | 文件 | 说明 |
|------|------|------|
| 后端 | `src/main/resources/application.yml` | `server.servlet.context-path` |
| 前端环境变量 | `web/.env` | `SERVLET_CONTEXT` 环境变量 |
| UmiJS 插件 | `web/config/common-plugin.js` | 自动读取 `.env` 注入 `define` + `proxy`（含 WebSocket） |

## Theme Customization

前端主题色通过 `.env` 环境变量自定义（由 `common-plugin` 自动注入，业务项目无需修改 `config.js`）：

```
# web/.env
THEME_PRIMARY_COLOR=#1961AC        # 主色，自动派生 hover/click 变体
THEME_SUCCESS_COLOR=#52c41a        # 成功色
THEME_WARNING_COLOR=#faad14        # 警告色
THEME_ERROR_COLOR=#ff4d4f          # 错误色
THEME_BACKGROUND_COLOR=#f5f5f5     # 背景色
```

未配置时使用默认值。`THEME_PRIMARY_COLOR` 会自动通过 `ColorsUtils.lighten/darken` 计算 `primary-color-hover` 和 `primary-color-click`。
