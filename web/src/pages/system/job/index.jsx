import {
    Alert,
    AutoComplete,
    Button,
    Divider,
    Form,
    Input,
    message,
    Modal,
    Popconfirm,
    Select,
    Space,
    Switch,
    Tag
} from 'antd'
import React from 'react'
import {PlusOutlined} from "@ant-design/icons";
import {ButtonList, HttpUtils, Page, ProTable, StringUtils, ValueType} from "../../../framework";


const cronOptions = [
    {
        label: '*/5 * * * * ? 每隔5秒',
        value: '*/5 * * * * ?'
    },
    {
        label: '0 */5 * * * ? 每隔5分钟',
        value: '0 */5 * * * ?'
    },
    {
        label: '0 0 22 * * ? 每天22点',
        value: '0 0 22 * * ?'
    },
    {
        label: '0 0 1 * * ? 每天1点',
        value: '0 0 1 * * ?'
    },
    {
        label: '0 0 1 1 * ? 每月1号凌晨1点',
        value: '0 0 1 1 * ?'
    }
]


export default class extends React.Component {

    state = {
        formValues: {},
        formOpen: false,

        selectedRowKeys: [],

        jobClassOptions: [],

        paramList: [],

        statusOpen: false,
        status: null,

        executeRecordOpen: false,
    }
    tableRef = React.createRef()
    formRef = React.createRef()

    componentDidMount() {
        HttpUtils.get('admin/job/jobClassOptions').then(rs => {
            this.setState({jobClassOptions: rs})
        })
    }

    handleAdd = () => {
        this.setState({formOpen: true, formValues: {}, paramList: []})
    }

    handleEdit = (record) => {
        this.loadJobParamFields(record.jobClass, record.jobData)
        this.setState({formOpen: true, formValues: record,})
    }

    loadJobParamFields(className, jobData) {
        HttpUtils.post("admin/job/getJobParamFields", jobData || {}, {className}).then(rs => {
            this.setState({paramList: rs})
        })
    }

    onFinish = (values) => {
        HttpUtils.post('admin/job/save', values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload();
        })
    }

    handleDelete = row => {
        const hide = message.loading("删除作业中...")
        HttpUtils.get('admin/job/delete', {id: row.id}).then(rs => {
            this.tableRef.current.reload();
        }).catch(hide)
    }

    handleTriggerJob = row => {
        HttpUtils.get('admin/job/triggerJob', {id: row.id}).then(rs => {
            this.tableRef.current.reload();
        })
    }

    columns = [
        {
            title: '名称',
            dataIndex: 'name',

        },
        {
            title: '执行类',
            dataIndex: 'jobClass',

        },

        {
            title: 'cron',
            dataIndex: 'cron',
        },


        {
            title: '参数',
            dataIndex: 'jobData',
            render(list) {
                if (list)
                    return JSON.stringify(list)
            }
        },


        {
            title: '启用状态',
            dataIndex: 'enabled',
            render: (v, record) => {
                return record.enabled ? <Tag color='green'>启用</Tag> : <Tag color='red'>停用</Tag>
            },
        },

        {
            title: '操作',
            dataIndex: 'option',
            fixed: 'right',
            render: (_, record) => {

                return (
                    <Space>
                        <Button size='small' onClick={() => this.showExecuteRecord(record)}>执行记录</Button>
                        <Button size='small' onClick={() => this.handleTriggerJob(record)}>执行一次</Button>
                        <Button size='small' onClick={() => this.handleEdit(record)}> 编辑 </Button>
                        <Popconfirm title='是否确定删除?' onConfirm={() => this.handleDelete(record)}>
                            <Button size='small'>删除</Button>
                        </Popconfirm>
                    </Space>
                );
            },
        },

    ]

    showStatus = () => {
        this.setState({statusOpen: true})
        HttpUtils.get('admin/job/status').then(rs => {
            this.setState({status: rs})
        })
    };

    showExecuteRecord(record) {
        this.setState({executeRecordOpen: true, formValues: record})
    }


    render() {
        return <Page padding>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={() => {
                    return <ButtonList>
                        <Button type='primary' onClick={() => this.handleAdd()} icon={<PlusOutlined/>}>
                            新增
                        </Button>
                        <Button onClick={this.showStatus}>
                            查看状态
                        </Button>
                    </ButtonList>
                }}
                request={(params) => HttpUtils.get('admin/job/page', params)}
                columns={this.columns}
                showToolbarSearch
            />


            <Modal title='作业调度'
                   open={this.state.formOpen}
                   destroyOnHidden
                   width={800}
                   onOk={() => this.formRef.current.submit()}
                   onCancel={() => this.setState({formOpen: false})}
            >

                <Form ref={this.formRef} labelCol={{flex: '100px'}}
                      initialValues={this.state.formValues}
                      onValuesChange={this.onValuesChange}
                      onFinish={this.onFinish}>
                    <Form.Item name='id' noStyle>
                    </Form.Item>
                    <Form.Item label='执行类' name='jobClass' rules={[{required: true}]}
                               tooltip='org.quartz.Job接口，参考io.tmgg.job.builtin.DemoJob'>
                        <Select options={this.state.jobClassOptions}/>
                    </Form.Item>
                    <Form.Item label='名称' name='name' rules={[{required: true}]}>
                        <Input/>
                    </Form.Item>

                    <Form.Item label='cron表达式' name='cron' help='格式：秒分时日月周,留空表示手动执行'
                               rules={[{required: true}]}>
                        <AutoComplete placeholder='如 0 */5 * * * ?' options={cronOptions}/>
                    </Form.Item>

                    <Form.Item label='启用' name='enabled' valuePropName='checked' rules={[{required: true}]}>
                        <Switch/>
                    </Form.Item>


                    {this.state.paramList?.map(p => (
                        <div key={p.name}>
                            <Divider>作业参数</Divider>
                            <Form.Item label={p.label}
                                       name={['jobData', p.name]}
                                       key={p.name}
                                       rules={[{required: p.required}]}>
                                {ValueType.renderField(p.componentType, p.componentProps)}
                            </Form.Item>
                        </div>
                    ))}
                </Form>
            </Modal>

            <Modal title='作业调度状态'
                   open={this.state.statusOpen}
                   onCancel={() => this.setState({statusOpen: false})}
                   footer={null}
                   width={1024}
            >
                <Alert message={<pre>{this.state.status}</pre>}></Alert>

            </Modal>

            <Modal title='作业调度记录'
                   open={this.state.executeRecordOpen}
                   onCancel={() => this.setState({executeRecordOpen: false})}
                   footer={null}
                   width={1024}
                   destroyOnHidden
            >
                <ProTable columns={[{
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
                        render: v => {
                            if (v != null) {
                                return v ? '成功' : '异常'
                            }
                        }
                    },
                    {
                        title: '返回结果',
                        dataIndex: 'result',
                        width: 300
                    },

                    {
                        title: '操作',
                        dataIndex: 'option',
                        render: (_, record) => {
                            let url = '/admin/sys/log/' + record.id;
                            return <a href={url} target='_blank'>日志</a>;
                        },
                    }
                ]} request={params => {
                    params.jobId = this.state.formValues.id
                    return HttpUtils.get('admin/job/executeRecord', params);
                }}></ProTable>

            </Modal>
        </Page>
    }

    onValuesChange = (changed, values) => {
        if (changed.jobClass) {
            this.loadJobParamFields(values.jobClass)
            const option = this.state.jobClassOptions.find(o => o.value === changed.jobClass)
            if (option) {
                let {label} = option;
                if (StringUtils.contains(label, " ")) { // 取中文名部门设置为name
                    this.formRef.current.setFieldValue("name", label.split(" ")[1])
                }
            }
        }

        if (changed.jobData) {
            this.loadJobParamFields(values.jobClass, values.jobData)
        }

    };
}



