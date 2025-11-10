import {message, Spin, TreeSelect} from 'antd';

import React from 'react';
import {HttpUtil} from "../../system";

/**
 * props : url
 */

export class FieldRemoteTreeMultipleSelect extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      url: props.url,
      data: [],
      value: [],
      fetching: false,
      key: this.props.id,
    };
  }

  componentDidMount() {
    this.loadData();
  }



  loadData = () => {
    const {url} = this.state;

    this.setState({fetching: true});
    HttpUtil.get(url).then((rs) => {
      if (rs == null || rs.success === false) {
        message.error(rs?.message || '异常');
        this.setState({fetching: false});
        return;
      }

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
    let {value, mode, onChange, ...restProps} = this.props;


    if (mode === 'read') {
      return this.renderRead();
    }

    return (
      <TreeSelect
        style={{width: '100%'}}
        allowClear={true}
        dropdownStyle={{maxHeight: 400, overflow: 'auto'}}
        treeData={data}
        showCheckedStrategy={TreeSelect.SHOW_ALL}
        treeDefaultExpandAll={false}

        multiple

        showArrow={true}

        value={value || undefined}
        onChange={this.handleChange}

        filterTreeNode={(inputValue, treeNode)=>{
          const {title} = treeNode
          return StrUtil.contains(title,inputValue)
        }}

        treeLine={{showLeafIcon:true}}
        {...restProps}
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
