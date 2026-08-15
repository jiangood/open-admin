import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, InputNumber, Modal, Transfer} from 'antd'
import React from 'react'
import {FieldBoolean, FormModal, HttpClient, Page, PageUtils, PermActions, ProTable, ViewText} from "../../../framework";


export default class RolePage extends React.Component {

    state = {
        usersModalOpen: false,
        usersModalLoading: false,
        selectedRecord: null,

        userList: [],
        targetKeys: [],
        selectedKeys: [],
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
        HttpClient.get('admin/sysRole/user-list', {id: record.id}, rs => {
            this.setState({userList: rs.list, targetKeys: rs.selectedKeys})
        })
    }

    onFinish = values => {
        const isNew = !values.id;
        const url = isNew ? 'admin/sysRole/create' : 'admin/sysRole/update';
        HttpClient.post(url, values, null, () => {
            this.tableRef.current.reload()
        })
    }

    handleDelete = record => {
        HttpClient.post('admin/sysRole/delete', {id: record.id}, null, () => {
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

                return <PermActions
                    more
                    size="small"
                    actions={[
                        {label: '用户设置', perm: 'sys-role:grant-permission', onClick: () => this.handleEditUser(record)},
                        {label: '权限设置', perm: 'sys-role:grant-permission', onClick: () => PageUtils.open('/system/role/rolePerm?id=' + record.id, '角色权限设置')},
                        {label: '编辑', perm: 'sys-role:update', onClick: () => this.handleEdit(record)},
                        {label: '删除', perm: 'sys-role:delete', confirm: '是否确定删除系统角色', onClick: () => this.handleDelete(record)},
                    ]}
                />;
            },
        },
    ]

    handleSaveUsers = () => {
        this.setState({usersModalLoading: true})
        const params = {
            id: this.state.selectedRecord.id,
            userIdList: this.state.targetKeys
        }
        HttpClient.post('admin/sysRole/grant-users', params, null, () => {
            this.setState({usersModalOpen: false, usersModalLoading: false})
        }, () => {
            this.setState({usersModalLoading: false})
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
                request={(params, success, error) => HttpClient.get('admin/sysRole/page', params, success, error)}
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
                    onChange={(nextTargetKeys, _direction, _moveKeys) => {
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
        </Page>


    }
}

