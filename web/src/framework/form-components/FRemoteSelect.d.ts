import React from 'react';
import { SelectProps } from 'antd/es/select';

export interface FRemoteSelectProps extends Omit<SelectProps, 'options' | 'children'> {

    /**
     * 请求地址
     */
    url: string ;
}

declare class FRemoteSelect extends React.Component<FRemoteSelectProps, any> {}

export default FRemoteSelect;