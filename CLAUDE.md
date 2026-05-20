# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

open-admin 是一个可嵌入的后台管理系统框架（脚手架），业务项目通过 Maven/pnpm 添加依赖即可获得完整的后台管理能力，无需从零搭建用户管理、角色权限、数据字典等功能。

## Tech Stack

- **Backend**: Java 21, Spring Boot 4.0, JPA (Hibernate), Spring Security, Quartz, MySQL 8+
- **Frontend**: React 19, Ant Design 6, UmiJS 4 (React framework, not Ant Design Pro)
- **Build**: Maven (backend), pnpm (frontend)

## Project Structure

```
open-admin/
├── pom.xml                    # Maven 父项目
├── src/main/java/io/github/jiangood/openadmin/
│   ├── OpenAdminConfiguration.java       # 自动配置入口 (@ComponentScan, @EntityScan, @EnableJpaRepositories)
│   ├── framework/
│   │   ├── config/            # Spring 配置 (Security, JPA, Jackson, SystemProperties)
│   │   │   ├── security/      # Spring Security 配置 + 权限刷新
│   │   │   ├── json/          # Jackson 自定义序列化/反序列化
│   │   │   └── MenuDefinition.java # 菜单数据定义 (从 YAML 加载)
│   │   ├── data/              # JPA 基础层: BaseEntity, BaseRepository(BaseRepositoryImpl), Spec (动态查询)
│   │   │   ├── converter/     # JPA AttributeConverter 集合
│   │   │   ├── id/            # ID 生成器 (UUIDv7, 前缀序列, 日表序列)
│   │   │   └── specification/ # 动态查询 Spec 构建器
│   │   ├── perm/              # 权限注解 @HasPermission + 切面
│   │   ├── log/               # 操作日志注解 @Log + 切面
│   │   ├── migration/         # 数据迁移
│   │   ├── validator/         # 自定义校验注解 (手机号, 身份证, 密码等)
│   │   └── enums/             # 基础枚举 (YesNo, Sex, ApproveStatus)
│   ├── auth/                  # 认证 (登录/登出/验证码/当前用户)
│   ├── console/               # 控制台公共 API (站点信息/菜单)
│   ├── util/                  # 工具类库 (BeanTool, StringTool, JsonTool, TreeTool, ExcelTool, FileTool 等)
│   └── modules/
│       ├── system/            # 系统管理模块 (用户/角色/菜单/组织/字典/文件/日志)
│       │   ├── entity/        # JPA 实体
│       │   ├── repository/    # Spring Data JPA Repository
│       │   ├── service/       # 业务逻辑
│       │   ├── controller/    # REST API
│       │   ├── dto/           # 请求/响应 DTO
│       │   └── file/          # 文件存储 (本地/Minio)
│       ├── job/               # 定时任务模块 (Quartz)
│       ├── api/               # API 开放接口模块 (对外接口管理)
│       ├── logviewer/         # 文件日志查看
│       │   ├── controller/
│       │   ├── service/
│       │   ├── config/        # Logback 配置
│       │   └── util/          # MDC 工具
├── web/                       # 前端项目 (UmiJS)
│   ├── package.json
│   ├── src/
│   │   ├── framework/         # 框架组件库 (npm 包 @jiangood/open-admin)
│   │   │   ├── components/    # 通用组件 (ProTable, Page, OrgTree, RoleTree, NamedIcon 等)
│   │   │   ├── fields/        # 表单字段组件 (FieldDictSelect, FieldRemoteSelect, FieldDate 等)
│   │   │   ├── views/         # 展示组件 (ViewFile, ViewImage, ViewBoolean 等)
│   │   │   └── utils/         # 工具类 (HttpUtils, SysUtils, DictUtils, TreeUtils, ThemeUtils 等)
│   │   ├── layouts/           # 布局组件 (admin 后台布局含菜单/Sider/Header/TabPage)
│   │   ├── pages/             # 业务页面
│   │   └── config/            # UmiJS 配置
│   └── config/                # UmiJS 构建配置
└── src/main/resources/
    ├── application.yml        # 主配置文件
    └── static/admin/public/   # 静态资源 (logo.jpg, login_bg.jpg)
```

## Key Architecture Patterns

- **BaseEntity/BaseRepository**: 所有实体继承 `BaseEntity` (含 id, createTime, updateTime 等通用字段)，Repository 继承 `BaseRepository` 提供通用 CRUD + JpaService 通用服务层
- **Spec 动态查询**: `Spec` + `SpecImpl` 构建 JPA 动态查询，支持 AND/OR 组合、关联查询、聚合函数，通过 `ExpressionTool` 处理多种操作符
- **菜单定义**: 通过 YAML 文件定义菜单树 (`SysMenuRepositoryYamlImpl`)，也支持数据库存储
- **代码生成**: 后端 JpaService 提供通用增删改查，前端 ProTable + Field 组件实现通用列表/表单。代码生成时主要关注 Entity/Repository + 前端页面
- **权限**: `@HasPermission` 注解 + AOP 切面，支持 SpEL 表达式
- **工具类**: `io.github.jiangood.openadmin.util` 包下大量工具类 (BeanTool, JsonTool, TreeTool, ExcelTool 等)，可按需使用
- **ID 生成**: 默认使用 UUIDv7 (时间排序，MySQL 友好)

## Logo 配置

配置文件 `application.yml`:
```yaml
sys:
  logo-url: /admin/public/logo.jpg   # logo 图片路径
```

- 默认值: `SystemProperties.java:56` — `private String logoUrl = "/admin/public/logo.jpg"`
- 默认图片文件: `src/main/resources/static/admin/public/logo.jpg`
- 前端渲染: `web/src/layouts/admin/index.jsx:104` — `<img src={siteInfo.logoUrl} />`
- 后端 API: `SysCommonController.java:56` — 通过 `/admin/public/site-info` 接口返回 `logoUrl`
- 配置方式: 修改 `application.yml` 中的 `sys.logo-url`，或替换 logo.jpg 文件，或上传文件后使用文件预览 URL

## Development Commands

```bash
# Backend - 编译
mvn clean compile

# Backend - 运行测试 (单个测试类)
mvn test -Dtest=BeanToolTest

# Backend - 打包 (可指定版本)
mvn package -Drevision=1.2.7

# Backend - 运行应用
mvn spring-boot:run

# Frontend - 安装依赖
cd web && pnpm install

# Frontend - 开发模式
cd web && pnpm dev

# Frontend - 构建
cd web && pnpm build
```

## Framework Module (web/src/framework)

The `web/src/framework` directory is published as an independent npm package `@jiangood/open-admin` — it's a reusable component/field/view library, not tied to specific business pages. Business pages in `web/src/pages/` import from it.
