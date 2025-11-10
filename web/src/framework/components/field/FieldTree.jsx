import {message, Tree} from 'antd';
import React from 'react';
import {HttpUtil} from "../../system";


export  class FieldTree extends React.Component {


  state = {
    treeData:  null,
    checked: [],
    confirmLoading: false,
  }
  actionRef = React.createRef();

  componentDidMount() {
    const {url} = this.props;
    HttpUtil.get(url).then(rs => {
      this.setState({treeData: rs})
    })

  }


  render() {
    console.log('checked keys', this.props.value)
    let {treeData} = this.state
    return <>
      <Tree
        treeData={treeData}
        multiple
        checkable
        checkStrictly={true}
        checkedKeys={this.props.value}
        onCheck={(keys)=>this.props.onChange(keys)}
      >
      </Tree>
    </>
  }


}



