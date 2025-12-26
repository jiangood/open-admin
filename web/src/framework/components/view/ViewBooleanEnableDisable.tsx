import React from 'react';
import { Tag } from 'antd';

// 定义组件的 Props 接口
interface ViewBooleanEnableDisableProps {
    value: boolean | null | undefined;
}

export class ViewBooleanEnableDisable extends React.Component<ViewBooleanEnableDisableProps, any> {

    render() {
        const { value } = this.props;

        if (value == null) {
            return null;
        }

        return value ? <Tag color={"green"}>启用</Tag> : <Tag color={"red"}>禁用</Tag>;
    }
}
