# 最佳实践

## 后端

### 实体类

- 继承 `BaseEntity` 获得 id、createTime、updateTime 等通用字段
- 使用 `@Remark` 为类和字段添加中文注释
- 使用 `@FieldNameConstants` 生成字段常量，便于 Spec 查询引用

```java
@Remark("用户信息")
@Entity
@Getter
@Setter
@FieldNameConstants
public class User extends BaseEntity {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50)
    @Remark("用户名")
    @Column(length = 50, nullable = false)
    private String username;
}
```

### 数据访问

- Repository 继承 `BaseRepository`，获得通用 CRUD
- 复杂查询使用 `Spec` 构建
- 原生 SQL 使用 `DbTool`

### 作业调度

- 继承 `BaseJob`，实现 `execute` 方法
- 使用 `@JobDescription` 定义作业信息
- 处理异常，确保作业不中断

## 前端

### 页面结构

```
web/src/pages/system/user/index.jsx
web/src/pages/system/role/index.jsx
```

### 推荐模式

- 使用 `Page` 作为容器
- 使用 `ProTable` 展示列表
- 使用 `ButtonList` 管理操作按钮
- 使用 `Field` 系列组件构建表单

## 系统配置

### 数据库

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/open_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

### 菜单配置

```yaml
data:
  menus:
    - id: system
      name: 系统管理
      icon: SettingOutlined
      children:
        - id: user
          name: 用户管理
          path: /system/user
          icon: UserOutlined
          perms:
            - {name: 查询, code: query}
            - {name: 新增, code: save}
            - {name: 删除, code: delete}
```

## 部署

```bash
# 后端
mvn package -DskipTests
java -jar target/open-admin.jar

# 前端
cd web && pnpm build
# 部署 dist/ 目录到 Web 服务器
```

### Nginx 配置

```nginx
server {
    listen 80;
    server_name example.com;

    location / {
        root /path/to/web;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
    }
}
```

## 安全

- 密码加密存储
- 输入验证和参数校验
- 前后端双重权限控制
- 避免在前端存储敏感信息
