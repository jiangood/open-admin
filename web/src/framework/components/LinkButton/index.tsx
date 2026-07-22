import React from "react";
import {Button} from "antd";
import {PageUtils} from "../../utils";

export function LinkButton(props) {
    const {path, label, children, size = 'small', ...rest} = props;
    return <Button size={size} {...rest} onClick={() => {
        PageUtils.open(path, label)
    }}>{children}</Button>
}
