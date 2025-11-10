import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, InputNumber, Modal, Popconfirm} from 'antd'
import React from 'react'
import {ButtonList, FieldUploadFile, HttpUtil, Page, ProTable, SysUtil, ViewFile} from "@tmgg/tmgg-base";
import {width} from "@tmgg/tmgg-commons-lang";


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
            title: '版本',
            dataIndex: 'version',
            render(version) {
                return 'v' +version;
            }
        },

        {
            title: '文件',
            dataIndex: 'fileId',
            render(id){
               const url = SysUtil.wrapServerUrl('sysFile/preview/' + id);
                return <a href={url} target='_blank'>查看文件</a>
            }

        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='sysManual:save' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='sysManual:delete' title='是否确定删除操作手册'  onConfirm={() => this.handleDelete(record)}>
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


    onFinish = values => {
        HttpUtil.post('sysManual/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }


    handleDelete = record => {
        HttpUtil.get('sysManual/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    render() {
        return <Page>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={(params, {selectedRows, selectedRowKeys}) => {
                    return <ButtonList>
                        <Button perm='sysManual:save' type='primary' onClick={this.handleAdd}>
                            <PlusOutlined/> 新增
                        </Button>
                    </ButtonList>
                }}
                request={(params) => HttpUtil.pageData('sysManual/page', params)}
                columns={this.columns}

            />

            <Modal title='操作手册'
                   open={this.state.formOpen}
                   onOk={() => this.formRef.current.submit()}
                   onCancel={() => this.setState({formOpen: false})}
                   destroyOnHidden
                   maskClosable={false}
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
                        <FieldUploadFile accept=".pdf" maxCount={1} listType='text'/>
                    </Form.Item>

                </Form>
            </Modal>
        </Page>


    }
}


