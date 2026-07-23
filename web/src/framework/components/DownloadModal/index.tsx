import React from "react";
import {Button, Modal, Progress, Tag} from "antd";
import {
  CloseOutlined,
  DownloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from "@ant-design/icons";
import axios from "axios";
import qs from 'qs';

const axiosInstance = axios.create({
  baseURL: (typeof SERVLET_CONTEXT !== 'undefined' && SERVLET_CONTEXT) || '',
  withCredentials: true,
  headers: {'Content-Type': 'application/json'},
  paramsSerializer: (params) => qs.stringify(params, {indices: false})
});

interface DownloadOptions {
  url: string;
  params?: Record<string, any>;
  data?: Record<string, any>;
  method?: 'GET' | 'POST';
  fileName?: string;
}

interface ModalState {
  open: boolean;
  status: '' | 'downloading' | 'completed' | 'failed';
  fileName: string;
  progress: number;
  loaded: number;
  total: number;
  speed: string;
  errorMessage: string;
}

export class DownloadModal extends React.Component<{}, ModalState> {
  static instance: DownloadModal | null = null;
  private abortController: AbortController | null = null;
  private lastOptions: DownloadOptions | null = null;
  private lastTime: number = 0;
  private lastLoaded: number = 0;
  private speedTimer: ReturnType<typeof setInterval> | null = null;

  constructor(props: {}) {
    super(props);
    this.state = {
      open: false,
      status: '',
      fileName: '',
      progress: 0,
      loaded: 0,
      total: 0,
      speed: '',
      errorMessage: '',
    };
    DownloadModal.instance = this;
  }

  static download(options: DownloadOptions) {
    const instance = DownloadModal.instance;
    if (instance) {
      instance.startDownload(options);
    }
  }

  componentWillUnmount() {
    this.clearSpeedTimer();
    DownloadModal.instance = null;
  }

  private clearSpeedTimer() {
    if (this.speedTimer) {
      clearInterval(this.speedTimer);
      this.speedTimer = null;
    }
  }

  private formatSize(bytes: number): string {
    if (bytes === 0) return '0 B';
    const units = ['B', 'KB', 'MB', 'GB'];
    const k = 1024;
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    const size = (bytes / Math.pow(k, i)).toFixed(i > 0 ? 1 : 0);
    return `${size} ${units[i]}`;
  }

  private formatSpeed(bytesPerSecond: number): string {
    return this.formatSize(bytesPerSecond) + '/s';
  }

  private handleCancel = () => {
    if (this.abortController) {
      this.abortController.abort();
      this.abortController = null;
    }
    this.clearSpeedTimer();
    this.setState({open: false, status: ''});
  };

  private handleClose = () => {
    this.setState({open: false, status: ''});
  };

  private handleRetry = () => {
    if (this.lastOptions) {
      this.startDownload(this.lastOptions);
    }
  };

  private saveBlob(response: any) {
    return new Promise<void>((resolve, reject) => {
      const {data: blob, headers} = response;

      // 检查是否为 JSON 错误响应
      if (blob.type === 'application/json') {
        const reader = new FileReader();
        reader.readAsText(blob, 'utf-8');
        reader.onload = function () {
          try {
            const rs = JSON.parse(reader.result as string);
            reject(new Error(rs.message || '下载失败'));
          } catch (e) {
            reject(new Error('解析错误响应失败'));
          }
        };
        return;
      }

      // 从 Content-Disposition 解析文件名
      const contentDisposition = headers['content-disposition'] || headers['Content-Disposition'];
      let filename = this.state.fileName;
      if (!filename && contentDisposition) {
        const match = /filename\*?=(?:['"]?)(?:UTF-8''|)(.+?)(?:['"]?$|;)/i.exec(contentDisposition);
        let parsedName = match && match[1] ? match[1].trim() : 'download.file';
        try {
          parsedName = decodeURIComponent(parsedName.replace(/"/g, ''));
        } catch (e) {
          parsedName = parsedName.replace(/"/g, '');
        }
        filename = parsedName;
      }

      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement('a');
      link.style.display = 'none';
      link.href = url;
      link.download = filename || 'download.file';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      resolve();
    });
  }

  startDownload(options: DownloadOptions) {
    // 取消之前的下载
    if (this.abortController) {
      this.abortController.abort();
    }

    this.lastOptions = options;
    this.abortController = new AbortController();
    this.lastTime = Date.now();
    this.lastLoaded = 0;

    this.setState({
      open: true,
      status: 'downloading',
      fileName: options.fileName || '',
      progress: 0,
      loaded: 0,
      total: 0,
      speed: '',
      errorMessage: '',
    });

    // 定时更新速度（当 total 未知时也能显示）
    this.speedTimer = setInterval(() => {
      const now = Date.now();
      const elapsed = (now - this.lastTime) / 1000;
      if (elapsed > 1 && this.lastLoaded > 0) {
        const speed = this.lastLoaded / elapsed;
        this.setState({speed: this.formatSpeed(speed)});
        this.lastTime = now;
        this.lastLoaded = 0;
      }
    }, 2000);

    const config: any = {
      url: options.url,
      method: options.method || 'GET',
      responseType: 'blob',
      signal: this.abortController.signal,
      onDownloadProgress: (progressEvent: any) => {
        const {loaded, total} = progressEvent;
        const now = Date.now();
        // 计算速度
        const elapsed = (now - this.lastTime) / 1000;
        let speed = 0;
        if (elapsed > 1) {
          speed = (loaded - this.lastLoaded) / elapsed;
          this.lastTime = now;
          this.lastLoaded = loaded;
        }
        this.setState({
          progress: total ? Math.round((loaded / total) * 100) : 0,
          loaded,
          total,
          speed: speed > 0 ? this.formatSpeed(speed) : this.state.speed,
        });
      },
    };

    if (options.params) {
      config.params = options.params;
    }
    if (options.data) {
      config.data = options.data;
    }

    axiosInstance(config).then((response: any) => {
      this.clearSpeedTimer();
      return this.saveBlob(response);
    }).then(() => {
      const total = this.state.total;
      this.setState({
        status: 'completed',
        progress: 100,
        speed: '',
        loaded: total || this.state.loaded,
      });
    }).catch((error: any) => {
      this.clearSpeedTimer();
      if (axios.isCancel(error)) {
        // 用户取消，不显示错误
        this.setState({open: false, status: ''});
        return;
      }
      let msg = '下载失败';
      if (error.message) {
        msg = error.message;
      }
      this.setState({
        status: 'failed',
        errorMessage: msg,
      });
    });
  }

  render() {
    const {open, status, fileName, progress, loaded, total, speed, errorMessage} = this.state;

    return (
      <Modal
        title={<span><DownloadOutlined style={{marginRight: 8}}/>文件下载</span>}
        open={open}
        maskClosable={false}
        closable={status !== 'downloading'}
        onCancel={this.handleClose}
        footer={
          status === 'downloading' ? (
            <Button danger onClick={this.handleCancel}>取消下载</Button>
          ) : status === 'failed' ? (
            <>
              <Button onClick={this.handleClose}>关闭</Button>
              <Button type="primary" onClick={this.handleRetry}>重试</Button>
            </>
          ) : null
        }
        destroyOnClose
      >
        <div style={{padding: '20px 0'}}>
          {/* 文件名 */}
          <div style={{marginBottom: 16, fontSize: 15, fontWeight: 500, color: '#333'}}>
            <DownloadOutlined style={{marginRight: 8}}/>
            {fileName || '未知文件'}
          </div>

          {/* 下载中 */}
          {status === 'downloading' && (
            <>
              <Progress percent={progress} status="active" strokeColor="#1677ff"/>
              <div style={{marginTop: 8, fontSize: 13, color: '#999'}}>
                <span>{this.formatSize(loaded)}</span>
                {total > 0 && <span> / {this.formatSize(total)}</span>}
                {speed && <span style={{marginLeft: 12}}>{speed}</span>}
              </div>
              <div style={{marginTop: 8}}>
                <Tag color="processing">下载中...</Tag>
              </div>
            </>
          )}

          {/* 已完成 */}
          {status === 'completed' && (
            <>
              <Progress percent={100} status="success"/>
              <div style={{marginTop: 8, fontSize: 13, color: '#999'}}>
                {loaded > 0 && <span>文件大小：{this.formatSize(loaded)}</span>}
              </div>
              <div style={{marginTop: 8}}>
                <Tag icon={<CheckCircleOutlined/>} color="success">下载完成</Tag>
              </div>
            </>
          )}

          {/* 失败 */}
          {status === 'failed' && (
            <>
              <Progress percent={progress} status="exception"/>
              <div style={{marginTop: 8, fontSize: 13, color: '#ff4d4f'}}>
                <CloseCircleOutlined style={{marginRight: 4}}/>
                {errorMessage}
              </div>
              <div style={{marginTop: 8}}>
                <Tag icon={<CloseCircleOutlined/>} color="error">下载失败</Tag>
              </div>
            </>
          )}
        </div>
      </Modal>
    );
  }
}
