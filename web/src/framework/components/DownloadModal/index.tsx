import React from "react";
import {Button, Modal, Progress, Tag} from "antd";
import {
  DownloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from "@ant-design/icons";
import axios from "axios";
import qs from 'qs';
import type {AxiosProgressEvent, AxiosRequestConfig, AxiosResponse} from "axios";

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_SERVER_SERVLET_CONTEXT_PATH,
  withCredentials: true,
  headers: {'Content-Type': 'application/json'},
  paramsSerializer: (params) => qs.stringify(params, {indices: false})
});

interface DownloadOptions {
  url: string;
  params?: Record<string, unknown>;
  data?: Record<string, unknown>;
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

export interface DownloadModalProps {
  title?: string;
  onFinish?: () => void;
}

export class DownloadModal extends React.Component<DownloadModalProps, ModalState> {
  private abortController: AbortController | null = null; // NOSONAR: 构造器中赋值
  private lastOptions: DownloadOptions | null = null;
  private lastTime: number = 0;
  private lastLoaded: number = 0;
  private speedTimer: ReturnType<typeof setInterval> | null = null;

  constructor(props: DownloadModalProps) {
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
  }

  download = (options: DownloadOptions) => { // NOSONAR: ref 暴露给父组件/业务项目调用的公共 API
    this.startDownload(options);
  };

  componentWillUnmount() {
    this.clearSpeedTimer();
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

  renderFooter = () => {
    const {status} = this.state;
    if (status === 'downloading') {
      return <Button danger onClick={this.handleCancel}>取消下载</Button>;
    }
    if (status === 'failed') {
      return (
        <>
          <Button onClick={this.handleClose}>关闭</Button>
          <Button type="primary" onClick={this.handleRetry}>重试</Button>
        </>
      );
    }
    return null;
  };

  private handleRetry = () => {
    if (this.lastOptions) {
      this.startDownload(this.lastOptions);
    }
  };

  private saveBlob(response: AxiosResponse) {
    return new Promise<void>((resolve, reject) => {
      const {data: blob, headers} = response;

      // 检查是否为 JSON 错误响应
      if (blob.type === 'application/json') {
        const reader = new FileReader();
        reader.readAsText(blob, 'utf-8');
        reader.onerror = function () {
        reject(new Error('读取下载数据失败'));
      };
      reader.onload = function () {
          try {
            const rs = JSON.parse(reader.result as string);
            reject(new Error(rs.message || '下载失败'));
          } catch {
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
        let parsedName = match?.[1] ? match[1].trim() : 'download.file';
        try {
          parsedName = decodeURIComponent(parsedName.replaceAll('"', ''));
        } catch {
          parsedName = parsedName.replaceAll('"', '');
        }
        filename = parsedName;
      }
      if (filename && filename !== this.state.fileName) {
        this.setState({fileName: filename});
      }

      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement('a');
      link.style.display = 'none';
      link.href = url;
      link.download = filename || 'download.file';
      document.body.appendChild(link);
      link.click();
      link.remove();
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

    const config: AxiosRequestConfig = {
      url: options.url,
      method: options.method || 'GET',
      responseType: 'blob',
      signal: this.abortController.signal,
      onDownloadProgress: (progressEvent: AxiosProgressEvent) => {
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
        this.setState((prev) => ({
          progress: total ? Math.round((loaded / total) * 100) : 0,
          loaded,
          total,
          speed: speed > 0 ? this.formatSpeed(speed) : prev.speed,
        }));
      },
    };

    if (options.params) {
      config.params = options.params;
    }
    if (options.data) {
      config.data = options.data;
    }

    axiosInstance(config).then((response) => {
      this.clearSpeedTimer();
      return this.saveBlob(response);
    }).then(() => {
      this.setState((prev) => ({
        status: 'completed',
        progress: 100,
        speed: '',
        loaded: prev.total || prev.loaded,
      }));
      this.props.onFinish?.();
    }).catch((error) => {
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
      console.error('[DownloadModal] 下载失败:', msg);
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
        title={<span><DownloadOutlined style={{marginRight: 8}}/>{this.props.title || '文件下载'}</span>}
        open={open}
        mask={{closable: false}}
        closable={status !== 'downloading'}
        onCancel={this.handleClose}
        footer={this.renderFooter()}
        destroyOnHidden
      >
        <div style={{padding: '20px 0'}}>
          {/* 文件名 */}
          <div style={{marginBottom: 16, fontSize: 15, fontWeight: 500, color: '#333'}}>
            文件名：{fileName || '未知文件'}
          </div>

          {/* 下载中 */}
          {status === 'downloading' && (
            <>
              <Progress percent={progress} status="active" strokeColor="var(--ant-color-primary)"/>
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
