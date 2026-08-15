import {Alert, Skeleton, Tree} from 'antd';
import React from 'react';
import {HttpClient} from "../utils";
import {NamedIcon} from "../components/NamedIcon";

interface OrgTreeProps {
    onChange?: (orgId: string | null) => void;
}

interface OrgTreeState {
    treeDataLoading: boolean;
    treeData: any[];
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
            icon={item => item.iconName ? <NamedIcon name={item.iconName}/> : null}
        />
    }
}
