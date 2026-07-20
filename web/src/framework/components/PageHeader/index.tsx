import React from "react";
import './index.less'

export interface BreadcrumbItem {
    title: React.ReactNode;
    href?: string;
}

interface PageHeaderProps {
    title?: React.ReactNode;
    breadcrumb?: BreadcrumbItem[];
    extra?: React.ReactNode;
    children?: React.ReactNode;
    style?: React.CSSProperties;
    className?: string;
}

export const PageHeader: React.FC<PageHeaderProps> = ({
    title,
    breadcrumb,
    extra,
    children,
    style,
    className,
}) => {
    const showBreadcrumb = breadcrumb && breadcrumb.length > 0;

    return <div className={'tmgg-page-header' + (className ? ' ' + className : '')} style={style}>
        <div className="tmgg-page-header-top">
            <div className="tmgg-page-header-left">
                {showBreadcrumb && (
                    <div className="tmgg-page-header-breadcrumb">
                        {breadcrumb.map((item, idx) => {
                            const isLast = idx === breadcrumb.length - 1;
                            return <React.Fragment key={idx}>
                                {idx > 0 && <span className="tmgg-page-header-breadcrumb-sep">/</span>}
                                {isLast
                                    ? <span className="tmgg-page-header-breadcrumb-current">{item.title}</span>
                                    : item.href
                                        ? <a className="tmgg-page-header-breadcrumb-link" href={item.href}>{item.title}</a>
                                        : <span className="tmgg-page-header-breadcrumb-item">{item.title}</span>
                                }
                            </React.Fragment>
                        })}
                    </div>
                )}
                {title && <h2 className="tmgg-page-header-title">{title}</h2>}
            </div>
            {extra && <div className="tmgg-page-header-extra">{extra}</div>}
        </div>
        {children && <div className="tmgg-page-header-content">{children}</div>}
    </div>
}
