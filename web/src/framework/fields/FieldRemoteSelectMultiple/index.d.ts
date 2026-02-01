import React from 'react';
import {SelectProps} from "antd/es/select";

interface FieldRemoteSelectMultipleProps extends Omit<SelectProps, 'options' | 'children'| 'mode' | 'value' | 'onChange'> {
    /**
     * 请求地址
     */
    url: string;
    /**
     * 选中的值
     */
    value?: any[];
    /**
     * 值变化回调
     */
    onChange?: (value: any[]) => void;
}

export class FieldRemoteSelectMultiple extends React.Component<FieldRemoteSelectMultipleProps, any> {
}