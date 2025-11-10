import React from 'react';

export interface InputNumberPercentProps {
  mode?: string | 'read';
  value?: Number;
  onChange?: (value: Number) => void;
}

declare class InputNumberPercent extends React.Component<InputNumberPercentProps, any> {}

declare class FieldInputNumberPercent extends React.Component<InputNumberPercentProps, any> {}
