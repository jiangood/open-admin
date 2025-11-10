import React from "react";
import {Typography} from "antd";

export class ViewText extends React.Component{

    render() {
        return <Typography.Text> {this.props.value}</Typography.Text>;
    }
}
