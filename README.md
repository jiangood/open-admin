# open-admin

![Maven Central](https://img.shields.io/maven-central/v/io.github.jiangood/open-admin)
![npm](https://img.shields.io/npm/v/@jiangood/open-admin)

open-admin 是一个可嵌入的后台管理系统框架（脚手架），**业务项目通过添加 Maven 和 npm 依赖即可获得完整的后台管理能力**，无需从零搭建用户管理、角色权限、数据字典等功能。

## 快速集成

```xml
<!-- 后端：pom.xml -->
<dependency>
    <groupId>io.github.jiangood</groupId>
    <artifactId>open-admin</artifactId>
    <version>${open-admin.version}</version>
</dependency>
```
前端：package.json
```json
{
  "dependencies": {
    "@jiangood/open-admin": "版本"
  }
}
```

添加依赖后，用户管理、角色权限、数据字典、Quartz 调度、文件管理等功能开箱即用。

## Claude Code 等AI工具集成

在业务项目中使用 Claude Code 创建 CRUD 模块前，请先安装 open-admin skill。在 Claude Code 中执行：

> 请帮我安装 skill，地址为 https://raw.githubusercontent.com/jiangood/open-admin/main/.claude/skills/open-admin.md

安装后，即可使用 `/open-admin` 指令创建业务模块，Claude 会按照框架规范自动生成 Entity → Repository → Service → Controller → 前端页面 → 菜单配置。

## 核心功能

1. **用户权限管理**：完整的用户、角色、权限体系
2. **数据字典管理**：统一的数据字典维护和使用
3. **作业调度**：基于 Quartz 的定时任务管理
4. **文件管理**：统一的文件上传、存储和管理
5. **系统配置**：灵活的系统参数配置
6. **操作日志**：完整的操作日志记录
7. **流程引擎**：内置工作流引擎，支持自定义流程

## 文档

完整文档请参阅 **[docs/index.md](docs/index.md)**：

- **快速开始**：[docs/quick-start.md](docs/quick-start.md) — 环境搭建、基础使用和集成到已有项目
- **架构设计**：[docs/architecture.md](docs/architecture.md) — 系统架构和设计理念
- **API 参考**：前后端组件、工具类、注解
- **开发指南**：编码规范、最佳实践

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | React 19, Ant Design 6, UmiJS 4 |
| 后端 | Java 21, Spring Boot 4.1, JPA (Hibernate), Spring Security, Quartz |
| 数据库 | MySQL 8+ |
| 构建 | Maven (后端), npm (前端) |
