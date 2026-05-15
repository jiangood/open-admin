# 前端组件库

## ProTable

数据表格组件，提供分页、筛选、工具栏等功能。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `request` | 数据请求函数 | `(params) => Promise` | — |
| `columns` | 列定义 | `Array` | — |
| `rowKey` | 行唯一标识 | `string` | `id` |
| `actionRef` | 表格操作引用 | `ref` | — |
| `toolBarRender` | 工具栏渲染 | `Function` | — |
| `rowSelection` | 行选择配置 | `object` | — |
| `defaultPageSize` | 每页条数 | `number` | 10 |
| `scrollY` | 垂直滚动高度 | `number` | — |
| `bordered` | 显示边框 | `boolean` | — |
| `toolbarOptions` | 显示工具栏 | `boolean` | `true` |
| `showToolbarSearch` | 显示搜索 | `boolean` | — |
| `formRef` | 表单引用 | `ref` | — |

```jsx
<ProTable
  actionRef={this.tableRef}
  toolBarRender={() => (
    <ButtonList>
      <Button perm='user:create' type='primary' onClick={handleAdd}>
        <PlusOutlined /> 新增
      </Button>
    </ButtonList>
  )}
  request={(params) => HttpUtils.get('admin/user/page', params)}
  columns={columns}
/>
```

## Page

页面容器组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `padding` | 内边距 | `boolean` | `false` |
| `backgroundGray` | 灰色背景 | `boolean` | `false` |

## Ellipsis

文本省略组件。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `length` | 截取长度 | `number` | 15 |
| `pre` | 预格式化文本 | `boolean` | — |

## LinkButton

链接跳转按钮。

| 参数 | 说明 | 类型 |
|------|------|------|
| `path` | 跳转路径 | `string` |
| `label` | 页面标签 | `string` |
| `blank` | 新窗口打开 | `boolean` |

## DownloadFileButton

文件下载按钮。

| 参数 | 说明 | 类型 |
|------|------|------|
| `url` | 下载地址 | `string` |
| `params` | 下载参数 | `object` |

## NamedIcon

通过名称渲染 Ant Design 图标。

```jsx
<NamedIcon name="UserOutlined" />
<NamedIcon name="SettingOutlined" style={{ fontSize: 20, color: "#1890ff" }} />
```

## PageLoading

页面加载状态。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `tip` | 加载提示 | `string` | `加载中...` |
