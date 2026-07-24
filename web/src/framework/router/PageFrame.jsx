import React from 'react';
import {Result} from 'antd';
import {matchRoute} from './matcher';
import {UrlUtils} from '../utils/UrlUtils';
import ErrorBoundary from '../components/ErrorBoundary';

/**
 * 类 iframe 组件：输入路由地址，渲染对应页面组件。
 * 不监听路由变化，url 不变则不重渲染；父级改 key 即重挂载（等价刷新）。
 */
export function PageFrame({url}) {
    const qIndex = url.indexOf('?');
    const pathname = qIndex === -1 ? url : url.substring(0, qIndex);
    const search = qIndex === -1 ? '' : url.substring(qIndex);

    const matched = matchRoute(pathname);

    let content;
    if (matched) {
        const {component: Comp, params} = matched;
        const location = {pathname, search, query: UrlUtils.getParams(url)};
        content = <Comp params={params} location={location}/>;
    } else {
        content = <Result status={404} title='页面不存在！' subTitle={<div>路由地址：{pathname}</div>}/>;
    }

    return <ErrorBoundary>{content}</ErrorBoundary>;
}
