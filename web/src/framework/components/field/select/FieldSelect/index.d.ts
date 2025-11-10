// @ts-ignore
import React from "react";

declare type FieldSelectProps = {

    url:  string,
    value?: any
    onChange?: (value:any)=>{},

    // 数据加载完
    onLoadDataFinish?:(list:any[])=>{};

    showRefresh?:boolean;

    placeholder?:string


    disabled?:false;

    /**
     * 是否多选
     */
    multiple?:boolean;

    /**
     *  多选选时返回对象数组
     *
     *  primitive 原始值，如123 , 多选时时数组， 如[123,456]
     *  object  包装成对象的值， 如 {id:123}, 多选时 [{id:123},{id:456}]
     *  joined  逗号分割的字符串， 如123,456
     *
     */
    valueType?: 'primitive'| 'object'  | 'joined' | undefined | null;


};

export class FieldSelect extends React.Component<FieldSelectProps, any> {
}
