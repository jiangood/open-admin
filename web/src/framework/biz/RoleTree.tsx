import {Skeleton, Tree, type TreeDataNode} from 'antd';
import React from 'react';
import {SolutionOutlined} from '@ant-design/icons';
import {HttpClient} from "../utils";

interface RoleTreeProps {
    onSelect?: (orgId: string | null) => void;
}

interface RoleTreeState {
    treeDataLoading: boolean;
    treeData: TreeDataNode[];
    currentOrgId: string | null;
}

export class RoleTree extends React.Component<RoleTreeProps, RoleTreeState> {

    state = {
        treeDataLoading: true,
        treeData: [],
        currentOrgId: null
    }

    componentDidMount() {
        HttpClient.get('admin/sysRole/biz-tree', null).then(tree => {
            this.setState({treeData: tree.data, treeDataLoading: false})
        })
    }

    onSelect = keys => {
        const orgId = keys[0] || null;
        this.props.onSelect(orgId)
    }

    render() {
        const {treeData, treeDataLoading} = this.state
        if (treeDataLoading) {
            return <Skeleton active title={{ width: '60%' }} paragraph={{ rows: 6 }} />;
        }

        return <Tree
            treeData={treeData}
            defaultExpandAll
            onSelect={this.onSelect}
            showIcon
            blockNode
            icon={renderRoleIcon}
        />
    }
}

function renderRoleIcon() {
    return <SolutionOutlined/>;
}
