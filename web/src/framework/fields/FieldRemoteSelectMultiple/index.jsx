import React from 'react';
import { Select } from 'antd';
import BaseRemoteSelect from '../BaseRemoteSelect';

export class FieldRemoteSelectMultiple extends BaseRemoteSelect {
    static defaultProps = {
        placeholder: '请搜索选择',
    };

    render() {
        const { value, onChange, url, ...selectProps } = this.props;
        const { data, loading } = this.state;

        return (
            <Select
                showSearch={this.getShowSearch()}
                value={value}
                onChange={onChange}
                options={data}
                notFoundContent={this.getNotFoundContent()}
                style={{ width: '100%', minWidth: 200 }}
                allowClear
                mode="multiple"
                loading={loading}
                {...selectProps}
            />
        );
    }
}
