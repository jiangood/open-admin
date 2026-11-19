import React from 'react'
import { Button, Drawer } from 'antd'
import { FullscreenExitOutlined, FullscreenOutlined } from '@ant-design/icons'
import { TaskLogViewer, TaskLogTransport } from '../TaskLogViewer'
import { UrlUtils } from '../../utils'

export interface TaskLogDrawerProps {
    open: boolean
    logId?: string | null
    /** HTTP 模式日志 key；未指定时默认 task/{logId} */
    logKey?: string
    /** 自定义读取地址（需返回 {content,nextOffset,eof}） */
    url?: string
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
 * 通用任务日志抽屉：按 logId/logKey 查看任务日志。
 * <p>
 * HTTP 模式走 logviewer（logKey 未指定时默认 task/{logId}）；WS 模式连接 wsUrl 实时 tail。
 */
export default class TaskLogDrawer extends React.Component<TaskLogDrawerProps, { fullScreen: boolean }> {
    state = { fullScreen: false }

    /** 下载地址：优先使用显式 url，否则按 logKey/logId 拼 raw 日志接口 */
    private getDownloadUrl(): string | null {
        const { url, logKey, logId } = this.props
        if (url) return url
        const key = logKey || (logId ? 'task/' + logId : undefined)
        if (!key) return null
        return UrlUtils.contextPath('/admin/sys/log/raw/' + key)
    }

    private handleDownload = () => {
        const downloadUrl = this.getDownloadUrl()
        if (!downloadUrl) return
        const { logKey, logId } = this.props
        const key = logKey || (logId ? 'task/' + logId : 'log')
        const fileName = key.replace(/[\\/:*?"<>|]/g, '-') + '.log'
        const link = document.createElement('a')
        link.href = downloadUrl
        link.download = fileName
        link.style.display = 'none'
        document.body.appendChild(link)
        link.click()
        link.remove()
    }

    private toggleFullScreen = () => {
        this.setState(s => ({ fullScreen: !s.fullScreen }))
    }

    private handleClose = () => {
        this.setState({ fullScreen: false })
        this.props.onClose?.()
    }

    render() {
        const { open, logId, logKey, url, title, onEnd, width, running, transport, wsUrl } = this.props
        const { fullScreen } = this.state
        const bodyHeight = typeof window !== 'undefined' ? window.innerHeight - 55 : 600
        const hasTarget = !!(logId || logKey || url)
        const canDownload = !!this.getDownloadUrl()
        const drawerSize = fullScreen ? '100vw' : (width || 860)
        return <Drawer title={title || '任务日志'} size={drawerSize} open={open}
                       onClose={this.handleClose}
                       extra={
                           <div style={{display: 'flex', gap: 8, alignItems: 'center'}}>
                               <Button
                                   size="small"
                                   type="text"
                                   onClick={this.toggleFullScreen}
                                   title={fullScreen ? '退出全屏' : '全屏'}
                                   icon={fullScreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
                               />
                               {canDownload && (
                                   <Button size="small" type="text" onClick={this.handleDownload}>下载日志</Button>
                               )}
                               <Button size="small" type="text" onClick={this.handleClose}>关闭</Button>
                           </div>
                       }
                       styles={{ body: { padding: 12, height: bodyHeight, overflow: 'hidden' } }}>
            {open && hasTarget
                ? <TaskLogViewer key={(logKey || logId || url || '') + '-' + (transport || 'http')}
                                 logId={logId || undefined}
                                 logKey={logKey}
                                 url={url}
                                 running={!!running}
                                 transport={transport || 'http'}
                                 wsUrl={wsUrl}
                                 onEnd={onEnd}
                                 height={bodyHeight - 24}/>
                : null}
        </Drawer>
    }
}
