import React from "react";
import {Alert, Spin} from "antd";
import {LoadingOutlined} from "@ant-design/icons";
import {getToken} from "../../config";
import './index.less'

export interface PageLoadingProps {
    message?: string;
    messages?: string[];
}

export function PageLoading(props: PageLoadingProps) { // NOSONAR: 仅读取 props，函数组件无需变更
    const titles = props.messages || (props.message ? [props.message] : ['页面加载中...']);
    const primaryColor = getToken().colorPrimary ?? "";

    return (
        <div className="oa-page-loading">
            <div className="oa-page-loading-content">
                <div>
                    <Spin indicator={<LoadingOutlined style={{fontSize: 48, color: primaryColor}} spin/>}/>
                </div>
                <div className="oa-page-loading-messages">
                    {titles.map((msg, i) => ( // NOSONAR: 提示文案列表静态无稳定 key
                        <Alert
                            key={i}
                            title={<span style={{color: primaryColor}}>{msg}</span>}
                            type="info"
                            showIcon
                            style={{
                                marginBottom: i < titles.length - 1 ? 8 : 0,
                                border: `1px solid ${primaryColor}20`,
                                background: `${primaryColor}08`,
                            }}
                        />
                    ))}
                </div>
            </div>
        </div>
    );
}
