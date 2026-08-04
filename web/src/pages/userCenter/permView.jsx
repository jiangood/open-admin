import React from "react";
import {Empty, Table, Tag, Typography} from "antd";
import {HttpUtils} from "../../framework";

export default class PermView extends React.Component {

    state = {
        data: null,
    }

    componentDidMount() {
        HttpUtils.get('admin/userCenter/perms').then(rs => {
            this.setState({data: rs})
        })
    }

    render() {
        const {data} = this.state;
        if (!data) {
            return <Empty description='加载中...'/>;
        }

        const orgColumns = [
            {
                title: '机构名称', dataIndex: 'title', key: 'title',
            },
            {
                title: '状态', dataIndex: 'status', key: 'status', width: 140,
                render: (status) => {
                    if (status === 'mine') {
                        return <Tag color='blue'>我的机构</Tag>;
                    }
                    if (status === 'owned') {
                        return <Tag color='green'>已授权</Tag>;
                    }
                    return '-';
                }
            },
        ];

        const menuColumns = [
            {
                title: '菜单名称', dataIndex: 'title', key: 'title',
            },
            {
                title: '权限名称', dataIndex: 'perms', key: 'perms',
                render: (perms) => (perms || []).join('、') || '-',
            },
            {
                title: '状态', dataIndex: 'status', key: 'status', width: 140,
                render: (status) => {
                    if (status === 'all') {
                        return <Tag color='green'>已授权</Tag>;
                    }
                    if (status === 'partial') {
                        return <Tag color='orange'>部分授权</Tag>;
                    }
                    return '-';
                }
            },
        ];

        return <div>
            <div style={{marginBottom: 16}}>
                <span>数据权限：
                    <Tag color='processing'>{data.dataPermLabel || '-'}</Tag>
                </span>
            </div>
            <Typography.Title level={5}>机构权限</Typography.Title>
            {data.orgRows && data.orgRows.length
                ? <Table size='small' rowKey='key' columns={orgColumns} dataSource={data.orgRows}
                         pagination={false} expandable={{defaultExpandAllRows: true}}/>
                : <Empty description='暂无机构数据'/>}
            <Typography.Title level={5} style={{marginTop: 24}}>菜单权限</Typography.Title>
            {data.menuRows && data.menuRows.length
                ? <Table size='small' rowKey='key' columns={menuColumns} dataSource={data.menuRows}
                         pagination={false} expandable={{defaultExpandAllRows: true}}/>
                : <Empty description='暂无权限数据'/>}
        </div>;
    }
}
