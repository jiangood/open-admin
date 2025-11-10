import React from "react";
import {ColumnsType} from "antd/es/table";

declare type EditTableProps = {
    columns:  ColumnsType,
    dataSource: any[]
    onFinish: (list:any[])=>{}
};

export class EditTable extends React.Component<EditTableProps, any> {
}
