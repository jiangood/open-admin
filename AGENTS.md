# AGENTS.md

## Project

open-admin — 可嵌入的后台管理系统框架（脚手架），不是一个独立应用。业务项目通过 Maven + pnpm 添加依赖集成。当前版本 2.0.1。

## Stack

- **Backend**: Java 21, Spring Boot 4.0, JPA (Hibernate), Spring Security, Quartz, MySQL 8+ (测试用 H2)
- **Frontend**: React 19, Ant Design 6, UmiJS 4 (非 Ant Design Pro), TypeScript 6
- **Build**: Maven (back), pnpm (front)

## Key commands

```bash
# Backend
mvn clean compile                # 编译
mvn test -Dtest=BeanToolTest     # 单个测试类
mvn test -q                      # 全部测试（静默）
mvn spring-boot:run              # 运行
mvn package -Drevision=1.2.7     # 打包

# Frontend（在 web/ 目录下）
pnpm install                     # 安装依赖
pnpm dev                         # 开发模式（代理到 127.0.0.1:8080）
pnpm build                       # 构建
```

## Auto-config entry

`OpenAdminConfiguration` (`src/main/java/io/github/jiangood/openadmin/`) — 外部项目通过 `@Import(OpenAdminConfiguration.class)` 或 `@ComponentScan("io.github.jiangood.openadmin")` 集成框架。包扫描/实体扫描/JPA Repository 均基于 `io.github.jiangood.openadmin`。

## Architecture notes

- **JPA 基础层**: 所有 Entity 继承 `BaseEntity`，Repository 继承 `BaseRepository` → `JpaService` 提供通用 CRUD
- **Spec 动态查询**: `Spec` + `SpecImpl` + `ExpressionTool` 构建 JPA 条件，支持 AND/OR/关联查询/聚合
- **权限**: `@HasPermission` 注解 + AOP 切面，支持 SpEL
- **ID 生成**: 默认 UUIDv7（`uuid-creator` 库，时间排序，MySQL 友好）
- **菜单定义**: YAML 文件定义菜单树（`SysMenuRepositoryYamlImpl`），也可存 DB
- **框架组件**: `web/src/framework/` 作为独立 npm 包 `@jiangood/open-admin` 发布。业务页面在 `web/src/pages/` 中引用

## Frontend quirks

- UmiJS 配置使用 hash 路由 (`history: { type: 'hash' }`) + JS 配置 (`config/config.js`)
- 代理 `/admin/*` → `http://127.0.0.1:8080`，WebSocket `/admin/ws` 同样代理
- 插件路径自适应：框架自身用 `./config/common-plugin`，业务项目用 `@jiangood/open-admin/config`
- `web/src/framework/` 中的组件会被重新导出，通过 `@jiangood/open-admin` 给业务项目使用

## Config

- 数据库连接用占位符 `${db_ip}`, `${db_port}`, `${db_database}`, `${db_username}`, `${db_password}`，在应用 `application.yml` 中定义
- 框架默认配置在 `application-lib.yml`（业务项目通过 `spring.config.import: classpath:application-lib.yml` 引入）
- Logo 配置: `sys.logo-url`（默认 `/admin/public/logo.jpg`）
- 默认密码: `sys.default-password: open-admin@1234`

## Publishing

- Git tag `v*` 触发 GitHub Action 发布 Maven Central + npm
- 版本号必须在 tag / pom.xml / web/package.json 三者间一致
- Maven profile `publish` 用于发布（包含 GPG 签名 + javadoc + source jar）
- 发布后自动同步阿里云 npm 镜像

## Tests

- 测试用 H2 内存数据库（无需 MySQL）
- 集成测试需要 Spring Boot 上下文（`@SpringBootTest`）—— 在 open-admin 模块内可直接运行
- CI 中运行 `mvn test -q`

## Docker

- 多阶段构建：Maven 3.9 + Temurin 21 (back) → Node 24 (front) → Temurin 21 JRE (runtime)
- Docker Compose 含 MySQL 8.0 + app，数据持久化到 `./mysql-data/` 和 `./app-data/`
- RSA 密钥位于 `data/` 目录（由 `RsaTool.java` 生成），容器重启需持久化

## Changelog

- Conventional Commits 规范，`cliff.toml` 配置 `git-cliff` 自动生成 CHANGELOG.md
