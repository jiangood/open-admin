# RemoteSelect 系列组件重构设计

## 目标

消除 `web/src/framework/fields/` 下 RemoteSelect/Tree 系列组件中重复的数据加载逻辑，提取共享基类，保持完全向后兼容。

## 涉及组件

**Select 系列（3 个）：**
- `FieldRemoteSelect` — 远程搜索单选
- `FieldRemoteSelectMultiple` — 远程搜索多选（数组值）
- `FieldRemoteSelectMultipleInline` — 远程搜索多选（逗号字符串值）

**Tree 系列（4 个）：**
- `FieldRemoteTreeSelect` — 远程树下拉单选
- `FieldRemoteTreeSelectMultiple` — 远程树下拉多选
- `FieldRemoteTree` — 扁平树多选
- `FieldRemoteTreeCascader` — 远程树级联选择

**包装组件（2 个，不修改）：**
- `FieldSysOrgTree` — 包装 FieldRemoteTree，只改 url
- `FieldSysOrgTreeSelect` — 包装 FieldRemoteTreeSelect，只改 url

## 方案：共享基类

### 新增文件

```
web/src/framework/fields/BaseRemoteSelect.js
```

基类职责：
- 状态管理：`loading`、`data`
- 数据加载：`HttpUtils.get(url, params)` + 竞态处理（fetchIdRef）
- 防抖搜索：`debounce(loadData, 800ms)`
- 错误处理：`console.error` + `message.error`
- 生命周期：`componentDidMount` 初始加载，`componentWillUnmount` 取消防抖
- Helper 方法：`getShowSearch()`、`getNotFoundContent()`

### 子类差异

| 组件 | 渲染组件 | 搜索 | 值格式 | 差异点 |
|------|---------|------|--------|-------|
| FieldRemoteSelect | Select | 是 | 单值 | 标准 Select |
| FieldRemoteSelectMultiple | Select(mode=multiple) | 是 | 数组 | mode='multiple' |
| FieldRemoteSelectMultipleInline | Select(mode=multiple) | 是 | 逗号字符串 | 值变换 split/join |
| FieldRemoteTreeSelect | TreeSelect | 否（filterTreeNode） | 单值 | 树形数据，无搜索参数 |
| FieldRemoteTreeSelectMultiple | TreeSelect(multiple) | 否（filterTreeNode） | 数组 | multiple=true |
| FieldRemoteTree | Tree(checkable) | 否 | 数组 | 扁平树，checkable |
| FieldRemoteTreeCascader | Cascader | 否 | 单值 | 级联选择 |

### 基类接口

```jsx
class BaseRemoteSelect extends React.Component {
  // 子类可覆写
  getUrl()                    — 默认返回 this.props.url
  getLoadParams(searchText)   — 拼接请求参数
  onLoadSuccess(data)         — 数据加载后回调
  onLoadError(error)          — 错误回调
  shouldLoadOnMount()         — 是否首次挂载加载，默认 true

  // 子类可直接使用
  loadData(searchText)        — 触发数据加载（自动防抖）
  this.state                  — { loading, data }
  getShowSearch()             — { filterOption: false, onSearch: handleSearch }
  getNotFoundContent()        — Spin 或 '数据为空'
}
```

### 不修改的文件

- `FieldSysOrgTree/index.jsx` — 仅包装 FieldRemoteTree，零改动
- `FieldSysOrgTreeSelect/index.jsx` — 同上
- 所有类型定义文件（`.d.ts`）— 输出名不变，类型兼容

## 向后兼容

- 所有类名和导出名不变：`import { FieldRemoteSelect } from "../../fields"`
- 所有 props 不变且行为兼容
- FieldRemoteSelect 获得防抖、竞态处理、错误提示（行为提升，不破坏兼容）
- 类型定义文件无需修改

## 测试要点

- 每个组件渲染快照不变
- 数据加载发起正确请求
- 搜索防抖生效
- 竞态条件下只应用最后一次
- 错误时显示 message.error
- MultipleInline 的逗号字符串 ↔ 数组转换正确
