import React from 'react';
import { DatePicker } from 'antd';
import {valueToDate} from "./TimePickerTool";

/**
 * 月份选择器
 *
 * 值类型为字符串
 *
 *
 */
export { MonthPickerString as FieldMonthPickerString };

const FMT = 'YYYY-MM';

export class MonthPickerString extends React.Component {

  static FORMAT = FMT


  onChange = (date) => {
    const str = date ? date.format(FMT) : null;
    this.props.onChange(str);
  };

  render() {
    let { value, readOnly = false, mode, ...rest} = this.props;
    if (mode == 'read') {
      return value;
    }

    value = valueToDate(value,FMT)


    return (
      <DatePicker
        {...rest}
        value={value}
        onChange={this.onChange}
        picker="month"
        disabled={readOnly || this.props.disabled}
      ></DatePicker>
    );
  }
}
