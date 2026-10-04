import React from 'react';
import type {ViewProps} from "../types";

export class ViewBoolean extends React.Component<ViewProps<boolean>> {
    render() {
        const {value} = this.props;
        if (value == null) {
            return null;
        }
        return value ? '是' : '否';
    }
}
