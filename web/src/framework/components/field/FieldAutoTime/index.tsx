/**
 * 根据时间类型自动渲染时间选择组件
 */
import React from "react";
import {FieldMonthPickerString, FieldYearPickerString, FieldYearQuarterString} from "../time";


type TypeEum = 'YEAR' | 'YEAR_MONTH' | 'YEAR_QUARTER';

interface Props {
  type: TypeEum; //  枚举
}

export  class FieldAutoTime extends React.Component<Props, any> {

  render() {
    let {type,...props} = this.props;
    switch (type) {
      case 'YEAR':
        return <FieldYearPickerString {...props}/>;
      case 'YEAR_MONTH':
        return <FieldMonthPickerString {...props}/>;
      case 'YEAR_QUARTER':
        return <FieldYearQuarterString {...props}/>;
    }

    return <div>未知组件 {type}</div>
  }

  static getDefaultValue(type:TypeEum) {
    let year = dayjs().format("YYYY");
    let month = dayjs().format("YYYY-MM");
    let quarter = dayjs().format("YYYY-QQ");

    switch (type) {
      case 'YEAR':
        return year;
      case 'YEAR_MONTH':
        return month;
      case 'YEAR_QUARTER':
        return quarter;
    }

    return null;
  }
}
