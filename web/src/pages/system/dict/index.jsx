import {DeleteOutlined, EditOutlined, PlusOutlined} from '@ant-design/icons';
import {Button, Card, Descriptions, Empty, Form, Input, InputNumber, Modal, Popconfirm, Splitter, Tree, Tag, TreeSelect} from 'antd';
import React from 'react';
import {
    ButtonList,
    FieldBoolean,
    FieldDictSelect,
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

    findNode = (nodes, key) => {
        for (const n of nodes) {
            if (n.id === key) return n
            if (n.children) {
                const found = this.findNode(n.children, key)
                if (found) return found
            }
        }
        return null
    }

    getParentLabel = () => {
        const {selectedType, typeTree} = this.state
        if (!selectedType?.pid) return null
        const parent = this.findNode(typeTree, selectedType.pid)
        return parent?.typeLabel || null
    }

    onTreeSelect = (selectedKeys) => {
        if (selectedKeys.length === 0) {
            this.setState({selectedType: null, selectedTypeCode: null})
            return
        }
        const node = this.findNode(this.state.typeTree, selectedKeys[0])
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

        return <Page title="数据字典" description="管理数据字典类型和字典项"
                    actions={<Button perm='sys-dict:create' icon={<PlusOutlined/>} onClick={this.handleTypeAdd}>新增类型</Button>}>
            <Splitter>
                <Splitter.Panel defaultSize={300} style={{paddingRight: 8}}>
                    <Card loading={this.state.treeLoading}
                          size='small'
                    >
                        <div style={{fontWeight: 600, marginBottom: 8}}>字典类型</div>
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

                <Splitter.Panel style={{paddingLeft: 8}}>
                    <Card size='small' style={{marginBottom: 8}}>
                        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8}}>
                            <span style={{fontWeight: 600}}>类型信息</span>
                            {hasTypeSelected && (
                                <span>
                                    <Button size='small' icon={<EditOutlined/>} perm='sys-dict:update'
                                            onClick={this.handleTypeEdit} style={{marginRight: 4}}>编辑</Button>
                                    <Popconfirm perm='sys-dict:delete' title='是否确定删除此类型及其所有子类型和字典项？'
                                                onConfirm={this.handleTypeDelete}>
                                        <Button size='small' danger icon={<DeleteOutlined/>}>删除</Button>
                                    </Popconfirm>
                                </span>
                            )}
                        </div>
                        {hasTypeSelected && <Descriptions size='small' column={3}>
                            <Descriptions.Item label="名称">{selectedType.typeLabel}</Descriptions.Item>
                            <Descriptions.Item label="类型编码">{selectedType.typeCode}</Descriptions.Item>
                            <Descriptions.Item label="分类">{this.getParentLabel() || '-'}</Descriptions.Item>
                            <Descriptions.Item label="序号">{selectedType.seq}</Descriptions.Item>
                            <Descriptions.Item label="启用"><ViewBooleanEnableDisable value={selectedType.enabled}/></Descriptions.Item>
                        </Descriptions>}
                    </Card>
                    <Card size='small'>
                        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8}}>
                            <span style={{fontWeight: 600}}>字典项</span>
                            {hasTypeSelected && <Button type='primary' icon={<PlusOutlined/>} onClick={this.handleItemAdd}>新增</Button>}
                        </div>
                        {hasTypeSelected && <ProTable
                                rowKey='uid'
                                actionRef={this.tableRef}
                                request={(params) => {
                                    params.typeCode = selectedTypeCode
                                    return HttpUtils.get('admin/dict/page', params)
                                }}
                                columns={this.columns}
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
