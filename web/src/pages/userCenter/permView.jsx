import React from "react";
import {Empty, Table, Tag} from "antd";
import {HttpClient, Gap} from "../../framework";

export default class PermView extends React.Component {

    state = {
        data: null,
    }

    componentDidMount() {
        HttpClient.get('admin/userCenter/perms', null, rs => {
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
                render: (status) => { // NOSONAR: 返回元素或占位文本，类型不同属预期
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
                render: (status) => { // NOSONAR: 返回元素或占位文本，类型不同属预期
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
            {data.orgRows?.length
                ? <Table size='small' rowKey='key' columns={orgColumns} dataSource={data.orgRows}
                         pagination={false} expandable={{defaultExpandAllRows: true}}/>
                : <Empty description='暂无机构数据'/>}
            <Gap/>
            {data.menuRows?.length
                ? <Table size='small' rowKey='key' columns={menuColumns} dataSource={data.menuRows}
                         pagination={false} expandable={{defaultExpandAllRows: true}}/>
                : <Empty description='暂无权限数据'/>}
        </div>;
    }
}
