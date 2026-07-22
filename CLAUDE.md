# CLAUDE.md

本文件为 Claude Code 等 AI 工具提供本仓库的指引。详细文档请参阅 [README.md](README.md)。

## Project Overview

open-admin 是一个可嵌入的后台管理系统框架。业务项目通过 Maven/npm 添加依赖即可获得完整的后台管理能力，无需从零搭建用户管理、角色权限、数据字典等功能。

## Tech Stack

- **Backend**: Java 21, Spring Boot 4.0.6, JPA (Hibernate), Spring Security, Quartz, MySQL 8+
- **Frontend**: React 19, Ant Design 6, UmiJS 4, TypeScript
- **Build**: Maven (backend), npm (frontend)

## Two-Project Workflow

```
D:/ws/
├── open-admin/              # 框架项目（本仓库）
│   ├── src/main/java/
│   ├── web/src/framework/   # 前端框架源码 (npm publish)
│   └── pom.xml
└── open-admin-example/      # 示例业务项目（依赖框架）
```

修改框架后需先执行 `mvn clean install -DskipTests`，然后在示例项目更新版本验证。

## Development Commands

```bash
mvn clean compile                                          # 编译
mvn test -Dtest=BeanToolTest                               # 运行单个测试
mvn test -Dtest='!*RepositoryTest,!*ServiceTest'           # 排除需要 MySQL 的测试
mvn clean package                                          # 打包
mvn -Papp spring-boot:run                                  # 独立应用启动
mvn clean install -DskipTests                              # 安装到本地仓库
node scripts/bump-version.js <新版本号>                     # 升级 pom.xml + web/package.json 版本号
cd web && npm install                                         # 前端安装依赖
cd web && npm run dev                                         # 前端开发模式
cd web && npm run build                                       # 前端构建
```

测试使用 H2 内存数据库，无需 MySQL。集成测试需要 MySQL，默认被排除。

## Auto-Configuration Mechanism

框架通过 Spring Boot 自动配置机制注入：`AutoConfiguration.imports` 注册 `OpenAdminConfiguration`（含 `@ComponentScan` / `@EntityScan` / `@EnableJpaRepositories`，扫描 `io.github.jiangood.openadmin`）。默认配置在 `application-lib.yml`，业务项目通过 `spring.config.import` 引入。

## Key Architecture Patterns

- **BaseEntity/BaseRepository**: 所有实体继承 BaseEntity（UUIDv7 id, createTime/createUser, updateTime/updateUser, delFlag），Repository 继承 BaseRepository
- **Spec 动态查询**: `Spec<T>` 链式构建 JPA Specification（eq/like/in/between/or 等），通过 `SpecImpl` + `ExpressionTool` 执行
- **PageExt**: 扩展 PageImpl，支持返回额外数据（如汇总行）
- **菜单加载**: `classpath*:application-menu*.yml` YAML 定义菜单（Map 格式，key 为菜单 id），也支持数据库存储
- **权限控制**: `@HasPermission` 注解 + AOP 切面，支持 SpEL 表达式
- **ID 生成**: 默认 UUIDv7（时间排序，MySQL 友好），也支持前缀序列 ID 和日表序列 ID
- **文件存储**: 支持 `local` / `s3` / `custom`，通过 `FileOperator` 接口抽象
- **操作日志**: `@Log` 注解 + AOP 切面，异步记录

## Adding a Business Module

1. **Entity**: 继承 `BaseEntity`，JPA 自动建表
2. **Repository**: 继承 `BaseRepository<T, String>`，获得通用 CRUD 和动态查询
3. **Service**: 继承 `BaseService<T>`，获得通用业务逻辑
4. **Controller**: RESTful，返回 `AjaxResult`，使用 `@HasPermission` 控制权限
5. **菜单**: `src/main/resources/application-menu*.yml` 定义菜单（Map 结构，key 为菜单 id，pid 表达父子关系）
6. **前端**: 使用 ProTable + Field* 组件快速搭建 CRUD 页面

## Context-Path & Theme

| 位置 | 文件 | 配置 |
|------|------|------|
| 后端 | `application.yml` | `server.servlet.context-path` |
| 前端 | `web/.env` | `SERVLET_CONTEXT` / `THEME_PRIMARY_COLOR` / `THEME_SUCCESS_COLOR` / ... |

完整配置和 API 参考请查看 [README.md](README.md)。
