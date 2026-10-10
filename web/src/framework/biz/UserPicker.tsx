import React from 'react';
import {Button, Empty, Form, Input, Modal, Splitter, Table} from 'antd';
import type {FormInstance, TableColumnsType} from 'antd';
import {CloseOutlined} from '@ant-design/icons';
import {OrgTree} from './OrgTree';
import {formatUserLabel, type PickerUser} from './pickerUser';
import {HttpClient} from '../utils';
import './UserPicker.less';

export interface UserPickerProps {
    /** 是否显示 */
    open: boolean;
    /** 弹窗标题 */
    title?: string;
    /** 是否多选，默认 true */
    multiple?: boolean;
    /** 已选用户 id（受控） */
    value?: string[];
    /** 弹窗宽度，默认 1080 */
    width?: number;
    /** 内容区高度，默认 530（刚好容纳默认每页 10 行，不出现表格内部纵向滚动） */
    height?: number;
    /** 分页数据源地址，默认 admin/sysUser/page */
    requestUrl?: string;
    /** 按 id 查询用户详情地址（用于回显），默认 admin/sysUser/by-ids */
    usersUrl?: string;
    /** 确定按钮 loading */
    confirmLoading?: boolean;
    /** 选择变化（点击确定时触发） */
    onChange?: (ids: string[], users: PickerUser[]) => void;
    /** 点击确定 */
    onOk?: (ids: string[], users: PickerUser[]) => void;
    /** 取消 */
    onCancel?: () => void;
}

interface UserPickerState {
    loading: boolean;
    dataSource: PickerUser[];
    total: number;
    current: number;
    pageSize: number;
    filters: {name?: string; account?: string};
    orgId: string | null;
    selectedMap: Record<string, PickerUser>;
    selectedKeyword: string;
}

// 候选人表格列与状态无关，提到模块级，避免每次勾选都重建列定义
const USER_TABLE_COLUMNS: TableColumnsType<PickerUser> = [
    {title: '姓名', dataIndex: 'name', width: 90},
    {title: '登录账号', dataIndex: 'account', width: 120},
    {title: '所属机构', dataIndex: 'orgLabel', ellipsis: true, render: (_, row) => row.orgLabel || row.unitLabel || ''},
];

// 机构树不随选择变化，memo 避免每次勾选都重渲染
const MemoOrgTree = React.memo(OrgTree);

/**
 * 通用选人器
 *
 * 机构树 + 服务端分页搜索 + 跨页多选，解决用户量大、同名难以区分的问题。
 */
export class UserPicker extends React.Component<UserPickerProps, UserPickerState> {

    static readonly defaultProps = {
        title: '选择用户',
        multiple: true,
    };

    formRef = React.createRef<FormInstance>();

    state: UserPickerState = {
        loading: false,
        dataSource: [],
        total: 0,
        current: 1,
        pageSize: 10,
        filters: {},
        orgId: null,
        selectedMap: {},
        selectedKeyword: '',
    };

    componentDidUpdate(prevProps: UserPickerProps) {
        if (this.props.open && !prevProps.open) {
            this.init();
        }
    }

    getRequestUrl = () => this.props.requestUrl || 'admin/sysUser/page';

    getUsersUrl = () => this.props.usersUrl || 'admin/sysUser/by-ids';

    init = () => {
        this.formRef.current?.resetFields();
        this.setState({current: 1, filters: {}, orgId: null, selectedMap: {}, selectedKeyword: ''}, () => {
            this.loadData();
            this.loadSelected(this.props.value || []);
        });
    };

    loadSelected = (ids: string[]) => {
        if (!ids.length) {
            this.setState({selectedMap: {}});
            return;
        }
        HttpClient.get<PickerUser[]>(this.getUsersUrl(), {ids}, {toastError: false}).then(rs => {
            const data = rs.data || [];
            const map: Record<string, PickerUser> = {};
            ids.forEach(id => {
                const user = data.find(item => item.id === id);
                if (user) {
                    map[id] = user;
                }
            });
            this.setState({selectedMap: map});
        });
    };

    loadData = () => {
        const {filters, orgId, current, pageSize} = this.state;
        this.setState({loading: true});
        HttpClient.get<{ content: PickerUser[]; totalElements: number | string; size: number }>(this.getRequestUrl(), {
            name: filters.name,
            account: filters.account,
            orgId: orgId || undefined,
            page: current,
            size: pageSize,
        }).then(rs => {
            const {content, totalElements, size} = rs.data;
            this.setState({dataSource: content || [], total: Number(totalElements), pageSize: size || pageSize, loading: false});
        }).catch(() => {
            this.setState({loading: false});
        });
    };

    onSearch = (values: UserPickerState['filters']) => {
        this.setState({filters: values, current: 1}, this.loadData);
    };

    resetSearch = () => {
        this.formRef.current?.resetFields();
        this.setState({filters: {}, current: 1}, this.loadData);
    };

    onSelectOrg = (orgId: string | null) => {
        this.setState({orgId, current: 1}, this.loadData);
    };

    // 跨页累计选择：保留已选行数据，去掉本次取消勾选的 key
    // 注意：preserveSelectedRowKeys 下，跨页已选但不在当前页的行在 selectedRows 中为 undefined，需跳过
    onSelectionChange = (keys: React.Key[], rows: PickerUser[]) => {
        const map = {...this.state.selectedMap};
        rows.forEach(row => {
            if (row && row.id != null) {
                map[row.id] = row;
            }
        });
        const keySet = new Set(keys.map(String));
        Object.keys(map).forEach(id => {
            if (!keySet.has(id)) {
                delete map[id];
            }
        });
        this.setState({selectedMap: map});
    };

    removeSelected = (id: string) => {
        const map = {...this.state.selectedMap};
        delete map[id];
        this.setState({selectedMap: map});
    };

    clearSelected = () => this.setState({selectedMap: {}, selectedKeyword: ''});

    handleOk = () => {
        const users = Object.values(this.state.selectedMap);
        const ids = users.map(user => user.id);
        this.props.onOk?.(ids, users);
        this.props.onChange?.(ids, users);
    };

    render() {
        const {open, title, multiple, width, height, confirmLoading, onCancel} = this.props;
        const {loading, dataSource, total, current, pageSize, selectedMap, selectedKeyword} = this.state;
        const selectedUsers = Object.values(selectedMap);
        const keyword = selectedKeyword.trim().toLowerCase();
        const filteredSelected = keyword
            ? selectedUsers.filter(user => formatUserLabel(user).toLowerCase().includes(keyword))
            : selectedUsers;

        return <Modal
            open={open}
            title={title}
            width={width || 1120}
            confirmLoading={confirmLoading}
            okText='确定'
            cancelText='取消'
            mask={{closable: false}}
            destroyOnHidden
            onOk={this.handleOk}
            onCancel={onCancel}
        >
            <Splitter style={{height: height || 530}}>
                <Splitter.Panel defaultSize={320} min={180} max={560} style={{paddingRight: 8}}>
                    <div className="oa-user-picker-tree">
                        <MemoOrgTree onChange={this.onSelectOrg}/>
                    </div>
                </Splitter.Panel>
                <Splitter.Panel style={{padding: '0 12px'}}>
                    <div style={{height: '100%', display: 'flex', flexDirection: 'column'}}>
                        <Form layout='inline' ref={this.formRef} onFinish={this.onSearch} style={{marginBottom: 8}}>
                            <Form.Item name='name'><Input allowClear placeholder='姓名' style={{width: 100}}/></Form.Item>
                            <Form.Item name='account'><Input allowClear placeholder='账号' style={{width: 100}}/></Form.Item>
                            <Form.Item><Button type='primary' htmlType='submit'>查询</Button></Form.Item>
                            <Form.Item><Button onClick={this.resetSearch}>重置</Button></Form.Item>
                        </Form>
                        <div style={{flex: 1, minHeight: 0, overflow: 'auto'}}>
                            <Table<PickerUser>
                                rowKey='id'
                                size='small'
                                loading={loading}
                                columns={USER_TABLE_COLUMNS}
                                dataSource={dataSource}
                                rowSelection={{
                                    type: multiple === false ? 'radio' : 'checkbox',
                                    selectedRowKeys: selectedUsers.map(user => user.id),
                                    preserveSelectedRowKeys: true,
                                    onChange: this.onSelectionChange,
                                }}
                                pagination={{
                                    current,
                                    pageSize,
                                    total,
                                    size: 'small',
                                    showSizeChanger: true,
                                    pageSizeOptions: [10, 20, 50, 100],
                                    showTotal: (count) => `共 ${count} 条`,
                                }}
                                onChange={(pagination) => this.setState({
                                    current: pagination.current || 1,
                                    pageSize: pagination.pageSize || pageSize,
                                }, this.loadData)}
                                locale={{emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description='暂无数据'/>}}
                            />
                        </div>
                    </div>
                </Splitter.Panel>
                <Splitter.Panel defaultSize={260} min={180} max={520} style={{paddingLeft: 8}}>
                    <div style={{height: '100%', display: 'flex', flexDirection: 'column'}}>
                        <div style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8}}>
                            <span>已选 <b>{selectedUsers.length}</b> 人</span>
                            {selectedUsers.length > 0 && <a onClick={this.clearSelected}>清空</a>}
                        </div>
                        <Input
                            allowClear
                            placeholder='搜索已选'
                            value={selectedKeyword}
                            onChange={(e) => this.setState({selectedKeyword: e.target.value})}
                            style={{marginBottom: 8}}
                        />
                        <div style={{flex: 1, minHeight: 0, overflow: 'auto'}}>
                            {selectedUsers.length === 0 && (
                                <div style={{color: 'var(--oa-color-text-tertiary, #999)', textAlign: 'center', marginTop: 24}}>暂未选择</div>
                            )}
                            {selectedUsers.length > 0 && filteredSelected.length === 0 && (
                                <div style={{color: 'var(--oa-color-text-tertiary, #999)', textAlign: 'center', marginTop: 24}}>无匹配人员</div>
                            )}
                            {filteredSelected.map(user => (
                                <div key={user.id} style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: 4,
                                    padding: '3px 0',
                                    borderBottom: '1px dashed var(--oa-color-border, #f0f0f0)',
                                }}>
                                    <span
                                        title={formatUserLabel(user)}
                                        style={{flex: 1, minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap'}}
                                    >
                                        {formatUserLabel(user)}
                                    </span>
                                    <CloseOutlined
                                        style={{cursor: 'pointer', color: 'var(--oa-color-text-tertiary, #999)', flexShrink: 0}}
                                        onClick={() => this.removeSelected(user.id)}
                                    />
                                </div>
                            ))}
                        </div>
                    </div>
                </Splitter.Panel>
            </Splitter>
        </Modal>;
    }
}
