import React from 'react';
import { FieldProps } from '../types';

interface FieldRemoteTreeSelectProps extends FieldProps<string | number> {
    /**
     * 请求地址
     */
    url: string;

    /**
     * 默认展开所有
     */
    treeDefaultExpandAll?: boolean;
    
    id?: string;
}

export class FieldRemoteTreeSelect extends React.Component<FieldRemoteTreeSelectProps, any> {
}