import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, InputNumber, Modal, Popconfirm, Select} from 'antd'
import React from 'react'
import {FieldBoolean, HttpUtils, Page, PermActions, ProTable} from "../../../framework";

const {TextArea} = Input;

export default class extends React.Component {

    state = {
        formValues: {},
        formOpen: false,
    }

    formRef = React.createRef()
    tableRef = React.createRef()

    handleAdd = () => {
        this.setState({formOpen: true, formValues: {}})
    }

    handleEdit = record => {
        this.setState({formOpen: true, formValues: record})
    }

    onFinish = values => {
        const isNew = !values.id;
        const url = isNew ? 'admin/article/create' : 'admin/article/update';
        HttpUtils.post(url, values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }

    handleDelete = record => {
        HttpUtils.post('admin/article/delete', {id: record.id}).then(rs => {
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
            title: '显示位置',
            dataIndex: 'position',
            render(v) {
                const map = {dropdown: '用户菜单', header: '顶栏导航', none: '不显示'}
                return map[v] || v
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
                return v == null ? null : (v ? '是' : '否')
            },
        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => {
                return (
                    <PermActions>
                        <Button size='small' perm='article:update' onClick={() => this.handleEdit(record)}>编辑</Button>
                        <Popconfirm perm='article:delete' title='确定删除?' onConfirm={() => this.handleDelete(record)}>
                            <Button size='small'>删除</Button>
                        </Popconfirm>
                    </PermActions>
                );
            },
        },
    ]

    render() {
        return <Page
            title="文章管理"
            description="管理系统文章，如关于、帮助等页面"
            actions={<Button perm='article:create' type='primary' icon={<PlusOutlined/>} onClick={this.handleAdd}>新增</Button>}
        >
            <ProTable
                actionRef={this.tableRef}
                request={(params) => HttpUtils.get('admin/article/page', params)}
                columns={this.columns}
            >
                <Form.Item label='编码' name='code'>
                    <Input/>
                </Form.Item>
                <Form.Item label='标题' name='title'>
                    <Input/>
                </Form.Item>
            </ProTable>

            <Modal title='文章'
                   open={this.state.formOpen}
                   onOk={() => this.formRef.current.submit()}
                   onCancel={() => this.setState({formOpen: false})}
                   destroyOnHidden
                   width={800}
            >
                <Form ref={this.formRef} labelCol={{flex: '100px'}}
                      initialValues={this.state.formValues}
                      onFinish={this.onFinish}
                >
                    <Form.Item name='id' noStyle></Form.Item>

                    <Form.Item label='编码' name='code' rules={[{required: true}]}>
                        <Input disabled={!!this.state.formValues.id}/>
                    </Form.Item>

                    <Form.Item label='标题' name='title' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>

                    <Form.Item label='内容' name='content'>
                        <TextArea rows={12}/>
                    </Form.Item>

                    <Form.Item label='显示位置' name='position' rules={[{required: true}]}>
                        <Select>
                            <Select.Option value='dropdown'>用户菜单</Select.Option>
                            <Select.Option value='header'>顶栏导航</Select.Option>
                            <Select.Option value='none'>不显示</Select.Option>
                        </Select>
                    </Form.Item>

                    <Form.Item label='排序' name='seq'>
                        <InputNumber/>
                    </Form.Item>

                    <Form.Item label='启用' name='enabled'>
                        <FieldBoolean/>
                    </Form.Item>
                </Form>
            </Modal>
        </Page>
    }
}
