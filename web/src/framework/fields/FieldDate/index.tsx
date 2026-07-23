/**
 * 根据时间类型自动渲染时间选择组件
 */
import React from "react";
import dayjs from "dayjs";
import {DatePicker, TimePicker} from "antd";
import {DateUtils} from "../../utils";

export class FieldDate extends React.Component {
    static defaultProps = {
        type: 'YYYY-MM-DD'
    };

    render() {
        const {type, value, onChange, ...rest} = this.props;
        const formattedType = DateUtils.convertTypeToFormat(type);

        switch (formattedType) {
            case 'YYYY':
                return <DatePicker
                    value={this.strToDate(value, formattedType)}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'YYYY'))}
                    picker="year"
                    {...rest}
                />;
            case 'YYYY-MM':
                return <DatePicker
                    value={this.strToDate(value, 'YYYY-MM')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'YYYY-MM'))}
                    picker="month"
                    {...rest}
                />;
            case 'YYYY-QQ':
                return <DatePicker
                    value={this.strToDate(value, 'YYYY-QQ')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'YYYY-QQ'))}
                    picker="quarter"
                    {...rest}
                />;
            case 'YYYY-MM-DD':
                return <DatePicker
                    value={this.strToDate(value, 'YYYY-MM-DD')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'YYYY-MM-DD'))}
                    {...rest}
                ></DatePicker>;
            case 'YYYY-MM-DD HH:mm':
                return <DatePicker
                    value={this.strToDate(value, 'YYYY-MM-DD HH:mm')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'YYYY-MM-DD HH:mm'))}
                    format='YYYY-MM-DD HH:mm'
                    showTime
                    {...rest}
                ></DatePicker>;
            case 'YYYY-MM-DD HH:mm:ss':
                return <DatePicker
                    value={this.strToDate(value, 'YYYY-MM-DD HH:mm:ss')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'YYYY-MM-DD HH:mm:ss'))}
                    showTime
                    {...rest}
                ></DatePicker>;
            case 'HH:mm':
                return <TimePicker
                    format='HH:mm'
                    value={this.strToDate(value, 'HH:mm')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'HH:mm'))}
                    {...rest}
                ></TimePicker>;
            case 'HH:mm:ss':
                return <TimePicker
                    value={this.strToDate(value, 'HH:mm:ss')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'HH:mm:ss'))}
                    {...rest}
                ></TimePicker>;
            default:
                return <div>未知组件 {formattedType}</div>;
        }

    }

    strToDate(value, fmt) {
        if (value != null && value !== '') {
            const type = typeof value;
            if (type === 'string' || type === 'number') {
                return dayjs(value, fmt);
            }
        }

        return value;
    }

    dateToStr(date, fmt) {
        return date ? date.format(fmt) : null;
    }

}