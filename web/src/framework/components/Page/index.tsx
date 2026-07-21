import React from "react";
import { Space, Typography } from "antd";
import './index.less'
import {ThemeUtils} from "../../utils";

interface PageProps {
    padding?: boolean;
    backgroundGray?: boolean;
    debug?: boolean;
    title?: React.ReactNode;
    description?: React.ReactNode;
    extra?: React.ReactNode;
    children?: React.ReactNode;
}

export const Page: React.FC<PageProps> = ({ padding = true, backgroundGray = false, debug = false, title, description, extra, children }) => {
    const hasHeader = title != null;
    const style: React.CSSProperties = {};

    if (!hasHeader && padding) {
        style.padding = 16;
    }
    if (backgroundGray) {
        style.backgroundColor = ThemeUtils.getColor("background-color");
    }
    if (debug) {
        style.backgroundColor = 'rgba(255, 0, 0, 0.08)';
    }

    if (hasHeader) {
        return <div className={'oa-page'} style={style}>
            <div className="oa-page-card">
                <div className="oa-page-header">
                    <div className="oa-page-header-top">
                        <div className="oa-page-header-left">
                            <div>
                                {title && <Typography.Title level={5} style={{ margin: 0 }}>{title}</Typography.Title>}
                                {description && <div><Typography.Text type="secondary">{description}</Typography.Text></div>}
                            </div>
                        </div>
                        {extra && <Space>{extra}</Space>}
                    </div>
                </div>
                <div className="oa-page-body">
                    {children}
                </div>
            </div>
        </div>
    }

    return <div className={'oa-page'} style={style}>
        {children}
    </div>
};