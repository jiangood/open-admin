import {DictUtils} from "../../utils";
import {Tag} from "antd";
import {Component} from "react";
import type {ViewProps} from "../types";

export class ViewApproveStatus extends Component<ViewProps<String>> {
    render() {
        const {value} = this.props;
        return DictUtils.dictTag('approveStatus', value);
    }
}
