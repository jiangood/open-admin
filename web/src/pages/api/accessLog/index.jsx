import {PlusOutlined} from '@ant-design/icons'
import {Button, Card,InputNumber, Popconfirm,Modal,Form,Input,message} from 'antd'
import React from 'react'
import {ButtonList,dictValueTag, ViewBoolean,FieldDateRange,FieldDictSelect,FieldRadioBoolean, FieldDatePickerString, FieldDateTimePickerString,Page, HttpUtil, ProTable} from "@tmgg/tmgg-base";



export default class extends React.Component {



  columns = [

    {
      title: '接口名称',
      dataIndex: 'name',




    },

    {
      title: '接口',
      dataIndex: 'action',




    },

      {
          title: 'requestId',
          dataIndex: 'requestId',

      },

    {
      title: '请求数据',
      dataIndex: 'requestData',




    },

    {
      title: '响应数据',
      dataIndex: 'responseData',




    },

    {
      title: 'ip',
      dataIndex: 'ip',




    },

    {
      title: 'ipLocation',
      dataIndex: 'ipLocation',




    },

    {
      title: '执行时间',
      dataIndex: 'executionTime',




    },

    {
      title: '接口账户',
      dataIndex: 'accountName',




    },


  ]



  render() {
    return <Page>
      <ProTable
          request={(params) => HttpUtil.pageData('apiAccessLog/page', params)}
          columns={this.columns}
      />


    </Page>


  }
}


