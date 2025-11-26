import {Alert, Button, Card, Divider, Modal, Popconfirm, Space, Typography} from 'antd';
import React from 'react';
import {PlusOutlined, QuestionCircleOutlined, QuestionCircleTwoTone, QuestionOutlined} from "@ant-design/icons";
import {HttpUtils, MsgBox, MessageUtils, PageUtils, ProTable, Gap} from "../../framework";

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
            title: '表单链接',
            dataIndex: 'formUrl'
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


    handleAdd = () => {
        let demo = `
@Component
@ProcessDefinitionDescription(key = "demo",name = "demo-派车流程", formKeys = @FormKeyDescription(value = "driverForm",label = "司机表单"))
public class DemoProcess implements ProcessDefinition {

    @Override
    public void onProcessEvent(FlowableEventType type, String initiator, String businessKey, Map<String, Object> variables) {

    }
}
`
        demo = <div style={{overflowX: "auto"}}>
            <Divider/>
            <Typography.Text>不支持页面创建， 请参考Java代码</Typography.Text>
            <pre>{demo}</pre>
        </div>
        MessageUtils.alert(demo, '流程模型创建说明', {width: 800})
    }


    handleDelete = row => {
        HttpUtils.get('admin/flowable/model/delete', {id: row.id}).then(rs => {
            this.actionRef.current.reload();
        })
    }


    render() {

        return <>
            <ProTable
                actionRef={this.actionRef}
                toolBarRender={() => <Button icon={<QuestionCircleOutlined/>} type='primary'
                                             onClick={this.handleAdd}>如何创建模型</Button>}
                request={(params) => HttpUtils.get('admin/flowable/model/page', params)}
                columns={this.columns}
                showToolbarSearch={true}
            />


        </>
    }


}



