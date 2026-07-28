import React from 'react';
import {CloseOutlined} from '@ant-design/icons';
import {ContextMenu} from '../../framework';
import './AdminTabs.less';

export class AdminTabs extends React.Component {
    state = {
        contextMenu: null,
        contextMenuTabKey: null,
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
                <div className="admin-tabs-bar">
                    {tabs.map(tab => (
                        <div
                            key={tab.key}
                            className={`admin-tab${tab.key === activeKey ? ' active' : ''}`}
                            onClick={() => onChange(tab.key)}
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
