# 首页

## 引用方式

### Maven

项目名称：`springboot-admin-starter`

引用方式：

```xml
<dependency>
    <groupId>io.admin</groupId>
    <artifactId>springboot-admin-starter</artifactId>
    <version>0.0.1-beta.30</version>
</dependency>
```

版本：[![Maven Central](https://img.shields.io/badge/maven-0.0.1--beta.30-blue.svg)](https://search.maven.org/artifact/io.admin/springboot-admin-starter/0.0.1-beta.30/jar)

### NPM

项目名称：`@jiangood/springboot-admin-starter`

引用方式：

```bash
npm install @jiangood/springboot-admin-starter@0.0.1-beta.30
```

版本：[![npm version](https://img.shields.io/badge/npm-0.0.1--beta.30-blue.svg)](https://www.npmjs.com/package/@jiangood/springboot-admin-starter/v/0.0.1-beta.30)

## 开发环境

| 模块   | 开发环境        | 文档链接 |
| :----- | :-------------- | :------- |
| 后端   | Java, Maven     | [后端模块](back.md) |
| 前端   | Node.js, NPM, React | [前端模块](front.md) |

## 项目依赖

### 后端

`src/main/resources/application-data-framework.yml` 文件定义了框架的核心配置项和菜单结构，业务项目需在 `application-data-biz.yml` 中进行配置。

### 前端

前端主要依赖 `preact` 和 `preact-hooks`。
