import React from "react";
import {ProTable} from "@tmgg/tmgg-base"
import {HttpUtil} from "@tmgg/tmgg-base";

export default class extends React.Component {

  columns = [
    {
      dataIndex: 'id',
      title:'流程定义标识',
    },
    {
      dataIndex: 'key',
      title: '流程定义键（key）'
    },
    {
      dataIndex: 'name',
      title:'名称'
    },

    {
      dataIndex: 'deploymentId',
      title: '部署标识'
    },
    {
      dataIndex: 'version',
      title: '版本'
    },
    {
      dataIndex: 'tenantId',
      title: '租户标识'
    },


  ]

  render() {
    return <ProTable
      search={false}
      columns={this.columns}
                     request={(params, sort, filter) => HttpUtil.pageData('flowable/monitor/processDefinition', params, sort)}
                     rowKey='id'
    >

    </ProTable>
  }
}
