import React from "react";
import { Typography } from "antd";
import './index.less'
import {getToken} from "../../config";
import { PermActions } from "../../biz/PermActions";

interface PageProps {
    padding?: boolean;
    backgroundGray?: boolean;
    debug?: boolean;
    title?: React.ReactNode;
    description?: React.ReactNode;
    actions?: React.ReactNode;
    children?: React.ReactNode;
}

export class Page extends React.Component<PageProps> {
    render() {
        const { padding = true, backgroundGray = false, debug = false, title, description, actions, children } = this.props;
        const hasHeader = title != null || description != null || actions != null;
        const style: React.CSSProperties = {};

        if (padding) {
            style.padding = 16;
        }
        const token = getToken();
        if (backgroundGray) {
            style.backgroundColor = token.colorBgLayout;
        }
        if (debug) {
            style.backgroundColor = 'rgba(255, 0, 0, 0.08)';
        }
        if (!style.backgroundColor) {
            style.backgroundColor = token.colorBgLayout;
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
                        {actions && <PermActions>{actions}</PermActions>}
                    </div>
                </div>
            )}
            {children}
        </div>;
    }
}