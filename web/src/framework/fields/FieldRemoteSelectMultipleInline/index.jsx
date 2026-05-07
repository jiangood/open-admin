import React from 'react';
import { Select } from 'antd';
import { StringUtils } from '../../utils';
import BaseRemoteSelect from '../BaseRemoteSelect';

/**
 * 多选，但是值是字符串，逗号拼接的
 */
export class FieldRemoteSelectMultipleInline extends BaseRemoteSelect {
    static defaultProps = {
        placeholder: '请搜索选择',
    };

    render() {
        const { value, onChange, url, ...selectProps } = this.props;
        const { data, loading } = this.state;

        return (
            <Select
                showSearch={this.getShowSearch()}
                value={StringUtils.split(value, ',')}
                onChange={(arr) => onChange && onChange(StringUtils.join(arr, ','))}
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
