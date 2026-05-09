import React from 'react';
import { Spin, Tree } from 'antd';
import { BaseRemoteSelect } from '../BaseRemoteSelect';

/**
 * 多选树
 *
 * 区别于下拉框，是扁平展示的树
 * 这种需要扁平展示的树，通常都是多选。
 */
export class FieldRemoteTree extends BaseRemoteSelect {
    getLoadParams() {
        return undefined;
    }

    render() {
        if (this.state.loading) return <Spin />;

        return (
            <Tree
                multiple
                checkable
                onCheck={(e) => this.props.onChange && this.props.onChange(e.checked)}
                checkedKeys={this.props.value}
                treeData={this.state.data}
                defaultExpandAll
                checkStrictly
            />
        );
    }
}
