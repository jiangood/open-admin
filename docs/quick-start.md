# 快速开始

## 环境要求

- **JDK 21+**
- **MySQL 8.0+**
- **Node.js 18+**
- **pnpm 9+**

## 项目搭建

### 1. 克隆项目

```bash
git clone https://github.com/jiangood/open-admin.git
cd open-admin
```

### 2. 后端配置

#### 数据库

```sql
CREATE DATABASE open_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `src/main/resources/application.yml` 数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/open_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

#### 启动后端

```bash
# 编译
mvn clean compile

# 启动（开发模式）
mvn spring-boot:run

# 打包
mvn package -DskipTests
java -jar target/open-admin.jar
```

### 3. 前端配置

```bash
cd web
pnpm install
pnpm dev
```

### 4. 访问系统

| 说明 | 地址 |
|------|------|
| 前端 | http://localhost:8000 |
| 后端 API | http://localhost:8080 |
| 默认登录 | admin / open-admin@1234 |

## 创建新模块

### 后端

1. **Entity** — `modules/xxx/entity/` 下创建实体类，继承 `BaseEntity`
2. **Repository** — `modules/xxx/repository/` 下创建 Repository 接口，继承 `BaseRepository`
3. **Service** — `modules/xxx/service/` 下创建 Service，使用构造器注入 Repository
4. **Controller** — `modules/xxx/controller/` 下创建 REST 接口

### 前端

1. 在 `web/src/pages/xxx/` 下创建页面组件
2. 在 `menu-lib.yml` 中配置菜单和权限

## 配置菜单

`src/main/resources/config/menu-lib.yml`:

```yaml
data:
  menus:
    - id: sys-user
      name: 用户管理
      path: /system/user
      icon: UserOutlined
      perms:
        - {name: 查询, code: query}
        - {name: 新增, code: save}
        - {name: 删除, code: delete}
        - {name: 重置密码, code: reset-password}
        - {name: 分配权限, code: grant-permission}
```

## 集成到已有业务项目

open-admin 作为可嵌入框架，可以直接作为依赖引入现有业务项目。

### 后端集成

在业务项目的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>io.github.jiangood</groupId>
    <artifactId>open-admin</artifactId>
    <version>${open-admin.version}</version>
</dependency>
```

然后在 `application.yml` 中配置数据源，项目启动后自动完成系统初始化（建表、默认菜单、管理员账号）。

### 前端集成

在业务项目的 `package.json` 中添加：

```json
{
  "dependencies": {
    "@jiangood/open-admin": "^1.0.0"
  }
}
```

引入后可直接使用 ProTable、字段组件、权限组件等框架组件，套用后台布局即可复用用户管理、角色管理、字典管理等全套管理页面。

### 按需使用

- **只后端不需要前端**：仅添加 Maven 依赖，通过 REST API 访问管理功能
- **只前端不需要后端**：仅添加 npm 依赖，对接自己的后端 API
- **全栈集成**：同时添加前后端依赖，开箱即用

## 常见问题

### 数据库连接失败

检查 MySQL 服务、连接配置和用户权限。

### 前端依赖安装失败

```bash
pnpm install --registry=https://registry.npmmirror.com
```

### 端口被占用

后端默认 8080，前端默认 8000，检查端口占用情况。
