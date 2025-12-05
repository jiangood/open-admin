import React from "react";
import {ColumnsType} from "antd/es/table";

declare type FieldTableProps = {
    columns:  ColumnsType,
    value?: any[]
    onChange?: (list:any[])=>{},
    style?: React.CSSProperties
};

/***
 * 可编辑表格
 */
export class FieldTable extends React.Component<FieldTableProps, any> {


}
