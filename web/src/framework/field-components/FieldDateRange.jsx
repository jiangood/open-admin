/**
 * 根据时间类型自动渲染时间选择组件
 */
import React from "react";
import dayjs from "dayjs";
import {DatePicker, TimePicker} from "antd";


export class FieldDateRange extends React.Component {
    static defaultProps = {
        type: 'YEAR_MONTH_DAY'
    }

    render() {
        let {type, value, onChange, ...rest} = this.props;
        switch (type) {
            case 'YEAR':
                return <DatePicker.RangePicker
                    value={this.strToDate(value, 'YYYY')}
                    onChange={v => onChange(this.dateToStr(v,'YYYY'))}
                    picker="year"
                    {...rest}
                />;
            case 'YEAR_MONTH':
                return <DatePicker.RangePicker
                    value={this.strToDate(value, 'YYYY-MM')}
                    onChange={v => onChange(this.dateToStr(v,'YYYY-MM'))}
                    picker="month"
                    {...rest}
                />;
            case 'YEAR_QUARTER':
                return <DatePicker.RangePicker
                    value={this.strToDate(value, 'YYYY-QQ')}
                    onChange={v => onChange(this.dateToStr(v,'YYYY-QQ'))}
                    picker="quarter"
                    {...rest}
                />;
            case 'YEAR_MONTH_DAY':
                return   <DatePicker.RangePicker
                    value={this.strToDate(value, 'YYYY-MM-DD')}
                    onChange={v => onChange(this.dateToStr(v,'YYYY-MM-DD'))}
                    {...rest}
                />;
            case 'YEAR_MONTH_DAY_HOUR_MINUTE':
                return <DatePicker.RangePicker
                    value={this.strToDate(value, 'YYYY-MM-DD HH:mm')}
                    onChange={v => onChange(this.dateToStr(v,'YYYY-MM-DD HH:mm'))}
                    format='YYYY-MM-DD HH:mm'
                    showTime
                    {...rest}
                />;
            case 'YEAR_MONTH_DAY_HOUR_MINUTE_SECOND':
                return <DatePicker.RangePicker
                    value={this.strToDate(value, 'YYYY-MM-DD HH:mm:ss')}
                    onChange={v => onChange(this.dateToStr(v,'YYYY-MM-DD HH:mm:ss'))}
                    showTime
                    {...rest}
                />;
            case 'HOUR_MINUTE':
                return <TimePicker.RangePicker
                    format='HH:mm'
                    value={this.strToDate(value, 'HH:mm')}
                    onChange={v => onChange(this.dateToStr(v,'HH:mm'))}
                    {...rest}
                />;
            case 'HOUR_MINUTE_SECOND':
                return <TimePicker.RangePicker
                    value={this.strToDate(value, 'HH:mm:ss')}
                    onChange={v => onChange(this.dateToStr(v,'HH:mm:ss'))}
                    {...rest}
                />;
        }

        return <div>未知组件 {type}</div>
    }



    strToDate(v, fmt) {
        console.log('调用转换')
        if (!v) {
            return null;
        }

        const arr = v.split("/")
        let s1 = arr[0];
        let s2 = arr[1];
        return [dayjs(s1), dayjs(s2)]
    }

    dateToStr(dateArr, fmt) {
        const d1 = dateArr[0]
        const d2 = dateArr[1]

        const s1 = d1 ? d1.format(fmt) : ""
        const s2 = d2 ? d2.format(fmt) : "";

        return s1 + '/' + s2
    }

}
