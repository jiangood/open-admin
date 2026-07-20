import React from "react";
import './index.less'
import {ThemeUtils} from "../../utils";

interface PageProps {
    padding?: boolean;
    backgroundGray?: boolean;
    debug?: boolean;
    children?: React.ReactNode;
}

export const Page: React.FC<PageProps> = ({ padding = true, backgroundGray = false, debug = false, children }) => {
    const style: React.CSSProperties = {};
    if (padding) {
        style.padding = 16;
    }
    if (backgroundGray) {
        style.backgroundColor = ThemeUtils.getColor("background-color");
    }
    if (debug) {
        style.backgroundColor = 'rgba(255, 0, 0, 0.08)';
    }

    return <div className={'oa-page'} style={style}>
        {children}
    </div>
};
