import React from 'react'
import {ColumnsType} from "antd/es/table";
import { FieldProps } from '../types';

/**
 * 可编辑表格
 */
interface FieldTableProps extends FieldProps<any[]> {
    columns: ColumnsType<any>;
    style?: React.CSSProperties;
}

export class FieldTable extends React.Component<FieldTableProps, any> {
}