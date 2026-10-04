import {DictUtils} from "../../utils";
import {Component} from "react";
import type {ViewProps} from "../types";

export class ViewApproveStatus extends Component<ViewProps<string>> {
    render() {
        const {value} = this.props;
        return DictUtils.dictTag('approveStatus', value);
    }
}
