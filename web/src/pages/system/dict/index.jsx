import React from "react";
import {Button, Form, Input, InputNumber, Modal, Popconfirm, Splitter, Tag, Tree, Typography} from "antd";
import {
    ButtonList,
    FieldBoolean, FieldDictSelect, FieldRemoteSelect,
    HttpUtils,
    Page,
    ProTable,
    ViewBooleanEnableDisable, ViewText
} from "../../../framework";
import {PlusOutlined} from "@ant-design/icons";

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
        HttpUtils.post('admin/dict/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }

    handleDelete = row => {
        HttpUtils.post('admin/dict/delete', row).then(rs => {
            this.tableRef.current.reload()
        })
    }

    columns = [
        {
            title: '类型标签',
            dataIndex: 'typeLabel',
        },
        {
            title: '类型编码',
            dataIndex: 'typeCode',
        },
        {
            title: '标签',
            dataIndex: 'label',
        },
        {
            title: '编码',
            dataIndex: 'code',
        },

        {
            title: '启用',
            dataIndex: 'enabled',
            render(v) {
                return <ViewBooleanEnableDisable value={v}/>
            }

        },
        {
            title: '显示颜色',
            dataIndex: 'color',
            render(v) {
                if (v) {
                    return <Tag color={v.toLowerCase()}>{v}</Tag>
                }

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
                if(!record.id){ // 非数据库定义的不让修改
                    return
                }

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

    render() {
        return <Page padding>
            <ProTable
                rowKey='uid'
                actionRef={this.tableRef}
                toolBarRender={() => {
                    return <ButtonList>
                        <Button perm='sysDict:save' type='primary' onClick={this.handleAdd} >
                            <PlusOutlined/> 新增
                        </Button>
                    </ButtonList>
                }}
                request={(params) => {
                    params.typeCode = this.state.typeCode
                    return HttpUtils.get('admin/dict/page', params);
                }}
                columns={this.columns}
                showToolbarSearch={true}
                scrollY={500}
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
                    <Form.Item label='类型' name='color' rules={[{required: true}]} >
                       <ViewText />
                    </Form.Item>
                    <Form.Item label='类型' name='typeCode' rules={[{required: true}]} >
                        <FieldRemoteSelect url='/admin/dict/typeOptions' />
                    </Form.Item>
                    <Form.Item label='编码' name='code' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>
                    <Form.Item label='标签' name='label' rules={[{required: true}]} help='显示文本'>
                        <Input/>
                    </Form.Item>
                    <Form.Item label='颜色' name='color' rules={[{required: true}]}>
                        <FieldDictSelect typeCode='statusColor' />
                    </Form.Item>
                    <Form.Item label='序号' name='seq' rules={[{required: true}]}>
                        <InputNumber/>
                    </Form.Item>

                    <Form.Item label='启用' name='enabled' rules={[{required: true}]}>
                        <FieldBoolean/>
                    </Form.Item>

                </Form>
            </Modal>
        </Page>


    }


}
