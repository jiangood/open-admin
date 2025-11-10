// @ts-ignore
import React from "react";

declare type FieldTableSelectProps = {
    /**
     * 远程方法连接， 返回的data数据为 io.tmgg.lang.obj.Table
     */
    url:string;

    type: 'checkbox' | 'radio';



    /**
     * 显示的字段
     */
    labelKey: string;

    placeholder?:string;

};

/**
 * 下拉表格
 *
 * 后端参考接口：
 */
export class FieldTableSelect extends React.Component<FieldTableSelectProps, any> {
}
