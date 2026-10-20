import React from 'react';
import {CloseOutlined} from '@ant-design/icons';
import {ContextMenu} from '../../framework';
import './AdminTabs.less';

export class AdminTabs extends React.Component {
    state = {
        contextMenu: null,
        contextMenuTabKey: null,
    };

    barRef = React.createRef();
    activeTabRef = React.createRef();

    componentDidMount() {
        this.scrollActiveIntoView();
    }

    componentDidUpdate(prevProps) {
        if (prevProps.activeKey !== this.props.activeKey) {
            this.scrollActiveIntoView();
        }
    }

    /** 激活的标签被滚动到可视区外时（标签过多），横向带入视野 */
    scrollActiveIntoView = () => {
        const bar = this.barRef.current;
        const tab = this.activeTabRef.current;
        if (!bar || !tab) return;
        const margin = 8;
        const barRect = bar.getBoundingClientRect();
        const tabRect = tab.getBoundingClientRect();
        let delta = 0;
        if (tabRect.left < barRect.left + margin) {
            delta = tabRect.left - barRect.left - margin;
        } else if (tabRect.right > barRect.right - margin) {
            delta = tabRect.right - barRect.right + margin;
        }
        if (delta !== 0) {
            bar.scrollTo({left: bar.scrollLeft + delta, behavior: 'smooth'});
        }
    };

    handleContextMenu = (e, key) => {
        e.preventDefault();
        this.setState({
            contextMenu: {x: e.clientX, y: e.clientY},
            contextMenuTabKey: key,
        });
    };

    handleContextMenuClick = ({key: actionKey}) => {
        const tabKey = this.state.contextMenuTabKey;
        this.setState({contextMenu: null});
        const {onRefresh, onClose, onCloseOthers, onCloseAll} = this.props;
        if (actionKey === 'refresh') {
            onRefresh(tabKey);
        } else if (actionKey === 'close') {
            onClose(tabKey);
        } else if (actionKey === 'closeOthers') {
            onCloseOthers(tabKey);
        } else if (actionKey === 'closeAll') {
            onCloseAll();
        }
    };

    render() {
        const {tabs, activeKey, onChange, onClose} = this.props;
        const {contextMenu} = this.state;

        return (
            <>
                <div className="admin-tabs-bar" ref={this.barRef}>
                    {tabs.map(tab => (
                        <div
                            key={tab.key}
                            ref={tab.key === activeKey ? this.activeTabRef : null}
                            className={`admin-tab${tab.key === activeKey ? ' active' : ''}`}
                            role="tab"
                            title={tab.label}
                            tabIndex={tab.key === activeKey ? 0 : -1}
                            aria-selected={tab.key === activeKey}
                            onClick={() => onChange(tab.key)}
                            onKeyDown={e => {
                                if (e.key === 'Enter' || e.key === ' ') {
                                    e.preventDefault();
                                    onChange(tab.key);
                                }
                            }}
                            onContextMenu={e => this.handleContextMenu(e, tab.key)}
                        >
                            <span className="admin-tab-label">{tab.label}</span>
                            {tabs.length > 1 && (
                                <CloseOutlined
                                    className="admin-tab-close"
                                    onClick={e => {
                                        e.stopPropagation();
                                        onClose(tab.key);
                                    }}
                                />
                            )}
                        </div>
                    ))}
                </div>
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
                        onClick={this.handleContextMenuClick}
                        onClose={() => this.setState({contextMenu: null})}
                    />
                )}
            </>
        );
    }
}
