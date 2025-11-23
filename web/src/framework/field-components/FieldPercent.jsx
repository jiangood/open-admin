import {InputNumber, Space} from 'antd';
import React from 'react';

export class FieldPercent extends React.Component {
    render() {
        const {value, onChange, ...rest} = this.props;


        return (
            <Space.Compact>
                <InputNumber
                    min={0}
                    max={100}
                    value={ Number((value * 100).toFixed(0))}
                    onChange={v => {
                        v = (v / 100).toFixed(2);
                        onChange(Number(v))
                    }}
                    {...rest}
                />
                <Space.Addon>%</Space.Addon>
            </Space.Compact>

        );
    }
}

