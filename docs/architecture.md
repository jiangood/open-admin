# 架构设计

## 系统架构

open-admin 采用前后端分离架构。

```
┌────────────────────────────────────────────────────┐
│                      前端                           │
│   React 19 + Ant Design 6 + UmiJS 4                │
│        ┌──────────────────────┐                    │
│        │  @jiangood/open-admin │  (框架组件库)      │
│        └──────────────────────┘                    │
└──────────────────┬─────────────────────────────────┘
                   │ HTTP API
┌──────────────────▼─────────────────────────────────┐
│                      后端                           │
│   Java 21 + Spring Boot 4.0 + JPA + Security       │
│                                                     │
│   ┌─────────┐ ┌──────────┐ ┌─────────┐ ┌────────┐ │
│   │ modules │ │framework │ │  lang   │ │ config │ │
│   │ (业务)  │ │  (框架)  │ │ (工具)  │ │ (配置) │ │
│   └─────────┘ └──────────┘ └─────────┘ └────────┘ │
└──────────────────┬─────────────────────────────────┘
                   │ JDBC
┌──────────────────▼─────────────────────────────────┐
│                    MySQL 8+                         │
└────────────────────────────────────────────────────┘
```

## 项目结构

```
open-admin/
├── src/main/java/io/github/jiangood/openadmin/
│   ├── framework/              # 框架基础层
│   │   ├── config/             # Spring 配置（Security、JPA、Jackson、OpenAPI）
│   │   ├── data/               # JPA 基础（BaseEntity、BaseRepository、Spec）
│   │   ├── perm/               # @HasPermission 注解 + 切面
│   │   ├── log/                # @Log 操作日志注解 + 切面
│   │   ├── filter/             # RequestBody 缓存过滤器
│   │   └── lifecycle/          # 应用生命周期钩子
│   ├── lang/                   # 工具类库（BeanTool、JsonTool、TreeTool、ExcelTool 等）
│   └── modules/
│       ├── system/             # 系统管理（用户/角色/菜单/组织/字典/文件/日志）
│       ├── job/                # 定时任务（Quartz）
│       ├── api/                # 开放接口管理
│       ├── logviewer/          # 文件日志查看
│       └── common/             # 通用（登录/认证/站点信息）
├── web/                        # 前端项目（UmiJS）
│   └── src/
│       ├── framework/          # @jiangood/open-admin 框架组件库
│       ├── pages/              # 业务页面
│       └── layouts/            # 布局组件
└── src/main/resources/
    ├── application.yml         # 主配置
    └── config/                 # 扩展配置（菜单、字典等）
```

## 核心模块

### framework 层

提供通用的框架能力，不包含业务逻辑：

- **data** — `BaseEntity`（通用字段）、`BaseRepositoryImpl`（通用 CRUD）、`Spec`（动态查询）、ID 生成器
- **config/security** — Spring Security 配置、`SysUserDetailsService`
- **perm** — `@HasPermission` 权限注解 + SpEL 表达式支持
- **log** — `@Log` 操作日志注解
- **migration** — 数据迁移

### modules 层

业务模块，各模块独立组织 entity/repository/service/controller。

## 设计理念

### 模块化

各模块职责清晰，可独立开发和维护。框架层与业务层分离，业务模块之间低耦合。

### 约定优于配置

- Entity 继承 `BaseEntity` 自动获得 id、createTime、updateTime
- Repository 继承 `BaseRepository` 获得通用 CRUD
- Service 继承 `JpaService` 获得通用业务方法

### 动态查询

`Spec` 构建器提供链式 API 构建 JPA 动态查询，支持 AND/OR 组合、关联查询、聚合函数。

### 权限体系

`@HasPermission` 注解 + AOP 切面实现声明式权限控制，支持 SpEL 表达式。
