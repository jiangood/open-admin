// @ts-ignore
import React from "react";

declare type FieldTableSelectProps = {
    url:string;
    placeholder?:string;
    columns: {
        title: string;
        dataIndex: string;
    }[];
};

/**
 * 下拉表格
 *
 * 后端参考接口：
 */
export class FieldTableSelect extends React.Component<FieldTableSelectProps, any> {
}
