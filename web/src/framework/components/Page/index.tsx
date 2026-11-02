import React from "react";
import { Space } from "antd";
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
    /** 右侧操作区：推荐传 PermAction[]（数据驱动），兼容带 perm 属性的节点 */
    actions?: PermAction[] | React.ReactNode;
    children?: React.ReactNode;
}

export class Page extends React.Component<PageProps> {
    render() {
        const { padding = true, debug = false, actions, children } = this.props;
        const style: React.CSSProperties = {};

        if (padding) {
            style.padding = 16;
        }
        // 页面底色由 .oa-page 统一使用 --oa-color-bg-layout，避免内联值脱离变量体系
        if (debug) {
            style.backgroundColor = 'rgba(255, 0, 0, 0.08)';
        }

        return <div className={'oa-page'} style={style}>
            {actions != null && (
                <div className="oa-page-header">
                    <div className="oa-page-header-top">
                        <div className="oa-page-header-left"/>
                        {Array.isArray(actions)
                            ? <PermActions actions={actions}/>
                            : renderActionNodes(actions)}
                    </div>
                </div>
            )}
            {children}
        </div>;
    }
}