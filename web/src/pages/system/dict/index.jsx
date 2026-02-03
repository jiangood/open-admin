import React from "react";
import {Button, Form, Input, InputNumber, Modal, Popconfirm, Splitter, Tag, Tree, Typography} from "antd";
import {
    ButtonList,
    FieldBoolean, FieldDictSelect,
    HttpUtils,
    Page,
    PageLoading,
    ProTable,
    ViewBooleanEnableDisable
} from "../../../framework";
import {PlusOutlined} from "@ant-design/icons";

export default class extends React.Component {

    state = {
        treeData: [],
        treeLoading: true,
        typeCode: null,
        formValues: {},
        formOpen: false
    }

    formRef = React.createRef()
    tableRef = React.createRef()

    async componentDidMount() {
        const treeData = await HttpUtils.get('admin/dict/tree');
        this.setState({treeData, treeLoading: false});
    }

    onSelect = (selectedKeys) => {
        const key = selectedKeys.length === 1 ? selectedKeys[0] : null;
        this.setState({typeCode: key}, () => {
            this.tableRef.current.reload()
        })
    }
    handleAdd = () => {
        this.setState({formOpen: true, formValues: {}})
    }

    handleEdit = record => {
        this.setState({formOpen: true, formValues: record})
    }

    onFinish = values => {
        values.sysDict = {id: this.props.sysDictId}
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
            title: '文本',
            dataIndex: 'name',
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
        if (this.state.treeLoading) {
            return <PageLoading/>
        }

        return <Page>
            <Splitter>
                <Splitter.Panel defaultSize={600} style={{paddingRight: 16}}>
                    <Tree
                        treeData={this.state.treeData}
                        onSelect={this.onSelect}

                        showIcon
                        blockNode
                        showLine
                        defaultExpandAll
                    >
                    </Tree>
                </Splitter.Panel>
                <Splitter.Panel style={{paddingLeft: 16}}>
                    {this.renderTypeInfo()}
                    <ProTable
                        rowKey='code'
                        actionRef={this.tableRef}
                        toolBarRender={() => {
                            return <ButtonList>
                                <Button perm='sysDict:save' type='primary' onClick={this.handleAdd} disabled={!this.state.typeCode}>
                                    <PlusOutlined/> 新增
                                </Button>
                            </ButtonList>
                        }}
                        request={(params) => {
                            params.typeCode = this.state.typeCode
                            return HttpUtils.get('admin/dict/page', params);
                        }}
                        columns={this.columns}
                        search={false}
                    />
                </Splitter.Panel>
            </Splitter>
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
                    <Form.Item label='类型编码' name='typeCode' rules={[{required: true}]} initialValue={this.state.typeCode}>
                        <Input/>
                    </Form.Item>
                    <Form.Item label='编码' name='code' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>
                    <Form.Item label='文本' name='name' rules={[{required: true}]} >
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

    renderTypeInfo() {
        if(this.state.typeCode){
            return <>
                <Typography.Text italic> 类型：</Typography.Text>
                <Typography.Text copyable>
                    {this.state.typeCode}
                </Typography.Text>
            </>;
        }
    }
}
