import React from "react";
import {Spin, Tree} from "antd";
import {HttpUtils} from "../../utils";

/**
 * 多选树
 *
 * 区别于下拉框，是扁平展示的树
 * 这种需要扁平展示的树，通常都是多选。
 *
 */
export class FieldRemoteTree extends React.Component {
    state = {
        treeLoading: true,
        treeData: [],
    };

    componentDidMount() {
        this.loadData();
    }

    loadData = async () => {
        this.setState({treeLoading: true});
        const {url} = this.props;
        try {
            const treeData = await HttpUtils.get(url);
            this.setState({treeData});
        } catch (e) {
            console.log(e);
        } finally {
            this.setState({treeLoading: false});
        }
    };

    render() {
        if (this.state.treeLoading) {
            return <Spin/>;
        }
        return <Tree
            multiple
            checkable
            onCheck={e => this.props.onChange && this.props.onChange(e.checked)}
            checkedKeys={this.props.value}
            treeData={this.state.treeData}
            defaultExpandAll
            checkStrictly
        >
        </Tree>;
    }
}