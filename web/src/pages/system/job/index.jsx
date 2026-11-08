import React from 'react'
import {
    Alert,
    Button,
    Card,
    Divider,
    Form,
    Input,
    Modal,
    Progress,
    Select,
    Statistic,
    Switch,
    Table,
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
    PermUtils,
    ProTable,
    UrlUtils,
    ValueType
} from "../../../framework";


/** 字节数转可读单位 */
function formatBytes(bytes) {
    if (!bytes || bytes <= 0) {
        return '0 B'
    }
    const units = ['B', 'KB', 'MB', 'GB', 'TB']
    const k = 1024
    const index = Math.min(Math.floor(Math.log(bytes) / Math.log(k)), units.length - 1)
    return `${(bytes / Math.pow(k, index)).toFixed(index > 0 ? 1 : 0)} ${units[index]}`
}

const statCardsStyle = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
    gap: 12,
    marginBottom: 12,
}

const statHintStyle = {
    marginTop: 4,
    fontSize: 12,
    color: 'var(--oa-color-text-tertiary, rgba(0, 0, 0, 0.45))',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
}

const statWarnStyle = {
    ...statHintStyle,
    color: 'var(--oa-color-error, #ff4d4f)',
}

/**
 * 任务列表各列宽度之和，作为表格最小宽度：
 * 容器更宽时铺满，更窄时横向滚动，避免列被压缩到内容溢出。
 */
const JOB_TABLE_MIN_WIDTH = 1280

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

        executeRecordOpen: false,

        logOpen: false,

        progressMap: {},

        cronDesc: '',

        importOpen: false,
        unregistered: [],
        importLoading: false,

        // 顶部卡片：统计 / 日志占用
        stat: null,
        logSize: null,
        // 「执行情况」弹窗内当前任务的日志占用
        jobLogSize: null,
    }
    tableRef = React.createRef()
    modalRef = React.createRef()
    progressTimer = null
    statTimer = null

    componentDidMount() {
        HttpClient.get('admin/job/job-class-options', null, {toastError: false}).then(rs => {
            this.setState({jobClassOptions: rs.data})
        }).catch(e => {
            console.error('[Job] 加载任务类选项失败:', e);
        })
        this.startProgressPolling()
        this.startStatPolling()
    }

    componentWillUnmount() {
        this.stopProgressPolling()
        this.stopStatPolling()
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

    startStatPolling = () => {
        this.loadStat()
        this.statTimer = setInterval(() => {
            if (!document.hidden) {
                this.loadStat()
            }
        }, 15000)
    }

    stopStatPolling = () => {
        if (this.statTimer) {
            clearInterval(this.statTimer)
            this.statTimer = null
        }
    }

    /**
     * 顶部卡片数据：任务统计 + 日志总占用。日志占用由后端缓存 30 秒，轮询开销很小。
     */
    loadStat = () => {
        if (!PermUtils.hasPermission('job:read')) {
            return
        }
        Promise.all([
            HttpClient.get('admin/job/stat', null, {toastError: false}),
            HttpClient.get('admin/job/log-size', null, {toastError: false}),
        ]).then(([statResult, logSizeResult]) => {
            this.setState({stat: statResult.data, logSize: logSizeResult.data})
            if (this.state.executeRecordOpen && this.state.selectedRecord) {
                this.loadJobLogSize(this.state.selectedRecord.id)
            }
        }).catch(e => {
            console.error('[Job] 加载任务统计失败:', e);
        })
    }

    loadJobLogSize = jobId => {
        HttpClient.get(`admin/job/${jobId}/log-size`, null, {toastError: false}).then(rs => {
            this.setState({jobLogSize: rs.data})
        }).catch(e => {
            console.error('[Job] 加载任务日志占用失败:', e);
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
        this.loadStat()
    }

    handleDelete = row => {
        const hide = getMessageApi().loading("删除任务中...")
        HttpClient.post('admin/job/delete', {id: row.id}, null, {toastError: false}).then(() => {
            hide();
            this.tableRef.current.reload();
            this.loadStat()
        }).catch(e => {
            console.error('[Job] 删除任务失败:', e);
            hide();
        })
    }

    handleTriggerJob = row => {
        HttpClient.post('admin/job/trigger-job', {id: row.id}, null, {toastError: false}).then(() => {
            this.tableRef.current.reload();
            this.loadProgress()
            this.loadStat()
        }).catch(e => {
            console.error('[Job] 触发任务失败:', e);
        })
    }

    showImport = () => {
        this.setState({importOpen: true})
        this.loadUnregistered()
    }

    loadUnregistered = () => {
        HttpClient.get('admin/job/unregistered', null, {toastError: false}).then(rs => {
            this.setState({unregistered: rs.data || []})
        }).catch(e => {
            console.error('[Job] 加载未注册任务失败:', e);
        })
    }

    handleImport = keys => {
        this.setState({importLoading: true})
        HttpClient.post('admin/job/import', {keys: keys && keys.length ? keys : null}, null, {toastError: false}).then(rs => {
            this.setState({importLoading: false})
            getMessageApi().success(rs.msg || '导入完成')
            this.loadUnregistered()
            this.tableRef.current.reload()
            this.loadStat()
        }).catch(e => {
            console.error('[Job] 导入任务失败:', e);
            this.setState({importLoading: false})
        })
    }

    columns = [
        {
            title: '名称',
            dataIndex: 'name',
            width: 180,
            render: v => v ? <span style={{overflowWrap: 'break-word'}}>{v}</span> : '-'
        },

        {
            title: '执行类',
            dataIndex: 'jobClassName',
            width: 160,
            render: v => v
                ? <span style={{
                    fontSize: 12,
                    color: 'var(--oa-color-text-tertiary, rgba(0, 0, 0, 0.45))',
                    wordBreak: 'break-all'
                }}>{v}</span>
                : '-'
        },

        {
            title: 'cron 表达式',
            dataIndex: 'cron',
            width: 140,
            render: (v) => {
                if (!v) {
                    return '-'
                }
                const desc = CronUtils.describe(v)
                return <Tooltip title={desc}>
                    <span style={{
                        fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace',
                        wordBreak: 'break-all'
                    }}>{v}</span>
                </Tooltip>
            }
        },

        {
            title: '参数',
            dataIndex: 'jobData',
            width: 180,
            render(list) {
                if (!list) {
                    return '-'
                }
                const text = JSON.stringify(list)
                return text === '{}' ? '-' : <span style={{wordBreak: 'break-all'}}>{text}</span>
            }
        },

        {
            title: '启用',
            dataIndex: 'enabled',
            width: 76,
            render: v => v ? <Tag color='green'>启用</Tag> : <Tag color='red'>停用</Tag>
        },

        {
            title: '记录方式',
            dataIndex: 'logAggregation',
            width: 100,
            render: v => <span style={{overflowWrap: 'break-word'}}>
                {DictUtils.dictLabel('jobLogAggregation', v) || '每次一份记录'}
            </span>
        },

        {
            title: '运行状态',
            dataIndex: 'progress',
            width: 170,
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
                            maxWidth: 160
                        }}>{p.message}</div>
                    </Tooltip>}
                </div>
            },
        },

        {
            title: '操作',
            dataIndex: 'option',
            width: 272,
            fixed: 'right',
            render: (_, record) => {

                return <PermActions
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

    showExecuteRecord(record) {
        this.setState({executeRecordOpen: true, selectedRecord: record, jobLogSize: null})
        this.loadProgress()
        this.loadJobLogSize(record.id)
    }

    /**
     * 顶部卡片：运行中 / 今日执行 / 日志占用 / 调度器
     */
    renderStatCards() {
        const {stat, logSize} = this.state
        if (!stat || !logSize || !PermUtils.hasPermission('job:read')) {
            return null
        }
        const scheduler = stat.scheduler || {}
        const logWarn = !!logSize.warn
        const warnTip = logSize.warnMb
            ? `已超过告警阈值 ${logSize.warnMb} MB。可缩短 sys.job-log-retain-days，或让任务按天/小时聚合日志。`
            : null
        const warnColor = {color: 'var(--oa-color-error, #ff4d4f)'}
        const schedulerTip = scheduler.error ? null
            : <div style={{lineHeight: 1.7, fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace'}}>
                <div>schedulerName: {scheduler.schedulerName || '-'}</div>
                <div>instanceId: {scheduler.instanceId || '-'}</div>
                <div>runningSince: {scheduler.runningSince || '-'}</div>
                <div>jobStoreClass: {scheduler.jobStoreClass || '-'}</div>
                <div>persistent: {String(!!scheduler.persistent)} · clustered: {String(!!scheduler.clustered)}</div>
                <div>threadPoolSize: {scheduler.threadPoolSize ?? '-'} · jobsExecuted: {scheduler.jobsExecuted ?? 0}</div>
            </div>

        return <div style={statCardsStyle}>
            <Card size='small'>
                <Statistic title='运行中' value={stat.runningCount} suffix='个'/>
                <div style={statHintStyle}>调度器已执行 {scheduler.jobsExecuted ?? 0} 次</div>
            </Card>
            <Card size='small'>
                <Statistic title='今日执行' value={stat.todayExecuteCount} suffix='次'/>
                <div style={stat.todayFailCount > 0 ? statWarnStyle : statHintStyle}>
                    今日失败 {stat.todayFailCount} 次
                </div>
            </Card>
            <Card size='small'>
                <Statistic title='日志占用' value={formatBytes(logSize.bytes)}
                           valueStyle={logWarn ? warnColor : undefined}/>
                <Tooltip title={logWarn ? warnTip : null}>
                    <div style={logWarn ? statWarnStyle : statHintStyle}>
                        {logSize.files} 个日志文件{logSize.warnMb ? ` · 阈值 ${logSize.warnMb} MB` : ''}
                    </div>
                </Tooltip>
            </Card>
            <Card size='small'>
                <Statistic title='调度器'
                           value={scheduler.error ? '异常' : `Quartz ${scheduler.version || '-'}`}
                           valueStyle={{fontSize: 20, ...(scheduler.error ? warnColor : {})}}/>
                <Tooltip title={schedulerTip}>
                    <div style={scheduler.error ? statWarnStyle : statHintStyle}>
                        {scheduler.error || `${scheduler.standby ? '待机' : (scheduler.started ? '运行中' : '未启动')}`
                            + ` · ${scheduler.jobStoreClass || '-'} · 线程 ${scheduler.threadPoolSize ?? '-'}`}
                    </div>
                </Tooltip>
            </Card>
        </div>
    }

    /**
     * 「执行情况」弹窗顶部：当前任务的日志占用
     */
    renderJobLogSize() {
        const {jobLogSize} = this.state
        if (!jobLogSize) {
            return null
        }
        return <div style={{marginBottom: 12, color: 'var(--oa-color-text-secondary, rgba(0, 0, 0, 0.65))'}}>
            该任务日志占用 <b>{formatBytes(jobLogSize.bytes)}</b>（{jobLogSize.files} 个文件）
        </div>
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

    render() {
        return <Page>
            {this.renderStatCards()}

            <ProTable
                actionRef={this.tableRef}
                searchInline
                scrollX={JOB_TABLE_MIN_WIDTH}
                toolBarRightRender={() => (
                    <PermActions actions={[
                        {label: '新增', type: 'primary', icon: <PlusOutlined/>, perm: 'job:create', onClick: () => this.handleAdd()},
                        {label: '导入任务', perm: 'job:create', onClick: this.showImport},
                    ]}/>
                )}
                request={(params) => HttpClient.get('admin/job/page', params)}
                columns={this.columns}
                searchFormRender={() => (
                    <Form.Item name='searchText' noStyle>
                        <Input prefix={<SearchOutlined/>} placeholder='任务名称 / 执行类' allowClear/>
                    </Form.Item>
                )}
            />


            <FormModal ref={this.modalRef} title='定时任务' width={800}
                       onFinish={this.onFinish}
                       onValuesChange={this.onValuesChange}>
                <Form.Item label='执行类' name='jobClass' rules={[{required: true}]}
                           tooltip='继承 BaseJob'>
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

            <Modal title='定时任务记录'
                   open={this.state.executeRecordOpen}
                   onCancel={() => this.setState({executeRecordOpen: false, jobLogSize: null})}
                   footer={null}
                   width={1024}
                   destroyOnHidden
            >
                {this.renderJobLogSize()}
                {this.renderRunningProgress()}
                <ProTable scrollX={800} columns={[
                    {
                        title: '开始时间',
                        dataIndex: 'beginTime',
                        width: 170,
                        render: v => v || '-'
                    },
                    {
                        title: '结束时间',
                        dataIndex: 'endTime',
                        width: 170,
                        render: (v, record) => v || (record.beginTime ? '运行中…' : '-')
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
                        render: v => (v == null || v === '') ? '-' : <span style={{wordBreak: 'break-all'}}>{v}</span>,
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

            <Modal title='导入代码中的任务'
                   open={this.state.importOpen}
                   onCancel={() => this.setState({importOpen: false})}
                   width={860}
                   footer={[
                       <Button key='close' onClick={() => this.setState({importOpen: false})}>关闭</Button>,
                       <Button key='importAll'
                               type='primary'
                               loading={this.state.importLoading}
                               disabled={!this.state.unregistered.length}
                               onClick={() => this.handleImport(this.state.unregistered.map(item => item.key))}>
                           全部导入
                       </Button>
                   ]}
            >
                <Alert type='info' showIcon style={{marginBottom: 12}}
                       title='以下任务在代码中存在，但数据库中还没有对应记录。导入后按注解或 SPI 声明的默认配置创建；已存在的任务不会被修改。'/>
                <Table
                    rowKey='key'
                    size='small'
                    pagination={false}
                    dataSource={this.state.unregistered}
                    locale={{emptyText: '没有未注册的任务'}}
                    columns={[
                        {
                            title: '任务 / 执行类',
                            dataIndex: 'name',
                            render: (v, record) => <div style={{lineHeight: 1.45}}>
                                <div>{v || record.jobClassName}</div>
                                <div style={{
                                    fontSize: 12,
                                    color: 'var(--oa-color-text-tertiary, rgba(0, 0, 0, 0.45))',
                                    wordBreak: 'break-all'
                                }}>{record.jobClassName}</div>
                                {record.key !== record.jobClassName && <div style={{
                                    fontSize: 12,
                                    color: 'var(--oa-color-text-tertiary, rgba(0, 0, 0, 0.45))'
                                }}>key：{record.key}</div>}
                            </div>
                        },
                        {
                            title: 'cron 表达式',
                            dataIndex: 'cron',
                            width: 180,
                            render: v => v || '-'
                        },
                        {
                            title: '默认状态',
                            dataIndex: 'enabled',
                            width: 100,
                            render: v => v ? <Tag color='green'>启用</Tag> : <Tag>停用</Tag>
                        },
                        {
                            title: '操作',
                            dataIndex: 'option',
                            width: 80,
                            render: (_, record) => <a onClick={() => this.handleImport([record.key])}>导入</a>
                        }
                    ]}
                />
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



