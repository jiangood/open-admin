import React from 'react';
import { DatePicker } from 'antd';
import {valueToDate} from "./TimePickerTool";

/**
 * 季度选择
 */
export { YearQuarterString as FieldYearQuarterString };

const FMT = 'YYYY-QQ';

export class YearQuarterString extends React.Component {

  static FORMAT = FMT


  onChange = (date, dateString) => {
    this.props.onChange(dateString);
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
        picker="quarter"
        disabled={readOnly || this.props.disabled}
      ></DatePicker>
    );
  }
}
