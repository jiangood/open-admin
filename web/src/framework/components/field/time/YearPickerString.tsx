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
export { YearPickerString as FieldYearPickerString };
const FMT = 'YYYY';
export class YearPickerString extends React.Component {

  static FORMAT = FMT


  onChange = (date) => {
    const str = date ? date.format(FMT): null;
    this.props.onChange(str);
  };

  render() {
    let { value, readOnly = false, mode } = this.props;
    if (mode == 'read') {
      return value;
    }
    value = valueToDate(value,FMT)

    return (
      <DatePicker
        {...this.props}
        value={value}
        onChange={this.onChange}
        picker="year"
        disabled={readOnly || this.props.disabled}
      ></DatePicker>
    );
  }
}
