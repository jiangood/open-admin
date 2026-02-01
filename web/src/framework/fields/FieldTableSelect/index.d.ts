import React from "react";
import { FieldProps } from '../types';
import {ColumnsType} from "antd/es/table";

/**
 * 下拉表格
 *
 * 后端参考接口：
 */
interface FieldTableSelectProps extends FieldProps<string | number> {
    url: string;
    placeholder?: string;

    // antd table 的列表
    columns: ColumnsType<any>;
}

export class FieldTableSelect extends React.Component<FieldTableSelectProps, any> {
}