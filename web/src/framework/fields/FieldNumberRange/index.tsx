import React from "react";
import {InputNumber} from "antd";
import {StringUtils} from "../../utils";
import type {FieldProps} from '../types';

const SP = StringUtils.ISO_SPLITTER;

/** 数字范围值，形如 "1/100"（起止以 / 分隔） */
export type FieldNumberRangeValue = string;

export type RangePartValue = number | string | null;

export interface FieldNumberRangeProps extends FieldProps<FieldNumberRangeValue> {
    /** 默认值（形如 "1/100"），value 为空时挂载后回填 */
    defaultValue?: string;
}

export class FieldNumberRange extends React.Component<FieldNumberRangeProps> {
    onChangeA = (a: RangePartValue) => {
        const {b} = this.parse(this.props.value);
        this.props.onChange?.(this.merge(a, b));
    };

    onChangeB = (b: RangePartValue) => {
        const {a} = this.parse(this.props.value);
        this.props.onChange?.(this.merge(a, b));
    };

    merge(a: RangePartValue, b: RangePartValue): string {
        if (a == null) {
            a = '';
        }
        if (b == null) {
            b = '';
        }
        return a + SP + b;
    }

    parse(v: string | null | undefined): { a: number | null; b: number | null } {
        if (v == null) {
            return {a: null, b: null};
        }
        const arr = v.split(SP);
        return {a: arr[0] ? Number(arr[0]) : null, b: arr[1] ? Number(arr[1]) : null};
    }

    componentDidMount() {
        const {value, defaultValue, onChange} = this.props;
        if (value == null && defaultValue) {
            onChange?.(defaultValue);
        }
    }

    render() {
        const {defaultValue} = this.props;
        let {value} = this.props;
        if (value == null) {
            value = defaultValue;
        }
        const {a, b} = this.parse(value);

        return <div style={{display: 'flex', alignItems: 'center'}}>
            <InputNumber value={a} onChange={this.onChangeA}/> - <InputNumber value={b} onChange={this.onChangeB}/>
        </div>;
    }

}