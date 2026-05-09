import {Alert, Skeleton, Tree} from 'antd';
import React from 'react';
import * as Icons from '@ant-design/icons';
import {HttpUtils} from "../../utils";

export class OrgTree extends React.Component<any, any> {

    state = {
        treeDataLoading: true,
        treeData: [],
        currentOrgId: null
    }

    componentDidMount() {
        HttpUtils.get('admin/sysOrg/dept-tree').then(tree => {
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
            return <Alert type={"warning"} message={'组织机构数据为空'}/>
        }

        return <Tree
            treeData={treeData}
            defaultExpandAll
            onSelect={this.onSelectOrg}
            showIcon
            blockNode
            icon={item => {
                const icon = Icons[item.iconName]
                if (icon) {
                    return React.createElement(icon)
                }
            }}
        />
    }
}
