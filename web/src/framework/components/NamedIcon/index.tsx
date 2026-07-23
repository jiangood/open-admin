import * as Icons from '@ant-design/icons';
import { WarningOutlined } from '@ant-design/icons';
import React from 'react';

type NamedIconProps = {
    name: string;
} & React.ComponentProps<typeof WarningOutlined>;

export function NamedIcon(props: NamedIconProps): React.ReactElement {
    const {name, ...rest} = props;
    const IconType = Icons[name as keyof typeof Icons];

    if (IconType) {
        return <IconType {...rest}></IconType>;
    }

    console.warn(`NamedIcon: icon "${name}" not found, using fallback`);
    return <WarningOutlined {...rest} />;
}
