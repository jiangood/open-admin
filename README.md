# springboot-admin-starter

## 1 介绍

`springboot-admin-starter` 是一个小型管理系统框架，为业务项目提供开箱即用的管理后台解决方案。项目集成了后端管理和前端组件，支持快速构建企业级管理系统。

说明后端最新版本为： ![Maven Version](https://img.shields.io/maven-central/v/io.github.jiangood/springboot-admin-starter)

说明前端最新版本为： ![NPM Version](https://img.shields.io/npm/v/@jiangood/springboot-admin-starter)

### 1.1 项目信息

**后端**：
- 项目名称：`springboot-admin-starter`
- 版本：`0.0.2`
- 描述：小型管理系统框架
- Java 版本要求：`17`
- 作者：jiangtao

**前端**：
- 框块名称：`@jiangood/springboot-admin-starter`
- 版本：`0.0.1-beta.30`
- 描述：springboot-admin-starter

### 1.2 开发环境

- Java 17
- Maven
- Node.js
- React 19
- Ant Design 6.x
- MySQL（或其他JPA支持的数据库）

### 1.3 后端依赖

| 依赖名称 | 版本 | 描述 |
|---------|------|-----|
| spring-boot-starter-web | 3.5.8 | Spring Boot Web组件 |
| spring-boot-starter-quartz | 3.5.8 | Spring Boot 任务调度 |
| spring-boot-starter-validation | 3.5.8 | Spring Boot 参数验证 |
| spring-boot-starter-aop | 3.5.8 | Spring Boot 面向切面编程 |
| spring-boot-starter-data-jpa | 3.5.8 | Spring Boot JPA数据访问 |
| spring-boot-starter-cache | 3.5.8 | Spring Boot 缓存支持 |
| spring-boot-starter-security | 3.5.8 | Spring Boot 安全框架 |
| mapstruct | 1.6.3 | 对象映射工具 |
| hutool-all | 5.8.41 | Java工具集 |
| poi-ooxml | 5.5.0 | Excel操作 |
| minio | 8.6.0 | MinIO对象存储客户端 |
| flowable-spring-boot-starter-process | 7.2.0 | 工作流引擎 |

### 1.4 前端依赖

| 依赖名称 | 版本 | 描述 |
|---------|------|-----|
| antd | ^6.0.0 | Ant Design组件库 |
| axios | ^1.13.2 | HTTP客户端 |
| react | ^19.0.0 | React框架 |
| react-dom | ^19.0.0 | React DOM渲染 |
| umi | ^4.0.0 | 前端框架 |

## 2 前端

`@jiangood/springboot-admin-starter` 提供了一套完整的前端组件和工具库，帮助业务项目快速构建管理界面。

### 2.1 组件类

| 名称 | 参数 | 说明 |
|------|------|-----|
| `ProModal` | `title`, `actionRef`, `ref`, `onShow`, `footer`, `width`, `children` | 弹窗组件，用于显示模态框 |
| `Ellipsis` | `length`, `children`, `pre` | 文本省略组件，点击可查看完整文本 |
| `PageLoading` | 无 | 页面加载组件 |
| `NamedIcon` | 无 | 命名图标组件 |
| `LinkButton` | 与`Button`相同 | 链接按钮组件 |
| `Gap` | `gap` | 间隔组件 |
| `Page` | 无 | 页面布局组件 |
| `DownloadFileButton` | `url`, `fileName` | 下载文件按钮 |
| `LoginPage` | 无 | 登录页面组件 |

### 2.2 工具类

| 名称 | 说明 | 举例 |
|------|------|------|
| `StringUtils` | 字符串处理工具 | `StringUtils.capitalize('hello')` |
| `DateUtils` | 日期处理工具 | `DateUtils.format('2024-01-01', 'YYYY-MM-DD')` |
| `ArrUtils` | 数组处理工具 | `ArrUtils.unique([1, 2, 2, 3])` |
| `UrlUtils` | URL处理工具 | `UrlUtils.parseQuery('key=value')` |
| `EventBusUtils` | 事件总线工具 | `EventBusUtils.emit('event', data)` |
| `ColorsUtils` | 颜色处理工具 | `ColorsUtils.random()` |
| `DomUtils` | DOM操作工具 | `DomUtils.addClass(element, 'class')` |
| `UuidUtils` | UUID生成工具 | `UuidUtils.uuid()` |
| `TreeUtils` | 树结构处理工具 | `TreeUtils.buildTree(list)` |
| `StorageUtils` | 存储工具 | `StorageUtils.set('key', 'value')` |
| `DeviceUtils` | 设备检测工具 | `DeviceUtils.isMobile()` |
| `ObjectUtils` | 对象处理工具 | `ObjectUtils.deepClone(obj)` |
| `ValidateUtils` | 验证工具 | `ValidateUtils.isEmail('test@test.com')` |
| `MessageUtils` | 消息提示工具 | `MessageUtils.success('成功')` |

### 2.3 字段组件

| 名称 | 参数 | 说明 |
|------|------|-----|
| `FieldDictSelect` | `value`, `onChange`, `typeCode` | 字典选择组件 |
| `FieldDate` | 与`DatePicker`相同 | 日期选择组件 |
| `FieldDateRange` | 与`RangePicker`相同 | 日期范围选择组件 |
| `FieldBoolean` | `value`, `onChange` | 布尔值选择组件 |
| `FieldPercent` | `value`, `onChange` | 百分比输入组件 |
| `FieldRemoteSelect` | `value`, `onChange`, `queryApi`, `labelField`, `valueField` | 远程选择组件 |
| `FieldRemoteTree` | `queryApi`, `value`, `onChange` | 远程树选择组件 |
| `FieldUploadFile` | `value`, `onChange`, `maxCount`, `accept` | 文件上传组件 |
| `FieldEditor` | `value`, `onChange` | 富文本编辑器组件 |

## 3 后端

### 3.1 菜单列表

框架内置了完整的菜单结构，包括：

- **系统管理** (`sys`)
  - 机构管理 (`sysOrg`)
  - 用户管理 (`sysUser`)
  - 角色管理 (`sysRole`)
  - 操作手册 (`sysManual`)
  - 系统设置 (`sysConfig`)
  - 数据字典 (`sysDict`)
  - 存储文件 (`sysFile`)
  - 作业调度 (`job`)
  - 操作日志 (`sysLog`)
  - 接口管理 (`api`)
  - 流程引擎 (`flowableModel`)
  - 报表管理 (`ureport`)

- **我的任务** (`myFlowableTask`)

### 3.2 业务项目如何配置

#### 3.2.1 添加依赖

```xml
<dependency>
    <groupId>io.github.jiangood</groupId>
    <artifactId>springboot-admin-starter</artifactId>
    <version>0.0.2</version>
</dependency>
```

```javascript
// 前端
npm install @jiangood/springboot-admin-starter
```

#### 3.2.2 配置文件

业务项目需要在 `application-data-biz.yml` 中配置业务相关的数据和菜单。

### 3.3 数据规范

#### 3.3.1 Spec.java

`Spec` 是一个简洁、动态、支持关联字段查询的 JPA Specification 构建器，主要功能包括：

- **链式调用**：支持链式调用收集条件
- **多种查询**：支持 `eq`、`ne`、`like`、`in`、`between` 等查询条件
- **分组统计**：支持 `groupBy`、`having`、聚合函数等
- **关联查询**：支持点操作如 `dept.name`
- **逻辑操作**：支持 `or`、`not` 等逻辑操作

```java
Spec<User> spec = Spec.of()
    .eq("status", 1)
    .like("name", "张三")
    .between("createTime", startDate, endDate);
```

#### 3.3.2 TreeUtils.java

树结构处理工具类，主要功能包括：

- **构建树**：`buildTree` 将列表转换为树结构
- **树遍历**：`walk` 递归处理树节点
- **获取叶子**：`getLeafs` 获取树的所有叶子节点
- **树转列表**：`treeToList` 将树结构转换为列表
- **树转Map**：`treeToMap` 将树结构转换为Map

```java
List<TreeOption> tree = TreeUtils.buildTree(list);
```

#### 3.3.3 RemarkUtils.java

注解处理工具类，用于获取 `@Remark` 注解的值：

- **字段注解**：`getRemark(Field)` 获取字段注解值
- **类注解**：`getRemark(Class<?>)` 获取类注解值
- **枚举注解**：`getRemark(Enum<?>)` 获取枚举注解值
- **方法注解**：`getRemark(Method)` 获取方法注解值

#### 3.3.4 ExcelUtils.java

Excel导入导出工具类，支持：

- **导入**：`importExcel` 从Excel导入数据到实体列表
- **导出**：`exportExcel` 从实体列表导出到Excel

```java
List<User> users = ExcelUtils.importExcel(User.class, inputStream);
```

### 3.4 数据工具

#### 3.4.1 JdbcUtils.java

原生SQL工具类，基于Spring的JdbcTemplate封装，支持：

- **查询操作**：`findOne`、`findAll`、`findMap` 等查询方法
- **更新操作**：`update`、`batchUpdate` 等更新方法
- **动态SQL**：`insert`、`updateById` 等动态构建SQL
- **分页查询**：`findAll(Pageable)` 支持分页查询
- **元数据操作**：`tableExists`、`columnExists` 等检查方法

```java
List<User> users = jdbcUtils.findAll(User.class, "SELECT * FROM user WHERE status = ?", status);
```

#### 3.4.2 ID生成器

框架提供多种ID生成策略：

- **UUID V7**：`@GenerateUuidV7` 按时间排序的UUID
- **前缀序列**：`@GeneratePrefixedSequence` 带前缀的序列ID生成器

```java
@Entity
public class User {
    @GenerateUuidV7
    private String id;
}
```

#### 3.4.3 转换器

提供多种数据转换器：

- **枚举转换器**：`BaseCodeEnumConverter` 枚举值转换
- **集合转换器**：`ToListConverter`、`ToSetConverter` 集合转换
- **Map转换器**：`ToMapConverter`、`ToMapStringObjectConverter` Map转换
- **特殊转换器**：`ToBigDecimalListConverter`、`ToEntryListConverter` 等

### 3.5 验证器

框架提供丰富的验证注解：

- `@ValidateMobile`：手机号验证
- `@ValidateIdNum`：身份证验证
- `@ValidateEmail`：邮箱验证
- `@ValidateCarDrivingLicence`：驾驶证验证
- `@ValidateCreditCode`：统一社会信用代码验证
- `@ValidatePassword`：密码验证
- `@ValidateChineseName`：中文姓名验证
- `@ValidatePlateNumber`：车牌号验证
- `@ValidateDate`：日期验证

### 3.6 业务工具

#### 3.6.1 LoginUtils.java

登录用户工具类，提供当前登录用户信息：

- **获取用户**：`getUser()` 获取当前登录用户
- **获取ID**：`getUserId()` 获取当前用户ID
- **获取角色**：`getRoles()` 获取当前用户角色
- **获取权限**：`getPermissions()` 获取当前用户权限
- **是否管理员**：`isAdmin()` 判断是否为管理员

```java
String userId = LoginUtils.getUserId();
boolean isAdmin = LoginUtils.isAdmin();
```

#### 3.6.2 任务调度

##### BaseJob.java

基础任务类，所有自定义任务需要继承此类：

```java
public class MyJob extends BaseJob {
    @Override
    public String execute(JobDataMap data, Logger logger) throws Exception {
        // 任务执行逻辑
        return "success";
    }
}
```

##### JobDescription.java

任务描述注解，用于定义任务信息：

```java
@JobDescription(label = "我的任务", 
    params = {@FieldDescription(name = "param1", label = "参数1")})
public class MyJob extends BaseJob {
    // 任务实现
}
```

### 3.7 数据配置

#### 3.7.1 application-data-framework.yml

框架核心配置文件，包含：

- **系统配置**：邮件配置、网站配置等
- **菜单配置**：系统菜单结构定义
- **权限配置**：各菜单的权限定义

#### 3.7.2 DataProperties.java

配置属性类，映射 `data` 配置节点：

- **菜单定义**：`menus` 菜单配置列表
- **配置定义**：`configs` 系统配置组列表

### 3.8 工作流程

#### 3.8.1 application-process.yml

工作流程配置文件，定义流程：

- **流程定义**：流程的key、名称、监听器
- **流程变量**：流程中使用的变量定义
- **表单定义**：各节点对应的表单配置

#### 3.8.2 LeaveProcessListener.java

流程监听器示例，实现 `ProcessListener` 接口：

```java
@Component
public class LeaveProcessListener implements ProcessListener {
    @Override
    public void onProcessEvent(FlowableEventType type, String initiator, 
                              String businessKey, Map<String, Object> variables) {
        // 流程事件处理逻辑
    }
}
```

## 4 模板代码

以下是以学生管理为例的通用代码模板：

### 4.1 实体类 (Student.java)

```java
package io.admin.modules.edu.entity;

import io.admin.common.utils.annotation.Remark;
import io.admin.framework.data.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Remark("学生")
@Entity
@Getter
@Setter
@Table
@FieldNameConstants
public class Student extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Remark("姓名")
    @Column(length = 100)
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String name;

    @Remark("年龄")
    private Integer age;

    @Remark("班级")
    @Column(length = 50)
    @Size(max = 50, message = "班级长度不能超过50个字符")
    private String className;

    @Remark("学号")
    @Column(length = 30, unique = true)
    @Size(max = 30, message = "学号长度不能超过30个字符")
    private String studentId;

    @Remark("联系电话")
    @Column(length = 20)
    @Size(max = 20, message = "电话长度不能超过20个字符")
    private String phone;

    @Remark("邮箱")
    @Column(length = 100)
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

}
```

### 4.2 DAO类 (StudentDao.java)

```java
package io.admin.modules.edu.dao;

import io.admin.framework.data.repository.BaseDao;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.edu.entity.Student;
import org.springframework.stereotype.Repository;

@Repository
public class StudentDao extends BaseDao<Student> {

    public Spec<Student> getSearchCondition(String searchText) {
        Spec<Student> spec = Spec.of();
        if (searchText != null && !searchText.isEmpty()) {
            spec.orLike(searchText, Student.Fields.name, Student.Fields.studentId);
        }
        return spec;
    }

}
```

### 4.3 Service类 (StudentService.java)

```java
package io.admin.modules.edu.service;

import io.admin.framework.data.service.BaseService;
import io.admin.modules.edu.dao.StudentDao;
import io.admin.modules.edu.entity.Student;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class StudentService extends BaseService<Student> {

    @Resource
    StudentDao dao;

}
```

### 4.4 Controller类 (StudentController.java)

```java
package io.admin.modules.edu.controller;

import io.admin.common.dto.AjaxResult;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.config.security.HasPermission;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.edu.entity.Student;
import io.admin.modules.edu.service.StudentService;
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
@RequestMapping("admin/student")
public class StudentController {

    @Resource
    StudentService service;

    @HasPermission("student:view")
    @RequestMapping("page")
    public AjaxResult page(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<Student> q = service.dao.getSearchCondition(searchText);
        Page<Student> page = service.findPageByRequest(q, pageable);
        return AjaxResult.ok().data(page);
    }

    @HasPermission("student:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody Student input, RequestBodyKeys updateFields) throws Exception {
        service.saveOrUpdateByRequest(input, updateFields);
        return AjaxResult.ok().msg("保存成功");
    }

    @HasPermission("student:delete")
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteByRequest(id);
        return AjaxResult.ok().msg("删除成功");
    }

}
```

### 4.5 前端页面 (student/index.jsx)

```javascript
import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, Modal, Popconfirm} from 'antd'
import React from 'react'
import {ButtonList, HttpUtils, Page, ProTable} from "../../../framework";

export default class extends React.Component {

    state = {
        formValues: {},
        formOpen: false
    }

    formRef = React.createRef()
    tableRef = React.createRef()

    columns = [
        {
            title: '姓名',
            dataIndex: 'name',
        },
        {
            title: '学号',
            dataIndex: 'studentId',
        },
        {
            title: '年龄',
            dataIndex: 'age',
        },
        {
            title: '班级',
            dataIndex: 'className',
        },
        {
            title: '联系电话',
            dataIndex: 'phone',
        },
        {
            title: '邮箱',
            dataIndex: 'email',
        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='student:save' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='student:delete' title='是否确定删除学生' onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                </ButtonList>
            ),
        },
    ]

    handleAdd = () => {
        this.setState({formOpen: true, formValues: {}})
    }

    handleEdit = record => {
        this.setState({formOpen: true, formValues: record})
    }

    onFinish = values => {
        HttpUtils.post('admin/student/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }

    handleDelete = record => {
        HttpUtils.get('admin/student/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    render() {
        return <Page>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={(params, {selectedRows, selectedRowKeys}) => {
                    return <ButtonList>
                        <Button perm='student:save' type='primary' onClick={this.handleAdd}>
                            <PlusOutlined/> 新增
                        </Button>
                    </ButtonList>
                }}
                request={(params) => HttpUtils.get('admin/student/page', params)}
                columns={this.columns}
            />

            <Modal title='学生信息'
                   open={this.state.formOpen}
                   onOk={() => this.formRef.current.submit()}
                   onCancel={() => this.setState({formOpen: false})}
                   destroyOnHidden
                   maskClosable={false}
            >
                <Form ref={this.formRef} labelCol={{flex: '100px'}}
                      initialValues={this.state.formValues}
                      onFinish={this.onFinish}
                >
                    <Form.Item name='id' noStyle></Form.Item>

                    <Form.Item label='姓名' name='name' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>

                    <Form.Item label='学号' name='studentId' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>

                    <Form.Item label='年龄' name='age'>
                        <Input type="number"/>
                    </Form.Item>

                    <Form.Item label='班级' name='className'>
                        <Input/>
                    </Form.Item>

                    <Form.Item label='联系电话' name='phone'>
                        <Input/>
                    </Form.Item>

                    <Form.Item label='邮箱' name='email'>
                        <Input type="email"/>
                    </Form.Item>

                </Form>
            </Modal>
        </Page>
    }
}
```

### 4.6 数据库配置示例 (application-data-framework.yml)

```yaml
data:
 
  menus:
    - id: student
      name: 学生管理
      path: /edu/student
      icon: UserOutlined
      perms:
        - name: 查看
          perm: student:view
        - name: 保存
          perm: student:save
        - name: 删除
          perm: student:delete
```