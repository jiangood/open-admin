# open-admin 文档

本文档是 open-admin 项目的官方文档，包含了项目的详细说明、使用指南和开发规范。

## 文档导航

### 项目概述

- **[架构设计](./architecture.md)** - 系统架构和设计理念

### 入门指南

- **[快速开始](./quick-start.md)** - 项目环境搭建和基础使用

### 核心功能

- **[用户权限管理](./core-features/user-permission.md)** - 用户、角色、权限体系
- **[数据字典管理](./core-features/data-dict.md)** - 统一的数据字典维护和使用
- **[作业调度](./core-features/job-schedule.md)** - 基于Quartz的定时任务管理
- **[文件管理](./core-features/file-management.md)** - 统一的文件上传、存储和管理
- **[系统配置](./core-features/system-config.md)** - 灵活的系统参数配置
- **[操作日志](./core-features/operation-log.md)** - 完整的操作日志记录

### API 文档

#### 前端 API

- **[组件库](./api/frontend/components.md)** - 前端核心组件使用指南
- **[字段组件](./api/frontend/field-components.md)** - 表单字段组件使用指南
- **[系统组件](./api/frontend/system-components.md)** - 系统级组件使用指南
- **[工具类](./api/frontend/utils.md)** - 前端工具类使用指南

#### 后端 API

- **[数据规范](./api/backend/data-spec.md)** - 数据查询和操作规范
- **[工具类](./api/backend/utils.md)** - 后端核心工具类使用指南
- **[注解](./api/backend/annotations.md)** - 后端注解使用指南
- **[验证器](./api/backend/validators.md)** - 数据验证器使用指南
- **[作业调度](./api/backend/job.md)** - 作业调度相关API

### 开发指南

- **[编码规范](./development/coding-standard.md)** - 后端和前端编码规范
- **[智能体使用](./development/agent.md)** - 智能体配置和使用指南
- **[最佳实践](./development/best-practices.md)** - 开发最佳实践

## 系统要求

- **JDK 21+**：后端开发和运行环境
- **MySQL 8.0+**：数据存储
- **Node.js 16+**：前端开发环境
- **npm 7+**：前端包管理工具

## 核心功能

1. **用户权限管理**：完整的用户、角色、权限体系
2. **数据字典管理**：统一的数据字典维护和使用
3. **作业调度**：基于Quartz的定时任务管理
4. **文件管理**：统一的文件上传、存储和管理
5. **系统配置**：灵活的系统参数配置
6. **操作日志**：完整的操作日志记录

## 技术栈

- **前端**：React、Ant Design
- **后端**：Java、Spring Boot、JPA、Quartz
- **数据库**：MySQL

## 联系我们

如果您有任何问题或建议，欢迎联系我们：

- GitHub：https://github.com/jiangood/open-admin
- 文档：https://jiangood.github.io/open-admin