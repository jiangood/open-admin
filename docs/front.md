# 前端模块

## 组件列表

| 名称 | 类型 | 说明 |
|---|---|---|
| DownloadFileButton | 组件 | 文件下载按钮 |
| Ellipsis | 组件 | 文本省略显示组件 |
| LinkButton | 组件 | 链接按钮 |
| NamedIcon | 组件 | 根据名称渲染Ant Design图标 |
| PageLoading | 组件 | 页面加载动画 |
| FieldBoolean | 组件 | 布尔值输入组件 (Checkbox, Radio, Select, Switch) |
| FieldDate | 组件 | 日期选择组件 |
| FieldDateRange | 组件 | 日期范围选择组件 |
| FieldDictSelect | 组件 | 字典选择组件 |
| FieldEditor | 组件 | 富文本编辑器 |
| FieldPercent | 组件 | 百分比输入组件 |
| FieldRemoteSelect | 组件 | 远程数据单选选择器 |
| FieldRemoteSelectMultiple | 组件 | 远程数据多选选择器 |
| FieldRemoteSelectMultipleInline | 组件 | 远程数据多选选择器 (行内显示) |
| FieldRemoteTree | 组件 | 远程数据树形选择组件 |
| FieldRemoteTreeCascader | 组件 | 远程数据级联选择器 |
| FieldRemoteTreeSelect | 组件 | 远程数据树形选择器 |
| FieldRemoteTreeSelectMultiple | 组件 | 远程数据树形多选选择器 |
| FieldSysOrgTree | 组件 | 组织机构树选择组件 |
| FieldSysOrgTreeSelect | 组件 | 组织机构树选择器 |
| FieldTable | 组件 | 可编辑表格组件 |
| FieldTableSelect | 组件 | 表格选择器 |
| FieldUploadFile | 组件 | 文件上传组件 |
| ViewBoolean | 组件 | 布尔值显示组件 |
| ViewPassword | 组件 | 密码显示组件 |

## 工具类列表

| 名称 | 类型 | 说明 |
|---|---|---|
| ArrUtils.contains | 工具函数 | 检查数组是否包含某个元素 |
| ArrUtils.containsAny | 工具函数 | 检查数组是否包含至少一个指定的元素 |
| ArrUtils.add | 工具函数 | 在数组末尾添加一个元素 |
| ArrUtils.addAt | 工具函数 | 在数组的指定索引处添加一个元素 |
| ArrUtils.addAll | 工具函数 | 将另一个数组的所有元素追加到目标数组的尾部 |
| ArrUtils.removeAt | 工具函数 | 移除数组指定索引处的元素 |
| ArrUtils.remove | 工具函数 | 移除数组中第一个匹配的元素 |
| ArrUtils.clear | 工具函数 | 清空数组 |
| ArrUtils.sub | 工具函数 | 截取数组的一个子集 |
| ArrUtils.swap | 工具函数 | 交换数组中两个元素的位置 |
| ArrUtils.insert | 工具函数 | 在数组的指定索引处插入一个元素 |
| ArrUtils.pushIfNotExist | 工具函数 | 如果元素不存在于数组中，则将其添加到数组末尾 |
| ArrUtils.pushAll | 工具函数 | 将新数组中的所有元素添加到目标数组的末尾 |
| ArrUtils.maxBy | 工具函数 | 获取对象数组中某一属性值最大的对象 |
| ArrUtils.unique | 工具函数 | 对数组进行去重 |
| ColorsUtils.rgbToHex | 工具函数 | 将 RGB 颜色对象转换为十六进制字符串 |
| ColorsUtils.rgbToString | 工具函数 | 将 RGB 颜色对象转换为 rgb() 或 rgba() 字符串 |
| ColorsUtils.hexToRgb | 工具函数 | 将十六进制颜色字符串转换为 RGB 颜色对象 |
| ColorsUtils.hsvToRgb | 工具函数 | 将 HSV 颜色对象转换为 RGB 颜色对象 |
| ColorsUtils.rgbToHsv | 工具函数 | 将 RGB 颜色对象转换为 HSV 颜色对象 |
| ColorsUtils.textToRgb | 工具函数 | 将颜色文本 (hex 或 rgb/rgba) 转换为 RGB 颜色对象 |
| ColorsUtils.lighten | 工具函数 | 调亮或调暗颜色 |
| ColorsUtils.luminosity | 工具函数 | 计算颜色的亮度 (Luminosity) |
| ColorsUtils.brightness | 工具函数 | 计算颜色的感知亮度 (Brightness) |
| ColorsUtils.blend | 工具函数 | 将前景颜色混合到背景颜色上 |
| ColorsUtils.changeAlpha | 工具函数 | 改变颜色字符串的 alpha 透明度 |
| DateUtils.year | 工具函数 | 获取年份 |
| DateUtils.month | 工具函数 | 获取月份，自动补0 |
| DateUtils.date | 工具函数 | 获取日期 |
| DateUtils.hour | 工具函数 | 获取小时，24进制 |
| DateUtils.minute | 工具函数 | 获取分钟 |
| DateUtils.second | 工具函数 | 获取秒 |
| DateUtils.formatDate | 工具函数 | 格式化日期 |
| DateUtils.formatTime | 工具函数 | 格式化时间 |
| DateUtils.formatDateTime | 工具函数 | 格式化日期时间 |
| DateUtils.formatDateCn | 工具函数 | 格式化中文日期 |
| DateUtils.now | 工具函数 | 获取当前日期时间字符串 |
| DateUtils.today | 工具函数 | 获取当前日期字符串 |
| DateUtils.thisYear | 工具函数 | 获取当前年份 |
| DateUtils.thisMonth | 工具函数 | 获取当前月份 |
| DateUtils.friendlyTime | 工具函数 | 显示友好时间 |
| DateUtils.friendlyTotalTime | 工具函数 | 总共耗时 |
| DateUtils.beginOfMonth | 工具函数 | 获取本月第一天日期 |
| DeviceUtils.isMobileDevice | 工具函数 | 判断当前设备是否为移动设备 |
| DeviceUtils.getWebsocketBaseUrl | 工具函数 | 构造 WebSocket 的基础 URL |
| DomUtils.offset | 工具函数 | 获取元素相对于视口的偏移量 |
| DomUtils.height | 工具函数 | 获取元素的外部高度 |
| DomUtils.width | 工具函数 | 获取元素的外部宽度 |
| EventBusUtils.on | 工具函数 | 注册事件监听器 |
| EventBusUtils.once | 工具函数 | 注册只触发一次的事件监听器 |
| EventBusUtils.emit | 工具函数 | 触发事件 |
| EventBusUtils.off | 工具函数 | 移除事件监听器 |
| MessageUtils.alert | 工具函数 | 弹出 Alert 提示框 |
| MessageUtils.confirm | 工具函数 | 弹出 Confirm 确认框 |
| MessageUtils.prompt | 工具函数 | 弹出 Prompt 输入框对话框 |
| MessageUtils.success | 工具函数 | 成功消息 |
| MessageUtils.error | 工具函数 | 错误消息 |
| MessageUtils.warning | 工具函数 | 警告消息 |
| MessageUtils.info | 工具函数 | 通用消息 |
| MessageUtils.loading | 工具函数 | 弹出 Loading 提示 |
| MessageUtils.hideAll | 工具函数 | 立即关闭所有 message 提示 |
| MessageUtils.notify | 工具函数 | 弹出右上角通知提醒框 |
| MessageUtils.notifySuccess | 工具函数 | 弹出成功通知 |
| MessageUtils.notifyError | 工具函数 | 弹出失败通知 |
| MessageUtils.notifyWarning | 工具函数 | 弹出警告通知 |
| ObjectUtils.get | 工具函数 | 安全地获取深度嵌套的对象属性的值 |
| ObjectUtils.copyPropertyIfPresent | 工具函数 | 复制对象属性，仅复制源对象中存在且目标对象中也有对应属性的那些值 |
| ObjectUtils.copyProperty | 工具函数 | 复制对象属性，将源对象中非 undefined 的属性值复制到目标对象对应的属性上 |
| StorageUtils.get | 工具函数 | 从 localStorage 获取数据 |
| StorageUtils.set | 工具函数 | 将数据存储到 localStorage |
| StringUtils.removePrefix | 工具函数 | 移除字符串前缀 |
| StringUtils.removeSuffix | 工具函数 | 移除字符串后缀 |
| StringUtils.random | 工具函数 | 生成指定长度的随机字符串 |
| StringUtils.nullText | 工具函数 | 处理空值，返回 "key未定义" |
| StringUtils.contains | 工具函数 | 检查字符串是否包含子字符串 |
| StringUtils.count | 工具函数 | 统计子字符串在原始字符串中出现的次数 |
| StringUtils.capitalize | 工具函数 | 将字符串的首字母转换为大写 |
| StringUtils.reverse | 工具函数 | 颠倒字符串的顺序 |
| StringUtils.subAfter | 工具函数 | 截取字符串，返回子字符串后面部分 |
| StringUtils.subAfterLast | 工具函数 | 截取字符串，返回最后一个子字符串后面的部分 |
| StringUtils.subBefore | 工具函数 | 截取字符串，返回子字符串前面的部分 |
| StringUtils.obfuscateString | 工具函数 | 混淆字符串 |
| StringUtils.pad | 工具函数 | 补零或补指定字符 |
| StringUtils.encrypt | 工具函数 | 简单加密：将每个字符的 ASCII 码加 1，并转换为四位十六进制表示 |
| StringUtils.decrypt | 工具函数 | 简单解密：还原加密字符串 |
| StringUtils.getWidth | 工具函数 | 获取字符串的显示宽度 |
| StringUtils.cutByWidth | 工具函数 | 按显示宽度截取字符串 |
| StringUtils.ellipsis | 工具函数 | 字符串省略处理（按显示宽度计算） |
| StringUtils.isStr | 工具函数 | 判断值是否为字符串类型 |
| StringUtils.toCamelCase | 工具函数 | 将下划线或连字符分隔的字符串转为驼峰命名 |
| StringUtils.toUnderlineCase | 工具函数 | 将驼峰命名字符串转为下划线命名 |
| StringUtils.equalsIgnoreCase | 工具函数 | 比较两个字符串是否相等，忽略大小写 |
| StringUtils.split | 工具函数 | 分割字符串 |
| StringUtils.join | 工具函数 | 连接字符串 |
| TreeUtils.treeToList | 工具函数 | 将树结构转换为列表结构 (扁平化) |
| TreeUtils.findKeysByLevel | 工具函数 | 根据层级查找所有节点的 ID 列表 |
| TreeUtils.buildTree | 工具函数 | 将扁平数组转换为树结构 |
| TreeUtils.walk | 工具函数 | 深度优先遍历树节点 |
| TreeUtils.findByKey | 工具函数 | 根据键值深度查找单个节点 |
| TreeUtils.findByKeyList | 工具函数 | 根据键值列表查找所有匹配的节点列表 |
| TreeUtils.getSimpleList | 工具函数 | 获得给定根节点列表下的所有节点 (扁平化列表) |
| TreeUtils.getKeyList | 工具函数 | 向上追溯，获取从根节点到指定值节点的完整 Key 路径 |
| UrlUtils.getParams | 工具函数 | 获取 URL 的参数对象 |
| UrlUtils.getPathname | 工具函数 | 获取不带参数的基础 URL (pathname) |
| UrlUtils.paramsToSearch | 工具函数 | 将参数对象转换为 URL 中的查询字符串 |
| UrlUtils.setParam | 工具函数 | 设置或删除 URL 中的一个参数，并返回新的 URL |
| UrlUtils.replaceParam | 工具函数 | 设置或替换 URL 中的一个参数 (deprecated) |
| UrlUtils.join | 工具函数 | 连接两个路径片段 |
| UuidUtils.uuidV4 | 工具函数 | 生成一个符合 v4 标准的 UUID |
| ValidateUtils.isEmail | 工具函数 | 检查给定的字符串是否为有效的电子邮件地址 |