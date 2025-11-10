import React from "react";
import {ColumnsType} from "antd/es/table";

declare type FieldEditTableProps = {
    columns:  ColumnsType,
    value?: any[]
    onChange?: (list:any[])=>{}
};

export class FieldEditTable extends React.Component<FieldEditTableProps, any> {


}
