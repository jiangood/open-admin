# 最佳实践

本文档介绍了 open-admin 框架的最佳实践，帮助开发者更好地使用和扩展框架功能。

## 后端开发最佳实践

### 1. 项目结构

**推荐的项目结构**：

```
src/main/java/io/github/jiangood/openadmin/modules/
├── system/           # 系统模块
│   ├── entity/       # 实体类
│   ├── dao/          # 数据访问层
│   ├── service/      # 业务逻辑层
│   └── controller/   # 控制器层
├── business/         # 业务模块
│   ├── entity/
│   ├── dao/
│   ├── service/
│   └── controller/
└── job/              # 作业调度模块
    ├── entity/
    ├── dao/
    ├── service/
    └── controller/
```

### 2. 实体类设计

**推荐做法**：

- 继承 `BaseEntity` 类，获得基础字段和方法
- 使用 `@Remark` 注解为类和字段添加注释
- 合理使用 JPA 注解定义数据库映射
- 为需要验证的字段添加验证注解

**示例**：

```java
import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.lang.annotation.Remark;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Remark("用户信息")
@Entity
@Getter
@Setter
@FieldNameConstants
public class User extends BaseEntity {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    @Remark("用户名")
    @Column(length = 50, nullable = false)
    private String username;

    @Remark("邮箱")
    @Column(length = 100)
    private String email;

    @Remark("手机号")
    @Column(length = 20)
    private String phone;
}
```

### 3. 数据访问层

**推荐做法**：

- 继承 `BaseDao` 类，获得基础查询方法
- 使用 `Spec` 构建复杂查询条件
- 对于复杂查询，使用 `JdbcUtils` 执行原生 SQL

**示例**：

```java
import io.github.jiangood.openadmin.framework.data.BaseDao;
import io.github.jiangood.openadmin.modules.system.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao extends BaseDao<User> {
    // 可以添加自定义查询方法
}
```

### 4. 业务逻辑层

**推荐做法**：

- 继承 `BaseService` 类，获得基础业务方法
- 使用 `@Resource` 注入 DAO 实例
- 实现业务逻辑，处理业务异常
- 使用事务注解管理事务

**示例**：

```java
import io.github.jiangood.openadmin.framework.data.BaseService;
import io.github.jiangood.openadmin.modules.system.dao.UserDao;
import io.github.jiangood.openadmin.modules.system.entity.User;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService extends BaseService<User> {

    @Resource
    private UserDao userDao;

    @Transactional
    public User saveUser(User user) {
        // 业务逻辑
        return userDao.save(user);
    }
}
```

### 5. 控制器层

**推荐做法**：

- 使用 `@RestController` 注解
- 使用 `@RequestMapping` 定义 API 路径
- 使用 `@HasPermission` 注解进行权限控制
- 使用 `Spec` 构建查询条件
- 使用 `AjaxResult` 统一返回格式

**示例**：

```java
import io.github.jiangood.openadmin.lang.dto.AjaxResult;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.Spec;
import io.github.jiangood.openadmin.modules.system.entity.User;
import io.github.jiangood.openadmin.modules.system.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/user")
public class UserController {

    @Resource
    private UserService userService;

    @HasPermission("user:view")
    @RequestMapping("page")
    public AjaxResult page(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) {
        Spec<User> q = Spec.<User>of().orLike(searchText, "username", "name", "email");
        Page<User> page = userService.findAllByUserAction(q, pageable);
        return AjaxResult.ok().data(page);
    }

    @HasPermission("user:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody User user) {
        userService.saveOrUpdateByUserAction(user);
        return AjaxResult.ok().msg("保存成功");
    }
}
```

### 6. 作业调度

**推荐做法**：

- 继承 `BaseJob` 类，实现 `execute` 方法
- 使用 `@JobDescription` 注解定义作业信息和参数
- 处理异常，确保作业能够正常完成
- 合理设置调度频率

**示例**：

```java
import io.github.jiangood.openadmin.modules.job.BaseJob;
import io.github.jiangood.openadmin.modules.job.JobDescription;
import io.github.jiangood.openadmin.lang.field.FieldDescription;
import org.quartz.JobDataMap;
import org.slf4j.Logger;

@JobDescription(label = "数据同步作业", params = {
    @FieldDescription(name = "syncType", label = "同步类型")
})
public class DataSyncJob extends BaseJob {
    @Override
    public String execute(JobDataMap data, Logger logger) throws Exception {
        logger.info("开始执行数据同步作业");
        try {
            // 同步逻辑
            return "同步成功";
        } catch (Exception e) {
            logger.error("同步失败", e);
            return "同步失败: " + e.getMessage();
        }
    }
}
```

## 前端开发最佳实践

### 1. 页面结构

**推荐的页面结构**：

```
web/src/pages/
├── system/
│   ├── user/        # 用户管理页面
│   │   └── index.jsx
│   ├── role/        # 角色管理页面
│   │   └── index.jsx
│   └── org/         # 组织管理页面
│       └── index.jsx
└── business/        # 业务页面
    ├── order/       # 订单管理页面
    │   └── index.jsx
    └── product/     # 产品管理页面
        └── index.jsx
```

### 2. 组件使用

**推荐做法**：

- 使用 `Page` 组件作为页面容器
- 使用 `ProTable` 组件展示数据列表
- 使用 `ButtonList` 组件管理操作按钮
- 使用 `Field` 系列组件构建表单
- 使用权限控制组件控制按钮显示

**示例**：

```jsx
import React from 'react';
import {Button, Form, Input, Modal, Popconfirm} from 'antd';
import {PlusOutlined} from '@ant-design/icons';
import {ButtonList, HttpUtils, Page, ProTable, FieldDictSelect} from "@jiangood/open-admin";

export default class UserPage extends React.Component {

    state = {
        formValues: {},
        formOpen: false
    }

    formRef = React.createRef();
    tableRef = React.createRef();

    columns = [
        {
            title: '用户名',
            dataIndex: 'username',
        },
        {
            title: '姓名',
            dataIndex: 'name',
        },
        {
            title: '状态',
            dataIndex: 'status',
            render: status => {
                return status === 1 ? '启用' : '禁用';
            }
        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='user:save' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='user:delete' title='是否确定删除用户' onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                </ButtonList>
            ),
        },
    ]

    handleAdd = () => {
        this.setState({formOpen: true, formValues: {}});
    }

    handleEdit = record => {
        this.setState({formOpen: true, formValues: record});
    }

    handleSave = values => {
        HttpUtils.post('admin/user/save', values).then(() => {
            this.setState({formOpen: false});
            this.tableRef.current.reload();
        });
    }

    handleDelete = record => {
        HttpUtils.get('admin/user/delete', {id: record.id}).then(() => {
            this.tableRef.current.reload();
        });
    }

    render() {
        return (
            <Page padding={true}>
                <ProTable
                    actionRef={this.tableRef}
                    toolBarRender={() => (
                        <ButtonList>
                            <Button perm='user:save' type='primary' onClick={this.handleAdd}>
                                <PlusOutlined/> 新增
                            </Button>
                        </ButtonList>
                    )}
                    request={(params) => HttpUtils.get('admin/user/page', params)}
                    columns={this.columns}
                />

                <Modal 
                    title='用户信息' 
                    open={this.state.formOpen}
                    onOk={() => this.formRef.current.submit()}
                    onCancel={() => this.setState({formOpen: false})}
                    destroyOnClose
                >
                    <Form 
                        ref={this.formRef} 
                        initialValues={this.state.formValues}
                        onFinish={this.handleSave}
                    >
                        <Form.Item name='id' noStyle></Form.Item>
                        <Form.Item label='用户名' name='username' rules={[{required: true}]}>
                            <Input/>
                        </Form.Item>
                        <Form.Item label='姓名' name='name' rules={[{required: true}]}>
                            <Input/>
                        </Form.Item>
                        <Form.Item label='状态' name='status'>
                            <FieldDictSelect dict='user_status'/>
                        </Form.Item>
                    </Form>
                </Modal>
            </Page>
        );
    }
}
```

### 3. 表单设计

**推荐做法**：

- 使用 Ant Design 的 `Form` 组件
- 使用 `Field` 系列组件构建表单项
- 合理设置表单验证规则
- 使用 `initialValues` 设置初始值

**示例**：

```jsx
<Form
    ref={this.formRef}
    initialValues={this.state.formValues}
    onFinish={this.handleSave}
>
    <Form.Item name='id' noStyle></Form.Item>
    
    <Form.Item label='名称' name='name' rules={[{required: true}]}>
        <Input/>
    </Form.Item>
    
    <Form.Item label='状态' name='status'>
        <FieldDictSelect dict='status'/>
    </Form.Item>
    
    <Form.Item label='所属部门' name='deptId'>
        <FieldSysOrgTreeSelect type='dept'/>
    </Form.Item>
    
    <Form.Item label='创建时间' name='createTime'>
        <FieldDate/>
    </Form.Item>
</Form>
```

### 4. 数据请求

**推荐做法**：

- 使用 `HttpUtils` 发送请求
- 处理请求结果和错误
- 使用异步/await 语法
- 合理设置请求参数

**示例**：

```jsx
import {HttpUtils} from "@jiangood/open-admin";

// GET 请求
const getUserList = async () => {
    try {
        const data = await HttpUtils.get('admin/user/page', {page: 1, size: 10});
        console.log(data);
    } catch (error) {
        console.error('获取用户列表失败', error);
    }
};

// POST 请求
const saveUser = async (user) => {
    try {
        const result = await HttpUtils.post('admin/user/save', user);
        console.log('保存成功', result);
    } catch (error) {
        console.error('保存失败', error);
    }
};
```

### 5. 权限控制

**推荐做法**：

- 在按钮上使用 `perm` 属性
- 使用 `ButtonList` 组件管理按钮权限
- 使用 `HasPerm` 组件控制页面元素显示
- 后端同时进行权限验证

**示例**：

```jsx
import {ButtonList, HasPerm} from "@jiangood/open-admin";
import {Button} from "antd";

// 在按钮上使用 perm 属性
<Button perm='user:save' type='primary' onClick={this.handleAdd}>
    新增用户
</Button>

// 使用 ButtonList 组件
<ButtonList>
    <Button perm='user:save' onClick={this.handleEdit}>编辑</Button>
    <Button perm='user:delete' onClick={this.handleDelete}>删除</Button>
    <Button perm='user:export' onClick={this.handleExport}>导出</Button>
</ButtonList>

// 使用 HasPerm 组件
<HasPerm perm="user:manage">
    <div>
        <h3>用户管理高级功能</h3>
        <Button onClick={this.handleImport}>导入用户</Button>
        <Button onClick={this.handleSync}>同步用户</Button>
    </div>
</HasPerm>
```

## 系统配置最佳实践

### 1. 数据库配置

**推荐做法**：

- 使用配置文件管理数据库连接
- 不同环境使用不同配置文件
- 合理设置连接池参数

**示例**：

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/open_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

### 2. 菜单配置

**推荐做法**：

- 在 `application-data.yml` 中配置菜单
- 合理设置菜单层级和权限
- 使用 Ant Design 图标

**示例**：

```yaml
# application-data.yml
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
          perm-names: [ 列表,保存,删除 ]
          perm-codes: [ user:view,user:save,user:delete ]
        - id: role
          name: 角色管理
          path: /system/role
          icon: IdcardOutlined
          perm-names: [ 管理 ]
          perm-codes: [ role:manage ]
```

### 3. 作业配置

**推荐做法**：

- 合理设置作业调度表达式
- 为作业添加详细描述
- 监控作业执行状态

**示例**：

```yaml
# 作业调度表达式示例
# 每天凌晨1点执行
0 0 1 * * ?

# 每5分钟执行一次
0 0/5 * * * ?

# 每周一早上8点执行
0 0 8 ? * MON
```

## 性能优化最佳实践

### 1. 后端优化

- 使用缓存减少数据库访问
- 合理使用索引
- 避免大事务
- 使用异步处理耗时操作
- 优化查询语句，避免全表扫描

### 2. 前端优化

- 使用 `React.memo` 优化组件渲染
- 使用 `useCallback` 和 `useMemo` 缓存函数和计算结果
- 避免在渲染过程中创建新的函数和对象
- 使用虚拟列表处理长列表
- 合理使用分页，避免一次性加载大量数据

### 3. 系统优化

- 合理设置 JVM 参数
- 优化数据库配置
- 使用 CDN 加速静态资源访问
- 启用 GZIP 压缩
- 合理配置服务器资源

## 安全最佳实践

### 1. 后端安全

- 使用 HTTPS 协议
- 实现 CSRF 防护
- 密码加密存储
- 合理设置权限控制
- 输入验证和参数校验
- 防止 SQL 注入和 XSS 攻击

### 2. 前端安全

- 避免在前端存储敏感信息
- 实现前端权限控制
- 输入验证和 XSS 防护
- 合理处理错误信息，避免泄露系统细节
- 使用安全的 API 调用方式

## 部署最佳实践

### 1. 环境准备

- 准备 JDK 17+ 环境
- 准备 MySQL 8.0+ 数据库
- 准备 Nginx 或其他 Web 服务器

### 2. 构建与部署

**后端部署**：

```bash
# 构建项目
./mvnw clean package -DskipTests

# 部署到服务器
scp target/open-admin.jar user@server:/path/to/deploy/

# 启动服务
java -jar open-admin.jar
```

**前端部署**：

```bash
# 构建项目
cd web
npm run build

# 部署到服务器
scp -r dist/* user@server:/path/to/web/
```

### 3. 配置 Nginx

**示例配置**：

```nginx
server {
    listen 80;
    server_name example.com;

    # 前端静态资源
    location / {
        root /path/to/web;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

## 监控与维护

### 1. 日志管理

- 合理配置日志级别
- 定期清理日志文件
- 使用 ELK 等工具集中管理日志

### 2. 监控系统

- 监控服务器资源使用情况
- 监控数据库性能
- 监控应用服务状态
- 设置告警机制

### 3. 定期维护

- 定期备份数据库
- 定期更新系统和依赖
- 定期检查系统性能
- 定期清理无用数据

## 总结

遵循这些最佳实践，可以帮助开发者更好地使用和扩展 open-admin 框架，提高开发效率和系统质量。同时，根据具体的业务需求和技术栈，可以适当调整和扩展这些实践，以适应不同的项目场景。