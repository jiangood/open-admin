import React from 'react';
import {Button, Modal, Popover, Tag, Tooltip} from 'antd';
import {StrUtil} from "@tmgg/tmgg-commons-lang";


export class ViewBooleanEnableDisable extends React.Component {


    render() {
        let {value} = this.props;
        if(value == null){
            return null;
        }

        return  value ? <Tag color={"green"}>启动</Tag> : <Tag color={"red"}>禁用</Tag>
    }
}
