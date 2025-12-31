import {DictUtils} from "../utils";
import {Tag} from "antd";
import {ViewStringProps} from "./ViewProps";

interface ViewProps {
    value: String;
}

/**
 * 查看审批状态组件
 * @param props
 * @constructor
 */
export function ViewApproveStatus(props:ViewStringProps) {
    let {value} = props;
    let txt=   DictUtils.dictLabel('approveStatus', value)

    const colorMap = {
        'DRAFT': 'orange',
        'PENDING': 'blue',
        'APPROVED': 'green',
        'REJECTED': 'red'
    };
    let color = colorMap[value]
    return   <Tag color={color}>{txt}</Tag> ;
}
