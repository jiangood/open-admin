import React from "react";
import {HttpUtil} from "../system";
import {Spin, Tree} from "antd";

export class FieldRemoteTree extends React.Component {

    state = {
        treeLoading: true,
        treeData: [],
    }

     componentDidMount() {
         this.loadData();
    }


    loadData = async () => {
        this.setState({treeLoading: true})
        let url = this.props.url;
        try {
            const treeData = await HttpUtil.get(url)
            this.setState({treeData})
        } catch (e) {
            console.log(e)
        } finally {
            this.setState({treeLoading: false})
        }
    };

    render() {
        if (this.state.treeLoading) {
            return <Spin/>
        }
        return <Tree
            multiple
            checkable
            onCheck={e => this.props.onChange(e.checked)}
            checkedKeys={this.props.value}
            treeData={this.state.treeData}
            defaultExpandAll
            checkStrictly
        >
        </Tree>
    }
}