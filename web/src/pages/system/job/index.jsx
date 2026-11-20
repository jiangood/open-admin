import React from 'react'
import {
    Alert,
    Button,
    Form,
    Input,
    Modal,
    Select,
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
    getMessageApi,
    HttpClient,
    Page,
    PageUtils,
    PermActions,
    PermUtils,
    ProTable,
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

const statBarStyle = {
    marginBottom: 8,
    height: 56,
    padding: '0 16px',
    display: 'flex',
    alignItems: 'center',
    boxSizing: 'border-box',
    border: '1px solid var(--oa-color-border, #f0f0f0)',
    borderRadius: 8,
    background: 'var(--oa-color-bg-container, #fff)',
}

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

        cronDesc: '',

        importOpen: false,
        unregistered: [],
        importLoading: false,

        // 顶部卡片：统计 / 日志占用
        stat: null,
        logSize: null,
    }
    tableRef = React.createRef()
    modalRef = React.createRef()
    statTimer = null

    componentDidMount() {
        HttpClient.get('admin/job/job-class-options', null, {autoShowErrorMessage: false}).then(rs => {
            this.setState({jobClassOptions: rs.data})
        }).catch(e => {
            console.error('[Job] 加载任务类选项失败:', e);
        })
        this.startStatPolling()
    }

    componentWillUnmount() {
        this.stopStatPolling()
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
            HttpClient.get('admin/job/stat', null, {autoShowErrorMessage: false}),
            HttpClient.get('admin/job/log-size', null, {autoShowErrorMessage: false}),
        ]).then(([statResult, logSizeResult]) => {
            this.setState({stat: statResult.data, logSize: logSizeResult.data})
        }).catch(e => {
            console.error('[Job] 加载任务统计失败:', e);
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
        HttpClient.post("admin/job/get-job-param-fields", jobData || {}, {className}, {autoShowErrorMessage: false}).then(rs => {
            this.setState({paramList: rs.data})
        }).catch(e => {
            console.error('[Job] 加载任务参数字段失败:', e);
        })
    }

    onFinish = async values => {
        const isNew = !values.id;
        const url = isNew ? 'admin/job/create' : 'admin/job/update';
        const rs = await HttpClient.post(url, values)
        getMessageApi().success(rs?.message || (isNew ? '新增成功' : '更新成功'))
        this.tableRef.current.reload()
        this.loadStat()
    }

    handleDelete = row => {
        const hide = getMessageApi().loading("删除任务中...")
        HttpClient.post('admin/job/delete', {id: row.id}, null, {autoShowErrorMessage: false}).then((rs) => {
            hide();
            getMessageApi().success(rs?.message || '删除成功')
            this.tableRef.current.reload();
            this.loadStat()
        }).catch(e => {
            console.error('[Job] 删除任务失败:', e);
            hide();
        })
    }

    showImport = () => {
        this.setState({importOpen: true})
        this.loadUnregistered()
    }

    loadUnregistered = () => {
        HttpClient.get('admin/job/unregistered', null, {autoShowErrorMessage: false}).then(rs => {
            this.setState({unregistered: rs.data || []})
        }).catch(e => {
            console.error('[Job] 加载未注册任务失败:', e);
        })
    }

    handleImport = keys => {
        this.setState({importLoading: true})
        HttpClient.post('admin/job/import', {keys: keys && keys.length ? keys : null}, null, {autoShowErrorMessage: false}).then(rs => {
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
            render: (v, record) => v
                ? <a style={{overflowWrap: 'break-word'}} onClick={() => PageUtils.open(`/system/job/${record.id}`)}>{v}</a>
                : '-'
        },

        {
            title: '执行类',
            dataIndex: 'jobClass',
            render: v => {
                if (!v) {
                    return '-';
                }
                const simpleName = v.substring(v.lastIndexOf('.') + 1);
                return <Tooltip title={v}>
                    <span style={{
                        fontSize: 12,
                        color: 'var(--oa-color-text-tertiary, rgba(0, 0, 0, 0.45))',
                        wordBreak: 'break-all'
                    }}>{simpleName}</span>
                </Tooltip>;
            }
        },

        {
            title: 'cron 表达式',
            dataIndex: 'cron',
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
            render: v => v ? <Tag color='green'>启用</Tag> : <Tag color='red'>停用</Tag>
        },

        {
            title: '操作',
            dataIndex: 'option',
            fixed: 'right',
            render: (_, record) => {

                return <PermActions
                    size="small"
                    actions={[
                        {label: '详情', type: 'primary', perm: 'job:read', onClick: () => PageUtils.open(`/system/job/${record.id}`)},
                        {label: '编辑', perm: 'job:update', onClick: () => this.handleEdit(record)},
                        {label: '删除', perm: 'job:delete', confirm: '是否确定删除?', onClick: () => this.handleDelete(record)},
                    ]}
                />;
            },
        },

    ]

    /**
     * 顶部统计卡片：简要文字描述，重要信息用 Tag 标识
     */
    renderStatCards() {
        const {stat, logSize} = this.state
        if (!stat || !logSize || !PermUtils.hasPermission('job:read')) {
            return null
        }
        const scheduler = stat.scheduler || {}
        const logWarn = !!logSize.warn
        const warnTip = logSize.warnMb
            ? `已超过告警阈值 ${logSize.warnMb} MB，可缩短 sys.job-log-retain-days 以减少日志占用。`
            : null
        const schedulerTip = scheduler.error ? null
            : <div style={{lineHeight: 1.7, fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace'}}>
                <div>schedulerName: {scheduler.schedulerName || '-'}</div>
                <div>instanceId: {scheduler.instanceId || '-'}</div>
                <div>runningSince: {scheduler.runningSince || '-'}</div>
                <div>jobStoreClass: {scheduler.jobStoreClass || '-'}</div>
                <div>persistent: {String(!!scheduler.persistent)} · clustered: {String(!!scheduler.clustered)}</div>
                <div>threadPoolSize: {scheduler.threadPoolSize ?? '-'} · jobsExecuted: {scheduler.jobsExecuted ?? 0}</div>
            </div>

        const schedulerStatus = scheduler.error ? '异常' : (scheduler.standby ? '待机' : (scheduler.started ? '运行中' : '未启动'))

        return <div style={statBarStyle}>
            <div style={{fontSize: 14, lineHeight: '22px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', width: '100%'}}>
                今日 {stat.todayExecuteCount} 次
                {stat.todayFailCount > 0 && <Tag color="error" style={{marginLeft: 4}}>失败 {stat.todayFailCount}</Tag>}
                <span style={{margin: '0 8px', color: 'var(--oa-color-text-quaternary, rgba(0,0,0,0.25))'}}>|</span>
                日志 {formatBytes(logSize.bytes)} · {logSize.files} 文件
                {logWarn && <Tooltip title={warnTip}><Tag color="error" style={{marginLeft: 4}}>超限</Tag></Tooltip>}
                <span style={{margin: '0 8px', color: 'var(--oa-color-text-quaternary, rgba(0,0,0,0.25))'}}>|</span>
                线程 <Tag color="blue">{scheduler.threadPoolSize ?? '-'}</Tag>
                {scheduler.error
                    ? <Tag color="error" style={{marginLeft: 4}}>异常</Tag>
                    : <Tooltip title={schedulerTip}><Tag color={scheduler.started && !scheduler.standby ? 'success' : 'default'} style={{marginLeft: 4}}>{schedulerStatus}</Tag></Tooltip>}
            </div>
        </div>
    }

    render() {
        return <Page>
            {this.renderStatCards()}

            <ProTable
                actionRef={this.tableRef}
                searchInline
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

                {this.state.paramList?.map(p => (
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
                ))}
            </FormModal>

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
