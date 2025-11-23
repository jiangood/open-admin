import React from 'react';
import { SelectProps } from 'antd/es/select';

export interface FieldBooleanProps  {

    type?:'select' | 'radio' | 'checkbox' |  'switch'; // 默认 select
    value?: boolean;
    onChange?: (value: boolean) => void;

}

export class FieldBoolean extends React.Component<FieldBooleanProps, any> {}

