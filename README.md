# 项目文档

## 介绍

解析 `pom.xml` 和 `web/package.json`。

### 开发环境

*   **Maven 版本:** 0.0.2
*   **NPM 版本:** 0.0.1-beta.30

### 依赖说明

#### 后端依赖 (来自 `pom.xml`)

*   `spring-boot-starter-web`: Spring Boot Web Starter
*   `spring-boot-starter-quartz`: Spring Boot Quartz Scheduler Starter
*   `spring-boot-starter-validation`: Spring Boot Validation Starter
*   `spring-boot-starter-aop`: Spring Boot AOP Starter
*   `spring-boot-starter-data-jpa`: Spring Boot Data JPA Starter
*   `spring-boot-starter-cache`: Spring Boot Cache Starter
*   `spring-boot-starter-security`: Spring Boot Security Starter
*   `spring-boot-configuration-processor`: Spring Boot Configuration Processor
*   `mapstruct`: Java Bean映射框架
*   `filters`: 图像处理滤镜 (用于验证码)
*   `minio`: MinIO 对象存储客户端
*   `okhttp-jvm`: HTTP 客户端 (MinIO 依赖)
*   `mail`: JavaMail API
*   `poi-ooxml`, `poi-scratchpad`: Apache POI (Excel/Word处理)
*   `itextpdf`: PDF 处理库
*   `uuid-creator`: UUID 生成库
*   `commons-dbutils`: Apache Commons DBUtils
*   `hutool-all`: Hutool Java工具库
*   `commons-lang3`: Apache Commons Lang
*   `guava`: Google Guava 核心库
*   `commons-io`: Apache Commons IO
*   `jackson-dataformat-yaml`: Jackson YAML 数据格式处理器
*   `commons-beanutils`: Apache Commons BeanUtils
*   `pinyin4j`: 汉字转拼音库
*   `jsoup`: HTML 解析器
*   `lombok`: 简化Java Bean开发
*   `mysql-connector-j`: MySQL JDBC 驱动
*   `ureport-console`: 报表引擎控制台
*   `flowable-spring-boot-starter-process`: Flowable 流程引擎 Spring Boot Starter

#### 前端依赖 (来自 `web/package.json` 的 `peerDependencies`)

*   `@ant-design/icons`: Ant Design 图标库
*   `@bpmn-io/properties-panel`: BPMN.io 属性面板
*   `@tinymce/tinymce-react`: TinyMCE React 组件
*   `@umijs/types`: UmiJS 类型定义
*   `antd`: Ant Design UI 组件库
*   `antd-img-crop`: Ant Design 图片裁剪组件
*   `axios`: 基于 Promise 的 HTTP 客户端
*   `bpmn-js`: BPMN 2.0 渲染工具
*   `bpmn-js-properties-panel`: BPMN.js 属性面板
*   `preact`: 快速 3KB React 替代品
*   `dayjs`: 轻量级日期处理库
*   `jsencrypt`: RSA 加密/解密库
*   `lodash`: JavaScript 工具函数库
*   `qs`: URL 参数解析/字符串化库
*   `react`: React UI 库
*   `react-dom`: React 的 DOM 渲染器
*   `umi`: 可插拔的企业级前端框架

### 菜单列表

菜单配置在 `src/main/resources/application-data-framework.yml` 中定义，业务项目可在 `application-data-biz.yml` 中进行配置。
以下是部分菜单结构示例：

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
      seq: 10000
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
        # ... 其他菜单项
```

业务项目配置示例：

在 `application-data-biz.yml` 中可以新增或覆盖框架定义的菜单，例如：

```yaml
data:
  menus:
    - id: biz
      name: 业务管理
      seq: 100
      children:
        - id: bizStudent
          name: 学生管理
          path: /biz/student
          icon: UsergroupAddOutlined
          perms:
            - name: 查看
              perm: bizStudent:view
            - name: 保存
              perm: bizStudent:save
            - name: 删除
              perm: bizStudent:delete
```

### 其他文档链接

待补充...

## 前端

### 组件 (web/src/framework/components)

| 名称                    | 参数                                                                               | 说明                                                                                                                                                                                            |
| :---------------------- | :--------------------------------------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `DownloadFileButton`    | `url: string`, `params: object`, `children: ReactNode`                             | 下载文件按钮。点击后从指定 URL 下载文件，下载过程中显示加载弹窗。                                                                                                                               |
| `Ellipsis`              | `length: number (默认: 15)`, `children: ReactNode`, `pre: boolean (默认: false)`  | 文本省略组件。当文本超过指定长度时显示省略号，点击可查看完整内容（弹窗）。                                                                                                                    |
| `LinkButton`            | `path: string`, `label: string`, `children: ReactNode`, `size: string (默认: 'small')` | 链接按钮。点击后使用 `PageUtils.open` 跳转到指定路径。                                                                                                                                           |
| `NamedIcon`             | `name: string`                                                                     | 动态图标组件。根据传入的 `name` 渲染 Ant Design 图标。                                                                                                                                          |
| `PageLoading`           | `message?: string (默认: '页面加载中...')`                                         | 全屏页面加载指示器。在页面加载或数据请求时显示。                                                                                                                                              |
| `Gap`                   | `n: number (默认: 0)`                                                              | 垂直间距组件。根据 `n` 值调整高度 (`16 + 8 * n` px)。                                                                                                                                           |
| `Page`                  | `padding: boolean (默认: false)`, `backgroundGray: boolean (默认: false)`, `children: ReactNode` | 页面容器组件。提供可选的内边距和灰色背景。                                                                                                                                                      |
| `ProModal`              | `title: string`, `ref: any`, `onShow?: function`, `footer?: ReactNode`, `width?: number (默认: 800)`, `children: ReactNode` | 增强型模态框组件。基于 Ant Design `Modal`，提供 `show`/`hide` 等方法控制显示，并支持自定义标题、宽度、页脚。                                                                                        |
| `ProTable`              | `request: function`, `columns: array`, `rowKey: string (默认: "id")`, `toolBarRender?: function`, `actionRef?: Ref`, `defaultPageSize?: number`, `rowSelection?: boolean | object`, `toolbarOptions?: boolean`, `showToolbarSearch?: boolean`, `scrollY?: number`, `bordered?: boolean`, `children?: ReactNode`, `formRef?: Ref` | 高级表格组件。集成数据请求、分页、排序、搜索表单、行选择、工具栏等功能。                                                                                                                    |
| `system/ButtonList`     | `children: ReactNode | ReactNode[]`, `maxNum?: number`                             | 带权限的按钮列表。根据 `children` 中按钮的 `perm` 属性，结合 `PermUtils.hasPermission` 过滤显示。                                                                                              |
| `system/HasPerm`        | `code: string`, `children: ReactNode`                                              | 权限控制组件。只有当用户拥有指定 `code` 权限时才渲染 `children`。                                                                                                                               |
| `ValueType`             | 无（工具对象）                                                                     | 动态渲染组件的工具对象。提供 `renderView(type, props)` 和 `renderField(type, props)` 方法，根据类型字符串动态渲染对应的视图或表单字段组件。                                                             |
| `view/ViewBooleanEnableDisable` | `value: boolean \| null \| undefined`                                                | 布尔值显示组件。将布尔值显示为绿色“启动”或红色“禁用”标签。                                                                                                                                      |
| `view/ViewEllipsis`     | `maxLength: number (默认: 15)`, `value: string`                                  | 视图省略文本组件。截断长文本并显示省略号，鼠标悬停时显示 Popover，点击 Popover 中的按钮可查看完整内容。                                                                                          |
| `view/ViewFile`         | `value: string`, `height?: number`                                                 | 文件预览组件。根据文件 ID (支持逗号分隔) 显示单个或多个文件（通过 iframe 嵌入，多文件使用 Carousel）。                                                                                         |
| `view/ViewImage`        | `value: string \| string[]`                                                        | 图片显示组件。显示一张或多张图片（支持文件 ID 或 URL），点击图片可全屏预览。                                                                                                                    |
| `view/ViewText`         | `value: string \| null \| undefined`                                               | 纯文本显示组件。使用 Ant Design 的 `Typography.Text` 显示文本，`value` 为空时显示 `null`。                                                                                                     |
| `view/ViewRange`        | `min: any (默认: '未知')`, `max: any (默认: '未知')`                               | 范围显示组件。以 "min - max" 格式显示范围，`min` 或 `max` 为空时显示“未知”，两者都为空时不渲染。                                                                                                  |

### 工具类 (web/src/framework/utils)

| 名称                    | 说明                                                                       | 主要方法                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| :---------------------- | :------------------------------------------------------------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ArrUtils`              | 数组操作工具类。                                                           | `contains`, `add`, `remove`, `clear`, `sub`, `swap`, `insert`, `pushIfNotExist`, `pushAll`, `maxBy`, `unique`                                                                                                                                                                                                                                                                                                                                                                                          |
| `ColorsUtils`           | 颜色处理工具类。                                                           | `rgbToHex`, `rgbToString`, `hexToRgb`, `hsvToRgb`, `rgbToHsv`, `textToRgb`, `lighten`, `luminosity`, `brightness`, `blend`, `changeAlpha`                                                                                                                                                                                                                                                                                                                                                      |
| `DateUtils`             | 日期时间处理工具类。                                                       | `year`, `month`, `date`, `hour`, `minute`, `second`, `formatDate`, `formatTime`, `formatDateTime`, `formatDateCn`, `now`, `today`, `thisYear`, `thisMonth`, `friendlyTime`, `friendlyTotalTime`, `beginOfMonth`                                                                                                                                                                                                                                                                                                                                                                                        |
| `DeviceUtils`           | 设备和网络工具类。                                                         | `isMobileDevice`, `getWebsocketBaseUrl`                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| `DomUtils`              | DOM 操作工具类。                                                           | `offset`, `height`, `width`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `EventBusUtils`         | 事件总线工具类。                                                           | `on`, `once`, `emit`, `off`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `MessageUtils`          | 消息提示工具类。                                                           | `alert`, `confirm`, `prompt`, `success`, `error`, `warning`, `info`, `loading`, `hideAll`, `notify`, `notifySuccess`, `notifyError`, `notifyWarning`                                                                                                                                                                                                                                                                                                                                             |
| `ObjectUtils`           | 对象操作工具类。                                                           | `get`, `copyPropertyIfPresent`, `copyProperty`                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| `StorageUtils`          | 本地存储工具类。                                                           | `get`, `set`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `StringUtils`           | 字符串操作工具类。                                                         | `removePrefix`, `removeSuffix`, `random`, `nullText`, `contains`, `count`, `capitalize`, `reverse`, `subAfter`, `subAfterLast`, `subBefore`, `obfuscateString`, `pad`, `encrypt`, `decrypt`, `getWidth`, `cutByWidth`, `ellipsis`, `isStr`, `toCamelCase`, `toUnderlineCase`, `equalsIgnoreCase`, `split`, `join` |
| `TreeUtils`             | 树结构操作工具类。                                                         | `treeToList`, `findKeysByLevel`, `buildTree`, `walk`, `findByKey`, `findByKeyList`, `getSimpleList`, `getKeyList`                                                                                                                                                                                                                                                                                                                                                                                         |
| `UrlUtils`              | URL 处理工具类。                                                           | `getParams`, `getPathname`, `paramsToSearch`, `setParam`, `replaceParam` (deprecated), `join`                                                                                                                                                                                                                                                                                                                                                                                                        |
| `UuidUtils`             | UUID 生成工具类。                                                          | `uuidV4`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `ValidateUtils`         | 验证工具类。                                                               | `isEmail`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `system/DictUtils`      | 字典数据工具类。                                                           | `dictList`, `dictOptions`, `dictLabel`, `dictTag`                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `system/FormRegistryUtils` | 表单组件注册工具类。                                                       | `register`, `get`, `has`, `getAllKeys`                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `system/HttpUtils`      | HTTP 请求工具类。                                                          | `get`, `post`, `postForm`, `downloadFile`                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `system/PageUtils`      | 页面、路由工具类。                                                         | `redirectToLogin`, `currentParams`, `currentPathname`, `currentUrl`, `currentPathnameLastPart`, `open`, `openNoLayout`, `currentLabel`, `closeCurrent`                                                                                                                                                                                                                                                                                                                                           |
| `system/PermUtils`      | 权限控制工具类。                                                           | `hasPermission`, `isPermitted`, `notPermitted`                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| `system/SysUtils`       | 系统信息管理工具类。                                                       | `getHeaders`, `setSiteInfo`, `getSiteInfo`, `setLoginInfo`, `getLoginInfo`, `setDictInfo`, `getDictInfo`                                                                                                                                                                                                                                                                                                                                                                                       |
| `system/ThemeUtils`     | 主题工具类。                                                               | `getColor`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |

## 后端

### `pom.xml`

除了在介绍部分提到的依赖，`pom.xml` 还定义了项目的 Maven 构建配置、插件（如 `maven-javadoc-plugin`, `maven-source-plugin`, `maven-gpg-plugin` 用于发布到 Maven Central），以及多环境配置。

### `src/main/java/io/admin/common/utils/tree`

该包提供了通用的树结构处理工具。

#### `TreeNode.java`

*   **描述:** 一个接口，定义了树节点的基本结构 (`id`, `pid`, `children`) 和行为。
*   **方法:** `getId()`, `setId()`, `getPid()`, `setPid()`, `getChildren()`, `setChildren()`, `setIsLeaf()` (默认方法)。

#### `TreeUtils.java`

*   **描述:** 静态工具类，提供树结构操作的通用方法，例如从扁平列表构建树、树的遍历、查找等。它被 `TreeManager` 用于底层逻辑。
*   **主要方法:** `buildTree` (多种重载), `treeToMap` (多种重载), `cleanEmptyChildren`, `walk` (多种重载), `getLeafs`, `treeToList`, `getPids`。

#### `TreeManager.java`

*   **描述:** 树结构管理器，封装了树的构建、遍历、查询等高级操作。它维护树的结构和节点的映射，以提供高效的数据访问和操作。
*   **工厂方法:** `of(List<X> dataList)`: 创建 `TreeManager` 实例。
*   **主要方法:** `traverseTree` (多种重载), `traverseTreeFromLeaf`, `getSortedList`, `getParentById`, `getParent`, `isLeaf` (多种重载), `getLeafCount`, `getLeafList`, `getLeafIdList`, `getParentIdListById`, `getIdsByLevel`, `getLevelById`。

#### `src/main/java/io/admin/common/utils/tree/drop`

该子包专注于树节点拖拽操作的结果处理。

##### `DropResult.java`

*   **描述:** 数据类，用于封装树节点拖拽操作的结果，包括新的父节点键 (`parentKey`) 和子节点的排序键列表 (`sortedKeys`)。
*   **属性:** `parentKey`, `sortedKeys`。

##### `TreeDropUtils.java`

*   **描述:** 静态工具类，用于根据拖拽事件计算树节点拖拽后的新结构和排序。
*   **主要方法:** `onDrop(DropEvent e, List<TreeOption> tree)`, `resort(List<String> list, DropEvent e)`。

### `src/main/java/io/admin/framework/data/specification/Spec.java`

*   **描述:** 一个简洁、动态的 JPA `Specification` 构建器。它支持多种查询操作符（相等、模糊匹配、范围等），能够处理关联字段（如 "dept.name"），并允许通过链式调用组合查询条件，默认使用 `AND` 逻辑，也支持显式 `OR`。此外，还支持 `QueryByExample`、`DISTINCT`、`GROUP BY`、`HAVING` 和聚合函数。
*   **工厂方法:** `of()`
*   **主要方法:** `addExample`, `eq`, `ne`, `gt`, `lt`, `ge`, `le`, `like` (及变体), `notLike`, `in`, `between`, `isNotNull`, `isNull`, `distinct`, `or`, `not`, `orLike`, `isMember`, `isNotMember`, `groupBy`, `having`, `selectFnc`, `select`。

### `src/main/java/io/admin/framework/data/specification/ExpressionUtils.java`

*   **描述:** 静态工具类，用于解析 JPA `CriteriaQuery` 中的 `Path` 表达式。它能够处理点分隔的字段路径（如 "dept.name"），并根据需要自动执行 `INNER JOIN` 以导航实体关系。
*   **主要方法:** `getPath(Root<?> root, String field)`。

## 模板

本节以“学生管理”模块为例，说明如何基于现有框架生成业务代码模板。

### 1. 后端代码模板 (Java)

参照 `src/main/java/io/admin/modules/system` 目录结构，业务模块（如 `modules/biz`）通常包含 `entity`, `dao`, `service`, `controller` 等层。

#### `StudentEntity.java` (实体类)

```java
package io.admin.modules.biz.entity;

import io.admin.framework.data.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "biz_student")
public class StudentEntity extends BaseEntity {
    private String name; // 学生姓名
    private Integer age; // 年龄
    private String studentNo; // 学号
    // ... 其他业务字段
}
```

#### `StudentDao.java` (数据访问层)

```java
package io.admin.modules.biz.dao;

import io.admin.framework.data.jpa.BaseDao;
import io.admin.modules.biz.entity.StudentEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentDao extends BaseDao<StudentEntity> {
    // 根据需要添加自定义查询方法
    StudentEntity findByStudentNo(String studentNo);
}
```

#### `StudentService.java` (业务逻辑层)

```java
package io.admin.modules.biz.service;

import io.admin.framework.common.service.BaseService;
import io.admin.modules.biz.dao.StudentDao;
import io.admin.modules.biz.entity.StudentEntity;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class StudentService extends BaseService<StudentEntity, StudentDao> {

    // 示例：分页查询
    public Page<StudentEntity> queryPage(Specification<StudentEntity> spec, Pageable pageable) {
        return baseDao.findAll(spec, pageable);
    }

    // 示例：保存学生
    public StudentEntity saveStudent(StudentEntity student) {
        return baseDao.save(student);
    }

    // 示例：根据ID获取学生
    public Optional<StudentEntity> getById(String id) {
        return baseDao.findById(id);
    }

    // ... 其他业务方法
}
```

#### `StudentController.java` (控制层)

```java
package io.admin.modules.biz.controller;

import io.admin.framework.common.api.R;
import io.admin.framework.data.specification.Spec;
import io.admin.modules.biz.entity.StudentEntity;
import io.admin.modules.biz.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/biz/student")
@AllArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * 分页查询学生列表
     */
    @GetMapping("/page")
    public R<Page<StudentEntity>> page(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       String name, String studentNo) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Spec<StudentEntity> spec = Spec.<StudentEntity>of()
                .like("name", name)
                .eq("studentNo", studentNo);
        Page<StudentEntity> studentPage = studentService.queryPage(spec, pageable);
        return R.ok(studentPage);
    }

    /**
     * 保存或更新学生
     */
    @PostMapping("/save")
    public R<StudentEntity> save(@RequestBody StudentEntity student) {
        StudentEntity savedStudent = studentService.saveStudent(student);
        return R.ok(savedStudent);
    }

    /**
     * 根据ID删除学生
     */
    @DeleteMapping("/delete/{id}")
    public R<String> delete(@PathVariable String id) {
        studentService.deleteById(id);
        return R.ok("删除成功");
    }

    /**
     * 根据ID获取学生详情
     */
    @GetMapping("/get/{id}")
    public R<StudentEntity> get(@PathVariable String id) {
        return R.ok(studentService.getById(id).orElse(null));
    }
}
```

### 2. 菜单配置 (`application-data-biz.yml`)

在您的业务项目 `src/main/resources/application-data-biz.yml` 中配置菜单，使前端能够显示学生管理页面。

```yaml
data:
  menus:
    - id: biz
      name: 业务管理
      seq: 100 # 调整排序
      children:
        - id: bizStudent # 页面唯一ID
          name: 学生管理 # 菜单名称
          path: /biz/student # 前端路由路径
          icon: TeamOutlined # Ant Design 图标名称，例如：TeamOutlined
          perms:
            - perm: bizStudent:view # 查看权限
              name: 查看
            - perm: bizStudent:save # 保存权限
              name: 保存
            - perm: bizStudent:delete # 删除权限
              name: 删除
            # ... 其他权限
```

### 3. 前端页面模板 (`web/src/pages/biz/student/index.jsx`)

参照 `web/src/pages/system/sysManual/index.jsx`，创建一个学生列表页面。

```jsx
import React, { useRef } from 'react';
import { Button, message, Space } from 'antd';
import { PlusOutlined, DeleteOutlined, EditOutlined } from '@ant-design/icons';
import { ProTable } from '@/framework/components/ProTable';
import { ProModal } from '@/framework/components/ProModal';
import { MessageUtils } from '@/framework/utils/MessageUtils';
import { HttpUtils } from '@/framework/utils/system/HttpUtils';
import { FormRegistryUtils } from '@/framework/utils/system/FormRegistryUtils'; // 假设你有一个学生表单

// 定义学生表单组件 (假设已经定义并注册)
// FormRegistryUtils.register('studentForm', StudentForm);

export default () => {
  const tableRef = useRef();
  const modalRef = useRef();
  const currentRecord = useRef({}); // 用于存储当前编辑的记录

  const columns = [
    {
      title: '学号',
      dataIndex: 'studentNo',
      align: 'center',
    },
    {
      title: '姓名',
      dataIndex: 'name',
      align: 'center',
    },
    {
      title: '年龄',
      dataIndex: 'age',
      align: 'center',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      valueType: 'date',
      align: 'center',
    },
    {
      title: '操作',
      valueType: 'option',
      align: 'center',
      render: (text, record, _, action) => [
        <Button
          type="link"
          icon={<EditOutlined />}
          onClick={() => handleEdit(record)}
          key="edit"
        >
          编辑
        </Button>,
        <Button
          type="link"
          danger
          icon={<DeleteOutlined />}
          onClick={() => handleDelete(record)}
          key="delete"
        >
          删除
        </Button>,
      ],
    },
  ];

  const handleAdd = () => {
    currentRecord.current = {}; // 清空当前记录
    modalRef.current.show();
  };

  const handleEdit = (record) => {
    currentRecord.current = record; // 设置当前编辑记录
    modalRef.current.show();
  };

  const handleDelete = async (record) => {
    const confirmed = await MessageUtils.confirm(`确定要删除学生 ${record.name} 吗？`);
    if (confirmed) {
      HttpUtils.delete(`/admin/biz/student/delete/${record.id}`).then(() => {
        message.success('删除成功');
        tableRef.current.reload(); // 刷新表格
      });
    }
  };

  const handleModalFinish = (values) => {
    const data = { ...currentRecord.current, ...values };
    HttpUtils.post('/admin/biz/student/save', data).then(() => {
      message.success('保存成功');
      modalRef.current.hide();
      tableRef.current.reload(); // 刷新表格
    });
  };

  return (
    <>
      <ProTable
        actionRef={tableRef}
        request={(params) => HttpUtils.get('/admin/biz/student/page', params)}
        columns={columns}
        rowKey="id"
        // 搜索表单项
        // children={
        //   <>
        //     <Form.Item name="name" label="姓名">
        //       <Input placeholder="请输入姓名" />
        //     </Form.Item>
        //     <Form.Item name="studentNo" label="学号">
        //       <Input placeholder="请输入学号" />
        //     </Form.Item>
        //   </>
        // }
        toolBarRender={() => [
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd} key="add">
            新增
          </Button>,
        ]}
      />
      <ProModal title="学生信息" ref={modalRef} onShow={() => {
        // 在 Modal 显示时，可以根据 currentRecord.current 初始化表单
        // 例如：formInstance.setFieldsValue(currentRecord.current);
      }}>
        {/* 实际的学生表单组件，需要自行定义 */}
        {/* <StudentForm initialValues={currentRecord.current} onFinish={handleModalFinish} /> */}
        <div>请在此处放置你的学生表单组件，并处理提交逻辑。</div>
        <Button type="primary" onClick={() => handleModalFinish({ /* 表单数据 */ })}>保存 (占位)</Button>
      </ProModal>
    </>
  );
};
```