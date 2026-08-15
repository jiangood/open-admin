import React from 'react';
import {Result} from 'antd';
import {matchRoute} from './matcher';
import {UrlUtils} from '../utils/UrlUtils';
import ErrorBoundary from '../components/ErrorBoundary';

interface PageFrameProps {
    url: string;
    show?: boolean;
}

export class PageFrame extends React.Component<PageFrameProps> {
    componentRef = React.createRef<{onShow?: () => void}>();

    componentDidMount() {
        this.callOnShowIfActive();
    }

    componentDidUpdate(prevProps: PageFrameProps) {
        if (!prevProps.show && this.props.show) {
            this.callOnShowIfActive();
        }
    }

    callOnShowIfActive() {
        if (this.props.show && this.componentRef.current?.onShow) {
            this.componentRef.current.onShow();
        }
    }

    render() {
        const {url} = this.props;
        const qIndex = url.indexOf('?');
        const pathname = qIndex === -1 ? url : url.substring(0, qIndex);
        const search = qIndex === -1 ? '' : url.substring(qIndex);

        const matched = matchRoute(pathname);

        let content;
        if (matched) {
            const {component: Comp, params} = matched;
            const location = {pathname, search, query: UrlUtils.getParams(url)};
            content = <Comp ref={this.componentRef} params={params} location={location}/>;
        } else {
            content = <Result status={404} title='页面不存在！' subTitle={<div>路由地址：{pathname}</div>}/>;
        }

        return <ErrorBoundary>{content}</ErrorBoundary>;
    }
}
