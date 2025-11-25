import {Skeleton, Tree} from 'antd';
import React from 'react';
import {HttpUtil} from "../system";
import {SolutionOutlined} from '@ant-design/icons';


export  class RoleTree  extends React.Component {

    state = {
        treeDataLoading: true,
        treeData: [],

        currentOrgId: null
    }


    componentDidMount() {
        HttpUtil.get('admin/sysRole/bizTree').then(tree => {
            this.setState({treeData: tree,treeDataLoading: false})
        })
    }

    onSelect = keys => {
        let orgId = keys[0] || null;
        this.props.onSelect(orgId)
    }


    render() {
        let {treeData, treeDataLoading} = this.state
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



