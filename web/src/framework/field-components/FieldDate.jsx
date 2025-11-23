/**
 * 根据时间类型自动渲染时间选择组件
 */
import React from "react";
import dayjs from "dayjs";
import {DatePicker, TimePicker} from "antd";


export class FieldDate extends React.Component {
    static defaultProps = {
        type: 'YEAR_MONTH_DAY'
    }

    render() {
        let {type, value, onChange, ...rest} = this.props;
        switch (type) {
            case 'YEAR':
                return <DatePicker
                    value={this.strToDate(value, 'YYYY')}
                    onChange={v => onChange(this.dateToStr(v,'YYYY'))}
                    picker="year"
                    {...rest}
                />;
            case 'YEAR_MONTH':
                return <DatePicker
                    value={this.strToDate(value, 'YYYY-MM')}
                    onChange={v => onChange(this.dateToStr(v,'YYYY-MM'))}
                    picker="month"
                    {...rest}
                />;
            case 'YEAR_QUARTER':
                return <DatePicker
                    value={this.strToDate(value, 'YYYY-QQ')}
                    onChange={v => onChange(this.dateToStr(v,'YYYY-QQ'))}
                    picker="quarter"
                    {...rest}
                />;
            case 'YEAR_MONTH_DAY':
                return   <DatePicker
                    value={this.strToDate(value, 'YYYY-MM-DD')}
                    onChange={v => onChange(this.dateToStr(v,'YYYY-MM-DD'))}
                    {...rest}
                ></DatePicker>;
            case 'YEAR_MONTH_DAY_HOUR_MINUTE':
                return <DatePicker
                    value={this.strToDate(value, 'YYYY-MM-DD HH:mm')}
                    onChange={v => onChange(this.dateToStr(v,'YYYY-MM-DD HH:mm'))}
                    format='YYYY-MM-DD HH:mm'
                    showTime
                    {...rest}
                ></DatePicker>;
            case 'YEAR_MONTH_DAY_HOUR_MINUTE_SECOND':
                return <DatePicker
                    value={this.strToDate(value, 'YYYY-MM-DD HH:mm:ss')}
                    onChange={v => onChange(this.dateToStr(v,'YYYY-MM-DD HH:mm:ss'))}
                    showTime
                    {...rest}
                ></DatePicker>;
            case 'HOUR_MINUTE':
                return <TimePicker
                    format='HH:mm'
                    value={this.strToDate(value, 'HH:mm')}
                    onChange={v => onChange(this.dateToStr(v,'HH:mm'))}
                    {...rest}
                ></TimePicker>;
            case 'HOUR_MINUTE_SECOND':
                return <TimePicker
                    value={this.strToDate(value, 'HH:mm:ss')}
                    onChange={v => onChange(this.dateToStr(v,'HH:mm:ss'))}
                    {...rest}
                ></TimePicker>;
        }

        return <div>未知组件 {type}</div>
    }

    static getDefaultValue(type) {
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

    strToDate(value, fmt) {
        if (value != null && value !== '') {
            let type = typeof value;
            if (type === 'string' || type === 'number') {
                value = dayjs(value, fmt);
            }
        }

        return value;
    }

    dateToStr(date, fmt) {
        return date ? date.format(fmt) : null;
    }

}
