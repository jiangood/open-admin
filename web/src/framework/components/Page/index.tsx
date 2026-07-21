import React from "react";
import { Typography } from "antd";
import './index.less'
import {ThemeUtils} from "../../utils";
import { ButtonList } from "../system/ButtonList";

interface PageProps {
    padding?: boolean;
    backgroundGray?: boolean;
    debug?: boolean;
    title?: React.ReactNode;
    description?: React.ReactNode;
    actions?: React.ReactNode;
    children?: React.ReactNode;
}

export const Page: React.FC<PageProps> = ({ padding = true, backgroundGray = false, debug = false, title, description, actions, children }) => {
    const hasHeader = title != null;
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
        {hasHeader && (
            <div className="oa-page-header">
                <div className="oa-page-header-top">
                    <div className="oa-page-header-left">
                        <div>
                            {title && <Typography.Title level={5} style={{ margin: 0 }}>{title}</Typography.Title>}
                            {description && <div><Typography.Text type="secondary">{description}</Typography.Text></div>}
                        </div>
                    </div>
                    {actions && <ButtonList>{actions}</ButtonList>}
                </div>
            </div>
        )}
        {children}
    </div>
};