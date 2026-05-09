export function ViewBoolean(props) {
    const {value} = props;
    return value == null ? null : (value ? '是' : '否')
}