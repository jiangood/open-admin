import {Alert, Skeleton, Tree} from 'antd';
import React from 'react';
import {HttpUtil} from "../system";
import * as Icons from '@ant-design/icons';


export  class OrgTree  extends React.Component {

    state = {
        treeDataLoading: true,
        treeData: [],

        currentOrgId: null
    }


    componentDidMount() {
        HttpUtil.get('sysOrg/unitTree').then(tree => {
            this.setState({treeData: tree,treeDataLoading: false})
        })
    }

    onSelectOrg = orgIds => {
        let orgId = orgIds[0] || null;
        this.props.onChange(orgId)
    }


    render() {
        let {treeData, treeDataLoading} = this.state
        if (treeDataLoading) {
            return <Skeleton title='加载中...'/>
        }

        if (treeData.length === 0) {
            return <Alert type={"warning"} message={'组织机构数据为空'}>
            </Alert>
        }

        return <Tree
            treeData={treeData}
            defaultExpandAll
            onSelect={this.onSelectOrg}
            showIcon
            blockNode
            icon={item=>{
                  const icon =  Icons[item.iconName]
                    if(icon){
                        return React.createElement(icon)
                    }
            }}
        >
        </Tree>
    }


}



