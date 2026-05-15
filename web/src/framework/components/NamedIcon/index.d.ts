import React from 'react';

type NamedIconProps = {
    name: string;
} & React.ComponentProps<typeof import('@ant-design/icons').WarningOutlined>;

export function NamedIcon(props: NamedIconProps): React.ReactElement;