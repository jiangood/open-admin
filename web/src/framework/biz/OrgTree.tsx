import {Alert, Skeleton, Tree, type TreeDataNode} from 'antd';
import React from 'react';
import {HttpClient} from "../utils";
import {NamedIcon} from "../components/NamedIcon";

interface OrgTreeProps {
    onChange?: (orgId: string | null) => void;
}

interface OrgTreeState {
    treeDataLoading: boolean;
    treeData: TreeDataNode[];
    currentOrgId: string | null;
}

export class OrgTree extends React.Component<OrgTreeProps, OrgTreeState> {

    state = {
        treeDataLoading: true,
        treeData: [],
        currentOrgId: null
    }

    componentDidMount() {
        HttpClient.get('admin/sysOrg/tree', null, tree => {
            this.setState({treeData: tree, treeDataLoading: false})
        })
    }

    onSelectOrg = orgIds => {
        const orgId = orgIds[0] || null;
        this.props.onChange(orgId)
    }

    render() {
        const {treeData, treeDataLoading} = this.state
        if (treeDataLoading) {
            return <Skeleton title='加载中...'/>
        }

        if (treeData.length === 0) {
            return <Alert type={"warning"} title={'组织机构数据为空'}/>
        }

        return <Tree
            treeData={treeData}
            defaultExpandAll
            onSelect={this.onSelectOrg}
            showIcon
            blockNode
            icon={renderOrgIcon}
        />
    }
}

function renderOrgIcon(item) {
    return item.iconName ? <NamedIcon name={item.iconName}/> : null;
}
