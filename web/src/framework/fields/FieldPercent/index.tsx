import {InputNumber, Space} from 'antd';
import React from 'react';

/**
 * 数字的百分数输入框
 */
export class FieldPercent extends React.Component {
    render() {
        const {value, onChange, ...rest} = this.props;

        return (
            <Space.Compact>
                <InputNumber
                    min={0}
                    max={100}
                    value={Number((value * 100).toFixed(0))}
                    onChange={v => {
                        if (v !== null) {
                            const percentValue = Number((v / 100).toFixed(2));
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