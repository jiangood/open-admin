import React from "react";
import {Space} from "antd";
import {EyeInvisibleOutlined, EyeOutlined} from "@ant-design/icons";
import {ViewProps, ViewStringProps} from "./ViewProps";

export class ViewPassword extends React.Component<ViewStringProps,any> {

    state = {
        visible: false
    }

    render() {
        let v = this.props.value;
        if (v == null) {
            return null
        }
        let visible = this.state.visible;
        return <Space>
            <span>{this.state.visible ? v : '******'}</span>
            <a onClick={() => this.setState({visible: !visible})}>
                {visible ? <EyeOutlined/> : <EyeInvisibleOutlined/>}
            </a>
        </Space>
    }
}
