# 前端工具类

## HttpUtils

HTTP 请求工具类。返回的数据自动处理了成功标志，`.then` 接收到的为 `data` 字段。

```js
// GET
HttpUtils.get('/api/users', {page: 1, size: 10}).then(data => ...);

// POST
HttpUtils.post('/api/users', {name: '张三'}).then(data => ...);

// 文件下载
HttpUtils.downloadFile('/api/files/download', {id: 1});
```

| 方法 | 说明 |
|------|------|
| `get(url, params, options)` | GET 请求 |
| `post(url, data, params, options)` | POST 请求 |
| `postForm(url, data, options)` | POST 表单 |
| `downloadFile(url, data, params, method, options)` | 文件下载 |

## MessageUtils

消息提示（基于 Ant Design message 封装）。

```js
MessageUtils.success('操作成功');
MessageUtils.error('操作失败');
MessageUtils.confirm('确认删除？').then(ok => { if (ok) ... });
```

## DateUtils

```js
DateUtils.formatDate(date);    // 2026-05-09
DateUtils.formatTime(date);    // 14:30:00
DateUtils.formatDateTime(date);
DateUtils.friendlyTime(pastDate);   // 3 分钟前
DateUtils.beginOfMonth();           // 当月第一天
```

## TreeUtils

```js
TreeUtils.buildTree(list, 'id', 'parentId');   // 扁平数组转树
TreeUtils.treeToList(tree);                      // 树转扁平数组
TreeUtils.walk(tree, node => console.log(node)); // 深度优先遍历
```

## 其他工具

| 工具类 | 主要方法 |
|--------|----------|
| `ArrUtils` | contains, unique, remove, maxBy |
| `StringUtils` | random, ellipsis, toCamelCase, split, join |
| `UrlUtils` | getParams, setParam, join |
| `StorageUtils` | get, set (localStorage) |
| `DeviceUtils` | isMobile, isPC |
| `ObjectUtils` | get (安全访问嵌套属性) |
| `ValidateUtils` | isEmail, isMobile, isIdCard |
| `ColorsUtils` | hexToRgb, rgbToHex |
| `UuidUtils` | uuidV4 |
| `DomUtils` | hasClass, addClass, removeClass |
| `SysUtils` | contextPath, getSiteInfo, setSiteInfo, getLoginInfo, setLoginInfo, getHeaders |

## SysUtils

系统工具类，提供站点信息、登录信息、请求头等便捷方法。

### contextPath

拼接 servlet context-path 前缀。用于 `<a>`、`<img>`、CSS `url()` 等不经过 axios 的硬编码 URL。

```js
// 自动拼接 context-path 前缀
SysUtils.contextPath('/admin/auth/login');   // → '/your-context/admin/auth/login'
SysUtils.contextPath('/admin/auth/login'); // → '/your-context/admin/auth/login'
```

> **注意**：通过 `HttpUtils` 发起的请求会自动带上 context-path，无需手动调用此方法。
