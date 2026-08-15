import {PlusOutlined} from '@ant-design/icons';
import {Button, Card, Form, Input, Modal, Select, Splitter, message} from 'antd';
import React from 'react';
import {
    PermActions,
    DictUtils,
    FieldBoolean,
    FieldRemoteSelect,
    FieldSysOrgTreeSelect,
    FormModal,
    HttpUtils,
    OrgTree,
    Page,
    ProTable,
    ViewText,
} from "../../../framework";
import UserPerm from "./userPerm";

export default class extends React.Component {

    state = {
        currentOrgId: null,
        addResultModal: { open: false, account: '', password: '' },
        resetPwdUser: null,
    }
    permRef = React.createRef();

    modalRef = React.createRef()
    tableRef = React.createRef()
    resetPwdRef = React.createRef()

    resetPwd(row) {
        this.setState({resetPwdUser: row}, () => {
            this.resetPwdRef.current.open()
        })
    }

    onFinishResetPwd = async values => {
        await HttpUtils.post('admin/sysUser/reset-pwd', {id: this.state.resetPwdUser.id, password: values.password})
        message.success('重置密码成功')
        this.setState({resetPwdUser: null})
        this.tableRef.current?.reload()
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
        this.modalRef.current.open({})
    }

    handleEdit = record => {
        this.modalRef.current.open({...record, orgId: record.orgId || record.unitId})
    }

    columns = [
        {
            title: '单位',
            dataIndex: 'unitLabel',
        },
        {
            title: '机构',
            dataIndex: 'orgLabel',
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
                return <PermActions
                    more
                    size="small"
                    actions={[
                        {label: '编辑', perm: 'sys-user:update', onClick: () => this.handleEdit(record)},
                        {label: '授权', perm: 'sys-user:grant-permission', onClick: () => this.permRef.current.show(record)},
                        {label: '重置密码', perm: 'sys-user:reset-password', onClick: () => this.resetPwd(record)},
                        {label: '删除', perm: 'sys-user:delete', confirm: '是否确定删除用户', onClick: () => this.handleDelete(record)},
                    ]}
                />;
            },
        },
    ];

    onFinish = async values => {
        const isNew = !values.id;
        const url = isNew ? 'admin/sysUser/create' : 'admin/sysUser/update';
        const result = await HttpUtils.post(url, values)
        if (result && result.password) {
            this.setState({
                addResultModal: {
                    open: true,
                    account: values.account,
                    password: result.password,
                }
            })
        }
        this.tableRef.current.reload()
    }

    render() {

        return <Page title="用户管理" description="管理系统用户">
            <Splitter>
                <Splitter.Panel defaultSize={240} style={{paddingRight: 8}}>
                    <Card size='small'>
                        <OrgTree onChange={this.onSelectOrg}/>
                    </Card>
                </Splitter.Panel>
                <Splitter.Panel style={{paddingLeft: 8}}>
                    <ProTable
                        searchFormCols={3}
                        actionRef={this.tableRef}
                        toolBarRender={() => (
                            <PermActions>
                                <Button perm='sys-user:create' type='primary' icon={<PlusOutlined/>} onClick={this.handleAdd}>新增</Button>
                            </PermActions>
                        )}
                        request={(params) => {
                            params.orgId = this.state.currentOrgId
                            return HttpUtils.get('admin/sysUser/page', params)
                        }}
                        columns={this.columns}
                        searchFormRender={() => (
                            <>
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
                                    <Select allowClear placeholder='全部' options={[{value: true, label: '启用'}, {value: false, label: '停用'}]}/>
                                </Form.Item>
                            </>
                        )}
                    />
                </Splitter.Panel>
            </Splitter>


            <FormModal ref={this.modalRef} title='系统用户' onFinish={this.onFinish}>

                <Form.Item label='所属机构' name='orgId' rules={[{required: true}]}>
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



            </FormModal>


            <Modal
                title="添加用户成功"
                open={this.state.addResultModal.open}
                onOk={() => this.setState({addResultModal: {open: false, account: '', password: ''}})}
                onCancel={() => this.setState({addResultModal: {open: false, account: '', password: ''}})}
                width={420}
                footer={
                    <div style={{display: 'flex', justifyContent: 'flex-end', gap: 8}}>
                        <Button onClick={() => {
                            navigator.clipboard.writeText(`账号：${this.state.addResultModal.account}\n密码：${this.state.addResultModal.password}`);
                            message.success('复制成功')
                        }}>复制</Button>
                        <Button type="primary" onClick={() => this.setState({addResultModal: {open: false, account: '', password: ''}})}>确定</Button>
                    </div>
                }
            >
                <div>
                    <div style={{marginBottom: 8}}>
                        账号：{this.state.addResultModal.account}
                    </div>
                    <div>
                        密码：{this.state.addResultModal.password}
                    </div>
                </div>
            </Modal>

            <FormModal ref={this.resetPwdRef} title='重置密码' onFinish={this.onFinishResetPwd} width={420}>
                {this.state.resetPwdUser && (
                    <>
                        <Form.Item label="账号">
                            <ViewText value={this.state.resetPwdUser.account}/>
                        </Form.Item>
                        <Form.Item label="姓名">
                            <ViewText value={this.state.resetPwdUser.name}/>
                        </Form.Item>
                    </>
                )}
                <Form.Item label="新密码" name="password" rules={[
                    {required: true, message: '请输入新密码'},
                    {pattern: /^[\x20-\x7E]{1,64}$/, message: '密码仅支持英文、数字与常见符号，长度不超过64位'}
                ]}>
                    <Input.Password maxLength={64} placeholder="请输入新密码"/>
                </Form.Item>
            </FormModal>

            <UserPerm ref={this.permRef} onOk={() => this.tableRef.current.reload()}/>

        </Page>
    }


}



