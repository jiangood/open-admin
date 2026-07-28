import React, {ReactNode} from "react";
import {PermUtils} from "../utils";

export class Perm extends React.Component<{ code: string, children: ReactNode }> {
    render() {
        if (PermUtils.hasPermission(this.props.code)) {
            return this.props.children;
        }
        return null;
    }
}
