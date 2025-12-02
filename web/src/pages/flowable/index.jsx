import {Button, Popconfirm, Space, Typography} from 'antd';
import React from 'react';
import {QuestionCircleOutlined} from "@ant-design/icons";
import {Gap, HttpUtils, MessageUtils, PageUtils, ProTable} from "../../framework";

export default class extends React.Component {


    actionRef = React.createRef();


    columns = [
        {
            title: '模型名称',
            dataIndex: 'name',
            sorter: true
        },
        {
            title: '唯一编码',
            dataIndex: 'code'
        },

        {
            title: '更新时间',
            dataIndex: 'updateTime',
        },


        {
            title: '操作',
            dataIndex: 'option',
            render: (_, record) => (
                <Space>
                    <Button size='small' type='primary'
                            onClick={() => PageUtils.open('/flowable/design?id=' + record.id, '流程设计' + record.name)}> 设计 </Button>
                    <Button size='small' onClick={() => this.handleEdit(record)}> 编辑 </Button>
                    <Popconfirm perm='flowable/model:delete' title={'是否确定删除流程模型'}
                                onConfirm={() => this.handleDelete(record)}>
                        <Button size='small' danger>删除</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];




    handleDelete = row => {
        HttpUtils.get('admin/flowable/model/delete', {id: row.id}).then(rs => {
            this.actionRef.current.reload();
        })
    }


    render() {

        return <>
            <ProTable
                actionRef={this.actionRef}
                request={(params) => HttpUtils.get('admin/flowable/model/page', params)}
                columns={this.columns}
                showToolbarSearch={true}
            />


        </>
    }


}



