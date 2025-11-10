import {Button, Modal, Popconfirm, Space} from 'antd';
import React from 'react';
import {HttpUtil, PageUtil, ProTable} from "@tmgg/tmgg-base";
import {PlusOutlined} from "@ant-design/icons";

const baseTitle = "流程模型";
const baseApi = 'flowable/model/';
const basePerm = 'flowable/model:';

const deleteTitle = '删除' + baseTitle


const delApi = baseApi + 'delete'
const pageApi = baseApi + 'page'

const delPerm = basePerm + 'delete'


export default class extends React.Component {


    state = {
        formValues: {},
        formOpen: false
    }

    actionRef = React.createRef();
    formRef = React.createRef();


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
                            onClick={() => PageUtil.open('/flowable/design?id=' + record.id, '流程设计' + record.name)}> 设计 </Button>
                    <Button size='small' onClick={() => this.handleEdit(record)}> 编辑 </Button>
                    <Popconfirm perm={delPerm} title={'是否确定' + deleteTitle}
                                onConfirm={() => this.handleDelete(record)}>
                        <Button size='small' danger>删除</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];


    handleAdd = () => {
        this.setState({
            formOpen: true,
            formValues: {}
        })
    }

    handleEdit = record => {
        this.setState({
            formOpen: true,
            formValues: record
        })
    }
    onFinish = values => {
        HttpUtil.post('flowable/model/save', values).then(rs => {
            this.actionRef.current.reload()
            this.setState({formOpen: false})
        })
    }

    handleDelete = row => {
        HttpUtil.get(delApi, {id: row.id}).then(rs => {
            this.actionRef.current.reload();
        })
    }


    render() {
        const demo = `@Component
@ProcessDefinitionDescription(key = "demo",name = "demo-派车流程", formKeys = @FormKeyDescription(value = "driverForm",label = "司机表单"))
public class DemoProcess implements ProcessDefinition {

    @Override
    public void onProcessEvent(FlowableEventType type, String initiator, String businessKey, Map<String, Object> variables) {

    }
}
`

        return <>
            <ProTable
                search={false}
                actionRef={this.actionRef}
                toolBarRender={() => <Button icon={<PlusOutlined/>} type='primary'
                                             onClick={this.handleAdd}> 新增</Button>}
                request={(params) => HttpUtil.pageData(pageApi, params)}
                columns={this.columns}
                rowSelection={false}
                rowKey="id"
                options={{search: true}}
            />

            <Modal title='模型基本信息'
                   open={this.state.formOpen}
                   onCancel={() => this.setState({formOpen: false})}
                   width={1024}
                   footer={null}
            >

                不支持页面创建， 请参考Java代码
                <pre>
{demo}
        </pre>

            </Modal>
        </>
    }


}



