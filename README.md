# springboot-admin-starter

## 1 介绍

本文档介绍了 `springboot-admin-starter` 框架，这是一个为业务项目提供后端管理系统基础功能的框架。该框架提供了完整的后端管理功能，包括用户管理、权限控制、系统配置、作业调度、流程引擎等核心功能，业务项目可以基于该框架快速搭建管理系统。

### 目录
- [1 介绍](#1-介绍)
- [2 前端](#2-前端)
- [3 后端](#3-后端)
- [4 模板代码](#4-模板代码)

### 开发环境

- **Java版本**: 17
- **Spring Boot版本**: 3.5.8
- **数据库**: MySQL
- **前端框架**: React 19.0.0
- **UI组件库**: Ant Design 6.x

### 版本信息

后端最新版本为： ![Maven Version](https://img.shields.io/maven-central/v/io.github.jiangood/springboot-admin-starter)

前端最新版本为： ![NPM Version](https://img.shields.io/npm/v/@jiangood/springboot-admin-starter)

### 后端依赖

| 依赖项 | 版本 | 描述 |
|--------|------|------|
| `org.springframework.boot:spring-boot-starter-web` | - | Spring Boot Web模块 |
| `org.springframework.boot:spring-boot-starter-quartz` | - | 作业调度模块 |
| `org.springframework.boot:spring-boot-starter-validation` | - | 参数验证模块 |
| `org.springframework.boot:spring-boot-starter-aop` | - | AOP模块 |
| `org.springframework.boot:spring-boot-starter-data-jpa` | - | JPA数据访问模块 |
| `org.springframework.boot:spring-boot-starter-cache` | - | 缓存模块 |
| `org.springframework.boot:spring-boot-starter-security` | - | 安全模块 |
| `org.mapstruct:mapstruct` | 1.6.3 | MapStruct映射工具 |
| `org.mapstruct:mapstruct-processor` | 1.6.3 | MapStruct注解处理器 |
| `com.jhlabs:filters` | 2.0.235-1 | 图像处理滤镜 |
| `io.minio:minio` | 8.6.0 | MinIO对象存储 |
| `javax.mail:mail` | 1.4.7 | 邮件发送 |
| `org.apache.poi:poi-ooxml` | 5.5.0 | Excel导入导出 |
| `org.apache.poi:poi-scratchpad` | 5.5.0 | POI文档处理 |
| `com.itextpdf:itextpdf` | 5.5.13.4 | PDF处理 |
| `com.github.f4b6a3:uuid-creator` | 6.1.1 | UUID生成器 |
| `commons-dbutils:commons-dbutils` | 1.8.1 | DBUtils工具库 |
| `cn.hutool:hutool-all` | 5.8.41 | Hutool工具包 |
| `org.apache.commons:commons-lang3` | - | Commons Lang3 |
| `com.google.guava:guava` | 33.5.0-jre | Google Guava工具库 |
| `commons-io:commons-io` | 2.21.0 | Commons IO工具库 |
| `org.flowable:flowable-spring-boot-starter-process` | 7.2.0 | Flowable流程引擎 |

### 前端依赖

| 依赖项 | 版本 | 描述 |
|--------|------|------|
| `@ant-design/icons` | ^5.4.0 | Ant Design图标库 |
| `antd` | ^6.0.0 | Ant Design UI组件库 |
| `axios` | ^1.13.2 | HTTP客户端 |
| `bpmn-js` | ^18.7.0 | BPMN流程图引擎 |
| `bpmn-js-properties-panel` | ^5.43.0 | BPMN属性面板 |
| `dayjs` | ^1.11.13 | 时间处理库 |
| `jsencrypt` | ^3.5.4 | 加密库 |
| `lodash` | ^4.17.21 | 工具函数库 |
| `qs` | ^6.14.0 | 查询字符串处理 |
| `react` | ^19.0.0 | React框架 |
| `react-dom` | ^19.0.0 | React DOM渲染 |
| `umi` | ^4.0.0 | 前端框架 |
| `antd-img-crop` | ^4.23.0 | 图片裁剪组件 |
| `@tinymce/tinymce-react` | ^6.0.0 | 富文本编辑器 |

---

## 2 前端

### 目录
- [组件](#组件)
- [工具函数](#工具函数)
- [字段组件](#字段组件)
- [视图组件](#视图组件)
- [页面组件](#页面组件)

### 组件

框架提供了丰富的前端组件，用于快速构建管理界面。

#### ProModal
**ProModal** 是一个高级弹窗组件，支持表单验证、数据提交等功能。

#### ProTable
**ProTable** 是一个高级表格组件，支持分页、搜索、排序等功能。

#### Page
**Page** 是页面布局组件，提供标准的页面结构。

#### Gap
**Gap** 是间距组件，用于控制元素之间的间距。

#### DownloadFileButton
**DownloadFileButton** 是下载文件按钮组件，支持文件下载功能。

#### Ellipsis
**Ellipsis** 是省略号组件，用于文本过长时显示省略号。

#### LinkButton
**LinkButton** 是链接按钮组件，支持跳转功能。

#### NamedIcon
**NamedIcon** 是命名图标组件，支持动态图标显示。

#### PageLoading
**PageLoading** 是页面加载组件，提供加载状态显示。

### 工具函数

框架提供了一些常用的工具函数。

### 字段组件

字段组件用于表单中的各种输入字段。

### 视图组件

视图组件用于数据显示和展示。

### 页面组件

页面组件用于构建完整页面。

---

## 3 后端

### 目录
- [菜单列表](#菜单列表)
- [业务项目配置](#业务项目配置)
- [数据规范](#数据规范)
- [树工具](#树工具)
- [注解工具](#注解工具)
- [Excel工具](#excel工具)
- [JDBC工具](#jdbc工具)
- [ID生成器](#id生成器)
- [数据转换器](#数据转换器)
- [验证器](#验证器)
- [登录工具](#登录工具)
- [作业调度](#作业调度)
- [流程引擎](#流程引擎)
- [系统配置](#系统配置)

### 菜单列表

框架提供了完整的菜单列表配置，包括：

- **我的任务**
  - 路径: `/flowable/task`
  - 权限: `myFlowableTask:manage`

- **系统管理**
  - **机构管理**: `/system/org` - 权限: `sysOrg:save`, `sysOrg:view`, `sysOrg:delete`
  - **用户管理**: `/system/user` - 权限: `sysUser:view`, `sysUser:save`, `sysUser:delete`, `sysUser:resetPwd`, `sysUser:grantPerm`
  - **角色管理**: `/system/role` - 权限: `sysRole:save`
  - **操作手册**: `/system/sysManual` - 权限: `sysManual:view`, `sysManual:delete`, `sysManual:save`
  - **系统设置**: `/system/config` - 权限: `sysConfig:view`, `sysConfig:save`
  - **数据字典**: `/system/dict` - 权限: `sysDict:view`, `sysDict:save`, `sysDict:delete`
  - **存储文件**: `/system/file` - 权限: `sysFile:view`, `sysFile:delete`
  - **作业调度**: `/system/job` - 权限: `job:view`, `job:save`, `job:triggerJob`, `job:jobLogClean`
  - **操作日志**: `/system/log` - 权限: `sysLog:view`
  - **接口管理**: `/system/api` - 权限: `api`
  - **流程引擎**: `/flowable` - 权限: `flowableModel:design`, `flowableModel:deploy`, `flowableTask:setAssignee`, `flowableInstance:close`
  - **报表管理**: `/ureport` - 权限: `ureport:view`, `ureport:design`

### 业务项目配置

业务项目可以通过 `application-data-biz.yml` 文件进行配置。

### 数据规范

**Spec.java** 提供了简洁、动态、支持关联字段查询的 JPA Specification 构建器。

#### 主要功能
- **基础查询**: `eq` (等于), `ne` (不等于), `gt` (大于), `lt` (小于), `like` (模糊查询), `in` (包含)
- **高级查询**: `between` (范围查询), `isNotNull` (非空), `isNull` (为空), `or` (或条件)
- **聚合函数**: `selectFnc` (支持 AVG, SUM, MIN, MAX, COUNT)
- **分组查询**: `groupBy` (分组), `having` (分组后条件)
- **集合操作**: `isMember` (成员判断), `isNotMember` (非成员判断)

### 树工具

**TreeUtils.java** 提供了将列表转换为树结构的功能。

#### 主要方法
- `buildTree`: 构建树结构
- `treeToMap`: 将树转换为Map
- `buildTreeByDict`: 基于Dict构建树
- `walk`: 递归遍历树
- `getLeafs`: 获取叶子节点
- `treeToList`: 将树转换为列表
- `getPids`: 获取节点父ID路径

### 注解工具

**RemarkUtils.java** 提供了获取注解信息的工具方法。

#### 主要方法
- `getRemark(Field)`: 获取字段注解信息
- `getRemark(Class)`: 获取类注解信息
- `getRemark(Enum)`: 获取枚举注解信息
- `getRemark(Method)`: 获取方法注解信息

### Excel工具

**ExcelUtils.java** 提供了Excel文件的导入导出功能。

#### 主要方法
- `importExcel`: Excel导入
- `exportExcel`: Excel导出

### JDBC工具

**JdbcUtils.java** 提供了原生SQL的操作工具。

#### 主要方法
- `update`: 执行更新操作
- `batchUpdate`: 批量更新操作
- `findOne`: 查询单条记录
- `findAll`: 查询多条记录
- `findScalar`: 查询标量值
- `findLong`: 查询长整型值
- `findInteger`: 查询整型值
- `findColumnList`: 查询列列表
- `exists`: 检查记录是否存在
- `findMapDict`: 查询字典Map
- `findBeanMap`: 查询Bean映射
- `findAll` (分页): 分页查询
- `insert`: 插入记录
- `updateById`: 按ID更新
- `tableExists`: 检查表是否存在
- `columnExists`: 检查列是否存在
- `generateCreateTableSql`: 生成建表SQL

### ID生成器

框架提供了ID生成器功能，用于生成唯一的ID。

### 数据转换器

框架提供了多种数据转换器：

| 转换器 | 描述 |
|--------|------|
| `BaseCodeEnumConverter` | 枚举转换器 |
| `BaseConverter` | 基础转换器 |
| `BaseToListConverter` | 转换为列表 |
| `ToBigDecimalListConverter` | 转换为BigDecimal列表 |
| `ToEntryListConverter` | 转换为Entry列表 |
| `ToIntListConverter` | 转换为整型列表 |
| `ToListBracketConverter` | 转换为带括号的列表 |
| `ToListComplexConverter` | 复杂列表转换器 |
| `ToListConverter` | 列表转换器 |
| `ToLongListConverter` | 转换为长整型列表 |
| `ToMapConverter` | 转换为Map |
| `ToMapStringObjectConverter` | 转换为Map<String,Object> |
| `ToPositionConverter` | 转换为位置 |
| `ToQueryStringMapConverter` | 转换为查询字符串Map |
| `ToSetComplexConverter` | 复杂集合转换器 |
| `ToSetConverter` | 集合转换器 |

### 验证器

框架提供了多种验证器：

| 验证器 | 描述 |
|--------|------|
| `ValidateCarDrivingLicence` | 验证驾驶证 |
| `ValidateChineseName` | 验证中文姓名 |
| `ValidateContainsChinese` | 验证包含中文 |
| `ValidateCreditCode` | 验证信用代码 |
| `ValidateDate` | 验证日期 |
| `ValidateGeneral` | 通用验证 |
| `ValidateHex` | 验证十六进制 |
| `ValidateIdNum` | 验证身份证号 |
| `ValidateIp` | 验证IP地址 |
| `ValidateIpv4` | 验证IPv4地址 |
| `ValidateMobile` | 验证手机号 |
| `ValidateNotContainsChinese` | 验证不包含中文 |
| `ValidatePassword` | 验证密码 |
| `ValidatePlateNumber` | 验证车牌号 |
| `ValidateStartWithLetter` | 验证以字母开头 |
| `ValidateYearMonth` | 验证年月 |
| `ValidateYearQuarter` | 验证年季度 |
| `ValidateZipCode` | 验证邮编 |

### 登录工具

**LoginUtils.java** 提供了获取当前登录用户信息的工具方法。

#### 主要方法
- `getUserId`: 获取当前用户ID
- `getUser`: 获取当前用户对象
- `getOrgPermissions`: 获取组织权限
- `getPermissions`: 获取用户权限
- `getRoles`: 获取用户角色
- `isAdmin`: 检查是否为管理员

### 作业调度

框架提供了作业调度功能，支持定时任务。

#### BaseJob
**BaseJob.java** 是所有作业任务的基类，提供了统一的日志记录和异常处理。

#### JobDescription
**JobDescription.java** 是作业描述注解，用于配置作业任务参数。

#### 示例作业
**HelloWorldJob.java** 是一个作业调度的示例任务。

### 流程引擎

框架集成了Flowable流程引擎，支持工作流程管理。

#### 示例流程监听器
**LeaveProcessListener.java** 是请假流程的示例监听器。

### 系统配置

框架提供了系统配置功能，支持在运行时修改系统参数。

#### 配置项
- **邮件配置**: 邮件发送账号、密码
- **网站配置**: 版权信息、登录背景图、登录框提示、站点标题、水印开关
- **系统配置**: 请求基础地址、JWT密码

#### 流程配置
`application-process.yml` 文件定义了流程引擎的配置。

---

## 4 模板代码

### 目录
- [实体类模板](#实体类模板)
- [数据访问对象模板](#数据访问对象模板)
- [服务类模板](#服务类模板)
- [控制器模板](#控制器模板)
- [前端页面模板](#前端页面模板)

### 实体类模板

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
 * ${entity_name}实体类
 * 注意：(${unique_fields}) 必须全局唯一
 */
@Remark("${entity_remark}")
@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(name = "uk_${table_name}", columnNames = {"${field1}", "${field2}"})})
@FieldNameConstants
public class ${EntityName} extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * ${field1_name}，最大长度100字符，不能为空
     */
    @NotNull
    @Remark("${field1_remark}")
    @Column(length = 100)
    @Size(max = 100, message = "${field1_name}长度不能超过100个字符")
    private String ${field1};

    /**
     * ${field2_name}，必须为正整数，不能为空
     */
    @NotNull
    @Remark("${field2_remark}")
    @Positive(message = "${field2_name}必须为正整数")
    private Integer ${field2};

    /**
     * ${field3_name}，最大长度32字符
     */
    @Remark("${field3_remark}")
    @Column(length = 32)
    @Size(max = 32, message = "${field3_name}长度不能超过32个字符")
    private String ${field3};

}
```

### 数据访问对象模板

```java
package io.admin.modules.system.dao;

import io.admin.framework.data.repository.BaseDao;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.system.entity.${EntityName};
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class ${EntityName}Dao extends BaseDao<${EntityName}> {

    public int findMax${Field2}(String ${field1}){
        Spec<${EntityName}> q = Spec.<${EntityName}>of().eq(${EntityName}.Fields.${field1}, ${field1});

        ${EntityName} e = this.findTop1(q, Sort.by(Sort.Direction.DESC, ${EntityName}.Fields.${field2}));

        return e == null ? 0 : e.get${Field2}();
    }

}
```

### 服务类模板

```java
package io.admin.modules.system.service;

import io.admin.framework.data.service.BaseService;
import io.admin.modules.system.dao.${EntityName}Dao;
import io.admin.modules.system.entity.${EntityName};
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ${EntityName}Service extends BaseService<${EntityName}> {

    @Resource
    ${EntityName}Dao dao;

    @Override
    public ${EntityName} saveOrUpdateByRequest(${EntityName} input, List<String> updateKeys) throws Exception {
        if(input.isNew()){
            int max${Field2} = dao.findMax${Field2}(input.get${Field1}());
            input.set${Field2}(max${Field2}+1);
        }

        return super.saveOrUpdateByRequest(input, updateKeys);
    }
}
```

### 控制器模板

```java
package io.admin.modules.system.controller;

import io.admin.common.dto.AjaxResult;
import io.admin.framework.config.argument.RequestBodyKeys;
import io.admin.framework.config.security.HasPermission;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.system.entity.${EntityName};
import io.admin.modules.system.service.${EntityName}Service;
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
@RequestMapping("admin/${entity_name}")
public class ${EntityName}Controller {

    @Resource
    ${EntityName}Service service;

    @HasPermission("${entity_perm}:view")
    @RequestMapping("page")
    public AjaxResult page(String searchText, @PageableDefault(direction = Sort.Direction.DESC, sort = "updateTime") Pageable pageable) throws Exception {
        Spec<${EntityName}> q = Spec.of();
        q.orLike(searchText, ${EntityName}.Fields.${field1});

        Page<${EntityName}> page = service.findPageByRequest(q, pageable);

        return AjaxResult.ok().data(page);
    }

    @HasPermission("${entity_perm}:save")
    @PostMapping("save")
    public AjaxResult save(@RequestBody ${EntityName} input, RequestBodyKeys updateFields) throws Exception {
        service.saveOrUpdateByRequest(input, updateFields);
        return AjaxResult.ok().msg("保存成功");
    }

    @HasPermission("${entity_perm}:delete")
    @RequestMapping("delete")
    public AjaxResult delete(String id) {
        service.deleteByRequest(id);
        return AjaxResult.ok().msg("删除成功");
    }

}
```

### 前端页面模板

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
            title: '${field1_remark}',
            dataIndex: '${field1}',
        },
        {
            title: '${field2_remark}',
            dataIndex: '${field2}',
            render(${field2}) {
                return 'v' + ${field2};
            }
        },
        {
            title: '${field3_remark}',
            dataIndex: '${field3}',
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
                    <Button size='small' perm='${entity_perm}:save' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='${entity_perm}:delete' title='是否确定删除${entity_remark}'  onConfirm={() => this.handleDelete(record)}>
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
        HttpUtils.post('admin/${entity_name}/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }

    handleDelete = record => {
        HttpUtils.get('admin/${entity_name}/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    render() {
        return <Page>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={(params, {selectedRows, selectedRowKeys}) => {
                    return <ButtonList>
                        <Button perm='${entity_perm}:save' type='primary' onClick={this.handleAdd}>
                            <PlusOutlined/> 新增
                        </Button>
                    </ButtonList>
                }}
                request={(params) => HttpUtils.get('admin/${entity_name}/page', params)}
                columns={this.columns}
            />

            <Modal title='${entity_remark}'
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

                    <Form.Item label='${field1_remark}' name='${field1}' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>

                    <Form.Item label='${field3_remark}' name='${field3}' rules={[{required: true}]}>
                        <FieldUploadFile accept=".pdf" maxCount={1} />
                    </Form.Item>

                </Form>
            </Modal>
        </Page>
    }
}
```