import React from 'react';
import {CloseOutlined, DownOutlined} from '@ant-design/icons';
import {Dropdown} from 'antd';
import {ContextMenu} from '../../framework';
import './AdminTabs.less';

export class AdminTabs extends React.Component {
    state = {
        contextMenu: null,
        contextMenuTabKey: null,
        overflowing: false,
    };

    barRef = React.createRef();
    activeTabRef = React.createRef();
    resizeObserver = null;

    componentDidMount() {
        this.scrollActiveIntoView();
        this.updateOverflow();
        this.observeOverflow();
        this.barRef.current?.addEventListener('wheel', this.handleWheel, {passive: false});
    }

    componentDidUpdate(prevProps) {
        if (prevProps.activeKey !== this.props.activeKey) {
            this.scrollActiveIntoView();
        }
        this.updateOverflow();
    }

    componentWillUnmount() {
        this.resizeObserver?.disconnect();
        this.barRef.current?.removeEventListener('wheel', this.handleWheel);
    }

    observeOverflow = () => {
        if (typeof ResizeObserver === 'undefined' || !this.barRef.current) return;
        this.resizeObserver = new ResizeObserver(() => {
            this.updateOverflow();
            // 窗口变窄后激活标签可能被挤出可视区，跟随重新定位
            this.scrollActiveIntoView();
        });
        this.resizeObserver.observe(this.barRef.current);
    };

    /** 标签总宽是否超过条带可用宽度（不含溢出时给按钮预留的 padding） */
    isOverflowing = () => {
        const bar = this.barRef.current;
        if (!bar) return false;
        const tabsWidth = [...bar.querySelectorAll('.admin-tab')]
            .reduce((sum, tab) => sum + tab.getBoundingClientRect().width, 0);
        // 基础左右 padding 各 8px；溢出时额外加的 40px 不参与判断，避免无法恢复
        return tabsWidth > bar.clientWidth - 16 + 1;
    };

    /** 标签超出可视宽度时显示右侧「全部标签」入口 */
    updateOverflow = () => {
        const overflowing = this.isOverflowing();
        if (overflowing !== this.state.overflowing) {
            this.setState({overflowing});
        }
    };

    /** 竖向滚轮在标签栏上转为横向滚动，避免标签被隐藏时无操作入口 */
    handleWheel = e => {
        const bar = this.barRef.current;
        if (!bar || !this.isOverflowing()) return;
        if (Math.abs(e.deltaY) <= Math.abs(e.deltaX)) return;
        bar.scrollLeft += e.deltaY;
        e.preventDefault();
    };

    /** 激活的标签被滚动到可视区外时（标签过多），横向带入视野 */
    scrollActiveIntoView = () => {
        const bar = this.barRef.current;
        const tab = this.activeTabRef.current;
        if (!bar || !tab) return;
        const margin = 8;
        // 溢出时右侧有「全部标签」按钮，避免激活标签滚到按钮下面
        const rightMargin = this.isOverflowing() ? 44 : margin;
        const barRect = bar.getBoundingClientRect();
        const tabRect = tab.getBoundingClientRect();
        let delta = 0;
        if (tabRect.left < barRect.left + margin) {
            delta = tabRect.left - barRect.left - margin;
        } else if (tabRect.right > barRect.right - rightMargin) {
            delta = tabRect.right - barRect.right + rightMargin;
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
        const {contextMenu, overflowing} = this.state;

        return (
            <>
                <div className={`admin-tabs-wrap${overflowing ? ' overflowing' : ''}`}>
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
                    {overflowing && (
                        <>
                            <div className="admin-tabs-fade"/>
                            <Dropdown
                                trigger={['click']}
                                placement="bottomRight"
                                menu={{
                                    items: tabs.map(tab => ({key: tab.key, label: tab.label})),
                                    selectable: true,
                                    selectedKeys: [activeKey],
                                    onClick: ({key}) => onChange(key),
                                    style: {maxHeight: 320, overflowY: 'auto'},
                                }}
                            >
                                <button type="button" className="admin-tabs-more" title="全部标签"
                                        aria-label="全部标签">
                                    <DownOutlined/>
                                </button>
                            </Dropdown>
                        </>
                    )}
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
