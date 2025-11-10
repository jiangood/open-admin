import {Tree} from "antd";
import React from "react";


export default class extends React.Component{

    render() {
        return <Tree
            multiple
            checkable
            onCheck={this.onCheck}
            checkedKeys={checked}
            treeData={treeData}
            defaultExpandAll
            checkStrictly

        >
        </Tree>
    }
}