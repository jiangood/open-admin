import {Button, Form, Modal, Popconfirm} from 'antd'
import React from 'react'

import {ButtonList, HttpUtil, ProTable,ValueType} from '@/framework'


export default class extends React.Component {

    state = {
        formValues: {},
        formOpen: false
    }

    formRef = React.createRef()
    tableRef = React.createRef()

    columns = [

        {
            title: '标识',
            dataIndex: 'id',
            width: 250,
        },

        {
            title: '参数标签',
            dataIndex: 'label', width: 250,
        },

        {
            title: '参数值',
            dataIndex: 'value',
            render(v, record) {
                if (v != null) {
                    return ValueType.renderView(record.valueType, {value: v})
                }
            }
        },


        {
            title: '默认值',
            dataIndex: 'defaultValue',
            render(v, record) {
                return ValueType.renderView(record.valueType, {value: v})
            }
        },
        {
            title: '备注',
            dataIndex: 'remark',
            width: 400
        },
        {
            title: '更新时间',
            dataIndex: 'updateTime', width: 150,
        },

        {
            title: '操作',
            dataIndex: 'option',
            fixed: 'right',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='sysConfig:save' onClick={() => this.handleEdit(record)}> 编辑 </Button>
                    <Popconfirm perm='sysConfig:delete' title='是否确定删除接口访客'
                                onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                </ButtonList>
            ),
        },
    ]


    handleEdit = record => {
        this.setState({formOpen: true, formValues: record})
    }


    onFinish = values => {
        HttpUtil.post('sysConfig/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }

    handleDelete = record => {
        HttpUtil.get('sysConfig/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    render() {
        return <>
            <ProTable
                actionRef={this.tableRef}
                request={(params) => HttpUtil.pageData('sysConfig/page', params)}
                columns={this.columns}
                defaultPageSize={1000}
            />

            <Modal title={'编辑系统参数'}
                   open={this.state.formOpen}
                   onOk={() => this.formRef.current.submit()}
                   onCancel={() => this.setState({formOpen: false})}
                   destroyOnHidden
                   maskClosable={false}
                   width={400}
            >

                <Form ref={this.formRef}
                      initialValues={this.state.formValues}
                      onFinish={this.onFinish}
                      layout='vertical'
                >

                    <Form.Item name='id' noStyle/>

                    <Form.Item name='value' label={this.state.formValues.label}>
                        {ValueType.renderField(this.state.formValues.valueType)}
                    </Form.Item>

                </Form>
            </Modal>
        </>


    }
}



