import React from "react";
import {Tag} from "antd";
import {HttpClient, ProTable} from "../../framework";

const columns = [
    {
        title: '时间', dataIndex: 'operationTime', sorter: true, width: 180,
    },
    {
        title: '操作', dataIndex: 'operation',
    },
    {
        title: '结果', dataIndex: 'success', width: 90,
        render: (success) => <Tag color={success ? 'green' : 'red'}>{success ? '成功' : '失败'}</Tag>
    },
    {
        title: '耗时', dataIndex: 'duration', width: 100,
        render: (duration) => duration == null ? '-' : `${duration} ms`
    },
    {
        title: 'IP', dataIndex: 'ip', width: 150,
    },
];

/**
 * 个人中心"最近操作"：仅展示当前登录用户的操作记录（后端按登录用户过滤）。
 */
export default function RecentOps() {
    return <ProTable
        rowKey='id'
        columns={columns}
        defaultPageSize={10}
        emptyText='暂无操作记录（仅记录系统关键操作）'
        request={(params) => HttpClient.get('admin/user-center/logs', params)}
    />
}
