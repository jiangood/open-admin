import React from 'react';
import {Button, Modal, Popover, Tooltip} from 'antd';
import {StrUtil} from "@tmgg/tmgg-commons-lang";


export class ViewBoolean extends React.Component {


    render() {
        let {value} = this.props;
        if(value == null){
            return null;
        }

        return  value ? '是': '否'
    }
}
