import React from 'react';
import { Select } from 'antd';
import BaseRemoteSelect from '../BaseRemoteSelect';

export class FieldRemoteSelect extends BaseRemoteSelect {
    static defaultProps = {
        placeholder: '请搜索选择',
    };

    render() {
        const { value, placeholder, ...rest } = this.props;
        const { data, loading } = this.state;

        return (
            <Select
                showSearch={this.getShowSearch()}
                options={data}
                notFoundContent={this.getNotFoundContent()}
                style={{ width: '100%', minWidth: 200 }}
                allowClear
                loading={loading}
                value={value}
                placeholder={placeholder}
                {...rest}
            />
        );
    }
}
