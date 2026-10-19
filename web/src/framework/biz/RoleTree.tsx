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
        }).catch(() => {
            // 失败时结束 loading 并置空（组件会显示空状态提示），避免一直停留在骨架屏；
            // 401 由 HttpClient 广播 loginExpired（全局弹窗提示），其余错误仍按 toastError 弹错
            this.setState({treeData: [], treeDataLoading: false})
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
