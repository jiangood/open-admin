# 目录

- [admin-spring-boot-starter 使用说明书](#admin-spring-boot-starter-使用说明书)
- [介绍](#介绍)
  - [项目依赖](#项目依赖)
  - [核心功能](#核心功能)
- [前端](#前端)
  - [组件](#组件)
    - [ProTable](#protable)
    - [ProModal](#promodal)
    - [Ellipsis](#ellipsis)
    - [LinkButton](#linkbutton)
    - [DownloadFileButton](#downloadfilebutton)
    - [PageLoading](#pageloading)
    - [NamedIcon](#namedicon)
    - [Page](#page)
  - [字段组件](#字段组件)
    - [FieldRemoteSelect](#fieldremoteselect)
    - [FieldRemoteSelectMultiple](#fieldremoteselectmultiple)
    - [FieldRemoteSelectMultipleInline](#fieldremoteselectmultipleinline)
    - [FieldRemoteTree](#fieldremotetree)
    - [FieldDictSelect](#fielddictselect)
    - [FieldEditor](#fieldeditor)
    - [FieldRemoteTreeCascader](#fieldremotetreecascader)
    - [FieldRemoteTreeSelect](#fieldremotetreeselect)
    - [FieldRemoteTreeSelectMultiple](#fieldremotetreeselectmultiple)
    - [FieldBoolean](#fieldboolean)
    - [FieldDate](#fielddate)
    - [FieldDateRange](#fielddaterange)
    - [FieldTable](#fieldtable)
    - [FieldTableSelect](#fieldtableselect)
    - [FieldSysOrgTree](#fieldsysorgtree)
    - [FieldSysOrgTreeSelect](#fieldsysorgtreeselect)
    - [FieldPercent](#fieldpercent)
    - [FieldUploadFile](#fielduploadfile)
  - [工具类](#工具类)
    - [HttpUtils](#httputils)
    - [MessageUtils](#messageutils)
    - [DateUtils](#dateutils)
    - [ArrUtils](#arrutils)
    - [UrlUtils](#urlutils)
    - [StringUtils](#stringutils)
    - [EventBusUtils](#eventbusutils)
    - [ColorsUtils](#colorsutils)
    - [DomUtils](#domutils)
    - [UuidUtils](#uuidutils)
    - [TreeUtils](#treeutils)
    - [StorageUtils](#storageutils)
    - [DeviceUtils](#deviceutils)
    - [ObjectUtils](#objectutils)
    - [ValidateUtils](#validateutils)
- [后端](#后端)
  - [数据规范相关](#数据规范相关)
    - [Spec](#spec)
  - [树相关工具](#树相关工具)
    - [TreeUtils](#treeutils-1)
  - [注解工具](#注解工具)
    - [RemarkUtils](#remarkutils)
  - [Excel工具](#excel工具)
    - [ExcelUtils](#excelutils)
  - [数据库工具](#数据库工具)
    - [JdbcUtils](#jdbcutils)
  - [ID生成相关注解](#id生成相关注解)
    - [GeneratePrefixedSequence](#generateprefixedsequence)
    - [GenerateUuidV7](#generateuuidv7)
  - [数据类型转换器](#数据类型转换器)
    - [BaseCodeEnumConverter](#basecodeenumconverter)
    - [BaseConverter](#baseconverter)
    - [BaseToListConverter](#basetolistconverter)
    - [ToBigDecimalListConverter](#tobigdecimallistconverter)
    - [ToEntryListConverter](#toentrylistconverter)
    - [ToIntListConverter](#tointlistconverter)
    - [ToListBracketConverter](#tolistbracketconverter)
    - [ToListComplexConverter](#tolistcomplexconverter)
    - [ToListConverter](#tolistconverter)
    - [ToLongListConverter](#tolonglistconverter)
    - [ToMapConverter](#tomapconverter)
    - [ToMapStringObjectConverter](#tomapstringobjectconverter)
    - [ToPositionConverter](#topositionconverter)
    - [ToQueryStringMapConverter](#toquerystringmapconverter)
    - [ToSetComplexConverter](#tosetcomplexconverter)
    - [ToSetConverter](#tosetconverter)
  - [数据验证注解](#数据验证注解)
    - [ValidateCarDrivingLicence](#validatecardrivinglicence)
    - [ValidateChineseName](#validatechinesename)
    - [ValidateContainsChinese](#validatecontainschinese)
    - [ValidateCreditCode](#validatecreditcode)
    - [ValidateDate](#validatedate)
    - [ValidateGeneral](#validategeneral)
    - [ValidateHex](#validatehex)
    - [ValidateIdNum](#validateidnum)
    - [ValidateIp](#validateip)
    - [ValidateIpv4](#validateipv4)
    - [ValidateMobile](#validatemobile)
    - [ValidateNotContainsChinese](#validatenotcontainschinese)
    - [ValidatePassword](#validatepassword)
    - [ValidatePlateNumber](#validateplatenumber)
    - [ValidateStartWithLetter](#validatestartwithletter)
    - [ValidateYearMonth](#validateyearmonth)
    - [ValidateYearQuarter](#validateyearquarter)
    - [ValidateZipCode](#validatezipcode)
  - [公共工具](#公共工具)
    - [LoginUtils](#loginutils)
  - [作业调度](#作业调度)
    - [BaseJob](#basejob)
    - [JobDescription](#jobdescription)
    - [HelloWorldJob](#helloworldjob)
  - [配置说明](#配置说明)
    - [application-data-framework.yml](#application-data-frameworkyml)
    - [DataProperties](#dataproperties)
  - [流程引擎](#流程引擎)
    - [application-process.yml](#application-processyml)
    - [LeaveProcessListener](#leaveprocesslistener)
- [一些规范和注释事项](#一些规范和注释事项)

# springboot-admin-starter 使用说明书

## 介绍

一个小型管理系统框架，提供了一整套前后端开箱即用的解决方案，包括用户管理、角色权限、数据字典、作业调度、流程引擎等功能。

本框架旨在简化企业级管理系统开发，提供了完整的权限体系、数据管理、作业调度和工作流引擎，支持快速构建各类管理后台系统。

根据README.md补充：
后端： ![Maven Version](https://img.shields.io/maven-central/v/io.github.jiangood/springboot-admin-starter)
```xml
<dependency>
  <groupId>io.github.jiangood</groupId>
  <artifactId>springboot-admin-starter</artifactId>
</dependency>
```

前端： ![NPM Version](https://img.shields.io/npm/v/@jiangood/springboot-admin-starter)
```
npm install @jiangood/springboot-admin-starter
```

### 项目依赖

后端依赖：
```xml
<dependency>
  <groupId>io.github.jiangood</groupId>
  <artifactId>springboot-admin-starter</artifactId>
</dependency>
```

前端依赖（模块名为`@jiangood/springboot-admin-starter`，antd使用最新版6.x）：
```bash
npm install @jiangood/springboot-admin-starter
```

### 核心功能

1. **用户权限管理**：完整的用户、角色、权限体系
2. **数据字典管理**：统一的数据字典维护和使用
3. **作业调度**：基于Quartz的定时任务管理
4. **流程引擎**：集成Flowable工作流引擎
5. **文件管理**：统一的文件上传、存储和管理
6. **系统配置**：灵活的系统参数配置
7. **操作日志**：完整的操作日志记录
8. **报表管理**：集成UReport报表引擎

## 前端

### 组件

#### ProTable

数据表格组件，提供分页、筛选、工具栏等功能。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `request` | 数据请求函数 | `(params) => Promise` | - |
| `columns` | 表格列定义 | `Array` | - |
| `rowKey` | 表格行的唯一标识 | `string` | `id` |
| `actionRef` | 表格操作引用 | `ref` | - |
| `toolBarRender` | 工具栏渲染函数 | `Function` | - |
| `rowSelection` | 行选择配置 | `object` | - |
| `defaultPageSize` | 默认每页条数 | `number` | 10 |
| `scrollY` | 表格垂直滚动区域高度 | `number` | - |
| `bordered` | 是否显示边框 | `boolean` | - |

#### ProModal

弹窗组件，用于展示对话框内容。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `title` | 弹窗标题 | `string` | - |
| `actionRef` | 弹窗操作引用（已弃用） | `ref` | - |
| `ref` | 弹窗引用 | `ref` | - |
| `onShow` | 弹窗显示回调 | `Function` | - |
| `footer` | 弹窗底部内容 | `ReactNode` | - |
| `width` | 弹窗宽度 | `number` | 800 |
| `children` | 弹窗内容 | `ReactNode` | - |

#### Ellipsis

文本省略组件，用于文本过长时以省略号显示，并可点击展开查看。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `length` | 文本截取长度 | `number` | 15 |
| `children` | 要处理的文本内容 | `ReactNode` | - |
| `pre` | 是否以预格式化文本显示 | `boolean` | - |

#### LinkButton

链接按钮组件，用于跳转到指定页面。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `path` | 跳转路径 | `string` | - |
| `label` | 页面标签 | `string` | - |
| `size` | 按钮大小 | `string` | `small` |
| `children` | 按钮内容 | `ReactNode` | - |

#### DownloadFileButton

下载文件按钮组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 文件下载地址 | `string` | - |
| `params` | 下载参数 | `object` | - |
| `children` | 按钮内容 | `ReactNode` | - |

#### PageLoading

页面加载组件。

无参数。

#### NamedIcon

命名图标组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `name` | 图标名称 | `string` | - |

#### Page

页面组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `padding` | 是否添加内边距 | `boolean` | `false` |
| `backgroundGray` | 是否设置背景为灰色 | `boolean` | `false` |
| `children` | 页面内容 | `ReactNode` | - |

### 字段组件

#### FieldRemoteSelect

远程搜索选择框组件，支持模糊搜索和远程加载选项。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取选项数据的API地址 | `string` | - |
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `placeholder` | 占位提示文本 | `string` | `请搜索选择` |

#### FieldRemoteSelectMultiple

远程搜索多选框组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取选项数据的API地址 | `string` | - |
| `value` | 当前值 | `Array` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `placeholder` | 占位提示文本 | `string` | `请搜索选择` |

#### FieldRemoteSelectMultipleInline

内联远程搜索多选框组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取选项数据的API地址 | `string` | - |
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `placeholder` | 占位提示文本 | `string` | `请搜索选择` |

#### FieldRemoteTree

远程树形选择组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取树数据的API地址 | `string` | - |
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |

#### FieldDictSelect

字典选择组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `dict` | 字典类型 | `string` | - |
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |

#### FieldEditor

富文本编辑器组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `height` | 编辑器高度 | `number` | - |

#### FieldRemoteTreeCascader

远程树形级联选择组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取树数据的API地址 | `string` | - |
| `value` | 当前值 | `Array` | - |
| `onChange` | 值改变回调函数 | `Function` | - |

#### FieldRemoteTreeSelect

远程树形选择器组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取树数据的API地址 | `string` | - |
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `treeDefaultExpandAll` | 是否默认展开所有节点 | `boolean` | `true` |

#### FieldRemoteTreeSelectMultiple

远程树形多选择器组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取树数据的API地址 | `string` | - |
| `value` | 当前值 | `Array` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `treeDefaultExpandAll` | 是否默认展开所有节点 | `boolean` | `true` |

#### FieldBoolean

布尔值选择组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `boolean` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `type` | 显示类型(select/radio/checkbox/switch) | `string` | `select` |

#### FieldDate

日期选择组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `type` | 日期类型 | `string` | `YYYY-MM-DD` |

#### FieldDateRange

日期范围选择组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `[string, string]` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `type` | 日期范围类型 | `string` | `YYYY-MM-DD` |

#### FieldTable

表格字段组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `Array` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `columns` | 表格列定义 | `Array` | - |
| `style` | 样式 | `object` | - |

#### FieldTableSelect

表格选择组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取表格数据的API地址 | `string` | - |
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `columns` | 表格列定义 | `Array` | - |
| `placeholder` | 占位提示文本 | `string` | `请搜索选择` |

#### FieldSysOrgTree

系统组织树选择组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `type` | 组织类型(dept/unit) | `string` | `dept` |

#### FieldSysOrgTreeSelect

系统组织树选择器组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `type` | 组织类型(dept/unit) | `string` | `dept` |

#### FieldPercent

百分比输入组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `number` | - |
| `onChange` | 值改变回调函数 | `Function` | - |

#### FieldUploadFile

文件上传组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `url` | 上传地址 | `string` | - |
| `listType` | 上传列表的内建样式 | `string` | `picture-card` |
| `maxCount` | 最大上传数量 | `number` | 1 |
| `accept` | 接受上传的文件类型 | `string` | - |
| `cropImage` | 是否裁剪图片 | `boolean` | - |

### 工具类

#### HttpUtils

HTTP请求工具类，用于发送各种HTTP请求。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `get` | 发送GET请求 | `(url, params, options)` | `Promise<any>` |
| `post` | 发送POST请求 | `(url, data, params, options)` | `Promise<any>` |
| `postForm` | 发送POST表单请求 | `(url, data, options)` | `Promise<any>` |
| `downloadFile` | 下载文件 | `(url, data, params, method, options)` | `Promise<void>` |

**说明**：`HttpUtils`请求返回的数据，自动处理了成功标志，`.then`接收到参数为实际响应后的`data`字段。

**示例**：
```javascript
import {HttpUtils} from '@jiangood/springboot-admin-starter';

// GET请求
HttpUtils.get('/api/users', {page: 1, size: 10})
  .then(data => console.log(data));

// POST请求
HttpUtils.post('/api/users', {name: '张三', age: 25})
  .then(data => console.log('用户创建成功', data));

// 文件下载
HttpUtils.downloadFile('/api/files/download', {id: 1});
```

#### MessageUtils

消息提示工具类，封装了Ant Design的消息提示功能。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `alert` | 弹出提示框 | `(content, config)` | - |
| `confirm` | 弹出确认框 | `(content, config)` | `Promise` |
| `prompt` | 弹出输入框 | `(message, initialValue, placeholder, config)` | `Promise` |
| `success` | 成功消息 | `(content, duration)` | - |
| `error` | 错误消息 | `(content, duration)` | - |
| `warning` | 警告消息 | `(content, duration)` | - |
| `info` | 信息消息 | `(content, duration)` | - |
| `loading` | 加载消息 | `(content, duration)` | - |
| `hideAll` | 隐藏所有消息 | - | - |
| `notify` | 通知提醒框 | `(message, description, type, placement, config)` | - |
| `notifySuccess` | 成功通知 | `(message, description, placement, config)` | - |
| `notifyError` | 错误通知 | `(message, description, placement, config)` | - |
| `notifyWarning` | 警告通知 | `(message, description, placement, config)` | - |

**示例**：
```javascript
import {MessageUtils} from '@jiangood/springboot-admin-starter';

// 成功消息
MessageUtils.success('操作成功');

// 确认框
MessageUtils.confirm('确认删除数据吗？').then(result => {
  if(result) {
    console.log('用户确认删除');
  }
});

// 错误消息
MessageUtils.error('操作失败');
```

#### DateUtils

日期工具类，提供日期相关的工具函数。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `year` | 获取年份 | `(date)` | `number` |
| `month` | 获取月份 | `(date)` | `string` |
| `date` | 获取日期 | `(date)` | `string` |
| `hour` | 获取小时 | `(date)` | `string` |
| `minute` | 获取分钟 | `(date)` | `string` |
| `second` | 获取秒 | `(date)` | `string` |
| `formatDate` | 格式化日期 | `(date)` | `string` |
| `formatTime` | 格式化时间 | `(date)` | `string` |
| `formatDateTime` | 格式化日期时间 | `(date)` | `string` |
| `formatDateCn` | 格式化中文日期 | `(date)` | `string` |
| `now` | 获取当前时间 | - | `string` |
| `today` | 获取当前日期 | - | `string` |
| `thisYear` | 获取当前年份 | - | `number` |
| `thisMonth` | 获取当前月份 | - | `string` |
| `friendlyTime` | 显示友好时间 | `(pastDate)` | `string` |
| `friendlyTotalTime` | 显示友好总时间 | `(time)` | `string` |
| `beginOfMonth` | 获取当月第一天 | - | `string` |

#### ArrUtils

数组工具类，提供数组相关的工具函数。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `contains` | 检查数组是否包含某个元素 | `(arr, item)` | `boolean` |
| `containsAny` | 检查数组是否包含至少一个指定元素 | `(arr, ...items)` | `boolean` |
| `add` | 在数组末尾添加元素 | `(arr, item)` | - |
| `addAt` | 在数组指定索引处添加元素 | `(arr, index, item)` | - |
| `addAll` | 将另一个数组的所有元素追加到目标数组 | `(arr, items)` | - |
| `removeAt` | 移除数组指定索引处的元素 | `(arr, index)` | - |
| `remove` | 移除数组中第一个匹配的元素 | `(arr, item)` | - |
| `clear` | 清空数组 | `(arr)` | - |
| `sub` | 截取数组的一个子集 | `(arr, fromIndex, toIndex)` | `T[]` |
| `swap` | 交换数组中两个元素的位置 | `(arr, item1, item2)` | - |
| `insert` | 在数组的指定索引处插入一个元素 | `(arr, index, item)` | - |
| `pushIfNotExist` | 如果元素不存在于数组中，则将其添加到数组末尾 | `(arr, item)` | - |
| `pushAll` | 将新数组中的所有元素添加到目标数组的末尾 | `(arr, newArr)` | - |
| `maxBy` | 获取对象数组中某一属性值最大的对象 | `(arr, key)` | `T` |
| `unique` | 对数组进行去重 | `(arr)` | `T[]` |

#### UrlUtils

URL工具类，提供URL相关的工具函数。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `getParams` | 获取URL参数 | `(url)` | `object` |
| `getPathname` | 获取不带参数的基础URL | `(url)` | `string` |
| `paramsToSearch` | 将参数对象转换为查询字符串 | `(params)` | `string` |
| `setParam` | 设置或删除URL中的参数 | `(url, key, value)` | `string` |
| `replaceParam` | 设置或替换URL中的参数 | `(url, key, value)` | `string` |
| `join` | 连接两个路径片段 | `(path1, path2)` | `string` |

#### StringUtils

字符串工具类，提供字符串相关的工具函数。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `removePrefix` | 移除字符串前缀 | `(str, ch)` | `string` |
| `removeSuffix` | 移除字符串后缀 | `(str, ch)` | `string` |
| `random` | 生成随机字符串 | `(length)` | `string` |
| `nullText` | 处理空值 | `(key)` | `string` |
| `contains` | 检查字符串是否包含子字符串 | `(str, subStr)` | `boolean` |
| `count` | 统计子字符串出现次数 | `(str, subStr)` | `number` |
| `capitalize` | 首字母大写 | `(str)` | `string` |
| `reverse` | 颠倒字符串顺序 | `(str)` | `string` |
| `subAfter` | 截取字符串后面部分 | `(source, str)` | `string` |
| `subAfterLast` | 截取最后一个子字符串后面部分 | `(source, str)` | `string` |
| `subBefore` | 截取字符串前面部分 | `(s, sub)` | `string` |
| `obfuscateString` | 混淆字符串 | `(str)` | `string` |
| `pad` | 补零或补指定字符 | `(input, totalLen, padChar)` | `string` |
| `getWidth` | 获取字符串显示宽度 | `(str)` | `number` |
| `cutByWidth` | 按显示宽度截取字符串 | `(str, maxWidth)` | `string` |
| `ellipsis` | 字符串省略处理 | `(str, len, suffix)` | `string` |
| `isStr` | 判断是否为字符串 | `(value)` | `boolean` |
| `toCamelCase` | 转为驼峰命名 | `(str, firstLower)` | `string` |
| `toUnderlineCase` | 转为下划线命名 | `(name)` | `string` |
| `equalsIgnoreCase` | 忽略大小写比较字符串 | `(a, b)` | `boolean` |
| `split` | 分割字符串 | `(str, sp)` | `string[]` |
| `join` | 连接字符串 | `(arr, sp)` | `string` |

#### EventBusUtils

事件总线工具类，用于组件间通信。

本项目中未找到该工具类的具体实现，可能是预留接口。

#### ColorsUtils

颜色工具类，提供颜色相关的工具函数。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `hexToRgb` | 十六进制转RGB | `(hex)` | `object` |
| `rgbToHex` | RGB转十六进制 | `(r, g, b)` | `string` |

#### DomUtils

DOM工具类，提供DOM操作相关的工具函数。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `hasClass` | 检查DOM元素是否有指定类名 | `(element, className)` | `boolean` |
| `addClass` | 给DOM元素添加类名 | `(element, className)` | - |
| `removeClass` | 给DOM元素移除类名 | `(element, className)` | - |

#### UuidUtils

UUID工具类，用于生成UUID。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `uuidV4` | 生成UUID v4 | - | `string` |

#### TreeUtils

树结构工具类，提供树相关的工具函数。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `treeToList` | 将树结构转换为列表 | `(tree)` | `Array` |
| `findKeysByLevel` | 根据层级查找节点ID | `(tree, level)` | `Array` |
| `buildTree` | 将扁平数组转换为树结构 | `(list, idKey, pidKey)` | `Array` |
| `walk` | 深度优先遍历树节点 | `(tree, callback)` | - |
| `findByKey` | 根据键值查找节点 | `(key, list, keyName)` | `T` |
| `findByKeyList` | 根据键值列表查找节点 | `(treeData, keyList)` | `Array` |
| `getSimpleList` | 获取树的所有节点 | `(treeNodeList)` | `Array` |
| `getKeyList` | 获取从根到目标节点的路径 | `(tree, value)` | `Array` |

#### StorageUtils

本地存储工具类，提供localStorage和sessionStorage操作。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `get` | 获取存储数据 | `(key, defaultValue)` | `T` |
| `set` | 设置存储数据 | `(key, value)` | - |

#### DeviceUtils

设备工具类，提供设备信息检测功能。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `isMobile` | 检测是否为移动端 | - | `boolean` |
| `isPC` | 检测是否为PC端 | - | `boolean` |

#### ObjectUtils

对象工具类，提供对象相关的工具函数。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `get` | 安全地获取嵌套对象属性值 | `(obj, path, defaultValue)` | `unknown` |
| `copyPropertyIfPresent` | 复制对象属性 | `(source, target)` | - |
| `copyProperty` | 复制非undefined属性 | `(source, target)` | - |

#### ValidateUtils

验证工具类，提供数据验证功能。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `isEmail` | 验证邮箱 | `(email)` | `boolean` |
| `isMobile` | 验证手机号 | `(mobile)` | `boolean` |
| `isIdCard` | 验证身份证 | `(idCard)` | `boolean` |

## 后端

### 数据规范相关

#### Spec

简洁、动态、支持关联字段查询的JPA Specification构建器。

**核心功能**：通过链式调用收集Specification，最终使用AND逻辑连接所有条件。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `of()` | 创建Spec实例 | - | `Spec<T>` |
| `eq(field, value)` | 等于条件 | `(String, Object)` | `Spec<T>` |
| `ne(field, value)` | 不等于条件 | `(String, Object)` | `Spec<T>` |
| `gt(field, value)` | 大于条件 | `(String, Comparable<T>)` | `Spec<T>` |
| `lt(field, value)` | 小于条件 | `(String, Comparable<T>)` | `Spec<T>` |
| `ge(field, value)` | 大于等于条件 | `(String, Comparable<T>)` | `Spec<T>` |
| `le(field, value)` | 小于等于条件 | `(String, Comparable<T>)` | `Spec<T>` |
| `like(field, value)` | 模糊匹配条件 | `(String, String)` | `Spec<T>` |
| `leftLike(field, value)` | 左模糊匹配条件 | `(String, String)` | `Spec<T>` |
| `rightLike(field, value)` | 右模糊匹配条件 | `(String, String)` | `Spec<T>` |
| `notLike(field, value)` | 非模糊匹配条件 | `(String, String)` | `Spec<T>` |
| `in(field, values)` | IN条件 | `(String, Collection<?>)` | `Spec<T>` |
| `between(field, value1, value2)` | BETWEEN条件 | `(String, Comparable<T>, Comparable<T>)` | `Spec<T>` |
| `isNotNull(field)` | 非空条件 | `(String)` | `Spec<T>` |
| `isNull(field)` | 空条件 | `(String)` | `Spec<T>` |
| `distinct(distinct)` | 去重条件 | `(boolean)` | `Spec<T>` |
| `or(specifications...)` | OR条件连接 | `(Specification<T>...)` | `Spec<T>` |
| `not(spec)` | 取反条件 | `(Specification<T>)` | `Spec<T>` |
| `orLike(value, fields...)` | OR模糊匹配 | `(String, String...)` | `Spec<T>` |
| `isMember(field, element)` | 集合成员条件 | `(String, Object)` | `Spec<T>` |
| `isNotMember(field, element)` | 非集合成员条件 | `(String, Object)` | `Spec<T>` |
| `groupBy(fields...)` | GROUP BY分组 | `(String...)` | `Spec<T>` |
| `having(spec)` | HAVING条件 | `(Specification<T>)` | `Spec<T>` |
| `addExample(t, ignores...)` | 示例查询 | `(T, String...)` | `Spec<T>` |
| `select(fields...)` | 选择字段 | `(String...)` | `Spec<T>` |
| `selectFnc(fn, field)` | 选择聚合函数 | `(AggregateFunction, String)` | `Spec<T>` |

**示例**：
```java
Spec<User> spec = Spec.of()
    .eq("status", 1)
    .like("name", "张")
    .between("createTime", startDate, endDate);
List<User> users = userRepository.findAll(spec);
```

### 树相关工具

#### TreeUtils

将列表转换为树结构的工具类，请使用TreeManager。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `buildTree(list)` | 构建树结构 | `(List<TreeOption>)` | `List<TreeOption>` |
| `buildTree(list, keyFn, pkeyFn, getChildren, setChildren)` | 通用构建树 | `(List<E>, Function, Function, Function, BiConsumer)` | `List<E>` |
| `treeToMap(tree)` | 将树转换为Map | `(List<TreeOption>)` | `Map<String, TreeOption>` |
| `treeToMap(tree, keyFn, getChildren)` | 通用树转Map | `(List<E>, Function, Function)` | `Map<String, E>` |
| `buildTreeByDict(list)` | 通过Dict构建树 | `(List<Dict>)` | `List<Dict>` |
| `cleanEmptyChildren(list, getChildren, setChildrenFn)` | 清理空子节点 | `(List<E>, Function, BiConsumer)` | - |
| `walk(list, getChildren, consumer)` | 遍历树节点(单参数) | `(List<E>, Function, Consumer)` | - |
| `walk(list, getChildren, consumer)` | 遍历树节点(双参数) | `(List<E>, Function, BiConsumer)` | - |
| `getLeafs(list, getChildren)` | 获取树的叶子节点 | `(List<E>, Function)` | `List<E>` |
| `treeToList(tree, getChildren)` | 树转列表 | `(List<E>, Function)` | `List<E>` |
| `getPids(nodeId, list, keyFn, pkeyFn)` | 获取节点的父节点列表 | `(String, List<E>, Function, Function)` | `List<String>` |

### 注解工具

#### RemarkUtils

获取字段、类、枚举等的@Remark注解值的工具类。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `getRemark(field)` | 获取字段的Remark注解值 | `(Field)` | `String` |
| `getRemark(cls)` | 获取类的Remark注解值 | `(Class<?>)` | `String` |
| `getRemark(enum)` | 获取枚举的Remark注解值 | `(Enum<?>)` | `String` |
| `getRemark(method)` | 获取方法的Remark注解值 | `(Method)` | `String` |

### Excel工具

#### ExcelUtils

Excel导入导出工具类，推荐构造单独的Bean用于导入导出。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `importExcel(cls, is)` | 导入Excel | `(Class<T>, InputStream)` | `List<T>` |
| `exportExcel(cls, list, os)` | 导出Excel | `(Class<T>, List<T>, OutputStream)` | - |

**使用注解**：使用`@Remark`注解来映射Excel列与实体字段。

**示例**：
```java
// 在实体类上使用@Remark注解
public class User {
    @Remark("用户名")
    private String username;

    @Remark("年龄")
    private Integer age;

    // getter/setter...
}

// 导入Excel
List<User> users = ExcelUtils.importExcel(User.class, inputStream);

// 导出Excel
ExcelUtils.exportExcel(User.class, users, outputStream);
```

### 数据库工具

#### JdbcUtils

Spring Boot环境下的原生SQL工具类，基于Spring的JdbcTemplate封装，专注于执行复杂的原生SQL查询和更新。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `update(sql, params...)` | 执行更新、插入或删除操作 | `(String, Object...)` | `int` |
| `batchUpdate(sql, batchArgs)` | 执行批量更新操作 | `(String, List<Object[]>)` | `int[]` |
| `execute(sql, params...)` | 执行任意SQL语句 | `(String, Object...)` | `int` |
| `executeQuietly(sql, params...)` | 执行SQL忽略异常 | `(String, Object...)` | `int` |
| `findOne(cls, sql, params...)` | 查询单条记录并映射到Java Bean | `(Class<T>, String, Object...)` | `T` |
| `findOneMap(sql, params...)` | 查询单条记录并返回Map | `(String, Object...)` | `Map<String, Object>` |
| `findAll(cls, sql, params...)` | 查询多条记录并映射到Java Bean列表 | `(Class<T>, String, Object...)` | `List<T>` |
| `findAllMap(sql, params...)` | 查询多条记录并返回Map列表 | `(String, Object...)` | `List<Map<String, Object>>` |
| `findScalar(sql, params...)` | 查询结果集的第一个值 | `(String, Object...)` | `Object` |
| `findLong(sql, params...)` | 查询结果集的第一个值并转换为Long | `(String, Object...)` | `Long` |
| `findInteger(sql, params...)` | 查询结果集的第一个值并转换为Integer | `(String, Object...)` | `Integer` |
| `findColumnList(elementType, sql, params...)` | 查询某一列并返回该列值的列表 | `(Class<T>, String, Object...)` | `List<T>` |
| `exists(sql, params...)` | 检查记录是否存在 | `(String, Object...)` | `boolean` |
| `findMapDict(sql, params...)` | 查询列表并组装成字典Map | `(String, Object...)` | `Map<String, Object>` |
| `findBeanMap(cls, sql, params...)` | 查询列表并映射到Map，以第一个字段作为键 | `(Class<V>, String, Object...)` | `Map<K, V>` |
| `findMapKeyed(sql, params...)` | 查询列表并映射到Map，以第一列作为键 | `(String, Object...)` | `Map<K, Map<String, Object>>` |
| `findAll(cls, pageable, sql, params...)` | 执行分页查询并返回Page对象 | `(Class<T>, Pageable, String, Object...)` | `Page<T>` |
| `insert(tableName, bean)` | 根据Bean数据动态构建SQL插入记录 | `(String, Object)` | `int` |
| `insert(tableName, map)` | 根据Map数据动态构建SQL插入记录 | `(String, Map<String, Object>)` | `int` |
| `updateById(table, bean)` | 根据Bean数据更新记录 | `(String, Object)` | `int` |
| `updateById(table, map)` | 根据Map数据更新记录 | `(String, Map<String, Object>)` | `int` |
| `update(table, bean, whereClause, whereParams...)` | 通用更新 | `(String, Object, String, Object...)` | `int` |
| `update(table, map, whereClause, whereParams...)` | 通用更新 | `(String, Map<String, Object>, String, Object...)` | `int` |
| `tableExists(tableName)` | 检查表是否存在 | `(String)` | `boolean` |
| `columnExists(tableName, columnName)` | 检查列是否存在 | `(String, String)` | `boolean` |
| `dropTable(tableName)` | 删除表 | `(String)` | `int` |
| `getTableNames()` | 获取所有表名 | - | `Set<String>` |
| `getColumnLabels(sql)` | 获取查询结果的列标签 | `(String)` | `String[]` |
| `generateCreateTableSql(cls, tableName)` | 根据类结构生成CREATE TABLE语句 | `(Class<?>, String)` | `String` |

**示例**：
```java
// 查询单个用户
User user = jdbcUtils.findOne(User.class, "SELECT * FROM user WHERE id = ?", 1);

// 查询用户列表
List<User> users = jdbcUtils.findAll(User.class, "SELECT * FROM user WHERE status = ?", 1);

// 分页查询
Pageable pageable = PageRequest.of(0, 10);
Page<User> userPage = jdbcUtils.findAll(User.class, pageable, "SELECT * FROM user", new Object[]{});

// 插入数据
User user = new User();
user.setName("张三");
user.setAge(25);
jdbcUtils.insert("t_user", user);

// 更新数据
jdbcUtils.updateById("t_user", user);
```

### ID生成相关注解

#### GeneratePrefixedSequence

使用前缀的序列ID生成器注解。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `prefix` | ID前缀 | `String` | - |

**示例**：
```java
@GeneratePrefixedSequence(prefix = "USER")
private String id;
```

#### GenerateUuidV7

UUID V7 ID生成器注解。

**示例**：
```java
@GenerateUuidV7
private String id;
```

### 数据类型转换器

#### BaseCodeEnumConverter

枚举值与数据库存储值之间的转换器，用于将实现了`CodeEnum`接口的枚举类型转换为数据库中的整数值。

**使用方式**：
```java
public class UserTypeConverter extends BaseCodeEnumConverter<UserType> {
}
```

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `convertToDatabaseColumn(attribute)` | 将枚举值转换为数据库存储值 | `(E)` | `Integer` |
| `convertToEntityAttribute(dbData)` | 将数据库存储值转换为枚举值 | `(Integer)` | `E` |

#### BaseConverter

基础转换器，用于将Java对象与数据库字符串字段之间进行转换，使用JSON格式进行序列化和反序列化。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `convertToDatabaseColumn(input)` | 将对象转换为JSON字符串 | `(T)` | `String` |
| `convertToEntityAttribute(dbData)` | 将JSON字符串转换为对象 | `(String)` | `T` |

#### BaseToListConverter

基础列表转换器。

#### ToBigDecimalListConverter

BigDecimal列表转换器，将BigDecimal列表与数据库字符串字段之间进行转换。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `convertToDatabaseColumn(list)` | 将BigDecimal列表转换为逗号分隔的字符串 | `(List<BigDecimal>)` | `String` |
| `convertToEntityAttribute(dbData)` | 将逗号分隔的字符串转换为BigDecimal列表 | `(String)` | `List<BigDecimal>` |

#### ToEntryListConverter

Entry列表转换器。

#### ToIntListConverter

整数列表转换器。

#### ToListBracketConverter

列表方括号格式转换器。

#### ToListComplexConverter

复杂列表类型转换器。

#### ToListConverter

通用列表转换器。

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `convertToDatabaseColumn(list)` | 将字符串列表转换为逗号分隔的字符串 | `(List<String>)` | `String` |
| `convertToEntityAttribute(dbData)` | 将逗号分隔的字符串转换为字符串列表 | `(String)` | `List<String>` |

#### ToLongListConverter

长整数列表转换器。

#### ToMapConverter

Map类型转换器。

#### ToMapStringObjectConverter

Map<String, Object>类型转换器。

#### ToPositionConverter

位置类型转换器。

#### ToQueryStringMapConverter

查询字符串Map转换器。

#### ToSetComplexConverter

复杂Set类型转换器。

#### ToSetConverter

通用Set类型转换器。

### 数据验证注解

#### ValidateCarDrivingLicence

验证驾驶证格式。

#### ValidateChineseName

验证中文姓名格式。

#### ValidateContainsChinese

验证包含中文字符。

#### ValidateCreditCode

验证统一社会信用代码格式。

#### ValidateDate

验证日期格式。

#### ValidateGeneral

通用验证。

#### ValidateHex

验证十六进制格式。

#### ValidateIdNum

验证身份证号码格式。

#### ValidateIp

验证IP地址格式。

#### ValidateIpv4

验证IPv4地址格式。

#### ValidateMobile

验证手机号码格式。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `message` | 验证失败提示消息 | `String` | `手机号码错误` |
| `groups` | 验证分组 | `Class<?>[]` | `{}` |
| `payload` | 载荷 | `Class<? extends Payload>[]` | `{}` |

**示例**：
```java
@ValidateMobile(message = "请输入正确的手机号码")
private String phone;
```

#### ValidateNotContainsChinese

验证不包含中文字符。

#### ValidatePassword

验证密码格式。

#### ValidatePlateNumber

验证车牌号格式。

#### ValidateStartWithLetter

验证以字母开头。

#### ValidateYearMonth

验证年月格式。

#### ValidateYearQuarter

验证年季度格式。

#### ValidateZipCode

验证邮政编码格式。

### 公共工具

#### LoginUtils

登录用户信息获取工具类。

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `getUserId()` | 获取当前登录用户ID | `String` |
| `getUser()` | 获取当前登录用户信息 | `LoginUser` |
| `getOrgPermissions()` | 获取当前登录用户机构权限 | `List<String>` |
| `getPermissions()` | 获取当前登录用户权限 | `List<String>` |
| `getRoles()` | 获取当前登录用户角色 | `List<String>` |
| `isAdmin()` | 判断当前用户是否为管理员 | `boolean` |

**示例**：
```java
String userId = LoginUtils.getUserId();
LoginUser user = LoginUtils.getUser();
List<String> permissions = LoginUtils.getPermissions();
boolean isAdmin = LoginUtils.isAdmin();
```

### 作业调度

#### BaseJob

基础作业类，所有自定义作业需继承此类。

**方法**：

`execute(JobDataMap data, Logger logger)` - 执行作业的核心方法，需要子类实现

**示例**：
```java
@JobDescription(label = "示例作业", params = {
    @FieldDescription(name = "param1", label = "参数1")
})
public class ExampleJob extends BaseJob {
    @Override
    public String execute(JobDataMap data, Logger logger) throws Exception {
        logger.info("执行示例作业");
        // 作业逻辑
        return "执行完成";
    }
}
```

#### JobDescription

作业描述注解。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `label` | 作业标签 | `String` | - |
| `params` | 参数定义 | `FieldDescription[]` | `{}` |
| `paramsProvider` | 参数提供者 | `JobParamFieldProvider` | `JobParamFieldProvider.class` |

#### HelloWorldJob

示例作业类，用于演示作业的实现方式。

### 配置说明

#### application-data-framework.yml

框架的核心配置项和菜单结构，业务项目需在`application-data.yml`中进行配置。

**数据配置部分**：
- `data.configs` - 系统配置元数据，包括邮件配置、网站配置、系统配置等分组
- `data.menus` - 系统菜单配置，定义了各种管理页面的菜单项及权限

**菜单配置示例**：
```yaml
data:
  menus:
    - name: 用户管理
      id: sysUser
      path: /system/user
      icon: UserOutlined
      perms:
        - name: 列表
          perm: sysUser:view
        - name: 保存
          perm: sysUser:save
        - name: 删除
          perm: sysUser:delete
```

#### DataProperties

数据配置属性类，用于加载application.yml中的data配置。

| 属性 | 说明 | 类型 |
|------|------|------|
| `menus` | 菜单定义列表 | `List<MenuDefinition>` |
| `configs` | 配置组定义列表 | `List<ConfigGroupDefinition>` |

### 流程引擎

#### application-process.yml

流程定义列表配置。

**示例**：
```yaml
process:
  list:
    - key: "leave_request"
      name: "请假流程"
      listener: example.flowable.modules.io.github.jiangood.as.LeaveProcessListener
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

#### LeaveProcessListener

流程监听器示例类，用于处理流程事件。

## 一些规范和注释事项

1. 表单中的有限使用框架中的表单组件，例如下拉选择，使用`FieldRemoteSelect`，其中`url`属性为`xxx/options`（后端的url）。

2. 注意事项：HttpUtils请求返回的数据，自动处理了成功标志，.then接收到参数为实际响应后的data字段。

3. 权限管理：系统使用基于角色的权限控制（RBAC），后端通过`@PreAuthorize`注解进行权限验证，前端通过`HasPerm`组件或`LoginUtils`进行权限判断。

4. 数据库设计：实体类使用JPA注解进行映射，ID字段推荐使用框架提供的ID生成注解（如`@GeneratePrefixedSequence`或`@GenerateUuidV7`）。

5. 参数验证：后端使用JSR-303注解（如`@ValidateMobile`）进行参数验证，前端使用Ant Design Form的验证规则。

6. 业务开发流程：
   - 定义实体类（使用JPA注解）
   - 创建DAO层接口
   - 创建Service层业务逻辑
   - 创建Controller层提供API接口
   - 前端使用ProTable等组件构建页面

7. 菜单配置：业务项目需在`application-data.yml`中配置菜单结构，框架会自动加载并生成菜单项。

8. 系统配置：可通过`application-data.yml`中的`data.configs`配置系统参数，用户可在系统设置中修改这些参数。
