import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Transfer, Tree} from 'antd'
import React from 'react'
import {FieldBoolean, FormModal, HttpUtils, Page, PageUtils, PermActions, ProTable, ViewText} from "../../../framework";


export default class extends React.Component {

    state = {
        usersModalOpen: false,
        usersModalLoading: false,
        selectedRecord: null,

        userList: [],
        targetKeys: [],
        selectedKeys: [],

        menuOpen: false,
        menuTree: [],
        menuTreeLoading: false,
        menuChecked: [],
        menuHalfChecked: []
    }

    modalRef = React.createRef()
    tableRef = React.createRef()

    handleAdd = () => {
        this.modalRef.current.open({})
    }

    handleEdit = record => {
        this.modalRef.current.open(record)
    }

    handleEditUser = record => {
        this.setState({usersModalOpen: true, selectedRecord: record})
        HttpUtils.get('admin/sysRole/user-list', {id: record.id}).then(rs => {
            this.setState({userList: rs.list, targetKeys: rs.selectedKeys})
        })
    }

    onFinish = async values => {
        const isNew = !values.id;
        const url = isNew ? 'admin/sysRole/create' : 'admin/sysRole/update';
        await HttpUtils.post(url, values)
        this.tableRef.current.reload()
    }

    handleDelete = record => {
        HttpUtils.post('admin/sysRole/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    columns = [

        {
            title: '名称',
            dataIndex: 'name',


        },

        {
            title: '编号',
            dataIndex: 'code',


        },

        {
            title: '排序',
            dataIndex: 'seq',


        },

        {
            title: '备注',
            dataIndex: 'remark',


        },

        {
            title: '启用',
            dataIndex: 'enabled',


            render(v) {
                return v == null ? null : (v ? '是' : '否')
            },


        },
        {
            title: '权限码',
            dataIndex: 'perms',
            width: 300,
            render(v) {
                if (v) {
                    return <ViewText value={v.join(',')} ellipsis={true} />
                }
            }

        },


        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => {

                return (
                    <PermActions>
                        <Button size='small' perm='sys-role:grant-permission'
                                onClick={() => this.handleEditUser(record)}>用户设置</Button>

                        <Button size='small' perm='sys-role:grant-permission'
                                onClick={() => PageUtils.open('/system/role/rolePerm?id=' + record.id, '角色权限设置')}>权限设置</Button>

                        <Button size='small' perm='sys-role:update'
                                onClick={() => this.handleEdit(record)}>编辑</Button>
                        <Popconfirm perm='sys-role:delete' title='是否确定删除系统角色'
                                    onConfirm={() => this.handleDelete(record)}>
                            <Button size='small'>删除</Button>
                        </Popconfirm>
                    </PermActions>
                );
            },
        },
    ]

    handleSaveUsers = () => {
        this.setState({usersModalLoading: true})
        const params = {
            id: this.state.selectedRecord.id,
            userIdList: this.state.targetKeys
        }
        HttpUtils.post('admin/sysRole/grant-users', params).then(rs => {
            this.setState({usersModalOpen: false, usersModalLoading: false})
        }).catch(() => {
            this.setState({usersModalLoading: false})
        })
    }

    handleEditMenu = (record) => {
        this.setState({menuOpen: true, selectedRecord: record, menuTreeLoading: true})
        HttpUtils.get('admin/sysRole/ownMenu', {id: record.id}).then(rs => {
            this.setState({menuChecked: rs.checked, menuHalfChecked: rs.halfChecked})
        })
        HttpUtils.get('admin/sysRole/menuTree').then(rs => {
            this.setState({menuTree: rs, menuTreeLoading: false})
        })
    }
    handleGrantMenu = () => {
        const params = {
            id: this.state.selectedRecord.id,
            menuIds: [...this.state.menuChecked, ...this.state.menuHalfChecked]
        }
        HttpUtils.post('admin/sysRole/grantMenu', params).then(rs => {
            this.setState({menuOpen: false})
        })
    }

    render() {
        return <Page
            title="角色管理"
            description="管理系统角色，包括角色权限分配、用户设置等"
        >
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={() => (
                    <PermActions>
                        <Button perm='sys-role:create' type='primary' icon={<PlusOutlined/>} onClick={this.handleAdd}>新增</Button>
                    </PermActions>
                )}
                request={(params) => HttpUtils.get('admin/sysRole/page', params)}
                columns={this.columns}
                searchFormRender={() => (
                    <>
                        <Form.Item label='角色名称' name='name'>
                            <Input/>
                        </Form.Item>
                        <Form.Item label='角色编码' name='code'>
                            <Input/>
                        </Form.Item>
                    </>
                )}
            />

            <FormModal ref={this.modalRef} title='系统角色' onFinish={this.onFinish}>

                <Form.Item label='名称' name='name' rules={[{required: true}]}>
                    <Input/>
                </Form.Item>

                <Form.Item label='编码' name='code' rules={[{required: true}]}>
                    <Input/>
                </Form.Item>

                <Form.Item label='排序' name='seq'>
                    <InputNumber/>
                </Form.Item>

                <Form.Item label='备注' name='remark'>
                    <Input/>
                </Form.Item>

                <Form.Item label='启用' name='enabled' rules={[{required: true}]}>
                    <FieldBoolean/>
                </Form.Item>

            </FormModal>


            <Modal title={'角色用户' + "【" + this.state.selectedRecord?.name + '】'}
                   open={this.state.usersModalOpen}
                   confirmLoading={this.state.usersModalLoading}
                   destroyOnHidden
                   mask={{ closable: false }}
                   width={800}
                   onCancel={() => this.setState({usersModalOpen: false})}
                   onOk={this.handleSaveUsers}
            >


                <Transfer
                    styles={{ section: { height: '60vh', width: 300 } }}

                    dataSource={this.state.userList} titles={["未选择", "已选择"]}
                    targetKeys={this.state.targetKeys}
                    selectedKeys={this.state.selectedKeys}
                    render={item => item.title}
                    onChange={(nextTargetKeys, direction, moveKeys) => {
                        this.setState({
                            targetKeys: nextTargetKeys
                        })
                    }}
                    onSelectChange={(sourceSelectedKeys, targetSelectedKeys) => {
                        this.setState({
                            selectedKeys: [...sourceSelectedKeys, ...targetSelectedKeys]
                        })
                    }}
                    showSearch
                />


            </Modal>

            <Modal title={'角色授权菜单权限' + "【" + this.state.selectedRecord?.name + '】'}
                   open={this.state.menuOpen}
                   destroyOnHidden
                   mask={{ closable: false }}
                   width={800}
                   onCancel={() => this.setState({menuOpen: false})}
                   onOk={this.handleGrantMenu}
                   loading={this.state.menuTreeLoading}
            >
                <Tree
                    height={600}
                    treeData={this.state.menuTree}
                    multiple
                    checkable
                    checkedKeys={{checked: this.state.menuChecked}}
                    onCheck={(keys, e) => {
                        this.setState({menuChecked: keys, menuHalfChecked: e.halfCheckedKeys})
                    }}
                    defaultExpandAll
                    titleRender={node => {
                        return <span title={node.perm}>{node.title}</span>
                    }}
                />
            </Modal>
        </Page>


    }
}

