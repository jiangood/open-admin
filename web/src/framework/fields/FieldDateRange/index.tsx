/**
 * 根据时间类型自动渲染时间选择组件
 */
import React from "react";
import dayjs from "dayjs";
import {DatePicker, TimePicker} from "antd";
import {DateUtils, StringUtils} from "../../utils";
import {DATE_PICKER_SPECS} from '../datePickerSpecs';
import type {FieldProps} from '../types';

const SP = StringUtils.ISO_SPLITTER;

/** 日期范围值，形如 "2024-01-01/2024-01-31"（起止以 / 分隔） */
export type FieldDateRangeValue = string; // NOSONAR: npm 包导出类型，业务项目引用

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
    [key: string]: unknown;
}

export class FieldDateRange extends React.Component<FieldDateRangeProps> {
    static readonly defaultProps = {
        type: 'YYYY-MM-DD'
    };

    render() {
        const {type, value, onChange, ...rest} = this.props;
        const formattedType = DateUtils.convertTypeToFormat(type as string);
        const spec = DATE_PICKER_SPECS[formattedType];

        if (!spec) {
            return <div>未知组件 {formattedType}</div>;
        }

        const Picker = spec.time ? TimePicker.RangePicker : DatePicker.RangePicker;
        return <Picker
            value={this.strToDate(value)}
            onChange={v => onChange?.(this.dateToStr(v, formattedType))}
            {...spec}
            {...rest}
        />;

    }

    strToDate(v: string | null | undefined): [dayjs.Dayjs, dayjs.Dayjs] | null {
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