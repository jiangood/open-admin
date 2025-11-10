import {Button} from 'antd'
import React from 'react'
import {HttpUtil, PageUtil, ProTable, SysUtil} from "@tmgg/tmgg-base";


export default class extends React.Component {

  state = {
    formValues: {},
    formOpen: false,

    selectedRowKeys: [],

  }

  tableRef = React.createRef()

  columns = [
    {
      title: '名称',
      dataIndex: ['sysJob','name'],
    },
    {
      title: '执行类',
      dataIndex: ['sysJob','jobClass'],
    },

    {
      title: '开始时间',
      dataIndex: 'beginTime',
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
    },
    {
      title: '耗时',
      dataIndex: 'jobRunTimeLabel',

    },
    {
      title: '是否成功',
      dataIndex: 'success',
      width: 200,
      render:v=>{
        if(v != null){
          return v ? '成功':'异常'
        }
      }
    },
    {
      title: '返回结果',
      dataIndex: 'result',
      width:300
    },

    {
      title: '操作',
      dataIndex: 'option',
      fixed:'right',
      render: (_, record) => {
        let url = SysUtil.getServerUrl() + 'sys/log/'+ record.id;
        return <a  href={url} target='_blank'>日志</a>;
      },
    },

  ]

  clean = (selectedRowKeys)=>{
    HttpUtil.post('/job/jobLogClean', {ids:selectedRowKeys} ).then(rs=>{
      this.tableRef.current.reload()
    })
  }



  render() {
    return <>
      <ProTable
        toolBarRender={(_,{selectedRowKeys})=><>
          <Button disabled={selectedRowKeys.length === 0} onClick={()=>this.clean(selectedRowKeys)} type='primary' >删除{selectedRowKeys.length}条</Button>
        </>}
        actionRef={this.tableRef}
        request={(params) => {
            const jobId = PageUtil.currentParams().jobId
          params.jobId = jobId;

          return HttpUtil.pageData('job/jobLog', params);
        }}
        columns={this.columns}
        rowSelection={{}}
        rowKey='id'
      />

    </>
  }
}



