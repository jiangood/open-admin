import {DictUtils} from "../../utils";
import {Tag} from "antd";
import {Component} from "react";

/**
 * 查看审批状态组件
 * @param props
 * @constructor
 */
export class ViewApproveStatus extends Component {
    render() {
        const {value} = this.props;
       return  DictUtils.dictTag('approveStatus', value)
    }
}