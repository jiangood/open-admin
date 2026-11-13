import React from 'react'
import { Drawer } from 'antd'
import { LogViewer } from '../LogViewer'

export interface TaskLogDrawerProps {
    open: boolean
    logId?: string | null
    title?: string
    /** 任务是否运行中；运行中时日志持续轮询，结束后补拉一次 */
    running?: boolean
    width?: number | string
    onClose?: () => void
    /** 任务结束回调（由外部在检测到 running 变为 false 后触发） */
    onEnd?: () => void
}

/**
 * 通用任务日志抽屉：按 logId 通过 logviewer HTTP 接口实时查看任务日志。
 * <p>
 * 日志 key 为 {@code task/{logId}}，权限按任务记录的 permission 字段鉴权。
 */
export default class TaskLogDrawer extends React.Component<TaskLogDrawerProps> {
    render() {
        const { open, logId, title, onClose, width, running } = this.props
        const bodyHeight = typeof window !== 'undefined' ? window.innerHeight - 55 : 600
        return <Drawer title={title || '任务日志'} size={width || 860} open={open}
                       mask={{ closable: false }}
                       onClose={onClose}
                       styles={{ body: { padding: 12, height: bodyHeight, overflow: 'hidden' } }}>
            {open && logId
                ? <LogViewer key={logId}
                             logKey={'task/' + logId}
                             running={!!running}
                             height={bodyHeight - 24}/>
                : null}
        </Drawer>
    }
}
