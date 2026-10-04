/**
 * 根据时间类型自动渲染时间选择组件
 */
import React from "react";
import dayjs from "dayjs";
import {DatePicker, TimePicker} from "antd";
import {DateUtils} from "../../utils";
import {DATE_PICKER_SPECS} from '../datePickerSpecs';
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
    [key: string]: unknown;
}

export class FieldDate extends React.Component<FieldDateProps> {
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

        const Picker = spec.time ? TimePicker : DatePicker;
        return <Picker
            value={this.strToDate(value, formattedType)}
            onChange={v => onChange?.(this.dateToStr(v, formattedType))}
            {...spec}
            {...rest}
        />;

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