import React from "react";
import {Button} from "antd";
import {PageUtil} from "../system";

export class LinkButton extends React.Component {

    render() {
        const {path,label, children, size='small', ...rest} = this.props;
        return <Button size={size} {...rest} onClick={()=>{
            PageUtil.open(path,label)
        }}>{children}</Button>
    }
}
