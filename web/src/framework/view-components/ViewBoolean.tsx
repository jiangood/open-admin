import {ViewBooleanProps} from "./ViewProps";

export function ViewBoolean(props: ViewBooleanProps) {
    let {value} = props;
    return value == null ? null : (value ? '是' : '否')
}
