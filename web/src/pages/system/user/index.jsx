import {PlusOutlined} from '@ant-design/icons';
import {Button, Card, Form, Input, Modal, Popconfirm, Select, Splitter} from 'antd';
import React from 'react';
import {
    ButtonList,
    DictUtils,
    FieldBoolean,
    FieldRemoteSelect,
    FieldSysOrgTreeSelect,
    HttpUtils,
    OrgTree,
    Page,
    ProTable,
} from "../../../framework";
import UserPerm from "./UserPerm";

export default class extends React.Component {

    state = {
        showAddForm: false,
        showEditForm: false,
        formValues: {},

        currentOrgId: null,
    }
    permRef = React.createRef();

    formRef = React.createRef()
    tableRef = React.createRef()

    resetPwd(row) {
        HttpUtils.post('admin/sysUser/reset-pwd', {id: row.id}).then(rs => {
            Modal.success({
                title: '重置密码成功',
                content: rs
            })
        })
    }

    handleDelete = r => {
        HttpUtils.post('admin/sysUser/delete', {id: r.id}).then(rs => {
            this.tableRef.current.reload();
        })
    }

    onSelectOrg = (key) => {
        this.setState({currentOrgId: key}, () => this.tableRef.current.reload())
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
                    <Button size='small' perm='sys-user:update' onClick={() => this.handleEdit(record)}> 编辑 </Button>

                    <Button size='small' perm='sys-user:grant-permission'
                            onClick={() => this.permRef.current.show(record)}> 授权 </Button>

                    <Popconfirm perm='sys-user:reset-password' title='确认重置密码？' onConfirm={() => this.resetPwd(record)}>
                        <Button size='small'>重置密码</Button>
                    </Popconfirm>

                    <Popconfirm perm='sys-user:delete' title={'是否确定删除用户'}
                                onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                </ButtonList>;
            },
        },
    ];

    onFinish = values => {
        const isNew = !values.id;
        const url = isNew ? 'admin/sysUser/create' : 'admin/sysUser/update';
        HttpUtils.post(url, values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }

    render() {

        return <Page title="用户管理" description="管理系统用户" actions={<Button perm='sys-user:create' type="primary" icon={<PlusOutlined/>} onClick={this.handleAdd}>新增</Button>}>
            <Splitter>
                <Splitter.Panel defaultSize={350} style={{paddingRight: 8}}>
                    <Card size='small'>
                        <OrgTree onChange={this.onSelectOrg}/>
                    </Card>
                </Splitter.Panel>
                <Splitter.Panel style={{paddingLeft: 8}}>
                    <ProTable
                        searchColumns={3}
                        actionRef={this.tableRef}
                        request={(params) => {
                            params.orgId = this.state.currentOrgId
                            return HttpUtils.get('admin/sysUser/page', params)
                        }}
                        columns={this.columns}
                    >
                        <Form.Item label='姓名' name='name'>
                            <Input/>
                        </Form.Item>
                        <Form.Item label='登录账号' name='account'>
                            <Input/>
                        </Form.Item>
                        <Form.Item label='手机号' name='phone'>
                            <Input/>
                        </Form.Item>
                        <Form.Item label='角色' name='roleId'>
                            <FieldRemoteSelect url='admin/sysRole/options' placeholder='请选择角色'/>
                        </Form.Item>
                        <Form.Item label='状态' name='enabled'>
                            <Select allowClear placeholder='全部'>
                                <Select.Option value={true}>启用</Select.Option>
                                <Select.Option value={false}>停用</Select.Option>
                            </Select>
                        </Form.Item>
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



