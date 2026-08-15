import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, InputNumber} from 'antd'
import React from 'react'
import {
    DictUtils,
    FieldBoolean,
    FieldDictSelect,
    FieldEditor,
    FieldUploadImage,
    FormModal,
    HttpClient,
    Page,
    PageUtils,
    PermActions,
    ProTable,
    ViewImage
} from "../../../framework";

export default class ArticleListPage extends React.Component {

    state = {
        editing: false,
    }

    modalRef = React.createRef()
    tableRef = React.createRef()

    handleAdd = () => {
        this.setState({editing: false})
        this.modalRef.current.open({})
    }

    handleEdit = record => {
        this.setState({editing: true})
        this.modalRef.current.open(record)
    }

    handlePreview = record => {
        PageUtils.open('/article/' + record.code, record.title)
    }

    onFinish = values => {
        const isNew = !values.id;
        const url = isNew ? 'admin/article/create' : 'admin/article/update';
        HttpClient.post(url, values, null, () => {
            this.tableRef.current.reload()
        })
    }

    handleDelete = record => {
        HttpClient.post('admin/article/delete', {id: record.id}, null, () => {
            this.tableRef.current.reload()
        })
    }

    columns = [
        {
            title: '编码',
            dataIndex: 'code',
        },
        {
            title: '标题',
            dataIndex: 'title',
        },
        {
            title: '主图',
            dataIndex: 'mainImage',
            width: 80,
            render(v) {
                return <ViewImage value={v} size={48}/>
            },
        },
        {
            title: '显示位置',
            dataIndex: 'position',
            render(v) {
                return DictUtils.dictLabel('articlePosition', v) || v
            },
        },
        {
            title: '排序',
            dataIndex: 'seq',
        },
        {
            title: '启用',
            dataIndex: 'enabled',
            render(v) {
                if (v == null) return null;
                return v ? '是' : '否';
            },
        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => {
                return <PermActions
                    more
                    size="small"
                    actions={[
                        {label: '预览', onClick: () => this.handlePreview(record)},
                        {label: '编辑', perm: 'article:update', onClick: () => this.handleEdit(record)},
                        {label: '删除', perm: 'article:delete', confirm: '确定删除?', onClick: () => this.handleDelete(record)},
                    ]}
                />;
            },
        },
    ]

    render() {
        return <Page
            title="文章管理"
            description="管理系统文章，如关于、帮助等页面"
        >
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={() => (
                    <PermActions>
                        <Button perm='article:create' type='primary' icon={<PlusOutlined/>} onClick={this.handleAdd}>新增</Button>
                    </PermActions>
                )}
                request={(params, success, error) => HttpClient.get('admin/article/page', params, success, error)}
                columns={this.columns}
                searchFormRender={() => (
                    <>
                        <Form.Item label='编码' name='code'>
                            <Input/>
                        </Form.Item>
                        <Form.Item label='标题' name='title'>
                            <Input/>
                        </Form.Item>
                    </>
                )}
            />

            <FormModal ref={this.modalRef} title='文章' width={800} onFinish={this.onFinish}>

                <Form.Item label='编码' name='code' rules={[{required: true}]}>
                    <Input disabled={this.state.editing}/>
                </Form.Item>

                <Form.Item label='标题' name='title' rules={[{required: true}]}>
                    <Input/>
                </Form.Item>

                <Form.Item label='主图' name='mainImage'>
                    <FieldUploadImage maxCount={1}/>
                </Form.Item>

                <Form.Item label='内容' name='content'>
                    <FieldEditor />
                </Form.Item>

                <Form.Item label='显示位置' name='position' rules={[{required: true}]}>
                    <FieldDictSelect typeCode='articlePosition'/>
                </Form.Item>

                <Form.Item label='排序' name='seq'>
                    <InputNumber/>
                </Form.Item>

                <Form.Item label='启用' name='enabled'>
                    <FieldBoolean/>
                </Form.Item>
            </FormModal>
        </Page>
    }
}
