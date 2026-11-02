import React from 'react';
import {Form, Input, Tag} from 'antd';
import {HttpClient, Page, PermActions, ProTable} from "../../../framework";

/**
 * 在线用户：一个登录会话一行，支持强制下线。
 * 数据来自内存中的 HttpSession 元数据，仅单实例部署准确。
 */
export default class OnlineUserPage extends React.Component {

    tableRef = React.createRef();

    handleKick = row => {
        HttpClient.post('admin/sys-online-user/kick', {id: row.id}).then(() => {
            this.tableRef.current.reload();
        });
    };

    columns = [
        {
            title: '登录账号',
            dataIndex: 'username',
            width: 140,
        },
        {
            title: '姓名',
            dataIndex: 'name',
            width: 120,
        },
        {
            title: '所属机构',
            dataIndex: 'unitName',
            width: 180,
            render: (_, record) => {
                // 单位与机构可能指向同一节点（如 admin 挂在根单位），去重后避免「默认单位 / 默认单位」
                const names = [...new Set([record.unitName, record.orgName].filter(Boolean))];
                return names.length ? names.join(' / ') : '-';
            },
        },
        {
            title: 'IP',
            dataIndex: 'ip',
            width: 140,
        },
        {
            title: '登录时间',
            dataIndex: 'loginTime',
            width: 170,
            render: v => v || '-',
        },
        {
            title: '最后活跃',
            dataIndex: 'lastActiveTime',
            width: 170,
            render: v => v || '-',
        },
        {
            title: '操作',
            dataIndex: 'option',
            width: 130,
            fixed: 'right',
            render: (_, record) => {
                if (record.current) {
                    return <Tag color="green">当前会话</Tag>;
                }
                return <PermActions
                    size="small"
                    actions={[{
                        label: '强制下线',
                        perm: 'sys-online:kick',
                        danger: true,
                        confirm: `确定强制下线「${record.name || record.username}」吗？`,
                        onClick: () => this.handleKick(record),
                    }]}
                />;
            },
        },
    ];

    render() {
        return <Page>
            <ProTable
                actionRef={this.tableRef}
                request={params => HttpClient.get('admin/sys-online-user/page', params)}
                columns={this.columns}
                searchFormRender={() => (
                    <Form.Item label='账号/姓名/IP' name='keyword'>
                        <Input placeholder='支持账号、姓名、IP 模糊搜索' allowClear/>
                    </Form.Item>
                )}
            />
        </Page>;
    }

}
