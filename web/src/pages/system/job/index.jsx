import React from 'react'
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
import {PlusOutlined} from "@ant-design/icons";
import {HttpUtils, Page, ProTable, StringUtils, UrlUtils, ValueType} from "../../../framework";


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
        HttpUtils.get('admin/job/job-class-options').then(rs => {
            this.setState({jobClassOptions: rs})
        }).catch(e => {
            console.error('[Job] 加载作业类选项失败:', e);
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
        HttpUtils.post("admin/job/get-job-param-fields", jobData || {}, {className}).then(rs => {
            this.setState({paramList: rs})
        }).catch(e => {
            console.error('[Job] 加载作业参数字段失败:', e);
        })
    }

    onFinish = (values) => {
        const isNew = !values.id;
        const url = isNew ? 'admin/job/create' : 'admin/job/update';
        HttpUtils.post(url, values).then(rs => {
            this.setState({formOpen: false})
            this.tableRef.current.reload();
        }).catch(e => {
            console.error('[Job] 保存作业失败:', e);
        })
    }

    handleDelete = row => {
        const hide = message.loading("删除作业中...")
        HttpUtils.post('admin/job/delete', {id: row.id}).then(rs => {
            this.tableRef.current.reload();
        }).catch(e => {
            console.error('[Job] 删除作业失败:', e);
            hide();
        })
    }

    handleTriggerJob = row => {
        HttpUtils.get('admin/job/trigger-job', {id: row.id}).then(rs => {
            this.tableRef.current.reload();
        }).catch(e => {
            console.error('[Job] 触发作业失败:', e);
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
                        <Button size='small' perm='job:trigger' onClick={() => this.handleTriggerJob(record)}>执行一次</Button>
                        <Button size='small' perm='job:update' onClick={() => this.handleEdit(record)}> 编辑 </Button>
                        <Popconfirm perm='job:delete' title='是否确定删除?' onConfirm={() => this.handleDelete(record)}>
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
        }).catch(e => {
            console.error('[Job] 加载状态失败:', e);
        })
    };

    showExecuteRecord(record) {
        this.setState({executeRecordOpen: true, formValues: record})
    }


    render() {
        return <Page title="作业调度" description="管理定时作业任务" actions={<><Button type='primary' icon={<PlusOutlined/>} onClick={() => this.handleAdd()}>新增</Button><Button onClick={this.showStatus}>查看状态</Button></>}>
            <ProTable
                actionRef={this.tableRef}
                request={(params) => HttpUtils.get('admin/job/page', params)}
                columns={this.columns}
            >
                <Form.Item label='名称' name='name'>
                    <Input/>
                </Form.Item>
                <Form.Item label='执行类' name='jobClass'>
                    <Input/>
                </Form.Item>
            </ProTable>


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
                                       initialValue={p.defaultValue}
                                       rules={[{required: p.required}]}>
                                {ValueType.renderField(p.componentType, {
                                    ...p.componentProps,
                                    placeholder: p.placeholder || '请输入'
                                })}
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
                <Alert title={<pre>{this.state.status}</pre>}></Alert>

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
                            const url = UrlUtils.contextPath('/admin/sys/log/' + record.id);
                            return <a href={url} target='_blank'>日志</a>;
                        },
                    }
                ]} request={params => {
                    params.jobId = this.state.formValues.id
                    return HttpUtils.get('admin/job/execute-record', params);
                }}></ProTable>

            </Modal>
        </Page>
    }

    onValuesChange = (changed, values) => {
        if (changed.jobClass) {
            this.loadJobParamFields(values.jobClass)
            const option = this.state.jobClassOptions.find(o => o.value === changed.jobClass)
            if (option) {
                const {label} = option;
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



