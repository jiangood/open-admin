import React from "react";
import {Checkbox, Input, Space} from "antd";
import {EyeInvisibleOutlined, EyeOutlined} from "@ant-design/icons";

export  class FieldPassword extends React.Component {


    render() {
        return <Input.Password {...this.props}/>
    }

}
