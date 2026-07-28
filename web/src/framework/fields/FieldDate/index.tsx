/**
 * 根据时间类型自动渲染时间选择组件
 */
import React from "react";
import dayjs from "dayjs";
import {DatePicker, TimePicker} from "antd";
import {DateUtils} from "../../utils";
import type {FieldProps} from '../types';

export interface FieldDateProps extends FieldProps<string> {
    /** 日期类型，如 YYYY-MM-DD、YYYY-MM、HH:mm:ss 等，默认 YYYY-MM-DD */
    type?: string;
    /** 占位文本（透传给 DatePicker/TimePicker） */
    placeholder?: string;
    /** 是否禁用 */
    disabled?: boolean;
    /** 自定义样式 */
    style?: React.CSSProperties;
    /** 其余属性透传给 DatePicker/TimePicker */
    [key: string]: any;
}

export class FieldDate extends React.Component<FieldDateProps> {
    static defaultProps = {
        type: 'YYYY-MM-DD'
    };

    render() {
        const {type, value, onChange, ...rest} = this.props;
        const formattedType = DateUtils.convertTypeToFormat(type as string);

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

    strToDate(value: string | number | dayjs.Dayjs | null | undefined, fmt: string): dayjs.Dayjs | null | undefined {
        if (value != null && value !== '') {
            const type = typeof value;
            if (type === 'string' || type === 'number') {
                return dayjs(value as string | number, fmt);
            }
        }

        return value as dayjs.Dayjs | null | undefined;
    }

    dateToStr(date: dayjs.Dayjs | null | undefined, fmt: string): string {
        // 清空时 date 为 null，此时按 null 回调给父组件
        return date ? date.format(fmt) : (null as unknown as string);
    }

}