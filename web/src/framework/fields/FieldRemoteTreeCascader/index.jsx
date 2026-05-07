import { Cascader, Spin } from 'antd';
import React from 'react';
import { TreeUtils } from '../../utils';
import BaseRemoteSelect from '../BaseRemoteSelect';

/**
 * 远程树级联选择器，类似 select，但是树级联
 *
 * 注意，value 为非数组形式，区别于 cascader 组件
 */
export class FieldRemoteTreeCascader extends BaseRemoteSelect {
    getLoadParams() {
        return undefined;
    }

    render() {
        const { data, loading } = this.state;
        if (loading) return <Spin />;

        const { value, onChange, ...rest } = this.props;

        let arr = [];
        if (value != null) {
            arr = TreeUtils.getKeyList(data, value);
        }

        return (
            <Cascader
                options={data}
                onChange={(arr) => {
                    onChange && onChange(arr[arr.length - 1]);
                }}
                value={arr}
                fieldNames={{ label: 'title', value: 'key' }}
                {...rest}
            />
        );
    }
}
