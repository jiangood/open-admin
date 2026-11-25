import React from 'react'
import {Button, Card, Col, Descriptions, Empty, Row, Space, Spin, Tabs, Tag, Tooltip} from 'antd'
import {CaretRightOutlined} from '@ant-design/icons'
import {CronUtils, getMessageApi, HttpClient, Page, PageUtils, PermUtils, ProTable, TaskLogDrawer} from '../../../framework'

const JOB_STATUS_META = {
    RUNNING: {label: '运行中', color: 'processing'},
    SUCCESS: {label: '成功', color: 'success'},
    PARTIAL: {label: '部分成功', color: 'warning'},
    SKIPPED: {label: '跳过', color: 'default'},
    FAILED: {label: '失败', color: 'error'},
    EXCEPTION: {label: '异常', color: 'volcano'},
    CANCELLED: {label: '已取消', color: 'default'},
}

function renderJobStatus(status) {
    const meta = JOB_STATUS_META[status]
    if (!meta) {
        return '-'
    }
    return <Tag color={meta.color}>{meta.label}</Tag>
}

/** 失败口径：部分成功 + 失败 + 异常 + 已取消 */
function failureTotal(stat) {
    if (!stat) {
        return 0
    }
    return (stat.partialCount || 0) + (stat.failCount || 0) + (stat.exceptionCount || 0) + (stat.cancelledCount || 0)
}

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
        jobLogSize: null,
        execStat: null,
        activeTab: 'records',
        logDrawerOpen: false,
        logDrawerId: null,
        logDrawerTitle: '',
        triggering: false,
    }

    recordsTableRef = React.createRef()

    componentDidMount() {
        this.loadAll()
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
            HttpClient.get(`admin/job/${jobId}`, null, {autoShowErrorMessage: false}),
            HttpClient.get(`admin/job/${jobId}/log-size`, null, {autoShowErrorMessage: false}),
            HttpClient.get(`admin/job/${jobId}/stat`, null, {autoShowErrorMessage: false}),
        ]).then(([jobRs, logSizeRs, statRs]) => {
            this.setState({
                job: jobRs.data,
                jobLogSize: logSizeRs.data,
                execStat: statRs.data,
                loading: false,
            })
        }).catch(e => {
            console.error('[JobDetail] 加载任务失败:', e)
            this.setState({loading: false})
        })
    }

    loadJobMeta = () => {
        const {jobId} = this
        if (!jobId) return
        Promise.all([
            HttpClient.get(`admin/job/${jobId}`, null, {autoShowErrorMessage: false}),
            HttpClient.get(`admin/job/${jobId}/log-size`, null, {autoShowErrorMessage: false}),
            HttpClient.get(`admin/job/${jobId}/stat`, null, {autoShowErrorMessage: false}),
        ]).then(([jobRs, logSizeRs, statRs]) => {
            this.setState({job: jobRs.data, jobLogSize: logSizeRs.data, execStat: statRs.data})
        }).catch(e => console.error('[JobDetail] 刷新任务信息失败:', e))
    }

    /** 刷新记录 Tab 表格（未挂载时跳过，如用户停留在统计/配置 Tab） */
    reloadRecords = () => {
        this.recordsTableRef?.current?.reload()
    }

    openLogDrawer = (record) => {
        const id = record && record.id
        if (!id) return
        this.setState({
            logDrawerOpen: true,
            logDrawerId: id,
            logDrawerTitle: record.beginTime ? `任务日志 - ${record.beginTime}` : '任务日志',
        })
    }

    closeLogDrawer = () => {
        this.setState({logDrawerOpen: false, logDrawerId: null, logDrawerTitle: ''})
    }

    handleTrigger = () => {
        const {jobId} = this
        if (!jobId) return
        this.setState({triggering: true})
        HttpClient.post('admin/job/trigger-job', {id: jobId}).then((rs) => {
            getMessageApi().success(rs?.message || '已触发执行')
            // Quartz 触发是异步的，执行记录不会立刻落库：延时刷新记录表和统计
            setTimeout(() => {
                this.reloadRecords()
                this.loadJobMeta()
            }, 800)
            setTimeout(() => {
                this.reloadRecords()
                this.loadJobMeta()
            }, 2000)
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
            {title: '结束时间', dataIndex: 'endTime', width: 170, render: (v, record) => {
                if (v) {
                    return v
                }
                return record.status === 'RUNNING' ? '运行中…' : '-'
            }},
            {title: '状态', dataIndex: 'status', width: 110, render: v => renderJobStatus(v)},
            {title: '返回结果', dataIndex: 'result', render: v => (v == null || v === '') ? '-' : <span style={{wordBreak: 'break-all'}}>{v}</span>},
            {
                title: '操作', dataIndex: 'option', width: 80, fixed: 'right',
                render: (_, record) => <Space size={4}>
                    <Button size="small" onClick={() => this.openLogDrawer(record)}>日志</Button>
                </Space>,
            },
        ]
        return <ProTable
            actionRef={this.recordsTableRef}
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
            <Descriptions.Item label="运行中">{execStat.runningCount ?? 0}</Descriptions.Item>
            <Descriptions.Item label="成功">{execStat.successCount ?? 0}</Descriptions.Item>
            <Descriptions.Item label="部分成功">
                {(execStat.partialCount ?? 0) > 0
                    ? <span style={{color: 'var(--oa-color-warning, #faad14)'}}>{execStat.partialCount}</span>
                    : (execStat.partialCount ?? 0)}
            </Descriptions.Item>
            <Descriptions.Item label="跳过">{execStat.skipCount ?? 0}</Descriptions.Item>
            <Descriptions.Item label="失败">
                {(execStat.failCount ?? 0) > 0
                    ? <span style={{color: 'var(--oa-color-error, #ff4d4f)'}}>{execStat.failCount}</span>
                    : (execStat.failCount ?? 0)}
            </Descriptions.Item>
            <Descriptions.Item label="异常">
                {(execStat.exceptionCount ?? 0) > 0
                    ? <span style={{color: 'var(--oa-color-error, #ff4d4f)'}}>{execStat.exceptionCount}</span>
                    : (execStat.exceptionCount ?? 0)}
            </Descriptions.Item>
            <Descriptions.Item label="已取消">{execStat.cancelledCount ?? 0}</Descriptions.Item>
            <Descriptions.Item label="最近执行开始">{execStat.lastBeginTime || '-'}</Descriptions.Item>
            <Descriptions.Item label="最近执行结束">
                {execStat.lastEndTime || (execStat.lastStatus === 'RUNNING' ? '运行中…' : '-')}
            </Descriptions.Item>
            <Descriptions.Item label="最近执行状态">{renderJobStatus(execStat.lastStatus)}</Descriptions.Item>
            <Descriptions.Item label="最近执行耗时">{runTimeText(execStat.lastRunTime)}</Descriptions.Item>
            <Descriptions.Item label="最近返回结果" span={2}>
                {execStat.lastResult ? <span style={{wordBreak: 'break-all'}}>{execStat.lastResult}</span> : '-'}
            </Descriptions.Item>
        </Descriptions>
    }

    render() {
        const {job, loading, jobLogSize, execStat, activeTab, triggering} = this.state
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

        return <Page>
            <Card style={{marginBottom: 12}}>
                <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 16, flexWrap: 'wrap', marginBottom: 16}}>
                    <div>
                        <div style={{fontSize: 18, fontWeight: 600, marginBottom: 8}}>{job.name || '-'}</div>
                        <Space size={8} wrap>
                            {job.enabled ? <Tag color="green">启用</Tag> : <Tag color="red">停用</Tag>}
                        </Space>
                    </div>
                    <div>
                        {canTrigger && (
                            <Button type="primary" icon={<CaretRightOutlined/>}
                                    loading={triggering} onClick={this.handleTrigger}>
                                执行一次
                            </Button>
                        )}
                    </div>
                </div>
                <Row gutter={[24, 12]}>
                    <Col xs={24} md={14}>
                        <Descriptions size="small" column={1} colon>
                            <Descriptions.Item label="执行类">
                                {(() => {
                                    const fullName = job.jobClass || job.jobClassName || '';
                                    if (!fullName) {
                                        return '-';
                                    }
                                    const simpleName = job.jobClass ? job.jobClass.substring(job.jobClass.lastIndexOf('.') + 1) : (job.jobClassName || fullName);
                                    return <Tooltip title={fullName}>
                                        <span style={{fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace'}}>
                                            {simpleName}
                                        </span>
                                    </Tooltip>;
                                })()}
                            </Descriptions.Item>
                            <Descriptions.Item label="cron 表达式">
                                <span style={{fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace'}}>{job.cron || '-'}</span>
                            </Descriptions.Item>
                        </Descriptions>
                    </Col>
                    <Col xs={24} md={10}>
                        <Descriptions size="small" column={2} colon>
                            <Descriptions.Item label="累计执行">
                                {execStat ? `${execStat.recordCount ?? 0} 次` : '-'}
                            </Descriptions.Item>
                            <Descriptions.Item label="累计失败">
                                {execStat
                                    ? failureTotal(execStat) > 0
                                        ? <span style={{color: 'var(--oa-color-error, #ff4d4f)'}}>{failureTotal(execStat)} 次</span>
                                        : '0 次'
                                    : '-'}
                            </Descriptions.Item>
                            <Descriptions.Item label="日志占用" span={2}>
                                {jobLogSize ? <span>{formatBytes(jobLogSize.bytes)}（{jobLogSize.files} 个文件）</span> : '-'}
                            </Descriptions.Item>
                        </Descriptions>
                    </Col>
                </Row>
            </Card>

            <Card>
                <Tabs
                    activeKey={activeTab}
                    onChange={key => this.setState({activeTab: key})}
                    items={[
                        {key: 'records', label: '执行记录', children: this.renderRecordsTab()},
                        {key: 'stat', label: '执行统计', children: this.renderStatTab()},
                        {key: 'config', label: '任务详情', children: this.renderConfigTab()},
                    ]}
                />
            </Card>

            <TaskLogDrawer
                open={this.state.logDrawerOpen}
                logKey={this.state.logDrawerId ? 'job/' + this.state.logDrawerId : undefined}
                title={this.state.logDrawerTitle || '任务日志'}
                running={false}
                width={900}
                onClose={this.closeLogDrawer}
            />
        </Page>
    }
}
