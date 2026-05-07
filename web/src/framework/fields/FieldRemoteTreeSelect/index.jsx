import { Spin, TreeSelect } from 'antd';
import React from 'react';
import { StringUtils } from '../../utils';
import BaseRemoteSelect from '../BaseRemoteSelect';

export class FieldRemoteTreeSelect extends BaseRemoteSelect {
    static defaultProps = {
        treeDefaultExpandAll: true,
    };

    /** 树形接口不需要请求参数 */
    getLoadParams() {
        return undefined;
    }

    render() {
        const { value, onChange, treeDefaultExpandAll } = this.props;
        const { data, loading } = this.state;

        if (loading) return <Spin />;

        return (
            <TreeSelect
                style={{ width: '100%', minWidth: 200 }}
                allowClear
                dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
                treeData={data}
                showCheckedStrategy={TreeSelect.SHOW_ALL}
                value={value || undefined}
                onChange={onChange}
                filterTreeNode={(inputValue, treeNode) =>
                    StringUtils.contains(treeNode.title, inputValue)
                }
                treeLine={{ showLeafIcon: true }}
                treeDefaultExpandAll={treeDefaultExpandAll}
            />
        );
    }
}
