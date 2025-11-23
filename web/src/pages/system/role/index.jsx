import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Input, InputNumber, Modal, Popconfirm, Transfer, Tree} from 'antd'
import React from 'react'
import {ButtonList, Ellipsis, FieldBoolean, HttpUtil, Page, PageUtil, ProTable} from "../../../framework";


export default class extends React.Component {

    state = {
        formValues: {},
        formOpen: false,

        usersModalOpen: false,


        userList: [],
        targetKeys: [],
        selectedKeys: [],

        menuOpen:false,
        menuTree:[],
        menuTreeLoading:false,
        menuChecked:[],
        menuHalfChecked:[]
    }

    formRef = React.createRef()
    tableRef = React.createRef()

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
            title: '是否内置',
            dataIndex: 'builtin',


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
                    return <Ellipsis>{v.join(',')}</Ellipsis>
                }
            }

        },


        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => {

                return (
                    <ButtonList>
                        <Button size='small' perm='sysRole:save'
                                onClick={() => this.handleEditUser(record)}>用户设置</Button>

                        <Button size='small' perm='sysRole:save' disabled={record.builtin}
                                onClick={() => PageUtil.open('/system/role/perm?id='+record.id, '角色权限设置')}>权限设置</Button>

                        <Button size='small' perm='sysRole:save' disabled={record.builtin}
                                onClick={() => this.handleEdit(record)}>编辑</Button>
                        <Popconfirm perm='sysRole:delete' disabled={record.builtin} title='是否确定删除系统角色'
                                    onConfirm={() => this.handleDelete(record)}>
                            <Button size='small'>删除</Button>
                        </Popconfirm>
                    </ButtonList>
                );
            },
        },
    ]

    handleAdd = () => {
        this.setState({formOpen: true, formValues: {}})
    }

    handleEdit = record => {
        this.setState({formOpen: true, formValues: record}, () => {

        })
    }


    handleEditUser = record => {
        this.setState({usersModalOpen: true, formValues: record})
        HttpUtil.get('admin/sysRole/userList', {id: record.id}).then(rs => {
            this.setState({userList: rs.list, targetKeys: rs.selectedKeys})
        })
    }



    onFinish = values => {
        HttpUtil.post('admin/sysRole/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }


    handleDelete = record => {
        HttpUtil.get('admin/sysRole/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    handleSaveUsers =()=>{
        const params = {
            id: this.state.formValues.id,
            userIdList:this.state.targetKeys
        }
        HttpUtil.post('admin/sysRole/grantUsers', params).then(rs => {
            this.setState({usersModalOpen:false})
        })
    }

    handleEditMenu =(record)=>{
        this.setState({menuOpen:true,formValues:record,menuTreeLoading:true})
        HttpUtil.get('admin/sysRole/ownMenu', {id: record.id}).then(rs => {
            this.setState({menuChecked:rs.checked,menuHalfChecked:rs.halfChecked})
        })
        HttpUtil.get('admin/sysRole/menuTree').then(rs => {
            this.setState({menuTree:rs, menuTreeLoading:false})
        })
    }
    handleGrantMenu =()=>{
        const params = {
            id: this.state.formValues.id,
            menuIds:[...this.state.menuChecked, ...this.state.menuHalfChecked]
        }
        HttpUtil.post('admin/sysRole/grantMenu', params).then(rs => {
            debugger
            this.setState({menuOpen:false})
        })
    }

    render() {
        return <Page>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={() => {
                    return <ButtonList>
                        <Button perm='sysRole:save' type='primary' onClick={this.handleAdd}>
                            <PlusOutlined/> 新增
                        </Button>
                    </ButtonList>
                }}
                request={(params) => HttpUtil.pageData('admin/sysRole/page', params)}
                columns={this.columns}

            />

            <Modal title='系统角色'
                   open={this.state.formOpen}
                   onOk={() => this.formRef.current.submit()}
                   onCancel={() => this.setState({formOpen: false})}
                   destroyOnHidden
                   maskClosable={false}
                   width={600}
            >

                <Form ref={this.formRef} labelCol={{flex: '100px'}}
                      initialValues={this.state.formValues}
                      onFinish={this.onFinish}
                >
                    <Form.Item name='id' noStyle></Form.Item>


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




                </Form>
            </Modal>


            <Modal title={'角色用户' +"【" +this.state.formValues?.name + '】'}
                   open={this.state.usersModalOpen }
                   destroyOnHidden
                   maskClosable={false}
                   width={800}
                   onCancel={() => this.setState({usersModalOpen: false})}
                   onOk={this.handleSaveUsers}
            >


                <Transfer
                    listStyle={{height:'60vh', width:300}}

                    dataSource={this.state.userList} titles={["未选择", "已选择"]}
                    targetKeys={this.state.targetKeys}
                    selectedKeys={this.state.selectedKeys}
                    render={item => item.title}
                    onChange={(nextTargetKeys, direction, moveKeys) => {
                        this.setState({
                            targetKeys: nextTargetKeys
                        })
                    }}
                    onSelectChange={(sourceSelectedKeys, targetSelectedKeys)=>{
                        this.setState({
                            selectedKeys: [...sourceSelectedKeys, ...targetSelectedKeys]
                        })
                    }}
                    showSearch
                />


            </Modal>

            <Modal title={'角色授权菜单权限' +"【" +this.state.formValues?.name + '】'}
                   open={this.state.menuOpen }
                   destroyOnHidden
                   maskClosable={false}
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
                        checkedKeys={{checked:this.state.menuChecked}}
                        onCheck={(keys,e) =>{
                            this.setState({menuChecked:keys,menuHalfChecked:e.halfCheckedKeys})
                        }}
                        defaultExpandAll
                        titleRender={node=>{
                            return <span title={node.perm} >{node.title}</span>
                        }}
                    >
                    </Tree>



            </Modal>
        </Page>


    }
}

