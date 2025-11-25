import {Button, Card, Form, Modal, Table} from 'antd'
import React from 'react'

import {ButtonList, HttpUtils, ValueType} from '../../../framework'


export default class extends React.Component {

    state = {
        data: [],
        formValues: {},
        formOpen: false
    }

    formRef = React.createRef()
    tableRef = React.createRef()

    columns = [
        {
            title: '参数名称',
            dataIndex: 'name',
            width: 300,

        },
        {
            title: '编码',
            dataIndex: 'code',
        },


        {
            title: '值',
            dataIndex: 'value',
            render(v, record) {
                if (v != null) {
                    return ValueType.renderView(record.valueType, {value: v})
                }
            }
        },


        {
            title: '说明',
            dataIndex: 'description',
        },
        {
            title: '更新时间',
            dataIndex: 'updateTime',
        },

        {
            title: '操作',
            dataIndex: 'option',
            fixed: 'right',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='sysConfig:save' onClick={() => this.handleEdit(record)}> 修改 </Button>
                </ButtonList>
            ),
        },
    ]

    componentDidMount() {
        this.loadData();
    }

    loadData() {
        HttpUtils.get('admin/sysConfig/page').then(rs => {
            this.setState({data: rs})
        })
    }

    handleEdit = record => {
        this.setState({formOpen: true, formValues: record})
    }


    onFinish = values => {
        HttpUtils.post('admin/sysConfig/save', values).then(rs => {
            this.setState({formOpen: false})
            this.loadData()
        })
    }


    render() {
        return <>
            <Card loading={this.state.data.length === 0}>
                <Table
                    dataSource={this.state.data}
                    actionRef={this.tableRef}
                    pagination={false}
                    expandable={
                        {
                            defaultExpandAllRows: true
                        }
                    }
                    columns={this.columns}
                    rowKey='id'
                    bordered
                    size='small'
                />
            </Card>


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



