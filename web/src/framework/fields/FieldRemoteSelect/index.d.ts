import React from 'react';
import { SelectProps } from "antd/es/select";
import { FieldProps } from '../types';

interface FieldRemoteSelectProps extends Omit<SelectProps, 'value' | 'onChange'>, FieldProps<any> {
    /**
     * 请求地址
     */
    url: string;
    /**
     * 防抖时间（毫秒）
     */
    debounceTime?: number;
    /**
     * 请求参数处理函数
     */
    paramsProcessor?: (params: any) => any;
    /**
     * 响应数据处理函数
     */
    dataProcessor?: (data: any) => any;
    /**
     * 缓存大小
     */
    cacheSize?: number;
    /**
     * 初始加载
     */
    initialLoad?: boolean;
    /**
     * 搜索时是否清空已选项
     */
    clearOnSearch?: boolean;
    /**
     * 请求失败回调
     */
    onError?: (error: any) => void;
    /**
     * 加载完成回调
     */
    onLoad?: (data: any) => void;
}

export const FieldRemoteSelect: React.FC<FieldRemoteSelectProps>;