import React from 'react';
import type {ViewProps} from "../types";

export class ViewBoolean extends React.Component<ViewProps<boolean>> {
    render() {
        const {value} = this.props;
        return value == null ? null : (value ? '是' : '否');
    }
}
