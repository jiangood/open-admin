# 前端字段组件

本文档介绍了 open-admin 前端框架中提供的字段组件，这些组件主要用于表单中。

## 字段组件

### FieldRemoteSelect

远程搜索选择框组件，支持模糊搜索和远程加载选项。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取选项数据的API地址 | `string` | - |
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `placeholder` | 占位提示文本 | `string` | `请搜索选择` |

### FieldRemoteSelectMultiple

远程搜索多选框组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取选项数据的API地址 | `string` | - |
| `value` | 当前值 | `Array` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `placeholder` | 占位提示文本 | `string` | `请搜索选择` |

### FieldRemoteTree

远程树形选择组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取树数据的API地址 | `string` | - |
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |

### FieldDictSelect

字典选择组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `dict` | 字典类型 | `string` | - |
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |

### FieldEditor

富文本编辑器组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `height` | 编辑器高度 | `number` | - |

### FieldRemoteTreeCascader

远程树形级联选择组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取树数据的API地址 | `string` | - |
| `value` | 当前值 | `Array` | - |
| `onChange` | 值改变回调函数 | `Function` | - |

### FieldRemoteTreeSelect

远程树形选择器组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取树数据的API地址 | `string` | - |
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `treeDefaultExpandAll` | 是否默认展开所有节点 | `boolean` | `true` |

### FieldRemoteTreeSelectMultiple

远程树形多选择器组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取树数据的API地址 | `string` | - |
| `value` | 当前值 | `Array` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `treeDefaultExpandAll` | 是否默认展开所有节点 | `boolean` | `true` |

### FieldBoolean

布尔值选择组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `boolean` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `type` | 显示类型(select/radio/checkbox/switch) | `string` | `select` |

### FieldDate

日期选择组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `type` | 日期类型 | `string` | `YYYY-MM-DD` |

### FieldDateRange

日期范围选择组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `[string, string]` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `type` | 日期范围类型 | `string` | `YYYY-MM-DD` |

### FieldTable

表格字段组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `Array` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `columns` | 表格列定义 | `Array` | - |
| `style` | 样式 | `object` | - |

### FieldTableSelect

表格选择组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 获取表格数据的API地址 | `string` | - |
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `columns` | 表格列定义 | `Array` | - |
| `placeholder` | 占位提示文本 | `string` | `请搜索选择` |

### FieldSysOrgTree

系统组织树选择组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `type` | 组织类型(dept/unit) | `string` | `dept` |

### FieldSysOrgTreeSelect

系统组织树选择器组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `type` | 组织类型(dept/unit) | `string` | `dept` |

### FieldPercent

百分比输入组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `number` | - |
| `onChange` | 值改变回调函数 | `Function` | - |

### FieldUploadFile

文件上传组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `value` | 当前值 | `string` | - |
| `onChange` | 值改变回调函数 | `Function` | - |
| `url` | 上传地址 | `string` | - |
| `listType` | 上传列表的内建样式 | `string` | `picture-card` |
| `maxCount` | 最大上传数量 | `number` | 1 |
| `accept` | 接受上传的文件类型 | `string` | - |
| `cropImage` | 是否裁剪图片 | `boolean` | - |

## 使用示例

### 表单中使用字段组件

```jsx
<Form ref={this.formRef} labelCol={{flex: '100px'}}
      initialValues={this.state.formValues}
      onFinish={this.handleSave}
>
  <Form.Item name='id' noStyle></Form.Item>

  <Form.Item label='名称' name='name' rules={[{required: true}]}>
    <Input/>
  </Form.Item>

  <Form.Item label='状态' name='status'>
    <FieldDictSelect dict='user_status'/>
  </Form.Item>

  <Form.Item label='所属部门' name='deptId'>
    <FieldSysOrgTreeSelect type='dept'/>
  </Form.Item>

  <Form.Item label='创建时间' name='createTime'>
    <FieldDate/>
  </Form.Item>

  <Form.Item label='描述' name='description'>
    <FieldEditor/>
  </Form.Item>

  <Form.Item label='头像' name='avatar'>
    <FieldUploadFile url='/api/file/upload' listType='picture'/>
  </Form.Item>
</Form>
```