import {DeleteOutlined, EditOutlined, PlusOutlined} from '@ant-design/icons';
import {AutoComplete, Button, Form, Input, InputNumber, Modal, Tag, TreeSelect} from 'antd';
import React from 'react';
import {
    PermActions,
    FieldBoolean,
    FormModal,
    getMessageApi,
    HttpClient,
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

export default class DictPage extends React.Component {

    state = {
        typeTree: [],
        itemsModalOpen: false,
        currentType: null,
    }

    typesTableRef = React.createRef();
    itemsTableRef = React.createRef();
    typeModalRef = React.createRef();
    itemModalRef = React.createRef();

    componentDidMount() {
        this.loadTypeTree()
    }

    loadTypeTree = () => {
        HttpClient.get('admin/dict/type-tree', null, {autoShowErrorMessage: false}).then(rs => {
            this.setState({typeTree: rs.data})
        }).catch(() => {})
    }

    /**
     * 将 type-tree 返回的树扁平化为类型列表（去掉父子结构，按 seq 升序）。
     * 拷贝时去掉 children 保证 ProTable 停留在扁平表格，不渲染可展开行。
     */
    flattenTypes = (nodes, out = []) => {
        for (const node of nodes || []) {
            const {children, ...type} = node
            out.push(type)
            this.flattenTypes(children, out)
        }
        return out.sort((a, b) => (a.seq ?? 0) - (b.seq ?? 0))
    }

    requestTypes = (params) => {
        return HttpClient.get('admin/dict/type-tree', null, {autoShowErrorMessage: false}).then(rs => {
            const types = this.flattenTypes(rs.data).filter(t => {
                if (params.typeCode && !(t.typeCode || '').includes(params.typeCode)) return false
                if (params.typeLabel && !(t.typeLabel || '').includes(params.typeLabel)) return false
                return true
            })
            return {data: {content: types, totalElements: types.length, size: 10}}
        })
    }

    handleTypeAdd = () => {
        this.typeModalRef.current.open({enabled: true})
    }

    handleTypeEdit = (record) => {
        this.typeModalRef.current.open({...record})
    }

    handleTypeDelete = (record) => {
        HttpClient.post('admin/dict/type-delete', {id: record.id}, null).then((rs) => {
            getMessageApi().success(rs?.message || '删除成功')
            this.loadTypeTree()
            this.typesTableRef.current?.reload()
        })
    }

    handleTypeFormFinish = async values => {
        const isNew = !values.id
        const url = isNew ? 'admin/dict/type-create' : 'admin/dict/type-update'
        const rs = await HttpClient.post(url, values)
        getMessageApi().success(rs?.message || (isNew ? '新增成功' : '更新成功'))
        this.loadTypeTree()
        this.typesTableRef.current?.reload()
    }

    handleItemsModal = (record) => {
        this.setState({currentType: record, itemsModalOpen: true})
    }

    handleItemsModalCancel = () => {
        this.setState({itemsModalOpen: false, currentType: null})
    }

    handleItemAdd = () => {
        this.itemModalRef.current.open({typeCode: this.state.currentType?.typeCode})
    }

    handleItemEdit = record => {
        this.itemModalRef.current.open(record)
    }

    handleItemDelete = row => {
        HttpClient.post('admin/dict/delete', row, null).then((rs) => {
            getMessageApi().success(rs?.message || '删除成功')
            this.itemsTableRef.current?.reload()
        })
    }

    onItemFormFinish = async values => {
        const isNew = !values.id
        const url = isNew ? 'admin/dict/create' : 'admin/dict/update'
        const rs = await HttpClient.post(url, values)
        getMessageApi().success(rs?.message || (isNew ? '新增成功' : '更新成功'))
        this.itemsTableRef.current?.reload()
    }

    typeColumns = [
        {title: '类型编码', dataIndex: 'typeCode'},
        {title: '类型名称', dataIndex: 'typeLabel'},
        {
            title: '内置枚举', dataIndex: 'isEnum',
            render(v) {
                return <Tag color={v ? 'blue' : 'default'}>{v ? '内置' : '自定义'}</Tag>
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
                    size="small"
                    actions={[
                        {label: '编辑', perm: 'sys-dict:update', icon: <EditOutlined/>, onClick: () => this.handleTypeEdit(record)},
                        {label: '删除', perm: 'sys-dict:delete', icon: <DeleteOutlined/>, danger: true, confirm: '是否确定删除此类型及其所有子类型和字典项？', onClick: () => this.handleTypeDelete(record)},
                        {label: '字典项', perm: 'sys-dict:read', onClick: () => this.handleItemsModal(record)},
                    ]}
                />;
            },
        },
    ]

    itemColumns = [
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
        const {itemsModalOpen, currentType} = this.state

        return <Page>
            <ProTable
                actionRef={this.typesTableRef}
                rowKey='id'
                request={this.requestTypes}
                columns={this.typeColumns}
                searchInline
                searchFormRender={() => (
                    <>
                        <Form.Item name='typeCode'><Input.Search allowClear placeholder='类型编码关键字'/></Form.Item>
                        <Form.Item name='typeLabel'><Input.Search allowClear placeholder='类型名称关键字'/></Form.Item>
                    </>
                )}
                toolBarRender={() => (
                    <PermActions size='small' actions={[
                        {label: '新增类型', perm: 'sys-dict:create', icon: <PlusOutlined/>, onClick: this.handleTypeAdd},
                    ]}/>
                )}
            />

            <Modal
                title={currentType ? `字典项 - ${currentType.typeLabel}` : '字典项'}
                open={itemsModalOpen}
                onCancel={this.handleItemsModalCancel}
                onOk={this.handleItemsModalCancel}
                footer={null}
                width={720}
            >
                {currentType && (
                    <>
                        <Button type='primary' style={{marginBottom: 8}} icon={<PlusOutlined/>} onClick={this.handleItemAdd}>
                            新增字典项
                        </Button>
                        <ProTable
                            key={currentType.typeCode}
                            rowKey='uid'
                            actionRef={this.itemsTableRef}
                            request={(params) => {
                                params.typeCode = currentType.typeCode
                                return HttpClient.get('admin/dict/page', params)
                            }}
                            columns={this.itemColumns}
                        />
                    </>
                )}
            </Modal>

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

            <FormModal ref={this.itemModalRef} title='编辑字典项'
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
