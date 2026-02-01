import React from 'react';
import { Tag } from 'antd';

export class ViewBooleanEnableDisable extends React.Component {

    render() {
        const { value } = this.props;

        if (value == null) {
            return null;
        }

        return value ? <Tag color={"green"}>启用</Tag> : <Tag color={"red"}>禁用</Tag>;
    }
}