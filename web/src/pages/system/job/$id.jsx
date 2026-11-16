import React from 'react'
import {Alert, Button, Card, Descriptions, Empty, Spin, Tabs, Tag} from 'antd'
import {CaretRightOutlined} from '@ant-design/icons'
import {CronUtils, DictUtils, getMessageApi, HttpClient, Page, PageUtils, PermUtils, ProTable, TaskLogDrawer} from '../../../framework'

function formatBytes(bytes) {
    if (!bytes || bytes <= 0) {
        return '0 B'
    }
    const units = ['B', 'KB', 'MB', 'GB', 'TB']
    const k = 1024
    const index = Math.min(Math.floor(Math.log(bytes) / Math.log(k)), units.length - 1)
    return `${(bytes / Math.pow(k, index)).toFixed(index > 0 ? 1 : 0)} ${units[index]}`
}

export const pageMeta = {
    title: '任务详情',
}

export default class JobDetailPage extends React.Component {

    state = {
        job: null,
        loading: true,
        progress: null,
        jobLogSize: null,
        execStat: null,
        activeTab: 'records',
        logDrawerOpen: false,
        logDrawerId: null,
        logDrawerRunning: false,
        logDrawerTitle: '',
        triggering: false,
        interrupting: false,
    }

    progressTimer = null

    componentDidMount() {
        this.loadAll()
        this.startProgressPolling()
    }

    componentWillUnmount() {
        this.stopProgressPolling()
    }

    get jobId() {
        return this.props.params?.id
    }

    loadAll = () => {
        const {jobId} = this
        if (!jobId) {
            this.setState({loading: false})
            return
        }
        this.setState({loading: true})
        Promise.all([
            HttpClient.get(`admin/job/${jobId}`, null, {toastError: false}),
            HttpClient.get(`admin/job/progress/${jobId}`, null, {toastError: false}),
            HttpClient.get(`admin/job/${jobId}/log-size`, null, {toastError: false}),
            HttpClient.get(`admin/job/${jobId}/stat`, null, {toastError: false}),
        ]).then(([jobRs, progressRs, logSizeRs, statRs]) => {
            this.setState({
                job: jobRs.data,
                progress: progressRs.data,
                jobLogSize: logSizeRs.data,
                execStat: statRs.data,
                loading: false,
            }, () => this.syncLogDrawerRunning())
        }).catch(e => {
            console.error('[JobDetail] 加载任务失败:', e)
            this.setState({loading: false})
        })
    }

    loadJobMeta = () => {
        const {jobId} = this
        if (!jobId) return
        Promise.all([
            HttpClient.get(`admin/job/${jobId}`, null, {toastError: false}),
            HttpClient.get(`admin/job/${jobId}/log-size`, null, {toastError: false}),
            HttpClient.get(`admin/job/${jobId}/stat`, null, {toastError: false}),
        ]).then(([jobRs, logSizeRs, statRs]) => {
            this.setState({job: jobRs.data, jobLogSize: logSizeRs.data, execStat: statRs.data})
        }).catch(e => console.error('[JobDetail] 刷新任务信息失败:', e))
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
        const {jobId} = this
        if (!jobId) return
        HttpClient.get(`admin/job/progress/${jobId}`, null, {toastError: false}).then(rs => {
            this.setState({progress: rs.data}, () => this.syncLogDrawerRunning())
        }).catch(e => console.error('[JobDetail] 加载任务进度失败:', e))
    }

    syncLogDrawerRunning = () => {
        const {progress, logDrawerOpen, logDrawerId} = this.state
        if (!logDrawerOpen || !logDrawerId) return
        const running = !!(progress && progress.state === 'RUNNING' && progress.logId === logDrawerId)
        if (running !== this.state.logDrawerRunning) {
            this.setState({logDrawerRunning: running})
        }
    }

    handleInterrupt = () => {
        const {jobId} = this
        if (!jobId) return
        this.setState({interrupting: true})
        HttpClient.post('admin/job/interrupt', {id: jobId}, null, {toastError: false}).then(rs => {
            getMessageApi().success(rs.msg || '已发送取消请求')
            this.loadProgress()
            setTimeout(() => this.loadProgress(), 800)
        }).catch(e => {
            console.error('[JobDetail] 取消任务失败:', e)
        }).finally(() => {
            this.setState({interrupting: false})
        })
    }

    openLatestLog = () => {
        const {jobId} = this
        const {progress} = this.state
        if (progress && progress.state === 'RUNNING' && progress.logId) {
            this.setState({
                logDrawerOpen: true,
                logDrawerId: progress.logId,
                logDrawerRunning: true,
                logDrawerTitle: '最新日志',
            })
            return
        }
        HttpClient.get('admin/job/execute-record', {jobId, current: 1, pageSize: 1}, {toastError: false}).then(rs => {
            const data = rs.data || {}
            const records = data.records || data.list || data.content || (Array.isArray(data) ? data : [])
            const latest = records[0]
            if (latest) {
                this.setState({
                    logDrawerOpen: true,
                    logDrawerId: latest.id,
                    logDrawerRunning: this.isLogRunning(latest.id),
                    logDrawerTitle: latest.beginTime ? `最新日志 - ${latest.beginTime}` : '最新日志',
                })
            } else {
                getMessageApi().info('暂无执行日志')
            }
        }).catch(e => {
            console.error('[JobDetail] 加载最新执行记录失败:', e)
            getMessageApi().error('加载最新日志失败')
        })
    }

    isLogRunning = (logId) => {
        const {progress} = this.state
        return !!(logId && progress && progress.state === 'RUNNING' && progress.logId === logId)
    }

    openLogDrawer = (record) => {
        const id = record && record.id
        if (!id) return
        this.setState({
            logDrawerOpen: true,
            logDrawerId: id,
            logDrawerRunning: this.isLogRunning(id),
            logDrawerTitle: record.beginTime ? `任务日志 - ${record.beginTime}` : '任务日志',
        })
    }

    closeLogDrawer = () => {
        this.setState({logDrawerOpen: false, logDrawerId: null, logDrawerRunning: false, logDrawerTitle: ''})
    }

    handleTrigger = () => {
        const {jobId} = this
        if (!jobId) return
        this.setState({triggering: true})
        HttpClient.post('admin/job/trigger-job', {id: jobId}, null, {toastError: false}).then(() => {
            getMessageApi().success('执行命令已发送')
            this.loadProgress()
            setTimeout(() => this.loadProgress(), 800)
            setTimeout(() => this.loadProgress(), 2000)
        }).catch(e => {
            console.error('[JobDetail] 触发任务失败:', e)
        }).finally(() => {
            this.setState({triggering: false})
        })
    }

    renderRecordsTab = () => {
        const {jobId} = this
        if (!jobId) return null
        const columns = [
            {title: '开始时间', dataIndex: 'beginTime', width: 170, render: v => v || '-'},
            {title: '结束时间', dataIndex: 'endTime', width: 170, render: (v, record) => v || (record.beginTime ? '运行中…' : '-')},
            {
                title: '成功/失败',
                dataIndex: 'executeCount',
                width: 150,
                render: (v, record) => {
                    const count = v == null ? 1 : v
                    const failCount = record.failCount == null ? (record.success === false ? 1 : 0) : record.failCount
                    const successCount = Math.max(count - failCount, 0)
                    if (successCount === 0 && failCount === 0) return '-'
                    return <span>
                        {successCount > 0 && <Tag color='success' style={{marginRight: 4}}>成功 {successCount}</Tag>}
                        {failCount > 0 && <Tag color='error' style={{marginRight: 0}}>失败 {failCount}</Tag>}
                    </span>
                },
            },
            {title: '返回结果', dataIndex: 'result', render: v => (v == null || v === '') ? '-' : <span style={{wordBreak: 'break-all'}}>{v}</span>},
            {
                title: '操作', dataIndex: 'option', width: 80, fixed: 'right',
                render: (_, record) => <Button size="small" onClick={() => this.openLogDrawer(record)}>日志</Button>,
            },
        ]
        return <ProTable
            rowKey="id"
            scrollX={800}
            columns={columns}
            request={(params) => {
                params.jobId = jobId
                return HttpClient.get('admin/job/execute-record', params)
            }}
        />
    }

    renderConfigTab = () => {
        const {job} = this.state
        if (!job) return null
        const cronDesc = job.cron ? CronUtils.describe(job.cron) : null
        let jobDataNode = '-'
        const {jobData} = job
        if (jobData && typeof jobData === 'object' && Object.keys(jobData).length > 0) {
            jobDataNode = <pre style={{
                margin: 0,
                padding: 12,
                background: 'var(--oa-color-fill-quaternary, #fafafa)',
                borderRadius: 6,
                maxHeight: 320,
                overflow: 'auto',
                fontSize: 12,
                lineHeight: 1.6,
                fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace',
                whiteSpace: 'pre-wrap',
                wordBreak: 'break-all',
            }}>{JSON.stringify(jobData, null, 2)}</pre>
        } else if (jobData && typeof jobData === 'string' && jobData.trim()) {
            jobDataNode = <pre style={{
                margin: 0, padding: 12, background: 'var(--oa-color-fill-quaternary, #fafafa)', borderRadius: 6,
                maxHeight: 320, overflow: 'auto', fontSize: 12, lineHeight: 1.6, whiteSpace: 'pre-wrap', wordBreak: 'break-all',
            }}>{jobData}</pre>
        }

        return <Descriptions bordered size="small" column={1} styles={{label: {width: 160}}}>
            <Descriptions.Item label="任务名称">{job.name || '-'}</Descriptions.Item>
            <Descriptions.Item label="执行类">
                <span style={{wordBreak: 'break-all', fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace'}}>
                    {job.jobClass || job.jobClassName || '-'}
                </span>
            </Descriptions.Item>
            <Descriptions.Item label="cron 表达式">
                <div>
                    <span style={{fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace'}}>{job.cron || '-'}</span>
                    {cronDesc ? <div style={{marginTop: 4, color: 'var(--oa-color-text-secondary, rgba(0, 0, 0, 0.65))'}}>解析：{cronDesc}</div> : null}
                </div>
            </Descriptions.Item>
            <Descriptions.Item label="启用">
                {job.enabled ? <Tag color="green">启用</Tag> : <Tag color="red">停用</Tag>}
            </Descriptions.Item>
            <Descriptions.Item label="记录方式">
                {DictUtils.dictLabel('jobLogAggregation', job.logAggregation) || '每次一份记录'}
            </Descriptions.Item>
            <Descriptions.Item label="任务参数（jobData）">{jobDataNode}</Descriptions.Item>
        </Descriptions>
    }

    renderStatTab = () => {
        const {execStat} = this.state
        if (!execStat) {
            return <Empty description="暂无统计数据" style={{padding: '32px 0'}}/>
        }
        const runTimeText = (ms) => {
            if (ms == null) return '-'
            if (ms < 1000) return `${ms} ms`
            return `${(ms / 1000).toFixed(ms >= 10000 ? 0 : 2)} s`
        }
        return <Descriptions bordered size="small" column={2} styles={{label: {width: 160}}}>
            <Descriptions.Item label="执行记录条数">{execStat.recordCount ?? 0}</Descriptions.Item>
            <Descriptions.Item label="累计执行次数">{execStat.totalExecuteCount ?? 0}</Descriptions.Item>
            <Descriptions.Item label="累计失败次数">
                {(execStat.totalFailCount ?? 0) > 0
                    ? <span style={{color: 'var(--oa-color-error, #ff4d4f)'}}>{execStat.totalFailCount}</span>
                    : (execStat.totalFailCount ?? 0)}
            </Descriptions.Item>
            <Descriptions.Item label="累计成功次数">
                {Math.max((execStat.totalExecuteCount ?? 0) - (execStat.totalFailCount ?? 0), 0)}
            </Descriptions.Item>
            <Descriptions.Item label="最近执行开始">{execStat.lastBeginTime || '-'}</Descriptions.Item>
            <Descriptions.Item label="最近执行结束">{execStat.lastEndTime || (execStat.lastBeginTime ? '运行中…' : '-')}</Descriptions.Item>
            <Descriptions.Item label="最近执行结果">
                {execStat.lastSuccess == null ? '-' : (execStat.lastSuccess ? <Tag color="success">成功</Tag> : <Tag color="error">失败</Tag>)}
            </Descriptions.Item>
            <Descriptions.Item label="最近执行耗时">{runTimeText(execStat.lastRunTime)}</Descriptions.Item>
            <Descriptions.Item label="最近返回结果" span={2}>
                {execStat.lastResult ? <span style={{wordBreak: 'break-all'}}>{execStat.lastResult}</span> : '-'}
            </Descriptions.Item>
        </Descriptions>
    }

    render() {
        const {job, loading, progress, jobLogSize, activeTab, triggering, interrupting} = this.state
        if (loading) {
            return <Page><div style={{textAlign: 'center', padding: '48px 0'}}><Spin/></div></Page>
        }
        if (!job) {
            return <Page>
                <Empty description="任务不存在或已被删除">
                    <Button onClick={() => PageUtils.open('/system/job')}>返回列表</Button>
                </Empty>
            </Page>
        }

        const canTrigger = PermUtils.hasPermission('job:trigger')
        const running = progress && progress.state === 'RUNNING'

        return <Page>
            <Card style={{marginBottom: 12}}>
                <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 16, flexWrap: 'wrap'}}>
                    <div style={{flex: 1, minWidth: 320}}>
                        <div style={{fontSize: 18, fontWeight: 600, marginBottom: 12}}>{job.name || '-'}</div>
                        <Descriptions size="small" column={2}>
                            <Descriptions.Item label="执行类" span={2}>
                                <span style={{wordBreak: 'break-all'}}>{job.jobClassName || job.jobClass || '-'}</span>
                            </Descriptions.Item>
                            <Descriptions.Item label="cron 表达式">
                                <span style={{fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace'}}>{job.cron || '-'}</span>
                            </Descriptions.Item>
                            <Descriptions.Item label="启用">
                                {job.enabled ? <Tag color="green">启用</Tag> : <Tag color="red">停用</Tag>}
                            </Descriptions.Item>
                            <Descriptions.Item label="记录方式">
                                {DictUtils.dictLabel('jobLogAggregation', job.logAggregation) || '每次一份记录'}
                            </Descriptions.Item>
                            <Descriptions.Item label="日志占用">
                                {jobLogSize ? <span>{formatBytes(jobLogSize.bytes)}（{jobLogSize.files} 个文件）</span> : '-'}
                            </Descriptions.Item>
                        </Descriptions>
                    </div>
                    <div style={{display: 'flex', gap: 8}}>
                        {canTrigger && (
                            <Button type="primary" icon={<CaretRightOutlined/>}
                                    loading={triggering} onClick={this.handleTrigger}>
                                执行一次
                            </Button>
                        )}
                        {canTrigger && running && (
                            <Button danger loading={interrupting} onClick={this.handleInterrupt}>
                                取消执行
                            </Button>
                        )}
                        <Button onClick={this.openLatestLog}>最新日志</Button>
                    </div>
                </div>
            </Card>

            {running && (
                <Alert type="info" showIcon style={{marginBottom: 12}}
                       title={progress.message || '任务运行中'}/>
            )}

            <Card>
                <Tabs
                    activeKey={activeTab}
                    onChange={key => this.setState({activeTab: key})}
                    items={[
                        {key: 'records', label: '记录', children: this.renderRecordsTab()},
                        {key: 'stat', label: '统计', children: this.renderStatTab()},
                        {key: 'config', label: '配置', children: this.renderConfigTab()},
                    ]}
                />
            </Card>

            <TaskLogDrawer
                open={this.state.logDrawerOpen}
                logKey={this.state.logDrawerId ? 'job/' + this.state.logDrawerId : undefined}
                title={this.state.logDrawerTitle || '任务日志'}
                running={this.state.logDrawerRunning}
                width={900}
                onClose={this.closeLogDrawer}
            />
        </Page>
    }
}