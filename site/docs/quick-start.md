# 快速开始

本指南将帮助你快速搭建 open-admin 项目环境并开始使用。

## 环境要求

在开始之前，请确保你的系统满足以下要求：

- **JDK 17+**：后端开发和运行环境
- **MySQL 8.0+**：数据存储
- **Node.js 16+**：前端开发环境
- **npm 7+**：前端包管理工具

## 项目搭建

### 1. 克隆项目

```bash
git clone https://github.com/jiangood/open-admin.git
cd open-admin
```

### 2. 后端环境配置

#### 2.1 数据库配置

1. 创建 MySQL 数据库：

```sql
CREATE DATABASE open_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `src/main/resources/application.yml` 中的数据库连接配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/open_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

#### 2.2 启动后端服务

```bash
# 编译项目
./mvnw clean package -DskipTests

# 启动服务
java -jar target/open-admin.jar
```

### 3. 前端环境配置

#### 3.1 安装依赖

```bash
cd web
npm install
```

#### 3.2 启动前端开发服务器

```bash
npm run dev
```

### 4. 访问系统

1. 前端地址：`http://localhost:8000`
2. 后端API地址：`http://localhost:8080`
3. 默认登录账号：
   - 用户名：admin
   - 密码：123456

## 基础使用

### 1. 创建新模块

#### 1.1 后端开发

1. 创建实体类（如 `User.java`），存储路径：`src/main/java/io/github/jiangood/openadmin/modules/xxx/entity`
2. 创建 DAO 接口（如 `UserDao.java`），存储路径：`src/main/java/io/github/jiangood/openadmin/modules/xxx/dao`
3. 创建 Service 类（如 `UserService.java`），存储路径：`src/main/java/io/github/jiangood/openadmin/modules/xxx/service`
4. 创建 Controller 类（如 `UserController.java`），存储路径：`src/main/java/io/github/jiangood/openadmin/modules/xxx/controller`

详细步骤请参考 [后端开发指南](./backend/)。

#### 1.2 前端开发

1. 创建页面组件（如 `user/index.jsx`），存储路径：`web/src/pages/xxx/index.jsx`
2. 配置路由和菜单

详细步骤请参考 [前端开发指南](./frontend/)。

### 2. 配置菜单

在 `src/main/resources/config/application-data.yml` 中配置系统菜单：

```yaml
data:
  menus:
    - id: sysUser
      name: 用户管理
      path: /system/user
      icon: UserOutlined
      perm-names: [ 列表,保存,删除,重置密码,授权数据 ]
      perm-codes: [ sysUser:view,sysUser:save,sysUser:delete,sysUser:resetPwd,sysUser:grantPerm ]
    - id: sysRole
      name: 角色管理
      path: /system/role
      icon: IdcardOutlined
      perm-names: [ 管理 ]
      perm-codes: [ sysRole:manage ]
```

### 3. 配置流程

在 `application-process.yml` 中配置流程定义：

```yaml
process:
  list:
    - key: "leave_request"
      name: "请假流程"
      listener: io.github.jiangood.openadmin.LeaveProcessListener
      variables:
        - name: "days"
          label: "请假天数"
          value-type: number
          required: true
        - name: "reason"
          label: "请假原因"
          required: true
      forms:
        - key: "start_form"
          label: "请假申请表"
        - key: "manager_approve_form"
          label: "经理审批表"
        - key: "finish_view"
          label: "流程结果查看"
```

## 技术栈

### 前端技术

- **React**：19.0.0+
- **Ant Design**：6.0.0+
- **Umi**：4.0.0+
- **@ant-design/icons**：6.0.0+
- **dayjs**：1.11.13+
- **axios**：1.13.2+

### 后端技术

- **Spring Boot**：3.5.10+
- **JPA**：内置
- **Quartz**：内置
- **MySQL**：8.0+
- **Hutool**：5.8.43+
- **Apache POI**：5.5.0+

## 常见问题

### 1. 数据库连接失败

- 检查 MySQL 服务是否启动
- 检查数据库连接配置是否正确
- 检查数据库用户权限是否足够

### 2. 前端依赖安装失败

- 检查 Node.js 版本是否符合要求
- 尝试使用 `npm install --registry=https://registry.npmmirror.com` 加速安装

### 3. 服务启动失败

- 检查端口是否被占用
- 检查日志文件中的错误信息

## 下一步

- 了解 [系统架构](./architecture.md)
- 学习 [前端开发](./frontend/)
- 学习 [后端开发](./backend/)
- 参考 [开发指南](./guide/)