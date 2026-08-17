import {PlusOutlined} from '@ant-design/icons';
import {Button, Card, Form, Input, Modal, Select, Splitter, Typography} from 'antd';
import React from 'react';
import {
    PermActions,
    DictUtils,
    FieldBoolean,
    FieldRemoteSelect,
    FieldSysOrgTreeSelect,
    FormModal,
    HttpClient,
    OrgTree,
    Page,
    ProTable,
    ViewText,
} from "../../../framework";
import UserPerm from "./userPerm";

export default class UserPage extends React.Component {

    state = {
        currentOrgId: null,
        addResultModal: { open: false, name: '', account: '', password: '' },
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
        await HttpClient.post('admin/sysUser/reset-pwd', {id: this.state.resetPwdUser.id, password: values.password})
        this.setState({resetPwdUser: null})
        this.tableRef.current?.reload()
    }

    handleDelete = r => {
        HttpClient.post('admin/sysUser/delete', {id: r.id}, null).then(() => {
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
                if (v == null) return null;
                return v ? '是' : '否';
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
        const result = await HttpClient.post(url, values);
        if (result?.data?.password) {
            this.setState({
                addResultModal: {
                    open: true,
                    name: values.name,
                    account: values.account,
                    password: result.data.password,
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
                        toolBarRender={() => ( // NOSONAR: AntD 渲染函数惯例
                            <PermActions>
                                <Button perm='sys-user:create' type='primary' icon={<PlusOutlined/>} onClick={this.handleAdd}>新增</Button>
                            </PermActions>
                        )}
                        request={(params) => {
                            params.orgId = this.state.currentOrgId
                            return HttpClient.get('admin/sysUser/page', params)
                        }}
                        columns={this.columns}
                        searchFormRender={() => ( // NOSONAR: AntD 渲染函数惯例
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
                onOk={() => this.setState({addResultModal: {open: false, name: '', account: '', password: ''}})}
                onCancel={() => this.setState({addResultModal: {open: false, name: '', account: '', password: ''}})}
                width={420}
                footer={
                    <Button type="primary" onClick={() => this.setState({addResultModal: {open: false, name: '', account: '', password: ''}})}>确定</Button>
                }
            >
                <Typography.Paragraph
                    copyable={{
                        text: `系统访问地址：${window.location.origin}\n姓名：${this.state.addResultModal.name}\n账号：${this.state.addResultModal.account}\n密码：${this.state.addResultModal.password}`,
                        tooltips: '点击复制',
                    }}
                    style={{marginBottom: 0}}
                >
                    <div>系统访问地址：{window.location.origin}</div>
                    <div>姓名：{this.state.addResultModal.name}</div>
                    <div>账号：{this.state.addResultModal.account}</div>
                    <div>密码：{this.state.addResultModal.password}</div>
                </Typography.Paragraph>
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



