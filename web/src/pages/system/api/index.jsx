import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, message, Modal, Popconfirm, Switch, Table, Tabs} from 'antd'
import React from 'react'
import {
    ArrUtils,
    ButtonList,
    FieldBoolean,
    FieldDate,
    HttpUtils, ObjectUtils,
    Page,
    ProTable,
    StringUtils,
    ViewPassword
} from "../../../framework";


export default class extends React.Component {

    state = {
        formValues: {},
        formOpen: false,

        list: [],
        grantFormOpen: false
    }

    formRef = React.createRef()
    tableRef = React.createRef()

    componentDidMount() {
        HttpUtils.get("/admin/apiAccount/permList").then(rs => {
            this.setState({list: rs})
        })
    }

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
        HttpUtils.post('admin/apiAccount/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    columns = [
        {
            title: '账户名称',
            dataIndex: 'name',
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
            title: '权限',
            dataIndex: 'perms',
            render(perms) {
                if (!perms) {
                    return
                }
                return perms.join(', ')
            }
        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='api'
                            onClick={() => {
                                this.handlePreGrant(record);
                            }}
                            type='primary'>授权</Button>
                    <Button size='small' perm='api' onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='api' title='是否确定删除接口访客' onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                    <Button size='small' perm='api' href={'/admin/apiAccount/export/' + record.id} target={'_blank'}>下载文档</Button>
                </ButtonList>
            ),
        },
    ]

    handlePreGrant(record) {
        this.setState({grantFormOpen: true, formValues: ObjectUtils.clone(record)})
    }

    randomAppSecret = () => {
        const appSecret = StringUtils.random(32)
        this.formRef.current.setFieldsValue({appSecret})
    }

    onGrantItemChange = (id, checked) => {
        const perms = this.state.formValues.perms
        if (checked) {
            ArrUtils.add(perms, id)
        } else {
            ArrUtils.remove(perms, id)
        }
        this.setState({formValues: this.state.formValues})
    }

    onGrant = () => {
        const hide = message.loading('授权中...', 0)
        let accountId = this.state.formValues.id;
        HttpUtils.post('admin/apiAccount/grant/'+accountId,  this.state.formValues.perms).then(rs => {
            this.setState({grantFormOpen: false})
            this.tableRef.current.reload()
        }).finally(hide)
    };


    render() {
        return <Page padding>
            <Tabs items={[
                {
                    label: '接口文档',
                    key: 'doc',
                    children: <iframe src={'/admin/swagger-ui/index.html'} width="100%" height="1000px"></iframe>
                },
                {
                    label: '账号管理',
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
                },
                {
                    label: '访问记录',
                    key: 'log',
                    children: <ProTable
                        request={(params) => HttpUtils.get('admin/apiLog/page', params)}
                        columns={[
                            {
                                title: '接口',
                                dataIndex: 'url',
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
                            }
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
                   mask={{closable:false}}
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


            <Modal title='权限列表' destroyOnHidden={true} width={800}
                   open={this.state.grantFormOpen}
                   onCancel={() => this.setState({grantFormOpen: false, formValues: null})}
                   onOk={this.onGrant}
            >
                <Table
                    dataSource={this.state.list}
                    pagination={false}
                    rowKey='id'
                    size={'small'}
                    columns={[
                        {dataIndex: 'id', title: '标识'},
                        {dataIndex: 'name', title: '名称'},
                        {dataIndex: 'description', title: '描述'},
                        {dataIndex: 'path', title: '路径'},
                        {
                            dataIndex: 'option', title: '操作',
                            render: (_, record) => {
                                let id = record.id;
                                return <Switch checked={this.state.formValues.perms.includes(id)}
                                               onChange={(checked) => {
                                                   this.onGrantItemChange(id, checked)
                                               }} />
                            }
                        }
                    ]}

                />
            </Modal>
        </Page>


    }
}

