import React from "react";
import { Space, Typography } from "antd";
import './index.less'
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
    /** @deprecated 页面底色已默认跟随布局底色，该参数不再产生差异；保留仅为兼容旧写法 */
    backgroundGray?: boolean;
    debug?: boolean;
    /** @deprecated 页面标题已由菜单 / 标签页提供，页面内无需重复展示；该属性将在后续版本移除 */
    title?: React.ReactNode;
    /** @deprecated 页面描述请直接在内容区使用 Typography 等排版组件；该属性将在后续版本移除 */
    description?: React.ReactNode;
    /** 右侧操作区：推荐传 PermAction[]（数据驱动），兼容带 perm 属性的节点 */
    actions?: PermAction[] | React.ReactNode;
    children?: React.ReactNode;
}

export class Page extends React.Component<PageProps> {
    warnedTitle = false;
    warnedDescription = false;

    /** 已废弃属性仅在首次使用时提示，避免每次渲染刷屏 */
    warnDeprecatedProps(title: React.ReactNode, description: React.ReactNode) {
        if (title != null && !this.warnedTitle) {
            this.warnedTitle = true;
            console.warn('[Page] title 属性已废弃：页面标题由菜单 / 标签页提供，请移除该属性（后续版本将不再支持）。');
        }
        if (description != null && !this.warnedDescription) {
            this.warnedDescription = true;
            console.warn('[Page] description 属性已废弃：请在内容区直接使用 Typography 等排版组件（后续版本将不再支持）。');
        }
    }

    render() {
        const { padding = true, debug = false, title, description, actions, children } = this.props;
        this.warnDeprecatedProps(title, description);
        const hasHeader = title != null || description != null || actions != null;
        const style: React.CSSProperties = {};

        if (padding) {
            style.padding = 16;
        }
        // 页面底色由 .oa-page 统一使用 --oa-color-bg-layout，避免内联值脱离变量体系
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