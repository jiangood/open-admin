import React from "react";
import {TreeProps} from "antd";
import { FieldProps } from '../types';

/**
 * 多选树
 *
 * 区别于下拉框，是扁平展示的树
 * 这种需要扁平展示的树，通常都是多选。
 *
 */
interface FieldRemoteTreeProps extends Omit<TreeProps, 'treeData' | 'checkedKeys' | 'onCheck' | 'multiple' | 'checkable' | 'defaultExpandAll' | 'checkStrictly'>, FieldProps<string[]> {
    /**
     * 请求地址
     */
    url: string;
}

export class FieldRemoteTree extends React.Component<FieldRemoteTreeProps, string> {
}