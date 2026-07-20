import React from "react";
import './index.less'

export interface BreadcrumbItem {
    title: React.ReactNode;
    href?: string;
}

interface PageHeaderProps {
    title?: React.ReactNode;
    description?: React.ReactNode;
    breadcrumb?: BreadcrumbItem[];
    extra?: React.ReactNode;
    children?: React.ReactNode;
    style?: React.CSSProperties;
    className?: string;
}

export const PageHeader: React.FC<PageHeaderProps> = ({
    title,
    description,
    breadcrumb,
    extra,
    children,
    style,
    className,
}) => {
    const showBreadcrumb = breadcrumb && breadcrumb.length > 0;

    return <div className={'oa-page-header' + (className ? ' ' + className : '')} style={style}>
        <div className="oa-page-header-top">
            <div className="oa-page-header-left">
                {showBreadcrumb && (
                    <div className="oa-page-header-breadcrumb">
                        {breadcrumb.map((item, idx) => {
                            const isLast = idx === breadcrumb.length - 1;
                            return <React.Fragment key={idx}>
                                {idx > 0 && <span className="oa-page-header-breadcrumb-sep">/</span>}
                                {isLast
                                    ? <span className="oa-page-header-breadcrumb-current">{item.title}</span>
                                    : item.href
                                        ? <a className="oa-page-header-breadcrumb-link" href={item.href}>{item.title}</a>
                                        : <span className="oa-page-header-breadcrumb-item">{item.title}</span>
                                }
                            </React.Fragment>
                        })}
                    </div>
                )}
                {title && <h2 className="oa-page-header-title">{title}</h2>}
                {description && <p className="oa-page-header-desc">{description}</p>}
            </div>
            {extra && <div className="oa-page-header-extra">{extra}</div>}
        </div>
        {children && <div className="oa-page-header-content">{children}</div>}
    </div>
}
