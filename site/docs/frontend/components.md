# 前端组件库

本文档介绍了 open-admin 前端框架中提供的核心组件。

## 基础组件

### ProTable

数据表格组件，提供分页、筛选、工具栏等功能。

**参数**：

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
| `toolbarOptions` | 是否显示工具栏 | `boolean` | `true` |
| `showToolbarSearch` | 是否显示工具栏搜索 | `boolean` | - |
| `formRef` | 表单引用 | `ref` | - |

**示例**：

```jsx
<ProTable
  actionRef={this.tableRef}
  toolBarRender={(params, {selectedRows, selectedRowKeys}) => {
    return <ButtonList>
      <Button perm='user:save' type='primary' onClick={this.handleAdd}>
        <PlusOutlined/> 新增
      </Button>
    </ButtonList>
  }}
  request={(params) => HttpUtils.get('admin/user/page', params)}
  columns={this.columns}
/>
```

### ProModal

弹窗组件，用于展示对话框内容。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `title` | 弹窗标题 | `string` | - |
| `actionRef` | 弹窗操作引用（已弃用） | `ref` | - |
| `ref` | 弹窗引用 | `ref` | - |
| `onShow` | 弹窗显示回调 | `Function` | - |
| `footer` | 弹窗底部内容 | `ReactNode` | - |
| `width` | 弹窗宽度 | `number` | 800 |
| `children` | 弹窗内容 | `ReactNode` | - |

**示例**：

```jsx
<Modal title='用户信息'
       open={this.state.formOpen}
       onOk={() => this.formRef.current.submit()}
       onCancel={() => this.setState({formOpen: false})}
       destroyOnHidden
       maskClosable={false}
>
  <Form ref={this.formRef} labelCol={{flex: '100px'}}
        initialValues={this.state.formValues}
        onFinish={this.handleSave}
  >
    <Form.Item name='id' noStyle></Form.Item>
    <Form.Item label='名称' name='name' rules={[{required: true}]}>
      <Input/>
    </Form.Item>
  </Form>
</Modal>
```

### Ellipsis

文本省略组件，用于文本过长时以省略号显示，并可点击展开查看。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `length` | 文本截取长度 | `number` | 15 |
| `children` | 要处理的文本内容 | `ReactNode` | - |
| `pre` | 是否以预格式化文本显示 | `boolean` | - |

### LinkButton

链接按钮组件，用于跳转到指定页面。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `path` | 跳转路径 | `string` | - |
| `label` | 页面标签 | `string` | - |
| `size` | 按钮大小 | `string` | `small` |
| `children` | 按钮内容 | `ReactNode` | - |

### DownloadFileButton

下载文件按钮组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `url` | 文件下载地址 | `string` | - |
| `params` | 下载参数 | `object` | - |
| `children` | 按钮内容 | `ReactNode` | - |

### PageLoading

页面加载组件。

**参数**：无

### NamedIcon

命名图标组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `name` | 图标名称 | `string` | - |

### Page

页面组件。

**参数**：

| 参数 | 说明 | 类型 | 默认值 |
|------|------|------|--------|
| `padding` | 是否添加内边距 | `boolean` | `false` |
| `backgroundGray` | 是否设置背景为灰色 | `boolean` | `false` |
| `children` | 页面内容 | `ReactNode` | - |

## 使用示例

### 完整页面示例

```jsx
import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, Modal, Popconfirm} from 'antd'
import React from 'react'
import {ButtonList, FieldUploadFile, HttpUtils, Page, ProTable} from "@jiangood/open-admin";

export default class extends React.Component {

    state = {
        formValues: {},
        formOpen: false
    }

    formRef = React.createRef()
    tableRef = React.createRef()

    columns = [
        {
            title: '名称',
            dataIndex: 'name',
        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='user:save' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='user:delete' title='是否确定删除用户信息'  onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                </ButtonList>
            ),
        },
    ]

    handleAdd = () => {
        this.setState({formOpen: true, formValues: {}})
    }

    handleEdit = record => {
        this.setState({formOpen: true, formValues: record})
    }

    handleSave = values => {
        HttpUtils.post('admin/user/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }

    handleDelete = record => {
        HttpUtils.get('admin/user/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    render() {
        return <Page padding={true}>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={(params, {selectedRows, selectedRowKeys}) => {
                    return <ButtonList>
                        <Button perm='user:save' type='primary' onClick={this.handleAdd}>
                            <PlusOutlined/> 新增
                        </Button>
                    </ButtonList>
                }}
                request={(params) => HttpUtils.get('admin/user/page', params)}
                columns={this.columns}
            />

            <Modal title='用户信息'
                   open={this.state.formOpen}
                   onOk={() => this.formRef.current.submit()}
                   onCancel={() => this.setState({formOpen: false})}
                   destroyOnHidden
                   maskClosable={false}
            >
                <Form ref={this.formRef} labelCol={{flex: '100px'}}
                      initialValues={this.state.formValues}
                      onFinish={this.handleSave}
                >
                    <Form.Item name='id' noStyle></Form.Item>

                    <Form.Item label='名称' name='name' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>

                    <Form.Item label='爱好' name='fav' >
                        <Input/>
                    </Form.Item>

                </Form>
            </Modal>
        </Page>
    }
}
```