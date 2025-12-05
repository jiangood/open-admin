# springboot-admin-starter
![Maven Version](https://img.shields.io/maven-central/v/io.github.jiangood/springboot-admin-starter)
## 简介

`springboot-admin-starter` 是一个功能强大的企业级应用快速开发入门套件，旨在提供一个现成的、集成了常用后台管理功能的基础平台。

它采用前后端分离的架构，后端基于 Spring Boot，前端基于 Ant Design，帮助开发者快速启动新项目，并专注于业务逻辑的实现。

---

## 功能列表
- 系统管理 
- 机构管理 
- 用户管理
- 角色管理 
- 操作手册 
- 系统配置
- 数据字典 
- 文件管理 

- 操作日志 
- 接口管理 
- 作业调度
- 流程引擎 
- 报表管理

## 前端框架 `web/src/framework`

### 组件

| 组件名 | 描述 |
| --- | --- |
| `DownloadFileButton` | 一个点击后会触发文件下载并显示加载状态的按钮。 |
| `LinkButton` | 一个用于页面导航的按钮。 |
| `NamedIcon` | 根据名称渲染一个 Ant Design 图标。 |
| `PageLoading` | 在页面加载时显示一个居中的加载指示器。 |

### 表单域组件

| 组件名 | 描述 |
| --- | --- |
| `FieldBoolean` | 提供多种样式（单选、复选框、下拉、开关）的布尔值输入组件。 |
| `FieldDate` | 根据指定的类型（年、月、日、时间等）渲染一个日期或时间选择器。 |
| `FieldDateRange` | 与 `FieldDate` 类似，但用于选择日期或时间范围。 |
| `FieldDictSelect` | 从数据字典中加载选项的下拉选择框。 |
| `FieldEditor` | 一个富文本编辑器，基于 TinyMCE。 |
| `FieldPercent` | 用于输入百分比值的组件，会将输入值在 0-100 范围和 0-1 范围之间转换。 |
| `FieldRemoteSelect` | 从远程 URL 异步加载数据的下拉选择框。 |
| `FieldRemoteSelectMultiple` | `FieldRemoteSelect` 的多选版本。 |
| `FieldRemoteSelectMultipleInline` | `FieldRemoteSelectMultiple` 的变体，其值是逗号分隔的字符串。 |
| `FieldRemoteTree` | 从远程 URL 加载数据的树形组件，支持多选。 |
| `FieldRemoteTreeCascader` | 从远程 URL 加dawisu据的级联选择器。 |
| `FieldRemoteTreeSelect` | 从远程 URL 加载数据的树形下拉选择框。 |
| `FieldRemoteTreeSelectMultiple` | `FieldRemoteTreeSelect` 的多选版本。 |
| `FieldSysOrgTree` | 用于显示系统组织架构的树形组件（部门或单位）。 |
| `FieldSysOrgTreeSelect` | 用于选择系统组织架构的树形下拉框。 |
| `FieldTable` | 一个可编辑的表格组件，支持动态增删行。 |
| `FieldTableSelect` | 一个下拉选择框，其弹出层是一个功能完备的表格，支持搜索和分页。 |
| `FieldUploadFile` | 文件上传组件，支持图片裁剪和预览。 |

### 视图组件

| 组件名 | 描述 |
| --- | --- |
| `ViewBoolean` | 将布尔值 `true` 和 `false` 显示为 "是" 和 "否"。 |
| `ViewPassword` | 用于显示密码，提供一个开关来切换密文和明文。 |

### 工具类

| 工具类名 | 描述 |
| --- | --- |
| `ArrUtils` | 提供了一系列数组操作的静态方法，如添加、删除、查找等。 |
| `ColorsUtils` | 用于颜色转换和操作的工具类，支持 RGB, HSV 和 Hex 格式。 |
| `DateUtils` | 提供日期格式化、解析和人性化时间显示等功能。 |
| `DeviceUtils` | 用于检测设备类型（如移动设备）和获取环境信息（如 WebSocket URL）。 |
| `DomUtils` | 提供了获取 DOM 元素尺寸和位置的方法。 |
| `EventBusUtils` | 一个静态的全局事件总线，用于应用内的消息传递。 |
| `MessageUtils` | 封装了 Ant Design 的 `Modal`, `message`, 和 `notification`，提供统一的 API。 |
| `ObjectUtils` | 提供了安全地获取和复制对象属性的方法。 |
| `StorageUtils` | 封装了 `localStorage`，提供带时间戳的数据存储和读取功能。 |
| `StringUtils` | 提供了丰富的字符串操作方法，如大小写转换、填充、加解密等。 |
| `TreeUtils` | 用于树状数据结构的转换、遍历和查找等操作。 |
| `UrlUtils` | 提供了操作 URL 参数、路径拼接等功能。 |
| `UuidUtils` | 用于生成 v4 UUID。 |
| `ValidateUtils` | 提供了数据验证的功能，目前支持邮箱格式验证。 |
