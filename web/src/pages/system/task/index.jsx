import React from 'react'
import {Button, Drawer, Form, Input, Modal, Select, Tag, Tooltip, message} from 'antd'
import {
    DateUtils,
    HttpClient,
    LogViewer,
    Page,
    PermActions,
    PermUtils,
    ProTable,
} from "../../../framework";

/**
 * 浠诲姟绫诲瀷鐨勫睍绀哄悕涓庨厤鑹层€備笌鍚庣 SysTaskLog.type 瀵瑰簲锛涙湭鐧昏鐨勭被鍨嬪洖閫€涓哄師鏍峰睍绀恒€? */
const TYPE_LABELS = {
    'agent-deploy': {label: 'Agent 閮ㄧ讲', color: 'purple'},
    deploy: {label: '搴旂敤閮ㄧ讲', color: 'blue'},
    build: {label: '椤圭洰鏋勫缓', color: 'geekblue'},
    'build-test': {label: '鏋勫缓娴嬭瘯', color: 'cyan'},
    'image-sync': {label: '闀滃儚鍚屾', color: 'green'},
    'image-export': {label: '闀滃儚瀵煎嚭', color: 'orange'},
    'image-import': {label: '闀滃儚瀵煎叆', color: 'gold'},
    'image-download': {label: '闀滃儚涓嬭浇', color: 'volcano'},
    'host-image-export': {label: '涓绘満闀滃儚瀵煎嚭', color: 'magenta'},
}

const STATUS_LABELS = {
    RUNNING: {label: '杩愯涓?, color: 'processing'},
    SUCCESS: {label: '鎴愬姛', color: 'success'},
    FAILED: {label: '澶辫触', color: 'error'},
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
 * 浠诲姟涓績锛氱粺涓€鏌ョ湅鍚庡彴闀夸换鍔＄殑鏃ュ織涓庡巻鍙茬粨鏋溿€? * <p>
 * 鏃ュ織姝ｆ枃鎸?logId 璧?logviewer锛坘ey = task/{logId}锛夛紝閴存潈鎸変换鍔¤褰曠殑 permission 瀛楁锛? * 杩愯涓殑浠诲姟鍙姹傚彇娑堬紙鍏抽棴浠诲姟鐧昏鐨勫彲涓柇璧勬簮锛夈€? * <p>
 * 杩愯涓殑浠诲姟鍦ㄩ〉闈㈠彲瑙佹椂姣?10 绉掑埛鏂颁竴娆″垪琛紝浠诲姟缁撴潫鐘舵€佷細鑷姩鍑虹幇銆? */
export const pageMeta = {
    title: '浠诲姟涓績',
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
            // 鎶藉眽鎵撳紑涓斾换鍔¤繍琛屼腑鏃讹紝鍚屾浠诲姟鐘舵€佷互渚挎棩蹇楄疆璇㈠湪缁撴潫鍚庡仠涓?            if (this.state.logVisible && this.state.logId && this.state.logRunning) {
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
        {title: '寮€濮嬫椂闂?, dataIndex: 'createTime', width: 170},
        {title: '绫诲瀷', dataIndex: 'type', width: 120, render: typeTagOf},
        {
            title: '浠诲姟', dataIndex: 'title', ellipsis: true,
            render: (v, row) => v || row.logId,
        },
        {title: '瑙﹀彂浜?, dataIndex: 'triggerUser', width: 110, render: v => v || '-'},
        {
            title: '鐘舵€?, dataIndex: 'status', width: 90,
            render: (_, row) => row.error
                ? <Tooltip title={row.error}>{statusTagOf(row.status)}</Tooltip>
                : statusTagOf(row.status),
        },
        {title: '缁撴潫鏃堕棿', dataIndex: 'endTime', width: 170, render: v => v || '-'},
        {
            title: '鑰楁椂', dataIndex: 'timeSpend', width: 100,
            render: (v, row) => row.status === 'RUNNING'
                ? '-'
                : v != null ? DateUtils.friendlyTotalTime(v) : '-',
        },
        {
            title: '鎿嶄綔', dataIndex: 'option', valueType: 'option', width: 150,
            render: (_, row) => <>
                <PermActions size='small' actions={[
                    {label: '鏌ョ湅鏃ュ織', perm: 'task:view', onClick: () => this.openLog(row)},
                ]}/>
                {row.status === 'RUNNING' && PermUtils.hasPermission('task:cancel') &&
                    <Button type='link' size='small' danger onClick={() => this.confirmCancel(row)}>取消</Button>}
            </>,
        },
    ]

    confirmCancel = row => {
        Modal.confirm({
            title: '确定取消该任务？',
            okText: '确定',
            cancelText: '取消',
            onOk: () => this.cancel(row),
        })
    }

    cancel = row => {
        return HttpClient.post('admin/sys/task/cancel', null, {logId: row.logId})
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
                              <Input placeholder='鎼滅储浠诲姟' allowClear/>
                          </Form.Item>
                          <Form.Item name='status' noStyle>
                              <Select placeholder='鐘舵€? allowClear style={{width: 120}} options={[
                                  {value: 'RUNNING', label: '杩愯涓?},
                                  {value: 'SUCCESS', label: '鎴愬姛'},
                                  {value: 'FAILED', label: '澶辫触'},
                              ]}/>
                          </Form.Item>
                      </>}
                      request={params => HttpClient.get('admin/sys/task/page', params)}
                      columns={this.columns}/>

            <Drawer title='任务日志' size={860} open={logVisible}
                    mask={{closable: false}}
                    onClose={this.closeLog}
                    styles={{body: {padding: 12, height: typeof window !== 'undefined' ? window.innerHeight - 55 : 600, overflow: 'hidden'}}}>
                {logVisible && logId
                    ? <LogViewer key={logId}
                                 logKey={'task/' + logId}
                                 running={logRunning}
                                 height={typeof window !== 'undefined' ? window.innerHeight - 79 : 500}/>
                    : null}
            </Drawer>
        </Page>
    }
}
