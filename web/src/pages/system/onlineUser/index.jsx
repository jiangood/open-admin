import React from 'react';
import {Popconfirm} from "antd";
import {ButtonList, HttpUtil, ProTable} from "@tmgg/tmgg-base";


export default class extends React.Component {


    columns = [
        {
            title: '账号',
            dataIndex: 'account'
        },
        {
            title: '姓名',
            dataIndex: 'name'
        },
        {
            title: '最后访问时间',
            dataIndex: 'lastAccessedTime'
        },
        {
            title: '失效时间',
            dataIndex: 'expireTime',
        },
        {
            title: '是否失效',
            dataIndex: 'expired',
        },


        {
            title: '最后登录地址',
            dataIndex: 'lastLoginAddress',
        },
        {
            title: '最后登录浏览器',
            dataIndex: 'lastLoginBrowser',
        },
        {
            title: '最后登录所用系统',
            dataIndex: 'lastLoginOs'
        },
        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <ButtonList>
                    <Popconfirm perm='sysOnlineUser:forceExist' title='是否强制下线该用户？'
                                onConfirm={() => this.forceExist(record)}>
                        <a>强制下线</a>
                    </Popconfirm>
                </ButtonList>
            ),
        },
    ];

    actionRef = React.createRef();

    forceExist = record => {
        HttpUtil.get('sysOnlineUser/forceExist', {sessionId: record.sessionId}).then(r => {
            this.actionRef.current.reload()
        })
    }


    render() {
        return <ProTable
            actionRef={this.actionRef}
            request={(params, sort, filter) => HttpUtil.pageData('sysOnlineUser/page', params, sort)}
            columns={this.columns}
            rowKey="sessionId"
            search={false}
        />
    }


}



