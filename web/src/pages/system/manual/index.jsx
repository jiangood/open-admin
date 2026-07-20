import {CloudDownloadOutlined, PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, Modal, Popconfirm, Tag} from 'antd'
import React from 'react'
import {ButtonList, FieldUploadFile, HttpUtils, Page, ProTable} from "../../../framework";


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
        const url = isNew ? 'admin/sysManual/create' : 'admin/sysManual/update';
        HttpUtils.post(url, values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }

    handleDelete = record => {
        HttpUtils.post('admin/sysManual/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    columns = [

        {
            title: '名称',
            dataIndex: 'name',


        },

        {
            title: '版本',
            dataIndex: 'version',
            render(version) {
                return 'v' + version;
            }
        },

        {
            title: '文件',
            dataIndex: 'fileId',
            render(id) {
                const url = 'admin/sysFile/preview/' + id;
                return <a href={url} target='_blank'>查看文件</a>
            }

        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='sys-manual:update' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='sys-manual:delete' title='是否确定删除操作手册'
                                onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                </ButtonList>
            ),
        },

    ]

    render() {
        return <Page title="操作手册">
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={(params, {selectedRows, selectedRowKeys}) => {
                    return <ButtonList>
                        <Button perm='sys-manual:create' type='primary' icon={<PlusOutlined/>} onClick={this.handleAdd}>
                            新增
                        </Button>
                    </ButtonList>
                }}
                request={(params) => HttpUtils.get('admin/sysManual/page', params)}
                columns={this.columns}

            />

            <Modal title='操作手册'
                   open={this.state.formOpen}
                   onOk={() => this.formRef.current.submit()}
                   onCancel={() => this.setState({formOpen: false})}
                   destroyOnHidden
                   mask={{ closable: false }}
            >

                <Form ref={this.formRef} labelCol={{flex: '100px'}}
                      initialValues={this.state.formValues}
                      onFinish={this.onFinish}
                >
                    <Form.Item name='id' noStyle></Form.Item>

                    <Form.Item label='名称' name='name' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>

                    <Form.Item label='文件' name='fileId' rules={[{required: true}]}>
                        <FieldUploadFile accept=".pdf" maxCount={1}/>
                    </Form.Item>

                </Form>
            </Modal>
        </Page>


    }
}


