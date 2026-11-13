import React from 'react'
import {Form, Input, Popconfirm, Select, Tag, Tooltip, message} from 'antd'
import {
    DateUtils,
    HttpClient,
    LogViewer,
    Page,
    PermActions,
    PermUtils,
    ProTable,
} from "../../../framework";
import {Drawer} from 'antd'

/**
 * 任务类型的展示名与配色。与后端 SysTaskLog.type 对应；未登记的类型回退为原样展示。
 */
const TYPE_LABELS = {
    'agent-deploy': {label: 'Agent 部署', color: 'purple'},
    deploy: {label: '应用部署', color: 'blue'},
    build: {label: '项目构建', color: 'geekblue'},
    'build-test': {label: '构建测试', color: 'cyan'},
    'image-sync': {label: '镜像同步', color: 'green'},
    'image-export': {label: '镜像导出', color: 'orange'},
    'image-import': {label: '镜像导入', color: 'gold'},
    'image-download': {label: '镜像下载', color: 'volcano'},
    'host-image-export': {label: '主机镜像导出', color: 'magenta'},
}

const STATUS_LABELS = {
    RUNNING: {label: '运行中', color: 'processing'},
    SUCCESS: {label: '成功', color: 'success'},
    FAILED: {label: '失败', color: 'error'},
}

const typeTagOf = type => {
    const meta = TYPE_LABELS[type]
    return meta ? <Tag color={meta.color}>{meta.label}</Tag> : <Tag>{type}</Tag>
}

const statusTagOf = status => {
    const meta = STATUS_LABELS[status]
    return meta ? <Tag color={meta.color}>{meta.label}</Tag> : <Tag>{status}</Tag>
}

/**
 * 任务中心：统一查看后台长任务的日志与历史结果。
 * <p>
 * 日志正文按 logId 走 logviewer（key = task/{logId}），鉴权按任务记录的 permission 字段；
 * 运行中的任务可请求取消（关闭任务登记的可中断资源）。
 * <p>
 * 运行中的任务在页面可见时每 10 秒刷新一次列表，任务结束状态会自动出现。
 */
export const pageMeta = {
    title: '任务中心',
}

export default class extends React.Component {

    tableRef = React.createRef()

    state = {
        logId: null,
        logRunning: false,
        logVisible: false,
    }

    componentDidMount() {
        this.timer = setInterval(this.poll, 10000)
    }

    componentWillUnmount() {
        clearInterval(this.timer)
    }

    poll = () => {
        if (!document.hidden) {
            this.tableRef.current?.reload()
            // 抽屉打开且任务运行中时，同步任务状态以便日志轮询在结束后停下
            if (this.state.logVisible && this.state.logId && this.state.logRunning) {
                this.syncLogRunning()
            }
        }
    }

    syncLogRunning = () => {
        const {logId} = this.state
        if (!logId) return
        HttpClient.get('admin/sys/task/get', {logId}).then(rs => {
            const task = rs && rs.data
            if (task && task.status && task.status !== 'RUNNING') {
                this.setState({logRunning: false})
            }
        }).catch(() => {})
    }

    columns = [
        {title: '开始时间', dataIndex: 'createTime', width: 170},
        {title: '类型', dataIndex: 'type', width: 120, render: typeTagOf},
        {
            title: '任务', dataIndex: 'title', ellipsis: true,
            render: (v, row) => v || row.logId,
        },
        {title: '触发人', dataIndex: 'triggerUser', width: 110, render: v => v || '-'},
        {
            title: '状态', dataIndex: 'status', width: 90,
            render: (_, row) => row.error
                ? <Tooltip title={row.error}>{statusTagOf(row.status)}</Tooltip>
                : statusTagOf(row.status),
        },
        {title: '结束时间', dataIndex: 'endTime', width: 170, render: v => v || '-'},
        {
            title: '耗时', dataIndex: 'timeSpend', width: 100,
            render: (v, row) => row.status === 'RUNNING'
                ? '-'
                : v != null ? DateUtils.friendlyTotalTime(v) : '-',
        },
        {
            title: '操作', dataIndex: 'option', valueType: 'option', width: 150,
            render: (_, row) => <>
                <PermActions size='small' actions={[
                    {label: '查看日志', perm: 'task:view', onClick: () => this.openLog(row)},
                ]}/>
                {row.status === 'RUNNING' && PermUtils.hasPermission('task:cancel') &&
                    <Popconfirm title='确定取消该任务？' onConfirm={() => this.cancel(row)}>
                        <a>取消</a>
                    </Popconfirm>}
            </>,
        },
    ]

    cancel = row => {
        HttpClient.post('admin/sys/task/cancel', null, {logId: row.logId})
            .then(rs => {
                message.success((rs && rs.message) || '已请求取消')
            })
            .finally(() => this.reload())
    }

    reload = () => {
        this.tableRef.current?.reload()
    }

    openLog = row => {
        if (row && row.logId) {
            this.setState({logId: row.logId, logRunning: row.status === 'RUNNING', logVisible: true})
        }
    }

    closeLog = () => {
        this.setState({logVisible: false, logRunning: false})
        this.reload()
    }

    render() {
        const {logId, logVisible, logRunning} = this.state
        return <Page padding>
            <ProTable actionRef={this.tableRef}
                      searchInline
                      searchFormRender={() => <>
                          <Form.Item name='searchText' noStyle>
                              <Input placeholder='搜索任务' allowClear/>
                          </Form.Item>
                          <Form.Item name='status' noStyle>
                              <Select placeholder='状态' allowClear style={{width: 120}} options={[
                                  {value: 'RUNNING', label: '运行中'},
                                  {value: 'SUCCESS', label: '成功'},
                                  {value: 'FAILED', label: '失败'},
                              ]}/>
                          </Form.Item>
                      </>}
                      request={params => HttpClient.get('admin/sys/task/page', params)}
                      columns={this.columns}/>

            <Drawer title='任务日志' size={860} open={logVisible}
                    mask={{closable: false}}
                    onClose={this.closeLog}>
                {logVisible && logId
                    ? <LogViewer key={logId}
                                 logKey={'task/' + logId}
                                 running={logRunning}
                                 height={typeof window !== 'undefined' ? window.innerHeight - 160 : 500}/>
                    : null}
            </Drawer>
        </Page>
    }
}
