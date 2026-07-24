import React from 'react';
import {history} from './history';

export function Link({to, children, ...rest}) {
    const {onClick: userOnClick, ...domProps} = rest;
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
