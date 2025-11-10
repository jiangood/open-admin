import React from 'react';
import { Switch } from 'antd';

export { SwitchYN as FieldSwitchYN };

export class SwitchYN extends React.Component {
  render() {
    let { value, mode } = this.props;

    if (mode === 'read') {
      if (value) {
        return value === 'Y' ? '是' : '否';
      }
      return '';
    }

    let strValue = null;
    if (value != null && value != '') {
      strValue = value == 'Y';
    } else {
      // 设置默认值
      strValue = 'N';
    }

    return <Switch value={strValue} {...this.props} onChange={this.onChange}></Switch>;
  }

  onChange = (b) => {
    this.props.onChange(b ? 'Y' : 'N');
  };
}
