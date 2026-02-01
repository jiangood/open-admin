// @ts-ignore
import React from "react";
import {ColumnsType} from "antd/es/table";

declare type ProTableProps = {
    columns:  ColumnsType,
    /**
     * 请求函数
      */
    request: (params:any)=>{};

    rowKey?: string,

    // 选择行，通ant table
    rowSelection?: any,

    toolBarRender?: (params:any, {selectedRows:[],selectedRowKeys:[]})=>{},
    bordered?:boolean,

    /**
     * 默认每页数量
     */
    defaultPageSize?:number,

    /**
     * 查询表单的引用
     */
    formRef?: React.Ref<any>,



    showToolbarSearch?:boolean


    /**
     *  垂直滚动条， true自动计算， 也可指定高度
     */
    scrollY?: boolean | number
};

export class ProTable extends React.Component<ProTableProps, any> {
}
