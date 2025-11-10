import { message, Spin, TreeSelect } from 'antd';

import React from 'react';
import {HttpUtil} from "../../system";
import {TreeUtil} from "@tmgg/tmgg-commons-lang";

export class FieldRemoteTreeSelect extends React.Component {
  state = {
    data: [],
    value: [],
    fetching: false,
    key: this.props.id,
  };

  componentDidMount() {
    this.fetchData();
  }





  fetchData = () => {
    const { url } = this.props;
    this.setState({ fetching: true });

    HttpUtil.get(url).then((rs) => {
      this.setState({ data:  rs, fetching: false });
    });
  };

  handleChange = (value) => {
    if (this.props.onChange) {
      this.props.onChange(value);
    }
  };

  render() {
    const { props } = this;
    let { mode, value } = props;
    let { data } = this.state;

    if (this.state.fetching) {
      return <Spin />;
    }

    if (mode === 'read') {
      if (value == null) {
        return '';
      }

      const target = TreeUtil.findByKey(data, value);
      if (target) {
        return target.title;
      }
      return '';
    }


    return (
      <TreeSelect
        style={{ width: '100%', minWidth: 200 }}
        allowClear={true}
        dropdownStyle={{ maxHeight: 400, overflow: 'auto' }}
        treeData={data}
        showCheckedStrategy={TreeSelect.SHOW_ALL}
        treeDefaultExpandAll={false}
        onChange={this.handleChange}

        filterTreeNode={(inputValue, treeNode)=>{
          const {title} = treeNode
          return str.contains(title,inputValue)
        }}
        treeLine={{showLeafIcon:true}}

        {...this.props}
        value={value || undefined}
        showArrow={true}
      />
    );
  }
}
