import React from "react";
import {ProTable} from "@tmgg/tmgg-base"
import {Popconfirm} from "antd";
import {HttpUtil} from "@tmgg/tmgg-base";

export default class extends React.Component {

  columns = [
    {
      dataIndex: 'id',
      title: 'id',
    },
    {
      dataIndex: 'processDefinitionName',
      title: '流程定义名称'
    },
    {
      dataIndex: 'businessKey',
      title: '业务标识'
    },
    {
      dataIndex: 'businessStatus',
      title: '业务状态'
    },


    {
      dataIndex: 'processDefinitionVersion',
      title: '流程定义版本'
    },

    {
      dataIndex: 'startTime',
      title: '开始时间'
    },
    {
      dataIndex: 'startUserId',
      title: '发起人ID'
    },

    {
      dataIndex: 'tenantId',
      title: '租户ID'
    },

    {
      dataIndex: 'options',
      title: '操作',
      render: (_, r) => {
        return <Popconfirm title={'关闭流程'} onConfirm={() => this.close(r.id)}> <a>关闭流程</a></Popconfirm>
      }
    }

  ]

  close = (id) => {
    HttpUtil.get('/flowable/monitor/processInstance/close', {id}).then((rs) => {
      this.tableRef.current.reload()
    })
  }

  tableRef = React.createRef()

  render() {
    return <ProTable    search={false}
      actionRef={this.tableRef}
      columns={this.columns}
      request={(params, sort, filter) => HttpUtil.pageData('flowable/monitor/processInstance', params, sort)}
      rowKey='id'
      scroll={{
        x: 'max-content'
      }}
    >

    </ProTable>
  }
}
