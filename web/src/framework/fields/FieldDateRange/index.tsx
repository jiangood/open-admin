/**
 * 根据时间类型自动渲染时间选择组件
 */
import React from "react";
import dayjs from "dayjs";
import {DatePicker, TimePicker} from "antd";
import {DateUtils, StringUtils} from "../../utils";
import type {FieldProps} from '../types';

const SP = StringUtils.ISO_SPLITTER;

/** 日期范围值，形如 "2024-01-01/2024-01-31"（起止以 / 分隔） */
export type FieldDateRangeValue = string;

export interface FieldDateRangeProps extends FieldProps<FieldDateRangeValue> {
    /** 日期类型，如 YYYY-MM-DD、YYYY-MM、HH:mm:ss 等，默认 YYYY-MM-DD */
    type?: string;
    /** 占位文本（透传给 RangePicker） */
    placeholder?: string | [string, string];
    /** 是否禁用 */
    disabled?: boolean | [boolean, boolean];
    /** 自定义样式 */
    style?: React.CSSProperties;
    /** 其余属性透传给 RangePicker */
    [key: string]: any;
}

export class FieldDateRange extends React.Component<FieldDateRangeProps> {
    static defaultProps = {
        type: 'YYYY-MM-DD'
    };

    render() {
        const {type, value, onChange, ...rest} = this.props;
        const formattedType = DateUtils.convertTypeToFormat(type as string);

        switch (formattedType) {
            case 'YYYY':
                return <DatePicker.RangePicker
                    value={this.strToDate(value, 'YYYY')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'YYYY'))}
                    picker="year"
                    {...rest}
                />;
            case 'YYYY-MM':
                return <DatePicker.RangePicker
                    value={this.strToDate(value, 'YYYY-MM')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'YYYY-MM'))}
                    picker="month"
                    {...rest}
                />;
            case 'YYYY-QQ':
                return <DatePicker.RangePicker
                    value={this.strToDate(value, 'YYYY-QQ')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'YYYY-QQ'))}
                    picker="quarter"
                    {...rest}
                />;
            case 'YYYY-MM-DD':
                return <DatePicker.RangePicker
                    value={this.strToDate(value, 'YYYY-MM-DD')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'YYYY-MM-DD'))}
                    {...rest}
                />;
            case 'YYYY-MM-DD HH:mm':
                return <DatePicker.RangePicker
                    value={this.strToDate(value, 'YYYY-MM-DD HH:mm')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'YYYY-MM-DD HH:mm'))}
                    format='YYYY-MM-DD HH:mm'
                    showTime
                    {...rest}
                />;
            case 'YYYY-MM-DD HH:mm:ss':
                return <DatePicker.RangePicker
                    value={this.strToDate(value, 'YYYY-MM-DD HH:mm:ss')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'YYYY-MM-DD HH:mm:ss'))}
                    showTime
                    {...rest}
                />;
            case 'HH:mm':
                return <TimePicker.RangePicker
                    format='HH:mm'
                    value={this.strToDate(value, 'HH:mm')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'HH:mm'))}
                    {...rest}
                />;
            case 'HH:mm:ss':
                return <TimePicker.RangePicker
                    value={this.strToDate(value, 'HH:mm:ss')}
                    onChange={v => onChange && onChange(this.dateToStr(v, 'HH:mm:ss'))}
                    {...rest}
                />;
            default:
                return <div>未知组件 {formattedType}</div>;
        }

    }

    strToDate(v: string | null | undefined, fmt: string): [dayjs.Dayjs, dayjs.Dayjs] | null {
        if (!v) {
            return null;
        }

        const arr = v.split(SP);
        const s1 = arr[0];
        const s2 = arr[1];
        return [dayjs(s1), dayjs(s2)];
    }

    dateToStr(dateArr: [dayjs.Dayjs | null, dayjs.Dayjs | null] | null, fmt: string): string {
        if (dateArr == null) {
            return "";
        }

        const arr = dateArr as [dayjs.Dayjs | null, dayjs.Dayjs | null];
        const d1 = arr[0];
        const d2 = arr[1];

        const s1 = d1 ? d1.format(fmt) : "";
        const s2 = d2 ? d2.format(fmt) : "";

        return s1 + SP + s2;
    }

}