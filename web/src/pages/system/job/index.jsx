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
    Select,
    Switch,
    Tag
} from 'antd'
import {PlusOutlined} from "@ant-design/icons";
import {FormModal, HttpUtils, Page, PermActions, ProTable, StringUtils, UrlUtils, ValueType} from "../../../framework";


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
        selectedRowKeys: [],

        jobClassOptions: [],

        paramList: [],

        statusOpen: false,
        status: null,

        executeRecordOpen: false,
    }
    tableRef = React.createRef()
    modalRef = React.createRef()

    componentDidMount() {
        HttpUtils.get('admin/job/job-class-options').then(rs => {
            this.setState({jobClassOptions: rs})
        }).catch(e => {
            console.error('[Job] 加载作业类选项失败:', e);
        })
    }

    handleAdd = () => {
        this.setState({paramList: []})
        this.modalRef.current.open({})
    }

    handleEdit = (record) => {
        this.loadJobParamFields(record.jobClass, record.jobData)
        this.modalRef.current.open(record)
    }

    loadJobParamFields(className, jobData) {
        HttpUtils.post("admin/job/get-job-param-fields", jobData || {}, {className}).then(rs => {
            this.setState({paramList: rs})
        }).catch(e => {
            console.error('[Job] 加载作业参数字段失败:', e);
        })
    }

    onFinish = async values => {
        const isNew = !values.id;
        const url = isNew ? 'admin/job/create' : 'admin/job/update';
        await HttpUtils.post(url, values)
        this.tableRef.current.reload()
    }

    handleDelete = row => {
        const hide = message.loading("删除作业中...")
        HttpUtils.post('admin/job/delete', {id: row.id}).then(rs => {
            hide();
            this.tableRef.current.reload();
        }).catch(e => {
            console.error('[Job] 删除作业失败:', e);
            hide();
        })
    }

    handleTriggerJob = row => {
        HttpUtils.post('admin/job/trigger-job', {id: row.id}).then(rs => {
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

                return <PermActions
                    more
                    size="small"
                    actions={[
                        {label: '执行记录', onClick: () => this.showExecuteRecord(record)},
                        {label: '执行一次', perm: 'job:trigger', onClick: () => this.handleTriggerJob(record)},
                        {label: '编辑', perm: 'job:update', onClick: () => this.handleEdit(record)},
                        {label: '删除', perm: 'job:delete', confirm: '是否确定删除?', onClick: () => this.handleDelete(record)},
                    ]}
                />;
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
        this.setState({executeRecordOpen: true, selectedRecord: record})
    }


    render() {
        return <Page title="作业调度" description="管理定时作业任务">
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={() => (
                    <PermActions>
                        <Button type='primary' perm='job:create' icon={<PlusOutlined/>} onClick={() => this.handleAdd()}>新增</Button>
                        <Button perm='job:read' onClick={this.showStatus}>查看状态</Button>
                    </PermActions>
                )}
                request={(params) => HttpUtils.get('admin/job/page', params)}
                columns={this.columns}
                searchFormRender={() => (
                    <>
                        <Form.Item label='名称' name='name'>
                            <Input/>
                        </Form.Item>
                        <Form.Item label='执行类' name='jobClass'>
                            <Input/>
                        </Form.Item>
                    </>
                )}
            />


            <FormModal ref={this.modalRef} title='作业调度' width={800}
                       onFinish={this.onFinish}
                       onValuesChange={this.onValuesChange}>
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
            </FormModal>

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
                            const url = UrlUtils.contextPath('/admin/sys/log/job/' + record.id);
                            return <a href={url} target='_blank'>日志</a>;
                        },
                    }
                ]} request={params => {
                    params.jobId = this.state.selectedRecord.id
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
                    this.modalRef.current.formInstance.setFieldValue("name", label.split(" ")[1])
                }
            }
        }

        if (changed.jobData) {
            this.loadJobParamFields(values.jobClass, values.jobData)
        }

    };
}



