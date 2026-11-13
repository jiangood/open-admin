import React from 'react'
import { Drawer } from 'antd'
import { TaskLogViewer, TaskLogTransport } from '../TaskLogViewer'

export interface TaskLogDrawerProps {
    open: boolean
    logId?: string | null
    title?: string
    /** 任务是否运行中 */
    running?: boolean
    width?: number | string
    /** 传输方式：默认 http */
    transport?: TaskLogTransport
    /** WebSocket 地址（transport 为 ws/auto 时使用） */
    wsUrl?: string
    onClose?: () => void
    /** 任务结束回调（ws 结束或外部触发） */
    onEnd?: () => void
}

/**
 * 通用任务日志抽屉：按 logId 查看任务日志。
 * <p>
 * HTTP 模式走 logviewer（logKey 默认 task/{logId}）；WS 模式连接 wsUrl 实时 tail。
 * 权限按任务记录的 permission 字段鉴权。
 */
export default class TaskLogDrawer extends React.Component<TaskLogDrawerProps> {
    render() {
        const { open, logId, title, onClose, onEnd, width, running, transport, wsUrl } = this.props
        const bodyHeight = typeof window !== 'undefined' ? window.innerHeight - 55 : 600
        return <Drawer title={title || '任务日志'} size={width || 860} open={open}
                       mask={{ closable: false }}
                       onClose={onClose}
                       styles={{ body: { padding: 12, height: bodyHeight, overflow: 'hidden' } }}>
            {open && logId
                ? <TaskLogViewer key={logId + '-' + (transport || 'http')}
                                 logId={logId}
                                 running={!!running}
                                 transport={transport || 'http'}
                                 wsUrl={wsUrl}
                                 onEnd={onEnd}
                                 height={bodyHeight - 24}/>
                : null}
        </Drawer>
    }
}
