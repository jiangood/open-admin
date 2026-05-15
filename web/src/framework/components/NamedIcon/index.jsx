import * as Icons from '@ant-design/icons';
import { WarningOutlined } from '@ant-design/icons';

export function NamedIcon(props) {
    const {name, ...rest} = props;
    const IconType = Icons[name];

    if (IconType) {
        return <IconType {...rest}></IconType>;
    }

    console.warn(`NamedIcon: icon "${name}" not found, using fallback`);
    return <WarningOutlined {...rest} />;
}