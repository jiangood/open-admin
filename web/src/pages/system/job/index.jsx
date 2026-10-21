import React from 'react'
import {
    Alert,
    Divider,
    Form,
    Input,
    Modal,
    Progress,
    Select,
    Switch,
    Tag,
    Tooltip
} from 'antd'
import {PlusOutlined, SearchOutlined} from "@ant-design/icons";
import {
    CronEditor,
    CronUtils,
    FormModal,
    DictUtils,
    FieldDictSelect,
    getMessageApi,
    HttpClient,
    LogViewer,
    Page,
    PermActions,
    ProTable,
    UrlUtils,
    ValueType
} from "../../../framework";


const cronPresets = [
    {
        label: '每隔 5 秒',
        value: '*/5 * * * * ?'
    },
    {
        label: '每隔 5 分钟',
        value: '0 */5 * * * ?'
    },
    {
        label: '每天 22:00',
        value: '0 0 22 * * ?'
    },
    {
        label: '每天 01:00',
        value: '0 0 1 * * ?'
    },
    {
        label: '每月 1 号 01:00',
        value: '0 0 1 1 * ?'
    },
    {
        label: '工作日 09:00',
        value: '0 0 9 ? * MON-FRI'
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

        logOpen: false,

        progressMap: {},

        cronDesc: '',
    }
    tableRef = React.createRef()
    modalRef = React.createRef()
    progressTimer = null

    componentDidMount() {
        HttpClient.get('admin/job/job-class-options', null, {toastError: false}).then(rs => {
            this.setState({jobClassOptions: rs.data})
        }).catch(e => {
            console.error('[Job] 加载任务类选项失败:', e);
        })
        this.startProgressPolling()
    }

    componentWillUnmount() {
        this.stopProgressPolling()
    }

    startProgressPolling = () => {
        this.loadProgress()
        this.progressTimer = setInterval(() => {
            if (!document.hidden) {
                this.loadProgress()
            }
        }, 3000)
    }

    stopProgressPolling = () => {
        if (this.progressTimer) {
            clearInterval(this.progressTimer)
            this.progressTimer = null
        }
    }

    loadProgress = () => {
        HttpClient.get('admin/job/progress', null, {toastError: false}).then(rs => {
            const progressMap = {}
            ;(rs.data || []).forEach(p => {
                progressMap[p.jobId] = p
            })
            this.setState({progressMap})
        }).catch(e => {
            console.error('[Job] 加载任务进度失败:', e);
        })
    }

    handleAdd = () => {
        this.setState({paramList: [], cronDesc: ''})
        this.modalRef.current.open({})
    }

    handleEdit = (record) => {
        this.setState({cronDesc: CronUtils.describe(record.cron)})
        this.loadJobParamFields(record.jobClass, record.jobData)
        this.modalRef.current.open(record)
    }

    loadJobParamFields(className, jobData) {
        HttpClient.post("admin/job/get-job-param-fields", jobData || {}, {className}, {toastError: false}).then(rs => {
            this.setState({paramList: rs.data})
        }).catch(e => {
            console.error('[Job] 加载任务参数字段失败:', e);
        })
    }

    onFinish = async values => {
        const isNew = !values.id;
        const url = isNew ? 'admin/job/create' : 'admin/job/update';
        await HttpClient.post(url, values)
        this.tableRef.current.reload()
    }

    handleDelete = row => {
        const hide = getMessageApi().loading("删除任务中...")
        HttpClient.post('admin/job/delete', {id: row.id}, null, {toastError: false}).then(() => {
            hide();
            this.tableRef.current.reload();
        }).catch(e => {
            console.error('[Job] 删除任务失败:', e);
            hide();
        })
    }

    handleTriggerJob = row => {
        HttpClient.post('admin/job/trigger-job', {id: row.id}, null, {toastError: false}).then(() => {
            this.tableRef.current.reload();
            this.loadProgress()
        }).catch(e => {
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
            title: 'cron 表达式',
            dataIndex: 'cron',
            width: 220,
            render: (v) => {
                if (!v) {
                    return '-'
                }
                const desc = CronUtils.describe(v)
                return <div style={{lineHeight: 1.45}}>
                    <div style={{
                        fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace',
                        wordBreak: 'break-all'
                    }}>{v}</div>
                    {desc && <div style={{
                        fontSize: 12,
                        color: 'var(--oa-color-text-tertiary, rgba(0, 0, 0, 0.45))'
                    }}>{desc}</div>}
                </div>
            }
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
            title: '执行记录方式',
            dataIndex: 'logAggregation',
            render: v => DictUtils.dictTag('jobLogAggregation', v) || '每次一份记录',
        },

        {
            title: '运行状态',
            dataIndex: 'progress',
            width: 220,
            render: (_, record) => {
                const p = this.state.progressMap[record.id]
                if (!p) {
                    return <Tag>未运行</Tag>
                }
                return <div>
                    <Tag color={p.stateColor || 'default'}>{p.stateLabel}</Tag>
                    {p.percent != null && <Progress percent={p.percent} size='small'/>}
                    {p.message && <Tooltip title={p.message}>
                        <div style={{
                            fontSize: 12,
                            color: 'var(--oa-color-text-secondary, rgba(0, 0, 0, 0.65))',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap',
                            maxWidth: 200
                        }}>{p.message}</div>
                    </Tooltip>}
                </div>
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
                        {label: '执行情况', perm: 'job:read', onClick: () => this.showExecuteRecord(record)},
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
        HttpClient.get('admin/job/status', null, {toastError: false}).then(rs => {
            this.setState({status: rs.data})
        }).catch(e => {
            console.error('[Job] 加载状态失败:', e);
        })
    };

    showExecuteRecord(record) {
        this.setState({executeRecordOpen: true, selectedRecord: record})
        this.loadProgress()
    }

    /**
     * 执行记录弹窗顶部：当前任务的运行中进度（内存态）
     */
    renderRunningProgress() {
        const {selectedRecord, progressMap} = this.state
        const p = selectedRecord ? progressMap[selectedRecord.id] : null
        if (!p || p.state !== 'RUNNING') {
            return null
        }
        return <Alert
            type='info'
            showIcon
            style={{marginBottom: 12}}
            title={<div>
                <div style={{marginBottom: 6}}>{p.message || '任务运行中'}</div>
                {p.percent != null && <Progress percent={p.percent} size='small'/>}
            </div>}
        />
    }

    showLog = (record) => {
        this.setState({logOpen: true, selectedLogRecord: record})
    }

    /**
     * 该执行记录对应的日志是否仍在写入：用进度快照里的 logId 精确匹配
     */
    isLogRunning = () => {
        const {selectedLogRecord, progressMap} = this.state
        if (!selectedLogRecord) {
            return false
        }
        return Object.values(progressMap).some(p =>
            p.logId === selectedLogRecord.id && p.state === 'RUNNING')
    }

    /** 结束时间与开始时间为同一天时只显示时分秒，避免重复日期 */
    formatRecordEndTime = (beginTime, endTime) => {
        if (!beginTime || !endTime) {
            return endTime || '-'
        }
        return endTime.slice(0, 10) === beginTime.slice(0, 10) ? endTime.slice(11) : endTime
    }


    render() {
        return <Page>
            <ProTable
                actionRef={this.tableRef}
                searchInline
                toolBarRightRender={() => (
                    <PermActions actions={[
                        {label: '新增', type: 'primary', icon: <PlusOutlined/>, perm: 'job:create', onClick: () => this.handleAdd()},
                        {label: '查看状态', perm: 'job:read', onClick: this.showStatus},
                    ]}/>
                )}
                request={(params) => HttpClient.get('admin/job/page', params)}
                columns={this.columns}
                searchFormRender={() => (
                    <>
                        <Form.Item name='name' noStyle>
                            <Input prefix={<SearchOutlined/>} placeholder='任务名称' allowClear/>
                        </Form.Item>
                        <Form.Item name='jobClass' noStyle>
                            <Input placeholder='执行类' allowClear/>
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

                <Form.Item label='cron表达式' name='cron'
                           help='必填，格式：秒 分 时 日 月 周（可选 年）'
                           extra={this.state.cronDesc ? `解析：${this.state.cronDesc}` : null}
                           rules={[{required: true}]}>
                    <CronEditor presets={cronPresets} placeholder='如 0 */5 * * * ?'/>
                </Form.Item>

                <Form.Item label='启用' name='enabled' valuePropName='checked' rules={[{required: true}]}>
                    <Switch/>
                </Form.Item>

                <Form.Item label='执行记录方式' name='logAggregation' initialValue='PER_RUN'
                           help='控制执行记录与日志文件的粒度。高频任务建议选择「每天一份记录」：一天只保留一条执行记录，日志文件按天合并'>
                    <FieldDictSelect typeCode='jobLogAggregation'/>
                </Form.Item>

                {this.state.paramList?.map(p => (
                    <div key={p.name}>
                        <Divider>任务参数</Divider>
                        <Form.Item label={p.label}
                                   name={['jobData', p.name]}
                                   key={p.name}
                                   initialValue={p.defaultValue}
                                   valuePropName={p.valueType === 'boolean' ? 'checked' : 'value'}
                                   rules={[{required: p.required}]}>
                            {ValueType.renderField(p.valueType, {
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
                {this.renderRunningProgress()}
                <ProTable columns={[
                    {
                        title: '开始 / 结束时间',
                        dataIndex: 'beginTime',
                        width: 170,
                        render: (v, record) => <div style={{lineHeight: 1.45}}>
                            <div>{v || '-'}</div>
                            <div style={{
                                fontSize: 12,
                                color: 'var(--oa-color-text-tertiary, rgba(0, 0, 0, 0.45))',
                                whiteSpace: 'nowrap'
                            }}>{record.endTime
                                ? '至 ' + this.formatRecordEndTime(v, record.endTime)
                                : '运行中…'}</div>
                        </div>
                    },
                    {
                        title: '成功/失败',
                        dataIndex: 'executeCount',
                        width: 150,
                        render: (v, record) => {
                            const count = v == null ? 1 : v
                            // 旧版本升级上来的记录没有 failCount，用 success 兜底
                            const failCount = record.failCount == null
                                ? (record.success === false ? 1 : 0)
                                : record.failCount
                            const successCount = Math.max(count - failCount, 0)
                            if (successCount === 0 && failCount === 0) {
                                return '-'
                            }
                            return <span>
                                {successCount > 0 &&
                                    <Tag color='success' style={{marginRight: 4}}>成功 {successCount}</Tag>}
                                {failCount > 0 &&
                                    <Tag color='error' style={{marginRight: 0}}>失败 {failCount}</Tag>}
                            </span>
                        },
                    },
                    {
                        title: '返回结果',
                        dataIndex: 'result',
                        render: v => (v == null || v === '') ? '-' : v,
                    },
                    {
                        title: '操作',
                        dataIndex: 'option',
                        width: 64,
                        fixed: 'right',
                        render: (_, record) => <a onClick={() => this.showLog(record)}>日志</a>,
                    }
                ]} request={(params) => {
                    params.jobId = this.state.selectedRecord.id
                    return HttpClient.get('admin/job/execute-record', params);
                }}></ProTable>

            </Modal>

            <Modal title='任务日志'
                   open={this.state.logOpen}
                   onCancel={() => this.setState({logOpen: false})}
                   width={1024}
                   destroyOnHidden
                   footer={this.state.selectedLogRecord ? (
                       <a href={UrlUtils.contextPath('/admin/sys/log/raw/job/' + this.state.selectedLogRecord.id)}
                          target='_blank' rel='noreferrer'>在新窗口打开</a>
                   ) : null}
            >
                {this.state.selectedLogRecord && (
                    <LogViewer key={this.state.selectedLogRecord.id}
                               logKey={'job/' + this.state.selectedLogRecord.id}
                               running={this.isLogRunning()}
                               height={480}/>
                )}
            </Modal>
        </Page>
    }

    onValuesChange = (changed, values) => {
        if (changed.cron !== undefined) {
            this.setState({cronDesc: CronUtils.describe(values.cron)})
        }

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



