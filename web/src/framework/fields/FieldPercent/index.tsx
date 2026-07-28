import {InputNumber, Space} from 'antd';
import type {InputNumberProps} from 'antd';
import React from 'react';
import type {FieldProps} from '../types';

export interface FieldPercentProps extends Omit<InputNumberProps, 'value' | 'onChange'>, FieldProps<number> {
    /** 小数位数，默认 2 */
    precision?: number;
}

/**
 * 数字的百分数输入框
 */
export class FieldPercent extends React.Component<FieldPercentProps> {
    static defaultProps = {
        precision: 2,
    };

    render() {
        const {value, onChange, precision, ...rest} = this.props;

        return (
            <Space.Compact>
                <InputNumber
                    min={0}
                    max={100}
                    value={value != null ? Number((value * 100).toFixed(precision)) : null}
                    onChange={v => {
                        if (v != null) {
                            const percentValue = Number((v / 100).toFixed(precision));
                            onChange && onChange(percentValue);
                        }
                    }}
                    {...rest}
                />
                <Space.Addon>%</Space.Addon>
            </Space.Compact>

        );
    }
}