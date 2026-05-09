import * as Icons from '@ant-design/icons';

type NamedIconProps = {
    name: string
};

export function NamedIcon(props: NamedIconProps) {
    const {name, ...rest} = props;
    const IconType = Icons[name];

    if (IconType) {
        return <IconType {...rest}></IconType>;
    }
}
