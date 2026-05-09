# open-admin 文档

open-admin 是一个小型管理系统框架（后台管理脚手架），提供用户管理、角色权限、数据字典、作业调度（Quartz）、文件管理、流程引擎等基础功能。

## 文档导航

### 入门

- **[快速开始](./quick-start.md)** — 环境搭建和基础使用
- **[架构设计](./architecture.md)** — 系统架构和设计理念

### 核心功能

- **[用户权限管理](./core-features/user-permission.md)** — 用户、角色、权限体系
- **[数据字典管理](./core-features/data-dict.md)** — 统一的数据字典维护和使用

### API / 前端

- **[组件库](./api/frontend/components.md)** — ProTable、Page 等核心组件
- **[字段组件](./api/frontend/field-components.md)** — 表单字段组件
- **[系统组件](./api/frontend/system-components.md)** — ButtonList、HasPerm、View 系列
- **[工具类](./api/frontend/utils.md)** — HttpUtils、DateUtils 等

### API / 后端

- **[数据规范](./api/backend/data-spec.md)** — Spec 动态查询构建器
- **[工具类](./api/backend/utils.md)** — ExcelTool、JdbcUtils、LoginTool 等
- **[注解](./api/backend/annotations.md)** — ID 生成、作业调度等注解
- **[验证器](./api/backend/validators.md)** — 手机号、身份证等校验注解
- **[作业调度](./api/backend/job.md)** — Quartz 定时任务

### 开发指南

- **[编码规范](./development/coding-standard.md)** — 后端和前端编码标准
- **[最佳实践](./development/best-practices.md)** — 开发最佳实践
- **[智能体使用](./development/agent.md)** — AI 辅助开发指南

## 系统要求

- **JDK 21+** — 后端开发和运行环境
- **MySQL 8.0+** — 数据存储
- **Node.js 18+** — 前端开发环境
- **pnpm 9+** — 前端包管理工具

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | React 19, Ant Design 6, UmiJS 4 |
| 后端 | Java 21, Spring Boot 4.0, JPA (Hibernate), Spring Security, Quartz |
| 数据库 | MySQL 8+ |
| 构建 | Maven (后端), pnpm (前端) |
