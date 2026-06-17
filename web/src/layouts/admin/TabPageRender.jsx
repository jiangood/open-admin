import React, {useState, useEffect, useRef} from "react";
import {history} from "umi";
import {PageRender} from "../PageRender";
import {UrlUtils} from "../../framework";

const MAX_TABS = 20;

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
    const accessOrderRef = useRef([]);
    const lastClickTimeRef = useRef({});
    const activeKeyRef = useRef(activeKey);
    activeKeyRef.current = activeKey;
    const pathMenuMapRef = useRef(pathMenuMap);
    pathMenuMapRef.current = pathMenuMap;

    const recordAccess = (key) => {
        accessOrderRef.current = accessOrderRef.current.filter(k => k !== key);
        accessOrderRef.current.push(key);
    };

    // Use history.listen to monitor route changes
    useEffect(() => {
        const unlisten = history.listen(({location}) => {
            const {pathname, search} = location;
            const key = pathname + (search || '');
            recordAccess(key);

            setTabs(prev => {
                if (prev.find(t => t.key === key)) return prev;
                const content = <PageRender pathname={pathname}/>;
                const label = getLabel(pathname, search, pathMenuMapRef.current);
                const next = [...prev, {key, label, closable: true, content}];
                if (next.length > MAX_TABS) {
                    const keep = new Set([key, ...accessOrderRef.current.slice(-3)]);
                    const removeIdx = next.findIndex(t => !keep.has(t.key));
                    if (removeIdx > -1) next.splice(removeIdx, 1);
                }
                return next;
            });

            setActiveKey(key);
        });

        // Trigger for initial route
        const {pathname, search} = history.location;
        const key = pathname + (search || '');
        recordAccess(key);
        setTabs([{key, label: getLabel(pathname, search, pathMenuMap), closable: false, content: <PageRender pathname={pathname}/>}]);
        setActiveKey(key);

        return unlisten;
    }, []);

    // Listen for close-page-event (stable listener, no re-registration)
    useEffect(() => {
        const handler = (e) => {
            const url = e.detail.url;
            setTabs(prev => {
                const next = prev.filter(t => t.key !== url);
                accessOrderRef.current = accessOrderRef.current.filter(k => k !== url);
                if (url === activeKeyRef.current && next.length > 0) {
                    history.push(next[next.length - 1].key);
                }
                return next;
            });
        };
        document.addEventListener('close-page-event', handler);
        return () => document.removeEventListener('close-page-event', handler);
    }, []);

    const handleTabClick = (key) => {
        const now = Date.now();
        const last = lastClickTimeRef.current[key] || 0;
        lastClickTimeRef.current[key] = now;
        if (now - last < 300) {
            // Double click → refresh
            setTabs(prev => {
                const idx = prev.findIndex(t => t.key === key);
                if (idx === -1) return prev;
                const originalContent = prev[idx].content;
                const next = [...prev];
                next[idx] = {...next[idx], content: '刷新中...'};
                requestAnimationFrame(() => {
                    setTabs(prev2 => {
                        const idx2 = prev2.findIndex(t => t.key === key);
                        if (idx2 === -1) return prev2;
                        const next2 = [...prev2];
                        next2[idx2] = {...next2[idx2], content: originalContent};
                        return next2;
                    });
                });
                return next;
            });
        } else if (key !== activeKey) {
            history.push(key);
        }
    };

    const handleClose = (key, e) => {
        e.stopPropagation();
        setTabs(prev => {
            const next = prev.filter(t => t.key !== key);
            accessOrderRef.current = accessOrderRef.current.filter(k => k !== key);
            if (key === activeKeyRef.current && next.length > 0) {
                history.push(next[next.length - 1].key);
            }
            return next;
        });
    };

    if (tabs.length === 0) return null;

    return (
        <div className="oa-tabs">
            <div className="oa-tab-bar">
                {tabs.map(tab => (
                    <div
                        key={tab.key}
                        className={'oa-tab' + (tab.key === activeKey ? ' active' : '')}
                        onClick={() => handleTabClick(tab.key)}
                    >
                        <span className="oa-tab-label">{tab.label}</span>
                        {tab.closable && tabs.length > 1 && (
                            <span className="oa-tab-close" onClick={(e) => handleClose(tab.key, e)}>×</span>
                        )}
                    </div>
                ))}
            </div>
            <div className="oa-tabs-content">
                {tabs.map(tab => (
                    <div
                        key={tab.key}
                        style={{display: tab.key === activeKey ? undefined : 'none'}}
                        className="oa-tab-pane"
                    >
                        {tab.content}
                    </div>
                ))}
            </div>
        </div>
    );
}