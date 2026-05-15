# 系统组件

## ButtonList

带权限控制的按钮列表。

| 参数 | 说明 | 类型 |
|------|------|------|
| `maxNum` | 超过数量收缩 | `number` |

```jsx
<ButtonList>
  <Button perm='user:create' onClick={handleAdd}>新增</Button>
  <Button perm='user:update' onClick={handleEdit}>编辑</Button>
  <Popconfirm perm='user:delete' title='确定删除?' onConfirm={handleDelete}>
    <Button>删除</Button>
  </Popconfirm>
</ButtonList>
```

## HasPerm

权限控制容器。

```jsx
<HasPerm perm="user:export">
  <导出功能 />
</HasPerm>
```

## View 系列

### ViewEllipsis

文本省略（展开/收起）。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `text` | 文本 | `string` | — |
| `length` | 截取长度 | `number` | 15 |
| `expandable` | 可展开 | `boolean` | true |
| `tooltip` | tooltip | `boolean` | true |

### ViewFile

文件链接和预览。

| 参数 | 说明 | 类型 |
|------|------|------|
| `value` | 文件路径 | `string` |
| `preview` | 支持预览 | `boolean` |

### ViewImage

图片显示和预览。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 图片路径 | `string` | — |
| `width` | 宽度 | `string` | `80px` |
| `height` | 高度 | `string` | `80px` |

### ViewBooleanEnableDisable

布尔值显示为"启用"/"禁用"。

```jsx
<ViewBooleanEnableDisable value={true} />  {/* 显示"启用" */}
```
