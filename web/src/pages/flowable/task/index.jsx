import React from "react";
import {Button, Modal, Tabs} from "antd";
import {HttpUtil, LinkButton, Page, PageLoading, ProTable} from "@tmgg/tmgg-base";
import InstanceInfo from "../../../components/InstanceInfo";


export default class extends React.Component {

    state = {
        show: true
    }

    render() {
        if (!this.state.show) {
            return <PageLoading/>
        }

        const items = [
            {label: '待办任务', key: '1', children: this.renderTodo()},
            {label: '已办任务', key: '2', children: this.renderDone()},
            {label: '我发起的', key: '3', children: this.renderMyStart()},
        ]

        return <Page padding>
            <Tabs defaultActiveKey="1" destroyOnHidden items={items}>

            </Tabs>
        </Page>
    }


    renderTodo = () => <ProTable
        toolbarOptions={{showSearch: false}}
        request={(params) => HttpUtil.pageData("flowable/userClient/todoTaskPage", params)}
        columns={[

            {
                title: '发起人',
                dataIndex: 'instanceStarter'
            },
            {
                title: '流程名称',
                dataIndex: 'instanceName',
            },
            {
                title: '当前节点',
                dataIndex: 'taskName',
                width: 100,
            },
            {
                title: '当前操作人',
                dataIndex: 'assigneeInfo',
                width: 100
            },

            {
                title: '发起时间',
                dataIndex: 'instanceStartTime',
            },
            {
                title: '任务创建时间',
                dataIndex: 'createTime',
            },
            {
                title: '接收时间',
                dataIndex: 'createTime',
            },


            {
                title: '操作',
                dataIndex: 'option',
                render: (_, record) => {
                    let path = '/flowable/task/form?taskId=' + record.id + '&instanceId=' + record.instanceId;
                    if (record.formKey) {
                        path += "&formKey=" + record.formKey
                    }
                    return (
                        <LinkButton
                            path={path}
                            label='处理任务'>处理</LinkButton>
                    );
                },
            },
        ]}
        size='small'
    />;

    renderDone = () => <ProTable

        request={(params) => HttpUtil.pageData("flowable/userClient/doneTaskPage", params)}
        columns={[
            {
                title: '流程名称',
                dataIndex: 'instanceName',
            },
            {
                title: '发起人',
                dataIndex: 'instanceStarter'
            },
            {
                title: '发起时间',
                dataIndex: 'instanceStartTime',
            },
            {
                title: '任务创建时间',
                dataIndex: 'createTime',
            },
            {
                title: '处理时间',
                dataIndex: 'endTime',
            },
            {
                title: '耗时(小时)',
                dataIndex: 'durationInHours',
            },
            {
                title: '处理节点',
                dataIndex: 'taskName'
            },
            {
                title: '操作人',
                dataIndex: 'assigneeInfo'
            },

            {
                title: '流程状态',
                dataIndex: 'instanceStatusLabel'
            },


            {
                title: '操作',
                dataIndex: 'option',
                render: (_, record) => (
                    <Button size='small' onClick={() => {
                        Modal.info({
                            title: '流程信息',
                            width: '800vw',
                            content: <InstanceInfo id={record.instanceId}/>
                        })
                    }}> 查看 </Button>
                ),
            },
        ]}
        size='small'
    />;

    renderMyStart = () => <ProTable
        request={(params) => HttpUtil.pageData("flowable/userClient/myInstance", params)}
        columns={[

            {
                title: '流程名称',
                dataIndex: 'processDefinitionName',
                render(_, r) {
                    return r.name || r.processDefinitionName
                }
            },
            {
                title: '发起人',
                dataIndex: 'startUserName',
            },
            {
                title: '发起时间',
                dataIndex: 'startTime',
            },
            {
                title: '业务标识',
                dataIndex: 'businessKey',

            },


            {
                title: '结束时间',
                dataIndex: 'endTime',
            },

            {
                title: '流程状态',
                dataIndex: 'x',
                render(_, row) {
                    return row.endTime == null ? '进行中' : '已结束'
                }
            },
            {
                title: '终止原因',
                dataIndex: 'deleteReason',
            },


            {
                title: '操作',
                dataIndex: 'option',
                render: (_, record) => (
                    <Button size='small' onClick={() => {

                        Modal.info({
                            title: '流程信息',
                            width: '80vw',
                            content: <InstanceInfo id={record.id}/>
                        })
                    }}> 查看 </Button>
                ),
            },
        ]}
    />;
}
