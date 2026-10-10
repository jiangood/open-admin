import React from 'react';
import {Tag} from 'antd';
import {HttpClient} from '../../utils';
import type {FieldProps} from '../types';
import {formatUserLabel, type PickerUser} from '../../biz/pickerUser';
import {UserPicker} from '../../biz/UserPicker';

export interface FieldUserPickerProps extends FieldProps<string[]> {
    /** 是否多选，默认 true */
    multiple?: boolean;
    /** 禁用 */
    disabled?: boolean;
    /** 空值提示，默认 请选择用户 */
    placeholder?: string;
    /** 弹窗标题 */
    title?: string;
    /** 分页数据源地址，默认 admin/sysUser/page */
    requestUrl?: string;
    /** 按 id 查询用户详情地址（用于回显），默认 admin/sysUser/by-ids */
    usersUrl?: string;
}

interface FieldUserPickerState {
    open: boolean;
    users: PickerUser[];
}

/**
 * 选人表单组件
 *
 * 展示已选用户标签并提供弹窗选择，遵循字段组件约定：value + onChange（value 为用户 id 数组）。
 */
export class FieldUserPicker extends React.Component<FieldUserPickerProps, FieldUserPickerState> {

    static readonly defaultProps = {
        multiple: true,
        placeholder: '请选择用户',
    };

    state: FieldUserPickerState = {
        open: false,
        users: [],
    };

    componentDidMount() {
        this.syncUsers();
    }

    componentDidUpdate(prevProps: FieldUserPickerProps) {
        if (prevProps.value !== this.props.value) {
            this.syncUsers();
        }
    }

    getUsersUrl = () => this.props.usersUrl || 'admin/sysUser/by-ids';

    // 根据 value(ids) 拉取并补全用户详情，按 ids 顺序展示
    syncUsers = () => {
        const ids = this.props.value || [];
        const known = new Map(this.state.users.map(user => [user.id, user]));
        const missing = ids.filter(id => !known.has(id));
        if (missing.length === 0) {
            this.setState({users: ids.map(id => known.get(id)).filter(Boolean) as PickerUser[]});
            return;
        }
        HttpClient.get<PickerUser[]>(this.getUsersUrl(), {ids}, {toastError: false}).then(rs => {
            const merged = new Map(this.state.users.map(user => [user.id, user]));
            (rs.data || []).forEach(user => merged.set(user.id, user));
            const currentIds = this.props.value || [];
            this.setState({users: currentIds.map(id => merged.get(id)).filter(Boolean) as PickerUser[]});
        });
    };

    remove = (id: string) => {
        this.props.onChange?.((this.props.value || []).filter(value => value !== id));
    };

    handleOk = (ids: string[], users: PickerUser[]) => {
        this.setState({open: false, users});
        this.props.onChange?.(ids);
    };

    render() {
        const {disabled, placeholder, multiple, title, requestUrl, usersUrl, value} = this.props;
        const {open, users} = this.state;
        return <>
            <div style={{
                minHeight: 32,
                display: 'flex',
                flexWrap: 'wrap',
                alignItems: 'center',
                gap: 4,
                padding: '4px 8px',
                border: '1px solid var(--oa-color-border, #d9d9d9)',
                borderRadius: 'var(--oa-border-radius, 6px)',
                background: disabled ? 'var(--oa-color-fill, #f5f5f5)' : 'var(--oa-color-bg-container, #fff)',
                cursor: disabled ? 'not-allowed' : undefined,
            }}>
                {users.length === 0 && <span style={{color: 'var(--oa-color-text-tertiary, #999)'}}>{placeholder}</span>}
                {users.map(user => (
                    <Tag key={user.id} closable={!disabled} onClose={(e) => {
                        e.preventDefault();
                        this.remove(user.id);
                    }}>
                        {formatUserLabel(user)}
                    </Tag>
                ))}
                {!disabled && (
                    <a style={{marginLeft: users.length ? 'auto' : 0}} onClick={() => this.setState({open: true})}>选择</a>
                )}
            </div>
            <UserPicker
                open={open}
                title={title}
                multiple={multiple}
                value={value}
                requestUrl={requestUrl}
                usersUrl={usersUrl}
                onOk={this.handleOk}
                onCancel={() => this.setState({open: false})}
            />
        </>;
    }
}
