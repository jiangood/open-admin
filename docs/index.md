# 首页

## 开发环境
- Maven 版本: 0.0.2 ![Maven Version](https://img.shields.io/maven-central/v/io.github.jiangood/springboot-admin-starter)
- NPM 版本: 0.0.1-beta.30 ![NPM Version](https://img.shields.io/npm/v/springboot-admin-starter)

## 后端依赖

以下是后端项目的主要依赖：

-   `org.springframework.boot:spring-boot-starter-web` (Web 应用启动器)
-   `org.springframework.boot:spring-boot-starter-quartz` (任务调度)
-   `org.springframework.boot:spring-boot-starter-validation` (数据验证)
-   `org.springframework.boot:spring-boot-starter-aop` (面向切面编程)
-   `org.springframework.boot:spring-boot-starter-data-jpa` (JPA 数据访问)
-   `org.springframework.boot:spring-boot-starter-cache` (缓存支持)
-   `org.springframework.boot:spring-boot-starter-security` (安全框架)
-   `org.springframework.boot:spring-boot-configuration-processor` (配置处理器)
-   `org.mapstruct:mapstruct` (Java Bean 映射)
-   `com.jhlabs:filters` (图像处理滤镜)
-   `io.minio:minio` (MinIO 存储客户端)
-   `com.squareup.okhttp3:okhttp-jvm` (HTTP 客户端)
-   `javax.mail:mail` (邮件服务)
-   `org.apache.poi:poi-ooxml` (Excel 文件操作)
-   `org.apache.poi:poi-scratchpad` (旧版 Office 文件操作)
-   `com.itextpdf:itextpdf` (PDF 文档处理)
-   `com.github.f4b6a3:uuid-creator` (UUID 生成器，主要用于生成UUID7)
-   `commons-dbutils:commons-dbutils` (数据库工具)
-   `cn.hutool:hutool-all` (Hutool 工具库)
-   `org.apache.commons:commons-lang3` (Apache 通用工具类)
-   `com.google.guava:guava` (Google Guava 常用工具)
-   `commons-io:commons-io` (Apache 通用 IO 工具)
-   `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` (YAML 数据格式支持)
-   `commons-beanutils:commons-beanutils` (Bean 工具类)
-   `com.belerweb:pinyin4j` (汉字转拼音)
-   `org.jsoup:jsoup` (HTML 解析器)
-   `org.projectlombok:lombok` (简化 Java 代码)
-   `com.mysql:mysql-connector-j` (MySQL 数据库连接驱动)
-   `io.github.jiangood:ureport-console` (报表控制台)
-   `org.flowable:flowable-spring-boot-starter-process` (Flowable 流程引擎启动器)

## 前端依赖

以下是前端项目的主要 `peerDependencies`：

-   `@ant-design/icons: ^5.4.0` (Ant Design 图标库)
-   `@bpmn-io/properties-panel: ^3.34.0` (BPMN.io 属性面板)
-   `@tinymce/tinymce-react: ^6.0.0` (TinyMCE 富文本编辑器 React 封装)
-   `@umijs/types: ^3.5.43` (UmiJS 类型定义)
-   `antd: ^6.0.0` (Ant Design UI 组件库)
-   `antd-img-crop: ^4.23.0` (Ant Design 图片裁剪)
-   `axios: ^1.13.2` (HTTP 客户端)
-   `bpmn-js: ^18.7.0` (BPMN.js 核心库)
-   `bpmn-js-properties-panel: ^5.43.0` (BPMN.js 属性面板)
-   `preact: ^10.27.2` (React 替代方案)
-   `dayjs: ^1.11.13` (日期处理库)
-   `jsencrypt: ^3.5.4` (RSA 加密)
-   `lodash: ^4.17.21` (JavaScript 工具库)
-   `qs: ^6.14.0` (URL 参数解析/序列化)
-   `react: ^19.0.0` (React 核心库)
-   `react-dom: ^19.0.0` (React DOM 渲染)
-   `umi: ^4.0.0` (UmiJS 框架)

## 菜单列表

系统菜单结构主要通过 `src/main/resources/application-data-framework.yml` 进行配置。以下是一些示例菜单项：

-   **我的任务**:
    -   ID: `myFlowableTask`
    -   路径: `/flowable/task`
    -   说明: 用户流程任务管理。
    -   权限: `myFlowableTask:manage` (任务管理)
-   **系统管理**:
    -   ID: `sys`
    -   说明: 包含多个子系统的管理功能。
    -   子菜单示例:
        -   **机构管理**: ID `sysOrg`, 路径 `/system/org`, 图标 `ApartmentOutlined`, 权限 (`sysOrg:save`, `sysOrg:view`, `sysOrg:delete`)。
        -   **用户管理**: ID `sysUser`, 路径 `/system/user`, 图标 `UserOutlined`, 权限 (`sysUser:view`, `sysUser:save`, `sysUser:delete`, `sysUser:resetPwd`, `sysUser:grantPerm`)。
        -   **系统配置**: ID `sysConfig`, 路径 `/system/config`, 图标 `SettingOutlined`, 权限 (`sysConfig:view`, `sysConfig:save`)。
        -   更多子菜单如角色管理、数据字典、存储文件等。

## 业务项目如何配置

业务项目可以在 `src/main/resources/application-data-biz.yml` 中配置自定义的菜单和配置项。
配置项的结构与 `application-data-framework.yml` 中的 `data.configs` 部分保持一致，例如：

```yaml
data:
  configs:
    - group-name: 我的自定义配置
      children:
        - code: my.custom.setting
          name: 自定义设置项
          value: my_value
          description: 这是一个示例自定义设置项
```

## 其他文档

-   [前端模块](front.md)
-   [后端模块](back.md)