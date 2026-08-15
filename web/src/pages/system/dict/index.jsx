import {DeleteOutlined, EditOutlined, PlusOutlined} from '@ant-design/icons';
import {AutoComplete, Button, Card, Descriptions, Empty, Form, Input, InputNumber, Splitter, Tree, Tag, TreeSelect, Typography} from 'antd';
import React from 'react';
import {
    PermActions,
    FieldBoolean,
    FormModal,
    HttpUtils,
    Page,
    ProTable,
    ViewSwitch,
} from "../../../framework";

const COLOR_OPTIONS = [
    {value: 'SUCCESS', label: '成功'},
    {value: 'PROCESSING', label: '处理中'},
    {value: 'ERROR', label: '错误'},
    {value: 'WARNING', label: '警告'},
    {value: 'DEFAULT', label: '默认'},
    {value: 'RED', label: '红色'},
    {value: 'BLUE', label: '蓝色'},
    {value: 'GREEN', label: '绿色'},
    {value: 'GRAY', label: '灰色'},
]

export default class extends React.Component {

    state = {
        typeTree: [],
        treeLoading: false,
        selectedType: null,
        selectedTypeCode: null,
    }

    modalRef = React.createRef();
    typeModalRef = React.createRef();
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
        this.typeModalRef.current.open({pid: this.state.selectedType?.id, enabled: true})
    }

    handleTypeEdit = () => {
        const {selectedType} = this.state
        if (!selectedType) return
        this.typeModalRef.current.open({...selectedType})
    }

    handleTypeDelete = () => {
        const {selectedType} = this.state
        if (!selectedType) return
        HttpUtils.post('admin/dict/type-delete', {id: selectedType.id}).then(() => {
            this.setState({selectedType: null, selectedTypeCode: null})
            this.loadTree()
        })
    }

    handleTypeFormFinish = async values => {
        const isNew = !values.id
        const url = isNew ? 'admin/dict/type-create' : 'admin/dict/type-update'
        await HttpUtils.post(url, values)
        this.loadTree()
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
        this.setState((prevState) => {
            const node = this.findNode(prevState.typeTree, selectedKeys[0])
            return {
                selectedType: node,
                selectedTypeCode: node?.typeCode || null
            }
        }, () => this.tableRef.current.reload())
    }

    handleItemAdd = () => {
        this.modalRef.current.open({typeCode: this.state.selectedTypeCode})
    }

    handleItemEdit = record => {
        this.modalRef.current.open(record)
    }

    handleItemDelete = row => {
        HttpUtils.post('admin/dict/delete', row).then(rs => {
            this.tableRef.current.reload()
        })
    }

    onItemFormFinish = async values => {
        const isNew = !values.id
        const url = isNew ? 'admin/dict/create' : 'admin/dict/update'
        await HttpUtils.post(url, values)
        this.tableRef.current.reload()
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
                return <ViewSwitch value={v}/>
            }
        },
        {title: '序号', dataIndex: 'seq'},
        {
            title: '操作', dataIndex: 'option',
            render: (_, record) => {
                return <PermActions
                    more
                    size="small"
                    actions={[
                        {label: '编辑', perm: 'sys-dict:update', onClick: () => this.handleItemEdit(record)},
                        {label: '删除', perm: 'sys-dict:delete', confirm: '是否确定删除字典项', onClick: () => this.handleItemDelete(record)},
                    ]}
                />;
            },
        },
    ]

    render() {
        const {selectedType, selectedTypeCode} = this.state
        const hasTypeSelected = selectedType != null && selectedTypeCode != null

        return <Page title="数据字典" description="管理数据字典类型和字典项">
            <Splitter>
                <Splitter.Panel defaultSize={300} style={{paddingRight: 8}}>
                    <Card loading={this.state.treeLoading}
                          size='small'
                          title={<div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                              <Typography.Text strong>字典类型</Typography.Text>
                              <PermActions size='small' actions={[
                                  {label: '新增类型', perm: 'sys-dict:create', icon: <PlusOutlined/>, onClick: this.handleTypeAdd},
                              ]}/>
                          </div>}
                    >
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
                            <Typography.Text strong>类型信息</Typography.Text>
                            {hasTypeSelected && (
                                <PermActions size='small' actions={[
                                    {label: '编辑', perm: 'sys-dict:update', icon: <EditOutlined/>, onClick: this.handleTypeEdit},
                                    {
                                        label: '删除',
                                        perm: 'sys-dict:delete',
                                        icon: <DeleteOutlined/>,
                                        confirm: '是否确定删除此类型及其所有子类型和字典项？',
                                        onClick: this.handleTypeDelete,
                                    },
                                ]}/>
                            )}
                        </div>
                        {hasTypeSelected && <Descriptions size='small' column={3}>
                            <Descriptions.Item label="名称">{selectedType.typeLabel}</Descriptions.Item>
                            <Descriptions.Item label="类型编码">{selectedType.typeCode}</Descriptions.Item>
                            <Descriptions.Item label="分类">{this.getParentLabel() || '-'}</Descriptions.Item>
                            <Descriptions.Item label="序号">{selectedType.seq}</Descriptions.Item>
                            <Descriptions.Item label="启用"><ViewSwitch value={selectedType.enabled}/></Descriptions.Item>
                        </Descriptions>}
                    </Card>
                    <Card size='small'>
                        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8}}>
                            <Typography.Text strong>字典项</Typography.Text>
                            {hasTypeSelected && <Button type='primary' icon={<PlusOutlined/>} onClick={this.handleItemAdd}>新增字典项</Button>}
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

            <FormModal ref={this.typeModalRef} title='编辑字典类型'
                       onFinish={this.handleTypeFormFinish}>
                <Form.Item label='父类型' name='pid'>
                    <TreeSelect treeData={this.state.typeTree}
                                fieldNames={{label: 'typeLabel', value: 'id'}}
                                allowClear/>
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
            </FormModal>

            <FormModal ref={this.modalRef} title='编辑字典项'
                       onFinish={this.onItemFormFinish}>
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
                    <AutoComplete
                        allowClear
                        maxLength={20}
                        options={COLOR_OPTIONS}
                        filterOption={(input, option) =>
                            (option.value + option.label).toLowerCase().includes(input.toLowerCase())
                        }
                        placeholder='如 SUCCESS、#ff0000'
                    />
                </Form.Item>
                <Form.Item label='序号' name='seq'>
                    <InputNumber/>
                </Form.Item>
                <Form.Item label='启用' name='enabled' rules={[{required: true}]}>
                    <FieldBoolean/>
                </Form.Item>
            </FormModal>
        </Page>
    }
}
