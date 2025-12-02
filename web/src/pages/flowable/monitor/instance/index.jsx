import {Button, Popconfirm, Space} from "antd";
import {HttpUtils, PageUtils, ProTable} from "../../../../framework";
import React from "react";

export default class extends React.Component {

    columns = [
        {
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
        },
        {
            title: '流程定义ID',
            dataIndex: 'processDefinitionId',
            key: 'processDefinitionId',
        },
        {
            title: '流程定义名称',
            dataIndex: 'processDefinitionName',
            key: 'processDefinitionName',
        },
        {
            title: '流程定义键',
            dataIndex: 'processDefinitionKey',
            key: 'processDefinitionKey',
        },
        {
            title: '流程定义版本',
            dataIndex: 'processDefinitionVersion',
            key: 'processDefinitionVersion',
        },
        {
            title: '流程定义分类',
            dataIndex: 'processDefinitionCategory',
            key: 'processDefinitionCategory',
        },
        {
            title: '部署ID',
            dataIndex: 'deploymentId',
            key: 'deploymentId',
        },
        {
            title: '业务键',
            dataIndex: 'businessKey',
            key: 'businessKey',
        },
        {
            title: '业务状态',
            dataIndex: 'businessStatus',
            key: 'businessStatus',
        },
        {
            title: '是否挂起',
            dataIndex: 'suspended',
            key: 'suspended',
            render: (value) => value ? '是' : '否',
        },
        {
            title: '流程变量',
            dataIndex: 'processVariables',
            key: 'processVariables',
        },
        {
            title: '租户ID',
            dataIndex: 'tenantId',
            key: 'tenantId',
        },
        {
            title: '名称',
            dataIndex: 'name',
            key: 'name',
        },
        {
            title: '描述',
            dataIndex: 'description',
            key: 'description',
        },
        {
            title: '本地化名称',
            dataIndex: 'localizedName',
            key: 'localizedName',
        },
        {
            title: '本地化描述',
            dataIndex: 'localizedDescription',
            key: 'localizedDescription',
        },
        {
            title: '开始时间',
            dataIndex: 'startTime',
            key: 'startTime',
        },
        {
            title: '启动用户ID',
            dataIndex: 'startUserId',
            key: 'startUserId',
        },
        {
            title: '回调ID',
            dataIndex: 'callbackId',
            key: 'callbackId',
        },
        {
            title: '回调类型',
            dataIndex: 'callbackType',
            key: 'callbackType',
        },
        {
            title: '父ID',
            dataIndex: 'parentId',
            key: 'parentId',
        },
        {
            title: '根流程实例ID',
            dataIndex: 'rootProcessInstanceId',
            key: 'rootProcessInstanceId',
        },
        {
            title: '活动ID',
            dataIndex: 'activityId',
            key: 'activityId',
        },
        {
            title: '当前活动ID',
            dataIndex: 'currentActivityId',
            key: 'currentActivityId',
        },
        {
            title: '当前活动名称',
            dataIndex: 'currentActivityName',
            key: 'currentActivityName',
        },
        {
            title: '当前活动类型',
            dataIndex: 'currentActivityType',
            key: 'currentActivityType',
        },
        {
            title: '当前活动行为类型',
            dataIndex: 'currentActivityBehaviorType',
            key: 'currentActivityBehaviorType',
        },
        {
            dataIndex: 'options',
            title: '操作',
            fixed: 'right',
            render: (_, r) => {
                return <Space>
                    <Button size='small' onClick={() => PageUtils.open(`/flowable/monitor/instance/view?id=${r.id}`, '查看流程')}>查看</Button>
                    <Popconfirm title={'关闭流程'}
                                onConfirm={() => this.close(r.id)}>
                        <Button size='small' >终止</Button>
                    </Popconfirm></Space>
            }
        }

    ]

    close = (id) => {
        HttpUtils.get('admin/flowable/monitor/processInstance/close', {id}).then((rs) => {
            this.tableRef.current.reload()
        })
    }

    tableRef = React.createRef()

    render() {
        return <ProTable
            actionRef={this.tableRef}
            columns={this.columns}
            request={(params) => HttpUtils.get('admin/flowable/monitor/instancePage', params)}
        >

        </ProTable>
    }
}
