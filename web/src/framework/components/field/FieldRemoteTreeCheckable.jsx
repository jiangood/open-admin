import {message, Spin, TreeSelect} from 'antd';

import React from 'react';
import {TreeUtil} from "@tmgg/tmgg-commons-lang";
import {HttpUtil} from "../../system";




/**
 * 带checkbox的tree select
 * props : url
 */
export class FieldRemoteTreeCheckable extends React.Component {



  state = {
    data: [],
    fetching: false,
  };


  componentDidMount() {
    const {url} = this.props;
    this.fetchData(url);
  }


  fetchData = (url) => {
    this.setState({fetching: true});

    HttpUtil.get(url).then((rs) => {
      this.setState({data: rs, fetching: false});
    });
  };

  handleChange = (value) => {
    if (this.props.onChange) {
      this.props.onChange(value);
    }
  };

  render() {
    const {data} = this.state;
    let {value, mode, onChange, url, ...restProps} = this.props;

    if (this.state.fetching) {
      return <Spin/>;
    }

    if (mode === 'read') {
      return this.renderRead();
    }

    return (
      <TreeSelect
        {...restProps}
        style={{width: '100%'}}
        allowClear={true}
        dropdownStyle={{maxHeight: 400, overflow: 'auto'}}
        treeData={data}
        showCheckedStrategy={TreeSelect.SHOW_ALL}
        treeDefaultExpandAll={false}
        onChange={this.handleChange}
        multiple
        value={value || []}
        treeCheckable
        treeLine
      />
    );
  }

  renderRead() {
    const {value} = this.props;
    if (value == null) {
      return '-';
    }
    const {data} = this.state;
    const itemList = TreeUtil.findByKeyList(data, value);
    return itemList.map((item) => item.title || item.label).join(',');
  }
}
