import React from "react";
import {Button, Card, Tabs, Tag} from "antd";
import {HttpUtil, ProTable} from "@tmgg/tmgg-base";

export default class  extends React.Component {

    tableRef = React.createRef()

    columns = [
        {
            title: '时间',
            dataIndex: 'createTime',
            width: 150
        },
        {
            title: '标题',
            dataIndex: 'title',
        },
        {
            title: '内容',
            dataIndex: 'content',
            render(content, row) {
                return <div dangerouslySetInnerHTML={{__html: content}}/>
            }
        },
        {
            title: '已读时间',
            dataIndex: 'readTime',
            width: 150
        },
        {
            title: '状态',
            dataIndex: 'isRead',
            render: (v, row) => {
                return v == true ? <Tag color='green'>已读</Tag> : <Tag color='red'>未读</Tag>;
            },
            width: 80
        },

        {
            title: '-',
            dataIndex: 'option',
            render: (read, row) => {
                if(!row.isRead)
                 return <Button size='small' onClick={() => this.read(row)}> 标记已读 </Button>
            },   width: 80
        }
    ]

    read = (record) => {
        HttpUtil.post("user/msg/read", record).then(rs => {
            this.tableRef.current.reload()
        })
    }



    render() {
        return <Card variant={"borderless"}>

            <Tabs defaultActiveKey="0" destroyOnHidden
                >
                <Tabs.TabPane key='0' tab='所有消息'>
                    <ProTable
                        actionRef={this.tableRef}
                        request={(params) => HttpUtil.pageData("user/msg/page", params)}
                        columns={this.columns}
                        toolbarOptions={{showSearch:false}}
                        size='small'
                    />
                </Tabs.TabPane>
                <Tabs.TabPane key='1' tab='未读消息'>
                    <ProTable
                        actionRef={this.tableRef}
                        request={(params) => HttpUtil.pageData("user/msg/page?read=false", params)}
                        columns={this.columns}
                        rowSelection={false}
                        rowKey='id'
                        toolbarOptions={{showSearch:false}}
                        size='small'
                    />


                </Tabs.TabPane>
                <Tabs.TabPane key='2' tab='已读消息'>

                    <ProTable
                        actionRef={this.tableRef}
                        request={(params) => HttpUtil.pageData("user/msg/page?read=true", params)}
                        rowSelection={false}
                        rowKey='id'
                        toolbarOptions={{showSearch:false}}
                        options={false}
                        size='small'
                        columns={this.columns}
                    />


                </Tabs.TabPane>

            </Tabs>

        </Card>
    }
}
