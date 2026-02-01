export function ViewBoolean(props) {
    let {value} = props;
    return value == null ? null : (value ? '是' : '否')
}