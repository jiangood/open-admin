# open-admin 文档

本文档是 open-admin 项目的官方文档，包含了项目的详细说明、使用指南和开发规范。

## 文档导航

### 项目概述


### 入门指南

- **[快速开始](./quick-start.md)** - 项目环境搭建和基础使用
- **[架构设计](./architecture.md)** - 系统架构和设计理念

### 前端文档

- **[组件库](./frontend/components.md)** - 前端核心组件使用指南
- **[字段组件](./frontend/field-components.md)** - 表单字段组件使用指南
- **[工具类](./frontend/utils.md)** - 前端工具类使用指南

### 后端文档

- **[数据规范](./backend/data-spec.md)** - 数据查询和操作规范
- **[工具类](./backend/utils.md)** - 后端核心工具类使用指南
- **[注解](./backend/annotations.md)** - 后端注解使用指南
- **[验证器](./backend/validators.md)** - 数据验证器使用指南

### 开发指南

- **[编码规范](./guide/coding-standard.md)** - 后端和前端编码规范
- **[智能体使用](./guide/agent.md)** - 智能体配置和使用指南

## 系统要求

- **JDK 17+**：后端开发和运行环境
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
7. **报表管理**：集成UReport报表引擎
8. **流程引擎**：内置工作流引擎，支持自定义流程

## 技术栈

- **前端**：React、Ant Design、UmiJS
- **后端**：Java、Spring Boot、JPA、Quartz
- **数据库**：MySQL
- **流程引擎**：内置工作流引擎
- **报表引擎**：UReport

## 联系我们

如果您有任何问题或建议，欢迎联系我们：

- GitHub：https://github.com/jiangood/open-admin
- 文档：https://jiangood.github.io/open-admin