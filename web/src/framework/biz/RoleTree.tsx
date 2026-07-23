import {Skeleton, Tree} from 'antd';
import React from 'react';
import {SolutionOutlined} from '@ant-design/icons';
import {HttpUtils} from "../utils";

interface RoleTreeProps {
    onSelect?: (orgId: string | null) => void;
}

interface RoleTreeState {
    treeDataLoading: boolean;
    treeData: any[];
    currentOrgId: string | null;
}

export class RoleTree extends React.Component<RoleTreeProps, RoleTreeState> {

    state = {
        treeDataLoading: true,
        treeData: [],
        currentOrgId: null
    }

    componentDidMount() {
        HttpUtils.get('admin/sysRole/biz-tree').then(tree => {
            this.setState({treeData: tree, treeDataLoading: false})
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
            icon={item => <SolutionOutlined/>}
        />
    }
}
