import React from 'react';
import { FieldProps } from '../types';

interface FieldRemoteTreeSelectMultipleProps extends FieldProps<(string | number)[]> {
    /**
     * 请求地址
     */
    url: string;

    /**
     * 默认展开所有
     */
    treeDefaultExpandAll?: boolean;
    
    id?: string;
    style?: React.CSSProperties;
}

export class FieldRemoteTreeSelectMultiple extends React.Component<FieldRemoteTreeSelectMultipleProps, any> {
}