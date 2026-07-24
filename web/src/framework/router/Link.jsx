import React from 'react';
import {history} from './history';

export function Link({to, children, ...rest}) {
    return (
        <a href={'#' + to}
           onClick={e => {
               e.preventDefault();
               history.push(to);
           }}
           {...rest}>
            {children}
        </a>
    );
}
