# 前端字段组件

表单字段组件，用于 `Form.Item` 中。

## 列表

| 组件 | 说明 |
|------|------|
| `FieldRemoteSelect` | 远程搜索选择框 |
| `FieldRemoteSelectMultiple` | 远程搜索多选框 |
| `FieldRemoteTree` | 远程树形选择 |
| `FieldRemoteTreeSelect` | 远程树形选择器 |
| `FieldRemoteTreeSelectMultiple` | 远程树形多选 |
| `FieldRemoteTreeCascader` | 远程级联选择 |
| `FieldDictSelect` | 字典选择 |
| `FieldBoolean` | 布尔值选择（select/radio/checkbox/switch） |
| `FieldDate` | 日期选择 |
| `FieldDateRange` | 日期范围选择 |
| `FieldEditor` | 富文本编辑器 |
| `FieldTable` | 表格字段 |
| `FieldTableSelect` | 表格选择 |
| `FieldSysOrgTree` | 系统组织树选择 |
| `FieldSysOrgTreeSelect` | 系统组织树选择器 |
| `FieldPercent` | 百分比输入 |
| `FieldUploadFile` | 文件上传 |

## 使用示例

```jsx
<Form ref={this.formRef}
      initialValues={this.state.formValues}
      onFinish={this.handleSave}>
  <Form.Item name='id' noStyle />

  <Form.Item label='名称' name='name' rules={[{required: true}]}>
    <Input />
  </Form.Item>

  <Form.Item label='状态' name='status'>
    <FieldDictSelect dict='user_status' />
  </Form.Item>

  <Form.Item label='所属部门' name='deptId'>
    <FieldSysOrgTreeSelect type='dept' />
  </Form.Item>

  <Form.Item label='创建时间' name='createTime'>
    <FieldDate />
  </Form.Item>

  <Form.Item label='头像' name='avatar'>
    <FieldUploadFile listType='picture' />
  </Form.Item>
</Form>
```

## FieldUploadFile

文件上传组件，上传地址为 `/admin/sysFile/upload`。

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 上传地址 | `string` | — |
| `listType` | 样式 | `string` | `picture-card` |
| `maxCount` | 最大数量 | `number` | 1 |
| `accept` | 接受类型 | `string` | — |
| `cropImage` | 裁剪图片 | `boolean` | — |

### 图片预览与缩略图

图片上传后，可通过预览接口访问：

```
/preview/{fileId}          # 原图
/preview/{fileId}?w=400    # 宽度 400px 缩略图
/preview/{fileId}?w=800    # 宽度 800px 缩略图
/preview/{fileId}?w=1200   # 宽度 1200px 缩略图
```

缩略图采用 **懒生成** 策略：上传时只保存原图，首次请求缩略图时实时生成并缓存，后续请求直接返回缓存。上传不会因图片格式兼容性问题（如 CMYK JPEG）中断。
