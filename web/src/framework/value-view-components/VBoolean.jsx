export function VBoolean(props) {
    let {value} = props;
    return value == null ? null : (value ? '是' : '否')
}
