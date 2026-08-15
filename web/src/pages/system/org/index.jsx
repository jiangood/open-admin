import {DeleteOutlined, EditOutlined, PlusOutlined, SettingOutlined, SyncOutlined} from '@ant-design/icons';
import {Button, Card, Checkbox, Descriptions, Form, Input, InputNumber, Popover, Space, Spin, Splitter, Switch, Tree, Typography} from 'antd';
import React from 'react';
import {
    FieldBoolean,
    FieldRemoteSelect,
    FieldRemoteTreeSelect,
    FieldUserSelect,
    FormModal, Gap,
    HttpClient,
    NamedIcon,
    Page,
    PermActions,
    ViewSwitch,
} from "../../../framework";

export default class extends React.Component {

    state = {
        selectedOrg: null,
        params: {
            onlyShowEnabled: true,
            onlyShowUnit: false,
            searchText: null,
        },
        treeData: [],
        treeLoading: false,
        draggable: false,
    }
    modalRef = React.createRef();
    treeRef = React.createRef();

    componentDidMount() {
        this.loadTree()
    }

    loadTree = () => {
        this.setState({treeLoading: true})
        HttpClient.get('admin/sysOrg/tree', this.state.params, rs => {
            this.setState({treeData: rs})
            this.setState({treeLoading: false});
        }, () => {
            this.setState({treeLoading: false});
        })
    }

    handleDelete = () => {
        const {selectedOrg} = this.state
        if (!selectedOrg) return
        HttpClient.post('admin/sysOrg/delete', {id: selectedOrg.id}, null, () => {
            this.setState({selectedOrg: null})
            this.loadTree()
        })
    }

    onSelect = (selectedKeys) => {
        if (selectedKeys.length === 0) {
            this.setState({selectedOrg: null})
            return
        }
        HttpClient.get("admin/sysOrg/detail", {id: selectedKeys[0]}, rs => {
            this.setState({selectedOrg: rs})
        })
    }

    handleAdd = () => {
        const {selectedOrg} = this.state
        this.modalRef.current.open({pid: selectedOrg?.id, enabled: true})
    }

    handleEdit = () => {
        this.modalRef.current.open({...this.state.selectedOrg})
    }

    handleModalFinish = values => {
        const isNew = !values.id
        const url = isNew ? 'admin/sysOrg/create' : 'admin/sysOrg/update'
        HttpClient.post(url, values, null, () => {
            this.loadTree()
        })
    }

    onDraggableChange = e => {
        this.setState({draggable: e})
    };

    render() {
        const {selectedOrg} = this.state
        const params = this.state.params

        return <Page title="组织机构" description="管理组织机构树" actions={
            <Button type='primary' perm='sys-org:create' icon={<PlusOutlined/>} onClick={this.handleAdd}>新增</Button>
        }>
            <Splitter>
                <Splitter.Panel defaultSize={400} style={{paddingRight: 8}}>

                    <Card size='small' >
                        <div style={{display: 'flex', alignItems: 'center', gap: 4}}>
                            <Input.Search placeholder='搜索' value={params.searchText} onChange={e => {
                                params.searchText = e.target.value
                                this.setState({params}, this.loadTree)
                            }} style={{flex: 1}}/>
                            <Popover
                                trigger='click'
                                placement='bottomRight'
                                title='设置'
                                content={<Space orientation='vertical'>
                                    <Checkbox checked={params.onlyShowEnabled}
                                              onChange={e => {
                                                  params.onlyShowEnabled = e.target.checked;
                                                  this.setState({params}, this.loadTree);
                                              }}>仅显示启用</Checkbox>
                                    <Checkbox checked={params.onlyShowUnit}
                                              onChange={e => {
                                                  params.onlyShowUnit = e.target.checked;
                                                  this.setState({params}, this.loadTree);
                                              }}>仅显示单位</Checkbox>
                                    <div>
                                        拖拽排序&nbsp;<Switch
                                        value={this.state.draggable}
                                        onChange={this.onDraggableChange}/>
                                    </div>
                                    <Button size='small' shape='round' icon={<SyncOutlined/>} onClick={this.loadTree}>刷新</Button>
                                </Space>}
                            >
                                <Button type='text' size='small' icon={<SettingOutlined/>}/>
                            </Popover>
                        </div>
                        <Gap />
                        <Spin spinning={this.state.treeLoading}>
                            <Tree ref={this.treeRef}
                                  treeData={this.state.treeData}
                                  onSelect={this.onSelect}
                                  showIcon
                                  blockNode
                                  icon={item => <NamedIcon name={item.data.iconName}/>}
                                  draggable={this.state.draggable}
                                  onDrop={this.onDrop}
                                  showLine
                                  defaultExpandAll
                            />
                        </Spin>
                    </Card>
                </Splitter.Panel>

                <Splitter.Panel style={{paddingLeft: 8}}>
                    <Card size='small' style={{marginBottom: 8}}>
                        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8}}>
                            <Typography.Text strong>机构信息</Typography.Text>
                            {selectedOrg && (
                                <PermActions size='small' actions={[
                                    {label: '编辑', perm: 'sys-org:update', icon: <EditOutlined/>, onClick: this.handleEdit},
                                    {
                                        label: '删除',
                                        perm: 'sys-org:delete',
                                        icon: <DeleteOutlined/>,
                                        confirm: '是否确定删除组织机构',
                                        onClick: this.handleDelete,
                                    },
                                ]}/>
                            )}
                        </div>
                        {selectedOrg && (
                            <Descriptions size='small' column={2}>
                                <Descriptions.Item label="唯一标识">{selectedOrg.id}</Descriptions.Item>
                                <Descriptions.Item label="名称">{selectedOrg.name}</Descriptions.Item>
                                <Descriptions.Item label="类型">{selectedOrg.typeLabel}</Descriptions.Item>
                                <Descriptions.Item label="上级机构">{selectedOrg.parentName || '-'}</Descriptions.Item>
                                <Descriptions.Item label="序号">{selectedOrg.seq ?? '-'}</Descriptions.Item>
                                <Descriptions.Item label="部门领导">{selectedOrg.leader?.name || '-'}</Descriptions.Item>
                                <Descriptions.Item label="启用"><ViewSwitch value={selectedOrg.enabled}/></Descriptions.Item>
                                {selectedOrg.extra1 && <Descriptions.Item label="扩展字段1">{selectedOrg.extra1}</Descriptions.Item>}
                                {selectedOrg.extra2 && <Descriptions.Item label="扩展字段2">{selectedOrg.extra2}</Descriptions.Item>}
                                {selectedOrg.extra3 && <Descriptions.Item label="扩展字段3">{selectedOrg.extra3}</Descriptions.Item>}
                            </Descriptions>
                        )}
                    </Card>
                </Splitter.Panel>
            </Splitter>

            <FormModal ref={this.modalRef} title='编辑组织机构'
                       onFinish={this.handleModalFinish}
                       labelCol={{flex: '120px'}}>
                <Form.Item label='父节点' name='pid'>
                    <FieldRemoteTreeSelect url='admin/sysOrg/tree'/>
                </Form.Item>
                <Form.Item label='名称' name='name' rules={[{required: true}]}>
                    <Input/>
                </Form.Item>
                <Form.Item label='序号' name='seq'>
                    <InputNumber/>
                </Form.Item>
                <Form.Item label='类型' name='type' rules={[{required: true}]}>
                    <FieldRemoteSelect url='admin/sysOrg/type-options' placeholder='请选择类型'/>
                </Form.Item>
                <Form.Item label='部门领导' name={['leader', 'id']}>
                    <FieldUserSelect/>
                </Form.Item>
                <Form.Item label='启用' name='enabled' rules={[{required: true}]}>
                    <FieldBoolean/>
                </Form.Item>
                <Form.Item label='扩展字段1' name='extra1'>
                    <Input/>
                </Form.Item>
                <Form.Item label='扩展字段2' name='extra2'>
                    <Input/>
                </Form.Item>
                <Form.Item label='扩展字段3' name='extra3'>
                    <Input/>
                </Form.Item>
            </FormModal>
        </Page>
    }

    onDrop = (e) => {
        const {dragNode, dropToGap, node} = e;
        const dropKey = node.key;
        const dragKey = dragNode.key;
        const dropPos = e.node.pos.split('-');
        const dropPosition = e.dropPosition - Number(dropPos[dropPos.length - 1]);
        HttpClient.post('admin/sysOrg/sort', {dropPosition, dropToGap, dropKey, dragKey}, null, () => this.loadTree())
    };
}



