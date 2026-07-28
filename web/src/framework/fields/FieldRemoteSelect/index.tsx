import React from 'react';
import {Select} from 'antd';
import {BaseRemoteSelect} from '../BaseRemoteSelect';
import type {SelectProps} from "antd/es/select";
import type {FieldProps} from '../types';

interface FieldRemoteSelectProps extends Omit<SelectProps, 'value' | 'onChange' | 'mode'>, FieldProps<any> {
    url: string;
    debounceTime?: number;
    multiple?: boolean;
    paramsProcessor?: (params: any) => any;
    responseProcessor?: (response: any) => any;
}

export class FieldRemoteSelect extends BaseRemoteSelect<FieldRemoteSelectProps> {
    static defaultProps = {
        placeholder: '请搜索选择',
    };

    render() {
        const {value, placeholder, multiple, ...rest} = this.props;
        const {data, loading} = this.state;

        return (
            <Select
                showSearch={this.getShowSearch()}
                options={data}
                notFoundContent={this.getNotFoundContent()}
                style={{width: '100%', minWidth: 200}}
                allowClear
                loading={loading}
                value={value}
                placeholder={placeholder}
                mode={multiple ? 'multiple' : undefined}
                {...rest}
            />
        );
    }
}
