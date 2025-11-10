import React from 'react';

import { TimePicker } from 'antd';
import {valueToDate} from "./TimePickerTool";

/**
 * 时分
 *
 * 值类型为字符串
 *
 *
 */
const FMT = 'HH:mm';

export class FieldHHmmPickerString extends React.Component {

  static FORMAT = FMT

  onChange = (date) => {
    const str = date ? date.format(FMT) : null;
    this.props.onChange(str);
  };

  render() {
    let { value, readOnly = false, mode } = this.props;
    if (mode == 'read') {
      return value;
    }
    value = valueToDate(value,FMT)


    return (
      <TimePicker
        {...this.props}
        value={value}
        onChange={this.onChange}
        format={FMT}
        disabled={readOnly || this.props.disabled}
      ></TimePicker>
    );
  }
}
