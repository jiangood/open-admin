import React from 'react';
import { DatePicker } from 'antd';
import {valueToDate} from "./TimePickerTool";

/**
 *
 * 值类型为字符串
 */
export { DatePickerString as FieldDatePickerString };

const FMT = 'YYYY-MM-DD';

export class DatePickerString extends React.Component {
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
      <DatePicker
        {...this.props}
        value={value}
        onChange={this.onChange}
        disabled={readOnly || this.props.disabled}
      ></DatePicker>
    );
  }
}
