import {PlusOutlined} from '@ant-design/icons';
import {Button, Form, Input, Modal, Popconfirm, Splitter, Tabs} from 'antd';
import React from 'react';
import {
    ButtonList,
    DictUtils,
    FieldBoolean,
    FieldSysOrgTreeSelect,
    HttpUtils,
    OrgTree,
    Page,
    ProTable,
    RoleTree
} from "../../../framework";
import UserPerm from "./UserPerm";

export default class extends React.Component {

    state = {
        showAddForm: false,
        showEditForm: false,
        formValues: {},

        currentOrgId: null,
        currentRoleId: null,
    }
    permRef = React.createRef();

    formRef = React.createRef()
    tableRef = React.createRef()

    resetPwd(row) {
        HttpUtils.post('admin/sysUser/resetPwd', {id: row.id}).then(rs => {
            Modal.success({
                title: '重置密码成功',
                content: rs
            })
        })
    }

    handleDelete = r => {
        HttpUtils.get('admin/sysUser/delete', {id: r.id}).then(rs => {
            this.tableRef.current.reload();
        })
    }

    onSelectOrg = (key) => {
        this.setState({currentOrgId: key}, () => this.tableRef.current.reload())
    }

    onSelectRole = (key) => {
        this.setState({currentRoleId: key}, () => this.tableRef.current.reload())
    }

    handleAdd = () => {
        this.setState({formOpen: true, formValues: {}})
    }

    handleEdit = record => {
        record.deptId = record.deptId || record.unitId
        this.setState({formOpen: true, formValues: record})
    }

    columns = [
        {
            title: '单位',
            dataIndex: 'unitLabel',
        },
        {
            title: '部门',
            dataIndex: 'deptLabel',
        },
        {
            title: '姓名',
            dataIndex: 'name',
            sorter: true
        },
        {
            title: '登录账号',
            dataIndex: 'account',
            sorter: true
        },


        {
            title: '手机',
            dataIndex: 'phone'
        },
        {
            title: '邮箱',
            dataIndex: 'email'
        },

        {
            title: '角色',
            dataIndex: 'roleIds',
            render: (_, row) => {
                if (row.roleNames) {
                    return row.roleNames.join(',')
                }
            },
        },
        {
            title: '状态',
            dataIndex: 'enabled',
            render(v) {
                return v == null ? null : (v ? '是' : '否')
            },
        },
        {
            title: '数据权限',
            dataIndex: 'dataPermType',
            render(v) {
                return DictUtils.dictTag('dataPermType', v)
            }
        },

        {
            title: '创建时间',
            dataIndex: 'createTime',
        },
        {
            title: '操作',
            dataIndex: 'option',
            fixed: 'right',
            render: (_, record) => {
                return <ButtonList>
                    <Button size='small' perm='sysUser:save' onClick={() => this.handleEdit(record)}> 编辑 </Button>

                    <Button size='small' perm='sysUser:grantPerm'
                            onClick={() => this.permRef.current.show(record)}> 授权 </Button>

                    <Popconfirm perm='sysUser:resetPwd' title='确认重置密码？' onConfirm={() => this.resetPwd(record)}>
                        <Button size='small'>重置密码</Button>
                    </Popconfirm>

                    <Popconfirm perm='sysUser:delete' title={'是否确定删除用户'}
                                onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                </ButtonList>;
            },
        },
    ];

    onFinish = values => {
        HttpUtils.post('admin/sysUser/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }

    render() {

        return <Page>
            <Splitter>
                <Splitter.Panel defaultSize={400}>
                    <Tabs
                        type='card'
                        size='small'
                        items={[
                            {
                                key: 'org',
                                label: '按组织机构',
                                children: <OrgTree onChange={this.onSelectOrg}/>
                            },
                            {
                                key: 'role',
                                label: '按角色',
                                children: <RoleTree onSelect={this.onSelectRole}/>
                            }
                        ]}/>

                </Splitter.Panel>
                <Splitter.Panel style={{paddingLeft: 16}}>
                    <ProTable
                        actionRef={this.tableRef}
                        toolBarRender={() => {
                            return <ButtonList>
                                <Button
                                    perm='sysUser:save'
                                    type="primary"
                                    onClick={this.handleAdd}>
                                    <PlusOutlined/> 新增
                                </Button>


                            </ButtonList>
                        }}
                        showToolbarSearch
                        request={(params) => {
                            params.orgId = this.state.currentOrgId
                            params.roleId = this.state.currentRoleId
                            return HttpUtils.get('admin/sysUser/page', params)
                        }
                        }
                        columns={this.columns}
                    >

                    </ProTable>
                </Splitter.Panel>
            </Splitter>


            <Modal title='系统用户'
                   open={this.state.formOpen}
                   onOk={() => this.formRef.current.submit()}
                   onCancel={() => this.setState({formOpen: false})}
                   destroyOnHidden
            >

                <Form ref={this.formRef} labelCol={{flex: '100px'}}
                      initialValues={this.state.formValues}
                      onFinish={this.onFinish}>
                    <Form.Item name='id' noStyle></Form.Item>

                    <Form.Item label='所属机构' name='deptId' rules={[{required: true}]}>
                        <FieldSysOrgTreeSelect/>
                    </Form.Item>

                    <Form.Item label='姓名' name='name' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>
                    <Form.Item label='账号' name='account' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>


                    <Form.Item label='电话' name='phone'>
                        <Input/>
                    </Form.Item>
                    <Form.Item label='邮箱' name='email'>
                        <Input/>
                    </Form.Item>

                    <Form.Item label='启用状态' name='enabled' rules={[{required: true}]}>
                        <FieldBoolean/>
                    </Form.Item>

                </Form>
            </Modal>


            <UserPerm ref={this.permRef} onOk={() => this.tableRef.current.reload()}/>

        </Page>
    }


}



