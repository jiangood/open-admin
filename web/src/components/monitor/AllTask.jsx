import React from "react";
import {ProTable} from "@tmgg/tmgg-base"
import {HttpUtil} from "@tmgg/tmgg-base";

export default class extends React.Component {

  columns = [
    {
      dataIndex: 'id',
      title:'任务标识',
    },
    {
      dataIndex: 'name',
      title:'名称',
    },
    {
      dataIndex: 'taskDefinitionKey',
      title:'任务定义标识',
    },
    {
      dataIndex: 'processDefinitionId',
      title: '流程定义标识'
    },
    {
      dataIndex: 'processInstanceId',
      title: '流程实例标识'
    },
    {
      dataIndex: 'activityId',
      title: 'activityId'
    },
    {
      dataIndex: 'activityType',
      title:'activityType'
    },


    {
      dataIndex: 'assignee',
      title: '指定处理人标识'
    },

    {
      dataIndex: 'executionId',
      title: 'executionId'
    },



    {
      dataIndex: 'startTime',
      title: '开始时间'
    },
    {
      dataIndex: 'endTime',
      title: '结束时间'
    },
    {
      dataIndex: 'tenantId',
      title: '租户标识'
    },


  ]

  render() {
    return <ProTable    search={false} columns={this.columns}
                     request={(params, sort, filter) => HttpUtil.pageData('flowable/monitor/task', params, sort)}
                     rowKey='id'
                     scroll={{
                       x:'max-content'
                     }}

    >

    </ProTable>
  }
}
