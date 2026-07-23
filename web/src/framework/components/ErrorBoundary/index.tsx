import React from 'react';
import {Button, Collapse, Result} from 'antd';
import {ThemeUtils} from '../../utils';

interface Props {
    children: React.ReactNode;
    /** 自定义降级 UI */
    fallback?: React.ReactNode;
    /** 最小化模式（仅显示标题和刷新按钮，不显示错误详情） */
    minimal?: boolean;
    /** 错误回调，可用于上报 */
    onError?: (error: Error, errorInfo: React.ErrorInfo) => void;
}

interface State {
    hasError: boolean;
    error: Error | null;
    errorInfo: React.ErrorInfo | null;
}

export default class ErrorBoundary extends React.Component<Props, State> {

    state: State = {
        hasError: false,
        error: null,
        errorInfo: null,
    };

    static getDerivedStateFromError(error: Error) {
        return {hasError: true, error};
    }

    componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
        this.setState({errorInfo});
        this.props.onError?.(error, errorInfo);
    }

    handleRetry = () => {
        this.setState({hasError: false, error: null, errorInfo: null});
    };

    render() {
        if (!this.state.hasError) {
            return this.props.children;
        }

        if (this.props.fallback) {
            return this.props.fallback;
        }

        // minimal 模式：简洁降级，只引导刷新
        if (this.props.minimal) {
            return (
                <Result
                    status="error"
                    title="应用出现异常"
                    subTitle="页面渲染时发生错误，请尝试刷新页面。"
                    extra={[
                        <Button key="refresh" onClick={() => window.location.reload()}>
                            刷新页面
                        </Button>,
                    ]}
                />
            );
        }

        const detailStyle: React.CSSProperties = {
            maxHeight: 300, overflow: 'auto',
            fontSize: 12, fontFamily: 'monospace',
            whiteSpace: 'pre-wrap', wordBreak: 'break-all',
            background: '#f5f5f5', padding: 12, borderRadius: 4,
            color: ThemeUtils.getColor('error-color') || '#ff4d4f',
        };

        return (
            <Result
                status="error"
                title="页面渲染异常"
                subTitle="组件渲染时发生错误，请尝试刷新页面。"
                extra={[
                    <Button key="retry" type="primary" onClick={this.handleRetry}>
                        重试
                    </Button>,
                    <Button key="refresh" onClick={() => window.location.reload()}>
                        刷新页面
                    </Button>,
                ]}
            >
                <Collapse
                    size="small"
                    items={[{
                        key: 'detail',
                        label: '错误详情',
                        children: (
                            <div style={detailStyle}>
                                {this.state.error?.stack || String(this.state.error)}
                            </div>
                        ),
                    }]}
                />
            </Result>
        );
    }
}
