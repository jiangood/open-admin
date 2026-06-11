import {DeleteOutlined, EditOutlined, PlusOutlined, SyncOutlined} from '@ant-design/icons';
import {Button, Card, Empty, Form, Input, InputNumber, Modal, Popconfirm, Space, Splitter, Tree, Tag, TreeSelect} from 'antd';
import React from 'react';
import {
    ButtonList,
    FieldBoolean,
    FieldDictSelect,
    Gap,
    HttpUtils,
    Page,
    ProTable,
    ViewBooleanEnableDisable,
} from "../../../framework";

export default class extends React.Component {

    state = {
        typeTree: [],
        treeLoading: false,
        selectedType: null,
        selectedTypeCode: null,

        formOpen: false,
        formValues: {},
        typeFormOpen: false,
        typeFormValues: {},
    }

    formRef = React.createRef();
    typeFormRef = React.createRef();
    tableRef = React.createRef();

    componentDidMount() {
        this.loadTree()
    }

    loadTree = () => {
        this.setState({treeLoading: true})
        HttpUtils.get('admin/dict/type-tree').then(rs => {
            this.setState({typeTree: rs})
        }).finally(() => {
            this.setState({treeLoading: false})
        })
    }

    handleTypeAdd = () => {
        this.setState({
            typeFormOpen: true,
            typeFormValues: {pid: this.state.selectedType?.id, enabled: true},
        })
    }

    handleTypeEdit = () => {
        const {selectedType} = this.state
        if (!selectedType) return
        this.setState({typeFormOpen: true, typeFormValues: {...selectedType}})
    }

    handleTypeDelete = () => {
        const {selectedType} = this.state
        if (!selectedType) return
        HttpUtils.post('admin/dict/type-delete', {id: selectedType.id}).then(() => {
            this.setState({selectedType: null, selectedTypeCode: null})
            this.loadTree()
        })
    }

    handleTypeFormFinish = values => {
        const isNew = !values.id
        const url = isNew ? 'admin/dict/type-create' : 'admin/dict/type-update'
        HttpUtils.post(url, values).then(rs => {
            this.setState({typeFormOpen: false})
            this.loadTree()
        })
    }

    onTreeSelect = (selectedKeys) => {
        if (selectedKeys.length === 0) {
            this.setState({selectedType: null, selectedTypeCode: null})
            return
        }
        const findNode = (nodes, key) => {
            for (const n of nodes) {
                if (n.id === key) return n
                if (n.children) {
                    const found = findNode(n.children, key)
                    if (found) return found
                }
            }
            return null
        }
        const node = findNode(this.state.typeTree, selectedKeys[0])
        this.setState({
            selectedType: node,
            selectedTypeCode: node?.typeCode || null
        },()=>this.tableRef.current.reload())
    }

    handleItemAdd = () => {
        this.setState({formOpen: true, formValues: {typeCode: this.state.selectedTypeCode}})
    }

    handleItemEdit = record => {
        this.setState({formOpen: true, formValues: record})
    }

    handleItemDelete = row => {
        HttpUtils.post('admin/dict/delete', row).then(rs => {
            this.tableRef.current.reload()
        })
    }

    onItemFormFinish = values => {
        const isNew = !values.id
        const url = isNew ? 'admin/dict/create' : 'admin/dict/update'
        HttpUtils.post(url, values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }

    columns = [
        {title: '编码', dataIndex: 'code'},
        {title: '标签', dataIndex: 'label'},
        {
            title: '颜色', dataIndex: 'color',
            render(v) {
                if (v) return <Tag color={v.toLowerCase()}>{v}</Tag>
            }
        },
        {
            title: '启用', dataIndex: 'enabled',
            render(v) {
                return <ViewBooleanEnableDisable value={v}/>
            }
        },
        {title: '序号', dataIndex: 'seq'},
        {
            title: '操作', dataIndex: 'option',
            render: (_, record) => {
                return (
                    <ButtonList>
                        <Button size='small' perm='sys-dict:update'
                                onClick={() => this.handleItemEdit(record)}>编辑</Button>
                        <Popconfirm perm='sys-dict:delete' title='是否确定删除字典项'
                                    onConfirm={() => this.handleItemDelete(record)}>
                            <Button size='small'>删除</Button>
                        </Popconfirm>
                    </ButtonList>
                );
            },
        },
    ]

    render() {
        const {selectedType, selectedTypeCode} = this.state
        const hasTypeSelected = selectedType != null && selectedTypeCode != null

        return <Page>
            <Splitter>
                <Splitter.Panel defaultSize={300}>
                    <Card loading={this.state.treeLoading}
                          title='字典类型'
                          extra={<Space>
                              <Button size='small' shape='round' icon={<SyncOutlined/>}
                                      onClick={this.loadTree}/>
                          </Space>}
                    >
                        <ButtonList>
                            <Button type='primary' size='small' onClick={this.handleTypeAdd}>
                                <PlusOutlined/> 新增类型
                            </Button>
                            <Button size='small' disabled={!selectedType} onClick={this.handleTypeEdit}>
                                <EditOutlined/> 编辑
                            </Button>
                            <Popconfirm title='是否确定删除此类型及其所有子类型和字典项？'
                                        disabled={!selectedType}
                                        onConfirm={this.handleTypeDelete}>
                                <Button size='small' disabled={!selectedType}>
                                    <DeleteOutlined/> 删除
                                </Button>
                            </Popconfirm>
                        </ButtonList>
                        <Gap/>
                        <Tree
                            treeData={this.state.typeTree}
                            onSelect={this.onTreeSelect}
                            fieldNames={{title: 'typeLabel', key: 'id'}}
                            showLine
                            defaultExpandAll
                            blockNode
                        />
                        {this.state.typeTree.length === 0 && <Empty/>}


                    </Card>
                </Splitter.Panel>

                <Splitter.Panel>
                    <Card title={hasTypeSelected ? `${selectedType.typeLabel} 的字典项` : '字典项'}
                          extra={
                              hasTypeSelected
                                  ? <Button type='primary' onClick={this.handleItemAdd}>
                                      <PlusOutlined/> 新增
                                  </Button>
                                  : null
                          }
                    >
                        {!hasTypeSelected
                            ? <Empty description='请在左侧选择一个字典类型'/>
                            : <ProTable
                                rowKey='uid'
                                actionRef={this.tableRef}
                                request={(params) => {
                                    params.typeCode = selectedTypeCode
                                    return HttpUtils.get('admin/dict/page', params)
                                }}
                                columns={this.columns}
                                showToolbarSearch
                            />
                        }
                    </Card>
                </Splitter.Panel>
            </Splitter>

            <Modal title={this.state.typeFormValues?.id ? '编辑字典类型' : '新增字典类型'}
                   open={this.state.typeFormOpen}
                   onOk={() => this.typeFormRef.current.submit()}
                   onCancel={() => this.setState({typeFormOpen: false})}
                   destroyOnClose
            >
                <Form ref={this.typeFormRef} labelCol={{flex: '100px'}}
                      initialValues={this.state.typeFormValues}
                      onFinish={this.handleTypeFormFinish}
                >
                    <Form.Item name='id' noStyle/>
                    <Form.Item label='父类型' name='pid'>
                        <TreeSelect treeData={this.state.typeTree}
                                    fieldNames={{label: 'typeLabel', value: 'id'}}
                                    allowClear
                        />
                    </Form.Item>
                    <Form.Item label='类型编码' name='typeCode' rules={[{required: true}]}>
                        <Input placeholder='举例: orderStatus'/>
                    </Form.Item>
                    <Form.Item label='类型名称' name='typeLabel' rules={[{required: true}]}>
                        <Input placeholder='举例: 订单状态'/>
                    </Form.Item>
                    <Form.Item label='启用' name='enabled' rules={[{required: true}]}>
                        <FieldBoolean/>
                    </Form.Item>
                    <Form.Item label='序号' name='seq'>
                        <InputNumber/>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal title='编辑字典项'
                   open={this.state.formOpen}
                   onOk={() => this.formRef.current.submit()}
                   onCancel={() => this.setState({formOpen: false})}
                   destroyOnClose
            >
                <Form ref={this.formRef} labelCol={{flex: '100px'}}
                      initialValues={this.state.formValues}
                      onFinish={this.onItemFormFinish}
                >
                    <Form.Item name='id' noStyle/>
                    <Form.Item label='类型编码' name='typeCode'>
                        <Input disabled/>
                    </Form.Item>
                    <Form.Item label='编码' name='code' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>
                    <Form.Item label='标签' name='label' rules={[{required: true}]} help='显示文本'>
                        <Input/>
                    </Form.Item>
                    <Form.Item label='颜色' name='color'>
                        <FieldDictSelect typeCode='statusColor'/>
                    </Form.Item>
                    <Form.Item label='序号' name='seq'>
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
