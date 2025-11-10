import { InputNumber } from 'antd';
import React from 'react';

export class InputNumberPercent extends React.Component {
  render() {
    const { props } = this;
    const { value, mode, ...rest } = props;

    if (mode === 'read') {
      return (value * 100).toFixed(2) + '%';
    }

    return (
      <InputNumber
        min={0}
        max={1}
        step={0.01}
        formatter={(value) => (value * 100).toFixed(2)}
        parser={(value) => (value / 100).toFixed(2)}
        addonAfter={'%'}
        value={value}
        {...rest}
      />
    );
  }
}

export { InputNumberPercent as FieldInputNumberPercent };
