import {ReactNode} from "react";
import {PermUtils} from "../../utils";

/**
 * 使用该组件，可以判断权限
 */
export function HasPerm(props: { code: string, children: ReactNode }) {
    let {code} = props;

    if (PermUtils.hasPermission(code)) {
        return props.children;
    }

}
