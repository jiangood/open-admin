import React from "react";
import {SelectProps} from "antd/es/select";
import { FieldProps } from '../types';

interface FieldDictSelectProps extends Omit<SelectProps, 'options' | 'children'|'mode' | 'value' | 'onChange'>, FieldProps<any> {
    /**
     * 字典类型编码
     */
    typeCode: string;
}

export function FieldDictSelect(props: FieldDictSelectProps);