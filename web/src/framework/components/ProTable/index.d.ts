// @ts-ignore
import React from "react";
import {ColumnsType} from "antd/es/table";
import {FormInstance} from "antd";

declare type ProTableProps = {
    columns:  ColumnsType,
    // 请求数据，注意：需处理下载
    request: (params:any)=>{};

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

    // 如果是false，则不显示
    toolbarOptions?:{
        /**
         * 显示搜索框
         */
        showSearch?:boolean;
    }|boolean;


    /**
     *  垂直滚动条， true自动计算， 也可指定高度
     */
    scrollY?: boolean | number
};

export class ProTable extends React.Component<ProTableProps, any> {
}
