import type {ViewProps} from "../types";

export function ViewBoolean(props: ViewProps<boolean>) {
    const {value} = props;
    return value == null ? null : (value ? '是' : '否');
}
