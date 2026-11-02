import React from "react";
import {history, PageFrame, EventBus, matchRoute, setPageTitle} from "../../framework";
import {AdminTabs} from "./AdminTabs";

function getPathname(url) {
    const qIndex = url.indexOf('?');
    return qIndex === -1 ? url : url.substring(0, qIndex);
}

/** 移除已弃用的 _label 参数，避免旧 URL 产生重复 Tab；其余 query 原样保留 */
function normalizeUrl(url) {
    const qIndex = url.indexOf('?');
    if (qIndex === -1) return url;
    const params = new URLSearchParams(url.substring(qIndex));
    if (!params.has('_label')) return url;
    params.delete('_label');
    const search = params.toString();
    return search ? `${url.substring(0, qIndex)}?${search}` : url.substring(0, qIndex);
}

function getLabel(pathname, pathMenuMap) {
    if (pathname === '/' || pathname === '/index') return '首页';
    const route = matchRoute(pathname);
    // 页面显式声明的标题优先；未声明时回退到后端菜单名
    if (route?.meta?.title) return route.meta.title;
    const menu = pathMenuMap[pathname];
    if (menu) return menu.name;
    if (!route) return '页面不存在';
    return '未命名';
}

export class TabLayout extends React.Component {
    constructor(props) {
        super(props);
        const initUrl = normalizeUrl(history.location.pathname + history.location.search);
        const initLabel = getLabel(getPathname(initUrl), props.pathMenuMap);
        this.state = {
            tabs: [{key: initUrl, label: initLabel, refreshKey: 0}],
            activeKey: initUrl,
        };
        this.activeKey = initUrl;
    }

    unlisten = null;
    unsubscribeClosePage = null;

    openTab = (rawUrl) => {
        const url = normalizeUrl(rawUrl);
        this.setState(prev => ({
            tabs: prev.tabs.some(t => t.key === url) ? prev.tabs
                : [...prev.tabs, {key: url, label: getLabel(getPathname(url), this.props.pathMenuMap), refreshKey: 0}],
            activeKey: url,
        }));
    };

    closeTab = (key) => {
        this.setState(prev => {
            if (!prev.tabs.some(t => t.key === key)) return null;
            const next = prev.tabs.filter(t => t.key !== key);
            if (next.length === 0) {
                setTimeout(() => history.push('/'), 0);
            } else if (key === this.activeKey) {
                const fallback = next[next.length - 1].key;
                setTimeout(() => history.push(fallback), 0);
            }
            return {tabs: next};
        });
    };

    refreshTab = (key) => {
        this.setState(prev => ({
            tabs: prev.tabs.map(t => t.key === key ? {...t, refreshKey: t.refreshKey + 1} : t),
        }));
    };

    handleClosePageEvent = (url) => {
        this.closeTab(url);
    };

    /** 浏览器标题跟随当前激活标签 */
    syncDocumentTitle() {
        const {tabs, activeKey} = this.state;
        const active = tabs.find(t => t.key === activeKey);
        setPageTitle(active && active.label !== '未命名' ? active.label : undefined);
    }

    handleCloseOthers = (key) => {
        this.setState(prev => ({tabs: prev.tabs.filter(t => t.key === key)}));
        if (key !== this.activeKey) {
            history.push(key);
        }
    };

    handleCloseAll = () => {
        this.setState({tabs: []});
        setTimeout(() => history.push('/'), 0);
    };

    componentDidMount() {
        this.unlisten = history.listen(({location}) => {
            this.openTab(location.pathname + location.search);
        });
        this.unsubscribeClosePage = EventBus.on('closePage', this.handleClosePageEvent);
        this.syncDocumentTitle();
    }

    componentDidUpdate(prevProps) {
        this.activeKey = this.state.activeKey;
        // 菜单信息晚于 Tab 创建时才加载完成：路径/菜单映射变化后回填「未命名」Tab 标题
        if (this.props.pathMenuMap !== prevProps.pathMenuMap) {
            this.refreshTabLabels();
        }
        this.syncDocumentTitle();
    }

    refreshTabLabels() {
        this.setState(prev => {
            let changed = false;
            const tabs = prev.tabs.map(t => {
                const label = getLabel(getPathname(t.key), this.props.pathMenuMap);
                if (label === t.label) return t;
                changed = true;
                return {...t, label};
            });
            return changed ? {tabs} : null;
        });
    }

    componentWillUnmount() {
        if (this.unlisten) {
            this.unlisten();
        }
        if (this.unsubscribeClosePage) {
            this.unsubscribeClosePage();
        }
    }

    render() {
        const {tabs, activeKey} = this.state;

        return (
            <div style={{display: 'flex', flexDirection: 'column', height: '100%'}}>
                <AdminTabs
                    tabs={tabs.map(t => ({key: t.key, label: t.label}))}
                    activeKey={activeKey}
                    onChange={key => history.push(key)}
                    onClose={this.closeTab}
                    onRefresh={this.refreshTab}
                    onCloseOthers={this.handleCloseOthers}
                    onCloseAll={this.handleCloseAll}
                />
                <div style={{flex: 1, overflow: 'hidden'}}>
                    {tabs.map(t => (
                        <div key={t.key} style={{display: t.key === activeKey ? 'block' : 'none', height: '100%'}}>
                            <PageFrame key={t.refreshKey} url={t.key} show={t.key === activeKey}/>
                        </div>
                    ))}
                </div>
            </div>
        );
    }
}

