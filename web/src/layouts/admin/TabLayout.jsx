import React from "react";
import {history, PageFrame, UrlUtils, EventBus} from "../../framework";
import {AdminTabs} from "./AdminTabs";

function getLabel(pathname, search, pathMenuMap) {
    if (pathname === '/') return '首页';
    const params = UrlUtils.getParams(search);
    if (params._label) return params._label;
    const menu = pathMenuMap[pathname];
    if (menu) return menu.name;
    return '未命名';
}

export class TabLayout extends React.Component {
    constructor(props) {
        super(props);
        const initUrl = history.location.pathname + history.location.search;
        const qIndex = initUrl.indexOf('?');
        const initPathname = qIndex === -1 ? initUrl : initUrl.substring(0, qIndex);
        const initSearch = qIndex === -1 ? '' : initUrl.substring(qIndex);
        const initLabel = getLabel(initPathname, initSearch, props.pathMenuMap);
        this.state = {
            tabs: [{key: initUrl, label: initLabel, refreshKey: 0}],
            activeKey: initUrl,
        };
        this.activeKey = initUrl;
    }

    unlisten = null;
    unsubscribeClosePage = null;

    openTab = (url) => {
        const qIndex = url.indexOf('?');
        const pathname = qIndex === -1 ? url : url.substring(0, qIndex);
        const search = qIndex === -1 ? '' : url.substring(qIndex);
        this.setState(prev => ({
            tabs: prev.tabs.some(t => t.key === url) ? prev.tabs
                : [...prev.tabs, {key: url, label: getLabel(pathname, search, this.props.pathMenuMap), refreshKey: 0}],
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
    }

    componentDidUpdate() {
        this.activeKey = this.state.activeKey;
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

