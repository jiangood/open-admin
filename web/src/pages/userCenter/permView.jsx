import React from "react";
import {Card, Empty, Table, Tag} from "antd";
import {HttpUtils} from "../../framework";

const DATA_PERM_LABEL = {
    ALL: '所有',
    LEVEL: '本级',
    CHILDREN: '本级和子级',
    CUSTOM: '自定义',
};

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
        const orgRows = (nodes) => (nodes || []).map(node => {
            const children = node.children && node.children.length ? orgRows(node.children) : undefined;
            let status;
            if (node.key === data.unitId || node.key === data.orgId) {
                status = 'mine';
            } else if (data.orgPermIds && data.orgPermIds.includes(node.key)) {
                status = 'owned';
            }
            return {key: node.key, title: node.title, status, children};
        });

        const menuColumns = [
            {
                title: '菜单名称', dataIndex: 'title', key: 'title',
            },
            {
                title: '权限名称', dataIndex: 'perms', key: 'permNames',
                render: (perms) => (perms || []).map(p => p.name).join('、') || '-',
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
        const menuRows = (nodes) => (nodes || []).map(node => {
            const leaves = (node.children || []).filter(c => c.isLeaf);
            const subMenus = (node.children || []).filter(c => !c.isLeaf);
            const perms = leaves.map(l => ({
                name: l.title,
                code: l.key,
                owned: data.ownedPerms && data.ownedPerms.includes(l.key),
            }));
            const ownedCount = perms.filter(p => p.owned).length;
            let status;
            if (perms.length === 0) {
                status = null;
            } else if (ownedCount === perms.length) {
                status = 'all';
            } else if (ownedCount > 0) {
                status = 'partial';
            }
            return {
                key: node.key,
                title: node.title,
                perms,
                status,
                children: subMenus.length ? menuRows(subMenus) : undefined,
            };
        });

        return <div>
            <div style={{marginBottom: 16}}>
                <span>数据权限：
                    <Tag color='processing'>{DATA_PERM_LABEL[data.dataPermType] || data.dataPermType || '-'}</Tag>
                </span>
            </div>
            <Card title='机构权限' size='small' style={{marginBottom: 16}}>
                {data.orgTree && data.orgTree.length
                    ? <Table size='small' rowKey='key' columns={orgColumns} dataSource={orgRows(data.orgTree)}
                             pagination={false} expandable={{defaultExpandAllRows: true}}/>
                    : <Empty description='暂无机构数据'/>}
            </Card>
            <Card title='菜单权限' size='small'>
                {data.menuTree && data.menuTree.length
                    ? <Table size='small' rowKey='key' columns={menuColumns} dataSource={menuRows(data.menuTree)}
                             pagination={false} expandable={{defaultExpandAllRows: true}}/>
                    : <Empty description='暂无权限数据'/>}
            </Card>
        </div>;
    }
}
