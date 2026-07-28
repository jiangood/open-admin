import React from 'react';
import { Space } from 'antd';
import { PermUtils } from '../utils';

interface PermActionsProps {
    gap?: boolean;
    children?: React.ReactNode;
}

export class PermActions extends React.Component<PermActionsProps> {
    render() {
        const { children, gap = true } = this.props;

        const checkPerm = (element) => {
            const _props = element?.props;
            return !_props?.perm || PermUtils.hasPermission(_props.perm);
        };

        const nodes = React.Children.toArray(children).filter(
            (child) => child != null && checkPerm(child)
        );

        if (!gap) return <>{nodes}</>;
        return <Space>{nodes}</Space>;
    }
}
