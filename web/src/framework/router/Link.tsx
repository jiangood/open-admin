import React, {type AnchorHTMLAttributes} from 'react';
import {history} from './history';

interface LinkProps extends Omit<AnchorHTMLAttributes<HTMLAnchorElement>, 'onClick'> {
    to: string;
    onClick?: React.MouseEventHandler<HTMLAnchorElement>;
}

export class Link extends React.Component<LinkProps> {
    render() {
        const {to, children, onClick: userOnClick, ...domProps} = this.props;
        return (
            <a href={'#' + to}
               {...domProps}
               onClick={e => {
                   userOnClick?.(e);
                   if (!e.defaultPrevented) {
                       e.preventDefault();
                       history.push(to);
                   }
               }}>
                {children}
            </a>
        );
    }
}
