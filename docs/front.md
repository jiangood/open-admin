# 前端模块

## 组件

### `DownloadFileButton`

-   **名称:** `DownloadFileButton`
-   **说明:** 用于触发文件下载的按钮组件。点击按钮后会显示一个加载中的提示框，并在文件下载完成后关闭。
-   **参数:**
    *   `url`: (string) 文件下载的请求地址。
    *   `params`: (object) 请求参数。
    *   `children`: (ReactNode) 按钮的子元素，通常是按钮文本。
    *   `...rest`: (object) Ant Design `Button` 组件的其他属性。

### `Ellipsis`

-   **名称:** `Ellipsis`
-   **说明:** 当文本内容过长时，将其截断并显示省略号。点击省略的文本可以查看完整的文本内容。
-   **参数:**
    *   `length`: (number) 可选，文本截断的长度，默认为 15。
    *   `children`: (string) 需要显示或截断的文本内容。
    *   `pre`: (boolean) 可选，如果为 true，则在显示完整文本时使用 `<pre>` 标签包裹。

### `LinkButton`

-   **名称:** `LinkButton`
-   **说明:** 一个链接按钮组件，点击后通过 `PageUtils.open` 方法打开指定路径。
-   **参数:**
    *   `path`: (string) 链接的目标路径。
    *   `label`: (string) 链接的标签。
    *   `children`: (ReactNode) 按钮的子元素，通常是按钮文本。
    *   `size`: (string) 可选，按钮大小，默认为 `'small'`。
    *   `...rest`: (object) Ant Design `Button` 组件的其他属性。

### `PageLoading`

-   **名称:** `PageLoading`
-   **说明:** 页面加载时显示的动画组件，包含一个大的 Spin 动画和可选的提示信息。
-   **参数:**
    *   `message`: (string) 可选，加载时显示的提示信息，默认为 '页面加载中...'。

### `NamedIcon`

-   **名称:** `NamedIcon`
-   **说明:** 根据传入的图标名称动态渲染 Ant Design 图标。
-   **参数:**
    *   `name`: (string) Ant Design 图标的名称字符串 (例如 "HomeOutlined", "UserOutlined")。
    *   `...rest`: (object) Ant Design Icon 组件的其他属性。

## 工具类

### `DateUtils`

-   **名称:** `DateUtils`
-   **说明:** 日期时间处理工具类，提供格式化、获取日期时间组成部分、计算友好时间等功能。
-   **常用方法:**
    *   `formatDate(d: Date)`: 格式化日期为 "YYYY-MM-DD"。
    *   `formatTime(d: Date)`: 格式化时间为 "HH:mm:ss"。
    *   `formatDateTime(d: Date)`: 格式化日期时间为 "YYYY-MM-DD HH:mm:ss"。
    *   `friendlyTime(pastDate: Date | string | number)`: 显示友好时间，如 "2小时前", "1周前"。
    *   `now()`: 获取当前日期时间字符串。

### `StringUtils`

-   **名称:** `StringUtils`
-   **说明:** 字符串处理工具类，提供移除前缀/后缀、生成随机字符串、判空、包含判断、统计、大小写转换、反转、截取、填充、简单加密解密、计算显示宽度、省略、类型判断、驼峰/下划线转换、忽略大小写比较、分割和连接等功能。
-   **常用方法:**
    *   `removePrefix(str: string | null | undefined, ch: string)`: 移除字符串前缀。
    *   `random(length: number)`: 生成指定长度的随机字符串。
    *   `ellipsis(str: string | null | undefined, len: number, suffix: string = '...')`: 字符串省略处理。
    *   `toCamelCase(str: string, firstLower: boolean = true)`: 将下划线或连字符分隔的字符串转为驼峰命名。
    *   `toUnderlineCase(name: string | null | undefined)`: 将驼峰命名字符串转为下划线命名。

### `TreeUtils`

-   **名称:** `TreeUtils`
-   **说明:** 树结构操作工具类，用于将扁平数据和树形结构互相转换，提供遍历、查找、层级处理等功能。
-   **常用方法:**
    *   `treeToList<T extends TreeNode>(tree: T[])`: 将树结构扁平化为列表，节点会添加 `level` 字段。
    *   `buildTree<T extends TreeNode>(list: T[], idKey: keyof T | 'id' = 'id', pidKey: keyof T | 'pid' = 'pid')`: 将扁平数组构建为树结构。
    *   `walk<T extends TreeNode>(tree: T[], callback: (node: T) => void)`: 深度优先遍历树节点。
    *   `findByKey<T extends TreeNode>(key: string | number, list: T[], keyName: keyof T | 'id' = 'id')`: 根据键值深度查找单个节点。

### `MessageUtils`

-   **名称:** `MessageUtils`
-   **说明:** 消息工具类，封装了 Ant Design 的 `Modal` (Alert/Confirm/Prompt)、`message` (全局通知/Loading) 和 `notification` (通知提醒框) 静态方法，用于在前端页面进行各种消息提示和交互。
-   **常用方法:**
    *   `alert(content: React.ReactNode, config?: Omit<ModalFuncProps, 'content' | 'icon' | 'onOk' | 'onCancel'>)`: 弹出 Alert 提示框。
    *   `confirm(content: React.ReactNode, config?: Omit<ModalFuncProps, 'content' | 'icon' | 'onOk' | 'onCancel'>)`: 弹出 Confirm 确认框，返回 Promise。
    *   `success(content: React.ReactNode, duration: number = 3)`: 弹出成功消息。
    *   `error(content: React.ReactNode, duration: number = 3)`: 弹出错误消息。
    *   `loading(content: React.ReactNode = '正在加载...', duration?: number)`: 弹出 Loading 提示。
    *   `notifySuccess(message: React.ReactNode, description: React.ReactNode)`: 弹出成功通知。
