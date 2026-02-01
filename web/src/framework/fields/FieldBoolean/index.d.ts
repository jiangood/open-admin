import React from 'react';
import { FieldProps } from '../types';

interface FieldBooleanProps extends FieldProps<boolean> {
    type?: 'select' | 'radio' | 'checkbox' | 'switch'; // 默认 select
}

export class FieldBoolean extends React.Component<FieldBooleanProps, any> {
}