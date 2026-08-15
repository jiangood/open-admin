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
import {FormModal, HttpClient, Page, PermActions, ProTable, UrlUtils, ValueType} from "../../../framework";


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


export default class JobPage extends React.Component {

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
        HttpClient.get('admin/job/job-class-options', null, rs => {
            this.setState({jobClassOptions: rs})
        }, e => {
            console.error('[Job] 加载任务类选项失败:', e);
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
        HttpClient.post("admin/job/get-job-param-fields", jobData || {}, {className}, rs => {
            this.setState({paramList: rs})
        }, e => {
            console.error('[Job] 加载任务参数字段失败:', e);
        })
    }

    onFinish = values => {
        const isNew = !values.id;
        const url = isNew ? 'admin/job/create' : 'admin/job/update';
        HttpClient.post(url, values, null, () => {
            this.tableRef.current.reload()
        })
    }

    handleDelete = row => {
        const hide = message.loading("删除任务中...")
        HttpClient.post('admin/job/delete', {id: row.id}, null, () => {
            hide();
            this.tableRef.current.reload();
        }, e => {
            console.error('[Job] 删除任务失败:', e);
            hide();
        })
    }

    handleTriggerJob = row => {
        HttpClient.post('admin/job/trigger-job', {id: row.id}, null, () => {
            this.tableRef.current.reload();
        }, e => {
            console.error('[Job] 触发任务失败:', e);
        })
    }

    columns = [
        {
            title: '名称',
            dataIndex: 'name',

        },
        {
            title: '执行类',
            dataIndex: 'jobClassName',
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
        HttpClient.get('admin/job/status', null, rs => {
            this.setState({status: rs})
        }, e => {
            console.error('[Job] 加载状态失败:', e);
        })
    };

    showExecuteRecord(record) {
        this.setState({executeRecordOpen: true, selectedRecord: record})
    }


    render() {
        return <Page>
            <ProTable
                actionRef={this.tableRef}
                toolBarRender={() => (
                    <PermActions>
                        <Button type='primary' perm='job:create' icon={<PlusOutlined/>} onClick={() => this.handleAdd()}>新增</Button>
                        <Button perm='job:read' onClick={this.showStatus}>查看状态</Button>
                    </PermActions>
                )}
                request={(params, success, error) => HttpClient.get('admin/job/page', params, success, error)}
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


            <FormModal ref={this.modalRef} title='定时任务' width={800}
                       onFinish={this.onFinish}
                       onValuesChange={this.onValuesChange}>
                <Form.Item label='执行类' name='jobClass' rules={[{required: true}]}
                           tooltip='继承 BaseJob，参考 HelloWorldJob'>
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
                        <Divider>任务参数</Divider>
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

            <Modal title='定时任务状态'
                   open={this.state.statusOpen}
                   onCancel={() => this.setState({statusOpen: false})}
                   footer={null}
                   width={1024}
            >
                <Alert title={<pre>{this.state.status}</pre>}></Alert>

            </Modal>

            <Modal title='定时任务记录'
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
                ]} request={(params, success, error) => {
                    params.jobId = this.state.selectedRecord.id
                    return HttpClient.get('admin/job/execute-record', params, success, error);
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
                const match = label.match(/[（(](.*?)[）)]/);
                if (match) { // 取括号内中文描述设置为name
                    this.modalRef.current.formInstance.setFieldValue("name", match[1])
                }
            }
        }

        if (changed.jobData) {
            this.loadJobParamFields(values.jobClass, values.jobData)
        }

    };
}



