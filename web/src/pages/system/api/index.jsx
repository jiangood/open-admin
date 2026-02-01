import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, Modal, Popconfirm, Tabs} from 'antd'
import React from 'react'
import {
    ButtonList,
    FieldBoolean,
    FieldDate,
    HttpUtils,
    Page,
    PageUtils,
    ProTable,
    StringUtils,
    ViewPassword
} from "../../../framework";
import {ApiDoc} from "./ApiDoc";


export default class extends React.Component {

    state = {
        formValues: {},
        formOpen: false
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
        HttpUtils.post('admin/apiAccount/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }

    handleDelete = record => {
        HttpUtils.get('admin/apiAccount/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    columns = [
        {
            title: '名称',
            dataIndex: 'name',
        },
        {
            title: 'appId',
            dataIndex: 'appId',
        },

        {
            title: 'appSecret',
            dataIndex: 'appSecret',
            render(v) {
                return <ViewPassword value={v}></ViewPassword>;
            }
        },
        {
            title: '准入IP',
            dataIndex: 'accessIp',
        },
        {
            title: '有效期',
            dataIndex: 'endTime',
        },
        {
            title: '启用',
            dataIndex: 'enable',
            render(v) {
                return v == null ? null : (v ? '是' : '否')
            },
        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='api'
                            onClick={() => PageUtils.open('/system/api/perm?accountId=' + record.id, '账户权限')}
                            type='primary'>权限</Button>
                    <Button size='small' perm='api' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='api' title='是否确定删除接口访客' onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                </ButtonList>
            ),
        },
    ]

    randomAppSecret = () => {
        const appSecret = StringUtils.random(32)
        this.formRef.current.setFieldsValue({appSecret})
    }

    render() {
        return <Page padding>
            <Tabs items={[

                {
                    label: '接口列表',
                    key: '2',
                    children: <ProTable
                        rowKey='action'
                        columns={[
                            {dataIndex: 'name', title: '名称'},
                            {dataIndex: 'action', title: '动作'},
                            {dataIndex: 'desc', title: '描述'},

                        ]}
                        request={(params,) => HttpUtils.get('admin/api/resource/page', params)}
                    />
                },

                {
                    label: '账号列表',
                    key: '1',
                    children: <ProTable
                        actionRef={this.tableRef}
                        toolBarRender={() => {
                            return <ButtonList>
                                <Button perm='api' type='primary' onClick={this.handleAdd}>
                                    <PlusOutlined/> 新增
                                </Button>
                            </ButtonList>
                        }}
                        request={(params) => HttpUtils.get('admin/apiAccount/page', params)}
                        columns={this.columns}
                    />
                }, {
                    label: '接口文档',
                    key: '3',
                    children: <ApiDoc/>
                },
                {
                    label: '访问记录',
                    key: '4',
                    children: <ProTable
                        request={(params) => HttpUtils.get('admin/apiAccessLog/page', params)}
                        columns={[

                            {
                                title: '接口名称',
                                dataIndex: 'name',


                            },

                            {
                                title: '接口',
                                dataIndex: 'action',


                            },

                            {
                                title: 'requestId',
                                dataIndex: 'requestId',

                            },

                            {
                                title: '请求数据',
                                dataIndex: 'requestData',


                            },

                            {
                                title: '响应数据',
                                dataIndex: 'responseData',


                            },

                            {
                                title: 'ip',
                                dataIndex: 'ip',


                            },

                            {
                                title: 'ipLocation',
                                dataIndex: 'ipLocation',


                            },

                            {
                                title: '执行时间',
                                dataIndex: 'executionTime',


                            },

                            {
                                title: '接口账户',
                                dataIndex: 'accountName',


                            },


                        ]}
                    />
                },

            ]}>

            </Tabs>


            <Modal title='接口访客'
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


                    <Form.Item label='appId' name='appId'>
                        <Input placeholder='多个用逗号分隔'/>
                    </Form.Item>
                    <Form.Item label='appSecret' name='appSecret' rules={[{required: true}, {len: 32}]}
                               help={<Button size='small' type='link' onClick={this.randomAppSecret}>随机生成</Button>}
                    >
                        <Input/>
                    </Form.Item>

                    <Form.Item label='准入IP' name='accessIp'>
                        <Input placeholder='多个用逗号分隔'/>
                    </Form.Item>
                    <Form.Item label='有效期' name='endTime' style={{marginTop: 32}}>
                        <FieldDate type='YYYY-MM-DD'/>
                    </Form.Item>
                    <Form.Item label='启用' name='enable' rules={[{required: true}]}>
                        <FieldBoolean/>
                    </Form.Item>

                </Form>
            </Modal>
        </Page>


    }
}

