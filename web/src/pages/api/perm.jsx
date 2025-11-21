import {PlusOutlined} from '@ant-design/icons'
import {Button, Form, Modal, Popconfirm} from 'antd'
import React from 'react'
import {
    ButtonList,
    FieldRadioBoolean,
    FieldTableSelect,
    HttpUtil,
    Page,
    PageUtil,
    ProTable,
    ViewBoolean
} from "../../framework";


export default class extends React.Component {

    state = {
        formValues: {},
        formOpen: false
    }

    formRef = React.createRef()
    tableRef = React.createRef()

    constructor(props) {
        super(props);
        this.accountId = PageUtil.currentParams().accountId
    }

    columns = [

        {
            title: '账户',
            dataIndex: ['account', 'name'],

        },

        {
            title: '接口名称',
            dataIndex: ['resource', 'name'],
        },
        {
            title: '接口动作',
            dataIndex: ['resource', 'action'],
        },
        {
            title: '描述',
            dataIndex: ['resource', 'desc'],
        },
        {
            title: '启用',
            dataIndex: 'enable',
            render(v) {
                return <ViewBoolean value={v}/>
            },
        },

        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Button size='small' perm='apiAccountResource:save'
                            onClick={() => this.handleEdit(record)}>编辑</Button>
                    <Popconfirm perm='apiAccountResource:delete' title='是否确定删除访客权限'
                                onConfirm={() => this.handleDelete(record)}>
                        <Button size='small'>删除</Button>
                    </Popconfirm>
                </ButtonList>
            ),
        },
    ]

    handleAdd = () => {
        this.setState({formOpen: true, formValues: {}})
    }

    handleEdit = record => {
        this.setState({formOpen: true, formValues: record})
    }


    onFinish = values => {
        values.account = {id: this.accountId}
        HttpUtil.post('admin/apiAccountResource/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload()
        })
    }


    handleDelete = record => {
        HttpUtil.get('admin/apiAccountResource/delete', {id: record.id}).then(rs => {
            this.tableRef.current.reload()
        })
    }

    render() {
        return <Page>




        </Page>


    }
}


