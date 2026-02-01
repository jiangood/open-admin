import React from "react";
import {Button} from "antd";
import {MessageUtils} from "../../utils";
import {ViewProcessInstanceProgress} from "../ViewProcessInstanceProgress";

export class ViewProcessInstanceProgressButton extends React.Component {
    state = {
        open:false,
    }

    onClick = () => {
        console.log('点击追踪流程')
        let content = <ViewProcessInstanceProgress value={this.props.value} />;
        MessageUtils.alert(content,{
            title:'流程审批信息',
            width:800
        })
    };
    render() {
       return <Button onClick={this.onClick} size='small'>追踪流程</Button>
    }


}