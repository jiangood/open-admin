import { Spin, TreeSelect } from 'antd';
import React from 'react';
import { StringUtils } from '../../utils';
import BaseRemoteSelect from '../BaseRemoteSelect';

export class FieldRemoteTreeSelectMultiple extends BaseRemoteSelect {
    static defaultProps = {
        treeDefaultExpandAll: true,
        style: {
            width: '100%',
            minWidth: 200,
        },
    };

    getLoadParams() {
        return undefined;
    }

    render() {
        const { value, onChange, style, treeDefaultExpandAll } = this.props;
        const { data, loading } = this.state;

        if (loading) return <Spin />;

        return (
            <TreeSelect
                style={style}
                allowClear
                dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
                treeData={data}
                showCheckedStrategy={TreeSelect.SHOW_ALL}
                value={value || undefined}
                onChange={onChange}
                multiple
                filterTreeNode={(inputValue, treeNode) =>
                    StringUtils.contains(treeNode.title, inputValue)
                }
                treeLine={{ showLeafIcon: true }}
                treeDefaultExpandAll={treeDefaultExpandAll}
            />
        );
    }
}
