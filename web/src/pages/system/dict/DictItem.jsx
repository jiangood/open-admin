import {PlusOutlined} from '@ant-design/icons'
import {Button, Col, Form, Input, InputNumber, Modal, Popconfirm, Row, Tag} from 'antd'
import React from 'react'

import {ButtonList, FieldRadioBoolean, HttpUtil, ProTable} from '@tmgg/tmgg-base'


export default class extends React.Component {

    state = {
        formValues: {},
        formOpen: false
    }

    formRef = React.createRef()
    tableRef = React.createRef()

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevProps.sysDictId !== this.props.sysDictId) {
            this.tableRef.current.reload()
        }
    }

    columns = [
        {
            title: '文本',
            dataIndex: 'text',
        },
        {
            title: '编码',
            dataIndex: 'code',
        },

        {
            title: '启用',
            dataIndex: 'enabled',
            render(v) {
                return v ? '是' : '否'
            }

        },
        {
            title: '显示颜色',
            dataIndex: 'color',
            render(v) {
                return <Tag color={v}>COLOR</Tag>
            }
        },
        {
            title: '系统内置',
            dataIndex: 'builtin',
            render(v) {
                return v ? '是' : '否'
            }

        },
        {
            title: '序号',
            dataIndex: 'seq',
        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => {

                return (
                    <ButtonList>
                        <Button size='small' perm='sysDict:save'
                                onClick={() => this.handleEdit(record)}> 编辑 </Button>
                        <Popconfirm perm='sysDict:delete' title='是否确定删除字典项'
                                    onConfirm={() => this.handleDelete(record)}>
                            <Button size='small'>删除</Button>
                        </Popconfirm>
                    </ButtonList>
                );
            },
        },
    ]

    handleAdd = () => {
        this.setState({formOpen: true, formValues: {}})
    }

    handleEdit = record => {
        this.setState({formOpen: true, formValues: record})
    }


    onFinish = values => {
        values.sysDict = {id: this.props.sysDictId}
        HttpUtil.post('sysDictItem/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }


    handleDelete = row => {
        HttpUtil.get('sysDictItem/delete', row).then(rs => {
            this.tableRef.current.reload()
        })
    }

    render() {
        return <>
            <ProTable
                headerTitle='字典项列表'
                actionRef={this.tableRef}
                toolBarRender={() => {
                    return <ButtonList>
                        <Button perm='sysDictItem:save' type='primary' onClick={this.handleAdd}>
                            <PlusOutlined/> 新增
                        </Button>
                    </ButtonList>
                }}
                request={(params) => {
                    params.sysDictId = this.props.sysDictId
                    return HttpUtil.pageData('sysDictItem/page', params);
                }}
                columns={this.columns}
                rowKey='id'
                search={false}
            />

            <Modal
                title='编辑数据字典项'
                open={this.state.formOpen}
                onOk={() => this.formRef.current.submit()}
                onCancel={() => this.setState({formOpen: false})}
                destroyOnHidden
            >

                <Form ref={this.formRef} labelCol={{flex: '100px'}}
                      initialValues={this.state.formValues}
                      onFinish={this.onFinish}>
                    <Form.Item name='id' noStyle></Form.Item>

                    <Row>
                        <Col span={12}>
                            <Form.Item label='编码' name='code' rules={[{required: true}]}>
                                <Input/>
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item label='文本' name='text' rules={[{required: true}]}>
                                <Input/>
                            </Form.Item>
                        </Col>
                    </Row>

                    <Row>
                        <Col span={12}>
                            <Form.Item label='颜色' name='color' rules={[{required: true}]}>
                                <Input/>
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item label='序号' name='seq' rules={[{required: true}]}>
                                <InputNumber/>
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item label='启用' name='enabled' rules={[{required: true}]}>
                        <FieldRadioBoolean/>
                    </Form.Item>

                </Form>
            </Modal>
        </>


    }
}



