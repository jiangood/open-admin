import React from 'react';
import { SelectProps } from 'antd/es/select';

export interface FRemoteSelectMultipleProps extends Omit<SelectProps, 'options' | 'children'> {

    /**
     * 请求地址
     */
    url: string ;
}

export class FRemoteSelectMultiple extends React.Component<FRemoteSelectMultipleProps, any> {}

