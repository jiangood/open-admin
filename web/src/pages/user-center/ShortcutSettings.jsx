import React from "react";
import {Button, Empty, Modal, Spin, Tag, Tree} from "antd";
import {DeleteOutlined, HolderOutlined, SettingOutlined} from "@ant-design/icons";
import {EventBus, getMessageApi, HttpClient, NamedIcon} from "../../framework";
import "./ShortcutSettings.less";

const MAX_SHORTCUTS = 10;

/**
 * 个人中心「快捷菜单」Tab。
 * <p>
 * 负责展示当前配置，并在弹窗中提供树形多选 + 拖拽排序。
 */
export default class ShortcutSettings extends React.Component {

    state = {
        shortcuts: [],
        loading: false,
        modalOpen: false,
        options: [],
        optionsLoading: false,
        selected: [],
        saving: false,
        dragIndex: -1,
    };

    componentDidMount() {
        this.loadShortcuts();
    }

    loadShortcuts = () => {
        this.setState({loading: true});
        return HttpClient.get('admin/user-center/shortcuts', null, {toastError: false})
            .then(rs => this.setState({shortcuts: rs.data || []}))
            .catch(() => this.setState({shortcuts: []}))
            .finally(() => this.setState({loading: false}));
    };

    loadOptions = () => {
        this.setState({optionsLoading: true});
        return HttpClient.get('admin/user-center/shortcut-options', null)
            .then(rs => this.setState({options: rs.data || []}))
            .finally(() => this.setState({optionsLoading: false}));
    };

    openModal = () => {
        this.setState({
            modalOpen: true,
            selected: this.state.shortcuts.map(item => ({...item})),
        });
        if (this.state.options.length === 0) {
            this.loadOptions();
        }
    };

    closeModal = () => {
        this.setState({modalOpen: false});
    };

    flattenOptions = () => {
        const map = new Map();
        const walk = (nodes) => {
            (nodes || []).forEach(node => {
                map.set(node.key, node);
                walk(node.children);
            });
        };
        walk(this.state.options);
        return map;
    };

    onCheck = (checkedKeys) => {
        const keys = Array.isArray(checkedKeys) ? checkedKeys : (checkedKeys?.checked || []);
        const optionMap = this.flattenOptions();
        const pageKeys = keys.filter(key => optionMap.get(key)?.isLeaf);

        if (pageKeys.length > MAX_SHORTCUTS) {
            getMessageApi().warning(`快捷菜单最多选择 ${MAX_SHORTCUTS} 个`);
            return;
        }

        const selected = this.state.selected;
        const selectedIds = new Set(selected.map(item => item.id));
        const next = selected.filter(item => pageKeys.includes(item.id));
        pageKeys.forEach(key => {
            if (!selectedIds.has(key)) {
                const node = optionMap.get(key);
                next.push({
                    id: node.key,
                    name: node.title,
                    path: node.path,
                    icon: node.icon,
                });
            }
        });
        this.setState({selected: next});
    };

    removeSelected = (id) => {
        this.setState(prevState => ({
            selected: prevState.selected.filter(item => item.id !== id),
        }));
    };

    onDragStart = (dragIndex) => {
        this.setState({dragIndex});
    };

    onDragOver = (event) => {
        // 必须阻止默认行为，否则浏览器不会触发 drop
        event.preventDefault();
    };

    onDrop = (dropIndex) => {
        const {dragIndex, selected} = this.state;
        if (dragIndex < 0 || dragIndex === dropIndex) {
            this.setState({dragIndex: -1});
            return;
        }
        const next = [...selected];
        const [moved] = next.splice(dragIndex, 1);
        next.splice(dropIndex, 0, moved);
        this.setState({selected: next, dragIndex: -1});
    };

    save = () => {
        const menuIds = this.state.selected.map(item => item.id);
        this.setState({saving: true});
        HttpClient.post('admin/user-center/shortcuts', {menuIds})
            .then(rs => {
                const shortcuts = rs.data || [];
                this.setState({
                    shortcuts,
                    modalOpen: false,
                    selected: shortcuts.map(item => ({...item})),
                });
                EventBus.emit('shortcutsChange');
            })
            .finally(() => {
                this.setState({saving: false});
            });
    };

    buildTreeData = (nodes) => (nodes || []).map(node => ({
        key: node.key,
        title: node.title,
        icon: node.icon ? <NamedIcon name={node.icon}/> : undefined,
        disableCheckbox: node.disableCheckbox,
        selectable: node.selectable !== false,
        isLeaf: node.isLeaf,
        children: node.children ? this.buildTreeData(node.children) : undefined,
    }));

    renderSelectedItem = (item, index) => {
        const {dragIndex} = this.state;
        return <div
            key={item.id}
            className={'shortcut-selected-item' + (dragIndex === index ? ' dragging' : '')}
            draggable
            onDragStart={(event) => {
                event.dataTransfer.effectAllowed = 'move';
                this.onDragStart(index);
            }}
            onDragOver={this.onDragOver}
            onDrop={(event) => {
                event.preventDefault();
                this.onDrop(index);
            }}
            onDragEnd={() => this.setState({dragIndex: -1})}
        >
            <HolderOutlined className='shortcut-drag-handle'/>
            {item.icon && <NamedIcon name={item.icon} className='shortcut-item-icon'/>}
            <span className='shortcut-item-name' title={item.name}>{item.name}</span>
            <Button type="text" size="small" danger
                    aria-label={`移除 ${item.name}`}
                    icon={<DeleteOutlined/>}
                    onClick={() => this.removeSelected(item.id)}/>
        </div>;
    };

    render() {
        const {
            shortcuts,
            loading,
            modalOpen,
            options,
            optionsLoading,
            selected,
            saving,
        } = this.state;

        return <>
            <div className='shortcut-tab'>
                <div className='shortcut-tab-head'>
                    <span className='shortcut-tab-tip'>
                        快捷菜单会固定显示在左侧菜单顶部，最多 {MAX_SHORTCUTS} 个，可拖拽调整顺序。
                    </span>
                    <Button type="primary" icon={<SettingOutlined/>} onClick={this.openModal}>
                        配置快捷菜单
                    </Button>
                </div>

                {shortcuts.length === 0
                    ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description='暂未配置快捷菜单'/>
                    : <div className='shortcut-tag-list'>
                        {shortcuts.map(item => (
                            <Tag key={item.id}
                                 icon={item.icon ? <NamedIcon name={item.icon}/> : undefined}>
                                {item.name}
                            </Tag>
                        ))}
                    </div>}
            </div>

            <Modal open={modalOpen}
                   title="配置快捷菜单"
                   width={820}
                   okText="保存"
                   cancelText="取消"
                   confirmLoading={saving || loading}
                   onCancel={this.closeModal}
                   onOk={this.save}
                   destroyOnHidden>
                <div className='shortcut-config'>
                    <div className='shortcut-config-pane'>
                        <div className='shortcut-config-title'>选择菜单</div>
                        <div className='shortcut-tree-box'>
                            {optionsLoading
                                ? <div className='shortcut-loading'><Spin size='small'/></div>
                                : options.length === 0
                                    ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE}
                                             description='当前没有可配置的页面菜单'/>
                                    : <Tree
                                        key={options.length}
                                        checkable
                                        checkStrictly
                                        showIcon
                                        defaultExpandAll
                                        treeData={this.buildTreeData(options)}
                                        checkedKeys={{
                                            checked: selected.map(item => item.id),
                                            halfChecked: [],
                                        }}
                                        onCheck={this.onCheck}
                                    />}
                        </div>
                    </div>

                    <div className='shortcut-config-pane'>
                        <div className='shortcut-config-title'>
                            已选 {selected.length}/{MAX_SHORTCUTS}
                        </div>
                        <div className='shortcut-selected-box'>
                            {selected.length === 0
                                ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE}
                                         description='从左侧勾选页面菜单'/>
                                : selected.map(this.renderSelectedItem)}
                        </div>
                    </div>
                </div>
            </Modal>
        </>;
    }
}
