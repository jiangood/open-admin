---
name: open-admin
description: 在业务项目中创建 CRUD 业务模块，遵循 open-admin 框架的 Entity→Repository→Service→Controller→前端页面→菜单配置的完整模式。适用于以 Maven JAR + npm 包方式引入 open-admin 的业务项目。
---

# open-admin — 业务模块创建指南

## 适用范围

当业务项目已通过 Maven JAR（`io.github.jiangood:open-admin`）和 npm 包（`@jiangood/open-admin`）方式集成了 open-admin 框架，需要创建新的业务 CRUD 模块时使用。

## 前提条件检查

开始之前，Claude 必须读取业务项目的以下文件确认集成状态：

### Maven

`pom.xml` 中必须有：

```xml
<dependency>
    <groupId>io.github.jiangood</groupId>
    <artifactId>open-admin</artifactId>
    <version>${open-admin.version}</version>
</dependency>
```

### Spring Boot 配置

`application.yml` 已导入框架默认配置（必选）：

```yaml
spring:
  config:
    import: classpath:application-lib.yml
```

同时检查 `@SpringBootApplication` 或 `@ComponentScan` 是否覆盖了业务项目自身的包扫描范围。业务项目需要确保自己的包（如 `com.mycompany.myproject`）被扫描到，框架的 `OpenAdminConfiguration` 只扫描 `io.github.jiangood.openadmin` 包。

### npm

`package.json` 中包含：

```json
"dependencies": {
    "@jiangood/open-admin": "^2.0.0",
    "umi": "^4.0.0",
    "antd": "^6.0.0",
    "react": "^19.0.0",
    ...
}
```

并确认 `config/config.js` 通过 `getPluginDir()` 机制使用了框架插件（即引用了 `@jiangood/open-admin/config`）。

### 目录结构检查

```
业务项目 src/ 下应包含：
  main/java/com/xxx/          # Java 源码
  main/resources/             # 配置资源
    config/                    # 菜单/字典 YAML（可选，无则使用框架默认）
  main/resources/application.yml
```

## 第一步：需求确认

向开发者确认以下信息：

1. **业务实体名称**：中英文名（如"客户 / Customer"）
2. **字段列表**：每个字段的名称、类型（String / Integer / BigDecimal / Boolean / LocalDateTime / 枚举）、是否必填、是否作为查询条件、是否为字典项
3. **权限规划**：增删改查分别用什么权限 code（如 `bizCustomer:query`、`bizCustomer:save` 等）
4. **菜单位置**：顶级菜单还是挂在现有菜单下

## 第二步：后端模块创建

### 命名约定

| 元素 | 命名规则 | 示例 |
|------|---------|------|
| 基础包 | `{groupId}.{project}` | `com.mycompany.myproject` |
| 模块包 | `{base}.modules.{module}` | `com.mycompany.myproject.modules.customer` |
| 实体类 | `{Entity}` | `Customer` |
| 数据库表 | 小写、下划线分隔、`biz_` 前缀 | `biz_customer` |
| 请求路径 | `admin/{kebab-module}` | `admin/customer` |
| 权限前缀 | `{camel-module}:{action}` | `customer:save` |

### Entity

- 包：`{base}.modules.{module}.entity.{Entity}`
- 继承 `io.github.jiangood.openadmin.framework.data.BaseEntity`（自带 id/UUIDv7、createTime、updateTime、createBy、updateBy、delFlag、remark）
- `@Table(name = "biz_xxx")` 指定物理表名
- `@Getter` `@Setter` `@FieldNameConstants`（Lombok）
- Java 21 的 `String` 类型字段不需要 `@Column`（Hibernate 自动驼峰转下划线），除非需要指定 `length` 或 `nullable`
- 校验用 `jakarta.validation` 注解（`@NotBlank`、`@NotNull`、`@Size`）
- 枚举字段使用框架 `BaseEnum` 接口 + `EnumConverter`（参考 `io.github.jiangood.openadmin.framework.enums`）
- 可选：`@Remark("字段说明")` 来自 `io.github.jiangood.openadmin.util.annotation.Remark`

```java
package com.mycompany.myproject.modules.customer.entity;

import io.github.jiangood.openadmin.framework.data.BaseEntity;
import io.github.jiangood.openadmin.util.annotation.Remark;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Remark("客户")
@Entity
@Getter
@Setter
@FieldNameConstants
@Table(name = "biz_customer")
public class Customer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @NotBlank
    @Remark("客户名称")
    @Size(max = 100)
    private String name;

    @Remark("联系人")
    @Size(max = 50)
    private String contact;

    @Remark("联系电话")
    @Size(max = 20)
    private String phone;

    @NotNull
    @Remark("状态")
    private Boolean enabled;
}
```

### Repository

- 包：`{base}.modules.{module}.repository.{Entity}Repository`
- 继承 `io.github.jiangood.openadmin.framework.data.BaseRepository<Entity, String>`
- 无需额外方法，通用 CRUD + Spec 动态查询 + 分页由 BaseRepository 提供
- 复杂查询用 `Spec` 构建（`io.github.jiangood.openadmin.framework.data.specification.Spec`）

```java
package com.mycompany.myproject.modules.customer.repository;

import com.mycompany.myproject.modules.customer.entity.Customer;
import io.github.jiangood.openadmin.framework.data.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends BaseRepository<Customer, String> {
}
```

### Service

- 包：`{base}.modules.{module}.service.{Entity}Service`
- 继承 `io.github.jiangood.openadmin.framework.data.BaseService<Entity>`
- 构造器注入 Repository（`super(repository)`）
- 通用方法由 BaseService 提供：`page()`、`list()`、`get()`、`save()`、`update()`、`deleteById()` 等
- 自定义业务逻辑在此层添加

```java
package com.mycompany.myproject.modules.customer.service;

import com.mycompany.myproject.modules.customer.entity.Customer;
import com.mycompany.myproject.modules.customer.repository.CustomerRepository;
import io.github.jiangood.openadmin.framework.data.BaseService;
import org.springframework.stereotype.Service;

@Service
public class CustomerService extends BaseService<Customer> {

    public CustomerService(CustomerRepository customerRepository) {
        super(customerRepository);
    }
}
```

### Controller

- 包：`{base}.modules.{module}.controller.{Entity}Controller`
- `@RestController` + `@RequestMapping("admin/{kebab-module}")`
- `@RequiredArgsConstructor` 构造器注入 Service
- `@HasPermission("{module}:{action}")` 权限控制
- 统一返回 `io.github.jiangood.openadmin.util.dto.AjaxResult`

标准 5 个端点：

| 端点 | 方法 | URL | 权限 | 说明 |
|------|------|-----|------|------|
| 分页查询 | `@RequestMapping("page")` | `admin/{module}/page` | `{module}:query` | 支持 searchText 模糊搜索 + Pageable |
| 详情 | `@GetMapping("info/{id}")` | `admin/{module}/info/{id}` | `{module}:query` | 返回单条记录 |
| 新增 | `@PostMapping("save")` | `admin/{module}/save` | `{module}:save` | @RequestBody @Valid |
| 修改 | `@PutMapping("update")` | `admin/{module}/update` | `{module}:update` | @RequestBody @Valid |
| 删除 | `@PostMapping("delete")` | `admin/{module}/delete` | `{module}:delete` | @RequestBody IdReq |
| 选项列表 | `@GetMapping("options")` | `admin/{module}/options` | `{module}:query` | 下拉框数据源（非必选） |

```java
package com.mycompany.myproject.modules.customer.controller;

import com.mycompany.myproject.modules.customer.entity.Customer;
import com.mycompany.myproject.modules.customer.service.CustomerService;
import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.framework.perm.HasPermission;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.github.jiangood.openadmin.util.dto.antd.Option;

@RestController
@RequestMapping("admin/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @HasPermission("customer:query")
    @RequestMapping("page")
    public AjaxResult page(String searchText,
            @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) {
        Spec<Customer> q = Spec.of().orLike(searchText, "name", "contact");
        Page<Customer> page = service.findAll(q, pageable);
        return AjaxResult.ok().data(page);
    }

    @HasPermission("customer:query")
    @GetMapping("info/{id}")
    public AjaxResult info(@PathVariable String id) {
        return service.findById(id)
                .map(c -> AjaxResult.ok().data(c))
                .orElse(AjaxResult.fail().msg("记录不存在"));
    }

    @HasPermission("customer:save")
    @PostMapping("save")
    public AjaxResult save(@Valid @RequestBody Customer input) {
        service.save(input);
        return AjaxResult.ok().msg("保存成功");
    }

    @HasPermission("customer:update")
    @PutMapping("update")
    public AjaxResult update(@Valid @RequestBody Customer input) {
        service.update(input);
        return AjaxResult.ok().msg("修改成功");
    }

    @HasPermission("customer:delete")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq req) {
        service.deleteById(req.getId());
        return AjaxResult.ok().msg("删除成功");
    }

    @HasPermission("customer:query")
    @GetMapping("options")
    public AjaxResult options(String searchText) {
        Spec<Customer> q = Spec.of().orLike(searchText, "name");
        List<Customer> list = service.findAll(q, Sort.by("name"));
        List<Option> options = list.stream().map(a -> Option.of(a.getId(), a.getName())).toList();
        return AjaxResult.ok().data(options);
    }
}
```

## 第三步：前端页面创建

### 路由机制说明

框架的 `common-plugin.js`（UmiJS 插件）在构建时自动扫描 `src/pages/` 目录和 `node_modules/@jiangood/open-admin/src/pages/` 目录，根据文件名和目录结构自动注册路由。**只需在 `src/pages/{模块}/index.jsx` 创建页面文件，无需手动配置路由。**

业务项目的 `config/config.js` 应使用 `@jiangood/open-admin/config` 作为配置源（通过 `getPluginDir()` 自动检测）。

### 页面模板

使用 class 组件，遵循框架现有页面风格：

```jsx
import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, Modal, Popconfirm} from 'antd'
import React from 'react'
import {ButtonList, HttpUtils, Page, ProTable} from "@jiangood/open-admin";

export default class extends React.Component {

    state = { formValues: {}, formOpen: false }
    formRef = React.createRef()
    tableRef = React.createRef()

    columns = [
        { title: '名称', dataIndex: 'name' },
        { title: '联系人', dataIndex: 'contact' },
        { title: '联系电话', dataIndex: 'phone' },
        { title: '状态', dataIndex: 'enabled', render: (v) => v ? '启用' : '停用' },
        { title: '创建时间', dataIndex: 'createTime', valueType: 'date' },
        { title: '操作', dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='customer:update' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='customer:delete' title='确定删除？' onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                </ButtonList>
            ),
        },
    ]

    handleAdd = () => this.setState({formOpen: true, formValues: {}})
    handleEdit = record => this.setState({formOpen: true, formValues: record})
    handleSubmit = values => {
        HttpUtils.post('admin/customer/save', values).then(() => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }
    handleDelete = record => {
        HttpUtils.post('admin/customer/delete', {id: record.id}).then(() => {
            this.tableRef.current.reload()
        })
    }

    render() {
        return <Page padding={true}>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={() => (
                    <ButtonList>
                        <Button perm='customer:save' type='primary' onClick={this.handleAdd}>
                            <PlusOutlined /> 新增
                        </Button>
                    </ButtonList>
                )}
                request={(params) => HttpUtils.get('admin/customer/page', params)}
                columns={this.columns}
                search={{
                    name: { label: '名称', type: 'text' },
                }}
            />
            <Modal title='信息'
                   open={this.state.formOpen}
                   onOk={() => this.formRef.current.submit()}
                   onCancel={() => this.setState({formOpen: false})}
                   destroyOnClose maskClosable={false}>
                <Form ref={this.formRef}
                      initialValues={this.state.formValues}
                      onFinish={this.handleSubmit}>
                    <Form.Item name='id' noStyle />
                    <Form.Item label='名称' name='name' rules={[{required: true}]}>
                        <Input />
                    </Form.Item>
                    <Form.Item label='联系人' name='contact'>
                        <Input />
                    </Form.Item>
                    <Form.Item label='联系电话' name='phone'>
                        <Input />
                    </Form.Item>
                    <Form.Item label='状态' name='enabled' valuePropName='checked'>
                        <Switch />
                    </Form.Item>
                </Form>
            </Modal>
        </Page>
    }
}
```

### 字段组件选用指南

当字段需要特殊业务组件时，从 `@jiangood/open-admin` 引入并替换模板中的 `Input`：

| 业务需求 | 组件 | import |
|---------|------|--------|
| 字典下拉 | `FieldDictSelect dict="dict_type"` | `@jiangood/open-admin` |
| 远程搜索下拉 | `FieldRemoteSelect url="admin/xxx/options"` | `@jiangood/open-admin` |
| 远程树选择 | `FieldRemoteTreeSelect url="..."` | `@jiangood/open-admin` |
| 组织树选择 | `FieldSysOrgTreeSelect` | `@jiangood/open-admin` |
| 部门/单位树 | `FieldDeptTreeSelect` / `FieldUnitTreeSelect` | `@jiangood/open-admin` |
| 布尔开关 | `FieldBoolean` | `@jiangood/open-admin` |
| 日期选择 | `FieldDate` / `FieldDateRange` | `@jiangood/open-admin` |
| 数字范围 | `FieldNumberRange` | `@jiangood/open-admin` |
| 富文本 | `FieldEditor` | `@jiangood/open-admin` |
| 文件上传 | `FieldUploadFile` | `@jiangood/open-admin` |
| 表格选择 | `FieldTableSelect` | `@jiangood/open-admin` |
| 百分比 | `FieldPercent` | `@jiangood/open-admin` |
| 多选 | `FieldRemoteSelectMultiple` | `@jiangood/open-admin` |

详情参考 `docs/api/frontend/field-components.md`。

### 展示视图组件选用指南

在表格列中渲染字段值时使用：

| 场景 | 组件 | import |
|------|------|--------|
| 布尔值（是/否） | `ViewBoolean` | `@jiangood/open-admin` |
| 布尔值（启用/停用） | `ViewBooleanEnableDisable` | `@jiangood/open-admin` |
| 审批状态 | `ViewApproveStatus` | `@jiangood/open-admin` |
| 图片预览 | `ViewImage` | `@jiangood/open-admin` |
| 文件下载 | `ViewFile` / `ViewFileButton` | `@jiangood/open-admin` |
| 密码脱敏 | `ViewPassword` | `@jiangood/open-admin` |

详情参考 `docs/api/frontend/system-components.md`。

## 第四步：菜单与权限配置

### YAML 菜单定义

业务项目在 `src/main/resources/config/application-data-{profile}.yml` 中定义自己的菜单。框架的 `SysMenuRepositoryYamlImpl` 支持多文件合并，框架默认菜单与业务菜单会自动合并。

```yaml
# src/main/resources/config/application-data-local.yml
data:
  menus:
    - id: customer
      name: 客户管理
      path: /customer
      icon: TeamOutlined
      perms:
        - {name: 查询, code: query}
        - {name: 新增, code: save}
        - {name: 修改, code: update}
        - {name: 删除, code: delete}
```

如需要将菜单挂在框架已有菜单下（如挂在"系统管理"下），在 YAML 中指定 `parentId`：

```yaml
    - id: customer
      parentId: system   # 挂在系统管理菜单下
      name: 客户管理
      path: /system/customer
      icon: TeamOutlined
```

### 权限对应关系

三层权限对应关系：

| 层级 | 配置位置 | 写法 |
|------|---------|------|
| 后端 | Controller `@HasPermission` | `@HasPermission("customer:save")` |
| 前端 | Button `perm` prop | `<Button perm="customer:save">新增</Button>` |
| 菜单 | YAML `perms` | `- {name: 新增, code: save}` |

框架通过 `@HasPermission` 注解 + AOP 切面拦截未授权请求。前端 `ButtonList` 和 `HasPerm` 组件根据当前用户的权限动态显示/隐藏按钮。

## 第五步：验证清单

完成后逐项确认：

### 编译验证
- [ ] `mvn compile` 编译通过
- [ ] 前端 `pnpm build` 或 `pnpm dev` 正常

### 后端验证
- [ ] `GET admin/{module}/page` 返回正确分页数据
- [ ] `GET admin/{module}/info/{id}` 返回单条数据
- [ ] `POST admin/{module}/save` 新增成功
- [ ] `PUT admin/{module}/update` 修改成功
- [ ] `POST admin/{module}/delete` 删除成功

### 前端验证
- [ ] 页面通过菜单访问正常显示
- [ ] 列表数据正常展示
- [ ] 搜索/分页功能正常
- [ ] 新增/编辑弹窗正常
- [ ] 删除操作正常

### 权限验证
- [ ] 无权限用户看不到操作按钮
- [ ] 未授权 API 返回 403

## 代码规范约束

- 使用构造器注入（`@RequiredArgsConstructor` + `final`），禁止 `@Resource` 字段注入
- 有 `BaseService<T>` 时继承并用 `super(repository)` 传入
- Controller 统一返回 `AjaxResult`
- Java import 使用框架的全限定名（参见上文模板）
- 前端 import 使用 `@jiangood/open-admin` 包名（框架组件位于此包中）
- 直接输出代码，避免冗余说明；确保代码完整可运行

## 参考文档

框架已有以下文档，需要时查阅：

- `docs/api/backend/data-spec.md` — Spec 动态查询 API（所有操作符用法）
- `docs/api/backend/validators.md` — 校验注解列表（@ValidateMobile 等）
- `docs/api/backend/annotations.md` — 框架注解（ID 生成器、@RateLimit 等）
- `docs/api/frontend/components.md` — 前端组件 API
- `docs/api/frontend/field-components.md` — 字段组件完整文档
- `docs/api/frontend/system-components.md` — 视图/系统组件
- `docs/development/best-practices.md` — 最佳实践
- `docs/core-features/user-permission.md` — 权限系统详解
- `docs/core-features/data-dict.md` — 数据字典
