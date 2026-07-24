import React, {useState, useEffect, useRef} from "react";
import {Tabs} from "antd";
import {history, PageFrame, UrlUtils, ContextMenu} from "../../framework";

function getLabel(pathname, search, pathMenuMap) {
    if (pathname === '/') return '首页';
    const params = UrlUtils.getParams(search);
    if (params._label) return params._label;
    const menu = pathMenuMap[pathname];
    if (menu) return menu.name;
    return '未命名';
}

export function TabPageRender({pathMenuMap}) {
    const [tabs, setTabs] = useState([]);
    const [activeKey, setActiveKey] = useState(null);
    const [contextMenu, setContextMenu] = useState(null);
    const activeKeyRef = useRef(activeKey);
    activeKeyRef.current = activeKey;
    const pathMenuMapRef = useRef(pathMenuMap);
    pathMenuMapRef.current = pathMenuMap;

    useEffect(() => {
        const openTab = (url) => {
            const qIndex = url.indexOf('?');
            const pathname = qIndex === -1 ? url : url.substring(0, qIndex);
            const search = qIndex === -1 ? '' : url.substring(qIndex);
            setTabs(prev => prev.find(t => t.key === url) ? prev
                : [...prev, {key: url, label: getLabel(pathname, search, pathMenuMapRef.current), refreshKey: 0}]);
            setActiveKey(url);
        };
        const unlisten = history.listen(({location}) => openTab(location.pathname + location.search));
        openTab(history.location.pathname + history.location.search);
        return unlisten;
    }, []);

    const closeTab = (key) => {
        setTabs(prev => {
            if (!prev.find(t => t.key === key)) return prev;
            const next = prev.filter(t => t.key !== key);
            if (next.length === 0) {
                setTimeout(() => history.push('/'), 0);
            } else if (key === activeKeyRef.current) {
                const fallback = next[next.length - 1].key;
                setTimeout(() => history.push(fallback), 0);
            }
            return next;
        });
    };

    useEffect(() => {
        const handler = (e) => closeTab(e.detail.url);
        document.addEventListener('close-page-event', handler);
        return () => document.removeEventListener('close-page-event', handler);
    }, []);

    const refreshTab = (key) => {
        setTabs(prev => prev.map(t => t.key === key ? {...t, refreshKey: t.refreshKey + 1} : t));
    };

    const handleTabContextMenu = (e, key) => {
        e.preventDefault();
        setContextMenu({x: e.clientX, y: e.clientY, tabKey: key});
    };

    const handleContextMenuClick = ({key: actionKey}) => {
        const {tabKey} = contextMenu;
        setContextMenu(null);
        if (actionKey === 'refresh') {
            refreshTab(tabKey);
        } else if (actionKey === 'close') {
            closeTab(tabKey);
        } else if (actionKey === 'closeOthers') {
            setTabs(prev => prev.filter(t => t.key === tabKey));
            if (tabKey !== activeKeyRef.current) {
                history.push(tabKey);
            }
        } else if (actionKey === 'closeAll') {
            setTabs([]);
            setTimeout(() => history.push('/'), 0);
        }
    };

    if (tabs.length === 0) return null;

    return (
        <>
            <Tabs
                type="editable-card"
                hideAdd
                activeKey={activeKey}
                onChange={key => history.push(key)}
                onEdit={(key, action) => {
                    if (action === 'remove') closeTab(key);
                }}
                items={tabs.map(t => ({
                    key: t.key,
                    closable: tabs.length > 1,
                    label: <span onContextMenu={e => handleTabContextMenu(e, t.key)}>{t.label}</span>,
                    children: <PageFrame key={t.refreshKey} url={t.key}/>,
                }))}
            />
            {contextMenu && (
                <ContextMenu
                    x={contextMenu.x}
                    y={contextMenu.y}
                    items={[
                        {key: 'refresh', label: '刷新'},
                        {key: 'close', label: '关闭', disabled: tabs.length <= 1},
                        {key: 'closeOthers', label: '关闭其他', disabled: tabs.length <= 1},
                        {key: 'closeAll', label: '关闭全部'},
                    ]}
                    onClick={handleContextMenuClick}
                    onClose={() => setContextMenu(null)}
                />
            )}
        </>
    );
}