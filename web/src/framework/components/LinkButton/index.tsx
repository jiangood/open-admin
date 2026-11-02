import React from "react";
import {Button} from "antd";
import {PageUtils} from "../../utils";

/**
 * 链接跳转按钮。
 * `label` 参数已弃用：目标页面的标题请使用 pageMeta.title。
 */
export function LinkButton(props) {
    const {path, label, children, size = 'small', ...rest} = props;
    return <Button size={size} {...rest} onClick={() => {
        PageUtils.open(path, label)
    }}>{children}</Button>
}
