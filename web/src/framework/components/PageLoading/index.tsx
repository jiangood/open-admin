import React from "react";
import {Alert, Spin} from "antd";
import {ThemeUtils} from "../../utils";

export function PageLoading(props: { message?: string; messages?: string[] }) {
    const titles = props.messages || (props.message ? [props.message] : ['页面加载中...']);

    return <div style={{
        height: '100vh', width: '100%',
        display: 'flex', alignItems: 'center', justifyContent: "center",
        color: ThemeUtils.getColor("primary-color")
    }}>
        <div style={{textAlign: "center", marginTop: '-10rem'}}>
            <div>
                <Spin size={"large"}></Spin>
            </div>
            <div style={{marginTop: '1rem'}}>
                {titles.map((msg, i) => (
                    <Alert key={i} message={msg} type="info" showIcon style={{marginBottom: i < titles.length - 1 ? 8 : 0}} />
                ))}
            </div>
        </div>
    </div>;
}
