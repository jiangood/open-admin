# 前端工具类

本文档介绍了 open-admin 前端框架中提供的工具类。

## 工具类列表

### HttpUtils

HTTP请求工具类，用于发送各种HTTP请求。

**方法**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `get` | 发送GET请求 | `(url, params, options)` | `Promise<any>` |
| `post` | 发送POST请求 | `(url, data, params, options)` | `Promise<any>` |
| `postForm` | 发送POST表单请求 | `(url, data, options)` | `Promise<any>` |
| `downloadFile` | 下载文件 | `(url, data, params, method, options)` | `Promise<void>` |

**说明**：`HttpUtils`请求返回的数据，自动处理了成功标志，`.then`接收到参数为实际响应后的`data`字段。

**示例**：

```javascript
import {HttpUtils} from '@jiangood/open-admin';

// GET请求
HttpUtils.get('/api/users', {page: 1, size: 10})
  .then(data => console.log(data));

// POST请求
HttpUtils.post('/api/users', {name: '张三', age: 25})
  .then(data => console.log('用户创建成功', data));

// 文件下载
HttpUtils.downloadFile('/api/files/download', {id: 1});
```

### MessageUtils

消息提示工具类，封装了Ant Design的消息提示功能。

**方法**：

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

**示例**：

```javascript
import {MessageUtils} from '@jiangood/open-admin';

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

### DateUtils

日期工具类，提供日期相关的工具函数。

**方法**：

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

### ArrUtils

数组工具类，提供数组相关的工具函数。

**方法**：

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

### UrlUtils

URL工具类，提供URL相关的工具函数。

**方法**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `getParams` | 获取URL参数 | `(url)` | `object` |
| `getPathname` | 获取不带参数的基础URL | `(url)` | `string` |
| `paramsToSearch` | 将参数对象转换为查询字符串 | `(params)` | `string` |
| `setParam` | 设置或删除URL中的参数 | `(url, key, value)` | `string` |
| `replaceParam` | 设置或替换URL中的参数 | `(url, key, value)` | `string` |
| `join` | 连接两个路径片段 | `(path1, path2)` | `string` |

### StringUtils

字符串工具类，提供字符串相关的工具函数。

**方法**：

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

### ColorsUtils

颜色工具类，提供颜色相关的工具函数。

**方法**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `hexToRgb` | 十六进制转RGB | `(hex)` | `object` |
| `rgbToHex` | RGB转十六进制 | `(r, g, b)` | `string` |

### DomUtils

DOM工具类，提供DOM操作相关的工具函数。

**方法**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `hasClass` | 检查DOM元素是否有指定类名 | `(element, className)` | `boolean` |
| `addClass` | 给DOM元素添加类名 | `(element, className)` | - |
| `removeClass` | 给DOM元素移除类名 | `(element, className)` | - |

### UuidUtils

UUID工具类，用于生成UUID。

**方法**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `uuidV4` | 生成UUID v4 | - | `string` |

### TreeUtils

树结构工具类，提供树相关的工具函数。

**方法**：

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

### StorageUtils

本地存储工具类，提供localStorage和sessionStorage操作。

**方法**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `get` | 获取存储数据 | `(key, defaultValue)` | `T` |
| `set` | 设置存储数据 | `(key, value)` | - |

### DeviceUtils

设备工具类，提供设备信息检测功能。

**方法**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `isMobile` | 检测是否为移动端 | - | `boolean` |
| `isPC` | 检测是否为PC端 | - | `boolean` |

### ObjectUtils

对象工具类，提供对象相关的工具函数。

**方法**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `get` | 安全地获取嵌套对象属性值 | `(obj, path, defaultValue)` | `unknown` |
| `copyPropertyIfPresent` | 复制对象属性 | `(source, target)` | - |
| `copyProperty` | 复制非undefined属性 | `(source, target)` | - |

### ValidateUtils

验证工具类，提供数据验证功能。

**方法**：

| 方法 | 说明 | 参数 | 返回值 |
|------|------|------|--------|
| `isEmail` | 验证邮箱 | `(email)` | `boolean` |
| `isMobile` | 验证手机号 | `(mobile)` | `boolean` |
| `isIdCard` | 验证身份证 | `(idCard)` | `boolean` |