# 项目文档

### 介绍

这是一个小型的管理系统框架，旨在提供快速开发管理系统的基础能力。

**开发环境**:

*   Java: 17
*   Maven: 3.x
*   Node.js: 18.x
*   NPM: 8.x

**Maven 引用**:

```xml
<dependency>
    <groupId>io.github.jiangood</groupId>
    <artifactId>springboot-admin-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```
最新版本为： ![Maven Version](https://img.shields.io/maven-central/v/io.github.jiangood/springboot-admin-starter)

**NPM 引用**:

```bash
npm install @jiangood/springboot-admin-starter
# 或者
yarn add @jiangood/springboot-admin-starter
```
最新版本为： ![NPM Version](https://img.shields.io/npm/v/@jiangood/springboot-admin-starter)

**后端依赖**:
该项目使用了 `Spring Boot 3.x`，主要依赖如下：
*   `spring-boot-starter-web`: Web 应用开发
*   `spring-boot-starter-quartz`: 作业调度
*   `spring-boot-starter-validation`: 数据校验
*   `spring-boot-starter-aop`: 面向切面编程
*   `spring-boot-starter-data-jpa`: JPA 持久化
*   `spring-boot-starter-cache`: 缓存
*   `spring-boot-starter-security`: 安全框架
*   `org.mapstruct`: Bean 映射
*   `com.jhlabs.filters`: 图像处理
*   `io.minio`: Minio 对象存储
*   `org.apache.poi`: Excel 处理
*   `com.itextpdf`: PDF 处理
*   `com.github.f4b6a3.uuid-creator`: UUID 生成
*   `cn.hutool`: 工具库
*   `com.google.guava`: Google 工具库
*   `commons-io`: IO 工具
*   `commons-beanutils`: Bean 工具
*   `com.belerweb.pinyin4j`: 汉字转拼音
*   `org.jsoup`: HTML 解析
*   `lombok`: 简化 Java 代码
*   `mysql-connector-j`: MySQL 驱动
*   `ureport-console`: 报表设计器
*   `flowable-spring-boot-starter-process`: 流程引擎

**前端依赖**:
该项目使用了 `UmiJS 4.x` 和 `Ant Design`，主要 `peerDependencies` 如下：
*   `@ant-design/icons`
*   `@bpmn-io/properties-panel`
*   `@tinymce/tinymce-react`
*   `@umijs/types`
*   `antd`
*   `antd-img-crop`
*   `axios`
*   `bpmn-js`
*   `bpmn-js-properties-panel`
*   `preact`
*   `dayjs`
*   `jsencrypt`
*   `lodash`
*   `qs`
*   `react`
*   `react-dom`
*   `umi`

**配置说明**:

框架提供了一套配置管理和菜单管理功能，通过 `application-data-framework.yml` 进行配置。

*   **菜单列表**:
    通过 `data.menus` 配置应用菜单，支持多级菜单、图标、权限控制等。
    ```yaml
    data:
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
            # ... 其他菜单
    ```

*   **业务项目如何配置**:
    业务项目可以在 `application-data-biz.yml` 中配置自己的菜单和数据。
    菜单配置结构与 `application-data-framework.yml` 相同，系统会合并两者的配置。
    例如，业务项目可以添加自己的模块菜单。

    **数据配置元数据**:
    通过 `data.configs` 配置系统参数，用于在前端生成配置表单，支持多种数据类型。
    ```yaml
    data:
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
        # ... 其他配置
    ```


### 前端

框架提供了丰富的组件和工具类，方便前端开发。

**组件 (web/src/framework/components)**

| 名称               | 参数（属性）                                          | 说明                 |
| :----------------- | :---------------------------------------------------- | :------------------- |
| `DownloadFileButton` | `url`: String, `params`: any                          | 文件下载按钮         |
| `Ellipsis`         | `children`: ReactNode, `lines`: Number, `length`: Number | 文本省略组件         |
| `LinkButton`       | `href`: String, `target`: String                      | 链接按钮             |
| `NamedIcon`        | `type`: String                                        | 根据名称显示图标     |
| `PageLoading`      | 无                                                    | 全局页面加载中提示   |
| `Gap`              | 无 (或 Ant Design Space Props)                        | 间距组件             |
| `Page`             | 无                                                    | 页面布局组件         |
| `ProModal`         | (Ant Design Modal Props)                              | 增强型弹窗组件       |
| `ProTable`         | (Ant Design Table Props, ProTable Props)              | 增强型表格组件       |
| `ValueType`        | `value`: any, `valueType`: String                     | 值类型渲染组件       |
| `view`             | 无                                                    | 视图组件（通用容器） |

**表单字段组件 (web/src/framework/field-components)**

| 名称                        | 参数（属性）                                           | 说明               |
| :-------------------------- | :----------------------------------------------------- | :----------------- |
| `FieldBoolean`              | `value`: Boolean                                       | 布尔值显示         |
| `FieldDate`                 | `value`: Date, `format`: String                        | 日期选择器         |
| `FieldDateRange`            | `value`: Array, `format`: String                       | 日期范围选择器     |
| `FieldDictSelect`           | `value`: String/Number, `dictName`: String             | 字典选择器         |
| `FieldEditor`               | `value`: String                                        | 富文本编辑器       |
| `FieldPercent`              | `value`: Number                                        | 百分比输入框       |
| `FieldRemoteSelect`         | `value`: String/Number, `url`: String, `labelKey`: String, `valueKey`: String | 远程单选选择器     |
| `FieldRemoteSelectMultiple` | `value`: Array, `url`: String, `labelKey`: String, `valueKey`: String | 远程多选选择器     |
| `FieldRemoteTree`           | `value`: String/Number, `url`: String                  | 远程树形选择器     |
| `FieldRemoteTreeCascader`   | `value`: Array, `url`: String                          | 远程树形联级选择器 |
| `FieldRemoteTreeSelect`     | `value`: String/Number, `url`: String                  | 远程树形选择       |
| `FieldRemoteTreeSelectMultiple` | `value`: Array, `url`: String                  | 远程多选树形选择   |
| `FieldSysOrgTree`           | `value`: String, `orgId`: String                       | 系统组织机构树     |
| `FieldSysOrgTreeSelect`     | `value`: String, `orgId`: String                       | 系统组织机构选择   |
| `FieldTable`                | `value`: Array, `columns`: Array                       | 表格字段           |
| `FieldTableSelect`          | `value`: String/Number, `columns`: Array, `request`: Function | 表格选择器         |
| `FieldUploadFile`           | `value`: String/Array, `action`: String                | 文件上传组件       |

**工具类 (web/src/framework/utils)**

| 名称           | 说明             |
| :------------- | :--------------- |
| `ArrUtils`     | 数组操作工具类   |
| `ColorsUtils`  | 颜色处理工具类   |
| `DateUtils`    | 日期处理工具类   |
| `DeviceUtils`  | 设备信息工具类   |
| `DomUtils`     | DOM 操作工具类   |
| `EventBusUtils` | 事件总线工具类   |
| `HttpUtils`    | HTTP 请求工具类  |
| `MessageUtils` | 消息提示工具类   |
| `ObjectUtils`  | 对象操作工具类   |
| `StorageUtils` | 浏览器存储工具类 |
| `StringUtils`  | 字符串处理工具类 |
| `TreeUtils`    | 树形结构工具类   |
| `UrlUtils`     | URL 处理工具类   |
| `UuidUtils`    | UUID 生成工具类  |
| `ValidateUtils` | 验证工具类       |

### 后端

**Tree 工具类 (src/main/java/io/admin/common/utils/tree)**

`io.admin.common.utils.tree` 目录下提供了一套完整的树形结构处理工具，包括：

*   `TreeNode.java`: 定义了树节点的通用接口或抽象类。
*   `TreeManager.java`: 树形结构的管理器，可能包含构建、查找、遍历等操作。
*   `TreeUtils.java`: 树形结构相关的实用方法，如列表转树、树转列表、查找子节点等。
主要用于将扁平化的列表数据转换为具有层级关系的树形结构，方便前端展示和后端处理。

**JPA 动态查询工具 (src/main/java/io/admin/framework/data/specification/Spec.java)**

`Spec.java` 是一个简洁、动态、支持关联字段查询的 JPA `Specification` 构建器。它通过链式调用收集查询条件，最终使用 `AND` 逻辑连接所有条件。

**核心特性**:

*   **链式 API**: 提供 `eq`, `ne`, `gt`, `lt`, `ge`, `le`, `like`, `in`, `between`, `isNull`, `isNotNull` 等丰富的查询方法。
*   **动态条件**: 条件值为空时，自动忽略该条件。
*   **关联字段查询**: 支持通过点操作符 (`dept.name`) 查询关联对象的属性。
*   **多条件 OR 连接**: 提供 `or` 方法，可以将多个 `Specification` 通过 `OR` 连接。
*   **模糊查询**: `like` 方法支持左右模糊匹配，且默认不区分大小写。
*   **Example 查询**: 支持基于 `Example` 的查询，方便快速构建简单查询。
*   **聚合函数**: 支持 `selectFnc` 方法用于聚合查询（AVG, SUM, MIN, MAX, COUNT）。
*   **集合成员查询**: `isMember`, `isNotMember` 用于 JPA `IS MEMBER OF` 语义，检查元素是否属于集合字段。
*   **分组和Having**: 提供 `groupBy` 和 `having` 方法支持 SQL 的分组和分组后过滤。

**示例**:

```java
// 构建一个 Spec 对象
Spec<User> spec = Spec.of();

// 添加查询条件
spec.eq("username", "testUser")
    .like("email", "example.com")
    .ge("age", 18)
    .in("status", Arrays.asList(1, 2))
    .or(Spec.of().eq("gender", "M"), Spec.of().eq("gender", "F")) // OR 条件
    .isMember("roles", adminRole); // 查询拥有 adminRole 的用户

// 应用到 JpaRepository
userRepository.findAll(spec);

// 示例：查询部门名称包含 "研发" 且用户年龄大于 20 的用户
Spec<User> userSpec = Spec.of()
    .like("department.name", "研发") // 关联查询
    .gt("age", 20);
userRepository.findAll(userSpec);
```

### 模板代码

以下是基于 **学生 (Student)** 业务的模板代码，您可以参考这些模板快速生成自己的业务代码。

#### Entity 模板

参考 `src/main/java/io/admin/modules/system/entity/SysManual.java`

```java
package io.admin.modules.business.entity;

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

/**
 * 学生实体类
 */
@Remark("学生信息")
@Entity
@Getter
@Setter
@Table(name = "biz_student") // 业务表命名建议以 biz_ 开头
@FieldNameConstants
public class Student extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 学生姓名
     */
    @NotNull(message = "学生姓名不能为空")
    @Remark("姓名")
    @Column(length = 50)
    @Size(max = 50, message = "学生姓名长度不能超过50个字符")
    private String name;

    /**
     * 学生年龄
     */
    @Remark("年龄")
    private Integer age;

    /**
     * 性别 (0: 女, 1: 男)
     */
    @Remark("性别")
    private Integer gender; // 可以定义为枚举类型
}
```

#### DAO 模板

参考 `src/main/java/io/admin/modules/system/dao/SysManualDao.java`

```java
package io.admin.modules.business.dao;

import io.admin.framework.data.repository.BaseDao;
import io.admin.modules.business.entity.Student;
import org.springframework.stereotype.Repository;

@Repository
public class StudentDao extends BaseDao<Student> {
    // 可以在这里添加自定义的查询方法
}
```

#### Service 模板

参考 `src/main/java/io/admin/modules/system/service/SysManualService.java`

```java
package io.admin.modules.business.service;

import io.admin.framework.data.service.BaseService;
import io.admin.modules.business.entity.Student;
import io.admin.modules.business.dao.StudentDao;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class StudentService extends BaseService<Student> {

    @Resource
    StudentDao dao;

    // 可以在这里添加业务逻辑
}
```

#### Controller 模板

参考 `src/main/java/io/admin/modules/system/controller/SysManualController.java`

```java
package io.admin.modules.business.controller;

import io.admin.common.dto.AjaxResult;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.config.security.HasPermission;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.business.entity.Student;
import io.admin.modules.business.service.StudentService;
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
@RequestMapping("admin/student") // 业务模块统一前缀 admin/
public class StudentController {

    @Resource
    StudentService service;

    /**
     * 分页查询
     * @param searchText 搜索文本
     * @param pageable 分页参数
     * @return
     * @throws Exception
     */
    @HasPermission("student:view") // 权限控制，例如 student:view
    @RequestMapping("page")
    public AjaxResult page(String searchText,
                           @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<Student> q = Spec.of();
        q.orLike(searchText, Student.Fields.name); // 根据姓名模糊搜索

        Page<Student> page = service.findPageByRequest(q, pageable);
        return AjaxResult.ok().data(page);
    }

    /**
     * 保存或更新
     * @param input 学生信息
     * @param updateFields 更新字段
     * @return
     * @throws Exception
     */
    @HasPermission("student:save") // 权限控制，例如 student:save
    @PostMapping("save")
    public AjaxResult save(@RequestBody Student input, RequestBodyKeys updateFields) throws Exception {
        service.saveOrUpdateByRequest(input, updateFields);
        return AjaxResult.ok().msg("保存成功");
    }

    /**
     * 删除
     * @param id 学生ID
     * @return
     */
    @HasPermission("student:delete") // 权限控制，例如 student:delete
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteByRequest(id);
        return AjaxResult.ok().msg("删除成功");
    }
}
```

#### 菜单配置模板

参考 `src/main/resources/application-data-framework.yml`

在业务项目的 `application-data-biz.yml` 中添加如下配置：

```yaml
data:
  menus:
    - id: biz
      name: 业务管理
      seq: 100 # 调整排序
      children:
        - id: student
          name: 学生管理
          path: /business/student # 前端路由路径
          icon: TeamOutlined # Ant Design Icon 图标名称
          perms:
            - perm: student:view
              name: 查看
            - perm: student:save
              name: 保存
            - perm: student:delete
              name: 删除
```

#### 前端页面模板

参考 `web/src/pages/system/sysManual/index.jsx`，业务项目前端引用时，使用 `@jiangood/springboot-admin-starter` 模块名。

```jsx
import { PlusOutlined } from '@ant-design/icons';
import { Button, Form, Input, Modal, Popconfirm, Radio } from 'antd';
import React from 'react';
import { ButtonList, HttpUtils, Page, ProTable } from '@jiangood/springboot-admin-starter'; // 注意这里引用的是模块名

export default class StudentManage extends React.Component {

    state = {
        formValues: {},
        formOpen: false,
    };

    formRef = React.createRef();
    tableRef = React.createRef();

    columns = [
        {
            title: '姓名',
            dataIndex: 'name',
        },
        {
            title: '年龄',
            dataIndex: 'age',
        },
        {
            title: '性别',
            dataIndex: 'gender',
            render: (gender) => (gender === 1 ? '男' : '女'),
        },
        {
            title: '操作',
            dataIndex: 'option',
            valueType: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='student:save' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='student:delete' title='是否确定删除该学生信息？' onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                </ButtonList>
            ),
        },
    ];

    handleAdd = () => {
        this.setState({ formOpen: true, formValues: {} });
    };

    handleEdit = (record) => {
        this.setState({ formOpen: true, formValues: record });
    };

    onFinish = (values) => {
        HttpUtils.post('admin/student/save', values).then((rs) => {
            this.setState({ formOpen: false });
            this.tableRef.current.reload();
        });
    };

    handleDelete = (record) => {
        HttpUtils.get('admin/student/delete', { id: record.id }).then((rs) => {
            this.tableRef.current.reload();
        });
    };

    render() {
        return (
            <Page>
                <ProTable
                    actionRef={this.tableRef}
                    toolBarRender={() => [
                        <Button perm='student:save' type='primary' onClick={this.handleAdd}>
                            <PlusOutlined /> 新增
                        </Button>,
                    ]}
                    request={(params) => HttpUtils.get('admin/student/page', params)}
                    columns={this.columns}
                />

                <Modal
                    title='学生信息'
                    open={this.state.formOpen}
                    onOk={() => this.formRef.current.submit()}
                    onCancel={() => this.setState({ formOpen: false })}
                    destroyOnClose // 切换为 destroyOnClose 以确保表单重置
                    maskClosable={false}
                >
                    <Form
                        ref={this.formRef}
                        labelCol={{ flex: '100px' }}
                        initialValues={this.state.formValues}
                        onFinish={this.onFinish}
                    >
                        <Form.Item name='id' hidden></Form.Item> {/* 隐藏ID字段 */}

                        <Form.Item label='姓名' name='name' rules={[{ required: true, message: '请输入学生姓名' }]}>
                            <Input />
                        </Form.Item>

                        <Form.Item label='年龄' name='age'>
                            <Input type='number' />
                        </Form.Item>

                        <Form.Item label='性别' name='gender'>
                            <Radio.Group>
                                <Radio value={1}>男</Radio>
                                <Radio value={0}>女</Radio>
                            </Radio.Group>
                        </Form.Item>
                    </Form>
                </Modal>
            </Page>
        );
    }
}
```