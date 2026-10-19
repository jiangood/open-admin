import React from "react";
import { Space, Typography } from "antd";
import './index.less'
import {getToken} from "../../config";
import { PermActions, type PermAction } from "../../biz/PermActions";
import { PermUtils } from "../../utils";

/** 兼容旧写法：按子节点的 perm 属性过滤（新代码请用 PermAction[]） */
function renderActionNodes(nodes: React.ReactNode) {
    const visible = React.Children.toArray(nodes).filter((node) => {
        const perm = (node as React.ReactElement<{ perm?: string }>)?.props?.perm;
        return !perm || PermUtils.hasPermission(perm);
    });
    if (visible.length === 0) return null;
    return <Space>{visible}</Space>;
}

interface PageProps {
    padding?: boolean;
    backgroundGray?: boolean;
    debug?: boolean;
    title?: React.ReactNode;
    description?: React.ReactNode;
    /** 右侧操作区：推荐传 PermAction[]（数据驱动），兼容带 perm 属性的节点 */
    actions?: PermAction[] | React.ReactNode;
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
                        {actions != null && (Array.isArray(actions)
                            ? <PermActions actions={actions}/>
                            : renderActionNodes(actions))}
                    </div>
                </div>
            )}
            {children}
        </div>;
    }
}