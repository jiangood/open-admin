# SpringBoot Admin Starter 开发文档

## 目录
- [1 介绍](#1-介绍)
- [2 前端](#2-前端)
- [3 后端](#3-后端)
- [4 模板代码](#4-模板代码)

---

## 1 介绍

**SpringBoot Admin Starter** 是一个轻量级的管理系统框架，旨在为业务项目提供快速搭建管理后台的能力。该框架提供了完整的前后端解决方案，集成了一系列常用的管理功能，如用户管理、角色管理、作业调度、流程引擎等。

### 开发环境
- **后端**：Java 17+, Spring Boot 3.5.8+, Maven
- **前端**：React 19+, TypeScript, Umi 4, Ant Design 6.x
- **数据库**：MySQL 8.0+, H2 (测试)
- **其他**：Node.js, npm

### 版本信息
- **后端最新版本**：![Maven Version](https://img.shields.io/maven-central/v/io.github.jiangood/springboot-admin-starter)
- **前端最新版本**：![NPM Version](https://img.shields.io/npm/v/@jiangood/springboot-admin-starter)

### 依赖信息

#### 后端依赖
| 依赖项 | 版本 | 描述 |
|--------|------|------|
| spring-boot-starter-web | 3.5.8 | Spring Boot Web 模块 |
| spring-boot-starter-quartz | 3.5.8 | Spring Boot 定时任务模块 |
| spring-boot-starter-data-jpa | 3.5.8 | JPA 数据访问模块 |
| spring-boot-starter-security | 3.5.8 | Spring Security 安全模块 |
| hutool-all | 5.8.41 | Hutool 工具类 |
| poi-ooxml | 5.5.0 | Apache POI Excel 导入导出 |
| flowable-spring-boot-starter-process | 7.2.0 | Flowable 工作流引擎 |
| mapstruct | 1.6.3 | 对象映射工具 |
| uuid-creator | 6.1.1 | UUID 生成器 |
| minio | 8.6.0 | MinIO 对象存储客户端 |

#### 前端依赖
| 依赖项 | 版本 | 描述 |
|--------|------|------|
| antd | ^6.0.0 | Ant Design UI 组件库 |
| axios | ^1.13.2 | HTTP 客户端 |
| react | ^19.0.0 | React 框架 |
| umi | ^4.0.0 | Umi 前端框架 |
| @tinymce/tinymce-react | ^6.0.0 | TinyMCE 富文本编辑器 |
| bpmn-js | ^18.7.0 | BPMN 流程图工具 |

---

## 2 前端

前端框架提供了丰富的组件和工具，用于快速构建管理后台界面。主要包含以下内容：

### 核心组件

- **ProTable**：增强型表格组件，支持分页、筛选、搜索、多选等功能
- **ProModal**：增强型模态框组件，用于表单弹窗
- **Page**：页面布局组件
- **DownloadFileButton**：文件下载按钮
- **Ellipsis**：文本省略组件
- **LinkButton**：链接按钮
- **NamedIcon**：命名图标组件
- **Gap**：间距组件
- **ValueType**：值类型组件
- **FieldUploadFile**：文件上传组件
- **ButtonList**：按钮列表组件

### 工具类

前端框架提供了丰富的工具类：

- **DateUtils**：日期处理工具
- **ArrUtils**：数组处理工具
- **UrlUtils**：URL处理工具
- **StringUtils**：字符串处理工具
- **EventBusUtils**：事件总线工具
- **ColorsUtils**：颜色处理工具
- **DomUtils**：DOM操作工具
- **UuidUtils**：UUID生成工具
- **TreeUtils**：树形结构工具
- **StorageUtils**：本地存储工具
- **DeviceUtils**：设备检测工具
- **ObjectUtils**：对象操作工具
- **ValidateUtils**：验证工具
- **MessageUtils**：消息提示工具

### ProTable 组件

ProTable 是一个功能强大的表格组件，集成了常见的表格功能，如搜索、分页、多选等。

**主要参数：**

| 参数 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `actionRef` | `any` | - | 对外暴露的方法引用 |
| `request` | `Function` | - | 请求数据的函数 |
| `columns` | `Array` | - | 表格列定义 |
| `rowKey` | `string` | `id` | 指定行的唯一标识 |
| `defaultPageSize` | `number` | 10 | 默认每页显示数量 |
| `toolbarOptions` | `boolean` | true | 是否显示工具栏选项 |
| `bordered` | `boolean` | false | 是否显示边框 |
| `scrollY` | `number` | - | 表格垂直滚动高度 |

**示例代码：**
```jsx
<ProTable
  actionRef={this.tableRef}
  request={(params) => HttpUtils.get('admin/sysUser/page', params)}
  columns={this.columns}
  rowKey="id"
/>
```

### ProModal 组件

ProModal 是一个增强型模态框组件，用于展示表单或其他内容。

**主要参数：**

| 参数 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `title` | `string` | - | 模态框标题 |
| `actionRef` | `any` | - | 对外暴露的方法引用 |
| `onShow` | `Function` | - | 显示时的回调函数 |
| `width` | `number` | 800 | 模态框宽度 |
| `footer` | `any` | - | 模态框底部内容 |

**示例代码：**
```jsx
<ProModal title='操作手册' actionRef={this.modalRef}>
  <Form>...</Form>
</ProModal>
```

---

## 3 后端

### 配置

后端配置主要参考 **application-data-framework.yml** 文件，该文件定义了系统的核心配置项和菜单结构。

**配置结构：**

| 配置项 | 说明 |
|--------|------|
| `data.configs` | 数据库配置元数据，用于系统配置管理 |
| `data.menus` | 菜单配置，定义应用的菜单结构和权限 |

### 菜单列表

框架预定义了以下菜单结构：

| 菜单名 | 路径 | 权限标识 | 描述 |
|--------|------|----------|------|
| 我的任务 | `/flowable/task` | `myFlowableTask:manage` | 任务管理模块 |
| 机构管理 | `/system/org` | `sysOrg:save`, `sysOrg:view`, `sysOrg:delete` | 机构管理模块 |
| 用户管理 | `/system/user` | `sysUser:view`, `sysUser:save`, `sysUser:delete`, `sysUser:resetPwd`, `sysUser:grantPerm` | 用户管理模块 |
| 角色管理 | `/system/role` | `sysRole:save` | 角色管理模块 |
| 操作手册 | `/system/sysManual` | `sysManual:view`, `sysManual:delete`, `sysManual:save` | 操作手册管理模块 |
| 系统配置 | `/system/config` | `sysConfig:view`, `sysConfig:save` | 系统配置模块 |
| 数据字典 | `/system/dict` | `sysDict:view`, `sysDict:save`, `sysDict:delete` | 数据字典管理模块 |
| 存储文件 | `/system/file` | `sysFile:view`, `sysFile:delete` | 文件存储模块 |
| 作业调度 | `/system/job` | `job:view`, `job:save`, `job:triggerJob`, `job:jobLogClean` | 作业调度管理模块 |
| 操作日志 | `/system/log` | `sysLog:view` | 操作日志模块 |
| 流程引擎 | `/flowable` | `flowableModel:design`, `flowableModel:deploy`, `flowableTask:setAssignee`, `flowableInstance:close` | 工作流引擎模块 |
| 报表管理 | `/ureport` | `ureport:view`, `ureport:design` | 报表管理模块 |

### 业务项目配置

业务项目需要在 **application-data-biz.yml** 中进行配置，以扩展或覆盖框架的默认配置。

### 核心工具类

#### Spec 查询构建器

**`Spec.java`** 提供了一个简洁、动态、支持关联字段查询的 JPA Specification 构建器。

**主要方法：**

| 方法 | 参数 | 说明 |
|------|------|------|
| `eq(String field, Object value)` | 字段名、值 | 等于查询 |
| `like(String field, String value)` | 字段名、值 | 模糊查询 |
| `in(String field, Collection<?> values)` | 字段名、值集合 | IN 查询 |
| `between(String field, C value1, C value2)` | 字段名、开始值、结束值 | 范围查询 |
| `orLike(String value, String... fields)` | 值、字段数组 | 多字段 OR 模糊查询 |
| `select(String... fields)` | 字段数组 | 指定查询字段 |
| `groupBy(String... fields)` | 字段数组 | 分组查询 |
| `isMember(String field, Object element)` | 字段名、元素 | 集合成员查询 |

**示例代码：**
```java
Spec<User> spec = Spec.of()
    .eq("status", 1)
    .like("name", "张")
    .between("createTime", startDate, endDate);
List<User> users = userService.findAll(spec);
```

#### TreeUtils 树形工具

**`TreeUtils.java`** 提供了将列表结构转换为树形结构的功能。

**主要方法：**

| 方法 | 参数 | 说明 |
|------|------|------|
| `buildTree(List list, Function keyFn, Function pkeyFn, Function getChildren, BiConsumer setChildren)` | 列表、键函数、父键函数、获取子节点函数、设置子节点函数 | 构建树结构 |
| `treeToMap(List tree, Function keyFn, Function getChildren)` | 树、键函数、获取子节点函数 | 将树转换为Map |
| `walk(List list, Function getChildren, Consumer consumer)` | 树、获取子节点函数、处理函数 | 遍历树节点 |
| `getLeafs(List list, Function getChildren)` | 树、获取子节点函数 | 获取叶子节点 |
| `treeToList(List tree, Function getChildren)` | 树、获取子节点函数 | 树转列表 |

**示例代码：**
```java
List<TreeNode> tree = TreeUtils.buildTree(
    list,
    TreeNode::getId,
    TreeNode::getParentId,
    TreeNode::getChildren,
    TreeNode::setChildren
);
```

#### RemarkUtils 注解工具

**`RemarkUtils.java`** 提供了获取 **@Remark** 注解值的工具方法。

**主要方法：**

| 方法 | 参数 | 说明 |
|------|------|------|
| `getRemark(Field field)` | 字段 | 获取字段注解值 |
| `getRemark(Class<?> t)` | 类 | 获取类注解值 |
| `getRemark(Enum<?> t)` | 枚举 | 获取枚举注解值 |
| `getRemark(Method method)` | 方法 | 获取方法注解值 |

#### ExcelUtils Excel工具

**`ExcelUtils.java`** 提供了Excel文件的导入导出功能。

**主要方法：**

| 方法 | 参数 | 说明 |
|------|------|------|
| `importExcel(Class<T> cls, InputStream is)` | 类、输入流 | 导入Excel |
| `exportExcel(Class<T> cls, List<T> list, OutputStream os)` | 类、数据列表、输出流 | 导出Excel |

**示例代码：**
```java
// 导出Excel
List<User> users = userService.findAll();
ExcelUtils.exportExcel(User.class, users, response.getOutputStream());

// 导入Excel
List<User> users = ExcelUtils.importExcel(User.class, inputStream);
```

#### JdbcUtils 数据库工具

**`JdbcUtils.java`** 提供了原生SQL操作的工具方法。

**主要方法：**

| 方法 | 参数 | 说明 |
|------|------|------|
| `findOne(Class<T> cls, String sql, Object... params)` | 类、SQL语句、参数 | 查询单条记录 |
| `findAll(Class<T> cls, String sql, Object... params)` | 类、SQL语句、参数 | 查询多条记录 |
| `update(String sql, Object... params)` | SQL语句、参数 | 更新操作 |
| `insert(String tableName, Object bean)` | 表名、实体对象 | 插入操作 |
| `updateById(String table, Object bean)` | 表名、实体对象 | 根据ID更新 |
| `findLong(String sql, Object... params)` | SQL语句、参数 | 查询返回Long值 |
| `findInteger(String sql, Object... params)` | SQL语句、参数 | 查询返回Integer值 |
| `findColumnList(Class<T> elementType, String sql, Object... params)` | 元素类型、SQL语句、参数 | 查询返回列列表 |
| `findMapDict(String sql, Object... params)` | SQL语句、参数 | 查询返回字典Map |

**示例代码：**
```java
// 查询数据
User user = jdbcUtils.findOne(User.class, "SELECT * FROM user WHERE id = ?", id);

// 更新数据
int count = jdbcUtils.update("UPDATE user SET name = ? WHERE id = ?", name, id);

// 插入数据
int count = jdbcUtils.insert("user", user);
```

### ID生成器

框架提供了多种ID生成策略：

- **@GenerateUuidV7**：使用UUID V7生成器，生成按时间排序的UUID
- **@GeneratePrefixedSequence**：使用带前缀的序列生成器，生成带指定前缀的序列ID

**示例代码：**
```java
@Entity
public class User extends BaseEntity {
    @GenerateUuidV7
    private String id;

    @GeneratePrefixedSequence(prefix = "ORD")
    private String orderNo;
}
```

### 数据转换器

框架提供了一系列数据转换器，用于处理数据库和实体之间的数据转换：

- **BaseConverter**：基础转换器，使用JSON序列化
- **ToBigDecimalListConverter**：转换为BigDecimal列表
- **ToIntListConverter**：转换为Integer列表
- **ToLongListConverter**：转换为Long列表
- **ToMapConverter**：转换为Map
- **ToMapStringObjectConverter**：转换为Map<String, Object>
- **ToListConverter**：转换为List
- **ToListBracketConverter**：转换为用方括号表示的列表
- **ToSetConverter**：转换为Set
- **BaseCodeEnumConverter**：转换为枚举值
- 等等

### 验证器

框架提供了一系列自定义验证器注解：

- **@ValidateMobile**：验证手机号
- **@ValidateIdNum**：验证身份证号
- **@ValidateCarDrivingLicence**：验证驾驶证号
- **@ValidateCreditCode**：验证统一社会信用代码
- **@ValidatePassword**：验证密码强度
- **@ValidateIp**：验证IP地址
- **@ValidateIpv4**：验证IPv4地址
- **@ValidatePlateNumber**：验证车牌号
- **@ValidateZipCode**：验证邮编
- 等等

### 登录工具

**`LoginUtils.java`** 提供了获取当前登录用户信息的工具方法。

**主要方法：**

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `getUserId()` | String | 获取当前用户ID |
| `getUser()` | LoginUser | 获取当前用户对象 |
| `getPermissions()` | List<String> | 获取当前用户权限列表 |
| `getRoles()` | List<String> | 获取当前用户角色列表 |
| `isAdmin()` | boolean | 判断是否为管理员 |

### 作业调度

框架集成了Quartz作业调度系统，提供了作业管理功能。

#### BaseJob 抽象类

**`BaseJob.java`** 是所有作业任务的基类，提供了作业执行的通用处理逻辑，自动记录执行日志。

#### JobDescription 注解

**`JobDescription.java`** 用于描述作业任务的元信息，包括标签和参数定义。

#### 作业示例

**`HelloWorldJob.java`** 是一个作业任务的示例实现。

**示例代码：**
```java
@JobDescription(label = "你好世界",  params = {@FieldDescription(name = "name", label = "姓名")})
public class HelloWorldJob extends BaseJob {
    @Override
    public String execute(JobDataMap data, Logger logger) throws Exception {
        System.out.printf("你好世界, 欢迎您%s!%n", data.get("name"));
        logger.info("运行你好世界");
        return "OK";
    }
}
```

### 流程引擎

框架集成了Flowable工作流引擎，提供了流程管理功能。

#### 流程配置

流程配置定义在 **application-process.yml** 文件中：

**主要配置项：**

| 配置项 | 说明 |
|--------|------|
| `process.list` | 流程定义列表 |
| `process.list[].key` | 流程定义的唯一标识 |
| `process.list[].name` | 流程名称 |
| `process.list[].listener` | 流程事件监听器 |
| `process.list[].variables` | 流程变量定义 |
| `process.list[].forms` | 流程表单定义 |

**示例配置：**
```yml
process:
  list:
    - key: "leave_request"
      name: "请假流程"
      listener: io.admin.modules.flowable.example.LeaveProcessListener
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

#### 流程监听器

**`ProcessListener.java`** 是流程事件监听器的接口，**`LeaveProcessListener.java`** 是其实现示例，用于处理流程事件。

---

## 4 模板代码

以下是一个完整的CRUD功能模块的代码模板，以操作手册模块为例：

### 实体类 (SysManual.java)

```java
package io.admin.modules.system.entity;

import io.admin.common.utils.annotation.Remark;
import io.admin.framework.data.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

/**
 * 操作手册实体类
 * 注意：(name, version) 必须全局唯一
 */
@Remark("操作手册")
@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(name = "uk_sys_manual", columnNames = {"name", "version"})})
@FieldNameConstants
public class SysManual extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 名称，最大长度100字符，不能为空
     */
    @NotNull
    @Remark("名称")
    @Column(length = 100)
    @Size(max = 100, message = "名称长度不能超过100个字符")
    private String name;

    /**
     * 版本号，必须为正整数，不能为空
     */
    @NotNull
    @Remark("版本")
    @Positive(message = "版本号必须为正整数")
    private Integer version;

    /**
     * 文件ID，最大长度32字符
     */
    @Remark("文件")
    @Column(length = 32)
    @Size(max = 32, message = "文件ID长度不能超过32个字符")
    private String fileId;

}
```

### DAO层 (SysManualDao.java)

```java
package io.admin.modules.system.dao;

import io.admin.framework.data.specification.Spec;
import io.admin.modules.system.entity.SysManual;
import io.admin.framework.data.repository.BaseDao;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class SysManualDao extends BaseDao<SysManual> {


    public int findMaxVersion(String name){
        Spec<SysManual> q = Spec.<SysManual>of().eq(SysManual.Fields.name, name);

        SysManual e = this.findTop1(q, Sort.by(Sort.Direction.DESC, SysManual.Fields.version));

        return e == null ? 0 : e.getVersion();
    }

}
```

### Service层 (SysManualService.java)

```java
package io.admin.modules.system.service;

import io.admin.modules.system.dao.SysManualDao;
import io.admin.modules.system.entity.SysManual;
import io.admin.framework.data.service.BaseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysManualService extends BaseService<SysManual> {

    @Resource
    SysManualDao dao;

    @Override
    public SysManual saveOrUpdateByRequest(SysManual input, List<String> updateKeys) throws Exception {
        if(input.isNew()){
            int maxVersion = dao.findMaxVersion(input.getName());
            input.setVersion(maxVersion+1);
        }

        return super.saveOrUpdateByRequest(input, updateKeys);
    }
}
```

### Controller层 (SysManualController.java)

```java
package io.admin.modules.system.controller;

import io.admin.common.dto.AjaxResult;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.config.security.HasPermission;

import io.admin.framework.data.specification.Spec;
import io.admin.modules.system.entity.SysManual;
import io.admin.modules.system.service.SysManualService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("admin/sysManual")
public class SysManualController  {

    @Resource
    SysManualService service;

    @HasPermission("sysManual:view")
    @RequestMapping("page")
    public AjaxResult page(   String searchText,
                              @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<SysManual> q = Spec.of();
        q.orLike(searchText, SysManual.Fields.name);

        Page<SysManual> page = service.findPageByRequest(q, pageable);



        return AjaxResult.ok().data(page);
    }


    @HasPermission("sysManual:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody SysManual input, RequestBodyKeys updateFields) throws Exception {
        service.saveOrUpdateByRequest(input, updateFields);
        return AjaxResult.ok().msg("保存成功");
    }

    @HasPermission("sysManual:delete")
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteByRequest(id);
        return AjaxResult.ok().msg("删除成功");
    }



    /**
     * 用户点击帮助按钮
     *
     * @param searchText
     * @param pageable
     * @return
     */
    @RequestMapping("pageForUser")
    public AjaxResult pageForUser(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = {"name", "version"}) Pageable pageable) {
        Spec<SysManual> q = Spec.of();

        q.orLike(searchText, "name");



        List<SysManual> list = service.findPageByRequest(q, Pageable.unpaged(pageable.getSort())).getContent();
        // 数据量不大，直接内存过滤吧

        Map<String,SysManual> rs = new HashMap<>();
        for (SysManual e : list) {
            if(!rs.containsKey(e.getName())){
                rs.put(e.getName(),e);
            }
        }

        Collection<SysManual> values = rs.values();


        return AjaxResult.ok().data(new PageImpl<>(new ArrayList<>(values)));
    }

}
```

### 前端页面 (index.jsx)

```jsx
import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, Modal, Popconfirm} from 'antd'
import React from 'react'
import {ButtonList, FieldUploadFile, HttpUtils, Page, ProTable} from "../../../framework";


export default class extends React.Component {

    state = {
        formValues: {},
        formOpen: false
    }

    formRef = React.createRef()
    tableRef = React.createRef()

    columns = [

        {
            title: '名称',
            dataIndex: 'name',


        },

        {
            title: '版本',
            dataIndex: 'version',
            render(version) {
                return 'v' +version;
            }
        },

        {
            title: '文件',
            dataIndex: 'fileId',
            render(id){
               const url = 'admin/sysFile/preview/' + id;
                return <a href={url} target='_blank'>查看文件</a>
            }

        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='sysManual:save' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='sysManual:delete' title='是否确定删除操作手册'  onConfirm={() => this.handleDelete(record)}>
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
        HttpUtils.post('admin/sysManual/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }


    handleDelete = record => {
        HttpUtils.get('admin/sysManual/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    render() {
        return <Page>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={(params, {selectedRows, selectedRowKeys}) => {
                    return <ButtonList>
                        <Button perm='sysManual:save' type='primary' onClick={this.handleAdd}>
                            <PlusOutlined/> 新增
                        </Button>
                    </ButtonList>
                }}
                request={(params) => HttpUtils.get('admin/sysManual/page', params)}
                columns={this.columns}

            />

            <Modal title='操作手册'
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

                    <Form.Item label='名称' name='name' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>

                    <Form.Item label='文件' name='fileId' rules={[{required: true}]}>
                        <FieldUploadFile accept=".pdf" maxCount={1} />
                    </Form.Item>

                </Form>
            </Modal>
        </Page>


    }
}
```

### 配置文件 (application-data-framework.yml)

```yml
## 框架的核心配置项和菜单结构。 业务项目需在 application-data-biz.yml 中进行配置。
data:
  # 数据库配置元数据
  configs:
    - group-name: 邮件配置
      children:
        - code: email.from
          name: 邮件发送账号
        - code: email.pass
          name: 邮件发送密码
    - group-name: 网站配置
      children:
        - code: sys.copyright
          name: 版权信息
        - code: sys.loginBackground
          name: 登录背景图
        - code: sys.loginBoxBottomTip
          name: 登录框下面的提示
        - code: sys.title
          name: 站点标题
        - code: sys.waterMark
          name: 开启水印
          value-type: boolean
          description: 在所有页面增加水印
    - group-name: 系统配置
      children:
        - code: sys.baseUrl
          name: 请求基础地址
          description: 非必填，可用于拼接完整请求地址
        - code: sys.jwtSecret
          name: jwt密码
  # 菜单配置
  menus:
    - name: 我的任务
      id: myFlowableTask
      path: /flowable/task
      seq: -1
      refresh-on-tab-click: true
      message-count-url: /admin/flowable/my/todoCount
      perms:
        - perm: myFlowableTask:manage
          name: 任务管理
    - id: sys
      name: 系统管理
      seq: 10000 # 系统管理排在后面
      children:
        - id: sysOrg
          name: 机构管理
          path: /system/org
          icon: ApartmentOutlined
          perms:
            - name: 保存
              perm: sysOrg:save
            - name: 查看
              perm: sysOrg:view
            - name: 删除
              perm: sysOrg:delete

        - id: sysUser
          name: 用户管理
          path: /system/user
          icon: UserOutlined
          perms:
            - name: 列表
              perm: sysUser:view
            - name: 保存
              perm: sysUser:save
            - name: 删除
              perm: sysUser:delete
            - name: 重置密码
              perm: sysUser:resetPwd
            - name: 授权数据
              perm: sysUser:grantPerm

        - id: sysRole
          name: 角色管理
          path: /system/role
          icon: IdcardOutlined
          perms:
            - name: 保存
              perm: sysRole:save
        - id: sysManual
          name: 操作手册
          path: /system/sysManual
          icon: CopyOutlined
          perms:
            - perm: sysManual:view
              name: 查看
            - perm: sysManual:delete
              name: 删除
            - perm: sysManual:save
              name: 保存

        - id: sysConfig
          name: 系统配置
          path: /system/config
          icon: SettingOutlined
          perms:
            - perm: sysConfig:view
              name: 查看
            - perm: sysConfig:save
              name: 保存
        - id: sysDict
          name: 数据字典
          path: /system/dict
          icon: FileSearchOutlined
          perms:
            - name: 查看
              perm: sysDict:view
            - name: 保存
              perm: sysDict:save
            - name: 删除
              perm: sysDict:delete
        - id: sysFile
          name: 存储文件
          path: /system/file
          icon: FolderOpenOutlined
          perms:
            - perm: sysFile:view
              name: 查看
            - perm: sysFile:delete
              name: 删除

        - id: job
          name: 作业调度
          path: /system/job
          icon: OrderedListOutlined
          perms:
            - perm: job:view
              name: 查看
            - perm: job:save
              name: 保存
            - name: 执行一次
              perm: job:triggerJob
            - name: 清理日志
              perm: job:jobLogClean
        - id: sysLog
          name: 操作日志
          path: /system/log
          icon: FileSearchOutlined
          perms:
            - name: 查看
              perm: sysLog:view
        - id: api
          name: 接口管理
          icon: ApiOutlined
          path: /system/api
          perms:
            - name: 管理
              perm: api
        - id: flowableModel
          name: 流程引擎
          path: /flowable
          perms:
            - perm: flowableModel:design
              name: 设计
            - perm: flowableModel:deploy
              name: 部署
            - perm: flowableTask:setAssignee
              name: 动态修改受理人
            - perm: flowableInstance:close
              name: 终止流程
        - id: ureport
          name: 报表管理
          icon: TableOutlined
          seq: 101
          path: /ureport
          perms:
            - perm: ureport:view
              name: 查看
            - perm: ureport:design
              name: 设计
```