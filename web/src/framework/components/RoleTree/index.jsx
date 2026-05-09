import {Skeleton, Tree} from 'antd';
import React from 'react';
import {SolutionOutlined} from '@ant-design/icons';
import {HttpUtils} from "../../utils";


export  class RoleTree  extends React.Component {

    state = {
        treeDataLoading: true,
        treeData: [],

        currentOrgId: null
    }


    componentDidMount() {
        HttpUtils.get('admin/sysRole/biz-tree').then(tree => {
            this.setState({treeData: tree,treeDataLoading: false})
        })
    }

    onSelect = keys => {
        const orgId = keys[0] || null;
        this.props.onSelect(orgId)
    }


    render() {
        const {treeData, treeDataLoading} = this.state
        if (treeDataLoading) {
            return <Skeleton title='加载中...'/>
        }


        return <Tree
            treeData={treeData}
            defaultExpandAll
            onSelect={this.onSelect}
            showIcon
            blockNode
            icon={item=>{
               return <SolutionOutlined />
            }}
        >
        </Tree>
    }


}
